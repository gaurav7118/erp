
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
package com.krawler.spring.accounting.debitnote;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.*;
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
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.bankreconciliation.accBankReconciliationDAO;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesCMN;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignHandler;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import com.krawler.spring.accounting.discount.accDiscountDAO;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.purchaseorder.accPurchaseOrderDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.vendor.accVendorDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.CommonFnController;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.importFunctionality.ImportDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.*;
import java.math.BigInteger;
import java.net.URLDecoder;
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
public class accDebitNoteController extends MultiActionController implements MessageSourceAware {

    private HibernateTransactionManager txnManager;
    private accDebitNoteDAO accDebitNoteobj;
    private accJournalEntryDAO accJournalEntryobj;
    private accProductDAO accProductObj;
    private accDiscountDAO accDiscountobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accTaxDAO accTaxObj;
    private auditTrailDAO auditTrailObj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private fieldDataManager fieldDataManagercntrl;
    private exportMPXDAOImpl exportDaoObj;
    private String successView;
    private accGoodsReceiptDAO accGoodsReceiptobj;
    private MessageSource messageSource;
    public ImportHandler importHandler;
    private ImportDAO importDao;
    public accCustomerDAO accCustomerDAOObj;
    private accVendorDAO accVendorDAOObj;
    private accDebitNoteService accDebitNoteService;
    private accAccountDAO accAccountDAOobj;
    private AccCommonTablesDAO accCommonTablesDAO;
    private AccJournalEntryModuleService journalEntryModuleServiceobj;
    private authHandlerDAO authHandlerDAOObj;
    private accPurchaseOrderDAO accPurchaseOrderobj;
    private accPaymentDAO accPaymentDAOobj;
    private accBankReconciliationDAO accBankReconciliationDAOObj;

    public void setAccBankReconciliationDAOObj(accBankReconciliationDAO accBankReconciliationDAOObj) {
        this.accBankReconciliationDAOObj = accBankReconciliationDAOObj;
    }
    
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
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

    public void setAccVendorDAO(accVendorDAO accVendorDAOObj) {
        this.accVendorDAOObj = accVendorDAOObj;
    }

    public void setaccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setaccDebitNoteDAO(accDebitNoteDAO accDebitNoteobj) {
        this.accDebitNoteobj = accDebitNoteobj;
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

    public void setaccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }

