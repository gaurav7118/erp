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
package com.krawler.spring.accounting.vendorpayment;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.CsvReader;
import com.krawler.common.util.IndiaComplianceConstants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.accounting.bankreconciliation.accBankReconciliationDAO;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesCMN;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.invoice.InvoiceConstants;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.vendor.accVendorDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.importFunctionality.ImportDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import com.krawler.spring.accounting.customDesign.CustomDesignHandler;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.payment.accPaymentService;
import com.krawler.spring.accounting.vendorpayment.service.AccVendorPaymentModuleService;
import com.krawler.spring.common.AccCommonTablesDAO;
import java.io.*;
import java.math.BigInteger;
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
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class accVendorPaymentController extends MultiActionController implements MessageSourceAware {

    private HibernateTransactionManager txnManager;
    private accGoodsReceiptDAO accGoodsReceiptobj;
    private accVendorPaymentDAO accVendorPaymentobj;
    private accJournalEntryDAO accJournalEntryobj;
    private accPaymentDAO accPaymentDAOobj;
    private accTaxDAO accTaxObj;
    private accCurrencyDAO accCurrencyDAOobj;
    private auditTrailDAO auditTrailObj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accAccountDAO accAccountDAOobj;
    private exportMPXDAOImpl exportDaoObj;
    private fieldDataManager fieldDataManagercntrl;
    private accBankReconciliationDAO accBankReconciliationObj;
    private String successView;
    private MessageSource messageSource;
    private EnglishNumberToWords EnglishNumberToWordsOjb = new EnglishNumberToWords();
    private ImportHandler importHandler;
    private ImportDAO importDao;
    private accPaymentService paymentService;
    private accVendorDAO accVendorDAOObj;
    private AccCommonTablesDAO accCommonTablesDAO;
    private accMasterItemsDAO accMasterItemsDAO;
    private AccVendorPaymentModuleService accVendorPaymentModuleServiceObj;

    /**
     * @param accVendorPaymentModuleServiceObj the
     * accVendorPaymentModuleServiceObj to set
     */
    public void setAccVendorPaymentModuleServiceObj(AccVendorPaymentModuleService accVendorPaymentModuleServiceObj) {
        this.accVendorPaymentModuleServiceObj = accVendorPaymentModuleServiceObj;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    public void setaccPaymentService(accPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public void setimportDAO(ImportDAO importDao) {
        this.importDao = importDao;
    }

    public void setAccVendorDAO(accVendorDAO accVendorDAOObj) {
        this.accVendorDAOObj = accVendorDAOObj;
    }

    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setaccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
    }

    public void setaccVendorPaymentDAO(accVendorPaymentDAO accVendorPaymentobj) {
        this.accVendorPaymentobj = accVendorPaymentobj;
    }

    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }

    public void setaccPaymentDAO(accPaymentDAO accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
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

    public void setaccBankReconciliationDAO(accBankReconciliationDAO accBankReconciliationObj) {
        this.accBankReconciliationObj = accBankReconciliationObj;
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

    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
    
    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }
    
    public void setAccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAO) {
        this.accMasterItemsDAO = accMasterItemsDAO;
    }

    public ModelAndView savePayment(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject jobjDetails = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean issuccess = false;
        boolean isChequePrint = false;
        String msg = "";
        String paymentid = "";
        String billno = "";
        String advanceamount = "";
        String amountpayment = "", accountaddress = "", accountName = "", JENumBer = "";
        int receipttype = -1;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            List li = savePayment(request);
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
            issuccess = true;
            if (!StringUtil.isNullOrEmpty(request.getParameter("isChequePrint"))) {
                isChequePrint = Boolean.parseBoolean(request.getParameter("isChequePrint"));
            }
            if (isChequePrint) {
                KwlReturnObject result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), request.getParameter("pmtmethod"));
                PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);

                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                if (!StringUtil.isNullOrEmpty(payMethod.getID())) {
                    requestParams.put("bankid", payMethod.getID());
                }

                KwlReturnObject result1 = accPaymentDAOobj.getChequeLayout(requestParams);
                List list = result1.getEntityList();
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    ChequeLayout chequeLayout = (ChequeLayout) itr.next();
                    JSONObject chequeobj = new JSONObject(chequeLayout.getCoordinateinfo());
                    jobjDetails.put("dateLeft", chequeobj.optString("dateLeft","0"));
                    jobjDetails.put("nameLeft", chequeobj.optString("nameLeft","0"));
                    jobjDetails.put("amtinwordLeft", chequeobj.optString("amtinwordLeft","0"));
                    jobjDetails.put("amtinwordLeftLine2", chequeobj.optString("amtinwordLeftLine2","0"));
                    jobjDetails.put("amtLeft", chequeobj.optString("amtLeft","0"));
                    jobjDetails.put("dateTop", chequeobj.optString("dateTop","0"));
                    jobjDetails.put("nameTop", chequeobj.optString("nameTop","0"));
                    jobjDetails.put("amtinwordTop", chequeobj.optString("amtinwordTop","0"));
                    jobjDetails.put("amtinwordTopLine2", chequeobj.optString("amtinwordTopLine2","0"));
                    jobjDetails.put("amtTop", chequeobj.optString("amtTop","0"));
                }
                SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("ddMMyy");
                Date creationDate = new Date(request.getParameter("creationdate"));
                String date = DATE_FORMAT.format(creationDate);
                String formatted_date_with_spaces = "";
                for (int i = 0; i < date.length(); i++) {
                    formatted_date_with_spaces += date.charAt(i);
                    formatted_date_with_spaces += "&nbsp&nbsp&nbsp&nbsp&nbsp";

                }
                String[] amount = (String[]) li.get(1);
                String[] amount1 = (String[]) li.get(2);
                String[] accName = (String[]) li.get(3);
                jobjDetails.put(amount[0], amount[1]);
                String amount_first_line = "";
                String amount_second_line = "";
                String action=" Only.";
                if (amount1[1].length() > 34 && amount1[1].charAt(34) == ' ') {
                    amount_first_line = amount1[1].substring(0, 34);
                    amount_second_line = amount1[1].substring(34, amount1[1].length());
                    jobjDetails.put(amount1[0], amount_first_line);
                    jobjDetails.put("amountinword1", amount_second_line +action);
                } else if (amount1[1].length() > 34) {
                    amount_first_line = amount1[1].substring(0, 34);
                    amount_first_line = amount1[1].substring(0, amount_first_line.lastIndexOf(" "));
                    amount_second_line = amount1[1].substring(amount_first_line.length(), amount1[1].length());
                    jobjDetails.put(amount1[0], amount_first_line);
                    jobjDetails.put("amountinword1", amount_second_line + action);
                } else {
                    if (amount1[1].length() < 27) {
                        jobjDetails.put(amount1[0], amount1[1] + action);
                        jobjDetails.put("amountinword1", "");
                    } else {
                        jobjDetails.put(amount1[0], amount1[1]);
                        jobjDetails.put("amountinword1", action);
                    }
                }
                jobjDetails.put(accName[0], accName[1]);
                jobjDetails.put("date", formatted_date_with_spaces);
                jArr.put(jobjDetails);

                String chqno = "";
                if (li.get(11) != null) {
                    chqno = (String) li.get(11);
            }
                KwlReturnObject result2 = accPaymentDAOobj.updateChequePrint(paymentid,companyid);
                auditTrailObj.insertAuditLog(AuditAction.MAKE_PAYMENT, "User " + sessionHandlerImpl.getUserFullName(request) + " has printed a cheque "+chqno+" for "+accName[1]+" in payment " + billno, request, paymentid);
            }
            msg = messageSource.getMessage("acc.pay.save", null, RequestContextUtils.getLocale(request)) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + billno + ",</b> " + messageSource.getMessage("acc.field.JENo", null, RequestContextUtils.getLocale(request)) + ": <b>" + JENumBer + "</b>";   //"Payment information has been saved successfully";
            txnManager.commit(status);
            status = txnManager.getTransaction(def);
            deleteJEArray(id[0], companyid);
            txnManager.commit(status);
            status = txnManager.getTransaction(def);
            deleteChequeOrCard(id[1], companyid);// method used inside savePayment
            txnManager.commit(status);
            if (request.getParameter("isEdit") != null && Boolean.parseBoolean(request.getParameter("isEdit"))) {
                deletePaymentForEdit(request, response);
            }
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            issuccess = false;
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            issuccess = false;
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
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
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView updatePaymentMadeTransactionDetailsInJE(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Invoice_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONObject jobj = new JSONObject();
        int jeupdatedcount = 0;
        boolean issuccess = false;
        try {
            String subdomain = "";
            String[] subdomainArray = null;
            HashMap<String, Object> tempParams = new HashMap<String, Object>();
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
                System.out.println("payment.company = " + companyid);
                KwlReturnObject result = accPaymentDAOobj.getPaymentMadeForJE(requestParams);
                Iterator itr = result.getEntityList().iterator();
                while (itr.hasNext()) {
                    Object obj[] = (Object[]) itr.next();
//                    Payment payment = (Payment) obj[0];
                    String paymentID = (String) obj[0];
                    String journalEntryID = "";
                    if (obj[0] != null) {
                        journalEntryID = (String) obj[1];
                    }
                    String journalentryforbankchargesID = "";
                    if (obj[2] != null) {
                        journalentryforbankchargesID = (String) obj[2];
                    }
                    String journalentryforbankinterestID = "";
                    if (obj[3] != null) {
                        journalentryforbankinterestID = (String) obj[3];
                    }
//                    System.out.println("payment.getID() = " + payment.getID());
                    boolean isUpdated = false;
                    tempParams = new HashMap<String, Object>();
                    tempParams.put("transactionID", paymentID);
                    tempParams.put("moduleID", Constants.Acc_Make_Payment_ModuleId);
                    if (!StringUtil.isNullOrEmpty(journalEntryID)) {
                        try {
                            tempParams.put("jeID", journalEntryID);
                            isUpdated = accJournalEntryobj.updateJEDetailsSQLQuery(tempParams);
                            if (isUpdated) {
                                jeupdatedcount++;
                            }
                        } catch (Exception ex) {
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(journalentryforbankchargesID)) {
                        try {
                            tempParams.put("jeID", journalentryforbankchargesID);
                            isUpdated = accJournalEntryobj.updateJEDetailsSQLQuery(tempParams);
                            if (isUpdated) {
                                jeupdatedcount++;
                            }
                        } catch (Exception ex) {
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(journalentryforbankinterestID)) {
                        try {
                            tempParams.put("jeID", journalentryforbankinterestID);
                            isUpdated = accJournalEntryobj.updateJEDetailsSQLQuery(tempParams);
                            if (isUpdated) {
                                jeupdatedcount++;
                            }
                        } catch (Exception ex) {
                        }
                    }
                }
            }
            txnManager.commit(status);
            issuccess = true;
        } catch (Exception ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            txnManager.rollback(status);
            issuccess = false;
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("Updated JE Records ", issuccess ? jeupdatedcount : 0);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView importOpeningBalancePayments(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
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
            requestParams.put("moduleName", "Opening Payment");
            requestParams.put("moduleid", Constants.Acc_opening_Payment);
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

                jobj = importOeningTransactionsRecords(request, datajobj);
            }else if (doAction.compareToIgnoreCase("validateData") == 0) {
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
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }

            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void validateHeaders(JSONArray validateJArray) throws AccountingException, ServiceException {
        try {

            List<String> list = new ArrayList<String>();
            list.add("Transaction Number");
            list.add("Transaction Date");
            list.add("Amount");
//            list.add("Due Date");
            list.add("Vendor Code");
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
                    throw new AccountingException(manadatoryField + " column is not availabe in file");
                }
            }

        } catch (JSONException ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

//    /*
//     * Method to save Opening Balance Payments For customer.
//     */
//    public ModelAndView saveOpeningBalancePayment(HttpServletRequest request, HttpServletResponse response) {
//        JSONObject jobj = new JSONObject();
//        JSONObject jobjDetails = new JSONObject();
//        JSONArray jArr = new JSONArray();
//        boolean issuccess = false;
//        String msg = "";
//        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
//        def.setName("BR_Tx");
//        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
//        TransactionStatus status = null;
//        boolean isAccountingExe = false;
//        String paymentNumber = null;
//        String companyid = "";
//        try {
//            paymentNumber = request.getParameter("number");
//            String paymentId = request.getParameter("transactionId");
//            companyid = sessionHandlerImpl.getCompanyid(request);
//            KwlReturnObject cncount = null;
//            if (StringUtil.isNullOrEmpty(paymentId)) {
//                /*
//                 * Checks duplicate number while creating new record
//                 */
//                cncount = accVendorPaymentobj.getPaymentFromNo(paymentNumber, companyid);
//                if (cncount.getRecordTotalCount() > 0) {
//                    isAccountingExe = true;
//                    throw new AccountingException(messageSource.getMessage("acc.payment.paymentno", null, RequestContextUtils.getLocale(request)) + paymentNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
//                }
//                /*
//                 * code for checking wheather entered number can be generated by
//                 * sequence format or not
//                 */
//                List resultList = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Make_Payment_ModuleId, paymentNumber, companyid);
//                if (!resultList.isEmpty()) {
//                    boolean isvalidEntryNumber = (Boolean) resultList.get(0);
//                    String formatName = (String) resultList.get(1);
//                    if (!isvalidEntryNumber) {
//                        isAccountingExe = true;
//                        throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, RequestContextUtils.getLocale(request)) + " <b>" + paymentNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, RequestContextUtils.getLocale(request)) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.pleaseentersomeothernumber", null, RequestContextUtils.getLocale(request)));
//                    }
//                }
//            }
//            synchronized (this) {
//                /*
//                 * Checks duplicate number for simultaneous transactions
//                 */
//                status = txnManager.getTransaction(def);
//                KwlReturnObject resultInv = accCommonTablesDAO.getTransactionInTemp(paymentNumber, companyid, Constants.Acc_Make_Payment_ModuleId);//Get entry from temporary table
//                if (resultInv.getRecordTotalCount() > 0) {
//                    isAccountingExe = true;
//                    throw new AccountingException(messageSource.getMessage("acc.PO.selectedPamentNo", null, RequestContextUtils.getLocale(request)) + paymentNumber + messageSource.getMessage("acc.field.alreadyinprocess.", null, RequestContextUtils.getLocale(request)));
//                } else {
//                    accCommonTablesDAO.insertTransactionInTemp(paymentNumber, companyid, Constants.Acc_Make_Payment_ModuleId);//Insert entry in temporary table
//                }
//                
//                
//                /*
//                 * Check for invalid cheque number
//                 */
//                
//                KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
//                CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
//
//                if (preferences.getChequeNoDuplicate() != Constants.ChequeNoIgnore) {// Ignore case 
//                    checkForInvalidChequeNumber(request, null);
//                }
//                if (!StringUtil.isNullOrEmpty(request.getParameter("paydetail"))) {
//                    JSONObject obj = new JSONObject(request.getParameter("paydetail"));
//                    String chequeNumber = obj.optString("chequenumber");
//                    String methodid = request.getParameter("pmtmethod");
//                    KwlReturnObject result1 = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), methodid);
//                    PaymentMethod payMethod = (PaymentMethod) result1.getEntityList().get(0);
//                    String bankId = payMethod.getAccount().getID();
//
//                    KwlReturnObject resultInv1 = accPaymentDAOobj.getSearchChequeNoTemp(chequeNumber, companyid, Constants.Cheque_ModuleId, bankId);
//                    if (resultInv1.getRecordTotalCount() > 0) {
//                        throw new AccountingException("Cheque Number : <b>" + chequeNumber + "</b> is already exist, Please enter different one");
//                    } else {
//                        accPaymentDAOobj.insertInvoiceOrCheque(chequeNumber, companyid, Constants.Cheque_ModuleId, bankId);
//                    }
//                }
//                
//                
//                txnManager.commit(status);
//            }
//            status = txnManager.getTransaction(def);
//            List li = saveOpeningBalancePayment(request);
//            boolean isEditInv = false;
//            String succMsg = messageSource.getMessage("acc.field.saved", null, RequestContextUtils.getLocale(request));
//            if (!li.isEmpty()) {
//                paymentNumber = li.get(0).toString();
//                isEditInv = (Boolean) li.get(1);
//            }
//            if (isEditInv) {
//                succMsg = messageSource.getMessage("acc.field.updated", null, RequestContextUtils.getLocale(request));
//            }
//            issuccess = true;
//            msg = messageSource.getMessage("acc.receipt.2", null, RequestContextUtils.getLocale(request)) + " " + paymentNumber + " " + messageSource.getMessage("acc.field.hasbeen", null, RequestContextUtils.getLocale(request)) + " " + succMsg + " " + messageSource.getMessage("acc.field.successfully", null, RequestContextUtils.getLocale(request));
//            txnManager.commit(status);
//            status = txnManager.getTransaction(def);
//            accCommonTablesDAO.deleteTransactionInTemp(paymentNumber, companyid, Constants.Acc_Make_Payment_ModuleId);//Delete entry in temporary table
//            txnManager.commit(status);
//
//        } catch (SessionExpiredException ex) {
//            if (status != null) {
//                txnManager.rollback(status);
//            }
//            try {
//                accCommonTablesDAO.deleteTransactionInTemp(paymentNumber, companyid, Constants.Acc_Make_Payment_ModuleId);//Delete entry in temporary table
//            } catch (ServiceException ex1) {
//                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex1);
//            }
//            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
//            msg = ex.getMessage();
//        } catch (ServiceException ex) {
//            if (status != null) {
//                txnManager.rollback(status);
//            }
//            try {
//                accCommonTablesDAO.deleteTransactionInTemp(paymentNumber, companyid, Constants.Acc_Make_Payment_ModuleId);//Delete entry in temporary table
//            } catch (ServiceException ex1) {
//                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex1);
//            }
//            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
//            msg = ex.getMessage();
//        } catch (Exception ex) {
//            if (status != null) {
//                txnManager.rollback(status);
//            }
//            try {
//                accCommonTablesDAO.deleteTransactionInTemp(paymentNumber, companyid, Constants.Acc_Make_Payment_ModuleId);//Delete entry in temporary table
//            } catch (ServiceException ex1) {
//                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex1);
//            }
//            msg = "" + ex.getMessage();
//            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
//        } finally {
//            try {
//                jobj.put("success", issuccess);
//                jobj.put("msg", msg);
//                jobj.put("data", jArr);
//                jobj.put("accException", isAccountingExe);
//            } catch (JSONException ex) {
//                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        return new ModelAndView("jsonView", "model", jobj.toString());
//    }
//
//    public List saveOpeningBalancePayment(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
//        List returnList = new ArrayList();
//        try {
//            String companyid = sessionHandlerImpl.getCompanyid(request);
//            DateFormat df = authHandler.getDateOnlyFormat(request);
//            KwlReturnObject result;
//            boolean isEditInvoice = false;
//            String auditMsg = "", auditID = "", memo = "";
//            auditMsg = "added";
//            auditID = AuditAction.OPENING_BALANCE_CREATED;
//            // Fetching request parameters
//
//            String paymentNumber = request.getParameter("number");
//            String transactionDateStr = request.getParameter("billdate");
//            String chequeDateStr = request.getParameter("chequeDate");
//            String currencyid = (request.getParameter("currencyid") == null ? sessionHandlerImpl.getCurrencyID(request) : request.getParameter("currencyid"));
//            String transactionAmountStr = request.getParameter("transactionAmount");
//            String paymentId = request.getParameter("transactionId");
//            String chequeNumber = request.getParameter("chequenumber");
//            String drawnOn = request.getParameter("drawnon");
//            String vendorId = request.getParameter("accountId");
//            double externalCurrencyRate = StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
//            boolean conversionRateFromCurrencyToBase = true;
//            if (request.getParameter("CurrencyToBaseExchangeRate") != null) {
//                conversionRateFromCurrencyToBase = Boolean.parseBoolean(request.getParameter("CurrencyToBaseExchangeRate"));
//            }
//
//            double exchangeRateForOpeningTransaction = 1;
//            if (!StringUtil.isNullOrEmpty(request.getParameter("exchangeRateForOpeningTransaction"))) {
//                exchangeRateForOpeningTransaction = Double.parseDouble(request.getParameter("exchangeRateForOpeningTransaction"));
//            }
//            
//            String paymentDetails = request.getParameter("paymentDetails");
//
//            String accountId = "";
//
//            if (!StringUtil.isNullOrEmpty(vendorId)) {
//                KwlReturnObject venresult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), vendorId);
//                Vendor vendor = (Vendor) venresult.getEntityList().get(0);
//                accountId = vendor.getAccount().getID();
//            }
//
//            Date transactionDate = df.parse(df.format(new Date()));
//            Date chequeDate = df.parse(df.format(new Date()));
//
//            if (!StringUtil.isNullOrEmpty(transactionDateStr)) {
//                transactionDate = df.parse(transactionDateStr);
//            }
//
//            if (!StringUtil.isNullOrEmpty(chequeDateStr)) {
//                chequeDate = df.parse(chequeDateStr);
//            }
//
//            double transactionAmount = 0d;
//            if (!StringUtil.isNullOrEmpty(transactionAmountStr)) {
//                transactionAmount = Double.parseDouble(transactionAmountStr);
//            }
//            if (!StringUtil.isNullOrEmpty(request.getParameter("memo"))) {
//                memo = request.getParameter("memo").toString();
//            }
//
//            // creating payment data
//
//            HashMap paymenthm = new HashMap();
//
//            /*
//             * as user can not chnaged entered number in edit so we have not
//             * cheked duplicate entry in edit. if this logic change we need to
//             * change here as well
//             */
//            if (StringUtil.isNullOrEmpty(paymentId)) {
////                result = accVendorPaymentobj.getPaymentFromNo(paymentNumber, companyid);
////                int count = result.getRecordTotalCount();
////                if (count > 0) {
////                    throw new AccountingException("Payment number '" + paymentNumber + "' already exists.");
////                }
//                paymenthm.put("entrynumber", paymentNumber);
//                paymenthm.put("autogenerated", false);
//                
//                /*
//                 * code for checking wheather entered number can be generated by
//                 * sequence format or not
//                 */
//               
//            }
//
//
//            if (!StringUtil.isNullOrEmpty(paymentId)) {
//                isEditInvoice = true;
//                auditMsg = "updated";
//                auditID = AuditAction.OPENING_BALANCE_UPDATED;
//                boolean isPaymentUsedInOtherTransactions = isPaymentUsedInOtherTransactions(paymentId, companyid);
//
//                if (isPaymentUsedInOtherTransactions) {
//                    throw new AccountingException(messageSource.getMessage("acc.nee.73", null, RequestContextUtils.getLocale(request)));
//                }
//
//                paymenthm.put("paymentid", paymentId);
//            }
//
//
//            paymenthm.put("depositamount", transactionAmount);//
//            paymenthm.put("currencyid", currencyid);//
//            paymenthm.put("externalCurrencyRate", externalCurrencyRate);//
//            paymenthm.put("memo", memo);//
//            paymenthm.put("companyid", companyid);//
//            paymenthm.put("chequeNumber", chequeNumber);
//            paymenthm.put("drawnOn", drawnOn);
//            paymenthm.put("creationDate", transactionDate);
//            paymenthm.put("chequeDate", chequeDate);
//            paymenthm.put("vendorId", vendorId);
//            paymenthm.put("accountId", accountId);
//            paymenthm.put("isOpeningBalencePayment", true);
//            paymenthm.put("normalPayment", false);
//            paymenthm.put("openingBalanceAmountDue", transactionAmount);
//            // Store Payment amount in base currency
//            if (conversionRateFromCurrencyToBase) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
//                paymenthm.put(Constants.originalOpeningBalanceBaseAmount, authHandler.round(transactionAmount*exchangeRateForOpeningTransaction, 2));
//                 paymenthm.put(Constants.openingBalanceBaseAmountDue, authHandler.round(transactionAmount*exchangeRateForOpeningTransaction, 2));
//            } else {
//                paymenthm.put(Constants.originalOpeningBalanceBaseAmount, authHandler.round(transactionAmount/exchangeRateForOpeningTransaction, 2));
//                paymenthm.put(Constants.openingBalanceBaseAmountDue, authHandler.round(transactionAmount/exchangeRateForOpeningTransaction, 2));
//            }
//            paymenthm.put("isadvancepayment", true);
////            paymenthm.put("contraentry", false);
//            paymenthm.put("exchangeRateForOpeningTransaction", exchangeRateForOpeningTransaction);
//            paymenthm.put("conversionRateFromCurrencyToBase", conversionRateFromCurrencyToBase);
//
//            String createdby = sessionHandlerImpl.getUserid(request);
//            String modifiedby = sessionHandlerImpl.getUserid(request);
//            long createdon = System.currentTimeMillis();
//            long updatedon = System.currentTimeMillis();
//
//            paymenthm.put("createdby", createdby);
//            paymenthm.put("modifiedby", modifiedby);
//            paymenthm.put("createdon", createdon);
//            paymenthm.put("updatedon", updatedon);
//
//            result = accVendorPaymentobj.savePayment(paymenthm);
//
//            Payment payment = (Payment) result.getEntityList().get(0);
//            returnList.add(payment.getPaymentNumber());
//            returnList.add(isEditInvoice);
//            
//            String customfield = request.getParameter("customfield");
//            if (!StringUtil.isNullOrEmpty(customfield)) {
//                JSONArray jcustomarray = new JSONArray(customfield);
//                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
//                customrequestParams.put("customarray", jcustomarray);
//                customrequestParams.put("modulename", Constants.Acc_OpeningBalanceMakePayment_modulename);
//                customrequestParams.put("moduleprimarykey", Constants.Acc_OpeningBalanceMakePaymentid);
//                customrequestParams.put("modulerecid", payment.getID());
//                customrequestParams.put("moduleid", Constants.Acc_Make_Payment_ModuleId);
//                customrequestParams.put("companyid", companyid);
//                customrequestParams.put("customdataclasspath",Constants.Acc_OpeningBalanceMakePayment_custom_data_classpath);
//                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
//                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
//                    paymenthm.put("paymentid", payment.getID());
//                    paymenthm.put("openingBalanceMakePaymentCustomData", payment.getID());
//                    result = accVendorPaymentobj.savePayment(paymenthm);
//                }
//            }
//            auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has  " + auditMsg + " a Opening Balance Make Payment" + paymentNumber, request, paymentNumber);
//        } catch (JSONException ex) {
//            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
//        } catch (ParseException ex) {
//            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
//        }
//        return returnList;
//    }

//    private boolean isPaymentUsedInOtherTransactions(String paymentId, String companyId) throws ServiceException {
//        boolean isPaymentUsedInOtherTransactions = false;
//
//        KwlReturnObject result;
//        if (!StringUtil.isNullOrEmpty(paymentId)) {
//            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Payment.class.getName(), paymentId);
//            Payment payment = (Payment) objItr.getEntityList().get(0);
//
//            if (payment != null) {
//                Set<PaymentDetail> paymentDetailSet = payment.getRows();
//                if (paymentDetailSet != null) {
//                    Iterator itr = paymentDetailSet.iterator();
//                    while (itr.hasNext()) {
//                        PaymentDetail row = (PaymentDetail) itr.next();
//                        if (row.getGoodsReceipt() != null) {
//                            isPaymentUsedInOtherTransactions = true;
//                        }
//                    }
//                }
//            }
//
//        }
//        return isPaymentUsedInOtherTransactions;
//    }

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
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Vendor");
        }
        return vendor;
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
        CsvReader csvReader=null;
        int total = 0, failed = 0;
        String companyid = sessionHandlerImpl.getCompanyid(request);
        String gcurrencyId = sessionHandlerImpl.getCurrencyID(request);
        String userId = sessionHandlerImpl.getUserid(request);
        String fileName = jobj.getString("filename");
        String masterPreference = request.getParameter("masterPreference");
        String delimiterType = request.getParameter("delimiterType");
        HashMap<String, FieldParams> customFieldParamMap = new HashMap<String, FieldParams>();
        KwlReturnObject resultObj = null;
        Payment payment = null;
        String customfield = "";
        DateFormat datef=authHandler.getDateOnlyFormat();
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
                        double exchangeRateForOpeningTransaction = 1;
                          
                        /*
                         * 1. Payment Number
                         */
                        String invoiceNumber = "";
                        if (columnConfig.containsKey("PaymentNumber")) {
                            invoiceNumber = recarr[(Integer) columnConfig.get("PaymentNumber")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(invoiceNumber)) {
                                throw new AccountingException("Empty data found in Transaction Number, cannot set empty data for Transaction Number.");
                            } else if (!transactionNumberSet.add(invoiceNumber)) {// this method retur true when added or false when already exit record not get added
                                throw new AccountingException("Duplicate Transaction Number '" + invoiceNumber + "' in file.");
                            } else {
                                KwlReturnObject result = accVendorPaymentobj.getPaymentFromNo(invoiceNumber, companyid);
                                int count = result.getRecordTotalCount();
                                if (count > 0) {
                                    throw new AccountingException(messageSource.getMessage("acc.field.Paymentnumber", null, RequestContextUtils.getLocale(request)) + invoiceNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                                }
                            }

                            JSONObject configObj = configMap.get("PaymentNumber");
                            int maxLength = configObj.optInt("maxLength", 0);
                            String validationType = configObj.optString("validatetype");
                            if ("string".equalsIgnoreCase(validationType) && !StringUtil.isNullOrEmpty(invoiceNumber) && invoiceNumber.length() > maxLength) {
                                if (masterPreference.equals(0)) {
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
                        
                        /*2. Vendor Code*/
                        String vendorCode="";
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
                        
                        /*3. Vendor Name
                         *if vendorID is empty it menas vendor is not found for given code. so need to serch data on name
                         */
                        String vendorName="";
                        if (StringUtil.isNullOrEmpty(vendorId)) {
                            if (columnConfig.containsKey("VendorName")) {
                                vendorName = recarr[(Integer) columnConfig.get("VendorName")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(vendorName)) {
                                    Vendor vendor=null;
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
                                    failureMsg+=messageSource.getMessage("acc.transactiondate.beforebbdate", null, RequestContextUtils.getLocale(request));
                                }
                                CompanyPreferencesCMN.checkLockPeriod(accCompanyPreferencesObj, requestParams, transactionDate, true);
                                } catch (ParseException ex) {
                                    failureMsg+="Incorrect date format for Transaction Date, Please specify values in " + dateFormat + " format.";
                                } catch (Exception ex){
                                    failureMsg+=ex.getMessage();
                                }
                            } else {
                                failureMsg+=messageSource.getMessage("acc.field.TransactionDateisnotavailable", null, RequestContextUtils.getLocale(request));
                            }
                        } else {
                           failureMsg+=messageSource.getMessage("acc.field.TransactionDateisnotavailable", null, RequestContextUtils.getLocale(request));
                        }

                        /*5. Payment Currency */
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
                                try {
                                    transactionAmount = Double.parseDouble(transactionAmountStr);
                                    if (transactionAmount <= 0) {
                                        failureMsg += "Amount can not be zero or negative.";
                                    }
                                } catch (NumberFormatException ex) {
                                    failureMsg += "Incorrect numeric value for Amount, Please ensure that value type of Amount matches with the Amount.";
                                }
                            }
                        } else {
                            failureMsg +=messageSource.getMessage("acc.field.TransactionAmountisnotavailable", null, RequestContextUtils.getLocale(request));
                        }
                        
                        /*7. Exchange Rate */
                        String exchangeRateForOpeningTransactionStr = "";
                        if (columnConfig.containsKey("ExchangeRateForOpeningTransaction")) {
                            exchangeRateForOpeningTransactionStr = recarr[(Integer) columnConfig.get("ExchangeRateForOpeningTransaction")].replaceAll("\"", "").trim();
                        } 
                        
                        if (!StringUtil.isNullOrEmpty(exchangeRateForOpeningTransactionStr)) {
                            try {
                                exchangeRateForOpeningTransaction = Double.parseDouble(exchangeRateForOpeningTransactionStr);
                                if (exchangeRateForOpeningTransaction <= 0) {
                                    failureMsg +=messageSource.getMessage("acc.field.ExchangeRateCannotbezeroornegative", null, RequestContextUtils.getLocale(request));
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
                            
                            String adate=datef.format(applyDate);
                            try{
                                applyDate=datef.parse(adate);
                            }catch(ParseException ex){
                                applyDate=cal.getTime();
                            }
                            
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
                        
                        /*
                         * 8. Payment Method
                         */
                        PaymentMethod payMethod = null;
                        if (columnConfig.containsKey("PaymentMethod")) {
                            String paymentMethodStr = recarr[(Integer) columnConfig.get("PaymentMethod")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(paymentMethodStr)) {
                                failureMsg += "Empty data found in Payment Method, cannot set empty data for Payment Method.";
                            } else {
                                KwlReturnObject retObj = accMasterItemsDAO.getPaymentMethodIdFromName(paymentMethodStr, companyid);
                                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                                    payMethod = (PaymentMethod) retObj.getEntityList().get(0);
                                }
                                if (payMethod == null) {
                                    failureMsg += "Payment Method entry not present in Currency list, Please create new Payment Method entry for "+paymentMethodStr+" as it requires some other details.";
                                }

                            }
                        } else {
                            failureMsg += "Payment Method is not available";
                        }
                        
                        
                        /*9. Cheque Number*/
                        String chequeNumber = "";
                        if (columnConfig.containsKey("ChequeNo")) {
                            chequeNumber = recarr[(Integer) columnConfig.get("ChequeNo")].replaceAll("\"", "").trim();
                            
                            JSONObject configObj = configMap.get("ChequeNo");
                            int maxLength = configObj.optInt("maxLength", 0);
                            String validationType = configObj.optString("validatetype");
                            if ("string".equalsIgnoreCase(validationType) && !StringUtil.isNullOrEmpty(chequeNumber) && chequeNumber.length() > maxLength) {
                                if (masterPreference.equalsIgnoreCase("0")) {
                                    failureMsg += "Data length greater than " + maxLength + " for column Cheque Number.";
                                } else {// for other two cases need to trim data upto max length
                                    chequeNumber = chequeNumber.substring(0, maxLength);
                                }
                            }
                        }
                        
                        /*10. Bank Name*/
                        String bankName = "";
                        String bankNameMasterItemID = "";
                        if (payMethod != null && payMethod.getDetailType() == PaymentMethod.TYPE_BANK) {
                            if (columnConfig.containsKey("BankName")) {
                                bankName = recarr[(Integer) columnConfig.get("BankName")].replaceAll("\"", "").trim();
                                if (StringUtil.isNullOrEmpty(bankName)) {
                                if(payMethod.getDetailType()==Constants.bank_detail_type){
                                        bankName = payMethod.getAccount().getAccountName();
                                    }
                                }
                            } else {
                                failureMsg += "Bank Name is not available";
                            }
                        }
                        
                        /*11. Cheque Date*/
                        Date chequeDate = null;
                        if (payMethod != null && payMethod.getDetailType() == PaymentMethod.TYPE_BANK) {
                            if (columnConfig.containsKey("DueDate")) {
                                String chequeDateStr = recarr[(Integer) columnConfig.get("DueDate")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(chequeDateStr)) {
                                    try {
                                        chequeDate = df.parse(chequeDateStr);
                                    } catch (ParseException ex) {
                                        if (masterPreference.equals("1")) {//add empty case or default case
                                            chequeDate = preferences.getBookBeginningFrom();// In UI default is book begining date so here taking book beging date as default date
                                        } else {
                                            failureMsg += "Incorrect date format for Cheque Date, Please specify values in " + dateFormat + " format.";
                                        }
                                    }
                                }
                            } else {
                                failureMsg += "Cheque Date is not available";
                            }
                        }
                        
                        /*
                         * 12. Payment Status
                         */
                        boolean cleared = false;
                        if (payMethod != null && payMethod.getDetailType() == PaymentMethod.TYPE_BANK) {
                            if (columnConfig.containsKey("PaymentStatus")) {
                                String paymentStatusStr = recarr[(Integer) columnConfig.get("PaymentStatus")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(paymentStatusStr) || payMethod ==null || (payMethod!=null && payMethod.getDetailType()==PaymentMethod.TYPE_CASH)) {
                                    cleared = false;
                                } else {
                                    if (!(paymentStatusStr.equalsIgnoreCase("Uncleared") || paymentStatusStr.equalsIgnoreCase("Cleared"))) {
                                        failureMsg += "Incorrect Payment Status type value for Payment Status. It should be either Cleared or Uncleared.";
                                    }
                                    if (paymentStatusStr.equalsIgnoreCase("Cleared")) {
                                        cleared = true;
                                    }
                                }
                            } else {
                                failureMsg += "Payment Status is not available";
                            }
                        }
                        
                         
                        /*
                         * 13. Clearence Date
                         */
                        Date clearenceDate = null;
                        if (columnConfig.containsKey("ClearenceDate")) {
                            if (payMethod.getDetailType() != PaymentMethod.TYPE_CASH && cleared) {// when payment type is other than cash and payment sttus is clear then only need of clerance date. So its validation
                                String clearenceDateStr = recarr[(Integer) columnConfig.get("ClearenceDate")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(clearenceDateStr)) {
                                    try {
                                        clearenceDate = df.parse(clearenceDateStr);
                                        if (chequeDate.compareTo(clearenceDate) > 0) {
                                            failureMsg += "Clearence date should be greter than Cheque date.";
                                        }
                                    } catch (ParseException ex) {
                                        failureMsg += "Incorrect date format for Clearence Date, Please specify values in " + dateFormat + " format.";
                                    }
                                } else {
                                    failureMsg += "You have entered the Payment Status as Cleared. So you cannot set empty data for Clearence Date.";
                                }
                            }
                        }

                        /*14. ReferenceNumber */
                        String referenceNumber = "";
                        if (columnConfig.containsKey("Description")) {
                            referenceNumber = recarr[(Integer) columnConfig.get("Description")].replaceAll("\"", "").trim();
                        }

                        /*
                         * 15. Memo
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
                                requestParamsCF.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Make_Payment_ModuleId, headArrayList.get(K)));
                                KwlReturnObject fieldParamsResult = accCommonTablesDAO.getFieldParams(requestParamsCF); // get custom field for module
                                FieldParams params = null;
                                if (fieldParamsResult.getRecordTotalCount() > 0) {
                                    params = (FieldParams) fieldParamsResult.getEntityList().get(0);
                                    customFieldParamMap.put(headArrayList.get(K).toString(), params);
                                }
                            }
                        }
                        
                        // creating payment data

                        HashMap paymenthm = new HashMap();
                        paymenthm.put("entrynumber", invoiceNumber);
                        paymenthm.put("autogenerated", false);
                        paymenthm.put("depositamount", transactionAmount);//
                        paymenthm.put("currencyid", currencyId);//
                        paymenthm.put("externalCurrencyRate", externalCurrencyRate);//
                        paymenthm.put("memo", memo);//
                        paymenthm.put("companyid", companyid);//
                        paymenthm.put("creationDate", transactionDate);
                        paymenthm.put("vendorId", vendorId);
                        paymenthm.put("accountId", accountId);
                        paymenthm.put("isOpeningBalencePayment", true);
                        paymenthm.put("normalPayment", false);
                        paymenthm.put("openingBalanceAmountDue", transactionAmount);
                        // Store Payment amount in base currency
                        paymenthm.put(Constants.openingBalanceBaseAmountDue, authHandler.round(transactionAmount*exchangeRateForOpeningTransaction, companyid));
                        paymenthm.put(Constants.originalOpeningBalanceBaseAmount, authHandler.round(transactionAmount*exchangeRateForOpeningTransaction, companyid));
                        paymenthm.put("isadvancepayment", true);
                        //            paymenthm.put("contraentry", false);
                        paymenthm.put("exchangeRateForOpeningTransaction", exchangeRateForOpeningTransaction);
                        paymenthm.put("conversionRateFromCurrencyToBase", true);
                        String createdby = sessionHandlerImpl.getUserid(request);
                        String modifiedby = sessionHandlerImpl.getUserid(request);
                        long createdon = System.currentTimeMillis();
                        long updatedon = System.currentTimeMillis();

                        paymenthm.put("createdby", createdby);
                        paymenthm.put("modifiedby", modifiedby);
                        paymenthm.put("createdon", createdon);
                        paymenthm.put("updatedon", updatedon);
                        
                         HashMap pdetailhm = new HashMap();
                        pdetailhm.put("paymethodid", payMethod.getID());
                        pdetailhm.put("companyid", companyid);

                        if (payMethod.getDetailType()!= PaymentMethod.TYPE_CASH) {
                            if (payMethod.getDetailType() == PaymentMethod.TYPE_BANK) {
                                HashMap chequehm = new HashMap();
                                chequehm.put("chequeno", chequeNumber);
                                chequehm.put("companyId", companyid);
                                chequehm.put("createdFrom", 2);
                                chequehm.put("bankAccount", (payMethod.getAccount() != null) ? payMethod.getAccount().getID() : "");
                                chequehm.put("description", referenceNumber);
                                chequehm.put("bankname", bankName);
                                chequehm.put("duedate",chequeDate);
                                chequehm.put("bankmasteritemid", bankNameMasterItemID);
                                KwlReturnObject cqresult = accPaymentDAOobj.addCheque(chequehm);
                                Cheque cheque = (Cheque) cqresult.getEntityList().get(0);
                                pdetailhm.put("chequeid", cheque.getID());
                            } 
                        }
                        KwlReturnObject pdresult = null;

                        pdresult = accPaymentDAOobj.addPayDetail(pdetailhm);
                        PayDetail pdetail = (PayDetail) pdresult.getEntityList().get(0);

                        paymenthm.put("paydetailsid", pdetail.getID());
                        resultObj = accVendorPaymentobj.savePayment(paymenthm);
                        payment = (Payment) resultObj.getEntityList().get(0);
                        
                        // For creating custom field array
                        JSONArray customJArr = fieldDataManagercntrl.getCustomFieldForOeningTransactionsRecords(headArrayList, customFieldParamMap, recarr,columnConfig,request);
                        customfield = customJArr.toString();
                        if (!StringUtil.isNullOrEmpty(customfield)) {
                            JSONArray jcustomarray = new JSONArray(customfield);
                            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                            customrequestParams.put("customarray", jcustomarray);
                            customrequestParams.put("modulename", Constants.Acc_OpeningBalanceMakePayment_modulename);
                            customrequestParams.put("moduleprimarykey", Constants.Acc_OpeningBalanceMakePaymentid);
                            customrequestParams.put("modulerecid", payment.getID());
                            customrequestParams.put("moduleid", Constants.Acc_Make_Payment_ModuleId);
                            customrequestParams.put("companyid", companyid);
                            customrequestParams.put("customdataclasspath", Constants.Acc_OpeningBalanceMakePayment_custom_data_classpath);
                            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                paymenthm.put("paymentid", payment.getID());
                                paymenthm.put("openingBalanceMakePaymentCustomData", payment.getID());
                                KwlReturnObject result = accVendorPaymentobj.savePayment(paymenthm);
                            }
                        }
                        
                         if (cleared) {
                            Date startDate = preferences.getFinancialYearFrom();
                            Calendar startCal = Calendar.getInstance();
                            startCal.setTime(startDate);
                            startCal.add(Calendar.YEAR, 1);
                            startCal.add(Calendar.DAY_OF_YEAR, -1);
                            Date endDate = startCal.getTime();
                            String bankAccountId = payMethod.getAccount().getID();
                            
                            Map<String, Object> bankReconsilationMap = new HashMap<>();
                            bankReconsilationMap.put("isOpeningPayment", true);
                            bankReconsilationMap.put("bankAccountId", bankAccountId);
                            bankReconsilationMap.put("startDate", startDate); //Financial Year Start Date
                            bankReconsilationMap.put("endDate", endDate); //Financial Year End Date
                            bankReconsilationMap.put("clearanceDate", clearenceDate);
                            bankReconsilationMap.put("endingAmount", 0.0);
                            bankReconsilationMap.put("companyId", companyid);
                            bankReconsilationMap.put("clearingamount", payment.getDepositAmount());
                            bankReconsilationMap.put("currencyid", currencyId);
                            bankReconsilationMap.put("details", "");
                            bankReconsilationMap.put("payment", payment);
                            bankReconsilationMap.put("ismultidebit", true);
                            bankReconsilationMap.put("createdby", sessionHandlerImpl.getUserid(request));
                            bankReconsilationMap.put("checkCount", 1);      //As the discussion with Mayur B. and Sagar A. sir MP relates to check count
                            bankReconsilationMap.put("depositeCount", 0);

                            HashMap<String, Object> globalParams = AccountingManager.getGlobalParams(request);
                            saveBankReconsilation(bankReconsilationMap, globalParams);
                            auditTrailObj.insertAuditLog(AuditAction.BANK_RECONCILIATION_ADDED, "User " + sessionHandlerImpl.getUserFullName(request) + " has reconciled " + payment.getPaymentNumber(), request, companyid);
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

            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
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
                logDataMap.put("Module", Constants.Acc_Make_Payment_ModuleId);
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userId);
                logDataMap.put("Company", companyid);
                importDao.saveImportLog(logDataMap);
                txnManager.commit(lstatus);
            } catch (Exception ex) {
                txnManager.rollback(lstatus);
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", getActualFileName(fileName));
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
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

    public List savePayment(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException {
        KwlReturnObject result = null;
        List list = new ArrayList();
        Payment payment = null;
        String oldjeid = null;
        String taxjedid = null;
        String Cardid = null;
        String oldChequeNo = "";
        double amount = 0;
        String netinword = "";
        List ll = new ArrayList();
        Invoice invoice = null;
        Customer cust = null;
        Vendor vend = null;
        StringBuffer billno = new StringBuffer();
        String person = "";
        boolean isCNDN;
        String chequeno="";
        try {
            Account dipositTo = null;

            double amountDiff = 0;
            boolean rateDecreased = false;
            String sequenceformat = request.getParameter("sequenceformat")!=null ? request.getParameter("sequenceformat") : "";
            String companyid = sessionHandlerImpl.getCompanyid(request);
            double externalCurrencyRate = request.getParameter("externalcurrencyrate")!=null ? StringUtil.getDouble(request.getParameter("externalcurrencyrate")) : 0.0;
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            String drAccDetails = request.getParameter("detail");
            String customfield = request.getParameter("customfield");
            String advancePaymentIdForCnDn = request.getParameter("advancePaymentIdForCnDn");
            String mainPaymentForCNDNId = request.getParameter("mainPaymentForCNDNId");
            String invoiceadvcndntype = request.getParameter("invoiceadvcndntype");
            int receiptType = StringUtil.getInteger(request.getParameter("receipttype"));
            int actualReceiptType = StringUtil.getInteger(request.getParameter("actualReceiptType") != null ? request.getParameter("actualReceiptType") : "0");
            boolean isMultiDebit = StringUtil.getBoolean(request.getParameter("ismultidebit"));
            isCNDN = StringUtil.getBoolean(request.getParameter("isCNDN"));
            boolean isAgainstDN = StringUtil.getBoolean(request.getParameter("isAgainstDN"));
            boolean isAdvancePayment = StringUtil.getBoolean(request.getParameter("isadvpayment"));
            boolean ignoreDuplicateChk = StringUtil.getBoolean(request.getParameter("ignoreDuplicateChk"));
            String entryNumber = request.getParameter("no");
            String receiptid = request.getParameter("billid");
            boolean isVendorPaymentEdit = (Boolean.parseBoolean((String) request.getParameter("isReceiptEdit")));
            String methodid = request.getParameter("pmtmethod");
            sessionHandlerImpl.updatePaymentMethodID(request, methodid);

            boolean isIBGTypeTransaction = StringUtil.getBoolean(request.getParameter("isIBGTypeTransaction"));
            String ibgDetailsID = request.getParameter("ibgDetailsID");
            String ibgCode = request.getParameter("ibgCode");
            Date creationDate = df.parse(request.getParameter("creationdate"));

            String jeid = null;
            String payDetailID = null;
            HashMap<String, JSONArray> Map1 = new HashMap();
            String jeentryNumber = null;
            boolean jeautogenflag = false;
            String jeIntegerPart = "";
            String jeDatePrefix = "";
            String jeDateAfterPrefix = "";
            String jeDateSuffix = "";
            String jeSeqFormatId = "";

            HashMap paymenthm = new HashMap();
            boolean ismanydbcr = StringUtil.getBoolean(request.getParameter("ismanydbcr"));
            paymenthm.put("ismanydbcr", ismanydbcr);
            paymenthm.put("isadvancepayment", isAdvancePayment);
            if (!StringUtil.isNullOrEmpty(advancePaymentIdForCnDn)) {
                paymenthm.put("advancePaymentIdForCnDn", advancePaymentIdForCnDn);
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("isEdit"))) {
                paymenthm.put("isEdit", Boolean.parseBoolean(request.getParameter("isEdit")));
            }
            if (!StringUtil.isNullOrEmpty(mainPaymentForCNDNId)) {
                paymenthm.put("mainPaymentForCNDNId", mainPaymentForCNDNId);
            }
            if (!StringUtil.isNullOrEmpty(invoiceadvcndntype) && (actualReceiptType == 0 || actualReceiptType == 1)) {
                paymenthm.put("invoiceadvcndntype", Integer.parseInt(invoiceadvcndntype));
            }

            HashMap<Integer, String> paymentHashMap = new HashMap<Integer, String>();
            if (!StringUtil.isNullOrEmpty(request.getParameter("datainvoiceadvcndn"))) {
                JSONArray jSONArray = new JSONArray(request.getParameter("datainvoiceadvcndn"));
                String paymentid = "";
                for (int i = 0; i < jSONArray.length(); i++) {
                    JSONObject jObject = jSONArray.getJSONObject(i);
                    paymentid = StringUtil.DecodeText(jObject.optString("paymentID"));
                    int invoiceadvcndntypejson = !StringUtil.isNullOrEmpty(jObject.getString("invoiceadvcndntype")) ? Integer.parseInt(jObject.getString("invoiceadvcndntype")) : 0;
                    paymentHashMap.put(invoiceadvcndntypejson, paymentid);
                }
                paymenthm.put("paymentHashMap", paymentHashMap);
            }
            paymenthm.put("receipttype", receiptType);
            paymenthm.put("actualReceiptType", actualReceiptType);

            if (isIBGTypeTransaction) {
                paymenthm.put("isIBGTypeTransaction", isIBGTypeTransaction);
                paymenthm.put("ibgDetailsID", ibgDetailsID);
                paymenthm.put("ibgCode", ibgCode);
            }

            double bankCharges = 0;
            double bankInterest = 0;
            boolean onlyAdvance = StringUtil.getBoolean(request.getParameter("onlyAdvance"));
            String bankChargesAccid = request.getParameter("bankChargesCmb");
            String bankInterestAccid = request.getParameter("bankInterestCmb");
            if (!StringUtil.isNullOrEmpty(request.getParameter("bankCharges")) && !StringUtil.isNullOrEmpty(bankChargesAccid)) {
                bankCharges = Double.parseDouble(request.getParameter("bankCharges"));
                paymenthm.put("bankCharges", bankCharges);
                paymenthm.put("bankChargesCmb", bankChargesAccid);
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("bankInterest")) && !StringUtil.isNullOrEmpty(bankInterestAccid)) {
                bankInterest = Double.parseDouble(request.getParameter("bankInterest"));
                paymenthm.put("bankInterest", bankInterest);
                paymenthm.put("bankInterestCmb", bankInterestAccid);
            }
            String paidToid = request.getParameter("paidToCmb");
            if (!StringUtil.isNullOrEmpty(request.getParameter("paidToCmb")) && !StringUtil.isNullOrEmpty(paidToid)) {
                paymenthm.put("paidToCmb", paidToid);
            }
            if (receiptType == 6) {
                paymenthm.put("customer", request.getParameter("accid"));
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
                    paymenthm.put("customer", request.getParameter("accid"));
                } else if (isVendor) {
                    paymenthm.put("vendorId", request.getParameter("accid"));
                }
            }
            String createdby = sessionHandlerImpl.getUserid(request);
            String modifiedby = sessionHandlerImpl.getUserid(request);
            long createdon = System.currentTimeMillis();
            long updatedon = System.currentTimeMillis();
            boolean bankReconsilationEntry = false, bankPayment = false;
            Date clearanceDate = null, startDate = null, endDate = null;
            String bankAccountId = "";
            Map<String, Object> bankReconsilationMap = new HashMap<String, Object>();

            boolean editAdvance = false;

            if (!StringUtil.isNullOrEmpty(receiptid)) {// for edit case
                KwlReturnObject receiptObj = accountingHandlerDAOobj.getObject(Payment.class.getName(), receiptid);
                payment = (Payment) receiptObj.getEntityList().get(0);
                oldjeid = payment.getJournalEntry().getID();
                JournalEntry jetemp = payment.getJournalEntry();
                if (jetemp != null) {
                    jeentryNumber = jetemp.getEntryNumber(); //preserving these data to generate same JE number in edit case                    
                    jeautogenflag = jetemp.isAutoGenerated();
                    jeSeqFormatId = jetemp.getSeqformat() == null ? "" : jetemp.getSeqformat().getID();
                    jeIntegerPart = String.valueOf(jetemp.getSeqnumber());
                    jeDatePrefix = jetemp.getDatePreffixValue();
                    jeDateAfterPrefix = jetemp.getDateAfterPreffixValue();
                    jeDateSuffix = jetemp.getDateSuffixValue();
                }

                if (payment.getPayDetail() != null) {
                    payDetailID = payment.getPayDetail().getID();
                    if (payment.getPayDetail().getCard() != null) {
                        Cardid = payment.getPayDetail().getCard().getID();
                    }
                    if (payment.getPayDetail().getCheque() != null) {
                        Cardid = payment.getPayDetail().getCheque().getID();
                        oldChequeNo = payment.getPayDetail().getCheque().getChequeNo();
                    }
                }
                if (payment != null) {
                    updateOpeningBalance(payment, companyid);
                }
                result = accVendorPaymentobj.deletePaymentsDetails(receiptid, companyid);
                result = accVendorPaymentobj.deletePaymentsDetailsOtherwise(receiptid);
                if (payment != null && payment.getRevalJeId() != null) {
                    result = accJournalEntryobj.deleteJEDtails(payment.getRevalJeId(), companyid);
                    result = accJournalEntryobj.deleteJE(payment.getRevalJeId(), companyid);
                }

                paymenthm.put("deposittojedetailid", null);
                paymenthm.put("depositamount", 0.0);
                //Delete old entries and insert new entries again from optimized table in edit case.
                accJournalEntryobj.deleteOnEditAccountJEs_optimized(oldjeid);
            }

            synchronized (this) { //this block is used to generate auto sequence number if number is not duplicate
                SequenceFormat prevSeqFormat = null;
                String prevseqnumber = "";
                String nextAutoNo = "";
                String nextAutoNoInt = "";
                String datePrefix = "";
                String dateafterPrefix = "";
                String dateSuffix = "";

                if (!StringUtil.isNullOrEmpty(receiptid)) {// for edit case
                    String advanceId = payment.getAdvanceid() != null ? payment.getAdvanceid().getID() : "";
                    result = accVendorPaymentobj.getDuplicatePNforNormal(entryNumber, companyid, receiptid, advanceId, payment);
                    int count = result.getRecordTotalCount();
                    if (count > 0 && sequenceformat.equals("NA")) {
                        throw new AccountingException(messageSource.getMessage("acc.field.Paymentnumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                    } else if (payment != null && payment.getSeqformat() != null) {
                        prevSeqFormat = payment.getSeqformat();
                        prevseqnumber = payment.getSeqnumber() + "";
                        paymenthm.put(Constants.SEQFORMAT, prevSeqFormat.getID());
                        paymenthm.put(Constants.SEQNUMBER, prevseqnumber);
                        paymenthm.put(Constants.DATEPREFIX, payment.getDatePreffixValue());
                        paymenthm.put(Constants.DATEAFTERPREFIX, payment.getDateAfterPreffixValue());
                        paymenthm.put(Constants.DATESUFFIX, payment.getDateSuffixValue());
                        nextAutoNo = entryNumber;
                    }
                } else if (paymentHashMap.containsKey(3) && !paymentHashMap.containsKey(1)) {
                    String cndnId = paymentHashMap.get(3);
                    KwlReturnObject cndnresult = accountingHandlerDAOobj.getObject(Payment.class.getName(), cndnId);
                    Payment paymentCnDn = null;
                    int count = 0;
                    if (!cndnresult.getEntityList().isEmpty() && cndnresult.getEntityList().get(0) != null) {
                        paymentCnDn = (Payment) cndnresult.getEntityList().get(0);
                        result = accVendorPaymentobj.getDuplicatePNforNormal(entryNumber, companyid, receiptid, cndnId, paymentCnDn);
                        count = result.getRecordTotalCount();
                    }
                    if (count > 0 && sequenceformat.equals("NA")) {
                        throw new AccountingException(messageSource.getMessage("acc.field.Paymentnumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                    } else if (paymentCnDn != null && paymentCnDn.getSeqformat() != null) {
                        prevSeqFormat = paymentCnDn.getSeqformat();
                        prevseqnumber = paymentCnDn.getSeqnumber() + "";
                        paymenthm.put(Constants.SEQFORMAT, prevSeqFormat.getID());
                        paymenthm.put(Constants.SEQNUMBER, prevseqnumber);
                        paymenthm.put(Constants.DATEPREFIX, paymentCnDn.getDatePreffixValue());
                        paymenthm.put(Constants.DATEAFTERPREFIX, paymentCnDn.getDateAfterPreffixValue());
                        paymenthm.put(Constants.DATESUFFIX, paymentCnDn.getDateSuffixValue());
                        nextAutoNo = entryNumber;
                    }
                } else if (!ignoreDuplicateChk && (actualReceiptType != 0 || (actualReceiptType == 0 && isAdvancePayment))) {   //true when advance created along with payment against invoice
                    if (actualReceiptType == 0 && isAdvancePayment && request.getParameter("data") != null) {
                        JSONArray jSONArray = new JSONArray(request.getParameter("data"));
                        JSONObject jSONObject = jSONArray.getJSONObject(0);
                        String advanceReceiptId = jSONObject.optString("billid", "");
                        if (!StringUtil.isNullOrEmpty(advanceReceiptId)) {
                            KwlReturnObject receiptObj = accountingHandlerDAOobj.getObject(Payment.class.getName(), advanceReceiptId);
                            Payment advpayment = (Payment) receiptObj.getEntityList().get(0);
                            if (advpayment != null && advpayment.getSeqformat() != null) {
                                prevSeqFormat = advpayment.getSeqformat();
                                prevseqnumber = advpayment.getSeqnumber() + "";
                                paymenthm.put(Constants.SEQFORMAT, prevSeqFormat.getID());
                                paymenthm.put(Constants.SEQNUMBER, prevseqnumber);
                                paymenthm.put(Constants.DATEPREFIX, advpayment.getDatePreffixValue());
                                paymenthm.put(Constants.DATEAFTERPREFIX, advpayment.getDateAfterPreffixValue());
                                paymenthm.put(Constants.DATESUFFIX, advpayment.getDateSuffixValue());
                                nextAutoNo = entryNumber;

                                JournalEntry entry = advpayment.getJournalEntry();
                                jeentryNumber = entry.getEntryNumber(); //preserving these data to generate same JE number in edit case                    
                                jeautogenflag = entry.isAutoGenerated();
                                jeSeqFormatId = entry.getSeqformat() == null ? "" : entry.getSeqformat().getID();
                                jeIntegerPart = String.valueOf(entry.getSeqnumber());
                                jeDatePrefix = entry.getDatePreffixValue();
                                jeDateAfterPrefix = entry.getDateAfterPreffixValue();
                                jeDateSuffix = entry.getDateSuffixValue();
                                editAdvance = true;
                            }
                            result = accVendorPaymentobj.getDuplicatePNforNormal(entryNumber, companyid, advanceReceiptId, "", advpayment);
                        }
                    } else {
                        result = accVendorPaymentobj.getPaymentFromNo(entryNumber, companyid);
                    }
                    int count = result.getRecordTotalCount();
                    if (count > 0 && sequenceformat.equals("NA")) {
                        throw new AccountingException(messageSource.getMessage("acc.field.Paymentnumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                    }
                }

                if (sequenceformat.equals("NA")) {//In case of NA checks wheather this number can also be generated by a sequence format or not
                    List resultList = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Make_Payment_ModuleId, entryNumber, companyid);
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
                        nextAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_PAYMENT, sequenceformat);
                    } else {
                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_PAYMENT, sequenceformat, seqformat_oldflag, creationDate);
                        nextAutoNo = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                        nextAutoNoInt = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                        datePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                        dateafterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                        dateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                        paymenthm.put(Constants.SEQFORMAT, sequenceformat);
                        paymenthm.put(Constants.SEQNUMBER, nextAutoNoInt);
                        paymenthm.put(Constants.DATEPREFIX, datePrefix);
                        paymenthm.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
                        paymenthm.put(Constants.DATESUFFIX, dateSuffix);
                    }
                    entryNumber = nextAutoNo;
                }
                if (!sequenceformat.equals("NA") && ignoreDuplicateChk) {//case of creating advance with normal
                    result = accVendorPaymentobj.getCurrentSeqNumberForAdvance(sequenceformat, companyid);
                    nextAutoNoInt = !(result.getEntityList().isEmpty()) ? (result.getEntityList().get(0) + "") : "0";
                    paymenthm.put(Constants.SEQNUMBER, nextAutoNoInt);
                    paymenthm.put(Constants.SEQFORMAT, sequenceformat);
                    nextAutoNo = entryNumber;
                }
                paymenthm.put("entrynumber", entryNumber);
                paymenthm.put("autogenerated", entryNumber.equals(nextAutoNo));
            }


            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            currencyid = (request.getParameter("currencyid") == null ? currency.getCurrencyID() : request.getParameter("currencyid"));

            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);

            KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);

            int countryLanguageId = Constants.OtherCountryLanguageId; // 0
            if (extraCompanyPreferences.isAmountInIndianWord()) {
                countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
            }
            
            paymenthm.put("currencyid", currencyid);
            paymenthm.put("externalCurrencyRate", externalCurrencyRate);
            paymenthm.put("isLinkedToClaimedInvoice", !StringUtil.isNullOrEmpty(request.getParameter("isLinkedToClaimedInvoice")) ? Boolean.parseBoolean(request.getParameter("isLinkedToClaimedInvoice")):false);
            result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), request.getParameter("pmtmethod"));
            PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);

            dipositTo = payMethod.getAccount();
            HashMap pdetailhm = new HashMap();
            pdetailhm.put("paymethodid", payMethod.getID());
            pdetailhm.put("companyid", companyid);
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

                    BigInteger nextSeqNumber = new BigInteger("0");
                    boolean checkForNextSequenceNumberAlso = true;
                    boolean isChequeNumberInString = false;
                    if (extraCompanyPreferences != null && extraCompanyPreferences.isShowAutoGeneratedChequeNumber()) {
                        try {// USER can enter String values also in such case exception will come

                            nextSeqNumber = new BigInteger(obj.getString("chequeno"));
                            // cheque whether Cheque Number exist or not if already exist then don't let it save

//                            if (extraCompanyPreferences.isShowAutoGeneratedChequeNumber() && nextSeqNumber.equals(0)) {
//                                throw new AccountingException("Cheque Number can not be zero");
//                            }
                        } catch (Exception ex) {
                            checkForNextSequenceNumberAlso = false;
                            isChequeNumberInString = true;
                        }
                    } else {
                        checkForNextSequenceNumberAlso = false;
                    }

                    boolean isChequeNumberAvailable = false;

                    boolean isEditCaseButChqNoChanged = false;
                    if (!StringUtil.isNullOrEmpty(obj.optString("chequeno")) && extraCompanyPreferences != null && extraCompanyPreferences.isShowAutoGeneratedChequeNumber()) {
                        try {// OLD CHQ NO. can be String value also in such case exception will come

                            HashMap chequeNohm = new HashMap();
                            chequeNohm.put("companyId", companyid);
                            chequeNohm.put("sequenceNumber", nextSeqNumber);
                            chequeNohm.put("checkForNextSequenceNumberAlso", checkForNextSequenceNumberAlso);
                            chequeNohm.put("nextChequeNumber", obj.optString("chequeno"));
                            chequeNohm.put("bankAccountId", payMethod.getAccount().getID());
                            isChequeNumberAvailable = paymentService.isChequeNumberAvailable(chequeNohm);

                            BigInteger oldChqNoIntValue = new BigInteger("0");
                            if (!StringUtil.isNullOrEmpty(oldChequeNo)) {
                                oldChqNoIntValue = new BigInteger(oldChequeNo);
                            }


                            if (!oldChqNoIntValue.equals(nextSeqNumber)) {
                                isEditCaseButChqNoChanged = true;
                            }

                            if (isChequeNumberInString) {
                                if (!oldChequeNo.equals(obj.optString("chequeno"))) {
                                    isEditCaseButChqNoChanged = true;
                                }
                            }

                        } catch (Exception ex) {
                            if (!oldChequeNo.equals(obj.optString("chequeno"))) {
                                isEditCaseButChqNoChanged = true;
                            }
                        }
                    } else {
                        if (!oldChequeNo.equals(obj.optString("chequeno"))) {
                            isEditCaseButChqNoChanged = true;
                        }
                    }
                    

                    if (!StringUtil.isNullOrEmpty(obj.optString("chequeno")) && isChequeNumberAvailable && isEditCaseButChqNoChanged) {
                        throw new AccountingException("Cheque Number : <b>" + obj.getString("chequeno") + "</b> is already exist, Please enter different one");
                    }

                    chequehm.put("chequeno", obj.optString("chequeno"));
                    chequehm.put("companyId", companyid);
                    chequehm.put("createdFrom", 1);
                    chequehm.put("sequenceNumber", nextSeqNumber);
                    Map<String, Object> seqchequehm = new HashMap<>();
                    obj.put(Constants.companyKey, companyid);
                    String chequesequenceformat =  obj.optString("sequenceformat");
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

                    String autoGenNextChqNo = paymentService.getNextChequeNumber(request, payMethod.getAccount().getID());
                    if (!StringUtil.isNullOrEmpty(obj.optString("chequeno")) && (autoGenNextChqNo.equals(obj.optString("chequeno")) || oldChequeNo.equals(obj.optString("chequeno")))) {// if cheque Number is auto Generated OR for edit case if old cheque no. == coming chequenumber
                        chequehm.put("isAutoGeneratedChequeNumber", true);
                    }

                    chequehm.put("bankAccount", (payMethod.getAccount() != null) ? payMethod.getAccount().getID() : "");
                    chequehm.put("description", StringUtil.DecodeText(obj.optString("description")));
                    chequehm.put("bankname", StringUtil.DecodeText(obj.optString("bankname")));
                    chequehm.put("duedate", df.parse(obj.getString("payDate")));
                    KwlReturnObject cqresult = accPaymentDAOobj.addCheque(chequehm);
                    Cheque cheque = (Cheque) cqresult.getEntityList().get(0);
                    pdetailhm.put("chequeid", cheque.getID());
                    chequeno=cheque.getChequeNo();
                } else if (payMethod.getDetailType() == PaymentMethod.TYPE_CARD) {
                    HashMap cardhm = new HashMap();
                    cardhm.put("cardno", obj.getString("cardno"));
                    cardhm.put("nameoncard", obj.getString("nameoncard"));
                    //cardhm.put("expirydate", obj.getString("expirydate"));
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
            paymenthm.put("paydetailsid", pdetail.getID());
            paymenthm.put("memo", request.getParameter("memo"));
            paymenthm.put("companyid", companyid);

            if (StringUtil.isNullOrEmpty(oldjeid) && !editAdvance) {
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
            jeDataMap.put("currencyid", currencyid);
            Set jedetails = new HashSet();
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
                    double amountDiffforInv = oldPaymentRowsAmount(request, jArray, currencyid, externalCurrencyRate);
                    rateDecreased = false;
                    if (amountDiffforInv < 0) {
                        rateDecreased = true;
                    }
                    amount += jobj.getDouble("payment");
                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", companyid);
//                    jedjson.put("amount", rateDecreased?(jobj.getDouble("payment") - amountDiffforInv):(jobj.getDouble("payment") + amountDiffforInv));
                    jedjson.put("amount", jobj.getDouble("payment") - amountDiffforInv);
                    jedjson.put("accountid", jobj.get("accountid"));
                    jedjson.put("gstCurrencyRate",jobj.optDouble("gstCurrencyRate",0.0));
                    if(jobj.optDouble("gstCurrencyRate",0.0)!=0.0)
                        jeDataMap.put("gstCurrencyRate", jobj.optDouble("gstCurrencyRate",0.0));
                    jedjson.put("forexGainLoss",amountDiffforInv);
                    jedjson.put("debit", true);
                    jedjson.put("jeid", jeid);
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                    if ((!jobj.getString("billno").equalsIgnoreCase("undefined")) && (!jobj.getString("billno").equalsIgnoreCase(""))) {
                        billno.append(jobj.getString("billno") + ",");
                    }
                    jArr.getJSONObject(i).put("rowjedid", jed.getID());

                    if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", ""))) {
                        JSONArray jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
                        Map1.put(jed.getID(), jcustomarray);
                    }
                }
                amountDiff = oldPaymentRowsAmount(request, jArr, currencyid, externalCurrencyRate);
                if (preferences.getForeignexchange() == null) {
                    throw new AccountingException(messageSource.getMessage("acc.common.forex", null, RequestContextUtils.getLocale(request)));
                }
                if (amountDiff != 0 && preferences.getForeignexchange() != null && Math.abs(amountDiff) >= 0.000001) {//Math.abs(amountDiff) < .0000001 Added this because in case of revaluation the diff in the rates was less than .0000001 and the amount calculation was going in Exachange rate account. 
                    rateDecreased = false;
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
            JournalEntryDetail JEdeatilId = null;
            boolean otherwiseFlag = false;
            boolean taxExist = false;
            List payentOtherwiseList = new ArrayList();
            HashMap paymentdetailotherwise = new HashMap();
            if (isMultiDebit) {
                JSONArray drAccArr = new JSONArray(drAccDetails);
                for (int i = 0; i < drAccArr.length(); i++) {
                    JSONObject jobj = drAccArr.getJSONObject(i);
                    boolean isdebit = jobj.has("isdebit") ? Boolean.parseBoolean(jobj.getString("isdebit")) : true;
//                    int advanceamounttype=request.getParameter("advanceamounttype") != null ? Integer.parseInt(request.getParameter("advanceamounttype")) : 0;
//                    if(isAdvancePayment && advanceamounttype > 0 && company.getCountry().getID().equals("137")){ //if advance and Singapoor Country
//                        double totalAmount=Double.parseDouble(jobj.getString("dramount"));
//                       double typeAmountTax=0;
//                        String accountID="";
//                       if(advanceamounttype ==1){//for Local Tax
//                            double percentage=6.0;
//                             KwlReturnObject ObjReturnObject = accAccountDAOobj.getAccountFromName(companyid, "SR");
//                             Account account =(Account)ObjReturnObject.getEntityList().get(0);
//                             if(account!=null)accountID=account.getID();
//                              typeAmountTax=(totalAmount*percentage)/(100+percentage);
//                        }else if(advanceamounttype ==2){//For Export Tax
//                            
//                            KwlReturnObject ObjReturnObject = accAccountDAOobj.getAccountFromName(companyid, "ZRE");
//                            Account account =(Account)ObjReturnObject.getEntityList().get(0);
//                            if(account!=null)accountID=account.getID();
//                            typeAmountTax=0;
//                        }
//                        jedjson = new JSONObject();
//                        jedjson.put("srno", jedetails.size() + 1);
//                        jedjson.put("companyid", companyid);
//                        jedjson.put("amount",(totalAmount-typeAmountTax));
//                        jedjson.put("accountid", jobj.getString("accountid"));
//                        jedjson.put("debit", isdebit);//true);
//                        jedjson.put("jeid", jeid);
//                        jedjson.put("description", jobj.getString("description"));
//                        jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
//                        JEdeatilId = (JournalEntryDetail) jedresult.getEntityList().get(0);
//                        jedetails.add(JEdeatilId);
//                        
//                        jedjson = new JSONObject();
//                        jedjson.put("srno", jedetails.size() + 1);
//                        jedjson.put("companyid", companyid);
//                        jedjson.put("amount", typeAmountTax);
//                        jedjson.put("accountid", jobj.getString("accountid"));
//                        jedjson.put("debit", isdebit);//true);
//                        jedjson.put("jeid", jeid);
//                        jedjson.put("description", jobj.getString("description"));
//                        jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
//                        JEdeatilId = (JournalEntryDetail) jedresult.getEntityList().get(0);
//                        jedetails.add(JEdeatilId);
//                        
//                    }else{//Normal Flow
                        
                        jedjson = new JSONObject();
                        jedjson.put("srno", jedetails.size() + 1);
                        jedjson.put("companyid", companyid);
                        jedjson.put("amount", Double.parseDouble(jobj.getString("dramount")));
                        jedjson.put("accountid", jobj.getString("accountid"));
                        jedjson.put("debit", isdebit);//true);
                        jedjson.put("jeid", jeid);
                        jedjson.put("description",StringUtil.DecodeText(jobj.optString("description")));
                        jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        JEdeatilId = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jedetails.add(JEdeatilId);                        
//                    }                
                    if ((!jobj.getString("billno").equalsIgnoreCase("undefined")) && (!jobj.getString("billno").equalsIgnoreCase(""))) {
                        billno.append(jobj.getString("billno") + ",");
                    }
                    double rowtaxamount = 0;
                    //taxExist=StringUtil.isNullOrEmpty(rowtaxid)?false:true;
                    if (receiptType == 2 || (!isVendorPaymentEdit && receiptType == 9) || (isVendorPaymentEdit && (receiptType == 1 || receiptType == 9) && (actualReceiptType == 9 || actualReceiptType == 2))) {
                        PaymentDetailOtherwise paymentDetailOtherwise = null;
                        String rowtaxid = jobj.getString("prtaxid");
                        KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), rowtaxid); // (Tax)session.get(Tax.class, taxid);
                        Tax rowtax = (Tax) txresult.getEntityList().get(0);
                        if (rowtax == null || rowtaxid.equalsIgnoreCase("-1")) {
                            paymentdetailotherwise.put("amount", Double.parseDouble(jobj.getString("dramount")));
                            paymentdetailotherwise.put("taxjedid", "");
                            paymentdetailotherwise.put("tax", rowtaxid.equalsIgnoreCase("-1") ? "None" : "");
                            paymentdetailotherwise.put("accountid", jobj.getString("accountid"));
                            paymentdetailotherwise.put("isdebit", isdebit);
                            paymentdetailotherwise.put("taxamount", rowtaxamount);
                            paymentdetailotherwise.put("description", StringUtil.DecodeText(jobj.optString("description")));
                            result = accVendorPaymentobj.savePaymentDetailOtherwise(paymentdetailotherwise);
                            paymentdetailotherwise.clear();
                            paymentDetailOtherwise = (PaymentDetailOtherwise) result.getEntityList().get(0);
                            payentOtherwiseList.add(paymentDetailOtherwise.getID());
                        } else {
                            rowtaxamount = Double.parseDouble(jobj.getString("taxamount"));
                            jedjson = new JSONObject();
                            jedjson.put("srno", jedetails.size() + 1);
                            jedjson.put("companyid", company.getCompanyID());
                            jedjson.put("amount", authHandler.formattedAmount(rowtaxamount, companyid));
                            jedjson.put("accountid", rowtax.getAccount().getID());
                            jedjson.put("debit", isdebit);//true);
                                jedjson.put("jeid", jeid);
                            jedjson.put("description", StringUtil.DecodeText(jobj.optString("description")));
                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jedetails.add(jed);

                            paymentdetailotherwise.put("amount", Double.parseDouble(jobj.getString("dramount")));
                            paymentdetailotherwise.put("taxjedid", jed.getID());
                            paymentdetailotherwise.put("tax", rowtax.getID());
                            paymentdetailotherwise.put("accountid", jobj.getString("accountid"));
                            paymentdetailotherwise.put("isdebit", isdebit);
                            paymentdetailotherwise.put("taxamount", rowtaxamount);
                            paymentdetailotherwise.put("description", StringUtil.DecodeText(jobj.optString("description")));
                            result = accVendorPaymentobj.savePaymentDetailOtherwise(paymentdetailotherwise);
                            paymentdetailotherwise.clear();
                            paymentDetailOtherwise = (PaymentDetailOtherwise) result.getEntityList().get(0);
                            payentOtherwiseList.add(paymentDetailOtherwise.getID());
                            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                            if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", ""))) {
                                JSONArray jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
                                customrequestParams.put("customarray", jcustomarray);
                                customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                                customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                                customrequestParams.put("modulerecid", jed.getID());
                                customrequestParams.put("recdetailId", paymentDetailOtherwise.getID());
                                customrequestParams.put("moduleid", Constants.Acc_Make_Payment_ModuleId);
                                customrequestParams.put("companyid", companyid);
                                customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                    JSONObject tempJobj = new JSONObject();
                                    tempJobj.put("accjedetailcustomdata", jed.getID());
                                    tempJobj.put("jedid", jed.getID());
                                    jedresult = accJournalEntryobj.updateJournalEntryDetails(tempJobj);

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
                            customrequestParams.put("recdetailId", paymentDetailOtherwise.getID());
                            customrequestParams.put("moduleid", Constants.Acc_Make_Payment_ModuleId);
                            customrequestParams.put("companyid", companyid);
                            customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                JSONObject tempJobj = new JSONObject();
                                tempJobj.put("accjedetailcustomdata", JEdeatilId.getID());
                                tempJobj.put("jedid", JEdeatilId.getID());
                                jedresult = accJournalEntryobj.updateJournalEntryDetails(tempJobj);

                            }
                        }

                    }

                }

            }

            if (bankCharges != 0) {
                if (!isAdvancePayment || (isAdvancePayment && onlyAdvance)) {
                    amount += bankCharges;
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
                    amount += bankInterest;
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
                jedjson.put("debit", false);
                jedjson.put("jeid", jeid);
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);

                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jedetails.add(jed);

                paymenthm.put("deposittojedetailid", jed.getID());
                paymenthm.put("depositamount", amount);
            }
            jeDataMap.put("jedetails", jedetails);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            paymenthm.put("journalentryid", journalEntry.getID());
            paymenthm.put("createdby", createdby);
            paymenthm.put("modifiedby", modifiedby);
            paymenthm.put("createdon", createdon);
            paymenthm.put("updatedon", updatedon);
            if (payment != null) {
                paymenthm.put("paymentid", payment.getID());
            }

            result = accVendorPaymentobj.savePayment(paymenthm);
            payment = (Payment) result.getEntityList().get(0);
            if (receiptType == 2 || (isVendorPaymentEdit && receiptType == 1 || receiptType == 9)) {//otherwise case and GL Code Case 
                for (int i = 0; i < payentOtherwiseList.size(); i++) {
                    paymentdetailotherwise.put("payment", payment.getID());
                    paymentdetailotherwise.put("paymentotherwise", payentOtherwiseList.get(i));
                    result = accVendorPaymentobj.savePaymentDetailOtherwise(paymentdetailotherwise);
                    paymentdetailotherwise.clear();
                }
            }

            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                customrequestParams.put("modulerecid", journalEntry.getID());
                customrequestParams.put("moduleid", Constants.Acc_Make_Payment_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    jeDataMap.put("accjecustomdataref", journalEntry.getID());
                    jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);
                }
            }

            //Save Payment Details

            HashSet payDetails = savePaymentRows(payment, company, jArr, isMultiDebit, invoice);
            paymenthm.put("paymentid", payment.getID());
            paymenthm.put("pdetails", payDetails);

            result = accVendorPaymentobj.savePayment(paymenthm);
            Iterator itr1 = payment.getRows().iterator();
            while (itr1.hasNext()) {
                PaymentDetail payd = (PaymentDetail) itr1.next();

                JSONArray jcustomarray = Map1.get(payd.getROWJEDID());
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                customrequestParams.put("modulerecid", payd.getROWJEDID());
                customrequestParams.put("recdetailId", payd.getID());
                customrequestParams.put("moduleid", Constants.Acc_Make_Payment_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    JSONObject tempJobj = new JSONObject();
                    tempJobj.put("accjedetailcustomdata", payd.getROWJEDID());
                    tempJobj.put("jedid", payd.getROWJEDID());
                    jedresult = accJournalEntryobj.updateJournalEntryDetails(tempJobj);

                }
            }
            payment = (Payment) result.getEntityList().get(0);
            if (bankReconsilationEntry) {
                bankReconsilationMap.put("clearingamount", amount);
                bankReconsilationMap.put("currencyid", currencyid);
                bankReconsilationMap.put("details", jArr);
                bankReconsilationMap.put("payment", payment);
                bankReconsilationMap.put("ismultidebit", isMultiDebit);
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
                paymenthm.clear();
                paymenthm.put("paymentid", request.getParameter("mainpaymentid"));
                paymenthm.put("advanceid", payment.getID());
                paymenthm.put("advanceamount", request.getParameter("advanceamt") != null ? Double.parseDouble(request.getParameter("advanceamt")) : 0);
                paymenthm.put("advanceamounttype", !StringUtil.isNullOrEmpty((String)request.getParameter("advanceamounttype"))  ? Integer.parseInt(request.getParameter("advanceamounttype")) : 0);
                result = accVendorPaymentobj.savePayment(paymenthm);
            }

            if (isCNDN) {
                String AccDetailsarrStr = request.getParameter("detailForCNDN");
                JSONArray drAccArr = new JSONArray(AccDetailsarrStr);
                if (!isAgainstDN) {
                    String paymentId = payment.getID();
                    KwlReturnObject cnhistoryresult = accVendorPaymentobj.getVendorCnPaymenyHistory("", 0.0, 0.0, paymentId);
                    List<CreditNotePaymentDetails> cnHistoryList = cnhistoryresult.getEntityList();
                    for (CreditNotePaymentDetails cnpd : cnHistoryList) {
                        String cnnoteid = cnpd.getCreditnote().getID()!=null?cnpd.getCreditnote().getID():"";
                        Double cnpaidamount = cnpd.getAmountPaid();
                        KwlReturnObject cnjedresult = accPaymentDAOobj.updateCnAmount(cnnoteid, -cnpaidamount);
                        KwlReturnObject opencnjedresult = accPaymentDAOobj.updateCnOpeningAmountDue(cnnoteid, -cnpaidamount);
                    }
                }

                for (int i = 0; i < drAccArr.length(); i++) {
                    JSONObject jobj = drAccArr.getJSONObject(i);
                    jedjson = new JSONObject();
                    double paidncamount = Double.parseDouble(jobj.getString("payment"));
                    double amountdue = Double.parseDouble(jobj.getString("amountdue"));
                    String cnnoteid = jobj.getString("noteid");
                    String paymentId = payment.getID();
                    if ((!jobj.getString("noteno").equalsIgnoreCase("undefined")) && (!jobj.getString("noteno").equalsIgnoreCase(""))) {
                        billno.append(jobj.getString("noteno") + ",");
                    }
                    person=" Against credit note ";
                    if (isAgainstDN) {
                        KwlReturnObject cnjedresult = accPaymentDAOobj.updateDnAmount(cnnoteid, paidncamount);
                        KwlReturnObject opencnjedresult = accPaymentDAOobj.updateDnOpeningAmountDue(cnnoteid, paidncamount);
                        cnjedresult = accVendorPaymentobj.saveVendorDnPaymenyHistory(cnnoteid, paidncamount, amountdue, paymentId);
                    } else {// make payment against vendor credit note and also customer credit note.
//                        KwlReturnObject cnhistoryresult = accVendorPaymentobj.getVendorCnPaymenyHistory(cnnoteid, paidncamount, amountdue, paymentId);
//                        List cnHistoryList=cnhistoryresult.getEntityList();
//                        Iterator iter = cnHistoryList.iterator();
//                         while (iter.hasNext()) {  
//                            Object[] obj = (Object[]) iter.next();
//                            Double cnpaidamount=obj[4]!=null?(Double)obj[4]:0.0;
//                            KwlReturnObject cnjedresult = accPaymentDAOobj.updateCnAmount(cnnoteid, -paidncamount);
//                            KwlReturnObject opencnjedresult = accPaymentDAOobj.updateCnOpeningAmountDue(cnnoteid, -paidncamount);
//                         }
                        KwlReturnObject cnjedresult = accPaymentDAOobj.updateCnAmount(cnnoteid, paidncamount);
                        KwlReturnObject opencnjedresult = accPaymentDAOobj.updateCnOpeningAmountDue(cnnoteid, paidncamount);
                        cnjedresult = accVendorPaymentobj.saveVendorCnPaymenyHistory(cnnoteid, paidncamount, amountdue, paymentId);
                    }
                }
            }

            if (jArr.length() > 0 && !isMultiDebit) {
                double finalAmountReval = 0;
                String basecurrency = sessionHandlerImpl.getCurrencyID(request);
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);
                    double ratio = 0;
                    double amountReval = 0;
                    double amountdue = jobj.getDouble("payment");
                    Date tranDate = null;
                    double exchangeRate = 0.0;
                    double exchangeRateReval = 0.0;
                    double exchangeratefortransaction = jobj.optDouble("exchangeratefortransaction", 1.00);
                    HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
                    GlobalParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
                    GlobalParams.put("gcurrencyid", basecurrency);
                    GlobalParams.put("dateformat", authHandler.getDateOnlyFormat(request));
                    result = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), jobj.getString("billid"));
                    GoodsReceipt gr = (GoodsReceipt) result.getEntityList().get(0);
                    boolean isopeningBalanceInvoice = gr.isIsOpeningBalenceInvoice();
                    tranDate = gr.getCreationDate();
                    if (!gr.isNormalInvoice()) {
                        exchangeRate = gr.getExchangeRateForOpeningTransaction();
                        exchangeRateReval = exchangeRate;
                    } else {
                        exchangeRate = gr.getJournalEntry().getExternalCurrencyRate();
                        exchangeRateReval = exchangeRate;
//                        tranDate = gr.getJournalEntry().getEntryDate();
                    }

                    HashMap<String, Object> invoiceId = new HashMap<String, Object>();
                    invoiceId.put("invoiceid", gr.getID());
                    invoiceId.put("companyid", sessionHandlerImpl.getCompanyid(request));
                    invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    RevaluationHistory revalueationHistory = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (revalueationHistory != null) {
                        exchangeRateReval = revalueationHistory.getEvalrate();
                    }

                    result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrency);
                    currency = (KWLCurrency) result.getEntityList().get(0);
                    String currid = currency.getCurrencyID();
                    if (gr.getCurrency() != null) {
                        currid = gr.getCurrency().getCurrencyID();
                    }
                    //            double oldrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, gr.getJournalEntry().getEntryDate());
                    KwlReturnObject bAmt = null;
                    if (isopeningBalanceInvoice && gr.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, tranDate, exchangeRate);
                    } else {
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, tranDate, exchangeRate);
                    }
                    double oldrate = (Double) bAmt.getEntityList().get(0);
                    //            double newrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
                    if (revalueationHistory == null && isopeningBalanceInvoice && gr.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
                    } else {
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
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
                    String revaljeid = PostJEFORReevaluation(request, finalAmountReval, companyid, preferences, basecurrency);
                    paymenthm.clear();
                    paymenthm.put("paymentid", payment.getID());
                    paymenthm.put("revalJeId", revaljeid);
                    result = accVendorPaymentobj.savePayment(paymenthm);

                }
            }

            //Insert new entries again in optimized table.
            accJournalEntryobj.saveAccountJEs_optimized(jeid);
            if (receiptType == 1) {
                person = " Against Vendor Invoice ";
            }
            list.add(payment);
            netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(amount)), currency,countryLanguageId);
        } catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("savePayment : " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("savePayment : " + ex.getMessage(), ex);
        }
        KwlReturnObject accresult = accountingHandlerDAOobj.getObject(Account.class.getName(), request.getParameter("accid"));
        Account account = (Account) accresult.getEntityList().get(0);
        String accountName = "";
        String accountaddress = "";
        String accountid = request.getParameter("accid");
        result = accVendorPaymentobj.getaccountdetailsPayment(accountid);
        if (result.getRecordTotalCount() > 0) {
            Vendor vendor = (Vendor) result.getEntityList().get(0);
            accountaddress = vendor.getAddress();
        }

        if (account != null) {
            accountName = account.getName();
        }

        KwlReturnObject accresultVen = accountingHandlerDAOobj.getObject(Vendor.class.getName(), request.getParameter("accid"));
        String vendorName = "";
        Vendor vendor = null;
        if (!accresultVen.getEntityList().isEmpty()) {
            vendor = (Vendor) accresultVen.getEntityList().get(0);

            if (vendor != null) {
                vendorName = vendor.getName();
            }
        }
        String customername = "";
        KwlReturnObject custObj = accountingHandlerDAOobj.getObject(Customer.class.getName(), request.getParameter("accid"));
        if (custObj.getEntityList().get(0) != null) {
            cust = (Customer) custObj.getEntityList().get(0);
            if (cust != null) {
                customername = cust.getName();
            }
        }

        String name = "";
        if (payment.getReceipttype() == 0 || payment.getReceipttype() == 1) {
            name = vendorName;
        } else if (payment.getReceipttype() == 6) {
            name = customername;
        } else if (payment.getReceipttype() == 7 || isCNDN) {
            name = payment.getVendor().getName();
        } else if (payment.getReceipttype() == 9) {
            if (payment.getPaidTo() != null) {
                name = payment.getPaidTo().getValue();

            }
        }
        String payee="";
        if(payment.getPaidTo()!=null){
            payee=payment.getPaidTo().getValue();
            payee=StringUtil.DecodeText(payee);
        }
        name=StringUtil.DecodeText(name);
        DecimalFormat df=new DecimalFormat("#,###,###,##0.00");
               
        String amt=df.format(amount);
        ll.add(new String[]{oldjeid, Cardid});
        ll.add(new String[]{"amount", amt});
        ll.add(new String[]{"amountinword", netinword});
        ll.add(new String[]{"accountName", name});
        ll.add(payment.getID());
        ll.add(payment.getPaymentNumber());
        ll.add(String.valueOf(payment.getAdvanceamount()));
        ll.add(accountaddress);
        ll.add(accountName);
        ll.add(payment.getJournalEntry().getEntryNumber());
        ll.add(payment.getReceipttype());
        ll.add(chequeno);
        String action = "made";
        boolean isEdit = StringUtil.isNullOrEmpty(request.getParameter("isEdit")) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
        boolean isCopy = StringUtil.isNullOrEmpty(request.getParameter("isCopyReceipt")) ? false : Boolean.parseBoolean(request.getParameter("isCopyReceipt"));
        if (isEdit == true && isCopy == false) {
            action = "updated";
        }
        if (billno.length() > 0) {
            billno.deleteCharAt(billno.length() - 1);
        }
        auditTrailObj.insertAuditLog("75", "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + " a Payment " + payment.getPaymentNumber() + person + billno, request, payment.getID());
        return (ArrayList) ll;
    }

    public ModelAndView getNextChequeNumber(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "",nextChequeNumber="" , nextSequenceNumber ="", dateprefix="", datesuffix="",prefix="",suffix="" , dateAfterPrefix="";
        boolean showleadingzero=true;
        try {
            JSONObject jsonParams= StringUtil.convertRequestToJsonObject(request);
//            String nextChequeNumber = paymentService.getNextChequeNumber(jsonParams);
            Map<String,Object> nextchequemap = accCompanyPreferencesObj.getNextChequeNumber(jsonParams);
           if(nextchequemap.containsKey(Constants.AUTO_ENTRYNUMBER)){
               nextChequeNumber = (String) nextchequemap.get(Constants.AUTO_ENTRYNUMBER);
            
        }
            boolean isFromBulkPayment = jsonParams.optBoolean("isfromBulkPayment");
          /*------------  Block is executed if it will be called from Bulk Payment---------- */
            if (isFromBulkPayment) {
                if (nextchequemap.containsKey(Constants.SEQNUMBER)) {
                    nextSequenceNumber = (String) nextchequemap.get(Constants.SEQNUMBER);

                }

                if (nextchequemap.containsKey(Constants.DATEPREFIX)) {
                    dateprefix = (String) nextchequemap.get(Constants.DATEPREFIX);

                }

                if (nextchequemap.containsKey(Constants.DATESUFFIX)) {
                    datesuffix = (String) nextchequemap.get(Constants.DATESUFFIX);

                }

                if (nextchequemap.containsKey("prefix")) {
                    prefix = (String) nextchequemap.get("prefix");

                }

                if (nextchequemap.containsKey("suffix")) {
                    suffix = (String) nextchequemap.get("suffix");

                }

                if (nextchequemap.containsKey("dateafterprefix")) {
                    dateAfterPrefix = (String) nextchequemap.get("dateafterprefix");

                }
                
                if (nextchequemap.containsKey("showleadingzero")) {
                    showleadingzero = (boolean) nextchequemap.get("showleadingzero");

                }

                jobj.put("nextSequenceNumber", nextSequenceNumber);
                jobj.put("dateprefix", dateprefix);
                jobj.put("dateAfterPrefix", dateAfterPrefix);
                jobj.put("datesuffix", datesuffix);
                jobj.put("prefix", prefix);
                jobj.put("suffix", suffix);
                jobj.put("showleadingzero", showleadingzero);

            }
         

            JSONObject dataObj = new JSONObject();
            jobj.put("nextChequeNumber", nextChequeNumber);
    
            issuccess = true;
        }catch (Exception ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }

        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView getChequeSequenceFormatList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean isSequenceformat=false;
        HashMap<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("bankAccountId", request.getParameter("bankAccountId"));
        dataMap.put("companyid", request.getParameter("companyid"));
        boolean issuccess = false;
        String msg = "";
        try {
            KwlReturnObject chequeSequenceFormatObj = accCompanyPreferencesObj.getChequeSequenceFormatList(dataMap);
            List chequeSequenceFormatList = chequeSequenceFormatObj.getEntityList();
            if (!chequeSequenceFormatList.isEmpty()) {
                isSequenceformat=true;
            }

            jobj.put("isSequenceformat", isSequenceformat);

            issuccess = true;
        }catch (Exception ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }

        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public String PostJEFORReevaluation(HttpServletRequest request, double finalAmountReval, String companyid, CompanyAccountPreferences preferences, String basecurrency) {
        String jeid = "";
        try {
            String jeentryNumber = null;
            String jeIntegerPart = "";
            String jeDatePrefix = "";
            String jeDateAfterPrefix = "";
            String jeDateSuffix = "";
            String jeSeqFormatId = "";
            boolean jeautogenflag = true;
            DateFormat df = authHandler.getDateOnlyFormat(request);
            /**
             * added Link Date to Realised JE. while link Advanced Payment to
             * Reevaluated Invoice.
             */
            String creationDate = !StringUtil.isNullObject(request.getParameter("linkingdate")) ? request.getParameter("linkingdate") : request.getParameter("creationdate");
            Date entryDate = StringUtil.isNullOrEmpty(creationDate) ? new Date() : df.parse(creationDate);
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
                jeIntegerPart = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                
                jeSeqFormatId = format.getID();
            }

            boolean creditDebitFlag = true;
            if (finalAmountReval < 0) {
                finalAmountReval = -(finalAmountReval);
                creditDebitFlag = false;
            }
            Map<String, Object> jeDataMapReval = AccountingManager.getGlobalParams(request);
            jeDataMapReval.put("entrynumber", jeentryNumber);
            jeDataMapReval.put("autogenerated", jeautogenflag);
            jeDataMapReval.put(Constants.SEQFORMAT, jeSeqFormatId);
            jeDataMapReval.put(Constants.SEQNUMBER, jeIntegerPart);
            jeDataMapReval.put(Constants.DATEPREFIX, jeDatePrefix);
            jeDataMapReval.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
            jeDataMapReval.put(Constants.DATESUFFIX, jeDateSuffix);
            jeDataMapReval.put("entrydate", entryDate);
            jeDataMapReval.put("companyid", companyid);
            //jeDataMapReval.put("memo", "Realised Gain/Loss");
            jeDataMapReval.put("currencyid", basecurrency);
            jeDataMapReval.put("isReval", 2);
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
            jedjsonreval.put("debit", creditDebitFlag ? false : true);
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

                throw new AccountingException(messageSource.getMessage("acc.field.NoUnrealisedGain/Lossaccountfound", null, RequestContextUtils.getLocale(request)));
            }
            jedjsonreval = new JSONObject();
            jedjsonreval.put("companyid", companyid);
            jedjsonreval.put("srno", jedetailsReval.size() + 1);
            jedjsonreval.put("amount", finalAmountReval);
            jedjsonreval.put("accountid", unrealised_accid);
            jedjsonreval.put("debit", creditDebitFlag ? true : false);
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
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public ModelAndView deletePaymentForEdit(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("VP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {

            String companyid = sessionHandlerImpl.getCompanyid(request);
            boolean isAdvancePayment = StringUtil.getBoolean(request.getParameter("isadvpayment"));
            boolean advanceAmountFlag = StringUtil.getBoolean(request.getParameter("advanceAmountFlag"));
            boolean invoiceAmountFlag = StringUtil.getBoolean(request.getParameter("invoiceAmountFlag"));
            boolean cndnAmountFlag = StringUtil.getBoolean(request.getParameter("cndnAmountFlag"));
            int actualReceiptType = StringUtil.getInteger(request.getParameter("actualReceiptType") != null ? request.getParameter("actualReceiptType") : "0");

            HashMap<Integer, String> paymentHashMap = new HashMap<Integer, String>();
            JSONArray jArr = new JSONArray();
            int invoiceadvcndntype = 0;
            if (request.getParameter("datainvoiceadvcndn") != null) {
                JSONArray jSONArray = new JSONArray(request.getParameter("datainvoiceadvcndn"));
                String paymentid = "", paymentno = "", paymentIds = "";
                KwlReturnObject result, resultMain;

                for (int i = 0; i < jSONArray.length(); i++) {
                    JSONObject jObject = jSONArray.getJSONObject(i);
                    paymentid = StringUtil.DecodeText(jObject.optString("paymentID"));
                    int invoiceadvcndntypejson = !StringUtil.isNullOrEmpty(jObject.getString("invoiceadvcndntype")) ? Integer.parseInt(jObject.getString("invoiceadvcndntype")) : 0;
                    paymentHashMap.put(invoiceadvcndntypejson, paymentid);
                }
                invoiceadvcndntype = !StringUtil.isNullOrEmpty(request.getParameter("invoiceadvcndntype")) ? Integer.parseInt(request.getParameter("invoiceadvcndntype")) : 0;
//                if(invoiceadvcndntype==2){
//                    if(paymentHashMap.containsKey(1))
//                        paymentIds+="'"+paymentHashMap.get(1)+"',";
//                    if(paymentHashMap.containsKey(3))
//                        paymentIds+="'"+paymentHashMap.get(3)+"'";
//                    List<Payment> paymentList = accVendorPaymentobj.getPaymentList(paymentIds);
//                        if (!paymentList.isEmpty()) {
//                            for(Payment payment:paymentList){
//                                if(payment.getAdvanceid()!=null&&payment.getAdvanceid().getID().equals(paymentHashMap.get(2)))
//                                    payment.setAdvanceid(null);
//                            }
//                            accVendorPaymentobj.savePaymentObject(paymentList);
//                        }
//                }

//                txnManager.commit(status);
//                status = txnManager.getTransaction(def);
//                String deleteCNDN=request.getParameter("deleteCNDN");
//                if(deleteCNDN!=null&&Boolean.parseBoolean(deleteCNDN)){
//                    if(paymentHashMap.containsKey(3)){
//                        deletePaymentPermanent(request,paymentHashMap.get(3));
//                    } 
//                }

            }

//            if(request.getParameter("mainData")!=null){
//                jArr=new JSONArray(request.getParameter("mainData"));
//                String paymentid = "", paymentno = "";
//                KwlReturnObject result, resultMain;
//                for (int i = 0; i < jArr.length(); i++) {
//                    JSONObject jObject = jArr.getJSONObject(i);
//                    paymentid = StringUtil.DecodeText(jObject.optString("billid"));
//                    resultMain = accountingHandlerDAOobj.getObject(Payment.class.getName(), paymentid);
//                    if (!resultMain.getEntityList().isEmpty() && resultMain.getEntityList().get(0) != null) {
//                        Payment payment = (Payment) resultMain.getEntityList().get(0);
//                        payment.setAdvanceid(null);
//                        accVendorPaymentobj.savePaymentObject(payment);
//                    }
//                }
//                txnManager.commit(status);
//                status = txnManager.getTransaction(def);
//            }


//                boolean isCNDN=StringUtil.getBoolean(request.getParameter("isCNDN"));
//            
//                JSONArray drAccArr = new JSONArray();
//                if (isCNDN) {
//                    String AccDetailsarrStr = request.getParameter("detailForCNDN");
//                    String allcnnoteids = "";
//                    drAccArr = new JSONArray(AccDetailsarrStr);
//                }
//                    if (drAccArr.length() == 0) {
//                        String deleteCNDN = request.getParameter("deleteCNDN");
//                        if (deleteCNDN != null && Boolean.parseBoolean(deleteCNDN)) {
//                            if (paymentHashMap.containsKey(3)) {
//                                deletePaymentPermanent(request, paymentHashMap.get(3));
//                            }
//
//                        }
//                    }else if(invoiceadvcndntype!=3){
//                        deletePaymentPermanent(request);
//                    }
//            if(request.getParameter("datainvoiceadvcndn")!=null&&(actualReceiptType!=0||(actualReceiptType==0&&paymentHashMap.containsKey(3)))){
            if (request.getParameter("datainvoiceadvcndn") != null) {
                if (paymentHashMap.containsKey(2) && !paymentHashMap.containsKey(3) && !paymentHashMap.containsKey(1)) {
                    deletePaymentPermanent(request);
                }else {
                    if (advanceAmountFlag && paymentHashMap.containsKey(2)) {
                        deletePaymentPermanent(request, paymentHashMap.get(2));
                    }
                    if (invoiceAmountFlag && paymentHashMap.containsKey(1)) {
                        deletePaymentPermanent(request, paymentHashMap.get(1));
                    }
                    if (cndnAmountFlag && paymentHashMap.containsKey(3)) {
                        deletePaymentPermanent(request, paymentHashMap.get(3));
                    }
                }
            } else {
                deletePaymentPermanent(request);
            }
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.pay.del", null, RequestContextUtils.getLocale(request));  //"Payment(s) has been deleted successfully";
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getInvoiceAdvanceCNDN(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

//            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
//            def.setName("VP_Tx");
//            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
//            TransactionStatus status = txnManager.getTransaction(def);
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            JSONArray jArr = new JSONArray();
            HashMap<String, String> paymentHashMap = new HashMap<String, String>();
            if (request.getParameter("selectedData") != null) {
                jArr = new JSONArray(request.getParameter("selectedData"));
                if (jArr.length() > 0) {
                    JSONObject jSONObject = jArr.getJSONObject(0);
                    if (!StringUtil.isNullOrEmpty(jSONObject.optString("billid", ""))) {
                        paymentHashMap.put("paymentId", jSONObject.getString("billid"));
                        paymentHashMap.put("invoiceadvcndntype", jSONObject.getString("invoiceadvcndntype"));
                        KwlReturnObject result = accVendorPaymentobj.getInvoiceAdvPaymentList(paymentHashMap);
                        List paymentList = result.getEntityList();
                        Iterator iter = paymentList.iterator();
                        JSONArray jrr = new JSONArray();
                        while (iter.hasNext()) {
                            JSONObject jSONObj = new JSONObject();
                            Payment payment = (Payment) iter.next();
                            jSONObj.put("paymentID", payment.getID());
                            jSONObj.put("invoiceadvcndntype", payment.getInvoiceAdvCndnType());
                            jrr.put(jSONObj);
                        }
                        jobj.put("data", jrr);
                        jobj.put("count", paymentList.size());
                    }
                }

            }
//                txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.pay.del", null, RequestContextUtils.getLocale(request));  //"Payment(s) has been deleted successfully";
        } catch (SessionExpiredException ex) {
//                txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
//                txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView saveContraPayment(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        String JENumBer = "";
        String billno = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            List li = saveContraPayment(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String[] id = (String[]) li.get(0);
            if (li.get(1) != null) {
                billno = li.get(1).toString();
            }
            if (li.get(2) != null) {
                JENumBer = li.get(2).toString();
            }
            issuccess = true;
            msg = messageSource.getMessage("acc.contra.save", null, RequestContextUtils.getLocale(request)) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + billno + ",</b>" + messageSource.getMessage("acc.field.JENo", null, RequestContextUtils.getLocale(request)) + ": <b>" + JENumBer + "</b>";   //"Payment information has been saved successfully";
            txnManager.commit(status);
//            status = txnManager.getTransaction(def);
//            deleteJEArray(id[0],companyid);
//            txnManager.commit(status);
//            status = txnManager.getTransaction(def);
//            deleteChequeOrCard(id[1],companyid);
//            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public List saveContraPayment(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException {
        KwlReturnObject result;
        List list = new ArrayList();
        Payment payment = null;
        String oldjeid = null;
        String Cardid = null;
        List ll = new ArrayList();
        try {
            String maininvoiceid = request.getParameter("maininvoiceid");//Invoice
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), maininvoiceid);
            Invoice invoice = (Invoice) cmpresult.getEntityList().get(0);
            Account dipositTo = invoice.getCustomer().getAccount();

            double amount = 0;
            double amountDiff = 0;
//            boolean rateDecreased = false;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String sequenceformat = request.getParameter("sequenceformat");
            double externalCurrencyRate = StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            String drAccDetails = request.getParameter("detail");
            boolean isMultiDebit = StringUtil.getBoolean(request.getParameter("ismultidebit"));
            String entryNumber = request.getParameter("no");
//            String receiptid =request.getParameter("billid");

            String methodid = request.getParameter("pmtmethod");
            sessionHandlerImpl.updatePaymentMethodID(request, methodid);

            String jeid = null;
//            String payDetailID=null;
            String jeentryNumber = null;
            boolean jeautogenflag = false;
            String jeIntegerPart = "";
            String jeDatePrefix = "";
            String jeDateAfterPrefix = "";
            String jeDateSuffix = "";
            String jeSeqFormatId = "";
            HashMap paymenthm = new HashMap();
            String nextAutoNo = "";
            Date creationDate = df.parse(request.getParameter("creationdate"));

            synchronized (this) {
                result = accVendorPaymentobj.getPaymentFromNo(entryNumber, companyid);
                int count = result.getRecordTotalCount();
                if (count > 0) {
                    if (sequenceformat.equals("NA")) {
                        throw new AccountingException(messageSource.getMessage("acc.field.Paymentnumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                    }
                }
                boolean seqformat_oldflag = StringUtil.getBoolean(request.getParameter("seqformat_oldflag"));

                String nextAutoNoInt = "";
                String datePrefix = "";
                String dateafterPrefix = "";
                String dateSuffix = "";
                if (seqformat_oldflag) {
                    nextAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_PAYMENT, sequenceformat);
                } else {
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_PAYMENT, sequenceformat, seqformat_oldflag, creationDate);
                    nextAutoNo = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated numbe
                    nextAutoNoInt = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                    datePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    dateafterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                    dateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                    
                    paymenthm.put(Constants.SEQFORMAT, sequenceformat);
                    paymenthm.put(Constants.SEQNUMBER, nextAutoNoInt);
                    paymenthm.put(Constants.DATEPREFIX, datePrefix);
                    paymenthm.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
                    paymenthm.put(Constants.DATESUFFIX, dateSuffix);
                }
                if (!sequenceformat.equals("NA")) {
                    entryNumber = nextAutoNo;
                }
            }
            paymenthm.put("entrynumber", entryNumber);

            paymenthm.put("autogenerated", entryNumber.equals(nextAutoNo));

            cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            currencyid = (request.getParameter("currencyid") == null ? currency.getCurrencyID() : request.getParameter("currencyid"));

//            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
//            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);

            paymenthm.put("currencyid", currencyid);
            paymenthm.put("externalCurrencyRate", externalCurrencyRate);
//            result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), request.getParameter("pmtmethod"));
//            PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);
//
//            dipositTo = payMethod.getAccount();
//            HashMap pdetailhm = new HashMap();
//            pdetailhm.put("paymethodid", payMethod.getID());
//            pdetailhm.put("companyid", companyid);
//            
//            KwlReturnObject pdresult=null;
//            if (!StringUtil.isNullOrEmpty(receiptid)&&!StringUtil.isNullOrEmpty(payDetailID))
//                pdetailhm.put("paydetailid", payDetailID);
//                pdresult = accPaymentDAOobj.addPayDetail(pdetailhm);

//            PayDetail pdetail = (PayDetail) pdresult.getEntityList().get(0);
//            paymenthm.put("paydetailsid", pdetail.getID());
            paymenthm.put("memo", request.getParameter("memo"));
            paymenthm.put("companyid", companyid);
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
            jeDataMap.put("memo", "Contra Entry " + request.getParameter("memo"));
            jeDataMap.put("currencyid", currencyid);
            Set jedetails = new HashSet();
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
//                amountDiff = oldPaymentRowsAmount(request, jArr, currencyid,externalCurrencyRate);
//                if (preferences.getForeignexchange() == null) {
//                    throw new AccountingException(messageSource.getMessage("acc.common.forex", null, RequestContextUtils.getLocale(request)));
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
//                    jedjson.put("debit", rateDecreased?true:false);
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
                    jedjson.put("debit", true);
                    jedjson.put("jeid", jeid);
                    jedjson.put("description", jobj.getString("description"));
                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                }
            } else {
                jedjson = new JSONObject();
                jedjson.put("companyid", companyid);
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put("amount", amount + amountDiff);
                jedjson.put("accountid", request.getParameter("accid"));
                jedjson.put("debit", true);
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
            jedjson.put("debit", false);
            jedjson.put("jeid", jeid);
            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);

            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetails.add(jed);
            jeDataMap.put("jedetails", jedetails);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            paymenthm.put("journalentryid", journalEntry.getID());
            paymenthm.put("contraentry", true);
            if (payment != null) {
                paymenthm.put("paymentid", payment.getID());
            }

            result = accVendorPaymentobj.savePayment(paymenthm);
            payment = (Payment) result.getEntityList().get(0);

            //Save Payment Details

            HashSet payDetails = savePaymentRows(payment, company, jArr, isMultiDebit, invoice);
            paymenthm.put("paymentid", payment.getID());
            paymenthm.put("pdetails", payDetails);

            result = accVendorPaymentobj.savePayment(paymenthm);
            payment = (Payment) result.getEntityList().get(0);

            //Insert new entries in optimized table.
            accJournalEntryobj.saveAccountJEs_optimized(jeid);

            list.add(payment);
//        } catch (UnsupportedEncodingException ex) {
//            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("savePayment : " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("savePayment : " + ex.getMessage(), ex);
        }
        ll.add(new String[]{oldjeid, Cardid});
        ll.add(payment.getPaymentNumber());
        ll.add(payment.getJournalEntry().getEntryNumber());
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
            String answer = "";
            
            if (number == 0) {
                return "Zero";
            }

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

        public String indianConvert(Double number,KWLCurrency currency) {
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
            KwlReturnObject brdresult1 = accBankReconciliationObj.getBRfromJE((String) requestParams.get("oldjeid"), (String) requestParams.get("companyId"), true);
            if (brdresult1.getRecordTotalCount() > 0) {
                BankReconciliation br = null;
                for (int i = 0; i < brdresult1.getEntityList().size(); i++) {
                    BankReconciliationDetail brd = (BankReconciliationDetail) brdresult1.getEntityList().get(i);
                    accBankReconciliationObj.permenantDeleteBankReconciliationDetail(brd.getID(), (String) requestParams.get("companyId"));
                    if (br == null) {
                        br = brd.getBankReconciliation();
                    }
                }
                accBankReconciliationObj.permenantDeleteBankReconciliation(br.getID(), (String) requestParams.get("companyId"));
            }
        }

    }

    private void saveBankReconsilation(Map<String, Object> requestParams, Map<String, Object> globalParams) throws ServiceException, JSONException, UnsupportedEncodingException {
        HashMap<String, Object> brMap = new HashMap<String, Object>();
        KwlReturnObject crresult = accCurrencyDAOobj.getCurrencyToBaseAmount(globalParams, (Double) requestParams.get("clearingamount"), (String) requestParams.get("currencyid"), (Date) requestParams.get("clearanceDate"), 0);
        double clearingAmount = (Double) crresult.getEntityList().get(0);
        boolean isOpeningPayment = false;
        HashSet hs = new HashSet();
        String billid = "";
        String jeid = "";
        double amount = 0;
        if (requestParams.containsKey("isOpeningPayment")) {
            isOpeningPayment = Boolean.parseBoolean(requestParams.get("isOpeningPayment").toString());
        }
        if (requestParams.containsKey("oldjeid")) {
            deleteBankReconcilation(requestParams);
        }

        brMap.put("startdate", (Date) requestParams.get("startDate"));
        brMap.put("enddate", (Date) requestParams.get("endDate"));
        brMap.put("clearanceDate", (Date) requestParams.get("clearanceDate"));
        brMap.put("clearingamount", (0 - clearingAmount));
        brMap.put("endingamount", (Double) requestParams.get("endingAmount"));
        brMap.put("accountid", (String) requestParams.get("bankAccountId"));
        brMap.put("companyid", (String) requestParams.get("companyId"));
        brMap.put("checkCount", (Integer) requestParams.get("checkCount"));
        brMap.put("depositeCount", (Integer) requestParams.get("depositeCount"));
        brMap.put("createdby", (String) requestParams.get("createdby"));
        KwlReturnObject brresult = accBankReconciliationObj.addBankReconciliation(brMap);
        BankReconciliation br = (BankReconciliation) brresult.getEntityList().get(0);
        String brid = br.getID();
        Payment payment = (Payment) requestParams.get("payment");
        String accountName = "";
        int moduleID = 0;
        if (!isOpeningPayment) {
            JournalEntry entry = payment.getJournalEntry();
            Set details = entry.getDetails();
            Iterator iter = details.iterator();
            while (iter.hasNext()) {
                JournalEntryDetail d = (JournalEntryDetail) iter.next();
                if (!d.isDebit()) {
                    continue;
                }
                accountName += d.getAccount().getName() + ", ";
            }

            //Calculate the Amount.
            JSONArray jArr = (JSONArray) requestParams.get("details");
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (jobj.optDouble("enteramount", 0) != 0) {
                    KwlReturnObject crresult1 = accCurrencyDAOobj.getCurrencyToBaseAmount(globalParams, jobj.getDouble("enteramount"), jobj.getString("currencyid"), (Date) requestParams.get("clearanceDate"), 0);
                    double amt = (Double) crresult1.getEntityList().get(0);
                    if (jobj.optBoolean("debit", true)) {
                        amount += amt;
                    } else {
                        amount -= amt;
                    }
                }
            }
            jeid = entry.getID();
            billid = null;
            moduleID = Constants.Acc_GENERAL_LEDGER_ModuleId;
        } else {
            jeid = null;
            billid = payment.getID();
            amount = clearingAmount;
            moduleID = Constants.Acc_Make_Payment_ModuleId;
            accountName = "";
        }
        accountName = accountName.substring(0, Math.max(0, accountName.length() - 2));

        HashMap<String, Object> brdMap = new HashMap<>();
        brdMap.put("companyid", (String) requestParams.get("companyId"));
        brdMap.put("amount", amount);
        brdMap.put("jeid", jeid);
        brdMap.put("accountname", accountName);
        brdMap.put("debit", false);
        brdMap.put("brid", brid);
        brdMap.put("transactionID", billid);
        brdMap.put("moduleID", moduleID);
        brdMap.put("isOpeningTransaction", isOpeningPayment);

        KwlReturnObject brdresult1 = accBankReconciliationObj.addBankReconciliationDetail(brdMap);
        BankReconciliationDetail brd1 = (BankReconciliationDetail) brdresult1.getEntityList().get(0);
        hs.add(brd1);
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

    public ModelAndView saveBillingPayment(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject jobjDetails = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean issuccess = false;
        boolean isChequePrint = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            List li = saveBillingPayment(request);
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
            msg = messageSource.getMessage("acc.pay.billsave", null, RequestContextUtils.getLocale(request));   //"Billing Payment information has been saved successfully";
//            msg = result.getMsg();
            txnManager.commit(status);
            status = txnManager.getTransaction(def);
            deleteJEArray(id[0], companyid);
            txnManager.commit(status);
            status = txnManager.getTransaction(def);
            deleteChequeOrCard(id[1], companyid);
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArr);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public List saveBillingPayment(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException {
        List list = new ArrayList();
        KwlReturnObject result;
        BillingPayment payment = null;
        String oldjeid = null;
        String Cardid = null;
        List ll = new ArrayList();
        BillingInvoice binvoice = null;//Set for contra entry
        String netinword = "";
        double amount = 0;
        try {
            Account dipositTo = null;
            double amountDiff = 0;
            boolean rateDecreased = false;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            double externalCurrencyRate = StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            String drAccDetails = request.getParameter("detail");
            String customfield = request.getParameter("customfield");
            boolean isMultiDebit = StringUtil.getBoolean(request.getParameter("ismultidebit"));
            boolean isAdvancePayment = StringUtil.getBoolean(request.getParameter("isadvpayment"));
            String entryNumber = request.getParameter("no");
            String receiptid = request.getParameter("billid");
            int receiptType = StringUtil.getInteger(request.getParameter("receipttype"));
            boolean isVendorPaymentEdit = (Boolean.parseBoolean((String) request.getParameter("isReceiptEdit")));
            String methodid = request.getParameter("pmtmethod");
            sessionHandlerImpl.updatePaymentMethodID(request, methodid);
            String sequenceformat = request.getParameter("sequenceformat");

            String jeid = null;
            String payDetailID = null;
            String jeentryNumber = null;
            boolean jeautogenflag = false;
            boolean bankReconsilationEntry = false, bankPayment = false;
            Date clearanceDate = null, startDate = null, endDate = null;
            String bankAccountId = "";
            Map<String, Object> bankReconsilationMap = new HashMap<String, Object>();
            HashMap billingPaymenthm = new HashMap();
            boolean ismanydbcr = StringUtil.getBoolean(request.getParameter("ismanydbcr"));
            billingPaymenthm.put("ismanydbcr", ismanydbcr);
            billingPaymenthm.put("receipttype", receiptType);
            if (receiptType == 6) {
                billingPaymenthm.put("customer", request.getParameter("accid"));
            }
            double bankCharges = 0;
            double bankInterest = 0;
            boolean onlyAdvance = StringUtil.getBoolean(request.getParameter("onlyAdvance"));
            String bankChargesAccid = request.getParameter("bankChargesCmb");
            String bankInterestAccid = request.getParameter("bankInterestCmb");
            if (!StringUtil.isNullOrEmpty(request.getParameter("bankCharges")) && !StringUtil.isNullOrEmpty(bankChargesAccid)) {
                bankCharges = Double.parseDouble(request.getParameter("bankCharges"));
                billingPaymenthm.put("bankCharges", bankCharges);
                billingPaymenthm.put("bankChargesCmb", bankChargesAccid);
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("bankInterest")) && !StringUtil.isNullOrEmpty(bankInterestAccid)) {
                bankInterest = Double.parseDouble(request.getParameter("bankInterest"));
                billingPaymenthm.put("bankInterest", bankInterest);
                billingPaymenthm.put("bankInterestCmb", bankInterestAccid);
            }
            String paidToid = request.getParameter("paidToCmb");
            if (!StringUtil.isNullOrEmpty(request.getParameter("paidToCmb")) && !StringUtil.isNullOrEmpty(paidToid)) {
                billingPaymenthm.put("paidToCmb", paidToid);
            }
            if (!StringUtil.isNullOrEmpty(receiptid)) {
                KwlReturnObject receiptObj = accountingHandlerDAOobj.getObject(BillingPayment.class.getName(), receiptid);
                payment = (BillingPayment) receiptObj.getEntityList().get(0);
                jeentryNumber = payment.getJournalEntry().getEntryNumber();
                oldjeid = payment.getJournalEntry().getID();
                jeautogenflag = payment.getJournalEntry().isAutoGenerated();
                if (payment.getPayDetail() != null) {
                    payDetailID = payment.getPayDetail().getID();
                    if (payment.getPayDetail().getCard() != null) {
                        Cardid = payment.getPayDetail().getCard().getID();
                    }
                    if (payment.getPayDetail().getCheque() != null) {
                        Cardid = payment.getPayDetail().getCheque().getID();
                    }
                }
                result = accVendorPaymentobj.deleteBillingPaymentsDetails(receiptid, companyid);
                result = accVendorPaymentobj.deleteBillingPaymentsDetailsOtherwise(receiptid);
                if (payment != null && payment.getRevalJeId() != null) {
                    result = accJournalEntryobj.deleteJEDtails(payment.getRevalJeId(), companyid);
                    result = accJournalEntryobj.deleteJE(payment.getRevalJeId(), companyid);
                }

                billingPaymenthm.put("deposittojedetailid", null);
                billingPaymenthm.put("depositamount", 0.0);
                //Delete old entries and insert new entries again from optimized table in edit case.
                accJournalEntryobj.deleteOnEditAccountJEs_optimized(oldjeid);
            } else {
                result = accVendorPaymentobj.getBillingPaymentFromNo(entryNumber, companyid);
                int count = result.getRecordTotalCount();
                if (count > 0) {
                    throw new AccountingException(messageSource.getMessage("acc.field.Paymentnumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                }
                boolean seqformat_oldflag = StringUtil.getBoolean(request.getParameter("seqformat_oldflag"));
                String nextAutoNo = "";
                String nextAutoNoInt = "";
                if (seqformat_oldflag) {
                    nextAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_BILLINGPAYMENT, sequenceformat);
                } else {
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_BILLINGPAYMENT, sequenceformat, seqformat_oldflag, new Date());
                    nextAutoNo = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    nextAutoNoInt = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                    billingPaymenthm.put(Constants.SEQFORMAT, sequenceformat);
                    billingPaymenthm.put(Constants.SEQNUMBER, nextAutoNoInt);
                }
                billingPaymenthm.put("entrynumber", entryNumber);
                billingPaymenthm.put("autogenerated", entryNumber.equals(nextAutoNo));
            }

            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            currencyid = (request.getParameter("currencyid") == null ? currency.getCurrencyID() : request.getParameter("currencyid"));

            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);



            billingPaymenthm.put("currencyid", currencyid);

            result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), request.getParameter("pmtmethod"));
            PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);

            dipositTo = payMethod.getAccount();
            HashMap bpdetailhm = new HashMap();
            bpdetailhm.put("paymethodid", payMethod.getID());
            bpdetailhm.put("companyid", companyid);
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
                    chequehm.put("bankname", StringUtil.DecodeText(obj.optString("bankname")));
                    chequehm.put("duedate", df.parse(obj.getString("payDate")));
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
                    bpdetailhm.put("chequeid", cheque.getID());
                } else if (payMethod.getDetailType() == PaymentMethod.TYPE_CARD) {
                    HashMap cardhm = new HashMap();
                    cardhm.put("cardno", obj.getString("cardno"));
                    cardhm.put("nameoncard", obj.getString("nameoncard"));
                    cardhm.put("expirydate", df.parse(obj.getString("expirydate")));
                    cardhm.put("cardtype", obj.getString("cardtype"));
                    cardhm.put("refno", obj.getString("refno"));
                    KwlReturnObject cdresult = accPaymentDAOobj.addCard(cardhm);
                    Card card = (Card) cdresult.getEntityList().get(0);
                    bpdetailhm.put("cardid", card.getID());
                }

            }
            KwlReturnObject pdresult = null;
            if (!StringUtil.isNullOrEmpty(receiptid) && !StringUtil.isNullOrEmpty(payDetailID)) {
                bpdetailhm.put("paydetailid", payDetailID);
            }
            pdresult = accPaymentDAOobj.addPayDetail(bpdetailhm);

            PayDetail pdetail = (PayDetail) pdresult.getEntityList().get(0);
            billingPaymenthm.put("paydetailsid", pdetail.getID());
            billingPaymenthm.put("memo", request.getParameter("memo"));
            billingPaymenthm.put("companyid", companyid);
            billingPaymenthm.put("deleted", false);

            if (StringUtil.isNullOrEmpty(oldjeid)) {
                String nextJEAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_JOURNALENTRY);
                jeentryNumber = nextJEAutoNo;// + "/" + entryNumber;
                jeautogenflag = true;
            }
            HashMap<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
            jeDataMap.put("entrynumber", jeentryNumber);
            jeDataMap.put("autogenerated", jeautogenflag);
            jeDataMap.put("entrydate", df.parse(request.getParameter("creationdate")));
            jeDataMap.put("companyid", companyid);
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
            HashMap<String, Object> jParam = new HashMap();
            KwlReturnObject jedresult = null;
            JournalEntryDetail jed = null;

            if (jArr.length() > 0 && !isMultiDebit) {
                amount = 0;
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);
                    amount += jobj.getDouble("payment");
                }
                amountDiff = oldBillingPaymentRowsAmount(request, jArr, currencyid, externalCurrencyRate);
                if (preferences.getForeignexchange() == null) {
                    throw new AccountingException(messageSource.getMessage("acc.pay.forex", null, RequestContextUtils.getLocale(request)));
                }
                if (amountDiff != 0 && preferences.getForeignexchange() != null) {
                    if (amountDiff < 0) {
                        rateDecreased = true;
                    }
                    jParam.put("srno", jedetails.size() + 1);
                    jParam.put("companyid", companyid);
                    jParam.put("amount", rateDecreased ? (-1 * amountDiff) : amountDiff);
                    jParam.put("accountid", preferences.getForeignexchange().getID());
                    jParam.put("debit", rateDecreased ? true : false);
                    jParam.put("jeid", jeid);
                    jedresult = accJournalEntryobj.updateJournalEntryDetails(jParam);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                }
            } else {
                amount = Double.parseDouble(request.getParameter("amount"));
            }
            JSONObject jedjson = null;
            //boolean taxExist=false;
            List payentOtherwiseList = new ArrayList();
            HashMap paymentdetailotherwise = new HashMap();
            if (isMultiDebit) {
                JSONArray drAccArr = new JSONArray(drAccDetails);
                for (int i = 0; i < drAccArr.length(); i++) {
                    JSONObject jobj = drAccArr.getJSONObject(i);
                    boolean isdebit = jobj.has("isdebit") ? Boolean.parseBoolean(jobj.getString("isdebit")) : true;
                    jParam = new HashMap();
                    jParam.put("srno", jedetails.size() + 1);
                    jParam.put("companyid", companyid);
                    jParam.put("amount", Double.parseDouble(jobj.getString("dramount")));
                    jParam.put("accountid", jobj.getString("accountid"));
                    jParam.put("debit", isdebit);//true);
                    jParam.put("jeid", jeid);
                    jParam.put("description", jobj.get("description"));
                    jedresult = accJournalEntryobj.updateJournalEntryDetails(jParam);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                    double rowtaxamount = 0;
                    //  taxExist=StringUtil.isNullOrEmpty(rowtaxid)?false:true;
                    if (receiptType == 2 || (isVendorPaymentEdit && receiptType == 1) || receiptType == 9) {//otherwise case and GL Code Case 
                        String rowtaxid = jobj.getString("prtaxid");
                        KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), rowtaxid); // (Tax)session.get(Tax.class, taxid);
                        Tax rowtax = (Tax) txresult.getEntityList().get(0);
                        if (rowtax == null || rowtaxid.equalsIgnoreCase("-1")) {
                            paymentdetailotherwise.put("amount", Double.parseDouble(jobj.getString("dramount")));
                            paymentdetailotherwise.put("taxjedid", "");
                            paymentdetailotherwise.put("tax", rowtaxid.equalsIgnoreCase("-1") ? "None" : "");
                            paymentdetailotherwise.put("accountid", jobj.getString("accountid"));
                            paymentdetailotherwise.put("isdebit", isdebit);
                            paymentdetailotherwise.put("taxamount", rowtaxamount);
                            paymentdetailotherwise.put("description", jobj.getString("description"));
                            result = accVendorPaymentobj.saveBillingPaymentDetailOtherwise(paymentdetailotherwise);
                            paymentdetailotherwise.clear();
                            BillingPaymentDetailOtherwise paymentDetailOtherwise = null;
                            paymentDetailOtherwise = (BillingPaymentDetailOtherwise) result.getEntityList().get(0);
                            payentOtherwiseList.add(paymentDetailOtherwise.getID());
                        } else {
                            rowtaxamount = Double.parseDouble(jobj.getString("curamount")) - Double.parseDouble(jobj.getString("dramount"));
                            jedjson = new JSONObject();
                            jedjson.put("srno", jedetails.size() + 1);
                            jedjson.put("companyid", company.getCompanyID());
                            jedjson.put("amount", authHandler.formattedAmount(rowtaxamount, companyid));
                            jedjson.put("accountid", rowtax.getAccount().getID());
                            jedjson.put("debit", isdebit);//true);
                            jedjson.put("jeid", jeid);
                            jedjson.put("description", jobj.getString("description"));
                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jedetails.add(jed);

                            paymentdetailotherwise.put("amount", Double.parseDouble(jobj.getString("dramount")));
                            paymentdetailotherwise.put("taxjedid", jed.getID());
                            paymentdetailotherwise.put("tax", rowtax.getID());
                            paymentdetailotherwise.put("accountid", jobj.getString("accountid"));
                            paymentdetailotherwise.put("isdebit", isdebit);
                            paymentdetailotherwise.put("taxamount", rowtaxamount);
                            paymentdetailotherwise.put("description", jobj.getString("description"));
                            result = accVendorPaymentobj.saveBillingPaymentDetailOtherwise(paymentdetailotherwise);
                            paymentdetailotherwise.clear();
                            BillingPaymentDetailOtherwise billingPaymentDetailOtherwise = null;
                            billingPaymentDetailOtherwise = (BillingPaymentDetailOtherwise) result.getEntityList().get(0);
                            payentOtherwiseList.add(billingPaymentDetailOtherwise.getID());
                        }

                    }

                }

            } else {

                jParam = new HashMap();
                jParam.put("srno", jedetails.size() + 1);
                jParam.put("companyid", companyid);
                jParam.put("amount", amount + amountDiff);
                jParam.put("accountid", request.getParameter("accid"));
                jParam.put("debit", true);
                jParam.put("jeid", jeid);
                jedresult = accJournalEntryobj.updateJournalEntryDetails(jParam);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jedetails.add(jed);
            }

            if (bankCharges != 0) {
                if (!isAdvancePayment || (isAdvancePayment && onlyAdvance)) {
                    amount += bankCharges;
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
                    amount += bankInterest;
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
                jParam = new HashMap();
                jParam.put("srno", jedetails.size() + 1);
                jParam.put("companyid", companyid);
                //(If currency USD and base currency SGD, inv rate 0.81 n pay rate 0.80 then amount = 1234.57, amountDiff = -15.24, depositAmt = 1250 SGD)
                jParam.put("amount", amount);
                jParam.put("accountid", dipositTo.getID());
                jParam.put("debit", false);
                jParam.put("jeid", jeid);
                jedresult = accJournalEntryobj.updateJournalEntryDetails(jParam);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jedetails.add(jed);
                billingPaymenthm.put("deposittojedetailid", jed.getID());
                billingPaymenthm.put("depositamount", amount);
            }

            jeDataMap.put("jedetails", jedetails);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            billingPaymenthm.put("journalentryid", journalEntry.getID());


            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                customrequestParams.put("modulerecid", journalEntry.getID());
                customrequestParams.put("moduleid", Constants.Acc_Make_Payment_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    jeDataMap.put("accjecustomdataref", journalEntry.getID());
                    jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);
                }
            }

            if (payment != null) {
                billingPaymenthm.put("billingPaymentid", payment.getID());
            }
            result = accVendorPaymentobj.saveBillingPayment(billingPaymenthm);
            payment = (BillingPayment) result.getEntityList().get(0);

            if (!StringUtil.isNullOrEmpty(receiptid)) {
                billingPaymenthm.put("billingpaymentid", payment.getID());
            }
            //Save Payment Details
            HashSet payDetails = saveBillingPaymentRows(payment, company, jArr, isMultiDebit, binvoice);
            billingPaymenthm.put("billingPaymentid", payment.getID());
            billingPaymenthm.put("externalCurrencyRate", externalCurrencyRate);
            billingPaymenthm.put("bpdetails", payDetails);

            result = accVendorPaymentobj.saveBillingPayment(billingPaymenthm);
            payment = (BillingPayment) result.getEntityList().get(0);
            if (receiptType == 2 || (isVendorPaymentEdit && receiptType == 1) || receiptType == 9) {//otherwise case and GL Code Case 
                for (int i = 0; i < payentOtherwiseList.size(); i++) {
                    paymentdetailotherwise.put("billingpayment", payment.getID());
                    paymentdetailotherwise.put("paymentotherwise", payentOtherwiseList.get(i));
                    result = accVendorPaymentobj.saveBillingPaymentDetailOtherwise(paymentdetailotherwise);
                    paymentdetailotherwise.clear();
                }
            }

            if (bankReconsilationEntry) {
                bankReconsilationMap.put("clearingamount", amount);
                bankReconsilationMap.put("currencyid", currencyid);
                bankReconsilationMap.put("details", jArr);
                bankReconsilationMap.put("bpayment", payment);
                bankReconsilationMap.put("ismultidebit", isMultiDebit);
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
                    result = accountingHandlerDAOobj.getObject(BillingGoodsReceipt.class.getName(), jobj.getString("billid"));
                    BillingGoodsReceipt gr = (BillingGoodsReceipt) result.getEntityList().get(0);
                    double exchangeRate = gr.getJournalEntry().getExternalCurrencyRate();
                    HashMap<String, Object> invoiceId = new HashMap<String, Object>();
                    invoiceId.put("invoiceid", gr.getID());
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
                    if (gr.getCurrency() != null) {
                        currid = gr.getCurrency().getCurrencyID();
                    }
                    //            double oldrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, gr.getJournalEntry().getEntryDate());
                    KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, gr.getJournalEntry().getEntryDate(), gr.getJournalEntry().getExternalCurrencyRate());
                    double oldrate = (Double) bAmt.getEntityList().get(0);
                    //            double newrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, creationDate, exchangeRate);
                    double newrate = (Double) bAmt.getEntityList().get(0);
                    ratio = oldrate - newrate;
                    amountReval = amountdue * ratio;
                    finalAmountReval = finalAmountReval + amountReval;
                }
                if (finalAmountReval != 0) {
                    String revaljeid = PostJEFORReevaluation(request, finalAmountReval, companyid, preferences, basecurrency);
                    billingPaymenthm.clear();
                    billingPaymenthm.put("billingPaymentid", payment.getID());
                    billingPaymenthm.put("revalJeId", revaljeid);
                    result = accVendorPaymentobj.saveBillingPayment(billingPaymenthm);
                }
            }
            list.add(payment);

        } catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        ll.add(new String[]{oldjeid, Cardid});
        KwlReturnObject accresult = accountingHandlerDAOobj.getObject(Account.class.getName(), request.getParameter("accid"));
        Account account = (Account) accresult.getEntityList().get(0);
        String accountName = "";
        if (account != null) {
            accountName = account.getName();
        }
        ll.add(new String[]{"amount", String.valueOf(amount)});
        ll.add(new String[]{"amountinword", netinword});
        ll.add(new String[]{"accountName", accountName});
        return (ArrayList) ll;
    }

    public ModelAndView saveContraBillingPayment(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject jobjDetails = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean issuccess = false;
        boolean isChequePrint = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            List li = saveContraBillingPayment(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String[] id = (String[]) li.get(0);
            issuccess = true;
            msg = messageSource.getMessage("acc.contra.save", null, RequestContextUtils.getLocale(request));   //"Billing Payment information has been saved successfully";
//            msg = result.getMsg();
            txnManager.commit(status);
//            status = txnManager.getTransaction(def);
//            deleteJEArray(id[0],companyid);
//            txnManager.commit(status);
//            status = txnManager.getTransaction(def);
//            deleteChequeOrCard(id[1],companyid);
//            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArr);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public List saveContraBillingPayment(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException {
        List list = new ArrayList();
        KwlReturnObject result;
        BillingPayment payment = null;
        String oldjeid = null;
        String Cardid = null;
        List ll = new ArrayList();
        String netinword = "";
        double amount = 0;
        try {
            String maininvoiceid = request.getParameter("maininvoiceid");//Billing Invoice
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(BillingInvoice.class.getName(), maininvoiceid);
            BillingInvoice binvoice = (BillingInvoice) cmpresult.getEntityList().get(0);
            Account dipositTo = binvoice.getCustomer().getAccount();
            String sequenceformat = request.getParameter("sequenceformat");

            double amountDiff = 0;
            boolean rateDecreased = false;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            double externalCurrencyRate = StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            String drAccDetails = request.getParameter("detail");
            boolean isMultiDebit = StringUtil.getBoolean(request.getParameter("ismultidebit"));
            String entryNumber = request.getParameter("no");
            String receiptid = request.getParameter("billid");

//            String methodid =request.getParameter("pmtmethod");
//            request.getSession().setAttribute("methodid", methodid);

            String jeid = null;
            String jeentryNumber = null;
            boolean jeautogenflag = false;
            HashMap billingPaymenthm = new HashMap();

            result = accVendorPaymentobj.getBillingPaymentFromNo(entryNumber, companyid);
            int count = result.getRecordTotalCount();
            if (count > 0) {
                throw new AccountingException(messageSource.getMessage("acc.field.Paymentnumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
            }
            boolean seqformat_oldflag = StringUtil.getBoolean(request.getParameter("seqformat_oldflag"));
            String nextAutoNo = "";
            String nextAutoNoInt = "";
            if (seqformat_oldflag) {
                nextAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_BILLINGPAYMENT, sequenceformat);
            } else {
                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_BILLINGPAYMENT, sequenceformat, seqformat_oldflag, new Date());
                nextAutoNo = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                nextAutoNoInt = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                billingPaymenthm.put(Constants.SEQFORMAT, sequenceformat);
                billingPaymenthm.put(Constants.SEQNUMBER, nextAutoNoInt);
            }
            billingPaymenthm.put("entrynumber", entryNumber);
            billingPaymenthm.put("autogenerated", entryNumber.equals(nextAutoNo));


            cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            currencyid = (request.getParameter("currencyid") == null ? currency.getCurrencyID() : request.getParameter("currencyid"));

            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);



            billingPaymenthm.put("currencyid", currencyid);

//            result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), request.getParameter("pmtmethod"));
//            PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);
//
//            dipositTo = payMethod.getAccount();
//            HashMap bpdetailhm = new HashMap();
//            bpdetailhm.put("paymethodid", payMethod.getID());
//            bpdetailhm.put("companyid", companyid);
//              KwlReturnObject pdresult=null;
//            if (!StringUtil.isNullOrEmpty(receiptid)&&!StringUtil.isNullOrEmpty(payDetailID))
//                bpdetailhm.put("paydetailid", payDetailID);
//                pdresult = accPaymentDAOobj.addPayDetail(bpdetailhm);
//
//            PayDetail pdetail = (PayDetail) pdresult.getEntityList().get(0);
//            billingPaymenthm.put("paydetailsid", pdetail.getID());
            billingPaymenthm.put("memo", request.getParameter("memo"));
            billingPaymenthm.put("companyid", companyid);
            billingPaymenthm.put("deleted", false);

            if (StringUtil.isNullOrEmpty(oldjeid)) {
                String nextJEAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_JOURNALENTRY);
                jeentryNumber = nextJEAutoNo;// + "/" + entryNumber;
                jeautogenflag = true;
            }
            HashMap<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
            jeDataMap.put("entrynumber", jeentryNumber);
            jeDataMap.put("autogenerated", jeautogenflag);
            jeDataMap.put("entrydate", df.parse(request.getParameter("creationdate")));
            jeDataMap.put("companyid", companyid);
            jeDataMap.put("memo", "Contra Entry : " + request.getParameter("memo"));
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
            HashMap<String, Object> jParam = new HashMap();
            KwlReturnObject jedresult = null;
            JournalEntryDetail jed = null;

            if (jArr.length() > 0 && !isMultiDebit) {
                amount = 0;
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);
                    amount += jobj.getDouble("payment");
                }
