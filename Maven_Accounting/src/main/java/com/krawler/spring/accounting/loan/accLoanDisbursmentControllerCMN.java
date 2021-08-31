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
package com.krawler.spring.accounting.loan;

import com.krawler.hql.accounting.InvoiceDocuments;
import com.krawler.hql.accounting.InvoiceDocumentCompMap;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.esp.web.resource.Links;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.invoice.InvoiceConstants;
import com.krawler.spring.accounting.invoice.accInvoiceControllerCMN;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.receipt.accReceiptDAO;
import com.krawler.spring.accounting.salesorder.accSalesOrderDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnMsg;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
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
 * @author Pandurang
 */
public class accLoanDisbursmentControllerCMN extends MultiActionController implements MessageSourceAware {

    private MessageSource messageSource;
    private accCustomerDAO accCustomerDAOobj;
    private HibernateTransactionManager txnManager;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private accLoanDAO accLoanDAOobj;
    private exportMPXDAOImpl exportDaoObj;
    private accReceiptDAO accReceiptDAOobj;
    private accJournalEntryDAO accJournalEntryobj;
    private auditTrailDAO auditTrailObj;
    private accCurrencyDAO accCurrencyDAOobj;
    private String successView;
    private accInvoiceDAO accInvoiceDAOObj;
    private accProductDAO accProductObj;
    private accSalesOrderDAO accSalesOrderDAOobj;

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    @Override
    public void setMessageSource(MessageSource msg) {
        this.messageSource = msg;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setaccCustomerDAO(accCustomerDAO accCustomerDAOobj) {
        this.accCustomerDAOobj = accCustomerDAOobj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    
    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }
    
    public void setaccLoanDAO(accLoanDAO accLoanDAOobj) {
        this.accLoanDAOobj = accLoanDAOobj;
    }
    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj){
    	this.exportDaoObj = exportDaoObj;
    }
    public void setaccReceiptDAO(accReceiptDAO accReceiptDAOobj) {
        this.accReceiptDAOobj = accReceiptDAOobj;
    }
    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public accCurrencyDAO getAccCurrencyDAOobj() {
        return accCurrencyDAOobj;
    }

    public void setAccCurrencyDAOobj(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }

    public void setAccInvoiceDAOObj(accInvoiceDAO accInvoiceDAOObj) {
        this.accInvoiceDAOObj = accInvoiceDAOObj;
    }