    public void setaccDebitNoteService(accDebitNoteService accDebitNoteService) {
        this.accDebitNoteService = accDebitNoteService;
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

    public void setAuthHandlerDAOObj(authHandlerDAO authHandlerDAOObj) {
        this.authHandlerDAOObj = authHandlerDAOObj;
    }
     public void setaccPurchaseOrderDAO(accPurchaseOrderDAO accPurchaseOrderobj) {
        this.accPurchaseOrderobj = accPurchaseOrderobj;
    }
     public void setaccPaymentDAO(accPaymentDAO accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }

    public ModelAndView updateDebitNote(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        boolean isAccountingExe = false;
        String channelName = "/DebitNoteReport/gridAutoRefresh";
        String debitNoteId = request.getParameter("noteid");
        String msg = "";
        String debitNoteNumBer = "";
        String JENumBer = "";
        String companyid = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        String auditMsg = "", auditID = "";
        TransactionStatus status = null;
        status = txnManager.getTransaction(def);
        try {
            companyid = sessionHandlerImpl.getCompanyid(request);
            List returnList = updateDebitNote(request);

            txnManager.commit(status);
            issuccess = true;
            DebitNote debitnote = (DebitNote) returnList.get(0);
            debitNoteNumBer = debitnote.getDebitNoteNumber();
            JENumBer = debitnote.getJournalEntry().getEntryNumber();
            if (!StringUtil.isNullOrEmpty(debitNoteId)) {
                auditID = AuditAction.DABIT_NOTE_MODIFIED;
                auditMsg = "updated";
            }
            auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + auditMsg + " a Debit Note " + debitNoteNumBer, request, debitnote.getID());
            if (debitnote.getApprovestatuslevel() != 11) {//pending for approval case
                String creditnoteSaved = messageSource.getMessage("acc.debitN.save", null, RequestContextUtils.getLocale(request));
                String butPendingForApproval = " " + messageSource.getMessage("acc.field.butpendingforApproval", null, RequestContextUtils.getLocale(request));
                msg = creditnoteSaved + butPendingForApproval + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + debitNoteNumBer + "</b>.";
            } else {
                msg = messageSource.getMessage("acc.debitN.save", null, RequestContextUtils.getLocale(request)) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + debitNoteNumBer + ",</b>" + messageSource.getMessage("acc.field.JENo", null, RequestContextUtils.getLocale(request)) + ": <b>" + JENumBer + "</b>";   //"Debit Note has been saved successfully";
            }
        } catch (SessionExpiredException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("accException", isAccountingExe);
                if (issuccess && !StringUtil.isNullOrEmpty(channelName)) {
                    jobj.put(Constants.userSessionId, (request.getSession(true)).getAttribute(Constants.userSessionId));// adding user session id to idenntify unique user session
                    ServerEventManager.publish(channelName, jobj.toString(), (ServletContext) this.getServletContext());
                }
            } catch (JSONException ex) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return new ModelAndView("jsonView", "model", jobj.toString());

    }
    public List updateDebitNote(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException, UnsupportedEncodingException {
        String id = null;
        DebitNote debitnote = null;
        List ll = new ArrayList();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String createdby = sessionHandlerImpl.getUserid(request);
            String debitNoteId = request.getParameter("noteid");
            int approvalStatusLevel = 11;
            String modifiedby = sessionHandlerImpl.getUserid(request);
            int moduleid = Constants.Acc_Debit_Note_ModuleId;
            /*
             * To update the following items which is not affecting the amount
             * and linking of the Invoice.
             */
            HashMap<String, Object> debithm = new HashMap<String, Object>();
            if (!StringUtil.isNullOrEmpty(debitNoteId)) {
                debithm.put("dnid", debitNoteId);
            }
            String costCenterId = !StringUtil.isNullOrEmpty(request.getParameter("costCenterId")) ? request.getParameter("costCenterId") : "";
            String salesPersonID = !StringUtil.isNullOrEmpty(request.getParameter("salesPersonID")) ? request.getParameter("salesPersonID") : "";
            debithm.put("memo", (!StringUtil.isNullOrEmpty(request.getParameter("memo"))) ? request.getParameter("memo") : "");
            debithm.put("companyid", companyid);
            debithm.put("currencyid", currencyid);
            debithm.put("modifiedby", modifiedby);
            debithm.put("updatedon", System.currentTimeMillis());
            debithm.put("costcenter", costCenterId);
            debithm.put("salesPersonID", salesPersonID);

            KwlReturnObject cnObj = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), debitNoteId);
            debitnote = (DebitNote) cnObj.getEntityList().get(0);
            /*
             * Updating line item information.
             */
            String Details = request.getParameter("details").toString();
            HashSet<InvoiceDetail> cnDetails = updateDebitNoteRows(debitnote, Details, debitnote.getJournalEntry(), moduleid, companyid);
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
                customrequestParams.put("modulerecid", debitnote.getJournalEntry().getID());
                customrequestParams.put("moduleid", moduleid);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    jeDataMap.put("accjecustomdataref", debitnote.getJournalEntry().getID());
                    jeDataMap.put("jeid", debitnote.getJournalEntry().getID());
                    jeDataMap.put("entrydate", debitnote.getJournalEntry().getEntryDate());
                    KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);
                }
            }
            HashMap<String, Object> DNApproveMap = new HashMap<String, Object>();
            List approvedlevel = null;
            DNApproveMap.put("companyid", sessionHandlerImpl.getCompanyid(request));
            DNApproveMap.put("level", 0);//Initialy it will be 0
            DNApproveMap.put("totalAmount", String.valueOf(authHandler.round(debitnote.getDnamountinbase(), companyid)));
            DNApproveMap.put("currentUser", createdby);
            DNApproveMap.put("fromCreate", true);
            DNApproveMap.put("moduleid", Constants.Acc_Debit_Note_ModuleId);
            DNApproveMap.put(Constants.PAGE_URL, URLUtil.getPageURL(request, loginpageFull));
            boolean isMailApplicable = false;
            approvedlevel = accDebitNoteService.approveDebitNote(debitnote, DNApproveMap, isMailApplicable); 
            approvalStatusLevel = (Integer) approvedlevel.get(0);
            debithm.put("approvestatuslevel", approvalStatusLevel);
            KwlReturnObject result = accDebitNoteobj.updateDebitNote(debithm);
            debitnote = (DebitNote) result.getEntityList().get(0);//Create Invoice without invoice-details.
            id = debitnote.getID();
            ll.add(debitnote);
            ll.add(id);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (Exception e) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, e);
        }
        return ll;
    }
    private HashSet<InvoiceDetail> updateDebitNoteRows(DebitNote debitNotenote, String debitnotedetails, JournalEntry je, int moduleid, String companyid) throws ServiceException, JSONException {
        HashSet<InvoiceDetail> rows = new HashSet<InvoiceDetail>();
        try {
            JSONArray jArr = new JSONArray(debitnotedetails);
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                DebitNoteTaxEntry row = null;
                if (jobj.has("rowid")) {
                    KwlReturnObject cnDetail = accountingHandlerDAOobj.getObject(DebitNoteTaxEntry.class.getName(), jobj.getString("rowid"));
                    row = (DebitNoteTaxEntry) cnDetail.getEntityList().get(0);
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
                        KwlReturnObject reasonresult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), jobj.optString("reason", ""));
                        MasterItem reason = (MasterItem) reasonresult.getEntityList().get(0);
                        row.setReason(reason);
                    } catch (Exception ex) {
                        KwlReturnObject reasonresult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), jobj.optString("reason", ""));
                        MasterItem reason = (MasterItem) reasonresult.getEntityList().get(0);
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
                    customrequestParams.put("moduleid", Constants.Acc_Debit_Note_ModuleId);
                    customrequestParams.put("companyid", companyid);
                    customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                    KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        JSONObject tempjedjson = new JSONObject();
                        tempjedjson.put("accjedetailcustomdata", row.getTotalJED().getID());
                        tempjedjson.put("jedid", row.getTotalJED().getID());
                        KwlReturnObject jedresult = accJournalEntryobj.updateJournalEntryDetails(tempjedjson);
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
                            accJournalEntryobj.updateJournalEntryDetails(tempjedjson);
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
                         * If dimension value is not tagged while creating CN
                         * then separated jed doesn't get post. Post that
                         * additional jedetail and also tag dimension value.
                         */
                        
                        jedjson = new JSONObject();
                        jedjson.put("srno", 1);
                        jedjson.put("companyid", row.getTotalJED().getCompany().getCompanyID());
                        jedjson.put("amount", row.getTotalJED().getAmount());
                        jedjson.put("accountid", debitNotenote.getAccount().getID());
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
                            jedjson = new JSONObject();
                            jedjson.put("srno", 1);
                            jedjson.put("companyid", row.getGstJED().getCompany().getCompanyID());
                            jedjson.put("amount", row.getGstJED().getAmount());
                            jedjson.put("accountid", debitNotenote.getAccount().getID());
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
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, e);
        }
        return rows;
    }
    public ModelAndView saveDebitNote(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        String debitNoteNumBer = "";
        String JENumBer = "";
        String sequenceformat = request.getParameter("sequenceformat");
        String debitNoteId = "";
        String nextDNAutoNo = "";
        String jeentryNumber = "";
        KwlReturnObject result;
        String jeSeqFormatId = "";
        String companyid="";
        int approvalStatusLevel = 11;
        boolean isAccountingExe=false; 
        boolean isTaxDeactivated = false;
        Company company=null;
        Map<String, Object> seqNumMap = new HashMap<String, Object>();
       
        String channelName = "/DebitNoteReport/gridAutoRefresh";
        //String nextAutoNoInt = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("PaymentMethods_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        int flag=0;
        String auditMsg = "", auditID = "";
        String entryNumber = request.getParameter("number");
        TransactionStatus status = null;
        String invoiceids = "";
        String goodReceiptNumbers = "";
        if (!StringUtil.isNullOrEmpty(request.getParameter("invoiceids"))) {
            invoiceids = request.getParameter("invoiceids");
        }
         String invoiceIdsArrary[] = invoiceids.split(",");
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
           
            JSONArray invoicedetails=null;
            int cntype = StringUtil.isNullOrEmpty(request.getParameter("cntype")) ? 1 : Integer.parseInt(request.getParameter("cntype"));
             /*
             * For Malaysian Company while save debit note against customer noteis as srid
             */
            if (cntype == 5||cntype == Constants.DebitNoteForOvercharge) {
                debitNoteId =request.getParameter("srid");
            } else {
                debitNoteId = request.getParameter("noteid");
            }
            companyid = sessionHandlerImpl.getCompanyid(request); 
            if(!StringUtil.isNullOrEmpty(request.getParameter("invoicedetails"))) {
                invoicedetails = new JSONArray(request.getParameter("invoicedetails"));
            }
            
             /*
             * check duplicate record while editing record 
             */
            KwlReturnObject cncount = null;
            if (!StringUtil.isNullOrEmpty(debitNoteId)) {//Edit case checks duplicate
                cncount = accDebitNoteobj.getDNFromNoteNoAndId(entryNumber, companyid, debitNoteId);
                if (cncount.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
                    isAccountingExe = true;
                    throw new AccountingException(messageSource.getMessage("acc.DN.debitnoteno", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                }
            } else {
                /*
                 * check duplicate record while creating new record
                 */
                cncount = accDebitNoteobj.getDNFromNoteNo(entryNumber, companyid);
                if (cncount.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
                    isAccountingExe = true;
                    throw new AccountingException(messageSource.getMessage("acc.DN.debitnoteno", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                }
                //Check Deactivate Tax in New Transaction.
                if ((cntype != 5 || cntype != Constants.DebitNoteForOvercharge) && !fieldDataManagercntrl.isTaxActivated(paramJobj)) {
                    isTaxDeactivated = true;
                    throw ServiceException.FAILURE(messageSource.getMessage("acc.tax.deactivated.tax.saveAlert", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                }
            }
            /*
             * Check squence format length for INDIA country
             */
            KwlReturnObject cmpny = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            company = (Company) cmpny.getEntityList().get(0);
            if (company.getCountry().getID().equals(String.valueOf(Constants.indian_country_id))) {
                if ((!StringUtil.isNullOrEmpty(sequenceformat) && !sequenceformat.equals("NA")) && !StringUtil.isNullOrEmpty(debitNoteId)) {
                    boolean seqformat_oldflg = StringUtil.getBoolean(paramJobj.optString("seqformat_oldflag"));
                    Date billDate = authHandler.getDateOnlyFormat().parse(paramJobj.optString("creationdate"));
                    seqNumMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_DEBITNOTE, sequenceformat, seqformat_oldflg, billDate);
                    String autoNum = (String) seqNumMap.get(Constants.AUTO_ENTRYNUMBER);
                    if (autoNum.length() > Constants.SequenceFormatMaxLength || !StringUtil.isSequenceFormatValid(autoNum)) {
                        throw new AccountingException(Constants.SequenceformatErrorMsg1 + autoNum + Constants.SequenceformatErrorMsg2 + Constants.SequenceFormatMaxLength + Constants.SequenceformatErrorMsg3);
                    }
                } else if (!StringUtil.isNullOrEmpty(sequenceformat) && sequenceformat.equals("NA") || !StringUtil.isSequenceFormatValid(entryNumber)) {
                    if ((!StringUtil.isNullOrEmpty(sequenceformat) && entryNumber.length() > Constants.SequenceFormatMaxLength) || !StringUtil.isSequenceFormatValid(entryNumber)) {
                        throw new AccountingException(Constants.SequenceformatErrorMsg1 + entryNumber + Constants.SequenceformatErrorMsg2 + Constants.SequenceFormatMaxLength + Constants.SequenceformatErrorMsg3);
                    }
                }
            }
            synchronized (this) {
                /*
                 * Checks duplicate number for simultaneous transactions
                 */
                status = txnManager.getTransaction(def);
                if (sequenceformat.equals("NA")) {
                    KwlReturnObject resultInv = accCommonTablesDAO.getTransactionInTemp(entryNumber, companyid, Constants.Acc_Debit_Note_ModuleId);
                    if (resultInv.getRecordTotalCount() > 0) {
                        isAccountingExe = true;
                        throw new AccountingException(messageSource.getMessage("acc.DN.selecteddebitnoteno", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyinprocess.", null, RequestContextUtils.getLocale(request)));
                    } else {
                        accCommonTablesDAO.insertTransactionInTemp(entryNumber, companyid, Constants.Acc_Debit_Note_ModuleId);
                    }
                }
//                for (String invoiceId : invoiceIdsArrary) {
//                    KwlReturnObject resultInv1 = accPaymentDAOobj.getInvoiceInTemp(invoiceId, companyid, Constants.Acc_Vendor_Invoice_ModuleId);
//                    if (resultInv1.getRecordTotalCount() > 0) {
//                        throw new AccountingException("Selected invoice is already in process, please try after sometime.");
//                    } else {
//                        accPaymentDAOobj.insertInvoiceOrCheque(invoiceId, companyid, Constants.Acc_Vendor_Invoice_ModuleId, "");
//                    }
//                }
                txnManager.commit(status);
            }
            status = txnManager.getTransaction(def);
            List returnList=null;
            /*
             * For Malaysian company call SaveDebitGst
             */
            if (cntype == 5 || cntype == Constants.DebitNoteForOvercharge) {
                returnList = saveDebitNoteGst(request);
            } else {
                returnList = saveDebitNote(request);
            }
           
            
            String oldJeId = (String) returnList.get(0);
            DebitNote debitnote = (DebitNote) returnList.get(1);
            
            List mailParams = Collections.EMPTY_LIST;
            if (returnList.get(2) != null) {//Approval Status Level
                approvalStatusLevel = Integer.parseInt(returnList.get(2).toString());
            }
            if (cntype != 5 && cntype != Constants.DebitNoteForOvercharge) {
                mailParams = (List) returnList.get(3);
            }
            
            debitNoteNumBer = debitnote.getDebitNoteNumber();
            JENumBer = debitnote.getJournalEntry().getEntryNumber();
            issuccess = true;
            txnManager.commit(status);
            TransactionStatus AutoNoStatus = null;
            boolean autogenerated=false;
            int nextAutoNoInt=0;
            String datePrefix = "";
            String dateafterPrefix = "";
            String dateSuffix = "";
            try {
                synchronized (this) {
                    
                    /*
                        auto number save functionality
                    */
                    DefaultTransactionDefinition def1 = new DefaultTransactionDefinition();
                    def1.setName("AutoNum_Tx");
                    def1.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                    AutoNoStatus = txnManager.getTransaction(def1);
                    if (!sequenceformat.equals("NA") && StringUtil.isNullOrEmpty(debitNoteId)) {
                        boolean seqformat_oldflag = StringUtil.getBoolean(request.getParameter("seqformat_oldflag"));
                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                        if (seqformat_oldflag) {
                            nextDNAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_DEBITNOTE, sequenceformat);
                        } else {
//                            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_DEBITNOTE, sequenceformat, seqformat_oldflag, debitnote.getJournalEntry().getEntryDate());
                              seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_DEBITNOTE, sequenceformat, seqformat_oldflag, debitnote.getCreationDate());
                            nextAutoNoInt = Integer.parseInt((String)seqNumberMap.get(Constants.SEQNUMBER));
                            datePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                            dateafterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                            dateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                        }
                        seqNumberMap.put(Constants.DOCUMENTID, debitnote.getID());
                        seqNumberMap.put(Constants.companyKey, companyid);
                        seqNumberMap.put(Constants.SEQUENCEFORMATID, sequenceformat);
                        debitNoteNumBer = accDebitNoteobj.updateDeditEntryNumber(seqNumberMap);
                        autogenerated = true;
                    }
                    /*
                     * new and copy case when document going for pending
                     * approval. In this case does not need to give number to
                     * Journal entry it get assigned when document if finally approved.
                     */
                    if (StringUtil.isNullOrEmpty(oldJeId) && approvalStatusLevel==11) {
                        HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                        JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                        JEFormatParams.put("modulename", "autojournalentry");
                        JEFormatParams.put("companyid", companyid);
                        JEFormatParams.put("isdefaultFormat", true);

                        KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                        SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
//                        String[] nextAutoNoTemp = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false);
//                        jeentryNumber = nextAutoNoTemp[0];  //next auto generated number
//                        jeIntegerPart = nextAutoNoTemp[1];
                        jeSeqFormatId = format.getID();
                        Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
//                        JENumBer = journalEntryModuleServiceobj.updateJEEntryNumberForNewJE(jeDataMap, debitnote.getJournalEntry(), companyid, jeSeqFormatId,debitnote.getJournalEntry().getPendingapproval());
                        KwlReturnObject returnObj = journalEntryModuleServiceobj.updateJEEntryNumberForNewJE(jeDataMap, debitnote.getJournalEntry(), companyid, jeSeqFormatId,debitnote.getJournalEntry().getPendingapproval());
                        if (returnObj.isSuccessFlag() && returnObj.getRecordTotalCount() > 0) {
                            JENumBer = (String) returnObj.getEntityList().get(0);
                        }
                    }
                    txnManager.commit(AutoNoStatus);
                    
                    /*
                        Check if selected invoice already knock-off from another transaction,
                        If yes then remove already saved debit note in above block.
                    */
                    if (cntype != Constants.DebitNoteForOvercharge) {//No need to check knockoff case for DN for Overcharge.
                        for (int i = 0; i < invoiceIdsArrary.length; i++) {
                            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invoiceIdsArrary[i]);
                            GoodsReceipt gReceipt = (GoodsReceipt) objItr.getEntityList().get(0);

                            if (gReceipt != null && invoicedetails != null) {
                                double invoiceAmoutDue = 0; // Opening transaction record
                                double invoiceAmoutDueBase = 0;
                                double amountReceivedBase = 0;
                                if (gReceipt.isIsOpeningBalenceInvoice() && !gReceipt.isNormalInvoice()) {
                                    invoiceAmoutDue = gReceipt.getOpeningBalanceAmountDue();
                                } else {
                                    invoiceAmoutDue = gReceipt.getInvoiceamountdue();
                                }
                                JSONObject invoiceJobj = invoicedetails.getJSONObject(i);
                                double amountReceived = invoiceJobj.getDouble("invamount");           //amount of DN 
                                double amountDue = invoiceJobj.getDouble("amountdue");
                                HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                                String currencyid = sessionHandlerImpl.getCurrencyID(request);
                                filterRequestParams.put(Constants.companyKey, companyid);
                                filterRequestParams.put(Constants.globalCurrencyKey, currencyid);
                                KwlReturnObject bAmt = null;
                                if (!gReceipt.isIsOpeningBalenceInvoice()) {
                                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, invoiceAmoutDue, currencyid, gReceipt.getCreationDate(), gReceipt.getJournalEntry().getExternalCurrencyRate());
                                } else {
                                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, invoiceAmoutDue, currencyid, gReceipt.getCreationDate(), gReceipt.getExchangeRateForOpeningTransaction());
                                }
                                KwlReturnObject bAmtRec = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, amountReceived, currencyid, debitnote.getCreationDate(), debitnote.getExternalCurrencyRate());
                                invoiceAmoutDueBase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                                amountReceivedBase = authHandler.round((Double) bAmtRec.getEntityList().get(0), companyid);
                                /**
                                 * I have comment this code due to some scenario
                                 * are interrupt. below condition for checks
                                 * simultaneous transaction,
                                 */
//                                if (invoiceAmoutDueBase < amountReceivedBase) {  // Last Amount column should be compared with Amount Due column (SDP-9124)
//                                    flag = 1;
//                                    issuccess = false;
//                                    status = txnManager.getTransaction(def);
//                                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
//                                    requestParams.put("dnid", debitnote.getID());
//                                    requestParams.put("companyid", companyid);
//                                    requestParams.put("dnno", debitNoteNumBer);
//                                    accDebitNoteobj.deleteDebitNotesPermanent(requestParams);
//                                    txnManager.commit(status);
//                                    status = null;
//                                    throw new AccountingException(messageSource.getMessage("acc.field.alreadyknock_off", null, RequestContextUtils.getLocale(request)));
//                                }
                                goodReceiptNumbers =  goodReceiptNumbers + gReceipt.getGoodsReceiptNumber() + ","; //fetching goods receipt Number for audit trail entry
                            }
                        }
                    }
                    /*
                        If valid invoice details then update invoice amount due accordingly
                    */
                    status = txnManager.getTransaction(def);
                    request.setAttribute("entrynumber", debitNoteNumBer);
                    if (!sequenceformat.equals("NA") && StringUtil.isNullOrEmpty(debitNoteId)) {

                        request.setAttribute("autogenerated", autogenerated);
                        request.setAttribute("seqformat", sequenceformat);
                        request.setAttribute("seqnumber", nextAutoNoInt);
                        request.setAttribute(Constants.DATEPREFIX, datePrefix);
                        request.setAttribute(Constants.DATEAFTERPREFIX, dateafterPrefix);
                        request.setAttribute(Constants.DATESUFFIX, dateSuffix);
                    }
                    if (cntype == 1 || cntype == 3) {
                         HashMap<String,Object> requestParams = new HashMap<>();
                        accDebitNoteService.linkDebitNote(request, debitnote.getID(),false,requestParams);    // "flase" flag is passed for inserting Audit Trial entry ( ERP-18558 )
                    }
                    if (!StringUtil.isNullOrEmpty(debitNoteId)) {
                        auditID = AuditAction.DABIT_NOTE_MODIFIED;
                        auditMsg = "updated";
                    } else {
                        auditID = AuditAction.DABIT_NOTE_CREATED;
                        auditMsg = "added";
                    }
                    goodReceiptNumbers = (!StringUtil.isNullOrEmpty(goodReceiptNumbers)) ? goodReceiptNumbers.substring(0, goodReceiptNumbers.length() - 1) : "";
                        if (approvalStatusLevel != 11) {//pending for approval case
                            String pendingforApproval = " " + messageSource.getMessage("acc.field.whichispendingforApproval", null, RequestContextUtils.getLocale(request));
                            auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + auditMsg + " a Debit Note " + debitNoteNumBer + pendingforApproval, request, debitnote.getID());
                        } else if (cntype == 1 || cntype == 3) { //check for DN which are linked with Purchase invoices
                            auditTrailObj.insertAuditLog(auditID, messageSource.getMessage("acc.field.User", null, RequestContextUtils.getLocale(request)) + sessionHandlerImpl.getUserFullName(request) + " " + messageSource.getMessage("acc.common.has", null, RequestContextUtils.getLocale(request)) + " " + auditMsg + " " + messageSource.getMessage("acc.module.name.10", null, RequestContextUtils.getLocale(request)) + " " + debitNoteNumBer + " " + messageSource.getMessage("acc.linkedVendorInvoice.auditTrail", null, RequestContextUtils.getLocale(request)) + " " + "[ " + goodReceiptNumbers + " ]", request, debitnote.getID());
                        } else {
                            auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + auditMsg + " a Debit Note " + debitNoteNumBer, request, debitnote.getID());
                        }
                    txnManager.commit(status);
                }
            }catch (ServiceException ex) {
                issuccess=false;
                throw ServiceException.FAILURE(ex.getMessage(), "", false);
            } catch (Exception ex) {
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid,Constants.Acc_Debit_Note_ModuleId);
                deleteTemporaryInvoicesEntries(invoiceIdsArrary, companyid);
                /*
                    If valid invoice details then flag = 0 otherwise set as 1;
                */
                if (AutoNoStatus != null && flag != 1) {
                    txnManager.rollback(AutoNoStatus);
                } else {
                    throw new AccountingException(ex.getMessage(), ex);
                    // }
                }
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            } 
            if (approvalStatusLevel != 11) {//pending for approval case
                String creditnoteSaved = messageSource.getMessage("acc.debitN.save", null, RequestContextUtils.getLocale(request));
                String butPendingForApproval = " " + messageSource.getMessage("acc.field.butpendingforApproval", null, RequestContextUtils.getLocale(request));
                msg = creditnoteSaved + butPendingForApproval + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + debitNoteNumBer + "</b>.";
            } else {
                    msg = messageSource.getMessage("acc.debitN.save", null, RequestContextUtils.getLocale(request)) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + debitNoteNumBer + ",</b>" + messageSource.getMessage("acc.field.JENo", null, RequestContextUtils.getLocale(request)) + ": <b>" + JENumBer + "</b>";   //"Debit Note has been saved successfully";
                }
            
           // txnManager.commit(status);
            
            status = txnManager.getTransaction(def);
            if (mailParams != null && !mailParams.isEmpty()) {
                Iterator itr = mailParams.iterator();
                /**
                 * parameters required for sending mail
                 */
                Map<String, Object> mailParameters = new HashMap();
                mailParameters.put(Constants.companyid, companyid);
                mailParameters.put(Constants.prNumber, debitNoteNumBer);
                mailParameters.put(Constants.moduleid, Constants.Acc_Debit_Note_ModuleId);
                mailParameters.put(Constants.createdBy, debitnote.getCreatedby().getUserID());
                mailParameters.put(Constants.PAGE_URL, paramJobj.optString(Constants.PAGE_URL));
                mailParameters.put("level", debitnote.getApprovestatuslevel());
                while (itr.hasNext()) {
                    HashMap<String, Object> paramsMap = (HashMap<String, Object>) itr.next();
                    mailParameters.put(Constants.ruleid, (String) paramsMap.get("ruleid"));
                    mailParameters.put(Constants.fromName, (String) paramsMap.get("fromName"));
                    mailParameters.put(Constants.hasApprover, (Boolean) paramsMap.get("hasApprover"));
                    accDebitNoteService.sendMailToApprover(mailParameters);
                }
            }
            accDebitNoteService.deleteJEArray(oldJeId, companyid);
            txnManager.commit(status);
            status = txnManager.getTransaction(def);
            accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Debit_Note_ModuleId);//Delete entry in temporary table
            deleteTemporaryInvoicesEntries(invoiceIdsArrary, companyid);
            txnManager.commit(status);
            
            //==============Create Rounding JE Start=====================
            if (invoiceIdsArrary.length > 0) {//DN Against Purchase Invoice
                //After used of invoice in DN it is needed to check for Rounding JE
                status = txnManager.getTransaction(def);
                try {
                    if (!StringUtil.isNullOrEmpty(debitNoteId)) {
                        paramJobj.put("isEdit", true);
                    }
                    paramJobj.put("debitnotenumber", debitNoteNumBer);
                    accDebitNoteService.postRoundingJEOnDebitNoteSave(paramJobj);
                    txnManager.commit(status);
                } catch (Exception ex) {
                    if (status != null) {
                        txnManager.rollback(status);
                    }
                    Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
            //==============Create Rounding JE End=====================

        } catch (ServiceException ex) {
            try {
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Debit_Note_ModuleId);//Delete entry in temporary table
                deleteTemporaryInvoicesEntries(invoiceIdsArrary, companyid);
            } catch (ServiceException ex1) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = "" + ex.getCause().getMessage();
            }
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AccountingException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            try {
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Debit_Note_ModuleId);//Delete entry in temporary table
                deleteTemporaryInvoicesEntries(invoiceIdsArrary, companyid);
            } catch (ServiceException ex1) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
             try {
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Debit_Note_ModuleId);//Delete entry in temporary table
                deleteTemporaryInvoicesEntries(invoiceIdsArrary, companyid);
            } catch (ServiceException ex1) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            msg = ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            if (status != null && flag!=1) {
                txnManager.rollback(status);
            }
            try {
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Debit_Note_ModuleId);//Delete entry in temporary table
                deleteTemporaryInvoicesEntries(invoiceIdsArrary, companyid);
            } catch (ServiceException ex1) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            msg = "" + ex.getMessage();
            if(ex.getMessage()==null){
            msg = "" + ex.getCause().getMessage();
            }
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("accException", isAccountingExe);
                jobj.put(Constants.isTaxDeactivated, isTaxDeactivated);
                if (issuccess && !StringUtil.isNullOrEmpty(channelName)) {
                    jobj.put(Constants.userSessionId, (request.getSession(true)).getAttribute(Constants.userSessionId));// adding user session id to idenntify unique user session
                    ServerEventManager.publish(channelName, jobj.toString(), (ServletContext) this.getServletContext());
                }
            } catch (JSONException ex) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /*
     * Method to save Opening Balance DN For Vendor.
     */
    public ModelAndView saveOpeningBalanceDN(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject jobjDetails = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("BR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
         boolean isAccountingExe=false;
        String dnNumber = request.getParameter("number");
        String companyid ="";
        try {
            companyid = sessionHandlerImpl.getCompanyid(request);
            String cnId = request.getParameter("transactionId");
            KwlReturnObject cncount=null;
            if (StringUtil.isNullOrEmpty(cnId)) {
                /*
                 * Checks duplicate number while creating new record
                 */
                cncount = accDebitNoteobj.getDNFromNoteNo(dnNumber, companyid);
                if (cncount.getRecordTotalCount() > 0) {
                    isAccountingExe=true;
                    throw new AccountingException(messageSource.getMessage("acc.DN.debitnoteno", null, RequestContextUtils.getLocale(request)) + dnNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                }
                
                /*
                 * code for checking wheather entered number can be generated by sequence format or not
                 */
                List resultList = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Debit_Note_ModuleId, dnNumber, companyid);
                if (!resultList.isEmpty()) {
                    boolean isvalidEntryNumber = (Boolean) resultList.get(0);
                    String formatName = (String) resultList.get(1);
                    if (!isvalidEntryNumber) {
                        isAccountingExe=true;
                        throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, RequestContextUtils.getLocale(request)) + " <b>" + dnNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, RequestContextUtils.getLocale(request)) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.pleaseentersomeothernumber", null, RequestContextUtils.getLocale(request)));
                    }
                }
            }
            
            synchronized (this) {
                /*
                 * Checks duplicate number for simultaneous transactions
                 */
                status = txnManager.getTransaction(def);
                    KwlReturnObject resultInv = accCommonTablesDAO.getTransactionInTemp(dnNumber, companyid, Constants.Acc_Debit_Note_ModuleId);//Get entry from temporary table
                    if (resultInv.getRecordTotalCount() > 0) {
                        isAccountingExe=true;
                        throw new AccountingException(messageSource.getMessage("acc.DN.selecteddebitnoteno", null, RequestContextUtils.getLocale(request)) + dnNumber + messageSource.getMessage("acc.field.alreadyinprocess.", null, RequestContextUtils.getLocale(request)));
                    } else {
                        accCommonTablesDAO.insertTransactionInTemp(dnNumber, companyid, Constants.Acc_Debit_Note_ModuleId);//Insert entry in temporary table
                    }
                txnManager.commit(status);
            }
            status = txnManager.getTransaction(def);
            List li = saveOpeningBalanceDN(request);
            String cnNumber = null;
            if (!li.isEmpty()) {
                cnNumber = li.get(0).toString();
            }

            issuccess = true;
            msg = messageSource.getMessage("acc.accPref.autoDN", null, RequestContextUtils.getLocale(request)) + " " + cnNumber + " " + messageSource.getMessage("acc.field.hasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
            status = txnManager.getTransaction(def);
            accCommonTablesDAO.deleteTransactionInTemp(cnNumber, companyid,Constants.Acc_Debit_Note_ModuleId);//Delete entry in temporary table
            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
           if(status!=null){
                txnManager.rollback(status);
            }
            try {
                accCommonTablesDAO.deleteTransactionInTemp(dnNumber, companyid,Constants.Acc_Debit_Note_ModuleId);//Delete entry in temporary table
            } catch (ServiceException ex1) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            if(status!=null){
                txnManager.rollback(status);
            }
            try {
                accCommonTablesDAO.deleteTransactionInTemp(dnNumber, companyid,Constants.Acc_Debit_Note_ModuleId);//Delete entry in temporary table
            } catch (ServiceException ex1) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            if(status!=null){
                txnManager.rollback(status);
            }
            try {
                accCommonTablesDAO.deleteTransactionInTemp(dnNumber, companyid,Constants.Acc_Debit_Note_ModuleId);//Delete entry in temporary table
            } catch (ServiceException ex1) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArr);
                 jobj.put("accException", isAccountingExe);
            } catch (JSONException ex) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public List saveOpeningBalanceDN(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
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

            String dnNumber = request.getParameter("number");
            String transactionDateStr = request.getParameter("billdate");
            String currencyid = (request.getParameter("currencyid") == null ? sessionHandlerImpl.getCurrencyID(request) : request.getParameter("currencyid"));
            String transactionAmountStr = request.getParameter("transactionAmount");
            String dnId = request.getParameter("transactionId");
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

            // creating dn data

            HashMap<String, Object> dnhm = new HashMap<String, Object>();

            if (StringUtil.isNullOrEmpty(dnId)) {
                result = accDebitNoteobj.getDNFromNoteNo(dnNumber, companyid);
                int count = result.getRecordTotalCount();
                if (count > 0) {
                    throw new AccountingException(messageSource.getMessage("acc.field.Debitnotenumber", null, RequestContextUtils.getLocale(request)) + dnNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                }
                dnhm.put("entrynumber", dnNumber);
                dnhm.put("autogenerated", false);
            }


            if (!StringUtil.isNullOrEmpty(dnId)) {

                // check for is DN Linked with Payment or Vendor Invoice.
                boolean isNoteLinkedWithPayment = accDebitNoteService.isNoteLinkedWithPayment(dnId);
                if (isNoteLinkedWithPayment) {
                    throw new AccountingException(messageSource.getMessage("acc.field.SelectedDebitNoteisLinkedWithPaymentsoitcannotbeedit", null, RequestContextUtils.getLocale(request)));
                }

                boolean isNoteLinkedWithInvoice = accDebitNoteService.isNoteLinkedWithInvoice(dnId, companyid);
                if (isNoteLinkedWithInvoice) {
                    throw new AccountingException(messageSource.getMessage("acc.field.SelectedDebitNoteisLinkedWithvendorinvoicesoitcannotbeedit", null, RequestContextUtils.getLocale(request)));
                }

                if(accDebitNoteService.isDebitNoteLinkedWithCreditNote(dnId, companyid)==true){
                    throw new AccountingException(messageSource.getMessage("acc.field.SelectedDebitNoteisLinkedWithPaymentsoitcannotbedelete", null, RequestContextUtils.getLocale(request)));
                }
                KwlReturnObject deleteResult = accDebitNoteobj.deleteDebitNoteDetails(dnId, companyid);

                isEditInvoice = true;
                auditMsg = "updated";
                auditID = AuditAction.OPENING_BALANCE_UPDATED;
                dnhm.put("dnid", dnId);
            }

            dnhm.put("dnamount", transactionAmount);//
            dnhm.put("currencyid", currencyid);//
            dnhm.put("externalCurrencyRate", externalCurrencyRate);//
            dnhm.put("memo", memo);//
            dnhm.put("companyid", companyid);//
            dnhm.put("narrationValue", narrationValue);//
            dnhm.put("creationDate", transactionDate);//
            dnhm.put("vendorid", vendorId);//
            dnhm.put("accountId", accountId);//
            dnhm.put("isOpeningBalenceDN", true);
            dnhm.put("normalDN", false);//
            dnhm.put("openingBalanceAmountDue", transactionAmount);//
            dnhm.put("exchangeRateForOpeningTransaction", exchangeRateForOpeningTransaction);//
            dnhm.put("isDNForVendor", true);//
            dnhm.put("openflag", true);
            dnhm.put("otherwise", true);
            dnhm.put("dnamountdue", transactionAmount);
            dnhm.put("conversionRateFromCurrencyToBase", conversionRateFromCurrencyToBase);
            
            // Store CN amount in base currency
            if (conversionRateFromCurrencyToBase) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                dnhm.put(Constants.originalOpeningBalanceBaseAmount, authHandler.round(transactionAmount*exchangeRateForOpeningTransaction, companyid));
                dnhm.put(Constants.openingBalanceBaseAmountDue, authHandler.round(transactionAmount*exchangeRateForOpeningTransaction, companyid));
            } else {
                dnhm.put(Constants.originalOpeningBalanceBaseAmount, authHandler.round(transactionAmount/exchangeRateForOpeningTransaction, companyid));
                dnhm.put(Constants.openingBalanceBaseAmountDue, authHandler.round(transactionAmount/exchangeRateForOpeningTransaction, companyid));//
            }
            
            if (isEditInvoice) {
                String modifiedby = sessionHandlerImpl.getUserid(request);
                long updatedon = System.currentTimeMillis();
                dnhm.put("modifiedby", modifiedby);
                dnhm.put("updatedon", updatedon);

                result = accDebitNoteobj.updateDebitNote(dnhm);
            } else {
                String createdby = sessionHandlerImpl.getUserid(request);
                String modifiedby = sessionHandlerImpl.getUserid(request);
                long createdon = System.currentTimeMillis();
                long updatedon = System.currentTimeMillis();

                dnhm.put("createdby", createdby);
                dnhm.put("modifiedby", modifiedby);
                dnhm.put("createdon", createdon);
                dnhm.put("updatedon", updatedon);
                dnhm.put("approvestatuslevel", 11);

                result = accDebitNoteobj.addDebitNote(dnhm);
            }


            HashSet<DebitNoteDetail> dndetails = new HashSet<DebitNoteDetail>();
            getDNDetails(dndetails, companyid);

            DebitNote dn = (DebitNote) result.getEntityList().get(0);

            Iterator itr = dndetails.iterator();
            while (itr.hasNext()) {
                DebitNoteDetail dnd = (DebitNoteDetail) itr.next();
                dnd.setDebitNote(dn);
            }
            dnhm.put("dnid", dn.getID());
            dnhm.put("dndetails", dndetails);

            result = accDebitNoteobj.updateDebitNote(dnhm);

            returnList.add(dn.getDebitNoteNumber());
            
            String customfield = request.getParameter("customfield");
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_OpeningBalanceDebitNote_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_OpeningBalanceDebitNoteid);
                customrequestParams.put("modulerecid", dn.getID());
                customrequestParams.put("moduleid", Constants.Acc_Debit_Note_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_OpeningBalanceDebitNote_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    dnhm.put("dnid", dn.getID());
                    dnhm.put("openingBalanceDebitNoteCustomData", dn.getID());
                    result = accDebitNoteobj.updateDebitNote(dnhm);
                }
            }
            auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has  " + auditMsg + " an Opening Balance Debit Note " + dnNumber, request, dnNumber);
        }catch (JSONException ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }catch (ParseException ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return returnList;
    }

    private void getDNDetails(HashSet<DebitNoteDetail> dndetails, String companyId) throws ServiceException {

        KwlReturnObject result = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
        Company company = (Company) result.getEntityList().get(0);

        DebitNoteDetail row = new DebitNoteDetail();
        String DebitNoteDetailID = StringUtil.generateUUID();
        row.setID(DebitNoteDetailID);
        row.setSrno(1);
        row.setTotalDiscount(0.00);
        row.setCompany(company);
        row.setMemo("");
        dndetails.add(row);
    }

    public ModelAndView getOpeningBalanceDNs(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = gettDebitNoteMap(request);
            String vendorid = request.getParameter("custVenId");
            requestParams.put("vendorid", vendorid);
            // get opening balance receipts created from opening balance button.
            KwlReturnObject result = accDebitNoteobj.getOpeningBalanceDNs(requestParams);
            List<DebitNote> list = result.getEntityList();
            getOpeningBalanceDNJson(request, list, DataJArr);

//            // getting normal receipts of past year which has been converted into opening balance receipts.
//            result = accReceiptDAOobj.getOpeningBalanceNormalReceipts(requestParams);
//            list = result.getEntityList();
//            getOpeningBalanceReceiptJson(request, list, DataJArr);

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
        } catch (JSONException ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }

        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void getOpeningBalanceDNJson(HttpServletRequest request, List<DebitNote> list, JSONArray dataArray) {
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
//            DecimalFormat decimalFormat = new DecimalFormat("#0.00");
            SimpleDateFormat sdf = (SimpleDateFormat) authHandler.getGlobalDateFormat();
            HashMap<String, Object> requestParams = gettDebitNoteMap(request);
            if (list != null && !list.isEmpty()) {
                Iterator it = list.iterator();
                HashMap<String, Object> badDebtMap = new HashMap<>();            
                badDebtMap.put("badDebtType", 0);
                KwlReturnObject badDebtResult=null;
                while (it.hasNext()) {
                    DebitNote cn = (DebitNote) it.next();
                    boolean isLinkedInvoiceClaimed=false;
                    Date cnCreationDate = null;
                    Double cnAmount = 0d;
                    
//                    if (cn.isNormalDN()) {
//                        cnCreationDate = cn.getJournalEntry().getEntryDate();
//                    } else {
//                        cnCreationDate = cn.getCreationDate();
//                    }e
                    cnCreationDate = cn.getCreationDate();

                    cnAmount = cn.getDnamount();

                    double exchangeRateForOtherCurrency = cn.getExchangeRateForOpeningTransaction();
                    boolean isopeningBalanceDN = cn.isIsOpeningBalenceDN();

                    JSONObject cnJson = new JSONObject();
                    cnJson.put("transactionId", cn.getID());
                    cnJson.put("exchangeRateForOtherCurrency", exchangeRateForOtherCurrency);
                    cnJson.put("isCurrencyToBaseExchangeRate", cn.isConversionRateFromCurrencyToBase());
                    cnJson.put("isNormalTransaction", cn.isNormalDN());
                    cnJson.put("transactionNo", cn.getDebitNoteNumber());
                    cnJson.put("transactionAmount", authHandler.formattedAmount(cnAmount, companyid));
                    cnJson.put("transactionDate", df.format(cnCreationDate));
                    cnJson.put("currencysymbol", (cn.getCurrency() == null ? "" : cn.getCurrency().getSymbol()));
                    cnJson.put("currencyid", (cn.getCurrency() == null ? "" : cn.getCurrency().getCurrencyID()));
                    cnJson.put("transactionAmountDue", authHandler.formattedAmount(cn.getOpeningBalanceAmountDue(), companyid));
                    cnJson.put("narration", cn.getNarration());
                    cnJson.put("memo", (StringUtil.isNullOrEmpty(cn.getMemo())? "" : cn.getMemo()));
                    if(cn.getModifiedby()!=null){
                            cnJson.put("lasteditedby",StringUtil.getFullName(cn.getModifiedby()));
                    }
                    double transactionAmountInBase = 0d;
                    if (Constants.OpeningBalanceBaseAmountFlag) {
                        transactionAmountInBase = cn.getOriginalOpeningBalanceBaseAmount();
                    } else {
                        KwlReturnObject bAmt = null;
                        if (isopeningBalanceDN && cn.isConversionRateFromCurrencyToBase()) {// if DN is opening balance DN and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, cnAmount, cn.getCurrency().getCurrencyID(), cn.getCreationDate(), exchangeRateForOtherCurrency);
                        } else {
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, cnAmount, cn.getCurrency().getCurrencyID(), cn.getCreationDate(), exchangeRateForOtherCurrency);
                        }
                        transactionAmountInBase = (Double) bAmt.getEntityList().get(0);
                    }
                    cnJson.put("transactionAmountInBase", authHandler.formattedAmount(transactionAmountInBase, companyid));
                    int isReval = 0;
                    KwlReturnObject brdAmt = accGoodsReceiptobj.getRevalFlag(cn.getID());
                    List reval = brdAmt.getEntityList();
                    if (!reval.isEmpty() && (Long) reval.get(0) > 0) {
                        isReval = 1;
                    }
                    cnJson.put("isreval", isReval);
                    /*
                     * Applicable for Malaysian Country. Checking if any linked invoice is claimed 
                     */
                    GoodsReceipt GR = null;
                    if(cn.getRows() != null && !cn.getRows().isEmpty()){
                        for(DebitNoteDetail noteDetail : cn.getRows()){
                            GR = noteDetail.getGoodsReceipt();
                            if (GR!=null && (noteDetail.getGoodsReceipt().getBadDebtType() == 1 || noteDetail.getGoodsReceipt().getBadDebtType() == 2)) {
                                badDebtMap.put("invoiceid", noteDetail.getGoodsReceipt().getID());
                                badDebtMap.put("companyid", cn.getCompany().getCompanyID());
                                badDebtResult = accGoodsReceiptobj.getBadDebtPurchaseInvoiceMappingForGoodsReceipt(badDebtMap);
                                List<BadDebtPurchaseInvoiceMapping> maplist = badDebtResult.getEntityList();
                                if (maplist != null && !maplist.isEmpty()) {
                                    BadDebtPurchaseInvoiceMapping mapping = maplist.get(0);
//                                    if (!isLinkedInvoiceClaimed && mapping.getBadDebtClaimedDate().getTime() > noteDetail.getGrLinkDate()) {
                                    if (!isLinkedInvoiceClaimed && (mapping.getBadDebtClaimedDate().after(noteDetail.getGrLinkDate()))) {
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
        } catch (ServiceException ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

    }

    public ModelAndView importOpeningBalanceCustomerDNs(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            boolean isVendor = false;
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
            requestParams.put("moduleName", "Opening Customer Debit Note");
            requestParams.put("moduleid", Constants.Acc_opening_Customer_DebitNote);
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

                jobj = importOeningTransactionsRecords(request, datajobj, isVendor);
            } else if (doAction.compareToIgnoreCase("validateData") == 0) {
                jobj = importHandler.validateFileData(requestParams);
            }
            issuccess = true;
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();

            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException e) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }

            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView importOpeningBalanceVendorDNs(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            boolean isVendor = true;
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
            requestParams.put("moduleName", "Opening Vendor Debit Note");
            requestParams.put("moduleid", Constants.Acc_opening_Vendor_DebitNote);
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

                jobj = importOeningTransactionsRecords(request, datajobj, isVendor);
            } else if (doAction.compareToIgnoreCase("validateData") == 0) {
                jobj = importHandler.validateFileData(requestParams);
            }
            issuccess = true;
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();

            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException e) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }

            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /*
     * Method to save Opening Balance DN For customer.
     */
    public ModelAndView saveOpeningBalanceCustomerDN(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject jobjDetails = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("BR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        String dnNumber = request.getParameter("number");
        boolean isAccountingExe=false;
        String companyid ="";
        try {
             companyid = sessionHandlerImpl.getCompanyid(request);
            String cnId = request.getParameter("transactionId");
            KwlReturnObject cncount=null;
            if (StringUtil.isNullOrEmpty(cnId)) {
                /*
                 * Checks duplicate number while creating new record
                 */
                cncount =  accDebitNoteobj.getDNFromNoteNo(dnNumber, companyid);
                if (cncount.getRecordTotalCount() > 0) {
                    isAccountingExe=true;
                    throw new AccountingException(messageSource.getMessage("acc.DN.debitnoteno", null, RequestContextUtils.getLocale(request)) + dnNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                }
                
                /*
                 * code for checking wheather entered number can be generated by sequence format or not
                 */
                List resultList = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Debit_Note_ModuleId, dnNumber, companyid);
                if (!resultList.isEmpty()) {
                    boolean isvalidEntryNumber = (Boolean) resultList.get(0);
                    String formatName = (String) resultList.get(1);
                    if (!isvalidEntryNumber) {
                        isAccountingExe=true;
                        throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, RequestContextUtils.getLocale(request)) + " <b>" + dnNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, RequestContextUtils.getLocale(request)) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.pleaseentersomeothernumber", null, RequestContextUtils.getLocale(request)));
                    }
                }
            }
            
            synchronized (this) {
                /*
                 * Checks duplicate number for simultaneous transactions
                 */
                status = txnManager.getTransaction(def);
                    KwlReturnObject resultInv = accCommonTablesDAO.getTransactionInTemp(dnNumber, companyid, Constants.Acc_Debit_Note_ModuleId);//Get entry from temporary table
                    if (resultInv.getRecordTotalCount() > 0) {
                        isAccountingExe=true;
                        throw new AccountingException(messageSource.getMessage("acc.DN.selecteddebitnoteno", null, RequestContextUtils.getLocale(request)) + dnNumber + messageSource.getMessage("acc.field.alreadyinprocess.", null, RequestContextUtils.getLocale(request)));
                    } else {
                        accCommonTablesDAO.insertTransactionInTemp(dnNumber, companyid, Constants.Acc_Debit_Note_ModuleId);//Insert entry into temporary table
                    }
                txnManager.commit(status);
            }
            status = txnManager.getTransaction(def);
            List li = saveOpeningBalanceCustomerDN(request);
//            String cnNumber = null;
            if (!li.isEmpty()) {
                dnNumber = li.get(0).toString();
            }

            issuccess = true;
            msg = messageSource.getMessage("acc.accPref.autoDN", null, RequestContextUtils.getLocale(request)) + " " + dnNumber + " " + messageSource.getMessage("acc.field.hasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
            status = txnManager.getTransaction(def);
            accCommonTablesDAO.deleteTransactionInTemp(dnNumber, companyid,Constants.Acc_Debit_Note_ModuleId);//Delete entry in temporary table
            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            if(status!=null){
                txnManager.rollback(status);
            }
            try {
                accCommonTablesDAO.deleteTransactionInTemp(dnNumber, companyid,Constants.Acc_Debit_Note_ModuleId);//Delete entry in temporary table
            } catch (ServiceException ex1) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            if(status!=null){
                txnManager.rollback(status);
            }
            try {
                accCommonTablesDAO.deleteTransactionInTemp(dnNumber, companyid,Constants.Acc_Debit_Note_ModuleId);//Delete entry in temporary table
            } catch (ServiceException ex1) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
           if(status!=null){
                txnManager.rollback(status);
            }
            try {
                accCommonTablesDAO.deleteTransactionInTemp(dnNumber, companyid,Constants.Acc_Debit_Note_ModuleId);//Delete entry in temporary table
            } catch (ServiceException ex1) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArr);
                 jobj.put("accException", isAccountingExe);
            } catch (JSONException ex) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public List saveOpeningBalanceCustomerDN(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
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

            String dnNumber = request.getParameter("number");
            String transactionDateStr = request.getParameter("billdate");
            String currencyid = (request.getParameter("currencyid") == null ? sessionHandlerImpl.getCurrencyID(request) : request.getParameter("currencyid"));
            String transactionAmountStr = request.getParameter("transactionAmount");
            String dnId = request.getParameter("transactionId");
            String narrationValue = request.getParameter("narration");
            String customerId = request.getParameter("accountId");
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

            if (!StringUtil.isNullOrEmpty(customerId)) {
                KwlReturnObject custresult = accountingHandlerDAOobj.getObject(Customer.class.getName(), customerId);
                Customer customer = (Customer) custresult.getEntityList().get(0);
                accountId = customer.getAccount().getID();
            }

            double transactionAmount = 0d;
            if (!StringUtil.isNullOrEmpty(transactionAmountStr)) {
                transactionAmount = Double.parseDouble(transactionAmountStr);
            }

            // creating dn data

            HashMap<String, Object> dnhm = new HashMap<String, Object>();

            if (StringUtil.isNullOrEmpty(dnId)) {
                result = accDebitNoteobj.getDNFromNoteNo(dnNumber, companyid);
                int count = result.getRecordTotalCount();
                if (count > 0) {
                    throw new AccountingException(messageSource.getMessage("acc.field.Debitnotenumber", null, RequestContextUtils.getLocale(request)) + " " + dnNumber + " " + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                }
                dnhm.put("entrynumber", dnNumber);
                dnhm.put("autogenerated", false);
            }


            if (!StringUtil.isNullOrEmpty(dnId)) {
                // check for is DN Linked with Payment or Vendor Invoice.
                boolean isNoteLinkedWithPayment = accDebitNoteService.isNoteLinkedWithPayment(dnId);
                if (isNoteLinkedWithPayment) {
                    throw new AccountingException(messageSource.getMessage("acc.field.SelectedDebitNoteisLinkedWithPaymentsoitcannotbeedit", null, RequestContextUtils.getLocale(request)));
                }

                boolean isNoteLinkedWithInvoice = accDebitNoteService.isNoteLinkedWithInvoice(dnId, companyid);
                if (isNoteLinkedWithInvoice) {
                    throw new AccountingException(messageSource.getMessage("acc.field.SelectedDebitNoteisLinkedWithvendorinvoicesoitcannotbeedit", null, RequestContextUtils.getLocale(request)));
                }
                KwlReturnObject deleteResult = accDebitNoteobj.deleteDebitNoteDetails(dnId, companyid);
                isEditInvoice = true;
                dnhm.put("dnid", dnId);
                auditMsg = "updated";
                auditID = AuditAction.OPENING_BALANCE_UPDATED;
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("memo"))) {
                memo = request.getParameter("memo").toString();
            }

            dnhm.put("dnamount", transactionAmount);//
            dnhm.put("currencyid", currencyid);//
            dnhm.put("externalCurrencyRate", externalCurrencyRate);//
            dnhm.put("memo", memo);//
            dnhm.put("companyid", companyid);//
            dnhm.put("narrationValue", narrationValue);//
            dnhm.put("creationDate", transactionDate);//
            dnhm.put("customerid", customerId);//
            dnhm.put("accountId", accountId);//
            dnhm.put("isOpeningBalenceDN", true);
            dnhm.put("normalDN", false);//
            dnhm.put("openingBalanceAmountDue", transactionAmount);//
            if (conversionRateFromCurrencyToBase) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                dnhm.put(Constants.originalOpeningBalanceBaseAmount, authHandler.round(transactionAmount * exchangeRateForOpeningTransaction, companyid));
                dnhm.put(Constants.openingBalanceBaseAmountDue, authHandler.round(transactionAmount * exchangeRateForOpeningTransaction, companyid));
            } else {
                dnhm.put(Constants.originalOpeningBalanceBaseAmount, authHandler.round(transactionAmount / exchangeRateForOpeningTransaction, companyid));
                dnhm.put(Constants.openingBalanceBaseAmountDue, authHandler.round(transactionAmount / exchangeRateForOpeningTransaction, companyid));//
            }
            dnhm.put("exchangeRateForOpeningTransaction", exchangeRateForOpeningTransaction);//
            dnhm.put("isDNForVendor", false);//
            dnhm.put("openflag", true);
            dnhm.put("otherwise", true);
            dnhm.put("dnamountdue", transactionAmount);
            dnhm.put("conversionRateFromCurrencyToBase", conversionRateFromCurrencyToBase);

            if (isEditInvoice) {
                String modifiedby = sessionHandlerImpl.getUserid(request);
                long updatedon = System.currentTimeMillis();
                dnhm.put("modifiedby", modifiedby);
                dnhm.put("updatedon", updatedon);

                result = accDebitNoteobj.updateDebitNote(dnhm);
            } else {
                String createdby = sessionHandlerImpl.getUserid(request);
                String modifiedby = sessionHandlerImpl.getUserid(request);
                long createdon = System.currentTimeMillis();
                long updatedon = System.currentTimeMillis();

                dnhm.put("createdby", createdby);
                dnhm.put("modifiedby", modifiedby);
                dnhm.put("createdon", createdon);
                dnhm.put("updatedon", updatedon);
                dnhm.put("approvestatuslevel", 11);

                result = accDebitNoteobj.addDebitNote(dnhm);
            }

            DebitNote dn = (DebitNote) result.getEntityList().get(0);

            HashSet<DebitNoteDetail> dndetails = new HashSet<DebitNoteDetail>();
            getDNDetails(dndetails, companyid);


            Iterator itr = dndetails.iterator();
            while (itr.hasNext()) {
                DebitNoteDetail dnd = (DebitNoteDetail) itr.next();
                dnd.setDebitNote(dn);
            }
            dnhm.put("dnid", dn.getID());
            dnhm.put("dndetails", dndetails);

            result = accDebitNoteobj.updateDebitNote(dnhm);

            returnList.add(dn.getDebitNoteNumber());
            
            String customfield = request.getParameter("customfield");
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_OpeningBalanceDebitNote_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_OpeningBalanceDebitNoteid);
                customrequestParams.put("modulerecid", dn.getID());
                customrequestParams.put("moduleid", Constants.Acc_Debit_Note_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_OpeningBalanceDebitNote_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    dnhm.put("dnid", dn.getID());
                    dnhm.put("openingBalanceDebitNoteCustomData", dn.getID());
                    result = accDebitNoteobj.updateDebitNote(dnhm);
                }
            }
            auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has  " + auditMsg + " an Opening Balance Customer Debit Note" + dnNumber, request, dnNumber);
        } catch (JSONException ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ParseException ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return returnList;
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

    private Vendor getVendorByCode(String vendorCode, String companyID, HttpServletRequest request) throws AccountingException {
        Vendor vendor = null;
        try {
            if (!StringUtil.isNullOrEmpty(vendorCode) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accVendorDAOObj.getVendorByCode(vendorCode, companyID);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    vendor = (Vendor) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException(messageSource.getMessage("acc.field.SystemFailurewhilefetchingVendor", null, RequestContextUtils.getLocale(request)));
        }
        return vendor;
    }

    private String getCurrencyId(String currencyName, HashMap currencyMap) {
        String currencyId = "";
        if (currencyMap != null && currencyMap.containsKey(currencyName)) {
            currencyId = currencyMap.get(currencyName).toString();
        }
        return currencyId;
    }

    private Customer getCustomerByCode(String customerCode, String companyID, HttpServletRequest request) throws AccountingException {
        Customer customer = null;
        try {
            if (!StringUtil.isNullOrEmpty(customerCode) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accCustomerDAOObj.getCustomerByCode(customerCode, companyID);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    customer = (Customer) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException(messageSource.getMessage("acc.field.SystemFailurewhilefetchingcustomer", null, RequestContextUtils.getLocale(request)));
        }
        return customer;
    }
       //If Exception occured or payment completed  then delete entry from temporary table
    public void deleteTemporaryInvoicesEntries(String invoiceIdsArrary [],String companyid) {
        try {
            for (String invoiceId : invoiceIdsArrary) {
                accPaymentDAOobj.deleteUsedInvoiceOrCheque(invoiceId, companyid);
            }
        } catch (Exception ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public JSONObject importOeningTransactionsRecords(HttpServletRequest request, JSONObject jobj, boolean isVendor) throws AccountingException, IOException, SessionExpiredException, JSONException {

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
            SimpleDateFormat formatter = (SimpleDateFormat) authHandler.getDateOnlyFormat(request);
            
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
                } else {
                    try {
                        /*
                         * 1. DN Number
                         */
                        String invoiceNumber = "";
                        if (columnConfig.containsKey("DebitNoteNumber")) {
                            invoiceNumber = recarr[(Integer) columnConfig.get("DebitNoteNumber")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(invoiceNumber)) {
                                throw new AccountingException("Empty data found in Transaction Number, cannot set empty data for Transaction Number.");
                            } else if (!transactionNumberSet.add(invoiceNumber)) {// this method retur true when added or false when already exit record not get added
                                throw new AccountingException("Duplicate Transaction Number '" + invoiceNumber + "' in file.");
                            } else {
                                KwlReturnObject result = accDebitNoteobj.getDNFromNoteNo(invoiceNumber, companyid);
                                int count = result.getRecordTotalCount();
                                if (count > 0) {
                                    throw new AccountingException(messageSource.getMessage("acc.field.Debitnotenumber", null, RequestContextUtils.getLocale(request)) + invoiceNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                                }
                            }

                            JSONObject configObj = configMap.get("DebitNoteNumber");
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

                        String accountId = "";
                        String vendorId = "";
                        String customerId = "";

                        if (isVendor) {
                             /*
                             * 2. Vendor Code
                             */
                            String vendorCode = "";
                            if (columnConfig.containsKey("VendorCode")) {
                                vendorCode = recarr[(Integer) columnConfig.get("VendorCode")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(vendorCode)) {
                                    Vendor vendor = getVendorByCode(vendorCode, companyid, request);
                                    if (vendor != null) {
                                        accountId = vendor.getAccount().getID();
                                        vendorId = vendor.getID();
                                    } else {
                                        if (masterPreference.equalsIgnoreCase("0")) { 
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
                                            failureMsg += messageSource.getMessage("acc.field.VendorisnotfoundforVendorCodeName", null, RequestContextUtils.getLocale(request));
                                        }
                                    } else {
                                        failureMsg += messageSource.getMessage("acc.field.VendorisnotfoundforVendorCodeName", null, RequestContextUtils.getLocale(request));
                                    }
                                } else {
                                    failureMsg += messageSource.getMessage("acc.field.VendorisnotfoundforVendorCodeName", null, RequestContextUtils.getLocale(request));
                                }
                            }
                        } else {
                            /*
                             * 2. Customer Code
                             */
                            if (columnConfig.containsKey("CustomerCode")) {
                                String customerCode = recarr[(Integer) columnConfig.get("CustomerCode")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(customerCode)) {
                                    Customer customer = getCustomerByCode(customerCode, companyid, request);
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
                                            failureMsg += messageSource.getMessage("acc.field.CustomerisnotfoundforCustomerCodeName", null, RequestContextUtils.getLocale(request));
                                        }
                                    } else {
                                        failureMsg += messageSource.getMessage("acc.field.CustomerisnotfoundforCustomerCodeName", null, RequestContextUtils.getLocale(request));
                                    }
                                } else {
                                    failureMsg += messageSource.getMessage("acc.field.CustomerisnotfoundforCustomerCodeName", null, RequestContextUtils.getLocale(request));
                                }
                            }
                        }

                        /*
                         * 4. Creation Date
                         */
                        String transactionDateStr = "";
                        Date transactionDate = null, bookbeginningdate = null;
                        if (columnConfig.containsKey("CreationDate")) {
                            transactionDateStr = recarr[(Integer) columnConfig.get("CreationDate")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(transactionDateStr)) {
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
                                        failureMsg += "Incorrect date format for Transaction Date, Please specify values in " + dateFormat + " format.";
                                    } catch (Exception ex) {
                                        failureMsg += ex.getMessage();
                                    }
                                }
                            } else {
                                failureMsg += messageSource.getMessage("acc.field.TransactionDateisnotavailable", null, RequestContextUtils.getLocale(request));
                            }
                        } else {
                            failureMsg += messageSource.getMessage("acc.field.TransactionDateisnotavailable", null, RequestContextUtils.getLocale(request));
                        }

                        /*
                         * 5. DN Currency
                         */
                        String currencyId = "";
                        if (isCurrencyCode?columnConfig.containsKey("currencyCode"):columnConfig.containsKey("Currency")) {
                            String currencyStr = recarr[isCurrencyCode?(Integer) columnConfig.get("currencyCode"):(Integer) columnConfig.get("Currency")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(currencyStr)) {
                                failureMsg += "Empty data found in Currency, cannot set empty data for Currency.";
                            } else {
                                currencyId = getCurrencyId(currencyStr, currencyMap);
                                if (StringUtil.isNullOrEmpty(currencyId)) {
                                    if (masterPreference.equalsIgnoreCase("0")) {// most restricted case
                                        failureMsg += "Currency entry not found in master list for Currency dropdown.";
                                    } else if (masterPreference.equalsIgnoreCase("1")) {// add empty value if data invalid for non mandatory
                                        failureMsg += "Currency entry not found in master list for Currency dropdown, cannot set empty data for Currency.";
                                    } else if (masterPreference.equalsIgnoreCase("2")) {//add new (most lanient)
                                        failureMsg += "Currency entry not present in Currency list, Please create new Currency entry for " + currencyStr + " as it requires some other details.";
                                    }
                                }
                            }
                        } else {
                            failureMsg += messageSource.getMessage("acc.field.Currencyisnotavailable", null, RequestContextUtils.getLocale(request));
                        }
                        
                        
                        /*6. Amount*/
                        String transactionAmountStr = "";
                        double transactionAmount = 0d;
                        if(columnConfig.containsKey("Amount")){
                            transactionAmountStr = recarr[(Integer) columnConfig.get("Amount")].replaceAll("\"", "").trim();
                            if(StringUtil.isNullOrEmpty(transactionAmountStr)){
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

                            Date applyDate = authHandler.getDateOnlyFormat().parse(authHandler.getDateOnlyFormat().format(cal.getTime()));

                            currMap.put("applydate", applyDate);
                            currMap.put("gcurrencyid", gcurrencyId);
                            currMap.put("companyid", companyid);
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
                                requestParamsCF.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Debit_Note_ModuleId, headArrayList.get(K)));
                                KwlReturnObject fieldParamsResult = accCommonTablesDAO.getFieldParams(requestParamsCF); // get custom field for module
                                FieldParams params = null;
                                if (fieldParamsResult.getRecordTotalCount() > 0) {
                                    params = (FieldParams) fieldParamsResult.getEntityList().get(0);
                                    customFieldParamMap.put(headArrayList.get(K).toString(), params);
                                }
                            }
                        }
                        
                        // creating dn data

                        HashMap<String, Object> dnhm = new HashMap<String, Object>();

                        dnhm.put("entrynumber", invoiceNumber);
                        dnhm.put("autogenerated", false);
                        dnhm.put("dnamount", transactionAmount);//
                        dnhm.put("currencyid", currencyId);//
                        dnhm.put("externalCurrencyRate", externalCurrencyRate);//
                        dnhm.put("memo", memo);//
                        dnhm.put("companyid", companyid);//
                        dnhm.put("narrationValue", narrationValue);//
                        dnhm.put("creationDate", transactionDate);//
                        if (isVendor) {
                            dnhm.put("vendorid", vendorId);//
                            dnhm.put("isDNForVendor", true);//
                        } else {
                            dnhm.put("customerid", customerId);//
                            dnhm.put("isDNForVendor", false);//
                        }
                        dnhm.put("accountId", accountId);//
                        dnhm.put("isOpeningBalenceDN", true);
                        dnhm.put("normalDN", false);//
                        dnhm.put("openingBalanceAmountDue", transactionAmount);//
                        dnhm.put(Constants.originalOpeningBalanceBaseAmount, authHandler.round(transactionAmount*exchangeRateForOpeningTransaction, companyid));
                        dnhm.put(Constants.openingBalanceBaseAmountDue, authHandler.round(transactionAmount*exchangeRateForOpeningTransaction, companyid));
                        dnhm.put("exchangeRateForOpeningTransaction", exchangeRateForOpeningTransaction);//

                        dnhm.put("openflag", true);
                        dnhm.put("dnamountdue", transactionAmount);
                        dnhm.put("conversionRateFromCurrencyToBase", true);

                        String createdby = sessionHandlerImpl.getUserid(request);
                        String modifiedby = sessionHandlerImpl.getUserid(request);
                        long createdon = System.currentTimeMillis();
                        long updatedon = System.currentTimeMillis();

                        dnhm.put("createdby", createdby);
                        dnhm.put("modifiedby", modifiedby);
                        dnhm.put("createdon", createdon);
                        dnhm.put("updatedon", updatedon);
                        dnhm.put("approvestatuslevel", 11);

                        KwlReturnObject result = accDebitNoteobj.addDebitNote(dnhm);


                        HashSet<DebitNoteDetail> dndetails = new HashSet<DebitNoteDetail>();
                        getDNDetails(dndetails, companyid);

                        DebitNote dn = (DebitNote) result.getEntityList().get(0);

                        Iterator itr = dndetails.iterator();
                        while (itr.hasNext()) {
                            DebitNoteDetail dnd = (DebitNoteDetail) itr.next();
                            dnd.setDebitNote(dn);
                        }
                        dnhm.put("dnid", dn.getID());
                        dnhm.put("dndetails", dndetails);
                        dnhm.put("otherwise", true);
                        result = accDebitNoteobj.updateDebitNote(dnhm);

                        JSONArray customJArr = fieldDataManagercntrl.getCustomFieldForOeningTransactionsRecords(headArrayList, customFieldParamMap, recarr, columnConfig ,request);
                        customfield = customJArr.toString();
                        if (!StringUtil.isNullOrEmpty(customfield)) {
                            JSONArray jcustomarray = new JSONArray(customfield);
                            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                            customrequestParams.put("customarray", jcustomarray);
                            customrequestParams.put("modulename", Constants.Acc_OpeningBalanceDebitNote_modulename);
                            customrequestParams.put("moduleprimarykey", Constants.Acc_OpeningBalanceDebitNoteid);
                            customrequestParams.put("modulerecid", dn.getID());
                            customrequestParams.put("moduleid", Constants.Acc_Debit_Note_ModuleId);
                            customrequestParams.put("companyid", companyid);
                            customrequestParams.put("customdataclasspath", Constants.Acc_OpeningBalanceDebitNote_custom_data_classpath);
                            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                dnhm.put("dnid", dn.getID());
                                dnhm.put("openingBalanceDebitNoteCustomData", dn.getID());
                                result = accDebitNoteobj.updateDebitNote(dnhm);
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
                msg = messageSource.getMessage("acc.field.Imported", null, RequestContextUtils.getLocale(request)) + success +" "+ messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (success > 1 ? "s" : "") + " "+ messageSource.getMessage("acc.field.success", null, RequestContextUtils.getLocale(request))+ " ";
                msg += (failed == 0 ? "." : messageSource.getMessage("acc.field.andfailedtoimport", null, RequestContextUtils.getLocale(request)) + failed + " "+ messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (failed > 1 ? "s" : "") + ".");
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

            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
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
                logDataMap.put("Module", Constants.Acc_Debit_Note_ModuleId);
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userId);
                logDataMap.put("Company", companyid);
                importDao.saveImportLog(logDataMap);
                txnManager.commit(lstatus);
            } catch (Exception ex) {
                txnManager.rollback(lstatus);
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", getActualFileName(fileName));
            } catch (JSONException ex) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
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

    public ModelAndView getOpeningBalanceCustomerDNs(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = gettDebitNoteMap(request);
            String customerId = request.getParameter("custVenId");
            requestParams.put("customerid", customerId);
            // get opening balance receipts created from opening balance button.
            KwlReturnObject result = accDebitNoteobj.getOpeningBalanceCustomerDNs(requestParams);
            List<DebitNote> list = result.getEntityList();
            getOpeningBalanceCustomerDNJson(request, list, DataJArr);

//            // getting normal receipts of past year which has been converted into opening balance receipts.
//            result = accReceiptDAOobj.getOpeningBalanceNormalReceipts(requestParams);
//            list = result.getEntityList();
//            getOpeningBalanceReceiptJson(request, list, DataJArr);

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
        } catch (JSONException ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }

        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void getOpeningBalanceCustomerDNJson(HttpServletRequest request, List<DebitNote> list, JSONArray dataArray) {
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
//            DecimalFormat decimalFormat = new DecimalFormat("#0.00");
            SimpleDateFormat sdf = (SimpleDateFormat) authHandler.getGlobalDateFormat();
            HashMap<String, Object> requestParams = gettDebitNoteMap(request);
            if (list != null && !list.isEmpty()) {
                Iterator it = list.iterator();
                while (it.hasNext()) {
                    DebitNote cn = (DebitNote) it.next();

                    Date cnCreationDate = null;
                    Double cnAmount = 0d;

                    cnCreationDate = cn.getCreationDate();
//                    if (cn.isNormalDN()) {
//                        cnCreationDate = cn.getJournalEntry().getEntryDate();
//                    } else {
//                        cnCreationDate = cn.getCreationDate();
//                    }

                    cnAmount = cn.getDnamount();

                    double exchangeRateForOtherCurrency = cn.getExchangeRateForOpeningTransaction();
                    boolean isopeningBalanceDN = cn.isIsOpeningBalenceDN();

                    JSONObject cnJson = new JSONObject();
                    cnJson.put("transactionId", cn.getID());
                    cnJson.put("exchangeRateForOtherCurrency", exchangeRateForOtherCurrency);
                    cnJson.put("isCurrencyToBaseExchangeRate", cn.isConversionRateFromCurrencyToBase());
                    cnJson.put("isNormalTransaction", cn.isNormalDN());
                    cnJson.put("transactionNo", cn.getDebitNoteNumber());
                    cnJson.put("transactionAmount", authHandler.formattedAmount(cnAmount, companyid));
                    cnJson.put("transactionDate", df.format(cnCreationDate));
                    cnJson.put("currencysymbol", (cn.getCurrency() == null ? "" : cn.getCurrency().getSymbol()));
                    cnJson.put("currencyid", (cn.getCurrency() == null ? "" : cn.getCurrency().getCurrencyID()));
                    cnJson.put("transactionAmountDue", authHandler.formattedAmount(cn.getOpeningBalanceAmountDue(), companyid));
                    cnJson.put("narration", cn.getNarration());
                    cnJson.put("memo", (StringUtil.isNullOrEmpty(cn.getMemo()) ? "" : cn.getMemo()));
                    if(cn.getModifiedby()!=null){
                            cnJson.put("lasteditedby",StringUtil.getFullName(cn.getModifiedby()));
                    }
                    double transactionAmountInBase = 0d;
                    if (Constants.OpeningBalanceBaseAmountFlag) {
                        transactionAmountInBase = cn.getOriginalOpeningBalanceBaseAmount();
                    } else {
                        KwlReturnObject bAmt = null;
                        if (isopeningBalanceDN && cn.isConversionRateFromCurrencyToBase()) {// if DN is opening balance DN and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, cnAmount, cn.getCurrency().getCurrencyID(), cn.getCreationDate(), exchangeRateForOtherCurrency);
                        } else {
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, cnAmount, cn.getCurrency().getCurrencyID(), cn.getCreationDate(), exchangeRateForOtherCurrency);
                        }
                        transactionAmountInBase = (Double) bAmt.getEntityList().get(0);
                    }
                    cnJson.put("transactionAmountInBase", authHandler.formattedAmount(transactionAmountInBase, companyid));
                    int isReval = 0;
                    KwlReturnObject brdAmt = accGoodsReceiptobj.getRevalFlag(cn.getID());
                    List reval = brdAmt.getEntityList();
                    if (!reval.isEmpty() && (Long) reval.get(0) > 0) {
                        isReval = 1;
                    }
                    cnJson.put("isreval", isReval);
                    HashMap<String,Object> reqParams1 = new HashMap<>();
                    reqParams1.put("dnid",cn.getID());
                    reqParams1.put("companyid",cn.getCompany().getCompanyID());
                    KwlReturnObject linkResult=accDebitNoteobj.getLinkDetailReceiptToDebitNote(reqParams1);
                    if(!linkResult.getEntityList().isEmpty()){
                        cnJson.put("isNoteLinkedToAdvancePayment", true);
                    }
                    dataArray.put(cnJson);
                }
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

    }

    public ModelAndView deleteOpeningDNPermanent(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("VP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String TransactionsInUse = deleteOpeningDNPermanent(request);
            txnManager.commit(status);
            issuccess = true;
            msg = (StringUtil.isNullOrEmpty(TransactionsInUse)) ? messageSource.getMessage("acc.debitN.del", null, RequestContextUtils.getLocale(request)) : messageSource.getMessage("acc.ob.DNExcept", null, RequestContextUtils.getLocale(request)) + " " + TransactionsInUse + " " + messageSource.getMessage("acc.ob.asInUseAreDeleted", null, RequestContextUtils.getLocale(request));
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public String deleteOpeningDNPermanent(HttpServletRequest request) throws AccountingException, SessionExpiredException, ServiceException {

        String companyid = sessionHandlerImpl.getCompanyid(request);
        String dnid[] = request.getParameterValues("billidArray");
        String dnno[] = request.getParameterValues("invoicenoArray");
        String TransactionsInUse = "";
        for (int count = 0; count < dnid.length; count++) {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("dnid", dnid[count]);
            requestParams.put("companyid", companyid);
            String dnInUse = dnno[count];
            try {
                if (!StringUtil.isNullOrEmpty(dnid[count])) {
//                String dnno=request.getParameter("invoiceno");
                    KwlReturnObject dnObj = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), dnid[count]);
                    DebitNote debitNote = (DebitNote) dnObj.getEntityList().get(0);
                    // check for is DN Linked with Payment.
                    boolean isNoteLinkedWithPayment = accDebitNoteService.isNoteLinkedWithPayment(dnid[count]);
                    boolean isNoteLinkedWithAdvancePayment = accDebitNoteService.isNoteLinkedWithAdvancePayment(dnid[count]);        
                    if (isNoteLinkedWithPayment || isNoteLinkedWithAdvancePayment) {
                       TransactionsInUse += dnInUse + ",";
                        throw new AccountingException(messageSource.getMessage("acc.field.SelectedDebitNoteisLinkedWithPaymentsoitcannotbedelete", null, RequestContextUtils.getLocale(request)));
                    }else if(accDebitNoteService.isDebitNoteLinkedWithCreditNote(dnid[count], companyid)==true){
                        TransactionsInUse += dnInUse + ",";
                        throw new AccountingException(messageSource.getMessage("acc.field.SelectedDebitNoteisLinkedWithPaymentsoitcannotbedelete", null, RequestContextUtils.getLocale(request)));
                    }
                    //Delete unrealised JE for Debit Note
                    accJournalEntryobj.permanentDeleteJournalEntryDetailReval(dnid[count], companyid);
                    accJournalEntryobj.permanentDeleteJournalEntryReval(dnid[count], companyid);
                    /*
                    1= update invoice Amount due if linked to Note
                    2 = delete entry from linking information table
                    3= delete Note data
                    */
                    
                    // delete foreign gain loss JE
                    List resultJe = accDebitNoteobj.getForeignGainLossJE(dnid[count], companyid);
                    if (resultJe.size() > 0 && resultJe.get(0) != null) {
                        Iterator itr = resultJe.iterator();
                        while (itr.hasNext()) {
                            Object object = itr.next();
                            String jeid = object != null ? object.toString() : "";
                           accDebitNoteService.deleteJEArray(jeid, companyid);
                        }
                    }
                    /*
                     * Before deleting DebitNoteDetail Keeping id of Goodsrceipt
                     * utlized in Payment
                     */
                    Set<String> grIDSet = new HashSet<>();
                    if (debitNote.getApprovestatuslevel() == 11 && !debitNote.isDeleted()) {
                        for (DebitNoteDetail dnd : debitNote.getRows()) {
                            if (dnd.getGoodsReceipt() != null) {
                                grIDSet.add(dnd.getGoodsReceipt().getID());
                            }
                        }
                    }

                    //Delete Rouding JEs if created against PI
                    String roundingJENo = "";
                    String roundingIDs = "";
                    if (!grIDSet.isEmpty()) {
                        String piIDs = "";

                        for (String piID : grIDSet) {
                            piIDs = piID + ",";
                        }
                        if (!StringUtil.isNullOrEmpty(piIDs)) {
                            piIDs = piIDs.substring(0, piIDs.length() - 1);
                        }
                        KwlReturnObject jeResult = accJournalEntryobj.getRoundingJournalEntryByGRIds(piIDs, companyid);
                        List<JournalEntry> jeList = jeResult.getEntityList();
                        for (JournalEntry roundingJE : jeList) {
                            roundingJENo = roundingJE.getEntryNumber() + ",";
                            roundingIDs = roundingJE.getID() + ",";
                            accDebitNoteService.deleteJEArray(roundingJE.getID(), companyid);
                        }
                        if (!StringUtil.isNullOrEmpty(roundingJENo)) {
                            roundingJENo = roundingJENo.substring(0, roundingJENo.length() - 1);
                        }
                        if (!StringUtil.isNullOrEmpty(roundingIDs)) {
                            roundingIDs = roundingIDs.substring(0, roundingIDs.length() - 1);
                        }
                    }

                    accDebitNoteService.updateOpeningInvoiceAmountDue(dnid[count], companyid);
                    accDebitNoteobj.deleteLinkingInformationOfDN(requestParams);
                    accDebitNoteobj.deleteDebitNotesPermanent(requestParams);
                    auditTrailObj.insertAuditLog(AuditAction.OPENING_BALANCE_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted an Opening Balance Debit Note Permanently " + dnno[count], request, dnid[count]);
                    if (!StringUtil.isNullOrEmpty(roundingJENo)) {
                        auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted Debit Note " + dnno + " Permanently. So Rounding JE No. " + roundingJENo + " deleted.", request, roundingIDs);
                    }
                }
            } catch (Exception ex) {
                //throw new AccountingException(messageSource.getMessage("acc.pay1.excp1", null, RequestContextUtils.getLocale(request)));
            }
        }
        return TransactionsInUse;
    }

    public ModelAndView saveBillingDebitNote(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("PaymentMethods_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            BillingDebitNote debitnote = saveBillingDebitNote(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.debitN.save", null, RequestContextUtils.getLocale(request));   //"Debit Note has been saved successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /**
     * Description : Method is used to Save saveCreditNoteGstDetails
     * @param <paramJobj> :-Contains parameters company ID
     * @param <returnJobj> :-Contains return result of saveCreditNoteGst
     * @return :return list
     */
    public List saveDebitNoteGst(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException, JSONException {
        List returnList = new ArrayList();
        String debitNoteNumber = "";
        String debitNoteId = "";
        String oldjeid = "";
        String jeid = "";
        DebitNote debitNote = null;
        KwlReturnObject result;
        String linkedDocuments = "";
        String unlinkMessage = "";
        String jeentryNumber = "";
        boolean jeautogenflag = false;
        String jeIntegerPart = "";
        String jeDatePrefix = "";
        String jeDateAfterPrefix = "";
        String jeDateSuffix = "";
        String jeSeqFormatId = "";
        int approvalStatusLevel = 11;
        boolean isEditNote = false;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyid = request.getParameter(Constants.currencyKey);
            double externalCurrencyRate = StringUtil.getDouble(!StringUtil.isNullOrEmpty(request.getParameter(Constants.externalcurrencyrate))?request.getParameter(Constants.externalcurrencyrate) : "0.0");
            KwlReturnObject comp = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) comp.getEntityList().get(0);
            int countryid = Integer.parseInt(company.getCountry().getID());
            
            String entryNumber = request.getParameter("number");
            debitNoteId = request.getParameter("srid");
            String costCenterId = request.getParameter(Constants.costcenter);
            boolean isNoteAlso = false;
            String userFullName = request.getParameter(Constants.userfullname);
            String customfield = request.getParameter("customfield");
            boolean isEditToApprove = StringUtil.isNullOrEmpty(request.getParameter("isEditToApprove"))?false:Boolean.parseBoolean(request.getParameter("isEditToApprove"));
            boolean isInsertAudTrail = false; // This flag is added for showing Debit No in Audit Trial ERP-18558
            boolean isnegativestockforlocwar = false;
            int cntype = StringUtil.isNullOrEmpty(request.getParameter("cntype")) ? 1 : Integer.parseInt(request.getParameter("cntype"));
            String srid = ("srid");
            String salesPersonId=request.getParameter("salesPerson"); //ERP-28249 Debit note against customer for malaysian country.
            String agentId = request.getParameter("agent");
            String isfavourite = request.getParameter("isfavourite");
            String sequenceformat = request.getParameter(Constants.sequenceformat);
            HashMap<String, Object> doDataMap = new HashMap<String, Object>();
            long createdon = System.currentTimeMillis();
            String createdby = sessionHandlerImpl.getUserid(request);
            String modifiedby = createdby;
            long updatedon = createdon;
            SimpleDateFormat formatter = (SimpleDateFormat) authHandler.getDateOnlyFormat();
            DateFormat df = authHandler.getDateOnlyFormat();
            Date creationDate = formatter.parse(request.getParameter(Constants.BillDate));
            if (sequenceformat.equals("NA") || !StringUtil.isNullOrEmpty(srid)) {
                doDataMap.put("entrynumber", entryNumber);
            } else {
                doDataMap.put("entrynumber", "");
            }
	    if (sequenceformat.equals("NA")) {//SDP-14953 - In case of NA checks wheather this number can also be generated by a sequence format or not
                List resultList = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Debit_Note_ModuleId, entryNumber, companyid);
                if (!resultList.isEmpty()) {
                    boolean isvalidEntryNumber = (Boolean) resultList.get(0);
                    String formatName = (String) resultList.get(1);
                    if (!isvalidEntryNumber) {
                        throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, RequestContextUtils.getLocale(request))+" <b>"+entryNumber+"</b> "+messageSource.getMessage("acc.common.belongsto", null, RequestContextUtils.getLocale(request))+" <b>"+formatName+"</b>. "+messageSource.getMessage("acc.common.plselectseqformat", null, RequestContextUtils.getLocale(request))+" <b>"+formatName+"</b> "+messageSource.getMessage("acc.common.insteadof", null, RequestContextUtils.getLocale(request)));
                    }
                }
            }
//            String taxid = request.getParameter("taxid");;
            String taxid = "";
            if (request.getParameter("taxid").equalsIgnoreCase("None")) {
                taxid = null;
            } else {
                taxid = request.getParameter("taxid");
            }
            if (!StringUtil.isNullOrEmpty(taxid)) {
                doDataMap.put("taxid", taxid);                
            }
            /*
            *ERP-39555 : GST Currency Rate in DN Overcharge/Undercharge when Country currency is SGD and base currency is other than SGD 
            */
            if(!StringUtil.isNullOrEmpty(request.getParameter("gstCurrencyRate"))){
            doDataMap.put("gstCurrencyRate", request.getParameter("gstCurrencyRate"));              
            }
            doDataMap.put("autogenerated", sequenceformat.equals("NA") ? false : true);
            doDataMap.put(Constants.memo, request.getParameter(Constants.memo));
            doDataMap.put(Constants.posttext, request.getParameter(Constants.posttext) == null ? "" : request.getParameter(Constants.posttext));
            doDataMap.put("customerid", request.getParameter("customer"));
            doDataMap.put("vendorid", request.getParameter("vendor"));
            if (request.getParameter(Constants.shipdate) != null && !StringUtil.isNullOrEmpty(request.getParameter(Constants.shipdate))) {
                doDataMap.put(Constants.shipdate, df.parse(request.getParameter(Constants.shipdate)));
            }
            doDataMap.put(Constants.shipvia, request.getParameter(Constants.shipvia));
            doDataMap.put(Constants.fob, request.getParameter(Constants.fob));
            doDataMap.put("orderdate", df.parse(request.getParameter(Constants.BillDate)));
            doDataMap.put("creationDate", formatter.parse(request.getParameter(Constants.BillDate)));

            doDataMap.put("isfavourite", isfavourite);
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                doDataMap.put("costcenter", costCenterId);
            }
            doDataMap.put(Constants.companyKey, companyid);
            doDataMap.put(Constants.currencyKey, currencyid);
            doDataMap.put("cntype", cntype);

            doDataMap.put("createdon", createdon);
            doDataMap.put("createdby", createdby);
            doDataMap.put("modifiedby", modifiedby);
            doDataMap.put("updatedon", updatedon);
            doDataMap.put("salesPerson",salesPersonId);     //ERP-28249 Debit note against customer for malaysian country.
            doDataMap.put("agent", agentId);
            doDataMap.put("isNoteAlso", isNoteAlso);
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.MVATTRANSACTIONNO))) {
                doDataMap.put(Constants.MVATTRANSACTIONNO, request.getParameter(Constants.MVATTRANSACTIONNO));
            }else{
            doDataMap.put(Constants.MVATTRANSACTIONNO,"");
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.SUPPLIERINVOICENO))) {
                doDataMap.put(Constants.SUPPLIERINVOICENO, request.getParameter(Constants.SUPPLIERINVOICENO));
            }
            if (!StringUtil.isNullOrEmpty(debitNoteId)) {
                /*
                 * In case of edit if format is NA then entry number can be changed so need to check duplicate
                 */
                result = accDebitNoteobj.getDNFromNoteNoAndId(entryNumber, companyid, debitNoteId);
                if (result.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
                    throw new AccountingException(messageSource.getMessage("acc.field.Creditnotenumber", null, Locale.forLanguageTag(request.getParameter(Constants.language))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(request.getParameter(Constants.language))));
                }
                
                isEditNote = true;
                /**
                 * For US country Delete Debit Note line level terms details in Overcharge/ Undercharge condition
                 */
                if (countryid == Constants.USA_country_id) {
                    accDebitNoteobj.deleteDebitNoteDetailTermMapAgainstDebitNote(debitNoteId, companyid);
                }
                accJournalEntryobj.permanentDeleteDebitNoteAgainstCustomerGst(debitNoteId, companyid);
                accDebitNoteobj.deleteDebitTaxDetails(debitNoteId, companyid);
                KwlReturnObject cnObj = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), debitNoteId);
                debitNote = (DebitNote) cnObj.getEntityList().get(0);
                oldjeid = debitNote.getJournalEntry().getID();
                JournalEntry jetemp = debitNote.getJournalEntry();
                approvalStatusLevel = debitNote.getApprovestatuslevel();
                if (jetemp != null) {
                    jeentryNumber = jetemp.getEntryNumber(); //preserving these data to generate same JE number in edit case                    
                    jeautogenflag = jetemp.isAutoGenerated();
                    jeSeqFormatId = jetemp.getSeqformat() == null ? "" : jetemp.getSeqformat().getID();
                    jeIntegerPart = String.valueOf(jetemp.getSeqnumber());
                    jeDatePrefix = jetemp.getDatePreffixValue();
                    jeDateAfterPrefix = jetemp.getDateAfterPreffixValue();
                    jeDateSuffix = jetemp.getDateSuffixValue();
                }
                doDataMap.put("dnid", debitNoteId);
            } else {
                doDataMap.put("autogenerated", !sequenceformat.equals("NA") ? true : false);
                doDataMap.put("oldRecord", false);
                Long seqNumber = null;
                result = accDebitNoteobj.getDNSequenceNo(companyid, creationDate);
                List list = result.getEntityList();
                if (!list.isEmpty()) {
                    seqNumber = (Long) list.get(0);
                }
                doDataMap.put("sequence", seqNumber.intValue());
            }
            Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
            if (StringUtil.isNullOrEmpty(oldjeid)) {
                jeDataMap.put("entrynumber", "");
            } else {
                jeDataMap.put("entrynumber", jeentryNumber);
                jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                jeDataMap.put(Constants.SEQNUMBER, jeIntegerPart);
                jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
                jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
                jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
            }
            jeDataMap.put("autogenerated", true);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeDataMap.put("entrydate", formatter.parse(request.getParameter(Constants.BillDate)));
            jeDataMap.put(Constants.Checklocktransactiondate, request.getParameter("creationdate"));//ERP-16800-Without parsing date
            jeDataMap.put("companyid", companyid);
            jeDataMap.put("memo", request.getParameter("memo"));
            jeDataMap.put("createdby", createdby);
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                jeDataMap.put("costcenterid", costCenterId);
            }
            jeDataMap.put("currencyid", currencyid);
            HashSet<JournalEntryDetail> jedetails = new HashSet();
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            jeid = journalEntry.getID();
            jeDataMap.put("jeid", jeid);
            doDataMap.put("journalentryid", jeid);

            double totalAmt = 0, totalRowDiscount = 0, totalAmountinbase = 0;
            double subTotal = 0, taxAmt = 0;
            JSONArray jArr = new JSONArray(request.getParameter(Constants.detail));
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                double qrate = authHandler.roundUnitPrice(jobj.optDouble("rate", 0),companyid);
                double quantity = authHandler.roundQuantity(jobj.getDouble("quantity"), companyid);
                double quotationPrice = authHandler.round(quantity * qrate, companyid);
                double discountQD = authHandler.round(jobj.optDouble("prdiscount", 0), companyid);
                double discountPerRow = 0;
                if (jobj.optInt("discountispercent", 0) == 1) {//percent discount
                    discountPerRow = authHandler.round((quotationPrice * discountQD / 100), companyid);
                } else {//flat discount
                    discountPerRow = discountQD;
                }
                totalRowDiscount += discountPerRow;
                taxAmt += jobj.optDouble("taxamount", 0.0);
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("subTotal"))) {
                subTotal = Double.parseDouble(request.getParameter("subTotal"));
            }
            /*
             * if global level tax applied it will execute only for dashbord
             * transaction
             */
            if (taxAmt == 0) {
                if (!StringUtil.isNullOrEmpty(request.getParameter("taxamount"))) {
                    taxAmt = Double.parseDouble(request.getParameter("taxamount"));
                }
            }

            totalAmt = subTotal + taxAmt;
            totalRowDiscount = authHandler.round(totalRowDiscount, companyid);
            doDataMap.put("totallineleveldiscount", totalRowDiscount);
            doDataMap.put("dnamount", totalAmt);
            doDataMap.put("dnamountdue",totalAmt);
            doDataMap.put("approvestatuslevel", approvalStatusLevel);
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            filterRequestParams.put(Constants.companyKey, companyid);
            filterRequestParams.put(Constants.globalCurrencyKey, currencyid);
            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, totalAmt, currencyid, df.parse(request.getParameter(Constants.BillDate)), externalCurrencyRate);
            totalAmountinbase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
            doDataMap.put("cnamountinbase", totalAmountinbase);
            doDataMap.put("dnamountinbase", totalAmountinbase);
            KwlReturnObject descbAmtTax = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, totalRowDiscount, currencyid, df.parse(request.getParameter(Constants.BillDate)), externalCurrencyRate);
            double descountinBase = authHandler.round((Double) descbAmtTax.getEntityList().get(0), companyid);
            doDataMap.put("discountinbase", descountinBase);
            doDataMap.put("creationDate", creationDate);
            KwlReturnObject doresult = null;
            if (isEditNote) {
                doresult = accDebitNoteobj.updateDebitNote(doDataMap);
            } else {
                //add accountID respective selected perosn in case of CN/DN Overchaged, Undercharged. 
                doDataMap.put("accountId", !StringUtil.isNullObject(request.getParameter("personaccid")) ? request.getParameter("personaccid") : "");
                doresult = accDebitNoteobj.addDebitNote(doDataMap);
            }
            debitNote = (DebitNote) doresult.getEntityList().get(0);
            double totalAmountInBase = 0;
            KwlReturnObject baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(jeDataMap, totalAmt, currencyid, creationDate, externalCurrencyRate);
            totalAmountInBase = (Double) baseAmount.getEntityList().get(0);
            HashMap<String, Object> CNApproveMap = new HashMap<String, Object>();
            CNApproveMap.put("companyid", sessionHandlerImpl.getCompanyid(request));
            List approvedlevel = null;
            if (isEditToApprove) {
                CNApproveMap.put("billid", debitNote.getID());
                CNApproveMap.put("userid", createdby);
                CNApproveMap.put("remark", "");
                CNApproveMap.put("userName", userFullName);
                CNApproveMap.put("level", debitNote.getApprovestatuslevel());
                CNApproveMap.put("amount", String.valueOf(authHandler.round(totalAmountInBase, companyid)));
                CNApproveMap.put("isEditToApprove", isEditToApprove);
                List list1 = accDebitNoteService.approvePendingDebitNote(CNApproveMap);
                approvalStatusLevel=11;
                jeDataMap.put(Constants.SEQFORMAT, journalEntry.getSeqformat()!=null?journalEntry.getSeqformat().getID():null);
                jeDataMap.put(Constants.SEQNUMBER, journalEntry.getSeqnumber());
                jeDataMap.put(Constants.DATEPREFIX, journalEntry.getDatePreffixValue());
                jeDataMap.put(Constants.DATEAFTERPREFIX, journalEntry.getDateAfterPreffixValue());
                jeDataMap.put(Constants.DATESUFFIX, journalEntry.getDateSuffixValue());
                jeDataMap.put("entrynumber", journalEntry.getEntryNumber());
            } else {
                CNApproveMap.put("level", 0);//Initialy it will be 0
                CNApproveMap.put("totalAmount", String.valueOf(authHandler.round(totalAmountInBase, companyid)));
                CNApproveMap.put("currentUser", createdby);
                CNApproveMap.put("fromCreate", true);
                CNApproveMap.put("moduleid", Constants.Acc_Debit_Note_ModuleId);
                CNApproveMap.put(Constants.PAGE_URL, URLUtil.getPageURL(request, loginpageFull));
                boolean isMailApplicable = false;
                approvedlevel = accDebitNoteService.approveDebitNote(debitNote, CNApproveMap, isMailApplicable);
                approvalStatusLevel = (Integer) approvedlevel.get(0);
            }
            if (approvalStatusLevel == 11) {
                jeDataMap.put("pendingapproval", 0);
            } else {
                jeDataMap.put("pendingapproval", 1);
            }
            debitNote.setApprovestatuslevel(approvalStatusLevel);
            doDataMap.put(Constants.Acc_id, debitNote.getID());
            Set<DebitNoteAgainstCustomerGst> podetails = null;
            List rowDetails = saveDebitNoteGstRows(request, companyid, journalEntry, externalCurrencyRate, debitNote);
            podetails = (HashSet) rowDetails.get(0);
            debitNote.setRowsGst(podetails);
            String accountid = "";
            KwlReturnObject kwlResult = null;
            if (cntype == Constants.DebitNoteForOvercharge) {
                kwlResult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), request.getParameter("vendor"));
                Vendor vendor = (Vendor) kwlResult.getEntityList().get(0);
                accountid = vendor.getAccount().getID();
            } else {
                kwlResult = accountingHandlerDAOobj.getObject(Customer.class.getName(), request.getParameter("customer"));
                Customer customer = (Customer) kwlResult.getEntityList().get(0);
                accountid = customer.getAccount().getID();
            }
            jedetails = (HashSet) rowDetails.get(2);
            double totalDNAmt = (Double) rowDetails.get(4);
            double totalDNAmtExludingTax = (Double) rowDetails.get(5);
            double totalDiscountAmt = (Double) rowDetails.get(6);
            JSONObject jedjson = new JSONObject();
            jedjson.put("srno", jedetails.size() + 1);
            jedjson.put(Constants.companyKey, companyid);
            jedjson.put("amount", totalDNAmt);
            jedjson.put("accountid", accountid);
            jedjson.put("debit", true);
            jedjson.put("jeid", jeid);
            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetails.add(jed);
            debitNote.setVendorEntry(jed);
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            if (totalDiscountAmt > 0) {
                String dicountAccount = null;
                if (cntype == Constants.DebitNoteForOvercharge) {
                    dicountAccount = preferences.getDiscountReceived().getID(); //In case of DebitNoteForOvercharge purchase invoice ERP-38665.
                } else {
                    dicountAccount = preferences.getDiscountGiven().getID(); //In case of  DebitNoteForUndercharge sale invoice ERP-38665.
                }
                jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put(Constants.companyKey, companyid);
                jedjson.put("amount", totalDiscountAmt);
                jedjson.put("accountid", dicountAccount);
                jedjson.put("debit", true);
                jedjson.put("jeid", jeid);
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jedetails.add(jed);
            }
            jeDataMap.put("jedetails", jedetails);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeDataMap.put("transactionId", debitNote.getID());
            jeDataMap.put("transactionModuleid", Constants.Acc_Debit_Note_ModuleId);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            
                if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                customrequestParams.put("modulerecid", journalEntry.getID());
                customrequestParams.put("moduleid", Constants.Acc_Debit_Note_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    jeDataMap.put("accjecustomdataref", journalEntry.getID());
                    jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);
                }
            }
            String linkMode = request.getParameter("fromLinkCombo");
            String[] linkNumbers = request.getParameter("linkNumber").split(",");
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("savePurchaseReturn : " + ex.getMessage(), ex);
        }
        returnList.add(oldjeid);
        returnList.add(debitNote);
        returnList.add(approvalStatusLevel);
        returnList.add(debitNoteNumber);
        returnList.add(debitNoteId);
        returnList.add(linkedDocuments);
        returnList.add(unlinkMessage);
        return returnList;
    }
    
    
    public List saveDebitNoteGstRows(HttpServletRequest request, String companyId, JournalEntry je, double externalCurrencyRate,DebitNote debitNote) throws ServiceException, AccountingException, SessionExpiredException, ParseException {
        List returnList = new ArrayList();
        Set rows = new HashSet();
        HashSet cndetails = new HashSet();
        HashSet jedetails = new HashSet();
        HashSet dnTaxEntryDetails = new HashSet();
        double totalDiscountAmt = 0;
        double totalDNAmt = 0;
        double totalDNAmtExludingTax = 0;
        try {

            boolean isNoteAlso = false;
            InvoiceDetail id = null;
            GoodsReceiptDetail grdetail = null;
            int cntype = StringUtil.isNullOrEmpty(request.getParameter("cntype")) ? 1 : Integer.parseInt(request.getParameter("cntype"));
            String debitNoteId = request.getParameter("srid");
            KwlReturnObject comp = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
            Company company = (Company) comp.getEntityList().get(0);
            int countryid = Integer.parseInt(company.getCountry().getID());
            if (!StringUtil.isNullOrEmpty(request.getParameter("isNoteAlso"))) {// isNoteAlso flag will be true if you are creating Sale/Purchase Return with Credit/Debit Note also
                isNoteAlso = Boolean.parseBoolean(request.getParameter("isNoteAlso"));
            }

            String globalTaxID = request.getParameter("taxid");
            double globalTaxPercent = 0;
            if (!StringUtil.isNullOrEmpty(globalTaxID)) {
                globalTaxPercent = StringUtil.isNullOrEmpty(request.getParameter("globalTaxPercent")) ? 0 : Double.parseDouble(request.getParameter("globalTaxPercent"));
            }
            JSONArray jArr = new JSONArray(request.getParameter(Constants.detail));
//
//            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyId);
//            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
//            KwlReturnObject extracap = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyId);
//            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracap.getEntityList().get(0);
           // String gcurrencyid = sessionHandlerImpl.getCurrencyID(request);
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                HashMap<String, Object> dodDataMap = new HashMap<String, Object>();
                
                if(jobj.has("srno")) {
                    dodDataMap.put("srno", jobj.getInt("srno"));
                }
                
                dodDataMap.put(Constants.companyKey, companyId);
                dodDataMap.put("cnId", debitNote.getID());
                dodDataMap.put(Constants.productid, jobj.getString(Constants.productid));
                
                if (jobj.has("priceSource") && jobj.get("priceSource") != null) {
                    dodDataMap.put("priceSource", !StringUtil.isNullOrEmpty(jobj.optString("priceSource")) ? StringUtil.DecodeText(jobj.optString("priceSource")) : "");
                }
                
                String linkMode = request.getParameter("fromLinkCombo");

                dodDataMap.put("description", jobj.getString("description"));
//                if (!StringUtil.isNullOrEmpty(debitNoteId)) {
//                    dodDataMap.put("id", jobj.getString("rowid"));
//                }
                
                dodDataMap.put("partno", jobj.getString("partno"));

                double actquantity = jobj.getDouble("quantity");
                double dquantity = jobj.getDouble("dquantity");
                double baseuomrate = 1;
                if (jobj.has("baseuomrate") && jobj.get("baseuomrate") != null) {
                    baseuomrate = jobj.getDouble("baseuomrate");
                }
                dodDataMap.put("quantity", actquantity);
                dodDataMap.put("returnquantity", dquantity);
                dodDataMap.put("baseuomrate", baseuomrate);
                if (jobj.has("uomid")) {
                    dodDataMap.put("uomid", jobj.getString("uomid"));
                }
                dodDataMap.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(actquantity, baseuomrate, companyId));
                dodDataMap.put("baseuomreturnquantity", authHandler.calculateBaseUOMQuatity(dquantity, baseuomrate, companyId));

                double receivedQty = dquantity*baseuomrate;

                dodDataMap.put("remark", jobj.optString("remark"));
                dodDataMap.put("reason", jobj.optString("reason"));

                String rowtaxid = "";
                if (!StringUtil.isNullOrEmpty(jobj.optString("prtaxid", null)) && jobj.optString("prtaxid").equalsIgnoreCase("None")) {
                    rowtaxid = null;
                } else {
                    rowtaxid = jobj.optString("prtaxid", null);
                }
                if (!StringUtil.isNullOrEmpty(rowtaxid)) {
                    KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), rowtaxid); // (Tax)session.get(Tax.class, taxid);
                    Tax rowtax = (Tax) txresult.getEntityList().get(0);
                    double rowtaxamountFromJS = StringUtil.getDouble(jobj.getString("taxamount"));

                    if (rowtax == null) {
                        throw new AccountingException("The Tax code(s) used in this transaction has been deleted.");//messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
                    } else {
                        dodDataMap.put("prtaxid", rowtaxid);
                        dodDataMap.put("taxamount", rowtaxamountFromJS);
                    }
                }

                int discountispercent = jobj.optInt("discountispercent", 1);
                
                double prdiscount = jobj.optDouble("prdiscount", 0);
                
                if (jobj.has("prdiscount") && jobj.get("prdiscount") != null) {
                    dodDataMap.put("discount", prdiscount);
                }
                if (jobj.has("discountispercent") && jobj.get("discountispercent") != null) {
                    dodDataMap.put("discountispercent", discountispercent);
                }

                if (!StringUtil.isNullOrEmpty(jobj.optString("invstore"))) {
                    dodDataMap.put("invstoreid", jobj.optString("invstore"));
                } else {
                    dodDataMap.put("invstoreid", "");
                }
                if (!StringUtil.isNullOrEmpty(jobj.optString("invlocation"))) {
                    dodDataMap.put("invlocationid", jobj.optString("invlocation"));
                } else {
                    dodDataMap.put("invlocationid", "");
                }

                if (!StringUtil.isNullOrEmpty(linkMode)) {
                    if (linkMode.equalsIgnoreCase("Customer Invoice") || linkMode.equalsIgnoreCase("Sales Invoice")) {
                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(InvoiceDetail.class.getName(), jobj.getString("rowid"));
                        id = (InvoiceDetail) rdresult.getEntityList().get(0);
                        dodDataMap.put("InvoiceDetail", id);
                    }
                    if (linkMode.equalsIgnoreCase("Purchase Invoice")) {
                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(GoodsReceiptDetail.class.getName(), jobj.getString("rowid"));
                        grdetail = (GoodsReceiptDetail) rdresult.getEntityList().get(0);
                        dodDataMap.put("grdetail", grdetail);
                    }
                }

                double unitPrice = 0;
                if (jobj.has("rate")) {
                    dodDataMap.put("rate", jobj.optDouble("rate",0));
                    unitPrice = jobj.optDouble("rate",0);
                }
                if (!StringUtil.isNullOrEmpty(jobj.optString("recTermAmount"))) {
                    dodDataMap.put("recTermAmount", jobj.optString("recTermAmount"));
                }
                
                KwlReturnObject result = accDebitNoteobj.saveDebitNoteGstDetails(dodDataMap);
                DebitNoteAgainstCustomerGst row = (DebitNoteAgainstCustomerGst) result.getEntityList().get(0);

                
                // Create Debit Nore
                    String purchase_accid = "";
                    KwlReturnObject proresult = accountingHandlerDAOobj.getObject(Product.class.getName(), jobj.getString(Constants.productid));
                    Product product = (Product) proresult.getEntityList().get(0);
                    double discountVal = 0d;
                    double totalAmt = unitPrice * receivedQty;
                    if (discountispercent == 1) {
                        discountVal = totalAmt * prdiscount / 100;
                    } else {
                        discountVal = prdiscount;
                    }
                    totalDiscountAmt += discountVal;
                    
                    double rowtaxamount = 0d;
                    double amountExcludingTax = 0;
                    Tax rowtax = null;
                    double dnRowAmount=jobj.optDouble("amount", 0);
                    if(!StringUtil.isNullOrEmpty(globalTaxID)){//when tax given at global Level
                        KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), globalTaxID);
                        rowtax = (Tax) txresult.getEntityList().get(0);
                        amountExcludingTax = dnRowAmount;// it comes excluding tax
                        rowtaxamount= authHandler.round(dnRowAmount * (globalTaxPercent/100), companyId);
                        totalDNAmt += amountExcludingTax+rowtaxamount;
                        totalDNAmtExludingTax += amountExcludingTax; 
                    } else if(!StringUtil.isNullOrEmpty(rowtaxid)) {
                        KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), rowtaxid);
                        rowtax = (Tax) txresult.getEntityList().get(0);
                        totalDNAmt += dnRowAmount;
                        rowtaxamount = jobj.optDouble("taxamount", 0);
                        amountExcludingTax = dnRowAmount - rowtaxamount;
                        totalDNAmtExludingTax += amountExcludingTax;
                    } else {// No tax applied
                        totalDNAmt += dnRowAmount;
                        rowtaxamount = 0;
                        amountExcludingTax = dnRowAmount ;
                        totalDNAmtExludingTax += dnRowAmount;
                    }
                    if(id != null && countryid == Constants.indian_country_id && id.getSalesJED() != null){//id is Goodsreceiptdetailid
                        purchase_accid = id.getSalesJED().getAccount().getID();//account used in goodsreceiptdetail
                    }else if(cntype == Constants.DebitNoteForOvercharge){
                        purchase_accid = product.getPurchaseAccount().getID();//Debit Note for Overcharge.
                    }else{
                        purchase_accid = product.getSalesAccount().getID();
                    }
                    DebitNoteTaxEntry taxEntry = new DebitNoteTaxEntry();
                    String DebitNoteTaxID = StringUtil.generateUUID();
                    taxEntry.setID(DebitNoteTaxID);
                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put(Constants.companyKey, company.getCompanyID());
                    jedjson.put("amount",amountExcludingTax+discountVal);
                    jedjson.put("accountid", purchase_accid);
                    jedjson.put("debit", false);
                    jedjson.put("jeid", je.getID());
                    jedjson.put("description", "");
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    JournalEntryDetail jedTotal = (JournalEntryDetail) jedresult.getEntityList().get(0);//ERP-17888                    
                    jedetails.add(jed);
                    row.setJedid(jed);
                    /**
                     * Save Debit Note Overcharge/Undercharge line level terms details for US Country
                     */
                    if (countryid == Constants.USA_country_id && jobj.has("LineTermdetails") && !StringUtil.isNullOrEmpty((String) jobj.optString("LineTermdetails"))) {
                        JSONArray termsArray = new JSONArray(StringUtil.DecodeText((String) jobj.optString("LineTermdetails")));
                        double termAmount = 0.0;
                        for (int j = 0; j < termsArray.length(); j++) {
                            HashMap<String, Object> DNDetailsTermsMap = new HashMap<>();
                            JSONObject termObject = termsArray.getJSONObject(j);
                            if (termObject.has("termid")) {
                                DNDetailsTermsMap.put("term", termObject.get("termid"));
                            }
                            if (termObject.has("termamount")) {
                                DNDetailsTermsMap.put("termamount", termObject.optDouble("termamount", 0.0));
                                termAmount += termObject.optDouble("termamount", 0.0);
                            }
                            if (termObject.has("termpercentage")) {
                                DNDetailsTermsMap.put("termpercentage", termObject.get("termpercentage"));
                            }
                            if (termObject.has("purchasevalueorsalevalue")) {
                                DNDetailsTermsMap.put("purchasevalueorsalevalue", termObject.get("purchasevalueorsalevalue"));
                            }
                            if (termObject.has("deductionorabatementpercent")) {
                                DNDetailsTermsMap.put("deductionorabatementpercent", termObject.get("deductionorabatementpercent"));
                            }
                            if (termObject.has("assessablevalue")) {
                                DNDetailsTermsMap.put("assessablevalue", termObject.get("assessablevalue"));
                            }
                            if (termObject.has("taxtype") && !StringUtil.isNullOrEmpty(termObject.getString("taxtype"))) {
                                DNDetailsTermsMap.put("taxtype", termObject.getInt("taxtype"));
                                if (termObject.has("taxvalue") && !StringUtil.isNullOrEmpty(termObject.getString("taxvalue"))) {
                                    if (termObject.getInt("taxtype") == 0) { // If Flat
                                        DNDetailsTermsMap.put("termamount", termObject.getDouble("taxvalue"));
                                    } else { // Else Percentage
                                        DNDetailsTermsMap.put("termpercentage", termObject.getDouble("taxvalue"));
                                    }
                                }
                            }
                            if (termObject.has("id")) {
                                DNDetailsTermsMap.put("id", termObject.get("id"));
                            }
                            DNDetailsTermsMap.put("debitnotedetail", row.getID());
                            DNDetailsTermsMap.put("isDefault", termObject.optString("isDefault", "false"));
                            DNDetailsTermsMap.put("productentitytermid", termObject.optString("productentitytermid"));
                            DNDetailsTermsMap.put("userid", sessionHandlerImpl.getUserid(request));
                            DNDetailsTermsMap.put("product", termObject.opt("productid"));
                            DNDetailsTermsMap.put("createdOn", new Date());
                            KwlReturnObject dnDetailTermMapResult = accDebitNoteobj.saveDebitNoteDetailTermMap(DNDetailsTermsMap);
                            DebitNoteDetailTermMap debitNoteDetailTermMap = dnDetailTermMapResult.getEntityList() != null && dnDetailTermMapResult.getEntityList().size() > 0 ? (DebitNoteDetailTermMap) dnDetailTermMapResult.getEntityList().get(0) : null;
                            if (debitNoteDetailTermMap != null && debitNoteDetailTermMap.getEntitybasedLineLevelTermRate() != null && debitNoteDetailTermMap.getEntitybasedLineLevelTermRate().getLineLevelTerms() != null && debitNoteDetailTermMap.getEntitybasedLineLevelTermRate().getLineLevelTerms().getAccount() != null) {
                                jedjson = new JSONObject();
                                jedjson.put("srno", jedetails.size() + 1);
                                jedjson.put("companyid", company.getCompanyID());
                                jedjson.put("amount", termObject.optDouble("termamount", 0.0));
                                jedjson.put("accountid", debitNoteDetailTermMap.getEntitybasedLineLevelTermRate().getLineLevelTerms().getAccount().getID());
                                jedjson.put("debit", false);
                                jedjson.put("jeid", je.getID());
                                jedjson.put("description", jobj.optString("description"));
                                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                jedetails.add(jed);
                            }
                            totalDNAmt += termObject.optDouble("termamount", 0.0);
                        }
                        row.setRowTaxAmount(termAmount);
                    }
                    // Add Custom fields details 
                if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", ""))) {
                    JSONArray jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
                    HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                    customrequestParams.put("customarray", jcustomarray);
                    customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                    customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                    customrequestParams.put("modulerecid", jed.getID());
                    customrequestParams.put("recdetailId", taxEntry.getID());
                    customrequestParams.put("moduleid", Constants.Acc_Debit_Note_ModuleId);
                    customrequestParams.put("companyid", company.getCompanyID());
                    customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                    KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        JSONObject tempjedjson = new JSONObject();
                        tempjedjson.put("accjedetailcustomdata", jed.getID());
                        tempjedjson.put("jedid", jed.getID());
                        jedresult = accJournalEntryobj.updateJournalEntryDetails(tempjedjson);
                    }
                }