//                amountDiff = oldBillingPaymentRowsAmount(request, jArr, currencyid, externalCurrencyRate);
//                if (preferences.getForeignexchange() == null) {
//                    throw new AccountingException(messageSource.getMessage("acc.pay.forex", null, RequestContextUtils.getLocale(request)));
//                }
//                if (amountDiff != 0 && preferences.getForeignexchange() != null) {
//                    if(amountDiff < 0) {
//                        rateDecreased = true;
//                    }
//                    jParam.put("srno", jedetails.size()+1);
//                    jParam.put("companyid", companyid);
//                    jParam.put("amount", rateDecreased?(-1*amountDiff):amountDiff);
//                    jParam.put("accountid", preferences.getForeignexchange().getID());
//                    jParam.put("debit", rateDecreased?true:false);
//                    jParam.put("jeid", jeid);
//                    jedresult = accJournalEntryobj.updateJournalEntryDetails(jParam);
//                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
//                    jedetails.add(jed);
//                }
            } else {
                amount = Double.parseDouble(request.getParameter("amount"));
            }
            if (isMultiDebit) {
                JSONArray drAccArr = new JSONArray(drAccDetails);
                for (int i = 0; i < drAccArr.length(); i++) {
                    JSONObject jobj = drAccArr.getJSONObject(i);
                    jParam = new HashMap();
                    jParam.put("srno", jedetails.size() + 1);
                    jParam.put("companyid", companyid);
                    jParam.put("amount", Double.parseDouble(jobj.getString("dramount")));
                    jParam.put("accountid", jobj.getString("accountid"));
                    jParam.put("debit", true);
                    jParam.put("jeid", jeid);
                    jParam.put("description", jobj.get("description"));
                    jedresult = accJournalEntryobj.updateJournalEntryDetails(jParam);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                }
            } else {

                jParam = new HashMap();
                jParam.put("srno", jedetails.size() + 1);
                jParam.put("companyid", companyid);
                jParam.put("amount", amount + amountDiff);
                jParam.put("accountid", request.getParameter("accid"));
                jParam.put("debit", true);
                jParam.put("jeid", jeid);
                jedresult = accJournalEntryobj.updateJournalEntryDetails(jParam);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jedetails.add(jed);
            }



            jParam = new HashMap();
            jParam.put("srno", jedetails.size() + 1);
            jParam.put("companyid", companyid);
            //(If currency USD and base currency SGD, inv rate 0.81 n pay rate 0.80 then amount = 1234.57, amountDiff = -15.24, depositAmt = 1250 SGD)
            jParam.put("amount", amount);
            jParam.put("accountid", dipositTo.getID());
            jParam.put("debit", false);
            jParam.put("jeid", jeid);
            jedresult = accJournalEntryobj.updateJournalEntryDetails(jParam);


            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetails.add(jed);

            jeDataMap.put("jedetails", jedetails);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            billingPaymenthm.put("journalentryid", journalEntry.getID());
            billingPaymenthm.put("contraentry", true);
            if (payment != null) {
                billingPaymenthm.put("billingPaymentid", payment.getID());
            }
            result = accVendorPaymentobj.saveBillingPayment(billingPaymenthm);
            payment = (BillingPayment) result.getEntityList().get(0);

            if (!StringUtil.isNullOrEmpty(receiptid)) {
                billingPaymenthm.put("billingpaymentid", payment.getID());
            }
            //Save Payment Details
            HashSet payDetails = saveBillingPaymentRows(payment, company, jArr, isMultiDebit, binvoice);
            billingPaymenthm.put("billingPaymentid", payment.getID());
            billingPaymenthm.put("externalCurrencyRate", externalCurrencyRate);
            billingPaymenthm.put("bpdetails", payDetails);

            result = accVendorPaymentobj.saveBillingPayment(billingPaymenthm);
            payment = (BillingPayment) result.getEntityList().get(0);

            //Insert new entries again in optimized table.
            accJournalEntryobj.saveAccountJEs_optimized(jeid);

            list.add(payment);

        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        ll.add(new String[]{oldjeid, Cardid});
        KwlReturnObject accresult = accountingHandlerDAOobj.getObject(Account.class.getName(), request.getParameter("accid"));
        Account account = (Account) accresult.getEntityList().get(0);
        String accountName = "";
        if (account != null) {
            accountName = account.getName();
        }
        ll.add(new String[]{"amount", String.valueOf(amount)});
        ll.add(new String[]{"amountinword", netinword});
        ll.add(new String[]{"accountName", accountName});
        return (ArrayList) ll;
    }

    public HashSet savePaymentRows(Payment payment, Company company, JSONArray jArr, boolean isMultiDebit, Invoice invoice) throws JSONException, ServiceException {
        HashSet pdetails = new HashSet();
        if (!isMultiDebit) {
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                PaymentDetail pd = new PaymentDetail();
                pd.setSrno(i + 1);
                pd.setID(StringUtil.generateUUID());
                double amountReceived = jobj.getDouble("payment");
                double amountReceivedConverted = jobj.getDouble("payment");
                pd.setAmount(jobj.getDouble("payment"));

                if (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString()) && !StringUtil.isNullOrEmpty(jobj.optString("currencyidtransaction", "")) && !jobj.optString("currencyidtransaction", "").equals(payment.getCurrency().getCurrencyID())) {
                    pd.setExchangeRateForTransaction(Double.parseDouble(jobj.get("exchangeratefortransaction").toString()));
                    KwlReturnObject resultCurrency = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), jobj.getString("currencyidtransaction"));
                    KWLCurrency kWLCurrency = (KWLCurrency) resultCurrency.getEntityList().get(0);
                    pd.setFromCurrency(kWLCurrency);
                    pd.setToCurrency(payment.getCurrency());
                    amountReceivedConverted = amountReceived / Double.parseDouble(jobj.get("exchangeratefortransaction").toString());
                }
                pd.setGstCurrencyRate(jobj.optDouble("gstCurrencyRate",0.0));
                pd.setCompany(company);
                //            pd.setGoodsReceipt((GoodsReceipt) session.get(GoodsReceipt.class, jobj.getString("billid")));
                KwlReturnObject result = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), jobj.getString("billid"));
                GoodsReceipt goodsReceipt = (GoodsReceipt) result.getEntityList().get(0);
                pd.setGoodsReceipt((GoodsReceipt) result.getEntityList().get(0));
                if (invoice != null) {
                    pd.setInvoice(invoice);
                }
                pd.setPayment(payment);
                if (jobj.has("rowjedid")) {
                    pd.setROWJEDID(jobj.getString("rowjedid"));
                }
                
                double amountReceivedConvertedInBaseCurrency = 0d;
                HashMap<String, Object> requestParams = new HashMap();
                requestParams.put(Constants.companyid, company.getCompanyID());
                requestParams.put("gcurrencyid", company.getCurrency().getCurrencyID());
                double externalCurrencyRate = 1d;
                boolean isopeningBalancePM = payment.isIsOpeningBalencePayment();
                Date PMCreationDate = null;
                PMCreationDate = payment.getCreationDate();
                if (isopeningBalancePM) {
                    externalCurrencyRate = payment.getExchangeRateForOpeningTransaction();
                } else {
//                    PMCreationDate = payment.getJournalEntry().getEntryDate();
                    externalCurrencyRate = payment.getJournalEntry().getExternalCurrencyRate();
                }
                String fromcurrencyid = payment.getCurrency().getCurrencyID();
                KwlReturnObject bAmt = null;
                if (isopeningBalancePM && payment.isConversionRateFromCurrencyToBase()) {// if Payment is opening balance Payment and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountReceived, fromcurrencyid, PMCreationDate, externalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountReceived, fromcurrencyid, PMCreationDate, externalCurrencyRate);
                }
                amountReceivedConvertedInBaseCurrency = (Double) bAmt.getEntityList().get(0);

                updateInvoiceAmountDue(goodsReceipt, payment, company, amountReceivedConverted, amountReceivedConvertedInBaseCurrency);
                updatePaymentAmountDue(payment, company, amountReceived, amountReceivedConvertedInBaseCurrency);
                pdetails.add(pd);
            }
        }
        return pdetails;
    }

    /*
     * Update vendor invoice due amount when payment is being made against that
     * vendor invoice.
     */
    public void updateInvoiceAmountDue(GoodsReceipt goodsReceipt, Payment payment, Company company, double amountReceivedForGoodsReceipt, double baseAmountReceivedForGoodsReceipt) throws JSONException, ServiceException {
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

    /*
     * Update payment due amount when payment is being made.
     */
    public void updatePaymentAmountDue(Payment payment, Company company, double amountReceived, double baseAmountReceived) throws JSONException, ServiceException {
        if (payment != null) {
            double receiptAmountDue = payment.getOpeningBalanceAmountDue();
            receiptAmountDue -= amountReceived;
            HashMap paymenthm = new HashMap();
            paymenthm.put("openingBalanceAmountDue", receiptAmountDue);
            paymenthm.put(Constants.openingBalanceBaseAmountDue, payment.getOpeningBalanceBaseAmountDue() - baseAmountReceived);
            paymenthm.put("paymentid", payment.getID());
            paymenthm.put("currencyid", payment.getCurrency().getCurrencyID());
            paymenthm.put("companyid", company.getCompanyID());

//            String createdby = sessionHandlerImpl.getUserid(request);
//            String modifiedby = sessionHandlerImpl.getUserid(request);
//            long createdon = System.currentTimeMillis();
//            long updatedon = System.currentTimeMillis();
            accVendorPaymentobj.savePayment(paymenthm);
        }
    }

    public HashSet saveBillingPaymentRows(BillingPayment payment, Company company, JSONArray jArr, boolean isMultiDebit, BillingInvoice binvoice) throws JSONException, ServiceException {
        HashSet hs = new HashSet();
        if (!isMultiDebit) {
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                BillingPaymentDetail rd = new BillingPaymentDetail();
                rd.setSrno(i + 1);
                rd.setAmount(jobj.getDouble("payment"));
                rd.setCompany(company);
//            rd.setBillingGoodsReceipt((BillingGoodsReceipt) session.get(BillingGoodsReceipt.class, jobj.getString("billid")));
                KwlReturnObject result = accountingHandlerDAOobj.getObject(BillingGoodsReceipt.class.getName(), jobj.getString("billid"));
                rd.setBillingGoodsReceipt((BillingGoodsReceipt) result.getEntityList().get(0));
                if (binvoice != null) {
                    rd.setBillingInvoice(binvoice);
                }
                rd.setBillingPayment(payment);
//            amount += jobj.getDouble("payment");
                hs.add(rd);
            }
        }
//        payment.setRows(hs);
        return hs;
    }

    public double oldPaymentRowsAmount(HttpServletRequest request, JSONArray jArr, String currencyid, double externalCurrencyRate) throws ServiceException, SessionExpiredException {
        double amount = 0, actualAmount = 0;
        try {
            String basecurrency = sessionHandlerImpl.getCurrencyID(request);
            HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
            GlobalParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            GlobalParams.put("gcurrencyid", basecurrency);
            GlobalParams.put("dateformat", authHandler.getDateOnlyFormat(request));
            Date creationDate = authHandler.getDateOnlyFormat(request).parse(request.getParameter("creationdate"));
            for (int i = 0; i < jArr.length(); i++) {
                double ratio = 0;
                JSONObject jobj = jArr.getJSONObject(i);
//                boolean revalFlag=false;
//            GoodsReceipt gr = (GoodsReceipt) session.get(GoodsReceipt.class, jobj.getString("billid"));
                double newrate = 0.0;
                boolean revalFlag = false;
                KwlReturnObject result = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), jobj.getString("billid"));
                GoodsReceipt gr = (GoodsReceipt) result.getEntityList().get(0);
                boolean isopeningBalanceInvoice = gr.isIsOpeningBalenceInvoice();
                Double recinvamount = jobj.getDouble("payment");
                boolean isopeningBalancePayment = jobj.optBoolean("isopeningBalancePayment", false);
                boolean isConversionRateFromCurrencyToBase = jobj.optBoolean("isConversionRateFromCurrencyToBase", false);
                double exchangeRate = 0d;
                Date goodsReceiptCreationDate = null;
                if (gr.isNormalInvoice()) {
                    exchangeRate = gr.getJournalEntry().getExternalCurrencyRate();
//                    goodsReceiptCreationDate = gr.getJournalEntry().getEntryDate();
                } else {
                    exchangeRate = gr.getExchangeRateForOpeningTransaction();
                }
                goodsReceiptCreationDate = gr.getCreationDate();


                HashMap<String, Object> invoiceId = new HashMap<String, Object>();
                invoiceId.put("invoiceid", gr.getID());
                invoiceId.put("companyid", sessionHandlerImpl.getCompanyid(request));
//                if (gr.isNormalInvoice()) {
                result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                if (history != null) {
                    exchangeRate = history.getEvalrate();
                    newrate = jobj.optDouble("exchangeratefortransaction", 0.0);
                    revalFlag = true;
                }
//                }
//            KWLCurrency currency = (KWLCurrency) session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
                result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrency);
                KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
                String currid = currency.getCurrencyID();
                if (gr.getCurrency() != null) {
                    currid = gr.getCurrency().getCurrencyID();
                }

                KwlReturnObject bAmt = null;
                if (currid.equalsIgnoreCase(currencyid)) {
                    if (history == null && isopeningBalanceInvoice && gr.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOtherAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
                    } else {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOther(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
                    }
                } else {
                    if (history == null && isopeningBalanceInvoice && gr.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModifiedAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
                    } else {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
                    }
                }
                double oldrate = (Double) bAmt.getEntityList().get(0);
                if (jobj.optDouble("exchangeratefortransaction", 0.0) != oldrate && jobj.optDouble("exchangeratefortransaction", 0.0) != 0.0 && Math.abs(jobj.optDouble("exchangeratefortransaction", 0.0) - oldrate) >= 0.000001) {
                    newrate = jobj.optDouble("exchangeratefortransaction", 0.0);
                    ratio = oldrate - newrate;
                    amount = (recinvamount - (recinvamount / newrate) * oldrate) / newrate;
                    KwlReturnObject bAmtActual = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, amount, currencyid, creationDate, newrate);
                    actualAmount += (Double) bAmtActual.getEntityList().get(0);
                } else {
                    if (currid.equalsIgnoreCase(currencyid)) {
                        if (history == null && isopeningBalanceInvoice && gr.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getOneCurrencyToOtherAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
                        } else {
                            bAmt = accCurrencyDAOobj.getOneCurrencyToOther(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
                        }
                    } else {
                        if (history == null && isopeningBalanceInvoice && gr.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModifiedAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
                        } else {
                            bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
                        }
                    }
                    if (!revalFlag) {
                        newrate = (Double) bAmt.getEntityList().get(0);
                    }
                    if (Math.abs(jobj.optDouble("exchangeratefortransaction", 0.0) - oldrate) >= 0.000001) {
                        ratio = oldrate - newrate;
                    }
                    amount = recinvamount * ratio;
                    KwlReturnObject bAmtActual = null;
                    if (isopeningBalancePayment && isConversionRateFromCurrencyToBase) {// if payment is opening balance payment and Conversion rate is taken from user is Currency to base then following method will be called.
                        bAmtActual = accCurrencyDAOobj.getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, amount, currencyid, creationDate, externalCurrencyRate);
                    } else {
                        bAmtActual = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, amount, currencyid, creationDate, externalCurrencyRate);
                    }
