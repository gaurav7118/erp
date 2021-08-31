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
package com.krawler.spring.accounting.creditnote;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.*;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.CsvReader;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.esp.handlers.ServerEventManager;
import static com.krawler.esp.web.resource.Links.loginpageFull;
import com.krawler.hql.accounting.*;
import com.krawler.hql.accounting.journalentry.service.AccJournalEntryModuleService;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesCMN;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignHandler;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import com.krawler.spring.accounting.discount.accDiscountDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.invoice.accInvoiceController;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.vendor.accVendorDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.*;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.importFunctionality.ImportDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
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

/**
 *
 * @author krawler
 */
public class accCreditNoteController extends MultiActionController implements MessageSourceAware {

    private HibernateTransactionManager txnManager;
    private accCreditNoteDAO accCreditNoteDAOobj;
    private accJournalEntryDAO accJournalEntryobj;
    private accProductDAO accProductObj;
    private accDiscountDAO accDiscountobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accTaxDAO accTaxObj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private exportMPXDAOImpl exportDaoObj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private fieldDataManager fieldDataManagercntrl;
    private String successView;
    private auditTrailDAO auditTrailObj;
    private MessageSource messageSource;
    private accAccountDAO accAccountDAOobj;
    private accInvoiceDAO accInvoiceDAOObj;
    public ImportHandler importHandler;
    private ImportDAO importDao;
    public accCustomerDAO accCustomerDAOObj;
    private accCreditNoteService accCreditNoteService;
    private accVendorDAO accVendorDAOObj;
    private AccCommonTablesDAO accCommonTablesDAO;
    private AccJournalEntryModuleService journalEntryModuleServiceobj;
    
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setaccCreditNoteDAO(accCreditNoteDAO accCreditNoteDAOobj) {
        this.accCreditNoteDAOobj = accCreditNoteDAOobj;
    }

    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }

    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }

    public void setaccDiscountDAO(accDiscountDAO accDiscountobj) {
        this.accDiscountobj = accDiscountobj;
    }

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }

    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }

    public void setAccInvoiceDAO(accInvoiceDAO accInvoiceDAOObj) {
        this.accInvoiceDAOObj = accInvoiceDAOObj;
    }

    public void setAccCustomerDAO(accCustomerDAO accCustomerDAOObj) {
        this.accCustomerDAOObj = accCustomerDAOObj;
    }

    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    public void setimportDAO(ImportDAO importDao) {
        this.importDao = importDao;
    }

    public void setaccCreditNoteService(accCreditNoteService accCreditNoteService) {
        this.accCreditNoteService = accCreditNoteService;
    }

    public void setAccVendorDAO(accVendorDAO accVendorDAOObj) {
        this.accVendorDAOObj = accVendorDAOObj;
    }

    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }
    public AccJournalEntryModuleService getJournalEntryModuleServiceobj() {
        return journalEntryModuleServiceobj;
    }

    public void setJournalEntryModuleServiceobj(AccJournalEntryModuleService journalEntryModuleServiceobj) {
        this.journalEntryModuleServiceobj = journalEntryModuleServiceobj;
    }
    
    public static HashMap<String, Object> getCreditNoteMap(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put(Constants.ss, request.getParameter(Constants.ss));
        //checking filetype to print all records for csv,print & pdf
        if (StringUtil.isNullOrEmpty(request.getParameter("filetype"))) {
            requestParams.put(Constants.start, request.getParameter(Constants.start));
            requestParams.put(Constants.limit, request.getParameter(Constants.limit));
        }
        requestParams.put(CCConstants.REQ_costCenterId, request.getParameter(CCConstants.REQ_costCenterId));
        requestParams.put(Constants.REQ_startdate, request.getParameter(Constants.REQ_startdate));
        requestParams.put(Constants.REQ_enddate, request.getParameter(Constants.REQ_enddate));
        requestParams.put(Constants.Acc_Search_Json, request.getParameter(Constants.Acc_Search_Json));
        requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
        requestParams.put(Constants.moduleid, request.getParameter(Constants.moduleid));
        requestParams.put(CCConstants.accid, request.getParameter(CCConstants.accid));
        requestParams.put(Constants.userdf, authHandler.getUserDateFormatter(request));
        requestParams.put("vendorid", request.getParameter("vendorid"));
        requestParams.put("customerid", request.getParameter("customerid"));
        int cntype = StringUtil.isNullOrEmpty(request.getParameter("cntype")) ? 1 : Integer.parseInt(request.getParameter("cntype"));
        requestParams.put("cntype", cntype);
        requestParams.put("noteid", request.getParameter("noteid"));
        requestParams.put("deleted", request.getParameter("deleted"));
        requestParams.put("nondeleted", request.getParameter("nondeleted"));
        requestParams.put("pendingapproval", request.getParameter("pendingapproval"));
        requestParams.put("currencyfilterfortrans", request.getParameter("currencyfilterfortrans"));
        int transactiontype = StringUtil.isNullOrEmpty(request.getParameter("transactiontype")) ? 1 : Integer.parseInt(request.getParameter("transactiontype"));
        requestParams.put("transactiontype", transactiontype);
        requestParams.put("upperLimitDate", request.getParameter("upperLimitDate") == null ? "" : request.getParameter("upperLimitDate"));
        if (request.getParameter("isReceipt") != null) {
            requestParams.put("isReceipt", request.getParameter("isReceipt"));
        }
        if (!StringUtil.isNullOrEmpty(sessionHandlerImpl.getBrowserTZ(request))) {
            requestParams.put("browsertz", sessionHandlerImpl.getBrowserTZ(request));
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.requestModuleId))) {
            requestParams.put(Constants.requestModuleId, request.getParameter(Constants.requestModuleId));
        }
        return requestParams;
    }
    
    public static HashMap<String, Object> getCreditNoteMapJson(JSONObject paramJobj) throws SessionExpiredException, JSONException, ServiceException, UnsupportedEncodingException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParamsJson(paramJobj);
        requestParams.put(Constants.ss, paramJobj.optString(Constants.ss,null));
        //checking filetype to print all records for csv,print & pdf
        if (StringUtil.isNullOrEmpty(paramJobj.optString("filetype"))) {
            requestParams.put(Constants.start, paramJobj.optString(Constants.start,null));
            requestParams.put(Constants.limit, paramJobj.optString(Constants.limit,null));
        }
        requestParams.put(CCConstants.REQ_costCenterId,paramJobj.optString(CCConstants.REQ_costCenterId,null));
        if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.REQ_startdate))&&!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.REQ_enddate))) {
         requestParams.put(Constants.REQ_startdate, StringUtil.DecodeText(paramJobj.optString(Constants.REQ_startdate,null)));
         requestParams.put(Constants.REQ_enddate, StringUtil.DecodeText(paramJobj.optString(Constants.REQ_enddate,null)));
        }
        requestParams.put(Constants.Acc_Search_Json, paramJobj.optString(Constants.Acc_Search_Json,null));
        requestParams.put(Constants.Filter_Criteria, paramJobj.optString(Constants.Filter_Criteria,null));
        requestParams.put(Constants.moduleid, paramJobj.optString(Constants.moduleid,null));
        requestParams.put(CCConstants.accid, paramJobj.optString(CCConstants.accid,null));
        requestParams.put(Constants.userdf, authHandler.getUserDateFormatterJson(paramJobj));
        requestParams.put("vendorid", paramJobj.optString("vendorid",null));
        requestParams.put("customerid", paramJobj.optString("customerid",null));
        int cntype = StringUtil.isNullOrEmpty(paramJobj.optString("cntype",null)) ? 1 : Integer.parseInt(paramJobj.getString("cntype"));
        requestParams.put("cntype", cntype);
        requestParams.put("noteid", paramJobj.optString("noteid",null));
        requestParams.put("deleted", paramJobj.optString("deleted",null));
        requestParams.put("nondeleted", paramJobj.optString("nondeleted",null));
        requestParams.put("pendingapproval", paramJobj.optString("pendingapproval",null));
        requestParams.put("currencyfilterfortrans", paramJobj.optString("currencyfilterfortrans",null));
        int transactiontype = StringUtil.isNullOrEmpty(paramJobj.optString("transactiontype",null)) ? 1 : Integer.parseInt(paramJobj.getString("transactiontype"));
        requestParams.put("transactiontype", transactiontype);
        requestParams.put("upperLimitDate", paramJobj.optString("upperLimitDate",null) == null ? "" : paramJobj.getString("upperLimitDate"));
        if (paramJobj.optString("isReceipt",null) != null) {
            requestParams.put("isReceipt", paramJobj.optString("isReceipt",null));
        }
        if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.browsertz))) {
            requestParams.put(Constants.browsertz, paramJobj.optString(Constants.browsertz));
        }
        if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.requestModuleId,null))) {
            requestParams.put(Constants.requestModuleId, paramJobj.optString(Constants.requestModuleId));
        }
        return requestParams;
    }
    
    public ModelAndView updateCreditNote(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        boolean isAccountingExe = false;
        String channelName = "/CreditNoteReport/gridAutoRefresh";
        String sequenceformat = request.getParameter("sequenceformat");
        String creditNoteId = request.getParameter("noteid");
        String entryNumber = request.getParameter("number");
        String msg = "";
        String creditNoteNumBer = "";
        String JENumBer = "";
        KwlReturnObject result;
        String companyid = "";
        int cntype = StringUtil.isNullOrEmpty(request.getParameter("cntype")) ? 1 : Integer.parseInt(request.getParameter("cntype"));
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        String auditMsg = "", auditID = "";
        TransactionStatus status = null;
        status = txnManager.getTransaction(def);
        try {
            companyid = sessionHandlerImpl.getCompanyid(request);
            List returnList = updateCreditNote(request);

            txnManager.commit(status);
            issuccess = true;
            CreditNote creditnote = (CreditNote) returnList.get(0);
            String invoiceids = request.getParameter("invoiceids");
            creditNoteNumBer = creditnote.getCreditNoteNumber();
            JENumBer = creditnote.getJournalEntry().getEntryNumber();
            if (!StringUtil.isNullOrEmpty(creditNoteId)) {
                auditID = AuditAction.CREDIT_NOTE_MODIFIED;
                auditMsg = "updated";
            }
            auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + auditMsg + " a Credit Note " + creditNoteNumBer, request, creditnote.getID());
            if (creditnote.getApprovestatuslevel() != 11) {//pending for approval case
                String creditnoteSaved = messageSource.getMessage("acc.creditN.save", null, RequestContextUtils.getLocale(request));
                String butPendingForApproval = " " + messageSource.getMessage("acc.field.butpendingforApproval", null, RequestContextUtils.getLocale(request));
                msg = creditnoteSaved + butPendingForApproval + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + creditNoteNumBer + "</b>.";
            } else {
                msg = messageSource.getMessage("acc.creditN.save", null, RequestContextUtils.getLocale(request)) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + creditNoteNumBer + ",</b>" + messageSource.getMessage("acc.field.JENo", null, RequestContextUtils.getLocale(request)) + ": <b>" + JENumBer + "</b>";   //"Credit Note has been saved successfully";
            }
        } catch (SessionExpiredException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
                jobj.put("accException", isAccountingExe);
                if (issuccess && !StringUtil.isNullOrEmpty(channelName)) {
                    jobj.put(Constants.userSessionId, (request.getSession(true)).getAttribute(Constants.userSessionId));// adding user session id to idenntify unique user session
                    ServerEventManager.publish(channelName, jobj.toString(), (ServletContext) this.getServletContext());
                }
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return new ModelAndView("jsonView", "model", jobj.toString());

    }
    public List updateCreditNote(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException, UnsupportedEncodingException {
        String id = null;
        CreditNote creditnote = null;
        List ll = new ArrayList();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String createdby = sessionHandlerImpl.getUserid(request);
            String creditNoteId = request.getParameter("noteid");
            String modifiedby = sessionHandlerImpl.getUserid(request);
             int moduleid = Constants.Acc_Credit_Note_ModuleId;
             int approvalStatusLevel = 11;
            /*
             * To update the following items which is not affecting the amount
             * and linking of the Invoice.
             */
             HashMap<String, Object> credithm = new HashMap<String, Object>();
             if (!StringUtil.isNullOrEmpty(creditNoteId)) {
                 credithm.put("cnid", creditNoteId);
             }
            String costCenterId = !StringUtil.isNullOrEmpty(request.getParameter("costCenterId")) ? request.getParameter("costCenterId") : "";
            String salesPersonID = !StringUtil.isNullOrEmpty(request.getParameter("salesPersonID")) ? request.getParameter("salesPersonID") : "";
            credithm.put("memo", (!StringUtil.isNullOrEmpty(request.getParameter("memo"))) ? request.getParameter("memo") : "");
            credithm.put(Constants.companyKey, companyid);
            credithm.put("currencyid", currencyid);
            credithm.put("modifiedby", modifiedby);
            credithm.put("updatedon", System.currentTimeMillis());
            credithm.put("costcenter", costCenterId);
            credithm.put("salesPersonID", salesPersonID);
            
            KwlReturnObject cnObj = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), creditNoteId);
            creditnote = (CreditNote) cnObj.getEntityList().get(0);
            /*
             * Updating line item information.
             */
            String invoiceDetails = request.getParameter("details").toString();
            HashSet<InvoiceDetail> cnDetails = updateCreditNoteRows(creditnote, invoiceDetails, creditnote.getJournalEntry(), moduleid, companyid);
