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
import com.krawler.common.admin.Company;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.customFieldMaster.fieldDataManager;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.bankreconciliation.accBankReconciliationDAO;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesConstants;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import com.krawler.spring.accounting.debitnote.accDebitNoteDAO;
import com.krawler.spring.accounting.discount.accDiscountDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.invoice.accInvoiceCMN;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.accounting.receipt.accReceiptController;
import com.krawler.spring.accounting.receipt.accReceiptControllerNew;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
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
 * @author Pandurang
 */
public class accCreditNoteControllerCMN extends MultiActionController implements MessageSourceAware{
    
    private HibernateTransactionManager txnManager;
    private accCreditNoteDAO accCreditNoteDAOobj;
    private exportMPXDAOImpl exportDaoObj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private String successView;
    private MessageSource messageSource;
    public ImportHandler importHandler;
    public accCustomerDAO accCustomerDAOObj;
    private accCreditNoteService accCreditNoteService;
    private accCreditNoteServiceCMN accCreditNoteServiceCMN;
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
    public void setAccCustomerDAO(accCustomerDAO accCustomerDAOObj) {
        this.accCustomerDAOObj = accCustomerDAOObj;
    }

    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    public void setaccCreditNoteService(accCreditNoteService accCreditNoteService) {
        this.accCreditNoteService = accCreditNoteService;
    }
    