//                        if(ratio>0){
//                            actualAmount += (Double) bAmtActual.getEntityList().get(0);
//                         }else{
//                            actualAmount -= (Double) bAmtActual.getEntityList().get(0);
//                         }
                    actualAmount += (Double) bAmtActual.getEntityList().get(0);
                }
            }
            //        amount = CompanyHandler.getBaseToCurrencyAmount(session, request, amount, currencyid, AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("oldPaymentRowsAmount : " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("oldPaymentRowsAmount : " + ex.getMessage(), ex);
        }
        return (actualAmount);
    }

    private double oldBillingPaymentRowsAmount(HttpServletRequest request, JSONArray jArr, String currencyid, double externalCurrencyRate) throws ServiceException, SessionExpiredException {
        double ratio = 0;
        double amount = 0;
        try {
            KwlReturnObject result;
            HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
            GlobalParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            GlobalParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
            GlobalParams.put("dateformat", authHandler.getDateOnlyFormat(request));
            Date creationDate = authHandler.getDateOnlyFormat(request).parse(request.getParameter("creationdate"));
            for (int i = 0; i < jArr.length(); i++) {
                boolean revalFlag = false;
                JSONObject jobj = jArr.getJSONObject(i);
//            BillingGoodsReceipt bgr=(BillingGoodsReceipt) session.get(BillingGoodsReceipt.class, jobj.getString("billid"));
                result = accountingHandlerDAOobj.getObject(BillingGoodsReceipt.class.getName(), jobj.getString("billid"));
                BillingGoodsReceipt bgr = (BillingGoodsReceipt) result.getEntityList().get(0);
                double exchangeRate = bgr.getJournalEntry().getExternalCurrencyRate();
                HashMap<String, Object> invoiceId = new HashMap<String, Object>();
                invoiceId.put("invoiceid", bgr.getID());
                invoiceId.put("companyid", sessionHandlerImpl.getCompanyid(request));
                result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                if (history != null) {
                    exchangeRate = history.getEvalrate();
                    revalFlag = true;
                }
                Double recinvamount = jobj.getDouble("payment");
//            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
                result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
                KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);

                String currid = currency.getCurrencyID();
                if (bgr.getCurrency() != null) {
                    currid = bgr.getCurrency().getCurrencyID();
                }
//            double oldrate=CompanyHandler.getCurrencyToBaseAmount(session,request,1.0,currid,bgr.getJournalEntry().getEntryDate(),externalCurrencyRate);
                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, bgr.getJournalEntry().getEntryDate(), exchangeRate);
                double oldrate = (Double) bAmt.getEntityList().get(0);

//            double  newrate=CompanyHandler.getCurrencyToBaseAmount(session,request,1.0,currid,AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")),externalCurrencyRate);
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, creationDate, externalCurrencyRate);
                double newrate = (Double) bAmt.getEntityList().get(0);

                ratio = oldrate - newrate;
                amount += recinvamount * ratio;

            }