//                    
                    String rowTaxJeId = "";
                    if (rowtax != null) {
                        jedjson = new JSONObject();
                        jedjson.put("srno", jedetails.size() + 1);
                        jedjson.put(Constants.companyKey, company.getCompanyID());
                        jedjson.put("amount", authHandler.formattedAmount(rowtaxamount, companyId));
                        jedjson.put("accountid", rowtax.getAccount().getID());
                        jedjson.put("debit", false);
                        jedjson.put("jeid", je.getID());
                        jedjson.put("description", "");
                        jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jedetails.add(jed);
                        row.setGstJED(jed);
                        rowTaxJeId = jed.getID();
                    }
                    KwlReturnObject accountresult = accountingHandlerDAOobj.getObject(Account.class.getName(), purchase_accid);
                    Account account = (Account) accountresult.getEntityList().get(0);

                    taxEntry.setAccount(account);
                    taxEntry.setAmount(amountExcludingTax);
                    taxEntry.setCompany(company);
                    if (!StringUtil.isNullOrEmpty(jobj.optString("reason", ""))) {
                        KwlReturnObject reasonresult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), jobj.optString("reason", ""));
                        MasterItem reason = (MasterItem) reasonresult.getEntityList().get(0);
                        taxEntry.setReason(reason);
                    }
                    taxEntry.setDescription("");
                    taxEntry.setIsForDetailsAccount(true);
                    taxEntry.setDebitForMultiCNDN(false);
                    taxEntry.setTax(rowtax);
                    taxEntry.setTaxJedId(rowTaxJeId);
                    taxEntry.setTaxamount(rowtaxamount);      
                    taxEntry.setTotalJED(jedTotal); //ERP-17888
                    dnTaxEntryDetails.add(taxEntry);