//             /*
//             * Updating Custom field data.
//             */
            Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
            String customfield = request.getParameter("customfield");
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                customrequestParams.put("modulerecid", creditnote.getJournalEntry().getID());
                customrequestParams.put(Constants.moduleid, moduleid);
                customrequestParams.put(Constants.companyKey, companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    jeDataMap.put("accjecustomdataref", creditnote.getJournalEntry().getID());
                    jeDataMap.put("jeid", creditnote.getJournalEntry().getID());
                    jeDataMap.put("entrydate", creditnote.getJournalEntry().getEntryDate());
                    KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);
                }
            }
            HashMap<String, Object> CNApproveMap = new HashMap<String, Object>();
            List approvedlevel = null;
            CNApproveMap.put("level", 0);//Initialy it will be 0
            CNApproveMap.put("totalAmount", String.valueOf(authHandler.round(creditnote.getCnamountinbase(), companyid)));
            CNApproveMap.put("currentUser", createdby);
            CNApproveMap.put("fromCreate", true);
            CNApproveMap.put("moduleid", Constants.Acc_Credit_Note_ModuleId);
            CNApproveMap.put(Constants.PAGE_URL, URLUtil.getPageURL(request, loginpageFull));
            CNApproveMap.put(Constants.companyKey, companyid);
            boolean isMailApplicable = false;
            approvedlevel = accCreditNoteService.approveCreditNote(creditnote, CNApproveMap, isMailApplicable);
            approvalStatusLevel = (Integer) approvedlevel.get(0);
            credithm.put("approvestatuslevel", approvalStatusLevel);
            KwlReturnObject result = accCreditNoteDAOobj.updateCreditNote(credithm);
            creditnote = (CreditNote) result.getEntityList().get(0);//Create Invoice without invoice-details.
            id = creditnote.getID();
            ll.add(creditnote);
            ll.add(id);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (Exception e) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, e);
        }
        return ll;
    }
    private HashSet<InvoiceDetail> updateCreditNoteRows(CreditNote creditnote, String creditNoteDetails, JournalEntry je, int moduleid, String companyid) throws ServiceException, JSONException {
        HashSet<InvoiceDetail> rows = new HashSet<InvoiceDetail>();
        try {
            JSONArray jArr = new JSONArray(creditNoteDetails);
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                CreditNoteTaxEntry row = null;
                if (jobj.has("rowid")) {
                    KwlReturnObject cnDetail = accountingHandlerDAOobj.getObject(CreditNoteTaxEntry.class.getName(), jobj.getString("rowid"));
                    row = (CreditNoteTaxEntry) cnDetail.getEntityList().get(0);
                }
                /*
                 * We can update the descritpion of line item.
                 */
                if (!StringUtil.isNullOrEmpty(jobj.optString("description"))) {
                    try {
                        row.setDescription(StringUtil.DecodeText(jobj.optString("description")));
                    } catch (Exception ex) {
                        row.setDescription(jobj.optString("description"));
                    }
                } else{
                    row.setDescription("");
                }
                                /*
                 * We can update the Reason of line item.
                 */
                if (!StringUtil.isNullOrEmpty(jobj.optString("reason"))) {
                    try {
                        MasterItem reason = (MasterItem) kwlCommonTablesDAOObj.getClassObject(MasterItem.class.getName(), jobj.optString("reason", ""));
                        row.setReason(reason);
                    } catch (Exception ex) {
                        MasterItem reason = (MasterItem) kwlCommonTablesDAOObj.getClassObject(MasterItem.class.getName(), jobj.optString("reason", ""));
                        row.setReason(reason);
                    }
                }
                
                if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", ""))) {
                    JSONArray jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
                    HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                    customrequestParams.put("customarray", jcustomarray);
                    customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                    customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                    customrequestParams.put("modulerecid", row.getTotalJED().getID());
                    customrequestParams.put("recdetailId", row.getID());
                    customrequestParams.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
                    customrequestParams.put(Constants.companyKey, companyid);
                    customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                    KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        JSONObject tempjedjson = new JSONObject();
                        tempjedjson.put("accjedetailcustomdata", row.getTotalJED().getID());
                        tempjedjson.put("jedid",  row.getTotalJED().getID());
                       KwlReturnObject  jedresult = accJournalEntryobj.updateJournalEntryDetails(tempjedjson);
                    }
                    /*
                     * Updating dimension value for tax entry. ERP-34578
                     */
                    if (row.getGstJED() != null) {
                        customrequestParams.put("modulerecid", row.getGstJED().getID());
                        customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            JSONObject tempjedjson = new JSONObject();
                            tempjedjson.put("accjedetailcustomdata", row.getGstJED().getID());
                            tempjedjson.put("jedid", row.getGstJED().getID());
                            KwlReturnObject jedresult = accJournalEntryobj.updateJournalEntryDetails(tempjedjson);
                        }
                    }
                    
                    JSONObject jedjson = new JSONObject();
                    HashMap<String, Object> params = new HashMap<String, Object>();
                    ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                    filter_names.add("mainjedid");
                    filter_params.add(row.getTotalJED().getID());
                    params.put("filter_names", filter_names);
                    params.put("filter_params", filter_params);

                    KwlReturnObject separatedJed = accJournalEntryobj.getJournalEntryDetails(params);
                    if (separatedJed.getEntityList() != null && separatedJed.getEntityList().size() > 0) {
                        /*
                         * Tagging new dimension value to additional jedetail
                         * against control account in linking case.
                         */
                        List<JournalEntryDetail> separatedJedList = separatedJed.getEntityList();
                        for (JournalEntryDetail separatedjed : separatedJedList) {
                            customrequestParams.put("modulerecid", separatedjed.getID()); 
                            customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                jedjson.put("accjedetailcustomdata", separatedjed.getID());
                                jedjson.put("jedid", separatedjed.getID());
                                accJournalEntryobj.updateJournalEntryDetails(jedjson);
                            }
                        }
                    } else {
                        /*
                         * If dimension value is not tagged while creating CN
                         * then separated jed doesn't get post. Post that
                         * additional jedetail and also tag dimension value.
                         */
                        
                        jedjson = new JSONObject();
                        jedjson.put("srno", 1);
                        jedjson.put("companyid", row.getTotalJED().getCompany().getCompanyID());
                        jedjson.put("amount", row.getTotalJED().getAmount());
                        jedjson.put("accountid", creditnote.getAccount().getID());
                        jedjson.put("debit", !row.getTotalJED().isDebit());
                        jedjson.put("jeid", row.getTotalJED().getJournalEntry().getID());
                        jedjson.put("mainjedid", row.getTotalJED().getID());
                        jedjson.put("description", jobj.optString("description"));
                        jedjson.put(Constants.ISSEPARATED, true);
                        KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        JournalEntryDetail pmAmountJed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        
                        customrequestParams.put("modulerecid", pmAmountJed.getID()); //Pls confirm
                        customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            jedjson.put("accjedetailcustomdata", pmAmountJed.getID());
                            jedjson.put("jedid", pmAmountJed.getID());
                            accJournalEntryobj.updateJournalEntryDetails(jedjson);
                        }
                    }

                    if (row.getGstJED() != null) {
                        /*
                         * Tagging new dimension value to additional jedetail
                         * against tax account in linking case.
                         */
                        filter_params.clear();
                        filter_params.add(row.getGstJED().getID());
                        params.put("filter_names", filter_names);
                        params.put("filter_params", filter_params);
                        separatedJed = accJournalEntryobj.getJournalEntryDetails(params);
                        if (separatedJed.getEntityList() != null && separatedJed.getEntityList().size() > 0) {
                            List<JournalEntryDetail> separatedJedList = separatedJed.getEntityList();
                            for (JournalEntryDetail separatedjed : separatedJedList) {
                                customrequestParams.put("modulerecid", separatedjed.getID()); //Pls confirm
                                customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                    jedjson.put("accjedetailcustomdata", separatedjed.getID());
                                    jedjson.put("jedid", separatedjed.getID());
                                    accJournalEntryobj.updateJournalEntryDetails(jedjson);
                                }
                            }
                        } else {
                            /*
                             * If dimension value is not tagged while creating
                             * CN then separated jed doesn't get post. Post that
                             * additional jedetail and also tag dimension value.
                             */
                            jedjson = new JSONObject();
                            jedjson.put("srno", 1);
                            jedjson.put("companyid", row.getGstJED().getCompany().getCompanyID());
                            jedjson.put("amount", row.getGstJED().getAmount());
                            jedjson.put("accountid", creditnote.getAccount().getID());
                            jedjson.put("debit", !row.getGstJED().isDebit());
                            jedjson.put("jeid", row.getGstJED().getJournalEntry().getID());
                            jedjson.put("mainjedid", row.getGstJED().getID());
                            jedjson.put("description", jobj.optString("description"));
                            jedjson.put(Constants.ISSEPARATED, true);
                            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            JournalEntryDetail pmAmountJed = (JournalEntryDetail) jedresult.getEntityList().get(0);

                            customrequestParams.put("modulerecid", pmAmountJed.getID()); //Pls confirm
                            customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                jedjson.put("accjedetailcustomdata", pmAmountJed.getID());
                                jedjson.put("jedid", pmAmountJed.getID());
                                accJournalEntryobj.updateJournalEntryDetails(jedjson);
                            }
                        }
                    }

                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("updateInvoiceRows : " + ex.getMessage(), ex);
        } catch (Exception e) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, e);
        }
        return rows;
    }
    public ModelAndView saveCreditNote(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String channelName = "";
        try {
            /*Get request parameters */
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            /*Call to Save Credit Note */
            jobj = accCreditNoteService.saveCreditNoteJSON(paramJobj);
            issuccess = jobj.optBoolean(Constants.RES_success, false);
            channelName = jobj.optString(Constants.channelName, null);
            jobj.put(Constants.userSessionId, (request.getSession(true)).getAttribute(Constants.userSessionId));// adding user session id to idenntify unique user session
        } catch (Exception ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
                if (issuccess && !StringUtil.isNullOrEmpty(channelName)) {
                    ServerEventManager.publish(channelName, jobj.toString(), (ServletContext) this.getServletContext());
                }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /**
     * This Method is used to Import Credit Note
     * @param request
     * @param response
     * @return 
     */
    public ModelAndView importCreditNotes(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            /* Get Import related global parameters */
            JSONObject paramJobj = getimportCreditNoteParams(request);
            /* Call validate and import data of Credit Note. */
            jobj = accCreditNoteService.importCreditNotesJSON(paramJobj);
        } catch (Exception ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    /**
     * Description : This Method is used to Get Request params for import Credit Note
     * @param request
     * @return JSONObject
     * @throws JSONException
     * @throws SessionExpiredException 
     */
    public JSONObject getimportCreditNoteParams(HttpServletRequest request) throws JSONException, SessionExpiredException {
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        paramJobj.put("servletContext", this.getServletContext());
        return paramJobj;
    }
    
    public ModelAndView importOpeningBalanceCustomerCNs(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            boolean isCustomer=true;
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
            requestParams.put("moduleName", "Opening Customer Credit Note");
            requestParams.put(Constants.moduleid, Constants.Acc_opening_Customer_CreditNote);
            requestParams.put("bookbeginning", preferences.getBookBeginningFrom());
            
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

                jobj = importOeningTransactionsRecords(request, datajobj,isCustomer);
            }else if (doAction.compareToIgnoreCase("validateData") == 0) {
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
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }

            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView importOpeningBalanceVendorCNs(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            boolean isCustomer=false;
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
            requestParams.put("moduleName", "Opening Vendor Credit Note");
            requestParams.put(Constants.moduleid, Constants.Acc_opening_Vendor_CreditNote);
            requestParams.put("bookbeginning", preferences.getBookBeginningFrom());
            
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

                jobj = importOeningTransactionsRecords(request, datajobj,isCustomer);
            }else if (doAction.compareToIgnoreCase("validateData") == 0) {
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
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }

            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /*
     * Method to save Opening Balance CN For customer.
     */
    public ModelAndView saveOpeningBalanceCN(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject jobjDetails = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean issuccess = false;
        boolean isAccountingExe=false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("BR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;//txnManager.getTransaction(def);
        String cnNumber = request.getParameter("number");
        String companyid ="";
        try {
            companyid = sessionHandlerImpl.getCompanyid(request);
            String cnId = request.getParameter("transactionId");
            KwlReturnObject cncount=null;
            if (StringUtil.isNullOrEmpty(cnId)) {//Add case checks duplicate and sequence fomat number
                //code form duplicate number
                cncount = accCreditNoteDAOobj.getCNFromNoteNo(cnNumber, companyid);
                if (cncount.getRecordTotalCount() > 0) {
                    isAccountingExe=true;
                    throw new AccountingException(messageSource.getMessage("acc.CN.creditnoteno", null, RequestContextUtils.getLocale(request)) + cnNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                }
                
                //code for checking wheather entered number can be generated by sequence format or not
                List resultList = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Credit_Note_ModuleId, cnNumber, companyid);
                if (!resultList.isEmpty()) {
                    boolean isvalidEntryNumber = (Boolean) resultList.get(0);
                    String formatName = (String) resultList.get(1);
                    if (!isvalidEntryNumber) {
                        isAccountingExe=true;
                        throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, RequestContextUtils.getLocale(request)) + " <b>" + cnNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, RequestContextUtils.getLocale(request)) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.pleaseentersomeothernumber", null, RequestContextUtils.getLocale(request)));
                    }
                }
            }
            
            synchronized (this) {//Checks duplicate number for simultaneous transactions
                status = txnManager.getTransaction(def);
                    KwlReturnObject resultInv = accCommonTablesDAO.getTransactionInTemp(cnNumber, companyid, Constants.Acc_Credit_Note_ModuleId);//Get entry from temporary table
                    if (resultInv.getRecordTotalCount() > 0) {
                        isAccountingExe=true;
                        throw new AccountingException(messageSource.getMessage("acc.CN.selectedcreditnoteno", null, RequestContextUtils.getLocale(request)) + cnNumber + messageSource.getMessage("acc.field.alreadyinprocess.", null, RequestContextUtils.getLocale(request)));
                    } else {
                        accCommonTablesDAO.insertTransactionInTemp(cnNumber, companyid, Constants.Acc_Credit_Note_ModuleId);//Insert entry in temporary table
                    }
                txnManager.commit(status);
            }
            status = txnManager.getTransaction(def);
            List li = saveOpeningBalanceCN(request);
            if (!li.isEmpty()) {
                cnNumber = li.get(0).toString();
            }

            issuccess = true;
            msg = messageSource.getMessage("acc.accPref.autoCN", null, RequestContextUtils.getLocale(request)) + " " + cnNumber + " " + messageSource.getMessage("acc.field.hasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
            status = txnManager.getTransaction(def);
            accCommonTablesDAO.deleteTransactionInTemp(cnNumber, companyid,Constants.Acc_Credit_Note_ModuleId);//Delete entry in temporary table
            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            if(status!=null){
                txnManager.rollback(status);
            }
            try {
                accCommonTablesDAO.deleteTransactionInTemp(cnNumber, companyid,Constants.Acc_Credit_Note_ModuleId);//Delete entry in temporary table
            } catch (ServiceException ex1) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            if(status!=null){
                txnManager.rollback(status);
            }
            try {
                accCommonTablesDAO.deleteTransactionInTemp(cnNumber, companyid,Constants.Acc_Credit_Note_ModuleId);//Delete entry in temporary table
            } catch (ServiceException ex1) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            if(status!=null){
                txnManager.rollback(status);
            }
            try {
                accCommonTablesDAO.deleteTransactionInTemp(cnNumber, companyid,Constants.Acc_Credit_Note_ModuleId);//Delete entry in temporary table
            } catch (ServiceException ex1) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
                jobj.put("data", jArr);
                jobj.put("accException", isAccountingExe);
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public List saveOpeningBalanceCN(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        List returnList = new ArrayList();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            KwlReturnObject result;
            boolean isEditInvoice = false;
            String auditMsg = "", auditID = "", memo="";
            auditMsg = "added";
            auditID = AuditAction.OPENING_BALANCE_CREATED;
            // Fetching request parameters

            String cnNumber = request.getParameter("number");
            String transactionDateStr = request.getParameter("billdate");
            String currencyid = (request.getParameter("currencyid") == null ? sessionHandlerImpl.getCurrencyID(request) : request.getParameter("currencyid"));
            String transactionAmountStr = request.getParameter("transactionAmount");
            String cnId = request.getParameter("transactionId");
            String narrationValue = request.getParameter("narration");
            String customerId = request.getParameter("accountId");
            boolean conversionRateFromCurrencyToBase = true;
            if (request.getParameter("CurrencyToBaseExchangeRate") != null) {
                conversionRateFromCurrencyToBase = Boolean.parseBoolean(request.getParameter("CurrencyToBaseExchangeRate"));
            }
            double externalCurrencyRate = StringUtil.getDouble(request.getParameter("externalcurrencyrate"));

            double exchangeRateForOpeningTransaction = 1;
            if (!StringUtil.isNullOrEmpty(request.getParameter("exchangeRateForOpeningTransaction"))) {
                exchangeRateForOpeningTransaction = Double.parseDouble(request.getParameter("exchangeRateForOpeningTransaction"));
            }

            Date transactionDate = df.parse(df.format(new Date()));
            Date chequeDate = df.parse(df.format(new Date()));

            if (!StringUtil.isNullOrEmpty(transactionDateStr)) {
                transactionDate = df.parse(transactionDateStr);
            }

            String accountId = "";

            if (!StringUtil.isNullOrEmpty(customerId)) {
                KwlReturnObject custresult = accountingHandlerDAOobj.getObject(Customer.class.getName(), customerId);
                Customer customer = (Customer) custresult.getEntityList().get(0);
                accountId = customer.getAccount().getID();
            }

            double transactionAmount = 0d;
            if (!StringUtil.isNullOrEmpty(transactionAmountStr)) {
                transactionAmount = Double.parseDouble(transactionAmountStr);
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("memo"))) {
                memo = request.getParameter("memo").toString();
            }

            // creating cn data

            HashMap<String, Object> credithm = new HashMap<String, Object>();

            if (StringUtil.isNullOrEmpty(cnId)) {
                result = accCreditNoteDAOobj.getCNFromNoteNo(cnNumber, companyid);
                int count = result.getRecordTotalCount();
                if (count > 0) {
                    throw new AccountingException("Credit Note number '" + cnNumber + "' already exists.");
                }
                credithm.put("entrynumber", cnNumber);
                credithm.put("autogenerated", false);
            }


            if (!StringUtil.isNullOrEmpty(cnId)) {
                // check whether CN is linked to  invoice or payment. if yes don't let it edit
                boolean isNoteLinkedWithPayment = accCreditNoteService.isNoteLinkedWithPayment(cnId);
                if (isNoteLinkedWithPayment) {
                    throw new AccountingException(messageSource.getMessage("acc.field.SelectedCreditNoteisLinkedWithPaymentsoitcannotbeedit", null, RequestContextUtils.getLocale(request)));
                }

                boolean isNoteLinkedWithInvoice = isNoteLinkedWithInvoice(cnId, companyid);
                if (isNoteLinkedWithInvoice) {
                    throw new AccountingException(messageSource.getMessage("acc.field.SelectedCreditNoteisLinkedWithInvoicesoitcannotbeedit", null, RequestContextUtils.getLocale(request)));
                }
                KwlReturnObject deleteresult= accCreditNoteDAOobj.deleteCreditNoteDetails(cnId, companyid);


                isEditInvoice = true;
                auditMsg = "updated";
                auditID = AuditAction.OPENING_BALANCE_UPDATED;
                credithm.put("cnid", cnId);
            }

            credithm.put("cnamount", transactionAmount);//
            credithm.put("currencyid", currencyid);//
            credithm.put("externalCurrencyRate", externalCurrencyRate);//
            credithm.put("memo", memo);//
            credithm.put(Constants.companyKey, companyid);//
            credithm.put("narrationValue", narrationValue);//
            credithm.put("creationDate", transactionDate);//
            credithm.put("customerid", customerId);//
            credithm.put("accountId", accountId);//
            credithm.put("isOpeningBalenceCN", true);
            credithm.put("normalCN", false);//
            credithm.put("openingBalanceAmountDue", transactionAmount);//
            credithm.put("exchangeRateForOpeningTransaction", exchangeRateForOpeningTransaction);//
            credithm.put("isCNForCustomer", true);//
            credithm.put("openflag", true);
            credithm.put("otherwise", true);
            credithm.put("cnamountdue", transactionAmount);
            credithm.put("conversionRateFromCurrencyToBase", conversionRateFromCurrencyToBase);
            credithm.put("approvestatuslevel", 11);
            
            // Store CN amount in base currency
            if (conversionRateFromCurrencyToBase) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                credithm.put(Constants.originalOpeningBalanceBaseAmount, authHandler.round(transactionAmount*exchangeRateForOpeningTransaction, companyid));
                credithm.put(Constants.openingBalanceBaseAmountDue, authHandler.round(transactionAmount*exchangeRateForOpeningTransaction, companyid));
            } else {
                credithm.put(Constants.originalOpeningBalanceBaseAmount, authHandler.round(transactionAmount/exchangeRateForOpeningTransaction, companyid));
                credithm.put(Constants.openingBalanceBaseAmountDue, authHandler.round(transactionAmount/exchangeRateForOpeningTransaction, companyid));
            }
            
            if (isEditInvoice) {
                String modifiedby = sessionHandlerImpl.getUserid(request);
                long updatedon = System.currentTimeMillis();
                credithm.put("modifiedby", modifiedby);
                credithm.put("updatedon", updatedon);

                result = accCreditNoteDAOobj.updateCreditNote(credithm);
            } else {
                String createdby = sessionHandlerImpl.getUserid(request);
                String modifiedby = sessionHandlerImpl.getUserid(request);
                long createdon = System.currentTimeMillis();
                long updatedon = System.currentTimeMillis();

                credithm.put("createdby", createdby);
                credithm.put("modifiedby", modifiedby);
                credithm.put("createdon", createdon);
                credithm.put("updatedon", updatedon);

                result = accCreditNoteDAOobj.addCreditNote(credithm);
            }

            CreditNote cn = (CreditNote) result.getEntityList().get(0);

            HashSet<CreditNoteDetail> cndetails = new HashSet<CreditNoteDetail>();

            getCNDetails(cndetails, companyid);

            Iterator itr = cndetails.iterator();
            while (itr.hasNext()) {
                CreditNoteDetail cnd = (CreditNoteDetail) itr.next();
                cnd.setCreditNote(cn);
            }
            credithm.put("cnid", cn.getID());
            credithm.put("cndetails", cndetails);

            accCreditNoteDAOobj.updateCreditNote(credithm);

            returnList.add(cn.getCreditNoteNumber());
            
            String customfield = request.getParameter("customfield");
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_OpeningBalanceCreditNote_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_OpeningBalanceCreditNoteid);
                customrequestParams.put("modulerecid", cn.getID());
                customrequestParams.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
                customrequestParams.put(Constants.companyKey, companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_OpeningBalanceCreditNote_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    credithm.put("cnid", cn.getID());
                    credithm.put("openingBalanceCreditNoteCustomData", cn.getID());
                    result = accCreditNoteDAOobj.updateCreditNote(credithm);
                }
            }
            
            auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has  " + auditMsg + " an Opening Balance Credit Note " + cnNumber, request, cnNumber);

        } catch (JSONException ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ParseException ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return returnList;
    }

    private void getCNDetails(HashSet<CreditNoteDetail> cndetails, String companyId) throws ServiceException {

        KwlReturnObject result = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
        Company company = (Company) result.getEntityList().get(0);

        CreditNoteDetail row = new CreditNoteDetail();
        String CreditNoteDetailID = StringUtil.generateUUID();
        row.setID(CreditNoteDetailID);
        row.setSrno(1);
        row.setTotalDiscount(0.00);
        row.setCompany(company);
        row.setMemo("");
        cndetails.add(row);
    }

    private Vendor getVendorByCode(String vendorCode, String companyID) throws AccountingException {
        Vendor vendor = null;
        try {
            if (!StringUtil.isNullOrEmpty(vendorCode) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accVendorDAOObj.getVendorByCode(vendorCode, companyID);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    vendor = (Vendor) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Vendor");
        }
        return vendor;
    }

    public HashMap getCurrencyMap(boolean isCurrencyCode) throws ServiceException {
        HashMap currencyMap = new HashMap();
        KwlReturnObject returnObject = accCurrencyDAOobj.getCurrencies(currencyMap);
        List currencyList = returnObject.getEntityList();
        if (currencyList != null && !currencyList.isEmpty()) {
            Iterator iterator = currencyList.iterator();
            while (iterator.hasNext()) {
                KWLCurrency currency = (KWLCurrency) iterator.next();
                if(isCurrencyCode){
                    currencyMap.put(currency.getCurrencyCode(), currency.getCurrencyID());
                }else{
                currencyMap.put(currency.getName(), currency.getCurrencyID());
                }
            }
        }
        return currencyMap;
    }

    private String getCurrencyId(String currencyName, HashMap currencyMap) {
        String currencyId = "";
        if (currencyMap != null && currencyMap.containsKey(currencyName)) {
            currencyId = currencyMap.get(currencyName).toString();
        }
        return currencyId;
    }

    private Customer getCustomerByCode(String customerCode, String companyID) throws AccountingException {
        Customer customer = null;
        try {
            if (!StringUtil.isNullOrEmpty(customerCode) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accCustomerDAOObj.getCustomerByCode(customerCode, companyID);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    customer = (Customer) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching customer");
        }
        return customer;
    }

    public JSONObject importOeningTransactionsRecords(HttpServletRequest request, JSONObject jobj, boolean isCustomer) throws AccountingException, IOException, SessionExpiredException, JSONException {

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("import_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        boolean commitedEx = false;
        boolean issuccess = true;
        String msg = "";
        FileInputStream fileInputStream = null;
        CsvReader csvReader=null;
        int total = 0, failed = 0;
        String companyid = sessionHandlerImpl.getCompanyid(request);
        String gcurrencyId = sessionHandlerImpl.getCurrencyID(request);
        String userId = sessionHandlerImpl.getUserid(request);
        String fileName = jobj.getString("filename");
        String masterPreference = request.getParameter("masterPreference");
        String delimiterType = request.getParameter("delimiterType");
        String customfield = "";
        HashMap<String, FieldParams> customFieldParamMap = new HashMap<String, FieldParams>();

        JSONObject returnObj = new JSONObject();

        try {
            
            String dateFormat = "yyyy-MM-dd", dateFormatId = request.getParameter("dateFormat");
            if (!StringUtil.isNullOrEmpty(dateFormatId)) {

                KwlReturnObject kdfObj = accountingHandlerDAOobj.getObject(KWLDateFormat.class.getName(), dateFormatId);
                KWLDateFormat kdf = (KWLDateFormat) kdfObj.getEntityList().get(0);
                if (kdf != null) {
                    dateFormat = kdf.getJavaForm();
                }
            }
            DateFormat df = new SimpleDateFormat(dateFormat);
            DateFormat datef = authHandler.getDateOnlyFormat();
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            KwlReturnObject extraPref = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extrareferences = (ExtraCompanyPreferences) extraPref.getEntityList().get(0);
            Boolean isCurrencyCode=extrareferences.isCurrencyCode();
            fileInputStream = new FileInputStream(jobj.getString("FilePath"));
            csvReader = new CsvReader(new InputStreamReader(fileInputStream), delimiterType);
            String record = "";
            int cnt = 0;

            double externalCurrencyRate = 0d;//StringUtil.getDouble(request.getParameter("externalcurrencyrate"));

            StringBuilder failedRecords = new StringBuilder();

            HashMap<String, Integer> columnConfig = new HashMap<String, Integer>();
            Map<String, JSONObject> configMap = new HashMap<>();
            JSONArray jSONArray = jobj.getJSONArray("resjson");
            List headArrayList = new ArrayList();

            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
                headArrayList.add(jSONObject.get("csvheader"));
                columnConfig.put(jSONObject.getString("columnname"), jSONObject.getInt("csvindex"));
                configMap.put(jSONObject.getString("columnname"), jSONObject);
            }
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            HashMap currencyMap = getCurrencyMap(isCurrencyCode);
            Set transactionNumberSet = new HashSet();

            while (csvReader.readRecord()) {
                String failureMsg = "";
                String[] recarr = csvReader.getValues();
                if(cnt==0){//Putting Header in failure File
                    failedRecords.append(createCSVrecord(recarr) + "\"Error Message\"");//failedRecords.append("\"Row No.\","+createCSVrecord(fileData)+"\"Error Message\""); 
                }else {
                    String accountId = "";
                    String customerId = "";
                    String vendorId = "";
                    try {
                        
                        /*1. CN Number : This is unique key it should be cheked first. if validation failed then no need to check for other cases.*/
                        String invoiceNumber="";
                        if (columnConfig.containsKey("CreditNoteNumber")) {
                            invoiceNumber = recarr[(Integer) columnConfig.get("CreditNoteNumber")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(invoiceNumber)) {
                                throw new AccountingException("Empty data found in Transaction Number, cannot set empty data for Transaction Number.");
                            } else if (!transactionNumberSet.add(invoiceNumber)) {// this method retur true when added or false when already exit record not get added
                                throw new AccountingException("Duplicate Transaction Number '" + invoiceNumber + "' in file.");
                            } else {
                                KwlReturnObject result = accCreditNoteDAOobj.getCNFromNoteNo(invoiceNumber, companyid);
                                int count = result.getRecordTotalCount();
                                if (count > 0) {
                                    throw new AccountingException("Credit Note number '" + invoiceNumber + "' already exists.");
                                }
                            }
                            
                            JSONObject configObj = configMap.get("CreditNoteNumber");
                            int maxLength = configObj.optInt("maxLength", 0);
                            String validationType = configObj.optString("validatetype");
                            if ("string".equalsIgnoreCase(validationType) && !StringUtil.isNullOrEmpty(invoiceNumber) && invoiceNumber.length() > maxLength) {
                                if (masterPreference.equalsIgnoreCase("0")) {
                                    failureMsg += "Data length greater than " + maxLength + " for column Transaction Number.";
                                } else {// for other two cases need to trim data upto max length
                                    invoiceNumber = invoiceNumber.substring(0, maxLength);
                                }
                            }
                        } else {
                            throw new AccountingException(messageSource.getMessage("acc.field.TransactionNumberisnotavailable", null, RequestContextUtils.getLocale(request)));
                        }    
                        
                        if (isCustomer) {//it will be true for Customer Credit Note and false for Vendor Credit Note
                            /*
                             * 2. Customer Code
                             */
                            if (columnConfig.containsKey("CustomerCode")) {
                                String customerCode = recarr[(Integer) columnConfig.get("CustomerCode")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(customerCode)) {
                                    Customer customer = getCustomerByCode(customerCode, companyid);
                                    if (customer != null) {
                                        accountId = customer.getAccount().getID();
                                        customerId = customer.getID();
                                    } else {
                                        if (masterPreference.equalsIgnoreCase("0")) { //Skip Record
                                            failureMsg += "Customer Code entry not found in master list for Customer Code dropdown.";
                                        } else if (masterPreference.equalsIgnoreCase("1")) {
                                            failureMsg += "Customer Code entry not found in master list for Customer Code dropdown, cannot set empty data for Customer Code.";
                                        } else if (masterPreference.equalsIgnoreCase("2")) {
                                            failureMsg += "Customer Code entry not present in Customer list, Please create new Customer entry for Customer Code as it requires some other details.";
                                        }
                                    }
                                }
                            }

                            /*
                             * 3. Customer Name if customerID is empty it menas
                             * customer is not found for given code. so need to
                             * serch data on name
                             */
                            if (StringUtil.isNullOrEmpty(customerId)) {
                                if (columnConfig.containsKey("CustomerName")) {
                                    String customerName = recarr[(Integer) columnConfig.get("CustomerName")].replaceAll("\"", "").trim();
                                    if (!StringUtil.isNullOrEmpty(customerName)) {
                                        Customer customer = null;
                                        KwlReturnObject retObj = accCustomerDAOObj.getCustomerByName(customerName, companyid);
                                        if (retObj != null && !retObj.getEntityList().isEmpty()) {
                                            customer = (Customer) retObj.getEntityList().get(0);
                                        }
                                        if (customer != null) {
                                            accountId = customer.getAccount().getID();
                                            customerId = customer.getID();
                                        } else {
                                            failureMsg +=messageSource.getMessage("acc.field.CustomerisnotfoundforCustomerCodeName", null, RequestContextUtils.getLocale(request));
                                        }
                                    } else {
                                        failureMsg+=messageSource.getMessage("acc.field.CustomerisnotfoundforCustomerCodeName", null, RequestContextUtils.getLocale(request));
                                    }
                                } else {
                                    failureMsg+=messageSource.getMessage("acc.field.CustomerisnotfoundforCustomerCodeName", null, RequestContextUtils.getLocale(request));
                                }
                            }
                        } else {
                            /*
                             * 2. Vendor Code
                             */
                            String vendorCode = "";
                            if (columnConfig.containsKey("VendorCode")) {
                                vendorCode = recarr[(Integer) columnConfig.get("VendorCode")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(vendorCode)) {
                                    Vendor vendor = getVendorByCode(vendorCode, companyid);
                                    if (vendor != null) {
                                        accountId = vendor.getAccount().getID();
                                        vendorId = vendor.getID();
                                    } else {
                                        if (masterPreference.equalsIgnoreCase("0")) { //Skip Record
                                            failureMsg += "Vendor Code entry not found in master list for Vendor Code dropdown.";
                                        } else if (masterPreference.equalsIgnoreCase("1")) {
                                            failureMsg += "Vendor Code entry not found in master list for Vendor Code dropdown, cannot set empty data for Vendor Code.";
                                        } else if (masterPreference.equalsIgnoreCase("2")) {
                                            failureMsg += "Vendor Code entry not present in Vendor list, Please create new Vendor entry for Vendor Code as it requires some other details.";
                                        }
                                    }
                                }
                            }

                            /*
                             * 3. Vendor Name if vendorID is empty it menas
                             * vendor is not found for given code. so need to
                             * serch data on name
                             */
                            String vendorName = "";
                            if (StringUtil.isNullOrEmpty(vendorId)) {
                                if (columnConfig.containsKey("VendorName")) {
                                    vendorName = recarr[(Integer) columnConfig.get("VendorName")].replaceAll("\"", "").trim();
                                    if (!StringUtil.isNullOrEmpty(vendorName)) {
                                        Vendor vendor = null;
                                        KwlReturnObject retObj = accVendorDAOObj.getVendorByName(vendorName, companyid);
                                        if (retObj != null && !retObj.getEntityList().isEmpty()) {
                                            vendor = (Vendor) retObj.getEntityList().get(0);
                                        }
                                        if (vendor != null) {
                                            accountId = vendor.getAccount().getID();
                                            vendorId = vendor.getID();
                                        } else {
                                            failureMsg +=messageSource.getMessage("acc.field.VendorisnotfoundforVendorCodeName", null, RequestContextUtils.getLocale(request));
                                        }
                                    } else {
                                        failureMsg +=messageSource.getMessage("acc.field.VendorisnotfoundforVendorCodeName", null, RequestContextUtils.getLocale(request));
                                    }
                                } else {
                                    failureMsg +=messageSource.getMessage("acc.field.VendorisnotfoundforVendorCodeName", null, RequestContextUtils.getLocale(request));
                                }
                            }
                        }
                        
                        /*4. Creation Date*/
                        String transactionDateStr = "";
                        Date transactionDate = null, bookbeginningdate = null;
                        if (columnConfig.containsKey("CreationDate")) {
                            transactionDateStr = recarr[(Integer) columnConfig.get("CreationDate")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(transactionDateStr)) {
                                try {
                                    // In UI we are not allowing user to give transaction date  on or after book beginning date
                                    // below code is for the same purpose  
                                    transactionDate = df.parse(transactionDateStr);
                                    transactionDate = CompanyPreferencesCMN.removeTimefromDate(transactionDate);
                                    bookbeginningdate = CompanyPreferencesCMN.removeTimefromDate(preferences.getBookBeginningFrom());
                                    if (transactionDate.after(bookbeginningdate) || transactionDate.equals(bookbeginningdate)) {
                                        failureMsg += messageSource.getMessage("acc.transactiondate.beforebbdate", null, RequestContextUtils.getLocale(request));
                                    }
                                    CompanyPreferencesCMN.checkLockPeriod(accCompanyPreferencesObj, requestParams, transactionDate, true);
                                } catch (ParseException ex) {
                                    failureMsg +="Incorrect date format for Transaction Date, Please specify values in " + dateFormat + " format.";
                                } catch (Exception ex){
                                    failureMsg+=ex.getMessage();
                                }
                            } else {
                                failureMsg +=messageSource.getMessage("acc.field.TransactionDateisnotavailable", null, RequestContextUtils.getLocale(request));
                            }
                        } else {
                            failureMsg +=messageSource.getMessage("acc.field.TransactionDateisnotavailable", null, RequestContextUtils.getLocale(request));
                        }

                        /*5. CN Currency */
                        String currencyId = "";
                        if (isCurrencyCode?columnConfig.containsKey("currencyCode"):columnConfig.containsKey("Currency")) {
                            String currencyStr = recarr[isCurrencyCode?(Integer) columnConfig.get("currencyCode"):(Integer) columnConfig.get("Currency")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(currencyStr)) {
                                failureMsg +="Empty data found in Currency, cannot set empty data for Currency.";
                            } else {
                                currencyId = getCurrencyId(currencyStr, currencyMap);
                                if (StringUtil.isNullOrEmpty(currencyId)) {
                                    if (masterPreference.equalsIgnoreCase("0")) {// most restricted case
                                        failureMsg += "Currency entry not found in master list for Currency dropdown.";
                                    } else if (masterPreference.equalsIgnoreCase("1")) {// add empty value if data invalid for non mandatory
                                        failureMsg += "Currency entry not found in master list for Currency dropdown, cannot set empty data for Currency.";
                                    } else if (masterPreference.equalsIgnoreCase("2")) {//add new (most lanient)
                                        failureMsg += "Currency entry not present in Currency list, Please create new Currency entry for "+currencyStr+" as it requires some other details.";
                                    }
                                }
                            }
                        } else {
                            failureMsg +=messageSource.getMessage("acc.field.Currencyisnotavailable", null, RequestContextUtils.getLocale(request));
                        } 
                        
                        
                        /*6. Amount*/
                        String transactionAmountStr = "";
                        double transactionAmount = 0d;
                        if (columnConfig.containsKey("Amount")) {
                            transactionAmountStr = recarr[(Integer) columnConfig.get("Amount")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(transactionAmountStr)) {
                                failureMsg += "Empty data found in Amount, cannot set empty data for Amount.";
                            } else {
                                try{
                                    transactionAmount = Double.parseDouble(transactionAmountStr);
                                    if (transactionAmount <= 0) {
                                        failureMsg += "Amount can not be zero or negative.";
                                    }
                                } catch(NumberFormatException ex){
                                    failureMsg+="Incorrect numeric value for Amount, Please ensure that value type of Amount matches with the Amount.";
                                }
                            }
                        } else {
                            failureMsg+=messageSource.getMessage("acc.field.TransactionAmountisnotavailable", null, RequestContextUtils.getLocale(request));
                        }
                        
                        /*7. Exchange Rate */
                        String exchangeRateForOpeningTransactionStr = "";
                        if (columnConfig.containsKey("ExchangeRateForOpeningTransaction")) {
                            exchangeRateForOpeningTransactionStr = recarr[(Integer) columnConfig.get("ExchangeRateForOpeningTransaction")].replaceAll("\"", "").trim();
                        } 
                        
                        double exchangeRateForOpeningTransaction = 1;
                        if (!StringUtil.isNullOrEmpty(exchangeRateForOpeningTransactionStr)) {
                            try {
                                exchangeRateForOpeningTransaction = Double.parseDouble(exchangeRateForOpeningTransactionStr);
                                if (exchangeRateForOpeningTransaction <= 0) {
                                    failureMsg += messageSource.getMessage("acc.field.ExchangeRateCannotbezeroornegative", null, RequestContextUtils.getLocale(request));
                                }
                            } catch (NumberFormatException ex) {
                                failureMsg += "Incorrect numeric value for Exchange Rate, Please ensure that value type of Exchange Rate matches with the Exchange Rate.";
                            }
                        } else {
                            Map<String, Object> currMap = new HashMap<String, Object>();
                            Date finYrStartDate = preferences.getFinancialYearFrom();
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(finYrStartDate);
                            cal.add(Calendar.DATE, -1);

                            Date applyDate = cal.getTime();
                            String date=datef.format(applyDate);
                            try{
                                applyDate=datef.parse(date);
                            }catch(ParseException ex){
                                applyDate = cal.getTime();
                            }
                            currMap.put("applydate", applyDate);
                            currMap.put("gcurrencyid", gcurrencyId);
                            currMap.put(Constants.companyKey, companyid);
                            KwlReturnObject retObj = accCurrencyDAOobj.getExcDetailID(currMap, currencyId, applyDate, null);
                            if (retObj != null) {
                                List li = retObj.getEntityList();
                                if (!li.isEmpty()) {
                                    Iterator itr = li.iterator();
                                    ExchangeRateDetails erd = (ExchangeRateDetails) itr.next();
                                    if (erd != null) {
                                        exchangeRateForOpeningTransaction = erd.getExchangeRate();
                                    }
                                }
                            }
                        }
                        
                        /*8. Narration*/
                        String narrationValue = "";
                        if (columnConfig.containsKey("Narration")) {
                            narrationValue = recarr[(Integer) columnConfig.get("Narration")].replaceAll("\"", "").trim();
                        }

                        /*
                         * 9. Memo
                         */
                        String memo = "";
                        if (columnConfig.containsKey("Memo")) {
                            memo = recarr[(Integer) columnConfig.get("Memo")].replaceAll("\"", "").trim();

                            JSONObject configObj = configMap.get("Memo");
                            int maxLength = configObj.optInt("maxLength", 0);
                            String validationType = configObj.optString("validatetype");
                            if ("string".equalsIgnoreCase(validationType) && !StringUtil.isNullOrEmpty(memo) && memo.length() > maxLength) {
                                if (masterPreference.equalsIgnoreCase("0")) {
                                    failureMsg += "Data length greater than " + maxLength + " for column Memo.";
                                } else {// for other two cases need to trim data upto max length
                                    memo = memo.substring(0, maxLength);
                                }
                            }
                        }
                        if (!StringUtil.isNullOrEmpty(failureMsg)) {
                            throw new AccountingException(failureMsg);
                        }
                        
                        if (customFieldParamMap.isEmpty()) {
                            for (int K = 0; K < headArrayList.size(); K++) {
                                HashMap<String, Object> requestParamsCF = new HashMap<String, Object>();
                                requestParamsCF.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.fieldlabel));
                                requestParamsCF.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Credit_Note_ModuleId, headArrayList.get(K)));
                                KwlReturnObject fieldParamsResult = accCommonTablesDAO.getFieldParams(requestParamsCF); // get custom field for module
                                FieldParams params = null;
                                if (fieldParamsResult.getRecordTotalCount() > 0) {
                                    params = (FieldParams) fieldParamsResult.getEntityList().get(0);
                                    customFieldParamMap.put(headArrayList.get(K).toString(), params);
                                }
                            }
                        }
                        
                        // creating cn data

                        HashMap<String, Object> credithm = new HashMap<String, Object>();
                        credithm.put("entrynumber", invoiceNumber);
                        credithm.put("autogenerated", false);


                        credithm.put("cnamount", transactionAmount);//
                        credithm.put("currencyid", currencyId);//
                        credithm.put("externalCurrencyRate", externalCurrencyRate);//
                        credithm.put("memo", memo);//
                        credithm.put(Constants.companyKey, companyid);//
                        credithm.put("narrationValue", narrationValue);//
                        credithm.put("creationDate", transactionDate);//
                        if (isCustomer) {
                            credithm.put("customerid", customerId);//
                            credithm.put("isCNForCustomer", true);//
                        } else {
                            credithm.put("vendorid", vendorId);//
                            credithm.put("isCNForCustomer", false);//
                        }
                        credithm.put("accountId", accountId);//
                        credithm.put("isOpeningBalenceCN", true);
                        credithm.put("normalCN", false);//
                        credithm.put("openingBalanceAmountDue", transactionAmount);//
                        credithm.put("exchangeRateForOpeningTransaction", exchangeRateForOpeningTransaction);//
                        credithm.put("conversionRateFromCurrencyToBase", true);//
                        // Store CN amount in base currency. conversionRateFromCurrencyToBase is always true for import case
                        credithm.put(Constants.originalOpeningBalanceBaseAmount, authHandler.round(transactionAmount*exchangeRateForOpeningTransaction, companyid));
                        credithm.put(Constants.openingBalanceBaseAmountDue, authHandler.round(transactionAmount*exchangeRateForOpeningTransaction, companyid));
                        credithm.put("openflag", true);
                        credithm.put("cnamountdue", transactionAmount);

                        String createdby = sessionHandlerImpl.getUserid(request);
                        String modifiedby = sessionHandlerImpl.getUserid(request);
                        long createdon = System.currentTimeMillis();
                        long updatedon = System.currentTimeMillis();

                        credithm.put("createdby", createdby);
                        credithm.put("modifiedby", modifiedby);
                        credithm.put("createdon", createdon);
                        credithm.put("updatedon", updatedon);
                        credithm.put("approvestatuslevel", 11);

                        KwlReturnObject result = accCreditNoteDAOobj.addCreditNote(credithm);

                        CreditNote cn = (CreditNote) result.getEntityList().get(0);

                        HashSet<CreditNoteDetail> cndetails = new HashSet<CreditNoteDetail>();

                        getCNDetails(cndetails, companyid);

                        Iterator itr = cndetails.iterator();
                        while (itr.hasNext()) {
                            CreditNoteDetail cnd = (CreditNoteDetail) itr.next();
                            cnd.setCreditNote(cn);
                        }
                        credithm.put("cnid", cn.getID());
                        credithm.put("cndetails", cndetails);
                        credithm.put("otherwise", true);
                        accCreditNoteDAOobj.updateCreditNote(credithm);

                        JSONArray customJArr = fieldDataManagercntrl.getCustomFieldForOeningTransactionsRecords(headArrayList, customFieldParamMap, recarr, columnConfig ,request);
                        customfield =customJArr.toString();
                        if (!StringUtil.isNullOrEmpty(customfield)) {
                            JSONArray jcustomarray = new JSONArray(customfield);
                            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                            customrequestParams.put("customarray", jcustomarray);
                            customrequestParams.put("modulename", Constants.Acc_OpeningBalanceCreditNote_modulename);
                            customrequestParams.put("moduleprimarykey", Constants.Acc_OpeningBalanceCreditNoteid);
                            customrequestParams.put("modulerecid", cn.getID());
                            customrequestParams.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
                            customrequestParams.put(Constants.companyKey, companyid);
                            customrequestParams.put("customdataclasspath", Constants.Acc_OpeningBalanceCreditNote_custom_data_classpath);
                            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                credithm.put("cnid", cn.getID());
                                credithm.put("openingBalanceCreditNoteCustomData", cn.getID());
                                result = accCreditNoteDAOobj.updateCreditNote(credithm);
                            }
                        }

                    } catch (Exception ex) {
                        failed++;
                        String errorMsg = ex.getMessage();
                        try {
                            JSONObject errorLog = new JSONObject(errorMsg);
                            errorMsg = errorLog.getString("errorMsg");
                        } catch (JSONException jex) {
                        }
                        failedRecords.append("\n" + createCSVrecord(recarr) + "\"" + errorMsg.replaceAll("\"", "") + "\"");
                    }
                    total++;
                }
                cnt++;
            }

            if (failed > 0) {
                createFailureFiles(fileName, failedRecords, ".csv");
            }

            int success = total - failed;
            if (total == 0) {
                msg = messageSource.getMessage("acc.field.Emptyfile", null, RequestContextUtils.getLocale(request));
            } else if (success == 0) {
//                issuccess = false;
                msg = messageSource.getMessage("acc.rem.169", null, RequestContextUtils.getLocale(request));
            } else if (success == total) {
                msg = messageSource.getMessage("acc.rem.168", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.field.Imported", null, RequestContextUtils.getLocale(request)) + success + "  "+ messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (success > 1 ? "s" : "") + " successfully"+" ";
                msg += (failed == 0 ? "." : messageSource.getMessage("acc.field.andfailedtoimport", null, RequestContextUtils.getLocale(request)) + failed+"  " + messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (failed > 1 ? "s" : "") + ".");
            }

            try {
                txnManager.commit(status);
            } catch (Exception ex) {
                commitedEx = true;
                throw ex;
            }
        } catch (Exception ex) {
            if (!commitedEx) { //if exception occurs during commit then dont call rollback
                txnManager.rollback(status);
            }
            issuccess = false;
            msg = "" + ex.getMessage();

            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException(messageSource.getMessage("acc.import.msg9", null, RequestContextUtils.getLocale(request)));
        } finally {
            fileInputStream.close();
            DefaultTransactionDefinition ldef = new DefaultTransactionDefinition();
            ldef.setName("import_Tx");
            ldef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus lstatus = txnManager.getTransaction(ldef);
            try {
                //Insert Integration log
                HashMap<String, Object> logDataMap = new HashMap<String, Object>();
                logDataMap.put("FileName", getActualFileName(fileName));
                logDataMap.put("StorageName", fileName);
                logDataMap.put("Log", msg);
                logDataMap.put("Type", "csv");
                logDataMap.put("FailureFileType", failed>0?"csv":"");
                logDataMap.put("TotalRecs", total);
                logDataMap.put("Rejected", failed);
                logDataMap.put("Module", Constants.Acc_Credit_Note_ModuleId);
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userId);
                logDataMap.put("Company", companyid);
                importDao.saveImportLog(logDataMap);
                txnManager.commit(lstatus);
            } catch (Exception ex) {
                txnManager.rollback(lstatus);
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                returnObj.put(Constants.RES_success, issuccess);
                returnObj.put(Constants.RES_msg, msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", getActualFileName(fileName));
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return returnObj;
    }

    public static String getActualFileName(String storageName) {
        String ext = storageName.substring(storageName.lastIndexOf("."));
        String actualName = storageName.substring(0, storageName.lastIndexOf("_"));
        actualName = actualName + ext;
        return actualName;
    }

    public String createCSVrecord(Object[] listArray) {
        String rec = "";
        for (int i = 0; i < listArray.length; i++) {    //Discard columns id at index 0 and isvalid,invalidColumns, validationlog at last 3 indexes.
//            String s = (listArray[i]==null)?"":listArray[i].toString();
            rec += "\"" + (listArray[i] == null ? "" : listArray[i].toString().replaceAll("\"", "")) + "\",";
        }
        return rec;
    }

    public void createFailureFiles(String filename, StringBuilder failedRecords, String ext) {
        String destinationDirectory;
        try {
            destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";

            if (!StringUtil.isNullOrEmpty(filename.substring(filename.lastIndexOf(".")))) {
                ext = filename.substring(filename.lastIndexOf("."));
            }

//            if (StringUtil.isNullOrEmpty(ext)) {
//                ext = filename.substring(filename.lastIndexOf("."));
//            }
            filename = filename.substring(0, filename.lastIndexOf("."));

            java.io.FileOutputStream failurefileOut = new java.io.FileOutputStream(destinationDirectory + "/" + filename + ImportLog.failureTag + ext);
            failurefileOut.write(failedRecords.toString().getBytes());
            failurefileOut.flush();
            failurefileOut.close();
        } catch (Exception ex) {
            System.out.println("\nError file write [success/failed] " + ex);
        }
    }

    /*
     * Method to save Opening Balance CN For Vendor.
     */
    public ModelAndView saveOpeningBalanceVendorCN(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject jobjDetails = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean issuccess = false;
        boolean isAccountingExe=false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("BR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;//txnManager.getTransaction(def);
        String cnNumber = request.getParameter("number");
        String companyid ="";
        try {
            companyid = sessionHandlerImpl.getCompanyid(request);
            String cnId = request.getParameter("transactionId");
            KwlReturnObject cncount=null;
            if (StringUtil.isNullOrEmpty(cnId)) {//Add case 
                /*
                 * code to chek duplicate number
                 */
                cncount = accCreditNoteDAOobj.getCNFromNoteNo(cnNumber, companyid);
                if (cncount.getRecordTotalCount() > 0) {
                    isAccountingExe=true;
                    throw new AccountingException(messageSource.getMessage("acc.CN.creditnoteno", null, RequestContextUtils.getLocale(request)) + cnNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                }
                /*
                 * code for checking wheather entered number can be generated by sequence format or not
                 */
                List resultList = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Credit_Note_ModuleId, cnNumber, companyid);
                if (!resultList.isEmpty()) {
                    boolean isvalidEntryNumber = (Boolean) resultList.get(0);
                    String formatName = (String) resultList.get(1);
                    if (!isvalidEntryNumber) {
                        isAccountingExe=true;
                        throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, RequestContextUtils.getLocale(request)) + " <b>" + cnNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, RequestContextUtils.getLocale(request)) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.pleaseentersomeothernumber", null, RequestContextUtils.getLocale(request)));
                    }
                }
            }
            
            synchronized (this) {//Checks duplicate number for simultaneous transactions
                status = txnManager.getTransaction(def);
                    KwlReturnObject resultInv = accCommonTablesDAO.getTransactionInTemp(cnNumber, companyid, Constants.Acc_Credit_Note_ModuleId);//Get entry from temporary table
                    if (resultInv.getRecordTotalCount() > 0) {
                        isAccountingExe=true;
                        throw new AccountingException(messageSource.getMessage("acc.CN.selectedcreditnoteno", null, RequestContextUtils.getLocale(request)) + cnNumber + messageSource.getMessage("acc.field.alreadyinprocess.", null, RequestContextUtils.getLocale(request)));
                    } else {
                        accCommonTablesDAO.insertTransactionInTemp(cnNumber, companyid, Constants.Acc_Credit_Note_ModuleId);//Insert entry into temporary table
                    }
                txnManager.commit(status);
            }
            status = txnManager.getTransaction(def);
            List li = saveOpeningBalanceVendorCN(request);
            if (!li.isEmpty()) {
                cnNumber = li.get(0).toString();
            }

            issuccess = true;
            msg = messageSource.getMessage("acc.dimension.module.4", null, RequestContextUtils.getLocale(request)) + " " + cnNumber + " " + messageSource.getMessage("acc.field.hasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
            status = txnManager.getTransaction(def);
            accCommonTablesDAO.deleteTransactionInTemp(cnNumber, companyid,Constants.Acc_Credit_Note_ModuleId);//Delete entry in temporary table
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            if(status!=null){
                txnManager.rollback(status);
            }
            try {
                accCommonTablesDAO.deleteTransactionInTemp(cnNumber, companyid,Constants.Acc_Credit_Note_ModuleId);//Delete entry in temporary table
            } catch (ServiceException ex1) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            if(status!=null){
                txnManager.rollback(status);
            }
            try {
                accCommonTablesDAO.deleteTransactionInTemp(cnNumber, companyid,Constants.Acc_Credit_Note_ModuleId);//Delete entry in temporary table
            } catch (ServiceException ex1) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            if(status!=null){
                txnManager.rollback(status);
            }
            try {
                accCommonTablesDAO.deleteTransactionInTemp(cnNumber, companyid,Constants.Acc_Credit_Note_ModuleId);//Delete entry in temporary table
            } catch (ServiceException ex1) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
                jobj.put("data", jArr);
                jobj.put("accException", isAccountingExe);
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public List saveOpeningBalanceVendorCN(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        List returnList = new ArrayList();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            KwlReturnObject result;
            boolean isEditInvoice = false;
            String auditMsg = "", auditID = "", memo = "";
            auditMsg = "added";
            auditID = AuditAction.OPENING_BALANCE_CREATED;
            // Fetching request parameters

            String cnNumber = request.getParameter("number");
            String transactionDateStr = request.getParameter("billdate");
            String currencyid = (request.getParameter("currencyid") == null ? sessionHandlerImpl.getCurrencyID(request) : request.getParameter("currencyid"));
            String transactionAmountStr = request.getParameter("transactionAmount");
            String cnId = request.getParameter("transactionId");
            String narrationValue = request.getParameter("narration");
            String vendorId = request.getParameter("accountId");
            double externalCurrencyRate = StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            boolean conversionRateFromCurrencyToBase = true;
            if (request.getParameter("CurrencyToBaseExchangeRate") != null) {
                conversionRateFromCurrencyToBase = Boolean.parseBoolean(request.getParameter("CurrencyToBaseExchangeRate"));
            }

            double exchangeRateForOpeningTransaction = 1;
            if (!StringUtil.isNullOrEmpty(request.getParameter("exchangeRateForOpeningTransaction"))) {
                exchangeRateForOpeningTransaction = Double.parseDouble(request.getParameter("exchangeRateForOpeningTransaction"));
            }

            Date transactionDate = df.parse(df.format(new Date()));
            Date chequeDate = df.parse(df.format(new Date()));

            if (!StringUtil.isNullOrEmpty(transactionDateStr)) {
                transactionDate = df.parse(transactionDateStr);
            }

            String accountId = "";

            if (!StringUtil.isNullOrEmpty(vendorId)) {
                KwlReturnObject venresult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), vendorId);
                Vendor vendor = (Vendor) venresult.getEntityList().get(0);
                accountId = vendor.getAccount().getID();
            }

            double transactionAmount = 0d;
            if (!StringUtil.isNullOrEmpty(transactionAmountStr)) {
                transactionAmount = Double.parseDouble(transactionAmountStr);
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("memo"))) {
                memo = request.getParameter("memo").toString();
            }

            // creating cn data

            HashMap<String, Object> credithm = new HashMap<String, Object>();

            if (StringUtil.isNullOrEmpty(cnId)) {
                result = accCreditNoteDAOobj.getCNFromNoteNo(cnNumber, companyid);
                int count = result.getRecordTotalCount();
                if (count > 0) {
                    throw new AccountingException(messageSource.getMessage("acc.field.Creditnotenumber", null, RequestContextUtils.getLocale(request)) + cnNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                }
                credithm.put("entrynumber", cnNumber);
                credithm.put("autogenerated", false);
            }


            if (!StringUtil.isNullOrEmpty(cnId)) {

                // check whether CN is linked to  invoice or payment. if yes don't let it edit
                boolean isNoteLinkedWithPayment = accCreditNoteService.isNoteLinkedWithPayment(cnId);
                if (isNoteLinkedWithPayment) {
                    throw new AccountingException(messageSource.getMessage("acc.field.SelectedCreditNoteisLinkedWithPaymentsoitcannotbeedit", null, RequestContextUtils.getLocale(request)));
                }

                boolean isNoteLinkedWithInvoice = isNoteLinkedWithInvoice(cnId, companyid);
                if (isNoteLinkedWithInvoice) {
                    throw new AccountingException(messageSource.getMessage("acc.field.SelectedCreditNoteisLinkedWithInvoicesoitcannotbeedit", null, RequestContextUtils.getLocale(request)));
                }
                KwlReturnObject deleteresult= accCreditNoteDAOobj.deleteCreditNoteDetails(cnId, companyid);
                isEditInvoice = true;
                credithm.put("cnid", cnId);
                auditMsg = "updated";
                auditID = AuditAction.OPENING_BALANCE_UPDATED;
            }

            credithm.put("cnamount", transactionAmount);//
            credithm.put("currencyid", currencyid);//
            credithm.put("externalCurrencyRate", externalCurrencyRate);//
            credithm.put("memo", memo);//
            credithm.put(Constants.companyKey, companyid);//
            credithm.put("narrationValue", narrationValue);//
            credithm.put("creationDate", transactionDate);//
            credithm.put("vendorid", vendorId);//
            credithm.put("accountId", accountId);//
            credithm.put("isOpeningBalenceCN", true);
            credithm.put("normalCN", false);//
            credithm.put("openingBalanceAmountDue", transactionAmount);//
            credithm.put("exchangeRateForOpeningTransaction", exchangeRateForOpeningTransaction);//
            credithm.put("isCNForCustomer", false);//
            credithm.put("openflag", false);
            credithm.put("otherwise", true);
            credithm.put("cnamountdue", transactionAmount);
            credithm.put("conversionRateFromCurrencyToBase", conversionRateFromCurrencyToBase);
            credithm.put("approvestatuslevel", 11);
            // Store CN amount in base currency
            if (conversionRateFromCurrencyToBase) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                credithm.put(Constants.originalOpeningBalanceBaseAmount, authHandler.round(transactionAmount*exchangeRateForOpeningTransaction, companyid));
                credithm.put(Constants.openingBalanceBaseAmountDue, authHandler.round(transactionAmount*exchangeRateForOpeningTransaction, companyid));
            } else {
                credithm.put(Constants.originalOpeningBalanceBaseAmount, authHandler.round(transactionAmount/exchangeRateForOpeningTransaction, companyid));
                credithm.put(Constants.openingBalanceBaseAmountDue, authHandler.round(transactionAmount/exchangeRateForOpeningTransaction, companyid));
            }
            if (isEditInvoice) {
                String modifiedby = sessionHandlerImpl.getUserid(request);
                long updatedon = System.currentTimeMillis();
                credithm.put("modifiedby", modifiedby);
                credithm.put("updatedon", updatedon);

                result = accCreditNoteDAOobj.updateCreditNote(credithm);
            } else {
                String createdby = sessionHandlerImpl.getUserid(request);
                String modifiedby = sessionHandlerImpl.getUserid(request);
                long createdon = System.currentTimeMillis();
                long updatedon = System.currentTimeMillis();

                credithm.put("createdby", createdby);
                credithm.put("modifiedby", modifiedby);
                credithm.put("createdon", createdon);
                credithm.put("updatedon", updatedon);

                result = accCreditNoteDAOobj.addCreditNote(credithm);
            }

            CreditNote cn = (CreditNote) result.getEntityList().get(0);

            HashSet<CreditNoteDetail> cndetails = new HashSet<CreditNoteDetail>();

            getCNDetails(cndetails, companyid);

            Iterator itr = cndetails.iterator();
            while (itr.hasNext()) {
                CreditNoteDetail cnd = (CreditNoteDetail) itr.next();
                cnd.setCreditNote(cn);
            }
            credithm.put("cnid", cn.getID());
            credithm.put("cndetails", cndetails);

            accCreditNoteDAOobj.updateCreditNote(credithm);

            returnList.add(cn.getCreditNoteNumber());
             
            String customfield = request.getParameter("customfield");
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_OpeningBalanceCreditNote_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_OpeningBalanceCreditNoteid);
                customrequestParams.put("modulerecid", cn.getID());
                customrequestParams.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
                customrequestParams.put(Constants.companyKey, companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_OpeningBalanceCreditNote_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    credithm.put("cnid", cn.getID());
                    credithm.put("openingBalanceCreditNoteCustomData", cn.getID());
                    result = accCreditNoteDAOobj.updateCreditNote(credithm);
                }
            }
            
            auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has  " + auditMsg + " an Opening Balance Vendor Credit Note " + cnNumber, request, cnNumber);
        } catch (JSONException ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }catch (ParseException ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return returnList;
    }

    public ModelAndView deleteOpeningCNPermanent(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("VP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String TransactionsInUse = deleteOpeningCNPermanent(request);
            txnManager.commit(status);
            issuccess = true;
            msg = (StringUtil.isNullOrEmpty(TransactionsInUse)) ? messageSource.getMessage("acc.creditN.dels", null, RequestContextUtils.getLocale(request)) : messageSource.getMessage("acc.ob.CNExcept", null, RequestContextUtils.getLocale(request)) + " " + TransactionsInUse + " " + messageSource.getMessage("acc.ob.asInUseAreDeleted", null, RequestContextUtils.getLocale(request));
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public String deleteOpeningCNPermanent(HttpServletRequest request) throws AccountingException, SessionExpiredException, ServiceException {

        String companyid = sessionHandlerImpl.getCompanyid(request);
        String cnid[] = request.getParameterValues("billidArray");
        String cnno[] = request.getParameterValues("invoicenoArray");
        String TransactionsInUse = "";
        for (int count = 0; count < cnid.length; count++) {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("cnid", cnid[count]);
            requestParams.put(Constants.companyKey, companyid);
            String cnInUse = cnno[count];
            try {
                if (!StringUtil.isNullOrEmpty(cnid[count])) {

                    KwlReturnObject dnObj = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), cnid[count]);
                    CreditNote creditNote = (CreditNote) dnObj.getEntityList().get(0);
                    // check for is CN Linked with Payment.
                    boolean isNoteLinkedWithPayment = accCreditNoteService.isNoteLinkedWithPayment(cnid[count]);
                    boolean isNoteLinkedWithAdvancePayment = accCreditNoteService.isNoteLinkedWithAdvancePayment(cnid[count]);
                    if (isNoteLinkedWithPayment || isNoteLinkedWithAdvancePayment) {
                        TransactionsInUse += cnInUse + ", ";
                        throw new AccountingException(messageSource.getMessage("acc.field.SelectedCreditNoteisLinkedWithPaymentsoitcannotbedelete", null, RequestContextUtils.getLocale(request)));
                    }else if(accCreditNoteService.isCreditNotelinkedInDebitNote(cnid[count], companyid)==true){
                        TransactionsInUse += cnInUse + ", ";
                        throw new AccountingException(messageSource.getMessage("acc.field.SelectedCreditNoteisLinkedWithDebitNoteoitcannotbedelete", null, RequestContextUtils.getLocale(request)));
                    }
                    //Delete unrealised JE for Credit Note
                    accJournalEntryobj.permanentDeleteJournalEntryDetailReval(cnid[count], companyid);
                    accJournalEntryobj.permanentDeleteJournalEntryReval(cnid[count], companyid);
                    /*
                     1= update invoice Amount due if linked to Note
                     2 = delete entry from linking information table
                     3= delete Note data
                     */
          
                    // delete foreign gain loss JE
                    List resultJe = accCreditNoteDAOobj.getForeignGainLossJE(cnid[count], companyid);
                    if (resultJe.size() > 0 && resultJe.get(0) != null) {
                        Iterator itr = resultJe.iterator();
                        while (itr.hasNext()) {
                            Object object = itr.next();
                            String jeid = object != null ? object.toString() : "";
                            accCreditNoteService.deleteJEArray(jeid, companyid);
                        }
                    }
                    /*
                     * Before deleting CreditNoteDetail Keeping id of Invoice
                     * utlized in Credit Note
                     */
                    Set<String> invoiceIDSet = new HashSet<>();
                    if (creditNote.getApprovestatuslevel() == 11 && !creditNote.isDeleted()) {
                        for (CreditNoteDetail cnd : creditNote.getRows()) {
                            if (cnd.getInvoice() != null) {
                                invoiceIDSet.add(cnd.getInvoice().getID());
                            }
                        }
                    }
                    //Delete Rouding JEs if created against SI
                    String roundingJENo = "";
                    String roundingIDs = "";
                    String invIDs = "";
                    for (String invID : invoiceIDSet) {
                        invIDs = invID + ",";
                    }
                    if (!StringUtil.isNullOrEmpty(invIDs)) {
                        invIDs = invIDs.substring(0, invIDs.length() - 1);
                    }
                    KwlReturnObject jeResult = accJournalEntryobj.getRoundingJournalEntryByGRIds(invIDs, companyid);
                    List<JournalEntry> jeList = jeResult.getEntityList();
                    for (JournalEntry roundingJE : jeList) {
                        roundingJENo = roundingJE.getEntryNumber() + ",";
                        roundingIDs = roundingJE.getID() + ",";
                        accCreditNoteService.deleteJEArray(roundingJE.getID(), companyid);
                    }

                    if (!StringUtil.isNullOrEmpty(roundingJENo)) {
                        roundingJENo = roundingJENo.substring(0, roundingJENo.length() - 1);
                    }
                    if (!StringUtil.isNullOrEmpty(roundingIDs)) {
                        roundingIDs = roundingIDs.substring(0, roundingIDs.length() - 1);
                    }

                    accCreditNoteService.updateOpeningInvoiceAmountDue(cnid[count], companyid);
                    accCreditNoteDAOobj.deleteLinkingInformationOfCN(requestParams);
                    accCreditNoteDAOobj.deleteCreditNotesPermanent(requestParams);
                    auditTrailObj.insertAuditLog(AuditAction.OPENING_BALANCE_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted an Opening Balance Credit Note Permanently " + cnno[count], request, cnid[count]);
                    if (!StringUtil.isNullOrEmpty(roundingJENo)) {
                        auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted Credit Note " + cnno + " Permanently. So Rounding JE No. " + roundingJENo + " deleted.", request, roundingIDs);
                    }
                }

            } catch (Exception ex) {
                // throw new AccountingException(messageSource.getMessage("acc.pay1.excp1", null, RequestContextUtils.getLocale(request)));
            }
        }
        return TransactionsInUse;
    }

    private boolean isNoteLinkedWithInvoice(String noteId, String companyId) {
        boolean isNoteLinkedWithInvoice = false;
        try {
            KwlReturnObject result = accCreditNoteDAOobj.getInvoicesLinkedWithCreditNote(noteId, companyId);
            List list = result.getEntityList();
            Iterator it = list.iterator();
            if (it.hasNext()) {
                isNoteLinkedWithInvoice = true;
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return isNoteLinkedWithInvoice;
    }



    public ModelAndView approvePendingCreditNote(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        boolean isAccountingExe = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Invoice_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            String billid = request.getParameter("billid");
            String billno = request.getParameter("billno");
            String remark = request.getParameter("remark");
            String userid = sessionHandlerImpl.getUserid(request);
            String userName = sessionHandlerImpl.getUserFullName(request);
            String baseUrl = URLUtil.getPageURL(request, loginpageFull);
             String psotingDateStr = request.getParameter("postingDate");
            double amount = StringUtil.isNullOrEmpty(request.getParameter("amount")) ? 0 : Double.parseDouble(request.getParameter("amount"));
            /*
             * ApprovalType =1 : approve as normal way 
             * ApprovalType =2 : approve CN agianst invoice as otherwise 
             * ApprovalType =3 : approve CN agianst invoice after editing, for this request not come here it goes to saveCreditNote
             */
            int approvalType = StringUtil.isNullOrEmpty(request.getParameter("approvalType"))?1:Integer.parseInt(request.getParameter("approvalType"));
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put("billid", billid);
            requestParams.put("billno", billno);
            requestParams.put("remark", remark);
            requestParams.put("userid", userid);
            requestParams.put("userName", userName);
            requestParams.put("amount", amount);
            requestParams.put("baseUrl", baseUrl);
            requestParams.put("postingDate", psotingDateStr);
            requestParams.put("reqHeader", request.getHeader("x-real-ip"));//USED TO INSERT DATA IN AUDIT TRIAL
            requestParams.put("remoteAddress", request.getRemoteAddr());//USED TO INSERT DATA IN AUDIT TRIAL

            if (approvalType == 2) {
                List list = accCreditNoteService.approvePendingCreditNoteAgainstInvoiceAsCNOtherwise(requestParams);
                msg = (String) list.get(0);
            } else {
                List list = accCreditNoteService.approvePendingCreditNote(requestParams);
                msg = (String) list.get(0);
            }
            issuccess = true;
            txnManager.commit(status);
            
            //=================Create Rounding JE After CN Approved Start===============
            try {
                accCreditNoteService.postRoundingJEAfterApproveCreditNote(paramJobj);
            } catch (ServiceException ex) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
            //=================Create Rounding JE After CN Approved End===============
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }catch(AccountingException ae){
            txnManager.rollback(status);
            isAccountingExe=true;
            msg = "" + ae.getMessage();
            msg = msg.replaceFirst("Transaction", "JE Posting");
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ae);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
                jobj.put("isAccountingExe", isAccountingExe);
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView rejectPendingCreditNote(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Invoice_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            int level = 0;
            String cnID = "";
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            JSONObject jObj = jArr.getJSONObject(0);
            cnID = StringUtil.DecodeText(jObj.optString("billid"));
            KwlReturnObject invRes = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), cnID);
            CreditNote creditNote = (CreditNote) invRes.getEntityList().get(0);
            level = creditNote.getApprovestatuslevel();
            
            String userid = sessionHandlerImpl.getUserid(request);
            String userName = sessionHandlerImpl.getUserFullName(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            double amount = StringUtil.isNullOrEmpty(request.getParameter("amount")) ? 0 : Double.parseDouble(request.getParameter("amount"));
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put("userid", userid);
            requestParams.put("userName", userName);
            requestParams.put("amount", amount);

            //Delete unrealised JE for Credit Note
            accJournalEntryobj.permanentDeleteJournalEntryDetailReval(cnID, companyid);
            accJournalEntryobj.permanentDeleteJournalEntryReval(cnID, companyid);
            
            // delete foreign gain loss JE
            List resultJe = accCreditNoteDAOobj.getForeignGainLossJE(cnID, companyid);
            if (resultJe.size() > 0 && resultJe.get(0) != null) {
                Iterator itr = resultJe.iterator();
                while (itr.hasNext()) {
                    Object object = itr.next();
                    String jeid = object != null ? object.toString() : "";
                    accCreditNoteService.deleteJEArray(jeid, companyid);
                }
            }
            
            // delete JE updating flag
            if(creditNote.getJournalEntry()!=null){
                accJournalEntryobj.deleteJEEntry(creditNote.getJournalEntry().getID(),companyid);
            }
            //update amount due of linking invoices
//            accCreditNoteService.updateOpeningInvoiceAmountDue(cnID, companyid);
            
                    
            boolean isRejected = accCreditNoteService.rejectPendingCreditNote(requestParams,jArr);
            txnManager.commit(status);
            issuccess = true;

            if (isRejected) {
                msg = messageSource.getMessage("acc.field.creditnotehasbeenrejectedsuccessfully", null, RequestContextUtils.getLocale(request)) + " by " + userName +" at Level " + level + ".";
            } else {
                msg = messageSource.getMessage("acc.vq.notAuthorisedToRejectThisRecord", null, RequestContextUtils.getLocale(request)) + " at Level " + level + ".";
            }

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView checkInvoiceKnockedOffDuringCreditNotePending (HttpServletRequest request,HttpServletResponse response){
        JSONObject jobj = new JSONObject();
        try{
           HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);  
           String billid = request.getParameter("billid");
           requestParams.put("billid", billid);
           jobj= accCreditNoteService.checkInvoiceKnockedOffDuringCreditNotePending(requestParams);
        } catch (Exception ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView updateCreditNoteTransactionDetailsInJE(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Invoice_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONObject jobj = new JSONObject();
        int jeupdatedcount = 0;
        boolean issuccess = false;
        try {
            HashMap<String, Object> tempParams = new HashMap<String, Object>();
            String subdomain = "";
            String[] subdomainArray = null;
            if (!StringUtil.isNullOrEmpty(request.getParameter("subdomain"))) {
                subdomain = (String) request.getParameter("subdomain");
                subdomainArray = subdomain.split(",");
            }
            KwlReturnObject company = accCompanyPreferencesObj.getCompanyList(subdomainArray);
            Iterator ctr = company.getEntityList().iterator();
            while (ctr.hasNext()) {
                String companyid = ctr.next().toString();
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put(Constants.companyKey, companyid);
                KwlReturnObject result = accCreditNoteDAOobj.getCreditNotesForJE(requestParams);
                Iterator itr = result.getEntityList().iterator();
                while (itr.hasNext()) {
                    CreditNote creditNote = (CreditNote) itr.next();
                    tempParams = new HashMap<String, Object>();
                    tempParams.put("transactionID", creditNote.getID());
                    tempParams.put("moduleID", Constants.Acc_Credit_Note_ModuleId);
                    tempParams.put("journalEntry", creditNote.getJournalEntry());
                    boolean isUpdated = accJournalEntryobj.updateJEDetails(tempParams);
                    if (isUpdated) {
                        jeupdatedcount++;
                    }
                }
            }
            txnManager.commit(status);
            issuccess = true;
        } catch (Exception ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
            txnManager.rollback(status);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put("Updated JE Records ", jeupdatedcount);
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getCreditNote(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            HashMap<String, Object> requestParams = getCreditNoteMap(request);
            KwlReturnObject result = accCreditNoteDAOobj.getCreaditNote(requestParams);
            jobj = getCreditNoteJson(request, result.getEntityList());
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    

    public JSONObject getCreditNoteJson(HttpServletRequest request, List list) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            HashMap<String, Object> requestParams = getCreditNoteMap(request);
            double tax = 0;
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("df");

            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                CreditNote creditMemo = (CreditNote) row[0];
                JournalEntry je = creditMemo.getJournalEntry();
                Customer customer = (Customer) row[1];
                JournalEntryDetail details = (JournalEntryDetail) row[2];
                JSONObject obj = new JSONObject();
                obj.put("noteid", creditMemo.getID());
                obj.put("noteno", creditMemo.getCreditNoteNumber());
                obj.put("journalentryid", je.getID());
                obj.put("currencysymbol", (creditMemo.getCurrency() == null ? currency.getSymbol() : creditMemo.getCurrency().getSymbol()));
                obj.put("currencyid", (creditMemo.getCurrency() == null ? currency.getCurrencyID() : creditMemo.getCurrency().getCurrencyID()));
                obj.put("entryno", je.getEntryNumber());
                obj.put("personid", customer.getID());
                obj.put("personname", customer.getAccount().getName());
                obj.put("amount", details.getAmount());
//                obj.put("date", df.format(je.getEntryDate()));
                obj.put("date", df.format(creditMemo.getCreationDate()));
                obj.put("memo", creditMemo.getMemo());
                obj.put("deleted", creditMemo.isDeleted());
                obj.put("costcenterid", creditMemo.getCostcenter() == null ? "" : creditMemo.getCostcenter().getID());
                obj.put("costcenterName", je.getCostcenter() == null ? "" : je.getCostcenter().getName());
                KwlReturnObject result = accJournalEntryobj.getJournalEntryDetail(je.getID(), sessionHandlerImpl.getCompanyid(request));
                Iterator iterator = result.getEntityList().iterator();
                while (iterator.hasNext()) {
                    JournalEntryDetail jed = (JournalEntryDetail) iterator.next();
                    Account account = null;
                    account = jed.getAccount();
                    //To do - need to test.
                    if (account.getMastertypevalue() == Group.ACCOUNTTYPE_GST) {//GST transaction.
                        if (jed.isDebit()) {
                            tax = jed.getAmount();
                        }
                    }
                }
                result = accCreditNoteDAOobj.getTotalTax_TotalDiscount(creditMemo.getID());
                double totTax = 0, totDiscount = 0;
                if (result != null && result.getEntityList() != null) {
                    Iterator resItr = result.getEntityList().iterator();
                    Object[] sumRow = (Object[]) resItr.next();
                    if (sumRow[0] != null) {
                        totTax = Double.parseDouble(sumRow[0].toString());
                    }
                    if (sumRow[1] != null) {
                        totDiscount = Double.parseDouble(sumRow[1].toString());
                    }
                }

                obj.put("noteSubTotal", details.getAmount() + totDiscount - totTax);
                obj.put("notetax", tax);
                obj.put("totalTax", totTax);
                obj.put("totalDiscount", totDiscount);
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getCreditNoteJson : " + ex.getMessage(), ex);
        }
        return jobj;
    }

    public ModelAndView getCreditNoteAccountsRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = getCreditNoteAccountsRows(request);
            issuccess = true;
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            msg = "accCreditNoteController.getCreditNoteAccountsRows:" + ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getCreditNoteAccountsRows(HttpServletRequest request) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArray = new JSONArray();
        try {
            String crNoteId = request.getParameter("noteId");
            boolean isCopy = !StringUtil.isNullOrEmpty(request.getParameter("isCopy")) ? Boolean.parseBoolean(request.getParameter("isCopy")) : false;
            KwlReturnObject result = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), crNoteId);
            CreditNote cn = (CreditNote) result.getEntityList().get(0);

            if (cn != null) {
                Set<CreditNoteTaxEntry> cnTaxEntryDetails = cn.getCnTaxEntryDetails();
                if (cnTaxEntryDetails != null && !cnTaxEntryDetails.isEmpty()) {
                    String countryID = cn.getCompany().getCountry() != null ? cn.getCompany().getCountry().getID() : null;

                        HashMap<String, Object> fieldrequestParams = new HashMap();
                        HashMap<String, String> customFieldMap = new HashMap<String, String>();
                        HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
                        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                        fieldrequestParams.put(Constants.filter_values, Arrays.asList(cn.getCompany().getCompanyID(), Constants.Acc_Credit_Note_ModuleId));
                        HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                        HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);

                    for (CreditNoteTaxEntry noteTaxEntry : cnTaxEntryDetails) {
                        JSONObject obj = new JSONObject();
                        obj.put("rowid", noteTaxEntry.getID());
                        obj.put("accountid", noteTaxEntry.getAccount().getID());
                        obj.put("dramount", noteTaxEntry.getAmount());
                        obj.put("prtaxid", (noteTaxEntry.getTax() != null) ? noteTaxEntry.getTax().getID() : "None");
//                        obj.put("prtaxid", noteTaxEntry.getTax() != null ? (isCopy ? (noteTaxEntry.getTax().isActivated() ? noteTaxEntry.getTax().getID() : "") : noteTaxEntry.getTax().getID()) : "");
                        obj.put("amountwithtax", noteTaxEntry.getAmount() + noteTaxEntry.getTaxamount());
                        obj.put("taxamount", noteTaxEntry.getTaxamount());
                        obj.put("rateIncludingGst", noteTaxEntry.getRateIncludingGst());
                        obj.put("description", StringUtil.DecodeText(noteTaxEntry.getDescription()));
                        obj.put("reason", (noteTaxEntry.getReason() != null)?noteTaxEntry.getReason().getID():"");
                        obj.put("gstCurrencyRate", noteTaxEntry.getGstCurrencyRate());
                        obj.put("debit", noteTaxEntry.isDebitForMultiCNDN());
                        obj.put("srNoForRow", noteTaxEntry.getSrNoForRow());
                        // ## Get Custom Field Data 
                        Map<String, Object> variableMap = new HashMap<String, Object>();
                        HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                        ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                        Detailfilter_names.add(Constants.Acc_JEDetail_recdetailId);
                        Detailfilter_params.add(noteTaxEntry.getID());
                        invDetailRequestParams.put("filter_names", Detailfilter_names);
                        invDetailRequestParams.put("filter_params", Detailfilter_params);
                        KwlReturnObject idcustresult = accCreditNoteDAOobj.geCreditNoteCustomData(invDetailRequestParams);
                        if (idcustresult.getEntityList().size() > 0) {
                            AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
                            AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                            boolean isExport = (request.getAttribute("isExport") == null) ? false : true;
                            JSONObject params = new JSONObject();
                            params.put("isExport", isExport);
                            fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);

//                            for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
//                                String coldata = varEntry.getValue() != null ? varEntry.getValue().toString() : "";
//                                if (customFieldMap.containsKey(varEntry.getKey())) {
//                                    KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), coldata);
//                                    FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
//                                    if (fieldComboData != null) {
//                                        obj.put(varEntry.getKey(), coldata != null ? coldata : "");//fieldComboData.getValue()!=null ?fieldComboData.getValue():"");
//                                    }
//                                } else {
//                                    if (!StringUtil.isNullOrEmpty(coldata)) {
//                                        obj.put(varEntry.getKey(), coldata);
//                                    }
//                                }
//                            }
                        }
                        
                        /**
                         * Fetch Term Details for HSN in India case
                         */
                        if (countryID.equalsIgnoreCase("" + Constants.indian_country_id)) { // Fetch  term details of Product
                            JSONObject json = new JSONObject();
                            json.put("creditNoteTaxEntry", noteTaxEntry.getID());
                            KwlReturnObject result6 = accCreditNoteDAOobj.getCreditNoteDetailTermMap(json);
                            if (result6.getEntityList() != null && result6.getEntityList().size() > 0 && result6.getEntityList().get(0) != null) {
                                ArrayList<CreditNoteDetailTermMap> productTermDetail = (ArrayList<CreditNoteDetailTermMap>) result6.getEntityList();
                                JSONArray productTermJsonArry = new JSONArray();
                                double termAccount = 0.0;
                                for (CreditNoteDetailTermMap productTermsMapObj : productTermDetail) {
                                    JSONObject productTermJsonObj = new JSONObject();
                                    productTermJsonObj.put("id", productTermsMapObj.getId());
                                    productTermJsonObj.put("termid", productTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getId());
                                    productTermJsonObj.put("productentitytermid", productTermsMapObj.getEntitybasedLineLevelTermRate() != null ? productTermsMapObj.getEntitybasedLineLevelTermRate().getId() : "");
                                    productTermJsonObj.put("isDefault", productTermsMapObj.isIsGSTApplied());
                                    productTermJsonObj.put("term", productTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getTerm());
                                    productTermJsonObj.put("formula", productTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getFormula());
                                    productTermJsonObj.put("formulaids", productTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getFormula());
                                    productTermJsonObj.put("termpercentage", productTermsMapObj.getPercentage());
                                    productTermJsonObj.put("originalTermPercentage", productTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getPercentage()); // For Service Tax Abatemnt calculation
                                    productTermJsonObj.put("termamount", productTermsMapObj.getTermamount());
                                    productTermJsonObj.put("glaccountname", productTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getAccount().getAccountName());
                                    productTermJsonObj.put("glaccount", productTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getAccount().getID());
                                    productTermJsonObj.put("IsOtherTermTaxable", productTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().isOtherTermTaxable());
                                    productTermJsonObj.put("sign", productTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getSign());
                                    productTermJsonObj.put("purchasevalueorsalevalue", productTermsMapObj.getPurchaseValueOrSaleValue());
                                    productTermJsonObj.put("deductionorabatementpercent", productTermsMapObj.getDeductionOrAbatementPercent());
                                    productTermJsonObj.put("assessablevalue", productTermsMapObj.getAssessablevalue());
                                    productTermJsonObj.put("taxtype", productTermsMapObj.getTaxType());
                                    productTermJsonObj.put("taxvalue", productTermsMapObj.getPercentage());
                                    productTermJsonObj.put("termtype", productTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getTermType());
                                    productTermJsonObj.put("termsequence", productTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getTermSequence());
                                    productTermJsonObj.put("formType", productTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getFormType());
                                    productTermJsonObj.put("accountid", productTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getAccount().getID());
                                    if(productTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getPayableAccount() != null){
                                        productTermJsonObj.put("payableaccountid", productTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getPayableAccount().getID());
                                    }
                                    productTermJsonArry.put(productTermJsonObj);
                                    termAccount += productTermsMapObj.getTermamount();
                                }
                                obj.put("LineTermdetails", productTermJsonArry.toString());
                                obj.put("recTermAmount",termAccount);
                                obj.put("amountwithtax", noteTaxEntry.getAmount() + termAccount);
                            }
                            obj.put("productid", StringUtil.isNullOrEmpty(noteTaxEntry.getProductid()) ? "" : noteTaxEntry.getProductid());
                            obj.put("termAmount", noteTaxEntry.getTermAmount());
                            obj.put("refdocid", noteTaxEntry.getID());
                            obj.put("taxamount", 0);
                            fieldDataManagercntrl.getGSTTaxClassHistory(obj);
                        }
                        jArray.put(obj);
                    }
                } else {// in case of if tax is not included while creation of CN Value will be go fron jedetail table in case of Edit.
                    JournalEntry je = cn.getJournalEntry();
                    Set<JournalEntryDetail> jeDetails = je.getDetails();
                    for (JournalEntryDetail jed : jeDetails) {
                        JSONObject obj = new JSONObject();
                        if (jed.isDebit()) {
                            obj.put("accountid", jed.getAccount().getID());
                            obj.put("dramount", jed.getAmount());
                            obj.put("prtaxid", "");
                            obj.put("amountwithtax", jed.getAmount());
                            obj.put("taxamount", 0);
                            obj.put("description", StringUtil.DecodeText(jed.getDescription()));
                            obj.put("debit", jed.isDebit());
                            jArray.put(obj);
                        }
                    }   
                }

            }
            JSONArray sortedArray = new JSONArray();
            sortedArray = authHandler.sortJson(jArray);
            if (sortedArray.length() == jArray.length()) {
                jArray = sortedArray;
            }
            jobj.put("data", jArray);
        }/* catch (UnsupportedEncodingException ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }*/ catch (JSONException ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return jobj;
    }

    public JSONObject getCreditNoteRow(HttpServletRequest request, String[] billids) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

            String companyid = sessionHandlerImpl.getCompanyid(request);
            String[] creditNote = (billids==null)? request.getParameter("bills").split(",") :billids;
            int i = 0;
            boolean cnAgainstVI=false;//This flag is used to show the link information where CN against VI(Malasian GST)
            JSONArray jArr = new JSONArray();
            DateFormat df = authHandler.getDateOnlyFormat();
            HashMap<String, Object> cnRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("creditNote.ID");
            order_by.add("srno");
            order_type.add("asc");
            cnRequestParams.put("filter_names", filter_names);
            cnRequestParams.put("filter_params", filter_params);
            cnRequestParams.put("order_by", order_by);
            cnRequestParams.put("order_type", order_type);
            
            while (creditNote != null && i < creditNote.length) {
                KwlReturnObject result = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), creditNote[i]);
                CreditNote cn = (CreditNote) result.getEntityList().get(0);
                filter_params.clear();
                filter_params.add(cn.getID());
                cnAgainstVI = cn.getCntype() == 4 ? true : false;
                KwlReturnObject grdresult = accCreditNoteDAOobj.getCreditNoteDetails(cnRequestParams);
                Iterator itr = grdresult.getEntityList().iterator();
                boolean cnusedflag = true;
                if (cn.getCntype() != 3 && cn.getCntype()!=4 && cn.isOtherwise() && cn.getCnamount() == cn.getCnamountdue()) {
                    cnusedflag = false;
                }
                while (itr.hasNext()) {
                    CreditNoteDetail row = (CreditNoteDetail) itr.next();
                    if (!cnusedflag) {
                        break;
                    }
                    JSONObject obj = new JSONObject();
                    Invoice invObj = row.getInvoice();
                    GoodsReceipt grObj = row.getGoodsReceipt();
                    if (invObj != null || grObj != null) {
                        Double invoiceAmount = 0d;
                        Date invoiceCreationDate = null;
                        invoiceCreationDate = cnAgainstVI ? grObj.getCreationDate() : invObj.getCreationDate();
                        if ((cnAgainstVI ? grObj.isIsOpeningBalenceInvoice() : invObj.isIsOpeningBalenceInvoice())) {
                            invoiceAmount = cnAgainstVI ? grObj.getOriginalOpeningBalanceAmount() : invObj.getOriginalOpeningBalanceAmount();
                        } else {
//                            invoiceCreationDate = cnAgainstVI ? grObj.getJournalEntry().getEntryDate() : invObj.getJournalEntry().getEntryDate();
                            invoiceAmount = cnAgainstVI ? grObj.getVendorEntry().getAmount() : invObj.getCustomerEntry().getAmount();
                        }
                        obj.put("invcreationdate", df.format(invoiceCreationDate));
                        obj.put("invduedate", cnAgainstVI ? df.format(row.getGoodsReceipt().getDueDate()) : df.format(row.getInvoice().getDueDate()));
                        obj.put("invamountdue", cnAgainstVI ? (grObj.isIsOpeningBalenceInvoice() ? grObj.getOpeningBalanceAmountDue() : grObj.getInvoiceamountdue()) : (invObj.isIsOpeningBalenceInvoice() ? invObj.getOpeningBalanceAmountDue() : invObj.getInvoiceamountdue()));
                        obj.put("invamount", invoiceAmount);
                        obj.put("withoutinventory", false);
                        obj.put("billid", cn.getID());
                        obj.put("billno", cn.getCreditNoteNumber());
                        obj.put("srno", row.getSrno());
                        obj.put("rowid", row.getID());
                        obj.put("productid", cnAgainstVI ? (row.getGoodsReceiptRow() == null ? "" : row.getGoodsReceiptRow().getInventory().getProduct().getID()) : (row.getInvoiceRow() == null ? "" : row.getInvoiceRow().getInventory().getProduct().getID()));
                        obj.put("productdetail", cnAgainstVI ? (row.getGoodsReceiptRow() == null ? "" : row.getGoodsReceiptRow().getInventory().getProduct().getName()) : (row.getInvoiceRow() == null ? "" : row.getInvoiceRow().getInventory().getProduct().getName()));
                        obj.put("unitname", cnAgainstVI ? ((row.getInventory() != null && row.getInventory().getUom() != null) ? row.getInventory().getUom().getNameEmptyforNA() : (row.getGoodsReceiptRow() != null && row.getGoodsReceiptRow().getInventory() != null && row.getGoodsReceiptRow().getInventory().getProduct() != null && row.getGoodsReceiptRow().getInventory().getProduct().getUnitOfMeasure() != null) ? row.getGoodsReceiptRow().getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA() : "") : (row.getInventory() != null ? row.getInventory().getUom().getNameEmptyforNA() : row.getInvoiceRow() == null ? "" : row.getInvoiceRow().getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA()));
                        obj.put("desc", cnAgainstVI ? (row.getGoodsReceiptRow() == null ? "" : row.getGoodsReceiptRow().getInventory().getProduct().getDescription()) : (row.getInvoiceRow() == null ? "" : row.getInvoiceRow().getInventory().getProduct().getDescription()));
                        obj.put("invstore", (StringUtil.isNullOrEmpty(row.getInvstoreid())) ? "" : row.getInvstoreid());
                        obj.put("invlocation", (StringUtil.isNullOrEmpty(row.getInvlocid())) ? "" : row.getInvlocid());
                        //                    obj.put("remark", row.getRemark());
                        obj.put("currencysymbol", cnAgainstVI ? ((grObj.getCurrency() != null ? (grObj.getCurrency().getSymbol()) : ((cn.getCurrency() == null ? currency.getSymbol() : cn.getCurrency().getSymbol())))) : ((invObj.getCurrency() != null ? (invObj.getCurrency().getSymbol()) : ((cn.getCurrency() == null ? currency.getSymbol() : cn.getCurrency().getSymbol())))));
                        obj.put("currencycode", cnAgainstVI ? ((grObj.getCurrency() != null ? (grObj.getCurrency().getCurrencyCode()) : ((cn.getCurrency() == null ? currency.getCurrencyCode() : cn.getCurrency().getCurrencyCode())))) : ((invObj.getCurrency() != null ? (invObj.getCurrency().getCurrencyCode()) : ((cn.getCurrency() == null ? currency.getCurrencyCode() : cn.getCurrency().getCurrencyCode())))));
                        if (cn.isOtherwise() && row.getPaidinvflag() != 1) {
                            obj.put("transectionid", cnAgainstVI ? (row.getGoodsReceipt() == null ? "" : row.getGoodsReceipt().getID()) : (row.getInvoice() == null ? "" : row.getInvoice().getID()));
                            obj.put("transectionno", cnAgainstVI ? (row.getGoodsReceipt() == null ? "" : row.getGoodsReceipt().getGoodsReceiptNumber()) : (row.getInvoice() == null ? "" : row.getInvoice().getInvoiceNumber()));
                            obj.put("memo", cnAgainstVI ? (row.getGoodsReceiptRow() == null ? "" : row.getGoodsReceiptRow().getGoodsReceipt().getMemo()) : (row.getInvoice() == null ? "" : row.getInvoice().getMemo()));
                        } else {
                            obj.put("transectionid", cnAgainstVI ? (row.getGoodsReceiptRow() == null ? "" : row.getGoodsReceiptRow().getGoodsReceipt().getID()) : (row.getInvoiceRow() == null ? "" : row.getInvoiceRow().getInvoice().getID()));
                            obj.put("transectionno", cnAgainstVI ? (row.getGoodsReceiptRow() == null ? "" : row.getGoodsReceiptRow().getGoodsReceipt().getGoodsReceiptNumber()) : (row.getInvoiceRow() == null ? "" : row.getInvoiceRow().getInvoice().getInvoiceNumber()));
                            obj.put("memo", cnAgainstVI ? (row.getGoodsReceiptRow() == null ? "" : row.getGoodsReceiptRow().getGoodsReceipt().getMemo()) : (row.getInvoiceRow() == null ? "" : row.getInvoiceRow().getInvoice().getMemo()));
                        }
                        obj.put("otherwise", cn.isOtherwise());
                        Discount disc = row.getDiscount();
                        if (disc != null) {
                            obj.put("discount", disc.getAmountinInvCurrency());
                            obj.put("paidAmountinTransactionCurrency", disc.getDiscountValue());
                        } else {
                            obj.put("discount", 0);
                            obj.put("paidAmountinTransactionCurrency", 0);
                        }

                        obj.put("exchangeratefortransaction", row.getExchangeRateForTransaction());
                        obj.put("quantity", row.getQuantity());
                        obj.put("taxamount", row.getTaxAmount());
                        obj.put("amounttoadjust", row.getAmountToAdjust());
                        obj.put("taxamounttoadjust", row.getTaxAmountToAdjust());
                        obj.put("adjustedamount", row.getAdjustedAmount());
                        obj.put("paidinvflag", row.getPaidinvflag());
                        obj.put("cntype", cn.getCntype());
                        obj.put("remark", !StringUtil.isNullOrEmpty(row.getRemark()) ? row.getRemark() : "");
                        jArr.put(obj);
                    }// End of Main If
                }//End of While
                getAccountDetailsForCreditNote(cn,jArr, companyid);//I have written this function to get account details on expander click of credit note in CN report.
                i++;
                JSONArray sortedArray = new JSONArray();
                sortedArray = authHandler.sortJson(jArr);
                if (sortedArray.length() == jArr.length()) {
                    jArr = sortedArray;
                }
                jobj.put("data", jArr);
            }//End of outer  While
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getCreditNoteRows : " + ex.getMessage(), ex);
        }
        return jobj;
    }

    public void getAccountDetailsForCreditNote(CreditNote cn, JSONArray jArr, String companyid) throws ServiceException {
        try {
            Set<JournalEntryDetail> jedDetails = cn.getJournalEntry()!=null ? cn.getJournalEntry().getDetails() : new HashSet(0);
            if (jedDetails.size() > 0) {
                HashMap<String, Object> fieldrequestParams = new HashMap();
                HashMap<String, String> customFieldMap = new HashMap<String, String>();
                HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(cn.getCompany().getCompanyID(), Constants.Acc_Credit_Note_ModuleId));
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);

                Iterator<JournalEntryDetail> jeDetailIte = jedDetails.iterator();
                while (jeDetailIte.hasNext()) {
                    JournalEntryDetail jedetail = jeDetailIte.next();
                    if (jedetail.getAccount()!=null && jedetail.getAccount().getMastertypevalue() != 4) {//4 for not including tax as a account.
                        JSONObject obj = new JSONObject();
                        if (jedetail.getID().equalsIgnoreCase(cn.getCustomerEntry().getID())) {
                            continue;
                        }
                        obj.put("accountname",!StringUtil.isNullOrEmpty(jedetail.getAccount().getName()) ? jedetail.getAccount().getName() : "-");
                        obj.put("description", !StringUtil.isNullOrEmpty(jedetail.getDescription()) ?jedetail.getDescription() : "-");
                        double amount = authHandler.round(jedetail.getAmount(), companyid);
                        obj.put("totalamount", amount);
                        obj.put("currencysymbol",cn.getCurrency()!=null ? cn.getCurrency().getSymbol() : "");
                        obj.put("isaccountdetails", true);
                        obj.put("taxpercent", 0);
                        obj.put("taxamount", 0);
                        obj.put("debit", "Debit");
                        String jeDetailId = jedetail.getID();
                        String jeDetailaccid = jedetail.getAccount().getID();
                        Set<CreditNoteTaxEntry> Taxset = cn.getCnTaxEntryDetails()!=null ? cn.getCnTaxEntryDetails() : new HashSet(0);
                        Iterator<CreditNoteTaxEntry> taxIte = Taxset.iterator();
                        while (taxIte.hasNext()) {
                            CreditNoteTaxEntry txEntry = taxIte.next();
                            double taxpercent = 0.0d;
                            String taxJeDetailId = txEntry.getTotalJED()!=null ? txEntry.getTotalJED().getID() : "";
                            String taxJeDetailaccid = txEntry.getAccount() != null ? txEntry.getAccount().getID() : "";
                            obj.put("reason", txEntry.getReason() == null ? "" : txEntry.getReason().getValue());
                            if (StringUtil.equal(jeDetailId, taxJeDetailId) && StringUtil.equal(jeDetailaccid, taxJeDetailaccid) && txEntry.isIsForDetailsAccount()) {
                                if (txEntry.getTax() != null) {
//                                    KwlReturnObject perresult = accTaxObj.getTaxPercent(cn.getCompany().getCompanyID(), cn.getJournalEntry().getEntryDate(), txEntry.getTax().getID());
                                    KwlReturnObject perresult = accTaxObj.getTaxPercent(cn.getCompany().getCompanyID(), cn.getCreationDate(), txEntry.getTax().getID());
                                    taxpercent = (Double) perresult.getEntityList().get(0);
                                }
                                obj.put("taxpercent", taxpercent);
                                double txAmount = authHandler.round(txEntry.getTaxamount(), companyid);
                                obj.put("taxamount", txAmount);
                                obj.put("debit", txEntry.isDebitForMultiCNDN() ? "Debit" : "Credit");
                                obj.put("srNoForRow", txEntry.getSrNoForRow());
                            }
                        }
                        // ## Get Custom Field Data 
                        Map<String, Object> variableMap = new HashMap<String, Object>();
                        HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                        ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                        Detailfilter_names.add(Constants.Acc_jedetailId);
                        Detailfilter_params.add(jedetail.getID());
                        invDetailRequestParams.put("filter_names", Detailfilter_names);
                        invDetailRequestParams.put("filter_params", Detailfilter_params);
                        JSONArray dimensionArr = new JSONArray();
                        KwlReturnObject idcustresult = accCreditNoteDAOobj.geCreditNoteCustomData(invDetailRequestParams);
                        if (idcustresult.getEntityList().size() > 0) {
                            AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
                            AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                            if (jeDetailCustom != null) {
                                JSONObject params = new JSONObject();
                                params.put("isExport", true);

                                fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                            }
                        }
                        jArr.put(obj);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getAccountDetailsForCreditNote : " + ex.getMessage(), ex);
        }
    }

    public ModelAndView deleteCreditNotes(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CN_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            deleteCreditNotes(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.creditN.dels", null, RequestContextUtils.getLocale(request));   //"Credit Note(s) has been deleted successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void deleteCreditNotes(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("noteid"))) {
                    String cnid = StringUtil.DecodeText(jobj.optString("noteid"));
                    KwlReturnObject result = accCreditNoteDAOobj.deleteCreditNote(cnid, companyid);

                    result = accCreditNoteDAOobj.getJEFromCN(cnid);
                    List list = result.getEntityList();
                    Iterator itr = list.iterator();
                    while (itr.hasNext()) {
                        String jeid = (String) itr.next();
                        result = accJournalEntryobj.deleteJournalEntry(jeid, companyid);
                        //Delete entry from optimized table
                        accJournalEntryobj.deleteAccountJEs_optimized(jeid);
                    }

                    result = accCreditNoteDAOobj.getCNDFromCN(cnid);
                    list = result.getEntityList();
                    itr = list.iterator();
                    while (itr.hasNext()) {
                        String discountid = (String) itr.next();
                        result = accDiscountobj.deleteDiscountEntry(discountid, companyid);
                    }
                    
                    //Delete unrealised JE for Credit Note
                    accJournalEntryobj.permanentDeleteJournalEntryDetailReval(cnid, companyid);
                    accJournalEntryobj.permanentDeleteJournalEntryReval(cnid, companyid);
                    /*
                     * query = "update Discount di set di.deleted=true where
                     * di.ID in(select cnd.discount.ID from CreditNoteDetail cnd
                     * where cnd.creditNote.ID in( " + qMarks + ") and
                     * cnd.company.companyID=di.company.companyID) and
                     * di.company.companyID=?";
                     * HibernateUtil.executeUpdate(session, query,
                     * params.toArray());
                     */
                    result = accCreditNoteDAOobj.getCNDFromCND(cnid);
                    list = result.getEntityList();
                    itr = list.iterator();
                    while (itr.hasNext()) {
                        String discountid = (String) itr.next();
                        result = accDiscountobj.deleteDiscountEntry(discountid, companyid);
                    }

                    /*
                     * query = "update Inventory inv set inv.deleted=true where
                     * inv.ID in(select cnd.inventory.ID from CreditNoteDetail
                     * cnd where cnd.creditNote.ID in( " + qMarks + ") and
                     * cnd.company.companyID=inv.company.companyID) and
                     * inv.company.companyID=?";
                     * HibernateUtil.executeUpdate(session, query,
                     * params.toArray());
                     */
                    result = accCreditNoteDAOobj.getCNIFromCND(cnid);
                    list = result.getEntityList();
                    itr = list.iterator();
                    while (itr.hasNext()) {
                        String inventoryid = (String) itr.next();
                        result = accProductObj.deleteInventoryEntry(inventoryid, companyid);
                    }
                }
            }
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)));
        }
    }

    public ModelAndView saveBillingCreditNote(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CN_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            BillingCreditNote creditnote = saveBillingCreditNote(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.creditN.save", null, RequestContextUtils.getLocale(request));   //"Credit Note has been saved successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public BillingCreditNote saveBillingCreditNote(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException {
        BillingCreditNote creditnote = null;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            DateFormat df = authHandler.getDateOnlyFormat();
            String customfield = request.getParameter("customfield");
            HashMap<String, Object> GlobalParams = AccountingManager.getGlobalParams(request);
            String sequenceformat = request.getParameter("sequenceformat");

            Date creationDate = df.parse(request.getParameter("creationdate"));
            double externalCurrencyRate = StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(), companyid);
            Company company = (Company) kwlCommonTablesDAOObj.getClassObject(Company.class.getName(), companyid);
            KWLCurrency kwlcurrency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), currencyid);

            currencyid = (request.getParameter("currencyid") == null ? kwlcurrency.getCurrencyID() : request.getParameter("currencyid"));
            KWLCurrency currency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), currencyid);

            String entryNumber = request.getParameter("number");
            KwlReturnObject result = accCreditNoteDAOobj.getBCNFromNoteNo(entryNumber, companyid);
            int count = result.getRecordTotalCount();
            if (count > 0) {
                throw new AccountingException(messageSource.getMessage("acc.field.Creditnotenumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
            }
            HashMap<String, Object> cnDataMap = new HashMap<String, Object>();
            boolean seqformat_oldflag = StringUtil.getBoolean(request.getParameter("seqformat_oldflag"));
            String nextCNAutoNo = "";
            String nextAutoNoInt = "";
            if (seqformat_oldflag) {
                nextCNAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_BILLINGCREDITNOTE, sequenceformat);
            } else {
                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_BILLINGCREDITNOTE, sequenceformat, seqformat_oldflag, creationDate);
                nextCNAutoNo = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                nextAutoNoInt = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                cnDataMap.put(Constants.SEQFORMAT, sequenceformat);
                cnDataMap.put(Constants.SEQNUMBER, nextAutoNoInt);
            }
            cnDataMap.put("entrynumber", entryNumber);
            cnDataMap.put("autogenerated", nextCNAutoNo.equals(entryNumber));
            cnDataMap.put("memo", request.getParameter("memo"));
            cnDataMap.put(Constants.companyKey, company.getCompanyID());
            cnDataMap.put("currencyid", currency.getCurrencyID());

            Long seqNumber = null;
            result = accCreditNoteDAOobj.getBCNSequenceNo(companyid, creationDate);
            List list = result.getEntityList();
            if (!list.isEmpty()) {
                seqNumber = (Long) list.get(0);
            }
            cnDataMap.put("sequence", seqNumber.intValue());

            String costCenterId = request.getParameter("costCenterId");
            boolean jeautogenflag = true;
            String nextJEAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_JOURNALENTRY);
            String jeentryNumber = nextJEAutoNo;// + "/" + entryNumber;
            Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
            jeDataMap.put("entrynumber", jeentryNumber);
            jeDataMap.put("autogenerated", jeautogenflag);
            jeDataMap.put("entrydate", creationDate);
            jeDataMap.put(Constants.companyKey, company.getCompanyID());
            jeDataMap.put("memo", request.getParameter("memo"));
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                jeDataMap.put("costcenterid", costCenterId);
            }
            jeDataMap.put("currencyid", currencyid);
            HashSet<JournalEntryDetail> jedetails = new HashSet();
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            String jeid = journalEntry.getID();
            jeDataMap.put("jeid", jeid);
            cnDataMap.put("journalentryid", jeid);

            List CNlist = saveBillingCreditNoteRows1(GlobalParams, request, company, currency, journalEntry, preferences, externalCurrencyRate);
            Double totalAmount = (Double) CNlist.get(0);
            Double discAccAmount = (Double) CNlist.get(1);
            HashSet<CreditNoteDetail> cndetails = (HashSet<CreditNoteDetail>) CNlist.get(2);
            jedetails = (HashSet<JournalEntryDetail>) CNlist.get(3);

            JSONObject jedjson = new JSONObject();
            jedjson.put("srno", jedetails.size() + 1);
            jedjson.put(Constants.companyKey, company.getCompanyID());
            jedjson.put("amount", discAccAmount);
            jedjson.put("accountid", request.getParameter("accid"));
            jedjson.put("debit", false);
            jedjson.put("jeid", jeid);
            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetails.add(jed);


            jeDataMap.put("jedetails", jedetails);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);

            result = accCreditNoteDAOobj.saveBillingCreditNote(cnDataMap);
            creditnote = (BillingCreditNote) result.getEntityList().get(0);

            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                customrequestParams.put("modulerecid", journalEntry.getID());
                customrequestParams.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
                customrequestParams.put(Constants.companyKey, companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    jeDataMap.put("accjecustomdataref", journalEntry.getID());
                    jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);
                }
            }

            cnDataMap.put("id", creditnote.getID());
            Iterator itr = cndetails.iterator();
            while (itr.hasNext()) {
                BillingCreditNoteDetail cnd = (BillingCreditNoteDetail) itr.next();
                cnd.setCreditNote(creditnote);
            }
            cnDataMap.put("cndetails", cndetails);

            result = accCreditNoteDAOobj.saveBillingCreditNote(cnDataMap);
            creditnote = (BillingCreditNote) result.getEntityList().get(0);
            auditTrailObj.insertAuditLog(AuditAction.CREDIT_NOTE_CREATED, "User " + sessionHandlerImpl.getUserFullName(request) + " created new Credit Note ", request, creditnote.getID());

            //Add entry in optimized table
            accJournalEntryobj.saveAccountJEs_optimized(jeid);

        } catch (JSONException |ParseException| SessionExpiredException ex) {
            throw ServiceException.FAILURE("saveCreditNote : " + ex.getMessage(), ex);
        } 
        return creditnote;
    }

    public ModelAndView getBillingCreditNote(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            HashMap<String, Object> requestParams = getCreditNoteMap(request);
            KwlReturnObject result = accCreditNoteDAOobj.getBillingCreaditNote(requestParams);
            jobj = getBillingCreditNoteJson(request, result.getEntityList());
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getBillingCreditNoteJson(HttpServletRequest request, List list) throws ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> requestParams = getCreditNoteMap(request);
            double tax = 0;
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("df");

            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                BillingCreditNote creditMemo = (BillingCreditNote) row[0];
                JournalEntry je = creditMemo.getJournalEntry();
                Customer customer = (Customer) row[1];
                JournalEntryDetail details = (JournalEntryDetail) row[2];
                JSONObject obj = new JSONObject();
                obj.put("noteid", creditMemo.getID());
                obj.put("noteno", creditMemo.getCreditNoteNumber());
                obj.put("journalentryid", je.getID());
                obj.put("currencysymbol", (creditMemo.getCurrency() == null ? currency.getSymbol() : creditMemo.getCurrency().getSymbol()));
                obj.put("currencyid", (creditMemo.getCurrency() == null ? currency.getCurrencyID() : creditMemo.getCurrency().getCurrencyID()));
                obj.put("entryno", je.getEntryNumber());
                obj.put("personid", customer.getID());
                obj.put("personname", customer.getAccount().getName());
                obj.put("amount", details.getAmount());
                obj.put("date", df.format(je.getEntryDate()));
                obj.put("memo", creditMemo.getMemo());
                obj.put("deleted", creditMemo.isDeleted());
                obj.put("costcenterid", je.getCostcenter() == null ? "" : je.getCostcenter().getID());
                obj.put("costcenterName", je.getCostcenter() == null ? "" : je.getCostcenter().getName());

                KwlReturnObject result = accJournalEntryobj.getJournalEntryDetail(je.getID(), sessionHandlerImpl.getCompanyid(request));
                Iterator iterator = result.getEntityList().iterator();
                while (iterator.hasNext()) {
                    JournalEntryDetail jed = (JournalEntryDetail) iterator.next();
                    Account account = null;
                    account = jed.getAccount();
                    //To do - need to test.
                    if (account.getMastertypevalue() == Group.ACCOUNTTYPE_GST) {//GST transaction.
                        if (jed.isDebit()) {
                            tax = jed.getAmount();
                        }
                    }
                }
                result = accCreditNoteDAOobj.getTotalTax_TotalDiscount_Billing(creditMemo.getID());
                double totTax = 0, totDiscount = 0;
                if (result != null && result.getEntityList() != null) {
                    Iterator resItr = result.getEntityList().iterator();
                    Object[] sumRow = (Object[]) resItr.next();
                    if (sumRow[0] != null) {
                        totTax = Double.parseDouble(sumRow[0].toString());
                    }
                    if (sumRow[1] != null) {
                        totDiscount = Double.parseDouble(sumRow[1].toString());
                    }
                }

                obj.put("noteSubTotal", details.getAmount() + totDiscount - totTax);
                obj.put("notetax", tax);
                obj.put("totalTax", totTax);
                obj.put("totalDiscount", totDiscount);
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (JSONException | SessionExpiredException ex) {
            throw ServiceException.FAILURE("getBillingCreditNoteJson : " + ex.getMessage(), ex);
        } 
        return jobj;
    }

    public ModelAndView getBillingCreditNoteRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = getBillingCreditNoteRows(request);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getBillingCreditNoteRows(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            KWLCurrency currency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            String[] creditNote = request.getParameterValues("bills");
            int i = 0;
            JSONArray jArr = new JSONArray();

            HashMap<String, Object> cnRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("creditNote.ID");
            order_by.add("srno");
            order_type.add("asc");
            cnRequestParams.put("filter_names", filter_names);
            cnRequestParams.put("filter_params", filter_params);
            cnRequestParams.put("order_by", order_by);
            cnRequestParams.put("order_type", order_type);

            while (creditNote != null && i < creditNote.length) {
                BillingCreditNote cn = (BillingCreditNote) kwlCommonTablesDAOObj.getClassObject(BillingCreditNote.class.getName(), creditNote[i]);
                filter_params.clear();
                filter_params.add(cn.getID());
                KwlReturnObject grdresult = accCreditNoteDAOobj.getBillingCreditNoteDetails(cnRequestParams);
                Iterator itr = grdresult.getEntityList().iterator();

                while (itr.hasNext()) {
                    BillingCreditNoteDetail row = (BillingCreditNoteDetail) itr.next();
                    BillingInvoiceDetail invRow = row.getInvoiceRow();
                    JSONObject obj = new JSONObject();
                    obj.put("withoutinventory", true);
                    obj.put("billid", cn.getID());
                    obj.put("billno", cn.getCreditNoteNumber());
                    obj.put("srno", row.getSrno());
                    obj.put("rowid", row.getID());
                    obj.put("productid", invRow.getProductDetail());
                    obj.put("productdetail", invRow.getProductDetail());
                    obj.put("desc", invRow.getProductDetail());
                    obj.put("memo", row.getMemo());
                    obj.put("currencysymbol", (cn.getCurrency() == null ? currency.getCurrencyID() : cn.getCurrency().getSymbol()));
                    obj.put("transectionid", row.getInvoiceRow().getBillingInvoice().getID());
                    obj.put("transectionno", row.getInvoiceRow().getBillingInvoice().getBillingInvoiceNumber());
                    obj.put("remark", !StringUtil.isNullOrEmpty(row.getRemark()) ? row.getRemark() : "");
                    Discount disc = row.getDiscount();
                    if (disc != null) {
                        obj.put("discount", disc.getDiscountValue());
                    } else {
                        obj.put("discount", 0);
                    }
                    obj.put("quantity", row.getQuantity());
                    obj.put("taxamount", row.getTaxAmount());
                    jArr.put(obj);
                }
                i++;
                jobj.put("data", jArr);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getBillingCreditNoteRows : " + ex.getMessage(), ex);
        }
        return jobj;
    }

    public ModelAndView deleteBillingCreditNotes(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CN_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            deleteBillingCreditNotes(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.creditN.dels", null, RequestContextUtils.getLocale(request));   //"Credit Note(s) has been deleted successfully";
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void deleteBillingCreditNotes(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("noteid"))) {
                    String cnid = StringUtil.DecodeText(jobj.optString("noteid"));

                    KwlReturnObject result = accCreditNoteDAOobj.deleteBillingCreditNote(cnid, companyid);

                    result = accCreditNoteDAOobj.getJEFromBCN(cnid);
                    List list = result.getEntityList();
                    Iterator itr = list.iterator();
                    while (itr.hasNext()) {
                        String jeid = (String) itr.next();
                        result = accJournalEntryobj.deleteJournalEntry(jeid, companyid);
                        //Delete entry from optimized table
                        accJournalEntryobj.deleteAccountJEs_optimized(jeid);
                    }
                    result = accCreditNoteDAOobj.getCNDFromBCN(cnid);
                    list = result.getEntityList();
                    itr = list.iterator();
                    while (itr.hasNext()) {
                        String discountid = (String) itr.next();
                        result = accDiscountobj.deleteDiscountEntry(discountid, companyid);
                    }

                    result = accCreditNoteDAOobj.getCNDFromBCND(cnid);
                    list = result.getEntityList();
                    itr = list.iterator();
                    while (itr.hasNext()) {
                        String discountid = (String) itr.next();
                        result = accDiscountobj.deleteDiscountEntry(discountid, companyid);
                    }
                }
            }
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)));
        }
    }

    public ModelAndView exportCreditNote(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            HashMap<String, Object> requestParams = getCreditNoteMap(request);
            KwlReturnObject result = accCreditNoteDAOobj.getCreaditNote(requestParams);
            jobj = getCreditNoteJson(request, result.getEntityList());
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateOnlyFormat().format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    public ModelAndView exportBillingCreditNote(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            HashMap<String, Object> requestParams = getCreditNoteMap(request);
            KwlReturnObject result = accCreditNoteDAOobj.getBillingCreaditNote(requestParams);
            jobj = getBillingCreditNoteJson(request, result.getEntityList());
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

   

    public ModelAndView linkCreditNote(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            /*Get request parameters */
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            List li = accCreditNoteService.linkCreditNote(paramJobj, "",true);   // "true" flag is passed for inserting Audit Trial entry ( ERP-18558 )
            issuccess = true;
            msg = messageSource.getMessage("acc.field.CreditNotehasbeenLinkedtoCustomerInvoicesuccessfully", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /*
     * Update invoice amount due amount.
     */
    public void updateInvoiceAmountDue(Invoice invoice, Company company, double amountReceivedForInvoice, double amountReceivedInBaseCurrencyForInvoice) throws JSONException, ServiceException {
        if (invoice != null) {
            double invoiceAmountDue = invoice.getOpeningBalanceAmountDue();
            invoiceAmountDue -= amountReceivedForInvoice;
            JSONObject invjson = new JSONObject();
            invjson.put("invoiceid", invoice.getID());
            invjson.put("companyid", company.getCompanyID());
            invjson.put("openingBalanceAmountDue", invoiceAmountDue);
            invjson.put(Constants.openingBalanceBaseAmountDue, invoice.getOriginalOpeningBalanceBaseAmount() - amountReceivedInBaseCurrencyForInvoice);
            invjson.put(Constants.invoiceamountdue, invoice.getInvoiceamountdue() - amountReceivedForInvoice);
            invjson.put(Constants.invoiceamountdueinbase, invoice.getInvoiceAmountDueInBase()- amountReceivedInBaseCurrencyForInvoice);
            accInvoiceDAOObj.updateInvoice(invjson, null);
        }
    }
    
    
    private List saveBillingCreditNoteRows1(HashMap GlobalParams, HttpServletRequest request, Company company, KWLCurrency currency, JournalEntry je, CompanyAccountPreferences preferences, double externalCurrencyRate) throws ServiceException, AccountingException, SessionExpiredException {
        List resultlist = new ArrayList();
        double totalAmount = 0;
        double totalTax = 0;
        double discAccAmount = 0.0, discTotal = 0;
        HashSet cndetails = new HashSet();
        HashSet jedetails = new HashSet();
        String creditAccount = "";
        double totalrowdiscount = 0, totalprodTax = 0;
        double totalDiscount = StringUtil.getDouble(request.getParameter("totalInvoiceDiscount"));

        JournalEntryDetail jed;
        try {
            boolean includeTax = StringUtil.getBoolean(request.getParameter("includetax"));
            String currencyid = (request.getParameter("currencyid") == null ? currency.getCurrencyID() : request.getParameter("currencyid"));
            JSONArray jArr = new JSONArray(request.getParameter("productdetails"));
            List list = new ArrayList();
            for (int i = 0; i < jArr.length(); i++) {
                double taxamount = 0;
                double amount = 0;
                JSONObject jobj = jArr.getJSONObject(i);
                BillingCreditNoteDetail row = new BillingCreditNoteDetail();
                row.setSrno(i + 1);
                double disc = jobj.getDouble("discamount");
                discTotal = discTotal + disc;
                row.setCompany(company);
                row.setMemo(request.getParameter("memo"));
                row.setQuantity(jobj.getDouble("remquantity"));
                if (!StringUtil.isNullOrEmpty(jobj.getString("gridRemark"))) {
                    row.setRemark(jobj.getString("gridRemark"));
                }
                BillingInvoiceDetail invoiceRow = (BillingInvoiceDetail) kwlCommonTablesDAOObj.getClassObject(BillingInvoiceDetail.class.getName(), (StringUtil.isNullOrEmpty(jobj.getString("linkto"))) ? jobj.getString("rowid") : jobj.getString("savedrowid"));
                row.setInvoiceRow(invoiceRow);

                double percent = 0;

                double rowdiscount = 0;
                if (invoiceRow.getDiscount() != null && invoiceRow.getDiscount().getDiscountValue() != 0) {
                    rowdiscount = invoiceRow.getDiscount().getDiscountValue() / Double.parseDouble(jobj.getString("quantity"));
                    rowdiscount = rowdiscount * Double.parseDouble(jobj.getString("remquantity"));
                    totalrowdiscount = totalrowdiscount + rowdiscount;
                    HashMap<String, Object> jedMap = new HashMap();
                    jedMap.put("srno", jedetails.size() + 1);
                    jedMap.put("companyid", company.getCompanyID());
                    jedMap.put("amount", rowdiscount);
                    jedMap.put("accountid", preferences.getDiscountGiven().getID());
                    jedMap.put("debit", false);
                    jedMap.put("jeid", je.getID());
                    KwlReturnObject jedresult = accJournalEntryobj.updateJournalEntryDetails(jedMap);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                }
                double prodTax = 0.0;
                if (invoiceRow.getTax() != null) {
                    /*
                     * Product level tax taken care of
                     */

                    KwlReturnObject perresult = accTaxObj.getTaxPercent(company.getCompanyID(), invoiceRow.getBillingInvoice().getJournalEntry().getEntryDate(), invoiceRow.getTax().getID());
                    double percentRow = (Double) perresult.getEntityList().get(0);
                    prodTax = percentRow * jobj.getDouble("discamount") / (percentRow + 100);
                    totalprodTax = totalprodTax + prodTax;
                    JSONObject jedtaxjson = new JSONObject();
                    jedtaxjson.put("srno", jedetails.size() + 1);
                    jedtaxjson.put("companyid", company.getCompanyID());
                    jedtaxjson.put("amount", prodTax);
                    jedtaxjson.put("accountid", invoiceRow.getTax().getAccount().getID());
                    jedtaxjson.put("debit", true);
                    jedtaxjson.put("jeid", je.getID());
                    KwlReturnObject jedtaxresult = accJournalEntryobj.addJournalEntryDetails(jedtaxjson);
                    jed = (JournalEntryDetail) jedtaxresult.getEntityList().get(0);
                    jedetails.add(jed);

                    /*
                     * Product level tax taken care of
                     */
                }

                if (invoiceRow.getBillingInvoice().getTax() != null) {
//                    percent = CompanyHandler.getTaxPercent(session, request, invoiceRow.getBillingInvoice().getJournalEntry().getEntryDate(), invoiceRow.getBillingInvoice().getTax().getID());
                    KwlReturnObject perresult = accTaxObj.getTaxPercent(company.getCompanyID(), invoiceRow.getBillingInvoice().getJournalEntry().getEntryDate(), invoiceRow.getBillingInvoice().getTax().getID());
                    percent = (Double) perresult.getEntityList().get(0);
                }
                if (includeTax && invoiceRow.getBillingInvoice().getTax() != null && i == jArr.length() - 1) {

                    taxamount = (discTotal - (totalDiscount)) * percent / 100;
                    row.setTaxAmount(taxamount);
                    totalTax += taxamount;
                    if (includeTax && taxamount > 0) {
                        JSONObject jedjson = new JSONObject();
                        jedjson.put("srno", jedetails.size() + 1);
                        jedjson.put("companyid", company.getCompanyID());
                        jedjson.put("amount", taxamount);
                        jedjson.put("accountid", invoiceRow.getBillingInvoice().getTax().getAccount().getID());
                        jedjson.put("debit", true);
                        jedjson.put("jeid", je.getID());
                        KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jedetails.add(jed);
                    }
                }
                if (i == jArr.length() - 1) {
                    row.setTotalDiscount(totalDiscount);
                }
                discAccAmount = discAccAmount + jobj.getDouble("discamount");
                cndetails.add(row);
                if (disc > 0) {
                    JSONObject discjson = new JSONObject();
                    discjson.put("discount", disc);
                    discjson.put("inpercent", false);
                    KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, jobj.getDouble("amount"), currencyid, null, externalCurrencyRate);
                    discjson.put("originalamount", (Double) bAmt.getEntityList().get(0));
                    discjson.put("companyid", company.getCompanyID());
                    KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                    Discount discount = (Discount) dscresult.getEntityList().get(0);
                    row.setDiscount(discount);
                }
                double creditAmount1 = (jobj.getDouble("discamount") + rowdiscount - prodTax);
                JSONObject jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put("companyid", company.getCompanyID());
                jedjson.put("amount", creditAmount1);
                jedjson.put("accountid", jobj.getString("creditoraccount"));
                jedjson.put("debit", true);
                jedjson.put("jeid", je.getID());
                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jedetails.add(jed);
            }



            if (totalDiscount != 0) {
                HashMap<String, Object> jedMap = new HashMap();
                jedMap.put("srno", jedetails.size() + 1);
                jedMap.put("companyid", company.getCompanyID());
                jedMap.put("amount", totalDiscount);
                jedMap.put("accountid", preferences.getDiscountGiven().getID());
                jedMap.put("debit", false);
                jedMap.put("jeid", je.getID());
                KwlReturnObject jedresult = accJournalEntryobj.updateJournalEntryDetails(jedMap);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jedetails.add(jed);
            }



            if (discAccAmount != 0.0) {
                totalAmount += discAccAmount;
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("CompanyHandler.saveDebitNote", ex);
        }
        resultlist.add(totalAmount + totalrowdiscount);
        resultlist.add(discAccAmount - totalDiscount + totalTax);
        resultlist.add(cndetails);
        resultlist.add(jedetails);
        return resultlist;
    }

    public ModelAndView getCreditNoteMerged(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        String msg = "";
        boolean issuccess = false;
        try {
            boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
            HashMap map = accCreditNoteService.getCreditNoteCommonCode(request, response);
            DataJArr = (JSONArray) map.get("data");
            int cnt = (Integer) map.get("count");
            JSONArray pagedJson = DataJArr;
            if (consolidateFlag) {
                String start = request.getParameter(Constants.start);
                String limit = request.getParameter(Constants.limit);
                if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                    pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
                }
            }
            jobj.put("data", pagedJson);
            jobj.put("count", cnt);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    
    public ModelAndView getOpeningBalanceCNs(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = getCreditNoteMap(request);
            String customerId = request.getParameter("custVenId");
            requestParams.put("customerid", customerId);
            // get opening balance receipts created from opening balance button.
            KwlReturnObject result = accCreditNoteDAOobj.getOpeningBalanceCNs(requestParams);
            List<CreditNote> list = result.getEntityList();
            getgetOpeningBalanceCNJson(request, list, DataJArr);

            int count = result.getRecordTotalCount();
            JSONArray pagedJson = DataJArr;
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }

            jobj.put("data", pagedJson);
            jobj.put("count", count);
            issuccess = true;
        } catch (JSONException |ServiceException |SessionExpiredException ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }catch (Exception ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }

        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void getgetOpeningBalanceCNJson(HttpServletRequest request, List<CreditNote> list, JSONArray dataArray) {
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat();
            HashMap<String, Object> requestParams = getCreditNoteMap(request);
            HashMap<String, Object> badDebtMap = new HashMap<>();            
            badDebtMap.put("badDebtType", 0);
            KwlReturnObject badDebtResult=null;
            if (list != null && !list.isEmpty()) {
                for (CreditNote cn:list) {
                    boolean isLinkedInvoiceClaimed=false;
                    Date cnCreationDate = null;
                    Double cnAmount = 0d;

                    cnCreationDate = cn.getCreationDate();
                    cnAmount = cn.getCnamount();

                    double exchangeRateForOtherCurrency = cn.getExchangeRateForOpeningTransaction();
                    boolean isopeningBalanceCN = cn.isIsOpeningBalenceCN();

                    JSONObject cnJson = new JSONObject();
                    cnJson.put("transactionId", cn.getID());
                    cnJson.put("exchangeRateForOtherCurrency", exchangeRateForOtherCurrency);
                    cnJson.put("isCurrencyToBaseExchangeRate", cn.isConversionRateFromCurrencyToBase());
                    cnJson.put("isNormalTransaction", cn.isNormalCN());
                    cnJson.put("transactionNo", cn.getCreditNoteNumber());
                    cnJson.put("transactionAmount", authHandler.formattedAmount(cnAmount, companyid));
                    cnJson.put("transactionDate", df.format(cnCreationDate));
                    cnJson.put("currencysymbol", (cn.getCurrency() == null ? "" : cn.getCurrency().getSymbol()));
                    cnJson.put("currencyid", (cn.getCurrency() == null ? "" : cn.getCurrency().getCurrencyID()));
                    cnJson.put("transactionAmountDue", authHandler.formattedAmount(cn.getOpeningBalanceAmountDue(), companyid));
                    cnJson.put("narration", cn.getNarration());
                    cnJson.put("memo", (StringUtil.isNullOrEmpty(cn.getMemo())?"" : cn.getMemo()));
                    if(cn.getModifiedby()!=null){
                            cnJson.put("lasteditedby",StringUtil.getFullName(cn.getModifiedby()));
                    }
                    
                    KwlReturnObject bAmt = null;
                    if (isopeningBalanceCN && cn.isConversionRateFromCurrencyToBase()) {// if CN is opening balance CN and Conversion rate is taken from user is Currency to base then following method will be called.
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, cnAmount, cn.getCurrency().getCurrencyID(), cn.getCreationDate(), exchangeRateForOtherCurrency);
                    } else {
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, cnAmount, cn.getCurrency().getCurrencyID(), cn.getCreationDate(), exchangeRateForOtherCurrency);
                    }

                    double transactionAmountInBase = (Double) bAmt.getEntityList().get(0);
                    cnJson.put("transactionAmountInBase", authHandler.formattedAmount(transactionAmountInBase, companyid));
                    int isReval = 0;
                    KwlReturnObject brdAmt = accInvoiceDAOObj.getRevalFlag(cn.getID());
                    List reval = brdAmt.getEntityList();
                    if (!reval.isEmpty() && (Long) reval.get(0) > 0) {
                        isReval = 1;
                    }
                    cnJson.put("isreval", isReval);
                    
                    /*
                     * Applicable for Malaysian Country. Checking if any linked invoice is claimed 
                     */
                    Invoice invoice=null;
                    if(cn.getRows() != null && !cn.getRows().isEmpty()){
                        for(CreditNoteDetail noteDetail : cn.getRows()){
                            invoice = noteDetail.getInvoice();
                            
                            if (invoice!=null && (noteDetail.getInvoice().getBadDebtType() == 1 || noteDetail.getInvoice().getBadDebtType() == 2)) {
                                badDebtMap.put("invoiceid", noteDetail.getInvoice().getID());
                                badDebtMap.put("companyid", cn.getCompany().getCompanyID());
                                badDebtResult = accInvoiceDAOObj.getBadDebtInvoiceMappingForInvoice(badDebtMap);
                                List<BadDebtInvoiceMapping> maplist = badDebtResult.getEntityList();
                                if (maplist != null && !maplist.isEmpty()) {
                                    BadDebtInvoiceMapping mapping = maplist.get(0);
                                    //if (!isLinkedInvoiceClaimed && mapping.getBadDebtClaimedDate().getTime() > noteDetail.getInvoiceLinkDate()) {
                                    if (!isLinkedInvoiceClaimed && (mapping.getBadDebtClaimedDate().after(noteDetail.getInvoiceLinkDate()))) {
                                        isLinkedInvoiceClaimed = true;
                                        cnJson.put("isLinkedInvoiceIsClaimed", isLinkedInvoiceClaimed);
                                    }
                                }
                            }
                            
                        }
                    }
                    dataArray.put(cnJson);
                }
            }
        } catch (ServiceException |JSONException |SessionExpiredException ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public ModelAndView getOpeningBalanceVendorCNs(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = getCreditNoteMap(request);
            String customerId = request.getParameter("custVenId");
            requestParams.put("vendorid", customerId);
            // get opening balance receipts created from opening balance button.
            KwlReturnObject result = accCreditNoteDAOobj.getOpeningBalanceVendorCNs(requestParams);
            List<CreditNote> list = result.getEntityList();
            getOpeningBalanceVendorCNJson(request, list, DataJArr);

            int count = result.getRecordTotalCount();
            JSONArray pagedJson = DataJArr;
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }

            jobj.put("data", pagedJson);
            jobj.put("count", count);
            issuccess = true;
        } catch (ServiceException |JSONException |SessionExpiredException ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }

        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void getOpeningBalanceVendorCNJson(HttpServletRequest request, List<CreditNote> list, JSONArray dataArray) {
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat();
            HashMap<String, Object> requestParams = getCreditNoteMap(request);
            if (list != null && !list.isEmpty()) {
                for (CreditNote cn:list) {
                    Date cnCreationDate = null;
                    Double cnAmount = 0d;
                    cnCreationDate = cn.getCreationDate();
                    cnAmount = cn.getCnamount();

                    double exchangeRateForOtherCurrency = cn.getExchangeRateForOpeningTransaction();
                    boolean isopeningBalanceCN = cn.isIsOpeningBalenceCN();

                    JSONObject cnJson = new JSONObject();
                    cnJson.put("transactionId", cn.getID());
                    cnJson.put("exchangeRateForOtherCurrency", exchangeRateForOtherCurrency);
                    cnJson.put("isCurrencyToBaseExchangeRate", cn.isConversionRateFromCurrencyToBase());
                    cnJson.put("isNormalTransaction", cn.isNormalCN());
                    cnJson.put("transactionNo", cn.getCreditNoteNumber());
                    cnJson.put("transactionAmount", authHandler.formattedAmount(cnAmount, companyid));
                    cnJson.put("transactionDate", df.format(cnCreationDate));
                    cnJson.put("currencysymbol", (cn.getCurrency() == null ? "" : cn.getCurrency().getSymbol()));
                    cnJson.put("currencyid", (cn.getCurrency() == null ? "" : cn.getCurrency().getCurrencyID()));
                    cnJson.put("transactionAmountDue", authHandler.formattedAmount(cn.getOpeningBalanceAmountDue(), companyid));
                    cnJson.put("narration", cn.getNarration());
                    cnJson.put("memo", (StringUtil.isNullOrEmpty(cn.getMemo())? "" : cn.getMemo()));
                    if(cn.getModifiedby()!=null){
                            cnJson.put("lasteditedby",cn.getModifiedby().getFirstName()+" "+cn.getModifiedby().getLastName());
                    }
                    KwlReturnObject bAmt = null;
                    if (isopeningBalanceCN && cn.isConversionRateFromCurrencyToBase()) {// if CN is opening balance CN and Conversion rate is taken from user is Currency to base then following method will be called.
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, cnAmount, cn.getCurrency().getCurrencyID(), cn.getCreationDate(), exchangeRateForOtherCurrency);
                    } else {
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, cnAmount, cn.getCurrency().getCurrencyID(), cn.getCreationDate(), exchangeRateForOtherCurrency);
                    }

                    double transactionAmountInBase = (Double) bAmt.getEntityList().get(0);
                    cnJson.put("transactionAmountInBase", authHandler.formattedAmount(transactionAmountInBase, companyid));
                    int isReval = 0;
                    KwlReturnObject brdAmt = accInvoiceDAOObj.getRevalFlag(cn.getID());
                    List reval = brdAmt.getEntityList();
                    if (!reval.isEmpty() && (Long) reval.get(0) > 0) {
                        isReval = 1;
                    }
                    cnJson.put("isreval", isReval);
                    HashMap<String,Object> reqParams1 = new HashMap<>();
                    reqParams1.put("cnid",cn.getID());
                    reqParams1.put("companyid",cn.getCompany().getCompanyID());
                    KwlReturnObject linkResult=accCreditNoteDAOobj.getLinkDetailPaymentToCreditNote(reqParams1);
                    if(!linkResult.getEntityList().isEmpty()){
                        cnJson.put("isNoteLinkedToAdvancePayment", true);
                    }
                    dataArray.put(cnJson);
                }
            }
        } catch (ServiceException | JSONException |SessionExpiredException ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public ModelAndView deleteCreditNoteTemporary(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            String url = this.getServletContext().getInitParameter(Constants.inventoryURL);
            paramJobj.put(Constants.inventoryURL, url);
            jobj = accCreditNoteService.deleteCreditNoteTemporary(paramJobj);
        } catch (JSONException | SessionExpiredException ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getNoteType(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            HashMap<String, Object> requestParams = getCreditNoteMap(request);;

            KwlReturnObject result = accCreditNoteDAOobj.getNoteType(requestParams);
            List list = result.getEntityList();
            int count = result.getRecordTotalCount();
            JSONArray DataJArr = getNoteType(request, list);
            jobj.put("data", DataJArr);
            jobj.put("count", count);
            issuccess = true;
        } catch (SessionExpiredException ex) {

            msg = ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {

            msg = "" + ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getNoteType(HttpServletRequest request, List list) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                NoteType noteType = (NoteType) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("id", noteType.getId());
                obj.put("typeid", noteType.getId());
                obj.put("name", noteType.getName());
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getNoteType : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView saveNoteType(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CN_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            msg = saveNoteType(request);
            issuccess = true;
            if (msg.equals("")) {
                msg = messageSource.getMessage("acc.notetype.save", null, RequestContextUtils.getLocale(request));   //" Note Type has been saved successfully";
            }
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public String saveNoteType(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException, UnsupportedEncodingException {

        NoteType noteType = null;
        String msg = "";
        try {
            int delCount = 0;
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            JSONArray jDelArr = new JSONArray(request.getParameter("deleteddata"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject typeresult = null;
            for (int i = 0; i < jDelArr.length(); i++) {
                JSONObject jobj = jDelArr.getJSONObject(i);
                String name = jobj.getString("name");
                if (StringUtil.isNullOrEmpty(jobj.getString("id")) == false && Integer.parseInt(jobj.getString("id")) != 0 && Integer.parseInt(jobj.getString("id")) != 1 && Integer.parseInt(jobj.getString("id")) != 2 && Integer.parseInt(jobj.getString("id")) != 3) {
                    try {
                        typeresult = accCreditNoteDAOobj.deleteNoteType(Integer.parseInt(jobj.getString("id")));
                        delCount += typeresult.getRecordTotalCount();
                        auditTrailObj.insertAuditLog(AuditAction.NOTE_TYPE_CREATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted Note type " + name, request, companyid);
                    } catch (ServiceException ex) {
                        throw new AccountingException(messageSource.getMessage("acc.uom.excp1", null, RequestContextUtils.getLocale(request)));
                    }
                } else if (StringUtil.isNullOrEmpty(jobj.getString("id")) == false) {
                    if (Integer.parseInt(jobj.getString("id")) == 0 || Integer.parseInt(jobj.getString("id")) == 1 || Integer.parseInt(jobj.getString("id")) == 2 || Integer.parseInt(jobj.getString("id")) == 3) {
                        msg = messageSource.getMessage("acc.field.CannotdeletedefaultNoteTypes", null, RequestContextUtils.getLocale(request));
                    }
                }
            }

            String auditMsg = "", auditID = "";
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                String name = jobj.getString("name");
                if (jobj.getBoolean("modified") == false) {
                    continue;
                }
                HashMap<String, Object> typeMap = new HashMap<String, Object>();
                typeMap.put("name", StringUtil.DecodeText(jobj.optString("name")));

                if (StringUtil.isNullOrEmpty(jobj.getString("id"))) {
                    auditMsg = "added";
                    auditID = AuditAction.NOTE_TYPE_CREATED;
                } else {
                    typeMap.put("id", Integer.parseInt(jobj.getString("id")));
                    auditMsg = "updated";
                    auditID = AuditAction.NOTE_TYPE_UPDATED;
                }
                if (!StringUtil.isNullOrEmpty(companyid)) {
                    typeMap.put("companyid", companyid);
                }
                if (!StringUtil.isNullOrEmpty(jobj.getString("id")) && Integer.parseInt(jobj.getString("id")) != 0 && Integer.parseInt(jobj.getString("id")) != 1 && Integer.parseInt(jobj.getString("id")) != 2 && Integer.parseInt(jobj.getString("id")) != 3) {
                    typeresult = accCreditNoteDAOobj.saveNoteTypes(typeMap);
                    noteType = (NoteType) typeresult.getEntityList().get(0);
                    auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + auditMsg + " Note type " + name, request, companyid);
                } else {
                    if (StringUtil.isNullOrEmpty(jobj.getString("id"))) {
                        typeresult = accCreditNoteDAOobj.saveNoteTypes(typeMap);
                        noteType = (NoteType) typeresult.getEntityList().get(0);
                        auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + auditMsg + " Note type " + name, request, companyid);
                    } else {
                        msg = messageSource.getMessage("acc.field.CannoteditdefaultNoteTypes", null, RequestContextUtils.getLocale(request));
                    }
                }
            }
        } catch (JSONException | SessionExpiredException ex) {
            throw ServiceException.FAILURE("saveNoteType : " + ex.getMessage(), ex);
        }
        return msg;
    }


    public ModelAndView updatePrint(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            KwlReturnObject result = null;
            String recordids = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("recordids"))) {
                recordids = request.getParameter("recordids");
            }
            ArrayList<String> SOIDList = CustomDesignHandler.getSelectedBillIDs(recordids);
            for (int cnt = 0; cnt < SOIDList.size(); cnt++) {
                HashMap<String, Object> hm = new HashMap<String, Object>();
                hm.put("cnid", SOIDList.get(cnt));
                hm.put("isprinted", request.getParameter("isprinted"));
                if (!StringUtil.isNullOrEmpty(SOIDList.get(cnt))) {
                    result = accCreditNoteDAOobj.updateCreditNote(hm);
                }
            }
            msg = messageSource.getMessage("acc.field.setsuccessfully", null, RequestContextUtils.getLocale(request));
            issuccess = true;
            txnManager.commit(status);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView saveCreditNoteAgainstInvoice(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        String creditNoteNumBer = "";
        String JENumBer = "";

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CN_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            List returnList = saveCreditNoteAgainstInvoice(request, jobj);

            String companyid = sessionHandlerImpl.getCompanyid(request);
            String oldJeId = (String) returnList.get(0);
            CreditNote creditnote = (CreditNote) returnList.get(1);

            creditNoteNumBer = creditnote.getCreditNoteNumber();
            JENumBer = creditnote.getJournalEntry().getEntryNumber();
            issuccess = true;
            msg = messageSource.getMessage("acc.creditN.save", null, RequestContextUtils.getLocale(request)) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + creditNoteNumBer + ",</b>" + messageSource.getMessage("acc.field.JENo", null, RequestContextUtils.getLocale(request)) + ": <b>" + JENumBer + "</b>";   //"Credit Note has been saved successfully";
            txnManager.commit(status);

            status = txnManager.getTransaction(def);
            accCreditNoteService.deleteJEArray(oldJeId, companyid);
            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public List saveCreditNoteAgainstInvoice(HttpServletRequest request, JSONObject returnJobj) throws ServiceException, SessionExpiredException, AccountingException {
        CreditNote creditnote = null;
        KwlReturnObject result;
        List ll = new ArrayList();
        String oldjeid = "";
        String auditMsg = "", auditID = "";
        try {
            boolean reloadInventory = false;//Flag used to reload inventory on Client Side If CN type equals to "Return" or "Defective"
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            DateFormat df = authHandler.getDateOnlyFormat();
            String customfield = request.getParameter("customfield");
            double externalCurrencyRate = StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            boolean otherwise = request.getParameter("otherwise") != null;
            String sequenceformat = request.getParameter("sequenceformat");
            String createdby = sessionHandlerImpl.getUserid(request);
            String creditNoteId = request.getParameter("noteid");
            String modifiedby = sessionHandlerImpl.getUserid(request);
            long createdon = System.currentTimeMillis();
            long updatedon = createdon;
            String jeentryNumber = "";
            boolean jeautogenflag = false;
            String jeIntegerPart = "";
            String jeDatePrefix = "";
            String jeDateAfterPrefix = "";
            String jeDateSuffix = "";
            String jeSeqFormatId = "";
            boolean isEditNote = false;
            String entryNumber = request.getParameter("number");
            HashMap<String, Object> GlobalParams = AccountingManager.getGlobalParams(request);

            Date creationDate = df.parse(request.getParameter("billdate"));
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);

            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);

            HashMap<String, Object> credithm = new HashMap<String, Object>();

            if (!StringUtil.isNullOrEmpty(creditNoteId)) {
                //In case of edit if format is NA then entry number can be changed so need to check duplicate
                result = accCreditNoteDAOobj.getCNFromNoteNoAndId(entryNumber, companyid, creditNoteId);
                if (result.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
                    throw new AccountingException(messageSource.getMessage("acc.field.Creditnotenumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                }
                if (sequenceformat.equals("NA")) {
                    credithm.put("entrynumber", entryNumber);
                }

                isEditNote = true;
                KwlReturnObject cnObj = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), creditNoteId);
                creditnote = (CreditNote) cnObj.getEntityList().get(0);
                oldjeid = creditnote.getJournalEntry().getID();
                JournalEntry jetemp = creditnote.getJournalEntry();
                if (jetemp != null) {
                    jeentryNumber = jetemp.getEntryNumber(); //preserving these data to generate same JE number in edit case                    
                    jeautogenflag = jetemp.isAutoGenerated();
                    jeSeqFormatId = jetemp.getSeqformat() == null ? "" : jetemp.getSeqformat().getID();
                    jeIntegerPart = String.valueOf(jetemp.getSeqnumber());
                }
                // chk for open
                if (creditnote != null) {
                    accCreditNoteService.updateOpeningInvoiceAmountDue(creditnote.getID(), companyid);
                }

                result = accCreditNoteDAOobj.deleteCreditNoteDetails(creditnote.getID(), companyid);
                result = accCreditNoteDAOobj.deleteCreditTaxDetails(creditnote.getID(), companyid);

                //Delete old entries and insert new entries again from optimized table in edit case.
                accJournalEntryobj.deleteOnEditAccountJEs_optimized(oldjeid);
                accCreditNoteService.deleteJEDetailsCustomData(oldjeid);
                credithm.put("cnid", creditNoteId);

            } else {
                synchronized (this) {
                    result = accCreditNoteDAOobj.getCNFromNoteNo(entryNumber, companyid);
                    int count = result.getRecordTotalCount();
                    if (count > 0 && sequenceformat.equals("NA")) {
                        throw new AccountingException(messageSource.getMessage("acc.field.Creditnotenumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                    }
                    String nextCNAutoNo = "";
                    String nextAutoNoInt = "";
                    String datePrefix = "";
                    String dateafterPrefix = "";
                    String dateSuffix = "";

                    if (!sequenceformat.equals("NA")) {
                        boolean seqformat_oldflag = StringUtil.getBoolean(request.getParameter("seqformat_oldflag"));
                        if (seqformat_oldflag) {
                            nextCNAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_CREDITNOTE, sequenceformat);
                        } else {
                            Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_CREDITNOTE, sequenceformat, seqformat_oldflag, creationDate);
                            nextCNAutoNo = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                            nextAutoNoInt = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                            datePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                            dateafterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                            dateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                            credithm.put(Constants.SEQFORMAT, sequenceformat);
                            credithm.put(Constants.SEQNUMBER, nextAutoNoInt);
                            credithm.put(Constants.DATEPREFIX, datePrefix);
                            credithm.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
                            credithm.put(Constants.DATESUFFIX, dateSuffix);
                        }
                        entryNumber = nextCNAutoNo;
                    }

                    credithm.put("entrynumber", entryNumber);
                    credithm.put("autogenerated", nextCNAutoNo.equals(entryNumber));
                    credithm.put("oldRecord", false);
                }

                Long seqNumber = null;
                result = accCreditNoteDAOobj.getCNSequenceNo(companyid, creationDate);
                List list = result.getEntityList();
                if (!list.isEmpty()) {
                    seqNumber = (Long) list.get(0);
                }
                credithm.put("sequence", seqNumber.intValue());
            }

            currencyid = (request.getParameter("currencyid") == null ? kwlcurrency.getCurrencyID() : request.getParameter("currencyid"));
            curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            String costCenterId = !StringUtil.isNullOrEmpty(request.getParameter("costCenterId"))?request.getParameter("costCenterId"):"";     
            credithm.put("memo", (!StringUtil.isNullOrEmpty(request.getParameter("memo")))?request.getParameter("memo"):"");
            credithm.put("companyid", company.getCompanyID());
            credithm.put("currencyid", currencyid);
            credithm.put("createdby", createdby);
            credithm.put("modifiedby", modifiedby);
            credithm.put("createdon", createdon);
            credithm.put("updatedon", updatedon);
            credithm.put("costcenter", costCenterId);
            credithm.put("creationDate", creationDate);

            if (StringUtil.isNullOrEmpty(oldjeid)) {
                synchronized (this) {
                    HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                    JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                    JEFormatParams.put("modulename", "autojournalentry");
                    JEFormatParams.put("companyid", companyid);
                    JEFormatParams.put("isdefaultFormat", true);

                    KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                    SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, creationDate);
                    jeentryNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    jeIntegerPart = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                    jeDatePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);//Date Prefix Part
                    jeDateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                    jeSeqFormatId = format.getID();
                    jeautogenflag = true;
                }
            }

            Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
            jeDataMap.put("entrynumber", jeentryNumber);
            jeDataMap.put("autogenerated", jeautogenflag);
            jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
            jeDataMap.put(Constants.SEQNUMBER, jeIntegerPart);
            jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
            jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
            jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
            jeDataMap.put("entrydate", creationDate);
            jeDataMap.put("companyid", company.getCompanyID());
            jeDataMap.put("memo", request.getParameter("memo"));
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                jeDataMap.put("costcenterid", costCenterId);
            }
            jeDataMap.put("currencyid", currencyid);
            HashSet<JournalEntryDetail> jedetails = new HashSet();
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            String jeid = journalEntry.getID();
            jeDataMap.put("jeid", jeid);
            credithm.put("journalentryid", jeid);
            List CNlist = new ArrayList();
            double cnamount = 0.0;
            double cnamountdue = 0.0;
            
            
            credithm.put("vendorid", request.getParameter("vendor"));
            
            KwlReturnObject venObj = accountingHandlerDAOobj.getObject(Vendor.class.getName(), request.getParameter("vendor"));
            Vendor vendor = (Vendor) venObj.getEntityList().get(0);
            credithm.put("otherwise", true);
            credithm.put("openflag", true);
            credithm.put("cntype", 4);
            CNlist = saveCreditNoteAgainstInvoiceRows(GlobalParams, request, company, currency, journalEntry, preferences, externalCurrencyRate);
            Double totalAmount = (Double) CNlist.get(3);
            HashSet<CreditNoteDetail> cndetails = (HashSet<CreditNoteDetail>) CNlist.get(0);
            jedetails = (HashSet<JournalEntryDetail>) CNlist.get(1);
            HashSet<CreditNoteTaxEntry> creditNoteTaxEntryDetails = (HashSet<CreditNoteTaxEntry>) CNlist.get(2);
            reloadInventory = false;//(Boolean) CNlist.get(4);
            returnJobj.put("reloadInventory", reloadInventory);
            
            cnamount = (Double) CNlist.get(3);
            
            credithm.put("cnamount", cnamount);
            credithm.put("cnamountdue", cnamount);

            double termTotalAmount = 0;
            HashMap<String, Double> termAcc = new HashMap<String, Double>();
            String cnTerms = request.getParameter("invoicetermsmap");
            if (!StringUtil.isNullOrEmpty(cnTerms)) {
                JSONArray termsArr = new JSONArray(cnTerms);
                for (int cnt = 0; cnt < termsArr.length(); cnt++) {
                    double termamount = termsArr.getJSONObject(cnt).getDouble("termamount");
                    termTotalAmount += termamount;
                    if (termAcc.containsKey(termsArr.getJSONObject(cnt).getString("glaccount"))) {
                        double tempAmount = termAcc.get(termsArr.getJSONObject(cnt).getString("glaccount"));
                        termAcc.put(termsArr.getJSONObject(cnt).getString("glaccount"), termamount + tempAmount);
                    } else {
                        termAcc.put(termsArr.getJSONObject(cnt).getString("glaccount"), termamount);
                    }
                }
            }
            totalAmount += termTotalAmount;


            JSONObject jedjson = new JSONObject();
            jedjson.put("srno", jedetails.size() + 1);
            jedjson.put("companyid", company.getCompanyID());
            jedjson.put("amount", totalAmount);
            jedjson.put("accountid", vendor.getAccount().getID());
            jedjson.put("debit", false);
            jedjson.put("jeid", jeid);
            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetails.add(jed);



            if (termAcc.size() > 0) {
                for (Map.Entry<String, Double> entry : termAcc.entrySet()) {
                    jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", entry.getValue() > 0 ? entry.getValue() : (entry.getValue() * (-1)));
                    jedjson.put("accountid", entry.getKey());
                    jedjson.put("debit", entry.getValue() > 0 ? true : false);
                    jedjson.put("jeid", jeid);
                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                }
            }

            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
// handle it for edit.
            if (isEditNote) {
                result = accCreditNoteDAOobj.updateCreditNote(credithm);
                auditID = AuditAction.CREDIT_NOTE_MODIFIED;
                auditMsg = "updated";
            } else {
                credithm.put("approvestatuslevel", 11);
                result = accCreditNoteDAOobj.addCreditNote(credithm);
                auditID = AuditAction.CREDIT_NOTE_CREATED;
                auditMsg = "added";
            }

            creditnote = (CreditNote) result.getEntityList().get(0);
            jeDataMap.put("jedetails", jedetails);  
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeDataMap.put("transactionId", creditnote.getID()); // Adding CN ID as a transactionId in JournalEntry
            jeDataMap.put("transactionModuleid", Constants.Acc_Credit_Note_ModuleId);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                customrequestParams.put("modulerecid", journalEntry.getID());
                customrequestParams.put("moduleid", Constants.Acc_Credit_Note_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    jeDataMap.put("accjecustomdataref", journalEntry.getID());
                    jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);
                }
            }
            credithm.put("cnid", creditnote.getID());
            Iterator itr = cndetails.iterator();
            while (itr.hasNext()) {
                CreditNoteDetail cnd = (CreditNoteDetail) itr.next();
                cnd.setCreditNote(creditnote);
            }
            credithm.put("cndetails", cndetails);

            Iterator cntaxitr = creditNoteTaxEntryDetails.iterator();
            while (cntaxitr.hasNext()) {
                CreditNoteTaxEntry noteTaxEntry = (CreditNoteTaxEntry) cntaxitr.next();
                noteTaxEntry.setCreditNote(creditnote);
            }
            credithm.put("creditNoteTaxEntryDetails", creditNoteTaxEntryDetails);

            result = accCreditNoteDAOobj.updateCreditNote(credithm);
            creditnote = (CreditNote) result.getEntityList().get(0);

            //Add entry in optimized table
            accJournalEntryobj.saveAccountJEs_optimized(jeid);


            if (preferences.isInventoryAccountingIntegration()) {

                String action = "17";
                boolean isDirectUpdateInvFlag = false;
                if (preferences.isUpdateInvLevel()) {
                    isDirectUpdateInvFlag = true;
                    action = "19";//Direct Inventory Update action
                }

                JSONArray productArray = new JSONArray();
                if (!StringUtil.isNullOrEmpty(request.getParameter("productdetails"))) {
                    JSONArray jArr = new JSONArray(request.getParameter("productdetails"));
                    for (int i = 0; i < jArr.length(); i++) {
                        JSONObject jobj = jArr.getJSONObject(i);
                        KwlReturnObject proresult = accountingHandlerDAOobj.getObject(Product.class.getName(), jobj.getString("productid"));
                        Product product = (Product) proresult.getEntityList().get(0);
                        if (reloadInventory) { //check inventory flag and then update inventory to the inventory systeam

                            KwlReturnObject inResult = accountingHandlerDAOobj.getObject(InvoiceDetail.class.getName(), (StringUtil.isNullOrEmpty(jobj.getString("linkto"))) ? jobj.getString("rowid") : jobj.getString("savedrowid"));
                            InvoiceDetail invoiceRow = (InvoiceDetail) inResult.getEntityList().get(0);

                            boolean updateInventoryFlag = invoiceRow.getInventory().isInvrecord();

                            if (preferences.isWithInvUpdate()) {
                                updateInventoryFlag = accCreditNoteService.getInvoiceStatusForDO(invoiceRow);
                            }

                            if (updateInventoryFlag) {
                                JSONObject productObject = new JSONObject();
                                productObject.put("itemUomId", jobj.getString("uomid"));
                                productObject.put("itemBaseUomRate", jobj.getDouble("baseuomrate"));
                                productObject.put("itemQuantity", jobj.getDouble("remquantity") * jobj.getDouble("baseuomrate"));
                                productObject.put("quantity", jobj.getDouble("remquantity") * jobj.getDouble("baseuomrate"));
                                //productObject.put("itemQuantity", jobj.getDouble("remquantity"));
                                productObject.put("itemCode", product.getProductid());
                                if (isDirectUpdateInvFlag) {
                                    productObject.put("storeid", jobj.getString("invstore"));
                                    productObject.put("locationid", jobj.getString("invlocation"));
                                    productObject.put("rate", jobj.getDouble("rate"));
                                }
                                productArray.put(productObject);

                            }
                        }
                    }
                    if (productArray.length() > 0) {

                        String sendDateFormat = "yyyy-MM-dd";
                        DateFormat dateformat = new SimpleDateFormat(sendDateFormat);
                        Date date = df.parse(request.getParameter("creationdate"));
                        String stringDate = dateformat.format(date);

                        JSONObject jSONObject = new JSONObject();
                        jSONObject.put("deliveryDate", stringDate);
                        jSONObject.put("dateFormat", sendDateFormat);
                        jSONObject.put("details", productArray);
                        jSONObject.put("orderNumber", entryNumber);
                        jSONObject.put("selling", true);

                        String url = this.getServletContext().getInitParameter(Constants.inventoryURL);
                        CommonFnController cfc = new CommonFnController();
                        cfc.updateInventoryLevel(request, jSONObject, url, action);
                    }
                }
            }

            cnTerms = request.getParameter("invoicetermsmap");
            List<HashMap<String, Object>> creditTermDataList = new ArrayList();
            if (StringUtil.isAsciiString(cnTerms)) {
                creditTermDataList = accCreditNoteService.mapDebitTerms(cnTerms, creditnote.getID(), sessionHandlerImpl.getUserid(request));

            }

            ll.add(oldjeid);
            ll.add(creditnote);
            auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + auditMsg + " a Credit Note " + creditnote.getCreditNoteNumber(), request, creditnote.getID());
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveCreditNote : " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveCreditNote : " + ex.getMessage(), ex);
        }
        return ll;
    }
    
    private List saveCreditNoteAgainstInvoiceRows(HashMap GlobalParams, HttpServletRequest request, Company company, KWLCurrency currency, JournalEntry je, CompanyAccountPreferences preferences, double externalCurrencyRate) throws JSONException, ServiceException, SessionExpiredException, ParseException, AccountingException {
        List srRowsDetails = new ArrayList();
        double totalCNAmt = 0;
        double totalCNAmtExludingTax = 0;
        HashSet cndetails = new HashSet();
        HashSet cnTaxEntryDetails = new HashSet();
        HashSet jedetails = new HashSet();
        JSONArray jArr = new JSONArray(request.getParameter("detail"));

        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);
            KwlReturnObject proresult = accountingHandlerDAOobj.getObject(Product.class.getName(), jobj.getString("productid"));
            Product product = (Product) proresult.getEntityList().get(0);

            String linkMode = request.getParameter("fromLinkCombo");

            String invoiceDetailsId = "";

            if (!StringUtil.isNullOrEmpty(linkMode)) {
                if (linkMode.equalsIgnoreCase("Delivery Order")) {
                } else if (linkMode.equalsIgnoreCase("Customer Invoice") || linkMode.equalsIgnoreCase("Sales Invoice")) {
                    invoiceDetailsId = jobj.getString("rowid");
                }
                else if (linkMode.equalsIgnoreCase("Purchase Invoice") || linkMode.equalsIgnoreCase("Vendor Invoice")) {
                    invoiceDetailsId = jobj.getString("rowid");
                }
            }

            String CreditNoteDetailID = StringUtil.generateUUID();
            CreditNoteDetail cnDetailRow = new CreditNoteDetail();
            cnDetailRow.setSrno(i + 1);
            cnDetailRow.setID(CreditNoteDetailID);
            cnDetailRow.setTotalDiscount(0.00);
            cnDetailRow.setCompany(company);
            
            if (!StringUtil.isNullOrEmpty(invoiceDetailsId) && linkMode.equalsIgnoreCase("Customer Invoice")) {
                KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(InvoiceDetail.class.getName(), invoiceDetailsId);
                InvoiceDetail id = (InvoiceDetail) rdresult.getEntityList().get(0);
                Invoice invoice = id.getInvoice();
                cnDetailRow.setInvoice(invoice);
            }
            if (!StringUtil.isNullOrEmpty(invoiceDetailsId) && linkMode.equalsIgnoreCase("Purchase Invoice")) {
                KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(GoodsReceiptDetail.class.getName(), invoiceDetailsId);
                GoodsReceiptDetail grDetails = (GoodsReceiptDetail) rdresult.getEntityList().get(0);
                GoodsReceipt gr = grDetails.getGoodsReceipt();
                cnDetailRow.setGoodsReceipt(gr);
                cnDetailRow.setGoodsReceiptRow(grDetails);
                
            }
            
            CreditNoteTaxEntry taxEntry = new CreditNoteTaxEntry();
            String CreditNoteTaxID = StringUtil.generateUUID();
            taxEntry.setID(CreditNoteTaxID);
            String sales_accid = product.getSalesAccount().getID();

            if (i == 0) {// create  cndetail entry only once in this case i.e if multitple Products are linked.
                cndetails.add(cnDetailRow);
            }

            double amountExcludingTax = 0;
            double rowtaxamountFromJS = jobj.optDouble("taxamounttoadjust", 0);


            totalCNAmt += jobj.optDouble("adjustedamount", 0);
            amountExcludingTax = jobj.optDouble("adjustedamount", 0) - rowtaxamountFromJS;
            totalCNAmtExludingTax += amountExcludingTax;

            JSONObject jedjson = new JSONObject();
            jedjson.put("srno", jedetails.size() + 1);
            jedjson.put("companyid", company.getCompanyID());
            jedjson.put("amount", amountExcludingTax);
            jedjson.put("accountid", sales_accid);
            jedjson.put("debit", true);
            jedjson.put("jeid", je.getID());
            jedjson.put("description", jobj.optString("description"));
            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetails.add(jed);


            // Entering data for CN Tax Entry

            double rowtaxamount = 0d;
            String rowTaxJeId = "";
            String rowtaxid = jobj.optString("prtaxid", "");
            KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), rowtaxid);
            Tax rowtax = (Tax) txresult.getEntityList().get(0);

            if (rowtax != null) {
                rowtaxamount = jobj.optDouble("taxamounttoadjust", 0);
                jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put("companyid", company.getCompanyID());
                jedjson.put("amount", authHandler.formattedAmount(rowtaxamount, company.getCompanyID()));
                jedjson.put("accountid", rowtax.getAccount().getID());
                jedjson.put("debit", true);
                jedjson.put("jeid", je.getID());
                jedjson.put("description", jobj.optString("description"));
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jedetails.add(jed);

                rowTaxJeId = jed.getID();
            }

            KwlReturnObject accountresult = accountingHandlerDAOobj.getObject(Account.class.getName(), sales_accid);
            Account account = (Account) accountresult.getEntityList().get(0);

            taxEntry.setAccount(account);
            taxEntry.setAmount(amountExcludingTax);
            taxEntry.setCompany(company);
            if (!StringUtil.isNullOrEmpty(jobj.optString("reason", ""))) {
                KwlReturnObject reasonresult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), jobj.optString("reason", ""));
                MasterItem reason = (MasterItem) reasonresult.getEntityList().get(0);
                taxEntry.setReason(reason);
            }
            taxEntry.setDescription(jobj.getString("description"));
            taxEntry.setIsForDetailsAccount(true);
            taxEntry.setTax(rowtax);
            taxEntry.setTaxJedId(rowTaxJeId);
            taxEntry.setTaxamount(rowtaxamount);

            cnTaxEntryDetails.add(taxEntry);
        }
        
        srRowsDetails.add(cndetails);
        srRowsDetails.add(jedetails);
        srRowsDetails.add(cnTaxEntryDetails);
        srRowsDetails.add(totalCNAmt);
        srRowsDetails.add(totalCNAmtExludingTax);

        return srRowsDetails;
    }
    public ModelAndView getCreditNoteMergedForPayment(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        String msg = "";
        boolean issuccess = false;
        try {
            HashMap<String, Object> requestParams = getCreditNoteMap(request);
            boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
            String[] companyids = (consolidateFlag && request.getParameter("companyids") != null) ? request.getParameter("companyids").split(",") : sessionHandlerImpl.getCompanyid(request).split(",");
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid") != null) ? request.getParameter("gcurrencyid") : sessionHandlerImpl.getCurrencyID(request);
            boolean isEdit = request.getParameter("isEdit") == null ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
            requestParams.put("isEdit", isEdit);
            
            boolean onlyAmountDue = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("onlyAmountDue"))) {
                onlyAmountDue = Boolean.parseBoolean(request.getParameter("onlyAmountDue"));
            }
            requestParams.put("onlyAmountDue", onlyAmountDue);
            
            HashSet cnList = new HashSet();
            requestParams.put(Constants.start, "");
            requestParams.put(Constants.limit, "");
            KwlReturnObject result = null;
            String companyid = "";
            for (int cnt = 0; cnt < companyids.length; cnt++) {
                companyid = companyids[cnt];
                request.setAttribute("companyid", companyid);
                request.setAttribute("gcurrencyid", gcurrencyid);
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);
                boolean isNoteForPayment = false;
                boolean isNewUI = true; // this flag is used in accCreditNoteImpl/getCreditNoteMerged for getting credit notes for new design of Payment Module
                boolean isVendor = false;
                if (!StringUtil.isNullOrEmpty(request.getParameter("isNoteForPayment"))) {
                    isNoteForPayment = Boolean.parseBoolean(request.getParameter("isNoteForPayment"));
                    isVendor = Boolean.parseBoolean(request.getParameter("isVendor"));
                }
                requestParams.put("isNoteForPayment", isNoteForPayment);
                requestParams.put("isNewUI", isNewUI);
                result = accCreditNoteDAOobj.getCreditNoteMerged(requestParams);
                if (isEdit && !StringUtil.isNullOrEmpty(request.getParameter("billId").toString())) {
                     KwlReturnObject cndnResult = accCreditNoteDAOobj.getVendorCnPayment(request.getParameter("billId"));
                     Iterator cnItr = cndnResult.getEntityList().iterator();
                     while(cnItr.hasNext()){
                         Object[] objects = (Object[]) cnItr.next();
                         String cnnoteid = objects[0] != null ? (String) objects[1] : "";
                         cnList.add(cnnoteid);
                     }
                }
                DataJArr = getCreditNoteMergedJsonForPayment(request, result.getEntityList(), DataJArr,cnList,isEdit);
                int cntype = StringUtil.isNullOrEmpty(request.getParameter("cntype")) ? 1 : Integer.parseInt(request.getParameter("cntype"));
                if (requestParams.containsKey(Constants.REQ_startdate) && requestParams.containsKey(Constants.REQ_enddate)) {
                    // I am clearing startdate and enddate  from requestParams because there is no need of startdate and enddate for fetching opening Debit Notes.
                    requestParams.put(Constants.REQ_startdate, "");
                    requestParams.put(Constants.REQ_enddate, "");
                }
                 /*
                 removed  isNoteForPayment   flag while fetching opening CN/DN to solve   ERP-14948
                    opening CN/DN does not load in MP/RP when Document currency and Payment method currency is different
                 */
                if (cntype == 10 || ( !isVendor)) {// cntype=10 is just for help. value 10 have no any sense.
                    result = accCreditNoteDAOobj.getOpeningBalanceCNs(requestParams);
                    requestParams.put("cntype", 10);
                    accCreditNoteService.getOpeningCreditNotesJson(requestParams, result.getEntityList(), DataJArr);
                } else if (cntype == 11 || ( isVendor)) {// cntype=11 is just for help. value 11 have no any sense.
                    result = accCreditNoteDAOobj.getOpeningBalanceVendorCNs(requestParams);
                    requestParams.put("cntype", 11);
                    accCreditNoteService.getOpeningCreditNotesJson(requestParams, result.getEntityList(), DataJArr);
                }

            }
            int cnt = DataJArr.length();
            JSONArray pagedJson = DataJArr;

            String start = request.getParameter(Constants.start);
            String limit = request.getParameter(Constants.limit);
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            jobj.put("data", pagedJson);
            jobj.put("count", cnt);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
        public JSONArray getCreditNoteMergedJsonForPayment(HttpServletRequest request, List list, JSONArray jArr, HashSet cnList, boolean isEdit) throws ServiceException {
        try {
            
            HashMap<String, Object> requestParams = getCreditNoteMap(request);
            String currencyid = (String) requestParams.get("gcurrencyid");
            String transactionCurrencyId = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("df");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                if (!isEdit || (isEdit && !cnList.contains((String) row[1]))) {   // here, (String)row[1] refers to credit note id

                    KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), (String) row[3]);
                    JournalEntryDetail details = (JournalEntryDetail) resultObject.getEntityList().get(0);

                    JSONObject obj = new JSONObject();

                    resultObject = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), (String) row[1]);
                    CreditNote creditMemo = (CreditNote) resultObject.getEntityList().get(0);
                    JournalEntry je = creditMemo.getJournalEntry();

                    Date creditNoteDate = null;
                    double externalCurrencyRate = 0d;
                    if (creditMemo.isNormalCN()) {
                        je = creditMemo.getJournalEntry();
                        creditNoteDate = creditMemo.getCreationDate();
                        externalCurrencyRate = je.getExternalCurrencyRate();
                    }

                    transactionCurrencyId = (creditMemo.getCurrency() == null ? currency.getCurrencyID() : creditMemo.getCurrency().getCurrencyID());

                    obj.put("currencyidtransaction", transactionCurrencyId);
                    obj.put("currencysymboltransaction", (creditMemo.getCurrency() == null ? currency.getSymbol() : creditMemo.getCurrency().getSymbol()));

                    String currencyFilterForTrans = "";
                    boolean isNoteForPayment = false;
                    if (!StringUtil.isNullOrEmpty(request.getParameter("isNoteForPayment"))) {
                        isNoteForPayment = Boolean.parseBoolean(request.getParameter("isNoteForPayment"));
                    }
                    if (requestParams.containsKey("currencyfilterfortrans") && isNoteForPayment) {
                        currencyFilterForTrans = requestParams.get("currencyfilterfortrans") != null ? (String) requestParams.get("currencyfilterfortrans") : "";
                        KwlReturnObject curresultpayment = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyFilterForTrans);
                        KWLCurrency currencyPayment = (KWLCurrency) curresultpayment.getEntityList().get(0);
                        obj.put("currencysymbol", (currencyPayment == null ? currency.getSymbol() : currencyPayment.getSymbol()));
                        obj.put("currencyid", (currencyPayment == null ? currency.getCurrencyID() : currencyPayment.getCurrencyID()));
                    } else {
                        obj.put("currencysymbol", (creditMemo.getCurrency() == null ? currency.getSymbol() : creditMemo.getCurrency().getSymbol()));
                        obj.put("currencyid", (creditMemo.getCurrency() == null ? currency.getCurrencyID() : creditMemo.getCurrency().getCurrencyID()));
                    }
                    double amountdue = creditMemo.isOtherwise() || creditMemo.getCntype()==5 ? creditMemo.getCnamountdue() : 0;         //getting amount due for credit note against vendor only for malaysian country ERP-27284 / ERP-28249
                    double amountDueOriginal = creditMemo.isOtherwise() || creditMemo.getCntype()==5 ? creditMemo.getCnamountdue() : 0;
                    if (!StringUtil.isNullOrEmpty(currencyFilterForTrans) && !StringUtil.isNullOrEmpty(transactionCurrencyId) && !currencyFilterForTrans.equals(transactionCurrencyId)) {
                        KwlReturnObject bAmtCurrencyFilter = null;
                        bAmtCurrencyFilter = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, amountdue, transactionCurrencyId, currencyFilterForTrans, creditNoteDate, externalCurrencyRate);
                        amountdue = (Double) bAmtCurrencyFilter.getEntityList().get(0);
                    }
                    amountdue = authHandler.round(amountdue, companyid);

                    obj.put("noteid", creditMemo.getID());
                    obj.put("noteno", creditMemo.getCreditNoteNumber());
                    obj.put("amount", creditMemo.isOtherwise() ? creditMemo.getCnamount() : details.getAmount());
                    obj.put("amountdue", (amountdue <= 0) ? 0 : authHandler.round(amountdue, companyid));
                    obj.put("exchangeratefortransaction", (amountDueOriginal <= 0 && amountdue <= 0) ? 0 : (amountdue / amountDueOriginal));
                    obj.put("amountDueOriginal", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal, companyid));
                    obj.put("amountDueOriginalSaved", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal, companyid));
                    obj.put("date", df.format(creditMemo.getCreationDate()));
                    obj.put("jeDate", je != null ? df.format(je.getEntryDate()) : creditMemo.getCreationDate());
                    obj.put("accountid", creditMemo.getAccount() == null ? "" : creditMemo.getAccount().getID());
                    obj.put("accountnames", creditMemo.getAccount() == null ? "" : creditMemo.getAccount().getName());
                    /*
                     * Get global custom data for payment
                     */
                    accCreditNoteService.getCreditNoteCustomDataForPayment(requestParams, obj, creditMemo, je);
                    if (requestParams.containsKey("isReceipt") && obj.optDouble("amountdue", 0.0) != 0) {
                        jArr.put(obj);
                    } else if (!requestParams.containsKey("isReceipt")) {
                        jArr.put(obj);
                    }
                }
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("getCreditNoteJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
        

      
    /**
     * Description : Method is used to Update Entry in linking information table
     * for Credit Note & Invoice If any Invoice linked with Credit Note
     *
     * @param <request> used to get sub domain for which you want to update
     * entry in linking information table
     * @param <response> used to send response
     * @return :JSONObject(contains success & Message whether script is
     * completed or not)
     */

    public ModelAndView updateSILinkingInformationWithCNScript(HttpServletRequest request, HttpServletResponse response) {
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
            if (!StringUtil.isNullOrEmpty(request.getParameter("subdomain"))) {
                subdomain = request.getParameter("subdomain").toString().trim();
                subdomainArray = subdomain.split(",");
            }
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            KwlReturnObject rCompanyId = null;
            rCompanyId = accInvoiceDAOObj.getAllCompanyFromDb(subdomainArray);
            Iterator itrCompanyId = rCompanyId.getEntityList().iterator();

            while (itrCompanyId.hasNext()) {
                String companyid = (String) itrCompanyId.next();
                requestParams.put(Constants.companyKey, companyid);
                KwlReturnObject result = accInvoiceDAOObj.getCreditNotesLinkedWithInvoice(requestParams);
                Iterator itr2 = result.getEntityList().iterator();
                while (itr2.hasNext()) {
                    String creditnoteid = (String) itr2.next();
                    if (!StringUtil.isNullOrEmpty(creditnoteid)) {
                        /*
                         * Method is used for updating linking information of
                         *
                         * Credit Note in linking table linked with Invoice
                         */

                        accCreditNoteService.updateLinkingInformationOfCreditNote(creditnoteid);
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
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, "Script completed for Updating Linking Information for Invoice linked with Credit Note");

            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView getCreditNoteWithAccountMerged(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        String msg = "";
        boolean issuccess = false;
        try {
            request.setAttribute("isExport",true);
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            HashMap map = accCreditNoteService.getCreditNoteCommonCode(paramJobj);
            DataJArr = (JSONArray) map.get("data");
            int cnt = (Integer) map.get("count");
            issuccess = true;
            JSONObject commData = accCreditNoteService.getColumnsForCreditNoteWithAccounts(request, issuccess);
            commData.put("coldata", DataJArr);
            commData.put("totalCount", cnt);
            jobj.put("valid", true);
            jobj.put("data", commData);

        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }

     
}