//         amount=CompanyHandler.getBaseToCurrencyAmount(session,request,amount,currencyid,AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")),externalCurrencyRate);
            KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, amount, currencyid, creationDate, externalCurrencyRate);
            amount = (Double) bAmt.getEntityList().get(0);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("oldPaymentRowsAmount : " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("oldPaymentRowsAmount : " + ex.getMessage(), ex);
        }
        return (amount);
    }

    public ModelAndView getSinglePaymentDataToLoad(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = getPaymentMap(request);
            String billid = request.getParameter("billid") != null ? request.getParameter("billid") : "";
            requestParams.put("billid", billid);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            requestParams.put("df", df);
            KwlReturnObject result = null;
            result = accVendorPaymentobj.getPayments(requestParams);
            Object[] paymentDetails = (Object[]) result.getEntityList().get(0);
            Payment payment = (Payment) paymentDetails[0];
            Account acc = (Account) paymentDetails[1];
            JSONObject paymentObj = getPaymentInfo(payment, acc, requestParams);
            jobj.put("data", paymentObj);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONObject getPaymentInfo(Payment payment, Account acc, HashMap<String, Object> requestParams) throws JSONException, ServiceException, UnsupportedEncodingException {
        DateFormat df = (DateFormat) requestParams.get("df");
        JSONObject obj = new JSONObject();
        obj.put("billid", payment.getID());
//        obj.put("billdate", df.format(payment.getJournalEntry().getEntryDate()));
        obj.put("billdate", df.format(payment.getCreationDate()));
        obj.put("companyid", payment.getCompany().getCompanyID());
        obj.put("companyname", payment.getCompany().getCompanyName());
        obj.put("entryno", payment.getJournalEntry().getEntryNumber());
        obj.put("journalentryid", payment.getJournalEntry().getID());
        obj.put("isadvancepayment", payment.isIsadvancepayment());
        obj.put("ismanydbcr", payment.isIsmanydbcr());
        obj.put("isprinted", payment.isPrinted());
        obj.put("bankCharges", payment.getBankChargesAmount());
        obj.put("bankChargesCmb", payment.getBankChargesAccount() != null ? payment.getBankChargesAccount().getID() : "");
        obj.put("bankChargesCmbValue", payment.getBankChargesAccount() != null ? payment.getBankChargesAccount().getName() : "");
        obj.put("bankInterest", payment.getBankInterestAmount());
        obj.put("bankInterestCmb", payment.getBankInterestAccount() != null ? payment.getBankInterestAccount().getID() : "");
        obj.put("bankInterestCmbValue", payment.getBankInterestAccount() != null ? payment.getBankInterestAccount().getName() : "");
        obj.put("paidToCmb", payment.getPaidTo() != null ? payment.getPaidTo().getID() : ""); 
        obj.put("paidToCmbValue", payment.getPaidTo() != null ? payment.getPaidTo().getValue(): "");
        obj.put(Constants.SEQUENCEFORMATID, payment.getSeqformat() == null ? "" : payment.getSeqformat().getID());
        obj.put("sequenceformatvalue", payment.getSeqformat() == null ? "" : payment.getSeqformat().getName());
        Customer customer = null;
        
        if (payment.getCustomer() != null) {
            KwlReturnObject custResult = accountingHandlerDAOobj.getObject(Customer.class.getName(), payment.getCustomer());
            customer = (Customer) custResult.getEntityList().get(0);
        }
        Vendor vendor = payment.getVendor();
        obj.put("personid", (vendor != null) ? vendor.getID() : acc.getID());
        KwlReturnObject result = accVendorPaymentobj.getPaymentVendorNames(payment.getCompany().getCompanyID(), payment.getID());
        List vNameList = result.getEntityList();
        Iterator vNamesItr = vNameList.iterator();
        String vendorNames = "";
        while (vNamesItr.hasNext()) {
            String tempName = URLEncoder.encode((String) vNamesItr.next(), "UTF-8");
            vendorNames += tempName;
            vendorNames += ",";
        }
        vendorNames = vendorNames.substring(0, Math.max(0, vendorNames.length() - 1));
        obj.put("personname", StringUtil.DecodeText((payment.getReceipttype() == 9 || payment.getReceipttype() == 2) ? vendorNames : ((vendor != null) ? vendor.getName() : (customer != null) ? customer.getName() : "")));    //Used decoder to avoid '+' symbol at white/empty space between words. 
                
        obj.put("memo", payment.getMemo());
        obj.put("deleted", payment.isDeleted());
        obj.put("currencysymbol",payment.getCurrency().getSymbol());
        obj.put("externalcurrencyrate", payment.getExternalCurrencyRate());
        obj.put("chequenumber", payment.getPayDetail() == null ? "" : (payment.getPayDetail().getPaymentMethod().getDetailType() == PaymentMethod.TYPE_BANK) ? (payment.getPayDetail().getCheque() != null ? payment.getPayDetail().getCheque().getChequeNo() : "") : "");
        obj.put("chequedescription", payment.getPayDetail() == null ? "" : (payment.getPayDetail().getPaymentMethod().getDetailType() == PaymentMethod.TYPE_BANK) ? (payment.getPayDetail().getCheque() != null ? (payment.getPayDetail().getCheque().getDescription() != null ? payment.getPayDetail().getCheque().getDescription() : "") : "") : "");
        obj.put("currencyid", payment.getCurrency().getCurrencyID());
        obj.put("paymentmethodname", (payment.getPayDetail() == null ? "" : payment.getPayDetail().getPaymentMethod().getMethodName()));
        obj.put("detailtype", (payment.getPayDetail() == null ? "" : payment.getPayDetail().getPaymentMethod().getDetailType()));
        obj.put("paymentmethod", (payment.getPayDetail() == null ? "" : payment.getPayDetail().getPaymentMethod().getID()));
        obj.put("paymentmethodacc", (payment.getPayDetail() == null ? "" : payment.getPayDetail().getPaymentMethod().getAccount().getID()));
        obj.put("paymentmethodaccname", (payment.getPayDetail() == null ? "" : payment.getPayDetail().getPaymentMethod().getAccount().getName()));
        
        if (payment.isIBGTypeTransaction()) {
            obj.put("isIBGTypeTransaction", payment.isIBGTypeTransaction());
            obj.put("ibgDetailsID", payment.getIbgreceivingbankdetails() == null ? "" : payment.getIbgreceivingbankdetails().getId());
            obj.put("ibgCode", payment.getIbgCode());
        }
        return obj;
    }
        
    public ModelAndView getPayments(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        List<JSONObject> list = new ArrayList<JSONObject>();
        List<JSONObject> tempList = new ArrayList<JSONObject>();
        int limitValue = 0, startValue = 0, dataCount = 0;
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = getPaymentMap(request);
            String start = (String) requestParams.get("start");
            String limit = (String) requestParams.get("limit");
            boolean contraentryflag = request.getParameter("contraentryflag") != null;
            boolean isAdvancePayment = request.getParameter("advancePayment") != null;
            boolean isAdvanceToCustomer = request.getParameter("advanceToCustomer") != null;
            boolean isPostDatedCheque = request.getParameter("isPostDatedCheque") != null;
            boolean isDishonouredCheque = request.getParameter("isDishonouredCheque") != null;
            boolean isGlcode = request.getParameter("isGlcode") != null;
            String billid = request.getParameter("billid") != null ? request.getParameter("billid") : "";

            boolean onlyOpeningBalanceTransactionsFlag = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("onlyOpeningBalanceTransactionsFlag"))) {
                onlyOpeningBalanceTransactionsFlag = Boolean.parseBoolean(request.getParameter("onlyOpeningBalanceTransactionsFlag"));
            }

            requestParams.put("contraentryflag", contraentryflag);
            requestParams.put("isadvancepayment", isAdvancePayment);
            requestParams.put("isadvancetocustomer", isAdvanceToCustomer);
            requestParams.put("isPostDatedCheque", isPostDatedCheque);
            requestParams.put("isDishonouredCheque", isDishonouredCheque);
            requestParams.put("isGlcode", isGlcode);
            requestParams.put("billid", billid);
            if (!StringUtil.isNullOrEmpty(request.getParameter("recordType"))) {
                requestParams.put("receipttype", request.getParameter("recordType"));
            }
            boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
            String[] companyids = (consolidateFlag && request.getParameter("companyids") != null) ? request.getParameter("companyids").split(",") : sessionHandlerImpl.getCompanyid(request).split(",");
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid") != null) ? request.getParameter("gcurrencyid") : sessionHandlerImpl.getCurrencyID(request);
            boolean eliminateflag = consolidateFlag;
