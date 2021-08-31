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
package com.krawler.spring.accounting.receipt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krawler.common.admin.AuditAction; 
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
//import com.krawler.customFieldMaster.fieldDataManager;
import static com.krawler.esp.web.resource.Links.loginpageFull;
import com.krawler.hql.accounting.*;
import com.krawler.hql.accounting.journalentry.service.AccJournalEntryModuleService;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.accounting.bankreconciliation.accBankReconciliationDAO;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesCMN;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesConstants;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignHandler;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.invoice.InvoiceConstants;
import com.krawler.spring.accounting.invoice.accInvoiceController;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.accounting.payment.accPaymentService;
import com.krawler.spring.accounting.receivepayment.service.AccReceivePaymentModuleService;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.importFunctionality.ImportDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.common.kwlCommonTablesDAO;
import java.io.*;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
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
public class accReceiptControllerNew extends MultiActionController implements MessageSourceAware {

    private HibernateTransactionManager txnManager;
    private accReceiptDAO accReceiptDAOobj;
    private accJournalEntryDAO accJournalEntryobj;
    private accPaymentDAO accPaymentDAOobj;
    private accTaxDAO accTaxObj;
    private accCurrencyDAO accCurrencyobj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private String successView;
    private accAccountDAO accAccountDAOobj;
    private auditTrailDAO auditTrailObj;
    private fieldDataManager fieldDataManagercntrl;
    private accBankReconciliationDAO accBankReconciliationObj;
    private accInvoiceDAO accInvoiceDAOObj;
    private MessageSource messageSource;
    public ImportHandler importHandler;
    private ImportDAO importDao;
    public accCustomerDAO accCustomerDAOObj;
    private accPaymentService paymentService;
    private accReceiptControllerNew.EnglishNumberToWords EnglishNumberToWordsOjb = new accReceiptControllerNew.EnglishNumberToWords();
    private accGoodsReceiptDAO accGoodsReceiptobj;
    private AccReceivePaymentModuleService accReceivePaymentModuleServiceObj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;

    
    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }
    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
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

    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }

    public void setaccReceiptDAO(accReceiptDAO accReceiptDAOobj) {
        this.accReceiptDAOobj = accReceiptDAOobj;
    }

    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }

    public void setaccPaymentDAO(accPaymentDAO accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyobj) {
        this.accCurrencyobj = accCurrencyobj;
    }

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
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

    public void setaccBankReconciliationDAO(accBankReconciliationDAO accBankReconciliationObj) {
        this.accBankReconciliationObj = accBankReconciliationObj;
    }

    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }

    public void setAccInvoiceDAO(accInvoiceDAO accInvoiceDAOObj) {
        this.accInvoiceDAOObj = accInvoiceDAOObj;
    }
    public void setaccPaymentService(accPaymentService paymentService) {
        this.paymentService = paymentService;
    }
    
    public accGoodsReceiptDAO getAccGoodsReceiptobj() {
        return accGoodsReceiptobj;
    }

    public void setAccGoodsReceiptobj(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
    }
    
    /**
     * @param accReceivePaymentModuleServiceObj the accReceivePaymentModuleServiceObj to set
     */
    public void setAccReceivePaymentModuleServiceObj(AccReceivePaymentModuleService accReceivePaymentModuleServiceObj) {
        this.accReceivePaymentModuleServiceObj = accReceivePaymentModuleServiceObj;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }
    public ModelAndView saveReceiptOld(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject jobjDetails = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean issuccess = false;
        boolean isChequePrint = false;
        String msg = "";
        String paymentid = "";
        String billno = "";
        String advanceamount = "";
        int receipttype = -1;
        String amountpayment = "", accountaddress = "", accountName = "", JENumBer = "";;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("BR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            List li = saveReceipt(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String[] id = (String[]) li.get(0);
            paymentid = (String) li.get(4);
            String[] amountnew = (String[]) li.get(1);
            amountpayment = amountnew[1].intern();
            billno = (String) li.get(5);
            advanceamount = (String) li.get(6);
            accountaddress = (String) li.get(7);
            accountName = (String) li.get(8);
            if (li.get(9) != null) {
                JENumBer = (String) li.get(9);
            }
            receipttype = (Integer) li.get(10);
            SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
            if (!StringUtil.isNullOrEmpty(request.getParameter("isChequePrint"))) {
                isChequePrint = Boolean.parseBoolean(request.getParameter("isChequePrint"));
            }
            if (isChequePrint) {
                Date creationDate = new Date(request.getParameter("creationdate"));
                String date = DATE_FORMAT.format(creationDate);
                String[] amount = (String[]) li.get(1);
                String[] amount1 = (String[]) li.get(2);
                String[] accName = (String[]) li.get(3);
                jobjDetails.put(amount[0], amount[1]);
                jobjDetails.put(amount1[0], amount1[1]);
                jobjDetails.put(accName[0], accName[1]);
                jobjDetails.put("date", date);
                jArr.put(jobjDetails);
            }
            issuccess = true;
            msg = messageSource.getMessage("acc.receipt.save", null, RequestContextUtils.getLocale(request)) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + billno + ",</b>" + messageSource.getMessage("acc.field.JENo", null, RequestContextUtils.getLocale(request)) + ": <b>" + JENumBer + "</b>";   //"Receipt has been saved successfully";
            txnManager.commit(status);
            status = txnManager.getTransaction(def);
            deleteJEArray(id[0], companyid);
            txnManager.commit(status);
            status = txnManager.getTransaction(def);
            deleteChequeOrCard(id[1], companyid);
            txnManager.commit(status);
            if (request.getParameter("isEdit") != null && Boolean.parseBoolean(request.getParameter("isEdit"))) {
                deleteReceiptForEdit(request, response);
            }
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("paymentid", paymentid);
                jobj.put("data", jArr);
                jobj.put("billno", billno);
                jobj.put("advanceamount", advanceamount);
                jobj.put("amount", amountpayment);
                jobj.put("address", accountaddress);
                jobj.put("accountName", accountName);
                jobj.put("receipttype", receipttype);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    /**
     * Description : Below Method is used to Update Receipt 
     * @param <request> used to get request Params
       @param <response> used to send client side
     * @return :ModelAndView
     */
    public ModelAndView updateReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> hashMap = updateCustomerReceiptDetails(request, response);
            jobj = (JSONObject) hashMap.get("jobj");
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            issuccess = false;
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /**
     * Description : Below Method is used to Update Receipt 
     * @param <request> used to get request Params
       @param <response> used to send client side
     * @return :HashMap
     */
    public HashMap<String, Object> updateCustomerReceiptDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        String paymentid = "";
        String billno = "";
        String advanceamount = "";
        KwlReturnObject result = null;
        boolean isEdit = false;
        int receipttype = -1;
        String AuditMsg = "", custVenEmailId = "";
        String amountpayment = "", accountaddress = "", accountName = "", JENumBer = "";
        HashMap<String, Object> hashMap = new HashMap<String, Object>();

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("BR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        String jeEntryNo = "";
        TransactionStatus status = null;
        JSONArray jSONArrayAgainstInvoice = new JSONArray();
        String companyid = "", memo = "", receivedFrom = "";
        Map<String, Object> commonReceiptMap = new HashMap<String, Object>();
        boolean accexception = false;
        try {
            status = txnManager.getTransaction(def);
            companyid = sessionHandlerImpl.getCompanyid(request);
            commonReceiptMap.put("companyid", companyid);
            Receipt editReceiptObject = null;

            if (request.getParameter("memo") != null) {
                commonReceiptMap.put("memo", (String) request.getParameter("memo"));
            }
            if (request.getParameter("paidToCmb") != null) {
                commonReceiptMap.put("receivedfrom", (String) request.getParameter("paidToCmb"));
            }
            if (request.getParameter("billid") != null) {
                commonReceiptMap.put("billid", (String) request.getParameter("billid"));
            }
            if (request.getParameter("customfield") != null) {
                commonReceiptMap.put("customfield", (String) request.getParameter("customfield"));
            }

            /* Update Receipt */
            editReceiptObject = updateReceipt(commonReceiptMap);

            if (editReceiptObject != null) {
                isEdit = true;
                billno = editReceiptObject.getReceiptNumber();
                jeEntryNo = editReceiptObject.getJournalEntry().getEntryNumber();

                String detailsJsonString = request.getParameter("Details");
                JSONArray jSONArray = new JSONArray(detailsJsonString);
                JSONArray jSONArrayAdvance = new JSONArray();
                JSONArray jSONArrayLocalAdvance = new JSONArray();
                JSONArray jSONArrayExportAdvance = new JSONArray();

                JSONArray jSONArrayCNDN = new JSONArray();
                JSONArray jSONArrayGL = new JSONArray();
                JSONArray jSONArrayLoanDisbursement = new JSONArray();

                for (int i = 0; i < jSONArray.length(); i++) {
                    JSONObject jSONObject = jSONArray.getJSONObject(i);
                    if (jSONObject.has("type") && jSONObject.optInt("type", 0) != 0 && jSONObject.has("enteramount") && jSONObject.optDouble("enteramount", 0.0) != 0) {
                        if (jSONObject.getInt("type") == Constants.AdvancePayment) {
                            jSONArrayAdvance.put(jSONObject);
                        } else if (jSONObject.getInt("type") == Constants.PaymentAgainstInvoice) {
                            jSONArrayAgainstInvoice.put(jSONObject);
                        } else if (jSONObject.getInt("type") == Constants.PaymentAgainstCNDN) {
                            jSONArrayCNDN.put(jSONObject);
                        } else if (jSONObject.getInt("type") == Constants.GLPayment) {
                            jSONArrayGL.put(jSONObject);
                        } else if (jSONObject.getInt("type") == Constants.LocalAdvanceTypePayment) {
                            jSONArrayLocalAdvance.put(jSONObject);
                        } else if (jSONObject.getInt("type") == Constants.ExportAdvanceTypePayment) {
                            jSONArrayExportAdvance.put(jSONObject);
                        } else if (jSONObject.getInt("type") == Constants.PaymentAgainstLoanDisbursement) {
                            jSONArrayLoanDisbursement.put(jSONObject);
                        }
                    }
                }

                /* Update  Receipt Advance details */
                if (jSONArrayAdvance.length() > 0) {
                    updateReceiptAdvanceDetails(jSONArrayAdvance, editReceiptObject, commonReceiptMap);
                }

                /* Update  Invoice details */
                if (jSONArrayAgainstInvoice.length() > 0) {

                    updateInvoiceDetails(jSONArrayAgainstInvoice, editReceiptObject, commonReceiptMap);
                }

                /* Update  Debit Note  details */
                if (jSONArrayCNDN.length() > 0) {
                    updateDebitNoteDetails(jSONArrayCNDN, editReceiptObject, commonReceiptMap);
                }

                /* Update  GL  details */
                if (jSONArrayGL.length() > 0) {
                    updateGLDetails(jSONArrayGL, editReceiptObject, commonReceiptMap);

                }
            }
            String action = "made";
            if (isEdit == true) {
                action = "updated";
            }
            txnManager.commit(status);
            issuccess = true;

            msg = messageSource.getMessage("acc.receipt.save", null, RequestContextUtils.getLocale(request)) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + billno + ",</b>" + messageSource.getMessage("acc.field.JENo", null, RequestContextUtils.getLocale(request)) + ": <b>" + JENumBer + "" + jeEntryNo + "</b>";   //"Receipt has been saved successfully";

            auditTrailObj.insertAuditLog(AuditAction.RECEIPT_ADDED, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + " a Receipt " + billno + AuditMsg, request, editReceiptObject.getID());

        } catch (SessionExpiredException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }

            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accReceiptControllerNew.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("paymentid", paymentid);
                jobj.put("data", jArr);
                jobj.put("billno", billno);
                jobj.put("isAccountingExe", accexception);
                jobj.put("advanceamount", advanceamount);
                jobj.put("amount", amountpayment);
                jobj.put("address", accountaddress);
                jobj.put("accountName", accountName);
                jobj.put("billingEmail", custVenEmailId);
                jobj.put("receipttype", receipttype);
                hashMap.put("jobj", jobj);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return hashMap;
    }
    
    /**
     * Description : Below Method is used to Update Receipt 
     * @param <commonReceiptMap> used to get common receipt params
     * @return :Receipt
     */
    public Receipt updateReceipt(Map<String, Object> commonReceiptMap) throws ServiceException, JSONException {

        String companyid = "", memo = "", receivedFrom = "", customfield = "";
        String receiptid = "";
        Receipt editReceiptObject = null;
        KwlReturnObject result = null;

        if (commonReceiptMap.containsKey("companyid") && commonReceiptMap.get("companyid") != null) {
            companyid = (String) commonReceiptMap.get("companyid");
        }
        if (commonReceiptMap.containsKey("billid") && commonReceiptMap.get("billid") != null) {
            receiptid = (String) commonReceiptMap.get("billid");
        }
        if (commonReceiptMap.containsKey("memo") && commonReceiptMap.get("memo") != null) {
            memo = (String) commonReceiptMap.get("memo");
        }
        if (commonReceiptMap.containsKey("receivedfrom") && commonReceiptMap.get("receivedfrom") != null) {
            receivedFrom = (String) commonReceiptMap.get("receivedfrom");
        }

        if (commonReceiptMap.containsKey("customfield") && commonReceiptMap.get("customfield") != null) {
            customfield = (String) commonReceiptMap.get("customfield");
        }

        /* Creat Receipt Object */
        if (!StringUtil.isNullOrEmpty(receiptid)) {
            KwlReturnObject receiptObj = accountingHandlerDAOobj.getObject(Receipt.class.getName(), receiptid);
            editReceiptObject = (Receipt) receiptObj.getEntityList().get(0);
        }

        if (editReceiptObject != null) {

            editReceiptObject.setMemo(memo);

            MasterItem masterItem = null;
            if (!StringUtil.isNullOrEmpty(receivedFrom)) {
                result = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), receivedFrom);
                masterItem = (MasterItem) result.getEntityList().get(0);
                if (masterItem != null) {

                    editReceiptObject.setReceivedFrom(masterItem);
                }
            }

            JournalEntry journalEntry = editReceiptObject.getJournalEntry();

            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                customrequestParams.put("modulerecid", journalEntry.getID());
                customrequestParams.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    KwlReturnObject receiptAccJECustomData = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), journalEntry.getID());
                    AccJECustomData accJECustomData = (AccJECustomData) receiptAccJECustomData.getEntityList().get(0);
                    journalEntry.setAccBillInvCustomData(accJECustomData);
                }
            }
        }

        return editReceiptObject;

    }

    /**
     * Description : Below Method is used to update Receipt Advance Details
     * @param <jSONArrayAdvance> used to get Receipt Advance Details
     * @param <editReceiptObject> used to update receipt
     * @param <commonReceiptMap> used to get common receipt params
     * @return :void
     */
    public void updateReceiptAdvanceDetails(JSONArray jSONArrayAdvance, Receipt editReceiptObject, Map<String, Object> commonReceiptMap) throws JSONException, ServiceException {

        JSONObject advancejobj = null;
        String companyid = "";
        if (commonReceiptMap.containsKey("companyid")) {
            companyid = (String) commonReceiptMap.get("companyid");
        }
        for (int i = 0; i < jSONArrayAdvance.length(); i++) {

            advancejobj = jSONArrayAdvance.getJSONObject(i);
            ReceiptAdvanceDetail row = null;
            if (advancejobj.has("rowdetailid")) {

                KwlReturnObject receiptAdvanceDetail = accountingHandlerDAOobj.getObject(ReceiptAdvanceDetail.class.getName(), advancejobj.getString("rowdetailid"));
                row = (ReceiptAdvanceDetail) receiptAdvanceDetail.getEntityList().get(0);
            }

            if (row != null) {
                try {
                    row.setDescription(StringUtil.DecodeText(advancejobj.optString("description","")));
                } catch (Exception ex) {
                    row.setDescription(advancejobj.optString("description",""));
                }
            }

            HashMap<String, JSONArray> jcustomarrayMap = new HashMap();

            if (!StringUtil.isNullOrEmpty(advancejobj.optString("customfield", ""))) {
                JSONArray jcustomarray = new JSONArray(advancejobj.optString("customfield", "[]"));
                jcustomarrayMap.put(row.getTotalJED().getID(), jcustomarray);
            }

            editReceiptObject.setJcustomarrayMap(jcustomarrayMap);

            HashMap<String, JSONArray> jcustomarrayMap1 = editReceiptObject.getJcustomarrayMap();
            JSONArray jcustomarray = jcustomarrayMap1.get(row.getTotalJED().getID());
            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
            customrequestParams.put("customarray", jcustomarray);
            customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
            customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
            customrequestParams.put("modulerecid", row.getTotalJED().getID());
            customrequestParams.put("recdetailId", row.getId());
            customrequestParams.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
            customrequestParams.put("companyid", companyid);
            customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                KwlReturnObject receiptAccJEDCustomData = accountingHandlerDAOobj.getObject(AccJEDetailCustomData.class.getName(), row.getTotalJED().getID());
                AccJEDetailCustomData accJEDetailCustomData = (AccJEDetailCustomData) receiptAccJEDCustomData.getEntityList().get(0);
                KwlReturnObject receiptJED = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), row.getTotalJED().getID());
                JournalEntryDetail journalEntryDetail = (JournalEntryDetail) receiptJED.getEntityList().get(0);
                journalEntryDetail.setAccJEDetailCustomData(accJEDetailCustomData);
            }

        }

    }

    /**
     * Description : Below Method is used to update Receipt Invoice Details
     * @param <jSONArrayAdvance> used to get Receipt Invoice Details
     * @param <editReceiptObject> used to update receipt
     * @param <commonReceiptMap> used to get common receipt params
     * @return :void
     */
    public void updateInvoiceDetails(JSONArray jSONArrayAgainstInvoice, Receipt editReceiptObject, Map<String, Object> commonReceiptMap) throws JSONException, ServiceException {
        JSONObject againstInvoicejobj = null;
        String companyid = "";
        if (commonReceiptMap.containsKey("companyid")) {
            companyid = (String) commonReceiptMap.get("companyid");
        }

        for (int i = 0; i < jSONArrayAgainstInvoice.length(); i++) {

            againstInvoicejobj = jSONArrayAgainstInvoice.getJSONObject(i);
            ReceiptDetail row = null;
            if (againstInvoicejobj.has("rowdetailid")) {

                KwlReturnObject receiptAdvanceDetail = accountingHandlerDAOobj.getObject(ReceiptDetail.class.getName(), againstInvoicejobj.getString("rowdetailid"));
                row = (ReceiptDetail) receiptAdvanceDetail.getEntityList().get(0);
            }

            if (row != null) {

                try {
                    row.setDescription(StringUtil.DecodeText(againstInvoicejobj.optString("description", "")));
                } catch (Exception ex) {
                    row.setDescription(againstInvoicejobj.optString("description", ""));
                }

            }

            HashMap<String, JSONArray> jcustomarrayMap = new HashMap();

            if (!StringUtil.isNullOrEmpty(againstInvoicejobj.optString("customfield", ""))) {
                JSONArray jcustomarray = new JSONArray(againstInvoicejobj.optString("customfield", "[]"));
                jcustomarrayMap.put(row.getTotalJED().getID(), jcustomarray);
            }

            editReceiptObject.setJcustomarrayMap(jcustomarrayMap);

            HashMap<String, JSONArray> jcustomarrayMap1 = editReceiptObject.getJcustomarrayMap();
            JSONArray jcustomarray = jcustomarrayMap1.get(row.getTotalJED().getID());
            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();

            customrequestParams.put("customarray", jcustomarray);
            customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
            customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
            customrequestParams.put("modulerecid", row.getTotalJED().getID());
            customrequestParams.put("recdetailId", row.getID());
            customrequestParams.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
            customrequestParams.put("companyid", companyid);
            customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                KwlReturnObject receiptAccJEDCustomData = accountingHandlerDAOobj.getObject(AccJEDetailCustomData.class.getName(), row.getTotalJED().getID());
                AccJEDetailCustomData accJEDetailCustomData = (AccJEDetailCustomData) receiptAccJEDCustomData.getEntityList().get(0);
                KwlReturnObject receiptJED = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), row.getTotalJED().getID());
                JournalEntryDetail journalEntryDetail = (JournalEntryDetail) receiptJED.getEntityList().get(0);
                journalEntryDetail.setAccJEDetailCustomData(accJEDetailCustomData);
            }

        }

    }
    /**
     * Description : Below Method is used to update Debit Note Details
     * @param <jSONArrayAdvance> used to get Receipt Advance Details
     * @param <editReceiptObject> used to update receipt
     * @param <commonReceiptMap> used to get common receipt params
     * @return :void
     */
    public void updateDebitNoteDetails(JSONArray jSONArrayCNDN, Receipt editReceiptObject, Map<String, Object> commonReceiptMap) throws JSONException, ServiceException {
        JSONObject againstDebitNotejobj = null;
        String companyid = "";
        if (commonReceiptMap.containsKey("companyid")) {
            companyid = (String) commonReceiptMap.get("companyid");
        }

        for (int i = 0; i < jSONArrayCNDN.length(); i++) {

            againstDebitNotejobj = jSONArrayCNDN.getJSONObject(i);
            DebitNotePaymentDetails row = null;
            if (againstDebitNotejobj.has("rowdetailid")) {

                KwlReturnObject receiptAdvanceDetail = accountingHandlerDAOobj.getObject(DebitNotePaymentDetails.class.getName(), againstDebitNotejobj.getString("rowdetailid"));
                row = (DebitNotePaymentDetails) receiptAdvanceDetail.getEntityList().get(0);
            }

            if (row != null) {
                try {
                    row.setDescription(StringUtil.DecodeText(againstDebitNotejobj.optString("description","")));
                } catch (Exception ex) {
                    row.setDescription(againstDebitNotejobj.optString("description",""));
                }
            }

            HashMap<String, JSONArray> jcustomarrayMap = new HashMap();

            if (!StringUtil.isNullOrEmpty(againstDebitNotejobj.optString("customfield", ""))) {
                JSONArray jcustomarray = new JSONArray(againstDebitNotejobj.optString("customfield", "[]"));
                jcustomarrayMap.put(row.getTotalJED().getID(), jcustomarray);
            }

            editReceiptObject.setJcustomarrayMap(jcustomarrayMap);

            HashMap<String, JSONArray> jcustomarrayMap1 = editReceiptObject.getJcustomarrayMap();
            JSONArray jcustomarray = jcustomarrayMap1.get(row.getTotalJED().getID());
            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();

            customrequestParams.put("customarray", jcustomarray);
            customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
            customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
            customrequestParams.put("modulerecid", row.getTotalJED().getID());
            customrequestParams.put("recdetailId", row.getID());
            customrequestParams.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
            customrequestParams.put("companyid", companyid);
            customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                KwlReturnObject receiptAccJEDCustomData = accountingHandlerDAOobj.getObject(AccJEDetailCustomData.class.getName(), row.getTotalJED().getID());
                AccJEDetailCustomData accJEDetailCustomData = (AccJEDetailCustomData) receiptAccJEDCustomData.getEntityList().get(0);
                KwlReturnObject receiptJED = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), row.getTotalJED().getID());
                JournalEntryDetail journalEntryDetail = (JournalEntryDetail) receiptJED.getEntityList().get(0);
                journalEntryDetail.setAccJEDetailCustomData(accJEDetailCustomData);
            }

        }
    }
   /**
     * Description : Below Method is used to update GL Details
     * @param <jSONArrayAdvance> used to get GL Details
     * @param <editReceiptObject> used to update receipt
     * @param <commonReceiptMap> used to get common receipt params
     * @return :void
     */
    public void updateGLDetails(JSONArray jSONArrayGL, Receipt editReceiptObject, Map<String, Object> commonReceiptMap) throws JSONException, ServiceException {
        JSONObject againstGLjobj = null;
        String companyid = "";
        if (commonReceiptMap.containsKey("companyid")) {
            companyid = (String) commonReceiptMap.get("companyid");
        }
        for (int i = 0; i < jSONArrayGL.length(); i++) {

            againstGLjobj = jSONArrayGL.getJSONObject(i);
            ReceiptDetailOtherwise row = null;
            if (againstGLjobj.has("rowdetailid")) {

                KwlReturnObject receiptAdvanceDetail = accountingHandlerDAOobj.getObject(ReceiptDetailOtherwise.class.getName(), againstGLjobj.getString("rowdetailid"));
                row = (ReceiptDetailOtherwise) receiptAdvanceDetail.getEntityList().get(0);
            }

            if (row != null) {
                try {
                    row.setDescription(StringUtil.DecodeText(againstGLjobj.optString("description","")));
                } catch (Exception ex) {
                    row.setDescription(againstGLjobj.optString("description",""));
                }
            }

            HashMap<String, JSONArray> jcustomarrayMap = new HashMap();

            if (!StringUtil.isNullOrEmpty(againstGLjobj.optString("customfield", ""))) {
                JSONArray jcustomarray = new JSONArray(againstGLjobj.optString("customfield", "[]"));
                jcustomarrayMap.put(row.getTotalJED().getID(), jcustomarray);
            }

            editReceiptObject.setJcustomarrayMap(jcustomarrayMap);

            HashMap<String, JSONArray> jcustomarrayMap1 = editReceiptObject.getJcustomarrayMap();
            JSONArray jcustomarray = jcustomarrayMap1.get(row.getTotalJED().getID());
            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();

            customrequestParams.put("customarray", jcustomarray);
            customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
            customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
            customrequestParams.put("modulerecid", row.getTotalJED().getID());
            customrequestParams.put("recdetailId", row.getID());
            customrequestParams.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
            customrequestParams.put("companyid", companyid);
            customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                KwlReturnObject receiptAccJEDCustomData = accountingHandlerDAOobj.getObject(AccJEDetailCustomData.class.getName(), row.getTotalJED().getID());
                AccJEDetailCustomData accJEDetailCustomData = (AccJEDetailCustomData) receiptAccJEDCustomData.getEntityList().get(0);
                KwlReturnObject receiptJED = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), row.getTotalJED().getID());
                JournalEntryDetail journalEntryDetail = (JournalEntryDetail) receiptJED.getEntityList().get(0);
                journalEntryDetail.setAccJEDetailCustomData(accJEDetailCustomData);
            }

        }

    }
    
    public ModelAndView saveReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
         boolean issuccess = false;
         String msg = "";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            paramJobj.put("locale", RequestContextUtils.getLocale(request));
            HashMap<String, Object> hashMap = accReceivePaymentModuleServiceObj.saveCustomerReceipt(paramJobj);
            jobj=(JSONObject)hashMap.get("jobj");
        }catch (Exception ex) {
            msg = "" + ex.getMessage();
            issuccess = false;
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    
    /* --------Function to create bulk Receive Payment grouped on different customer------- */
    public ModelAndView saveBulkCustomerReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            paramJobj.put("locale", RequestContextUtils.getLocale(request));
            HashMap<String, Object> hashMap = accReceivePaymentModuleServiceObj.saveBulkCustomerReceipt(paramJobj);
            jobj = (JSONObject) hashMap.get("jobj");
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            issuccess = false;
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    
    /**
     * This Method is used to Import Receive Payment 
     * @param request
     * @param response
     * @return 
     */
    public ModelAndView importReceivePayment(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            /* Get Import related global parameters */
            JSONObject paramJobj = getReceivePaymentParams(request);
            /* Call validate and import data of VQ. */
            jobj = accReceivePaymentModuleServiceObj.importReceivePaymentJSON(paramJobj);
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    /**
     * Description : This Method is used to Get Request params for import Receive Payment
     * @param request
     * @return JSONObject
     * @throws JSONException
     * @throws SessionExpiredException 
     */
    public JSONObject getReceivePaymentParams(HttpServletRequest request) throws JSONException, SessionExpiredException {
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        paramJobj.put("servletContext", this.getServletContext());
        return paramJobj;
    }
    
    /*
    * The public HashMap<String, Object> saveCustomerReceipt(HttpServletRequest request, HttpServletResponse response) as it was moved to service layer 
      as per discussion with Sagar M sir removed the function from this class  
    */        
    //If Exception occured or payment completed  then delete entry from temporary table
    public void deleteTemporaryInvoicesEntries(JSONArray jSONArrayAgainstInvoice,String companyid) {
        try {
            for (int i = 0; i < jSONArrayAgainstInvoice.length(); i++) {
                JSONObject invoiceJobj = jSONArrayAgainstInvoice.getJSONObject(i);
                String invoiceId = invoiceJobj.getString("documentid");
                accPaymentDAOobj.deleteUsedInvoiceOrCheque(invoiceId, companyid);
            }
        } catch (Exception ex) {
            Logger.getLogger(accReceiptControllerNew.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    public void setValuesForAuditTrialMessage(Receipt oldgrd,Map<String,Object> oldgreceipt, Map<String, Object>newAuditKey) throws SessionExpiredException {
        try {
            if (oldgrd != null) {
                //Receipt Number Change
                oldgreceipt.put(Constants.ReceiptNumber, oldgrd.getReceiptNumber());
                newAuditKey.put(Constants.ReceiptNumber, "Receipt Number");
                //Received From Change
                oldgreceipt.put(Constants.ReceivedFrom, oldgrd.getReceivedFrom() != null ? oldgrd.getReceivedFrom().getValue():"");
                newAuditKey.put(Constants.ReceivedFrom, "Received From");
                //Creation Date
//                oldgreceipt.put(Constants.CreationDate, oldgrd.isIsOpeningBalenceReceipt() ? oldgrd.getCreationDate() : oldgrd.getJournalEntry().getEntryDate());
                oldgreceipt.put(Constants.CreationDate, oldgrd.getCreationDate());
                newAuditKey.put(Constants.CreationDate, "Creation Date");
                //Memo
                oldgreceipt.put(Constants.Memo, oldgrd.getMemo());
                newAuditKey.put(Constants.Memo, "Memo");
                if (oldgrd.getPayDetail() != null && oldgrd.getPayDetail() != null) {
                    int oldPaymentMethodType = oldgrd.getPayDetail().getPaymentMethod().getDetailType();
                    String oldPaymentMethodTypeName = oldgrd.getPayDetail().getPaymentMethod().getMethodName();
                    //PaymentMethodType
                    oldgreceipt.put(Constants.PaymentMethodType, oldPaymentMethodTypeName);
                    newAuditKey.put(Constants.PaymentMethodType, "Payment Method Type");
                    if (oldPaymentMethodType == PaymentMethod.TYPE_BANK) {
                        //Cheque
                        Cheque oldCheck = oldgrd.getPayDetail().getCheque();
                        oldgreceipt.put(Constants.Cheque, oldCheck);
                        newAuditKey.put(Constants.Cheque, "Cheque");
                        //Check Number
                        oldgreceipt.put(Constants.ChequeNumber, oldCheck.getChequeNo());
                        newAuditKey.put(Constants.ChequeNumber, "Cheque Number");
                        //Bank Name
                        oldgreceipt.put(Constants.BankName, oldCheck.getBankName());
                        newAuditKey.put(Constants.BankName, "Bank Name");
                        //Check Date
                        oldgreceipt.put(Constants.CheckDate, oldCheck.getDueDate());
                        newAuditKey.put(Constants.CheckDate, "Check Date");
                    } else if (oldPaymentMethodType == PaymentMethod.TYPE_CARD) {
                        // Card
                        oldgreceipt.put(Constants.Card, oldgrd.getPayDetail().getCard());
                        newAuditKey.put(Constants.Card, "Card");
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accReceiptControllerNew.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
        
    public ModelAndView saveDishonouredReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        String paymentid = "";
        String billno = "";
        String advanceamount = "";
        int receipttype = -1;
        String amountpayment = "", accountaddress = "", accountName = "", JENumBer = "";;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("BR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            List li = saveDashonouredReceipt(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String[] id = (String[]) li.get(0);
            paymentid = (String) li.get(1);
            billno = li.get(2).toString();
            JENumBer = li.get(3).toString();
            issuccess = true;
            msg = messageSource.getMessage("acc.paymentreceived.ChequeforReceipt", null, RequestContextUtils.getLocale(request))+" "+ billno +" "+messageSource.getMessage("acc.paymentreceived.hasDishonouredDishonouredChequeJENo", null, RequestContextUtils.getLocale(request))+" "+ ": <b>" + JENumBer + "</b>";  
            txnManager.commit(status);
            status = txnManager.getTransaction(def);
            txnManager.commit(status);
            status = txnManager.getTransaction(def);
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("paymentid", paymentid);
                jobj.put("data", jArr);
                jobj.put("billno", billno);
                jobj.put("advanceamount", advanceamount);
                jobj.put("amount", amountpayment);
                jobj.put("address", accountaddress);
                jobj.put("accountName", accountName);
                jobj.put("receipttype", receipttype);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }    
    public ModelAndView revertDishonouredReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        String paymentid = "";
        String billno = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("BR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            List li = revertDishonouredReceipt(request);
            paymentid = (String) li.get(0);
            billno = li.get(1).toString();
            Set<String> amountDueUpdatedInvoiceIDSet = (Set<String>) li.get(3);
            issuccess = true;
            msg = messageSource.getMessage("acc.paymentreceived.DishonouredChequeforReceipt", null, RequestContextUtils.getLocale(request))+" "+ billno +" "+messageSource.getMessage("acc.paymentreceived.hasbeenrevertedSuccessfully", null, RequestContextUtils.getLocale(request));  
            txnManager.commit(status);
            
            //========Code for Rounding JE Started=============
            paramJobj.put("reciptNumber", billno);
            try{
                accReceivePaymentModuleServiceObj.postRoundingJEOnRevertDishonouredReceipt(paramJobj,amountDueUpdatedInvoiceIDSet);
            } catch(Exception ex){
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
            //========Code for Rounding JE Ended=============
            
        } catch (AccountingException ex) {
            txnManager.rollback(status);
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("paymentid", paymentid);
                jobj.put("billno", billno);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public List revertDishonouredReceipt(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
        Receipt receipt = null;
        String disHonouredChequeJE = null,disHonouredChequeJENumber="";
        KwlReturnObject jeDetailsResult = null;
        Set<String> amountDueUpdatedInvoiceIDSet = new HashSet<>();
        List ll = new ArrayList();
        try {
            String receiptid = request.getParameter("billid");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            JournalEntry journalEntry = null;

            if (!StringUtil.isNullOrEmpty(receiptid)) {
                KwlReturnObject receiptObj = accountingHandlerDAOobj.getObject(Receipt.class.getName(), receiptid);
                receipt = (Receipt) receiptObj.getEntityList().get(0);
                if (receipt != null && receipt.getDisHonouredChequeJe() != null) {
                    disHonouredChequeJE = receipt.getDisHonouredChequeJe().getID();
                    journalEntry = receipt.getDisHonouredChequeJe();
                    disHonouredChequeJENumber=journalEntry.getEntryNumber();
                    receipt.setIsDishonouredCheque(false);
                    receipt.getJournalEntry().setIsDishonouredCheque(false);
                    accReceiptDAOobj.updateDisHonouredJEFromReceipt(receiptid, companyid);
                    jeDetailsResult = accJournalEntryobj.deleteJEDtails(disHonouredChequeJE, companyid);
                    jeDetailsResult = accJournalEntryobj.deleteJE(disHonouredChequeJE, companyid);

                }
            }
            HashMap<String, Object> reqParams1 = new HashMap();
            reqParams1.put("receiptid", receipt.getID());
            reqParams1.put("companyid", companyid);
            KwlReturnObject linkresult = accReceiptDAOobj.getLinkDetailReceipt(reqParams1);
            List<LinkDetailReceipt> linkedDetaisReceipts = linkresult.getEntityList();
            for (LinkDetailReceipt ldr : linkedDetaisReceipts) {
                Invoice invoice = ldr.getInvoice();
                double amountReceivedConvertedInBaseCurrency = 0d;
                HashMap<String, Object> requestParams = new HashMap();
                requestParams.put(Constants.companyid, receipt.getCompany().getCompanyID());
                requestParams.put("gcurrencyid", receipt.getCompany().getCurrency().getCurrencyID());
                double externalCurrencyRate = 1d;
                boolean isopeningBalanceRCP = receipt.isIsOpeningBalenceReceipt();
                Date rcpCreationDate = null;
                rcpCreationDate = receipt.getCreationDate();
                if (isopeningBalanceRCP) {
                    externalCurrencyRate = receipt.getExchangeRateForOpeningTransaction();
                } else {
//                    rcpCreationDate = receipt.getJournalEntry().getEntryDate();
                    externalCurrencyRate = receipt.getJournalEntry().getExternalCurrencyRate();
                }
                String fromcurrencyid = receipt.getCurrency().getCurrencyID();
                KwlReturnObject bAmt = null;
                if (isopeningBalanceRCP && receipt.isConversionRateFromCurrencyToBase()) {// if Receipt is opening balance Receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, ldr.getAmount(), fromcurrencyid, rcpCreationDate, externalCurrencyRate);
                } else {
                    bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, ldr.getAmount(), fromcurrencyid, rcpCreationDate, externalCurrencyRate);
                }
                amountReceivedConvertedInBaseCurrency = (Double) bAmt.getEntityList().get(0);
                double amountDue=0.0;
                if (invoice.isIsOpeningBalenceInvoice()) {
                    amountDue = invoice.getOpeningBalanceAmountDue();
                } else {
                    amountDue = invoice.getInvoiceamountdue();
                }
                if (amountDue < ldr.getAmountInInvoiceCurrency()) {
                     throw new AccountingException("Invoice(s) linked with selected receipt are already paid, so can not be reverted <b>");
                    
                } else {
                    KwlReturnObject invoiceResult = updateInvoiceAmountDueAndReturnResult(invoice, receipt, receipt.getCompany(), ldr.getAmountInInvoiceCurrency(), amountReceivedConvertedInBaseCurrency);
                    /*
                     * Update the amountduedate while reverting
                     */
                    if (invoiceResult != null && invoiceResult.getEntityList() != null && invoiceResult.getEntityList().size() > 0) {
                        invoice = (Invoice) invoiceResult.getEntityList().get(0);
                        if ((invoice.isIsOpeningBalenceInvoice() && invoice.getOpeningBalanceAmountDue() == 0) || (!invoice.isIsOpeningBalenceInvoice() && invoice.getInvoiceamountdue() == 0)) {
                            try {
                                HashMap<String, Object> dataMap = new HashMap<String, Object>();
//                                dataMap.put("amountduedate", receipt != null ? (receipt.isIsOpeningBalenceReceipt() ? receipt.getCreationDate() : receipt.getJournalEntry().getEntryDate()) : null);
                                dataMap.put("amountduedate", receipt != null ? receipt.getCreationDate() : null);
                                accInvoiceDAOObj.saveInvoiceAmountDueZeroDate(invoice, dataMap);
                            } catch (Exception ex) {
                                System.out.println("" + ex.getMessage());
                            }
                        }
                    }
                }
                amountDueUpdatedInvoiceIDSet.add(invoice.getID());
            }

            Set<ReceiptDetail> receiptDetails = receipt.getRows();
            if (receiptDetails != null && !receiptDetails.isEmpty()) {
                for (ReceiptDetail receiptDetail : receiptDetails) {
                    Invoice invoice = receiptDetail.getInvoice();
                    double amountReceivedConvertedInBaseCurrency = 0d;
                    HashMap<String, Object> requestParams = new HashMap();
                    requestParams.put(Constants.companyid, receipt.getCompany().getCompanyID());
                    requestParams.put("gcurrencyid", receipt.getCompany().getCurrency().getCurrencyID());
                    double externalCurrencyRate = 1d;
                    boolean isopeningBalanceRCP = receipt.isIsOpeningBalenceReceipt();
                    Date rcpCreationDate = null;
                    rcpCreationDate = receipt.getCreationDate();
                    if (isopeningBalanceRCP) {
                        externalCurrencyRate = receipt.getExchangeRateForOpeningTransaction();
                    } else {
//                        rcpCreationDate = receipt.getJournalEntry().getEntryDate();
                        externalCurrencyRate = receipt.getJournalEntry().getExternalCurrencyRate();
                    }
                    String fromcurrencyid = receipt.getCurrency().getCurrencyID();
                    KwlReturnObject bAmt = null;
                    KwlReturnObject bDiscountAmt = null;
                    double discountAmtInInvoiceCurrency = authHandler.round(receiptDetail.getDiscountAmount() / receiptDetail.getExchangeRateForTransaction(), receipt.getCompany().getCompanyID());
                    double discountAmount = receiptDetail.getDiscountAmount();
                    if (isopeningBalanceRCP && receipt.isConversionRateFromCurrencyToBase()) {// if Receipt is opening balance Receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                        bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, receiptDetail.getAmount(), fromcurrencyid, rcpCreationDate, externalCurrencyRate);
                        bDiscountAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, discountAmount, fromcurrencyid, rcpCreationDate, externalCurrencyRate);
                    } else {
                        bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, receiptDetail.getAmount(), fromcurrencyid, rcpCreationDate, externalCurrencyRate);
                        bDiscountAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, discountAmount, fromcurrencyid, rcpCreationDate, externalCurrencyRate);
                    }
                    amountReceivedConvertedInBaseCurrency = (Double) bAmt.getEntityList().get(0);
                    double amountDue = 0.0;
                    if (invoice.isIsOpeningBalenceInvoice()) {
                        amountDue = invoice.getOpeningBalanceAmountDue();
                    } else {
                        amountDue = invoice.getInvoiceamountdue();
                    }
                    if (amountDue < (receiptDetail.getAmountInInvoiceCurrency()+discountAmtInInvoiceCurrency)) {
                        throw new AccountingException("Invoice(s) linked with selected receipt are already paid,so can not be reverted <b>");
                    } else {
                        KwlReturnObject invoiceResult = updateInvoiceAmountDueAndReturnResult(invoice, receipt, receipt.getCompany(), (receiptDetail.getAmountInInvoiceCurrency()+discountAmtInInvoiceCurrency), (amountReceivedConvertedInBaseCurrency+discountAmount));
                        /*
                         * Update the amountduedate while reverting
                         */
                        if (invoiceResult != null && invoiceResult.getEntityList() != null && invoiceResult.getEntityList().size() > 0) {
                            invoice = (Invoice) invoiceResult.getEntityList().get(0);
                            if ((invoice.isIsOpeningBalenceInvoice() && invoice.getOpeningBalanceAmountDue() == 0) || (!invoice.isIsOpeningBalenceInvoice() && invoice.getInvoiceamountdue() == 0)) {
                                try {
                                    HashMap<String, Object> dataMap = new HashMap<String, Object>();
//                                    dataMap.put("amountduedate", receipt != null ? (receipt.isIsOpeningBalenceReceipt() ? receipt.getCreationDate() : receipt.getJournalEntry().getEntryDate()) : null);
                                    dataMap.put("amountduedate", receipt != null ? receipt.getCreationDate() : null);
                                    accInvoiceDAOObj.saveInvoiceAmountDueZeroDate(invoice, dataMap);
                                } catch (Exception ex) {
                                    System.out.println("" + ex.getMessage());
                                }
                            }
                        }
                    }
                    amountDueUpdatedInvoiceIDSet.add(invoice.getID());
                }
            }

            KwlReturnObject linkresultDn = accReceiptDAOobj.getLinkDetailReceiptToDebitNote(reqParams1);
            List<LinkDetailReceiptToDebitNote >linkedDetaisReceiptsToDn = linkresultDn.getEntityList();
            for (LinkDetailReceiptToDebitNote ldr : linkedDetaisReceiptsToDn) {
                DebitNote DN = ldr.getDebitnote();
                double amountReceivedConvertedInBaseCurrency = 0d;
                HashMap<String, Object> requestParams = new HashMap();
                requestParams.put(Constants.companyid, receipt.getCompany().getCompanyID());
                requestParams.put("gcurrencyid", receipt.getCompany().getCurrency().getCurrencyID());
                double externalCurrencyRate = 1d;
                boolean isopeningBalanceRCP = receipt.isIsOpeningBalenceReceipt();
                Date rcpCreationDate = null;
                rcpCreationDate = receipt.getCreationDate();
                if (isopeningBalanceRCP) {
                    externalCurrencyRate = receipt.getExchangeRateForOpeningTransaction();
                } else {
//                    rcpCreationDate = receipt.getJournalEntry().getEntryDate();
                    externalCurrencyRate = receipt.getJournalEntry().getExternalCurrencyRate();
                }
                String fromcurrencyid = receipt.getCurrency().getCurrencyID();
                KwlReturnObject bAmt = null;
                if (isopeningBalanceRCP && receipt.isConversionRateFromCurrencyToBase()) {// if Receipt is opening balance Receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, ldr.getAmount(), fromcurrencyid, rcpCreationDate, externalCurrencyRate);
                } else {
                    bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, ldr.getAmount(), fromcurrencyid, rcpCreationDate, externalCurrencyRate);
                }
                amountReceivedConvertedInBaseCurrency = (Double) bAmt.getEntityList().get(0);
                double paidncamount = ldr.getAmountInDNCurrency();
                    double amountDue = 0.0;
                    if (DN.isIsOpeningBalenceDN()) {
                        amountDue = DN.getOpeningBalanceAmountDue();
                    } else {
                        amountDue = DN.getDnamountdue();
                    }
                    if (amountDue < paidncamount) {
                        throw new AccountingException("Debit note linked with selected receipt is already paid,so can not be reverted");
                    } else {
                        KwlReturnObject cnjedresult = accReceiptDAOobj.updateDnAmount(DN.getID(), paidncamount);
                        KwlReturnObject cnopeningjedresult = accReceiptDAOobj.updateDnOpeningAmountDue(DN.getID(), paidncamount);
                        KwlReturnObject cnopeningbasejedresult = accReceiptDAOobj.updateDnOpeningBaseAmountDue(DN.getID(),amountReceivedConvertedInBaseCurrency);
                    }                
            }
            
            Set<DebitNotePaymentDetails> notePaymentDetailses = receipt.getDebitNotePaymentDetails();
            if (notePaymentDetailses != null && !notePaymentDetailses.isEmpty()) {
                for (DebitNotePaymentDetails dnpd : notePaymentDetailses) {
                    DebitNote note = dnpd.getDebitnote();
                    double paidncamount = dnpd.getAmountPaid();
                    double amountDue = 0.0;
                    if (note.isIsOpeningBalenceDN()) {
                        amountDue = note.getOpeningBalanceAmountDue();
                    } else {
                        amountDue = note.getDnamountdue();
                    }
                    if (amountDue < paidncamount) {
                        throw new AccountingException("Debit note linked with selected receipt is already paid,so can not be reverted");
                    } else {
                        KwlReturnObject cnjedresult = accReceiptDAOobj.updateDnAmount(note.getID(), paidncamount);
                        KwlReturnObject cnopeningjedresult = accReceiptDAOobj.updateDnOpeningAmountDue(note.getID(), paidncamount);
                        KwlReturnObject cnopeningbasejedresult = accReceiptDAOobj.updateDnOpeningBaseAmountDue(note.getID(), dnpd.getAmountInBaseCurrency());
                    }
                }
            }
            Set<ReceiptDetailLoan> loanDetails = receipt.getReceiptDetailsLoan();
            if(loanDetails!=null && !loanDetails.isEmpty()){
                for(ReceiptDetailLoan RDL : loanDetails){
                    RepaymentDetails RepaymentDetail = RDL.getRepaymentDetail();
                    RepaymentDetail.setAmountdue(RepaymentDetail.getAmountdue()-RDL.getAmountInRepaymentDetailCurrency());
                    RepaymentDetail.setPaymentStatus(PaymentStatus.Paid);
                }
            }
            
            /**
             * Recalculating the amount due of Advance Make payment used in
             * Receive payment if Advance payment is externally linked from report then
             * we store entries in LinkDetailReceiptToAdvancePayment because we
             * can link multiple advance payment to refund receive payment when we link externally from
             * report.  ERP-39559
             */
//            boolean isReceiptLinkedFromReport = false;
            Set<LinkDetailReceiptToAdvancePayment> linkDetailReceiptToAdvancePayment = receipt.getLinkDetailReceiptsToAdvancePayment();
            for (LinkDetailReceiptToAdvancePayment ldr : linkDetailReceiptToAdvancePayment) {
//                isReceiptLinkedFromReport = true;
                String paymentId = ldr.getPaymentId();
                if (!StringUtil.isNullOrEmpty(paymentId)) {
                    
                    JSONObject params = new JSONObject();
                    /**
                     * Below code gets the linking information of advance
                     * payment of payment which is linked to refund receipt
                     * whichs being reverted and checks if amount due is less
                     * then the amount paid in adv payment if yes then throws an
                     * exception else updates the amount due of advance payment.
                     */
                    params.put("amount", ldr.getAmountInPaymentCurrency());
                    params.put("paymentid", paymentId);
                    params.put(Constants.companyid, companyid);
                    KwlReturnObject advanceDetailReturnObj = accReceiptDAOobj.getAdvanceDetailInformationFromPaymentId(params);
                    if (advanceDetailReturnObj.getEntityList() != null && advanceDetailReturnObj.getEntityList().size() > 0) {
                        Iterator itr = advanceDetailReturnObj.getEntityList().iterator();
                        while (itr.hasNext()) {
                            Object[] result = (Object[]) itr.next();
                            double amountInAdvPaymentCurrency = ldr.getAmountInPaymentCurrency();
                            double amountDue = !StringUtil.isNullObject(result[1]) ? Double.parseDouble(result[1].toString()) : 0;
                            if (amountDue < amountInAdvPaymentCurrency) {
                                throw new AccountingException("Advance receive payment linked with selected payment is already paid,so can not be reverted.");
                            }
                        }
                        accReceiptDAOobj.updateAdvancePaymentAmountDueLinkedExternally(params);
                    }
                }
            }
            /**
             * If receipt is linked to payment at transaction time on line level
             * then we store the entry of linked advance payment in
             * ReceiptAdvanceDetail as only one advance payment can be linked to one
             * refund line item on line level but we can take multiple refund or deposit
             * on line level. ERP-39559
             */
//            if (!isReceiptLinkedFromReport) {
                Set<ReceiptAdvanceDetail> receiptAdvanceDetails = receipt.getReceiptAdvanceDetails();
                if (!StringUtil.isNullObject(receiptAdvanceDetails) && !receiptAdvanceDetails.isEmpty()) {
                    for (ReceiptAdvanceDetail rad : receiptAdvanceDetails) {
                        String advancePaymentId = rad.getAdvancedetailid();
                        if (!StringUtil.isNullOrEmpty(advancePaymentId)) {
                            JSONObject params = new JSONObject();
                            double amountDue = 0;
                            double amountOfReceipt = rad.getAmount();
                            double exchangeRate = rad.getExchangeratefortransaction();
                            double amountConvertedInPaymentAdvanceCurrency = authHandler.round((amountOfReceipt / exchangeRate), companyid);
                            List<Object[]> advPayDetails = accReceiptDAOobj.getAdvancePaymentDetails(advancePaymentId);
                            if (!StringUtil.isNullObject(advPayDetails) && !advPayDetails.isEmpty()) {
                                Object[] objArray = (Object[]) advPayDetails.get(0);
                                amountDue = !StringUtil.isNullObject(objArray[3]) ? Double.parseDouble(objArray[3].toString()) : 0;
                                if (amountDue < amountConvertedInPaymentAdvanceCurrency) {
                                    throw new AccountingException("Advance Make payment linked with selected payment is already paid,so can not be reverted.");
                                }
                                params.put("amount", amountConvertedInPaymentAdvanceCurrency);
                                params.put("paymentAdvanceDetailId", advancePaymentId);
                                accReceiptDAOobj.updateAdvancePaymentAmountDue(params);
                            }
                    }
                    /**
                     * Reverting the amount due of Refund Payment which is
                     * linked to Advance receipt in case receipt is marked as
                     * dishonoured.
                     */
                    String receiptAdvanceDetailsId = rad.getId();
                    JSONObject params = new JSONObject();
                    params.put("receiptadvancedetail", receiptAdvanceDetailsId);
                    params.put("isToRevertAmtDue", true);
                    params.put(Constants.companyid, companyid);
                    /**
                     * Below code gets the refund receipt details which is linked to Advance payment
                     * which is being reverted and checks if amount due is
                     * less then the amount paid in adv payment if yes then
                     * throws an exception else updates the amount due of
                     * refund receipt.
                     */
                    KwlReturnObject paymentDetailsReturnObj = accReceiptDAOobj.getRefundPaymentDetailsLinkedToAdvance(params);
                    if (paymentDetailsReturnObj.getEntityList() != null && paymentDetailsReturnObj.getEntityList().size() > 0) {
                        Iterator itr = paymentDetailsReturnObj.getEntityList().iterator();
                        while (itr.hasNext()) {
                            Object[] result = (Object[]) itr.next();
                            double amountConvertedInPaymentRefundCurrency = !StringUtil.isNullObject(result[0]) ? Double.parseDouble(result[0].toString()) : 0;
                            double amountDue = !StringUtil.isNullObject(result[1]) ? Double.parseDouble(result[1].toString()) : 0;
                            double exchangeRate = !StringUtil.isNullObject(result[2]) ? Double.parseDouble(result[2].toString()) : 1;
                            if (amountDue < amountConvertedInPaymentRefundCurrency) {
                                throw new AccountingException("Refund payment linked with selected receipt is already paid, so can not be reverted.");
                            }
                        }
                        accReceiptDAOobj.updateRefundPaymentLinkedWithAdvance(params);
                    }
                }
                /**
                 * Reverting the amount due of Refund Payment which is
                 * externally i.e from report linked to Advance receipt in case
                 * receipt is marked as dishonoured.
                 */
                JSONObject params = new JSONObject();
                params.put("receiptId", receiptid);
                params.put(Constants.companyid, companyid);
                KwlReturnObject paymentReceiptDetailsReturnObj = accReceiptDAOobj.getRefundPaymentLinkDetailsLinkedWithAdvance(params);
                if (paymentReceiptDetailsReturnObj.getEntityList() != null && paymentReceiptDetailsReturnObj.getEntityList().size() > 0) {
                    Iterator itr = paymentReceiptDetailsReturnObj.getEntityList().iterator();
                    while (itr.hasNext()) {
                        Object[] result = (Object[]) itr.next();
                        double amountDue = !StringUtil.isNullObject(result[0]) ? Double.parseDouble(result[0].toString()) : 0;
                        double amountInAdvReceiptCurrency = !StringUtil.isNullObject(result[1]) ? Double.parseDouble(result[1].toString()) : 0;
                        double exchangeRateForTransaction = !StringUtil.isNullObject(result[2]) ? Double.parseDouble(result[2].toString()) : 1;
                        double amountConvertedInPaymentRefundCurrency = (amountInAdvReceiptCurrency * exchangeRateForTransaction);
                        double finalAmountDue = authHandler.round((amountDue - amountConvertedInPaymentRefundCurrency), companyid);
                        if (amountDue < authHandler.round(amountConvertedInPaymentRefundCurrency, companyid)) {
                            throw new AccountingException("Refund payment linked with selected receipt is already paid, so can not be reverted.");
                        }
                        params.put("amountDue", finalAmountDue);
                        int cnt = accReceiptDAOobj.updateRefundPaymentExternallyLinkedWithAdvance(params);
                    }
                }
            }
//            }

            auditTrailObj.insertAuditLog(AuditAction.RECEIPT_ADDED, "User " + sessionHandlerImpl.getUserFullName(request) + " has reverted Cancelled/Dishonored Cheque for Receipt " + receipt.getReceiptNumber()+" along with Cancelled/Dishonored JE No. "+disHonouredChequeJENumber+" has deleted.", request, receipt.getID());

        } catch (AccountingException ex) {
            throw new AccountingException(ex.getMessage(), ex);

        } catch (Exception ex) {
            throw ServiceException.FAILURE("revertDishonouredReceipt : " + ex.getMessage(), ex);
        }
        ll.add(receipt.getID());
        ll.add(receipt.getReceiptNumber());
        ll.add(receipt.getReceipttype());
        ll.add(amountDueUpdatedInvoiceIDSet);
        return (ArrayList) ll;
    }
    
    public List saveDashonouredReceipt(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
        KwlReturnObject result = null;
        Receipt receipt = null;
        String oldjeid = null;
        String Cardid = null;
        String netinword = "";
        String accountid = "";
        double amount = 0;
        List ll = new ArrayList();
        Set<String> amountDueUpdatedInvoiceIDSet = new HashSet<>();
        try {
            String receiptid = request.getParameter("billid");
            String cheque_Dis_Date = !StringUtil.isNullOrEmpty(request.getParameter("entrydate")) ? request.getParameter("entrydate") : "";
            DateFormat df = authHandler.getDateOnlyFormat();
            String jeid = null;
            boolean jeautogenflag = false;
            String jeSeqFormatId = "";
            String jeIntegerPart = "";
            String jeDatePrefix = "";
            String jeDateAfterPrefix = "";
            String jeDateSuffix = "";
            HashMap<String, JSONArray> Map1 = new HashMap();
            String jeentryNumber = null;
            JSONObject jedjson = null;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            JournalEntry journalEntry = null;

            if (!StringUtil.isNullOrEmpty(receiptid)) {
                KwlReturnObject receiptObj = accountingHandlerDAOobj.getObject(Receipt.class.getName(), receiptid);
                receipt = (Receipt) receiptObj.getEntityList().get(0);
                oldjeid = receipt.getJournalEntry().getID();
                JournalEntry jetemp = receipt.getJournalEntry();
                synchronized (this) {
                    HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                    JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                    JEFormatParams.put("modulename", CompanyPreferencesConstants.AUTOJOURNALENTRY);
                    JEFormatParams.put("companyid", companyid);
                    JEFormatParams.put("isdefaultFormat", true);

                    KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                    SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, jetemp.getEntryDate());
                    jeentryNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    jeIntegerPart = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                    jeDatePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                    jeDateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                    jeSeqFormatId = format.getID();
                    jeautogenflag = true;
                }
                  
                Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
                jeDataMap.put("entrynumber", jeentryNumber);
                jeDataMap.put("autogenerated", jeautogenflag);
                jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                jeDataMap.put(Constants.SEQNUMBER, jeIntegerPart);
                jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
                jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
                jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
                if (!cheque_Dis_Date.equals("")) {
                    jeDataMap.put("entrydate", df.parse(cheque_Dis_Date));
                } else {
                    jeDataMap.put("entrydate", jetemp.getEntryDate());
                }
                jeDataMap.put("companyid", companyid);
                String name = " ";
                if (receipt.getCustomer() != null) {
                    name += ", "+receipt.getCustomer().getName();
                } else if (receipt.getVendor() != null && !receipt.getVendor().equals("")) {
                    Map<String, Object> paramMap = new HashMap<>();
                    paramMap.put("id", receipt.getVendor());
                    Object exPrefObject = kwlCommonTablesDAOObj.getRequestedObjectFields(Vendor.class, new String[]{"name"}, paramMap);
//                    KwlReturnObject vendorResult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), receipt.getVendor());
//                    Vendor vendor = (Vendor) vendorResult.getEntityList().get(0);
                    String vendorName=(String)exPrefObject;
                    name += ", "+vendorName;
                }
                jeDataMap.put("memo", "Cancelled/Dishonored Cheque For Receipt " + receipt.getReceiptNumber() + name);            //ERM-744
                jeDataMap.put("currencyid", jetemp.getCurrency().getCurrencyID());
                //adding external currency rate for document create in foreing currency with exchange rate
                jeDataMap.put(Constants.EXTERNALCURRENCYRATE, !StringUtil.isNullObject(jetemp.getExternalCurrencyRate()) ? jetemp.getExternalCurrencyRate() : "");
                HashSet jedetails = new HashSet();
                KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
                journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
                journalEntry.setIsDishonouredCheque(true);
                jetemp.setIsDishonouredCheque(true);
                journalEntry.setTransactionModuleid(Constants.Acc_Dishonoured_Receive_Payment_ModuleId);
                journalEntry.setTransactionId(receiptid);
                jeid = journalEntry.getID();
                jeDataMap.put("jeid", jeid);

                KwlReturnObject jeDetailsResult = accJournalEntryobj.getJournalEntryDetail(oldjeid, companyid);
                List list = jeDetailsResult.getEntityList();
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    JournalEntryDetail jed = (JournalEntryDetail) itr.next();
                    jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", authHandler.formattedAmount(jed.getAmount(), companyid));
                    jedjson.put("accountid", jed.getAccount().getID());
                    jedjson.put("debit", !jed.isDebit());
                    jedjson.put("jeid", jeid);
                    jedjson.put("description", jed.getDescription());
                    jedjson.put(Constants.ISSEPARATED, jed.isIsSeparated());
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    JournalEntryDetail tempjed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(tempjed);
                }
            }

            HashMap<String, Object> reqParams1 = new HashMap();
            reqParams1.put("receiptid", receipt.getID());
            reqParams1.put("companyid", companyid);
            KwlReturnObject linkresult = accReceiptDAOobj.getLinkDetailReceipt(reqParams1);
            List<LinkDetailReceipt> linkedDetaisReceipts = linkresult.getEntityList();
            for (LinkDetailReceipt ldr : linkedDetaisReceipts) {
                Invoice invoice = ldr.getInvoice();
                double amountReceivedConvertedInBaseCurrency = 0d;
                HashMap<String, Object> requestParams = new HashMap();
                requestParams.put(Constants.companyid, receipt.getCompany().getCompanyID());
                requestParams.put("gcurrencyid", receipt.getCompany().getCurrency().getCurrencyID());
                double externalCurrencyRate = 1d;
                boolean isopeningBalanceRCP = receipt.isIsOpeningBalenceReceipt();
                Date rcpCreationDate = null;
                rcpCreationDate = receipt.getCreationDate();
                if (isopeningBalanceRCP) {
                    externalCurrencyRate = receipt.getExchangeRateForOpeningTransaction();
                } else {
//                    rcpCreationDate = receipt.getJournalEntry().getEntryDate();
                    externalCurrencyRate = receipt.getJournalEntry().getExternalCurrencyRate();
                }
                String fromcurrencyid = receipt.getCurrency().getCurrencyID();
                KwlReturnObject bAmt = null;
                if (isopeningBalanceRCP && receipt.isConversionRateFromCurrencyToBase()) {// if Receipt is opening balance Receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, ldr.getAmount(), fromcurrencyid, rcpCreationDate, externalCurrencyRate);
                } else {
                    bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, ldr.getAmount(), fromcurrencyid, rcpCreationDate, externalCurrencyRate);
                }
                amountReceivedConvertedInBaseCurrency = (Double) bAmt.getEntityList().get(0);
                KwlReturnObject invoiceResult=updateInvoiceAmountDueAndReturnResult(invoice, receipt, receipt.getCompany(), -ldr.getAmountInInvoiceCurrency(), -amountReceivedConvertedInBaseCurrency);
                if (invoiceResult != null && invoiceResult.getEntityList() != null && invoiceResult.getEntityList().size() > 0) {
                    invoice = (Invoice) invoiceResult.getEntityList().get(0);
                    if ((invoice.isIsOpeningBalenceInvoice() && invoice.getOpeningBalanceAmountDue() != 0) || (!invoice.isIsOpeningBalenceInvoice() && invoice.getInvoiceamountdue() != 0)) {
                        invoice.setAmountDueDate(null);  //If amount due becomes non zero setting amountduedate to null                      
                    }
                }
                amountDueUpdatedInvoiceIDSet.add(invoice.getID());
            }
            Set<ReceiptDetail> receiptDetails = receipt.getRows();
            if (receiptDetails != null && !receiptDetails.isEmpty()) {
                for (ReceiptDetail receiptDetail : receiptDetails) {
                    Invoice invoice = receiptDetail.getInvoice();
                    double amountReceivedConvertedInBaseCurrency = 0d;
                    HashMap<String, Object> requestParams = new HashMap();
                    requestParams.put(Constants.companyid, receipt.getCompany().getCompanyID());
                    requestParams.put("gcurrencyid", receipt.getCompany().getCurrency().getCurrencyID());
                    double externalCurrencyRate = 1d;
                    boolean isopeningBalanceRCP = receipt.isIsOpeningBalenceReceipt();
                    Date rcpCreationDate = null;
                    rcpCreationDate = receipt.getCreationDate();
                    if (isopeningBalanceRCP) {
                        externalCurrencyRate = receipt.getExchangeRateForOpeningTransaction();
                    } else {
//                        rcpCreationDate = receipt.getJournalEntry().getEntryDate();
                        externalCurrencyRate = receipt.getJournalEntry().getExternalCurrencyRate();
                    }
                    String fromcurrencyid = receipt.getCurrency().getCurrencyID();
                    KwlReturnObject bAmt = null;
                    KwlReturnObject bDiscountAmt = null;
                    double discountAmtInInvoiceCurrency = authHandler.round(receiptDetail.getDiscountAmount() / receiptDetail.getExchangeRateForTransaction(), receipt.getCompany().getCompanyID());
                    double discountAmount = receiptDetail.getDiscountAmount();
                    if (isopeningBalanceRCP && receipt.isConversionRateFromCurrencyToBase()) {// if Receipt is opening balance Receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                        bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, receiptDetail.getAmount(), fromcurrencyid, rcpCreationDate, externalCurrencyRate);
                        bDiscountAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, discountAmount, fromcurrencyid, rcpCreationDate, externalCurrencyRate);
                    } else {
                        bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, receiptDetail.getAmount(), fromcurrencyid, rcpCreationDate, externalCurrencyRate);
                        bDiscountAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, discountAmount, fromcurrencyid, rcpCreationDate, externalCurrencyRate);
                    }
                    amountReceivedConvertedInBaseCurrency = (Double) bAmt.getEntityList().get(0);
                    discountAmount=(Double) bDiscountAmt.getEntityList().get(0);
                    KwlReturnObject invoiceResult=updateInvoiceAmountDueAndReturnResult(invoice, receipt, receipt.getCompany(), -(receiptDetail.getAmountInInvoiceCurrency()+discountAmtInInvoiceCurrency), -(amountReceivedConvertedInBaseCurrency+discountAmount));
                    if (invoiceResult != null && invoiceResult.getEntityList() != null && invoiceResult.getEntityList().size() > 0) {
                        invoice = (Invoice) invoiceResult.getEntityList().get(0);
                        if ((invoice.isIsOpeningBalenceInvoice() && invoice.getOpeningBalanceAmountDue() != 0) || (!invoice.isIsOpeningBalenceInvoice() && invoice.getInvoiceamountdue() != 0)) {
                            invoice.setAmountDueDate(null);//If amount due becomes non zero setting amountduedate to null                      
                        }
                    }
                    amountDueUpdatedInvoiceIDSet.add(invoice.getID());
                }
            }

            KwlReturnObject linkresultDn = accReceiptDAOobj.getLinkDetailReceiptToDebitNote(reqParams1);
            List<LinkDetailReceiptToDebitNote >linkedDetaisReceiptsToDn = linkresultDn.getEntityList();
            for (LinkDetailReceiptToDebitNote ldr : linkedDetaisReceiptsToDn) {
                DebitNote DN = ldr.getDebitnote();
                double amountReceivedConvertedInBaseCurrency = 0d;
                HashMap<String, Object> requestParams = new HashMap();
                requestParams.put(Constants.companyid, receipt.getCompany().getCompanyID());
                requestParams.put("gcurrencyid", receipt.getCompany().getCurrency().getCurrencyID());
                double externalCurrencyRate = 1d;
                boolean isopeningBalanceRCP = receipt.isIsOpeningBalenceReceipt();
                Date rcpCreationDate = null;
                rcpCreationDate = receipt.getCreationDate();
                if (isopeningBalanceRCP) {
                    externalCurrencyRate = receipt.getExchangeRateForOpeningTransaction();
                } else {
//                    rcpCreationDate = receipt.getJournalEntry().getEntryDate();
                    externalCurrencyRate = receipt.getJournalEntry().getExternalCurrencyRate();
                }
                String fromcurrencyid = receipt.getCurrency().getCurrencyID();
                KwlReturnObject bAmt = null;
                if (isopeningBalanceRCP && receipt.isConversionRateFromCurrencyToBase()) {// if Receipt is opening balance Receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, ldr.getAmount(), fromcurrencyid, rcpCreationDate, externalCurrencyRate);
                } else {
                    bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, ldr.getAmount(), fromcurrencyid, rcpCreationDate, externalCurrencyRate);
                }
                amountReceivedConvertedInBaseCurrency = (Double) bAmt.getEntityList().get(0);
                double paidncamount = ldr.getAmountInDNCurrency();
                KwlReturnObject cnjedresult = accReceiptDAOobj.updateDnAmount(DN.getID(), -paidncamount);
                KwlReturnObject cnopeningjedresult = accReceiptDAOobj.updateDnOpeningAmountDue(DN.getID(), -paidncamount);
                KwlReturnObject cnopeningbasejedresult = accReceiptDAOobj.updateDnOpeningBaseAmountDue(DN.getID(),-amountReceivedConvertedInBaseCurrency);
            }
            
            receipt.setIsDishonouredCheque(true);
            receipt.setDisHonouredChequeJe(journalEntry);
            
            Set<DebitNotePaymentDetails> notePaymentDetailses = receipt.getDebitNotePaymentDetails();
            if (notePaymentDetailses != null && !notePaymentDetailses.isEmpty()) {
                for (DebitNotePaymentDetails dnpd : notePaymentDetailses) {
                    DebitNote note = dnpd.getDebitnote();
                    double paidncamount = dnpd.getAmountPaid();
                    KwlReturnObject cnjedresult = accReceiptDAOobj.updateDnAmount(note.getID(), -paidncamount);
                    KwlReturnObject cnopeningjedresult = accReceiptDAOobj.updateDnOpeningAmountDue(note.getID(), -paidncamount);
                    KwlReturnObject cnopeningbasejedresult = accReceiptDAOobj.updateDnOpeningBaseAmountDue(note.getID(), -dnpd.getAmountInBaseCurrency());
                }
            }
            Set<ReceiptDetailLoan> loanDetails = receipt.getReceiptDetailsLoan();
            if(loanDetails!=null && !loanDetails.isEmpty()){
                for(ReceiptDetailLoan RDL : loanDetails){
                    RepaymentDetails RepaymentDetail = RDL.getRepaymentDetail();
                    RepaymentDetail.setAmountdue(RepaymentDetail.getAmountdue()+RDL.getAmountInRepaymentDetailCurrency());
                    RepaymentDetail.setPaymentStatus(PaymentStatus.Unpaid);
                }
            }
            /**
             * Restoring the amount due of Advance Make payment used in
             * Receive payment if Advance payment is externally linked from report then
             * we store entries in LinkDetailReceiptToAdvancePayment because we
             * can link multiple advance payment to refund receive payment when we link externally from
             * report.  ERP-39559
             */
//            boolean isReceiptLinkedFromReport = false;
            Set<LinkDetailReceiptToAdvancePayment> linkDetailReceiptToAdvancePayment = receipt.getLinkDetailReceiptsToAdvancePayment();
            for (LinkDetailReceiptToAdvancePayment ldr : linkDetailReceiptToAdvancePayment) {
//                isReceiptLinkedFromReport = true;
                String paymentId = ldr.getPaymentId();
                if (!StringUtil.isNullOrEmpty(paymentId)) {
                    JSONObject params = new JSONObject();
                    params.put("amount", ldr.getAmountInPaymentCurrency());
                    params.put("paymentid", paymentId);
                    params.put(Constants.companyid, companyid);
                    params.put("isToAddAmount", true);      //Sending isToAddAmount true because we have to add amount in amountdue column at the time of marking the receipt Dishonoured
                    accReceiptDAOobj.updateAdvancePaymentAmountDueLinkedExternally(params);
                    }
                }
            /**
             * If receipt is linked to payment at transaction time on line level
             * then we store the entry of linked advance payment in
             * ReceiptAdvanceDetail as only one advance payment can be linked to
             * one refund line item on line level but we can take multiple
             * refund or deposit on line level. ERP-39559
             */
//            if (!isReceiptLinkedFromReport) {
                Set<ReceiptAdvanceDetail> receiptAdvanceDetails = receipt.getReceiptAdvanceDetails();
                if (!StringUtil.isNullObject(receiptAdvanceDetails) && !receiptAdvanceDetails.isEmpty()) {
                    for (ReceiptAdvanceDetail rad : receiptAdvanceDetails) {
                        String advancePaymentId = rad.getAdvancedetailid();
                        if (!StringUtil.isNullOrEmpty(advancePaymentId)) {
                            JSONObject params = new JSONObject();
                            double exchangeRate = rad.getExchangeratefortransaction();
                            double amountOfReceipt = rad.getAmount();
                            double amountConvertedInPaymentAdvanceCurrency = authHandler.round((amountOfReceipt / exchangeRate), companyid);
                            params.put("amount", amountConvertedInPaymentAdvanceCurrency);
                            params.put("paymentAdvanceDetailId", advancePaymentId);
                            params.put("isToAddAmount", true);      //Sending isToAddAmount true because we have to add amount in amountdue column at the time of marking the receipt Dishonoured
                            accReceiptDAOobj.updateAdvancePaymentAmountDue(params);
                        }
                        /**
                         * Restoring the amount due of Refund Payment which is
                         * linked to Advance receipt in case receipt is marked
                         * as dishonoured.
                         */
                        String receiptAdvanceDetailsId = rad.getId();
                        JSONObject params = new JSONObject();
                        params.put("receiptadvancedetail", receiptAdvanceDetailsId);
                        params.put(Constants.companyid, companyid);
                        int updateCnt = accReceiptDAOobj.updateRefundPaymentLinkedWithAdvance(params);
                    }
                    /**
                     * Restoring the amount due of Refund Payment which is
                     * externally i.e from report linked to Advance receipt in
                     * case receipt is marked as dishonoured.
                     */
                    JSONObject params = new JSONObject();
                    params.put("receiptId", receiptid);
                    params.put(Constants.companyid, companyid);
                    KwlReturnObject paymentReceiptDetailsReturnObj = accReceiptDAOobj.getRefundPaymentLinkDetailsLinkedWithAdvance(params);
                    if (paymentReceiptDetailsReturnObj.getEntityList() != null && paymentReceiptDetailsReturnObj.getEntityList().size() > 0) {
                        Iterator itr = paymentReceiptDetailsReturnObj.getEntityList().iterator();
                        while (itr.hasNext()) {
                            Object[] linkDetailsResult = (Object[]) itr.next();
                            double amountDue = !StringUtil.isNullObject(linkDetailsResult[0]) ? Double.parseDouble(linkDetailsResult[0].toString()) : 0;
                            double amountConvertedInPaymentRefundCurrency = !StringUtil.isNullObject(linkDetailsResult[1]) ? Double.parseDouble(linkDetailsResult[1].toString()) : 0;
                            double exchangeRateForTransaction = !StringUtil.isNullObject(linkDetailsResult[2]) ? Double.parseDouble(linkDetailsResult[2].toString()) : 1;
                            double finalAmountDue = authHandler.round((amountDue + (amountConvertedInPaymentRefundCurrency * exchangeRateForTransaction)), companyid);
                            params.put("amountDue", finalAmountDue);
                            int cnt = accReceiptDAOobj.updateRefundPaymentExternallyLinkedWithAdvance(params);
                        }
                    }
                }
//            }
                
            String roundingJENo = "";
            String roundingJEIds = "";
            //Deleting Rounding JE of thoses Invoices whose amount due updated due to dishouned cheque
            if (!amountDueUpdatedInvoiceIDSet.isEmpty()) {
                String invIDs = "";
                for (String invID : amountDueUpdatedInvoiceIDSet) {
                    invIDs = invID + ",";
                }
                if (!StringUtil.isNullOrEmpty(invIDs)) {
                    invIDs = invIDs.substring(0, invIDs.length() - 1);
                }
                KwlReturnObject jeResult = accJournalEntryobj.getRoundingJournalEntryByGRIds(invIDs, companyid);
                List<JournalEntry> jeList = jeResult.getEntityList();
                for (JournalEntry roundingJE : jeList) {
                    roundingJENo = roundingJE.getEntryNumber() + ",";
                    roundingJEIds = roundingJE.getID() + ",";
                    deleteJEArray(roundingJE.getID(), companyid);
                }
                if (!StringUtil.isNullOrEmpty(roundingJENo)) {
                    roundingJENo = roundingJENo.substring(0, roundingJENo.length() - 1);
                }
                if (!StringUtil.isNullOrEmpty(roundingJEIds)) {
                    roundingJEIds = roundingJEIds.substring(0, roundingJEIds.length() - 1);
                }
            }

            auditTrailObj.insertAuditLog(AuditAction.RECEIPT_ADDED, "User " + sessionHandlerImpl.getUserFullName(request) + " has updated a receipt " + receipt.getReceiptNumber() + " for Cancelled/Dishonored Cheque with Cancelled/Dishonored JE No. " + receipt.getDisHonouredChequeJe().getEntryNumber(), request, receipt.getID());
            
            if (!StringUtil.isNullOrEmpty(roundingJENo)) {
                auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " has updated a receipt " + receipt.getReceiptNumber() + " for Cancelled/Dishonored Cheque." + messageSource.getMessage("acc.roundingje.roundingje", null, RequestContextUtils.getLocale(request)) + " " + roundingJENo + " " + messageSource.getMessage("acc.roundingje.roundingjedelted", null, RequestContextUtils.getLocale(request)) + ".", request, roundingJEIds);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveReceipt : " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveReceipt : " + ex.getMessage(), ex);
        }
        ll.add(new String[]{oldjeid, Cardid});
        ll.add(receipt.getID());
        ll.add(receipt.getReceiptNumber());
        ll.add(receipt.getDisHonouredChequeJe().getEntryNumber());
        ll.add(receipt.getReceipttype());
        ll.add(amountDueUpdatedInvoiceIDSet);
        return (ArrayList) ll;
    }
        
    public void saveReevalJournalEntryObjects(HttpServletRequest request, JSONArray detailsJSONArray, Receipt receipt, int type,String oldRevaluationJE,Map<String,Object> counterMap) throws SessionExpiredException, ServiceException, AccountingException {
        try {
            if (detailsJSONArray.length() > 0) {
                double finalAmountReval = 0;
                String basecurrency = sessionHandlerImpl.getCurrencyID(request);
                KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), receipt.getCompany().getCompanyID());
                CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
                JSONObject requestObj = convertRequestToJSONObjectForLinkPayment(request);
                finalAmountReval = getReevalJournalEntryAmount(request, detailsJSONArray, receipt, type);
                if (finalAmountReval != 0) {
                    /**
                     * added transactionID and transactionModuleID to Realised
                     * JE.
                     */
                    counterMap.put("transactionModuleid", receipt.isIsOpeningBalenceReceipt() ? Constants.Acc_opening_Receipt : Constants.Acc_Receive_Payment_ModuleId);
                    counterMap.put("transactionId", receipt.getID());
                    String revaljeid = PostJEFORReevaluation(requestObj, finalAmountReval, receipt.getCompany().getCompanyID(), preferences, basecurrency, oldRevaluationJE, counterMap);
                    receipt.setRevalJeId(revaljeid);
                }
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
    }
    public double getReevalJournalEntryAmount(HttpServletRequest request, JSONArray detailsJSONArray, Receipt receipt, int type) throws SessionExpiredException, ServiceException, AccountingException {
        double amount = 0;
        Set jedetails = new HashSet();
         double finalAmountReval = 0;
        try {
            if (detailsJSONArray.length() > 0) {
                String basecurrency = sessionHandlerImpl.getCurrencyID(request);
                KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), receipt.getCompany().getCompanyID());
                CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);

                KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), receipt.getCompany().getCompanyID());
                ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);


                for (int i = 0; i < detailsJSONArray.length(); i++) {
                    JSONObject jobj = detailsJSONArray.getJSONObject(i);
                    double ratio = 0;
                    double amountReval = 0;
                    String revalId = null;
                    Date tranDate = null;
                    double exchangeRate = 0.0;
                    double exchangeRateReval = 0.0;
                    //boolean isRealised=false;
                    double amountdue = jobj.getDouble("enteramount");
                    double exchangeratefortransaction = jobj.optDouble("exchangeratefortransaction", 1.00);
                    HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
                    GlobalParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
                    GlobalParams.put("gcurrencyid", basecurrency);
                    GlobalParams.put("dateformat", authHandler.getDateOnlyFormat(request));
                    Date creationDate = authHandler.getDateOnlyFormat(request).parse(request.getParameter("creationdate"));
                    KwlReturnObject result = accountingHandlerDAOobj.getObject(Invoice.class.getName(), jobj.getString("documentid"));
                    Invoice invoice = (Invoice) result.getEntityList().get(0);
                    boolean isopeningBalanceInvoice = invoice.isIsOpeningBalenceInvoice();
                    tranDate = invoice.getCreationDate();
                    if (!invoice.isNormalInvoice()) {
                        exchangeRate = invoice.getExchangeRateForOpeningTransaction();
                        exchangeRateReval = exchangeRate;
                    } else {
                        exchangeRate = invoice.getJournalEntry().getExternalCurrencyRate();
                        exchangeRateReval = exchangeRate;
//                        tranDate = invoice.getJournalEntry().getEntryDate();
                    }

                    HashMap<String, Object> invoiceId = new HashMap<String, Object>();
                    invoiceId.put("invoiceid", invoice.getID());
                    invoiceId.put("companyid", sessionHandlerImpl.getCompanyid(request));
                    invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
                    //Checking the document entery in revalution history if any for current rate
                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    RevaluationHistory revalueationHistory = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (revalueationHistory != null) {
                        exchangeRateReval = revalueationHistory.getEvalrate();
                    }

                    result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrency);
                    KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
                    String currid = currency.getCurrencyID();
                    if (invoice.getCurrency() != null) {
                        currid = invoice.getCurrency().getCurrencyID();
                    }
                    //            double oldrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, gr.getJournalEntry().getEntryDate());
                    KwlReturnObject bAmt = null;
                    if (isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                        bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, tranDate, exchangeRate);
                    } else {
                        bAmt = accCurrencyobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, tranDate, exchangeRate);
                    }

                    double oldrate = (Double) bAmt.getEntityList().get(0);
                    //            double newrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
                    if (revalueationHistory == null && isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                        bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
                    } else {
                        bAmt = accCurrencyobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
                    }

                    double newrate = (Double) bAmt.getEntityList().get(0);
                    ratio = oldrate - newrate;
                    if (Math.abs(exchangeratefortransaction - newrate) <= 0.000001) {
                        exchangeratefortransaction = newrate;
                    }
                    double amountdueNew = amountdue / exchangeratefortransaction;
                    amountdueNew = Math.round(amountdueNew * 1000) / 1000d;
                    amountReval = ratio * amountdueNew;
                    finalAmountReval = finalAmountReval + amountReval;
                }
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return finalAmountReval;
    }
    
    public double ReevalJournalEntryForOpeningReceipt(JSONObject requestObj, Receipt receipt, double linkReceiptAmount, double exchangeratefortransaction) throws SessionExpiredException, ServiceException, AccountingException {
        double finalAmountReval = 0;
        try {
            String basecurrency = requestObj.optString("baseCurrency");
            double ratio = 0;
            double amountReval = 0;
            String revalId = null;
            Date tranDate = null;
            double exchangeRate = 0.0;
            double exchangeRateReval = 0.0;
            double amountdue = linkReceiptAmount;
            String companyid = requestObj.optString(Constants.companyid);
            Map<String, Object> GlobalParams = new HashMap<>();
            GlobalParams.put("companyid", companyid);
            GlobalParams.put("gcurrencyid", basecurrency);
            GlobalParams.put("dateformat", (DateFormat)requestObj.opt("dateonlyformat"));
            Date creationDate = receipt.getCreationDate();
            boolean isopeningBalanceInvoice = receipt.isIsOpeningBalenceReceipt();
            tranDate = receipt.getCreationDate();
            if (!receipt.isNormalReceipt()) {
                exchangeRate = receipt.getExchangeRateForOpeningTransaction();
                exchangeRateReval = exchangeRate;
            } else {
                exchangeRate = receipt.getJournalEntry().getExternalCurrencyRate();
                exchangeRateReval = exchangeRate;
//                tranDate = receipt.getJournalEntry().getEntryDate();
            }
            HashMap<String, Object> invoiceId = new HashMap<String, Object>();
            invoiceId.put("invoiceid", receipt.getID());
            invoiceId.put("companyid", companyid);
            invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
            //Checking the document entery in revalution history if any for current rate
            KwlReturnObject result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
            RevaluationHistory revalueationHistory = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
            if (revalueationHistory != null) {
                exchangeRateReval = revalueationHistory.getEvalrate();
            }

            result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrency);
            KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
            String currid = currency.getCurrencyID();
            if (receipt.getCurrency() != null) {
                currid = receipt.getCurrency().getCurrencyID();
            }
            //            double oldrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, gr.getJournalEntry().getEntryDate());
            KwlReturnObject bAmt = null;
            if (isopeningBalanceInvoice && receipt.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, tranDate, exchangeRate);
            } else {
                bAmt = accCurrencyobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, tranDate, exchangeRate);
            }

            double oldrate = (Double) bAmt.getEntityList().get(0);
            //            double newrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
            if (revalueationHistory == null && isopeningBalanceInvoice && receipt.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
            } else {
                bAmt = accCurrencyobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
            }

            double newrate = (Double) bAmt.getEntityList().get(0);
            ratio = oldrate - newrate;
            if (Math.abs(exchangeratefortransaction - newrate) <= 0.000001) {
                exchangeratefortransaction = newrate;
            }
            double amountdueNew = amountdue / exchangeratefortransaction;
            amountdueNew = Math.round(amountdueNew * 1000) / 1000d;
            amountReval = ratio * amountdueNew;
            finalAmountReval = finalAmountReval + amountReval;
        } catch (ServiceException e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return finalAmountReval;
    }
    public double ReevalJournalEntryForAdvancePayment(JSONObject requestObj, String advancedetailID, double linkReceiptAmount, double exchangeratefortransaction) throws SessionExpiredException, ServiceException, AccountingException, JSONException {
        double finalAmountReval = 0;
        try {
            String basecurrency = requestObj.optString("baseCurrency");
            double ratio = 0;
            double amountReval = 0;
            Date tranDate = null;
            double exchangeRate = 0.0;
            double exchangeRateReval = 0.0;
            double amountdue = linkReceiptAmount;
            String companyid = requestObj.optString(Constants.companyid);
            Map<String, Object> GlobalParams = new HashMap<>();
            GlobalParams.put("companyid", companyid);
            GlobalParams.put("gcurrencyid", basecurrency);
            GlobalParams.put("dateformat", (DateFormat)requestObj.opt("dateonlyformat"));
            
            List<Object[]> advPayDetails=accReceiptDAOobj.getAdvancePaymentDetails(advancedetailID);
            if (!advPayDetails.isEmpty()) {
                Object[] objArray = (Object[]) advPayDetails.get(0);
                String paymentID = objArray[0].toString();
                /**
                 * Putting payment id and is payment a opening payment in
                 * requestObj as this method only returns double value and we
                 * need payment id and isOpeningPayment flag in caller
                 * method(advanceDetailObject and
                 * saveLinkedReceiptToAdvancePaymentDetails) of this
                 * method.ERP-41455.
                 */
                requestObj.put("paymentId", paymentID);
                requestObj.put("isOpeningPayment", objArray[4] != null ? objArray[4].toString().equals("1") : false);
                String currencyID = objArray[1].toString();
                String JournalEnteryID = objArray[2].toString();
                KwlReturnObject resultObj = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), JournalEnteryID);
                JournalEntry journalEntry = (JournalEntry) resultObj.getEntityList().get(0);
                
                if(!StringUtil.isNullOrEmpty(paymentID) && !StringUtil.isNullOrEmpty(currencyID) && journalEntry !=null){
                    exchangeRate = journalEntry.getExternalCurrencyRate();
                    exchangeRateReval = exchangeRate;
                    tranDate = journalEntry.getEntryDate();
                    Map<String, Object> invoiceId = new HashMap<>();
                    invoiceId.put("invoiceid", paymentID);
                    invoiceId.put("companyid", companyid);
                    invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
                    //Checking the document entery in revalution history if any for current rate
                    KwlReturnObject result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    RevaluationHistory revalueationHistory = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (revalueationHistory != null) {
                        exchangeRateReval = revalueationHistory.getEvalrate();
                    }

                    result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrency);
                    KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
                    String currid = currency.getCurrencyID();
                    if (!StringUtil.isNullOrEmpty(currencyID)) {
                        currid = currencyID;
                    }
                    //            double oldrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, gr.getJournalEntry().getEntryDate());
                    KwlReturnObject bAmt = accCurrencyobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, tranDate, exchangeRate);
                    double oldrate = (Double) bAmt.getEntityList().get(0);
                    bAmt = accCurrencyobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, tranDate, exchangeRateReval);
                    double newrate = (Double) bAmt.getEntityList().get(0);
                    ratio = oldrate - newrate;
                    if (Math.abs(exchangeratefortransaction - newrate) <= 0.000001) {
                        exchangeratefortransaction = newrate;
                    }
                    double amountdueNew = amountdue / exchangeratefortransaction;
                    amountdueNew = Math.round(amountdueNew * 1000) / 1000d;
                    amountReval = ratio * amountdueNew;
                    finalAmountReval = finalAmountReval + amountReval;
                }
                
            }
            
        } catch (ServiceException e) {
            throw ServiceException.FAILURE("saveReceipt : " + e.getMessage(), e);
        }
        return finalAmountReval;
    }
    public double ReevalJournalEntryForOpeningInvoice(JSONObject requestObj, Invoice invoice, double linkInvoiceAmount, double exchangeratefortransaction) throws SessionExpiredException, ServiceException, AccountingException {
        double finalAmountReval = 0;
        try {
            String basecurrency = requestObj.optString("baseCurrency");
            double ratio = 0;
            double amountReval = 0;
            String revalId = null;
            Date tranDate = null;
            double exchangeRate = 0.0;
            double exchangeRateReval = 0.0;
            double amountdue = linkInvoiceAmount;
            String companyid=requestObj.optString(Constants.companyid);
            Map<String, Object> GlobalParams = new HashMap<>();
            GlobalParams.put("companyid", companyid);
            GlobalParams.put("gcurrencyid", basecurrency);
            GlobalParams.put("dateformat", (DateFormat)requestObj.opt("dateonlyformat"));
            Date creationDate = invoice.getCreationDate();
            boolean isopeningBalanceInvoice = invoice.isIsOpeningBalenceInvoice();
            tranDate = invoice.getCreationDate();
            if (!invoice.isNormalInvoice()) {
                exchangeRate = invoice.getExchangeRateForOpeningTransaction();
                exchangeRateReval = exchangeRate;
            } else {
                exchangeRate = invoice.getJournalEntry().getExternalCurrencyRate();
                exchangeRateReval = exchangeRate;
//                tranDate = invoice.getJournalEntry().getEntryDate();
            }
            Map<String, Object> invoiceId = new HashMap<>();
            invoiceId.put("invoiceid", invoice.getID());
            invoiceId.put("companyid", companyid);
            invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
            //Checking the document entery in revalution history if any for current rate
            KwlReturnObject result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
            RevaluationHistory revalueationHistory = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
            if (revalueationHistory != null) {
                exchangeRateReval = revalueationHistory.getEvalrate();
            }

            result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrency);
            KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
            String currid = currency.getCurrencyID();
            if (invoice.getCurrency() != null) {
                currid = invoice.getCurrency().getCurrencyID();
            }
            //            double oldrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, gr.getJournalEntry().getEntryDate());
            KwlReturnObject bAmt = null;
            if (isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, tranDate, exchangeRate);
            } else {
                bAmt = accCurrencyobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, tranDate, exchangeRate);
            }

            double oldrate = (Double) bAmt.getEntityList().get(0);
            //            double newrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
            if (revalueationHistory == null && isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
            } else {
                bAmt = accCurrencyobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
            }

            double newrate = (Double) bAmt.getEntityList().get(0);
            ratio = oldrate - newrate;
            if (Math.abs(exchangeratefortransaction - newrate) <= 0.000001) {
                exchangeratefortransaction = newrate;
            }
            double amountdueNew = amountdue / exchangeratefortransaction;
            amountdueNew = Math.round(amountdueNew * 1000) / 1000d;
            amountReval = ratio * amountdueNew;
            finalAmountReval = finalAmountReval + amountReval;
        } catch (ServiceException e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return finalAmountReval;
    }
    //Need to check for external currecy rate

    public double ReevalJournalEntryForOpeningDebiteNote(JSONObject requestObj, DebitNote debitNote, double linkInvoiceAmount, double exchangeratefortransaction) throws SessionExpiredException, ServiceException, AccountingException {
        double amount = 0;
        double finalAmountReval = 0;
        try {
            String basecurrency = requestObj.optString("baseCurrency");
            double ratio = 0;
            double amountReval = 0;
            String revalId = null;
            Date tranDate = null;
            double exchangeRate = 0.0;
            double exchangeRateReval = 0.0;
            double amountdue = linkInvoiceAmount;
            String companyid = requestObj.optString(Constants.companyid);;
            Map<String, Object> GlobalParams = new HashMap<>();
            GlobalParams.put("companyid", companyid);
            GlobalParams.put("gcurrencyid", basecurrency);
            GlobalParams.put("dateformat", (DateFormat)requestObj.opt("dateonlyformat"));
            Date creationDate = debitNote.getCreationDate();
            boolean isopeningBalanceInvoice = debitNote.isIsOpeningBalenceDN();
            tranDate = debitNote.getCreationDate();
            if (!debitNote.isNormalDN()) {
                exchangeRate = debitNote.getExchangeRateForOpeningTransaction();
                exchangeRateReval = exchangeRate;
            } else {
                exchangeRate = debitNote.getJournalEntry().getExternalCurrencyRate();
                exchangeRateReval = exchangeRate;
//                tranDate = debitNote.getJournalEntry().getEntryDate();
            }
            HashMap<String, Object> invoiceId = new HashMap<>();
            invoiceId.put("invoiceid", debitNote.getID());
            invoiceId.put("companyid", companyid);
            invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
            //Checking the document entery in revalution history if any for current rate
            KwlReturnObject result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
            RevaluationHistory revalueationHistory = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
            if (revalueationHistory != null) {
                exchangeRateReval = revalueationHistory.getEvalrate();
            }

            result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrency);
            KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
            String currid = currency.getCurrencyID();
            if (debitNote.getCurrency() != null) {
                currid = debitNote.getCurrency().getCurrencyID();
            }
            //            double oldrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, gr.getJournalEntry().getEntryDate());
            KwlReturnObject bAmt = null;
            if (isopeningBalanceInvoice && debitNote.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, tranDate, exchangeRate);
            } else {
                bAmt = accCurrencyobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, tranDate, exchangeRate);
            }

            double oldrate = (Double) bAmt.getEntityList().get(0);
            //            double newrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
            if (revalueationHistory == null && isopeningBalanceInvoice && debitNote.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
            } else {
                bAmt = accCurrencyobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
            }

            double newrate = (Double) bAmt.getEntityList().get(0);
            ratio = oldrate - newrate;
            if (Math.abs(exchangeratefortransaction - newrate) <= 0.000001) {
                exchangeratefortransaction = newrate;
            }
            double amountdueNew = amountdue / exchangeratefortransaction;
            amountdueNew = Math.round(amountdueNew * 1000) / 1000d;
            amountReval = ratio * amountdueNew;
            finalAmountReval = finalAmountReval + amountReval;
        } catch (ServiceException e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return finalAmountReval;
    }
     public double ReevalJournalEntryCNDN(HttpServletRequest request, DebitNote debitNote,CompanyAccountPreferences preferences, double exchangeratefortransaction,double amountdue, Map<String,Object> counterMap) throws SessionExpiredException, ServiceException, AccountingException {
       double finalAmountReval = 0;
        try {
             String basecurrency = sessionHandlerImpl.getCurrencyID(request);
             double ratio = 0;
             double amountReval = 0;
             Date tranDate = null;
             double exchangeRate = 0.0;
             double exchangeRateReval = 0.0;
             Map<String, Object> GlobalParams = new HashMap<>();
             GlobalParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
             GlobalParams.put("gcurrencyid", basecurrency);
             GlobalParams.put("dateformat", authHandler.getDateFormatter(request));
             boolean isopeningBalanceInvoice = debitNote.isIsOpeningBalenceDN();
             tranDate = debitNote.getCreationDate();
             if (!debitNote.isNormalDN()) {
                 exchangeRate = debitNote.getExchangeRateForOpeningTransaction();
                 exchangeRateReval = exchangeRate;
             } else {
                 exchangeRate = debitNote.getJournalEntry().getExternalCurrencyRate();
                 exchangeRateReval = exchangeRate;
//                 tranDate = debitNote.getJournalEntry().getEntryDate();
             }

             Map<String, Object> invoiceId = new HashMap<>();
             invoiceId.put("invoiceid", debitNote.getID());
             invoiceId.put("companyid", sessionHandlerImpl.getCompanyid(request));
             invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
             //Checking the document entery in revalution history if any for current rate
             KwlReturnObject result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
             RevaluationHistory revalueationHistory = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
             if (revalueationHistory != null) {
                 exchangeRateReval = revalueationHistory.getEvalrate();
             }
             result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrency);
             KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
             String currid = currency.getCurrencyID();
             if (debitNote.getCurrency() != null) {
                 currid = debitNote.getCurrency().getCurrencyID();
             }
             //            double oldrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, gr.getJournalEntry().getEntryDate());
             KwlReturnObject bAmt = null;
             if (isopeningBalanceInvoice && debitNote.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                 bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, tranDate, exchangeRate);
             } else {
                 bAmt = accCurrencyobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, tranDate, exchangeRate);
             }
             double oldrate = (Double) bAmt.getEntityList().get(0);
             //            double newrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
             if (revalueationHistory == null && isopeningBalanceInvoice && debitNote.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                 bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, tranDate, exchangeRateReval);
             } else {
                 bAmt = accCurrencyobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, tranDate, exchangeRateReval);
             }
             double newrate = (Double) bAmt.getEntityList().get(0);
             ratio = oldrate - newrate;
             if (Math.abs(exchangeratefortransaction - newrate) <= 0.000001) {
                 exchangeratefortransaction = newrate;
             }

             double amountdueNew = amountdue / exchangeratefortransaction;
             amountdueNew = Math.round(amountdueNew * 1000) / 1000d;
             amountReval = ratio * amountdueNew;
             finalAmountReval = amountReval;
             if (finalAmountReval != 0) {
                 String oldRevaluationJE = debitNote.getRevalJeId();
                 JSONObject requestObj = convertRequestToJSONObjectForLinkPayment(request);
                 /**
                  * added transactionID and transactionModuleID to Realised JE.
                  */
                 counterMap.put("transactionModuleid", debitNote.isIsOpeningBalenceDN() ? (debitNote.isdNForVendor() ? Constants.Acc_opening_Vendor_DebitNote : Constants.Acc_opening_Customer_DebitNote) : Constants.Acc_Debit_Note_ModuleId);
                 counterMap.put("transactionId", debitNote.getID());
                 String revaljeid = PostJEFORReevaluation(requestObj, -(finalAmountReval), preferences.getCompany().getCompanyID(), preferences, basecurrency, oldRevaluationJE, counterMap);
                 debitNote.setRevalJeId(revaljeid);
                 finalAmountReval = 0;
             }

         } catch (SessionExpiredException | ServiceException | JSONException e) {
             throw ServiceException.FAILURE("saveReceipt : " + e.getMessage(), e);
         }
         return finalAmountReval;
    }
    public ModelAndView getSinglePaymentDataToLoad(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = getReceiptRequestMap(request);
            boolean isCopyTransaction = Boolean.parseBoolean(request.getParameter("isCopyTransaction"));
            requestParams.put("isCopyTransaction", isCopyTransaction);
            String billid = request.getParameter("billid") != null ? request.getParameter("billid") : "";
            requestParams.put("billid", billid);
            DateFormat df = authHandler.getOnlyDateFormat(request);
            requestParams.put("df", df);
            KwlReturnObject result = null;
            result = accReceiptDAOobj.getReceipts(requestParams);
            Object[] objects = (Object[]) result.getEntityList().get(0);
            Receipt receipt = (Receipt) objects[0];
            Account acc = (Account) objects[1];
            JSONObject receiptObj = getReceiptInfo(receipt, acc, requestParams);
            jobj.put("data", receiptObj);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public JSONObject getReceiptInfo(Receipt receipt, Account acc, HashMap<String, Object> requestParams) throws SessionExpiredException, ServiceException {
        //JSONObject jobj=new JSONObject();
        //JSONArray jArr=new JSONArray();        
        JSONObject obj = new JSONObject();
        try {
            String companyid = (String) requestParams.get("companyid");
            DateFormat df = (DateFormat) requestParams.get("df");
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("id", companyid);
            Object exPrefObject = kwlCommonTablesDAOObj.getRequestedObjectFields(ExtraCompanyPreferences.class, new String[]{"columnPref"}, paramMap);
            JSONObject jObj = StringUtil.isNullObject(exPrefObject) ? new JSONObject() : new JSONObject(exPrefObject.toString());
            boolean isPostingDateCheck = false;
            if (!StringUtil.isNullObject(jObj) && jObj.has(Constants.IS_POSTING_DATE_CHECK) && jObj.get(Constants.IS_POSTING_DATE_CHECK) != null && jObj.optBoolean(Constants.IS_POSTING_DATE_CHECK, false)) {
                isPostingDateCheck = true;
            }
            obj.put("withoutinventory", false);
            Vendor vendor = null;
            String address = "";
            Customer customer = null;//(Customer) cresult.getEntityList().get(0);
            if (receipt.getCustomer() != null) {
                customer = receipt.getCustomer();
                obj.put("personid", receipt.getCustomer().getID());
                obj.put("personcode",receipt.getCustomer().getAcccode());
                address = customer.getBillingAddress();
            } else if (receipt.getVendor() != null) {
                KwlReturnObject custResult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), receipt.getVendor());
                vendor = (Vendor) custResult.getEntityList().get(0);
                obj.put("personid", vendor.getID());
                obj.put("personcode", vendor.getAcccode());
//                    address = CommonFun;
                //Do it afterword as mayur is still moving the function
            } else {
                obj.put("personid", acc.getID());
            }
            obj.put("address", address);
            obj.put("billid", receipt.getID());
            if (isPostingDateCheck) {
                obj.put("billdate", df.format(receipt.getCreationDate()));
                obj.put("creationdate", df.format(receipt.getCreationDate()));
            } else {
                obj.put("billdate", df.format(receipt.getJournalEntry().getEntryDate()));
                obj.put("creationdate", df.format(receipt.getJournalEntry().getEntryDate()));
            }
            obj.put("companyid", receipt.getCompany().getCompanyID());
            obj.put("companyname", receipt.getCompany().getCompanyName());
            obj.put("entryno", receipt.getJournalEntry().getEntryNumber());
            obj.put("journalentryid", receipt.getJournalEntry().getID());
            obj.put("isadvancepayment", receipt.isIsadvancepayment());
            obj.put("ismanydbcr", receipt.isIsmanydbcr());
            obj.put("isprinted", receipt.isPrinted());
            obj.put("bankCharges", receipt.getBankChargesAmount());
            obj.put("bankChargesCmb", receipt.getBankChargesAccount() != null ? receipt.getBankChargesAccount().getID() : "");
            obj.put("bankChargesCmbValue", receipt.getBankChargesAccount() != null ? ((!StringUtil.isNullOrEmpty(receipt.getBankChargesAccount().getName()))?receipt.getBankChargesAccount().getName():receipt.getBankChargesAccount().getAcccode()) : "");
            obj.put("bankInterest", receipt.getBankInterestAmount());
            obj.put("bankInterestCmb", receipt.getBankInterestAccount() != null ? receipt.getBankInterestAccount().getID() : "");
            obj.put("bankInterestCmbValue", receipt.getBankInterestAccount() != null ? ((!StringUtil.isNullOrEmpty(receipt.getBankInterestAccount().getName()))?receipt.getBankInterestAccount().getName():receipt.getBankInterestAccount().getAcccode()) : "");
            obj.put("paidToCmb", receipt.getReceivedFrom() != null ? receipt.getReceivedFrom().getID() : ""); 
            obj.put("paidToCmbValue", receipt.getReceivedFrom() != null ? receipt.getReceivedFrom().getValue(): "");
            obj.put(Constants.SEQUENCEFORMATID, receipt.getSeqformat() == null ? "" : receipt.getSeqformat().getID());
            obj.put("sequenceformatvalue", receipt.getSeqformat() == null ? "" : receipt.getSeqformat().getName());
//            obj.put("personid", (customer != null) ? customer.getID() : acc.getID());
            KwlReturnObject result = accReceiptDAOobj.getReceiptCustomerNames(receipt.getCompany().getCompanyID(), receipt.getID());
            List cNameList = result.getEntityList();
            Iterator cNamesItr = cNameList.iterator();
            StringBuilder customerNames = new StringBuilder("");
//            String customerNames = "";
//            while (cNamesItr.hasNext()) {
//                String tempName = URLEncoder.encode((String) cNamesItr.next(), "UTF-8");
//                customerNames += tempName;
//                customerNames += ",";
//            }
//            customerNames = customerNames.substring(0, Math.max(0, customerNames.length() - 1));
            obj.put("memo", receipt.getMemo());
            obj.put("deleted", receipt.isDeleted());
            obj.put("currencysymbol", receipt.getCurrency().getSymbol());
            if(receipt.getExternalCurrencyRate()==0) {
//                KwlReturnObject bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, 1.0d, receipt.getCurrency().getCurrencyID(), receipt.getJournalEntry().getEntryDate(), receipt.getJournalEntry().getExternalCurrencyRate());
                KwlReturnObject bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, 1.0d, receipt.getCurrency().getCurrencyID(), receipt.getCreationDate(), receipt.getJournalEntry().getExternalCurrencyRate());
                obj.put("externalcurrencyrate", 1/(Double) bAmt.getEntityList().get(0));
            } else {
                obj.put("externalcurrencyrate", receipt.getExternalCurrencyRate());
            }
            obj.put("chequenumber", receipt.getPayDetail() == null ? "" : (receipt.getPayDetail().getPaymentMethod().getDetailType() == PaymentMethod.TYPE_BANK) ? (receipt.getPayDetail().getCheque() != null ? receipt.getPayDetail().getCheque().getChequeNo() : "") : "");
            obj.put("chequedescription", receipt.getPayDetail() == null ? "" : (receipt.getPayDetail().getPaymentMethod().getDetailType() == PaymentMethod.TYPE_BANK) ? (receipt.getPayDetail().getCheque() != null ? (receipt.getPayDetail().getCheque().getDescription() != null ? receipt.getPayDetail().getCheque().getDescription() : "") : "") : "");
            obj.put("currencyid", receipt.getCurrency().getCurrencyID());
            obj.put("paymentmethodname", (receipt.getPayDetail() == null ? "" : receipt.getPayDetail().getPaymentMethod().getMethodName()));
            obj.put("detailtype", (receipt.getPayDetail() == null ? "" : receipt.getPayDetail().getPaymentMethod().getDetailType()));
            obj.put("pmtmethod", (receipt.getPayDetail() == null ? "" : receipt.getPayDetail().getPaymentMethod().getID()));
            obj.put("paymentmethodacc", (receipt.getPayDetail() == null ? "" : receipt.getPayDetail().getPaymentMethod().getAccount().getID()));
            obj.put("nonRefundable", receipt.isNonRefundable());
            obj.put("paymentmethodaccname", (receipt.getPayDetail() == null ? "" : receipt.getPayDetail().getPaymentMethod().getAccount().getName()));
            obj.put("amount", authHandler.round( (receipt.getDepositAmount()), companyid) );         // Seperate JE for Bank charges and interest so dont club amount.
            obj.put("paymentwindowtype", receipt.getPaymentWindowType() != 0 ? receipt.getPaymentWindowType() : 1);
            obj.put("no", receipt.getReceiptNumber());
            obj.put("paymentCurrencyToPaymentMethodCurrencyExchangeRate",receipt.getPaymentcurrencytopaymentmethodcurrencyrate());
            KwlReturnObject bAmt = null;
            String baseCurrency=(String)requestParams.get("gcurrencyid");
            double totalAmount=receipt.getDepositAmount();
            if (baseCurrency!=null&&!baseCurrency.equalsIgnoreCase(receipt.getCurrency().getCurrencyID())) {
                    bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, receipt.getDepositAmount(), receipt.getCurrency().getCurrencyID(), receipt.getCreationDate(), receipt.getJournalEntry().getExternalCurrencyRate());
                    totalAmount=(!bAmt.getEntityList().isEmpty())?(Double)bAmt.getEntityList().get(0):0d;
            }
            /**
             * Put GST document history.
             */
            if (receipt.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                obj.put("refdocid", receipt.getID());
                fieldDataManagercntrl.getGSTDocumentHistory(obj);

            }
            obj.put("paymentamountinbasecurrency",totalAmount);
            JSONArray jSONArray = new JSONArray();
            JSONArray jSONArrayPayDetails = new JSONArray();
            getPayDetailsInfo(receipt, acc, requestParams,jSONArrayPayDetails);
            getReceiptDetailsInfo(receipt, acc, requestParams,jSONArray);
            getReceiptAdvanceDetailsInfo(receipt, acc, requestParams,jSONArray);
            getReceiptCNDNDetailsInfo(receipt, acc, requestParams,jSONArray);
            getReceiptDetailsOtherwiseInfo(receipt, acc, requestParams,jSONArray,customerNames);
            requestParams.put("receipt", receipt);
            requestParams.put("jArr", jSONArray);
            getReceiptDetailsLoanInfo(requestParams);
            obj.put("personname", StringUtil.DecodeText((vendor == null && customer == null) ? customerNames.toString() : ((vendor != null) ? vendor.getName() : (customer != null) ? customer.getName() : "")));    //Used decoder to avoid '+' symbol at white/empty space between words. 
            JSONArray sortedArray = new JSONArray();
            sortedArray = paymentService.sortJson(jSONArray);
            if (sortedArray.length() == jSONArray.length()) {
                jSONArray = sortedArray;
            }
        if(jSONArray.length()!=0){
            obj.put("Details", new JSONObject().put("data", jSONArray));
        }
        if(jSONArrayPayDetails.length()!=0){
            obj.put("paydetail", new JSONObject().put("data", jSONArrayPayDetails.get(0)));
        }
        if(vendor != null){
            obj.put("residentialstatus", vendor.getResidentialstatus());
            if(!StringUtil.isNullOrEmpty(vendor.getDeducteeType())){
                obj.put("deducteetype", vendor.getDeducteeType());
            }
        }
        if(customer != null){
            obj.put("residentialstatus", customer.getResidentialstatus());
            if(!StringUtil.isNullOrEmpty(customer.getDeducteeType())){
                obj.put("deducteetype", customer.getDeducteeType());
            }
        }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getReceiptJson : " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getReceiptJson : " + ex.getMessage(), ex);
        }
        return obj;
    }


    public ModelAndView reloadGridOnCurrencyChange(HttpServletRequest request, HttpServletResponse response) throws JSONException, ServiceException, UnsupportedEncodingException {
        JSONObject obj = new JSONObject();
        Customer customer = null;
        boolean issuccess = false;
        String msg = "";
        try {  
            HashMap<String, Object> requestParams = getReceiptRequestMap(request);
            String billid = request.getParameter("billid") != null ? request.getParameter("billid") : "";
            requestParams.put("billid", billid);
            DateFormat df = authHandler.getOnlyDateFormat(request);
            requestParams.put("df", df);
            requestParams.put("newcurrency", request.getParameter("newcurrency"));
            requestParams.put("changedDate", request.getParameter("date"));
            KwlReturnObject result = null;
            result = accReceiptDAOobj.getReceipts(requestParams);
            Object[] objects = (Object[]) result.getEntityList().get(0);
            Receipt receipt = (Receipt) objects[0];
            Account acc = (Account) objects[1];
            obj=reloadGridOnCurrencyChange(receipt, acc, requestParams);
            issuccess = true;
        } catch (ServiceException ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                obj.put("success", issuccess);
                obj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", obj.toString());
    }
    public JSONObject reloadGridOnCurrencyChange(Receipt receipt, Account acc, HashMap<String, Object> requestParams) throws JSONException, ServiceException, UnsupportedEncodingException, ParseException {
        DateFormat df = (DateFormat) requestParams.get("df");
        JSONObject obj = new JSONObject();
        Vendor vendor = null;
        boolean issuccess = false;
        String companyid = (String) requestParams.get("companyid");
        String msg = "";
            Customer customer = receipt.getCustomer();
            if (receipt.getVendor() != null) {
                KwlReturnObject custResult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), receipt.getVendor());
                vendor = (Vendor) custResult.getEntityList().get(0);
                obj.put("personid", vendor.getID());
            } else if (receipt.getCustomer() != null) {
                obj.put("personid", customer.getID());
            } else {
                obj.put("personid", acc.getID());
            }
            KwlReturnObject result = accReceiptDAOobj.getReceiptCustomerNames(receipt.getCompany().getCompanyID(), receipt.getID());
            List vNameList = result.getEntityList();
            Iterator vNamesItr = vNameList.iterator();
            StringBuilder vendorNames = new StringBuilder("");
            JSONArray jSONArray = new JSONArray();
            JSONArray jSONArrayPayDetails = new JSONArray();
            //used to know whether to change the amount accoring to date or not
            String baseCurrencyID=receipt.getCompany().getCurrency().getCurrencyID();
            String fromCurrencyID=receipt.getCurrency().getCurrencyID();
            String tocurrencyid = (requestParams.get("newcurrency") == null ? baseCurrencyID : requestParams.get("newcurrency")+"");
            Date paymentCreationDate = null;
            Date actualPaymentCreationDate = null;
            double externalCurrencyRate = 0d;
            if (receipt.isIsOpeningBalenceReceipt() && !receipt.isNormalReceipt()) {
                    if(requestParams.containsKey("changedDate")&&requestParams.get("changedDate")!=null&&!requestParams.get("changedDate").toString().equals("")){
                         Date changedDate = new Date(requestParams.get("changedDate").toString());
                         paymentCreationDate = df.parse(df.format(changedDate));
                    }else{
                         paymentCreationDate = receipt.getCreationDate();
                    }
                    actualPaymentCreationDate = df.parse(df.format(receipt.getCreationDate()));
                    externalCurrencyRate = receipt.getExchangeRateForOpeningTransaction();
                } else {
                    if(requestParams.containsKey("changedDate")&&requestParams.get("changedDate")!=null&&!requestParams.get("changedDate").toString().equals("")){
                         Date changedDate = new Date(requestParams.get("changedDate").toString());
                         paymentCreationDate = df.parse(df.format(changedDate));
                    }else{
//                         paymentCreationDate = receipt.getJournalEntry().getEntryDate();
                         paymentCreationDate = receipt.getCreationDate();
                    }
//                    actualPaymentCreationDate = df.parse(df.format(receipt.getJournalEntry().getEntryDate()));
                    actualPaymentCreationDate = df.parse(df.format(receipt.getCreationDate()));
                    externalCurrencyRate = receipt.getJournalEntry().getExternalCurrencyRate();
                }
            KwlReturnObject bAmt = null;
            bAmt = accCurrencyobj.getOneCurrencyToOther(requestParams, 1.0d, fromCurrencyID, tocurrencyid, paymentCreationDate, externalCurrencyRate);
            double ramount = (Double) bAmt.getEntityList().get(0);
            bAmt = accCurrencyobj.getOneCurrencyToOther(requestParams, 1.0d, fromCurrencyID, tocurrencyid, actualPaymentCreationDate, externalCurrencyRate);
            double ramountactual = (Double) bAmt.getEntityList().get(0);
            //if currency and date is same call edit case's function
        if (fromCurrencyID.equals(tocurrencyid) &&ramountactual==ramount) {
            getReceiptDetailsInfo(receipt, acc, requestParams, jSONArray);
            getReceiptAdvanceDetailsInfo(receipt, acc, requestParams, jSONArray);
            getReceiptCNDNDetailsInfo(receipt, acc, requestParams, jSONArray);
            getReceiptDetailsOtherwiseInfo(receipt, acc, requestParams, jSONArray, vendorNames);
        } else {
            getReceiptDetailsInfoOnCurrencyChange(receipt, acc, requestParams, jSONArray);
            getReceiptAdvanceDetailsInfoOnCurrencyChange(receipt, acc, requestParams, jSONArray);
            getReceiptCNDNDetailsInfoOnCurrencyChange(receipt, acc, requestParams, jSONArray);
            getReceiptDetailsOtherwiseInfoOnCurrencyChange(receipt, acc, requestParams, jSONArray, vendorNames);
        }
            bAmt = accCurrencyobj.getIfBaseToCurrencyRatePresence(requestParams, 1.0d, tocurrencyid, paymentCreationDate, externalCurrencyRate);
            double baseRate = (Double) bAmt.getEntityList().get(0);
            boolean exchangeRateFound=true;
            if(baseRate==0){
                exchangeRateFound=false;
            }
            obj.put("exchangeRateFound", exchangeRateFound);  
            obj.put("personname", StringUtil.DecodeText((vendor == null && customer == null) ? vendorNames.toString() : ((vendor != null) ? vendor.getName() : (customer != null) ? customer.getName() : "")));    //Used decoder to avoid '+' symbol at white/empty space between words. 
            if (jSONArray.length() != 0) {
                JSONObject jSONObject=new JSONObject();
                jSONObject.put("data", jSONArray);
                obj.put("Details", jSONObject);
                obj.put("paymentwindowtype", receipt.getPaymentWindowType());
                if(requestParams.containsKey("totalenteredamount")){
                    obj.put("amount", requestParams.get("totalenteredamount"));
                }else{
                    obj.put("amount", authHandler.round(receipt.getDepositAmount()-receipt.getBankChargesAmount()-receipt.getBankInterestAmount(), companyid));
                }
            }
            issuccess = true;
        return obj;
    }
    
    
    public void getPayDetailsInfo(Receipt receipt, Account acc, HashMap<String, Object> requestParams,JSONArray jSONArray) throws JSONException, ServiceException, UnsupportedEncodingException {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat df1 = (DateFormat) requestParams.get("df");
        boolean isCopyTransaction = false;
        if (requestParams.containsKey("isCopyTransaction") && requestParams.get("isCopyTransaction") != null) {
            isCopyTransaction = (Boolean) requestParams.get("isCopyTransaction");
        }
        PayDetail payDetail=receipt.getPayDetail();
        JSONObject obj = new JSONObject();
        obj.put("chequenumber", receipt.getPayDetail() == null ? "" : (receipt.getPayDetail().getPaymentMethod().getDetailType() == PaymentMethod.TYPE_BANK) ? (receipt.getPayDetail().getCheque() != null ? receipt.getPayDetail().getCheque().getChequeNo() : "") : "");
        obj.put("description", receipt.getPayDetail() == null ? "" : (receipt.getPayDetail().getPaymentMethod().getDetailType() == PaymentMethod.TYPE_BANK) ? (receipt.getPayDetail().getCheque() != null ? (receipt.getPayDetail().getCheque().getDescription() != null ? receipt.getPayDetail().getCheque().getDescription() : "") : "") : "");
        obj.put("currencyid", (receipt.getCurrency().getCurrencyID()));
        obj.put("methodid", (receipt.getPayDetail() == null ? "" : receipt.getPayDetail().getPaymentMethod().getID()));
        obj.put("detailtype", (receipt.getPayDetail() == null ? "" : receipt.getPayDetail().getPaymentMethod().getDetailType()));
        if(receipt.getPayDetail()!=null&&receipt.getPayDetail().getCheque()!=null && receipt.getPayDetail().getCheque().getDueDate()!=null) {
            obj.put("postdate", df1.format(receipt.getPayDetail().getCheque().getDueDate()));
        }
        obj.put("paymentthrough", receipt.getPayDetail() == null ? "" : (receipt.getPayDetail().getPaymentMethod().getDetailType() == PaymentMethod.TYPE_BANK) ? (receipt.getPayDetail().getCheque() != null ? (receipt.getPayDetail().getCheque().getBankName() != null ? receipt.getPayDetail().getCheque().getBankName() : "") : "") : "");
        obj.put("paymentthroughid", receipt.getPayDetail() == null ? "" : (receipt.getPayDetail().getPaymentMethod().getDetailType() == PaymentMethod.TYPE_BANK) ? (receipt.getPayDetail().getCheque() != null ? (receipt.getPayDetail().getCheque().getBankMasterItem() != null ? receipt.getPayDetail().getCheque().getBankMasterItem().getID() : "") : "") : "");
        if (receipt.getPayDetail() != null) {
            try {
                obj.put("expirydate", (receipt.getPayDetail().getCard() == null ? "" : df.format(receipt.getPayDetail().getCard().getExpiryDate())));
            } catch (IllegalArgumentException ae) {
                obj.put("expirydate", "");
            }
            
            obj.put("refdetail", (receipt.getPayDetail().getCard() == null ? (receipt.getPayDetail().getCheque() == null ? "" : receipt.getPayDetail().getCheque().getDescription()) : receipt.getPayDetail().getCard().getCardType()));
            obj.put("refno", (receipt.getPayDetail().getCard() == null ? (receipt.getPayDetail().getCheque() == null ? "" : receipt.getPayDetail().getCheque().getChequeNo()) : receipt.getPayDetail().getCard().getRefNo()));
            obj.put("chequedescription", ((receipt.getPayDetail() == null || receipt.getPayDetail().getCheque() == null) ? "" : (receipt.getPayDetail().getCheque().getDescription() != null ? receipt.getPayDetail().getCheque().getDescription() : "")));
            obj.put("refname", (receipt.getPayDetail().getCard() == null ? (receipt.getPayDetail().getCheque() == null ? "" : (receipt.getPayDetail().getCheque().getBankMasterItem() == null ? receipt.getPayDetail().getCheque().getBankName() : receipt.getPayDetail().getCheque().getBankMasterItem().getID())) : receipt.getPayDetail().getCard().getCardHolder()));
            if (receipt.getPayDetail().getCard() != null) {
                obj.put("refcardno", receipt.getPayDetail().getCard().getCardNo());
//                        obj.put("refexpdate", payment.getPayDetail().getCard().getExpiryDate());
            }
        }
        obj.put("clearancedate", "");
        obj.put("paymentstatus", "Uncleared");
        if (receipt.getPayDetail() != null) {
            KwlReturnObject clearanceDate = accBankReconciliationObj.getBRfromJE(receipt.getJournalEntry().getID(), receipt.getCompany().getCompanyID(), false);
            if (clearanceDate != null && clearanceDate.getEntityList() != null && clearanceDate.getEntityList().size() > 0) {
                BankReconciliationDetail brd = (BankReconciliationDetail) clearanceDate.getEntityList().get(0);
                if (brd.getBankReconciliation().getClearanceDate() != null && !isCopyTransaction) {     //SDP-10874
                    obj.put("clearancedate", df1.format(brd.getBankReconciliation().getClearanceDate()));
                    obj.put("paymentstatus", "Cleared");
                }
            }
        }
        jSONArray.put(obj);
    }
    public void getReceiptDetailsInfo(Receipt receipt, Account acc, HashMap<String, Object> requestParams,JSONArray jSONArray) throws JSONException, ServiceException, UnsupportedEncodingException {
        DateFormat df = (DateFormat) requestParams.get("df");
        boolean isCopyTransaction = requestParams.get("isCopyTransaction")!=null ? (Boolean)requestParams.get("isCopyTransaction") : false;
        boolean ispendingAproval = (requestParams.containsKey("ispendingAproval") && requestParams.get("ispendingAproval") != null) ? Boolean.parseBoolean((String)requestParams.get("ispendingAproval")) : false;
        boolean isView = (requestParams.containsKey("isView") && requestParams.get("isView") != null) ? Boolean.parseBoolean((String)requestParams.get("isView")) : false;
        boolean isDishonouredCheque = receipt.isIsDishonouredCheque();
        Set<ReceiptDetail> paymentDetails=receipt.getRows();
        HashMap<String, Object> fieldrequestParams = new HashMap();
        HashMap<String, String> customFieldMap = new HashMap<String, String>();
        HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
        fieldrequestParams.put(Constants.filter_values, Arrays.asList(receipt.getCompany().getCompanyID(), Constants.Acc_Receive_Payment_ModuleId));
        HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
        HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap,customFieldMap,customDateFieldMap);
        String companyid = (String) requestParams.get("companyid");
        for (ReceiptDetail paymentDetail : paymentDetails) {
            JSONObject obj = new JSONObject();
            Invoice invoice=paymentDetail.getInvoice();
            boolean isInvoiceIsClaimed=(invoice.getBadDebtType() == Constants.Invoice_Claimed || invoice.getBadDebtType() == Constants.Invoice_Recovered);
            obj.put("type",Constants.PaymentAgainstInvoice);
            obj.put("debit",false);
            obj.put("modified", true);
            obj.put("exchangeratefortransaction", paymentDetail.getExchangeRateForTransaction());
            if(invoice.isIsOpeningBalenceInvoice()){
                obj.put("transactionAmount",invoice.getOriginalOpeningBalanceAmount());
            }else {
                obj.put("transactionAmount",invoice.getCustomerEntry().getAmount());
            }
            if (isInvoiceIsClaimed) {
                obj.put("enteramount", isCopyTransaction ? authHandler.round(invoice.getClaimAmountDue() * paymentDetail.getExchangeRateForTransaction(), companyid) : paymentDetail.getAmount());
                obj.put("amountdue", isCopyTransaction ? authHandler.round(invoice.getClaimAmountDue() * paymentDetail.getExchangeRateForTransaction(), companyid) : authHandler.round(invoice.getClaimAmountDue() * paymentDetail.getExchangeRateForTransaction(), companyid) + paymentDetail.getAmount());
                obj.put("amountDueOriginal", isCopyTransaction ? invoice.getClaimAmountDue() : invoice.getClaimAmountDue() + paymentDetail.getAmountInInvoiceCurrency());
                obj.put("amountDueOriginalSaved", isCopyTransaction ? invoice.getClaimAmountDue() : invoice.getClaimAmountDue() + paymentDetail.getAmountInInvoiceCurrency());
            } else {
                if (invoice.isIsOpeningBalenceInvoice()) {
                    obj.put("enteramount", isCopyTransaction ? authHandler.round(invoice.getOpeningBalanceAmountDue() * paymentDetail.getExchangeRateForTransaction(), companyid) : paymentDetail.getAmount());
                    obj.put("discountname", ((isCopyTransaction || ispendingAproval) && !isView) ? 0 : paymentDetail.getDiscountAmount());
                } else {
                    obj.put("enteramount", isCopyTransaction ? authHandler.round(invoice.getInvoiceamountdue() * paymentDetail.getExchangeRateForTransaction(), companyid) : paymentDetail.getAmount());
                    obj.put("discountname", ((isCopyTransaction || ispendingAproval) && !isView) ? 0 : paymentDetail.getDiscountAmount());
                }
                
                double discountAmtInInvoiceCurrency = paymentDetail.getDiscountAmountInInvoiceCurrency();
                double discountAmount = paymentDetail.getDiscountAmount();
                if (invoice.isIsOpeningBalenceInvoice()) {
                    if (receipt.getApprovestatuslevel() == Constants.APPROVED_STATUS_LEVEL) {
                        obj.put("amountdue", (isCopyTransaction || isDishonouredCheque) ? authHandler.round(invoice.getOpeningBalanceAmountDue() * paymentDetail.getExchangeRateForTransaction(), companyid) : authHandler.round(invoice.getOpeningBalanceAmountDue() * paymentDetail.getExchangeRateForTransaction(), companyid) + (paymentDetail.getAmount()+discountAmount));
                        obj.put("amountDueOriginal", (isCopyTransaction || isDishonouredCheque) ? invoice.getOpeningBalanceAmountDue() : invoice.getOpeningBalanceAmountDue() + (paymentDetail.getAmountInInvoiceCurrency()+discountAmtInInvoiceCurrency));
                        obj.put("amountDueOriginalSaved", (isCopyTransaction || isDishonouredCheque) ? invoice.getOpeningBalanceAmountDue() : invoice.getOpeningBalanceAmountDue() + (paymentDetail.getAmountInInvoiceCurrency()+discountAmtInInvoiceCurrency));
                        obj.put("amountdueafterdiscount", (isCopyTransaction || isDishonouredCheque) ? authHandler.round(invoice.getOpeningBalanceAmountDue() * paymentDetail.getExchangeRateForTransaction(), companyid) : authHandler.round(((invoice.getOpeningBalanceAmountDue() * paymentDetail.getExchangeRateForTransaction())+paymentDetail.getAmount()), companyid));
                    } else {
                        obj.put("amountdue", authHandler.round(invoice.getOpeningBalanceAmountDue() * paymentDetail.getExchangeRateForTransaction(), companyid));
                        obj.put("amountDueOriginal", invoice.getOpeningBalanceAmountDue());
                        obj.put("amountDueOriginalSaved", invoice.getOpeningBalanceAmountDue());
                        obj.put("amountdueafterdiscount", authHandler.round(((invoice.getOpeningBalanceAmountDue() * paymentDetail.getExchangeRateForTransaction())-discountAmount), companyid));
                    }
                } else {
                    if (receipt.getApprovestatuslevel() == Constants.APPROVED_STATUS_LEVEL) {
                        obj.put("amountdue", (isCopyTransaction || isDishonouredCheque)? authHandler.round(invoice.getInvoiceamountdue() * paymentDetail.getExchangeRateForTransaction(), companyid) : authHandler.round(invoice.getInvoiceamountdue() * paymentDetail.getExchangeRateForTransaction(), companyid) + (paymentDetail.getAmount()+discountAmount));
                        obj.put("amountDueOriginal", (isCopyTransaction || isDishonouredCheque) ? invoice.getInvoiceamountdue() : invoice.getInvoiceamountdue() + (paymentDetail.getAmountInInvoiceCurrency()+discountAmtInInvoiceCurrency));
                        obj.put("amountDueOriginalSaved", (isCopyTransaction || isDishonouredCheque) ? invoice.getInvoiceamountdue() : invoice.getInvoiceamountdue() + (paymentDetail.getAmountInInvoiceCurrency()+discountAmtInInvoiceCurrency));
                        obj.put("amountdueafterdiscount", (isCopyTransaction || isDishonouredCheque) ? authHandler.round(invoice.getInvoiceamountdue() * paymentDetail.getExchangeRateForTransaction(), companyid) : authHandler.round(((invoice.getInvoiceamountdue() * paymentDetail.getExchangeRateForTransaction())+paymentDetail.getAmount()), companyid));
                    } else {
                        obj.put("amountdue", authHandler.round(invoice.getInvoiceamountdue() * paymentDetail.getExchangeRateForTransaction(), companyid));
                        obj.put("amountDueOriginal", invoice.getInvoiceamountdue());
                        obj.put("amountDueOriginalSaved", invoice.getInvoiceamountdue());
                        obj.put("amountdueafterdiscount", authHandler.round(((invoice.getInvoiceamountdue() * paymentDetail.getExchangeRateForTransaction())-discountAmount), companyid));
                    }
                }
//                if (invoice.isNormalInvoice()) {
//                    obj.put("amountdue", isCopyTransaction ? authHandler.round(invoice.getInvoiceamountdue() * paymentDetail.getExchangeRateForTransaction(), companyid) : authHandler.round(invoice.getInvoiceamountdue() * paymentDetail.getExchangeRateForTransaction(), companyid) + paymentDetail.getAmount());
//                    obj.put("amountDueOriginal", isCopyTransaction ? invoice.getInvoiceamountdue() : invoice.getInvoiceamountdue() + paymentDetail.getAmountInInvoiceCurrency());
//                    obj.put("amountDueOriginalSaved", isCopyTransaction ? invoice.getInvoiceamountdue() : invoice.getInvoiceamountdue() + paymentDetail.getAmountInInvoiceCurrency());
//                } else {
//                    obj.put("amountdue", isCopyTransaction ? authHandler.round(invoice.getOpeningBalanceAmountDue() * paymentDetail.getExchangeRateForTransaction(), companyid) : authHandler.round(invoice.getOpeningBalanceAmountDue() * paymentDetail.getExchangeRateForTransaction(), companyid) + paymentDetail.getAmount());
//                    obj.put("amountDueOriginal", isCopyTransaction ? invoice.getOpeningBalanceAmountDue() : invoice.getOpeningBalanceAmountDue() + paymentDetail.getAmountInInvoiceCurrency());
//                    obj.put("amountDueOriginalSaved", isCopyTransaction ? invoice.getOpeningBalanceAmountDue() : invoice.getOpeningBalanceAmountDue() + paymentDetail.getAmountInInvoiceCurrency());
//                }
            }
            obj.put("taxamount", 0);
            obj.put("prtaxid", "");
            obj.put("description", StringUtil.DecodeText(paymentDetail.getDescription()));
            obj.put("documentid", invoice.getID());
            obj.put("documentno", invoice.getInvoiceNumber());
            obj.put("payment", "");
            obj.put("currencyid", receipt.getCurrency().getCurrencyID());
            obj.put("currencysymbol", receipt.getCurrency().getSymbol());
            obj.put("currencyname", receipt.getCurrency().getName());
            obj.put("currencysymboltransaction", invoice.getCurrency().getSymbol());
            obj.put("currencynametransaction", invoice.getCurrency().getName());
            obj.put("currencyidtransaction", invoice.getCurrency().getCurrencyID());
            obj.put("gstCurrencyRate", paymentDetail.getGstCurrencyRate());
            obj.put("date", df.format((Date)invoice.getCreationDate()));
//             if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
//                obj.put("date", df.format((Date)invoice.getCreationDate()));
//            }else{
//                obj.put("date", df.format((Date)invoice.getJournalEntry().getEntryDate()));
//            }
            obj.put("claimedDate", invoice.getDebtClaimedDate()==null?"":df.format(invoice.getDebtClaimedDate()));  
            obj.put("srNoForRow", paymentDetail.getSrNoForRow());
            obj.put("rowdetailid", paymentDetail.getID());
            obj.put("invoicecreationdate", df.format(invoice.getCreationDate()));
//            if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
//                obj.put("invoicecreationdate", df.format(invoice.getCreationDate()));
//            }else{
//                obj.put("invoicecreationdate", df.format(invoice.getJournalEntry().getEntryDate()));
//            }
            obj.put("invoiceduedate", df.format((Date) invoice.getDueDate()));
            obj.put("applicabledays", invoice.getTermid() != null ? invoice.getTermid().getApplicableDays() : -1);
            obj.put("discounttype", (invoice.getTermid() != null && invoice.getTermid().getDiscountName() != null) ? invoice.getTermid().getDiscountName().isDiscounttype() : "");
            obj.put("discountvalue", (invoice.getTermid() != null && invoice.getTermid().getDiscountName() != null) ? invoice.getTermid().getDiscountName().getValue() : "");
            /**
             * Passing JE date in response for validating date on JS Side
             * ERM-655.
             */
            if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
                obj.put("jeDate", df.format((Date) invoice.getCreationDate()));
            } else {
                obj.put("jeDate", df.format((Date) invoice.getJournalEntry().getEntryDate()));
            }
            if(invoice.isIsOpeningBalenceInvoice()){
                obj.put("amount",invoice.getOriginalOpeningBalanceAmount());
            }else {
                obj.put("amount", invoice.getCustomerEntry().getAmount());
            }
            getCustomDataInfo(obj,paymentDetail.getID(),customFieldMap,FieldMap,replaceFieldMap);
            jSONArray.put(obj);
        }
    }
    public void getReceiptDetailsInfoOnCurrencyChange(Receipt receipt, Account acc, HashMap<String, Object> requestParams,JSONArray jSONArray) throws JSONException, ServiceException, UnsupportedEncodingException, ParseException {
        DateFormat df = (DateFormat) requestParams.get("df");
        Set<ReceiptDetail> paymentDetails=receipt.getRows();
        HashMap<String, Object> fieldrequestParams = new HashMap();
        HashMap<String, String> customFieldMap = new HashMap<String, String>();
        HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
        fieldrequestParams.put(Constants.filter_values, Arrays.asList(receipt.getCompany().getCompanyID(), Constants.Acc_Receive_Payment_ModuleId));
        HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
        HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap,customFieldMap,customDateFieldMap);
        for (ReceiptDetail paymentDetail : paymentDetails) {
            JSONObject obj = new JSONObject();
            Invoice invoice=paymentDetail.getInvoice();
            obj.put("type",Constants.PaymentAgainstInvoice);
            obj.put("modified", true);
//            obj.put("exchangeratefortransaction", paymentDetail.getExchangeRateForTransaction());
//            obj.put("enteramount", paymentDetail.getAmount());
//            obj.put("amountdue", paymentDetail.getAmountDueInPaymentCurrency());
            getReceiptDetailsAmountDue(paymentDetail, requestParams, obj);
//            obj.put("amountDueOriginal", paymentDetail.getAmountDueInInvoiceCurrency());
//            obj.put("amountDueOriginalSaved", paymentDetail.getAmountDueInInvoiceCurrency());
            obj.put("amountDueOriginal", invoice.getInvoiceamountdue()+paymentDetail.getAmountInInvoiceCurrency());
            obj.put("amountDueOriginalSaved", invoice.getInvoiceamountdue()+paymentDetail.getAmountInInvoiceCurrency());
            obj.put("taxamount", 0);
            obj.put("prtaxid", "");
            obj.put("description", paymentDetail.getDescription());
            obj.put("documentid", invoice.getID());
            obj.put("documentno", invoice.getInvoiceNumber());
            obj.put("payment", "");
//            obj.put("currencyid", receipt.getCurrency().getCurrencyID());
//            obj.put("currencysymbol", receipt.getCurrency().getSymbol());
//            obj.put("currencyname", receipt.getCurrency().getName());
            obj.put("currencysymboltransaction", invoice.getCurrency().getSymbol());
            obj.put("currencynametransaction", invoice.getCurrency().getName());
            obj.put("currencyidtransaction", invoice.getCurrency().getCurrencyID());
            obj.put("gstCurrencyRate", paymentDetail.getGstCurrencyRate());
            obj.put("date", df.format((Date)invoice.getCreationDate()));
//            if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
//                obj.put("date", df.format((Date)invoice.getCreationDate()));
//            }else{
//                obj.put("date", df.format((Date)invoice.getJournalEntry().getEntryDate()));
//            }
            getCustomDataInfo(obj,paymentDetail.getID(),customFieldMap,FieldMap,replaceFieldMap);
            jSONArray.put(obj);
        }
    }
    
    public void getReceiptDetailsAmountDue(ReceiptDetail paymentDetail, HashMap<String, Object> requestParams,JSONObject jSONObject) throws JSONException, ServiceException, UnsupportedEncodingException, ParseException {
                 DateFormat df = (DateFormat) requestParams.get("df");
                Date paymentCreationDate = null;
                double externalCurrencyRate = 0d;
                String baseCurrencyID=paymentDetail.getCompany().getCurrency().getCurrencyID();
                String companyid = jSONObject.optString("companyid");
//                boolean isopeningBalancePayment = pd.getPayment().isIsOpeningBalencePayment();
                if (paymentDetail.getReceipt().isIsOpeningBalenceReceipt() && !paymentDetail.getReceipt().isNormalReceipt()) {
                    if(requestParams.containsKey("changedDate")&&requestParams.get("changedDate")!=null&&!requestParams.get("changedDate").toString().equals("")){
                         Date changedDate = new Date(requestParams.get("changedDate").toString());
                         paymentCreationDate = df.parse(df.format(changedDate));
                    }else{
                         paymentCreationDate = paymentDetail.getReceipt().getCreationDate();
                    }
                    externalCurrencyRate = paymentDetail.getReceipt().getExchangeRateForOpeningTransaction();
                } else {
                    if(requestParams.containsKey("changedDate")&&requestParams.get("changedDate")!=null&&!requestParams.get("changedDate").toString().equals("")){
                         Date changedDate = new Date(requestParams.get("changedDate").toString());
                         paymentCreationDate = df.parse(df.format(changedDate));
                    }else{
//                        paymentCreationDate = paymentDetail.getReceipt().getJournalEntry().getEntryDate();
                        paymentCreationDate = paymentDetail.getReceipt().getCreationDate();
                    }
                    externalCurrencyRate = paymentDetail.getReceipt().getJournalEntry().getExternalCurrencyRate();
                }
                    Invoice invoice=paymentDetail.getInvoice();
                    String fromcurrencyid=invoice.getCurrency().getCurrencyID();
                    String tocurrencyid = (requestParams.get("newcurrency") == null ? baseCurrencyID : requestParams.get("newcurrency")+"");
                    KwlReturnObject currResult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), tocurrencyid);
                    KWLCurrency currency = (KWLCurrency) currResult.getEntityList().get(0);
                    jSONObject.put("currencyid", tocurrencyid);
                    jSONObject.put("currencysymbol", currency.getSymbol());
                    jSONObject.put("currencyname", currency.getName());
                    KwlReturnObject bAmt = null;
//                    if (isopeningBalancePayment && pd.getPayment().isConversionRateFromCurrencyToBase()) {// if payment is opening balance payment and Conversion rate is taken from user is Currency to base then following method will be called.
//                        bAmt = accCurrencyDAOobj.getOneCurrencyToOtherAccordingToCurrencyToBaseExchangeRate(request, pd.getAmount(), fromcurrencyid, currencyid, paymentCreationDate, externalCurrencyRate);
//                    } else {
                        bAmt = accCurrencyobj.getOneCurrencyToOther(requestParams, 1.0d, fromcurrencyid, tocurrencyid, paymentCreationDate, externalCurrencyRate);
//                    }
                   double ramount = (Double) bAmt.getEntityList().get(0);
                   double amountDue=authHandler.round(invoice.getInvoiceamountdue(), companyid);
                   jSONObject.put("amountdue",authHandler.round(ramount*amountDue, companyid)+paymentDetail.getAmount());
                   jSONObject.put("enteramount",authHandler.round(ramount*paymentDetail.getAmountInInvoiceCurrency(), companyid));
                   if (requestParams.containsKey("totalenteredamount")) {
                        double totalAmount = (Double) requestParams.get("totalenteredamount");
                        requestParams.put("totalenteredamount", authHandler.round(ramount*paymentDetail.getAmountInInvoiceCurrency(), companyid) + totalAmount);
                    } else {
                        requestParams.put("totalenteredamount", authHandler.round(ramount*paymentDetail.getAmountInInvoiceCurrency(), companyid));
                    }
                   if(!invoice.getCurrency().getCurrencyID().equals(tocurrencyid)){
                        jSONObject.put("exchangeratefortransaction",ramount);
                   }else{
                        jSONObject.put("exchangeratefortransaction",1);
                   }

    }
    
    public void getReceiptAdvanceDetailsInfo(Receipt receipt, Account acc, HashMap<String, Object> requestParams,JSONArray jSONArray) throws JSONException, ServiceException, UnsupportedEncodingException {
        DateFormat df = (DateFormat) requestParams.get("df");
        boolean isCopyTransaction = requestParams.get("isCopyTransaction")!=null ? (Boolean)requestParams.get("isCopyTransaction") : false;
        Set<ReceiptAdvanceDetail> advanceDetails=receipt.getReceiptAdvanceDetails();
        HashMap<String, Object> fieldrequestParams = new HashMap();
        HashMap<String, String> customFieldMap = new HashMap<String, String>();
        HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
        fieldrequestParams.put(Constants.filter_values, Arrays.asList(receipt.getCompany().getCompanyID(), Constants.Acc_Receive_Payment_ModuleId));
        HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
        HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap,customFieldMap,customDateFieldMap);
        for (ReceiptAdvanceDetail advanceDetail : advanceDetails) {
            JSONObject obj = new JSONObject();
            obj.put("type",advanceDetail.getAdvanceType());
            obj.put("debit",false);
            obj.put("modified", true);
            obj.put("enteramount", advanceDetail.getAmount());
            obj.put("taxamount", advanceDetail.getTaxamount());
            obj.put("prtaxid", !StringUtil.isNullObject(advanceDetail.getTax())?advanceDetail.getTax().getID():"");
//            obj.put("prtaxid", advanceDetail.getTax() != null ? (isCopyTransaction ? (advanceDetail.getTax().isActivated() ? advanceDetail.getTax().getID() : "") : advanceDetail.getTax().getID()) : "");
            obj.put("description",  advanceDetail.getDescription() == null?"":StringUtil.DecodeText(advanceDetail.getDescription()));
            obj.put("payment", "");
            /*
             * isCopyTransaction is used for identifying the copy case
             * When this flag is true, data in the grid will be loaded in the similar way as laoded for create new case.
             * In copy case, document type and amount received will be same as original receipt, but document number will be blank
             * also, in case of refund, if advance payment with different currency is linked, copy case will show initial transaction currency at line level as receipt currency
             * (similar to create new case)
             */
            if(advanceDetail.getAdvancedetailid()!=null) {    // Refund against vendor
                List<Object[]> advPayDetails = accountingHandlerDAOobj.getPaymentAdvanceDetailsInRefundCase(advanceDetail.getAdvancedetailid());
                Object[] row = advPayDetails.get(0);
                double paymentAdvAmountDue = Double.parseDouble(row[2].toString());
                KwlReturnObject paymentcurrResult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), row[1].toString());
                KWLCurrency payentcurrency = (KWLCurrency) paymentcurrResult.getEntityList().get(0);
                obj.put("documentid", isCopyTransaction?"":advanceDetail.getAdvancedetailid());
                obj.put("documentno", isCopyTransaction?"":row[0]);
                obj.put("currencyid", isCopyTransaction?receipt.getCurrency().getCurrencyID():payentcurrency.getCurrencyID());
                obj.put("currencysymbol", isCopyTransaction?receipt.getCurrency().getSymbol():payentcurrency.getSymbol());
                obj.put("currencyname", isCopyTransaction?receipt.getCurrency().getName():payentcurrency.getName());
                obj.put("currencysymboltransaction", isCopyTransaction?receipt.getCurrency().getSymbol():payentcurrency.getSymbol());
                obj.put("currencynametransaction", isCopyTransaction?receipt.getCurrency().getName():payentcurrency.getName());
                obj.put("currencyidtransaction", isCopyTransaction?receipt.getCurrency().getCurrencyID():payentcurrency.getCurrencyID());
                double enternedAmnt= advanceDetail.getAmount();
//                double enternedAmntOriginal= advanceDetail.getAmount();
                if (!advanceDetail.getReceipt().getCurrency().getCurrencyID().equals(payentcurrency.getCurrencyID())) {
//                    KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, enternedAmnt, payment.getCurrency().getCurrencyID(),currencyid,  re.getJournalEntry().getEntryDate(), 0);
//                    enternedAmntOriginal = (Double) bAmt.getEntityList().get(0);
                    obj.put("currencyidpayment", payentcurrency.getCurrencyID());
                    obj.put("currencysymbolpayment", payentcurrency.getSymbol());
                }
//                double exchangeratefortransaction =  (enternedAmntOriginal <= 0 && enternedAmnt <= 0) ? 0 : (enternedAmnt / enternedAmntOriginal);
                double exchangeratefortransaction = advanceDetail.getExchangeratefortransaction();
                if (receipt.getApprovestatuslevel() == Constants.APPROVED_STATUS_LEVEL) {
                    obj.put("amountdue", isCopyTransaction ? 0 : paymentAdvAmountDue * exchangeratefortransaction + advanceDetail.getAmount());
                    obj.put("amountDueOriginal", isCopyTransaction ? 0 : paymentAdvAmountDue + (advanceDetail.getAmount() / exchangeratefortransaction));
                    obj.put("amountDueOriginalSaved", isCopyTransaction ? 0 : paymentAdvAmountDue + (advanceDetail.getAmount() / exchangeratefortransaction));
                } else {
                    obj.put("amountdue", isCopyTransaction ? 0 : paymentAdvAmountDue * exchangeratefortransaction);
                    obj.put("amountDueOriginal", isCopyTransaction ? 0 : paymentAdvAmountDue);
                    obj.put("amountDueOriginalSaved", isCopyTransaction ? 0 : paymentAdvAmountDue);
                }
                obj.put("exchangeratefortransaction", isCopyTransaction?1:exchangeratefortransaction);
                obj.put("transactionAmount", row[3]);
            }  else if(advanceDetail.getGST()!=null){                   // Advance payment received against Customer for Malaysian country
                obj.put("exchangeratefortransaction", 1);              
                obj.put("amountdue", isCopyTransaction?0:advanceDetail.getAmountDue());
                obj.put("amountDueOriginal", isCopyTransaction?0:advanceDetail.getAmountDue());
                obj.put("amountDueOriginalSaved", isCopyTransaction?0:advanceDetail.getAmountDue());
                obj.put("documentid", advanceDetail.getGST().getID());
                obj.put("documentno", "");
                obj.put("currencyid", receipt.getCurrency().getCurrencyID());
                obj.put("currencysymbol", receipt.getCurrency().getSymbol());
                obj.put("currencyname", receipt.getCurrency().getName());
                obj.put("currencysymboltransaction", receipt.getCurrency().getSymbol());
                obj.put("currencynametransaction", receipt.getCurrency().getName());
                obj.put("currencyidtransaction", receipt.getCurrency().getCurrencyID());
                obj.put("transactionAmount", 0);
            }else {                                                  // Advance payment received against customer for non-malaysian country
                obj.put("exchangeratefortransaction", 1);
                obj.put("amountdue", isCopyTransaction?0:advanceDetail.getAmountDue());
                obj.put("amountDueOriginal", isCopyTransaction?0:advanceDetail.getAmountDue());
                obj.put("amountDueOriginalSaved", isCopyTransaction?0:advanceDetail.getAmountDue());
                obj.put("documentid", "");
                obj.put("documentno", "");
                obj.put("currencyid", receipt.getCurrency().getCurrencyID());
                obj.put("currencysymbol", receipt.getCurrency().getSymbol());
                obj.put("currencyname", receipt.getCurrency().getName());
                obj.put("currencysymboltransaction", receipt.getCurrency().getSymbol());
                obj.put("currencynametransaction", receipt.getCurrency().getName());
                obj.put("currencyidtransaction", receipt.getCurrency().getCurrencyID());
                obj.put("transactionAmount", 0);
            }
            obj.put("srNoForRow", advanceDetail.getSrNoForRow());
            obj.put("rowdetailid", advanceDetail.getId());
            getCustomDataInfo(obj, advanceDetail.getId(), customFieldMap, FieldMap, replaceFieldMap);
            /**
             * Fetch Term Details for HSN in India case
             */
            if (receipt.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) { // Fetch  term details of Product
                obj.put("adId", advanceDetail.getId());
                obj.put("productid", advanceDetail.getProduct()!=null?advanceDetail.getProduct().getID():"");
                obj.put("productname", advanceDetail.getProduct()!=null?advanceDetail.getProduct().getName():"");   // Show product name in productgrid ERM-1016             
                obj.put("rcmapplicable", advanceDetail.getProduct()!=null?advanceDetail.getProduct().isRcmApplicable():false);
                KwlReturnObject result6 = accReceiptDAOobj.getAdvanceDetailsTerm(obj);
                if (result6.getEntityList() != null && result6.getEntityList().size() > 0 && result6.getEntityList().get(0) != null) {
                    ArrayList<ReceiptAdvanceDetailTermMap> productTermDetail = (ArrayList<ReceiptAdvanceDetailTermMap>) result6.getEntityList();
                    JSONArray productTermJsonArry = new JSONArray();
                    for (ReceiptAdvanceDetailTermMap productTermsMapObj : productTermDetail) {
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
                        productTermJsonObj.put("payableaccountid", productTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getPayableAccount().getID());
                        productTermJsonArry.put(productTermJsonObj);
                    }
                    obj.put("LineTermdetails", productTermJsonArry.toString());
                    obj.put("recTermAmount", advanceDetail.getTaxamount());
                }
                /**
                 * Put GST Tax Class History.
                 */
                obj.put("refdocid", advanceDetail.getId());
                fieldDataManagercntrl.getGSTTaxClassHistory(obj);
            }
            jSONArray.put(obj);
        }
    }
    public void getReceiptAdvanceDetailsInfoOnCurrencyChange(Receipt receipt, Account acc, HashMap<String, Object> requestParams,JSONArray jSONArray) throws JSONException, ServiceException, UnsupportedEncodingException, ParseException {
        DateFormat df = (DateFormat) requestParams.get("df");
        Set<ReceiptAdvanceDetail> advanceDetails=receipt.getReceiptAdvanceDetails();
        HashMap<String, Object> fieldrequestParams = new HashMap();
        HashMap<String, String> customFieldMap = new HashMap<String, String>();
        HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
        fieldrequestParams.put(Constants.filter_values, Arrays.asList(receipt.getCompany().getCompanyID(), Constants.Acc_Receive_Payment_ModuleId));
        HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
        HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap,customFieldMap,customDateFieldMap);
        for (ReceiptAdvanceDetail advanceDetail : advanceDetails) {
            JSONObject obj = new JSONObject();
            obj.put("type",Constants.AdvancePayment);
            obj.put("modified", true);
//            obj.put("exchangeratefortransaction", 1);
//            obj.put("enteramount", advanceDetail.getAmount());
//            obj.put("amountdue", advanceDetail.getAmountDue());
            getAdvanceDetailsAmountDue(advanceDetail, requestParams, obj);
            obj.put("amountDueOriginal", advanceDetail.getAmountDue());
            obj.put("amountDueOriginalSaved", advanceDetail.getAmountDue());
            obj.put("taxamount", 0);
            obj.put("prtaxid", "");
            obj.put("description", advanceDetail.getDescription());
            obj.put("documentid", "");
            obj.put("documentno", "");
            obj.put("payment", "");
//            obj.put("currencyid", receipt.getCurrency().getCurrencyID());
//            obj.put("currencysymbol", receipt.getCurrency().getSymbol());
//            obj.put("currencyname", receipt.getCurrency().getName());
            obj.put("currencysymboltransaction", receipt.getCurrency().getSymbol());
            obj.put("currencynametransaction", receipt.getCurrency().getName());
            obj.put("currencyidtransaction", receipt.getCurrency().getCurrencyID());
            getCustomDataInfo(obj,advanceDetail.getId(),customFieldMap,FieldMap,replaceFieldMap);
            jSONArray.put(obj);
        }
    }
    
    public void getReceiptDetailsLoanInfo(HashMap<String, Object> requestParams) throws JSONException, ServiceException, UnsupportedEncodingException {
        DateFormat df = (DateFormat) requestParams.get("df");
        boolean isCopyTransaction = requestParams.get("isCopyTransaction")!=null ? (Boolean)requestParams.get("isCopyTransaction") : false;
        Receipt receipt = (Receipt) requestParams.get("receipt");
        JSONArray jSONArray = (JSONArray) requestParams.get("jArr");
        String companyid = (String) requestParams.get("companyid");
        Set<ReceiptDetailLoan> receiptDetailsLoan= receipt.getReceiptDetailsLoan();
        for (ReceiptDetailLoan RDL : receiptDetailsLoan) {
            JSONObject obj = new JSONObject();
            RepaymentDetails RD=RDL.getRepaymentDetail();
            Disbursement disbursement = RD.getDisbursement();
            obj.put("type",Constants.PaymentAgainstLoanDisbursement);
            obj.put("debit",false);
            obj.put("modified", true);
            obj.put("exchangeratefortransaction", RDL.getExchangeRateForTransaction());
            obj.put("enteramount", isCopyTransaction?authHandler.round(RD.getAmountdue()*RDL.getExchangeRateForTransaction(), companyid):RDL.getAmount());
            
            obj.put("amountdue", isCopyTransaction?authHandler.round(RD.getAmountdue()*RDL.getExchangeRateForTransaction(), companyid):authHandler.round(RD.getAmountdue()*RDL.getExchangeRateForTransaction(), companyid)+RDL.getAmount());
            obj.put("amountDueOriginal", isCopyTransaction?RD.getAmountdue():RD.getAmountdue()+RDL.getAmountInRepaymentDetailCurrency());
            obj.put("amountDueOriginalSaved", isCopyTransaction?RD.getAmountdue():RD.getAmountdue()+RDL.getAmountInRepaymentDetailCurrency());
            
            obj.put("taxamount", 0);
            obj.put("prtaxid", "");
            obj.put("description", StringUtil.DecodeText(RDL.getDescription()));
            obj.put("documentid", disbursement.getID());
            obj.put("repaymentscheduleid", RD.getID());
            obj.put("documentno", disbursement.getLoanrefnumber());
            obj.put("payment", "");
            obj.put("currencyid", receipt.getCurrency().getCurrencyID());
            obj.put("currencysymbol", receipt.getCurrency().getSymbol());
            obj.put("currencyname", receipt.getCurrency().getName());
            obj.put("currencysymboltransaction", disbursement.getCurrency().getSymbol());
            obj.put("currencynametransaction", disbursement.getCurrency().getName());
            obj.put("currencyidtransaction", disbursement.getCurrency().getCurrencyID());
            obj.put("gstCurrencyRate", RDL.getGstCurrencyRate());
            obj.put("date", df.format((Date)disbursement.getDisbursementdate()));
            obj.put("srNoForRow", RDL.getSrNoForRow());
            jSONArray.put(obj);
        }
    }
    public void getAdvanceDetailsAmountDue(ReceiptAdvanceDetail advanceDetail, HashMap<String, Object> requestParams,JSONObject jSONObject) throws JSONException, ServiceException, UnsupportedEncodingException, ParseException {

                DateFormat df = (DateFormat) requestParams.get("df");
                Date paymentCreationDate = null;
                double externalCurrencyRate = 0d;
                String companyid = jSONObject.optString("companyid");
                String baseCurrencyID=advanceDetail.getCompany().getCurrency().getCurrencyID();
//                boolean isopeningBalancePayment = pd.getPayment().isIsOpeningBalencePayment();

                if (advanceDetail.getReceipt().isIsOpeningBalenceReceipt() && !advanceDetail.getReceipt().isNormalReceipt()) {
                    if(requestParams.containsKey("changedDate")&&requestParams.get("changedDate")!=null&&!requestParams.get("changedDate").toString().equals("")){
                        Date changedDate = new Date(requestParams.get("changedDate").toString());
                         paymentCreationDate = df.parse(df.format(changedDate));
                    }else{
                        paymentCreationDate = advanceDetail.getReceipt().getCreationDate();
                    }
                    externalCurrencyRate = advanceDetail.getReceipt().getExchangeRateForOpeningTransaction();
                } else {
                    if(requestParams.containsKey("changedDate")&&requestParams.get("changedDate")!=null&&!requestParams.get("changedDate").toString().equals("")){
                        Date changedDate = new Date(requestParams.get("changedDate").toString());
                         paymentCreationDate = df.parse(df.format(changedDate));
                    }else{
//                        paymentCreationDate = advanceDetail.getReceipt().getJournalEntry().getEntryDate();
                        paymentCreationDate = advanceDetail.getReceipt().getCreationDate();
                    }
                    externalCurrencyRate = advanceDetail.getReceipt().getJournalEntry().getExternalCurrencyRate();
                }

                    String fromcurrencyid=advanceDetail.getReceipt().getCurrency().getCurrencyID();
                    String tocurrencyid = (requestParams.get("newcurrency") == null ? baseCurrencyID : requestParams.get("newcurrency")+"");
                    KwlReturnObject currResult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), tocurrencyid);
                    KWLCurrency currency = (KWLCurrency) currResult.getEntityList().get(0);
                    jSONObject.put("currencyid", tocurrencyid);
                    jSONObject.put("currencysymbol", currency.getSymbol());
                    jSONObject.put("currencyname", currency.getName());
                    KwlReturnObject bAmt = null;
//                    if (isopeningBalancePayment && pd.getPayment().isConversionRateFromCurrencyToBase()) {// if payment is opening balance payment and Conversion rate is taken from user is Currency to base then following method will be called.
//                        bAmt = accCurrencyDAOobj.getOneCurrencyToOtherAccordingToCurrencyToBaseExchangeRate(request, pd.getAmount(), fromcurrencyid, currencyid, paymentCreationDate, externalCurrencyRate);
//                    } else {
                        bAmt = accCurrencyobj.getOneCurrencyToOther(requestParams, 1.0d, fromcurrencyid, tocurrencyid, paymentCreationDate, externalCurrencyRate);
//                    }
                   double ramount = (Double) bAmt.getEntityList().get(0);
                   jSONObject.put("amountdue",authHandler.round(ramount*advanceDetail.getAmountDue(), companyid));
                   jSONObject.put("enteramount",authHandler.round(ramount*advanceDetail.getAmount(), companyid));
                   if (requestParams.containsKey("totalenteredamount")) {
                        double totalAmount = (Double) requestParams.get("totalenteredamount");
                        requestParams.put("totalenteredamount", authHandler.round(ramount*advanceDetail.getAmountDue(), companyid) + totalAmount);
                    } else {
                        requestParams.put("totalenteredamount", authHandler.round(ramount*advanceDetail.getAmountDue(), companyid));
                    }
                   if(!fromcurrencyid.equals(tocurrencyid)){
                        jSONObject.put("exchangeratefortransaction",ramount);
                   }
                   else{
                        jSONObject.put("exchangeratefortransaction",1);
                   }

    }
    public void getReceiptCNDNDetailsInfo(Receipt receipt, Account acc, HashMap<String, Object> requestParams,JSONArray jSONArray) throws JSONException, ServiceException, UnsupportedEncodingException {
        DateFormat df = (DateFormat) requestParams.get("df");
        String companyid = (String) requestParams.get("companyid");
        boolean isCopyTransaction = requestParams.get("isCopyTransaction")!=null ? (Boolean)requestParams.get("isCopyTransaction") : false;
        JSONArray innerJArrCNDN = new JSONArray();
            HashMap<String, Object> fieldrequestParams = new HashMap();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(receipt.getCompany().getCompanyID(), Constants.Acc_Receive_Payment_ModuleId));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap,customFieldMap,customDateFieldMap);
                KwlReturnObject cndnResult = accReceiptDAOobj.getCustomerDnPayment(receipt.getID());
                boolean isDishonouredCheque = receipt.isIsDishonouredCheque();
                List<DebitNotePaymentDetails> dnpdList = cndnResult.getEntityList();
                for (DebitNotePaymentDetails dnpd:dnpdList) {
                    Double cnPaidAmountOriginalCurrency = dnpd.getAmountPaid();
                    Double cnPaidAmountPaymentCurrency = dnpd.getPaidAmountInReceiptCurrency();
                    Double exchangeratefortransaction = dnpd.getExchangeRateForTransaction();
                    String description= dnpd.getDescription()!=null?StringUtil.DecodeText(dnpd.getDescription()):"";
                    Double gstCurrencyRate= dnpd.getGstCurrencyRate();
                    int srNoForRow= dnpd.getSrno();
                    DebitNote debitNote = dnpd.getDebitnote();
                    JSONObject obj = new JSONObject();
                    obj.put("type",Constants.PaymentAgainstCNDN);
                    obj.put("debit",false);
                    obj.put("modified", true);
                    obj.put("exchangeratefortransaction", exchangeratefortransaction);
                    obj.put("enteramount", isCopyTransaction?authHandler.round((debitNote.getDnamountdue()*exchangeratefortransaction), companyid):cnPaidAmountPaymentCurrency);
                     if (receipt.getApprovestatuslevel() == Constants.APPROVED_STATUS_LEVEL) {
                        obj.put("amountdue", (isCopyTransaction || isDishonouredCheque) ? authHandler.round((debitNote.getDnamountdue() * exchangeratefortransaction), companyid) : authHandler.round((debitNote.getDnamountdue() * exchangeratefortransaction), companyid) + cnPaidAmountPaymentCurrency);
                        obj.put("amountDueOriginal", (isCopyTransaction || isDishonouredCheque) ? debitNote.getDnamountdue() : debitNote.getDnamountdue() + cnPaidAmountOriginalCurrency);
                        obj.put("amountDueOriginalSaved", (isCopyTransaction || isDishonouredCheque) ? debitNote.getDnamountdue() : debitNote.getDnamountdue() + cnPaidAmountOriginalCurrency);
                    }else{
                        obj.put("amountdue", authHandler.round((debitNote.getDnamountdue() * exchangeratefortransaction), companyid));
                        obj.put("amountDueOriginal", debitNote.getDnamountdue());
                        obj.put("amountDueOriginalSaved", debitNote.getDnamountdue());
                    }
                    
                    obj.put("taxamount", 0);
                    obj.put("prtaxid", "");
                    obj.put("description", description);
                    obj.put("documentid", debitNote.getID());
                    obj.put("documentno", debitNote.getDebitNoteNumber());
                    obj.put("payment", "");
                    obj.put("currencyid", receipt.getCurrency().getCurrencyID());
                    obj.put("currencysymbol", receipt.getCurrency().getSymbol());
                    obj.put("currencyname", receipt.getCurrency().getName());
                    obj.put("currencysymboltransaction", debitNote.getCurrency().getSymbol());
                    obj.put("currencynametransaction", debitNote.getCurrency().getName());
                    obj.put("currencyidtransaction", debitNote.getCurrency().getCurrencyID());
                    obj.put("gstCurrencyRate", gstCurrencyRate);
                    obj.put("srNoForRow", srNoForRow);
                    obj.put("rowdetailid", dnpd.getID());
                    if (debitNote.isIsOpeningBalenceDN() && !debitNote.isNormalDN()) {
                        obj.put("transactionAmount", debitNote.getDnamount());
                    } else {
                        obj.put("transactionAmount", debitNote.getVendorEntry().getAmount());
                    }
                    obj.put("date", df.format((Date)debitNote.getCreationDate()));
//                    if (debitNote.isIsOpeningBalenceDN() && !debitNote.isNormalDN()) {
//                        obj.put("date", df.format((Date)debitNote.getCreationDate()));
//                    }else{
//                        obj.put("date", df.format((Date)debitNote.getJournalEntry().getEntryDate()));
//                    }
                    /**
                     * Passing JE date in response for validating date on JS
                     * Side ERM-655.
                     */
                    if (debitNote.isIsOpeningBalenceDN() && !debitNote.isNormalDN()) {
                        obj.put("jeDate", df.format((Date) debitNote.getCreationDate()));
                    } else {
                        obj.put("jeDate", df.format((Date) debitNote.getJournalEntry().getEntryDate()));
                    }
                    if(!StringUtil.isNullOrEmpty(dnpd.getID()))
                        getCustomDataInfo(obj,dnpd.getID(),customFieldMap,FieldMap,replaceFieldMap);
                    jSONArray.put(obj);
        }
    } 
    public void getReceiptCNDNDetailsInfoOnCurrencyChange(Receipt receipt, Account acc, HashMap<String, Object> requestParams,JSONArray jSONArray) throws JSONException, ServiceException, UnsupportedEncodingException, ParseException {
        DateFormat df = (DateFormat) requestParams.get("df");
        JSONArray innerJArrCNDN = new JSONArray();
            HashMap<String, Object> fieldrequestParams = new HashMap();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(receipt.getCompany().getCompanyID(), Constants.Acc_Receive_Payment_ModuleId));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap,customFieldMap,customDateFieldMap);
                KwlReturnObject cndnResult = accReceiptDAOobj.getCustomerDnPayment(receipt.getID());
                List<DebitNotePaymentDetails> cndnList = cndnResult.getEntityList();
                for (DebitNotePaymentDetails dnpd:cndnList) {
                    Double cnPaidAmountOriginalCurrency = dnpd.getAmountPaid();
                    Double cnPaidAmountPaymentCurrency = dnpd.getPaidAmountInReceiptCurrency();
                    Double cnAmountOriginalCurrency = dnpd.getAmountDue();
                    Double exchangeratefortransaction = dnpd.getExchangeRateForTransaction();
                    Double gstCurrencyRate= dnpd.getGstCurrencyRate();
                    DebitNote debitNote = dnpd.getDebitnote();
                    JSONObject obj = new JSONObject();
                    obj.put("type",Constants.PaymentAgainstCNDN);
                    obj.put("modified", true);
//                    obj.put("exchangeratefortransaction", exchangeratefortransaction);
//                    obj.put("enteramount", cnPaidAmountPaymentCurrency);
//                    obj.put("amountdue", cnAmountPaymentCurrency);
                    getCNDNDetailsAmountDue(debitNote, receipt, cnAmountOriginalCurrency, cnPaidAmountOriginalCurrency,cnPaidAmountPaymentCurrency,exchangeratefortransaction, requestParams, obj);
//                    obj.put("amountDueOriginal", cnAmountOriginalCurrency);
//                    obj.put("amountDueOriginalSaved", cnAmountOriginalCurrency);
                    obj.put("amountDueOriginal", debitNote.getDnamountdue()+cnPaidAmountOriginalCurrency);
                    obj.put("amountDueOriginalSaved", debitNote.getDnamountdue()+cnPaidAmountOriginalCurrency);
                    obj.put("taxamount", 0);
                    obj.put("prtaxid", "");
                    obj.put("description", "");
                    obj.put("documentid", debitNote.getID());
                    obj.put("documentno", debitNote.getDebitNoteNumber());
                    obj.put("payment", "");
//                    obj.put("currencyid", receipt.getCurrency().getCurrencyID());
//                    obj.put("currencysymbol", receipt.getCurrency().getSymbol());
//                    obj.put("currencyname", receipt.getCurrency().getName());
                    obj.put("currencysymboltransaction", debitNote.getCurrency().getSymbol());
                    obj.put("currencynametransaction", debitNote.getCurrency().getName());
                    obj.put("currencyidtransaction", debitNote.getCurrency().getCurrencyID());
                    obj.put("gstCurrencyRate", gstCurrencyRate);
                    obj.put("date", df.format((Date)debitNote.getCreationDate()));
//                     if (debitNote.isIsOpeningBalenceDN() && !debitNote.isNormalDN()) {
//                        obj.put("date", df.format((Date)debitNote.getCreationDate()));
//                    }else{
//                        obj.put("date", df.format((Date)debitNote.getJournalEntry().getEntryDate()));
//                    }
                    if(!StringUtil.isNullOrEmpty(dnpd.getID()))
                        getCustomDataInfo(obj,dnpd.getID(),customFieldMap,FieldMap,replaceFieldMap);
                    jSONArray.put(obj);
        }
    } 
    public void getCNDNDetailsAmountDue(DebitNote debitNote,Receipt receipt,double cnAmountOriginalCurrency,double cnPaidAmountOriginalCurrency,double cnPaidAmountPaymentCurrency,double exchangeratefortransaction,HashMap<String, Object> requestParams,JSONObject jSONObject) throws JSONException, ServiceException, UnsupportedEncodingException, ParseException {

                DateFormat df = (DateFormat) requestParams.get("df");
                Date paymentCreationDate = null;
                double externalCurrencyRate = 0d;
                String companyid = jSONObject.optString("companyid");
                String baseCurrencyID=debitNote.getCompany().getCurrency().getCurrencyID();
//                boolean isopeningBalancePayment = pd.getPayment().isIsOpeningBalencePayment();

                if (receipt.isIsOpeningBalenceReceipt() && !receipt.isNormalReceipt()) {
                    if(requestParams.containsKey("changedDate")&&requestParams.get("changedDate")!=null&&!requestParams.get("changedDate").toString().equals("")){
                        Date changedDate = new Date(requestParams.get("changedDate").toString());
                         paymentCreationDate = df.parse(df.format(changedDate));
                    }else{
                        paymentCreationDate = receipt.getCreationDate();
                    }
                    externalCurrencyRate = receipt.getExchangeRateForOpeningTransaction();
                } else {
                    if(requestParams.containsKey("changedDate")&&requestParams.get("changedDate")!=null&&!requestParams.get("changedDate").toString().equals("")){
                        Date changedDate = new Date(requestParams.get("changedDate").toString());
                         paymentCreationDate = df.parse(df.format(changedDate));
                    }else{
//                        paymentCreationDate = receipt.getJournalEntry().getEntryDate();
                        paymentCreationDate = receipt.getCreationDate();
                    }
                    externalCurrencyRate = receipt.getJournalEntry().getExternalCurrencyRate();
                }

                    String fromcurrencyid=debitNote.getCurrency().getCurrencyID();
                    String tocurrencyid = (requestParams.get("newcurrency") == null ? baseCurrencyID : requestParams.get("newcurrency")+"");
                    KwlReturnObject currResult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), tocurrencyid);
                    KWLCurrency currency = (KWLCurrency) currResult.getEntityList().get(0);
                    jSONObject.put("currencyid", tocurrencyid);
                    jSONObject.put("currencysymbol", currency.getSymbol());
                    jSONObject.put("currencyname", currency.getName());
                    KwlReturnObject bAmt = null;
//                    if (isopeningBalancePayment && pd.getPayment().isConversionRateFromCurrencyToBase()) {// if payment is opening balance payment and Conversion rate is taken from user is Currency to base then following method will be called.
//                        bAmt = accCurrencyDAOobj.getOneCurrencyToOtherAccordingToCurrencyToBaseExchangeRate(request, pd.getAmount(), fromcurrencyid, currencyid, paymentCreationDate, externalCurrencyRate);
//                    } else {
                        bAmt = accCurrencyobj.getOneCurrencyToOther(requestParams, 1.0d, fromcurrencyid, tocurrencyid, paymentCreationDate, externalCurrencyRate);
//                    }
                   double ramount = (Double) bAmt.getEntityList().get(0);
//                   jSONObject.put("amountdue",authHandler.round(ramount*cnAmountOriginalCurrency, 2));
                  jSONObject.put("amountdue", authHandler.round((debitNote.getDnamountdue()*exchangeratefortransaction), companyid)+cnPaidAmountPaymentCurrency);
                   jSONObject.put("enteramount",authHandler.round(ramount*cnPaidAmountOriginalCurrency, companyid));
                   if (requestParams.containsKey("totalenteredamount")) {
                        double totalAmount = (Double) requestParams.get("totalenteredamount");
                        requestParams.put("totalenteredamount", authHandler.round(ramount*cnPaidAmountOriginalCurrency, companyid) + totalAmount);
                    } else {
                        requestParams.put("totalenteredamount", authHandler.round(ramount*cnPaidAmountOriginalCurrency, companyid));
                    }
                   if(!debitNote.getCurrency().getCurrencyID().equals(tocurrencyid)){
                        jSONObject.put("exchangeratefortransaction",ramount);
                   }else{
                        jSONObject.put("exchangeratefortransaction",1);
                   }

    }
    public void getReceiptDetailsOtherwiseInfo(Receipt receipt, Account acc, HashMap<String, Object> requestParams,JSONArray jSONArray,StringBuilder vendorNames) throws JSONException, ServiceException, UnsupportedEncodingException {
        DateFormat df = (DateFormat) requestParams.get("df");
        // Comparator for sorting GL records as per sequence in UI for Copy/ Edit case ref SDP-5425
        Set<ReceiptDetailOtherwise> receiptDetailOtherwises = new TreeSet<>(new Comparator<ReceiptDetailOtherwise>() {

            @Override
            public int compare(ReceiptDetailOtherwise MP1, ReceiptDetailOtherwise MP2) {
                if (MP1.getSrNoForRow() > MP2.getSrNoForRow()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        receiptDetailOtherwises.addAll(receipt.getReceiptDetailOtherwises());
        HashMap<String, Object> fieldrequestParams = new HashMap();
        HashMap<String, String> customFieldMap = new HashMap<String, String>();
        HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
        fieldrequestParams.put(Constants.filter_values, Arrays.asList(receipt.getCompany().getCompanyID(), Constants.Acc_Receive_Payment_ModuleId));
        HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
        HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap,customFieldMap,customDateFieldMap);
         boolean isCopyTransaction = (boolean) (requestParams.containsKey("isCopyTransaction") ? requestParams.get("isCopyTransaction") : false);
         double taxPercent=0.0;
        for (ReceiptDetailOtherwise receiptDetailOtherwise : receiptDetailOtherwises) {
            JSONObject obj = new JSONObject();
            obj.put("type",Constants.GLPayment);
            obj.put("modified", true);
            obj.put("exchangeratefortransaction", 1);
             taxPercent=0.0;
            if (receiptDetailOtherwise.getTax() != null) {
//                KwlReturnObject perresult = accTaxObj.getTaxPercent(receipt.getCompany().getCompanyID(), receipt.getJournalEntry().getEntryDate(), receiptDetailOtherwise.getTax().getID());
                KwlReturnObject perresult = accTaxObj.getTaxPercent(receipt.getCompany().getCompanyID(), receipt.getCreationDate(), receiptDetailOtherwise.getTax().getID());
                taxPercent = (Double) perresult.getEntityList().get(0);
            }
            obj.put("taxpercent", taxPercent);
            obj.put("enteramount", receiptDetailOtherwise.getAmount());
            obj.put("amountdue", 0);
            obj.put("transactionAmount",0);
            obj.put("amountDueOriginal", 0);
            obj.put("amountDueOriginalSaved", 0);
            obj.put("taxamount", receiptDetailOtherwise.getTaxamount());
            if(receiptDetailOtherwise.getTax()!=null){
                obj.put("taxname", receiptDetailOtherwise.getTax().getName());
                obj.put("prtaxid", receiptDetailOtherwise.getTax().getID());
//                obj.put("taxname", isCopyTransaction ? (receiptDetailOtherwise.getTax().isActivated() ? receiptDetailOtherwise.getTax().getName() : "") : receiptDetailOtherwise.getTax().getName());
//                obj.put("prtaxid", isCopyTransaction ? (receiptDetailOtherwise.getTax().isActivated() ? receiptDetailOtherwise.getTax().getID() : "") : receiptDetailOtherwise.getTax().getID());
            }else{
                obj.put("taxname", "");
                obj.put("prtaxid", "");
            }
            try {
            obj.put("description", StringUtil.DecodeText(receiptDetailOtherwise.getDescription()));
            } catch (Exception e) {
                obj.put("description", receiptDetailOtherwise.getDescription());
            }
            obj.put("documentid", receiptDetailOtherwise.getAccount().getID());
//            obj.put("documentno", receiptDetailOtherwise.getAccount().getName());
            obj.put("documentno", StringUtil.isNullOrEmpty(receiptDetailOtherwise.getAccount().getName()) ? (StringUtil.isNullOrEmpty(receiptDetailOtherwise.getAccount().getAcccode()) ? "" : receiptDetailOtherwise.getAccount().getAcccode()) : receiptDetailOtherwise.getAccount().getName());
            obj.put(Constants.accountCode, receiptDetailOtherwise.getAccount() != null && StringUtil.isNullOrEmpty(receiptDetailOtherwise.getAccount().getAcccode()) ? "" : receiptDetailOtherwise.getAccount().getAcccode());
            obj.put("payment", "");
            obj.put("currencyid", receipt.getCurrency().getCurrencyID());
            obj.put("currencysymbol", receipt.getCurrency().getSymbol());
            obj.put("currencyname", receipt.getCurrency().getName());
            obj.put("currencysymboltransaction", receipt.getCurrency().getSymbol());
            obj.put("currencynametransaction", receipt.getCurrency().getName());
            obj.put("currencyidtransaction", receipt.getCurrency().getCurrencyID());
            obj.put("debit", receiptDetailOtherwise.isIsdebit()); // flag used against GL - multi CN/DN
            obj.put("srNoForRow", receiptDetailOtherwise.getSrNoForRow());
            obj.put("rowdetailid", receiptDetailOtherwise.getID());
            obj.put("appliedGst",receiptDetailOtherwise.getGstapplied()==null?"":receiptDetailOtherwise.getGstapplied().getID());
            Account pdoAccount = receiptDetailOtherwise.getAccount();
            obj.put("masterTypeValue", pdoAccount.getMastertypevalue());
            vendorNames.append(URLEncoder.encode(receiptDetailOtherwise.getAccount().getName(),"UTF-8")).append(",");
            getCustomDataInfo(obj,receiptDetailOtherwise.getID(),customFieldMap,FieldMap,replaceFieldMap);
            jSONArray.put(obj);
        }
        if(vendorNames.lastIndexOf(",")>=0)
            vendorNames.deleteCharAt(vendorNames.lastIndexOf(","));
    }
    public void getReceiptDetailsOtherwiseInfoOnCurrencyChange(Receipt receipt, Account acc, HashMap<String, Object> requestParams,JSONArray jSONArray,StringBuilder vendorNames) throws JSONException, ServiceException, UnsupportedEncodingException, ParseException {
        DateFormat df = (DateFormat) requestParams.get("df");
        // Comparator for sorting GL records as per sequence in UI for Copy/ Edit case ref SDP-5425
        Set<ReceiptDetailOtherwise> receiptDetailOtherwises = new TreeSet<>(new Comparator<ReceiptDetailOtherwise>() {

            @Override
            public int compare(ReceiptDetailOtherwise MP1, ReceiptDetailOtherwise MP2) {
                if (MP1.getSrNoForRow() > MP2.getSrNoForRow()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        receiptDetailOtherwises.addAll(receipt.getReceiptDetailOtherwises());
        HashMap<String, Object> fieldrequestParams = new HashMap();
        HashMap<String, String> customFieldMap = new HashMap<String, String>();
        HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
        fieldrequestParams.put(Constants.filter_values, Arrays.asList(receipt.getCompany().getCompanyID(), Constants.Acc_Receive_Payment_ModuleId));
        HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
        HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap,customFieldMap,customDateFieldMap);
        double taxPercent=0.0;
        for (ReceiptDetailOtherwise receiptDetailOtherwise : receiptDetailOtherwises) {
            JSONObject obj = new JSONObject();
            obj.put("type",Constants.GLPayment);
            obj.put("modified", true);
            obj.put("exchangeratefortransaction", 1);
//            obj.put("enteramount", receiptDetailOtherwise.getAmount());
            getReceiptOtherwiseDetailsAmountDue(receiptDetailOtherwise, requestParams, obj);
             taxPercent=0.0;
            if (receiptDetailOtherwise.getTax() != null) {
//                KwlReturnObject perresult = accTaxObj.getTaxPercent(receipt.getCompany().getCompanyID(), receipt.getJournalEntry().getEntryDate(), receiptDetailOtherwise.getTax().getID());
                KwlReturnObject perresult = accTaxObj.getTaxPercent(receipt.getCompany().getCompanyID(), receipt.getCreationDate(), receiptDetailOtherwise.getTax().getID());
                taxPercent = (Double) perresult.getEntityList().get(0);
            }
            obj.put("taxpercent", taxPercent);
            obj.put("amountdue", 0);
            obj.put("amountDueOriginal", 0);
            obj.put("amountDueOriginalSaved", 0);
//            obj.put("taxamount", receiptDetailOtherwise.getTaxamount());
            if(receiptDetailOtherwise.getTax()!=null){
                obj.put("taxname", receiptDetailOtherwise.getTax().getName());
                obj.put("prtaxid", receiptDetailOtherwise.getTax().getID());
            }else{
                obj.put("taxname", "");
                obj.put("prtaxid", "");
            }
            obj.put("description", receiptDetailOtherwise.getDescription());
            obj.put("documentid", receiptDetailOtherwise.getAccount().getID());
            obj.put("documentno", receiptDetailOtherwise.getAccount().getName());
            obj.put("payment", "");
            obj.put("currencyid", receipt.getCurrency().getCurrencyID());
            obj.put("currencysymbol", receipt.getCurrency().getSymbol());
            obj.put("currencyname", receipt.getCurrency().getName());
            obj.put("currencysymboltransaction", receipt.getCurrency().getSymbol());
            obj.put("currencynametransaction", receipt.getCurrency().getName());
            obj.put("currencyidtransaction", receipt.getCurrency().getCurrencyID());
            vendorNames.append(URLEncoder.encode(receiptDetailOtherwise.getAccount().getName(),"UTF-8")).append(",");
            getCustomDataInfo(obj,receiptDetailOtherwise.getID(),customFieldMap,FieldMap,replaceFieldMap);
            jSONArray.put(obj);
        }
        if(vendorNames.lastIndexOf(",")>=0)
            vendorNames.deleteCharAt(vendorNames.lastIndexOf(","));
    }
    public void getReceiptOtherwiseDetailsAmountDue(ReceiptDetailOtherwise receiptDetailOtherwise, HashMap<String, Object> requestParams,JSONObject jSONObject) throws JSONException, ServiceException, UnsupportedEncodingException, ParseException {

                DateFormat df = (DateFormat) requestParams.get("df");
                Date paymentCreationDate = null;
                double externalCurrencyRate = 0d;
                String companyid = jSONObject.optString("companyid");
                String baseCurrencyID=receiptDetailOtherwise.getReceipt().getCompany().getCurrency().getCurrencyID();
//                boolean isopeningBalancePayment = pd.getPayment().isIsOpeningBalencePayment();

                if (receiptDetailOtherwise.getReceipt().isIsOpeningBalenceReceipt() && !receiptDetailOtherwise.getReceipt().isNormalReceipt()) {
                    if(requestParams.containsKey("changedDate")&&requestParams.get("changedDate")!=null&&!requestParams.get("changedDate").toString().equals("")){
                        Date changedDate = new Date(requestParams.get("changedDate").toString());
                         paymentCreationDate = df.parse(df.format(changedDate));
                    }else{
                        paymentCreationDate = receiptDetailOtherwise.getReceipt().getCreationDate();
                    }
                    externalCurrencyRate = receiptDetailOtherwise.getReceipt().getExchangeRateForOpeningTransaction();
                } else {
                    if(requestParams.containsKey("changedDate")&&requestParams.get("changedDate")!=null&&!requestParams.get("changedDate").toString().equals("")){
                        Date changedDate = new Date(requestParams.get("changedDate").toString());
                         paymentCreationDate = df.parse(df.format(changedDate));
                    }else{
//                        paymentCreationDate = receiptDetailOtherwise.getReceipt().getJournalEntry().getEntryDate();
                        paymentCreationDate = receiptDetailOtherwise.getReceipt().getCreationDate();
                    }
                    externalCurrencyRate = receiptDetailOtherwise.getReceipt().getJournalEntry().getExternalCurrencyRate();
                }

                    String fromcurrencyid=receiptDetailOtherwise.getReceipt().getCurrency().getCurrencyID();
                    String tocurrencyid = (requestParams.get("newcurrency") == null ? baseCurrencyID : requestParams.get("newcurrency")+"");
                    KwlReturnObject currResult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), tocurrencyid);
                    KWLCurrency currency = (KWLCurrency) currResult.getEntityList().get(0);
                    jSONObject.put("currencyid", tocurrencyid);
                    jSONObject.put("currencysymbol", currency.getSymbol());
                    jSONObject.put("currencyname", currency.getName());
                    KwlReturnObject bAmt = null;
//                    if (isopeningBalancePayment && pd.getPayment().isConversionRateFromCurrencyToBase()) {// if payment is opening balance payment and Conversion rate is taken from user is Currency to base then following method will be called.
//                        bAmt = accCurrencyDAOobj.getOneCurrencyToOtherAccordingToCurrencyToBaseExchangeRate(request, pd.getAmount(), fromcurrencyid, currencyid, paymentCreationDate, externalCurrencyRate);
//                    } else {
                        bAmt = accCurrencyobj.getOneCurrencyToOther(requestParams, 1.0d, fromcurrencyid, tocurrencyid, paymentCreationDate, externalCurrencyRate);
//                    }
                   double ramount = (Double) bAmt.getEntityList().get(0);
                   jSONObject.put("enteramount",authHandler.round(ramount*receiptDetailOtherwise.getAmount(), companyid));
                    if (requestParams.containsKey("totalenteredamount")) {
                        double totalAmount = (Double) requestParams.get("totalenteredamount");
                        requestParams.put("totalenteredamount", authHandler.round(ramount*receiptDetailOtherwise.getAmount(), companyid) + totalAmount);
                    } else {
                        requestParams.put("totalenteredamount", authHandler.round(ramount*receiptDetailOtherwise.getAmount(), companyid));
                    }
                   double taxPercent = 0.0;
                    if (receiptDetailOtherwise.getTax() != null) {
//                        KwlReturnObject perresult = accTaxObj.getTaxPercent(receiptDetailOtherwise.getReceipt().getCompany().getCompanyID(), receiptDetailOtherwise.getReceipt().getJournalEntry().getEntryDate(), receiptDetailOtherwise.getTax().getID());
                        KwlReturnObject perresult = accTaxObj.getTaxPercent(receiptDetailOtherwise.getReceipt().getCompany().getCompanyID(), receiptDetailOtherwise.getReceipt().getCreationDate(), receiptDetailOtherwise.getTax().getID());
                        taxPercent = (Double) perresult.getEntityList().get(0);
                    }
                   double taxamount=taxPercent*(authHandler.round(ramount*receiptDetailOtherwise.getAmount(), companyid))/100;
                   jSONObject.put("taxamount", taxamount);
                   if(!fromcurrencyid.equals(tocurrencyid)){
                        jSONObject.put("exchangeratefortransaction",ramount);
                   }else{
                        jSONObject.put("exchangeratefortransaction",1);
                   }

    }
    
    public void getCustomDataInfo(JSONObject obj,String rowId,HashMap<String, String> customFieldMap,HashMap<String, Integer> FieldMap,HashMap<String, String> replaceFieldMap) throws JSONException, ServiceException, UnsupportedEncodingException {
                    // ## Get Custom Field Data 
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                    ArrayList Detailfilter_names = new ArrayList(),Detailfilter_params = new ArrayList();
                    Detailfilter_names.add(Constants.Acc_JEDetail_recdetailId);
                    Detailfilter_params.add(rowId);
                    invDetailRequestParams.put("filter_names", Detailfilter_names);
                    invDetailRequestParams.put("filter_params", Detailfilter_params);
                    KwlReturnObject idcustresult = accReceiptDAOobj.getReciptPaymentCustomData(invDetailRequestParams);
                    if(idcustresult.getEntityList().size()>0) {
                        AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
                        AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap,variableMap);
                        for (Entry<String, Object> varEntry : variableMap.entrySet()) {
                            String coldata = varEntry.getValue()!=null?varEntry.getValue().toString():"";
                            if(customFieldMap.containsKey(varEntry.getKey())){
//                                    KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), coldata);
//                                    FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                                   if(coldata != null){
                                       obj.put(varEntry.getKey(), coldata!=null?coldata:"");//fieldComboData.getValue()!=null ?fieldComboData.getValue():"");
                                   }
                              }else{
                                     if (!StringUtil.isNullOrEmpty(coldata)) {
                                        obj.put(varEntry.getKey(), coldata);
                                     }
                              }
                        }
                    }
    } 
    public PayDetail getPayDetailObject(HttpServletRequest request,Receipt editReceiptObject,Receipt receipt) throws SessionExpiredException, ServiceException, AccountingException {
        List list = new ArrayList();
        String oldChequeNo = "";
        PayDetail pdetail = null;

        try {
//            String detailsJsonString = request.getParameter("Details");;
//            JSONArray jSONArray = new JSONArray(detailsJsonString);
            Account dipositTo = null;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
//            String receiptid = request.getParameter("billid");
            String methodid = request.getParameter("pmtmethod");
            sessionHandlerImpl.updatePaymentMethodID(request, methodid);
//            boolean bankReconsilationEntry = false, bankPayment = false;
//            Date clearanceDate = null, startDate = null, endDate = null;
//            String bankAccountId = "";            
//            String payDetailID = null;
//            JournalEntry oldJE = editReceiptObject != null ? editReceiptObject.getJournalEntry() : null;

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            currencyid = (request.getParameter("currencyid") == null ? currency.getCurrencyID() : request.getParameter("currencyid"));

//            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
//            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
//
//            KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
//            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);
//
            KwlReturnObject result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), request.getParameter("pmtmethod"));
            PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);

            dipositTo = payMethod.getAccount();
            HashMap receipthm = new HashMap();
            receipthm.put("paymethodid", payMethod.getID());
            receipthm.put("companyid", companyid);
            if (payMethod.getDetailType() != PaymentMethod.TYPE_CASH && request.getParameter("paydetail") != null) {

                JSONObject obj = new JSONObject(request.getParameter("paydetail"));
                if (payMethod.getDetailType() == PaymentMethod.TYPE_BANK) {                    
                    saveBankReconsilationDetails(request,receipt);                   
                    HashMap chequehm = new HashMap();
                    chequehm.put("chequeno", obj.optString("chequenumber"));
                    chequehm.put("companyId", companyid);
                    chequehm.put("createdFrom", 2);
                    chequehm.put("bankAccount", (payMethod.getAccount() != null) ? payMethod.getAccount().getID() : "");
                    chequehm.put("description", StringUtil.DecodeText(obj.getString("description")));
                    chequehm.put("bankname", StringUtil.DecodeText(obj.getString("paymentthrough")));
                    chequehm.put("duedate", df.parse(obj.getString("postdate")));
                    chequehm.put("bankmasteritemid", obj.optString("paymentthroughid",""));
                    Map<String, Object> seqchequehm = new HashMap<>();
                    obj.put(Constants.companyKey, companyid);
                    String chequesequenceformat =  obj.optString("sequenceformat");
                    String nextChequeNumber="";
                    /**
                     * getNextChequeNumber method to generate next sequence number using
                     * sequence format,also saving the dateprefix and datesuffix in cheque table.
                     */
                    if (!StringUtil.isNullOrEmpty(chequesequenceformat) && !chequesequenceformat.equals("NA")) {
                        seqchequehm = accCompanyPreferencesObj.getNextChequeNumber(obj);
                    }
                  
                    if (seqchequehm.containsKey(Constants.AUTO_ENTRYNUMBER)) {
                        chequehm.put("chequeno", (String) seqchequehm.get(Constants.AUTO_ENTRYNUMBER));
                    }
                    if (seqchequehm.containsKey(Constants.SEQNUMBER)) {
                        chequehm.put("sequenceNumber", (String) seqchequehm.get(Constants.SEQNUMBER));
                    }
                    if (seqchequehm.containsKey(Constants.DATEPREFIX)) {
                        chequehm.put(Constants.DATEPREFIX, (String) seqchequehm.get(Constants.DATEPREFIX));
                    }
                    if (seqchequehm.containsKey(Constants.DATEAFTERPREFIX)) {
                        chequehm.put(Constants.DATEAFTERPREFIX, (String) seqchequehm.get(Constants.DATEAFTERPREFIX));
                    }
                    if (seqchequehm.containsKey(Constants.DATESUFFIX)) {
                        chequehm.put(Constants.DATESUFFIX, (String) seqchequehm.get(Constants.DATESUFFIX));
                    }
                    KwlReturnObject cqresult = accPaymentDAOobj.addCheque(chequehm);
                    Cheque cheque = (Cheque) cqresult.getEntityList().get(0);
                    receipthm.put("chequeid", cheque.getID());
                } else if (payMethod.getDetailType() == PaymentMethod.TYPE_CARD) {
                    HashMap cardhm = new HashMap();
                    cardhm.put("cardno", obj.getString("CardNo"));
                    cardhm.put("nameoncard", obj.getString("nameoncard"));
                    cardhm.put("expirydate", df.parse(obj.getString("expirydate")));
                    cardhm.put("cardtype", obj.getString("cardtype"));
                    cardhm.put("refno", obj.getString("refno"));
                    KwlReturnObject cdresult = accPaymentDAOobj.addCard(cardhm);
                    Card card = (Card) cdresult.getEntityList().get(0);
                    receipthm.put("cardid", card.getID());
                }
            } 
            pdetail = accPaymentDAOobj.saveOrUpdatePayDetail(receipthm);
        }catch (Exception e) {
             throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return pdetail;
    }
    public void saveBankReconsilationDetails(HttpServletRequest request, Receipt receipt) throws ServiceException {
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            String detailsJsonString = request.getParameter("Details");;
            JSONArray jSONArray = new JSONArray(detailsJsonString);
            JSONObject obj = new JSONObject(request.getParameter("paydetail"));

            KwlReturnObject result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), request.getParameter("pmtmethod"));
            PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);

            if (request.getAttribute("oldjeid") != null && !StringUtil.isNullOrEmpty((String) request.getAttribute("oldjeid"))) {
                Map<String, Object> delReqMap = new HashMap<String, Object>();
                delReqMap.put("oldjeid", request.getAttribute("oldjeid"));
                delReqMap.put("companyId", companyid);
                deleteBankReconcilation(delReqMap);  //deleting bank reconsilation info 
            }
            boolean bankReconsilationEntry = obj.getString("paymentstatus") != null ? obj.getString("paymentstatus").equals("Cleared") : false;
            if (bankReconsilationEntry) {
                String bankAccountId = payMethod.getAccount().getID();
                Date startDate = df.parse(df.format(Calendar.getInstance().getTime()));
                Date endDate = df.parse(df.format(Calendar.getInstance().getTime()));
                Date clearanceDate = df.parse(obj.getString("clearancedate"));
                
                HashMap<String, Object> globalParams = AccountingManager.getGlobalParams(request);
                Map<String, Object> bankReconsilationMap = new HashMap<String, Object>();
                bankReconsilationMap.put("bankAccountId", bankAccountId);
                bankReconsilationMap.put("startDate", startDate);//dont know the significance so have just put current date 
                bankReconsilationMap.put("endDate", endDate);//dont know the significance so have just put current date
                bankReconsilationMap.put("clearanceDate", clearanceDate);
                bankReconsilationMap.put("endingAmount", 0.0);
                bankReconsilationMap.put("companyId", companyid);
                bankReconsilationMap.put("clearingamount", receipt.getDepositAmount());
                bankReconsilationMap.put("currencyid", currencyid);
                bankReconsilationMap.put("details", jSONArray);
                bankReconsilationMap.put("receipt", receipt);
                bankReconsilationMap.put("ismultidebit", true);
                bankReconsilationMap.put("createdby", sessionHandlerImpl.getUserid(request));
                bankReconsilationMap.put("checkCount", 0);
                bankReconsilationMap.put("depositeCount", 1);   //As the discussion with Mayur B. and Sagar A. sir RP relates to deposit count
                saveBankReconsilation(bankReconsilationMap, globalParams);
                auditTrailObj.insertAuditLog(AuditAction.BANK_RECONCILIATION_ADDED, "User " + sessionHandlerImpl.getUserFullName(request) + " has reconciled "+receipt.getReceiptNumber(), request, companyid); 
            }          
        } catch (Exception ex) {
            throw ServiceException.FAILURE("savePayment : " + ex.getMessage(), ex);
        }
    }
    
    public void saveCNDNDetailObject(HttpServletRequest request, JSONArray jSONArrayAgainstCNDN, Receipt receipt, int type,Map<String,Object> counterMap) throws SessionExpiredException, ServiceException, AccountingException {
        try {
            boolean isAgainstDN = StringUtil.getBoolean(request.getParameter("isAgainstDN"));
            String methodid = request.getParameter("pmtmethod");
            sessionHandlerImpl.updatePaymentMethodID(request, methodid);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company)companyResult.getEntityList().get(0);
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
           CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            String person = "";
            StringBuffer billno = new StringBuffer();
            if (type == Constants.PaymentAgainstCNDN) {
                JSONArray drAccArr = jSONArrayAgainstCNDN;
                for (int i = 0; i < drAccArr.length(); i++) {
                    JSONObject jobj = drAccArr.getJSONObject(i);
                    double paidncamount = Double.parseDouble(jobj.getString("enteramount"));
                    double amountdue = Double.parseDouble(jobj.getString("amountdue"));
                    String dnnoteid = jobj.getString("documentid");
                    String receiptId = receipt.getID();
                    int srNoForRow = StringUtil.isNullOrEmpty("srNoForRow") ? 0 : Integer.parseInt(jobj.getString("srNoForRow"));
                    if ((!jobj.getString("documentno").equalsIgnoreCase("undefined")) && (!jobj.getString("documentno").equalsIgnoreCase(""))) {
                        billno.append(jobj.getString("documentno") + ",");
                    }
                    KwlReturnObject cnResult = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), dnnoteid);
                    DebitNote debitNote = (DebitNote) cnResult.getEntityList().get(0);
                    String tocurrency = receipt.getCurrency().getCurrencyID();
                    String fromcurrency = receipt.getCurrency().getCurrencyID();
                    double exchangeratefortransaction = 1;
                    double amountinpaymentcurrency = amountdue;
                    double paidamountinpaymentcurrency = paidncamount;
                    if (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString()) && !StringUtil.isNullOrEmpty(jobj.optString("currencyidtransaction", "")) && !jobj.optString("currencyidtransaction", "").equals(receipt.getCurrency().getCurrencyID())) {
                        tocurrency = jobj.optString("currencyidtransaction", "");
                        fromcurrency = receipt.getCurrency().getCurrencyID();
                        exchangeratefortransaction = Double.parseDouble(jobj.optString("exchangeratefortransaction", "1"));
                        amountdue = amountinpaymentcurrency / Double.parseDouble(jobj.get("exchangeratefortransaction").toString());
                        amountdue = authHandler.round(amountdue, companyid);
                        paidncamount = paidamountinpaymentcurrency / Double.parseDouble(jobj.get("exchangeratefortransaction").toString());
                        paidncamount = authHandler.round(paidncamount, companyid);
                    }
                    /*
                     * Amount received against DN will be converted to base currency as per spot rate of DN 
                     */
                    double dnExternalCurrencyRate=0d;
                    Date dnCreationDate = null;
                    dnCreationDate = debitNote.getCreationDate();
                    if(debitNote.isIsOpeningBalenceDN()){
                        dnExternalCurrencyRate = debitNote.getExchangeRateForOpeningTransaction();
                    } else {
                        dnExternalCurrencyRate = debitNote.getJournalEntry().getExternalCurrencyRate();
//                        dnCreationDate = debitNote.getJournalEntry().getEntryDate();
                    }
                    if (debitNote.isIsOpeningBalenceDN()) {
                        if (debitNote.isConversionRateFromCurrencyToBase()) {
                            dnExternalCurrencyRate = 1 / dnExternalCurrencyRate;
                        }
                    }
                    double amountPaidAgainstDNInPaymentCurrency=Double.parseDouble(jobj.getString("enteramount"));
                    double amountPaidAgainstDNInBaseCurrency=amountPaidAgainstDNInPaymentCurrency;
                    double externalCurrencyRateForPayment = receipt.getJournalEntry().getExternalCurrencyRate();
                    KwlReturnObject bAmt = null;
                    HashMap<String, Object> requestParams = new HashMap();
                    requestParams.put(Constants.companyid, companyid);
                    requestParams.put("gcurrencyid", company.getCurrency().getCurrencyID());
                    bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, paidncamount, debitNote.getCurrency().getCurrencyID(), dnCreationDate, dnExternalCurrencyRate);
                    amountPaidAgainstDNInBaseCurrency= (Double)bAmt.getEntityList().get(0);
                    amountPaidAgainstDNInBaseCurrency=authHandler.round(amountPaidAgainstDNInBaseCurrency, companyid);
                    //Revalution Journal Entrey for Receipt to Customer for debit notes
                    ReevalJournalEntryCNDN(request, debitNote, preferences,exchangeratefortransaction, amountPaidAgainstDNInPaymentCurrency,counterMap);
                    
                    person=" Against debit note ";
                    if (isAgainstDN) {
                        KwlReturnObject cnjedresult = accReceiptDAOobj.updateCnAmount(dnnoteid, paidncamount);
                        KwlReturnObject cnopeningjedresult = accReceiptDAOobj.updateCnOpeningAmountDue(dnnoteid, paidncamount);
                        cnjedresult = accReceiptDAOobj.saveCustomerCnPaymenyHistory(dnnoteid, paidncamount, amountdue, receiptId);
                    } else {// make payment against vendor credit note and also customer credit note.
                        KwlReturnObject cnjedresult = accReceiptDAOobj.updateDnAmount(dnnoteid, paidncamount);
                        KwlReturnObject opencnjedresult = accReceiptDAOobj.updateDnOpeningAmountDue(dnnoteid, paidncamount);
                        KwlReturnObject openingDnBaseAmtDueResult = accReceiptDAOobj.updateDnOpeningBaseAmountDue(dnnoteid, amountPaidAgainstDNInBaseCurrency);
                        HashMap<String, String> hashMapCnPayment = new HashMap<String, String>();
                        hashMapCnPayment.put("cnnoteid", dnnoteid);
                        hashMapCnPayment.put("paymentId", receiptId);
                        hashMapCnPayment.put("originalamountdue", amountdue + "");
                        hashMapCnPayment.put("paidncamount", paidncamount + "");
                        hashMapCnPayment.put("tocurrency", tocurrency);
                        hashMapCnPayment.put("fromcurrency", fromcurrency);
                        hashMapCnPayment.put("exchangeratefortransaction", exchangeratefortransaction + "");
                        hashMapCnPayment.put("amountinpaymentcurrency", amountinpaymentcurrency + "");
                        hashMapCnPayment.put("paidamountinpaymentcurrency", paidamountinpaymentcurrency + "");
                        hashMapCnPayment.put("amountinbasecurrency", amountPaidAgainstDNInBaseCurrency+"");
                        hashMapCnPayment.put("description", jobj.getString("description"));
                        hashMapCnPayment.put("gstCurrencyRate", jobj.optString("gstCurrencyRate", "0.0"));
                        hashMapCnPayment.put("srNoForRow", "" + srNoForRow);
                        if (jobj.has("jedetail") && jobj.get("jedetail") != null) {
                            hashMapCnPayment.put("jedetail", (String)jobj.get("jedetail"));
                        }else{
                            hashMapCnPayment.put("jedetail", "");
                        }
                        cnjedresult = accReceiptDAOobj.saveCustomerDnPaymenyHistory(hashMapCnPayment);
                    }
                    String rowJeId = rowJeId = jobj.optString("rowjedid");
                    KwlReturnObject returnObject = accReceiptDAOobj.getCustomerDnPayment(receiptId, dnnoteid);
                    List<DebitNotePaymentDetails> list = returnObject.getEntityList();
                    for (DebitNotePaymentDetails dnpd:list) {
                        String id = dnpd.getID()!=null?dnpd.getID():"";
                        HashMap<String, JSONArray> jcustomarrayMap = receipt.getJcustomarrayMap();
                        JSONArray jcustomarray = jcustomarrayMap.get(rowJeId);
                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                        customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                        customrequestParams.put("modulerecid", rowJeId);
                        customrequestParams.put("recdetailId", id);
                        customrequestParams.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
                        customrequestParams.put("companyid", companyid);
                        customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            KwlReturnObject receiptAccJEDCustomData = accountingHandlerDAOobj.getObject(AccJEDetailCustomData.class.getName(), rowJeId);
                            AccJEDetailCustomData accJEDetailCustomData = (AccJEDetailCustomData) receiptAccJEDCustomData.getEntityList().get(0);
                            KwlReturnObject receiptJED = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), rowJeId);
                            JournalEntryDetail journalEntryDetail = (JournalEntryDetail) receiptJED.getEntityList().get(0);
                            journalEntryDetail.setAccJEDetailCustomData(accJEDetailCustomData);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
    }
    
    public HashSet receiptDetailObject(HttpServletRequest request, JSONArray jSONArrayAgainstInvoice,Receipt receipt, int type) throws SessionExpiredException, ServiceException, AccountingException {
        List list = new ArrayList();
        HashSet payDetails = null;
        GoodsReceipt goodsReceipt = null;
        try {

            String companyid = sessionHandlerImpl.getCompanyid(request);
            boolean isMultiDebit = StringUtil.getBoolean(request.getParameter("ismultidebit"));
            String methodid = request.getParameter("pmtmethod");
            sessionHandlerImpl.updatePaymentMethodID(request, methodid);
            HashMap<String, JSONArray> Map1 = new HashMap();

            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);
            JSONArray jArr = new JSONArray();
            if (jSONArrayAgainstInvoice != null) {
                jArr = jSONArrayAgainstInvoice;
            }
            payDetails = saveReceiptRows(request,receipt, company, jArr, goodsReceipt, type);
        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return payDetails;
    }
    public HashSet<ReceiptAdvanceDetail> advanceDetailObject(HttpServletRequest request, JSONArray jSONArrayAdvance, Receipt receipt, int type) throws SessionExpiredException, ServiceException, AccountingException {
        List list = new ArrayList();
        HashSet advanceDetails = null;
        Invoice invoice = null;
        try {
            JSONObject requestObj = convertRequestToJSONObjectForLinkPayment(request);
            advanceDetails= new HashSet<ReceiptAdvanceDetail>();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            Map<String,Object> counterMap = new HashMap<>();
            counterMap.put("counter", 0);//this is used to avoid JE Sequence Number
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            for (int i = 0; i < jSONArrayAdvance.length(); i++) {
                JSONObject jobj = jSONArrayAdvance.getJSONObject(i);
                double amountReceived = jobj.getDouble("enteramount");
                KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                Company company = (Company) cmpresult.getEntityList().get(0);
                ReceiptAdvanceDetail advanceDetail = new ReceiptAdvanceDetail();
                advanceDetail.setId(StringUtil.generateUUID());
                advanceDetail.setCompany(company);
                advanceDetail.setReceipt(receipt);
                advanceDetail.setAmount(amountReceived);
                
                /*
                    If Make Payment against Customer and used advance receipt against it then need to maintain receipt object for reference 
                */
                if(!StringUtil.isNullOrEmpty(jobj.optString("documentid",""))) { // documentid will be non-NULL only in two cases - 1) RP against Customer- advance payment for malaysia  2) refund against vendor for any country
                    if(Boolean.parseBoolean(request.getParameter("iscustomer")) && Integer.parseInt(company.getCountry().getID()) == Constants.malaysian_country_id){   // Advance payment against customer for malaysia country
                        String taxid = jobj.getString("documentid");
                        KwlReturnObject taxResult = accountingHandlerDAOobj.getObject(Tax.class.getName(), taxid);
                        Tax taxObj = (Tax)taxResult.getEntityList().get(0);
                        advanceDetail.setGST(taxObj);
                                               
                    } else {                             // Refund case against vendor for any country
                    String advanceDetailId = jobj.getString("documentid");
                    advanceDetail.setAdvancedetailid(advanceDetailId);
                    double amountReceivedConverted = jobj.getDouble("enteramount");
                    if (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString())) {
                        double adjustedRate=Double.parseDouble(jobj.optString("exchangeratefortransaction", "1.0").toString());
//                        double adjustedRate=jobj.optDouble("amountdue",0)/jobj.optDouble("amountDueOriginal",0);
    //                    amountReceivedConverted = amountReceived / Double.parseDouble(jobj.get("exchangeratefortransaction").toString());
                        amountReceivedConverted = amountReceived / adjustedRate;
                        amountReceivedConverted = authHandler.round(amountReceivedConverted, companyid);
                        advanceDetail.setExchangeratefortransaction(adjustedRate);
                         }
                         if (!StringUtil.isNullOrEmpty(advanceDetailId)) {
                            //JE For Receipt which is of Opening Type                       
                            double exchangeratefortransaction = Double.parseDouble(jobj.optString("exchangeratefortransaction", "1"));
                            String basecurrency = sessionHandlerImpl.getCurrencyID(request);
                            double finalAmountReval = ReevalJournalEntryForAdvancePayment(requestObj, advanceDetailId, amountReceived, exchangeratefortransaction);
                            if (finalAmountReval != 0) {
                                /**
                                 * added transactionID and transactionModuleID
                                 * to Realised JE.ERP-41455.
                                 */
                                counterMap.put("transactionModuleid", requestObj.optBoolean("isOpeningPayment", false)?Constants.Acc_opening_Payment:Constants.Acc_Make_Payment_ModuleId);
                                counterMap.put("transactionId", requestObj.optString("paymentId"));
                                String revaljeid = PostJEFORReevaluation(requestObj, finalAmountReval, companyid, preferences, basecurrency, null, counterMap);
                                advanceDetail.setRevalJeId(revaljeid);
                            }
                        }
                        
                    /*
                        amountReceived with negative sign as we have to substract amount
                    */
                    accountingHandlerDAOobj.updateAdvanceDetailAmountDueOnAmountReceived(advanceDetailId, (-amountReceivedConverted));
                }
                }
                
                /*
                 * Receive Payment against customer - amountdue is same as paid amount. User can link this advance amount aginst invoice
                 * Receive Payment against vendor - deposite/refund amount will no be used for other transactions. so need to set amountdue as 0
                 */
                if(receipt.getCustomer()!=null) {
                    advanceDetail.setAmountDue(amountReceived);
                } else if (receipt.getVendor() != null && StringUtil.isNullOrEmpty(advanceDetail.getAdvancedetailid())) { // if document is not linked to refund receipt amount due is amount entered in reciept
                    advanceDetail.setAmountDue(amountReceived);
                } else if (receipt.getVendor() != null) {
                    advanceDetail.setAmountDue(0);
                } 
                advanceDetail.setAdvanceType(type);
                if (jobj.has("jedetail") && jobj.get("jedetail") != null) {
                    KwlReturnObject resJED = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), (String)jobj.get("jedetail"));
                    JournalEntryDetail jedObj = (JournalEntryDetail) resJED.getEntityList().get(0);
                    advanceDetail.setTotalJED(jedObj);
                }
                advanceDetails.add(advanceDetail);
                advanceDetail.setDescription(StringUtil.DecodeText(jobj.getString("description")));
                if (jobj.has("rowjedid")) {
                    advanceDetail.setROWJEDID(jobj.getString("rowjedid"));
                }
                if (jobj.has("srNoForRow")) {
                    int srNoForRow = StringUtil.isNullOrEmpty("srNoForRow") ? 0 : Integer.parseInt(jobj.getString("srNoForRow"));
                    advanceDetail.setSrNoForRow(srNoForRow);
                }
                if(jobj.has("prtaxid")){
                    String lineLevelTaxId = jobj.getString("prtaxid");
                    KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), lineLevelTaxId); 
                    Tax rowtax = (Tax) txresult.getEntityList().get(0);
                    if(rowtax!=null){
                        advanceDetail.setTax(rowtax);
                        advanceDetail.setTaxamount(jobj.optDouble("taxamount",0.0));
                    }
                }
                HashMap<String, JSONArray> jcustomarrayMap = receipt.getJcustomarrayMap();
                JSONArray jcustomarray = jcustomarrayMap.get(advanceDetail.getROWJEDID());
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                customrequestParams.put("modulerecid", advanceDetail.getROWJEDID());
                customrequestParams.put("recdetailId", advanceDetail.getId());
                customrequestParams.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    KwlReturnObject receiptAccJEDCustomData = accountingHandlerDAOobj.getObject(AccJEDetailCustomData.class.getName(), advanceDetail.getROWJEDID());
                    AccJEDetailCustomData accJEDetailCustomData = (AccJEDetailCustomData) receiptAccJEDCustomData.getEntityList().get(0);
                    KwlReturnObject receiptJED = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), advanceDetail.getROWJEDID());
                    JournalEntryDetail journalEntryDetail = (JournalEntryDetail) receiptJED.getEntityList().get(0);
                    journalEntryDetail.setAccJEDetailCustomData(accJEDetailCustomData);
                }
                
            }
            
        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return advanceDetails;
    }
    
    public Set<JournalEntryDetail> journalEntryDetailCommonObjects(HttpServletRequest request, JSONArray detailsJSONArray, JournalEntry journalEntry, Receipt receipt, int type) throws SessionExpiredException, ServiceException, AccountingException {
        double amount = 0;
        double amountIncludingBankCharges=0;
        Set jedetails = new HashSet();
        try {
            JSONObject jedjson = null;
            JournalEntryDetail jed = null;
            Account dipositTo = null;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            double bankCharges = 0;
            double bankInterest = 0;
            String bankChargesAccid = request.getParameter("bankChargesCmb");
            String bankInterestAccid = request.getParameter("bankInterestCmb");
            HashMap<String, JSONArray> Map1 = new HashMap();
            HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
            GlobalParams.put("companyid", companyid);
            GlobalParams.put("gcurrencyid", currencyid);

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            currencyid = (request.getParameter("currencyid") == null ? currency.getCurrencyID() : request.getParameter("currencyid"));

            KwlReturnObject result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), request.getParameter("pmtmethod"));
            PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);

            dipositTo = payMethod.getAccount();

            String jeid = null;
            if (journalEntry != null) {
                jeid = journalEntry.getID();
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("bankCharges")) && !StringUtil.isNullOrEmpty(bankChargesAccid)) {
                bankCharges = Double.parseDouble(request.getParameter("bankCharges"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("bankInterest")) && !StringUtil.isNullOrEmpty(bankInterestAccid)) {
                bankInterest = Double.parseDouble(request.getParameter("bankInterest"));
            }
            amount = Double.parseDouble(request.getParameter("amount"));
            amountIncludingBankCharges=amount;
            //All Fore
            if (bankCharges != 0) {
                amountIncludingBankCharges += bankCharges;
//                jedjson = new JSONObject();
//                jedjson.put("srno", jedetails.size() + 1);
//                jedjson.put("companyid", companyid);
//                (If currency USD and base currency SGD, inv rate 0.81 n pay rate 0.80 then amount = 1234.57, amountDiff = -15.24, depositAmt = 1250 SGD)
//                jedjson.put("amount", bankCharges);
//                jedjson.put("accountid", bankChargesAccid);
//                jedjson.put("debit", false);            // receipt side charges
//                jedjson.put("jeid", jeid);
//                jed = accJournalEntryobj.getJournalEntryDetails(jedjson);
//                jedetails.add(jed);
            }
            if (bankInterest != 0) {
                amountIncludingBankCharges += bankInterest;
//                jedjson = new JSONObject();
//                jedjson.put("srno", jedetails.size() + 1);
//                jedjson.put("companyid", companyid);
//                //(If currency USD and base currency SGD, inv rate 0.81 n pay rate 0.80 then amount = 1234.57, amountDiff = -15.24, depositAmt = 1250 SGD)
//                jedjson.put("amount", bankInterest);
//                jedjson.put("accountid", bankInterestAccid);
//                jedjson.put("debit", false);    // receipt side charges
//                jedjson.put("jeid", jeid);
//                jed = accJournalEntryobj.getJournalEntryDetails(jedjson);
//                jedetails.add(jed);
            }
            String transactionCurrency = receipt.getCurrency() != null ? receipt.getCurrency().getCurrencyID() : receipt.getCompany().getCurrency().getCurrencyID();
            if (amount != 0) {
                jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put("companyid", companyid);
                //(If currency USD and base currency SGD, inv rate 0.81 n pay rate 0.80 then amount = 1234.57, amountDiff = -15.24, depositAmt = 1250 SGD)
                jedjson.put("amount", amount);
                jedjson.put("accountid", dipositTo.getID());
                jedjson.put("debit", true);
                jedjson.put("jeid", jeid);
                jed = accJournalEntryobj.getJournalEntryDetails(jedjson);
                jedetails.add(jed);
                receipt.setDeposittoJEDetail(jed);
                receipt.setDepositAmount(amount);       // put amount excluding bank charges
                try{
//                    KwlReturnObject baseAmount = accCurrencyobj.getCurrencyToBaseAmount(GlobalParams, amount, transactionCurrency, journalEntry.getEntryDate(), journalEntry.getExternalCurrencyRate());
                    KwlReturnObject baseAmount = accCurrencyobj.getCurrencyToBaseAmount(GlobalParams, amount, transactionCurrency, receipt.getCreationDate(), journalEntry.getExternalCurrencyRate());
                    double depositamountinbase = (Double) baseAmount.getEntityList().get(0);
                    depositamountinbase = authHandler.round(depositamountinbase, companyid);
                    receipt.setDepositamountinbase(depositamountinbase);
                }catch(Exception ex){
                    System.out.println(ex.getMessage());
                }
            }            
        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return jedetails;
    }    
    
    public Set<JournalEntryDetail> journalEntryDetailObject(HttpServletRequest request, JSONArray detailsJSONArray, JournalEntry journalEntry, Receipt receipt, int type) throws SessionExpiredException, ServiceException, AccountingException {
        double amount = 0;
        StringBuffer billno = new StringBuffer();
        Set jedetails = new HashSet();
        try {
            Account dipositTo = null;
            double amountDiff = 0;
            boolean rateDecreased = false;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            double externalCurrencyRate = StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            String methodid = request.getParameter("pmtmethod");
            sessionHandlerImpl.updatePaymentMethodID(request, methodid);
            double balaceAmount=0.0;
            String accountIdComPreAdjRec="";

            String jeid = null;
            if (journalEntry != null) {
                jeid = journalEntry.getID();
            }
            HashMap<String, JSONArray> jcustomarrayMap = new HashMap();
            receipt.setJcustomarrayMap(jcustomarrayMap);
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            currencyid = (request.getParameter("currencyid") == null ? currency.getCurrencyID() : request.getParameter("currencyid"));

            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);

            KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);

            KwlReturnObject result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), request.getParameter("pmtmethod"));
            PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);

            dipositTo = payMethod.getAccount();

            String accountId = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("balaceAmount"))) {
                balaceAmount = Double.parseDouble(request.getParameter("balaceAmount"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("accountIdComPreAdjRec"))) {
                accountIdComPreAdjRec = request.getParameter("accountIdComPreAdjRec");
            }
            boolean isCustomer = Boolean.parseBoolean(request.getParameter("iscustomer"));
            if (!StringUtil.isNullOrEmpty(request.getParameter("accid"))) {
                if (isCustomer) {
                    KwlReturnObject resultCVAccount = accountingHandlerDAOobj.getObject(Customer.class.getName(), request.getParameter("accid"));
                    if (!resultCVAccount.getEntityList().isEmpty()) {
                        Customer customer = (Customer) resultCVAccount.getEntityList().get(0);
                        accountId = customer.getAccount().getID();
                        receipt.setPaymentWindowType(1);
                    }
                } else {
                    KwlReturnObject resultCVAccount = accountingHandlerDAOobj.getObject(Vendor.class.getName(), request.getParameter("accid"));
                    if (!resultCVAccount.getEntityList().isEmpty()) {
                        Vendor vendor = (Vendor) resultCVAccount.getEntityList().get(0);
                        accountId = vendor.getAccount().getID();
                        receipt.setPaymentWindowType(2);
                    }
                }
            } else {
                receipt.setPaymentWindowType(3);
            }

            JSONArray jArr = new JSONArray();
            if (detailsJSONArray != null) {
                jArr = detailsJSONArray;
            }
            if (jArr.length() > 0 && type == Constants.PaymentAgainstInvoice) {
                amount = 0;
                /*
                 * SDP-4352/ERP-27671 
                 * gstOutputAccountId is id of 'GST Output' account.
                 */
                String gstOutputAccountId = "";
                KwlReturnObject accountReturnObject = accAccountDAOobj.getAccountFromName(companyid, Constants.MALAYSIAN_GST_OUTPUT_TAX);
                List accountResultList = accountReturnObject.getEntityList();
                if (!accountResultList.isEmpty()) {
                    gstOutputAccountId = ((Account) accountResultList.get(0)).getID();
                }
                for (int i = 0; i < jArr.length(); i++) { // Changed for New Customer/Vendor Removed account dependency
                    JSONObject jobj = jArr.getJSONObject(i);
                    String accountIdForGst="";
                    double amountReceived = jobj.getDouble("enteramount");// amount in receipt currency
                    double amountReceivedConverted = jobj.getDouble("enteramount"); // amount in invoice currency
                    JSONArray jArray = new JSONArray();
                    jArray.put(jobj);
                    KwlReturnObject resultInvoice = accountingHandlerDAOobj.getObject(Invoice.class.getName(), jobj.getString("documentid"));
                    Invoice invoice = (Invoice) resultInvoice.getEntityList().get(0);
                    double adjustedRate=1.0;
                    if (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString()) && !StringUtil.isNullOrEmpty(invoice.getCurrency().getCurrencyID()) && !invoice.getCurrency().getCurrencyID().equals(receipt.getCurrency().getCurrencyID())) {
                        adjustedRate=jobj.optDouble("amountdue",0)/jobj.optDouble("amountDueOriginal",0);
                        amountReceivedConverted = amountReceived / adjustedRate;;
                        amountReceivedConverted = authHandler.round(amountReceivedConverted, companyid);
                    }
                    boolean isInvoiceIsClaimed=false;
                    isInvoiceIsClaimed = invoice.getBadDebtType()==Constants.Invoice_Claimed || invoice.getBadDebtType() == Constants.Invoice_Recovered;
                    double amountDiffforInv = authHandler.round(oldReceiptRowsAmount(request, jArray, currencyid, externalCurrencyRate), companyid);
                    rateDecreased = false;
                    if (amountDiffforInv < 0) {
                        rateDecreased = true;
                    }
                    amount += authHandler.round(jobj.getDouble("enteramount"), companyid);
                    if (!isInvoiceIsClaimed) {
                        JSONObject jedjson = new JSONObject();
                        jedjson.put("srno", jedetails.size() + 1);
                        jedjson.put("companyid", companyid);
                        jedjson.put("amount", authHandler.round((jobj.getDouble("enteramount") - amountDiffforInv), companyid));
                        jedjson.put("accountid", invoice.getAccount() != null ? invoice.getAccount().getID() : accountId);
                        if (jobj.optDouble("gstCurrencyRate", 0.0) != 0.0) {
                            jedjson.put("gstCurrencyRate", jobj.optDouble("gstCurrencyRate", 0.0));
                            journalEntry.setGstCurrencyRate(jobj.optDouble("gstCurrencyRate", 0.0));
                            jedjson.put("paymentType", type);
                        }
                        jedjson.put("forexGainLoss", amountDiffforInv);
                        jedjson.put("debit", false);
                        jedjson.put("jeid", jeid);
                        jedjson.put("description", jobj.getString("description"));
//                        JournalEntryDetail jed = accJournalEntryobj.getJournalEntryDetails(jedjson);
//                        jedetails.add(jed);
                        KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jedetails.add(jed);
//                        accJournalEntryobj.saveJournalEntryDetailsSet(jedetails);

                        if ((!jobj.getString("documentno").equalsIgnoreCase("undefined")) && (!jobj.getString("documentno").equalsIgnoreCase(""))) {
                            billno.append(jobj.getString("documentno") + ",");
                        }
                        jArr.getJSONObject(i).put("rowjedid", jed.getID());

                        if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", "[]"))) {
                            JSONArray jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
                            jcustomarrayMap.put(jed.getID(), jcustomarray);
                        }
                        jArr.getJSONObject(i).put("jedetail", jed.getID());
                    } else {   
                        // Logic of posting JE for claimed invoices
                        String badDebtRecoveredAccountId = extraCompanyPreferences.getGstBadDebtsRecoverAccount();
                        KwlReturnObject accObj = accountingHandlerDAOobj.getObject(Account.class.getName(), badDebtRecoveredAccountId);
                        Account account = (Account) accObj.getEntityList().get(0);
                        if (account == null) {
                            throw new AccountingException("GST Bad Debt Recover Account is not available in database");
                        }
                        double amountPaidForInvoice = jobj.getDouble("enteramount") - amountDiffforInv;
                        boolean isGlobalLevelTax = false;
                        boolean isOpeningBalanceInvoice= invoice.isIsOpeningBalenceInvoice();
                        if (isOpeningBalanceInvoice) {
                            isGlobalLevelTax = true;
                        } else {
                            if (invoice.getTaxEntry() != null && invoice.getTaxEntry().getAmount() > 0) {
                                isGlobalLevelTax = true;
                            }
                        }
                        double taxAmountInInvoiceCurrency = 0d;
                        double taxAmountReceivedInPaymentCurrency = 0d;
                        double taxAmountReceivedInInvoiceCurrency = 0d;
                        double totalTaxAmountReceivedInInvoiceCurrency = 0d;
                        double invoiceTotalAmountInInvoiceCurrency =authHandler.round(isOpeningBalanceInvoice?invoice.getOriginalOpeningBalanceAmount():invoice.getCustomerEntry().getAmount(), companyid);
                        double invoiceAmountExcludingTaxInPaymentCurrency = 0.0;
                        double invoiceAmountReceivedExcludingTaxInInvoiceCurrency = 0.0;
                        amountPaidForInvoice = authHandler.round(amountPaidForInvoice, companyid);
                        if(isGlobalLevelTax){
                            Tax gloabLevelTax = invoice.getTax();
                            if(invoice.isIsOpeningBalenceInvoice()){
                                accountIdForGst = gstOutputAccountId;
                            } else {
                                accountIdForGst = gloabLevelTax.getAccount().getID();
                            }
                            taxAmountInInvoiceCurrency = authHandler.round(isOpeningBalanceInvoice?invoice.getTaxamount():invoice.getTaxEntry().getAmount(), companyid);
                            taxAmountReceivedInPaymentCurrency = (taxAmountInInvoiceCurrency * amountPaidForInvoice)/invoiceTotalAmountInInvoiceCurrency;
                            taxAmountReceivedInPaymentCurrency = authHandler.round(taxAmountReceivedInPaymentCurrency, companyid);
                            taxAmountReceivedInInvoiceCurrency = (taxAmountInInvoiceCurrency*amountReceivedConverted)/invoiceTotalAmountInInvoiceCurrency;
                            taxAmountReceivedInInvoiceCurrency = authHandler.round(taxAmountReceivedInInvoiceCurrency, companyid);
                            totalTaxAmountReceivedInInvoiceCurrency += taxAmountReceivedInInvoiceCurrency;
                            invoiceAmountExcludingTaxInPaymentCurrency = amountPaidForInvoice - taxAmountReceivedInPaymentCurrency;
                            
                            JSONObject jedjson = new JSONObject();
                            jedjson.put("srno", jedetails.size() + 1);
                            jedjson.put("companyid", companyid);
                            jedjson.put("accountid", badDebtRecoveredAccountId);
                            jedjson.put("amount", invoiceAmountExcludingTaxInPaymentCurrency);
                            jedjson.put("debit", false);
                            jedjson.put("jeid", jeid);
                            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jedetails.add(jed);
                            
                            jedjson = new JSONObject();
                            jedjson.put("srno", jedetails.size() + 1);
                            jedjson.put("companyid", companyid);
                            jedjson.put("accountid", accountIdForGst);
                            jedjson.put("amount", taxAmountReceivedInPaymentCurrency);
                            jedjson.put("debit", false);
                            jedjson.put("jeid", jeid);
                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            JournalEntryDetail jedForRecoveryJe = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jedetails.add(jedForRecoveryJe);
                            
//                            accJournalEntryobj.saveJournalEntryDetailsSet(jedetails);
                            
                            jArr.getJSONObject(i).put("rowjedid", jedForRecoveryJe.getID());
                            if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", "[]"))) {
                                JSONArray jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
                                jcustomarrayMap.put(jedForRecoveryJe.getID(), jcustomarray);
                            }
                            jArr.getJSONObject(i).put("jedetail", jedForRecoveryJe.getID());
                            
                        } else {
                            JSONObject jedjson = new JSONObject();
                            invoiceAmountExcludingTaxInPaymentCurrency = amountPaidForInvoice;
                            for (InvoiceDetail details : invoice.getRows()) {
                                taxAmountInInvoiceCurrency = details.getRowTaxAmount()+details.getRowTermTaxAmount();
                                taxAmountReceivedInPaymentCurrency = (taxAmountInInvoiceCurrency * amountPaidForInvoice)/invoiceTotalAmountInInvoiceCurrency;
                                taxAmountReceivedInPaymentCurrency = authHandler.round(taxAmountReceivedInPaymentCurrency, companyid);
                                taxAmountReceivedInInvoiceCurrency = (taxAmountInInvoiceCurrency*amountReceivedConverted)/invoiceTotalAmountInInvoiceCurrency;
                                taxAmountReceivedInInvoiceCurrency = authHandler.round(taxAmountReceivedInInvoiceCurrency, companyid);
                                totalTaxAmountReceivedInInvoiceCurrency += taxAmountReceivedInInvoiceCurrency;
                                invoiceAmountExcludingTaxInPaymentCurrency -= taxAmountReceivedInPaymentCurrency;
                                                                
                                jedjson = new JSONObject();
                                jedjson.put("srno", jedetails.size() + 1);
                                jedjson.put("companyid", companyid);
                                jedjson.put("accountid", details.getTax().getAccount().getID());
                                jedjson.put("amount", taxAmountReceivedInPaymentCurrency);
                                jedjson.put("debit", false);
                                jedjson.put("jeid", jeid);
                                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                jedetails.add(jed);
                            }
                            jedjson = new JSONObject();
                            jedjson.put("srno", jedetails.size() + 1);
                            jedjson.put("companyid", companyid);
                            jedjson.put("accountid", badDebtRecoveredAccountId);
                            jedjson.put("amount", invoiceAmountExcludingTaxInPaymentCurrency);
                            jedjson.put("debit", false);
                            jedjson.put("jeid", jeid);
                            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            JournalEntryDetail jedForRecoveryJe = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jedetails.add(jedForRecoveryJe);
                            
//                            accJournalEntryobj.saveJournalEntryDetailsSet(jedetails);
                            jArr.getJSONObject(i).put("rowjedid", jedForRecoveryJe.getID());
                            if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", "[]"))) {
                                JSONArray jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
                                jcustomarrayMap.put(jedForRecoveryJe.getID(), jcustomarray);
                            }
                            jArr.getJSONObject(i).put("jedetail", jedForRecoveryJe.getID());
                        }
                        invoiceAmountReceivedExcludingTaxInInvoiceCurrency = amountReceivedConverted - totalTaxAmountReceivedInInvoiceCurrency;
                        HashMap<String, Object> mappingObj = new HashMap<String, Object>();
                        mappingObj.put("companyId", companyid);
                        mappingObj.put("invoiceId", invoice.getID());
                        mappingObj.put("invoiceReceivedAmt", invoiceAmountReceivedExcludingTaxInInvoiceCurrency);
//                        mappingObj.put("recoveredDate", receipt.getJournalEntry().getEntryDate());
                        mappingObj.put("recoveredDate", receipt.getCreationDate());
                        mappingObj.put("gstToRecover", totalTaxAmountReceivedInInvoiceCurrency);
                        mappingObj.put("badDebtType", 1);
                        mappingObj.put("receiptid", receipt.getID());
                        KwlReturnObject mapResult = accInvoiceDAOObj.saveBadDebtInvoiceMapping(mappingObj);
                    }

                }
                amountDiff = authHandler.round(oldReceiptRowsAmount(request, jArr, currencyid, externalCurrencyRate), companyid);

                if (preferences.getForeignexchange() == null) {
                    throw new AccountingException(messageSource.getMessage("acc.receipt.forex", null, RequestContextUtils.getLocale(request)));
                }
                if (amountDiff != 0 && preferences.getForeignexchange() != null && Math.abs(amountDiff) >= 0.000001) {//Math.abs(amountDiff) < .0000001 Added this because in case of revaluation the diff in the rates was less than .0000001 and the amount calculation was going in Exachange rate account. 
                    if (amountDiff < 0) {
                        rateDecreased = true;
                    }
                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", rateDecreased ? (-1 * amountDiff) : amountDiff);
                    jedjson.put("accountid", preferences.getForeignexchange().getID());
                    if(journalEntry.getGstCurrencyRate()!=0.0){
                        jedjson.put("forexGainLoss",rateDecreased ? (-1 * amountDiff) : amountDiff);
                        jedjson.put("paymentType",type);
                    }
                    jedjson.put("debit", rateDecreased ? true : false);
                    jedjson.put("jeid", jeid);
                    JournalEntryDetail jed = accJournalEntryobj.getJournalEntryDetails(jedjson);
                    jedetails.add(jed);
//                    accJournalEntryobj.saveJournalEntryDetailsSet(jedetails);
                }
            } else {
                amount = authHandler.round(Double.parseDouble(request.getParameter("amount")), companyid);
            }

            if(jArr.length() > 0 && type == Constants.PaymentAgainstLoanDisbursement){
                double amountDifference=0.0;
                double enteredAmount=0.0;
                double principalAmount=0.0;
                double interestAmount=0.0;
                double interestAmountInPaymentCurrency=0.0;
                double principalInPaymentCurrency=0.0;
                String transactionCurrencyId="";
                for (int i = 0; i < jArr.length(); i++) { // Changed for New Customer/Vendor Removed account dependency
                    JSONObject jobj = jArr.getJSONObject(i);
                    transactionCurrencyId = jobj.getString("currencyidtransaction");
                    JSONArray jArray = new JSONArray();
                    jArray.put(jobj);
                    KwlReturnObject resultRepaymentDetails = accountingHandlerDAOobj.getObject(RepaymentDetails.class.getName(), jobj.getString("repaymentscheduleid"));
                    RepaymentDetails RD = (RepaymentDetails) resultRepaymentDetails.getEntityList().get(0);
                    enteredAmount = jobj.getDouble("enteramount");
                    interestAmount = RD.getInterest();
                    principalAmount = RD.getPrinciple();
                    interestAmountInPaymentCurrency = (interestAmount*enteredAmount)/(principalAmount+interestAmount);
                    principalInPaymentCurrency = enteredAmount - interestAmountInPaymentCurrency;
                    jobj.put("principalInPaymentCurrency", principalInPaymentCurrency);
                    amount += enteredAmount;
                    if(!StringUtil.isNullOrEmpty(transactionCurrencyId) && !StringUtil.isNullOrEmpty(currencyid)){
                        if(transactionCurrencyId.equals(currencyid)){
                            amountDifference = LoanRepaymentForexGailLossAmountSameCurrency(request, jobj, receipt, transactionCurrencyId, currencyid, externalCurrencyRate);
                        } else {
                            amountDifference = LoanRepaymentForexGailLossAmount(request, jobj, receipt, transactionCurrencyId, currencyid, externalCurrencyRate);
                        }
                    }
                                        
                    rateDecreased = false;
                    if (amountDifference < 0) {
                        rateDecreased = true;
                    }
                    
                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", principalInPaymentCurrency - amountDifference);
                    jedjson.put("accountid", RD.getDisbursement().getDebitaccount()!=null?RD.getDisbursement().getDebitaccount().getID():accountId);                  
                    if(jobj.optDouble("gstCurrencyRate",0.0)!=0.0){
                        jedjson.put("gstCurrencyRate",jobj.optDouble("gstCurrencyRate",0.0));
                        journalEntry.setGstCurrencyRate(jobj.optDouble("gstCurrencyRate",0.0));
                        jedjson.put("paymentType",type);
                    }
                    jedjson.put("forexGainLoss",amountDifference);
                    jedjson.put("debit", false);
                    jedjson.put("jeid", jeid);
                    jedjson.put("description", jobj.getString("description"));
//                    JournalEntryDetail jed = accJournalEntryobj.getJournalEntryDetails(jedjson);
//                      jedetails.add(jed);
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
//                    accJournalEntryobj.saveJournalEntryDetailsSet(jedetails);
                    
                    JSONObject jedForInterestAmount = new JSONObject();
                    jedForInterestAmount.put("srno", jedetails.size() + 1);
                    jedForInterestAmount.put("companyid", companyid);
                    jedForInterestAmount.put("amount", interestAmountInPaymentCurrency);
                    jedForInterestAmount.put("accountid", !StringUtil.isNullOrEmpty(extraCompanyPreferences.getLoanInterestAccount())?extraCompanyPreferences.getLoanInterestAccount():accountId);                  
                    jedForInterestAmount.put("debit", false);
                    jedForInterestAmount.put("jeid", jeid);
                    jedForInterestAmount.put("description", "Interest Amount");
                    JournalEntryDetail jedForInterest = accJournalEntryobj.getJournalEntryDetails(jedForInterestAmount);
                    jedetails.add(jedForInterest);
//                    accJournalEntryobj.saveJournalEntryDetailsSet(jedetails);
                    if ((!jobj.getString("documentno").equalsIgnoreCase("undefined")) && (!jobj.getString("documentno").equalsIgnoreCase(""))) {
                        billno.append(jobj.getString("documentno") + ",");
                    }
                    jArr.getJSONObject(i).put("rowjedid", jed.getID());

                    if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", "[]"))) {
                        JSONArray jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
                        jcustomarrayMap.put(jed.getID(), jcustomarray);
                    }
                    jArr.getJSONObject(i).put("jedetail", jed.getID());
                    if (preferences.getForeignexchange() == null) {
                    throw new AccountingException(messageSource.getMessage("acc.receipt.forex", null, RequestContextUtils.getLocale(request)));
                }
                if (amountDifference != 0 && preferences.getForeignexchange() != null && Math.abs(amountDifference) >= 0.000001) {//Math.abs(amountDiff) < .0000001 Added this because in case of revaluation the diff in the rates was less than .0000001 and the amount calculation was going in Exachange rate account. 
                    if (amountDifference < 0) {
                        rateDecreased = true;
                    }
                    JSONObject jedjsonnew = new JSONObject();
                    jedjsonnew.put("srno", jedetails.size() + 1);
                    jedjsonnew.put("companyid", companyid);
                    jedjsonnew.put("amount", rateDecreased ? (-1 * amountDifference) : amountDifference);
                    jedjsonnew.put("accountid", preferences.getForeignexchange().getID());
                    if(journalEntry.getGstCurrencyRate()!=0.0){
                        jedjsonnew.put("forexGainLoss",rateDecreased ? (-1 * amountDifference) : amountDifference);
                        jedjsonnew.put("paymentType",type);
                    }
                    jedjsonnew.put("debit", rateDecreased ? true : false);
                    jedjsonnew.put("jeid", jeid);
                    JournalEntryDetail jednew = accJournalEntryobj.getJournalEntryDetails(jedjsonnew);
                    jedetails.add(jednew);
//                    accJournalEntryobj.saveJournalEntryDetailsSet(jedetails);
                }
                }                
            }
            JSONObject jedjson = null;
            KwlReturnObject jedresult = null;
            JournalEntryDetail jed = null;
            JournalEntryDetail JEdeatilId = null;
            List receiptOtherwiseList = new ArrayList();
            HashMap receiptdetailotherwise = new HashMap();

            if (type == Constants.AdvancePayment || type == Constants.GLPayment || type == Constants.PaymentAgainstCNDN || type == Constants.LocalAdvanceTypePayment || type == Constants.ExportAdvanceTypePayment) {//advance,GL,Cn
                JSONArray drAccArr = detailsJSONArray;
                for (int i = 0; i < drAccArr.length(); i++) {
                    JSONObject jobj = drAccArr.getJSONObject(i);
                    boolean isdebit = jobj.has("isdebit") ? Boolean.parseBoolean(jobj.getString("isdebit")) : false;
                    if (type == Constants.GLPayment) {
                        isdebit = jobj.has("debit") ? Boolean.parseBoolean(jobj.getString("debit")) : false;
                    }
                    double forexgainloss = 0;
                    if (type == Constants.PaymentAgainstCNDN) {
                        String transactionCurrencyId = jobj.getString("currencyidtransaction");
                        if (!StringUtil.isNullOrEmpty(transactionCurrencyId) && !transactionCurrencyId.equals(receipt.getCurrency().getCurrencyID())) {
                            forexgainloss = cndnReceiptForexGailLossAmount(request, jobj, receipt, transactionCurrencyId, currencyid, externalCurrencyRate);
                        } else if (transactionCurrencyId.equals(receipt.getCurrency().getCurrencyID())) {
                            forexgainloss = cndnReceiptForexGailLossAmountForSameCurrency(request, jobj, receipt, transactionCurrencyId, currencyid, externalCurrencyRate);
                        }
                    }
                    if(type == Constants.AdvancePayment && receipt.getVendor()!=null && !StringUtil.isNullOrEmpty(jobj.getString("documentid"))){   // Refund type payment
                        String transactionCurrencyId = jobj.getString("currencyidtransaction");
                        JSONObject requestObj = convertRequestToJSONObjectForLinkPayment(request);
                        forexgainloss = RefundReceiptForexGailLossAmount(requestObj, jobj, receipt, transactionCurrencyId, currencyid, externalCurrencyRate);
                    }
            //        check the logic implemented after words from atul 
                    
                    //**********************************Start***********************
                    if(type==Constants.AdvancePayment && (Integer.parseInt(company.getCountry().getID()) == Constants.malaysian_country_id) && Boolean.parseBoolean(request.getParameter("iscustomer"))  && (!StringUtil.isNullOrEmpty(jobj.getString("documentid").toString()))   ){ //if advance payment done against customer for Malaysia Country
                        double totalAmount=Double.parseDouble(jobj.getString("enteramount"));
                        double typeAmountTax=0;
                        String accountID="";
                        
                        /*
                        if(type == Constants.LocalAdvanceTypePayment){//for Local Tax
//                            double percentage=6.0;
                             KwlReturnObject ObjReturnObject = accAccountDAOobj.getAccountFromName(companyid, Constants.MALASIAN_GST_SR_TAX_CODE);
//                             KwlReturnObject ObjReturnObject = accAccountDAOobj.getAccountFromName(companyid, "SR");
                             Account account =(Account)ObjReturnObject.getEntityList().get(0);
                             
                             KwlReturnObject taxObj = accAccountDAOobj.getTaxFromCode(companyid, Constants.MALASIAN_GST_SR_TAX_CODE);
                             Tax tax = (Tax) taxObj.getEntityList().get(0);
                             
                             KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, df.parse(request.getParameter("creationdate")), tax.getID());
                             double percentage = (Double) perresult.getEntityList().get(0);
                             
                             if(account!=null)accountID=account.getID();
                              typeAmountTax=(totalAmount*percentage)/(100+percentage);
                              typeAmountTax=authHandler.roundUnitPrice(typeAmountTax);
                              
                              if(tax != null){
                                  receipt.setTax(tax);
                                  receipt.setTaxAmount(typeAmountTax);
                              }
                        }else if(type == Constants.ExportAdvanceTypePayment){//For Export Tax
                            KwlReturnObject ObjReturnObject = accAccountDAOobj.getAccountFromName(companyid, Constants.MALASIAN_GST_ZRE_TAX_CODE);
//                            KwlReturnObject ObjReturnObject = accAccountDAOobj.getAccountFromName(companyid, "ZRE");
                            Account account =(Account)ObjReturnObject.getEntityList().get(0);
                            if(account!=null)accountID=account.getID();
                            typeAmountTax=0;
                            typeAmountTax=authHandler.roundUnitPrice(typeAmountTax);
                            
                            KwlReturnObject taxObj = accAccountDAOobj.getTaxFromCode(companyid, Constants.MALASIAN_GST_ZRE_TAX_CODE);
                            Tax tax = (Tax) taxObj.getEntityList().get(0);

                            if(tax != null){
                                receipt.setTax(tax);
                                receipt.setTaxAmount(typeAmountTax);
                            }
                        }
                        */
                        
                        String taxid = jobj.getString("documentid");
                        KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), taxid);
                        Tax taxObject = (Tax) txresult.getEntityList().get(0);
                        Account account = taxObject.getAccount();
                        if (account != null) {
                             accountID = account.getID();
                        }
//                        typeAmountTax = (totalAmount * percentage) / (100 + percentage);
//                        typeAmountTax = authHandler.roundUnitPrice(typeAmountTax);
                        typeAmountTax = jobj.optDouble("taxamount",0.0);   // Above code aommented as tax amount is editable.
                        if (taxObject != null) {
                              receipt.setTax(taxObject);
                              receipt.setTaxAmount(typeAmountTax);
                         }
                            
                        jedjson = new JSONObject();
                        jedjson.put("srno", jedetails.size() + 1);
                        jedjson.put("companyid", companyid);
                        jedjson.put("amount",totalAmount-typeAmountTax);
                        jedjson.put("accountid", accountId);
                        jedjson.put("debit", isdebit);//false);
                        jedjson.put("jeid", jeid);
                        jedjson.put("description",jobj.getString("description"));
                        jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        JEdeatilId = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jedetails.add(JEdeatilId);
                        if(typeAmountTax!=0){         // If taxamount is zero then je details will not be saved
                            jedjson = new JSONObject();
                            jedjson.put("srno", jedetails.size() + 1);
                            jedjson.put("companyid", companyid);
                            jedjson.put("amount", typeAmountTax);
                            jedjson.put("accountid", !StringUtil.isNullOrEmpty(accountID) ? accountID : jobj.getString("accountid"));
                            jedjson.put("debit", isdebit);//false);
                            jedjson.put("jeid", jeid);
                            jedjson.put("description", jobj.getString("description"));
                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            JEdeatilId = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jedetails.add(JEdeatilId);
                        }
                    }else{//Normal Flow

                        jedjson = new JSONObject();
                        jedjson.put("srno", jedetails.size() + 1);
                        jedjson.put("companyid", companyid);
                        jedjson.put("amount", Double.parseDouble(jobj.getString("enteramount"))- forexgainloss);
                        if (type == Constants.GLPayment) {
                            jedjson.put("accountid", jobj.getString("documentid"));//Changed account Id 
                        } else if (type == Constants.AdvancePayment) {
                            jedjson.put("accountid", accountId);//Changed account Id
                        } else if (type == Constants.PaymentAgainstCNDN) {
                            KwlReturnObject debitNoteResult = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), jobj.getString("documentid"));
                            DebitNote debitMemo = (DebitNote) debitNoteResult.getEntityList().get(0);
                            jedjson.put("accountid", debitMemo.getAccount() != null ? debitMemo.getAccount().getID() : accountId);//Changed account Id
                        }
                        if(type == Constants.PaymentAgainstCNDN&&jobj.optDouble("gstCurrencyRate",0.0)!=0.0){
                            jedjson.put("gstCurrencyRate",jobj.optDouble("gstCurrencyRate",0.0));
                            journalEntry.setGstCurrencyRate(jobj.optDouble("gstCurrencyRate",0.0));
                            jedjson.put("paymentType",type);
                            jedjson.put("forexGainLoss",forexgainloss);
                        }
                        jedjson.put("debit", isdebit);//false);
                        jedjson.put("jeid", jeid);
                        jedjson.put("description", jobj.getString("description"));
                        jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        JEdeatilId = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jedetails.add(JEdeatilId);
                    }
                    if ((!jobj.getString("documentno").equalsIgnoreCase("undefined")) && (!jobj.getString("documentno").equalsIgnoreCase(""))) {
                        billno.append(jobj.getString("documentno") + ",");
                    }
                    drAccArr.getJSONObject(i).put("rowjedid", JEdeatilId.getID());
                    if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", ""))) {
                        JSONArray jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
                        jcustomarrayMap.put(JEdeatilId.getID(), jcustomarray);
                    }
                    drAccArr.getJSONObject(i).put("jedetail", JEdeatilId.getID());
                    if (preferences.getForeignexchange() == null) {
                        throw new AccountingException(messageSource.getMessage("acc.common.forex", null, RequestContextUtils.getLocale(request)));
                    }
                    if (forexgainloss != 0 && preferences.getForeignexchange() != null && Math.abs(forexgainloss) >= 0.000001) {//Math.abs(forexgainloss) < .0000001 Added this because in case of revaluation the diff in the rates was less than .0000001 and the amount calculation was going in Exachange rate account. 
                        rateDecreased = false;
                        if (forexgainloss < 0) {
                            rateDecreased = true;
                        }
                        jedjson.put("srno", jedetails.size() + 1);
                        jedjson.put("companyid", companyid);
                        jedjson.put("amount", rateDecreased ? (-1 * forexgainloss) : forexgainloss);
                        if (type == Constants.PaymentAgainstCNDN&&journalEntry.getGstCurrencyRate() != 0.0) {
                            jedjson.put("forexGainLoss", rateDecreased ? (-1 * forexgainloss) : forexgainloss);
                            jedjson.put("paymentType", type);
                        }
                        jedjson.put("accountid", preferences.getForeignexchange().getID());
                        jedjson.put("debit", rateDecreased ? true : false);
                        jedjson.put("jeid", jeid);
                        jed = accJournalEntryobj.getJournalEntryDetails(jedjson);
                        jedetails.add(jed);
//                        accJournalEntryobj.saveJournalEntryDetailsSet(jedetails);
                    }
                    
                    // Logic for saving line level details of Receipt against GL code.
                    double rowtaxamount = 0;
                    if (type == Constants.GLPayment) {//Otherwise for receive Payment
                        ReceiptDetailOtherwise receiptDetailOtherwise = null;
                        String rowtaxid = jobj.getString("prtaxid");
                        KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), rowtaxid); // (Tax)session.get(Tax.class, taxid);
                        Tax rowtax = (Tax) txresult.getEntityList().get(0);
                        String appliedGst = jobj.optString("appliedGst","");
                        KwlReturnObject gstresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), appliedGst); 
                        
                        if (rowtax == null || rowtaxid.equalsIgnoreCase("-1")) {
                            receiptdetailotherwise.put("amount", Double.parseDouble(jobj.getString("enteramount")));
                            receiptdetailotherwise.put("taxjedid", "");
                            receiptdetailotherwise.put("tax", rowtaxid.equalsIgnoreCase("-1") ? "None" : "");
                            receiptdetailotherwise.put("accountid", jobj.getString("documentid"));
                            receiptdetailotherwise.put("isdebit", isdebit);
                            receiptdetailotherwise.put("taxamount", rowtaxamount);
                            receiptdetailotherwise.put("description",jobj.getString("description"));
                             receiptdetailotherwise.put("receipt", receipt.getID());
                            if (jobj.has("srNoForRow")) {
                                int srNoForRow = StringUtil.isNullOrEmpty("srNoForRow") ? 0 : Integer.parseInt(jobj.getString("srNoForRow"));
                                receiptdetailotherwise.put("srNoForRow", srNoForRow);
                            }
                            if(jobj.has("jedetail") && jobj.get("jedetail")!=null){
                                receiptdetailotherwise.put("jedetail", (String)jobj.get("jedetail"));
                            }else{
                                receiptdetailotherwise.put("jedetail", "");
                            }
                            if (gstresult.getEntityList().get(0)!=null) {
                                Tax gstAppliedObj = (Tax) gstresult.getEntityList().get(0);
                                if (gstAppliedObj != null) {
                                    receiptdetailotherwise.put("gstApplied", gstAppliedObj);
                                }
                            }
                            result = accReceiptDAOobj.saveReceiptDetailOtherwise(receiptdetailotherwise);
                            receiptdetailotherwise.clear();
                            receiptDetailOtherwise = (ReceiptDetailOtherwise) result.getEntityList().get(0);
                            receiptOtherwiseList.add(receiptDetailOtherwise.getID());
                        } else {
                            rowtaxamount = Double.parseDouble(jobj.getString("taxamount"));
                            jedjson = new JSONObject();
                            jedjson.put("srno", jedetails.size() + 1);
                            jedjson.put("companyid", company.getCompanyID());
                            jedjson.put("amount", authHandler.formattedAmount(rowtaxamount, companyid));
                            jedjson.put("accountid", rowtax.getAccount().getID());
                            jedjson.put("debit", isdebit);//false);
                            jedjson.put("jeid", jeid);
                            jedjson.put("description", jobj.getString("description"));
                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jedetails.add(jed);
//                            accJournalEntryobj.saveJournalEntryDetailsSet(jedetails);

                            receiptdetailotherwise.put("amount", Double.parseDouble(jobj.getString("enteramount")));
                            receiptdetailotherwise.put("taxjedid", jed.getID());
                            receiptdetailotherwise.put("tax", rowtax.getID());
                            receiptdetailotherwise.put("accountid", jobj.getString("documentid"));
                            receiptdetailotherwise.put("isdebit", isdebit);
                            receiptdetailotherwise.put("taxamount", rowtaxamount);
                            receiptdetailotherwise.put("receipt", receipt.getID());
                            receiptdetailotherwise.put("description", jobj.getString("description"));
                            if (jobj.has("srNoForRow")) {
                                int srNoForRow = StringUtil.isNullOrEmpty("srNoForRow") ? 0 : Integer.parseInt(jobj.getString("srNoForRow"));
                                receiptdetailotherwise.put("srNoForRow", srNoForRow);
                            }
                            Tax gstAppliedObj=null;
                            if (gstresult.getEntityList().get(0) != null) {
                                gstAppliedObj = (Tax) gstresult.getEntityList().get(0);
                            }    
                            if (gstAppliedObj != null) {
                                    receiptdetailotherwise.put("gstApplied", gstAppliedObj);
                            } else {
                                    receiptdetailotherwise.put("gstApplied", rowtax);
                            }
                            if(jobj.has("jedetail") && jobj.get("jedetail")!=null){
                                receiptdetailotherwise.put("jedetail", (String)jobj.get("jedetail"));
                            }else{
                                receiptdetailotherwise.put("jedetail", "");
                            }
                            receiptdetailotherwise.put("taxjedetail", jed.getID());
                            result = accReceiptDAOobj.saveReceiptDetailOtherwise(receiptdetailotherwise);
                            receiptdetailotherwise.clear();
                            receiptDetailOtherwise = (ReceiptDetailOtherwise) result.getEntityList().get(0);
                            receiptOtherwiseList.add(receiptDetailOtherwise.getID());
                            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                            if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", ""))) {
                                JSONArray jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
                                customrequestParams.put("customarray", jcustomarray);
                                customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                                customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                                customrequestParams.put("modulerecid", jed.getID());
                                customrequestParams.put("recdetailId", receiptDetailOtherwise.getID());
                                customrequestParams.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
                                customrequestParams.put("companyid", companyid);
                                customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                    KwlReturnObject receiptAccJECustomData = accountingHandlerDAOobj.getObject(AccJEDetailCustomData.class.getName(), jed.getID());
                                    AccJEDetailCustomData accJEDetailCustomData = (AccJEDetailCustomData) receiptAccJECustomData.getEntityList().get(0);
                                    jed.setAccJEDetailCustomData(accJEDetailCustomData);
                                }
                            }
                        }

                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", ""))) {
                            JSONArray jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
                            customrequestParams.put("customarray", jcustomarray);
                            customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                            customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                            customrequestParams.put("modulerecid", JEdeatilId.getID());
                            customrequestParams.put("recdetailId", receiptDetailOtherwise.getID());
                            customrequestParams.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
                            customrequestParams.put("companyid", companyid);
                            customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                KwlReturnObject receiptAccJECustomData = accountingHandlerDAOobj.getObject(AccJEDetailCustomData.class.getName(), JEdeatilId.getID());
                                AccJEDetailCustomData accJEDetailCustomData = (AccJEDetailCustomData) receiptAccJECustomData.getEntityList().get(0);
                                JEdeatilId.setAccJEDetailCustomData(accJEDetailCustomData);
                            }
                        }
                    }
                }
            }
            
            /*
             * If balace Amount is Greater than Zero then balace amount is save in respective selected account in company preferance. 
             */
             if (type == Constants.BALACEAMOUNT) {
                jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put("companyid", company.getCompanyID());
                jedjson.put("amount", balaceAmount);
                jedjson.put("accountid", accountIdComPreAdjRec);
                jedjson.put("debit", false);
                jedjson.put("jeid", jeid);
                jedjson.put("description", "");
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jedetails.add(jed);
//                accJournalEntryobj.saveJournalEntryDetailsSet(jedetails);
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return jedetails;
    }
    
    public Set<JournalEntryDetail> journalEntryDetailCommonObjectsForBankCharges(HttpServletRequest request,JournalEntry journalEntry, Receipt receipt, boolean bankCharge) throws SessionExpiredException, ServiceException, AccountingException {
        double amount = 0;
        Set jedetails = new HashSet();
        try {
            KwlReturnObject jedresult = null;
            JSONObject jedjson = null;
            JournalEntryDetail jed = null;
            JournalEntryDetail JEdeatilId = null;
            Account dipositTo = null;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            double bankCharges = 0;
            double bankInterest = 0;
            String bankChargesAccid = request.getParameter("bankChargesCmb");
            String bankInterestAccid = request.getParameter("bankInterestCmb");
            KwlReturnObject result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), request.getParameter("pmtmethod"));
            PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);

            dipositTo = payMethod.getAccount();

            String jeid = null;
            if (journalEntry != null) {
                jeid = journalEntry.getID();
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("bankCharges")) && !StringUtil.isNullOrEmpty(bankChargesAccid)) {
                bankCharges = Double.parseDouble(request.getParameter("bankCharges"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("bankInterest")) && !StringUtil.isNullOrEmpty(bankInterestAccid)) {
                bankInterest = Double.parseDouble(request.getParameter("bankInterest"));
            }
           // amount = Double.parseDouble(request.getParameter("amount"));
            //All Fore
            if (bankCharge && bankCharges != 0) {
                amount += bankCharges;
                jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put("companyid", companyid);
                //(If currency USD and base currency SGD, inv rate 0.81 n pay rate 0.80 then amount = 1234.57, amountDiff = -15.24, depositAmt = 1250 SGD)
                jedjson.put("amount", bankCharges);
                jedjson.put("accountid", bankChargesAccid);
                jedjson.put("debit", true);            // receipt side charges
                jedjson.put("jeid", jeid);

                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                JEdeatilId = (JournalEntryDetail) jedresult.getEntityList().get(0);
//                        jedetails.add(JEdeatilId);
                jedjson.put("jedid", JEdeatilId.getID());
                jed = accJournalEntryobj.getJournalEntryDetails(jedjson);
                jedetails.add(jed);
            }
            if (!bankCharge && bankInterest != 0) {
                amount += bankInterest;
                jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put("companyid", companyid);
                //(If currency USD and base currency SGD, inv rate 0.81 n pay rate 0.80 then amount = 1234.57, amountDiff = -15.24, depositAmt = 1250 SGD)
                jedjson.put("amount", bankInterest);
                jedjson.put("accountid", bankInterestAccid);
                jedjson.put("debit", true);    // receipt side charges
                jedjson.put("jeid", jeid);
                jed = accJournalEntryobj.getJournalEntryDetails(jedjson);
                jedetails.add(jed);
            }
            if (amount != 0) {
                jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put("companyid", companyid);
                //(If currency USD and base currency SGD, inv rate 0.81 n pay rate 0.80 then amount = 1234.57, amountDiff = -15.24, depositAmt = 1250 SGD)
                jedjson.put("amount", amount);
                jedjson.put("accountid", dipositTo.getID());
                jedjson.put("debit", false);
                jedjson.put("jeid", jeid);
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                JEdeatilId = (JournalEntryDetail) jedresult.getEntityList().get(0);
                // jedetails.add(JEdeatilId);
                jedjson.put("jedid", JEdeatilId.getID());
                jed = accJournalEntryobj.getJournalEntryDetails(jedjson);
                jedetails.add(jed);
            //    receipt.setDeposittoJEDetail(jed);
                //    receipt.setDepositAmount(amount);
            }            
        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return jedetails;
    } 
    
    public double cndnReceiptForexGailLossAmount(HttpServletRequest request,JSONObject jobj, Receipt receipt ,String transactionCurrencyId, String currencyid, double externalCurrencyRate) throws ServiceException, SessionExpiredException {
        double amount = 0, actualAmount = 0;
        String companyid = sessionHandlerImpl.getCompanyid(request);
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("gcurrencyid",sessionHandlerImpl.getCurrencyID(request));
        requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
        requestParams.put("dateformat", authHandler.getDateOnlyFormat(request));
        double enterAmountPaymentCurrencyOld=0;
        double enterAmountTrancastionCurrencyNew=0;
        double amountdiff=0;
        try {
            Double enteramount = jobj.getDouble("enteramount");
            String documentId = jobj.getString("documentid");
            KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), documentId);
            DebitNote debitNote = (DebitNote) resultObject.getEntityList().get(0);
            JournalEntry je = debitNote.getJournalEntry();
            Date creditNoteDate = null;
            if (debitNote.isNormalDN()) {
                je = debitNote.getJournalEntry();
//                creditNoteDate = je.getEntryDate();
                externalCurrencyRate = je.getExternalCurrencyRate();
            } else {
                externalCurrencyRate =debitNote.getExchangeRateForOpeningTransaction();
                if(debitNote.isConversionRateFromCurrencyToBase()) {
                   externalCurrencyRate = 1 / externalCurrencyRate;
                }
            }
            creditNoteDate = debitNote.getCreationDate();
            
            HashMap<String, Object> invoiceId = new HashMap<>();
            invoiceId.put("invoiceid", debitNote.getID());
            invoiceId.put("companyid", sessionHandlerImpl.getCompanyid(request));
            KwlReturnObject result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
            RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
            if (history != null) {
                externalCurrencyRate = history.getEvalrate();
            }

            KwlReturnObject bAmt = null;
            if (!StringUtil.isNullOrEmpty(transactionCurrencyId) && !StringUtil.isNullOrEmpty(transactionCurrencyId) && !transactionCurrencyId.equals(currencyid)) {
                enterAmountTrancastionCurrencyNew = enteramount;
                if (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString())) {
                    double exchangeratefortransaction = Double.parseDouble(jobj.optString("exchangeratefortransaction", "1"));
                    enterAmountTrancastionCurrencyNew = enteramount / Double.parseDouble(jobj.get("exchangeratefortransaction").toString());
                    enterAmountTrancastionCurrencyNew = authHandler.round(enterAmountTrancastionCurrencyNew, companyid);
                    KwlReturnObject bAmtCurrencyFilter = null;
                    bAmtCurrencyFilter = accCurrencyobj.getOneCurrencyToOtherModified(requestParams, enterAmountTrancastionCurrencyNew, transactionCurrencyId, currencyid, creditNoteDate, externalCurrencyRate);
                    enterAmountPaymentCurrencyOld = authHandler.round((Double) bAmtCurrencyFilter.getEntityList().get(0), companyid);
                    amountdiff=enteramount-enterAmountPaymentCurrencyOld;
                }
            }            
        }catch (Exception ex) {
            throw ServiceException.FAILURE("cndnReceiptForexGailLossAmount : " + ex.getMessage(), ex);
        }
        return (amountdiff);
    }
    
    public double cndnReceiptForexGailLossAmountForSameCurrency(HttpServletRequest request, JSONObject jobj, Receipt receipt, String transactionCurrencyId, String currencyid, double externalCurrencyRate) throws ServiceException, SessionExpiredException, JSONException {
        double amount = 0, actualAmount = 0;
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
        requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
        requestParams.put("dateformat", authHandler.getDateOnlyFormat(request));
        try {

            double exchangeRate = 0d;
            double newrate = 0.0;
            Double enteramount = jobj.getDouble("enteramount");
            String documentId = jobj.getString("documentid");
            KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), documentId);
            DebitNote debitMemo = (DebitNote) resultObject.getEntityList().get(0);
            JournalEntry je = debitMemo.getJournalEntry();
            Date creditNoteDate = null;
            if (debitMemo.isNormalDN()) {
                je = debitMemo.getJournalEntry();
//                creditNoteDate = je.getEntryDate();
                exchangeRate = je.getExternalCurrencyRate();
            } else {
                exchangeRate =debitMemo.getExchangeRateForOpeningTransaction();
                if(debitMemo.isConversionRateFromCurrencyToBase()) {
                   exchangeRate = 1 / exchangeRate;
                }
            }
            creditNoteDate = debitMemo.getCreationDate();
            double paymentExternalCurrencyRate=StringUtil.isNullOrEmpty(request.getParameter("externalcurrencyrate"))?1.0:Double.parseDouble(request.getParameter("externalcurrencyrate"));
                            
            KwlReturnObject bAmt = null;
            if(exchangeRate!=paymentExternalCurrencyRate){
                bAmt = accCurrencyobj.getOneCurrencyToOtherWithDiffRates(requestParams, 1.0, transactionCurrencyId, currencyid, creditNoteDate, exchangeRate,paymentExternalCurrencyRate);
            }else{
                bAmt = accCurrencyobj.getOneCurrencyToOther(requestParams, 1.0, transactionCurrencyId, currencyid, creditNoteDate, exchangeRate);
            }
            double oldrate = (Double) bAmt.getEntityList().get(0);
            newrate = jobj.optDouble("exchangeratefortransaction", 0.0);
            amount = (enteramount - (enteramount / newrate) * oldrate) / newrate;
            KwlReturnObject bAmtActual = accCurrencyobj.getBaseToCurrencyAmount(requestParams, amount, currencyid, creditNoteDate, newrate);
            actualAmount += (Double) bAmtActual.getEntityList().get(0);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("cndnReceiptForexGailLossAmountForSameCurrency : " + ex.getMessage(), ex);
        }
        return (actualAmount);
    }
    
    public JournalEntry journalEntryObject(HttpServletRequest request, Receipt editReceiptObject,String receiptID) throws SessionExpiredException, ServiceException, AccountingException {
        JournalEntry journalEntry = null;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            String userid = sessionHandlerImpl.getUserid(request);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            currencyid = (request.getParameter("currencyid") == null ? currency.getCurrencyID() : request.getParameter("currencyid"));
            String methodid = request.getParameter("pmtmethod");
            double PaymentCurrencyToPaymentMethodCurrencyRate = StringUtil.getDouble(request.getParameter("paymentCurrencyToPaymentMethodCurrencyExchangeRate"));
            boolean ismulticurrencypaymentje= StringUtil.getBoolean(request.getParameter("ismulticurrencypaymentje"));
            sessionHandlerImpl.updatePaymentMethodID(request, methodid);
            String jeid = null;
            String jeentryNumber = null;
            boolean jeautogenflag = false;
            String jeIntegerPart = "";
            String jeSeqFormatId = "";
            Date entryDate = df.parse(request.getParameter("creationdate"));
             double externalCurrencyRate = StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);
            Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);

            if (editReceiptObject == null) {
                synchronized (this) {
                    HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                    JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                    JEFormatParams.put("modulename", "autojournalentry");
                    JEFormatParams.put("companyid", companyid);
                    JEFormatParams.put("isdefaultFormat", true);

                    KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                    SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, entryDate);
                    jeentryNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    jeSeqFormatId = format.getID();
                    jeautogenflag = true;

                    jeDataMap.put("entrynumber", "");
                    jeDataMap.put("autogenerated", jeautogenflag);
                    jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
//                    jeDataMap.put(Constants.SEQNUMBER, jeIntegerPart);
                }
            } else if (editReceiptObject != null && editReceiptObject.getJournalEntry() != null) {
                JournalEntry entry = editReceiptObject.getJournalEntry();
                jeid = editReceiptObject.getJournalEntry().getID();
//                jeDataMap.put("jeid", jeid);
                jeDataMap.put("entrynumber", entry.getEntryNumber());
                jeDataMap.put("autogenerated", entry.isAutoGenerated());
                jeDataMap.put(Constants.SEQFORMAT, entry.getSeqformat().getID());
                jeDataMap.put(Constants.SEQNUMBER, entry.getSeqnumber());
                jeDataMap.put(Constants.DATEPREFIX, entry.getDatePreffixValue());
                jeDataMap.put(Constants.DATEAFTERPREFIX, entry.getDateAfterPreffixValue());
                jeDataMap.put(Constants.DATESUFFIX, entry.getDateSuffixValue());
            }
            jeDataMap.put("entrydate", entryDate);
            jeDataMap.put("companyid", company.getCompanyID());
            jeDataMap.put("memo", request.getParameter("memo"));
            jeDataMap.put("currencyid", currencyid);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeDataMap.put("PaymentCurrencyToPaymentMethodCurrencyRate", PaymentCurrencyToPaymentMethodCurrencyRate);
            jeDataMap.put("ismulticurrencypaymentje", ismulticurrencypaymentje);
            jeDataMap.put("createdby", userid);
            jeDataMap.put("transactionId",receiptID);
            jeDataMap.put("transactionModuleid",Constants.Acc_Receive_Payment_ModuleId);
            journalEntry = accJournalEntryobj.getJournalEntry(jeDataMap);
        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return journalEntry;
    }
     public JournalEntry journalEntryObjectBankCharges(HttpServletRequest request, Receipt editReceiptObject,int counter,boolean isBankCharge,boolean paymentWithoutJe,JournalEntry oldBankChargeJE,JournalEntry oldBankInterestJE) throws SessionExpiredException, ServiceException, AccountingException {
        JournalEntry journalEntry = null;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            currencyid = (request.getParameter("currencyid") == null ? currency.getCurrencyID() : request.getParameter("currencyid"));
            String methodid = request.getParameter("pmtmethod");
            sessionHandlerImpl.updatePaymentMethodID(request, methodid);
            String jeid = null;
            String jeentryNumber = null;
            boolean jeautogenflag = false;
            String jeIntegerPart = "";
            String jeSeqFormatId = "";
             double externalCurrencyRate = StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);
            Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
            if(paymentWithoutJe){
//                counter--;
            }
            if (editReceiptObject == null || paymentWithoutJe) {
//                synchronized (this) {
//                    HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
//                    JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
//                    JEFormatParams.put("modulename", "autojournalentry");
//                    JEFormatParams.put("companyid", companyid);
//                    JEFormatParams.put("isdefaultFormat", true);
//
//                    KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
//                    SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
//                    String[] nextAutoNoTemp = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false);
//                    int sequence = Integer.parseInt(nextAutoNoTemp[1]);
//                    sequence = sequence + counter;
//                    String number = "" + sequence;
//                    String action = "" + (sequence - counter);
//                    nextAutoNoTemp[0].replaceAll(action, number);
//                    jeentryNumber = nextAutoNoTemp[0].replaceAll(action, number);  //next auto generated number
////                    jeentryNumber = nextAutoNoTemp[0];
//                    jeIntegerPart = nextAutoNoTemp[1];
//                    jeSeqFormatId = format.getID();
//                    jeautogenflag = true;

                    jeDataMap.put("entrynumber", "");
                    jeDataMap.put("autogenerated", true);
//                    jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
//                    jeDataMap.put(Constants.SEQNUMBER, number);
//                }
            } else if (editReceiptObject != null && oldBankChargeJE != null && isBankCharge) {
                JournalEntry entry = oldBankChargeJE;
                jeid = editReceiptObject.getJournalEntry().getID();
//                jeDataMap.put("jeid", jeid);
                jeDataMap.put("entrynumber", entry.getEntryNumber());
                jeDataMap.put("autogenerated", entry.isAutoGenerated());
                jeDataMap.put(Constants.SEQFORMAT, entry.getSeqformat().getID());
                jeDataMap.put(Constants.SEQNUMBER, entry.getSeqnumber());
                jeDataMap.put(Constants.DATEPREFIX, entry.getDatePreffixValue());
                jeDataMap.put(Constants.DATESUFFIX, entry.getDateSuffixValue());
            } else if (editReceiptObject != null && oldBankInterestJE != null && !isBankCharge) {
                JournalEntry entry = oldBankInterestJE;
                jeDataMap.put("entrynumber", entry.getEntryNumber());
                jeDataMap.put("autogenerated", entry.isAutoGenerated());
                jeDataMap.put(Constants.SEQFORMAT, entry.getSeqformat().getID());
                jeDataMap.put(Constants.SEQNUMBER, entry.getSeqnumber());
                jeDataMap.put(Constants.DATEPREFIX, entry.getDatePreffixValue());
                jeDataMap.put(Constants.DATESUFFIX, entry.getDateSuffixValue());
            }
            jeDataMap.put("entrydate", df.parse(request.getParameter("creationdate")));
            jeDataMap.put("companyid", company.getCompanyID());
            jeDataMap.put("memo", request.getParameter("memo"));
            jeDataMap.put("currencyid", currencyid);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            journalEntry = accJournalEntryobj.getJournalEntry(jeDataMap);
        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return journalEntry;
    }
    
    public Receipt createReceiptObject(HttpServletRequest request, Receipt editReceiptObject) throws SessionExpiredException, ServiceException, AccountingException {
        KwlReturnObject result = null;
        List list = new ArrayList();
        Invoice invoice = null;
        Customer cust = null;
        Vendor vend = null;
        Receipt receiptObject = null;
            Account dipositTo = null;
            String sequenceformat = request.getParameter("sequenceformat")!=null?request.getParameter("sequenceformat"):"NA";
            String companyid = sessionHandlerImpl.getCompanyid(request);
            double externalCurrencyRate = StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            DateFormat df = authHandler.getDateOnlyFormat(request);
            Date creationDate=null;
            if(!StringUtil.isNullOrEmpty(request.getParameter("creationdate"))){
                try{
                    creationDate=df.parse(request.getParameter("creationdate"));
                }catch(Exception ex){
                    throw ServiceException.FAILURE("createReceiptObject : " + ex.getMessage(), ex);
                }
            }
            String entryNumber = request.getParameter("no");
            String methodid = request.getParameter("pmtmethod");        
            double PaymentCurrencyToPaymentMethodCurrencyRate = StringUtil.getDouble(request.getParameter("paymentCurrencyToPaymentMethodCurrencyExchangeRate"));
            sessionHandlerImpl.updatePaymentMethodID(request, methodid);            
            int receiptType = StringUtil.getInteger(request.getParameter("receipttype"));
            int actualReceiptType = StringUtil.getInteger(request.getParameter("actualReceiptType") != null ? request.getParameter("actualReceiptType") : "0");
            String payDetailID = null;
            HashMap<String, JSONArray> Map1 = new HashMap();
            String jeentryNumber = null;
            HashMap receipthm = new HashMap();
            boolean ismanydbcr = StringUtil.getBoolean(request.getParameter("ismanydbcr"));
            receipthm.put("ismanydbcr", ismanydbcr);
            receipthm.put("receipttype", receiptType);
            receipthm.put("actualReceiptType", actualReceiptType);
            double bankCharges = 0;
            double bankInterest = 0;
            String accountId = request.getParameter("accid");
            String bankChargesAccid = request.getParameter("bankChargesCmb");
            String bankInterestAccid = request.getParameter("bankInterestCmb");
            boolean onlyAdvance = StringUtil.getBoolean(request.getParameter("onlyAdvance"));
            
            String oldjeid = null;
            String Cardid = null;
            String oldChequeNo = "";
            
            String createdby = sessionHandlerImpl.getUserid(request);
            String modifiedby = sessionHandlerImpl.getUserid(request);
            long createdon = System.currentTimeMillis();
            long updatedon = System.currentTimeMillis();

            StringBuffer billno = new StringBuffer();
            if (!StringUtil.isNullOrEmpty(request.getParameter("bankCharges")) && !StringUtil.isNullOrEmpty(bankChargesAccid)) {
                bankCharges = Double.parseDouble(request.getParameter("bankCharges"));
                receipthm.put("bankCharges", bankCharges);
                receipthm.put("bankChargesCmb", bankChargesAccid);
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("bankInterest")) && !StringUtil.isNullOrEmpty(bankInterestAccid)) {
                bankInterest = Double.parseDouble(request.getParameter("bankInterest"));
                receipthm.put("bankInterest", bankInterest);
                receipthm.put("bankInterestCmb", bankInterestAccid);
            }
            String paidToid = request.getParameter("paidToCmb");
            if (!StringUtil.isNullOrEmpty(request.getParameter("paidToCmb")) && !StringUtil.isNullOrEmpty(paidToid)) {
                receipthm.put("paidToCmb", paidToid);
            }
            
                boolean isCustomer = false;
                boolean isVendor = false;
                if (!StringUtil.isNullOrEmpty(request.getParameter("accid"))) {
                KwlReturnObject custObj = accountingHandlerDAOobj.getObject(Customer.class.getName(), request.getParameter("accid"));
                if (custObj.getEntityList().get(0) != null) {
                    cust = (Customer) custObj.getEntityList().get(0);
                }
                if (cust != null) {
                    isCustomer = true;
                }

                
                KwlReturnObject vendObj = accountingHandlerDAOobj.getObject(Vendor.class.getName(), request.getParameter("accid"));
                if (vendObj.getEntityList().get(0) != null) {
                    vend = (Vendor) vendObj.getEntityList().get(0);
                }
                if (vend != null) {
                    isVendor = true;
                }
                }
                if (isCustomer) {
                    receipthm.put("customerId", request.getParameter("accid"));
                } else if (isVendor) {
                    receipthm.put("vendor", request.getParameter("accid"));
                }

            boolean bankReconsilationEntry = false;
            boolean bankPayment = false;
            Date clearanceDate = null, startDate = null, endDate = null;
            String bankAccountId = "";
            Map<String, Object> bankReconsilationMap = new HashMap<String, Object>();
            boolean editAdvance = false;
            
            
            if (editReceiptObject != null) {// for edit case
                oldjeid = editReceiptObject.getJournalEntry().getID();
                JournalEntry jetemp = editReceiptObject.getJournalEntry();

                if (editReceiptObject.getPayDetail() != null) {
                    payDetailID = editReceiptObject.getPayDetail().getID();
                    if (editReceiptObject.getPayDetail().getCard() != null) {
                        Cardid = editReceiptObject.getPayDetail().getCard().getID();
                    }
                    if (editReceiptObject.getPayDetail().getCheque() != null) {
                        Cardid = editReceiptObject.getPayDetail().getCheque().getID();
                        oldChequeNo = editReceiptObject.getPayDetail().getCheque().getChequeNo();
                    }
                }
            }
            
            
            synchronized (this) { //this block is used to generate auto sequence number if number is not duplicate
                String nextAutoNo = "";
                String nextAutoNoInt = "";
                int count = 0;
                if (editReceiptObject != null) {
                    if (sequenceformat.equals("NA")) {
                        if (!entryNumber.equals(editReceiptObject.getReceiptNumber())) {
                            result = accReceiptDAOobj.getReceiptFromBillNo(entryNumber, companyid);
                            receipthm.put("entrynumber", entryNumber);
                            receipthm.put("autogenerated", entryNumber.equals(nextAutoNo));
                            count = result.getRecordTotalCount();
                        }
                        if (count > 0) {
                            throw new AccountingException(messageSource.getMessage("acc.field.Receiptnumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                        }
                    }
                } else {
                    if (!sequenceformat.equals("NA")) {
                        receipthm.put(Constants.SEQFORMAT, sequenceformat);
                        receipthm.put(Constants.SEQNUMBER, "");

                    }
                    if(sequenceformat.equals("NA")){
                        receipthm.put("entrynumber", entryNumber);
                    }else{
                        receipthm.put("entrynumber", "");
                    }
                    
                    receipthm.put("autogenerated", sequenceformat.equals("NA") ? false : true);
                }
            }
            if (sequenceformat.equals("NA")) {//In case of NA checks wheather this number can also be generated by a sequence format or not
                List resultList = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Receive_Payment_ModuleId, entryNumber, companyid);
                if (!resultList.isEmpty()) {
                    boolean isvalidEntryNumber = (Boolean) resultList.get(0);
                    String formatName = (String) resultList.get(1);
                    if (!isvalidEntryNumber) {
                        throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, RequestContextUtils.getLocale(request)) + " <b>" + entryNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, RequestContextUtils.getLocale(request)) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null, RequestContextUtils.getLocale(request)) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null, RequestContextUtils.getLocale(request)));
                    }
                }
            }
            

            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            String currencyid = (request.getParameter("currencyid") == null ? currency.getCurrencyID() : request.getParameter("currencyid"));

//            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
//            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
//            
//            KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
//            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);


            

            receipthm.put("currencyid", currencyid);
            receipthm.put("nonRefundable",!StringUtil.isNullOrEmpty(request.getParameter("NonRefundable")));
            receipthm.put("externalCurrencyRate", externalCurrencyRate);
            KwlReturnObject payresult = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), request.getParameter("pmtmethod"));
            PaymentMethod payMethod = (PaymentMethod) payresult.getEntityList().get(0);
            receipthm.put("memo", request.getParameter("memo"));
            receipthm.put("companyid", company.getCompanyID());
            receipthm.put("createdby", createdby);
            receipthm.put("modifiedby", modifiedby);
            receipthm.put("creationDate", creationDate);
            receipthm.put(Constants.Checklocktransactiondate,request.getParameter("creationdate"));//ERP-16800-Without parsing date
            receipthm.put("createdon", createdon);
            receipthm.put("updatedon", updatedon);
            receipthm.put("isLinkedToClaimedInvoice", !StringUtil.isNullOrEmpty(request.getParameter("isLinkedToClaimedInvoice")) ? Boolean.parseBoolean(request.getParameter("isLinkedToClaimedInvoice")):false);
            receipthm.put("PaymentCurrencyToPaymentMethodCurrencyRate", PaymentCurrencyToPaymentMethodCurrencyRate);
            if (editReceiptObject != null) {
                receipthm.put("receiptid", editReceiptObject.getID());
            }
            receiptObject = accReceiptDAOobj.getReceiptObj(receipthm);
        return receiptObject;
    }
    /*
     * Method to save Opening Balance Receipts For customer.
     */
    public ModelAndView saveOpeningBalanceReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject jobjDetails = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("BR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            List li = saveOpeningBalanceReceipt(request);
            String receiptNumber = null;
            boolean isEditInv = false;
            String succMsg = messageSource.getMessage("acc.field.saved", null, RequestContextUtils.getLocale(request));
            if (!li.isEmpty()) {
                receiptNumber = li.get(0).toString();
                isEditInv = (Boolean) li.get(1);
            }
            if (isEditInv) {
                succMsg = messageSource.getMessage("acc.field.updated", null, RequestContextUtils.getLocale(request));
            }
            issuccess = true;
            msg = messageSource.getMessage("acc.field.Receipt", null, RequestContextUtils.getLocale(request)) + " " + receiptNumber + " " + messageSource.getMessage("acc.field.hasbeen", null, RequestContextUtils.getLocale(request)) + " " + succMsg + " " + messageSource.getMessage("acc.field.successfully", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArr);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView importOpeningBalanceReceipts(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            String doAction = request.getParameter("do");
            System.out.println("A(( " + doAction + " start : " + new Date());

            JSONObject datajobj = new JSONObject();

            if (doAction.compareToIgnoreCase("getMapCSV") == 0) {
                datajobj = importHandler.getMappingCSVHeader(request);
                JSONArray jSONArray = datajobj.getJSONArray("Header");
                validateHeaders(jSONArray, request);

                jobj = importOeningTransactionsRecords(request, datajobj);
//                msg = "All records are imported successfully.";
                issuccess = true;
            }
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();

            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException e) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }

            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }

    public void validateHeaders(JSONArray validateJArray, HttpServletRequest request) throws AccountingException, ServiceException {
        try {

            List<String> list = new ArrayList<String>();
            list.add("Transaction Number");
            list.add("Transaction Date");
            list.add("Amount");
//            list.add("Due Date");
            list.add("Customer Code");
//            list.add("Exchange Rate");
            list.add("Currency");



            List<String> fileHeaderList = new ArrayList<String>();

            for (int i = 0; i < validateJArray.length(); i++) {
                String header = validateJArray.getJSONObject(i).getString("header").trim();
                fileHeaderList.add(header);
            }

            // iterating for manadatory columns

            for (String manadatoryField : list) {
                if (!fileHeaderList.contains(manadatoryField)) {
                    throw new AccountingException(manadatoryField + messageSource.getMessage("acc.field.columnisnotavailabeinfile", null, RequestContextUtils.getLocale(request)));
                }
            }

        } catch (JSONException ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    public List saveOpeningBalanceReceipt(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        List returnList = new ArrayList();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            KwlReturnObject result;
            boolean isEditInvoice = false;
            String auditMsg = "", auditID = "";
            auditMsg = "added";
            auditID = AuditAction.OPENING_BALANCE_CREATED;
            // Fetching request parameters

            String receiptNumber = request.getParameter("number");
            String transactionDateStr = request.getParameter("billdate");
            String chequeDateStr = request.getParameter("chequeDate");
            String currencyid = (request.getParameter("currencyid") == null ? sessionHandlerImpl.getCurrencyID(request) : request.getParameter("currencyid"));
            String transactionAmountStr = request.getParameter("transactionAmount");
            String receiptId = request.getParameter("transactionId");
            String chequeNumber = request.getParameter("chequenumber");
            String drawnOn = request.getParameter("drawnon");
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
            
            String accountId = "";

            if (!StringUtil.isNullOrEmpty(customerId)) {
                KwlReturnObject custresult = accountingHandlerDAOobj.getObject(Customer.class.getName(), customerId);
                Customer customer = (Customer) custresult.getEntityList().get(0);
                accountId = customer.getAccount().getID();
            }

            Date transactionDate = df.parse(df.format(new Date()));
            Date chequeDate = df.parse(df.format(new Date()));

            if (!StringUtil.isNullOrEmpty(transactionDateStr)) {
                transactionDate = df.parse(transactionDateStr);
            }

            if (!StringUtil.isNullOrEmpty(chequeDateStr)) {
                chequeDate = df.parse(chequeDateStr);
            }

            double transactionAmount = 0d;
            if (!StringUtil.isNullOrEmpty(transactionAmountStr)) {
                transactionAmount = Double.parseDouble(transactionAmountStr);
            }

            // creating receipt data

            HashMap receipthm = new HashMap();

            if (StringUtil.isNullOrEmpty(receiptId)) {
                result = accReceiptDAOobj.getReceiptFromBillNo(receiptNumber, companyid);
                int count = result.getRecordTotalCount();
                if (count > 0) {
                    throw new AccountingException(messageSource.getMessage("acc.field.Receiptnumber", null, RequestContextUtils.getLocale(request)) + " " + receiptNumber + " " + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                }
                receipthm.put("entrynumber", receiptNumber);
                receipthm.put("autogenerated", false);
            }


            if (!StringUtil.isNullOrEmpty(receiptId)) {
                isEditInvoice = true;
                auditMsg = "updated";
                auditID = AuditAction.OPENING_BALANCE_UPDATED;
                boolean isPaymentUsedInOtherTransactions = isPaymentUsedInOtherTransactions(receiptId, companyid);

                if (isPaymentUsedInOtherTransactions) {
                    throw new AccountingException(messageSource.getMessage("acc.nee.73", null, RequestContextUtils.getLocale(request)));
                }

                receipthm.put("receiptid", receiptId);
            }


            receipthm.put("depositamount", transactionAmount);//
            receipthm.put("currencyid", currencyid);//
            receipthm.put("externalCurrencyRate", externalCurrencyRate);//
            receipthm.put("memo", "");//
            receipthm.put("companyid", companyid);//
            receipthm.put("chequeNumber", chequeNumber);//
            receipthm.put("drawnOn", drawnOn);//
            receipthm.put("creationDate", transactionDate);//
            receipthm.put("chequeDate", chequeDate);//
            receipthm.put("customerId", customerId);//
            receipthm.put("accountId", accountId);//
            receipthm.put("isOpeningBalenceReceipt", true);//
            receipthm.put("normalReceipt", false);//
            receipthm.put("openingBalanceAmountDue", transactionAmount);//
            receipthm.put("isadvancepayment", true);
            receipthm.put("contraentry", false);
            receipthm.put("exchangeRateForOpeningTransaction", exchangeRateForOpeningTransaction);
            receipthm.put("conversionRateFromCurrencyToBase", conversionRateFromCurrencyToBase);
            // Store Receipt amount in base currency
            if (conversionRateFromCurrencyToBase) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                receipthm.put(Constants.openingBalanceBaseAmountDue, authHandler.round(transactionAmount*exchangeRateForOpeningTransaction, companyid));
                receipthm.put(Constants.originalOpeningBalanceBaseAmount, authHandler.round(transactionAmount*exchangeRateForOpeningTransaction, companyid));
            } else {
                receipthm.put(Constants.openingBalanceBaseAmountDue, authHandler.round(transactionAmount/exchangeRateForOpeningTransaction, companyid));
                receipthm.put(Constants.originalOpeningBalanceBaseAmount, authHandler.round(transactionAmount/exchangeRateForOpeningTransaction, companyid));
            }
            
            String createdby = sessionHandlerImpl.getUserid(request);
            String modifiedby = sessionHandlerImpl.getUserid(request);
            long createdon = System.currentTimeMillis();
            long updatedon = System.currentTimeMillis();

            receipthm.put("createdby", createdby);
            receipthm.put("modifiedby", modifiedby);
            receipthm.put("createdon", createdon);
            receipthm.put("updatedon", updatedon);

            result = accReceiptDAOobj.saveReceipt(receipthm);

            Receipt receipt = (Receipt) result.getEntityList().get(0);
            returnList.add(receipt.getReceiptNumber());
            returnList.add(isEditInvoice);
              
            String customfield = request.getParameter("customfield");
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_OpeningBalanceReceipt_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_OpeningBalanceReceiptid);
                customrequestParams.put("modulerecid", receipt.getID());
                customrequestParams.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath",Constants.Acc_OpeningBalanceReceipt_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    receipthm.put("receiptid", receipt.getID());
                    receipthm.put("openingBalanceReceiptCustomData", receipt.getID());
                    result = accReceiptDAOobj.saveReceipt(receipthm);
                }
            }
            auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has  " + auditMsg + " a Opening Balance Receipt " + receiptNumber, request, receiptNumber);
        } catch (JSONException ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ParseException ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return returnList;
    }

    private boolean isPaymentUsedInOtherTransactions(String paymentId, String companyId) throws ServiceException {
        boolean isPaymentUsedInOtherTransactions = false;

        KwlReturnObject result;
        if (!StringUtil.isNullOrEmpty(paymentId)) {
            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Receipt.class.getName(), paymentId);
            Receipt receipt = (Receipt) objItr.getEntityList().get(0);

            if (receipt != null) {
                Set<ReceiptDetail> receiptDetailSet = receipt.getRows();
                if (receiptDetailSet != null) {
                    Iterator itr = receiptDetailSet.iterator();
                    while (itr.hasNext()) {
                        ReceiptDetail row = (ReceiptDetail) itr.next();
                        if (row.getInvoice() != null) {
                            isPaymentUsedInOtherTransactions = true;
                        }
                    }
                }
            }

        }
        return isPaymentUsedInOtherTransactions;
    }

    public HashMap getCurrencyMap() throws ServiceException {
        HashMap currencyMap = new HashMap();
        KwlReturnObject returnObject = accCurrencyobj.getCurrencies(currencyMap);
        List currencyList = returnObject.getEntityList();

        if (currencyList != null && !currencyList.isEmpty()) {
            Iterator iterator = currencyList.iterator();
            while (iterator.hasNext()) {
                KWLCurrency currency = (KWLCurrency) iterator.next();
                currencyMap.put(currency.getName(), currency.getCurrencyID());
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
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException(messageSource.getMessage("acc.field.SystemFailurewhilefetchingcustomer", null, RequestContextUtils.getLocale(request)));
        }
        return customer;
    }

    public JSONObject importOeningTransactionsRecords(HttpServletRequest request, JSONObject jobj) throws AccountingException, IOException, SessionExpiredException, JSONException {

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("import_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        boolean commitedEx = false;
        boolean issuccess = true;
        String msg = "";
        FileInputStream fileInputStream = null;
        BufferedReader br = null;
        int total = 0, failed = 0;
        String companyid = sessionHandlerImpl.getCompanyid(request);
        String gcurrencyId = sessionHandlerImpl.getCurrencyID(request);
        String userId = sessionHandlerImpl.getUserid(request);
        String fileName = jobj.getString("name");

        JSONObject returnObj = new JSONObject();

        try {

            String dateFormat = null, dateFormatId = request.getParameter("dateFormat");
            if (!StringUtil.isNullOrEmpty(dateFormatId)) {

                KwlReturnObject kdfObj = accountingHandlerDAOobj.getObject(KWLDateFormat.class.getName(), dateFormatId);
                KWLDateFormat kdf = (KWLDateFormat) kdfObj.getEntityList().get(0);

                dateFormat = kdf != null ? kdf.getJavaForm() : null;
            }

            DateFormat df = new SimpleDateFormat(dateFormat);
            DateFormat datef=authHandler.getDateOnlyFormat();
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);

            fileInputStream = new FileInputStream(jobj.getString("FilePath"));
            br = new BufferedReader(new InputStreamReader(fileInputStream));
            String record = "";
            int cnt = 0;

            double externalCurrencyRate = 0d;//StringUtil.getDouble(request.getParameter("externalcurrencyrate"));

            StringBuilder failedRecords = new StringBuilder();

            JSONArray jSONArray = jobj.getJSONArray("Header");
            List headArrayList = new ArrayList();
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
                headArrayList.add(jSONObject.get("header"));
            }

            failedRecords.append(createCSVrecord(headArrayList.toArray()) + "\"Error Message\"");//failedRecords.append("\"Row No.\","+createCSVrecord(fileData)+"\"Error Message\"");

            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);

            while ((record = br.readLine()) != null) {
                if (cnt != 0) {

                    String[] recarr = record.split(",");
                    try {
                        double exchangeRateForOpeningTransaction = 1;

                        int customerCodeIndex = headArrayList.indexOf("Customer Code");

                        String customerCode = recarr[customerCodeIndex].trim();
                        if (!StringUtil.isNullOrEmpty(customerCode)) {
                            customerCode = customerCode.replaceAll("\"", "");
                        } else {
                            throw new AccountingException(messageSource.getMessage("acc.field.CustomerCodeisnotavailable", null, RequestContextUtils.getLocale(request)));
                        }

                        String accountId = "";
                        String customerId = "";
                        Customer customer = getCustomerByCode(customerCode, companyid, request);
                        if (customer != null) {
                            accountId = customer.getAccount().getID();
                            customerId = customer.getID();
                        } else {
                            throw new AccountingException(messageSource.getMessage("acc.field.CustomerisnotfoundforCustomerCode", null, RequestContextUtils.getLocale(request)) + customerCode);
                        }

                        HashMap currencyMap = getCurrencyMap();

                        int invoiceNumberIndex = headArrayList.indexOf("Transaction Number");

                        String invoiceNumber = recarr[invoiceNumberIndex].trim();
                        if (!StringUtil.isNullOrEmpty(invoiceNumber)) {
                            invoiceNumber = invoiceNumber.replaceAll("\"", "");
                        } else {
                            throw new AccountingException(messageSource.getMessage("acc.field.TransactionNumberisnotavailable", null, RequestContextUtils.getLocale(request)));
                        }


                        int transactionDateIndex = headArrayList.indexOf("Transaction Date");

                        String transactionDateStr = recarr[transactionDateIndex].trim();
                        if (!StringUtil.isNullOrEmpty(transactionDateStr)) {
                            transactionDateStr = transactionDateStr.replaceAll("\"", "");
                        } else {
                            throw new AccountingException(messageSource.getMessage("acc.field.TransactionDateisnotavailable", null, RequestContextUtils.getLocale(request)));
                        }


                        int transactionAmountIndex = headArrayList.indexOf("Amount");

                        String transactionAmountStr = recarr[transactionAmountIndex].trim();
                        if (!StringUtil.isNullOrEmpty(transactionAmountStr)) {
                            transactionAmountStr = transactionAmountStr.replaceAll("\"", "");
                        } else {
                            throw new AccountingException(messageSource.getMessage("acc.field.TransactionAmountisnotavailable", null, RequestContextUtils.getLocale(request)));
                        }


//                        int dueDateIndex = headArrayList.indexOf("Due Date");
//
//                        String dueDateStr = recarr[dueDateIndex].trim();
//                        if(!StringUtil.isNullOrEmpty(dueDateStr)){
//                            dueDateStr = dueDateStr.replaceAll("\"", "");
//                        }else{
//                            throw new AccountingException("Due Date is not available");
//                        }


                        int exchangeRateIndex = headArrayList.indexOf("Exchange Rate");

                        String exchangeRateForOpeningTransactionStr = "";
                        if (exchangeRateIndex > 0) {
                            exchangeRateForOpeningTransactionStr = recarr[exchangeRateIndex].trim();

                            if (!StringUtil.isNullOrEmpty(exchangeRateForOpeningTransactionStr)) {
                                exchangeRateForOpeningTransactionStr = exchangeRateForOpeningTransactionStr.replaceAll("\"", "");
                            }
                        }


                        int currencyIndex = headArrayList.indexOf("Currency");

                        String currencyStr = recarr[currencyIndex].trim();
                        if (!StringUtil.isNullOrEmpty(currencyStr)) {
                            currencyStr = currencyStr.replaceAll("\"", "");
                        } else {
                            throw new AccountingException(messageSource.getMessage("acc.field.Currencyisnotavailable", null, RequestContextUtils.getLocale(request)));
                        }

                        String currencyId = getCurrencyId(currencyStr, currencyMap);

                        if (StringUtil.isNullOrEmpty(currencyId)) {
                            throw new AccountingException(messageSource.getMessage("acc.field.CurrencyformatyouenteredisnotcorrectitshouldbelikeSGDollarSGD", null, RequestContextUtils.getLocale(request)));
                        }

                        int chequeNumberIndex = headArrayList.indexOf("Cheque Number");

                        String chequeNumber = "";

                        if (chequeNumberIndex > 0) {
                            String chequeNumberStr = recarr[chequeNumberIndex].trim();
                            if (!StringUtil.isNullOrEmpty(chequeNumberStr)) {
                                chequeNumber = chequeNumberStr.replaceAll("\"", "");
                            }
                        }

                        int drawnOnIndex = headArrayList.indexOf("Drawn On");

                        String drawnOn = "";

                        if (drawnOnIndex > 0) {
                            String drawnOnStr = recarr[drawnOnIndex].trim();
                            if (!StringUtil.isNullOrEmpty(drawnOnStr)) {
                                drawnOn = drawnOnStr.replaceAll("\"", "");
                            }
                        }

                        int chequeDateIndex = headArrayList.indexOf("Cheque Date");

                        Date chequeDate = null;

                        if (chequeDateIndex > 0) {
                            String chequeDateStr = recarr[chequeDateIndex].trim();
                            if (!StringUtil.isNullOrEmpty(chequeDateStr)) {
                                chequeDate = df.parse(chequeDateStr);
                            }
                        }


                        Date transactionDate = null, bookbeginningdate = null;
                        Date lastModifiedDate = df.parse(df.format(new Date()));


                        if (!StringUtil.isNullOrEmpty(transactionDateStr)) {
                            transactionDate = df.parse(transactionDateStr);
                            transactionDate = CompanyPreferencesCMN.removeTimefromDate(transactionDate);
                            bookbeginningdate = CompanyPreferencesCMN.removeTimefromDate(preferences.getBookBeginningFrom());
                            if (transactionDate.after(bookbeginningdate) || transactionDate.equals(bookbeginningdate)) {
                                throw new AccountingException(messageSource.getMessage("acc.transactiondate.beforebbdate", null, RequestContextUtils.getLocale(request)));
                            }
                            CompanyPreferencesCMN.checkLockPeriod(accCompanyPreferencesObj, requestParams, transactionDate, true);
                        }

                        if (transactionDate.after(preferences.getBookBeginningFrom()) || transactionDate.equals(preferences.getBookBeginningFrom())) {
                            throw new AccountingException(messageSource.getMessage("acc.field.TransactiondatemustbebeforeFinancialYearStartDate", null, RequestContextUtils.getLocale(request)));
                        }



                        if (!StringUtil.isNullOrEmpty(exchangeRateForOpeningTransactionStr)) {
                            exchangeRateForOpeningTransaction = Double.parseDouble(exchangeRateForOpeningTransactionStr);
                        } else {
                            Map<String, Object> currMap = new HashMap<String, Object>();
                            Date finYrStartDate = preferences.getFinancialYearFrom();
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(finYrStartDate);
                            cal.add(Calendar.DATE, -1);

                            Date applyDate = cal.getTime();
                            String adate=datef.format(applyDate);
                            try{
                                applyDate=datef.parse(adate);
                            }catch(ParseException ex){
                                applyDate=cal.getTime();
                            }
                            currMap.put("applydate", applyDate);
                            currMap.put("gcurrencyid", gcurrencyId);
                            currMap.put("companyid", companyid);
                            KwlReturnObject retObj = accCurrencyobj.getExcDetailID(currMap, currencyId, applyDate, null);
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

                        if (exchangeRateForOpeningTransaction <= 0) {
                            throw new AccountingException(messageSource.getMessage("acc.field.ExchangeRateCannotbezeroornegative", null, RequestContextUtils.getLocale(request)));
                        }

                        double transactionAmount = 0d;
                        if (!StringUtil.isNullOrEmpty(transactionAmountStr)) {
                            transactionAmount = Double.parseDouble(transactionAmountStr);
                        }

                        HashMap receipthm = new HashMap();

                        KwlReturnObject result = accReceiptDAOobj.getReceiptFromBillNo(invoiceNumber, companyid);
                        int count = result.getRecordTotalCount();
                        if (count > 0) {
                            throw new AccountingException(messageSource.getMessage("acc.field.Receiptnumber", null, RequestContextUtils.getLocale(request)) + invoiceNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                        }
                        receipthm.put("entrynumber", invoiceNumber);
                        receipthm.put("autogenerated", false);


                        receipthm.put("depositamount", transactionAmount);//
                        receipthm.put("currencyid", currencyId);//
                        receipthm.put("externalCurrencyRate", externalCurrencyRate);//
                        receipthm.put("memo", "");//
                        receipthm.put("companyid", companyid);//
                        receipthm.put("chequeNumber", chequeNumber);//
                        receipthm.put("drawnOn", drawnOn);//
                        receipthm.put("creationDate", transactionDate);//
                        receipthm.put("chequeDate", chequeDate);//
                        receipthm.put("customerId", customerId);//
                        receipthm.put("accountId", accountId);//
                        receipthm.put("isOpeningBalenceReceipt", true);//
                        receipthm.put("normalReceipt", false);//
                        receipthm.put("openingBalanceAmountDue", transactionAmount);//
                        receipthm.put("isadvancepayment", true);
                        receipthm.put("contraentry", false);
                        receipthm.put("exchangeRateForOpeningTransaction", exchangeRateForOpeningTransaction);
                        receipthm.put("conversionRateFromCurrencyToBase", true);
                        // Store Receipt amount in base currency
                        receipthm.put(Constants.openingBalanceBaseAmountDue, authHandler.round(transactionAmount*exchangeRateForOpeningTransaction, companyid));
                        receipthm.put(Constants.originalOpeningBalanceBaseAmount, authHandler.round(transactionAmount*exchangeRateForOpeningTransaction, companyid));
                        String createdby = sessionHandlerImpl.getUserid(request);
                        String modifiedby = sessionHandlerImpl.getUserid(request);
                        long createdon = System.currentTimeMillis();
                        long updatedon = System.currentTimeMillis();

                        receipthm.put("createdby", createdby);
                        receipthm.put("modifiedby", modifiedby);
                        receipthm.put("createdon", createdon);
                        receipthm.put("updatedon", updatedon);

                        result = accReceiptDAOobj.saveReceipt(receipthm);

                    } catch (Exception ex) {
                        failed++;
                        String errorMsg = ex.getMessage(), invalidColumns = "";
                        try {
                            JSONObject errorLog = new JSONObject(errorMsg);
                            errorMsg = errorLog.getString("errorMsg");
                            invalidColumns = errorLog.getString("invalidColumns");
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
                msg = messageSource.getMessage("acc.field.Imported", null, RequestContextUtils.getLocale(request)) + success + messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (success > 1 ? "s" : "") + messageSource.getMessage("acc.field.successfully", null, RequestContextUtils.getLocale(request));
                msg += (failed == 0 ? "." : messageSource.getMessage("acc.field.andfailedtoimport", null, RequestContextUtils.getLocale(request)) + failed + messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (failed > 1 ? "s" : "") + ".");
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

            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException(messageSource.getMessage("acc.import.msg9", null, RequestContextUtils.getLocale(request)));
        } finally {
            fileInputStream.close();
            br.close();

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
                logDataMap.put("Module", Constants.Acc_Receive_Payment_ModuleId);
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userId);
                logDataMap.put("Company", companyid);
                importDao.saveImportLog(logDataMap);
                txnManager.commit(lstatus);
            } catch (Exception ex) {
                txnManager.rollback(lstatus);
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", getActualFileName(fileName));
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
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

    public List saveReceipt(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
        KwlReturnObject result=null;
        Receipt receipt = null;
        String oldjeid = null;
        String Cardid = null;
        String netinword = "";
        double amount = 0;
        List ll = new ArrayList();
        GoodsReceipt greceipt = null;
        Customer cust = null;
        Vendor vend = null;
        try {
            Account dipositTo = null;
            double amountDiff = 0;
            boolean rateDecreased = false;
            String sequenceformat = request.getParameter("sequenceformat");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            double externalCurrencyRate = StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String entryNumber = request.getParameter("no");
            String customfield = request.getParameter("customfield");
            String receiptid = request.getParameter("billid");
            String methodid = request.getParameter("pmtmethod");        
            String advancePaymentIdForCnDn =request.getParameter("advancePaymentIdForCnDn");
            String mainPaymentForCNDNId =request.getParameter("mainPaymentForCNDNId");
            String invoiceadvcndntype =request.getParameter("invoiceadvcndntype");
            sessionHandlerImpl.updatePaymentMethodID(request, methodid);            
            boolean isMultiDebit = StringUtil.getBoolean(request.getParameter("ismultidebit"));
            boolean isAdvancePayment = StringUtil.getBoolean(request.getParameter("isadvpayment"));
            boolean isadvanceFromVendor = StringUtil.getBoolean(request.getParameter("isadvanceFromVendor"));
            boolean isCNDN = StringUtil.getBoolean(request.getParameter("isCNDN"));
            boolean isAgainstDN = StringUtil.getBoolean(request.getParameter("isAgainstDN"));
            boolean ignoreDuplicateChk = StringUtil.getBoolean(request.getParameter("ignoreDuplicateChk"));
            int receiptType = StringUtil.getInteger(request.getParameter("receipttype"));
            int actualReceiptType = StringUtil.getInteger(request.getParameter("actualReceiptType") != null ? request.getParameter("actualReceiptType") : "0");
            boolean isReceiptPaymentEdit = (Boolean.parseBoolean((String) request.getParameter("isReceiptEdit")));
            String drAccDetails = request.getParameter("detail");
            String jeid = null;
            boolean jeautogenflag = false;
            String jeSeqFormatId = "";
            String jeIntegerPart = "";
            String jeDatePrefix = "";
            String jeDateAfterPrefix = "";
            String jeDateSuffix = "";
            String payDetailID = null;
            HashMap<String, JSONArray> Map1 = new HashMap();
            String jeentryNumber = null;
            HashMap receipthm = new HashMap();
            receipthm.put("isadvancepayment", isAdvancePayment);
            receipthm.put("isadvanceFromVendor", isadvanceFromVendor);
            if(!StringUtil.isNullOrEmpty(advancePaymentIdForCnDn)){
                receipthm.put("advancePaymentIdForCnDn", advancePaymentIdForCnDn);
            }
            if(!StringUtil.isNullOrEmpty(mainPaymentForCNDNId)){
                receipthm.put("mainPaymentForCNDNId", mainPaymentForCNDNId);
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("isEdit"))){
                receipthm.put("isEdit", Boolean.parseBoolean(request.getParameter("isEdit")));
            }
            if(!StringUtil.isNullOrEmpty(invoiceadvcndntype)&&(actualReceiptType==0||actualReceiptType==1)){
                receipthm.put("invoiceadvcndntype", Integer.parseInt(invoiceadvcndntype));
            }
            HashMap<Integer,String> paymentHashMap=new HashMap<Integer, String>();
            if(!StringUtil.isNullOrEmpty(request.getParameter("datainvoiceadvcndn"))){
                JSONArray jSONArray=new JSONArray(request.getParameter("datainvoiceadvcndn"));
                String paymentid = "";
                for (int i = 0; i < jSONArray.length(); i++) {
                    JSONObject jObject = jSONArray.getJSONObject(i);
                    paymentid = StringUtil.DecodeText(jObject.getString("paymentID"));
                    int invoiceadvcndntypejson=!StringUtil.isNullOrEmpty(jObject.getString("invoiceadvcndntype"))?Integer.parseInt(jObject.getString("invoiceadvcndntype")):0;
                    paymentHashMap.put(invoiceadvcndntypejson, paymentid);
                }
                receipthm.put("paymentHashMap", paymentHashMap);
            }
            boolean ismanydbcr = StringUtil.getBoolean(request.getParameter("ismanydbcr"));
            receipthm.put("ismanydbcr", ismanydbcr);
            receipthm.put("receipttype", receiptType);
            receipthm.put("actualReceiptType", actualReceiptType);
            double bankCharges = 0;
            double bankInterest = 0;
            String accountId = request.getParameter("accid");
            String bankChargesAccid = request.getParameter("bankChargesCmb");
            String bankInterestAccid = request.getParameter("bankInterestCmb");
            boolean onlyAdvance = StringUtil.getBoolean(request.getParameter("onlyAdvance"));
            String createdby = sessionHandlerImpl.getUserid(request);
            String modifiedby = sessionHandlerImpl.getUserid(request);
            long createdon = System.currentTimeMillis();
            long updatedon = System.currentTimeMillis();
            String person = "";
            if (receiptType == 1) {
                person = " Against Customer Invoice ";
            }

            StringBuffer billno = new StringBuffer();
            if (!StringUtil.isNullOrEmpty(request.getParameter("bankCharges")) && !StringUtil.isNullOrEmpty(bankChargesAccid)) {
                bankCharges = Double.parseDouble(request.getParameter("bankCharges"));
                receipthm.put("bankCharges", bankCharges);
                receipthm.put("bankChargesCmb", bankChargesAccid);
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("bankInterest")) && !StringUtil.isNullOrEmpty(bankInterestAccid)) {
                bankInterest = Double.parseDouble(request.getParameter("bankInterest"));
                receipthm.put("bankInterest", bankInterest);
                receipthm.put("bankInterestCmb", bankInterestAccid);
            }
            String paidToid = request.getParameter("paidToCmb");
            if (!StringUtil.isNullOrEmpty(request.getParameter("paidToCmb")) && !StringUtil.isNullOrEmpty(paidToid)) {
                receipthm.put("paidToCmb", paidToid);
            }
            if (receiptType == 6) {
                receipthm.put("vendor", request.getParameter("accid"));
            } else {
                boolean isCustomer = false;
                KwlReturnObject custObj = accountingHandlerDAOobj.getObject(Customer.class.getName(), request.getParameter("accid"));
                if (custObj.getEntityList().get(0) != null) {
                    cust = (Customer) custObj.getEntityList().get(0);
                }
                if (cust != null) {
                    isCustomer = true;
                }

                boolean isVendor = false;
                KwlReturnObject vendObj = accountingHandlerDAOobj.getObject(Vendor.class.getName(), request.getParameter("accid"));
                if (vendObj.getEntityList().get(0) != null) {
                    vend = (Vendor) vendObj.getEntityList().get(0);
                }
                if (vend != null) {
                    isVendor = true;
                }

                if (isCustomer) {
                    receipthm.put("customerId", request.getParameter("accid"));
                } else if (isVendor) {
                    receipthm.put("vendor", request.getParameter("accid"));
                }
            }

            boolean bankReconsilationEntry = false;
            boolean bankPayment = false;
            Date clearanceDate = null, startDate = null, endDate = null;
            String bankAccountId = "";
            Map<String, Object> bankReconsilationMap = new HashMap<String, Object>();
            boolean editAdvance = false;
            Date creationDate = df.parse(request.getParameter("creationdate"));

            int countryLanguageId = Constants.OtherCountryLanguageId; // 0
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
            if (extraCompanyPreferences.isAmountInIndianWord()) {
                countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
            }

            if (!StringUtil.isNullOrEmpty(receiptid)) {
                KwlReturnObject receiptObj = accountingHandlerDAOobj.getObject(Receipt.class.getName(), receiptid);
                receipt = (Receipt) receiptObj.getEntityList().get(0);
                oldjeid = receipt.getJournalEntry().getID();
                JournalEntry jetemp = receipt.getJournalEntry();
                if (jetemp != null) {
                    jeentryNumber = jetemp.getEntryNumber();   //preserving these data to generate same JE number in edit case                    
                    jeautogenflag = jetemp.isAutoGenerated();
                    jeSeqFormatId = jetemp.getSeqformat() == null ? "" : jetemp.getSeqformat().getID();
                    jeIntegerPart = String.valueOf(jetemp.getSeqnumber());
                    jeDatePrefix = jetemp.getDatePreffixValue();
                    jeDatePrefix = jetemp.getDateAfterPreffixValue();
                    jeDateSuffix = jetemp.getDateSuffixValue();
                }
                if (receipt.getPayDetail() != null) {
                    payDetailID = receipt.getPayDetail().getID();
                    if (receipt.getPayDetail().getCard() != null) {
                        Cardid = receipt.getPayDetail().getCard().getID();
                    }
                    if (receipt.getPayDetail().getCheque() != null) {
                        Cardid = receipt.getPayDetail().getCheque().getID();
                    }
                }
                if (receipt != null) {
                    accReceivePaymentModuleServiceObj.updateOpeningBalance(receipt, companyid);
                    JSONObject params = new JSONObject();
                    accReceivePaymentModuleServiceObj.updateReceiptAdvancePaymentAmountDue(receipt, companyid,params);
                }
                result = accReceiptDAOobj.deleteReceiptDetails(receiptid, companyid);
                result = accReceiptDAOobj.deleteReceiptDetailsOtherwise(receiptid);
                if (receipt != null && receipt.getRevalJeId() != null) {
                    result = accJournalEntryobj.deleteJEDtails(receipt.getRevalJeId(), companyid);
                    result = accJournalEntryobj.deleteJE(receipt.getRevalJeId(), companyid);
                }
                receipthm.put("deposittojedetailid", null);
                receipthm.put("depositamount", 0.0);
                //Delete old entries and insert new entries again from optimized table in edit case.
                accJournalEntryobj.deleteOnEditAccountJEs_optimized(oldjeid);
            }

            synchronized (this) {
                SequenceFormat prevSeqFormat = null;
                String nextAutoNumber = "";
                String nextAutoNoInt = "";
                String datePrefix = "";
                String dateafterPrefix = "";
                String dateSuffix = "";
                String prevseqnumber = "";

                if (!StringUtil.isNullOrEmpty(receiptid)) {  // for edit case
                    String advanceId = receipt.getAdvanceid() != null ? receipt.getAdvanceid().getID() : "";
                        result = accReceiptDAOobj.getDuplicateForNormalReceipt(entryNumber, companyid, receiptid, advanceId,receipt);
                    int count = result.getRecordTotalCount();
                    if (count > 0 && sequenceformat.equals("NA")) {
                        throw new AccountingException(messageSource.getMessage("acc.field.Paymentnumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                    } else if (receipt != null && receipt.getSeqformat() != null) {
                        prevSeqFormat = receipt.getSeqformat();
                        prevseqnumber = receipt.getSeqnumber() + "";
                        receipthm.put(Constants.SEQFORMAT, prevSeqFormat.getID());
                        receipthm.put(Constants.SEQNUMBER, prevseqnumber);
                        receipthm.put(Constants.DATEPREFIX, receipt.getDatePreffixValue());
                        receipthm.put(Constants.DATEAFTERPREFIX, receipt.getDateAfterPreffixValue());
                        receipthm.put(Constants.DATESUFFIX, receipt.getDateSuffixValue());
                        nextAutoNumber = entryNumber;
                    }
                }else if(paymentHashMap.containsKey(3)&&!paymentHashMap.containsKey(1)){
                    String cndnId=paymentHashMap.get(3);
                    KwlReturnObject cndnresult = accountingHandlerDAOobj.getObject(Receipt.class.getName(), cndnId);
                    Receipt receiptCnDn=null;
                    int count=0;
                    if (!cndnresult.getEntityList().isEmpty() && cndnresult.getEntityList().get(0) != null) {
                        receiptCnDn = (Receipt) cndnresult.getEntityList().get(0);
                        result = accReceiptDAOobj.getDuplicateForNormalReceipt(entryNumber, companyid, receiptid, cndnId, receiptCnDn);
                        count = result.getRecordTotalCount();
                    }
                    if (count > 0 && sequenceformat.equals("NA")) {
                        throw new AccountingException(messageSource.getMessage("acc.field.Paymentnumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                    } else if(receiptCnDn!=null && receiptCnDn.getSeqformat()!=null){                             
                            prevSeqFormat=receiptCnDn.getSeqformat();
                            prevseqnumber=receiptCnDn.getSeqnumber()+"";
                            receipthm.put(Constants.SEQFORMAT, prevSeqFormat.getID());
                            receipthm.put(Constants.SEQNUMBER, prevseqnumber);
                            receipthm.put(Constants.DATEPREFIX, receiptCnDn.getDatePreffixValue());
                            receipthm.put(Constants.DATEAFTERPREFIX, receiptCnDn.getDateAfterPreffixValue());
                            receipthm.put(Constants.DATESUFFIX, receiptCnDn.getDateSuffixValue());
                            nextAutoNumber = entryNumber;                        
                    }
                 } else if (!ignoreDuplicateChk && (actualReceiptType != 0 || (actualReceiptType == 0 && isAdvancePayment))) {//true when advance created along with payment against invoice
                    if (actualReceiptType == 0 && isAdvancePayment && request.getParameter("data") != null) {
                        JSONArray jSONArray = new JSONArray(request.getParameter("data"));
                        JSONObject jSONObject = jSONArray.getJSONObject(0);
                        String advReceiptId = jSONObject.optString("billid", "");
                        if (!StringUtil.isNullOrEmpty(advReceiptId)) {
                            KwlReturnObject receiptObj = accountingHandlerDAOobj.getObject(Receipt.class.getName(), advReceiptId);
                            Receipt advreceipt = (Receipt) receiptObj.getEntityList().get(0);
                            if (advreceipt != null && advreceipt.getSeqformat() != null) {
                                prevSeqFormat = advreceipt.getSeqformat();
                                prevseqnumber = advreceipt.getSeqnumber() + "";
                                receipthm.put(Constants.SEQFORMAT, prevSeqFormat.getID());
                                receipthm.put(Constants.SEQNUMBER, prevseqnumber);
                                receipthm.put(Constants.DATEPREFIX, advreceipt.getDatePreffixValue());
                                receipthm.put(Constants.DATEAFTERPREFIX, advreceipt.getDateAfterPreffixValue());
                                receipthm.put(Constants.DATESUFFIX, advreceipt.getDateSuffixValue());
                                nextAutoNumber = entryNumber;

                                JournalEntry jetemp = advreceipt.getJournalEntry();
                                jeentryNumber = jetemp.getEntryNumber();   //preserving these data to generate same JE number in edit case                    
                                jeautogenflag = jetemp.isAutoGenerated();
                                jeSeqFormatId = jetemp.getSeqformat() == null ? "" : jetemp.getSeqformat().getID();
                                jeIntegerPart = String.valueOf(jetemp.getSeqnumber());
                                jeDatePrefix = jetemp.getDatePreffixValue();
                                jeDateAfterPrefix = jetemp.getDateAfterPreffixValue();
                                jeDateSuffix = jetemp.getDateSuffixValue();
                                editAdvance = true;
                            }
                        result = accReceiptDAOobj.getDuplicateForNormalReceipt(entryNumber, companyid, advReceiptId,"",advreceipt);
                        }
                    } else {
                        result = accReceiptDAOobj.getReceiptFromBillNo(entryNumber, companyid);
                    }
                    int count = result.getRecordTotalCount();
                    if (count > 0 && sequenceformat.equals("NA")) {
                        throw new AccountingException(messageSource.getMessage("acc.field.Paymentnumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                    }
                }
                if (sequenceformat.equals("NA")) {//In case of NA checks wheather this number can also be generated by a sequence format or not
                    List resultList = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Receive_Payment_ModuleId, entryNumber, companyid);
                    if (!resultList.isEmpty()) {
                        boolean isvalidEntryNumber = (Boolean) resultList.get(0);
                        String formatName = (String) resultList.get(1);
                        if (!isvalidEntryNumber) {
                            throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, RequestContextUtils.getLocale(request)) + " <b>" + entryNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, RequestContextUtils.getLocale(request)) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null, RequestContextUtils.getLocale(request)) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null, RequestContextUtils.getLocale(request)));
                        }
                    }
                }

                if (!sequenceformat.equals("NA") && prevSeqFormat == null && !ignoreDuplicateChk) { //to generate sequence number
                    boolean seqformat_oldflag = StringUtil.getBoolean(request.getParameter("seqformat_oldflag"));
                    if (seqformat_oldflag) {
                        nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_RECEIPT, sequenceformat);
                    } else {
                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_RECEIPT, sequenceformat, seqformat_oldflag, creationDate);
                        nextAutoNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                        nextAutoNoInt = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                        datePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                        dateafterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                        dateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                        receipthm.put(Constants.SEQFORMAT, sequenceformat);
                        receipthm.put(Constants.SEQNUMBER, nextAutoNoInt);
                        receipthm.put(Constants.DATEPREFIX, datePrefix);
                        receipthm.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
                        receipthm.put(Constants.DATESUFFIX, dateSuffix);
                    }
                    entryNumber = nextAutoNumber;
                }
                if (!sequenceformat.equals("NA") && ignoreDuplicateChk) {//case of creating advance with normal
                    result = accReceiptDAOobj.getCurrentSeqNumberForAdvance(sequenceformat, companyid);
                    nextAutoNoInt = !(result.getEntityList().isEmpty()) ? (result.getEntityList().get(0) + "") : "0";
                    receipthm.put(Constants.SEQNUMBER, nextAutoNoInt);
                    receipthm.put(Constants.SEQFORMAT, sequenceformat);
                    nextAutoNumber = entryNumber;
                }
                receipthm.put("entrynumber", entryNumber);
                receipthm.put("autogenerated", nextAutoNumber.equals(entryNumber));
            }

            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            String currencyid = (request.getParameter("currencyid") == null ? currency.getCurrencyID() : request.getParameter("currencyid"));

            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);


            receipthm.put("currencyid", currencyid);
            receipthm.put("externalCurrencyRate", externalCurrencyRate);
            KwlReturnObject payresult = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), request.getParameter("pmtmethod"));
            PaymentMethod payMethod = (PaymentMethod) payresult.getEntityList().get(0);

            dipositTo = payMethod.getAccount();
            HashMap pdetailhm = new HashMap();
            pdetailhm.put("paymethodid", payMethod.getID());
            pdetailhm.put("companyid", company.getCompanyID());
            if (payMethod.getDetailType() != PaymentMethod.TYPE_CASH) {

                JSONObject obj = new JSONObject(request.getParameter("paydetail"));
                if (payMethod.getDetailType() == PaymentMethod.TYPE_BANK) {
                    bankPayment = true;
                    bankReconsilationEntry = obj.getString("paymentStatus") != null ? obj.getString("paymentStatus").equals("Cleared") : false;
                    if (bankReconsilationEntry) {
                        bankAccountId = request.getParameter("bankaccid");
                        startDate = df.parse(request.getParameter("startdate"));
                        endDate = df.parse(request.getParameter("enddate"));
                        clearanceDate = df.parse(obj.getString("clearanceDate"));
                        bankReconsilationMap.put("bankAccountId", bankAccountId);
                        bankReconsilationMap.put("startDate", startDate);
                        bankReconsilationMap.put("endDate", endDate);
                        bankReconsilationMap.put("clearanceDate", clearanceDate);
                        bankReconsilationMap.put("endingAmount", 0.0);
                        bankReconsilationMap.put("companyId", companyid);
                    }
                    HashMap chequehm = new HashMap();
                    chequehm.put("chequeno", obj.optString("chequeno"));
                    chequehm.put("description", StringUtil.DecodeText(obj.optString("description")));
                    chequehm.put("bankname", StringUtil.DecodeText(obj.optString("bankname")));
                    chequehm.put("duedate", df.parse(obj.getString("payDate")));
                    chequehm.put("bankmasteritemid", obj.getString("bankmasteritemid"));
                    chequehm.put("companyId", companyid);
                    chequehm.put("bankAccount", (payMethod.getAccount() != null) ? payMethod.getAccount().getID() : "");
                    chequehm.put("createdFrom", 2);
                    Map<String, Object> seqchequehm = new HashMap<>();
                    obj.put(Constants.companyKey, companyid);
                    String chequesequenceformat =  obj.optString("sequenceformat");
                    String nextChequeNumber="";
                    /**
                     * getNextChequeNumber method to generate next sequence number using
                     * sequence format,also saving the dateprefix and datesuffix in cheque table.
                     */
                    if (!StringUtil.isNullOrEmpty(chequesequenceformat) && !chequesequenceformat.equals("NA")) {
                        seqchequehm = accCompanyPreferencesObj.getNextChequeNumber(obj);
                    }
                  
                    if (seqchequehm.containsKey(Constants.AUTO_ENTRYNUMBER)) {
                        chequehm.put("chequeno", (String) seqchequehm.get(Constants.AUTO_ENTRYNUMBER));
                    }
                    if (seqchequehm.containsKey(Constants.SEQNUMBER)) {
                        chequehm.put("sequenceNumber", (String) seqchequehm.get(Constants.SEQNUMBER));
                    }
                    if (seqchequehm.containsKey(Constants.DATEPREFIX)) {
                        chequehm.put(Constants.DATEPREFIX, (String) seqchequehm.get(Constants.DATEPREFIX));
                    }
                    if (seqchequehm.containsKey(Constants.DATEAFTERPREFIX)) {
                        chequehm.put(Constants.DATEAFTERPREFIX, (String) seqchequehm.get(Constants.DATEAFTERPREFIX));
                    }
                    if (seqchequehm.containsKey(Constants.DATESUFFIX)) {
                        chequehm.put(Constants.DATESUFFIX, (String) seqchequehm.get(Constants.DATESUFFIX));
                    }
                    KwlReturnObject cqresult = accPaymentDAOobj.addCheque(chequehm);
                    Cheque cheque = (Cheque) cqresult.getEntityList().get(0);
                    pdetailhm.put("chequeid", cheque.getID());
                } else if (payMethod.getDetailType() == PaymentMethod.TYPE_CARD) {
                    HashMap cardhm = new HashMap();
                    cardhm.put("cardno", obj.getString("cardno"));
                    cardhm.put("nameoncard", obj.getString("nameoncard"));
                    cardhm.put("expirydate", df.parse(obj.getString("expirydate")));
                    cardhm.put("cardtype", obj.getString("cardtype"));
                    cardhm.put("refno", obj.getString("refno"));
                    KwlReturnObject cdresult = accPaymentDAOobj.addCard(cardhm);
                    Card card = (Card) cdresult.getEntityList().get(0);
                    pdetailhm.put("cardid", card.getID());
                }
            }
            KwlReturnObject pdresult = null;
            if (!StringUtil.isNullOrEmpty(receiptid) && !StringUtil.isNullOrEmpty(payDetailID)) {
                pdetailhm.put("paydetailid", payDetailID);
            }
            pdresult = accPaymentDAOobj.addPayDetail(pdetailhm);
            PayDetail pdetail = (PayDetail) pdresult.getEntityList().get(0);
            receipthm.put("paydetailsid", pdetail.getID());

            receipthm.put("memo", request.getParameter("memo"));
            receipthm.put("companyid", company.getCompanyID());

            Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
            if (StringUtil.isNullOrEmpty(oldjeid) && !editAdvance) {
                synchronized (this) {
                    HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                    JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                    JEFormatParams.put("modulename", CompanyPreferencesConstants.AUTOJOURNALENTRY);
                    JEFormatParams.put("companyid", companyid);
                    JEFormatParams.put("isdefaultFormat", true);

                    KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                    SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, creationDate);
                    jeentryNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    jeIntegerPart = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                    jeDatePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                    jeDateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                    jeSeqFormatId = format.getID();
                    jeautogenflag = true;
                }
            }
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
            jeDataMap.put("currencyid", currencyid);
            HashSet jedetails = new HashSet();
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            jeid = journalEntry.getID();
            jeDataMap.put("jeid", jeid);
            String detail = request.getParameter("detail");
            JSONArray jArr = new JSONArray();
            if (!StringUtil.isNullOrEmpty(detail)) {
                jArr = new JSONArray(detail);
            }
            if (jArr.length() > 0 && !isMultiDebit) {
                amount = 0;
                for (int i = 0; i < jArr.length(); i++) { // Changed for New Customer/Vendor Removed account dependency
                    JSONObject jobj = jArr.getJSONObject(i);
                    JSONArray jArray = new JSONArray();
                    jArray.put(jobj);
                    double amountDiffforInv = oldReceiptRowsAmount(request, jArray, currencyid, externalCurrencyRate);
                    rateDecreased = false;
                    if (amountDiffforInv < 0) {
                        rateDecreased = true;
                    }
                    amount += jobj.getDouble("payment");
                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", jobj.getDouble("payment") - amountDiffforInv);
                    jedjson.put("accountid", jobj.get("accountid"));
                    jedjson.put("debit", false);
                    jedjson.put("jeid", jeid);
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                    jArr.getJSONObject(i).put("rowjedid", jed.getID());

                    if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", ""))) {
                        JSONArray jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
                        Map1.put(jed.getID(), jcustomarray);
                    }
                    if ((!jobj.getString("billno").equalsIgnoreCase("undefined")) && (!jobj.getString("billno").equalsIgnoreCase(""))) {
                        billno.append(jobj.getString("billno") + ",");
                    }
                }

                amountDiff = oldReceiptRowsAmount(request, jArr, currencyid, externalCurrencyRate);

                if (preferences.getForeignexchange() == null) {
                    throw new AccountingException(messageSource.getMessage("acc.receipt.forex", null, RequestContextUtils.getLocale(request)));
                }
                if (amountDiff != 0 && preferences.getForeignexchange() != null && Math.abs(amountDiff) >= 0.000001) {//Math.abs(amountDiff) < .0000001 Added this because in case of revaluation the diff in the rates was less than .0000001 and the amount calculation was going in Exachange rate account. 
                    if (amountDiff < 0) {
                        rateDecreased = true;
                    }
                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", rateDecreased ? (-1 * amountDiff) : amountDiff);
                    jedjson.put("accountid", preferences.getForeignexchange().getID());
                    jedjson.put("debit", rateDecreased ? true : false);
                    jedjson.put("jeid", jeid);
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                }

            } else {
                amount = Double.parseDouble(request.getParameter("amount"));
            }
            JSONObject jedjson = null;
            KwlReturnObject jedresult = null;
            JournalEntryDetail jed = null;
            JournalEntryDetail JEdetailID = null;
            boolean taxExist = false;
            List receiptOtherwiseList = new ArrayList();
            HashMap receiptdetailotherwise = new HashMap();
            if (isMultiDebit) {
                JSONArray drAccArr = new JSONArray(drAccDetails);
                for (int i = 0; i < drAccArr.length(); i++) {
                    JSONObject jobj = drAccArr.getJSONObject(i);
                    boolean isdebit = jobj.has("isdebit") ? Boolean.parseBoolean(jobj.getString("isdebit")) : false;
                    int advanceamounttype=request.getParameter("advanceAmountType") != null ? Integer.parseInt(request.getParameter("advanceAmountType")) : 0;
                    if(isAdvancePayment && advanceamounttype > 0 && company.getCountry().getID().equals("137")){ //if advance and Singapoor Country
                        double totalAmount=Double.parseDouble(jobj.getString("dramount"));
                        double typeAmountTax=0;
                        String accountID="";
                        if(advanceamounttype ==1){//for Local Tax
                            double percentage=6.0;
                             KwlReturnObject ObjReturnObject = accAccountDAOobj.getAccountFromName(companyid, "SR");
                             Account account =(Account)ObjReturnObject.getEntityList().get(0);
                             if(account!=null)accountID=account.getID();
                              typeAmountTax=(totalAmount*percentage)/(100+percentage);
                              typeAmountTax=authHandler.roundUnitPrice(typeAmountTax,companyid);
                        }else if(advanceamounttype ==2){//For Export Tax
                            
                            KwlReturnObject ObjReturnObject = accAccountDAOobj.getAccountFromName(companyid, "ZRE");
                            Account account =(Account)ObjReturnObject.getEntityList().get(0);
                            if(account!=null)accountID=account.getID();
                            typeAmountTax=0;
                            typeAmountTax=authHandler.roundUnitPrice(typeAmountTax,companyid);
                        }
                        jedjson = new JSONObject();
                        jedjson.put("srno", jedetails.size() + 1);
                        jedjson.put("companyid", companyid);
                        jedjson.put("amount",(totalAmount-typeAmountTax));
                        jedjson.put("accountid", jobj.getString("accountid"));
                        jedjson.put("debit", isdebit);//false);
                        jedjson.put("jeid", jeid);
                        jedjson.put("description",URLEncoder.encode(jobj.getString("description"),StaticValues.ENCODING));
                        jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        JEdetailID = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jedetails.add(JEdetailID);
                        
                        jedjson = new JSONObject();
                        jedjson.put("srno", jedetails.size() + 1);
                        jedjson.put("companyid", companyid);
                        jedjson.put("amount", typeAmountTax);
                        jedjson.put("accountid", !StringUtil.isNullOrEmpty(accountID)?accountID:jobj.getString("accountid"));
                        jedjson.put("debit", isdebit);//false);
                        jedjson.put("jeid", jeid);
                        jedjson.put("description", URLEncoder.encode(jobj.getString("description"),StaticValues.ENCODING));
                        jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        JEdetailID = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jedetails.add(JEdetailID);
                        
                    }else{//Normal Flow
                        
                        jedjson = new JSONObject();
                        jedjson.put("srno", jedetails.size() + 1);
                        jedjson.put("companyid", companyid);
                        jedjson.put("amount", Double.parseDouble(jobj.getString("dramount")));
                        jedjson.put("accountid", jobj.getString("accountid"));
                        jedjson.put("debit", isdebit);//false);
                        jedjson.put("jeid", jeid);
                        jedjson.put("description", URLEncoder.encode(jobj.getString("description"),StaticValues.ENCODING));
                        jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        JEdetailID = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jedetails.add(JEdetailID);
                        
                    }
                    if ((!jobj.getString("billno").equalsIgnoreCase("undefined")) && (!jobj.getString("billno").equalsIgnoreCase(""))) {
                            billno.append(jobj.getString("billno") + ",");
                     }                    
                    double rowtaxamount = 0;
                    //taxExist=StringUtil.isNullOrEmpty(rowtaxid)?false:true;
                    if (receiptType == 2 || receiptType == 9) {//Otherwise for receive Payment
                        ReceiptDetailOtherwise receiptDetailOtherwise = null;
                        String rowtaxid = jobj.getString("prtaxid");
                        KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), rowtaxid); // (Tax)session.get(Tax.class, taxid);
                        Tax rowtax = (Tax) txresult.getEntityList().get(0);
                        if (rowtax == null || rowtaxid.equalsIgnoreCase("-1")) {
                            receiptdetailotherwise.put("amount", Double.parseDouble(jobj.getString("dramount")));
                            receiptdetailotherwise.put("taxjedid", "");
                            receiptdetailotherwise.put("tax", rowtaxid.equalsIgnoreCase("-1") ? "None" : "");
                            receiptdetailotherwise.put("accountid", jobj.getString("accountid"));
                            receiptdetailotherwise.put("isdebit", isdebit);
                            receiptdetailotherwise.put("taxamount", rowtaxamount);
                            receiptdetailotherwise.put("description",URLEncoder.encode(jobj.getString("description"),StaticValues.ENCODING));
                            result = accReceiptDAOobj.saveReceiptDetailOtherwise(receiptdetailotherwise);
                            receiptdetailotherwise.clear();
//                                    ReceiptDetailOtherwise receiptDetailOtherwise=null;
                            receiptDetailOtherwise = (ReceiptDetailOtherwise) result.getEntityList().get(0);
                            receiptOtherwiseList.add(receiptDetailOtherwise.getID());
                        } else {
                            rowtaxamount = Double.parseDouble(jobj.getString("taxamount"));
                            jedjson = new JSONObject();
                            jedjson.put("srno", jedetails.size() + 1);
                            jedjson.put("companyid", company.getCompanyID());
                            jedjson.put("amount", authHandler.formattedAmount(rowtaxamount, companyid));
                            jedjson.put("accountid", rowtax.getAccount().getID());
                            jedjson.put("debit", isdebit);//false);
                            jedjson.put("jeid", jeid);
                            jedjson.put("description", URLEncoder.encode(jobj.getString("description"),StaticValues.ENCODING));
                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jedetails.add(jed);
                            receipthm.put("taxentryid", jed.getID());

                            receiptdetailotherwise.put("amount", Double.parseDouble(jobj.getString("dramount")));
                            receiptdetailotherwise.put("taxjedid", jed.getID());
                            receiptdetailotherwise.put("tax", rowtax.getID());
                            receiptdetailotherwise.put("accountid", jobj.getString("accountid"));
                            receiptdetailotherwise.put("isdebit", isdebit);
                            receiptdetailotherwise.put("taxamount", rowtaxamount);
                            receiptdetailotherwise.put("description",URLEncoder.encode(jobj.getString("description"),StaticValues.ENCODING));
                            result = accReceiptDAOobj.saveReceiptDetailOtherwise(receiptdetailotherwise);
                            receiptdetailotherwise.clear();
                            receiptDetailOtherwise = (ReceiptDetailOtherwise) result.getEntityList().get(0);
                            receiptOtherwiseList.add(receiptDetailOtherwise.getID());
                            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                            if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", ""))) {
                                JSONArray jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
                                customrequestParams.put("customarray", jcustomarray);
                                customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                                customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                                customrequestParams.put("modulerecid", jed.getID());
                                customrequestParams.put("recdetailId", receiptDetailOtherwise.getID());
                                customrequestParams.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
                                customrequestParams.put("companyid", companyid);
                                customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                    JSONObject tempJObj = new JSONObject();
                                    tempJObj.put("accjedetailcustomdata", jed.getID());
                                    tempJObj.put("jedid", jed.getID());
                                    jedresult = accJournalEntryobj.updateJournalEntryDetails(tempJObj);

                                }
                            }
                        }

                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", ""))) {
                            JSONArray jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
                            customrequestParams.put("customarray", jcustomarray);
                            customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                            customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                            customrequestParams.put("modulerecid", JEdetailID.getID());
                            customrequestParams.put("recdetailId", receiptDetailOtherwise.getID());
                            customrequestParams.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
                            customrequestParams.put("companyid", companyid);
                            customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                JSONObject tempJObj = new JSONObject();
                                tempJObj.put("accjedetailcustomdata", JEdetailID.getID());
                                tempJObj.put("jedid", JEdetailID.getID());
                                jedresult = accJournalEntryobj.updateJournalEntryDetails(tempJObj);

                            }
                        }


                    }
                }
            }

            if (bankCharges != 0) {
                if (!isAdvancePayment || (isAdvancePayment && onlyAdvance)) {
                    amount -= bankCharges;
                    jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    //(If currency USD and base currency SGD, inv rate 0.81 n pay rate 0.80 then amount = 1234.57, amountDiff = -15.24, depositAmt = 1250 SGD)
                    jedjson.put("amount", bankCharges);
                    jedjson.put("accountid", bankChargesAccid);
                    jedjson.put("debit", true);
                    jedjson.put("jeid", jeid);
                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                }
            }

            if (bankInterest != 0) {
                if (!isAdvancePayment || (isAdvancePayment && onlyAdvance)) {
                    amount -= bankInterest;
                    jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    //(If currency USD and base currency SGD, inv rate 0.81 n pay rate 0.80 then amount = 1234.57, amountDiff = -15.24, depositAmt = 1250 SGD)
                    jedjson.put("amount", bankInterest);
                    jedjson.put("accountid", bankInterestAccid);
                    jedjson.put("debit", true);
                    jedjson.put("jeid", jeid);
                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                }
            }

            if (amount != 0) {
                jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put("companyid", companyid);
                //(If currency USD and base currency SGD, inv rate 0.81 n pay rate 0.80 then amount = 1234.57, amountDiff = -15.24, depositAmt = 1250 SGD)
                jedjson.put("amount", amount);
                jedjson.put("accountid", dipositTo.getID());
                jedjson.put("debit", true);
                jedjson.put("jeid", jeid);
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jedetails.add(jed);

                receipthm.put("deposittojedetailid", jed.getID());
                receipthm.put("depositamount", amount);
            }
            jeDataMap.put("jedetails", jedetails);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);

            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                customrequestParams.put("modulerecid", journalEntry.getID());
                customrequestParams.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    jeDataMap.put("accjecustomdataref", journalEntry.getID());
                    jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);
                }
            }

            receipthm.put("journalentryid", journalEntry.getID());
            if (!StringUtil.isNullOrEmpty(receiptid)) {
                receipthm.put("receiptid", receipt.getID());
            }

            result = accReceiptDAOobj.saveReceipt(receipthm);
            receipt = (Receipt) result.getEntityList().get(0);
            receipthm.put("receiptid", receipt.getID());

            HashSet receiptDetails = new HashSet();
            if (!isMultiDebit) {
                receiptDetails = saveReceiptRows(receipt, company, jArr, greceipt);
            }
            receipthm.put("receiptdetails", receiptDetails);
            receipthm.put("createdby", createdby);
            receipthm.put("modifiedby", modifiedby);
            receipthm.put("createdon", createdon);
            receipthm.put("updatedon", updatedon);

            result = accReceiptDAOobj.saveReceipt(receipthm);
            Iterator itr1 = receipt.getRows().iterator();
            while (itr1.hasNext()) {
                ReceiptDetail payd = (ReceiptDetail) itr1.next();

                JSONArray jcustomarray = Map1.get(payd.getROWJEDID());
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                customrequestParams.put("modulerecid", payd.getROWJEDID());
                customrequestParams.put("recdetailId", payd.getID());
                customrequestParams.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    JSONObject tempJObj = new JSONObject();
                    tempJObj.put("accjedetailcustomdata", payd.getROWJEDID());
                    tempJObj.put("jedid", payd.getROWJEDID());
                    jedresult = accJournalEntryobj.updateJournalEntryDetails(tempJObj);

                }
            }
            receipt = (Receipt) result.getEntityList().get(0);
            if (receiptType == 2 || receiptType == 9) {
                for (int i = 0; i < receiptOtherwiseList.size(); i++) {
                    receiptdetailotherwise.put("receipt", receipt.getID());
                    receiptdetailotherwise.put("receiptotherwise", receiptOtherwiseList.get(i));
                    result = accReceiptDAOobj.saveReceiptDetailOtherwise(receiptdetailotherwise);
                    receiptdetailotherwise.clear();
                }
            }
            if (bankReconsilationEntry) {
                bankReconsilationMap.put("clearingamount", amount);
                bankReconsilationMap.put("currencyid", currencyid);
                bankReconsilationMap.put("details", jArr);
                bankReconsilationMap.put("ismultidebit", isMultiDebit);
                bankReconsilationMap.put("receipt", receipt);
                if (!StringUtil.isNullOrEmpty(oldjeid)) {
                    bankReconsilationMap.put("oldjeid", oldjeid);
                }
                HashMap<String, Object> globalParams = AccountingManager.getGlobalParams(request);
                saveBankReconsilation(bankReconsilationMap, globalParams);
            }
            if (bankPayment && !bankReconsilationEntry && !StringUtil.isNullOrEmpty(oldjeid)) {
                bankReconsilationMap.put("oldjeid", oldjeid);
                bankReconsilationMap.put("companyId", companyid);
                deleteBankReconcilation(bankReconsilationMap);
            }

            if (isAdvancePayment && !StringUtil.isNullOrEmpty(request.getParameter("mainpaymentid"))) {//Link advance payments id with main payment id
                receipthm.clear();
                receipthm.put("receiptid", request.getParameter("mainpaymentid"));
                receipthm.put("advanceid", receipt.getID());
                receipthm.put("advanceamount", request.getParameter("advanceamt") != null ? Double.parseDouble(request.getParameter("advanceamt")) : 0);
                receipthm.put("advanceamounttype", !StringUtil.isNullOrEmpty((String)request.getParameter("advanceAmountType")) ? Integer.parseInt(request.getParameter("advanceAmountType")) : 0);
                result = accReceiptDAOobj.saveReceipt(receipthm);
            }

            if (isCNDN) {
                String AccDetailsarrStr = request.getParameter("detailForCNDN");
                JSONArray drAccArr = new JSONArray(AccDetailsarrStr);
                
                 if (!isAgainstDN) {
                     String paymentId=receipt.getID();
                     KwlReturnObject cnhistoryresult = accReceiptDAOobj.getCustomerDnPaymenyHistory("", 0.0, 0.0, paymentId);
                        List<DebitNotePaymentDetails> dnHistoryList=cnhistoryresult.getEntityList();
                         for (DebitNotePaymentDetails dnpd:dnHistoryList) {  
                            String dnnoteid = dnpd.getDebitnote().getID()!=null?dnpd.getDebitnote().getID():"";
                            Double dnpaidamount=dnpd.getAmountPaid();
                            KwlReturnObject cnjedresult = accReceiptDAOobj.updateDnAmount(dnnoteid, -dnpaidamount);
                            KwlReturnObject opencnjedresult = accReceiptDAOobj.updateDnOpeningAmountDue(dnnoteid, -dnpaidamount);
                         }
                }
                
                for (int i = 0; i < drAccArr.length(); i++) {
                    JSONObject jobj = drAccArr.getJSONObject(i);
                    jedjson = new JSONObject();
                    double paidncamount = Double.parseDouble(jobj.getString("payment"));
                    double amountdue = Double.parseDouble(jobj.getString("amountdue"));
                    String dnnoteid = jobj.getString("noteid");
                    String paymentId = receipt.getID();
                    if ((!jobj.getString("noteno").equalsIgnoreCase("undefined")) && (!jobj.getString("noteno").equalsIgnoreCase(""))) {
                        billno.append(jobj.getString("noteno") + ",");
                    }
                    person=" Against debit note ";
                    if (isAgainstDN) {
                        KwlReturnObject cnjedresult = accReceiptDAOobj.updateCnAmount(dnnoteid, paidncamount);
                        KwlReturnObject cnopeningjedresult = accReceiptDAOobj.updateCnOpeningAmountDue(dnnoteid, paidncamount);
                        cnjedresult = accReceiptDAOobj.saveCustomerCnPaymenyHistory(dnnoteid, paidncamount, amountdue, paymentId);
                    } else {
                        KwlReturnObject cnjedresult = accReceiptDAOobj.updateDnAmount(dnnoteid, paidncamount);
                        KwlReturnObject cnopeningjedresult = accReceiptDAOobj.updateDnOpeningAmountDue(dnnoteid, paidncamount);
                        cnjedresult = accReceiptDAOobj.saveCustomerDnPaymenyHistory(dnnoteid, paidncamount, amountdue, paymentId);
                    }
                }
            }

            //Insert new entries again in optimized table.
            accJournalEntryobj.saveAccountJEs_optimized(jeid);

            netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(amount)), currency,countryLanguageId);
            String action = "made";
            boolean isEdit = StringUtil.isNullOrEmpty(request.getParameter("isEdit")) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
            boolean isCopy = StringUtil.isNullOrEmpty(request.getParameter("isCopyReceipt")) ? false : Boolean.parseBoolean(request.getParameter("isCopyReceipt"));
            if (isEdit == true && isCopy == false) {
                action = "updated";
            }
            if (billno.length() > 0) {
                billno.deleteCharAt(billno.length() - 1);
            }
            auditTrailObj.insertAuditLog(AuditAction.RECEIPT_ADDED, "User "+sessionHandlerImpl.getUserFullName(request) +" has "+action+" a receipt "+receipt.getReceiptNumber()+person+billno , request, receipt.getID());
            if (jArr.length() > 0 && !isMultiDebit) {
                double finalAmountReval = 0;
                String basecurrency = sessionHandlerImpl.getCurrencyID(request);
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);
                    double ratio = 0;
                    double amountReval = 0;
                    Date tranDate = null;
                    double exchangeRate = 0.0;
                    double exchangeRateReval = 0.0;
                    //boolean isRealised=false;
                    double amountdue = jobj.getDouble("payment");
                    double exchangeratefortransaction = jobj.optDouble("exchangeratefortransaction", 1.00);
                    HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
                    GlobalParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
                    GlobalParams.put("gcurrencyid", basecurrency);
                    GlobalParams.put("dateformat", authHandler.getDateOnlyFormat(request));
                    result = accountingHandlerDAOobj.getObject(Invoice.class.getName(), jobj.getString("billid"));
                    Invoice invoice = (Invoice) result.getEntityList().get(0);
                    boolean isopeningBalanceInvoice = invoice.isIsOpeningBalenceInvoice();
                    tranDate = invoice.getCreationDate();
                    if (!invoice.isNormalInvoice()) {
                        exchangeRate = invoice.getExchangeRateForOpeningTransaction();
                        exchangeRateReval = exchangeRate;
                    } else {
                        exchangeRate = invoice.getJournalEntry().getExternalCurrencyRate();
                        exchangeRateReval = exchangeRate;
//                        tranDate = invoice.getJournalEntry().getEntryDate();
                    }

                    HashMap<String, Object> invoiceId = new HashMap<String, Object>();
                    invoiceId.put("invoiceid", invoice.getID());
                    invoiceId.put("companyid", sessionHandlerImpl.getCompanyid(request));
                    invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
                    //Checking the document entery in revalution history if any for current rate
                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    RevaluationHistory revalueationHistory = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (revalueationHistory != null) {
                        exchangeRateReval = revalueationHistory.getEvalrate();
                    }

                    result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrency);
                    currency = (KWLCurrency) result.getEntityList().get(0);
                    String currid = currency.getCurrencyID();
                    if (invoice.getCurrency() != null) {
                        currid = invoice.getCurrency().getCurrencyID();
                    }
                    //            double oldrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, gr.getJournalEntry().getEntryDate());
                    KwlReturnObject bAmt = null;
                    if (isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                        bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, tranDate, exchangeRate);
                    } else {
                        bAmt = accCurrencyobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, tranDate, exchangeRate);
                    }

                    double oldrate = (Double) bAmt.getEntityList().get(0);
                    //            double newrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
                    if (revalueationHistory == null && isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                        bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
                    } else {
                        bAmt = accCurrencyobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
                    }

                    double newrate = (Double) bAmt.getEntityList().get(0);
                    ratio = oldrate - newrate;
                    if (Math.abs(exchangeratefortransaction - newrate) <= 0.000001) {
                        exchangeratefortransaction = newrate;
                    }
                    double amountdueNew = amountdue / exchangeratefortransaction;
                    amountdueNew = Math.round(amountdueNew * 1000) / 1000d;
                    amountReval = ratio * amountdueNew;
                    finalAmountReval = finalAmountReval + amountReval;
                }
                if (finalAmountReval != 0) {
                    Map<String,Object> counterMap=new HashMap<>();
                    counterMap.put("counter", 0);
                    JSONObject requestObj = convertRequestToJSONObjectForLinkPayment(request);
                    /**
                     * added transactionID and transactionModuleID to Realised
                     * JE.
                     */
                    counterMap.put("transactionModuleid", receipt.isIsOpeningBalenceReceipt() ? Constants.Acc_opening_Receipt : Constants.Acc_Receive_Payment_ModuleId);
                    counterMap.put("transactionId", receipt.getID());
                    String revaljeid = PostJEFORReevaluation(requestObj, finalAmountReval, companyid, preferences, basecurrency,null,counterMap);
                    receipthm.clear();
                    receipthm.put("receiptid", receipt.getID());
                    receipthm.put("revalJeId", revaljeid);
                    result = accReceiptDAOobj.saveReceipt(receipthm);

                }
            }
        } catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveReceipt : " + ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveReceipt : " + ex.getMessage(), ex);
        }
        ll.add(new String[]{oldjeid, Cardid});
        KwlReturnObject accresult = accountingHandlerDAOobj.getObject(Account.class.getName(), request.getParameter("accid"));
        Account account = (Account) accresult.getEntityList().get(0);
        String accountName = "";
        String accountaddress = "";
        String customerName = "";
        String accountid = request.getParameter("accid");
        result = accReceiptDAOobj.getaccountdetailsReceipt(accountid);
        if (result.getRecordTotalCount() > 0) {
            Customer customer = (Customer) result.getEntityList().get(0);
            accountaddress = customer.getBillingAddress();
            customerName = customer.getName();
        }
        if (account != null) {
            accountName = account.getName();
        }
        ll.add(new String[]{"amount", String.valueOf(amount)});
        ll.add(new String[]{"amountinword", netinword});
        ll.add(new String[]{"accountName", customerName});
        ll.add(receipt.getID());
        ll.add(receipt.getReceiptNumber());
        ll.add(String.valueOf(receipt.getAdvanceamount()));
        ll.add(accountaddress);
        ll.add(accountName);
        ll.add(receipt.getJournalEntry().getEntryNumber());
        ll.add(receipt.getReceipttype());
//       ll.add(paymentmethod);
        return (ArrayList) ll;
    }

    public String PostJEFORReevaluation(JSONObject requestObj, double finalAmountReval, String companyid, CompanyAccountPreferences preferences, String basecurrency,String oldRevaluationJE, Map<String,Object> counterMap) {
        String jeid = "";
        try {
            String jeentryNumber = "";
            String jeSeqFormatId = "";
            String jeIntegerPart = "";
            String jeDatePrefix = "";
            String jeDateAfterPrefix = "";
            String jeDateSuffix = "";
            boolean jeautogenflag = false;
            DateFormat df = (DateFormat)requestObj.opt("dateonlyformat");
            /**
             * added Link Date to Realised JE. while link Advanced payment to
             * Reevaluated Invoice. Use 'linkingdateString'
             */
            String creationDate = !StringUtil.isNullOrEmpty(requestObj.optString("linkingdateString","")) ? requestObj.optString("linkingdateString","") : requestObj.optString("creationdate");
            Date entryDate = StringUtil.isNullOrEmpty(creationDate)? new Date():df.parse(creationDate);
            int counter=(Integer) counterMap.get("counter");
            synchronized (this) {
                HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                JEFormatParams.put("modulename", CompanyPreferencesConstants.AUTOJOURNALENTRY);
                JEFormatParams.put("companyid", companyid);
                JEFormatParams.put("isdefaultFormat", true);

                KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, entryDate);
                jeautogenflag = true;
                if(StringUtil.isNullOrEmpty(oldRevaluationJE)){
                    String nextAutoNoTemp = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    int sequence = Integer.parseInt((String)seqNumberMap.get(Constants.SEQNUMBER));
                    jeDatePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                    jeDateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                    sequence = sequence + counter;
                    String number = "" + sequence;
                    String action = "" + (sequence - counter);
                    nextAutoNoTemp.replaceAll(action, number);
                    jeentryNumber = nextAutoNoTemp.replaceAll(action, number);  //next auto generated number
                    jeSeqFormatId = format.getID();
                    jeIntegerPart = String.valueOf(sequence);
                    jeautogenflag = true;
                    counter++;
                    counterMap.put("counter", counter);
                }else if (!StringUtil.isNullOrEmpty(oldRevaluationJE)) {
                    KwlReturnObject result = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), oldRevaluationJE);
                    JournalEntry entry = (JournalEntry) result.getEntityList().get(0);
                    jeid = entry.getID();
                    jeentryNumber = entry.getEntryNumber();
                    jeSeqFormatId = entry.getSeqformat().getID();
                    jeIntegerPart = String.valueOf(entry.getSeqnumber());
                    jeDatePrefix = entry.getDatePreffixValue();
                    jeDateAfterPrefix = entry.getDateAfterPreffixValue();
                    jeDateSuffix = entry.getDateSuffixValue();
                    result = accJournalEntryobj.deleteJEDtails(oldRevaluationJE, companyid);
                    result = accJournalEntryobj.deleteJE(oldRevaluationJE, companyid);
                }
            }
            boolean creditDebitFlag = true;
            if (finalAmountReval < 0) {
                finalAmountReval = -(finalAmountReval);
                creditDebitFlag = false;
            }
            
//            Map<String, Object> jeDataMapReval = StringUtil.jsonToMap(requestObj.optJSONObject("globalparams"));
            Map<String, Object> jeDataMapReval = AccountingManager.getGlobalParamsJson(requestObj.optJSONObject("paramJobj"));
            jeDataMapReval.put("entrynumber", jeentryNumber);
            jeDataMapReval.put("autogenerated", jeautogenflag);
            jeDataMapReval.put(Constants.SEQFORMAT, jeSeqFormatId);
            jeDataMapReval.put(Constants.SEQNUMBER, jeIntegerPart);
            jeDataMapReval.put(Constants.DATEPREFIX, jeDatePrefix);
            jeDataMapReval.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
            jeDataMapReval.put(Constants.DATESUFFIX, jeDateSuffix);
            jeDataMapReval.put("entrydate",entryDate);
            jeDataMapReval.put("companyid", companyid);
            //jeDataMapReval.put("memo", "Realised Gain/Loss");
            jeDataMapReval.put("currencyid", basecurrency);
            jeDataMapReval.put("isReval", 2);
            jeDataMapReval.put("transactionModuleid", counterMap.containsKey("transactionModuleid") ? counterMap.get("transactionModuleid") : 0);
            jeDataMapReval.put("transactionId", counterMap.get("transactionId"));
            Set jedetailsReval = new HashSet();
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMapReval);//Create Journal entry without JEdetails
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            jeid = journalEntry.getID();
            jeDataMapReval.put("jeid", jeid);
            JSONObject jedjsonreval = new JSONObject();
            jedjsonreval.put("srno", jedetailsReval.size() + 1);
            jedjsonreval.put("companyid", companyid);
            jedjsonreval.put("amount", finalAmountReval);//rateDecreased?(-1*amountDiff):
            jedjsonreval.put("accountid", preferences.getForeignexchange().getID());
            jedjsonreval.put("debit", creditDebitFlag ? true : false);
            jedjsonreval.put("jeid", jeid);
            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjsonreval);
            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetailsReval.add(jed);
            /*
             * Featching Custom field/Dimension Data from Company prefrences.
             */
            String customfield = "";
            String lineleveldimensions = "";
            KwlReturnObject result = accJournalEntryobj.getRevaluationJECustomData(companyid);
            RevaluationJECustomData revaluationJECustomData = (result != null && result.getEntityList().size() > 0  && result.getEntityList().get(0) != null) ? (RevaluationJECustomData) result.getEntityList().get(0) : null;
            if (revaluationJECustomData != null) {
                customfield = revaluationJECustomData.getCustomfield();
                lineleveldimensions = revaluationJECustomData.getLineleveldimensions();
            }

            /*
             * Make dimensions entry
             */
            setDimensionForRevalJEDetail(lineleveldimensions, jed);
            String unrealised_accid = "";
            if (preferences.getUnrealisedgainloss() != null) {
                unrealised_accid = preferences.getUnrealisedgainloss().getID();
            } else {
                Locale locale = (Locale) requestObj.opt("locale");
                throw new AccountingException(messageSource.getMessage("acc.field.NoUnrealisedGain/Lossaccountfound", null, locale));
            }
            jedjsonreval = new JSONObject();
            jedjsonreval.put("companyid", companyid);
            jedjsonreval.put("srno", jedetailsReval.size() + 1);
            jedjsonreval.put("amount", finalAmountReval);
            jedjsonreval.put("accountid", unrealised_accid);
            jedjsonreval.put("debit", creditDebitFlag ? false : true);
            jedjsonreval.put("jeid", jeid);
            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjsonreval);
            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetailsReval.add(jed);
            /*
             * Make dimensions entry
             */
            setDimensionForRevalJEDetail(lineleveldimensions, jed);
                       
            jeDataMapReval.put("jedetails", jedetailsReval);
            jeDataMapReval.put("externalCurrencyRate", 0.0);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMapReval);
            /*
             * Make custom field entry
             */
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                customrequestParams.put("modulerecid", jeid);
                customrequestParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    Map<String, Object> customjeDataMap = new HashMap<>();
                    customjeDataMap.put("accjecustomdataref", jeid);
                    customjeDataMap.put("jeid", jeid);
                    customjeDataMap.put("istemplate", journalEntry.getIstemplate());
                    customjeDataMap.put("isReval", journalEntry.getIsReval());
                    accJournalEntryobj.updateCustomFieldJournalEntry(customjeDataMap);
                }
            }
        
        } catch (Exception ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jeid;
    }

    /**
     * Description :This method is used to save Dimension For Reval JEDetail
     */
    public void setDimensionForRevalJEDetail(String lineleveldimensions, JournalEntryDetail jed) {
        try {
            if (!StringUtil.isNullOrEmpty(lineleveldimensions)) {
                JSONArray jcustomarray = new JSONArray(lineleveldimensions);
                HashMap<String, Object> customrequestParams = new HashMap<>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                customrequestParams.put("modulerecid", jed.getID());
                customrequestParams.put("recdetailId", jed.getID());
                customrequestParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                customrequestParams.put("companyid", jed.getCompany().getCompanyID());
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    //jed.setAccJEDetailCustomData((AccJEDetailCustomData) hibernateTemplate.get(AccJEDetailCustomData.class, jed.getID()));
                    JSONObject jedjsonreval = new JSONObject();
                    jedjsonreval.put("accjedetailcustomdata", jed.getID());
                    jedjsonreval.put("jedid", jed.getID());
                    accJournalEntryobj.updateJournalEntryDetails(jedjsonreval);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accReceiptControllerNew.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public ModelAndView saveContraReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        String JENumBer = "";
        String billno = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("BR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            List li = saveContraReceipt(request);
            if (li.get(1) != null) {
                billno = li.get(1).toString();
            }
            if (li.get(2) != null) {
                JENumBer = li.get(2).toString();
            }
            issuccess = true;
            msg = messageSource.getMessage("acc.contra.save", null, RequestContextUtils.getLocale(request)) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + billno + ",</b>" + messageSource.getMessage("acc.field.JENo", null, RequestContextUtils.getLocale(request)) + ": <b>" + JENumBer + "</b>";   //"Receipt has been saved successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public List saveContraReceipt(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
        KwlReturnObject result;
        Receipt receipt = null;
        String oldjeid = null;
        String Cardid = null;
        List ll = new ArrayList();
        try {
            String maininvoiceid = request.getParameter("maininvoiceid");//Goods Receipt
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), maininvoiceid);
            String sequenceformat = request.getParameter("sequenceformat");
            GoodsReceipt greceipt = (GoodsReceipt) cmpresult.getEntityList().get(0);
            Account dipositTo = greceipt.getVendor().getAccount();


            double amount = 0;
            double amountDiff = 0;
//            boolean rateDecreased = false;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            double externalCurrencyRate = StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String entryNumber = request.getParameter("no");
            String receiptid = request.getParameter("billid");
//            boolean otherwise = ((request.getParameter("otherwise")!=null)?Boolean.parseBoolean(request.getParameter("otherwise")):false);
//            String methodid =request.getParameter("pmtmethod");
//            request.getSession().setAttribute("methodid", methodid);
            boolean isMultiDebit = StringUtil.getBoolean(request.getParameter("ismultidebit"));
            String drAccDetails = request.getParameter("detail");
            String jeid = null;
            boolean jeautogenflag = false;
//            String payDetailID=null;
            String jeentryNumber = null;
            String jeIntegerPart = "";
            String jeDatePrefix = "";
            String jeDateAfterPrefix = "";
            String jeDateSuffix = "";

            String jeSeqFormatId = "";
            HashMap receipthm = new HashMap();
            String nextAutoNumber = "";
            String datePrefix = "";
            String dateafterPrefix = "";
            String dateSuffix = "";
            Date creationDate = df.parse(request.getParameter("creationdate"));

            synchronized (this) {
                result = accReceiptDAOobj.getReceiptFromBillNo(entryNumber, companyid);
                int count = result.getRecordTotalCount();
                if (count > 0) {
                    if (sequenceformat.equals("NA")) {
                        throw new AccountingException(messageSource.getMessage("acc.field.Receiptnumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                    }
                }
                boolean seqformat_oldflag = StringUtil.getBoolean(request.getParameter("seqformat_oldflag"));

                String nextAutoNoInt = "";
                if (seqformat_oldflag) {
                    nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_RECEIPT, sequenceformat);
                } else {
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_RECEIPT, sequenceformat, seqformat_oldflag, creationDate);
                    nextAutoNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    nextAutoNoInt = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                    datePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    dateafterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                    dateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part

                    receipthm.put(Constants.SEQFORMAT, sequenceformat);
                    receipthm.put(Constants.SEQNUMBER, nextAutoNoInt);
                    receipthm.put(Constants.DATEPREFIX, datePrefix);
                    receipthm.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
                    receipthm.put(Constants.DATESUFFIX, dateSuffix);
                }
                if (!sequenceformat.equals("NA")) {
                    entryNumber = nextAutoNumber;
                }
            }
            receipthm.put("entrynumber", entryNumber);

            receipthm.put("autogenerated", nextAutoNumber.equals(entryNumber));


            cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            String currencyid = (request.getParameter("currencyid") == null ? currency.getCurrencyID() : request.getParameter("currencyid"));

//            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
//            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);


            receipthm.put("currencyid", currencyid);
            receipthm.put("externalCurrencyRate", externalCurrencyRate);
//            KwlReturnObject payresult = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), request.getParameter("pmtmethod"));
//            PaymentMethod payMethod = (PaymentMethod) payresult.getEntityList().get(0);

//            dipositTo = payMethod.getAccount();
//            HashMap pdetailhm = new HashMap();
//            pdetailhm.put("paymethodid", payMethod.getID());
//            pdetailhm.put("companyid", company.getCompanyID());
//            
//            KwlReturnObject pdresult=null;
//            if (!StringUtil.isNullOrEmpty(receiptid)&&!StringUtil.isNullOrEmpty(payDetailID))
//                pdetailhm.put("paydetailid", payDetailID);
//                pdresult = accPaymentDAOobj.addPayDetail(pdetailhm);
//            PayDetail pdetail = (PayDetail) pdresult.getEntityList().get(0);
//            receipthm.put("paydetailsid", pdetail.getID());

            receipthm.put("memo", request.getParameter("memo"));
            receipthm.put("companyid", company.getCompanyID());

            Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
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
                    jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                    jeDateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                    jeSeqFormatId = format.getID();
                    jeautogenflag = true;
                }
            }
            jeDataMap.put("entrynumber", jeentryNumber);
            jeDataMap.put("autogenerated", jeautogenflag);
            jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
            jeDataMap.put(Constants.SEQNUMBER, jeIntegerPart);
            jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
            jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
            jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
            jeDataMap.put("entrydate", creationDate);
            jeDataMap.put("companyid", company.getCompanyID());
            jeDataMap.put("memo", "Contra Entry " + request.getParameter("memo"));
            jeDataMap.put("currencyid", currencyid);
            HashSet jedetails = new HashSet();
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            jeid = journalEntry.getID();
            jeDataMap.put("jeid", jeid);
            String detail = request.getParameter("detail");
            JSONArray jArr = new JSONArray();
            if (!StringUtil.isNullOrEmpty(detail)) {
                jArr = new JSONArray(detail);
            }
            if (jArr.length() > 0 && !isMultiDebit) {
                amount = 0;
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);
                    amount += jobj.getDouble("payment");
                }

//                amountDiff = oldReceiptRowsAmount(request, jArr, currencyid, externalCurrencyRate);
//
//                if (preferences.getForeignexchange() == null) {
//                    throw new AccountingException(messageSource.getMessage("acc.receipt.forex", null, RequestContextUtils.getLocale(request)));
//                }
//                if (amountDiff != 0 && preferences.getForeignexchange() != null) {
//                    if(amountDiff < 0) {
//                        rateDecreased = true;
//                    }
//                    JSONObject jedjson = new JSONObject();
//                    jedjson.put("srno", jedetails.size()+1);
//                    jedjson.put("companyid", companyid);
//                    jedjson.put("amount", rateDecreased?(-1*amountDiff):amountDiff);
//                    jedjson.put("accountid", preferences.getForeignexchange().getID());
//                    jedjson.put("debit", rateDecreased?false:true);
//                    jedjson.put("jeid", jeid);
//                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
//                    JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
//                    jedetails.add(jed);
//                }

            } else {
                amount = Double.parseDouble(request.getParameter("amount"));
            }
            JSONObject jedjson = null;
            KwlReturnObject jedresult = null;
            JournalEntryDetail jed = null;
            if (isMultiDebit) {
                JSONArray drAccArr = new JSONArray(drAccDetails);
                for (int i = 0; i < drAccArr.length(); i++) {
                    JSONObject jobj = drAccArr.getJSONObject(i);
                    jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", Double.parseDouble(jobj.getString("dramount")));
                    jedjson.put("accountid", jobj.getString("accountid"));
                    jedjson.put("debit", false);
                    jedjson.put("jeid", jeid);
                    jedjson.put("description", jobj.getString("description"));
                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                }
            } else {
                jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put("companyid", companyid);
                jedjson.put("amount", amount + amountDiff);
                jedjson.put("accountid", request.getParameter("accid"));
                jedjson.put("debit", false);
                jedjson.put("jeid", jeid);
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jedetails.add(jed);
            }

            jedjson = new JSONObject();
            jedjson.put("srno", jedetails.size() + 1);
            jedjson.put("companyid", companyid);
            //(If currency USD and base currency SGD, inv rate 0.81 n pay rate 0.80 then amount = 1234.57, amountDiff = -15.24, depositAmt = 1250 SGD)
            jedjson.put("amount", amount);
            jedjson.put("accountid", dipositTo.getID());
            jedjson.put("debit", true);
            jedjson.put("jeid", jeid);
            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetails.add(jed);
            jeDataMap.put("jedetails", jedetails);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);

            receipthm.put("journalentryid", journalEntry.getID());
            receipthm.put("contraentry", true);
            if (!StringUtil.isNullOrEmpty(receiptid)) {
                receipthm.put("receiptid", receipt.getID());
            }

            result = accReceiptDAOobj.saveReceipt(receipthm);
            receipt = (Receipt) result.getEntityList().get(0);
            receipthm.put("receiptid", receipt.getID());

            HashSet receiptDetails = new HashSet();
            if (!isMultiDebit)//To do - need to save vendor invoice no as well
            {
                receiptDetails = saveReceiptRows(receipt, company, jArr, greceipt);
            }
            receipthm.put("receiptdetails", receiptDetails);

            result = accReceiptDAOobj.saveReceipt(receipthm);
            receipt = (Receipt) result.getEntityList().get(0);

            //Insert new entries in optimized table.
            accJournalEntryobj.saveAccountJEs_optimized(jeid);

            //To do - make audit entry
//            auditTrailObj.insertAuditLog(AuditAction.RECEIPT_ADDED, "User "+sessionHandlerImpl.getUserFullName(request) +" created new contra entry for Invoice No.", request, receipt.getID());

        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveReceipt : " + ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveReceipt : " + ex.getMessage(), ex);
        }
        ll.add(new String[]{oldjeid, Cardid});
        ll.add(receipt.getReceiptNumber());
        ll.add(receipt.getJournalEntry().getEntryNumber());
        return (ArrayList) ll;
    }

    public class EnglishNumberToWords {

        private final String[] tensNames = {
            "", " Ten", " Twenty", " Thirty", " Forty", " Fifty", " Sixty", " Seventy", " Eighty", " Ninety"
        };
        private final String[] numNames = {
            "", " One", " Two", " Three", " Four", " Five", " Six", " Seven", " Eight", " Nine", " Ten", " Eleven", " Twelve",
            " Thirteen", " Fourteen", " Fifteen", " Sixteen", " Seventeen", " Eighteen", " Nineteen"
        };

        private String convertLessThanOneThousand(int number) {
            String soFar;
            if (number % 100 < 20) {
                soFar = numNames[number % 100];
                number /= 100;
            } else {
                soFar = numNames[number % 10];
                number /= 10;
                soFar = tensNames[number % 10] + soFar;
                number /= 10;
            }
            if (number == 0) {
                return soFar;
            }
            return numNames[number] + " Hundred" + soFar;
        }

        private String convertLessOne(int number, KWLCurrency currency) {
            String soFar;
            String val = currency.getAfterDecimalName();
            if (number % 100 < 20) {
                soFar = numNames[number % 100];
                number /= 100;
            } else {
                soFar = numNames[number % 10];
                number /= 10;
                soFar = tensNames[number % 10] + soFar;
                number /= 10;
            }
            if (number == 0) {
                return " And " + soFar + " " + val;
            }
            return " And " + numNames[number] + " " + val + soFar;
        }

        public String convert(Double number, KWLCurrency currency, int countryLanguageId) {
            if (number == 0) {
                return "Zero";
            }

            String answer = "";

            if (countryLanguageId == Constants.OtherCountryLanguageId) { // For universal conversion of amount in words. i.e. in Billion,trillion etc
                answer = universalConvert(number, currency);
            } else if (countryLanguageId == Constants.CountryIndiaLanguageId) { // For Indian word format.ie. in lakhs, crores
                answer = indianConvert(number, currency);
            }
            return answer;
        }
        
        public String universalConvert(Double number, KWLCurrency currency) {
            boolean isNegative = false;
            if (number < 0) {
                isNegative = true;
                number = -1 * number;
            }
            String snumber = Double.toString(number);
            String mask = "000000000000.00";
            DecimalFormat df = new DecimalFormat(mask);
            snumber = df.format(number);
            int billions = Integer.parseInt(snumber.substring(0, 3));
            int millions = Integer.parseInt(snumber.substring(3, 6));
            int hundredThousands = Integer.parseInt(snumber.substring(6, 9));
            int thousands = Integer.parseInt(snumber.substring(9, 12));
            int fractions = Integer.parseInt(snumber.substring(13, 15));
            String tradBillions;
            switch (billions) {
                case 0:
                    tradBillions = "";
                    break;
                case 1:
                    tradBillions = convertLessThanOneThousand(billions) + " Billion ";
                    break;
                default:
                    tradBillions = convertLessThanOneThousand(billions) + " Billion ";
            }
            String result = tradBillions;

            String tradMillions;
            switch (millions) {
                case 0:
                    tradMillions = "";
                    break;
                case 1:
                    tradMillions = convertLessThanOneThousand(millions) + " Million ";
                    break;
                default:
                    tradMillions = convertLessThanOneThousand(millions) + " Million ";
            }
            result = result + tradMillions;

            String tradHundredThousands;
            switch (hundredThousands) {
                case 0:
                    tradHundredThousands = "";
                    break;
                case 1:
                    tradHundredThousands = "One Thousand ";
                    break;
                default:
                    tradHundredThousands = convertLessThanOneThousand(hundredThousands) + " Thousand ";
            }
            result = result + tradHundredThousands;
            String tradThousand;
            tradThousand = convertLessThanOneThousand(thousands);
            result = result + tradThousand;
            String paises;
            switch (fractions) {
                case 0:
                    paises = "";
                    break;
                default:
                    paises = convertLessOne(fractions, currency);
            }
            result = result + paises; //to be done later
            result = result.replaceAll("^\\s+", "").replaceAll("\\b\\s{2,}\\b", " ");
            if (isNegative) {
                result = "Minus " + result;
            }
//            result = result.substring(0, 1).toUpperCase() + result.substring(1).toLowerCase(); // Make first letter of operand capital.
            return result;
        }
        
        public String indianConvert(Double number, KWLCurrency currency) {
            boolean isNegative = false;
            if (number < 0) {
                isNegative = true;
                number = -1 * number;
            }
            String snumber = Double.toString(number);
            String mask = "000000000000000.00";  //ERP-17681
            DecimalFormat df = new DecimalFormat(mask);
            snumber = df.format(number);

            int n = Integer.parseInt(snumber.substring(0, 15));
            int fractions = Integer.parseInt(snumber.split("\\.").length != 0 ? snumber.split("\\.")[1] : "0");
            if (n == 0) {
                return "Zero";
            }
            String arr1[] = {"One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"};
            String arr2[] = {"Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"};
            String unit[] = {"Arab", "Crore", "Lakh", "Thousand", "Hundred", ""};
            int factor[] = {1000000000, 10000000, 100000, 1000, 100, 1};
            String answer = "", paises = "";
            if (n < 0) {
                answer = "Minus";
                n = -n;
            }
            int quotient, units, tens;
            for (int i = 0; i < factor.length; i++) {
                quotient = n / factor[i];
                if (quotient > 0) {
                    if (quotient < 20) {
                        answer = answer + " " + arr1[quotient - 1];
                    } else {
                        units = quotient % 10;
                        tens = quotient / 10;
                        if (units > 0) {
                            answer = answer + " " + arr2[tens - 2] + " " + arr1[units - 1];
                        } else {
                            answer = answer + " " + arr2[tens - 2] + " ";
                        }
                    }
                    answer = answer + " " + unit[i];
                }
                n = n % factor[i];
            }
            switch (fractions) {
                case 0:
                    paises = "";
                    break;
                default:
                    paises = convertLessOne(fractions, currency);
            }
            answer = answer + paises; //to be done later
            return answer.trim();
        }
    }

    private void deleteBankReconcilation(Map<String, Object> requestParams) throws ServiceException {
        if (requestParams.containsKey("oldjeid")) {
            String reconsilationID = "";
            String unReconsilationID = "";
            String jeid=(String) requestParams.get("oldjeid");
            String companyid=(String) requestParams.get("companyId"); 
            
            //Deleting  BankReconciliationDetail
            KwlReturnObject reconsiledDetails = accBankReconciliationObj.getBRfromJE(jeid, companyid, true);
            if (reconsiledDetails.getRecordTotalCount() > 0) {                
                List<BankReconciliationDetail> brd = reconsiledDetails.getEntityList();
                for (BankReconciliationDetail reconciliation : brd) {
                    accBankReconciliationObj.permenantDeleteBankReconciliationDetail(reconciliation.getID(), companyid);
                    reconsilationID = reconciliation.getBankReconciliation().getID();
                }                
            }
            
            //Deleting  BankUnreconciliationDetail
            KwlReturnObject unReconsiledDetails = accBankReconciliationObj.getBankUnReconsiledfromJE(jeid, companyid, true);
            if (unReconsiledDetails.getRecordTotalCount() > 0) {              
                List<BankUnreconciliationDetail> brd = unReconsiledDetails.getEntityList();
                for (BankUnreconciliationDetail reconciliation : brd) {
                    accBankReconciliationObj.permenantDeleteBankUnReconciliationDetail(reconciliation.getID(), companyid);
                    unReconsilationID = reconciliation.getBankReconciliation().getID();
                }            
            }
            if(!StringUtil.isNullOrEmpty(reconsilationID)){
                accBankReconciliationObj.deleteBankReconciliation(reconsilationID,companyid);
            }
            if(!StringUtil.isNullOrEmpty(unReconsilationID)){
                accBankReconciliationObj.deleteBankReconciliation(unReconsilationID,companyid);
            }                    
        }
    }    

    private void saveBankReconsilation(Map<String, Object> requestParams, Map<String, Object> globalParams) throws ServiceException, JSONException, UnsupportedEncodingException {
        HashMap<String, Object> brMap = new HashMap<String, Object>();
        KwlReturnObject crresult = accCurrencyobj.getCurrencyToBaseAmount(globalParams, (Double) requestParams.get("clearingamount"), (String) requestParams.get("currencyid"), (Date) requestParams.get("clearanceDate"), 0);
        double clearingAmount = (Double) crresult.getEntityList().get(0);

        if (requestParams.containsKey("oldjeid")) {
            deleteBankReconcilation(requestParams);
        }

        brMap.put("startdate", (Date) requestParams.get("startDate"));
        brMap.put("enddate", (Date) requestParams.get("endDate"));
        brMap.put("clearanceDate", (Date) requestParams.get("clearanceDate"));
        brMap.put("clearingamount", clearingAmount);
        brMap.put("endingamount", (Double) requestParams.get("endingAmount"));
        brMap.put("accountid", (String) requestParams.get("bankAccountId"));
        brMap.put("companyid", (String) requestParams.get("companyId"));
        brMap.put("createdby", (String) requestParams.get("createdby"));
        brMap.put("checkCount", (Integer) requestParams.get("checkCount"));
        brMap.put("depositeCount", (Integer) requestParams.get("depositeCount"));
        KwlReturnObject brresult = accBankReconciliationObj.addBankReconciliation(brMap);
        BankReconciliation br = (BankReconciliation) brresult.getEntityList().get(0);
        String brid = br.getID();
        Receipt receipt = null;
        BillingReceipt breceipt = null;
        if (requestParams.containsKey("receipt")) {
            receipt = (Receipt) requestParams.get("receipt");
        } else {
            breceipt = (BillingReceipt) requestParams.get("breceipt");
        } 
        JournalEntry entry = receipt != null ? receipt.getJournalEntry() : breceipt.getJournalEntry();
        Set details = entry.getDetails();
        Iterator iter = details.iterator();
        String accountName = "";
        while (iter.hasNext()) {
            JournalEntryDetail d = (JournalEntryDetail) iter.next();
            if (d.isDebit()) {
                continue;
            }
            accountName += d.getAccount().getName() + ", ";
        }
        accountName = accountName.substring(0, Math.max(0, accountName.length() - 2));
        HashSet hs = new HashSet();
        boolean isMultiDebit = Boolean.parseBoolean(requestParams.get("ismultidebit").toString());
        JSONArray jArr = (JSONArray) requestParams.get("details");
        double amount = 0;
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);
            if (jobj.optDouble("enteramount", 0) != 0) {
                KwlReturnObject crresult1 = accCurrencyobj.getCurrencyToBaseAmount(globalParams, (isMultiDebit ? jobj.getDouble("enteramount") : jobj.getDouble("payment")), jobj.getString("currencyid"), (Date) requestParams.get("clearanceDate"), 0);
                double amt = (Double) crresult1.getEntityList().get(0);
                if (jobj.optBoolean("debit",false)) {
                    amount -= amt;
                } else {
                    amount += amt;
                }
            }
        }
        HashMap<String, Object> brdMap = new HashMap<String, Object>();
        brdMap.put("companyid", (String) requestParams.get("companyId"));
        brdMap.put("amount", amount);
        brdMap.put("jeid", entry.getID());
        brdMap.put("accountname", accountName);
        brdMap.put("debit", true);
        brdMap.put("brid", brid);
        brdMap.put("reconcileDate", (Date) requestParams.get("clearanceDate"));
        KwlReturnObject brdresult = accBankReconciliationObj.addBankReconciliationDetail(brdMap);
        BankReconciliationDetail brd = (BankReconciliationDetail) brdresult.getEntityList().get(0);
        hs.add(brd);
    }

    public void deleteJEArray(String oldjeid, String companyid) throws ServiceException, AccountingException, SessionExpiredException {
        try {      //delete old invoice
            JournalEntryDetail jed = null;
            if (!StringUtil.isNullOrEmpty(oldjeid)) {
                KwlReturnObject result = accJournalEntryobj.getJournalEntryDetail(oldjeid, companyid);
                List list = result.getEntityList();
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    jed = (JournalEntryDetail) itr.next();
                    //Sagar - No need to revert entry from optimized table as entries are already reverted from calling main function in edit case.
                    result = accJournalEntryobj.deleteJournalEntryDetailRow(jed.getID(), companyid);
                }
                result = accJournalEntryobj.permanentDeleteJournalEntry(oldjeid, companyid);
                KwlReturnObject jedresult1 = accJournalEntryobj.deleteJECustomData(oldjeid);
            }
        } catch (Exception ex) {
            //Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    public void deleteChequeOrCard(String id, String companyid) throws ServiceException, AccountingException, SessionExpiredException {
        try {
            if (id != null) {
                accPaymentDAOobj.deleteCard(id, companyid);
                accPaymentDAOobj.deleteChequePermanently(id, companyid);
            }
        } catch (Exception ex) {
            //Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    private double oldReceiptRowsAmount(HttpServletRequest request, JSONArray jArr, String currencyid, double externalCurrencyRate) throws ServiceException, SessionExpiredException {
        double amount = 0, actualAmount = 0;
        KwlReturnObject result;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
            Date creationDate = authHandler.getDateOnlyFormat(request).parse(request.getParameter("creationdate"));
            for (int i = 0; i < jArr.length(); i++) {
                double ratio = 0;
                JSONObject jobj = jArr.getJSONObject(i);
                Date invoiceCreationDate = new Date();

                double newrate = 0.0;
                boolean revalFlag = false;
                //            Invoice invoice=(Invoice) session.get(Invoice.class, jobj.getString("billid"));
                result = accountingHandlerDAOobj.getObject(Invoice.class.getName(), jobj.getString("documentid"));
                Invoice invoice = (Invoice) result.getEntityList().get(0);
                boolean isopeningBalanceInvoice = invoice.isIsOpeningBalenceInvoice();
                double exchangeRate = 0d;
                invoiceCreationDate = invoice.getCreationDate();
                if (!invoice.isNormalInvoice() && invoice.isIsOpeningBalenceInvoice()) {
                    exchangeRate = invoice.getExchangeRateForOpeningTransaction();
                    if(invoice.isConversionRateFromCurrencyToBase()){
                        exchangeRate = 1/exchangeRate;
                    }
                } else {
                    exchangeRate = invoice.getJournalEntry().getExternalCurrencyRate();
//                    invoiceCreationDate = invoice.getJournalEntry().getEntryDate();
                }

                HashMap<String, Object> invoiceId = new HashMap<String, Object>();
                invoiceId.put("invoiceid", invoice.getID());
                invoiceId.put("companyid", sessionHandlerImpl.getCompanyid(request));
                //invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
//                if (!invoice.isIsOpeningBalenceInvoice()) {
                result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                if (history != null) {
                    exchangeRate = history.getEvalrate();
                    newrate = jobj.optDouble("exchangeratefortransaction", 0.0);
                    revalFlag = true;
                }
//                }
//                

//            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
                result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
                KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
                String currid = currency.getCurrencyID();
                if (invoice.getCurrency() != null) {
                    currid = invoice.getCurrency().getCurrencyID();
                }

                if (currid.equalsIgnoreCase(currencyid)) {
                    // code in if condition is commented for ticket ERP-18047
//                    if (history == null && isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
//                        result = accCurrencyobj.getOneCurrencyToOtherAccordingToCurrencyToBaseExchangeRate(requestParams, 1.0, currid, currencyid, invoiceCreationDate, exchangeRate);
//                    } else {
                            double paymentExternalCurrencyRate=StringUtil.isNullOrEmpty(request.getParameter("externalcurrencyrate"))?0.0:Double.parseDouble(request.getParameter("externalcurrencyrate"));
                            if(exchangeRate!=paymentExternalCurrencyRate){
                                result = accCurrencyobj.getOneCurrencyToOtherWithDiffRates(requestParams, 1.0, currid, currencyid, invoiceCreationDate, exchangeRate,paymentExternalCurrencyRate);
                            }else{
                                result = accCurrencyobj.getOneCurrencyToOther(requestParams, 1.0, currid, currencyid, invoiceCreationDate, exchangeRate);
                            }
//                    }
                } else {
                    // code in if condition is commented for ticket ERP-18047
//                    if (history == null && isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
//                        result = accCurrencyobj.getOneCurrencyToOtherModifiedAccordingToCurrencyToBaseExchangeRate(requestParams, 1.0, currid, currencyid, invoiceCreationDate, exchangeRate);
//                    } else {
                            double paymentExternalCurrencyRate=StringUtil.isNullOrEmpty(request.getParameter("externalcurrencyrate"))?0.0:Double.parseDouble(request.getParameter("externalcurrencyrate"));
                            if(exchangeRate!=paymentExternalCurrencyRate){
                                result = accCurrencyobj.getOneCurrencyToOtherWithDiffRates(requestParams, 1.0, currid, currencyid, invoiceCreationDate, exchangeRate,paymentExternalCurrencyRate);
                            }else{
                                result = accCurrencyobj.getOneCurrencyToOtherModified(requestParams, 1.0, currid, currencyid, invoiceCreationDate, exchangeRate);
                            }
//                    }
                }
                double oldrate = (Double) result.getEntityList().get(0);
                Double recinvamount = jobj.getDouble("enteramount");
                boolean isopeningBalanceRecceipt = jobj.optBoolean("isopeningBalanceRecceipt", false);
                boolean isConversionRateFromCurrencyToBase = jobj.optBoolean("isConversionRateFromCurrencyToBase", false);
                if (jobj.optDouble("exchangeratefortransaction", 0.0) != oldrate && jobj.optDouble("exchangeratefortransaction", 0.0) != 0.0 && Math.abs(jobj.optDouble("exchangeratefortransaction", 0.0) - oldrate) >= 0.000001) {
                    newrate = jobj.optDouble("exchangeratefortransaction", 0.0);
                    ratio = oldrate - newrate;
                    amount = (recinvamount - (recinvamount / newrate) * oldrate) / newrate;
                    KwlReturnObject bAmtActual = accCurrencyobj.getBaseToCurrencyAmount(requestParams, amount, currencyid, creationDate, newrate);
                    actualAmount += authHandler.round((Double) bAmtActual.getEntityList().get(0), companyid);
                } else {
                    if (currid.equalsIgnoreCase(currencyid)) {
                        // code in if condition is commented for ticket ERP-18047
//                        if (isopeningBalanceRecceipt && isConversionRateFromCurrencyToBase) {// if receipt is opening balance receipt and Conversion rate is taken from user is Currency to base then following method will be called.
//                            result = accCurrencyobj.getOneCurrencyToOtherAccordingToCurrencyToBaseExchangeRate(requestParams, 1.0, currid, currencyid, invoiceCreationDate, externalCurrencyRate);
//                        } else {
                            double paymentExternalCurrencyRate=StringUtil.isNullOrEmpty(request.getParameter("externalcurrencyrate"))?0.0:Double.parseDouble(request.getParameter("externalcurrencyrate"));
                            if(exchangeRate!=paymentExternalCurrencyRate){
                                result = accCurrencyobj.getOneCurrencyToOtherWithDiffRates(requestParams, 1.0, currid, currencyid, invoiceCreationDate, exchangeRate,paymentExternalCurrencyRate);
                            }else{
                                result = accCurrencyobj.getOneCurrencyToOther(requestParams, 1.0, currid, currencyid, invoiceCreationDate, exchangeRate);
                            }
//                        }
                    } else {
                        // code in if condition is commented for ticket ERP-18047
//                        if (isopeningBalanceRecceipt && isConversionRateFromCurrencyToBase) {// if receipt is opening balance receipt and Conversion rate is taken from user is Currency to base then following method will be called.
//                            result = accCurrencyobj.getOneCurrencyToOtherModifiedAccordingToCurrencyToBaseExchangeRate(requestParams, 1.0, currid, currencyid, invoiceCreationDate, externalCurrencyRate);
//                        } else {
                           double paymentExternalCurrencyRate=StringUtil.isNullOrEmpty(request.getParameter("externalcurrencyrate"))?0.0:Double.parseDouble(request.getParameter("externalcurrencyrate"));
                            if(exchangeRate!=paymentExternalCurrencyRate){
                                result = accCurrencyobj.getOneCurrencyToOtherWithDiffRates(requestParams, 1.0, currid, currencyid, invoiceCreationDate, exchangeRate,paymentExternalCurrencyRate);
                            }else{
                                result = accCurrencyobj.getOneCurrencyToOtherModified(requestParams, 1.0, currid, currencyid, invoiceCreationDate, exchangeRate);
                            }
//                        }
                    }
                    if (!revalFlag) {
                        newrate = (Double) result.getEntityList().get(0);
                    }
                    if (Math.abs(jobj.optDouble("exchangeratefortransaction", 0.0) - oldrate) >= 0.000001) {
                        ratio = oldrate - newrate;
                    }
                    amount = recinvamount * ratio;
                    KwlReturnObject bAmtActual = null;
                    if (isopeningBalanceRecceipt && isConversionRateFromCurrencyToBase) {// if receipt is opening balance receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                        bAmtActual = accCurrencyobj.getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amount, currencyid, creationDate, externalCurrencyRate);
                    } else {
                        bAmtActual = accCurrencyobj.getBaseToCurrencyAmount(requestParams, amount, currencyid, creationDate, externalCurrencyRate);
                    }
                    actualAmount += (Double) bAmtActual.getEntityList().get(0);
                }

            }

        } catch (ParseException ex) {
            throw ServiceException.FAILURE("oldReceiptRowsAmount : " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("oldReceiptRowsAmount : " + ex.getMessage(), ex);
        }
        return (actualAmount);
    }

    private HashSet saveReceiptRows(Receipt receipt, Company company, JSONArray jArr, GoodsReceipt greceipt) throws JSONException, ServiceException {
        HashSet details = new HashSet();
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);
            ReceiptDetail rd = new ReceiptDetail();
            rd.setSrno(i + 1);
            rd.setID(StringUtil.generateUUID());
            double amountReceived = jobj.getDouble("payment");// amount in receipt currency
            double amountReceivedConverted = jobj.getDouble("payment"); // amount in invoice currency
            rd.setAmount(jobj.getDouble("payment"));
            if (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString()) && !StringUtil.isNullOrEmpty(jobj.optString("currencyidtransaction", "")) && !jobj.optString("currencyidtransaction", "").equals(receipt.getCurrency().getCurrencyID())) {
                rd.setExchangeRateForTransaction(Double.parseDouble(jobj.get("exchangeratefortransaction").toString()));
                KwlReturnObject resultCurrency = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), jobj.getString("currencyidtransaction"));
                KWLCurrency kWLCurrency = (KWLCurrency) resultCurrency.getEntityList().get(0);
                rd.setFromCurrency(kWLCurrency);
                rd.setToCurrency(receipt.getCurrency());
                amountReceivedConverted = amountReceived / Double.parseDouble(jobj.get("exchangeratefortransaction").toString());
            }
            rd.setCompany(company);
            KwlReturnObject result = accountingHandlerDAOobj.getObject(Invoice.class.getName(), jobj.getString("billid"));
            Invoice invoice = (Invoice) result.getEntityList().get(0);
            rd.setInvoice((Invoice) result.getEntityList().get(0));
            if (greceipt != null) {
                rd.setGoodsReceipt(greceipt);
            }
            rd.setReceipt(receipt);
            if (jobj.has("rowjedid")) {
                rd.setROWJEDID(jobj.getString("rowjedid"));
            }
            details.add(rd);
          
            double amountReceivedConvertedInBaseCurrency = 0d;
            HashMap<String, Object> requestParams = new HashMap();
            requestParams.put(Constants.companyid, company.getCompanyID());
            requestParams.put("gcurrencyid", company.getCurrency().getCurrencyID());
            double externalCurrencyRate = 1d;
            boolean isopeningBalanceRCP = receipt.isIsOpeningBalenceReceipt();
            Date rcpCreationDate = null;
            rcpCreationDate = receipt.getCreationDate();
            if (isopeningBalanceRCP) {
                externalCurrencyRate = receipt.getExchangeRateForOpeningTransaction();
            } else {
//                rcpCreationDate = receipt.getJournalEntry().getEntryDate();
                externalCurrencyRate = receipt.getJournalEntry().getExternalCurrencyRate();
            }
            String fromcurrencyid = receipt.getCurrency().getCurrencyID();
            KwlReturnObject bAmt = null;
            if (isopeningBalanceRCP && receipt.isConversionRateFromCurrencyToBase()) {// if Receipt is opening balance Receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountReceived, fromcurrencyid, rcpCreationDate, externalCurrencyRate);
            } else {
                bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, amountReceived, fromcurrencyid, rcpCreationDate, externalCurrencyRate);
            }
            amountReceivedConvertedInBaseCurrency = (Double) bAmt.getEntityList().get(0);
            
            updateInvoiceAmountDue(invoice, receipt, company, amountReceivedConverted, amountReceivedConvertedInBaseCurrency);
            updateReceiptAmountDue(receipt, company, amountReceived, amountReceivedConvertedInBaseCurrency);
        }
        return details;
        }
    private HashSet saveReceiptRows(HttpServletRequest request,Receipt receipt, Company company, JSONArray jArr, GoodsReceipt greceipt,int type) throws JSONException, ServiceException, UnsupportedEncodingException {
        HashSet details = new HashSet();
        String companyid=company.getCompanyID();
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);
            ReceiptDetail rd = new ReceiptDetail();
            rd.setSrno(i + 1);
            rd.setID(StringUtil.generateUUID());
            double amountReceived = jobj.getDouble("enteramount");// amount in receipt currency
            double amountReceivedConverted = jobj.getDouble("enteramount"); // amount in invoice currency
            rd.setAmount(jobj.getDouble("enteramount"));
//            if (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString()) && !StringUtil.isNullOrEmpty(jobj.optString("currencyidtransaction", "")) && !jobj.optString("currencyidtransaction", "").equals(receipt.getCurrency().getCurrencyID())) {
//                rd.setExchangeRateForTransaction(Double.parseDouble(jobj.get("exchangeratefortransaction").toString()));
//                KwlReturnObject resultCurrency = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), jobj.getString("currencyidtransaction"));
//                KWLCurrency kWLCurrency = (KWLCurrency) resultCurrency.getEntityList().get(0);
//                rd.setFromCurrency(kWLCurrency);
//                rd.setToCurrency(receipt.getCurrency());
//                amountReceivedConverted = amountReceived / Double.parseDouble(jobj.get("exchangeratefortransaction").toString());
//            }
            rd.setCompany(company);
            KwlReturnObject result = accountingHandlerDAOobj.getObject(Invoice.class.getName(), jobj.getString("documentid"));
            Invoice invoice = (Invoice) result.getEntityList().get(0);
            boolean isClaimedInvoice = invoice.getBadDebtType() == Constants.Invoice_Claimed || invoice.getBadDebtType() == Constants.Invoice_Recovered;
            rd.setInvoice((Invoice) result.getEntityList().get(0));
            if (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString()) && !StringUtil.isNullOrEmpty(invoice.getCurrency().getCurrencyID()) && !invoice.getCurrency().getCurrencyID().equals(receipt.getCurrency().getCurrencyID())) {
                    rd.setExchangeRateForTransaction(Double.parseDouble(jobj.get("exchangeratefortransaction").toString()));
                    rd.setFromCurrency(invoice.getCurrency());
                    rd.setToCurrency(receipt.getCurrency());
                    double adjustedRate=jobj.optDouble("amountdue",0)/jobj.optDouble("amountDueOriginal",0);
//                    amountReceivedConverted = amountReceived / Double.parseDouble(jobj.get("exchangeratefortransaction").toString());
                    amountReceivedConverted = amountReceived / adjustedRate;;
                    amountReceivedConverted = authHandler.round(amountReceivedConverted, companyid);
                    rd.setAmountInInvoiceCurrency(amountReceivedConverted);
                    rd.setAmountDueInInvoiceCurrency(jobj.optDouble("amountDueOriginal",0));
                    rd.setAmountDueInPaymentCurrency(jobj.optDouble("amountdue",0));
                }else{
                    rd.setExchangeRateForTransaction(Double.parseDouble(jobj.get("exchangeratefortransaction").toString()));
                    rd.setFromCurrency(invoice.getCurrency());
                    rd.setToCurrency(receipt.getCurrency());
                    amountReceivedConverted = authHandler.round(amountReceived, companyid);
                    rd.setAmountInInvoiceCurrency(amountReceivedConverted);
                    rd.setAmountDueInInvoiceCurrency(jobj.optDouble("amountDueOriginal",0));
                    rd.setAmountDueInPaymentCurrency(jobj.optDouble("amountdue",0));
                }
            rd.setGstCurrencyRate(jobj.optDouble("gstCurrencyRate",0.0));
            rd.setDescription(StringUtil.DecodeText(jobj.optString("description")));
            if (greceipt != null) {
                rd.setGoodsReceipt(greceipt);
            }
            rd.setReceipt(receipt);
            if (jobj.has("rowjedid")) {
                rd.setROWJEDID(jobj.getString("rowjedid"));
            }
            if (jobj.has("srNoForRow")) {
                int srNoForRow = StringUtil.isNullOrEmpty("srNoForRow") ? 0 : Integer.parseInt(jobj.getString("srNoForRow"));
                rd.setSrNoForRow(srNoForRow);
            }
            if (jobj.has("jedetail") && jobj.get("jedetail") != null) {
                KwlReturnObject resJED = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), (String) jobj.get("jedetail"));
                JournalEntryDetail jedObj = (JournalEntryDetail) resJED.getEntityList().get(0);
                rd.setTotalJED(jedObj);
            }
            HashMap<String, JSONArray> jcustomarrayMap = receipt.getJcustomarrayMap();      
            JSONArray jcustomarray = jcustomarrayMap.get(rd.getROWJEDID());
            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
            customrequestParams.put("customarray", jcustomarray);
            customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
            customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
            customrequestParams.put("modulerecid", rd.getROWJEDID());
            customrequestParams.put("recdetailId", rd.getID());
            customrequestParams.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
            customrequestParams.put("companyid", companyid);
            customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                KwlReturnObject receiptAccJEDCustomData = accountingHandlerDAOobj.getObject(AccJEDetailCustomData.class.getName(), rd.getROWJEDID());
                AccJEDetailCustomData accJEDetailCustomData = (AccJEDetailCustomData) receiptAccJEDCustomData.getEntityList().get(0);
                KwlReturnObject receiptJED = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), rd.getROWJEDID());
                JournalEntryDetail journalEntryDetail = (JournalEntryDetail) receiptJED.getEntityList().get(0);
                journalEntryDetail.setAccJEDetailCustomData(accJEDetailCustomData);
            }
            details.add(rd);
            double amountReceivedConvertedInBaseCurrency = 0d;
            HashMap<String, Object> requestParams = new HashMap();
            requestParams.put(Constants.companyid, company.getCompanyID());
            requestParams.put("gcurrencyid", company.getCurrency().getCurrencyID());
//            double externalCurrencyRate = 1d;
//            boolean isopeningBalanceRCP = receipt.isIsOpeningBalenceReceipt();
//            Date rcpCreationDate = null;
//            if (isopeningBalanceRCP) {
//                rcpCreationDate = receipt.getCreationDate();
//                externalCurrencyRate = receipt.getExchangeRateForOpeningTransaction();
//            } else {
//                rcpCreationDate = receipt.getJournalEntry().getEntryDate();
//                externalCurrencyRate = receipt.getJournalEntry().getExternalCurrencyRate();
//            }
            /*
                    * Amount received against Invoice will be converted to base currency as per spot rate of INvoice 
            */
            double grExternalCurrencyRate=0d;
                Date grCreationDate = null;
                grCreationDate = invoice.getCreationDate();
                if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
                    grExternalCurrencyRate = invoice.getExchangeRateForOpeningTransaction();
                } else {
//                    grCreationDate = invoice.getJournalEntry().getEntryDate();
                    grExternalCurrencyRate = invoice.getJournalEntry().getExternalCurrencyRate();
                }
            String fromcurrencyid = invoice.getCurrency().getCurrencyID();
            KwlReturnObject bAmt = null;
//            if (isopeningBalanceRCP && receipt.isConversionRateFromCurrencyToBase()) {// if Receipt is opening balance Receipt and Conversion rate is taken from user is Currency to base then following method will be called.
//                bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountReceived, fromcurrencyid, rcpCreationDate, externalCurrencyRate);
//            } else {
//                bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, amountReceived, fromcurrencyid, rcpCreationDate, externalCurrencyRate);
//            }
            if(invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()){
                   if(invoice.isConversionRateFromCurrencyToBase()){
                       grExternalCurrencyRate=1/grExternalCurrencyRate;
                   }
                }
            bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, amountReceivedConverted, fromcurrencyid, grCreationDate, grExternalCurrencyRate);
            amountReceivedConvertedInBaseCurrency = (Double) bAmt.getEntityList().get(0);
            amountReceivedConvertedInBaseCurrency=authHandler.round(amountReceivedConvertedInBaseCurrency, companyid);
            rd.setAmountInBaseCurrency(amountReceivedConvertedInBaseCurrency);
            
            /*Store the date on which the amount due has been set to zero*/
            KwlReturnObject invoiceResult=null;
            if (receipt.getApprovestatuslevel()==Constants.APPROVED_STATUS_LEVEL) {
                invoiceResult = updateInvoiceAmountDueAndReturnResult(invoice, receipt, company, amountReceivedConverted, amountReceivedConvertedInBaseCurrency);
            }
            if (!isClaimedInvoice) {
                if (invoiceResult != null && invoiceResult.getEntityList() != null && invoiceResult.getEntityList().size() > 0) {
                    Invoice inv = (Invoice) invoiceResult.getEntityList().get(0);
                    if (inv.isIsOpeningBalenceInvoice() && inv.getOpeningBalanceAmountDue() == 0) {
                        try {
                            DateFormat df = authHandler.getDateOnlyFormat(request);
                            HashMap<String, Object> dataMap = new HashMap<String, Object>();
                            dataMap.put("amountduedate", df.parse(request.getParameter("creationdate")));
                            accInvoiceDAOObj.saveInvoiceAmountDueZeroDate(inv, dataMap);
                        } catch (Exception ex) {
                            System.out.println("" + ex.getMessage());
                        }
                    } else if (inv.getInvoiceamountdue() == 0) {
                        try {
                            DateFormat df = authHandler.getDateOnlyFormat(request);
                            HashMap<String, Object> dataMap = new HashMap<String, Object>();
                            dataMap.put("amountduedate", df.parse(request.getParameter("creationdate")));
                            accInvoiceDAOObj.saveInvoiceAmountDueZeroDate(inv, dataMap);
                        } catch (Exception ex) {
                            System.out.println("" + ex.getMessage());
                        }
                    }
                }
            }
            if (receipt != null) {
                    double receiptAmountDue = receipt.getOpeningBalanceAmountDue();
                    receiptAmountDue -= amountReceived;
                    receipt.setOpeningBalanceAmountDue(receiptAmountDue);
                    receipt.setOpeningBalanceBaseAmountDue(receipt.getOpeningBalanceBaseAmountDue() - amountReceivedConvertedInBaseCurrency);
                }
        }
        return details;
    }
    private HashMap<String, Object> saveLinkedReceiptDetails(JSONObject requestObj, Receipt receipt, Company company, JSONArray jArr, GoodsReceipt greceipt,int type,Map<String,Object> counterMap) throws JSONException, ServiceException, AccountingException, SessionExpiredException, ParseException {
        HashSet linkdetails = new HashSet();
        HashMap<String, Object> linkDetails = new HashMap<String, Object>();
        Date maxLinkingDate = null;
        String companyid=company.getCompanyID();
        String linkingdate = requestObj.optString("linkingdateString");
        Locale locale = (Locale) requestObj.opt("locale");
        DateFormat df = authHandler.getDateOnlyFormat();
        if (!StringUtil.isNullOrEmpty(linkingdate)) {
            maxLinkingDate = df.parse(linkingdate);
        }
        String JENumbers=null;
        KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), company.getCompanyID());
        CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0); 
        HashMap<String, Object> prefparams = new HashMap<>();
        prefparams.put("id", companyid);
        Object columnPref = kwlCommonTablesDAOObj.getRequestedObjectFields(IndiaComplianceCompanyPreferences.class, new String[]{"istaxonadvancereceipt"}, prefparams);
        boolean istaxonadvancereceipt = false;
        if (columnPref != null) {
            istaxonadvancereceipt = Boolean.parseBoolean(columnPref.toString());
        }
        if (type==Constants.AdvancePayment) {
            String baseCurrency = requestObj.optString("baseCurrency");
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);
            LinkDetailReceipt rd = new LinkDetailReceipt();
            rd.setSrno(i + 1);
            rd.setID(StringUtil.generateUUID());
            double amountReceived = jobj.getDouble("enteramount");// amount in receipt currency
            double amountReceivedConverted = jobj.getDouble("enteramount"); // amount in invoice currency
            rd.setAmount(jobj.getDouble("enteramount"));
            rd.setCompany(company);
            KwlReturnObject result = accountingHandlerDAOobj.getObject(Invoice.class.getName(), jobj.getString("documentid"));
            Invoice invoice = (Invoice) result.getEntityList().get(0);
            rd.setInvoice((Invoice) result.getEntityList().get(0));
            double invoiceamountduebeforelink=invoice.getInvoiceAmountDueInBase();
            //JE For Invoice which is Linked to Invoice
            double exchangeRateforTransaction=jobj.optDouble("exchangeratefortransaction",1.0);
            if(invoice!=null){
                double finalAmountReval=ReevalJournalEntryForOpeningInvoice(requestObj, invoice,amountReceived,exchangeRateforTransaction);
                if (finalAmountReval != 0) {
                    String basecurrency = requestObj.optString("baseCurrency");
                    /**
                     * added transactionID and transactionModuleID to Realised
                     * JE.
                     */
                    counterMap.put("transactionModuleid", invoice.isIsOpeningBalenceInvoice() ? Constants.Acc_opening_Sales_Invoice : Constants.Acc_Invoice_ModuleId);
                    counterMap.put("transactionId", invoice.getID());
                    String revaljeid = PostJEFORReevaluation(requestObj, finalAmountReval, invoice.getCompany().getCompanyID(), preferences, basecurrency, null,counterMap);
                    rd.setRevalJeId(revaljeid);
                 }
            }
            //JE For Receipt which is of Opening Type
            if (receipt != null && (receipt.isIsOpeningBalenceReceipt() || (!receipt.getReceiptAdvanceDetails().isEmpty()))) {
                String basecurrency = requestObj.optString("baseCurrency");
                double finalAmountReval = ReevalJournalEntryForOpeningReceipt(requestObj, receipt, amountReceived, exchangeRateforTransaction);
                if (finalAmountReval != 0) {
                    /**
                     * added transactionID and transactionModuleID to Realised
                     * JE.
                     */
                    counterMap.put("transactionModuleid", receipt.isIsOpeningBalenceReceipt() ? Constants.Acc_opening_Receipt : Constants.Acc_Receive_Payment_ModuleId);
                    counterMap.put("transactionId", receipt.getID());
                    String revaljeid = PostJEFORReevaluation(requestObj, -(finalAmountReval), receipt.getCompany().getCompanyID(), preferences, basecurrency, null,counterMap);
                    rd.setRevalJeIdReceipt(revaljeid);
                }
            }
            if (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString()) && !StringUtil.isNullOrEmpty(invoice.getCurrency().getCurrencyID()) && !invoice.getCurrency().getCurrencyID().equals(receipt.getCurrency().getCurrencyID())) {
                    rd.setExchangeRateForTransaction(exchangeRateforTransaction);
                    rd.setFromCurrency(invoice.getCurrency());
                    rd.setToCurrency(receipt.getCurrency());
                    // adjusted exchange rate used to handle case like ERP-34884
                    double adjustedRate = exchangeRateforTransaction;
                    if(jobj.optDouble("amountdue", 0) !=0 && jobj.optDouble("amountDueOriginal", 0)!=0){
                       adjustedRate = jobj.optDouble("amountdue", 0) / jobj.optDouble("amountDueOriginal", 0); 
                    }
                    amountReceivedConverted = amountReceived / adjustedRate;
                    amountReceivedConverted = authHandler.round(amountReceivedConverted, companyid);
                    rd.setAmountInInvoiceCurrency(amountReceivedConverted);
                }else{
                    rd.setExchangeRateForTransaction(Double.parseDouble(jobj.get("exchangeratefortransaction").toString()));
                    rd.setFromCurrency(invoice.getCurrency());
                    rd.setToCurrency(receipt.getCurrency());
                    amountReceivedConverted = authHandler.round(amountReceived, companyid);
                    rd.setAmountInInvoiceCurrency(amountReceivedConverted);
                }
            if (greceipt != null) {
                rd.setGoodsReceipt(greceipt);
            }
            rd.setReceipt(receipt);
            if (jobj.has("rowjedid")) {
                rd.setROWJEDID(jobj.getString("rowjedid"));
            }          
            Date linkingDate = new Date();
//            Date invoiceDate = invoice.isIsOpeningBalenceInvoice() ? invoice.getCreationDate() : invoice.getJournalEntry().getEntryDate();
//            Date receiptDate = receipt.isIsOpeningBalenceReceipt() ? receipt.getCreationDate() : receipt.getJournalEntry().getEntryDate();
            Date invoiceDate = invoice.getCreationDate();
            Date receiptDate = receipt.getCreationDate();
            
            Date maxDate = null;
            if (maxLinkingDate != null) {
                maxDate = maxLinkingDate;
            } else {
                List<Date> datelist = new ArrayList<Date>();
                datelist.add(linkingDate);
                datelist.add(invoiceDate);
                datelist.add(receiptDate);
                Collections.sort(datelist); //Sort the Date object & get the dates in ASC order. Pick-up the last record as Max Date
                maxDate = datelist.get(datelist.size() - 1);
//                maxDate = Math.max(Math.max(linkingDate.getTime(), invoiceDate.getTime()), receiptDate.getTime());
            }
            
            rd.setReceiptLinkDate(maxDate);
            HashMap<String, JSONArray> jcustomarrayMap = receipt.getJcustomarrayMap();
            JSONArray jcustomarray = jcustomarrayMap.get(rd.getROWJEDID());
            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
            customrequestParams.put("customarray", jcustomarray);
            customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
            customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
            customrequestParams.put("modulerecid", rd.getROWJEDID());
            customrequestParams.put("recdetailId", rd.getID());
            customrequestParams.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
            customrequestParams.put("companyid", companyid);
            customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                KwlReturnObject receiptAccJEDCustomData = accountingHandlerDAOobj.getObject(AccJEDetailCustomData.class.getName(), rd.getROWJEDID());
                AccJEDetailCustomData accJEDetailCustomData = (AccJEDetailCustomData) receiptAccJEDCustomData.getEntityList().get(0);
                KwlReturnObject receiptJED = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), rd.getROWJEDID());
                JournalEntryDetail journalEntryDetail = (JournalEntryDetail) receiptJED.getEntityList().get(0);
                journalEntryDetail.setAccJEDetailCustomData(accJEDetailCustomData);
            }
            double amountReceivedConvertedInBaseCurrency = 0d;
            HashMap<String, Object> requestParams = new HashMap();
            requestParams.put(Constants.companyid, company.getCompanyID());
            requestParams.put("gcurrencyid", company.getCurrency().getCurrencyID());
            double externalCurrencyRate = 1d;
            boolean isopeningBalanceRCP = receipt.isIsOpeningBalenceReceipt();
            Date rcpCreationDate = null;
            rcpCreationDate = receipt.getCreationDate();
            if (isopeningBalanceRCP) {
                externalCurrencyRate = receipt.getExchangeRateForOpeningTransaction();
            } else {
//                rcpCreationDate = receipt.getJournalEntry().getEntryDate();
                externalCurrencyRate = receipt.getJournalEntry().getExternalCurrencyRate();
            }
            String fromcurrencyid = receipt.getCurrency().getCurrencyID();
            KwlReturnObject bAmt = null;
            if (isopeningBalanceRCP && receipt.isConversionRateFromCurrencyToBase()) {// if Receipt is opening balance Receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountReceived, fromcurrencyid, rcpCreationDate, externalCurrencyRate);
            } else {
                bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, amountReceived, fromcurrencyid, rcpCreationDate, externalCurrencyRate);
            }
            amountReceivedConvertedInBaseCurrency = (Double) bAmt.getEntityList().get(0);
            amountReceivedConvertedInBaseCurrency = authHandler.round(amountReceivedConvertedInBaseCurrency, companyid);
            /*
                Calculate Invoice amountdue on current amount received and exchangerate
            */
            double invoiceExternalCurrencyRate = 1d;
            boolean isopeningBalanceInvoice = invoice.isIsOpeningBalenceInvoice();
            Date invoiceCreationDate = null;
            invoiceCreationDate = invoice.getCreationDate();
            if (isopeningBalanceInvoice) {
                invoiceExternalCurrencyRate = invoice.getExchangeRateForOpeningTransaction();
            } else {
//                invoiceCreationDate = invoice.getJournalEntry().getEntryDate();
                invoiceExternalCurrencyRate = invoice.getJournalEntry().getExternalCurrencyRate();
            }
            String invoicefromcurrencyid = invoice.getCurrency().getCurrencyID();
            KwlReturnObject invoicebAmt = null;
            if (isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if Receipt is opening balance Receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                invoicebAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountReceivedConverted, invoicefromcurrencyid, invoiceCreationDate, invoiceExternalCurrencyRate);
            } else {
                invoicebAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, amountReceivedConverted, invoicefromcurrencyid, invoiceCreationDate, invoiceExternalCurrencyRate);
            }
            double invoiceamountReceivedConvertedInBaseCurrency = (Double) invoicebAmt.getEntityList().get(0);
            invoiceamountReceivedConvertedInBaseCurrency = authHandler.round(invoiceamountReceivedConvertedInBaseCurrency, companyid);
            //updateInvoiceAmountDue(invoice, receipt, company, amountReceivedConverted, invoiceamountReceivedConvertedInBaseCurrency);
            KwlReturnObject invoiceResult = updateInvoiceAmountDueAndReturnResult(invoice, receipt, company, amountReceivedConverted, invoiceamountReceivedConvertedInBaseCurrency);
            if (invoiceResult != null && invoiceResult.getEntityList() != null && invoiceResult.getEntityList().size() > 0) {
                Invoice inv = (Invoice) invoiceResult.getEntityList().get(0);
                if (inv.isIsOpeningBalenceInvoice() && inv.getOpeningBalanceAmountDue() == 0) {
                    try {
//                        DateFormat df = authHandler.getDateOnlyFormatter(request);
                        HashMap<String, Object> dataMap = new HashMap<String, Object>();
                        dataMap.put("amountduedate", maxDate);
                        accInvoiceDAOObj.saveInvoiceAmountDueZeroDate(inv, dataMap);
                    } catch (Exception ex) {
                        System.out.println("" + ex.getMessage());
                    }
                } else if (inv.getInvoiceamountdue() == 0) {
                    try {
//                        DateFormat df = authHandler.getDateOnlyFormatter(request);
                        HashMap<String, Object> dataMap = new HashMap<String, Object>();
                        dataMap.put("amountduedate",  maxDate);
                        accInvoiceDAOObj.saveInvoiceAmountDueZeroDate(inv, dataMap);
                    } catch (Exception ex) {
                        System.out.println("" + ex.getMessage());
                    }
                }
            }
            
                
            /*
                Start gains/loss calculation
                Calculate Gains/Loss if Invoice exchange rate changed at the time of linking with advance payment
            */
//            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
//            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            
            if(isopeningBalanceRCP && receipt.isConversionRateFromCurrencyToBase()){
                externalCurrencyRate=1/externalCurrencyRate;
            }
            if (preferences.getForeignexchange() == null) {
                throw new AccountingException(messageSource.getMessage("acc.common.forex", null, locale));
            }
            double amountDiff = checkFxGainLossOnLinkInvoices(invoice, Double.parseDouble(jobj.optString("exchangeratefortransaction","1")),externalCurrencyRate, amountReceived, receipt.getCurrency().getCurrencyID(), baseCurrency, company.getCompanyID());
            if (amountDiff != 0 && preferences.getForeignexchange() != null && Math.abs(amountDiff) >= 0.000001) {//Math.abs(amountDiff) < .0000001 Added this because in case of revaluation the diff in the rates was less than .0000001 and the amount calculation was going in Exachange rate account. 
                boolean rateDecreased = false;
                if (amountDiff < 0) {
                    rateDecreased = true;
                }
                JournalEntry journalEntry = null;
//                Map<String, Object> jeDataMap = StringUtil.jsonToMap(requestObj.optJSONObject("globalparams"));
                Map<String, Object> jeDataMap = AccountingManager.getGlobalParamsJson(requestObj.optJSONObject("paramJobj"));
                int counter = 0;
                if (counterMap.containsKey("counter")) {
                    counter = (Integer) counterMap.get("counter");
                }
                String jeentryNumber = null;
                String jeDatePrefix = "";
                String jeDateAfterPrefix = "";
                String jeDateSuffix = "";
                boolean jeautogenflag = false;
                String jeSeqFormatId = "";
                Date entryDate = null;
                if (maxLinkingDate != null) {
                    entryDate = new Date(maxLinkingDate.getTime());
                } else {
                    entryDate = new Date();
                }
                synchronized (this) {
                    HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                    JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                    JEFormatParams.put("modulename", "autojournalentry");
                    JEFormatParams.put("companyid", companyid);
                    JEFormatParams.put("isdefaultFormat", true);

                    KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                    SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, entryDate);
                    String nextAutoNoTemp = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    int sequence = Integer.parseInt((String)seqNumberMap.get(Constants.SEQNUMBER));
                    jeDatePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                    jeDateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
            
                    
                    sequence = sequence + counter;
                    String number = "" + sequence;
                    String action = "" + (sequence - counter);
                    nextAutoNoTemp.replaceAll(action, number);
                    jeentryNumber = nextAutoNoTemp.replaceAll(action, number);  //next auto generated number
                    jeSeqFormatId = format.getID();
                    jeautogenflag = true;

                    jeDataMap.put("entrynumber", jeentryNumber);
                    jeDataMap.put("autogenerated", jeautogenflag);
                    jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                    jeDataMap.put(Constants.SEQNUMBER, number);
                    jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
                    jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
                    jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
                    counter++;
                    counterMap.put("counter", counter);
                }
                jeDataMap.put("entrydate",entryDate); // ERP-8987    SDP-2944 FW: Linking Date for JE
                jeDataMap.put("companyid", companyid);
                jeDataMap.put("memo", "Exchange Gains/Loss posted against Advance Receipt '"+receipt.getReceiptNumber()+"' linked to Invoice '"+invoice.getInvoiceNumber()+"'");
                jeDataMap.put("currencyid", receipt.getCurrency().getCurrencyID());
                jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
                jeDataMap.put("isexchangegainslossje", true);
                jeDataMap.put("transactionId",receipt.getID());
                jeDataMap.put("transactionModuleid", Constants.Acc_Receive_Payment_ModuleId);
                journalEntry = accJournalEntryobj.getJournalEntry(jeDataMap);
                accJournalEntryobj.saveJournalEntryByObject(journalEntry);

                boolean isDebit = rateDecreased ? true : false;
                JSONObject jedjson = new JSONObject();
                jedjson.put("srno", 1);
                jedjson.put("companyid", companyid);
                jedjson.put("amount", rateDecreased ? (-1 * amountDiff) : amountDiff);
                jedjson.put("accountid", preferences.getForeignexchange().getID());
                jedjson.put("debit", isDebit);
                jedjson.put("jeid", journalEntry.getID());
                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                Set<JournalEntryDetail> detail = new HashSet();
                detail.add(jed);

                jedjson = new JSONObject();
                jedjson.put("srno", 2);
                jedjson.put("companyid", companyid);
                jedjson.put("amount", rateDecreased ? (-1 * amountDiff) : amountDiff);
                jedjson.put("accountid", invoice.getAccount().getID());
                jedjson.put("debit", !isDebit);
                jedjson.put("jeid", journalEntry.getID());
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                detail.add(jed);
                journalEntry.setDetails(detail);
                accJournalEntryobj.saveJournalEntryDetailsSet(detail);

                rd.setLinkedGainLossJE(journalEntry.getID());
            }

            /**
             * Post JE for Linking case for India Invoice
             */
            /**
             * Need to calculate amount on which adjustment JE need to be post
             * Formula is : 1) minus invoice amount excluding tax and its amount
             * due i.e. invoiceadjustlimitamt 2) Take link Amount i.e.
             * amountReceivedConverted 3) If invoiceadjustlimitamt<=0 then no
             * need to post JE 4) If invoiceadjustlimitamt <
             * amountReceivedConverted then calculate amount for JE using
             * invoiceadjustlimitamt 5) If invoiceadjustlimitamt >
             * amountReceivedConverted then calculate amount for JE using
             * amountReceivedConverted
             */
            double invoiceamtinbase = invoice.getExcludingGstAmountInBase();
            double invoiceadjustedamountbeforelink=invoice.getInvoiceamountinbase()-invoiceamountduebeforelink;
            double invoiceadjustlimitamt = invoiceamtinbase - invoiceadjustedamountbeforelink;
            double amountforadjustment = amountReceivedConverted;
            if (invoiceadjustlimitamt <= 0) {
                /**
                 * No Need to post JE
                 */
                amountforadjustment = 0d;
            } else if (invoiceadjustlimitamt < amountReceivedConverted) {
                amountforadjustment = invoiceadjustlimitamt;
            } else {
                amountforadjustment = amountReceivedConverted;
            }   
            Set<ReceiptAdvanceDetail> advanceDetails = receipt.getReceiptAdvanceDetails();
            List<ReceiptAdvanceDetailTermMap> advDetailTermMaps = new LinkedList<>();
            if (company.getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                if (!advanceDetails.isEmpty()) {
                    for (ReceiptAdvanceDetail advDetail : advanceDetails) {
                        String advId = advDetail.getId();
                        /**
                         * Get term details from Advance
                         */
                        JSONObject jSONObject = new JSONObject();
                        jSONObject.put("adId", advId);
                        KwlReturnObject kwlObject = accReceiptDAOobj.getAdvanceDetailsTerm(jSONObject);
                        advDetailTermMaps = kwlObject.getEntityList();
                    }
                }
            }
            if (amountforadjustment>0 && istaxonadvancereceipt && company.getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id) && receipt!=null && receipt.getReceiptAdvanceDetails()!=null
                    && !invoice.isRcmapplicable()&&!advDetailTermMaps.isEmpty()) {
                JournalEntry journalEntry = null;
//                Map<String, Object> jeDataMap = StringUtil.jsonToMap(requestObj.optJSONObject("globalparams"));
                Map<String, Object> jeDataMap = AccountingManager.getGlobalParamsJson(requestObj.optJSONObject("paramJobj"));
                int counter = 0;
                if (counterMap.containsKey("counter")) {
                    counter = (Integer) counterMap.get("counter");
                }
                String jeentryNumber = null;
                String jeDatePrefix = "";
                String jeDateAfterPrefix = "";
                String jeDateSuffix = "";
                boolean jeautogenflag = false;
                String jeSeqFormatId = "";
                Date entryDate = null;
                if (maxLinkingDate != null) {
                    entryDate = new Date(maxLinkingDate.getTime());
                } else {
                    entryDate = new Date();
                }
                synchronized (this) {
                    HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                    JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                    JEFormatParams.put("modulename", "autojournalentry");
                    JEFormatParams.put("companyid", companyid);
                    JEFormatParams.put("isdefaultFormat", true);
                    KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                    SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, entryDate);
                    String nextAutoNoTemp = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    int sequence = Integer.parseInt((String) seqNumberMap.get(Constants.SEQNUMBER));
                    jeDatePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    jeDateAfterPrefix = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                    jeDateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                    sequence = sequence + counter;
                    String number = "" + sequence;
                    String action = "" + (sequence - counter);
                    nextAutoNoTemp.replaceAll(action, number);
                    jeentryNumber = nextAutoNoTemp.replaceAll(action, number);  //next auto generated number
                    jeSeqFormatId = format.getID();
                    jeautogenflag = true;
                    jeDataMap.put("entrynumber", jeentryNumber);
                    if (StringUtil.isNullOrEmpty(JENumbers)) {
                        JENumbers = jeentryNumber;
                    } else {
                        JENumbers = jeentryNumber != "" ? JENumbers.concat("," + jeentryNumber) : "";
                    }
                    jeDataMap.put("autogenerated", jeautogenflag);
                    jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                    jeDataMap.put(Constants.SEQNUMBER, number);
                    jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
                    jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
                    jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
                    counter++;
                    counterMap.put("counter", counter);
                }
                jeDataMap.put("entrydate", entryDate); // ERP-8987    SDP-2944 FW: Linking Date for JE
                jeDataMap.put("companyid", companyid);
                jeDataMap.put("memo", "GST Adjustment posted against Advance Receipt '" + receipt.getReceiptNumber() + "' linked to Invoice '" + invoice.getInvoiceNumber() + "'");
                jeDataMap.put("currencyid", receipt.getCurrency().getCurrencyID());
                jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
//                jeDataMap.put("isexchangegainslossje", true);
                jeDataMap.put("transactionId", receipt.getID());
                jeDataMap.put("transactionModuleid", Constants.Acc_Receive_Payment_ModuleId);
                journalEntry = accJournalEntryobj.getJournalEntry(jeDataMap);
                accJournalEntryobj.saveJournalEntryByObject(journalEntry);

                /**
                 * need to get receipt details and its GST details
                 */
                if (!advanceDetails.isEmpty()) {
                    Set<JournalEntryDetail> detail = new HashSet();
                    for (ReceiptAdvanceDetail advanceDetail : advanceDetails) {
                        String adId = advanceDetail.getId();
                        /**
                         * Get term details from Advance
                         */
                        JSONObject jSONObject = new JSONObject();
                        jSONObject.put("adId", adId);
                        KwlReturnObject kro = accReceiptDAOobj.getAdvanceDetailsTerm(jSONObject);
                        List<ReceiptAdvanceDetailTermMap> advanceDetailTermMaps = kro.getEntityList();
                        int srcount = 1;
                        double totalpercentage=0d;
                            for (ReceiptAdvanceDetailTermMap receiptAdvanceDetailTermMap : advanceDetailTermMaps) {
                            totalpercentage +=receiptAdvanceDetailTermMap.getPercentage();
                            }
                            for (ReceiptAdvanceDetailTermMap receiptAdvanceDetailTermMap : advanceDetailTermMaps) {
                                double termamount = 0d;//receiptAdvanceDetailTermMap.getTermamount();
                                double percentage=receiptAdvanceDetailTermMap.getPercentage();
                                String gstAdvpayableAcc = receiptAdvanceDetailTermMap.getEntitybasedLineLevelTermRate().getLineLevelTerms().getPayableAccount().getID();
                                String gstAcc = receiptAdvanceDetailTermMap.getEntitybasedLineLevelTermRate().getLineLevelTerms().getAccount().getID();
                                /**
                                 * Calculate Amount Formula is written based on
                             * calculate gst amount by applying percentage on
                             * amount excluding tax amount Example : 
                             * Advance amount = 1000+50 =1050
                             * Invoice Amount = 500+25=525
                             * Linking JE amount = Calculate GST amount on 500 not on 525
                                 */
//                            double amtWithoutTax = (amountReceivedConverted * 100 / (100 + totalpercentage));
                                double amount = amountforadjustment * percentage / 100;
                                termamount = authHandler.round(amount, companyid);
                                JSONObject jedjson = new JSONObject();
                                jedjson.put("srno", srcount);
                                jedjson.put("companyid", companyid);
                                jedjson.put("amount", termamount);
                                jedjson.put("accountid", gstAcc);
                                jedjson.put("debit", true);
                                jedjson.put("jeid", journalEntry.getID());
                                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                detail.add(jed);
                                srcount++;

                                jedjson = new JSONObject();
                                jedjson.put("srno", srcount);
                                jedjson.put("companyid", companyid);
                                jedjson.put("amount", termamount);
                                jedjson.put("accountid", gstAdvpayableAcc);
                                jedjson.put("debit", false);
                                jedjson.put("jeid", journalEntry.getID());
                                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                detail.add(jed);
                                srcount++;
                            }
                        }
                    journalEntry.setDetails(detail);
                    accJournalEntryobj.saveJournalEntryDetailsSet(detail);
                    rd.setLinkedGSTJE(journalEntry.getID());
                }
            }
            // End Gains/Loss Calculation

            for (ReceiptAdvanceDetail advanceDetail : receipt.getReceiptAdvanceDetails()) {
//                advanceDetail.setAmountDue(advanceDetail.getAmountDue() - amountReceived);
                /**
                 * For link Transaction from Receipt, Update Amount Due with
                 * rounding.
                 */
                double finalamountdue = authHandler.round(advanceDetail.getAmountDue() - amountReceived, companyid);
                advanceDetail.setAmountDue(finalamountdue);
            }
            if (receipt != null && isopeningBalanceRCP) {
                double receiptAmountDue = receipt.getOpeningBalanceAmountDue();
                double receiptAmountDueInBase = receipt.getOpeningBalanceBaseAmountDue();
                    receiptAmountDue -= amountReceived;
                    receipt.setOpeningBalanceAmountDue(receiptAmountDue);
//                    receipt.setOpeningBalanceBaseAmountDue(receipt.getOpeningBalanceBaseAmountDue() - amountReceivedConvertedInBaseCurrency);
                    
                    if (isopeningBalanceRCP && receipt.isConversionRateFromCurrencyToBase()) {// if Receipt is opening balance Receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                        bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, receiptAmountDue, fromcurrencyid, rcpCreationDate, receipt.getExchangeRateForOpeningTransaction());
                    } else {
                        bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, receiptAmountDue, fromcurrencyid, rcpCreationDate, receipt.getExchangeRateForOpeningTransaction());
                    }
                    receiptAmountDueInBase = (Double) bAmt.getEntityList().get(0);
                    receiptAmountDueInBase = authHandler.round(receiptAmountDueInBase, companyid);
                    receipt.setOpeningBalanceBaseAmountDue(receiptAmountDueInBase);
                    HashMap<String, Object> receipthm = new HashMap<String, Object>();
                    receipthm.put("receiptid", receipt.getID());
                    receipthm.put("openingBalanceAmountDue", receipt.getOpeningBalanceAmountDue());
                    receipthm.put(Constants.openingBalanceBaseAmountDue, receipt.getOpeningBalanceBaseAmountDue());
                    accReceiptDAOobj.saveReceipt(receipthm);
                }
            linkdetails.add(rd); 
            linkDetails.put("linkDetailPayment", linkdetails);
        }       
        linkDetails.put("JournalEntries",JENumbers);
        }
        return linkDetails;
    }
    /*
        To implement below method we referred oldReceiptRowsAmount() which is used to calculate Forex Gains/Loss for invoices  
    */
    
    public double checkFxGainLossOnLinkInvoices(Invoice inv, double newInvoiceExchageRate, double paymentExchangeRate, double recinvamount, String paymentCurrency, String baseCurrency, String companyid) throws ServiceException {
        double amount = 0;
        HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
        GlobalParams.put("companyid", companyid);
        GlobalParams.put("gcurrencyid", baseCurrency);
        double goodsReceiptExchangeRate = 0d;
        Date goodsReceiptCreationDate = null;
        boolean isopeningBalanceInvoice = inv.isIsOpeningBalenceInvoice();
        if (inv.isNormalInvoice()) {
            goodsReceiptExchangeRate = inv.getJournalEntry().getExternalCurrencyRate();
//            goodsReceiptCreationDate = inv.getJournalEntry().getEntryDate();
        } else {
            goodsReceiptExchangeRate = inv.getExchangeRateForOpeningTransaction();
            if(isopeningBalanceInvoice && inv.isConversionRateFromCurrencyToBase()){//converting rate to Base to Other Currency Rate
                goodsReceiptExchangeRate=1/goodsReceiptExchangeRate;
            }
        }
        goodsReceiptCreationDate = inv.getCreationDate();

        boolean revalFlag = false;
        
        HashMap<String, Object> invoiceId = new HashMap<String, Object>();
        invoiceId.put("invoiceid", inv.getID());
        invoiceId.put("companyid", companyid);
        KwlReturnObject result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
        RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
        if (history != null) {
            goodsReceiptExchangeRate = history.getEvalrate();
            revalFlag = true;
        }
        String currid = inv.getCurrency().getCurrencyID();
        KwlReturnObject bAmt = null;
        if (currid.equalsIgnoreCase(paymentCurrency)) {
//            if (history == null && isopeningBalanceInvoice && inv.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
//                bAmt = accCurrencyobj.getOneCurrencyToOtherAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, paymentCurrency, goodsReceiptCreationDate, goodsReceiptExchangeRate);
//            } else {
                double paymentExternalCurrencyRate = paymentExchangeRate;
                if (goodsReceiptExchangeRate != paymentExternalCurrencyRate) {
                    bAmt = accCurrencyobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, paymentCurrency, goodsReceiptCreationDate, goodsReceiptExchangeRate, paymentExternalCurrencyRate);
                } else {
                    bAmt = accCurrencyobj.getOneCurrencyToOther(GlobalParams, 1.0, currid, paymentCurrency, goodsReceiptCreationDate, goodsReceiptExchangeRate);
            }
//            }
        } else {
//            if (history == null && isopeningBalanceInvoice && inv.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
//                bAmt = accCurrencyobj.getOneCurrencyToOtherModifiedAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, paymentCurrency, goodsReceiptCreationDate, goodsReceiptExchangeRate);
//            } else {
                double paymentExternalCurrencyRate = paymentExchangeRate;
                if (goodsReceiptExchangeRate != paymentExternalCurrencyRate) {
                    bAmt = accCurrencyobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, paymentCurrency, goodsReceiptCreationDate, goodsReceiptExchangeRate, paymentExternalCurrencyRate);
                } else {
                    bAmt = accCurrencyobj.getOneCurrencyToOtherModified(GlobalParams, 1.0, currid, paymentCurrency, goodsReceiptCreationDate, goodsReceiptExchangeRate);
                }
//        }
        }
        double oldrate = (Double) bAmt.getEntityList().get(0);
        double newrate = 0.0;
        double ratio = 0;
        if (newInvoiceExchageRate != oldrate && newInvoiceExchageRate != 0.0 
                && Math.abs(newInvoiceExchageRate - oldrate) >= 0.000001) {
            newrate = newInvoiceExchageRate;
            ratio = oldrate - newrate;
            amount = (recinvamount - (recinvamount / newrate) * oldrate);
        } 
        amount = authHandler.round(amount, companyid);
        return amount;
    }
    
    /*
     * Update invoice due amount when payment is being made against that
     * invoice.
     */
    public void updateInvoiceAmountDue(Invoice invoice, Receipt receipt, Company company, double amountReceivedForInvoice, double baseAmountReceivedForInvoice) throws JSONException, ServiceException {
        if (invoice != null) {
            double invoiceAmountDue = invoice.getOpeningBalanceAmountDue();
            invoiceAmountDue -= amountReceivedForInvoice;
            JSONObject invjson = new JSONObject();
            invjson.put("invoiceid", invoice.getID());
            invjson.put("companyid", company.getCompanyID());
            invjson.put("openingBalanceAmountDue", invoiceAmountDue);
            invjson.put(Constants.openingBalanceBaseAmountDue, invoice.getOpeningBalanceBaseAmountDue() - baseAmountReceivedForInvoice);
            invjson.put(Constants.invoiceamountdue, invoice.getInvoiceamountdue() - amountReceivedForInvoice);
            invjson.put(Constants.invoiceamountdueinbase, invoice.getInvoiceAmountDueInBase() - baseAmountReceivedForInvoice);
            accInvoiceDAOObj.updateInvoice(invjson, null);
        }
    }
    /*Function to update invoiceamountdue and return KwlReturnObject*/
    public KwlReturnObject updateInvoiceAmountDueAndReturnResult(Invoice invoice, Receipt receipt, Company company, double amountReceivedForInvoice, double baseAmountReceivedForInvoice) throws JSONException, ServiceException {
        KwlReturnObject result = null;
        if (invoice != null) {
            boolean isopeningBalanceInvoice = invoice.isIsOpeningBalenceInvoice();
            boolean isInvoiceIsClaimed = (invoice.getBadDebtType() == Constants.Invoice_Claimed || invoice.getBadDebtType() == Constants.Invoice_Recovered);
            JSONObject invjson = new JSONObject();
            invjson.put("invoiceid", invoice.getID());
            invjson.put("companyid", company.getCompanyID());
            if (isInvoiceIsClaimed) {
                invjson.put(Constants.claimAmountDue, invoice.getClaimAmountDue()-amountReceivedForInvoice);
            } else {
                double invoiceAmountDue = invoice.getOpeningBalanceAmountDue();
                invoiceAmountDue -= amountReceivedForInvoice;
                invjson.put("openingBalanceAmountDue", invoiceAmountDue);
                String invoicefromcurrencyid = invoice.getCurrency().getCurrencyID();
                KwlReturnObject invoicebAmt = null;
                double invoiceExternalCurrencyRate = 1d;
                if (isopeningBalanceInvoice) {
                    invoiceExternalCurrencyRate = invoice.getExchangeRateForOpeningTransaction();
                } else {
                    invoiceExternalCurrencyRate = invoice.getJournalEntry().getExternalCurrencyRate();
                }
                Date invoiceCreationDate = null;
                invoiceCreationDate = invoice.getCreationDate();
                HashMap<String, Object> requestParams = new HashMap();
                requestParams.put(Constants.companyid, company.getCompanyID());
                requestParams.put("gcurrencyid", company.getCurrency().getCurrencyID());
                if (isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if Receipt is opening balance Receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                    invoicebAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, invoiceAmountDue, invoicefromcurrencyid, invoiceCreationDate, invoiceExternalCurrencyRate);
                } else if (isopeningBalanceInvoice && !invoice.isConversionRateFromCurrencyToBase()) {
                    invoicebAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, invoiceAmountDue, invoicefromcurrencyid, invoiceCreationDate, invoiceExternalCurrencyRate);
                } else {
                    invoicebAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, (invoice.getInvoiceamountdue() - amountReceivedForInvoice), invoicefromcurrencyid, invoiceCreationDate, invoiceExternalCurrencyRate);
                }
                double invoiceAmountDueInBase = (Double) invoicebAmt.getEntityList().get(0);
                invoiceAmountDueInBase = authHandler.round(invoiceAmountDueInBase, company.getCompanyID());
//                invjson.put(Constants.openingBalanceBaseAmountDue, invoice.getOpeningBalanceBaseAmountDue() - baseAmountReceivedForInvoice);
                invjson.put(Constants.openingBalanceBaseAmountDue, invoiceAmountDueInBase);
                invjson.put(Constants.invoiceamountdue, invoice.getInvoiceamountdue() - amountReceivedForInvoice);
//                invjson.put(Constants.invoiceamountdueinbase, invoice.getInvoiceAmountDueInBase() - baseAmountReceivedForInvoice);
                invjson.put(Constants.invoiceamountdueinbase,invoiceAmountDueInBase);
            }
            result = accInvoiceDAOObj.updateInvoice(invjson, null);
        }
        return result;
    }

    /*
     * Update receipt due amount when payment is being made.
     */
    public void updateReceiptAmountDue(Receipt receipt, Company company, double amountReceived, double baseAmountReceivedConverted) throws JSONException, ServiceException {
        if (receipt != null) {
            double receiptAmountDue = receipt.getOpeningBalanceAmountDue();
            receiptAmountDue -= amountReceived;
            HashMap receipthm = new HashMap();
            receipthm.put("openingBalanceAmountDue", receiptAmountDue);
            if(receipt.isIsOpeningBalenceReceipt()) {
                receipthm.put(Constants.openingBalanceBaseAmountDue, receipt.getOpeningBalanceBaseAmountDue() - baseAmountReceivedConverted);
            }
            receipthm.put("receiptid", receipt.getID());
            receipthm.put("currencyid", receipt.getCurrency().getCurrencyID());
            receipthm.put("companyid", company.getCompanyID());
            accReceiptDAOobj.saveReceipt(receipthm);
        }
    }

    public ModelAndView getOpeningBalanceReceipts(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = getReceiptRequestMap(request);
            String customerId = request.getParameter("custVenId");
            requestParams.put("customerid", customerId);
            // get opening balance receipts created from opening balance button.
            KwlReturnObject result = accReceiptDAOobj.getOpeningBalanceReceipts(requestParams);
            List<Receipt> list = result.getEntityList();
            getOpeningBalanceReceiptJson(request, list, DataJArr);

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
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }

        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void getOpeningBalanceReceiptJson(HttpServletRequest request, List<Receipt> list, JSONArray dataArray) {
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
//            DecimalFormat decimalFormat = new DecimalFormat("#0.00");
            SimpleDateFormat sdf = (SimpleDateFormat) authHandler.getGlobalDateFormat();
            HashMap<String, Object> requestParams = getReceiptRequestMap(request);
            if (list != null && !list.isEmpty()) {
                Iterator it = list.iterator();
                while (it.hasNext()) {
                    Receipt receipt = (Receipt) it.next();

                    Date receiptCreationDate = null;
                    Double receiptAmount = 0d;

                    receiptCreationDate = receipt.getCreationDate();
                    receiptAmount = receipt.getDepositAmount();

                    double exchangeRateForOtherCurrency = receipt.getExchangeRateForOpeningTransaction();
                    boolean isopeningBalanceRecceipt = receipt.isIsOpeningBalenceReceipt();

                    JSONObject receiptJson = new JSONObject();
                    receiptJson.put("transactionId", receipt.getID());
                    receiptJson.put("exchangeRateForOtherCurrency", exchangeRateForOtherCurrency);
                    receiptJson.put("isCurrencyToBaseExchangeRate", receipt.isConversionRateFromCurrencyToBase());
                    receiptJson.put("isNormalTransaction", receipt.isNormalReceipt());
                    receiptJson.put("transactionNo", receipt.getReceiptNumber());
                    receiptJson.put("transactionAmount", authHandler.formattedAmount(receiptAmount, companyid));
                    receiptJson.put("transactionDate", sdf.format(receiptCreationDate));
                    receiptJson.put("currencysymbol", (receipt.getCurrency() == null ? "" : receipt.getCurrency().getSymbol()));
                    receiptJson.put("currencyid", (receipt.getCurrency() == null ? "" : receipt.getCurrency().getCurrencyID()));
                    receiptJson.put("transactionAmountDue", authHandler.formattedAmount(receipt.getOpeningBalanceAmountDue(), companyid));
                    receiptJson.put("chequeNumber", receipt.getChequeNumber());
                    receiptJson.put("drawnOn", receipt.getDrawnOn());
                    receiptJson.put("chequeDate", receipt.getChequeDate() != null ? df.format(receipt.getChequeDate()) : "");
                    double transactionAmountInBase = 0d;
                    if (Constants.OpeningBalanceBaseAmountFlag) {
                        transactionAmountInBase = receipt.getOriginalOpeningBalanceBaseAmount();
                    } else {
                        KwlReturnObject bAmt = null;
                        if (isopeningBalanceRecceipt && receipt.isConversionRateFromCurrencyToBase()) {// if receipt is opening balance receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, receiptAmount, receipt.getCurrency().getCurrencyID(), receipt.getCreationDate(), exchangeRateForOtherCurrency);
                        } else {
                            bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, receiptAmount, receipt.getCurrency().getCurrencyID(), receipt.getCreationDate(), exchangeRateForOtherCurrency);
                        }

                        transactionAmountInBase = (Double) bAmt.getEntityList().get(0);
                    }
                    receiptJson.put("transactionAmountInBase", authHandler.formattedAmount(transactionAmountInBase, companyid));
                    dataArray.put(receiptJson);
                }
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

    }

    public static HashMap<String, Object> getReceiptRequestMap(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put("ss", request.getParameter("ss"));
        requestParams.put("start", request.getParameter("start"));
        requestParams.put("limit", request.getParameter("limit"));
        requestParams.put("deleted", request.getParameter("deleted"));
        requestParams.put("nondeleted", request.getParameter("nondeleted"));
        requestParams.put(Constants.REQ_startdate, request.getParameter("stdate"));
        requestParams.put(Constants.REQ_enddate, request.getParameter("enddate"));
        requestParams.put(Constants.Acc_Search_Json, request.getParameter(Constants.Acc_Search_Json));
        requestParams.put(Constants.Filter_Criteria, request.getParameter(InvoiceConstants.Filter_Criteria));
        requestParams.put(Constants.moduleid, request.getParameter(Constants.moduleid));
        requestParams.put("ispendingAproval", request.getParameter("ispendingAproval"));
        if (request.getParameter("isView") != null) {
            requestParams.put("isView", request.getParameter("isView"));
        }
        if (request.getParameter("isCopyTransaction") != null) {
            if (request.getParameter("isCopyTransaction").equalsIgnoreCase("true")) {
                requestParams.put("isCopyTransaction", true);
            } else {
                requestParams.put("isCopyTransaction", false);
            }
        }
        return requestParams;
    }

    public ModelAndView saveBillingReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject jobjDetails = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean issuccess = false;
        boolean isChequePrint = false;

        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("BR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            List li = saveBillingReceipt(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String[] id = (String[]) li.get(0);
            issuccess = true;
            if (!StringUtil.isNullOrEmpty(request.getParameter("isChequePrint"))) {
                isChequePrint = Boolean.parseBoolean(request.getParameter("isChequePrint"));
            }
            if (isChequePrint) {
                SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
                Date creationDate = new Date(request.getParameter("creationdate"));
                String date = DATE_FORMAT.format(creationDate);
                String[] amount = (String[]) li.get(1);
                String[] amount1 = (String[]) li.get(2);
                String[] accName = (String[]) li.get(3);
                jobjDetails.put(amount[0], amount[1]);
                jobjDetails.put(amount1[0], amount1[1]);
                jobjDetails.put(accName[0], accName[1]);
                jobjDetails.put("date", date);
                jArr.put(jobjDetails);
            }
            msg = messageSource.getMessage("acc.receipt.save", null, RequestContextUtils.getLocale(request));   //"Receipt has been saved successfully";
            txnManager.commit(status);
            status = txnManager.getTransaction(def);
            deleteJEArray(id[0], companyid);
            txnManager.commit(status);
            status = txnManager.getTransaction(def);
            deleteChequeOrCard(id[1], companyid);
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            issuccess = false;
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArr);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public List saveBillingReceipt(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
        KwlReturnObject result;
        BillingReceipt receipt = null;
        String oldjeid = null;
        String Cardid = null;
        double amount = 0;
        String netinword = "";
        BillingGoodsReceipt bgreceipt = null;//Set for contra entry
        List ll = new ArrayList();
        try {
            Account dipositTo = null;

            double amountDiff = 0;
            boolean rateDecreased = false;
            String sequenceformat = request.getParameter("sequenceformat");
            String customfield = request.getParameter("customfield");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            double externalExchangeRate = StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String entryNumber = request.getParameter("no");
            boolean isMultiDebit = StringUtil.getBoolean(request.getParameter("ismultidebit"));
            boolean isAdvancePayment = StringUtil.getBoolean(request.getParameter("isadvpayment"));
            String receiptid = request.getParameter("billid");
            String methodid = request.getParameter("pmtmethod");
            boolean otherwise = ((request.getParameter("otherwise") != null) ? Boolean.parseBoolean(request.getParameter("otherwise")) : false);
            sessionHandlerImpl.updatePaymentMethodID(request, methodid);
            String drAccDetails = request.getParameter("detail");
            int receiptType = StringUtil.getInteger(request.getParameter("receipttype"));
            String jeid = null;
            String payDetailID = null;
            String jeentryNumber = null;
            boolean jeautogenflag = false;
            boolean bankReconsilationEntry = false, bankPayment = false;
            Date clearanceDate = null, startDate = null, endDate = null;
            String bankAccountId = "";
            Map<String, Object> bankReconsilationMap = new HashMap<String, Object>();
            boolean isadvanceFromVendor = StringUtil.getBoolean(request.getParameter("isadvanceFromVendor"));
            HashMap receipthm = new HashMap();
            boolean ismanydbcr = StringUtil.getBoolean(request.getParameter("ismanydbcr"));
            receipthm.put("isadvanceFromVendor", isadvanceFromVendor);
            receipthm.put("ismanydbcr", ismanydbcr);
            receipthm.put("receipttype", receiptType);
            if (receiptType == 6) {
                receipthm.put("vendor", request.getParameter("accid"));
            }
            double bankCharges = 0;
            double bankInterest = 0;
            String bankChargesAccid = request.getParameter("bankChargesCmb");
            String bankInterestAccid = request.getParameter("bankInterestCmb");
            boolean onlyAdvance = StringUtil.getBoolean(request.getParameter("onlyAdvance"));
            if (!StringUtil.isNullOrEmpty(request.getParameter("bankCharges")) && !StringUtil.isNullOrEmpty(bankChargesAccid)) {
                bankCharges = Double.parseDouble(request.getParameter("bankCharges"));
                receipthm.put("bankCharges", bankCharges);
                receipthm.put("bankChargesCmb", bankChargesAccid);
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("bankInterest")) && !StringUtil.isNullOrEmpty(bankInterestAccid)) {
                bankInterest = Double.parseDouble(request.getParameter("bankInterest"));
                receipthm.put("bankInterest", bankInterest);
                receipthm.put("bankInterestCmb", bankInterestAccid);
            }
            String paidToid = request.getParameter("paidToCmb");
            if (!StringUtil.isNullOrEmpty(request.getParameter("paidToCmb")) && !StringUtil.isNullOrEmpty(paidToid)) {
                receipthm.put("paidToCmb", paidToid);
            }

            if (!StringUtil.isNullOrEmpty(receiptid)) {
                KwlReturnObject receiptObj = accountingHandlerDAOobj.getObject(BillingReceipt.class.getName(), receiptid);
                receipt = (BillingReceipt) receiptObj.getEntityList().get(0);
                jeentryNumber = receipt.getJournalEntry().getEntryNumber();
                oldjeid = receipt.getJournalEntry().getID();
                jeautogenflag = receipt.getJournalEntry().isAutoGenerated();
                if (receipt.getPayDetail() != null) {
                    payDetailID = receipt.getPayDetail().getID();
                    if (receipt.getPayDetail().getCard() != null) {
                        Cardid = receipt.getPayDetail().getCard().getID();
                    }
                    if (receipt.getPayDetail().getCheque() != null) {
                        Cardid = receipt.getPayDetail().getCheque().getID();
                    }
                }
                result = accReceiptDAOobj.deleteBillingReceiptDetails(receiptid, companyid);
                result = accReceiptDAOobj.deleteBillingReceiptDetailsOtherwise(receiptid);
                if (receipt != null && receipt.getRevalJeId() != null) {
                    result = accJournalEntryobj.deleteJEDtails(receipt.getRevalJeId(), companyid);
                    result = accJournalEntryobj.deleteJE(receipt.getRevalJeId(), companyid);
                }

                receipthm.put("deposittojedetailid", null);
                receipthm.put("depositamount", 0.0);
                //Delete old entries and insert new entries again from optimized table in edit case.
                accJournalEntryobj.deleteOnEditAccountJEs_optimized(oldjeid);
            } else {
                result = accReceiptDAOobj.getBillingReceiptFromBillNo(entryNumber, companyid);
                int count = result.getRecordTotalCount();
                if (count > 0) {
                    throw new AccountingException(messageSource.getMessage("acc.field.Receiptnumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                }
                boolean seqformat_oldflag = StringUtil.getBoolean(request.getParameter("seqformat_oldflag"));
                String nextAutoNumber = "";
                String nextAutoNoInt = "";
                String datePrefix = "";
                String dateafterPrefix = "";
                String dateSuffix = "";
                if (seqformat_oldflag) {
                    nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_BILLINGRECEIPT, sequenceformat);
                } else {
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_BILLINGRECEIPT, sequenceformat, seqformat_oldflag, new Date());
                    nextAutoNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    nextAutoNoInt = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                    datePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    dateafterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                    dateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                    
                    receipthm.put(Constants.SEQFORMAT, sequenceformat);
                    receipthm.put(Constants.SEQNUMBER, nextAutoNoInt);
                    receipthm.put(Constants.DATEPREFIX, datePrefix);
                    receipthm.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
                    receipthm.put(Constants.DATESUFFIX, dateSuffix);
                }
                receipthm.put("entrynumber", entryNumber);
                receipthm.put("autogenerated", nextAutoNumber.equals(entryNumber));
            }

            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);
            
            int countryLanguageId = Constants.OtherCountryLanguageId; // 0
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
            if (extraCompanyPreferences.isAmountInIndianWord()) {
                countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
            }
            
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            String currencyid = (request.getParameter("currencyid") == null ? currency.getCurrencyID() : request.getParameter("currencyid"));

            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);

            receipthm.put("currencyid", currencyid);
            receipthm.put("externalCurrencyRate", externalExchangeRate);
            KwlReturnObject payresult = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), request.getParameter("pmtmethod"));
            PaymentMethod payMethod = (PaymentMethod) payresult.getEntityList().get(0);

            dipositTo = payMethod.getAccount();
            HashMap pdetailhm = new HashMap();
            pdetailhm.put("paymethodid", payMethod.getID());
            pdetailhm.put("companyid", company.getCompanyID());
            if (payMethod.getDetailType() != PaymentMethod.TYPE_CASH) {
                JSONObject obj = new JSONObject(request.getParameter("paydetail"));
                if (payMethod.getDetailType() == PaymentMethod.TYPE_BANK) {
                    bankPayment = true;
                    bankReconsilationEntry = obj.getString("paymentStatus") != null ? obj.getString("paymentStatus").equals("Cleared") : false;
                    if (bankReconsilationEntry) {
                        bankAccountId = request.getParameter("bankaccid");
                        startDate = df.parse(request.getParameter("startdate"));
                        endDate = df.parse(request.getParameter("enddate"));
                        clearanceDate = df.parse(obj.getString("clearanceDate"));
                        bankReconsilationMap.put("bankAccountId", bankAccountId);
                        bankReconsilationMap.put("startDate", startDate);
                        bankReconsilationMap.put("endDate", endDate);
                        bankReconsilationMap.put("clearanceDate", clearanceDate);
                        bankReconsilationMap.put("endingAmount", 0.0);
                        bankReconsilationMap.put("companyId", companyid);
                    }
                    HashMap chequehm = new HashMap();
                    chequehm.put("chequeno", obj.getString("chequeno"));
                    chequehm.put("description", StringUtil.DecodeText(obj.optString("description")));
                    chequehm.put("duedate", df.parse(obj.getString("payDate")));
                    chequehm.put("bankname", StringUtil.DecodeText(obj.optString("bankname")));
                    chequehm.put("bankmasteritemid", obj.getString("bankmasteritemid"));
                    Map<String, Object> seqchequehm = new HashMap<>();
                    obj.put(Constants.companyKey, companyid);
                    String chequesequenceformat =  obj.optString("sequenceformat");
                    /**
                     * getNextChequeNumber method to generate next sequence number using
                     * sequence format,also saving the dateprefix and datesuffix in cheque table.
                     */
                    if (!StringUtil.isNullOrEmpty(chequesequenceformat) && !chequesequenceformat.equals("NA")) {
                        seqchequehm = accCompanyPreferencesObj.getNextChequeNumber(obj);
                    }
                  
                    if (seqchequehm.containsKey(Constants.AUTO_ENTRYNUMBER)) {
                        chequehm.put("chequeno", (String) seqchequehm.get(Constants.AUTO_ENTRYNUMBER));
                    }
                    if (seqchequehm.containsKey(Constants.SEQNUMBER)) {
                        chequehm.put("sequenceNumber", (String) seqchequehm.get(Constants.SEQNUMBER));
                    }
                    if (seqchequehm.containsKey(Constants.DATEPREFIX)) {
                        chequehm.put(Constants.DATEPREFIX, (String) seqchequehm.get(Constants.DATEPREFIX));
                    }
                    if (seqchequehm.containsKey(Constants.DATEAFTERPREFIX)) {
                        chequehm.put(Constants.DATEAFTERPREFIX, (String) seqchequehm.get(Constants.DATEAFTERPREFIX));
                    }
                    if (seqchequehm.containsKey(Constants.DATESUFFIX)) {
                        chequehm.put(Constants.DATESUFFIX, (String) seqchequehm.get(Constants.DATESUFFIX));
                    }
                    KwlReturnObject cqresult = accPaymentDAOobj.addCheque(chequehm);
                    Cheque cheque = (Cheque) cqresult.getEntityList().get(0);
                    pdetailhm.put("chequeid", cheque.getID());
                } else if (payMethod.getDetailType() == PaymentMethod.TYPE_CARD) {
                    HashMap cardhm = new HashMap();
                    cardhm.put("cardno", obj.getString("cardno"));
                    cardhm.put("nameoncard", obj.getString("nameoncard"));
                    cardhm.put("expirydate", df.parse(obj.getString("expirydate")));
                    cardhm.put("cardtype", obj.getString("cardtype"));
                    cardhm.put("refno", obj.getString("refno"));
                    KwlReturnObject cdresult = accPaymentDAOobj.addCard(cardhm);
                    Card card = (Card) cdresult.getEntityList().get(0);
                    pdetailhm.put("cardid", card.getID());
                }
            }
            KwlReturnObject pdresult = null;
            if (!StringUtil.isNullOrEmpty(receiptid) && !StringUtil.isNullOrEmpty(payDetailID)) {
                pdetailhm.put("paydetailid", payDetailID);
            }
            pdresult = accPaymentDAOobj.addPayDetail(pdetailhm);
            PayDetail pdetail = (PayDetail) pdresult.getEntityList().get(0);
            receipthm.put("paydetailsid", pdetail.getID());
            receipthm.put("memo", request.getParameter("memo"));
            receipthm.put("companyid", company.getCompanyID());

            Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
            if (StringUtil.isNullOrEmpty(oldjeid)) {
                String nextJEAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_JOURNALENTRY);
                jeentryNumber = nextJEAutoNo;// + "/" + entryNumber;
                jeautogenflag = true;
            }
            jeDataMap.put("entrynumber", jeentryNumber);
            jeDataMap.put("autogenerated", jeautogenflag);
            jeDataMap.put("entrydate", df.parse(request.getParameter("creationdate")));
            jeDataMap.put("companyid", company.getCompanyID());
            jeDataMap.put("memo", request.getParameter("memo"));
            jeDataMap.put("currencyid", currencyid);
            HashSet jedetails = new HashSet();
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            jeid = journalEntry.getID();
            jeDataMap.put("jeid", jeid);
            String detail = request.getParameter("detail");
            JSONArray jArr = new JSONArray();
            if (!StringUtil.isNullOrEmpty(detail)) {
                jArr = new JSONArray(detail);
            }
            if (jArr.length() > 0 && !isMultiDebit) {
                amount = 0;
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);
                    amount += jobj.getDouble("payment");
                }
                amountDiff = oldBillingReceiptRowsAmount(request, jArr, currencyid, externalExchangeRate);

                if (preferences.getForeignexchange() == null) {
                    throw new AccountingException(messageSource.getMessage("acc.receipt.forex", null, RequestContextUtils.getLocale(request)));
                }
                if (amountDiff != 0 && preferences.getForeignexchange() != null) {
                    if (amountDiff < 0) {
                        rateDecreased = true;
                    }
                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", rateDecreased ? (-1 * amountDiff) : amountDiff);
                    jedjson.put("accountid", preferences.getForeignexchange().getID());
                    jedjson.put("debit", rateDecreased ? false : true);
                    jedjson.put("jeid", jeid);
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                }
            } else {
                amount = Double.parseDouble(request.getParameter("amount"));
            }
            JSONObject jedjson = null;
            KwlReturnObject jedresult = null;
            JournalEntryDetail jed = null;
            boolean taxExist = false;
            List receiptOtherwiseList = new ArrayList();
            HashMap receiptdetailotherwise = new HashMap();
            if (isMultiDebit) {
                JSONArray drAccArr = new JSONArray(drAccDetails);
                for (int i = 0; i < drAccArr.length(); i++) {
                    JSONObject jobj = drAccArr.getJSONObject(i);
                    boolean isdebit = jobj.has("isdebit") ? Boolean.parseBoolean(jobj.getString("isdebit")) : false;
                    jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", Double.parseDouble(jobj.getString("dramount")));
                    jedjson.put("accountid", jobj.getString("accountid"));
                    jedjson.put("debit", isdebit);//false);
                    jedjson.put("jeid", jeid);
                    jedjson.put("description", jobj.getString("description"));
                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                    double rowtaxamount = 0;

                    if (receiptType == 2 || receiptType == 9) {
                        String rowtaxid = jobj.getString("prtaxid");
                        //taxExist=StringUtil.isNullOrEmpty(rowtaxid)?false:true;
                        KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), rowtaxid); // (Tax)session.get(Tax.class, taxid);
                        Tax rowtax = (Tax) txresult.getEntityList().get(0);
                        if (rowtax == null || rowtaxid.equalsIgnoreCase("-1")) {
                            receiptdetailotherwise.put("amount", Double.parseDouble(jobj.getString("dramount")));
                            receiptdetailotherwise.put("taxjedid", "");
                            receiptdetailotherwise.put("tax", rowtaxid.equalsIgnoreCase("-1") ? "None" : "");
                            receiptdetailotherwise.put("accountid", jobj.getString("accountid"));
                            receiptdetailotherwise.put("isdebit", isdebit);
                            receiptdetailotherwise.put("taxamount", rowtaxamount);
                            receiptdetailotherwise.put("description", jobj.getString("description"));
                            result = accReceiptDAOobj.saveBillingReceiptDetailOtherwise(receiptdetailotherwise);
                            receiptdetailotherwise.clear();
                            BillingReceiptDetailOtherwise receiptDetailOtherwise = null;
                            receiptDetailOtherwise = (BillingReceiptDetailOtherwise) result.getEntityList().get(0);
                            receiptOtherwiseList.add(receiptDetailOtherwise.getID());
                        } else {
                            rowtaxamount = Double.parseDouble(jobj.getString("curamount")) - Double.parseDouble(jobj.getString("dramount"));
                            jedjson = new JSONObject();
                            jedjson.put("srno", jedetails.size() + 1);
                            jedjson.put("companyid", company.getCompanyID());
                            jedjson.put("amount", authHandler.formattedAmount(rowtaxamount, companyid));
                            jedjson.put("accountid", rowtax.getAccount().getID());
                            jedjson.put("debit", isdebit);//false);
                            jedjson.put("jeid", jeid);
                            jedjson.put("description", jobj.getString("description"));
                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jedetails.add(jed);

                            receiptdetailotherwise.put("amount", Double.parseDouble(jobj.getString("dramount")));
                            receiptdetailotherwise.put("isdebit", isdebit);
                            receiptdetailotherwise.put("taxjedid", jed.getID());
                            receiptdetailotherwise.put("tax", rowtax.getID());
                            receiptdetailotherwise.put("accountid", jobj.getString("accountid"));
                            receiptdetailotherwise.put("taxamount", rowtaxamount);
                            receiptdetailotherwise.put("description", jobj.getString("description"));
                            result = accReceiptDAOobj.saveBillingReceiptDetailOtherwise(receiptdetailotherwise);
                            receiptdetailotherwise.clear();
                            BillingReceiptDetailOtherwise receiptDetailOtherwise = null;
                            receiptDetailOtherwise = (BillingReceiptDetailOtherwise) result.getEntityList().get(0);
                            receiptOtherwiseList.add(receiptDetailOtherwise.getID());
                        }

                    }

                }
            } else {
                jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put("companyid", companyid);
                jedjson.put("amount", amount + amountDiff);
                jedjson.put("accountid", request.getParameter("accid"));
                jedjson.put("debit", false);
                jedjson.put("jeid", jeid);
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jedetails.add(jed);
            }

            if (bankCharges != 0) {
                if (!isAdvancePayment || (isAdvancePayment && onlyAdvance)) {
                    amount -= bankCharges;
                    jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    //(If currency USD and base currency SGD, inv rate 0.81 n pay rate 0.80 then amount = 1234.57, amountDiff = -15.24, depositAmt = 1250 SGD)
                    jedjson.put("amount", bankCharges);
                    jedjson.put("accountid", bankChargesAccid);
                    jedjson.put("debit", true);
                    jedjson.put("jeid", jeid);
                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                }
            }

            if (bankInterest != 0) {
                if (!isAdvancePayment || (isAdvancePayment && onlyAdvance)) {
                    amount -= bankInterest;
                    jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    //(If currency USD and base currency SGD, inv rate 0.81 n pay rate 0.80 then amount = 1234.57, amountDiff = -15.24, depositAmt = 1250 SGD)
                    jedjson.put("amount", bankInterest);
                    jedjson.put("accountid", bankInterestAccid);
                    jedjson.put("debit", true);
                    jedjson.put("jeid", jeid);
                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                }
            }

            if (amount != 0) {
                jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put("companyid", companyid);
                //(If currency USD and base currency SGD, inv rate 0.81 n pay rate 0.80 then amount = 1234.57, amountDiff = -15.24, depositAmt = 1250 SGD)
                jedjson.put("amount", amount);
                jedjson.put("accountid", dipositTo.getID());
                jedjson.put("debit", true);
                jedjson.put("jeid", jeid);
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jedetails.add(jed);
                receipthm.put("deposittojedetailid", jed.getID());
                receipthm.put("depositamount", amount);
            }
            jeDataMap.put("jedetails", jedetails);
            jeDataMap.put("externalCurrencyRate", externalExchangeRate);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);

            receipthm.put("journalentryid", journalEntry.getID());

            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                customrequestParams.put("modulerecid", journalEntry.getID());
                customrequestParams.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    jeDataMap.put("accjecustomdataref", journalEntry.getID());
                    jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);
                }
            }

            if (!StringUtil.isNullOrEmpty(receiptid)) {
                receipthm.put("billingreceiptid", receipt.getID());
            }
            result = accReceiptDAOobj.saveBillingReceipt(receipthm);
            receipt = (BillingReceipt) result.getEntityList().get(0);
            receipthm.put("billingreceiptid", receipt.getID());

            HashSet receiptDetails = new HashSet();
            if (!isMultiDebit) {
                receiptDetails = saveBillingReceiptRows(receipt, company, jArr, bgreceipt);
            }
            receipthm.put("receiptdetails", receiptDetails);

            result = accReceiptDAOobj.saveBillingReceipt(receipthm);
            receipt = (BillingReceipt) result.getEntityList().get(0);
            if (receiptType == 2 || receiptType == 9) {//otherwise case and GL Code Case 
                for (int i = 0; i < receiptOtherwiseList.size(); i++) {
                    receiptdetailotherwise.put("billingreceipt", receipt.getID());
                    receiptdetailotherwise.put("receiptotherwise", receiptOtherwiseList.get(i));
                    result = accReceiptDAOobj.saveBillingReceiptDetailOtherwise(receiptdetailotherwise);
                    receiptdetailotherwise.clear();
                }
            }
            if (bankReconsilationEntry) {
                bankReconsilationMap.put("clearingamount", amount);
                bankReconsilationMap.put("currencyid", currencyid);
                bankReconsilationMap.put("details", jArr);
                bankReconsilationMap.put("ismultidebit", isMultiDebit);
                bankReconsilationMap.put("breceipt", receipt);
                if (!StringUtil.isNullOrEmpty(oldjeid)) {
                    bankReconsilationMap.put("oldjeid", oldjeid);
                }
                HashMap<String, Object> globalParams = AccountingManager.getGlobalParams(request);
                saveBankReconsilation(bankReconsilationMap, globalParams);
            }
            if (bankPayment && !bankReconsilationEntry && !StringUtil.isNullOrEmpty(oldjeid)) {
                bankReconsilationMap.put("oldjeid", oldjeid);
                bankReconsilationMap.put("companyId", companyid);
                deleteBankReconcilation(bankReconsilationMap);
            }

            //Insert new entries again in optimized table.
            accJournalEntryobj.saveAccountJEs_optimized(jeid);

            netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(amount)), currency,countryLanguageId);
            auditTrailObj.insertAuditLog(AuditAction.RECEIPT_ADDED, "User " + sessionHandlerImpl.getUserFullName(request) + " created new receipt ", request, receipt.getID());
            if (jArr.length() > 0 && !isMultiDebit) {
                double finalAmountReval = 0;
                String basecurrency = sessionHandlerImpl.getCurrencyID(request);
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);
                    double ratio = 0;
                    double amountReval = 0;
                    double amountdue = jobj.getDouble("payment");
                    HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
                    GlobalParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
                    GlobalParams.put("gcurrencyid", basecurrency);
                    GlobalParams.put("dateformat", authHandler.getDateOnlyFormat(request));
                    Date creationDate = authHandler.getDateOnlyFormat(request).parse(request.getParameter("creationdate"));
                    result = accountingHandlerDAOobj.getObject(BillingInvoice.class.getName(), jobj.getString("billid"));
                    BillingInvoice invoice = (BillingInvoice) result.getEntityList().get(0);
                    double exchangeRate = invoice.getJournalEntry().getExternalCurrencyRate();
                    HashMap<String, Object> invoiceId = new HashMap<String, Object>();
                    invoiceId.put("invoiceid", invoice.getID());
                    invoiceId.put("companyid", sessionHandlerImpl.getCompanyid(request));
                    invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    RevaluationHistory revalueationHistory = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (revalueationHistory != null) {
                        exchangeRate = revalueationHistory.getEvalrate();
                    }

                    result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrency);
                    currency = (KWLCurrency) result.getEntityList().get(0);
                    String currid = currency.getCurrencyID();
                    if (invoice.getCurrency() != null) {
                        currid = invoice.getCurrency().getCurrencyID();
                    }
                    //            double oldrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, gr.getJournalEntry().getEntryDate());
                    KwlReturnObject bAmt = accCurrencyobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, invoice.getJournalEntry().getEntryDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                    double oldrate = (Double) bAmt.getEntityList().get(0);
                    //            double newrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
                    bAmt = accCurrencyobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, creationDate, exchangeRate);
                    double newrate = (Double) bAmt.getEntityList().get(0);
                    ratio = oldrate - newrate;
                    amountReval = amountdue * ratio;
                    finalAmountReval = finalAmountReval + amountReval;
                }
                if (finalAmountReval != 0) {
                    Map<String,Object> counterMap=new HashMap<>();
                    counterMap.put("counter", 0);
                    JSONObject requestObj = convertRequestToJSONObjectForLinkPayment(request);
                    /**
                     * added transactionID and transactionModuleID to Realised
                     * JE.
                     */
                    counterMap.put("transactionModuleid", Constants.Acc_Receive_Payment_ModuleId);
                    counterMap.put("transactionId", receipt.getID());
                    String revaljeid = PostJEFORReevaluation(requestObj, finalAmountReval, companyid, preferences, basecurrency,null,counterMap);
                    receipthm.clear();
                    receipthm.put("billingreceiptid", receipt.getID());
                    receipthm.put("revalJeId", revaljeid);
                    result = accReceiptDAOobj.saveBillingReceipt(receipthm);

                }
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveBillingReceipt : " + ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveBillingReceipt : " + ex.getMessage(), ex);
        }
        KwlReturnObject accresult = accountingHandlerDAOobj.getObject(Account.class.getName(), request.getParameter("accid"));
        Account account = (Account) accresult.getEntityList().get(0);
        String accountName = "";
        if (account != null) {
            accountName = account.getName();
        }
        ll.add(new String[]{oldjeid, Cardid});
        ll.add(new String[]{"amount", String.valueOf(amount)});
        ll.add(new String[]{"amountinword", netinword});
        ll.add(new String[]{"accountName", accountName});
        return (ArrayList) ll;
    }

    public ModelAndView saveContraBillingReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject jobjDetails = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean issuccess = false;
        boolean isChequePrint = false;

        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("BR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            List li = saveContraBillingReceipt(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String[] id = (String[]) li.get(0);
            issuccess = true;
            msg = messageSource.getMessage("acc.contra.save", null, RequestContextUtils.getLocale(request));   //"Receipt has been saved successfully";
            txnManager.commit(status);
//            status = txnManager.getTransaction(def);
//            deleteJEArray(id[0],companyid);
//            txnManager.commit(status);
//            status = txnManager.getTransaction(def);
//            deleteChequeOrCard(id[1],companyid);
//            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            issuccess = false;
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArr);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public List saveContraBillingReceipt(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
        KwlReturnObject result;
        BillingReceipt receipt = null;
        String oldjeid = null;
        String Cardid = null;
        double amount = 0;
        String netinword = "";
        List ll = new ArrayList();
        try {
            String maininvoiceid = request.getParameter("maininvoiceid");//Billing Goods Receipt
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(BillingGoodsReceipt.class.getName(), maininvoiceid);
            BillingGoodsReceipt bgreceipt = (BillingGoodsReceipt) cmpresult.getEntityList().get(0);
            Account dipositTo = bgreceipt.getVendor().getAccount();

            double amountDiff = 0;
            boolean rateDecreased = false;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String sequenceformat = request.getParameter("sequenceformat");
            double externalExchangeRate = StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String entryNumber = request.getParameter("no");
            boolean isMultiDebit = StringUtil.getBoolean(request.getParameter("ismultidebit"));
            String receiptid = request.getParameter("billid");
//            String methodid =request.getParameter("pmtmethod");            
//            request.getSession().setAttribute("methodid", methodid); 
            String drAccDetails = request.getParameter("detail");
            String jeid = null;
            String jeentryNumber = null;
            boolean jeautogenflag = false;

            HashMap receipthm = new HashMap();

            result = accReceiptDAOobj.getBillingReceiptFromBillNo(entryNumber, companyid);
            int count = result.getRecordTotalCount();
            if (count > 0) {
                throw new AccountingException(messageSource.getMessage("acc.field.Receiptnumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
            }
            boolean seqformat_oldflag = StringUtil.getBoolean(request.getParameter("seqformat_oldflag"));
            String nextAutoNumber = "";
            String nextAutoNoInt = "";
            String datePrefix = "";
            String dateafterPrefix = "";
            String dateSuffix = "";
            if (seqformat_oldflag) {
                nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_BILLINGRECEIPT, sequenceformat);
            } else {
                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_BILLINGRECEIPT, sequenceformat, seqformat_oldflag, new Date());
                nextAutoNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                nextAutoNoInt = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                datePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                dateafterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                dateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                receipthm.put(Constants.SEQFORMAT, sequenceformat);
                receipthm.put(Constants.SEQNUMBER, nextAutoNoInt);
                receipthm.put(Constants.DATEPREFIX, datePrefix);
                receipthm.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
                receipthm.put(Constants.DATESUFFIX, dateSuffix);
            }
            receipthm.put("entrynumber", entryNumber);
            receipthm.put("autogenerated", nextAutoNumber.equals(entryNumber));

            cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            String currencyid = (request.getParameter("currencyid") == null ? currency.getCurrencyID() : request.getParameter("currencyid"));

            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);

            receipthm.put("currencyid", currencyid);
            receipthm.put("externalCurrencyRate", externalExchangeRate);
//            KwlReturnObject payresult = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), request.getParameter("pmtmethod"));
//            PaymentMethod payMethod = (PaymentMethod) payresult.getEntityList().get(0);
//
//            dipositTo = payMethod.getAccount();
//            HashMap pdetailhm = new HashMap();
//            pdetailhm.put("paymethodid", payMethod.getID());
//            pdetailhm.put("companyid", company.getCompanyID());
//            
//            KwlReturnObject pdresult=null;
//            if (!StringUtil.isNullOrEmpty(receiptid)&&!StringUtil.isNullOrEmpty(payDetailID))
//                pdetailhm.put("paydetailid", payDetailID);
//            pdresult = accPaymentDAOobj.addPayDetail(pdetailhm);            
//            PayDetail pdetail = (PayDetail) pdresult.getEntityList().get(0);
//            receipthm.put("paydetailsid", pdetail.getID());
            receipthm.put("memo", request.getParameter("memo"));
            receipthm.put("companyid", company.getCompanyID());

            Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
            if (StringUtil.isNullOrEmpty(oldjeid)) {
                String nextJEAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_JOURNALENTRY);
                jeentryNumber = nextJEAutoNo;// + "/" + entryNumber;
                jeautogenflag = true;
            }
            jeDataMap.put("entrynumber", jeentryNumber);
            jeDataMap.put("autogenerated", jeautogenflag);
            jeDataMap.put("entrydate", df.parse(request.getParameter("creationdate")));
            jeDataMap.put("companyid", company.getCompanyID());
            jeDataMap.put("memo", " Contra Entry : " + request.getParameter("memo"));
            jeDataMap.put("currencyid", currencyid);
            HashSet jedetails = new HashSet();
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            jeid = journalEntry.getID();
            jeDataMap.put("jeid", jeid);
            String detail = request.getParameter("detail");
            JSONArray jArr = new JSONArray();
            if (!StringUtil.isNullOrEmpty(detail)) {
                jArr = new JSONArray(detail);
            }
            if (jArr.length() > 0 && !isMultiDebit) {
                amount = 0;
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);
                    amount += jobj.getDouble("payment");
                }
//                amountDiff = oldBillingReceiptRowsAmount(request, jArr, currencyid,externalExchangeRate);
//
//                if (preferences.getForeignexchange() == null) {
//                    throw new AccountingException(messageSource.getMessage("acc.receipt.forex", null, RequestContextUtils.getLocale(request)));
//                }
//                if (amountDiff != 0 && preferences.getForeignexchange() != null) {
//                    if(amountDiff < 0) {
//                        rateDecreased = true;
//                    }
//                    JSONObject jedjson = new JSONObject();
//                    jedjson.put("srno", jedetails.size()+1);
//                    jedjson.put("companyid", companyid);
//                    jedjson.put("amount", rateDecreased?(-1*amountDiff):amountDiff);
//                    jedjson.put("accountid", preferences.getForeignexchange().getID());
//                    jedjson.put("debit", rateDecreased?false:true);
//                    jedjson.put("jeid", jeid);
//                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
//                    JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
//                    jedetails.add(jed);
//                }
            } else {
                amount = Double.parseDouble(request.getParameter("amount"));
            }
            JSONObject jedjson = null;
            KwlReturnObject jedresult = null;
            JournalEntryDetail jed = null;
            if (isMultiDebit) {
                JSONArray drAccArr = new JSONArray(drAccDetails);
                for (int i = 0; i < drAccArr.length(); i++) {
                    JSONObject jobj = drAccArr.getJSONObject(i);
                    jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", Double.parseDouble(jobj.getString("dramount")));
                    jedjson.put("accountid", jobj.getString("accountid"));
                    jedjson.put("debit", false);
                    jedjson.put("jeid", jeid);
                    jedjson.put("description", jobj.getString("description"));
                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                }
            } else {
                jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put("companyid", companyid);
                jedjson.put("amount", amount + amountDiff);
                jedjson.put("accountid", request.getParameter("accid"));
                jedjson.put("debit", false);
                jedjson.put("jeid", jeid);
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jedetails.add(jed);
            }

            jedjson = new JSONObject();
            jedjson.put("srno", jedetails.size() + 1);
            jedjson.put("companyid", companyid);
            //(If currency USD and base currency SGD, inv rate 0.81 n pay rate 0.80 then amount = 1234.57, amountDiff = -15.24, depositAmt = 1250 SGD)
            jedjson.put("amount", amount);
            jedjson.put("accountid", dipositTo.getID());
            jedjson.put("debit", true);
            jedjson.put("jeid", jeid);
            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetails.add(jed);

            jeDataMap.put("jedetails", jedetails);
            jeDataMap.put("externalCurrencyRate", externalExchangeRate);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);

            receipthm.put("journalentryid", journalEntry.getID());
            receipthm.put("contraentry", true);
            if (!StringUtil.isNullOrEmpty(receiptid)) {
                receipthm.put("billingreceiptid", receipt.getID());
            }
            result = accReceiptDAOobj.saveBillingReceipt(receipthm);
            receipt = (BillingReceipt) result.getEntityList().get(0);
            receipthm.put("billingreceiptid", receipt.getID());

            HashSet receiptDetails = new HashSet();
            if (!isMultiDebit) {
                receiptDetails = saveBillingReceiptRows(receipt, company, jArr, bgreceipt);
            }
            receipthm.put("receiptdetails", receiptDetails);

            result = accReceiptDAOobj.saveBillingReceipt(receipthm);
            receipt = (BillingReceipt) result.getEntityList().get(0);

            //Insert new entries again in optimized table.
            accJournalEntryobj.saveAccountJEs_optimized(jeid);

            //To do - need to make audit entry
//            auditTrailObj.insertAuditLog(AuditAction.RECEIPT_ADDED, "User "+sessionHandlerImpl.getUserFullName(request) +" created new receipt ", request, receipt.getID());       
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveBillingReceipt : " + ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveBillingReceipt : " + ex.getMessage(), ex);
        }
        KwlReturnObject accresult = accountingHandlerDAOobj.getObject(Account.class.getName(), request.getParameter("accid"));
        Account account = (Account) accresult.getEntityList().get(0);
        String accountName = "";
        if (account != null) {
            accountName = account.getName();
        }
        ll.add(new String[]{oldjeid, Cardid});
        ll.add(new String[]{"amount", String.valueOf(amount)});
        ll.add(new String[]{"amountinword", netinword});
        ll.add(new String[]{"accountName", accountName});
        return (ArrayList) ll;
    }

    private HashSet saveBillingReceiptRows(BillingReceipt receipt, Company company, JSONArray jArr, BillingGoodsReceipt bgreceipt) throws JSONException, ServiceException {
        HashSet details = new HashSet();
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);
            BillingReceiptDetail rd = new BillingReceiptDetail();
            rd.setSrno(i + 1);
            rd.setAmount(jobj.getDouble("payment"));
            rd.setCompany(company);
            KwlReturnObject result = accountingHandlerDAOobj.getObject(BillingInvoice.class.getName(), jobj.getString("billid"));
            rd.setBillingInvoice((BillingInvoice) result.getEntityList().get(0));
            rd.setBillingReceipt(receipt);
            if (bgreceipt != null) {
                rd.setBillingGoodsReceipt(bgreceipt);
            }
            details.add(rd);
        }
        return details;
    }

    private double oldBillingReceiptRowsAmount(HttpServletRequest request, JSONArray jArr, String currencyid, double externalExchangeRate) throws JSONException, ServiceException, SessionExpiredException, ParseException {
        double ratio = 0;
        double amount = 0;
        KwlReturnObject result;
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
        requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);
//            BillingInvoice invoice = (BillingInvoice) session.get(BillingInvoice.class, jobj.getString("billid"));
            result = accountingHandlerDAOobj.getObject(BillingInvoice.class.getName(), jobj.getString("billid"));
            BillingInvoice invoice = (BillingInvoice) result.getEntityList().get(0);
            double exchangeRate = invoice.getJournalEntry().getExternalCurrencyRate();
            HashMap<String, Object> invoiceId = new HashMap<String, Object>();
            invoiceId.put("invoiceid", invoice.getID());
            invoiceId.put("companyid", sessionHandlerImpl.getCompanyid(request));
            //invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
            result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
            RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
            if (history != null) {
                exchangeRate = history.getEvalrate();
            }
//            KWLCurrency currency = (KWLCurrency) session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
            String currid = currency.getCurrencyID();
            if (invoice.getCurrency() != null) {
                currid = invoice.getCurrency().getCurrencyID();
            }

//            double oldrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, invoice.getJournalEntry().getEntryDate());
            result = accCurrencyobj.getCurrencyToBaseAmount(requestParams, 1.0, currid, invoice.getJournalEntry().getEntryDate(), exchangeRate);
            double oldrate = (Double) result.getEntityList().get(0);
//            double newrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
            result = accCurrencyobj.getCurrencyToBaseAmount(requestParams, 1.0, currid, authHandler.getDateOnlyFormat(request).parse(request.getParameter("creationdate")), externalExchangeRate);
            double newrate = (Double) result.getEntityList().get(0);

            ratio = oldrate - newrate;
            Double recinvamount = jobj.getDouble("payment");
            amount += recinvamount * ratio;
        }
//        amount = CompanyHandler.getBaseToCurrencyAmount(session, request, amount, currencyid, AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
        result = accCurrencyobj.getBaseToCurrencyAmount(requestParams, amount, currencyid, authHandler.getDateOnlyFormat(request).parse(request.getParameter("creationdate")), externalExchangeRate);
        amount = (Double) result.getEntityList().get(0);
        return (amount);
    }

//    public ModelAndView getBillingReceipts(HttpServletRequest request, HttpServletResponse response) {
//        JSONObject jobj=new JSONObject();
//        boolean issuccess = false;
//        String msg = "";
//		try {
//            HashMap<String, Object> requestParams = getReceiptRequestMap(request);
//            KwlReturnObject result = accReceiptDAOobj.getBillingReceipts(requestParams);
//            jobj = getBillingReceiptJson(request, result.getEntityList());
//            jobj.put("count", result.getRecordTotalCount());
//            issuccess = true;
//        } catch (Exception ex) {
//            msg = ""+ex.getMessage();
//            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            try{
//                jobj.put("success", issuccess);
//                jobj.put("msg", msg);
//            } catch (JSONException ex) {
//                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        return new ModelAndView("jsonView", "model", jobj.toString());
//    }
    public List<JSONObject> getBillingReceiptJson(HttpServletRequest request, List list, List<JSONObject> jsonObjectlist) throws SessionExpiredException, ServiceException, UnsupportedEncodingException {
        //JSONObject jobj=new JSONObject();
        //JSONArray jArr=new JSONArray();
        try {
            HashMap<String, Object> rRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                BillingReceipt receipt = (BillingReceipt) row[0];
                Account acc = (Account) row[1];
                JSONObject obj = new JSONObject();
                JSONArray jArr1 = new JSONArray();
                obj.put("withoutinventory", true);
                obj.put("billid", receipt.getID());
                obj.put("companyid", receipt.getCompany().getCompanyID());
                obj.put("companyname", receipt.getCompany().getCompanyName());
                obj.put("entryno", receipt.getJournalEntry().getEntryNumber());
                obj.put("journalentryid", receipt.getJournalEntry().getID());
                KwlReturnObject customerresult = accountingHandlerDAOobj.getObject(Customer.class.getName(), acc.getID());
                Customer customer = (Customer) customerresult.getEntityList().get(0);
                obj.put("personemail", customer != null ? customer.getEmail() : "");
                obj.put("personid", acc.getID());
                obj.put("billno", receipt.getBillingReceiptNumber());
                obj.put("ismanydbcr", receipt.isIsmanydbcr());
                obj.put("bankCharges", receipt.getBankChargesAmount());
                obj.put("bankChargesCmb", receipt.getBankChargesAccount() != null ? receipt.getBankChargesAccount().getID() : "");
                obj.put("bankInterest", receipt.getBankInterestAmount());
                obj.put("bankInterestCmb", receipt.getBankInterestAccount() != null ? receipt.getBankInterestAccount().getID() : "");
                obj.put("paidToCmb", receipt.getReceivedFrom() == null ? "" : receipt.getReceivedFrom().getID());
                obj.put("billdate", authHandler.getDateOnlyFormat(request).format(receipt.getJournalEntry().getEntryDate()));//receiptdate
                obj.put("currencysymbol", (receipt.getCurrency() == null ? currency.getCurrencyID() : receipt.getCurrency().getSymbol()));
                obj.put("externalcurrencyrate", receipt.getExternalCurrencyRate());
                Iterator itrRow = receipt.getRows().iterator();
                rRequestParams.clear();
                filter_names.clear();
                filter_params.clear();
                filter_names.add("billingReceipt.ID");
                filter_params.add(receipt.getID());
                rRequestParams.put("filter_names", filter_names);
                rRequestParams.put("filter_params", filter_params);
                KwlReturnObject pdoresult = accReceiptDAOobj.getBillingReceiptDetailOtherwise(rRequestParams);
                List<BillingReceiptDetailOtherwise> list1 = pdoresult.getEntityList();
                Iterator pdoRow = list1.iterator();

                double amount = 0;
                if (!receipt.getRows().isEmpty()) {
                    while (itrRow.hasNext()) {
                        amount += ((BillingReceiptDetail) itrRow.next()).getAmount();
                    }
                    obj.put("otherwise", false);
                } else if (pdoRow != null && list1.size() > 0) {
                    for (BillingReceiptDetailOtherwise receiptDetailOtherwise : list1) {
                        if (receipt.getID().equals(receiptDetailOtherwise.getBillingReceipt().getID())) {
                            JSONObject obj1 = new JSONObject();
                            obj1.put("debitaccid", receiptDetailOtherwise.getAccount().getID());
                            obj1.put("debitamt", receiptDetailOtherwise.getAmount());
                            obj1.put("isdebit", receiptDetailOtherwise.isIsdebit());
                            obj1.put("desc", receiptDetailOtherwise.getDescription() != null ? receiptDetailOtherwise.getDescription() : "");
                            obj1.put("prtaxid", receiptDetailOtherwise.getTax() != null ? receiptDetailOtherwise.getTax().getID() : "");
                            obj1.put("curamount", (receiptDetailOtherwise.getAmount() + receiptDetailOtherwise.getTaxamount()));
                            if (receipt.isIsmanydbcr()) {
                                if (receiptDetailOtherwise.isIsdebit()) {
                                    amount -= (receiptDetailOtherwise.getAmount() + receiptDetailOtherwise.getTaxamount());
                                } else {
                                    amount += (receiptDetailOtherwise.getAmount() + receiptDetailOtherwise.getTaxamount());
                                }
                            } else {
                                amount += (receiptDetailOtherwise.getAmount() + receiptDetailOtherwise.getTaxamount());
                            }
                            jArr1.put(obj1);
                        }
                    }

                    obj.put("otherwise", true);
                    obj.put("detailsjarr", jArr1);
                } else {
//                    itrRow=receipt.getJournalEntry().getDetails().iterator();
//                    amount+=((JournalEntryDetail)itrRow.next()).getAmount();
//                    obj.put("otherwise",true);                   
                    itrRow = receipt.getJournalEntry().getDetails().iterator();
                    while (itrRow.hasNext()) {
                        JournalEntryDetail jed = ((JournalEntryDetail) itrRow.next());
                        if (jed.isDebit()) {
                            if (receipt.getDeposittoJEDetail() != null) {
                                amount = receipt.getDeposittoJEDetail().getAmount();
                            } else {
                                amount = jed.getAmount();
                            }
                        } else {
                            JSONObject obj1 = new JSONObject();
                            obj1.put("debitaccid", jed.getAccount().getID());
                            obj1.put("isdebit", jed.isDebit());
                            obj1.put("debitamt", jed.getAmount());
                            obj1.put("curamount", jed.getAmount());
                            obj1.put("desc", jed.getDescription() != null ? jed.getDescription() : "");
                            jArr1.put(obj1);
                        }
                    }
                    obj.put("otherwise", true);
                    obj.put("detailsjarr", jArr1);
                }

                KwlReturnObject result = accReceiptDAOobj.getBillingReceiptCustomerNames(receipt.getCompany().getCompanyID(), receipt.getID());
                List cNameList = result.getEntityList();
                Iterator cNamesItr = cNameList.iterator();
                String customerNames = "";
                while (cNamesItr.hasNext()) {
                    String tempName = URLEncoder.encode((String) cNamesItr.next(), "UTF-8");
                    customerNames += tempName;
                    customerNames += ",";
                }
                customerNames = customerNames.substring(0, Math.max(0, customerNames.length() - 1));
                obj.put("amount", authHandler.formattedAmount(amount, companyid));
                obj.put("personname", customerNames);
                obj.put("receipttype", "");
                obj.put("memo", receipt.getMemo());
                obj.put("deleted", receipt.isDeleted());
                obj.put("paymentmethod", (receipt.getPayDetail() == null ? "" : receipt.getPayDetail().getPaymentMethod().getMethodName()));
                obj.put("amount", amount);
                obj.put("currencysymbol", (receipt.getCurrency() == null ? currency.getSymbol() : receipt.getCurrency().getSymbol()));
                obj.put("currencyid", (receipt.getCurrency() == null ? currency.getCurrencyID() : receipt.getCurrency().getCurrencyID()));
                obj.put("methodid", (receipt.getPayDetail() == null ? "" : receipt.getPayDetail().getPaymentMethod().getID()));
                obj.put("detailtype", (receipt.getPayDetail() == null ? "" : receipt.getPayDetail().getPaymentMethod().getDetailType()));
                if (receipt.getPayDetail() != null) {
                    try {
                        obj.put("expirydate", (receipt.getPayDetail().getCard() == null ? "" : authHandler.getDateOnlyFormat(request).format(receipt.getPayDetail().getCard().getExpiryDate())));
                        obj.put("refcardno", (receipt.getPayDetail().getCard() == null ? "" : (receipt.getPayDetail().getCard().getCardNo() == null ? "" : receipt.getPayDetail().getCard().getCardNo())));
                    } catch (IllegalArgumentException ae) {
                        obj.put("expirydate", "");
                    }
                    obj.put("refdetail", (receipt.getPayDetail().getCard() == null ? (receipt.getPayDetail().getCheque() == null ? "" : receipt.getPayDetail().getCheque().getDescription()) : receipt.getPayDetail().getCard().getCardType()));
                    obj.put("refno", (receipt.getPayDetail().getCard() == null ? (receipt.getPayDetail().getCheque() == null ? "" : receipt.getPayDetail().getCheque().getChequeNo()) : receipt.getPayDetail().getCard().getRefNo()));
                    obj.put("refname", (receipt.getPayDetail().getCard() == null ? (receipt.getPayDetail().getCheque() == null ? "" : (receipt.getPayDetail().getCheque().getBankMasterItem() == null ? receipt.getPayDetail().getCheque().getBankName() : receipt.getPayDetail().getCheque().getBankMasterItem().getID())) : receipt.getPayDetail().getCard().getCardHolder()));
                }
                obj.put("clearanceDate", "");
                obj.put("paymentStatus", false);
                if (receipt.getPayDetail() != null) {
                    KwlReturnObject clearanceDate = accBankReconciliationObj.getBRfromJE(receipt.getJournalEntry().getID(), receipt.getCompany().getCompanyID(), false);
                    if (clearanceDate != null && clearanceDate.getEntityList() != null && clearanceDate.getEntityList().size() > 0) {
                        BankReconciliationDetail brd = (BankReconciliationDetail) clearanceDate.getEntityList().get(0);
                        if (brd.getBankReconciliation().getClearanceDate() != null) {
                            obj.put("clearanceDate", authHandler.getDateOnlyFormat(request).format(brd.getBankReconciliation().getClearanceDate()));
                            obj.put("paymentStatus", true);
                        }
                    }
                }
                jsonObjectlist.add(obj);
            }
            // jobj.put("data", jArr);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getBillingReceiptJson : " + ex.getMessage(), ex);
        }
        return jsonObjectlist;
    }

    public ModelAndView getBillingReceiptRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            jobj = getBillingReceiptRowsJSON(request);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    private JSONObject getBillingReceiptRowsJSON(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        try {
//                KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            boolean isReceiptEdit = Boolean.parseBoolean(request.getParameter("isReceiptEdit"));
            String[] billingreceipt = request.getParameterValues("bills");
            int i = 0;
            double taxPercent = 0;
            HashMap<String, Object> rRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("billingReceipt.ID");
            order_by.add("srno");
            order_type.add("asc");
            rRequestParams.put("filter_names", filter_names);
            rRequestParams.put("filter_params", filter_params);
            rRequestParams.put("order_by", order_by);
            rRequestParams.put("order_type", order_type);

            JSONArray jArr = new JSONArray();
            while (billingreceipt != null && i < billingreceipt.length) {
//                    BillingReceipt re=(BillingReceipt)session.get(BillingReceipt.class, billingreceipt[i]);
                KwlReturnObject result = accountingHandlerDAOobj.getObject(BillingReceipt.class.getName(), billingreceipt[i]);
                BillingReceipt re = (BillingReceipt) result.getEntityList().get(0);
//                Iterator itr=re.getRows().iterator();
                filter_params.clear();
                filter_params.add(re.getID());
                KwlReturnObject grdresult = accReceiptDAOobj.getBillingReceiptDetails(rRequestParams);
                Iterator itr = grdresult.getEntityList().iterator();

                while (itr.hasNext()) {
                    BillingReceiptDetail row = (BillingReceiptDetail) itr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", isReceiptEdit ? row.getBillingInvoice().getID() : re.getID());
                    obj.put("srno", row.getSrno());
                    obj.put("rowid", row.getID());
                    obj.put("currencysymbol", (row.getBillingReceipt().getCurrency() == null ? currency.getCurrencyID() : row.getBillingReceipt().getCurrency().getSymbol()));
                    obj.put("transectionno", row.getBillingInvoice().getBillingInvoiceNumber());
                    obj.put("transectionid", row.getBillingInvoice().getID());
                    obj.put("amount", (isReceiptEdit ? row.getBillingInvoice().getCustomerEntry().getAmount() : row.getAmount()));
                    obj.put("amountpaid", row.getAmount());
                    obj.put("duedate", authHandler.getDateOnlyFormat(request).format(row.getBillingInvoice().getDueDate()));
                    obj.put("creationdate", authHandler.getDateOnlyFormat(request).format(row.getBillingInvoice().getJournalEntry().getEntryDate()));
                    double totalamount = row.getBillingInvoice().getCustomerEntry().getAmount();
                    obj.put("totalamount", totalamount);

                    KwlReturnObject amtrs = accReceiptDAOobj.getBillingReceiptAmountFromInvoice(row.getBillingInvoice().getID());
                    double ramount = amtrs.getEntityList().size() > 0 ? (Double) amtrs.getEntityList().get(0) : 0;
                    double amountdue = totalamount - ramount;
                    obj.put("amountduenonnegative", (isReceiptEdit ? amountdue + row.getAmount() : amountdue));
                    if (row.getBillingInvoice().getTax() != null) {
                        KwlReturnObject perresult = accTaxObj.getTaxPercent(row.getCompany().getCompanyID(), row.getBillingInvoice().getJournalEntry().getEntryDate(), row.getBillingInvoice().getTax().getID());
                        taxPercent = (Double) perresult.getEntityList().get(0);
                    }
                    obj.put("taxpercent", taxPercent);
                    obj.put("discount", row.getBillingInvoice().getDiscount() == null ? 0 : row.getBillingInvoice().getDiscount().getDiscountValue());
                    obj.put("payment", row.getBillingInvoice().getID());
                    obj.put("totalamount", row.getBillingInvoice().getCustomerEntry().getAmount());
                    jArr.put(obj);
                }
                i++;
                jobj.put("data", jArr);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getBillingReceiptRowsJSON : " + ex.getMessage(), ex);
        }
        return jobj;
    }

//    public ModelAndView deleteBillingReceipt(HttpServletRequest request, HttpServletResponse response) {
//        JSONObject jobj = new JSONObject();
//        boolean issuccess = false;
//        String msg = "";
//        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
//        def.setName("BR_Tx");
//        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
//        TransactionStatus status = txnManager.getTransaction(def);
//        try{
//            String receiptsJson = request.getParameter("data");
//            String companyid = sessionHandlerImpl.getCompanyid(request);
//            int no = deleteBillingReceipt(receiptsJson, companyid);
//            txnManager.commit(status);
//            issuccess = true;
//            msg = messageSource.getMessage("acc.receipt.billdel", null, RequestContextUtils.getLocale(request));   //"Billing Receipt(s) has been deleted successfully";
//        } catch (SessionExpiredException ex) {
//            txnManager.rollback(status);
//            msg = ex.getMessage();
//            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (Exception ex) {
//            txnManager.rollback(status);
//            msg = ""+ex.getMessage();
//        } finally {
//            try {
//                jobj.put("success", issuccess);
//                jobj.put("msg", msg);
//            } catch (JSONException ex) {
//                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        return new ModelAndView("jsonView", "model", jobj.toString());
//    }
//
//    public int deleteBillingReceipt(String receiptsJson, String companyid) throws AccountingException, ServiceException {
//        String msg = "";
//        int numRows = 0;
//        try{
//            JSONArray jArr = new JSONArray(receiptsJson);
//
//            String receiptid;
//            KwlReturnObject result;
//            for (int i = 0; i < jArr.length(); i++) {
//                JSONObject jobj = jArr.getJSONObject(i);
//                receiptid = StringUtil.DecodeText(jobj.optString("billid"));
//                String jeid1 = StringUtil.DecodeText(jobj.optString("journalentryid"));
//                //Delete Billing receipt details
////                result = accReceiptDAOobj.deleteBillingReceiptDetails(receiptid, companyid);
//                //Delete Billing Receipt
//                result = accReceiptDAOobj.deleteBillingReceiptEntry(receiptid, companyid);
//
//                HashMap<String,Object> requestParams = new HashMap<String, Object>();
//                requestParams.put("oldjeid", jeid1);
//                requestParams.put("companyId", companyid);
//                deleteBankReconcilation(requestParams);
//
////                jeid = StringUtil.DecodeText(jobj.optString("journalentryid"));
//                //Delete Journal Entry and Details
////                result = accJournalEntryobj.deleteJEDtails(jeid, companyid);
//                //Delete Journal Entry Details
//
//                result = accReceiptDAOobj.getJEFromBR(receiptid, companyid);
//                      List list = result.getEntityList();
//                      Iterator itr = list.iterator();
//                      while(itr.hasNext()) {
//                          String jeid = (String) itr.next();
//                          result = accJournalEntryobj.deleteJournalEntry(jeid, companyid);
//                      }
//
////              query = "update JournalEntry je set je.deleted=true where je.ID in(select p.journalEntry.ID from BillingReceipt p where p.ID in("+qMarks +") and p.company.companyID=je.company.companyID) and je.company.companyID=?";
//
//                numRows++;
//            }
////            issuccess = true;
////            msg = "Billing Receipt(s) has been deleted successfully";
//        } catch (UnsupportedEncodingException ex) {
//            throw ServiceException.FAILURE("Can't extract the records. <br>Encoding not supported", ex);
//        } catch (ServiceException ex) {
//            msg = "Selected record(s) is currently used in the transaction(s).";
//            throw new AccountingException(msg);
//           // Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (JSONException ex) {
//            msg = ex.getMessage();
//            throw new AccountingException(msg);
//           // Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return numRows;
//
//    }
    //    public ModelAndView exportBillingReceipt(HttpServletRequest request, HttpServletResponse response) {
//        JSONObject jobj = new JSONObject();
//        String view = "jsonView_ex";
//        try{
//            HashMap<String, Object> requestParams = getReceiptRequestMap(request);
//            KwlReturnObject result = accReceiptDAOobj.getBillingReceipts(requestParams);
//            jobj = getBillingReceiptJson(request, result.getEntityList());
//            String fileType = request.getParameter("filetype");
//            if (StringUtil.equal(fileType, "print")) {
//                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
//                jobj.put("GenerateDate", GenerateDate);
//                view = "jsonView-empty";
//            }
//            exportDaoObj.processRequest(request, response, jobj);
//        } catch (SessionExpiredException ex) {
//            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
//        } catch(Exception ex) {
//            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return new ModelAndView(view, "model", jobj.toString());
//    }
    private class ReceiptDateComparator implements Comparator<JSONObject> {

        @Override
        public int compare(JSONObject o1, JSONObject o2) {  //sort data on date
            int result = 0;
            try {
                Date date1 = new Date(o1.getString("billdate"));
                Date date2 = new Date(o2.getString("billdate"));

                if (date1.getTime() > date2.getTime()) {
                    result = 1;
                } else if (date1.getTime() < date2.getTime()) {
                    result = -1;
                } else {
                    result = 0;
                }
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
            return result;
        }
    }

    public ModelAndView linkReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            List li = linkReceiptNew(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.field.PaymentinformationhasbeenLinkedtoCustomerInvoicesuccessfully", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public List linkReceipt(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        List result = new ArrayList();
        GoodsReceipt greceipt = null;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String paymentid =request.getParameter("paymentid");
            String invoiceids[] = request.getParameter("invoiceids").split(",");
            String paymentno =request.getParameter("paymentno");
            String invoicenos =request.getParameter("invoicenos");
            double amount = 0;

            String amounts[] = request.getParameter("amounts").split(",");    
//            KwlReturnObject grresult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceid);
//            Invoice gr = (Invoice) grresult.getEntityList().get(0);

            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

            KwlReturnObject receiptObj = accountingHandlerDAOobj.getObject(Receipt.class.getName(), paymentid);
            Receipt payment = (Receipt) receiptObj.getEntityList().get(0);

            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            JournalEntry je = null;
            String jeid = "";
            if (payment.isNormalReceipt()) {
                je = payment.getJournalEntry();
                jeid = je.getID();
            }
            
            JournalEntryDetail updatejed = new JournalEntryDetail();
            boolean isopeningBalanceRecceipt = payment.isIsOpeningBalenceReceipt();
            double eternalCurrencyRate = 0d;
            if (!payment.isNormalReceipt() && payment.isIsOpeningBalenceReceipt()) {
                eternalCurrencyRate = payment.getExchangeRateForOpeningTransaction();
            }
            //Commented the code becoz now only advance payment can be linked and in that we dont need to delete the je and create it again
            //Delete entry from optimized table
//            if (payment.isNormalReceipt()) {
//                accJournalEntryobj.deleteOnEditAccountJEs_optimized(jeid);
//                eternalCurrencyRate = je.getExternalCurrencyRate();
//
//                Iterator itrRow = je.getDetails().iterator();
//
//                while (itrRow.hasNext()) {
//                    JournalEntryDetail jed = ((JournalEntryDetail) itrRow.next());
////                if(payment.isIsmanydbcr()) {
////                    if (jed.isDebit()) {
////                        amount -= jed.getAmount();
////                        updatejed = jed;
////                    } else {
////                        amount += jed.getAmount();
////                        accJournalEntryobj.deleteJournalEntryDetailRow(jed.getID(), companyid);
////                    }
////                } else {
//                    if (jed.isDebit()) {
//                        amount = jed.getAmount();
//                        updatejed = jed;
//                    } else {
//                        accJournalEntryobj.deleteJournalEntryDetailRow(jed.getID(), companyid);
//                    }
////                }
//                }
//            }
            JSONArray jArr = new JSONArray();
            String customerId="";
            String accountId="";
            String linkedInvoiceids="";
            String linkedInvoicenos="";
            Iterator itrRow = payment.getRows().iterator();
           Map<String,Double> paymentRowsHashMap=new HashMap<String, Double>();
            double receiptAmount=0.0;
            if (!payment.getRows().isEmpty()) {//deleting linked data if the Payment is parctially linked
                    while (itrRow.hasNext()) {
                        ReceiptDetail receiptDetail=((ReceiptDetail) itrRow.next());
                        paymentRowsHashMap.put(receiptDetail.getInvoice().getID(), receiptDetail.getAmount());
                        receiptAmount+=receiptDetail.getAmount();
                    }
                accReceiptDAOobj.deleteReceiptDetailsAndUpdateAmountDue(payment.getID(),payment.getCompany().getCompanyID());
                HashMap<String, Object> requestParams = new HashMap();
                requestParams.put(Constants.companyid, company.getCompanyID());
                requestParams.put("gcurrencyid", company.getCurrency().getCurrencyID());
                double externalCurrencyRate = 1d;
                boolean isopeningBalanceRP = payment.isIsOpeningBalenceReceipt();
                Date rpCreationDate = null;
                rpCreationDate = payment.getCreationDate();
                if (!payment.isNormalReceipt() && payment.isIsOpeningBalenceReceipt()) {
                    externalCurrencyRate = payment.getExchangeRateForOpeningTransaction();
                } else {
//                    rpCreationDate = payment.getJournalEntry().getEntryDate();
                    externalCurrencyRate = payment.getJournalEntry().getExternalCurrencyRate();
                }
                String fromcurrencyid = payment.getCurrency().getCurrencyID();
                KwlReturnObject bAmt = null;
                if (isopeningBalanceRP && payment.isConversionRateFromCurrencyToBase()) {// if Receipt is opening balance Receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, receiptAmount, fromcurrencyid, rpCreationDate, externalCurrencyRate);
                } else {
                    bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, receiptAmount, fromcurrencyid, rpCreationDate, externalCurrencyRate);
                }
                double openingbalanceBaseAmountDue = (Double) bAmt.getEntityList().get(0);
            
                payment.setOpeningBalanceAmountDue(payment.getOpeningBalanceAmountDue()+receiptAmount);
                payment.setOpeningBalanceBaseAmountDue(payment.getOpeningBalanceBaseAmountDue()+openingbalanceBaseAmountDue);
           }
            for (int k = 0; k < invoiceids.length; k++) {//creating a hash map with payment and their linked invoice
                if (StringUtil.isNullOrEmpty(invoiceids[k])) {
                    continue;
                }
                double usedcnamount = 0d;
                if (!StringUtil.isNullOrEmpty(amounts[k])) {
                    usedcnamount = Double.parseDouble((String) amounts[k]);
                } else {
                    usedcnamount = 0;
                }
                if (usedcnamount == 0) {
                    continue;
                }
                KwlReturnObject invoiceresult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceids[k]);
                Invoice invoice = (Invoice) invoiceresult.getEntityList().get(0);
                if (linkedInvoiceids.equals("") && invoice != null) {
                    linkedInvoiceids += invoice.getID();
                    linkedInvoicenos += invoice.getInvoiceNumber();
                } else if (invoice != null) {
                    linkedInvoiceids += "," + invoice.getID();
                    linkedInvoicenos += "," + invoice.getInvoiceNumber();
                }
                customerId = invoice.getCustomer().getID();
                accountId = invoice.getCustomer().getAccount().getID();

                    JSONObject jobj = new JSONObject();

                if (paymentRowsHashMap.containsKey(invoiceids[k])) {
                    double actualAmount = paymentRowsHashMap.get(invoiceids[k]);
                    jobj.put("payment", Double.parseDouble(amounts[k]) + actualAmount);
                    paymentRowsHashMap.remove(invoiceids[k]);
                } else {
                    jobj.put("payment", amounts[k]);
                }

                jobj.put("billid", invoiceids[k]);
                jobj.put("isopeningBalanceRecceipt", isopeningBalanceRecceipt);
                    jobj.put("isConversionRateFromCurrencyToBase", payment.isConversionRateFromCurrencyToBase());
                    jArr.put(jobj);
                }

            for (Entry<String, Double> entry : paymentRowsHashMap.entrySet()) {//creating json for saving linked data
                JSONObject jobj = new JSONObject();
                String key = entry.getKey();
                Double value = entry.getValue();
                jobj.put("payment", value);
                jobj.put("billid", key);
                jobj.put("isopeningBalancePayment", isopeningBalanceRecceipt);
                jobj.put("isConversionRateFromCurrencyToBase", payment.isConversionRateFromCurrencyToBase());
                jArr.put(jobj);
            }
            HashMap<String, Object> paymenthm = new HashMap<String, Object>();
            HashSet payDetails = saveReceiptRows(payment, company, jArr, greceipt);
            paymenthm.put("receiptid", payment.getID());
            paymenthm.put("receiptdetails", payDetails);
            paymenthm.put("customerId", customerId);
            accReceiptDAOobj.saveReceipt(paymenthm);

//Commented the code becoz now only advance payment can be linked and in that we dont need to delete the je and create it again
//            JournalEntryDetail updatejed1 = null;
//            if (payment.isNormalReceipt()) {
//                JSONObject jedjson = new JSONObject();
//                jedjson.put("companyid", companyid);
//                jedjson.put("srno", 2);
//                jedjson.put("amount", amount);
//                jedjson.put("accountid", accountId);
//                jedjson.put("debit", false);
//                jedjson.put("jeid", jeid);
//                KwlReturnObject kjed = accJournalEntryobj.addJournalEntryDetails(jedjson);
//                updatejed1 = (JournalEntryDetail) kjed.getEntityList().get(0);
//            }
//
//            if (jArr.length() > 0) {
//                boolean rateDecreased = false;
//                amount = 0;
//                JSONObject jobj = new JSONObject();
//                for (int i = 0; i < jArr.length(); i++) {
//                    jobj = jArr.getJSONObject(i);
//                    amount += jobj.getDouble("payment");
//                }
//                double amountDiff = oldReceiptRowsAmount(request, jArr, payment.getCurrency().getCurrencyID(), eternalCurrencyRate);
//                if (preferences.getForeignexchange() == null) {
//                    throw new AccountingException(messageSource.getMessage("acc.pay.forex", null, RequestContextUtils.getLocale(request)));
//                }
//                if (amountDiff != 0 && preferences.getForeignexchange() != null && payment.isNormalReceipt()) {
//                    if (amountDiff < 0) {
//                        rateDecreased = true;
//                    }
//                    JSONObject jParam = new JSONObject();
//                    jParam.put("srno", 3);
//                    jParam.put("companyid", companyid);
//                    jParam.put("amount", rateDecreased ? (-1 * amountDiff) : amountDiff);
//                    jParam.put("accountid", preferences.getForeignexchange().getID());
//                    jParam.put("debit", rateDecreased ? false : true);
//                    jParam.put("jeid", jeid);
//                    accJournalEntryobj.addJournalEntryDetails(jParam);
//
//                    jParam = new JSONObject();
//                    jParam.put("jedid", updatejed1.getID());
//                    jParam.put("amount", rateDecreased ? (updatejed.getAmount() + amountDiff) : updatejed1.getAmount() + amountDiff);
//                    accJournalEntryobj.updateJournalEntryDetails(jParam);
//                }
//            }
//
//            //Insert new entries again in optimized table.
//            if (payment.isNormalReceipt()) {
//                accJournalEntryobj.saveAccountJEs_optimized(jeid);
//            }
           auditTrailObj.insertAuditLog(AuditAction.LINKEDRECEIPT, "User " + sessionHandlerImpl.getUserFullName(request) + " has linked Receipt"+paymentno+" to "+linkedInvoicenos, request,linkedInvoiceids); 
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    public List linkReceiptNew(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        List result = new ArrayList();
        GoodsReceipt greceipt = null;
        try {
            JSONObject requestObj = convertRequestToJSONObjectForLinkPayment(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String paymentid =request.getParameter("paymentid");
            double amount = 0;
            String amounts[] = request.getParameter("amounts").split(",");    
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

            KwlReturnObject receiptObj = accountingHandlerDAOobj.getObject(Receipt.class.getName(), paymentid);
            Receipt payment = (Receipt) receiptObj.getEntityList().get(0);
            String paymentno =payment.getReceiptNumber();

//            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
//            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            JournalEntry je = null;
            String jeid = "";
            String JENumber="";
             boolean isopeningBalancePayment = payment.isIsOpeningBalenceReceipt();
            if (payment.isNormalReceipt()) {
                je = payment.getJournalEntry();
                jeid = je.getID();
            }
            
            JournalEntryDetail updatejed = new JournalEntryDetail();
            boolean isopeningBalanceRecceipt = payment.isIsOpeningBalenceReceipt();
            double eternalCurrencyRate = 0d;
            if (!payment.isNormalReceipt() && payment.isIsOpeningBalenceReceipt()) {
                eternalCurrencyRate = payment.getExchangeRateForOpeningTransaction();
            }
            JSONArray jArr = new JSONArray();
            String customerId="";
            String accountId="";
            String linkedInvoiceids="";
            String linkedInvoicenos="";
            Map<String,Object> counterMap=new HashMap<>();
            counterMap.put("counter", 0);
           JSONArray linkJSONArray=request.getParameter("linkdetails")!=null?new JSONArray(request.getParameter("linkdetails")):new JSONArray();
            for (int k = 0; k < linkJSONArray.length(); k++) {//creating a hash map with payment and their linked invoice
                JSONObject jSONObject=linkJSONArray.getJSONObject(k);
                if (jSONObject.optDouble("invamount",0)!=0) {
                    String invoiceId=jSONObject.optString("billid","");
                    double invAmount=jSONObject.optDouble("invamount",0);
                    double exchangeratefortransaction=jSONObject.optDouble("exchangeratefortransaction",1);
                    KwlReturnObject grresult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceId);
                    Invoice gr = (Invoice) grresult.getEntityList().get(0);
                    if(linkedInvoiceids.equals("")&&gr!=null){
                        linkedInvoiceids+=gr.getID();
                        linkedInvoicenos+=gr.getInvoiceNumber();
                    }else if(gr!=null){
                        linkedInvoiceids+=","+gr.getID();
                        linkedInvoicenos+=","+gr.getInvoiceNumber();
                    }
                    customerId=gr.getCustomer().getID();
                    accountId=gr.getCustomer().getAccount().getID();
                    JSONObject jobj = new JSONObject();
                    jobj.put("enteramount", invAmount);
                    jobj.put("documentid", invoiceId);
                    jobj.put("isopeningBalancePayment", isopeningBalancePayment);
                    jobj.put("isConversionRateFromCurrencyToBase", payment.isConversionRateFromCurrencyToBase());
                    jobj.put("exchangeratefortransaction", exchangeratefortransaction);
                    jArr.put(jobj);
                }
            }
            HashMap<String, Object> paymenthm = new HashMap<String, Object>();
            HashMap<String, Object> linkDetails = saveLinkedReceiptDetails(requestObj, payment, company, jArr, greceipt,Constants.AdvancePayment,counterMap);
            paymenthm.put("receiptid", payment.getID());
            paymenthm.put("linkDetails", linkDetails.get("linkDetailPayment"));
            JENumber= linkDetails.containsKey("JournalEntries")&& !StringUtil.isNullOrEmpty((String)linkDetails.get("JournalEntries"))?(String)linkDetails.get("JournalEntries"):"";
            paymenthm.put("customerId", customerId);
            accReceiptDAOobj.saveReceipt(paymenthm);
            auditTrailObj.insertAuditLog(AuditAction.LINKEDRECEIPT, "User " + sessionHandlerImpl.getUserFullName(request) + " has linked Receipt "+paymentno+" to "+linkedInvoicenos, request,linkedInvoiceids); 
            result.add(JENumber);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return result;
    }

    public ModelAndView linkBillingReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            List li = linkBillingReceipt(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.field.PaymentinformationhasbeenLinkedtoCustomerInvoicesuccessfully", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public List linkBillingReceipt(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        List result = new ArrayList();
        BillingGoodsReceipt bgreceipt = null;//Set only for contra entry
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String paymentid = request.getParameter("paymentid");
            String invoiceid = request.getParameter("invoiceid");
            double amount = 0;

            KwlReturnObject grresult = accountingHandlerDAOobj.getObject(BillingInvoice.class.getName(), invoiceid);
            BillingInvoice gr = (BillingInvoice) grresult.getEntityList().get(0);

            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

            KwlReturnObject receiptObj = accountingHandlerDAOobj.getObject(BillingReceipt.class.getName(), paymentid);
            BillingReceipt payment = (BillingReceipt) receiptObj.getEntityList().get(0);


            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            JournalEntry je = payment.getJournalEntry();
            String jeid = je.getID();

            //Delete entry from optimized table
            accJournalEntryobj.deleteOnEditAccountJEs_optimized(jeid);

            Iterator itrRow = je.getDetails().iterator();
            JournalEntryDetail updatejed = new JournalEntryDetail();
            while (itrRow.hasNext()) {
                JournalEntryDetail jed = ((JournalEntryDetail) itrRow.next());
//                if(payment.isIsmanydbcr()) {
//                    if (jed.isDebit()) {
//                        amount -= jed.getAmount();
//                        updatejed = jed;
//                    } else {
//                        amount += jed.getAmount();
//                        accJournalEntryobj.deleteJournalEntryDetailRow(jed.getID(), companyid);
//                    }
//                } else {
                if (jed.isDebit()) {
                    amount = jed.getAmount();
                    updatejed = jed;
                } else {
                    accJournalEntryobj.deleteJournalEntryDetailRow(jed.getID(), companyid);
                }
            }
            JSONArray jArr = new JSONArray();
            JSONObject jobj = new JSONObject();
            jobj.put("payment", amount);
            jobj.put("billid", invoiceid);
            jArr.put(jobj);

            HashMap<String, Object> paymenthm = new HashMap<String, Object>();
            HashSet payDetails = saveBillingReceiptRows(payment, company, jArr, bgreceipt);
            paymenthm.put("billingreceiptid", payment.getID());
            paymenthm.put("receiptdetails", payDetails);
            accReceiptDAOobj.saveBillingReceipt(paymenthm);

            JSONObject jedjson = new JSONObject();
            jedjson.put("companyid", companyid);
            jedjson.put("srno", 2);
            jedjson.put("amount", amount);
            jedjson.put("accountid", gr.getCustomer().getID());
            jedjson.put("debit", false);
            jedjson.put("jeid", jeid);
            KwlReturnObject kjed = accJournalEntryobj.addJournalEntryDetails(jedjson);
            JournalEntryDetail updatejed1 = (JournalEntryDetail) kjed.getEntityList().get(0);

            if (jArr.length() > 0) {
                boolean rateDecreased = false;
                amount = 0;
                for (int i = 0; i < jArr.length(); i++) {
                    jobj = jArr.getJSONObject(i);
                    amount += jobj.getDouble("payment");
                }
                double amountDiff = oldBillingReceiptRowsAmount(request, jArr, payment.getCurrency().getCurrencyID(), je.getExternalCurrencyRate());
                if (preferences.getForeignexchange() == null) {
                    throw new AccountingException(messageSource.getMessage("acc.pay.forex", null, RequestContextUtils.getLocale(request)));
                }
                if (amountDiff != 0 && preferences.getForeignexchange() != null) {
                    if (amountDiff < 0) {
                        rateDecreased = true;
                    }
                    //amountDiff = amount / amountDiff;
                    JSONObject jParam = new JSONObject();
                    jParam.put("srno", 3);
                    jParam.put("companyid", companyid);
                    jParam.put("amount", rateDecreased ? (-1 * amountDiff) : amountDiff);
                    jParam.put("accountid", preferences.getForeignexchange().getID());
                    jParam.put("debit", rateDecreased ? false : true);
                    jParam.put("jeid", jeid);
                    accJournalEntryobj.addJournalEntryDetails(jParam);
                    //jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    //jedetails.add(jed);

                    jParam = new JSONObject();
                    jParam.put("jedid", updatejed1.getID());
                    jParam.put("amount", rateDecreased ? (updatejed.getAmount() + amountDiff) : (updatejed1.getAmount() + amountDiff));
                    accJournalEntryobj.updateJournalEntryDetails(jParam);

                }
            }

            //Insert new entries again in optimized table.
            accJournalEntryobj.saveAccountJEs_optimized(jeid);

        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ModelAndView deleteReceiptMerged(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = accReceivePaymentModuleServiceObj.deleteReceiptMerged(paramJobj);

        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {

            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /**
     * Added @Transactional instead of txnmanager - ERP-32983.No any changes
     * other than this has been done in code.
     * @param request
     * @param response
     * @return
     */
    public ModelAndView deleteReceiptForEdit(HttpServletRequest request, HttpServletResponse response) throws JSONException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false; 
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj=accReceivePaymentModuleServiceObj.deleteReceiptForEdit(paramJobj);
            issuccess = jobj.optBoolean(Constants.RES_success, issuccess);
            msg = jobj.optString(Constants.RES_msg);
        } catch (AccountingException ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try{
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch(JSONException ex){
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    //function for deletion of recive payment receipt
    public ModelAndView deleteReceiptPermanent(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("R_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            accReceivePaymentModuleServiceObj.deleteReceiptPermanent(paramJobj);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.receipt.del", null, RequestContextUtils.getLocale(request));   //"Receipt(s) has been deleted successfully";
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + (ex.getMessage() != null ? ex.getMessage() : ex.getCause().getMessage());
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void deleteReceiptPermanently(HttpServletRequest request,String companyid, Receipt receipt) throws AccountingException, SessionExpiredException, ServiceException, ParseException {
        try {
            KwlReturnObject result;
            Map<String, Object> requestMap = AccountingManager.getGlobalParams(request);
            if (receipt != null && receipt.getRevalJeId() != null) {
                result = accJournalEntryobj.deleteJEDtails(receipt.getRevalJeId(), companyid);// 2 For realised JE
                result = accJournalEntryobj.deleteJE(receipt.getRevalJeId(), companyid);
            }
            
            /**
             * Need to check TDS / TCS JE posted for receipt
             */
            if (receipt.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("receiptid", receipt.getID());
                KwlReturnObject kwlReturnObject = accReceiptDAOobj.getReceiptInvoiceJEMapping(jSONObject);
                if (!kwlReturnObject.getEntityList().isEmpty() && kwlReturnObject.getEntityList().size() > 0 && kwlReturnObject.getEntityList().get(0) != null) {
                    throw new AccountingException(messageSource.getMessage("acc.reconcilation.Cannotdeletepayment", null, RequestContextUtils.getLocale(request)) + " " + "<b>" + receipt.getReceiptNumber() + "</b>" + " " + messageSource.getMessage("acc.tdstcs.linked", null, RequestContextUtils.getLocale(request)));
                }
            }

            /*
             Delete Forex Gains/Loss JEs 
             */
            if (receipt.getLinkDetailReceipts() != null && !receipt.getLinkDetailReceipts().isEmpty()) {
                Set<LinkDetailReceipt> linkedDetailReceiptList = receipt.getLinkDetailReceipts();
                for (LinkDetailReceipt ldprow : linkedDetailReceiptList) {
                    if (!StringUtil.isNullOrEmpty(ldprow.getLinkedGainLossJE())) {
                        result = accJournalEntryobj.deleteJEDtails(ldprow.getLinkedGainLossJE(), companyid);
                        result = accJournalEntryobj.deleteJE(ldprow.getLinkedGainLossJE(), companyid);
                    }
                    /**
                     * Delete JE posted for GST Linking
                     */
                     if (!StringUtil.isNullOrEmpty(ldprow.getLinkedGSTJE())) {
                        result = accJournalEntryobj.deleteJEDtails(ldprow.getLinkedGSTJE(), companyid);
                        result = accJournalEntryobj.deleteJE(ldprow.getLinkedGSTJE(), companyid);
                    }
                }
            }

            if (receipt != null) {
                accReceivePaymentModuleServiceObj.updateOpeningBalance(receipt, companyid);
                JSONObject params = new JSONObject();
                accReceivePaymentModuleServiceObj.updateReceiptAdvancePaymentAmountDue(receipt, companyid,params);
            }
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("receiptid", receipt.getID());
            requestParams.put("companyid", companyid);
            requestParams.put("receiptno", receipt.getReceiptNumber());
            if (!StringUtil.isNullOrEmpty(receipt.getID())) {
                if (receipt.getLinkDetailReceipts() != null && !receipt.getLinkDetailReceipts().isEmpty()) {
                    accReceiptDAOobj.deleteLinkReceiptsDetailsAndUpdateAmountDue(requestMap,receipt.getID(), companyid,false);
                }
                accReceiptDAOobj.deleteReceiptPermanent(requestParams);
            }

        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)));
        }
    }
    /*
        Update payment advance amount due on receipt with refund entry delete
    */
  
    public ModelAndView deleteOpeningReceiptPermanent(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("R_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            deleteOpeningReceiptPermanent(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.receipt.del", null, RequestContextUtils.getLocale(request));   //"Receipt(s) has been deleted successfully";
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void deleteOpeningReceiptPermanent(HttpServletRequest request) throws AccountingException, SessionExpiredException, ServiceException {

        String companyid = sessionHandlerImpl.getCompanyid(request);
        String receiptid[] = request.getParameterValues("billidArray");
        String invoiceno[] = request.getParameterValues("invoicenoArray");
        for (int count = 0; count < receiptid.length; count++) {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("receiptid", receiptid[count]);
            requestParams.put("companyid", companyid);
            try {
                if (!StringUtil.isNullOrEmpty(receiptid[count])) {

                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Receipt.class.getName(), receiptid[count]);
                    Receipt receipt = (Receipt) objItr.getEntityList().get(0);
                    if (receipt != null) {
                        accReceivePaymentModuleServiceObj.updateOpeningBalance(receipt, companyid);
                        JSONObject params = new JSONObject();
                        accReceivePaymentModuleServiceObj.updateReceiptAdvancePaymentAmountDue(receipt, companyid,params);
                    }
                    accReceiptDAOobj.deleteReceiptPermanent(requestParams);
                    auditTrailObj.insertAuditLog(AuditAction.OPENING_BALANCE_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted Opening Balance Receipt Permanently " + invoiceno[count], request, receiptid[count]);
                }


            } catch (Exception ex) {
                throw new AccountingException(messageSource.getMessage("acc.pay1.excp1", null, RequestContextUtils.getLocale(request)));
            }
        }
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
                boolean withInventory = Boolean.parseBoolean(request.getParameter("withInv"));
                HashMap<String, Object> hm = new HashMap<String, Object>();
                hm.put("receiptid", SOIDList.get(cnt));
                hm.put("isprinted", request.getParameter("isprinted"));
                if (!StringUtil.isNullOrEmpty(SOIDList.get(cnt))) {
                    result = accReceiptDAOobj.saveReceipt(hm);
                }
            }
            msg = messageSource.getMessage("acc.field.setsuccessfully", null, RequestContextUtils.getLocale(request));
            issuccess = true;
            txnManager.commit(status);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    } 
     
     public ModelAndView getInvoiceAdvanceCNDN(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
            String msg="";
            boolean issuccess = false;

//            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
//            def.setName("VP_Tx");
//            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
//            TransactionStatus status = txnManager.getTransaction(def);
            try {
                String companyid = sessionHandlerImpl.getCompanyid(request);
                JSONArray jArr=new JSONArray();
                HashMap<String,String> paymentHashMap=new HashMap<String, String>();
                if(request.getParameter("selectedData")!=null){
                    jArr=new JSONArray(request.getParameter("selectedData"));
                    if(jArr.length()>0){
                        JSONObject jSONObject=jArr.getJSONObject(0);
                         if(!StringUtil.isNullOrEmpty(jSONObject.optString("billid",""))){
                            paymentHashMap.put("paymentId", jSONObject.getString("billid"));
                            paymentHashMap.put("invoiceadvcndntype", jSONObject.getString("invoiceadvcndntype"));
                             KwlReturnObject result = accReceiptDAOobj.getInvoiceAdvPaymentList(paymentHashMap);
                             List paymentList=result.getEntityList();
                              Iterator iter = paymentList.iterator();
                              JSONArray jrr=new JSONArray();
                              while (iter.hasNext()) {  
                                    JSONObject jSONObj=new JSONObject();
                                    Receipt receipt = (Receipt) iter.next();
                                    jSONObj.put("paymentID",receipt.getID());
                                    jSONObj.put("invoiceadvcndntype",receipt.getInvoiceAdvCndnType());
                                    jrr.put(jSONObj);
                            }              
                            jobj.put("data",jrr);
                            jobj.put("count",paymentList.size());
                         }
                    }
                    
                }
//                txnManager.commit(status);
                issuccess = true;
                msg = messageSource.getMessage("acc.pay.del", null, RequestContextUtils.getLocale(request));  //"Payment(s) has been deleted successfully";
            } catch (SessionExpiredException ex) {
//                txnManager.rollback(status);
                msg = ex.getMessage();
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
//                txnManager.rollback(status);
                msg = ""+ex.getMessage();
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            } finally{
                try {
                    jobj.put("success", issuccess);
                    jobj.put("msg", msg);
                } catch (JSONException ex) {
                    Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return new ModelAndView("jsonView", "model", jobj.toString());
    }
 
    public ModelAndView unlinkReceiptInvoices(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            List li = unlinkReceiptNew(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.field.PaymentinformationhasbeenUnLinkedtoCustomerInvoicesuccessfully", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
}
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public List unlinkReceiptNew(HttpServletRequest request) throws ServiceException, SessionExpiredException,JSONException,AccountingException {
        List result = new ArrayList();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String paymentid =request.getParameter("paymentid");
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

            KwlReturnObject receiptKWLObj = accountingHandlerDAOobj.getObject(Receipt.class.getName(), paymentid);
            Receipt receipt = (Receipt) receiptKWLObj.getEntityList().get(0);
            String paymentno =receipt.getReceiptNumber();
            
            String linkedInvoiceids="";
            String linkedInvoicenos="";
            List<String> linkedDetailInvoice = new ArrayList();
            JSONArray linkJSONArray=request.getParameter("linkdetails")!=null?new JSONArray(request.getParameter("linkdetails")):new JSONArray();
            for (int k = 0; k < linkJSONArray.length(); k++) {//creating a hash map with payment and their linked invoice
                JSONObject jSONObject=linkJSONArray.getJSONObject(k);
                String linkId=jSONObject.optString("linkdetailid","");
                linkedDetailInvoice.add(linkId);
            }
            
            double receiptexternalCurrencyRate = 1d;
            boolean isopeningBalanceRCP = receipt.isIsOpeningBalenceReceipt();
            Date rcpCreationDate = null;
            rcpCreationDate = receipt.getCreationDate();
            if (isopeningBalanceRCP) {
                receiptexternalCurrencyRate = receipt.getExchangeRateForOpeningTransaction();
            } else {
//                rcpCreationDate = receipt.getJournalEntry().getEntryDate();
                receiptexternalCurrencyRate = receipt.getJournalEntry().getExternalCurrencyRate();
            }
            String fromcurrencyid = receipt.getCurrency().getCurrencyID();
            
            String linkedDetailIDs = "";
            for(String invID : linkedDetailInvoice) {
                linkedDetailIDs = linkedDetailIDs.concat("'").concat(invID).concat("',");
            }
            if(!StringUtil.isNullOrEmpty(linkedDetailIDs.toString())) {
                linkedDetailIDs = linkedDetailIDs.substring(0, linkedDetailIDs.length()-1);
            }
            double sumOfTotalAmount = 0;
            List<Invoice> invoiceList=new ArrayList<Invoice>();
            Set<String> invoiceIDSet = new HashSet<>();
            List<LinkDetailReceipt> details = accReceiptDAOobj.getDeletedLinkedReceiptInvoices(receipt, linkedDetailInvoice, companyid);
            for (LinkDetailReceipt receiptDetail : details) {
                Invoice invoice = receiptDetail.getInvoice();
                boolean isOpeningInvoice = invoice.isIsOpeningBalenceInvoice();
                double externalCurrencyRateForLinking  = receiptDetail.getExchangeRateForTransaction();
                sumOfTotalAmount += receiptDetail.getAmount();
                
                 //Converting amount in invoice currency
                double ammountInInvoiceCurrency;
                if (externalCurrencyRateForLinking != 0) {
                    ammountInInvoiceCurrency = authHandler.round(receiptDetail.getAmount() / externalCurrencyRateForLinking, companyid);
                } else {
                    ammountInInvoiceCurrency = receiptDetail.getAmountInInvoiceCurrency();
                }

                //Converting Invoice amount in Base currency accrding to invoice exchange rate
//                Date invoiceCreationdate = isOpeningInvoice ? invoice.getCreationDate() : invoice.getJournalEntry().getEntryDate();
                Date invoiceCreationdate = invoice.getCreationDate();
                double externalCurrencyRate = isOpeningInvoice ? invoice.getExchangeRateForOpeningTransaction() : invoice.getJournalEntry().getExternalCurrencyRate();
                
                HashMap<String, Object> requestParams = new HashMap();
                requestParams.put(Constants.companyid, companyid);
                requestParams.put("gcurrencyid", company.getCurrency().getCurrencyID());
                
                KwlReturnObject bAmt = null;
//                if (isOpeningInvoice && invoice.isConversionRateFromCurrencyToBase()) {
//                    bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, ammountInInvoiceCurrency, invoice.getCurrency().getCurrencyID(), invoiceCreationdate, externalCurrencyRate);
//                } else {
//                    bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, ammountInInvoiceCurrency, invoice.getCurrency().getCurrencyID(), invoiceCreationdate, externalCurrencyRate);
//                }
//                double amountReceivedConvertedInInvoiceBaseCurrency = authHandler.round((Double) bAmt.getEntityList().get(0),companyid);
                double amountReceivedConvertedInInvoiceBaseCurrency = 0;
                
                if (invoice.isIsOpeningBalenceInvoice()) {
                    double amountdue = invoice.getOpeningBalanceAmountDue();
                    /*
                     * set status flag for opening invoices
                     */
                    double amountdueforstatus = amountdue + receiptDetail.getAmountInInvoiceCurrency();
                    if (authHandler.round(amountdueforstatus, companyid) <= 0) {
                        invoice.setIsOpenReceipt(false);
                    } else {
                        invoice.setIsOpenReceipt(true);
                    }
                    if (invoice.isConversionRateFromCurrencyToBase()) {
                        bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, (invoice.getOpeningBalanceAmountDue() + receiptDetail.getAmountInInvoiceCurrency()), invoice.getCurrency().getCurrencyID(), invoiceCreationdate, externalCurrencyRate);
                    } else {
                        bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, (invoice.getOpeningBalanceAmountDue() + receiptDetail.getAmountInInvoiceCurrency()), invoice.getCurrency().getCurrencyID(), invoiceCreationdate, externalCurrencyRate);
                    }
                    amountReceivedConvertedInInvoiceBaseCurrency = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                    invoice.setOpeningBalanceAmountDue(invoice.getOpeningBalanceAmountDue() + receiptDetail.getAmountInInvoiceCurrency());
                    invoice.setOpeningBalanceBaseAmountDue(amountReceivedConvertedInInvoiceBaseCurrency);
                } else {
                    double amountdue = invoice.getInvoiceamountdue();
//                    double amountDueInbase = invoice.getInvoiceAmountDueInBase(); 
                    /*
                     * set status flag for amount due
                     */
                    double amountdueforstatus = amountdue + receiptDetail.getAmountInInvoiceCurrency();
                    if (authHandler.round(amountdueforstatus, companyid) <= 0) {
                        invoice.setIsOpenReceipt(false);
                    } else {
                        invoice.setIsOpenReceipt(true);
                    }
                    invoice.setInvoiceamountdue(amountdue + receiptDetail.getAmountInInvoiceCurrency());
                    bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, (amountdue + receiptDetail.getAmountInInvoiceCurrency()), invoice.getCurrency().getCurrencyID(), invoiceCreationdate, externalCurrencyRate);
                    amountReceivedConvertedInInvoiceBaseCurrency = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                    invoice.setInvoiceAmountDueInBase(amountReceivedConvertedInInvoiceBaseCurrency);
                    if ((amountdue + receiptDetail.getAmountInInvoiceCurrency()) != 0) {
                        invoice.setAmountDueDate(null);
                    }
                }
                
                linkedInvoicenos = linkedInvoicenos.concat(invoice.getInvoiceNumber()).concat(",");
                invoiceList.add(invoice);
                invoiceIDSet.add(invoice.getID());
                
                // check if Forex/Gain Loss JE generated. If yes then need to delete JE too
                if(receiptDetail.getLinkedGainLossJE()!=null && !receiptDetail.getLinkedGainLossJE().isEmpty()) {
                    deleteJEArray(receiptDetail.getLinkedGainLossJE(), companyid);
                }

                /**
                 * Need to check TDS / TCS JE posted for receipt
                 */
                if (receipt.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                    JSONObject jSONObject = new JSONObject();
                    jSONObject.put("receiptid", receipt.getID());
                    KwlReturnObject kwlReturnObject = accReceiptDAOobj.getReceiptInvoiceJEMapping(jSONObject);
                    if (!kwlReturnObject.getEntityList().isEmpty() && kwlReturnObject.getEntityList().size() > 0 && kwlReturnObject.getEntityList().get(0) != null) {
                        throw new AccountingException(messageSource.getMessage("acc.reconcilation.Cannotdeletepayment", null, RequestContextUtils.getLocale(request)) + " " + "<b>" + receipt.getReceiptNumber() + "</b>" + " " + messageSource.getMessage("acc.tdstcs.linked", null, RequestContextUtils.getLocale(request)));
                    }
                }
                /**
                 * Delete JE posted for GST Linking
                 */
                if (receiptDetail.getLinkedGSTJE() != null && !receiptDetail.getLinkedGSTJE().isEmpty()) {
                    deleteJEArray(receiptDetail.getLinkedGSTJE(), companyid);
                }
                if (receiptDetail != null && !StringUtil.isNullOrEmpty(receiptDetail.getRevalJeId()) ) {
                    accJournalEntryobj.deleteJEDtails(receiptDetail.getRevalJeId(), companyid);
                    accJournalEntryobj.deleteJE(receiptDetail.getRevalJeId(), companyid);
                }
                if (receiptDetail != null && !StringUtil.isNullOrEmpty(receiptDetail.getRevalJeIdReceipt()) ) {
                    accJournalEntryobj.deleteJEDtails(receiptDetail.getRevalJeIdReceipt(), companyid);
                    accJournalEntryobj.deleteJE(receiptDetail.getRevalJeIdReceipt(), companyid);
                }
                
                /* Deleting Linking information of Received Payment from Linking table if it is unlinked*/
                HashMap<String, Object> linkingrequestParams = new HashMap<String, Object>();
                linkingrequestParams.put("receiptid", receipt.getID());
                linkingrequestParams.put("linkedTransactionID", receiptDetail.getInvoice().getID());
                linkingrequestParams.put("unlinkflag", true);
                accReceiptDAOobj.deleteLinkingInformationOfRP(linkingrequestParams);
            }
            
            if (sumOfTotalAmount!=0 && receipt.getReceiptAdvanceDetails() != null && !receipt.getReceiptAdvanceDetails().isEmpty()) {
                for (ReceiptAdvanceDetail advanceDetail : receipt.getReceiptAdvanceDetails()) {
                    double linkedAmountDue = advanceDetail.getAmountDue();
                    /**
                     * For Unlink Transaction from Receipt, Update Amount Due
                     * with rounding.
                     */
                    double finalamountdue = authHandler.round(linkedAmountDue + sumOfTotalAmount, companyid);
                    advanceDetail.setAmountDue(finalamountdue);
                    List<Object> objectList = new ArrayList<Object>();
                    objectList.add((Object) advanceDetail);
                    accAccountDAOobj.saveOrUpdateAll(objectList);
                }
            } else if(sumOfTotalAmount!=0 && isopeningBalanceRCP) {
                HashMap<String, Object> requestParams = new HashMap();
                requestParams.put(Constants.companyid, companyid);
                requestParams.put("gcurrencyid", company.getCurrency().getCurrencyID());
                KwlReturnObject bAmt = null;
                if (isopeningBalanceRCP && receipt.isConversionRateFromCurrencyToBase()) {// if Receipt is opening balance Receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, sumOfTotalAmount, fromcurrencyid, rcpCreationDate, receiptexternalCurrencyRate);
                } else {
                        bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, sumOfTotalAmount, fromcurrencyid, rcpCreationDate, receiptexternalCurrencyRate);
                }
                double amountPaymentConvertedInBaseCurrency = (Double) bAmt.getEntityList().get(0);
                receipt.setOpeningBalanceAmountDue(sumOfTotalAmount + receipt.getOpeningBalanceAmountDue());
                receipt.setOpeningBalanceBaseAmountDue(amountPaymentConvertedInBaseCurrency + receipt.getOpeningBalanceBaseAmountDue());
                List<Object> objectList = new ArrayList<Object>();
                objectList.add((Object) receipt);
                accAccountDAOobj.saveOrUpdateAll(objectList);
            }
            if(!invoiceList.isEmpty()){
                List<Object> objectList = new ArrayList<Object>(invoiceList);
                accAccountDAOobj.saveOrUpdateAll(objectList);
            }
            if(!StringUtil.isNullOrEmpty(linkedInvoicenos)) {
                linkedInvoicenos = linkedInvoicenos.substring(0, linkedInvoicenos.length()-1);
            }
            accReceiptDAOobj.deleteSelectedLinkedReceiptInvoices(receipt.getID(), linkedDetailIDs, companyid,"");

            if (!StringUtil.isNullOrEmpty(linkedInvoicenos)) {
                auditTrailObj.insertAuditLog(AuditAction.LINKEDRECEIPT, "User " + sessionHandlerImpl.getUserFullName(request) + " has unlinked Receipt " + paymentno + " from " + linkedInvoicenos, request, linkedInvoiceids);
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
                deleteJEArray(roundingJE.getID(), companyid);
            }

            if (!StringUtil.isNullOrEmpty(roundingJENo)) {
                roundingJENo = roundingJENo.substring(0, roundingJENo.length() - 1);
            }
            if (!StringUtil.isNullOrEmpty(roundingIDs)) {
                roundingIDs = roundingIDs.substring(0, roundingIDs.length() - 1);
            }
            if (!StringUtil.isNullOrEmpty(roundingJENo)) {
                auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " "+messageSource.getMessage("acc.roundingje.unlinkedreceipt", null, RequestContextUtils.getLocale(request)) + " "+paymentno + " "+ messageSource.getMessage("acc.roundingje.fromsalesinvoice", null, RequestContextUtils.getLocale(request))+ " "+linkedInvoicenos + "."+ messageSource.getMessage("acc.roundingje.roundingje", null, RequestContextUtils.getLocale(request))+" "+ roundingJENo + " "+messageSource.getMessage("acc.roundingje.roundingjedelted", null, RequestContextUtils.getLocale(request))+".", request, roundingIDs);
            }

//            auditTrailObj.insertAuditLog(AuditAction.LINKEDRECEIPT, "User " + sessionHandlerImpl.getUserFullName(request) + " has unlinked Receipt "+paymentno+" from "+linkedInvoicenos, request,linkedInvoiceids); 

        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return result;
    }
    public double RefundReceiptForexGailLossAmount(JSONObject requestObj, JSONObject jobj, Receipt receipt, String transactionCurrencyId, String currencyid, double externalCurrencyRate) throws ServiceException, SessionExpiredException, JSONException, ParseException {
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        String companyId = requestObj.optString(Constants.companyid);
        DateFormat df = (DateFormat)requestObj.opt("dateonlyformat");
        requestParams.put("gcurrencyid", requestObj.optString("baseCurrency"));
        requestParams.put("companyid", companyId);
        requestParams.put("dateformat", df);
//        double enterAmountPaymentCurrencyOld = 0;
//        double enterAmountTrancastionCurrencyNew = 0;
        double amountdiff = 0;
        Date creationDate = null;
        if (!StringUtil.isNullOrEmpty(requestObj.optString("creationdate"))) { // Check added for SDP-5827 as in link case creation date is Null
            creationDate = df.parse(requestObj.getString("creationdate"));
        } else {
//            creationDate = receipt.getJournalEntry().getEntryDate();
            creationDate = receipt.getCreationDate();
        }
        double paymentExternalCurrencyRate = StringUtil.isNullOrEmpty(requestObj.optString("externalcurrencyrate")) ? receipt.getExternalCurrencyRate() : Double.parseDouble(requestObj.optString("externalcurrencyrate"));
        boolean revalFlag = false;
        double newrate = 0.0;
        double amount = 0;
        try {
            Date paymentDate = null;
            String JE = "";
            Double enteramount = jobj.getDouble("enteramount");
            String documentId = jobj.getString("documentid");
            Double recinvamount = jobj.getDouble("enteramount");
            boolean isopeningBalanceRecceipt = jobj.optBoolean("isopeningBalanceRecceipt", false);
            boolean isConversionRateFromCurrencyToBase = jobj.optBoolean("isConversionRateFromCurrencyToBase", false);
            double ratio = 0;


            KwlReturnObject resultObject = null;
            resultObject = accReceiptDAOobj.getPaymentInformationFromAdvanceDetailId(documentId, companyId);
            List list = resultObject.getEntityList();
            Object[] object = (Object[]) list.get(0);
            externalCurrencyRate = Double.parseDouble(object[0].toString());

            //if Advance is Revaluated then we will revaluated rate
            HashMap<String, Object> invoiceId = new HashMap<>();
            invoiceId.put("invoiceid", documentId);
            invoiceId.put("companyid", requestObj.optString(Constants.companyid));
            KwlReturnObject result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
            RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
            if (history != null) {
                externalCurrencyRate = history.getEvalrate();
                newrate = jobj.optDouble("exchangeratefortransaction", 0.0);
                revalFlag = true;
            }

            JE = object[1].toString();
            KwlReturnObject JEResult = accJournalEntryobj.getEntryDateFromJEId(JE, companyId);
            List JEList = JEResult.getEntityList();
            paymentDate = (Date) JEList.get(0);

            result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), requestObj.optString("baseCurrency"));
            KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
            String currid = currency.getCurrencyID();
            if (object[4] != null) {
                currid = object[4].toString();
            }
//                enterAmountTrancastionCurrencyNew = enteramount;
//                if (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString())) {
//                    double exchangeratefortransaction = Double.parseDouble(jobj.optString("exchangeratefortransaction", "1"));
//                    enterAmountTrancastionCurrencyNew = enteramount / exchangeratefortransaction;
//                    enterAmountTrancastionCurrencyNew = authHandler.round(enterAmountTrancastionCurrencyNew, 2);
//                    KwlReturnObject bAmtCurrencyFilter = null;
//                    bAmtCurrencyFilter = accCurrencyobj.getOneCurrencyToOtherModified(requestParams, enterAmountTrancastionCurrencyNew, transactionCurrencyId, currencyid, paymentDate, externalCurrencyRate);
//                    enterAmountPaymentCurrencyOld = authHandler.round((Double) bAmtCurrencyFilter.getEntityList().get(0),2);
//                    amountdiff=enteramount-enterAmountPaymentCurrencyOld;
//            }

            if (currid.equalsIgnoreCase(currencyid)) {

//                double paymentExternalCurrencyRate = StringUtil.isNullOrEmpty(request.getParameter("externalcurrencyrate")) ? receipt.getExternalCurrencyRate() : Double.parseDouble(request.getParameter("externalcurrencyrate"));
                if (externalCurrencyRate != paymentExternalCurrencyRate) {
                    result = accCurrencyobj.getOneCurrencyToOtherWithDiffRates(requestParams, 1.0, currid, currencyid, paymentDate, externalCurrencyRate, paymentExternalCurrencyRate);
                } else {
                    result = accCurrencyobj.getOneCurrencyToOther(requestParams, 1.0, currid, currencyid, paymentDate, externalCurrencyRate);
                }
            } else {

//                double paymentExternalCurrencyRate = StringUtil.isNullOrEmpty(request.getParameter("externalcurrencyrate")) ? 0.0 : Double.parseDouble(request.getParameter("externalcurrencyrate"));
                if (externalCurrencyRate != paymentExternalCurrencyRate) {
                    result = accCurrencyobj.getOneCurrencyToOtherWithDiffRates(requestParams, 1.0, currid, currencyid, paymentDate, externalCurrencyRate, paymentExternalCurrencyRate);
                } else {
                    result = accCurrencyobj.getOneCurrencyToOtherModified(requestParams, 1.0, currid, currencyid, paymentDate, externalCurrencyRate);
                }
//                    }
            }
            double oldrate = (Double) result.getEntityList().get(0);

            if (jobj.optDouble("exchangeratefortransaction", 0.0) != oldrate && jobj.optDouble("exchangeratefortransaction", 0.0) != 0.0 && Math.abs(jobj.optDouble("exchangeratefortransaction", 0.0) - oldrate) >= 0.000001) {
                newrate = jobj.optDouble("exchangeratefortransaction", 0.0);
                ratio = oldrate - newrate;
                amount = (recinvamount - (recinvamount / newrate) * oldrate) / newrate;
                KwlReturnObject bAmtActual = accCurrencyobj.getBaseToCurrencyAmount(requestParams, amount, currencyid, creationDate, newrate);
                amountdiff += (Double) bAmtActual.getEntityList().get(0);
            } else {
                if (currid.equalsIgnoreCase(currencyid)) {

//                    double paymentExternalCurrencyRate = StringUtil.isNullOrEmpty(request.getParameter("externalcurrencyrate")) ? 0.0 : Double.parseDouble(request.getParameter("externalcurrencyrate"));
                    if (externalCurrencyRate != paymentExternalCurrencyRate) {
                        result = accCurrencyobj.getOneCurrencyToOtherWithDiffRates(requestParams, 1.0, currid, currencyid, paymentDate, externalCurrencyRate, paymentExternalCurrencyRate);
                    } else {
                        result = accCurrencyobj.getOneCurrencyToOther(requestParams, 1.0, currid, currencyid, paymentDate, externalCurrencyRate);
                    }
                } else {
//                    double paymentExternalCurrencyRate = StringUtil.isNullOrEmpty(request.getParameter("externalcurrencyrate")) ? 0.0 : Double.parseDouble(request.getParameter("externalcurrencyrate"));
                    if (externalCurrencyRate != paymentExternalCurrencyRate) {
                        result = accCurrencyobj.getOneCurrencyToOtherWithDiffRates(requestParams, 1.0, currid, currencyid, paymentDate, externalCurrencyRate, paymentExternalCurrencyRate);
                    } else {
                        result = accCurrencyobj.getOneCurrencyToOtherModified(requestParams, 1.0, currid, currencyid, paymentDate, externalCurrencyRate);
                    }
                }
                if (!revalFlag) {
                    newrate = (Double) result.getEntityList().get(0);
                }
                if (Math.abs(jobj.optDouble("exchangeratefortransaction", 0.0) - oldrate) >= 0.000001) {
                    ratio = oldrate - newrate;
                }
                amount = recinvamount * ratio;
                KwlReturnObject bAmtActual = null;
                if (isopeningBalanceRecceipt && isConversionRateFromCurrencyToBase) {// if receipt is opening balance receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmtActual = accCurrencyobj.getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amount, currencyid, creationDate, externalCurrencyRate);
                } else {
                    bAmtActual = accCurrencyobj.getBaseToCurrencyAmount(requestParams, amount, currencyid, creationDate, externalCurrencyRate);
                }
                amountdiff += (Double) bAmtActual.getEntityList().get(0);
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("cndnPaymentForexGailLossAmount : " + ex.getMessage(), ex);
        }
        return (amountdiff);
    }
    
    
    public ModelAndView linkReceiptToDocuments(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
	synchronized (this) {   //SDP-13011
            TransactionStatus status = txnManager.getTransaction(def);
            try {
                JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
                String ReceiptJENumber="";
                // if linking receipt of refund type
                boolean isRefundTransaction = (!StringUtil.isNullOrEmpty(request.getParameter("isRefundTransaction"))) ? Boolean.parseBoolean(request.getParameter("isRefundTransaction")) : false;
                JSONObject requestObj=convertRequestToJSONObjectForLinkPayment(request);
                List li = linkReceiptToDocuments(requestObj);
                issuccess = true;
                if (li.size()!= 0 && li.get(0) != null) {
                     ReceiptJENumber= (String) li.get(0);
                }

                // success msg for refund transaction
                if (isRefundTransaction) {
                    msg = messageSource.getMessage("acc.field.paymentInformationHasBeenLinkedtoAdvancePaymentSuccessfully", null, RequestContextUtils.getLocale(request));
                } else {
                    msg = messageSource.getMessage("acc.field.PaymentinformationhasbeenLinkedtoCIAndDNsuccessfully", null, RequestContextUtils.getLocale(request));
                    if(!StringUtil.isNullOrEmpty(ReceiptJENumber)){
                        msg += ""+ "JE No.: " +"<b>" + ReceiptJENumber + "<b>";
                    }
                    
                }

                txnManager.commit(status);

                //========Code for Rounding JE Start=============
                try {
                    accReceivePaymentModuleServiceObj.postRoundingJEAfterLinkingInvoiceInReceipt(paramJobj);
                } catch (ServiceException | TransactionException ex) {
                    Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
                }
                //========Code for Rounding JE End=============

            } catch (SessionExpiredException ex) {
                txnManager.rollback(status);
                msg = ex.getMessage();
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                txnManager.rollback(status);
                msg = "" + ex.getMessage();
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    jobj.put("success", issuccess);
                    jobj.put("msg", msg);
                } catch (JSONException ex) {
                    Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public JSONObject convertRequestToJSONObjectForLinkPayment(HttpServletRequest request) throws JSONException,SessionExpiredException {
        boolean isRefundTransaction = (!StringUtil.isNullOrEmpty(request.getParameter("isRefundTransaction"))) ? Boolean.parseBoolean(request.getParameter("isRefundTransaction")) : false;
        String companyId = sessionHandlerImpl.getCompanyid(request);
        JSONArray linkJSONArray = request.getParameter("linkdetails") != null ? new JSONArray(request.getParameter("linkdetails")) : new JSONArray();
        String linkingdateString = !StringUtil.isNullOrEmpty(request.getParameter("linkingdate")) ? request.getParameter("linkingdate") : "";
        String receiptid = !StringUtil.isNullOrEmpty(request.getParameter("paymentid")) ? request.getParameter("paymentid") : "";
        Locale locale = RequestContextUtils.getLocale(request);
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        JSONObject requestObj = new JSONObject();
        requestObj.put(Constants.companyid, companyId);
        requestObj.put("isRefundTransaction", isRefundTransaction);
        requestObj.put("linkdetails", linkJSONArray);
        requestObj.put("linkingdateString", linkingdateString);
        requestObj.put("receiptid", receiptid);
        requestObj.put("locale", locale);
        requestObj.put("paramJobj", paramJobj);
        requestObj.put("paymentid", request.getParameter("paymentid"));
        requestObj.put("amounts", request.getParameter("amounts"));
        requestObj.put("userfullname", sessionHandlerImpl.getUserFullName(request));
        requestObj.put("userid", sessionHandlerImpl.getUserid(request));
        requestObj.put("x-real-ip", request.getHeader("x-real-ip"));
        requestObj.put("detail", request.getParameter("detail"));
        requestObj.put("remoteaddr", request.getRemoteAddr());
        requestObj.put("baseCurrency", sessionHandlerImpl.getCurrencyID(request));
        requestObj.put("globalparams",AccountingManager.getGlobalParams(request));
        requestObj.put("dateonlyformat", authHandler.getDateOnlyFormat(request));
        requestObj.put("creationdate", request.getParameter("creationdate"));
        requestObj.put("externalcurrencyrate", request.getParameter("externalcurrencyrate"));
        return requestObj;
    }
    
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
        requestParams.put("asofdate", request.getParameter("asofdate"));
        requestParams.put("isAged", request.getParameter("isAged"));
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
        if (request.getAttribute("custVendorID") != null) {
            requestParams.put("custVendorID", request.getAttribute("custVendorID").toString());
        } else {
            requestParams.put("custVendorID", request.getParameter("custVendorID"));
        }
        requestParams.put("datefilter", request.getParameter("datefilter"));
        requestParams.put(InvoiceConstants.duration, (request.getParameter(InvoiceConstants.duration) != null) ? Integer.parseInt(request.getParameter(InvoiceConstants.duration)) : 0);
        requestParams.put("isConsignment", (request.getParameter("isConsignment") != null) ? Boolean.parseBoolean(request.getParameter("isConsignment")) : false);
        requestParams.put("custWarehouse", (request.getParameter("custWarehouse") == null) ? "" : request.getParameter("custWarehouse"));
        requestParams.put("upperLimitDate", request.getParameter("upperLimitDate") == null ? "" : request.getParameter("upperLimitDate"));
        if (request.getParameter("isReceipt") != null) {
            requestParams.put("isReceipt", request.getParameter("isReceipt"));
        }
        requestParams.put("filterForClaimedDateForPayment", request.getParameter("filterForClaimedDateForPayment")==null?"":request.getParameter("filterForClaimedDateForPayment"));
        requestParams.put("isDraft", (request.getParameter("isDraft") != null) ? Boolean.parseBoolean(request.getParameter("isDraft")) : false);
        return requestParams;
    }
    
    public List linkReceiptToDocuments(JSONObject requestObj) throws ServiceException, SessionExpiredException, AccountingException {
        List result = new ArrayList();
        List linkedInvoicesList = new ArrayList();
        List linkedNotesList = new ArrayList();
        List linkedAdvancePaymentList = new ArrayList();
//        JSONObject requestObj=new JSONObject();
        try {

            JSONArray invoiceArray = new JSONArray();
            JSONArray noteArray = new JSONArray();
            JSONArray advancePaymentArray = new JSONArray();
            String companyId = requestObj.optString(Constants.companyid);
            JSONArray linkJSONArray = requestObj.optJSONArray("linkdetails");
            
            Map<String,Object> counterMap=new HashMap<>();
            counterMap.put("counter", 0);
            JSONObject paramJobj = requestObj.optJSONObject("paramJobj");
            /**
             * if linking date is fall in closed year the exception has been
             * thrown.
             */
            String linkingdateString = requestObj.optString("linkingdateString");
            DateFormat df = authHandler.getDateOnlyFormat();
            if (!StringUtil.isNullOrEmpty(linkingdateString)) {
                int linkingdate = df.parse(linkingdateString).getYear();
                paramJobj.put("yearid", linkingdate+1900);
            }
            Locale locale=(Locale)requestObj.opt("locale");
            boolean isBookClose = accCompanyPreferencesObj.isBookClose(paramJobj);
            if (isBookClose) {
                throw  new AccountingException(messageSource.getMessage("acc.compref.closebook.Youcannotlinkthedocumentsinclosedyear", null, locale));
            }
	    double linkamount = 0, receiptAmountDue = 0;
            for (int i = 0; i < linkJSONArray.length(); i++) {
                JSONObject obj = linkJSONArray.getJSONObject(i);
                int documenttype = Integer.parseInt(obj.optString("documentType"));
                if (documenttype == Constants.PaymentAgainstInvoice && obj.optDouble("linkamount", 0.0) != 0) {
		    linkamount += obj.optDouble("linkamount", 0.0);
                    invoiceArray.put(obj);
                } else if (documenttype == Constants.PaymentAgainstCNDN && obj.optDouble("linkamount", 0.0) != 0) {
		    linkamount += obj.optDouble("linkamount", 0.0);
                    noteArray.put(obj);
                } else if (documenttype == Constants.AdvancePayment && obj.optDouble("linkamount", 0.0) != 0) { // record of advance payment type added for linking to refund receipt
		    linkamount += obj.optDouble("linkamount", 0.0);
                    advancePaymentArray.put(obj);
                }
            }
            linkamount=authHandler.round(linkamount, companyId);
 	    
            /*
                Advance Payment(Receive Payment) link with Invoices throug Link Transaction Button call : If link invoice(s) total amount is greater than Payment amount due 
                then only below code gets executed.
                Refer : SDP-13011
            */
            if (linkamount > 0) {
                HashMap<String, Object> requestMap = new HashMap<String, Object>();
                requestMap.put(Constants.companyKey, paramJobj.get(Constants.companyKey));
                requestMap.put(Constants.globalCurrencyKey, paramJobj.get(Constants.globalCurrencyKey));
                String receiptid = requestObj.optString("receiptid");
                KwlReturnObject presult = accountingHandlerDAOobj.getObject(Receipt.class.getName(), receiptid);
                Receipt receipt = (Receipt) presult.getEntityList().get(0);
                if(receipt.isIsOpeningBalenceReceipt()){
                    receiptAmountDue = receipt.getOpeningBalanceAmountDue();
                }else if (receipt.getReceiptAdvanceDetails() != null) {
                    for (ReceiptAdvanceDetail receiptAdvanceDetail : receipt.getReceiptAdvanceDetails()) {
                        receiptAmountDue += receiptAdvanceDetail.getAmountDue();
                    }
                }
                receiptAmountDue = authHandler.round(receiptAmountDue, receipt.getCompany().getCompanyID());    //SDP-13452 - Need to round the receipt amount due to do comparison between receiptAmountDue and linkPayment
                if (receiptAmountDue < linkamount) {
                    throw new AccountingException(messageSource.getMessage("acc.receivepayment.advpayment.amountdue.validation1", null, locale) + "<b>" + receipt.getReceiptNumber() + "</b>" + messageSource.getMessage("acc.payment.advpayment.amountdue.validation2", null, locale));
                }
            }

            if (invoiceArray.length() > 0) {
                    linkedInvoicesList = linkReceiptToInvoices(requestObj, invoiceArray,counterMap);
            }
            if (noteArray.length() > 0) {
                linkedNotesList = linkReceiptToDebitNote(requestObj, noteArray,counterMap);
            }
            // linking advance payment to receipt of refund type
            if (advancePaymentArray.length() > 0) {
                linkedAdvancePaymentList = linkReceiptToAdvancePayment(requestObj, advancePaymentArray, counterMap);
            }
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (AccountingException e){
            throw new AccountingException(e.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        result.addAll(linkedInvoicesList);
        result.addAll(linkedNotesList);
        result.addAll(linkedAdvancePaymentList);
        return result;
    }
    
    public List linkReceiptToInvoices(JSONObject requestObj, JSONArray invoiceArray,Map<String,Object> counterMap) throws ServiceException, SessionExpiredException {
        List result = new ArrayList();
        GoodsReceipt greceipt = null;
        try {
            String companyid = requestObj.optString(Constants.companyid);
            String paymentid = requestObj.optString("paymentid");
            double amount = 0;
            String amounts[] = !StringUtil.isNullOrEmpty(requestObj.optString("amounts"))?requestObj.optString("amounts").split(","):new String[]{};
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

            KwlReturnObject receiptObj = accountingHandlerDAOobj.getObject(Receipt.class.getName(), paymentid);
            Receipt payment = (Receipt) receiptObj.getEntityList().get(0);
            String paymentno = payment.getReceiptNumber();

            JournalEntry je = null;
            String jeid = "";
            String JENumber="";
            boolean isopeningBalancePayment = payment.isIsOpeningBalenceReceipt();
            if (payment.isNormalReceipt()) {
                je = payment.getJournalEntry();
                jeid = je.getID();
            }

            double eternalCurrencyRate = 0d;
            if (!payment.isNormalReceipt() && payment.isIsOpeningBalenceReceipt()) {
                eternalCurrencyRate = payment.getExchangeRateForOpeningTransaction();
            }
            JSONArray jArr = new JSONArray();
            String customerId = "";
            String accountId = "";
            String linkedInvoiceids = "";
            String linkedInvoicenos = "";
            for (int k = 0; k < invoiceArray.length(); k++) {//creating a hash map with payment and their linked invoice
                JSONObject jSONObject = invoiceArray.getJSONObject(k);
                if (jSONObject.optDouble("linkamount", 0) != 0) {
                    String invoiceId = jSONObject.optString("documentid", "");
                    double invAmount = jSONObject.optDouble("linkamount", 0);
                    double exchangeratefortransaction = jSONObject.optDouble("exchangeratefortransaction", 1);
                    double amountdue=jSONObject.optDouble("amountdue",0);
                    double amountDueOriginal=jSONObject.optDouble("amountDueOriginal",0);

                    KwlReturnObject grresult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceId);
                    Invoice gr = (Invoice) grresult.getEntityList().get(0);
                    if (linkedInvoiceids.equals("") && gr != null) {
                        linkedInvoiceids += gr.getID();
                        linkedInvoicenos += gr.getInvoiceNumber();
                    } else if (gr != null) {
                        linkedInvoiceids += "," + gr.getID();
                        linkedInvoicenos += "," + gr.getInvoiceNumber();
                    }
                    customerId = gr.getCustomer().getID();
                    accountId = gr.getCustomer().getAccount().getID();
                    JSONObject jobj = new JSONObject();
                    jobj.put("enteramount", invAmount);
                    jobj.put("documentid", invoiceId);
                    jobj.put("isopeningBalancePayment", isopeningBalancePayment);
                    jobj.put("isConversionRateFromCurrencyToBase", payment.isConversionRateFromCurrencyToBase());
                    jobj.put("exchangeratefortransaction", exchangeratefortransaction);
                    jobj.put("amountdue", amountdue);
                    jobj.put("amountDueOriginal", amountDueOriginal);
                    jArr.put(jobj);
                    
                    /*Method is used to save linking informatio of Advance Received Payment 
                     *
                     when linking with Debit Note */
                    saveLinkingInformationOfPaymentWithSalesInvoice(gr, payment, payment.getReceiptNumber());
                }
            }
            HashMap<String, Object> paymenthm = new HashMap<String, Object>();
            HashMap linkDetails = saveLinkedReceiptDetails(requestObj, payment, company, jArr, greceipt, Constants.AdvancePayment,counterMap);
            paymenthm.put("receiptid", payment.getID());
            paymenthm.put("linkDetails", linkDetails.get("linkDetailPayment"));
            JENumber= linkDetails.containsKey("JournalEntries")&& !StringUtil.isNullOrEmpty((String)linkDetails.get("JournalEntries"))?(String)linkDetails.get("JournalEntries"):"";
            paymenthm.put("customerId", customerId);
            accReceiptDAOobj.saveReceipt(paymenthm);
            String userFullName = requestObj.optString("userfullname");
            HashMap<String, Object> auditRequestParams = new HashMap<>();
            auditRequestParams.put("userid", requestObj.optString("userid"));
            auditRequestParams.put("reqHeader", requestObj.optString("x-real-ip"));
            auditRequestParams.put("remoteAddress", requestObj.optString("remoteaddr"));
            auditTrailObj.insertAuditLog(AuditAction.LINKEDRECEIPT, "User " + userFullName + " has linked Receipt " + paymentno + " to Sales Invoice(s) " + linkedInvoicenos, auditRequestParams, linkedInvoiceids);
            result.add(JENumber);
        }  catch (Exception ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return result;
    }
    public List linkReceiptToDebitNote(JSONObject requestObj, JSONArray noteArray,Map<String,Object> counterMap) throws ServiceException, SessionExpiredException {
        List result = new ArrayList();
        try {
            String companyid = requestObj.optString(Constants.companyid);
            String paymentid = requestObj.optString("paymentid");
            double amount = 0;
            String amounts[] = !StringUtil.isNullOrEmpty(requestObj.optString("amounts"))?requestObj.optString("amounts").split(","):new String[]{};
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

            KwlReturnObject receiptObj = accountingHandlerDAOobj.getObject(Receipt.class.getName(), paymentid);
            Receipt receipt = (Receipt) receiptObj.getEntityList().get(0);
            String receiptno = receipt.getReceiptNumber();

            JournalEntry je = null;
            String jeid = "";
            boolean isopeningBalancePayment = receipt.isIsOpeningBalenceReceipt();
            if (receipt.isNormalReceipt()) {
                je = receipt.getJournalEntry();
                jeid = je.getID();
            }

            JournalEntryDetail updatejed = new JournalEntryDetail();
            boolean isopeningBalanceRecceipt = receipt.isIsOpeningBalenceReceipt();
            double eternalCurrencyRate = 0d;
            if (!receipt.isNormalReceipt() && receipt.isIsOpeningBalenceReceipt()) {
                eternalCurrencyRate = receipt.getExchangeRateForOpeningTransaction();
            }
            JSONArray jArr = new JSONArray();
            String customerId = "";
            String accountId = "";
            String linkedNotesids = "";
            String linkedNotesnos = "";
            for (int k = 0; k < noteArray.length(); k++) {//creating a hash map with payment and their linked invoice
                JSONObject jSONObject = noteArray.getJSONObject(k);
                if (jSONObject.optDouble("linkamount", 0) != 0) {
                    String noteId = jSONObject.optString("documentid", "");
                    double invAmount = jSONObject.optDouble("linkamount", 0);
                    double exchangeratefortransaction = jSONObject.optDouble("exchangeratefortransaction", 1);
                    KwlReturnObject dnresult = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), noteId);
                    DebitNote dn = (DebitNote) dnresult.getEntityList().get(0);
                    if (linkedNotesids.equals("") && dn != null) {
                        linkedNotesids += dn.getID();
                        linkedNotesnos += dn.getDebitNoteNumber();
                    } else if (dn != null) {
                        linkedNotesids += "," + dn.getID();
                        linkedNotesnos += "," + dn.getDebitNoteNumber();
                    }
                    customerId = dn.getCustomer().getID();
                    accountId = dn.getCustomer().getAccount().getID();
                    JSONObject jobj = new JSONObject();
                    jobj.put("enteramount", invAmount);
                    jobj.put("documentid", noteId);
                    jobj.put("isopeningBalancePayment", isopeningBalancePayment);
                    jobj.put("isConversionRateFromCurrencyToBase", receipt.isConversionRateFromCurrencyToBase());
                    jobj.put("exchangeratefortransaction", exchangeratefortransaction);
                    jArr.put(jobj);
                    
                    /*Method is used to save linking informatio of Advance Received Payment 
                    *
                     when linking with Debit Note */
                    
                    saveLinkingInformationOfPaymentWithDN(dn, receipt, receipt.getReceiptNumber());
                }
            }
            HashMap<String, Object> paymenthm = new HashMap<String, Object>();
            HashSet linkDetails = saveLinkedReceiptToDebitNoteDetails(requestObj, receipt, company, jArr, Constants.AdvancePayment,counterMap);
            paymenthm.put("receiptid", receipt.getID());
            paymenthm.put("linkWithDebitNoteDetails", linkDetails);
            paymenthm.put("customerId", customerId);
            accReceiptDAOobj.saveReceipt(paymenthm);
            String userFullName = requestObj.optString("userfullname");
            HashMap<String, Object> auditRequestParams = new HashMap<>();
            auditRequestParams.put("userid", requestObj.optString("userid"));
            auditRequestParams.put("reqHeader", requestObj.optString("x-real-ip"));
            auditRequestParams.put("remoteAddress", requestObj.optString("remoteaddr"));
            auditTrailObj.insertAuditLog(AuditAction.LINKEDRECEIPT, "User " + userFullName + " has linked Receipt " + receiptno + " to Debit Note(s) " + linkedNotesnos, auditRequestParams, linkedNotesids);
            
        } catch (Exception ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return result;
    }
    
    private HashSet saveLinkedReceiptToDebitNoteDetails(JSONObject requestObj, Receipt receipt, Company company, JSONArray jArr, int type, Map<String,Object> counterMap) throws JSONException, ServiceException, AccountingException, SessionExpiredException, ParseException {
        HashSet linkdetails = new HashSet();
        String companyid = company.getCompanyID();
        Date maxLinkingDate = null;
        String linkingdate = requestObj.optString("linkingdateString");
        DateFormat df = authHandler.getDateOnlyFormat();
        if (!StringUtil.isNullOrEmpty(linkingdate)) {
            maxLinkingDate = df.parse(linkingdate);
        }

        KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), company.getCompanyID());
        CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
        if (type == Constants.AdvancePayment) {
            String baseCurrency = requestObj.optString("baseCurrency");
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                LinkDetailReceiptToDebitNote rd = new LinkDetailReceiptToDebitNote();
                rd.setSrno(i + 1);
                rd.setID(StringUtil.generateUUID());
                double amountReceived = jobj.getDouble("enteramount");// amount in receipt currency
                double amountReceivedConverted = jobj.getDouble("enteramount"); // amount in invoice currency
                rd.setAmount(jobj.getDouble("enteramount"));
                rd.setCompany(company);
                KwlReturnObject result = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), jobj.getString("documentid"));
                DebitNote dn = (DebitNote) result.getEntityList().get(0);
                rd.setDebitnote((DebitNote) result.getEntityList().get(0));
                if (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString()) && !StringUtil.isNullOrEmpty(dn.getCurrency().getCurrencyID()) && !dn.getCurrency().getCurrencyID().equals(receipt.getCurrency().getCurrencyID())) {
                    rd.setExchangeRateForTransaction(Double.parseDouble(jobj.get("exchangeratefortransaction").toString()));
                    rd.setFromCurrency(dn.getCurrency());
                    rd.setToCurrency(receipt.getCurrency());
                    amountReceivedConverted = amountReceived / Double.parseDouble(jobj.get("exchangeratefortransaction").toString());
                    amountReceivedConverted = authHandler.round(amountReceivedConverted, companyid);
                    rd.setAmountInDNCurrency(amountReceivedConverted);
                } else {
                    rd.setExchangeRateForTransaction(Double.parseDouble(jobj.get("exchangeratefortransaction").toString()));
                    rd.setFromCurrency(dn.getCurrency());
                    rd.setToCurrency(receipt.getCurrency());
                    amountReceivedConverted = authHandler.round(amountReceived, companyid);
                    rd.setAmountInDNCurrency(amountReceivedConverted);
                }
                rd.setReceipt(receipt);

                
                //JE For Debit which is Linked to Receipt
                double exchangeRateforTransaction = jobj.optDouble("exchangeratefortransaction", 1.0);
                if (dn != null) {
                    double finalAmountReval = ReevalJournalEntryForOpeningDebiteNote(requestObj, dn, amountReceived, exchangeRateforTransaction);
                    if (finalAmountReval != 0) {
                        String basecurrency = requestObj.optString("baseCurrency");
                        /**
                         * added transactionID and transactionModuleID to
                         * Realised JE.
                         */
                        counterMap.put("transactionModuleid", dn.isIsOpeningBalenceDN() ? (dn.isdNForVendor() ? Constants.Acc_opening_Vendor_DebitNote : Constants.Acc_opening_Customer_DebitNote) : Constants.Acc_Debit_Note_ModuleId);
                        counterMap.put("transactionId", dn.getID());
                        String revaljeid = PostJEFORReevaluation(requestObj, finalAmountReval, company.getCompanyID(), preferences, basecurrency, null,counterMap );
                        rd.setRevalJeId(revaljeid);
                    }
                }
                //JE For Receipt which is of Opening Type
                if (receipt != null &&  (receipt.isIsOpeningBalenceReceipt() || (!receipt.getReceiptAdvanceDetails().isEmpty()))) {
                    String basecurrency = requestObj.optString("baseCurrency");
                    double finalAmountReval = ReevalJournalEntryForOpeningReceipt(requestObj, receipt, amountReceived, exchangeRateforTransaction);
                    if (finalAmountReval != 0) {
                        /**
                         * added transactionID and transactionModuleID to
                         * Realised JE.
                         */
                        counterMap.put("transactionModuleid", receipt.isIsOpeningBalenceReceipt() ? Constants.Acc_opening_Receipt : Constants.Acc_Receive_Payment_ModuleId);
                        counterMap.put("transactionId", receipt.getID());
                        String revaljeid = PostJEFORReevaluation(requestObj, -(finalAmountReval), receipt.getCompany().getCompanyID(), preferences, basecurrency, null,counterMap);
                        rd.setRevalJeIdReceipt(revaljeid);
                    }
                }
                Date linkingDate = new Date();
//                Date dnDate = dn.isIsOpeningBalenceDN() ? dn.getCreationDate() : dn.getJournalEntry().getEntryDate();
//                Date receiptDate = receipt.isIsOpeningBalenceReceipt() ? receipt.getCreationDate() : receipt.getJournalEntry().getEntryDate();
                Date dnDate = dn.getCreationDate();
                Date receiptDate = receipt.getCreationDate();
                Date maxDate = null;
                if (maxLinkingDate != null) {
                    maxDate = maxLinkingDate;
                } else {
                    List<Date> datelist = new ArrayList<Date>();
                    datelist.add(linkingDate);
                    datelist.add(receiptDate);
                    datelist.add(dnDate);
                    Collections.sort(datelist); //Sort the Date object & get the dates in ASC order. Pick-up the last record as Max Date
                    maxDate = datelist.get(datelist.size() - 1);
//                    maxDate = Math.max(Math.max(linkingDate.getTime(), dnDate.getTime()), receiptDate.getTime());
                }
                rd.setReceiptLinkDate(maxDate);
                HashMap<String, JSONArray> jcustomarrayMap = receipt.getJcustomarrayMap();

                double amountReceivedConvertedInBaseCurrency = 0d;
                HashMap<String, Object> requestParams = new HashMap();
                requestParams.put(Constants.companyid, company.getCompanyID());
                requestParams.put("gcurrencyid", company.getCurrency().getCurrencyID());
                double externalCurrencyRate = 1d;
                boolean isopeningBalanceRCP = receipt.isIsOpeningBalenceReceipt();
                Date rcpCreationDate = null;
                rcpCreationDate = receipt.getCreationDate();
                if (isopeningBalanceRCP) {
                    externalCurrencyRate = receipt.getExchangeRateForOpeningTransaction();
                } else {
//                    rcpCreationDate = receipt.getJournalEntry().getEntryDate();
                    externalCurrencyRate = receipt.getJournalEntry().getExternalCurrencyRate();
                }
                String fromcurrencyid = receipt.getCurrency().getCurrencyID();
                KwlReturnObject bAmt = null;
                if (isopeningBalanceRCP && receipt.isConversionRateFromCurrencyToBase()) {// if Receipt is opening balance Receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountReceived, fromcurrencyid, rcpCreationDate, externalCurrencyRate);
                } else {
                    bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, amountReceived, fromcurrencyid, rcpCreationDate, externalCurrencyRate);
                }
                amountReceivedConvertedInBaseCurrency = (Double) bAmt.getEntityList().get(0);
                amountReceivedConvertedInBaseCurrency = authHandler.round(amountReceivedConvertedInBaseCurrency, companyid);

                double dnExternalCurrencyRate = 1d;
                boolean isopeningBalanceDN = dn.isIsOpeningBalenceDN();
                Date dnCreationDate = null;
                dnCreationDate = dn.getCreationDate();
                if (isopeningBalanceDN) {
                    dnExternalCurrencyRate = dn.getExchangeRateForOpeningTransaction();
                } else {
//                    dnCreationDate = dn.getJournalEntry().getEntryDate();
                    dnExternalCurrencyRate = dn.getJournalEntry().getExternalCurrencyRate();
                }
                String dnfromcurrencyid = dn.getCurrency().getCurrencyID();
                KwlReturnObject dnbAmt = null;
                if (isopeningBalanceDN && dn.isConversionRateFromCurrencyToBase()) {// if Receipt is opening balance Receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                    dnbAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountReceivedConverted, dnfromcurrencyid, dnCreationDate, dnExternalCurrencyRate);
                } else {
                    dnbAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, amountReceivedConverted, dnfromcurrencyid, dnCreationDate, dnExternalCurrencyRate);
                }
                double dnamountReceivedConvertedInBaseCurrency = (Double) dnbAmt.getEntityList().get(0);
                dnamountReceivedConvertedInBaseCurrency = authHandler.round(dnamountReceivedConvertedInBaseCurrency, companyid);

                KwlReturnObject cnjedresult = accReceiptDAOobj.updateDnAmount(dn.getID(), amountReceivedConverted);
                KwlReturnObject opencnjedresult = accReceiptDAOobj.updateDnOpeningAmountDue(dn.getID(), amountReceivedConverted);
                KwlReturnObject openingDnBaseAmtDueResult = accReceiptDAOobj.updateDnOpeningBaseAmountDue(dn.getID(), dnamountReceivedConvertedInBaseCurrency);

                /*
                 * Start gains/loss calculation Calculate Gains/Loss if Invoice
                 * exchange rate changed at the time of linking with advance
                 * payment
                 */
//                KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
//                CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
                
                if (isopeningBalanceRCP && receipt.isConversionRateFromCurrencyToBase()) {
                    externalCurrencyRate = 1 / externalCurrencyRate;
                }
                Locale locale = (Locale) requestObj.opt("locale");
                if (preferences.getForeignexchange() == null) {
                    throw new AccountingException(messageSource.getMessage("acc.common.forex", null, locale));
                }
                double amountDiff = checkFxGainLossOnLinkdebitNote(dn, Double.parseDouble(jobj.optString("exchangeratefortransaction", "1")), externalCurrencyRate, amountReceived, receipt.getCurrency().getCurrencyID(), baseCurrency, company.getCompanyID());
                if (amountDiff != 0 && preferences.getForeignexchange() != null && Math.abs(amountDiff) >= 0.000001) {//Math.abs(amountDiff) < .0000001 Added this because in case of revaluation the diff in the rates was less than .0000001 and the amount calculation was going in Exachange rate account. 
                    boolean rateDecreased = false;
                    if (amountDiff < 0) {
                        rateDecreased = true;
                    }
                    JournalEntry journalEntry = null;
//                    ObjectMapper mapper = new ObjectMapper();
//                    Map<String, Object> jeDataMap = null;
//                    try {
//                        jeDataMap = mapper.readValue(requestObj.opt("globalparams").toString(), Map.class);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    Map<String, Object> jeDataMap = StringUtil.jsonToMap(requestObj.optJSONObject("globalparams"));
                    Map<String, Object> jeDataMap = AccountingManager.getGlobalParamsJson(requestObj.optJSONObject("paramJobj"));
                    int counter = 0;
                    if(counterMap.containsKey("counter")){
                       counter=(Integer) counterMap.get("counter");
                    }
                    String jeentryNumber = null;
                    String jeDatePrefix = "";
                    String jeDateAfterPrefix = "";
                    String jeDateSuffix = "";
                    boolean jeautogenflag = false;
                    String jeSeqFormatId = "";
                    Date entryDate = maxDate;
                    synchronized (this) {
                        HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                        JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                        JEFormatParams.put("modulename", "autojournalentry");
                        JEFormatParams.put("companyid", companyid);
                        JEFormatParams.put("isdefaultFormat", true);

                        KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                        SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, entryDate);
                        String nextAutoNoTemp = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                        int sequence = Integer.parseInt((String)seqNumberMap.get(Constants.SEQNUMBER));
                        jeDatePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                        jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                        jeDateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part

                        sequence = sequence + counter;
                        String number = "" + sequence;
                        String action = "" + (sequence - counter);
                        nextAutoNoTemp.replaceAll(action, number);
                        jeentryNumber = nextAutoNoTemp.replaceAll(action, number);  //next auto generated number
                        jeSeqFormatId = format.getID();
                        jeautogenflag = true;
                        counter++;
                        counterMap.put("counter", counter);
                        jeDataMap.put("entrynumber", jeentryNumber);
                        jeDataMap.put("autogenerated", jeautogenflag);
                        jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                        jeDataMap.put(Constants.SEQNUMBER, number);
                        jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
                        jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
                        jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
                    }
                    jeDataMap.put("entrydate", entryDate);
                    jeDataMap.put("companyid", companyid);
                    jeDataMap.put("memo", "Exchange Gains/Loss posted against Advance Receipt '" + receipt.getReceiptNumber() + "' linked to Debit Note '" + dn.getDebitNoteNumber() + "'");
                    jeDataMap.put("currencyid", receipt.getCurrency().getCurrencyID());
                    jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
                    jeDataMap.put("isexchangegainslossje", true);
                    jeDataMap.put("transactionId", receipt.getID());
                    jeDataMap.put("transactionModuleid", Constants.Acc_Receive_Payment_ModuleId);
                    journalEntry = accJournalEntryobj.getJournalEntry(jeDataMap);
                    accJournalEntryobj.saveJournalEntryByObject(journalEntry);

                    boolean isDebit = rateDecreased ? true : false;
                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", 1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", rateDecreased ? (-1 * amountDiff) : amountDiff);
                    jedjson.put("accountid", preferences.getForeignexchange().getID());
                    jedjson.put("debit", isDebit);
                    jedjson.put("jeid", journalEntry.getID());
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    Set<JournalEntryDetail> detail = new HashSet();
                    detail.add(jed);

                    jedjson = new JSONObject();
                    jedjson.put("srno", 2);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", rateDecreased ? (-1 * amountDiff) : amountDiff);
                    jedjson.put("accountid", dn.getAccount() == null ? dn.getCustomer().getAccount().getID() : dn.getAccount().getID());
                    jedjson.put("debit", !isDebit);
                    jedjson.put("jeid", journalEntry.getID());
                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    detail.add(jed);
                    journalEntry.setDetails(detail);
                    accJournalEntryobj.saveJournalEntryDetailsSet(detail);

                    rd.setLinkedGainLossJE(journalEntry.getID());
                }
                // End Gains/Loss Calculation

                for (ReceiptAdvanceDetail advanceDetail : receipt.getReceiptAdvanceDetails()) {
//                    advanceDetail.setAmountDue(advanceDetail.getAmountDue() - amountReceived);
                    advanceDetail.setAmountDue(authHandler.round(advanceDetail.getAmountDue()-amountReceived,company.getCompanyID()));
                }
                if (receipt != null && isopeningBalanceRCP) {
                    double receiptAmountDue = receipt.getOpeningBalanceAmountDue();
                    receiptAmountDue -= amountReceived;
                    receipt.setOpeningBalanceAmountDue(receiptAmountDue);
                    receipt.setOpeningBalanceBaseAmountDue(receipt.getOpeningBalanceBaseAmountDue() - amountReceivedConvertedInBaseCurrency);

                    HashMap<String, Object> receipthm = new HashMap<String, Object>();
                    receipthm.put("receiptid", receipt.getID());
                    receipthm.put("openingBalanceAmountDue", receipt.getOpeningBalanceAmountDue());
                    receipthm.put(Constants.openingBalanceBaseAmountDue, receipt.getOpeningBalanceBaseAmountDue());
                    accReceiptDAOobj.saveReceipt(receipthm);
                }
                linkdetails.add(rd);
            }
        }
        return linkdetails;
    }
    
    public double checkFxGainLossOnLinkdebitNote(DebitNote dn, double newInvoiceExchageRate, double paymentExchangeRate, double receivedDnAmount, String paymentCurrency, String baseCurrency, String companyid) throws ServiceException {
        double amount = 0;
        HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
        GlobalParams.put("companyid", companyid);
        GlobalParams.put("gcurrencyid", baseCurrency);
        double dnExchangeRate = 0d;
        Date dnCreationDate = null;
        boolean revalFlag = false;
        boolean isopeningBalanceDn = dn.isIsOpeningBalenceDN();
        if (dn.isNormalDN()) {
            dnExchangeRate = dn.getJournalEntry().getExternalCurrencyRate();
//            dnCreationDate = dn.getJournalEntry().getEntryDate();
        } else {
            dnExchangeRate = dn.getExchangeRateForOpeningTransaction();
             if(isopeningBalanceDn && dn.isConversionRateFromCurrencyToBase()){//converting rate to Base to Other Currency Rate
                dnExchangeRate=1/dnExchangeRate;
            }
        }
        dnCreationDate = dn.getCreationDate();
        
        Map<String, Object> documentMap = new HashMap<>();
        documentMap.put("invoiceid", dn.getID());
        documentMap.put("companyid", companyid);
        KwlReturnObject result = accJournalEntryobj.getRevalInvoiceId(documentMap);
        RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
        if (history != null) {
            dnExchangeRate = history.getEvalrate();
            revalFlag=true;
        }
        String currid = dn.getCurrency().getCurrencyID();
        KwlReturnObject bAmt = null;
        if (currid.equalsIgnoreCase(paymentCurrency)) {
//            if (isopeningBalanceDn && dn.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
//                bAmt = accCurrencyobj.getOneCurrencyToOtherAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, paymentCurrency, dnCreationDate, dnExchangeRate);
//            } else {
                double paymentExternalCurrencyRate = paymentExchangeRate;
                if (dnExchangeRate != paymentExternalCurrencyRate) {
                    bAmt = accCurrencyobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, paymentCurrency, dnCreationDate, dnExchangeRate, paymentExternalCurrencyRate);
                } else {
                    bAmt = accCurrencyobj.getOneCurrencyToOther(GlobalParams, 1.0, currid, paymentCurrency, dnCreationDate, dnExchangeRate);
                }
//            }
        } else {
//            if (isopeningBalanceDn && dn.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
//                bAmt = accCurrencyobj.getOneCurrencyToOtherModifiedAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, paymentCurrency, dnCreationDate, dnExchangeRate);
//            } else {
                double paymentExternalCurrencyRate = paymentExchangeRate;
                if (dnExchangeRate != paymentExternalCurrencyRate) {
                    bAmt = accCurrencyobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, paymentCurrency, dnCreationDate, dnExchangeRate, paymentExternalCurrencyRate);
                } else {
                    bAmt = accCurrencyobj.getOneCurrencyToOtherModified(GlobalParams, 1.0, currid, paymentCurrency, dnCreationDate, dnExchangeRate);
                }
//            }
        }
        double oldrate = (Double) bAmt.getEntityList().get(0);
        double newrate = 0.0;
        double ratio = 0;
        if (newInvoiceExchageRate != oldrate && newInvoiceExchageRate != 0.0 && Math.abs(newInvoiceExchageRate - oldrate) >= 0.000001) {
            newrate = newInvoiceExchageRate;
            ratio = oldrate - newrate;
            amount = (receivedDnAmount - (receivedDnAmount / newrate) * oldrate);
        }
        return amount;
    }
    public ModelAndView unlinkReceiptDocuments(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            // for refund reciept type transaction
            boolean isRefundTransaction = (!StringUtil.isNullOrEmpty(request.getParameter("isRefundTransaction"))) ? Boolean.parseBoolean(request.getParameter("isRefundTransaction")) : false;
            List li = unlinkReceiptFromTransactions(request);
            issuccess = true;
            if (isRefundTransaction) {
                msg = messageSource.getMessage("acc.field.paymentInformationHasBeenUnLinkedfromAdvancePaymentSuccessfully", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.field.PaymentinformationhasbeenUnLinkedtoCustomerInvoiceAndDnsuccessfully", null, RequestContextUtils.getLocale(request));
            }
            txnManager.commit(status);
        } catch (AccountingException ex){
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public List unlinkReceiptFromTransactions(HttpServletRequest request) throws ServiceException, SessionExpiredException,AccountingException,JSONException {
        List result = new ArrayList();
        try {
            //Unlink Invoices
            result = unlinkReceiptNew(request);
            //Unlink Debit Notes
            result = unlinkReceiptFromDebitNote(request);
            // Unlink Advance Payments
            result = unlinkReceiptFromAdvancePayment(request);
        }  catch (AccountingException e){
            throw new AccountingException(e.getMessage());
        }catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } 
        return result;
    }
    
    public List unlinkReceiptFromDebitNote(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        List result = new ArrayList();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String paymentid = request.getParameter("paymentid");
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

            KwlReturnObject receiptKWLObj = accountingHandlerDAOobj.getObject(Receipt.class.getName(), paymentid);
            Receipt receipt = (Receipt) receiptKWLObj.getEntityList().get(0);
            String paymentno = receipt.getReceiptNumber();

            String linkedDnids = "";
            String linkedDnnos = "";
            List<String> linkedDetailDn = new ArrayList();
            JSONArray linkJSONArray = request.getParameter("linkdetails") != null ? new JSONArray(request.getParameter("linkdetails")) : new JSONArray();
            for (int k = 0; k < linkJSONArray.length(); k++) {//creating a hash map with payment and their linked invoice
                JSONObject jSONObject = linkJSONArray.getJSONObject(k);
                String linkId = jSONObject.optString("linkdetailid", "");
                linkedDetailDn.add(linkId);
            }

            double receiptexternalCurrencyRate = 1d;
            boolean isopeningBalanceRCP = receipt.isIsOpeningBalenceReceipt();
            Date rcpCreationDate = null;
            rcpCreationDate = receipt.getCreationDate();
            if (isopeningBalanceRCP) {
                receiptexternalCurrencyRate = receipt.getExchangeRateForOpeningTransaction();
            } else {
//                rcpCreationDate = receipt.getJournalEntry().getEntryDate();
                receiptexternalCurrencyRate = receipt.getJournalEntry().getExternalCurrencyRate();
            }
            String fromcurrencyid = receipt.getCurrency().getCurrencyID();

            String linkedDetailIDs = "";
            for (String dnID : linkedDetailDn) {
                linkedDetailIDs = linkedDetailIDs.concat("'").concat(dnID).concat("',");
            }
            if (!StringUtil.isNullOrEmpty(linkedDetailIDs.toString())) {
                linkedDetailIDs = linkedDetailIDs.substring(0, linkedDetailIDs.length() - 1);
            }
            double sumOfTotalAmount = 0;
            List<DebitNote> DNList = new ArrayList<DebitNote>();
            List<LinkDetailReceiptToDebitNote> details = accReceiptDAOobj.getDeletedLinkedReceiptDebitNotes(receipt, linkedDetailIDs, companyid);
            for (LinkDetailReceiptToDebitNote receiptDetail : details) {
                DebitNote DN = receiptDetail.getDebitnote();
                double amountdue = DN.getDnamountdue();
                DN.setDnamountdue(amountdue + receiptDetail.getAmountInDNCurrency());
                double externalCurrencyRateForLinking = 1d;
                externalCurrencyRateForLinking = receiptDetail.getExchangeRateForTransaction();
                sumOfTotalAmount += receiptDetail.getAmount();
                HashMap<String, Object> requestParams = new HashMap();
                requestParams.put(Constants.companyid, companyid);
                requestParams.put("gcurrencyid", company.getCurrency().getCurrencyID());
                KwlReturnObject bAmt = null;
                if (isopeningBalanceRCP && receipt.isConversionRateFromCurrencyToBase()) {// if Receipt is opening balance Receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, receiptDetail.getAmount(), fromcurrencyid, rcpCreationDate, externalCurrencyRateForLinking);
                } else {
                    bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, receiptDetail.getAmount(), fromcurrencyid, rcpCreationDate, externalCurrencyRateForLinking);
                }
                double amountReceivedConvertedInDNBaseCurrency = (Double) bAmt.getEntityList().get(0);

                DN.setOpeningBalanceAmountDue(DN.getOpeningBalanceAmountDue() + receiptDetail.getAmountInDNCurrency());
                DN.setOpeningBalanceBaseAmountDue(DN.getOpeningBalanceBaseAmountDue() + amountReceivedConvertedInDNBaseCurrency);
                linkedDnnos = linkedDnnos.concat(DN.getDebitNoteNumber()).concat(",");
                DNList.add(DN);

                // check if Forex/Gain Loss JE generated. If yes then need to delete JE too
                if (receiptDetail.getLinkedGainLossJE() != null && !receiptDetail.getLinkedGainLossJE().isEmpty()) {
                    deleteJEArray(receiptDetail.getLinkedGainLossJE(), companyid);
                }
                if (receiptDetail != null && !StringUtil.isNullOrEmpty(receiptDetail.getRevalJeId()) ) {
                    accJournalEntryobj.deleteJEDtails(receiptDetail.getRevalJeId(), companyid);
                    accJournalEntryobj.deleteJE(receiptDetail.getRevalJeId(), companyid);
                }
                if (receiptDetail != null && !StringUtil.isNullOrEmpty(receiptDetail.getRevalJeIdReceipt()) ) {
                    accJournalEntryobj.deleteJEDtails(receiptDetail.getRevalJeIdReceipt(), companyid);
                    accJournalEntryobj.deleteJE(receiptDetail.getRevalJeIdReceipt(), companyid);
                }
                
                /* Deleting Linking iformation of Received Payment from Linking table if it is unlinked*/
                HashMap<String, Object> linkingrequestParams = new HashMap<String, Object>();
                linkingrequestParams.put("receiptid", receipt.getID());
                linkingrequestParams.put("linkedTransactionID", receiptDetail.getDebitnote().getID());
                linkingrequestParams.put("unlinkflag", true);
                accReceiptDAOobj.deleteLinkingInformationOfRP(linkingrequestParams);

            }

            if (sumOfTotalAmount != 0 && receipt.getReceiptAdvanceDetails() != null && !receipt.getReceiptAdvanceDetails().isEmpty()) {
                for (ReceiptAdvanceDetail advanceDetail : receipt.getReceiptAdvanceDetails()) {
                    double linkedAmountDue = advanceDetail.getAmountDue();
                    advanceDetail.setAmountDue(linkedAmountDue + sumOfTotalAmount);
                    List<Object> objectList = new ArrayList<Object>();
                    objectList.add((Object) advanceDetail);
                    accAccountDAOobj.saveOrUpdateAll(objectList);
                }
            } else if (sumOfTotalAmount != 0 && isopeningBalanceRCP) {
                HashMap<String, Object> requestParams = new HashMap();
                requestParams.put(Constants.companyid, companyid);
                requestParams.put("gcurrencyid", company.getCurrency().getCurrencyID());
                KwlReturnObject bAmt = null;
                if (isopeningBalanceRCP && receipt.isConversionRateFromCurrencyToBase()) {// if Receipt is opening balance Receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, sumOfTotalAmount, fromcurrencyid, rcpCreationDate, receiptexternalCurrencyRate);
                } else {
                    bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, sumOfTotalAmount, fromcurrencyid, rcpCreationDate, receiptexternalCurrencyRate);
                }
                double amountPaymentConvertedInBaseCurrency = (Double) bAmt.getEntityList().get(0);
                receipt.setOpeningBalanceAmountDue(sumOfTotalAmount + receipt.getOpeningBalanceAmountDue());
                receipt.setOpeningBalanceBaseAmountDue(amountPaymentConvertedInBaseCurrency + receipt.getOpeningBalanceBaseAmountDue());
                List<Object> objectList = new ArrayList<Object>();
                objectList.add((Object) receipt);
                accAccountDAOobj.saveOrUpdateAll(objectList);
            }
            if (!DNList.isEmpty()) {
                List<Object> objectList = new ArrayList<Object>(DNList);
                accAccountDAOobj.saveOrUpdateAll(objectList);
            }
            if (!StringUtil.isNullOrEmpty(linkedDnnos)) {
                linkedDnnos = linkedDnnos.substring(0, linkedDnnos.length() - 1);
            }
            accReceiptDAOobj.deleteSelectedLinkedReceiptDebitNotes(receipt.getID(), linkedDetailIDs, companyid);
            if (!StringUtil.isNullOrEmpty(linkedDnnos)) {
                auditTrailObj.insertAuditLog(AuditAction.LINKEDRECEIPT, "User " + sessionHandlerImpl.getUserFullName(request) + " has unlinked Receipt " + paymentno + " from " + linkedDnnos, request, linkedDnids);
            }
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    public HashSet saveReceiptDetailLoanObject(HttpServletRequest request, JSONArray jSONArrayLoanDisbursement,Receipt receipt) throws SessionExpiredException, ServiceException, AccountingException {
        List list = new ArrayList();
        HashSet payDetails = null;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String methodid = request.getParameter("pmtmethod");
            sessionHandlerImpl.updatePaymentMethodID(request, methodid);
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);
            JSONArray jArr = new JSONArray();
            if (jSONArrayLoanDisbursement != null) {
                jArr = jSONArrayLoanDisbursement;
            }
            payDetails = saveReceiptDetailsLoan(receipt, company, jArr);
        } catch (SessionExpiredException | ServiceException | JSONException | UnsupportedEncodingException e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return payDetails;
    }
    
    private HashSet saveReceiptDetailsLoan(Receipt receipt, Company company, JSONArray jArr) throws JSONException, ServiceException, UnsupportedEncodingException {
        HashSet details = new HashSet();
        String companyid = company.getCompanyID();
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);
            ReceiptDetailLoan rd = new ReceiptDetailLoan();
            rd.setSrno(i + 1);
            rd.setID(StringUtil.generateUUID());
            double amountReceived = jobj.getDouble("enteramount");// amount in receipt currency
            double amountReceivedConverted = jobj.getDouble("enteramount"); // amount in loan disbursement currency
            rd.setAmount(jobj.getDouble("enteramount"));
            rd.setCompany(company);
            KwlReturnObject result = accountingHandlerDAOobj.getObject(RepaymentDetails.class.getName(), jobj.getString("repaymentscheduleid"));
            RepaymentDetails RepaymentDetailObject = (RepaymentDetails) result.getEntityList().get(0);
            double repaymentDetailAmountDue = RepaymentDetailObject.getAmountdue();
            KWLCurrency repaymentDetailCurrency = RepaymentDetailObject.getDisbursement().getCurrency();
            String repaymentDetailCurrencyId = repaymentDetailCurrency.getCurrencyID();
            if (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString()) && !StringUtil.isNullOrEmpty(repaymentDetailCurrencyId) && !repaymentDetailCurrencyId.equals(receipt.getCurrency().getCurrencyID())) {
                double adjustedRate = jobj.optDouble("amountdue", 0) / jobj.optDouble("amountDueOriginal", 0);
                amountReceivedConverted = amountReceived / adjustedRate;
            }
            rd.setExchangeRateForTransaction(Double.parseDouble(jobj.get("exchangeratefortransaction").toString()));
            rd.setFromCurrency(repaymentDetailCurrency);
            rd.setToCurrency(receipt.getCurrency());
            amountReceivedConverted = authHandler.round(amountReceivedConverted, companyid);
            rd.setAmountInRepaymentDetailCurrency(amountReceivedConverted);
            rd.setGstCurrencyRate(jobj.optDouble("gstCurrencyRate", 0.0));
            rd.setDescription(StringUtil.DecodeText(jobj.optString("description")));
            rd.setReceipt(receipt);
            rd.setRepaymentDetail(RepaymentDetailObject);
            if (jobj.has("rowjedid")) {
                rd.setROWJEDID(jobj.getString("rowjedid"));
            }
            if (jobj.has("srNoForRow")) {
                int srNoForRow = StringUtil.isNullOrEmpty("srNoForRow") ? 0 : Integer.parseInt(jobj.getString("srNoForRow"));
                rd.setSrNoForRow(srNoForRow);
            }
            if (jobj.has("jedetail") && jobj.get("jedetail") != null) {
                KwlReturnObject resJED = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), (String) jobj.get("jedetail"));
                JournalEntryDetail jedObj = (JournalEntryDetail) resJED.getEntityList().get(0);
                rd.setTotalJED(jedObj);
            }
            
            double amountReceivedConvertedInBaseCurrency = 0d;
            HashMap<String, Object> requestParams = new HashMap();
            requestParams.put(Constants.companyid, company.getCompanyID());
            requestParams.put("gcurrencyid", company.getCurrency().getCurrencyID());
            double DisbursementExternalCurrencyRate = RepaymentDetailObject.getDisbursement().getJournalEntry().getExternalCurrencyRate();
            Date DisbursementCreationDate = RepaymentDetailObject.getDisbursement().getJournalEntry().getEntryDate();
            
            String fromcurrencyid = repaymentDetailCurrencyId;
            KwlReturnObject bAmt = null;
            bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, amountReceivedConverted, fromcurrencyid, DisbursementCreationDate, DisbursementExternalCurrencyRate);
            amountReceivedConvertedInBaseCurrency = (Double) bAmt.getEntityList().get(0);
            amountReceivedConvertedInBaseCurrency = authHandler.round(amountReceivedConvertedInBaseCurrency, companyid);
            rd.setAmountInBaseCurrency(amountReceivedConvertedInBaseCurrency);
            
            RepaymentDetailObject.setAmountdue(repaymentDetailAmountDue-amountReceivedConverted);
            RepaymentDetailObject.setPaymentStatus(PaymentStatus.Paid);
            if (receipt != null) {
                double receiptAmountDue = receipt.getOpeningBalanceAmountDue();
                receiptAmountDue -= amountReceived;
                receipt.setOpeningBalanceAmountDue(receiptAmountDue);
                receipt.setOpeningBalanceBaseAmountDue(receipt.getOpeningBalanceBaseAmountDue() - amountReceivedConvertedInBaseCurrency);
            }
            details.add(rd);
        }
        return details;
    }
    
    public double LoanRepaymentForexGailLossAmount(HttpServletRequest request,JSONObject jobj, Receipt receipt ,String transactionCurrencyId, String currencyid, double externalCurrencyRate) throws ServiceException, SessionExpiredException {
        String companyid = sessionHandlerImpl.getCompanyid(request);
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("gcurrencyid",sessionHandlerImpl.getCurrencyID(request));
        requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
        requestParams.put("dateformat", authHandler.getDateOnlyFormatter(request));
        double enterAmountPaymentCurrencyOld=0;
        double enterAmountTrancastionCurrencyNew=0;
        double amountdiff=0;
        try {
            
            Double enteramount = jobj.getDouble("enteramount");
            String documentId = jobj.getString("repaymentscheduleid");
            KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(RepaymentDetails.class.getName(), documentId);
            RepaymentDetails RD = (RepaymentDetails) resultObject.getEntityList().get(0);
            JournalEntry je = RD.getDisbursement().getJournalEntry();
            Date DisbursementDate = je.getEntryDate();
            externalCurrencyRate = je.getExternalCurrencyRate();
            
            if (!StringUtil.isNullOrEmpty(transactionCurrencyId) && !StringUtil.isNullOrEmpty(transactionCurrencyId) && !transactionCurrencyId.equals(currencyid)) {
                enterAmountTrancastionCurrencyNew = enteramount;
                if (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString())) {
                    double exchangeratefortransaction = Double.parseDouble(jobj.optString("exchangeratefortransaction", "1"));
                    enterAmountTrancastionCurrencyNew = enteramount / exchangeratefortransaction;
                    enterAmountTrancastionCurrencyNew = authHandler.round(enterAmountTrancastionCurrencyNew, companyid);
                    KwlReturnObject bAmtCurrencyFilter = null;
                    bAmtCurrencyFilter = accCurrencyobj.getOneCurrencyToOtherModified(requestParams, enterAmountTrancastionCurrencyNew, transactionCurrencyId, currencyid, DisbursementDate, externalCurrencyRate);
                    enterAmountPaymentCurrencyOld = authHandler.round((Double) bAmtCurrencyFilter.getEntityList().get(0), companyid);
                    amountdiff=enteramount-enterAmountPaymentCurrencyOld;
                }
            }
            
        }catch (Exception ex) {
            throw ServiceException.FAILURE("LoanRepaymentForexGailLossAmount : " + ex.getMessage(), ex);
        }
        return (amountdiff);
    }
    
    public double LoanRepaymentForexGailLossAmountSameCurrency(HttpServletRequest request, JSONObject jobj, Receipt receipt, String transactionCurrencyId, String currencyid, double externalCurrencyRate) throws ServiceException, SessionExpiredException, JSONException {
        double amount = 0, actualAmount = 0;
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
        requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
        requestParams.put("dateformat", authHandler.getDateOnlyFormatter(request));
        try {

            double exchangeRate = 0d;
            double newrate = 0.0;
            Double enteramount = jobj.getDouble("principalInPaymentCurrency");
            String documentId = jobj.getString("repaymentscheduleid");
            KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(RepaymentDetails.class.getName(), documentId);
            RepaymentDetails RD = (RepaymentDetails) resultObject.getEntityList().get(0);
            JournalEntry je = RD.getDisbursement().getJournalEntry();
            Date DisbursementDate = je.getEntryDate();
            
            double paymentExternalCurrencyRate=StringUtil.isNullOrEmpty(request.getParameter("externalcurrencyrate"))?1.0:Double.parseDouble(request.getParameter("externalcurrencyrate"));
                            
            KwlReturnObject bAmt = null;
            if(exchangeRate!=paymentExternalCurrencyRate){
                bAmt = accCurrencyobj.getOneCurrencyToOtherWithDiffRates(requestParams, 1.0, transactionCurrencyId, currencyid, DisbursementDate, exchangeRate,paymentExternalCurrencyRate);
            }else{
                bAmt = accCurrencyobj.getOneCurrencyToOther(requestParams, 1.0, transactionCurrencyId, currencyid, DisbursementDate, exchangeRate);
            }
            double oldrate = (Double) bAmt.getEntityList().get(0);
            newrate = jobj.optDouble("exchangeratefortransaction", 0.0);
            amount = (enteramount - (enteramount / newrate) * oldrate) / newrate;
            KwlReturnObject bAmtActual = accCurrencyobj.getBaseToCurrencyAmount(requestParams, amount, currencyid, DisbursementDate, newrate);
            actualAmount += (Double) bAmtActual.getEntityList().get(0);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("LoanRepaymentForexGailLossAmountSameCurrency : " + ex.getMessage(), ex);
        }
        return (actualAmount);
    }

    public void saveLinkingInformationOfPaymentWithDN(DebitNote debitnote, Receipt receipt, String paymentNo) throws ServiceException {

        try {
            /* Save Debit Note Linking & Receipt Paymnet Linking information in linking table*/
            HashMap<String, Object> requestParamsLinking = new HashMap<String, Object>();
            requestParamsLinking.put("linkeddocid", receipt.getID());
            requestParamsLinking.put("docid", debitnote.getID());
            requestParamsLinking.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
            requestParamsLinking.put("linkeddocno", paymentNo);
            requestParamsLinking.put("sourceflag", 0);
            KwlReturnObject result = accReceiptDAOobj.updateEntryInDebitNoteLinkingTable(requestParamsLinking);

            requestParamsLinking.put("linkeddocid", debitnote.getID());
            requestParamsLinking.put("docid", receipt.getID());
            requestParamsLinking.put("moduleid", Constants.Acc_Debit_Note_ModuleId);
            requestParamsLinking.put("linkeddocno", debitnote.getDebitNoteNumber());
            requestParamsLinking.put("sourceflag", 1);
            result = accReceiptDAOobj.saveReceiptLinking(requestParamsLinking);

        } catch (Exception ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
    
    public void saveLinkingInformationOfPaymentWithSalesInvoice(Invoice invoice, Receipt receipt, String paymentNo) throws ServiceException {

        try {
            /* Save Debit Note Linking & Receipt Paymnet Linking information in linking table*/
            HashMap<String, Object> requestParamsLinking = new HashMap<String, Object>();
            requestParamsLinking.put("linkeddocid", receipt.getID());
            requestParamsLinking.put("docid", invoice.getID());
            requestParamsLinking.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
            requestParamsLinking.put("linkeddocno", paymentNo);
            requestParamsLinking.put("sourceflag", 0);
            KwlReturnObject result = accInvoiceDAOObj.saveInvoiceLinking(requestParamsLinking);  

            requestParamsLinking.put("linkeddocid", invoice.getID());
            requestParamsLinking.put("docid", receipt.getID());
            requestParamsLinking.put("moduleid", Constants.Acc_Invoice_ModuleId);
            requestParamsLinking.put("linkeddocno", invoice.getInvoiceNumber());
            requestParamsLinking.put("sourceflag", 1);
            result = accReceiptDAOobj.saveReceiptLinking(requestParamsLinking);

        } catch (Exception ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
    
    /**
     * Description : Below Method is used to get Advance Payment linked to Refund Receipt
     * @param <request> to get companyid, user full name from session and getting parameter paymentid
     * @param <advancePaymentArray> getting advance payment details link to refund receipt
     * @param <counterMap> used for counter
     * @return List
     * @throws ServiceException
     * @throws SessionExpiredException 
     */
    public List linkReceiptToAdvancePayment(JSONObject requestObj, JSONArray advancePaymentArray, Map<String, Object> counterMap) throws ServiceException, SessionExpiredException, AccountingException {
        List result = new ArrayList();
        try {
            String companyid = requestObj.optString(Constants.companyid);
            String paymentid = requestObj.optString("paymentid");
            KwlReturnObject cmpResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpResult.getEntityList().get(0);

            KwlReturnObject receiptObj = accountingHandlerDAOobj.getObject(Receipt.class.getName(), paymentid);
            Receipt receipt = (Receipt) receiptObj.getEntityList().get(0);
            String receiptNo = receipt.getReceiptNumber();

            JSONArray jArr = new JSONArray();
            String linkedAdvPaymentIds = "";
            String linkedAdvPaymentNos = "";
            double currentAmtDueOfAdvDetail=0.0d;
            double linkedAmtInPaymentCurrency=0.0d;
            // creating a hash map with payment and their linked advance payment
            for (int k = 0; k < advancePaymentArray.length(); k++) {
                JSONObject jSONObject = advancePaymentArray.getJSONObject(k);
                if (jSONObject.optDouble("linkamount", 0) != 0) {
                    String advanceDetailId = jSONObject.optString("documentid", "");
                    double paymentAmount = jSONObject.optDouble("linkamount", 0);
                    double exchangeratefortransaction = jSONObject.optDouble("exchangeratefortransaction", 1);
                    KwlReturnObject paymentResult = accReceiptDAOobj.getPaymentInformationFromAdvanceDetailId(advanceDetailId, companyid);
                    Object[] paymentInfoObjArr = (Object[]) paymentResult.getEntityList().get(0);
                    currentAmtDueOfAdvDetail = (double) paymentInfoObjArr[7];
                    currentAmtDueOfAdvDetail = authHandler.round(currentAmtDueOfAdvDetail, companyid);
                    linkedAmtInPaymentCurrency = paymentAmount/exchangeratefortransaction;
                    linkedAmtInPaymentCurrency = authHandler.round(linkedAmtInPaymentCurrency, companyid);
                    if(linkedAmtInPaymentCurrency>currentAmtDueOfAdvDetail){
                        throw new AccountingException("Amount entered for payment cannot be greater than it's amount due. Please check the amount due of payment "+(String) paymentInfoObjArr[3]+" before proceeding.");
                    }
                    if (linkedAdvPaymentIds.equals("") && paymentInfoObjArr[2] != null) {
                        linkedAdvPaymentIds += (String) paymentInfoObjArr[2];
                        linkedAdvPaymentNos += (String) paymentInfoObjArr[3];
                    } else if (paymentInfoObjArr[2] != null) {
                        linkedAdvPaymentIds += "," + (String) paymentInfoObjArr[2];
                        linkedAdvPaymentNos += "," + (String) paymentInfoObjArr[3];
                    }
                    
                    JSONObject jobj = new JSONObject();
                    jobj.put("enteramount", paymentAmount);
                    jobj.put("documentid", advanceDetailId);
                    jobj.put("isConversionRateFromCurrencyToBase", receipt.isConversionRateFromCurrencyToBase());
                    jobj.put("exchangeratefortransaction", exchangeratefortransaction);
                    jArr.put(jobj);

                    // Method is used to save linking informatio of Refund Received Payment when linking with Advance Payment
                    saveLinkingInformationOfRefundPaymentWithAdvancePayment((String) paymentInfoObjArr[2], (String) paymentInfoObjArr[3], receipt);
                }
            }

            // save linked receipt to advance payment details
            HashSet linkDetails = saveLinkedReceiptToAdvancePaymentDetails(requestObj, receipt, company, jArr, counterMap);

            // update linked advance details in receipt
            HashMap<String, Object> paymenthm = new HashMap<>();
            paymenthm.put("receiptid", receipt.getID());
            paymenthm.put("linkWithAdvancePaymentDetails", linkDetails);
            accReceiptDAOobj.saveReceipt(paymenthm);
            
            // insert audit trial entry
            String userFullName = requestObj.optString("userfullname");
            HashMap<String, Object> auditRequestParams = new HashMap<>();
            auditRequestParams.put("userid", requestObj.optString("userid"));
            auditRequestParams.put("reqHeader", requestObj.optString("x-real-ip"));
            auditRequestParams.put("remoteAddress", requestObj.optString("remoteaddr"));
            auditTrailObj.insertAuditLog(AuditAction.LINKEDRECEIPT, "User " + userFullName + " has linked Receipt " + receiptNo + " to Advance Payment(s) " + linkedAdvPaymentNos, auditRequestParams, linkedAdvPaymentIds);
        } catch (AccountingException e){
            throw new AccountingException(e.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return result;
    }

    /**
     * Description : Below Method is used to save Advance Payment linked to Refund Receipt
     * @param request get currencyid, dateformat, linkingdate from HttpServletRequest
     * @param receipt get Receipt information
     * @param company get Company information
     * @param jArr get linking information
     * @param counterMap used for counter
     * @return HashSet
     * @throws JSONException
     * @throws ServiceException
     * @throws AccountingException
     * @throws SessionExpiredException
     * @throws ParseException 
     */
    private HashSet saveLinkedReceiptToAdvancePaymentDetails(JSONObject requestObj, Receipt receipt, Company company, JSONArray jArr, Map<String, Object> counterMap) throws JSONException, ServiceException, AccountingException, SessionExpiredException, ParseException {
        HashSet linkdetails = new HashSet();
        String companyid = company.getCompanyID();
        String baseCurrency = requestObj.optString("baseCurrency");
        DateFormat df = authHandler.getDateOnlyFormat();
        Date maxLinkingDate = null;
        String linkingdate = requestObj.optString("linkingdateString");
        if (!StringUtil.isNullOrEmpty(linkingdate)) {
            maxLinkingDate = df.parse(linkingdate);
        }

        // getting company preferecnes object
        KwlReturnObject capResult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
        CompanyAccountPreferences preferences = (CompanyAccountPreferences) capResult.getEntityList().get(0);

        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);
            LinkDetailReceiptToAdvancePayment rd = new LinkDetailReceiptToAdvancePayment();
            rd.setSrno(i + 1);
            rd.setID(StringUtil.generateUUID());
            double amountReceived = jobj.getDouble("enteramount"); // amount in receipt currency
            double amountReceivedConverted = jobj.getDouble("enteramount"); // amount in advance payment currency
            rd.setAmount(jobj.getDouble("enteramount"));
            rd.setCompany(company);

            KwlReturnObject paymentResult = accReceiptDAOobj.getPaymentInformationFromAdvanceDetailId(jobj.getString("documentid"), companyid);
            Object[] paymentInfoObjArr = (Object[]) paymentResult.getEntityList().get(0);

            KwlReturnObject paymentCurrencyResult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), (String) paymentInfoObjArr[4]);
            KWLCurrency paymentCurrency = (KWLCurrency) paymentCurrencyResult.getEntityList().get(0);

            rd.setPaymentId((String) paymentInfoObjArr[2]);
            if (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString()) && !StringUtil.isNullOrEmpty((String) paymentInfoObjArr[4]) && !paymentInfoObjArr[4].toString().equals(receipt.getCurrency().getCurrencyID())) {
                rd.setExchangeRateForTransaction(Double.parseDouble(jobj.get("exchangeratefortransaction").toString()));
                rd.setFromCurrency(paymentCurrency);
                rd.setToCurrency(receipt.getCurrency());
                amountReceivedConverted = amountReceived / Double.parseDouble(jobj.get("exchangeratefortransaction").toString());
                amountReceivedConverted = authHandler.round(amountReceivedConverted, companyid);
                rd.setAmountInPaymentCurrency(amountReceivedConverted);
            } else {
                rd.setExchangeRateForTransaction(Double.parseDouble(jobj.get("exchangeratefortransaction").toString()));
                rd.setFromCurrency(paymentCurrency);
                rd.setToCurrency(receipt.getCurrency());
                amountReceivedConverted = authHandler.round(amountReceived, companyid);
                rd.setAmountInPaymentCurrency(amountReceivedConverted);
            }
            rd.setReceipt(receipt);

            // JE For Advance Payment which is Linked to Receipt
            double exchangeRateforTransaction = jobj.optDouble("exchangeratefortransaction", 1.0);
            if (paymentInfoObjArr[2] != null) {
                double finalAmountReval = ReevalJournalEntryForAdvancePayment(requestObj, jobj.getString("documentid"), amountReceived, exchangeRateforTransaction);
                if (finalAmountReval != 0) {
                    /**
                     * added transactionID and transactionModuleID to Realised
                     * JE.ERP-41455.
                     */
                    counterMap.put("transactionModuleid", requestObj.optBoolean("isOpeningPayment", false) ? Constants.Acc_opening_Payment : Constants.Acc_Make_Payment_ModuleId);
                    counterMap.put("transactionId", requestObj.optString("paymentId"));
                    String revaljeid = PostJEFORReevaluation(requestObj, finalAmountReval, companyid, preferences, baseCurrency, null, counterMap);
                    rd.setRevalJeId(revaljeid);
                }
            }
            // JE For Receipt 
            if (receipt != null && (receipt.isIsOpeningBalenceReceipt() || (!receipt.getReceiptAdvanceDetails().isEmpty()))) {
                double finalAmountReval = ReevalJournalEntryForOpeningReceipt(requestObj, receipt, amountReceived, exchangeRateforTransaction);
                if (finalAmountReval != 0) {
                    /**
                     * added transactionID and transactionModuleID to Realised
                     * JE.
                     */
                    counterMap.put("transactionModuleid", receipt.isIsOpeningBalenceReceipt() ? Constants.Acc_opening_Receipt : Constants.Acc_Receive_Payment_ModuleId);
                    counterMap.put("transactionId", receipt.getID());
                    String revaljeid = PostJEFORReevaluation(requestObj, -(finalAmountReval), receipt.getCompany().getCompanyID(), preferences, baseCurrency, null, counterMap);
                    rd.setRevalJeIdReceipt(revaljeid);
                }
            }

            Date linkingDate = new Date();
            Date advPaymentDate = (Date) paymentInfoObjArr[5];
//            Date receiptDate = receipt.getJournalEntry().getEntryDate();
            Date receiptDate = receipt.getCreationDate();
            Date maxDate = null;
            if (maxLinkingDate != null) {
                maxDate = maxLinkingDate;
            } else {
                List<Date> datelist = new ArrayList<Date>();
                datelist.add(linkingDate);
                datelist.add(advPaymentDate);
                datelist.add(receiptDate);
                Collections.sort(datelist); //Sort the Date object & get the dates in ASC order. Pick-up the last record as Max Date
                maxDate = datelist.get(datelist.size() - 1);
                //maxDate = Math.max(Math.max(linkingDate.getTime(), advPaymentDate.getTime()), receiptDate.getTime());
            }
            rd.setReceiptLinkDate(maxDate);

            HashMap<String, Object> requestParams = new HashMap();
            requestParams.put(Constants.companyid, companyid);
            requestParams.put("gcurrencyid", company.getCurrency().getCurrencyID());

//            Date rcpCreationDate = receipt.getJournalEntry().getEntryDate();
            Date rcpCreationDate = receipt.getCreationDate();
            double externalCurrencyRate = receipt.getJournalEntry().getExternalCurrencyRate();
            String fromcurrencyid = receipt.getCurrency().getCurrencyID();
            KwlReturnObject bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, amountReceived, fromcurrencyid, rcpCreationDate, externalCurrencyRate);
            double amountReceivedConvertedInBaseCurrency = (Double) bAmt.getEntityList().get(0);
            amountReceivedConvertedInBaseCurrency = authHandler.round(amountReceivedConvertedInBaseCurrency, companyid);

            Date advPaymentCreationDate = (Date) paymentInfoObjArr[5];
            double advPaymentExternalCurrencyRate = (Double) paymentInfoObjArr[0];
            String advPaymentFromCurrencyId = (String) paymentInfoObjArr[4];
            KwlReturnObject advPaymentbAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, amountReceivedConverted, advPaymentFromCurrencyId, advPaymentCreationDate, advPaymentExternalCurrencyRate);
            double advPaymentAmountReceivedConvertedInBaseCurrency = (Double) advPaymentbAmt.getEntityList().get(0);
            advPaymentAmountReceivedConvertedInBaseCurrency = authHandler.round(advPaymentAmountReceivedConvertedInBaseCurrency, companyid);

            // for updating amount due of advance payment
            accountingHandlerDAOobj.updateAdvanceDetailAmountDueOnAmountReceived(jobj.getString("documentid"), (-amountReceivedConverted));

            /*
             * Start gains/loss calculation Calculate Gains/Loss if Advance
             * Payment exchange rate changed at the time of linking with refund
             * receipt payment
             */
            Locale locale = (Locale) requestObj.opt("locale");
            if (preferences.getForeignexchange() == null) {
                throw new AccountingException(messageSource.getMessage("acc.common.forex", null, locale));
            }
            double amountDiff = RefundReceiptForexGailLossAmount(requestObj, jobj, receipt, (String) paymentInfoObjArr[4], receipt.getCurrency().getCurrencyID(), externalCurrencyRate);

            if (amountDiff != 0 && preferences.getForeignexchange() != null && Math.abs(amountDiff) >= 0.000001) { // Math.abs(amountDiff) < .0000001 Added this because in case of revaluation the diff in the rates was less than .0000001 and the amount calculation was going in Exachange rate account. 
                boolean rateDecreased = false;
                if (amountDiff < 0) {
                    rateDecreased = true;
                }
                JournalEntry journalEntry = null;
//                Map<String, Object> jeDataMap = StringUtil.jsonToMap(requestObj.optJSONObject("globalparams"));
                Map<String, Object> jeDataMap = AccountingManager.getGlobalParamsJson(requestObj.optJSONObject("paramJobj"));
                int counter = 0;
                if (counterMap.containsKey("counter")) {
                    counter = (Integer) counterMap.get("counter");
                }
                Date entryDate = maxDate;
                synchronized (this) {
                    HashMap<String, Object> JEFormatParams = new HashMap<>();
                    JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                    JEFormatParams.put("modulename", "autojournalentry");
                    JEFormatParams.put("companyid", companyid);
                    JEFormatParams.put("isdefaultFormat", true);

                    KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                    SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                    Map<String, Object> seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, entryDate);
                    String nextAutoNoTemp = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER); // next auto generated number
                    int sequence = Integer.parseInt((String) seqNumberMap.get(Constants.SEQNUMBER));
                    String jeDatePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX); // Date Prefix Part
                    String jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                    String jeDateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX); // Date Suffix Part

                    sequence = sequence + counter;
                    String number = "" + sequence;
                    String action = "" + (sequence - counter);
                    nextAutoNoTemp.replaceAll(action, number);
                    String jeentryNumber = nextAutoNoTemp.replaceAll(action, number); // next auto generated number
                    String jeSeqFormatId = format.getID();
                    boolean jeautogenflag = true;
                    counter++;
                    counterMap.put("counter", counter);
                    jeDataMap.put("entrynumber", jeentryNumber);
                    jeDataMap.put("autogenerated", jeautogenflag);
                    jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                    jeDataMap.put(Constants.SEQNUMBER, number);
                    jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
                    jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
                    jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
                }
                jeDataMap.put("entrydate", entryDate); 
                jeDataMap.put("companyid", companyid);
                jeDataMap.put("memo", "Exchange Gains/Loss posted against Refund Receipt '" + receipt.getReceiptNumber() + "' linked to Advance Payment '" + (String) paymentInfoObjArr[3] + "'");
                jeDataMap.put("currencyid", receipt.getCurrency().getCurrencyID());
                jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
                jeDataMap.put("isexchangegainslossje", true);
                jeDataMap.put("transactionId", receipt.getID());
                jeDataMap.put("transactionModuleid", Constants.Acc_Receive_Payment_ModuleId);
                journalEntry = accJournalEntryobj.getJournalEntry(jeDataMap);
                accJournalEntryobj.saveJournalEntryByObject(journalEntry);

                boolean isDebit = rateDecreased ? true : false;
                JSONObject jedjson = new JSONObject();
                jedjson.put("srno", 1);
                jedjson.put("companyid", companyid);
                jedjson.put("amount", rateDecreased ? (-1 * amountDiff) : amountDiff);
                jedjson.put("accountid", preferences.getForeignexchange().getID());
                jedjson.put("debit", isDebit);
                jedjson.put("jeid", journalEntry.getID());
                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                Set<JournalEntryDetail> detail = new HashSet();
                detail.add(jed);

                jedjson = new JSONObject();
                jedjson.put("srno", 2);
                jedjson.put("companyid", companyid);
                jedjson.put("amount", rateDecreased ? (-1 * amountDiff) : amountDiff);
                jedjson.put("accountid", (String) paymentInfoObjArr[6]);
                jedjson.put("debit", !isDebit);
                jedjson.put("jeid", journalEntry.getID());
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                detail.add(jed);
                journalEntry.setDetails(detail);
                accJournalEntryobj.saveJournalEntryDetailsSet(detail);

                rd.setLinkedGainLossJE(journalEntry.getID());
            }
            // End Gains/Loss Calculation
            for (ReceiptAdvanceDetail advanceDetail : receipt.getReceiptAdvanceDetails()) {
                if (advanceDetail.getAmountDue() > 0) {
//                     advanceDetail.setAmountDue(advanceDetail.getAmountDue() - amountReceived);
                     advanceDetail.setAmountDue(authHandler.round(advanceDetail.getAmountDue()-amountReceived,company.getCompanyID()));
                     /*
                      * Set Set id for advace detail id in ReceiptAdvanceDetail
                      */
//                     advanceDetail.setAdvancedetailid(jobj.getString("documentid"));
                }
            }
            linkdetails.add(rd);
        }
        return linkdetails;
    }

    /**
     * Description : Method is used to save linking information of Receipt and Advance Payment
     * @param <advPaymentId> used to get Advance Payment id
     * @param <advPaymentNo> used to get Advance Payment number
     * @param <receipt> used to get Receipt information
     * @return void
     * @throws ServiceException 
     */
    public void saveLinkingInformationOfRefundPaymentWithAdvancePayment(String advPaymentId, String advPaymentNo, Receipt receipt) throws ServiceException {
        try {
            // Save Advance Payment Linking & Receipt Paymnet Linking information in linking table
            HashMap<String, Object> requestParamsLinking = new HashMap<>();
            requestParamsLinking.put("linkeddocid", receipt.getID());
            requestParamsLinking.put("docid", advPaymentId);
            requestParamsLinking.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
            requestParamsLinking.put("linkeddocno", receipt.getReceiptNumber());
            requestParamsLinking.put("sourceflag", 0);
            accGoodsReceiptobj.savePaymentLinking(requestParamsLinking);

            requestParamsLinking.clear();
            requestParamsLinking.put("linkeddocid", advPaymentId);
            requestParamsLinking.put("docid", receipt.getID());
            requestParamsLinking.put("moduleid", Constants.Acc_Make_Payment_ModuleId);
            requestParamsLinking.put("linkeddocno", advPaymentNo);
            requestParamsLinking.put("sourceflag", 1);
            accReceiptDAOobj.saveReceiptLinking(requestParamsLinking);
        } catch (Exception ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
    
    /**
     * Description : Method is used to unlinking Receipt from Advance Payment
     * @param <request> used to get companyid, paymentid, linkdetails
     * @return List
     * @throws ServiceException
     * @throws SessionExpiredException 
     */
    public List unlinkReceiptFromAdvancePayment(HttpServletRequest request) throws AccountingException, ServiceException, SessionExpiredException {
        List result = new ArrayList();
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String paymentid = request.getParameter("paymentid");

            // for getting receipt object
            KwlReturnObject receiptKWLObj = accountingHandlerDAOobj.getObject(Receipt.class.getName(), paymentid);
            Receipt receipt = (Receipt) receiptKWLObj.getEntityList().get(0);
            String paymentno = receipt.getReceiptNumber();

            String linkedAdvancePaymentids = "";
            String linkedAdvancePaymentnos = "";
            List<String> linkedDetailAdvancePayment = new ArrayList();
            JSONArray linkJSONArray = request.getParameter("linkdetails") != null ? new JSONArray(request.getParameter("linkdetails")) : new JSONArray();
            String invoicelinkingyearArr = request.getParameter("invoicelinkingyearArr") != null ? request.getParameter("invoicelinkingyearArr") : "";
            for (int k = 0; k < linkJSONArray.length(); k++) { // creating a hash map with payment and their linked advance payment
                JSONObject jSONObject = linkJSONArray.getJSONObject(k);
                String linkId = jSONObject.optString("linkdetailid", "");
                linkedDetailAdvancePayment.add(linkId);
            }
            String[] invoicelinkingarr = invoicelinkingyearArr.split(",");
            for (int l = 0; l < invoicelinkingarr.length; l++) {
                String linkingyear = invoicelinkingarr[l];
                /**
                 * if linking date is fall in closed year the exception has been
                 * thrown.
                 */
                boolean isBookClose = false;
                if (!StringUtil.isNullOrEmpty(linkingyear)) {
                    paramJobj.put("yearid", linkingyear);
                    isBookClose = accCompanyPreferencesObj.isBookClose(paramJobj);
                }
                if (isBookClose) {
                    throw new AccountingException(messageSource.getMessage("acc.compref.closebook.Youcannotunlinkthedocumentsinclosedyear", null, RequestContextUtils.getLocale(request)));
                }
            }
            // for getting link ids
            String linkedDetailIDs = "";
            for (String advPaymentID : linkedDetailAdvancePayment) {
                linkedDetailIDs = linkedDetailIDs.concat("'").concat(advPaymentID).concat("',");
            }
            if (!StringUtil.isNullOrEmpty(linkedDetailIDs.toString())) {
                linkedDetailIDs = linkedDetailIDs.substring(0, linkedDetailIDs.length() - 1);
            }
            
            double sumOfTotalAmount = 0;
            List<LinkDetailReceiptToAdvancePayment> details = accReceiptDAOobj.getDeletedLinkedReceiptAdvancePayment(receipt, linkedDetailIDs, companyid);
            for (LinkDetailReceiptToAdvancePayment receiptDetail : details) {
                KwlReturnObject paymentResult = accReceiptDAOobj.getPaymentInformationFromPaymentId(receiptDetail.getPaymentId(), companyid);
                Object[] paymentInfoObjArr = (Object[]) paymentResult.getEntityList().get(0);
                
                // for updating amount due of advance payment
                accountingHandlerDAOobj.updateAdvanceDetailAmountDueOnAmountReceived((String) paymentInfoObjArr[2], receiptDetail.getAmountInPaymentCurrency());
                /**
                 * We need to add amount only if payment is not dishonoured if
                 * payment is already dishonoured then the amountdue is
                 * already restored at the time of marking payment dishonoured.
                 */
                if ((char) paymentInfoObjArr[4] != 'T') {
                    sumOfTotalAmount += receiptDetail.getAmount();
                }
                linkedAdvancePaymentnos = linkedAdvancePaymentnos.concat((String) paymentInfoObjArr[1]).concat(",");

                // check if Forex/Gain Loss JE generated. If yes then need to delete JE too
                if (receiptDetail.getLinkedGainLossJE() != null && !receiptDetail.getLinkedGainLossJE().isEmpty()) {
                    deleteJEArray(receiptDetail.getLinkedGainLossJE(), companyid);
                }
                if (receiptDetail != null && !StringUtil.isNullOrEmpty(receiptDetail.getRevalJeId())) {
                    accJournalEntryobj.deleteJEDtails(receiptDetail.getRevalJeId(), companyid);
                    accJournalEntryobj.deleteJE(receiptDetail.getRevalJeId(), companyid);
                }
                if (receiptDetail != null && !StringUtil.isNullOrEmpty(receiptDetail.getRevalJeIdReceipt())) {
                    accJournalEntryobj.deleteJEDtails(receiptDetail.getRevalJeIdReceipt(), companyid);
                    accJournalEntryobj.deleteJE(receiptDetail.getRevalJeIdReceipt(), companyid);
                }

                /*
                 * Deleting Linking iformation of Received Payment from Linking
                 * table if it is unlinked
                 */
                HashMap<String, Object> linkingrequestParams = new HashMap<>();
                linkingrequestParams.put("receiptid", receipt.getID());
                linkingrequestParams.put("linkedTransactionID", receiptDetail.getPaymentId());
                linkingrequestParams.put("unlinkflag", true);
                accReceiptDAOobj.deleteLinkingInformationOfRP(linkingrequestParams);
            }

            // for updating amount due
            if (sumOfTotalAmount != 0 && receipt.getReceiptAdvanceDetails() != null && !receipt.getReceiptAdvanceDetails().isEmpty()) {
                for (ReceiptAdvanceDetail advanceDetail : receipt.getReceiptAdvanceDetails()) {
                    /**
                     * updating amount due even when linked payment is marked
                     * dishonoured.This code is written because we can link
                     * advance payment at line level and externally i.e from
                     * report to refund receipt.
                     */
                    KwlReturnObject paymentResult = accReceiptDAOobj.getPaymentInformationFromAdvanceDetailId(advanceDetail.getAdvancedetailid(), companyid);
                    boolean isDishonoured = false;
                    if (paymentResult.getEntityList() != null && paymentResult.getEntityList().size() > 0) {
                        Object[] paymentInfoObjArr = (Object[]) paymentResult.getEntityList().get(0);
                        isDishonoured = ((char) paymentInfoObjArr[8] == 'T');
                    }
                    if (StringUtil.isNullOrEmpty(advanceDetail.getAdvancedetailid()) || isDishonoured) {
                        double linkedAmountDue = advanceDetail.getAmountDue();
                        advanceDetail.setAmountDue(linkedAmountDue + sumOfTotalAmount);
                        List<Object> objectList = new ArrayList<>();
                        objectList.add((Object) advanceDetail);
                        accAccountDAOobj.saveOrUpdateAll(objectList);
                    }
                    
//                    if (!StringUtil.isNullOrEmpty(advanceDetail.getAdvancedetailid())) {
//                        double linkedAmountDue = advanceDetail.getAmountDue();
//                        advanceDetail.setAmountDue(linkedAmountDue + sumOfTotalAmount);
//                        advanceDetail.setAdvancedetailid(null);
//                        List<Object> objectList = new ArrayList<>();
//                        objectList.add((Object) advanceDetail);
//                        accAccountDAOobj.saveOrUpdateAll(objectList);
//                    }
                }
            }
            
            if (!StringUtil.isNullOrEmpty(linkedAdvancePaymentnos)) {
                linkedAdvancePaymentnos = linkedAdvancePaymentnos.substring(0, linkedAdvancePaymentnos.length() - 1);
            }
            // for deleting linked receipts Advance Details
            accReceiptDAOobj.deleteSelectedLinkedReceiptAdvanceDetails(receipt.getID(), linkedDetailIDs, companyid);
            // for inserting Audit Trail entry
            if (!StringUtil.isNullOrEmpty(linkedAdvancePaymentnos)) {
                auditTrailObj.insertAuditLog(AuditAction.LINKEDRECEIPT, "User " + sessionHandlerImpl.getUserFullName(request) + " has unlinked Receipt " + paymentno + " from " + linkedAdvancePaymentnos, request, linkedAdvancePaymentids);
            }
        }  catch (AccountingException e){
            throw new AccountingException(e.getMessage());
        }catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    /*
    *Method to Approve Pending Receive Payments
    */
    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = {ServiceException.class, AccountingException.class})
    public ModelAndView approvePendingReceivePayment(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

//        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
//        def.setName("Invoice_Tx");
//        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
//        TransactionStatus status = txnManager.getTransaction(def);
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            String billid = request.getParameter("billid");
            String billno = request.getParameter("billno");
            String remark = request.getParameter("remark");
            String psotingDateStr = request.getParameter("postingDate");
            String userid = sessionHandlerImpl.getUserid(request);
            String userName = sessionHandlerImpl.getUserFullName(request);
            String baseUrl = URLUtil.getPageURL(request, loginpageFull);
            double amount = StringUtil.isNullOrEmpty(request.getParameter("amount")) ? 0 : Double.parseDouble(request.getParameter("amount"));
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put("billid", billid);
            requestParams.put("billno", billno);
            requestParams.put("remark", remark);
            requestParams.put("userid", userid);
            requestParams.put("userName", userName);
            requestParams.put("amount", amount);
            requestParams.put("reqHeader", request.getHeader("x-real-ip"));//USED TO INSERT DATA IN AUDIT TRIAL
            requestParams.put("remoteAddress", request.getRemoteAddr());//USED TO INSERT DATA IN AUDIT TRIAL
            requestParams.put("baseUrl", baseUrl);
            requestParams.put("postingDate", psotingDateStr);
            
            List list = accReceivePaymentModuleServiceObj.approvePendingReceivePayment(requestParams);
            msg = (String) list.get(0);
            issuccess = true;
            
            //========Rounding JE Related Code Start================
            try {
                accReceivePaymentModuleServiceObj.postRoundingJEAfterReceiptApprove(paramJobj);
            } catch (Exception ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
            //========Rounding JE Related Code End================
            
//            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
//            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(AccountingException ae){
            msg = "" + ae.getMessage();
            msg = msg.replaceFirst("Transaction", "JE Posting");
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ae);
        }catch (Exception ex) {
//            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
   
    @Transactional(propagation = Propagation.REQUIRED)
    public ModelAndView rejectPendingReceivePayment(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

//        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
//        def.setName("VP_Tx");
//        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
//        TransactionStatus status = txnManager.getTransaction(def);
        try {
            request.setAttribute("rejectReceipt", true);
            String userName = sessionHandlerImpl.getUserFullName(request);
            String receiptid = "";
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            int level = 0;
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject receiptJobj = jArr.getJSONObject(i);
                receiptid = StringUtil.DecodeText(receiptJobj.optString("billid"));
                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Receipt.class.getName(), receiptid);
                Receipt receipt = (Receipt) objItr.getEntityList().get(0);
                level = receipt.getApprovestatuslevel();
            }
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            accReceivePaymentModuleServiceObj.deleteReceiptTemporary(paramJobj);
//            deleteReceiptMerged(request);
//            boolean isRejected = (boolean) request.getAttribute("isRejected");        //was giving null pointer exception as deleteReceiptMerged(request) is moved to service layer and request dependency is removed
            boolean isRejected = paramJobj.optBoolean("isRejected",false);
            if (isRejected) {
                msg = messageSource.getMessage("acc.field.receivepaymenthasbeenrejectedsuccessfully", null, RequestContextUtils.getLocale(request)) + " by " + userName + " at Level " + level + ".";
            } else {
                msg = messageSource.getMessage("acc.vq.notAuthorisedToRejectThisRecord", null, RequestContextUtils.getLocale(request)) + " at Level " + level + ".";
            }
//            txnManager.commit(status);
            issuccess = true;
        } catch (SessionExpiredException ex) {
//            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accReceiptControllerNew.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
//            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accReceiptControllerNew.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptControllerNew.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }       

    public ModelAndView checkInvoiceKnockedOffDuringReceivePaymentPending(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";

        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            String billid = request.getParameter("billid");
            String companyid = request.getParameter("companyid");

            requestParams.put("billid", billid);
            requestParams.put("companyid", companyid);

            jobj = accReceivePaymentModuleServiceObj.checkInvoiceKnockedOffDuringReceivePaymentPending(requestParams);

        } catch (SessionExpiredException ex) {

            msg = ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {

            msg = "" + ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
     /**
     * @param request
     * @param response
     * @return ModelAndView
     * @throws com.krawler.hql.accounting.AccountingException
     * @throws com.krawler.common.session.SessionExpiredException
     * @description Function to check whether any transaction is made and
     * discount is calculated in Receive Payment because if we allow to disable
     * the check the data will be Corrupt in case of edit of receipt.
     */
    public ModelAndView checkTransactionsForDiscountOnPaymentTerms(HttpServletRequest request, HttpServletResponse response) throws AccountingException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            JSONObject reqJobj = new JSONObject();
            reqJobj.put("companyid", companyid);
            KwlReturnObject result = accReceivePaymentModuleServiceObj.checkTransactionsForDiscountOnPaymentTerms(reqJobj);
            List list = result.getEntityList();
            if (!list.isEmpty() && (Integer.parseInt(list.get(0).toString()) > 0 || (list.size() > 1 && Integer.parseInt(list.get(1).toString()) > 0))) {
                issuccess = false;
                throw new AccountingException(messageSource.getMessage("acc.companypref.disablediscountonpaymenttermerrormsg", null, RequestContextUtils.getLocale(request)));
            } else {
                issuccess = true;
                msg = "";
            }
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accReceiptControllerNew.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AccountingException ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accReceiptControllerNew.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accReceiptControllerNew.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptControllerNew.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
}