//                }
                rows.add(row);
                
            }
            returnList.add(rows);
            returnList.add(cndetails);
            returnList.add(jedetails);
            returnList.add(dnTaxEntryDetails);
            returnList.add(totalDNAmt);
            returnList.add(totalDNAmtExludingTax);
            returnList.add(totalDiscountAmt);
        }  catch (JSONException ex) {
            throw ServiceException.FAILURE("savePurchaseReturnRows : " + ex.getMessage(), ex);
        }
        return returnList;
    }
    public List saveDebitNote(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException {
        DebitNote debitnote = null;
        List list = new ArrayList();
        KwlReturnObject result;
        List ll = new ArrayList();
        String oldjeid = "";
        String auditMsg = "", auditID = "";
        try {
            boolean reloadInventory = false;//Flag used to reload inventory on Client Side If CN type equals to "Return" or "Defective"
            double externalCurrencyRate = StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            String sequenceformat = request.getParameter("sequenceformat");
            String debitNoteId = request.getParameter("noteid");
            String jeentryNumber = "";
            boolean jeautogenflag = false;
            String jeIntegerPart = "";
            String jeDatePrefix = "";
            String jeDateAfterPrefix = "";
            String jeDateSuffix = "";
            String jeSeqFormatId = "";
            String addressID="";
            boolean isEditNote = false;
            String entryNumber = request.getParameter("number");
            DateFormat df = authHandler.getDateOnlyFormat();
            HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
            GlobalParams.put("companyid", companyid);
            GlobalParams.put("gcurrencyid", currencyid);
            GlobalParams.put("dateformat", df);
            String customfield = request.getParameter("customfield");
            boolean otherwise = request.getParameter("otherwise") != null;
            boolean isEditToApprove = StringUtil.isNullOrEmpty(request.getParameter("isEditToApprove"))?false:Boolean.parseBoolean(request.getParameter("isEditToApprove"));
            int approvalStatusLevel = 11;
            List mailParams=Collections.EMPTY_LIST;
            
            String accountid = request.getParameter("accountid") != null ? request.getParameter("accountid") : "";
            Boolean isCopy = request.getParameter("isCopy") != null ? Boolean.parseBoolean(request.getParameter("isCopy")) : false;
            String oldNoteId = request.getParameter("billid") != null ? request.getParameter("billid") : "";
            
            Date creationDate = df.parse(request.getParameter("creationdate"));
            String createdby = sessionHandlerImpl.getUserid(request);
            String modifiedby = sessionHandlerImpl.getUserid(request);
            String userFullName = sessionHandlerImpl.getUserFullName(request);
            long createdon = System.currentTimeMillis();
            long updatedon = System.currentTimeMillis();
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);

            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);

            entryNumber = request.getParameter("number");
            currencyid = (request.getParameter("currencyid") == null ? kwlcurrency.getCurrencyID() : request.getParameter("currencyid"));
            curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

            HashMap<String, Object> dnhm = new HashMap<String, Object>();
            int cntype = StringUtil.isNullOrEmpty(request.getParameter("cntype")) ? 1 : Integer.parseInt(request.getParameter("cntype"));
            if (!StringUtil.isNullOrEmpty(debitNoteId)) {
                //In case of edit if format is NA then entry number can be changed so need to check duplicate
                result = accDebitNoteobj.getDNFromNoteNoAndId(entryNumber, companyid, debitNoteId);
                if (result.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
                    throw new AccountingException(messageSource.getMessage("acc.field.Debitnotenumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                }
                isEditNote = true;
                KwlReturnObject dnObj = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), debitNoteId);
                debitnote = (DebitNote) dnObj.getEntityList().get(0);
                oldjeid = debitnote.getJournalEntry().getID();
                /*
                 * Taking original account in edit and copy Debit note. Refer
                 * SDP-7867
                 */
                if (debitnote.getVendorEntry() != null) {
                    accountid = debitnote.getVendorEntry().getAccount().getID();
                }

                approvalStatusLevel = debitnote.getApprovestatuslevel();
                JournalEntry jetemp = debitnote.getJournalEntry();
                if (jetemp != null) {
                    jeentryNumber = jetemp.getEntryNumber(); //preserving these data to generate same JE number in edit case                    
                    jeautogenflag = jetemp.isAutoGenerated();
                    jeSeqFormatId = jetemp.getSeqformat() == null ? "" : jetemp.getSeqformat().getID();
                    jeIntegerPart = String.valueOf(jetemp.getSeqnumber());
                    jeDatePrefix = jetemp.getDatePreffixValue();
                    jeDateAfterPrefix = jetemp.getDateAfterPreffixValue();
                    jeDateSuffix = jetemp.getDateSuffixValue();
                }
                if (debitnote != null) {
                    boolean isdebitNoteWithSalesReturn = (debitnote.getPurchaseReturn()!= null && debitnote.getPurchaseReturn().isIsNoteAlso()) ? true : false;
                    if (!isEditToApprove && !isdebitNoteWithSalesReturn) {//for case editToApprove doesnot need to update invoice amount because we did not reduce invoice amount for pending DN
                        accDebitNoteService.updateOpeningInvoiceAmountDue(debitnote.getID(), companyid);
                    }
                    
                    // delete foreign gain loss JE
                    List resultJe = accDebitNoteobj.getForeignGainLossJE(debitnote.getID(), companyid);
                    if (resultJe.size() > 0 && resultJe.get(0) != null) {
                        Iterator itr = resultJe.iterator();
                        while (itr.hasNext()) {
                            Object object = itr.next();
                            String jeid = object != null ? object.toString() : "";
                            accDebitNoteService.deleteJEArray(jeid, companyid);
                        }
                    }
                }
                
                 //taking address id in edit case
                addressID=debitnote.getBillingShippingAddresses()!=null?debitnote.getBillingShippingAddresses().getID():"";
                
                result = accDebitNoteobj.deleteDebitNoteDetails(debitnote.getID(), companyid);
                if (company.getCountry() != null && company.getCountry().getID().equals(Constants.INDIA_COUNTRYID) && debitnote.getDnTaxEntryDetails() != null) {
                    /**
                     * delete DebitNoteDetailTermMap mapping while editing
                     * DN.
                     */
                    String ids = "";
                    for (DebitNoteTaxEntry dnTaxEntry : debitnote.getDnTaxEntryDetails()) {
                        ids += "'" + dnTaxEntry.getID() + "',";
                    }
                    if(!StringUtil.isNullOrEmpty(ids)){
                        accDebitNoteobj.deleteDebitNoteDetailTermMap(ids.substring(0, ids.length() - 1));
                    }
                    accDebitNoteobj.deleteGstTaxClassDetails(debitNoteId);
                }
                result = accDebitNoteobj.deleteDebitTaxDetails(debitnote.getID(), companyid);

                //Delete old entries and insert new entries again from optimized table in edit case.
                accJournalEntryobj.deleteOnEditAccountJEs_optimized(oldjeid);
                deleteJEDetailsCustomData(oldjeid);
                dnhm.put("dnid", debitNoteId);
                /* Deleting linking information of Debit while editing*/
                accDebitNoteobj.deleteLinkingInformationOfDN(dnhm);
                if (company.getCountry() != null && company.getCountry().getID().equals(Constants.INDIA_COUNTRYID)) {
                    JSONObject json = new JSONObject();
                    json.put("debitnoteid", debitNoteId);
                    accDebitNoteobj.deleteDebitNoteInvoiceMappingInfo(json);
                }
                
            } else {
                if (isCopy) {
                    /*
                     * Taking original account in edit and copy Debit note.
                     * Refer SDP-7867
                     */
                    KwlReturnObject obj = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), oldNoteId);
                    debitnote = (DebitNote) obj.getEntityList().get(0);
                    //While copying debit note if customer or vendor is changed then take account of new Customer/Vendor.
                    boolean isCustomerChanged = false;
                    if (!StringUtil.isNullOrEmpty(request.getParameter("accid"))) {
                        if (cntype == 4 && debitnote.getCustomer() != null) {//DN against customer
                            isCustomerChanged = !(debitnote.getCustomer().getID().equals(request.getParameter("accid")));
                        } else if (debitnote.getVendor() != null) {
                            isCustomerChanged = !(debitnote.getVendor().getID().equals(request.getParameter("accid")));
                        }
                    }

                    if (debitnote != null && debitnote.getVendorEntry() != null && !isCustomerChanged) {
                        accountid = debitnote.getVendorEntry().getAccount().getID();
                    }
                }
                    dnhm.put("autogenerated", !sequenceformat.equals("NA")?true:false);
                    dnhm.put("oldRecord", false);

                Long seqNumber = null;
                result = accDebitNoteobj.getDNSequenceNo(companyid, creationDate);
                List li = result.getEntityList();
                if (!li.isEmpty()) {
                    seqNumber = (Long) li.get(0);
                }
                dnhm.put("sequence", seqNumber.intValue());

            }
            if (sequenceformat.equals("NA") || !StringUtil.isNullOrEmpty(debitNoteId)) {
                dnhm.put("entrynumber", entryNumber);
            } else {
                dnhm.put("entrynumber", "");
            }
            if (sequenceformat.equals("NA")) {//In case of NA checks wheather this number can also be generated by a sequence format or not
                List resultList = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Debit_Note_ModuleId, entryNumber, companyid);
                if (!resultList.isEmpty()) {
                    boolean isvalidEntryNumber = (Boolean) resultList.get(0);
                    String formatName = (String) resultList.get(1);
                    if (!isvalidEntryNumber) {
                        throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, RequestContextUtils.getLocale(request))+" <b>"+entryNumber+"</b> "+messageSource.getMessage("acc.common.belongsto", null, RequestContextUtils.getLocale(request))+" <b>"+formatName+"</b>. "+messageSource.getMessage("acc.common.plselectseqformat", null, RequestContextUtils.getLocale(request))+" <b>"+formatName+"</b> "+messageSource.getMessage("acc.common.insteadof", null, RequestContextUtils.getLocale(request)));
                    }
                }
            }
            String costCenterId = !StringUtil.isNullOrEmpty(request.getParameter("costCenterId"))?request.getParameter("costCenterId"):"";
            dnhm.put("memo", request.getParameter("memo"));
            dnhm.put("companyid", companyid);
            dnhm.put("currencyid", currencyid);
            dnhm.put("createdby", createdby);
            dnhm.put("modifiedby", modifiedby);
            dnhm.put("createdon", createdon);
            dnhm.put("updatedon", updatedon);
            dnhm.put("costcenter", costCenterId);
            dnhm.put("includingGST", Boolean.parseBoolean(request.getParameter("includingGST")));
            dnhm.put("externalCurrencyRate", externalCurrencyRate);
            dnhm.put("approvestatuslevel", approvalStatusLevel);
            
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.MVATTRANSACTIONNO))) {
                dnhm.put(Constants.MVATTRANSACTIONNO, request.getParameter(Constants.MVATTRANSACTIONNO));
            }else{
                dnhm.put(Constants.MVATTRANSACTIONNO, "");
            }
           
            String supplierInvoiceNo = "";//SDP-4510
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.SUPPLIERINVOICENO))) {
                supplierInvoiceNo = request.getParameter(Constants.SUPPLIERINVOICENO);
            }
            dnhm.put(Constants.SUPPLIERINVOICENO, supplierInvoiceNo);
            Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
              if (StringUtil.isNullOrEmpty(oldjeid)) {
                jeDataMap.put("entrynumber", "");
            } else {
                jeDataMap.put("entrynumber", jeentryNumber);
                jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                jeDataMap.put(Constants.SEQNUMBER, jeIntegerPart);
                jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
                jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
                jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
            }
            
            jeDataMap.put("autogenerated",true);
           
            jeDataMap.put("entrydate", creationDate);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeDataMap.put(Constants.Checklocktransactiondate, request.getParameter("creationdate"));//ERP-16800-Without parsing date
            jeDataMap.put("companyid", company.getCompanyID());
            jeDataMap.put("memo", request.getParameter("memo"));
            jeDataMap.put("createdby", createdby);
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                jeDataMap.put("costcenterid", costCenterId);
            }
            jeDataMap.put("currencyid", currencyid);
            HashSet<JournalEntryDetail> jedetails = new HashSet();
            dnhm.put(Constants.Checklocktransactiondate, request.getParameter("creationdate"));
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            String jeid = journalEntry.getID();
            jeDataMap.put("jeid", jeid);
            dnhm.put("journalentryid", jeid);

            List DNlist = new ArrayList();
            double dnamount = 0.0;
            double dnamountdue = 0.0;
//            Double totalAmount = saveDebitNoteRows(session, request, debitnote, company, hs, preferences,kwlcurrency);
            // saveDebitNoteDiscountRows(session, request, debitnote, company);
            if (cntype == 4) {//CN against vendor
                dnhm.put("customerid", request.getParameter("accid"));
            } else {
                dnhm.put("vendorid", request.getParameter("accid"));
            }
            request.setAttribute("customerAccountId",accountid);
            
            String invoiceids = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("invoiceids"))) {
                invoiceids = request.getParameter("invoiceids");
            }
            String invoiceIdsArrary[] = invoiceids.split(",");
            JSONArray invoicedetails = null;
            if (!StringUtil.isNullOrEmpty(request.getParameter("invoicedetails"))) {
                invoicedetails = new JSONArray(request.getParameter("invoicedetails"));
            }
            Map<String, Double> invoiceIdAmountDueMap = new HashMap<>();//to calculate invoiceamountdue
            Map<String, String> invoiceIdNoMap = new HashMap<>();//to calculate invoiceamountdue
            if (cntype != Constants.DebitNoteForOvercharge) {//No need to check knockoff case for DN for Overcharge.
                for (int i = 0; i < invoiceIdsArrary.length; i++) {
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invoiceIdsArrary[i]);
                    GoodsReceipt gReceipt = (GoodsReceipt) objItr.getEntityList().get(0);

                    if (gReceipt != null && invoicedetails != null) {
                        double invoiceAmoutDue = 0; // Opening transaction record
                        double invoiceAmoutDueBase = 0;
                        double amountReceivedBase = 0;
                        if (gReceipt.isIsOpeningBalenceInvoice() && !gReceipt.isNormalInvoice()) {
                            invoiceAmoutDue = gReceipt.getOpeningBalanceAmountDue();
                        } else {
                            invoiceAmoutDue = gReceipt.getInvoiceamountdue();
                        }
                        JSONObject invoiceJobj = invoicedetails.getJSONObject(i);
                        double amountReceived = invoiceJobj.getDouble("invamount");           //amount of DN 
                        HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                        String gCurrencyid = sessionHandlerImpl.getCurrencyID(request);
                        filterRequestParams.put(Constants.companyKey, companyid);
                        filterRequestParams.put(Constants.globalCurrencyKey, currencyid);
                        KwlReturnObject bAmt = null;
                        if (!gReceipt.isIsOpeningBalenceInvoice()) {
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, invoiceAmoutDue, gCurrencyid, gReceipt.getCreationDate(), gReceipt.getJournalEntry().getExternalCurrencyRate());
                        } else {
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, invoiceAmoutDue, gCurrencyid, gReceipt.getCreationDate(), gReceipt.getExchangeRateForOpeningTransaction());
                        }
                        KwlReturnObject bAmtRec = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, amountReceived, gCurrencyid, creationDate, externalCurrencyRate);
                        invoiceAmoutDueBase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                        amountReceivedBase = authHandler.round((Double) bAmtRec.getEntityList().get(0), companyid);
                        double diffDueReceive = 0.0;
                        if (invoiceIdAmountDueMap.containsKey(invoiceIdsArrary[i]) && invoiceIdAmountDueMap.get(invoiceIdsArrary[i]) != null) {
                            Double tempinvamtdue = (Double) invoiceIdAmountDueMap.get(invoiceIdsArrary[i]);
                            diffDueReceive = tempinvamtdue - amountReceivedBase;
                        } else {
                            diffDueReceive = invoiceAmoutDueBase - amountReceivedBase;
                        }
                        invoiceIdAmountDueMap.put(invoiceIdsArrary[i], diffDueReceive);
                        invoiceIdNoMap.put(invoiceIdsArrary[i], gReceipt.getGoodsReceiptNumber());
                    }
                }
                if (!invoiceIdAmountDueMap.isEmpty() && invoiceIdAmountDueMap != null) {
                    StringBuilder invoiceNoBuildString = new StringBuilder();
                    for (Map.Entry<String, Double> extraColsEntry : invoiceIdAmountDueMap.entrySet()) {
                        if (extraColsEntry.getValue() < 0) {
                            if (extraColsEntry.getValue() < 0.0 || extraColsEntry.getValue() < 0) {
                                String invoicenumber = invoiceIdNoMap.get(extraColsEntry.getKey());
                                if (invoiceNoBuildString.length() > 0) {
                                    invoiceNoBuildString.append(",");
                                }
                                invoiceNoBuildString.append(invoicenumber);
                            }
                        }
                    }
                    if (invoiceNoBuildString.length() > 0) {
                        String invoicenos = invoiceNoBuildString.toString();
                        if (invoicenos.contains(",")) {
                            invoicenos = invoicenos.substring(invoicenos.lastIndexOf(","));
                        }
                        throw ServiceException.FAILURE("Selected Invoice(s) "+ invoicenos +" "+messageSource.getMessage("acc.field.alreadyknock_off_Invoice", null, RequestContextUtils.getLocale(request)), "", false);
                    }
                }
            }
            
            if (otherwise && cntype == 2) {//Debit note otherwise
                dnhm.put("otherwise", true);
                dnhm.put("openflag", true);
                dnamount = Double.parseDouble(request.getParameter("amount"));
                dnamountdue = dnamount;
                dnhm.put("dnamount", dnamount);
                KwlReturnObject baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, dnamount, currencyid, creationDate, externalCurrencyRate);
                double dnamountinbase = (Double) baseAmount.getEntityList().get(0);
                dnhm.put("dnamountinbase", authHandler.round(dnamountinbase, companyid));
                dnhm.put("dnamountdue", dnamountdue);
                dnhm.put("cntype", 2);
                DNlist = saveDebitNoteRowsOW(GlobalParams, request, company, currency, journalEntry, preferences, externalCurrencyRate);
            } else if (cntype == 3) {//Debit Note against Paid vendor Invoice
                dnhm.put("otherwise", true);
                dnhm.put("openflag", true);
                dnhm.put("cntype", 1);
                DNlist = saveDebitNoteRows1(GlobalParams, request, company, currency, journalEntry, preferences, externalCurrencyRate);
                dnhm.put("dnamount", (Double) DNlist.get(5));
                KwlReturnObject baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, (Double) DNlist.get(5), currencyid, creationDate, externalCurrencyRate);
                double dnamountinbase = (Double) baseAmount.getEntityList().get(0);
                dnhm.put("dnamountinbase", authHandler.round(dnamountinbase, companyid));
                dnhm.put("dnamountdue", (Double) DNlist.get(5));
                dnhm.put("cntype", 3);
            } else if (cntype == 4) {//Debit note otherwise
                dnhm.put("otherwise", true);
                dnhm.put("openflag", true);
                dnamount = Double.parseDouble(request.getParameter("amount"));
                dnamountdue = dnamount;
                dnhm.put("dnamount", dnamount);
                KwlReturnObject baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, dnamount, currencyid, creationDate, externalCurrencyRate);
                double dnamountinbase = (Double) baseAmount.getEntityList().get(0);
                dnhm.put("dnamountinbase", authHandler.round(dnamountinbase, companyid));
                dnhm.put("dnamountdue", dnamountdue);
                dnhm.put("cntype", 4);
                DNlist = saveDebitNoteRowsOW(GlobalParams, request, company, currency, journalEntry, preferences, externalCurrencyRate);
            } else {
                dnhm.put("otherwise", true);
                dnhm.put("openflag", true);
                dnamount = Double.parseDouble(request.getParameter("amount"));
                dnamountdue = dnamount;
                dnhm.put("dnamount", dnamount);
                KwlReturnObject baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, dnamount, currencyid, creationDate, externalCurrencyRate);
                double dnamountinbase = (Double) baseAmount.getEntityList().get(0);
                dnhm.put("dnamountinbase", authHandler.round(dnamountinbase, companyid));
                dnhm.put("dnamountdue", dnamountdue);
                dnhm.put("cntype", 1);
//                DNlist = saveDebitNoteRows1(GlobalParams, request, company, currency, journalEntry, preferences,externalCurrencyRate);
                DNlist = saveDebitNoteRowsOW(GlobalParams, request, company, currency, journalEntry, preferences, externalCurrencyRate);
            }
            // List DNlist = saveDebitNoteRows1(GlobalParams, request, company, currency, journalEntry, preferences,externalCurrencyRate);
            Double totalAmount = (Double) DNlist.get(0);
            Double discAccAmount = (Double) DNlist.get(1);
            HashSet<DebitNoteDetail> dndetails = (HashSet<DebitNoteDetail>) DNlist.get(2);
            HashSet<DebitNoteTaxEntry> dnTaxEntryDetails = (HashSet<DebitNoteTaxEntry>) DNlist.get(5);
            jedetails = (HashSet<JournalEntryDetail>) DNlist.get(3);
            reloadInventory = (Boolean) DNlist.get(4);
            /*
             * If invoice terms applied then add mapping in against invoice
             */
            double termTotalAmount = 0;
            HashMap<String, Double> termAcc = new HashMap<String, Double>();
            String dnTerms = request.getParameter("invoicetermsmap");
            if (!StringUtil.isNullOrEmpty(dnTerms)) {
                JSONArray termsArr = new JSONArray(dnTerms);
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
            jedjson.put("accountid", accountid);
            jedjson.put("debit", true);
            jedjson.put("jeid", jeid);
            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetails.add(jed);
            
            dnhm.put("accountId", jed.getAccount().getID());
            dnhm.put("vendorentry", jed.getID());
            if (termAcc.size() > 0) {
                for (Map.Entry<String, Double> entry : termAcc.entrySet()) {
                    jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", entry.getValue() > 0 ? entry.getValue() : (entry.getValue() * (-1)));
                    jedjson.put("accountid", entry.getKey());
                    jedjson.put("debit", entry.getValue() > 0 ? false : true);
                    jedjson.put("jeid", jeid);
                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                }
            }

//            JournalEntry journalEntry = CompanyHandler.makeJournalEntry(session, company.getCompanyID(), AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")),
//            request.getParameter("memo"), "JE" + debitnote.getDebitNoteNumber(),currency.getCurrencyID(), hs,request);
//            debitnote.setJournalEntry(journalEntry);

//            jeDataMap.put("jedetails", jedetails);
//            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
//            jeDataMap.put("transactionId", debitnote.getID());
//            jeDataMap.put("transactionModuleid", Constants.Acc_Debit_Note_ModuleId);
//            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
//            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            
            //**********Debit Node address related code start**************
            
            boolean isDefaultAddress = request.getParameter("defaultAdress") != null ? Boolean.parseBoolean(request.getParameter("defaultAdress")) : false;
            Map<String, Object> addressParams = Collections.EMPTY_MAP;
            if (isDefaultAddress) { //defautladdress came true only when user create a new CN without saving any address from address window.customer/vendor addresses taken default 
                if(cntype == 4){//Credit Note against customer
                    addressParams = AccountingAddressManager.getDefaultCustomerAddressParams(request.getParameter("accid"), companyid, accountingHandlerDAOobj);
                } else {
                    addressParams = AccountingAddressManager.getDefaultVendorAddressParams(request.getParameter("accid"), companyid, accountingHandlerDAOobj);
                }
            } else {
                if(cntype == 4){//Credit Note against customer
                    addressParams = AccountingAddressManager.getAddressParams(request, false);
                } else {
                    addressParams = AccountingAddressManager.getAddressParams(request, true);
                }
            }

            if (!StringUtil.isNullOrEmpty(addressID) ) { //If Edit case then updating existing DN address 
                addressParams.put("id", addressID);
            }
            KwlReturnObject addressresult = accountingHandlerDAOobj.saveAddressDetail(addressParams, companyid);
            BillingShippingAddresses bsa = (BillingShippingAddresses) addressresult.getEntityList().get(0);
            dnhm.put("billshipAddressid", bsa.getID());
            dnhm.put("creationDate", creationDate);
      
            //*************Debit Node address related code end*************

            if (isEditNote) {
                result = accDebitNoteobj.updateDebitNote(dnhm);
               // auditID = AuditAction.DABIT_NOTE_MODIFIED;
               // auditMsg = "updated";
            } else {
                result = accDebitNoteobj.addDebitNote(dnhm);
               // auditID = AuditAction.DABIT_NOTE_CREATED;
               // auditMsg = "added";
            }

            debitnote = (DebitNote) result.getEntityList().get(0);
                        /**
             * Save GST History Customer/Vendor data.
             */
            if (debitnote.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                JSONObject paramJobj=new JSONObject();
                paramJobj.put("CustomerVendorTypeId", request.getParameter("CustomerVendorTypeId"));
                paramJobj.put("GSTINRegistrationTypeId", request.getParameter("GSTINRegistrationTypeId"));
                paramJobj.put("gstdochistoryid", request.getParameter("gstdochistoryid"));
                paramJobj.put("gstin", request.getParameter("gstin"));
                paramJobj.put("docid", debitnote.getID());
                paramJobj.put("moduleid", Constants.Acc_Debit_Note_ModuleId);
                fieldDataManagercntrl.createRequestMapToSaveDocHistory(paramJobj);
            }
                       
            /*******************Debit Note Multilevel Approval Rule Related Code  Start*************************/
            double totalAmountInBase = 0;
            KwlReturnObject baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, totalAmount, currencyid, creationDate, externalCurrencyRate);
            totalAmountInBase = (Double) baseAmount.getEntityList().get(0);
            HashMap<String, Object> CNApproveMap = new HashMap<String, Object>();
            CNApproveMap.put("companyid", sessionHandlerImpl.getCompanyid(request));
            List approvedlevel = null;
            if (isEditToApprove) {
                CNApproveMap.put("billid", debitnote.getID());
                CNApproveMap.put("userid", createdby);
                CNApproveMap.put("remark", "");
                CNApproveMap.put("userName", userFullName);
                CNApproveMap.put("level", debitnote.getApprovestatuslevel());
                CNApproveMap.put("amount", String.valueOf(authHandler.round(totalAmountInBase, companyid)));
                CNApproveMap.put("isEditToApprove", isEditToApprove);
                List list1 = accDebitNoteService.approvePendingDebitNote(CNApproveMap);
                approvalStatusLevel=11;
                jeDataMap.put(Constants.SEQFORMAT, journalEntry.getSeqformat()!=null?journalEntry.getSeqformat().getID():null);
                jeDataMap.put(Constants.SEQNUMBER, journalEntry.getSeqnumber());
                jeDataMap.put(Constants.DATEPREFIX, journalEntry.getDatePreffixValue());
                jeDataMap.put(Constants.DATEAFTERPREFIX, journalEntry.getDateAfterPreffixValue());
                jeDataMap.put(Constants.DATESUFFIX, journalEntry.getDateSuffixValue());
                jeDataMap.put("entrynumber", journalEntry.getEntryNumber());
            } else {
                CNApproveMap.put("level", 0);//Initialy it will be 0
                CNApproveMap.put("totalAmount", String.valueOf(authHandler.round(totalAmountInBase, companyid)));
                CNApproveMap.put("currentUser", createdby);
                CNApproveMap.put("fromCreate", true);
                CNApproveMap.put("moduleid", Constants.Acc_Debit_Note_ModuleId);
                CNApproveMap.put(Constants.PAGE_URL, URLUtil.getPageURL(request, loginpageFull));
                boolean isMailApplicable = false;
                approvedlevel = accDebitNoteService.approveDebitNote(debitnote, CNApproveMap, isMailApplicable);
                approvalStatusLevel = (Integer) approvedlevel.get(0);
                mailParams = (List) approvedlevel.get(1);
            }
            
            dnhm.put("approvestatuslevel", approvalStatusLevel);
            if (approvalStatusLevel == 11) {
                jeDataMap.put("pendingapproval", 0);
            } else {
                jeDataMap.put("pendingapproval", 1);
            }
            /************************Debit Note Multilevel Approval RuleRelated Code End*****************************/
     
            jeDataMap.put("jedetails", jedetails);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeDataMap.put("transactionId", debitnote.getID());
            jeDataMap.put("transactionModuleid", Constants.Acc_Debit_Note_ModuleId); // store DN id and moduleid in journalentry
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                customrequestParams.put("modulerecid", journalEntry.getID());
                customrequestParams.put("moduleid", Constants.Acc_Debit_Note_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    jeDataMap.put("accjecustomdataref", journalEntry.getID());
                    jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);
                }
            }

            dnhm.put("dnid", debitnote.getID());
            Iterator itr = dndetails.iterator();
            while (itr.hasNext()) {
                DebitNoteDetail cnd = (DebitNoteDetail) itr.next();
                cnd.setDebitNote(debitnote);
            }
            dnhm.put("dndetails", dndetails);

            Iterator dntaxitr = dnTaxEntryDetails.iterator();
            while (dntaxitr.hasNext()) {
                DebitNoteTaxEntry noteTaxEntry = (DebitNoteTaxEntry) dntaxitr.next();
                noteTaxEntry.setDebitNote(debitnote);
            }
            dnhm.put("debitNoteTaxEntryDetails", dnTaxEntryDetails);

//            session.saveOrUpdate(debitnote);
            result = accDebitNoteobj.updateDebitNote(dnhm);
            debitnote = (DebitNote) result.getEntityList().get(0);

            //Add entry in optimized table
            accJournalEntryobj.saveAccountJEs_optimized(jeid);

            list.add(debitnote);
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
                        if (reloadInventory) { //check inventory flag and then update inventory to the inventory system

                            KwlReturnObject grResult = accountingHandlerDAOobj.getObject(GoodsReceiptDetail.class.getName(), (StringUtil.isNullOrEmpty(jobj.getString("linkto"))) ? jobj.getString("rowid") : jobj.getString("savedrowid"));
                            GoodsReceiptDetail goodsReceiptRow = (GoodsReceiptDetail) grResult.getEntityList().get(0);

                            boolean updateInventoryFlag = goodsReceiptRow.getInventory().isInvrecord(); //check update inventory flag and then update inventory to the inventory system
                            if (preferences.isWithInvUpdate()) {
                                updateInventoryFlag = getInvoiceStatusForGRO(goodsReceiptRow);
                            }

                            if (updateInventoryFlag) {
                                JSONObject productObject = new JSONObject();
                                productObject.put("itemUomId", jobj.getString("uomid"));
                                productObject.put("itemBaseUomRate", jobj.getDouble("baseuomrate"));
                                productObject.put("itemQuantity", jobj.getDouble("remquantity") * jobj.getDouble("baseuomrate") * (-1));
                                productObject.put("quantity", jobj.getDouble("remquantity") * jobj.getDouble("baseuomrate") * (-1));
                                //productObject.put("itemQuantity", jobj.getDouble("remquantity")*(-1));
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
                        jSONObject.put("purchasing", true);

                        String url = this.getServletContext().getInitParameter("inventoryURL");
                        CommonFnController cfc = new CommonFnController();
                        cfc.updateInventoryLevel(request, jSONObject, url, action);
                    }
                }
            }
            dnTerms = request.getParameter("invoicetermsmap");
            if (StringUtil.isAsciiString(dnTerms)) {
                mapDebitTerms(dnTerms, debitnote.getID(), sessionHandlerImpl.getUserid(request));
            }