    public void setaccCreditNoteServiceCMN(accCreditNoteServiceCMN accCreditNoteServiceCMN) {
        this.accCreditNoteServiceCMN = accCreditNoteServiceCMN;
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
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        List li = accCreditNoteServiceCMN.linkCreditNote(request, "", true);   // "true" flag is passed for inserting Audit Trial entry ( ERP-18558 )
            issuccess = true;
            msg = messageSource.getMessage("acc.field.CreditNotehasbeenLinkedtoCustomerInvoicesuccessfully", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);

            //==============Create Rounding JE Start=====================
            try {
                accCreditNoteService.postRoundingJEAfterLinkingInvoiceInCreditNote(paramJobj);
            } catch (ServiceException ex) {
                Logger.getLogger(accCreditNoteControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
            //==============Create Rounding JE End=======================

        } catch (AccountingException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
            Logger.getLogger(accCreditNoteControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCreditNoteControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCreditNoteControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accCreditNoteControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
        try {
            jobj.put(Constants.RES_success, issuccess);
            jobj.put(Constants.RES_msg, msg);
        } catch (JSONException ex) {
            Logger.getLogger(accCreditNoteControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    return new ModelAndView("jsonView", "model", jobj.toString());
}
     
    public ModelAndView getCreditNoteLinkedDocumnets(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
        jobj = accCreditNoteServiceCMN.getCreditNoteLinkedDocumnets(request);
            issuccess = true;
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCreditNoteControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            msg = "accCreditNoteControllerCMN.getCreditNoteInvoiceRows:" + ex.getMessage();
            Logger.getLogger(accCreditNoteControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCreditNoteControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView unlinkCreditNote(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
        List li = accCreditNoteServiceCMN.unlinkCreditNoteFromTransactions(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.cn.unlinkedFromSalesInvoice", null, RequestContextUtils.getLocale(request));
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

       public ModelAndView deleteCreditNotesPermanent(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = accCreditNoteServiceCMN.deleteCreditNotesPermanentJSON(paramJobj);
        } catch (SessionExpiredException | JSONException ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getCreditNoteRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = accCreditNoteServiceCMN.getCreditNoteRows(paramJobj, null);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = "accCreditNoteControllerCMN.getCreditNoteRows:" + ex.getMessage();
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

    public ModelAndView exportCreditNoteMerged(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        String view = "jsonView_ex";
        try {
            request.setAttribute("isExport", true);
            String type = request.getParameter("type")==null ?" ": request.getParameter("type") ;
            if (type.equals("detailedXls")) {
                DataJArr = accCreditNoteServiceCMN.exportCreditNoteWithDetails(request, response, DataJArr);
            } else {
                HashMap<String, Object> requestParams = accCreditNoteServiceCMN.getCreditNoteMap(request);
                boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
                String[] companyids = (consolidateFlag && request.getParameter("companyids") != null) ? request.getParameter("companyids").split(",") : sessionHandlerImpl.getCompanyid(request).split(",");
                String gcurrencyid = (consolidateFlag && request.getParameter(Constants.globalCurrencyKey) != null) ? request.getParameter(Constants.globalCurrencyKey) : sessionHandlerImpl.getCurrencyID(request);
                boolean eliminateflag = consolidateFlag;
                if (consolidateFlag) {
                    requestParams.put(Constants.start, "");
                    requestParams.put(Constants.limit, "");
                }
                if (requestParams.containsKey("ss") && !StringUtil.isNullOrEmpty((String) requestParams.get("ss"))) {
                    requestParams.put("ss", StringUtil.DecodeText(requestParams.get("ss").toString()));
                }
                ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences", sessionHandlerImpl.getCompanyid(request));
                if (extraPref != null && extraPref.isEnablesalespersonAgentFlow()) {
                    int permCode = sessionHandlerImpl.getPerms(request, Constants.CUSTOMER_PERMCODE);
                    if (!((permCode & Constants.CUSTOMER_VIEWALL_PERMCODE) == Constants.CUSTOMER_VIEWALL_PERMCODE)) {
                        /*
                     * when (permCode & Constants.CUSTOMER_VIEWALL_PERMCODE) == Constants.CUSTOMER_VIEWALL_PERMCODE is true then user has permission to view all customers documents,so at that time there is need to filter record according to user&salesperson. 
                         */
                        String userId = sessionHandlerImpl.getUserid(request);
                        requestParams.put("userid", userId);
                        requestParams.put("enablesalespersonagentflow", extraPref.isEnablesalespersonAgentFlow());
                    }
                }
                KwlReturnObject result = null;
                String companyid = "";
                for (int cnt = 0; cnt < companyids.length; cnt++) {
                    companyid = companyids[cnt];
                    request.setAttribute(Constants.companyKey, companyid);
                    request.setAttribute(Constants.globalCurrencyKey, gcurrencyid);
                    requestParams.put(Constants.companyKey, companyid);
                    requestParams.put(Constants.globalCurrencyKey, gcurrencyid);
                    int cntype = StringUtil.isNullOrEmpty(request.getParameter("cntype")) ? 1 : Integer.parseInt(request.getParameter("cntype"));
                    if (cntype == 1 || cntype == 4 || cntype == 12 || cntype == 13 || cntype == 5 || cntype == Constants.CreditNoteForOvercharge) {       //Other than opening CN
                        result = accCreditNoteDAOobj.getCreditNoteMerged(requestParams);
                        DataJArr = accCreditNoteService.getCreditNoteMergedJson(request, result.getEntityList(), DataJArr);
                    } else {
                        boolean isNoteForPayment = false;
                        boolean isVendor = false;
                        if (!StringUtil.isNullOrEmpty(request.getParameter("isNoteForPayment"))) {
                            isNoteForPayment = Boolean.parseBoolean(request.getParameter("isNoteForPayment"));
                            isVendor = Boolean.parseBoolean(request.getParameter("isVendor"));
                        }
                        requestParams.put("isNoteForPayment", isNoteForPayment);
                        if (cntype == 10 || (isNoteForPayment && !isVendor)) {  //Get Opening Balance for Customer
                            result = accCreditNoteDAOobj.getOpeningBalanceCNs(requestParams);
                            requestParams.put("cntype", 10);
                            accCreditNoteService.getOpeningCreditNotesJson(requestParams, result.getEntityList(), DataJArr);
                        } else if (cntype == 11 || (isNoteForPayment && isVendor)) {    //Get Opening Balance for Vendor
                            result = accCreditNoteDAOobj.getOpeningBalanceVendorCNs(requestParams);
                            requestParams.put("cntype", 11);
                            accCreditNoteService.getOpeningCreditNotesJson(requestParams, result.getEntityList(), DataJArr);
                        }
                    }
                }
            }
            jobj.put(Constants.RES_data, DataJArr);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                DataJArr = accCreditNoteServiceCMN.addTotalsForPrint(request,DataJArr);
                String startDate = request.getParameter("startdate");
                String endDate = request.getParameter("enddate");
                if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                    startDate = authHandler.getDateOnlyFormat().format(authHandler.getDateOnlyFormat().parse(startDate));
                    endDate = authHandler.getDateOnlyFormat().format(authHandler.getDateOnlyFormat().parse(endDate));
                    jobj.put("isFromToDateRequired", true);
                    jobj.put("stdate", startDate);
                    jobj.put("enddate", endDate);
                } else {
                    String GenerateDate = authHandler.getDateOnlyFormat().format(new Date());
                    jobj.put("GenerateDate", GenerateDate);
                }
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
            }
