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
package com.krawler.spring.accounting.invoice;

import com.krawler.accounting.integration.common.IntegrationCommonService;
import com.krawler.accounting.integration.common.IntegrationConstants;
import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import static com.krawler.common.util.Constants.Acc_Contract_Order_ModuleId;
import static com.krawler.common.util.Constants.Acc_Lease_Contract;
import com.krawler.common.util.CsvReader;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.*;
import com.krawler.inventory.model.stockmovement.StockMovement;
import com.krawler.inventory.model.stockmovement.StockMovementService;
import com.krawler.inventory.model.store.Store;
import com.krawler.hql.accounting.invoice.service.AccInvoiceModuleService;
import com.krawler.inventory.model.stock.StockService;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesCMN;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesConstants;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignHandler;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import com.krawler.spring.accounting.discount.accDiscountDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.journalentry.JournalEntryConstants;
import com.krawler.spring.accounting.journalentry.accJournalEntryController;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.multiLevelApprovalRule.AccMultiLevelApprovalDAO;
import com.krawler.spring.accounting.product.accProductController;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.productmodule.service.AccProductModuleService;
import com.krawler.spring.accounting.salesorder.accSalesOrderController;
import com.krawler.spring.accounting.salesorder.accSalesOrderDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.importFunctionality.ImportDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.permissionHandler.permissionHandlerDAO;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.esp.handlers.APICallHandlerService;
import static com.krawler.esp.web.resource.Links.loginpageFull;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.model.ist.InterStoreTransferRequest;
import com.krawler.hql.accounting.invoice.service.ImportInvoiceThread;
import com.krawler.inventory.exception.SeqFormatException;
import com.krawler.inventory.model.ist.InterStoreTransferService;
import java.io.*;
import java.util.*;
import javax.mail.MessagingException;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.ServletContext;
import org.hibernate.Session;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.sequence.SeqFormat;
import com.krawler.inventory.model.sequence.SeqService;
import com.krawler.inventory.model.stockmovement.StockMovementDetail;
import com.krawler.inventory.model.stockmovement.TransactionModule;
import com.krawler.inventory.model.stockmovement.TransactionType;
import com.krawler.inventory.model.stockout.ShippingDeliveryOrder;
import com.krawler.spring.accounting.product.TransactionBatch;
import com.krawler.spring.common.CommonFnControllerService;
import com.krawler.spring.common.fieldManagerDAO;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;

/**
 *
 * @author krawler
 */
public class accInvoiceController extends MultiActionController implements MessageSourceAware {

    private HibernateTransactionManager txnManager;
    private AccMultiLevelApprovalDAO accMultiLevelApprovalDAOObj;
    private authHandlerDAO authHandlerDAOObj;
    private accInvoiceDAO accInvoiceDAOobj;
    private accMasterItemsDAO accMasterItemsDAOobj;
    private accSalesOrderDAO accSalesOrderDAOObj;
    private accJournalEntryDAO accJournalEntryobj;
    private accProductDAO accProductObj;
    private accDiscountDAO accDiscountobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private fieldDataManager fieldDataManagercntrl;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accTaxDAO accTaxObj;
    private String successView;
    private MessageSource messageSource;
    private auditTrailDAO auditTrailObj;
    private accAccountDAO accAccountDAOobj;
    public ImportHandler importHandler;
    public accCustomerDAO accCustomerDAOObj;
    private ImportDAO importDao;
    private AccCommonTablesDAO accCommonTablesDAO;
    private AccProductModuleService accProductModuleService;
    private StockMovementService stockMovementService;
    private StockService stockService;
    private AccInvoiceModuleService accInvoiceModuleService;
    private permissionHandlerDAO permissionHandlerDAOObj;
    private APICallHandlerService apiCallHandlerService;
    private fieldManagerDAO fieldManagerDAOobj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private InterStoreTransferService istService;
    private IntegrationCommonService integrationCommonService;
    private CommonFnControllerService commonFnControllerService;
    private exportMPXDAOImpl exportDAO;
    private ImportInvoiceThread importInvoiceThreadobj;
    private SeqService seqService;
    String recId = "";
    String tranID = "";

    public void setimportInvoiceThread(ImportInvoiceThread importInvoiceThreadobj) {
        this.importInvoiceThreadobj = importInvoiceThreadobj;
    }
    
    public void setSeqService(SeqService seqService) {
        this.seqService = seqService;
    }

    
    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setIntegrationCommonService(IntegrationCommonService integrationCommonService) {
        this.integrationCommonService = integrationCommonService;
    }

    public void setStockMovementService(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    public void setStockService(StockService stockService) {
        this.stockService = stockService;
    }

    public void setaccMultiLevelApprovalDAOObj(AccMultiLevelApprovalDAO accMultiLevelApprovalDAO) {
        this.accMultiLevelApprovalDAOObj = accMultiLevelApprovalDAO;
    }

    public void setauthHandlerDAO(authHandlerDAO authHandlerDAOObj1) {
        this.authHandlerDAOObj = authHandlerDAOObj1;
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

    public void setpermissionHandlerDAO(permissionHandlerDAO permissionHandlerDAOObj1) {
        this.permissionHandlerDAOObj = permissionHandlerDAOObj1;
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

    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }

    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }

    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    public void setimportDAO(ImportDAO importDao) {
        this.importDao = importDao;
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

    public void setAccInvoiceModuleService(AccInvoiceModuleService accInvoiceModuleService) {
        this.accInvoiceModuleService = accInvoiceModuleService;
    }

    public void setAccProductModuleService(AccProductModuleService accProductModuleService) {
        this.accProductModuleService = accProductModuleService;
    }

    public void setaccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }

    public void setApiCallHandlerService(APICallHandlerService apiCallHandlerService) {
        this.apiCallHandlerService = apiCallHandlerService;
    }

    public void setFieldManagerDAO(fieldManagerDAO fieldManagerDAOobj) {
        this.fieldManagerDAOobj = fieldManagerDAOobj;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public void setIstService(InterStoreTransferService istService) {
        this.istService = istService;
    }

    public void setCommonFnControllerService(CommonFnControllerService commonFnControllerService) {
        this.commonFnControllerService = commonFnControllerService;
    }

    public void setExportDAO(exportMPXDAOImpl exportDAO) {
        this.exportDAO = exportDAO;
    }

    public synchronized KwlReturnObject updateJEEntryNumberForNewJE(HttpServletRequest request, JournalEntry JE, String companyid, String sequenceFormat, int approvedLevel) {
        String entryNumber = "";
        List list = new ArrayList();
        boolean successFlag = true;
        try {
            Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
            String nextJEAutoNo = null;
            boolean seqformat_oldflag = false;
            String nextAutoNo = "";
            String nextAutoNoInt = "";
            String datePrefix = "";
            String dateafterPrefix = "";
            String dateSuffix = "";
            String postingDateStr = request.getParameter("postingDate");
            DateFormat df = authHandler.getDateOnlyFormat();
            Date postingDate = null;
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("id", companyid);
            Object exPrefObject = kwlCommonTablesDAOObj.getRequestedObjectFields(ExtraCompanyPreferences.class, new String[]{"columnPref"}, paramMap);
            JSONObject jObj = StringUtil.isNullObject(exPrefObject) ? new JSONObject() : new JSONObject(exPrefObject.toString());
            boolean isPostingDateCheck = false;
            if (!StringUtil.isNullObject(jObj) && jObj.has(Constants.IS_POSTING_DATE_CHECK) && jObj.get(Constants.IS_POSTING_DATE_CHECK) != null && jObj.optBoolean(Constants.IS_POSTING_DATE_CHECK, false)) {
                isPostingDateCheck = true;
            }
            if (!StringUtil.isNullOrEmpty(postingDateStr)) {
                postingDate = df.parse(postingDateStr);
            }
            if (seqformat_oldflag) {
                nextJEAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_JOURNALENTRY, sequenceFormat);
            } else {
                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                if (postingDate != null && isPostingDateCheck) {
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, sequenceFormat, seqformat_oldflag, postingDate);
                } else {
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, sequenceFormat, seqformat_oldflag, JE.getEntryDate());
                }
                nextJEAutoNo = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                nextAutoNoInt = (String) seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                datePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                dateafterPrefix = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                dateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                jeDataMap.put(Constants.SEQFORMAT, sequenceFormat);
                jeDataMap.put(Constants.SEQNUMBER, nextAutoNoInt);
                jeDataMap.put(Constants.DATEPREFIX, datePrefix);
                jeDataMap.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
                jeDataMap.put(Constants.DATESUFFIX, dateSuffix);
                entryNumber = nextJEAutoNo;

            }
            if (isPostingDateCheck && postingDate != null) {
                jeDataMap.put("entrydate", postingDate);
            } else {
                jeDataMap.put("entrydate", JE.getEntryDate());
            }
            jeDataMap.put("companyid", companyid);
            jeDataMap.put("entrynumber", entryNumber);
            jeDataMap.put("jeid", JE.getID());
            jeDataMap.put("istemplate", JE.getIstemplate());
            jeDataMap.put("isReval", JE.getIsReval());
            jeDataMap.put("isDraft", JE.isDraft());
            jeDataMap.put("pendingapproval", approvedLevel);
            KwlReturnObject je1result = accJournalEntryobj.saveJournalEntry(jeDataMap);
            list.add(entryNumber);
        } catch (Exception e) {
            list.add(e.getMessage());
            successFlag = false;
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, e);
        }
        return new KwlReturnObject(successFlag, "JE entry number has been updated successfully", null, list, list.size());
    }

    public ModelAndView saveModuleUnit(HttpServletRequest request, HttpServletResponse response) {
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        boolean isAutoCreateDO = false;
        TransactionStatus status = null;//txnManager.getTransaction(def);
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String companyid = "";
        try {

            status = txnManager.getTransaction(def);
            companyid = sessionHandlerImpl.getCompanyid(request);
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("registrationtype", (!StringUtil.isNullOrEmpty(request.getParameter("registrationtype"))) ? request.getParameter("registrationtype") : "");
            requestParams.put("unitname", (!StringUtil.isNullOrEmpty(request.getParameter("unitname"))) ? request.getParameter("unitname") : "");
            requestParams.put("eccnumber", (!StringUtil.isNullOrEmpty(request.getParameter("eccnumber"))) ? request.getParameter("eccnumber") : "");
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cap.getEntityList().get(0);
            KwlReturnObject store = accountingHandlerDAOobj.getObject(Store.class.getName(), request.getParameter("warehouseid"));
            Store st = (Store) store.getEntityList().get(0);
            requestParams.put("companyid", company);
            requestParams.put("warehouseid", st);
            KwlReturnObject ExciseTempMap = accInvoiceDAOobj.saveExciseTemplateMapping(requestParams);
            txnManager.commit(status);
            issuccess = true;
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
            return new ModelAndView("jsonView", "model", jobj.toString());
        }
    }

    /*
     * accInvoiceController Function : saveOrUpdateModuleUnit. 
     * @params :Request. 
     * @params : Response. 
     * Use: Save/Update Excise Unit data record in
     * array format.
     */
    public ModelAndView saveOrUpdateModuleUnit(HttpServletRequest request, HttpServletResponse response) {
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        boolean isAutoCreateDO = false;
        TransactionStatus status = null;//txnManager.getTransaction(def);
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String companyid = "";
        try {

            status = txnManager.getTransaction(def);
            companyid = sessionHandlerImpl.getCompanyid(request);
            JSONArray jArr = new JSONArray(request.getParameter(Constants.RES_data));
            JSONArray jDelArr = new JSONArray(request.getParameter("deleteddata"));
            for (int i = 0; i < jDelArr.length(); i++) {
                try {
                    jobj = jDelArr.getJSONObject(i);

                    HashMap<String, Object> requestParams = new HashMap<String, Object>();

                    if (jobj.has("id") && jobj.get("id") != null) {
                        requestParams.put("id", jobj.getString("id"));
                    }
                    if (jobj.has("name") && jobj.get("name") != null) {
                        requestParams.put("unitname", StringUtil.DecodeText(jobj.optString("name")));
                    } else {
                        requestParams.put("unitname", "");
                    }

                    KwlReturnObject checkIdUsedInTranscation = accInvoiceDAOobj.checkIdUsedInTranscation(requestParams);
                    int count = checkIdUsedInTranscation.getRecordTotalCount();
                    if (count > 0) {
                        throw new AccountingException(messageSource.getMessage("acc.exciseUnit.excpDele", null, RequestContextUtils.getLocale(request)));
                    }
                    KwlReturnObject deleteExciseTempMap = accInvoiceDAOobj.deleteExciseTemplateMapping(requestParams);
                    msg = deleteExciseTempMap.getMsg();
                } catch (ServiceException ex) {
                    throw new AccountingException(messageSource.getMessage("acc.exciseUnit.excp", null, RequestContextUtils.getLocale(request)));
                }
            }

            for (int i = 0; i < jArr.length(); i++) {
                jobj = jArr.getJSONObject(i);
                if (jobj.getBoolean("modified") == false) {
                    continue;
                }
                HashMap<String, Object> requestParams = new HashMap<String, Object>();

                KwlReturnObject cap = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                Company company = (Company) cap.getEntityList().get(0);
                requestParams.put("companyid", company);

                if (jobj.has("id") && jobj.get("id") != null) {
                    requestParams.put("id", jobj.getString("id"));
                }
                if (jobj.has("name") && jobj.get("name") != null) {
                    requestParams.put("unitname", StringUtil.DecodeText(jobj.optString("name")));
                } else {
                    requestParams.put("unitname", "");
                }
                if (jobj.has("registrationType") && jobj.get("registrationType") != null) {
                    requestParams.put("registrationtype", jobj.getString("registrationType"));
                } else {
                    requestParams.put("registrationtype", "");
                }
                if (jobj.has("ECCNo") && jobj.get("ECCNo") != null) {
                    requestParams.put("eccnumber", jobj.getString("ECCNo"));
                } else {
                    requestParams.put("eccnumber", "");
                }
                if (jobj.has("warehouse") && jobj.get("warehouse") != null) {
                    KwlReturnObject store = accountingHandlerDAOobj.getObject(Store.class.getName(), jobj.getString("warehouse"));
                    Store st = (Store) store.getEntityList().get(0);
                    requestParams.put("warehouseid", st);
                }

                KwlReturnObject ExciseTempMap = accInvoiceDAOobj.saveExciseTemplateMapping(requestParams);
                msg = ExciseTempMap.getMsg();
            }
            txnManager.commit(status);
            issuccess = true;
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
            return new ModelAndView("jsonView", "model", jobj.toString());
        }
    }

    /*Save Invoice*/
    public ModelAndView saveInvoice(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String channelName = "";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            String url = this.getServletContext().getInitParameter(Constants.inventoryURL);
            paramJobj.put(Constants.inventoryURL, url);
            String baseUrl = URLUtil.getPageURL(request, loginpageFull);
            paramJobj.put("baseUrl", baseUrl);
            jobj = accInvoiceModuleService.saveInvoice(paramJobj);
            issuccess = jobj.optBoolean(Constants.RES_success, false);
            channelName = jobj.optString(Constants.channelName, null);
            jobj.put(Constants.userSessionId, (request.getSession(true)).getAttribute(Constants.userSessionId));// adding user session id to idenntify unique user session
            return new ModelAndView("jsonView", "model", jobj.toString());
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);

        } finally {
            if (issuccess && !StringUtil.isNullOrEmpty(channelName)) {
                ServerEventManager.publish(channelName, jobj.toString(), (ServletContext) this.getServletContext());
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView UpdateInvoiceFormDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "", channelName = "", invoiceid = "", deliveryOid = "", billNo = "", doinvflag = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        status = txnManager.getTransaction(def);
        try {
            boolean iscash = StringUtil.isNullOrEmpty(request.getParameter("incash")) ? false : Boolean.parseBoolean(request.getParameter("incash"));
            List li = updateInvoiceFormDetails(request);
            txnManager.commit(status);
            issuccess = true;
            /*
             * * To refresh a Invoice List.
             */
            channelName = "/CustomerInvoiceAndCashSalesReport/gridAutoRefresh";
            /*
             * * Composing the message to display after save operation.
             */
//            msg = (iscash ? messageSource.getMessage("acc.vendor.cashinv.update", null, RequestContextUtils.getLocale(request)) : messageSource.getMessage("acc.vendor.inv.update", null, RequestContextUtils.getLocale(request))) + " " + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + invoiceNumber + ".</b> " + (messageSource.getMessage("acc.field.JENo", null, RequestContextUtils.getLocale(request)) + ": <b>" + jeNumber + "</b>");
            /*
             * * Composing the message to insert into Audit Trail.
             */
            String action = "updated";
//            auditTrailObj.insertAuditLog(AuditAction.INVOICE_UPDATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + (" Customer Invoice ") + recId, request, tranID);
        } catch (SessionExpiredException | ServiceException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AccountingException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("invid", invoiceid);
                jobj.put("doid", deliveryOid);
                jobj.put("dono", billNo);
                jobj.put("doinvflag", doinvflag);
                if (issuccess && !StringUtil.isNullOrEmpty(channelName)) {
                    jobj.put(Constants.userSessionId, (request.getSession(true)).getAttribute(Constants.userSessionId));// adding user session id to idenntify unique user session
                    ServerEventManager.publish(channelName, jobj.toString(), (ServletContext) this.getServletContext());
                }
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public List updateInvoiceFormDetails(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException, UnsupportedEncodingException {
        String id = null;
        List ll = new ArrayList();
        JSONArray jArr = null;
        String invoiceid = null;
        Invoice invoice = null;
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            if (!StringUtil.isNullOrEmpty(request.getParameter("idsArray"))) {
                jArr = new JSONArray(request.getParameter("idsArray"));
                for (int i = 0; i < jArr.length(); i++) {
                    invoiceid = (String) jArr.get(i);
                    JSONObject invoicePrmt = new JSONObject();
                    if (!StringUtil.isNullOrEmpty(invoiceid)) {
                        invoicePrmt.put("invoiceid", invoiceid);
                    }
                    invoicePrmt.put("companyid", companyid);
                    invoicePrmt.put("FormSeriesNo", request.getParameter("FormSeriesNo") == null ? "" : request.getParameter("FormSeriesNo"));
                    invoicePrmt.put("FormNo", request.getParameter("FormNo") == null ? "" : request.getParameter("FormNo"));
                    invoicePrmt.put("FormDate", request.getParameter("FormDate") == null ? "" : df.parse(request.getParameter("FormDate")));
                    invoicePrmt.put("FormAmount", request.getParameter("FormAmount") == null ? 0 : Double.parseDouble(request.getParameter("FormAmount")));
                    invoicePrmt.put("FormStatus", "3");
//                    KwlReturnObject result = accInvoiceDAOobj.updateInvoice(invoicePrmt);
                    KwlReturnObject result = accInvoiceDAOobj.updateInvoice(invoicePrmt, new HashSet());
                    invoice = (Invoice) result.getEntityList().get(0);
                    id = invoice.getID();
                    String invoiceno = invoice.getInvoiceNumber();
                    ArrayList returnList = new ArrayList();
                    returnList.add(id);
                    returnList.add(invoiceno);
                    returnList.add(invoice.getJournalEntry().getEntryNumber());
                    ll.add(returnList);
                }
                ll.add("Submitted");
            }
        } catch (Exception e) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, e);
        }
        return ll;
    }

    public ModelAndView importOpeningBalanceInvoice(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
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
            requestParams.put("moduleName", "Opening Sales Invoice");
            requestParams.put("moduleid", Constants.Acc_opening_Sales_Invoice);
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
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }

            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView recoverBadDebtInvoices(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);

        try {
            String invoices = recoverBadDebtInvoices(request);

            issuccess = true;
            msg = "Invoices has been Recovered successfully" + invoices;//messageSource.getMessage("acc.agedPay.inv", null, RequestContextUtils.getLocale(request)) + " " + invoiceNumber + " " + messageSource.getMessage("acc.field.hasbeen", null, RequestContextUtils.getLocale(request)) + " " + succMsg + " " + messageSource.getMessage("acc.field.successfully", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (AccountingException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getAdjustmentTax(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONArray dataArray = getAdjustmentTax(request);
            jobj.put("data", dataArray);
            jobj.put("totalCount", dataArray.length());
            issuccess = true;

        } catch (JSONException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    private JSONArray getAdjustmentTax(HttpServletRequest request) throws SessionExpiredException {
        JSONArray jArr = new JSONArray();
        String companyId = sessionHandlerImpl.getCompanyid(request);

        DateFormat df = authHandler.getDateOnlyFormat(request);

        boolean isInputTax = Boolean.parseBoolean(request.getParameter("isInputTax"));

        try {

            String taxNames[] = isInputTax ? new String[]{Constants.MALASIAN_GST_AJP1_TAX_CODE, Constants.MALAYSIAN_GST_AJP_TAX_CODE} : new String[]{Constants.MALASIAN_GST_AJS1_TAX_CODE, Constants.MALAYSIAN_GST_AJS_TAX_CODE};

            for (String taxName : taxNames) {
                KwlReturnObject ObjReturnObject = accAccountDAOobj.getTaxFromCode(companyId, taxName);
                List<Tax> taxlist = ObjReturnObject.getEntityList();
                for (Tax tax : taxlist) {
//                    Tax tax = (Tax) ObjReturnObject.getEntityList().get(0);

                    KwlReturnObject taxObj = accTaxObj.getTaxPercent(companyId, new Date(), tax.getID());
                    double taxPer = (Double) taxObj.getEntityList().get(0);

                    JSONObject obj = new JSONObject();
                    obj.put("taxid", tax.getID());
                    obj.put("taxname", tax.getName());
                    obj.put("taxdescription", tax.getDescription());
                    obj.put("percent", taxPer);
                    obj.put("taxcode", tax.getTaxCode());
                    obj.put("accountid", tax.getAccount().getID());
                    obj.put("accountname", tax.getAccount().getName());
                    obj.put("taxtypeid", tax.getTaxtype());
                    obj.put("companyid", tax.getCompany().getCompanyID());
                    obj.put("taxTypeName", tax.getTaxtype() == 2 ? "Sales" : "Purchase");
                    //            obj.put(APPLYDATE, authHandler.getDateFormatter(request).format(row[2]));
                    jArr.put(obj);

                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView getTaxAdjustments(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONArray dataArray = getTaxAdjustments(request);
            jobj.put("data", dataArray);
            jobj.put("totalCount", dataArray.length());
            issuccess = true;

        } catch (JSONException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    private JSONArray getTaxAdjustments(HttpServletRequest request) throws SessionExpiredException {
        JSONArray jArr = new JSONArray();
        String companyId = sessionHandlerImpl.getCompanyid(request);

        DateFormat df = authHandler.getDateOnlyFormat(request);

        boolean isInputTax = Boolean.parseBoolean(request.getParameter("isInputTax"));

        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("companyid", companyId);
        requestParams.put("isInputTax", isInputTax);

        requestParams.put(Constants.ss, request.getParameter(Constants.ss));

        String start = "0";
        String limit = "30";

        if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.start)) && !StringUtil.isNullOrEmpty(request.getParameter(Constants.limit))) {
            start = request.getParameter(Constants.start);
            limit = request.getParameter(Constants.limit);
        }
        requestParams.put(Constants.start, start);
        requestParams.put(Constants.limit, limit);

        try {
            KwlReturnObject result = accInvoiceDAOobj.getTaxAdjustments(requestParams);

            List list = result.getEntityList();

            Iterator it = list.iterator();
            while (it.hasNext()) {
                TaxAdjustment taxAdjustment = (TaxAdjustment) it.next();
                JSONObject jobj = new JSONObject();

                jobj.put("documentId", taxAdjustment.getId());
                jobj.put("documentNo", taxAdjustment.getDocumentNo());
                jobj.put("documentDate", df.format(taxAdjustment.getCreationDate()));
                jobj.put("amount", taxAdjustment.getAmount());
                jobj.put("gstAmount", taxAdjustment.getTaxAmount());
                jobj.put("tax", (taxAdjustment.getTax()) != null ? taxAdjustment.getTax().getID() : "");
                jobj.put("taxName", (taxAdjustment.getTax()) != null ? taxAdjustment.getTax().getTaxCode() : "");
                jobj.put("reason", (taxAdjustment.getReason()) != null ? taxAdjustment.getReason().getID() : "");
                jobj.put("reasonName", (taxAdjustment.getReason()) != null ? taxAdjustment.getReason().getValue() : "");
                jArr.put(jobj);
            }

        } catch (JSONException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView deleteTaxAdjustment(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);

        try {
            String documentNo = deleteTaxAdjustment(request);

            issuccess = true;
            msg = "Document " + documentNo + " has been saved successfully";
            txnManager.commit(status);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (AccountingException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public String deleteTaxAdjustment(HttpServletRequest request) throws ParseException, SessionExpiredException, ServiceException, AccountingException {
        String documentNo = "";
        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);

            String documentId = request.getParameter("documentId");

            KwlReturnObject customerresult = accountingHandlerDAOobj.getObject(TaxAdjustment.class.getName(), documentId);
            TaxAdjustment taxAdjustment = (TaxAdjustment) customerresult.getEntityList().get(0);

            documentNo = taxAdjustment.getDocumentNo();

            HashMap<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("documentId", documentId);
            dataMap.put("companyId", companyId);

            KwlReturnObject result = accInvoiceDAOobj.deleteTaxAdjustment(dataMap);
            //delete old je in case of edit
            deleteJEArray(taxAdjustment.getJournalEntry().getID(), companyId);
        } catch (ServiceException ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return documentNo;
    }

    public ModelAndView saveTaxAdjustment(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);

        try {
            saveTaxAdjustment(request);

            issuccess = true;
            msg = "Information has been saved successfully";//messageSource.getMessage("acc.agedPay.inv", null, RequestContextUtils.getLocale(request)) + " " + invoiceNumber + " " + messageSource.getMessage("acc.field.hasbeen", null, RequestContextUtils.getLocale(request)) + " " + succMsg + " " + messageSource.getMessage("acc.field.successfully", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (AccountingException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void saveTaxAdjustment(HttpServletRequest request) throws ParseException, SessionExpiredException, ServiceException, AccountingException {
        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String documentNo = request.getParameter("documentNo");
            String documentId = request.getParameter("documentId");
            String billdateStr = request.getParameter("billdate");

            String currencyid = sessionHandlerImpl.getCurrencyID(request);

            String oldjeid = "";
            String jeid = "";
            String jeentryNumber = "";
            boolean jeautogenflag = false;
            String jeIntegerPart = "";
            String jeDatePrefix = "";
            String jeDateAfterPrefix = "";
            String jeDateSuffix = "";
            String jeSeqFormatId = "";

            HashSet<JournalEntryDetail> jedetails = new HashSet();
            JournalEntry journalEntry = null;
            Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
            KwlReturnObject jeresult = null;

            JournalEntry jetemp = null;

            boolean isEdit = false;

            DateFormat df = authHandler.getDateOnlyFormat(request);

            Date billDate = null;

            if (!StringUtil.isNullOrEmpty(billdateStr)) {
                billDate = df.parse(billdateStr);
            }

            double amount = Double.parseDouble(request.getParameter("transactionAmount"));
            double gstAmount = Double.parseDouble(request.getParameter("gstAmount"));
            String taxId = request.getParameter("tax");
            String reason = request.getParameter("reason");
            boolean isInputAdjustment = Boolean.parseBoolean(request.getParameter("isInputAdjustment"));

            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;

            String inputTaxAdjustmentAccountId = extraCompanyPreferences.getInputTaxAdjustmentAccount();
            String outputTaxAdjustmentAccountId = extraCompanyPreferences.getOutputTaxAdjustmentAccount();

            String taxAdjustmentAccountId = "";

            if (isInputAdjustment) {
                taxAdjustmentAccountId = inputTaxAdjustmentAccountId;
            } else {
                taxAdjustmentAccountId = outputTaxAdjustmentAccountId;
            }

            HashMap<String, Object> dataMap = new HashMap<String, Object>();

            dataMap.put("companyId", companyId);
            dataMap.put("documentNo", documentNo);
            dataMap.put("billDate", billDate);
            dataMap.put("amount", amount);
            dataMap.put("gstAmount", gstAmount);
            dataMap.put("taxId", taxId);
            dataMap.put("reason", reason);
            dataMap.put("isInputAdjustment", isInputAdjustment);

            KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), taxId); // (Tax)session.get(Tax.class, taxid);
            Tax tax = (Tax) txresult.getEntityList().get(0);
            if (tax == null) {
                throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
            }

            if (!StringUtil.isNullOrEmpty(documentId)) {

                isEdit = true;
                dataMap.put("documentId", documentId);
                KwlReturnObject customerresult = accountingHandlerDAOobj.getObject(TaxAdjustment.class.getName(), documentId);
                TaxAdjustment taxAdjustment = (TaxAdjustment) customerresult.getEntityList().get(0);

                oldjeid = taxAdjustment.getJournalEntry().getID();
                jetemp = taxAdjustment.getJournalEntry();

                if (jetemp != null) {  //in edit case get all the detail
                    jeentryNumber = jetemp.getEntryNumber(); //preserving these data to generate same JE number in edit case                    
                    jeautogenflag = jetemp.isAutoGenerated();
                    jeSeqFormatId = jetemp.getSeqformat() == null ? "" : jetemp.getSeqformat().getID();
                    jeIntegerPart = String.valueOf(jetemp.getSeqnumber());
                }

                //Delete old entries and insert new entries again from optimized table in edit case.
                accJournalEntryobj.deleteOnEditAccountJEs_optimized(oldjeid);
                deleteJEDetailsCustomData(oldjeid);

            } else {
                // Check whether same named Transaction exist or not

                HashMap<String, Object> recMap = new HashMap<String, Object>();
                recMap.put("isInputTax", isInputAdjustment);
                recMap.put("documentNo", documentNo);
                recMap.put("companyid", companyId);
                KwlReturnObject result = accInvoiceDAOobj.getTaxAdjustments(recMap);

                if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                    throw new AccountingException("Tax Adjustment Document Number : " + documentNo + " already exist");
                }

            }

            if (StringUtil.isNullOrEmpty(oldjeid)) {  //in create new case 
                synchronized (this) {
                    HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                    JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                    JEFormatParams.put("modulename", "autojournalentry");
                    JEFormatParams.put("companyid", companyId);
                    JEFormatParams.put("isdefaultFormat", true);

                    KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                    SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyId, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, billDate);
                    jeentryNumber = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    jeIntegerPart = (String) seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                    jeDatePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    jeDateAfterPrefix = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                    jeDateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
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
            jeDataMap.put("entrydate", billDate);
            jeDataMap.put("companyid", companyId);
            jeDataMap.put("isTaxAdjustmentJE", true);
            jeDataMap.put("memo", (isInputAdjustment) ? "Input Tax Adjustment" : "Output Tax Adjustment");
            jeDataMap.put("currencyid", currencyid);

            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            jeid = journalEntry.getID();
            jeDataMap.put("jeid", jeid);

            JSONObject jedjson = new JSONObject();
            jedjson.put("srno", jedetails.size() + 1);
            jedjson.put("companyid", companyId);
            jedjson.put("amount", gstAmount);
            jedjson.put("accountid", taxAdjustmentAccountId);
            jedjson.put("debit", isInputAdjustment);
            jedjson.put("jeid", jeid);
            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetails.add(jed);

            jedjson = new JSONObject();
            jedjson.put("srno", jedetails.size() + 1);
            jedjson.put("companyid", companyId);
            jedjson.put("amount", gstAmount);
            jedjson.put("accountid", tax.getAccount().getID());
            jedjson.put("debit", !isInputAdjustment);
            jedjson.put("jeid", jeid);
            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetails.add(jed);

            jeDataMap.put("jedetails", jedetails);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details

            KwlReturnObject result = null;

            dataMap.put("journalEntryId", jeid);

            if (isEdit) {
                result = accInvoiceDAOobj.updateTaxAdjustment(dataMap);
            } else {
                result = accInvoiceDAOobj.saveTaxAdjustment(dataMap);
            }
            //delete old je in case of edit
            deleteJEArray(oldjeid, companyId);
        } catch (JSONException ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }

    }

    public ModelAndView claimBadDebtInvoices(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";

        try {
            claimBadDebtInvoices(request);

            issuccess = true;
            msg = messageSource.getMessage("acc.malaysiangst.invoiceIsClaimed", null, RequestContextUtils.getLocale(request));
        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void claimBadDebtInvoices(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException {
        TransactionStatus status = null;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String claimedDateStr = request.getParameter("claimedDate");
            String claimedPeriodStr = request.getParameter("claimedPeriod");
            int claimedPeriod = 0;

            if (!StringUtil.isNullOrEmpty(claimedPeriodStr)) {
                claimedPeriod = Integer.parseInt(claimedPeriodStr);
            }

            Date claimedDate = authHandler.getDateOnlyFormat(request).parse(claimedDateStr);

            JSONArray jArr = new JSONArray(request.getParameter("invoiceData"));

            ExtraCompanyPreferences extraCompanyPreferences = null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;

            String badDebtReleifAccountId = extraCompanyPreferences.getGstBadDebtsReleifAccount();

            KwlReturnObject accObj = accountingHandlerDAOobj.getObject(Account.class.getName(), badDebtReleifAccountId);
            Account account = (Account) accObj.getEntityList().get(0);
            if (account == null) {
                throw new AccountingException("GST Bad Debt Relief Account is not available in database");
            }

            String badDebtSuspenseAccountId = extraCompanyPreferences.getGstBadDebtsSuspenseAccount();
            accObj = accountingHandlerDAOobj.getObject(Account.class.getName(), badDebtSuspenseAccountId);
            account = (Account) accObj.getEntityList().get(0);
            if (account == null) {
                throw new AccountingException("GST Bad Debt Suspense Account is not available in database");
            }

            String badDebtRecoveredAccountId = extraCompanyPreferences.getGstBadDebtsRecoverAccount();
            accObj = accountingHandlerDAOobj.getObject(Account.class.getName(), badDebtRecoveredAccountId);
            account = (Account) accObj.getEntityList().get(0);
            if (account == null) {
                throw new AccountingException("GST Bad Debt Recover Account is not available in database");
            }

            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("IC_Tx");
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
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
            for (int i = 0; i < jArr.length(); i++) {

                status = txnManager.getTransaction(def);
                JSONObject jobj = jArr.getJSONObject(i);

                String invoiceId = jobj.getString("billId");
                double invoiceReceivedAmt = jobj.optDouble("paidAmtAfterClaimed", 0);
                double gstToRecover = jobj.optDouble("gstToRecover", 0);// Will come in base currency

                KwlReturnObject invObj = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceId);
                Invoice invoice = (Invoice) invObj.getEntityList().get(0);
                boolean isOpeningBalanceInvoice = invoice.isIsOpeningBalenceInvoice();
                // Calculating gstToRecover in invoice currency

                Map<String, Object> requestParams = AccountingManager.getGlobalParams(request);
                if (isOpeningBalanceInvoice) {
                    gstToRecover = (invoice.isConversionRateFromCurrencyToBase()) ? (gstToRecover / (invoice.getExchangeRateForOpeningTransaction())) : (gstToRecover * (invoice.getExchangeRateForOpeningTransaction()));
                } else {
                    String fromcurrencyid = invoice.getCurrency().getCurrencyID();
//                    KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, gstToRecover, fromcurrencyid, invoice.getJournalEntry().getEntryDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                    KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, gstToRecover, fromcurrencyid, invoice.getCreationDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                    gstToRecover = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                }
                HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                JEFormatParams.put("modulename", "autojournalentry");
                JEFormatParams.put("companyid", companyid);
                JEFormatParams.put("isdefaultFormat", true);

                KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, claimedDate);
                String jeentryNumber = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                String jeIntegerPart = (String) seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                String jeDatePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                String jeDateAfterPrefix = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                String jeDateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                String jeSeqFormatId = format.getID();
                boolean jeautogenflag = true;

                HashMap<String, Object> BadDebtFormatParams = new HashMap<String, Object>();
                BadDebtFormatParams.put("moduleid", Constants.SALES_BAD_DEBT_CLAIM_ModuleId);
                BadDebtFormatParams.put("modulename", "autosalesbaddebtclaimid");
                BadDebtFormatParams.put("companyid", companyid);
                BadDebtFormatParams.put("isdefaultFormat", true);
                KwlReturnObject kwlbaddebtObj = accCompanyPreferencesObj.getSequenceFormat(BadDebtFormatParams);
                if (kwlbaddebtObj.getEntityList().size() == 0) {
                    throw new AccountingException("Sequence Format For Sales Bad Debt Claim is not Set ");
                }
                SequenceFormat baddebtformat = (SequenceFormat) kwlbaddebtObj.getEntityList().get(0);
                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_BADDEBTINVOICECLAIM, baddebtformat.getID(), false, claimedDate);
                String baddebtentryNumber = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                int baddebtIntegerPart = Integer.parseInt((String) seqNumberMap.get(Constants.SEQNUMBER));
                String datePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                String dateafterPrefix = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                String dateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                String baddebtSeqFormatId = baddebtformat.getID();

                Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
                jeDataMap.put("entrynumber", jeentryNumber);
                jeDataMap.put("autogenerated", jeautogenflag);
                jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                jeDataMap.put(Constants.SEQNUMBER, jeIntegerPart);
                jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
                jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
                jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
                jeDataMap.put("entrydate", claimedDate);
                jeDataMap.put("isBadDebtJE", true);
                jeDataMap.put("companyid", companyid);
                jeDataMap.put("memo", "Bad Debt Relief For " + (isOpeningBalanceInvoice ? "Opening " : "") + "Sales Tax Invoice " + invoice.getInvoiceNumber());
                jeDataMap.put("currencyid", invoice.getCurrency().getCurrencyID());
                jeDataMap.put("baddebtentryNumber", baddebtentryNumber);
                if (isOpeningBalanceInvoice) {
                    jeDataMap.put("externalCurrencyRate", invoice.isConversionRateFromCurrencyToBase() ? (1 / invoice.getExchangeRateForOpeningTransaction()) : invoice.getExchangeRateForOpeningTransaction());
                }
                KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
                JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
                String jeid = journalEntry.getID();

                Set<JournalEntryDetail> jeDetails = new HashSet<JournalEntryDetail>();

                Set<InvoiceDetail> invoiceDetails = invoice.getRows();

                double taxAmount = 0d;
                double invoiceTotalAmount = 0.0;
                double invoiceAmountExcludingTax = 0.0;
                double invoiceAmtDue = isOpeningBalanceInvoice ? invoice.getOpeningBalanceAmountDue() : invoice.getInvoiceamountdue();
                boolean isGlobalLevelTax = false;
                if (isOpeningBalanceInvoice) {
                    isGlobalLevelTax = true;
                    invoiceTotalAmount = invoice.getOriginalOpeningBalanceAmount();
                    taxAmount += ((invoice.getTaxamount()) * invoiceAmtDue) / invoiceTotalAmount;
                } else {
                    invoiceTotalAmount = invoice.getCustomerEntry().getAmount();
                    if (invoice.getTaxEntry() != null && invoice.getTaxEntry().getAmount() > 0) {
                        isGlobalLevelTax = true;
                    }
                    if (isGlobalLevelTax) {
                        taxAmount += ((invoice.getTaxEntry().getAmount() * invoiceAmtDue) / invoiceTotalAmount);
                    } else {
                        for (InvoiceDetail detail : invoiceDetails) {
                            taxAmount += (detail.getRowTaxAmount() * invoiceAmtDue) / invoiceTotalAmount;
                        }
                    }
                }
                invoiceTotalAmount = authHandler.round(invoiceTotalAmount, companyid);
                taxAmount = authHandler.round(taxAmount, companyid);
                invoiceAmountExcludingTax = invoiceAmtDue - taxAmount;
                invoiceAmountExcludingTax = authHandler.round(invoiceAmountExcludingTax, companyid);

                // Debit to Bad debt claim account
                JSONObject jedjson = new JSONObject();
                jedjson.put("srno", jeDetails.size() + 1);
                jedjson.put("companyid", companyid);
                jedjson.put("amount", invoiceAmountExcludingTax);
                jedjson.put("accountid", badDebtReleifAccountId);
                jedjson.put("debit", true);
                jedjson.put("jeid", jeid);
                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jeDetails.add(jed);

                // Credit to invoice account
                jedjson = new JSONObject();
                jedjson.put("srno", jeDetails.size() + 1);
                jedjson.put("companyid", companyid);
                jedjson.put("amount", invoiceAmtDue);
                jedjson.put("accountid", (invoice.getAccount() != null) ? invoice.getAccount().getID() : invoice.getCustomer().getID());
                jedjson.put("debit", false);
                jedjson.put("jeid", jeid);
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jeDetails.add(jed);

                // Debit to tax account
                if (isGlobalLevelTax) {

                    String accountIdForTax = "";
                    if (isOpeningBalanceInvoice) {
                        accountIdForTax = gstOutputAccountId;
                    } else {
                        accountIdForTax = invoice.getTax().getAccount().getID();
                    }
                    jedjson = new JSONObject();
                    jedjson.put("srno", jeDetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", taxAmount);
                    jedjson.put("accountid", accountIdForTax);
                    jedjson.put("debit", true);
                    jedjson.put("jeid", jeid);
                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jeDetails.add(jed);
                } else {
                    Tax taxObj = new Tax();
                    for (InvoiceDetail detail : invoiceDetails) {
                        taxObj = detail.getTax();
                        if (taxObj != null) {
                            jedjson = new JSONObject();
                            jedjson.put("srno", jeDetails.size() + 1);
                            jedjson.put("companyid", companyid);
                            jedjson.put("amount", authHandler.round((detail.getRowTaxAmount() * invoiceAmtDue) / invoiceTotalAmount, companyid));
                            jedjson.put("accountid", taxObj.getAccount().getID());
                            jedjson.put("debit", true);
                            jedjson.put("jeid", jeid);
                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jeDetails.add(jed);
                        }
                    }
                }

                JSONObject invjson = new JSONObject();
                invjson.put("invoiceid", invoiceId);
                invjson.put("companyid", companyid);
                invjson.put("claimedDate", claimedDate);
                invjson.put("claimedPeriod", claimedPeriod);
                invjson.put("badDebtType", 1);
                invjson.put("claimamountdue", invoiceAmtDue);
                invjson.put("amountduedate", claimedDate);
                if (isOpeningBalanceInvoice) {
                    invjson.put("openingBalanceAmountDue", 0.0);
                    invjson.put(Constants.openingBalanceBaseAmountDue, 0.0);
                } else {
                    invjson.put(Constants.invoiceamountdue, 0.0);
                    invjson.put(Constants.invoiceamountdueinbase, 0.0);
                }
                KwlReturnObject result = accInvoiceDAOobj.updateInvoice(invjson, new HashSet());

//                for (InvoiceDetail detail : invoiceDetails) {
//                    if (detail.getTax() != null && detail.getRowTaxAmount() > 0) {
//                        
//                        String taxId = detail.getTax().getID();
//                        
//                        KwlReturnObject taxObj =  accTaxObj.getTaxPercent(companyid, invoice.getJournalEntry().getEntryDate(), taxId);
//                            
//                        double taxPer = (Double) taxObj.getEntityList().get(0);
//                        
//                        
////                        double gstToRecover = invoiceReceivedAmt*taxPer/(100+taxPer);
//                        
//                        JSONObject jedjson = new JSONObject();
//                        jedjson.put("srno", jeDetails.size() + 1);
//                        jedjson.put("companyid", companyid);
//                        jedjson.put("amount", gstToRecover);//detail.getRowTaxAmount()-gstToRecover);
//                        jedjson.put("accountid", detail.getTax().getAccount().getID());
//                        jedjson.put("debit", true);
//                        jedjson.put("jeid", jeid);
//                        KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
//                        JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
//                        jeDetails.add(jed);
//
//
//                        jedjson = new JSONObject();
//                        jedjson.put("srno", jeDetails.size() + 1);
//                        jedjson.put("companyid", companyid);
//                        jedjson.put("amount", gstToRecover);//detail.getRowTaxAmount()-gstToRecover);
//                        jedjson.put("accountid", badDebtReleifAccountId);
//                        jedjson.put("debit", false);
//                        jedjson.put("jeid", jeid);
//                        jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
//                        jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
//                        jeDetails.add(jed);
//
//                        taxAmount += gstToRecover;//(detail.getRowTaxAmount()-gstToRecover);
//                    }
//                }
//
//                if (invoice.getTaxEntry() != null && invoice.getTaxEntry().getAmount() > 0) {
//                    
//                    String taxId = invoice.getTax().getID();
//
//                    KwlReturnObject taxObj = accTaxObj.getTaxPercent(companyid, invoice.getJournalEntry().getEntryDate(), taxId);
//
//                    double taxPer = (Double) taxObj.getEntityList().get(0);
//
//
////                    double gstToRecover = invoiceReceivedAmt * taxPer / (100 + taxPer);
//                    
//                    
//                    
//                    JSONObject jedjson = new JSONObject();
//                    jedjson.put("srno", jeDetails.size() + 1);
//                    jedjson.put("companyid", companyid);
//                    jedjson.put("amount", gstToRecover);//invoice.getTaxEntry().getAmount()-gstToRecover);
//                    jedjson.put("accountid", invoice.getTaxEntry().getAccount().getID());
//                    jedjson.put("debit", true);
//                    jedjson.put("jeid", jeid);
//                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
//                    JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
//                    jeDetails.add(jed);
//
//
//                    jedjson = new JSONObject();
//                    jedjson.put("srno", jeDetails.size() + 1);
//                    jedjson.put("companyid", companyid);
//                    jedjson.put("amount", gstToRecover);
//                    jedjson.put("accountid", badDebtReleifAccountId);
//                    jedjson.put("debit", false);
//                    jedjson.put("jeid", jeid);
//                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
//                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
//                    jeDetails.add(jed);
//
//                    taxAmount += gstToRecover;//(invoice.getTaxEntry().getAmount()-gstToRecover);
//                }
//
//                JournalEntryDetail centry = invoice.getCustomerEntry();
//                double invoiceAmt = centry.getAmount();
//                
//                double invoiceAmtDue = invoice.getInvoiceamountdue();
//                
//                // Convert Invoice Amount Due in Base Currency
//                
////                Map<String, Object> requestParams = AccountingManager.getGlobalParams(request);
////                
////                String fromcurrencyid = invoice.getCurrency().getCurrencyID();                         
////                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, invoiceAmtDue, fromcurrencyid, invoice.getJournalEntry().getEntryDate(), invoice.getJournalEntry().getExternalCurrencyRate());
////                invoiceAmtDue=authHandler.round((Double) bAmt.getEntityList().get(0),3);
//
//                double invoiceAmtExcludingTax = invoiceAmtDue - taxAmount;
//
//                JSONObject jedjson = new JSONObject();
//                jedjson.put("srno", jeDetails.size() + 1);
//                jedjson.put("companyid", companyid);
//                jedjson.put("amount", invoiceAmtExcludingTax);
//                jedjson.put("accountid", badDebtSuspenseAccountId);
//                jedjson.put("debit", true);
//                jedjson.put("jeid", jeid);
//                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
//                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
//                jeDetails.add(jed);
//
//
//                jedjson = new JSONObject();
//                jedjson.put("srno", jeDetails.size() + 1);
//                jedjson.put("companyid", companyid);
//                jedjson.put("amount", invoiceAmtExcludingTax);
//                jedjson.put("accountid", badDebtSuspenseAccountId);
//                jedjson.put("debit", false);
//                jedjson.put("jeid", jeid);
//                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
//                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
//                jeDetails.add(jed);
                // Save Mapping
                HashMap<String, Object> mappingObj = new HashMap<String, Object>();
                mappingObj.put("companyId", companyid);
                mappingObj.put("invoiceId", invoiceId);
                mappingObj.put("journalEntryId", jeid);
                mappingObj.put("badDebtAmtClaimed", invoiceAmtDue);
                mappingObj.put("badDebtGSTAmtClaimed", gstToRecover);
                mappingObj.put("claimedDate", claimedDate);
                mappingObj.put("badDebtType", 0);
                mappingObj.put("autoGenerated", true);
                mappingObj.put("seqformat", baddebtSeqFormatId);
                mappingObj.put("seqnumber", baddebtIntegerPart);
                mappingObj.put("baddebtentryNumber", baddebtentryNumber);
                mappingObj.put(Constants.DATEPREFIX, datePrefix);
                mappingObj.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
                mappingObj.put(Constants.DATESUFFIX, dateSuffix);
                KwlReturnObject mapResult = accInvoiceDAOobj.saveBadDebtInvoiceMapping(mappingObj);
                txnManager.commit(status);
            }
        } catch (ParseException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ServiceException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    public String recoverBadDebtInvoices(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException {
        String invoiceno = "";
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String invliceIds = request.getParameter("billids");
            String recoveredDateStr = request.getParameter("claimedDate");
            String claimedPeriodStr = request.getParameter("claimedPeriod");
            int claimedPeriod = 0;

            if (!StringUtil.isNullOrEmpty(claimedPeriodStr)) {
                claimedPeriod = Integer.parseInt(claimedPeriodStr);
            }

            Date recoveredDate = authHandler.getDateOnlyFormat(request).parse(recoveredDateStr);

//            String[] invoiceIdsArray = invliceIds.split(",");
            JSONArray jArr = new JSONArray(request.getParameter("invoiceData"));

            ExtraCompanyPreferences extraCompanyPreferences = null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;

            String badDebtReleifAccountId = extraCompanyPreferences.getGstBadDebtsReleifAccount();

            KwlReturnObject accObj = accountingHandlerDAOobj.getObject(Account.class.getName(), badDebtReleifAccountId);
            Account account = (Account) accObj.getEntityList().get(0);
            if (account == null) {
                throw new AccountingException("GST Bad Debt Relief Account is not available in database");
            }

            String badDebtSuspenseAccountId = extraCompanyPreferences.getGstBadDebtsSuspenseAccount();
            accObj = accountingHandlerDAOobj.getObject(Account.class.getName(), badDebtSuspenseAccountId);
            account = (Account) accObj.getEntityList().get(0);
            if (account == null) {
                throw new AccountingException("GST Bad Debt Suspense Account is not available in database");
            }

            String badDebtRecoveredAccountId = extraCompanyPreferences.getGstBadDebtsRecoverAccount();
            accObj = accountingHandlerDAOobj.getObject(Account.class.getName(), badDebtRecoveredAccountId);
            account = (Account) accObj.getEntityList().get(0);
            if (account == null) {
                throw new AccountingException("GST Bad Debt Recover Account is not available in database");
            }

            for (int i = 0; i < jArr.length(); i++) {

                JSONObject jobj = jArr.getJSONObject(i);

                String invoiceId = jobj.getString("billId");
                double invoiceReceivedAmt = jobj.optDouble("paidAmtAfterClaimed", 0);
                double gstToRecover = jobj.optDouble("gstToRecover", 0);

                KwlReturnObject invObj = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceId);
                Invoice invoice = (Invoice) invObj.getEntityList().get(0);
                if (invoiceReceivedAmt == 0 || gstToRecover == 0) {// if payment is not received for selected invoice then no need to run recovery process
                    invoiceno += invoice.getInvoiceNumber() + ",";
                    continue;
                }

                // Calculating gstToRecover in invoice currency
                Map<String, Object> requestParams = AccountingManager.getGlobalParams(request);

                String fromcurrencyid = invoice.getCurrency().getCurrencyID();
//                KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, gstToRecover, fromcurrencyid, invoice.getJournalEntry().getEntryDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, gstToRecover, fromcurrencyid, invoice.getCreationDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                gstToRecover = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);

//                bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, invoiceReceivedAmt, fromcurrencyid, invoice.getJournalEntry().getEntryDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, invoiceReceivedAmt, fromcurrencyid, invoice.getCreationDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                invoiceReceivedAmt = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);

                JSONObject invjson = new JSONObject();
                invjson.put("invoiceid", invoiceId);
                invjson.put("companyid", companyid);
//                invjson.put("recoveredDate", recoveredDate);
                invjson.put("badDebtType", 2);

                KwlReturnObject result = accInvoiceDAOobj.updateInvoice(invjson, new HashSet());

                HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                JEFormatParams.put("modulename", "autojournalentry");
                JEFormatParams.put("companyid", companyid);
                JEFormatParams.put("isdefaultFormat", true);

                KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, recoveredDate);
                String jeentryNumber = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                String jeIntegerPart = (String) seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                String jeDatePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                String jeDateAfterPrefix = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                String jeDateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                String jeSeqFormatId = format.getID();
                boolean jeautogenflag = true;

                HashMap<String, Object> BadDebtFormatParams = new HashMap<String, Object>();
                BadDebtFormatParams.put("moduleid", Constants.SALES_BAD_DEBT_RECOVER_ModuleId);
                BadDebtFormatParams.put("modulename", "autosalesbaddebtrecoverid");
                BadDebtFormatParams.put("companyid", companyid);
                BadDebtFormatParams.put("isdefaultFormat", true);
                KwlReturnObject kwlbaddebtObj = accCompanyPreferencesObj.getSequenceFormat(BadDebtFormatParams);
                if (kwlbaddebtObj.getEntityList().size() == 0) {
                    throw new AccountingException("Sequence Format For Sales Bad Debt Recover is not Set ");
                }
                SequenceFormat baddebtformat = (SequenceFormat) kwlbaddebtObj.getEntityList().get(0);
                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_BADDEBTINVOICERECOVER, baddebtformat.getID(), false, recoveredDate);
                String baddebtentryNumber = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                int baddebtIntegerPart = Integer.parseInt((String) seqNumberMap.get(Constants.SEQNUMBER));
                String datePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                String dateafterPrefix = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                String dateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                String baddebtSeqFormatId = baddebtformat.getID();

                Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
                jeDataMap.put("entrynumber", jeentryNumber);
                jeDataMap.put("autogenerated", jeautogenflag);
                jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                jeDataMap.put(Constants.SEQNUMBER, jeIntegerPart);
                jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
                jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
                jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
                jeDataMap.put("entrydate", recoveredDate);
                jeDataMap.put("isBadDebtJE", true);
                jeDataMap.put("companyid", companyid);
                jeDataMap.put("baddebtentryNumber", baddebtentryNumber);
                jeDataMap.put("memo", "Bad Debt Recovered For Tax Invoice " + invoice.getInvoiceNumber());
                jeDataMap.put("currencyid", invoice.getCurrency().getCurrencyID());
                KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
                JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
                String jeid = journalEntry.getID();

                Set<JournalEntryDetail> jeDetails = new HashSet<JournalEntryDetail>();

                Set<InvoiceDetail> invoiceDetails = invoice.getRows();

                double taxAmount = 0d;

                for (InvoiceDetail detail : invoiceDetails) {
                    if (detail.getTax() != null && detail.getRowTaxAmount() > 0) {

                        String taxId = detail.getTax().getID();

//                        KwlReturnObject taxObj =  accTaxObj.getTaxPercent(companyid, invoice.getJournalEntry().getEntryDate(), taxId);
                        KwlReturnObject taxObj = accTaxObj.getTaxPercent(companyid, invoice.getCreationDate(), taxId);

                        double taxPer = (Double) taxObj.getEntityList().get(0);

//                        double gstToRecover = invoiceReceivedAmt*taxPer/(100+taxPer);
                        JSONObject jedjson = new JSONObject();
                        jedjson.put("srno", jeDetails.size() + 1);
                        jedjson.put("companyid", companyid);
                        jedjson.put("amount", gstToRecover);//detail.getRowTaxAmount()-gstToRecover);
                        jedjson.put("accountid", detail.getTax().getAccount().getID());
                        jedjson.put("debit", false);
                        jedjson.put("jeid", jeid);
                        KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jeDetails.add(jed);

                        jedjson = new JSONObject();
                        jedjson.put("srno", jeDetails.size() + 1);
                        jedjson.put("companyid", companyid);
                        jedjson.put("amount", gstToRecover);//detail.getRowTaxAmount()-gstToRecover);
                        jedjson.put("accountid", badDebtRecoveredAccountId);
                        jedjson.put("debit", true);
                        jedjson.put("jeid", jeid);
                        jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jeDetails.add(jed);

                        taxAmount += gstToRecover;//(detail.getRowTaxAmount()-gstToRecover);
                    }
                }

                if (invoice.getTaxEntry() != null && invoice.getTaxEntry().getAmount() > 0) {

                    String taxId = invoice.getTax().getID();

//                    KwlReturnObject taxObj =  accTaxObj.getTaxPercent(companyid, invoice.getJournalEntry().getEntryDate(), taxId);
                    KwlReturnObject taxObj = accTaxObj.getTaxPercent(companyid, invoice.getCreationDate(), taxId);

                    double taxPer = (Double) taxObj.getEntityList().get(0);

//                    double gstToRecover = invoiceReceivedAmt*taxPer/(100+taxPer);
                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", jeDetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", gstToRecover);//invoice.getTaxEntry().getAmount()-gstToRecover);
                    jedjson.put("accountid", invoice.getTaxEntry().getAccount().getID());
                    jedjson.put("debit", false);
                    jedjson.put("jeid", jeid);
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jeDetails.add(jed);

                    jedjson = new JSONObject();
                    jedjson.put("srno", jeDetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", gstToRecover);//invoice.getTaxEntry().getAmount()-gstToRecover);
                    jedjson.put("accountid", badDebtRecoveredAccountId);
                    jedjson.put("debit", true);
                    jedjson.put("jeid", jeid);
                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jeDetails.add(jed);

                    taxAmount += (gstToRecover);//(invoice.getTaxEntry().getAmount()-gstToRecover);
                }

                double invoiceAmtDue = invoice.getInvoiceamountdue();

                double invoiceReceivedAmtExcludingTax = invoiceReceivedAmt - taxAmount;//invoiceAmtDue - taxAmount;

                JSONObject jedjson = new JSONObject();
                jedjson.put("srno", jeDetails.size() + 1);
                jedjson.put("companyid", companyid);
                jedjson.put("amount", invoiceReceivedAmtExcludingTax);
                jedjson.put("accountid", badDebtSuspenseAccountId);
                jedjson.put("debit", true);
                jedjson.put("jeid", jeid);
                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jeDetails.add(jed);

                jedjson = new JSONObject();
                jedjson.put("srno", jeDetails.size() + 1);
                jedjson.put("companyid", companyid);
                jedjson.put("amount", invoiceReceivedAmtExcludingTax);
                jedjson.put("accountid", badDebtSuspenseAccountId);
                jedjson.put("debit", false);
                jedjson.put("jeid", jeid);
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jeDetails.add(jed);

                // Save Mapping
                HashMap<String, Object> mappingObj = new HashMap<String, Object>();
                mappingObj.put("companyId", companyid);
                mappingObj.put("invoiceId", invoiceId);
                mappingObj.put("journalEntryId", jeid);
                mappingObj.put("invoiceReceivedAmt", invoiceReceivedAmt);
                mappingObj.put("recoveredDate", recoveredDate);
                mappingObj.put("gstToRecover", gstToRecover);
                mappingObj.put("badDebtType", 1);
                mappingObj.put("autoGenerated", true);
                mappingObj.put("seqformat", baddebtSeqFormatId);
                mappingObj.put("seqnumber", baddebtIntegerPart);
                mappingObj.put(Constants.DATEPREFIX, datePrefix);
                mappingObj.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
                mappingObj.put(Constants.DATESUFFIX, dateSuffix);
                mappingObj.put("baddebtentryNumber", baddebtentryNumber);
                KwlReturnObject mapResult = accInvoiceDAOobj.saveBadDebtInvoiceMapping(mappingObj);

            }
        } catch (ParseException ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        if (invoiceno.length() > 0) {
            invoiceno = " Except " + invoiceno.substring(0, invoiceno.length() - 1);
        }
        return invoiceno;
    }

    public ModelAndView unClaimBadDebtInvoices(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);

        try {
            unClaimBadDebtInvoices(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.malaysiangst.invoiceIsUnclaimed", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (AccountingException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void unClaimBadDebtInvoices(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException {
        try {
            String invoiceids = request.getParameter("invoiceIds");
            if (!StringUtil.isNullOrEmpty(invoiceids)) {
                String companyId = sessionHandlerImpl.getCompanyid(request);
                String[] invoices = invoiceids.split(",");
                String invoiceid = "";
                HashMap<String, Object> map = new HashMap<>();
                map.put("companyid", companyId);
                map.put("badDebtType", 0);
                KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
                Company company = (Company) companyResult.getEntityList().get(0);
                HashMap<String, Object> requestParams = new HashMap();
                requestParams.put(Constants.companyid, companyId);
                requestParams.put("gcurrencyid", company.getCurrency().getCurrencyID());
                HashMap<String, Object> requestParamsForMapping = new HashMap();
                requestParamsForMapping.put(Constants.companyid, companyId);
                KwlReturnObject result = null;
                KwlReturnObject resultForBaseAmount = null;
                List<BadDebtInvoiceMapping> list = null;
                BadDebtInvoiceMapping mapping = null;
                double badDebtAmountClaimed = 0;
                double badDebtAmountClaimedInBase = 0;
                Invoice invoice = null;
                JournalEntry JE = null;
                boolean isOpeningBalanceInvoice = false;
                for (int i = 0; i < invoices.length; i++) {
                    map.put("invoiceid", invoices[i]);
                    result = accInvoiceDAOobj.getBadDebtInvoiceMappingForInvoice(map);
                    list = result.getEntityList();
                    mapping = list.get(0);
                    badDebtAmountClaimed = mapping.getBadDebtAmtClaimed();
                    result = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoices[i]);
                    invoice = (Invoice) result.getEntityList().get(0);
                    isOpeningBalanceInvoice = invoice.isIsOpeningBalenceInvoice();
                    if (isOpeningBalanceInvoice) {
                        /*
                         * set status flag for opening invoices
                         */
                        double amountdueforstatus = invoice.getOpeningBalanceAmountDue() + badDebtAmountClaimed;
                        if (authHandler.round(amountdueforstatus, companyId) <= 0) {
                            invoice.setIsOpenReceipt(false);
                        } else {
                            invoice.setIsOpenReceipt(true);
                        }
                        invoice.setOpeningBalanceAmountDue(invoice.getOpeningBalanceAmountDue() + badDebtAmountClaimed);
                    } else {
                        /*
                         set status flag for amount due 
                         */
                        double amountdueforstatus = invoice.getInvoiceamountdue() + badDebtAmountClaimed;
                        if (authHandler.round(amountdueforstatus, companyId) <= 0) {
                            invoice.setIsOpenReceipt(false);
                        } else {
                            invoice.setIsOpenReceipt(true);
                        }
                        invoice.setInvoiceamountdue(invoice.getInvoiceamountdue() + badDebtAmountClaimed);
                    }
                    invoice.setAmountDueDate(null);
                    invoice.setClaimAmountDue(0.0);
                    invoice.setBadDebtType(0);
                    invoice.setDebtClaimedDate(null);
                    resultForBaseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, badDebtAmountClaimed, invoice.getCurrency().getCurrencyID(), invoice.getCreationDate(), invoice.getExternalCurrencyRate());
                    badDebtAmountClaimedInBase = (double) resultForBaseAmount.getEntityList().get(0);
                    if (isOpeningBalanceInvoice) {
                        invoice.setOpeningBalanceBaseAmountDue(invoice.getOpeningBalanceBaseAmountDue() + badDebtAmountClaimedInBase);
                    } else {
                        invoice.setInvoiceAmountDueInBase(badDebtAmountClaimedInBase);
                    }
                    JE = mapping.getJournalEntry();
                    requestParamsForMapping.put("id", mapping.getId());
                    accInvoiceDAOobj.deleteBadDebtInvoiceMapping(requestParamsForMapping);
                    result = accJournalEntryobj.deleteJEDtails(JE.getID(), companyId);
                    result = accJournalEntryobj.deleteJE(JE.getID(), companyId);
                }
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
    /*
     * Method to save Opening Balance Invoices For customer.
     */

    public ModelAndView saveOpeningBalanceInvoice(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        boolean isAccountingExe = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = null;//txnManager.getTransaction(def);
        String companyid = "";
        String entryNumber = request.getParameter("number");
        String invoiceid = request.getParameter("transactionId");
        try {
            companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject cncount = null;
            /*
             * Checks duplicate and sequence format number in add case
             */
            if (StringUtil.isNullOrEmpty(invoiceid)) {
                //code to check dupate num
                cncount = accInvoiceDAOobj.getInvoiceCount(entryNumber, companyid);
                if (cncount.getRecordTotalCount() > 0) {
                    isAccountingExe = true;
                    throw new AccountingException(messageSource.getMessage("acc.INV.invoiceno", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                }
                //code for checking wheather entered number can be generated by sequence format or not
                List resultList = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Invoice_ModuleId, entryNumber, companyid);
                if (!resultList.isEmpty()) {
                    boolean isvalidEntryNumber = (Boolean) resultList.get(0);
                    String formatName = (String) resultList.get(1);
                    if (!isvalidEntryNumber) {
                        isAccountingExe = true;
                        throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, RequestContextUtils.getLocale(request)) + " <b>" + entryNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, RequestContextUtils.getLocale(request)) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.pleaseentersomeothernumber", null, RequestContextUtils.getLocale(request)));
                    }
                }
            }

            /*
             Checks duplicate number for simultaneous transactions
             */
            synchronized (this) {
                status = txnManager.getTransaction(def);
                /*
                 Checks number entry in temporary table
                 */
                KwlReturnObject resultInv = accCommonTablesDAO.getTransactionInTemp(entryNumber, companyid, Constants.Acc_Invoice_ModuleId);
                if (resultInv.getRecordTotalCount() > 0) {
                    isAccountingExe = true;
                    throw new AccountingException(messageSource.getMessage("acc.INV.selectedinvoiceno", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyinprocess.", null, RequestContextUtils.getLocale(request)));
                } else {
                    /*
                     Insert entry in temporary table
                     */
                    accCommonTablesDAO.insertTransactionInTemp(entryNumber, companyid, Constants.Acc_Invoice_ModuleId);
                }
                txnManager.commit(status);
            }
            status = txnManager.getTransaction(def);
            List li = saveOpeningBalanceInvoice(request);
            boolean isEditInv = false;
            String succMsg = messageSource.getMessage("acc.field.saved", null, RequestContextUtils.getLocale(request));
            String invoiceNumber = "";
            if (!li.isEmpty()) {
                invoiceNumber = li.get(0).toString();
                jobj.put("invoiceNumber", invoiceNumber);
                isEditInv = (Boolean) li.get(1);
            }
            issuccess = true;
            if (isEditInv) {
                succMsg = messageSource.getMessage("acc.field.updated", null, RequestContextUtils.getLocale(request));
            }
            msg = messageSource.getMessage("acc.agedPay.inv", null, RequestContextUtils.getLocale(request)) + " " + invoiceNumber + " " + messageSource.getMessage("acc.field.hasbeen", null, RequestContextUtils.getLocale(request)) + " " + succMsg + " " + messageSource.getMessage("acc.field.successfully", null, RequestContextUtils.getLocale(request));
            /*
             Delete entry from temporary table
             */
            accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Invoice_ModuleId);
            txnManager.commit(status);
        } catch (ServiceException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
            try {
                /*
                 Delete entry from temporary table
                 */
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Invoice_ModuleId);
            } catch (ServiceException ex1) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (AccountingException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
            try {
                /*
                 Delete entry from temporary table
                 */
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Invoice_ModuleId);
            } catch (ServiceException ex1) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
            try {
                /*
                 Delete entry from temporary table
                 */
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Invoice_ModuleId);
            } catch (ServiceException ex1) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
            if (ex.getMessage() == null) {
                msg = "" + ex.getCause().getMessage();
            }
            try {
                /*
                 Delete entry from temporary table
                 */
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Invoice_ModuleId);
            } catch (ServiceException ex1) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("accException", isAccountingExe);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public HashMap getCurrencyMap(boolean isCurrencyCode) throws ServiceException {
        HashMap currencyMap = new HashMap();
        KwlReturnObject returnObject = accCurrencyDAOobj.getCurrencies(currencyMap);
        List currencyList = returnObject.getEntityList();

        if (currencyList != null && !currencyList.isEmpty()) {
            Iterator iterator = currencyList.iterator();
            while (iterator.hasNext()) {
                KWLCurrency currency = (KWLCurrency) iterator.next();
                if (isCurrencyCode) {
                    currencyMap.put(currency.getCurrencyCode(), currency.getCurrencyID());
                } else {
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
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching customer");
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
        CsvReader csvReader = null;
        int total = 0, failed = 0;
        String companyid = sessionHandlerImpl.getCompanyid(request);
        String gcurrencyId = sessionHandlerImpl.getCurrencyID(request);
        String userId = sessionHandlerImpl.getUserid(request);
        String fileName = jobj.getString("filename");
        String customfield = "";
        String masterPreference = request.getParameter("masterPreference");
        String delimiterType = request.getParameter("delimiterType");
        KwlReturnObject resultObj = null;
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
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            KwlReturnObject extraPref = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extrareferences = (ExtraCompanyPreferences) extraPref.getEntityList().get(0);
            Boolean isCurrencyCode = extrareferences.isCurrencyCode();
            fileInputStream = new FileInputStream(jobj.getString("FilePath"));
            csvReader = new CsvReader(new InputStreamReader(fileInputStream), delimiterType);
            String record = "";
            int cnt = 0;

            double externalCurrencyRate = 0d;//StringUtil.getDouble(request.getParameter("externalcurrencyrate"));

            StringBuilder failedRecords = new StringBuilder();

            HashMap<String, Integer> columnConfig = new HashMap<String, Integer>();
            JSONArray jSONArray = jobj.getJSONArray("resjson");
            Map<String, JSONObject> configMap = new HashMap<>();
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
                if (cnt == 0) {//Putting Header in failure File
                    failedRecords.append(createCSVrecord(recarr) + "\"Error Message\"");//failedRecords.append("\"Row No.\","+createCSVrecord(fileData)+"\"Error Message\""); 
                } else {
                    try {
                        String accountId = "";
                        String customerId = "";

                        /*1. Invoice Number. This is unique key it should be cheked first. if validation failed then no need to check for other cases.*/
                        String invoiceNumber = "";
                        if (columnConfig.containsKey("InvoiceNumber")) {
                            invoiceNumber = recarr[(Integer) columnConfig.get("InvoiceNumber")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(invoiceNumber)) {
                                throw new AccountingException("Empty data found in Transaction Number, cannot set empty data for Transaction Number.");
                            } else if (!transactionNumberSet.add(invoiceNumber)) {// this method retur true when added or false when already exit record not get added
                                throw new AccountingException("Duplicate Transaction Number '" + invoiceNumber + "' in file.");
                            } else {
                                KwlReturnObject result = accInvoiceDAOobj.getInvoiceCount(invoiceNumber, companyid);
                                int nocount = result.getRecordTotalCount();
                                if (nocount > 0) {
                                    throw new AccountingException("Invoice number '" + invoiceNumber + "' already exists.");
                                }
                            }

                            JSONObject configObj = configMap.get("InvoiceNumber");
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

                        /*2. Customer Code*/
                        String customerCode = "";
                        if (columnConfig.containsKey("CustomerCode")) {
                            customerCode = recarr[(Integer) columnConfig.get("CustomerCode")].replaceAll("\"", "").trim();
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

                        /*3. Customer Name
                         *if customerID is empty it menas customer is not found for given code. so need to serch data on name
                         */
                        if (StringUtil.isNullOrEmpty(customerCode)) {
                            if (columnConfig.containsKey("CustomerName")) {
                                String customerName = recarr[(Integer) columnConfig.get("CustomerName")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(customerName)) {
                                    Customer customer = null;
                                    KwlReturnObject retObj = accSalesOrderDAOObj.getCutomer(customerName, companyid);
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

                        /*4. Creation Date*/
                        boolean istransactionDateValid = true;
                        String transactionDateStr = "";
                        Date transactionDate = null, bookbeginningdate = null;
                        if (columnConfig.containsKey("CreationDate")) {
                            transactionDateStr = recarr[(Integer) columnConfig.get("CreationDate")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(transactionDateStr)) {
//                                if (!StringUtil.isNullOrEmpty(transactionDateStr)) {

                                try {
                                    transactionDate = df.parse(transactionDateStr);
                                    // In UI we are not allowing user to give transaction date  on or after book beginning date
                                    // below code is for the same purpose
                                    transactionDate = CompanyPreferencesCMN.removeTimefromDate(transactionDate);
                                    bookbeginningdate = CompanyPreferencesCMN.removeTimefromDate(preferences.getBookBeginningFrom());
                                    if (transactionDate.after(bookbeginningdate)) { // Now, in UI transaction date equal to book beginning date is allowed in opening SI.
                                        failureMsg += messageSource.getMessage("acc.transactiondate.beforebbdate", null, RequestContextUtils.getLocale(request));
                                        istransactionDateValid = false;
                                    }
                                    CompanyPreferencesCMN.checkLockPeriod(accCompanyPreferencesObj, requestParams, transactionDate, true);
                                } catch (ParseException ex) {
                                    failureMsg += "Incorrect date format for Transaction Date, Please specify values in " + dateFormat + " format.";
                                    istransactionDateValid = false;
                                } catch (Exception ex) {
                                    failureMsg += ex.getMessage();
                                }
                            } else {
                                failureMsg += messageSource.getMessage("acc.field.TransactionDateisnotavailable", null, RequestContextUtils.getLocale(request));
                                istransactionDateValid = false;
                            }
                        } else {
                            failureMsg += messageSource.getMessage("acc.field.TransactionDateisnotavailable", null, RequestContextUtils.getLocale(request));
                            istransactionDateValid = false;
                        }

                        /*5. Due Date */
                        Date dueDate = null;
                        if (columnConfig.containsKey("DueDate")) {
                            String dueDateStr = recarr[(Integer) columnConfig.get("DueDate")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(dueDateStr)) {
                                try {
                                    dueDate = df.parse(dueDateStr);
                                } catch (ParseException ex) {
                                    failureMsg += "Incorrect date format for Due Date, Please specify values in " + dateFormat + " format.";
                                }
                            }
                        }

                        if (istransactionDateValid && dueDate != null && dueDate.before(transactionDate)) {
                            failureMsg += messageSource.getMessage("acc.field.duedatebeforTransactionDate", null, RequestContextUtils.getLocale(request));
                        }

                        /*6. Credit Term */
                        String termID = "";
                        if (columnConfig.containsKey("Termid")) {
                            String termName = recarr[(Integer) columnConfig.get("Termid")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(termName)) {
                                KwlReturnObject termResult = accSalesOrderDAOObj.getTerm(termName, companyid);
                                if (termResult != null && !termResult.getEntityList().isEmpty()) {
                                    Term term = (Term) termResult.getEntityList().get(0);
                                    termID = term.getID();
                                    if (dueDate == null && transactionDate != null) {
                                        Calendar cal = Calendar.getInstance();
                                        cal.setTime(transactionDate);
                                        cal.add(Calendar.DATE, term.getTermdays());
                                        Date calDate = null;
                                        String calString = authHandler.getDateOnlyFormat().format(cal.getTime());
                                        try {
                                            calDate = authHandler.getDateOnlyFormat().parse(calString);
                                        } catch (ParseException ex) {
                                            calDate = cal.getTime();
                                            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                        dueDate = calDate;
                                    }
                                } else {
                                    if (masterPreference.equalsIgnoreCase("0")) {// most restricted case
                                        failureMsg += "Credit Term entry not found in master list for Credit Term dropdown.";
                                    } else if (masterPreference.equalsIgnoreCase("1")) {// add empty value if data invalid for non mandatory
                                        failureMsg += "Credit Term entry not found in master list for Credit Term dropdown, cannot set empty data for Credit Term.";
                                    } else if (masterPreference.equalsIgnoreCase("2")) {//add new (most lanient)
                                        failureMsg += "Credit Term entry not present in Credit Term list, Please create new Credit Term entry for " + termName + " as it requires some other details.";
                                    }
                                }
                            } else {
                                failureMsg += "Empty data found in Credit Term, cannot set empty data for Credit Term.";
                            }
                        } else {
                            failureMsg += messageSource.getMessage("acc.field.Termisnotavailable", null, RequestContextUtils.getLocale(request));
                        }

                        /*7. Invoice Currency */
                        String currencyId = "";
                        if (isCurrencyCode ? columnConfig.containsKey("currencyCode") : columnConfig.containsKey("Currency")) {
                            String currencyStr = recarr[isCurrencyCode ? (Integer) columnConfig.get("currencyCode") : (Integer) columnConfig.get("Currency")].replaceAll("\"", "").trim();
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

                        /*8. Amount*/
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
                            failureMsg += messageSource.getMessage("acc.field.TransactionAmountisnotavailable", null, RequestContextUtils.getLocale(request));
                        }

                        /*9. Exchange Rate */
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

                            Date calDate = null;
                            String calString = authHandler.getDateOnlyFormat().format(cal.getTime());
                            try {
                                calDate = authHandler.getDateOnlyFormat().parse(calString);
                            } catch (ParseException ex) {
                                calDate = cal.getTime();
                                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            Date applyDate = calDate;

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

                        /*10. Purchase Order Number*/
                        String porefNo = "";
                        if (columnConfig.containsKey("PoRefNumber")) {
                            porefNo = recarr[(Integer) columnConfig.get("PoRefNumber")].replaceAll("\"", "").trim();

                            JSONObject configObj = configMap.get("PoRefNumber");
                            int maxLength = configObj.optInt("maxLength", 0);
                            String validationType = configObj.optString("validatetype");
                            if ("string".equalsIgnoreCase(validationType) && !StringUtil.isNullOrEmpty(porefNo) && porefNo.length() > maxLength) {
                                if (masterPreference.equalsIgnoreCase("0")) {
                                    failureMsg += "Data length greater than " + maxLength + " for column Purchase Order Number.";
                                } else {// for other two cases need to trim data upto max length
                                    porefNo = porefNo.substring(0, maxLength);
                                }
                            }
                        }

                        /*11. Purchase Order Date*/
                        Date poRefDate = transactionDate;
                        if (columnConfig.containsKey("PoRefDate")) {
                            String porefStr = recarr[(Integer) columnConfig.get("PoRefDate")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(porefStr)) {
                                try {
                                    poRefDate = df.parse(porefStr);
                                    poRefDate = CompanyPreferencesCMN.removeTimefromDate(poRefDate);
                                    CompanyPreferencesCMN.checkLockPeriod(accCompanyPreferencesObj, requestParams, poRefDate, true);
                                } catch (ParseException ex) {
                                    failureMsg += "Incorrect date format for Purchase Order Date, Please specify values in " + dateFormat + " format.";
                                } catch (Exception ex) {
                                    failureMsg += ex.getMessage();
                                }
                            }
                        }

                        if (istransactionDateValid && poRefDate != null && poRefDate.after(transactionDate)) {
                            throw new AccountingException(messageSource.getMessage("acc.field.purchaseorderdateafterTransactionDate", null, RequestContextUtils.getLocale(request)));
                        }

                        /*12.Sales Person */
                        String salesPersonId = "";
                        if (columnConfig.containsKey("salesperson")) {
                            String salesPersonName = recarr[(Integer) columnConfig.get("salesperson")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(salesPersonName)) {
                                MasterItem masterItem = null;
                                KwlReturnObject retObj = accCustomerDAOObj.getSalesPersonByName(companyid, salesPersonName);
                                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                                    masterItem = (MasterItem) retObj.getEntityList().get(0);
                                }
                                if (masterItem != null) {
                                    salesPersonId = masterItem.getID();
                                } else {
                                    if (masterPreference.equalsIgnoreCase("0")) {// most restricted case
                                        failureMsg += "Sales Person entry not found in master list for Sales Person dropdown.";
                                    } else if (masterPreference.equalsIgnoreCase("1")) {// add empty value if data invalid for non mandatory
                                        salesPersonId = "";
                                    } else if (masterPreference.equalsIgnoreCase("2")) {//add new (most lanient)
                                        failureMsg += "Sales Person entry not present in Sales Person list, Please create new Sales Person entry for " + salesPersonName + " as it requires some other details.";
                                    }
                                }
                            }
                        }

                        /*13. Memo*/
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
                                requestParamsCF.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Invoice_ModuleId, headArrayList.get(K)));
                                KwlReturnObject fieldParamsResult = accCommonTablesDAO.getFieldParams(requestParamsCF); // get custom field for module
                                FieldParams params = null;
                                if (fieldParamsResult.getRecordTotalCount() > 0) {
                                    params = (FieldParams) fieldParamsResult.getEntityList().get(0);
                                    customFieldParamMap.put(headArrayList.get(K).toString(), params);
                                }
                            }
                        }

                        // creating invoice json
                        JSONObject invjson = new JSONObject();

                        invjson.put("entrynumber", invoiceNumber);
                        invjson.put("autogenerated", false);

                        KwlReturnObject ERresult = accCurrencyDAOobj.getExcDetailID(requestParams, currencyId, transactionDate, null);
                        ExchangeRateDetails erd = (ExchangeRateDetails) ERresult.getEntityList().get(0);
                        String erdid = (erd == null) ? null : erd.getID();
                        invjson.put("erdid", erdid);

                        invjson.put("shipaddress", "");

                        invjson.put("porefno", porefNo);
                        invjson.put("duedate", dueDate);
                        invjson.put("poRefDate", poRefDate);
                        invjson.put("companyid", companyid);
                        invjson.put("currencyid", currencyId);
                        invjson.put("externalCurrencyRate", externalCurrencyRate);
                        invjson.put("salesPerson", salesPersonId);
                        invjson.put("partialinv", false);
                        invjson.put("customerid", customerId);
                        invjson.put("accountid", accountId);
                        invjson.put("billto", "");
                        invjson.put("creationDate", transactionDate);
                        invjson.put("lastModifiedDate", df.parse(df.format(new Date())));
                        invjson.put("isOpeningBalenceInvoice", true);
                        invjson.put("isNormalInvoice", false);
                        invjson.put("originalOpeningBalanceAmount", transactionAmount);
                        invjson.put("openingBalanceAmountDue", transactionAmount);
                        invjson.put("exchangeRateForOpeningTransaction", exchangeRateForOpeningTransaction);
                        invjson.put("conversionRateFromCurrencyToBase", true);
                        invjson.put("termid", termID);
                        invjson.put("memo", memo);
                        // Store Invoice amount in base currency
                        invjson.put(Constants.originalOpeningBalanceBaseAmount, authHandler.round(transactionAmount * exchangeRateForOpeningTransaction, companyid));
                        invjson.put(Constants.openingBalanceBaseAmountDue, authHandler.round(transactionAmount * exchangeRateForOpeningTransaction, companyid));
                        invjson.put(Constants.Checklocktransactiondate, request.getParameter("billdate"));
                        resultObj = accInvoiceDAOobj.addInvoice(invjson, new HashSet());
                        Invoice invoice = (Invoice) resultObj.getEntityList().get(0);

                        // For creating custom field array
                        JSONArray customJArr = fieldDataManagercntrl.getCustomFieldForOeningTransactionsRecords(headArrayList, customFieldParamMap, recarr, columnConfig, request);
                        customfield = customJArr.toString();
                        if (!StringUtil.isNullOrEmpty(customfield)) {
                            JSONArray jcustomarray = new JSONArray(customfield);
                            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                            customrequestParams.put("customarray", jcustomarray);
                            customrequestParams.put("modulename", "OpeningBalanceInvoice");
                            customrequestParams.put("moduleprimarykey", Constants.Acc_OpeningBalanceInvoiceid);
                            customrequestParams.put("modulerecid", invoice.getID());
                            customrequestParams.put("moduleid", Constants.Acc_Invoice_ModuleId);
                            customrequestParams.put("companyid", companyid);
                            customrequestParams.put("customdataclasspath", Constants.Acc_OpeningBalanceInvoice_custom_data_classpath);
                            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                invjson.put("invoiceid", invoice.getID());
                                invjson.put("openingBalanceInvoiceCustomData", invoice.getID());
                                KwlReturnObject result = accInvoiceDAOobj.updateInvoice(invjson, new HashSet());
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
                msg = messageSource.getMessage("acc.field.Imported", null, RequestContextUtils.getLocale(request)) + success + messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (success > 1 ? "s" : "") + messageSource.getMessage("acc.field.successfully.", null, RequestContextUtils.getLocale(request));
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
            throw new AccountingException("Error While Importing Records.");
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
                logDataMap.put("FailureFileType", failed > 0 ? "csv" : "");
                logDataMap.put("TotalRecs", total);
                logDataMap.put("Rejected", failed);
                logDataMap.put("Module", Constants.Acc_Invoice_ModuleId);
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

//            if(StringUtil.isNullOrEmpty(ext)) {
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

    public List saveOpeningBalanceInvoice(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException {
        List returnList = new ArrayList();
        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject result = null;
            int nocount;
            boolean isEditInvoice = false;
            String auditMsg = "", auditID = "", memo = "";
            double invoiceOldAmountDue = 0d;
            Date oldLastModifiedDate = df.parse(df.format(new Date()));

            // Fetching request parameters
            String invoiceNumber = request.getParameter("number");
            String transactionDateStr = request.getParameter("billdate");
            String dueDateStr = request.getParameter("dueDate");
            String currencyid = (request.getParameter("currencyid") == null ? sessionHandlerImpl.getCurrencyID(request) : request.getParameter("currencyid"));
            String lastSavedCurrencyid = (request.getParameter("currencyid") == null ? sessionHandlerImpl.getCurrencyID(request) : request.getParameter("currencyid"));
            String transactionAmountStr = request.getParameter("transactionAmount");
            String poRefDateStr = request.getParameter("poRefDate");
            String salesPerson = request.getParameter("salesPerson");
            String invoiceid = request.getParameter("transactionId");
            String sequenceformat = request.getParameter("sequenceformat");
            String porefno = request.getParameter("porefno");
            String customerId = request.getParameter("accountId");
            String termId = (request.getParameter("termid") == null) ? "" : request.getParameter("termid");
            boolean conversionRateFromCurrencyToBase = true;
            if (request.getParameter("CurrencyToBaseExchangeRate") != null) {
                conversionRateFromCurrencyToBase = Boolean.parseBoolean(request.getParameter("CurrencyToBaseExchangeRate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("memo"))) {
                memo = request.getParameter("memo").toString();
            }
            double exchangeRateForOpeningTransaction = 1;
            if (!StringUtil.isNullOrEmpty(request.getParameter("exchangeRateForOpeningTransaction"))) {
                exchangeRateForOpeningTransaction = Double.parseDouble(request.getParameter("exchangeRateForOpeningTransaction"));
            }
            double externalCurrencyRate = StringUtil.getDouble(request.getParameter("externalcurrencyrate"));

            Date transactionDate = df.parse(df.format(new Date()));
            Date lastModifiedDate = df.parse(df.format(new Date()));
            Date dueDate = df.parse(df.format(new Date()));
            Date poRefDate = df.parse(df.format(new Date()));

            if (!StringUtil.isNullOrEmpty(transactionDateStr)) {
                transactionDate = df.parse(transactionDateStr);
            }
            if (!StringUtil.isNullOrEmpty(dueDateStr)) {
                dueDate = df.parse(dueDateStr);
            }
            if (!StringUtil.isNullOrEmpty(poRefDateStr)) {
                poRefDate = df.parse(poRefDateStr);
            }

            double transactionAmount = 0d;
            if (!StringUtil.isNullOrEmpty(transactionAmountStr)) {
                transactionAmount = Double.parseDouble(transactionAmountStr);
            }

            // creating invoice json
            JSONObject invjson = new JSONObject();

            if (!StringUtil.isNullOrEmpty(invoiceid)) {

                KwlReturnObject invResult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceid); // (Tax)session.get(Tax.class, taxid);
                Invoice invoice = (Invoice) invResult.getEntityList().get(0);
                invoiceOldAmountDue = invoice.getOpeningBalanceAmountDue();
                oldLastModifiedDate = df.parse(df.format(invoice.getLastModifiedDate()));
                isEditInvoice = true;
                lastSavedCurrencyid = invoice.getCurrency().getCurrencyID();
                invjson.put("invoiceid", invoiceid);
            }

            // data processing for invoice auto number
            if (StringUtil.isNullOrEmpty(invoiceid)) {
                result = accInvoiceDAOobj.getInvoiceCount(invoiceNumber, companyid);
                nocount = result.getRecordTotalCount();
                if (nocount > 0) {
                    throw new AccountingException("Invoice number '" + invoiceNumber + "' already exists.");
                }
                invjson.put("entrynumber", invoiceNumber);
                invjson.put("autogenerated", false);
            }

            String accountId = "";

            if (!StringUtil.isNullOrEmpty(customerId)) {
                KwlReturnObject cusResult = accountingHandlerDAOobj.getObject(Customer.class.getName(), customerId);
                Customer customer = (Customer) cusResult.getEntityList().get(0);
                accountId = customer.getAccount().getID();
            }

            KwlReturnObject ERresult = accCurrencyDAOobj.getExcDetailID(requestParams, currencyid, df.parse(request.getParameter("billdate")), null);
            ExchangeRateDetails erd = (ExchangeRateDetails) ERresult.getEntityList().get(0);
            String erdid = (erd == null) ? null : erd.getID();
            invjson.put("erdid", erdid);

            invjson.put("shipaddress", "");

            invjson.put("porefno", porefno);
            invjson.put("duedate", dueDate);
            invjson.put("poRefDate", poRefDate);
            invjson.put("companyid", companyid);
            invjson.put("currencyid", currencyid);
            invjson.put("externalCurrencyRate", externalCurrencyRate);
            invjson.put("salesPerson", salesPerson);
            invjson.put("partialinv", false);
            invjson.put("customerid", customerId);
            invjson.put("accountid", accountId);
            invjson.put("billto", "");
            invjson.put("creationDate", transactionDate);
            invjson.put("lastModifiedDate", lastModifiedDate);
            invjson.put("isOpeningBalenceInvoice", true);
            invjson.put("isNormalInvoice", false);
            invjson.put("originalOpeningBalanceAmount", transactionAmount);
            invjson.put("openingBalanceAmountDue", transactionAmount);
            invjson.put("termid", termId);
            invjson.put("memo", memo);
            // Store invoice amount in base currency
            if (conversionRateFromCurrencyToBase) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                invjson.put(Constants.originalOpeningBalanceBaseAmount, authHandler.round(transactionAmount * exchangeRateForOpeningTransaction, companyid));
                invjson.put(Constants.openingBalanceBaseAmountDue, authHandler.round(transactionAmount * exchangeRateForOpeningTransaction, companyid));
            } else {
                invjson.put(Constants.originalOpeningBalanceBaseAmount, authHandler.round(transactionAmount / exchangeRateForOpeningTransaction, companyid));
                invjson.put(Constants.openingBalanceBaseAmountDue, authHandler.round(transactionAmount / exchangeRateForOpeningTransaction, companyid));
            }
            invjson.put("exchangeRateForOpeningTransaction", exchangeRateForOpeningTransaction);
            invjson.put("conversionRateFromCurrencyToBase", conversionRateFromCurrencyToBase);

            if (!StringUtil.isNullOrEmpty(request.getParameter("excludingGstAmount"))) {
                double excludingGstAmount = 0d;
                excludingGstAmount = Double.parseDouble(request.getParameter("excludingGstAmount"));
                invjson.put("excludingGstAmount", excludingGstAmount);
                if (conversionRateFromCurrencyToBase) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                    invjson.put("excludingGstAmountInBase", authHandler.round(excludingGstAmount * exchangeRateForOpeningTransaction, companyid));
                } else {
                    invjson.put("excludingGstAmountInBase", authHandler.round(excludingGstAmount / exchangeRateForOpeningTransaction, companyid));
                }
            }

            if (!StringUtil.isNullOrEmpty(request.getParameter("taxAmount"))) {
                double taxAmount = 0d;
                taxAmount = Double.parseDouble(request.getParameter("taxAmount"));
                invjson.put("taxAmount", taxAmount);
                if (conversionRateFromCurrencyToBase) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                    invjson.put("taxAmountInBase", authHandler.round(taxAmount * exchangeRateForOpeningTransaction, companyid));
                } else {
                    invjson.put("taxAmountInBase", authHandler.round(taxAmount / exchangeRateForOpeningTransaction, companyid));
                }
            }

            if (isEditInvoice) {

                boolean isInvoiceUsedInOtherTransactions = isInvoiceUsedInOtherTransactions(invoiceid, companyid);
                if (isInvoiceUsedInOtherTransactions) {
                    throw new AccountingException(messageSource.getMessage("acc.nee.73", null, RequestContextUtils.getLocale(request)));
                }

                result = accInvoiceDAOobj.updateInvoice(invjson, new HashSet());
                auditMsg = "updated";
                auditID = AuditAction.OPENING_BALANCE_UPDATED;
            } else {
                invjson.put(Constants.Checklocktransactiondate, request.getParameter("billdate"));
                result = accInvoiceDAOobj.addInvoice(invjson, new HashSet());
                auditMsg = "added";
                auditID = AuditAction.OPENING_BALANCE_CREATED;
            }

            Invoice invoice = (Invoice) result.getEntityList().get(0);//Create Invoice without invoice-details.
            returnList.add(invoice.getInvoiceNumber());
            returnList.add(isEditInvoice);
            String customfield = request.getParameter("customfield");
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", "OpeningBalanceInvoice");
                customrequestParams.put("moduleprimarykey", Constants.Acc_OpeningBalanceInvoiceid);
                customrequestParams.put("modulerecid", invoice.getID());
                customrequestParams.put("moduleid", Constants.Acc_Invoice_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_OpeningBalanceInvoice_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    invjson.put("invoiceid", invoice.getID());
                    invjson.put("openingBalanceInvoiceCustomData", invoice.getID());
                    result = accInvoiceDAOobj.updateInvoice(invjson, new HashSet());
                }
            }
            auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has  " + auditMsg + " an Opening Balance Customer Invoice " + invoiceNumber, request, invoiceNumber);
            // Updating Account Opening balance
//            updateAccountOpeningBalance(request,customerId,invoiceOldAmountDue,transactionAmount,currencyid,lastSavedCurrencyid,oldLastModifiedDate,transactionDate);

        } catch (ParseException ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return returnList;
    }

    private boolean isInvoiceUsedInOtherTransactions(String invoiceId, String companyId) throws ServiceException {
        boolean isInvoiceUsedInOtherTransactions = false;
        KwlReturnObject result;
        if (!StringUtil.isNullOrEmpty(invoiceId)) {
            isInvoiceUsedInOtherTransactions = accInvoiceDAOobj.isInvoiceUsedInCreditNote(invoiceId, companyId);
        }
        return isInvoiceUsedInOtherTransactions;
    }

    public ModelAndView saveBulkInvoice(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        String jeentryNumber = "";
        String jeSeqFormatId = "";
        String jeIntegerPart = "";
        String jeDatePrefix = "";
        String jeDateAfterPrefix = "";
        String jeDateSuffix = "";
        KwlReturnObject result = null;
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = null;
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);

            String jeid = null;
            double discValue = 0.0;
            boolean jeautogenflag = false;
            Invoice invoice = null;
            String nextAutoNumber = "";
            String doId = "";
            String invoiceNo = "";
            List doIdArr1 = new ArrayList();
            List doNoArr = new ArrayList();
            HashMap<String, JSONArray> hm = new HashMap<String, JSONArray>();

            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            String userId = sessionHandlerImpl.getUserid(request);

            /* Iterating loop on no of selected DO to create Invoice
             Preparing Json Array on Key of customer ID
             */
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobData = jArr.getJSONObject(i);
                String personid = jobData.getString("personid");
                if (!hm.containsKey(personid)) {
                    hm.put(personid, new JSONArray());
                }
                hm.get(personid).put(jobData);
            }

            Boolean bulkInvoices = false;
            String sequenceformatInvoice = request.getParameter("sequenceformatInvoice");
            bulkInvoices = Boolean.parseBoolean(request.getParameter("bulkInvoices"));
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            int j = 0;
            status = txnManager.getTransaction(def);
            /* Iterating loop on each record with Key Customer ID*/
            for (String personIdKey : hm.keySet()) {
                JSONArray js = hm.get(personIdKey);
                for (int i = 0; i < js.length(); i++) {
                    JSONObject jobData = js.getJSONObject(i);
                    if (bulkInvoices) {
                        doIdArr1.clear();
                        doNoArr.clear();
                        for (int k = 0; k < js.length(); k++) {
                            JSONObject jobTempData = js.getJSONObject(k);
                            doIdArr1.add(jobTempData.getString("billid"));
                            doNoArr.add(jobTempData.getString("billno"));
                        }
                    }
                    doId = jobData.getString("billid");
                    DateFormat df = authHandler.getUserDateFormatterWithoutTimeZone(request);
                    HashMap<String, Object> dataMap = new HashMap<String, Object>();
                    KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(DeliveryOrder.class.getName(), doId);
                    DeliveryOrder deliveryOrder = (DeliveryOrder) rdresult.getEntityList().get(0);
                    String custID = deliveryOrder.getCustomer().getID();
                    String custWarehouse = "";
                    if (deliveryOrder.getCustWarehouse() != null) {
                        custWarehouse = deliveryOrder.getCustWarehouse().getId();
                    }
                    String movementtype = "";
                    if (deliveryOrder.isIsconsignment()) {
                        movementtype = deliveryOrder.getMovementType() != null ? deliveryOrder.getMovementType().getID() : "";
                    }
                    String createdby = sessionHandlerImpl.getUserid(request);
                    String modifiedby = sessionHandlerImpl.getUserid(request);
                    long createdon = System.currentTimeMillis();
                    long updatedon = System.currentTimeMillis();

                    String taxid = deliveryOrder.getTax() != null ? deliveryOrder.getTax().getID() : "";
                    String costCenterId = "";
                    costCenterId = deliveryOrder.getCostcenter() == null ? "" : deliveryOrder.getCostcenter().getID();
                    double externalCurrencyRate = deliveryOrder.getExternalCurrencyRate();

                    String currencyid = (deliveryOrder.getCurrency() != null ? deliveryOrder.getCurrency().getCurrencyID() : sessionHandlerImpl.getCurrencyID(request));
                    Customer customer = new Customer();
                    String accountId = "";

                    Map<String, Object> invoicePrmt = new HashMap<String, Object>();

                    KwlReturnObject custresult = null;
                    if (!StringUtil.isNullOrEmpty(custID)) {
                        custresult = accountingHandlerDAOobj.getObject(Customer.class.getName(), custID);
                        customer = (Customer) custresult.getEntityList().get(0);
                        if (customer.getAccount() != null) {
                            accountId = customer.getAccount().getID();
                        }
                    }

                    if (!StringUtil.isNullOrEmpty(accountId)) {
                        invoicePrmt.put("accountid", accountId);
                    }
                    if (!StringUtil.isNullOrEmpty(custID)) {
                        invoicePrmt.put("customerid", custID);
                    }
                    double taxamount = 0;
                    invoicePrmt.put("autogenerated", true);

                    //Entry No generate
                    int from = StaticValues.AUTONUM_INVOICE;

                    String nextAutoNoInt = "";
                    String datePrefix = "";
                    String dateafterPrefix = "";
                    String dateSuffix = "";

                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, from, sequenceformatInvoice, false, null);// when creation date is today sending null
                    nextAutoNumber = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    nextAutoNoInt = (String) seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                    datePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    dateafterPrefix = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                    dateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part

                    BillingShippingAddresses addresses = deliveryOrder.getBillingShippingAddresses();
                    invoicePrmt.put(InvoiceConstants.entrynumber, nextAutoNumber);
                    invoicePrmt.put("seqnumber", nextAutoNoInt);
                    invoicePrmt.put(Constants.DATEPREFIX, datePrefix);
                    invoicePrmt.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
                    invoicePrmt.put(Constants.DATESUFFIX, dateSuffix);
                    invoicePrmt.put("seqformat", sequenceformatInvoice);
                    invoicePrmt.put("isfavourite", deliveryOrder.isFavourite());
                    invoicePrmt.put("porefno", deliveryOrder.getCustomerPORefNo());

                    invoicePrmt.put("shipaddress", addresses == null ? "" : addresses.getShippingAddress());

                    invoicePrmt.put("billshipAddressid", deliveryOrder.getBillingShippingAddresses() == null ? "" : deliveryOrder.getBillingShippingAddresses().getID());

                    if (!(doIdArr1.size() > 1 && bulkInvoices)) { // multiple DOs selected to create single invoice
                        invoicePrmt.put("shipvia", deliveryOrder.getShipvia());
                        invoicePrmt.put("fob", deliveryOrder.getFob());
                        invoicePrmt.put("salesPerson", deliveryOrder.getSalesperson() != null ? deliveryOrder.getSalesperson().getID() : "");
                        invoicePrmt.put(InvoiceConstants.memo, deliveryOrder.getMemo());
                        if (deliveryOrder.getShipdate() != null) {
                            invoicePrmt.put(InvoiceConstants.shipdate, deliveryOrder.getShipdate());
                        }
                    } else {
                        invoicePrmt.put("shipvia", "");
                        invoicePrmt.put("fob", "");
                        invoicePrmt.put("salesPerson", "");
                        invoicePrmt.put(InvoiceConstants.memo, "");
                    }
                    invoicePrmt.put("companyid", companyid);
                    invoicePrmt.put("currencyid", currencyid);
                    invoicePrmt.put("externalCurrencyRate", externalCurrencyRate);
                    invoicePrmt.put("createdby", createdby);
                    Calendar c = Calendar.getInstance();
                    c.setTime(new Date());
                    c.add(Calendar.DATE, customer.getCreditTerm().getTermdays());  // number of days to add
                    String dt = df.format(c.getTime());
//                    dt = sdf.format(c.getTime());
                    invoicePrmt.put(InvoiceConstants.duedate, authHandler.getUserDateFormatterWithoutTimeZone(request).parse(dt));
                    invoicePrmt.put("modifiedby", modifiedby);
                    invoicePrmt.put("createdon", createdon);
                    invoicePrmt.put("updatedon", updatedon);
                    invoicePrmt.put("termid", customer.getCreditTerm().getID());
                    if (!StringUtil.isNullOrEmpty(custWarehouse)) {
                        invoicePrmt.put("custWarehouse", custWarehouse);
                    }
                    if (!StringUtil.isNullOrEmpty(movementtype)) {
                        invoicePrmt.put("movementtype", movementtype);
                    }
                    Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
                    Calendar c1 = Calendar.getInstance();
                    c1.setTime(new Date());
                    String dt1 = df.format(c1.getTime());
                    Date entryDate = authHandler.getUserDateFormatterWithoutTimeZone(request).parse(dt1);

                    synchronized (this) {
                        HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                        JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                        JEFormatParams.put("modulename", CompanyPreferencesConstants.AUTOJOURNALENTRY);
                        JEFormatParams.put("companyid", companyid);
                        JEFormatParams.put("isdefaultFormat", true);

                        KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                        SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, entryDate);
                        jeentryNumber = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                        jeIntegerPart = (String) seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                        jeDatePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                        jeDateAfterPrefix = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                        jeDateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                        jeSeqFormatId = format.getID();
                        jeautogenflag = true;
                    }
//            }
                    jeDataMap.put("entrynumber", jeentryNumber);
                    jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                    jeDataMap.put(Constants.SEQNUMBER, jeIntegerPart);
                    jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
                    jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
                    jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
                    jeDataMap.put("autogenerated", true);

                    jeDataMap.put(InvoiceConstants.entrydate, entryDate);
                    jeDataMap.put("companyid", companyid);
//                    jeDataMap.put("memo", jobData.getString("memo"));
                    jeDataMap.put("createdby", createdby);
                    jeDataMap.put("currencyid", currencyid);
                    jeDataMap.put("costcenterid", costCenterId);
                    jeDataMap.put("transactionModuleid", Constants.Acc_Invoice_ModuleId);//For journal Entry Type

                    HashSet jeDetails = new HashSet();
                    KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
                    JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
                    jeid = journalEntry.getID();
                    invoicePrmt.put("journalerentryid", jeid);
                    jeDataMap.put("jeid", jeid);
                    if (bulkInvoices) {
                        result = accInvoiceDAOobj.getDeliverOrderDetailsForBulkInvoices(doIdArr1, companyid);
                    } else {
                        /*Fetch DO details to save Invoice details */
                        result = accInvoiceDAOobj.getDeliverOrderDetails(doId, companyid);
                    }

                    List dll = saveDeliveryRows(request, result, companyid, jobData, jeDetails, jeid);
                    double[] totals = (double[]) dll.get(0);

                    HashSet<InvoiceDetail> deliveryOrderdetails = (HashSet<InvoiceDetail>) dll.get(1);
                    ArrayList<String> prodList = (ArrayList<String>) dll.get(2);
                    discValue = totals[0];

                    taxamount = totals[2];
                    double totalInvAmount = totals[1] - discValue;//totalamount - discount
                    boolean gstIncluded = deliveryOrder.isGstIncluded();
                    /* If "including GST" option true then tax amount is not added
                     in total invoice amount
                     */
                    if (!gstIncluded) {
                        totalInvAmount += totals[2];
                    }

                    /*
                     * If invoice terms applied then add mapping against invoice
                     */
                    double termTotalAmount = 0;
                    JSONArray termsArr = new JSONArray();
                    /*  Get Term details applied on Delivery Order*/
                    // if(gstIncluded){
                    termsArr = accInvoiceModuleService.getTermDetailsForDeliveryOrder(deliveryOrder.getID());
                  // }

                    String InvoiceTerms = termsArr.toString();
                    HashMap<String, Double> termAcc = new HashMap<String, Double>();

                    if (termsArr != null && termsArr.length() > 0) {

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
                        if (termsArr.length() > 0) {
                            invoicePrmt.put(Constants.termsincludegst, false);
                        }
                    }

                    /* If term is mapped with Tax then Calculating tax as per logic
                     i.e taxi is applied on total Amount including Term amount
                     */
                    if (!StringUtil.isNullOrEmpty(taxid)) {
                        List taxTermMapping = accInvoiceDAOobj.getTerms(taxid);
                        double taxPercent = 0;
                        if (taxTermMapping != null && taxTermMapping.size() > 0) {
                            KwlReturnObject taxresult = accTaxObj.getTaxPercent(sessionHandlerImpl.getCompanyid(request), deliveryOrder.getOrderDate(), taxid);
                            taxPercent = (Double) taxresult.getEntityList().get(0);
                            taxamount = ((totals[1] - totals[0] + termTotalAmount) * taxPercent) / 100;
                            totalInvAmount = totalInvAmount - totals[2] + taxamount;

                        }
                    }

                    /* Adding term amount in total Amount of invoice */
                    totalInvAmount += termTotalAmount;

                    Date creationDate = (Date) authHandler.getUserDateFormatterWithoutTimeZone(request).parse(dt1);
                    KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, totalInvAmount, currencyid, creationDate, externalCurrencyRate);
                    double totalInvAmountinBase = (Double) bAmt.getEntityList().get(0);
                    invoicePrmt.put(Constants.invoiceamount, totalInvAmount);
                    invoicePrmt.put(Constants.invoiceamountinbase, totalInvAmountinBase);
                    invoicePrmt.put(Constants.invoiceamountdue, totalInvAmount);
                    invoicePrmt.put(Constants.invoiceamountdueinbase, totalInvAmountinBase);
                    invoicePrmt.put(Constants.discountAmount, discValue);
                    DateFormat dfInv = authHandler.getDateOnlyFormat();
                    Date transactionDate = dfInv.parse(dfInv.format(new Date()));
                    invoicePrmt.put("creationDate", transactionDate);
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, discValue, currencyid, creationDate, externalCurrencyRate);
                    double discValueInBase = (Double) bAmt.getEntityList().get(0);
                    invoicePrmt.put(Constants.discountAmountInBase, discValueInBase);

                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", jeDetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", totalInvAmount);
                    jedjson.put("accountid", accountId);
                    jedjson.put("debit", true);
                    jedjson.put("jeid", jeid);
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jeDetails.add(jed);
                    invoicePrmt.put("customerentryid", jed.getID());

                    /* Saving Jedetails for discount*/
                    if (discValue > 0) {
                        jedjson = new JSONObject();
                        jedjson.put("srno", jeDetails.size() + 1);
                        jedjson.put(Constants.companyKey, companyid);
                        jedjson.put("amount", discValue);
                        jedjson.put("accountid", preferences.getDiscountGiven().getID());
                        jedjson.put("debit", true);
                        jedjson.put("jeid", jeid);
                        jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jeDetails.add(jed);
                    }

                    /* Saving jedetail for term amount */
                    if (termAcc.size() > 0) {
                        for (Map.Entry<String, Double> entry : termAcc.entrySet()) {
                            jedjson = new JSONObject();
                            jedjson.put("srno", jeDetails.size() + 1);
                            jedjson.put(Constants.companyKey, companyid);
                            jedjson.put("amount", entry.getValue() > 0 ? entry.getValue() : (entry.getValue() * (-1)));
                            jedjson.put("accountid", entry.getKey());
                            jedjson.put("debit", entry.getValue() > 0 ? false : true);
                            jedjson.put("jeid", jeid);
                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jeDetails.add(jed);
                        }
                    }

                    if (!StringUtil.isNullOrEmpty(taxid)) {
                        KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), taxid); // (Tax)session.get(Tax.class, taxid);
                        Tax tax = (Tax) txresult.getEntityList().get(0);
                        if (tax == null) {
                            throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
                        }
                        invoicePrmt.put("taxid", taxid);
                        if (taxamount > 0) {
                            jedjson = new JSONObject();
                            jedjson.put("srno", jeDetails.size() + 1);
                            jedjson.put("companyid", companyid);
                            jedjson.put("amount", taxamount);
                            jedjson.put("accountid", tax.getAccount().getID());
                            jedjson.put("debit", false);
                            jedjson.put("jeid", jeid);
                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jeDetails.add(jed);

                            invoicePrmt.put("taxentryid", jed.getID());

                        }
                    }

                    double taxAmountinBase = 0;
                    double excludingGstAmountInBase = 0;

                    /* Add global level "tax amount" and "tax amount in base" value 
                     required to show invoice in GST report
                     */
                    invoicePrmt.put("taxAmount", taxamount);
                    KwlReturnObject baseAmountObj = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, taxamount, currencyid, deliveryOrder.getOrderDate(), externalCurrencyRate);
                    taxAmountinBase = (Double) baseAmountObj.getEntityList().get(0);
                    taxAmountinBase = authHandler.round(taxAmountinBase, companyid);
                    invoicePrmt.put("taxAmountInBase", taxAmountinBase);
                    excludingGstAmountInBase = totals[1] - discValue;

                    if (gstIncluded) {
                        excludingGstAmountInBase = totals[1] - totals[2] - discValue;
                        //excludingGstAmountInBase += totals[3];//LIne level Term Amount
                    }
                    invoicePrmt.put("excludingGstAmount", excludingGstAmountInBase);
                    baseAmountObj = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, excludingGstAmountInBase, currencyid, deliveryOrder.getOrderDate(), externalCurrencyRate);
                    excludingGstAmountInBase = (Double) baseAmountObj.getEntityList().get(0);
                    excludingGstAmountInBase = authHandler.round(excludingGstAmountInBase, companyid);
                    invoicePrmt.put("excludingGstAmountInBase", excludingGstAmountInBase);

                    JSONObject invjson = new JSONObject();
                    Set<String> set = invoicePrmt.keySet();
                    for (String key : set) {
                        invjson.accumulate(key, invoicePrmt.get(key));
                    }
                    invjson.put(Constants.Checklocktransactiondate, request.getParameter("billdate"));
                    result = accInvoiceDAOobj.addInvoice(invjson, new HashSet());
                    invoice = (Invoice) result.getEntityList().get(0);//Create Invoice without invoice-details.
                    int approvalStatusLevel = 11;

                    invoicePrmt.put("approvalstatuslevel", 11);

                    invoicePrmt.put("invoiceid", invoice.getID());
                    invoicePrmt.put("pendingapproval", 0);
                    invjson = new JSONObject();
                    set = invoicePrmt.keySet();
                    for (String key : set) {
                        invjson.accumulate(key, invoicePrmt.get(key));
                    }

                    Iterator itr = deliveryOrderdetails.iterator();
                    while (itr.hasNext()) {
                        InvoiceDetail ivd = (InvoiceDetail) itr.next();
                        if (ivd.getInventory().isInvrecord()) {
                            Inventory invtry = ivd.getInventory();
                            invtry.setActquantity(invtry.getQuantity());
                            invtry.setQuantity(0);
                        }
                        ivd.setInvoice(invoice);
                    }
                    invoicePrmt.put("invoiceid", invoice.getID());
                    invoicePrmt.put("gstIncluded", gstIncluded);

                    invjson = new JSONObject();
                    set = invoicePrmt.keySet();
                    for (String key : set) {
                        invjson.accumulate(key, invoicePrmt.get(key));
                    }

                    /*
                     * If invoice terms applied then add mapping against invoice
                     */
                    if (StringUtil.isAsciiString(InvoiceTerms)) {

                        accInvoiceModuleService.mapInvoiceTerms(InvoiceTerms, invoice.getID(), userId, false);
                    }

                    /*-----Saving Custom Fields for Invoice----------*/
                    HashMap<String, Object> customRequestParams = new HashMap();
                    customRequestParams.put("deliveryOrder", deliveryOrder);
                    customRequestParams.put("journalEntryId", journalEntry.getID());
                    customRequestParams.put("companyid", companyid);
                    customRequestParams.put("jeDataMap", jeDataMap);

                    accInvoiceModuleService.saveCustomFieldsForInvoice(customRequestParams);

                    /*----Ending Of saving Custom Fields-----*/
                    result = accInvoiceDAOobj.updateInvoice(invjson, deliveryOrderdetails);
                    /* To update Flag in DO whether it is available for SI next time or not*/
                    accInvoiceModuleService.updateOpenStatusFlagForDOInSI(doId);
                    JSONObject jeJobj = new JSONObject();
                    HashSet<JournalEntryDetail> details = new HashSet<JournalEntryDetail>();
                    if (approvalStatusLevel == 11) {
                        jeJobj.put("pendingapproval", 0);
                    }
                    jeJobj.put("isDraft", invoice.isDraft());
                    jeJobj.put("jeid", jeid);
                    jeJobj.put(JournalEntryConstants.COMPANYID, companyid);
                    jeJobj.put("transactionId", invoice.getID());
                    accJournalEntryobj.updateJournalEntry(jeJobj, details);
                    if (bulkInvoices) {
                        for (int ii = 0; ii < doIdArr1.size(); ii++) {
                            accInvoiceModuleService.updateOpenStatusFlagForDOInSI(doIdArr1.get(ii).toString());
                        }
                    } else {
                        accInvoiceModuleService.updateOpenStatusFlagForDOInSI(doId);
                    }

                    if (bulkInvoices) {

                        invoiceNo += nextAutoNumber + ", ";

                    } else {
                        invoiceNo += nextAutoNumber + ", ";

                    }
                    String action = "";
                    if (!bulkInvoices) {
                        action = "created Individual invoices";
                        auditTrailObj.insertAuditLog(AuditAction.INVOICE_CREATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + "" + " " + nextAutoNumber + "", request, nextAutoNumber);

                    }

                    if (bulkInvoices) {

                        for (int l = 0; l < doNoArr.size(); l++) {
                            /*
                             * saving linking informaion of Sales Order while
                             * linking with Sales Invoice
                             */

                            HashMap<String, Object> requestParamsLinking = new HashMap<String, Object>();
                            requestParamsLinking.put("linkeddocid", invoice.getID());
                            requestParamsLinking.put("docid", doIdArr1.get(l));
                            requestParamsLinking.put(Constants.moduleid, Constants.Acc_Invoice_ModuleId);
                            requestParamsLinking.put("linkeddocno", nextAutoNumber);
                            requestParamsLinking.put("sourceflag", 0);
                            KwlReturnObject result3 = accInvoiceDAOobj.saveDeliveryOrderLinking(requestParamsLinking);


                            /*
                             * saving linking informaion of Sales Invoice while
                             * linking with Sales Order
                             */
                            requestParamsLinking.put("linkeddocid", doIdArr1.get(l));
                            requestParamsLinking.put("docid", invoice.getID());
                            requestParamsLinking.put(Constants.moduleid, Constants.Acc_Sales_Order_ModuleId);
                            requestParamsLinking.put("linkeddocno", doNoArr.get(l));
                            requestParamsLinking.put("sourceflag", 1);
                            result3 = accInvoiceDAOobj.saveInvoiceLinking(requestParamsLinking);
                        }

                    } else {
                        /*
                         * saving linking informaion of Sales Order while
                         * linking with Sales Invoice
                         */

                        HashMap<String, Object> requestParamsLinking = new HashMap<String, Object>();
                        requestParamsLinking.put("linkeddocid", invoice.getID());
                        requestParamsLinking.put("docid", deliveryOrder.getID());
                        requestParamsLinking.put(Constants.moduleid, Constants.Acc_Invoice_ModuleId);
                        requestParamsLinking.put("linkeddocno", nextAutoNumber);
                        requestParamsLinking.put("sourceflag", 0);
                        KwlReturnObject result3 = accInvoiceDAOobj.saveDeliveryOrderLinking(requestParamsLinking);


                        /*
                         * saving linking informaion of Sales Invoice while
                         * linking with Sales Order
                         */
                        requestParamsLinking.put("linkeddocid", deliveryOrder.getID());
                        requestParamsLinking.put("docid", invoice.getID());
                        requestParamsLinking.put(Constants.moduleid, Constants.Acc_Sales_Order_ModuleId);
                        requestParamsLinking.put("linkeddocno", deliveryOrder.getDeliveryOrderNumber());
                        requestParamsLinking.put("sourceflag", 1);
                        result3 = accInvoiceDAOobj.saveInvoiceLinking(requestParamsLinking);
                    }

                    if (bulkInvoices) {
                        break;
                    }

                }
                j++;
            }
            invoiceNo = invoiceNo.substring(0, (invoiceNo.lastIndexOf(",")));
            issuccess = true;

            String action = "";
            if (bulkInvoices) {
                action = "created Bulk invoices";
                auditTrailObj.insertAuditLog(AuditAction.INVOICE_CREATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + "" + " " + invoiceNo + "", request, invoiceNo);
            }

            txnManager.commit(status);
            msg = "Invoice generated successfully.<br><b> Invoice No:</b> " + "<font style=\"word-break: break-all;\">" + invoiceNo + "." + "</font>";
        } catch (SessionExpiredException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AccountingException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return new ModelAndView("jsonView", "model", jobj.toString());

    }

    /*To Create invoices from selected SO    
     1.Individual invoices per SO
     2.Bulk invoices per customer
     */
    public ModelAndView saveBulkInvoicesFromSO(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray data = new JSONArray();
        String msg = "";
        String jeentryNumber = "";
        String jeSeqFormatId = "";
        String jeIntegerPart = "";
        String jeDatePrefix = "";
        String jeDateAfterPrefix = "";
        String jeDateSuffix = "";
        KwlReturnObject result = null;
        boolean issuccess = false;
        HashMap hMap = new HashMap();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = null;
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);

            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Sales_Order_ModuleId));
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            String userId = sessionHandlerImpl.getUserid(request);

            String jeid = null;
            double discValue = 0.0;

            Invoice invoice = null;
            String nextAutoNumber = "";
            String salesOrderId = "";
            String invoiceNo = "";

            List salesOrderIdArray = new ArrayList();
            List salesOrderNoArray = new ArrayList();

            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);

            HashMap<String, JSONArray> hm = new HashMap<String, JSONArray>();

            /* Preparing a map with Key customer ID
            
             Data in Hashmap with            
             Key-Customer ID
             Value -Json Array
             
             */
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobData = jArr.getJSONObject(i);
                String personid = jobData.getString("personid");
                if (!hm.containsKey(personid)) {
                    hm.put(personid, new JSONArray());
                }
                hm.get(personid).put(jobData);
            }

            Boolean bulkInvoices = false;
            String sequenceformatInvoice = request.getParameter("sequenceformatInvoice");
            bulkInvoices = Boolean.parseBoolean(request.getParameter("bulkInvoices"));
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            int j = 0;

            /* Iterating Map on key Customer ID*/
            for (String personIdKey : hm.keySet()) {

                JSONArray js = hm.get(personIdKey);
                try {
                    /* Opening transaction while creating bulk invoices*/
                    if (bulkInvoices) {
                        status = txnManager.getTransaction(def);
                    }

                    /*iterating loop on data json array of key customer id */
                    for (int i = 0; i < js.length(); i++) {

                        if (!bulkInvoices) {
                            /* Opening transaction while creating individual invoices*/
                            status = txnManager.getTransaction(def);
                        }

                        try {
                            JSONObject jobData = js.getJSONObject(i);
                            if (bulkInvoices) {
                                salesOrderIdArray.clear();
                                salesOrderNoArray.clear();
                                for (int k = 0; k < js.length(); k++) {
                                    JSONObject jobTempData = js.getJSONObject(k);
                                    salesOrderIdArray.add(jobTempData.getString("billid"));
                                    salesOrderNoArray.add(jobTempData.getString("billno"));
                                }
                            }

                            DateFormat df = authHandler.getUserDateFormatterWithoutTimeZone(request);
                            HashMap<String, Object> dataMap = new HashMap<String, Object>();

                            salesOrderId = jobData.getString("billid");
                            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), salesOrderId);
                            SalesOrder salesOrderObj = (SalesOrder) rdresult.getEntityList().get(0);

                            String custID = salesOrderObj.getCustomer().getID();
                            String custWarehouse = "";
                            if (salesOrderObj.getCustWarehouse() != null) {
                                custWarehouse = salesOrderObj.getCustWarehouse().getId();
                            }
                            String movementtype = "";
                            if (salesOrderObj.isIsconsignment()) {
                                movementtype = salesOrderObj.getMovementType() != null ? salesOrderObj.getMovementType().getID() : "";
                            }
                            String createdby = sessionHandlerImpl.getUserid(request);
                            String modifiedby = sessionHandlerImpl.getUserid(request);
                            long createdon = System.currentTimeMillis();
                            long updatedon = System.currentTimeMillis();

                            String taxid = salesOrderObj.getTax() != null ? salesOrderObj.getTax().getID() : "";
                            String costCenterId = "";
                            costCenterId = salesOrderObj.getCostcenter() == null ? "" : salesOrderObj.getCostcenter().getID();
                            double externalCurrencyRate = salesOrderObj.getExternalCurrencyRate();
                            Discount discount = null;

                            String currencyid = (salesOrderObj.getCurrency() != null ? salesOrderObj.getCurrency().getCurrencyID() : sessionHandlerImpl.getCurrencyID(request));
                            Customer customer = new Customer();
                            String accountId = "";
                            Map<String, Object> oldInvoicePrmt = new HashMap<String, Object>();
                            Map<String, Object> invoicePrmt = new HashMap<String, Object>();
                            Map<String, Object> newAuditKey = new HashMap<String, Object>();
                            KwlReturnObject custresult = null;

                            if (!StringUtil.isNullOrEmpty(custID)) {
                                custresult = accountingHandlerDAOobj.getObject(Customer.class.getName(), custID);
                                customer = (Customer) custresult.getEntityList().get(0);
                                if (customer.getAccount() != null) {
                                    accountId = customer.getAccount().getID();
                                }
                            }

                            if (!StringUtil.isNullOrEmpty(accountId)) {
                                invoicePrmt.put("accountid", accountId);
                            }
                            if (!StringUtil.isNullOrEmpty(custID)) {
                                invoicePrmt.put("customerid", custID);
                            }
                            double taxamount = 0;
                            invoicePrmt.put("autogenerated", true);

                            //Entry No generate
                            int from = StaticValues.AUTONUM_INVOICE;

                            String nextAutoNoInt = "";
                            String datePrefix = "";
                            String dateafterPrefix = "";
                            String dateSuffix = "";

                            Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, from, sequenceformatInvoice, false, null);// when creation date is today sending null
                            nextAutoNumber = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                            nextAutoNoInt = (String) seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                            datePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                            dateafterPrefix = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                            dateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part

                            BillingShippingAddresses addresses = salesOrderObj.getBillingShippingAddresses();
                            invoicePrmt.put(InvoiceConstants.entrynumber, nextAutoNumber);
                            invoicePrmt.put("seqnumber", nextAutoNoInt);
                            invoicePrmt.put(Constants.DATEPREFIX, datePrefix);
                            invoicePrmt.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
                            invoicePrmt.put(Constants.DATESUFFIX, dateSuffix);
                            invoicePrmt.put("seqformat", sequenceformatInvoice);
                            invoicePrmt.put("isfavourite", salesOrderObj.isFavourite());

                            invoicePrmt.put("shipaddress", addresses == null ? "" : addresses.getShippingAddress());

                            if (!(salesOrderIdArray.size() > 1 && bulkInvoices)) {
                                invoicePrmt.put("shipvia", salesOrderObj.getShipvia());
                                invoicePrmt.put("fob", salesOrderObj.getFob());
                                invoicePrmt.put("salesPerson", salesOrderObj.getSalesperson() != null ? salesOrderObj.getSalesperson().getID() : "");
                                invoicePrmt.put(InvoiceConstants.memo, salesOrderObj.getMemo());
                                if (salesOrderObj.getShipdate() != null) {
                                    invoicePrmt.put(InvoiceConstants.shipdate, salesOrderObj.getShipdate());
                                }
                            } else {
                                invoicePrmt.put("shipvia", "");
                                invoicePrmt.put("fob", "");
                                invoicePrmt.put("salesPerson", "");
                                invoicePrmt.put(InvoiceConstants.memo, "");
                            }
                            invoicePrmt.put("companyid", companyid);
                            invoicePrmt.put("currencyid", currencyid);
                            invoicePrmt.put("externalCurrencyRate", externalCurrencyRate);
                            invoicePrmt.put("createdby", createdby);
                            Calendar c = Calendar.getInstance();
                            c.setTime(new Date());
                            c.add(Calendar.DATE, customer.getCreditTerm().getTermdays());  // number of days to add
                            String dt = df.format(c.getTime());

                            invoicePrmt.put(InvoiceConstants.duedate, authHandler.getUserDateFormatterWithoutTimeZone(request).parse(dt));
                            invoicePrmt.put("modifiedby", modifiedby);
                            invoicePrmt.put("createdon", createdon);
                            invoicePrmt.put("updatedon", updatedon);
                            invoicePrmt.put("termid", customer.getCreditTerm().getID());
                            if (!StringUtil.isNullOrEmpty(custWarehouse)) {
                                invoicePrmt.put("custWarehouse", custWarehouse);
                            }
                            if (!StringUtil.isNullOrEmpty(movementtype)) {
                                invoicePrmt.put("movementtype", movementtype);
                            }
                            Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
                            Calendar c1 = Calendar.getInstance();
                            c1.setTime(new Date());
                            String dt1 = df.format(c1.getTime());
                            Date entryDate = authHandler.getUserDateFormatterWithoutTimeZone(request).parse(dt1);

                            synchronized (this) {
                                HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                                JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                                JEFormatParams.put("modulename", CompanyPreferencesConstants.AUTOJOURNALENTRY);
                                JEFormatParams.put("companyid", companyid);
                                JEFormatParams.put("isdefaultFormat", true);

                                KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                                SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, entryDate);
                                jeentryNumber = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                                jeIntegerPart = (String) seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                                jeDatePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                                jeDateAfterPrefix = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                                jeDateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                                jeSeqFormatId = format.getID();

                            }

                            jeDataMap.put("entrynumber", jeentryNumber);
                            jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                            jeDataMap.put(Constants.SEQNUMBER, jeIntegerPart);
                            jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
                            jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
                            jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
                            jeDataMap.put("autogenerated", true);
                            jeDataMap.put(Constants.memo, salesOrderObj.getMemo());

                            jeDataMap.put(InvoiceConstants.entrydate, entryDate);
                            jeDataMap.put("companyid", companyid);
                            jeDataMap.put("createdby", createdby);
                            jeDataMap.put("currencyid", currencyid);
                            jeDataMap.put("costcenterid", costCenterId);
                            jeDataMap.put("transactionModuleid", 2);//For journal Entry Type

                            HashSet jeDetails = new HashSet();
                            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
                            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
                            jeid = journalEntry.getID();
                            invoicePrmt.put("journalerentryid", jeid);
                            jeDataMap.put("jeid", jeid);

                            if (bulkInvoices) {
                                /*Fetching Sales order detais for all selected SO per customer */
                                result = accSalesOrderDAOObj.getSalesOrderDetailsForBulkInvoices(salesOrderIdArray, companyid);
                            } else {
                                /*Fetching Sales order detais per SO*/
                                result = accSalesOrderDAOObj.getSalesOrderDetails(salesOrderId, companyid);
                            }

                            /* Function to save Invoice details*/
                            List dll = saveInvoiceDetail(request, result, companyid, jobData, jeDetails, jeid);
                            double[] totals = (double[]) dll.get(0);
                            JSONObject discjson = new JSONObject();
                            discjson.put("originalamount", totals[1] - totals[0] + totals[2]);//Do check
                            discjson.put("companyid", companyid);
                            KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                            discount = (Discount) dscresult.getEntityList().get(0);
                            invoicePrmt.put("discountid", discount.getID());
                            discValue = discount.getDiscountValue();
                            HashSet<InvoiceDetail> deliveryOrderdetails = (HashSet<InvoiceDetail>) dll.get(1);
                            ArrayList<String> prodList = (ArrayList<String>) dll.get(2);
                            discValue += totals[0];

                            taxamount = totals[2];
                            double totalInvAmount = totals[1] - discValue;//totalamount - discount
                            boolean gstIncluded = salesOrderObj.isGstIncluded();
                            /* If "including GST" option true then tax amount is not added
                             in total invoice amount
                             */
                            if (!gstIncluded) {
                                totalInvAmount += totals[2];
                            }

                            /*
                             * If invoice terms applied then add mapping against invoice
                             */
                            double termTotalAmount = 0;
                            /*  Get Term details applied on Sales Order*/
                            JSONArray termsArr = getTermDetailsForSalesOrder(salesOrderObj.getID());
                            String InvoiceTerms = termsArr.toString();
                            HashMap<String, Double> termAcc = new HashMap<String, Double>();

                            if (termsArr != null && termsArr.length() > 0) {

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
                                if (termsArr.length() > 0) {
                                    invoicePrmt.put(Constants.termsincludegst, false);
                                }
                            }

                            /* If term is mapped with Tax then Calculating tax as per logic*/
                            if (!StringUtil.isNullOrEmpty(taxid)) {
                                List taxTermMapping = accInvoiceDAOobj.getTerms(taxid);
                                double taxPercent = 0;
                                if (taxTermMapping != null && taxTermMapping.size() > 0) {
                                    KwlReturnObject taxresult = accTaxObj.getTaxPercent(sessionHandlerImpl.getCompanyid(request), salesOrderObj.getOrderDate(), taxid);
                                    taxPercent = (Double) taxresult.getEntityList().get(0);
                                    taxamount = ((totals[1] - totals[0] + termTotalAmount) * taxPercent) / 100;
                                    totalInvAmount = totalInvAmount - totals[2] + taxamount;

                                }
                            }

                            /* Adding term amoount in total Amount of invoice */
                            totalInvAmount += termTotalAmount;

                            invoicePrmt.put(Constants.invoiceamountdue, totalInvAmount);
                            Date creationDate = (Date) authHandler.getUserDateFormatterWithoutTimeZone(request).parse(dt1);
                            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, totalInvAmount, currencyid, creationDate, externalCurrencyRate);
                            double totalInvAmountinBase = (Double) bAmt.getEntityList().get(0);
                            invoicePrmt.put(Constants.invoiceamount, totalInvAmount);
                            invoicePrmt.put(Constants.invoiceamountinbase, totalInvAmountinBase);
                            invoicePrmt.put(Constants.invoiceamountdueinbase, totalInvAmountinBase);
                            invoicePrmt.put(Constants.discountAmount, discValue);
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, discValue, currencyid, creationDate, externalCurrencyRate);
                            double discValueInBase = (Double) bAmt.getEntityList().get(0);
                            invoicePrmt.put(Constants.discountAmountInBase, discValueInBase);

                            JSONObject jedjson = new JSONObject();
                            jedjson.put("srno", jeDetails.size() + 1);
                            jedjson.put("companyid", companyid);
                            jedjson.put("amount", totalInvAmount);
                            jedjson.put("accountid", accountId);
                            jedjson.put("debit", true);
                            jedjson.put("jeid", jeid);
                            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jeDetails.add(jed);
                            invoicePrmt.put("customerentryid", jed.getID());

                            /* Saving Jedetails for discount*/
                            if (discValue > 0) {
                                jedjson = new JSONObject();
                                jedjson.put("srno", jeDetails.size() + 1);
                                jedjson.put(Constants.companyKey, companyid);
                                jedjson.put("amount", discValue);
                                jedjson.put("accountid", preferences.getDiscountGiven().getID());
                                jedjson.put("debit", true);
                                jedjson.put("jeid", jeid);
                                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                jeDetails.add(jed);
                            }

                            /* Saving jedetail for term amount */
                            if (termAcc.size() > 0) {
                                for (Map.Entry<String, Double> entry : termAcc.entrySet()) {
                                    jedjson = new JSONObject();
                                    jedjson.put("srno", jeDetails.size() + 1);
                                    jedjson.put(Constants.companyKey, companyid);
                                    jedjson.put("amount", entry.getValue() > 0 ? entry.getValue() : (entry.getValue() * (-1)));
                                    jedjson.put("accountid", entry.getKey());
                                    jedjson.put("debit", entry.getValue() > 0 ? false : true);
                                    jedjson.put("jeid", jeid);
                                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                    jeDetails.add(jed);
                                }
                            }

                            /*saving  jedetails for Global Tax*/
                            if (!StringUtil.isNullOrEmpty(taxid)) {
                                KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), taxid); // (Tax)session.get(Tax.class, taxid);
                                Tax tax = (Tax) txresult.getEntityList().get(0);
                                if (tax == null) {
                                    throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
                                }
                                invoicePrmt.put("taxid", taxid);
                                if (taxamount > 0) {
                                    jedjson = new JSONObject();
                                    jedjson.put("srno", jeDetails.size() + 1);
                                    jedjson.put("companyid", companyid);
                                    jedjson.put("amount", taxamount);
                                    jedjson.put("accountid", tax.getAccount().getID());
                                    jedjson.put("debit", false);
                                    jedjson.put("jeid", jeid);
                                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                    jeDetails.add(jed);
                                    invoicePrmt.put("taxentryid", jed.getID());

                                }
                            }

                            double taxAmountinBase = 0;
                            double excludingGstAmountInBase = 0;

                            /* Add global level "tax amount" and "tax amount in base" value 
                             required to show invoice in GST report
                             */
                            invoicePrmt.put("taxAmount", taxamount);
                            KwlReturnObject baseAmountObj = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, taxamount, currencyid, salesOrderObj.getOrderDate(), externalCurrencyRate);
                            taxAmountinBase = (Double) baseAmountObj.getEntityList().get(0);
                            taxAmountinBase = authHandler.round(taxAmountinBase, companyid);
                            invoicePrmt.put("taxAmountInBase", taxAmountinBase);
                            excludingGstAmountInBase = totals[1] - discValue;

                            if (gstIncluded) {
                                excludingGstAmountInBase = totals[1] - totals[2] - discValue;
                                excludingGstAmountInBase += totals[3];//LIne level Term Amount
                            }
                            invoicePrmt.put("excludingGstAmount", excludingGstAmountInBase);
                            baseAmountObj = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, excludingGstAmountInBase, currencyid, salesOrderObj.getOrderDate(), externalCurrencyRate);
                            excludingGstAmountInBase = (Double) baseAmountObj.getEntityList().get(0);
                            excludingGstAmountInBase = authHandler.round(excludingGstAmountInBase, companyid);
                            invoicePrmt.put("excludingGstAmountInBase", excludingGstAmountInBase);
                            invoicePrmt.put("creationDate", entryDate);

                            JSONObject invjson = new JSONObject();
                            Set<String> set = invoicePrmt.keySet();
                            for (String key : set) {
                                invjson.accumulate(key, invoicePrmt.get(key));
                            }
                            invjson.put(Constants.Checklocktransactiondate, request.getParameter("billdate"));

                            result = accInvoiceDAOobj.addInvoice(invjson, new HashSet());
                            invoice = (Invoice) result.getEntityList().get(0);//Create Invoice without invoice-details.
                            int approvalStatusLevel = 11;

                            invoicePrmt.put("approvalstatuslevel", 11);

                            invoicePrmt.put("invoiceid", invoice.getID());
                            invoicePrmt.put("pendingapproval", 0);

                            /*
                             * If invoice terms applied then add mapping against invoice
                             */
                            if (StringUtil.isAsciiString(InvoiceTerms)) {

                                accInvoiceModuleService.mapInvoiceTerms(InvoiceTerms, invoice.getID(), userId, false);
                            }

                            JSONObject obj = new JSONObject();
                            /* Getting Sales Order Custom/Dimension field*/
                            Map<String, Object> variableMap = new HashMap<String, Object>();
                            SalesOrderCustomData jeDetailCustom = (SalesOrderCustomData) salesOrderObj.getSoCustomData();
                            replaceFieldMap = new HashMap<String, String>();
                            if (jeDetailCustom != null) {
                                AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                                boolean isExport = false;
                                JSONObject params = new JSONObject();
                                params.put(Constants.companyKey, companyid);
                                params.put(Constants.isExport, isExport);
                                params.put(Constants.isdefaultHeaderMap, false);

                                params.put(Constants.browsertz, "");

                                fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                            }

                            /* Write a function to fetch custom field from fieldparams against fieldlabel
                             where moduleid will be invoice  
                          
                             */
                            HashMap requestParams1 = new HashMap();
                            JSONArray jcustomarray = new JSONArray();
                            JSONObject jsonObject = new JSONObject();
                            Iterator ite = obj.keys();
                            String fieldLabel = "";
                            String fieldValue = "";

                            requestParams1.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_field, Constants.companyid, Constants.moduleid));
                            FieldParams fieldParams = null;
                            while (ite.hasNext()) {
                                fieldLabel = (String) ite.next();
                                fieldValue = obj.getString(fieldLabel);

                                requestParams1.put(Constants.filter_values, Arrays.asList(fieldLabel, companyid, Constants.Acc_Invoice_ModuleId));

                                /* Fetching custom/dimension field "field Label" wise */
                                List result1 = accAccountDAOobj.getFieldParamsFieldLabelWise(requestParams1); // get custom field module wise from fieldlabel
                                if (result1 != null && result1.size() > 0) {

                                    fieldParams = (FieldParams) result1.get(0);

                                    jsonObject.put("fieldid", fieldParams.getId());

                                    /* If multiselect drop down then fetching actual value instead of id */
                                    if (fieldParams.getFieldtype() == Constants.MULTISELECTCOMBO || fieldParams.getFieldtype() == Constants.FIELDSET) {

                                        HashMap<String, Object> fieldParamsMap = new HashMap<String, Object>();
                                        fieldParamsMap.put("companyid", companyid);
                                        fieldParamsMap.put("moduleid", Constants.Acc_Sales_Order_ModuleId);
                                        fieldParamsMap.put("filedname", fieldLabel);
                                        String multiSelectId = fieldManagerDAOobj.getFieldParamsId(fieldParamsMap);
                                        fieldValue = fieldManagerDAOobj.getParamsValue(multiSelectId, fieldValue);
                                    }

                                    jsonObject.put("xtype", fieldParams.getFieldtype());
                                    jsonObject.put("fieldname", fieldParams.getFieldname());
                                    jsonObject.put(fieldParams.getFieldname(), "Col" + fieldParams.getColnum());
                                    jsonObject.put("refcolumn_name", "Col" + fieldParams.getRefcolnum());

                                    if (fieldParams.getFieldtype() == Constants.SINGLESELECTCOMBO || fieldParams.getFieldtype() == Constants.MULTISELECTCOMBO || fieldParams.getFieldtype() == Constants.FIELDSET) {
                                        String ids = fieldManagerDAOobj.getIdsUsingParamsValue(fieldParams.getId(), fieldValue);     // get ids for module using values and field id  
                                        fieldValue = ids;

                                    }
                                    jsonObject.put("Col" + fieldParams.getColnum(), fieldValue);
                                    jsonObject.put("fieldDataVal", fieldValue);

                                    jcustomarray.put(jsonObject);
                                }

                                /* Saving Custom field for invoice module */
                                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                                customrequestParams.put("customarray", jcustomarray);
                                customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                                customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                                customrequestParams.put("modulerecid", journalEntry.getID());
                                customrequestParams.put(Constants.moduleid, Constants.Acc_Invoice_ModuleId);
                                customrequestParams.put(Constants.companyKey, companyid);
                                customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                    jeDataMap.put("accjecustomdataref", journalEntry.getID());
                                    jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);
                                }

                            }

                            invjson = new JSONObject();
                            set = invoicePrmt.keySet();
                            for (String key : set) {
                                invjson.accumulate(key, invoicePrmt.get(key));
                            }

                            Iterator itr = deliveryOrderdetails.iterator();
                            while (itr.hasNext()) {
                                InvoiceDetail ivd = (InvoiceDetail) itr.next();
                                if (ivd.getInventory().isInvrecord()) {
                                    Inventory invtry = ivd.getInventory();
                                    invtry.setActquantity(invtry.getQuantity());
                                    invtry.setQuantity(0);
                                }
                                ivd.setInvoice(invoice);
                            }
                            invoicePrmt.put("invoiceid", invoice.getID());

                            invoicePrmt.put("gstIncluded", gstIncluded);

                            invjson = new JSONObject();
                            set = invoicePrmt.keySet();
                            for (String key : set) {
                                invjson.accumulate(key, invoicePrmt.get(key));
                            }

                            result = accInvoiceDAOobj.updateInvoice(invjson, deliveryOrderdetails);

                            JSONObject jeJobj = new JSONObject();
                            HashSet<JournalEntryDetail> details = new HashSet<JournalEntryDetail>();
                            if (approvalStatusLevel == 11) {
                                jeJobj.put("pendingapproval", 0);
                            }
                            jeJobj.put("isDraft", invoice.isDraft());
                            jeJobj.put("jeid", jeid);
                            jeJobj.put(JournalEntryConstants.COMPANYID, companyid);
                            jeJobj.put("transactionId", invoice.getID());
                            accJournalEntryobj.updateJournalEntry(jeJobj, details);

                            if (bulkInvoices) {
                                for (int ii = 0; ii < salesOrderIdArray.size(); ii++) {
                                    /* Updating Link Flag & open flag of SO  */

                                    KwlReturnObject rdresult1 = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), salesOrderIdArray.get(ii).toString());
                                    SalesOrder salesOrderObj1 = (SalesOrder) rdresult1.getEntityList().get(0);
                                    hMap.put("salesOrder", salesOrderObj1);
                                    hMap.put("value", "1");
                                    hMap.put("isSOOpen", false);
                                    accInvoiceDAOobj.updateSOLinkflag(hMap);
                                }
                            } else {
                                /* Updating Link Flag & open flag of SO  */

                                hMap.put("salesOrder", salesOrderObj);
                                hMap.put("value", "1");
                                hMap.put("isSOOpen", false);
                                accInvoiceDAOobj.updateSOLinkflag(hMap);
                            }

                            if (bulkInvoices) {

                                invoiceNo += nextAutoNumber + ", ";

                            } else {
                                invoiceNo += nextAutoNumber + ", ";

                            }
                            String action = "";
                            if (!bulkInvoices) {
                                action = "created Individual invoices";
                                auditTrailObj.insertAuditLog(AuditAction.INVOICE_CREATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + "" + " " + nextAutoNumber + "", request, nextAutoNumber);

                            }

                            if (bulkInvoices) {

                                for (int l = 0; l < salesOrderNoArray.size(); l++) {
                                    /*
                                     * saving linking informaion of Sales Order while
                                     * linking with Sales Invoice
                                     */

                                    HashMap<String, Object> requestParamsLinking = new HashMap<String, Object>();
                                    requestParamsLinking.put("linkeddocid", invoice.getID());
                                    requestParamsLinking.put("docid", salesOrderIdArray.get(l));
                                    requestParamsLinking.put(Constants.moduleid, Constants.Acc_Invoice_ModuleId);
                                    requestParamsLinking.put("linkeddocno", nextAutoNumber);
                                    requestParamsLinking.put("sourceflag", 0);
                                    KwlReturnObject result3 = accSalesOrderDAOObj.saveSalesOrderLinking(requestParamsLinking);


                                    /*
                                     * saving linking informaion of Sales Invoice while
                                     * linking with Sales Order
                                     */
                                    requestParamsLinking.put("linkeddocid", salesOrderIdArray.get(l));
                                    requestParamsLinking.put("docid", invoice.getID());
                                    requestParamsLinking.put(Constants.moduleid, Constants.Acc_Sales_Order_ModuleId);
                                    requestParamsLinking.put("linkeddocno", salesOrderNoArray.get(l));
                                    requestParamsLinking.put("sourceflag", 1);
                                    result3 = accInvoiceDAOobj.saveInvoiceLinking(requestParamsLinking);
                                }

                            } else {
                                /*
                                 * saving linking informaion of Sales Order while
                                 * linking with Sales Invoice
                                 */

                                HashMap<String, Object> requestParamsLinking = new HashMap<String, Object>();
                                requestParamsLinking.put("linkeddocid", invoice.getID());
                                requestParamsLinking.put("docid", salesOrderObj.getID());
                                requestParamsLinking.put(Constants.moduleid, Constants.Acc_Invoice_ModuleId);
                                requestParamsLinking.put("linkeddocno", nextAutoNumber);
                                requestParamsLinking.put("sourceflag", 0);
                                KwlReturnObject result3 = accSalesOrderDAOObj.saveSalesOrderLinking(requestParamsLinking);


                                /*
                                 * saving linking informaion of Sales Invoice while
                                 * linking with Sales Order
                                 */
                                requestParamsLinking.put("linkeddocid", salesOrderObj.getID());
                                requestParamsLinking.put("docid", invoice.getID());
                                requestParamsLinking.put(Constants.moduleid, Constants.Acc_Sales_Order_ModuleId);
                                requestParamsLinking.put("linkeddocno", salesOrderObj.getSalesOrderNumber());
                                requestParamsLinking.put("sourceflag", 1);
                                result3 = accInvoiceDAOobj.saveInvoiceLinking(requestParamsLinking);
                            }

                            /* Leaving from inner loop as only one invoice(in case of bulk) per customer 
                      
                             irrespective of the no of selected SO
                      
                             */
                            if (bulkInvoices) {

                                break;
                            }
                            /* If creating individual Invoices per SO */
                            if (!bulkInvoices) {
                                /* Committing transaction while creating individual invoices*/
                                txnManager.commit(status);
                            }

                        } catch (Exception ex) {
                            if (status != null) {
                                txnManager.rollback(status);
                            }
                            msg = "" + ex.getMessage();
                            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                        }
                    }
                    /* If creating bulk Invoices per customer */
                    if (bulkInvoices) {

                        /* Committing transaction while creating bulk invoices*/
                        txnManager.commit(status);
                    }
                } catch (Exception ex) {
                    if (status != null) {
                        txnManager.rollback(status);
                    }
                    msg = "" + ex.getMessage();
                    Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                }

                j++;

            }
            invoiceNo = invoiceNo.substring(0, (invoiceNo.lastIndexOf(",")));
            issuccess = true;

            try {
                String action = "";
                if (bulkInvoices) {
                    status = txnManager.getTransaction(def);
                    action = "created Bulk invoices";
                    auditTrailObj.insertAuditLog(AuditAction.INVOICE_CREATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + "" + " " + invoiceNo + "", request, invoiceNo);
                    txnManager.commit(status);
                }
            } catch (Exception ex) {
                if (status != null) {
                    txnManager.rollback(status);
                }
                msg = "" + ex.getMessage();
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }

            msg = "Invoice generated successfully.<br><b> Invoice No:</b> " + "<font style=\"word-break: break-all;\">" + invoiceNo + "." + "</font>";
        } catch (SessionExpiredException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return new ModelAndView("jsonView", "model", jobj.toString());

    }
    /* Function used to save Bulk DO from SO*/

    public ModelAndView saveBulkDOFromSO(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray data = new JSONArray();
        String msg = "";
        KwlReturnObject result = null;
        boolean issuccess = false;
        HashMap hMap = new HashMap();
        String productIds = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = null;
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String userId = sessionHandlerImpl.getUserid(request);

            ExtraCompanyPreferences extraCompanyPreferences = null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);

            DeliveryOrder deliveryOrder = null;
            String nextAutoNumber = "";
            String salesOrderId = "";
            String invoiceNo = "";

            List salesOrderIdArray = new ArrayList();
            List salesOrderNoArray = new ArrayList();
            double totalAmountInDocumentCurrecy = 0;
            JSONArray productDiscountJArr = new JSONArray();
            String pendingDeliveryOrders = "";

            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);

            HashMap<String, JSONArray> hm = new HashMap<String, JSONArray>();

            /* Preparing a map with Key customer ID
            
             Data in Hashmap with            
             Key-Customer ID
             Value -Json Array
             
             */
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobData = jArr.getJSONObject(i);
                String personid = jobData.getString("personid");
                if (!hm.containsKey(personid)) {
                    hm.put(personid, new JSONArray());
                }
                hm.get(personid).put(jobData);
            }

            Boolean bulkInvoices = false;
            String sequenceformatInvoice = request.getParameter("sequenceformatInvoice");
            bulkInvoices = Boolean.parseBoolean(request.getParameter("bulkInvoices"));

            int j = 0;

            /* Iterating Map on key Customer ID*/
            for (String personIdKey : hm.keySet()) {

                JSONArray js = hm.get(personIdKey);
                try {
                    /* Opening transaction while creating bulk invoices*/
                    if (bulkInvoices) {
                        status = txnManager.getTransaction(def);
                    }

                    /*iterating loop on data json array of key customer id */
                    for (int i = 0; i < js.length(); i++) {

                        try {
                            if (!bulkInvoices) {
                                /* Opening transaction while creating individual invoices*/
                                status = txnManager.getTransaction(def);
                            }

                            JSONObject jobData = js.getJSONObject(i);
                            if (bulkInvoices) {
                                salesOrderIdArray.clear();
                                salesOrderNoArray.clear();
                                for (int k = 0; k < js.length(); k++) {
                                    JSONObject jobTempData = js.getJSONObject(k);
                                    salesOrderIdArray.add(jobTempData.getString("billid"));
                                    salesOrderNoArray.add(jobTempData.getString("billno"));
                                }
                            }

                            DateFormat df = authHandler.getUserDateFormatterWithoutTimeZone(request);
                            HashMap<String, Object> dataMap = new HashMap<String, Object>();

                            salesOrderId = jobData.getString("billid");
                            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), salesOrderId);
                            SalesOrder salesOrderObj = (SalesOrder) rdresult.getEntityList().get(0);

                            String custID = salesOrderObj.getCustomer().getID();
                            String custWarehouse = "";
                            if (salesOrderObj.getCustWarehouse() != null) {
                                custWarehouse = salesOrderObj.getCustWarehouse().getId();
                            }
                            String movementtype = "";

                            String createdby = sessionHandlerImpl.getUserid(request);
                            String modifiedby = sessionHandlerImpl.getUserid(request);
                            long createdon = System.currentTimeMillis();
                            long updatedon = System.currentTimeMillis();

                            double externalCurrencyRate = salesOrderObj.getExternalCurrencyRate();

                            String currencyid = (salesOrderObj.getCurrency() != null ? salesOrderObj.getCurrency().getCurrencyID() : sessionHandlerImpl.getCurrencyID(request));
                            Customer customer = new Customer();
                            String accountId = "";

                            HashMap<String, Object> invoicePrmt = new HashMap<String, Object>();
                            KwlReturnObject custresult = null;

                            if (!StringUtil.isNullOrEmpty(custID)) {
                                custresult = accountingHandlerDAOobj.getObject(Customer.class.getName(), custID);
                                customer = (Customer) custresult.getEntityList().get(0);
                                if (customer.getAccount() != null) {
                                    accountId = customer.getAccount().getID();
                                }
                            }

                            if (!StringUtil.isNullOrEmpty(accountId)) {
                                invoicePrmt.put("accountid", accountId);
                            }
                            if (!StringUtil.isNullOrEmpty(custID)) {
                                invoicePrmt.put("customerid", custID);
                            }

                            invoicePrmt.put("autogenerated", true);//Important for Next Auto Sequence Number
                            invoicePrmt.put("customerporefno", salesOrderObj.getCustomerPORefNo());

                            Calendar c = Calendar.getInstance();
                            c.setTime(new Date());
                            c.add(Calendar.DATE, customer.getCreditTerm().getTermdays());  // number of days to add
                            String dt = df.format(c.getTime());
                            Calendar c1 = Calendar.getInstance();
                            c1.setTime(new Date());
                            c.add(Calendar.DATE, customer.getCreditTerm().getTermdays());  // number of days to add
                            String dt1 = df.format(c1.getTime());
                            Date entryDate = authHandler.getUserDateFormatterWithoutTimeZone(request).parse(dt1);

                            //Entry No generate
                            int from = StaticValues.AUTONUM_DELIVERYORDER;

                            String nextAutoNoInt = "";
                            String datePrefix = "";
                            String dateafterPrefix = "";
                            String dateSuffix = "";
                            Map<String, Object> seqNumberMap = new HashMap<String, Object>();

                            synchronized (this) {

                                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, from, sequenceformatInvoice, false, entryDate);// when creation date is today sending null
                                nextAutoNumber = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                                nextAutoNoInt = (String) seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                                datePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                                dateafterPrefix = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                                dateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                            }

                            BillingShippingAddresses addresses = salesOrderObj.getBillingShippingAddresses();
                            invoicePrmt.put(InvoiceConstants.entrynumber, nextAutoNumber);
                            invoicePrmt.put("seqnumber", nextAutoNoInt);
                            invoicePrmt.put(Constants.DATEPREFIX, datePrefix);
                            invoicePrmt.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
                            invoicePrmt.put(Constants.DATESUFFIX, dateSuffix);
                            invoicePrmt.put("seqformat", sequenceformatInvoice);
                            invoicePrmt.put("isfavourite", salesOrderObj.isFavourite());

                            invoicePrmt.put("shipaddress", addresses == null ? "" : addresses.getShippingAddress());

                            if (!(salesOrderIdArray.size() > 1 && bulkInvoices)) {
                                invoicePrmt.put("shipvia", salesOrderObj.getShipvia());
                                invoicePrmt.put("fob", salesOrderObj.getFob());
                                invoicePrmt.put("salesPerson", salesOrderObj.getSalesperson() != null ? salesOrderObj.getSalesperson().getID() : "");
                                invoicePrmt.put(InvoiceConstants.memo, salesOrderObj.getMemo());
                                if (salesOrderObj.getShipdate() != null) {
                                    invoicePrmt.put(InvoiceConstants.shipdate, salesOrderObj.getShipdate());
                                }
                            } else {
                                invoicePrmt.put("shipvia", "");
                                invoicePrmt.put("fob", "");
                                invoicePrmt.put("salesPerson", "");
                                invoicePrmt.put(InvoiceConstants.memo, "");
                            }
                            invoicePrmt.put("companyid", companyid);
                            invoicePrmt.put("currencyid", currencyid);
                            invoicePrmt.put("externalCurrencyRate", externalCurrencyRate);
                            invoicePrmt.put("createdby", createdby);

                            invoicePrmt.put(InvoiceConstants.duedate, authHandler.getUserDateFormatterWithoutTimeZone(request).parse(dt));
                            invoicePrmt.put("modifiedby", modifiedby);
                            invoicePrmt.put("createdon", createdon);
                            invoicePrmt.put("updatedon", updatedon);
                            invoicePrmt.put("termid", customer.getCreditTerm().getID());
                            if (!StringUtil.isNullOrEmpty(custWarehouse)) {
                                invoicePrmt.put("custWarehouse", custWarehouse);
                            }
                            if (!StringUtil.isNullOrEmpty(movementtype)) {
                                invoicePrmt.put("movementtype", movementtype);
                            }
                            String taxid = "";
                            /*---If global level Tax is applied------*/
                            if (salesOrderObj.getTax() != null) {
                                taxid = salesOrderObj.getTax().getID();
                            }
                            invoicePrmt.put("taxid", taxid);

                            if (bulkInvoices) {
                                /*Fetching Sales order detais for all selected SO per customer */
                                result = accSalesOrderDAOObj.getSalesOrderDetailsForBulkInvoices(salesOrderIdArray, companyid);
                            } else {
                                /*Fetching Sales order detais per SO*/
                                result = accSalesOrderDAOObj.getSalesOrderDetails(salesOrderId, companyid);
                            }

                            invoicePrmt.put("orderdate", entryDate);

                            /*  Get Term details applied on Sales Order*/
                            JSONArray termsArr = getTermDetailsForSalesOrder(salesOrderObj.getID());
                            String deliveryOrderTerms = termsArr.toString();

                            /*----------Saving Term Amount---*/
                            if (StringUtil.isAsciiString(deliveryOrderTerms)) {
                                if (new JSONArray(deliveryOrderTerms).length() > 0) {
                                    //invoicePrmt.put(Constants.termsincludegst, Boolean.parseBoolean(paramJobj.optString(Constants.termsincludegst)));//need to check
                                }
                            }

                            /* Adding Delivery Order here*/
                            KwlReturnObject result2 = accInvoiceDAOobj.saveDeliveryOrder(invoicePrmt);
                            deliveryOrder = (DeliveryOrder) result2.getEntityList().get(0);//Create Invoice without invoice-details.
                            Set<JournalEntryDetail> inventoryJEdetails = new HashSet<>();

                            /*
                             * Check if there is any non-inventory present or not, as Inventory Journal Entry should not be posted for non-inventory products.
                             */
                            boolean postInventoryJournalEntry = false;

                            for (int k = 0; k < result.getEntityList().size(); k++) {
                                SalesOrderDetail salesOrderDetail = (SalesOrderDetail) result.getEntityList().get(k);

                                if (!StringUtil.isNullOrEmpty(salesOrderDetail.getProduct().getID())) {
                                    KwlReturnObject proresult = accountingHandlerDAOobj.getObject(Product.class.getName(), salesOrderDetail.getProduct().getID());
                                    Product product = (Product) proresult.getEntityList().get(0);
                                    if (product != null && product.getProducttype() != null && !(product.getProducttype().getID().equals(Producttype.SERVICE) || product.getProducttype().getID().equals(Producttype.NON_INVENTORY_PART) || product.getProducttype().getID().equals(Producttype.Inventory_Non_Sales))) {
                                        postInventoryJournalEntry = true;
                                        break;
                                    }
                                }
                            }

                            /*---Saving Inventory JE for Delivery Order-------*/
                            JournalEntry inventoryJE = null;
                            String inventoryjeid = "";
                            try {
                                if (extraCompanyPreferences != null && (extraCompanyPreferences.isActivateMRPModule() || preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD) && postInventoryJournalEntry) {
                                    String oldjeid1 = null;
                                    String jeentryNumber1 = null;
                                    boolean jeautogenflag1 = false;
                                    String jeIntegerPart1 = "";
                                    String jeDatePrefix1 = "";
                                    String jeAfterDatePrefix1 = "";
                                    String jeDateSuffix1 = "";
                                    String jeSeqFormatId1 = "";

                                    /*Check whether all required Information is getting saved in JE or not */
                                    Map<String, Object> jeDataMap1 = AccountingManager.getGlobalParams(request);

                                    if (StringUtil.isNullOrEmpty(oldjeid1)) {
                                        synchronized (this) {
                                            HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                                            JEFormatParams.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                                            JEFormatParams.put("modulename", "autojournalentry");
                                            JEFormatParams.put(Constants.companyKey, companyid);
                                            JEFormatParams.put("isdefaultFormat", true);
                                            KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                                            SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);

                                            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, entryDate);
                                            jeentryNumber1 = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                                            jeIntegerPart1 = (String) seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                                            jeDatePrefix1 = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                                            jeAfterDatePrefix1 = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                                            jeDateSuffix1 = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                                            jeSeqFormatId1 = format.getID();
                                            jeautogenflag1 = true;
                                        }
                                    }
                                    jeDataMap1.put("entrynumber", jeentryNumber1);
                                    jeDataMap1.put("autogenerated", jeautogenflag1);
                                    jeDataMap1.put(Constants.SEQFORMAT, jeSeqFormatId1);
                                    jeDataMap1.put(Constants.SEQNUMBER, jeIntegerPart1);
                                    jeDataMap1.put(Constants.DATEPREFIX, jeDatePrefix1);
                                    jeDataMap1.put(Constants.DATEAFTERPREFIX, jeAfterDatePrefix1);
                                    jeDataMap1.put(Constants.DATESUFFIX, jeDateSuffix1);
                                    jeDataMap1.put("entrydate", deliveryOrder.getOrderDate());//Need to check whether it is valid or not?
                                    jeDataMap1.put(Constants.companyKey, companyid);
                                    jeDataMap1.put("createdby", createdby);
                                    jeDataMap1.put(Constants.memo, salesOrderObj.getMemo());//check is it valid ?
                                    jeDataMap1.put(Constants.currencyKey, currencyid);
                                    jeDataMap1.put("costcenterid", salesOrderObj.getCostcenter());//check is it valid ?
                                    jeDataMap1.put("transactionModuleid", Constants.Acc_Delivery_Order_ModuleId);
                                    jeDataMap1.put("transactionId", deliveryOrder.getID());
                                    KwlReturnObject jeresult1 = accJournalEntryobj.saveJournalEntry(jeDataMap1);
                                    inventoryJE = (JournalEntry) jeresult1.getEntityList().get(0);
                                    inventoryjeid = inventoryJE.getID();
                                    deliveryOrder.setInventoryJE(inventoryJE);
                                }
                            } catch (Exception ex) {
                                Logger.getLogger(accInvoiceController.class.getName()).log(Level.WARNING, ex.getMessage());
                            }

                            /* Function to save Delivery Order details*/
                            List dll = saveDeliveryOrderDetail(request, result, companyid, deliveryOrder, inventoryJEdetails, inventoryjeid, nextAutoNumber);

                            /*--------Adding Inventory Details Here----------*/
                            if (inventoryJE != null && (extraCompanyPreferences.isActivateMRPModule() || preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD)) {
                                inventoryJE.setDetails(inventoryJEdetails);
                                accJournalEntryobj.saveJournalEntryDetailsSet(inventoryJEdetails);
                            }

                            /*  Setting Rows in delivery Order*/
                            HashSet<DeliveryOrderDetail> deliveryOrderdetails = (HashSet<DeliveryOrderDetail>) dll.get(1);
                            deliveryOrder.setRows(deliveryOrderdetails);

                            Iterator doDetailsItr = deliveryOrderdetails.iterator();

                            /*-----Getting Unique Ids of product----*/
                            while (doDetailsItr.hasNext()) {
                                DeliveryOrderDetail doDetail = (DeliveryOrderDetail) doDetailsItr.next();
                                if (productIds.indexOf(doDetail.getProduct().getID()) == -1) {
                                    productIds += doDetail.getProduct().getID() + ",";
                                }
                            }

                            if (!StringUtil.isNullOrEmpty(productIds)) {
                                productIds = productIds.substring(0, productIds.length() - 1);
                            }

                            /*-------Unique Ids of Product End-------*/
                            HashMap<String, Object> GlobalParams = AccountingManager.getGlobalParams(request);
                            double totalAmount = 0;
                            double subtotal = 0;
                            double quantity = 0;
                            double discountAmount = 0;
                            double discountAmountInBase = 0;
                            double taxAmount = 0;
                            String productId = "";

                            if (deliveryOrderdetails != null && !deliveryOrderdetails.isEmpty()) {
                                for (DeliveryOrderDetail cnt : deliveryOrderdetails) {
                                    /*
                                     * Check if  "Include GST" for rate  
                                     * */
                                    double rate = 0;
                                    if (salesOrderObj.isGstIncluded()) {
                                        rate = cnt.getRateincludegst();
                                    } else {
                                        rate = cnt.getRate();
                                        taxAmount += authHandler.round(cnt.getRowTaxAmount(), companyid);
                                    }
                                    productId = cnt.getInventory().getProduct().getID();
                                    quantity = cnt.getInventory().getQuantity();
                                    totalAmount += authHandler.round(rate * quantity, companyid);
                                    subtotal = authHandler.round(rate * quantity, companyid);

                                    double rowDiscVal = 0;
                                    if (cnt.getDiscountispercent() == 1) {
                                        rowDiscVal = authHandler.round((subtotal * cnt.getDiscount() / 100), companyid);
                                        discountAmount += rowDiscVal;
                                    } else {
                                        rowDiscVal = authHandler.round(cnt.getDiscount(), companyid);
                                        discountAmount += rowDiscVal;
                                    }
                                    // Mapping Product and Discount
                                    KwlReturnObject dAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, rowDiscVal, currencyid, salesOrderObj.getOrderDate(), deliveryOrder.getExternalCurrencyRate());
                                    double discAmountinBase = (Double) dAmount.getEntityList().get(0);
                                    discAmountinBase = authHandler.round(discAmountinBase, companyid);
                                    JSONObject productDiscountObj = new JSONObject();
                                    productDiscountObj.put("productId", productId);
                                    productDiscountObj.put("discountAmount", discAmountinBase);
                                    productDiscountJArr.put(productDiscountObj);
                                }
                            }
                            totalAmountInDocumentCurrecy = totalAmount - discountAmount;
                            double taxPercent = 0;

                            /*----Global Level Tax---*/
                            if (salesOrderObj.getTax() != null) {
                                KwlReturnObject taxresult = accTaxObj.getTaxPercent(sessionHandlerImpl.getCompanyid(request), salesOrderObj.getOrderDate(), salesOrderObj.getTax().getID());
                                taxPercent = (Double) taxresult.getEntityList().get(0);
                                double ordertaxamount = (taxPercent == 0 ? 0 : authHandler.round((totalAmountInDocumentCurrecy * taxPercent / 100), companyid));
                                taxAmount += ordertaxamount;
                            }
                            totalAmountInDocumentCurrecy = totalAmountInDocumentCurrecy + taxAmount;
                            double totalAmountInBaseCurrecy = 0;

                            if (deliveryOrder.getCurrency() != null) {

                                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, totalAmountInDocumentCurrecy, deliveryOrder.getCurrency().getCurrencyID(), deliveryOrder.getOrderDate(), deliveryOrder.getExternalCurrencyRate());
                                totalAmountInBaseCurrecy = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                                KwlReturnObject bAmt1 = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, discountAmount, deliveryOrder.getCurrency().getCurrencyID(), deliveryOrder.getOrderDate(), deliveryOrder.getExternalCurrencyRate());
                                discountAmountInBase = authHandler.round((Double) bAmt1.getEntityList().get(0), companyid);

                                JSONObject productDiscountObj = new JSONObject();
                                productDiscountObj.put("productId", productId);
                                productDiscountObj.put("discountAmount", discountAmountInBase);
                                productDiscountJArr.put(productDiscountObj);
                            }
                            totalAmountInDocumentCurrecy = authHandler.round(totalAmountInDocumentCurrecy, companyid);

                            deliveryOrder.setDiscountinbase(discountAmountInBase);
                            deliveryOrder.setTotalamount(totalAmountInDocumentCurrecy);
                            deliveryOrder.setTotalamountinbase(totalAmountInBaseCurrecy);
                            //deliveryOrder.setApprovestatuslevel(11);

                            /* ----Invoice Term Related Code---- */
                            if (StringUtil.isAsciiString(deliveryOrderTerms)) {
                                Map<String, Object> termMap = new HashMap<>();
                                termMap.put("invoiceterms", deliveryOrderTerms);
                                termMap.put("userid", userId);
                                termMap.put("isdo", true);
                                termMap.put("transactionid", deliveryOrder.getID());
                                accInvoiceModuleService.mapInvoiceTerms(termMap);
                            }
                            /* ----Invoice Term Related Code Ends here---- */


                            /*-------Saving Global level Custom/Dimension field for Delivery Order-------------*/
                            if (!bulkInvoices) {
                                HashMap<String, Object> custommParams = new HashMap<String, Object>();
                                custommParams.put("companyid", companyid);
                                custommParams.put("salesOrderObj", salesOrderObj);
                                custommParams.put("salesOrderObj", salesOrderObj);
                                custommParams.put("deliveryOrder", deliveryOrder);

                                saveCustomDimensionFieldOfDO(custommParams);
                            }

                            /*-------End Of Saving Global level Custom/Dimension field for Delivery Order-------------*/
                            /*---Udating Delivery Order------- */
                            result = accInvoiceDAOobj.updateDeliveryOrder(deliveryOrder);

                            /*------Code for Updating Sales Order Status(linkflag->2 & isopen->T--------*/
                            if (bulkInvoices) {//Bulk DO case
                                for (int ii = 0; ii < salesOrderIdArray.size(); ii++) {

                                    KwlReturnObject rdresult1 = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), salesOrderIdArray.get(ii).toString());
                                    SalesOrder salesOrderObj1 = (SalesOrder) rdresult1.getEntityList().get(0);
                                    hMap.put("salesOrder", salesOrderObj1);
                                    hMap.put("value", "2");
                                    hMap.put("isSOOpen", false);
                                    accInvoiceDAOobj.updateSOLinkflag(hMap);
                                }
                            } else {//Individual DO Case

                                hMap.put("salesOrder", salesOrderObj);
                                hMap.put("value", "2");
                                hMap.put("isSOOpen", false);
                                accInvoiceDAOobj.updateSOLinkflag(hMap);
                            }
                            /*------Code for Updating Sales Order Status(linkflag->2 & isopen->'F' Ends Here--------*/

                            if (bulkInvoices) {

                                invoiceNo += nextAutoNumber + ", ";

                            } else {
                                invoiceNo += nextAutoNumber + ", ";

                            }
                            String action = "";
                            if (!bulkInvoices) {
                                action = "created Individual Delivery Order(s)";
                                auditTrailObj.insertAuditLog(AuditAction.DELIVERY_ORDER, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + "" + " " + nextAutoNumber + "", request, nextAutoNumber);

                            }

                            if (bulkInvoices) {

                                for (int l = 0; l < salesOrderNoArray.size(); l++) {
                                    /*
                                     * saving linking informaion of Sales Order while
                                     * linking with Delivery Order
                                     */

                                    HashMap<String, Object> requestParamsLinking = new HashMap<String, Object>();
                                    requestParamsLinking.put("linkeddocid", deliveryOrder.getID());
                                    requestParamsLinking.put("docid", salesOrderObj.getID());
                                    requestParamsLinking.put(Constants.moduleid, Constants.Acc_Delivery_Order_ModuleId);
                                    requestParamsLinking.put("linkeddocno", nextAutoNumber);
                                    requestParamsLinking.put("sourceflag", 0);
                                    KwlReturnObject result3 = accSalesOrderDAOObj.saveSalesOrderLinking(requestParamsLinking);


                                    /*
                                     * saving linking informaion of Delivery Order while
                                     * linking with Sales Order
                                     */
                                    requestParamsLinking.put("linkeddocid", salesOrderIdArray.get(l));
                                    requestParamsLinking.put("docid", deliveryOrder.getID());
                                    requestParamsLinking.put(Constants.moduleid, Constants.Acc_Sales_Order_ModuleId);
                                    requestParamsLinking.put("linkeddocno", salesOrderNoArray.get(l));
                                    requestParamsLinking.put("sourceflag", 1);
                                    result3 = accInvoiceDAOobj.saveDeliveryOrderLinking(requestParamsLinking);
                                }

                            } else {
                                /*
                                 * saving linking informaion of Sales Order while
                                 * linking with Delivery Order
                                 */

                                HashMap<String, Object> requestParamsLinking = new HashMap<String, Object>();
                                requestParamsLinking.put("linkeddocid", deliveryOrder.getID());
                                requestParamsLinking.put("docid", salesOrderObj.getID());
                                requestParamsLinking.put(Constants.moduleid, Constants.Acc_Delivery_Order_ModuleId);
                                requestParamsLinking.put("linkeddocno", nextAutoNumber);
                                requestParamsLinking.put("sourceflag", 0);
                                KwlReturnObject result3 = accSalesOrderDAOObj.saveSalesOrderLinking(requestParamsLinking);


                                /*
                                 * saving linking informaion of Delivery Order while
                                 * linking with Sales Order
                                 */
                                requestParamsLinking.put("linkeddocid", salesOrderObj.getID());
                                requestParamsLinking.put("docid", deliveryOrder.getID());
                                requestParamsLinking.put(Constants.moduleid, Constants.Acc_Sales_Order_ModuleId);
                                requestParamsLinking.put("linkeddocno", salesOrderObj.getSalesOrderNumber());
                                requestParamsLinking.put("sourceflag", 1);
                                result3 = accInvoiceDAOobj.saveDeliveryOrderLinking(requestParamsLinking);
                            }

                            /* Leaving from inner loop as only one invoice(in case of bulk) per customer 
                      
                             irrespective of the no of selected SO
                      
                             */
                            /*  Code for showing DO in main tab or pending tab */
                            HashMap<String, Object> doApproveMap = new HashMap<String, Object>();
                            doApproveMap.put(Constants.companyKey, companyid);
                            doApproveMap.put("level", 0);
                            doApproveMap.put("totalAmount", String.valueOf(totalAmountInDocumentCurrecy));
                            doApproveMap.put("currentUser", userId);
                            doApproveMap.put("fromCreate", true);
                            doApproveMap.put("productDiscountMapList", productDiscountJArr);
                            doApproveMap.put(Constants.moduleid, Constants.Acc_Delivery_Order_ModuleId);
                            doApproveMap.put(Constants.PAGE_URL, URLUtil.getPageURL(request, loginpageFull));
                            List approvedlevel = approveDO(deliveryOrder, doApproveMap, false);
                            int approvalstatus = (Integer) approvedlevel.get(0);

                            if (approvalstatus != 11) {
                                pendingDeliveryOrders += deliveryOrder.getDeliveryOrderNumber() + ",";
                            }

                            if (bulkInvoices) {

                                break;
                            }
                            /* If creating individual Invoices per SO */
                            if (!bulkInvoices) {
                                /* Committing transaction while creating individual invoices*/
                                txnManager.commit(status);
                            }

                        } catch (Exception ex) {
                            if (status != null) {
                                txnManager.rollback(status);
                            }
                            msg = "" + ex.getMessage();
                            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                        }
                    }

                    /* If creating bulk Invoices per customer */
                    if (bulkInvoices) {

                        /* Committing transaction while creating bulk invoices*/
                        txnManager.commit(status);
                    }
                } catch (Exception ex) {
                    if (status != null) {
                        txnManager.rollback(status);
                    }
                    msg = "" + ex.getMessage();
                    Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                }

                j++;

            }

            invoiceNo = invoiceNo.substring(0, (invoiceNo.lastIndexOf(",")));
            issuccess = true;
            jobj.put("productIds", productIds);

            try {
                String action = "";
                if (bulkInvoices) {
                    //status = txnManager.getTransaction(def);
                    action = "created Bulk Delivery Order(s)";
                    auditTrailObj.insertAuditLog(AuditAction.DELIVERY_ORDER, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + "" + " " + invoiceNo + "", request, invoiceNo);
                    // txnManager.commit(status);
                }
            } catch (Exception ex) {
                if (status != null) {
                    txnManager.rollback(status);
                }
                msg = "" + ex.getMessage();
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }

            msg = "Delivery Order(s) generated successfully.<br><b> Delivery Order No(s):</b> " + "<font style=\"word-break: break-all;\">" + invoiceNo + "." + "</font>";
            /* Preparing message for pending DO(s)*/
            if (!StringUtil.isNullOrEmpty(pendingDeliveryOrders)) {
                msg += "<br>";
                msg += "<br>";
                msg += "<b>Note:</b> " + "Delivery Order(s) went for pending approval.";
                msg += "<br>";
                msg += "<b>Delivery Order No(s):</b> " + "<font style=\"word-break: break-all;\">" + pendingDeliveryOrders.substring(0, pendingDeliveryOrders.length() - 1) + "." + "</font>";
            }
        } catch (SessionExpiredException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return new ModelAndView("jsonView", "model", jobj.toString());

    }

    /*-------Saving Global level Custom/Dimension field for Delivery Order-------------*/
    public void saveCustomDimensionFieldOfDO(HashMap<String, Object> customParams) throws ServiceException {

        SalesOrder salesOrderObj = (SalesOrder) customParams.get("salesOrderObj");
        String companyid = (String) customParams.get("companyid");
        DeliveryOrder deliveryOrder = (DeliveryOrder) customParams.get("deliveryOrder");

        HashMap<String, String> customFieldMap = new HashMap<String, String>();
        HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
        HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
        HashMap<String, Object> fieldrequestParams = new HashMap();
        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
        fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Sales_Order_ModuleId));
        HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
        try {
            JSONObject obj = new JSONObject();
            /* Getting Sales Order Custom/Dimension field*/
            Map<String, Object> variableMap = new HashMap<String, Object>();
            SalesOrderCustomData jeDetailCustom = (SalesOrderCustomData) salesOrderObj.getSoCustomData();
            replaceFieldMap = new HashMap<String, String>();
            if (jeDetailCustom != null) {
                AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                boolean isExport = false;
                JSONObject params = new JSONObject();
                params.put(Constants.companyKey, companyid);
                params.put(Constants.isExport, isExport);
                params.put(Constants.isdefaultHeaderMap, false);

                params.put(Constants.browsertz, "");

                fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
            }

            /* Write a function to fetch custom field from fieldparams against fieldlabel
             where moduleid will be invoice  
                          
             */
            HashMap requestParams1 = new HashMap();
            JSONArray jcustomarray = new JSONArray();
            JSONObject jsonObject = new JSONObject();
            Iterator ite = obj.keys();
            String fieldLabel = "";
            String fieldValue = "";

            requestParams1.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_field, Constants.companyid, Constants.moduleid));
            FieldParams fieldParams = null;
            while (ite.hasNext()) {
                fieldLabel = (String) ite.next();
                fieldValue = obj.getString(fieldLabel);

                requestParams1.put(Constants.filter_values, Arrays.asList(fieldLabel, companyid, Constants.Acc_Delivery_Order_ModuleId));

                /* Fetching custom/dimension field "field Label" wise */
                List result1 = accAccountDAOobj.getFieldParamsFieldLabelWise(requestParams1); // get custom field module wise from fieldlabel
                if (result1 != null && result1.size() > 0) {

                    fieldParams = (FieldParams) result1.get(0);

                    jsonObject.put("fieldid", fieldParams.getId());

                    /* If multiselect drop down then fetching actual value instead of id */
                    if (fieldParams.getFieldtype() == Constants.MULTISELECTCOMBO || fieldParams.getFieldtype() == Constants.FIELDSET) {

                        HashMap<String, Object> fieldParamsMap = new HashMap<String, Object>();
                        fieldParamsMap.put("companyid", companyid);
                        fieldParamsMap.put("moduleid", Constants.Acc_Sales_Order_ModuleId);
                        fieldParamsMap.put("filedname", fieldLabel);
                        String multiSelectId = fieldManagerDAOobj.getFieldParamsId(fieldParamsMap);
                        fieldValue = fieldManagerDAOobj.getParamsValue(multiSelectId, fieldValue);
                    }

                    jsonObject.put("xtype", fieldParams.getFieldtype());
                    jsonObject.put("fieldname", fieldParams.getFieldname());
                    jsonObject.put(fieldParams.getFieldname(), "Col" + fieldParams.getColnum());
                    jsonObject.put("refcolumn_name", "Col" + fieldParams.getRefcolnum());

                    if (fieldParams.getFieldtype() == Constants.SINGLESELECTCOMBO || fieldParams.getFieldtype() == Constants.MULTISELECTCOMBO || fieldParams.getFieldtype() == Constants.FIELDSET) {
                        String ids = fieldManagerDAOobj.getIdsUsingParamsValue(fieldParams.getId(), fieldValue);     // get ids for module using values and field id  
                        fieldValue = ids;

                    }
                    jsonObject.put("Col" + fieldParams.getColnum(), fieldValue);
                    jsonObject.put("fieldDataVal", fieldValue);

                    jcustomarray.put(jsonObject);
                }

                /* Saving Custom field for Delivery Order module */
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_DeliveryOrder_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_DeliveryOrderid);
                customrequestParams.put("modulerecid", deliveryOrder.getID());
                customrequestParams.put(Constants.moduleid, Constants.Acc_Delivery_Order_ModuleId);
                customrequestParams.put(Constants.companyKey, companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_DeliveryOrder_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    HashMap<String, Object> doDataMap = new HashMap<String, Object>();
                    doDataMap.put("accadeliveryordercustomdataref", deliveryOrder.getID());
                    KwlReturnObject accresult = accInvoiceDAOobj.updateDeliveryOrderCustomData(doDataMap);
                }

            }
        } catch (JSONException ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /* Fetching Term details for Sales Order */
    public JSONArray getTermDetailsForSalesOrder(String id) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            HashMap<String, Object> requestParam = new HashMap();

            requestParam.put("salesOrder", id);
            KwlReturnObject curresult = accSalesOrderDAOObj.getSalesOrderTermMap(requestParam);
            List<SalesOrderTermMap> termMap = curresult.getEntityList();
            for (SalesOrderTermMap SalesOrderTermMap : termMap) {
                InvoiceTermsSales mt = SalesOrderTermMap.getTerm();
                JSONObject jsonobj = new JSONObject();
                jsonobj.put("id", mt.getId());
                jsonobj.put("term", mt.getTerm());
                jsonobj.put("glaccount", mt.getAccount().getID());
                jsonobj.put("sign", mt.getSign());
                jsonobj.put("formula", mt.getFormula());
                jsonobj.put("termpercentage", SalesOrderTermMap.getPercentage());
                jsonobj.put("termamount", SalesOrderTermMap.getTermamount());
                jArr.put(jsonobj);
            }

        } catch (JSONException ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }

    private HashSet<InvoiceDetail> updateInvoiceRows(Invoice invoice, String invoiceDetails, JournalEntry je, int moduleid, String companyid) throws ServiceException, JSONException {
        HashSet<InvoiceDetail> rows = new HashSet<InvoiceDetail>();
        try {
            JSONArray jArr = new JSONArray(invoiceDetails);
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                String linkto = jobj.getString("linkto");
                InvoiceDetail row = null;
                if (jobj.has("rowid")) {
                    KwlReturnObject invDetailsResult = accountingHandlerDAOobj.getObject(InvoiceDetail.class.getName(), StringUtil.isNullOrEmpty(linkto) ? jobj.getString("rowid") : jobj.getString("docrowid"));
                    row = (InvoiceDetail) invDetailsResult.getEntityList().get(0);
                }

                /*
                 * To change the sequence of product
                 */
                if (row != null) {
                    if (jobj.has("srno")) {
                        row.setSrno(jobj.getInt("srno"));
                    }

                    /*
                     * We can update the descritpion of line item.
                     */
                    if (!StringUtil.isNullOrEmpty(jobj.optString("desc"))) {
                        try {
                            row.setDescription(StringUtil.DecodeText(jobj.optString("desc")));
                        } catch (Exception ex) {
                            row.setDescription(jobj.optString("desc"));
                        }
                    } else {
                        row.setDescription(null);
                    }

                    /*
                     * To update the custom field data of line items.
                     */
                    JSONObject jedjson = new JSONObject();
                    if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", ""))) {
                        JSONArray jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                        customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);
                        customrequestParams.put("modulerecid", row.getSalesJED().getID()); //Pls confirm
                        customrequestParams.put("recdetailId", row.getInventory().getID());
                        customrequestParams.put("moduleid", moduleid);
                        customrequestParams.put("companyid", companyid);
                        customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            jedjson.put("accjedetailcustomdata", row.getSalesJED().getID());
                            jedjson.put("jedid", row.getSalesJED().getID());
                            KwlReturnObject jedresult = accJournalEntryobj.updateJournalEntryDetails(jedjson);
                        }
                        /*
                         * Updating dimension value for tax entry. ERP-34578
                         */
                        if (row.getGstJED() != null) {
                            customrequestParams.put("modulerecid", row.getGstJED().getID()); //Pls confirm
                            customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                jedjson.put("accjedetailcustomdata", row.getGstJED().getID());
                                jedjson.put("jedid", row.getGstJED().getID());
                                accJournalEntryobj.updateJournalEntryDetails(jedjson);
                            }
                        }

                        HashMap<String, Object> params = new HashMap<String, Object>();
                        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                        filter_names.add("mainjedid");
                        filter_params.add(row.getSalesJED().getID());
                        params.put("filter_names", filter_names);
                        params.put("filter_params", filter_params);

                        KwlReturnObject separatedJed = accJournalEntryobj.getJournalEntryDetails(params);
                        if (separatedJed.getEntityList() != null && separatedJed.getEntityList().size() > 0) {
                            /*
                             * Tagging new dimension value to additional
                             * jedetail against control account in linking case.
                             */
                            List<JournalEntryDetail> separatedJedList = separatedJed.getEntityList();
                            for (JournalEntryDetail separatedjed : separatedJedList) {
                                customrequestParams.put("modulerecid", separatedjed.getID());
                                customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                    /**
                                     * SDP-14699 : Create New 'jedjson' Object
                                     * for updating existing JED.
                                     */
                                    jedjson = new JSONObject();
                                    jedjson.put("accjedetailcustomdata", separatedjed.getID());
                                    jedjson.put("jedid", separatedjed.getID());
                                    accJournalEntryobj.updateJournalEntryDetails(jedjson);
                                }
                            }
                        } else {
                            /*
                             * If dimension value is not tagged while creating
                             * PI then separated jed doesn't get post. Post that
                             * additional jedetail and also tag dimension value.
                             */
                            jedjson = new JSONObject();
                            jedjson.put("srno", 1);
                            jedjson.put("companyid", row.getSalesJED().getCompany().getCompanyID());
                            jedjson.put("amount", row.getSalesJED().getAmount());
                            jedjson.put("accountid", row.getInvoice().getAccount().getID());
                            jedjson.put("debit", !row.getSalesJED().isDebit());
                            jedjson.put("jeid", row.getSalesJED().getJournalEntry().getID());
                            jedjson.put("mainjedid", row.getSalesJED().getID());
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
                                /*
                                 * Tagging new dimension value to additional
                                 * jedetail against tax account in linking case.
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
                                 * If dimension value is not tagged while
                                 * creating PI then separated jed doesn't get
                                 * post. Post that additional jedetail and also
                                 * tag dimension value.
                                 */
                                jedjson = new JSONObject();
                                jedjson.put("srno", 1);
                                jedjson.put("companyid", row.getGstJED().getCompany().getCompanyID());
                                jedjson.put("amount", row.getGstJED().getAmount());
                                jedjson.put("accountid", row.getInvoice().getAccount().getID());
                                jedjson.put("debit", !row.getGstJED().isDebit());
                                jedjson.put("jeid", row.getGstJED().getJournalEntry().getID());
                                jedjson.put("mainjedid", row.getGstJED().getID());
                                jedjson.put(Constants.ISSEPARATED, true);
                                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                JournalEntryDetail pmAmountJed = (JournalEntryDetail) jedresult.getEntityList().get(0);

                                customrequestParams.put("modulerecid", pmAmountJed.getID()); //Pls confirm
                                customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                    /**
                                     * SDP-14699 : Create New 'jedjson' Object
                                     * for updating existing JED.
                                     */
                                    jedjson = new JSONObject();
                                    jedjson.put("accjedetailcustomdata", pmAmountJed.getID());
                                    jedjson.put("jedid", pmAmountJed.getID());
                                    accJournalEntryobj.updateJournalEntryDetails(jedjson);
                                }
                            }
                        }
                    }

                    /*
                     * To update the custom field data of product level items.
                     */
                    if (!StringUtil.isNullOrEmpty(jobj.optString("productcustomfield", ""))) {
                        JSONArray jcustomarray = new JSONArray(jobj.optString("productcustomfield", "[]"));
                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                        customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);
                        customrequestParams.put("modulerecid", row.getSalesJED().getID());
                        customrequestParams.put("moduleid", Constants.Acc_Invoice_ModuleId);
                        customrequestParams.put("recdetailId", row.getInventory().getID());
                        customrequestParams.put("productId", row.getInventory().getProduct().getID());
                        customrequestParams.put("companyid", companyid);
                        customrequestParams.put("customdataclasspath", Constants.Acc_JEDetail_Productcustom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            /**
                             * SDP-14699 : Create New 'jedjson' Object for
                             * updating existing JED.
                             */
                            jedjson = new JSONObject();
                            jedjson.put("accjedetailproductcustomdataref", row.getSalesJED().getID());
                            jedjson.put("jedid", row.getSalesJED().getID());
                            KwlReturnObject jedresult = accJournalEntryobj.updateJournalEntryDetails(jedjson);
                        }
                    }
                    rows.add(row);
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("updateInvoiceRows : " + ex.getMessage(), ex);
        } catch (Exception e) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, e);
        }
        return rows;
    }

    public List updateInvoice(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException, UnsupportedEncodingException {
        String id = null;
        KwlReturnObject result = null;
        List ll = new ArrayList();
        ArrayList discountArr = new ArrayList();
        String invoiceid = null;
        Invoice invoice = null;
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            DateFormat userdf = authHandler.getUserDateFormatter(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            invoiceid = request.getParameter("invoiceid");
            boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false;
            boolean isLeaseFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isLeaseFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isLeaseFixedAsset")) : false;
            boolean isConsignment = (!StringUtil.isNullOrEmpty(request.getParameter("isConsignment"))) ? Boolean.parseBoolean(request.getParameter("isConsignment")) : false;
            int moduleid = isFixedAsset ? Constants.Acc_FixedAssets_DisposalInvoice_ModuleId : isConsignment ? Constants.Acc_ConsignmentInvoice_ModuleId : isLeaseFixedAsset ? Constants.LEASE_INVOICE_MODULEID : Constants.Acc_Invoice_ModuleId;
            /*
             * To update the following items which is not affecting the amount
             * and linking of the Invoice.
             */

            JSONObject invoicePrmt = new JSONObject();
            if (!StringUtil.isNullOrEmpty(invoiceid)) {
                invoicePrmt.put("invoiceid", invoiceid);
            }
            invoicePrmt.put(InvoiceConstants.memo, request.getParameter("memo") == null ? "" : request.getParameter("memo"));
            invoicePrmt.put("billto", request.getParameter("billto") == null ? "" : request.getParameter("billto"));
            invoicePrmt.put("shipaddress", request.getParameter("shipaddress") == null ? "" : request.getParameter("shipaddress"));
            if (request.getParameter(InvoiceConstants.shipdate) != null && !StringUtil.isNullOrEmpty(request.getParameter(InvoiceConstants.shipdate))) {
                invoicePrmt.put(InvoiceConstants.shipdate, df.parse(request.getParameter(InvoiceConstants.shipdate)));//ERP-24687
            }
            invoicePrmt.put("porefno", request.getParameter("porefno") == null ? "" : request.getParameter("porefno"));
            invoicePrmt.put("companyid", companyid);
            invoicePrmt.put("salesPerson", request.getParameter("salesPerson") == null ? "" : request.getParameter("salesPerson"));
            invoicePrmt.put("shipvia", request.getParameter("shipvia"));
            invoicePrmt.put("fob", request.getParameter("fob") == null ? "" : request.getParameter("fob"));
            invoicePrmt.put("modifiedby", sessionHandlerImpl.getUserid(request));
            invoicePrmt.put("updatedon", System.currentTimeMillis());
            invoicePrmt.put("posttext", request.getParameter("posttext"));
            invoicePrmt.put("costcenterid", request.getParameter("costcenter"));
            String formtype = request.getParameter("formtypeid");
            String excisetypeid = !StringUtil.isNullOrEmpty(request.getParameter("excisetypeid")) ? request.getParameter("excisetypeid") : null;
            if (!StringUtil.isNullOrEmpty(request.getParameter("deliveryTime"))) {
                invoicePrmt.put("deliveryTime", request.getParameter("deliveryTime"));
            }
            
            KwlReturnObject invResult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceid);
            invoice = (Invoice) invResult.getEntityList().get(0);

            Map<String, Object> addressParams = new HashMap<String, Object>();
            String billingAddress = request.getParameter(Constants.BILLING_ADDRESS);
            if (!StringUtil.isNullOrEmpty(billingAddress)) {
                addressParams = AccountingAddressManager.getAddressParams(request, false);
            } else {
                addressParams = AccountingAddressManager.getDefaultCustomerAddressParams(invoice.getCustomer().getID(), companyid, accountingHandlerDAOobj);// addressParams = getCustomerDefaultAddressParams(customer,companyid);
            }
            BillingShippingAddresses bsa = invoice.getBillingShippingAddresses();//used to update billing shipping addresses
            addressParams.put("id", bsa != null ? bsa.getID() : "");
            KwlReturnObject addressresult = accountingHandlerDAOobj.saveAddressDetail(addressParams, companyid);
            bsa = (BillingShippingAddresses) addressresult.getEntityList().get(0);
            String addressid = bsa.getID();
            invoicePrmt.put("billshipAddressid", addressid);
            Map<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            String transactionDateStr = request.getParameter("billdate");
            if (!StringUtil.isNullOrEmpty(transactionDateStr)) {
                Date transactionDate = df.parse(transactionDateStr);

                //ERROR PRONE CODE. VERIFY IT CAREFULLY - Book Begining Date & Transaction Date.
                transactionDate = CompanyPreferencesCMN.removeTimefromDate(transactionDate);

                CompanyPreferencesCMN.checkLockPeriod(accCompanyPreferencesObj, requestParams, transactionDate, true);
            }
            /*
             * Updating line item information.
             */
            String invoiceDetails = request.getParameter("detail");
            HashSet<InvoiceDetail> invDetails = updateInvoiceRows(invoice, invoiceDetails, invoice.getJournalEntry(), moduleid, companyid);
            /*
             * Updating Custom field data.
             */
            Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
            String customfield = request.getParameter("customfield");
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                customrequestParams.put("modulerecid", invoice.getJournalEntry().getID());
                customrequestParams.put("moduleid", moduleid);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    jeDataMap.put("accjecustomdataref", invoice.getJournalEntry().getID());
                    jeDataMap.put("jeid", invoice.getJournalEntry().getID());
                    jeDataMap.put("entrydate", invoice.getJournalEntry().getEntryDate());
                    KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);
                }
            }
                if (invoice.getCompany().getCountry() != null && invoice.getCompany().getCountry().getID().equals(String.valueOf(Constants.indian_country_id))) {
                if (StringUtil.isNullOrEmpty(formtype)) {
                    invoicePrmt.put("excisetypeid", excisetypeid);
                }
                invoicePrmt.put("excisetypeid", excisetypeid);
            }
         /* check for dublication of customer PORefNo in linked invoiced edit-case*/
            ExtraCompanyPreferences extraCompanyPreferences = null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
                JSONObject jObj = new JSONObject((String) extraCompanyPreferences.getColumnPref()); 
            if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref()) && jObj.has(Constants.customerPoReferenceNo) && jObj.get(Constants.customerPoReferenceNo) != null && (Boolean) jObj.get(Constants.customerPoReferenceNo) != false) { 
                    String CustomerPORefNo = request.getParameter("porefno") != null ? request.getParameter("porefno") : "";
                    String salesInvNo = "", salesInvNoTotal = "";
                    String invid = request.getParameter("invoiceid") != null ? request.getParameter("invoiceid") : "";
                    if (!StringUtil.isNullOrEmpty(CustomerPORefNo)) {
                        JSONObject reqParams = new JSONObject();
                        reqParams.put("CustomerPORefNo", CustomerPORefNo);
                        reqParams.put("companyid", companyid);
                        reqParams.put("invid", invid);
                        result = accInvoiceDAOobj.getInvoiceFromCustomerPORefNo(reqParams);
                        if (result != null && result.getRecordTotalCount() > 0) {
                            List<String> li = result.getEntityList();
                            if (!li.isEmpty()) {
                                for (String obj : li) {
                                    salesInvNo = !StringUtil.isNullOrEmpty(obj) ? obj : "";
                                    salesInvNoTotal += salesInvNo + ",";
                                }
                               throw new AccountingException(messageSource.getMessage("acc.gst.CustPORefErrorSI", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))) + " <b>" + salesInvNoTotal.substring(0, salesInvNoTotal.length() - 1) + "<b>");
                             }
                        }
                    }
            }     
             result = accInvoiceDAOobj.updateInvoice(invoicePrmt, invDetails);
            invoice = (Invoice) result.getEntityList().get(0);//Create Invoice without invoice-details.
            id = invoice.getID();
            /*
             * Data for return information.
             */
            String personalid = invoice.getCustomer().getAccount().getID();
            String accname = invoice.getCustomer().getAccount().getName();
            String invoiceno = invoice.getInvoiceNumber();
            String address = invoice.getCustomer().getBillingAddress();
            String fullShippingAddress = "";
            if (invoice.getBillingShippingAddresses() != null) {
                fullShippingAddress = invoice.getBillingShippingAddresses().getFullShippingAddress();
            }
            /*
             Set cost center value 
             */
            if (!StringUtil.isNullOrEmpty(request.getParameter("costcenter")) && invoice.getJournalEntry() != null) {
                KwlReturnObject cap = accountingHandlerDAOobj.getObject(CostCenter.class.getName(), request.getParameter("costcenter"));
                CostCenter costCenter = (CostCenter) cap.getEntityList().get(0);
                invoice.getJournalEntry().setCostcenter(costCenter);
            } else {
                invoice.getJournalEntry().setCostcenter(null);
            }
            tranID = id;
            recId = invoiceno;
            ll.add(new String[]{id, ""});
            ll.add(discountArr);
            ll.add((invoice.getPendingapproval() == 1) ? "Pending Approval" : "Approved");
            ll.add(personalid);
            ll.add(accname);
            ll.add(invoiceno);
            ll.add(address);
            ll.add(invoice.getInvoiceamount());
            ll.add(invoice.getJournalEntry().getEntryNumber());
            ll.add(fullShippingAddress);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (AccountingException e) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, e);
            throw ServiceException.FAILURE(e.getMessage(), "erp24", false);
        } catch (Exception e) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, e);
        }
        return ll;
    }

    public ModelAndView updateLinkedInvoice(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        boolean isAccountingExe = false;
        String msg = "", channelName = "", invoiceid = "", deliveryOid = "", billNo = "", doinvflag = "";

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        status = txnManager.getTransaction(def);    //Please asked about this to sagar sir
        try {
            boolean isLeaseFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isLeaseFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isLeaseFixedAsset")) : false;
            boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false;
            boolean isConsignment = request.getParameter("isConsignment") != null ? Boolean.parseBoolean(request.getParameter("isConsignment")) : false;
            boolean iscash = StringUtil.isNullOrEmpty(request.getParameter("incash")) ? false : Boolean.parseBoolean(request.getParameter("incash"));
            List li = updateInvoice(request);
            if (li != null && !li.isEmpty()) {
                String[] id = (String[]) li.get(0);
                String invoiceNumber = (String) li.get(5);
                String jeNumber = (String) li.get(8);
                invoiceid = (String) id[0];
                jobj.put("invoiceid", invoiceid);
                jobj.put("accountid", li.get(3));
                jobj.put("accountName", li.get(4));
                jobj.put("invoiceNo", li.get(5));
                jobj.put("address", li.get(6));
                jobj.put("amount", li.get(7));
                jobj.put("fullShippingAddress", li.get(9));

                txnManager.commit(status);
                issuccess = true;
                /*
                 * To refresh a Invoice List.
                 */
                if (isFixedAsset && !isConsignment) {
                    channelName = "/FixedAssetDisposalInvoiceList/gridAutoRefresh";
                } else if (isLeaseFixedAsset && !isConsignment) {
                    channelName = "/LeaseInvoiceList/gridAutoRefresh";
                } else if (!(isFixedAsset || isLeaseFixedAsset || isConsignment)) {//For normal CS and SI
                    channelName = "/CustomerInvoiceAndCashSalesReport/gridAutoRefresh";
                }
                /*
                 * Composing the message to display after save operation.
                 */
                if (isConsignment) {
                    msg = (iscash ? messageSource.getMessage("acc.Consignment.inv.cashsave", null, RequestContextUtils.getLocale(request)) : messageSource.getMessage("acc.Consignment.inv.creditsave", null, RequestContextUtils.getLocale(request))) + " " + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + invoiceNumber + ",</b> " + messageSource.getMessage("acc.field.JENo", null, RequestContextUtils.getLocale(request)) + ": <b>" + jeNumber + "</b>";
                } else {
                    msg = (iscash ? messageSource.getMessage("acc.invupdate.2", null, RequestContextUtils.getLocale(request)) : messageSource.getMessage("acc.invupdate.1", null, RequestContextUtils.getLocale(request))) + " " + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + invoiceNumber + ".</b> " + (messageSource.getMessage("acc.field.JENo", null, RequestContextUtils.getLocale(request)) + ": <b>" + jeNumber + "</b>");
                }
                /*
                 * Composing the message to insert into Audit Trail.
                 */
                String action = "updated";
                if (isLeaseFixedAsset) {
                    action += " Lease";
                }
                auditTrailObj.insertAuditLog(AuditAction.INVOICE_UPDATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + (isConsignment ? " Consignment Sales Invoice " : " Customer Invoice ") + recId, request, tranID);//    ERP-18011
            }

        } catch (SessionExpiredException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AccountingException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            isAccountingExe = true;
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("isAccountingExe", isAccountingExe);
                jobj.put("invid", invoiceid);
                jobj.put("doid", deliveryOid);
                jobj.put("dono", billNo);
                jobj.put("doinvflag", doinvflag);

                if (issuccess && !StringUtil.isNullOrEmpty(channelName)) {
                    jobj.put(Constants.userSessionId, (request.getSession(true)).getAttribute(Constants.userSessionId));// adding user session id to idenntify unique user session
                    ServerEventManager.publish(channelName, jobj.toString(), (ServletContext) this.getServletContext());
                }
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView updateDOStatusWithSIScript(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Invoice_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        try {
            String subdomain = "";
            String[] subdomainArray = null;
            if (!StringUtil.isNullOrEmpty(request.getParameter("subdomain"))) {
                subdomain = (String) request.getParameter("subdomain");
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
                    requestParams.put("companyid", companyid);
                    KwlReturnObject result = accInvoiceDAOobj.getDeliveryorder(requestParams);
                    Iterator itr = result.getEntityList().iterator();

                    while (itr.hasNext()) {
                        String id = (String) itr.next();
                        if (!StringUtil.isNullOrEmpty(id)) {
                            accInvoiceModuleService.updateOpenStatusFlagForDOInSI(id);
                        }
                    }
                    txnManager.commit(status);
                } catch (Exception ex) {
                    if (status != null) {
                        txnManager.rollback(status);
                    }
                    Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            issuccess = true;

        } catch (Exception ex) {

            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("msg", "Script completed for update isOpen Flag in DO");
                jobj.put("success", issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView updateSOisOpenAndLinkingWithCIScript(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Invoice_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        try {
            String subdomain = "";
            String[] subdomainArray = null;
            if (!StringUtil.isNullOrEmpty(request.getParameter("subdomain"))) {
                subdomain = (String) request.getParameter("subdomain");
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
                    requestParams.put("linkflag", "1");
                    requestParams.put("companyid", companyid);
                    KwlReturnObject result = accInvoiceDAOobj.getSalesorder(requestParams);
                    Iterator itr = result.getEntityList().iterator();
                    while (itr.hasNext()) {

                        // String linkNumber = (String) itr.next();
                        SalesOrder salesOrder = (SalesOrder) itr.next();

//                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), linkNumber);
//                        SalesOrder salesOrder = (SalesOrder) rdresult.getEntityList().get(0);
                        String invoiceStatus = accInvoiceModuleService.getSalesOrderStatus(salesOrder);
                        boolean isSOOpen = false;
                        if (invoiceStatus.equals("Open")) {
                            isSOOpen = true;
                        }
                        HashMap hMap = new HashMap();
                        hMap.put("salesOrder", salesOrder);
                        hMap.put("value", "1");
                        hMap.put("isSOOpen", isSOOpen);
                        accInvoiceDAOobj.updateSOLinkflag(hMap);
                        issuccess = true;
                    }
                    txnManager.commit(status);
                } catch (Exception ex) {
                    if (status != null) {
                        txnManager.rollback(status);
                    }
                    Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());

    }

    public ModelAndView updateCQScriptForSI(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("RIC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            KwlReturnObject companyList = accSalesOrderDAOObj.getCompanyList();
            Iterator citr = companyList.getEntityList().iterator();
            while (citr.hasNext()) {
                String company = (String) citr.next();
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("companyid", company);
                requestParams.put("linkflag", "1");
                KwlReturnObject result = accSalesOrderDAOObj.getQuotationsForCQScript(requestParams);
                Iterator itr = result.getEntityList().iterator();
                while (itr.hasNext()) {
                    String linkNumbers = (String) itr.next();
                    if (!StringUtil.isNullOrEmpty(linkNumbers)) {
                        accInvoiceModuleService.updateOpenStatusFlagForSI(linkNumbers);
                    }
                }
            }
            issuccess = true;
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("msg", "Script completed update the isOpen Flag in CQ");
                jobj.put("success", issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
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

    public void deleteEditedInvoiceDiscount(ArrayList discArr, String companyid) throws ServiceException, AccountingException, SessionExpiredException {
        try {
            for (int i = 0; i < discArr.size(); i++) {
                if (discArr.get(i) != null) {
                    accDiscountobj.deleteDiscount(discArr.get(i).toString(), companyid);
                }
            }
        } catch (Exception ex) {
            //Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    private List saveDeliveryRows(HttpServletRequest request, KwlReturnObject result, String companyId, JSONObject jobData, HashSet jeDetails, String jeId) throws ServiceException, SessionExpiredException, AccountingException, UnsupportedEncodingException {
        HashSet hs = new HashSet(), rows = new HashSet();
        ArrayList<String> prodList = new ArrayList<String>();
        double totaldiscount = 0, totalamount = 0, taxamount = 0;
        DateFormat df = authHandler.getDateOnlyFormat(request);
        List ll = new ArrayList();
        try {
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyId);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            boolean isLinkedWithDO = false;
            if (result.getEntityList().size() > 0) {
//            JSONArray jArr = new JSONArray(invoiceDetails);
                for (int i = 0; i < result.getEntityList().size(); i++) {
                    double amount = 0;
                    DeliveryOrderDetail deliveryOrderDetail = (DeliveryOrderDetail) result.getEntityList().get(i);
                    double quantity = getDeliveryOrderDetailStatus(deliveryOrderDetail);
                    if (quantity == 0) {
                        continue;
                    }
                    InvoiceDetail row = new InvoiceDetail();
                    double discountPercent = deliveryOrderDetail.getDiscountispercent();
                    double discountValue = 0;
                    if (discountPercent == 0) { // Flat
                        discountValue = deliveryOrderDetail.getDiscount();
                    } else { // Percentage
                        discountValue = deliveryOrderDetail.getDiscount() / 100;
                    }
                    amount += deliveryOrderDetail.getRate() * quantity;
                    double taxAmt = 0;
                    double rowTaxPercent = 0;
                    double taxPercent = 0;
                    if (deliveryOrderDetail.getTax() != null) {
                        taxAmt = (deliveryOrderDetail.getRowTaxAmount());
                        taxAmt = (deliveryOrderDetail.getRowTaxAmount());
                        KwlReturnObject taxresult = accTaxObj.getTaxPercent(sessionHandlerImpl.getCompanyid(request), deliveryOrderDetail.getDeliveryOrder().getOrderDate(), deliveryOrderDetail.getTax().getID());
                        taxPercent = (Double) taxresult.getEntityList().get(0);
                        rowTaxPercent = taxPercent;
                    }
                    Tax rowtax = null;
                    double rowtaxamount = 0d;
                    amount += taxAmt;
                    JournalEntryDetail jed;
                    row.setSrno(deliveryOrderDetail.getSrno());
                    row.setWasRowTaxFieldEditable(true);// this field has been added after making row tax field editable its value will be set always true from its implementation. i.e from 28-jan-2014.REASON -  when Row Tax Amount field was not editable then tax calculation was taking place according to tax percent, as selected From Tax combo in JS Side.
                    row.setPriceSource(!StringUtil.isNullOrEmpty(deliveryOrderDetail.getPriceSource()) ? StringUtil.DecodeText(deliveryOrderDetail.getPriceSource()) : "");

                    boolean includingGST = false;
                    /* Set rateincludegst for invoicedetail */
                    if (deliveryOrderDetail.getDeliveryOrder().isGstIncluded()) {
                        includingGST = true;
                    }

                    if (deliveryOrderDetail.getRateincludegst() != 0) {
                        row.setRateincludegst(deliveryOrderDetail.getRateincludegst());

                    }

                    if (!StringUtil.isNullOrEmpty(deliveryOrderDetail.getDescription())) {
                        try {
                            row.setDescription(StringUtil.DecodeText(deliveryOrderDetail.getDescription()));
                        } catch (Exception ex) {
                            row.setDescription(deliveryOrderDetail.getDescription());
                        }
                    }

                    KwlReturnObject prdresult = accountingHandlerDAOobj.getObject(Product.class.getName(), deliveryOrderDetail.getProduct().getID());
                    Product product = (Product) prdresult.getEntityList().get(0);

//                    double profitLossAmt = 0d;
                    if (!StringUtil.isNullOrEmpty(deliveryOrderDetail.getInvstoreid())) {
                        row.setInvstoreid(deliveryOrderDetail.getInvstoreid());
                    } else {
                        row.setInvstoreid("");
                    }

                    if (!StringUtil.isNullOrEmpty(deliveryOrderDetail.getInvlocid())) {
                        row.setInvlocid(deliveryOrderDetail.getInvlocid());
                    } else {
                        row.setInvlocid("");
                    }
                    row.setDeliveryOrderDetail(deliveryOrderDetail);
                    boolean updateInventoryFlag = (preferences.isWithInvUpdate()) ? false : true;
                    row.setPartamount(0.0);

                    KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
                    Company company = (Company) cmpresult.getEntityList().get(0);
                    row.setCompany(company);
                    row.setRate(deliveryOrderDetail.getRate());

                    prodList.add(deliveryOrderDetail.getProduct().getID());

                    JSONObject inventoryjson = new JSONObject();
//                    quantity = getDeliveryOrderDetailStatus(deliveryOrderDetail);
                    inventoryjson.put("productid", deliveryOrderDetail.getProduct().getID());
                    inventoryjson.put("quantity", quantity);
                    if (deliveryOrderDetail.getUom() != null) {
                        inventoryjson.put("uomid", deliveryOrderDetail.getUom().getID());
                    }
                    inventoryjson.put("baseuomquantity", updateInventoryFlag ? deliveryOrderDetail.getBaseuomquantity() : 0);
                    inventoryjson.put("actquantity", updateInventoryFlag ? 0 : quantity);
                    inventoryjson.put("baseuomrate", deliveryOrderDetail.getBaseuomrate());
                    inventoryjson.put("invrecord", updateInventoryFlag ? true : false);

                    inventoryjson.put("description", deliveryOrderDetail.getDescription());
                    inventoryjson.put("carryin", false);
                    inventoryjson.put("defective", false);
                    inventoryjson.put("newinventory", false);
                    inventoryjson.put("companyid", companyId);
                    inventoryjson.put("updatedate", deliveryOrderDetail.getDeliveryOrder().getOrderDate());
                    KwlReturnObject invresult = accProductObj.addInventory(inventoryjson);
                    Inventory inventory = (Inventory) invresult.getEntityList().get(0);

                    row.setInventory(inventory);
                    row.setDeliveryOrderDetail(deliveryOrderDetail);
                    double rate = row.getRate();
                    /* Calculating unit price when including GST option true */
                    if (includingGST) {
                        rate = deliveryOrderDetail.getRateincludegst();
                    }
                    double rowAmount = 0;
                    rowAmount = authHandler.round(rate * quantity, companyId);
                    rowAmount = authHandler.round(rowAmount, companyId);
                    double rowdiscount = 0;
                    totalamount += rowAmount;
                    Discount discount = null;

                    double disc = deliveryOrderDetail.getDiscount();
                    int rowdisc = deliveryOrderDetail.getDiscountispercent();

                    double rowTaxAmtInBase = 0.0;

                    KwlReturnObject bAmt = null, jeResult = null;
                    JournalEntry journalEntry = null;
                    HashMap<String, Object> requestParams = new HashMap();

                    if (!requestParams.containsKey("gcurrencyid") && !StringUtil.isNullOrEmpty(jeId)) {
                        jeResult = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), jeId);
                        journalEntry = (JournalEntry) jeResult.getEntityList().get(0);
                        requestParams.put("gcurrencyid", journalEntry.getCompany().getCurrency().getCurrencyID());
                        requestParams.put("companyid", journalEntry.getCompany().getCompanyID());

                    }
                    if (deliveryOrderDetail.getTax() != null) {
                        row.setTax(deliveryOrderDetail.getTax());
                        row.setRowTaxAmount(deliveryOrderDetail.getRowTaxAmount());
                        taxamount += deliveryOrderDetail.getRowTaxAmount();
                        rowtaxamount = deliveryOrderDetail.getRowTaxAmount();
                        /* Saving row taxamount in base*/
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, rowtaxamount, journalEntry.getCurrency().getCurrencyID(), journalEntry.getEntryDate(), journalEntry.getExternalCurrencyRate());
                        rowTaxAmtInBase = (Double) bAmt.getEntityList().get(0);
                        rowTaxAmtInBase = authHandler.round(rowTaxAmtInBase, companyId);
                        row.setRowTaxAmountInBase(rowTaxAmtInBase);
                        rowtax = deliveryOrderDetail.getTax();
                    }

                    if (disc != 0.0) {
                        JSONObject discjson = new JSONObject();
                        discjson.put("discount", disc);
                        discjson.put("inpercent", (rowdisc == 1) ? true : false);
                        discjson.put("originalamount", rowAmount);
                        discjson.put("companyid", companyId);
                        KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                        discount = (Discount) dscresult.getEntityList().get(0);
                        row.setDiscount(discount);
                        rowdiscount = discount.getDiscountValue();
                        totaldiscount += rowdiscount;
                    }

                    double rowExcludingGstAmount = 0d;
                    double rowExcludingGstAmountInBase = 0d;
                    /*Below code is used to save exlcuding gst amt and excluding gst amount in base
                     required to show invoice in GST report
                    
                     */
                    rowExcludingGstAmount = rowAmount;
                    if (includingGST) {//include in GST
                        rowExcludingGstAmount -= rowdiscount;
                        double taxAppliedOn = 0;
                        taxAppliedOn = (100 * rowExcludingGstAmount) / (100 + rowTaxPercent);
                        rowExcludingGstAmount = taxAppliedOn;
                    } else {
                        rowExcludingGstAmount -= rowdiscount;
                    }
                    rowExcludingGstAmount = authHandler.round(rowExcludingGstAmount, companyId);
                    row.setRowExcludingGstAmount(rowExcludingGstAmount);

                    /*Save Excluding GST amount  in base*/
                    if (journalEntry != null) {
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, rowExcludingGstAmount, journalEntry.getCurrency().getCurrencyID(), journalEntry.getEntryDate(), journalEntry.getExternalCurrencyRate());
                        rowExcludingGstAmountInBase = (Double) bAmt.getEntityList().get(0);
                    }
                    rowExcludingGstAmountInBase = authHandler.round(rowExcludingGstAmountInBase, companyId);
                    row.setRowExcludingGstAmountInBase(rowExcludingGstAmountInBase);

                    /*
                     For JE Detail Entry for sales amount = rowAmount - Tax Amount
                     */
                    double JElineAmount = rowAmount; // Row Amount Not Include Gst(Tax)

                    if (includingGST) { // Check For  Row Amount Include Gst(Tax)

                        JElineAmount -= rowtaxamount;
                    }

                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", jeDetails.size() + 1);
                    jedjson.put("companyid", companyId);
                    jedjson.put("amount", JElineAmount);
                    jedjson.put("accountid", product.getSalesAccount().getID());
                    jedjson.put("debit", false);
                    jedjson.put("jeid", jeId);
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jeDetails.add(jed);

                    row.setDeferredJeDetailId(jed.getID());

                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    row.setSalesJED(jed);

                    /* ---Save JE detail for Tax----*/
                    if (rowtax != null) {
                        jedjson = new JSONObject();
                        jedjson.put("srno", jeDetails.size() + 1);
                        jedjson.put("companyid", companyId);
                        jedjson.put("amount", rowtaxamount);
                        jedjson.put("accountid", rowtax.getAccount().getID());
                        jedjson.put("debit", false);
                        jedjson.put("jeid", jeId);
                        KwlReturnObject taxjedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        jed = (JournalEntryDetail) taxjedresult.getEntityList().get(0);
                        jeDetails.add(jed);
                        row.setGstJED(jed);
                    }

                    /* Get custom/Dimension field from Sales Order which have been used in SO  & also available for Invoice */
                    HashMap<String, Object> fieldrequestParams1 = new HashMap();
                    HashMap<String, String> customFieldMap = new HashMap<String, String>();
                    HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
                    fieldrequestParams1.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                    fieldrequestParams1.put(Constants.filter_values, Arrays.asList(companyId, Constants.Acc_Delivery_Order_ModuleId, 1));
                    HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                    HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams1, replaceFieldMap, customFieldMap, customDateFieldMap);

                    /* Get Custom/Dimension field Data */
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    JSONObject obj = new JSONObject();

                    DeliveryOrderDetailCustomData jeDetailCustom = (DeliveryOrderDetailCustomData) deliveryOrderDetail.getDeliveryOrderDetailCustomData();

                    AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);

                    if (jeDetailCustom != null) {
                        JSONObject params = new JSONObject();
                        params.put(Constants.isExport, false);
                        params.put("isForReport", true);
                        params.put(Constants.isdefaultHeaderMap, false);

                        fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);

                    }

                    /* Write a function to fetch custom field from fieldparams against fieldlabel
                     where moduleid will be invoice  
                          
                     */
                    HashMap requestParams1 = new HashMap();
                    Iterator ite = obj.keys();
                    String fieldLabel = "";
                    String fieldValue = "";

                    while (ite.hasNext()) {
                        fieldLabel = (String) ite.next();
                        fieldValue = obj.getString(fieldLabel);

                        requestParams1.put("fieldname", fieldLabel);
                        requestParams1.put("fieldValue", fieldValue);
                        requestParams1.put("invoicedetail", row);
                        requestParams1.put("companyid", companyId);
                        requestParams1.put("jedetail", jed);
                        requestParams1.put("moduleid", Constants.Acc_Invoice_ModuleId);

                        /* Function used to check custom/dimension field present in invoice
                         also & if present then being saved for Invoice module
                         */
                        checkAndSaveLineLevelCustomField(requestParams1);

                    }

                    /* Get custom/dimension fields data for "Product module"
                     which have been used in SO at line level
                     */
                    HashMap<String, Object> fieldrequestParamsProduct = new HashMap();
                    Map<String, Object> variableMapProduct = new HashMap<String, Object>();
                    fieldrequestParamsProduct.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                    fieldrequestParamsProduct.put(Constants.filter_values, Arrays.asList(companyId, Constants.Acc_Product_Master_ModuleId, 0));
                    HashMap<String, String> replaceFieldMapProduct = new HashMap<String, String>();
                    HashMap<String, Integer> FieldMapProduct = accAccountDAOobj.getFieldParamsMap(fieldrequestParamsProduct, replaceFieldMapProduct);

                    KwlReturnObject resultProduct = accountingHandlerDAOobj.getObject(DeliveryOrderDetailProductCustomData.class.getName(), deliveryOrderDetail.getID());
                    DeliveryOrderDetailProductCustomData objProduct = (DeliveryOrderDetailProductCustomData) resultProduct.getEntityList().get(0);
                    JSONObject obj1 = new JSONObject();
                    if (objProduct != null) {
                        JSONObject params = new JSONObject();
                        params.put(Constants.isExport, false);
                        params.put("isForReport", true);

                        /* Preparing a map with key "Field Label" &  data as "Field Value" 
                         for product custom field used in Sales Order
                         */
                        setCustomColumnValuesForProduct(objProduct, FieldMapProduct, replaceFieldMapProduct, variableMapProduct, params);
                        for (Map.Entry<String, Object> varEntry : variableMapProduct.entrySet()) {
                            String coldata = varEntry.getValue().toString();
                            if (!StringUtil.isNullOrEmpty(coldata)) {
                                obj1.put(varEntry.getKey(), coldata);
                                obj1.put("key", varEntry.getKey());
//               
                            }
                        }

                        ite = obj1.keys();
                        while (ite.hasNext()) {

                            fieldLabel = (String) ite.next();
                            fieldValue = obj1.getString(fieldLabel);

                            requestParams1.put("fieldname", fieldLabel);
                            requestParams1.put("fieldValue", fieldValue);
                            requestParams1.put("invoicedetail", row);
                            requestParams1.put("companyid", companyId);
                            requestParams1.put("jedetail", jed);
                            requestParams1.put("moduleid", Constants.Acc_Product_Master_ModuleId);
                            requestParams1.put("isFromProduct", true);

                            /* Function used to check custom/dimension field present in invoice
                             also & if present then being saved for Invoice module
                             */
                            checkAndSaveLineLevelCustomField(requestParams1);

                        }

                    }

                    rows.add(row);
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveDeliveryRows : " + ex.getMessage(), ex);
        }
//        invoice.setRows(rows);
        ll.add(new double[]{totaldiscount, totalamount, taxamount});
        ll.add(rows);
        ll.add(prodList);
        return ll;
    }

    private void setCustomColumnValuesForProduct(AccountCustomData doDetailsProductCustomData, HashMap<String, Integer> fieldMap, Map<String, String> replaceFieldMap,
            Map<String, Object> variableMap, JSONObject params) {
        for (Map.Entry<String, Integer> field : fieldMap.entrySet()) {
            boolean isForReport = params.optBoolean(Constants.isForReport, false);
            boolean isExport = params.optBoolean(Constants.isExport, false);
            Integer colnumber = field.getValue();
            if (colnumber > 0) { // colnumber will be 0 if key is part of reference map
                Integer isref = fieldMap.get(field.getKey() + "#" + colnumber);// added '#' while creating map collection for custom fields.
                // Without this change, it creates problem if two custom columns having name like XYZ and XYZ1
                String coldata = null;
                if (isref != null) {
                    try {
                        if (doDetailsProductCustomData != null) {
                            coldata = doDetailsProductCustomData.getCol(colnumber);
                        }
                        if (!StringUtil.isNullOrEmpty(coldata)) {
                            if (coldata.length() > 1) {
                                if (isref == 1) {
                                } else if (isref == 0 || isref == 7) {
                                    if (isForReport) {
                                        String valueForReport = "";
                                        String[] valueData = coldata.split(",");
                                        for (String value : valueData) {
                                            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), value);
                                            FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                                            if (fieldComboData != null) {
                                                valueForReport += fieldComboData.getValue() + ",";
                                            }
                                        }
                                        if (valueForReport.length() > 1) {
                                            coldata = valueForReport.substring(0, valueForReport.length() - 1);
                                        }
                                    } else {
                                        coldata = coldata;
                                    }
                                } else if (isref == 3 && isExport) {
                                    DateFormat df2 = new SimpleDateFormat(Constants.yyyyMMdd);
                                    DateFormat defaultDateFormat = new SimpleDateFormat(Constants.MMMMdyyyy);
                                    Date dateFromDB = null;
                                    try {
                                        dateFromDB = defaultDateFormat.parse(coldata);
                                        coldata = df2.format(dateFromDB);

                                    } catch (Exception e) {
                                    }
                                }
                            }
                            variableMap.put(field.getKey(), coldata);
                        }
                    } catch (IllegalArgumentException ex) {
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    /*Save invoice details while creating bulk or individual invoices from SO */
    private List saveInvoiceDetail(HttpServletRequest request, KwlReturnObject result, String companyId, JSONObject jobData, HashSet jeDetails, String jeId) throws ServiceException, SessionExpiredException, AccountingException, UnsupportedEncodingException {
        HashSet hs = new HashSet(), rows = new HashSet();
        ArrayList<String> prodList = new ArrayList<String>();
        double totaldiscount = 0, totalamount = 0, taxamount = 0;
        DateFormat df = authHandler.getDateOnlyFormat(request);
        List ll = new ArrayList();
        double amount = 0;
        double lineLevelTermAmount = 0;
        try {
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyId);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);

            KwlReturnObject extraCompanyPreferencesObj = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyId);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraCompanyPreferencesObj.getEntityList().get(0);

            if (result.getEntityList().size() > 0) {

                /* Iterating SO details to save in invoice details*/
                for (int i = 0; i < result.getEntityList().size(); i++) {

                    SalesOrderDetail deliveryOrderDetail = (SalesOrderDetail) result.getEntityList().get(i);

                    /* Manually Closed line item of SO is not participating in creation of invoice */
                    if (deliveryOrderDetail.isIsLineItemClosed()) {
                        continue;
                    }
                    double quantity = getSalesOrderDetailStatus(deliveryOrderDetail);
                    if (quantity == 0) {
                        continue;
                    }
                    amount = deliveryOrderDetail.getRate() * quantity;
                    /* Check here quantity fetched is OK? */
                    InvoiceDetail row = new InvoiceDetail();
                    double discountPercent = deliveryOrderDetail.getDiscountispercent();
                    double discountValue = 0;
                    if (discountPercent == 0) { // Flat discount
                        discountValue = deliveryOrderDetail.getDiscount();
                    } else { // Percentage discount
                        discountValue = deliveryOrderDetail.getDiscount() / 100;
                        discountValue = amount * discountValue;
                    }

                    double taxAmt = 0;
                    double taxPercent = 0;
                    double rowTaxPercent = 0;
                    /* Do check line & global taxes to identify to execute block of code if else*/
                    if (deliveryOrderDetail.getTax() != null) {//Line Level Tax
                        taxAmt = (deliveryOrderDetail.getRowTaxAmount());
                        KwlReturnObject taxresult = accTaxObj.getTaxPercent(sessionHandlerImpl.getCompanyid(request), deliveryOrderDetail.getSalesOrder().getOrderDate(), deliveryOrderDetail.getTax().getID());
                        taxPercent = (Double) taxresult.getEntityList().get(0);
                        rowTaxPercent = taxPercent;

                    } else {

                        if (deliveryOrderDetail.getSalesOrder().getTax() != null) {
                            KwlReturnObject taxresult = accTaxObj.getTaxPercent(sessionHandlerImpl.getCompanyid(request), deliveryOrderDetail.getSalesOrder().getOrderDate(), deliveryOrderDetail.getSalesOrder().getTax().getID());
                            taxPercent = (Double) taxresult.getEntityList().get(0);

                        }
                        taxAmt = (taxPercent == 0 ? 0 : authHandler.round(((amount - discountValue) * taxPercent / 100), companyId));
                    }
                    Tax rowtax = null;
                    double rowtaxamount = 0d;
                    JournalEntryDetail jed;
                    row.setSrno(deliveryOrderDetail.getSrno());
                    row.setWasRowTaxFieldEditable(true);// this field has been added after making row tax field editable its value will be set always true from its implementation. i.e from 28-jan-2014.REASON -  when Row Tax Amount field was not editable then tax calculation was taking place according to tax percent, as selected From Tax combo in JS Side.
                    row.setPriceSource(!StringUtil.isNullOrEmpty(deliveryOrderDetail.getPriceSource()) ? StringUtil.DecodeText(deliveryOrderDetail.getPriceSource()) : "");

                    boolean includingGST = false;
                    /* Set rateincludegst for invoicedetail */

                    //check for malaysian company
                    if (deliveryOrderDetail.getRateincludegst() != 0) {
                        row.setRateincludegst(deliveryOrderDetail.getRateincludegst());
                        includingGST = true;

                    }

                    /*  Setting Line level Term amount in case of Including GSt true */
                    if (includingGST) {
                        if (deliveryOrderDetail.getLineLevelTermAmount() != 0) {
                            lineLevelTermAmount += deliveryOrderDetail.getLineLevelTermAmount();
                            row.setLineLevelTermAmount(deliveryOrderDetail.getLineLevelTermAmount());
                        }
                    }

                    if (!StringUtil.isNullOrEmpty(deliveryOrderDetail.getDescription())) {
                        try {
                            row.setDescription(StringUtil.DecodeText(deliveryOrderDetail.getDescription()));
                        } catch (Exception ex) {
                            row.setDescription(deliveryOrderDetail.getDescription());
                        }
                    }

                    KwlReturnObject prdresult = accountingHandlerDAOobj.getObject(Product.class.getName(), deliveryOrderDetail.getProduct().getID());
                    Product product = (Product) prdresult.getEntityList().get(0);

                    if (!StringUtil.isNullOrEmpty(deliveryOrderDetail.getInvstoreid())) {
                        row.setInvstoreid(deliveryOrderDetail.getInvstoreid());
                    } else {
                        row.setInvstoreid("");
                    }

                    if (!StringUtil.isNullOrEmpty(deliveryOrderDetail.getInvlocid())) {
                        row.setInvlocid(deliveryOrderDetail.getInvlocid());
                    } else {
                        row.setInvlocid("");
                    }
                    row.setSalesorderdetail(deliveryOrderDetail);
                    boolean updateInventoryFlag = (preferences.isWithInvUpdate()) ? false : true;
                    row.setPartamount(0.0);//As we are using SO completely in Invoice 

                    KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
                    Company company = (Company) cmpresult.getEntityList().get(0);
                    row.setCompany(company);
                    row.setRate(deliveryOrderDetail.getRate());

                    prodList.add(deliveryOrderDetail.getProduct().getID());

                    JSONObject inventoryjson = new JSONObject();

                    inventoryjson.put("productid", deliveryOrderDetail.getProduct().getID());
                    inventoryjson.put("quantity", quantity);
                    if (deliveryOrderDetail.getUom() != null) {
                        inventoryjson.put("uomid", deliveryOrderDetail.getUom().getID());
                    }
                    inventoryjson.put("baseuomquantity", updateInventoryFlag ? deliveryOrderDetail.getBaseuomquantity() : 0);
                    inventoryjson.put("actquantity", updateInventoryFlag ? 0 : quantity);
                    inventoryjson.put("baseuomrate", deliveryOrderDetail.getBaseuomrate());
                    inventoryjson.put("invrecord", updateInventoryFlag ? true : false);

                    inventoryjson.put("description", deliveryOrderDetail.getDescription());
                    inventoryjson.put("carryin", false);
                    inventoryjson.put("defective", false);
                    inventoryjson.put("newinventory", false);
                    inventoryjson.put("companyid", companyId);
                    inventoryjson.put("updatedate", deliveryOrderDetail.getSalesOrder().getOrderDate());

                    /* Adding entry in inventory for invoice*/
                    KwlReturnObject invresult = accProductObj.addInventory(inventoryjson);
                    Inventory inventory = (Inventory) invresult.getEntityList().get(0);

                    row.setInventory(inventory);
                    double rate = row.getRate();
                    double rowAmount = 0;

                    /* Calculating unit price when including GST option true */
                    if (includingGST) {
                        rate = deliveryOrderDetail.getRateincludegst();
                    }
                    rowAmount = authHandler.round(rate * quantity, companyId);
                    rowAmount = authHandler.round(rowAmount, companyId);
                    double rowdiscount = 0;
                    totalamount += rowAmount;
                    Discount discount = null;

                    double rowTaxAmtInBase = 0.0;
                    double disc = deliveryOrderDetail.getDiscount();
                    int rowdisc = deliveryOrderDetail.getDiscountispercent();
                    KwlReturnObject bAmt = null, jeResult = null;
                    JournalEntry journalEntry = null;
                    HashMap<String, Object> requestParams = new HashMap();

                    if (!requestParams.containsKey("gcurrencyid") && !StringUtil.isNullOrEmpty(jeId)) {
                        jeResult = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), jeId);
                        journalEntry = (JournalEntry) jeResult.getEntityList().get(0);
                        requestParams.put("gcurrencyid", journalEntry.getCompany().getCurrency().getCurrencyID());
                        requestParams.put("companyid", journalEntry.getCompany().getCompanyID());

                    }

                    if (deliveryOrderDetail.getTax() != null) {
                        row.setTax(deliveryOrderDetail.getTax());
                        row.setRowTaxAmount(deliveryOrderDetail.getRowTaxAmount());
                        rowtaxamount = deliveryOrderDetail.getRowTaxAmount();
                        /* Saving row taxamount in base*/
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, rowtaxamount, journalEntry.getCurrency().getCurrencyID(), journalEntry.getEntryDate(), journalEntry.getExternalCurrencyRate());
                        rowTaxAmtInBase = (Double) bAmt.getEntityList().get(0);
                        rowTaxAmtInBase = authHandler.round(rowTaxAmtInBase, companyId);
                        row.setRowTaxAmountInBase(rowTaxAmtInBase);
                        taxamount += deliveryOrderDetail.getRowTaxAmount();
                        rowtax = deliveryOrderDetail.getTax();
                    } else {

                        taxamount += taxAmt;
                    }

                    /* Saving discount*/
                    if (disc != 0.0) {
                        JSONObject discjson = new JSONObject();
                        discjson.put("discount", disc);
                        discjson.put("inpercent", (rowdisc == 1) ? true : false);
                        discjson.put("originalamount", rowAmount);
                        discjson.put("companyid", companyId);
                        KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                        discount = (Discount) dscresult.getEntityList().get(0);
                        row.setDiscount(discount);
                        rowdiscount = discount.getDiscountValue();
                        totaldiscount += rowdiscount;
                    }

                    double rowExcludingGstAmount = 0d;
                    double rowExcludingGstAmountInBase = 0d;
                    /*Below code is used to save exlcuding gst amt and excluding gst amount in base
                     required to show invoice in GST report
                    
                     */
                    rowExcludingGstAmount = rowAmount;
                    if (includingGST) {//include in GST
                        rowExcludingGstAmount -= rowdiscount;
                        rowExcludingGstAmount += deliveryOrderDetail.getLineLevelTermAmount();//including GST price + Line level term amount
                        double taxAppliedOn = 0;
                        taxAppliedOn = (100 * rowExcludingGstAmount) / (100 + rowTaxPercent);
                        rowExcludingGstAmount = taxAppliedOn;
                    } else {
                        rowExcludingGstAmount -= rowdiscount;
                    }
                    rowExcludingGstAmount = authHandler.round(rowExcludingGstAmount, companyId);
                    row.setRowExcludingGstAmount(rowExcludingGstAmount);

                    /*Save Excluding GST amount  in base*/
                    if (journalEntry != null) {
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, rowExcludingGstAmount, journalEntry.getCurrency().getCurrencyID(), journalEntry.getEntryDate(), journalEntry.getExternalCurrencyRate());
                        rowExcludingGstAmountInBase = (Double) bAmt.getEntityList().get(0);
                    }
                    rowExcludingGstAmountInBase = authHandler.round(rowExcludingGstAmountInBase, companyId);
                    row.setRowExcludingGstAmountInBase(rowExcludingGstAmountInBase);

                    /*
                     For JE Detail Entry for sales amount = rowAmount - Tax Amount
                     */
                    double JElineAmount = rowAmount; // Row Amount Not Include Gst(Tax)

                    if (includingGST) { // Check For  Row Amount Include Gst(Tax)

                        JElineAmount -= rowtaxamount;
                    }

                    JSONObject jedjson = new JSONObject();

                    jedjson.put("srno", jeDetails.size() + 1);
                    jedjson.put("companyid", companyId);
                    jedjson.put("amount", JElineAmount);
                    jedjson.put("accountid", product.getSalesAccount().getID());
                    jedjson.put("debit", false);
                    jedjson.put("jeid", jeId);
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jeDetails.add(jed);

                    row.setDeferredJeDetailId(jed.getID());

                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    row.setSalesJED(jed);

                    /* Doing entry in JEdetails for tax ,if tax is applied at row level*/
                    if (rowtax != null) {
                        jedjson = new JSONObject();
                        jedjson.put("srno", jeDetails.size() + 1);
                        jedjson.put("companyid", companyId);
                        jedjson.put("amount", rowtaxamount);
                        jedjson.put("accountid", rowtax.getAccount().getID());
                        jedjson.put("debit", false);
                        jedjson.put("jeid", jeId);
                        KwlReturnObject taxjedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        jed = (JournalEntryDetail) taxjedresult.getEntityList().get(0);
                        jeDetails.add(jed);
                        row.setGstJED(jed);
                    }

                    /* Get custom/Dimension field from Sales Order which have been used in SO  & also available for Invoice */
                    HashMap<String, Object> fieldrequestParams1 = new HashMap();
                    HashMap<String, String> customFieldMap = new HashMap<String, String>();
                    HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
                    fieldrequestParams1.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                    fieldrequestParams1.put(Constants.filter_values, Arrays.asList(companyId, Constants.Acc_Sales_Order_ModuleId, 1));
                    HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                    HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams1, replaceFieldMap, customFieldMap, customDateFieldMap);

                    /* Get Custom/Dimension field Data */
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    JSONObject obj = new JSONObject();

                    SalesOrderDetailsCustomData jeDetailCustom = (SalesOrderDetailsCustomData) deliveryOrderDetail.getSoDetailCustomData();

                    AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);

                    if (jeDetailCustom != null) {
                        JSONObject params = new JSONObject();
                        params.put(Constants.isExport, false);
                        params.put("isForReport", true);
                        params.put(Constants.isdefaultHeaderMap, false);

                        fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);

                    }

                    /* Write a function to fetch custom field from fieldparams against fieldlabel
                     where moduleid will be invoice  
                          
                     */
                    HashMap requestParams1 = new HashMap();
                    Iterator ite = obj.keys();
                    String fieldLabel = "";
                    String fieldValue = "";

                    while (ite.hasNext()) {
                        fieldLabel = (String) ite.next();
                        fieldValue = obj.getString(fieldLabel);

                        requestParams1.put("fieldname", fieldLabel);
                        requestParams1.put("fieldValue", fieldValue);
                        requestParams1.put("invoicedetail", row);
                        requestParams1.put("companyid", companyId);
                        requestParams1.put("jedetail", jed);
                        requestParams1.put("moduleid", Constants.Acc_Invoice_ModuleId);

                        /* Function used to check custom/dimension field present in invoice
                         also & if present then being saved for Invoice module
                         */
                        checkAndSaveLineLevelCustomField(requestParams1);

                    }

                    /* Get custom/dimension fields data for "Product module"
                     which have been used in SO at line level
                     */
                    HashMap<String, Object> fieldrequestParamsProduct = new HashMap();
                    Map<String, Object> variableMapProduct = new HashMap<String, Object>();
                    fieldrequestParamsProduct.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                    fieldrequestParamsProduct.put(Constants.filter_values, Arrays.asList(companyId, Constants.Acc_Product_Master_ModuleId, 0));
                    HashMap<String, String> replaceFieldMapProduct = new HashMap<String, String>();
                    HashMap<String, Integer> FieldMapProduct = accAccountDAOobj.getFieldParamsMap(fieldrequestParamsProduct, replaceFieldMapProduct);

                    KwlReturnObject resultProduct = accountingHandlerDAOobj.getObject(SalesOrderDetailProductCustomData.class.getName(), deliveryOrderDetail.getID());
                    SalesOrderDetailProductCustomData objProduct = (SalesOrderDetailProductCustomData) resultProduct.getEntityList().get(0);
                    JSONObject obj1 = new JSONObject();
                    if (objProduct != null) {
                        JSONObject params = new JSONObject();
                        params.put(Constants.isExport, false);
                        params.put("isForReport", true);

                        /* Preparing a map with key "Field Label" &  data as "Field Value" 
                         for product custom field used in Sales Order
                         */
                        setCustomColumnValuesForProduct(objProduct, FieldMapProduct, replaceFieldMapProduct, variableMapProduct, params);
                        for (Map.Entry<String, Object> varEntry : variableMapProduct.entrySet()) {
                            String coldata = varEntry.getValue().toString();
                            if (!StringUtil.isNullOrEmpty(coldata)) {
                                obj1.put(varEntry.getKey(), coldata);
                                obj1.put("key", varEntry.getKey());
//               
                            }
                        }

                        ite = obj1.keys();
                        while (ite.hasNext()) {

                            fieldLabel = (String) ite.next();
                            fieldValue = obj1.getString(fieldLabel);

                            requestParams1.put("fieldname", fieldLabel);
                            requestParams1.put("fieldValue", fieldValue);
                            requestParams1.put("invoicedetail", row);
                            requestParams1.put("companyid", companyId);
                            requestParams1.put("jedetail", jed);
                            requestParams1.put("moduleid", Constants.Acc_Product_Master_ModuleId);
                            requestParams1.put("isFromProduct", true);

                            /* Function used to check custom/dimension field present in invoice
                             also & if present then being saved for Invoice module
                             */
                            checkAndSaveLineLevelCustomField(requestParams1);

                        }

                    }

                    rows.add(row);
                }
            }

        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveInvoiceDetail : " + ex.getMessage(), ex);
        }

        ll.add(new double[]{totaldiscount, totalamount, taxamount, lineLevelTermAmount});
        ll.add(rows);
        ll.add(prodList);
        return ll;
    }

    /*----------- Saving bulk Delivery Order Details -----------*/
    private List saveDeliveryOrderDetail(HttpServletRequest request, KwlReturnObject result, String companyId, DeliveryOrder deliveryOrder, Set<JournalEntryDetail> inventoryJEDetails, String inventoryJEid, String nextAutoNumber) throws ServiceException, SessionExpiredException, AccountingException, UnsupportedEncodingException, ParseException {

        HashSet hs = new HashSet(), rows = new HashSet();
        ArrayList<String> prodList = new ArrayList<String>();
        double totaldiscount = 0, totalamount = 0, taxamount = 0;

        List ll = new ArrayList();
        double lineLevelTermAmount = 0;
        try {
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyId);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);

            KwlReturnObject extraCompanyPreferencesObj = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyId);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraCompanyPreferencesObj.getEntityList().get(0);
            List<StockMovement> stockMovementsList = new ArrayList<StockMovement>();//check it is empty or anything else

            if (result.getEntityList().size() > 0) {

                /* Iterating SO details to save in Delivery Order details*/
                for (int i = 0; i < result.getEntityList().size(); i++) {

                    SalesOrderDetail salesOrderDetail = (SalesOrderDetail) result.getEntityList().get(i);

                    /* Manually Closed line item of SO is not participating in creation of Delivery Order */
                    if (salesOrderDetail.isIsLineItemClosed()) {
                        continue;
                    }
                    KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(SalesOrderDetail.class.getName(), salesOrderDetail.getID()); //for link
                    SalesOrderDetail sod = (SalesOrderDetail) rdresult.getEntityList().get(0);


                    /*------------------Lock Quantity Calculation Starts Here---------*/
                    HashMap<String, Object> lockQuantityMap = new HashMap<String, Object>();
                    lockQuantityMap.put("companyId", companyId);
                    lockQuantityMap.put("salesOrderDetail", salesOrderDetail);
                    JSONObject lockInfo = accInvoiceModuleService.updateLockQuantityCalculation(lockQuantityMap);

                    /*----------Lock Quantity Calculation End here------------*/
                    /* -------------Update Balance Quantity of Sales Order Detail Strat Here------------ */
                    HashMap poMap = new HashMap();
                    poMap.put("sodetails", salesOrderDetail.getID());
                    poMap.put(Constants.companyKey, companyId);
                    poMap.put("balanceqty", salesOrderDetail.getBalanceqty());
                    poMap.put("add", false);
                    accCommonTablesDAO.updateSalesorderOrderStatus(poMap);

                    /*-----Update Balance Quantity of Sales Order Detail Ends Here-----*/
                    boolean updateInventoryFlag = (preferences.isWithInvUpdate()) ? false : true;

                    /*-----------------Start Saving Delivery Order Details--------------*/
                    HashMap<String, Object> dodDataMap = new HashMap<String, Object>();
                    dodDataMap.put("srno", salesOrderDetail.getSrno());
                    dodDataMap.put(Constants.companyKey, companyId);
                    dodDataMap.put("doid", deliveryOrder.getID());
                    dodDataMap.put(Constants.productid, salesOrderDetail.getProduct().getID());

                    dodDataMap.put("description", salesOrderDetail.getDescription());
                    dodDataMap.put("deliveredquantity", salesOrderDetail.getBalanceqty());//should be SO balance Qty
                    dodDataMap.put("baseuomdeliveredquantity", authHandler.calculateBaseUOMQuatity(salesOrderDetail.getBalanceqty(), salesOrderDetail.getBaseuomrate(), companyId));

                    dodDataMap.put("baseuomrate", salesOrderDetail.getBaseuomrate());
                    dodDataMap.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(salesOrderDetail.getBalanceqty(), salesOrderDetail.getBaseuomrate(), companyId));
                    dodDataMap.put("quantity", salesOrderDetail.getQuantity());//here check what should be 
                    dodDataMap.put("remark", salesOrderDetail.getRemark());

                    if (preferences.isInventoryAccountingIntegration() && preferences.isWithInvUpdate() && preferences.isUpdateInvLevel()) {

                        if (!StringUtil.isNullOrEmpty(salesOrderDetail.getInvstoreid())) {

                            dodDataMap.put("invstoreid", salesOrderDetail.getInvstoreid());
                        } else {
                            dodDataMap.put("invstoreid", "");
                        }

                        if (!StringUtil.isNullOrEmpty(salesOrderDetail.getInvlocid())) {

                            dodDataMap.put("invlocationid", salesOrderDetail.getInvlocid());
                        } else {
                            dodDataMap.put("invlocationid", "");
                        }

                    }

                    double taxAmt = 0;

                    /*----Line Level Tax---*/
                    if (salesOrderDetail.getTax() != null) {
                        taxAmt = (salesOrderDetail.getRowTaxAmount());
                        dodDataMap.put("prtaxid", salesOrderDetail.getTax().getID());
                        dodDataMap.put("taxamount", taxAmt);
                    }

                    if (salesOrderDetail.getDiscount() != 0) {
                        dodDataMap.put("discount", salesOrderDetail.getDiscount());
                    }
                    if (salesOrderDetail.getDiscountispercent() == 1) {
                        dodDataMap.put("discountispercent", 1);
                    }

                    dodDataMap.put("SalesOrderDetail", sod);

                    /*----BatcDetail Calculation Starts Here-------*/
                    String batchdetails = null;
                    Product product = salesOrderDetail.getProduct();
                    SalesOrder so = salesOrderDetail.getSalesOrder();
                    JSONObject paramJobj = new JSONObject();
                    paramJobj.put("moduleid", Constants.Acc_Delivery_Order_ModuleId);
                    paramJobj.put("companyid", companyId);
                    paramJobj.put(Constants.globalCurrencyKey, salesOrderDetail.getSalesOrder().getCurrency().getCurrencyID());
                    paramJobj.put("numberDo", nextAutoNumber);

                    batchdetails = so.isLockquantityflag() ? getNewBatchJson(product, paramJobj, salesOrderDetail.getID()) : "";

                    /*----BatcDetail Calculation Ends Here-------*/
                    /*----Updating Inventory Table-------*/
                    JSONObject inventoryjson = new JSONObject();
                    inventoryjson.put(Constants.productid, salesOrderDetail.getProduct().getID());
                    inventoryjson.put("quantity", salesOrderDetail.getQuantity());//     inventoryjson.put("quantity", dquantity - venQty); must check whether it is valid or invalid
                    inventoryjson.put("description", salesOrderDetail.getDescription());
                    if (salesOrderDetail.getUom() != null) {
                        inventoryjson.put("uomid", salesOrderDetail.getUom().getID());
                    }
                    inventoryjson.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(salesOrderDetail.getBalanceqty(), salesOrderDetail.getBaseuomrate(), companyId));
                    inventoryjson.put("actquantity", salesOrderDetail.getQuantity());//what is approved quantity also check is it required here?
                    inventoryjson.put("baseuomrate", salesOrderDetail.getBaseuomrate());
                    inventoryjson.put("invrecord", updateInventoryFlag ? true : false);

                    inventoryjson.put("description", salesOrderDetail.getDescription());
                    inventoryjson.put("carryin", false);
                    inventoryjson.put("defective", false);
                    inventoryjson.put("newinventory", false);
                    inventoryjson.put("companyid", companyId);
                    inventoryjson.put("updatedate", salesOrderDetail.getSalesOrder().getOrderDate());


                    /* Adding entry in inventory for invoice*/
                    KwlReturnObject invresult = accProductObj.addInventory(inventoryjson);
                    Inventory inventory = (Inventory) invresult.getEntityList().get(0);

                    /*----Updating Inventory Table End Here-------*/
                    dodDataMap.put("Inventory", inventory);
                    HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
                    JSONObject jobj = new JSONObject();
                    GlobalParams.put(Constants.companyKey, companyId);
                    GlobalParams.put(Constants.globalCurrencyKey, deliveryOrder.getCurrency().getCurrencyID());
                    GlobalParams.put(Constants.df, authHandler.getDateOnlyFormat());
                    GlobalParams.put(Constants.userdf, authHandler.getUserDateFormatterWithoutTimeZone(jobj));
                    Map<String, List<TransactionBatch>> priceValuationMap = new HashMap<>();

                    /* ------------------------Start perpetual Inventory Code-------------------*/
                    if (!(product.getProducttype().getID().equals(Producttype.SERVICE) || product.getProducttype().getID().equals(Producttype.NON_INVENTORY_PART) || product.getProducttype().getID().equals(Producttype.Inventory_Non_Sales))) {
                        if (extraCompanyPreferences != null && (extraCompanyPreferences.isActivateMRPModule() || preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD)) {
                            try {
                                if (product != null && product.getInventoryAccount() != null && product.getCostOfGoodsSoldAccount() != null) {
                                    HashMap<String, Object> requestMap = new HashMap<>();
                                    requestMap.put(Constants.productid, product.getID());

                                    requestMap.put(Constants.companyKey, companyId);
                                    requestMap.put(Constants.df, authHandler.getDateOnlyFormat());
                                    requestMap.put(Constants.globalCurrencyKey, deliveryOrder.getCurrency().getCurrencyID());
                                    requestMap.put("GlobalParams", GlobalParams);
                                    requestMap.put(Constants.REQ_enddate, deliveryOrder.getOrderDate().toString());

                                    requestMap.put("dquantity", authHandler.calculateBaseUOMQuatity(salesOrderDetail.getBalanceqty(), salesOrderDetail.getBaseuomrate(), companyId));
                                    Map<String, Double> batchQuantityMap = new HashMap<>();
                                    if (!StringUtil.isNullOrEmpty(batchdetails)) {
                                        String batchSerialIds = accInvoiceModuleService.getBatchSerialIDs(batchdetails, product, batchQuantityMap);
                                        if (!StringUtil.isNullOrEmpty(batchSerialIds)) {
                                            requestMap.put("batchSerialId", batchSerialIds.split(","));
                                        }
                                    }

                                    // Accrued Purchase Account
                                    JSONObject jedjson = new JSONObject();
                                    jedjson = new JSONObject();
                                    jedjson.put("srno", inventoryJEDetails.size() + 1);
                                    jedjson.put(Constants.companyKey, companyId);
                                    jedjson.put("amount", 0.0);
                                    jedjson.put("accountid", product.getCostOfGoodsSoldAccount() != null ? product.getCostOfGoodsSoldAccount().getID() : "");
                                    jedjson.put("debit", true);
                                    jedjson.put("jeid", inventoryJEid);
                                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                    JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                    dodDataMap.put("cogsjedetailid", jed.getID());
                                    inventoryJEDetails.add(jed);

                                    // Inventory Account
                                    jedjson.put("srno", inventoryJEDetails.size() + 1);
                                    jedjson.put(Constants.companyKey, companyId);
                                    jedjson.put("amount", 0.0);
                                    jedjson.put("accountid", product.getInventoryAccount() != null ? product.getInventoryAccount().getID() : "");
                                    jedjson.put("debit", false);
                                    jedjson.put("jeid", inventoryJEid);
                                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                    dodDataMap.put("inventoryjedetailid", jed.getID());
                                    inventoryJEDetails.add(jed);
                                }
                            } catch (Exception ex) {
                                Logger.getLogger(accInvoiceController.class.getName()).log(Level.WARNING, ex.getMessage());
                            }
                        }
                    }

                    /*--------Ends Perpetual Ineventory Code Here-------*/
                    boolean includingGST = false;
                    /* Set rateincludegst for invoicedetail */

                    /*  Setting Line level Term amount in case of Including GSt true */
                    if (includingGST) {
                        if (salesOrderDetail.getLineLevelTermAmount() != 0) {
                            dodDataMap.put("recTermAmount", salesOrderDetail.getLineLevelTermAmount());

                        }
                    }

                    /* ----- Saving Delivery Order Details Here-----*/
                    KwlReturnObject result1 = accInvoiceDAOobj.saveDeliveryOrderDetails(dodDataMap);
                    DeliveryOrderDetail row = (DeliveryOrderDetail) result1.getEntityList().get(0);
                    row.setRate(salesOrderDetail.getRate());

                    /*---If Including GST true then setting Rate------  */
                    if (salesOrderDetail.getRateincludegst() != 0) {
                        row.setRateincludegst(salesOrderDetail.getRateincludegst());
                        includingGST = true;

                    }

                    /* ----------------------Ending Details save here-------------*/
                    boolean isLock = lockInfo.optBoolean("isLock");
                    boolean isbatchlockedinSO = lockInfo.optBoolean("isbatchlockedinSO");
                    boolean isSeriallockedinSO = lockInfo.optBoolean("isSeriallockedinSO");
                    String replacebatchdetails = "";

                    if (!StringUtil.isNullOrEmpty(batchdetails)) {

                       //this function is to reduce the stock from company level means stock is delivered from company warehouse                          
                        accInvoiceModuleService.saveDONewBatchJson(batchdetails, inventory, paramJobj, row, stockMovementsList, isLock, isbatchlockedinSO, isSeriallockedinSO, replacebatchdetails);

                    }

                    /*---------------------- Get custom/Dimension field from Sales Order which have been used in SO  & also available for Invoice------------------ */
                    HashMap<String, Object> fieldrequestParams1 = new HashMap();
                    HashMap<String, String> customFieldMap = new HashMap<String, String>();
                    HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
                    fieldrequestParams1.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                    fieldrequestParams1.put(Constants.filter_values, Arrays.asList(companyId, Constants.Acc_Sales_Order_ModuleId, 1));
                    HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                    HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams1, replaceFieldMap, customFieldMap, customDateFieldMap);

                    /* Get Custom/Dimension field Data */
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    JSONObject obj = new JSONObject();

                    SalesOrderDetailsCustomData jeDetailCustom = (SalesOrderDetailsCustomData) salesOrderDetail.getSoDetailCustomData();

                    AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);

                    if (jeDetailCustom != null) {
                        JSONObject params = new JSONObject();
                        params.put(Constants.isExport, false);
                        params.put("isForReport", true);
                        params.put(Constants.isdefaultHeaderMap, false);

                        fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);

                    }
                    /*----------------Get Custom/Dimension Filed From SO ends Here-------------*/

                    HashMap requestParams1 = new HashMap();
                    Iterator ite = obj.keys();
                    String fieldLabel = "";
                    String fieldValue = "";

                    while (ite.hasNext()) {
                        fieldLabel = (String) ite.next();
                        fieldValue = obj.getString(fieldLabel);

                        requestParams1.put("fieldname", fieldLabel);
                        requestParams1.put("fieldValue", fieldValue);
                        requestParams1.put("deliveryOrderDetail", row);
                        requestParams1.put("companyid", companyId);
                        requestParams1.put("jedetail", null);//changed
                        requestParams1.put("moduleid", Constants.Acc_Delivery_Order_ModuleId);

                        /* Function used to check custom/dimension field present in invoice
                         also & if present then being saved for DO module
                         */
                        checkAndSaveLineLevelCustomFieldForDO(requestParams1);

                    }

                    /* Get custom/dimension fields data for "Product module"
                     which have been used in SO at line level
                     */
                    HashMap<String, Object> fieldrequestParamsProduct = new HashMap();
                    Map<String, Object> variableMapProduct = new HashMap<String, Object>();
                    fieldrequestParamsProduct.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                    fieldrequestParamsProduct.put(Constants.filter_values, Arrays.asList(companyId, Constants.Acc_Product_Master_ModuleId, 0));
                    HashMap<String, String> replaceFieldMapProduct = new HashMap<String, String>();
                    HashMap<String, Integer> FieldMapProduct = accAccountDAOobj.getFieldParamsMap(fieldrequestParamsProduct, replaceFieldMapProduct);

                    KwlReturnObject resultProduct = accountingHandlerDAOobj.getObject(SalesOrderDetailProductCustomData.class.getName(), salesOrderDetail.getID());
                    SalesOrderDetailProductCustomData objProduct = (SalesOrderDetailProductCustomData) resultProduct.getEntityList().get(0);
                    JSONObject obj1 = new JSONObject();
                    if (objProduct != null) {
                        JSONObject params = new JSONObject();
                        params.put(Constants.isExport, false);
                        params.put("isForReport", true);

                        /* Preparing a map with key "Field Label" &  data as "Field Value" 
                         for product custom field used in Sales Order
                         */
                        setCustomColumnValuesForProduct(objProduct, FieldMapProduct, replaceFieldMapProduct, variableMapProduct, params);
                        for (Map.Entry<String, Object> varEntry : variableMapProduct.entrySet()) {
                            String coldata = varEntry.getValue().toString();
                            if (!StringUtil.isNullOrEmpty(coldata)) {
                                obj1.put(varEntry.getKey(), coldata);
                                obj1.put("key", varEntry.getKey());
//               
                            }
                        }

                        ite = obj1.keys();
                        while (ite.hasNext()) {

                            fieldLabel = (String) ite.next();
                            fieldValue = obj1.getString(fieldLabel);

                            requestParams1.put("fieldname", fieldLabel);
                            requestParams1.put("fieldValue", fieldValue);
                            requestParams1.put("deliveryOrderDetail", row);
                            requestParams1.put("companyid", companyId);
                            requestParams1.put("moduleid", Constants.Acc_Product_Master_ModuleId);
                            requestParams1.put("isFromProduct", true);

                            /* Function used to check custom/dimension field present in invoice
                             also & if present then being saved for Invoice module
                             */
                            checkAndSaveLineLevelCustomFieldForDO(requestParams1);

                        }

                    }

                    rows.add(row);
                }

                if (extraCompanyPreferences != null && extraCompanyPreferences.isActivateInventoryTab() && !stockMovementsList.isEmpty()) {
                    stockMovementService.addOrUpdateBulkStockMovement(deliveryOrder.getCompany(), deliveryOrder.getID(), stockMovementsList);
                }
            }

        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveInvoiceDetail : " + ex.getMessage(), ex);
        }

        ll.add(new double[]{totaldiscount, totalamount, taxamount, lineLevelTermAmount});
        ll.add(rows);
        ll.add(prodList);
        return ll;
    }

    /*------------ Code to get Batch Details Json While creating Bulk DO from SO---------- */
    public String getNewBatchJson(Product product, JSONObject paramJobj, String documentid) throws ServiceException, SessionExpiredException, JSONException {
        JSONArray jSONArray = new JSONArray();
        DateFormat df = authHandler.getDateFormatter(paramJobj);
        KwlReturnObject kmsg = null;
        boolean linkingFlag = (StringUtil.isNullOrEmpty(paramJobj.optString("linkingFlag", null))) ? false : Boolean.parseBoolean((String) paramJobj.get("linkingFlag"));
        boolean isEdit = (StringUtil.isNullOrEmpty(paramJobj.optString("isEdit", null))) ? false : Boolean.parseBoolean((String) paramJobj.get("isEdit"));
        boolean isConsignment = (StringUtil.isNullOrEmpty(paramJobj.optString("isConsignment", null))) ? false : Boolean.parseBoolean((String) paramJobj.get("isConsignment"));
        String moduleID = paramJobj.optString("moduleid", null);
        boolean isBatch = false;
        if (!product.isIsBatchForProduct() && !product.isIslocationforproduct() && !product.isIswarehouseforproduct() && !product.isIsrowforproduct() && !product.isIsrackforproduct() && !product.isIsbinforproduct() && product.isIsSerialForProduct()) {
            kmsg = accCommonTablesDAO.getOnlySerialDetails(documentid, linkingFlag, moduleID, isConsignment, isEdit);
        } else {
            isBatch = true;
            kmsg = accCommonTablesDAO.getBatchSerialDetails(documentid, !product.isIsSerialForProduct(), linkingFlag, moduleID, isConsignment, isEdit, "");
        }

        HashMap<String, Object> fieldrequestParams = new HashMap();
        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
        fieldrequestParams.put(Constants.filter_values, Arrays.asList((String) paramJobj.get(Constants.companyKey), Constants.SerialWindow_ModuleId, 1));
        HashMap<String, String> customFieldMap = new HashMap<String, String>();
        HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
        HashMap<String, String> replaceFieldMap1 = new HashMap<String, String>();
        // HashMap<String, Integer> fieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, replaceFieldMap);
        HashMap<String, Integer> fieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap1, customFieldMap, customDateFieldMap);
//        product.getName()
        double ActbatchQty = 1;
        double approvedSerialQty = 0;
        double batchQty = 0;
        List batchserialdetails = kmsg.getEntityList();
        Iterator iter = batchserialdetails.iterator();
        while (iter.hasNext()) {
            Object[] objArr = (Object[]) iter.next();
            JSONObject obj = new JSONObject();
            obj.put("id", objArr[0] != null ? (String) objArr[0] : "");
            obj.put("location", objArr[2] != null ? (String) objArr[2] : "");
            obj.put("warehouse", objArr[3] != null ? (String) objArr[3] : "");
            if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                obj.put("purchasebatchidValue", objArr[1] != null ? (String) objArr[1] : "");
            } else {
                obj.put("batch", objArr[1] != null ? (String) objArr[1] : "");
                obj.put("batchname", objArr[1] != null ? (String) objArr[1] : "");
            }
            if (isBatch) {
                obj.put("row", objArr[15] != null ? (String) objArr[15] : "");
                obj.put("rack", objArr[16] != null ? (String) objArr[16] : "");
                obj.put("bin", objArr[17] != null ? (String) objArr[17] : "");
            }
            if ((product.isIsBatchForProduct() || product.isIslocationforproduct() || product.isIswarehouseforproduct() || product.isIsrowforproduct() || product.isIsrackforproduct() || product.isIsbinforproduct()) && product.isIsSerialForProduct()) {
                if (isConsignment && linkingFlag) {
                    ActbatchQty = accCommonTablesDAO.getApprovedSerialQty(documentid, (String) objArr[0], isEdit);
                } else {
                    ActbatchQty = accCommonTablesDAO.getBatchQuantity(documentid, (String) objArr[0]);
                }

                if (batchQty == 0) {
                    batchQty = ActbatchQty;
                }
                if (batchQty == ActbatchQty) {
                    obj.put("isreadyonly", false);
                    obj.put("quantity", ActbatchQty);
                } else {
                    obj.put("isreadyonly", true);
                    obj.put("quantity", "");
                }

            } else {
                obj.put("isreadyonly", false);
                obj.put("quantity", ActbatchQty);
            }
            if (!product.isIsBatchForProduct() && !product.isIslocationforproduct() && !product.isIswarehouseforproduct() && !product.isIsrowforproduct() && !product.isIsrackforproduct() && !product.isIsbinforproduct() && product.isIsSerialForProduct()) {
                obj.put("mfgdate", "");
                obj.put("expdate", "");
            } else {
                obj.put("mfgdate", objArr[4] != null ? df.format(objArr[4]) : "");
                obj.put("expdate", objArr[5] != null ? df.format(objArr[5]) : "");
            }

//            obj.put("quantity", objArr[6] != null ? objArr[6] : "");
//            obj.put("quantity",ActbatchQty);
            obj.put("lockquantity", objArr[12] != null ? objArr[12] : "");

            if ((product.isIsBatchForProduct() || product.isIslocationforproduct() || product.isIswarehouseforproduct() || product.isIsrowforproduct() || product.isIsrackforproduct() || product.isIsbinforproduct()) && !product.isIsSerialForProduct()) {
                obj.put("quantity", objArr[11] != null ? objArr[11] : "");
            }

            if (!StringUtil.isNullOrEmpty(product.getID())) {
                obj.put("productid", product.getID());
            }
            obj.put("balance", 0);
            obj.put("asset", "");
            obj.put("serialnoid", objArr[7] != null ? (String) objArr[7] : "");
            if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                obj.put("purchaseserialidValue", objArr[8] != null ? (String) objArr[8] : "");
            } else {
                obj.put("serialno", objArr[8] != null ? (String) objArr[8] : "");
            }

            obj.put("skufield", objArr[13] != null ? (String) objArr[13] : "");
            obj.put("purchasebatchid", objArr[0] != null ? (String) objArr[0] : "");
            obj.put("purchaseserialid", objArr[7] != null ? (String) objArr[7] : "");
            obj.put("expstart", (objArr[9] != null && !objArr[9].toString().equalsIgnoreCase("")) ? df.format(objArr[9]) : "");
            obj.put("expend", (objArr[10] != null && !objArr[10].toString().equalsIgnoreCase("")) ? df.format(objArr[10]) : "");
            obj.put("documentid", documentid != null ? documentid : "");
            if (linkingFlag && isConsignment && !isEdit) { //For geting only unused Serial batch details in DO
                if (product.isIsSerialForProduct() && objArr[7] != null) {
                    KwlReturnObject result1 = accountingHandlerDAOobj.getObject(NewBatchSerial.class.getName(), objArr[7].toString());
                    NewBatchSerial newBatchSerial = (NewBatchSerial) result1.getEntityList().get(0);
                    if (newBatchSerial != null && newBatchSerial.getQuantitydue() == 0) {
                        //                    batchQty--;
                        continue;
                    }
                } else if (product.isIsBatchForProduct() && !product.isIsSerialForProduct() && objArr[0] != null) {
                    KwlReturnObject result1 = accountingHandlerDAOobj.getObject(NewProductBatch.class.getName(), objArr[0].toString());
                    NewProductBatch newProductBatch = (NewProductBatch) result1.getEntityList().get(0);
                    if (newProductBatch != null && newProductBatch.getQuantitydue() == 0) {
                        continue;
                    }
                }
            }
            if (objArr[14] != null && !objArr[14].toString().equalsIgnoreCase("")) {
                KwlReturnObject result1 = accountingHandlerDAOobj.getObject(SerialDocumentMapping.class.getName(), objArr[14].toString());
                SerialDocumentMapping sdm = (SerialDocumentMapping) result1.getEntityList().get(0);
                Map<String, Object> variableMap = new HashMap<String, Object>();
                SerialCustomData serialCustomData = (SerialCustomData) sdm.getSerialCustomData();
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                AccountingManager.setCustomColumnValues(serialCustomData, fieldMap, replaceFieldMap, variableMap);
                for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                    String coldata = varEntry.getValue().toString();
                    String valueForReport = "";
                    if (customFieldMap.containsKey(varEntry.getKey()) && coldata != null) {
                        try {
                            String[] valueData = coldata.split(",");
                            for (String value : valueData) {
                                FieldComboData fieldComboData = (FieldComboData) kwlCommonTablesDAOObj.getClassObject(FieldComboData.class.getName(), value);
                                if (fieldComboData != null) {
//                                    valueForReport += fieldComboData.getValue() + ",";
                                    valueForReport += value + ",";
                                }
                            }
                            if (valueForReport.length() > 1) {
                                valueForReport = valueForReport.substring(0, valueForReport.length() - 1);
                            }
                            obj.put(varEntry.getKey(), valueForReport);//fieldComboData.getValue()!=null ?fieldComboData.getValue():"");
                        } catch (Exception ex) {
                            obj.put(varEntry.getKey(), coldata);
                        }
                    } else if (customDateFieldMap.containsKey(varEntry.getKey())) {
                        DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
                        DateFormat defaultDateFormat = new SimpleDateFormat(Constants.MMMMdyyyy);
                        Date dateFromDB = null;
                        try {
                            dateFromDB = defaultDateFormat.parse(coldata);
                            coldata = df2.format(dateFromDB);

                        } catch (Exception e) {
                        }
                        obj.put(varEntry.getKey(), coldata);
                    } else {
                        if (!StringUtil.isNullOrEmpty(coldata)) {
                            obj.put(varEntry.getKey(), coldata);
                        }
                    }
                }
            }
            String serialNoId = objArr[7] != null ? (String) objArr[7] : "";
            int transType = Constants.Acc_ConsignmentSalesReturn_ModuleId;
            String docId = "";
            KwlReturnObject reusablecountobj = accCommonTablesDAO.getSerialsReusableCount(product.getID(), objArr[8] != null ? (String) objArr[8] : "", product.getCompany().getCompanyID(), transType, false, docId, objArr[0] != null ? (String) objArr[0] : "");
            if (reusablecountobj.getEntityList() != null && !reusablecountobj.getEntityList().isEmpty()) {
                if (reusablecountobj.getEntityList().get(0) != null) {
                    double sumCount = Double.parseDouble(reusablecountobj.getEntityList().get(0).toString());
                    obj.put("reusablecount", sumCount);
                } else {
                    obj.put("reusablecount", 0);
                }
            } else {
                obj.put("reusablecount", 0);
            }
            jSONArray.put(obj);
            batchQty--;

        }

        return jSONArray.toString();
    }

    /*  Function used to check custom/dimension field for invoice module , 
     if present then being saved custom/dimension 
     */
    private void checkAndSaveLineLevelCustomField(HashMap requestParams) throws ServiceException {

        HashMap requestParams1 = new HashMap();
        JSONObject jsonObject = new JSONObject();
        FieldParams fieldParams = null;
        JSONArray jcustomarray = new JSONArray();
        JSONObject jedjson = new JSONObject();

        String companyid = (String) requestParams.get("companyid");
        String fieldname = (String) requestParams.get("fieldname");
        String fieldValue = (String) requestParams.get("fieldValue");
        InvoiceDetail row = (InvoiceDetail) requestParams.get("invoicedetail");
        JournalEntryDetail jed = (JournalEntryDetail) requestParams.get("jedetail");
        int moduleid = (Integer) requestParams.get("moduleid");
        boolean isFromProduct = requestParams.get("isFromProduct") != null ? (Boolean) requestParams.get("isFromProduct") : false;
        try {

            requestParams1.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.Acc_custom_field));

            requestParams1.put(Constants.filter_values, Arrays.asList(companyid, moduleid, fieldname));

            if (isFromProduct) {
                requestParams1.put("isFromProduct", true);
            }

            /* Check whether can we use existing function written in FieldDatamanger or accountImpl */
            List result1 = accAccountDAOobj.getFieldParamsFieldLabelWise(requestParams1); // get custom field module wise from fieldlabel
            if (result1 != null && result1.size() > 0) {
                fieldParams = (FieldParams) result1.get(0);

                jsonObject.put("fieldid", fieldParams.getId());
                jsonObject.put("xtype", fieldParams.getFieldtype());
                jsonObject.put("fieldname", fieldParams.getFieldname());
                jsonObject.put(fieldParams.getFieldname(), "Col" + fieldParams.getColnum());
                jsonObject.put("refcolumn_name", "Col" + fieldParams.getRefcolnum());

                if (fieldParams.getFieldtype() == Constants.SINGLESELECTCOMBO || fieldParams.getFieldtype() == Constants.MULTISELECTCOMBO) {
                    String ids = fieldManagerDAOobj.getIdsUsingParamsValue(fieldParams.getId(), fieldValue);     // get ids for module using values and field id  
                    fieldValue = ids;

                }
                jsonObject.put("Col" + fieldParams.getColnum(), fieldValue);
                jsonObject.put("fieldDataVal", fieldValue);

                jcustomarray.put(jsonObject);
            }

            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
            KwlReturnObject jedresult = null;
            if (isFromProduct) {
                // Add Custom fields details for Product

                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);
                customrequestParams.put("modulerecid", jed.getID());
                customrequestParams.put(Constants.moduleid, Constants.Acc_Invoice_ModuleId);
                customrequestParams.put("recdetailId", row.getInventory().getID());
                customrequestParams.put("productId", row.getInventory().getProduct().getID());
                customrequestParams.put(Constants.companyKey, companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_JEDetail_Productcustom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    jedjson.put("accjedetailproductcustomdataref", jed.getID());
                    jedjson.put("jedid", jed.getID());
                    jedresult = accJournalEntryobj.updateJournalEntryDetails(jedjson);
                }
            } else {

                // Add Custom fields details 
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                customrequestParams.put("modulerecid", jed.getID());
                customrequestParams.put("recdetailId", row.getInventory().getID());
                customrequestParams.put(Constants.moduleid, Constants.Acc_Invoice_ModuleId);
                customrequestParams.put(Constants.companyKey, companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    jedjson.put("accjedetailcustomdata", jed.getID());
                    jedjson.put("jedid", jed.getID());
                    jedresult = accJournalEntryobj.updateJournalEntryDetails(jedjson);
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("checkAndSaveCustomField : " + ex.getMessage(), ex);
        }

    }

    /*  Function used to check custom/dimension field for DO module , 
     if present then being saved custom/dimension 
     */
    private void checkAndSaveLineLevelCustomFieldForDO(HashMap requestParams) throws ServiceException {

        HashMap requestParams1 = new HashMap();
        JSONObject jsonObject = new JSONObject();
        FieldParams fieldParams = null;
        JSONArray jcustomarray = new JSONArray();

        HashMap deliveryOrderMap = new HashMap();

        String companyid = (String) requestParams.get("companyid");
        String fieldname = (String) requestParams.get("fieldname");
        String fieldValue = (String) requestParams.get("fieldValue");
        DeliveryOrderDetail row = (DeliveryOrderDetail) requestParams.get("deliveryOrderDetail");

        int moduleid = (Integer) requestParams.get("moduleid");
        boolean isFromProduct = requestParams.get("isFromProduct") != null ? (Boolean) requestParams.get("isFromProduct") : false;
        try {

            requestParams1.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.Acc_custom_field));

            requestParams1.put(Constants.filter_values, Arrays.asList(companyid, moduleid, fieldname));

            if (isFromProduct) {
                requestParams1.put("isFromProduct", true);
            }

            /* Check whether can we use existing function written in FieldDatamanger or accountImpl */
            List result1 = accAccountDAOobj.getFieldParamsFieldLabelWise(requestParams1); // get custom field module wise from fieldlabel
            if (result1 != null && result1.size() > 0) {
                fieldParams = (FieldParams) result1.get(0);

                jsonObject.put("fieldid", fieldParams.getId());
                jsonObject.put("xtype", fieldParams.getFieldtype());
                jsonObject.put("fieldname", fieldParams.getFieldname());
                jsonObject.put(fieldParams.getFieldname(), "Col" + fieldParams.getColnum());
                jsonObject.put("refcolumn_name", "Col" + fieldParams.getRefcolnum());

                if (fieldParams.getFieldtype() == Constants.SINGLESELECTCOMBO || fieldParams.getFieldtype() == Constants.MULTISELECTCOMBO) {
                    String ids = fieldManagerDAOobj.getIdsUsingParamsValue(fieldParams.getId(), fieldValue);     // get ids for module using values and field id  
                    fieldValue = ids;

                }
                jsonObject.put("Col" + fieldParams.getColnum(), fieldValue);
                jsonObject.put("fieldDataVal", fieldValue);

                jcustomarray.put(jsonObject);
            }

            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();

            if (isFromProduct) {
                // Add Custom fields details for Product

                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", "DeliveryorderDetail");
                customrequestParams.put("moduleprimarykey", "DoDetailID");
                customrequestParams.put("modulerecid", row.getID());
                customrequestParams.put(Constants.moduleid, Constants.Acc_Delivery_Order_ModuleId);
                customrequestParams.put("recdetailId", row.getID());
                customrequestParams.put("productId", row.getInventory().getProduct().getID());
                customrequestParams.put(Constants.companyKey, companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_DODETAIL_Productcustom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);

            } else {

                // Add Custom fields details 
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", "DeliveryOrderDetail");
                customrequestParams.put("moduleprimarykey", "DeliveryOrderDetailId");
                customrequestParams.put("modulerecid", row.getID());
                customrequestParams.put(Constants.moduleid, Constants.Acc_Delivery_Order_ModuleId);
                customrequestParams.put(Constants.companyKey, companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_DeliveryOrderDetail_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    deliveryOrderMap.put("dodetailscustomdataref", row.getID());
                    accInvoiceDAOobj.updateDODetailsCustomData(deliveryOrderMap);
                }

            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("checkAndSaveLineLevelCustomFieldForDO : " + ex.getMessage(), ex);
        }

    }

    /* 
     function is used to prepare a map for product custom data  used in Sales Order
     */
    private void setCustomColumnValuesForProduct(SalesOrderDetailProductCustomData soDetailsProductCustomData, HashMap<String, Integer> fieldMap, Map<String, String> replaceFieldMap,
            Map<String, Object> variableMap, JSONObject params) {
        for (Map.Entry<String, Integer> field : fieldMap.entrySet()) {
            boolean isForReport = params.optBoolean("isForReport", false);
            boolean isExport = params.optBoolean(Constants.isExport, false);
            Integer colnumber = field.getValue();
            if (colnumber > 0) { // colnumber will be 0 if key is part of reference map
                Integer isref = fieldMap.get(field.getKey() + "#" + colnumber);// added '#' while creating map collection for custom fields.
                // Without this change, it creates problem if two custom columns having name like XYZ and XYZ1
                String coldata = null;
                if (isref != null) {
                    try {
                        if (soDetailsProductCustomData != null) {
                            coldata = soDetailsProductCustomData.getCol(colnumber);
                        }
                        if (!StringUtil.isNullOrEmpty(coldata)) {
                            if (coldata.length() > 1) {
                                if (isref == 1) {
                                } else if (isref == 0 || isref == 7) {
                                    if (isForReport) {
                                        String valueForReport = "";
                                        String[] valueData = coldata.split(",");
                                        for (String value : valueData) {
                                            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), value);
                                            FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                                            if (fieldComboData != null) {
                                                valueForReport += fieldComboData.getValue() + ",";
                                            }
                                        }
                                        if (valueForReport.length() > 1) {
                                            coldata = valueForReport.substring(0, valueForReport.length() - 1);
                                        }
                                    } else {
                                        coldata = coldata;
                                    }
                                } else if (isref == 3 && isExport) {
                                    DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
                                    DateFormat defaultDateFormat = new SimpleDateFormat(Constants.MMMMdyyyy);
                                    Date dateFromDB = null;
                                    try {
                                        dateFromDB = defaultDateFormat.parse(coldata);
                                        coldata = df2.format(dateFromDB);

                                    } catch (Exception e) {
                                    }
                                }
                            }
                            variableMap.put(field.getKey(), coldata);
                        }
                    } catch (IllegalArgumentException ex) {
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    /*  Function is used to get Line level status of SO
     whether they are used or unused
     */
    public double getSalesOrderDetailStatus(SalesOrderDetail sod) throws ServiceException {
        double result = sod.getQuantity();
        KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), sod.getCompany().getCompanyID());
        CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
        KwlReturnObject idresult = accInvoiceDAOobj.getIDFromSOD(sod.getID());
        List list = idresult.getEntityList();
        Iterator ite1 = list.iterator();
        double qua = 0.0;
        boolean fullInv = false;
        double quantPartTtInv = 0.0;
        while (ite1.hasNext()) {
            InvoiceDetail ge = (InvoiceDetail) ite1.next();
            if (ge.getInvoice().isPartialinv()) {

                double quantity = ge.getInventory().getQuantity();
                quantPartTtInv += quantity * ge.getPartamount();
            } else {
                fullInv = true;

                qua += ge.getInventory().getQuantity();
            }
        }

        if (fullInv) {
            result = sod.getQuantity() - qua;
        } else {
            if (sod.getQuantity() * 100 > quantPartTtInv) {
                result = sod.getQuantity() - qua;
            } else {
                result = 0;
            }
        }

        return result;
    }

    public double getDeliveryOrderDetailStatus(DeliveryOrderDetail sod) throws ServiceException {
        double result = sod.getDeliveredQuantity();

        KwlReturnObject idresult = accInvoiceDAOobj.getIDFromDOD(sod.getID());
        List list = idresult.getEntityList();
        Iterator ite1 = list.iterator();
        double qua = 0.0;
        boolean fullInv = false;
        double quantPartTtInv = 0.0;
        while (ite1.hasNext()) {
            InvoiceDetail ge = (InvoiceDetail) ite1.next();
            if (ge.getInvoice().isPartialinv()) {
//                Need to test properly.
//                double quantity = ge.getInventory().isInvrecord() ? ge.getInventory().getQuantity() : ge.getInventory().getActquantity();
                double quantity = ge.getInventory().getQuantity();
                quantPartTtInv += quantity * ge.getPartamount();
            } else {
                fullInv = true;
//                qua += ge.getInventory().isInvrecord() ? ge.getInventory().getQuantity() : ge.getInventory().getActquantity();
                qua += ge.getInventory().getQuantity();
            }
        }

        KwlReturnObject idresultsr = accInvoiceDAOobj.getIDFromDODFORSR(sod.getID());
        List listsr = idresultsr.getEntityList();
        Iterator ite2 = listsr.iterator();
        double returnqua = 0;
        while (ite2.hasNext()) {
            SalesReturnDetail ge = (SalesReturnDetail) ite2.next();
            returnqua += ge.getInventory().getQuantity();
        }

        if (fullInv) {
            result = sod.getDeliveredQuantity() - (qua + returnqua);
        } else {
            if (sod.getDeliveredQuantity() * 100 > quantPartTtInv) {
                result = sod.getDeliveredQuantity() - (qua + returnqua);
            } else {
                result = 0;
            }
        }
//        result = result - qua;
        return result;
    }

    private void deleteAssetDetailsForDO(String doId, String companyId) throws ServiceException {
        HashMap<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put("companyid", companyId);
        requestMap.put("doid", doId);
        requestMap.put("deleteMappingAlso", true);
        accInvoiceDAOobj.deleteAssetDetailsLinkedWithDeliveryOrder(requestMap);
    }

    private void deleteDOContractMappings(String doId, String companyId) throws ServiceException {
        HashMap<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put("companyid", companyId);
        requestMap.put("doid", doId);
        accInvoiceDAOobj.deleteDOContractMappings(requestMap);
    }

    private void deleteAssetDetails(Invoice invoice, String companyId) throws ServiceException {
        HashMap<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put("companyid", companyId);
        requestMap.put("invoiceid", invoice.getID());
        requestMap.put("deleteMappingAlso", true);
        accInvoiceDAOobj.deleteAssetDetailsLinkedWithInvoice(requestMap);
    }

    private void deleteInvoiceContractMappings(Invoice invoice, String companyId) throws ServiceException {
        HashMap<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put("companyid", companyId);
        requestMap.put("invoiceid", invoice.getID());
        accInvoiceDAOobj.deleteInvoiceContractMappings(requestMap);
    }

//    public Set<AssetInvoiceDetailMapping> saveAssetInvoiceDetailMapping(String invoiceDetailId, Set<AssetDetails> assetDetailsSet, String companyId, int moduleId) throws AccountingException {
//        Set<AssetInvoiceDetailMapping> assetInvoiceDetailMappings = new HashSet<AssetInvoiceDetailMapping>();
//        try {
//            for (AssetDetails assetDetails : assetDetailsSet) {
//                HashMap<String, Object> dataMap = new HashMap<String, Object>();
//                dataMap.put("invoiceDetail", invoiceDetailId);
//                dataMap.put("moduleId", moduleId);
//                dataMap.put("assetDetails", assetDetails.getId());
//                dataMap.put("company", companyId);
//                KwlReturnObject object = accProductObj.saveAssetInvoiceDetailMapping(dataMap);
//
//                AssetInvoiceDetailMapping detailMapping = (AssetInvoiceDetailMapping) object.getEntityList().get(0);
//                assetInvoiceDetailMappings.add(detailMapping);
//            }
//        } catch (ServiceException ex) {
//            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
//            throw new AccountingException("Error while processing data.");
//        }
//        return assetInvoiceDetailMappings;
//    }
//    public Set<AssetDetails> saveAssetDetails(HttpServletRequest request, String productId, String assetDetails, int assetSoldFlag, boolean isUsedFlag, boolean isFromInvoice, boolean isLeaseFixedAsset, boolean isFromSalesReturn) throws SessionExpiredException, AccountingException, UnsupportedEncodingException {
//        Set<AssetDetails> assetDetailsSet = new HashSet<AssetDetails>();
//        try {
//            JSONArray jArr = new JSONArray(assetDetails);
//            String companyId = sessionHandlerImpl.getCompanyid(request);
//
//            for (int i = 0; i < jArr.length(); i++) {
//                JSONObject jobj = jArr.getJSONObject(i);
//                String assetId = StringUtil.DecodeText(jobj.optString("assetId"));
//                double sellAmt = jobj.optDouble("sellAmount", 0);
//
//                HashMap<String, Object> dataMap = new HashMap<String, Object>();
//
//                KwlReturnObject DOObj = accountingHandlerDAOobj.getObject(AssetDetails.class.getName(), assetId);
//                AssetDetails assetDetail = (AssetDetails) DOObj.getEntityList().get(0);
//                String assetName = StringUtil.isNullOrEmpty(assetDetail.getAssetId()) ? "" : assetDetail.getAssetId();
//
//                if (isFromSalesReturn && isLeaseFixedAsset) {
//                    dataMap.put("assetDetailId", assetId);
//                    dataMap.put("productId", productId);
//                    dataMap.put("isLinkedToLeaseSO", false);// in case of lease sales return flag will be 0
//                } else {
//                    if (isLeaseFixedAsset) {
//                        if (isFromInvoice) {
//                            dataMap.put("isLeaseInvoiceCreated", true);
//                        } else {
//                            dataMap.put("isLeaseDOCreated", true);
//                        }
//                    } else {
//                        dataMap.put("isUsedFlag", isUsedFlag);// this flag is only for normal FA INVOICE AND FA DO
//                    }
//                    dataMap.put("assetDetailId", assetId);
//                    dataMap.put("sellAmount", sellAmt);
//                    dataMap.put("productId", productId);
//                    dataMap.put("invrecord", true);
////                    dataMap.put("isUsedFlag", isUsedFlag);
//                    dataMap.put("assetSoldFlag", assetSoldFlag);
//                }
//                dataMap.put("companyId", companyId);
//
//                KwlReturnObject result = accProductObj.updateAssetDetails(dataMap);
//
////                if (!isFromInvoice && assetSoldFlag == 2 && jobj.has("batchdetails") && jobj.getString("batchdetails") != null) {
//                if (!isFromInvoice && jobj.has("batchdetails") && jobj.getString("batchdetails") != null) {
//                    String batchDetails = jobj.getString("batchdetails");
//                    if (!StringUtil.isNullOrEmpty(batchDetails) && !batchDetails.equalsIgnoreCase("null")) {
//                        String assetMainId = assetId;
//                        dataMap.put("assetDetailId", assetMainId);
//                        ProductBatch productBatch = null;
//
//                        if (isFromSalesReturn) {
//                            productBatch = saveAssetSRBatch(batchDetails, assetMainId, productId, request);
//                        } else {
////                            productBatch = saveAssetBatch(batchDetails, assetMainId, assetName, productId, isFromSalesReturn, isFromInvoice, request);
//                            saveAssetNewBatch(batchDetails, assetMainId, assetName, productId, isFromSalesReturn, isFromInvoice, assetId, request);
//                        }
//
////                        if (productBatch != null) {
////                            dataMap.put("batch", productBatch.getId());
////                            dataMap.put("id", assetId);
////                            result = accProductObj.updateAssetDetails(dataMap);
////                            //     row = (AssetDetails) result.getEntityList().get(0);
////                        }
//                    }
//                }
//
//                assetDetail = (AssetDetails) result.getEntityList().get(0);
//
//                assetDetailsSet.add(assetDetail);
//
//            }
//        } catch (ServiceException ex) {
//            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
//            throw new AccountingException("Error While Processing Data");
//        } catch (ParseException ex) {
//            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
//            throw new AccountingException("Error While Processing Data");
//        } catch (JSONException ex) {
//            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
//            throw new AccountingException("Error While Processing Data");
//        }
//        return assetDetailsSet;
//    }
    public double getInvoiceDetailAMount(SalesOrderDetail sod) throws ServiceException {

        KwlReturnObject idresult = accInvoiceDAOobj.getIDFromSOD(sod.getID());
        List list = idresult.getEntityList();
        Iterator ite1 = list.iterator();
        double quantPartTtInv = 0.0;
        while (ite1.hasNext()) {
            InvoiceDetail ge = (InvoiceDetail) ite1.next();
            if (ge.getInvoice().isPartialinv()) {
//                double quantity = ge.getInventory().isInvrecord()? ge.getInventory().getQuantity() : ge.getInventory().getActquantity();      
                double quantity = ge.getInventory().getQuantity();
                quantPartTtInv += quantity * ge.getPartamount();
            }
        }

        return quantPartTtInv;
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

    public ModelAndView saveRepeateInvoiceInfo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        String repeateid = "";
        String detail = "";
        int moduleid = 0;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("RIC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            GregorianCalendar gc = new GregorianCalendar(); //It returns actual Date object            
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            HashMap<String, Object> requestParams = new HashMap<String, Object>();  //Map for notification mail data
            String loginUserId = sessionHandlerImpl.getUserid(request);
            requestParams.put("loginUserId", loginUserId);
//            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
//            sdf.setTimeZone(TimeZone.getTimeZone("GMT"+sessionHandlerImpl.getTimeZoneDifference(request)));

            HashMap<String, Object> dataMap = new HashMap<String, Object>();

            boolean allowEditingRecurredDocuments = StringUtil.isNullOrEmpty(request.getParameter("alloweditingrecurreddocuments")) ? false : Boolean.parseBoolean(request.getParameter("alloweditingrecurreddocuments"));
            String editedRecurredDocumentsApprover = !StringUtil.isNullOrEmpty(request.getParameter("approverofediteddocument")) ? request.getParameter("approverofediteddocument") : "";
            dataMap.put("allowEditingRecurredDocuments", allowEditingRecurredDocuments);
            dataMap.put("editedRecurredDocumentsApprover", editedRecurredDocumentsApprover);

            int intervalUnit = Integer.parseInt(request.getParameter("interval"));
            dataMap.put("intervalUnit", intervalUnit);
            boolean isActivate = StringUtil.isNullOrEmpty(request.getParameter("isactivate")) ? true : Boolean.parseBoolean(request.getParameter("isactivate"));
            int NoOfpost = Integer.parseInt(request.getParameter("NoOfpost"));
            dataMap.put("NoOfpost", NoOfpost);
            dataMap.put("intervalType", request.getParameter("intervalType"));
            Date startDate = df.parse(request.getParameter("startDate"));
//            dataMap.put("isCustomer", request.getParameter("isCustomer"));
            LinkedHashMap<String, Object> oldRecurringInvoicePrmt = new LinkedHashMap<String, Object>();
            Map<String, Object> RecurringinvoicePrmt = new HashMap<String, Object>();
            Map<String, Object> newAuditKey = new HashMap<String, Object>();

            //By default every recurring invoice ll be considered as Pending for approval. So if user do recurring by mistake, he need not to worry about it. Invoice ll recur only on his approval            
            boolean isEdit = StringUtil.isNullOrEmpty(request.getParameter("isedit")) ? false : Boolean.parseBoolean(request.getParameter("isedit"));
            if (!isEdit) {
                //boolean ispendingapproval = true;
                String approver = "";
                int notifyme = StringUtil.isNullOrEmpty(request.getParameter("notifyme")) ? 1 : Integer.parseInt(request.getParameter("notifyme"));
                if (notifyme == 1) {  // 0 : Auto Recurring, 1: Pending Recurring JE
                    dataMap.put("isactivate", false);
                    approver = !StringUtil.isNullOrEmpty(request.getParameter("approver")) ? request.getParameter("approver") : "";
                    dataMap.put("approver", approver);
                    dataMap.put("ispendingapproval", true);    //1: Pending Recurring JE
                    requestParams.put("ispendingapproval", true);
                } else {    //Auto Entry
                    dataMap.put("approver", approver);
                    dataMap.put("isactivate", isActivate);  //isActivate=true means recurring invoice is in active mode.                    
                    dataMap.put("ispendingapproval", false);
                    requestParams.put("ispendingapproval", false);
                }
            }

            String repeateId = request.getParameter("repeateid");
            boolean isnew = false;
            int advanceNoofDays = StringUtil.isNullOrEmpty(request.getParameter("advancedays")) ? 0 : Integer.parseInt(request.getParameter("advancedays"));
            DateFormat dff = authHandler.getDateOnlyFormat();
            Date invoiceAdvanceCreationDate = !StringUtil.isNullOrEmpty(request.getParameter("advanceDate")) ? dff.parse(request.getParameter("advanceDate")) : startDate;

            dataMap.put("advancedays", advanceNoofDays);
            dataMap.put("advanceDate", invoiceAdvanceCreationDate);
            if (StringUtil.isNullOrEmpty(repeateId)) {
                dataMap.put("startDate", startDate);
                dataMap.put("nextDate", startDate);
                requestParams.put("nextDate", startDate);
                gc.setTime(startDate);
                isnew = true;
            } else {
                dataMap.put("id", repeateId);
                Date nextDate = startDate;//RepeatedInvoices.calculateNextDate(startDate, intervalUnit, request.getParameter("intervalType"));
                dataMap.put("nextDate", nextDate);
                requestParams.put("nextDate", nextDate);
                gc.setTime(nextDate);
            }
            if (advanceNoofDays > 0) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(invoiceAdvanceCreationDate);
                cal.add(Calendar.DATE, -1);
                Date dateBefore1Day = cal.getTime();
                gc.setTime(dateBefore1Day);
            } else {
                gc.add(Calendar.DAY_OF_YEAR, -1);
            }
            String gcString = authHandler.getDateOnlyFormat().format(gc.getTime());
            Date gcDate = authHandler.getDateOnlyFormat().parse(gcString);
            Date prevDate = gcDate;
            dataMap.put("prevDate", prevDate);

            if (!StringUtil.isNullOrEmpty(request.getParameter("expireDate"))) {
                dataMap.put("expireDate", df.parse(request.getParameter("expireDate")));
                requestParams.put("prevDate", prevDate);
            }
            if (!isnew) {
                KwlReturnObject rst = accountingHandlerDAOobj.getObject(RepeatedInvoices.class.getName(), repeateId);
                RepeatedInvoices Repeatedinv = (RepeatedInvoices) rst.getEntityList().get(0);
                setValuesForAuditTrialMessageForRecurring(Repeatedinv, request, oldRecurringInvoicePrmt, RecurringinvoicePrmt, newAuditKey);
            }

            KwlReturnObject rObj = accInvoiceDAOobj.saveRepeateInvoiceInfo(dataMap);
            RepeatedInvoices rinvoice = (RepeatedInvoices) rObj.getEntityList().get(0);

            JSONObject invjson = new JSONObject();
            String InvoiceID = request.getParameter("invoiceid");
            invjson.put("invoiceid", InvoiceID);
            invjson.put("repeateid", rinvoice.getId());

            boolean isCustomer = Boolean.parseBoolean(request.getParameter("isCustomer"));
            int PendingApprove = 0;
            int ApproveStatusLevel = 11;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            if (isCustomer) {
                KwlReturnObject Invresult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), InvoiceID);
                Invoice invoice = (Invoice) Invresult.getEntityList().get(0);
                PendingApprove = invoice.getPendingapproval();
                ApproveStatusLevel = invoice.getApprovestatuslevel();
            } else {
//                KwlReturnObject Invresult = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), InvoiceID);
//            GoodsReceipt invoice = (GoodsReceipt) Invresult.getEntityList().get(0);
                PendingApprove = accInvoiceDAOobj.getPendingapprovalForVendorInvoice(companyid, InvoiceID);
            }
            invjson.put("pendingapproval", PendingApprove);
            invjson.put("approvalstatuslevel", ApproveStatusLevel);
            if (isCustomer) {
                accInvoiceDAOobj.updateInvoice(invjson, null);
            } else {
                accInvoiceDAOobj.updateGoodsReceipt(invjson, null);
            }

            if (!StringUtil.isNullOrEmpty(request.getParameter("detail"))) {
                repeateid = rinvoice.getId();
                int delcount = accJournalEntryobj.DelRepeateJEMemo(repeateid, "RepeatedInvoiceID");
                detail = request.getParameter("detail");
                JSONArray arrMemo = new JSONArray(detail);
                for (int i = 0; i < arrMemo.length(); i++) {
                    JSONObject jsonmemo = arrMemo.getJSONObject(i);
                    HashMap<String, Object> dataMapformemo = new HashMap<String, Object>();
                    dataMapformemo.put("no", Integer.parseInt(jsonmemo.get("no").toString()));
                    dataMapformemo.put("memo", jsonmemo.get("memo"));
                    dataMapformemo.put("RepeatedInvoiceID", rinvoice.getId());
                    KwlReturnObject savememo = accJournalEntryobj.saveRepeateJEMemo(dataMapformemo);
                }
            }
            String billno = request.getParameter("billno");
            requestParams.put("billno", billno);
            msg = messageSource.getMessage("acc.inv.recSave", null, RequestContextUtils.getLocale(request));   //"Recurring Invoice has been saved successfully";

            if (!isnew) {
                if (rinvoice.getNextDate() != null) {
                    dataMap.put("NextGenerationDate", df.format(rinvoice.getNextDate()));
                } else {
                    dataMap.put("NextGenerationDate", "");
                }
                if (rinvoice.getExpireDate() != null) {
                    dataMap.put("expireDate", df.format(rinvoice.getExpireDate()));
                } else {
                    dataMap.put("expireDate", "");
                }

            }
            String action = "Created";
            String auditMessage = "";
            if (!isnew) {
                action = "Updated";
                auditMessage = AccountingManager.BuildAuditTrialMessage(dataMap, oldRecurringInvoicePrmt, moduleid, newAuditKey);
            }

            auditTrailObj.insertAuditLog(AuditAction.INVOICE_CREATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + " Recurring Invoice " + billno + auditMessage, request, tranID);

            issuccess = true;
            ExtraCompanyPreferences extraCompanyPreferences = null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
            DocumentEmailSettings documentEmailSettings = null;
            KwlReturnObject documentEmailresult = accountingHandlerDAOobj.getObject(DocumentEmailSettings.class.getName(), sessionHandlerImpl.getCompanyid(request));
            documentEmailSettings = documentEmailresult != null ? (DocumentEmailSettings) documentEmailresult.getEntityList().get(0) : null;
            if (documentEmailSettings != null) {
                if (documentEmailSettings.isRecurringInvoiceMail()) {
                    SendMail(requestParams);    //Notification Mail
                }
            }
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getInvoiceRepeateDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "", parentInvoiceId = "";
        try {
            if (!StringUtil.isNullOrEmpty(request.getParameter("parentid"))) {
                parentInvoiceId = request.getParameter("parentid");
            } else if (!StringUtil.isNullOrEmpty(request.getParameter("bills"))) {
                parentInvoiceId = request.getParameter("bills");
            }
            JSONArray JArr = new JSONArray();
            String[] invoices = null;
            KwlReturnObject details = null;
            int i = 0;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            invoices = (invoices == null) ? parentInvoiceId.split(",") : invoices;
            while (invoices != null && i < invoices.length) {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("parentInvoiceId", invoices[i]);
                requestParams.put("companyid", companyid);
                details = accInvoiceDAOobj.getRepeateInvoicesDetailsForExpander(requestParams);
                List detailsList = details.getEntityList();
                if (detailsList.size() > 0) {
                    Iterator itr = detailsList.iterator();
                    while (itr.hasNext()) {
                        Object[] repeatedInvoice = (Object[]) itr.next();
                        JSONObject obj = new JSONObject();
                        obj.put("invoiceId", repeatedInvoice[0].toString());
                        obj.put("invoiceNo", repeatedInvoice[1].toString());
                        obj.put("parentInvoiceId", invoices[i]);
                        obj.put("isExpander", true);
                        JArr.put(obj);
                    }
                    i++;
                } else {
                    JSONObject obj = new JSONObject();
                    obj.put("parentInvoiceId", invoices[i]);
                    obj.put("isExpander", false);
                    JArr.put(obj);
                    i++;
                }
            }
            jobj.put("data", JArr);
            jobj.put("count", invoices.length);
            issuccess = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView deleteDeliveryOrders(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();

        try {
            JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
            requestJobj.put("servletContext", this.getServletContext());
            jobj = accInvoiceModuleService.deleteTemporaryDeliveryOrders(requestJobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView closeDeliveryOrders(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        boolean isConsignment = false;
        String companyid = sessionHandlerImpl.getCompanyid(request);
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        if (!StringUtil.isNullOrEmpty(request.getParameter("isConsignment"))) {
            isConsignment = Boolean.parseBoolean(request.getParameter("isConsignment"));
        }
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String data = request.getParameter("jsondata");
            JSONArray jArr = new JSONArray(data);
            List doNoList = new ArrayList<String>();
            for (int i = 0; i < jArr.length(); i++) {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                JSONObject jObj = jArr.optJSONObject(i);
                String billId = jObj.getString("billid");
                String billNo = jObj.optString("billno", "");
                requestParams.put("billid", billId);
                requestParams.put("companyid", companyid);
                accInvoiceDAOobj.closeDeliveryOrdersPermanent(requestParams, companyid);
                if (!StringUtil.isNullOrEmpty(billNo)) {
                    doNoList.add(billNo);
                }
            }
            String auditMessage = "User " + sessionHandlerImpl.getUserFullName(request) + " has closed Delivery Order(s) : " + StringUtil.join(",", doNoList);
            auditTrailObj.insertAuditLog(AuditAction.DELIVERY_ORDER, auditMessage, request, "");

            txnManager.commit(status);
            msg = "Selected Delivery Order is Closed Successfully";
            issuccess = true;

        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView saveDOStatus(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("BR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            saveDOStatus(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.field.Statusupdatedsuccessfully", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void saveDOStatus(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        String doId = request.getParameter("dId");
        String status = request.getParameter("status");
        accInvoiceDAOobj.saveDeliveryOrderStatus(doId, status);

    }

    public ModelAndView deleteDeliveryOrdersPermanent(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            paramJobj.put("servletContext", this.getServletContext());
            jobj = accInvoiceModuleService.deleteDeliveryOrdersJSON(paramJobj);
        } catch (JSONException ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public AssetMaintenanceWorkOrder saveAssetMaintenanceWorkOrder(HttpServletRequest request) throws SessionExpiredException, AccountingException, ParseException, UnsupportedEncodingException, ServiceException {
        AssetMaintenanceWorkOrder assetMaintenanceWorkOrder = null;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyid = (request.getParameter("currencyid") == null ? sessionHandlerImpl.getCurrencyID(request) : request.getParameter("currencyid"));

            String entryNumber = request.getParameter("number");

            String assignedTo = request.getParameter("assignedTo");

            String schedulerId = request.getParameter("schedulerId");

            String workOrderId = request.getParameter("workOrderId");
            String AssetName = request.getParameter("assetName");
            Date billdate = null;

            Date startDate = authHandler.getDateOnlyFormat(request).parse(request.getParameter("startDate"));
            Date endDate = authHandler.getDateOnlyFormat(request).parse(request.getParameter("endDate"));

            if (!StringUtil.isNullOrEmpty(request.getParameter("billdate"))) {
                billdate = authHandler.getDateOnlyFormat(request).parse(request.getParameter("billdate"));
            }

            List woDetailsReturnList = null;

            String createdby = sessionHandlerImpl.getUserid(request);
            String modifiedby = sessionHandlerImpl.getUserid(request);
            long createdon = System.currentTimeMillis();
            long updatedon = System.currentTimeMillis();

            HashMap<String, Object> doDataMap = new HashMap<String, Object>();

            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);

            // create New
            if (StringUtil.isNullOrEmpty(workOrderId)) {

                KwlReturnObject wocnt = accInvoiceDAOobj.getWorkOrderCount(entryNumber, companyid);

                if (wocnt.getRecordTotalCount() > 0) {
                    throw new AccountingException("Work Order number '" + entryNumber + "' already exists.");
                }

                doDataMap.put("createdon", createdon);

            } else {// Edit Case
                // Delete Work Order Details
                KwlReturnObject result = accInvoiceDAOobj.getWorkOrderInventory(workOrderId);
                KwlReturnObject resultBatch = accInvoiceDAOobj.getWorkOrderBatches(workOrderId, companyid);

                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("woid", workOrderId);
                requestParams.put("companyid", companyid);

                accInvoiceDAOobj.deleteWorkOrdersBatchSerialDetails(requestParams); //dlete serial no and mapping
                accInvoiceDAOobj.deleteWorkOrderDetails(workOrderId, companyid);

                List list = result.getEntityList();
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    String inventoryid = (String) itr.next();
                    accProductObj.deleteInventory(inventoryid, companyid);
                }

                List listBatch = resultBatch.getEntityList();
                Iterator itrBatch = listBatch.iterator();
                while (itrBatch.hasNext()) {
                    String batchid = (String) itrBatch.next();
                    accCommonTablesDAO.deleteBatches(batchid, companyid);
                }

                doDataMap.put("id", workOrderId);

            }
            doDataMap.put("entrynumber", entryNumber);
            doDataMap.put("workOrderDate", billdate);
            doDataMap.put("startDate", startDate);
            doDataMap.put("endDate", endDate);
            doDataMap.put("schedulerId", schedulerId);
            doDataMap.put("remark", request.getParameter("remark"));
            doDataMap.put("assignedTo", assignedTo);

            doDataMap.put("createdby", createdby);
            doDataMap.put("modifiedby", modifiedby);
            doDataMap.put("updatedon", updatedon);

            doDataMap.put("companyid", companyid);
            doDataMap.put("currencyid", currencyid);

            KwlReturnObject doresult = accInvoiceDAOobj.saveWorkOrder(doDataMap);

            assetMaintenanceWorkOrder = (AssetMaintenanceWorkOrder) doresult.getEntityList().get(0);

            doDataMap.put("id", assetMaintenanceWorkOrder.getId());

            woDetailsReturnList = saveWorkOrderRows(request, assetMaintenanceWorkOrder, companyid);

            HashSet wodetails = ((HashSet) woDetailsReturnList.get(0));

            assetMaintenanceWorkOrder.setMaintenanceWorkOrderDetails(wodetails);

            // Updating Asset Maintenance Scheduler For actual start date, actual end date, and Work job id
            HashMap<String, Object> dataMap = new HashMap<String, Object>();

            dataMap.put("id", schedulerId);
            dataMap.put("actualStartDate", startDate);
            dataMap.put("actualEndDate", endDate);
//            dataMap.put("workOrderId", assetMaintenanceWorkOrder.getId());
            dataMap.put("assignedTo", assignedTo);

            KwlReturnObject result = accProductObj.updateMaintenanceSchedule(dataMap);

            KwlReturnObject scObj = accountingHandlerDAOobj.getObject(AssetMaintenanceScheduler.class.getName(), schedulerId);
            AssetMaintenanceScheduler scheduler = (AssetMaintenanceScheduler) scObj.getEntityList().get(0);

            String ScheduleName = "";

            if (scheduler != null && scheduler.getAssetMaintenanceSchedulerObject() != null) {
                ScheduleName = scheduler.getAssetMaintenanceSchedulerObject().getScheduleName();
            }

            String workordenumber = assetMaintenanceWorkOrder.getWorkOrderNumber();
            String action = " Added ";
            String auditaction = AuditAction.ASSET_MAINTENANCE_WORK_ORDER_ADDED;
            if (!StringUtil.isNullOrEmpty(workOrderId)) {
                action = " Updated ";
                auditaction = AuditAction.ASSET_MAINTENANCE_WORK_ORDER_UPDATED;

            }
            auditTrailObj.insertAuditLog(auditaction, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + " Work Order " + workordenumber + " of schedule " + ScheduleName + " for asset " + AssetName, request, companyid);

        } catch (ServiceException ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveAssetMaintenanceWorkOrder : " + ex.getMessage(), ex);
        }

        return assetMaintenanceWorkOrder;
    }

    public List saveWorkOrderRows(HttpServletRequest request, AssetMaintenanceWorkOrder workOrder, String companyid) throws SessionExpiredException, ParseException, UnsupportedEncodingException, ServiceException, AccountingException {
        List returnList = new ArrayList();
        HashSet rows = new HashSet();

        try {
            JSONArray jArr = new JSONArray(request.getParameter("detail"));

            List<StockMovement> stockMovementsList = new ArrayList<StockMovement>();

            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);

                HashMap<String, Object> dodDataMap = new HashMap<String, Object>();

                if (jobj.has("srno") && !StringUtil.isNullOrEmpty(jobj.getString("srno"))) {
                    dodDataMap.put("srno", jobj.getInt("srno"));
                }

                dodDataMap.put("companyid", companyid);
                dodDataMap.put("woid", workOrder.getId());
                dodDataMap.put("productid", jobj.getString("productid"));

                String description = "";
                double baseuomrate = 1;
                double quantity = jobj.getDouble("quantity");
                double dquantity = 0;

                if (jobj.has("baseuomrate") && jobj.get("baseuomrate") != null) {
                    baseuomrate = jobj.getDouble("baseuomrate");
                }

                if (jobj.has("uomid")) {
                    dodDataMap.put("uomid", jobj.getString("uomid"));
                }

                dquantity = jobj.getDouble("dquantity");
                description = jobj.getString("description");
                dodDataMap.put("description", description);
                dodDataMap.put("deliveredquantity", dquantity);
                dodDataMap.put("baseuomdeliveredquantity", authHandler.calculateBaseUOMQuatity(dquantity, baseuomrate, companyid));

                dodDataMap.put("baseuomrate", baseuomrate);
                dodDataMap.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(quantity, baseuomrate, companyid));
                dodDataMap.put("quantity", quantity);
                dodDataMap.put("remark", jobj.optString("remark"));
//                dodDataMap.put("reason", jobj.optString("reason"));

                KwlReturnObject proresult = accountingHandlerDAOobj.getObject(Product.class.getName(), jobj.getString("productid"));
                Product product = (Product) proresult.getEntityList().get(0);

                JSONObject inventoryjson = new JSONObject();
                inventoryjson.put("productid", jobj.getString("productid"));
                inventoryjson.put("quantity", dquantity);
                inventoryjson.put("description", description);
                if (jobj.has("uomid")) {
                    inventoryjson.put("uomid", jobj.getString("uomid"));
                }

                inventoryjson.put("baseuomrate", baseuomrate);
                inventoryjson.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(dquantity, baseuomrate, companyid));
                inventoryjson.put("carryin", false);
                inventoryjson.put("defective", false);
                inventoryjson.put("newinventory", false);
                inventoryjson.put("companyid", companyid);
                inventoryjson.put("updatedate", authHandler.getDateOnlyFormat(request).parse(request.getParameter("billdate")));

                KwlReturnObject invresult = accProductObj.addInventory(inventoryjson);
                Inventory inventory = (Inventory) invresult.getEntityList().get(0);

                dodDataMap.put("Inventory", inventory);
                if (jobj.has("rate")) {
                    dodDataMap.put("rate", jobj.getString("rate"));
                }

                KwlReturnObject result = accInvoiceDAOobj.saveWorkOrderDetails(dodDataMap);
                AssetMaintenanceWorkOrderDetail row = (AssetMaintenanceWorkOrderDetail) result.getEntityList().get(0);
                if (jobj.has("batchdetails") && jobj.getString("batchdetails") != null) {
                    String batchDetails = jobj.getString("batchdetails");
                    if (!StringUtil.isNullOrEmpty(batchDetails)) {
                        saveWorkOrderNewBatch(batchDetails, inventory, request, row, stockMovementsList, false);
                    }
                }

                rows.add(row);
            }

        } catch (ServiceException ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveWorkOrderRows : " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveWorkOrderRows : " + ex.getMessage(), ex);
        }

        returnList.add(rows);
        return returnList;
    }

    public ModelAndView saveAssetMaintenanceWorkOrder(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        String billno = "";
        String billid = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            AssetMaintenanceWorkOrder workOrder = saveAssetMaintenanceWorkOrder(request);

            billid = workOrder.getId();
            billno = workOrder.getWorkOrderNumber();

            issuccess = true;
            msg = messageSource.getMessage("acc.Workorder.WorkOrder", null, RequestContextUtils.getLocale(request)) + " " + billno + " " + messageSource.getMessage("acc.field.hasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request));//messageSource.getMessage("acc.wo.save", null, RequestContextUtils.getLocale(request)) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + " <b>" + billno + "</b>";   //;                
            txnManager.commit(status);

        } catch (Exception ex) {
            issuccess = false;
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("billid", billid);
                jobj.put("billno", billno);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView saveDeliveryOrder(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException {
        JSONObject jobj = new JSONObject();
        String channelName = "";
        boolean issuccess = false;
        try {
            /*Get request parameters */
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            /*Call to Save Delivery Order Details*/
            String baseUrl = URLUtil.getPageURL(request, loginpageFull);
            paramJobj.put("baseUrl", baseUrl);
            String userName = sessionHandlerImpl.getUserFullName(request);
            paramJobj.put(Constants.username, userName);
            jobj = accInvoiceModuleService.saveDeliveryOrderJSON(paramJobj);
            issuccess = jobj.optBoolean(Constants.RES_success, false);
            jobj.put(Constants.userSessionId, (request.getSession(true)).getAttribute(Constants.userSessionId));// adding user session id to idenntify unique user session
            channelName = jobj.optString(Constants.channelName, null);
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            if (issuccess && !StringUtil.isNullOrEmpty(channelName)) {
                ServerEventManager.publish(channelName, jobj.toString(), (ServletContext) this.getServletContext());
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /**
     * This Method is used to Import Delivery Orders
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView importDeliveryOrders(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            /* Get Import related global parameters */
            JSONObject paramJobj = getDeliveryOrdersParams(request);
            /* Call validate and import data of VQ. */
            jobj = accInvoiceModuleService.importDeliveryOrdersJSON(paramJobj);
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /*
     * function to close Line level product used in Delivery Order manually, if it is no longer required
     */
    public ModelAndView closeDoDetail(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String message = "";
        try {
            String doDetailId = (String) request.getParameter("DetailId");
            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(DeliveryOrderDetail.class.getName(), doDetailId);
            DeliveryOrderDetail deliveryOrderDetail = (DeliveryOrderDetail) rdresult.getEntityList().get(0);
            DeliveryOrder deliveryOrder = deliveryOrderDetail.getDeliveryOrder();

            if (!deliveryOrder.isIsDOClosed() && !deliveryOrderDetail.isIsLineItemClosed()) {
                /* DO is allowing to close if it not Closed already or fully linked in DO*/
                accInvoiceDAOobj.closeDeliveryDetailsOrdersPermanent(doDetailId, deliveryOrder.getCompany().getCompanyID());
                deliveryOrderDetail.setIsLineItemClosed(true);
                jobj.put(Constants.RES_success, true);
                jobj.put(Constants.RES_msg, "Selected Consignment DO record has been manually closed.");
                return new ModelAndView("jsonView", "model", jobj.toString());
            } else {
                /* If DO record is already closed */
                jobj.put(Constants.RES_success, false);
                jobj.put(Constants.RES_msg, "Selected Consignment DO record is already closed.");
                return new ModelAndView("jsonView", "model", jobj.toString());

            }
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, message);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /**
     * Description : This Method is used to Get Request params for import
     * Delivery Order
     *
     * @param request
     * @return JSONObject
     * @throws JSONException
     * @throws SessionExpiredException
     */
    public JSONObject getDeliveryOrdersParams(HttpServletRequest request) throws JSONException, SessionExpiredException {
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        paramJobj.put("servletContext", this.getServletContext());
        return paramJobj;
    }

    /**
     * Description : This Method is used to create the common Delivery Order Map
     *
     * @param <request> used to get request parameters
     * @return :HashMap
     */
    public HashMap<String, Object> createDeliveryOrderMap(HttpServletRequest request) throws ServiceException {

        HashMap<String, Object> doMap = new HashMap<String, Object>();
        String entryNumber = "", companyid = "";
        try {
            companyid = sessionHandlerImpl.getCompanyid(request);
            String grid = request.getParameter("doid");
            DateFormat df = authHandler.getDateOnlyFormat(request);
            boolean isConsignment = request.getParameter("isConsignment") != null ? Boolean.parseBoolean(request.getParameter("isConsignment")) : false;
            boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false;
            boolean isLeaseFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false;
            boolean isEdit = StringUtil.isNullOrEmpty(request.getParameter("isEdit")) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
            boolean isCopy = StringUtil.isNullOrEmpty(request.getParameter("copyInv")) ? false : Boolean.parseBoolean(request.getParameter("copyInv"));
            entryNumber = request.getParameter("numberDo") != null ? request.getParameter("numberDo") : request.getParameter("number");
            String modifiedby = sessionHandlerImpl.getUserid(request);
            String costCenterId = request.getParameter("costcenter");
            String status = request.getParameter("statuscombo");
            long updatedon = System.currentTimeMillis();
            doMap.put("isConsignment", isConsignment);
            doMap.put("isFixedAsset", isFixedAsset);
            doMap.put("isLeaseFixedAsset", isLeaseFixedAsset);
            doMap.put("isEdit", isEdit);
            doMap.put("isCopy", isCopy);
            doMap.put("grid", grid);
            doMap.put("companyid", companyid);
            doMap.put("entryNumber", entryNumber);
            doMap.put("id", request.getParameter("doid"));  //DO ID        
            doMap.put("modifiedby", modifiedby);
            doMap.put("updatedon", updatedon);
            doMap.put("orderdate", df.parse(request.getParameter("billdate")));
            doMap.put("memo", request.getParameter("memo"));
            doMap.put("shipvia", request.getParameter("shipvia"));
            doMap.put("fob", request.getParameter("fob"));
            doMap.put("customerporefno", request.getParameter("customerporefno"));
            doMap.put("status", status);
            doMap.put("salesPerson", request.getParameter("salesPerson"));

            if (!StringUtil.isNullOrEmpty(request.getParameter("permitNumber"))) {
                doMap.put("permitNumber", request.getParameter("permitNumber"));
            }
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                doMap.put("costCenterId", costCenterId);
            }
            if (request.getParameter("shipdate") != null && !StringUtil.isNullOrEmpty(request.getParameter("shipdate"))) {
                doMap.put("shipdate", df.parse(request.getParameter("shipdate")));
            }
            doMap.put("customfield", request.getParameter("customfield"));
            doMap.put("detail", request.getParameter("detail"));

        } catch (Exception ex) {
            throw ServiceException.FAILURE("createDeliveryOrderMap : " + ex.getMessage(), ex);
        }
        return doMap;

    }

    /**
     * Description : This Method is used to update the delivery order data
     *
     * @param <doMap> used to get delivery order data
     * @return :List
     */
    public List updateDeliveryOrder(HashMap<String, Object> doMap) throws SessionExpiredException, ServiceException, AccountingException, JSONException, UnsupportedEncodingException {
        List list = new ArrayList();
        DeliveryOrder deliveryOrder = null;
        boolean isFixedAsset = false;
        boolean isConsignment = false;
        boolean isLeaseFixedAsset = false;
        String companyid = "", customfield = "";
        try {
            if (doMap.containsKey("companyid") && doMap.get("companyid") != null) {
                companyid = (String) doMap.get("companyid");
            }
            if (doMap.containsKey("isFixedAsset") && doMap.get("isFixedAsset") != null) {
                isFixedAsset = (Boolean) doMap.get("isFixedAsset");
            }
            if (doMap.containsKey("isLeaseFixedAsset") && doMap.get("isLeaseFixedAsset") != null) {
                isLeaseFixedAsset = (Boolean) doMap.get("isLeaseFixedAsset");
            }
            if (doMap.containsKey("isConsignment") && doMap.get("isConsignment") != null) {
                isConsignment = (Boolean) doMap.get("isConsignment");
            }
            if (doMap.containsKey("customfield") && doMap.get("customfield") != null) {
                customfield = (String) doMap.get("customfield");
            }

            KwlReturnObject doresult = accInvoiceDAOobj.saveDeliveryOrder(doMap);
            deliveryOrder = (DeliveryOrder) doresult.getEntityList().get(0);
            if (doMap.containsKey("memo")) {
                stockMovementService.updateDOMemo(deliveryOrder.getID(), "" + doMap.get("memo"));
            }
            doMap.put("id", deliveryOrder.getID());

            HashSet<DeliveryOrderDetail> groDetails = updateDeliveryOrderRows(doMap);
            list.add(deliveryOrder);

            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);

                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_DeliveryOrder_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_DeliveryOrderid);
                customrequestParams.put("modulerecid", deliveryOrder.getID());
                customrequestParams.put("moduleid", isFixedAsset ? Constants.Acc_FixedAssets_DeliveryOrder_ModuleId : isConsignment ? Constants.Acc_ConsignmentDeliveryOrder_ModuleId : isLeaseFixedAsset ? Constants.Acc_Lease_DO : Constants.Acc_Delivery_Order_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_DeliveryOrder_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    doMap.put("accadeliveryordercustomdataref", deliveryOrder.getID());
                    KwlReturnObject accresult = accInvoiceDAOobj.updateDeliveryOrderCustomData(doMap);
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("updateDeliveryOrderRows : " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("updateDeliveryOrderRows : " + ex.getMessage(), ex);
        }

        return list;
    }

    /**
     * Description : This Method is used to update the delivery order line level
     * data
     *
     * @param <doMap> used to get delivery order data
     * @return :HashSet
     */
    private HashSet updateDeliveryOrderRows(HashMap<String, Object> doMap) throws ServiceException, JSONException {
        HashSet<DeliveryOrderDetail> rows = new HashSet<>();
        String detail = "";
        boolean isFixedAsset = false;
        boolean isConsignment = false;
        boolean isLeaseFixedAsset = false;
        String companyid = "", customfield = "", id = "";
        try {

            if (doMap.containsKey("companyid") && doMap.get("companyid") != null) {
                companyid = (String) doMap.get("companyid");
            }
            if (doMap.containsKey("isFixedAsset") && doMap.get("isFixedAsset") != null) {
                isFixedAsset = (Boolean) doMap.get("isFixedAsset");
            }
            if (doMap.containsKey("isConsignment") && doMap.get("isConsignment") != null) {
                isConsignment = (Boolean) doMap.get("isConsignment");
            }
            if (doMap.containsKey("isLeaseFixedAsset") && doMap.get("isLeaseFixedAsset") != null) {
                isLeaseFixedAsset = (Boolean) doMap.get("isLeaseFixedAsset");
            }
            if (doMap.containsKey("customfield") && doMap.get("customfield") != null) {
                customfield = (String) doMap.get("customfield");
            }
            if (doMap.containsKey("detail") && doMap.get("detail") != null) {
                detail = (String) doMap.get("detail");
            }
            if (doMap.containsKey("id") && doMap.get("id") != null) {
                id = (String) doMap.get("id");
            }

            JSONArray jArr = new JSONArray(detail);

            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);

                String linkto = jobj.getString("linkto");
                DeliveryOrderDetail row = null;
                if (jobj.has("rowid")) {
                    KwlReturnObject invDetailsResult = accountingHandlerDAOobj.getObject(DeliveryOrderDetail.class.getName(), StringUtil.isNullOrEmpty(linkto) ? jobj.getString("rowid") : jobj.getString("docrowid"));
                    row = (DeliveryOrderDetail) invDetailsResult.getEntityList().get(0);
                }

                if (row != null) {

                    if (jobj.has("srno")) {
                        row.setSrno(jobj.getInt("srno"));
                    }

                    if (!StringUtil.isNullOrEmpty(jobj.optString("description"))) {
                        try {
                            row.setDescription(StringUtil.DecodeText(jobj.optString("description")));
                        } catch (Exception ex) {
                            row.setDescription(jobj.optString("description"));
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(jobj.optString("remark"))) { // Updating Remark feild after Link in Document's
                        try {
                            row.setRemark(StringUtil.DecodeText(jobj.optString("remark")));
                        } catch (Exception ex) {
                            row.setRemark(jobj.optString("remark"));
                        }
                    }
                    customfield = jobj.getString("customfield");
                    if (!StringUtil.isNullOrEmpty(customfield)) {
                        HashMap<String, Object> DOMap = new HashMap<String, Object>();
                        JSONArray jcustomarray = new JSONArray(customfield);
                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", "DeliveryOrderDetail");
                        customrequestParams.put("moduleprimarykey", "DeliveryOrderDetailId");
                        customrequestParams.put("modulerecid", row.getID());
                        customrequestParams.put("moduleid", isFixedAsset ? Constants.Acc_FixedAssets_DeliveryOrder_ModuleId : isConsignment ? Constants.Acc_ConsignmentDeliveryOrder_ModuleId : isLeaseFixedAsset ? Constants.Acc_Lease_DO : Constants.Acc_Delivery_Order_ModuleId);
                        customrequestParams.put("companyid", companyid);
                        DOMap.put("id", row.getID());
                        customrequestParams.put("customdataclasspath", Constants.Acc_DeliveryOrderDetail_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            DOMap.put("dodetailscustomdataref", row.getID());
                            accInvoiceDAOobj.updateDODetailsCustomData(DOMap);
                        }
                    }
                    // Add Custom fields details for Product
                    if (!StringUtil.isNullOrEmpty(jobj.optString("productcustomfield", ""))) {
                        JSONArray jcustomarray = new JSONArray(jobj.optString("productcustomfield", "[]"));
                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", "DeliveryorderDetail");
                        customrequestParams.put("moduleprimarykey", "DoDetailID");
                        customrequestParams.put("modulerecid", row.getID());
                        customrequestParams.put("moduleid", Constants.Acc_Delivery_Order_ModuleId);
                        customrequestParams.put("recdetailId", row.getID());
                        customrequestParams.put("productId", row.getProduct().getID());
                        customrequestParams.put("companyid", companyid);
                        customrequestParams.put("customdataclasspath", Constants.Acc_DODETAIL_Productcustom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    }
                    rows.add(row);
                }
            }

        } catch (JSONException ex) {
            throw ServiceException.FAILURE("updateDeliveryOrderRows : " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("updateDeliveryOrderRows : " + ex.getMessage(), ex);
        }
        return rows;
    }

    /**
     * Description : This Method is used to update Delivery Order
     *
     * @param <request> used to get request parameters
     * @param <response> used to send response
     * @return :JSONObject
     */
    public ModelAndView updateDeliveryOrder(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        String billno = "";
        String billid = "";
        String channelName = "", moduleName = "";
        boolean issuccess = false;
        boolean accexception = false;
        boolean isEdit = false;
        boolean isCopy = false;
        boolean isConsignment = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;//txnManager.getTransaction(def);
        try {
            HashMap<String, Object> grOrderMap = createDeliveryOrderMap(request);

            if (grOrderMap.containsKey("isEdit") && grOrderMap.get("isEdit") != null) {
                isEdit = (Boolean) grOrderMap.get("isEdit");
            }
            if (grOrderMap.containsKey("isCopy") && grOrderMap.get("isCopy") != null) {
                isCopy = (Boolean) grOrderMap.get("isCopy");
            }

            if (grOrderMap.containsKey("isConsignment") && grOrderMap.get("isConsignment") != null) {
                isConsignment = (Boolean) grOrderMap.get("isConsignment");
            }

            status = txnManager.getTransaction(def);
            List li = updateDeliveryOrder(grOrderMap);
            DeliveryOrder deliveryOrder = (DeliveryOrder) li.get(0);
            billid = deliveryOrder.getID();
            billno = deliveryOrder.getDeliveryOrderNumber();
            issuccess = true;

            if (isConsignment) {
                msg = messageSource.getMessage("acc.consignment.DO.save", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.do.save", null, RequestContextUtils.getLocale(request));
            }

            String auditSMS = "";
            String action = "added new";
            if (isEdit == true && isCopy == false) {
                action = "updated";
            }
            if (isConsignment) {
                moduleName = " Consignment Delivery Order ";
                msg += messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + " <b>" + billno + "</b>";   //" consignment Delivery order has been saved successfully";
                auditSMS = " has " + action + " " + moduleName + " " + billno;
            } else {
                moduleName = Constants.Delivery_Order;
                msg += messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + " <b>" + billno + "</b>";   //"Delivery order has been saved successfully";
                auditSMS = " has " + action + " " + moduleName + " " + billno;
            }

            auditTrailObj.insertAuditLog(AuditAction.GOODS_RECEIPT_CREATED, "User " + sessionHandlerImpl.getUserFullName(request) + auditSMS, request, deliveryOrder.getID());

            if (deliveryOrder.isFixedAssetDO() && !(deliveryOrder.isIsconsignment())) {
                channelName = "/FixedAssetDeliveryList/gridAutoRefresh";
            } else if (deliveryOrder.isLeaseDO() && !(deliveryOrder.isIsconsignment())) {
                channelName = "/LeaseDeliveryOrderReport/gridAutoRefresh";
            } else if (!(deliveryOrder.isFixedAssetDO() || deliveryOrder.isLeaseDO() || deliveryOrder.isIsconsignment())) {
                channelName = "/DeliveryOrderReport/gridAutoRefresh";
            }
            txnManager.commit(status);
        } catch (ServiceException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AccountingException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("billid", billid);
                jobj.put("billno", billno);
                jobj.put("accException", accexception);
                jobj.put("pendingApproval", false);
                if (issuccess && !StringUtil.isNullOrEmpty(channelName)) {
                    jobj.put(Constants.userSessionId, (request.getSession(true)).getAttribute(Constants.userSessionId));// adding user session id to idenntify unique user session
                    ServerEventManager.publish(channelName, jobj.toString(), (ServletContext) this.getServletContext());
                }
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView approveDeliveryOrder(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Invoice_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        StringBuffer productIds = new StringBuffer();
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            String remark = request.getParameter("remark");
            String doID = request.getParameter("billid");
            String currentUser = sessionHandlerImpl.getUserid(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject DOObj = accountingHandlerDAOobj.getObject(DeliveryOrder.class.getName(), doID);
            DeliveryOrder doObj = (DeliveryOrder) DOObj.getEntityList().get(0);
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);

            String postingDateStr = request.getParameter("postingDate");
            DateFormat df = authHandler.getDateOnlyFormat();
            Date postingDate = null;
            if (!StringUtil.isNullOrEmpty(postingDateStr)) {
                postingDate = df.parse(postingDateStr);
            }
            KwlReturnObject ecpObj = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences ecp = (ExtraCompanyPreferences) ecpObj.getEntityList().get(0);
            boolean isInventoryActivated = ecp != null ? ecp.isActivateInventoryTab() : false;

            Set<DeliveryOrderDetail> doRows = doObj.getRows();
            String currencyid = doObj.getCurrency() != null ? doObj.getCurrency().getCurrencyID() : sessionHandlerImpl.getCurrencyID(request);
            HashMap<String, Object> GlobalParams = AccountingManager.getGlobalParams(request);
            double subtotal = 0;
            double quantity = 0;
            JSONArray productDiscountJArr = new JSONArray();
            if (doRows != null && !doRows.isEmpty()) {
                for (DeliveryOrderDetail cnt : doRows) {
                    String productId = cnt.getInventory().getProduct().getID();
                    quantity = cnt.getInventory().getQuantity();
                    subtotal = authHandler.round(cnt.getRate() * quantity, companyid);
                    double rowDiscVal = 0;
                    if (cnt.getDiscountispercent() == 1) {
                        rowDiscVal = authHandler.round((subtotal * cnt.getDiscount() / 100), companyid);
                    } else {
                        rowDiscVal = authHandler.round(cnt.getDiscount(), companyid);
                    }
                    // Mapping Product and Discount
                    KwlReturnObject dAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, rowDiscVal, currencyid, doObj.getOrderDate(), doObj.getExternalCurrencyRate());
                    double discAmountinBase = (Double) dAmount.getEntityList().get(0);
                    discAmountinBase = authHandler.round(discAmountinBase, companyid);
                    JSONObject productDiscountObj = new JSONObject();
                    productDiscountObj.put("productId", productId);
                    productDiscountObj.put("discountAmount", discAmountinBase);
                    productDiscountJArr.put(productDiscountObj);
                    if (!StringUtil.isNullOrEmpty(productId) && productIds.indexOf(productId) == -1) {
                        productIds.append(productId).append(",");
                    }
                }
            }

            double amount = StringUtil.isNullOrEmpty(request.getParameter("totalorderamount")) ? 0 : authHandler.round(Double.parseDouble(request.getParameter("totalorderamount")), companyid);

            HashMap<String, Object> doApproveMap = new HashMap<String, Object>();
            int level = doObj.getApprovestatuslevel();
            doApproveMap.put("companyid", companyid);
            doApproveMap.put("level", level);
            doApproveMap.put("totalAmount", String.valueOf(amount));
            doApproveMap.put("currentUser", currentUser);
            doApproveMap.put("fromCreate", false);
            doApproveMap.put("productDiscountMapList", productDiscountJArr);
            doApproveMap.put("moduleid", Constants.Acc_Delivery_Order_ModuleId);
            String exPrefObject = ecp.getColumnPref();
            JSONObject jObj = StringUtil.isNullObject(exPrefObject) ? new JSONObject() : new JSONObject(exPrefObject.toString());
            if (!StringUtil.isNullObject(jObj) && jObj.has(Constants.IS_POSTING_DATE_CHECK) && jObj.get(Constants.IS_POSTING_DATE_CHECK) != null && jObj.optBoolean(Constants.IS_POSTING_DATE_CHECK, false)) {
                doApproveMap.put("postingDate", postingDate);
            }
            doApproveMap.put(Constants.PAGE_URL, paramJobj.optString(Constants.PAGE_URL));

            List approvedLevelList = approveDO(doObj, doApproveMap, true);
            int approvedLevel = (Integer) approvedLevelList.get(0);
            String baseUrl = URLUtil.getPageURL(request, loginpageFull);
            if (approvedLevel == 11) {
                Set<DeliveryOrderDetail> doDetails = doObj.getRows();
                List<StockMovement> stockMovementsList = new ArrayList<>();
                for (DeliveryOrderDetail doDetail : doDetails) {
                    Product product = doDetail.getProduct();
                    if ((product != null && !doObj.isIsconsignment()) && ecp.isActivateInventoryTab() && (product.isIsBatchForProduct() || product.isIslocationforproduct() || product.isIswarehouseforproduct() || product.isIsrowforproduct() || product.isIsrackforproduct() || product.isIsbinforproduct() || product.isIsSerialForProduct())) {
                        accInvoiceModuleService.newStockMovementDeliveryOrder(doDetail, stockMovementsList);
                    }
                    Inventory inventory = doDetail.getInventory();
                    if (inventory.isInvrecord()) {
                        inventory.setBaseuomquantity(inventory.getActquantity());
                        inventory.getProduct().setAvailableQuantity(inventory.getProduct().getAvailableQuantity() - inventory.getBaseuomquantity());
                        inventory.setActquantity(0.0);
                    }
                }

                if (ecp != null && ecp.isActivateInventoryTab() && !stockMovementsList.isEmpty()) {
                    stockMovementService.addOrUpdateBulkStockMovement(doObj.getCompany(), doObj.getID(), stockMovementsList);
                }
                /*
                 * Post free gift JE for free gift type DO
                 */
                if (Integer.parseInt(doObj.getCompany().getCountry().getID()) == (Constants.malaysian_country_id)) {
                    paramJobj.put("approvalStatusLevel", approvedLevel);
                    accInvoiceModuleService.postJEForFreeGiftDo(paramJobj, doObj);
                }
            }

            if (approvedLevel != Constants.NoAuthorityToApprove && preferences.isSendapprovalmail()) {//If allow to send approval mail in company account preferences
                String userName = sessionHandlerImpl.getUserFullName(request);
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                Company company = (Company) returnObject.getEntityList().get(0);
                String sendorInfo = (!company.isEmailFromCompanyCreator()) ? Constants.ADMIN_EMAILID : authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                String creatormail = company.getCreator().getEmailID();
                String documentcreatoremail = (doObj != null && doObj.getCreatedby() != null) ? doObj.getCreatedby().getEmailID() : "";
                String fname = company.getCreator().getFirstName() == null ? "" : company.getCreator().getFirstName();
                String lname = company.getCreator().getLastName() == null ? "" : company.getCreator().getLastName();
                String creatorname = fname + " " + lname;
                String approvalpendingStatusmsg = "";
                HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                ArrayList<String> emailArray = new ArrayList<>();
                qdDataMap.put(Constants.companyKey, companyid);
                qdDataMap.put("level", level);
                qdDataMap.put(Constants.moduleid, Constants.Acc_Delivery_Order_ModuleId);
//                emailArray = commonFnControllerService.getUserApprovalEmail(qdDataMap);
                emailArray.add(creatormail);
                if (!StringUtil.isNullOrEmpty(documentcreatoremail) && !creatormail.equalsIgnoreCase(documentcreatoremail)) {
                    emailArray.add(documentcreatoremail);
                }
                String[] emails = {};
                emails = emailArray.toArray(emails);
//                String[] emails = {creatormail};                
                if (!StringUtil.isNullOrEmpty(preferences.getApprovalEmails())) {
                    String[] compPrefMailIds = preferences.getApprovalEmails().split(",");
                    emails = AccountingManager.getMergedMailIds(emails, compPrefMailIds);
                }
                if (doObj.getApprovestatuslevel() < 11) {
                    qdDataMap.put("ApproveMap", doApproveMap);
                    approvalpendingStatusmsg = commonFnControllerService.getApprovalstatusmsg(qdDataMap);
                }
                Map<String, Object> mailParameters = new HashMap();
                mailParameters.put("Number", doObj.getDeliveryOrderNumber());
                mailParameters.put("userName", userName);
                mailParameters.put("emails", emails);
                mailParameters.put("sendorInfo", sendorInfo);
                mailParameters.put("moduleName", Constants.Delivery_Order);
                mailParameters.put("addresseeName", "All");
                mailParameters.put("companyid", company.getCompanyID());
                mailParameters.put("baseUrl", baseUrl);
                mailParameters.put("approvalstatuslevel", doObj.getApprovestatuslevel());
                mailParameters.put("approvalpendingStatusmsg", approvalpendingStatusmsg);
                if (emails.length > 0) {
                    accountingHandlerDAOobj.sendApprovedEmails(mailParameters);
                }
            }

            //message construction
            if (approvedLevel != Constants.NoAuthorityToApprove) {
                msg = messageSource.getMessage("acc.field.DeliveryOrderhasbeenapprovedsuccessfully", null, RequestContextUtils.getLocale(request));
                msg = msg.substring(0, msg.length() - 1);
            } else {
                msg = messageSource.getMessage("acc.msgbox.YouarenotauthorizedtoapprovethisrecordatLevel", null, RequestContextUtils.getLocale(request));
            }

            // Save Approval History
            if (approvedLevel != Constants.NoAuthorityToApprove) {
                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                hashMap.put("transtype", Constants.DELIVERY_ORDER_APPROVAL);
                hashMap.put("transid", doObj.getID());
                hashMap.put("approvallevel", doObj.getApprovestatuslevel());//  If approvedLevel = 11 then its final Approval
                hashMap.put("remark", remark);
                hashMap.put("userid", sessionHandlerImpl.getUserid(request));
                hashMap.put("companyid", companyid);
                accountingHandlerDAOobj.updateApprovalHistory(hashMap);

                // Audit log entry
                auditTrailObj.insertAuditLog(AuditAction.DELIVERY_ORDER, "User " + sessionHandlerImpl.getUserFullName(request) + " has Approved a Delivery Order " + doObj.getDeliveryOrderNumber() + " at Level-" + doObj.getApprovestatuslevel(), request, doObj.getID());
                txnManager.commit(status);
                issuccess = true;
                KwlReturnObject kmsg = null;
                String roleName = "Company User";
                kmsg = permissionHandlerDAOObj.getRoleofUser(sessionHandlerImpl.getUserid(request));
                Iterator userRoleIterator = kmsg.getEntityList().iterator();
                while (userRoleIterator.hasNext()) {
                    Object[] row = (Object[]) userRoleIterator.next();
                    roleName = row[1].toString();
                }
                msg += " by " + roleName + " " + sessionHandlerImpl.getUserFullName(request) + " at Level " + doObj.getApprovestatuslevel() + ".";
            } else {
                txnManager.commit(status);
                issuccess = true;
                msg += doObj.getApprovestatuslevel() + ".";
            }

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("productIds", productIds);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /* function is moved to service layer */
    /*
     private void newStockMovementDeliveryOrder(DeliveryOrderDetail doDetail,List<StockMovement> stockMovementsList) throws ServiceException{
     try {
     String documentid=doDetail.getID();
     Product product=doDetail.getProduct();
     KwlReturnObject kmsg = null;
     List<Object[]> batchserialdetails = null;
     if (!product.isIsSerialForProduct()) {
     kmsg = accCommonTablesDAO.getBatchSerialDetails(documentid, true, false,  Integer.toString(Constants.Acc_Delivery_Order_ModuleId), false, true, "");
     batchserialdetails = kmsg.getEntityList();
     } else {
     kmsg = accCommonTablesDAO.getBatchSerialDetails(documentid, !product.isIsSerialForProduct(), false, Integer.toString(Constants.Acc_Delivery_Order_ModuleId), false, true, "");
     batchserialdetails = kmsg.getEntityList();
     }

     double ActbatchQty = 1;
     double batchQty = 0;
     if (batchserialdetails != null) {
     // Save Batch detail for both  option for serial no and batch also as if batch option is off then also we are generating batch in backend
     StockMovementDetail smd = null;
     StockMovement stockMovement = null;
     Map<Store, StockMovement> storeWiseStockMovement = new HashMap<>();
     for (Object[] objArr : batchserialdetails) {
     String locationid = "";
     String warehouseid = "";
     String batchid = (String) objArr[0];
     if (objArr[2] != null) {
     locationid = (String) objArr[2];
     }
     if (objArr[3] != null) {
     warehouseid = (String) objArr[3];
     }
     //double quantity = getNewBatchRemainingQuantity(locationid, warehouseid, companyid, product.getID(), batchid, moduleID, isEdit, documentid);
     //                    obj.put("avlquantity", quantity);
     if ((product.isIsBatchForProduct() || product.isIslocationforproduct() || product.isIswarehouseforproduct() || product.isIsrowforproduct() || product.isIsrackforproduct() || product.isIsbinforproduct())) {
     ActbatchQty = accCommonTablesDAO.getBatchQuantity(documentid, (String) objArr[0]);
     if (batchQty == 0) {
     batchQty = ActbatchQty;

     KwlReturnObject warehouseObj = accountingHandlerDAOobj.getObject(Store.class.getName(), warehouseid);
     Store store = (Store) warehouseObj.getEntityList().get(0);
     if (product.isIswarehouseforproduct() && product.isIslocationforproduct()) {
     if (storeWiseStockMovement.containsKey(store)) {
     stockMovement = storeWiseStockMovement.get(store);
     stockMovement.setQuantity(stockMovement.getQuantity() + batchQty);
     } else {
     stockMovement = new StockMovement();
     if (store != null) {
     stockMovement.setStore(store);
     }
     stockMovement.setCompany(product.getCompany());
     stockMovement.setProduct(product);
     stockMovement.setStockUoM(product.getUnitOfMeasure());
     stockMovement.setPricePerUnit(doDetail.getBaseuomrate() < 1 ? (doDetail.getRate() * (1 / doDetail.getBaseuomrate())) : doDetail.getRate() / doDetail.getBaseuomrate());
     stockMovement.setQuantity(batchQty);
     stockMovement.setTransactionDate(doDetail.getDeliveryOrder().getOrderDate());
     stockMovement.setModuleRefId(doDetail.getDeliveryOrder().getID());
     stockMovement.setModuleRefDetailId(doDetail.getID());
     stockMovement.setCustomer(doDetail.getDeliveryOrder().getCustomer());
     stockMovement.setCostCenter(doDetail.getDeliveryOrder().getCostcenter());
     stockMovement.setTransactionNo(doDetail.getDeliveryOrder().getDeliveryOrderNumber());
     if (doDetail.getDeliveryOrder().isIsconsignment()) {
     stockMovement.setTransactionModule(TransactionModule.ERP_Consignment_DO);
     stockMovement.setRemark("Consignment Delivery Order Created");
     } else {
     stockMovement.setTransactionModule(TransactionModule.ERP_DO);
     stockMovement.setRemark("Delivery Order Created");
     }                                    
     stockMovement.setTransactionType(TransactionType.OUT);
     stockMovement.setMemo(doDetail.getDeliveryOrder().getMemo());
     storeWiseStockMovement.put(store, stockMovement);
     }
     }
     }
     if ((product.isIslocationforproduct() || product.isIswarehouseforproduct() || product.isIsBatchForProduct() || product.isIsrowforproduct() || product.isIsrackforproduct() || product.isIsbinforproduct()) && (batchQty == ActbatchQty)) {
     KwlReturnObject locationUpdate = accountingHandlerDAOobj.getObject(Location.class.getName(), locationid);
     Location locationObj = (Location) locationUpdate.getEntityList().get(0);
     if (product.isIswarehouseforproduct() && product.isIslocationforproduct()) {
     smd = new StockMovementDetail();
     if (locationObj != null) {
     smd.setLocation(locationObj);
     }
     if (product.isIsrowforproduct()) {
     KwlReturnObject krObject = accountingHandlerDAOobj.getObject(StoreMaster.class.getName(), (String) objArr[15]);
     StoreMaster row = (StoreMaster) krObject.getEntityList().get(0);
     smd.setRow(row);
     }
     if (product.isIsrackforproduct()) {
     KwlReturnObject krObject = accountingHandlerDAOobj.getObject(StoreMaster.class.getName(), (String) objArr[16]);
     StoreMaster rack = (StoreMaster) krObject.getEntityList().get(0);
     smd.setRack(rack);
     }
     if (product.isIsbinforproduct()) {
     KwlReturnObject krObject = accountingHandlerDAOobj.getObject(StoreMaster.class.getName(), (String) objArr[17]);
     StoreMaster bin = (StoreMaster) krObject.getEntityList().get(0);
     smd.setBin(bin);
     }

     if (product.isIsBatchForProduct() && objArr[0] != null) {
     KwlReturnObject result1 = accountingHandlerDAOobj.getObject(NewProductBatch.class.getName(), objArr[0].toString());
     NewProductBatch newProductBatch = (NewProductBatch) result1.getEntityList().get(0);
     if (newProductBatch != null) {
     smd.setBatchName(newProductBatch.getBatchname());
     }
     }
     smd.setQuantity(batchQty);
     smd.setStockMovement(stockMovement);

     stockMovement.getStockMovementDetails().add(smd);
     }

     }
     }
     batchQty--;

     if (product.isIsSerialForProduct() && objArr[7] != null) {
     KwlReturnObject result1 = accountingHandlerDAOobj.getObject(NewBatchSerial.class.getName(), objArr[7].toString());
     NewBatchSerial newBatchSerial = (NewBatchSerial) result1.getEntityList().get(0);
     if (newBatchSerial != null && product.isIswarehouseforproduct() && product.isIslocationforproduct()) {
     smd.addSerialName(newBatchSerial.getSerialname());
     }
     } else {
     batchQty = 0;
     }

     }
     if (product.isIswarehouseforproduct() && product.isIslocationforproduct()) {
     for (Map.Entry<Store, StockMovement> entry : storeWiseStockMovement.entrySet()) {
     stockMovementsList.add(entry.getValue());
     }
     }
     }
     } catch (Exception ex) {
     Logger.getLogger(accInvoiceController.class.getName()).log(Level.INFO, ex.getMessage());
     }
        
     } */
    public void saveApprovedDOBatchDetails(HttpServletRequest request, DeliveryOrderDetail doDetail, boolean isInventoryActivated) throws SessionExpiredException, ServiceException, AccountingException {
        // this will work only for those products having only warehouse/location is active.For batch,serial code is not present.
        //this function is specially written for DO with Auto build Assembly and sent for approval(so default warehouse,default location will be used in this
        Company company = doDetail.getCompany();
        ExtraCompanyPreferences extraCompanyPreferences = null;
        KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), company.getCompanyID());
        extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
        boolean isnegativestockforlocwar = extraCompanyPreferences.isIsnegativestockforlocwar();

        DateFormat df1 = authHandler.getDateOnlyFormat(request);
        Product product = doDetail.getProduct();
        NewProductBatch productBatch = null;

        //save newproductbatch data for DO
        if ((product.isIswarehouseforproduct() || product.isIslocationforproduct()) && !product.isIsBatchForProduct() && !product.isIsSerialForProduct()) {

            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("company.companyID");
            filter_params.add(company.getCompanyID());
            String warehouse = "";
            if (product.isIswarehouseforproduct() && !StringUtil.isNullOrEmpty((product.getWarehouse() != null ? product.getWarehouse().getId() : null))) {
                warehouse = product.getWarehouse().getId();
                filter_names.add("warehouse.id");
                filter_params.add(warehouse);
            }
            String location = "";
            if (product.isIslocationforproduct() && !StringUtil.isNullOrEmpty((product.getLocation() != null ? product.getLocation().getId() : null))) {
                location = product.getLocation().getId();
                filter_names.add("location.id");
                filter_params.add(location);
            }
            if (!StringUtil.isNullOrEmpty(product.getID())) {
                filter_names.add("product");
                filter_params.add(product.getID());
            }
            filterRequestParams.put("filter_names", filter_names);
            filterRequestParams.put("filter_params", filter_params);
            filterRequestParams.put("order_by", order_by);
            filterRequestParams.put("order_type", order_type);
            KwlReturnObject result = accMasterItemsDAOobj.getNewBatches(filterRequestParams, false, false);
            List listResult = result.getEntityList();

            Iterator itrResult = listResult.iterator();
            Double quantityToDue = doDetail.getBaseuomquantity();
            while (itrResult.hasNext()) {
                NewProductBatch newProductBatch = (NewProductBatch) itrResult.next();
                if (quantityToDue > 0 && newProductBatch != null) {
                    double dueQty = newProductBatch.getQuantitydue();
                    HashMap<String, Object> batchUpdateQtyMap = new HashMap<String, Object>();
                    batchUpdateQtyMap.put("id", newProductBatch.getId());
                    if (quantityToDue > dueQty) {
                        if (!isnegativestockforlocwar) {
                            throw new AccountingException("Quantity is not sufficient in default warehouse/location.So,you can't approve this DO.");
                        } else {
                            batchUpdateQtyMap.put("qty", String.valueOf(-(quantityToDue)));
                        }
                    } else {
                        batchUpdateQtyMap.put("qty", String.valueOf(-(quantityToDue)));
                    }
                    accCommonTablesDAO.saveBatchAmountDue(batchUpdateQtyMap);

                    //save locationbatchdocumentmapping data for DO
                    HashMap<String, Object> documentMap = new HashMap<String, Object>();
                    documentMap.put("quantity", String.valueOf(doDetail.getBaseuomquantity()));
                    documentMap.put("documentid", doDetail.getID());
                    documentMap.put("transactiontype", "27");
                    documentMap.put("mfgdate", newProductBatch.getMfgdate());
                    documentMap.put("expdate", newProductBatch.getExpdate());
                    documentMap.put("batchmapid", newProductBatch.getId());
                    accCommonTablesDAO.saveBatchDocumentMapping(documentMap);

                    //make stock movement entry on inventory side if Inventory is activated
                    if (product.isIswarehouseforproduct() && product.isIslocationforproduct() && isInventoryActivated) {
                        KwlReturnObject warehouseObj = accountingHandlerDAOobj.getObject(Store.class.getName(), warehouse);
                        Store store = (Store) warehouseObj.getEntityList().get(0);
                        StockMovement stockMovement = new StockMovement();
                        if (store != null) {
                            stockMovement.setStore(store);
                        }
                        stockMovement.setCompany(company);
                        stockMovement.setProduct(doDetail.getProduct());
                        stockMovement.setStockUoM(doDetail.getProduct().getUnitOfMeasure());
                        stockMovement.setPricePerUnit(doDetail.getRate() / doDetail.getBaseuomrate());
                        stockMovement.setQuantity(quantityToDue);
                        stockMovement.setTransactionDate(doDetail.getDeliveryOrder().getOrderDate());
                        stockMovement.setModuleRefId(doDetail.getDeliveryOrder().getID());
                        stockMovement.setModuleRefDetailId(doDetail.getID());
                        stockMovement.setRemark("DO Created.");
                        stockMovement.setCustomer(doDetail.getDeliveryOrder().getCustomer());
                        stockMovement.setCostCenter(doDetail.getDeliveryOrder().getCostcenter());
                        stockMovement.setTransactionNo(doDetail.getDeliveryOrder().getDeliveryOrderNumber());
                        stockMovement.setTransactionModule(TransactionModule.ERP_DO);
                        stockMovement.setMemo(doDetail.getDeliveryOrder().getMemo());
                        stockMovement.setTransactionType(TransactionType.OUT);

                        StockMovementDetail smd = new StockMovementDetail();

                        KwlReturnObject locationUpdate = accountingHandlerDAOobj.getObject(Location.class.getName(), location);
                        Location locationObj = (Location) locationUpdate.getEntityList().get(0);

                        if (locationObj != null) {
                            smd.setLocation(locationObj);
                        }
                        smd.setQuantity(quantityToDue);
                        smd.setBatchName("");
                        smd.setStockMovement(stockMovement);
                        stockMovement.getStockMovementDetails().add(smd);
                        stockMovementService.addStockMovement(stockMovement);
                    }
                }
            }
        }
    }

    public ModelAndView rejectPendingInvoice(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Invoice_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String roleName = "";
            int level = 0;
            KwlReturnObject userRoleResult = permissionHandlerDAOObj.getRoleofUser(sessionHandlerImpl.getUserid(request));
            Iterator itr = userRoleResult.getEntityList().iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                roleName = row[1].toString();
            }

            String invID = "";
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            JSONObject jObj = jArr.getJSONObject(0);
            invID = StringUtil.DecodeText(jObj.optString("billid"));
            KwlReturnObject invRes = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invID);
            Invoice invObj = (Invoice) invRes.getEntityList().get(0);
            level = invObj.getApprovestatuslevel();

            boolean isRejected = rejectPendingInvoice(request);
            txnManager.commit(status);
            issuccess = true;

            if (isRejected) {
                msg = messageSource.getMessage("acc.field.SalesInvoicehasbeenrejectedsuccessfully", null, RequestContextUtils.getLocale(request)) + " by " + roleName + " " + sessionHandlerImpl.getUserFullName(request) + " at Level " + level + ".";
            } else {
                msg = messageSource.getMessage("acc.vq.notAuthorisedToRejectThisRecord", null, RequestContextUtils.getLocale(request)) + " at Level " + level + ".";
            }

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public boolean rejectPendingInvoice(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException, ScriptException {
        boolean isRejected = false;
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String remark = request.getParameter("remark");
            String currentUser = sessionHandlerImpl.getUserid(request);
            KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), currentUser);
            User user = (User) userResult.getEntityList().get(0);
            String actionId = "66", actionMsg = "rejected";
            int level = 0;
            String amount = "";
            for (int i = 0; i < jArr.length(); i++) {
                boolean hasAuthorityToReject = false;
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("billid"))) {
                    String invid = StringUtil.DecodeText(jobj.optString("billid"));
                    KwlReturnObject cap = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invid);
                    Invoice invObj = (Invoice) cap.getEntityList().get(0);
                    double totalAmount = StringUtil.isNullOrEmpty(request.getParameter("totalorderamount")) ? 0 : authHandler.round(Double.parseDouble(request.getParameter("totalorderamount")), companyid);
                    HashMap<String, Object> invApproveMap = new HashMap<String, Object>();
                    level = invObj.getApprovestatuslevel();
                    invApproveMap.put("companyid", companyid);
                    invApproveMap.put("level", level);
                    invApproveMap.put("totalAmount", String.valueOf(totalAmount));
                    invApproveMap.put("currentUser", currentUser);
                    invApproveMap.put("fromCreate", false);
                    invApproveMap.put("moduleid", Constants.Acc_Invoice_ModuleId);
                    amount = String.valueOf(totalAmount);
                    if (AccountingManager.isCompanyAdmin(user)) {
                        hasAuthorityToReject = true;
                    } else {
                        hasAuthorityToReject = accountingHandlerDAOobj.checkForMultiLevelApprovalRules(invApproveMap);
                    }
                    if (hasAuthorityToReject) {
                        KwlReturnObject InvoiceDo = accInvoiceDAOobj.getAutogeneratedDOFromInvoices(invObj.getID(), companyid);
                        if (InvoiceDo.getEntityList() != null && InvoiceDo.getEntityList().size() > 0) {
                            Object[] oj = (Object[]) InvoiceDo.getEntityList().get(0);
                            String DeliveryOrderID = oj[1].toString();

                            accInvoiceDAOobj.releaseBatchSerialData(DeliveryOrderID, companyid);

                            accInvoiceDAOobj.rejectPendingDO(DeliveryOrderID, companyid);
                        }
                        accInvoiceDAOobj.rejectPendingInvoice(invObj.getID(), companyid);
                        isRejected = true;
                        // Maintain Approval History of Rejected Record
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("transtype", Constants.Invoice_APPROVAL);
                        hashMap.put("transid", invObj.getID());
                        hashMap.put("approvallevel", Math.abs(invObj.getApprovestatuslevel()));//  If approvedLevel = 11 then its final Approval
                        hashMap.put("remark", remark);
                        hashMap.put("userid", sessionHandlerImpl.getUserid(request));
                        hashMap.put("companyid", companyid);
                        hashMap.put("isrejected", true);
                        accountingHandlerDAOobj.updateApprovalHistory(hashMap);
                        auditTrailObj.insertAuditLog(actionId, "User " + sessionHandlerImpl.getUserFullName(request) + " " + actionMsg + " Sales Invoice " + invObj.getInvoiceNumber(), request, invObj.getID());
                    }
                }
            }
        }/* catch (UnsupportedEncodingException ex) {
         throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)), ex);
         }*/ catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)));
        }
        return isRejected;
    }

    public ModelAndView rejectPendingDO(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Quotation_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String roleName = "";
            int level = 0;
            KwlReturnObject userRoleResult = permissionHandlerDAOObj.getRoleofUser(sessionHandlerImpl.getUserid(request));
            Iterator itr = userRoleResult.getEntityList().iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                roleName = row[1].toString();
            }
            String doID = "";
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            JSONObject jObj = jArr.getJSONObject(0);
            doID = StringUtil.DecodeText(jObj.optString("billid"));
            KwlReturnObject DOObj = accountingHandlerDAOobj.getObject(DeliveryOrder.class.getName(), doID);
            DeliveryOrder doObj = (DeliveryOrder) DOObj.getEntityList().get(0);
            level = doObj.getApprovestatuslevel();
            boolean isRejected = rejectPendingDO(request);
            txnManager.commit(status);
            issuccess = true;
            if (isRejected) {
                msg = messageSource.getMessage("acc.field.DeliverOrderhasbeenrejectedsuccessfully", null, RequestContextUtils.getLocale(request)) + " by " + roleName + " " + sessionHandlerImpl.getUserFullName(request) + " at Level " + level + ".";
            } else {
                msg = messageSource.getMessage("acc.vq.notAuthorisedToRejectThisRecord", null, RequestContextUtils.getLocale(request)) + " at level " + level + ".";
            }
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public boolean rejectPendingDO(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException, ScriptException {
        boolean isRejected = false;
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currentUser = sessionHandlerImpl.getUserid(request);
            KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), currentUser);
            User user = (User) userResult.getEntityList().get(0);
            String actionId = "66", actionMsg = "rejected";
            int level = 0;
            String amount = "";
            for (int i = 0; i < jArr.length(); i++) {
                boolean hasAuthorityToReject = false;
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("billid"))) {
                    String doid = StringUtil.DecodeText(jobj.optString("billid"));
                    KwlReturnObject cap = accountingHandlerDAOobj.getObject(DeliveryOrder.class.getName(), doid);
                    DeliveryOrder doObj = (DeliveryOrder) cap.getEntityList().get(0);
//                    PurchaseRequisition requisition = (PurchaseRequisition) kwlCommonTablesDAOObj.getClassObject(PurchaseRequisition.class.getName(), poid);
                    double totalAmount = StringUtil.isNullOrEmpty(request.getParameter("totalorderamount")) ? 0 : authHandler.round(Double.parseDouble(request.getParameter("totalorderamount")), companyid);
                    amount = String.valueOf(totalAmount);
                    level = doObj.getApprovestatuslevel();
                    if (AccountingManager.isCompanyAdmin(user)) {
                        hasAuthorityToReject = true;
                    } else {
                        hasAuthorityToReject = accountingHandlerDAOobj.checkForMultiLevelApprovalRule(level, companyid, amount, currentUser, Constants.Acc_Delivery_Order_ModuleId);
                    }
                    if (hasAuthorityToReject) {
                        accInvoiceDAOobj.rejectPendingDO(doObj.getID(), companyid);
                        isRejected = true;
                        // Maintain Approval History of Rejected Record
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("transtype", Constants.DELIVERY_ORDER_APPROVAL);
                        hashMap.put("transid", doObj.getID());
                        hashMap.put("approvallevel", Math.abs(doObj.getApprovestatuslevel()));//  If approvedLevel = 11 then its final Approval
                        hashMap.put("remark", "");
                        hashMap.put("userid", sessionHandlerImpl.getUserid(request));
                        hashMap.put("companyid", companyid);
                        hashMap.put("isrejected", true);
                        accountingHandlerDAOobj.updateApprovalHistory(hashMap);
                        String auditMsg = "User " + sessionHandlerImpl.getUserFullName(request) + " " + actionMsg + " Delivery Order " + doObj.getDeliveryOrderNumber();
                        //Update the sales order balance quantity.
                        String linkedSONumbers = "";
                        KwlReturnObject soResult = accInvoiceDAOobj.updateSOBalanceQtyAfterDO(doid, "", companyid);
                        if(soResult!=null && soResult.getEntityList() != null && soResult.getEntityList().size()>0){
                            linkedSONumbers = soResult.getEntityList().toString();
                            auditMsg += " and it's respective linked Sales Order(s) " + linkedSONumbers + " are available again.";
                        }
                        /* 
                         * Deleting linking information for SO->DO while rejecting DO
                         */
                        Set<DeliveryOrderDetail> dodetails = doObj.getRows();
                        for (DeliveryOrderDetail deliveryOrderDetail : dodetails) {
                            if (deliveryOrderDetail.getSodetails() != null ) { // SO linked in DO
                                SalesOrderDetail salesOrderDetail = deliveryOrderDetail.getSodetails();
                                deliveryOrderDetail.setSodetails(null);
                                salesOrderDetail.getSalesOrder().setLinkflag(0);
                                salesOrderDetail.getSalesOrder().setIsopen(true);
                                salesOrderDetail.setIsLineItemClosed(false);
                                HashMap<String, Object> linkingrequestParams = new HashMap<String, Object>();
                                linkingrequestParams.put("doid", doObj.getID());
                                linkingrequestParams.put(Constants.billid, salesOrderDetail.getSalesOrder() == null ? "" : salesOrderDetail.getSalesOrder().getID());
                                linkingrequestParams.put("type", 2);
                                linkingrequestParams.put("unlinkflag", true);
                                accInvoiceDAOobj.deleteLinkingInformationOfDO(linkingrequestParams);
                            }
                        }
                        auditTrailObj.insertAuditLog(actionId, auditMsg, request, doObj.getID());                        
                    }
                }
            }
        } /*catch (UnsupportedEncodingException ex) {
         throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)), ex);
         }*/ catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)));
        }
        return isRejected;
    }

    public List<String> approveDO(DeliveryOrder doObj, HashMap<String, Object> doApproveMap, boolean isMailApplicable) throws SessionExpiredException, AccountingException, ServiceException, ScriptException, MessagingException, JSONException {
        boolean hasAuthority = false;
        String companyid = "";
        List returnList = new ArrayList();
        List mailParamList = new ArrayList();
        int returnStatus;
        Date postingDate = null;
        if (doApproveMap.containsKey("companyid") && doApproveMap.get("companyid") != null) {
            companyid = doApproveMap.get("companyid").toString();
        }
        String currentUser = "";
        if (doApproveMap.containsKey("currentUser") && doApproveMap.get("currentUser") != null) {
            currentUser = doApproveMap.get("currentUser").toString();
        }
        int level = 0;
        if (doApproveMap.containsKey("level") && doApproveMap.get("level") != null) {
            level = Integer.parseInt(doApproveMap.get("level").toString());
        }
        String amount = "";
        if (doApproveMap.containsKey("totalAmount") && doApproveMap.get("totalAmount") != null) {
            amount = doApproveMap.get("totalAmount").toString();
        }
        boolean fromCreate = false;
        if (doApproveMap.containsKey("fromCreate") && doApproveMap.get("fromCreate") != null) {
            fromCreate = Boolean.parseBoolean(doApproveMap.get("fromCreate").toString());
        }
        if (doApproveMap.containsKey("postingDate") && doApproveMap.get("postingDate") != null) {
            postingDate = (Date) doApproveMap.get("postingDate");
        }
        JSONArray productDiscountMapList = null;
        if (doApproveMap.containsKey("productDiscountMapList") && doApproveMap.get("productDiscountMapList") != null) {
            productDiscountMapList = new JSONArray(doApproveMap.get("productDiscountMapList").toString());
        }
        if (!fromCreate) {
            String thisUser = currentUser;
            KwlReturnObject userclass = accountingHandlerDAOobj.getObject(User.class.getName(), thisUser);
            User user = (User) userclass.getEntityList().get(0);

            if (AccountingManager.isCompanyAdmin(user)) {
                hasAuthority = true;
            } else {
                hasAuthority = accountingHandlerDAOobj.checkForMultiLevelApprovalRules(doApproveMap);
            }
        } else {
            hasAuthority = true;
        }
        if (hasAuthority) {
            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = mgr.getEngineByName("JavaScript");
            int approvalStatus = 11;
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            String prNumber = doObj.getDeliveryOrderNumber();
            String doID = doObj.getID();
            HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
            qdDataMap.put("companyid", companyid);
            qdDataMap.put("level", level + 1);
            qdDataMap.put("moduleid", Constants.Acc_Delivery_Order_ModuleId);
            KwlReturnObject flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(qdDataMap);
            Iterator itr = flowresult.getEntityList().iterator();
            String fromName = "User";
            fromName = doObj.getCreatedby().getFirstName().concat(" ").concat(doObj.getCreatedby().getLastName());
            /**
             * parameters required for sending mail
             */
            Map<String, Object> mailParameteres = new HashMap();
            mailParameteres.put(Constants.companyid, companyid);
            mailParameteres.put(Constants.prNumber, prNumber);
            mailParameteres.put(Constants.fromName, fromName);
            mailParameteres.put(Constants.moduleid, Constants.Acc_Delivery_Order_ModuleId);
            mailParameteres.put(Constants.isCash, false);
            mailParameteres.put(Constants.createdBy, doObj.getCreatedby().getUserID());
            if (doApproveMap.containsKey(Constants.PAGE_URL)) {
                mailParameteres.put(Constants.PAGE_URL, (String) doApproveMap.get(Constants.PAGE_URL));
            }
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                mailParameteres.put(Constants.ruleid, row[0].toString());
                //            JSONObject obj = new JSONObject();
                HashMap<String, Object> recMap = new HashMap();
                String rule = "";
                if (row[2] != null) {
                    rule = row[2].toString();
                }
                String discountRule = "";
                if (row[7] != null) {
                    discountRule = row[7].toString();
                }
                boolean sendForApproval = false;
                int appliedUpon = Integer.parseInt(row[5].toString());
                if (appliedUpon == Constants.Specific_Products || appliedUpon == Constants.Specific_Products_Discount) {
                    if (productDiscountMapList != null) {
                        sendForApproval = AccountingManager.checkForProductAndProductDiscountRule(productDiscountMapList, appliedUpon, rule, discountRule);
                    }
                } else if (appliedUpon == Constants.Specific_Products_Category) {
                    /*
                     * Check If Rule is apply on product
                     * category from multiapproverule window
                     */
                    sendForApproval = accountingHandlerDAOobj.checkForProductCategoryForProduct(productDiscountMapList, appliedUpon, rule);
                } else {
                    rule = rule.replaceAll("[$$]+", amount);
                }
                if (StringUtil.isNullOrEmpty(rule) || (!StringUtil.isNullOrEmpty(rule) && appliedUpon != Constants.Specific_Products && appliedUpon != Constants.Specific_Products_Discount && appliedUpon != Constants.Specific_Products_Category && Boolean.parseBoolean(engine.eval(rule).toString())) || sendForApproval) {
                    // send emails
                    boolean hasApprover = Boolean.parseBoolean(row[3].toString());
                    mailParameteres.put(Constants.hasApprover, hasApprover);
                    if (isMailApplicable) {
                        approvalStatus = level + 1;
                        mailParameteres.put("level", level);
                        accInvoiceModuleService.sendMailToApprover(mailParameteres);

                    } else {
                        approvalStatus = level + 1;
                        recMap.put("ruleid", row[0].toString());
                        recMap.put("fromName", fromName);
                        recMap.put("hasApprover", hasApprover);

                        mailParamList.add(recMap);
                    }

                }
            }
            accInvoiceDAOobj.approvePendingDO(doID, companyid, approvalStatus);
            if (doObj != null && doObj.getInventoryJE() != null) {
                KwlReturnObject invJEObj = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), doObj.getInventoryJE().getID());
                JournalEntry invJE = (JournalEntry) invJEObj.getEntityList().get(0);
                if (invJE != null) {
                    invJE.setApprovestatuslevel(approvalStatus);
                    if (postingDate != null) {
                        invJE.setEntryDate(postingDate);
                    }
                }
            }
            returnStatus = approvalStatus;
        } else {
            returnStatus = Constants.NoAuthorityToApprove; //if not have approval permission then return one fix value like 999
        }
        returnList.add(returnStatus);
        returnList.add(mailParamList);

        return returnList;
    }

    public ModelAndView applyForTaxToDeliveryOrders(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);

        try {
            applyForTaxToDeliveryOrders(request);

            issuccess = true;
            msg = "Delivery Order(s) has been applied for taxes successfully.";//messageSource.getMessage("acc.agedPay.inv", null, RequestContextUtils.getLocale(request)) + " " + invoiceNumber + " " + messageSource.getMessage("acc.field.hasbeen", null, RequestContextUtils.getLocale(request)) + " " + succMsg + " " + messageSource.getMessage("acc.field.successfully", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (AccountingException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void applyForTaxToDeliveryOrders(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException, JSONException, UnsupportedEncodingException, ParseException {
        DeliveryOrder deliveryOrder = null;
        String companyid = sessionHandlerImpl.getCompanyid(request);
        DateFormat df = authHandler.getDateOnlyFormat(request);
        JSONArray jArr = new JSONArray(request.getParameter("invoiceData"));
        Date appliedDate = null;
        if (!StringUtil.isNullOrEmpty(request.getParameter("filterationDate"))) {
            appliedDate = df.parse(request.getParameter("filterationDate"));
        }
        for (int i = 0; i < jArr.length(); i++) {

            JSONObject jobj = jArr.getJSONObject(i);

            String doid = jobj.getString("billId");

            HashMap<String, Object> doDataMap = new HashMap<String, Object>();
            KwlReturnObject doObj = accountingHandlerDAOobj.getObject(DeliveryOrder.class.getName(), doid);
            DeliveryOrder deliveryOrder1 = (DeliveryOrder) doObj.getEntityList().get(0);
            doDataMap.put("id", doid);
            doDataMap.put("companyid", companyid);
            doDataMap.put("isAppliedForTax", true);
            doDataMap.put("orderdate", deliveryOrder1.getOrderDate());
            doDataMap.put("appliedDate", appliedDate);
            KwlReturnObject doresult = accInvoiceDAOobj.saveDeliveryOrder(doDataMap);
            deliveryOrder = (DeliveryOrder) doresult.getEntityList().get(0);

            System.out.println("Delivery Order : " + deliveryOrder.getDeliveryOrderNumber() + " has been applied for tax");
        }
    }

    public ModelAndView updateSOScript(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        String channelName = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("PO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        try {
            String subdomain = "";
            String[] subdomainArray = null;
            if (!StringUtil.isNullOrEmpty(request.getParameter("subdomain"))) {
                subdomain = request.getParameter("subdomain").toString().trim();
                subdomainArray = subdomain.split(",");
            }
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            KwlReturnObject rCompanyId = null;
            rCompanyId = accInvoiceDAOobj.getAllCompanyFromDb(subdomainArray);
            Iterator itrCompanyId = rCompanyId.getEntityList().iterator();
            while (itrCompanyId.hasNext()) {
                TransactionStatus status = null;
                String companyid = (String) itrCompanyId.next();
                try {
                    status = txnManager.getTransaction(def);

                    ExtraCompanyPreferences extraCompanyPreferences = null;
                    KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
                    extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
                    KwlReturnObject result = null;
                    requestParams.put("companyid", companyid);
                    requestParams.put("value", "2");
                    result = accInvoiceDAOobj.getSOForScript(requestParams);
                    Iterator itr = result.getEntityList().iterator();
                    while (itr.hasNext()) {
                        String linkNumbers = (String) itr.next();
                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), linkNumbers);
                        SalesOrder salesOrder = (SalesOrder) rdresult.getEntityList().get(0);
                        requestParams.put("salesOrder", salesOrder);
                        requestParams.put("value", "2");
                        String status1 = accInvoiceModuleService.getSalesOrderStatusForDO(salesOrder, false, extraCompanyPreferences, "");
                        boolean isSOOpen = false;
                        if (status1.equals("Open")) {
                            isSOOpen = true;
                        }
                        requestParams.put("isSOOpen", isSOOpen);
                        accInvoiceDAOobj.updateSOLinkflag(requestParams);
                    }
                    txnManager.commit(status);
                    msg += companyid + " TRUE<br/>";
                } catch (Exception ex) {
                    if (status != null) {
                        txnManager.rollback(status);
                    }
                    Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
                    msg += companyid + " FALSE<br/>";
                }
            }
            issuccess = true;
        } catch (Exception ex) {

            ex.printStackTrace();
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);

            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView updateSIScript(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        String channelName = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("PO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        try {
            String subdomain = "";
            String[] subdomainArray = null;
            if (!StringUtil.isNullOrEmpty(request.getParameter("subdomain"))) {
                subdomain = request.getParameter("subdomain").toString().trim();
                subdomainArray = subdomain.split(",");
            }
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            KwlReturnObject rCompanyId = null;
            rCompanyId = accInvoiceDAOobj.getAllCompanyFromDb(subdomainArray);
            Iterator itrCompanyId = rCompanyId.getEntityList().iterator();
            while (itrCompanyId.hasNext()) {
                TransactionStatus status = null;
                String companyid = (String) itrCompanyId.next();
                try {
                    status = txnManager.getTransaction(def);

                    KwlReturnObject result = null;
                    requestParams.put("companyid", companyid);
                    result = accInvoiceDAOobj.getSIDForScript(requestParams);
                    Iterator itr = result.getEntityList().iterator();
                    while (itr.hasNext()) {
                        String linkNumbers = (String) itr.next();
                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), linkNumbers);
                        Invoice invoice = (Invoice) rdresult.getEntityList().get(0);
                        requestParams.put("invoice", invoice);
                        String status1 = accInvoiceModuleService.getInvoiceStatusForDO(invoice);
                        boolean isSOOpen = false;
                        if (status1.equals("Open")) {
                            isSOOpen = true;
                        }
                        requestParams.put("isOpenDO", isSOOpen);
                        accInvoiceDAOobj.updateInvoiceLinkflag(requestParams);
                    }
                    msg += (companyid + "-TRUE<br/>");
                    txnManager.commit(status);
                } catch (Exception ex) {
                    msg += (companyid + "-FALSE<br/>");
                    if (status != null) {
                        txnManager.rollback(status);
                    }
                    Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);

                }
            }
            issuccess = true;

        } catch (Exception ex) {
            ex.printStackTrace();
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);

            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public String getInvoiceStatusForDO(Invoice iv) throws ServiceException {
        Set<InvoiceDetail> ivDetail = iv.getRows();
        Iterator ite = ivDetail.iterator();
        String result = "Closed";
        while (ite.hasNext()) {
            InvoiceDetail iDetail = (InvoiceDetail) ite.next();
            KwlReturnObject idresult = accInvoiceDAOobj.getDOIDFromInvoiceDetails(iDetail.getID());
            List list = idresult.getEntityList();
            Iterator ite1 = list.iterator();
            double qua = 0;
            while (ite1.hasNext()) {
                DeliveryOrderDetail ge = (DeliveryOrderDetail) ite1.next();
                qua += ge.getInventory().getQuantity();
            }
            if (qua < iDetail.getInventory().getQuantity()) {
                result = "Open";
                break;
            }
        }
        return result;
    }

    /**
     *
     * @param deliveryOrder
     * @param doApproveMap (Constants.PAGE_URL)
     * @return
     * @throws SessionExpiredException
     * @throws ServiceException
     * @throws JSONException
     * @throws AccountingException
     * @throws ScriptException
     */
    public int getDOCurrentApprovalStatusLevel(DeliveryOrder deliveryOrder, HashMap<String, Object> doApproveMap) throws SessionExpiredException, ServiceException, JSONException, AccountingException, ScriptException {

        int currentDOApprovalLevel = 11;
        // Check for Multi level Approval Rules
        if (!(deliveryOrder.isFixedAssetDO() || deliveryOrder.isLeaseDO() || deliveryOrder.isIsconsignment() || deliveryOrder.isIsAutoGeneratedDO())) {
            // if this is a lease or fixed asset DO or consignment Dothen it will not go for approval
            boolean isMailApplicable = false;
            boolean hasAuthority = false;
            String companyid = "";
            List mailParamList = new ArrayList();
            if (doApproveMap.containsKey("companyid") && doApproveMap.get("companyid") != null) {
                companyid = doApproveMap.get("companyid").toString();
            }
            String currentUser = "";
            if (doApproveMap.containsKey("currentUser") && doApproveMap.get("currentUser") != null) {
                currentUser = doApproveMap.get("currentUser").toString();
            }
            int level = 0;
            if (doApproveMap.containsKey("level") && doApproveMap.get("level") != null) {
                level = Integer.parseInt(doApproveMap.get("level").toString());
            }
            String amount = "";
            if (doApproveMap.containsKey("totalAmount") && doApproveMap.get("totalAmount") != null) {
                amount = doApproveMap.get("totalAmount").toString();
            }
            boolean fromCreate = false;
            if (doApproveMap.containsKey("fromCreate") && doApproveMap.get("fromCreate") != null) {
                fromCreate = Boolean.parseBoolean(doApproveMap.get("fromCreate").toString());
            }
            JSONArray productDiscountMapList = null;
            if (doApproveMap.containsKey("productDiscountMapList") && doApproveMap.get("productDiscountMapList") != null) {
                productDiscountMapList = new JSONArray(doApproveMap.get("productDiscountMapList").toString());
            }
            if (!fromCreate) {
                String thisUser = currentUser;
                KwlReturnObject userclass = accountingHandlerDAOobj.getObject(User.class.getName(), thisUser);
                User user = (User) userclass.getEntityList().get(0);

                if (AccountingManager.isCompanyAdmin(user)) {
                    hasAuthority = true;
                } else {
                    hasAuthority = accountingHandlerDAOobj.checkForMultiLevelApprovalRules(doApproveMap);
                }
            } else {
                hasAuthority = true;
            }
            if (hasAuthority) {
                ScriptEngineManager mgr = new ScriptEngineManager();
                ScriptEngine engine = mgr.getEngineByName("JavaScript");
                int approvalStatus = 11;
                KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
                String prNumber = deliveryOrder.getDeliveryOrderNumber();
                String doID = deliveryOrder.getID();
                HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                qdDataMap.put("companyid", companyid);
                qdDataMap.put("level", level + 1);
                qdDataMap.put("moduleid", Constants.Acc_Delivery_Order_ModuleId);
                KwlReturnObject flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(qdDataMap);
                Iterator itr = flowresult.getEntityList().iterator();
                String fromName = "User";
                fromName = deliveryOrder.getCreatedby().getFirstName().concat(" ").concat(deliveryOrder.getCreatedby().getLastName());
                /**
                 * parameters required for sending mail
                 */
                Map<String, Object> mailParameteres = new HashMap();
                mailParameteres.put(Constants.companyid, companyid);
                mailParameteres.put(Constants.prNumber, prNumber);
                mailParameteres.put(Constants.fromName, fromName);
                mailParameteres.put(Constants.moduleid, Constants.Acc_Delivery_Order_ModuleId);
                mailParameteres.put(Constants.isCash, false);
                mailParameteres.put(Constants.createdBy, deliveryOrder.getCreatedby().getUserID());
                if (doApproveMap.containsKey(Constants.PAGE_URL)) {
                    mailParameteres.put(Constants.PAGE_URL, (String) doApproveMap.get(Constants.PAGE_URL));
                }
                while (itr.hasNext()) {
                    Object[] row = (Object[]) itr.next();
                    mailParameteres.put(Constants.ruleid, row[0].toString());
                    //            JSONObject obj = new JSONObject();
                    HashMap<String, Object> recMap = new HashMap();
                    String rule = "";
                    if (row[2] != null) {
                        rule = row[2].toString();
                    }
                    String discountRule = "";
                    if (row[7] != null) {
                        discountRule = row[7].toString();
                    }
                    boolean sendForApproval = false;
                    int appliedUpon = Integer.parseInt(row[5].toString());
                    if (appliedUpon == Constants.Specific_Products || appliedUpon == Constants.Specific_Products_Discount) {
                        if (productDiscountMapList != null) {
                            sendForApproval = AccountingManager.checkForProductAndProductDiscountRule(productDiscountMapList, appliedUpon, rule, discountRule);
                        }
                    } else if (appliedUpon == Constants.Specific_Products_Category) {
                        /*
                         * Check If Rule is apply on product category from
                         * multiapproverule window
                         */
                        sendForApproval = accountingHandlerDAOobj.checkForProductCategoryForProduct(productDiscountMapList, appliedUpon, rule);
                    } else {
                        rule = rule.replaceAll("[$$]+", amount);
                    }
                    if (StringUtil.isNullOrEmpty(rule) || (!StringUtil.isNullOrEmpty(rule) && appliedUpon != Constants.Specific_Products && appliedUpon != Constants.Specific_Products_Discount && appliedUpon != Constants.Specific_Products_Category && Boolean.parseBoolean(engine.eval(rule).toString())) || sendForApproval) {
                        // send emails
                        boolean hasApprover = Boolean.parseBoolean(row[3].toString());
                        mailParameteres.put(Constants.hasApprover, hasApprover);
                        if (isMailApplicable) {
                            accInvoiceModuleService.sendMailToApprover(mailParameteres);
                            approvalStatus = level + 1;
                        } else {
                            approvalStatus = level + 1;
                            recMap.put("ruleid", row[0].toString());
                            recMap.put("fromName", fromName);
                            recMap.put("hasApprover", hasApprover);
                            mailParamList.add(recMap);
                        }

                    }
                }
                currentDOApprovalLevel = approvalStatus;
            } else {
                currentDOApprovalLevel = Constants.NoAuthorityToApprove; //if not have approval permission then return one fix value like 999
            }
        }
        return currentDOApprovalLevel;
    }

    /*  saveAutoBuildAssemblyBatch 
     Here product is Assembly product which is going to be aasembled
     productbuild object is passed to save its id in locationbatchdocumentmapping table as documentid ie. buildid
     */
    private void sendRequestToCRMForUpdatingProductReplacementStatus(HttpServletRequest request, JSONArray crmArray) throws JSONException, SessionExpiredException, ServiceException {
        Session session = null;
        if (crmArray.length() > 0) {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            JSONObject crmJson = new JSONObject();
            crmJson.put("data", crmArray);
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("userid", sessionHandlerImpl.getUserid(request));
            userData.put("companyid", companyid);
            userData.put("requestStatusDetails", crmArray);

//            session = HibernateUtil.getCurrentSession();
//            String crmURL = this.getServletContext().getInitParameter("crmURL");
            String crmURL = URLUtil.buildRestURL(Constants.crmURL);
            crmURL = crmURL + "master/replacementformstatus";
            apiCallHandlerService.restPostMethod(crmURL, userData.toString());
//            try {
//                JSONObject resObj = apiCallHandlerService.callApp(crmURL, userData, companyid, "204");
//            } catch (Exception ex) {
//                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
//            }
//            finally{
//                HibernateUtil.closeSession(session);
//            }
        }
    }

    private Set<InvoiceContractMapping> getInvoiceContractMappings(HttpServletRequest request, Invoice invoice) throws ServiceException {
        String[] deliveryOrderIds = request.getParameter("linkNumber").split(",");
        Set<InvoiceContractMapping> contractMappings = new HashSet<InvoiceContractMapping>();
        for (int i = 0; i < deliveryOrderIds.length; i++) {
            if (!StringUtil.isNullOrEmpty(deliveryOrderIds[i])) {
                KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(DeliveryOrder.class.getName(), deliveryOrderIds[i]);
                DeliveryOrder deliveryOrder = (DeliveryOrder) rdresult.getEntityList().get(0);

                Set<DOContractMapping> dOContractMappings = deliveryOrder.getdOContractMappings();
                if (!dOContractMappings.isEmpty()) {
                    List<String> contractMappingDuplicateCheckList = new ArrayList<String>();

                    /*
                     * DO can be create by linking with multiple sales order.
                     * and each sales order may point to different Contract. so
                     * for a single DO there may be multiple Contracts. and
                     * docontractmapping table may contain duplicate values of
                     * contract.
                     */
                    for (DOContractMapping contractMapping : dOContractMappings) {
                        if (!contractMappingDuplicateCheckList.contains(contractMapping.getContract().getID())) {
                            contractMappingDuplicateCheckList.add(contractMapping.getContract().getID());
                            InvoiceContractMapping invoiceContractMapping = new InvoiceContractMapping();
                            invoiceContractMapping.setCompany(invoice.getCompany());
                            invoiceContractMapping.setContract(contractMapping.getContract());
                            invoiceContractMapping.setDeliveryOrder(deliveryOrder);
                            invoiceContractMapping.setInvoice(invoice);
                            contractMappings.add(invoiceContractMapping);
                        }
                    }
                }
            }
        }
        return contractMappings;
    }

    private Set<InvoiceContractMapping> getInvoiceContractMappingsWithoutDO(HttpServletRequest request, Invoice invoice, String contractId) throws ServiceException {
        Set<InvoiceContractMapping> contractMappings = new HashSet<InvoiceContractMapping>();

        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(Contract.class.getName(), contractId);
        if (!rdresult.getEntityList().isEmpty()) {
            Contract contract = (Contract) rdresult.getEntityList().get(0);
            InvoiceContractMapping invoiceContractMapping = new InvoiceContractMapping();
            invoiceContractMapping.setCompany(invoice.getCompany());
            invoiceContractMapping.setContract(contract);
            invoiceContractMapping.setInvoice(invoice);
            contractMappings.add(invoiceContractMapping);

        }
        return contractMappings;
    }

    public ProductBatch saveAssetBatch(String batchJSON, String assetId, String assetName, String productId, boolean isFromSalesReturn, boolean isFromInvoice, HttpServletRequest request) throws JSONException, ParseException, SessionExpiredException, ServiceException, UnsupportedEncodingException {
        JSONArray jArr = new JSONArray(batchJSON);

        KwlReturnObject kmsg = null;
        String purchasebatchid = "";
        String purchaseserialid = "";
        String defaultLocation = "";
        String defaultWarehouse = "";
        boolean isBatch = false;
        boolean isserial = false;
        boolean isBatchForProduct = false;
        boolean isSerialForProduct = false;
        DateFormat df = authHandler.getDateOnlyFormat(request);
        String companyId = sessionHandlerImpl.getCompanyid(request);
        KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyId);
        CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
        isBatch = preferences.isIsBatchCompulsory();
        isserial = preferences.isIsSerialCompulsory();

        if (!StringUtil.isNullOrEmpty(productId)) {
            KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), productId);
            Product product = (Product) prodresult.getEntityList().get(0);
            isBatchForProduct = product.isIsBatchForProduct();
            isSerialForProduct = product.isIsSerialForProduct();
        }

        boolean isLeaseFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isLeaseFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isLeaseFixedAsset")) : false;
        for (int i = 0; i < 1; i++) {
            HashMap<String, Object> pdfTemplateMap = new HashMap<String, Object>();
            JSONObject jSONObject = new JSONObject(jArr.get(i).toString());
            pdfTemplateMap.put("companyid", companyId);
            pdfTemplateMap.put("name", StringUtil.DecodeText(jSONObject.optString("batch")));
            if (jSONObject.has("mfgdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("mfgdate"))) {
                pdfTemplateMap.put("mfgdate", df.parse(jSONObject.getString("mfgdate")));
            }
            if (jSONObject.has("expdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("expdate"))) {
                pdfTemplateMap.put("expdate", df.parse(jSONObject.getString("expdate")));
            }
            pdfTemplateMap.put("quantity", jSONObject.getString("quantity"));
            pdfTemplateMap.put("balance", jSONObject.getString("balance"));
            defaultLocation = accCommonTablesDAO.getDefaultLocation(companyId);
            defaultWarehouse = accCommonTablesDAO.getDefaultWarehouse(companyId);
            if (!StringUtil.isNullOrEmpty(defaultLocation) && isLeaseFixedAsset && !isFromSalesReturn && !isFromInvoice) {     //
                pdfTemplateMap.put("location", defaultLocation);
            } else {
                pdfTemplateMap.put("location", jSONObject.getString("location"));
            }
            if (!StringUtil.isNullOrEmpty(defaultWarehouse) && isLeaseFixedAsset && !isFromSalesReturn && !isFromInvoice) {     //
                pdfTemplateMap.put("warehouse", defaultWarehouse);
            } else {
                pdfTemplateMap.put("warehouse", jSONObject.getString("warehouse"));
            }

            pdfTemplateMap.put("product", productId);
            pdfTemplateMap.put("asset", assetId);   //stored the assetid of the 
            purchasebatchid = jSONObject.getString("purchasebatchid");
            pdfTemplateMap.put("isopening", false);
            pdfTemplateMap.put("transactiontype", "3");//This is DO Type Tranction 
            pdfTemplateMap.put("ispurchase", false);
            kmsg = accCommonTablesDAO.saveBatchForProduct(pdfTemplateMap);
        }
        ProductBatch productBatch = null;
        String productBatchId = "";
        if (kmsg.getEntityList().size() != 0) {
            productBatch = (ProductBatch) kmsg.getEntityList().get(0);
            productBatchId = productBatch.getId();
            HashMap<String, Object> pdfTemplateMap = new HashMap<String, Object>();
            pdfTemplateMap.put("purchasebatchid", purchasebatchid);
            pdfTemplateMap.put("salesbatchid", productBatch.getId());
            kmsg = accCommonTablesDAO.saveBatchMapping(pdfTemplateMap);
        }
        if (isSerialForProduct) {
            for (int i = 0; i < jArr.length(); i++) {
                HashMap<String, Object> pdfTemplateMap = new HashMap<String, Object>();
                JSONObject jSONObject = new JSONObject(jArr.get(i).toString());
                pdfTemplateMap.put("id", jSONObject.getString("serialnoid"));
                pdfTemplateMap.put("companyid", companyId);
                pdfTemplateMap.put("product", productId);
                pdfTemplateMap.put("name", StringUtil.DecodeText(jSONObject.optString("serialno")));
                if (jSONObject.has("expstart") && !StringUtil.isNullOrEmpty(jSONObject.getString("expstart"))) {
                    pdfTemplateMap.put("expfromdate", df.parse(jSONObject.getString("expstart")));
                }
                if (jSONObject.has("expend") && !StringUtil.isNullOrEmpty(jSONObject.getString("expend"))) {
                    pdfTemplateMap.put("exptodate", df.parse(jSONObject.getString("expend")));
                }
                pdfTemplateMap.put("batch", productBatchId);
                pdfTemplateMap.put("transactiontype", "3");//This is DO Type Tranction 
                pdfTemplateMap.put("ispurchase", false);
                kmsg = accCommonTablesDAO.saveSerialForBatch(pdfTemplateMap);

                if (kmsg.getEntityList().size() != 0) {
                    BatchSerial batchSerial = (BatchSerial) kmsg.getEntityList().get(0);
                    String salesSerial = batchSerial.getId();
                    pdfTemplateMap = new HashMap<String, Object>();
                    pdfTemplateMap.put("purchaseserialid", jSONObject.getString("purchaseserialid"));
                    pdfTemplateMap.put("salesserialid", salesSerial);
                    kmsg = accCommonTablesDAO.saveSalesPurchaseSerialMapping(pdfTemplateMap);
                }

            }
        }
        if (!StringUtil.isNullOrEmpty(defaultLocation) && isLeaseFixedAsset && !isFromSalesReturn && !isFromInvoice) {
            KwlReturnObject defaultLocationobj = accountingHandlerDAOobj.getObject(InventoryLocation.class.getName(), defaultLocation);
            InventoryLocation defaultLoc = (InventoryLocation) defaultLocationobj.getEntityList().get(0);
            String locationId = defaultLoc.getId();
            String locationname = defaultLoc.getName();
            if (!StringUtil.isNullOrEmpty(locationId) && !StringUtil.isNullOrEmpty(locationname)) {
                auditTrailObj.insertAuditLog(AuditAction.LOCATION_CHANGED, "User " + sessionHandlerImpl.getUserFullName(request) + " changed the the location for asset "
                        + assetName + " to " + locationname + " ", request, locationId);  //location
            }
        }
        if (!StringUtil.isNullOrEmpty(defaultWarehouse) && isLeaseFixedAsset && !isFromSalesReturn && !isFromInvoice) {
            KwlReturnObject defaultWarehouseobj = accountingHandlerDAOobj.getObject(InventoryWarehouse.class.getName(), defaultWarehouse);
            InventoryWarehouse defaultWar = (InventoryWarehouse) defaultWarehouseobj.getEntityList().get(0);
            String warehouseId = defaultWar.getId();
            String warehousename = defaultWar.getName();
            if (!StringUtil.isNullOrEmpty(warehouseId) && !StringUtil.isNullOrEmpty(warehousename) && !StringUtil.isNullOrEmpty(assetName)) {
                auditTrailObj.insertAuditLog(AuditAction.WAREHOUSE_CHANGED, "User " + sessionHandlerImpl.getUserFullName(request) + " changed the the warehouse for asset "
                        + assetName + " to " + warehousename + " ", request, warehouseId);  //warehouse changed
            }
        }
        return productBatch;
    }

//    public void saveAssetNewBatch(String batchJSON, String assetId, String assetName, String productId, boolean isFromSalesReturn, boolean isFromInvoice, String documentId, HttpServletRequest request) throws JSONException, ParseException, SessionExpiredException, ServiceException, UnsupportedEncodingException {
//        JSONArray jArr = new JSONArray(batchJSON);
//        String purchasebatchid = "";
//        KwlReturnObject kmsg = null;
//        double ActbatchQty = 1;
//        double batchQty = 0;
//        boolean isBatch = false;
//        boolean isserial = false;
//        boolean isLocationForProduct = false;
//        boolean isWarehouseForProduct = false;
//        boolean isBatchForProduct = false;
//        boolean isSerialForProduct = false;
//        boolean isConsignment = false;
//        DateFormat df = authHandler.getDateFormatter(request);
//        String companyid = sessionHandlerImpl.getCompanyid(request);
//        String transType = "0";
//        if (!StringUtil.isNullOrEmpty(request.getParameter("transType"))) {
//            transType = request.getParameter("transType");
//        }
//        if (!StringUtil.isNullOrEmpty(request.getParameter("isConsignment"))) {
//            isConsignment = Boolean.parseBoolean(request.getParameter("isConsignment"));
//        }
//        KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
//        CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
//        isBatch = preferences.isIsBatchCompulsory();
//        isserial = preferences.isIsSerialCompulsory();
//
//        if (!StringUtil.isNullOrEmpty(productId)) {
//            KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), productId);
//            Product product = (Product) prodresult.getEntityList().get(0);
//            isLocationForProduct = product.isIslocationforproduct();
//            isWarehouseForProduct = product.isIswarehouseforproduct();
//            isBatchForProduct = product.isIsBatchForProduct();
//            isSerialForProduct = product.isIsSerialForProduct();
//        }
//
//        //Save Batch detail for both  option for serial no and batch also as if batch option is off then also we are generating batch in backend
//        for (int i = 0; i < jArr.length(); i++) {
//            JSONObject jSONObject = new JSONObject(jArr.get(i).toString());
//            if (jSONObject.has("quantity") && !jSONObject.getString("quantity").equals("undefined")) {
//                ActbatchQty = jSONObject.getDouble("quantity");
//            }
//            if (batchQty == 0) {
//                batchQty = jSONObject.getDouble("quantity");
//            }
//            if ((isLocationForProduct || isWarehouseForProduct || isBatchForProduct) && (batchQty == ActbatchQty)) {
//                HashMap<String, Object> documentMap = new HashMap<String, Object>();
//                documentMap.put("quantity", jSONObject.getString("quantity"));
//
//                documentMap.put("documentid", documentId);
//                documentMap.put("transactiontype", transType);//This is DO Type Tranction  
//                if (jSONObject.has("mfgdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("mfgdate"))) {
//                    documentMap.put("mfgdate", AuthHandler.getDateFormatter(request).parse(jSONObject.getString("mfgdate")));
//                }
//                if (jSONObject.has("expdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("expdate"))) {
//                    documentMap.put("expdate", AuthHandler.getDateFormatter(request).parse(jSONObject.getString("expdate")));
//                }
//                documentMap.put("batchmapid", jSONObject.getString("purchasebatchid"));
//
//
//
//                if (!isBatchForProduct && !isSerialForProduct) {
//                    HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
//                    ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
//                    filter_names.add("company.companyID");
//                    filter_params.add(sessionHandlerImpl.getCompanyid(request));
//
//                    if (!StringUtil.isNullOrEmpty(jSONObject.getString("warehouse"))) {
//                        String warehouse = jSONObject.getString("warehouse");
//                        filter_names.add("warehouse.id");
//                        filter_params.add(warehouse);
//                    }
//                    if (!StringUtil.isNullOrEmpty(jSONObject.getString("location"))) {
//                        String location = jSONObject.getString("location");
//                        filter_names.add("location.id");
//                        filter_params.add(location);
//                    }
//
//
//                    // if (!StringUtil.isNullOrEmpty(request.getParameter("productid"))) {
//                    filter_names.add("product");
//                    filter_params.add(productId);
//                    // }
//                     if(!StringUtil.isNullOrEmpty(assetId)){
//                         filter_names.add("asset");
//                         filter_params.add(assetId);
//                     }
//
//                    filterRequestParams.put("filter_names", filter_names);
//                    filterRequestParams.put("filter_params", filter_params);
//                    filterRequestParams.put("order_by", order_by);
//                    filterRequestParams.put("order_type", order_type);
//                    KwlReturnObject result = accMasterItemsDAOobj.getNewBatches(filterRequestParams);
//                    List listResult = result.getEntityList();
//                    Iterator itrResult = listResult.iterator();
//                    Double quantityToDue = ActbatchQty;
//                    while (itrResult.hasNext()) {
//                        NewProductBatch newProductBatch = (NewProductBatch) itrResult.next();
//                        if (quantityToDue > 0) {
//                            double dueQty = newProductBatch.getQuantitydue();
//                            HashMap<String, Object> batchUpdateQtyMap = new HashMap<String, Object>();
//                            batchUpdateQtyMap.put("id", newProductBatch.getId());
//                            if (dueQty > 0) {
//                                if (quantityToDue > dueQty) {
//                                    batchUpdateQtyMap.put("qty", String.valueOf(-(dueQty)));
//                                    quantityToDue = quantityToDue - dueQty;
//
//                                } else {
//                                    batchUpdateQtyMap.put("qty", String.valueOf(-(quantityToDue)));
//                                    quantityToDue = quantityToDue - quantityToDue;
//
//                                }
//                                documentMap.put("batchmapid", newProductBatch.getId());
//                                accCommonTablesDAO.saveBatchAmountDue(batchUpdateQtyMap);
//
//                            }
//                        }
//
//                    }
//
//
//
//                } else {
//
//                    HashMap<String, Object> batchUpdateQtyMap = new HashMap<String, Object>();
//                    batchUpdateQtyMap.put("qty", String.valueOf(-(Double.parseDouble(jSONObject.getString("quantity")))));
//                    batchUpdateQtyMap.put("id", jSONObject.getString("purchasebatchid"));
//                    accCommonTablesDAO.saveBatchAmountDue(batchUpdateQtyMap);
//
//                }
//                accCommonTablesDAO.saveBatchDocumentMapping(documentMap);
//
//
//            }
//            batchQty--;
//
//
//            if (isSerialForProduct) {  //if serial no option is on then only save the serial no details 
//
//                HashMap<String, Object> documentMap = new HashMap<String, Object>();
//                documentMap.put("quantity", 1);
//                documentMap.put("serialmapid", jSONObject.getString("purchaseserialid"));
//                documentMap.put("documentid", documentId);
//                documentMap.put("transactiontype", transType);//This is GRN Type Tranction  
//                if (jSONObject.has("expstart") && !StringUtil.isNullOrEmpty(jSONObject.getString("expstart"))) {
//                    documentMap.put("expfromdate", AuthHandler.getDateFormatter(request).parse(jSONObject.getString("expstart")));
//                }
//                if (jSONObject.has("expend") && !StringUtil.isNullOrEmpty(jSONObject.getString("expend"))) {
//                    documentMap.put("exptodate", AuthHandler.getDateFormatter(request).parse(jSONObject.getString("expend")));
//                }
//
//
//                accCommonTablesDAO.saveSerialDocumentMapping(documentMap);
//                HashMap<String, Object> serialUpdateQtyMap = new HashMap<String, Object>();
//                serialUpdateQtyMap.put("qty", "-1");
//
//                serialUpdateQtyMap.put("id", jSONObject.getString("purchaseserialid"));
//                accCommonTablesDAO.saveSerialAmountDue(serialUpdateQtyMap);
//
//            } else {
//                batchQty = 0;
//            }
//        }
//
//    }
//    public ProductBatch saveAssetSRBatch(String batchJSON, String assetId, String productId, HttpServletRequest request) throws JSONException, ParseException, SessionExpiredException, ServiceException, UnsupportedEncodingException {
//        JSONArray jArr = new JSONArray(batchJSON);
//
//        KwlReturnObject kmsg = null;
//        String purchasebatchid = "";
//        String purchaseserialid = "";
//        boolean isBatch = false;
//        boolean isserial = false;
//        boolean isBatchForProduct = false;
//        boolean isSerialForProduct = false;
//        DateFormat df = authHandler.getDateFormatter(request);
//        String companyId = sessionHandlerImpl.getCompanyid(request);
//        KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyId);
//        CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
//        isBatch = preferences.isIsBatchCompulsory();
//        isserial = preferences.isIsSerialCompulsory();
//
//        if (!StringUtil.isNullOrEmpty(productId)) {
//            KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), productId);
//            Product product = (Product) prodresult.getEntityList().get(0);
//            isBatchForProduct = product.isIsBatchForProduct();
//            isSerialForProduct = product.isIsSerialForProduct();
//        }
//
//
//        for (int i = 0; i < 1; i++) {
//            HashMap<String, Object> pdfTemplateMap = new HashMap<String, Object>();
//            JSONObject jSONObject = new JSONObject(jArr.get(i).toString());
//            pdfTemplateMap.put("companyid", companyId);
//            pdfTemplateMap.put("name", StringUtil.DecodeText(jSONObject.optString("batch")));
//            if (jSONObject.has("mfgdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("mfgdate"))) {
//                pdfTemplateMap.put("mfgdate", df.parse(jSONObject.getString("mfgdate")));
//            }
//            if (jSONObject.has("expdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("expdate"))) {
//                pdfTemplateMap.put("expdate", df.parse(jSONObject.getString("expdate")));
//            }
//            pdfTemplateMap.put("quantity", jSONObject.getString("quantity"));
//            pdfTemplateMap.put("balance", jSONObject.getString("balance"));
//            pdfTemplateMap.put("location", jSONObject.getString("location"));
//            pdfTemplateMap.put("product", productId);
//            pdfTemplateMap.put("asset", assetId);   //stored the assetid of the 
//            pdfTemplateMap.put("warehouse", jSONObject.getString("warehouse"));
//            purchasebatchid = jSONObject.getString("purchasebatchid");
//            pdfTemplateMap.put("isopening", false);
//            pdfTemplateMap.put("transactiontype", "4");//This is DO Type Tranction 
//            pdfTemplateMap.put("ispurchase", false);
//            kmsg = accCommonTablesDAO.saveBatchForProduct(pdfTemplateMap);
//        }
//        ProductBatch productBatch = null;
//        String productBatchId = "";
//        if (kmsg.getEntityList().size() != 0) {
//            productBatch = (ProductBatch) kmsg.getEntityList().get(0);
//            productBatchId = productBatch.getId();
//            HashMap<String, Object> pdfTemplateMap = new HashMap<String, Object>();
//            pdfTemplateMap.put("batchtomap", purchasebatchid);
//            pdfTemplateMap.put("batchmap", productBatch.getId());
//            pdfTemplateMap.put("returntype", "1");
//            kmsg = accCommonTablesDAO.saveReturnBatchMapping(pdfTemplateMap);
//        }
//        if (isSerialForProduct) {
//            for (int i = 0; i < jArr.length(); i++) {
//                HashMap<String, Object> pdfTemplateMap = new HashMap<String, Object>();
//                JSONObject jSONObject = new JSONObject(jArr.get(i).toString());
//                pdfTemplateMap.put("id", jSONObject.getString("serialnoid"));
//                pdfTemplateMap.put("companyid", companyId);
//                pdfTemplateMap.put("product", productId);
//                pdfTemplateMap.put("name", StringUtil.DecodeText(jSONObject.optString("serialno")));
//                if (jSONObject.has("expstart") && !StringUtil.isNullOrEmpty(jSONObject.getString("expstart"))) {
//                    pdfTemplateMap.put("expfromdate", df.parse(jSONObject.getString("expstart")));
//                }
//                if (jSONObject.has("expend") && !StringUtil.isNullOrEmpty(jSONObject.getString("expend"))) {
//                    pdfTemplateMap.put("exptodate", df.parse(jSONObject.getString("expend")));
//                }
//                pdfTemplateMap.put("batch", productBatchId);
//                pdfTemplateMap.put("transactiontype", "4");//This is DO Type Tranction 
//                pdfTemplateMap.put("ispurchase", false);
//                kmsg = accCommonTablesDAO.saveSerialForBatch(pdfTemplateMap);
//
//                if (kmsg.getEntityList().size() != 0) {
//                    BatchSerial batchSerial = (BatchSerial) kmsg.getEntityList().get(0);
//                    String salesSerial = batchSerial.getId();
//                    pdfTemplateMap = new HashMap<String, Object>();
//                    pdfTemplateMap.put("maptoserialid", jSONObject.getString("purchaseserialid"));
//                    pdfTemplateMap.put("mapserialid", salesSerial);
//                    pdfTemplateMap.put("returntype", "1");
//                    kmsg = accCommonTablesDAO.saveReturnSerialMapping(pdfTemplateMap);
//                    accCommonTablesDAO.deleteSalesPurchaseSerialMapping(jSONObject.getString("purchaseserialid"));
//                }
//
//            }
//        }
//        return productBatch;
//    }
    public ProductBatch saveDOBatch(String batchJSON, Inventory inventory, HttpServletRequest request) throws JSONException, ParseException, SessionExpiredException, ServiceException, UnsupportedEncodingException {
        JSONArray jArr = new JSONArray(batchJSON);

        KwlReturnObject kmsg = null;
        String purchasebatchid = "";
        String purchaseserialid = "";
        double quantity = 0;
        boolean isBatch = false;
        boolean isserial = false;
        boolean isBatchForProduct = false;
        boolean isSerialForProduct = false;
        DateFormat df = authHandler.getDateOnlyFormat(request);
        String companyid = sessionHandlerImpl.getCompanyid(request);
        KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
        CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
        isBatch = preferences.isIsBatchCompulsory();
        isserial = preferences.isIsSerialCompulsory();

        if (!StringUtil.isNullOrEmpty(inventory.getProduct().getID())) {

            KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), inventory.getProduct().getID());
            Product product = (Product) prodresult.getEntityList().get(0);
            isBatchForProduct = product.isIsBatchForProduct();
            isSerialForProduct = product.isIsSerialForProduct();
        }

        for (int i = 0; i < 1; i++) {
            HashMap<String, Object> pdfTemplateMap = new HashMap<String, Object>();
            JSONObject jSONObject = new JSONObject(jArr.get(i).toString());
            pdfTemplateMap.put("companyid", inventory.getCompany().getCompanyID());
            //pdfTemplateMap.put("name", jSONObject.getString("batch"));
            pdfTemplateMap.put("name", StringUtil.DecodeText(jSONObject.optString("batch")));
            if (jSONObject.has("mfgdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("mfgdate"))) {
                pdfTemplateMap.put("mfgdate", df.parse(jSONObject.getString("mfgdate")));
            }
            if (jSONObject.has("expdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("expdate"))) {
                pdfTemplateMap.put("expdate", df.parse(jSONObject.getString("expdate")));
            }
            quantity = Double.parseDouble(jSONObject.getString("quantity"));
            pdfTemplateMap.put("quantity", jSONObject.getString("quantity"));
            pdfTemplateMap.put("balance", jSONObject.getString("balance"));
            pdfTemplateMap.put("location", jSONObject.getString("location"));
            pdfTemplateMap.put("product", inventory.getProduct().getID());
            pdfTemplateMap.put("warehouse", jSONObject.getString("warehouse"));
            purchasebatchid = jSONObject.getString("purchasebatchid");
            pdfTemplateMap.put("isopening", false);
            pdfTemplateMap.put("transactiontype", "3");//This is DO Type Tranction 
            pdfTemplateMap.put("ispurchase", false);
            kmsg = accCommonTablesDAO.saveBatchForProduct(pdfTemplateMap);
        }
        ProductBatch productBatch = null;
        String productBatchId = "";
        if (kmsg.getEntityList().size() != 0) {
            productBatch = (ProductBatch) kmsg.getEntityList().get(0);
            productBatchId = productBatch.getId();
            HashMap<String, Object> pdfTemplateMap = new HashMap<String, Object>();
            pdfTemplateMap.put("purchasebatchid", purchasebatchid);
            pdfTemplateMap.put("salesbatchid", productBatch.getId());
            pdfTemplateMap.put("quantity", quantity);
            kmsg = accCommonTablesDAO.saveBatchMapping(pdfTemplateMap);
        }
        if (isSerialForProduct) {  //if serial no option is on then only save the serial no details 
            for (int i = 0; i < jArr.length(); i++) {
                HashMap<String, Object> pdfTemplateMap = new HashMap<String, Object>();
                JSONObject jSONObject = new JSONObject(jArr.get(i).toString());
                pdfTemplateMap.put("id", jSONObject.getString("serialnoid"));
                pdfTemplateMap.put("companyid", inventory.getCompany().getCompanyID());
                pdfTemplateMap.put("product", inventory.getProduct().getID());
                // pdfTemplateMap.put("name", jSONObject.getString("serialno"));
                pdfTemplateMap.put("name", StringUtil.DecodeText(jSONObject.optString("serialno")));
                if (jSONObject.has("expstart") && !StringUtil.isNullOrEmpty(jSONObject.getString("expstart"))) {
                    pdfTemplateMap.put("expfromdate", df.parse(jSONObject.getString("expstart")));
                }
                if (jSONObject.has("expend") && !StringUtil.isNullOrEmpty(jSONObject.getString("expend"))) {
                    pdfTemplateMap.put("exptodate", df.parse(jSONObject.getString("expend")));
                }
                pdfTemplateMap.put("batch", productBatchId);
                pdfTemplateMap.put("transactiontype", "3");//This is DO Type Tranction 
                pdfTemplateMap.put("ispurchase", false);
                kmsg = accCommonTablesDAO.saveSerialForBatch(pdfTemplateMap);

                if (kmsg.getEntityList().size() != 0) {
                    BatchSerial batchSerial = (BatchSerial) kmsg.getEntityList().get(0);
                    String salesSerial = batchSerial.getId();
                    pdfTemplateMap = new HashMap<String, Object>();
                    pdfTemplateMap.put("purchaseserialid", jSONObject.getString("purchaseserialid"));
                    pdfTemplateMap.put("salesserialid", salesSerial);
                    kmsg = accCommonTablesDAO.saveSalesPurchaseSerialMapping(pdfTemplateMap);
                }
            }
        }
        return productBatch;
    }

    public void saveDONewBatch(String batchJSON, Inventory inventory, HttpServletRequest request, DeliveryOrderDetail deliveryOrderDetail, List<StockMovement> stockMovementsList, boolean isLockedinSo, boolean isbatchlockedinSO, boolean isSeriallockedinSO, String replacebatchdetails, List<InterStoreTransferRequest> interStoreTransferList) throws JSONException, ParseException, SessionExpiredException, ServiceException, UnsupportedEncodingException, AccountingException {
        accInvoiceModuleService.saveDONewBatch(batchJSON, inventory, request, deliveryOrderDetail, stockMovementsList, isLockedinSo, isbatchlockedinSO, isSeriallockedinSO, replacebatchdetails, interStoreTransferList);

    }

    // this function is used to make in the entry to customer warehouse means stock is added to the customer warehouse

    public void saveConsignmentNewBatch(String batchJSON, Inventory inventory, HttpServletRequest request, String documentId) throws JSONException, ParseException, SessionExpiredException, ServiceException, UnsupportedEncodingException, AccountingException {
        accInvoiceModuleService.saveConsignmentNewBatch(batchJSON, inventory, request, documentId);
    }

    public void saveWorkOrderNewBatch(String batchJSON, Inventory inventory, HttpServletRequest request, AssetMaintenanceWorkOrderDetail deliveryOrderDetail, List<StockMovement> stockMovementsList, boolean isLockedinSo) throws JSONException, ParseException, SessionExpiredException, ServiceException, UnsupportedEncodingException, AccountingException {
        JSONArray jArr = new JSONArray(batchJSON);
        String purchasebatchid = "";
        KwlReturnObject kmsg = null;
        double ActbatchQty = 1;
        double batchQty = 0;
        boolean isBatch = false;
        boolean isserial = false;
        boolean isLocationForProduct = false;
        boolean isWarehouseForProduct = false;
        boolean isBatchForProduct = false;
        boolean isSerialForProduct = false;
        boolean isRowForProduct = false;
        boolean isRackForProduct = false;
        boolean isBinForProduct = false;
        boolean isConsignment = false;
        DateFormat df = authHandler.getDateOnlyFormat(request);
        String companyid = sessionHandlerImpl.getCompanyid(request);
        if (!StringUtil.isNullOrEmpty(request.getParameter("isConsignment"))) {
            isConsignment = Boolean.parseBoolean(request.getParameter("isConsignment"));
        }
        KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
        CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
        isBatch = preferences.isIsBatchCompulsory();
        isserial = preferences.isIsSerialCompulsory();

        if (!StringUtil.isNullOrEmpty(inventory.getProduct().getID())) {
            KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), inventory.getProduct().getID());
            Product product = (Product) prodresult.getEntityList().get(0);
            isLocationForProduct = product.isIslocationforproduct();
            isWarehouseForProduct = product.isIswarehouseforproduct();
            isBatchForProduct = product.isIsBatchForProduct();
            isSerialForProduct = product.isIsSerialForProduct();
            isRowForProduct = product.isIsrowforproduct();
            isRackForProduct = product.isIsrackforproduct();
            isBinForProduct = product.isIsbinforproduct();
        }

//        //Save Batch detail for both  option for serial no and batch also as if batch option is off then also we are generating batch in backend
//        Set<AttachedSerial> attachedSerialsList=new HashSet<AttachedSerial> ();
//        Set<SMAttachedBatch>  attachedBatchsList=new HashSet<SMAttachedBatch>();
//        SMAttachedBatch attachedBatch=null;
//        StockMovement stockMovement=null;
        for (int i = 0; i < jArr.length(); i++) {

            JSONObject jSONObject = new JSONObject(jArr.get(i).toString());
            if (jSONObject.has("quantity") && !jSONObject.getString("quantity").equals("undefined") && !jSONObject.getString("quantity").isEmpty()) {
                ActbatchQty = jSONObject.getDouble("quantity");
            }
            if (batchQty == 0) {
                batchQty = jSONObject.getDouble("quantity");
//                attachedSerialsList=new HashSet<AttachedSerial> ();
//                attachedBatchsList=new HashSet<SMAttachedBatch>();
//                KwlReturnObject warehouseObj = accountingHandlerDAOobj.getObject(Store.class.getName(),jSONObject.getString("warehouse"));
//                Store store = (Store) warehouseObj.getEntityList().get(0);            
//                KwlReturnObject locationUpdate = accountingHandlerDAOobj.getObject(Location.class.getName(), jSONObject.getString("location"));
//                Location locationObj = (Location) locationUpdate.getEntityList().get(0);                   
//                stockMovement=new StockMovement();
//                stockMovement.setCompany(inventory.getCompany());
//                stockMovement.setProduct(inventory.getProduct());
//                stockMovement.setStockUoM(inventory.getProduct().getUnitOfMeasure());
//                stockMovement.setPricePerUnit(deliveryOrderDetail.getRate());
//                stockMovement.setQuantity(jSONObject.optDouble("quantity",0.0));
//                stockMovement.setTransactionDate(deliveryOrderDetail.getDeliveryOrder().getOrderDate());
//                stockMovement.setModuleRefId(deliveryOrderDetail.getDeliveryOrder().getID());
//                stockMovement.setCustomer(deliveryOrderDetail.getDeliveryOrder().getCustomer());
//                stockMovement.setCostCenter(deliveryOrderDetail.getDeliveryOrder().getCostcenter());
//                stockMovement.setTransactionNo(deliveryOrderDetail.getDeliveryOrder().getDeliveryOrderNumber());
//                stockMovement.setTransactionModule(stockMovement.getTransactionModule().ERP_DO);
//                stockMovement.setTransactionType(TransactionType.OUT);
//                if(store!=null){
//                    stockMovement.setStore(store);
//                }
//                if(locationObj!=null){
//                    stockMovement.setLocation(locationObj);
//                }
//                if(!isBatchForProduct && !isSerialForProduct){
//                    stockMovementsList.add(stockMovement);
//                }
            }
            if ((isLocationForProduct || isWarehouseForProduct || isBatchForProduct || isRowForProduct || isRackForProduct || isBinForProduct) && (batchQty == ActbatchQty)) {
                HashMap<String, Object> documentMap = new HashMap<String, Object>();
                documentMap.put("quantity", jSONObject.getString("quantity"));

                documentMap.put("documentid", deliveryOrderDetail.getID());
                documentMap.put("transactiontype", "27");//This is DO Type Tranction  
                if (jSONObject.has("mfgdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("mfgdate"))) {
                    documentMap.put("mfgdate", authHandler.getDateOnlyFormat(request).parse(jSONObject.getString("mfgdate")));
                }
                if (jSONObject.has("expdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("expdate"))) {
                    documentMap.put("expdate", authHandler.getDateOnlyFormat(request).parse(jSONObject.getString("expdate")));
                }
                documentMap.put("batchmapid", jSONObject.getString("purchasebatchid"));

                if (!isBatchForProduct && !isSerialForProduct) {
                    HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                    ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                    filter_names.add("company.companyID");
                    filter_params.add(sessionHandlerImpl.getCompanyid(request));

                    if (!StringUtil.isNullOrEmpty(jSONObject.getString("warehouse"))) {
                        String warehouse = jSONObject.getString("warehouse");
                        filter_names.add("warehouse.id");
                        filter_params.add(warehouse);
                    }
                    if (!StringUtil.isNullOrEmpty(jSONObject.getString("location"))) {
                        String location = jSONObject.getString("location");
                        filter_names.add("location.id");
                        filter_params.add(location);
                    }
                    if (!StringUtil.isNullOrEmpty(jSONObject.getString("row"))) {
                        String row = jSONObject.getString("row");
                        filter_names.add("row.id");
                        filter_params.add(row);
                    }
                    if (!StringUtil.isNullOrEmpty(jSONObject.getString("rack"))) {
                        String rack = jSONObject.getString("rack");
                        filter_names.add("rack.id");
                        filter_params.add(rack);
                    }
                    if (!StringUtil.isNullOrEmpty(jSONObject.getString("bin"))) {
                        String bin = jSONObject.getString("bin");
                        filter_names.add("bin.id");
                        filter_params.add(bin);
                    }

                    // if (!StringUtil.isNullOrEmpty(request.getParameter("productid"))) {
                    filter_names.add("product");
                    filter_params.add(inventory.getProduct().getID());
                    // }

                    filterRequestParams.put("filter_names", filter_names);
                    filterRequestParams.put("filter_params", filter_params);
                    filterRequestParams.put("order_by", order_by);
                    filterRequestParams.put("order_type", order_type);
                    KwlReturnObject result = accMasterItemsDAOobj.getNewBatches(filterRequestParams, false, false);
                    List listResult = result.getEntityList();
                    Iterator itrResult = listResult.iterator();
                    Double quantityToDue = ActbatchQty;
                    while (itrResult.hasNext()) {
                        NewProductBatch newProductBatch = (NewProductBatch) itrResult.next();
                        if (quantityToDue > 0) {
                            double dueQty = newProductBatch.getQuantitydue();
                            HashMap<String, Object> batchUpdateQtyMap = new HashMap<String, Object>();
                            batchUpdateQtyMap.put("id", newProductBatch.getId());
                            if (dueQty > 0) {
                                if (quantityToDue > dueQty) {
                                    batchUpdateQtyMap.put("qty", String.valueOf(-(dueQty)));
                                    if (isLockedinSo) {
                                        batchUpdateQtyMap.put("lockquantity", String.valueOf(-(dueQty)));
                                    }
                                    if (isConsignment) {
                                        batchUpdateQtyMap.put("consignquantity", String.valueOf(dueQty));
                                    }
                                    quantityToDue = quantityToDue - dueQty;

                                } else {
                                    batchUpdateQtyMap.put("qty", String.valueOf(-(quantityToDue)));
                                    if (isLockedinSo) {
                                        batchUpdateQtyMap.put("lockquantity", String.valueOf(-(quantityToDue)));
                                    }
                                    if (isConsignment) {
                                        batchUpdateQtyMap.put("consignquantity", String.valueOf(quantityToDue));
                                    }
                                    quantityToDue = quantityToDue - quantityToDue;

                                }
                                documentMap.put("batchmapid", newProductBatch.getId());
                                accCommonTablesDAO.saveBatchAmountDue(batchUpdateQtyMap);

                            }
                        }

                    }

                } else {

                    HashMap<String, Object> batchUpdateQtyMap = new HashMap<String, Object>();
                    batchUpdateQtyMap.put("qty", String.valueOf(-(Double.parseDouble(jSONObject.getString("quantity")))));
                    if (isLockedinSo) {
                        batchUpdateQtyMap.put("lockquantity", String.valueOf(-(Double.parseDouble(jSONObject.getString("quantity")))));
                    }
                    if (isConsignment) {
                        batchUpdateQtyMap.put("consignquantity", String.valueOf(Double.parseDouble(jSONObject.getString("quantity"))));
                    }
                    batchUpdateQtyMap.put("id", jSONObject.getString("purchasebatchid"));
                    accCommonTablesDAO.saveBatchAmountDue(batchUpdateQtyMap);
                    //Code to Send Batch
//                    KwlReturnObject batchObj = accountingHandlerDAOobj.getObject(NewProductBatch.class.getName(), jSONObject.getString("purchasebatchid"));
//                    NewProductBatch newProductBatch1 = (NewProductBatch) batchObj.getEntityList().get(0);
//                    attachedBatch = new SMAttachedBatch();
//                    attachedBatch.setQuantity(Double.parseDouble(jSONObject.getString("quantity")));
//                    attachedBatch.setBatch(newProductBatch1);
//                    attachedBatch.setCompany(inventory.getCompany());
//                    attachedBatch.setStockMovement(stockMovement);
//                    attachedBatchsList.add(attachedBatch);

                }
                accCommonTablesDAO.saveBatchDocumentMapping(documentMap);

            }
            batchQty--;
//            if(!isSerialForProduct && isBatchForProduct){
//                    stockMovement.setAttachedBaches(attachedBatchsList);
//                    stockMovementsList.add(stockMovement);                  
//            }

            if (isSerialForProduct) {  //if serial no option is on then only save the serial no details

                HashMap<String, Object> documentMap = new HashMap<String, Object>();
                documentMap.put("quantity", 1);
                documentMap.put("serialmapid", jSONObject.getString("purchaseserialid"));
                documentMap.put("documentid", deliveryOrderDetail.getID());
                documentMap.put("transactiontype", "27");//This is GRN Type Tranction 
                if (jSONObject.has("expstart") && !StringUtil.isNullOrEmpty(jSONObject.getString("expstart"))) {
                    documentMap.put("expfromdate", authHandler.getDateOnlyFormat(request).parse(jSONObject.getString("expstart")));
                }
                if (jSONObject.has("expend") && !StringUtil.isNullOrEmpty(jSONObject.getString("expend"))) {
                    documentMap.put("exptodate", authHandler.getDateOnlyFormat(request).parse(jSONObject.getString("expend")));
                }

                accCommonTablesDAO.saveSerialDocumentMapping(documentMap);
                HashMap<String, Object> serialUpdateQtyMap = new HashMap<String, Object>();
                serialUpdateQtyMap.put("qty", "-1");
                if (isLockedinSo) {
                    serialUpdateQtyMap.put("lockquantity", "-1");
                }
                if (isConsignment) {
                    serialUpdateQtyMap.put("consignquantity", "1");
                }
                serialUpdateQtyMap.put("id", jSONObject.getString("purchaseserialid"));
                accCommonTablesDAO.saveSerialAmountDue(serialUpdateQtyMap);

                //Code to Send Serial Numbers to Inventory
//                accCommonTablesDAO.saveSerialDocumentMapping(documentMap);
//                KwlReturnObject serialObj = accountingHandlerDAOobj.getObject(NewBatchSerial.class.getName(), jSONObject.getString("purchaseserialid"));
//                NewBatchSerial newBatchSerial = (NewBatchSerial) serialObj.getEntityList().get(0);
//                if(newBatchSerial!=null){
//                    AttachedSerial attachedSerial=new AttachedSerial(newBatchSerial, attachedBatch);
//                    attachedSerialsList.add(attachedSerial);
//                }
//                
//                if(batchQty==0){     
//                   if(attachedBatch!=null){
//                        attachedBatch.setAttachedSerials(attachedSerialsList);
//                        stockMovement.setAttachedBaches(attachedBatchsList);
//                    }else{
//                        attachedBatch = new SMAttachedBatch();
//                        attachedBatch.setCompany(inventory.getCompany());
//                        attachedBatch.setStockMovement(stockMovement);
//                        attachedBatch.setAttachedSerials(attachedSerialsList);
//                        attachedBatchsList.add(attachedBatch);
//                        stockMovement.setAttachedBaches(attachedBatchsList);
//                    }
//                    stockMovementsList.add(stockMovement);             
//                }
            } else {
                batchQty = 0;
            }
        }

    }

//    public ModelAndView saveSalesReturn(HttpServletRequest request, HttpServletResponse response) {
//        JSONObject jobj = new JSONObject();
//        String msg = "";
//        String billno = "";
//        boolean issuccess = false;
//        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
//        def.setName("SO_Tx");
//        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
//        TransactionStatus status = txnManager.getTransaction(def);
//        try {
//            SalesReturn salesReturn = saveSalesReturn(request);
//            billno = salesReturn.getSalesReturnNumber();
//            issuccess = true;
//            boolean isConsignment = request.getParameter("isConsignment") != null ? Boolean.parseBoolean(request.getParameter("isConsignment")) : false;
//            if (isConsignment) {
//                msg = messageSource.getMessage("acc.Consignment.SalesReturnhasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request)) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + " <b>" + billno + "</b>";
//            } else {
//                msg = messageSource.getMessage("acc.field.SalesReturnhasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request)) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + " <b>" + billno + "</b>";
//            }
//            boolean isEdit = StringUtil.isNullOrEmpty(request.getParameter("isEdit")) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
//            boolean isCopy = StringUtil.isNullOrEmpty(request.getParameter("copyInv")) ? false : Boolean.parseBoolean(request.getParameter("copyInv"));
//            String action = "added a new";
//            if (isEdit == true && isCopy == false) {
//                action = "updated";
//            }
//            auditTrailObj.insertAuditLog(AuditAction.SALES_RETURN, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + " sales return " + salesReturn.getSalesReturnNumber(), request, salesReturn.getID());
//
//            txnManager.commit(status);
//        } catch (Exception ex) {
//            txnManager.rollback(status);
//            msg = "" + ex.getMessage();
//            if (ex.getMessage() == null) {
//                msg = ex.getCause().getMessage();
//            }
//            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            try {
//                jobj.put("success", issuccess);
//                jobj.put("msg", msg);
//
//            } catch (JSONException ex) {
//                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        return new ModelAndView("jsonView", "model", jobj.toString());
//    }
//    public SalesReturn saveSalesReturn(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException, JSONException, UnsupportedEncodingException {
//        SalesReturn salesReturn = null;
//        try {
//            String companyid = sessionHandlerImpl.getCompanyid(request);
//            String currencyid = (request.getParameter("currencyid") == null ? sessionHandlerImpl.getCurrencyID(request) : request.getParameter("currencyid"));
//            String entryNumber = request.getParameter("number");
//            String costCenterId = request.getParameter("costcenter");
//
//            String srid = request.getParameter("srid");
//            String isfavourite = request.getParameter("isfavourite");
//            String sequenceformat = request.getParameter("sequenceformat");
//            boolean isLeaseFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isLeaseFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isLeaseFixedAsset")) : false;
//            boolean isConsignment = (!StringUtil.isNullOrEmpty(request.getParameter("isConsignment"))) ? Boolean.parseBoolean(request.getParameter("isConsignment")) : false;
//            long createdon = System.currentTimeMillis();
//            String createdby = sessionHandlerImpl.getUserid(request);
//            String modifiedby = sessionHandlerImpl.getUserid(request);
//            long updatedon = createdon;
//            String custWarehouse = request.getParameter("custWarehouse");
//            String nextAutoNumber = "";
//            String auditMsg = "";
//            HashMap<String, Object> doDataMap = new HashMap<String, Object>();
//
//
//            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
//            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
//
//            synchronized (this) {
//                if (!StringUtil.isNullOrEmpty(srid)) {//Edit case
//                    KwlReturnObject socnt = accInvoiceDAOobj.getSalesReturnCountForEdit(entryNumber, companyid, srid);
//                    if (socnt.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
//                        throw new AccountingException("Sales Return number '" + entryNumber + "' already exists.");
//                    }
//
//
//                    // in case of edition of Lease Sales Return you need handle assetdetails table data, and need to 
//                    // delete entry from assetdetailsinvdetailmapping table
//                    if (isLeaseFixedAsset) {
//                        HashMap<String, Object> deleteParams = new HashMap<String, Object>();
//                        deleteParams.put("srid", srid);
//                        deleteParams.put("companyid", companyid);
//                        accInvoiceDAOobj.deleteAssetDetailsLinkedWithSalesReturn(deleteParams);
//                    }
//                    HashMap<String, Object> requestParamsForDeleteBatch = new HashMap<String, Object>();
//                    requestParamsForDeleteBatch.put("srid", srid);
//                    requestParamsForDeleteBatch.put("companyid", companyid);
//
//                    if (!sequenceformat.equals("NA")) {
//                        nextAutoNumber = entryNumber;
//                        doDataMap.put("id", srid);
//
//                        KwlReturnObject result = accInvoiceDAOobj.getSalesReturnInventory(srid);
//                        KwlReturnObject resultBatch = accInvoiceDAOobj.getSalesReturnBatches(srid, companyid);
//
//                        if (preferences.isInventoryAccountingIntegration() && preferences.isWithInvUpdate()) {
//                        JSONArray productArray = new JSONArray();
//                            String action = "17";
//                            boolean isDirectUpdateInvFlag = false;
//                            if (preferences.isUpdateInvLevel()) {
//                                isDirectUpdateInvFlag = true;
//                                action = "19";//Direct Inventory Update action
//                            }
//                            KwlReturnObject resultmap=accInvoiceDAOobj.getSalesReturnsBatchDetails(requestParamsForDeleteBatch);
//                            List list = resultmap.getEntityList();
//                            Iterator itr4 = list.iterator();
//                            while (itr4.hasNext()) {                        
//                            LocationBatchDocumentMapping locationBDM=(LocationBatchDocumentMapping) itr4.next();
//                            KwlReturnObject res = accountingHandlerDAOobj.getObject(SalesReturnDetail.class.getName(), locationBDM.getDocumentid());
//                            SalesReturnDetail salesReturnDetail = (SalesReturnDetail) res.getEntityList().get(0);
//                            if (locationBDM != null && salesReturnDetail !=null && locationBDM.getBatchmapid().getWarehouse()!=null && locationBDM.getBatchmapid().getLocation() !=null) {                 
//                                JSONObject productObject=new JSONObject();
//                                productObject.put("itemUomId", salesReturnDetail.getProduct().getUnitOfMeasure().getID());
//                                productObject.put("itemBaseUomRate", 1);
//                                productObject.put("itemQuantity", locationBDM.getQuantity()* (-1));
//                                productObject.put("quantity", locationBDM.getQuantity()* (-1));
//                                //productObject.put("itemQuantity", jobj.getDouble("dquantity") * (-1));                            
//                                productObject.put("itemCode", salesReturnDetail.getProduct().getProductid());
//                                productObject.put("storeid", locationBDM.getBatchmapid().getWarehouse().getId());
//                                productObject.put("locationid", locationBDM.getBatchmapid().getLocation().getId());
//                                productObject.put("rate", salesReturnDetail.getRate()/salesReturnDetail.getBaseuomquantity());             
//                                productArray.put(productObject);
//                            }
//
//
//                        }
//
//                            KwlReturnObject res = accountingHandlerDAOobj.getObject(SalesReturn.class.getName(), srid);
//                             salesReturn = (SalesReturn) res.getEntityList().get(0);
//
//                            if (productArray.length() > 0) {
//
//                                String sendDateFormat = "yyyy-MM-dd";
//                                DateFormat dateformat = new SimpleDateFormat(sendDateFormat);
//                                Date date = salesReturn.getOrderDate();
//                                String stringDate = dateformat.format(date);
//
//                                JSONObject jSONObject = new JSONObject();
//                                jSONObject.put("deliveryDate", stringDate);
//                                jSONObject.put("dateFormat", sendDateFormat);
//                                jSONObject.put("details", productArray);
//                                jSONObject.put("orderNumber", salesReturn.getSalesReturnNumber());
//                                jSONObject.put("companyId", companyid);
//                                jSONObject.put("selling", true);
//
//                                String url = this.getServletContext().getInitParameter("inventoryURL");
//                                CommonFnController cfc = new CommonFnController();
//                                cfc.updateInventoryLevel(request, jSONObject, url, action);
//                            }
//                        }
//                        accInvoiceDAOobj.deleteSalesReturnsBatchSerialDetails(requestParamsForDeleteBatch); //dlete serial no and mapping
//
//                        accInvoiceDAOobj.deleteSalesReturnDetails(srid, companyid);
//                        List list = result.getEntityList();
//                        Iterator itr = list.iterator();
//                        while (itr.hasNext()) {
//                            String inventoryid = (String) itr.next();
//                            accProductObj.deleteInventory(inventoryid, companyid);
//                        }
//                        List listBatch = resultBatch.getEntityList();
//                        Iterator itrBatch = listBatch.iterator();
//                        while (itrBatch.hasNext()) {
//                            String batchid = (String) itrBatch.next();
//                            accCommonTablesDAO.deleteBatches(batchid, companyid);
//                        }
//                    }
//                    doDataMap.put("id", srid);
//
//                    KwlReturnObject result = accInvoiceDAOobj.getSalesReturnInventory(srid);
//                    accInvoiceDAOobj.deleteSalesReturnsBatchSerialDetails(requestParamsForDeleteBatch);
//                    accInvoiceDAOobj.deleteSalesReturnDetails(srid, companyid);
//                    List list = result.getEntityList();
//                    Iterator itr = list.iterator();
//                    while (itr.hasNext()) {
//                        String inventoryid = (String) itr.next();
//                        accProductObj.deleteInventory(inventoryid, companyid);
//                    }
//                    auditMsg = " updated Sales Return ";
//                } else {  // Creae new case
//                    KwlReturnObject socnt = accInvoiceDAOobj.getSalesReturnCount(entryNumber, companyid);
//                    if (socnt.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
//                        throw new AccountingException("Sales Return number '" + entryNumber + "' already exists.");
//                    }
//                    if (!sequenceformat.equals("NA")) {
//                        boolean seqformat_oldflag = StringUtil.getBoolean(request.getParameter("seqformat_oldflag"));
//                        String nextAutoNoInt = "";
//                        if (seqformat_oldflag) {
//                            nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_SALESRETURN, sequenceformat);
//                        } else {
//                            String[] nextAutoNoTemp = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_SALESRETURN, sequenceformat, seqformat_oldflag);
//                            nextAutoNumber = nextAutoNoTemp[0];
//                            nextAutoNoInt = nextAutoNoTemp[1];
//                            doDataMap.put(Constants.SEQFORMAT, sequenceformat);
//                            doDataMap.put(Constants.SEQNUMBER, nextAutoNoInt);
//                        }
//                        entryNumber = nextAutoNumber;
//                    }
//                    auditMsg = " added new Sales Return ";
//                }
//                if (sequenceformat.equals("NA")) {//In case of NA checks wheather this number can also be generated by a sequence format or not 
//                    List list = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Sales_Return_ModuleId, entryNumber, companyid);
//                    if (!list.isEmpty()) {
//                        boolean isvalidEntryNumber = (Boolean) list.get(0);
//                        String formatName = (String) list.get(1);
//                        if (!isvalidEntryNumber) {
//                            throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, RequestContextUtils.getLocale(request)) + " <b>" + entryNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, RequestContextUtils.getLocale(request)) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null, RequestContextUtils.getLocale(request)) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null, RequestContextUtils.getLocale(request)));
//                        }
//                    }
//                }
//            }
//
//            DateFormat df = authHandler.getDateFormatter(request);
//            doDataMap.put("entrynumber", entryNumber);
//            doDataMap.put("autogenerated", nextAutoNumber.equals(entryNumber));
//            doDataMap.put("memo", request.getParameter("memo"));
//            doDataMap.put("isLeaseFixedAsset", isLeaseFixedAsset);
//            doDataMap.put("posttext", request.getParameter("posttext") == null ? "" : request.getParameter("posttext"));
//            doDataMap.put("customerid", request.getParameter("customer"));
//            if (request.getParameter("shipdate") != null && !StringUtil.isNullOrEmpty(request.getParameter("shipdate"))) {
//                doDataMap.put("shipdate", df.parse(request.getParameter("shipdate")));
//            }
//            doDataMap.put("shipvia", request.getParameter("shipvia"));
//            doDataMap.put("fob", request.getParameter("fob"));
//            doDataMap.put("orderdate", df.parse(request.getParameter("billdate")));
//
//            doDataMap.put("isfavourite", isfavourite);
//            if (!StringUtil.isNullOrEmpty(costCenterId)) {
//                doDataMap.put("costCenterId", costCenterId);
//            }
//            doDataMap.put("companyid", companyid);
//            doDataMap.put("currencyid", currencyid);
//
//            doDataMap.put("isConsignment", isConsignment);
//            if (!StringUtil.isNullOrEmpty(custWarehouse)) {
//                doDataMap.put("custWarehouse", custWarehouse);
//            }
//
//            if (request.getParameter("contractid") == null) {
//                String linkMode = request.getParameter("fromLinkCombo");
//                String linkNumber = request.getParameter("linkNumber");
//                Contract contract = null;
//                if (!StringUtil.isNullOrEmpty(linkMode)) {
//                    if (linkMode.equalsIgnoreCase("Delivery Order")) {
//                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(DeliveryOrder.class.getName(), linkNumber);
//                        if (rdresult.getEntityList() != null && !rdresult.getEntityList().isEmpty()) {
//                            DeliveryOrder deliveryOrder = (DeliveryOrder) rdresult.getEntityList().get(0);
//
//                            Set<DOContractMapping> dOContractMappings = deliveryOrder.getdOContractMappings();
//                            if (dOContractMappings != null && !dOContractMappings.isEmpty()) {
//
//                                for (DOContractMapping contractMapping : dOContractMappings) {
//                                    contract = contractMapping.getContract();
//                                }
//                            }
//                        }
//                    } else if (linkMode.equalsIgnoreCase("Customer Invoice")) {
//                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), linkNumber);
//                        if (rdresult.getEntityList() != null && !rdresult.getEntityList().isEmpty()) {
//                            Invoice invoice = (Invoice) rdresult.getEntityList().get(0);
//
//                            Set<InvoiceContractMapping> invContractMappings = invoice.getContractMappings();
//                            if (invContractMappings != null && !invContractMappings.isEmpty()) {
//                                for (InvoiceContractMapping contractMapping : invContractMappings) {
//                                    contract = contractMapping.getContract();
//                                }
//                            }
//                        }
//                    }
//                }
//                doDataMap.put("contractid", contract != null ? contract.getID() : "");
//            } else {
//                doDataMap.put("contractid", request.getParameter("contractid"));
//            }
//
//            doDataMap.put("createdon", createdon);
//            doDataMap.put("createdby", createdby);
//            doDataMap.put("modifiedby", modifiedby);
//            doDataMap.put("updatedon", updatedon);
//
//            KwlReturnObject doresult = accInvoiceDAOobj.saveSalesReturn(doDataMap);
//            salesReturn = (SalesReturn) doresult.getEntityList().get(0);
//
//            doDataMap.put("id", salesReturn.getID());
//            JSONArray productArray = new JSONArray();
//            HashSet dodetails = saveSalesReturnRows(request, salesReturn, companyid, productArray);
//
//
//
//            String customfield = request.getParameter("customfield");
//            if (!StringUtil.isNullOrEmpty(customfield)) {
//                JSONArray jcustomarray = new JSONArray(customfield);
//                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
//                customrequestParams.put("customarray", jcustomarray);
//                customrequestParams.put("modulename", Constants.Acc_SalesReturn_modulename);
//                customrequestParams.put("moduleprimarykey", Constants.Acc_SalesReturnId);
//                customrequestParams.put("modulerecid", salesReturn.getID());
//                customrequestParams.put("moduleid", Constants.Acc_Sales_Return_ModuleId);
//                customrequestParams.put("companyid", companyid);
//                customrequestParams.put("customdataclasspath", Constants.Acc_SalesReturn_custom_data_classpath);
//                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
//                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
//                    doDataMap.put("accsalesreturncustomdataref", salesReturn.getID());
//                    KwlReturnObject accresult = accInvoiceDAOobj.updateSalesReturnCustomData(doDataMap);
//                }
//            }
//
//            if (preferences.isInventoryAccountingIntegration() && preferences.isWithInvUpdate()) {
//
//                String action = "17";
//                boolean isDirectUpdateInvFlag = false;
//                if (preferences.isUpdateInvLevel()) {
//                    isDirectUpdateInvFlag = true;
//                    action = "19";//Direct Inventory Update action
//                }
//
////                JSONArray productArray = new JSONArray();
////                if (!StringUtil.isNullOrEmpty(request.getParameter("detail"))) {
////                    JSONArray jArr = new JSONArray(request.getParameter("detail"));
////                    for (int i = 0; i < jArr.length(); i++) {
////                        JSONObject jobj = jArr.getJSONObject(i);
////                        KwlReturnObject proresult = accountingHandlerDAOobj.getObject(Product.class.getName(), jobj.getString("productid"));
////                        Product product = (Product) proresult.getEntityList().get(0);
////
////                        JSONObject productObject = new JSONObject();
////
////                        double productRate = 0;
////                        if (!jobj.isNull("rowid") && !StringUtil.isNullOrEmpty(jobj.getString("rowid")) && !jobj.getString("rowid").equalsIgnoreCase("undefined")) {
////                            productRate = getProductPrice(request, jobj.getString("rowid"));
////                        }
////
////                        if (!StringUtil.isNullOrEmpty(srid)) {
////                            if (jobj.optDouble("changedQuantity", 0) != 0) {
////                                if (!StringUtil.isNullOrEmpty(request.getParameter("deletedData"))) {
////                                    productObject.put("itemUomId", jobj.getString("uomid"));
////                                    productObject.put("itemBaseUomRate", jobj.getDouble("baseuomrate"));
////                                    productObject.put("itemQuantity", jobj.getInt("dquantity") * jobj.getDouble("baseuomrate"));
////                                    productObject.put("quantity", jobj.getDouble("dquantity"));
////                                    //productObject.put("itemQuantity", jobj.getDouble("dquantity"));
////                                    productObject.put("itemCode", product.getProductid());
////                                    if (isDirectUpdateInvFlag) {
////                                        productObject.put("storeid", jobj.optString("invstore"));
////                                        productObject.put("locationid", jobj.optString("invlocation"));
////                                        productObject.put("rate", productRate);
////                                    }
////                                    productArray.put(productObject);
////                                } else {
////                                    productObject.put("itemUomId", jobj.getString("uomid"));
////                                    productObject.put("itemBaseUomRate", jobj.getDouble("baseuomrate"));
////                                    productObject.put("itemQuantity", jobj.getDouble("changedQuantity"));
////                                    productObject.put("quantity", jobj.getDouble("changedQuantity"));
////                                    //productObject.put("itemQuantity", jobj.getDouble("changedQuantity"));
////                                    productObject.put("itemCode", product.getProductid());
////                                    if (isDirectUpdateInvFlag) {
////                                        productObject.put("storeid", jobj.optString("invstore"));
////                                        productObject.put("locationid", jobj.optString("invlocation"));
////                                        productObject.put("rate", productRate);
////                                    }
////                                    productArray.put(productObject);
////                                }
////                            }
////                        } else {
////                            productObject.put("itemUomId", jobj.getString("uomid"));
////                            productObject.put("itemBaseUomRate", jobj.getDouble("baseuomrate"));
////                            productObject.put("itemQuantity", jobj.getInt("dquantity") * jobj.getDouble("baseuomrate"));
////                            productObject.put("quantity", jobj.getDouble("dquantity"));
////                            //productObject.put("itemQuantity", jobj.getDouble("dquantity"));                            
////                            productObject.put("itemCode", product.getProductid());
////                            if (isDirectUpdateInvFlag) {
////                                productObject.put("storeid", jobj.optString("invstore"));
////                                productObject.put("locationid", jobj.optString("invlocation"));
////                                productObject.put("rate", productRate);
////                            }
////                            productArray.put(productObject);
////                        }
////                    }
////
////                    if (!StringUtil.isNullOrEmpty(srid)) {
////                        if (!StringUtil.isNullOrEmpty(request.getParameter("deletedData"))) {
////                            JSONArray deleteArr = new JSONArray(request.getParameter("deletedData"));
////                            for (int i = 0; i < deleteArr.length(); i++) {
////                                JSONObject jobj = deleteArr.getJSONObject(i);
////
////                                KwlReturnObject proresult = accountingHandlerDAOobj.getObject(Product.class.getName(), jobj.getString("productid"));
////                                Product product = (Product) proresult.getEntityList().get(0);
////
////                                double productRate = 0;
////                                if (!jobj.isNull("rowid") && !StringUtil.isNullOrEmpty(jobj.getString("rowid")) && !jobj.getString("rowid").equalsIgnoreCase("undefined")) {
////                                    productRate = getProductPrice(request, jobj.getString("rowid"));
////                                }
////
////                                JSONObject productObject = new JSONObject();
////                                productObject.put("itemUomId", jobj.getString("productuomid"));
////                                productObject.put("itemBaseUomRate", jobj.getDouble("productbaseuomrate"));
////                                productObject.put("itemQuantity", jobj.getDouble("productbaseuomquantity") * (-1));
////                                productObject.put("quantity", jobj.getDouble("productquantity") * (-1));
////                                //productObject.put("itemQuantity", jobj.getDouble("productquantity") * (-1));
////                                productObject.put("itemCode", product.getProductid());
////                                if (isDirectUpdateInvFlag) {
////                                    productObject.put("storeid", jobj.optString("productinvstore"));
////                                    productObject.put("locationid", jobj.optString("productinvlocation"));
////                                    productObject.put("rate", productRate);
////                                }
////                                productArray.put(productObject);
////                            }
////                        }
////                    }
//
//                    if (productArray.length() > 0) {
//
//                        String sendDateFormat = "yyyy-MM-dd";
//                        DateFormat dateformat = new SimpleDateFormat(sendDateFormat);
//                        Date date = df.parse(request.getParameter("billdate"));
//                        String stringDate = dateformat.format(date);
//
//                        JSONObject jSONObject = new JSONObject();
//                        jSONObject.put("deliveryDate", stringDate);
//                        jSONObject.put("dateFormat", sendDateFormat);
//                        jSONObject.put("details", productArray);
//                        jSONObject.put("orderNumber", entryNumber);
//                        jSONObject.put("selling", true);
//
//                        String url = this.getServletContext().getInitParameter("inventoryURL");
//                        CommonFnController cfc = new CommonFnController();
//                        cfc.updateInventoryLevel(request, jSONObject, url, action);
//                    }
////                }
//            }
//            auditTrailObj.insertAuditLog(AuditAction.SALES_RETURN, "User " + sessionHandlerImpl.getUserFullName(request) + " has" + auditMsg + salesReturn.getSalesReturnNumber(), request, salesReturn.getID());
//        } catch (ParseException ex) {
//            throw ServiceException.FAILURE("saveDeliveryOrder : " + ex.getMessage(), ex);
//        }
//        return salesReturn;
//    }
//    public String getDeliveryReturnStatus(DeliveryOrder so) throws ServiceException {
//        Set<DeliveryOrderDetail> orderDetail = so.getRows();
//        Iterator ite = orderDetail.iterator();
//
//        String result = "Closed";
//        while (ite.hasNext()) {
//            DeliveryOrderDetail soDetail = (DeliveryOrderDetail) ite.next();
//            KwlReturnObject idresult = accInvoiceDAOobj.getIDFromSRD(soDetail.getID());
//            List list = idresult.getEntityList();
//            Iterator ite1 = list.iterator();
//            double qua = 0;
//            while (ite1.hasNext()) {
//                SalesReturnDetail ge = (SalesReturnDetail) ite1.next();
//                qua += ge.getInventory().getQuantity();
//            }
//            if (qua < soDetail.getActualQuantity()) {
//                result = "Open";
//                break;
//            }
//        }
//        return result;
//    }
//    public HashSet saveSalesReturnRows(HttpServletRequest request, SalesReturn salesReturn, String companyid,JSONArray productArray) throws ServiceException, AccountingException, SessionExpiredException, ParseException, UnsupportedEncodingException {
//        HashSet rows = new HashSet();
//        try {
//            JSONArray jArr = new JSONArray(request.getParameter("detail"));
//
//            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
//            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
//
//            boolean isLeaseFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isLeaseFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isLeaseFixedAsset")) : false;
//            boolean isConsignment = (!StringUtil.isNullOrEmpty(request.getParameter("isConsignment"))) ? Boolean.parseBoolean(request.getParameter("isConsignment")) : false;
//
//            for (int i = 0; i < jArr.length(); i++) {
//                JSONObject jobj = jArr.getJSONObject(i);
//                HashMap<String, Object> dodDataMap = new HashMap<String, Object>();
//                dodDataMap.put("srno", i + 1);
//                dodDataMap.put("companyid", companyid);
//                dodDataMap.put("srid", salesReturn.getID());
//                dodDataMap.put("productid", jobj.getString("productid"));
//                dodDataMap.put("reason", jobj.getString("reason"));
//                String linkMode = request.getParameter("fromLinkCombo");
//
//                KwlReturnObject proresult = accountingHandlerDAOobj.getObject(Product.class.getName(), jobj.getString("productid"));
//                Product product = (Product) proresult.getEntityList().get(0);
//
//                dodDataMap.put("description", jobj.getString("description"));
//                dodDataMap.put("partno", jobj.getString("partno"));
//
//                double actquantity = jobj.getDouble("quantity");
//                double dquantity = jobj.getDouble("dquantity");
//                double baseuomrate = 1;
//                if (jobj.has("baseuomrate") && jobj.get("baseuomrate") != null) {
//                    baseuomrate = jobj.getDouble("baseuomrate");
//                }
//                dodDataMap.put("quantity", actquantity);
//                dodDataMap.put("returnquantity", dquantity);
//                dodDataMap.put("baseuomrate", baseuomrate);
//                if (jobj.has("uomid")) {
//                    dodDataMap.put("uomid", jobj.getString("uomid"));
//                }
//                dodDataMap.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(actquantity, baseuomrate));
//                dodDataMap.put("baseuomreturnquantity", authHandler.calculateBaseUOMQuatity(dquantity, baseuomrate));
//                dodDataMap.put("remark", jobj.optString("remark"));
//                dodDataMap.put("reason", jobj.optString("reason"));
//                
//                String rowtaxid = jobj.optString("prtaxid", "");
//                if (!StringUtil.isNullOrEmpty(rowtaxid)) {
//                    boolean taxExist = false;
//                    KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), rowtaxid); // (Tax)session.get(Tax.class, taxid);
//                    Tax rowtax = (Tax) txresult.getEntityList().get(0);
//                    double rowtaxamountFromJS = StringUtil.getDouble(jobj.getString("taxamount"));
//
//                    if (rowtax == null) {
//                        throw new AccountingException("The Tax code(s) used in this transaction has been deleted.");
//                    } else {
//                        dodDataMap.put("prtaxid", rowtaxid);
//                        dodDataMap.put("taxamount", rowtaxamountFromJS);
//                    }
//                }
//
////                if (preferences.isInventoryAccountingIntegration() && preferences.isWithInvUpdate() && preferences.isUpdateInvLevel()) {
//                if (!StringUtil.isNullOrEmpty(jobj.optString("invstore"))) {
//                    dodDataMap.put("invstoreid", jobj.optString("invstore"));
//                } else {
//                    dodDataMap.put("invstoreid", "");
//                }
//                if (!StringUtil.isNullOrEmpty(jobj.optString("invlocation"))) {
//                    dodDataMap.put("invlocationid", jobj.optString("invlocation"));
//                } else {
//                    dodDataMap.put("invlocationid", "");
//                }
////                }
//
//                if (!StringUtil.isNullOrEmpty(linkMode)) {
//                    if (linkMode.equalsIgnoreCase("Delivery Order")) {
//                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(DeliveryOrderDetail.class.getName(), jobj.getString("rowid"));
//                        DeliveryOrderDetail sod = (DeliveryOrderDetail) rdresult.getEntityList().get(0);
//                        dodDataMap.put("DeliveryOrderDetail", sod);
//                    } else if (linkMode.equalsIgnoreCase("Customer Invoice") || linkMode.equalsIgnoreCase("Sales Invoice")) {
//                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(InvoiceDetail.class.getName(), jobj.getString("rowid"));
//                        InvoiceDetail id = (InvoiceDetail) rdresult.getEntityList().get(0);
//                        dodDataMap.put("InvoiceDetail", id);
//                    }
//                }
//
//                JSONObject inventoryjson = new JSONObject();
//                inventoryjson.put("productid", jobj.getString("productid"));
//                inventoryjson.put("description", jobj.getString("description"));
//                inventoryjson.put("quantity", dquantity);
//                if (jobj.has("uomid")) {
//                    inventoryjson.put("uomid", jobj.getString("uomid"));
//                }
//                inventoryjson.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(dquantity, baseuomrate));
//                inventoryjson.put("baseuomrate", baseuomrate);
//                if (isLeaseFixedAsset) {
//                    inventoryjson.put("leaseFlag", isLeaseFixedAsset);
//                }
//                if (isConsignment) {
//                    inventoryjson.put("consignuomquantity", -(dquantity * baseuomrate));
//                    inventoryjson.put("isConsignment", isConsignment);
//                }
//                inventoryjson.put("carryin", true);
//                inventoryjson.put("defective", false);
//                inventoryjson.put("newinventory", false);
//                inventoryjson.put("companyid", companyid);
//                inventoryjson.put("updatedate", AuthHandler.getDateFormatter(request).parse(request.getParameter("billdate")));
//                KwlReturnObject invresult = accProductObj.addInventory(inventoryjson);
//                Inventory inventory = (Inventory) invresult.getEntityList().get(0);
//
//                dodDataMap.put("Inventory", inventory);
//                if (jobj.has("rate")) {
//                    dodDataMap.put("rate", jobj.getString("rate"));
//                }
//                KwlReturnObject result = accInvoiceDAOobj.saveSalesReturnDetails(dodDataMap);
//                SalesReturnDetail row = (SalesReturnDetail) result.getEntityList().get(0);
//
//
//                if (jobj.has("batchdetails") && jobj.getString("batchdetails") != null) {
//                    String batchDetails = jobj.getString("batchdetails");
//                    if (!StringUtil.isNullOrEmpty(batchDetails)) {
//                        saveNewSRBatch(batchDetails, inventory,request,row,productArray);
////                        if (productBatch != null) {
////                            dodDataMap.put("batch", productBatch.getId());
////                            dodDataMap.put("id", row.getID());
//////                            dodDataMap.put("invstoreid",productBatch.getLocation().getId());
//////                            dodDataMap.put("invlocationid",productBatch.getWarehouse().getId());
////                            result = accInvoiceDAOobj.saveSalesReturnDetails(dodDataMap);
////                            row = (SalesReturnDetail) result.getEntityList().get(0);
////                        }
//                    }
//                }
//
//                String contractid = request.getParameter("contractid");
//                if (!StringUtil.isNullOrEmpty(linkMode) && !StringUtil.isNullOrEmpty(contractid)) {
//                    if (linkMode.equalsIgnoreCase("Delivery Order")) {
//                        String status = "Open";
//                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(DeliveryOrderDetail.class.getName(), jobj.getString("rowid"));
//
//                        DeliveryOrderDetail sod = (DeliveryOrderDetail) rdresult.getEntityList().get(0);
//
//                        Set<DOContractMapping> dOContractMappings = sod.getDeliveryOrder().getdOContractMappings();
//                        if (dOContractMappings != null && !dOContractMappings.isEmpty()) {
//                            Contract contract = null;
//                            for (DOContractMapping contractMapping : dOContractMappings) {
//                                contract = contractMapping.getContract();
//                            }
//                            if (contract != null) {
//
//                                status = accInvoiceModuleService.getDeliveryReturnStatus(sod.getDeliveryOrder());
////                        ['1','Pending'],['2','Pending & Closed'],['3','Done'],['4','Done & Closed']]
//                                int contractSRStatus = 1;
//
//                                if (contract.getSrstatus() == 2 && status.equalsIgnoreCase("Closed")) {
//                                    contractSRStatus = 4;
//                                } else if (contract.getSrstatus() == 2 && status.equalsIgnoreCase("Open")) {
//                                    contractSRStatus = 2;
//                                } else if (contract.getSrstatus() == 1 && status.equalsIgnoreCase("Closed")) {
//                                    contractSRStatus = 3;
//                                }
//                                accSalesOrderDAOObj.changeContractSRStatus(contract.getID(), contractSRStatus);
//                            }
//                        }
//                    }
//                }
//
//                boolean isFromSalesReturn = true;
//
//                if (isLeaseFixedAsset && product.isAsset()) {
//                    Set<AssetDetails> assetDetailsSet = accInvoiceModuleService.saveAssetDetails(request, jobj.getString("productid"), jobj.getString("assetDetails"), 0, false, false, isLeaseFixedAsset, isFromSalesReturn);
//
//                    Set<AssetInvoiceDetailMapping> assetInvoiceDetailMappings = accInvoiceModuleService.saveAssetInvoiceDetailMapping(row.getID(), assetDetailsSet, companyid, Constants.Acc_Sales_Return_ModuleId);
//                }
//
//
//                String customfield = jobj.getString("customfield");
//                if (!StringUtil.isNullOrEmpty(customfield)) {
//                    HashMap<String, Object> DOMap = new HashMap<String, Object>();
//                    JSONArray jcustomarray = new JSONArray(customfield);
//
//                    HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
//                    customrequestParams.put("customarray", jcustomarray);
//                    customrequestParams.put("modulename", "SalesReturnDetail");
//                    customrequestParams.put("moduleprimarykey", "SalesReturnDetailId");
//                    customrequestParams.put("modulerecid", row.getID());
//                    customrequestParams.put("moduleid", Constants.Acc_Sales_Return_ModuleId);
//                    customrequestParams.put("companyid", companyid);
//                    DOMap.put("id", row.getID());
//                    customrequestParams.put("customdataclasspath", Constants.Acc_SalesReturnDetails_custom_data_classpath);
//                    KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
//                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
//                        DOMap.put("srdetailscustomdataref", row.getID());
//                        accInvoiceDAOobj.updateSRDetailsCustomData(DOMap);
//                    }
//                }
//                rows.add(row);
//            }
//        } catch (JSONException ex) {
//            throw ServiceException.FAILURE("saveSalesReturnRows : " + ex.getMessage(), ex);
//        }
//        return rows;
//    }
//    public void saveNewSRBatch(String batchJSON, Inventory inventory, HttpServletRequest request, SalesReturnDetail salesReturnDetail, JSONArray productArray) throws JSONException, ParseException, SessionExpiredException, ServiceException, UnsupportedEncodingException {
//        JSONArray jArr = new JSONArray(batchJSON);
//        double ActbatchQty = 1;
//        double batchQty = 0;
//        boolean isLocationForProduct = false;
//        boolean isWarehouseForProduct = false;
//        boolean isBatchForProduct = false;
//        boolean isSerialForProduct = false;
//        boolean isConsignment = false;
//        DateFormat df = authHandler.getDateFormatter(request);
//        String companyid = sessionHandlerImpl.getCompanyid(request);
//        if (!StringUtil.isNullOrEmpty(request.getParameter("isConsignment"))) {
//            isConsignment = Boolean.parseBoolean(request.getParameter("isConsignment"));
//        }
//        if (!StringUtil.isNullOrEmpty(inventory.getProduct().getID())) {
//            KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), inventory.getProduct().getID());
//            Product product = (Product) prodresult.getEntityList().get(0);
//            isLocationForProduct = product.isIslocationforproduct();
//            isWarehouseForProduct = product.isIswarehouseforproduct();
//            isBatchForProduct = product.isIsBatchForProduct();
//            isSerialForProduct = product.isIsSerialForProduct();
//        }
//
//        //Save Batch detail for both  option for serial no and batch also as if batch option is off then also we are generating batch in backend
//        for (int i = 0; i < jArr.length(); i++) {
//            JSONObject jSONObject = new JSONObject(jArr.get(i).toString());
//            if (jSONObject.has("quantity") && !jSONObject.getString("quantity").equals("undefined")) {
//                ActbatchQty = jSONObject.getDouble("quantity");
//            }
//            if (batchQty == 0) {
//                batchQty = jSONObject.getDouble("quantity");
//                if (isLocationForProduct && isWarehouseForProduct ){
//                    JSONObject productObject=new JSONObject();
//                    productObject.put("itemUomId", inventory.getProduct().getUnitOfMeasure().getID());
//                    productObject.put("itemBaseUomRate", 1);
//                    productObject.put("itemQuantity", jSONObject.optDouble("quantity",0.0));
//                    productObject.put("quantity", jSONObject.optDouble("quantity",0.0));
//                    //productObject.put("itemQuantity", jobj.getDouble("dquantity") * (-1));                            
//                    productObject.put("itemCode", inventory.getProduct().getProductid());
//                    productObject.put("storeid", jSONObject.getString("warehouse"));
//                    productObject.put("locationid", jSONObject.getString("location"));
//                    productObject.put("rate", salesReturnDetail.getRate());             
//                    productArray.put(productObject);
//                }
//            }
//            if ((isLocationForProduct || isWarehouseForProduct || isBatchForProduct) && (batchQty == ActbatchQty)) {
//                HashMap<String, Object> documentMap = new HashMap<String, Object>();
//                documentMap.put("quantity", jSONObject.getString("quantity"));
//                documentMap.put("batchmapid", jSONObject.getString("purchasebatchid"));
//                documentMap.put("documentid", salesReturnDetail.getID());
//                documentMap.put("transactiontype", "29");//This is GRN Type Tranction  
//                if (jSONObject.has("mfgdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("mfgdate"))) {
//                    documentMap.put("mfgdate", AuthHandler.getDateFormatter(request).parse(jSONObject.getString("mfgdate")));
//                }
//                if (jSONObject.has("expdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("expdate"))) {
//                    documentMap.put("expdate", AuthHandler.getDateFormatter(request).parse(jSONObject.getString("expdate")));
//                }
//
//
//                if (!isBatchForProduct && !isSerialForProduct) {
//                    HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
//                    ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
//                    filter_names.add("company.companyID");
//                    filter_params.add(sessionHandlerImpl.getCompanyid(request));
//
//                    if (!StringUtil.isNullOrEmpty(jSONObject.getString("warehouse"))) {
//                        String warehouse = jSONObject.getString("warehouse");
//                        filter_names.add("warehouse.id");
//                        filter_params.add(warehouse);
//                    }
//                    if (!StringUtil.isNullOrEmpty(jSONObject.getString("location"))) {
//                        String location = jSONObject.getString("location");
//                        filter_names.add("location.id");
//                        filter_params.add(location);
//                    }
//
//
//                    // if (!StringUtil.isNullOrEmpty(request.getParameter("productid"))) {
//                    filter_names.add("product");
//                    filter_params.add(inventory.getProduct().getID());
//                    // }
//
//
//                    filterRequestParams.put("filter_names", filter_names);
//                    filterRequestParams.put("filter_params", filter_params);
//                    filterRequestParams.put("order_by", order_by);
//                    filterRequestParams.put("order_type", order_type);
//                    KwlReturnObject result = accMasterItemsDAOobj.getNewBatches(filterRequestParams);
//                    List listResult = result.getEntityList();
//                    Iterator itrResult = listResult.iterator();
//                    Double quantityToDue = ActbatchQty;
//                    while (itrResult.hasNext()) {
//                        NewProductBatch newProductBatch = (NewProductBatch) itrResult.next();
//                        if (quantityToDue > 0) {
//                            double Qty = newProductBatch.getQuantity();
//                            double dueQty = newProductBatch.getQuantitydue();
//                            HashMap<String, Object> batchUpdateQtyMap = new HashMap<String, Object>();
//                            batchUpdateQtyMap.put("id", newProductBatch.getId());
//                            if ((Qty - dueQty) > 0) {
//                                if (quantityToDue > (Qty - dueQty)) {
//                                    batchUpdateQtyMap.put("qty", String.valueOf(((Qty - dueQty))));
//                                    quantityToDue = quantityToDue - (Qty - dueQty);
//                                    if (isConsignment) {
//                                        batchUpdateQtyMap.put("consignquantity", String.valueOf((-(Qty - dueQty))));//in do we are adding consignquantity and for return removing consignquantity
//                                    }
//
//                                } else {
//                                    batchUpdateQtyMap.put("qty", String.valueOf((quantityToDue)));
//                                    if (isConsignment) {
//                                        batchUpdateQtyMap.put("consignquantity", String.valueOf(-(quantityToDue)));  //in do we are adding consignquantity and for return removing consignquantity
//                                    }
//                                    quantityToDue = quantityToDue - quantityToDue;
//                                }
//                                documentMap.put("batchmapid", newProductBatch.getId());
//                                accCommonTablesDAO.saveBatchAmountDue(batchUpdateQtyMap);
//
//                            }
//                        }
//
//                    }
//
//
//
//                } else {
//
//                    HashMap<String, Object> batchUpdateQtyMap = new HashMap<String, Object>();
//                    batchUpdateQtyMap.put("qty", String.valueOf((Double.parseDouble(jSONObject.getString("quantity")))));
//                    if (isConsignment) {
//                        batchUpdateQtyMap.put("consignquantity", String.valueOf(-(Double.parseDouble(jSONObject.getString("quantity"))))); //in do we are adding consignquantity and for return removing consignquantity
//                    }
//
//                    batchUpdateQtyMap.put("id", jSONObject.getString("purchasebatchid"));
//                    accCommonTablesDAO.saveBatchAmountDue(batchUpdateQtyMap);
//
//                }
//
//                accCommonTablesDAO.saveBatchDocumentMapping(documentMap);
//
//
//            }
//            batchQty--;
//
//
//            if (isSerialForProduct) {  //if serial no option is on then only save the serial no details 
//
//                HashMap<String, Object> documentMap = new HashMap<String, Object>();
//                documentMap.put("quantity", 1);
//                documentMap.put("serialmapid", jSONObject.getString("purchaseserialid"));
//                documentMap.put("documentid", salesReturnDetail.getID());
//                documentMap.put("transactiontype", "29");//This is GRN Type Tranction  
//                if (jSONObject.has("expstart") && !StringUtil.isNullOrEmpty(jSONObject.getString("expstart"))) {
//                    documentMap.put("expfromdate", AuthHandler.getDateFormatter(request).parse(jSONObject.getString("expstart")));
//                }
//                if (jSONObject.has("expend") && !StringUtil.isNullOrEmpty(jSONObject.getString("expend"))) {
//                    documentMap.put("exptodate", AuthHandler.getDateFormatter(request).parse(jSONObject.getString("expend")));
//                }
//
//                accCommonTablesDAO.saveSerialDocumentMapping(documentMap);
//                HashMap<String, Object> serialUpdateQtyMap = new HashMap<String, Object>();
//                serialUpdateQtyMap.put("qty", "1");
//                if (isConsignment) {
//                    serialUpdateQtyMap.put("consignquantity", "-1"); //in do we are adding consignquantity and for return removing consignquantity
//                }
//                serialUpdateQtyMap.put("id", jSONObject.getString("purchaseserialid"));
//                accCommonTablesDAO.saveSerialAmountDue(serialUpdateQtyMap);
//
//            } else {
//                batchQty = 0;
//            }
//        }
//
//    }
    public ProductBatch saveSRBatch(String batchJSON, Inventory inventory, HttpServletRequest request) throws JSONException, ParseException, SessionExpiredException, ServiceException, UnsupportedEncodingException, AccountingException {
        JSONArray jArr = new JSONArray(batchJSON);

        KwlReturnObject kmsg = null;
        String purchasebatchid = "";
        String purchaseserialid = "";
        double quantity = 0;
        boolean isBatch = false;
        boolean isserial = false;
        boolean isBatchForProduct = false;
        boolean isSerialForProduct = false;
        boolean isserialusedinDO = false;
        DateFormat df = authHandler.getDateOnlyFormat(request);
        String companyid = sessionHandlerImpl.getCompanyid(request);
        KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
        CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
        isBatch = preferences.isIsBatchCompulsory();
        isserial = preferences.isIsSerialCompulsory();

        if (!StringUtil.isNullOrEmpty(inventory.getProduct().getID())) {

            KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), inventory.getProduct().getID());
            Product product = (Product) prodresult.getEntityList().get(0);
            isBatchForProduct = product.isIsBatchForProduct();
            isSerialForProduct = product.isIsSerialForProduct();
        }

        String linkMode = request.getParameter("fromLinkCombo");
        for (int i = 0; i < 1; i++) {
            HashMap<String, Object> pdfTemplateMap = new HashMap<String, Object>();
            JSONObject jSONObject = new JSONObject(jArr.get(i).toString());
            pdfTemplateMap.put("companyid", inventory.getCompany().getCompanyID());
            //pdfTemplateMap.put("name", jSONObject.getString("batch"));
            pdfTemplateMap.put("name", StringUtil.DecodeText(jSONObject.optString("batch")));
            if (jSONObject.has("mfgdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("mfgdate"))) {
                pdfTemplateMap.put("mfgdate", df.parse(jSONObject.getString("mfgdate")));
            }
            if (jSONObject.has("expdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("expdate"))) {
                pdfTemplateMap.put("expdate", df.parse(jSONObject.getString("expdate")));
            }
            quantity = Double.parseDouble(jSONObject.getString("quantity"));
            pdfTemplateMap.put("quantity", jSONObject.getString("quantity"));
            pdfTemplateMap.put("balance", jSONObject.getString("balance"));
            pdfTemplateMap.put("location", jSONObject.getString("location"));
            pdfTemplateMap.put("product", inventory.getProduct().getID());
            pdfTemplateMap.put("warehouse", jSONObject.getString("warehouse"));
            purchasebatchid = jSONObject.getString("purchasebatchid");
            pdfTemplateMap.put("isopening", false);
            pdfTemplateMap.put("transactiontype", "4");//This is DO Type Tranction 
            pdfTemplateMap.put("ispurchase", false);
            kmsg = accCommonTablesDAO.saveBatchForProduct(pdfTemplateMap);
        }
        ProductBatch productBatch = null;
        String productBatchId = "";
        if (kmsg.getEntityList().size() != 0) {
            productBatch = (ProductBatch) kmsg.getEntityList().get(0);
            productBatchId = productBatch.getId();
            HashMap<String, Object> pdfTemplateMap = new HashMap<String, Object>();
            pdfTemplateMap.put("batchtomap", purchasebatchid);
            pdfTemplateMap.put("batchmap", productBatch.getId());
            pdfTemplateMap.put("returntype", "1");
            pdfTemplateMap.put("quantity", quantity);
            kmsg = accCommonTablesDAO.saveReturnBatchMapping(pdfTemplateMap);
        }
        if (isSerialForProduct) {  //if serial no option is on then only save the serial no details   
            for (int i = 0; i < jArr.length(); i++) {
                HashMap<String, Object> pdfTemplateMap = new HashMap<String, Object>();
                JSONObject jSONObject = new JSONObject(jArr.get(i).toString());
                if (!StringUtil.isNullOrEmpty(jSONObject.getString("serialnoid"))) {
                    isserialusedinDO = accCommonTablesDAO.isserialusedinDOandSR(jSONObject.getString("serialnoid"));
                }
                if (isserialusedinDO) {  //check wether already salesreturn made for this serial
                    throw new AccountingException("Serial No " + StringUtil.DecodeText(jSONObject.optString("serialno")) + " is already used in <br> some transaction.");
                }
                if (StringUtil.isNullOrEmpty(linkMode)) {  // in link case if do not send id becase it will update the record of that id
                    pdfTemplateMap.put("id", jSONObject.getString("serialnoid"));
                }

                pdfTemplateMap.put("companyid", inventory.getCompany().getCompanyID());
                pdfTemplateMap.put("product", inventory.getProduct().getID());
                pdfTemplateMap.put("name", StringUtil.DecodeText(jSONObject.optString("serialno")));
                if (jSONObject.has("expstart") && !StringUtil.isNullOrEmpty(jSONObject.getString("expstart"))) {
                    pdfTemplateMap.put("expfromdate", df.parse(jSONObject.getString("expstart")));
                }
                if (jSONObject.has("expend") && !StringUtil.isNullOrEmpty(jSONObject.getString("expend"))) {
                    pdfTemplateMap.put("exptodate", df.parse(jSONObject.getString("expend")));
                }
                pdfTemplateMap.put("batch", productBatchId);
                pdfTemplateMap.put("transactiontype", "4");//This is DO Type Tranction 
                pdfTemplateMap.put("ispurchase", true);   // as this should be available after SR in DO or PR
                kmsg = accCommonTablesDAO.saveSerialForBatch(pdfTemplateMap);

                if (kmsg.getEntityList().size() != 0) {
                    BatchSerial batchSerial = (BatchSerial) kmsg.getEntityList().get(0);
                    String salesSerial = batchSerial.getId();
                    pdfTemplateMap = new HashMap<String, Object>();
                    pdfTemplateMap.put("maptoserialid", jSONObject.getString("purchaseserialid"));
                    pdfTemplateMap.put("mapserialid", salesSerial);
                    pdfTemplateMap.put("returntype", "1");
                    kmsg = accCommonTablesDAO.saveReturnSerialMapping(pdfTemplateMap);
                    accCommonTablesDAO.deleteSalesPurchaseSerialMapping(jSONObject.getString("purchaseserialid"));
                }
            }
        }
        return productBatch;
    }

    public ModelAndView updateDeliveryOrderFavourite(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            KwlReturnObject result = null;
            HashMap<String, Object> doDataMap = new HashMap<String, Object>();
            String id = request.getParameter("id");
            doDataMap.put("id", id);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            doDataMap.put("companyid", companyid);
            doDataMap.put("orderdate", new Date(request.getParameter("date")));
            doDataMap.put("isfavourite", request.getParameter("isfavourite"));
            if (!StringUtil.isNullOrEmpty(id)) {
                result = accInvoiceDAOobj.saveDeliveryOrder(doDataMap);
            }
            msg = messageSource.getMessage("acc.field.setsuccessfully", null, RequestContextUtils.getLocale(request));
            issuccess = true;
            txnManager.commit(status);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView updateDeliveryOrderPrint(HttpServletRequest request, HttpServletResponse response) {
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
                HashMap<String, Object> doDataMap = new HashMap<String, Object>();
                doDataMap.put("id", SOIDList.get(cnt));
                doDataMap.put("isprinted", request.getParameter("isprinted"));
                if (!StringUtil.isNullOrEmpty(SOIDList.get(cnt))) {
                    result = accInvoiceDAOobj.saveDeliveryOrder(doDataMap);
                }
            }
            msg = messageSource.getMessage("acc.field.setsuccessfully", null, RequestContextUtils.getLocale(request));
            issuccess = true;
            txnManager.commit(status);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView updateSalesReturnFavourite(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            KwlReturnObject result = null;
            HashMap<String, Object> doDataMap = new HashMap<String, Object>();
            String id = request.getParameter("id");
            doDataMap.put("id", id);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            doDataMap.put("companyid", companyid);
            doDataMap.put("orderdate", new Date(request.getParameter("date")));

            doDataMap.put("isfavourite", request.getParameter("isfavourite"));
            if (!StringUtil.isNullOrEmpty(id)) {
                result = accInvoiceDAOobj.saveSalesReturn(doDataMap);
            }
            msg = messageSource.getMessage("acc.field.setsuccessfully", null, RequestContextUtils.getLocale(request));
            issuccess = true;
            txnManager.commit(status);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView updateFavourite(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            KwlReturnObject result = null;
            HashSet<InvoiceDetail> invcdetails = null;
            String invoiceid = request.getParameter("invoiceid");
            JSONObject invjson = new JSONObject();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            boolean isDraft = StringUtil.isNullOrEmpty(request.getParameter("isDraft")) ? false : Boolean.parseBoolean(request.getParameter("isDraft"));
            invjson.put("companyid", companyid);
            invjson.put("orderdate", new Date(request.getParameter("date")));
            invjson.put("invoiceid", invoiceid);
            invjson.put("isfavourite", request.getParameter("isfavourite"));
            invjson.put("isDraft", isDraft);
            if (!StringUtil.isNullOrEmpty(invoiceid)) {
                result = accInvoiceDAOobj.updateInvoice(invjson, invcdetails);
            }
            msg = messageSource.getMessage("acc.field.setsuccessfully", null, RequestContextUtils.getLocale(request));
            issuccess = true;
            txnManager.commit(status);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (JSONException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView deleteAssetMaintenanceScheduleEvent(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            deleteAssetMaintenanceScheduleEvent(request);

            msg = messageSource.getMessage("acc.Workorder.MaintenanceScheduleEventhasbeendeletedsuccefully", null, RequestContextUtils.getLocale(request));
            issuccess = true;
            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());

    }

    public void deleteAssetMaintenanceScheduleEvent(HttpServletRequest request) throws SessionExpiredException, ParseException, ServiceException, AccountingException {

        try {
            String scheduleId = request.getParameter("scheduleId");

            String companyid = sessionHandlerImpl.getCompanyid(request);

            String hiddenCurrentDateStr = request.getParameter("hiddenCurrentDate");

            Date currentDate = null;

            DateFormat df = authHandler.getDateOnlyFormat(request);

            if (!StringUtil.isNullOrEmpty(hiddenCurrentDateStr)) {
                currentDate = df.parse(hiddenCurrentDateStr);
            }

            KwlReturnObject scObj = accountingHandlerDAOobj.getObject(AssetMaintenanceScheduler.class.getName(), scheduleId);
            AssetMaintenanceScheduler scheduler = (AssetMaintenanceScheduler) scObj.getEntityList().get(0);

            if (df.parse(df.format(scheduler.getStartDate())).equals(currentDate) || df.parse(df.format(scheduler.getStartDate())).before(currentDate)) {// if schedule start date is equal to current date or before current date it should not be edit. i.e if on schedule has been started then it cannot be commit
                throw new AccountingException("Schedule Event has been started so it cannot be delete.");
            }

            // Delete Schedules work order linked with it
            deleteSchedulerWorkOrder(scheduleId, companyid);

            HashMap<String, Object> dataMap = new HashMap<String, Object>();

            dataMap.put("id", scheduleId);

            dataMap.put("companyId", companyid);

            String ScheduleName = "";
            if (scheduler != null && scheduler.getAssetMaintenanceSchedulerObject() != null) {
                ScheduleName = scheduler.getAssetMaintenanceSchedulerObject().getScheduleName();
            }
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy");
            Date sd = sdf.parse(df.format(scheduler.getStartDate()));
            Date ed = sdf.parse(df.format(scheduler.getEndDate()));

            KwlReturnObject result = accInvoiceDAOobj.deleteAssetMaintenanceScheduleEvent(dataMap);
            auditTrailObj.insertAuditLog(AuditAction.ASSET_MAINTENANCE_SCHEDULE_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted schedule event with start date " + sdf.format(sd) + " and end date " + sdf.format(ed) + " of schedule " + ScheduleName + " for asset " + scheduler.getAssetDetails().getAssetId(), request, companyid);
        } catch (ServiceException ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accInvoiceController.deleteAssetMaintenanceScheduleObject() -: " + ex.getMessage(), ex);
        }

    }

    public ModelAndView deleteAssetMaintenanceWorkOrder(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            deleteAssetMaintenanceWorkOrder(request);

            msg = messageSource.getMessage("acc.assetworkorder.WorkOrderdelete", null, RequestContextUtils.getLocale(request));
            issuccess = true;
            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());

    }

    public void deleteAssetMaintenanceWorkOrder(HttpServletRequest request) throws SessionExpiredException, ParseException, ServiceException, AccountingException {

        try {
            String workOrderId = request.getParameter("workOrderId");

            String companyid = sessionHandlerImpl.getCompanyid(request);

//            String hiddenCurrentDateStr = request.getParameter("hiddenCurrentDate");
//            Date currentDate = null;
            DateFormat df = authHandler.getDateOnlyFormat(request);

//            if (!StringUtil.isNullOrEmpty(hiddenCurrentDateStr)) {
//                currentDate = df.parse(hiddenCurrentDateStr);
//            }
            KwlReturnObject scObj = accountingHandlerDAOobj.getObject(AssetMaintenanceWorkOrder.class.getName(), workOrderId);
            AssetMaintenanceWorkOrder workOrder = (AssetMaintenanceWorkOrder) scObj.getEntityList().get(0);
            String asseid = workOrder.getAssetMaintenanceScheduler().getAssetDetails().getAssetId();
            String schedulename = workOrder.getAssetMaintenanceScheduler().getAssetMaintenanceSchedulerObject().getScheduleName();
            AssetMaintenanceScheduler maintenanceScheduler = workOrder.getAssetMaintenanceScheduler();
//
//            if (df.parse(df.format(schedulerObject.getStartDate())).equals(currentDate) || df.parse(df.format(schedulerObject.getStartDate())).before(currentDate)) {// if schedule start date is equal to current date or before current date it should not be edit. i.e if on schedule has been started then it cannot be commit
//                throw new AccountingException("Schedule Name '" + schedulerObject.getScheduleName() + "' has been started so it cannot be delete.");
//            }

            // Delete Schedules work order linked with it
            String workOrderIDS = "'" + workOrderId + "'";

            KwlReturnObject resultBS = accInvoiceDAOobj.deleteWorkOrdersBatchSerialDetails(workOrderIDS, companyid);
            KwlReturnObject result = accInvoiceDAOobj.deleteAssetMaintenanceWorkOrder(workOrderIDS, companyid);
            maintenanceScheduler.setAssignedTo(null);
            auditTrailObj.insertAuditLog(AuditAction.ASSET_MAINTENANCE_WORK_ORDER_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted Work Order " + workOrder.getWorkOrderNumber() + " of schedule " + schedulename + " for Asset " + asseid, request, companyid);
        } catch (ServiceException ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accInvoiceController.deleteAssetMaintenanceScheduleObject() -: " + ex.getMessage(), ex);
        }

    }

    public ModelAndView deleteAssetMaintenanceScheduleObject(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            deleteAssetMaintenanceScheduleObject(request);

            msg = "Maintenance Schedule has been deleted succefully";
            issuccess = true;
            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());

    }

    public void deleteAssetMaintenanceScheduleObject(HttpServletRequest request) throws SessionExpiredException, ParseException, ServiceException, AccountingException {

        try {
            String scheduleId = request.getParameter("scheduleId");

            String companyid = sessionHandlerImpl.getCompanyid(request);

            String hiddenCurrentDateStr = request.getParameter("hiddenCurrentDate");

            Date currentDate = null;

            DateFormat df = authHandler.getDateOnlyFormat(request);

            if (!StringUtil.isNullOrEmpty(hiddenCurrentDateStr)) {
                currentDate = df.parse(hiddenCurrentDateStr);
            }

            KwlReturnObject scObj = accountingHandlerDAOobj.getObject(AssetMaintenanceSchedulerObject.class.getName(), scheduleId);
            AssetMaintenanceSchedulerObject schedulerObject = (AssetMaintenanceSchedulerObject) scObj.getEntityList().get(0);

            if (df.parse(df.format(schedulerObject.getStartDate())).equals(currentDate) || df.parse(df.format(schedulerObject.getStartDate())).before(currentDate)) {// if schedule start date is equal to current date or before current date it should not be edit. i.e if on schedule has been started then it cannot be commit
                throw new AccountingException(messageSource.getMessage("acc.maintenance.schedule.name", null, RequestContextUtils.getLocale(request)) + " '" + schedulerObject.getScheduleName() + "' " + messageSource.getMessage("acc.MaintenanceSchedules.hasbeenstartedsoitcannotbedelete", null, RequestContextUtils.getLocale(request)));
            }

            // Delete Schedules work order linked with it
            deleteWorkOrderOfSchedulerObject(scheduleId, companyid);

            HashMap<String, Object> dataMap = new HashMap<String, Object>();

            dataMap.put("id", scheduleId);

            dataMap.put("companyId", companyid);

            KwlReturnObject result = accInvoiceDAOobj.deleteAssetMaintenanceScheduleObject(dataMap);
            auditTrailObj.insertAuditLog(AuditAction.ASSET_MAINTENANCE_SCHEDULE_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted Asset Maintenance Schedule " + schedulerObject.getScheduleName(), request, companyid);
        } catch (ServiceException ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accInvoiceController.deleteAssetMaintenanceScheduleObject() -: " + ex.getMessage(), ex);
        }

    }

    public void deleteWorkOrderOfSchedulerObject(String schedulerObjectId, String companyid) throws ServiceException {

        KwlReturnObject scObj = accountingHandlerDAOobj.getObject(AssetMaintenanceSchedulerObject.class.getName(), schedulerObjectId);
        AssetMaintenanceSchedulerObject schedulerObject = (AssetMaintenanceSchedulerObject) scObj.getEntityList().get(0);

        if (!schedulerObject.getAssetMaintenanceSchedulers().isEmpty()) {
            Set<AssetMaintenanceScheduler> maintenanceSchedulers = schedulerObject.getAssetMaintenanceSchedulers();
            for (AssetMaintenanceScheduler maintenanceScheduler : maintenanceSchedulers) {
                deleteSchedulerWorkOrder(maintenanceScheduler.getId(), companyid);
            }
        }
    }

    public void deleteSchedulerWorkOrder(String schedulerId, String companyid) throws ServiceException {
        try {

            HashMap<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("schedulerId", schedulerId);
            dataMap.put("companyId", companyid);

            KwlReturnObject woResult = accInvoiceDAOobj.deleteAssetMaintenanceWorkOrderofSchedules(dataMap);

        } catch (ServiceException ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE("accInvoiceController.deleteSchedulerWorkOrder() -: " + ex.getMessage(), ex);
        }

    }

    public ModelAndView updateAssetMaintenanceScheduleAndWorkOrder(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            updateAssetMaintenanceScheduleAndWorkOrder(request);

            msg = messageSource.getMessage("acc.maintenance.setsuccessfully", null, RequestContextUtils.getLocale(request));//Asset Maintenance Schedule has been saved successfully
            issuccess = true;
            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());

    }

    public void updateAssetMaintenanceScheduleAndWorkOrder(HttpServletRequest request) throws SessionExpiredException, ParseException, ServiceException {

        String schedulerId = request.getParameter("scheduleId");
        String workOrderId = request.getParameter("workOrderId");
        String status = request.getParameter("status");
        String assignedTo = request.getParameter("assignedTo");
        boolean isToUpdateWorkOrderAlso = false;// if event's acctual start date or actual end date or assigned to is being modified then only 

        String companyid = sessionHandlerImpl.getCompanyid(request);

        DateFormat df = authHandler.getDateOnlyFormat(request);
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy");

        Date actualStartDate = null;
        Date actualEndDate = null;

        if (!StringUtil.isNullOrEmpty(request.getParameter("actualStartDate"))) {
            actualStartDate = df.parse(request.getParameter("actualStartDate"));
        }

        if (!StringUtil.isNullOrEmpty(request.getParameter("actualEndDate"))) {
            actualEndDate = df.parse(request.getParameter("actualEndDate"));
        }

        Date oldActualStartDate = null;
        Date oldActualEndDate = null;
        String oldStatus = "";
        String oldAssignTo = "";
        String schedulename = "";
        String assetid = "";
        String auditmsg = "";
        Date startdate = null;
        Date enddate = null;
        HashMap<String, Object> dataMap = new HashMap<String, Object>();

        if (!StringUtil.isNullOrEmpty(schedulerId)) {

            KwlReturnObject schdeleObj = accountingHandlerDAOobj.getObject(AssetMaintenanceScheduler.class.getName(), schedulerId);
            AssetMaintenanceScheduler scheduler = (AssetMaintenanceScheduler) schdeleObj.getEntityList().get(0);

            oldActualStartDate = scheduler.getActualStartDate();
            oldActualEndDate = scheduler.getActualEndDate();
            if (!StringUtil.isNullOrEmpty(assignedTo) && scheduler.getAssignedTo() != null && !StringUtil.isNullOrEmpty(scheduler.getAssignedTo().getValue())) {
                oldAssignTo = scheduler.getAssignedTo().getValue();

            }
            if (!StringUtil.isNullOrEmpty(status) && scheduler.getStatus() != null && !StringUtil.isNullOrEmpty(scheduler.getStatus().getValue())) {
                oldStatus = scheduler.getStatus().getValue();
            }
            assetid = scheduler.getAssetDetails().getAssetId();

            startdate = scheduler.getStartDate();
            enddate = scheduler.getEndDate();
            schedulename = (scheduler.getAssetMaintenanceSchedulerObject() != null) ? scheduler.getAssetMaintenanceSchedulerObject().getScheduleName() : "";
            dataMap.put("id", schedulerId);
            if (actualStartDate != null) {
                dataMap.put("actualStartDate", actualStartDate);
                auditmsg += " Actual start date from " + df.format(oldActualStartDate) + " to " + df.format(actualStartDate) + "<br> ";
            }

            if (actualEndDate != null) {
                dataMap.put("actualEndDate", actualEndDate);
                auditmsg += " Actual end date from " + df.format(oldActualEndDate) + " to " + df.format(actualEndDate) + "<br> ";
            }
            dataMap.put("companyId", companyid);

            if (!StringUtil.isNullOrEmpty(status)) {
                dataMap.put("status", status);

                KwlReturnObject masterObj = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), status);
                MasterItem masterItem = (MasterItem) masterObj.getEntityList().get(0);

                auditmsg += " Status from " + oldStatus + " to " + masterItem.getValue() + "<br> ";
            }

            if (!StringUtil.isNullOrEmpty(assignedTo)) {
                dataMap.put("assignedTo", assignedTo);

                KwlReturnObject masterObj = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), assignedTo);
                MasterItem masterItem = (MasterItem) masterObj.getEntityList().get(0);

                auditmsg += " AssignedTo from  " + oldAssignTo + " to " + masterItem.getValue() + "<br> ";
            }

            KwlReturnObject result = accProductObj.updateMaintenanceSchedule(dataMap);

            HashMap<String, Object> woMap = new HashMap<String, Object>();

            woMap.put("scheduleId", scheduler.getId());

            woMap.put("companyId", companyid);

            KwlReturnObject woResult = accProductObj.getAssetMaintenanceWorkOrders(woMap);

            if (woResult != null && !woResult.getEntityList().isEmpty()) {// if work order exist for selected schedule then only it will be update
                dataMap = new HashMap<String, Object>();
                dataMap.put("id", workOrderId);
                if (actualStartDate != null) {
                    dataMap.put("startDate", actualStartDate);
                }
                if (actualEndDate != null) {
                    dataMap.put("endDate", actualEndDate);
                }

                if (!StringUtil.isNullOrEmpty(assignedTo)) {
                    dataMap.put("assignedTo", assignedTo);
                }

                KwlReturnObject woresult = accInvoiceDAOobj.saveWorkOrder(dataMap);
            }
        }

        String startenddate = " start date " + sdf.format(sdf.parse(df.format(startdate))) + " and end date " + sdf.format(sdf.parse(df.format(enddate))) + ",";
        auditTrailObj.insertAuditLog(AuditAction.ASSET_MAINTENANCE_SCHEDULE_UPDATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has updated " + "<br>" + " below items for event with " + (java.util.Arrays.toString(startenddate.split(","))) + " of schedule " + schedulename + " for asset " + assetid + "<br>" + auditmsg, request, companyid);
    }

    public ModelAndView saveAssetMaintenanceSchedule(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {

            accProductModuleService.saveAssetMaintenanceSchedule(request, "");

            msg = messageSource.getMessage("acc.maintenance.setsuccessfully", null, RequestContextUtils.getLocale(request));//Asset Maintenance Schedule has been saved successfully
            issuccess = true;
            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

//    public void saveAssetMaintenanceSchedule(HttpServletRequest request) throws SessionExpiredException, ServiceException {
//        try {
//            String companyId = sessionHandlerImpl.getCompanyid(request);
//
//            String assetId = request.getParameter("assetId");
//
//            Date scheduleStartDate = null;
//
//            String scheduleStartDateStr = request.getParameter("startDate");
//
//            if (!StringUtil.isNullOrEmpty(scheduleStartDateStr)) {
//                scheduleStartDate = authHandler.getDateFormatter(request).parse(scheduleStartDateStr);
//            }
//
//            Date scheduleEndDate = null;
//
//            String scheduleEndDateStr = request.getParameter("endDate");
//
//            if (!StringUtil.isNullOrEmpty(scheduleEndDateStr)) {
//                scheduleEndDate = authHandler.getDateFormatter(request).parse(scheduleEndDateStr);
//            }
//
//            Date firstScheduleEndDate = null;
//
//            String firstScheduleEndDateStr = request.getParameter("firstScheduleEndDate");
//
//            if (!StringUtil.isNullOrEmpty(firstScheduleEndDateStr)) {
//                firstScheduleEndDate = authHandler.getDateFormatter(request).parse(firstScheduleEndDateStr);
//            }
//
//            boolean isAdhocSchedule = false;
//
//            if (!StringUtil.isNullOrEmpty(request.getParameter("isAdHocSchedule"))) {
//                isAdhocSchedule = Boolean.parseBoolean(request.getParameter("isAdHocSchedule"));
//            }
//
//            int frequency = 0;
//
//            if (!StringUtil.isNullOrEmpty(request.getParameter("repeatInterval"))) {
//                frequency = Integer.parseInt(request.getParameter("repeatInterval"));
//            }
//
//            String frequencyType = "";
//
//            if (!StringUtil.isNullOrEmpty(request.getParameter("intervalType"))) {
//                frequencyType = request.getParameter("intervalType");
//            }
//
//            int totalSchedules = 0;
//
//            if (!StringUtil.isNullOrEmpty(request.getParameter("totalEvents"))) {
//                totalSchedules = Integer.parseInt(request.getParameter("totalEvents"));
//            }
//
//            int scheduleDuration = 0;
//
//            if (!StringUtil.isNullOrEmpty(request.getParameter("scheduleDuration"))) {
//                scheduleDuration = Integer.parseInt(request.getParameter("scheduleDuration"));
//            }
//
//            for (int i = 0; i < totalSchedules; i++) {
//
//                Date startDate = scheduleStartDate;
//                Calendar cal = Calendar.getInstance();
//                cal.setTime(startDate);
//
//                if (isAdhocSchedule) {
//                    // Calculate start date
//                    cal.add(Calendar.DATE, i * scheduleDuration);
//                    startDate = cal.getTime();
//                    
//                    // Calculate End Date
//                    cal.add(Calendar.DATE, scheduleDuration);
//                    
//                    if (scheduleDuration > 0) {
//                        cal.add(Calendar.DATE, -1);
//                    }
//                    
//                } else {
//                    // Calculate start date
//                    if (frequencyType.equalsIgnoreCase("day")) {
//                        cal.add(Calendar.DATE, i * frequency);
//                    } else if (frequencyType.equalsIgnoreCase("week")) {
//                        cal.add(Calendar.WEEK_OF_MONTH, i * frequency);
//                    } else if (frequencyType.equalsIgnoreCase("year")) {
//                        cal.add(Calendar.YEAR, i * frequency);
//                    }
//
//                    startDate = cal.getTime();
//
//                    // Calculate End Date
//                    if (frequencyType.equalsIgnoreCase("day")) {
//                        cal.add(Calendar.DATE, frequency);
//                    } else if (frequencyType.equalsIgnoreCase("week")) {
//                        cal.add(Calendar.WEEK_OF_MONTH, frequency);
//                    } else if (frequencyType.equalsIgnoreCase("year")) {
//                        cal.add(Calendar.YEAR, frequency);
//                    }
//                    
//                    if (frequency > 0) {
//                        cal.add(Calendar.DATE, -1);
//                    }
//                }
//                
//
//                Date endDate = cal.getTime();
//
//                Date actualStartDate = startDate;
//
//                Date actualEndDate = endDate;
//
//
//                HashMap<String, Object> dataMap = new HashMap<String, Object>();
//
//                dataMap.put("startDate", startDate);
//                dataMap.put("endDate", endDate);
//                dataMap.put("actualStartDate", actualStartDate);
//                dataMap.put("actualEndDate", actualEndDate);
//                dataMap.put("isAdhocSchedule", isAdhocSchedule);
//                dataMap.put("frequency", frequency);
//                dataMap.put("frequencyType", frequencyType);
//                dataMap.put("totalSchedules", totalSchedules);
//                dataMap.put("scheduleDuration", scheduleDuration);
//                dataMap.put("companyId", companyId);
//                dataMap.put("assetId", assetId);
//
//                KwlReturnObject result = accInvoiceDAOobj.saveMaintenanceSchedule(dataMap);
//
//            }
//        } catch (ServiceException ex) {
//            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
//            throw ServiceException.FAILURE("accInvoiceController.saveAssetMaintenanceSchedule() -: " + ex.getMessage(), ex);
//        } catch (ParseException ex) {
//            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
//            throw ServiceException.FAILURE("accInvoiceController.saveAssetMaintenanceSchedule() -: " + ex.getMessage(), ex);
//        }
//    }
    public int getFrequencyTypeValue(String frequencyType, Date startDate) {
        int frequencyTypeValue = 0;

        if (frequencyType.equalsIgnoreCase("day")) {
            frequencyTypeValue = 1;
        } else if (frequencyType.equalsIgnoreCase("week")) {
            frequencyTypeValue = 7;
        } else if (frequencyType.equalsIgnoreCase("year")) {
            frequencyTypeValue = 365;

            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);

            int year = cal.get(Calendar.YEAR);

            if ((year % 4) == 0) {
                frequencyTypeValue = 366;
            }
        }
        return frequencyTypeValue;
    }

    public ModelAndView updatePrint(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            KwlReturnObject result = null;
            HashSet<InvoiceDetail> invcdetails = null;
            String recordids = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("recordids"))) {
                recordids = request.getParameter("recordids");
            }
            ArrayList<String> SOIDList = CustomDesignHandler.getSelectedBillIDs(recordids);
            for (int cnt = 0; cnt < SOIDList.size(); cnt++) {
                JSONObject invjson = new JSONObject();
                invjson.put("invoiceid", SOIDList.get(cnt));
                invjson.put("isprinted", request.getParameter("isprinted"));
                if (!StringUtil.isNullOrEmpty(SOIDList.get(cnt))) {
                    result = accInvoiceDAOobj.updateInvoice(invjson, invcdetails);
                }
            }
            msg = messageSource.getMessage("acc.field.setsuccessfully", null, RequestContextUtils.getLocale(request));
            issuccess = true;
            txnManager.commit(status);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (JSONException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView approvePendingInvoice(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Invoice_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        String billno = request.getParameter("billno");
        boolean iscash = StringUtil.isNullOrEmpty(request.getParameter("incash")) ? false : Boolean.parseBoolean(request.getParameter("incash"));
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            boolean isApproved = approvePendingInvoice(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String invoiceId = request.getParameter("billid");
            if (isApproved) {
                KwlReturnObject InvoiceDo = accInvoiceDAOobj.getDOFromInvoices(invoiceId, companyid, true);
                if (InvoiceDo.getEntityList() != null && InvoiceDo.getEntityList().size() > 0) {
                    Object[] oj = (Object[]) InvoiceDo.getEntityList().get(0);
                    String DeliveryOrderID = oj[1].toString();
                    accInvoiceDAOobj.approvePendingDO(DeliveryOrderID, companyid, 11);
                    KwlReturnObject extraCap = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
                    ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraCap.getEntityList().get(0);
                    KwlReturnObject dores = accountingHandlerDAOobj.getObject(DeliveryOrder.class.getName(), DeliveryOrderID);
                    DeliveryOrder doObj = (DeliveryOrder) dores.getEntityList().get(0);
                    Set<DeliveryOrderDetail> doDetails = doObj.getRows();
                    List<StockMovement> stockMovementsList = new ArrayList<>();
                    for (DeliveryOrderDetail doDetail : doDetails) {
                        Product product = doDetail.getProduct();
                        if ((product != null && !doObj.isIsconsignment()) && extraCompanyPreferences.isActivateInventoryTab() && (product.isIsBatchForProduct() || product.isIslocationforproduct() || product.isIswarehouseforproduct() || product.isIsrowforproduct() || product.isIsrackforproduct() || product.isIsbinforproduct() || product.isIsSerialForProduct())) {
                            accInvoiceModuleService.newStockMovementDeliveryOrder(doDetail, stockMovementsList);
                        }

                        Inventory inventory = doDetail.getInventory();
                        if (inventory.isInvrecord()) {
                            inventory.setBaseuomquantity(inventory.getActquantity());
                            inventory.setActquantity(0.0);
                        }
                    }
                    if (extraCompanyPreferences != null && extraCompanyPreferences.isActivateInventoryTab() && !stockMovementsList.isEmpty()) {
                        stockMovementService.addOrUpdateBulkStockMovement(doObj.getCompany(), doObj.getID(), stockMovementsList);
                    }
                }
            }
            issuccess = true;
            String action = "Cash Sales ";
            String auditaction = AuditAction.CASH_SALES_APPROVED;
            if (!iscash) {
                action = "Customer Invoice ";
                auditaction = AuditAction.CUSTOMERINVOICEAPPROVED;
            }
            auditTrailObj.insertAuditLog(auditaction, "User " + sessionHandlerImpl.getUserFullName(request) + " has Approved a " + action + billno, request, billno);
            txnManager.commit(status);
            msg = isApproved ? messageSource.getMessage("acc.field.Invoicehasbeenupdatedsuccessfully", null, RequestContextUtils.getLocale(request)) : messageSource.getMessage("acc.field.CannotapproveInvoicebecausequantitiesavailablesomeproductmentionedInvoicebelowavailablequantity", null, RequestContextUtils.getLocale(request));
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public boolean approvePendingInvoice(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {

        String companyid = sessionHandlerImpl.getCompanyid(request);
        String userid = sessionHandlerImpl.getUserid(request);
        String billid = request.getParameter("billid");
        Boolean isbilling = Boolean.parseBoolean(request.getParameter("isbilling"));
        String remark = request.getParameter("remark");
        boolean iscash = StringUtil.isNullOrEmpty(request.getParameter("incash")) ? false : Boolean.parseBoolean(request.getParameter("incash"));
        String jeID = "";
        boolean isInventoryAvailable = true;
        boolean updateJEFlag = false;
        boolean isSendMailForNextLevelUsers = true;
        String invoiceNumber = "";
        int level = 0;
        String customerEmailId = "";
        String baseUrl = URLUtil.getPageURL(request, loginpageFull);
        KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
        CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
        {
            KwlReturnObject invObj = accountingHandlerDAOobj.getObject(Invoice.class.getName(), billid);
            Invoice invoice = (Invoice) invObj.getEntityList().get(0);
            jeID = invoice.getJournalEntry().getID();
            HashMap<String, Object> addressParams = new HashMap<String, Object>();
            addressParams.put("companyid", companyid);
            addressParams.put("isDefaultAddress", true);    //always true to get defaultaddress
            addressParams.put("isBillingAddress", true);    //true to get billing address
            addressParams.put("customerid", invoice.getCustomer().getID());
            CustomerAddressDetails customerAddressDetails = accountingHandlerDAOobj.getCustomerAddressobj(addressParams);
            customerEmailId = customerAddressDetails != null ? customerAddressDetails.getEmailID() : "";
            Set<InvoiceDetail> invoiceDetails = invoice.getRows();
            for (InvoiceDetail invoiceDetail : invoiceDetails) {
                Inventory inventory = invoiceDetail.getInventory();
                Product product = inventory.getProduct();
                KwlReturnObject result = accProductObj.getQuantity(product.getID());
                double quantity = (result.getEntityList().get(0) == null) ? 0 : Double.parseDouble(result.getEntityList().get(0).toString());
                if ((!StringUtil.equal(product.getProducttype().getID(), "4efb0286-5627-102d-8de6-001cc0794cfa")) && inventory.isInvrecord() && quantity < inventory.getActquantity()) {
                    isInventoryAvailable = false;
                    break;
                }
            }
            if (invoice.getPendingapproval() == invoice.getApprovallevel()) {
                if (isInventoryAvailable) {
                    for (InvoiceDetail invoiceDetail : invoiceDetails) {
                        Inventory inventory = invoiceDetail.getInventory();
                        if (inventory.isInvrecord()) {
                            inventory.setBaseuomquantity(inventory.getActquantity());
                            inventory.setActquantity(0.0);
                        }
                    }
                }
                updateJEFlag = true;
                isSendMailForNextLevelUsers = false;
            }
            invoiceNumber = invoice.getInvoiceNumber();
        }

        if (isInventoryAvailable) {

            if (updateJEFlag) {
                accJournalEntryobj.approvePendingJE(jeID);
                //Insert new entries in optimized table.
                accJournalEntryobj.saveAccountJEs_optimized(jeID);
            }

            int approvedLevel = accInvoiceDAOobj.approvePendingInvoice(billid, isbilling, companyid, userid);
            HashMap<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("transtype", Constants.CUSTOMER_INVOICE_APPROVAL);
            hashMap.put("transid", billid);
            hashMap.put("approvallevel", approvedLevel);
            hashMap.put("remark", remark);
            hashMap.put("userid", userid);
            hashMap.put("companyid", companyid);
            accountingHandlerDAOobj.updateApprovalHistory(hashMap);
            if (isSendMailForNextLevelUsers && preferences.isSendapprovalmail()) { //this only for level 2. we aleady check is pending level and approve level are same or not
                String[] emails = {};
                String userName = sessionHandlerImpl.getUserFullName(request);
                String moduleName = "Customer Invoice";
                emails = accountingHandlerDAOobj.getApprovalUserList(request, moduleName, 2);//Leval value hard coded as 2
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                Company company = (Company) returnObject.getEntityList().get(0);
                String fromEmailId = (!company.isEmailFromCompanyCreator()) ? Constants.ADMIN_EMAILID : authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                //String fromEmailId = "admin@deskera.com";
                /**
                 * parameters required for sending mail
                 */
                Map<String, Object> mailParameters = new HashMap();
                mailParameters.put(Constants.companyid, companyid);
                mailParameters.put(Constants.prNumber, invoiceNumber);
                mailParameters.put(Constants.modulename, moduleName);
                mailParameters.put(Constants.fromName, userName);
                mailParameters.put(Constants.fromEmailID, fromEmailId);
                mailParameters.put(Constants.PAGE_URL, baseUrl);
                mailParameters.put(Constants.emails, emails);
                accountingHandlerDAOobj.sendApprovalEmails(mailParameters);
            } else if (preferences.isSendapprovalmail()) {
                String[] emails = {};
                String userName = sessionHandlerImpl.getUserFullName(request);
                String moduleName = "Customer Invoice";
                String approvalpendingStatusmsg = "";
                emails = accountingHandlerDAOobj.getApprovalUserList(request, moduleName, 2);//Leval value hard coded as 2
                if (!StringUtil.isNullOrEmpty(preferences.getApprovalEmails())) {
                    String[] compPrefMailIds = preferences.getApprovalEmails().split(",");
                    emails = AccountingManager.getMergedMailIds(emails, compPrefMailIds);
                }
                HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                qdDataMap.put(Constants.companyKey, companyid);
                qdDataMap.put("level", approvedLevel);
                qdDataMap.put(Constants.moduleid, Constants.Acc_Invoice_ModuleId);
                if (approvedLevel < 11) {
                    approvalpendingStatusmsg = commonFnControllerService.getApprovalstatusmsg(qdDataMap);
                }
                Map<String, Object> mailParameters = new HashMap();
                mailParameters.put("Number", invoiceNumber);
                mailParameters.put("userName", userName);
                mailParameters.put("emails", emails);
                mailParameters.put("sendorInfo", Constants.ADMIN_EMAILID);
                mailParameters.put("moduleName", moduleName);
                mailParameters.put("addresseeName", "All");
                mailParameters.put("companyid", companyid);
                mailParameters.put("baseUrl", baseUrl);
                mailParameters.put("approvalstatuslevel", approvedLevel);
                mailParameters.put("approvalpendingStatusmsg", approvalpendingStatusmsg);
                if (emails.length > 0) {
                    accountingHandlerDAOobj.sendApprovedEmails(mailParameters);
                }
            }
        }

//        ExtraCompanyPreferences extraCompanyPreferences = null;
//        KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
//        extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
        DocumentEmailSettings documentEmailSettings = null;
        KwlReturnObject documentEmailresult = accountingHandlerDAOobj.getObject(DocumentEmailSettings.class.getName(), sessionHandlerImpl.getCompanyid(request));
        documentEmailSettings = documentEmailresult != null ? (DocumentEmailSettings) documentEmailresult.getEntityList().get(0) : null;
        if (documentEmailSettings != null) {
            if (documentEmailSettings.isSalesInvoiceGenerationMail() && !isSendMailForNextLevelUsers) { //sending invoice generation mail when final approval is done
                String userName = sessionHandlerImpl.getUserFullName(request);

                String creatorEMailId = preferences.getCompany().getCreator() != null ? preferences.getCompany().getCreator().getEmailID() : "";
                List<String> mailIds = new ArrayList();
                if (!StringUtil.isNullOrEmpty(customerEmailId)) {
                    mailIds.add(customerEmailId);
                }
                if (!StringUtil.isNullOrEmpty(creatorEMailId)) {
                    mailIds.add(creatorEMailId);
                }
                String[] temp = new String[mailIds.size()];
                String[] tomailids = mailIds.toArray(temp);
                String moduleName = "";
                if (iscash) {
                    moduleName = "Cash Sales";
                } else {
                    moduleName = "Sales Invoice";
                }
                accountingHandlerDAOobj.sendSaveTransactionEmails(invoiceNumber, moduleName, tomailids, userName, false, companyid);
            }
        }

        return isInventoryAvailable;
    }

    public ModelAndView getBatchRemainingQuantity(HttpServletRequest request, HttpServletResponse response) throws JSONException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        jobj = accInvoiceModuleService.getBatchRemainingQuantity(paramJobj);
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getBatchRemainingQuantityforAssembly(HttpServletRequest request, HttpServletResponse response) throws JSONException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        jobj = accInvoiceModuleService.getBatchRemainingQuantityforAssembly(paramJobj);
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getInvQuantity(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        String companyid = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CF_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);

        double quantity = 0;
        boolean fromSubmit = false;
        boolean isVendor = false;

        String batchdetails = request.getParameter("batchdetails");
        if (!StringUtil.isNullOrEmpty(request.getParameter("fromSubmit"))) {
            fromSubmit = Boolean.parseBoolean(request.getParameter("fromSubmit"));
        }

        if (!StringUtil.isNullOrEmpty(batchdetails)) {
            try {
                JSONArray batchDetailsArray = new JSONArray(batchdetails);
                companyid = sessionHandlerImpl.getCompanyid(request);
                for (int i = 0; i < batchDetailsArray.length(); i++) {
                    isVendor = false;
                    JSONObject batchObj = batchDetailsArray.optJSONObject(i);
                    String documentid = batchObj.optString("documentid", "");
                    if (batchObj.has("stocktype") && !StringUtil.isNullOrEmpty(batchObj.getString("stocktype")) && "0".equals(batchObj.getString("stocktype"))) {
                        isVendor = true;
                    }
                    double qty = 0.0;
                    qty = batchObj.optDouble("quantity", 0.0);
                    String productid = batchObj.getString("productid");
                    quantity = accCommonTablesDAO.getInvQuantity(documentid, productid, isVendor);

                    if (fromSubmit) {  // on submit window check all rows quantity are availble otherwise make quantty unavaiabale
                        if (quantity < qty) {
                            quantity = 0;
                            break;
                        }
                    }
                }

                jobj.put("quantity", authHandler.formattedQuantity(quantity, companyid));
                issuccess = true;
                msg = "Batch Remaining Quantity has been received successfully.";
                txnManager.commit(status);
            } catch (Exception ex) {
                txnManager.rollback(status);
                msg = ex.getMessage();
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            } finally {
                try {
                    jobj.put("success", issuccess);
                    jobj.put("msg", msg);
                } catch (JSONException ex) {
                    Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getBatchRemainingQuantityForMultipleRecords(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        JSONArray JArray = new JSONArray();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CF_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            double quantity = 0;
            boolean isEdit = false;
            boolean linkflag = false;
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String batchdetails = request.getParameter("batchdetails");
            String transType = request.getParameter("transType");
            if (!StringUtil.isNullOrEmpty(request.getParameter("isEdit"))) {
                isEdit = Boolean.parseBoolean(request.getParameter("isEdit"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("linkflag"))) {
                linkflag = Boolean.parseBoolean(request.getParameter("linkflag"));
            }

            if (!StringUtil.isNullOrEmpty(batchdetails)) {
                JSONArray batchDetailsArray = new JSONArray(batchdetails);
                for (int i = 0; i < batchDetailsArray.length(); i++) {
                    JSONObject batchObj = batchDetailsArray.optJSONObject(i);
                    String purchasebatchid = batchObj.optString("purchasebatchid", "");
                    String documentid = batchObj.optString("documentid", "");
                    double qty = 0.0;
                    String locationid = batchObj.optString("location", "");
                    String warehouseid = batchObj.optString("warehouse", "");
                    qty = batchObj.optDouble("quantity", 0.0);
                    String productid = batchObj.getString("productid");
                    Product product = null;
                    KwlReturnObject prodresult = accountingHandlerDAOobj.getObject(Product.class.getName(), productid);
                    product = prodresult != null ? (Product) prodresult.getEntityList().get(0) : null;
                    quantity = accInvoiceModuleService.getNewBatchRemainingQuantity(locationid, warehouseid, companyId, productid, purchasebatchid, transType, isEdit, documentid, linkflag);
                    batchObj.put("avlquantity", authHandler.roundQuantity(quantity, companyId));
                    JArray.put(batchObj);
                }
            }
            jobj.put("data", JArray);
            issuccess = true;
            msg = "Batch Remaining Quantity has been received successfully.";
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    private double getBatchRemainingQuantity(String purchasebatchid, int moduleid, String companyId) throws ServiceException {
        double quantity = 0.0;
        try {
            if (!StringUtil.isNullOrEmpty(purchasebatchid)) {
                quantity = accCommonTablesDAO.getBatchRemainingQuantity(purchasebatchid, moduleid, companyId);
            }
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accInvoiceController.getBatchRemainingQuantity() -: " + ex.getMessage(), ex);
        }
        return authHandler.roundQuantity(quantity, companyId);
    }

    public ModelAndView savePackingDoList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        String billno = "";
        String billid = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Pack_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String packingDoListId = "";
            PackingDoList packingDoList = savePackingDoList(request, packingDoListId);
            billid = packingDoList.getID();
            billno = packingDoList.getPackNumber();
            issuccess = true;
            msg = messageSource.getMessage("acc.packeddolist.save", null, RequestContextUtils.getLocale(request)) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + " <b>" + billno + "</b>";   //"Delivery order has been saved successfully";                
            txnManager.commit(status);
        } catch (Exception ex) {
            System.out.println(ex);
            System.out.println(ex.getMessage());
            System.out.println(ex.getCause());
            System.out.println(ex.getStackTrace());
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("billid", billid);
                jobj.put("billno", billno);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    /*
     Function to save packing record
     */

    public ModelAndView savePacking(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        String billno = "";
        String billid = "";
        String doid = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Pack_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String detailjson = request.getParameter("detail") == null ? "" : request.getParameter("detail").toString();
            JSONArray detailarray = new JSONArray(detailjson);
//             String detailobj = detailarray.getJSONObject(0).opt("packingdodetails").toString();
//             detailarray = new JSONArray(detailobj);
            JSONObject finaldetailobj = detailarray.getJSONObject(0);
//             String duequantity = finaldetailobj.optString("duequantity");
            String duequantity = String.valueOf(finaldetailobj.optInt("dquantity") - finaldetailobj.optInt("packedqty"));
            doid = finaldetailobj.optString("billid");

            String packingDoListId = "";
            Packing packing = savePacking(request, packingDoListId);
            billid = packing.getID();
            billno = packing.getPackNumber();
            issuccess = true;
            msg = messageSource.getMessage("acc.packeddolist.save", null, RequestContextUtils.getLocale(request)) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + " <b>" + billno + "</b>";   //"Delivery order has been saved successfully";                
            txnManager.commit(status);
            status = txnManager.getTransaction(def);
            Map<String, Object> reqmap = new HashMap();
            reqmap.put("packingid", billid);
            reqmap.put("doid", doid);
            reqmap.put("duequantity", duequantity);
            reqmap.put("companyid", companyid);
            setDOStatus(reqmap);
            txnManager.commit(status);
        } catch (Exception ex) {
            System.out.println(ex);
            System.out.println(ex.getMessage());
            System.out.println(ex.getCause());
            System.out.println(ex.getStackTrace());
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("billid", billid);
                jobj.put("billno", billno);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public PackingDoList savePackingDoList(HttpServletRequest request, String packingDoListId) throws SessionExpiredException, ServiceException, AccountingException, JSONException, UnsupportedEncodingException {
        PackingDoList packingDoList = null;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String entryNumber = request.getParameter("packNumber") != null ? request.getParameter("packNumber") : request.getParameter("number");
            String status = request.getParameter("statuscombo");
            String billid = request.getParameter("billid");
            String createdby = sessionHandlerImpl.getUserid(request);
            String modifiedby = sessionHandlerImpl.getUserid(request);
            long createdon = System.currentTimeMillis();
            long updatedon = System.currentTimeMillis();
            List packedDoListDetailsList = null;
            HashMap<String, Object> packingDoListMap = new HashMap<String, Object>();
            if (!StringUtil.isNullOrEmpty(billid)) { //Need to write Edit Case
            }
            synchronized (this) {
                if (!StringUtil.isNullOrEmpty(billid)) { //Edit case
                } else { //Create new case
                    KwlReturnObject dopackinglistcnt = accInvoiceDAOobj.getPackingDoListCount(entryNumber, companyid);
                    if (dopackinglistcnt.getRecordTotalCount() > 0) {
                        throw new AccountingException("Packing DO List number '" + entryNumber + "' already exists.");
                    }

                }
            }
            DateFormat df = authHandler.getDateOnlyFormat(request);
            packingDoListMap.put("entrynumber", entryNumber);
            packingDoListMap.put("memo", request.getParameter("memo"));
            packingDoListMap.put("customerid", request.getParameter("customerid"));
            packingDoListMap.put("customer", request.getParameter("customer"));
            packingDoListMap.put("letterofcn", request.getParameter("letterofcn"));
            packingDoListMap.put("partialshipment", request.getParameter("partialshipment"));
            packingDoListMap.put("transhipment", request.getParameter("transhipment"));
            packingDoListMap.put("portofloading", request.getParameter("portofloading"));
            packingDoListMap.put("portofdischarge", request.getParameter("portofdischarge"));
            packingDoListMap.put("vessel", request.getParameter("vessel"));
            packingDoListMap.put("incoterms", request.getParameter("incoterms"));
            packingDoListMap.put("dateoflc", df.parse(request.getParameter("dateoflc")));
            packingDoListMap.put("packingdate", df.parse(request.getParameter("packingDate")));
            packingDoListMap.put("status", status);
            packingDoListMap.put("createdby", createdby);
            packingDoListMap.put("modifiedby", modifiedby);
            packingDoListMap.put("createdon", createdon);
            packingDoListMap.put("updatedon", updatedon);
            packingDoListMap.put("companyid", companyid);
            KwlReturnObject doresult = accInvoiceDAOobj.savePackingDoList(packingDoListMap);
            packingDoList = (PackingDoList) doresult.getEntityList().get(0);
            packingDoListMap.put("id", packingDoList.getID());
            packedDoListDetailsList = savePackingDoListRows(request, packingDoList, companyid, packingDoListId);
            HashSet packedDoListDetails = ((HashSet) packedDoListDetailsList.get(0));
            packingDoList.setRows(packedDoListDetails);
            boolean isEdit = StringUtil.isNullOrEmpty(request.getParameter("isEdit")) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
            boolean isCopy = StringUtil.isNullOrEmpty(request.getParameter("copyInv")) ? false : Boolean.parseBoolean(request.getParameter("copyInv"));
            String actionMsg = "added new";
            if (isEdit == true && isCopy == false) {
                actionMsg = "updated";
            }
            auditTrailObj.insertAuditLog(AuditAction.DELIVERY_ORDER, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + actionMsg + " Packing Do List " + packingDoList.getPackNumber(), request, packingDoList.getID());

        } catch (AccountingException ex) {
            throw new AccountingException(ex.getMessage());
        } catch (Exception ex) {
            String msg = "savePackingDoList : " + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = "savePackingDoList : " + ex.getCause().getMessage();
            }
            throw ServiceException.FAILURE(msg, ex);
        }
        return packingDoList;
    }

    public Packing savePacking(HttpServletRequest request, String packingId) throws SessionExpiredException, ServiceException, AccountingException, JSONException, UnsupportedEncodingException {
        Packing packing = null;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String entryNumber = request.getParameter("packNumber") != null ? request.getParameter("packNumber") : request.getParameter("number");
            String status = request.getParameter("statuscombo");
            String billid = request.getParameter("billid");
            String sequenceformat = request.getParameter("sequenceformat");
            String createdby = sessionHandlerImpl.getUserid(request);
            String modifiedby = sessionHandlerImpl.getUserid(request);
            long createdon = System.currentTimeMillis();
            long updatedon = System.currentTimeMillis();
            DateFormat df = authHandler.getDateOnlyFormat(request);
            List packedDoListDetailsList = null;
            HashMap<String, Object> packingMap = new HashMap<String, Object>();
            if (!StringUtil.isNullOrEmpty(billid)) { //Need to write Edit Case
            }
            Map<String, Object> seqNumberMap = new HashMap<String, Object>();
            int seqNumber = 0;
            synchronized (this) {
                if (StringUtil.isNullOrEmpty(billid)) { //Create new case
                    KwlReturnObject dopackinglistcnt = accInvoiceDAOobj.getPackingListCount(entryNumber, companyid);
                    if (dopackinglistcnt.getRecordTotalCount() > 0) {
                        throw new AccountingException("Packing DO List number '" + entryNumber + "' already exists.");
                    }
                }
                if (!sequenceformat.equals("NA")) {
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_PACKINGDO, sequenceformat, false, df.parse(request.getParameter("packingDate")));
                    entryNumber = seqNumberMap.containsKey(Constants.AUTO_ENTRYNUMBER) ? (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER) : "";
                    if (seqNumberMap.containsKey(Constants.SEQNUMBER) && !StringUtil.isNullObject(seqNumberMap.get(Constants.SEQNUMBER)) && !StringUtil.isNullOrEmpty(seqNumberMap.get(Constants.SEQNUMBER).toString())) {
                        seqNumber = Integer.parseInt(seqNumberMap.get(Constants.SEQNUMBER).toString());
                    }
                    packingMap.put("seqnumber", seqNumber);
                } else {
                    List list = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_PackingDO_ModuleId, entryNumber, companyid);
                    if (!list.isEmpty()) {
                        boolean isvalidEntryNumber = (Boolean) list.get(0);
                        String formatName = (String) list.get(1);
                        String language = RequestContextUtils.getLocale(request).getLanguage() + "_" + RequestContextUtils.getLocale(request).getCountry();
                        if (!isvalidEntryNumber) {
                            throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, Locale.forLanguageTag(language)) + " <b>" + entryNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, Locale.forLanguageTag(language)) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null, Locale.forLanguageTag(language)) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null, Locale.forLanguageTag(language)));
                        }
                    }
                }
            }
            packingMap.put("entrynumber", entryNumber);
            packingMap.put("autogenerate", sequenceformat.equals("NA") ? false : true);
            packingMap.put("sequenceformat", sequenceformat);
            packingMap.put("memo", request.getParameter("memo"));
            packingMap.put("customer", request.getParameter("customer"));
            packingMap.put("packingdate", df.parse(request.getParameter("packingDate")));
            packingMap.put("status", status);
            packingMap.put("companyid", companyid);
            KwlReturnObject doresult = accInvoiceDAOobj.savePacking(packingMap);
            packing = (Packing) doresult.getEntityList().get(0);
            packingMap.put("id", packing.getID());
            packedDoListDetailsList = savePackingRows(request, packing, companyid, packingId);
            HashSet packedDoListDetails = ((HashSet) packedDoListDetailsList.get(0));
            packing.setRows(packedDoListDetails);
            boolean isEdit = StringUtil.isNullOrEmpty(request.getParameter("isEdit")) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
            boolean isCopy = StringUtil.isNullOrEmpty(request.getParameter("copyInv")) ? false : Boolean.parseBoolean(request.getParameter("copyInv"));
            String actionMsg = "added new";
            if (isEdit == true && isCopy == false) {
                actionMsg = "updated";
            }
            auditTrailObj.insertAuditLog(AuditAction.DELIVERY_ORDER, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + actionMsg + " Packing Do List " + packing.getPackNumber(), request, packing.getID());
        } catch (AccountingException ex) {
            throw new AccountingException(ex.getMessage());
        } catch (Exception ex) {
            String msg = "savePacking : " + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = "savePacking : " + ex.getCause().getMessage();
            }
            throw ServiceException.FAILURE(msg, ex);
        }
        return packing;
    }

    public void setDOStatus(Map<String, Object> map) throws ServiceException {
        if (map.containsKey("packingid")) {
            try {
//                String detailjson = request.getParameter("detail") == null ? "" : request.getParameter("detail").toString();
//                JSONArray detailarray = new JSONArray(detailjson);
                String packingid = (String) map.get("packingid");
                String dueqtystring = (String) map.get("duequantity");
                String doid = (String) map.get("doid");
                String companyid = (String) map.get("companyid");
                Map<String, String> dopackedshippedparams = new HashMap<>();
                dopackedshippedparams.put("doid", doid);
                dopackedshippedparams.put("companyid", companyid);
                dopackedshippedparams.put("issavepacking", "true");
                double duequantity = 0.0;
                boolean ispack = true;
                String currentproductid = "";
                KwlReturnObject packingresult = accountingHandlerDAOobj.getObject(Packing.class.getName(), packingid);
                Packing packing = (Packing) packingresult.getEntityList().get(0);
                Set<PackingDetail> packingdetails = packing.getRows();
                Set<String> currentproducts = new HashSet<>();
                /**
                 * If multiple products exist in DO but only a single is packed
                 * check if the rest of the products are packed before updating
                 * the DO status
                 */
                JSONArray dopackedshippedarr = accInvoiceDAOobj.getDOPackedShippedQty(dopackedshippedparams);
                Map<String, Object> dopickqtymap = new HashMap<String, Object>();
                dopickqtymap.put("doid", doid);
                Double dopickqty  = accInvoiceDAOobj.getDOPickedQty(dopickqtymap);
                DeliveryOrderDetail dodetail = null;
      /*  Following code commented due to ERP-38974 and to avoid extra iteration 
                for(int i=0;i<detailarray.length();i++){
                    JSONObject finaldetailobj = detailarray.getJSONObject(i);                    
                    for (PackingDetail packingdetail : packingdetails) {
                        if (packingdetail.getProduct().getID().equalsIgnoreCase(finaldetailobj.optString("productid"))) {
                            double actqty = packingdetail.getActualQuantity();
                            double packqty = packingdetail.getPackingQuantity();
                            duequantity = (finaldetailobj.optInt("dquantity") - finaldetailobj.optInt("packedqty")) - packqty;
                            currentproductid = packingdetail.getProduct().getID();
                            if (actqty != packqty) {                                

                                if (duequantity != 0.0) {
                                    ispack = false;
                                    break;
                                }
                            }                            

                            currentproducts.add(currentproductid);
                            dodetail = packingdetail.getDodetailid();
                        }
                    }
                    if(!ispack){
                        break;
                    }
                }

                if (dopackedshippedarr.length() != currentproducts.size()) {   //process only if all products are not packed in current transaction 
                    for (int rs = 0; rs < dopackedshippedarr.length(); rs++) {
                        JSONObject jsonobj = dopackedshippedarr.getJSONObject(rs);
                        String jsonproduct = jsonobj.optString("product");
                        if (!currentproducts.contains(jsonproduct)) {
                            double actualqty = Double.parseDouble(jsonobj.optString("actualqty") == "" ? "0" : jsonobj.optString("actualqty"));
                            double packedqty = Double.parseDouble(jsonobj.optString("packedqty") == "" ? "0" : jsonobj.optString("packedqty"));
                            if (actualqty != packedqty) {
                                ispack = false;
                            }
                        }
                    }
                }  */
//                double dqty = 0.0;
//                for(int i=0;i<detailarray.length();i++){
//                    JSONObject finaldetailobj = detailarray.getJSONObject(i);
//                    dqty  = dqty + finaldetailobj.optDouble("dquantity");                    
//                }
                JSONObject jsonobj = dopackedshippedarr.getJSONObject(0);
                if(dopickqty!=jsonobj.optDouble("packedqty")){
                    ispack = false;
                }
                DeliveryOrder deliveryOrder = null;
                if(!StringUtil.isNullOrEmpty(doid)){
                    KwlReturnObject doresult = accountingHandlerDAOobj.getObject(DeliveryOrder.class.getName(), doid);
                    deliveryOrder = (DeliveryOrder) doresult.getEntityList().get(0);
                    currentproducts = null;
                    dopackedshippedarr = null;
                }
                

                /**
                 * If ispack = true then update status
                 */
                if (ispack && !StringUtil.isNullObject(deliveryOrder)) {
                    map.put("deliveryOrder", deliveryOrder);
                    map.put("companyid", companyid);
                    map.put("ispack", ispack);
                    accInvoiceDAOobj.updateDeliveryOrderStatus(map);
                }
            } catch (Exception ex) {

                throw ServiceException.FAILURE("setDOstatus : " + ex.getMessage(), ex);
            }

        }

    }

    public List savePackingDoListRows(HttpServletRequest request, PackingDoList packingDoList, String companyid, String packingDoListId) throws ServiceException, AccountingException, SessionExpiredException, ParseException, UnsupportedEncodingException {
        List returnList = new ArrayList();
        HashSet rows = new HashSet();
        try {
            JSONArray jArr = new JSONArray(request.getParameter("detail"));
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            List packedDoListDoDetailsList = null;
            List packedDoListPackingDetailsList = null;
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                HashMap<String, Object> packingDoListDetailsMap = new HashMap<String, Object>();
                packingDoListDetailsMap.put("srno", i + 1);
                packingDoListDetailsMap.put("companyid", companyid);
                packingDoListDetailsMap.put("packingDoList", packingDoList.getID());
                packingDoListDetailsMap.put("productid", jobj.getString("productid"));
                String description = "";
                double quantityindo = 0;
                quantityindo = jobj.optDouble("dquantity");
                double shipquantity = 0;
                shipquantity = jobj.optDouble("shipquantity");
                description = jobj.getString("description");
                packingDoListDetailsMap.put("description", description);
                packingDoListDetailsMap.put("quantityindo", quantityindo);
                packingDoListDetailsMap.put("shipquantity", shipquantity);
                packingDoListDetailsMap.put("remark", jobj.optString("remark"));
                packingDoListDetailsMap.put("deliveryorder", jobj.optString("billid"));
                KwlReturnObject result = accInvoiceDAOobj.savePackingDolistDetails(packingDoListDetailsMap);
                PackingDoListDetail row = (PackingDoListDetail) result.getEntityList().get(0);
                if (jobj.has("packingdodetails") && jobj.getString("packingdodetails") != null) {
                    packedDoListDoDetailsList = savePackingDoListDoDetailsRows(request, packingDoList, companyid, row, jobj.getString("packingdodetails"));
                    HashSet packedDoListDoDetails = ((HashSet) packedDoListDoDetailsList.get(0));
                    row.setShipingdodetails(packedDoListDoDetails);
                }
                if (jobj.has("packingdetails") && jobj.getString("packingdetails") != null) {
                    packedDoListPackingDetailsList = savePackingDoListPackingRows(request, packingDoList, companyid, row, jobj.getString("packingdetails"));
                    HashSet packedDoListPackingDetails = ((HashSet) packedDoListPackingDetailsList.get(0));
                    row.setPackingdetails(packedDoListPackingDetails);
                }
                rows.add(row);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveDeliveryOrderRows : " + ex.getMessage(), ex);
        }
        returnList.add(rows);
        return returnList;
    }
    /*
     Function to save packing row details
     */

    public List savePackingRows(HttpServletRequest request, Packing packing, String companyid, String packingDoListId) throws ServiceException, AccountingException, SessionExpiredException, ParseException, UnsupportedEncodingException {
        List returnList = new ArrayList();
        HashSet rows = new HashSet();
        try {
            JSONArray jArr = new JSONArray(request.getParameter("detail"));
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            List packedDoDetailsList = null;
            List PackingDetailsList = null;
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                HashMap<String, Object> packingDetailsMap = new HashMap<String, Object>();
                packingDetailsMap.put("srno", i + 1);
                packingDetailsMap.put("companyid", companyid);
                packingDetailsMap.put("packingDoList", packing.getID());
                packingDetailsMap.put("productid", jobj.getString("productid"));
                double quantityindo = 0;
                quantityindo = jobj.optDouble("dquantity");
                double packquantity = 0;
                packquantity = jobj.optDouble("shipquantity");
                packingDetailsMap.put("description", StringUtil.decodeString(jobj.optString("description")));
                packingDetailsMap.put("packageNumber", StringUtil.decodeString(jobj.optString("packageNumber")));
                packingDetailsMap.put("quantityindo", quantityindo);
                packingDetailsMap.put("packquantity", packquantity);
                packingDetailsMap.put("remark", jobj.optString("remark"));
                packingDetailsMap.put("deliveryorder", jobj.optString("billid"));
                packingDetailsMap.put("deliveryorderdetail", jobj.optString("rowid"));
                KwlReturnObject result = accInvoiceDAOobj.savePackingDetails(packingDetailsMap);
                PackingDetail row = (PackingDetail) result.getEntityList().get(0);
                if (jobj.has("packingdodetails") && jobj.getString("packingdodetails") != null && !StringUtil.isNullOrEmpty(jobj.getString("packingdodetails"))) {
                    packedDoDetailsList = savePackingDoDetails(request, packing, companyid, row, jobj.getString("packingdodetails"));
                    HashSet packedDoListDoDetails = ((HashSet) packedDoDetailsList.get(0));
                    row.setDodetails(packedDoListDoDetails);
                }
                if (jobj.has("packingdetails") && jobj.getString("packingdetails") != null && !StringUtil.isNullOrEmpty(jobj.getString("packingdetails"))) {
                    PackingDetailsList = savePackingItemDetails(request, packing, companyid, row, jobj.getString("packingdetails"));
                    HashSet packedDoListPackingDetails = ((HashSet) PackingDetailsList.get(0));
                    row.setPackingdetails(packedDoListPackingDetails);
                }
                rows.add(row);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("savePackingRows : " + ex.getMessage(), ex);
        }
        returnList.add(rows);
        return returnList;
    }

    public List savePackingDoListDoDetailsRows(HttpServletRequest request, PackingDoList packingDoList, String companyid, PackingDoListDetail packingDoListDetail, String packingdodetails) throws ServiceException, AccountingException, SessionExpiredException, ParseException, UnsupportedEncodingException {
        List returnList = new ArrayList();
        HashSet rows = new HashSet();
        try {
            JSONArray jArr = new JSONArray(packingdodetails);
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);

            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                HashMap<String, Object> packingDoDetailsMap = new HashMap<String, Object>();
                packingDoDetailsMap.put("srno", i + 1);
                packingDoDetailsMap.put("companyid", companyid);
                packingDoDetailsMap.put("packingDoList", packingDoList.getID());
                packingDoDetailsMap.put("packingDoListDetail", packingDoListDetail.getID());
                packingDoDetailsMap.put("productid", jobj.getString("productid"));
                packingDoDetailsMap.put("productname", jobj.getString("productname"));
                packingDoDetailsMap.put("deliveryorder", jobj.getString("billid"));
                double duequantity = 0;
                double quantityindo = 0;
                quantityindo = jobj.optDouble("quantityindo");
                double shipquantity = 0;
                shipquantity = jobj.optDouble("shipquantity");
                duequantity = jobj.optDouble("duequantity");
                packingDoDetailsMap.put("quantityindo", quantityindo);
                packingDoDetailsMap.put("shipquantity", shipquantity);
                packingDoDetailsMap.put("shipedquantity", (quantityindo - duequantity) + shipquantity);
                KwlReturnObject result = accInvoiceDAOobj.saveShipingDoDetails(packingDoDetailsMap);
                ShipingDoDetails row = (ShipingDoDetails) result.getEntityList().get(0);
                rows.add(row);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveDeliveryOrderRows : " + ex.getMessage(), ex);
        }
        returnList.add(rows);
        return returnList;
    }
    /*
     Function to save packing DO details
     */

    public List savePackingDoDetails(HttpServletRequest request, Packing packing, String companyid, PackingDetail packingDetail, String packingdodetails) throws ServiceException, AccountingException, SessionExpiredException, ParseException, UnsupportedEncodingException {
        List returnList = new ArrayList();
        HashSet rows = new HashSet();
        try {
            JSONArray jArr = new JSONArray(packingdodetails);
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                HashMap<String, Object> packingDoDetailsMap = new HashMap<String, Object>();
                packingDoDetailsMap.put("srno", i + 1);
                packingDoDetailsMap.put("companyid", companyid);
                packingDoDetailsMap.put("packingDoList", packing.getID());
                packingDoDetailsMap.put("packingDoListDetail", packingDetail.getID());
                packingDoDetailsMap.put("productid", jobj.getString("productid"));
                packingDoDetailsMap.put("productname", jobj.getString("productname"));
                packingDoDetailsMap.put("deliveryorder", jobj.getString("billid"));
                double quantityindo = 0;
                quantityindo = jobj.optDouble("quantityindo");
                double shipquantity = 0;
                shipquantity = jobj.optDouble("shipquantity");
                packingDoDetailsMap.put("quantityindo", quantityindo);
                packingDoDetailsMap.put("packquantity", shipquantity);
                KwlReturnObject result = accInvoiceDAOobj.savePackingDoDetails(packingDoDetailsMap);
                DoDetails row = (DoDetails) result.getEntityList().get(0);
                rows.add(row);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("savePackingDoDetails : " + ex.getMessage(), ex);
        }
        returnList.add(rows);
        return returnList;
    }

    public List savePackingDoListPackingRows(HttpServletRequest request, PackingDoList packingDoList, String companyid, PackingDoListDetail packingDoListDetail, String packingdetails) throws ServiceException, AccountingException, SessionExpiredException, ParseException, UnsupportedEncodingException {
        List returnList = new ArrayList();
        HashSet rows = new HashSet();
        try {
            JSONArray jArr = new JSONArray(packingdetails);
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);

            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                HashMap<String, Object> packingDetailsMap = new HashMap<String, Object>();
                packingDetailsMap.put("srno", i + 1);
                packingDetailsMap.put("companyid", companyid);
                packingDetailsMap.put("packingDoList", packingDoList.getID());
                packingDetailsMap.put("packingDoListDetail", packingDoListDetail.getID());
                packingDetailsMap.put("productid", jobj.getString("productid"));
                packingDetailsMap.put("productname", jobj.getString("productname"));
                packingDetailsMap.put("description", jobj.getString("description"));
                packingDetailsMap.put("packageid", jobj.getString("packages"));
                packingDetailsMap.put("packagemeasurement", jobj.getString("packagemeasurement"));
                double packagequantity = jobj.optDouble("packagequantity");
                double packageperquantity = jobj.optDouble("packageperquantity");
                double grossweight = jobj.optDouble("grossweight");
                double packageweight = jobj.optDouble("packageweight");
                double productweight = jobj.optDouble("productweight");
                double totalpackagequantity = jobj.optDouble("totalpackagequantity");
                packingDetailsMap.put("packagequantity", packagequantity);
                packingDetailsMap.put("packageperquantity", packageperquantity);
                packingDetailsMap.put("grossweight", grossweight);
                packingDetailsMap.put("packageweight", packageweight);
                packingDetailsMap.put("productweight", productweight);
                packingDetailsMap.put("totalpackagequantity", totalpackagequantity);
                packingDetailsMap.put("deliveryorder", jobj.optString("billid"));
                KwlReturnObject result = accInvoiceDAOobj.savePackingDoListPackingDetails(packingDetailsMap);
                ItemPackingDetail row = (ItemPackingDetail) result.getEntityList().get(0);
                rows.add(row);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveDeliveryOrderRows : " + ex.getMessage(), ex);
        }
        returnList.add(rows);
        return returnList;
    }
    /*
     Function to save packing details
     */

    public List savePackingItemDetails(HttpServletRequest request, Packing packing, String companyid, PackingDetail packingDetail, String packingdetails) throws ServiceException, AccountingException, SessionExpiredException, ParseException, UnsupportedEncodingException {
        List returnList = new ArrayList();
        HashSet rows = new HashSet();
        try {
            JSONArray jArr = new JSONArray(packingdetails);
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                HashMap<String, Object> packingItemDetailsMap = new HashMap<String, Object>();
                packingItemDetailsMap.put("srno", i + 1);
                packingItemDetailsMap.put("companyid", companyid);
                packingItemDetailsMap.put("packingDoList", packing.getID());
                packingItemDetailsMap.put("packingDoListDetail", packingDetail.getID());
                packingItemDetailsMap.put("productid", jobj.getString("productid"));
                packingItemDetailsMap.put("productname", jobj.getString("productname"));
                packingItemDetailsMap.put("productweight", jobj.getString("productweight"));
                packingItemDetailsMap.put("description", jobj.getString("description"));
                packingItemDetailsMap.put("packageid", jobj.getString("packages"));
                packingItemDetailsMap.put("packagemeasurement", jobj.getString("packagemeasurement"));
                double packagequantity = jobj.optDouble("packagequantity");
                double packageperquantity = jobj.optDouble("packageperquantity");
                double grossweight = jobj.optDouble("grossweight");
                double packageweight = jobj.optDouble("packageweight");
                double productweight = jobj.optDouble("productweight");
                double totalpackagequantity = jobj.optDouble("totalpackagequantity");
                packingItemDetailsMap.put("packagequantity", packagequantity);
                packingItemDetailsMap.put("packageperquantity", packageperquantity);
                packingItemDetailsMap.put("grossweight", grossweight);
                packingItemDetailsMap.put("packageweight", packageweight);
                packingItemDetailsMap.put("productweight", productweight);
                packingItemDetailsMap.put("totalpackagequantity", totalpackagequantity);
                packingItemDetailsMap.put("deliveryorder", jobj.optString("billid"));
                KwlReturnObject result = accInvoiceDAOobj.savePackingItemDetails(packingItemDetailsMap);
                ItemDetail row = (ItemDetail) result.getEntityList().get(0);
                rows.add(row);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("savePackingItemDetails : " + ex.getMessage(), ex);
        }
        returnList.add(rows);
        return returnList;
    }

    public ModelAndView deletePackingDoListsPermanent(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String linkedTransaction = deletePackingDoListsPermanent(request);
            txnManager.commit(status);
            issuccess = true;
            if (StringUtil.isNullOrEmpty(linkedTransaction)) {
                msg = messageSource.getMessage("acc.PackingDolist.del", null, RequestContextUtils.getLocale(request));   //"Delivery Order has been deleted successfully";
            }
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    /*
     Function to delete packing
     */

    public ModelAndView deletePackingPermanent(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String linkedTransaction = deletePackingPermanent(request);
            txnManager.commit(status);
            issuccess = true;
            if (StringUtil.isNullOrEmpty(linkedTransaction)) {
                msg = messageSource.getMessage("acc.Packing.del", null, RequestContextUtils.getLocale(request));   //"Delivery Order has been deleted successfully";
            } else {
                String[] linkedTransactions = linkedTransaction.split(",");
                msg = linkedTransactions[0] + " " + messageSource.getMessage("acc.PackingShipping.del", null, null);
                if (linkedTransactions.length == 1) {
                    issuccess = false;
                }
            }
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public String deletePackingDoListsPermanent(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        String linkedTransaction = "";
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String packId = "", packNo = "";
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                packId = StringUtil.DecodeText(jobj.optString("billid"));
                packNo = jobj.getString("billno");
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("packId", packId);
                requestParams.put("companyid", companyid);
                requestParams.put("packNo", packNo);
                accInvoiceDAOobj.deletePackingDoListsPermanent(requestParams);
                auditTrailObj.insertAuditLog(AuditAction.Packing_Do_List, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted a Packing Do List Permanently " + packNo, request, packId);
            }
        } /*catch (UnsupportedEncodingException ex) {
         throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
         }*/ catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)));
        }
        return linkedTransaction;
    }

    public String deletePackingPermanent(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        String linkedTransaction = "";
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String packId = "", packNo = "";
            boolean ispick = true;
            double shipqty = 0.0;
            String ispackdetailsdeleted = "";
            StringBuilder appstring = new StringBuilder();
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                packId = StringUtil.DecodeText(jobj.optString("billid"));
                packNo = jobj.getString("billno");
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("packId", packId);
                requestParams.put("companyid", companyid);
                requestParams.put("packNo", packNo);
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("packing.ID", packId);
                List packingresult = kwlCommonTablesDAOObj.getRequestedObjectFieldsInCollection(PackingDetail.class, new String[]{"dodetailid"}, paramMap);
                if (packingresult != null && packingresult.get(0) != null) {
                    DeliveryOrderDetail packing = (DeliveryOrderDetail) packingresult.get(0);
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("deliveryOrder", packing.getDeliveryOrder());
                    map.put("companyid", companyid);
                    map.put("is_pick", ispick);
                    map.put("dodid", packing.getID());
                    shipqty = accInvoiceDAOobj.getShippingQuantity(map);
                    if (shipqty == 0.0) {
                        KwlReturnObject packingdeleted = accInvoiceDAOobj.deletePackingPermanent(requestParams);
                        if (!StringUtil.isNullObject(packingdeleted) && packingdeleted.isSuccessFlag()) {
                            accInvoiceDAOobj.updateDeliveryOrderStatus(map);
                        }
                        auditTrailObj.insertAuditLog(AuditAction.Packing_Do_List, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted a Packing Permanently " + packNo, request, packId);
                        ispackdetailsdeleted = "true";
                    } else {
                        appstring.append("Delivery Order " + packing.getDeliveryOrder().getDeliveryOrderNumber() + " ");
                    }
                }
            }
            if (!StringUtil.isNullOrEmpty(appstring.toString())) {
                if (!StringUtil.isNullOrEmpty(ispackdetailsdeleted)) {
                    appstring.append("," + ispackdetailsdeleted);
                }
                linkedTransaction = appstring.toString();
            }
        }/* catch (UnsupportedEncodingException ex) {
         throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
         } */ catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)));
        }
        return linkedTransaction;
    }

    public ModelAndView approveInvoice(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "<ol style='list-style: inside none inside; padding: 5px;'>";
        String notAuthorisedMsg = "<ol style='list-style: inside none inside; padding: 5px;'>";
        String userMsg = "";
        String combineUseMsg = "";
        String notAuthorisedUserMsg = "";
        boolean issuccess = false;
        boolean isAccountingExe = false;
        JSONArray pendingTransArray = new JSONArray();
        TransactionStatus status = null;
        StringBuffer productIds = new StringBuffer();
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            String currentUser = sessionHandlerImpl.getUserid(request);
            String remark = request.getParameter("remark");
            String doID = "";
            String detail = request.getParameter("data");
            String companyid = "";
            String recurredInvoiceApproverID = "";
            String roleName = "Company User";
            double amount = 0;
            if (!StringUtil.isNullOrEmpty(detail)) {
                pendingTransArray = new JSONArray(detail);
            }
            boolean iscash = StringUtil.isNullOrEmpty(request.getParameter("incash")) ? false : Boolean.parseBoolean(request.getParameter("incash"));
            companyid = sessionHandlerImpl.getCompanyid(request);
            /*Below for loop is used to approve transactions in batch*/
            for (int i = 0; i < pendingTransArray.length(); i++) {

                JSONObject jobj1 = pendingTransArray.getJSONObject(i);
                amount = authHandler.round(jobj1.optDouble("totalorderamount", 0), companyid);//StringUtil.isNullOrEmpty(request.getParameter("totalorderamount")) ? 0 : authHandler.round(Double.parseDouble(request.getParameter("totalorderamount")), Constants.AMOUNT_DIGIT_AFTER_DECIMAL);
                recurredInvoiceApproverID = jobj1.optString("recurredinvoiceapproverid", "");//(!StringUtil.isNullOrEmpty(request.getParameter("recurredinvoiceapproverid"))) ? request.getParameter("recurredinvoiceapproverid") : "";
                doID = jobj1.optString("billid", "");

                if (!StringUtil.isNullOrEmpty(doID)) {
                    KwlReturnObject CQObj = accountingHandlerDAOobj.getObject(Invoice.class.getName(), doID);
                    Invoice cqObj = (Invoice) CQObj.getEntityList().get(0);
                    KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                    CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
                    KwlReturnObject extraCap = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
                    ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraCap.getEntityList().get(0);

                    HashMap<String, Object> invApproveMap = new HashMap<String, Object>();
                    int level = cqObj.getApprovestatuslevel();
                    String currencyid = cqObj.getCurrency() != null ? cqObj.getCurrency().getCurrencyID() : sessionHandlerImpl.getCurrencyID(request);
                    // Add Product and discounts mapping
                    HashMap<String, Object> GlobalParams = AccountingManager.getGlobalParams(request);
                    JSONArray productDiscountJArr = new JSONArray();
                    Set<InvoiceDetail> invoiceDetails = cqObj.getRows();
                    for (InvoiceDetail invDetail : invoiceDetails) {
                        if (invDetail.getInventory() != null) {
                            String productId = invDetail.getInventory().getProduct().getID();
                            Discount invDiscount = invDetail.getDiscount();
                            double discAmountinBase = 0;
                            if (invDiscount != null) {
                                double discountVal = invDiscount.getDiscountValue();
//                                KwlReturnObject dAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, discountVal, currencyid, cqObj.isIsOpeningBalenceInvoice() && cqObj.getCreationDate() != null ? cqObj.getCreationDate() : cqObj.getJournalEntry().getEntryDate(), cqObj.getExternalCurrencyRate());
                                KwlReturnObject dAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, discountVal, currencyid, cqObj.getCreationDate(), cqObj.getExternalCurrencyRate());
                                discAmountinBase = (Double) dAmount.getEntityList().get(0);
                            }
                            discAmountinBase = authHandler.round(discAmountinBase, companyid);
                            JSONObject productDiscountObj = new JSONObject();
                            productDiscountObj.put("productId", productId);
                            productDiscountObj.put("discountAmount", discAmountinBase);
                            productDiscountJArr.put(productDiscountObj);
                        }
                    }
                    invApproveMap.put("companyid", companyid);
                    invApproveMap.put("level", level);
                    invApproveMap.put("totalAmount", String.valueOf(cqObj.getInvoiceamountinbase()));
                    invApproveMap.put("currentUser", currentUser);
                    invApproveMap.put("fromCreate", false);
                    invApproveMap.put("productDiscountMapList", productDiscountJArr);
                    invApproveMap.put("moduleid", Constants.Acc_Invoice_ModuleId);
                    invApproveMap.put("iscash", iscash);
                    invApproveMap.put("recurredinvoiceapproverid", recurredInvoiceApproverID);
                    invApproveMap.put(Constants.PAGE_URL, URLUtil.getPageURL(request, loginpageFull));
                    int approvedLevel = 0;
                    String JENumber = "";
                    String JEMsg = "";
                    String avalaraMsg = "";
                    DefaultTransactionDefinition def1 = new DefaultTransactionDefinition();
                    def1.setName("PO_Tx");
                    def1.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
                    TransactionStatus syncstatus = txnManager.getTransaction(def1);
                    synchronized (this) {
                        try {
                            List approvedLevelList = accInvoiceModuleService.approveInvoice(cqObj, invApproveMap, true);
                            approvedLevel = (Integer) approvedLevelList.get(0);
                            String postingDateStr = request.getParameter("postingDate");
                            DateFormat df = authHandler.getDateOnlyFormat();
                            Date postingDate = null;
                            if (!StringUtil.isNullOrEmpty(postingDateStr)) {
                                postingDate = df.parse(postingDateStr);
                            }
                            String jeID = cqObj.getJournalEntry().getID();
                            String jeCompany = cqObj.getJournalEntry().getCompany().getCompanyID();
                            if (approvedLevel == 11) {
                                if (StringUtil.isNullOrEmpty(cqObj.getJournalEntry().getEntryNumber())) {
                                    int isApproved = 0;
                                    HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                                    JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                                    JEFormatParams.put("modulename", CompanyPreferencesConstants.AUTOJOURNALENTRY);
                                    JEFormatParams.put("companyid", companyid);
                                    JEFormatParams.put("isdefaultFormat", true);
                                    KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                                    SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
//                                    String JENumBer = updateJEEntryNumberForNewJE(request, cqObj.getJournalEntry(), companyid, format.getID(), isApproved);
                                    String JENumBer = "";
                                    KwlReturnObject returnObj = updateJEEntryNumberForNewJE(request, cqObj.getJournalEntry(), companyid, format.getID(), isApproved);
                                    if (returnObj.isSuccessFlag() && returnObj.getRecordTotalCount() > 0) {
                                        JENumBer = (String) returnObj.getEntityList().get(0);
                                    } else if (!returnObj.isSuccessFlag()) {
                                        throw new AccountingException((String) returnObj.getEntityList().get(0));
                                    }
                                } else {
                                    JSONObject jeJobj = new JSONObject();
                                    HashSet<JournalEntryDetail> details = new HashSet<JournalEntryDetail>();
                                    jeJobj.put("jeid", jeID);
                                    jeJobj.put(JournalEntryConstants.COMPANYID, jeCompany);
                                    jeJobj.put("pendingapproval", 0);
                                    Map<String, Object> paramMap = new HashMap<>();
                                    paramMap.put("id", companyid);
                                    Object exPrefObject = kwlCommonTablesDAOObj.getRequestedObjectFields(ExtraCompanyPreferences.class, new String[]{"columnPref"}, paramMap);
                                    JSONObject jObj = StringUtil.isNullObject(exPrefObject) ? new JSONObject() : new JSONObject(exPrefObject.toString());
                                    boolean isPostingDateCheck = false;
                                    if (!StringUtil.isNullObject(jObj) && jObj.has(Constants.IS_POSTING_DATE_CHECK) && jObj.get(Constants.IS_POSTING_DATE_CHECK) != null && jObj.optBoolean(Constants.IS_POSTING_DATE_CHECK, false)) {
                                        isPostingDateCheck = true;
                                    }
                                    if (isPostingDateCheck) {
                                        jeJobj.put("entrydate", postingDate);
                                    }
                                    accJournalEntryobj.updateJournalEntry(jeJobj, details);
                                }
                                JENumber = " with JE No. " + cqObj.getJournalEntry().getEntryNumber();
                                JEMsg = " JE No : <b>" + cqObj.getJournalEntry().getEntryNumber() + "</b>";
                                // Approve Auto Generated DO if respective SI approved
                                KwlReturnObject InvoiceDo = accInvoiceDAOobj.getAutogeneratedDOFromInvoices(cqObj.getID(), companyid);
                                if (InvoiceDo.getEntityList() != null && InvoiceDo.getEntityList().size() > 0) {
                                    Object[] oj = (Object[]) InvoiceDo.getEntityList().get(0);
                                    String DeliveryOrderID = oj[1].toString();
                                    KwlReturnObject dores = accountingHandlerDAOobj.getObject(DeliveryOrder.class.getName(), DeliveryOrderID);
                                    DeliveryOrder doObj = (DeliveryOrder) dores.getEntityList().get(0);
                                    if (doObj.getApprovestatuslevel() != 11) {
                                        accInvoiceDAOobj.approvePendingDO(DeliveryOrderID, companyid, 11);
                                        Set<DeliveryOrderDetail> doDetails = doObj.getRows();
                                        List<StockMovement> stockMovementsList = new ArrayList<>();
                                        for (DeliveryOrderDetail doDetail : doDetails) {
                                            Product product = doDetail.getProduct();
                                            if ((product != null && !doObj.isIsconsignment()) && extraCompanyPreferences.isActivateInventoryTab() && (product.isIsBatchForProduct() || product.isIslocationforproduct() || product.isIswarehouseforproduct() || product.isIsrowforproduct() || product.isIsrackforproduct() || product.isIsbinforproduct() || product.isIsSerialForProduct())) {
                                                accInvoiceModuleService.newStockMovementDeliveryOrder(doDetail, stockMovementsList);
                                            }
                                            Inventory inventory = doDetail.getInventory();
                                            if (inventory.isInvrecord()) {
                                                inventory.setBaseuomquantity(inventory.getActquantity());
                                                inventory.getProduct().setAvailableQuantity(inventory.getProduct().getAvailableQuantity() - inventory.getBaseuomquantity());
                                                inventory.setActquantity(0.0);
                                            }
                                            if (product != null && productIds.indexOf(product.getID()) == -1) {
                                                productIds.append(product.getID()).append(",");
                                            }
                                        }
                                        if (extraCompanyPreferences != null && extraCompanyPreferences.isActivateInventoryTab() && !stockMovementsList.isEmpty()) {
                                            stockMovementService.addOrUpdateBulkStockMovement(doObj.getCompany(), doObj.getID(), stockMovementsList);
                                        }
                                    }
                                }
                                JSONObject paramsJobj = new JSONObject();
                                paramsJobj.put(IntegrationConstants.integrationPartyIdKey, IntegrationConstants.integrationPartyId_AVALARA);
                                paramsJobj.put(Constants.companyKey, companyid);
                                if (extraCompanyPreferences.isAvalaraIntegration() && extraCompanyPreferences.getLineLevelTermFlag() == 1) {
                                    if (integrationCommonService.isTaxCalculationEnabled(paramsJobj)) {
                                        /**
                                         * Commit taxes to Avalara and save
                                         * taxes only if Tax Committing is
                                         * enabled in System Controls If Tax
                                         * Committing is disabled but Tax
                                         * Calculation is enabled, then we only
                                         * calculate the taxes again (without
                                         * committing) to save we commit taxes
                                         * only when invoice is neither draft
                                         * not template invoice
                                         */
                                        boolean isCommit = integrationCommonService.isTaxCommittingEnabled(paramsJobj);
                                        isCommit = isCommit && (!cqObj.isDraft() && (cqObj.getIstemplate() != 2));
                                        JSONObject tempJobj = accInvoiceModuleService.commitTaxToAvalaraAndSave(paramJobj, cqObj, cqObj.getID(), cqObj.getInvoiceNumber(), companyid, false, "", isCommit);
                                        avalaraMsg = tempJobj.optString(Constants.RES_msg, msg);
                                    } else {
                                        avalaraMsg += "<br><br><b>NOTE:</b> " + messageSource.getMessage("acc.integration.taxNotCommitted", null, Locale.forLanguageTag(paramJobj.getString(Constants.language)));
                                    }
                                }
                            }
                            txnManager.commit(syncstatus);
                        } catch (Exception exception) {
                            txnManager.rollback(syncstatus);
                            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, exception);
                            throw new AccountingException(exception.getMessage(), exception);
                        }
                    }
                    DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                    def.setName("PO_Tx");
                    def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
                    status = txnManager.getTransaction(def);
                    String baseUrl = URLUtil.getPageURL(request, loginpageFull);
                    if (approvedLevel != Constants.NoAuthorityToApprove && preferences.isSendapprovalmail()) {//If allow to send approval mail in company account preferences
                        String userName = sessionHandlerImpl.getUserFullName(request);
                        KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                        Company company = (Company) returnObject.getEntityList().get(0);
                        String sendorInfo = (!company.isEmailFromCompanyCreator()) ? Constants.ADMIN_EMAILID : authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                        String creatormail = company.getCreator().getEmailID();
                        String documentcreatoremail = (cqObj != null && cqObj.getCreatedby() != null) ? cqObj.getCreatedby().getEmailID() : "";
                        ArrayList<String> emailArray = new ArrayList<>();
                        String fname = company.getCreator().getFirstName() == null ? "" : company.getCreator().getFirstName();
                        String lname = company.getCreator().getLastName() == null ? "" : company.getCreator().getLastName();
                        String ruleId = "";
                        String creatorname = fname + " " + lname;
                        String approvalpendingStatusmsg = "";
                        HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                        qdDataMap.put(Constants.companyKey, companyid);
                        qdDataMap.put("level", level);
                        qdDataMap.put(Constants.moduleid, Constants.Acc_Invoice_ModuleId);
//                        emailArray =commonFnControllerService.getUserApprovalEmail(qdDataMap);
                        emailArray.add(creatormail);
                        if (!StringUtil.isNullOrEmpty(documentcreatoremail) && !creatormail.equalsIgnoreCase(documentcreatoremail)) {
                            emailArray.add(documentcreatoremail);
                        }
                        String[] emails = {};
                        emails = emailArray.toArray(emails);

                        if (!StringUtil.isNullOrEmpty(preferences.getApprovalEmails())) {
                            String[] compPrefMailIds = preferences.getApprovalEmails().split(",");
                            emails = AccountingManager.getMergedMailIds(emails, compPrefMailIds);
                        }
                        if (cqObj.getApprovestatuslevel() < 11) {
                            qdDataMap.put("ApproveMap", invApproveMap);
                            approvalpendingStatusmsg = commonFnControllerService.getApprovalstatusmsg(qdDataMap);
                        }
                        Map<String, Object> mailParameters = new HashMap();
                        mailParameters.put("Number", cqObj.getInvoiceNumber());
                        mailParameters.put("userName", userName);
                        mailParameters.put("emails", emails);
                        mailParameters.put("sendorInfo", sendorInfo);
                        mailParameters.put("moduleName", Constants.CUSTOMER_INVOICE);
                        mailParameters.put("addresseeName", "All");
                        mailParameters.put("companyid", companyid);
                        mailParameters.put("baseUrl", baseUrl);
                        mailParameters.put("approvalstatuslevel", cqObj.getApprovestatuslevel());
                        mailParameters.put("approvalpendingStatusmsg", approvalpendingStatusmsg);
                        if (emails.length > 0) {
//                            accountingHandlerDAOobj.sendApprovedEmails(cqObj.getInvoiceNumber(), userName, emails, sendorInfo, Constants.CUSTOMER_INVOICE, "All", companyid, baseUrl);
                            accountingHandlerDAOobj.sendApprovedEmails(mailParameters);
                        }
                    }
                    // Save Approval History
                    if (approvedLevel != Constants.NoAuthorityToApprove) {
                        HashMap<String, Object> hashMap = new HashMap<String, Object>();
                        hashMap.put("transtype", Constants.Invoice_APPROVAL);
                        hashMap.put("transid", cqObj.getID());
                        hashMap.put("approvallevel", cqObj.getApprovestatuslevel());//  If approvedLevel = 11 then its final Approval
                        hashMap.put("remark", remark);
                        hashMap.put("userid", sessionHandlerImpl.getUserid(request));
                        hashMap.put("companyid", companyid);
                        accountingHandlerDAOobj.updateApprovalHistory(hashMap);
                        // Audit log entry
                        String action = "Cash Sales ";
                        String auditaction = AuditAction.CASH_SALES_APPROVED;
                        if (!iscash) {
                            action = "Customer Invoice ";
                            auditaction = AuditAction.CUSTOMERINVOICEAPPROVED;
                        }
                        auditTrailObj.insertAuditLog(auditaction, "User " + sessionHandlerImpl.getUserFullName(request) + " has Approved a " + action + cqObj.getInvoiceNumber() + JENumber + " at Level-" + cqObj.getApprovestatuslevel(), request, cqObj.getID());

                        txnManager.commit(status);
                        issuccess = true;
                        KwlReturnObject kmsg = null;

                        kmsg = permissionHandlerDAOObj.getRoleofUser(sessionHandlerImpl.getUserid(request));
                        Iterator ite2 = kmsg.getEntityList().iterator();
                        while (ite2.hasNext()) {
                            Object[] row = (Object[]) ite2.next();
                            roleName = row[1].toString();
                        }
                        userMsg = roleName + " " + sessionHandlerImpl.getUserFullName(request) + " " + messageSource.getMessage("acc.field.transactionhasbeenapprovedsuccessfully", null, RequestContextUtils.getLocale(request));
                        if (!StringUtil.isNullOrEmpty(recurredInvoiceApproverID)) {
                            msg += "<li>" + messageSource.getMessage("acc.invapprovedsuccessfully", null, RequestContextUtils.getLocale(request)) + " by " + roleName + " " + sessionHandlerImpl.getUserFullName(request) + "</li>";
                        } else {
                            /*Message binding as per approval transaction*/
                            msg += "<li>" + messageSource.getMessage("acc.field.Invoice", null, RequestContextUtils.getLocale(request)) + "<b>" + cqObj.getInvoiceNumber() + "</b> " + " at Level " + cqObj.getApprovestatuslevel() + "." + JEMsg + "</li>";
                        }
                        msg += avalaraMsg;//Append Avalara tax commit message
                    } else {
                        txnManager.commit(status);
                        issuccess = true;
                        notAuthorisedUserMsg = roleName + " " + sessionHandlerImpl.getUserFullName(request) + " " + messageSource.getMessage("acc.field.transactionsarenotapproved", null, RequestContextUtils.getLocale(request));
                        if (!StringUtil.isNullOrEmpty(recurredInvoiceApproverID)) {
                            notAuthorisedMsg += "<li>" + messageSource.getMessage("acc.pendingrecurredinvoice.approver.notauthorizedmessage", null, RequestContextUtils.getLocale(request)) + "</li>";
                        } else {
                            /*Message binding as per user are not authorise to  approval transaction*/
                            notAuthorisedMsg += "<li>" + messageSource.getMessage("acc.field.Invoice", null, RequestContextUtils.getLocale(request)) + "<b>" + cqObj.getInvoiceNumber() + "</b> " + " at Level " + cqObj.getApprovestatuslevel() + "." + JEMsg + "</li>";
                        }
                    }
                }
            }
            msg += "</ol>";
            notAuthorisedMsg += "</ol>";
            msg = userMsg + msg;
            notAuthorisedMsg = notAuthorisedUserMsg + notAuthorisedMsg;
            combineUseMsg = msg + notAuthorisedMsg;
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AccountingException ae) {       //Please Note accounting exception should only be thrown in case of if JE posting date dose not belongs to lockin period.As further code is written only to handle AccountingException in case of lockin period.
            if (!StringUtil.isNullObject(status)) {
                txnManager.rollback(status);
            }
            isAccountingExe = true;
            combineUseMsg = ae.getMessage();
            combineUseMsg = combineUseMsg.replaceFirst("Transaction", "JE Posting");
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ae);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("productIds", productIds);
                jobj.put("msg", combineUseMsg);
                jobj.put("isAccountingExe", isAccountingExe);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getStockAvailabilityforAllproducts(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        KwlReturnObject kmsg = null;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CF_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            double listQty = 0;
            boolean isstockNotAvailable = false;
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String movementtype = request.getParameter("movementtype");
            JSONArray jArr = new JSONArray(request.getParameter("detail"));
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject obj = jArr.getJSONObject(i);

                String productid = obj.getString("productid");
                double baseuomquantity = 0.0;
                if (obj.has("baseuomquantity") && obj.get("baseuomquantity") != null) {
                    baseuomquantity = obj.optDouble("baseuomquantity", 0);
                } else {
                    baseuomquantity = obj.optDouble("quantity", 0);
                }

                JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
                kmsg = accCommonTablesDAO.getBatchSerialDetailsforProduct(productid, false, false, paramJobj);
//                kmsg = accCommonTablesDAO.getBatchSerialDetailsforProduct(productid,false,false, request);
                List batchList = kmsg.getEntityList();
                listQty = kmsg.getRecordTotalCount();
                if (listQty < baseuomquantity) {
                    isstockNotAvailable = true;
                }
                if (isstockNotAvailable) {
                    break;
                }

            }
            jobj.put("isstockNotAvailable", isstockNotAvailable);
            issuccess = true;
            msg = "Stock Availability has been received successfully.";
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    //Send notification mail on set of Recurring JE
    public void SendMail(HashMap requestParams) throws ServiceException {
        String loginUserId = (String) requestParams.get("loginUserId");
        User user = (User) accInvoiceDAOobj.getUserObject(loginUserId);
        KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), user.getCompany().getCompanyID());
        Company company = (Company) returnObject.getEntityList().get(0);
        String sendorInfo = (!company.isEmailFromCompanyCreator()) ? Constants.ADMIN_EMAILID : authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
        boolean ispendingapproval = (Boolean) requestParams.get("ispendingapproval");
        String billno = (String) requestParams.get("billno");
        SimpleDateFormat sdf = new SimpleDateFormat();
        String cEmail = user.getEmailID() != null ? user.getEmailID() : "";
        if (!StringUtil.isNullOrEmpty(cEmail)) {
            try {
                String subject = "A new recurring invoice has been created";
                //String sendorInfo = "admin@deskera.com";
                String htmlTextC = "";
                htmlTextC += "<br/>Hello " + user.getFirstName() + "<br/>";
                if (ispendingapproval) {
                    htmlTextC += "<br/>Invoice <b>\"" + billno + "\"</b> has been set recurring successfully. <br/></br>";
                } else {
                    htmlTextC += "<br/>Invoice <b>\"" + billno + "\"</b> has been set recurring successfully. <br/><br/>";
                }
                htmlTextC += "<br/>Regards,<br/>";
                htmlTextC += "<br/>ERP System<br/>";
                htmlTextC += "<br/><br/>";
                htmlTextC += "<br/>This is an auto generated email. Do not reply<br/>";

                String plainMsgC = "";
                plainMsgC += "\nHello " + user.getFirstName() + "\n";
                if (ispendingapproval) {
                    plainMsgC += "\nInvoice <b>\"" + billno + "\"</b> has been set recurring successfully. \n\n";
                } else {
                    plainMsgC += "\nInvoice <b>\"" + billno + "\"</b> has been set recurring successfully. \n\n";
                }
                plainMsgC += "\nRegards,\n";
                plainMsgC += "\nDeskera Financials\n";
                plainMsgC += "\n\n";
                plainMsgC += "\nThis is an auto generated email. Do not reply.\n";

                Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                SendMailHandler.postMail(new String[]{cEmail}, subject, htmlTextC, plainMsgC, sendorInfo, smtpConfigMap);
            } catch (Exception ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }

    }//sendMail

    public void sendReorderLevelEmails(String userId, DeliveryOrder dod) throws ServiceException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject jeresult = accountingHandlerDAOobj.getObject(User.class.getName(), userId);
        User sender = (User) jeresult.getEntityList().get(0);
        String sendorInfo = sender.getEmailID() != null ? sender.getEmailID() : authHandlerDAOObj.getSysEmailIdByCompanyID(dod.getCompany().getCompanyID());
        KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), sender.getCompany().getCompanyID());
        Company company = (Company) returnObject.getEntityList().get(0);
        Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
        List ll;
        String msg = null;

        try {
            if (dod != null && dod.getRows() != null) {
                int sno = 1;

                String emailIds = "";
                String mailSeparator = ",";
                boolean isfirst = true;

                for (DeliveryOrderDetail sod : dod.getRows()) {
                    KwlReturnObject kwlobject = accountingHandlerDAOobj.getObject(Store.class.getName(), accCommonTablesDAO.getStoreIdForNonbatchSerialByDODetailId(sender.getCompany().getCompanyID(), sod.getID()));
                    Store store = (Store) kwlobject.getEntityList().get(0);
                    Set<User> mgrSet =new HashSet(); 
                    mgrSet.addAll(store.getStoreManagerSet());
                    mgrSet.addAll(store.getStoreExecutiveSet());
                    double availableQtyinStore = stockService.getProductQuantityInStore(sod.getProduct(), store);
                    if (availableQtyinStore < sod.getProduct().getReorderLevel()) {
                        for (User user : mgrSet) {
                            if (isfirst) {
                                emailIds += user.getEmailID();
                                isfirst = false;
                            } else {
                                emailIds += mailSeparator + user.getEmailID();
                            }

                        }

                        String subject = "Reorder Level Notification";
                        String htmlTextC = "";
                        htmlTextC += "<br/>Hi,<br/>";
                        htmlTextC += "<br/>Quantity for product <b>" + sod.getProduct().getName() + "</b>  has gone below reorder level.";
                        htmlTextC += "<br/><b> Quantity :</b> " + availableQtyinStore + "</b><br/><b> Store :</b> " + store.getFullName() + "</b>";
//                htmlTextC += "<br/><b>Store :</b>     "+store + "</b>";
//                        htmlTextC += "<br/><br/>This is an auto generated email. Do not reply.<br/>";
                        htmlTextC += "<br/><br/>Regards,<br/>";
                        htmlTextC += "<br/>ERP System<br/>";
                        htmlTextC += "<br/><br/>";
                        htmlTextC += "<br/>This is an auto generated email. Do not reply.<br/>";
                        String plainMsgC = "";
                        plainMsgC += "\nRegards,\n";
                        plainMsgC += "\nDeskera Financials\n";
                        plainMsgC += "\n\n";
                        plainMsgC += "\nThis is an auto generated email. Do not reply.\n";
                        SendMailHandler.postMail(emailIds.split(","), subject, htmlTextC, plainMsgC, sendorInfo, smtpConfigMap);
                    }
                }
            }
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public void setValuesForAuditTrialForDO(DeliveryOrder olddo, HttpServletRequest request, Map<String, Object> oldgreceipt, Map<String, Object> doDataMap, Map<String, Object> newAuditKey) throws SessionExpiredException {
        DateFormat df = authHandler.getUserDateFormatterWithoutTimeZone(request);
        try {
            //Setting values in map for oldgreceipt
            if (olddo != null) {
                KwlReturnObject currobretrurnlist = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), olddo.getCurrency().getCurrencyID());
                KWLCurrency oldcurrencyobj = (KWLCurrency) currobretrurnlist.getEntityList().get(0);
                KwlReturnObject customerretrurnlist = accountingHandlerDAOobj.getObject(Customer.class.getName(), olddo.getCustomer().getID());
                Customer oldcustomer = (Customer) customerretrurnlist.getEntityList().get(0);

                if (olddo.getSalesperson() != null) {
                    KwlReturnObject oldmastersalesperson = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), olddo.getSalesperson().getID());
                    MasterItem oldsalesPerson = (MasterItem) oldmastersalesperson.getEntityList().get(0);
                    oldgreceipt.put("auditSalesPerson", oldsalesPerson != null ? oldsalesPerson.getValue() : "");
                } else {
                    oldgreceipt.put("auditSalesPerson", "");
                }
                newAuditKey.put("auditSalesPerson", "Sales Person");

                if (olddo.getStatus() != null) {
                    KwlReturnObject oldmasteritemstatuslist = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), olddo.getStatus().getID());
                    MasterItem oldstatus = (MasterItem) oldmasteritemstatuslist.getEntityList().get(0);
                    oldgreceipt.put("auditStatus", oldstatus != null ? oldstatus.getValue() : "");
                } else {
                    oldgreceipt.put("auditStatus", "");
                }
                newAuditKey.put("auditStatus", "Status");

                oldgreceipt.put(Constants.CustomerName, oldcustomer.getName());
                newAuditKey.put(Constants.CustomerName, "Customer");
                oldgreceipt.put(InvoiceConstants.entrynumber, olddo.getDeliveryOrderNumber());
                newAuditKey.put(InvoiceConstants.entrynumber, "Entry Number");
                oldgreceipt.put(Constants.CurrencyName, oldcurrencyobj.getName());//Currency name
                newAuditKey.put(Constants.CurrencyName, "Currency");
                oldgreceipt.put(InvoiceConstants.memo, StringUtil.isNullOrEmpty(olddo.getMemo()) ? "" : olddo.getMemo());
                newAuditKey.put(InvoiceConstants.memo, "Memo");
                oldgreceipt.put("shipvia", StringUtil.isNullOrEmpty(olddo.getShipvia()) ? "" : olddo.getShipvia());
                newAuditKey.put("shipvia", "Ship Via");
                oldgreceipt.put("fob", StringUtil.isNullOrEmpty(olddo.getFob()) ? "" : olddo.getFob());
                newAuditKey.put("fob", "FOB");
                oldgreceipt.put("AuditOrderDate", olddo.getOrderDate() != null ? df.format(olddo.getOrderDate()) : "");
                newAuditKey.put("AuditOrderDate", "Delivery Order Date");
                oldgreceipt.put("AuditShipDate", olddo.getShipdate() != null ? df.format(olddo.getShipdate()) : "");
                newAuditKey.put("AuditShipDate", "Ship Date");
            }

            //Setting values in map for doDataMap
            KwlReturnObject newcurrencyreturnobj = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), request.getParameter(InvoiceConstants.currencyid));
            KWLCurrency newcurrencyobj = (KWLCurrency) newcurrencyreturnobj.getEntityList().get(0);
            doDataMap.put(Constants.CurrencyName, newcurrencyobj.getName());//Currencey name
            KwlReturnObject newcustomerlist = accountingHandlerDAOobj.getObject(Customer.class.getName(), request.getParameter("customer"));
            Customer newcustomer = (Customer) newcustomerlist.getEntityList().get(0);
            doDataMap.put(Constants.CustomerName, newcustomer.getName());//Customer Name

            if (!StringUtil.isNullOrEmpty(request.getParameter("salesPerson"))) {
                KwlReturnObject newmastersales = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), request.getParameter("salesPerson"));
                MasterItem salesPerson = (MasterItem) newmastersales.getEntityList().get(0);
                doDataMap.put("auditSalesPerson", salesPerson.getValue());//SalesPerson Name
            } else {
                doDataMap.put("auditSalesPerson", "");//SalesPerson Name
            }

            if (!StringUtil.isNullOrEmpty(request.getParameter("statuscombo"))) {
                KwlReturnObject newmasterstatus = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), request.getParameter("statuscombo"));
                MasterItem newstatus = (MasterItem) newmasterstatus.getEntityList().get(0);
                doDataMap.put("auditStatus", newstatus.getValue());//Status
            } else {
                doDataMap.put("auditStatus", "");//Status
            }

        } catch (Exception ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setValuesForAuditTrialMessageForRecurring(RepeatedInvoices oldRepeatedInvoice, HttpServletRequest request, LinkedHashMap<String, Object> oldgreceipt, Map<String, Object> greceipthm, Map<String, Object> newAuditKey) throws SessionExpiredException {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {

            if (oldRepeatedInvoice != null) {
                oldgreceipt.put("NextGenerationDate", oldRepeatedInvoice.getNextDate() != null ? df.format(oldRepeatedInvoice.getNextDate()) : "");
                newAuditKey.put("NextGenerationDate", "Next Invoice Date");
                oldgreceipt.put("intervalUnit", oldRepeatedInvoice.getIntervalUnit());
                newAuditKey.put("intervalUnit", "Interval");
                oldgreceipt.put("intervalType", oldRepeatedInvoice.getIntervalType());
                newAuditKey.put("intervalType", "Interval Type");
                oldgreceipt.put("NoOfpost", oldRepeatedInvoice.getNoOfInvoicespost());
                newAuditKey.put("NoOfpost", "Recurring Invoice Count");
                oldgreceipt.put("expireDate", oldRepeatedInvoice.getExpireDate() != null ? df.format(oldRepeatedInvoice.getExpireDate()) : "");
                newAuditKey.put("expireDate", "Expire Date");
                oldgreceipt.put("advancedays", oldRepeatedInvoice.getAdvanceNoofdays());
                newAuditKey.put("advancedays", "Advance No.of Days");
                oldgreceipt.put("advanceDate", oldRepeatedInvoice.getInvoiceAdvanceCreationDate() != null ? df.format(oldRepeatedInvoice.getInvoiceAdvanceCreationDate()) : "");
                newAuditKey.put("advanceDate", "Invoice Generation Date");
            }

        } catch (Exception ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ModelAndView updateLinkingCQScript(HttpServletRequest request, HttpServletResponse response) {
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
            if (!StringUtil.isNullOrEmpty(request.getParameter("subdomain"))) {
                subdomain = request.getParameter("subdomain").toString().trim();
                subdomainArray = subdomain.split(",");
            }
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            KwlReturnObject rCompanyId = null;
            rCompanyId = accInvoiceDAOobj.getAllCompanyFromDb(subdomainArray);
            Iterator itrCompanyId = rCompanyId.getEntityList().iterator();
            boolean isInvoice = false;
            while (itrCompanyId.hasNext()) {
                String companyid = (String) itrCompanyId.next();
                requestParams.put("companyid", companyid);
                KwlReturnObject result = null;
                result = accSalesOrderDAOObj.getLinkedInvoiceWithCQ(requestParams);
                Iterator itr = result.getEntityList().iterator();
                while (itr.hasNext()) {
                    String invoiceid = (String) itr.next();
                    if (!StringUtil.isNullOrEmpty(invoiceid)) {
                        isInvoice = true;
                        updateLinkingInformationOfCQ(invoiceid, companyid, isInvoice);
                    }
                }
                isInvoice = false;
                result = accSalesOrderDAOObj.getLinkedSalesOrderWithCQ(requestParams);
                Iterator itr1 = result.getEntityList().iterator();
                while (itr1.hasNext()) {
                    String goodsreceiptid = (String) itr1.next();
                    if (!StringUtil.isNullOrEmpty(goodsreceiptid)) {
                        updateLinkingInformationOfCQ(goodsreceiptid, companyid, isInvoice);
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
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", "Script completed for Updating Linking Information for CQ linking");

            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    private void updateLinkingInformationOfCQ(String linkNumbers, String companyid, boolean isInvoice) throws ServiceException {
        List list = null;
        try {
            if (isInvoice) {
                KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(InvoiceDetail.class.getName(), linkNumbers);
                InvoiceDetail invdetail = (InvoiceDetail) rdresult.getEntityList().get(0);

                String invoiceno = invdetail.getInvoice().getInvoiceNumber();
                String invoiceid = invdetail.getInvoice().getID();
                String quotationno = invdetail.getQuotationDetail().getQuotation().getQuotationNumber();
                String quotationid = invdetail.getQuotationDetail().getQuotation().getID();

                KwlReturnObject result = accInvoiceDAOobj.checkEntryForInvoiceInLinkingTable(invoiceid, quotationid);
                list = result.getEntityList();
                if (list == null || list.isEmpty()) {

                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("sourceflag", 0);
                    requestParams.put("moduleid", Constants.Acc_Invoice_ModuleId);
                    requestParams.put("linkeddocno", invoiceno);
                    requestParams.put("docid", quotationid);
                    requestParams.put("linkeddocid", invoiceid);
                    result = accSalesOrderDAOObj.saveQuotationLinking(requestParams);

                    requestParams.put("sourceflag", 1);
                    requestParams.put("moduleid", Constants.Acc_Customer_Quotation_ModuleId);
                    requestParams.put("linkeddocno", quotationno);
                    requestParams.put("docid", invoiceid);
                    requestParams.put("linkeddocid", quotationid);
                    result = accInvoiceDAOobj.saveInvoiceLinking(requestParams);
                }
            } else {
                KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(SalesOrderDetail.class.getName(), linkNumbers);
                SalesOrderDetail sodetail = (SalesOrderDetail) rdresult.getEntityList().get(0);

                String salesOrderNo = sodetail.getSalesOrder().getSalesOrderNumber();
                String salesOrderId = sodetail.getSalesOrder().getID();
                String quotationNo = sodetail.getQuotationDetail().getQuotation().getQuotationNumber();
                String quotationId = sodetail.getQuotationDetail().getQuotation().getID();

                KwlReturnObject result = accInvoiceDAOobj.checkEntryForSalesOrderInLinkingTable(salesOrderId, quotationId);
                list = result.getEntityList();

                if (list == null || list.isEmpty()) {

                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("sourceflag", 0);
                    requestParams.put("moduleid", Constants.Acc_Sales_Order_ModuleId);
                    requestParams.put("linkeddocno", salesOrderNo);
                    requestParams.put("docid", quotationId);
                    requestParams.put("linkeddocid", salesOrderId);
                    result = accSalesOrderDAOObj.saveQuotationLinking(requestParams);

                    requestParams.put("sourceflag", 1);
                    requestParams.put("moduleid", Constants.Acc_Customer_Quotation_ModuleId);
                    requestParams.put("linkeddocno", quotationNo);
                    requestParams.put("docid", salesOrderId);
                    requestParams.put("linkeddocid", quotationId);
                    result = accSalesOrderDAOObj.saveSalesOrderLinking(requestParams);

                }
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("updateLinkingInformationOfCQ : " + ex.getMessage(), ex);
        }

    }

    /* Method is wriiten for updating linking information in 
     * 
     * linking table for old data*/
    public ModelAndView updateLinkingSOScript(HttpServletRequest request, HttpServletResponse response) {
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
            rCompanyId = accInvoiceDAOobj.getAllCompanyFromDb(subdomainArray);
            Iterator itrCompanyId = rCompanyId.getEntityList().iterator();
            boolean isInvoice = false;
            while (itrCompanyId.hasNext()) {
                String companyid = (String) itrCompanyId.next();
                requestParams.put("companyid", companyid);
                KwlReturnObject result = null;
                result = accSalesOrderDAOObj.getLinkedInvoiceWithSO(requestParams);
                Iterator itr = result.getEntityList().iterator();
                while (itr.hasNext()) {
                    String invoiceid = (String) itr.next();
                    if (!StringUtil.isNullOrEmpty(invoiceid)) {
                        isInvoice = true;
                        updateLinkingInformationOfSO(invoiceid, isInvoice);
                    }
                }
                isInvoice = false;
                result = accSalesOrderDAOObj.getLinkedDeliveryOrderWithSO(requestParams);
                Iterator itr1 = result.getEntityList().iterator();
                while (itr1.hasNext()) {
                    String goodsreceiptid = (String) itr1.next();
                    if (!StringUtil.isNullOrEmpty(goodsreceiptid)) {
                        updateLinkingInformationOfSO(goodsreceiptid, isInvoice);
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
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", "Script completed for Updating Linking Information for Sales Order linking");

            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /* Method is used for Updating Entry in linking information table 
     
     * for SO->SI and SO->DO*/
    private void updateLinkingInformationOfSO(String linkNumbers, boolean isInvoice) throws ServiceException {
        List list = null;
        try {
            if (isInvoice) {
                KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(InvoiceDetail.class.getName(), linkNumbers);
                InvoiceDetail invdetail = (InvoiceDetail) rdresult.getEntityList().get(0);

                String invoiceno = invdetail.getInvoice().getInvoiceNumber();
                String invoiceid = invdetail.getInvoice().getID();
                String salesOrderNo = invdetail.getSalesorderdetail().getSalesOrder().getSalesOrderNumber();
                String salesOrderId = invdetail.getSalesorderdetail().getSalesOrder().getID();

                KwlReturnObject result = accInvoiceDAOobj.checkEntryForInvoiceInLinkingTable(invoiceid, salesOrderId);
                list = result.getEntityList();
                if (list == null || list.isEmpty()) {

                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("sourceflag", 0);
                    requestParams.put("moduleid", Constants.Acc_Invoice_ModuleId);
                    requestParams.put("linkeddocno", invoiceno);
                    requestParams.put("docid", salesOrderId);
                    requestParams.put("linkeddocid", invoiceid);
                    result = accSalesOrderDAOObj.saveSalesOrderLinking(requestParams);

                    requestParams.put("sourceflag", 1);
                    requestParams.put("moduleid", Constants.Acc_Sales_Order_ModuleId);
                    requestParams.put("linkeddocno", salesOrderNo);
                    requestParams.put("docid", invoiceid);
                    requestParams.put("linkeddocid", salesOrderId);
                    result = accInvoiceDAOobj.saveInvoiceLinking(requestParams);
                }
            } else {
                KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(DeliveryOrderDetail.class.getName(), linkNumbers);
                DeliveryOrderDetail dodetail = (DeliveryOrderDetail) rdresult.getEntityList().get(0);

                String deliveryOrderNo = dodetail.getDeliveryOrder().getDeliveryOrderNumber();
                String deliveryOrderId = dodetail.getDeliveryOrder().getID();
                String salesOrderNo = dodetail.getSodetails().getSalesOrder().getSalesOrderNumber();
                String salesOrderId = dodetail.getSodetails().getSalesOrder().getID();

                KwlReturnObject result = accInvoiceDAOobj.checkEntryForDeliveryOrderInLinkingTable(deliveryOrderId, salesOrderId);
                list = result.getEntityList();

                if (list == null || list.isEmpty()) {

                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("sourceflag", 0);
                    requestParams.put("moduleid", Constants.Acc_Delivery_Order_ModuleId);
                    requestParams.put("linkeddocno", deliveryOrderNo);
                    requestParams.put("docid", salesOrderId);
                    requestParams.put("linkeddocid", deliveryOrderId);
                    result = accSalesOrderDAOObj.saveSalesOrderLinking(requestParams);

                    requestParams.put("sourceflag", 1);
                    requestParams.put("moduleid", Constants.Acc_Sales_Order_ModuleId);
                    requestParams.put("linkeddocno", salesOrderNo);
                    requestParams.put("docid", deliveryOrderId);
                    requestParams.put("linkeddocid", salesOrderId);
                    result = accInvoiceDAOobj.saveDeliveryOrderLinking(requestParams);

                }
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("updateLinkingInformationOfSO : " + ex.getMessage(), ex);
        }

    }

    /* Method is wriiten for updating linking information in 
     * 
     * linking table for old data*/
    public ModelAndView updateLinkingSIScript(HttpServletRequest request, HttpServletResponse response) {
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
            rCompanyId = accInvoiceDAOobj.getAllCompanyFromDb(subdomainArray);
            Iterator itrCompanyId = rCompanyId.getEntityList().iterator();
            boolean isDeliveryOrder = false;
            while (itrCompanyId.hasNext()) {
                String companyid = (String) itrCompanyId.next();
                requestParams.put("companyid", companyid);
                KwlReturnObject result = null;
                result = accSalesOrderDAOObj.getLinkedDeliveryOrderWithInvoice(requestParams);
                Iterator itr = result.getEntityList().iterator();
                while (itr.hasNext()) {
                    String invoiceid = (String) itr.next();
                    if (!StringUtil.isNullOrEmpty(invoiceid)) {
                        isDeliveryOrder = true;
                        updateLinkingInformationOfSI(invoiceid, isDeliveryOrder);
                    }
                }
                isDeliveryOrder = false;
                result = accSalesOrderDAOObj.getLinkedSalesReturnWithInvoice(requestParams);
                Iterator itr1 = result.getEntityList().iterator();
                while (itr1.hasNext()) {
                    String goodsreceiptid = (String) itr1.next();
                    if (!StringUtil.isNullOrEmpty(goodsreceiptid)) {
                        updateLinkingInformationOfSI(goodsreceiptid, isDeliveryOrder);
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
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", "Script completed for Updating Linking Information for Invoice linking");

            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /* Method is used for Updating Entry in linking information table 
     for SI->SR and SI->DO*/
    private void updateLinkingInformationOfSI(String linkNumbers, boolean isInvoice) throws ServiceException {
        List list = null;
        try {
            if (isInvoice) {
                KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(DeliveryOrderDetail.class.getName(), linkNumbers);
                DeliveryOrderDetail deliveryOrderDetail = (DeliveryOrderDetail) rdresult.getEntityList().get(0);

                String deliveryOrderNo = deliveryOrderDetail.getDeliveryOrder().getDeliveryOrderNumber();
                String deliveryOrderId = deliveryOrderDetail.getDeliveryOrder().getID();
                String invoiceNo = deliveryOrderDetail.getCidetails().getInvoice().getInvoiceNumber();
                String invoiceId = deliveryOrderDetail.getCidetails().getInvoice().getID();

                KwlReturnObject result = accInvoiceDAOobj.checkEntryForDeliveryOrderInLinkingTable(deliveryOrderId, invoiceId);
                list = result.getEntityList();
                if (list == null || list.isEmpty()) {

                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("sourceflag", 0);
                    requestParams.put("moduleid", Constants.Acc_Delivery_Order_ModuleId);
                    requestParams.put("linkeddocno", deliveryOrderNo);
                    requestParams.put("docid", invoiceId);
                    requestParams.put("linkeddocid", deliveryOrderId);
                    result = accInvoiceDAOobj.saveInvoiceLinking(requestParams);

                    requestParams.put("sourceflag", 1);
                    requestParams.put("moduleid", Constants.Acc_Invoice_ModuleId);
                    requestParams.put("linkeddocno", invoiceNo);
                    requestParams.put("docid", deliveryOrderId);
                    requestParams.put("linkeddocid", invoiceId);
                    result = accInvoiceDAOobj.saveDeliveryOrderLinking(requestParams);
                }
            } else {
                KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(SalesReturnDetail.class.getName(), linkNumbers);
                SalesReturnDetail salesReturnDetail = (SalesReturnDetail) rdresult.getEntityList().get(0);

                String salesReturnNo = salesReturnDetail.getSalesReturn().getSalesReturnNumber();
                String salesReturnId = salesReturnDetail.getSalesReturn().getID();
                String invoiceNo = salesReturnDetail.getCidetails().getInvoice().getInvoiceNumber();
                String invoiceId = salesReturnDetail.getCidetails().getInvoice().getID();

                KwlReturnObject result = accInvoiceDAOobj.checkEntryForSalesReturnInLinkingTable(salesReturnId, invoiceId);
                list = result.getEntityList();

                if (list == null || list.isEmpty()) {

                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("sourceflag", 0);
                    requestParams.put("moduleid", Constants.Acc_Sales_Return_ModuleId);
                    requestParams.put("linkeddocno", salesReturnNo);
                    requestParams.put("docid", invoiceId);
                    requestParams.put("linkeddocid", salesReturnId);
                    result = accInvoiceDAOobj.saveInvoiceLinking(requestParams);

                    requestParams.put("sourceflag", 1);
                    requestParams.put("moduleid", Constants.Acc_Invoice_ModuleId);
                    requestParams.put("linkeddocno", invoiceNo);
                    requestParams.put("docid", salesReturnId);
                    requestParams.put("linkeddocid", invoiceId);
                    result = accInvoiceDAOobj.saveSalesReturnLinking(requestParams);

                }
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("updateLinkingInformationOfSI : " + ex.getMessage(), ex);
        }

    }

    /* Method is wriiten for updating linking information in 
     * 
     * linking table for old data*/
    public ModelAndView updateLinkingDOScript(HttpServletRequest request, HttpServletResponse response) {
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
            rCompanyId = accInvoiceDAOobj.getAllCompanyFromDb(subdomainArray);
            Iterator itrCompanyId = rCompanyId.getEntityList().iterator();
            boolean isDeliveryOrder = false;
            while (itrCompanyId.hasNext()) {
                String companyid = (String) itrCompanyId.next();
                requestParams.put("companyid", companyid);
                KwlReturnObject result = null;
                result = accSalesOrderDAOObj.getLinkedInvoicesWithDeliveryOrder(requestParams);
                Iterator itr = result.getEntityList().iterator();
                while (itr.hasNext()) {
                    String invoiceid = (String) itr.next();
                    if (!StringUtil.isNullOrEmpty(invoiceid)) {
                        isDeliveryOrder = true;
                        updateLinkingInformationOfDO(invoiceid, isDeliveryOrder);
                    }
                }
                isDeliveryOrder = false;
                result = accSalesOrderDAOObj.getLinkedSalesReturnWithDeliveryOrder(requestParams);
                Iterator itr1 = result.getEntityList().iterator();
                while (itr1.hasNext()) {
                    String goodsreceiptid = (String) itr1.next();
                    if (!StringUtil.isNullOrEmpty(goodsreceiptid)) {
                        updateLinkingInformationOfDO(goodsreceiptid, isDeliveryOrder);
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
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", "Script completed for Updating Linking Information for Delivery Order linking");

            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /* Method is used for Updating Entry in linking information table 
     for DO->SI and DO->SR*/
    private void updateLinkingInformationOfDO(String linkNumbers, boolean isInvoice) throws ServiceException {
        List list = null;
        try {
            if (isInvoice) {
                KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(InvoiceDetail.class.getName(), linkNumbers);
                InvoiceDetail invoiceDetail = (InvoiceDetail) rdresult.getEntityList().get(0);

                String invoiceNo = invoiceDetail.getInvoice().getInvoiceNumber();
                String invoiceId = invoiceDetail.getInvoice().getID();
                String deliveryOrderNo = invoiceDetail.getDeliveryOrderDetail().getDeliveryOrder().getDeliveryOrderNumber();
                String deliveryOrderId = invoiceDetail.getDeliveryOrderDetail().getDeliveryOrder().getID();

                KwlReturnObject result = accInvoiceDAOobj.checkEntryForInvoiceInLinkingTable(invoiceId, deliveryOrderId);
                list = result.getEntityList();
                if (list == null || list.isEmpty()) {

                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("sourceflag", 0);
                    requestParams.put("moduleid", Constants.Acc_Invoice_ModuleId);
                    requestParams.put("linkeddocno", invoiceNo);
                    requestParams.put("docid", deliveryOrderId);
                    requestParams.put("linkeddocid", invoiceId);
                    result = accInvoiceDAOobj.saveDeliveryOrderLinking(requestParams);

                    requestParams.put("sourceflag", 1);
                    requestParams.put("moduleid", Constants.Acc_Delivery_Order_ModuleId);
                    requestParams.put("linkeddocno", deliveryOrderNo);
                    requestParams.put("docid", invoiceId);
                    requestParams.put("linkeddocid", deliveryOrderId);
                    result = accInvoiceDAOobj.saveInvoiceLinking(requestParams);
                }
            } else {
                KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(SalesReturnDetail.class.getName(), linkNumbers);
                SalesReturnDetail salesReturnDetail = (SalesReturnDetail) rdresult.getEntityList().get(0);

                String salesReturnNo = salesReturnDetail.getSalesReturn().getSalesReturnNumber();
                String salesReturnId = salesReturnDetail.getSalesReturn().getID();
                String deliveryOrderNo = salesReturnDetail.getDodetails().getDeliveryOrder().getDeliveryOrderNumber();
                String deliveryOrderId = salesReturnDetail.getDodetails().getDeliveryOrder().getID();

                KwlReturnObject result = accInvoiceDAOobj.checkEntryForSalesReturnInLinkingTable(salesReturnId, deliveryOrderId);
                list = result.getEntityList();

                if (list == null || list.isEmpty()) {

                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("sourceflag", 0);
                    requestParams.put("moduleid", Constants.Acc_Sales_Return_ModuleId);
                    requestParams.put("linkeddocno", salesReturnNo);
                    requestParams.put("docid", deliveryOrderId);
                    requestParams.put("linkeddocid", salesReturnId);
                    result = accInvoiceDAOobj.saveDeliveryOrderLinking(requestParams);

                    requestParams.put("sourceflag", 1);
                    requestParams.put("moduleid", Constants.Acc_Delivery_Order_ModuleId);
                    requestParams.put("linkeddocno", deliveryOrderNo);
                    requestParams.put("docid", salesReturnId);
                    requestParams.put("linkeddocid", deliveryOrderId);
                    result = accInvoiceDAOobj.saveSalesReturnLinking(requestParams);

                }
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("updateLinkingInformationOfDO : " + ex.getMessage(), ex);
        }

    }

    /**
     * Description : Method is used to Update Entry in linking information table
     * for Sales Order & Purchase Order If any Sales Order linked with Purchase
     * Order
     *
     * @param <request> :-used to get sub domain for which you want to update
     * entry in linking information table
     * @param <response>:- used to send response
     * @return :JSONObject(contains success & Message whether script is
     * completed or not)
     */
    public ModelAndView updateSOLinkingWithPOScript(HttpServletRequest request, HttpServletResponse response) {
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
            rCompanyId = accInvoiceDAOobj.getAllCompanyFromDb(subdomainArray);
            Iterator itrCompanyId = rCompanyId.getEntityList().iterator();

            while (itrCompanyId.hasNext()) {
                String companyid = (String) itrCompanyId.next();
                requestParams.put("companyid", companyid);
                KwlReturnObject result = accInvoiceDAOobj.getLinkedPOWithSO(requestParams);
                Iterator itr2 = result.getEntityList().iterator();
                while (itr2.hasNext()) {
                    String podetailID = (String) itr2.next();
                    if (!StringUtil.isNullOrEmpty(podetailID)) {

                        /*
                         * Method is used for updating linking information of
                         *
                         * Sales Order & Purchase Order in linking table
                         */
                        updateLinkingInformationOfSO(podetailID);
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
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", "Script completed for Updating Linking Information for Sales Order linked with Purchase Order");

            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    private void updateLinkingInformationOfSO(String linkNumbers) throws ServiceException {
        List list = null;
        try {

            KwlReturnObject rdresult = accSalesOrderDAOObj.getPODetail(linkNumbers);

            Object[] obj = (Object[]) rdresult.getEntityList().get(0);
            String purchaseOrderNo = obj[1].toString();
            String purchaseOrderID = obj[0].toString();
            String sodetailID = obj[2].toString();
            rdresult = accountingHandlerDAOobj.getObject(SalesOrderDetail.class.getName(), sodetailID);
            SalesOrderDetail sodetail = (SalesOrderDetail) rdresult.getEntityList().get(0);

            String salesOrderNo = sodetail.getSalesOrder().getSalesOrderNumber();
            String salesOrderID = sodetail.getSalesOrder().getID();

            KwlReturnObject result = accInvoiceDAOobj.checkEntryForSalesOrderInLinkingTable(salesOrderID, purchaseOrderID);
            list = result.getEntityList();
            if (list == null || list.isEmpty()) {

                HashMap<String, Object> requestParams = new HashMap<String, Object>();

                requestParams.put("sourceflag", 0);
                requestParams.put("moduleid", Constants.Acc_Purchase_Order_ModuleId);
                requestParams.put("linkeddocno", purchaseOrderNo);
                requestParams.put("docid", salesOrderID);
                requestParams.put("linkeddocid", purchaseOrderID);
                result = accSalesOrderDAOObj.saveSalesOrderLinking(requestParams);

                requestParams.put("sourceflag", 1);
                requestParams.put("moduleid", Constants.Acc_Sales_Order_ModuleId);
                requestParams.put("linkeddocno", salesOrderNo);
                requestParams.put("docid", purchaseOrderID);
                requestParams.put("linkeddocid", salesOrderID);
                result = accSalesOrderDAOObj.updateEntryInPurchaseOrderLinkingTable(requestParams);
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("updateLinkingInformationOfSO : " + ex.getMessage(), ex);
        }

    }
    /*Script for Deleting Entry from invoicelinking table if document present in invoice linking is already unlinked from invoice */

    public ModelAndView updateInvoiceLinkingTableScript(HttpServletRequest request, HttpServletResponse response) {
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
            rCompanyId = accInvoiceDAOobj.getAllCompanyFromDb(subdomainArray);
            Iterator itrCompanyId = rCompanyId.getEntityList().iterator();

            while (itrCompanyId.hasNext()) {
                String companyid = (String) itrCompanyId.next();
                requestParams.put("companyid", companyid);
                requestParams.put("moduleid", Constants.Acc_Delivery_Order_ModuleId);
                /* Get Delivery Order From invoicelinking*/
                KwlReturnObject result = accInvoiceDAOobj.getIDFromInvoiceLinking(requestParams);
                List invoice = result.getEntityList();
                Iterator iterator = invoice.iterator();
                while (iterator.hasNext()) {
                    Object[] object = (Object[]) iterator.next();
                    String docid = (String) object[0];
                    String linkeddocid = (String) object[1];
                    if (!StringUtil.isNullOrEmpty(linkeddocid) && !StringUtil.isNullOrEmpty(docid)) {
                        /* used to delete Entry from invoicelinking table*/
                        deletingEntryFromInvoiceLinkingIfNotLinkedWithAnyDocument(docid, linkeddocid, Constants.Acc_Delivery_Order_ModuleId);
                    }
                }
                requestParams.put("moduleid", Constants.Acc_Sales_Order_ModuleId);
                /* Get Sales Order From invoicelinking*/
                result = accInvoiceDAOobj.getIDFromInvoiceLinking(requestParams);
                invoice = result.getEntityList();
                Iterator iterator1 = invoice.iterator();
                while (iterator1.hasNext()) {
                    Object[] object = (Object[]) iterator1.next();
                    String docid = (String) object[0];
                    String linkeddocid = (String) object[1];
                    if (!StringUtil.isNullOrEmpty(linkeddocid) && !StringUtil.isNullOrEmpty(docid)) {

                        deletingEntryFromInvoiceLinkingIfNotLinkedWithAnyDocument(docid, linkeddocid, Constants.Acc_Sales_Order_ModuleId);
                    }
                }
                requestParams.put("moduleid", Constants.Acc_Sales_Return_ModuleId);
                /* Get Sales Return From invoicelinking*/
                result = accInvoiceDAOobj.getIDFromInvoiceLinking(requestParams);
                invoice = result.getEntityList();
                Iterator iterator2 = invoice.iterator();
                while (iterator2.hasNext()) {
                    Object[] object = (Object[]) iterator2.next();
                    String docid = (String) object[0];
                    String linkeddocid = (String) object[1];
                    if (!StringUtil.isNullOrEmpty(linkeddocid) && !StringUtil.isNullOrEmpty(docid)) {

                        deletingEntryFromInvoiceLinkingIfNotLinkedWithAnyDocument(docid, linkeddocid, Constants.Acc_Sales_Return_ModuleId);
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
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", "Script completed for Deleting Entry from Invoice linking table");

            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    /* used to delete Entry from invoicelinking table*/

    private void deletingEntryFromInvoiceLinkingIfNotLinkedWithAnyDocument(String docid, String linkeddocid, int moduleid) throws ServiceException {
        List list = null;
        try {
            int type = 0;
            if (moduleid == Constants.Acc_Delivery_Order_ModuleId) {
                KwlReturnObject result = accInvoiceDAOobj.checkEntryForDeliveryOrderInLinkingTable(linkeddocid, docid);
                list = result.getEntityList();
                type = 3;

            } else if (moduleid == Constants.Acc_Sales_Order_ModuleId) {
                KwlReturnObject result = accInvoiceDAOobj.checkEntryForSalesOrderInLinkingTable(linkeddocid, docid);
                list = result.getEntityList();
                type = 6;
            } else if (moduleid == Constants.Acc_Sales_Return_ModuleId) {
                KwlReturnObject result = accInvoiceDAOobj.checkEntryForSalesReturnInLinkingTable(linkeddocid, docid);
                list = result.getEntityList();
                type = 4;
            }

            if (list == null || list.isEmpty()) {

                HashMap<String, Object> linkingrequestParams = new HashMap<String, Object>();
                linkingrequestParams.put("linkedTransactionID", linkeddocid);
                linkingrequestParams.put("billid", docid);
                linkingrequestParams.put("type", type);
                linkingrequestParams.put("unlinkflag", true);
                accInvoiceDAOobj.deleteLinkingInformationOfSI(linkingrequestParams);
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("deletingEntryFromInvoiceLinkingIfNotLinkedWithAnyDocument : " + ex.getMessage(), ex);
        }

    }

    public ModelAndView getCompanyUnit(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = null;
        String start = null;
        String limit = null;
        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            if (!StringUtil.isNullOrEmpty(request.getParameter("excludeUsedExciseUnits"))) {
                requestParams.put("excludeUsedExciseUnits", request.getParameter("excludeUsedExciseUnits"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("start"))) {
                start = request.getParameter("start");
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("limit"))) {
                limit = request.getParameter("limit");
            }
            List list = accInvoiceDAOobj.getCompanyUnit(requestParams);

            JSONArray DataJArr = getCompanyUnitJson(request, list);
            int count = DataJArr.length();
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                DataJArr = StringUtil.getPagedJSON(DataJArr, Integer.parseInt(start), Integer.parseInt(limit));
            }
            jobj.put("count", count);
            jobj.put(Constants.RES_data, DataJArr);
            issuccess = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

    public JSONArray getCompanyUnitJson(HttpServletRequest request, List excisedetailsTemplatemap) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            if (excisedetailsTemplatemap != null && !excisedetailsTemplatemap.isEmpty()) {
                for (int i = 0; i < excisedetailsTemplatemap.size(); i++) {
                    Object row[] = (Object[]) excisedetailsTemplatemap.get(i);
                    KwlReturnObject receiptResult = accountingHandlerDAOobj.getObject(ExciseDetailsTemplateMap.class.getName(), row[0].toString());
                    ExciseDetailsTemplateMap receipt = (ExciseDetailsTemplateMap) receiptResult.getEntityList().get(0);
                    JSONObject obj = new JSONObject();
                    obj.put("id", receipt.getId());
                    obj.put("name", receipt.getUnitname());
                    obj.put("registrationType", receipt.getRegistrationType());  // ERP-27117 : Excise Unit Window shown as Grid 
                    obj.put("ECCNo", receipt.getECCNo());
                    obj.put("warehouse", receipt.getWarehouseid() != null ? receipt.getWarehouseid().getId() : "");
                    jArr.put(obj);
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getCompanyUnitJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView insertServiceTaxAccount(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Invoice_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        try {
            String subdomain = "";
            String[] subdomainArray = null;

            if (!StringUtil.isNullOrEmpty(request.getParameter("subdomain"))) {
                subdomain = (String) request.getParameter("subdomain");
                subdomainArray = subdomain.split(",");
            }
            KwlReturnObject company = accInvoiceDAOobj.getCompanyList(subdomainArray);
            Iterator ctr = company.getEntityList().iterator();
            int count = 1;
            while (ctr.hasNext()) {
                TransactionStatus status = null;
                try {
                    status = txnManager.getTransaction(def);
                    String companyid = ctr.next().toString();
                    KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                    Company companyObj = (Company) rdresult.getEntityList().get(0);
                    if (companyObj.getCountry() != null && companyObj.getCountry().getID().equals("" + Constants.indian_country_id)) {  //Indian Company insert the service Account
                        jobj.put("company Name" + (count++), companyObj.getCompanyName());
                        List list = new ArrayList();
                        list.add(companyid);
                        HashMap requestParams = new HashMap();
                        requestParams.put("grpOldId", 3);
                        requestParams.put("companyid", companyid);
                        KwlReturnObject result = accInvoiceDAOobj.getGroupId(requestParams);
                        List ll = result.getEntityList();
                        Iterator itr = ll.iterator();
                        if (itr.hasNext()) {
                            Group group = (Group) itr.next(); //Get Group id (Code : 3- Other Current Liability) Company wise.
                            list.add(group.getID());
                        }
                        list.add(new Date());
                        accInvoiceDAOobj.createServiceTaxAccount((Object[]) list.toArray()); //check if account already exist then not create else create new account  
                        jobj.put("done", "success");
                    }
                    txnManager.commit(status);
                    jobj.put("msg", "Script completed for update insertServiceTaxAccount");
                } catch (Exception ex) {
                    jobj.put("msg", "Script completed for Not update insertServiceTaxAccount");
                    if (status != null) {
                        txnManager.rollback(status);
                    }
                    Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /**
     * Description: Method for insert NOP List for existing company.
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView insertNOPInMasterItems(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Invoice_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        JSONObject jobj = new JSONObject();
        String[] defMasterNopIds = {"38", "39", "40"};
        boolean issuccess = false;
        try {
            String subdomain = "";
            String[] subdomainArray = null;

            if (!StringUtil.isNullOrEmpty(request.getParameter("subdomain"))) {
                subdomain = (String) request.getParameter("subdomain");
                subdomainArray = subdomain.split(",");
            }
            KwlReturnObject company = accInvoiceDAOobj.getCompanyList(subdomainArray);
            Iterator ctr = company.getEntityList().iterator();
            int count = 1;
            while (ctr.hasNext()) {
                TransactionStatus status = null;
                try {
                    status = txnManager.getTransaction(def);
                    String companyid = ctr.next().toString();
                    KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                    Company companyObj = (Company) rdresult.getEntityList().get(0);
                    //Indian Company insert the service Account
                    if (companyObj.getCountry() != null && companyObj.getCountry().getID().equals("" + Constants.indian_country_id)) {
                        jobj.put("company Name" + (count++), companyObj.getCompanyName());
                        // NOP List of id loop
                        for (int i = 0; i < defMasterNopIds.length; i++) {
                            HashMap<String, Object> requestParams = new HashMap<String, Object>();
                            requestParams.put("nopid", defMasterNopIds[i]);
                            requestParams.put("masterGroup", Constants.NatureofPaymentGroup);
                            // Check the Section is present in Default Master Items table.
                            KwlReturnObject rdresultNopList = accMasterItemsDAOobj.checkDefaultMasterItemsNOPlist(requestParams);
                            // Check the Section is present in Default Master Items table.
                            if (rdresultNopList.getRecordTotalCount() == 1) {
                                HashMap<String, Object> filterRequestParams = new HashMap<>();
                                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                                filter_names.add("masterGroup.ID");
                                filter_params.add(Constants.NatureofPaymentGroup);
                                filter_names.add("company.companyID");
                                filter_params.add(companyid);
                                filter_names.add("defaultMasterItem.ID");
                                filter_params.add(defMasterNopIds[i]);
                                filterRequestParams.put("filter_names", filter_names);
                                filterRequestParams.put("filter_params", filter_params);
                                KwlReturnObject rdresultCheckMaster = accMasterItemsDAOobj.getMasterItems(filterRequestParams);
                                // Check Duplicat NOP's
                                if (rdresultCheckMaster.getRecordTotalCount() > 0) {
                                    continue;
                                }

                                List list = rdresultNopList.getEntityList();
                                Iterator itr = list.iterator();
                                while (itr.hasNext()) {
                                    DefaultMasterItem dni = (DefaultMasterItem) itr.next();
                                    requestParams.put("companyid", companyid);
                                    // Add the Section in Master Items table.
                                    accMasterItemsDAOobj.copyMasterItemsNopList(requestParams);
                                    jobj.put(dni.getCode() + ":", dni.getValue());
                                }
                            } else {
                                jobj.put("desc :", "Please Execute DBChanges_October2016 file");
                                break;
                            }
                        }
                        jobj.put("done", "success");
                    }
                    txnManager.commit(status);
                    jobj.put("msg", "Script completed to Insert NOP's");
                } catch (Exception ex) {
                    jobj.put("msg", "Script Not completed to Insert NOP's");
                    if (status != null) {
                        txnManager.rollback(status);
                    }
                    Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /**
     * Description: Method for importing Sales Invoices.
     *
     * @param request
     * @param response
     * @return
     */
//    public ModelAndView importInvoices(HttpServletRequest request, HttpServletResponse response) {
//        JSONObject jobj = new JSONObject();
//        String msg = "";
//        boolean issuccess = false;
//        try {
//            JSONObject paramJobj = null;
//            String doAction = request.getParameter("do");
//            if (doAction.compareToIgnoreCase("import") == 0) {
//                paramJobj = getImportInvoicesParams(request);
//                importInvoiceThreadobj.add(paramJobj);
//                if (!importInvoiceThreadobj.isIsworking()) {
//                    Thread t = new Thread(importInvoiceThreadobj);
//                    t.setPriority(7);
//                    t.start();
//                }
//                jobj.put("exceededLimit", "yes");
//                jobj.put("success", true);                
//            } else if (doAction.compareToIgnoreCase("validateData") == 0) {
//                paramJobj = getImportInvoicesParams(request);
//                String eParams = paramJobj.optString("extraParams", "");
//                if (!StringUtil.isNullOrEmpty(eParams)) {
//                    JSONObject extraParams = StringUtil.isNullOrEmpty(eParams) ? new JSONObject() : new JSONObject(eParams);
//                    HashMap<String, Object> requestParams = importHandler.getImportRequestParams(paramJobj);
//                    requestParams.put("extraParams", extraParams);
//                    requestParams.put("extraObj", null);
//                    requestParams.put("servletContext", paramJobj.get("servletContext"));
//
//                    jobj = importHandler.validateFileData(requestParams);
//                    jobj.put(Constants.RES_success, true);
//                }
//            }
//            issuccess = true;
//        } catch (Exception ex) {
//            issuccess = false;
//            msg = "" + ex.getMessage();
//            try {
//                jobj.put("success", issuccess);
//                jobj.put("msg", msg);
//            } catch (JSONException e) {
//                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
//            }
//
//            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
//        }
//        return new ModelAndView("jsonView", "model", jobj.toString());
//    }

    /**
     * Description: Method for getting parameters of import Sales Invoices.
     *
     * @param request
     * @return
     * @throws JSONException
     * @throws SessionExpiredException
     */
    public JSONObject getImportInvoicesParams(HttpServletRequest request) throws JSONException, SessionExpiredException {
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        paramJobj.put("servletContext", this.getServletContext());
        paramJobj.put("baseurl", paramJobj.optString(Constants.PAGE_URL));
        return paramJobj;
    }

    /**
     * @Desc : Save Shipping DO
     * @param request
     * @param response
     * @return
     */
    public ModelAndView saveShippingDeliveryOrder(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        String billno = "";
        String billid = "";
        String productids = "";
        String shipnumber = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Pack_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String packingDoListId = "";
            JSONObject shippingObj = saveShippingDeliveryOrder(request, packingDoListId);
            billid = shippingObj.optString("stockoutid");
            billno = shippingObj.optString("shipnumber");
            productids = shippingObj.optString("productids", "");
            if (!StringUtil.isNullOrEmpty(productids)) {
                productids = productids.substring(0, productids.length() - 1);
            }
            issuccess = true;
            msg = messageSource.getMessage("acc.shippeddolist.save", null, RequestContextUtils.getLocale(request)) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + " <b>" + billno + "</b>";   //"Delivery order has been saved successfully";                
            txnManager.commit(status);
        } catch (AccountingException ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
        } catch (Exception ex) {
            System.out.println(ex);
            System.out.println(ex.getMessage());
            System.out.println(ex.getCause());
            System.out.println(ex.getStackTrace());
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("billid", billid);
                jobj.put("productIds", productids);
                jobj.put("billno", billno);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject saveShippingDeliveryOrder(HttpServletRequest request, String packingDoListId) throws SessionExpiredException, ServiceException, AccountingException, JSONException, UnsupportedEncodingException {
        PackingDoList packingDoList = null;
        AccCustomData accCustomData = null;
        KwlReturnObject objItr= null;
        HashMap<String,Object> extraparams = new HashMap<>();
        JSONObject stockoutobj = new JSONObject();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String userId = sessionHandlerImpl.getUserid(request);
            String entryNumber = request.getParameter("packNumber") != null ? request.getParameter("packNumber") : request.getParameter("number");
            String status = request.getParameter("statuscombo");
            String billid = request.getParameter("billid");
            String [] billIds = request.getParameter("billids").split(",");
            JSONArray globalCustomfield = new JSONArray();
            String createdby = sessionHandlerImpl.getUserid(request);
            String modifiedby = sessionHandlerImpl.getUserid(request);
            long createdon = System.currentTimeMillis();
            long updatedon = System.currentTimeMillis();
            List packedDoListDetailsList = null;
            HashMap<String, Object> packingDoListMap = new HashMap<String, Object>();
            String sequenceformat = request.getParameter("sequenceformat");
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String userDateFormat = sessionHandlerImpl.getCompanySessionObj(request).getUserdateformat();
            extraparams.put(Constants.companyid, companyid);
            extraparams.put(Constants.userdateformat, userDateFormat);
            extraparams.put(Constants.moduleid, Constants.Acc_Delivery_Order_ModuleId);
            extraparams.put(Constants.linkModuleId, Constants.Inventory_Stock_Adjustment_ModuleId);
            extraparams.put(Constants.customcolumn, 0); // Global Fields
            
            Map<String, Object> seqNumberMap = new HashMap<String, Object>();
            int seqNumber = 0;
            
            /**
             * Create Custom field Array for Sock Adjustment from DO
             */
            if (billIds.length > 0) {
                objItr = accountingHandlerDAOobj.getObject(DeliveryOrder.class.getName(), billIds[0]);
                DeliveryOrder DO = (DeliveryOrder) objItr.getEntityList().get(0);
                if (DO.getDeliveryOrderCustomData() != null) {
                    accCustomData = DO.getDeliveryOrderCustomData();
                    globalCustomfield = accAccountDAOobj.createCustomFieldValueArray(accCustomData, extraparams);
                }
            }

            synchronized (this) {
                if (StringUtil.isNullOrEmpty(billid)) { //Create new case
                    KwlReturnObject dopackinglistcnt = accInvoiceDAOobj.getShippingListCount(entryNumber, companyid);
                    if (dopackinglistcnt.getRecordTotalCount() > 0) {
                        throw new AccountingException("Shipping DO List number '" + entryNumber + "' already exists.");
                    }
                }
                if (!sequenceformat.equals("NA")) {
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_SHIPPINGDO, sequenceformat, false, df.parse(request.getParameter("packingDate")));
                    entryNumber = seqNumberMap.containsKey(Constants.AUTO_ENTRYNUMBER) ? (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER) : "";
                    if (seqNumberMap.containsKey(Constants.SEQNUMBER) && !StringUtil.isNullObject(seqNumberMap.get(Constants.SEQNUMBER)) && !StringUtil.isNullOrEmpty(seqNumberMap.get(Constants.SEQNUMBER).toString())) {
                        seqNumber = Integer.parseInt(seqNumberMap.get(Constants.SEQNUMBER).toString());
                    }
                    packingDoListMap.put("seqnumber", seqNumber);
                } else {
                    List list = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_ShippingDO_ModuleId, entryNumber, companyid);
                    if (!list.isEmpty()) {
                        boolean isvalidEntryNumber = (Boolean) list.get(0);
                        String formatName = (String) list.get(1);
                        String language = RequestContextUtils.getLocale(request).getLanguage() + "_" + RequestContextUtils.getLocale(request).getCountry();
                        if (!isvalidEntryNumber) {
                            throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, Locale.forLanguageTag(language)) + " <b>" + entryNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, Locale.forLanguageTag(language)) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null, Locale.forLanguageTag(language)) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null, Locale.forLanguageTag(language)));
                        }
                    }
                }
            }
            packingDoListMap.put("entrynumber", entryNumber);
            packingDoListMap.put("autogenerate", sequenceformat.equals("NA") ? false : true);
            packingDoListMap.put("sequenceformat", sequenceformat);
            packingDoListMap.put("memo", request.getParameter("memo"));
            packingDoListMap.put("customerid", request.getParameter("customerid"));
            packingDoListMap.put("customer", request.getParameter("customer"));
            packingDoListMap.put("letterofcn", request.getParameter("letterofcn"));
            packingDoListMap.put("partialshipment", request.getParameter("partialshipment"));
            packingDoListMap.put("transhipment", request.getParameter("transhipment"));
            packingDoListMap.put("portofloading", request.getParameter("portofloading"));
            packingDoListMap.put("portofdischarge", request.getParameter("portofdischarge"));
            packingDoListMap.put("vessel", request.getParameter("vessel"));
            packingDoListMap.put("incoterms", request.getParameter("incoterms"));
            if (!StringUtil.isNullOrEmpty(request.getParameter("dateoflc"))) {
                packingDoListMap.put("dateoflc", df.parse(request.getParameter("dateoflc")));
            }
            packingDoListMap.put("packingdate", df.parse(request.getParameter("packingDate")));
            packingDoListMap.put("status", status);
            packingDoListMap.put("createdby", createdby);
            packingDoListMap.put("modifiedby", modifiedby);
            packingDoListMap.put("createdon", createdon);
            packingDoListMap.put("updatedon", updatedon);
            packingDoListMap.put("companyid", companyid);
            KwlReturnObject doresult = accInvoiceDAOobj.saveShippingDeliveryOrder(packingDoListMap);
            ShippingDeliveryOrder shippingDeliveryOrder = (ShippingDeliveryOrder) doresult.getEntityList().get(0);

            /**
             * Pass data to Stock OUT
             */
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            params.put("companyId", companyid);
            params.put("userId", userId);
            params.put("adjustmentReason", "");
            params.put("df", df);
            params.put("shipdo", shippingDeliveryOrder.getID());
            params.put("request", request);
            params.put("customfield", globalCustomfield.toString());
            params.put("userDateFormat", userDateFormat);
            stockoutobj = saveStockOutUsingShippingDO(params);

            /**
             * Update Status for DO
             */
            Map<String, Object> reqmap = new HashMap();
            reqmap.put("dodid", stockoutobj.optString("dodid"));
            accInvoiceModuleService.setDOStatus(reqmap);

            stockoutobj.put("shipid", shippingDeliveryOrder.getID());
            stockoutobj.put("shipnumber", shippingDeliveryOrder.getShipNumber());
        } catch (AccountingException ex) {
            throw new AccountingException(ex.getMessage());
        } catch (Exception ex) {
            String msg = "savePackingDoList : " + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = "savePackingDoList : " + ex.getCause().getMessage();
            }
            throw ServiceException.FAILURE(msg, ex);
        }
        return stockoutobj;
    }

    public JSONObject saveStockOutUsingShippingDO(JSONObject params) throws ServiceException, AccountingException, SessionExpiredException, ParseException, UnsupportedEncodingException, SeqFormatException {
        List returnList = new ArrayList();
        HashSet rows = new HashSet();
        JSONObject jSONObject1 = new JSONObject();
        KwlReturnObject objItr = null;
        HashMap<String,Object> extraparams = new HashMap<>();

        try {
            JSONArray jArr = new JSONArray(params.optString("detail"));

            /**
             * Create JSON for Stock OUT
             */
            DateFormat df = new SimpleDateFormat(Constants.MMMMdyyyy);
            double shipqty = 0;
            double baseuomrate = 0;
            extraparams.put(Constants.companyid, params.optString("companyId"));
            extraparams.put(Constants.userdateformat, params.optString("userDateFormat"));
            extraparams.put(Constants.moduleid, Constants.Acc_Delivery_Order_ModuleId);
            extraparams.put(Constants.linkModuleId, Constants.Inventory_Stock_Adjustment_ModuleId);
            extraparams.put(Constants.customcolumn, 1); // Line Fields
            
            for (int i = 0; i < jArr.length(); i++) {
                JSONArray stockOutData = new JSONArray();
                JSONObject jobj = jArr.getJSONObject(i);
                JSONObject object = new JSONObject();
                JSONArray linelevelcustomdata = new JSONArray();
                String productid = jobj.optString("productid");
                String dodId = jobj.optString("dodid");
                if (!StringUtil.isNullOrEmpty(productid)) {
                    KwlReturnObject proresult = accountingHandlerDAOobj.getObject(Product.class.getName(), productid);
                    Product product = (Product) proresult.getEntityList().get(0);
                    if (product != null) {
                        object.put("productid", jobj.optString("productid"));
                        object.put("uomid", jobj.optString("uomid"));
                        object.put("dodid", jobj.optString("dodid"));
                        object.put("memo", params.optString("memo"));
                        object.put("bussinessDate", (params.optString("packingDate")));
                        object.put("adjustmentType", "Stock Out");
                        object.put("shipdo", params.optString("shipdo"));
                        shipqty = jobj.optDouble("shipquantity");
                        baseuomrate = jobj.optDouble("baseuomrate");
                        object.put("quantity", -shipqty);
                        object.put("shipqty", shipqty);
                        object.put("dquantity", jobj.optDouble("dquantity"));
                        object.put("baseuomrate", jobj.optDouble("baseuomrate"));
                        JSONArray nArray = new JSONArray(jobj.optString("batchdetails"));
                        JSONArray jSONArray = new JSONArray();
                        for (int storedata = 0; storedata < nArray.length(); storedata++) {
                            JSONObject jobj1 = nArray.getJSONObject(storedata);

                            JSONObject nObject = new JSONObject();
                            object.put("storeId", jobj1.optString("warehouse"));
                            nObject.put("locationId", jobj1.optString("location"));
                            nObject.put("rowId", jobj1.optString("row"));
                            nObject.put("rackId", jobj1.optString("rack"));
                            nObject.put("binId", jobj1.optString("bin"));
                            if (product.isIsBatchForProduct() && StringUtil.isNullOrEmpty(jobj1.optString("batchName"))) {
                                throw new AccountingException("Please provide valid batch details.");
                            }
                            nObject.put("batchName", jobj1.optString("batchName"));
                            if (product.isIsSerialForProduct() && StringUtil.isNullOrEmpty(jobj1.optString("serialno"))) {
                                throw new AccountingException("Please provide valid serial details.");
                            } else if (product.isIsSerialForProduct() && !StringUtil.isNullOrEmpty(jobj1.optString("serialno"))) {
                                String[] adjSerialArr = jobj1.optString("serialno").split(",");
                                if (jobj1.optDouble("quantity") != adjSerialArr.length) {
                                    throw new AccountingException("Please provide valid serial details.");
                                }
                            }
                            nObject.put("serialNames", jobj1.optString("serialno"));
                            nObject.put("skuFields", jobj1.optString("skuFields"));
                            nObject.put("approvalSerials", jobj1.optString("approvalSerials"));
                            shipqty = jobj1.optDouble("quantity");
                            nObject.put("quantity", -shipqty);
                            nObject.put("mfgdate", jobj1.optString("mfgdate"));
                            nObject.put("expdate", jobj1.optString("expdate"));
                            object.put("productid", jobj1.optString("productid"));
                            object.put("productname", jobj1.optString("productname"));
                            object.put("deliveryorder", jobj1.optString("deliveryorder"));
                            nObject.put("warrantyexpfromdate", jobj1.optString("warrantyexpfromdate"));
                            nObject.put("warrantyexptodate", jobj1.optString("warrantyexptodate"));
                            jSONArray.put(nObject);
                        }
                        object.put("stockDetails", jSONArray);
                        stockOutData.put(object);

                    }
                    
                    /**
                     * Create Line level custom data array
                     */
                    objItr = accountingHandlerDAOobj.getObject(DeliveryOrderDetail.class.getName(), dodId);
                    DeliveryOrderDetail doDetail = (DeliveryOrderDetail) objItr.getEntityList().get(0);
                    if (doDetail.getDeliveryOrderDetailCustomData() != null) {
                        AccCustomData accCustomData = doDetail.getDeliveryOrderDetailCustomData();
                        linelevelcustomdata = accAccountDAOobj.createCustomFieldValueArray(accCustomData, extraparams);
                    }
                        
                    Map<String, Object> reqMap = new HashMap();
                    reqMap.put("stockOutData", stockOutData.toString());
                    reqMap.put("customer", params.optString("customerid"));
                    reqMap.put("companyId", params.optString("companyId"));
                    reqMap.put("request", (HttpServletRequest) params.opt("request"));
                    reqMap.put("userId", params.optString("userId"));
                    reqMap.put("adjustmentReason", params.optString("adjustmentReason"));
                    reqMap.put("customfield", params.optString(Constants.customfield));
                    reqMap.put(Constants.LineLevelCustomData, linelevelcustomdata.toString());
                    jSONObject1 = accInvoiceModuleService.saveStockOutfromDO(reqMap);
                }
            }
            if(jSONObject1.has("seqFormat") && jSONObject1.get("seqFormat")!= null){
                SeqFormat seqFormat=(SeqFormat)jSONObject1.get("seqFormat");
                seqService.updateSeqNumber(seqFormat);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveDeliveryOrderRows : " + ex.getMessage(), ex);
        } catch (AccountingException ex) {
            throw new AccountingException(ex.getMessage());
        }
        return jSONObject1;
    }

    /**
     *
     * @param request
     * @Desc : Function to save contract
     * @param response
     * @return
     */
    public ModelAndView saveContract(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        String billid = "";
        String billno = "";
        String amount = "";
        int pendingApproval = 0;
        boolean issuccess = false;
        boolean isTaxDeactivated = false;
        boolean isNormalContract = Boolean.parseBoolean(request.getParameter("isNormalContract"));;
        boolean isEdit = Boolean.parseBoolean(request.getParameter("isEdit"));;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("COntract_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            String soid = request.getParameter("invoiceid");
            //Check Deactivate Tax in New Transaction.
            if (StringUtil.isNullOrEmpty(soid)) {
                if (!fieldDataManagercntrl.isTaxActivated(paramJobj)) {
                    isTaxDeactivated = true;
                    throw ServiceException.FAILURE(messageSource.getMessage("acc.tax.deactivated.tax.saveAlert", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                }
            }

            List li = saveContract(request);
            billid = (String) li.get(0);
            billno = (String) li.get(1);
            boolean isAutoCreateDO = false;
            isAutoCreateDO = Boolean.parseBoolean(paramJobj.optString("isAutoCreateDO"));
            String domsg = "";
            if (isAutoCreateDO) {
                /**
                 * Contract created with Auto DO
                 */
                /*Get request parameters */
                paramJobj.put("isAutoCreateDO", false);
                paramJobj.put("isFromContract", true);
                paramJobj.put("isLeaseFixedAsset", true);
                String invoiceid = "";
                List doList = accInvoiceModuleService.saveDeliveryOrder(paramJobj, invoiceid);
                DeliveryOrder deliveryOrder = (DeliveryOrder) doList.get(0);
                String sequenceformatDo = paramJobj.optString("sequenceformatDo", null) != null ? paramJobj.optString("sequenceformatDo") : paramJobj.optString("sequenceformat", null);
                String doid = deliveryOrder.getID();
                String companyid = deliveryOrder.getCompany().getCompanyID();
                String billNo = deliveryOrder.getDeliveryOrderNumber();
                if (isAutoCreateDO && !sequenceformatDo.equals("NA")) {
                    /**
                     * Set Sequence Number to Delivery Order
                     */
                    String nextAutoNumber1 = "";
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_DELIVERYORDER, sequenceformatDo, false, deliveryOrder.getOrderDate());
                    seqNumberMap.put(Constants.DOCUMENTID, deliveryOrder.getID());
                    seqNumberMap.put(Constants.companyKey, companyid);
                    seqNumberMap.put(Constants.SEQUENCEFORMATID, sequenceformatDo);
                    billNo = accInvoiceDAOobj.updateDOEntryNumberForNewDO(seqNumberMap);

                }
                Map<String, Object> auditRequestParams = new HashMap<String, Object>();
                auditRequestParams.put(Constants.reqHeader, paramJobj.getString(Constants.reqHeader));
                auditRequestParams.put(Constants.remoteIPAddress, paramJobj.getString(Constants.remoteIPAddress));
                auditRequestParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));
                String action = "added new";
                String auditMSG = " has " + action + " Auto Lease Delivery Order " + billNo + " with contract " + billno;
                auditTrailObj.insertAuditLog(AuditAction.DELIVERY_ORDER, "User " + paramJobj.optString(Constants.userfullname) + auditMSG, auditRequestParams, deliveryOrder.getID());
                domsg = " With Lease Delivery Order " + ": <b>" + billNo + "</b>";
            }

            issuccess = true;
            int istemplate = 0;
            if (!StringUtil.isNullOrEmpty(request.getParameter("istemplate"))) {
                istemplate = Integer.parseInt(request.getParameter("istemplate"));
            }
            if (istemplate == 1) {
                msg = messageSource.getMessage("acc.field.SalesOrderandTemplatehasbeensavedsuccessfully" + ".", null, RequestContextUtils.getLocale(request));  //+ (pendingApprovalFlag ? messageSource.getMessage("acc.field.butSalesOrderispendingforApproval", null, RequestContextUtils.getLocale(request)) : 
            } else if (istemplate == 2) {
                msg = messageSource.getMessage("acc.field.SalesOrderTemplatehasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.co.save", null, RequestContextUtils.getLocale(request)) + ((pendingApproval == 1) ? messageSource.getMessage("acc.field.butpendingforApproval", null, RequestContextUtils.getLocale(request)) : ".") + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + billno + "</b>";   //"Sales order has been saved successfully";
            }
            msg = msg + domsg;
            jobj.put("SOID", billid);
            jobj.put("billid", billid);
            jobj.put("billno", billno);

            if (!isEdit) {
                auditTrailObj.insertAuditLog("77", " User " + sessionHandlerImpl.getUserFullName(request) + " has added a new" + (isNormalContract ? "" : " Lease") + " contract Details " + billno, request, billid);
            } else {
                auditTrailObj.insertAuditLog("77", " User " + sessionHandlerImpl.getUserFullName(request) + " has updated a " + (isNormalContract ? "" : " Lease") + " contract Details " + billno, request, billid);
            }

            txnManager.commit(status);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put(Constants.isTaxDeactivated, isTaxDeactivated);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public List saveContract(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException, JSONException, UnsupportedEncodingException {
        Contract contract = null;
        List newList = new ArrayList();
        int pendingApprovalFlag = 0;
        try {
            int istemplate = request.getParameter("istemplate") != null ? Integer.parseInt(request.getParameter("istemplate")) : 0;
            String taxid = null;
            String customfield = request.getParameter("customfield");
            boolean isNormalContract = false;// if contract is created normaly not from lease module

            if (!StringUtil.isNullOrEmpty(request.getParameter("isNormalContract"))) {
                isNormalContract = Boolean.parseBoolean(request.getParameter("isNormalContract"));
            }
            String sequenceformat = request.getParameter("sequenceformat");
            String companyid = sessionHandlerImpl.getCompanyid(request);

            String createdby = sessionHandlerImpl.getUserid(request);
            String modifiedby = sessionHandlerImpl.getUserid(request);

            long createdon = System.currentTimeMillis();
            long updatedon = createdon;

            String entryNumber = request.getParameter("number");
            String soid = request.getParameter("invoiceid");//Need to change
            String costCenterId = request.getParameter("costcenter");
            String nextAutoNumber = "";
            String[] deletedRecsArr = (String[]) request.getParameterValues("deletedServiceDates");

            boolean isEdit = request.getParameter("isEdit") != null ? StringUtil.getBoolean(request.getParameter("isEdit")) : false;
            boolean isCopy = request.getParameter("copyInv") != null ? StringUtil.getBoolean(request.getParameter("copyInv")) : false;
            boolean isLinkedTransaction = request.getParameter("isLinkedTransaction") != null ? StringUtil.getBoolean(request.getParameter("isLinkedTransaction")) : false;
            boolean islockQuantity = request.getParameter("islockQuantity") != null ? StringUtil.getBoolean(request.getParameter("islockQuantity")) : false;
            double externalCurrencyRate = StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            HashMap<String, Object> GlobalParams = AccountingManager.getGlobalParams(request);
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            
            HashMap<String, Object> soDataMap = new HashMap<String, Object>();
            String currencyid = (request.getParameter("currencyid") == null ? sessionHandlerImpl.getCurrencyID(request) : request.getParameter("currencyid"));
            synchronized (this) {
                KwlReturnObject socnt = accSalesOrderDAOObj.getContractCount(entryNumber, companyid,isEdit,soid);
                if (socnt.getRecordTotalCount() > 0 && istemplate != 2) {
                    if (sequenceformat.equals("NA")) {
                        throw new AccountingException(messageSource.getMessage("acc.field.contractNumber", null, RequestContextUtils.getLocale(request)) + " " + entryNumber + " " + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                    }
                }
                    if (!StringUtil.isNullOrEmpty(soid)) {
                        nextAutoNumber = entryNumber;
                        soDataMap.put("id", soid);
                        KwlReturnObject result = accSalesOrderDAOObj.getReplacementAndMaintenance(soid);
                        List list = result.getEntityList();

                        result = accSalesOrderDAOObj.getInvoiceAndDeliveryOrderOfContract(soid);
                        List list1 = result.getEntityList();

                        list.addAll(list1);

                        if (!list.isEmpty()) {
                            throw new AccountingException("Selected record is currently used So it cannot be edited.");
                        }
                        accSalesOrderDAOObj.deleteContractDetails(soid, companyid);
//                        accSalesOrderDAOobj.deleteServiceDetails(soid, companyid, deletedRecs);
                        accSalesOrderDAOObj.deletecontractFiles(soid);                        
                    }
                else {
                    boolean seqformat_oldflag = StringUtil.getBoolean(request.getParameter("seqformat_oldflag"));
                    String nextAutoNoInt = "";
                    String datePrefix = "";
                    String dateafterPrefix = "";
                    String dateSuffix = "";
                    if (!sequenceformat.equals("NA")) {
                        if (seqformat_oldflag) {
                            nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_CONTRACT, sequenceformat);
                        } else {
                            Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_CONTRACT, sequenceformat, seqformat_oldflag, null);// There is no creation date of Contract hence putting null for server date
                            nextAutoNumber = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                            nextAutoNoInt = (String) seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                            datePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                            dateafterPrefix = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                            dateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                            soDataMap.put(Constants.SEQFORMAT, sequenceformat);
                            soDataMap.put(Constants.SEQNUMBER, nextAutoNoInt);
                            soDataMap.put(Constants.DATEPREFIX, datePrefix);
                            soDataMap.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
                            soDataMap.put(Constants.DATESUFFIX, dateSuffix);

                        }
                        entryNumber = nextAutoNumber;
                    }
                }
                if (sequenceformat.equals("NA")) {//In case of NA checks wheather this number can also be generated by a sequence format or not
                    List list = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Contract_Order_ModuleId, entryNumber, companyid);
                    if (!list.isEmpty()) {
                        boolean isvalidEntryNumber = (Boolean) list.get(0);
                        String formatName = (String) list.get(1);
                        if (!isvalidEntryNumber) {
                            throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, RequestContextUtils.getLocale(request)) + " <b>" + entryNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, RequestContextUtils.getLocale(request)) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null, RequestContextUtils.getLocale(request)) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null, RequestContextUtils.getLocale(request)));
                        }
                    }
                }
            }
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String CustomerId = request.getParameter("customer");
            KwlReturnObject custresult = accountingHandlerDAOobj.getObject(Customer.class.getName(), CustomerId);
            Customer customer = (Customer) custresult.getEntityList().get(0);
            String customerCRMID = customer.getCrmaccountid();  //customer account id in crm 

            double leaseAmount = StringUtil.isNullOrEmpty(request.getParameter("leaseAmount")) ? 0.0 : Double.parseDouble(request.getParameter("leaseAmount"));
            double securityDeposite = StringUtil.isNullOrEmpty(request.getParameter("securityDeposite")) ? 0.0 : Double.parseDouble(request.getParameter("securityDeposite"));
            soDataMap.put("entrynumber", entryNumber);
            soDataMap.put("isNormalContract", isNormalContract);
            soDataMap.put("autogenerated", nextAutoNumber.equals(entryNumber));
            soDataMap.put("emailid", request.getParameter("emailid"));
            soDataMap.put("numberOfPeriods", request.getParameter("numberOfPeriods"));
            soDataMap.put("frequencyType", request.getParameter("frequencyType"));
            soDataMap.put("memo", request.getParameter("memo"));
            soDataMap.put("contactperson", request.getParameter("contactperson"));
            soDataMap.put("leaseAmount", leaseAmount);
            soDataMap.put("securityDeposite", securityDeposite);
            soDataMap.put("termtype", request.getParameter("leaseTermType"));
            soDataMap.put("termvalue", request.getParameter("termvalue"));
            soDataMap.put("posttext", request.getParameter("posttext"));
            soDataMap.put("customerid", request.getParameter("customer"));
            soDataMap.put("salesorder", request.getParameter("salesorderno"));  //ERP-30712-Sales Order ID
            soDataMap.put("sono", request.getParameter("sono"));        //ERP-30712-Sales Order Number
            soDataMap.put("signinDate", StringUtil.isNullOrEmpty(request.getParameter("signinDate")) ? "" : df.parse(request.getParameter("signinDate")));
            soDataMap.put("originalendDate", StringUtil.isNullOrEmpty(request.getParameter("originalendDate")) ? "" : df.parse(request.getParameter("originalendDate")));
            soDataMap.put("enddate", StringUtil.isNullOrEmpty(request.getParameter("enddate")) ? "" : df.parse(request.getParameter("enddate")));
            soDataMap.put("signdate", StringUtil.isNullOrEmpty(request.getParameter("signinDate")) ? "" : df.parse(request.getParameter("signinDate")));
            soDataMap.put("moveindate", StringUtil.isNullOrEmpty(request.getParameter("moveindate")) ? "" : df.parse(request.getParameter("moveindate")));
            soDataMap.put("moveoutdate", StringUtil.isNullOrEmpty(request.getParameter("moveoutdate")) ? "" : df.parse(request.getParameter("moveoutdate")));
            soDataMap.put("startdate", StringUtil.isNullOrEmpty(request.getParameter("startdate")) ? "" : df.parse(request.getParameter("startdate")));
            soDataMap.put("currencyid", currencyid);
            soDataMap.put("termid", request.getParameter("termid"));
            soDataMap.put("isfavourite", request.getParameter("isfavourite"));
            soDataMap.put("salesPerson", request.getParameter("salesPerson"));
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                soDataMap.put("costCenterId", costCenterId);
            }
            soDataMap.put("companyid", companyid);
            soDataMap.put("createdby", createdby);
            soDataMap.put("modifiedby", modifiedby);
            soDataMap.put("createdon", createdon);
            soDataMap.put("updatedon", updatedon);

            KwlReturnObject soresult = accSalesOrderDAOObj.saveContract(soDataMap);
            contract = (Contract) soresult.getEntityList().get(0);
            if (isEdit) {    //refer ticket ERP-17512
                HashMap<String, Object> dataMap = new HashMap<String, Object>();
                dataMap.put("contractid", contract.getID());
                accSalesOrderDAOObj.deleteContractDates(dataMap);
            }
            addContractDates(request, contract);
//            }
            // Save PO Custom Data

            if (!StringUtil.isNullOrEmpty(customfield)) {
                HashMap<String, Object> SOMap = new HashMap<String, Object>();
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", "contract");
                customrequestParams.put("moduleprimarykey", "contractID");
                customrequestParams.put("modulerecid", contract.getID());
                customrequestParams.put("moduleid", isNormalContract ? Constants.Acc_Contract_Order_ModuleId : Constants.Acc_Lease_Contract);
                customrequestParams.put("companyid", companyid);
                SOMap.put("id", contract.getID());
                customrequestParams.put("customdataclasspath", Constants.Acc_Contract_Order_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    SOMap.put("contractcustomdataref", contract.getID());
                    accSalesOrderDAOObj.saveContract(SOMap);
                }
            }

//            salesOrderId = contract.getID();
//            salesOrderNumber = contract.getContractNumber();
            soDataMap.put("id", contract.getID());
            List rowDetails = saveContractRows(request, contract, companyid, currencyid, GlobalParams, externalCurrencyRate);
            String billid = contract.getID();
//            String pendingApprovalFlagnew=String.valueOf(pendingApprovalFlag);
            String billno = contract.getContractNumber();
//            newList.add(pendingApprovalFlagnew);
            newList.add(billid);
            newList.add(billno);
            String salesorder = request.getParameter("salesorderno");
            String fileidstr = request.getParameter("fileidstr");
            if (!StringUtil.isNullOrEmpty(salesorder)) {
                HashMap<String, Object> SOMap = new HashMap<String, Object>();
                SOMap.put("isEdit", isEdit);
                SOMap.put("islockQuantity", islockQuantity);
                SOMap.put("isLinkedTransaction", isLinkedTransaction);
                SOMap.put("isCopy", isCopy);
                SOMap.put("id", salesorder);
                SOMap.put("contractid", contract.getID());
                SOMap.put("companyid", companyid);
                SOMap.put("orderdate", new Date(createdon));
                accSalesOrderDAOObj.saveSalesOrder(SOMap);
            }
            // Save Activity Schedule
            JSONArray crmActivityArray = new JSONArray();

            boolean isScheduleIncluded = false;

            if (!StringUtil.isNullOrEmpty(request.getParameter("isScheduleIncluded"))) {
                isScheduleIncluded = Boolean.parseBoolean(request.getParameter("isScheduleIncluded"));
            }

            String deletedRecs = "";

            if (isScheduleIncluded) {

                String scheduleId = request.getParameter("scheduleId");

                if (!StringUtil.isNullOrEmpty(scheduleId)) {// calculate deleted services records in case of edit

//                    AssetMaintenanceSchedulerObject schedulerObject = 
                    KwlReturnObject scObj = accountingHandlerDAOobj.getObject(AssetMaintenanceSchedulerObject.class.getName(), scheduleId);
                    AssetMaintenanceSchedulerObject maintenanceSchedulerObject = (AssetMaintenanceSchedulerObject) scObj.getEntityList().get(0);

                    Set<AssetMaintenanceScheduler> maintenanceSchedulers = maintenanceSchedulerObject.getAssetMaintenanceSchedulers();

                    Iterator mainIt = maintenanceSchedulers.iterator();

                    while (mainIt.hasNext()) {
                        AssetMaintenanceScheduler scheduler = (AssetMaintenanceScheduler) mainIt.next();

                        deletedRecs += scheduler.getId() + ",";

                    }

                    if (deletedRecs.length() > 0) {
                        deletedRecs = deletedRecs.substring(0, deletedRecs.length() - 1);
                    }

                }

                AssetMaintenanceSchedulerObject schedulerObject = accProductModuleService.saveAssetMaintenanceSchedule(request, contract.getID());

                Set<AssetMaintenanceScheduler> maintenanceSchedulers = schedulerObject.getAssetMaintenanceSchedulers();

                Iterator mainIt = maintenanceSchedulers.iterator();

                while (mainIt.hasNext()) {
                    AssetMaintenanceScheduler scheduler = (AssetMaintenanceScheduler) mainIt.next();

                    JSONObject crmObj = new JSONObject();

                    Calendar tempCalendarStartDate = Calendar.getInstance();
                    tempCalendarStartDate.setTimeInMillis(scheduler.getStartDate().getTime());
                    tempCalendarStartDate.setTimeZone(TimeZone.getTimeZone("GMT"));
                    crmObj.put("serviceStartDate", scheduler.getStartDate());
                    crmObj.put("serviceStartDate2", tempCalendarStartDate.getTimeInMillis());
                    crmObj.put("serviceStartDate_yyyyMMdd", scheduler.getStartDate() != null ? authHandler.getFormatedDate(scheduler.getStartDate(), Constants.yyyyMMdd) : "");
//                    crmObj.put("servicedate", scheduler.getStartDate());
//                    crmObj.put("servicedate2", tempCalendarStartDate.getTimeInMillis());

                    Calendar tempCalendarEndDate = Calendar.getInstance();
                    tempCalendarEndDate.setTimeInMillis(scheduler.getEndDate().getTime());
                    tempCalendarEndDate.setTimeZone(TimeZone.getTimeZone("GMT"));
                    crmObj.put("serviceEndDate", scheduler.getEndDate());
                    crmObj.put("serviceEndDate2", tempCalendarEndDate.getTimeInMillis());
                    crmObj.put("serviceEndDate_yyyyMMdd", scheduler.getEndDate() != null ? authHandler.getFormatedDate(scheduler.getEndDate(), Constants.yyyyMMdd) : "");

                    crmObj.put("serviceId", scheduler.getId());
                    crmActivityArray.put(crmObj);
                }
                int agreedServices = 0;
                if (maintenanceSchedulers != null && maintenanceSchedulers.size() > 0) {
                    agreedServices = maintenanceSchedulers.size();
                }
                soDataMap.put("agreedservices", agreedServices);
                soresult = accSalesOrderDAOObj.saveContract(soDataMap);
            }
            boolean isActivitysuccess = false;
            if (!StringUtil.isNullOrEmpty(customerCRMID)) {
                isActivitysuccess = createActivityInCRM(companyid, billid, billno, customerCRMID, request, crmActivityArray, deletedRecs, isEdit);
                newList.add(isActivitysuccess);
            }
            if (!isActivitysuccess && isEdit && !StringUtil.isNullOrEmpty(customerCRMID)) {
                throw new AccountingException(messageSource.getMessage("acc.contract.activityexists", null, RequestContextUtils.getLocale(request)));
            }
            if (!StringUtil.isNullOrEmpty(fileidstr)) {
                String[] fileidstrArray = fileidstr.split(",");
                for (int cnt = 0; cnt < fileidstrArray.length; cnt++) {
                    accSalesOrderDAOObj.updateContractFiles(billid, fileidstrArray[cnt]);
                }

            }
            
             //Get mapping details id of contract document
            String savedFilesMappingId = request.getParameter("savedFilesMappingId");
            if (!StringUtil.isNullOrEmpty(savedFilesMappingId)) {
                /**
                 * Save temporary saved attachment files mapping in permanent
                 * table
                 */
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("savedFilesMappingId", savedFilesMappingId);
                jsonObj.put("docId", billid);
                jsonObj.put("companyid", companyid);
                accInvoiceModuleService.saveDocuments(jsonObj);
            }

            String moduleName = Constants.moduleID_NameMap.get(Acc_Lease_Contract);
            if (isNormalContract) {
                moduleName = Constants.moduleID_NameMap.get(Acc_Contract_Order_ModuleId);
            }
            //Send Mail when Purchase Requisition is generated or modified.
            DocumentEmailSettings documentEmailSettings = null;
            KwlReturnObject documentEmailresult = accountingHandlerDAOobj.getObject(DocumentEmailSettings.class.getName(), sessionHandlerImpl.getCompanyid(request));
            documentEmailSettings = documentEmailresult != null ? (DocumentEmailSettings) documentEmailresult.getEntityList().get(0) : null;
            if (documentEmailSettings != null) {
                boolean sendmail = false;
                boolean isEditMail = false;
                if (StringUtil.isNullOrEmpty(soid)) {
                    if (documentEmailSettings.isLeaseContractGenerationMail()) { ////Create New Case
                        sendmail = true;
                    }
                } else {
                    isEditMail = true;
                    if (documentEmailSettings.isLeaseContractUpdationMail()) { // edit case  
                        sendmail = true;
                    }
                }
                if (sendmail) {
                    String userMailId = "", userName = "", currentUserid = "";
                    String createdByEmail = "";
                    String createdById = "";
                    HashMap<String, Object> requestParams = AccountingManager.getEmailNotificationParams(request);
                    if (requestParams.containsKey("userfullName") && requestParams.get("userfullName") != null) {
                        userName = (String) requestParams.get("userfullName");
                    }
                    if (requestParams.containsKey("usermailid") && requestParams.get("usermailid") != null) {
                        userMailId = (String) requestParams.get("usermailid");
                    }
                    if (requestParams.containsKey(Constants.useridKey) && requestParams.get(Constants.useridKey) != null) {
                        currentUserid = (String) requestParams.get(Constants.useridKey);
                    }
                    List<String> mailIds = new ArrayList();
                    if (!StringUtil.isNullOrEmpty(userMailId)) {
                        mailIds.add(userMailId);
                    }
                    /*
                     if Edit mail option is true then get userid and Email id of document creator.
                     */
                    if (isEditMail) {
                        if (contract != null && contract.getCreatedby() != null) {
                            createdByEmail = contract.getCreatedby().getEmailID();
                            createdById = contract.getCreatedby().getUserID();
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
                    String contractNumber = entryNumber;
                    accountingHandlerDAOobj.sendSaveTransactionEmails(contractNumber, moduleName, tomailids, userName, isEditMail, companyid);
                }
            }

        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveContract " + ex.getMessage(), ex);
        }
        return newList;
    }

    /**
     *
     * @param companyid
     * @param billid
     * @param billno
     * @param customerCRMID
     * @param request
     * @param crmActivityArray
     * @param deleteServiceArray
     * @param isEdit
     * @Desc : Function to create activity in CRM
     * @return
     */
    private boolean createActivityInCRM(String companyid, String billid, String billno, String customerCRMID, HttpServletRequest request, JSONArray crmActivityArray, String deleteServiceArray, boolean isEdit) {
        boolean isActivitysuccess = false;
        //Session session=null;
        try {

            String crmReqmsg = "";
//            String crmURL = this.getServletContext().getInitParameter("crmURL");
            String action = "203";
            String crmURL = URLUtil.buildRestURL(Constants.crmURL);
            crmURL = crmURL + "master/activity";
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("userid", sessionHandlerImpl.getUserid(request));
            userData.put("companyid", companyid);
            userData.put("action", action);

            //session = HibernateUtil.getCurrentSession();
            JSONObject pjobj = new JSONObject();
            pjobj.put("contractid", (billid != null) ? billid : "");
            pjobj.put("contractno", (billno != null) ? billno : "");
            pjobj.put("isEdit", isEdit ? isEdit : false);
            pjobj.put("customerid", (customerCRMID != null) ? customerCRMID : "");
//            pjobj.put("agreedservices", agreedservices);// no use
            pjobj.put("deleteServiceArray", deleteServiceArray);
//            JSONArray jArr = new JSONArray(request.getParameter("servicedetail"));
            pjobj.put("servicedetail", crmActivityArray);

            userData.put("data", pjobj);

            JSONObject resObj = apiCallHandlerService.restPostMethod(crmURL, userData.toString());
//            JSONObject resObj = apiCallHandlerService.callApp(crmURL, userData, companyid, action);
            if (!resObj.isNull("success") && resObj.getBoolean("success")) {
                isActivitysuccess = resObj.getBoolean("success");
                crmReqmsg = resObj.getString(Constants.RES_MESSAGE);
            }

        } catch (Exception ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, "accContractController.createActivityInCRM", ex);
        }
//        finally{
//            HibernateUtil.closeSession(session);
//        }
        return isActivitysuccess;
    }

    public List saveContractRows(HttpServletRequest request, Contract contract, String companyid, String currencyid, HashMap<String, Object> GlobalParams, double externalCurrencyRate) throws ServiceException, AccountingException, UnsupportedEncodingException, JSONException, SessionExpiredException {
        HashSet rows = new HashSet();
//        HashSet aggredservicerows = new HashSet();
        List ll = new ArrayList();
        ArrayList<String> prodList = new ArrayList<String>();
        int lineLevelTermFlag=0;
        HashMap<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("id", companyid);
        Object preferencesArray = (Object) kwlCommonTablesDAOObj.getRequestedObjectFields(ExtraCompanyPreferences.class, new String[]{"lineLevelTermFlag"}, paramsMap);
        lineLevelTermFlag = (Integer) preferencesArray;     
        JSONObject paramJobj=StringUtil.convertRequestToJsonObject(request);
        try {
            double totalAmount = 0.0;
            JSONArray jArr = new JSONArray(request.getParameter("detail"));
            boolean isNormalContract = false;// if contract is created normaly not from lease module

            if (!StringUtil.isNullOrEmpty(request.getParameter("isNormalContract"))) {
                isNormalContract = Boolean.parseBoolean(request.getParameter("isNormalContract"));
            }
            DateFormat df = authHandler.getDateOnlyFormat(request);
            for (int i = 0; i < jArr.length(); i++) {
                double rowAmount = 0;
                JSONObject jobj = jArr.getJSONObject(i);
                HashMap<String, Object> sodDataMap = new HashMap<String, Object>();
                sodDataMap.put("srno", i + 1);
                sodDataMap.put("companyid", companyid);
                sodDataMap.put("soid", contract.getID());
                sodDataMap.put("productid", jobj.getString("productid"));

                prodList.add(jobj.getString("productid"));

                sodDataMap.put("rate", jobj.getDouble("rate"));//CompanyHandler.getCalCurrencyAmount(session,request,jobj.getDouble("rate"),request.getParameter("currencyid"),null));
                sodDataMap.put("unitPricePerInvoice", jobj.optDouble("unitPricePerInvoice", 0));//CompanyHandler.getCalCurrencyAmount(session,request,jobj.getDouble("rate"),request.getParameter("currencyid"),null));
                sodDataMap.put("quantity", jobj.getDouble("quantity"));
                if (jobj.has("uomid")) {
                    sodDataMap.put("uomid", jobj.getString("uomid"));
                }
                if (jobj.has("baseuomquantity") && jobj.get("baseuomquantity") != null) {
                    sodDataMap.put("baseuomquantity", jobj.getDouble("baseuomquantity"));
                    sodDataMap.put("baseuomrate", jobj.getDouble("baseuomrate"));
                } else {
                    sodDataMap.put("baseuomquantity", jobj.getDouble("quantity"));
                    sodDataMap.put("baseuomrate", jobj.getDouble("baseuomrate"));
                }
                sodDataMap.put("remark", jobj.optString("remark"));
                String rowtaxid = jobj.getString("prtaxid");

                /*-------------When No Tax is applied in Lease Order Product then "None" Value is coming for "prtaxid" 
                 from getSalesOrderrows() function As check is Applied ,if taxid is null then send "None" value for "partaxid" from getSalesOrderrows() function 
                 while selecting Lease Order in Contract form-------------*/
                if (rowtaxid.equalsIgnoreCase("None")) {
                    rowtaxid = "";
                }
                //try {
                sodDataMap.put("desc", StringUtil.DecodeText(jobj.optString("desc")));
                /*} catch (UnsupportedEncodingException ex) {
                 sodDataMap.put("desc", jobj.optString("desc"));
                 }*/

                if (jobj.has("prdiscount") && jobj.get("prdiscount") != null) {
                    sodDataMap.put("discount", jobj.getDouble("prdiscount"));
                }
                if (jobj.has("discountispercent") && jobj.get("discountispercent") != null) {
                    sodDataMap.put("discountispercent", jobj.getInt("discountispercent"));
                }

                KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, jobj.getDouble("rate"), currencyid, df.parse(request.getParameter("startdate")), externalCurrencyRate);

//                rowAmount = (Double) bAmt.getEntityList().get(0) * jobj.getDouble("quantity");
                rowAmount = (Double) jobj.optDouble("rate", 0.0) * jobj.getDouble("quantity");

                if (!StringUtil.isNullOrEmpty(rowtaxid)) {
                    KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), rowtaxid); // (Tax)session.get(Tax.class, taxid);
                    Tax rowtax = (Tax) txresult.getEntityList().get(0);
                    if (rowtax == null) {
                        throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
                    } else {
                        sodDataMap.put("rowtaxid", rowtaxid);
                        double rowtaxamount = StringUtil.getDouble(jobj.getString("taxamount"));
                        sodDataMap.put("rowTaxAmount", rowtaxamount);
                        bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, rowtaxamount, currencyid, df.parse(request.getParameter("startdate")), externalCurrencyRate);
                        rowtaxamount = (Double) bAmt.getEntityList().get(0);

                        rowAmount = rowAmount + rowtaxamount;
                    }

                }
                sodDataMap.put("rowTermAmount", StringUtil.getDouble(jobj.optString("recTermAmount","0")));
                //  row.setTax(rowtax);

                KwlReturnObject result = accSalesOrderDAOObj.saveContractDetails(sodDataMap);
                ContractDetail row = (ContractDetail) result.getEntityList().get(0);

//              Save contract Details Custom Data
                String customfield = jobj.getString("customfield");
                if (!StringUtil.isNullOrEmpty(customfield)) {
                    HashMap<String, Object> SOMap = new HashMap<String, Object>();
                    JSONArray jcustomarray = new JSONArray(customfield);

                    HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                    customrequestParams.put("customarray", jcustomarray);
                    customrequestParams.put("modulename", "SalesContractDetail");
                    customrequestParams.put("moduleprimarykey", "ScDetailID");
                    customrequestParams.put("modulerecid", row.getID());
                    customrequestParams.put("moduleid", isNormalContract ? Constants.Acc_Contract_Order_ModuleId : Constants.Acc_Lease_Contract);
                    customrequestParams.put("companyid", companyid);
                    SOMap.put("id", row.getID());
                    customrequestParams.put("customdataclasspath", Constants.Acc_Contract_Details_custom_data_classpath);
                    KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        SOMap.put("contractcustomdataref", row.getID());
                        accSalesOrderDAOObj.updateContractReference(SOMap);
                    }
                }

                rows.add(row);
//                rows.add(servicerow);
                totalAmount += rowAmount;
                if (jobj.has("LineTermdetails") && StringUtil.DecodeText((String) jobj.optString("LineTermdetails"))!= "") {
                    JSONArray termsArray = new JSONArray(StringUtil.DecodeText((String) jobj.optString("LineTermdetails")));
                    for (int j = 0; j < termsArray.length(); j++) {
                        HashMap<String, Object> ContractDetailsTermsMap = new HashMap<String, Object>();
                        JSONObject termObject = termsArray.getJSONObject(j);

                        if (termObject.has("termid")) {
                            ContractDetailsTermsMap.put("term", termObject.get("termid"));
                        }
                        if (termObject.has("termamount")) {
                            ContractDetailsTermsMap.put("termamount", termObject.get("termamount"));
                        }
                        if (termObject.has("termpercentage")) {
                            ContractDetailsTermsMap.put("termpercentage", termObject.get("termpercentage"));
                        }
                        if (termObject.has("purchasevalueorsalevalue")) {
                            ContractDetailsTermsMap.put("purchasevalueorsalevalue", termObject.get("purchasevalueorsalevalue"));
                        }
                        if (termObject.has("deductionorabatementpercent")) {
                            ContractDetailsTermsMap.put("deductionorabatementpercent", termObject.get("deductionorabatementpercent"));
                        }
                        if (termObject.has("taxtype") && !StringUtil.isNullOrEmpty(termObject.getString("taxtype"))) {
                            ContractDetailsTermsMap.put("taxtype", termObject.getInt("taxtype"));
                            if (termObject.has("taxvalue") && !StringUtil.isNullOrEmpty(termObject.getString("taxvalue"))) {
                                if (termObject.getInt("taxtype") == 0) { // If Flat
                                    ContractDetailsTermsMap.put("termamount", termObject.getDouble("taxvalue"));
                                } else { // Else Percentage
                                    ContractDetailsTermsMap.put("termpercentage", termObject.getDouble("taxvalue"));
                                }
                            }
                        }
                        ContractDetailsTermsMap.put("contractDetailID", row.getID());
                        ContractDetailsTermsMap.put("isDefault", termObject.optString("isDefault", "false"));
                        ContractDetailsTermsMap.put("productentitytermid", termObject.optString("productentitytermid"));
                        ContractDetailsTermsMap.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));

                        accSalesOrderDAOObj.saveContractDetailsTermsMap(ContractDetailsTermsMap);
                    }

                    if (lineLevelTermFlag == 1) {
                        /**
                         * Save GST History Customer/Vendor data.
                         */
                        jobj.put("detaildocid", row.getID());
                        jobj.put("moduleid", Constants.Acc_Lease_Contract);
                        fieldDataManagercntrl.createRequestMapToSaveTaxClassHistory(jobj);
                    }
                }
            }
            ll.add(rows);
            ll.add(totalAmount);
            ll.add(prodList);
            jArr = new JSONArray(request.getParameter("servicedetail"));
//             SimpleDateFormat sdf=new SimpleDateFormat("EEE, dd MMM yyyy");

            JSONArray crmActivityArray = new JSONArray();

        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveContratctRows : " + ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("saveContratctRows : " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveContratctRows : " + ex.getMessage(), ex);
        }
        return ll;
    }

    public void addContractDates(HttpServletRequest request, Contract contract) throws SessionExpiredException, AccountingException, ServiceException, ParseException {
        HashMap<String, Object> soDataMap = new HashMap<String, Object>();
        HashMap<String, Object> contractData = new HashMap<String, Object>();
        DateFormat df = authHandler.getDateOnlyFormat(request);
        // Save Activity Schedule
        if (contract != null) {
            JSONArray crmActivityArray = new JSONArray();
            boolean isScheduleIncluded = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("isScheduleIncluded"))) {
                isScheduleIncluded = Boolean.parseBoolean(request.getParameter("isScheduleIncluded"));
            }
            String deletedRecs = "";
            String isRenewContract = request.getParameter("isRenewContract");
            if (isScheduleIncluded && !StringUtil.isNullOrEmpty(isRenewContract)) {
                String scheduleId = request.getParameter("scheduleId");
                if (!StringUtil.isNullOrEmpty(scheduleId)) {// calculate deleted services records in case of edit
                    KwlReturnObject scObj = accountingHandlerDAOobj.getObject(AssetMaintenanceSchedulerObject.class.getName(), scheduleId);
                    AssetMaintenanceSchedulerObject maintenanceSchedulerObject = (AssetMaintenanceSchedulerObject) scObj.getEntityList().get(0);
                    Set<AssetMaintenanceScheduler> maintenanceSchedulers = maintenanceSchedulerObject.getAssetMaintenanceSchedulers();
                    Iterator mainIt = maintenanceSchedulers.iterator();
                    while (mainIt.hasNext()) {
                        AssetMaintenanceScheduler scheduler = (AssetMaintenanceScheduler) mainIt.next();
                        deletedRecs += scheduler.getId() + ",";
                    }
                    if (deletedRecs.length() > 0) {
                        deletedRecs = deletedRecs.substring(0, deletedRecs.length() - 1);
                    }
                }
                AssetMaintenanceSchedulerObject schedulerObject = accProductModuleService.saveAssetMaintenanceSchedule(request, contract.getID());
                Set<AssetMaintenanceScheduler> maintenanceSchedulers = schedulerObject.getAssetMaintenanceSchedulers();
                for (AssetMaintenanceScheduler scheduler : maintenanceSchedulers) {
                    try {
                        JSONObject crmObj = new JSONObject();
                        Calendar tempCalendarStartDate = Calendar.getInstance();
                        tempCalendarStartDate.setTimeInMillis(scheduler.getStartDate().getTime());
                        tempCalendarStartDate.setTimeZone(TimeZone.getTimeZone("GMT"));
                        crmObj.put("serviceStartDate", scheduler.getStartDate());
                        crmObj.put("serviceStartDate2", tempCalendarStartDate.getTimeInMillis());
                        Calendar tempCalendarEndDate = Calendar.getInstance();
                        tempCalendarEndDate.setTimeInMillis(scheduler.getEndDate().getTime());
                        tempCalendarEndDate.setTimeZone(TimeZone.getTimeZone("GMT"));
                        crmObj.put("serviceEndDate", scheduler.getEndDate());
                        crmObj.put("serviceEndDate2", tempCalendarEndDate.getTimeInMillis());
                        crmObj.put("serviceId", scheduler.getId());
                        crmActivityArray.put(crmObj);
                    } catch (JSONException ex) {
                        Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                int agreedServices = 0;
                if (maintenanceSchedulers != null && maintenanceSchedulers.size() > 0) {
                    agreedServices = maintenanceSchedulers.size();
                }
                contractData.put("id", contract.getID());
                contractData.put("agreedservices", agreedServices);
                KwlReturnObject soresult = accSalesOrderDAOObj.saveContract(contractData);
                String companyid = contract.getCompany().getCompanyID();
                KwlReturnObject cap = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
                ExtraCompanyPreferences preferences = (ExtraCompanyPreferences) cap.getEntityList().get(0);
                if (preferences.isActivateCRMIntegration()) {
                    boolean isActivitysuccess = false;
                    String CustomerId = request.getParameter("customer");
                    KwlReturnObject custresult = accountingHandlerDAOobj.getObject(Customer.class.getName(), CustomerId);
                    Customer customer = (Customer) custresult.getEntityList().get(0);
                    String customerCRMID = customer.getCrmaccountid();  //customer account id in crm 
                    String billid = contract.getID();
                    String billno = contract.getContractNumber();
                    if (!StringUtil.isNullOrEmpty(customerCRMID)) {
                        isActivitysuccess = createActivityInCRM(companyid, billid, billno, customerCRMID, request, crmActivityArray, deletedRecs, true);
                    }
                    if (!isActivitysuccess && !StringUtil.isNullOrEmpty(customerCRMID)) {
                        throw new AccountingException(messageSource.getMessage("acc.contract.activityexists", null, RequestContextUtils.getLocale(request)));
                    }
                }
            }

            // save contract dates
            soDataMap.put("enddate", StringUtil.isNullOrEmpty(request.getParameter("enddate")) ? "" : df.parse(request.getParameter("enddate")));
            soDataMap.put("startdate", StringUtil.isNullOrEmpty(request.getParameter("startdate")) ? "" : df.parse(request.getParameter("startdate")));
            soDataMap.put("contractid", contract.getID());
            soDataMap.put(Constants.df, df);
            KwlReturnObject soresult = accSalesOrderDAOObj.getContractDates(soDataMap);
            int count1 = soresult.getRecordTotalCount();
            if (count1 > 0) {
                throw new AccountingException(messageSource.getMessage("acc.contract.contractsDatesExists", null, RequestContextUtils.getLocale(request)));
            } else {
                KwlReturnObject coDates = accSalesOrderDAOObj.saveContractDates(soDataMap);
            }
        }

    }
    
    /**
     * Get Additional memo details JSON for INDONESIA 
     * @param request
     * @param response
     * @return 
     */
    public ModelAndView getAdditionalMemo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jObj = new JSONObject();
        boolean isSuccess = false;
        try {
            JSONObject requestParams = StringUtil.convertRequestToJsonObject(request);
            jObj = accInvoiceModuleService.getAdditionalMemo(requestParams);
            isSuccess = true;
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jObj.put(Constants.RES_success, isSuccess);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jObj.toString());
    }
    /**
     * ERP-34156
     *
     * @desc checks if pickpackDO created
     * @param request
     * @param response
     * @return true (for Present) and false (for Not Present)
     */
    public ModelAndView isPickPackShipDOPresent(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jObj = null;
        boolean isSuccess = false;
        String msg = "";
        try {
            JSONObject requestParams = StringUtil.convertRequestToJsonObject(request);
            requestParams.put(Constants.locale, RequestContextUtils.getLocale(request));
            jObj = accInvoiceModuleService.isPickPackShipDOPresent(requestParams);
            isSuccess = jObj.optBoolean(Constants.RES_success);
            msg = jObj.optString(Constants.RES_msg);
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jObj.put(Constants.RES_success, isSuccess);
                jObj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jObj.toString());
    }

    public ModelAndView isPackingStoreUsedBefore(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jObj = null;
        boolean isSuccess = false;
        try {
            JSONObject requestParams = StringUtil.convertRequestToJsonObject(request);
            jObj = accInvoiceModuleService.isPackingStoreUsedBefore(requestParams);
            isSuccess = jObj.optBoolean(Constants.RES_success);
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jObj.put(Constants.RES_success, isSuccess);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jObj.toString());
    }

    public ModelAndView isStoreUsedInTransaction(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jObj = new JSONObject();
        boolean isSuccess = false;
        boolean isUsedInTransaction = false;
        try {
            JSONObject requestParams = StringUtil.convertRequestToJsonObject(request);
            isUsedInTransaction = accInvoiceModuleService.isStoreUsedInTransaction(requestParams);
            isSuccess = true;
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jObj.put(Constants.RES_success, isSuccess);
                jObj.put("isUsedInTransaction", isUsedInTransaction);
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jObj.toString());
    }
}