//            if (cntype == 1 || cntype == 2 || cntype == 3) {
//                linkDebitNote(request, debitnote.getID());
//            }

           // auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + auditMsg + " a debit Note " + debitnote.getDebitNoteNumber(), request, debitnote.getID());
            String moduleName =Constants.DEBIT_NOTE;
            //Send Mail when Purchase Requisition is generated or modified.
            DocumentEmailSettings documentEmailSettings = null;
            KwlReturnObject documentEmailresult = accountingHandlerDAOobj.getObject(DocumentEmailSettings.class.getName(), sessionHandlerImpl.getCompanyid(request));
            documentEmailSettings = documentEmailresult != null ? (DocumentEmailSettings) documentEmailresult.getEntityList().get(0) : null;
            if (documentEmailSettings != null) {
                boolean sendmail = false;
                boolean isEditMail = false;
                if (StringUtil.isNullOrEmpty(debitNoteId)) { 
                    if (documentEmailSettings.isDebitNoteGenerationMail()) { ////Create New Case
                        sendmail = true;
                    }
                } else {
                    isEditMail = true;
                    if (documentEmailSettings.isDebitNoteUpdationMail()) { // edit case  
                         sendmail = true;
                    }
                }
                if (sendmail) {
                    String userMailId="",userName="",currentUserid="";
                    String createdByEmail = "";
                    String createdById = "";
                    HashMap<String, Object> requestParams= AccountingManager.getEmailNotificationParams(request);
                    if(requestParams.containsKey("userfullName")&& requestParams.get("userfullName")!=null){
                        userName=(String)requestParams.get("userfullName");
                    }
                    if(requestParams.containsKey("usermailid")&& requestParams.get("usermailid")!=null){
                        userMailId=(String)requestParams.get("usermailid");
                    }
                    if(requestParams.containsKey(Constants.useridKey)&& requestParams.get(Constants.useridKey)!=null){
                        currentUserid=(String)requestParams.get(Constants.useridKey);
                    }
                    
                    List<String> mailIds = new ArrayList();
                    if (!StringUtil.isNullOrEmpty(userMailId)) {
                        mailIds.add(userMailId);
                    }
                    
                    /*
                     if Edit mail option is true then get userid and Email id of document creator.
                     */
                    if (isEditMail) {
                        if (debitnote != null && debitnote.getCreatedby() != null) {
                            createdByEmail = debitnote.getCreatedby().getEmailID();
                            createdById = debitnote.getCreatedby().getUserID();
                        }
                        /*
                         if current user userid == document creator userid then don't add creator email ID in List.
                         */
                        if (!StringUtil.isNullOrEmpty(createdByEmail) && !(currentUserid.equalsIgnoreCase(createdById))) {
                            mailIds.add(createdByEmail);
                        }
                    }
                   
                    String[] temp = new String[mailIds.size()];
                    String[] tomailids = mailIds.toArray(temp);
                    String prNumber = entryNumber;
                    accountingHandlerDAOobj.sendSaveTransactionEmails(prNumber, moduleName, tomailids, userName, isEditMail, companyid);
                }
            }
            if (company.getCountry() != null && company.getCountry().getID().equals(Constants.INDIA_COUNTRYID) && !StringUtil.isNullOrEmpty(request.getParameter("linkinvoiceids"))) {
                String[] linkinvoiceids = request.getParameter("linkinvoiceids").split(",");
                if (linkinvoiceids != null && linkinvoiceids.length > 0) {
                    for (String invoiceid : linkinvoiceids) {
                        JSONObject json = new JSONObject();
                        json.put("debitnoteid", debitnote.getID());
                        if (debitnote.getVendor() != null) {
                            json.put("goodsReceipt", invoiceid);
                        } else {
                            json.put("invoiceid", invoiceid);
                        }
                        accDebitNoteobj.saveDebitNoteInvoiceMappingInfo(json);
                    }
                }
            }
            ll.add(oldjeid);
            ll.add(debitnote);
            ll.add(approvalStatusLevel);
            ll.add(mailParams);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveDebitNote : " + ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveDebitNote : " + ex.getMessage(), ex);
        }
        return ll;
    }
    
        public ModelAndView approvePendingDebitNote(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException {
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
            String baseUrl = URLUtil.getPageURL(request, loginpageFull);
            String psotingDateStr = request.getParameter("postingDate");
            String userid = sessionHandlerImpl.getUserid(request);
            String userName = sessionHandlerImpl.getUserFullName(request);
            double amount = StringUtil.isNullOrEmpty(request.getParameter("amount")) ? 0 : Double.parseDouble(request.getParameter("amount"));
            /*
             * ApprovalType =1 : approve as normal way 
             * ApprovalType =2 : approve DN agianst invoice as otherwise 
             * ApprovalType =3 : approve DN agianst invoice after editing, for this request not come here it goes to saveDebitNote
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
                List list = accDebitNoteService.approvePendingDebitNoteAgainstInvoiceAsDNOtherwise(requestParams);
                msg = (String) list.get(0);
            } else {
                List list = accDebitNoteService.approvePendingDebitNote(requestParams);
                msg = (String) list.get(0);
            }
            issuccess = true;
            txnManager.commit(status);
            
            
            //below code is for Rounding off JE Generation if needed
            status = txnManager.getTransaction(def);
            try {
                accDebitNoteService.postRoundingJEAfterApproveDebitNote(paramJobj);
                txnManager.commit(status);
            } catch (ServiceException ex) {
                if (status != null) {
                    txnManager.rollback(status);
                }
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AccountingException ae) {
                txnManager.rollback(status);
                isAccountingExe = true;
                msg = "" + ae.getMessage();
                msg = msg.replaceFirst("Transaction", "JE Posting");
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ae);
            }catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("isAccountingExe", isAccountingExe);
            } catch (JSONException ex) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView rejectPendingDebitNote(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Invoice_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            int level = 0;
            String dnID = "";
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            JSONObject jObj = jArr.getJSONObject(0);
            dnID = StringUtil.DecodeText(jObj.optString("billid"));
            KwlReturnObject invRes = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), dnID);
            DebitNote debitNote = (DebitNote) invRes.getEntityList().get(0);
            level = debitNote.getApprovestatuslevel();
            
            String userid = sessionHandlerImpl.getUserid(request);
            String userName = sessionHandlerImpl.getUserFullName(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            double amount = StringUtil.isNullOrEmpty(request.getParameter("amount")) ? 0 : Double.parseDouble(request.getParameter("amount"));
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put("userid", userid);
            requestParams.put("userName", userName);
            requestParams.put("amount", amount);

            //Delete unrealised JE for Credit Note
            accJournalEntryobj.permanentDeleteJournalEntryDetailReval(dnID, companyid);
            accJournalEntryobj.permanentDeleteJournalEntryReval(dnID, companyid);
            
            // delete foreign gain loss JE
            List resultJe = accDebitNoteobj.getForeignGainLossJE(dnID, companyid);
            if (resultJe.size() > 0 && resultJe.get(0) != null) {
                Iterator itr = resultJe.iterator();
                while (itr.hasNext()) {
                    Object object = itr.next();
                    String jeid = object != null ? object.toString() : "";
                    accDebitNoteService.deleteJEArray(jeid, companyid);
                }
            }
            
            // delete JE updating flag
            if(debitNote.getJournalEntry()!=null){
                accJournalEntryobj.deleteJEEntry(debitNote.getJournalEntry().getID(),companyid);
            }
            //update amount due of linking invoices
//            accCreditNoteService.updateOpeningInvoiceAmountDue(cnID, companyid);
            
                    
            boolean isRejected = accDebitNoteService.rejectPendingDebitNote(requestParams,jArr);
            txnManager.commit(status);
            issuccess = true;

            if (isRejected) {
                msg = messageSource.getMessage("acc.field.debitnotehasbeenrejectedsuccessfully", null, RequestContextUtils.getLocale(request)) + " by " + userName +" at Level " + level + ".";
            } else {
                msg = messageSource.getMessage("acc.vq.notAuthorisedToRejectThisRecord", null, RequestContextUtils.getLocale(request)) + " at Level " + level + ".";
            }

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView checkInvoiceKnockedOffDuringDebitNotePending (HttpServletRequest request,HttpServletResponse response){
        JSONObject jobj = new JSONObject();
        try{
           HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);  
           String billid = request.getParameter("billid");
           requestParams.put("billid", billid);
           jobj= accDebitNoteService.checkInvoiceKnockedOffDuringDebitNotePending(requestParams);
        } catch (Exception ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView updateDebitNoteTransactionDetailsInJE(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
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
                requestParams.put("companyid", companyid);
                KwlReturnObject result = accDebitNoteobj.getDebitNotesForJE(requestParams);
                Iterator itr = result.getEntityList().iterator();
                while (itr.hasNext()) {
                    DebitNote debitNote = (DebitNote) itr.next();
                    tempParams = new HashMap<String, Object>();
                    tempParams.put("transactionID", debitNote.getID());
                    tempParams.put("moduleID", Constants.Acc_Debit_Note_ModuleId);
                    tempParams.put("journalEntry", debitNote.getJournalEntry());
                    boolean isUpdated = accJournalEntryobj.updateJEDetails(tempParams);
                    if (isUpdated) {
                        jeupdatedcount++;
                    }
                }
            }
            txnManager.commit(status);
            issuccess = true;
        } catch (Exception ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            txnManager.rollback(status);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("Updated JE Records ", jeupdatedcount);
            } catch (JSONException ex) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    private void deleteJEDetailsCustomData(String jeid) throws ServiceException {
        KwlReturnObject cap = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), jeid);
        JournalEntry salesOrderDetails = (JournalEntry) cap.getEntityList().get(0);
        Set<JournalEntryDetail> journalEntryDetails = salesOrderDetails.getDetails();
        for (JournalEntryDetail journalEntryDetail : journalEntryDetails) {
            String jeDetailsId = journalEntryDetail.getID();
            KwlReturnObject jedresult1 = accJournalEntryobj.deleteJEDetailsCustomData(jeDetailsId);
        }
    }

    public List mapDebitTerms(String dnTerms, String debitNoteId, String userid) throws ServiceException {
        List ll = new ArrayList();
        try {
            JSONArray termsArr = new JSONArray(dnTerms);
            for (int cnt = 0; cnt < termsArr.length(); cnt++) {
                JSONObject temp = termsArr.getJSONObject(cnt);
                HashMap<String, Object> termMap = new HashMap<String, Object>();
                termMap.put("term", temp.getString("id"));
                termMap.put("termamount", Double.parseDouble(temp.getString("termamount")));
                int percentage = 0;
                if (!StringUtil.isNullOrEmpty(temp.getString("termpercentage"))) {
                    percentage = Integer.parseInt(temp.getString("termpercentage"));
                }
                termMap.put("termpercentage", percentage);
                termMap.put("debitNoteId", debitNoteId);
                termMap.put("creationdate", new Date());
                termMap.put("userid", userid);
                accDebitNoteobj.saveDebitNoteTermMap(termMap);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return ll;
    }

    public BillingDebitNote saveBillingDebitNote(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException {
        BillingDebitNote billingDebitnote = null;
        List list = new ArrayList();
        KwlReturnObject result;
        try {
            double externalCurrencyRate = StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            String sequenceformat = request.getParameter("sequenceformat");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
            GlobalParams.put("companyid", companyid);
            GlobalParams.put("gcurrencyid", currencyid);
            GlobalParams.put("dateformat", df);
            String customfield = request.getParameter("customfield");
            Date creationDate = df.parse(request.getParameter("creationdate"));

//            CompanyAccountPreferences preferences = (CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request));
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);

//            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

//            KWLCurrency kwlcurrency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);

//            BillingDebitNote debitnote = new BillingDebitNote();
            String entryNumber = request.getParameter("number");
            currencyid = (request.getParameter("currencyid") == null ? kwlcurrency.getCurrencyID() : request.getParameter("currencyid"));

//            KWLCurrency currency=(KWLCurrency)session.get(KWLCurrency.class,currencyid);
            curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

//            String q="from DebitNote where debitNoteNumber=? and company.companyID=?";
//            if(!HibernateUtil.executeQuery(session, q, new Object[]{entryNumber, AuthHandler.getCompanyid(request)}).isEmpty())
//                throw new AccountingException("Debit note number '" + entryNumber + "' already exists.");

            result = accDebitNoteobj.getBDNFromNoteNo(entryNumber, companyid);
            int count = result.getRecordTotalCount();
            if (count > 0) {
                throw new AccountingException(messageSource.getMessage("acc.field.Debitnotenumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
            }

//            debitnote.setDebitNoteNumber(request.getParameter("number"));
//            debitnote.setAutoGenerated(CompanyHandler.getNextAutoNumber(session, preferences, StaticValues.AUTONUM_DEBITNOTE).equals(entryNumber));
//            debitnote.setMemo(request.getParameter("memo"));
//            debitnote.setDeleted(false);
//            debitnote.setCompany(company);
//            debitnote.setCurrency(currency);
            HashMap<String, Object> bdnhm = new HashMap<String, Object>();
            boolean seqformat_oldflag = StringUtil.getBoolean(request.getParameter("seqformat_oldflag"));
            String nextDNAutoNo = "";
            String nextAutoNoInt = "";
            String datePrefix = "";
            String dateafterPrefix = "";
            String dateSuffix = "";
            if (seqformat_oldflag) {
                nextDNAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_BILLINGDEBITNOTE, sequenceformat);
            } else {
                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_BILLINGDEBITNOTE, sequenceformat, seqformat_oldflag, new Date());
                nextDNAutoNo = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                nextAutoNoInt = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                datePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                dateafterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                dateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                bdnhm.put(Constants.SEQFORMAT, sequenceformat);
                bdnhm.put(Constants.SEQNUMBER, nextAutoNoInt);
                bdnhm.put(Constants.DATEPREFIX, datePrefix);
                bdnhm.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
                bdnhm.put(Constants.DATESUFFIX, dateSuffix);
            }
            bdnhm.put("entrynumber", entryNumber);
            bdnhm.put("autogenerated", nextDNAutoNo.equals(entryNumber));
            bdnhm.put("memo", request.getParameter("memo"));
            bdnhm.put("deleted", false);
            bdnhm.put("companyid", companyid);
            bdnhm.put("currencyid", currencyid);

            Long seqNumber = null;
//            String query = "select count(dn.ID) from DebitNote dn inner join dn.journalEntry je  where dn.company.companyID=? and je.entryDate<=?";
//            List list = HibernateUtil.executeQuery(session, query, new Object[]{AuthHandler.getCompanyid(request), AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate"))});//

            result = accDebitNoteobj.getBDNSequenceNo(companyid, creationDate);
            List li = result.getEntityList();
            if (!li.isEmpty()) {
                seqNumber = (Long) li.get(0);
            }

//            debitnote.setSequence(seqNumber.intValue());
            bdnhm.put("sequence", seqNumber.intValue());

            String costCenterId = request.getParameter("costCenterId");
            boolean jeautogenflag = true;
            String nextJEAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_JOURNALENTRY);
            String jeentryNumber = nextJEAutoNo;// + "/" + entryNumber;

            Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
            jeDataMap.put("entrynumber", jeentryNumber);
            jeDataMap.put("autogenerated", jeautogenflag);
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
            bdnhm.put("journalentryid", jeid);

//            HashSet hs = new HashSet();
//            Double totalAmount = saveBillingDebitNoteRows(session, request, debitnote, company, hs, preferences,kwlcurrency,externalCurrencyRate);
            // saveBillingDebitNoteDiscountRows(session, request, debitnote, company);
            List DNlist = saveBillingDebitNoteRows1(GlobalParams, request, company, currency, journalEntry, preferences, externalCurrencyRate);
            Double totalAmount = (Double) DNlist.get(0);
            HashSet<DebitNoteDetail> dndetails = (HashSet<DebitNoteDetail>) DNlist.get(2);
            jedetails = (HashSet<JournalEntryDetail>) DNlist.get(3);
            Double discAccAmount = (Double) DNlist.get(1);

//            JournalEntryDetail jed = new JournalEntryDetail();
//            jed.setCompany(company);
//            jed.setAmount(totalAmount);
//            jed.setAccount((Account) session.get(Account.class, request.getParameter("accid")));
//            jed.setDebit(true);
//            hs.add(jed);

            HashMap<String, Object> jedMap = new HashMap();
            jedMap.put("srno", jedetails.size() + 1);
            jedMap.put("companyid", company.getCompanyID());
            jedMap.put("amount", discAccAmount);
            jedMap.put("accountid", request.getParameter("accid"));
            jedMap.put("debit", true);
            jedMap.put("jeid", jeid);
            KwlReturnObject jedresult = accJournalEntryobj.updateJournalEntryDetails(jedMap);
            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetails.add(jed);


//            JournalEntry journalEntry = CompanyHandler.makeJournalEntry(session, company.getCompanyID(), AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")),
//            request.getParameter("memo"), "JE" + debitnote.getDebitNoteNumber(),currency.getCurrencyID(),externalCurrencyRate, hs,request);
//            debitnote.setJournalEntry(journalEntry);
//            session.saveOrUpdate(debitnote);

            jeDataMap.put("jedetails", jedetails);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);

            result = accDebitNoteobj.saveBillingDebitNote(bdnhm);
            billingDebitnote = (BillingDebitNote) result.getEntityList().get(0);

            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                customrequestParams.put("modulerecid", journalEntry.getID());
                customrequestParams.put("moduleid", Constants.Acc_Debit_Note_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    jeDataMap.put("accjecustomdataref", journalEntry.getID());
                    jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);
                }
            }

            bdnhm.put("bdnid", billingDebitnote.getID());
            Iterator itr = dndetails.iterator();
            while (itr.hasNext()) {
                BillingDebitNoteDetail cnd = (BillingDebitNoteDetail) itr.next();
                cnd.setDebitNote(billingDebitnote);
            }
            bdnhm.put("bdndetails", dndetails);

//            session.saveOrUpdate(debitnote);
            result = accDebitNoteobj.saveBillingDebitNote(bdnhm);
            billingDebitnote = (BillingDebitNote) result.getEntityList().get(0);

            //Add entry in optimized table
            accJournalEntryobj.saveAccountJEs_optimized(jeid);

            list.add(billingDebitnote);

        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveDebitNote : " + ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveDebitNote : " + ex.getMessage(), ex);
        }
        return billingDebitnote;
    }

    public List saveDebitNoteRows(HashMap<String, Object> GlobalParams, HttpServletRequest request, Company company, KWLCurrency currency, JournalEntry je, CompanyAccountPreferences preferences, double externalCurrencyRate) throws JSONException, ServiceException, SessionExpiredException, ParseException {
        List resultlist = new ArrayList();
        double totalAmount = 0;
        double totalTax = 0;
        double discAccAmount = 0;
        HashSet dndetails = new HashSet();
        HashSet jedetails = new HashSet();

        JournalEntryDetail jed;
        KwlReturnObject result;

        boolean includeTax = StringUtil.getBoolean(request.getParameter("includetax"));
        String companyid = (String) GlobalParams.get("companyid");
        String currencyid = (request.getParameter("currencyid") == null ? currency.getCurrencyID() : request.getParameter("currencyid"));
        JSONArray jArr = new JSONArray(request.getParameter("productdetails"));
        List list = new ArrayList();
        for (int i = 0; i < jArr.length(); i++) {
            double taxamount = 0;
            double amount = 0;
            JSONObject jobj = jArr.getJSONObject(i);
            DebitNoteDetail row = new DebitNoteDetail();
            row.setSrno(i + 1);
            double disc = jobj.getDouble("discamount");
            row.setCompany(company);
//            row.setDebitNote(dn);
            row.setMemo(request.getParameter("memo"));
            row.setQuantity(jobj.getDouble("remquantity"));

//            GoodsReceiptDetail goodsReceiptRow = (GoodsReceiptDetail) session.get(GoodsReceiptDetail.class, jobj.getString("rowid"));
            result = accountingHandlerDAOobj.getObject(GoodsReceiptDetail.class.getName(), (StringUtil.isNullOrEmpty(jobj.getString("linkto"))) ? jobj.getString("rowid") : jobj.getString("savedrowid"));
            GoodsReceiptDetail goodsReceiptRow = (GoodsReceiptDetail) result.getEntityList().get(0);

            row.setGoodsReceiptRow(goodsReceiptRow);
            Product product = goodsReceiptRow.getInventory().getProduct();
//            Account account = (Account) session.get(Account.class, product.getPurchaseReturnAccount().getID());
            result = accountingHandlerDAOobj.getObject(Account.class.getName(), product.getPurchaseReturnAccount().getID());
            Account account = (Account) result.getEntityList().get(0);

            double percent = 0;
            if (goodsReceiptRow.getGoodsReceipt().getTax() != null) {
//                KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, goodsReceiptRow.getGoodsReceipt().getJournalEntry().getEntryDate(), goodsReceiptRow.getGoodsReceipt().getTax().getID());
                KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, goodsReceiptRow.getGoodsReceipt().getCreationDate(), goodsReceiptRow.getGoodsReceipt().getTax().getID());
                percent = (Double) perresult.getEntityList().get(0);
            }
//            if (jobj.getInt("typeid") == 2 || jobj.getInt("typeid") == 3) {
            if (jobj.getInt("typeid") > 1) {
//                Inventory inventory = CompanyHandler.makeInventory(session, request, product, jobj.getInt("remquantity"), jobj.optString("desc"), false, (jobj.getInt("typeid") == 3 ? false : true));
                JSONObject inventoryjson = new JSONObject();
                inventoryjson.put("productid", product.getID());
                double baseuomrate = 1;
                if (jobj.has("baseuomrate")) {
                    baseuomrate = jobj.getDouble("baseuomrate");
                }
                double quantity = jobj.getDouble("remquantity");
                if (jobj.has("uomid")) {
                    inventoryjson.put("uomid", jobj.getString("uomid"));
                }
//                inventoryjson.put("quantity", updateInventoryFlag ? quantity : 0);                
//                inventoryjson.put("baseuomquantity", updateInventoryFlag ? quantity * baseuomrate : 0 );
//                inventoryjson.put("actquantity", updateInventoryFlag ? 0 : quantity * baseuomrate);
                inventoryjson.put("quantity", quantity);
                inventoryjson.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(quantity,baseuomrate, companyid));
                inventoryjson.put("actquantity", 0);
                inventoryjson.put("baseuomrate", baseuomrate);

                inventoryjson.put("description", jobj.optString("desc"));
                inventoryjson.put("carryin", false);
                inventoryjson.put("defective", (jobj.getInt("typeid") == 3 ? false : true));
                inventoryjson.put("newinventory", false);
                inventoryjson.put("companyid", company.getCompanyID());
                inventoryjson.put("updatedate", authHandler.getDateOnlyFormat(request).parse(request.getParameter("creationdate")));
                KwlReturnObject invresult = accProductObj.addInventory(inventoryjson);
                Inventory inventory = (Inventory) invresult.getEntityList().get(0);
                row.setInventory(inventory);
                if (list.contains(account)) {
                    dndetails.add(row);
                    continue;
                }
                for (int k = 0; k < jArr.length(); k++) {
                    JSONObject jobj1 = jArr.getJSONObject(k);
//                    GoodsReceiptDetail compGoodsReceiptRow = (GoodsReceiptDetail) session.get(GoodsReceiptDetail.class, jobj1.getString("rowid"));
                    result = accountingHandlerDAOobj.getObject(GoodsReceiptDetail.class.getName(), jobj1.getString("rowid"));
                    GoodsReceiptDetail compGoodsReceiptRow = (GoodsReceiptDetail) result.getEntityList().get(0);
                    Product compProduct = compGoodsReceiptRow.getInventory().getProduct();

//                    Account compAccount = (Account) session.get(Account.class, compProduct.getPurchaseReturnAccount().getID());
                    result = accountingHandlerDAOobj.getObject(Account.class.getName(), compProduct.getPurchaseReturnAccount().getID());
                    Account compAccount = (Account) result.getEntityList().get(0);
//                    if (account == compAccount && (jobj1.getInt("typeid") == 2  || jobj.getInt("typeid") == 3)) {
                    if (account == compAccount && (jobj1.getInt("typeid") > 1)) {
                        amount = amount + jobj1.getDouble("discamount");
                        list.add(compAccount);
                    }
                    if (disc > 0) {
//                        Discount discount = new Discount();
//                        discount.setDiscount(disc);
//                        discount.setInPercent(false);
//                        discount.setOriginalAmount(CompanyHandler.getBaseToCurrencyAmount(session,request,jobj.getDouble("amount"),currencyid,null));
//                        discount.setCompany(company);
//                        session.save(discount);
                        JSONObject discjson = new JSONObject();
                        discjson.put("discount", disc);
                        discjson.put("inpercent", false);
                        KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, jobj.getDouble("amount"), currencyid, null, externalCurrencyRate);
                        discjson.put("originalamount", (Double) bAmt.getEntityList().get(0));
                        discjson.put("companyid", company.getCompanyID());
                        KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                        Discount discount = (Discount) dscresult.getEntityList().get(0);
                        row.setDiscount(discount);

                        if (includeTax) {
                            taxamount = disc * percent / 100;
                            row.setTaxAmount(taxamount);
                            totalTax += taxamount;
                            if (includeTax && taxamount > 0) {
//                                jed = new JournalEntryDetail();
//                                jed.setCompany(company);
//                                jed.setAmount(taxamount);
//                                jed.setAccount(goodsReceiptRow.getGoodsReceipt().getTax().getAccount());
//                                jed.setDebit(false);
//                                hs.add(jed);
                                JSONObject jedjson = new JSONObject();
                                jedjson.put("srno", jedetails.size() + 1);
                                jedjson.put("companyid", company.getCompanyID());
                                jedjson.put("amount", taxamount);
                                jedjson.put("accountid", goodsReceiptRow.getGoodsReceipt().getTax().getAccount().getID());
                                jedjson.put("debit", false);
                                jedjson.put("jeid", je.getID());
                                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                jedetails.add(jed);
                            }
                        }
                    }
                }
            } else {
                if (includeTax) {
                    taxamount = disc * percent / 100;
                    row.setTaxAmount(taxamount);
                    totalTax += taxamount;
                    if (includeTax && taxamount > 0) {
//                        jed = new JournalEntryDetail();
//                        jed.setCompany(company);
//                        jed.setAmount(taxamount);
//                        jed.setAccount(goodsReceiptRow.getGoodsReceipt().getTax().getAccount());
//                        jed.setDebit(false);
//                        hs.add(jed);
                        JSONObject jedjson = new JSONObject();
                        jedjson.put("srno", jedetails.size() + 1);
                        jedjson.put("companyid", company.getCompanyID());
                        jedjson.put("amount", taxamount);
                        jedjson.put("accountid", goodsReceiptRow.getGoodsReceipt().getTax().getAccount().getID());
                        jedjson.put("debit", false);
                        jedjson.put("jeid", je.getID());
                        KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jedetails.add(jed);
                    }
                }
                discAccAmount = discAccAmount + jobj.getDouble("discamount");
                dndetails.add(row);
                if (disc > 0) {
//                    Discount discount = new Discount();
//                    discount.setDiscount(disc);
//                    discount.setInPercent(false);
//                    discount.setOriginalAmount(CompanyHandler.getBaseToCurrencyAmount(session,request,jobj.getDouble("amount"),currencyid,null));
//                    discount.setCompany(company);
//                    session.save(discount);
//                    row.setDiscount(discount);
                    JSONObject discjson = new JSONObject();
                    discjson.put("discount", disc);
                    discjson.put("inpercent", false);
                    KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, jobj.getDouble("amount"), currencyid, null, 0);
                    discjson.put("originalamount", (Double) bAmt.getEntityList().get(0));
                    discjson.put("companyid", company.getCompanyID());
                    KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                    Discount discount = (Discount) dscresult.getEntityList().get(0);
                    row.setDiscount(discount);
                }
                continue;
            }
            dndetails.add(row);
//            jed = new JournalEntryDetail();
//            jed.setCompany(company);
//            jed.setAmount(amount);
//            jed.setAccount(account);
//            jed.setDebit(false);
            JSONObject jedjson = new JSONObject();
            jedjson.put("srno", jedetails.size() + 1);
            jedjson.put("companyid", company.getCompanyID());
            jedjson.put("amount", amount);
            jedjson.put("accountid", account.getID());
            jedjson.put("debit", false);
            jedjson.put("jeid", je.getID());
            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);

            jedetails.add(jed);
            totalAmount += amount;
        }
        if (discAccAmount != 0.0) {
//            jed = new JournalEntryDetail();
//            jed.setCompany(company);
//            jed.setAmount(discAccAmount);
//            jed.setAccount((Account) session.get(Account.class, preferences.getDiscountReceived().getID()));
//            jed.setDebit(false);
            JSONObject jedjson = new JSONObject();
            jedjson.put("srno", jedetails.size() + 1);
            jedjson.put("companyid", company.getCompanyID());
            jedjson.put("amount", discAccAmount);
            jedjson.put("accountid", preferences.getDiscountReceived().getID());
            jedjson.put("debit", false);
            jedjson.put("jeid", je.getID());
            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetails.add(jed);
            totalAmount += discAccAmount;
        }

        resultlist.add(totalAmount + totalTax);
        resultlist.add(discAccAmount);
        resultlist.add(dndetails);
        resultlist.add(jedetails);
        return resultlist;
    }

    public List saveBillingDebitNoteRows(HashMap<String, Object> GlobalParams, HttpServletRequest request, Company company, KWLCurrency currency, JournalEntry je, CompanyAccountPreferences preferences, double externalCurrencyRate) throws JSONException, ServiceException, SessionExpiredException, ParseException {
        double totalAmount = 0;
        double totalTax = 0;
        double discAccAmount = 0;
        List resultlist = new ArrayList();
        HashSet bdndetails = new HashSet();
        HashSet jedetails = new HashSet();

        JournalEntryDetail jed;
        KwlReturnObject result;
        String debitAccount = "";

//            HashSet rows = new HashSet();
//            Account account;

        boolean includeTax = StringUtil.getBoolean(request.getParameter("includetax"));
        String companyid = (String) GlobalParams.get("companyid");
        String currencyid = (request.getParameter("currencyid") == null ? currency.getCurrencyID() : request.getParameter("currencyid"));
        JSONArray jArr = new JSONArray(request.getParameter("productdetails"));
        List list = new ArrayList();
        for (int i = 0; i < jArr.length(); i++) {
            double taxamount = 0;
            double amount = 0;
            JSONObject jobj = jArr.getJSONObject(i);
            BillingDebitNoteDetail row = new BillingDebitNoteDetail();
            row.setSrno(i + 1);
            double disc = jobj.getDouble("discamount");
            row.setCompany(company);
//                row.setDebitNote(dn);
//                row.setMemo(dn.getMemo());
            row.setMemo(request.getParameter("memo"));
            row.setQuantity(jobj.getDouble("remquantity"));

//                BillingGoodsReceiptDetail goodsReceiptRow = (BillingGoodsReceiptDetail) session.get(BillingGoodsReceiptDetail.class, jobj.getString("rowid"));
            result = accountingHandlerDAOobj.getObject(BillingGoodsReceiptDetail.class.getName(), (StringUtil.isNullOrEmpty(jobj.getString("linkto"))) ? jobj.getString("rowid") : jobj.getString("savedrowid"));
            BillingGoodsReceiptDetail goodsReceiptRow = (BillingGoodsReceiptDetail) result.getEntityList().get(0);

            if (debitAccount.equals("")) {
                String je1 = goodsReceiptRow.getBillingGoodsReceipt().getJournalEntry().getID();
                KwlReturnObject result1 = accJournalEntryobj.getJournalEntryDetail(je1, company.getCompanyID());
                if (result1 != null && result1.getEntityList() != null && result1.getEntityList().size() > 0) {
                    List<JournalEntryDetail> jedList = result1.getEntityList();
                    for (JournalEntryDetail jed1 : jedList) {
                        //To do - no billing mode
//                            if(jed1.getAccount().getGroup().getID().equals(Group.EXPENSES) && jed1.isDebit()) {
//                                debitAccount = jed1.getAccount().getID();
//                                break;
//                            }
                    }
                }
            }


            row.setGoodsReceiptRow(goodsReceiptRow);
            String product = goodsReceiptRow.getProductDetail();
            double percent = 0;
            if (goodsReceiptRow.getBillingGoodsReceipt().getTax() != null) {
//                  percent=CompanyHandler.getTaxPercent(session, request, goodsReceiptRow.getBillingGoodsReceipt().getJournalEntry().getEntryDate(),goodsReceiptRow.getBillingGoodsReceipt().getTax().getID());
                KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, goodsReceiptRow.getBillingGoodsReceipt().getJournalEntry().getEntryDate(), goodsReceiptRow.getBillingGoodsReceipt().getTax().getID());
                percent = (Double) perresult.getEntityList().get(0);

            }

            if (includeTax) {
                taxamount = disc * percent / 100;
                row.setTaxAmount(taxamount);
                totalTax += taxamount;
                if (includeTax && taxamount > 0) {
//                        jed = new JournalEntryDetail();
//                        jed.setCompany(company);
//                        jed.setAmount(taxamount);
//                        jed.setAccount(goodsReceiptRow.getBillingGoodsReceipt().getTax().getAccount());
//                        jed.setDebit(false);
//                        hs.add(jed);
                    HashMap<String, Object> jedMap = new HashMap();
                    jedMap.put("srno", jedetails.size() + 1);
                    jedMap.put("companyid", company.getCompanyID());
                    jedMap.put("amount", taxamount);
                    jedMap.put("accountid", goodsReceiptRow.getBillingGoodsReceipt().getTax().getAccount().getID());
                    jedMap.put("debit", false);
                    jedMap.put("jeid", je.getID());
                    KwlReturnObject jedresult = accJournalEntryobj.updateJournalEntryDetails(jedMap);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                }
            }
            discAccAmount = discAccAmount + jobj.getDouble("discamount");
            bdndetails.add(row);
            if (disc > 0) {
//                    Discount discount = new Discount();
//                    discount.setDiscount(disc);
//                    discount.setInPercent(false);
//                    discount.setOriginalAmount(CompanyHandler.getBaseToCurrencyAmount(session,request,jobj.getDouble("amount"),currencyid,null,externalCurrencyRate));
//                    discount.setCompany(company);
//                    session.save(discount);
//                    row.setDiscount(discount);

                HashMap<String, Object> discMap = new HashMap();
                discMap.put("discount", disc);
                discMap.put("inpercent", false);
                KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, jobj.getDouble("amount"), currencyid, null, externalCurrencyRate);
                discMap.put("originalamount", (Double) bAmt.getEntityList().get(0));
                discMap.put("companyid", company.getCompanyID());
                KwlReturnObject dscresult = accDiscountobj.updateDiscount(discMap);
                Discount discount = (Discount) dscresult.getEntityList().get(0);
                row.setDiscount(discount);

            }
        }
        if (discAccAmount != 0.0) {
//                jed = new JournalEntryDetail();
//                jed.setCompany(company);
//                jed.setAmount(discAccAmount);
//                jed.setAccount((Account) session.get(Account.class, preferences.getDiscountReceived().getID()));
//                jed.setDebit(false);
//
            HashMap<String, Object> jedMap = new HashMap();
            jedMap.put("srno", jedetails.size() + 1);
            jedMap.put("companyid", company.getCompanyID());
            jedMap.put("amount", discAccAmount);
            jedMap.put("accountid", (!debitAccount.equals("")) ? debitAccount : preferences.getDiscountReceived().getID());
            jedMap.put("debit", false);
            jedMap.put("jeid", je.getID());
            KwlReturnObject jedresult = accJournalEntryobj.updateJournalEntryDetails(jedMap);
            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetails.add(jed);
            totalAmount += discAccAmount;
        }
        resultlist.add(totalAmount + totalTax);
        resultlist.add(discAccAmount);
        resultlist.add(bdndetails);
        resultlist.add(jedetails);
        return resultlist;
    }

    public ModelAndView getDebitNote(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = gettDebitNoteMap(request);
            KwlReturnObject result = accDebitNoteobj.getDebitNotes(requestParams);
            JSONArray DataJArr = getDebitNotesJson(requestParams, result.getEntityList());
            jobj.put("data", DataJArr);
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getBillingDebitNote(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = gettDebitNoteMap(request);
            KwlReturnObject result = accDebitNoteobj.getBillingDebitNotes(requestParams);
            JSONArray DataJArr = getBillingDebitNotesJson(requestParams, result.getEntityList());
            jobj.put("data", DataJArr);
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public static HashMap<String, Object> gettDebitNoteMap(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put(Constants.ss, request.getParameter(Constants.ss));
        //checking filetype to print all records for csv,print & pdf
        if (StringUtil.isNullOrEmpty(request.getParameter("filetype"))) {
            requestParams.put(Constants.start, request.getParameter(Constants.start));
            requestParams.put(Constants.limit, request.getParameter(Constants.limit));
        }
        requestParams.put(CCConstants.REQ_costCenterId, request.getParameter(CCConstants.REQ_costCenterId));
        requestParams.put(CCConstants.accid, request.getParameter(CCConstants.accid));
        if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.REQ_startdate))) {
            requestParams.put(Constants.REQ_startdate, StringUtil.DecodeText(request.getParameter(Constants.REQ_startdate)));
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.REQ_enddate))) {
            requestParams.put(Constants.REQ_enddate, StringUtil.DecodeText(request.getParameter(Constants.REQ_enddate)));
        }
        requestParams.put(Constants.Acc_Search_Json, request.getParameter(Constants.Acc_Search_Json));
        requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
        requestParams.put(Constants.moduleid, request.getParameter(Constants.moduleid));
        requestParams.put(Constants.userdf, authHandler.getUserDateFormatterWithoutTimeZone(request));
        requestParams.put("vendorid", request.getParameter("vendorid"));
        requestParams.put("customerid", request.getParameter("customerid"));
        int cntype = StringUtil.isNullOrEmpty(request.getParameter("cntype")) ? 1 : Integer.parseInt(request.getParameter("cntype"));
        requestParams.put("cntype", cntype);
        requestParams.put("noteid", request.getParameter("noteid"));
        requestParams.put("deleted", request.getParameter("deleted"));
        requestParams.put("nondeleted", request.getParameter("nondeleted"));
        requestParams.put("currencyfilterfortrans", request.getParameter("currencyfilterfortrans"));
        int transactiontype = StringUtil.isNullOrEmpty(request.getParameter("transactiontype")) ? 1 : Integer.parseInt(request.getParameter("transactiontype"));
        requestParams.put("transactiontype", transactiontype);
        requestParams.put("upperLimitDate", request.getParameter("upperLimitDate")==null?"":request.getParameter("upperLimitDate"));
        if (request.getParameter("isReceipt") != null) {
            requestParams.put("isReceipt", request.getParameter("isReceipt"));
        }
        requestParams.put("isExport", request.getParameter("isExport")==null?false:Boolean.parseBoolean(request.getParameter("isExport")));
        requestParams.put("pendingapproval", StringUtil.isNullOrEmpty(request.getParameter("pendingapproval"))?false:Boolean.parseBoolean(request.getParameter("pendingapproval")));
        if (!StringUtil.isNullOrEmpty(sessionHandlerImpl.getBrowserTZ(request))) {
            requestParams.put("browsertz", sessionHandlerImpl.getBrowserTZ(request));
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.requestModuleId))) {
            requestParams.put(Constants.requestModuleId, request.getParameter(Constants.requestModuleId));
        }
        return requestParams;
    }

    public static HashMap<String, Object> gettDebitNoteMapJson(JSONObject paramJobj) throws SessionExpiredException, JSONException, ServiceException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParamsJson(paramJobj);
        requestParams.put(Constants.ss, paramJobj.optString(Constants.ss,null));
        //checking filetype to print all records for csv,print & pdf
        if (StringUtil.isNullOrEmpty(paramJobj.optString("filetype"))) {
            requestParams.put(Constants.start, paramJobj.optString(Constants.start,null));
            requestParams.put(Constants.limit, paramJobj.optString(Constants.limit,null));
        }
        requestParams.put(CCConstants.REQ_costCenterId, paramJobj.optString(CCConstants.REQ_costCenterId,null));
        requestParams.put(CCConstants.accid, paramJobj.optString(CCConstants.accid,null));
        requestParams.put(Constants.REQ_startdate, paramJobj.optString(Constants.REQ_startdate,null));
        requestParams.put(Constants.REQ_enddate, paramJobj.optString(Constants.REQ_enddate,null));
        requestParams.put(Constants.Acc_Search_Json, paramJobj.optString(Constants.Acc_Search_Json,null));
        requestParams.put(Constants.Filter_Criteria, paramJobj.optString(Constants.Filter_Criteria,null));
        requestParams.put(Constants.moduleid, paramJobj.optString(Constants.moduleid,null));
        requestParams.put(Constants.userdf, authHandler.getUserDateFormatterWithoutTimeZone(paramJobj));
        requestParams.put("vendorid", paramJobj.optString("vendorid",null));
        requestParams.put("customerid", paramJobj.optString("customerid",null));
        int cntype = StringUtil.isNullOrEmpty(paramJobj.optString("cntype",null)) ? 1 : Integer.parseInt(paramJobj.getString("cntype"));
        requestParams.put("cntype", cntype);
        requestParams.put("noteid", paramJobj.optString("noteid",null));
        requestParams.put("deleted", paramJobj.optString("deleted",null));
        requestParams.put("nondeleted", paramJobj.optString("nondeleted",null));
        requestParams.put("currencyfilterfortrans", paramJobj.optString("currencyfilterfortrans",null));
        int transactiontype = StringUtil.isNullOrEmpty(paramJobj.optString("transactiontype",null)) ? 1 : Integer.parseInt(paramJobj.getString("transactiontype"));
        requestParams.put("transactiontype", transactiontype);
        requestParams.put("upperLimitDate", paramJobj.optString("upperLimitDate",null)==null?"":paramJobj.getString("upperLimitDate"));
        if (paramJobj.optString("isReceipt",null) != null) {
            requestParams.put("isReceipt", paramJobj.optString("isReceipt",null));
        }
        requestParams.put("isExport", paramJobj.optString("isExport",null)==null?false:Boolean.parseBoolean(paramJobj.getString("isExport")));
        return requestParams;
    }
    
    public JSONArray getDebitNotesJson(HashMap<String, Object> requestParams, List list) throws ServiceException {
        JSONArray JArr = new JSONArray();
        try {
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("df");
            double tax = 0;
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                DebitNote debitMemo = (DebitNote) row[0];
                JournalEntry je = debitMemo.getJournalEntry();
                Vendor vendor = (Vendor) row[1];
                JournalEntryDetail details = (JournalEntryDetail) row[2];
                JSONObject obj = new JSONObject();
                obj.put("noteid", debitMemo.getID());
                obj.put("noteno", debitMemo.getDebitNoteNumber());
                obj.put("journalentryid", je.getID());
                obj.put("currencysymbol", (debitMemo.getCurrency() == null ? currency.getSymbol() : debitMemo.getCurrency().getSymbol()));
                obj.put("currencyid", (debitMemo.getCurrency() == null ? currency.getCurrencyID() : debitMemo.getCurrency().getCurrencyID()));
                obj.put("entryno", je.getEntryNumber());
                obj.put("personid", vendor.getID());
                obj.put("personname", vendor.getAccount().getName());
                obj.put("amount", details.getAmount());
//                obj.put("date", df.format(je.getEntryDate()));
                obj.put("date", df.format(debitMemo.getCreationDate()));
                obj.put("memo", debitMemo.getMemo());
                obj.put("deleted", debitMemo.isDeleted());
                obj.put("costcenterid", debitMemo.getCostcenter() == null ? "" : debitMemo.getCostcenter().getID());
                obj.put("costcenterName", je.getCostcenter() == null ? "" : je.getCostcenter().getName());

                KwlReturnObject result = accJournalEntryobj.getJournalEntryDetail(je.getID(), je.getCompany().getCompanyID());
                Iterator iterator = result.getEntityList().iterator();
                while (iterator.hasNext()) {
                    JournalEntryDetail jed = (JournalEntryDetail) iterator.next();
                    Account account = null;
                    account = jed.getAccount();
                    //To do - need to test
                    if (account.getMastertypevalue() == Group.ACCOUNTTYPE_GST) {
                        if (!jed.isDebit()) {
                            tax = jed.getAmount();
                        }
                    }
                }
                result = accDebitNoteobj.getTotalTax_TotalDiscount(debitMemo.getID());
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
                JArr.put(obj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accDebitNoteController.getDebitNotesJson : " + ex.getMessage(), ex);
        }
        return JArr;
    }

    public JSONArray getBillingDebitNotesJson(HashMap<String, Object> requestParams, List list) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("df");
            double tax = 0;
            Iterator itr = list.iterator();

            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                BillingDebitNote debitMemo = (BillingDebitNote) row[0];
                JournalEntry je = debitMemo.getJournalEntry();
                Vendor vendor = (Vendor) row[1];
                JournalEntryDetail details = (JournalEntryDetail) row[2];
                JSONObject obj = new JSONObject();
                obj.put("noteid", debitMemo.getID());
                obj.put("noteno", debitMemo.getDebitNoteNumber());
                obj.put("journalentryid", je.getID());
                obj.put("currencysymbol", (debitMemo.getCurrency() == null ? currency.getSymbol() : debitMemo.getCurrency().getSymbol()));
                obj.put("currencyid", (debitMemo.getCurrency() == null ? currency.getCurrencyID() : debitMemo.getCurrency().getCurrencyID()));
                obj.put("entryno", je.getEntryNumber());
                obj.put("personid", vendor.getID());
                obj.put("personname", vendor.getAccount().getName());
                obj.put("amount", details.getAmount());
                obj.put("date", df.format(je.getEntryDate()));
                obj.put("memo", debitMemo.getMemo());
                obj.put("deleted", debitMemo.isDeleted());
                obj.put("costcenterid", je.getCostcenter() == null ? "" : je.getCostcenter().getID());
                obj.put("costcenterName", je.getCostcenter() == null ? "" : je.getCostcenter().getName());

                KwlReturnObject result = accJournalEntryobj.getJournalEntryDetail(je.getID(), je.getCompany().getCompanyID());
                Iterator iterator = result.getEntityList().iterator();
                while (iterator.hasNext()) {
                    JournalEntryDetail jed = (JournalEntryDetail) iterator.next();
                    Account account = null;
                    account = jed.getAccount();
                    //To do - need to test
                    if (account.getMastertypevalue() == Group.ACCOUNTTYPE_GST) {
                        if (!jed.isDebit()) {
                            tax = jed.getAmount();
                        }
                    }
                }
                result = accDebitNoteobj.getTotalTax_TotalDiscount_Billing(debitMemo.getID());
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
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accDebitNoteController.getBillingDebitNotesJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView getDebitNoteAccountsRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = getDebitNoteAccountsRows(request);
            issuccess = true;
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            msg = "accDebitNoteController.getDebitNoteAccountsRows:" + ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getDebitNoteAccountsRows(HttpServletRequest request) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArray = new JSONArray();
        try {
            String dNoteId = request.getParameter("noteId");
            boolean isCopy = !StringUtil.isNullOrEmpty(request.getParameter("isCopy")) ? Boolean.parseBoolean(request.getParameter("isCopy")) : false;
            KwlReturnObject result = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), dNoteId);
            DebitNote dn = (DebitNote) result.getEntityList().get(0);

            if (dn != null) {
                Set<DebitNoteTaxEntry> dnTaxEntryDetails = dn.getDnTaxEntryDetails();
                if (dnTaxEntryDetails != null && !dnTaxEntryDetails.isEmpty()) {
                    String countryID = dn.getCompany().getCountry() != null ? dn.getCompany().getCountry().getID() : null;
                        HashMap<String, Object> fieldrequestParams = new HashMap();
                        HashMap<String, String> customFieldMap = new HashMap<String, String>();
                        HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
                        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                        fieldrequestParams.put(Constants.filter_values, Arrays.asList(dn.getCompany().getCompanyID(), Constants.Acc_Debit_Note_ModuleId));
                        HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                        HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);

                    for (DebitNoteTaxEntry noteTaxEntry : dnTaxEntryDetails) {
                        JSONObject obj = new JSONObject();
                        obj.put("rowid", noteTaxEntry.getID());
                        obj.put("accountid", noteTaxEntry.getAccount().getID());
                        obj.put("dramount", noteTaxEntry.getAmount());
                        obj.put("prtaxid", (noteTaxEntry.getTax() != null) ? noteTaxEntry.getTax().getID() : "None");
//                        obj.put("prtaxid", noteTaxEntry.getTax() != null ? (isCopy ? (noteTaxEntry.getTax().isActivated() ? noteTaxEntry.getTax().getID() : "") : noteTaxEntry.getTax().getID()) : "");
                        obj.put("amountwithtax", noteTaxEntry.getAmount() + noteTaxEntry.getTaxamount());
                        obj.put("rateIncludingGst", noteTaxEntry.getRateIncludingGst());
                        obj.put("taxamount", noteTaxEntry.getTaxamount());
                        obj.put("reason", (noteTaxEntry.getReason()!=null)?noteTaxEntry.getReason().getID():"");
                        obj.put("istdsamount", noteTaxEntry.isTDSAmount());
                        obj.put("description", StringUtil.DecodeText(noteTaxEntry.getDescription()));
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
                        KwlReturnObject idcustresult = accDebitNoteobj.geDebitNoteCustomData(invDetailRequestParams);
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
                            json.put("debitNoteTaxEntry", noteTaxEntry.getID());
                            KwlReturnObject result6 = accDebitNoteobj.getDebitNoteDetailTermMap(json);
                            if (result6.getEntityList() != null && result6.getEntityList().size() > 0 && result6.getEntityList().get(0) != null) {
                                ArrayList<DebitNoteDetailTermMap> productTermDetail = (ArrayList<DebitNoteDetailTermMap>) result6.getEntityList();
                                JSONArray productTermJsonArry = new JSONArray();
                                double termAccount = 0.0;
                                for (DebitNoteDetailTermMap productTermsMapObj : productTermDetail) {
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
                                    if(productTermsMapObj.getEntitybasedLineLevelTermRate()!=null && productTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms()!=null && productTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getPayableAccount()!=null){     //SDP-12993
                                        productTermJsonObj.put("payableaccountid", (productTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getPayableAccount().getID()));
                                    } else {
                                        productTermJsonObj.put("payableaccountid", "");
                                    }
                                    productTermJsonArry.put(productTermJsonObj);
                                    termAccount += productTermsMapObj.getTermamount();
                                }
                                obj.put("LineTermdetails", productTermJsonArry.toString());
                                obj.put("recTermAmount", termAccount);
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
                } else {// in case of if tax is not included while creation of DN Value will be go fron jedetail table in case of Edit.
                    JournalEntry je = dn.getJournalEntry();
                    Set<JournalEntryDetail> jeDetails = je.getDetails();
                    for (JournalEntryDetail jed : jeDetails) {
                        JSONObject obj = new JSONObject();
                        if (!jed.isDebit()) {
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
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }*/ catch (JSONException ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return jobj;
    }

    public ModelAndView getDebitNoteRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        String companyid = "";
        try {
            boolean isForReport = false;
            companyid = sessionHandlerImpl.getCompanyid(request);
            String dtype = request.getParameter("dtype");
            if (!StringUtil.isNullOrEmpty(dtype) && dtype.equals("report")) {
                isForReport = true;
            }
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
            requestParams.put("bills", StringUtil.isNullOrEmpty(request.getParameter("bills"))?null:request.getParameter("bills").split(","));
            requestParams.put("df", authHandler.getDateOnlyFormat(request));
            requestParams.put("df", authHandler.getDateOnlyFormat(request));
            requestParams.put("companyid", companyid);
            requestParams.put("isForReport", isForReport);
            if (!StringUtil.isNullOrEmpty(Constants.isEdit)) {
                requestParams.put(Constants.isEdit, Boolean.parseBoolean(request.getParameter(Constants.isEdit)));
            }
            
            DateFormat userdateformatter = authHandlerDAOObj.getUserDateFormatter(sessionHandlerImpl.getDateFormatID(request), sessionHandlerImpl.getTimeZoneDifference(request), true);
            requestParams.put("userdateformatter", userdateformatter);
            
            JSONArray DataJArr = accDebitNoteService.getDebitNoteRowsJson(requestParams);
            jobj.put("data", DataJArr);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (Exception ex) {
            issuccess = false;
            msg = "accDebitNoteController.getDebitNoteRows : " + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getBillingDebitNoteRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
            requestParams.put("bills", request.getParameterValues("bills"));

            JSONArray DataJArr = getBillingDebitNoteRowsJson(requestParams);
            jobj.put("data", DataJArr);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (Exception ex) {
            issuccess = false;
            msg = "accDebitNoteController.getBillingDebitNoteRows : " + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getBillingDebitNoteRowsJson(HashMap<String, Object> requestParams) throws ServiceException {
        JSONArray JArr = new JSONArray();
        try {
//            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

//            String[] creditNote = request.getParameterValues("bills");
            String[] creditNote = (String[]) requestParams.get("bills");
            int i = 0;

            HashMap<String, Object> dnRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("debitNote.ID");
            order_by.add("srno");
            order_type.add("asc");
            dnRequestParams.put("filter_names", filter_names);
            dnRequestParams.put("filter_params", filter_params);
            dnRequestParams.put("order_by", order_by);
            dnRequestParams.put("order_type", order_type);

            while (creditNote != null && i < creditNote.length) {
//                BillingDebitNote dn = (BillingDebitNote) session.get(BillingDebitNote.class, creditNote[i]);
                KwlReturnObject dnresult = accountingHandlerDAOobj.getObject(BillingDebitNote.class.getName(), creditNote[i]);
                BillingDebitNote dn = (BillingDebitNote) dnresult.getEntityList().get(0);
//                Iterator itr = dn.getRows().iterator();
                filter_params.clear();
                filter_params.add(dn.getID());
                KwlReturnObject grdresult = accDebitNoteobj.getBillingDebitNoteDetails(dnRequestParams);
                Iterator itr = grdresult.getEntityList().iterator();

                while (itr.hasNext()) {
                    BillingDebitNoteDetail row = (BillingDebitNoteDetail) itr.next();
                    BillingGoodsReceiptDetail grRow = row.getGoodsReceiptRow();
                    JSONObject obj = new JSONObject();
                    obj.put("withoutinventory", true);
                    obj.put("billid", dn.getID());
                    obj.put("billno", dn.getDebitNoteNumber());
                    obj.put("srno", row.getSrno());
                    obj.put("rowid", row.getID());
                    obj.put("productid", grRow.getProductDetail());
                    obj.put("productdetail", grRow.getProductDetail());
                    obj.put("desc", grRow.getProductDetail());
                    obj.put("memo", row.getMemo());
                    obj.put("currencysymbol", (dn.getCurrency() == null ? currency.getSymbol() : dn.getCurrency().getSymbol()));
                    obj.put("transectionid", grRow.getBillingGoodsReceipt().getID());
                    obj.put("transectionno", grRow.getBillingGoodsReceipt().getBillingGoodsReceiptNumber());
                    obj.put("remark", !StringUtil.isNullOrEmpty(row.getRemark()) ? row.getRemark() : "");
                    Discount disc = row.getDiscount();
                    if (disc != null) {
                        obj.put("discount", disc.getDiscountValue());
                    } else {
                        obj.put("discount", 0);
                    }
                    obj.put("quantity", row.getQuantity());
                    obj.put("taxamount", row.getTaxAmount());
                    JArr.put(obj);
                }
                i++;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accDebitNoteController.getBillingDebitNoteRowsJson : " + ex.getMessage(), ex);
        }
        return JArr;
    }

    public ModelAndView deleteDebitNotes(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("DN_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            deleteDebitNotes(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.debitN.del", null, RequestContextUtils.getLocale(request));   //"Debit Note(s) has been deleted successfully";
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView deleteBillingDebitNotes(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("DN_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            deleteBillingDebitNotes(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.debitN.billDel", null, RequestContextUtils.getLocale(request));   //"Billing Debit Note(s) has been deleted successfully";
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void deleteDebitNotes(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
//        ArrayList params = new ArrayList();
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
//            String qMarks = "";
            String companyid = sessionHandlerImpl.getCompanyid(request);
//            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));

            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("noteid"))) {
                    String dnid = StringUtil.DecodeText(jobj.optString("noteid"));
//                    params.add(jobj.getString("noteid"));
//                    qMarks += "?,";
//                    query = "update DebitNote set deleted=true where ID in("+qMarks +") and company.companyID=?";
//                    HibernateUtil.executeUpdate(session, query, params.toArray());
                    KwlReturnObject result = accDebitNoteobj.deleteDebitNote(dnid, companyid);
                    
                    //Deleteing Unrealised Entery for Debit Note
                    accJournalEntryobj.permanentDeleteJournalEntryDetailReval(dnid, companyid);
                    accJournalEntryobj.permanentDeleteJournalEntryReval(dnid, companyid);

//                    query = "update JournalEntry je set je.deleted=true  where je.ID in(select dn.journalEntry.ID from DebitNote dn where dn.ID in( " + qMarks + ") and dn.company.companyID=je.company.companyID) and je.company.companyID=?";
//                    HibernateUtil.executeUpdate(session, query, params.toArray());
                    result = accDebitNoteobj.getJEFromDN(dnid);
                    List list = result.getEntityList();
                    Iterator itr = list.iterator();
                    while (itr.hasNext()) {
                        String jeid = (String) itr.next();
                        result = accJournalEntryobj.deleteJournalEntry(jeid, companyid);
                        //Delete entry from optimized table
                        accJournalEntryobj.deleteAccountJEs_optimized(jeid);
                    }

//                    query = "update Discount di set di.deleted=true  where di.ID in(select dnd.discount.ID from DebitNoteDiscount dnd where dnd.debitNote.ID in( " + qMarks + ") and dnd.company.companyID=di.company.companyID) and di.company.companyID=?";
//                    HibernateUtil.executeUpdate(session, query, params.toArray());
                    result = accDebitNoteobj.getDNDFromDN(dnid);
                    list = result.getEntityList();
                    itr = list.iterator();
                    while (itr.hasNext()) {
                        String discountid = (String) itr.next();
                        result = accDiscountobj.deleteDiscountEntry(discountid, companyid);
                    }

                    /*
                     * query = "update Discount di set di.deleted=true where
                     * di.ID in(select dnd.discount.ID from DebitNoteDetail dnd
                     * where dnd.debitNote.ID in( " + qMarks + ") and
                     * dnd.company.companyID=di.company.companyID) and
                     * di.company.companyID=?";
                     * HibernateUtil.executeUpdate(session, query,
                     * params.toArray());
                     */
                    result = accDebitNoteobj.getDNDIFromDN(dnid);
                    list = result.getEntityList();
                    itr = list.iterator();
                    while (itr.hasNext()) {
                        String discountid = (String) itr.next();
                        result = accDiscountobj.deleteDiscountEntry(discountid, companyid);
                    }
                    /*
                     * query = "update Inventory inv set inv.deleted=true where
                     * inv.ID in(select dnd.inventory.ID from DebitNoteDetail
                     * dnd where dnd.debitNote.ID in( " + qMarks + ") and
                     * dnd.company.companyID=inv.company.companyID) and
                     * inv.company.companyID=?";
                     * HibernateUtil.executeUpdate(session, query,
                     * params.toArray());
                     */
                    result = accDebitNoteobj.getDNDInvFromDN(dnid);
                    list = result.getEntityList();
                    itr = list.iterator();
                    while (itr.hasNext()) {
                        String inventoryid = (String) itr.next();
                            result = accProductObj.deleteInventoryEntry(inventoryid, companyid);
                        }
                    }
                }
//            params.add(company.getCompanyID());
//            qMarks = qMarks.substring(0, Math.max(0, qMarks.length() - 1));
//            String query;
//            List list;
        } /*catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        }*/ catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)));
        }
    }

    public void deleteBillingDebitNotes(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
//        ArrayList params = new ArrayList();
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
//            String qMarks = "";
//            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
            String companyid = sessionHandlerImpl.getCompanyid(request);

            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("noteid"))) {
                    String bdnid = StringUtil.DecodeText(jobj.optString("noteid"));
//                    params.add(StringUtil.DecodeText(jobj.optString("noteid")));
//                    qMarks += "?,";
//                }
//            }
//            params.add(company.getCompanyID());
//            qMarks = qMarks.substring(0, Math.max(0, qMarks.length() - 1));
//            String query;
//            List list;
//            query = "update BillingDebitNote set deleted=true where ID in("+qMarks +") and company.companyID=?";
//            HibernateUtil.executeUpdate(session, query, params.toArray());
                    KwlReturnObject result = accDebitNoteobj.deleteBillingDebitNote(bdnid, companyid);


                    //query = "update JournalEntry je set je.deleted=true  where je.ID in(select dn.journalEntry.ID from BillingDebitNote dn where dn.ID in( " + qMarks + ") and dn.company.companyID=je.company.companyID) and je.company.companyID=?";
                    result = accDebitNoteobj.getJEFromBDN(bdnid, companyid);
                    List list = result.getEntityList();
                    Iterator itr = list.iterator();
                    while (itr.hasNext()) {
                        String jeid = ((BillingDebitNote) itr.next()).getJournalEntry().getID();
                        result = accJournalEntryobj.deleteJournalEntry(jeid, companyid);
                        //Delete entry from optimized table
                        accJournalEntryobj.deleteAccountJEs_optimized(jeid);
                    }

//            query = "update Discount di set di.deleted=true  where di.ID in(select dnd.discount.ID from BillingDebitNoteDiscount dnd where dnd.debitNote.ID in( " + qMarks + ") and dnd.company.companyID=di.company.companyID) and di.company.companyID=?";
                    result = accDebitNoteobj.getDNDFromBDN(bdnid, companyid);
                    list = result.getEntityList();
                    itr = list.iterator();
                    while (itr.hasNext()) {
                        String discountid = ((BillingDebitNoteDiscount) itr.next()).getDiscount().getID();
                        result = accDiscountobj.deleteDiscountEntry(discountid, companyid);
                    }

//            query = "update Discount di set di.deleted=true  where di.ID in(select dnd.discount.ID from BillingDebitNoteDetail dnd where dnd.debitNote.ID in( " + qMarks + ") and dnd.company.companyID=di.company.companyID) and di.company.companyID=?";
                    result = accDebitNoteobj.getDNDFromBDND(bdnid, companyid);
                    list = result.getEntityList();
                    itr = list.iterator();
                    while (itr.hasNext()) {
                        String discountid = ((BillingDebitNoteDetail) itr.next()).getDiscount().getID();
                        result = accDiscountobj.deleteDiscountEntry(discountid, companyid);
                    }
                }
            }
        }/* catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        }*/ catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)));
        }
    }

    public ModelAndView exportDebitNote(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            HashMap<String, Object> requestParams = gettDebitNoteMap(request);
            KwlReturnObject result = accDebitNoteobj.getDebitNotes(requestParams);
            JSONArray DataJArr = getDebitNotesJson(requestParams, result.getEntityList());
            jobj.put("data", DataJArr);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    public ModelAndView exportBillingDebitNote(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            HashMap<String, Object> requestParams = gettDebitNoteMap(request);
            KwlReturnObject result = accDebitNoteobj.getBillingDebitNotes(requestParams);
            JSONArray DataJArr = getBillingDebitNotesJson(requestParams, result.getEntityList());
            jobj.put("data", DataJArr);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateOnlyFormat(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    public boolean getInvoiceStatusForGRO(GoodsReceiptDetail iDetail) throws ServiceException {
        boolean updateInventoryFlag = false;
        KwlReturnObject idresult = accDebitNoteobj.getGDOIDFromVendorInvoiceDetails(iDetail.getID());
        List list = idresult.getEntityList();
        Iterator ite1 = list.iterator();
        int qua = 0;
        if (ite1.hasNext()) {
            updateInventoryFlag = true;
        }

        return updateInventoryFlag;
    }

    public ModelAndView linkDebitNote(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
             HashMap<String,Object> requestParams = new HashMap<>();
            List li = accDebitNoteService.linkDebitNote(request, "",true,requestParams);   // "true" flag is passed for inserting Audit Trial entry ( ERP-18558 )
            issuccess = true;
            msg = messageSource.getMessage("acc.field.DebitNotehasbeenLinkedtoVendorInvoicesuccessfully", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /*
     * Update invoice amount due amount.
     */
    public void updateInvoiceAmountDue(GoodsReceipt goodsReceipt, Company company, double amountReceivedForGoodsReceipt, double baseAmountReceivedForGoodsReceipt) throws JSONException, ServiceException {
        if (goodsReceipt != null) {
            double invoiceAmountDue = goodsReceipt.getOpeningBalanceAmountDue();
            invoiceAmountDue -= amountReceivedForGoodsReceipt;
            Map<String, Object> greceipthm = new HashMap<String, Object>();
            greceipthm.put("grid", goodsReceipt.getID());;
            greceipthm.put("companyid", company.getCompanyID());
            greceipthm.put("openingBalanceAmountDue", invoiceAmountDue);
            greceipthm.put(Constants.openingBalanceBaseAmountDue, goodsReceipt.getOpeningBalanceBaseAmountDue() - baseAmountReceivedForGoodsReceipt);
            greceipthm.put(Constants.invoiceamountdue, goodsReceipt.getInvoiceamountdue() - amountReceivedForGoodsReceipt);
            greceipthm.put(Constants.invoiceamountdueinbase, goodsReceipt.getInvoiceAmountDueInBase()- baseAmountReceivedForGoodsReceipt);
            accGoodsReceiptobj.updateGoodsReceipt(greceipthm);
        }
    }

    private List saveDebitNoteRowsOW(HashMap GlobalParams, HttpServletRequest request, Company company, KWLCurrency currency, JournalEntry je, CompanyAccountPreferences preferences, double externalCurrencyRate) throws JSONException, ServiceException, SessionExpiredException, ParseException, AccountingException {
        List resultlist = new ArrayList();
        double cnamount = 0.0;
        HashSet cndetails = new HashSet();
        HashSet jedetails = new HashSet();
        HashSet dnTaxEntryDetails = new HashSet();
        Boolean isCopy = request.getParameter("isCopy") != null ? Boolean.parseBoolean(request.getParameter("isCopy")) : false;
        JournalEntryDetail jed;
        boolean reloadInventory = false;
        String customerAccountId = request.getAttribute("customerAccountId")!=null ? (String)request.getAttribute("customerAccountId"):"";
        String countryId = company.getCountry() != null ? company.getCountry().getID() : null;
//        String vendorentry="";
        int i = 0;
        String details = request.getParameter("details");
        if (!StringUtil.isNullOrEmpty(details)) {
            JSONArray jArr = new JSONArray(details);
            for (int iter = 0; iter < jArr.length(); iter++) {
                JSONObject jobj = jArr.getJSONObject(iter);
                String DebitNoteDetailID = StringUtil.generateUUID();
                DebitNoteDetail row = new DebitNoteDetail();
                row.setSrno(i + 1);
                row.setID(DebitNoteDetailID);
                row.setTotalDiscount(0.00);
                row.setCompany(company);
                row.setMemo(StringUtil.DecodeText(jobj.optString("description")));
                String purchase_accid = "";
                boolean isTDSApplied = jobj.optBoolean("istdsamount",false);
                DebitNoteTaxEntry taxEntry = new DebitNoteTaxEntry();
                String DebitNoteTaxID = StringUtil.generateUUID();
                taxEntry.setRateIncludingGst(jobj.optDouble("rateIncludingGst"));
                taxEntry.setID(DebitNoteTaxID);
                int cntype = StringUtil.isNullOrEmpty(request.getParameter("cntype")) ? 1 : Integer.parseInt(request.getParameter("cntype"));
                int srNoForRow = StringUtil.isNullOrEmpty("srNoForRow") ? 0 : Integer.parseInt(jobj.getString("srNoForRow"));
                boolean isdebit = jobj.has("debit") ? Boolean.parseBoolean(jobj.optString("debit","false")) : false;
                if (cntype == 1 || cntype == 4 || cntype == 2) {//DN against customer
                    if (!StringUtil.isNullOrEmpty(jobj.optString("accountid"))) {
                        purchase_accid = jobj.optString("accountid");
                    } else {
                        throw new AccountingException(messageSource.getMessage("acc.field.Nocreditaccountselected", null, RequestContextUtils.getLocale(request)));
                    }
                } else {
                    KwlReturnObject dscresult = this.accAccountDAOobj.getAccountFromName(company.getCompanyID(), Constants.PURCHASE_ACCOUNT);
                    List ll = dscresult.getEntityList();
                    if (ll.size() == 1) {
                        purchase_accid = ((Account) ll.get(0)).getID();
                    } else {
                        throw new AccountingException(messageSource.getMessage("acc.field.NoPurchaseaccountfound", null, RequestContextUtils.getLocale(request)));
                    }
                }
                if (!isdebit) {
                    /*
                     *calculate cnamount when including GST check is enable
                     */
                    if (Boolean.parseBoolean(request.getParameter("includingGST"))) {
                        cnamount += Double.parseDouble(jobj.optString("rateIncludingGst"));//jobj.getDouble("discamount");// Line amount excluding GST
                    } else {
                        cnamount += Double.parseDouble(jobj.optString("dramount"));//jobj.getDouble("discamount");        
                    }
                } else {
                    if (Boolean.parseBoolean(request.getParameter("includingGST"))) {
                        cnamount -= Double.parseDouble(jobj.optString("rateIncludingGst"));//jobj.getDouble("discamount");// Line amount excluding GST
                    } else {
                        cnamount -= Double.parseDouble(jobj.optString("dramount"));//jobj.getDouble("discamount");        
                    }
                }
                
                double amount = 0d;
                JSONObject jedjson = new JSONObject();
                JSONObject sepatratedjedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put("companyid", company.getCompanyID());
                if (Boolean.parseBoolean(request.getParameter("includingGST"))) {
                    amount = Double.parseDouble(jobj.optString("rateIncludingGst"));
                } else {
                    amount = Double.parseDouble(jobj.optString("dramount"));
                }
                jedjson.put("amount", amount);
                jedjson.put("accountid", purchase_accid);
                jedjson.put("debit", isdebit);
                jedjson.put("jeid", je.getID());
                jedjson.put("description", jobj.optString("description"));
                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jedetails.add(jed);
                // Add Custom fields details 
                if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", ""))) {
                    /*
                     * Posting additional jedetail against control account for
                     * matching balance sheet in advance search.
                     */
                    sepatratedjedjson = new JSONObject();
                    sepatratedjedjson.put("srno", jedetails.size() + 1);
                    sepatratedjedjson.put("companyid", company.getCompanyID());
                    sepatratedjedjson.put("amount", amount);
                    sepatratedjedjson.put("accountid", customerAccountId);
                    sepatratedjedjson.put("debit", !isdebit);
                    sepatratedjedjson.put("jeid", je.getID());
                    sepatratedjedjson.put("mainjedid", jed.getID());
                    sepatratedjedjson.put("description", jobj.optString("description"));
                    sepatratedjedjson.put(Constants.ISSEPARATED, true);
                    jedresult = accJournalEntryobj.addJournalEntryDetails(sepatratedjedjson);
                    JournalEntryDetail pmAmountJed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(pmAmountJed);

                    JSONArray jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
                    HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                    customrequestParams.put("customarray", jcustomarray);
                    customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                    customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                    customrequestParams.put("modulerecid", pmAmountJed.getID());
                    customrequestParams.put("recdetailId", taxEntry.getID());
                    customrequestParams.put("moduleid", Constants.Acc_Debit_Note_ModuleId);
                    customrequestParams.put("companyid", company.getCompanyID());
                    customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                    KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        JSONObject tempjedjson = new JSONObject();
                        tempjedjson.put("accjedetailcustomdata", pmAmountJed.getID());
                        tempjedjson.put("jedid", pmAmountJed.getID());
                        accJournalEntryobj.updateJournalEntryDetails(tempjedjson);
                    }

                    customrequestParams.put("modulerecid", jed.getID());
                    customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        JSONObject tempjedjson = new JSONObject();
                        tempjedjson.put("accjedetailcustomdata", jed.getID());
                        tempjedjson.put("jedid", jed.getID());
                        jedresult = accJournalEntryobj.updateJournalEntryDetails(tempjedjson);
                    }
                }
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                row.setTotalJED(jed);
                taxEntry.setTotalJED(jed);
                String rowtaxid = jobj.optString("prtaxid", "");
                double rowtaxamount = 0d;
                double gstCurrencyRate = 0d;
                String rowTaxJeId = "";
                KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), rowtaxid);
                Tax rowtax = (Tax) txresult.getEntityList().get(0);
                if (rowtax != null) {
//                     DecimalFormat f = new DecimalFormat("##.00");
                    if(jobj.has("taxamount") && !StringUtil.isNullOrEmpty(jobj.getString("taxamount"))){
                        rowtaxamount = Double.parseDouble(jobj.optString("taxamount", "0.0"));
                    }
                    if(!StringUtil.isNullOrEmpty(jobj.optString("gstCurrencyRate", "0.0")))
                        gstCurrencyRate = Double.parseDouble(jobj.optString("gstCurrencyRate", "0.0"));
                    jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", company.getCompanyID());
                    jedjson.put("amount", authHandler.formattedAmount(rowtaxamount, company.getCompanyID()));
                    jedjson.put("accountid", rowtax.getAccount().getID());
                    jedjson.put("debit", isdebit);
                    jedjson.put("jeid", je.getID());
                    jedjson.put("description", jobj.optString("description"));
                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                    // Add Custom fields details 
                    if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", ""))) {
                        /*
                         * Posting additional jedetail against control account
                         * for matching balance sheet in advance search.
                         */
                        sepatratedjedjson = new JSONObject();
                        sepatratedjedjson.put("srno", jedetails.size() + 1);
                        sepatratedjedjson.put("companyid", company.getCompanyID());
                        sepatratedjedjson.put("amount", authHandler.formattedAmount(rowtaxamount, company.getCompanyID()));
                        sepatratedjedjson.put("accountid", customerAccountId);
                        sepatratedjedjson.put("debit", !isdebit);
                        sepatratedjedjson.put("jeid", je.getID());
                        sepatratedjedjson.put("mainjedid", jed.getID());
                        sepatratedjedjson.put("description", jobj.optString("description"));
                        sepatratedjedjson.put(Constants.ISSEPARATED, true);
                        jedresult = accJournalEntryobj.addJournalEntryDetails(sepatratedjedjson);
                        JournalEntryDetail pmTaxAmountJed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jedetails.add(pmTaxAmountJed);
                        
                        JSONArray jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                        customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                        customrequestParams.put("modulerecid", pmTaxAmountJed.getID());
                        customrequestParams.put("recdetailId", taxEntry.getID());
                        customrequestParams.put("moduleid", Constants.Acc_Debit_Note_ModuleId);
                        customrequestParams.put("companyid", company.getCompanyID());
                        customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            JSONObject tempjedjson = new JSONObject();
                            tempjedjson.put("accjedetailcustomdata", pmTaxAmountJed.getID());
                            tempjedjson.put("jedid", pmTaxAmountJed.getID());
                            accJournalEntryobj.updateJournalEntryDetails(tempjedjson);
                        }
                        
                        customrequestParams.put("modulerecid", jed.getID());
                        customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            JSONObject tempjedjson = new JSONObject();
                            tempjedjson.put("accjedetailcustomdata", jed.getID());
                            tempjedjson.put("jedid", jed.getID());
                            jedresult = accJournalEntryobj.updateJournalEntryDetails(tempjedjson);
                        }
                    }
                    rowTaxJeId = jed.getID();
                    if (!isdebit) {
                    cnamount += rowtaxamount;
                    } else {
                        cnamount -= rowtaxamount;
                    }
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    row.setGstJED(jed);
                    taxEntry.setGstJED(jed);
                }

                if (iter == 0) {// create  dndetail entry only once in this case i.e if multitple accounts are linked.
                    cndetails.add(row);
                }
                
                KwlReturnObject accountresult = accountingHandlerDAOobj.getObject(Account.class.getName(), purchase_accid);
                Account account = (Account) accountresult.getEntityList().get(0);

                taxEntry.setAccount(account);
                taxEntry.setAmount((Double.parseDouble(jobj.optString("dramount"))));
                taxEntry.setCompany(company);