//            if(consolidateFlag) {
            requestParams.put(Constants.start, start);
            requestParams.put(Constants.limit, limit);
//            }
            KwlReturnObject result = null;
            KwlReturnObject openingBalanceReceiptsResult = null;
            KwlReturnObject billingResult = null;
            String companyid = "";
            int totalCnt = 0;
            for (int cnt = 0; cnt < companyids.length; cnt++) {
                companyid = companyids[cnt];
                request.setAttribute("companyid", companyid);
                request.setAttribute("gcurrencyid", gcurrencyid);
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);

                tempList.clear();
                if (onlyOpeningBalanceTransactionsFlag) {
                    // getting opening balance receipts
                    openingBalanceReceiptsResult = accVendorPaymentobj.getAllOpeningBalancePayments(requestParams);
                    totalCnt +=openingBalanceReceiptsResult.getRecordTotalCount();
                    JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
//                    tempList = accVendorPaymentModuleServiceObj.getOpeningBalanceReceiptJsonForReport(request, openingBalanceReceiptsResult.getEntityList(), tempList);
                    tempList = accVendorPaymentModuleServiceObj.getOpeningBalanceReceiptJsonForReport(paramJobj, openingBalanceReceiptsResult.getEntityList(), tempList);
                } else {
                    result = accVendorPaymentobj.getPayments(requestParams);
                    totalCnt +=result.getRecordTotalCount();
                    tempList = accVendorPaymentModuleServiceObj.getPaymentsJson(requestParams, result.getEntityList(), tempList);
                    billingResult = accVendorPaymentobj.getBillingPayments(requestParams);
                    tempList = accVendorPaymentModuleServiceObj.getBillingPaymentsJson(requestParams, billingResult.getEntityList(), tempList);
                }
                Collections.sort(tempList, Collections.reverseOrder(new VendorPaymentDateComparator()));
                list.addAll(tempList);
            }

            if(companyids.length > 1) {
                if (!StringUtil.isNullOrEmpty(limit) && !StringUtil.isNullOrEmpty(start)) {
                    limitValue = Integer.parseInt(limit);
                    startValue = Integer.parseInt(start);
                } else {
                    limitValue = list.size();
                    startValue = 0;
                }
                Iterator iterator = list.iterator();
                for (int i = 0; i < list.size(); i++) {
                    if (i >= startValue && dataCount < limitValue) {
                        JSONObject jSONObject = (JSONObject) iterator.next();
                        jArr.put(jSONObject);
                        dataCount++;
                    } else {
                        iterator.next();
                    }
                    if (dataCount == limitValue) {
                        break;
                    }
                }
            } else {
                for (Object obj : list) {
                    JSONObject jSONObject = (JSONObject) obj;
                    jArr.put(jSONObject);
                    dataCount++;
                }
            }
            jobj.put("data", jArr);
            jobj.put("count", totalCnt);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getOpeningBalancePayments(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = getPaymentMap(request);
            String vendorId = request.getParameter("custVenId");
            requestParams.put(InvoiceConstants.vendorid, vendorId);
            // get opening balance Payments created from opening balance button.

            KwlReturnObject result = accVendorPaymentobj.getOpeningBalancePayments(requestParams);
            List<Payment> list = result.getEntityList();
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
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }

        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void getOpeningBalanceReceiptJson(HttpServletRequest request, List<Payment> list, JSONArray dataArray) {
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");
            HashMap<String, Object> requestParams = getPaymentMap(request);
            if (list != null && !list.isEmpty()) {
                Iterator it = list.iterator();
                while (it.hasNext()) {
                    Payment payment = (Payment) it.next();

                    Date receiptCreationDate = null;
                    Double receiptAmount = 0d;

                    receiptCreationDate = payment.getCreationDate();
                    receiptAmount = payment.getDepositAmount();

                    double exchangeRateForOtherCurrency = payment.getExchangeRateForOpeningTransaction();
//                    double exchangeRateForOtherCurrencyModifiedForJS = 0d;
                    boolean isopeningBalancePayment = payment.isIsOpeningBalencePayment();

                    JSONObject receiptJson = new JSONObject();
                    receiptJson.put("methodid", payment.getPayDetail() == null ? "" : payment.getPayDetail().getPaymentMethod() == null ? "" : payment.getPayDetail().getPaymentMethod().getID());
                    receiptJson.put("detailtype", (payment.getPayDetail() == null ? "" : payment.getPayDetail().getPaymentMethod().getDetailType()));
                    if (payment.getPayDetail() != null) {
                        try {
                            receiptJson.put("expirydate", (payment.getPayDetail().getCard() == null ? "" : df.format(payment.getPayDetail().getCard().getExpiryDate())));
                        } catch (IllegalArgumentException ae) {
                            receiptJson.put("expirydate", "");
                        }
                        try {
                            receiptJson.put("dueDate", (payment.getPayDetail().getCheque() == null ? "" : df.format(payment.getPayDetail().getCheque().getDueDate())));
                        } catch (IllegalArgumentException ae) {
                            receiptJson.put("dueDate", "");
                        }
                        receiptJson.put("refdetail", (payment.getPayDetail().getCard() == null ? (payment.getPayDetail().getCheque() == null ? "" : payment.getPayDetail().getCheque().getDescription()) : payment.getPayDetail().getCard().getCardType()));
                        receiptJson.put("refno", (payment.getPayDetail().getCard() == null ? (payment.getPayDetail().getCheque() == null ? "" : payment.getPayDetail().getCheque().getChequeNo()) : payment.getPayDetail().getCard().getRefNo()));
                        receiptJson.put("refname", (payment.getPayDetail().getCard() == null ? (payment.getPayDetail().getCheque() == null ? "" : payment.getPayDetail().getCheque().getBankName()) : payment.getPayDetail().getCard().getCardHolder()));
                        if (payment.getPayDetail().getCard() != null) {
                            receiptJson.put("refcardno", payment.getPayDetail().getCard().getCardNo());
                        }
                    }
                    receiptJson.put("clearanceDate", "");
                    receiptJson.put("paymentStatus", false);
                    if (payment.getPayDetail() != null) {
                        KwlReturnObject clearanceDate = accBankReconciliationObj.getBRWithoutJE(payment.getID(), payment.getCompany().getCompanyID(), Constants.Acc_Make_Payment_ModuleId);
                        if (clearanceDate != null && clearanceDate.getEntityList() != null && clearanceDate.getEntityList().size() > 0) {
                            BankReconciliationDetail brd = (BankReconciliationDetail) clearanceDate.getEntityList().get(0);
                            if (brd.getBankReconciliation().getClearanceDate() != null) {
                                receiptJson.put("clearanceDate", df.format(brd.getBankReconciliation().getClearanceDate()));
                                receiptJson.put("paymentStatus", true);
                            }
                        }
                    }
                    receiptJson.put("transactionId", payment.getID());
                    receiptJson.put("exchangeRateForOtherCurrency", exchangeRateForOtherCurrency);
                    receiptJson.put("isCurrencyToBaseExchangeRate", payment.isConversionRateFromCurrencyToBase());
                    receiptJson.put("isNormalTransaction", payment.isNormalPayment());
                    receiptJson.put("transactionNo", payment.getPaymentNumber());
                    receiptJson.put("transactionAmount", authHandler.formattedAmount(receiptAmount, companyid));
                    receiptJson.put("transactionDate", df.format(receiptCreationDate));
                    receiptJson.put("currencysymbol", (payment.getCurrency() == null ? "" : payment.getCurrency().getSymbol()));
                    receiptJson.put("currencyid", (payment.getCurrency() == null ? "" : payment.getCurrency().getCurrencyID()));
                    receiptJson.put("transactionAmountDue", authHandler.formattedAmount(payment.getOpeningBalanceAmountDue(), companyid));
                    receiptJson.put("chequeNumber", payment.getChequeNumber());
                    receiptJson.put("drawnOn", payment.getDrawnOn());
                    receiptJson.put("chequeDate", payment.getChequeDate() != null ? df.format(payment.getChequeDate()) : "");
                    receiptJson.put("memo", (StringUtil.isNullOrEmpty(payment.getMemo()) ? "" : payment.getMemo()));
                    if((payment.getLinkDetailPayments()!=null&&!payment.getLinkDetailPayments().isEmpty()) || (payment.getLinkDetailPaymentToCreditNote()!=null && !payment.getLinkDetailPaymentToCreditNote().isEmpty())){
                        receiptJson.put("isPaymentLinked", true);
                    }else{
                        receiptJson.put("isPaymentLinked", false);
                    }
                    double transactionAmountInBase = 0d;
                    if (Constants.OpeningBalanceBaseAmountFlag) {
                        transactionAmountInBase = payment.getOriginalOpeningBalanceBaseAmount();
                    } else {
                        KwlReturnObject bAmt = null;
                        if (isopeningBalancePayment && payment.isConversionRateFromCurrencyToBase()) {// if payment is opening balance payment and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, receiptAmount, payment.getCurrency().getCurrencyID(), payment.getCreationDate(), exchangeRateForOtherCurrency);
                        } else {
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, receiptAmount, payment.getCurrency().getCurrencyID(), payment.getCreationDate(), exchangeRateForOtherCurrency);
                        }
                        transactionAmountInBase = (Double) bAmt.getEntityList().get(0);
                    }
                    receiptJson.put("transactionAmountInBase", authHandler.formattedAmount(transactionAmountInBase, companyid));
                    int isReval = 0;
                    KwlReturnObject brdAmt = accGoodsReceiptobj.getRevalFlag(payment.getID());
                    List reval = brdAmt.getEntityList();
                    if (!reval.isEmpty() && (Long) reval.get(0) > 0) {
                        isReval = 1;
                    }
                    receiptJson.put("isreval", isReval);
                    dataArray.put(receiptJson);
                }
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

    }

    public static HashMap<String, Object> getPaymentMap(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put("start", request.getParameter("start"));
        requestParams.put("limit", request.getParameter("limit"));
        requestParams.put("ss", request.getParameter("ss"));
        requestParams.put("deleted", request.getParameter("deleted"));
        requestParams.put("nondeleted", request.getParameter("nondeleted"));
        requestParams.put(Constants.REQ_startdate, request.getParameter("stdate"));
        requestParams.put(Constants.REQ_enddate, request.getParameter("enddate"));
        requestParams.put(Constants.Acc_Search_Json, request.getParameter(Constants.Acc_Search_Json));
        requestParams.put(Constants.Filter_Criteria, request.getParameter(InvoiceConstants.Filter_Criteria));
        requestParams.put(Constants.moduleid, request.getParameter(Constants.moduleid));
        if (request.getParameter("dir") != null && !StringUtil.isNullOrEmpty(request.getParameter("dir"))
                && request.getParameter("sort") != null && !StringUtil.isNullOrEmpty(request.getParameter("sort"))) {
            requestParams.put("dir", request.getParameter("dir"));
            requestParams.put("sort", request.getParameter("sort"));
        }
        if (!StringUtil.isNullOrEmpty(sessionHandlerImpl.getBrowserTZ(request))) {
            requestParams.put("browsertz", sessionHandlerImpl.getBrowserTZ(request));
        }
        return requestParams;
    }

    public static HashMap<String, Object> getPaymentMapJson(JSONObject paramJobj) throws SessionExpiredException, JSONException, ServiceException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParamsJson(paramJobj);
        requestParams.put("start", paramJobj.optString("start",null));
        requestParams.put("limit", paramJobj.optString("limit",null));
        requestParams.put("ss", paramJobj.optString("ss",null));
        requestParams.put("deleted", paramJobj.optString("deleted",null));
        requestParams.put("nondeleted", paramJobj.optString("nondeleted",null));
        requestParams.put(Constants.REQ_startdate, paramJobj.optString("stdate",null));
        requestParams.put(Constants.REQ_enddate, paramJobj.optString("enddate",null));
        requestParams.put(Constants.Acc_Search_Json, paramJobj.optString(Constants.Acc_Search_Json,null));
        requestParams.put(Constants.Filter_Criteria, paramJobj.optString(InvoiceConstants.Filter_Criteria,null));
        requestParams.put(Constants.moduleid, paramJobj.optString(Constants.moduleid,null));
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("dir",null)) && !StringUtil.isNullOrEmpty(paramJobj.optString("sort",null))) {
            requestParams.put("dir", paramJobj.optString("dir",null));
            requestParams.put("sort", paramJobj.optString("sort",null));
        }
        return requestParams;
    }
    
    public List<JSONObject> getOpeningBalanceReceiptJsonForReport(HttpServletRequest request, List list, List<JSONObject> jsonObjectlist) {
        List<JSONObject> returnList = new ArrayList<JSONObject>();
        try {
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            Iterator itr = list.iterator();
            while (itr.hasNext()) {

                Payment payment = (Payment) itr.next();

                JSONObject obj = new JSONObject();
                obj.put("withoutinventory", false);
                obj.put("isOpeningBalanceTransaction", payment.isIsOpeningBalencePayment());
                obj.put("isNormalTransaction", payment.isNormalPayment());
//                KwlReturnObject vendorresult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), acc.getID());
                Vendor vendor = payment.getVendor();
                if (vendor != null) {
                    obj.put("address", vendor.getAddress());
                    obj.put("personemail", vendor.getEmail());
                } else {
                    obj.put("address", "");
                    obj.put("personemail", "");
                }
                obj.put("billid", payment.getID());
                obj.put("companyid", payment.getCompany().getCompanyID());
                obj.put("companyname", payment.getCompany().getCompanyName());
                obj.put("entryno", "");
                obj.put("journalentryid", "");
                obj.put("personid", vendor.getID());
                obj.put("billno", payment.getPaymentNumber());
                obj.put("isadvancepayment", payment.isIsadvancepayment());
                obj.put("isadvancefromvendor", false);
                obj.put("ismanydbcr", false);
                obj.put("bankCharges", 0.0);
                obj.put("bankChargesCmb", "");
                obj.put("bankInterest", 0.0);
                obj.put("bankInterestCmb", "");
                obj.put("paidToCmb", "");

                obj.put("advanceUsed", false);
                obj.put("advanceid", "");
                obj.put("advanceamount", 0.0);
                obj.put("advanceamounttype", 0);
                obj.put("billdate", authHandler.getDateOnlyFormat(request).format(payment.getCreationDate()));
//                obj.put("receipttype", receipt.getReceipttype());

                obj.put("amount", authHandler.formattedAmount(payment.getDepositAmount(), companyid));
                obj.put("personname", vendor == null ? "" : vendor.getName());
                obj.put("memo", "");
                obj.put("deleted", payment.isDeleted());
                obj.put("currencysymbol", (payment.getCurrency() == null ? currency.getSymbol() : payment.getCurrency().getSymbol()));
                obj.put("externalcurrencyrate", payment.getExternalCurrencyRate());
                obj.put("currencyid", (payment.getCurrency() == null ? currency.getCurrencyID() : payment.getCurrency().getCurrencyID()));
                obj.put("methodid", "");
                obj.put("detailtype", "");
                obj.put("paymentmethod", "");
                if (!payment.getRows().isEmpty()) {
                    obj.put("otherwise", false);
                } else {
                    obj.put("otherwise", true);
                }

                returnList.add(obj);
            }
        } catch (JSONException ex) {
            returnList = null;
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            returnList = null;
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            returnList = null;
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        if (returnList != null) {
            jsonObjectlist.addAll(returnList);
        }
        return jsonObjectlist;
    }

    public List<JSONObject> getPaymentsJson(HashMap<String, Object> requestParams, List list, List<JSONObject> jsonlist) throws ServiceException {
        //JSONObject jobj = new JSONObject();
        //JSONArray JArr = new JSONArray();
        try {
            HashMap<String, Object> rRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            String companyid = (String) requestParams.get("companyid");
            String currencyid = (String) requestParams.get("gcurrencyid");

            HashMap<String, Object> fieldrequestParams = new HashMap();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Make_Payment_ModuleId));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);


            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("df");