    public void setAccProductObj(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    
    public void setaccSalesOrderDAO(accSalesOrderDAO accSalesOrderDAOobj) {
        this.accSalesOrderDAOobj = accSalesOrderDAOobj;
    }

    public ModelAndView saveLoanDisbursementRule(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        int isDuplicateflag = -1;
        boolean isUpdate = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("RIC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String id = request.getParameter("id");
            String ruleType = request.getParameter("ruleType");
            String minIncome = request.getParameter("minIncome");
            String maxIncome = request.getParameter("maxIncome");
            String eligibility = request.getParameter("eligibility");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String userid = sessionHandlerImpl.getUserid(request);

//            isDuplicateflag = accSalesOrderDAOobj.checkConsignmentApprovalRules(id, requestorid, warehouseid, locations, approverids, companyid);
//            if (isDuplicateflag == 1 || isDuplicateflag == -1) {
            KwlReturnObject coresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) coresult.getEntityList().get(0);
            coresult = accountingHandlerDAOobj.getObject(User.class.getName(), userid);
            User creator = (User) coresult.getEntityList().get(0);

            long createdon = System.currentTimeMillis();
            long updatedon = System.currentTimeMillis();

            LoanRules loanRules = new LoanRules();
            if (!StringUtil.isNullOrEmpty(id)) {
                KwlReturnObject result1 = accountingHandlerDAOobj.getObject(LoanRules.class.getName(), id);
                loanRules = (LoanRules) result1.getEntityList().get(0);
                isUpdate=true;
            }
            if (ruleType.equalsIgnoreCase("0")) {
                loanRules.setLoanRuleType(LoanRuleType.ABSOLUTEVALUE);
            } else if (ruleType.equalsIgnoreCase("1")) {
                loanRules.setLoanRuleType(LoanRuleType.MULTIPLEOFSALARY);
            } else if (ruleType.equalsIgnoreCase("2")) {
                loanRules.setLoanRuleType(LoanRuleType.UNLIMITED);
            }

            loanRules.setCompany(company);
            loanRules.setCreatedon(createdon);
            loanRules.setUpdatedon(updatedon);
            loanRules.setCreatedby(creator);
            loanRules.setModifiedby(creator);
            if (!StringUtil.isNullOrEmpty(minIncome)) {
                loanRules.setMinIncome(Double.parseDouble(minIncome));
            } else {
                loanRules.setMinIncome(0);
            }
            if (!StringUtil.isNullOrEmpty(maxIncome)) {
                loanRules.setMaxIncome(Double.parseDouble(maxIncome));
            } else {
                loanRules.setMaxIncome(0);
            }
            if (!StringUtil.isNullOrEmpty(eligibility)) {
                loanRules.setEligibility(Double.parseDouble(eligibility));
            }


            accCustomerDAOobj.saveLoanDisburementRule(loanRules);

            String auditID = "";
            String auditMsg = "";
            
            if (isUpdate) {
                auditID = AuditAction.UPDATE_MANAGE_ELIGIBILITY_RULES;
                auditMsg = "User " + sessionHandlerImpl.getUserFullName(request)+" "+ messageSource.getMessage("acc.loan.ManageEligibilityRules.update.audit", null, RequestContextUtils.getLocale(request));
                msg = messageSource.getMessage("acc.field.acc.field.Eligibilityhasbeenupdatedsuccessfully", null, RequestContextUtils.getLocale(request));
            } else {
                auditID = AuditAction.ADD_MANAGE_ELIGIBILITY_RULES;
                auditMsg = "User " + sessionHandlerImpl.getUserFullName(request) +" "+ messageSource.getMessage("acc.loan.ManageEligibilityRules.add.audit", null, RequestContextUtils.getLocale(request));
                msg = messageSource.getMessage("acc.field.acc.field.Eligibilityhasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request));
            }
            auditTrailObj.insertAuditLog(auditID, auditMsg, request, id);
//            } else if (isDuplicateflag == 0) {
//                msg = messageSource.getMessage("acc.field.ConsignmentRequestApprovalDuplicateMsg", null, RequestContextUtils.getLocale(request));
//            } else if (isDuplicateflag == 3) {
//                msg = messageSource.getMessage("acc.field.acc.field.ConsignmentRuleWarningMsg", null, RequestContextUtils.getLocale(request));
//            }
            issuccess = true;
            txnManager.commit(status);


        } catch (SessionExpiredException | ServiceException | NumberFormatException | NoSuchMessageException | TransactionException ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accLoanDisbursmentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("isDuplicateflag", isDuplicateflag);
            } catch (JSONException ex) {
                Logger.getLogger(accLoanDisbursmentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getLoanRules(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = null;
        try {
            HashMap<String, Object> requestParams = new HashMap<>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            filter_names.add("company.companyID");
            filter_params.add(companyid);
            requestParams.put(Constants.filterNamesKey, filter_names);
            requestParams.put(Constants.filterParamsKey, filter_params);

            KwlReturnObject result = accCustomerDAOobj.getLoanRules(requestParams);
            List<LoanRules> list = result.getEntityList();
            int count = result.getRecordTotalCount();

            JSONArray jSONArray = new JSONArray();
            for (LoanRules loanRules : list) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("id", loanRules.getID());
                jSONObject.put("minannualincome", loanRules.getMinIncome());
                jSONObject.put("maxannualincome", loanRules.getMaxIncome());
                if (loanRules.getLoanRuleType() == LoanRuleType.ABSOLUTEVALUE) {
                    jSONObject.put("ruletype", "0");
                    jSONObject.put("ruletypename", "Absolute Value");
                    jSONObject.put("eligibility", loanRules.getEligibility());
                } else if (loanRules.getLoanRuleType() == LoanRuleType.MULTIPLEOFSALARY) {
                    jSONObject.put("ruletype", "1");
                    jSONObject.put("ruletypename", "Multiple of Salary");
                     jSONObject.put("eligibility", loanRules.getEligibility() + " X Salary");
                } else if (loanRules.getLoanRuleType() == LoanRuleType.UNLIMITED) {
                    jSONObject.put("ruletype", "2");
                    jSONObject.put("ruletypename", "Unlimited");
                     jSONObject.put("eligibility", "Unlimited");
                }            
                jSONArray.put(jSONObject);
            }
            jobj.put(Constants.RES_data, jSONArray);
            jobj.put(Constants.RES_count, count);
            issuccess = true;
        } catch (SessionExpiredException | ServiceException | JSONException ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(accLoanDisbursmentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

    public ModelAndView deleteLoanDisbursementRule(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        KwlReturnObject result = null;
        boolean issuccess = false;
        String msg = "";
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String id = request.getParameter("id");
            String auditMsg="";
            result = accCustomerDAOobj.deleteLoanRule(companyid, id);
            String auditID = AuditAction.DELETE_MANAGE_ELIGIBILITY_RULES;
            issuccess = result.isSuccessFlag();
            //msg = "Loan rule deleted successfully.";
            msg = messageSource.getMessage("acc.field.acc.field.Eligibilityhasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
            
            auditMsg = "User " + sessionHandlerImpl.getUserFullName(request)+" " +messageSource.getMessage("acc.loan.ManageEligibilityRules.delete.audit", null, RequestContextUtils.getLocale(request));
            auditTrailObj.insertAuditLog(auditID, auditMsg, request, id);

        } catch (SessionExpiredException | ServiceException ex) {
            Logger.getLogger(accLoanDisbursmentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(accLoanDisbursmentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

    public ModelAndView saveDisbursement(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        String billid = "";
        String billno = "";
        String jeID = "";
        String JENumBer = "";
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("RIC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            /*
             * check Loan Ref no is already exist or not
             */
             String disbursementid = request.getParameter("disbursementid");
             String entryNumber = request.getParameter("number");
             String sequenceformat = request.getParameter("sequenceformat");
             String companyid = sessionHandlerImpl.getCompanyid(request);
             synchronized (this) {
                KwlReturnObject socnt = accLoanDAOobj.getDisbursementCount(entryNumber, companyid);
                if (socnt.getRecordTotalCount() > 0) {
                    if (StringUtil.isNullOrEmpty(disbursementid)) {
                        if (sequenceformat.equals("NA")) {
                            throw new AccountingException(messageSource.getMessage("acc.accPref.autoloanrefnumber", null, RequestContextUtils.getLocale(request)) + " " + entryNumber + " " + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                        }
                        }
                }
             }
            
            
            List li = saveDisbursement(request);
            billid = (String) li.get(0);
            billno = (String) li.get(1);
            jeID = (String) li.get(2);
            JENumBer = (String) li.get(3);
            jobj.put("billid", billid);
            jobj.put("billno", billno);
//            if(!isEdit) {
//                auditTrailObj.insertAuditLog("77", " User " + sessionHandlerImpl.getUserFullName(request) + " has added a new"+(isNormalContract ? "":" Lease")+" contract Details " + salesOrderNumber, request, salesOrderId);
//            } else {
//                auditTrailObj.insertAuditLog("77", " User " + sessionHandlerImpl.getUserFullName(request) + " has updated a "+(isNormalContract ? "":" Lease")+" contract Details " + salesOrderNumber, request, salesOrderId);
//            }
            String auditMsg="";
            String auditID="";
            if (!StringUtil.isNullOrEmpty(disbursementid)) {
                auditID = AuditAction.UPDATE_DISBURSEMENT;
                auditMsg = "User " + sessionHandlerImpl.getUserFullName(request) +" "+  messageSource.getMessage("acc.loan.disbursement.update.audit", null, RequestContextUtils.getLocale(request));
            } else {
                auditID = AuditAction.ADD_DISBURSEMENT;
                auditMsg = "User " + sessionHandlerImpl.getUserFullName(request) +" "+messageSource.getMessage("acc.loan.disbursement.add.audit", null, RequestContextUtils.getLocale(request));
            }
            auditTrailObj.insertAuditLog(auditID, auditMsg, request, billid);
            
            msg =messageSource.getMessage("acc.loan.DisbursementsuccessfullygeneratedNumber", null, RequestContextUtils.getLocale(request));
            msg += "<b>" + billno + ",</b> " + messageSource.getMessage("acc.field.JENo", null, RequestContextUtils.getLocale(request)) + ": <b>" + JENumBer + "</b>";
            
            txnManager.commit(status);
            issuccess = true;

        } catch (NumberFormatException | TransactionException| AccountingException | JSONException ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accLoanDisbursmentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accLoanDisbursmentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public List saveDisbursement(HttpServletRequest request) {
        Disbursement disbursement = null;
        List newList = new ArrayList();
        String jeentryNumber = "";
        String jeSeqFormatId = "";
        String jeIntegerPart = "";
        String jeDatePrefix = "";
        String jeDateAfterPrefix = "";
        String jeDateSuffix = "";
        String oldjeid = null;
        String jeid = null;
        try {

            String sequenceformat = request.getParameter("sequenceformat");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String createdby = sessionHandlerImpl.getUserid(request);
            String modifiedby = sessionHandlerImpl.getUserid(request);

            long createdon = System.currentTimeMillis();
            long updatedon = createdon;
            String entryNumber = request.getParameter("number");
            String disbursementid = request.getParameter("disbursementid");
            String nextAutoNumber = "";
            boolean jeautogenflag = false;
            boolean isEdit = request.getParameter("isEdit") != null ? StringUtil.getBoolean(request.getParameter("isEdit")) : false;
            double externalCurrencyRate = StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            String savedFilesMappingId = request.getParameter("savedFilesMappingId");
            DateFormat df = authHandler.getDateOnlyFormat(request);
            Date entryDate = df.parse(request.getParameter("applicationdate"));
            KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company)companyResult.getEntityList().get(0);
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) cap.getEntityList().get(0);
        
            if (StringUtil.isNullOrEmpty(extraCompanyPreferences.getLoanAccount()) == true) {
                throw new AccountingException(messageSource.getMessage("acc.field.contractNumber", null, RequestContextUtils.getLocale(request)) + " " + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
            }
            String accountid = extraCompanyPreferences.getLoanAccount();
            Map<String, Object> dataMap = new HashMap<>();
            String currencyid = (request.getParameter("currencyid") == null ? sessionHandlerImpl.getCurrencyID(request) : request.getParameter("currencyid"));
            synchronized (this) {
                KwlReturnObject socnt = accLoanDAOobj.getDisbursementCount(entryNumber, companyid);
                if (socnt.getRecordTotalCount() > 0) {
                    if (StringUtil.isNullOrEmpty(disbursementid)) {
                        if (sequenceformat.equals("NA")) {
                            throw new AccountingException(messageSource.getMessage("acc.field.contractNumber", null, RequestContextUtils.getLocale(request)) + " " + entryNumber + " " + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                        }
                    } else {
                        nextAutoNumber = entryNumber;
                        dataMap.put("disbursementid", disbursementid);
                        KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Disbursement.class.getName(), disbursementid);
                        disbursement = (Disbursement) returnObject.getEntityList().get(0);
                        
                        if (disbursement.getJournalEntry() != null) {
                            JournalEntry jetemp = disbursement.getJournalEntry();
                            oldjeid = disbursement.getJournalEntry().getID();
                            if (jetemp != null) {
                                jeentryNumber = jetemp.getEntryNumber(); //preserving these data to generate same JE number in edit case                    
                                jeautogenflag = jetemp.isAutoGenerated();
                                jeSeqFormatId = jetemp.getSeqformat() == null ? "" : jetemp.getSeqformat().getID();
                                jeIntegerPart = String.valueOf(jetemp.getSeqnumber());
                                jeDatePrefix = jetemp.getDatePreffixValue();
                                jeDateAfterPrefix = jetemp.getDateAfterPreffixValue();
                                jeDateSuffix = jetemp.getDateSuffixValue();
                            }

                            //Delete old entries and insert new entries again from optimized table in edit case.
//                            accJournalEntryobj.deleteOnEditAccountJEs_optimized(oldjeid);
                            disbursement.setCustomerEntry(null);
                            disbursement.setJournalEntry(null);
                            
                        }
//                        result = accSalesOrderDAOobj.getInvoiceAndDeliveryOrderOfContract(soid);
//                        List list1 = result.getEntityList();
//                        
//                        list.addAll(list1);
//                        
//                        
//                        if (!list.isEmpty()) {
//                            throw new AccountingException("Selected record is currently used So it cannot be edited.");
//                        }
                        accLoanDAOobj.deleteRepaymetDetails(disbursementid, companyid);
//                        accSalesOrderDAOobj.deletecontractFiles(soid);
                    }
                } else {
                    boolean seqformat_oldflag = StringUtil.getBoolean(request.getParameter("seqformat_oldflag"));
                    String nextAutoNoInt = "";
                    String datePrefix = "";
                    String dateAfterPrefix = "";
                    String dateSuffix = "";
                    if (!sequenceformat.equals("NA")) {
                        if (seqformat_oldflag) {
                            nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_Loan_Management, sequenceformat);
                        } else {
                            Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_Loan_Management, sequenceformat, seqformat_oldflag, entryDate);
                            nextAutoNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                            nextAutoNoInt = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                            datePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                            dateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);//Date After Prefix Part
                            dateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                            
                            dataMap.put(Constants.SEQFORMAT, sequenceformat);
                            dataMap.put(Constants.SEQNUMBER, nextAutoNoInt);
                            dataMap.put(Constants.DATEPREFIX, datePrefix);
                            dataMap.put(Constants.DATEAFTERPREFIX, dateAfterPrefix);
                            dataMap.put(Constants.DATESUFFIX, dateSuffix);
                        }
                        entryNumber = nextAutoNumber;
                    }
                }
                if (sequenceformat.equals("NA")) {//In case of NA checks wheather this number can also be generated by a sequence format or not
                    List list = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Loan_Management_ModuleId, entryNumber, companyid);
                    if (!list.isEmpty()) {
                        boolean isvalidEntryNumber = (Boolean) list.get(0);
                        String formatName = (String) list.get(1);
                        if (!isvalidEntryNumber) {
                            throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, RequestContextUtils.getLocale(request)) + " <b>" + entryNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, RequestContextUtils.getLocale(request)) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null, RequestContextUtils.getLocale(request)) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null, RequestContextUtils.getLocale(request)));
                        }
                    }
                }
            }


            double loanAmount = StringUtil.isNullOrEmpty(request.getParameter("loanamount")) ? 0.0 : Double.parseDouble(request.getParameter("loanamount"));
            double jeAmount=loanAmount;
            String loanTermType = request.getParameter("loanTermType");
            int termValue = StringUtil.isNullOrEmpty(request.getParameter("termValue")) ? 0 : Integer.parseInt(request.getParameter("termValue"));
            int installmentinterval = StringUtil.isNullOrEmpty(request.getParameter("installmentinterval")) ? 0 : Integer.parseInt(request.getParameter("installmentinterval"));
            int loanRuleTypeValue = StringUtil.isNullOrEmpty(request.getParameter("loanRuleTypeValue")) ? 9 : Integer.parseInt(request.getParameter("loanRuleTypeValue"));
            double loanRate = StringUtil.isNullOrEmpty(request.getParameter("loanrate")) ? 0.0 : Double.parseDouble(request.getParameter("loanrate"));
            dataMap.put("loanrate", loanRate);
            dataMap.put("loanfee", StringUtil.isNullOrEmpty(request.getParameter("loanfee")) ? 0.0 : Double.parseDouble(request.getParameter("loanfee")));
            dataMap.put("percentloanfee", StringUtil.isNullOrEmpty(request.getParameter("percentloanfee")) ? 10.0 : Double.parseDouble(request.getParameter("percentloanfee")));
            dataMap.put("loanamount", loanAmount);
            dataMap.put("loaneligibility", StringUtil.isNullOrEmpty(request.getParameter("maximumloanEligibility")) ? 0.0 : Double.parseDouble(request.getParameter("maximumloanEligibility")));
            dataMap.put("externalCurrencyRate", StringUtil.isNullOrEmpty(request.getParameter("externalcurrencyrate")) ? 0.0 : Double.parseDouble(request.getParameter("externalcurrencyrate")));
            dataMap.put("termValue", termValue);
            dataMap.put("installmentinterval", installmentinterval);
            dataMap.put("entrynumber", entryNumber);
            dataMap.put("autogenerated", nextAutoNumber.equals(entryNumber));
            dataMap.put("installmenttype", request.getParameter("installmenttype"));
            dataMap.put("scheduletype", request.getParameter("scheduletype"));
            dataMap.put("loancategory", request.getParameter("loancategory"));
            dataMap.put("loantype", request.getParameter("loanType"));
            dataMap.put("loanTermType", loanTermType);
            dataMap.put("surety", request.getParameter("surety"));
            String disburseAccountId=request.getParameter("disburseAccount");
            String customerid = request.getParameter("customer");
            dataMap.put("disburseAccount", disburseAccountId);
            dataMap.put("loanAccount", accountid);

            dataMap.put("disbursementdate", StringUtil.isNullOrEmpty(request.getParameter("disbursementdate")) ? "" : df.parse(request.getParameter("disbursementdate")));
            dataMap.put("firstpaymentdate", StringUtil.isNullOrEmpty(request.getParameter("firstpaymentdate")) ? "" : df.parse(request.getParameter("firstpaymentdate")));
            dataMap.put("approveddate", StringUtil.isNullOrEmpty(request.getParameter("approveddate")) ? "" : df.parse(request.getParameter("approveddate")));
            dataMap.put("applicationdate", StringUtil.isNullOrEmpty(request.getParameter("applicationdate")) ? "" : df.parse(request.getParameter("applicationdate")));
            dataMap.put("currencyid", currencyid);
            dataMap.put("customerid", customerid);
            dataMap.put("companyid", companyid);

            dataMap.put("createdby", createdby);
            dataMap.put("modifiedby", modifiedby);
            dataMap.put("createdon", createdon);
            dataMap.put("updatedon", updatedon);
            
            if(loanRuleTypeValue == 0){
                dataMap.put("loanRuleType", LoanRuleType.ABSOLUTEVALUE);
            }else if(loanRuleTypeValue ==1){
                dataMap.put("loanRuleType", LoanRuleType.MULTIPLEOFSALARY);
            }else if(loanRuleTypeValue ==2){
                dataMap.put("loanRuleType", LoanRuleType.UNLIMITED);
            }
             
            KwlReturnObject result = accLoanDAOobj.saveLoanDisbursement(dataMap);
            disbursement = (Disbursement) result.getEntityList().get(0);
            String disbursementID = disbursement.getID();
            String loanRefNO = disbursement.getLoanrefnumber();

            //loanTermType [ 1- Month 2- Year]
            //Saving Repayment Details Against Loan amount
            Date firstPaymentDate = StringUtil.isNullOrEmpty(request.getParameter("firstpaymentdate")) ? new Date() : df.parse(request.getParameter("firstpaymentdate"));
            int termInMonths = 0;
            double interestRateMonth = 0;
            int differenceInDate=1;
            boolean isWeekly=false;
            boolean ismonthly=false;
            boolean isYear=false;
            boolean isDaily=false;
            boolean isBiWeekly=false;
             termInMonths = termValue;
            interestRateMonth = loanRate/100;
            if (loanTermType.equals(Constants.Monthly)) {
                ismonthly=true;
            } else if(loanTermType.equals(Constants.Yearly)) {
                isYear=true;
            }else if(loanTermType.equals(Constants.Weekly)) {
                differenceInDate=7;
                isWeekly=true;
            }else if(loanTermType.equals(Constants.Daily)) {
                isDaily=true;
            }else{
                differenceInDate=15;
                isBiWeekly=true;
            }
            Calendar cal = Calendar.getInstance();
            int srno = 1;
            double newBalance = 0;
            double principalPaid = 0;
            double interstPaid = 0;
//             mp=p*im*Math.pow(1+im,(double)nm)/(Math.pow(1+im,(double)nm)-1);
            double monthlyPayment = loanAmount * interestRateMonth * Math.pow(1 + interestRateMonth, (double) termInMonths) / (Math.pow(1 + interestRateMonth, (double) termInMonths) - 1);
            for (int intervalCount = 1; intervalCount < termInMonths; intervalCount++) { //amortization schedule except last interval

                RepaymentDetails repaymentDetails = new RepaymentDetails();
                repaymentDetails.setDisbursement(disbursement);
                repaymentDetails.setSrno(srno++);
                repaymentDetails.setCompany(disbursement.getCompany());
                repaymentDetails.setPaymentStatus(PaymentStatus.Unpaid);
                repaymentDetails.setStartDate(firstPaymentDate);
                cal.setTime(firstPaymentDate);
                if(isWeekly){
                    cal.add(Calendar.DATE, differenceInDate);
                }else if(ismonthly){
                    cal.add(Calendar.MONTH, differenceInDate);
                }else if(isYear){
                     cal.add(Calendar.YEAR, differenceInDate);
                }else if(isDaily){
                     cal.add(Calendar.DATE, differenceInDate);
                }else{
                     cal.add(Calendar.DATE, differenceInDate);
                }
                
                firstPaymentDate = cal.getTime();
                String date=df.format(firstPaymentDate);
                try{
                    firstPaymentDate=df.parse(date);
                }catch(ParseException ex){
                    firstPaymentDate = cal.getTime();
                }
                repaymentDetails.setEndDate(firstPaymentDate);

                repaymentDetails.setStartingbalance(loanAmount);
                interstPaid = loanAmount * interestRateMonth;     //interest paid
                principalPaid = monthlyPayment - interstPaid;   //princial paid
                newBalance = loanAmount - principalPaid; //new balance  
                loanAmount = newBalance;
                repaymentDetails.setMonthlyInstalment(monthlyPayment);
                repaymentDetails.setEndingbalance(newBalance);
                repaymentDetails.setPrinciple(principalPaid);
                repaymentDetails.setInterest(interstPaid);
                repaymentDetails.setAmount(authHandler.round((principalPaid + interstPaid), companyid));
                repaymentDetails.setAmountdue(authHandler.round((principalPaid + interstPaid), companyid));
                accSalesOrderDAOobj.SaveUpdateObject(repaymentDetails);

            }
            principalPaid = loanAmount;
            interstPaid = loanAmount * interestRateMonth;
            monthlyPayment = principalPaid + interstPaid;
            newBalance = 0;

            RepaymentDetails repaymentDetails = new RepaymentDetails();
            repaymentDetails.setDisbursement(disbursement);
            repaymentDetails.setSrno(srno++);
            repaymentDetails.setStartingbalance(loanAmount);
            repaymentDetails.setMonthlyInstalment(monthlyPayment);
            repaymentDetails.setPrinciple(principalPaid);
            repaymentDetails.setInterest(interstPaid);
            repaymentDetails.setEndingbalance(newBalance);
            repaymentDetails.setAmount(authHandler.round((principalPaid + interstPaid), companyid));
            repaymentDetails.setAmountdue(authHandler.round((principalPaid + interstPaid), companyid));
            repaymentDetails.setCompany(disbursement.getCompany());
            repaymentDetails.setPaymentStatus(PaymentStatus.Unpaid);
            repaymentDetails.setStartDate(firstPaymentDate);
            if (isWeekly) {
                cal.add(Calendar.DATE, differenceInDate);
            } else if (ismonthly) {
                cal.add(Calendar.MONTH, differenceInDate);
            } else if (isYear) {
                cal.add(Calendar.YEAR, differenceInDate);
            } else if (isDaily) {
               cal.add(Calendar.DATE, differenceInDate);
            }else{
                cal.add(Calendar.DATE, differenceInDate);
            }
            firstPaymentDate = cal.getTime();
            String date=df.format(firstPaymentDate);
            try{
                firstPaymentDate=df.parse(date);
            }catch(ParseException ex){
                firstPaymentDate = cal.getTime();
            }
            repaymentDetails.setEndDate(firstPaymentDate);
            accSalesOrderDAOobj.SaveUpdateObject(repaymentDetails);
//                          
//               double monthlyPayment =  calculateMonthlyPayment(loanAmount, termInYears, interestRate);
//                public static double calculateMonthlyPayment(
//                    int loanAmount, int termInYears, double interestRate) {
//
//                    // Convert interest rate into a decimal
//                    // eg. 6.5% = 0.065
//
//                    interestRate /= 100.0;
//
//                    // Monthly interest rate
//                    // is the yearly rate divided by 12
//
//                    double monthlyRate = interestRate / 12.0;
//
//                    // The length of the term in months
//                    // is the number of years times 12
//
//                    int termInMonths = termInYears * 12;
//
//                    // Calculate the monthly payment
//                    // Typically this formula is provided so
//                    // we won't go into the details
//
//                    // The Math.pow() method is used calculate values raised to a power
//
//                    double monthlyPayment =
//                        (loanAmount*monthlyRate) /
//                            (1-Math.pow(1+monthlyRate, -termInMonths));
//
//                    return monthlyPayment;
//                }



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
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, entryDate);
                    jeentryNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    jeIntegerPart = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                    jeDatePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);//Date After Prefix Part
                    jeDateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                    jeSeqFormatId = format.getID();
                    jeautogenflag = true;
                }
            }
            jeDataMap.put("entrynumber", jeentryNumber);
            jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
            jeDataMap.put(Constants.SEQNUMBER, jeIntegerPart);
            jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
            jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
            jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
            jeDataMap.put("autogenerated", true);
            jeDataMap.put(InvoiceConstants.entrydate, entryDate);
            jeDataMap.put("companyid", companyid);
            jeDataMap.put("memo", "");
            jeDataMap.put("createdby", createdby);
            jeDataMap.put("currencyid", currencyid);

            jeDataMap.put(Constants.Checklocktransactiondate, request.getParameter("applicationdate"));//for checking lock period ERP-16800
            HashSet jeDetails = new HashSet();
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            jeid = journalEntry.getID();
            jeDataMap.put("jeid", jeid);



            JSONObject jedjson = new JSONObject();
            jedjson.put("srno", jeDetails.size() + 1);
            jedjson.put("companyid", companyid);
            jedjson.put("amount", jeAmount);
            jedjson.put("accountid", accountid);
            jedjson.put("debit", true);
            jedjson.put("jeid", jeid);
            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jeDetails.add(jed);

//            disbursement.setCustomerEntry(jed);

            jedjson = new JSONObject();
            jedjson.put("srno", jeDetails.size() + 1);
            jedjson.put("companyid", companyid);
            jedjson.put("amount", jeAmount);
            jedjson.put("accountid", disburseAccountId);
            jedjson.put("debit", false);
            jedjson.put("jeid", jeid);
            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jeDetails.add(jed);

            jeDataMap.put("jedetails", jeDetails);
            jeDataMap.put("externalCurrencyRate", 0.0);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);
            