//                    taxEntry.setCreditNote(null);
                taxEntry.setDescription(StringUtil.DecodeText(jobj.optString("description")));
                if (!StringUtil.isNullOrEmpty(jobj.optString("reason", ""))) {
                    KwlReturnObject reasonresult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), jobj.optString("reason", ""));
                    MasterItem reason = (MasterItem) reasonresult.getEntityList().get(0);
                    taxEntry.setReason(reason);
                }
                taxEntry.setIsForDetailsAccount(true);
                taxEntry.setDebitForMultiCNDN(isdebit);
                taxEntry.setTax(rowtax);
                taxEntry.setTDSAmount(isTDSApplied);
                taxEntry.setTaxJedId(rowTaxJeId);
                taxEntry.setTaxamount(rowtaxamount);
                taxEntry.setGstCurrencyRate(gstCurrencyRate);
                taxEntry.setSrNoForRow(srNoForRow);
                taxEntry.setProductid(jobj.optString("productid", null));
                
                if (!StringUtil.isNullOrEmpty(countryId) && countryId.equalsIgnoreCase("" + Constants.indian_country_id) && jobj.has("LineTermdetails") && !StringUtil.isNullOrEmpty((String) jobj.optString("LineTermdetails"))) {
                    JSONArray termsArray = new JSONArray(StringUtil.DecodeText((String) jobj.optString("LineTermdetails")));
                    double termAmount = 0.0;
                    for (int j = 0; j < termsArray.length(); j++) {
                        HashMap<String, Object> purchaseOrderDetailsTermsMap = new HashMap<>();
                        JSONObject termObject = termsArray.getJSONObject(j);
                        if (termObject.has("termid")) {
                            purchaseOrderDetailsTermsMap.put("term", termObject.get("termid"));
                        }
                        if (termObject.has("termamount")) {
                            purchaseOrderDetailsTermsMap.put("termamount",termObject.optDouble("termamount",0.0));
                            termAmount += termObject.optDouble("termamount",0.0);
                        }
                        if (termObject.has("termpercentage")) {
                            purchaseOrderDetailsTermsMap.put("termpercentage", termObject.get("termpercentage"));
                        }
                        if (termObject.has("purchasevalueorsalevalue")) {
                            purchaseOrderDetailsTermsMap.put("purchasevalueorsalevalue", termObject.get("purchasevalueorsalevalue"));
                        }
                        if (termObject.has("deductionorabatementpercent")) {
                            purchaseOrderDetailsTermsMap.put("deductionorabatementpercent", termObject.get("deductionorabatementpercent"));
                        }
                        if (termObject.has("assessablevalue")) {
                            purchaseOrderDetailsTermsMap.put("assessablevalue", termObject.get("assessablevalue"));
                        }
                        if (termObject.has("taxtype") && !StringUtil.isNullOrEmpty(termObject.getString("taxtype"))) {
                            purchaseOrderDetailsTermsMap.put("taxtype", termObject.getInt("taxtype"));
                            if (termObject.has("taxvalue") && !StringUtil.isNullOrEmpty(termObject.getString("taxvalue"))) {
                                if (termObject.getInt("taxtype") == 0) { // If Flat
                                    purchaseOrderDetailsTermsMap.put("termamount", termObject.getDouble("taxvalue"));
                                } else { // Else Percentage
                                    purchaseOrderDetailsTermsMap.put("termpercentage", termObject.getDouble("taxvalue"));
                                }
                            }
                        }
                        if (termObject.has("id")) {
                            purchaseOrderDetailsTermsMap.put("id", isCopy ? "" : termObject.get("id"));
                        }
                        purchaseOrderDetailsTermsMap.put("debitNoteTaxEntry", taxEntry.getID());
                        purchaseOrderDetailsTermsMap.put("isDefault", termObject.optString("isDefault", "false"));
                        purchaseOrderDetailsTermsMap.put("productentitytermid", termObject.optString("productentitytermid"));
                        purchaseOrderDetailsTermsMap.put("userid", sessionHandlerImpl.getUserid(request));
                        purchaseOrderDetailsTermsMap.put("product", termObject.opt("productid"));
                        purchaseOrderDetailsTermsMap.put("createdOn", new Date());
                        KwlReturnObject dnDetailTermMapResult = accDebitNoteobj.saveDebitNoteDetailTermMap(purchaseOrderDetailsTermsMap);
                        DebitNoteDetailTermMap debitNoteDetailTermMap = dnDetailTermMapResult.getEntityList() != null && dnDetailTermMapResult.getEntityList().size() > 0 ? (DebitNoteDetailTermMap) dnDetailTermMapResult.getEntityList().get(0) : null;
                        if (debitNoteDetailTermMap != null && debitNoteDetailTermMap.getEntitybasedLineLevelTermRate() != null && debitNoteDetailTermMap.getEntitybasedLineLevelTermRate().getLineLevelTerms() != null && debitNoteDetailTermMap.getEntitybasedLineLevelTermRate().getLineLevelTerms().getAccount() != null) {
                            jedjson = new JSONObject();
                            jedjson.put("srno", jedetails.size() + 1);
                            jedjson.put("companyid", company.getCompanyID());
                            jedjson.put("amount", termObject.optDouble("termamount",0.0));
                            jedjson.put("accountid", debitNoteDetailTermMap.getEntitybasedLineLevelTermRate().getLineLevelTerms().getAccount().getID());
                            jedjson.put("debit", isdebit);
                            jedjson.put("jeid", je.getID());
                            jedjson.put("description", jobj.optString("description"));
                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jedetails.add(jed);
                        }
                        if (!isdebit) {
                            cnamount += termObject.optDouble("termamount",0.0);
                        } else {
                            cnamount -= termObject.optDouble("termamount",0.0);
                        }
                    }
                    taxEntry.setTermAmount(termAmount);
                }
                if (!StringUtil.isNullOrEmpty(countryId) && countryId.equalsIgnoreCase("" + Constants.indian_country_id)) {
                    /**
                     * Save GST History Customer/Vendor data.
                     */
                    jobj.put("detaildocid", taxEntry.getID());
                    jobj.put("moduleid", Constants.Acc_Debit_Note_ModuleId);
                    fieldDataManagercntrl.createRequestMapToSaveTaxClassHistory(jobj);
                }
                dnTaxEntryDetails.add(taxEntry);
                

                i++;
            }

        } else {
            DebitNoteDetail row = new DebitNoteDetail();
            String DebitNoteDetailID = StringUtil.generateUUID();
            row.setID(DebitNoteDetailID);
            row.setSrno(i + 1);
            row.setTotalDiscount(0.00);
            row.setCompany(company);
            row.setMemo(request.getParameter("memo"));
            String purchase_accid = "";
            int cntype = StringUtil.isNullOrEmpty(request.getParameter("cntype")) ? 1 : Integer.parseInt(request.getParameter("cntype"));
            if (cntype == 4 || cntype == 2) {//DN against customer
                if (!StringUtil.isNullOrEmpty(request.getParameter("reverseaccid"))) {
                    purchase_accid = request.getParameter("reverseaccid");
                } else {
                    throw new AccountingException(messageSource.getMessage("acc.field.Nocreditaccountselected", null, RequestContextUtils.getLocale(request)));
                }
            } else {
                KwlReturnObject dscresult = this.accAccountDAOobj.getAccountFromName(company.getCompanyID(), Constants.PURCHASE_ACCOUNT);
                List ll = dscresult.getEntityList();
                if (ll.size() == 1) {
                    purchase_accid = ((Account) ll.get(0)).getID();
                } else {
                    throw new AccountingException(messageSource.getMessage("acc.field.NoPurchaseaccountfound", null, RequestContextUtils.getLocale(request)));
                }
            }
            cnamount = Double.parseDouble(request.getParameter("amount"));//jobj.getDouble("discamount");

            //        JSONObject discjson = new JSONObject();
            //        discjson.put("discount", cnamount);
            //        discjson.put("inpercent", false);
            ////        KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, cnamount, currencyid, null, externalCurrencyRate);
            //        discjson.put("originalamount", 0);//(Double) bAmt.getEntityList().get(0));
            //        discjson.put("companyid", company.getCompanyID());
            //        dscresult = accDiscountobj.addDiscount(discjson);
            //        Discount discount = (Discount) dscresult.getEntityList().get(0);
            //        row.setDiscount(discount);
            cndetails.add(row);

            JSONObject jedjson = new JSONObject();
            jedjson.put("srno", jedetails.size() + 1);
            jedjson.put("companyid", company.getCompanyID());
            jedjson.put("amount", (cnamount));
            jedjson.put("accountid", purchase_accid);
            jedjson.put("debit", false);
            jedjson.put("jeid", je.getID());
            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetails.add(jed);
//            vendorentry=jed.getID();
        }
        resultlist.add(cnamount);  //resultlist.add(totalAmount + totalTax);
        resultlist.add(cnamount);
        resultlist.add(cndetails);
        resultlist.add(jedetails);
        resultlist.add(reloadInventory);
        resultlist.add(dnTaxEntryDetails);