//            DecimalFormat f = new DecimalFormat("##.00");
            Iterator itr = list.iterator();
            Customer customer = null;
            while (itr.hasNext()) {


                /*
                 * If you are modifying in this method then you will need to
                 * modify on accReportsController.java - getIBGEntryJson()
                 * method AND on AccReportsServiceImpl.java getPaymentAmount()
                 * method
                 */

                JSONArray jArr1 = new JSONArray();
                Object[] row = (Object[]) itr.next();
                Payment payment = (Payment) row[0];
                Account acc = (Account) row[1];
                JSONObject obj = new JSONObject();
                obj.put("withoutinventory", false);
//                KwlReturnObject vendorresult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), acc.getID());
                if (payment.getCustomer() != null) {
                    KwlReturnObject custResult = accountingHandlerDAOobj.getObject(Customer.class.getName(), payment.getCustomer());
                    customer = (Customer) custResult.getEntityList().get(0);
                }

                Vendor vendor = payment.getVendor();
                if (vendor != null) {
                    obj.put("address", vendor.getAddress());
                    obj.put("personemail", vendor.getEmail());
                } else {
                    obj.put("address", "");
                    obj.put("personemail", "");
                }
//                Vendor vendor = (Vendor) session.get(Vendor.class, acc.getID());
//                if(vendor!=null)
//                   obj.put("address", vendor.getAddress());
//                        obj.put("address", vendor.getAddress());

                obj.put("billid", payment.getID());
                obj.put("companyid", payment.getCompany().getCompanyID());
                obj.put("companyname", payment.getCompany().getCompanyName());
                obj.put("entryno", payment.getJournalEntry().getEntryNumber() != null ? payment.getJournalEntry().getEntryNumber() : "");
                obj.put("journalentryid", payment.getJournalEntry().getID());
                obj.put("isadvancepayment", payment.isIsadvancepayment());
                obj.put("ismanydbcr", payment.isIsmanydbcr());
                obj.put("isprinted", payment.isPrinted());
                obj.put("bankCharges", payment.getBankChargesAmount());
                obj.put("bankChargesCmb", payment.getBankChargesAccount() != null ? payment.getBankChargesAccount().getID() : "");
                obj.put("bankInterest", payment.getBankInterestAmount());
                obj.put("bankInterestCmb", payment.getBankInterestAccount() != null ? payment.getBankInterestAccount().getID() : "");
                obj.put("paidToCmb", payment.getPaidTo() == null ? "" : payment.getPaidTo().getID());
                obj.put("paidto", payment.getPaidTo() != null ? payment.getPaidTo().getValue() : "");  //to show the paid to option in grid
                obj.put(Constants.SEQUENCEFORMATID, payment.getSeqformat() == null ? "" : payment.getSeqformat().getID());
                boolean advanceUsed = false;
                if (payment.getAdvanceid() != null && !payment.getAdvanceid().isDeleted()) {
                    rRequestParams.clear();
                    filter_names.clear();
                    filter_params.clear();
                    filter_names.add("payment.ID");
                    filter_params.add(payment.getAdvanceid().getID());
                    rRequestParams.put("filter_names", filter_names);
                    rRequestParams.put("filter_params", filter_params);
                    KwlReturnObject grdresult = accVendorPaymentobj.getPaymentDetails(rRequestParams);
                    advanceUsed = grdresult.getEntityList().size() > 0 ? true : false;
                }
                Payment paymentObject = null;
                if (payment.getInvoiceAdvCndnType() == 2 || payment.getInvoiceAdvCndnType() == 1) {
                    paymentObject = accVendorPaymentobj.getPaymentObject(payment);
                    if (paymentObject != null) {
                        obj.put("cndnid", paymentObject.getID());
                    }
                } else if (payment.getInvoiceAdvCndnType() == 3) {
                    obj.put("cndnid", payment.getID());
                }
                obj.put("invoiceadvcndntype", payment.getInvoiceAdvCndnType());
                obj.put("cndnAndInvoiceId", !StringUtil.isNullOrEmpty(payment.getCndnAndInvoiceId()) ? payment.getCndnAndInvoiceId() : "");
                obj.put("advanceUsed", advanceUsed);
                obj.put("advanceid", (payment.getAdvanceid() != null && !payment.getAdvanceid().isDeleted()) ? payment.getAdvanceid().getID() : "");
                obj.put("advanceamount", payment.getAdvanceamount());
                obj.put("advanceamounttype", payment.getAdvanceamounttype());
                obj.put("receipttype", payment.getReceipttype());
                obj.put("personid", (vendor != null) ? vendor.getID() : acc.getID());
//                obj.put("customervendorname", (vendor!=null)? vendor.getName() : (customer!=null)? customer.getName():"");
                obj.put("billno", payment.getPaymentNumber());
//                obj.put("billdate", df.format(payment.getJournalEntry().getEntryDate()));//receiptdate
                obj.put("billdate", df.format(payment.getCreationDate()));//receiptdate

                rRequestParams.clear();
                filter_names.clear();
                filter_params.clear();
                filter_names.add("payment.ID");
                filter_params.add(payment.getID());
                rRequestParams.put("filter_names", filter_names);
                rRequestParams.put("filter_params", filter_params);
                KwlReturnObject pdoresult = accVendorPaymentobj.getPaymentDetailOtherwise(rRequestParams);
                List<PaymentDetailOtherwise> list1 = pdoresult.getEntityList();
                Iterator pdoRow = list1.iterator();

                Iterator itrRow = payment.getRows().iterator();
                double amount = 0, totaltaxamount = 0;
                if (!payment.getRows().isEmpty()) {
                    while (itrRow.hasNext()) {
                        amount += ((PaymentDetail) itrRow.next()).getAmount();
                    }
                    obj.put("otherwise", false);
                } else if (pdoRow != null && list1.size() > 0) {
                    for (PaymentDetailOtherwise paymentDetailOtherwise : list1) {
                        if (payment.getID().equals(paymentDetailOtherwise.getPayment().getID())) {
                            double taxamount = 0;
                            if (paymentDetailOtherwise.getTax() != null) {
                                taxamount = paymentDetailOtherwise.getTaxamount();
                                totaltaxamount += taxamount;
                            }
                            JSONObject obj1 = new JSONObject();
                            obj1.put("debitaccid", paymentDetailOtherwise.getAccount().getID());
                            obj1.put("debitamt", paymentDetailOtherwise.getAmount());
                            obj1.put("isdebit", paymentDetailOtherwise.isIsdebit());
                            obj1.put("desc", paymentDetailOtherwise.getDescription() != null ? paymentDetailOtherwise.getDescription() : "");
                            obj1.put("prtaxid", paymentDetailOtherwise.getTax() != null ? paymentDetailOtherwise.getTax().getID() : "");
                            obj1.put("taxamount", taxamount);
                            obj1.put("curamount", (paymentDetailOtherwise.getAmount() + paymentDetailOtherwise.getTaxamount()));
                            if (payment.isIsmanydbcr()) {
                                if (paymentDetailOtherwise.isIsdebit()) {
                                    amount += Double.parseDouble(authHandler.formattedAmount(((paymentDetailOtherwise.getAmount() + paymentDetailOtherwise.getTaxamount())), companyid));
                                } else {
                                    amount -= Double.parseDouble(authHandler.formattedAmount(((paymentDetailOtherwise.getAmount() + paymentDetailOtherwise.getTaxamount())), companyid));
                                }
                            } else {
                                amount = amount + Double.parseDouble(authHandler.formattedAmount(((paymentDetailOtherwise.getAmount() + paymentDetailOtherwise.getTaxamount())), companyid));
                            }
                            // ## Get Custom Field Data 
                            Map<String, Object> variableMap = new HashMap<String, Object>();
                            HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                            ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                            Detailfilter_names.add(Constants.Acc_JEDetail_recdetailId);
                            Detailfilter_params.add(paymentDetailOtherwise.getID());
                            invDetailRequestParams.put("filter_names", Detailfilter_names);
                            invDetailRequestParams.put("filter_params", Detailfilter_params);
                            KwlReturnObject idcustresult = accVendorPaymentobj.getVendorPaymentCustomData(invDetailRequestParams);
                            if (idcustresult.getEntityList().size() > 0) {
                                AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
                                AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                                for (Entry<String, Object> varEntry : variableMap.entrySet()) {
                                    String coldata = varEntry.getValue() != null ? varEntry.getValue().toString() : "";
                                    if (customFieldMap.containsKey(varEntry.getKey())) {
                                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), coldata);
                                        FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                                        if (fieldComboData != null) {
                                            obj1.put(varEntry.getKey(), coldata != null ? coldata : "");//fieldComboData.getValue()!=null ?fieldComboData.getValue():"");
                                        }
                                    } else {
                                        if (!StringUtil.isNullOrEmpty(coldata)) {
                                            obj1.put(varEntry.getKey(), coldata);
                                        }
                                    }
                                }
                            }
                            jArr1.put(obj1);
                        }
                    }

                    obj.put("otherwise", true);
                    obj.put("detailsjarr", jArr1);
                } else {
                    itrRow = payment.getJournalEntry().getDetails().iterator();
                    while (itrRow.hasNext()) {
                        JournalEntryDetail jed = ((JournalEntryDetail) itrRow.next());
                        if (!jed.isDebit()) {
                            if (payment.getDeposittoJEDetail() != null) {
                                amount = payment.getDeposittoJEDetail().getAmount();
                            } else {
                                amount = jed.getAmount();
                            }
                        } else {
                            JSONObject obj1 = new JSONObject();
                            obj1.put("debitaccid", jed.getAccount().getID());
                            obj1.put("debitamt", jed.getAmount());
                            obj1.put("isdebit", jed.isDebit());
                            obj1.put("curamount", jed.getAmount());
                            obj1.put("desc", jed.getDescription() != null ? jed.getDescription() : "");
                            jArr1.put(obj1);
                        }
                    }
                    obj.put("otherwise", true);
                    obj.put("detailsjarr", jArr1);
                }
                obj.put("amount", authHandler.formattedAmount(amount, companyid));
                obj.put("totaltaxamount", authHandler.formattedAmount(totaltaxamount, companyid));


                String paycurrencyid = (payment.getCurrency() == null ? currency.getCurrencyID() : payment.getCurrency().getCurrencyID());