            disbursement.setJournalEntry(journalEntry);
            accSalesOrderDAOobj.SaveUpdateObject(disbursement);
            newList.add(disbursementID);
            newList.add(loanRefNO);
            newList.add(journalEntry.getID());
            newList.add(journalEntry.getEntryNumber());
//            
            if(isEdit && !StringUtil.isNullOrEmpty(oldjeid)){
                    accJournalEntryobj.deleteJEDtails(oldjeid, companyid);
                    result = accJournalEntryobj.permanentDeleteJournalEntry(oldjeid, companyid);
                    KwlReturnObject jedresult1 = accJournalEntryobj.deleteJECustomData(oldjeid);
            }
            //Save temporary saved files mapping in permanent table
            if(!StringUtil.isNullOrEmpty(savedFilesMappingId)){
                Map<String,Object> fileMap = new HashMap<>();
                fileMap.put("id", savedFilesMappingId);
                fileMap.put("companyid", companyid);
                KwlReturnObject mappedFilesResult = accSalesOrderDAOobj.getMappedFilesResult(fileMap);
                List mappedFiles = mappedFilesResult.getEntityList();
                Iterator itr = mappedFiles.iterator();
                KwlReturnObject objectResult = null;
                while(itr.hasNext()){
                    Object[] row = (Object[])itr.next();
                    String id = (String) row[0];
                    String documentId = (String) row[1];
                    objectResult = accountingHandlerDAOobj.getObject(InvoiceDocuments.class.getName(), documentId);
                    InvoiceDocuments document = (InvoiceDocuments) objectResult.getEntityList().get(0);
                    InvoiceDocumentCompMap invoiceDocumentmapping = new InvoiceDocumentCompMap();
                    invoiceDocumentmapping.setInvoiceID(disbursementID);
                    invoiceDocumentmapping.setCompany(company);
                    invoiceDocumentmapping.setDocument(document);
                    accSalesOrderDAOobj.SaveUpdateObject(invoiceDocumentmapping);
                }
                accSalesOrderDAOobj.deleteTemporaryMappedFiles(savedFilesMappingId,companyid);
            }
//             //Insert new entries again in optimized table.
//            accJournalEntryobj.saveAccountJEs_optimized(jeid);
//            String salesorder = request.getParameter("salesorderno");
//            String fileidstr = request.getParameter("fileidstr");
//            if (!StringUtil.isNullOrEmpty(salesorder)) {
//                HashMap<String, Object> SOMap = new HashMap<String, Object>();
//                    SOMap.put("id", salesorder);
//                    SOMap.put("contractid", contract.getID());
//                    SOMap.put("companyid", companyid);
//                    SOMap.put("orderdate",new Date(createdon));
//                    accSalesOrderDAOobj.saveSalesOrder(SOMap);
//                }
//            if (!StringUtil.isNullOrEmpty(fileidstr)) {
//                String[] fileidstrArray = fileidstr.split(",");
//                for (int cnt = 0; cnt < fileidstrArray.length; cnt++) {
//                    accSalesOrderDAOobj.updateContractFiles(billid, fileidstrArray[cnt]);
//                }
//            }    
        } catch (AccountingException ex) {
            try {
                throw new AccountingException(ex.getMessage());
            } catch (AccountingException ex1) {
                Logger.getLogger(accLoanDisbursmentControllerCMN.class.getName()).log(Level.SEVERE, null, ex1);
            }
        } catch (NumberFormatException | SessionExpiredException | ServiceException | NoSuchMessageException | ParseException | JSONException ex) {
            try {
                throw ServiceException.FAILURE("saveDisbursement " + ex.getMessage(), ex);
            } catch (ServiceException ex1) {
                Logger.getLogger(accLoanDisbursmentControllerCMN.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        return newList;
    }

  public ModelAndView getLoanDisbursements(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            jobj = getLoanDisbursements(request, false);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accLoanDisbursmentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accLoanDisbursmentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", Constants.model, jobj.toString());
    }
    
  public ModelAndView exportLoanDisbursements(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            jobj = getLoanDisbursements(request, true);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accLoanDisbursmentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accLoanDisbursmentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
  
    public JSONObject getLoanDisbursements(HttpServletRequest request, boolean isExport) {
        JSONObject jobj = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        try {
            Map<String, Object> requestParams = new HashMap<>();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            requestParams.put(Constants.df, df);
            requestParams.put(Constants.ss, request.getParameter(Constants.ss));
            requestParams.put(Constants.REQ_startdate, request.getParameter(Constants.REQ_startdate));
            requestParams.put(Constants.REQ_enddate, request.getParameter(Constants.REQ_enddate));
            requestParams.put("billid", request.getParameter("billid"));
            if (!isExport) {
                String start = request.getParameter("start");
                String limit = request.getParameter("limit");
                requestParams.put("start", start);
                requestParams.put("limit", limit);
            }
            requestParams.put("companyid", companyid);
            KwlReturnObject result = accLoanDAOobj.getDisbursement(requestParams);
            List<Disbursement> list = result.getEntityList();
            int count = result.getRecordTotalCount();
            
            // Column Model
            JSONObject commData = new JSONObject();
            JSONObject jMeta = new JSONObject();
            JSONArray jarrColumns = new JSONArray();
            JSONArray jarrRecords = new JSONArray();
            JSONObject jobjTemp = new JSONObject();
            
             String StoreRec="id,billid,loanrefno,suretyname,currencycode,currencysymbol,currencyid,customer,customerid,externalcurrencyrate,installmentinterval,installmenttype,"
                    + "loanTermValue,loanType,scheduletype,surety,maximumloanEligibility,loanrate,loanfee,loanamount,sequenceformatid,loanTermType,"
                    + "loancategory,termValue,disburseAccount,loanAccount,loanRuleType,loanRuleTypeValue,masterReceivedForm,isPaid,percentloanfee";
            
            String[] recArr=StoreRec.split(",");
            for (String rec: recArr){
                jobjTemp = new JSONObject();
                jobjTemp.put("name",rec);
                jarrRecords.put(jobjTemp);
            }
            
           
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", "");
            jobjTemp.put("dataIndex", "id");
            jobjTemp.put("hidden", true);
            jobjTemp.put("hideable", false);
            jarrColumns.put(jobjTemp);
          
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.loan.disbursementtab.loanrefno", null, RequestContextUtils.getLocale(request))); //// "Loan Ref No",
            jobjTemp.put("dataIndex", "loanrefno");
            jobjTemp.put("width", 75);
            jobjTemp.put("pdfwidth", 75);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "customerName");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.cust.name", null, RequestContextUtils.getLocale(request))); 
            jobjTemp.put("dataIndex", "customerName");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", 75);
            jobjTemp.put("pdfwidth", 75);
            jarrColumns.put(jobjTemp);
            

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "applicationdate");
            jobjTemp.put("type", "date");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.loan.disbursementtab.Applicationdate", null, RequestContextUtils.getLocale(request))); // "Date",
            jobjTemp.put("dataIndex", "applicationdate");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", 75);
            jobjTemp.put("pdfwidth", 75);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.loan.disbursementtab.surety", null, RequestContextUtils.getLocale(request))); // "Surty",
            jobjTemp.put("dataIndex", "suretyname");
            jobjTemp.put("align", "left");
            jobjTemp.put("width", 75);
            jobjTemp.put("pdfwidth", 75);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "firstpaymentdate");
            jobjTemp.put("type", "date");
            jarrRecords.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "disbursementdate");
            jobjTemp.put("type", "date");
            jarrRecords.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "firstpaymentdate");
            jobjTemp.put("type", "date");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.loan.disbursementtab.firstpaymentdate", null, RequestContextUtils.getLocale(request))); // "Date",
            jobjTemp.put("dataIndex", "firstpaymentdate");
            jobjTemp.put("width", 75);
            jobjTemp.put("pdfwidth", 75);
            jarrColumns.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "approveddate");
            jobjTemp.put("align", "center");
            jobjTemp.put("type", "date");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.loan.disbursementtab.approveddate", null, RequestContextUtils.getLocale(request))); // "Date",
            jobjTemp.put("dataIndex", "approveddate");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", 75);
            jobjTemp.put("pdfwidth", 75);
            jarrColumns.put(jobjTemp);
            
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.loan.disbursementtab.approvedloanamount", null, RequestContextUtils.getLocale(request))); // "Loan Amount",
            jobjTemp.put("dataIndex", "loanamount");
            jobjTemp.put("align", "right");
            jobjTemp.put("pdfrenderer", "rowcurrency");
            jobjTemp.put("width", 75);
            jobjTemp.put("pdfwidth", 75);
            jarrColumns.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.invoiceList.attachDocuments", null, RequestContextUtils.getLocale(request))); // "Loan Amount",
            jobjTemp.put("dataIndex", "attachdoc");
            jobjTemp.put("width", 75);
            jarrColumns.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "attachdoc");
            jarrRecords.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.invoiceList.attachments", null, RequestContextUtils.getLocale(request))); // "Loan Amount",
            jobjTemp.put("dataIndex", "attachment");
            jobjTemp.put("width", 75);
            jarrColumns.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "attachment");
            jarrRecords.put(jobjTemp);
           
             for (Disbursement disbursement : list) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("id", disbursement.getID());
                jSONObject.put("billid", disbursement.getID());
                jSONObject.put("number", disbursement.getLoanrefnumber());
                jSONObject.put("loanrefno", disbursement.getLoanrefnumber());
                jSONObject.put("customerName", disbursement.getCustomer().getName());
                jSONObject.put("loanfee", disbursement.getLoanFee());
                jSONObject.put("percentloanfee", disbursement.getPercentloanfee());
                jSONObject.put("loanamount", disbursement.getLoanAmount());
                jSONObject.put("loanrate", disbursement.getLoanRate());
                jSONObject.put("loanTermValue", disbursement.getTermValue());
                jSONObject.put("loancategory", disbursement.getLoancategory());
                jSONObject.put("customer", disbursement.getCustomer().getID());
                jSONObject.put("masterReceivedForm", ((disbursement.getCustomer().getMappingReceivedFrom() != null) ? disbursement.getCustomer().getMappingReceivedFrom().getID() : ""));
                jSONObject.put("customerid", disbursement.getCustomer().getID());
                jSONObject.put("externalcurrencyrate", disbursement.getExternalCurrencyRate());
                jSONObject.put("scheduletype", disbursement.getScheduletype());
                jSONObject.put("termValue", disbursement.getTermValue());
                jSONObject.put("loanTermType", disbursement.getTermType());
                jSONObject.put("disburseAccount", disbursement.getCreditaccount() == null ?"": disbursement.getCreditaccount().getID());
                jSONObject.put("loanAccount", disbursement.getDebitaccount() == null ?"": disbursement.getDebitaccount().getID());
                jSONObject.put(Constants.SEQUENCEFORMATID,disbursement.getSeqformat()==null?"":disbursement.getSeqformat().getID());
                jSONObject.put("surety",disbursement.getSurety()!= null ? disbursement.getSurety().getID() :"");
                jSONObject.put("suretyname",disbursement.getSurety()!= null ? disbursement.getSurety().getName() :"");
                jSONObject.put("loanType", disbursement.getLoanType().getID());
                jSONObject.put("installmenttype", disbursement.getInstallmenttype());
                jSONObject.put("installmentinterval", disbursement.getInstallmentinterval());
                jSONObject.put("maximumloanEligibility", disbursement.getLoanEligibility());
                jSONObject.put(InvoiceConstants.currencysymbol, disbursement.getCurrency() == null ? "" : disbursement.getCurrency().getSymbol());
                jSONObject.put(InvoiceConstants.currencyid, (disbursement.getCurrency() == null ? "" : disbursement.getCurrency().getCurrencyID()));
                jSONObject.put(InvoiceConstants.currencyname, (disbursement.getCurrency() == null ? "" : disbursement.getCurrency().getName()));
                jSONObject.put("applicationdate", disbursement.getApplicationdate() == null ? "" : df.format(disbursement.getApplicationdate()));
                jSONObject.put("approveddate", disbursement.getApproveddate() == null ? "" : df.format(disbursement.getApproveddate()));
                jSONObject.put("disbursementdate", disbursement.getDisbursementdate() == null ? "" : df.format(disbursement.getDisbursementdate()));
                jSONObject.put("firstpaymentdate", disbursement.getFirstpaymentdate() == null ? "" : df.format(disbursement.getFirstpaymentdate()));
                jSONObject.put("attachdoc", "");
                
                JSONObject jObj = accCustomerDAOobj.checkPaymentStausIsPaid(disbursement.getCustomer().getID(), companyid,disbursement.getLoanrefnumber());
                    JSONArray jArr1= jObj.getJSONArray("list");
                    if (jArr1 != null && jArr1.length() > 0 ) {
                        jSONObject.put("isPaid", true);
                    }else{
                        jSONObject.put("isPaid", false);
                    }
                 if (disbursement.getLoanRuleType() != null) {
                     jSONObject.put("loanRuleType", disbursement.getLoanRuleType());
                     if (disbursement.getLoanRuleType().equals(LoanRuleType.ABSOLUTEVALUE)) {
                         jSONObject.put("loanRuleTypeValue", disbursement.getLoanRuleType().ordinal());
                     } else if (disbursement.getLoanRuleType().equals(LoanRuleType.ABSOLUTEVALUE)) {
                         jSONObject.put("loanRuleTypeValue", disbursement.getLoanRuleType().ordinal());
                     } else if (disbursement.getLoanRuleType().equals(LoanRuleType.ABSOLUTEVALUE)) {
                         jSONObject.put("loanRuleTypeValue", disbursement.getLoanRuleType().ordinal());
                     }
                 }
                
                 HashMap<String, Object> hashMap = new HashMap<String, Object>();
                 hashMap.put("invoiceID", disbursement.getID());
                 hashMap.put("companyid", companyid);
                 KwlReturnObject object = accountingHandlerDAOobj.getinvoiceDocuments(hashMap);
                 int attachemntcount = object.getRecordTotalCount();
                 jSONObject.put("attachment", attachemntcount);

                dataJArr.put(jSONObject);
            }
         
            commData.put("success", true);
            commData.put("coldata", dataJArr);
            commData.put("columns", jarrColumns);
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            commData.put("totalCount", count);
            jMeta.put("fields", jarrRecords);
            commData.put("metaData", jMeta);
            JSONArray jcom = new JSONArray();
            jcom.put(commData);

            jobj.put("valid", true);
            if (isExport) {
                jobj.put("data", dataJArr);
            } else {
                jobj.put("data", commData);
            }
       } catch (SessionExpiredException | ServiceException | JSONException ex) {
            Logger.getLogger(accLoanDisbursmentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
  
    public ModelAndView getRepaymentSheduleDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            jobj = getRepaymentSheduleDetails(request, false);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accLoanDisbursmentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accLoanDisbursmentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", Constants.model, jobj.toString());
    }
    public ModelAndView exportRepaymentSheduleDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
          
            jobj = getRepaymentSheduleDetails(request, true);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accLoanDisbursmentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accLoanDisbursmentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    public JSONObject getRepaymentSheduleDetails(HttpServletRequest request, boolean isExport) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        String loanTermType = "";
        try {
            Map<String, Object> requestParams = new HashMap<>();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String disbursement = request.getParameter("disbursement");
            String paymentStatus = request.getParameter("paymentStatus");
            DateFormat df = authHandler.getDateOnlyFormat(request);
            requestParams.put(Constants.df, df);
            requestParams.put(Constants.ss, request.getParameter(Constants.ss));
            requestParams.put(Constants.REQ_startdate, request.getParameter(Constants.REQ_startdate));
            requestParams.put(Constants.REQ_enddate, request.getParameter(Constants.REQ_enddate));
            String start = "";
            String limit = "";
            if (!isExport) {
                start = request.getParameter("start");
                limit = request.getParameter("limit");
                requestParams.put("start", start);
                requestParams.put("limit", limit);
            }
            requestParams.put("companyid", companyid);
            requestParams.put("disbursement", disbursement);
            requestParams.put("paymentStatus", paymentStatus);
            KwlReturnObject result = accLoanDAOobj.getRepaymentSheduleDetails(requestParams);
            List<RepaymentDetails> list = result.getEntityList();
            int count = result.getRecordTotalCount();
            String currencyIdOfLoanDocument = "";
            String currencySymbolOfLoanDocument = "";
            double amount = 0.0, amountdue = 0.0, amountdueoriginal = 0.0;

            // Column Model
            JSONObject commData = new JSONObject();
            JSONObject jMeta = new JSONObject();
            JSONArray jarrColumns = new JSONArray();
            JSONArray jarrRecords = new JSONArray();
            JSONObject jobjTemp = new JSONObject();
            String StoreRec = " id,currencycode,currencysymbol,documentno,currencyid,monthyear,startingbalance,intrest,principal,"
                    + "monthlyinstalment,endingbalance,paymentstatus,startdate,enddate,disbursement,description,amountdue,amountDueOriginal,"
                    + "amountDueOriginalSaved,exchangeratefortransaction,currencyidtransaction,currencysymboltransaction,amount,repaymentscheduleid,week";
            String[] recArr = StoreRec.split(",");
            for (String rec : recArr) {
                jobjTemp = new JSONObject();
                jobjTemp.put("name", rec);
                jarrRecords.put(jobjTemp);
            }

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "");
            jobjTemp.put("dataIndex", "id");
            jobjTemp.put("hidden", true);
            jarrColumns.put(jobjTemp);
            for (RepaymentDetails repaymentDetails : list) {

                JSONObject jSONObject = new JSONObject();
                jSONObject.put("id", repaymentDetails.getID());
                Date date = repaymentDetails.getStartDate();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                String month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH);
//                String weekDay = calendar.getDisplayName(Calendar.WEEK_OF_MONTH, Calendar.LONG, Locale.ENGLISH);
                int weekDate = calendar.get(Calendar.DATE);
                int year = calendar.get(Calendar.YEAR);
                if (repaymentDetails.getDisbursement().getTermType().equals(Constants.Monthly) || repaymentDetails.getDisbursement().getTermType().equals(Constants.Yearly)) { // TermType=Weekly
                    loanTermType = "Month & Year";
                    jSONObject.put("monthyear", month + " " + year);
                } else if (repaymentDetails.getDisbursement().getTermType().equals(Constants.Weekly)) {
                    loanTermType = "Weekly";
                    jSONObject.put("monthyear", weekDate + " " + month + " " + year);
                } else if (repaymentDetails.getDisbursement().getTermType().equals(Constants.Daily)) {
                    loanTermType = "Daily";
                    jSONObject.put("monthyear", weekDate + " " + month + " " + year);

                }else{
                    loanTermType = "Bi-Weekly";
                    jSONObject.put("monthyear", weekDate + " " + month + " " + year);
                }
                jSONObject.put(InvoiceConstants.currencysymbol, repaymentDetails.getDisbursement().getCurrency() == null ? "" : repaymentDetails.getDisbursement().getCurrency().getSymbol());
                jSONObject.put(InvoiceConstants.currencyid, (repaymentDetails.getDisbursement().getCurrency() == null ? "" : repaymentDetails.getDisbursement().getCurrency().getCurrencyID()));
                jSONObject.put(InvoiceConstants.currencyname, (repaymentDetails.getDisbursement().getCurrency() == null ? "" : repaymentDetails.getDisbursement().getCurrency().getName()));
                jSONObject.put("startingbalance", repaymentDetails.getStartingbalance());
                jSONObject.put("intrest", repaymentDetails.getInterest());
                jSONObject.put("documentno", repaymentDetails.getDisbursement().getLoanrefnumber());
                jSONObject.put("principal", repaymentDetails.getPrinciple());
                jSONObject.put("monthlyinstalment", repaymentDetails.getMonthlyInstalment());
                jSONObject.put("endingbalance", repaymentDetails.getEndingbalance());
                jSONObject.put("paymentstatus", repaymentDetails.getPaymentStatus());
                jSONObject.put("startdate", repaymentDetails.getStartDate());
                jSONObject.put("enddate", repaymentDetails.getEndDate());
                amountdue = repaymentDetails.getAmountdue();
                amountdueoriginal = amountdue;
                currencyIdOfLoanDocument = repaymentDetails.getDisbursement().getCurrency().getCurrencyID();
                currencySymbolOfLoanDocument = repaymentDetails.getDisbursement().getCurrency().getSymbol();
                jSONObject.put("amountdue", amountdue);
                jSONObject.put("amount", repaymentDetails.getAmount());
                jSONObject.put("amountDueOriginal", amountdueoriginal);
                jSONObject.put("repaymentscheduleid", repaymentDetails.getID());
                jSONObject.put("amountDueOriginalSaved", amountdueoriginal);
                jSONObject.put("exchangeratefortransaction", (amountdueoriginal <= 0 && amountdue <= 0) ? 0 : (amountdue / amountdueoriginal));
                jSONObject.put("currencyidtransaction", currencyIdOfLoanDocument);
                jSONObject.put("currencysymboltransaction", currencySymbolOfLoanDocument);
                jSONObject.put("description", "Re-Payment for " + month + "-" + year);
                jSONObject.put("disbursement", repaymentDetails.getDisbursement().getID());
                dataJArr.put(jSONObject);
            }
            JSONArray pagedJson = new JSONArray();
            pagedJson = dataJArr;
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            jobjTemp = new JSONObject();
            jobjTemp.put("header", loanTermType); // "Month Year",
            jobjTemp.put("dataIndex", "monthyear");
            jobjTemp.put("width", 75);
            jobjTemp.put("pdfwidth", 75);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.loan.repaymentreport.startingbalance", null, RequestContextUtils.getLocale(request))); // "Starting Balance",
            jobjTemp.put("dataIndex", "startingbalance");
            jobjTemp.put("align", "right");
            jobjTemp.put("pdfrenderer", "rowcurrency");
            jobjTemp.put("width", 75);
            jobjTemp.put("pdfwidth", 75);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", loanTermType + " Instalment"); //"Monthly Instalment",
            jobjTemp.put("dataIndex", "monthlyinstalment");
            jobjTemp.put("align", "right");
            jobjTemp.put("pdfrenderer", "rowcurrency");
            jobjTemp.put("width", 75);
            jobjTemp.put("pdfwidth", 75);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.loan.repaymentreport.principal", null, RequestContextUtils.getLocale(request))); // "Principal",
            jobjTemp.put("dataIndex", "principal");
            jobjTemp.put("align", "right");
            jobjTemp.put("pdfrenderer", "rowcurrency");
            jobjTemp.put("width", 75);
            jobjTemp.put("pdfwidth", 75);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.loan.repaymentreport.interest", null, RequestContextUtils.getLocale(request))); // "Intrest",
            jobjTemp.put("dataIndex", "intrest");
            jobjTemp.put("align", "right");
            jobjTemp.put("pdfrenderer", "rowcurrency");
            jobjTemp.put("width", 75);
            jobjTemp.put("pdfwidth", 75);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.loan.repaymentreport.endingbalance", null, RequestContextUtils.getLocale(request))); // "Ending Balance",
            jobjTemp.put("dataIndex", "endingbalance");
            jobjTemp.put("align", "right");
            jobjTemp.put("pdfrenderer", "rowcurrency");
            jobjTemp.put("width", 75);
            jobjTemp.put("pdfwidth", 75);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.loan.repaymentreport.Paymentstatus", null, RequestContextUtils.getLocale(request))); // "Payment Status",
            jobjTemp.put("dataIndex", "paymentstatus");
            jobjTemp.put("width", 75);
            jobjTemp.put("pdfwidth", 75);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);


            commData.put("success", true);
            commData.put("coldata", pagedJson);
            commData.put("columns", jarrColumns);
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            commData.put("totalCount", count);
            jMeta.put("fields", jarrRecords);
            commData.put("metaData", jMeta);
            JSONArray jcom = new JSONArray();
            jcom.put(commData);

            jobj.put("valid", true);
            if (isExport) {
                jobj.put("data", dataJArr);
            } else {
                jobj.put("data", commData);
            }
        } catch (SessionExpiredException | JSONException ex) {
            Logger.getLogger(accLoanDisbursmentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
    
    
   public ModelAndView deleteDisbursementsPermanent(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String linkedTransaction = deleteDisbursementsPermanent(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.loan.disbursementDeletedSuccessfully" + linkedTransaction, null, RequestContextUtils.getLocale(request));
            
        } catch (SessionExpiredException  ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accLoanDisbursmentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accLoanDisbursmentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accLoanDisbursmentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public String deleteDisbursementsPermanent(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        String linkedTransaction = "";
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String modulename ="Loan Disbursment";
            String auditMsg="";
            String auditID = AuditAction.DELETE_DISBURSEMENT;
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                String disbursementid = StringUtil.DecodeText(jobj.optString("disbursementid"));
                KwlReturnObject res = accountingHandlerDAOobj.getObject(Disbursement.class.getName(), disbursementid);
                Disbursement disbursement = (Disbursement) res.getEntityList().get(0);
                String disburementno = disbursement.getLoanrefnumber();
                Map<String, Object> requestParams = new HashMap<>();
                requestParams.put("disbursementid", disbursementid);
                requestParams.put("companyid", companyid);
                requestParams.put("disbursementno", disburementno);
                if (!StringUtil.isNullOrEmpty(disbursementid)) {
                    accLoanDAOobj.deleteDisbursementsPermanent(requestParams);
                    accJournalEntryobj.deleteJournalEntryPermanent(disbursement.getJournalEntry().getID(), companyid);
//                    auditTrailObj.insertAuditLog("86", " User " + sessionHandlerImpl.getUserFullName(request) + " has deleted a "+modulename+" Permanently " + pono, request, poid);
                }
                auditMsg = "User " + sessionHandlerImpl.getUserFullName(request) +" " +messageSource.getMessage("acc.loan.disbursement.delete.audit", null, RequestContextUtils.getLocale(request));
                auditTrailObj.insertAuditLog(auditID, auditMsg+disburementno, request, disbursementid);
            }
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)));
        }
        return linkedTransaction;
    }
    
    public ModelAndView getLoanRepaymentSchedulesForReceipt(HttpServletRequest request , HttpServletResponse response){
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        JSONArray pagedJson = new JSONArray();
        JSONArray jArray = new JSONArray();
        try{
            Map<String, Object> requestParams = new HashMap<>();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyId = sessionHandlerImpl.getCurrencyID(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            requestParams.put(Constants.df, df);
            requestParams.put(Constants.ss, request.getParameter(Constants.ss));
            requestParams.put(Constants.REQ_startdate, request.getParameter(Constants.REQ_startdate));
            requestParams.put(Constants.REQ_enddate, request.getParameter(Constants.REQ_enddate));
            requestParams.put("paymentStatus", request.getParameter("paymentStatus"));
            requestParams.put("upperLimitDate", request.getParameter("upperLimitDate"));
            requestParams.put("isEdit", request.getParameter("isEdit"));
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            if (!StringUtil.isNullOrEmpty(request.getParameter("accid"))) {
                String accId = request.getParameter("accid");;
                requestParams.put("accId", accId);
            }
            requestParams.put("companyid", companyid);
            
            KwlReturnObject result = accLoanDAOobj.getRepaymentSheduleDetailsForPayment(requestParams);
            List<RepaymentDetails> list = result.getEntityList();
            if (!list.isEmpty()) {
                Map<String, Object> mapForJArray = new HashMap<String, Object>();
                mapForJArray.put("list", list);
                mapForJArray.put("df", df);
                mapForJArray.put("currencyfilterfortrans", request.getParameter("currencyfilterfortrans"));
                mapForJArray.put("currencyId", currencyId);
                mapForJArray.put("companyid", companyid);
                jArray = getLoanRepaymentSchedulesForReceiptArray(mapForJArray);
                pagedJson = jArray;
                if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                    pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
                }
            }
            success = true;
            // Below function is to get column model and record.- ERP-18727
            getColumnModelAndRecordDataForRepayment(request,jobj);    
            jobj.put("coldata", pagedJson);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch(Exception ex){
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(accLoanDisbursmentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString()); 
    }
    public JSONArray getLoanRepaymentSchedulesForReceiptArray(Map<String,Object> map){
        JSONArray jArray=new JSONArray();
        List<RepaymentDetails> list = (List<RepaymentDetails>) map.get("list");
        DateFormat df = (DateFormat) map.get("df");
        try{
        if(list!=null && !list.isEmpty()){
            Date startDate=null;
            Calendar cal = Calendar.getInstance();
            String repaymentMonthName="";
            int repaymentYearName=0;
            double amount=0.0, amountdue=0.0, amountdueoriginal=0.0;
            String currencyFilterForTrans = map.get("currencyfilterfortrans")!=null?map.get("currencyfilterfortrans").toString():"";
            String currencyId = map.get("currencyId").toString();
            String companyId = map.get("companyid").toString();
            String currencyIdOfLoanDocument = "";
            String currencySymbolOfLoanDocument = "";
            HashMap<String,Object> requestParamsForCurrency = new HashMap<>();
            requestParamsForCurrency.put("gcurrencyid", currencyId);
            requestParamsForCurrency.put("companyid", companyId);
            for(RepaymentDetails RD : list){
                JSONObject obj = new JSONObject();
                obj.put("billno", RD.getDisbursement().getLoanrefnumber());
                startDate = RD.getStartDate();
                cal.setTime(startDate);
                repaymentMonthName = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH);
                repaymentYearName = cal.get(Calendar.YEAR);
                obj.put("repaymentschedulename", repaymentMonthName+" "+repaymentYearName);
                obj.put("repaymentscheduleid", RD.getID());
                obj.put("accountid", RD.getDisbursement().getCustomer().getAccount()!=null?RD.getDisbursement().getCustomer().getAccount().getID():"");
                obj.put("billid", RD.getDisbursement().getID());
                obj.put("date", df.format(RD.getDisbursement().getDisbursementdate()));
                obj.put("amount", RD.getAmount());
                amountdue = RD.getAmountdue();
                amountdueoriginal = amountdue;
                currencyIdOfLoanDocument = RD.getDisbursement().getCurrency().getCurrencyID();
                currencySymbolOfLoanDocument = RD.getDisbursement().getCurrency().getSymbol();
                if(!StringUtil.isNullOrEmpty(currencyFilterForTrans) && !currencyFilterForTrans.equalsIgnoreCase(currencyIdOfLoanDocument)){
                    KwlReturnObject bAmtCurrencyFilter = null;
                    bAmtCurrencyFilter = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParamsForCurrency, amountdue, currencyIdOfLoanDocument, currencyFilterForTrans, RD.getDisbursement().getDisbursementdate(), RD.getDisbursement().getExternalCurrencyRate());
                    amountdue = (Double) bAmtCurrencyFilter.getEntityList().get(0);
                }
                obj.put("amountdue", amountdue);
                obj.put("amountDueOriginal", amountdueoriginal);
                obj.put("amountDueOriginalSaved", amountdueoriginal);
                obj.put("exchangeratefortransaction", (amountdueoriginal <= 0 && amountdue <= 0) ? 0 : (amountdue / amountdueoriginal));
                obj.put("currencyidtransaction", currencyIdOfLoanDocument);
                obj.put("currencysymboltransaction", currencySymbolOfLoanDocument);
                obj.put("description", "Re-Payment for "+repaymentMonthName+"-"+repaymentYearName);
                jArray.put(obj);
            }
        }
        } catch (ServiceException | JSONException ex){
            Logger.getLogger(accLoanDisbursmentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArray;
    }
    
    public void getColumnModelAndRecordDataForRepayment(HttpServletRequest request,JSONObject object) {
        JSONObject jMeta = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        JSONObject jobjTemp = new JSONObject();
        String storeRec = "";
        try {
            storeRec = "repaymentschedulename,repaymentscheduleid,exchangeratefortransaction,currencysymboltransaction,currencyidtransaction,currencynametransaction,currencyname,currencyid,accountid,description";
            String[] recArr=storeRec.split(",");
            // Get those fields in record for whome, no special properties present like type, defVal, mapping etc.
            for (String rec: recArr){
                jobjTemp = new JSONObject();
                jobjTemp.put("name",rec);
                jarrRecords.put(jobjTemp);
            }
            
            // Get those fields in record which has special properties
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "documentno");
            jobjTemp.put("mapping", "billno");
            jarrRecords.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "date");
            jobjTemp.put("type", "date");
            jarrRecords.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "amount");
            jobjTemp.put("type", "float");
            jarrRecords.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "amountdue");
            jobjTemp.put("type", "float");
            jarrRecords.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "amountDueOriginal");
            jobjTemp.put("type", "float");
            jarrRecords.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "amountDueOriginalSaved");
            jobjTemp.put("type", "float");
            jarrRecords.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "documentid");
            jobjTemp.put("mapping", "billid");
            jarrRecords.put(jobjTemp);
            
            // Gel column model - 
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.accPref.autoloanrefnumber", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "documentno");
            jarrColumns.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.loan.disbursementtab.repaymentSchedule", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "repaymentschedulename");
            jobjTemp.put("align", "center");
            jarrColumns.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.loan.disbursementtab.disbursementdate", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "date");
            jobjTemp.put("align", "center");
            jarrColumns.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.het.53", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "amount");
            jobjTemp.put("align", "right");
            jarrColumns.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.mp.amtDue", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "amountDueOriginal");
            jobjTemp.put("align", "right");
            jarrColumns.put(jobjTemp);
            
            // get above data along with extra data
            object.put("columns", jarrColumns);
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            jMeta.put("fields", jarrRecords);
            object.put("metaData", jMeta);
            
        } catch (JSONException | NoSuchMessageException ex) {
            Logger.getLogger(accLoanDisbursmentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public ModelAndView attachDocuments(HttpServletRequest request, HttpServletResponse response) {
        String msg = "";
        Boolean success = false;
        JSONObject jobj = new JSONObject();
        JSONObject finalObject = new JSONObject();
        String savedFilesMappingId = "";
        String savedFilesId = "", savedDocId = "";
        String[] returnData = new String[3];
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CF_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            returnData = uploadDoc(request);
            savedFilesMappingId = returnData[0];
            savedFilesId = returnData[1];
            savedDocId = returnData[2];
            success = true;
            msg = messageSource.getMessage("acc.invoiceList.bt.fileUploadedSuccess", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
        } catch (ServiceException ex) {
            success = false;
            msg = ex.getMessage();
            txnManager.rollback(status);
            Logger.getLogger(accLoanDisbursmentControllerCMN.class.getName()).log(Level.SEVERE, "saveUsers", ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            success = false;
            txnManager.rollback(status);
            Logger.getLogger(accLoanDisbursmentControllerCMN.class.getName()).log(Level.SEVERE, "saveUsers", ex);
        } finally {
            try {
                jobj.put("success", success);
                jobj.put("msg", msg);
                jobj.put("savedFilesMappingId", savedFilesMappingId);
                jobj.put("file", savedFilesId);
                jobj.put("docid", savedDocId);
                finalObject.put("data", jobj);
            } catch (com.krawler.utils.json.base.JSONException ex) {
                Logger.getLogger(accLoanDisbursmentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", "model", finalObject.toString());
    }
    
    public String[] uploadDoc(HttpServletRequest request) throws ServiceException, AccountingException {
        String[] returnData = new String[3];
        String savedFilesMappingId="";
        String savedFilesId="";
        String savedDocId="";
        try {
            String result = "";
            Boolean fileflag = false;
            String fileName = "";
            boolean isUploaded;
            String Ext;
            final String sep = StorageHandler.GetFileSeparator();
            DiskFileUpload fu = new DiskFileUpload();
            java.util.List fileItems = null;
            FileItem fi = null;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            savedFilesMappingId = request.getParameter("savedFilesMappingId");
            try {
                fileItems = fu.parseRequest(request);
            } catch (FileUploadException e) {
                throw ServiceException.FAILURE("ProfileHandler.updateProfile", e);
            }
            java.util.HashMap arrParam = new java.util.HashMap();
            for (java.util.Iterator k = fileItems.iterator(); k.hasNext();) {
                fi = (FileItem) k.next();
                arrParam.put(fi.getFieldName(), fi.getString());
                if (!fi.isFormField()) {
                    if (fi.getSize() != 0) {
                        fileflag = true;
                        fileName = new String(fi.getName().getBytes());
                    } else {
                        throw new AccountingException("File not uploaded! File should not be empty.");    //When file is empty
                    }
                }
            }

            if (fileflag) {
                try {
                    String storePath = StorageHandler.GetDocStorePath();
                    File destDir = new File(storePath);
                    if (!destDir.exists()) {
                        destDir.mkdirs();
                    }
                    int doccount = 0;
                    fu = new DiskFileUpload();
                    fu.setSizeMax(-1);
                    fu.setSizeThreshold(4096);
                    fu.setRepositoryPath(storePath);
                    for (Iterator i = fileItems.iterator(); i.hasNext();) {
                        fi = (FileItem) i.next();
                        if (!fi.isFormField() && fi.getSize() != 0 && doccount < 3) {
                            Ext = "";
                            doccount++;//ie 8 fourth file gets attached				
                            String filename = UUID.randomUUID().toString();
                            try {
                                fileName = new String(fi.getName().getBytes(), "UTF8");
                                if (fileName.contains(".")) {
                                    Ext = fileName.substring(fileName.lastIndexOf("."));
                                }
                                if (fi.getSize() != 0) {
                                    isUploaded = true;
                                    File uploadFile = new File(storePath + sep
                                            + filename + Ext);
                                    fi.write(uploadFile);

                                    InvoiceDocuments document=new InvoiceDocuments();
                                    document.setDocID(filename);
                                    document.setDocName(fileName);
                                    document.setDocType("");                                  
                                    KwlReturnObject cmp = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                                    Company company = (Company) cmp.getEntityList().get(0);
                                    Map<String,Object> fileMap = new HashMap<>();
                                    if(StringUtil.isNullOrEmpty(savedFilesMappingId)){
                                        savedFilesMappingId = UUID.randomUUID().toString();
                                    } else {
                                        KwlReturnObject savedFilesIdResult = accInvoiceDAOObj.getDocumentIdFromMappingId(savedFilesMappingId,companyid);
                                        List savedFilesList = savedFilesIdResult.getEntityList();
                                        Iterator itr = savedFilesList.iterator();
                                        while(itr.hasNext()){
                                            savedFilesId += itr.next().toString()+",";
                                        }
                                    }
                                    fileMap.put("id", savedFilesMappingId);
                                    fileMap.put("companyid", companyid);
                                    if(arrParam.containsKey("invoiceid")&&!StringUtil.isNullOrEmpty(arrParam.get("invoiceid").toString())){
                                        fileMap.put("invoiceid", arrParam.get("invoiceid").toString());
                                    } else {
                                        fileMap.put("invoiceid", "");
                                    }
                                    
                                    HashMap<String,Object> hashMap=new HashMap<String, Object>();
                                    hashMap.put("InvoceDocument",document);
                                    accInvoiceDAOObj.saveinvoiceDocuments(hashMap);
                                    fileMap.put("documentid", document.getID());
                                    accLoanDAOobj.saveFileMapping(fileMap);
                                    savedFilesId+=document.getID()+",";
                                    savedDocId = document.getDocID();
                                } else {
                                    isUploaded = false;
                                }
                            } catch (Exception e) {
                                Logger.getLogger(accLoanDisbursmentControllerCMN.class.getName()).log(Level.SEVERE, null, e);
                                throw ServiceException.FAILURE("accLoanDisbursmentControllerCMN.uploadDoc", e);
                            }
                        }
                    }                    
                } catch (Exception ex) {
                    Logger.getLogger(accLoanDisbursmentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                    throw ServiceException.FAILURE("accLoanDisbursmentControllerCMN.uploadDoc", ex);
                }
            }           
        } catch(AccountingException ae){
            throw new AccountingException("File not uploaded! File should not be empty.");
        } catch (Exception ex) {
            Logger.getLogger(accLoanDisbursmentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accLoanDisbursmentControllerCMN.uploadDoc", ex);
        }
        returnData[0] = savedFilesMappingId;
        if(!StringUtil.isNullOrEmpty(savedFilesId)){
            savedFilesId = savedFilesId.substring(0, savedFilesId.length()-1);
        }
        returnData[1] = savedFilesId;
        returnData[2] = savedDocId;
        return returnData;
    }     

    public ModelAndView getTemporarySavedFiles(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            String fileId = request.getParameter("fileid");
            String fileidStr = "";
            if (!StringUtil.isNullOrEmpty(fileId)) {
                String fileIdarr[] = fileId.split(",");
                for (int i = 0; i < fileIdarr.length; i++) {
                    fileidStr += "'" + fileIdarr[i] + "',";
                }
                if (!StringUtil.isNullOrEmpty(fileidStr)) {
                    fileidStr = fileidStr.substring(0, fileidStr.length() - 1);
                }
            }
            requestParams.put("fileid", fileidStr);
            KwlReturnObject kmsg = accLoanDAOobj.getTemporarySavedFiles(requestParams);
            jobj = getTemporarySavedFiles(kmsg.getEntityList(), request, kmsg.getRecordTotalCount());
        } catch (Exception ex) {
            Logger.getLogger(accLoanDisbursmentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    /**
     * Get temporary and permanent saved files from documents table
     * @param request
     * @param response
     * @return
     * @throws ServletException 
     */
    public ModelAndView getTemporaryAndPermanentSavedFiles(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            //Get fileid of temporary saved files
            String fileId = request.getParameter("fileid");
            boolean copyInv = StringUtil.isNullOrEmpty(request.getParameter("copyInv")) ? false : Boolean.parseBoolean(request.getParameter("copyInv").toString());
            String fileidStr = "";
            if (!StringUtil.isNullOrEmpty(fileId)) {
                String fileIdarr[] = fileId.split(",");
                for (int i = 0; i < fileIdarr.length; i++) {
                    fileidStr += "'" + fileIdarr[i] + "',";
                }
                if (!StringUtil.isNullOrEmpty(fileidStr)) {
                    fileidStr = fileidStr.substring(0, fileidStr.length() - 1);
                }
            }
            //If fileid is present then fetch temporary documents
            if(!StringUtil.isNullOrEmpty(fileidStr)) {
                requestParams.put("fileid", fileidStr);
                //Get temporary saved documents
                KwlReturnObject kmsg = accLoanDAOobj.getTemporarySavedFiles(requestParams);
                //put document details in json object
                jobj = getTemporarySavedFiles(kmsg.getEntityList(), request, kmsg.getRecordTotalCount());
            }
            /*
             *In copy case we are not fetching data from permanant Table.
             */
            if(!copyInv){
                //Get permanent saved documents
                String docid = request.getParameter("docid") != null ? request.getParameter("docid") : "";
                String companyid = request.getParameter("companyid") != null ? request.getParameter("companyid") : "";
                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                hashMap.put("invoiceID", docid);
                hashMap.put("companyid", companyid);
                //Get document details from documentid and company
                KwlReturnObject object = accInvoiceDAOObj.getinvoiceDocuments(hashMap);
                //put document details in json object
                Iterator ite = object.getEntityList().iterator();
                while (ite.hasNext()) {
                    JSONObject temp = new JSONObject();
                    Object[] row = (Object[])ite.next();
                    temp.put("id", row[2]);
                    temp.put("filename", row[0]);
                    temp.put("imgname", row[0]);
                    temp.put("docid", row[2]);
                    jobj.append("data", temp);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accLoanDisbursmentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getTemporarySavedFiles(List ll, HttpServletRequest request, int totalSize) {
        JSONObject jobj = new JSONObject();
        try {
            Iterator ite = ll.iterator();
            while (ite.hasNext()) {
                JSONObject temp = new JSONObject();
                Object[] row = (Object[])ite.next();
                temp.put("id", row[0]);
                temp.put("filename", row[1]);
                temp.put("imgname", row[1]);
                temp.put("docid", row[0]);
                temp.put("docRefId",row[3]);
                jobj.append("data", temp);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return jobj;
    }
}