//        resultlist.add(vendorentry);
        return resultlist;
    }

    public List saveDebitNoteRows1(HashMap<String, Object> GlobalParams, HttpServletRequest request, Company company, KWLCurrency currency, JournalEntry je, CompanyAccountPreferences preferences, double externalCurrencyRate) throws JSONException, ServiceException, SessionExpiredException, ParseException {
        List resultlist = new ArrayList();
        double totalAmount = 0;
        double totalTax = 0, prodTax = 0;
        double discAccAmount = 0, discTotal = 0;
        HashSet dndetails = new HashSet();
        HashSet jedetails = new HashSet();
        boolean reloadInventory = false;
        JournalEntryDetail jed;
        KwlReturnObject result;

        double debitAmount = 0, creditAmount = 0;
        String cntype = StringUtil.isNullOrEmpty(request.getParameter("cntype")) ? "1" : request.getParameter("cntype");

        double totalInvoiceDiscount = StringUtil.getDouble(request.getParameter("totalInvoiceDiscount"));
        boolean includeTax = StringUtil.getBoolean(request.getParameter("includetax"));
        String companyid = (String) GlobalParams.get("companyid");
        String currencyid = (request.getParameter("currencyid") == null ? currency.getCurrencyID() : request.getParameter("currencyid"));
        JSONArray jArr = new JSONArray(request.getParameter("productdetails"));
        for (int i = 0; i < jArr.length(); i++) {
            double taxamount = 0;
            double amount = 0;
            prodTax = 0;
            JSONObject jobj = jArr.getJSONObject(i);
            DebitNoteDetail row = new DebitNoteDetail();
            String DebitNoteDetailID = StringUtil.generateUUID();
            row.setID(DebitNoteDetailID);
            row.setSrno(i + 1);
            double rowDiscountpercent = jobj.getDouble("prdiscount");
            double disc = jobj.getDouble("discamount");
            double rowDiscount = 0;
            discTotal = discTotal + disc;
            row.setCompany(company);
            row.setMemo(request.getParameter("memo"));
            row.setQuantity(jobj.getDouble("remquantity"));
            if (!StringUtil.isNullOrEmpty(jobj.getString("gridRemark"))) {
                row.setRemark(jobj.getString("gridRemark"));
            }
            String accountId = preferences.getDiscountReceived().getID();
            if (!StringUtil.isNullOrEmpty(jobj.getString("accountId"))) {
                accountId = jobj.getString("accountId");
            }
            row.setTotalDiscount(0.00);
            if (cntype.equals("3")) {
                row.setPaidinvflag(1);
            }

            if (preferences.isInventoryAccountingIntegration() && preferences.isUpdateInvLevel()) {
                if (!StringUtil.isNullOrEmpty(jobj.optString("invstore"))) {
                    row.setInvstoreid(jobj.optString("invstore"));
                } else {
                    row.setInvstoreid("");
                }
                if (!StringUtil.isNullOrEmpty(jobj.optString("invlocation"))) {
                    row.setInvlocid(jobj.optString("invlocation"));
                } else {
                    row.setInvlocid("");
                }
            }

            result = accountingHandlerDAOobj.getObject(GoodsReceiptDetail.class.getName(), (StringUtil.isNullOrEmpty(jobj.getString("linkto"))) ? jobj.getString("rowid") : jobj.getString("savedrowid"));
            GoodsReceiptDetail goodsReceiptRow = (GoodsReceiptDetail) result.getEntityList().get(0);

            row.setGoodsReceiptRow(goodsReceiptRow);
            Product product = goodsReceiptRow.getInventory().getProduct();
            result = accountingHandlerDAOobj.getObject(Account.class.getName(), product.getPurchaseReturnAccount().getID());

            double percent = 0;
            if (goodsReceiptRow.getGoodsReceipt().getTax() != null) {
                KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, goodsReceiptRow.getGoodsReceipt().getCreationDate(), goodsReceiptRow.getGoodsReceipt().getTax().getID());
                percent = (Double) perresult.getEntityList().get(0);
            }
            amount = jobj.getDouble("discamount");

            boolean updateInventoryFlag = goodsReceiptRow.getInventory().isInvrecord();

            if (preferences.isWithInvUpdate()) {
                updateInventoryFlag = getInvoiceStatusForGRO(goodsReceiptRow);
            }

            if (jobj.getInt("typeid") > 0) {
                reloadInventory = true;
                JSONObject inventoryjson = new JSONObject();
                inventoryjson.put("productid", product.getID());
                inventoryjson.put("invrecord", updateInventoryFlag ? true : false);
                double baseuomrate = 1;
                if (jobj.has("baseuomrate")) {
                    baseuomrate = jobj.getDouble("baseuomrate");
                }
                double quantity = jobj.getDouble("remquantity");
                if (jobj.has("uomid")) {
                    inventoryjson.put("uomid", jobj.getString("uomid"));
                }
                inventoryjson.put("quantity", quantity);
                inventoryjson.put("baseuomquantity", updateInventoryFlag ?  authHandler.calculateBaseUOMQuatity(quantity,baseuomrate, companyid) : 0);
                inventoryjson.put("actquantity", updateInventoryFlag ? 0 : authHandler.calculateBaseUOMQuatity(quantity,baseuomrate, companyid));
                inventoryjson.put("baseuomrate", baseuomrate);

                inventoryjson.put("description", jobj.optString("desc"));
                inventoryjson.put("carryin", false);
                inventoryjson.put("defective", true);
                inventoryjson.put("newinventory", false);
                inventoryjson.put("companyid", company.getCompanyID());
                inventoryjson.put("updatedate", authHandler.getDateOnlyFormat().parse(request.getParameter("creationdate")));
                KwlReturnObject invresult = accProductObj.addInventory(inventoryjson);
                Inventory inventory = (Inventory) invresult.getEntityList().get(0);
                row.setInventory(inventory);

                result = accountingHandlerDAOobj.getObject(GoodsReceiptDetail.class.getName(), (StringUtil.isNullOrEmpty(jobj.getString("linkto"))) ? jobj.getString("rowid") : jobj.getString("savedrowid"));
                GoodsReceiptDetail compGoodsReceiptRow = (GoodsReceiptDetail) result.getEntityList().get(0);
                Product compProduct = compGoodsReceiptRow.getInventory().getProduct();

                result = accountingHandlerDAOobj.getObject(Account.class.getName(), compProduct.getPurchaseReturnAccount().getID());
                if (disc > 0) {

                    if (includeTax && i == jArr.length() - 1) {
                        taxamount = (discTotal - (totalInvoiceDiscount)) * percent / 100;
                        row.setTaxAmount(taxamount);
                        totalTax += taxamount;
                        if (includeTax && taxamount > 0) {
                            creditAmount += taxamount;
                            JSONObject jedjson = new JSONObject();
                            jedjson.put("srno", jedetails.size() + 1);
                            jedjson.put("companyid", company.getCompanyID());
                            jedjson.put("amount", taxamount);
                            jedjson.put("accountid", goodsReceiptRow.getGoodsReceipt().getTax().getAccount().getID());
                            jedjson.put("debit", false);
                            jedjson.put("jeid", je.getID());
                            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jedetails.add(jed);
                        }
                    }
/////////////////////////////////////////////    Total Tax Over

/////////////////////////////////////////////    Total Discount if Exist
                    if (totalInvoiceDiscount > 0 && i == jArr.length() - 1) {
                        debitAmount += totalInvoiceDiscount;
                        row.setTotalDiscount(totalInvoiceDiscount);
                        JSONObject jedjson = new JSONObject();
                        jedjson.put("srno", jedetails.size() + 1);
                        jedjson.put("companyid", company.getCompanyID());
                        jedjson.put("amount", totalInvoiceDiscount);
                        jedjson.put("accountid", preferences.getDiscountReceived().getID());
                        jedjson.put("debit", true);
                        jedjson.put("jeid", je.getID());
                        KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jedetails.add(jed);
                    }
/////////////////////////////////////////////    Total Discount if Exist Over


/////////////////////////////////////////////    Discount Row Added
                    JSONObject discjson = new JSONObject();
                    discjson.put("discount", disc);
                    discjson.put("inpercent", false);
                    KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, jobj.getDouble("amount"), currencyid, null, externalCurrencyRate);
                    discjson.put("originalamount", (Double) bAmt.getEntityList().get(0));
                    discjson.put("companyid", company.getCompanyID());
                    KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                    Discount discount = (Discount) dscresult.getEntityList().get(0);
                    row.setDiscount(discount);
                    dndetails.add(row);
/////////////////////////////////////////////    Discount Row Added Over
                }
/////////////////////////////////////////////    Product Tax
                if (compGoodsReceiptRow.getTax() != null) {
                    /*
                     * Product level tax taken care of
                     */

                    KwlReturnObject perresult = accTaxObj.getTaxPercent(company.getCompanyID(), compGoodsReceiptRow.getGoodsReceipt().getCreationDate(), compGoodsReceiptRow.getTax().getID());
                    percent = (Double) perresult.getEntityList().get(0);
                    prodTax = percent * amount / (percent + 100);
                    creditAmount += prodTax;

                    JSONObject jedtaxjson = new JSONObject();
                    jedtaxjson.put("srno", jedetails.size() + 1);
                    jedtaxjson.put("companyid", company.getCompanyID());
                    jedtaxjson.put("amount", prodTax);
                    jedtaxjson.put("accountid", compGoodsReceiptRow.getTax().getAccount().getID());
                    jedtaxjson.put("debit", false);
                    jedtaxjson.put("jeid", je.getID());
                    KwlReturnObject jedtaxresult = accJournalEntryobj.addJournalEntryDetails(jedtaxjson);
                    jed = (JournalEntryDetail) jedtaxresult.getEntityList().get(0);
                    jedetails.add(jed);

                    /*
                     * Product level tax taken care of
                     */
                }
/////////////////////////////////////////////    Product Tax

                if (rowDiscountpercent > 0) {
                    rowDiscount = (rowDiscountpercent * (amount - prodTax)) / (100 - rowDiscountpercent);
                    debitAmount += rowDiscount;
/////////////////////////////////////////////    Product Row Discount Added
                    JSONObject jedjson1 = new JSONObject();
                    jedjson1.put("srno", jedetails.size() + 1);
                    jedjson1.put("companyid", company.getCompanyID());
                    jedjson1.put("amount", rowDiscount);
                    jedjson1.put("accountid", preferences.getDiscountReceived().getID());
                    jedjson1.put("debit", true);
                    jedjson1.put("jeid", je.getID());
                    KwlReturnObject jedresult1 = accJournalEntryobj.addJournalEntryDetails(jedjson1);
                    jed = (JournalEntryDetail) jedresult1.getEntityList().get(0);
                    jedetails.add(jed);

/////////////////////////////////////////////    Product Row Discount Added Over
                }

                creditAmount += (amount - prodTax + rowDiscount);
                JSONObject jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put("companyid", company.getCompanyID());
                jedjson.put("amount", (amount - prodTax + rowDiscount));
                jedjson.put("accountid", accountId);
                jedjson.put("debit", false);
                jedjson.put("jeid", je.getID());
                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);

                jedetails.add(jed);

                totalAmount = totalAmount + amount + taxamount;// - totalInvoiceDiscount;

            } else {

                discAccAmount = jobj.getDouble("discamount");

/////////////////////////////////////////////    Total Tax
                if (includeTax && i == jArr.length() - 1) {
                    taxamount = (discTotal - (totalInvoiceDiscount)) * percent / 100;
                    row.setTaxAmount(taxamount);
                    totalTax += taxamount;
                    if (includeTax && taxamount > 0) {
                        creditAmount += taxamount;
                        JSONObject jedjson = new JSONObject();
                        jedjson.put("srno", jedetails.size() + 1);
                        jedjson.put("companyid", company.getCompanyID());
                        jedjson.put("amount", taxamount);
                        jedjson.put("accountid", goodsReceiptRow.getGoodsReceipt().getTax().getAccount().getID());
                        jedjson.put("debit", false);
                        jedjson.put("jeid", je.getID());
                        KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jedetails.add(jed);
                    }
                }
/////////////////////////////////////////////    Total Tax Over

/////////////////////////////////////////////    Total Discount if Exist
                if (totalInvoiceDiscount > 0 && i == jArr.length() - 1) {
                    row.setTotalDiscount(totalInvoiceDiscount);
                    debitAmount += totalInvoiceDiscount;
                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", company.getCompanyID());
                    jedjson.put("amount", totalInvoiceDiscount);
                    jedjson.put("accountid", preferences.getDiscountReceived().getID());
                    jedjson.put("debit", true);
                    jedjson.put("jeid", je.getID());
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                }
/////////////////////////////////////////////    Total Discount if Exist Over

/////////////////////////////////////////////    Discount Row Added
                if (disc > 0) {
                    JSONObject discjson = new JSONObject();
                    discjson.put("discount", disc);
                    discjson.put("inpercent", false);
                    KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, jobj.getDouble("amount"), currencyid, null, 0);
                    discjson.put("originalamount", (Double) bAmt.getEntityList().get(0));
                    discjson.put("companyid", company.getCompanyID());
                    KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                    Discount discount = (Discount) dscresult.getEntityList().get(0);
                    row.setDiscount(discount);
                    dndetails.add(row);
                }
/////////////////////////////////////////////    Discount Row Added Over

/////////////////////////////////////////////    Product Tax
                if (goodsReceiptRow.getTax() != null) {
                    /*
                     * Product level tax taken care of
                     */

//                    KwlReturnObject perresult = accTaxObj.getTaxPercent(company.getCompanyID(), goodsReceiptRow.getGoodsReceipt().getJournalEntry().getEntryDate(), goodsReceiptRow.getTax().getID());
                    KwlReturnObject perresult = accTaxObj.getTaxPercent(company.getCompanyID(), goodsReceiptRow.getGoodsReceipt().getCreationDate(), goodsReceiptRow.getTax().getID());
                    percent = (Double) perresult.getEntityList().get(0);
                    prodTax = percent * discAccAmount / (percent + 100);
                    creditAmount += prodTax;
                    JSONObject jedtaxjson = new JSONObject();
                    jedtaxjson.put("srno", jedetails.size() + 1);
                    jedtaxjson.put("companyid", company.getCompanyID());
                    jedtaxjson.put("amount", prodTax);
                    jedtaxjson.put("accountid", goodsReceiptRow.getTax().getAccount().getID());
                    jedtaxjson.put("debit", false);
                    jedtaxjson.put("jeid", je.getID());
                    KwlReturnObject jedtaxresult = accJournalEntryobj.addJournalEntryDetails(jedtaxjson);
                    jed = (JournalEntryDetail) jedtaxresult.getEntityList().get(0);
                    jedetails.add(jed);

                    /*
                     * Product level tax taken care of
                     */
                }
/////////////////////////////////////////////    Product Tax

                if (rowDiscountpercent > 0) {
                    rowDiscount = (rowDiscountpercent * (amount - prodTax)) / (100 - rowDiscountpercent);
                    debitAmount += rowDiscount;
/////////////////////////////////////////////    Product Row Discount Added
                    JSONObject jedjson1 = new JSONObject();
                    jedjson1.put("srno", jedetails.size() + 1);
                    jedjson1.put("companyid", company.getCompanyID());
                    jedjson1.put("amount", rowDiscount);
                    jedjson1.put("accountid", preferences.getDiscountReceived().getID());
                    jedjson1.put("debit", true);
                    jedjson1.put("jeid", je.getID());
                    KwlReturnObject jedresult1 = accJournalEntryobj.addJournalEntryDetails(jedjson1);
                    jed = (JournalEntryDetail) jedresult1.getEntityList().get(0);
                    jedetails.add(jed);

/////////////////////////////////////////////    Product Row Discount Added Over
                }


/////////////////////////////////////////////    Discount Received
                if (discAccAmount != 0.0) {
                    creditAmount += (discAccAmount - prodTax + rowDiscount);
                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", company.getCompanyID());
                    jedjson.put("amount", discAccAmount - prodTax + rowDiscount);
                    jedjson.put("accountid", accountId);
                    jedjson.put("debit", false);
                    jedjson.put("jeid", je.getID());
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                }
/////////////////////////////////////////////    Discount Received Over

                totalAmount = totalAmount + discAccAmount + taxamount;// - totalInvoiceDiscount;
            }
        }

        resultlist.add(totalAmount - totalInvoiceDiscount);    //+totalTax);
        resultlist.add(discAccAmount);
        resultlist.add(dndetails);
        resultlist.add(jedetails);
        resultlist.add(reloadInventory);
        resultlist.add(creditAmount);
        return resultlist;
    }

    public List saveBillingDebitNoteRows1(HashMap<String, Object> GlobalParams, HttpServletRequest request, Company company, KWLCurrency currency, JournalEntry je, CompanyAccountPreferences preferences, double externalCurrencyRate) throws JSONException, ServiceException, SessionExpiredException, ParseException {
        double totalAmount = 0;
        double totalTax = 0;
        double discAccAmount = 0, discTotal = 0;
        double totalrowdiscount = 0, totalprodTax = 0;
        List resultlist = new ArrayList();
        HashSet bdndetails = new HashSet();
        HashSet jedetails = new HashSet();

        JournalEntryDetail jed;
        KwlReturnObject result;

        double totalDiscount = StringUtil.getDouble(request.getParameter("totalInvoiceDiscount"));
        boolean includeTax = StringUtil.getBoolean(request.getParameter("includetax"));
        String companyid = (String) GlobalParams.get("companyid");
        String currencyid = (request.getParameter("currencyid") == null ? currency.getCurrencyID() : request.getParameter("currencyid"));
        JSONArray jArr = new JSONArray(request.getParameter("productdetails"));
        for (int i = 0; i < jArr.length(); i++) {
            double taxamount = 0;
            JSONObject jobj = jArr.getJSONObject(i);
            BillingDebitNoteDetail row = new BillingDebitNoteDetail();
            row.setSrno(i + 1);
            double disc = jobj.getDouble("discamount");
            discTotal = discTotal + disc;
            row.setCompany(company);
            row.setMemo(request.getParameter("memo"));
            row.setQuantity(jobj.getDouble("remquantity"));
            if (!StringUtil.isNullOrEmpty(jobj.getString("gridRemark"))) {
                row.setRemark(jobj.getString("gridRemark"));
            }

            result = accountingHandlerDAOobj.getObject(BillingGoodsReceiptDetail.class.getName(), (StringUtil.isNullOrEmpty(jobj.getString("linkto"))) ? jobj.getString("rowid") : jobj.getString("savedrowid"));
            BillingGoodsReceiptDetail goodsReceiptRow = (BillingGoodsReceiptDetail) result.getEntityList().get(0);

            double rowdiscount = 0;
            if (goodsReceiptRow.getDiscount() != null && goodsReceiptRow.getDiscount().getDiscountValue() != 0) {
                rowdiscount = goodsReceiptRow.getDiscount().getDiscountValue() / Double.parseDouble(jobj.getString("quantity"));
                rowdiscount = rowdiscount * Double.parseDouble(jobj.getString("remquantity"));
                totalrowdiscount = totalrowdiscount + rowdiscount;
                HashMap<String, Object> jedMap = new HashMap();
                jedMap.put("srno", jedetails.size() + 1);
                jedMap.put("companyid", company.getCompanyID());
                jedMap.put("amount", rowdiscount);
                jedMap.put("accountid", preferences.getDiscountReceived().getID());
                jedMap.put("debit", true);
                jedMap.put("jeid", je.getID());
                KwlReturnObject jedresult = accJournalEntryobj.updateJournalEntryDetails(jedMap);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jedetails.add(jed);
            }
            double prodTax = 0;
            if (goodsReceiptRow.getTax() != null) {
                /*
                 * Product level tax taken care of
                 */

                KwlReturnObject perresult = accTaxObj.getTaxPercent(company.getCompanyID(), goodsReceiptRow.getBillingGoodsReceipt().getJournalEntry().getEntryDate(), goodsReceiptRow.getTax().getID());
                double percent = (Double) perresult.getEntityList().get(0);
                prodTax = percent * jobj.getDouble("discamount") / (percent + 100);
                totalprodTax = totalprodTax + prodTax;
                JSONObject jedtaxjson = new JSONObject();
                jedtaxjson.put("srno", jedetails.size() + 1);
                jedtaxjson.put("companyid", company.getCompanyID());
                jedtaxjson.put("amount", prodTax);
                jedtaxjson.put("accountid", goodsReceiptRow.getTax().getAccount().getID());
                jedtaxjson.put("debit", false);
                jedtaxjson.put("jeid", je.getID());
                KwlReturnObject jedtaxresult = accJournalEntryobj.addJournalEntryDetails(jedtaxjson);
                jed = (JournalEntryDetail) jedtaxresult.getEntityList().get(0);
                jedetails.add(jed);

                /*
                 * Product level tax taken care of
                 */
            }

            row.setGoodsReceiptRow(goodsReceiptRow);
            String product = goodsReceiptRow.getProductDetail();
            double percent = 0;
            if (goodsReceiptRow.getBillingGoodsReceipt().getTax() != null) {
//                  percent=CompanyHandler.getTaxPercent(session, request, goodsReceiptRow.getBillingGoodsReceipt().getJournalEntry().getEntryDate(),goodsReceiptRow.getBillingGoodsReceipt().getTax().getID());
                KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, goodsReceiptRow.getBillingGoodsReceipt().getJournalEntry().getEntryDate(), goodsReceiptRow.getBillingGoodsReceipt().getTax().getID());
                percent = (Double) perresult.getEntityList().get(0);

            }

            if (includeTax && goodsReceiptRow.getBillingGoodsReceipt().getTax() != null && i == jArr.length() - 1) {
                taxamount = (discTotal - (totalDiscount)) * percent / 100;
                row.setTaxAmount(taxamount);
                totalTax += taxamount;
                if (includeTax && taxamount > 0) {
                    HashMap<String, Object> jedMap = new HashMap();
                    jedMap.put("srno", jedetails.size() + 1);
                    jedMap.put("companyid", company.getCompanyID());
                    jedMap.put("amount", taxamount);
                    jedMap.put("accountid", goodsReceiptRow.getBillingGoodsReceipt().getTax().getAccount().getID());
                    jedMap.put("debit", false);
                    jedMap.put("jeid", je.getID());
                    KwlReturnObject jedresult = accJournalEntryobj.updateJournalEntryDetails(jedMap);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                }
            }
            if (i == jArr.length() - 1) {
                row.setTotalDiscount(totalDiscount);
            }
            discAccAmount = discAccAmount + jobj.getDouble("discamount");
            bdndetails.add(row);
            if (disc > 0) {

                HashMap<String, Object> discMap = new HashMap();
                discMap.put("discount", disc);
                discMap.put("inpercent", false);
                KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, jobj.getDouble("amount"), currencyid, null, externalCurrencyRate);
                discMap.put("originalamount", (Double) bAmt.getEntityList().get(0));
                discMap.put("companyid", company.getCompanyID());
                KwlReturnObject dscresult = accDiscountobj.updateDiscount(discMap);
                Discount discount = (Discount) dscresult.getEntityList().get(0);
                row.setDiscount(discount);

            }
            double debitAmount1 = (jobj.getDouble("discamount") + rowdiscount - prodTax);
            HashMap<String, Object> jedMap = new HashMap();
            jedMap.put("srno", jedetails.size() + 1);
            jedMap.put("companyid", company.getCompanyID());
            jedMap.put("amount", debitAmount1); //discAccAmount - totalDiscount);
            jedMap.put("accountid", jobj.getString("creditoraccount"));
            jedMap.put("debit", false);
            jedMap.put("jeid", je.getID());
            KwlReturnObject jedresult = accJournalEntryobj.updateJournalEntryDetails(jedMap);
            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetails.add(jed);
        }


        if (totalDiscount != 0) {
            HashMap<String, Object> jedMap = new HashMap();
            jedMap.put("srno", jedetails.size() + 1);
            jedMap.put("companyid", company.getCompanyID());
            jedMap.put("amount", totalDiscount);
            jedMap.put("accountid", preferences.getDiscountReceived().getID());
            jedMap.put("debit", true);
            jedMap.put("jeid", je.getID());
            KwlReturnObject jedresult = accJournalEntryobj.updateJournalEntryDetails(jedMap);
            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetails.add(jed);
        }


        if (discAccAmount != 0.0) {
            totalAmount += discAccAmount;
        }
        resultlist.add(totalAmount + totalrowdiscount);
        resultlist.add(discAccAmount - totalDiscount + totalTax);
        resultlist.add(bdndetails);
        resultlist.add(jedetails);
        return resultlist;
    }

    public ModelAndView getDebitNoteMerged(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try {
            boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
            HashMap map = accDebitNoteService.getDebitNoteCommonCode(request, response);
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
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
   
    public ModelAndView exportDebitNoteMerged(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        String view = "jsonView_ex";
        try {
            String type = !StringUtil.isNullOrEmpty(request.getParameter("type").toString()) ? request.getParameter("type") : "";
            if (type.equals("detailedXls")) {
                request.setAttribute("isExport", true);
                DataJArr =  exportDebitNoteWithDetails(request, response, DataJArr);
            } else {
            HashMap<String, Object> requestParams = gettDebitNoteMap(request);
            boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
            String[] companyids = (consolidateFlag && request.getParameter("companyids") != null) ? request.getParameter("companyids").split(",") : sessionHandlerImpl.getCompanyid(request).split(",");
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid") != null) ? request.getParameter("gcurrencyid") : sessionHandlerImpl.getCurrencyID(request);
            boolean eliminateflag = consolidateFlag;
            if (consolidateFlag) {
                requestParams.put(Constants.start, "");
                requestParams.put(Constants.limit, "");
            }
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(),  sessionHandlerImpl.getCompanyid(request));
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            if (extraCompanyPreferences != null && extraCompanyPreferences.isEnablesalespersonAgentFlow()) {
                int permCode = sessionHandlerImpl.getPerms(request, Constants.VENDOR_PERMCODE);
                if (!((permCode & Constants.VENDOR_VIEWALL_PERMCODE) == Constants.VENDOR_VIEWALL_PERMCODE)) {
                    /*
                     * when (permCode & Constants.VENDOR_VIEWALL_PERMCODE) == Constants.VENDOR_VIEWALL_PERMCODE is true then user has permission to view all vendors documents,so at that time there is need to filter record according to user&agent. 
                     */
                    String userId = sessionHandlerImpl.getUserid(request);
                    requestParams.put("userid", userId);
                    requestParams.put("enablesalespersonagentflow", extraCompanyPreferences.isEnablesalespersonAgentFlow());
                }
            }
            
            KwlReturnObject result = null;
            String companyid = "";
            for (int cnt = 0; cnt < companyids.length; cnt++) {
                companyid = companyids[cnt];
                request.setAttribute("companyid", companyid);
                request.setAttribute("gcurrencyid", gcurrencyid);
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);
                requestParams.put("isExport",true);

                int cntype = StringUtil.isNullOrEmpty(request.getParameter("cntype")) ? 1 : Integer.parseInt(request.getParameter("cntype"));
                if (cntype == 1 || cntype == 4 || cntype == 12 || cntype == 13 || cntype == 5 || cntype == Constants.DebitNoteForOvercharge) {       //Other than opening CN
                    result = accDebitNoteobj.getDebitNoteMerged(requestParams);
                    DataJArr = accDebitNoteService.getDebitNotesMergedJson(requestParams, result.getEntityList(), DataJArr);
                } else {
                    boolean isNoteForPayment = false;
                    boolean isVendor = false;
                    if (!StringUtil.isNullOrEmpty(request.getParameter("isNoteForPayment"))) {
                        isNoteForPayment = Boolean.parseBoolean(request.getParameter("isNoteForPayment"));
                        isVendor = Boolean.parseBoolean(request.getParameter("isVendor"));
                    }
                    requestParams.put("isNoteForPayment", isNoteForPayment);
                    if (cntype == 10 || (isNoteForPayment && isVendor)) {   //Get Opening Balance for Vendor
                        result = accDebitNoteobj.getOpeningBalanceDNs(requestParams);
                        requestParams.put("cntype", 10);
                        accDebitNoteService.getOpeningDebitNotesJson(requestParams, result.getEntityList(), DataJArr);
                    } else if (cntype == 11 || (isNoteForPayment && !isVendor)) {   //Get Opening Balance for Customer
                        result = accDebitNoteobj.getOpeningBalanceCustomerDNs(requestParams);
                        requestParams.put("cntype", 11);
                        accDebitNoteService.getOpeningDebitNotesJson(requestParams, result.getEntityList(), DataJArr);
                    }
                }
            }
            }
            jobj.put("data", DataJArr);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    public JSONArray exportDebitNoteWithDetails(HttpServletRequest request, HttpServletResponse response, JSONArray dataArray) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        JSONArray finalArray = new JSONArray();
        String companyid = "";
        try {
            HashMap dataHashMap = accDebitNoteService.getDebitNoteCommonCode(request, response);
            DataJArr = (JSONArray) dataHashMap.get("data");
            companyid = sessionHandlerImpl.getCompanyid(request);
            int cnCount=0;
            for (int i = 0; i < DataJArr.length(); i++) {
                JSONObject obj = DataJArr.getJSONObject(i);
                String billid = obj.optString("noteid", "");
                KwlReturnObject dnResult = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), billid);
                DebitNote DN = (DebitNote) dnResult.getEntityList().get(0);
                finalArray.put(obj);
                HashMap<String,Object> requestParams = new HashMap<>();
                DateFormat userdateformatter = authHandlerDAOObj.getUserDateFormatter(sessionHandlerImpl.getDateFormatID(request), sessionHandlerImpl.getTimeZoneDifference(request), true);
                requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
                requestParams.put("bills", billid.split(","));
                requestParams.put("df", authHandler.getDateOnlyFormat(request));
                requestParams.put("userdateformatter", userdateformatter);
                requestParams.put("isExport", request.getAttribute("isExport"));
                requestParams.put("companyid", companyid);
                boolean isForReport = false;
                String dtype = !StringUtil.isNullOrEmpty(request.getParameter("dtype").toString()) ? request.getParameter("dtype") : "";
                if (!StringUtil.isNullOrEmpty(dtype) && dtype.equals("report")) {
                    isForReport = true;
                }
                requestParams.put("isForReport", isForReport);
                JSONArray jArray = accDebitNoteService.getDebitNoteRowsJson(requestParams);
                int accDetailCount=0;
                int invDetailCount=0;
                for (int j = 0; j < jArray.length(); j++) {
                    JSONObject row = jArray.getJSONObject(j);
                    exportDaoObj.editJsonKeyForExcelFile(row, Constants.Acc_Credit_Note_ModuleId);
                    if (row.optBoolean("isaccountdetails", false)) {
                        row.put("srnoforaccount", accDetailCount+1);
                        row.put("taxamount", "");
                        row.put("reason", "");
                        accDetailCount++;
                        finalArray.put(row);
                    } else {
                        row.put("srno", invDetailCount+1);
                        row.put("currencycode", "");
                        row.put("memo", "");
                        finalArray.put(row);
                        invDetailCount++;
                    }
                }
                cnCount++;
            }
            jobj.put("data", finalArray);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return finalArray;
    }
    
    public ModelAndView deleteDebitNotesMerged(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("DN_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String linkedTransactions = deleteDebitNotesMerged(request);
            txnManager.commit(status);
            issuccess = true;
            if(StringUtil.isNullOrEmpty(linkedTransactions)){
                msg = messageSource.getMessage("acc.debitN.del", null, RequestContextUtils.getLocale(request));   //"Debit Note(s) has been deleted successfully";
            } else {
                msg = messageSource.getMessage("acc.dnexcept.deleted", null, RequestContextUtils.getLocale(request)) + " " +  linkedTransactions.substring(0, linkedTransactions.length()-2) + " " + messageSource.getMessage("acc.field.deletedsuccessfully", null, RequestContextUtils.getLocale(request)) + " " + messageSource.getMessage("acc.field.usedintransactionorlockingperiod", null, RequestContextUtils.getLocale(request));   //"Sales Order has been deleted successfully;
            }     
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public String deleteDebitNotesMerged(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
//        ArrayList params = new ArrayList();
        String linkedTransactions="";
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
//            String qMarks = "";
            String companyid = sessionHandlerImpl.getCompanyid(request);
//            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);

            boolean isMassDelete = true; //flag for bulk delete
            if(jArr.length() == 1){
                isMassDelete = false;
            }
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("noteid"))) {
                    boolean withoutinventory = Boolean.parseBoolean(jobj.getString("withoutinventory"));
                    String dnid = StringUtil.DecodeText(jobj.optString("noteid"));
                    String dnno = jobj.getString("noteno");
                    String jid = jobj.optString("journalentryid","");
                    String entryno=jobj.optString("entryno","");
                    String roundingJENo = "";
                    String roundingJEIDs = "";
                    boolean isNoteLinkedWithPayment = accDebitNoteService.isNoteLinkedWithPayment(dnid);
                    if(isNoteLinkedWithPayment){
                        linkedTransactions+=dnno+", ";
                        continue;
                    }
                    boolean isNoteLinkedWithAdvancePayment = accDebitNoteService.isNoteLinkedWithAdvancePayment(dnid);
                    if(isNoteLinkedWithAdvancePayment){
                        linkedTransactions+=dnno+", ";
                        continue;
                    }
                    if(accDebitNoteService.isDebitNoteLinkedWithCreditNote(dnid, companyid)==true){
                        linkedTransactions+=dnno+", ";
                        continue;
                    }
                    /**
                     * Method to check the payment is Reconciled or not according to its JE id
                     */
                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("jeid", jid);
                    requestParams.put("companyid", companyid);
                    boolean isReconciledFlag = accBankReconciliationDAOObj.isRecordReconciled(requestParams);
                    if(isReconciledFlag){
                        linkedTransactions += dnno+", ";
                        if(isMassDelete){ //if bulk delete then only append document no
                            continue;
                        } else{ //if single document delete then throw exception with proper message
                            if(!StringUtil.isNullOrEmpty(linkedTransactions)){
                                linkedTransactions = linkedTransactions.substring(0, linkedTransactions.length()-2);
                            }
                            throw new AccountingException(messageSource.getMessage("acc.reconcilation.Cannotdeletepayment", null, RequestContextUtils.getLocale(request))+" "+"<b>"+ linkedTransactions + " " +"</b>"+messageSource.getMessage("acc.reconcilation.asitisreconciled", null, RequestContextUtils.getLocale(request)));
                        }
                    }
                    if (withoutinventory) {
                        String bdnid = StringUtil.DecodeText(jobj.optString("noteid"));
//                    params.add(StringUtil.DecodeText(jobj.optString("noteid")));
//                    qMarks += "?,";
//                }
//            }
//            params.add(company.getCompanyID());
//            qMarks = qMarks.substring(0, Math.max(0, qMarks.length() - 1));
//            String query;
//            List list;
//            query = "update BillingDebitNote set deleted=true where ID in("+qMarks +") and company.companyID=?";
//            HibernateUtil.executeUpdate(session, query, params.toArray());
                        KwlReturnObject result = accDebitNoteobj.deleteBillingDebitNote(bdnid, companyid);


                        //query = "update JournalEntry je set je.deleted=true  where je.ID in(select dn.journalEntry.ID from BillingDebitNote dn where dn.ID in( " + qMarks + ") and dn.company.companyID=je.company.companyID) and je.company.companyID=?";
                        result = accDebitNoteobj.getJEFromBDN(bdnid, companyid);
                        List list = result.getEntityList();
                        Iterator itr = list.iterator();
                        while (itr.hasNext()) {
                            String jeid = ((BillingDebitNote) itr.next()).getJournalEntry().getID();
                            result = accJournalEntryobj.deleteJournalEntry(jeid, companyid);
                            //Delete entry from optimized table
                            accJournalEntryobj.deleteAccountJEs_optimized(jeid);
                        }

//            query = "update Discount di set di.deleted=true  where di.ID in(select dnd.discount.ID from BillingDebitNoteDiscount dnd where dnd.debitNote.ID in( " + qMarks + ") and dnd.company.companyID=di.company.companyID) and di.company.companyID=?";
                        result = accDebitNoteobj.getDNDFromBDN(bdnid, companyid);
                        list = result.getEntityList();
                        itr = list.iterator();
                        while (itr.hasNext()) {
                            String discountid = ((BillingDebitNoteDiscount) itr.next()).getDiscount().getID();
                            result = accDiscountobj.deleteDiscountEntry(discountid, companyid);
                        }

//            query = "update Discount di set di.deleted=true  where di.ID in(select dnd.discount.ID from BillingDebitNoteDetail dnd where dnd.debitNote.ID in( " + qMarks + ") and dnd.company.companyID=di.company.companyID) and di.company.companyID=?";
                        result = accDebitNoteobj.getDNDFromBDND(bdnid, companyid);
                        list = result.getEntityList();
                        itr = list.iterator();
                        while (itr.hasNext()) {
                            String discountid = ((BillingDebitNoteDetail) itr.next()).getDiscount().getID();
                            result = accDiscountobj.deleteDiscountEntry(discountid, companyid);
                        }
                    } else {
                        
                        // check for is DN created from Sales Return
                        KwlReturnObject dnObj = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), dnid);
                        DebitNote debitNote = (DebitNote) dnObj.getEntityList().get(0);
                      
                        if (debitNote.getPurchaseReturn() != null) {
                           // throw new AccountingException("Debit Note : "+debitNote.getDebitNoteNumber()+" is created from purchase return so please delete Purchase Return for deleting it.");
                           linkedTransactions+=dnno+", ";
                           continue;
                        }

                        /*
                         * Before deleting DebitNoteDetail Keeping id of
                         * Goodsrceipt utlized in Payment
                         */
                        Set<String> grIDSet = new HashSet<>();
                        if (debitNote != null && debitNote.getApprovestatuslevel() == 11 && !debitNote.isDeleted()) {
                            for (DebitNoteDetail dnd : debitNote.getRows()) {
                                if (dnd.getGoodsReceipt() != null) {
                                    grIDSet.add(dnd.getGoodsReceipt().getID());
                                }
                            }
                        }
//                    params.add(jobj.getString("noteid"));
//                    qMarks += "?,";
//                    query = "update DebitNote set deleted=true where ID in("+qMarks +") and company.companyID=?";
//                    HibernateUtil.executeUpdate(session, query, params.toArray());
                        /*ERP-40734
                         *To check that the Debit note is belongs to Locked accoundting period
                         *If it is belong to locked accounting period it will throw the exception
                         */
                        HashMap<String, Object> deleteMap = new HashMap<String, Object>();
                        KwlReturnObject result = null;
                        deleteMap.put("dnid", dnid);
                        deleteMap.put("companyid", companyid);
                        deleteMap.put("entryno", entryno);
                        try {
                            result = accDebitNoteService.deleteDebitNotePartialy(deleteMap);
                        } catch (AccountingException ex) {
                            linkedTransactions += dnno + ", ";
                            continue;
                        }

//                        accDebitNoteService.updateOpeningInvoiceAmountDue(dnid, companyid);
//                        KwlReturnObject result = accDebitNoteobj.deleteDebitNote(dnid, companyid);
////                    query = "update JournalEntry je set je.deleted=true  where je.ID in(select dn.journalEntry.ID from DebitNote dn where dn.ID in( " + qMarks + ") and dn.company.companyID=je.company.companyID) and je.company.companyID=?";
////                    HibernateUtil.executeUpdate(session, query, params.toArray());
//                        result = accDebitNoteobj.getJEFromDN(dnid);
//                        List list = result.getEntityList();
//                        Iterator itr = list.iterator();
//                        while (itr.hasNext()) {
//                            String jeid = (String) itr.next();
//                            result = accJournalEntryobj.deleteJournalEntry(jeid, companyid);
//                            //Delete entry from optimized table
//                            accJournalEntryobj.deleteAccountJEs_optimized(jeid);
//                        }
//
////                    query = "update Discount di set di.deleted=true  where di.ID in(select dnd.discount.ID from DebitNoteDiscount dnd where dnd.debitNote.ID in( " + qMarks + ") and dnd.company.companyID=di.company.companyID) and di.company.companyID=?";
////                    HibernateUtil.executeUpdate(session, query, params.toArray());
//                        result = accDebitNoteobj.getDNDFromDN(dnid);
//                        list = result.getEntityList();
//                        itr = list.iterator();
//                        while (itr.hasNext()) {
//                            String discountid = (String) itr.next();
//                            result = accDiscountobj.deleteDiscountEntry(discountid, companyid);
//                        }
//
//                        /*
//                         * query = "update Discount di set di.deleted=true where
//                         * di.ID in(select dnd.discount.ID from DebitNoteDetail
//                         * dnd where dnd.debitNote.ID in( " + qMarks + ") and
//                         * dnd.company.companyID=di.company.companyID) and
//                         * di.company.companyID=?";
//                         * HibernateUtil.executeUpdate(session, query,
//                         * params.toArray());
//                         */
//                        result = accDebitNoteobj.getDNDIFromDN(dnid);
//                        list = result.getEntityList();
//                        itr = list.iterator();
//                        while (itr.hasNext()) {
//                            String discountid = (String) itr.next();
//                            result = accDiscountobj.deleteDiscountEntry(discountid, companyid);
//                        }
                        /*
                         * query = "update Inventory inv set inv.deleted=true
                         * where inv.ID in(select dnd.inventory.ID from
                         * DebitNoteDetail dnd where dnd.debitNote.ID in( " +
                         * qMarks + ") and
                         * dnd.company.companyID=inv.company.companyID) and
                         * inv.company.companyID=?";
                         * HibernateUtil.executeUpdate(session, query,
                         * params.toArray());
                         */
                        result = accDebitNoteobj.getDNDInvFromDN(dnid);
                        List list = result.getEntityList();
                        Iterator itr = list.iterator(); 
                        while (itr.hasNext()) {
                            String inventoryid = (String) itr.next();
                            if (!StringUtil.isNullOrEmpty(inventoryid)) {
                                result = accProductObj.deleteInventoryEntry(inventoryid, companyid);
                            }
                        }
                        //Delete Rouding JEs if created against PI
                        if (!grIDSet.isEmpty()) {
                            String piIDs = "";
                            for (String piID : grIDSet) {
                                piIDs = piID + ",";
                            }
                            if (!StringUtil.isNullOrEmpty(piIDs)) {
                                piIDs = piIDs.substring(0, piIDs.length() - 1);
                            }
                            KwlReturnObject jeResult = accJournalEntryobj.getRoundingJournalEntryByGRIds(piIDs, companyid);
                            List<JournalEntry> jeList = jeResult.getEntityList();
                            for (JournalEntry roundingJE : jeList) {
                                roundingJENo = roundingJE.getEntryNumber() + ",";
                                roundingJEIDs = roundingJE.getID() + ",";
                                accDebitNoteService.deleteJEArray(roundingJE.getID(), companyid);
                            }
                            if (!StringUtil.isNullOrEmpty(roundingJENo)) {
                                roundingJENo = roundingJENo.substring(0, roundingJENo.length() - 1);
                            }
                            if (!StringUtil.isNullOrEmpty(roundingJEIDs)) {
                                roundingJEIDs = roundingJEIDs.substring(0, roundingJEIDs.length() - 1);
                            }
                        }
                        
                        if (preferences.isInventoryAccountingIntegration()) {

                            String action = "17";
                            boolean isDirectUpdateInvFlag = false;
                            if (preferences.isUpdateInvLevel()) {
                                isDirectUpdateInvFlag = true;
                                action = "19";//Direct Inventory Update action
                            }

                            JSONArray productArray = new JSONArray();

//                            KwlReturnObject res = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), dnid);
//                            DebitNote debitNote = (DebitNote) res.getEntityList().get(0);

                            Set<DebitNoteDetail> debitNoteDetails = debitNote.getRows();
                            for (DebitNoteDetail debitNoteDetail : debitNoteDetails) {
                                if (debitNoteDetail.getInventory() != null) {
                                    JSONObject productObject = new JSONObject();
                                    productObject.put("itemUomId", debitNoteDetail.getInventory().getUom().getID());
                                    productObject.put("itemBaseUomRate", debitNoteDetail.getInventory().getBaseuomrate());
                                    productObject.put("itemQuantity", debitNoteDetail.getInventory().getBaseuomquantity());
                                    productObject.put("quantity", debitNoteDetail.getInventory().getQuantity());
                                    //productObject.put("itemQuantity", debitNoteDetail.getInventory().getQuantity());
                                    productObject.put("itemCode", debitNoteDetail.getInventory().getProduct().getProductid());
                                    if (isDirectUpdateInvFlag) {
                                        productObject.put("storeid", debitNoteDetail.getInvstoreid());
                                        productObject.put("locationid", debitNoteDetail.getInvlocid());
                                    }
                                    productArray.put(productObject);
                                }
                            }
                            if (productArray.length() > 0) {

                                String sendDateFormat = "yyyy-MM-dd";
                                DateFormat dateformat = new SimpleDateFormat(sendDateFormat);
//                                Date date = debitNote.getJournalEntry().getEntryDate();
                                Date date = debitNote.getCreationDate();
                                String stringDate = dateformat.format(date);

                                JSONObject jSONObject = new JSONObject();
                                jSONObject.put("deliveryDate", stringDate);
                                jSONObject.put("dateFormat", sendDateFormat);
                                jSONObject.put("details", productArray);
                                jSONObject.put("orderNumber", debitNote.getDebitNoteNumber());
                                jSONObject.put("companyId", companyid);
                                jSONObject.put("purchasing", true);

                                String url = this.getServletContext().getInitParameter("inventoryURL");
                                CommonFnController cfc = new CommonFnController();
                                cfc.updateInventoryLevel(request, jSONObject, url, action);
                            }
                        }
                        
                    }
                    
                    // delete foreign gain loss JE
                    List resultJe = accDebitNoteobj.getForeignGainLossJE(dnid, companyid);
                    if (resultJe.size()>0 && resultJe.get(0) != null) {
                        Iterator itr1 = resultJe.iterator();
                        while (itr1.hasNext()) {
                            Object object = itr1.next();
                            String jeid = object != null ? object.toString() : "";
                            KwlReturnObject result = accJournalEntryobj.deleteJournalEntry(jeid, companyid);
                        }
                    }
                    StringBuffer journalEntryMsg = new StringBuffer();
                    if (!StringUtil.isNullOrEmpty(entryno)) {
                        journalEntryMsg.append(" along with the JE No. " + entryno);
                    }
                    auditTrailObj.insertAuditLog(AuditAction.DEBIT_NOTE_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted Debit Note " + dnno+journalEntryMsg.toString(), request, dnid);
                    
                    if(!StringUtil.isNullOrEmpty(roundingJENo)){
                        auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted Debit Note " + dnno+". So Rounding JE No. "+ roundingJENo +" deleted.", request, roundingJEIDs);
                    }
                }
            }