//                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amount, paycurrencyid, payment.getJournalEntry().getEntryDate(), payment.getJournalEntry().getExternalCurrencyRate());
                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amount, paycurrencyid, payment.getCreationDate(), payment.getJournalEntry().getExternalCurrencyRate());
                double amountinbase = (Double) bAmt.getEntityList().get(0);
                obj.put("amountinbase", authHandler.formattedAmount(amountinbase, companyid));


                KwlReturnObject result = accVendorPaymentobj.getPaymentVendorNames(companyid, payment.getID());
                List vNameList = result.getEntityList();
                Iterator vNamesItr = vNameList.iterator();
                String vendorNames = "";
                while (vNamesItr.hasNext()) {
                    String tempName = URLEncoder.encode((String) vNamesItr.next(), "UTF-8");
                    vendorNames += tempName;
                    vendorNames += ",";
                }
                vendorNames = vendorNames.substring(0, Math.max(0, vendorNames.length() - 1));
                obj.put("personname", (payment.getReceipttype() == 9 || payment.getReceipttype() == 2) ? vendorNames : ((vendor != null) ? vendor.getName() : (customer != null) ? customer.getName() : ""));
                obj.put("memo", payment.getMemo());
                obj.put("deleted", payment.isDeleted());
                obj.put("currencysymbol", (payment.getCurrency() == null ? currency.getSymbol() : payment.getCurrency().getSymbol()));
                obj.put("externalcurrencyrate", payment.getExternalCurrencyRate());
                obj.put("paymentmethod", (payment.getPayDetail() == null ? "" : payment.getPayDetail().getPaymentMethod().getMethodName()));
                obj.put("chequenumber", payment.getPayDetail() == null ? "" : (payment.getPayDetail().getPaymentMethod().getDetailType() == PaymentMethod.TYPE_BANK) ? (payment.getPayDetail().getCheque() != null ? payment.getPayDetail().getCheque().getChequeNo() : "") : "");
                obj.put("chequedescription", payment.getPayDetail() == null ? "" : (payment.getPayDetail().getPaymentMethod().getDetailType() == PaymentMethod.TYPE_BANK) ? (payment.getPayDetail().getCheque() != null ? (payment.getPayDetail().getCheque().getDescription() != null ? payment.getPayDetail().getCheque().getDescription() : "") : "") : "");
                obj.put("currencyid", (payment.getCurrency() == null ? currency.getCurrencyID() : payment.getCurrency().getCurrencyID()));
                obj.put("methodid", (payment.getPayDetail() == null ? "" : payment.getPayDetail().getPaymentMethod().getID()));
                obj.put("detailtype", (payment.getPayDetail() == null ? "" : payment.getPayDetail().getPaymentMethod().getDetailType()));
                if (payment.getPayDetail() != null) {
                    try {
                        obj.put("expirydate", (payment.getPayDetail().getCard() == null ? "" : df.format(payment.getPayDetail().getCard().getExpiryDate())));
                    } catch (IllegalArgumentException ae) {
                        obj.put("expirydate", "");
                    }
                    obj.put("refdetail", (payment.getPayDetail().getCard() == null ? (payment.getPayDetail().getCheque() == null ? "" : payment.getPayDetail().getCheque().getDescription()) : payment.getPayDetail().getCard().getCardType()));

//                if (payment.getPayDetail() != null) {
                    obj.put("refno", (payment.getPayDetail().getCard() == null ? (payment.getPayDetail().getCheque() == null ? "" : payment.getPayDetail().getCheque().getChequeNo()) : payment.getPayDetail().getCard().getRefNo()));
                    obj.put("refname", (payment.getPayDetail().getCard() == null ? (payment.getPayDetail().getCheque() == null ? "" : payment.getPayDetail().getCheque().getBankName()) : payment.getPayDetail().getCard().getCardHolder()));
                    if (payment.getPayDetail().getCard() != null) {
                        obj.put("refcardno", payment.getPayDetail().getCard().getCardNo());
//                        obj.put("refexpdate", payment.getPayDetail().getCard().getExpiryDate());
                    }
//                }
                }
                obj.put("clearanceDate", "");
                obj.put("paymentStatus", false);
                if (payment.getPayDetail() != null) {
                    KwlReturnObject clearanceDate = accBankReconciliationObj.getBRfromJE(payment.getJournalEntry().getID(), payment.getCompany().getCompanyID(), false);
                    if (clearanceDate != null && clearanceDate.getEntityList() != null && clearanceDate.getEntityList().size() > 0) {
                        BankReconciliationDetail brd = (BankReconciliationDetail) clearanceDate.getEntityList().get(0);
                        if (brd.getBankReconciliation().getClearanceDate() != null) {
                            obj.put("clearanceDate", df.format(brd.getBankReconciliation().getClearanceDate()));
                            obj.put("paymentStatus", true);
                        }
                    }
                }

                if (payment.isIBGTypeTransaction()) {
                    obj.put("isIBGTypeTransaction", payment.isIBGTypeTransaction());
                    obj.put("ibgDetailsID", payment.getIbgreceivingbankdetails().getId());
                    obj.put("ibgCode", payment.getIbgCode());
                }

                jsonlist.add(obj);
            }
            //jobj.put("data", JArr);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getPaymentsJson : " + ex.getMessage(), ex);
        }
        return jsonlist;
    }

    public List<JSONObject> getBillingPaymentsJson(HashMap<String, Object> requestParams, List list, List<JSONObject> jsonlist) throws ServiceException {
        try {
            HashMap<String, Object> rRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            String companyid = (String) requestParams.get("companyid");
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("df");
            Iterator itr = list.iterator();
            //JSONArray jArr=new JSONArray();
            while (itr.hasNext()) {
                JSONArray jArr1 = new JSONArray();
                Object[] row = (Object[]) itr.next();
                BillingPayment receipt = (BillingPayment) row[0];
                Account acc = (Account) row[1];
                JSONObject obj = new JSONObject();
                obj.put("withoutinventory", true);
                obj.put("billid", receipt.getID());
                obj.put("companyid", receipt.getCompany().getCompanyID());
                obj.put("companyname", receipt.getCompany().getCompanyName());
                obj.put("entryno", receipt.getJournalEntry().getEntryNumber());
                obj.put("journalentryid", receipt.getJournalEntry().getID());
                obj.put("personid", acc.getID());
                obj.put("billno", receipt.getBillingPaymentNumber());
                obj.put("ismanydbcr", receipt.isIsmanydbcr());
                obj.put("bankCharges", receipt.getBankChargesAmount());
                obj.put("bankChargesCmb", receipt.getBankChargesAccount() != null ? receipt.getBankChargesAccount().getID() : "");
                obj.put("bankInterest", receipt.getBankInterestAmount());
                obj.put("bankInterestCmb", receipt.getBankInterestAccount() != null ? receipt.getBankInterestAccount().getID() : "");
                obj.put("paidToCmb", receipt.getPaidTo() == null ? "" : receipt.getPaidTo().getID());
                obj.put("currencysymbol", (receipt.getCurrency() == null ? currency.getCurrencyID() : receipt.getCurrency().getSymbol()));
                obj.put("externalcurrencyrate", receipt.getExternalCurrencyRate());
                obj.put("billdate", df.format(receipt.getJournalEntry().getEntryDate()));//receiptdate
                Iterator itrRow = receipt.getRows().iterator();

                rRequestParams.clear();
                filter_names.clear();
                filter_params.clear();
                filter_names.add("billingPayment.ID");
                filter_params.add(receipt.getID());
                rRequestParams.put("filter_names", filter_names);
                rRequestParams.put("filter_params", filter_params);
                KwlReturnObject pdoresult = accVendorPaymentobj.getBillingPaymentDetailOtherwise(rRequestParams);
                List<BillingPaymentDetailOtherwise> list1 = pdoresult.getEntityList();
                Iterator pdoRow = list1.iterator();
                double amount = 0;
                if (!receipt.getRows().isEmpty()) {
                    while (itrRow.hasNext()) {
                        amount += ((BillingPaymentDetail) itrRow.next()).getAmount();
                    }
                    obj.put("otherwise", false);
                } else if (pdoRow != null && list1.size() > 0) {
                    for (BillingPaymentDetailOtherwise billingPaymentDetailOtherwise : list1) {
                        if (receipt.getID().equals(billingPaymentDetailOtherwise.getBillingPayment().getID())) {
                            JSONObject obj1 = new JSONObject();
                            obj1.put("debitaccid", billingPaymentDetailOtherwise.getAccount().getID());
                            obj1.put("debitamt", billingPaymentDetailOtherwise.getAmount());
                            obj1.put("isdebit", billingPaymentDetailOtherwise.isIsdebit());
                            obj1.put("desc", billingPaymentDetailOtherwise.getDescription() != null ? billingPaymentDetailOtherwise.getDescription() : "");
                            obj1.put("prtaxid", billingPaymentDetailOtherwise.getTax() != null ? billingPaymentDetailOtherwise.getTax().getID() : "");
                            obj1.put("curamount", (billingPaymentDetailOtherwise.getAmount() + billingPaymentDetailOtherwise.getTaxamount()));
                            if (receipt.isIsmanydbcr()) {
                                if (billingPaymentDetailOtherwise.isIsdebit()) {
                                    amount += (billingPaymentDetailOtherwise.getAmount() + billingPaymentDetailOtherwise.getTaxamount());
                                } else {
                                    amount -= (billingPaymentDetailOtherwise.getAmount() + billingPaymentDetailOtherwise.getTaxamount());
                                }
                            } else {
                                amount = amount + (billingPaymentDetailOtherwise.getAmount() + billingPaymentDetailOtherwise.getTaxamount());
                            }
                            jArr1.put(obj1);
                        }
                    }

                    obj.put("otherwise", true);
                    obj.put("detailsjarr", jArr1);
                } else {
                    itrRow = receipt.getJournalEntry().getDetails().iterator();
                    while (itrRow.hasNext()) {
                        JournalEntryDetail jed = ((JournalEntryDetail) itrRow.next());
                        if (!jed.isDebit()) {
                            if (receipt.getDeposittoJEDetail() != null) {
                                amount = receipt.getDeposittoJEDetail().getAmount();
                            } else {
                                amount = jed.getAmount();
                            }
                        } else {
                            JSONObject obj1 = new JSONObject();
                            obj1.put("debitaccid", jed.getAccount().getID());
                            obj1.put("debitamt", jed.getAmount());
                            obj1.put("isdebit", jed.isDebit());
                            obj1.put("curamount", jed.getAmount());
                            obj1.put("desc", jed.getDescription() != null ? jed.getDescription() : "");
                            jArr1.put(obj1);
                        }
                    }
                    obj.put("otherwise", true);
                    obj.put("detailsjarr", jArr1);
                }
                obj.put("receipttype", "");
                KwlReturnObject result = accVendorPaymentobj.getBillingPaymentVendorNames(companyid, receipt.getID());
                List vNameList = result.getEntityList();
                Iterator vNamesItr = vNameList.iterator();
                String vendorNames = "";
                while (vNamesItr.hasNext()) {
                    String tempName = URLEncoder.encode((String) vNamesItr.next(), "UTF-8");
                    vendorNames += tempName;
                    vendorNames += ",";
                }
                vendorNames = vendorNames.substring(0, Math.max(0, vendorNames.length() - 1));
                KwlReturnObject vendorresult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), acc.getID());
                Vendor vendor = (Vendor) vendorresult.getEntityList().get(0);
                obj.put("personemail", vendor != null ? vendor.getEmail() : "");
                obj.put("personname", vendorNames);
                obj.put("memo", receipt.getMemo());
                obj.put("deleted", receipt.isDeleted());
                obj.put("paymentmethod", (receipt.getPayDetail() == null ? "" : receipt.getPayDetail().getPaymentMethod().getMethodName()));
                obj.put("amount", authHandler.formattedAmount(amount, companyid));
                obj.put("currencysymbol", (receipt.getCurrency() == null ? currency.getSymbol() : receipt.getCurrency().getSymbol()));
                obj.put("currencyid", (receipt.getCurrency() == null ? currency.getCurrencyID() : receipt.getCurrency().getCurrencyID()));
                obj.put("methodid", (receipt.getPayDetail() == null ? "" : receipt.getPayDetail().getPaymentMethod().getID()));
                obj.put("detailtype", (receipt.getPayDetail() == null ? "" : receipt.getPayDetail().getPaymentMethod().getDetailType()));
                if (receipt.getPayDetail() != null) {
                    try {
                        obj.put("expirydate", (receipt.getPayDetail().getCard() == null ? "" : df.format(receipt.getPayDetail().getCard().getExpiryDate())));
                    } catch (IllegalArgumentException ae) {
                        obj.put("expirydate", "");
                    }
                    obj.put("refdetail", (receipt.getPayDetail().getCard() == null ? (receipt.getPayDetail().getCheque() == null ? "" : receipt.getPayDetail().getCheque().getDescription()) : receipt.getPayDetail().getCard().getCardType()));
                    obj.put("refno", (receipt.getPayDetail().getCard() == null ? (receipt.getPayDetail().getCheque() == null ? "" : receipt.getPayDetail().getCheque().getChequeNo()) : receipt.getPayDetail().getCard().getRefNo()));
                    obj.put("refname", (receipt.getPayDetail().getCard() == null ? (receipt.getPayDetail().getCheque() == null ? "" : receipt.getPayDetail().getCheque().getBankName()) : receipt.getPayDetail().getCard().getCardHolder()));
                    if (receipt.getPayDetail().getCard() != null) {
                        obj.put("refcardno", receipt.getPayDetail().getCard().getCardNo());
                        //obj.put("refexpdate", receipt.getPayDetail().getCard().getExpiryDate());
                    }
                }
                obj.put("clearanceDate", "");
                obj.put("paymentStatus", false);
                if (receipt.getPayDetail() != null) {
                    KwlReturnObject clearanceDate = accBankReconciliationObj.getBRfromJE(receipt.getJournalEntry().getID(), receipt.getCompany().getCompanyID(), false);
                    if (clearanceDate != null && clearanceDate.getEntityList() != null && clearanceDate.getEntityList().size() > 0) {
                        BankReconciliationDetail brd = (BankReconciliationDetail) clearanceDate.getEntityList().get(0);
                        if (brd.getBankReconciliation().getClearanceDate() != null) {
                            obj.put("clearanceDate", df.format(brd.getBankReconciliation().getClearanceDate()));
                            obj.put("paymentStatus", true);
                        }
                    }
                }
                jsonlist.add(obj);
            }
            //jobj.put("data", jArr);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getBillingPaymentsJson : " + ex.getMessage(), ex);
        }
        return jsonlist;
    }

    public ModelAndView getPaymentRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";

        try {
//            HashMap<String, Object> requestParams = new HashMap<String, Object>();
//            requestParams.put("gcurrencyid", AuthHandler.getCurrencyID(request));
//            requestParams.put("dateformat", AuthHandler.getDateFormatter(request));
//            requestParams.put("bills", request.getParameterValues("bills"));
//
//            JSONArray DataJArr = getPaymentRowsJson(requestParams);
//            jobj.put("data", DataJArr.length()>0?DataJArr:"");
            jobj = getPaymentRows(request, true);
            issuccess = true;
            msg = messageSource.getMessage("acc.common.rec", null, RequestContextUtils.getLocale(request));
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getVendorCnPayment(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        try {
            KwlReturnObject jobj1;
            String billid = request.getParameter("bills");
            String cnWithAdvanceInvoice = request.getParameter("cnwithadvanceinvoice");
            String noteType = request.getParameter("noteType");
            if (noteType.equals("Credit Note")) {
                jobj1 = accVendorPaymentobj.getVendorCnPayment(billid);
            } else {
                jobj1 = accVendorPaymentobj.getVendorDnPayment(billid);
            }
            List lst = jobj1.getEntityList();
            if (Boolean.parseBoolean(cnWithAdvanceInvoice) && lst.isEmpty()) {
                if (noteType.equals("Credit Note")) {
                    jobj1 = accVendorPaymentobj.getVendorCnPaymentWithAdvance(billid);
                } else {
                    jobj1 = accVendorPaymentobj.getVendorDnPaymentWithAdvance(billid);
                }
                lst = jobj1.getEntityList();
            }
            Iterator iter = lst.iterator();
            JSONArray jrr = new JSONArray();
            while (iter.hasNext()) {
                JSONObject jobj2 = new JSONObject();
                Object[] obj = (Object[]) iter.next();
                String tmp = obj[0].toString();
                String cnno = "";
                String noteid = "";
                String currencyid = "";
                String currencysymbol = "";
                double amountDue = 0.0;
                if (noteType.equals("Credit Note")) {
                    KwlReturnObject cnresult = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), obj[1].toString());
                    CreditNote cn = (CreditNote) cnresult.getEntityList().get(0);
                    noteid = cn.getID();
                    cnno = cn.getCreditNoteNumber();
                    amountDue = cn.getCnamountdue();
                    currencyid = cn.getCurrency().getCurrencyID();
                    currencysymbol = cn.getCurrency().getSymbol();
                    
                } else {
                    KwlReturnObject cnresult = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), obj[1].toString());
                    DebitNote cn = (DebitNote) cnresult.getEntityList().get(0);
                    cnno = cn.getDebitNoteNumber();
                    noteid = cn.getID();
                    amountDue = cn.getDnamountdue();
                    currencyid = cn.getCurrency().getCurrencyID();
                    currencysymbol = cn.getCurrency().getSymbol();
                }

                jobj2.put("select", true);
                jobj2.put("noteid", noteid);
                jobj2.put("noteno", cnno);
                jobj2.put("amount", obj[3].toString());
                jobj2.put("amountdue", obj[4].toString());
                jobj2.put("payment", obj[3].toString());
                jobj2.put("currencyid", currencyid);
                jobj2.put("currencysymbol", currencysymbol);
                jrr.put(jobj2);
            }
            jobj.put("data", jrr);
            jobj.put("count", lst.size());
            issuccess = true;
        } catch (Exception ex) {
            System.out.print(ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", "");
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getBillingPaymentRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            jobj = getPaymentRows(request, false);
            issuccess = true;
            msg = messageSource.getMessage("acc.common.rec", null, RequestContextUtils.getLocale(request));
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getBillingPaymentRowsJson(HashMap<String, Object> requestParams) throws ServiceException {
        JSONArray JArr = new JSONArray();
        try {
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            boolean isVendorPaymentEdit = (Boolean.parseBoolean((String) requestParams.get("isReceiptEdit")));
            String[] billingreceipt = (String[]) requestParams.get("bills");
            int i = 0;
            double taxPercent = 0;
            DateFormat df = (DateFormat) requestParams.get("dateformat");
            HashMap<String, Object> pRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("billingPayment.ID");
            order_by.add("srno");
            order_type.add("asc");
            pRequestParams.put("filter_names", filter_names);
            pRequestParams.put("filter_params", filter_params);
            pRequestParams.put("order_by", order_by);
            pRequestParams.put("order_type", order_type);


            while (billingreceipt != null && i < billingreceipt.length) {
//                BillingPayment re=(BillingPayment)session.get(BillingPayment.class, billingreceipt[i]);
                KwlReturnObject presult = accountingHandlerDAOobj.getObject(BillingPayment.class.getName(), billingreceipt[i]);
                BillingPayment re = (BillingPayment) presult.getEntityList().get(0);
//                Iterator itr=re.getRows().iterator();
                filter_params.clear();
                filter_params.add(re.getID());
                KwlReturnObject grdresult = accVendorPaymentobj.getBillingPaymentDetails(pRequestParams);
                Iterator itr = grdresult.getEntityList().iterator();


                while (itr.hasNext()) {
                    BillingPaymentDetail row = (BillingPaymentDetail) itr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", isVendorPaymentEdit ? row.getBillingGoodsReceipt().getID() : re.getID());
                    obj.put("srno", row.getSrno());
                    obj.put("rowid", row.getID());
                    obj.put("transectionno", row.getBillingGoodsReceipt().getBillingGoodsReceiptNumber());
                    obj.put("transectionid", (isVendorPaymentEdit ? row.getBillingGoodsReceipt().getVendorEntry().getAmount() : row.getBillingGoodsReceipt().getID()));
                    obj.put("amount", row.getAmount());
                    obj.put("amountpaid", row.getAmount());
                    obj.put("currencysymbol", (row.getBillingPayment().getCurrency() == null ? currency.getCurrencyID() : row.getBillingPayment().getCurrency().getSymbol()));
                    obj.put("duedate", df.format(row.getBillingGoodsReceipt().getDueDate()));
                    obj.put("creationdate", df.format(row.getBillingGoodsReceipt().getJournalEntry().getEntryDate()));
                    double totalamount = row.getBillingGoodsReceipt().getVendorEntry().getAmount();
                    obj.put("totalamount", totalamount);

                    KwlReturnObject amtrs = accGoodsReceiptobj.getAmtromBPD(row.getBillingGoodsReceipt().getID());
                    double ramount = amtrs.getEntityList().size() > 0 ? (Double) amtrs.getEntityList().get(0) : 0;
                    double amountdue = totalamount - ramount;

                    if (row.getBillingGoodsReceipt().getTax() != null) {
                        KwlReturnObject perresult = accTaxObj.getTaxPercent(row.getCompany().getCompanyID(), row.getBillingGoodsReceipt().getJournalEntry().getEntryDate(), row.getBillingGoodsReceipt().getTax().getID());
                        taxPercent = (Double) perresult.getEntityList().get(0);
                    }
                    obj.put("taxpercent", taxPercent);
                    obj.put("discount", row.getBillingGoodsReceipt().getDiscount() == null ? 0 : row.getBillingGoodsReceipt().getDiscount().getDiscountValue());
                    obj.put("payment", row.getBillingGoodsReceipt().getID());
                    obj.put("amountduenonnegative", (isVendorPaymentEdit ? amountdue + row.getAmount() : amountdue));
                    obj.put("totalamount", row.getBillingGoodsReceipt().getVendorEntry().getAmount());
                    JArr.put(obj);
                }
                i++;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentController.getBillingPaymentRowsJson : " + ex.getMessage(), ex);
        }
        return JArr;
    }

    public JSONObject getPaymentRows(HttpServletRequest request, boolean flag) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
            requestParams.put("dateformat", authHandler.getDateOnlyFormat(request));
            requestParams.put("bills", request.getParameterValues("bills"));
            requestParams.put("isReceiptEdit", request.getParameter("isReceiptEdit"));
            JSONArray DataJArr = new JSONArray();
//            if(flag){
//                DataJArr = getPaymentRowsJson(requestParams); xz
//            }else{
            DataJArr = getBillingPaymentRowsJson(requestParams);
//            }
            jobj.put("data", DataJArr.length() > 0 ? DataJArr : "");
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getPaymentRows : " + ex.getMessage(), ex);
//            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }

    /**
     * Deprecating below method as it is move to accVendorPaymentControllerCMN.java refer ERM-64.
     * @param request
     * @param response
     * @return
     * @deprecated
     */
    @Deprecated
    public ModelAndView exportPayment(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        List<JSONObject> list = new ArrayList<JSONObject>();
        List<JSONObject> tempList = new ArrayList<JSONObject>();
        int limitValue = 0, startValue = 0, dataCount = 0;
        String view = "jsonView_ex";
        try {
            HashMap<String, Object> requestParams = getPaymentMap(request);
            if (!StringUtil.isNullOrEmpty(request.getParameter("ss"))) {
                requestParams.put("ss", StringUtil.DecodeText(request.getParameter("ss")));
            }
            String start = (String) requestParams.get("start");
            String limit = (String) requestParams.get("limit");
            boolean contraentryflag = request.getParameter("contraentryflag") != null;
            boolean isAdvancePayment = request.getParameter("advancePayment") != null;
            boolean isAdvanceToCustomer = request.getParameter("advanceToCustomer") != null;
            boolean isPostDatedCheque = request.getParameter("isPostDatedCheque") != null;
            boolean isDishonouredCheque = request.getParameter("isDishonouredCheque") != null;
            boolean isGlcode = request.getParameter("isGlcode") != null;
            boolean exportPtw = request.getParameter("exportPtw") != null;
            boolean ispendingApproval = request.getParameter("ispendingAproval") != null?request.getParameter("ispendingAproval")!=""?true:false:false;
            
            String fileType = request.getParameter("filetype");
            String paymentWindowType = "";
            requestParams.put("contraentryflag", contraentryflag);
            requestParams.put("isadvancepayment", isAdvancePayment);
            requestParams.put("isadvancetocustomer", isAdvanceToCustomer);
            requestParams.put("isPostDatedCheque", isPostDatedCheque);
            requestParams.put("isDishonouredCheque", isDishonouredCheque);
            requestParams.put("isGlcode", isGlcode);
            requestParams.put("fileType", fileType);
            requestParams.put("isExport", true);
            requestParams.put("ispendingAproval",ispendingApproval);
            
            boolean onlyOpeningBalanceTransactionsFlag = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("onlyOpeningBalanceTransactionsFlag"))) {
                onlyOpeningBalanceTransactionsFlag = Boolean.parseBoolean(request.getParameter("onlyOpeningBalanceTransactionsFlag"));
            }
            
            boolean allAdvPayment = request.getParameter("allAdvPayment") != null;
            boolean unUtilizedAdvPayment = request.getParameter("unUtilizedAdvPayment") != null;
            boolean partiallyUtilizedAdvPayment = request.getParameter("partiallyUtilizedAdvPayment") != null;
            boolean fullyUtilizedAdvPayment = request.getParameter("fullyUtilizedAdvPayment") != null;
            boolean nonorpartiallyUtilizedAdvPayment = request.getParameter("nonorpartiallyUtilizedAdvPayment") != null;

            if (!StringUtil.isNullOrEmpty(request.getParameter("allAdvPayment"))) {
                allAdvPayment = Boolean.parseBoolean(request.getParameter("allAdvPayment"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("unUtilizedAdvPayment"))) {
                unUtilizedAdvPayment = Boolean.parseBoolean(request.getParameter("unUtilizedAdvPayment"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("partiallyUtilizedAdvPayment"))) {
                partiallyUtilizedAdvPayment = Boolean.parseBoolean(request.getParameter("partiallyUtilizedAdvPayment"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("fullyUtilizedAdvPayment"))) {
                fullyUtilizedAdvPayment = Boolean.parseBoolean(request.getParameter("fullyUtilizedAdvPayment"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("nonorpartiallyUtilizedAdvPayment"))) {
                nonorpartiallyUtilizedAdvPayment = Boolean.parseBoolean(request.getParameter("nonorpartiallyUtilizedAdvPayment"));
            }

            requestParams.put("allAdvPayment", allAdvPayment);
            requestParams.put("unUtilizedAdvPayment", unUtilizedAdvPayment);
            requestParams.put("partiallyUtilizedAdvPayment", partiallyUtilizedAdvPayment);
            requestParams.put("fullyUtilizedAdvPayment", fullyUtilizedAdvPayment);
            requestParams.put("nonorpartiallyUtilizedAdvPayment", nonorpartiallyUtilizedAdvPayment);
            
            
//            if (exportPtw) { // In export payment report, filter was not working
                if (!StringUtil.isNullOrEmpty(request.getParameter("paymentWindowType"))) {
                    requestParams.put("paymentWindowType", Integer.parseInt(request.getParameter("paymentWindowType")));
                }
//            }
            boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
            String[] companyids = (consolidateFlag && request.getParameter("companyids") != null) ? request.getParameter("companyids").split(",") : sessionHandlerImpl.getCompanyid(request).split(",");
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid") != null) ? request.getParameter("gcurrencyid") : sessionHandlerImpl.getCurrencyID(request);
            boolean eliminateflag = consolidateFlag;
//            if(consolidateFlag) {
            requestParams.put(Constants.start, "");
            requestParams.put(Constants.limit, "");
//            }
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
            KwlReturnObject billingResult = null;
            String companyid = "";
            for (int cnt = 0; cnt < companyids.length; cnt++) {
                companyid = companyids[cnt];
                request.setAttribute("companyid", companyid);
                request.setAttribute("gcurrencyid", gcurrencyid);
                request.setAttribute("isExport", true);
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);

                tempList.clear();
                if (onlyOpeningBalanceTransactionsFlag) {    //To export opening balance records (To Print Data in PDF, CSV format)
                    result = accVendorPaymentobj.getAllOpeningBalancePayments(requestParams);
                    JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
//                    tempList = accVendorPaymentModuleServiceObj.getOpeningBalanceReceiptJsonForReport(request, result.getEntityList(), tempList);
                    tempList = accVendorPaymentModuleServiceObj.getOpeningBalanceReceiptJsonForReport(paramJobj, result.getEntityList(), tempList);
                } else {
                    result = accVendorPaymentobj.getPayments(requestParams);
                    tempList = accVendorPaymentModuleServiceObj.getPaymentsJson(requestParams, result.getEntityList(), tempList);
                }
                list.addAll(tempList); 
            }
            limitValue = list.size();
            startValue = 0;
            Iterator iterator = list.iterator();
            for (int i = 0; i < list.size(); i++) {
                if (i >= startValue && dataCount < limitValue) {
                    JSONObject jSONObject = (JSONObject) iterator.next();
                    jArr.put(jSONObject);
                    dataCount++;
                }
                if (dataCount == limitValue) {
                    break;
                }
            }
            jobj.put("data", jArr);
            jobj.put("count", list.size());
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    private class VendorPaymentDateComparator implements Comparator<JSONObject> {

        @Override
        public int compare(JSONObject o1, JSONObject o2) {   //sort data on date
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
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
            return result;
        }
    }

    public ModelAndView linkPayment(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            List li = linkPayment(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.field.PaymentinformationhasbeenLinkedtoVendorInvoicesuccessfully", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

     public List linkPayment(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        List result = new ArrayList();
        Invoice invoice = null;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            String paymentid =request.getParameter("paymentid");
            String invoiceids[] = request.getParameter("invoiceids").split(",");
            String paymentno =request.getParameter("paymentno");
            String invoicenos =request.getParameter("invoicenos");
            double amount = 0;

            String amounts[] = request.getParameter("amounts").split(",");

            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

            KwlReturnObject receiptObj = accountingHandlerDAOobj.getObject(Payment.class.getName(), paymentid);
            Payment payment = (Payment) receiptObj.getEntityList().get(0);

            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            JournalEntry je = null;
            JournalEntryDetail updatejed = null;
            double eternalCurrencyRate = 0d;
            boolean isopeningBalancePayment = payment.isIsOpeningBalencePayment();
            String jeid = "";
            if (payment.isNormalPayment()) {
                je = payment.getJournalEntry();
                jeid = je.getID();
                eternalCurrencyRate = je.getExternalCurrencyRate();
            }
            if (!payment.isNormalPayment() && payment.isIsOpeningBalencePayment()) {
                eternalCurrencyRate = payment.getExchangeRateForOpeningTransaction();
                amount = payment.getDepositAmount();
            }
//Commented the code becoz now only advance payment can be linked and in that we dont need to delete the je and create it again
//            if (payment.isNormalPayment()) {
//                //Delete entry from optimized table
//                accJournalEntryobj.deleteOnEditAccountJEs_optimized(jeid);
//
//                Iterator itrRow = je.getDetails().iterator();
//                updatejed = new JournalEntryDetail();
//                while (itrRow.hasNext()) {
//                    JournalEntryDetail jed = ((JournalEntryDetail) itrRow.next());
//                    if (!jed.isDebit()) {
//                        amount = jed.getAmount();
//                        updatejed = jed;
//                    } else {
//                        accJournalEntryobj.deleteJournalEntryDetailRow(jed.getID(), companyid);
//                    }
//                }
//            }            
           JSONArray jArr = new JSONArray();
           String vendorId="";
           String accountId="";
           String linkedInvoiceids="";
           String linkedInvoicenos="";
           
           Iterator itrRow = payment.getRows().iterator();
           Map<String,Double> paymentRowsHashMap=new HashMap<String, Double>();
           double paymentAmount=0.0;
           if (!payment.getRows().isEmpty()) {//deleting linked data if the Payment is parctially linked
                    while (itrRow.hasNext()) {
                        PaymentDetail paymentDetail=((PaymentDetail) itrRow.next());
                        paymentRowsHashMap.put(paymentDetail.getGoodsReceipt().getID(), paymentDetail.getAmount());
                        paymentAmount+=paymentDetail.getAmount();
                    }
                    accVendorPaymentobj.deletePaymentsDetailsAndUpdateAmountDue(payment.getID(),payment.getCompany().getCompanyID(),payment.getApprovestatuslevel());
                    HashMap<String, Object> requestParams = new HashMap();
                    requestParams.put(Constants.companyid, company.getCompanyID());
                    requestParams.put("gcurrencyid", company.getCurrency().getCurrencyID());
                    double externalCurrencyRate = 1d;
                    boolean isopeningBalanceRP = payment.isIsOpeningBalencePayment();
                    Date rpCreationDate = null;
                    rpCreationDate = payment.getCreationDate();
                    if (!payment.isNormalPayment() && payment.isIsOpeningBalencePayment()) {
                        externalCurrencyRate = payment.getExchangeRateForOpeningTransaction();
                    } else {
//                        rpCreationDate = payment.getJournalEntry().getEntryDate();
                        externalCurrencyRate = payment.getJournalEntry().getExternalCurrencyRate();
                    }
                    String fromcurrencyid = payment.getCurrency().getCurrencyID();
                    KwlReturnObject bAmt = null;
                    if (isopeningBalanceRP && payment.isConversionRateFromCurrencyToBase()) {// if Receipt is opening balance Receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, paymentAmount, fromcurrencyid, rpCreationDate, externalCurrencyRate);
                    } else {
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, paymentAmount, fromcurrencyid, rpCreationDate, externalCurrencyRate);
                    }
                    double openingbalanceBaseAmountDue = (Double) bAmt.getEntityList().get(0);

                    payment.setOpeningBalanceAmountDue(payment.getOpeningBalanceAmountDue()+paymentAmount);
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
                KwlReturnObject grresult = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invoiceids[k]);
                GoodsReceipt gr = (GoodsReceipt) grresult.getEntityList().get(0);
                if(linkedInvoiceids.equals("")&&gr!=null){
                    linkedInvoiceids+=gr.getID();
                    linkedInvoicenos+=gr.getGoodsReceiptNumber();
                }else if(gr!=null){
                    linkedInvoiceids+=","+gr.getID();
                    linkedInvoicenos+=","+gr.getGoodsReceiptNumber();
                }
                vendorId=gr.getVendor().getID();
                accountId=gr.getVendor().getAccount().getID();
                
                JSONObject jobj = new JSONObject();
                
                if(paymentRowsHashMap.containsKey(invoiceids[k])){
                    double actualAmount=paymentRowsHashMap.get(invoiceids[k]);
                    jobj.put("payment", Double.parseDouble(amounts[k])+actualAmount);
                    paymentRowsHashMap.remove(invoiceids[k]);
                }else{
                    jobj.put("payment", amounts[k]);
                }
                jobj.put("billid", invoiceids[k]);
                jobj.put("isopeningBalancePayment", isopeningBalancePayment);
                jobj.put("isConversionRateFromCurrencyToBase", payment.isConversionRateFromCurrencyToBase());
                jArr.put(jobj);
            }
            
            for (Entry<String, Double> entry : paymentRowsHashMap.entrySet()) {//creating json for saving linked data
                JSONObject jobj = new JSONObject();
                String key = entry.getKey();
                Double value = entry.getValue();
                jobj.put("payment", value);
                jobj.put("billid", key);
                jobj.put("isopeningBalancePayment", isopeningBalancePayment);
                jobj.put("isConversionRateFromCurrencyToBase", payment.isConversionRateFromCurrencyToBase());
                jArr.put(jobj);
            }
            
            
            HashMap<String, Object> paymenthm = new HashMap<String, Object>();
            HashSet payDetails = savePaymentRows(payment, company, jArr, false, invoice);
            paymenthm.put("vendorId", vendorId);
            paymenthm.put("paymentid", payment.getID());
            paymenthm.put("pdetails", payDetails);
            accVendorPaymentobj.savePayment(paymenthm);

            //Commented the code becoz now only advance payment can be linked and in that we dont need to delete the je and create it again
//            JournalEntryDetail updatejed1 = null;
//            if (payment.isNormalPayment()) {
//                JSONObject jedjson = new JSONObject();
//                jedjson.put("companyid", companyid);
//                jedjson.put("srno", 2);
//                jedjson.put("amount", amount);
//                jedjson.put("accountid", accountId);
//                jedjson.put("debit", true);
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
//                double amountDiff = oldPaymentRowsAmount(request, jArr, payment.getCurrency().getCurrencyID(), eternalCurrencyRate);
//                if (preferences.getForeignexchange() == null) {
//                    throw new AccountingException(messageSource.getMessage("acc.pay.forex", null, RequestContextUtils.getLocale(request)));
//                }
//                if (amountDiff != 0 && preferences.getForeignexchange() != null && payment.isNormalPayment()) {
//                    if (amountDiff < 0) {
//                        rateDecreased = true;
//                    }
//                    JSONObject jParam = new JSONObject();
//                    jParam.put("srno", 3);
//                    jParam.put("companyid", companyid);
//                    jParam.put("amount", rateDecreased ? (-1 * amountDiff) : amountDiff);
//                    jParam.put("accountid", preferences.getForeignexchange().getID());
//                    jParam.put("debit", rateDecreased ? true : false);
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
//            if (payment.isNormalPayment()) {
//                //Insert new entries again in optimized table.
//                accJournalEntryobj.saveAccountJEs_optimized(jeid);
//            }
            auditTrailObj.insertAuditLog(AuditAction.LINKEDPAYMENT, "User " + sessionHandlerImpl.getUserFullName(request) + " has linked Payment "+paymentno+" to "+linkedInvoicenos, request,linkedInvoiceids);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ModelAndView linkBillingPayment(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            List li = linkBillingPayment(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.field.PaymentinformationhasbeenLinkedtoVendorInvoicesuccessfully", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
//            status = txnManager.getTransaction(def);
//            txnManager.commit(status);
//            status = txnManager.getTransaction(def);
//            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public List linkBillingPayment(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        List result = new ArrayList();
        BillingInvoice binvoice = null;//Set for contra entry
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            String paymentid = request.getParameter("paymentid");
            String invoiceid = request.getParameter("invoiceid");
            double amount = 0;

            KwlReturnObject grresult = accountingHandlerDAOobj.getObject(BillingGoodsReceipt.class.getName(), invoiceid);
            BillingGoodsReceipt gr = (BillingGoodsReceipt) grresult.getEntityList().get(0);

            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

            KwlReturnObject receiptObj = accountingHandlerDAOobj.getObject(BillingPayment.class.getName(), paymentid);
            BillingPayment payment = (BillingPayment) receiptObj.getEntityList().get(0);

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
                if (!jed.isDebit()) {
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
            HashSet payDetails = saveBillingPaymentRows(payment, company, jArr, false, binvoice);
            paymenthm.put("billingPaymentid", payment.getID());
            paymenthm.put("bpdetails", payDetails);
            accVendorPaymentobj.saveBillingPayment(paymenthm);

            JSONObject jedjson = new JSONObject();
            jedjson.put("companyid", companyid);
            jedjson.put("srno", 2);
            jedjson.put("amount", amount);
            jedjson.put("accountid", gr.getVendor().getID());
            jedjson.put("debit", true);
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
                double amountDiff = oldBillingPaymentRowsAmount(request, jArr, payment.getCurrency().getCurrencyID(), je.getExternalCurrencyRate());
                if (preferences.getForeignexchange() == null) {
                    throw new AccountingException(messageSource.getMessage("acc.pay.forex", null, RequestContextUtils.getLocale(request)));
                }
                if (amountDiff != 0 && preferences.getForeignexchange() != null) {
                    if (amountDiff < 0) {
                        rateDecreased = true;
                    }
                    JSONObject jParam = new JSONObject();
                    jParam.put("srno", 3);
                    jParam.put("companyid", companyid);
                    jParam.put("amount", rateDecreased ? (-1 * amountDiff) : amountDiff);
                    jParam.put("accountid", preferences.getForeignexchange().getID());
                    jParam.put("debit", rateDecreased ? true : false);
                    jParam.put("jeid", jeid);
                    accJournalEntryobj.addJournalEntryDetails(jParam);

                    jParam = new JSONObject();
                    jParam.put("jedid", updatejed1.getID());
                    jParam.put("amount", rateDecreased ? (updatejed.getAmount() + amountDiff) : updatejed1.getAmount() + amountDiff);
                    accJournalEntryobj.updateJournalEntryDetails(jParam);
                }
            }

            //Insert new entries again in optimized table.
            accJournalEntryobj.saveAccountJEs_optimized(jeid);

        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ModelAndView deletePaymentMerged(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("VP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            deletePaymentMerged(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.pay.del", null, RequestContextUtils.getLocale(request));  //"Payment(s) has been deleted successfully";
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void deletePaymentMerged(HttpServletRequest request) throws AccountingException, SessionExpiredException, ServiceException {
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String paymentid = "", jeid = "", paymentno = "";
            KwlReturnObject result, result1;
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                paymentid = StringUtil.DecodeText(jobj.optString("billid"));
                jeid = StringUtil.DecodeText(jobj.optString("journalentryid"));
                paymentno = jobj.getString("billno");
                boolean withoutinventory = Boolean.parseBoolean(jobj.getString("withoutinventory"));
                if (withoutinventory) {
                    //    query = "update BillingPayment set deleted=true where ID in("+qMarks +") and company.companyID=?";
                    result = accVendorPaymentobj.deleteBillingPaymentEntry(paymentid, companyid);

                    //query = "update JournalEntry je set je.deleted=true where je.ID in(select p.journalEntry.ID from BillingPayment p where p.ID in("+qMarks +") and p.company.companyID=je.company.companyID) and je.company.companyID=?";
                    result = accVendorPaymentobj.getJEFromBillingPayment(paymentid, companyid);
                    List list = result.getEntityList();
                    Iterator itr = list.iterator();
                    while (itr.hasNext()) {
                        String jeId = ((BillingPayment) itr.next()).getJournalEntry().getID();
                        result = accJournalEntryobj.deleteJournalEntry(jeId, companyid);
                        //Delete entry from optimized table
                        accJournalEntryobj.deleteAccountJEs_optimized(jeId);
                    }
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(BillingPayment.class.getName(), paymentid);
                    BillingPayment billingPayment = (BillingPayment) objItr.getEntityList().get(0);
                    if (billingPayment != null && billingPayment.getRevalJeId() != null) {
                        result = accJournalEntryobj.deleteJournalEntry(billingPayment.getRevalJeId(), companyid);
                    }
                } else {
                    /*
                     * //Delete Payment Details result =
                     * accVendorPaymentobj.deletePaymentsDetails(paymentid,
                     * companyid); //Delete Payment result =
                     * accVendorPaymentobj.deletePayments(paymentid, companyid);
                     *
                     * jeid = jobj.getString("journalentryid"); //Delete Journal
                     * Entry and Details result =
                     * accJournalEntryobj.deleteJEDtails(jeid, companyid);
                     * //Delete Journal Entry Details result =
                     * accJournalEntryobj.deleteJE(jeid, companyid);
                     */

//                query = "update Payment set deleted=true where ID in("+qMarks +") and company.companyID=?";
//                HibernateUtil.executeUpdate(session, query, params.toArray());
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Payment.class.getName(), paymentid);
                    Payment payment = (Payment) objItr.getEntityList().get(0);
                    if (payment != null) {
                        updateOpeningBalance(payment, companyid);
                    }
                    result = accVendorPaymentobj.deletePaymentEntry(paymentid, companyid);

//                query = "update JournalEntry je set je.deleted=true where je.ID in(select p.journalEntry.ID from Payment p where p.ID in("+qMarks +") and p.company.companyID=je.company.companyID) and je.company.companyID=?";
//                HibernateUtil.executeUpdate(session, query, params.toArray());
                    result = accVendorPaymentobj.getJEFromPayment(paymentid);
                    List list = result.getEntityList();
                    Iterator itr = list.iterator();
                    while (itr.hasNext()) {
                        String jeId = (String) itr.next();
                        result = accJournalEntryobj.deleteJournalEntry(jeId, companyid);
                        //Delete entry from optimized table
                        accJournalEntryobj.deleteAccountJEs_optimized(jeId);
                    }
                    if (payment != null && payment.getRevalJeId() != null) {
                        result = accJournalEntryobj.deleteJournalEntry(payment.getRevalJeId(), companyid);
                    }

                    if (payment != null && payment.getPayDetail() != null && payment.getPayDetail().getCheque() != null) {
                        accPaymentDAOobj.deleteCheque(payment.getPayDetail().getCheque().getID(), companyid);
                    }

                }
                result1 = accVendorPaymentobj.getpaymenthistory(paymentid);
                List ls = result1.getEntityList();
                Iterator<Object[]> itr1 = ls.iterator();
                while (itr1.hasNext()) {
                    Object[] row = (Object[]) itr1.next();
                    String cnid = row[0].toString();
                    Double amount = Double.parseDouble(row[1].toString());
                    KwlReturnObject cnidresult = accVendorPaymentobj.updateCnUpAmount(cnid, amount);
                }
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("oldjeid", jeid);
                requestParams.put("companyId", companyid);
                deleteBankReconcilation(requestParams);
            }
            auditTrailObj.insertAuditLog(AuditAction.MAKE_PAYMENT, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted a Payment " + paymentno, request, paymentid);
        } /*catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        }*/ catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)));
        }
    }

    public ModelAndView deletePaymentPermanent(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("VP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            deletePaymentPermanent(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.pay.del", null, RequestContextUtils.getLocale(request));  //"Payment(s) has been deleted successfully";
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + (ex.getMessage() != null ? ex.getMessage() : ex.getCause().getMessage());
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void deletePaymentPermanent(HttpServletRequest request) throws AccountingException, SessionExpiredException, ServiceException, ParseException {
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            JSONArray jArr = new JSONArray();
            if (request.getParameter("data") != null && !request.getParameter("data").equals("")) {
                jArr = new JSONArray(request.getParameter("data"));
            }
            String paymentid = "", paymentno = "";
            KwlReturnObject result, result1;
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                paymentid = StringUtil.DecodeText(jobj.optString("billid"));
                paymentno = jobj.getString("billno");
                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Payment.class.getName(), paymentid);
                Payment payment = (Payment) objItr.getEntityList().get(0);
                if (payment != null && payment.getRevalJeId() != null) {
                    result = accJournalEntryobj.deleteJEDtails(payment.getRevalJeId(), companyid);// 2 For realised JE
                    result = accJournalEntryobj.deleteJE(payment.getRevalJeId(), companyid);
                }
                if (payment != null) {
                    updateOpeningBalance(payment, companyid);
                }
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("paymentid", paymentid);
                requestParams.put("companyid", companyid);
                requestParams.put("paymentno", paymentno);
                if (!StringUtil.isNullOrEmpty(paymentid)) {
                    DateFormat dateFormatForLock = authHandler.getDateOnlyFormat(request);
                    Date entryDateForLock = null;
                    if (jobj.has("billdate")) {
                        entryDateForLock = dateFormatForLock.parse(jobj.getString("billdate"));
                    }
                    if (entryDateForLock != null) {
                        requestParams.put("entrydate", entryDateForLock);
                        requestParams.put("df", dateFormatForLock);
                    }
                    accVendorPaymentobj.deletePaymentPermanent(requestParams);

                    auditTrailObj.insertAuditLog(AuditAction.MAKE_PAYMENT, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted a payment pemananetly " + paymentno, request, paymentid);
                }
            }

        } /*catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        } */catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)));
        }
    }

    public void deletePaymentPermanent(HttpServletRequest request, String paymentId) throws AccountingException, SessionExpiredException, ServiceException, ParseException {
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String paymentid = "", paymentno = "";
            KwlReturnObject result, result1;
            paymentid = paymentId;
            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Payment.class.getName(), paymentid);
            Payment payment = (Payment) objItr.getEntityList().get(0);
            if (payment != null && payment.getRevalJeId() != null) {
                result = accJournalEntryobj.deleteJEDtails(payment.getRevalJeId(), companyid);// 2 For realised JE
                result = accJournalEntryobj.deleteJE(payment.getRevalJeId(), companyid);
            }
            if (payment != null) {
                updateOpeningBalance(payment, companyid);
            }
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("paymentid", paymentid);
            requestParams.put("companyid", companyid);
            if (!StringUtil.isNullOrEmpty(paymentid)) {
                DateFormat dateFormatForLock = authHandler.getDateOnlyFormat(request);
                Date entryDateForLock = null;
                if (payment != null && payment.getCreationDate() != null) {
//                     entryDateForLock = dateFormatForLock.parse(jobj.getString("billdate"));
                    entryDateForLock = payment.getCreationDate();
                }
                if (entryDateForLock != null) {
                    requestParams.put("entrydate", entryDateForLock);
                    requestParams.put("df", dateFormatForLock);
                }
                accVendorPaymentobj.deletePaymentPermanent(requestParams);
                auditTrailObj.insertAuditLog(AuditAction.MAKE_PAYMENT, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted a payment pemananetly " + paymentno, request, paymentid);
            }

        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)));
        }
    }

    /*
     * Update invoice due amount when payment is being deleted.
     */
    public void updateOpeningBalance(Payment payment, String companyId) throws JSONException, ServiceException {
        if (payment != null) {
            Set<PaymentDetail> paymentDetailSet = payment.getRows();
            if (paymentDetailSet != null && !payment.isDeleted()) { // if payment already temporary deleted then amountdue already updated. No need to update amountdue again for permament delete.
                Iterator itr = paymentDetailSet.iterator();
                while (itr.hasNext()) {
                    PaymentDetail row = (PaymentDetail) itr.next();
                    double discountAmtInInvoiceCurrency = authHandler.round(row.getDiscountAmount() / row.getExchangeRateForTransaction(), companyId);
                    double discountAmount = row.getDiscountAmount();
                    if (!row.getGoodsReceipt().isNormalInvoice() && row.getGoodsReceipt().isIsOpeningBalenceInvoice()) {
                        double amountPaid = row.getAmount();
                        GoodsReceipt goodsReceipt = row.getGoodsReceipt();
                        HashMap<String, Object> requestParams = new HashMap();
                        requestParams.put(Constants.companyid, payment.getCompany().getCompanyID());
                        requestParams.put("gcurrencyid", payment.getCompany().getCurrency().getCurrencyID());
                        double externalCurrencyRate = 1d;
                        boolean isopeningBalanceRP = payment.isIsOpeningBalencePayment();
                        Date rpCreationDate = null;
                        rpCreationDate = payment.getCreationDate();
                        if (!payment.isNormalPayment() && payment.isIsOpeningBalencePayment()) {
                            externalCurrencyRate = payment.getExchangeRateForOpeningTransaction();
                        } else {
//                            rpCreationDate = payment.getJournalEntry().getEntryDate();
                            externalCurrencyRate = payment.getJournalEntry().getExternalCurrencyRate();
                        }
                        String fromcurrencyid = payment.getCurrency().getCurrencyID();
                        KwlReturnObject bAmt = null;
                        KwlReturnObject bDiscountAmt = null;
                        if (isopeningBalanceRP && payment.isConversionRateFromCurrencyToBase()) {// if Payment is opening balance Receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountPaid, fromcurrencyid, rpCreationDate, externalCurrencyRate);
                            bDiscountAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, discountAmount, fromcurrencyid, rpCreationDate, externalCurrencyRate);
                        } else {
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountPaid, fromcurrencyid, rpCreationDate, externalCurrencyRate);
                            bDiscountAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, discountAmount, fromcurrencyid, rpCreationDate, externalCurrencyRate);
                        }
                        discountAmount = (Double) bDiscountAmt.getEntityList().get(0);
                        
                        double invoiceAmountDue = goodsReceipt.getOpeningBalanceAmountDue()+discountAmount;
                        double openingbalanceBaseAmountDue = goodsReceipt.getOpeningBalanceBaseAmountDue() + (Double) bAmt.getEntityList().get(0)+discountAmount;
                        if (row.getExchangeRateForTransaction() != 0) {
                            invoiceAmountDue += amountPaid / row.getExchangeRateForTransaction();
                        } else {
                            invoiceAmountDue += amountPaid;
                        }
                        Map<String, Object> greceipthm = new HashMap<String, Object>();
                        greceipthm.put("grid", goodsReceipt.getID());;
                        greceipthm.put("companyid", companyId);
                        greceipthm.put("openingBalanceAmountDue", invoiceAmountDue);
                        greceipthm.put(Constants.openingBalanceBaseAmountDue, openingbalanceBaseAmountDue);
                        accGoodsReceiptobj.updateGoodsReceipt(greceipthm);
                    } else if(row.getGoodsReceipt().isNormalInvoice()){
                        double amountPaid = row.getAmount();
                        GoodsReceipt goodsReceipt = row.getGoodsReceipt();
                        double invoiceAmountDue = goodsReceipt.getInvoiceamountdue();
                        if (row.getExchangeRateForTransaction() != 0) {
                            invoiceAmountDue += amountPaid / row.getExchangeRateForTransaction();
                        } else {
                            invoiceAmountDue += amountPaid;
                        }
                        KwlReturnObject bAmt = null;
                        HashMap<String, Object> requestParams = new HashMap();
                        requestParams.put(Constants.companyid, companyId);
                        requestParams.put(Constants.globalCurrencyKey, goodsReceipt.getCompany().getCurrency().getCurrencyID());
                        double externalCurrencyRate = 0d;
                        externalCurrencyRate = goodsReceipt.getJournalEntry() != null ? goodsReceipt.getJournalEntry().getExternalCurrencyRate() : 1;
                        String fromcurrencyid = goodsReceipt.getCurrency().getCurrencyID();
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountPaid, fromcurrencyid, goodsReceipt.getCreationDate(), externalCurrencyRate);
                        double totalBaseAmountDue = authHandler.round((Double) bAmt.getEntityList().get(0),companyId);
                        double invoiceAmountDueInBase = goodsReceipt.getInvoiceAmountDueInBase();
                        invoiceAmountDueInBase += totalBaseAmountDue;
                        Map<String, Object> greceipthm = new HashMap<String, Object>();
                        greceipthm.put("grid", goodsReceipt.getID());;
                        greceipthm.put("companyid", companyId);
                        greceipthm.put(Constants.invoiceamountdue, invoiceAmountDue);
                        greceipthm.put(Constants.invoiceamountdueinbase, invoiceAmountDueInBase);
                        accGoodsReceiptobj.updateGoodsReceipt(greceipthm);
                }
            }
        }
    }
    }
    
    /**
     * Added @Transactional instead of txnmanager - ERP-32983. Moved code
     * containing business logic to the AccVendorPaymentModuleServiceImpl No any
     * changes other than this has been done in code.
     * @param request
     * @param response
     * @return
     */
    public ModelAndView deleteOpeningPaymentPermanent(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            String paymentid[] = request.getParameterValues("billidArray");
            String paymentno[] = request.getParameterValues("invoicenoArray");
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj=accVendorPaymentModuleServiceObj.deleteOpeningPaymentPermanent(paramJobj,paymentid,paymentno);
            issuccess = jobj.optBoolean(Constants.RES_success, false);
            msg = jobj.optString(Constants.RES_msg);
        } catch (SessionExpiredException ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try{
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch(JSONException ex){
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
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
                hm.put("paymentid", SOIDList.get(cnt));
                hm.put("isprinted", request.getParameter("isprinted"));
                if (!StringUtil.isNullOrEmpty(SOIDList.get(cnt))) {
                    result = accVendorPaymentobj.savePayment(hm);
                }
            }
            msg = messageSource.getMessage("acc.field.setsuccessfully", null, RequestContextUtils.getLocale(request));
            issuccess = true;
            txnManager.commit(status);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView printCheck(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobjDetails = new JSONObject();
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        String msg = "";
        JSONObject chequeobj=new JSONObject();
        boolean issuccess = false;
        try {
            
            String  paymentid=request.getParameter("paymentid");
            String paymentno=request.getParameter("paymentno");
            String chequeno =request.getParameter("chequeno");
            KwlReturnObject result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), request.getParameter("paymentMethod"));
            PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);

            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            if (!StringUtil.isNullOrEmpty(payMethod.getID())) {
                requestParams.put("bankid", payMethod.getID());
            }
            String companyid=payMethod.getCompany().getCompanyID();
            
            int countryLanguageId = Constants.OtherCountryLanguageId; // 0
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
            if (extraCompanyPreferences.isAmountInIndianWord()) {
                countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
            }
            
            KwlReturnObject result1 = accPaymentDAOobj.getChequeLayout(requestParams);
            List list = result1.getEntityList();
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                ChequeLayout chequeLayout = (ChequeLayout) itr.next();
                chequeobj = new JSONObject(chequeLayout.getCoordinateinfo());
                jobjDetails.put("dateLeft", chequeobj.optString("dateLeft","0"));
                jobjDetails.put("nameLeft", chequeobj.optString("nameLeft","0"));
                jobjDetails.put("amtinwordLeft", chequeobj.optString("amtinwordLeft","0"));
                jobjDetails.put("amtinwordLeftLine2", chequeobj.optString("amtinwordLeftLine2","0"));
                jobjDetails.put("amtLeft", chequeobj.optString("amtLeft","0"));
                jobjDetails.put("dateTop", chequeobj.optString("dateTop","0"));
                jobjDetails.put("nameTop", chequeobj.optString("nameTop","0"));
                jobjDetails.put("amtinwordTop", chequeobj.optString("amtinwordTop","0"));
                jobjDetails.put("amtinwordTopLine2", chequeobj.optString("amtinwordTopLine2","0"));
                jobjDetails.put("amtTop", chequeobj.optString("amtTop","0"));
            }
            SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("ddMMyy");
            Date creationDate = new Date(request.getParameter("Printdate"));
            String date = DATE_FORMAT.format(creationDate);
            String formatted_date_with_spaces = "";
            for (int i = 0; i < date.length(); i++) {
                formatted_date_with_spaces += date.charAt(i);
                formatted_date_with_spaces += "&nbsp&nbsp&nbsp&nbsp&nbsp";

            }
            String netinword = "";
            DecimalFormat df=new DecimalFormat("#,###,###,##0.00");
  
            String amount2=request.getParameter("amount");
            String basecurrency=request.getParameter("currencyid");
            KwlReturnObject result2 = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrency);
            KWLCurrency currency = (KWLCurrency) result2.getEntityList().get(0);
            netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(amount2)), currency,countryLanguageId);
            String[] amount = {"amount", df.format(Double.parseDouble(amount2))};
            String[] amount1 = {"amountinword", netinword};
            String s = StringUtil.DecodeText(request.getParameter("name"));
            String[] accName = {"accountName", s};
            jobjDetails.put(amount[0], amount[1]);
            String amount_first_line = "";
            String amount_second_line = "";
            String action=" Only.";
                if (amount1[1].length() > 34 && amount1[1].charAt(34) == ' ') {
                    amount_first_line = amount1[1].substring(0, 34);
                    amount_second_line = amount1[1].substring(34, amount1[1].length());
                    jobjDetails.put(amount1[0], amount_first_line );
                    jobjDetails.put("amountinword1", amount_second_line+action);
                } else if (amount1[1].length() > 34) {
                    amount_first_line = amount1[1].substring(0, 34);
                    amount_first_line = amount1[1].substring(0, amount_first_line.lastIndexOf(" "));
                    amount_second_line = amount1[1].substring(amount_first_line.length(), amount1[1].length());
                    jobjDetails.put(amount1[0], amount_first_line );
                    jobjDetails.put("amountinword1", amount_second_line+action);
                } else {
                    if(amount1[1].length()<27){
                      jobjDetails.put(amount1[0], amount1[1]+action); 
                      jobjDetails.put("amountinword1", "");
                    }
                    else{
                        jobjDetails.put(amount1[0], amount1[1]); 
                        jobjDetails.put("amountinword1", action);   
                    }
                    
                }
            jobjDetails.put(accName[0], accName[1]);
            jobjDetails.put("date", formatted_date_with_spaces);
            
             
            boolean isFontStylePresent = chequeobj.has("fontStyle") && !StringUtil.isNullOrEmpty(chequeobj.getString("fontStyle")) ? true : false;
            String fontStyle = chequeobj.has("fontStyle") && !StringUtil.isNullOrEmpty(chequeobj.getString("fontStyle")) ? chequeobj.getString("fontStyle") : "";
            char fontStyleChar;
            if (fontStyle.equals("1")) {
                fontStyleChar = 'b';
            } else if (fontStyle.equals("2")) {
                fontStyleChar = 'i';
            } else {
                fontStyleChar = 'p';
            }
            
            
            //for name
            if (chequeobj.has("dateFontSize") || isFontStylePresent) {
                if (isFontStylePresent && !StringUtil.isNullOrEmpty(chequeobj.getString("dateFontSize"))) {
                    formatted_date_with_spaces = "<font size=" + chequeobj.getString("dateFontSize") + "><" + fontStyleChar + ">" + formatted_date_with_spaces + "</" + fontStyleChar + "></font> ";
                    jobjDetails.put("date", formatted_date_with_spaces);
                } else if (!StringUtil.isNullOrEmpty(chequeobj.getString("dateFontSize"))) {
                   formatted_date_with_spaces = "<font size=" + chequeobj.getString("dateFontSize") + ">" + formatted_date_with_spaces + "</font> ";
                    jobjDetails.put("date", formatted_date_with_spaces);
                } else {
                    formatted_date_with_spaces = "<" + fontStyleChar + ">" + formatted_date_with_spaces + "</" + fontStyleChar + ">";
                    jobjDetails.put("date", formatted_date_with_spaces);

                }
            }
            //for name
            if (chequeobj.has("nameFontSize") || isFontStylePresent) {
                if (isFontStylePresent && !StringUtil.isNullOrEmpty(chequeobj.getString("nameFontSize"))) {
                    accName[1] = "<font size=" + chequeobj.getString("nameFontSize") + "><" + fontStyleChar + ">" + accName[1] + "</" + fontStyleChar + "></font> ";
                    jobjDetails.put(accName[0], accName[1]);
                } else if (!StringUtil.isNullOrEmpty(chequeobj.getString("nameFontSize"))) {
                    accName[1] = "<font size=" + chequeobj.getString("nameFontSize") + ">" + accName[1] + "</font> ";
                    jobjDetails.put(accName[0], accName[1]);
                } else {
                    accName[1] = "<" + fontStyleChar + ">" + accName[1] + "</" + fontStyleChar + ">";
                    jobjDetails.put(accName[0], accName[1]);

                }
            }
            
            //for amount in words
            if (chequeobj.has("amountInWordsFontSize") || isFontStylePresent) {
                if (isFontStylePresent && !StringUtil.isNullOrEmpty(chequeobj.getString("amountInWordsFontSize"))) {
                    amount_first_line = "<font size=" + chequeobj.getString("amountInWordsFontSize") + "><" + fontStyleChar + ">" + amount_first_line + "</" + fontStyleChar + "></font> ";
                    amount_second_line = "<font size=" + chequeobj.getString("amountInWordsFontSize") + "><" + fontStyleChar + ">" + amount_second_line +" "+action+ "</" + fontStyleChar + "></font> ";
                     if (amount1[1].length() > 34) {
                        jobjDetails.put(amount1[0], amount_first_line);
                        jobjDetails.put("amountinword1", amount_second_line);
                    } else if (amount1[1].length() < 27) {
                        amount1[1] = "<font size=" + chequeobj.getString("amountInWordsFontSize") + "><" + fontStyleChar + ">" + amount1[1] +" "+action+ "</" + fontStyleChar + "></font> ";
                        jobjDetails.put(amount1[0], amount1[1]);
                        jobjDetails.put("amountinword1", "");
                    }
                } else if (!StringUtil.isNullOrEmpty(chequeobj.getString("amountInWordsFontSize"))) {
                    amount_first_line = "<font size=" + chequeobj.getString("amountInWordsFontSize") + ">" + amount_first_line + "</font> ";
                    amount_second_line = "<font size=" + chequeobj.getString("amountInWordsFontSize") + ">" + amount_second_line +" "+action+ "</font> ";
                    if (amount1[1].length() > 34) {
                        jobjDetails.put(amount1[0], amount_first_line);
                        jobjDetails.put("amountinword1", amount_second_line);
                    } else if (amount1[1].length() < 27) {
                        amount1[1] = "<font size=" + chequeobj.getString("amountInWordsFontSize") + ">" + amount1[1] +" "+action+ "</font> ";
                        jobjDetails.put(amount1[0], amount1[1]);
                        jobjDetails.put("amountinword1", "");
                    }
                } else {
                    amount_first_line = "<" + fontStyleChar + ">" + amount_first_line + "</" + fontStyleChar + ">";
                    amount_second_line = "<" + fontStyleChar + ">" + amount_second_line +" "+action+ "</" + fontStyleChar + ">";
                     if (amount1[1].length() > 34) {
                        jobjDetails.put(amount1[0], amount_first_line);
                        jobjDetails.put("amountinword1", amount_second_line);
                    } else if (amount1[1].length() < 27) {
                         amount1[1] = "<" + fontStyleChar + ">" + amount1[1] +" "+action+ "</" + fontStyleChar + ">";
                        jobjDetails.put(amount1[0], amount1[1]);
                        jobjDetails.put("amountinword1", "");
                    }
                }
            }
            
            //for amount in number
            if (chequeobj.has("amountFontSize") || isFontStylePresent) {
                if (isFontStylePresent && !StringUtil.isNullOrEmpty(chequeobj.getString("amountFontSize"))) {
                    amount[1] = "<font size=" + chequeobj.getString("amountFontSize") + "><" + fontStyleChar + ">" + amount[1] + "</" + fontStyleChar + "></font> ";
                    jobjDetails.put(amount[0], amount[1]);
                } else if (!StringUtil.isNullOrEmpty(chequeobj.getString("amountFontSize"))) {
                    amount[1] = "<font size=" + chequeobj.getString("amountFontSize") + ">" + amount[1] + "</font> ";
                    jobjDetails.put(amount[0], amount[1]);
                } else {
                    amount[1] = "<" + fontStyleChar + ">" + amount[1] + "</" + fontStyleChar + ">";
                    jobjDetails.put(amount[0], amount[1]);

                }
            }
            
            jArr.put(jobjDetails);
           KwlReturnObject result3 = accPaymentDAOobj.updateChequePrint(paymentid,companyid);
            issuccess = true;
            auditTrailObj.insertAuditLog(AuditAction.MAKE_PAYMENT, "User " + sessionHandlerImpl.getUserFullName(request) + " has printed a cheque "+chequeno+" for "+accName[1]+" in payment " + paymentno, request, paymentid);
            msg = messageSource.getMessage("acc.pay.del", null, RequestContextUtils.getLocale(request));  //"Payment(s) has been deleted successfully";
        } catch (Exception ex) {

            msg = "" + ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);

                jobj.put("data", jArr);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;

    }
    
    /**
     * Description : Below Method is used to Save New TDS Rate.
     * @param request
     * @param response
     * @throws com.krawler.common.session.SessionExpiredException
     */
    public ModelAndView AddTDSRate(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            AddTDSRate(request);
            issuccess = true;
            txnManager.commit(status);
        } catch(AccountingException e){
            issuccess = false;
            msg = e.getMessage();
            txnManager.rollback(status);
        }catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

    /**
     * Description : Below Method is used to Save New TDS Rate.
     * @param request
     * @return void
     * @throws ServiceException
     * @throws com.krawler.common.session.SessionExpiredException
     * @throws com.krawler.hql.accounting.AccountingException
     */
    public void AddTDSRate(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException, JSONException, UnsupportedEncodingException {
        LineLevelTerms invTerm = null;
        try {
            List<HashMap<String, Object>> storeDate = new ArrayList<HashMap<String, Object>>();
            String companyid = sessionHandlerImpl.getCompanyid(request), addnatureofpayment = "";
            HashMap<String, Object> TDSRateMap = new HashMap();
            TDSRateMap.put("companyid", companyid);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            TDSRateMap.put("creationdate", new Date());
            if (!StringUtil.isNullOrEmpty(request.getParameter("adddeducteetype"))) {
                String multideducteetypes[] = request.getParameter("adddeducteetype").split(",");
                for (String deducteeType : multideducteetypes) {
                    if (!StringUtil.isNullOrEmpty(request.getParameter("addnatureofpayment"))) {
                        addnatureofpayment = request.getParameter("addnatureofpayment");
                        KwlReturnObject masterItemResult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), addnatureofpayment);
                        MasterItem masterItem = (MasterItem) masterItemResult.getEntityList().get(0);
                        TDSRateMap.put("addnatureofpayment", masterItem != null ? masterItem.getDefaultMasterItem().getID() : "");
                    }
                    if (!StringUtil.isNullOrEmpty(request.getParameter("addresidentialstatus"))) {
                        int residentStatus = Integer.parseInt(request.getParameter("addresidentialstatus"));
                        if (IndiaComplianceConstants.ResidentialStatus_Resident == residentStatus) {
                            TDSRateMap.put("addresidentialstatus", "0");//Resident
                        } else {
                            TDSRateMap.put("addresidentialstatus", "1");//Non-Resident
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(request.getParameter("addbasicexemptionpertransaction"))) {
                        TDSRateMap.put("addbasicexemptionpertransaction", Double.parseDouble(request.getParameter("addbasicexemptionpertransaction")));
                    }
                    if (!StringUtil.isNullOrEmpty(request.getParameter("addbasicexemptionperannum"))) {
                        TDSRateMap.put("addbasicexemptionperannum", Double.parseDouble(request.getParameter("addbasicexemptionperannum")));
                    }
                    if (!StringUtil.isNullOrEmpty(deducteeType)) {
                        TDSRateMap.put("adddeducteetype", deducteeType);
                    }
                    if (!StringUtil.isNullOrEmpty(request.getParameter("addtdsrate"))) {
                        TDSRateMap.put("addtdsrate", request.getParameter("addtdsrate"));
                    }
                    if (!StringUtil.isNullOrEmpty(request.getParameter("addtdsfromdate"))) {
                        TDSRateMap.put("addtdsfromdate", df.parse(request.getParameter("addtdsfromdate")));
                    }
                    if (!StringUtil.isNullOrEmpty(request.getParameter("addtdstodate"))) {
                        TDSRateMap.put("addtdstodate", df.parse(request.getParameter("addtdstodate")));
                    }
                    if (!StringUtil.isNullOrEmpty(request.getParameter("addtdsrateifpannotavailable"))) {
                        TDSRateMap.put("addtdsrateifpannotavailable", Double.parseDouble(request.getParameter("addtdsrateifpannotavailable")));
                    }
                    if (!StringUtil.isNullOrEmpty(request.getParameter("id"))) {
                        TDSRateMap.put("id", Integer.parseInt(request.getParameter("id")));
                    }
                    if (!StringUtil.isNullOrEmpty(request.getParameter("id"))) {
                        HashMap<String, String> hmData = new HashMap<String, String>();
                        hmData.put("tdsID", TDSRateMap.get("id").toString());
                        hmData.put("companyid", companyid);
                        //Function To check whether selected TDS Master Rate Record(s) is used in Transaction(Advance Payment).
                        KwlReturnObject Advancecount = accVendorPaymentobj.ISTDSMasterRatesUsedInAdvancePayment(hmData);
                        if (Advancecount.getRecordTotalCount() > 0) {
                            throw new AccountingException("Selected record(s) is currently used in the transaction(s). So it cannot be change.");
                        } else {
                            //Function To check whether selected TDS Master Rate Record(s) is used in Transaction(Purchase Invoice).
                            KwlReturnObject PIcount = accVendorPaymentobj.ISTDSMasterRatesUsedInPI(hmData);
                            if (PIcount.getRecordTotalCount() > 0) {
                                throw new AccountingException("Selected record(s) is currently used in the transaction(s). So it cannot be change.");
                            }
                        }
                    }
                    //First Check whether Duplicate record exist or not.
                    List<TDSRate> list = accCommonTablesDAO.CheckDuplicateTDSMasterRate(TDSRateMap);
                    if (list == null || list.size() <= 0) {
                        accCommonTablesDAO.AddTDSRate(TDSRateMap);
                    } else {
                        throw new AccountingException("Rates For Given Dates Are Already Exist In Grid.");
                    }
                }
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(accVendorPaymentController.class.getName() + ".AddTDSRate : " + ex.getMessage(), ex);
        } catch (ParseException ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}