//            params.add(company.getCompanyID());
//            qMarks = qMarks.substring(0, Math.max(0, qMarks.length() - 1));
//            String query;
//            List list;
        }/* catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        }*/ catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)));
        }
        return linkedTransactions;
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
            HashSet<DebitNoteDetail> dndetails = null;
            String recordids = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("recordids"))) {
                recordids = request.getParameter("recordids");
            }
            ArrayList<String> SOIDList = CustomDesignHandler.getSelectedBillIDs(recordids);
            for (int cnt = 0; cnt < SOIDList.size(); cnt++) {
                boolean withInventory = Boolean.parseBoolean(request.getParameter("withInv"));
                HashMap<String, Object> hm = new HashMap<String, Object>();
                hm.put("dnid", SOIDList.get(cnt));
                hm.put("isprinted", request.getParameter("isprinted"));
                if (!StringUtil.isNullOrEmpty(SOIDList.get(cnt))) {
                    result = accDebitNoteobj.updateDebitNote(hm);
                }
            }
            msg = messageSource.getMessage("acc.field.setsuccessfully", null, RequestContextUtils.getLocale(request));
            issuccess = true;
            txnManager.commit(status);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView saveDebitNoteAgainstInvoice(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        String debitNoteNumBer = "";
        String JENumBer = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("PaymentMethods_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            List returnList = saveDebitNoteAgainstInvoice(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String oldJeId = (String) returnList.get(0);
            DebitNote debitnote = (DebitNote) returnList.get(1);

            debitNoteNumBer = debitnote.getDebitNoteNumber();
            JENumBer = debitnote.getJournalEntry().getEntryNumber();
            issuccess = true;
            msg = messageSource.getMessage("acc.debitN.save", null, RequestContextUtils.getLocale(request)) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + debitNoteNumBer + ",</b>" + messageSource.getMessage("acc.field.JENo", null, RequestContextUtils.getLocale(request)) + ": <b>" + JENumBer + "</b>";   //"Debit Note has been saved successfully";
            txnManager.commit(status);

            status = txnManager.getTransaction(def);
            accDebitNoteService.deleteJEArray(oldJeId, companyid);
            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
     public List saveDebitNoteAgainstInvoice(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException {
        DebitNote debitnote = null;
        List list = new ArrayList();
        KwlReturnObject result;
        List ll = new ArrayList();
        String oldjeid = "";
        String auditMsg = "", auditID = "";
        try {
            boolean reloadInventory = false;//Flag used to reload inventory on Client Side If CN type equals to "Return" or "Defective"
            double externalCurrencyRate = StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            String sequenceformat = request.getParameter("sequenceformat");
            String debitNoteId = request.getParameter("noteid");
            String jeentryNumber = "";
            boolean jeautogenflag = false;
            String jeIntegerPart = "";
            String jeDatePrefix = "";
            String jeAfterDatePrefix = "";
            String jeDateSuffix = "";
            String jeSeqFormatId = "";
            boolean isEditNote = false;
            String entryNumber = request.getParameter("number");
            DateFormat df = authHandler.getDateOnlyFormat(request);
            HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
            GlobalParams.put("companyid", companyid);
            GlobalParams.put("gcurrencyid", currencyid);
            GlobalParams.put("dateformat", df);
            String customfield = request.getParameter("customfield");
            boolean otherwise = request.getParameter("otherwise") != null;
            Date creationDate = df.parse(request.getParameter("billdate"));
            String createdby = sessionHandlerImpl.getUserid(request);
            String modifiedby = sessionHandlerImpl.getUserid(request);
            long createdon = System.currentTimeMillis();
            long updatedon = System.currentTimeMillis();
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);
            entryNumber = request.getParameter("number");
            currencyid = (request.getParameter("currencyid") == null ? kwlcurrency.getCurrencyID() : request.getParameter("currencyid"));
            curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            HashMap<String, Object> dnhm = new HashMap<String, Object>();
            if (!StringUtil.isNullOrEmpty(debitNoteId)) {
                //In case of edit if format is NA then entry number can be changed so need to check duplicate
                result = accDebitNoteobj.getDNFromNoteNoAndId(entryNumber, companyid, debitNoteId);
                if (result.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
                    throw new AccountingException(messageSource.getMessage("acc.field.Debitnotenumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                }
                if (sequenceformat.equals("NA")) {
                    dnhm.put("entrynumber", entryNumber);
                }

                isEditNote = true;
                KwlReturnObject dnObj = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), debitNoteId);
                debitnote = (DebitNote) dnObj.getEntityList().get(0);
                oldjeid = debitnote.getJournalEntry().getID();
                JournalEntry jetemp = debitnote.getJournalEntry();
                if (jetemp != null) {
                    jeentryNumber = jetemp.getEntryNumber(); //preserving these data to generate same JE number in edit case                    
                    jeautogenflag = jetemp.isAutoGenerated();
                    jeSeqFormatId = jetemp.getSeqformat() == null ? "" : jetemp.getSeqformat().getID();
                    jeIntegerPart = String.valueOf(jetemp.getSeqnumber());
                    jeDatePrefix = jetemp.getDatePreffixValue();
                    jeAfterDatePrefix = jetemp.getDateAfterPreffixValue();
                    jeDateSuffix = jetemp.getDateSuffixValue();
                }
                if (debitnote != null) {
                    accDebitNoteService.updateOpeningInvoiceAmountDue(debitnote.getID(), companyid);
                }
                result = accDebitNoteobj.deleteDebitNoteDetails(debitnote.getID(), companyid);
                result = accDebitNoteobj.deleteDebitTaxDetails(debitnote.getID(), companyid);

                //Delete old entries and insert new entries again from optimized table in edit case.
                accJournalEntryobj.deleteOnEditAccountJEs_optimized(oldjeid);
                deleteJEDetailsCustomData(oldjeid);
                dnhm.put("dnid", debitNoteId);
            } else {
                synchronized (this) {
                    result = accDebitNoteobj.getDNFromNoteNo(entryNumber, companyid);
                    int count = result.getRecordTotalCount();
                    if (count > 0) {
                        if (sequenceformat.equals("NA")) {
                            throw new AccountingException(messageSource.getMessage("acc.field.Debitnotenumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                        }
                    }
                    String nextDNAutoNo = "";
                    String nextAutoNoInt = "";
                    String datePrefix = "";
                    String dateafterPrefix = "";
                    String dateSuffix = "";

                    if (!sequenceformat.equals("NA")) {
                        boolean seqformat_oldflag = StringUtil.getBoolean(request.getParameter("seqformat_oldflag"));
                        if (seqformat_oldflag) {
                            nextDNAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_DEBITNOTE, sequenceformat);
                        } else {
                            Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_DEBITNOTE, sequenceformat, seqformat_oldflag, creationDate);
                            nextDNAutoNo = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                            nextAutoNoInt = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                            datePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                            dateafterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                            dateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                            dnhm.put(Constants.SEQFORMAT, sequenceformat);
                            dnhm.put(Constants.SEQNUMBER, nextAutoNoInt);
                            dnhm.put(Constants.DATEPREFIX, datePrefix);
                            dnhm.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
                            dnhm.put(Constants.DATESUFFIX, dateSuffix);
                        }
                        entryNumber = nextDNAutoNo;
                    }

                    dnhm.put("entrynumber", entryNumber);
                    dnhm.put("autogenerated", nextDNAutoNo.equals(entryNumber));
                    dnhm.put("oldRecord", false);
                }

                Long seqNumber = null;
                result = accDebitNoteobj.getDNSequenceNo(companyid, creationDate);
                List li = result.getEntityList();
                if (!li.isEmpty()) {
                    seqNumber = (Long) li.get(0);
                }
                dnhm.put("sequence", seqNumber.intValue());
            }
            String costCenterId = !StringUtil.isNullOrEmpty(request.getParameter("costCenterId"))?request.getParameter("costCenterId"):"";
            dnhm.put("memo", request.getParameter("memo"));
            dnhm.put("companyid", companyid);
            dnhm.put("currencyid", currencyid);
            dnhm.put("createdby", createdby);
            dnhm.put("modifiedby", modifiedby);
            dnhm.put("createdon", createdon);
            dnhm.put("updatedon", updatedon);
            dnhm.put("costcenter", costCenterId);
            dnhm.put("creationDate", creationDate);
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.MVATTRANSACTIONNO))) {
                dnhm.put(Constants.MVATTRANSACTIONNO, request.getParameter(Constants.MVATTRANSACTIONNO));
            }
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
                    jeAfterDatePrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);//Date After Prefix Part
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
            jeDataMap.put(Constants.DATEAFTERPREFIX, jeAfterDatePrefix);
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
            dnhm.put("journalentryid", jeid);
            List DNlist = new ArrayList();
            dnhm.put("customerid", request.getParameter("customer"));

            //Debit Note against Paid vendor Invoice
            dnhm.put("otherwise", true);
            dnhm.put("openflag", true);
//            dnhm.put("cntype", 1);
            DNlist = saveDebitNoteAgainstInvoiceRows(GlobalParams, request, company, currency, journalEntry, preferences, externalCurrencyRate);
            dnhm.put("dnamount", (Double) DNlist.get(3));
            dnhm.put("dnamountdue", (Double) DNlist.get(3));
            dnhm.put("cntype", 4);

            Double totalAmount = (Double) DNlist.get(3);
            HashSet<DebitNoteDetail> dndetails = (HashSet<DebitNoteDetail>) DNlist.get(0);
            HashSet<DebitNoteTaxEntry> dnTaxEntryDetails = (HashSet<DebitNoteTaxEntry>) DNlist.get(2);
            jedetails = (HashSet<JournalEntryDetail>) DNlist.get(1);
//            reloadInventory = (Boolean) DNlist.get(4);
            double termTotalAmount = 0;
            HashMap<String, Double> termAcc = new HashMap<String, Double>();
            String dnTerms = request.getParameter("invoicetermsmap");
            if (!StringUtil.isNullOrEmpty(dnTerms)) {
                JSONArray termsArr = new JSONArray(dnTerms);
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
            
             KwlReturnObject venObj = accountingHandlerDAOobj.getObject(Customer.class.getName(), request.getParameter("customer"));
            Customer customer = (Customer) venObj.getEntityList().get(0);
            
            jedjson.put("accountid",customer.getAccount().getID());
            jedjson.put("debit", true);
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
                    jedjson.put("debit", entry.getValue() > 0 ? false : true);
                    jedjson.put("jeid", jeid);
                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                }
            }
//            jeDataMap.put("jedetails", jedetails);
//            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
//            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
//            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);

            if (isEditNote) {
                result = accDebitNoteobj.updateDebitNote(dnhm);
                auditID = AuditAction.DABIT_NOTE_MODIFIED;
                auditMsg = "updated";
            } else {
                dnhm.put("approvestatuslevel", 11);
                result = accDebitNoteobj.addDebitNote(dnhm);
                auditID = AuditAction.DABIT_NOTE_CREATED;
                auditMsg = "added";
            }

            debitnote = (DebitNote) result.getEntityList().get(0);
            jeDataMap.put("jedetails", jedetails);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeDataMap.put("transactionId", debitnote.getID());
            jeDataMap.put("transactionModuleid", Constants.Acc_Debit_Note_ModuleId); // store DN id and moduleid in journalentry
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                customrequestParams.put("modulerecid", journalEntry.getID());
                customrequestParams.put("moduleid", Constants.Acc_Debit_Note_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    jeDataMap.put("accjecustomdataref", journalEntry.getID());
                    jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);
                }
            }

            dnhm.put("dnid", debitnote.getID());
            Iterator itr = dndetails.iterator();
            while (itr.hasNext()) {
                DebitNoteDetail cnd = (DebitNoteDetail) itr.next();
                cnd.setDebitNote(debitnote);
            }
            dnhm.put("dndetails", dndetails);

            Iterator dntaxitr = dnTaxEntryDetails.iterator();
            while (dntaxitr.hasNext()) {
                DebitNoteTaxEntry noteTaxEntry = (DebitNoteTaxEntry) dntaxitr.next();
                noteTaxEntry.setDebitNote(debitnote);
            }
            dnhm.put("debitNoteTaxEntryDetails", dnTaxEntryDetails);

            result = accDebitNoteobj.updateDebitNote(dnhm);
            debitnote = (DebitNote) result.getEntityList().get(0);

            accJournalEntryobj.saveAccountJEs_optimized(jeid);

            list.add(debitnote);
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
                        if (reloadInventory) { //check inventory flag and then update inventory to the inventory system

                            KwlReturnObject grResult = accountingHandlerDAOobj.getObject(GoodsReceiptDetail.class.getName(), (StringUtil.isNullOrEmpty(jobj.getString("linkto"))) ? jobj.getString("rowid") : jobj.getString("savedrowid"));
                            GoodsReceiptDetail goodsReceiptRow = (GoodsReceiptDetail) grResult.getEntityList().get(0);

                            boolean updateInventoryFlag = goodsReceiptRow.getInventory().isInvrecord(); //check update inventory flag and then update inventory to the inventory system
                            if (preferences.isWithInvUpdate()) {
                                updateInventoryFlag = getInvoiceStatusForGRO(goodsReceiptRow);
                            }

                            if (updateInventoryFlag) {
                                JSONObject productObject = new JSONObject();
                                productObject.put("itemUomId", jobj.getString("uomid"));
                                productObject.put("itemBaseUomRate", jobj.getDouble("baseuomrate"));
                                productObject.put("itemQuantity", jobj.getDouble("remquantity") * jobj.getDouble("baseuomrate") * (-1));
                                productObject.put("quantity", jobj.getDouble("remquantity") * jobj.getDouble("baseuomrate") * (-1));
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
                        jSONObject.put("purchasing", true);

                        String url = this.getServletContext().getInitParameter("inventoryURL");
                        CommonFnController cfc = new CommonFnController();
                        cfc.updateInventoryLevel(request, jSONObject, url, action);
                    }
                }
            }
            dnTerms = request.getParameter("invoicetermsmap");
            if (StringUtil.isAsciiString(dnTerms)) {
                mapDebitTerms(dnTerms, debitnote.getID(), sessionHandlerImpl.getUserid(request));
            }

            ll.add(oldjeid);
            ll.add(debitnote);
            auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + auditMsg + " a Debit Note " + debitnote.getDebitNoteNumber(), request, debitnote.getID());
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveDebitNote : " + ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveDebitNote : " + ex.getMessage(), ex);
        }
        return ll;
    }
     
    public List saveDebitNoteAgainstInvoiceRows(HashMap<String, Object> GlobalParams, HttpServletRequest request, Company company, KWLCurrency currency, JournalEntry je, CompanyAccountPreferences preferences, double externalCurrencyRate) throws JSONException, ServiceException, SessionExpiredException, ParseException {
          List srRowsDetails = new ArrayList();
        double totalDNAmt = 0;
        double totalDNAmtExludingTax = 0;
        HashSet dndetails = new HashSet();
        HashSet dnTaxEntryDetails = new HashSet();
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
//                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(InvoiceDetail.class.getName(), jobj.getString("rowid"));
//                        InvoiceDetail id = (InvoiceDetail) rdresult.getEntityList().get(0);
//                        dodDataMap.put("InvoiceDetail", id);
                }
            }

           

//            KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
//                    Company company = (Company) companyResult.getEntityList().get(0);

            String debitNoteDetailID = StringUtil.generateUUID();
            DebitNoteDetail debitNoteDetailRow = new DebitNoteDetail();
            debitNoteDetailRow.setSrno(i + 1);
            debitNoteDetailRow.setID(debitNoteDetailID);
            debitNoteDetailRow.setTotalDiscount(0.00);
            debitNoteDetailRow.setCompany(company);
            
            if (!StringUtil.isNullOrEmpty(invoiceDetailsId)) {
                KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(GoodsReceiptDetail.class.getName(), invoiceDetailsId);
                GoodsReceiptDetail id = (GoodsReceiptDetail) rdresult.getEntityList().get(0);
                debitNoteDetailRow.setGoodsReceiptRow(id);
                }
            if (!StringUtil.isNullOrEmpty(invoiceDetailsId) && (linkMode.equalsIgnoreCase("Customer Invoice") || linkMode.equalsIgnoreCase("Sales Invoice"))) {
                KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(InvoiceDetail.class.getName(), invoiceDetailsId);
                InvoiceDetail id = (InvoiceDetail) rdresult.getEntityList().get(0);
                Invoice invoice = id.getInvoice();
                debitNoteDetailRow.setInvoice(invoice);
                debitNoteDetailRow.setInvoiceRow(id);
                }
//                cnDetailRow.setMemo(jobj.optString("description"));
            DebitNoteTaxEntry taxEntry = new DebitNoteTaxEntry();
            String DebitNoteTaxID = StringUtil.generateUUID();
            taxEntry.setID(DebitNoteTaxID);
            String sales_accid = product.getSalesAccount().getID();

            if (i == 0) {// create  cndetail entry only once in this case i.e if multitple Products are linked.
                dndetails.add(debitNoteDetailRow);
            }

            double amountExcludingTax = 0;
            double rowtaxamountFromJS = jobj.optDouble("taxamounttoadjust", 0);


            totalDNAmt += jobj.optDouble("adjustedamount", 0);
            amountExcludingTax = jobj.optDouble("adjustedamount", 0) - rowtaxamountFromJS;
            totalDNAmtExludingTax += amountExcludingTax;

            JSONObject jedjson = new JSONObject();
            jedjson.put("srno", jedetails.size() + 1);
            jedjson.put("companyid", company.getCompanyID());
            jedjson.put("amount", amountExcludingTax);
            jedjson.put("accountid", sales_accid);
            jedjson.put("debit", false);
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
                    jedjson.put("debit", false);
                    jedjson.put("jeid", je.getID());
                    jedjson.put("description", "");
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
//                    taxEntry.setCreditNote(null);
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

            dnTaxEntryDetails.add(taxEntry);
        }
        
        srRowsDetails.add(dndetails);
        srRowsDetails.add(jedetails);
        srRowsDetails.add(dnTaxEntryDetails);
        srRowsDetails.add(totalDNAmt);
        srRowsDetails.add(totalDNAmtExludingTax);
        return srRowsDetails;
    }
    
    public ModelAndView getDebitNoteMergedForPayment(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        String msg = "";
        boolean issuccess = false;
        try {
            HashMap<String, Object> requestParams = gettDebitNoteMap(request);
            boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
            String[] companyids = (consolidateFlag && request.getParameter("companyids") != null) ? request.getParameter("companyids").split(",") : sessionHandlerImpl.getCompanyid(request).split(",");
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid") != null) ? request.getParameter("gcurrencyid") : sessionHandlerImpl.getCurrencyID(request);
            boolean eliminateflag = consolidateFlag;
            boolean isEdit = request.getParameter("isEdit") == null ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
            requestParams.put("isEdit", isEdit);
            
            boolean onlyAmountDue = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("onlyAmountDue"))) {
                onlyAmountDue = Boolean.parseBoolean(request.getParameter("onlyAmountDue"));
            }
            requestParams.put("onlyAmountDue", onlyAmountDue);

            HashSet dnList = new HashSet();
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
                requestParams.put("companyid", companyid);
                boolean isNoteForPayment = false;
                boolean isNewUI = true; // this flag is used in accDebitNoteImpl/getDebitNoteMerged for getting credit notes for new design of Payment Module
                boolean isVendor = false;
                if (!StringUtil.isNullOrEmpty(request.getParameter("isNoteForPayment"))) {
                    isNoteForPayment = Boolean.parseBoolean(request.getParameter("isNoteForPayment"));
                    isVendor = Boolean.parseBoolean(request.getParameter("isVendor"));
                }
                requestParams.put("isNoteForPayment", isNoteForPayment);
                requestParams.put("isNewUI", isNewUI);
                result = accDebitNoteobj.getDebitNoteMerged(requestParams);
                if (isEdit && !StringUtil.isNullOrEmpty(request.getParameter("billId").toString())) {
                    KwlReturnObject dnResult = accDebitNoteobj.getCustomerDnPayment(request.getParameter("billId"));
                    Iterator dnItr = dnResult.getEntityList().iterator();
                    while (dnItr.hasNext()) {
                        Object[] objects = (Object[]) dnItr.next();
                        String cnnoteid = objects[0] != null ? (String) objects[1] : "";
                        dnList.add(cnnoteid);
                    }
                }
                DataJArr = getDebitNotesMergedJsonForPayment(requestParams, result.getEntityList(), DataJArr, dnList, isEdit);
                int cntype = StringUtil.isNullOrEmpty(request.getParameter("cntype")) ? 1 : Integer.parseInt(request.getParameter("cntype"));
                 if (requestParams.containsKey(Constants.REQ_startdate) && requestParams.containsKey(Constants.REQ_enddate)) {
                    // I am clearing startdate and enddate  from requestParams because there is no need of startdate and enddate for fetching opening Debit Notes.
                    requestParams.put(Constants.REQ_startdate, "");
                    requestParams.put(Constants.REQ_enddate, "");
                }
                 /*
                 removed  isNoteForPayment flag to solve   ERP-14948
                    opening CN/DN does not load in MP/RP when Document currency and Payment method currency is different
                 */
                if (cntype == 10 || ( isVendor)) { // get Vendor Debit Note
                    result = accDebitNoteobj.getOpeningBalanceDNs(requestParams);
                    requestParams.put("cntype", 10);
                    accDebitNoteService.getOpeningDebitNotesJson(requestParams, result.getEntityList(), DataJArr);
                } else if (cntype == 11 || (!isVendor)) {// get Customer Debit Note  
                    result = accDebitNoteobj.getOpeningBalanceCustomerDNs(requestParams);
                    requestParams.put("cntype", 11);
                    accDebitNoteService.getOpeningDebitNotesJson(requestParams, result.getEntityList(), DataJArr);
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
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getDebitNotesMergedJsonForPayment(HashMap<String, Object> requestParams, List list, JSONArray JArr, HashSet dnList, boolean isEdit) throws ServiceException {
//         JSONArray JArr = new JSONArray();
        try {
            String currencyid = (String) requestParams.get("gcurrencyid");
            String companyid = (String) requestParams.get("companyid");
            String transactionCurrencyId = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("df");
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                if (!isEdit || (isEdit && !dnList.contains((String) row[1]))) { // here, (String)row[1] refers to debit note id

                    KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), (String) row[3]);
                    JournalEntryDetail details = (JournalEntryDetail) resultObject.getEntityList().get(0);

                    JSONObject obj = new JSONObject();
                    resultObject = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), (String) row[1]);
                    DebitNote debitMemo = (DebitNote) resultObject.getEntityList().get(0);
                    JournalEntry je = debitMemo.getJournalEntry();
                    Date debitNoteDate = null;
                    double externalCurrencyRate = 0d;
                    if (debitMemo.isNormalDN()) {
                        je = debitMemo.getJournalEntry();
//                        debitNoteDate = je.getEntryDate();
                        debitNoteDate = debitMemo.getCreationDate();
                        externalCurrencyRate = je.getExternalCurrencyRate();
                    }
                    transactionCurrencyId = (debitMemo.getCurrency() == null ? currency.getCurrencyID() : debitMemo.getCurrency().getCurrencyID());
                    obj.put("currencyidtransaction", transactionCurrencyId);
                    obj.put("currencysymboltransaction", (debitMemo.getCurrency() == null ? currency.getSymbol() : debitMemo.getCurrency().getSymbol()));
                  
                    String currencyFilterForTrans = "";
                    boolean isNoteForPayment = false;
                    if (requestParams.get("isNoteForPayment") != null) {
                        isNoteForPayment = (Boolean) requestParams.get("isNoteForPayment");
                    }
                    if (requestParams.containsKey("currencyfilterfortrans") && isNoteForPayment) {
                        currencyFilterForTrans = requestParams.get("currencyfilterfortrans") != null ? (String) requestParams.get("currencyfilterfortrans") : "";
                        KwlReturnObject curresultpayment = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyFilterForTrans);
                        KWLCurrency currencyPayment = (KWLCurrency) curresultpayment.getEntityList().get(0);
                        obj.put("currencysymbol", (currencyPayment == null ? currency.getSymbol() : currencyPayment.getSymbol()));
                        obj.put("currencyid", (currencyPayment == null ? currency.getCurrencyID() : currencyPayment.getCurrencyID()));
                    } else {
                        obj.put("currencysymbol", (debitMemo.getCurrency() == null ? currency.getSymbol() : debitMemo.getCurrency().getSymbol()));
                        obj.put("currencyid", (debitMemo.getCurrency() == null ? currency.getCurrencyID() : debitMemo.getCurrency().getCurrencyID()));

                    }
                    double amountdue = debitMemo.isOtherwise() || debitMemo.getDntype()==5 ? debitMemo.getDnamountdue() : 0;    //debitMemo.getDntype()==5 in case of debit note against customer for malaysian country(ERP-28249)
                    double amountDueOriginal = debitMemo.isOtherwise() || debitMemo.getDntype()==5  ? debitMemo.getDnamountdue() : 0;   //debitMemo.getDntype()==5 in case of debit note against customer for malaysian country(ERP-28249)
                    if (!StringUtil.isNullOrEmpty(currencyFilterForTrans) && !StringUtil.isNullOrEmpty(transactionCurrencyId) && !currencyFilterForTrans.equals(transactionCurrencyId)) {
                        KwlReturnObject bAmtCurrencyFilter = null;
                        bAmtCurrencyFilter = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, amountdue, transactionCurrencyId, currencyFilterForTrans, debitNoteDate, externalCurrencyRate);
                        amountdue = (Double) bAmtCurrencyFilter.getEntityList().get(0);
                    }
                    amountdue = authHandler.round(amountdue, companyid);

                    obj.put("noteid", debitMemo.getID());
                    obj.put("noteno", debitMemo.getDebitNoteNumber());
                    obj.put("amount", debitMemo.isOtherwise() ? debitMemo.getDnamount() : details.getAmount());
                    obj.put("amountdue", (amountdue <= 0) ? 0 : authHandler.round(amountdue, companyid));
                    obj.put("exchangeratefortransaction", (amountDueOriginal <= 0 && amountdue <= 0) ? 0 : (amountdue / amountDueOriginal));
                    obj.put("amountDueOriginal", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal, companyid));
                    obj.put("amountDueOriginalSaved", (amountDueOriginal <= 0) ? 0 : authHandler.round(amountDueOriginal, companyid));
//                    obj.put("date", df.format(je.getEntryDate()));
                    obj.put("date", df.format(debitMemo.getCreationDate()));
                    /**
                     * Passing JE date in response for validating date on JS
                     * Side ERM-655.
                     */
                    obj.put("jeDate", je != null ? df.format(je.getEntryDate()) : debitMemo.getCreationDate());
                    obj.put("accountid", debitMemo.getAccount() == null ? "" : debitMemo.getAccount().getID());
                    obj.put("accountnames", debitMemo.getAccount() == null ? "" : debitMemo.getAccount().getName());
                    /*
                     * Get global custom data for Receipt
                     */
                    accDebitNoteService.getDebitNoteCustomDataForPayment(requestParams, obj, debitMemo, je);
                    if (requestParams.containsKey("isReceipt") && obj.optDouble("amountdue", 0.0) != 0) {
                        JArr.put(obj);
                    } else if (!requestParams.containsKey("isReceipt")) {
                        JArr.put(obj);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accDebitNoteController.getDebitNotesJson : " + ex.getMessage(), ex);
        }
        return JArr;
    }


    
    /**
     * Description : Method is used to Update Entry in linking information table
     * for Debit Note & Invoice If any Invoice linked with Debit Note
     *
     * @param <request> :-used to get sub domain for which you want to update
     * entry in linking information table
     * @param <response>:- used to send response
     * @return :JSONObject(contains success & Message whether script is
     * completed or not)
     */
     public ModelAndView updatePILinkingInformationWithDNScript(HttpServletRequest request, HttpServletResponse response) {
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
            rCompanyId = accPurchaseOrderobj.getAllCompanyFromDb(subdomainArray);
            Iterator itrCompanyId = rCompanyId.getEntityList().iterator();
         
            while (itrCompanyId.hasNext()) {
                String companyid = (String) itrCompanyId.next();
                requestParams.put("companyid", companyid);
                KwlReturnObject result = accPurchaseOrderobj.getLinkedDebitNoteWithPI(requestParams);
                Iterator itr2 = result.getEntityList().iterator();
                while (itr2.hasNext()) {
                    String debitnoteid = (String) itr2.next();
                    if (!StringUtil.isNullOrEmpty(debitnoteid)) {
                        /*
                         * Method is used for updating linking information of
                         *
                         * Debit Note in linking table linked with Purchase Invoice
                         */

                         accDebitNoteService.updateLinkingInformationOfDebitNote(debitnoteid);
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
            Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", "Script completed for Updating Linking Information for Purchase Invoice linked with Debit Note");

            } catch (JSONException ex) {
                Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
}
