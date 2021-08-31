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
package com.krawler.spring.accounting.goodsreceipt;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import static com.krawler.common.util.Constants.Acc_Vendor_Invoice_ModuleId;
import static com.krawler.common.util.Constants.GSTRegType_Unregistered;
import com.krawler.common.util.CsvReader;
import com.krawler.common.util.IndiaComplianceConstants;
import com.krawler.common.util.JSONUtil;
import com.krawler.common.util.LandingCostAllocationType;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.esp.handlers.ServerEventManager;
import static com.krawler.esp.web.resource.Links.loginpageFull;
import com.krawler.hql.accounting.*;
import com.krawler.hql.accounting.journalentry.service.AccJournalEntryModuleService;
import com.krawler.inventory.exception.InventoryException;
import com.krawler.inventory.model.ist.GRODetailISTMapping;
import com.krawler.inventory.model.ist.InterStoreTransferService;
import com.krawler.inventory.model.ist.InterStoreTransferStatus;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.stock.StockService;
import com.krawler.inventory.model.stockmovement.*;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.bankreconciliation.accBankReconciliationDAO;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesCMN;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesConstants;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignHandler;
import com.krawler.spring.accounting.discount.accDiscountDAO;
import com.krawler.spring.accounting.goodsreceipt.service.ImportPurchaseInvoice;
import com.krawler.spring.accounting.goodsreceipt.service.accGoodsReceiptModuleService;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.journalentry.JournalEntryConstants;
import com.krawler.spring.accounting.journalentry.accJournalEntryController;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.masteritems.accMasterItemsController;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.multiLevelApprovalRule.AccMultiLevelApprovalDAO;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.accounting.product.accProductController;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.purchaseorder.accPurchaseOrderController;
import com.krawler.spring.accounting.purchaseorder.accPurchaseOrderDAO;
import com.krawler.spring.accounting.salesorder.accSalesOrderService;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.term.accTermDAO;
import com.krawler.spring.accounting.vendor.accVendorDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.CommonFnController;
import com.krawler.spring.common.CommonFnControllerService;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.common.fieldManagerDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.importFunctionality.ImportDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.permissionHandler.permissionHandlerDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;
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
public class accGoodsReceiptController extends MultiActionController implements GoodsReceiptConstants, MessageSourceAware {

    private HibernateTransactionManager txnManager;
    private AccMultiLevelApprovalDAO accMultiLevelApprovalDAOObj;
    private authHandlerDAO authHandlerDAOObj;
    private accGoodsReceiptDAO accGoodsReceiptobj;
    private accJournalEntryDAO accJournalEntryobj;
    private accProductDAO accProductObj;
    private accDiscountDAO accDiscountobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private String successView;
    private MessageSource messageSource;
    private fieldDataManager fieldDataManagercntrl;
    private AccJournalEntryModuleService journalEntryModuleServiceobj;
    private auditTrailDAO auditTrailObj;
    private ImportHandler importHandler;
    private StockMovementService stockMovementService;
    private ImportDAO importDao;
    private accTaxDAO accTaxObj;
    private AccCommonTablesDAO accCommonTablesDAO;
    private accPurchaseOrderDAO accPurchaseOrderobj;
    private permissionHandlerDAO permissionHandlerDAOObj;
    private accMasterItemsDAO accMasterItemsDAOobj;
    private accPaymentDAO accPaymentDAOobj;
    private accTermDAO accTermObj;
    private accBankReconciliationDAO accBankReconciliationObj;
    private accGoodsReceiptModuleService accGoodsReceiptModuleService;
    private accSalesOrderService accSalesOrderServiceobj;
    private accAccountDAO accAccountDAOobj;
    private fieldManagerDAO fieldManagerDAOobj;
    String tranID ="";
    String recId ="";
    private InterStoreTransferService istService;
    private StockService stockService;
    private exportMPXDAOImpl exportDaoObj;
    private CommonFnControllerService commonFnControllerService;
    private ImportPurchaseInvoice ImportPurchaseInvoiceobj;

    public void setImportPurchaseInvoice(ImportPurchaseInvoice ImportPurchaseInvoiceobj) {
        this.ImportPurchaseInvoiceobj = ImportPurchaseInvoiceobj;
    }
    public void setAccSalesOrderServiceobj(accSalesOrderService accSalesOrderServiceobj) {
        this.accSalesOrderServiceobj = accSalesOrderServiceobj;
    }

    public void setIstService(InterStoreTransferService istService) {
        this.istService = istService;
    }

    public void setStockService(StockService stockService) {
        this.stockService = stockService;
    }
    public void setFieldManagerDAOobj(fieldManagerDAO fieldManagerDAOobj) {
        this.fieldManagerDAOobj = fieldManagerDAOobj;
    }
    
    public void setaccTermDAO(accTermDAO accTermObj) {
        this.accTermObj = accTermObj;
    }
        
    public void setaccPaymentDAO(accPaymentDAO accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setStockMovementService(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    public void setJournalEntryModuleServiceobj(AccJournalEntryModuleService journalEntryModuleServiceobj) {
        this.journalEntryModuleServiceobj = journalEntryModuleServiceobj;
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

    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    public void setimportDAO(ImportDAO importDao) {
        this.importDao = importDao;
    }

    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setaccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
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

    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }

    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setaccPurchaseOrderDAO(accPurchaseOrderDAO accPurchaseOrderobj) {
        this.accPurchaseOrderobj = accPurchaseOrderobj;
    }

    public void setaccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }

    public void setaccBankReconciliationDAO(accBankReconciliationDAO accBankReconciliationObj) {
        this.accBankReconciliationObj = accBankReconciliationObj;
    }
    
    public void setAccGoodsReceiptModuleService(accGoodsReceiptModuleService accGoodsReceiptModuleService) {
        this.accGoodsReceiptModuleService = accGoodsReceiptModuleService;
    }

    public void setAccAccountDAOobj(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }
    
    public void setCommonFnControllerService(CommonFnControllerService commonFnControllerService) {
        this.commonFnControllerService = commonFnControllerService;
    }
    
    public ModelAndView saveGoodsReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String channelName = "";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            String url = this.getServletContext().getInitParameter(Constants.inventoryURL);
            paramJobj.put(Constants.inventoryURL, url);
            String baseUrl = URLUtil.getPageURL(request, loginpageFull);
            paramJobj.put("baseUrl", baseUrl);
            jobj = accGoodsReceiptModuleService.saveGoodsReceipt(paramJobj);
                issuccess = jobj.optBoolean(Constants.RES_success, false);
            channelName = jobj.optString(Constants.channelName, null);
            jobj.put(Constants.userSessionId, (request.getSession(true)).getAttribute(Constants.userSessionId));// adding user session id to idenntify unique user session
            return new ModelAndView("jsonView", "model", jobj.toString());
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);

        } finally {
            if (issuccess && !StringUtil.isNullOrEmpty(channelName)) {
                ServerEventManager.publish(channelName, jobj.toString(), (ServletContext) this.getServletContext());
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView UpdateGoodsReceiptFormDetails(HttpServletRequest request, HttpServletResponse response) {
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
            List li = updateGoodsReceiptFormDetails(request);
            txnManager.commit(status);
            issuccess = true;
            /*
             * * To refresh a Invoice List.
             */
            channelName = "/VendorInvoiceAndCashPurchaseReport/gridAutoRefresh";
            /*
             * * Composing the message to display after save operation.
             */
//            msg = (iscash ? messageSource.getMessage("acc.vendor.cashinv.update", null, RequestContextUtils.getLocale(request)) : messageSource.getMessage("acc.vendor.inv.update", null, RequestContextUtils.getLocale(request))) + " " + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + invoiceNumber + ".</b> " + (messageSource.getMessage("acc.field.JENo", null, RequestContextUtils.getLocale(request)) + ": <b>" + jeNumber + "</b>");
            /*
             * * Composing the message to insert into Audit Trail.
             */
            String action = "updated";
//            auditTrailObj.insertAuditLog(AuditAction.INVOICE_UPDATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + (" Vendor Invoice ") + recId, request, tranID);

        } catch (SessionExpiredException | ServiceException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AccountingException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
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
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    private HashSet<GoodsReceiptDetail> updateGoodsReceiptRows(String invoiceDetails, JournalEntry je, int moduleid, String companyid) throws ServiceException, JSONException {
        HashSet<GoodsReceiptDetail> rows = new HashSet<>();
        try {
            JSONArray jArr = new JSONArray(invoiceDetails);
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                String linkto = jobj.getString("linkto");
                GoodsReceiptDetail row = null;
                if (jobj.has("rowid")) {
                    KwlReturnObject invDetailsResult = accountingHandlerDAOobj.getObject(GoodsReceiptDetail.class.getName(),  StringUtil.isNullOrEmpty(linkto)?jobj.getString("rowid"):jobj.getString("docrowid"));
                    row = (GoodsReceiptDetail) invDetailsResult.getEntityList().get(0);
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
                            row.setDescription( StringUtil.DecodeText(jobj.optString("desc")));
                        } catch (Exception ex) {
                            row.setDescription(jobj.optString("desc"));
                        }
                    } else{
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
                        customrequestParams.put("modulerecid", row.getPurchaseJED().getID()); //Pls confirm
                        customrequestParams.put("recdetailId", row.getInventory().getID());
                        customrequestParams.put("moduleid", moduleid);
                        customrequestParams.put("companyid", companyid);
                        customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            jedjson.put("accjedetailcustomdata", row.getPurchaseJED().getID());
                            jedjson.put("jedid", row.getPurchaseJED().getID());
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
                        filter_params.add(row.getPurchaseJED().getID());
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
                            jedjson.put("companyid", row.getPurchaseJED().getCompany().getCompanyID());
                            jedjson.put("amount", row.getPurchaseJED().getAmount());
                            jedjson.put("accountid", row.getGoodsReceipt().getAccount().getID());
                            jedjson.put("debit", !row.getPurchaseJED().isDebit());
                            jedjson.put("jeid", row.getPurchaseJED().getJournalEntry().getID());
                            jedjson.put("mainjedid", row.getPurchaseJED().getID());
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
                             * Tagging new dimension value to additional
                             * jedetail against tax account in linking case.
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
                                        /**
                                         * SDP-14699 : Create New 'jedjson'
                                         * Object for updating existing JED.
                                         */
                                        jedjson = new JSONObject();
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
                                jedjson.put("accountid", row.getGoodsReceipt().getAccount().getID());
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
                        customrequestParams.put("modulerecid", row.getPurchaseJED().getID());
                        customrequestParams.put("moduleid", moduleid);
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
                            jedjson.put("accjedetailproductcustomdataref", row.getPurchaseJED().getID());
                            jedjson.put("jedid", row.getPurchaseJED().getID());
                            KwlReturnObject jedresult = accJournalEntryobj.updateJournalEntryDetails(jedjson);
                        }
                    }
                    rows.add(row);
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("updateInvoiceRows : " + ex.getMessage(), ex);
        } catch (Exception e) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, e);
        }
        return rows;
    }
    private HashSet<ExpenseGRDetail> updateExpenseGoodsReceiptRows(String invoiceDetails, JournalEntry je, int moduleid, String companyid) throws ServiceException, JSONException {
        HashSet<ExpenseGRDetail> rows = new HashSet<>();
        try {
            JSONArray jArr = new JSONArray(invoiceDetails);
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                ExpenseGRDetail row = null;
                if (jobj.has("rowid")) {
                    KwlReturnObject invDetailsResult = accountingHandlerDAOobj.getObject(ExpenseGRDetail.class.getName(), jobj.getString("rowid"));
                    row = (ExpenseGRDetail) invDetailsResult.getEntityList().get(0);
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
                            row.setDescription( StringUtil.DecodeText(jobj.optString("desc")));
                        } catch (Exception ex) {
                            row.setDescription(jobj.optString("desc"));
                        }
                    }

                    if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", ""))) {
                        JSONArray jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                        customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);
                        customrequestParams.put("modulerecid", row.getPurchaseJED().getID());
                        customrequestParams.put("recdetailId", row.getID());
                        customrequestParams.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);
                        customrequestParams.put("companyid", row.getCompany().getCompanyID());
                        customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            JSONObject jedjson = new JSONObject();
                            jedjson.put("accjedetailcustomdata", row.getPurchaseJED().getID());
                            jedjson.put("jedid", row.getPurchaseJED().getID());
                            jedjson.put(DESCRIPTION,  StringUtil.DecodeText(jobj.getString(DESC)));
                            KwlReturnObject jedresult = accJournalEntryobj.updateJournalEntryDetails(jedjson);
                        }
                        }
                    rows.add(row);
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("updateInvoiceRows : " + ex.getMessage(), ex);
        } catch (Exception e) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, e);
        }
        return rows;
    }

    public List updateGoodsReceipt(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException, UnsupportedEncodingException {
        String id = null;
        List ll = new ArrayList();
        ArrayList discountArr = new ArrayList();
        String invoiceid = null;
        GoodsReceipt invoice = null;
        try {
            DateFormat userdf = authHandler.getDateOnlyFormat(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            invoiceid = request.getParameter("invoiceid");
            boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false;
            boolean isLeaseFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isLeaseFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isLeaseFixedAsset")) : false;
            boolean isConsignment = (!StringUtil.isNullOrEmpty(request.getParameter("isConsignment"))) ? Boolean.parseBoolean(request.getParameter("isConsignment")) : false;
            int moduleid = isFixedAsset ? Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId : isConsignment?Constants.Acc_Consignment_GoodsReceipt_ModuleId:Constants.Acc_Vendor_Invoice_ModuleId;
            /*
             * To update the following items which is not affecting the amount
             * and linking of the Invoice.
             */
            HashMap<String, Object> invoicePrmt = new HashMap<>();
            if (!StringUtil.isNullOrEmpty(invoiceid)) {
                invoicePrmt.put("grid", invoiceid);
            }
            invoicePrmt.put(GoodsReceiptConstants.MEMO, request.getParameter(GoodsReceiptConstants.MEMO) == null ? "" : request.getParameter(GoodsReceiptConstants.MEMO));
            invoicePrmt.put(GoodsReceiptConstants.BILLTO, request.getParameter(GoodsReceiptConstants.BILLTO) == null ? "" : request.getParameter(GoodsReceiptConstants.BILLTO));
            invoicePrmt.put(GoodsReceiptConstants.SHIPADDRESS, request.getParameter(GoodsReceiptConstants.SHIPADDRESS) == null ? "" : request.getParameter(GoodsReceiptConstants.SHIPADDRESS));
            if (request.getParameter(GoodsReceiptConstants.SHIPDATE) != null && !StringUtil.isNullOrEmpty(request.getParameter(GoodsReceiptConstants.SHIPDATE))) {
                invoicePrmt.put(GoodsReceiptConstants.SHIPDATE, userdf.parse(request.getParameter(GoodsReceiptConstants.SHIPDATE)));
            }
            String costcenterid = request.getParameter("costcenter")== null ? "" : request.getParameter("costcenter");
            invoicePrmt.put("porefno", request.getParameter("porefno") == null ? "" : request.getParameter("porefno"));
            invoicePrmt.put("companyid", companyid);
            invoicePrmt.put("salesPerson", request.getParameter("salesPerson") == null ? "" : request.getParameter("salesPerson"));
            invoicePrmt.put("agent", request.getParameter("agent") == null ? "" : request.getParameter("agent"));
            invoicePrmt.put("shipvia", request.getParameter("shipvia"));
            invoicePrmt.put("fob", request.getParameter("fob") == null ? "" : request.getParameter("fob"));
            invoicePrmt.put("modifiedby", sessionHandlerImpl.getUserid(request));
            invoicePrmt.put("updatedon", System.currentTimeMillis());
            invoicePrmt.put("posttext", request.getParameter("posttext"));
            if (!StringUtil.isNullOrEmpty(request.getParameter("deliveryTime"))) {
                invoicePrmt.put("deliveryTime", request.getParameter("deliveryTime"));
            }

            KwlReturnObject invResult = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invoiceid);
            invoice = (GoodsReceipt) invResult.getEntityList().get(0);

            Map<String, Object> addressParams = new HashMap<String, Object>();
            String billingAddress = request.getParameter(Constants.BILLING_ADDRESS);
            if (!StringUtil.isNullOrEmpty(billingAddress)) {
                addressParams = AccountingAddressManager.getAddressParams(request, false);
            } else {
                addressParams = AccountingAddressManager.getDefaultCustomerAddressParams(invoice.getVendor().getID(), companyid, accountingHandlerDAOobj);// addressParams = getCustomerDefaultAddressParams(customer,companyid);
            }
            BillingShippingAddresses bsa = invoice.getBillingShippingAddresses();//used to update billing shipping addresses
            addressParams.put("id", bsa != null ? bsa.getID() : "");
            KwlReturnObject addressresult = accountingHandlerDAOobj.saveAddressDetail(addressParams, companyid);
            bsa = (BillingShippingAddresses) addressresult.getEntityList().get(0);
            String addressid = bsa.getID();
            invoicePrmt.put("billshipAddressid", addressid);
            /*
             * Updating line item information.
             */
            Map<String,Object>requestParams =  AccountingManager.getGlobalParams(request);
            String transactionDateStr = request.getParameter("billdate");
            if (!StringUtil.isNullOrEmpty(transactionDateStr)) {
                Date transactionDate = userdf.parse(transactionDateStr);

                //ERROR PRONE CODE. VERIFY IT CAREFULLY - Book Begining Date & Transaction Date.
                transactionDate = CompanyPreferencesCMN.removeTimefromDate(transactionDate);

                CompanyPreferencesCMN.checkLockPeriod(accCompanyPreferencesObj, requestParams, transactionDate, true);
            }

            if (!StringUtil.isNullOrEmpty(request.getParameter(DETAIL))) {
                String invoiceDetails = request.getParameter("detail");
                updateGoodsReceiptRows(invoiceDetails, invoice.getJournalEntry(), moduleid, companyid);
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter(EXPENSEDETAIL))) {
                String invoiceDetails = request.getParameter(EXPENSEDETAIL);
                updateExpenseGoodsReceiptRows(invoiceDetails, invoice.getJournalEntry(), moduleid, companyid);
            }
            
//            invoicePrmt.put("grdetails", invDetails);
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
                    jeDataMap.put(CCConstants.JSON_costcenterid, costcenterid);
                    KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);
                }
            }

            KwlReturnObject result = accGoodsReceiptobj.updateGoodsReceipt(invoicePrmt);
            invoice = (GoodsReceipt) result.getEntityList().get(0);//Create Invoice without invoice-details.
            id = invoice.getID();
            /*
             * Data for return information.
             */
            String personalid = invoice.getVendor().getAccount().getID();
            String accname = invoice.getVendor().getAccount().getName();
            String invoiceno = invoice.getGoodsReceiptNumber();
            String address = invoice.getVendor().getAddress();
            String fullShippingAddress = "";
            if (invoice.getBillingShippingAddresses() != null) {
                fullShippingAddress = invoice.getBillingShippingAddresses().getFullShippingAddress();
            }
            
            /*
             Set cost center value 
             */
            if (!StringUtil.isNullOrEmpty(costcenterid) && invoice.getJournalEntry()!=null) {
                KwlReturnObject cap = accountingHandlerDAOobj.getObject(CostCenter.class.getName(), costcenterid);
                CostCenter costCenter = (CostCenter) cap.getEntityList().get(0);
                invoice.getJournalEntry().setCostcenter(costCenter);
            }else{
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
            ll.add(invoice.getInvoiceAmount());
            ll.add(invoice.getJournalEntry().getEntryNumber());
            ll.add(fullShippingAddress);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (AccountingException e) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, e);
            throw ServiceException.FAILURE(e.getMessage(), "erp24", false);
        } catch (Exception e) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, e);
        }
        return ll;
    }
    public List updateGoodsReceiptFormDetails(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException, UnsupportedEncodingException {
        String id = null;
        List ll = new ArrayList();
        JSONArray jArr = null;
        String invoiceid = null;
        GoodsReceipt invoice = null;
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            if (!StringUtil.isNullOrEmpty(request.getParameter("idsArray"))) {
                jArr = new JSONArray(request.getParameter("idsArray"));
                for (int i = 0; i < jArr.length(); i++) {
                    invoiceid = (String) jArr.get(i);
                    HashMap<String, Object> invoicePrmt = new HashMap<>();
                    if (!StringUtil.isNullOrEmpty(invoiceid)) {
                        invoicePrmt.put("grid", invoiceid);
                    }
                    invoicePrmt.put("companyid", companyid);
                    invoicePrmt.put("FormSeriesNo", request.getParameter("FormSeriesNo") == null ? "" : request.getParameter("FormSeriesNo"));
                    invoicePrmt.put("FormNo", request.getParameter("FormNo") == null ? "" : request.getParameter("FormNo"));
                    invoicePrmt.put("FormDate", request.getParameter("FormDate") == null ? "" : df.parse(request.getParameter("FormDate")));
                    invoicePrmt.put("FormAmount", request.getParameter("FormAmount") == null ? 0: Double.parseDouble(request.getParameter("FormAmount")));
                    invoicePrmt.put("FormStatus", "3");
                    KwlReturnObject result = accGoodsReceiptobj.updateGoodsReceipt(invoicePrmt);
                    invoice = (GoodsReceipt) result.getEntityList().get(0);
                    id = invoice.getID();
                    String invoiceno = invoice.getGoodsReceiptNumber();
                    ArrayList returnList = new ArrayList();
                    returnList.add(id);
                    returnList.add(invoiceno);
                    returnList.add(invoice.getJournalEntry().getEntryNumber());
                    ll.add(returnList);
                }
                    ll.add("Submitted");
            }
        } catch (Exception e) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, e);
        }
        return ll;
    }

    public ModelAndView updateLinkedGoodsReceipt(HttpServletRequest request, HttpServletResponse response) {
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
            List li = updateGoodsReceipt(request);
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
                if (isFixedAsset) {
                    channelName = "/FixedAssetAcquiredInvoiceList/gridAutoRefresh";
                } else {
                    channelName = "/VendorInvoiceAndCashPurchaseReport/gridAutoRefresh";
                }
                /*
                 * Composing the message to display after save operation.
                 */

                if (isConsignment) {
                    msg = (iscash ? messageSource.getMessage("acc.gr.update", null, RequestContextUtils.getLocale(request)) : messageSource.getMessage("acc.consignment.veninv.update", null, RequestContextUtils.getLocale(request))) + " " + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + invoiceNumber + ",</b> " + messageSource.getMessage("acc.field.JENo", null, RequestContextUtils.getLocale(request)) + ": <b>" + jeNumber + "</b>";
                } else {
                    msg = (iscash ? messageSource.getMessage("acc.vendor.cashinv.update", null, RequestContextUtils.getLocale(request)) : messageSource.getMessage("acc.vendor.inv.update", null, RequestContextUtils.getLocale(request))) + " " + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + invoiceNumber + ".</b> " + (messageSource.getMessage("acc.field.JENo", null, RequestContextUtils.getLocale(request)) + ": <b>" + jeNumber + "</b>");
                }
                /*
                 * Composing the message to insert into Audit Trail.
                 */
                String action = "updated";
                if (isLeaseFixedAsset) {
                    action += " Lease";
                }
                auditTrailObj.insertAuditLog(AuditAction.INVOICE_UPDATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + (isConsignment ? " Consignment Vendor Invoice " : " Vendor Invoice ") + recId, request, tranID);//    ERP-18011
            }

        } catch (SessionExpiredException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AccountingException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            isAccountingExe = true;
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
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
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
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
            requestParams.put("moduleName", "Opening Purchase Invoice");
            requestParams.put("moduleid", Constants.Acc_opening_Prchase_Invoice);
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
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }

            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void validateHeaders(JSONArray validateJArray) throws AccountingException, ServiceException {
        try {

            List<String> list = new ArrayList<String>();
            list.add("Transaction Number");
            list.add("Transaction Date");
            list.add("Amount");
            list.add("Due Date");
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
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    /*
     * Method to save Opening Balance Invoices For Vendor.
     */
    public ModelAndView saveOpeningBalanceGoodsReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        boolean isAccountingExe=false;
        String msg = "";
        String auditAction = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;//txnManager.getTransaction(def);
        String companyid ="";
        String entryNumber = request.getParameter(NUMBER);
        String invoiceid = request.getParameter("transactionId");
        try {
            companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject cncount=null;
            /*
                Checks duplicate number and sequence format number in add case 
            */
            if (StringUtil.isNullOrEmpty(invoiceid)) {
                /*
                 * chek duplicate number
                 */
                cncount = accGoodsReceiptobj.getReceiptFromNo(entryNumber, companyid);
                if (cncount.getRecordTotalCount() > 0) {
                    isAccountingExe=true;
                    throw new AccountingException(messageSource.getMessage("acc.INV.purchaseinvno", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                }
                /*
                 * code for checking wheather entered number can be generated by
                 * sequence format or not
                 */
                List resultList = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Vendor_Invoice_ModuleId, entryNumber, companyid);
                if (!resultList.isEmpty()) {
                    boolean isvalidEntryNumber = (Boolean) resultList.get(0);
                    String formatName = (String) resultList.get(1);
                    if (!isvalidEntryNumber) {
                        isAccountingExe=true;
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
                    KwlReturnObject resultInv = accCommonTablesDAO.getTransactionInTemp(entryNumber, companyid, Constants.Acc_Vendor_Invoice_ModuleId);
                    if (resultInv.getRecordTotalCount() > 0) {
                        isAccountingExe=true;
                        throw new AccountingException(messageSource.getMessage("acc.INV.selectedpurchaseinvno", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyinprocess.", null, RequestContextUtils.getLocale(request)));
                    } else {
                        /*
                            Insert entry in temporary table
                        */
                        accCommonTablesDAO.insertTransactionInTemp(entryNumber, companyid, Constants.Acc_Vendor_Invoice_ModuleId);
                    }
                txnManager.commit(status);
            }
            status = txnManager.getTransaction(def);
            List li = saveOpeningBalanceGoodsReceipt(request);
            boolean isEditInv = false;
            String succMsg = messageSource.getMessage("acc.field.saved",null,RequestContextUtils.getLocale(request));
            String invoiceNumber = "";
            if (!li.isEmpty()) {
                invoiceNumber = li.get(0).toString();
                jobj.put("invoiceNumber", invoiceNumber);
                isEditInv = (Boolean) li.get(1);
            }
            issuccess = true;
            if (isEditInv) {
                succMsg =messageSource.getMessage("acc.field.updated", null, RequestContextUtils.getLocale(request));
            }
            msg = messageSource.getMessage("acc.accPref.autoVI", null, RequestContextUtils.getLocale(request)) + " " + invoiceNumber + " " + messageSource.getMessage("acc.field.hasbeen", null, RequestContextUtils.getLocale(request)) + " " + succMsg + " " + messageSource.getMessage("acc.field.successfully", null, RequestContextUtils.getLocale(request));
            /*
                Delete entry from temporary table
            */
            accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid,Constants.Acc_Vendor_Invoice_ModuleId);
            txnManager.commit(status);
        } catch (JSONException ex) {
            if(status!=null){
                txnManager.rollback(status);
            }
            try {
                /*
                    Delete entry from temporary table
                */
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid,Constants.Acc_Vendor_Invoice_ModuleId);
            } catch (ServiceException ex1) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ParseException ex) {
            if(status!=null){
                txnManager.rollback(status);
            }
            try {
                /*
                    Delete entry from temporary table
                */
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid,Constants.Acc_Vendor_Invoice_ModuleId);
            } catch (ServiceException ex1) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (AccountingException ex) {
            if(status!=null){
                txnManager.rollback(status);
            }
            try {
                /*
                    Delete entry from temporary table
                */
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid,Constants.Acc_Vendor_Invoice_ModuleId);
            } catch (ServiceException ex1) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            if(status!=null){
                txnManager.rollback(status);
            }
            try {
                /*
                    Delete entry from temporary table
                */
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid,Constants.Acc_Vendor_Invoice_ModuleId);
            } catch (ServiceException ex1) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            if(status!=null){
                txnManager.rollback(status);
            }
            try {
                /*
                    Delete entry from temporary table
                */
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid,Constants.Acc_Vendor_Invoice_ModuleId);
            } catch (ServiceException ex1) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        }catch (Exception ex) {
            if(status!=null){
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
            if(ex.getMessage()==null){
                msg = "" + ex.getCause().getMessage();
            }
            try {
                /*
                    Delete entry from temporary table
                */
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid,Constants.Acc_Invoice_ModuleId);
            } catch (ServiceException ex1) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("accException", isAccountingExe);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public List saveOpeningBalanceGoodsReceipt(HttpServletRequest request) throws ParseException, AccountingException, ServiceException {
        List returnList = new ArrayList();
        try {
            Map<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject result = null;
            int nocount = 0;
            boolean isEditInvoice = false;
            GoodsReceipt gr = null;
            String auditMsg = "", auditID = "", memo="";
            // Fetching request parameters

            String invoiceNumber = request.getParameter("number");
            String transactionDateStr = request.getParameter("billdate");
            String dueDateStr = request.getParameter("dueDate");
            String currencyid = (request.getParameter("currencyid") == null ? sessionHandlerImpl.getCurrencyID(request) : request.getParameter("currencyid"));
            String transactionAmountStr = request.getParameter("transactionAmount");
            String partyInvoiceDateStr = request.getParameter("poRefDate");
            String salesPerson = request.getParameter("salesPerson");
            String invoiceid = request.getParameter("transactionId");
            String partyInvoiceNumber = request.getParameter("porefno");
            String vendorId = request.getParameter("accountId");
            String termId = request.getParameter("termid");
            double exchangeRateForOpeningTransaction = 1;
            if (!StringUtil.isNullOrEmpty(request.getParameter("exchangeRateForOpeningTransaction"))) {
                exchangeRateForOpeningTransaction = Double.parseDouble(request.getParameter("exchangeRateForOpeningTransaction"));
            }
            double externalCurrencyRate = StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            boolean conversionRateFromCurrencyToBase = true;
            if (request.getParameter("CurrencyToBaseExchangeRate") != null) {
                conversionRateFromCurrencyToBase = Boolean.parseBoolean(request.getParameter("CurrencyToBaseExchangeRate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("memo"))) {
                memo = request.getParameter("memo").toString();
            }

            Date transactionDate = df.parse(df.format(new Date()));
            Date dueDate = df.parse(df.format(new Date()));
            Date partyInvoiceDate = df.parse(df.format(new Date()));

            if (!StringUtil.isNullOrEmpty(transactionDateStr)) {
                transactionDate = df.parse(transactionDateStr);
            }
            if (!StringUtil.isNullOrEmpty(dueDateStr)) {
                dueDate = df.parse(dueDateStr);
            }
            if (!StringUtil.isNullOrEmpty(partyInvoiceDateStr)) {
                partyInvoiceDate = df.parse(partyInvoiceDateStr);
            }

            double transactionAmount = 0d;
            if (!StringUtil.isNullOrEmpty(transactionAmountStr)) {
                transactionAmount = Double.parseDouble(transactionAmountStr);
            }


            Map<String, Object> greceipthm = new HashMap<String, Object>();

            if (!StringUtil.isNullOrEmpty(invoiceid)) {
                greceipthm.put(GRID, invoiceid);
                isEditInvoice = true;
            }

            // data processing for invoice auto number

            if (StringUtil.isNullOrEmpty(invoiceid)) {
                result = accGoodsReceiptobj.getReceiptFromNo(invoiceNumber, companyid);
                nocount = result.getRecordTotalCount();
                if (nocount > 0) {
                    throw new AccountingException(messageSource.getMessage("acc.field.VendorInvoicenumber", null, RequestContextUtils.getLocale(request)) + " " + invoiceNumber + " " + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                }
                greceipthm.put(ENTRYNUMBER, invoiceNumber);
                greceipthm.put(AUTOGENERATED, false);
            }

            greceipthm.put(MEMO, "");
            greceipthm.put(BILLTO, "");
            greceipthm.put(SHIPADDRESS, "");

            greceipthm.put(DUEDATE, dueDate);
            greceipthm.put(CURRENCYID, currencyid);
            greceipthm.put(COMPANYID, companyid);
            greceipthm.put(termid, termId);
            greceipthm.put("externalCurrencyRate", externalCurrencyRate);
            greceipthm.put("venbilladdress", "");
            greceipthm.put("venshipaddress", "");

            greceipthm.put("partyInvoiceNumber", partyInvoiceNumber);
            greceipthm.put("conversionRateFromCurrencyToBase", conversionRateFromCurrencyToBase);

//            String vendorId = request.getParameter("vendor");
            String accountid = vendorId;
            KwlReturnObject custresult = null;
            if (!StringUtil.isNullOrEmpty(vendorId)) {
                custresult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), vendorId);
                Vendor vendor = (Vendor) custresult.getEntityList().get(0);
                if (vendor.getAccount() != null) {
                    accountid = vendor.getAccount().getID();
                }
            }

            String erdid = null;
//            Date billDate=request.getParameter(BILLDATE)==null?null:df.parse(request.getParameter(BILLDATE));
//            KwlReturnObject ERresult = accCurrencyDAOobj.getExcDetailID(requestParams, currencyid, transactionDate, null);
//            List ERlist = ERresult.getEntityList();
//            if(!ERlist.isEmpty()) {
//                Iterator itr = ERlist.iterator();
//                ExchangeRateDetails erd = (ExchangeRateDetails) itr.next();
//                erdid = erd.getID();
//            }
            greceipthm.put(ERDID, erdid);

            greceipthm.put(VENDORID, vendorId);
            greceipthm.put(ACCOUNTID, accountid);
            greceipthm.put("salesPerson", salesPerson);
            greceipthm.put("exchangeRateForOpeningTransaction", exchangeRateForOpeningTransaction);
            greceipthm.put("creationDate", transactionDate);
            greceipthm.put("originalOpeningBalanceAmount", transactionAmount);
            greceipthm.put("openingBalanceAmountDue", transactionAmount);
            // Store invoice amount in base currency
            if (conversionRateFromCurrencyToBase) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                greceipthm.put(Constants.originalOpeningBalanceBaseAmount, authHandler.round(transactionAmount * exchangeRateForOpeningTransaction, companyid));
                greceipthm.put(Constants.openingBalanceBaseAmountDue, authHandler.round(transactionAmount * exchangeRateForOpeningTransaction, companyid));
            } else {
                greceipthm.put(Constants.originalOpeningBalanceBaseAmount, authHandler.round(transactionAmount / exchangeRateForOpeningTransaction, companyid));
                greceipthm.put(Constants.openingBalanceBaseAmountDue, authHandler.round(transactionAmount / exchangeRateForOpeningTransaction, companyid));
            }
            
            if (!StringUtil.isNullOrEmpty(request.getParameter("excludingGstAmount"))) {
                double excludingGstAmount = 0d;
                excludingGstAmount = Double.parseDouble(request.getParameter("excludingGstAmount"));
                greceipthm.put("excludingGstAmount", excludingGstAmount);
                if (conversionRateFromCurrencyToBase) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                    greceipthm.put("excludingGstAmountInBase", authHandler.round(excludingGstAmount * exchangeRateForOpeningTransaction, companyid));
                } else {
                    greceipthm.put("excludingGstAmountInBase", authHandler.round(excludingGstAmount / exchangeRateForOpeningTransaction, companyid));
                }
            }
            
            if (!StringUtil.isNullOrEmpty(request.getParameter("taxAmount"))) {
                double taxAmount = 0d;
                taxAmount=Double.parseDouble(request.getParameter("taxAmount"));
                greceipthm.put("taxAmount", taxAmount);
                if (conversionRateFromCurrencyToBase) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                    greceipthm.put("taxAmountInBase", authHandler.round(taxAmount * exchangeRateForOpeningTransaction, companyid));
                } else {
                    greceipthm.put("taxAmountInBase", authHandler.round(taxAmount / exchangeRateForOpeningTransaction, companyid));
                }
            }
            
            greceipthm.put("partyInvoiceDate", partyInvoiceDate);
            greceipthm.put("isOpeningBalenceInvoice", true);
            greceipthm.put("isNormalInvoice", false);
            greceipthm.put(INCASH, request.getParameter(INCASH));
            greceipthm.put("memo", memo);

            if (isEditInvoice) {

                boolean isGRUsedInOtherTransactions = isGRUsedInOtherTransactions(invoiceid, companyid);
                if (isGRUsedInOtherTransactions) {
                    throw new AccountingException(messageSource.getMessage("acc.nee.73", null, RequestContextUtils.getLocale(request)));
                }

                result = accGoodsReceiptobj.updateGoodsReceipt(greceipthm);
                auditMsg = "updated";
                auditID = AuditAction.OPENING_BALANCE_UPDATED;
            } else {
                 greceipthm.put(Constants.Checklocktransactiondate, request.getParameter("billdate"));
                result = accGoodsReceiptobj.addGoodsReceipt(greceipthm);
                auditMsg = "added";
                auditID = AuditAction.OPENING_BALANCE_CREATED;
            }

            gr = (GoodsReceipt) result.getEntityList().get(0);

            returnList.add(gr.getGoodsReceiptNumber());
            returnList.add(isEditInvoice);
            String customfield = request.getParameter("customfield");
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_OpeningBalanceVendorInvoice_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_OpeningBalanceVendorInvoiceid);
                customrequestParams.put("modulerecid", gr.getID());
                customrequestParams.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", "com.krawler.hql.accounting.OpeningBalanceVendorInvoiceCustomData");
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    greceipthm.put(GRID, gr.getID());
                    greceipthm.put("openingBalanceVendorInvoiceCustomData", gr.getID());
                    result = accGoodsReceiptobj.updateGoodsReceipt(greceipthm);
                }
            }
            auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has  " + auditMsg + " an Opening Balance Vendor Invoice " + invoiceNumber, request, invoiceNumber);
        } catch (JSONException jex) {
            throw ServiceException.FAILURE("saveOpeningBalanceGoodsReceipt : " + jex.getMessage(), jex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return returnList;
    }

    private boolean isGRUsedInOtherTransactions(String grId, String companyId) throws ServiceException {
        boolean isGRUsedInOtherTransactions = false;
        KwlReturnObject result;
        if (!StringUtil.isNullOrEmpty(grId)) {
            isGRUsedInOtherTransactions = accGoodsReceiptobj.isGRUsedInDebitNote(grId, companyId);
        }
        return isGRUsedInOtherTransactions;
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
        KwlReturnObject resultObj = null;
        GoodsReceipt gr = null;
        String customfield = "";
        HashMap<String, FieldParams> customFieldParamMap = new HashMap<String, FieldParams>();
        JSONObject returnObj = new JSONObject();

        try {

            String dateFormat ="yyyy-MM-dd", dateFormatId = request.getParameter("dateFormat");
            if (!StringUtil.isNullOrEmpty(dateFormatId)) {
                KwlReturnObject kdfObj = accountingHandlerDAOobj.getObject(KWLDateFormat.class.getName(), dateFormatId);
                KWLDateFormat kdf = (KWLDateFormat) kdfObj.getEntityList().get(0);
                if(kdf!=null){
                    dateFormat =kdf.getJavaForm(); 
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
            HashMap currencyMap = accGoodsReceiptModuleService.getCurrencyMap(isCurrencyCode);
            Set transactionNumberSet = new HashSet();

            while (csvReader.readRecord()) {
                String failureMsg = "";
                String[] recarr = csvReader.getValues();
                if (cnt == 0) {//Putting Header in failure File
                    failedRecords.append(accGoodsReceiptModuleService.createCSVrecord(recarr) + "\"Error Message\"");//failedRecords.append("\"Row No.\","+createCSVrecord(fileData)+"\"Error Message\""); 
                } else {
                    try {
                        /*
                         * 1. Invoice Number
                         */
                        String invoiceNumber = "";
                        if (columnConfig.containsKey("GoodsReceiptNumber")) {
                            invoiceNumber = recarr[(Integer) columnConfig.get("GoodsReceiptNumber")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(invoiceNumber)) {
                                throw new AccountingException("Empty data found in Transaction Number, cannot set empty data for Transaction Number.");
                            } else if(!transactionNumberSet.add(invoiceNumber)){// this method retur true when added or false when already exit record not get added
                                throw new AccountingException("Duplicate Transaction Number '" + invoiceNumber + "' in file.");
                            } else {
                                KwlReturnObject InvoiceResult = accGoodsReceiptobj.getReceiptFromNo(invoiceNumber, companyid);
                                int nocount = InvoiceResult.getRecordTotalCount();
                                if (nocount > 0) {
                                    throw new AccountingException(messageSource.getMessage("acc.field.VendorInvoicenumber", null, RequestContextUtils.getLocale(request)) + invoiceNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                                }
                            }
                            
                            JSONObject configObj = configMap.get("GoodsReceiptNumber");
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
                        
                         /*2. Customer Code*/
                        String vendorCode="";
                        if (columnConfig.containsKey("VendorCode")) {
                            vendorCode = recarr[(Integer) columnConfig.get("VendorCode")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(vendorCode)) {
                                Vendor vendor = accGoodsReceiptModuleService.getVendorByCode(vendorCode, companyid);
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
                        if (StringUtil.isNullOrEmpty(vendorId)) {
                                String vendorName="";
                            if (columnConfig.containsKey("VendorName")) {
                                vendorName = recarr[(Integer) columnConfig.get("VendorName")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(vendorName)) {
                                    Vendor vendor = accGoodsReceiptModuleService.getVendorByName(vendorName, companyid);
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

                        /*4. Creation Date*/
                        String transactionDateStr = "";
                        boolean istransactionDateValid=true;
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
                                        istransactionDateValid=false;
                                        failureMsg +=messageSource.getMessage("acc.transactiondate.beforebbdate", null, RequestContextUtils.getLocale(request));
                                    }
                                    CompanyPreferencesCMN.checkLockPeriod(accCompanyPreferencesObj, requestParams, transactionDate, true);
                                } catch (ParseException ex) {
                                    istransactionDateValid=false;
                                    failureMsg +="Incorrect date format for Transaction Date, Please specify values in " + dateFormat + " format.";
                                } catch (Exception ex) {
                                    istransactionDateValid=false;
                                    failureMsg += ex.getMessage();
                                }
                            } else {
                                istransactionDateValid=false;
                                failureMsg +=messageSource.getMessage("acc.field.TransactionDateisnotavailable", null, RequestContextUtils.getLocale(request));
                            }
                        } else {
                            istransactionDateValid=false;
                            failureMsg +=messageSource.getMessage("acc.field.TransactionDateisnotavailable", null, RequestContextUtils.getLocale(request));
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
                        
                        if(istransactionDateValid && dueDate != null && dueDate.before(transactionDate)){
                            failureMsg +=messageSource.getMessage("acc.field.duedatebeforTransactionDate", null, RequestContextUtils.getLocale(request));
                        }
                        
                       /*6. Debit Term */
                        String termID="";
                        if (columnConfig.containsKey("Termid")) {
                            String termName = recarr[(Integer) columnConfig.get("Termid")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(termName)) {
                                HashMap<String, Object> termMap = new HashMap<String, Object>();
                                termMap.put("companyid", companyid);
                                termMap.put("termname", termName);
                                KwlReturnObject termResult = accTermObj.getTerm(termMap);
                                if (termResult != null && !termResult.getEntityList().isEmpty()) {
                                    Term term = (Term) termResult.getEntityList().get(0);
                                    termID = term.getID();
                                    if (dueDate == null && transactionDate!=null) {
                                        Calendar cal = Calendar.getInstance();
                                        cal.setTime(transactionDate);
                                        cal.add(Calendar.DATE, term.getTermdays());
                                        String dueDateString = authHandler.getDateOnlyFormat().format(cal.getTime());
                                        dueDate = authHandler.getDateOnlyFormat().parse(dueDateString);
                                    }
                                } else {
                                    if (masterPreference.equalsIgnoreCase("0")) {// most restricted case
                                        failureMsg += "Debit Term entry not found in master list for Debit Term dropdown.";
                                    } else if (masterPreference.equalsIgnoreCase("1")) {// add empty value if data invalid for non mandatory
                                        failureMsg += "Debit Term entry not found in master list for Debit Term dropdown, cannot set empty data for Debit Term.";
                                    } else if (masterPreference.equalsIgnoreCase("2")) {//add new (most lanient)
                                        failureMsg += "Debit Term entry not present in Debit Term list, Please create new Debit Term entry for " + termName + " as it requires some other details.";
                                    }
                                }
                            } else {
                               failureMsg += messageSource.getMessage("acc.field.Termisnotavailable", null, RequestContextUtils.getLocale(request));
                            }
                        } else {
                            failureMsg += messageSource.getMessage("acc.field.Termisnotavailable", null, RequestContextUtils.getLocale(request));
                        }
                        
                        /*7. Invoice Currency */
                        String currencyId = "";
                        if (isCurrencyCode?columnConfig.containsKey("currencyCode"):columnConfig.containsKey("Currency")) {
                            String currencyStr = recarr[isCurrencyCode?(Integer) columnConfig.get("currencyCode"):(Integer) columnConfig.get("Currency")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(currencyStr)) {
                                failureMsg += "Empty data found in Amount, cannot set empty data for Amount.";
                            } else {
                                currencyId = accGoodsReceiptModuleService.getCurrencyId(currencyStr, currencyMap);
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

                        /*8. Amount*/
                        String transactionAmountStr = "";
                        double transactionAmount = 0d;
                        if(columnConfig.containsKey("Amount")){
                            transactionAmountStr = recarr[(Integer) columnConfig.get("Amount")].replaceAll("\"", "").trim();
                            if(StringUtil.isNullOrEmpty(transactionAmountStr)){
                              failureMsg += "Empty data found in Amount, cannot set empty data for Amount.";
                            } else {
                                try {
                                    transactionAmount = Double.parseDouble(transactionAmountStr);
                                    if (transactionAmount <= 0) {
                                        failureMsg += "Amount can not be zero or negative.";
                                    }
                                } catch (NumberFormatException ex) {
                                    failureMsg+="Incorrect numeric value for Amount, Please ensure that value type of Amount matches with the Amount.";
                                }
                            }
                        } else {
                            failureMsg +=messageSource.getMessage("acc.field.TransactionAmountisnotavailable", null, RequestContextUtils.getLocale(request));
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
                            Date applyDate = new Date();
                            String applyDateString = authHandler.getDateOnlyFormat().format(cal.getTime());
                            applyDate = authHandler.getDateOnlyFormat().parse(applyDateString);

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
                        
                        /*10. Party Invoice Number*/
                        String partyInvoiceNumber = "";
                        if (columnConfig.containsKey("PartyInvoiceNumber")) {
                            partyInvoiceNumber = recarr[(Integer) columnConfig.get("PartyInvoiceNumber")].replaceAll("\"", "").trim();
                            
                            JSONObject configObj = configMap.get("PartyInvoiceNumber");
                            int maxLength = configObj.optInt("maxLength", 0);
                            String validationType = configObj.optString("validatetype");
                            if ("string".equalsIgnoreCase(validationType) && !StringUtil.isNullOrEmpty(partyInvoiceNumber) && partyInvoiceNumber.length() > maxLength) {
                                if (masterPreference.equalsIgnoreCase("0")) {
                                    failureMsg += "Data length greater than " + maxLength + " for column Party Invoice Number.";
                                } else {// for other two cases need to trim data upto max length
                                    partyInvoiceNumber = partyInvoiceNumber.substring(0, maxLength);
                                }
                            }
                        }
                        
                        
                        /*11. Party Invoice Date*/
                        Date partyInvoiceDate = transactionDate;
                        if (columnConfig.containsKey("PartyInvoiceDate")) {
                            String partyInvoiceStr = recarr[(Integer) columnConfig.get("PartyInvoiceDate")].replaceAll("\"", "").trim();
                            partyInvoiceStr = partyInvoiceStr.replaceAll("\"", "");
                            if (!StringUtil.isNullOrEmpty(partyInvoiceStr)) {
                                try{
                                    partyInvoiceDate = df.parse(partyInvoiceStr);
                                    partyInvoiceDate = CompanyPreferencesCMN.removeTimefromDate(partyInvoiceDate);
                                    CompanyPreferencesCMN.checkLockPeriod(accCompanyPreferencesObj, requestParams, partyInvoiceDate, true);
                                } catch (NumberFormatException ex) {
                                    failureMsg += "Incorrect date format for Purchase Order Date, Please specify values in " + dateFormat + " format.";
                                } catch (Exception ex) {
                                    failureMsg += ex.getMessage();
                                }
                            }
                        }
                        
                        /*12.Sales Person */  
                        String masterAgenID = "";
                        if (columnConfig.containsKey("MasterAgent")) {
                            String masterAgentName = recarr[(Integer) columnConfig.get("MasterAgent")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(masterAgentName)) {
                                String masterGroupID =  String.valueOf(20);// 20 is group ID for Agent
                                KwlReturnObject retObj = importDao.getMasterItem(companyid, masterAgentName, masterGroupID);
                                if (retObj != null && !retObj.getEntityList().isEmpty() && retObj.getEntityList().get(0) != null) {
                                    masterAgenID = retObj.getEntityList().get(0).toString();
                                }
                                if (StringUtil.isNullOrEmpty(masterAgenID)) {
                                    if (masterPreference.equalsIgnoreCase("0")) {// most restricted case
                                        failureMsg += "Sales Person entry not found in master list for Sales Person dropdown.";
                                    } else if (masterPreference.equalsIgnoreCase("1")) {// add empty value if data invalid for non mandatory
                                        masterAgenID="";
                                    } else if (masterPreference.equalsIgnoreCase("2")) {//add new (most lanient)
                                        failureMsg += "Sales Person entry not present in Sales Person list, Please create new Sales Person entry for "+masterAgentName+" as it requires some other details.";
                                    }
                                }
                            } 
                        } 
                        
                        /*12. Memo*/
                        String memo="";
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
                                requestParamsCF.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Vendor_Invoice_ModuleId, headArrayList.get(K)));
                                KwlReturnObject fieldParamsResult = accCommonTablesDAO.getFieldParams(requestParamsCF); // get custom field for module
                                FieldParams params = null;
                                if (fieldParamsResult.getRecordTotalCount() > 0) {
                                    params = (FieldParams) fieldParamsResult.getEntityList().get(0);
                                    customFieldParamMap.put(headArrayList.get(K).toString(), params);
                                }
                            }
                        }

                        // creating goods receipt hash map

                        Map<String, Object> greceipthm = new HashMap<String, Object>();


                        // data processing for invoice auto number
                        greceipthm.put(ENTRYNUMBER, invoiceNumber);
                        greceipthm.put(AUTOGENERATED, false);

                        greceipthm.put(MEMO, memo);
                        greceipthm.put(BILLTO, "");
                        greceipthm.put(SHIPADDRESS, "");

                        greceipthm.put(DUEDATE, dueDate);
                        greceipthm.put(CURRENCYID, currencyId);
                        greceipthm.put(COMPANYID, companyid);
                        greceipthm.put("externalCurrencyRate", externalCurrencyRate);
                        greceipthm.put("salesPerson", masterAgenID);
                        greceipthm.put("venbilladdress", "");
                        greceipthm.put("venshipaddress", "");

                        greceipthm.put("partyInvoiceNumber", partyInvoiceNumber);
                        greceipthm.put("conversionRateFromCurrencyToBase", true);

                        KwlReturnObject ERresult = accCurrencyDAOobj.getExcDetailID(requestParams, currencyId, transactionDate, null);
                        ExchangeRateDetails erd = (ExchangeRateDetails) ERresult.getEntityList().get(0);
                        String erdid = (erd == null) ? null : erd.getID();

                        greceipthm.put(ERDID, erdid);

                        greceipthm.put(VENDORID, vendorId);
                        greceipthm.put(ACCOUNTID, accountId);
                        greceipthm.put("exchangeRateForOpeningTransaction", exchangeRateForOpeningTransaction);
                        greceipthm.put("creationDate", transactionDate);
                        greceipthm.put("originalOpeningBalanceAmount", transactionAmount);
                        greceipthm.put("openingBalanceAmountDue", transactionAmount);
                        greceipthm.put("termid", termID);
                        // Store invoice amount in base currency
                        greceipthm.put(Constants.originalOpeningBalanceBaseAmount, authHandler.round(transactionAmount * exchangeRateForOpeningTransaction, companyid));
                        greceipthm.put(Constants.openingBalanceBaseAmountDue, authHandler.round(transactionAmount * exchangeRateForOpeningTransaction, companyid));
                        greceipthm.put("partyInvoiceDate", partyInvoiceDate);
                        greceipthm.put("isOpeningBalenceInvoice", true);
                        greceipthm.put("isNormalInvoice", false);
//                         greceipthm.put(Constants.Checklocktransactiondate, transactionDate);//year lock is already done above so need to do here again. Also here null value is going which causing null pionter exception
                        resultObj = accGoodsReceiptobj.addGoodsReceipt(greceipthm);
                        gr = (GoodsReceipt) resultObj.getEntityList().get(0);
                        // For creating custom field array
                        JSONArray customJArr = fieldDataManagercntrl.getCustomFieldForOeningTransactionsRecords(headArrayList, customFieldParamMap, recarr,columnConfig, request);
                        customfield = customJArr.toString();
                        if (!StringUtil.isNullOrEmpty(customfield)) {
                            JSONArray jcustomarray = new JSONArray(customfield);
                            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                            customrequestParams.put("customarray", jcustomarray);
                            customrequestParams.put("modulename", Constants.Acc_OpeningBalanceVendorInvoice_modulename);
                            customrequestParams.put("moduleprimarykey", Constants.Acc_OpeningBalanceVendorInvoiceid);
                            customrequestParams.put("modulerecid", gr.getID());
                            customrequestParams.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);
                            customrequestParams.put("companyid", companyid);
                            customrequestParams.put("customdataclasspath", "com.krawler.hql.accounting.OpeningBalanceVendorInvoiceCustomData");
                            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                greceipthm.put(GRID, gr.getID());
                                greceipthm.put("openingBalanceVendorInvoiceCustomData", gr.getID());
                                KwlReturnObject result = accGoodsReceiptobj.updateGoodsReceipt(greceipthm);
                            }
                        }
                                
                    } catch (Exception ex) {
                        failed++;
                        String errorMsg = ex.getMessage(), invalidColumns = "";
                        try {
                            JSONObject errorLog = new JSONObject(errorMsg);
                            errorMsg = errorLog.getString("errorMsg");
                            invalidColumns = errorLog.getString("invalidColumns");
                        } catch (JSONException jex) {
                        }
                        failedRecords.append("\n" + accGoodsReceiptModuleService.createCSVrecord(recarr) + "\"" + errorMsg.replaceAll("\"", "") + "\"");
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
                msg = messageSource.getMessage("acc.field.Imported", null, RequestContextUtils.getLocale(request)) + " " + success + " " + messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (success > 1 ? "s" : "") + " " + messageSource.getMessage("acc.field.successfully", null, RequestContextUtils.getLocale(request));
                msg += (failed == 0 ? "." : messageSource.getMessage("acc.field.andfailedtoimport", null, RequestContextUtils.getLocale(request)) + " " + failed + " " + messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (failed > 1 ? "s" : "") + ".");
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

            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
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
                logDataMap.put("Module", Constants.Acc_Vendor_Invoice_ModuleId);
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userId);
                logDataMap.put("Company", companyid);
                importDao.saveImportLog(logDataMap);
                txnManager.commit(lstatus);
            } catch (Exception ex) {
                txnManager.rollback(lstatus);
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", getActualFileName(fileName));
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
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

    public Map<String, Object> getGoodsReceiptRequestMap(HttpServletRequest request, Map<String, Object> requestMap) throws SessionExpiredException, ServiceException, AccountingException, UnsupportedEncodingException {
        DateFormat df = authHandler.getDateOnlyFormat(request);
        String grid = request.getParameter(INVOICEID);
        KwlReturnObject result = null;
        GoodsReceiptOrder groObj = null;

        String companyid = sessionHandlerImpl.getCompanyid(request);
        boolean isAllowToEdit = (!StringUtil.isNullOrEmpty(request.getParameter(Constants.IS_INVOICE_ALLOW_TO_EDIT))) ? Boolean.parseBoolean(request.getParameter(Constants.IS_INVOICE_ALLOW_TO_EDIT)) : false;
        try {
            ExtraCompanyPreferences extraCompanyPreferences = null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
            requestMap.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestMap.put("df", df);
            requestMap.put("sequenceformat", request.getParameter("sequenceformat"));
            requestMap.put("grid", request.getParameter(INVOICEID));
            requestMap.put("posttext", request.getParameter("posttext"));
            requestMap.put("customfield", request.getParameter("customfield"));
            requestMap.put("shipLength", request.getParameter("shipLength"));
            requestMap.put("gstCurrencyRate", request.getParameter("gstCurrencyRate") != null ? Double.parseDouble(request.getParameter("gstCurrencyRate")) : 0.0);
            requestMap.put("invoicetype", request.getParameter("invoicetype"));
            requestMap.put("formtype", request.getParameter("formtypeid"));
            requestMap.put("vatcommodity", request.getParameter("vatcommodityid"));
            requestMap.put("RMCDApprovalNo", request.getParameter("RMCDApprovalNo"));
            requestMap.put("methodid", request.getParameter("pmtmethod") != null ? request.getParameter("pmtmethod") : "");
            requestMap.put("istemplate", request.getParameter("istemplate") != null ? Integer.parseInt(request.getParameter("istemplate")) : 0);
            requestMap.put("agent", request.getParameter("agent"));
            requestMap.put("goodsReceiptOrderid", request.getParameter("doid"));
            boolean isCapitalGoodsAcquired = false;
            String formtype = request.getParameter("formtypeid");
            requestMap.put("formtype", formtype);
            boolean rcmApplicable = request.getParameter("gtaapplicable") == null ? false : Boolean.parseBoolean(request.getParameter("gtaapplicable"));
            requestMap.put("gtaapplicable", rcmApplicable);
            requestMap.put("indiaExcise", request.getParameter("indiaExcise"));
            requestMap.put("transType", request.getParameter("transType"));
            
            
               
            if (!StringUtil.isNullOrEmpty(request.getParameter("isCapitalGoodsAcquired"))) {
                isCapitalGoodsAcquired = Boolean.parseBoolean(request.getParameter("isCapitalGoodsAcquired"));
                requestMap.put("isCapitalGoodsAcquired", isCapitalGoodsAcquired);
            }
            boolean isRetailPurchase = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("isRetailPurchase"))) {
                isRetailPurchase = Boolean.parseBoolean(request.getParameter("isRetailPurchase"));
                requestMap.put("isRetailPurchase", isRetailPurchase);
            }

            boolean importService = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("importService"))) {
                importService = Boolean.parseBoolean(request.getParameter("importService"));
                requestMap.put("isRetailPurchase", isRetailPurchase);
            }
            boolean isExciseInvoice = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("isExciseInvoice"))) {
                isExciseInvoice = Boolean.parseBoolean(request.getParameter("isExciseInvoice"));
                requestMap.put("isExciseInvoice", isExciseInvoice);
            }
            String defaultnatureofpurchase = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("defaultnatureofpurchase"))) {
                defaultnatureofpurchase = request.getParameter("defaultnatureofpurchase");
                requestMap.put("defaultnatureofpurchase", defaultnatureofpurchase);
            }
            String manufacturerType = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("manufacturerType"))) {
                manufacturerType = request.getParameter("manufacturerType");
                requestMap.put("manufacturerType", manufacturerType);
            }
            boolean iscash = StringUtil.isNullOrEmpty(request.getParameter("incash")) ? false : Boolean.parseBoolean(request.getParameter("incash"));
            boolean isEdit = StringUtil.isNullOrEmpty(request.getParameter("isEdit")) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
            boolean isCopy = StringUtil.isNullOrEmpty(request.getParameter("copyInv")) ? false : Boolean.parseBoolean(request.getParameter("copyInv"));
            boolean isLeaseFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isLeaseFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isLeaseFixedAsset")) : false;
            boolean selfBilledInvoice = (!StringUtil.isNullOrEmpty(request.getParameter("isselfbilledinvoice"))) ? Boolean.parseBoolean(request.getParameter("isselfbilledinvoice")) : false;
            boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false;
            boolean isConsignment = (!StringUtil.isNullOrEmpty(request.getParameter("isConsignment"))) ? Boolean.parseBoolean(request.getParameter("isConsignment")) : false;
            String currentUser = sessionHandlerImpl.getUserid(request);
            String deletedLinkedDocumentID = request.getParameter("deletedLinkedDocumentId");

            requestMap.put("iscash", iscash);
            requestMap.put("isEdit", isEdit);
            requestMap.put("isCopy", isCopy);
            requestMap.put("isLeaseFixedAsset", isLeaseFixedAsset);
            requestMap.put("selfBilledInvoice", selfBilledInvoice);
            requestMap.put("isFixedAsset", isFixedAsset);
            requestMap.put("isConsignment", isConsignment);
            requestMap.put("currentUser", currentUser);
            requestMap.put("deletedLinkedDocumentID", deletedLinkedDocumentID);
            
            String vendId = request.getParameter("vendor");
            boolean isDefaultAddress = request.getParameter("defaultAdress") != null ? Boolean.parseBoolean(request.getParameter("defaultAdress")) : false;
            Map<String, Object> addressParams = Collections.EMPTY_MAP;
            if (isDefaultAddress) { //defautladdress came true only when user create a new GRO without saving any address from address window.customer addresses taken default 
                if (extraCompanyPreferences.isIsAddressFromVendorMaster()) {
                    addressParams = AccountingAddressManager.getDefaultVendorAddressParams(vendId, companyid, accountingHandlerDAOobj);
                } else {
                    addressParams = AccountingAddressManager.getDefaultVendorCompanyAddressParams(vendId, companyid, accountingHandlerDAOobj);
                }
            } else {
                addressParams = AccountingAddressManager.getAddressParams(request, true);
            }

            requestMap.put("addressParams", addressParams);
            String createdby = sessionHandlerImpl.getUserid(request);
            String modifiedby = sessionHandlerImpl.getUserid(request);
            long createdon = System.currentTimeMillis();
            long updatedon = System.currentTimeMillis();
            requestMap.put("createdby", createdby);
            requestMap.put("modifiedby", modifiedby);
            requestMap.put("updatedon", updatedon);
            requestMap.put("createdon", createdon);
            String goodsReceiptOrderid = request.getParameter("doid");;
            boolean isExpenseInv = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("isExpenseInv"))) {
                isExpenseInv = Boolean.parseBoolean(request.getParameter("isExpenseInv"));
                requestMap.put("isExpenseInv", isExpenseInv);
            }
            requestMap.put("vendorId", request.getParameter("vendor"));
            requestMap.put("billingAddress", request.getParameter(Constants.BILLING_ADDRESS));
            result = accGoodsReceiptobj.getGRInventory(grid);
            //deleting Goods Receipt order row
            if (!StringUtil.isNullOrEmpty(goodsReceiptOrderid)) {
                KwlReturnObject GROObj = accountingHandlerDAOobj.getObject(GoodsReceiptOrder.class.getName(), goodsReceiptOrderid);
                groObj = (GoodsReceiptOrder) GROObj.getEntityList().get(0);
                request.setAttribute("DOSeqNum", groObj.getSeqnumber());
                request.setAttribute(Constants.DATEPREFIX, groObj.getDatePreffixValue());
                request.setAttribute(Constants.DATESUFFIX, groObj.getDateSuffixValue());
                HashMap<String, Object> DeliveryorderMap = new HashMap<String, Object>();
                DeliveryorderMap.put("doid", goodsReceiptOrderid);
                DeliveryorderMap.put("companyid", companyid);
                if (isAllowToEdit) {
                    accGoodsReceiptobj.deleteLinkingInformationOfGR(DeliveryorderMap);
                }
                accGoodsReceiptobj.deleteGoodsReceiptOrdersPermanent(DeliveryorderMap);
            }

            requestMap.put(INCASH, request.getParameter(INCASH));
            requestMap.put(Constants.Checklocktransactiondate, request.getParameter("billdate"));
            requestMap.put("costCenterId", request.getParameter(CCConstants.REQ_costcenter));
            requestMap.put("taxid", request.getParameter(TAXID));

            String costCenterId = request.getParameter(CCConstants.REQ_costcenter);
            String taxid = request.getParameter(TAXID);
            double taxamount = StringUtil.getDouble(request.getParameter(TAXAMOUNT));
            taxamount = authHandler.round(taxamount, companyid);
            requestMap.put("taxamount", taxamount);

            double externalCurrencyRate = StringUtil.getDouble(request.getParameter(EXTERNALCURRENCYRATE));
            requestMap.put("externalCurrencyRate", externalCurrencyRate);
            Discount discount = null;
            double discValue = 0.0;
//            double shippingCharges = StringUtil.getDouble(request.getParameter("shipping"));   //Removed from all the ransactions [PS]
//            double otherCharges = StringUtil.getDouble(request.getParameter("othercharges"));
            boolean inCash = Boolean.parseBoolean(request.getParameter(INCASH));
            requestMap.put("inCash", Boolean.parseBoolean(request.getParameter(INCASH)));
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);

            KwlReturnObject cmp = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmp.getEntityList().get(0);
            int countryid = company.getCountry() != null ? Integer.parseInt(company.getCountry().getID()) : 0;
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            //            KWLCurrency currency = (KWLCurrency) session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            currencyid = (request.getParameter(CURRENCYID) == null ? currency.getCurrencyID() : request.getParameter(CURRENCYID));
            requestMap.put("currencyid", currencyid);
            String entryNumber = request.getParameter(NUMBER);
            requestMap.put("entryNumber", request.getParameter(NUMBER));
            requestMap.put("vendorid", request.getParameter(VENDOR));

            String vendorid = request.getParameter(VENDOR);
            requestMap.put(MEMO, request.getParameter(MEMO));
            requestMap.put(BILLTO, request.getParameter(BILLTO));
            requestMap.put(SHIPADDRESS, request.getParameter(SHIPADDRESS));
//            greceipthm.put(SHIPDATE, df.parse(request.getParameter(SHIPDATE)));
            if (request.getParameter(SHIPDATE) != null && !StringUtil.isNullOrEmpty(request.getParameter(SHIPDATE))) {
                requestMap.put(SHIPDATE, df.parse(request.getParameter(SHIPDATE)));
            }
            requestMap.put(DUEDATE, df.parse(request.getParameter(DUEDATE)));
            requestMap.put(CURRENCYID, currencyid);
            requestMap.put(COMPANYID, companyid);
            requestMap.put("shipvia", request.getParameter("shipvia"));
            requestMap.put(termid, request.getParameter("termid"));
            requestMap.put("fob", request.getParameter("fob"));
            requestMap.put("externalCurrencyRate", externalCurrencyRate);
            requestMap.put("agent", request.getParameter("agent"));
            requestMap.put("isfavourite", request.getParameter("isfavourite"));
            requestMap.put("landedInvoiceNumber", (request.getParameter("landedInvoiceNumber") == null) ? "" : request.getParameter("landedInvoiceNumber"));
            requestMap.put("venbilladdress", request.getParameter("venbilladdress") == null ? "" : request.getParameter("venbilladdress"));
            requestMap.put("venshipaddress", request.getParameter("venshipaddress") == null ? "" : request.getParameter("venshipaddress"));
            boolean gstIncluded = request.getParameter("includingGST") == null ? false : Boolean.parseBoolean(request.getParameter("includingGST"));
            requestMap.put("gstIncluded", gstIncluded);
            requestMap.put("selfBilledInvoice", selfBilledInvoice);
            String RMCDApprovalNo = request.getParameter("RMCDApprovalNo");
            requestMap.put("RMCDApprovalNo", RMCDApprovalNo);
            requestMap.put("paydetail", request.getParameter("paydetail"));

            requestMap.put("bankAccountId", request.getParameter("bankaccid"));
            requestMap.put("endDate", df.parse(request.getParameter("enddate")));
            requestMap.put("startDate", df.parse(request.getParameter("startDate")));
            requestMap.put("bankAccountId", 0.0);
            requestMap.put("endingAmount", request.getParameter("bankaccid"));
            requestMap.put("bankAccountId", request.getParameter("bankaccid"));


            Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
            requestMap.put("jeDataMap", jeDataMap);
            requestMap.put("DETAIL", request.getParameter(DETAIL));
            requestMap.put(EXPENSEDETAIL, request.getParameter(EXPENSEDETAIL));
            requestMap.put(DISCOUNT, request.getParameter(DISCOUNT));
            requestMap.put(INPERCENT, Boolean.parseBoolean(request.getParameter(PERDISCOUNT)));
            requestMap.put("InvoiceTerms", request.getParameter("invoicetermsmap"));
            requestMap.put(Constants.termsincludegst, Boolean.parseBoolean(request.getParameter(Constants.termsincludegst)));
            requestMap.put(Constants.termsincludegst, Boolean.parseBoolean(request.getParameter(Constants.termsincludegst)));
            HashMap<String, Object> requestParams = AccountingManager.getEmailNotificationParams(request);
            requestMap.put("requestParams", requestParams);
            HashMap<String, Object> globalParams = AccountingManager.getGlobalParams(request);
            requestMap.put("globalParams", globalParams);
            String InvoiceTerms = request.getParameter("invoicetermsmap");
            Locale locale = RequestContextUtils.getLocale(request);
            requestMap.put("locale", locale);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveGoodsReceipt : " + ex.getMessage(), ex);
        }
        return requestMap;
    }

      private void updateVQisOpenAndLinking(String linking) throws ServiceException {
        try {
            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(VendorQuotation.class.getName(), linking);
            VendorQuotation quotation = (VendorQuotation) rdresult.getEntityList().get(0);
            HashMap hMap = new HashMap();
            boolean isopen = false;
            Set<VendorQuotationDetail> rows = quotation.getRows();
            Iterator itrVQD = rows.iterator();
            while (itrVQD.hasNext()) {
                VendorQuotationDetail row = (VendorQuotationDetail) itrVQD.next();
                KwlReturnObject idresult = accPurchaseOrderobj.getGRDFromVQD(row.getID());
                List list = idresult.getEntityList();
                Iterator iteGRD = list.iterator();
                double qua = 0.0;
                while (iteGRD.hasNext()) {
                    GoodsReceiptDetail grd = (GoodsReceiptDetail) iteGRD.next();
                    qua += grd.getInventory().getQuantity();
                }
                double addobj = row.getQuantity() - qua;
                if (addobj > 0) {
                    isopen = true;
                    break;
                }
            }
            hMap.put("isOpen", isopen);
            hMap.put("quotation", quotation);
            hMap.put("value", "1");
            accGoodsReceiptobj.updateVQLinkflag(hMap);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("updateVQisOpenAndLinking : " + ex.getMessage(), ex);
        }

    }

    private void updatePOisOpenAndLinkingWithVI(String linking) throws ServiceException {
        try {
            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), linking);
            PurchaseOrder purchaseOrder = (PurchaseOrder) rdresult.getEntityList().get(0);
            HashMap hMap = new HashMap();

            boolean isopen = false;
            if (!purchaseOrder.isIsExpenseType()) {
                Set<PurchaseOrderDetail> rows = purchaseOrder.getRows();
                Iterator itrPOD = rows.iterator();
                while (itrPOD.hasNext()) {
                    PurchaseOrderDetail row = (PurchaseOrderDetail) itrPOD.next();
                    KwlReturnObject idresult = accGoodsReceiptobj.getReceiptDFromPOD(row.getID());
                    List list = idresult.getEntityList();
                    Iterator iteGRD = list.iterator();
                    double qua = 0.0;
                    while (iteGRD.hasNext()) {
                        GoodsReceiptDetail grd = (GoodsReceiptDetail) iteGRD.next();
                        qua += grd.getInventory().getQuantity();
                    }
                    double addobj = row.getQuantity() - qua;
                    if (addobj > 0) {
                        isopen = true;
                        break;
                    }
                }
            } else if (purchaseOrder.isIsExpenseType()) {
                /**
                 * to Update Status of 'Expese PO' according to
                 * 'Balance Amount '.
                 */
                Set<ExpensePODetail> rows = purchaseOrder.getExpenserows();
                Map<Object,Object> params = new HashMap<>();
                for (ExpensePODetail expensePODetail : rows) {
                    params.put("expensePODetail", expensePODetail.getID());
                    params.put("companyid", expensePODetail.getCompany().getCompanyID());
                    KwlReturnObject idresult = accGoodsReceiptobj.getExpenseGRDetailFromPOD(params);
                    List<ExpenseGRDetail> list = idresult.getEntityList();
                    double amount = 0.0;
                    for (ExpenseGRDetail expenseGRDetail : list) {
                        amount += expenseGRDetail.getAmount();
                    }
                    double balAmount = expensePODetail.getAmount() - amount;
                    HashMap poMap = new HashMap();
                    poMap.put("expPODetailsID", expensePODetail.getID());
                    poMap.put("companyid", expensePODetail.getCompany().getCompanyID());
                    poMap.put("balAmount", balAmount);
                    poMap.put("update", true);
                    accCommonTablesDAO.updateExpensePurchaseOrderStatus(poMap);
                    if (balAmount > 0) {
                        isopen = true;                    
                    }
                }
            }
            hMap.put("isOpen", isopen);
            hMap.put("purchaseOrder", purchaseOrder);
            hMap.put("value", "1");
            accGoodsReceiptobj.updatePOLinkflag(hMap);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("updatePOisOpenAndLinkingWithVI : " + ex.getMessage(), ex);
        }

    }
      private void updateGRisOpenAndLinkingWithVI(String linking) throws ServiceException {
        try {

            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(GoodsReceiptOrder.class.getName(), linking);
            GoodsReceiptOrder goodsReceiptOrder = (GoodsReceiptOrder) rdresult.getEntityList().get(0);
            Set<GoodsReceiptOrderDetails> orderDetail = (Set<GoodsReceiptOrderDetails>) goodsReceiptOrder.getRows();
            String status1 = getGoodsReceiptOrderStatus(orderDetail);
            HashMap hMap = new HashMap();
            hMap.put("goodsReceiptOrder", goodsReceiptOrder);
            if (status1.equals("Open")) {
                hMap.put("isOpenInPI", true);
            } else {
                hMap.put("isOpenInPI", false);
            }
            accGoodsReceiptobj.updateGRLinkflag(hMap);

        } catch (Exception ex) {
            throw ServiceException.FAILURE("updatePOisOpenAndLinkingWithVI : " + ex.getMessage(), ex);
        }

    }
     

    
     public ModelAndView updateVQScript(HttpServletRequest request, HttpServletResponse response) {
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
            rCompanyId = accPurchaseOrderobj.getAllCompanyFromDb(subdomainArray);
            Iterator itrCompanyId = rCompanyId.getEntityList().iterator();
            while (itrCompanyId.hasNext()) {
                String companyid = (String) itrCompanyId.next();
                requestParams.put("companyid", companyid);
                requestParams.put("linkFlag", "1");
                KwlReturnObject result = null;
                result = accPurchaseOrderobj.getQuotationsForScript(requestParams);
                Iterator itr = result.getEntityList().iterator();
                while (itr.hasNext()) {
                    String linkNumbers = (String) itr.next();
                    if (!StringUtil.isNullOrEmpty(linkNumbers)) {
                        updateVQisOpenAndLinking(linkNumbers);
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
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", "Script completed update the isOpen Flag in VQ");
//                jobj.put("pendingapproval", pendingapproval);

            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView updateGRScript(HttpServletRequest request, HttpServletResponse response) {
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
            rCompanyId = accPurchaseOrderobj.getAllCompanyFromDb(subdomainArray);
            Iterator itrCompanyId = rCompanyId.getEntityList().iterator();
            TransactionStatus status = null;
            while (itrCompanyId.hasNext()) {
                String companyid = (String) itrCompanyId.next();
                try {
                    status = txnManager.getTransaction(def);
                    requestParams.put("companyid", companyid);
                    requestParams.put("linkFlag", "1");
                    KwlReturnObject result = null;
                    result = accPurchaseOrderobj.getGRForScript(requestParams);
                    Iterator itr = result.getEntityList().iterator();
                    while (itr.hasNext()) {
                        String linkNumbers = (String) itr.next();
                        if (!StringUtil.isNullOrEmpty(linkNumbers)) {
                            updateGRisOpenAndLinkingWithVI(linkNumbers);
                        }
                    }
                    msg += (companyid + "-True<br/>");
                    txnManager.commit(status);
                } catch (Exception ex) {
                    msg += (companyid + "-False<br/>");
                    if (status != null) {
                        txnManager.rollback(status);
                    }
                    Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            issuccess = true;

        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView updateOpenStatusFlagOfPOInGRScript(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Invoice_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONObject jobj = new JSONObject();
        //String companyid = sessionHandlerImpl.getCompanyid(request);
        boolean issuccess = false;

        try {
            String groorderId="";
            KwlReturnObject company = accGoodsReceiptobj.getCompanyList();
            Iterator ctr = company.getEntityList().iterator();
            while (ctr.hasNext()) {
                String companyid = ctr.next().toString();
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("linkflag", "2");
                requestParams.put("companyid", companyid);
                KwlReturnObject result = accGoodsReceiptobj.getPurchaseorder(requestParams);
                Iterator itr = result.getEntityList().iterator();
                while (itr.hasNext()) {
                    String linkNumbers = (String) itr.next();
                    if (!StringUtil.isNullOrEmpty(linkNumbers)) {
                        accGoodsReceiptModuleService.updatePOisOpenAndLinkingWithGR(linkNumbers,groorderId);
                    }

                    issuccess = true;
                }
            }
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
   public ModelAndView updateOpenStatusFlagOfPIInGRScript(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Invoice_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            TransactionStatus status = null;
            String subdomain = "";
            String[] subdomainArray = null;
            if (!StringUtil.isNullOrEmpty(request.getParameter("subdomain"))) {
                subdomain = request.getParameter("subdomain").toString().trim();
                subdomainArray = subdomain.split(",");
            }
            KwlReturnObject company = accPurchaseOrderobj.getAllCompanyFromDb(subdomainArray);
            Iterator ctr = company.getEntityList().iterator();
            while (ctr.hasNext()) {
                String companyid = ctr.next().toString();
                try {
                    status = txnManager.getTransaction(def);
                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("companyid", companyid);
                    KwlReturnObject result = accGoodsReceiptobj.getGoodsrecipt(requestParams);
                    if (result.getRecordTotalCount() > 0) {
                        Iterator itr = result.getEntityList().iterator();
                        while (itr.hasNext()) {
                            String linkNumbers = (String) itr.next();
                            if (!StringUtil.isNullOrEmpty(linkNumbers)) {
                                accGoodsReceiptModuleService.updatePIisOpenAndLinkingWithGR(linkNumbers);
                            }
                        }
                    }
                    txnManager.commit(status);
                    msg += (companyid + "-TRUE<br/>");
                } catch (Exception ex) {
                    msg += (companyid + "-False<br/>");
                    if (status != null) {
                        txnManager.rollback(status);
                    }
                    Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            issuccess = true;
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("success", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }


    public ModelAndView updateOpenStatusFlagOfPOInVIScript(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Invoice_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        try {

            KwlReturnObject company = accGoodsReceiptobj.getCompanyList();
            Iterator ctr = company.getEntityList().iterator();
            while (ctr.hasNext()) {
                String companyid = ctr.next().toString();
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("linkflag", "1");
                requestParams.put("companyid", companyid);
                KwlReturnObject result = accGoodsReceiptobj.getPurchaseorder(requestParams);
                Iterator itr = result.getEntityList().iterator();
                while (itr.hasNext()) {
                    String linkNumbers = (String) itr.next();
                    if (!StringUtil.isNullOrEmpty(linkNumbers)) {
                        updatePOisOpenAndLinkingWithVI(linkNumbers);
                    }

                    issuccess = true;
                }
            }
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView updateLinkingPOScript(HttpServletRequest request, HttpServletResponse response) {
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
            rCompanyId = accPurchaseOrderobj.getAllCompanyFromDb(subdomainArray);
            Iterator itrCompanyId = rCompanyId.getEntityList().iterator();
            boolean isgoodsreceipt = false;
            while (itrCompanyId.hasNext()) {
                String companyid = (String) itrCompanyId.next();
                requestParams.put("companyid", companyid);
                KwlReturnObject result = null;
                result = accPurchaseOrderobj.getLinkedVendorInvoiceWithPO(requestParams);
                Iterator itr = result.getEntityList().iterator();
                while (itr.hasNext()) {
                    String invoiceid = (String) itr.next();
                    if (!StringUtil.isNullOrEmpty(invoiceid)) {
                        isgoodsreceipt = true;
                        updateLinkingInformationOfPO(invoiceid, companyid, isgoodsreceipt);
                    }
                }
                isgoodsreceipt = false;
                result = accPurchaseOrderobj.getLinkedGoodsReceiptWithPO(requestParams);
                Iterator itr1 = result.getEntityList().iterator();
                while (itr1.hasNext()) {
                    String goodsreceiptid = (String) itr1.next();
                    if (!StringUtil.isNullOrEmpty(goodsreceiptid)) {
                        updateLinkingInformationOfPO(goodsreceiptid, companyid, isgoodsreceipt);
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
                jobj.put("success", issuccess);
                jobj.put("msg", "Script completed for Updating Linking Information for PO linking");
//                jobj.put("pendingapproval", pendingapproval);

            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    private void updateLinkingInformationOfPO(String linkNumbers, String companyid, boolean isgoodsreceipt) throws ServiceException {
        List list = null;
        try {
            if (isgoodsreceipt) {
                KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(GoodsReceiptDetail.class.getName(), linkNumbers);
                GoodsReceiptDetail invdetail = (GoodsReceiptDetail) rdresult.getEntityList().get(0);

                String invoiceno = invdetail.getGoodsReceipt().getGoodsReceiptNumber();
                String invoiceid = invdetail.getGoodsReceipt().getID();
                String pono = invdetail.getPurchaseorderdetail().getPurchaseOrder().getPurchaseOrderNumber();
                String poid = invdetail.getPurchaseorderdetail().getPurchaseOrder().getID();

                KwlReturnObject result = accGoodsReceiptobj.checkEntryForGoodsReceiptInLinkingTable(invoiceid, poid);
                list = result.getEntityList();
                if (list == null || list.isEmpty()) {

                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("sourceflag", 0);
                    requestParams.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);
                    requestParams.put("linkeddocno", invoiceno);
                    requestParams.put("docid", poid);
                    requestParams.put("linkeddocid", invoiceid);
                    result = accPurchaseOrderobj.savePOLinking(requestParams);


                    requestParams.put("sourceflag", 1);
                    requestParams.put("moduleid", Constants.Acc_Purchase_Order_ModuleId);
                    requestParams.put("linkeddocno", pono);
                    requestParams.put("docid", invoiceid);
                    requestParams.put("linkeddocid", poid);
                    result = accGoodsReceiptobj.saveVILinking(requestParams);
                }
            } else {
                KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(GoodsReceiptOrderDetails.class.getName(), linkNumbers);
                GoodsReceiptOrderDetails grodetail = (GoodsReceiptOrderDetails) rdresult.getEntityList().get(0);


                String grno = grodetail.getGrOrder().getGoodsReceiptOrderNumber();
                String grid = grodetail.getGrOrder().getID();
                String pono = grodetail.getPodetails().getPurchaseOrder().getPurchaseOrderNumber();
                String poid = grodetail.getPodetails().getPurchaseOrder().getID();

                KwlReturnObject result = accGoodsReceiptobj.checkEntryForGoodsReceiptOrderInLinkingTable(grid, poid);
                list = result.getEntityList();

                if (list == null || list.isEmpty()) {


                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("sourceflag", 0);
                    requestParams.put("moduleid", Constants.Acc_Goods_Receipt_ModuleId);
                    requestParams.put("linkeddocno", grno);
                    requestParams.put("docid", poid);
                    requestParams.put("linkeddocid", grid);
                    result = accPurchaseOrderobj.savePOLinking(requestParams);


                    requestParams.put("sourceflag", 1);
                    requestParams.put("moduleid", Constants.Acc_Purchase_Order_ModuleId);
                    requestParams.put("linkeddocno", pono);
                    requestParams.put("docid", grid);
                    requestParams.put("linkeddocid", poid);
                    result = accGoodsReceiptobj.saveGRLinking(requestParams);

                }
            }


        } catch (Exception ex) {
            throw ServiceException.FAILURE("updateLinkingInformationOfPO : " + ex.getMessage(), ex);
        }

    }

    public ModelAndView updateLinkingPIScript(HttpServletRequest request, HttpServletResponse response) {
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
            rCompanyId = accPurchaseOrderobj.getAllCompanyFromDb(subdomainArray);
            Iterator itrCompanyId = rCompanyId.getEntityList().iterator();

            while (itrCompanyId.hasNext()) {
                String companyid = (String) itrCompanyId.next();
                requestParams.put("companyid", companyid);

                KwlReturnObject result = null;
                result = accPurchaseOrderobj.getLinkedGoodsReceiptWithPI(requestParams);
                Iterator itr = result.getEntityList().iterator();
                while (itr.hasNext()) {
                    String gordetailid = (String) itr.next();
                    if (!StringUtil.isNullOrEmpty(gordetailid)) {

                        updateLinkingInformationOfPILinkedWithGR(gordetailid, companyid);
                    }
                }
                result = accPurchaseOrderobj.getLinkedPurchaseReturnWithPI(requestParams);
                Iterator itr1 = result.getEntityList().iterator();

                while (itr1.hasNext()) {
                    String prdetailid = (String) itr1.next();
                    if (!StringUtil.isNullOrEmpty(prdetailid)) {

                        updateLinkingInformationOfPILinkedWithPR(prdetailid, companyid);
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
                jobj.put("success", issuccess);
                jobj.put("msg", "Script completed for Updating Linking Information for PI linking");
//                jobj.put("pendingapproval", pendingapproval);

            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    private void updateLinkingInformationOfPILinkedWithGR(String grodetailid, String companyid) throws ServiceException {
        try {

            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(GoodsReceiptOrderDetails.class.getName(), grodetailid);
            GoodsReceiptOrderDetails grodetails = (GoodsReceiptOrderDetails) rdresult.getEntityList().get(0);

            String goodsreceiptno = grodetails.getVidetails().getGoodsReceipt().getGoodsReceiptNumber();
            String linkedDocid = grodetails.getGrOrder().getID();
            String linkedDocno = grodetails.getGrOrder().getGoodsReceiptOrderNumber();
            String linkNumbers = grodetails.getVidetails().getGoodsReceipt().getID();

            KwlReturnObject result = accGoodsReceiptobj.checkEntryForGoodsReceiptOrderInLinkingTable(linkedDocid, linkNumbers);
            List list = result.getEntityList();
            if (list == null || list.isEmpty()) {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("sourceflag", 0);
                requestParams.put("moduleid", Constants.Acc_Goods_Receipt_ModuleId);

                requestParams.put("linkeddocno", linkedDocno);
                requestParams.put("docid", linkNumbers);
                requestParams.put("linkeddocid", linkedDocid);
                result = accGoodsReceiptobj.saveVILinking(requestParams);


                requestParams.put("sourceflag", 1);
                requestParams.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);
                requestParams.put("linkeddocno", goodsreceiptno);
                requestParams.put("docid", linkedDocid);
                requestParams.put("linkeddocid", linkNumbers);
                result = accGoodsReceiptobj.saveGRLinking(requestParams);
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("updateLinkingInformationOfPILinkedWithGR : " + ex.getMessage(), ex);
        }

    }

    private void updateLinkingInformationOfPILinkedWithPR(String prdetailid, String companyid) throws ServiceException {
        try {

            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(PurchaseReturnDetail.class.getName(), prdetailid);
            PurchaseReturnDetail prdetails = (PurchaseReturnDetail) rdresult.getEntityList().get(0);

            String invoiceno = prdetails.getVidetails().getGoodsReceipt().getGoodsReceiptNumber();
            String invoiceid = prdetails.getVidetails().getGoodsReceipt().getID();
            String prid = prdetails.getPurchaseReturn().getID();
            String prno = prdetails.getPurchaseReturn().getPurchaseReturnNumber();


            KwlReturnObject result = accGoodsReceiptobj.checkEntryForPurchaseReturnInLinkingTable(prid, invoiceid);
            List list = result.getEntityList();
            if (list == null || list.isEmpty()) {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("sourceflag", 0);
                requestParams.put("moduleid", Constants.Acc_Purchase_Return_ModuleId);

                requestParams.put("linkeddocno", prno);
                requestParams.put("docid", invoiceid);
                requestParams.put("linkeddocid", prid);
                result = accGoodsReceiptobj.saveVILinking(requestParams);


                requestParams.put("sourceflag", 1);
                requestParams.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);
                requestParams.put("linkeddocno", invoiceno);
                requestParams.put("docid", prid);
                requestParams.put("linkeddocid", invoiceid);
                result = accGoodsReceiptobj.savePRLinking(requestParams);
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("updateLinkingInformationOfPILinkedWithPR : " + ex.getMessage(), ex);
        }

    }

    public ModelAndView updateLinkingGRScript(HttpServletRequest request, HttpServletResponse response) {
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
            rCompanyId = accPurchaseOrderobj.getAllCompanyFromDb(subdomainArray);
            Iterator itrCompanyId = rCompanyId.getEntityList().iterator();

            while (itrCompanyId.hasNext()) {
                String companyid = (String) itrCompanyId.next();
                requestParams.put("companyid", companyid);

                KwlReturnObject result = null;
                result = accPurchaseOrderobj.getLinkedVendorInvoiceWithGR(requestParams);
                Iterator itr = result.getEntityList().iterator();
                while (itr.hasNext()) {
                    String grdetailid = (String) itr.next();
                    if (!StringUtil.isNullOrEmpty(grdetailid)) {

                        updateLinkingInformationOfGRLinkedWithPI(grdetailid, companyid);
                    }
                }
                result = accPurchaseOrderobj.getLinkedPurchaseReturnWithGR(requestParams);
                Iterator itr1 = result.getEntityList().iterator();

                while (itr1.hasNext()) {
                    String prdetailid = (String) itr1.next();
                    if (!StringUtil.isNullOrEmpty(prdetailid)) {

                        updateLinkingInformationOfGRLinkedWithPR(prdetailid, companyid);
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
                jobj.put("success", issuccess);
                jobj.put("msg", "Script completed for updating Linking Information for GR linking");
//                jobj.put("pendingapproval", pendingapproval);

            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    private void updateLinkingInformationOfGRLinkedWithPI(String grdetailid, String companyid) throws ServiceException {
        try {

            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(GoodsReceiptDetail.class.getName(), grdetailid);
            GoodsReceiptDetail grdetails = (GoodsReceiptDetail) rdresult.getEntityList().get(0);

            String goodsreceiptno = grdetails.getGoodsReceiptOrderDetails().getGrOrder().getGoodsReceiptOrderNumber();
            String goodsreceiptid = grdetails.getGoodsReceiptOrderDetails().getGrOrder().getID();
            String invoiceid = grdetails.getGoodsReceipt().getID();
            String invoiceno = grdetails.getGoodsReceipt().getGoodsReceiptNumber();

            KwlReturnObject result = accGoodsReceiptobj.checkEntryForGoodsReceiptInLinkingTable(invoiceid, goodsreceiptid);
            List list = result.getEntityList();
            if (list == null || list.isEmpty()) {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("sourceflag", 0);
                requestParams.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);

                requestParams.put("linkeddocno", invoiceno);
                requestParams.put("docid", goodsreceiptid);
                requestParams.put("linkeddocid", invoiceid);
                result = accGoodsReceiptobj.saveGRLinking(requestParams);



                requestParams.put("sourceflag", 1);
                requestParams.put("moduleid", Constants.Acc_Goods_Receipt_ModuleId);
                requestParams.put("linkeddocno", goodsreceiptno);
                requestParams.put("docid", invoiceid);
                requestParams.put("linkeddocid", goodsreceiptid);
                result = accGoodsReceiptobj.saveVILinking(requestParams);
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("updateLinkingInformationOfGRLinkedWithPI : " + ex.getMessage(), ex);
        }

    }

    private void updateLinkingInformationOfGRLinkedWithPR(String prdetailid, String companyid) throws ServiceException {
        try {

            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(PurchaseReturnDetail.class.getName(), prdetailid);
            PurchaseReturnDetail prdetails = (PurchaseReturnDetail) rdresult.getEntityList().get(0);

            String goodsreceiptno = prdetails.getGrdetails().getGrOrder().getGoodsReceiptOrderNumber();
            String goodsreceiptid = prdetails.getGrdetails().getGrOrder().getID();
            String prno = prdetails.getPurchaseReturn().getPurchaseReturnNumber();
            String prid = prdetails.getPurchaseReturn().getID();

            KwlReturnObject result = accGoodsReceiptobj.checkEntryForPurchaseReturnInLinkingTable(prid, goodsreceiptid);
            List list = result.getEntityList();
            if (list == null || list.isEmpty()) {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("sourceflag", 0);
                requestParams.put("moduleid", Constants.Acc_Purchase_Return_ModuleId);

                requestParams.put("linkeddocno", prno);
                requestParams.put("docid", goodsreceiptid);
                requestParams.put("linkeddocid", prid);
                result = accGoodsReceiptobj.saveGRLinking(requestParams);



                requestParams.put("sourceflag", 1);
                requestParams.put("moduleid", Constants.Acc_Goods_Receipt_ModuleId);
                requestParams.put("linkeddocno", goodsreceiptno);
                requestParams.put("docid", prid);
                requestParams.put("linkeddocid", goodsreceiptid);
                result = accGoodsReceiptobj.savePRLinking(requestParams);
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("updateLinkingInformationOfGRLinkedWithPR : " + ex.getMessage(), ex);
        }

    }
    
   public ModelAndView updateLinkingVQScript(HttpServletRequest request, HttpServletResponse response) {
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
            rCompanyId = accPurchaseOrderobj.getAllCompanyFromDb(subdomainArray);
            Iterator itrCompanyId = rCompanyId.getEntityList().iterator();

            while (itrCompanyId.hasNext()) {
                String companyid = (String) itrCompanyId.next();
                requestParams.put("companyid", companyid);

                KwlReturnObject result = null;
                result = accPurchaseOrderobj.getLinkedVendorInvoiceWithVQ(requestParams);
                Iterator itr = result.getEntityList().iterator();
                while (itr.hasNext()) {
                    String grdetailid = (String) itr.next();
                    if (!StringUtil.isNullOrEmpty(grdetailid)) {

                        updateLinkingInformationOfVQLinkedWithPI(grdetailid, companyid);
                    }
                }
                result = accPurchaseOrderobj.getLinkedPurchaseOrderWithVQ(requestParams);
                Iterator itr1 = result.getEntityList().iterator();

                while (itr1.hasNext()) {
                    String podetailid = (String) itr1.next();
                    if (!StringUtil.isNullOrEmpty(podetailid)) {

                        updateLinkingInformationOfVQLinkedWithPO(podetailid, companyid);
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
                jobj.put("success", issuccess);
                jobj.put("msg", "Script completed for Updating Linking Information for VQ linking");
//                jobj.put("pendingapproval", pendingapproval);

            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    } 

   
    private void updateLinkingInformationOfVQLinkedWithPI(String grdetailid, String companyid) throws ServiceException {
        try {

            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(GoodsReceiptDetail.class.getName(), grdetailid);
            GoodsReceiptDetail grdetails = (GoodsReceiptDetail) rdresult.getEntityList().get(0);

            String goodsreceiptno = grdetails.getGoodsReceipt().getGoodsReceiptNumber();
            String goodsreceiptid = grdetails.getGoodsReceipt().getID();
            String vqno = grdetails.getVendorQuotationDetail().getVendorquotation().getQuotationNumber();
            String vqid = grdetails.getVendorQuotationDetail().getVendorquotation().getID();

            KwlReturnObject result = accGoodsReceiptobj.checkEntryForGoodsReceiptInLinkingTable(goodsreceiptid, vqid);
            List list = result.getEntityList();
            if (list == null || list.isEmpty()) {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("sourceflag", 0);
                requestParams.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);

                requestParams.put("linkeddocno", goodsreceiptno);
                requestParams.put("docid", vqid);
                requestParams.put("linkeddocid", goodsreceiptid);
                result = accPurchaseOrderobj.saveVQLinking(requestParams);


                requestParams.put("sourceflag", 1);
                requestParams.put("moduleid", Constants.Acc_Vendor_Quotation_ModuleId);
                requestParams.put("linkeddocno", vqno);
                requestParams.put("docid", goodsreceiptid);
                requestParams.put("linkeddocid", vqid);
                result = accGoodsReceiptobj.saveVILinking(requestParams);
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("updateLinkingInformationOfVQLinkedWithPI : " + ex.getMessage(), ex);
        }

    }

    private void updateLinkingInformationOfVQLinkedWithPO(String podetailid, String companyid) throws ServiceException {
        try {

            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(PurchaseOrderDetail.class.getName(), podetailid);
            PurchaseOrderDetail podetails = (PurchaseOrderDetail) rdresult.getEntityList().get(0);

            String pono = podetails.getPurchaseOrder().getPurchaseOrderNumber();
            String poid = podetails.getPurchaseOrder().getID();
            String vqno = podetails.getVqdetail().getVendorquotation().getQuotationNumber();
            String vqid = podetails.getVqdetail().getVendorquotation().getID();

            KwlReturnObject result = accGoodsReceiptobj.checkEntryForPurchaseOrderInLinkingTable(poid, vqid);
            List list = result.getEntityList();
            if (list == null || list.isEmpty()) {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("sourceflag", 0);
                requestParams.put("moduleid", Constants.Acc_Purchase_Order_ModuleId);

                requestParams.put("linkeddocno", pono);
                requestParams.put("docid", vqid);
                requestParams.put("linkeddocid", poid);
                result = accPurchaseOrderobj.saveVQLinking(requestParams);


                requestParams.put("sourceflag", 1);
                requestParams.put("moduleid", Constants.Acc_Vendor_Quotation_ModuleId);
                requestParams.put("linkeddocno", vqno);
                requestParams.put("docid", poid);
                requestParams.put("linkeddocid", vqid);
                result = accPurchaseOrderobj.savePOLinking(requestParams);
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("updateLinkingInformationOfVQLinkedWithPO : " + ex.getMessage(), ex);
        }

    }

    private void deleteAssetDetails(GoodsReceipt gr, String companyId) throws ServiceException {
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("greceiptid", gr.getID());
        requestParams.put("companyid", companyId);
        accGoodsReceiptobj.deleteAssetDetailsLinkedWithGR(requestParams);


//        Set<GoodsReceiptDetail> receiptDetailsSet = gr.getRows();
//        for (GoodsReceiptDetail detail : receiptDetailsSet) {
//            // get AssetInvoiceDetailMapping for goodsReceiptDetail
//
//            HashMap<String, Object> assetDetailsParams = new HashMap<String, Object>();
//            assetDetailsParams.put("companyid", companyId);
//            assetDetailsParams.put("invoiceDetailId", detail.getID());
//            assetDetailsParams.put("moduleId", Constants.Acc_Vendor_Invoice_ModuleId);
//            KwlReturnObject assetInvMapObj = accProductObj.getAssetInvoiceDetailMapping(assetDetailsParams);
//            List assetInvMapList = assetInvMapObj.getEntityList();
//            Iterator assetInvMapListIt = assetInvMapList.iterator();
//            while (assetInvMapListIt.hasNext()) {
//                AssetInvoiceDetailMapping invoiceDetailMapping = (AssetInvoiceDetailMapping) assetInvMapListIt.next();
//                AssetDetails assetDetails = invoiceDetailMapping.getAssetDetails();
//
//                HashMap<String, Object> assetDetailsDeleteParams = new HashMap<String, Object>();
//                assetDetailsDeleteParams.put("companyId", companyId);
//                assetDetailsDeleteParams.put("assetDetailId", assetDetails.getId());
//
//                accProductObj.deleteAssetDetails(assetDetailsDeleteParams);
//
//            }
//            // delete AssetInvoiceDetailMapping
//
//            accProductObj.deleteAssetInvoiceDetailMapping(assetDetailsParams);
//        }

    }

    public List mapInvoiceTerms(String InvoiceTerms, String ID, String userid, boolean isGR) throws ServiceException {
        List ll = new ArrayList();
        try {
            JSONArray termsArr = new JSONArray(InvoiceTerms);
            for (int cnt = 0; cnt < termsArr.length(); cnt++) {
                JSONObject temp = termsArr.getJSONObject(cnt);
                HashMap<String, Object> termMap = new HashMap<String, Object>();
                termMap.put("term", temp.getString("id"));
                termMap.put("termamount", Double.parseDouble(temp.getString("termamount")));
                termMap.put("termtaxamount", temp.optDouble("termtaxamount",0));
                termMap.put("termtaxamountinbase", temp.optDouble("termtaxamountinbase",0));
                termMap.put("termtax", temp.optString("termtax",null));
                termMap.put("termAmountExcludingTax", temp.optDouble("termAmountExcludingTax",0));
                termMap.put("termAmountExcludingTaxInBase", temp.optDouble("termAmountExcludingTaxInBase",0));
                termMap.put("termamountinbase", temp.optDouble("termamountinbase",0));
                double percentage = 0;
                if (!StringUtil.isNullOrEmpty(temp.getString("termpercentage"))) {
                    percentage = Double.parseDouble(temp.getString("termpercentage"));
                }
                termMap.put("termpercentage", percentage);
                termMap.put("creationdate", new Date());
                termMap.put("userid", userid);
                termMap.put("grdetail",temp.optString("grdetail"));
                if (isGR) {
                    termMap.put("goodsReceiptOrderID", ID);
                    accGoodsReceiptobj.saveGoodsReceiptTermMap(termMap);
                }else{
                    termMap.put("invoice", ID);
                    accGoodsReceiptobj.saveInvoiceTermMap(termMap);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return ll;
    }
    public List mapReceiptDetailTerms(String termsObj, Inventory invObj, String userid) throws ServiceException {
        List ll = new ArrayList();
        try {
            JSONArray termsArr = new JSONArray(termsObj);
            for (int cnt = 0; cnt < termsArr.length(); cnt++) {
                JSONObject temp = termsArr.getJSONObject(cnt);
                HashMap<String, Object> termMap = new HashMap<String, Object>();
                termMap.put("term", temp.has("termid")?temp.getString("termid"):temp.getString("id"));
                termMap.put("termamount", StringUtil.isNullOrEmpty(temp.getString("termamount"))? 0.0 : Double.parseDouble(temp.getString("termamount")));
                if (temp.has("glaccount") && !StringUtil.isNullOrEmpty(temp.getString("glaccount"))) {
                    termMap.put("accountid", temp.getString("glaccount"));
                }
                double percentage = 0;
                if (!StringUtil.isNullOrEmpty(temp.getString("termpercentage"))) {
                    percentage = Double.parseDouble(temp.getString("termpercentage"));
                }
                termMap.put("termpercentage", percentage);
                termMap.put("assessablevalue", temp.has("assessablevalue") && !StringUtil.isNullOrEmpty(temp.getString("assessablevalue")) ? Double.parseDouble(temp.getString("assessablevalue")) : 0.0);
//                termMap.put("glaccountname",temp.has("glaccountname")? temp.getString("glaccountname") : "");
//                termMap.put("accountid",temp.has("accountid")? temp.getString("accountid") : "");
//                termMap.put("glaccount",temp.has("glaccount")? temp.getString("glaccount") : "");
                termMap.put("creationdate", new Date());
                termMap.put("userid", userid);
                termMap.put("purchasevalueorsalevalue", (temp.has("purchasevalueorsalevalue") && !StringUtil.isNullOrEmpty(temp.getString("purchasevalueorsalevalue")))?temp.getDouble("purchasevalueorsalevalue"):0.0);
                termMap.put("deductionorabatementpercent", (temp.has("deductionorabatementpercent") && !StringUtil.isNullOrEmpty(temp.getString("deductionorabatementpercent")))?temp.getDouble("deductionorabatementpercent"):0.0);
                
                if (temp.has("taxtype") && !StringUtil.isNullOrEmpty(temp.getString("taxtype"))) {
                    termMap.put("taxtype", temp.getInt("taxtype"));
                    if (temp.has("taxvalue") && !StringUtil.isNullOrEmpty(temp.getString("taxvalue"))) {
                        if(temp.getInt("taxtype")==0){ // If Flat
                            termMap.put("termamount", temp.getDouble("termamount"));
                        } else { // Else Percentage
                            termMap.put("termpercentage", temp.getDouble("taxvalue"));
                        }
                    }
                }
                /**
                 * ERP-32829 
                 */
                termMap.put("isDefault", temp.optString("isDefault", "false"));
                termMap.put("productentitytermid", temp.optString("productentitytermid"));
                termMap.put("creditnotavailedaccount", temp.optString("creditnotavailedaccount"));
                termMap.put("payableaccountid", temp.optString("payableaccountid"));
                termMap.put("termname", temp.optString("term"));
                ll.add(termMap);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return ll;
    }

    public ModelAndView saveGoodsReceiptOrder(HttpServletRequest request, HttpServletResponse response) {
        
        JSONObject jobj = new JSONObject();
        String channelName = "";
        boolean issuccess = false;
        JSONObject paramJobj = null;
        try {
            /*Get request parameters */
            paramJobj = StringUtil.convertRequestToJsonObject(request);
            /*Call to Save Delivery Order Details*/
            
            String baseUrl = URLUtil.getPageURL(request, loginpageFull);
            paramJobj.put("baseUrl", baseUrl);
            String userName = sessionHandlerImpl.getUserFullName(request);
            paramJobj.put(Constants.username,userName);
            
            jobj = accGoodsReceiptModuleService.saveGoodsReceiptOrder(paramJobj);
            issuccess = jobj.optBoolean(Constants.RES_success, false);
            channelName = jobj.optString(Constants.channelName, null);
            jobj.put(Constants.userSessionId, (request.getSession(true)).getAttribute(Constants.userSessionId));
        } catch (AccountingException ex) {
             try {
                jobj.put("success", false);
                jobj.put("msg", ex.getMessage());
                if (!jobj.has("accException")) {
                    jobj.put("accException", paramJobj.optBoolean("accException", false));
                }
            } catch (JSONException e1x) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, e1x);
            }
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            if (issuccess && !StringUtil.isNullOrEmpty(channelName)) {
                ServerEventManager.publish(channelName, jobj.toString(), (ServletContext) this.getServletContext());
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public HashMap<String, Object> createGROrderMap(HttpServletRequest request) {

        HashMap<String, Object> grOrderMap = new HashMap<String, Object>();
        String entryNumber="",companyid="";
        try {
            companyid = sessionHandlerImpl.getCompanyid(request);
            String grid = request.getParameter("doid");
            DateFormat df = authHandler.getDateOnlyFormat(request);
            boolean isConsignment = request.getParameter("isConsignment") != null ? Boolean.parseBoolean(request.getParameter("isConsignment")) : false;
            boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false;
            boolean isAutoCreateDO = Boolean.FALSE.parseBoolean(request.getParameter("isAutoCreateDO"));
            boolean isEdit = StringUtil.isNullOrEmpty(request.getParameter("isEdit")) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
            boolean isCopy = StringUtil.isNullOrEmpty(request.getParameter("copyInv")) ? false : Boolean.parseBoolean(request.getParameter("copyInv"));
            entryNumber = request.getParameter("numberDo") != null ? request.getParameter("numberDo") : request.getParameter("number");
            String modifiedby = sessionHandlerImpl.getUserid(request);
            String costCenterId = request.getParameter("costcenter");
            String status = request.getParameter("statuscombo");
            long updatedon = System.currentTimeMillis();
            grOrderMap.put("isConsignment", isConsignment);
            grOrderMap.put("isFixedAsset", isFixedAsset);
            grOrderMap.put("isEdit", isEdit);
            grOrderMap.put("isCopy", isCopy);
            grOrderMap.put("grid", grid);
            grOrderMap.put("companyid", companyid);
            grOrderMap.put("entryNumber", entryNumber);
            grOrderMap.put("isAutoCreateDO", isAutoCreateDO);
            
            grOrderMap.put("id",request.getParameter("doid"));  //GoodReceiptOrder ID        
            grOrderMap.put("modifiedby", modifiedby);
            grOrderMap.put("updatedon", updatedon);
            grOrderMap.put("orderdate", df.parse(request.getParameter("billdate")));
            
            grOrderMap.put("memo", request.getParameter("memo"));
            grOrderMap.put("shipvia", request.getParameter("shipvia"));
            grOrderMap.put("fob", request.getParameter("fob"));
            grOrderMap.put("status", status);
            grOrderMap.put("agent", request.getParameter("agent"));
            
            if (!StringUtil.isNullOrEmpty(request.getParameter("permitNumber"))) {
                grOrderMap.put("permitNumber", request.getParameter("permitNumber"));
            }
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                grOrderMap.put("costCenterId", costCenterId);
            }
            
            if (request.getParameter(SHIPDATE) != null && !StringUtil.isNullOrEmpty(request.getParameter(SHIPDATE))) {
                grOrderMap.put(SHIPDATE, df.parse(request.getParameter(SHIPDATE)));
            }
            grOrderMap.put("customfield", request.getParameter("customfield"));
            grOrderMap.put("detail", request.getParameter("detail"));

        } catch (Exception ex) {

        }
        return grOrderMap;

    }
    private HashSet updateGoodsReceiptOrderRows(HashMap<String, Object> grOrderMap) throws ServiceException, JSONException {
        HashSet<GoodsReceiptOrderDetails> rows = new HashSet<>();
        String detail = "";
        boolean isFixedAsset = false;
        boolean isConsignment = false;
        String companyid = "", customfield = "", id = "";
        try {

            if (grOrderMap.containsKey("companyid") && grOrderMap.get("companyid") != null) {
                companyid = (String) grOrderMap.get("companyid");
            }
            if (grOrderMap.containsKey("isFixedAsset") && grOrderMap.get("isFixedAsset") != null) {
                isFixedAsset = (Boolean) grOrderMap.get("isFixedAsset");
            }
            if (grOrderMap.containsKey("isConsignment") && grOrderMap.get("isConsignment") != null) {
                isConsignment = (Boolean) grOrderMap.get("isConsignment");
            }
            if (grOrderMap.containsKey("customfield") && grOrderMap.get("customfield") != null) {
                customfield = (String) grOrderMap.get("customfield");
            }
            if (grOrderMap.containsKey("detail") && grOrderMap.get("detail") != null) {
                detail = (String) grOrderMap.get("detail");
            }
            if (grOrderMap.containsKey("id") && grOrderMap.get("id") != null) {
                id = (String) grOrderMap.get("id");
            }

            JSONArray jArr = new JSONArray(detail);

            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);

                String linkto = jobj.getString("linkto");
                GoodsReceiptOrderDetails row = null;
                if (jobj.has("rowid")) {
                    KwlReturnObject invDetailsResult = accountingHandlerDAOobj.getObject(GoodsReceiptOrderDetails.class.getName(), StringUtil.isNullOrEmpty(linkto)?jobj.getString("rowid"):jobj.getString("docrowid"));
                    row = (GoodsReceiptOrderDetails) invDetailsResult.getEntityList().get(0);
                }

                if (row != null) {
                    
                    if (jobj.has("srno")) {
                        row.setSrno(jobj.getInt("srno"));
                    }
                    if (!StringUtil.isNullOrEmpty(jobj.optString("description"))) {
                        try {
                            row.setDescription( StringUtil.DecodeText(jobj.optString("description")));
                        } catch (Exception ex) {
                            row.setDescription(jobj.optString("description"));
                        }
                    }

                    customfield = jobj.getString("customfield");
                    if (!StringUtil.isNullOrEmpty(customfield)) {
                        HashMap<String, Object> GROMap = new HashMap<String, Object>();
                        JSONArray jcustomarray = new JSONArray(customfield);
                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", "GoodsReceiptOrderDetails");
                        customrequestParams.put("moduleprimarykey", "GoodsReceiptOrderDetailsId");
                        customrequestParams.put("modulerecid", row.getID());
                        customrequestParams.put("moduleid", isFixedAsset ? Constants.Acc_FixedAssets_GoodsReceipt_ModuleId : isConsignment ? Constants.Acc_Consignment_GoodsReceiptOrder_ModuleId : Constants.Acc_Goods_Receipt_ModuleId);
                        customrequestParams.put("companyid", companyid);
                        GROMap.put("id", row.getID());
                        customrequestParams.put("customdataclasspath", Constants.Acc_GoodsReceiptOrderDetailsCustomDate_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            GROMap.put("grDetailsordercustomdataref", row.getID());
                            accGoodsReceiptobj.updateGRDetailsCustomData(GROMap);
                        }
                    }
                    // Add Custom fields details for Product
                    if (!StringUtil.isNullOrEmpty(jobj.optString("productcustomfield", ""))) {
                        JSONArray jcustomarray = new JSONArray(jobj.optString("productcustomfield", "[]"));
                        HashMap<String, Object> grMap = new HashMap<>();
                        HashMap<String, Object> customrequestParams = new HashMap<>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", "GrProductCustomData");
                        customrequestParams.put("moduleprimarykey", "GrDetailID");
                        customrequestParams.put("modulerecid", row.getID());
                        customrequestParams.put("moduleid", Constants.Acc_Goods_Receipt_ModuleId);
                        customrequestParams.put("companyid", companyid);
                        grMap.put("id", row.getID());
                        customrequestParams.put("customdataclasspath", Constants.Acc_GRODetail_Productcustom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            grMap.put("grodetailproductcustomdataref", row.getID());
                            accGoodsReceiptobj.updateGRDetailsProductCustomData(grMap);
                        }
                    }
                    rows.add(row);
                }
            }

        } catch (JSONException ex) {
            throw ServiceException.FAILURE("updateInvoiceRows : " + ex.getMessage(), ex);
        } catch (Exception e) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, e);
        }
        return rows;
    }
    
    public List updateGoodsReceiptOrder(HashMap<String, Object> grOrderMap) throws SessionExpiredException, ServiceException, AccountingException, JSONException, UnsupportedEncodingException {
        List list = new ArrayList();
        GoodsReceiptOrder grOrder = null;
        boolean isFixedAsset = false;
        boolean isConsignment = false;
        String companyid = "",customfield="";

        try{
        if (grOrderMap.containsKey("companyid") && grOrderMap.get("companyid") != null) {
            companyid = (String) grOrderMap.get("companyid");
        }
        if (grOrderMap.containsKey("isFixedAsset") && grOrderMap.get("isFixedAsset") != null) {
            isFixedAsset = (Boolean) grOrderMap.get("isFixedAsset");
        }
        if (grOrderMap.containsKey("isConsignment") && grOrderMap.get("isConsignment") != null) {
            isConsignment = (Boolean) grOrderMap.get("isConsignment");
        }
        if (grOrderMap.containsKey("customfield") && grOrderMap.get("customfield") != null) {
            customfield = (String) grOrderMap.get("customfield");
        }
        
        
         KwlReturnObject doresult = accGoodsReceiptobj.saveGoodsReceiptOrder(grOrderMap);
         grOrder = (GoodsReceiptOrder) doresult.getEntityList().get(0);
        
        
        grOrderMap.put("id", grOrder.getID());
         
        HashSet<GoodsReceiptOrderDetails> groDetails =updateGoodsReceiptOrderRows(grOrderMap);
        list.add(grOrder);
        if (!StringUtil.isNullOrEmpty(customfield)) {
            JSONArray jcustomarray = new JSONArray(customfield);
            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
            customrequestParams.put("customarray", jcustomarray);
            customrequestParams.put("modulename", Constants.Acc_GoodsReceipt_modulename);
            customrequestParams.put("moduleprimarykey", Constants.Acc_GoodsReceiptId);
            customrequestParams.put("modulerecid", grOrder.getID());
            customrequestParams.put("moduleid", isFixedAsset ? Constants.Acc_FixedAssets_GoodsReceipt_ModuleId : isConsignment ? Constants.Acc_Consignment_GoodsReceiptOrder_ModuleId : Constants.Acc_Goods_Receipt_ModuleId);
            customrequestParams.put("companyid", companyid);
            customrequestParams.put("customdataclasspath", Constants.Acc_GoodsReceipt_custom_data_classpath);
            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                grOrderMap.put("accgoodsreceiptcustomdataref", grOrder.getID());
                KwlReturnObject accresult = accGoodsReceiptobj.updateGoodsReceiptCustomData(grOrderMap);
            }
        }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("updateInvoiceRows : " + ex.getMessage(), ex);
        } catch (Exception e) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, e);
        }

        return list;
    }
    public ModelAndView updateGoodsReceiptOrder(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        String billno = "";
        String billid = "";
        String channelName = "", entryNumber = "", companyid = "",moduleName="";
        boolean issuccess = false;
        boolean accexception = false;
        boolean isEdit = false;
        boolean isCopy = false;
        boolean isConsignment = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;//txnManager.getTransaction(def);

        HashMap<String, Object> grOrderMap = createGROrderMap(request);

        if (grOrderMap.containsKey("isEdit") && grOrderMap.get("isEdit") != null) {
            isEdit = (Boolean) grOrderMap.get("isEdit");
        }
        if (grOrderMap.containsKey("isCopy") && grOrderMap.get("isCopy") != null) {
            isCopy = (Boolean) grOrderMap.get("isCopy");
        }

        if (grOrderMap.containsKey("isConsignment") && grOrderMap.get("isConsignment") != null) {
            isConsignment = (Boolean) grOrderMap.get("isConsignment");
        }
        if (grOrderMap.containsKey("companyid") && grOrderMap.get("companyid") != null) {
            companyid = (String) grOrderMap.get("companyid");
        }
        if (grOrderMap.containsKey("entryNumber") && grOrderMap.get("entryNumber") != null) {
            entryNumber = (String) grOrderMap.get("entryNumber");
        }
        

        int approvedLevel = 11;
        try {
            status = txnManager.getTransaction(def);
            List li = updateGoodsReceiptOrder(grOrderMap);
            GoodsReceiptOrder grOrder = (GoodsReceiptOrder) li.get(0);
            billid = grOrder.getID();
            billno = grOrder.getGoodsReceiptOrderNumber();
            issuccess = true;

            if (isConsignment) {
                msg = messageSource.getMessage("acc.consignment.GR.save", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.gro.save", null, RequestContextUtils.getLocale(request));
            }

            String auditSMS = "";
            String action = "added new";
            if (isEdit == true && isCopy == false) {
                action = "updated";
            }

            if(isConsignment) {
                moduleName ="Consignment Goods Receipt";
                msg +=  messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + billno + "</b>";   //"Goods receipt has been saved successfully";
                auditSMS = " has " + action + " "+moduleName+" " + billno+(approvedLevel != 11 ? " "+messageSource.getMessage("acc.field.whichispendingforApproval", null, null) : "");
            } else {
                moduleName = Constants.Goods_Receipt;
                msg +=  messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + billno + "</b>";   //"Goods receipt has been saved successfully";
                auditSMS = " has " + action + " "+moduleName+" " + billno+(approvedLevel != 11 ? " "+messageSource.getMessage("acc.field.whichispendingforApproval", null, null) : "");
            }
            
            auditTrailObj.insertAuditLog(AuditAction.GOODS_RECEIPT_CREATED, "User " + sessionHandlerImpl.getUserFullName(request) + auditSMS, request, grOrder.getID());


            if (grOrder.isFixedAssetGRO() && !(grOrder.isIsconsignment())) {
                channelName = "/FixedAssetReceiptList/gridAutoRefresh";
            } else if (!(grOrder.isFixedAssetGRO() || grOrder.isIsconsignment())) {
                channelName = "/GoodsReceiptReport/gridAutoRefresh";
            }
            txnManager.commit(status);
        } catch (AccountingException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
           
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("billid", billid);
                jobj.put("billno", billno);
                jobj.put("accException", accexception);
                jobj.put("pendingApproval", approvedLevel != 11);
                if (issuccess && !StringUtil.isNullOrEmpty(channelName)) {
                    jobj.put(Constants.userSessionId, (request.getSession(true)).getAttribute(Constants.userSessionId));// adding user session id to idenntify unique user session
                    ServerEventManager.publish(channelName, jobj.toString(), (ServletContext) this.getServletContext());
                }
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

     public void getNewBatchJson(Product product, HttpServletRequest request, String documentid, HashMap<Integer, Object[]> grBatchdetalisMap) throws ServiceException, SessionExpiredException, JSONException, ParseException {
        JSONArray jSONArray = new JSONArray();
        DateFormat df = authHandler.getDateOnlyFormat(request);
        KwlReturnObject kmsg = null;
        boolean linkingFlag = (StringUtil.isNullOrEmpty(request.getParameter("linkingFlag")))?false:Boolean.parseBoolean(request.getParameter("linkingFlag"));
        boolean isEdit=(StringUtil.isNullOrEmpty(request.getParameter("isEdit")))?false:Boolean.parseBoolean(request.getParameter("isEdit"));
        String moduleID=request.getParameter("moduleid");
          boolean isBatch=false;
        if (!product.isIsBatchForProduct() && !product.isIslocationforproduct() && !product.isIswarehouseforproduct() && !product.isIsrowforproduct() && !product.isIsrackforproduct() && !product.isIsbinforproduct() && product.isIsSerialForProduct()) {
            kmsg = accCommonTablesDAO.getOnlySerialDetails(documentid,linkingFlag,moduleID,false,isEdit);
        } else {
             isBatch=true;
            kmsg = accCommonTablesDAO.getBatchSerialDetails(documentid,!product.isIsSerialForProduct(),linkingFlag,moduleID,false,isEdit,"");
        }
//      
        int count=0;
        double ActbatchQty = 1;
        double batchQty = 0;
        List batchserialdetails = kmsg.getEntityList();
        Iterator iter = batchserialdetails.iterator();
        while (iter.hasNext()) {
            Object[] objArr = (Object[]) iter.next();
            String productBatchId=objArr[0] != null ? (String) objArr[0] : "";            
           String batchid=(String)objArr[0];
           String pid=(String)objArr[0]; 
           String mfgdate="";
           String expdate="";
            if (!product.isIsBatchForProduct() && !product.isIslocationforproduct() && !product.isIswarehouseforproduct() && !product.isIsrowforproduct() && !product.isIsrackforproduct() && !product.isIsbinforproduct() && product.isIsSerialForProduct()) {
                mfgdate="";
                expdate="";
            } else {
                mfgdate=objArr[4] != null ? df.format(objArr[4]) : "";
                expdate=objArr[5] != null ? df.format(objArr[5]) : "";
            }

           
            String serialDetailsId= objArr[7] != null ? (String) objArr[7] : "";
//        
            
            //for blocking quantity in salesorder use hashmap
            Object[] batchserialobjArr= null; 
                if (!StringUtil.isNullOrEmpty(serialDetailsId) && !StringUtil.isNullOrEmpty(productBatchId)) {
                    batchserialobjArr = new Object[5];
                    batchserialobjArr[0] = serialDetailsId;
                    batchserialobjArr[1] = productBatchId;
                    if ( !StringUtil.isNullOrEmpty(mfgdate)) {
                        batchserialobjArr[2] = authHandler.getDateOnlyFormat(request).parse(mfgdate);
                    }
                    if (!StringUtil.isNullOrEmpty(expdate) ) {
                        batchserialobjArr[3] = authHandler.getDateOnlyFormat(request).parse(expdate);
                    }
                    grBatchdetalisMap.put(count++, batchserialobjArr);
                }

        }

    }
    public ModelAndView approveGoodsReceiptOrder(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Invoice_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        StringBuffer productIds = new StringBuffer();
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            Boolean restrictDuplicateBatch = false;
            String remark = request.getParameter("remark");
            String groID = request.getParameter("billid");
            String currentUser = sessionHandlerImpl.getUserid(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject GROObj = accountingHandlerDAOobj.getObject(GoodsReceiptOrder.class.getName(), groID);
            GoodsReceiptOrder groObj = (GoodsReceiptOrder) GROObj.getEntityList().get(0);
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            ExtraCompanyPreferences extraCompanyPreferences = null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            double amount = StringUtil.isNullOrEmpty(request.getParameter("totalorderamount"))? 0 : authHandler.round(Double.parseDouble(request.getParameter("totalorderamount")),companyid);
            
            String postingDateStr = request.getParameter("postingDate");
            DateFormat df = authHandler.getDateOnlyFormat();
            Date postingDate = null;
            if (!StringUtil.isNullOrEmpty(postingDateStr)) {
                postingDate = df.parse(postingDateStr);
            }
            HashMap<String, Object> grApproveMap = new HashMap<String, Object>();      
            Set<GoodsReceiptOrderDetails> groDetRows = groObj.getRows();
            int level = groObj.getApprovestatuslevel();
            String currencyid=groObj.getCurrency()!=null?groObj.getCurrency().getCurrencyID():sessionHandlerImpl.getCurrencyID(request);
            HashMap<String, Object> GlobalParams = AccountingManager.getGlobalParams(request);
            double subtotal = 0;
            double quantity = 0;
            String baseUrl = URLUtil.getPageURL(request, loginpageFull);
            JSONArray productDiscountJArr=new JSONArray();
            if (groDetRows != null && !groDetRows.isEmpty()) {
                for (GoodsReceiptOrderDetails cnt : groDetRows) {
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
                    KwlReturnObject dAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, rowDiscVal, currencyid, groObj.getOrderDate(), groObj.getExternalCurrencyRate());
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
            
            grApproveMap.put("companyid", companyid);
            grApproveMap.put("level", level);
            grApproveMap.put("totalAmount", String.valueOf(amount));
            grApproveMap.put("currentUser", currentUser);
            grApproveMap.put("fromCreate", false);
            grApproveMap.put("productDiscountMapList", productDiscountJArr);
            grApproveMap.put("moduleid", Constants.Acc_Goods_Receipt_ModuleId);
            grApproveMap.put(Constants.PAGE_URL, baseUrl);
            String exPrefObject=extraCompanyPreferences.getColumnPref();
            JSONObject jObj = StringUtil.isNullObject(exPrefObject) ? new JSONObject() : new JSONObject(exPrefObject.toString());
            if (!StringUtil.isNullObject(jObj) && jObj.has(Constants.IS_POSTING_DATE_CHECK) && jObj.get(Constants.IS_POSTING_DATE_CHECK) != null && jObj.optBoolean(Constants.IS_POSTING_DATE_CHECK, false)) {
                grApproveMap.put("postingDate", postingDate);
            }
            if(jObj.has("restrictDuplicateBatch")){
                restrictDuplicateBatch = jObj.getBoolean("restrictDuplicateBatch");
            }
            List approvedLevelList = accGoodsReceiptModuleService.approveGRO(groObj, grApproveMap, true);
            int approvedLevel = (Integer) approvedLevelList.get(0);
            
            /*---Update Inventory & Stock------*/
            if (approvedLevel == 11) {
                Set<GoodsReceiptOrderDetails> groDetails = groObj.getRows();
                 List<StockMovement> stockMovementsList = new ArrayList<>();
                for (GoodsReceiptOrderDetails groDetail : groDetails) {
                     Product product=groDetail.getProduct();
                    if (product != null && extraCompanyPreferences.isActivateInventoryTab() &&(product.isIsBatchForProduct() || product.isIslocationforproduct() || product.isIswarehouseforproduct() || product.isIsrowforproduct() || product.isIsrackforproduct() || product.isIsbinforproduct() && product.isIsSerialForProduct())) {
                        accGoodsReceiptModuleService.newStockMovementGROrder(groDetail, stockMovementsList);
                    }
                    Inventory inventory = groDetail.getInventory();
                    if (inventory.isInvrecord()) {
                        inventory.setBaseuomquantity(inventory.getActquantity());
                        inventory.getProduct().setAvailableQuantity(inventory.getProduct().getAvailableQuantity()+inventory.getBaseuomquantity());
                        inventory.setActquantity(0.0);
                    }
                }
                    //ERP-37579 after GRN with pending approval has been approved update quantity in NewProductbatch and NewBatchSerial tables
                    accGoodsReceiptModuleService.updateInvTablesAfterPendingApproval(groObj,false,restrictDuplicateBatch);
                if (extraCompanyPreferences != null && extraCompanyPreferences.isActivateInventoryTab() && !stockMovementsList.isEmpty()) {
                    stockMovementService.addOrUpdateBulkStockMovement(groObj.getCompany(), groObj.getID(), stockMovementsList);
                }
            }
            

            /*----Send mail if allowed from system preferences------- */
            if (approvedLevel != Constants.NoAuthorityToApprove && preferences.isSendapprovalmail()) { // If allow to send approval mail in company account preferences


                HashMap emailMap = new HashMap();
                String userName = sessionHandlerImpl.getUserFullName(request);
                emailMap.put("userName", userName);
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                Company company = (Company) returnObject.getEntityList().get(0);
                emailMap.put("company", company);
                emailMap.put("goodsReceipt", groObj);
                emailMap.put("baseUrl", baseUrl);
                emailMap.put("preferences", preferences);
                emailMap.put("ApproveMap", grApproveMap);
                emailMap.put("level", level);
                accGoodsReceiptModuleService.sendApprovalMailForGRIfAllowedFromSystemPreferences(emailMap);

            }
                        
            int pendingApprovalFlag = (approvedLevel != 11) ? 1 : 0;
            if (groObj.getInventoryJE() != null && extraCompanyPreferences != null && (extraCompanyPreferences.isActivateMRPModule() || preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD)) {
                groObj.getInventoryJE().setPendingapproval(pendingApprovalFlag);
            }
            // Save Approval History
            if (approvedLevel != Constants.NoAuthorityToApprove) {
                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                hashMap.put("transtype", Constants.GOODS_RECEIPT_APPROVAL);
                hashMap.put("transid", groObj.getID());
                hashMap.put("approvallevel", groObj.getApprovestatuslevel()); //  If approvedLevel = 11 then its final Approval
                hashMap.put("remark", remark);
                hashMap.put("userid", sessionHandlerImpl.getUserid(request));
                hashMap.put("companyid", companyid);
                accountingHandlerDAOobj.updateApprovalHistory(hashMap);

                // Audit log entry
                auditTrailObj.insertAuditLog("65", "User " + sessionHandlerImpl.getUserFullName(request) + " has Approved a Goods Receipt " + groObj.getGoodsReceiptOrderNumber()+" at Level-"+groObj.getApprovestatuslevel(), request, groObj.getID());
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
                msg = messageSource.getMessage("acc.field.GoodsReceiptHasBeenApprovedSuccessfully", null, RequestContextUtils.getLocale(request))+" by "+roleName+" "+sessionHandlerImpl.getUserFullName(request)+" at Level "+groObj.getApprovestatuslevel()+".";
            }else{
                txnManager.commit(status);
                issuccess = true;
                msg = messageSource.getMessage("acc.msgbox.YouarenotauthorizedtoapprovethisrecordatLevel", null, RequestContextUtils.getLocale(request)) + groObj.getApprovestatuslevel()+".";
            }
            
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        }catch (AccountingException ae){
            txnManager.rollback(status);
            msg = "" + ae.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ae);
        }catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("productIds", productIds);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
 
    /*---As function is moved to service layer that's why commented this function---------- */
    
    /* 
    private void newStockMovementGROrder(GoodsReceiptOrderDetails goodsReceiptOrderDetails,List<StockMovement> stockMovementsList) throws ServiceException{
        try {
            String documentid=goodsReceiptOrderDetails.getID();
            Product product=goodsReceiptOrderDetails.getProduct();
            KwlReturnObject kmsg = null;
            List<Object[]> batchserialdetails = null;
            if (!product.isIsSerialForProduct()) {
                kmsg = accCommonTablesDAO.getBatchSerialDetails(documentid, true, false,  Integer.toString(Constants.Acc_Goods_Receipt_ModuleId), false, true, "");
                batchserialdetails = kmsg.getEntityList();
            } else {
                kmsg = accCommonTablesDAO.getBatchSerialDetails(documentid, !product.isIsSerialForProduct(), false, Integer.toString(Constants.Acc_Goods_Receipt_ModuleId), false, true, "");
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
                                    stockMovement.setPricePerUnit(goodsReceiptOrderDetails.getBaseuomrate() < 1 ? (goodsReceiptOrderDetails.getRate() * (1 / goodsReceiptOrderDetails.getBaseuomrate())) : goodsReceiptOrderDetails.getRate() / goodsReceiptOrderDetails.getBaseuomrate());
                                    stockMovement.setQuantity(batchQty);
                                    stockMovement.setTransactionDate(goodsReceiptOrderDetails.getGrOrder().getOrderDate());
                                    stockMovement.setModuleRefId(goodsReceiptOrderDetails.getGrOrder().getID());
                                    stockMovement.setModuleRefDetailId(goodsReceiptOrderDetails.getID());
                                    stockMovement.setVendor(goodsReceiptOrderDetails.getGrOrder().getVendor());
                                    stockMovement.setCostCenter(goodsReceiptOrderDetails.getGrOrder().getCostcenter());
                                    stockMovement.setTransactionNo(goodsReceiptOrderDetails.getGrOrder().getGoodsReceiptOrderNumber());
                                    if (goodsReceiptOrderDetails.getGrOrder().isIsconsignment()) {
                                        stockMovement.setTransactionModule(TransactionModule.ERP_Consignment_GR);
                                        stockMovement.setRemark("Consignment GRN created");
                                    } else {
                                        stockMovement.setTransactionModule(TransactionModule.ERP_GRN);
                                        stockMovement.setRemark("GRN created");
                                    }
                                    stockMovement.setTransactionType(TransactionType.IN);
                                    stockMovement.setMemo(goodsReceiptOrderDetails.getGrOrder().getMemo());
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
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.INFO, ex.getMessage());
        }
    } */
    public ModelAndView rejectPendingGR(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("GoodsReceipt_Tx");
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
            invID =  StringUtil.DecodeText(jObj.getString("billid"));
            KwlReturnObject invRes = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invID);
            GoodsReceipt invObj = (GoodsReceipt) invRes.getEntityList().get(0);
            level = invObj.getApprovestatuslevel();
            
            boolean isRejected = rejectPendingGR(request);
            txnManager.commit(status);
            issuccess = true;
            
            if (isRejected) {
                msg = messageSource.getMessage("acc.field.PurchaseInvoicehasbeenrejectedsuccessfully", null, RequestContextUtils.getLocale(request)) + " by " + roleName + " " + sessionHandlerImpl.getUserFullName(request) + " at Level " + level + ".";
            } else {
                msg = messageSource.getMessage("acc.vq.notAuthorisedToRejectThisRecord", null, RequestContextUtils.getLocale(request)) + " at Level " + level + ".";
            }
            
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public boolean rejectPendingGR(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException, ScriptException {
        boolean isRejected = false;
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currentUser = sessionHandlerImpl.getUserid(request);
            String remark = request.getParameter("remark");
            KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), currentUser);
            User user = (User) userResult.getEntityList().get(0);
            String actionId = "66", actionMsg = "rejected";
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            String countryid = user.getCompany().getCountry().getID();
            int level = 0;
            String amount = "";
            for (int i = 0; i < jArr.length(); i++) {
                boolean hasAuthorityToReject = false;
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("billid"))) {
                    String invid =  StringUtil.DecodeText(jobj.getString("billid"));
                    KwlReturnObject cap = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invid);
                    GoodsReceipt invObj = (GoodsReceipt) cap.getEntityList().get(0);
                    double totalAmount = StringUtil.isNullOrEmpty(request.getParameter("totalorderamount")) ? 0 : authHandler.round(Double.parseDouble(request.getParameter("totalorderamount")), companyid);
                    HashMap<String, Object> invApproveMap = new HashMap<String, Object>();
                    level = invObj.getApprovestatuslevel();
                    invApproveMap.put("companyid", companyid);
                    invApproveMap.put("level", level);
                    invApproveMap.put("totalAmount", String.valueOf(totalAmount));
                    invApproveMap.put("currentUser", currentUser);
                    invApproveMap.put("fromCreate", false);
                    invApproveMap.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);
                    amount = String.valueOf(totalAmount);
                    if (AccountingManager.isCompanyAdmin(user)) {
                        hasAuthorityToReject = true;
                    } else {
                        hasAuthorityToReject = accountingHandlerDAOobj.checkForMultiLevelApprovalRules(invApproveMap);
                    }
                    if (hasAuthorityToReject) {
                        // also reject pending auto generated GR for same Invoice if any
                        KwlReturnObject InvoiceGRO = accGoodsReceiptobj.getAutoGeneratedGROFromVInvoices(invObj.getID(), companyid);
                        if (InvoiceGRO.getEntityList() != null && InvoiceGRO.getEntityList().size() > 0) {
                            Object[] oj = (Object[]) InvoiceGRO.getEntityList().get(0);
                            String goodsRecieptOrderID = oj[1].toString();
                            accGoodsReceiptobj.rejectPendingGRO(goodsRecieptOrderID, companyid);
                        }
                        
                        /* If rejected PI linked with PO then It will be available for another PI for transaction.*/
                        
                        HashMap<String, Object> doRequestParams = new HashMap<String, Object>();
                        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                        filter_names.add("goodsReceipt.ID");
                        filter_params.add(invid);
                        doRequestParams.put("filter_names", filter_names);
                        doRequestParams.put("filter_params", filter_params);

                        KwlReturnObject podresult = accGoodsReceiptobj.getGoodsReceiptDetails(doRequestParams);
                        Iterator itr = podresult.getEntityList().iterator();
                        while (itr.hasNext()) {
                            GoodsReceiptDetail row = (GoodsReceiptDetail) itr.next();
                            if (row.getPurchaseorderdetail() != null) {
                                String linkid = row.getPurchaseorderdetail().getPurchaseOrder().getID();
                                if (!StringUtil.isNullOrEmpty(linkid)) {
                                    KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), linkid);
                                    PurchaseOrder purchaseOrder = (PurchaseOrder) rdresult.getEntityList().get(0);
                                    HashMap hMap = new HashMap();
                                    hMap.put("purchaseOrder", purchaseOrder);
                                    hMap.put("value", "0");
                                    accGoodsReceiptobj.updatePOLinkflag(hMap);
                                }
                            }
                        }
                        /* Delete RCM Un-Registered Journal Entry details table data on Reject Puurchase Invoice
                         * && invObj.isGtaapplicable()
                         */
                        if(Constants.isRCMPurchaseURD5KLimit && !StringUtil.isNullOrEmpty(countryid) && Integer.parseInt(countryid) == Constants.indian_country_id){
                            if (invObj != null && invObj.getVendor() != null) {
                                if (invObj.getVendor().getGSTRegistrationType() != null && invObj.getVendor().getGSTRegistrationType().getDefaultMasterItem() != null) {
                                    String DefaultMasterItemId = invObj.getVendor().getGSTRegistrationType().getDefaultMasterItem().getID();
                                    if (DefaultMasterItemId.equals(Constants.GSTRegType.get(Constants.GSTRegType_Unregistered))) {
                                        JSONObject URDjobj = new JSONObject();
                                        URDjobj.put("receiptID", invObj.getID());
                                        URDjobj.put(Constants.companyid, companyid);
                                        accGoodsReceiptobj.deleteURDVendorRCMPurchaseInvoice(URDjobj);
                                        /**
                                         * Modify All Journal Entry details if daily
                                         * limit cross on particular Bill date
                                         */
                                        Date entryDateForLock = null;
                                        DateFormat dateFormatForLock = authHandler.getDateOnlyFormat();
                                        entryDateForLock = invObj.getJournalEntry().getEntryDate();
                                        if (entryDateForLock != null) {
                                            paramJobj.put("billdate", dateFormatForLock.format(entryDateForLock));
                                            paramJobj.put("companyid", companyid);
                                            paramJobj.put(Constants.df, dateFormatForLock);
                                            paramJobj.put("invoiceAmount", 0);
                                            paramJobj.put("GRNNumber", invObj.getGoodsReceiptNumber());
                                            accGoodsReceiptModuleService.modifyURDVendorRCMPurchaseInvoiceJEDetails(paramJobj);
                                        }
                                    }
                                }
                            }
                        }
                        //then Reject the Invoice
                        accGoodsReceiptobj.rejectPendingGR(invObj.getID(), companyid);
                        isRejected = true;
                        String JeMsg = "";
                        if (invObj.getJournalEntry()!=null && !StringUtil.isNullOrEmpty(invObj.getJournalEntry().getEntryNumber())) {
                            JeMsg = " with " + messageSource.getMessage("acc.field.JENo", null, RequestContextUtils.getLocale(request)) + ":" + invObj.getJournalEntry().getEntryNumber();
                        }
                        // Maintain Approval History of Rejected Record
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("transtype", Constants.VENDOR_Invoice_APPROVAL);
                        hashMap.put("transid", invObj.getID());
                        hashMap.put("approvallevel", Math.abs(invObj.getApprovestatuslevel()));//  If approvedLevel = 11 then its final Approval
                        hashMap.put("remark",remark);
                        hashMap.put("userid", sessionHandlerImpl.getUserid(request));
                        hashMap.put("companyid", companyid);
                        hashMap.put("isrejected", true);
                        accountingHandlerDAOobj.updateApprovalHistory(hashMap);
                        auditTrailObj.insertAuditLog(actionId, "User " + sessionHandlerImpl.getUserFullName(request) + " " + actionMsg + " Vendor Invoice " + invObj.getGoodsReceiptNumber()+ JeMsg + " at level -" + invObj.getApprovestatuslevel(), request, invObj.getID());
                    }
                }
            }
        }  catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)));
        }
        return isRejected;
    }

    public ModelAndView rejectPendingGRO(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Quotation_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String roleName="";
            int level=0;
            KwlReturnObject userRoleResult = permissionHandlerDAOObj.getRoleofUser(sessionHandlerImpl.getUserid(request));
            Iterator itr = userRoleResult.getEntityList().iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                roleName = row[1].toString();
            }            
            String grID="";
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            JSONObject jObj= jArr.getJSONObject(0);
            grID=  StringUtil.DecodeText(jObj.getString("billid"));
            KwlReturnObject GRObj = accountingHandlerDAOobj.getObject(GoodsReceiptOrder.class.getName(), grID);
            GoodsReceiptOrder grObj = (GoodsReceiptOrder) GRObj.getEntityList().get(0);
            level=grObj.getApprovestatuslevel();
            boolean isRejected=rejectPendingGRO(request);
            txnManager.commit(status);
            issuccess = true;
            if (isRejected) {
                msg = messageSource.getMessage("acc.field.GoodsReceiptHasBeenRejectedSuccessfully", null, RequestContextUtils.getLocale(request))+" by "+roleName+" "+sessionHandlerImpl.getUserFullName(request)+" at Level "+level+".";
            } else {
                msg = messageSource.getMessage("acc.vq.notAuthorisedToRejectThisRecord", null, RequestContextUtils.getLocale(request))+" at level "+level+".";
            }
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public boolean rejectPendingGRO(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException, ScriptException {
        boolean isRejected=false;
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currentUser = sessionHandlerImpl.getUserid(request);
            KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), currentUser);
            User user = (User) userResult.getEntityList().get(0);
            String actionId = "66", actionMsg = "rejected";
            int level=0;
            String amount="";
            for (int i = 0; i < jArr.length(); i++) {
                boolean hasAuthorityToReject=false;
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("billid"))) {
                    String doid =  StringUtil.DecodeText(jobj.getString("billid"));
                    KwlReturnObject cap = accountingHandlerDAOobj.getObject(GoodsReceiptOrder.class.getName(), doid);
                    GoodsReceiptOrder groObj = (GoodsReceiptOrder) cap.getEntityList().get(0);
                    double totalAmount = StringUtil.isNullOrEmpty(request.getParameter("totalorderamount")) ? 0 : authHandler.round(Double.parseDouble(request.getParameter("totalorderamount")), companyid);
                    amount=String.valueOf(totalAmount);
                    level= groObj.getApprovestatuslevel();
                    if (AccountingManager.isCompanyAdmin(user)) {
                        hasAuthorityToReject = true;
                    } else {
                        hasAuthorityToReject = accountingHandlerDAOobj.checkForMultiLevelApprovalRule(level, companyid, amount, currentUser, Constants.Acc_Goods_Receipt_ModuleId);
                    }
                    if(hasAuthorityToReject){
                        accGoodsReceiptobj.rejectPendingGRO(groObj.getID(), companyid);
                        isRejected=true;
                        // Maintain Approval History of Rejected Record
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("transtype", Constants.GOODS_RECEIPT_APPROVAL);
                        hashMap.put("transid", groObj.getID());
                        hashMap.put("approvallevel", Math.abs(groObj.getApprovestatuslevel()));//  If approvedLevel = 11 then its final Approval
                        hashMap.put("remark", "");
                        hashMap.put("userid", sessionHandlerImpl.getUserid(request));
                        hashMap.put("companyid", companyid);
                        hashMap.put("isrejected", true);
                        accountingHandlerDAOobj.updateApprovalHistory(hashMap);
                        auditTrailObj.insertAuditLog(actionId, "User " + sessionHandlerImpl.getUserFullName(request) + " " + actionMsg + " Goods Receipt " + groObj.getGoodsReceiptOrderNumber(), request, groObj.getID());
                    }
                }
            }
        }  catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)));
        }
        return isRejected;
    }
    
    public String getGoodsReceiptOrderStatus(Set<GoodsReceiptOrderDetails> orderDetail) throws ServiceException {
//        Set<GoodsReceiptOrderDetails> orderDetail = so.getRows();
        Iterator ite = orderDetail.iterator();

        String result = "Closed";
        while (ite.hasNext()) {
            GoodsReceiptOrderDetails soDetail = (GoodsReceiptOrderDetails) ite.next();
            KwlReturnObject idresult = accGoodsReceiptobj.getIDFromGROD(soDetail.getID());
            List list = idresult.getEntityList();
            Iterator ite1 = list.iterator();
            double qua = 0;
            while (ite1.hasNext()) {
                GoodsReceiptDetail ge = (GoodsReceiptDetail) ite1.next();
//                qua += ge.getInventory().isInvrecord() ? ge.getInventory().getQuantity() : ge.getInventory().getActquantity();
                qua += ge.getInventory().getQuantity();
            }
            if (qua < soDetail.getDeliveredQuantity()) {
                result = "Open";
                break;
            }
        }
        return result;
    }
    
//    public void saveAssetOpeningDepreciation(HttpServletRequest request, Set<AssetDetails> assetDetailsSet, String companyId) throws AccountingException, JSONException, SessionExpiredException, UnsupportedEncodingException {
//        try {
//            double openingBalance = 0.00;
//            int depreciationCalculationType = 0;
//            Date finanDate = null;
//            Map<String, Object> filterParams = new HashMap<String, Object>();
//            filterParams.put("id", companyId);
//            KwlReturnObject kresult = accCompanyPreferencesObj.getCompanyPreferences(filterParams);
//            CompanyAccountPreferences preferences = null;
//            if (kresult.getEntityList().size() > 0) {
//                preferences = (CompanyAccountPreferences) kresult.getEntityList().get(0);
//            }
//            KwlReturnObject extraresult = accCompanyPreferencesObj.getExtraCompanyPreferences(filterParams);
//            ExtraCompanyPreferences extra = null;
//            if (extraresult.getEntityList().size() > 0) {
//                extra = (ExtraCompanyPreferences) extraresult.getEntityList().get(0);
//            }
//            if (extra != null) {
//                depreciationCalculationType = extra.getAssetDepreciationCalculationType();
//            }
//            if (preferences != null && preferences.getFinancialYearFrom() != null) {
//                finanDate = preferences.getFirstFinancialYearFrom() != null ? preferences.getFirstFinancialYearFrom() : preferences.getFinancialYearFrom();
//            }
//            for (AssetDetails assetDetails : assetDetailsSet) {
//                if (assetDetails.getInstallationDate().before(finanDate)) {
//                    String backyears = "";
//
//                    Calendar cal1 = Calendar.getInstance();
//                    cal1.setTime(finanDate);
//                    cal1.add(Calendar.DATE, -1);
//
//                    int currentyear = cal1.get(Calendar.YEAR);
//                    Calendar cal2 = Calendar.getInstance();
//                    cal2.setTime(assetDetails.getInstallationDate());
//                    int creationyear1 = cal2.get(Calendar.YEAR);
//
//                    while (creationyear1 <= currentyear) {
//                        backyears += creationyear1 + ",";
//                        creationyear1++;
//                    }
//                    JSONArray curfinalJArr = new JSONArray();
//                    
//                    HashMap<String, Object> fieldrequestParams = new HashMap();
//                    fieldrequestParams.put("startMonth", assetDetails.getInstallationDate().getMonth());
//                    fieldrequestParams.put("endMonth", cal1.getTime().getMonth());
//                    fieldrequestParams.put("assetdetailIds", assetDetails.getId());
//                    fieldrequestParams.put("years", backyears);
//                    fieldrequestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
//                    fieldrequestParams.put("depreciationCalculationType", depreciationCalculationType);
//                    
//                    curfinalJArr = accProductModuleService.getAssetDepreciation(fieldrequestParams);
//
//                    for (int i = 0; i < curfinalJArr.length(); i++) {
//                        JSONObject newjobj = curfinalJArr.getJSONObject(i);
//                        if (newjobj.has("firstperiodamtInBase")) {
//                            openingBalance += newjobj.getDouble("firstperiodamtInBase");
//                        }
//                    }
//                    if (curfinalJArr.length() > 0) {
//                        for (int i = 0; i < curfinalJArr.length(); i++) {
//                            JSONObject jobj = curfinalJArr.getJSONObject(i);
//                            double perioddepreciation = Double.parseDouble(StringUtil.DecodeText(jobj.optString("perioddepreciation")));
//                            HashMap<String, Object> ddMap = new HashMap<String, Object>();
//                            ddMap.put("depreciationCreditToAccountId", (assetDetails.getProduct().getDepreciationProvisionGLAccount() != null) ? assetDetails.getProduct().getDepreciationProvisionGLAccount().getID() : assetDetails.getProduct().getPurchaseAccount().getID());// this is containing value of Asset Controlling Account.
//                            ddMap.put("depreciationGLAccountId", assetDetails.getProduct().getDepreciationGLAccount().getID());
//                            ddMap.put("productId", assetDetails.getProduct().getID());
//                            ddMap.put("assetId", assetDetails.getId());
//                            ddMap.put("period", Integer.parseInt(StringUtil.DecodeText(jobj.optString("period"))));
//                            ddMap.put("companyid", companyId);
//                            ddMap.put("jeid", null);
//                            ddMap.put("periodamount", perioddepreciation);
//                            ddMap.put("accamount", jobj.optDouble("accdepreciation", 0));
//                            ddMap.put("netbookvalue", jobj.optDouble("netbookvalue", 0));
//                            // add depreciation detail
//                            accProductObj.addDepreciationDetail(ddMap);
//                        }
//                    }
//                    assetDetails.setOpeningDepreciation(openingBalance);
////                    if (assetDetails.getProduct().getDepreciationProvisionGLAccount() != null) {
////                        assetDetails.getProduct().getDepreciationProvisionGLAccount().setOpeningBalance(assetDetails.getProduct().getDepreciationProvisionGLAccount().getOpeningBalance() - openingBalance);
////                        String auditMsg = " udated the Opening Balance Of Account  " + assetDetails.getProduct().getDepreciationProvisionGLAccount().getAccountName() + ((!StringUtil.isNullOrEmpty(assetDetails.getProduct().getDepreciationProvisionGLAccount().getAcccode())) ? "(" + assetDetails.getProduct().getDepreciationProvisionGLAccount().getAcccode() + ")" : "") + " By " + openingBalance + " from the Opening Depreation of Asset " + assetDetails.getAssetId();
////
////                        auditTrailObj.insertAuditLog(AuditAction.OPENING_BALANCE_UPDATED, "User " + sessionHandlerImpl.getUserFullName(request) + auditMsg, request, assetDetails.getId());
////                    }
//                }
//            }
//        } catch (ServiceException ex) {
//            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
//            throw new AccountingException("Error while processing data.");
//        }
//    }
//    
public void updateBatchDetailsForSO(HashMap<Integer, Object[]> grBatchdetalisMap, String productId,Inventory inventory,HttpServletRequest request, String documentId) throws JSONException, ParseException, SessionExpiredException, ServiceException, UnsupportedEncodingException {

        
      //  KwlReturnObject kmsg = null;
        DateFormat df = authHandler.getDateOnlyFormat(request);
        String companyid = sessionHandlerImpl.getCompanyid(request);
        String userid = sessionHandlerImpl.getUserid(request);
        boolean isEdit = StringUtil.isNullOrEmpty(request.getParameter("isEdit")) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));        
        boolean activateCRblockingWithoutStock = false;
        User user=null;
        KwlReturnObject extracap = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
        ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracap.getEntityList().get(0);
        activateCRblockingWithoutStock = extraCompanyPreferences.isActivateCRblockingWithoutStock();
        if (!StringUtil.isNullOrEmpty(userid)) {
            KwlReturnObject jeresult = accountingHandlerDAOobj.getObject(User.class.getName(), userid);
            user = (User) jeresult.getEntityList().get(0);
        }
        // if CRBlockingWithoutStock feature is activated then procceed further
        if (activateCRblockingWithoutStock) {

            // get Pending consignment requests 
            KwlReturnObject pendingReqList = accGoodsReceiptobj.getPendingConsignmentRequests(companyid,productId);

            if (pendingReqList != null && pendingReqList.isSuccessFlag() && pendingReqList.getRecordTotalCount() > 0) {

                List<SalesOrder> consReqList = (List<SalesOrder>) pendingReqList.getEntityList();

                /*
                 * this set is used to check whether serial is locked already or
                 * not.this has to be used bcoz somewhere in code sql query is
                 * used and somewhere hql is used (so hibernatetemplates session
                 * will be different) so changes made in Objects will not get
                 * reflected due to different hibernate session.So for this ,
                 * map is used to save locked serial until commit operation is
                 * performed.
                 */
                Set usedProductBatchSerialSet = new HashSet();

                // Sales Order for loop
                for (int i = 0; i < consReqList.size(); i++) {

                    SalesOrder so = consReqList.get(i);
                    String serialNames="",batchNames="",auditMessage = "",sonumber="";
                    sonumber=so.getSalesOrderNumber();
                    Set<SalesOrderDetail> rows = so.getRows();

                    //Sales Order Detail for loop
                    for (SalesOrderDetail soDetail : rows) {

                        try {
                            Product product = soDetail.getProduct();
                            HashMap<Integer, Object[]> BatchdetalisMap = new HashMap<Integer, Object[]>();
                            KwlReturnObject kmsg = null;
                            int batchcnt = 0;
                            int cnt = 0;
                            boolean isquantityNotavl = false;  //this flag is used to check whether serial batch quantity is avilabale 

                            //get products batch serial list that is available (ie. non-locked)
                            if (product.isIsBatchForProduct() && product.isIsSerialForProduct() && product.isIslocationforproduct() && product.isIswarehouseforproduct()) {
                                JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
                                kmsg = accCommonTablesDAO.getBatchSerialDetailsforProduct(productId,product.isIsSerialForProduct(), isEdit, paramJobj);
//                                kmsg = accCommonTablesDAO.getBatchSerialDetailsforProduct(productId,product.isIsSerialForProduct(), isEdit, request);
                                List<Object[]> batchList = kmsg.getEntityList();
                                for (Object[] ObjBatchrow : batchList) {
                                    BatchdetalisMap.put(cnt++, ObjBatchrow);
                                }
                            }

                            String sodetailsid = soDetail.getID();
                            double lockquantitydue = soDetail.getLockquantitydue();
                            int cntp = (int) lockquantitydue;

                            for (int j = 0; j < cntp; j++) {

                                for (int serialCnt = 0; serialCnt < cnt; serialCnt++) {

                                    Object[] objArr = BatchdetalisMap.get(serialCnt);

                                    if (objArr != null) {

                                        String serialId = objArr[0] != null ? (String) objArr[0] : "";
                                        String batchId = objArr[1] != null ? (String) objArr[1] : "";
                                        String batchname = objArr[2] != null ? (String) objArr[2] : "";
                                        String warehouse = objArr[10] != null ? (String) objArr[10] : "";
                                        String location = objArr[11] != null ? (String) objArr[11] : "";
                                        String serialname = objArr[12] != null ? (String) objArr[12] : "";

                                        Date mfgDateObj = null;
                                        Date expDateObj = null;

                                        String checkInSet = product.getID() + batchId + serialId;

                                        if (!usedProductBatchSerialSet.contains(checkInSet)) {

                                            if (objArr[3] != null) { //ie mfgdate is not null
                                                java.sql.Timestamp mfgdatets = (java.sql.Timestamp) objArr[3];
                                                mfgDateObj = new Date(mfgdatets.getTime());
                                            }
                                            if (objArr[4] != null) { //ie expdate is not null
                                                java.sql.Timestamp expdatets = (java.sql.Timestamp) objArr[4];
                                                expDateObj = new Date(expdatets.getTime());
                                            }
   
                                            if (!StringUtil.isNullOrEmpty(serialname)) {
                                                serialNames += "'" + serialname + "',";
                                            }

                                            if (!StringUtil.isNullOrEmpty(serialNames)) {
                                                serialNames = serialNames.substring(0, serialNames.length() - 1);
                                            }
                                            if (!StringUtil.isNullOrEmpty(batchname)) {
                                                batchNames += "'" + batchname + "',";
                                            }

                                            if (!StringUtil.isNullOrEmpty(batchNames)) {
                                                batchNames = batchNames.substring(0, batchNames.length() - 1);
                                            }

                                            if (!StringUtil.isNullOrEmpty(sodetailsid) && !StringUtil.isNullOrEmpty(batchId) && !StringUtil.isNullOrEmpty(serialId)) {
                                                HashMap<String, Object> documentMap = new HashMap<String, Object>();
                                                documentMap.put("quantity", "1");
                                                documentMap.put("documentid", sodetailsid);
                                                documentMap.put("transactiontype", "20");//This is SO Type Tranction   sales order moduleid

                                                if (mfgDateObj != null) {
                                                    documentMap.put("mfgdate", mfgDateObj);
                                                }
                                                if (expDateObj != null) {
                                                    documentMap.put("expdate", expDateObj);
                                                }
                                                documentMap.put("batchmapid", batchId);
                                                accCommonTablesDAO.saveBatchDocumentMapping(documentMap);

                                                HashMap<String, Object> batchUpdateQtyMap = new HashMap<String, Object>();
                                                batchUpdateQtyMap.put("id", batchId);
                                                batchUpdateQtyMap.put("lockquantity", "1");
                                                accCommonTablesDAO.saveBatchAmountDue(batchUpdateQtyMap);

                                                
                                                
                                                HashMap<String, Object> serialdocumentMap = new HashMap<String, Object>();
                                                serialdocumentMap.put("quantity", "1");
                                                serialdocumentMap.put("documentid", sodetailsid);

                                                if (mfgDateObj != null) {
                                                    serialdocumentMap.put("mfgdate", mfgDateObj);
                                                }
                                                if (expDateObj != null) {
                                                    serialdocumentMap.put("expdate", expDateObj);
                                                }
                                                serialdocumentMap.put("serialmapid", serialId);
                                                serialdocumentMap.put("transactiontype", "20");//This is so Type Tranction  
                                                
                                                HashMap<String, Object> requestParams = new HashMap<String, Object>();

                                                requestParams.put("companyid", companyid);
                                                if (!StringUtil.isNullOrEmpty(userid)) {
                                                    requestParams.put("requestorid",userid);
                                                }
                                                if (!StringUtil.isNullOrEmpty(warehouse)) {
                                                    requestParams.put("warehouse", warehouse);
                                                }
                                                if (!StringUtil.isNullOrEmpty(location)) {
                                                    requestParams.put("location", location);
                                                }

                                                //code to Apply Pending Approval Rule
                                                KwlReturnObject ruleResult = accMasterItemsDAOobj.CheckRuleForPendingApproval(requestParams);
                                                Iterator itr = ruleResult.getEntityList().iterator();
                                                Set<User> approverSet = null;
                                                boolean isRequestPending = false;
                                                while (itr.hasNext()) {
                                                    ConsignmentRequestApprovalRule approvalRule = (ConsignmentRequestApprovalRule) itr.next();
                                                    if (approvalRule != null) {
                                                        KwlReturnObject res = accGoodsReceiptobj.getConsignmentRequestApproverList(approvalRule.getID());
                                                        List<User> userlist = res.getEntityList();
                                                        Set<User> users = new HashSet<User>();;
                                                        for (User us : userlist) {
                                                            users.add(us);
                                                        }
                                                        approverSet = users;
                                                        isRequestPending = true;
                                                        break;
                                                    }
                                                }
                                                if (isRequestPending) {
                                                    serialdocumentMap.put("requestpendingapproval", RequestApprovalStatus.PENDING);
                                                    serialdocumentMap.put("approver", approverSet);
                                                }
                                                
                                                accCommonTablesDAO.saveSerialDocumentMapping(serialdocumentMap);

                                                HashMap<String, Object> serialUpdateQtyMap = new HashMap<String, Object>();
                                                serialUpdateQtyMap.put("lockquantity", "1");
                                                serialUpdateQtyMap.put("id", serialId);
                                                accCommonTablesDAO.saveSerialAmountDue(serialUpdateQtyMap);

                                                String setName = product.getID() + batchId + serialId;
                                                usedProductBatchSerialSet.add(setName);

                                                batchcnt += 1;
                                                break;
                                            }
                                        }

                                    } else {
                                        isquantityNotavl = true;  //if quantity is not available then break and come out of for loop
                                        break;
                                    }

                                }

                            }
                            accCommonTablesDAO.updateSOLockQuantitydue(sodetailsid, batchcnt, companyid);
                            if (isquantityNotavl) {
                                break;
                            }

                        } catch (Exception ex) {
                            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                    auditMessage = "User " + user.getFullName() + " has assigned Stock for Request No: " + sonumber + " With Batch: " + batchNames + " and With Serials: " + serialNames + ", " + auditMessage;
                    auditTrailObj.insertAuditLog(AuditAction.STOCK_AUTOASSIGNED, auditMessage, request, "0");
                }

            }

        }
        
    }

    public ModelAndView deleteGoodsReceiptOrders(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        StringBuffer productIds = new StringBuffer();
        boolean issuccess = false;
        boolean isConsignment = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        if (!StringUtil.isNullOrEmpty(request.getParameter("isConsignment"))) {
            isConsignment = Boolean.parseBoolean(request.getParameter("isConsignment"));
        }
        TransactionStatus status = txnManager.getTransaction(def);
        String linkedTransaction = "";
        try {
            List list = deleteGoodsReceiptOrders(request);
            if (list != null && list.size() > 0) {
                linkedTransaction = (String) list.get(0);
            }
            
            txnManager.commit(status);
            issuccess = true;
            if (StringUtil.isNullOrEmpty(linkedTransaction)) {
                 if (isConsignment) {
                    msg = messageSource.getMessage("acc.consignment.GR.del", null, RequestContextUtils.getLocale(request));   //"Delivery Order has been deleted successfully";
                } else {
                    msg = messageSource.getMessage("acc.gro.del", null, RequestContextUtils.getLocale(request));   //"Goods receipt has been deleted successfully";
                }
            } else {
                if (isConsignment) {
                    msg = messageSource.getMessage("acc.field.consGoodsreceiptsexcept", null, RequestContextUtils.getLocale(request)) + " " + linkedTransaction.substring(0, linkedTransaction.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
                } else {
                    //msg = messageSource.getMessage("acc.field.Goodsreceiptsexcept", null, RequestContextUtils.getLocale(request)) + " " + linkedTransaction.substring(0, linkedTransaction.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
                    msg = messageSource.getMessage("acc.field.Goodsreceiptsexcept", null, RequestContextUtils.getLocale(request)) + " " + linkedTransaction.substring(0, linkedTransaction.length() - 2) + " " + messageSource.getMessage("acc.field.Goodsreceiptsnotdeletedreason", null, RequestContextUtils.getLocale(request));
                }

            }
            if (list != null && list.size() > 0) {
                productIds = (StringBuffer) list.get(1);
            }
//            deleteGoodsReceiptOrders(request);
//            txnManager.commit(status);
//            issuccess = true;
//            msg = messageSource.getMessage("acc.gro.del", null, RequestContextUtils.getLocale(request));   //"Goods receipt has been deleted successfully";
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("productIds", productIds);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public List deleteGoodsReceiptOrders(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        String linkedTransaction = "";
        StringBuffer productIds = new StringBuffer();
        List resultList = new ArrayList();
        try {
            GoodsReceiptOrder GoodsReceiptOrder = null;
            String doid="",dono="";
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            boolean isFixedAsset = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) {
                isFixedAsset = Boolean.parseBoolean(request.getParameter("isFixedAsset"));
            }
            boolean isnegativestockforlocwar = false;
            ExtraCompanyPreferences extraCompanyPreferences = null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
            isnegativestockforlocwar = extraCompanyPreferences.isIsnegativestockforlocwar();
            String transactionDeleted="";
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("billid"))) {
                     doid =  StringUtil.DecodeText(jobj.getString("billid"));
                     
                    if (!StringUtil.isNullOrEmpty(jobj.optString("billno"))) {
                        dono =  StringUtil.DecodeText(jobj.getString("billno"));
                    }
                    
                    KwlReturnObject res = accountingHandlerDAOobj.getObject(GoodsReceiptOrder.class.getName(), doid);
                    GoodsReceiptOrder = (GoodsReceiptOrder) res.getEntityList().get(0);
                    HashMap<String, Object> requestParamsforbatchserial = new HashMap<String, Object>();
                    requestParamsforbatchserial.put("doid", doid);
                    requestParamsforbatchserial.put("companyid", companyid);
                    requestParamsforbatchserial.put("dono", dono);
                    requestParamsforbatchserial.put("isFixedAsset", GoodsReceiptOrder.isFixedAssetGRO());
                    requestParamsforbatchserial.put("isnegativestockforlocwar", isnegativestockforlocwar);
                    if (extraCompanyPreferences != null && (extraCompanyPreferences.isActivateMRPModule() || preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD)) { // delete JE temporary
                        KwlReturnObject result = accGoodsReceiptobj.getProductsFromGoodReceiptOrder(doid, companyid);
                        if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                            List productList = result.getEntityList();
                            if (productList != null && !productList.isEmpty()) {
                                for (Object object : productList) {
                                    String productid = (String) object;
                                    if (productIds.indexOf(productid) == -1) {
                                        productIds.append(productid).append(",");
                                    }
                                }
                            }
                        }
                    }
                     //DELETE ReceiptOrder Details  Term Map ... (deleteGRODetailsTermMap)
                    //This block should be execute only in case of permanent delete, so commenting this code.
                     if (extraCompanyPreferences.getLineLevelTermFlag()==1) {
                        boolean isDeleteReceiptOrderTermMap = accGoodsReceiptobj.deleteGRODetailsTermMap(doid);
                        if (!isDeleteReceiptOrderTermMap) {
                            throw new Exception("Goods Receipt Order details mapped with terms");
                        }
                    }
                    KwlReturnObject result = accGoodsReceiptobj.getGROFromPR(doid, companyid);
                    List list = result.getEntityList();
                    if (!list.isEmpty()) {
                        //throw new AccountingException("Selected record(s) is currently used in the Purchase Return(s). So it cannot be deleted.");
                        linkedTransaction += dono + ", ";
                        continue;
                    }
                    KwlReturnObject result1 = accGoodsReceiptobj.getGROFromInv(doid, companyid);
                    List list1 = result1.getEntityList();
                    if (!list1.isEmpty()) {
                        //throw new AccountingException("Selected record(s) is currently used in the Goods Receipt(s). So it cannot be deleted.");
                        linkedTransaction += dono + ", ";
                        continue;
                    }
                    KwlReturnObject result2 = accGoodsReceiptobj.getSerialNoUsedinDOFromGRO(doid, companyid);
                    List list2 = result2.getEntityList();
                    if (!list2.isEmpty()) {
                        linkedTransaction += dono + ", ";
                        continue;
                    }
                    if (!extraCompanyPreferences.isIsnegativestockforlocwar()) { //ERP-12639 : if [Activate Negative Stock For Location Warehouse] check is true then we ll not check whether the GR batch id is used in DO or not.
                        Set<GoodsReceiptOrderDetails> grdSet = GoodsReceiptOrder.getRows();
                        boolean checkBatchusedinDO = false;
                        for (GoodsReceiptOrderDetails grd : grdSet) {
                            if ((grd.getProduct().isIsBatchForProduct() && grd.getProduct().isIswarehouseforproduct() && grd.getProduct().isIslocationforproduct()) || !(grd.getProduct().isIsSerialForProduct())) {
                                KwlReturnObject resultqty = accGoodsReceiptobj.getAvailableQtyOfBatchUsedinDOFromGRO(doid, companyid);
                                List listQty = resultqty.getEntityList();
                                if (!listQty.isEmpty()) {
                                    checkBatchusedinDO = true;
                                    requestParamsforbatchserial.clear();
                                    break;
                                }
                            }else {
                                Double qty = grd.getProduct().getAvailableQuantity() - grd.getBaseuomdeliveredquantity();
                                if (qty < 0) {
                                    checkBatchusedinDO = true;
                                    requestParamsforbatchserial.clear();
                                    break;
                                }
                            }
                        }
                        if (checkBatchusedinDO) {
                        KwlReturnObject result3 = accGoodsReceiptobj.getbatchUsedinDOFromGRO(doid, companyid);
                        List list3 = result3.getEntityList();
                        if (!list3.isEmpty()) {
                            linkedTransaction += dono + ", ";
                            continue;
                        }
                    }
                    }
                    if (preferences.isInventoryAccountingIntegration() && preferences.isWithInvUpdate()) {
                        JSONArray productArray = new JSONArray();

                        String action = "17";
                        boolean isDirectUpdateInvFlag = false;
                        if (preferences.isUpdateInvLevel()) {
                            isDirectUpdateInvFlag = true;
                            action = "19";//Direct Inventory Update action
                        }


                        Set<GoodsReceiptOrderDetails> goodsReceiptOrderDetails = GoodsReceiptOrder.getRows();
                        for (GoodsReceiptOrderDetails goodsReceiptOrderDetail : goodsReceiptOrderDetails) {
                            JSONObject productObject = new JSONObject();
                            productObject.put("itemUomId", goodsReceiptOrderDetail.getInventory().getUom().getID());
                            productObject.put("itemBaseUomRate", goodsReceiptOrderDetail.getInventory().getBaseuomrate());
                            productObject.put("itemQuantity", goodsReceiptOrderDetail.getInventory().getBaseuomquantity() * (-1));
                            productObject.put("quantity", goodsReceiptOrderDetail.getInventory().getQuantity() * (-1));
                            //productObject.put("itemQuantity", goodsReceiptOrderDetail.getInventory().getQuantity()*(-1));
                            productObject.put("itemCode", goodsReceiptOrderDetail.getInventory().getProduct().getProductid());
                            if (isDirectUpdateInvFlag) {
                                productObject.put("storeid", goodsReceiptOrderDetail.getInvstoreid());
                                productObject.put("locationid", goodsReceiptOrderDetail.getInvlocid());
                            }
                            productArray.put(productObject);
                        }
                        if (productArray.length() > 0) {

                            String sendDateFormat = "yyyy-MM-dd";
                            DateFormat dateformat = new SimpleDateFormat(sendDateFormat);
                            Date date = GoodsReceiptOrder.getOrderDate();
                            String stringDate = dateformat.format(date);

                            JSONObject jSONObject = new JSONObject();
                            jSONObject.put("deliveryDate", stringDate);
                            jSONObject.put("dateFormat", sendDateFormat);
                            jSONObject.put("details", productArray);
                            jSONObject.put("orderNumber", GoodsReceiptOrder.getGoodsReceiptOrderNumber());
                            jSONObject.put("companyId", companyid);
                            jSONObject.put("purchasing", true);

                            String url = this.getServletContext().getInitParameter("inventoryURL");
                            CommonFnController cfc = new CommonFnController();
                            cfc.updateInventoryLevel(request, jSONObject, url, action);
                        }
                    }
                    String audtmsg = "";
                    if (isFixedAsset) {
                        audtmsg = " Asset ";
                    } else {
                        audtmsg = " ";   
                    }
                    // Check is GRO has Depreciated OR SoldAsset
                    if (GoodsReceiptOrder.isFixedAssetGRO()) {
                        boolean isGROhasDepreciatedAsset = accGoodsReceiptobj.isGROhasDepreciatedAsset(doid, companyid);

                        String exceptionMsg = "Goods Receipt Order " + dono + " has depreciated asset(s) so it cannot be deleted.";

                        // check GRO hase sold asset or not

                        boolean isGROhasSoldAsset = accGoodsReceiptobj.isGROhasSoldAsset(doid, companyid);

                        if (!isGROhasDepreciatedAsset) {
                            isGROhasDepreciatedAsset = isGROhasSoldAsset;
                        }


                        // Check GRO has Leased Asset

                        boolean isGROhasLeasedAsset = accGoodsReceiptobj.isGROhasLeasedAsset(doid, companyid);

                        if (!isGROhasDepreciatedAsset) {
                            isGROhasDepreciatedAsset = isGROhasLeasedAsset;
                        }

                        if (isGROhasSoldAsset) {
                            exceptionMsg = "Goods Receipt Order " + dono + " has sold asset(s) so it cannot be deleted.";
                        } else if (isGROhasLeasedAsset) {
                            exceptionMsg = "Goods Receipt Order " + dono + " has Leased asset(s) so it cannot be deleted.";
                        }


                        if (isGROhasDepreciatedAsset) {
                            throw new AccountingException(exceptionMsg);
                        }

                        HashMap<String, Object> requestParams = new HashMap<String, Object>();
                        requestParams.put("doid", doid);
                        requestParams.put("companyid", companyid);
                        accGoodsReceiptobj.deleteAssetDetailsLinkedWithGROrder(requestParams);
                    }
                    
                    KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(GoodsReceiptOrder.class.getName(), doid);
                    GoodsReceiptOrder dOrders = (GoodsReceiptOrder) rdresult.getEntityList().get(0);
                    Set<GoodsReceiptOrderDetails> groDetails = dOrders.getRows();
                    Iterator it = groDetails.iterator();
                    while (it.hasNext()) {
                        GoodsReceiptOrderDetails dodObj = (GoodsReceiptOrderDetails) it.next();
                        JSONObject json = new JSONObject();
                        json.put("grodid", dodObj.getID());
                        KwlReturnObject kwlReturnObject = stockService.getGRODetailISTMapping(json);
                        List<GRODetailISTMapping> groDetailIstMappings = kwlReturnObject.getEntityList();
                        for (GRODetailISTMapping groDetailIstMapping : groDetailIstMappings) {
                            /**
                             * Don't allow user to edit GRN, if GRN is
                             * approved/rejected from QC store.
                             */
                            if (groDetailIstMapping.getApprovedInterStoreTransferRequests() != null && !groDetailIstMapping.getApprovedInterStoreTransferRequests().isEmpty()) {
                                throw new AccountingException(messageSource.getMessage("acc.goodsreceiptnote.cannot.be.deleted.as.approvedOrRejected.byQAstore", new Object[]{"delete",dono, "approved"}, RequestContextUtils.getLocale(request)));
                            }
                            if (groDetailIstMapping.getRejectedInterStoreTransferRequests() != null && !groDetailIstMapping.getRejectedInterStoreTransferRequests().isEmpty()) {
                                throw new AccountingException(messageSource.getMessage("acc.goodsreceiptnote.cannot.be.deleted.as.approvedOrRejected.byQAstore", new Object[]{"delete",dono, "rejected"}, RequestContextUtils.getLocale(request)));
                            }
                            /**
                             * Delete inter store transfer request which is
                             * created to send GRN document for QC approval.
                             */

                            if (groDetailIstMapping.getInterStoreTransferRequest() != null) {
                                if (groDetailIstMapping.getInterStoreTransferRequest().getStatus() != InterStoreTransferStatus.INTRANSIT) {
                                    throw new AccountingException(messageSource.getMessage("acc.goodsreceiptnote.cannot.be.deleted.as.stockisaccepted.byQAstore", new Object[]{"delete",dono}, RequestContextUtils.getLocale(request)));
                                } else {
                                    istService.deleteISTRequest(groDetailIstMapping.getInterStoreTransferRequest());
                                }
                            }
                        }
                        accProductObj.deleteInventoryEntry(dodObj.getID(), companyid);
                    }
                    // update the purchase order balance quantity
                    if (!StringUtil.isNullObject(requestParamsforbatchserial) && requestParamsforbatchserial.size() > 0 && !requestParamsforbatchserial.isEmpty()) {
                        accGoodsReceiptobj.updatePOBalanceQtyAfterGR(doid, "", companyid);
                        stockMovementService.removeStockMovementByReferenceId(GoodsReceiptOrder.getCompany(), GoodsReceiptOrder.getID());
                        accGoodsReceiptobj.deleteGoodsReceiptOrdersBatchSerialDetails(requestParamsforbatchserial); //dlete serial no and mapping
                        if (extraCompanyPreferences != null && (extraCompanyPreferences.isActivateMRPModule() || preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD)) { // delete JE temporary
                            if (GoodsReceiptOrder != null && GoodsReceiptOrder.getInventoryJE() != null) {
                                result = accJournalEntryobj.deleteJournalEntry(GoodsReceiptOrder.getInventoryJE().getID(), companyid);
                            }
                        }
                    accGoodsReceiptobj.deleteGoodsReceiptOrder(doid, companyid);
                    auditTrailObj.insertAuditLog(AuditAction.GOODS_RECEIPT_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted"+ audtmsg +"Goods Receipt " + dono, request, doid);
                     transactionDeleted += GoodsReceiptOrder.getGoodsReceiptOrderNumber();
                    }
                }
               
            }
            if(StringUtil.isNullOrEmpty(transactionDeleted)){
                throw new AccountingException(messageSource.getMessage("acc.field.Goodsreceiptsquantitynotavailable", null, RequestContextUtils.getLocale(request)));
            }

            if (productIds.length() > 0) {
                productIds = new StringBuffer(productIds.substring(0, productIds.length() - 1));
            }
            resultList.add(0, linkedTransaction);
            resultList.add(1, productIds);
        } catch (AccountingException ex) {
            throw new AccountingException(ex.getMessage());
        } catch (InventoryException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)));
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)));
        }catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return resultList;
    }

    public ModelAndView claimBadDebtInvoices(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        
        try {
            claimBadDebtInvoices(request);

            issuccess = true;
            msg = messageSource.getMessage("acc.malaysiangst.invoiceIsClaimed", null, RequestContextUtils.getLocale(request));
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (AccountingException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void claimBadDebtInvoices(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException {
        TransactionStatus status = null;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String invliceIds = request.getParameter("billids");
            String claimedDateStr = request.getParameter("claimedDate");
            String claimedPeriodStr = request.getParameter("claimedPeriod");
            int claimedPeriod = 0;

            if (!StringUtil.isNullOrEmpty(claimedPeriodStr)) {
                claimedPeriod = Integer.parseInt(claimedPeriodStr);
            }


            Date claimedDate = authHandler.getDateOnlyFormat(request).parse(claimedDateStr);

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

            //ERP-10400, For Purchase
            String badDebtReleifPurchaseAccountId = extraCompanyPreferences.getGstBadDebtsReleifPurchaseAccount();
            accObj = accountingHandlerDAOobj.getObject(Account.class.getName(), badDebtReleifPurchaseAccountId);
            account = accObj != null?(Account) accObj.getEntityList().get(0):null;
            if (account == null) {
                throw new AccountingException(messageSource.getMessage("acc.gst.bad.debt.releif.purchaseaccountsexceptionMsg", null, RequestContextUtils.getLocale(request)));
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

            String badDebtRecoveredPurchaseAccountId = extraCompanyPreferences.getGstBadDebtsRecoverPurchaseAccount();
            accObj = accountingHandlerDAOobj.getObject(Account.class.getName(), badDebtRecoveredPurchaseAccountId);
            account = accObj != null?(Account) accObj.getEntityList().get(0):null;
            if (account == null) {
                throw new AccountingException(messageSource.getMessage("acc.gst.bad.debt.recover.purchaseaccountsexceptionMsg", null, RequestContextUtils.getLocale(request)));
            }
            String gstInputAccountId="";
            
            KwlReturnObject accountReturnObject = accAccountDAOobj.getAccountFromName(companyid, Constants.MALAYSIAN_GST_INPUT_TAX);
            List accountResultList = accountReturnObject.getEntityList();
            if (!accountResultList.isEmpty()) {
                gstInputAccountId = ((Account) accountResultList.get(0)).getID();
            }
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("JE_Tx");
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        
            for (int i = 0; i < jArr.length(); i++) {
                status = txnManager.getTransaction(def);
                JSONObject jobj = jArr.getJSONObject(i);

                String invoiceId = jobj.getString("billId");
                double invoiceReceivedAmt = jobj.optDouble("paidAmtAfterClaimed", 0);
                double gstToRecover = jobj.optDouble("gstToRecover", 0);

                KwlReturnObject invObj = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invoiceId);
                GoodsReceipt goodsReceipt = (GoodsReceipt) invObj.getEntityList().get(0);
                boolean isOpeningBalanceInvoice = goodsReceipt.isIsOpeningBalenceInvoice();
                // Calculating gstToRecover in invoice currency

                Map<String, Object> requestParams = AccountingManager.getGlobalParams(request);
                if (isOpeningBalanceInvoice) {
                    gstToRecover = (goodsReceipt.isConversionRateFromCurrencyToBase()) ? (gstToRecover / (goodsReceipt.getExchangeRateForOpeningTransaction())) : (gstToRecover * (goodsReceipt.getExchangeRateForOpeningTransaction()));
                } else {
                    String fromcurrencyid = goodsReceipt.getCurrency().getCurrencyID();
//                    KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, gstToRecover, fromcurrencyid, goodsReceipt.getJournalEntry().getEntryDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                    KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, gstToRecover, fromcurrencyid, goodsReceipt.getCreationDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
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
                String jeentryNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                String jeIntegerPart = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                String jeDatePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                String jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                String jeDateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                String jeSeqFormatId = format.getID();
                boolean jeautogenflag = true;

                HashMap<String, Object> BadDebtFormatParams = new HashMap<String, Object>();
                BadDebtFormatParams.put("moduleid", Constants.PURCHASE_BAD_DEBT_CLAIM_ModuleId);
                BadDebtFormatParams.put("modulename", "autopurchasebaddebtclaimid");
                BadDebtFormatParams.put("companyid", companyid);
                BadDebtFormatParams.put("isdefaultFormat", true);
                KwlReturnObject kwlbaddebtObj = accCompanyPreferencesObj.getSequenceFormat(BadDebtFormatParams);
                if (kwlbaddebtObj.getEntityList().size() == 0) {
                    throw new AccountingException("Sequence Format For Purchase Bad Debt Claim is not Set ");
                }
                SequenceFormat baddebtformat = (SequenceFormat) kwlbaddebtObj.getEntityList().get(0);
                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_BADDEBTPURCHASECLAIM, baddebtformat.getID(), false, claimedDate);
                String baddebtentryNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                int baddebtIntegerPart = Integer.parseInt((String)seqNumberMap.get(Constants.SEQNUMBER));
                String datePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                String dateafterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                String dateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
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
                jeDataMap.put("baddebtentryNumber", baddebtentryNumber);
                jeDataMap.put("memo", "Bad Debt Relief For " + (isOpeningBalanceInvoice ? "Opening " : "") + "Purchase Tax Invoice " + goodsReceipt.getGoodsReceiptNumber());
                jeDataMap.put("currencyid", goodsReceipt.getCurrency().getCurrencyID());
                if (isOpeningBalanceInvoice) {
                    jeDataMap.put("externalCurrencyRate", goodsReceipt.isConversionRateFromCurrencyToBase() ? (1 / goodsReceipt.getExchangeRateForOpeningTransaction()) : goodsReceipt.getExchangeRateForOpeningTransaction());
                }
                KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
                JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
                String jeid = journalEntry.getID();

                Set<JournalEntryDetail> jeDetails = new HashSet<JournalEntryDetail>();

                Set<GoodsReceiptDetail> invoiceDetails = goodsReceipt.getRows();

                double taxAmount = 0d;
                double invoiceTotalAmount = 0.0;
                double invoiceAmountExcludingTax = 0.0;
                double invoiceAmtDue = isOpeningBalanceInvoice ? goodsReceipt.getOpeningBalanceAmountDue() : goodsReceipt.getInvoiceamountdue();
                boolean isGlobalLevelTax = false;

                if (isOpeningBalanceInvoice) {
                    isGlobalLevelTax = true;
                    invoiceTotalAmount = goodsReceipt.getOriginalOpeningBalanceAmount();
                    taxAmount += ((goodsReceipt.getTaxamount()) * invoiceAmtDue) / invoiceTotalAmount;
                } else {
                    invoiceTotalAmount = goodsReceipt.getVendorEntry().getAmount();
                    if (goodsReceipt.getTaxEntry() != null && goodsReceipt.getTaxEntry().getAmount() > 0) {
                        isGlobalLevelTax = true;
                    }
                    if (isGlobalLevelTax) {
                        taxAmount += ((goodsReceipt.getTaxEntry().getAmount() * invoiceAmtDue) / invoiceTotalAmount);
                    } else {
                        for (GoodsReceiptDetail detail : invoiceDetails) {
                            taxAmount += (detail.getRowTaxAmount() * invoiceAmtDue) / invoiceTotalAmount;
                        }
                    }
                }
                invoiceTotalAmount = authHandler.round(invoiceTotalAmount, companyid);
                taxAmount = authHandler.round(taxAmount, companyid);
                invoiceAmountExcludingTax = invoiceAmtDue - taxAmount;
                invoiceAmountExcludingTax = authHandler.round(invoiceAmountExcludingTax, companyid);

                // Credit to Bad debt claim account
                JSONObject jedjson = new JSONObject();
                jedjson.put("srno", jeDetails.size() + 1);
                jedjson.put("companyid", companyid);
                jedjson.put("amount", invoiceAmountExcludingTax);
                jedjson.put("accountid", badDebtReleifPurchaseAccountId);
                jedjson.put("debit", false);
                jedjson.put("jeid", jeid);
                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jeDetails.add(jed);

                // Debit to invoice account
                jedjson = new JSONObject();
                jedjson.put("srno", jeDetails.size() + 1);
                jedjson.put("companyid", companyid);
                jedjson.put("amount", invoiceAmtDue);
                jedjson.put("accountid", (goodsReceipt.getAccount() != null) ? goodsReceipt.getAccount().getID() : goodsReceipt.getVendor().getID());
                jedjson.put("debit", true);
                jedjson.put("jeid", jeid);
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jeDetails.add(jed);

                // credit to tax account
                if (isGlobalLevelTax) {
                    String accountIdForTax = "";
                    if (isOpeningBalanceInvoice) {
                        accountIdForTax = gstInputAccountId;
                    } else {
                        accountIdForTax = goodsReceipt.getTax().getAccount().getID();
                    }
                    jedjson = new JSONObject();
                    jedjson.put("srno", jeDetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", taxAmount);
                    jedjson.put("accountid", accountIdForTax);
                    jedjson.put("debit", false);
                    jedjson.put("jeid", jeid);
                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jeDetails.add(jed);
                } else {
                    Tax taxObj = new Tax();
                    for (GoodsReceiptDetail detail : invoiceDetails) {
                        taxObj = detail.getTax();
                        if (taxObj != null) {
                            jedjson = new JSONObject();
                            jedjson.put("srno", jeDetails.size() + 1);
                            jedjson.put("companyid", companyid);
                            jedjson.put("amount", authHandler.round((detail.getRowTaxAmount() * invoiceAmtDue) / invoiceTotalAmount, companyid));
                            jedjson.put("accountid", taxObj.getAccount().getID());
                            jedjson.put("debit", false);
                            jedjson.put("jeid", jeid);
                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jeDetails.add(jed);
                        }
                    }
                }

                Map<String, Object> dataMap = new HashMap<String, Object>();
                dataMap.put(GRID, invoiceId);
                dataMap.put("companyid", companyid);
                dataMap.put("claimedDate", claimedDate);
                dataMap.put("claimedPeriod", claimedPeriod);
                dataMap.put("badDebtType", 1);
                dataMap.put(Constants.claimAmountDue, invoiceAmtDue);
                dataMap.put("amountduedate", claimedDate);
                if (isOpeningBalanceInvoice) {
                    dataMap.put("isOpeningBalenceInvoice", true);
                    dataMap.put("isNormalInvoice", false);
                    dataMap.put("openingBalanceAmountDue", 0.0);
                    dataMap.put(Constants.openingBalanceBaseAmountDue, 0.0);
                } else {
                    dataMap.put(Constants.invoiceamountdue, 0.0);
                    dataMap.put(Constants.invoiceamountdueinbase, 0.0);
                }
                dataMap.put(Constants.invoiceamountdueinbase, 0.0);
                dataMap.put("approvalstatuslevel", 11);
                KwlReturnObject result = accGoodsReceiptobj.addGoodsReceipt(dataMap);
//                for (GoodsReceiptDetail detail : invoiceDetails) {
//                    if (detail.getTax() != null && detail.getRowTaxAmount() > 0) {
//
//                        String taxId = detail.getTax().getID();
//
//                        KwlReturnObject taxObj = accTaxObj.getTaxPercent(companyid, goodsReceipt.getJournalEntry().getEntryDate(), taxId);
//
//                        double taxPer = (Double) taxObj.getEntityList().get(0);
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
////                        jedjson.put("accountid", badDebtReleifAccountId);
//                        jedjson.put("accountid", badDebtReleifPurchaseAccountId);//ERP-10400, For Purchase
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
//                if (goodsReceipt.getTaxEntry() != null && goodsReceipt.getTaxEntry().getAmount() > 0) {
//
//                    String taxId = goodsReceipt.getTax().getID();
//
//                    KwlReturnObject taxObj = accTaxObj.getTaxPercent(companyid, goodsReceipt.getJournalEntry().getEntryDate(), taxId);
//
//                    double taxPer = (Double) taxObj.getEntityList().get(0);
//
////                    double gstToRecover = invoiceReceivedAmt*taxPer/(100+taxPer);
//
//                    JSONObject jedjson = new JSONObject();
//                    jedjson.put("srno", jeDetails.size() + 1);
//                    jedjson.put("companyid", companyid);
//                    jedjson.put("amount", gstToRecover);//goodsReceipt.getTaxEntry().getAmount()-gstToRecover);
//                    jedjson.put("accountid", goodsReceipt.getTaxEntry().getAccount().getID());
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
//                    jedjson.put("amount", gstToRecover);//goodsReceipt.getTaxEntry().getAmount()-gstToRecover);
////                    jedjson.put("accountid", badDebtReleifAccountId);
//                    jedjson.put("accountid", badDebtReleifPurchaseAccountId);//ERP-10400, For Purchase
//                    jedjson.put("debit", false);
//                    jedjson.put("jeid", jeid);
//                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
//                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
//                    jeDetails.add(jed);
//
//                    taxAmount += gstToRecover;//(goodsReceipt.getTaxEntry().getAmount()-gstToRecover);
//                }
//
//                JournalEntryDetail centry = goodsReceipt.getVendorEntry();
//                double invoiceAmt = centry.getAmount();
//                double invoiceAmtDue = goodsReceipt.getInvoiceamountdue();
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
                mappingObj.put(Constants.DATEPREFIX, datePrefix);
                mappingObj.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
                mappingObj.put(Constants.DATESUFFIX, dateSuffix);
                mappingObj.put("baddebtentryNumber", baddebtentryNumber);
                KwlReturnObject mapResult = accGoodsReceiptobj.saveBadDebtInvoiceMapping(mappingObj);

                txnManager.commit(status);
            }
        } catch (ParseException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ServiceException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
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
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (AccountingException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void unClaimBadDebtInvoices(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException {
        try {
            String invoiceids = request.getParameter("invoiceIds");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            if(!StringUtil.isNullOrEmpty(invoiceids)){
                String companyId = sessionHandlerImpl.getCompanyid(request);
                String[] invoices = invoiceids.split(",");
                String invoiceid="";
                HashMap<String,Object> map = new HashMap<>();
                map.put("companyid", companyId);    
                map.put("badDebtType", 0);    
                KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
                Company company = (Company)companyResult.getEntityList().get(0);
                HashMap<String, Object> requestParams = new HashMap();
                requestParams.put(Constants.companyid, companyId);
                requestParams.put("gcurrencyid", company.getCurrency().getCurrencyID());
                
                HashMap<String, Object> requestParamsForMapping = new HashMap();
                requestParamsForMapping.put(Constants.companyid, companyId);
                
                KwlReturnObject result = null;
                KwlReturnObject resultForBaseAmount = null;
                List<BadDebtPurchaseInvoiceMapping> list=null;
                BadDebtPurchaseInvoiceMapping mapping=null;
                double badDebtAmountClaimed=0;
                double badDebtAmountClaimedInBase=0;
                GoodsReceipt invoice=null;
                JournalEntry JE = null;
                boolean isOpeningBalanceInvoice=false;
                for(int i=0;i<invoices.length;i++){
                    map.put("invoiceid", invoices[i]);
                    result = accGoodsReceiptobj.getBadDebtPurchaseInvoiceMappingForGoodsReceipt(map);
                    list = result.getEntityList();
                    mapping = list.get(0);
                    badDebtAmountClaimed = mapping.getBadDebtAmtClaimed();
                    result = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invoices[i]);
                    invoice = (GoodsReceipt)result.getEntityList().get(0);
                    isOpeningBalanceInvoice = invoice.isIsOpeningBalenceInvoice();
                    if(isOpeningBalanceInvoice){
                        double amountdue = invoice.getOpeningBalanceAmountDue() ;
                        /*
                         set status flag for opening invoices
                         */
                        double amountdueforstatus = amountdue + badDebtAmountClaimed;
                        if (authHandler.round(amountdueforstatus, companyid) <= 0) {
                            invoice.setIsOpenPayment(false);
                        } else {
                            invoice.setIsOpenPayment(true);
                        }
                        invoice.setOpeningBalanceAmountDue(invoice.getOpeningBalanceAmountDue()+badDebtAmountClaimed);
                    } else {
                        /*
                         set status flag for amount due 
                         */
                        double amountdueforstatus = invoice.getInvoiceamountdue() + badDebtAmountClaimed;
                        if (authHandler.round(amountdueforstatus, companyid) <= 0) {
                            invoice.setIsOpenPayment(false);
                        } else {
                            invoice.setIsOpenPayment(true);
                        }
                       invoice.setInvoiceamountdue(invoice.getInvoiceamountdue()+badDebtAmountClaimed);     
                    }
                    invoice.setAmountDueDate(null);
                    invoice.setBadDebtType(0);
                    invoice.setDebtClaimedDate(null);
                    invoice.setClaimAmountDue(0.0);
                    resultForBaseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, badDebtAmountClaimed, invoice.getCurrency().getCurrencyID(), invoice.getCreationDate(), invoice.getExternalCurrencyRate());
                    badDebtAmountClaimedInBase = (double) resultForBaseAmount.getEntityList().get(0);
                    if(isOpeningBalanceInvoice){
                        invoice.setOpeningBalanceBaseAmountDue(invoice.getOpeningBalanceBaseAmountDue()+badDebtAmountClaimedInBase);
                    } else { 
                        invoice.setInvoiceAmountDueInBase(badDebtAmountClaimedInBase);
                    }    
                    JE = mapping.getJournalEntry();
                    requestParamsForMapping.put("id", mapping.getId());
                    accGoodsReceiptobj.deleteBadDebtPurchaseInvoiceMapping(requestParamsForMapping);
                    result = accJournalEntryobj.deleteJEDtails(JE.getID(), companyId);
                    result = accJournalEntryobj.deleteJE(JE.getID(), companyId);
                }
            }
        }catch (ServiceException ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
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
            String recoverInvoice=recoverBadDebtInvoices(request);

            issuccess = true;
            msg = "Purchase Invoices has been Recovered successfully"+recoverInvoice;//messageSource.getMessage("acc.agedPay.inv", null, RequestContextUtils.getLocale(request)) + " " + invoiceNumber + " " + messageSource.getMessage("acc.field.hasbeen", null, RequestContextUtils.getLocale(request)) + " " + succMsg + " " + messageSource.getMessage("acc.field.successfully", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (AccountingException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public String recoverBadDebtInvoices(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException {
       String invoiceno="";
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

            String badDebtReleifPurchaseAccountId = extraCompanyPreferences.getGstBadDebtsReleifPurchaseAccount();
            accObj = accountingHandlerDAOobj.getObject(Account.class.getName(), badDebtReleifPurchaseAccountId);
            account = accObj != null?(Account) accObj.getEntityList().get(0):null;
            if (account == null) {
                throw new AccountingException(messageSource.getMessage("acc.gst.bad.debt.releif.purchaseaccountsexceptionMsg", null, RequestContextUtils.getLocale(request)));
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

            String badDebtRecoveredPurchaseAccountId = extraCompanyPreferences.getGstBadDebtsRecoverPurchaseAccount();
            accObj = accountingHandlerDAOobj.getObject(Account.class.getName(), badDebtRecoveredPurchaseAccountId);
            account = accObj != null?(Account) accObj.getEntityList().get(0):null;
            if (account == null) {
                throw new AccountingException(messageSource.getMessage("acc.gst.bad.debt.recover.purchaseaccountsexceptionMsg", null, RequestContextUtils.getLocale(request)));
            }


            for (int i = 0; i < jArr.length(); i++) {

                JSONObject jobj = jArr.getJSONObject(i);

                String invoiceId = jobj.getString("billId");
                double invoiceReceivedAmt = jobj.optDouble("paidAmtAfterClaimed", 0);
                double gstToRecover = jobj.optDouble("gstToRecover", 0);

                KwlReturnObject invObj = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invoiceId);
                GoodsReceipt goodsReceipt = (GoodsReceipt) invObj.getEntityList().get(0);
                if (invoiceReceivedAmt == 0 || gstToRecover == 0) {// if payment is not made for selected invoice then no need to run recovery process
                   invoiceno+=goodsReceipt.getGoodsReceiptNumber()+",";
                    continue;
                }


                // Calculating gstToRecover in invoice currency

                Map<String, Object> requestParams = AccountingManager.getGlobalParams(request);

                String fromcurrencyid = goodsReceipt.getCurrency().getCurrencyID();
//                KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, gstToRecover, fromcurrencyid, goodsReceipt.getJournalEntry().getEntryDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, gstToRecover, fromcurrencyid, goodsReceipt.getCreationDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                gstToRecover = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);

//                bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, invoiceReceivedAmt, fromcurrencyid, goodsReceipt.getJournalEntry().getEntryDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, invoiceReceivedAmt, fromcurrencyid, goodsReceipt.getCreationDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                invoiceReceivedAmt = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);

                Map<String, Object> dataMap = new HashMap<String, Object>();
                dataMap.put(GRID, invoiceId);
                dataMap.put("companyid", companyid);
//                dataMap.put("recoveredDate", recoveredDate);
                dataMap.put("badDebtType", 2);

                dataMap.put(Constants.Checklocktransactiondate, request.getParameter("billdate"));
                KwlReturnObject result = accGoodsReceiptobj.addGoodsReceipt(dataMap);


                HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                JEFormatParams.put("modulename", "autojournalentry");
                JEFormatParams.put("companyid", companyid);
                JEFormatParams.put("isdefaultFormat", true);

                KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, recoveredDate);
                String jeentryNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                String jeIntegerPart = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                String jeDatePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                String jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                String jeDateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                String jeSeqFormatId = format.getID();
                boolean jeautogenflag = true;
                
                HashMap<String, Object> BadDebtFormatParams = new HashMap<String, Object>();
                BadDebtFormatParams.put("moduleid", Constants.PURCHASE_BAD_DEBT_RECOVER_ModuleId);
                BadDebtFormatParams.put("modulename", "autopurchasebaddebtrecoverid");
                BadDebtFormatParams.put("companyid", companyid);
                BadDebtFormatParams.put("isdefaultFormat", true);
                KwlReturnObject kwlbaddebtObj = accCompanyPreferencesObj.getSequenceFormat(BadDebtFormatParams);
                if (kwlbaddebtObj.getEntityList().size() == 0) {
                    throw new AccountingException("Sequence Format For Purchase Bad Debt Recover is not Set ");
                }
                SequenceFormat baddebtformat = (SequenceFormat) kwlbaddebtObj.getEntityList().get(0);
                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_BADDEBTPURCHASERECOVER, baddebtformat.getID(), false, recoveredDate);
                String baddebtentryNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                int baddebtIntegerPart = Integer.parseInt((String)seqNumberMap.get(Constants.SEQNUMBER));
                String datePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                String dateafterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                String dateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
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
                jeDataMap.put("baddebtentryNumber",baddebtentryNumber);
                jeDataMap.put("memo", "Bad Debt Recovered For Tax Purchase Invoice " + goodsReceipt.getGoodsReceiptNumber());
                jeDataMap.put("currencyid", goodsReceipt.getCurrency().getCurrencyID());
                KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
                JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
                String jeid = journalEntry.getID();

                Set<JournalEntryDetail> jeDetails = new HashSet<JournalEntryDetail>();

                Set<GoodsReceiptDetail> invoiceDetails = goodsReceipt.getRows();

                double taxAmount = 0d;

                for (GoodsReceiptDetail detail : invoiceDetails) {
                    if (detail.getTax() != null && detail.getRowTaxAmount() > 0) {

                        String taxId = detail.getTax().getID();

//                        KwlReturnObject taxObj = accTaxObj.getTaxPercent(companyid, goodsReceipt.getJournalEntry().getEntryDate(), taxId);
                        KwlReturnObject taxObj = accTaxObj.getTaxPercent(companyid, goodsReceipt.getCreationDate(), taxId);

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
//                        jedjson.put("accountid", badDebtRecoveredAccountId);
                        jedjson.put("accountid", badDebtRecoveredPurchaseAccountId);//ERP-10400, For Purchase
                        jedjson.put("debit", true);
                        jedjson.put("jeid", jeid);
                        jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jeDetails.add(jed);

                        taxAmount += gstToRecover;//(detail.getRowTaxAmount()-gstToRecover);
                    }
                }

                if (goodsReceipt.getTaxEntry() != null && goodsReceipt.getTaxEntry().getAmount() > 0) {

                    String taxId = goodsReceipt.getTax().getID();

//                    KwlReturnObject taxObj = accTaxObj.getTaxPercent(companyid, goodsReceipt.getJournalEntry().getEntryDate(), taxId);
                    KwlReturnObject taxObj = accTaxObj.getTaxPercent(companyid, goodsReceipt.getCreationDate(), taxId);

                    double taxPer = (Double) taxObj.getEntityList().get(0);


//                    double gstToRecover = invoiceReceivedAmt*taxPer/(100+taxPer);

                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", jeDetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", gstToRecover);//goodsReceipt.getTaxEntry().getAmount()-gstToRecover);
                    jedjson.put("accountid", goodsReceipt.getTaxEntry().getAccount().getID());
                    jedjson.put("debit", false);
                    jedjson.put("jeid", jeid);
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jeDetails.add(jed);


                    jedjson = new JSONObject();
                    jedjson.put("srno", jeDetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", gstToRecover);//goodsReceipt.getTaxEntry().getAmount()-gstToRecover);
//                    jedjson.put("accountid", badDebtRecoveredAccountId);
                    jedjson.put("accountid", badDebtRecoveredPurchaseAccountId); //ERP-10400, For Purchase
                    jedjson.put("debit", true);
                    jedjson.put("jeid", jeid);
                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jeDetails.add(jed);

                    taxAmount += gstToRecover;//(goodsReceipt.getTaxEntry().getAmount()-gstToRecover);
                }

                double invoiceAmtDue = goodsReceipt.getInvoiceamountdue();

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
                mappingObj.put("gstToRecover", gstToRecover);
                mappingObj.put("recoveredDate", recoveredDate);
                mappingObj.put("badDebtType", 1);
                mappingObj.put("autoGenerated", true);
                mappingObj.put("seqformat", baddebtSeqFormatId);
                mappingObj.put("seqnumber", baddebtIntegerPart);
                mappingObj.put(Constants.DATEPREFIX, datePrefix);
                mappingObj.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
                mappingObj.put(Constants.DATESUFFIX, dateSuffix);
                mappingObj.put("baddebtentryNumber", baddebtentryNumber);
                KwlReturnObject mapResult = accGoodsReceiptobj.saveBadDebtInvoiceMapping(mappingObj);

            }
        } catch (ParseException ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        if(invoiceno.length()>0){
            invoiceno=" Except "+invoiceno.substring(0, invoiceno.length()-1);
        }
        return invoiceno;
    }

    public ModelAndView deleteGoodsReceiptOrdersPermanent(HttpServletRequest request, HttpServletResponse response) throws JSONException, SessionExpiredException {
        JSONObject responsejobj = new JSONObject();
        String url = this.getServletContext().getInitParameter("inventoryURL");
        JSONObject nObject = StringUtil.convertRequestToJsonObject(request);
        nObject.put(Constants.inventoryURL, url);
        responsejobj = accGoodsReceiptModuleService.deleteGoodsReceiptOrdersPermanentJSON(nObject);
        return new ModelAndView("jsonView", "model", responsejobj.toString());
    }
    
    public Set<AssetInvoiceDetailMapping> saveAssetInvoiceDetailMapping(String invoiceDetailId, Set<AssetDetails> assetDetailsSet, String companyId, int moduleId) throws AccountingException {
        Set<AssetInvoiceDetailMapping> assetInvoiceDetailMappings = new HashSet<AssetInvoiceDetailMapping>();
        try {
            for (AssetDetails assetDetails : assetDetailsSet) {
                HashMap<String, Object> dataMap = new HashMap<String, Object>();
                dataMap.put("invoiceDetail", invoiceDetailId);
                dataMap.put("moduleId", moduleId);
                dataMap.put("assetDetails", assetDetails.getId());
                dataMap.put("company", companyId);
                KwlReturnObject object = accProductObj.saveAssetInvoiceDetailMapping(dataMap);

                AssetInvoiceDetailMapping detailMapping = (AssetInvoiceDetailMapping) object.getEntityList().get(0);
                assetInvoiceDetailMappings.add(detailMapping);
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw new AccountingException("Error while processing data.");
        }
        return assetInvoiceDetailMappings;
    }

    public ModelAndView saveGROStatus(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("BR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            saveGROStatus(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.field.Statusupdatedsuccessfully", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void saveGROStatus(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        String doId = request.getParameter("dId");
        String status = request.getParameter("status");
        accGoodsReceiptobj.saveGoodsReceiptOrderStatus(doId, status);

    }

    public ModelAndView saveBillingGoodsReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            List li = saveBillingGoodsReceipt(request);
            issuccess = true;
            boolean pendingApprovalFlag = false;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            boolean inCash = Boolean.parseBoolean(request.getParameter(INCASH));
            String[] id = (String[]) li.get(0);
            ArrayList discountArr = (ArrayList) li.get(1);
            String pendingstatus = (String) li.get(2);
            jobj.put(INVOICEID, id[0]);
            if (StringUtil.equal("Pending Approval", pendingstatus)) {
                pendingstatus = " but pending for Approval.";
                pendingApprovalFlag = true;
            } else {
                pendingstatus = ".";
            }
            int istemplate = 0;
            if (!StringUtil.isNullOrEmpty(request.getParameter("istemplate"))) {
                istemplate = Integer.parseInt(request.getParameter("istemplate"));
            }
            if (istemplate == 1) {
                msg = (inCash ? messageSource.getMessage("acc.field.PurchaseReceipt", null, RequestContextUtils.getLocale(request)) : messageSource.getMessage("acc.agedPay.venInv", null, RequestContextUtils.getLocale(request))) + messageSource.getMessage("acc.field.andTemplatehasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request)) + (pendingApprovalFlag ? (messageSource.getMessage("acc.field.but", null, RequestContextUtils.getLocale(request)) + (inCash ? messageSource.getMessage("acc.field.PurchaseReceipt", null, RequestContextUtils.getLocale(request)) : messageSource.getMessage("acc.agedPay.venInv", null, RequestContextUtils.getLocale(request))) + messageSource.getMessage("acc.field.ispendingforApproval", null, RequestContextUtils.getLocale(request))) : ".");
            } else if (istemplate == 2) {
                msg = (inCash ? messageSource.getMessage("acc.field.PurchaseReceipt", null, RequestContextUtils.getLocale(request)) : messageSource.getMessage("acc.agedPay.venInv", null, RequestContextUtils.getLocale(request))) + messageSource.getMessage("acc.field.Templatehasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request));
            } else {
                msg = (inCash ? messageSource.getMessage("acc.field.PurchaseReceipt", null, RequestContextUtils.getLocale(request)) : messageSource.getMessage("acc.agedPay.venInv", null, RequestContextUtils.getLocale(request))) + messageSource.getMessage("acc.field.hasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request)) + pendingstatus;
            }
            jobj.put("pendingApproval", pendingApprovalFlag);
            txnManager.commit(status);
            status = txnManager.getTransaction(def);
            accGoodsReceiptModuleService.deleteEditedGoodsReceiptJE(id[1], companyid);
            txnManager.commit(status);
            status = txnManager.getTransaction(def);
            accGoodsReceiptModuleService.deleteEditedGoodsReceiptDiscount(discountArr, companyid);
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public List saveBillingGoodsReceipt(HttpServletRequest request) throws AccountingException, ServiceException, SessionExpiredException {
        BillingGoodsReceipt billingGoodsReceipt = null;
        String id = null;
        List ll = new ArrayList();
        String jeentryNumber = null;
        ArrayList discountArr = new ArrayList();
        String oldjeid = null;
        try {
            int istemplate = request.getParameter("istemplate") != null ? Integer.parseInt(request.getParameter("istemplate")) : 0;
            KwlReturnObject result = null;
            KwlReturnObject templateResult = null;
            String jeid = null;
            String customfield = request.getParameter("customfield");
            String sequenceformat = request.getParameter("sequenceformat");
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            boolean jeautogenflag = false;
            int nocount;
//            Discount discount=null;
            double externalCurrencyRate = StringUtil.getDouble(request.getParameter(EXTERNALCURRENCYRATE));
            Map<String, Object> requestMap = new HashMap();
            DateFormat df = authHandler.getDateOnlyFormat(request);
            double discValue = 0.0;
            String taxid = null;
            taxid = request.getParameter(TAXID);

            String costCenterId = request.getParameter(CCConstants.REQ_costcenter);
            double taxamount = StringUtil.getDouble(request.getParameter(TAXAMOUNT));
            double shippingCharges = StringUtil.getDouble(request.getParameter("shipping"));
            String posttext = request.getParameter("posttext");
            double otherCharges = StringUtil.getDouble(request.getParameter("othercharges"));
            boolean inCash = Boolean.parseBoolean(request.getParameter(INCASH));
            String grid = request.getParameter(INVOICEID);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            if (!StringUtil.isNullOrEmpty(grid)) {
                KwlReturnObject grObj = accountingHandlerDAOobj.getObject(BillingGoodsReceipt.class.getName(), grid);
                billingGoodsReceipt = (BillingGoodsReceipt) grObj.getEntityList().get(0);
                oldjeid = billingGoodsReceipt.getJournalEntry().getID();
                jeautogenflag = billingGoodsReceipt.getJournalEntry().isAutoGenerated();
                ////deleting invoice row
                result = accGoodsReceiptobj.deleteBillingGoodsReceiptDetails(grid, companyid);

                ////Deleting all Invoice Detail discount
                ////Deleting all Invoice Detail discount
                result = accGoodsReceiptobj.getGRDetailsDiscount(grid);
                List<String> list = result.getEntityList();
//                Iterator itr = list.iterator();
//                while (itr.hasNext()) {
//                    String discountid = (String) itr.next();
                if (list != null && !list.isEmpty()) {
                    for (String discountid : list) {
                        discountArr.add(discountid);
                        //     accDiscountobj.deleteDiscount(discountid,companyid);
                    }
                }
                String discountid = (billingGoodsReceipt.getDiscount() == null ? null : billingGoodsReceipt.getDiscount().getID());
                billingGoodsReceipt.setDiscount(null);
                if (StringUtil.isNullOrEmpty(discountid)) {
                    discountArr.add(discountid);
                }


                ////Deleting Invoice Detail Journalentry Detail
                String nl = null;
                Map<String, Object> dataMap = new HashMap<String, Object>();

                dataMap.put(ID, grid);
                dataMap.put("posttext", posttext);
                dataMap.put(MARKED_FAVOURITE, request.getParameter(MARKED_FAVOURITE));
                dataMap.put("otherentryid", nl);
                dataMap.put(SHIPENTRYID, nl);
                dataMap.put(TAXID, nl);
                dataMap.put(TAXENTRYID, nl);
                dataMap.put(CUSTOMERENTRYID, nl);
                KwlReturnObject uresult = accGoodsReceiptobj.saveBillingGoodsReceipt(dataMap);
                billingGoodsReceipt = (BillingGoodsReceipt) uresult.getEntityList().get(0);
                jeentryNumber = billingGoodsReceipt.getJournalEntry().getEntryNumber();

                //Delete old entries and insert new entries again from optimized table in edit case.
                accJournalEntryobj.deleteOnEditAccountJEs_optimized(oldjeid);
                accGoodsReceiptModuleService.deleteJEDetailsCustomData(oldjeid);
            }


            KwlReturnObject coPref = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) coPref.getEntityList().get(0);

            KwlReturnObject coCurr = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) coCurr.getEntityList().get(0);

            KwlReturnObject CompObj = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) CompObj.getEntityList().get(0);

            KwlReturnObject vendorObj = accountingHandlerDAOobj.getObject(Vendor.class.getName(), request.getParameter(VENDOR));
            Vendor vendor = (Vendor) vendorObj.getEntityList().get(0);

//            CompanyAccountPreferences preferences=(CompanyAccountPreferences)session.get(CompanyAccountPreferences.class,AuthHandler.getCompanyid(request));
//            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
//            Company company=(Company)session.get(Company.class,AuthHandler.getCompanyid(request));


            String currencyid = (request.getParameter(CURRENCYID) == null ? currency.getCurrencyID() : request.getParameter(CURRENCYID));
            //BillingGoodsReceipt bgr=new BillingGoodsReceipt();

            String entryNumber = request.getParameter(NUMBER);
            if (StringUtil.isNullOrEmpty(grid)) {
                Map<String, Object> bgrMap = new HashMap();
                bgrMap.put("billingGoodsReceiptNumber", entryNumber);
                bgrMap.put(COMPANYID, sessionHandlerImpl.getCompanyid(request));
                KwlReturnObject accGrRc = accGoodsReceiptobj.getBillingGoodsReceipt(bgrMap);
                List bgrList = accGrRc.getEntityList();
                if (!bgrList.isEmpty() && istemplate != 2) {
                    if (inCash) {
                        throw new AccountingException(messageSource.getMessage("acc.field.Purchasereceiptnumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                    } else {
                        throw new AccountingException(messageSource.getMessage("acc.field.VendorInvoicenumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                    }
                }

//            String q="from BillingGoodsReceipt where billingGoodsReceiptNumber=? and company.companyID=?";
//            if(!HibernateUtil.executeQuery(session, q, new Object[]{entryNumber, AuthHandler.getCompanyid(request)}).isEmpty())
//                throw new AccountingException("Vendor Invoice number '"+entryNumber+"' already exists.<br>Save again with given auto number.<br>Or type other number");

//            bgr.setBillingGoodsReceiptNumber(entryNumber);
                int from = StaticValues.AUTONUM_BILLINGGOODSRECEIPT;
                if (inCash) {
                    from = StaticValues.AUTONUM_BILLINGCASHPURCHASE;
                }
                requestMap.put("billingGoodsReceiptNumber", entryNumber);
                boolean seqformat_oldflag = StringUtil.getBoolean(request.getParameter("seqformat_oldflag"));
                String nextAutoNo = "";
                String nextAutoNoInt = "";
                if (seqformat_oldflag) {
                    nextAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, from, sequenceformat);
                } else {
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, from, sequenceformat, seqformat_oldflag, new Date());
                    nextAutoNo = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    nextAutoNoInt = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                    requestMap.put(Constants.SEQFORMAT, sequenceformat);
                    requestMap.put(Constants.SEQNUMBER, nextAutoNoInt);
                }
                requestMap.put("autoGenerated", nextAutoNo.equals(entryNumber));
            }
            requestMap.put(ID, grid);
            requestMap.put("posttext", posttext);
            requestMap.put(MEMO, request.getParameter(MEMO));
//            bgr.setMemo(request.getParameter("memo"));
            requestMap.put("billFrom", request.getParameter(BILLTO));
//            bgr.setBillFrom(request.getParameter("billto"));
            requestMap.put(SHIPFROM, request.getParameter(SHIPADDRESS));
//            bgr.setShipFrom(request.getParameter("shipaddress"));
            requestMap.put(CURRENCYID, currencyid);
//            bgr.setCurrency((KWLCurrency)session.get(KWLCurrency.class,currencyid));
//            requestMap.put("shipDate", authHandler.getDateFormatter(request).parse(request.getParameter(SHIPDATE)));
            if (request.getParameter(SHIPDATE) != null && !StringUtil.isNullOrEmpty(request.getParameter(SHIPDATE))) {
                requestMap.put("shipDate", df.parse(request.getParameter(SHIPDATE)));
            }
//            bgr.setShipDate(AuthHandler.getDateFormatter(request).parse(request.getParameter("shipdate")));
            requestMap.put("dueDate", authHandler.getDateOnlyFormat(request).parse(request.getParameter(DUEDATE)));
            requestMap.put("shipvia", request.getParameter("shipvia"));
            requestMap.put("fob", request.getParameter("fob"));
//            bgr.setDueDate(AuthHandler.getDateFormatter(request).parse(request.getParameter("duedate")));
            requestMap.put(COMPANYID, companyid);
//            bgr.setCompany(company);
            requestMap.put(VENDORID, request.getParameter(VENDOR));
            requestMap.put(MARKED_FAVOURITE, request.getParameter(MARKED_FAVOURITE));
            requestMap.put("venbilladdress", request.getParameter("venbilladdress") == null ? "" : request.getParameter("venbilladdress"));
            requestMap.put("venshipaddress", request.getParameter("venshipaddress") == null ? "" : request.getParameter("venshipaddress"));
//            Vendor vendor=(Vendor)session.get(Vendor.class, request.getParameter("vendor"));
//            bgr.setVendor(vendor);

//            JournalEntry journalEntry=CompanyHandler.makeJournalEntry(session, company.getCompanyID(), AuthHandler.getDateFormatter(request).parse(request.getParameter("billdate")),
//            request.getParameter("memo"), "JE"+bgr.getBillingGoodsReceiptNumber(),currencyid, externalCurrencyRate, hs,request);
//            
            if (StringUtil.isNullOrEmpty(oldjeid)) {
                String nextJEAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_JOURNALENTRY);
                jeentryNumber = nextJEAutoNo;// + "/" + entryNumber;
                jeautogenflag = true;
            }
            Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
            jeDataMap.put(ENTRYNUMBER, jeentryNumber);
            jeDataMap.put(AUTOGENERATED, jeautogenflag);
            jeDataMap.put(ENTRYDATE, df.parse(request.getParameter(BILLDATE)));
            jeDataMap.put(COMPANYID, companyid);
            jeDataMap.put(MEMO, request.getParameter(MEMO));
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeDataMap.put(CURRENCYID, currencyid);
            jeDataMap.put(CCConstants.JSON_costcenterid, costCenterId);

            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            jeid = journalEntry.getID();
            requestMap.put("journalEntryid", jeid);
            jeDataMap.put(JEID, jeid);


            //      HashSet jedetails = new HashSet();
            Set<JournalEntryDetail> hs = new HashSet<JournalEntryDetail>();
//            double[] totals=saveBillingGoodsReceiptRows(session, request, bgr, company, hs);
            List li = saveBillingGoodsReceiptRows(request, jeid, company, hs, currency, externalCurrencyRate);
            double[] totals = (double[]) li.get(0);
            Set<BillingGoodsReceiptDetail> bgrdetails = (HashSet<BillingGoodsReceiptDetail>) li.get(1);

            double disc = StringUtil.getDouble(request.getParameter(DISCOUNT));
            //double disc=CompanyHandler.getDouble(request, "discount");
            if (disc > 0) {
                Map<String, Object> discountMap = new HashMap();
                discountMap.put(DISCOUNT, disc);
                discountMap.put(INPERCENT, (request.getParameter(PERDISCOUNT) == null ? false : Boolean.parseBoolean(request.getParameter(PERDISCOUNT))));
                discountMap.put(ORIGINALAMOUNT, totals[1] - totals[0] + totals[2]);
                discountMap.put(COMPANYID, companyid);
//                discount=new Discount();
//                discount.setDiscount(disc);
//                discount.setInPercent(Boolean.parseBoolean(request.getParameter("perdiscount")));
//                discount.setOriginalAmount(totals[1]-totals[0]);
//                discount.setCompany(company);
                KwlReturnObject disObjKwl = accDiscountobj.updateDiscount(discountMap);
                Discount disObj = (Discount) disObjKwl.getEntityList().get(0);
                requestMap.put(DISCOUNTID, disObj.getID());
//                bgr.setDiscount(discount);

//                session.save(discount);
                discValue = disObj.getDiscountValue();
            }
            discValue += totals[0];

            double totalInvAmount = totals[1] + shippingCharges + otherCharges - discValue + taxamount + totals[2];
            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, totalInvAmount, currencyid, df.parse(request.getParameter("billdate")), externalCurrencyRate);
            double totalInvAmountinBase = (Double) bAmt.getEntityList().get(0);


            ArrayList amountApprove = (accountingHandlerDAOobj.getApprovalFlagForAmount(totalInvAmountinBase, Constants.VENDOR_INVOICE_APPROVAL, Constants.TRANS_AMOUNT, companyid));
            int pendingApprovalFlag = (istemplate != 2) ? ((Boolean) amountApprove.get(0) ? 1 : 0) : 0;//No need of approval if transaction is saved as only template

            Map<String, Object> jeMap = new HashMap<String, Object>();
            jeMap.put(SRNO, hs.size() + 1);
//            JournalEntryDetail jed=new JournalEntryDetail();
            jeMap.put(COMPANYID, companyid);
//            jed.setCompany(company);
            jeMap.put(AMOUNT, totalInvAmount);
//            jed.setAmount(totals[1]+shippingCharges+otherCharges-discValue+taxamount);
            if (!inCash) {
                jeMap.put(ACCOUNTID, vendor.getAccount().getID());
//                jed.setAccount(vendor.getAccount());
            } else {
                jeMap.put(ACCOUNTID, preferences.getCashAccount().getID());
//                jed.setAccount(preferences.getCashAccount());
            }
            jeMap.put(DEBIT, false);
            jeMap.put(JEID, jeid);
//            jed.setDebit(false);
            KwlReturnObject jedresult = accJournalEntryobj.updateJournalEntryDetails(jeMap);
            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            hs.add(jed);
            requestMap.put("vendorEntryid", jed.getID());


            if (discValue > 0) {
                jeMap = new HashMap<String, Object>();
                jeMap.put(SRNO, hs.size() + 1);
//                jed=new JournalEntryDetail();
                jeMap.put(COMPANYID, companyid);
//                jed.setCompany(company);
                jeMap.put(AMOUNT, discValue);
//                jed.setAmount(discValue);
                jeMap.put(ACCOUNTID, preferences.getDiscountReceived().getID());
//                jed.setAccount(preferences.getDiscountReceived());
                jeMap.put(DEBIT, false);
//                jed.setDebit(false);
                jeMap.put(JEID, jeid);
                jedresult = accJournalEntryobj.updateJournalEntryDetails(jeMap);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                hs.add(jed);
//                hs.add(jed);
            }
            if (shippingCharges > 0) {
//                jed=new JournalEntryDetail();
                jeMap = new HashMap<String, Object>();
                jeMap.put(SRNO, hs.size() + 1);
                jeMap.put(COMPANYID, companyid);
//                jed.setCompany(company);
                jeMap.put(AMOUNT, shippingCharges);
//                jed.setAmount(shippingCharges);
                jeMap.put(ACCOUNTID, preferences.getShippingCharges().getID());
//                jed.setAccount(preferences.getShippingCharges());
                jeMap.put(DEBIT, true);
//                jed.setDebit(true);
                jeMap.put(JEID, jeid);
                jedresult = accJournalEntryobj.updateJournalEntryDetails(jeMap);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                hs.add(jed);
                requestMap.put("shipEntryid", jed.getID());
//                hs.add(jed);
//                bgr.setShipEntry(jed);
            }
//            if (otherCharges > 0) {
////                jed=new JournalEntryDetail();
//                jeMap = new HashMap<String, Object>();
//                jeMap.put(SRNO, hs.size() + 1);
//                jeMap.put(COMPANYID, companyid);
////                jed.setCompany(company);
//                jeMap.put(AMOUNT, otherCharges);
////                jed.setAmount(otherCharges);
//                jeMap.put(ACCOUNT, preferences.getOtherCharges().getID());
////                jed.setAccount(preferences.getOtherCharges());
//                jeMap.put(DEBIT, true);
////                jed.setDebit(true);
//                jeMap.put(JEID, jeid);
//                jedresult = accJournalEntryobj.updateJournalEntryDetails(jeMap);
//                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
//                hs.add(jed);
//                requestMap.put("otherEntryid", jed.getID());
////                hs.add(jed);
////                bgr.setOtherEntry(jed);
//            }
            if (taxid != null && !taxid.isEmpty()) {
                //Tax tax=  (Tax)session.get(Tax.class, taxid);
                result = accountingHandlerDAOobj.getObject(Tax.class.getName(), taxid);
                Tax tax = (Tax) result.getEntityList().get(0);
                if (tax == null) {
                    throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
                }
                requestMap.put(TAXID, taxid);

//                    bgr.setTax(tax);
                if (taxamount > 0) {
                    jeMap = new HashMap<String, Object>();
                    jeMap.put(SRNO, hs.size() + 1);
//                    jed=new JournalEntryDetail();
                    jeMap.put(COMPANYID, companyid);
//                    jed.setCompany(company);
                    jeMap.put(AMOUNT, taxamount);
//                    jed.setAmount(taxamount);
                    jeMap.put(ACCOUNTID, tax.getAccount().getID());
//                    jed.setAccount(tax.getAccount());
                    jeMap.put(DEBIT, true);
//                    jed.setDebit(true);
                    jeMap.put(JEID, jeid);
                    jedresult = accJournalEntryobj.updateJournalEntryDetails(jeMap);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    hs.add(jed);
                    requestMap.put("taxEntryid", jed.getID());

//                    hs.add(jed);
//                    bgr.setTaxEntry(jed);
                }
            }


            jeDataMap.put(JEDETAILS, hs);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeDataMap.put("pendingapproval", pendingApprovalFlag);
            jeDataMap.put("istemplate", istemplate);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);


            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                customrequestParams.put("modulerecid", journalEntry.getID());
                customrequestParams.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    jeDataMap.put("accjecustomdataref", journalEntry.getID());
                    jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);
                }
            }


//            JournalEntry journalEntry=CompanyHandler.makeJournalEntry(session, company.getCompanyID(), AuthHandler.getDateFormatter(request).parse(request.getParameter("billdate")),
//            request.getParameter("memo"), "JE"+bgr.getBillingGoodsReceiptNumber(),currencyid, externalCurrencyRate, hs,request);
//            bgr.setJournalEntry(journalEntry);

            String erdid = null;

            Date billDate = request.getParameter(BILLDATE) == null ? null : df.parse(request.getParameter(BILLDATE));

            KwlReturnObject ERresult = accCurrencyDAOobj.getExcDetailID(requestParams, currencyid, billDate, null);
            List ERlist = ERresult.getEntityList();
            if (!ERlist.isEmpty()) {
                Iterator itr = ERlist.iterator();
                ExchangeRateDetails erd = (ExchangeRateDetails) itr.next();
                erdid = erd.getID();
            }
            requestMap.put("exchangeRateDetailsid", erdid);

//            ExchangeRateDetails erd=CompanyHandler.getExcDetailID(session,request,currencyid,AuthHandler.getDateFormatter(request).parse(request.getParameter("billdate")),null);
//            bgr.setExchangeRateDetail(erd);

            requestMap.put("pendingapproval", pendingApprovalFlag);
            requestMap.put("istemplate", istemplate);
            requestMap.put("approvallevel", (Integer) amountApprove.get(1));
            result = accGoodsReceiptobj.saveBillingGoodsReceipt(requestMap);
            billingGoodsReceipt = (BillingGoodsReceipt) result.getEntityList().get(0);

            requestMap.put(ID, billingGoodsReceipt.getID());
//            Iterator itr = bgrdetails.iterator();
//            while(itr.hasNext()){
//                BillingGoodsReceiptDetail bgrd = (BillingGoodsReceiptDetail) itr.next();
            if (bgrdetails != null && !bgrdetails.isEmpty()) {
                for (BillingGoodsReceiptDetail bgrd : bgrdetails) {
                    bgrd.setBillingGoodsReceipt(billingGoodsReceipt);
                }
            }
            requestMap.put(ROWS, bgrdetails);

            result = accGoodsReceiptobj.saveBillingGoodsReceipt(requestMap);
            billingGoodsReceipt = (BillingGoodsReceipt) result.getEntityList().get(0);

            //Insert new entries again in optimized table.
            accJournalEntryobj.saveAccountJEs_optimized(jeid);

            //accJournalEntryobj.updateJETemplateCode(journalEntry, companyid); 
//            session.saveOrUpdate(bgr);
            id = billingGoodsReceipt.getID();

            ll.add(new String[]{id, oldjeid});
            ll.add(discountArr);
            ll.add((pendingApprovalFlag == 1) ? "Pending Approval" : "Approved");

            if (pendingApprovalFlag == 1) { //this for send approval email
                String[] emails = {};
                String invoiceNumber = billingGoodsReceipt.getBillingGoodsReceiptNumber();
                String userName = sessionHandlerImpl.getUserFullName(request);
                String moduleName = "Vendor Invoice";
                emails = accountingHandlerDAOobj.getApprovalUserList(request, moduleName, 1);
                               
                String fromEmailId = (!company.isEmailFromCompanyCreator()) ? Constants.ADMIN_EMAILID : authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                /**
                 * parameters required for sending mail
                 */
                Map<String, Object> mailParameters = new HashMap();
                mailParameters.put(Constants.companyid, companyid);
                mailParameters.put(Constants.prNumber, invoiceNumber);
                mailParameters.put(Constants.modulename, moduleName);
                mailParameters.put(Constants.fromName, userName);
                mailParameters.put(Constants.fromEmailID, fromEmailId);
                mailParameters.put(Constants.PAGE_URL, URLUtil.getPageURL(request, loginpageFull));
                mailParameters.put(Constants.emails, emails); 

                //String fromEmailId = "admin@deskera.com";
                accountingHandlerDAOobj.sendApprovalEmails(mailParameters);
            }
            //Save record as template
            if (!StringUtil.isNullOrEmpty(request.getParameter("templatename")) && (istemplate == 1 || istemplate == 2)) {
                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                String moduletemplateid = request.getParameter("moduletemplateid");
                hashMap.put("templatename", request.getParameter("templatename"));
                if (!StringUtil.isNullOrEmpty(moduletemplateid)) {
                    hashMap.put("moduletemplateid", request.getParameter("moduletemplateid"));
                }
                if (inCash) {
                    hashMap.put("moduleid", Constants.Acc_BillingCash_Purchase_ModuleId);
                } else {
                    hashMap.put("moduleid", Constants.Acc_Vendor_BillingInvoice_ModuleId);
                }
                hashMap.put("modulerecordid", billingGoodsReceipt.getID());
                hashMap.put("companyid", companyid);
                if(!StringUtil.isNullOrEmpty(request.getParameter("companyunitid"))){
                    hashMap.put("companyunitid", request.getParameter("companyunitid"));// Added Unit ID if it is present in request
                }
                /**
                 * checks the template name is already exist in create and edit template case
                 */
                templateResult = accountingHandlerDAOobj.getModuleTemplateForTemplatename(hashMap);
                nocount = templateResult.getRecordTotalCount();
                if (nocount > 0) {
                    throw new AccountingException(messageSource.getMessage("acc.tmp.templateNameAlreadyExists", null, RequestContextUtils.getLocale(request)));
                }
                accountingHandlerDAOobj.saveModuleTemplate(hashMap);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }


        return ll;
    }

    public List saveBillingGoodsReceiptRows(HttpServletRequest request, String jeid, Company company, Set<JournalEntryDetail> jeDetails, KWLCurrency currency, double externalCurrencyRate) throws JSONException, ServiceException, AccountingException, SessionExpiredException, ParseException {
//        HashSet rows = new HashSet();
//        double totaldiscount = 0;
//        double totalamount = 0;
        Set rows = new HashSet();
        double totaldiscount = 0, totalamount = 0, taxamount = 0;
        List ll = new ArrayList();
        JSONArray jArr = new JSONArray(request.getParameter(DETAIL));
        try {
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(COMPANYID, sessionHandlerImpl.getCompanyid(request));
            requestParams.put(GCURRENCYID, sessionHandlerImpl.getCurrencyID(request));
            requestParams.put(DATEFORMAT, authHandler.getDateOnlyFormat(request));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            for (int i = 0; i < jArr.length(); i++) {
//                JournalEntryDetail jed;
                JSONObject jobj = jArr.getJSONObject(i);
                BillingGoodsReceiptDetail row = new BillingGoodsReceiptDetail();
                String rowid = StringUtil.generateUUID();
                row.setID(rowid);
                row.setSrno(i + 1);
                row.setWasRowTaxFieldEditable(true);// this field has been added after making row tax field editable its value will be set always true from its implementation. i.e from 28-jan-2014.REASON -  when Row Tax Amount field was not editable then tax calculation was taking place according to tax percent, as selected From Tax combo in JS Side.
                //BillingPurchaseOrderDetail rd = (BillingPurchaseOrderDetail) session.get(BillingPurchaseOrderDetail.class, jobj.getString("rowid"));
                KwlReturnObject bpodresult = accountingHandlerDAOobj.getObject(BillingPurchaseOrderDetail.class.getName(), jobj.getString("rowid"));
                BillingPurchaseOrderDetail rd = (BillingPurchaseOrderDetail) bpodresult.getEntityList().get(0);

                row.setCompany(company);
                row.setPurchaseOrderDetail(rd);
//                row.setBillingGoodsReceipt(bgr);
                row.setRate(jobj.getDouble(RATE));

                row.setQuantity(jobj.getDouble(QUANTITY));
                row.setAmount(jobj.getDouble(CALAMOUNT));
                row.setProductDetail( StringUtil.DecodeText(jobj.optString("productdetail")));
                totalamount += (row.getRate() * row.getQuantity());
                Discount discount = null;
                double disc = jobj.getDouble(PRDISCOUNT);
                int rowdisc = jobj.getInt("discountispercent");
                if (disc != 0.0) {
                    Map<String, Object> discMap = new HashMap();
                    discMap.put(DISCOUNT, disc);
                    discMap.put(INPERCENT, (rowdisc == 1) ? true : false);
                    discMap.put(ORIGINALAMOUNT, row.getRate() * jobj.getDouble(QUANTITY));
                    discMap.put(COMPANYID, company.getCompanyID());
                    KwlReturnObject dscresult = accDiscountobj.updateDiscount(discMap);
                    discount = (Discount) dscresult.getEntityList().get(0);
                    row.setDiscount(discount);

//                    discount = new Discount();
//                    discount.setDiscount(disc);
//                    discount.setOriginalAmount(row.getRate() * row.getQuantity());
//                    discount.setInPercent(true);
//                    discount.setCompany(company);
//                    row.setDiscount(discount);
//                    session.save(discount);
                    totaldiscount += discount.getDiscountValue();
                }
                Map<String, Object> jeMap = new HashMap<String, Object>();
                jeMap.put(SRNO, jeDetails.size() + 1);
                jeMap.put(COMPANYID, companyid);
                jeMap.put(ACCOUNTID, jobj.getString("creditoraccount"));
                jeMap.put(AMOUNT, row.getRate() * jobj.getDouble(QUANTITY));
                jeMap.put(DEBIT, true);
                jeMap.put(JEID, jeid);
                KwlReturnObject jedresult1 = accJournalEntryobj.updateJournalEntryDetails(jeMap);
                JournalEntryDetail jed1 = (JournalEntryDetail) jedresult1.getEntityList().get(0);
                jeDetails.add(jed1);
                row.setDebtorEntry(jed1);

                // Add Custom column details 
                if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", ""))) {
                    JSONArray jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
                    HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                    customrequestParams.put("customarray", jcustomarray);
                    customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                    customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);
                    customrequestParams.put("modulerecid", jed1.getID());
                    customrequestParams.put("recdetailId", rowid);
                    customrequestParams.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);
                    customrequestParams.put("companyid", companyid);
                    customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                    KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        jeMap.put("accjedetailcustomdata", jed1.getID());
                        jeMap.put("jedid", jed1.getID());
                        jedresult1 = accJournalEntryobj.updateJournalEntryDetails(jeMap);
                    }
                }

                String rowtaxid = "";
                if (!StringUtil.isNullOrEmpty(jobj.optString(PRTAXID, null)) && jobj.optString(PRTAXID).equalsIgnoreCase("None")) {
                    rowtaxid = null;    
                } else {
                    rowtaxid = jobj.optString(PRTAXID,null);
                }
                if (!StringUtil.isNullOrEmpty(rowtaxid)) {
                    boolean taxExist = false;
                    KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), rowtaxid); // (Tax)session.get(Tax.class, taxid);
                    Tax rowtax = (Tax) txresult.getEntityList().get(0);
                    if (rowtax == null) {
                        throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
                    } else {
                        row.setTax(rowtax);
                        double rowtaxamount = StringUtil.getDouble(jobj.getString(TAXAMOUNT));
                        // KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, rowtaxamount, currencyid, df.parse(request.getParameter("billdate")), externalCurrencyRate);
                        // rowtaxamount= (Double) bAmt.getEntityList().get(0);
                        row.setRowTaxAmount(rowtaxamount);
                        taxamount += rowtaxamount;
                        if (taxamount > 0) {
//                            Iterator itr = jeDetails.iterator();
//                            while (itr.hasNext()) {
//                                jed = (JournalEntryDetail) itr.next();
                            if (jeDetails != null && !jeDetails.isEmpty()) {
                                for (JournalEntryDetail jed : jeDetails) {
                                    if (jed.getAccount() == rowtax.getAccount()) {
                                        //                                          jed.setAmount(jed.getAmount() + row.getRate() * inventory.getQuantity());
                                        JSONObject jedjson = new JSONObject();
                                        jedjson.put(JEDID, jed.getID());
                                        jedjson.put(AMOUNT, jed.getAmount() + rowtaxamount);
                                        KwlReturnObject jedresult = accJournalEntryobj.updateJournalEntryDetails(jedjson);
                                        jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                        taxExist = true;
                                        break;
                                    }
                                }
                            }
                            if (!taxExist) {
                                JSONObject jedjson = new JSONObject();
                                jedjson = new JSONObject();
                                jedjson.put(SRNO, jeDetails.size() + 1);
                                jedjson.put(COMPANYID, company.getCompanyID());
                                jedjson.put(AMOUNT, rowtaxamount);
                                jedjson.put(ACCOUNTID, rowtax.getAccount().getID());
                                jedjson.put(DEBIT, true);
                                jedjson.put(JEID, jeid);
                                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                jeDetails.add(jed);

                            }
                        }
                    }
                }
                rows.add(row);
            }
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            ll.add(new double[]{totaldiscount, totalamount, taxamount});
            ll.add(rows);
        }
        //bgr.setRows(rows);
        return ll;
    }

    public ModelAndView updateGoodsReceiptOrderFavourite(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("R_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
        TransactionStatus status = txnManager.getTransaction(def);

        try {
            KwlReturnObject result = null;
            HashMap<String, Object> doDataMap = new HashMap<String, Object>();
            String id = request.getParameter("id");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            doDataMap.put("companyid", companyid);
            doDataMap.put("orderdate", new Date(request.getParameter("date")));
            doDataMap.put("id", id);
            doDataMap.put("isfavourite", request.getParameter("isfavourite"));
            if (!StringUtil.isNullOrEmpty(id)) {
                result = accGoodsReceiptobj.saveGoodsReceiptOrder(doDataMap);
            }
            msg = "set successfully.";
            issuccess = true;
            txnManager.commit(status);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView updateGoodsReceiptOrderPrint(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("R_Tx");
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
                    result = accGoodsReceiptobj.saveGoodsReceiptOrder(doDataMap);
                }
            }
            msg = messageSource.getMessage("acc.field.setsuccessfully", null, RequestContextUtils.getLocale(request));
            issuccess = true;
            txnManager.commit(status);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView updateFavourite(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("R_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);



        TransactionStatus status = txnManager.getTransaction(def);

        try {
            KwlReturnObject result = null;
            HashMap<String, Object> greceipthm = new HashMap<String, Object>();
            boolean withInventory = Boolean.parseBoolean(request.getParameter("withInv"));
            boolean rfqflag = false;
            if (request.getParameter("rfqflag") != null) {
                rfqflag = Boolean.parseBoolean(request.getParameter("rfqflag"));
            }
            String invoiceid = request.getParameter("invoiceid");
            greceipthm.put(GRID, invoiceid);
            greceipthm.put(ID, invoiceid);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            greceipthm.put("companyid", companyid);
            greceipthm.put("orderdate", new Date(request.getParameter("date")));
            greceipthm.put("isfavourite", request.getParameter("isfavourite"));
            if (!StringUtil.isNullOrEmpty(invoiceid)) {
                if (withInventory) {
                    result = accGoodsReceiptobj.saveBillingGoodsReceipt(greceipthm);
                } else if (rfqflag) {
                    result = accPurchaseOrderobj.saveRFQ(greceipthm);
                } else {
                    result = accGoodsReceiptobj.updateGoodsReceipt(greceipthm);
                }
            }
            msg = messageSource.getMessage("acc.field.setsuccessfully", null, RequestContextUtils.getLocale(request));
            issuccess = true;
            txnManager.commit(status);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView updatePrint(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("R_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);



        TransactionStatus status = txnManager.getTransaction(def);

        try {
            KwlReturnObject result = null;
            boolean withInventory = Boolean.parseBoolean(request.getParameter("withInv"));
            String recordids = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("recordids"))) {
                recordids = request.getParameter("recordids");
            }
            ArrayList<String> SOIDList = CustomDesignHandler.getSelectedBillIDs(recordids);
            Map<String, Object> greceipthm = new HashMap<String, Object>();
            for (int cnt = 0; cnt < SOIDList.size(); cnt++) {
                greceipthm.put(GRID, SOIDList.get(cnt));
                greceipthm.put("isprinted", request.getParameter("isprinted"));
                if (!StringUtil.isNullOrEmpty(SOIDList.get(cnt))) {
                    if (withInventory) {
                        result = accGoodsReceiptobj.saveBillingGoodsReceipt(greceipthm);
                    } else {
                        result = accGoodsReceiptobj.updateGoodsReceipt(greceipthm);
                    }
                }
            }
            msg = messageSource.getMessage("acc.field.setsuccessfully", null, RequestContextUtils.getLocale(request));
            issuccess = true;
            txnManager.commit(status);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
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
            approvePendingInvoice(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String invoiceId = request.getParameter("billid");
            KwlReturnObject InvoiceGRO = accGoodsReceiptobj.getGROFromVInvoices(invoiceId, companyid);
            if (InvoiceGRO.getEntityList() != null && InvoiceGRO.getEntityList().size() > 0) {
                Object[] oj = (Object[]) InvoiceGRO.getEntityList().get(0);
                String goodsRecieptOrderID = oj[1].toString();
                accGoodsReceiptobj.approvePendingGRO(goodsRecieptOrderID, companyid, 11);
                KwlReturnObject grores = accountingHandlerDAOobj.getObject(GoodsReceiptOrder.class.getName(), goodsRecieptOrderID);
                GoodsReceiptOrder doObj = (GoodsReceiptOrder) grores.getEntityList().get(0);
                Set<GoodsReceiptOrderDetails> groDetails = doObj.getRows();
                List<StockMovement> stockMovementsList = new ArrayList<>();
                for (GoodsReceiptOrderDetails groDetail : groDetails) {
                    Product product=groDetail.getProduct();
                    if (product != null &&(product.isIsBatchForProduct() || product.isIslocationforproduct() || product.isIswarehouseforproduct() || product.isIsrowforproduct() || product.isIsrackforproduct() || product.isIsbinforproduct() || product.isIsSerialForProduct())) {
                        accGoodsReceiptModuleService.newStockMovementGROrder(groDetail, stockMovementsList);
                    }
                    Inventory inventory = groDetail.getInventory();
                    if (inventory.isInvrecord()) {
                        inventory.setBaseuomquantity(inventory.getActquantity());
                        inventory.setActquantity(0.0);
                    }
                }
                ExtraCompanyPreferences extraCompanyPreferences = null;
                KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
                extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
                if (extraCompanyPreferences != null && extraCompanyPreferences.isActivateInventoryTab() && !stockMovementsList.isEmpty()) {
                    stockMovementService.addOrUpdateBulkStockMovement(doObj.getCompany(), doObj.getID(), stockMovementsList);
                }
            }

            issuccess = true;
            String action = "Cash Purcahse ";
            String auditaction = AuditAction.CASH_PURCHASE_APPROVED;
            if (!iscash) {
                action = "Vendor Invoice ";
                auditaction = AuditAction.VENDORINVOICEAPPROVED;
            }
            auditTrailObj.insertAuditLog(auditaction, "User " + sessionHandlerImpl.getUserFullName(request) + " has Approved a " + action + billno, request, billno);
            txnManager.commit(status);
            msg = messageSource.getMessage("acc.field.Invoicestatushasbeenupdatedsuccessfully", null, RequestContextUtils.getLocale(request));
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void approvePendingInvoice(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {

        String companyid = sessionHandlerImpl.getCompanyid(request);
        String userid = sessionHandlerImpl.getUserid(request);
        String billid = request.getParameter("billid");
        Boolean isbilling = Boolean.parseBoolean(request.getParameter("isbilling"));
        String remark = request.getParameter("remark");
        boolean iscash = StringUtil.isNullOrEmpty(request.getParameter("incash")) ? false : Boolean.parseBoolean(request.getParameter("incash"));
        String jeID = "";
        boolean isSendMailForNextLevelUsers = true;
        String invoiceNumber = "";
        String vendorEmailId = "";
        boolean updateJEFlag = false;
        KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
        CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
        String baseUrl = URLUtil.getPageURL(request, loginpageFull);
        if (isbilling) {
            KwlReturnObject invObj = accountingHandlerDAOobj.getObject(BillingGoodsReceipt.class.getName(), billid);
            BillingGoodsReceipt invoice = (BillingGoodsReceipt) invObj.getEntityList().get(0);
            jeID = invoice.getJournalEntry().getID();
            invoiceNumber = invoice.getBillingGoodsReceiptNumber();
            if (invoice.getPendingapproval() == invoice.getApprovallevel()) {
                updateJEFlag = true;
                isSendMailForNextLevelUsers = false;
            }

        } else {
            KwlReturnObject invObj = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), billid);
            GoodsReceipt invoice = (GoodsReceipt) invObj.getEntityList().get(0);
            jeID = invoice.getJournalEntry().getID();
            invoiceNumber = invoice.getGoodsReceiptNumber();
            HashMap<String, Object> addressParams = new HashMap<String, Object>();
            addressParams.put("companyid", companyid);
            addressParams.put("isDefaultAddress", true); //always true to get defaultaddress
            addressParams.put("isBillingAddress", true); //true to get billing address
            addressParams.put("vendorid", invoice.getVendor().getID());
            VendorAddressDetails vendorAddressDetail = accountingHandlerDAOobj.getVendorAddressObj(addressParams);
            vendorEmailId = vendorAddressDetail != null ? vendorAddressDetail.getEmailID() : "";
            if (invoice.getPendingapproval() == invoice.getApprovallevel()) {
                Set<GoodsReceiptDetail> invoiceDetails = invoice.getRows();

                for (GoodsReceiptDetail invoiceDetail : invoiceDetails) {
                    Inventory inventory = invoiceDetail.getInventory();
                    if (inventory.isInvrecord()) {
                        inventory.setBaseuomquantity(inventory.getActquantity());
                        inventory.setActquantity(0);
                    }
                }
                updateJEFlag = true;
                isSendMailForNextLevelUsers = false;
            }
        }

        if (updateJEFlag) {
            accJournalEntryobj.approvePendingJE(jeID);
            accJournalEntryobj.saveAccountJEs_optimized(jeID);
        }

        //Insert new entries in optimized table.

        int approvedLevel = accGoodsReceiptobj.approvePendingInvoice(billid, isbilling, companyid, userid);
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("transtype", Constants.VENDOR_INVOICE_APPROVAL);
        hashMap.put("transid", billid);
        hashMap.put("approvallevel", approvedLevel);
        hashMap.put("remark", remark);
        hashMap.put("userid", userid);
        hashMap.put("companyid", companyid);
        accountingHandlerDAOobj.updateApprovalHistory(hashMap);
        KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
        Company company = (Company) returnObject.getEntityList().get(0);
        String fromEmailId = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
        if (isSendMailForNextLevelUsers && preferences.isSendapprovalmail()) { //this only for level 2. we aleady check is pending level and approve level are same or not
            String[] emails = {};
            String userName = sessionHandlerImpl.getUserFullName(request);
            String moduleName = "Vendor Invoice";
            emails = accountingHandlerDAOobj.getApprovalUserList(request, moduleName, 2);
            if (!StringUtil.isNullOrEmpty(preferences.getApprovalEmails())) {
                String[] compPrefMailIds = preferences.getApprovalEmails().split(",");
                emails = AccountingManager.getMergedMailIds(emails, compPrefMailIds);
            }
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
            //String fromEmailId = Constants.ADMIN_EMAILID;
            accountingHandlerDAOobj.sendApprovalEmails(mailParameters);
        } else if (preferences.isSendapprovalmail()) {
            String[] emails = {};
            String userName = sessionHandlerImpl.getUserFullName(request);
            String moduleName = "Vendor Invoice";
            String approvalpendingStatusmsg = "";
            emails = accountingHandlerDAOobj.getApprovalUserList(request, moduleName, 2);//Leval value hard coded as 2
            if (!StringUtil.isNullOrEmpty(preferences.getApprovalEmails())) {
                String[] compPrefMailIds = preferences.getApprovalEmails().split(",");
                emails = AccountingManager.getMergedMailIds(emails, compPrefMailIds);
            }
            HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
            qdDataMap.put(Constants.companyKey, companyid);
            qdDataMap.put("level", approvedLevel);
            qdDataMap.put(Constants.moduleid, Constants.Acc_Vendor_Invoice_ModuleId);
            if (approvedLevel < 11) {
                approvalpendingStatusmsg=commonFnControllerService.getApprovalstatusmsg(qdDataMap);
                }
            Map<String, Object> mailParameters = new HashMap();
            mailParameters.put("Number", invoiceNumber);
            mailParameters.put("userName", userName);
            mailParameters.put("emails", emails);
            mailParameters.put("moduleName", moduleName);
            mailParameters.put("sendorInfo", fromEmailId);
            mailParameters.put("addresseeName", "All");
            mailParameters.put("companyid", company.getCompanyID());
            mailParameters.put("baseUrl", baseUrl);
            mailParameters.put("approvalstatuslevel", approvedLevel);
            mailParameters.put("approvalpendingStatusmsg", approvalpendingStatusmsg);
            if (emails.length > 0) {
                accountingHandlerDAOobj.sendApprovedEmails(mailParameters);
            }
        }

//        ExtraCompanyPreferences extraCompanyPreferences = null;
//        KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
//        extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
        DocumentEmailSettings documentEmailSettings = null;
        KwlReturnObject documentEmailresult = accountingHandlerDAOobj.getObject(DocumentEmailSettings.class.getName(), sessionHandlerImpl.getCompanyid(request));
        documentEmailSettings = documentEmailresult != null ? (DocumentEmailSettings) documentEmailresult.getEntityList().get(0) : null;
        if (documentEmailSettings != null && documentEmailSettings.isPurchaseInvoiceGenerationMail()&& !isSendMailForNextLevelUsers) {
            String userName = sessionHandlerImpl.getUserFullName(request);
            String creatorEMailId = preferences.getCompany().getCreator() != null ? preferences.getCompany().getCreator().getEmailID() : "";
            List<String> mailIds = new ArrayList();
            if (!StringUtil.isNullOrEmpty(vendorEmailId)) {
                mailIds.add(vendorEmailId);
    }
            if (!StringUtil.isNullOrEmpty(creatorEMailId)) {
                mailIds.add(creatorEMailId);
            }
            String[] temp = new String[mailIds.size()];
            String[] tomailids = mailIds.toArray(temp);
            String moduleName = "";
            if (iscash) {
                moduleName = "Cash Purchase";
            } else {
                moduleName = "Purchase Invoice";
            }
            accountingHandlerDAOobj.sendSaveTransactionEmails(invoiceNumber, moduleName, tomailids, userName, false, companyid);
        }
    }


    public void savePRNewBatch(String batchJSON, Inventory inventory, HttpServletRequest request, PurchaseReturnDetail purchaseReturnDetail, List<StockMovement> stockMovementsList) throws JSONException, ParseException, SessionExpiredException, ServiceException, UnsupportedEncodingException, AccountingException {
        JSONArray jArr = new JSONArray(batchJSON);
        KwlReturnObject kmsg = null;
        double ActbatchQty = 1;
        double batchQty = 0;
        boolean isBatch = false;
        boolean isserial = false;
        boolean isLocationForProduct = false;
        boolean isWarehouseForProduct = false;
        boolean isRowForProduct = false;
        boolean isRackForProduct = false;
        boolean isBinForProduct = false;
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
            isLocationForProduct = product.isIslocationforproduct();
            isRowForProduct = product.isIsrowforproduct();
            isRackForProduct = product.isIsrackforproduct();
            isBinForProduct = product.isIsbinforproduct();
            isWarehouseForProduct = product.isIswarehouseforproduct();
            isBatchForProduct = product.isIsBatchForProduct();
            isSerialForProduct = product.isIsSerialForProduct();
        }

        //Save Batch detail for both  option for serial no and batch also as if batch option is off then also we are generating batch in backend
        StockMovementDetail smd=null;
        StockMovement stockMovement=null;
        Map<Store, StockMovement> storeWiseStockMovement = new HashMap<Store, StockMovement>();
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jSONObject = new JSONObject(jArr.get(i).toString());
            if (jSONObject.has("quantity") && !jSONObject.getString("quantity").equals("undefined")) {
                ActbatchQty = jSONObject.getDouble("quantity");
            }
            if (batchQty == 0) {
                batchQty = jSONObject.getDouble("quantity");
                
                KwlReturnObject warehouseObj = accountingHandlerDAOobj.getObject(Store.class.getName(), jSONObject.getString("warehouse"));
                Store store = (Store) warehouseObj.getEntityList().get(0);
                
                if (storeWiseStockMovement.containsKey(store)) {
                    stockMovement = storeWiseStockMovement.get(store);
                    stockMovement.setQuantity(stockMovement.getQuantity() + jSONObject.optDouble("quantity", 0.0));
                } else {
                    stockMovement = new StockMovement();
                    if (store != null) {
                        stockMovement.setStore(store);
                    }
                    stockMovement.setCompany(inventory.getCompany());
                    stockMovement.setProduct(inventory.getProduct());
                    stockMovement.setStockUoM(inventory.getProduct().getUnitOfMeasure());
                    stockMovement.setPricePerUnit(purchaseReturnDetail.getRate()/purchaseReturnDetail.getBaseuomrate());
                    stockMovement.setQuantity(jSONObject.optDouble("quantity", 0.0));
                    stockMovement.setTransactionDate(purchaseReturnDetail.getPurchaseReturn().getOrderDate());
                    stockMovement.setModuleRefId(purchaseReturnDetail.getPurchaseReturn().getID());
                    stockMovement.setModuleRefDetailId(purchaseReturnDetail.getID());
                    stockMovement.setVendor(purchaseReturnDetail.getPurchaseReturn().getVendor());
                    stockMovement.setCostCenter(purchaseReturnDetail.getPurchaseReturn().getCostcenter());
                    stockMovement.setTransactionNo(purchaseReturnDetail.getPurchaseReturn().getPurchaseReturnNumber());
                    stockMovement.setTransactionModule(TransactionModule.ERP_PURCHASE_RETURN);
                    stockMovement.setTransactionType(TransactionType.OUT);
                    storeWiseStockMovement.put(store, stockMovement);
                }
            }
            if ((isLocationForProduct || isWarehouseForProduct || isBatchForProduct) && (batchQty == ActbatchQty)) {
                HashMap<String, Object> documentMap = new HashMap<String, Object>();
                documentMap.put("quantity", jSONObject.getString("quantity"));
                documentMap.put("batchmapid", jSONObject.getString("purchasebatchid"));
                documentMap.put("documentid", purchaseReturnDetail.getID());
                documentMap.put("transactiontype", "31");//This is PR Type Tranction  
                if (jSONObject.has("mfgdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("mfgdate"))) {
                    documentMap.put("mfgdate", authHandler.getDateOnlyFormat(request).parse(jSONObject.getString("mfgdate")));
                }
                if (jSONObject.has("expdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("expdate"))) {
                    documentMap.put("expdate", authHandler.getDateOnlyFormat(request).parse(jSONObject.getString("expdate")));
                }
                documentMap.put("purchasereturn", "true");

                KwlReturnObject locationUpdate = accountingHandlerDAOobj.getObject(Location.class.getName(), jSONObject.getString("location"));
                Location locationObj = (Location) locationUpdate.getEntityList().get(0);
                smd = new StockMovementDetail();
                if (locationObj != null) {
                    smd.setLocation(locationObj);
                }
                if (isRowForProduct) {
                    KwlReturnObject krObject = accountingHandlerDAOobj.getObject(StoreMaster.class.getName(), jSONObject.optString("row"));
                    StoreMaster row = (StoreMaster) krObject.getEntityList().get(0);
                    smd.setRow(row);
                }
                if (isRackForProduct) {
                    KwlReturnObject krObject = accountingHandlerDAOobj.getObject(StoreMaster.class.getName(), jSONObject.optString("rack"));
                    StoreMaster rack = (StoreMaster) krObject.getEntityList().get(0);
                    smd.setRack(rack);
                }
                if (isBinForProduct) {
                    KwlReturnObject krObject = accountingHandlerDAOobj.getObject(StoreMaster.class.getName(), jSONObject.optString("bin"));
                    StoreMaster bin = (StoreMaster) krObject.getEntityList().get(0);
                    smd.setBin(bin);
                }
                smd.setQuantity(Double.parseDouble(jSONObject.getString("quantity")));
                smd.setBatchName("");
                smd.setStockMovement(stockMovement);
                stockMovement.getStockMovementDetails().add(smd);
                
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
                    filter_names.add("product");
                    filter_params.add(inventory.getProduct().getID());

                    filterRequestParams.put("filter_names", filter_names);
                    filterRequestParams.put("filter_params", filter_params);
                    filterRequestParams.put("order_by", order_by);
                    filterRequestParams.put("order_type", order_type);
                    KwlReturnObject result = accMasterItemsDAOobj.getNewBatches(filterRequestParams,false,false);
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
                                    quantityToDue = quantityToDue - dueQty;

                                } else {
                                    batchUpdateQtyMap.put("qty", String.valueOf(-(quantityToDue)));
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
                    batchUpdateQtyMap.put("id", jSONObject.getString("purchasebatchid"));
                    accCommonTablesDAO.saveBatchAmountDue(batchUpdateQtyMap);
                    //Code to Send Batch
                    KwlReturnObject batchObj = accountingHandlerDAOobj.getObject(NewProductBatch.class.getName(), jSONObject.getString("purchasebatchid"));
                    NewProductBatch newProductBatch1 = (NewProductBatch) batchObj.getEntityList().get(0);
                    
                    smd.setBatchName(newProductBatch1.getBatchname());

                }
                accCommonTablesDAO.saveBatchDocumentMapping(documentMap);


            }
            batchQty--;
            if (!isSerialForProduct && isBatchForProduct) {
//                stockMovement.setAttachedBaches(attachedBatchsList);
                stockMovementsList.add(stockMovement);

            }


            if (isSerialForProduct) {  //if serial no option is on then only save the serial no details 

                HashMap<String, Object> documentMap = new HashMap<String, Object>();
                documentMap.put("quantity", 1);
                documentMap.put("serialmapid", jSONObject.getString("purchaseserialid"));
                documentMap.put("documentid", purchaseReturnDetail.getID());
                documentMap.put("transactiontype", "31");//This is GRN Type Tranction  
                if (jSONObject.has("expstart") && !StringUtil.isNullOrEmpty(jSONObject.getString("expstart"))) {
                    documentMap.put("expfromdate", authHandler.getDateOnlyFormat(request).parse(jSONObject.getString("expstart")));
                }
                if (jSONObject.has("expend") && !StringUtil.isNullOrEmpty(jSONObject.getString("expend"))) {
                    documentMap.put("exptodate", authHandler.getDateOnlyFormat(request).parse(jSONObject.getString("expend")));
                }
                documentMap.put("purchasereturn", "true");

                accCommonTablesDAO.saveSerialDocumentMapping(documentMap);
                HashMap<String, Object> serialUpdateQtyMap = new HashMap<String, Object>();
                serialUpdateQtyMap.put("qty", "-1");
                serialUpdateQtyMap.put("id", jSONObject.getString("purchaseserialid"));
                serialUpdateQtyMap.put("purchasereturn", "true");
                accCommonTablesDAO.saveSerialAmountDue(serialUpdateQtyMap);
                //Code to Send Serial Numbers to Inventory
                KwlReturnObject serialObj = accountingHandlerDAOobj.getObject(NewBatchSerial.class.getName(), jSONObject.getString("purchaseserialid"));
                NewBatchSerial newBatchSerial = (NewBatchSerial) serialObj.getEntityList().get(0);
                if (newBatchSerial != null) {
                    smd.addSerialName(newBatchSerial.getSerialname());
                }
            } else {
                batchQty = 0;
            }
        }
        if (isWarehouseForProduct && isLocationForProduct) {
            for (Map.Entry<Store, StockMovement> entry : storeWiseStockMovement.entrySet()) {
                stockMovementsList.add(entry.getValue());
            }
        }

    }

    public ModelAndView getMasterItemPriceFormulaPrice(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            jobj = getMasterItemPriceFormulaPrice(request);

            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accMasterItemsController.getMasterItemPriceFormula : " + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getMasterItemPriceFormulaPrice(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            String productId = request.getParameter("productId");
            boolean iscalculatefromqty = false;
            String iscalculatefromqtystr = request.getParameter("iscalculatefromqty");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            if (!StringUtil.isNullOrEmpty(iscalculatefromqtystr)) {
                iscalculatefromqty = Boolean.parseBoolean(iscalculatefromqtystr);
            }
            double itemNo = 0;
            String strVal = request.getParameter("item");
            if (!StringUtil.isNullOrEmpty(strVal)) {
                itemNo = Double.parseDouble(strVal);
            }

            KwlReturnObject result = accGoodsReceiptobj.getMasterItemPriceFormulaPrice(productId, itemNo);
            List list = result.getEntityList();
            JSONArray jArr = new JSONArray();
            if (list != null && !list.isEmpty()) {
                MasterItemPriceFormula masterItemPriceFormula = (MasterItemPriceFormula) list.get(0);
                double lowervalue = masterItemPriceFormula.getLowerlimitvalue();
                double uppervalue = masterItemPriceFormula.getUpperlimitvalue();
                double basevalue = masterItemPriceFormula.getBasevalue();
                double incvalue = masterItemPriceFormula.getIncvalue();

                JSONObject obj = new JSONObject();
                double priceValue = basevalue + (itemNo - lowervalue) * incvalue;
                if (iscalculatefromqty && itemNo > 0) {
                    priceValue = priceValue / itemNo;
                }
                priceValue = authHandler.round(priceValue, companyid);
                obj.put("pricevalue", priceValue);
                jArr.put(obj);
            } else {
                JSONObject obj = new JSONObject();
                itemNo = authHandler.round(itemNo, companyid);
                obj.put("pricevalue", itemNo);
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("count", jArr.length());
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }

//    public Map<String, Object> getVendorDefaultAddressParams(Vendor vendor,String companyid) {
//        Map<String, Object> addressMap = new HashMap<String, Object>();
//        try {
//            HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
//            addrRequestParams.put("vendorid", vendor.getID());
//            addrRequestParams.put("companyid", companyid);
//            addrRequestParams.put("isDefaultAddress", true);
//            KwlReturnObject addressResult = accountingHandlerDAOobj.getVendorAddressDetails(addrRequestParams);
//            if (!addressResult.getEntityList().isEmpty()) {
//                List<VendorAddressDetails> vendAddrList = addressResult.getEntityList();
//                for (VendorAddressDetails vendAddr : vendAddrList) {
//                    if (vendAddr.isIsBillingAddress()) {
//                        addressMap.put(Constants.BILLING_ADDRESS_TYPE, vendAddr.getAliasName()==null?"":vendAddr.getAliasName());
//                        addressMap.put(Constants.BILLING_ADDRESS, vendAddr.getAddress()==null?"":vendAddr.getAddress());
//                        addressMap.put(Constants.BILLING_COUNTRY, vendAddr.getCountry()==null?"":vendAddr.getCountry());
//                        addressMap.put(Constants.BILLING_STATE, vendAddr.getState()==null?"":vendAddr.getState());
//                        addressMap.put(Constants.BILLING_CITY, vendAddr.getCity()==null?"":vendAddr.getCity());
//                        addressMap.put(Constants.BILLING_POSTAL, vendAddr.getPostalCode()==null?"":vendAddr.getPostalCode());
//                        addressMap.put(Constants.BILLING_EMAIL, vendAddr.getEmailID()==null?"":vendAddr.getEmailID());
//                        addressMap.put(Constants.BILLING_FAX, vendAddr.getFax()==null?"":vendAddr.getFax());
//                        addressMap.put(Constants.BILLING_MOBILE, vendAddr.getMobileNumber()==null?"":vendAddr.getMobileNumber());
//                        addressMap.put(Constants.BILLING_PHONE, vendAddr.getPhone()==null?"":vendAddr.getPhone());
//                        addressMap.put(Constants.BILLING_RECIPIENT_NAME, vendAddr.getRecipientName()==null?"":vendAddr.getRecipientName());
//                        addressMap.put(Constants.BILLING_CONTACT_PERSON, vendAddr.getContactPerson()==null?"":vendAddr.getContactPerson());
//                        addressMap.put(Constants.BILLING_CONTACT_PERSON_NUMBER, vendAddr.getContactPersonNumber()==null?"":vendAddr.getContactPersonNumber());
//                    } else {
//                        addressMap.put(Constants.SHIPPING_ADDRESS_TYPE, vendAddr.getAliasName()==null?"":vendAddr.getAliasName());
//                        addressMap.put(Constants.SHIPPING_ADDRESS, vendAddr.getAddress()==null?"":vendAddr.getAddress());
//                        addressMap.put(Constants.SHIPPING_COUNTRY, vendAddr.getCountry()==null?"":vendAddr.getCountry());
//                        addressMap.put(Constants.SHIPPING_STATE, vendAddr.getState()==null?"":vendAddr.getState());
//                        addressMap.put(Constants.SHIPPING_CITY, vendAddr.getCity()==null?"":vendAddr.getCity());
//                        addressMap.put(Constants.SHIPPING_EMAIL, vendAddr.getEmailID()==null?"":vendAddr.getEmailID());
//                        addressMap.put(Constants.SHIPPING_FAX, vendAddr.getFax()==null?"":vendAddr.getFax());
//                        addressMap.put(Constants.SHIPPING_MOBILE, vendAddr.getMobileNumber()==null?"":vendAddr.getMobileNumber());
//                        addressMap.put(Constants.SHIPPING_PHONE, vendAddr.getPhone()==null?"":vendAddr.getPhone());
//                        addressMap.put(Constants.SHIPPING_RECIPIENT_NAME, vendAddr.getPhone()==null?"":vendAddr.getRecipientName());
//                        addressMap.put(Constants.SHIPPING_POSTAL, vendAddr.getPostalCode()==null?"":vendAddr.getPostalCode());
//                        addressMap.put(Constants.SHIPPING_CONTACT_PERSON_NUMBER, vendAddr.getContactPersonNumber()==null?"":vendAddr.getContactPersonNumber());
//                        addressMap.put(Constants.SHIPPING_CONTACT_PERSON, vendAddr.getContactPerson()==null?"":vendAddr.getContactPerson());
//                    }
//                }
//            }
//        } catch (Exception ex) {
//            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return addressMap;
//    }

     public ModelAndView getRepeateVendorInvoicesDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "", parentInvoiceId = "";
        try {
            if(!StringUtil.isNullOrEmpty(request.getParameter("parentid"))){
                parentInvoiceId = request.getParameter("parentid");
            } else if(!StringUtil.isNullOrEmpty(request.getParameter("bills"))){
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
                details = accGoodsReceiptobj.getRepeateVendorInvoicesDetailsForExpander(requestParams);
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
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
     public ModelAndView getRepeateBillingGoodsReceiptDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            String parentInvoiceId = request.getParameter("parentid");
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("parentInvoiceId", parentInvoiceId);
            KwlReturnObject details = accGoodsReceiptobj.getRepeateBillingGoodsReceiptDetails(requestParams);
            List detailsList = details.getEntityList();
            Iterator itr = detailsList.iterator();

            JSONArray JArr = new JSONArray();
            while (itr.hasNext()) {
                BillingGoodsReceipt repeatedInvoice = (BillingGoodsReceipt) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("invoiceId", repeatedInvoice.getID());
                obj.put("invoiceNo", repeatedInvoice.getBillingGoodsReceiptNumber());
                obj.put("parentInvoiceId", parentInvoiceId);
                JArr.put(obj);
            }

            jobj.put("data", JArr);
            jobj.put("count", details.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
     
    public ModelAndView approvegr(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "<ol style='list-style: inside none inside; padding: 5px;'>";
        String notAuthorisedMsg = "<ol style='list-style: inside none inside; padding: 5px;'>";
        String userMsg = "";
        String combineUseMsg = "";
        String notAuthorisedUserMsg = "";
        String moduleName = "";
        boolean issuccess = false;
        boolean isAccountingExe = false;
        JSONArray pendingTransArray = new JSONArray();
        TransactionStatus status = null;
        StringBuffer productIds = new StringBuffer();
        try {
            String currentUser = sessionHandlerImpl.getUserid(request);
            String remark = request.getParameter("remark");
            String detail = request.getParameter("data");
            String grID = "";
            String roleName = "Company User";
            String postingDateStr = request.getParameter("postingDate");
            DateFormat df = authHandler.getDateOnlyFormat();
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            Date postingDate = null;
            if (!StringUtil.isNullOrEmpty(postingDateStr)) {
                postingDate = df.parse(postingDateStr);
            }
            boolean iscash = StringUtil.isNullOrEmpty(request.getParameter("incash")) ? false : Boolean.parseBoolean(request.getParameter("incash"));
            if (iscash) {
                moduleName = "cash purchase ";
            } else {
                moduleName = "purchase Invoice";
            }
            if (!StringUtil.isNullOrEmpty(detail)) {
                pendingTransArray = new JSONArray(detail);
            }
            boolean isLeaseFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isLeaseFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isLeaseFixedAsset")) : false;
            boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String baseUrl = URLUtil.getPageURL(request, loginpageFull);
            /*Below for loop is used to approve transactions in batch*/
            for (int i = 0; i < pendingTransArray.length(); i++) {
                JSONObject jobj1 = pendingTransArray.getJSONObject(i);
                double amount = authHandler.round(jobj1.optDouble("totalorderamount", 0), companyid);//StringUtil.isNullOrEmpty(request.getParameter("totalorderamount"))? 0 : authHandler.round(Double.parseDouble(request.getParameter("totalorderamount")),Constants.AMOUNT_DIGIT_AFTER_DECIMAL);
                grID = jobj1.optString("billid", "");
                KwlReturnObject CQObj = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), grID);
                GoodsReceipt cqObj = (GoodsReceipt) CQObj.getEntityList().get(0);
                KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
                KwlReturnObject extraPref = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
                ExtraCompanyPreferences extrareferences = (ExtraCompanyPreferences) extraPref.getEntityList().get(0);
                HashMap<String, Object> invApproveMap = new HashMap<String, Object>();
                int level = cqObj.getApprovestatuslevel();
                String currencyid = cqObj.getCurrency() != null ? cqObj.getCurrency().getCurrencyID() : sessionHandlerImpl.getCurrencyID(request);
                // Add Product and discounts mapping
                HashMap<String, Object> GlobalParams = AccountingManager.getGlobalParams(request);
                JSONArray productDiscountJArr = new JSONArray();
                Set<GoodsReceiptDetail> grDetails = cqObj.getRows();
                for (GoodsReceiptDetail grDetail : grDetails) {
                    if (grDetail.getInventory() != null) {
                        String productId = grDetail.getInventory().getProduct().getID();
                        Discount invDiscount = grDetail.getDiscount();
                        double discAmountinBase = 0;
                        if (invDiscount != null) {
                            double discountVal = invDiscount.getDiscountValue();
//                            KwlReturnObject dAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, discountVal, currencyid, cqObj.isIsOpeningBalenceInvoice() && cqObj.getCreationDate() != null ? cqObj.getCreationDate() : cqObj.getJournalEntry().getEntryDate(), cqObj.getExternalCurrencyRate());
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
                invApproveMap.put("totalAmount", String.valueOf(cqObj.getInvoiceAmountInBase()));
                invApproveMap.put("currentUser", currentUser);
                invApproveMap.put("fromCreate", false);
                invApproveMap.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);
                invApproveMap.put("iscash", iscash);
                invApproveMap.put("productDiscountMapList", productDiscountJArr);
                invApproveMap.put(Constants.PAGE_URL, baseUrl);
                int approvedLevel = 0;
                String JENumber = "";
                String JEMsg = "";
                DefaultTransactionDefinition def1 = new DefaultTransactionDefinition();
                def1.setName("PO_Tx");
                def1.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
                TransactionStatus syncstatus = txnManager.getTransaction(def1);
                synchronized (this) {
                    try {

                        List approvedLevelList = approvegr(cqObj, invApproveMap, true);
                        approvedLevel = (Integer) approvedLevelList.get(0);
                        //approvedLevel = approvegr(cqObj, invApproveMap, true);
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
                                Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
                                jeDataMap.put("postingDate",postingDateStr);
//                                String JENumBer = journalEntryModuleServiceobj.updateJEEntryNumberForNewJE(jeDataMap, cqObj.getJournalEntry(), companyid, format.getID(), isApproved);
                                String JENumBer = "";
                                KwlReturnObject returnObj = journalEntryModuleServiceobj.updateJEEntryNumberForNewJE(jeDataMap, cqObj.getJournalEntry(), companyid, format.getID(), isApproved);
                                if (returnObj.isSuccessFlag() && returnObj.getRecordTotalCount() > 0) {
                                    JENumBer = (String) returnObj.getEntityList().get(0);
                                } else if (!returnObj.isSuccessFlag()) {
                                    throw new AccountingException((String)returnObj.getEntityList().get(0));
                                }
                            } else {
                                JSONObject jeJobj = new JSONObject();
                                HashSet<JournalEntryDetail> details = new HashSet<JournalEntryDetail>();
                                jeJobj.put("jeid", jeID);
                                jeJobj.put("companyid", jeCompany);
                                jeJobj.put("pendingapproval", 0);
                                JSONObject jObj = extrareferences.getColumnPref() != null ? new JSONObject(extrareferences.getColumnPref())  : new JSONObject();
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
                            // Approve Auto Generated GR if respective PI approved
                            KwlReturnObject InvoiceGRO = accGoodsReceiptobj.getAutoGeneratedGROFromVInvoices(grID, companyid);
                            if (InvoiceGRO.getEntityList() != null && InvoiceGRO.getEntityList().size() > 0) {
                                Object[] oj = (Object[]) InvoiceGRO.getEntityList().get(0);
                                String goodsRecieptOrderID = oj[1].toString();
                                KwlReturnObject grores = accountingHandlerDAOobj.getObject(GoodsReceiptOrder.class.getName(), goodsRecieptOrderID);
                                GoodsReceiptOrder doObj = (GoodsReceiptOrder) grores.getEntityList().get(0);
                                if (doObj.getApprovestatuslevel() != 11) {
                                    accGoodsReceiptobj.approvePendingGRO(goodsRecieptOrderID, companyid, 11);

                                    List<StockMovement> stockMovementsList = new ArrayList<>();
                                    Set<GoodsReceiptOrderDetails> groDetails = doObj.getRows();
                                    for (GoodsReceiptOrderDetails groDetail : groDetails) {
                                        Product product = groDetail.getProduct();
                                        if (product != null && (extrareferences != null && extrareferences.isActivateInventoryTab()) && (product.isIsBatchForProduct() || product.isIslocationforproduct() || product.isIswarehouseforproduct() || product.isIsrowforproduct() || product.isIsrackforproduct() || product.isIsbinforproduct() || product.isIsSerialForProduct())) {
                                            accGoodsReceiptModuleService.newStockMovementGROrder(groDetail, stockMovementsList);
                                        }
                                        Inventory inventory = groDetail.getInventory();
                                        if (inventory.isInvrecord()) {
                                            inventory.setBaseuomquantity(inventory.getActquantity());
                                            inventory.getProduct().setAvailableQuantity(inventory.getProduct().getAvailableQuantity() + inventory.getBaseuomquantity());
                                            inventory.setActquantity(0.0);
                                        }
                                        if (product != null && productIds.indexOf(product.getID()) == -1) {
                                            productIds.append(product.getID()).append(",");
                                        }
                                    }
                                    if (doObj.getInventoryJE() != null && extrareferences != null && (extrareferences.isActivateMRPModule() || preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD)) {
                                        doObj.getInventoryJE().setPendingapproval(0); // approve GRO Inventory JE
                                    }
                                    if (extrareferences != null && extrareferences.isActivateInventoryTab() && !stockMovementsList.isEmpty()) {
                                        stockMovementService.addOrUpdateBulkStockMovement(doObj.getCompany(), doObj.getID(), stockMovementsList);
                                    }
                                }
                            }
                            
                            /* Modify RCM Un-Registered Journal Entry details table data on Approve Purchase Invoice
                                && cqObj.isGtaapplicable()
                            */
                            Date entryDateForLock = null;
                            entryDateForLock = cqObj.getJournalEntry().getEntryDate();
                            if (Constants.isRCMPurchaseURD5KLimit && cqObj != null && cqObj.getVendor() != null) {
                                if (cqObj.getVendor().getGSTRegistrationType() != null && cqObj.getVendor().getGSTRegistrationType().getDefaultMasterItem() != null) {
                                    String DefaultMasterItemId = cqObj.getVendor().getGSTRegistrationType().getDefaultMasterItem().getID();
                                    if (DefaultMasterItemId.equals(Constants.GSTRegType.get(Constants.GSTRegType_Unregistered))) {
                                        /**
                                         * Modify All Journal Entry details if daily limit cross on
                                         * particular Bill date
                                         */
                                        if (entryDateForLock != null) {
                                            paramJobj.put("billdate", df.format(entryDateForLock));
                                            paramJobj.put("companyid", companyid);
                                            paramJobj.put(Constants.df, df);
                                            paramJobj.put("invoiceAmount", 0);
                                            paramJobj.put("GRNNumber", cqObj.getGoodsReceiptNumber());
                                            accGoodsReceiptModuleService.modifyURDVendorRCMPurchaseInvoiceJEDetails(paramJobj);
                                        }
                                    }
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
                if (approvedLevel != Constants.NoAuthorityToApprove && preferences.isSendapprovalmail()) {//If allow to send approval mail in company account preferences

                    HashMap emailMap = new HashMap();
                    String userName = sessionHandlerImpl.getUserFullName(request);
                    emailMap.put("userName", userName);
                    KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                    Company company = (Company) returnObject.getEntityList().get(0);
                    emailMap.put("company", company);
                    emailMap.put("purchaseInvoice", cqObj);
                    emailMap.put("baseUrl", baseUrl);
                    emailMap.put("preferences", preferences);
                    emailMap.put("ApproveMap", invApproveMap);

                    accGoodsReceiptModuleService.sendApprovalMailIfAllowedFromSystemPreferences(emailMap);
                }
                // Save Approval History
                if (approvedLevel != Constants.NoAuthorityToApprove) {
                    HashMap<String, Object> hashMap = new HashMap<String, Object>();
                    hashMap.put("transtype", Constants.VENDOR_Invoice_APPROVAL);
                    hashMap.put("transid", cqObj.getID());
                    hashMap.put("approvallevel", cqObj.getApprovestatuslevel());//  If approvedLevel = 11 then its final Approval
                    hashMap.put("remark", remark);
                    hashMap.put("userid", sessionHandlerImpl.getUserid(request));
                    hashMap.put("companyid", companyid);
                    accountingHandlerDAOobj.updateApprovalHistory(hashMap);
                    // Audit log entry
                    String action = "Cash Purchase ";
                    String auditaction = AuditAction.CASH_SALES_APPROVED;
                    if (!iscash) {
                        action = "Vendor Invoice ";
                        auditaction = AuditAction.CUSTOMERINVOICEAPPROVED;
                    }
                    if (cqObj.getJournalEntry()!=null && !StringUtil.isNullOrEmpty(cqObj.getJournalEntry().getEntryNumber())) {
                        JENumber = " with JE No. " + cqObj.getJournalEntry().getEntryNumber();
                    }
                    auditTrailObj.insertAuditLog(auditaction, "User " + sessionHandlerImpl.getUserFullName(request) + " has Approved a " + action + cqObj.getGoodsReceiptNumber() + JENumber + " at Level-" + cqObj.getApprovestatuslevel(), request, cqObj.getID());

                    txnManager.commit(status);
                    issuccess = true;
                    KwlReturnObject kmsg = null;
                    kmsg = permissionHandlerDAOObj.getRoleofUser(sessionHandlerImpl.getUserid(request));
                    Iterator ite2 = kmsg.getEntityList().iterator();
                    while (ite2.hasNext()) {
                        Object[] row = (Object[]) ite2.next();
                        roleName = row[1].toString();
                    }
                    /*Message binding as per approval transaction*/
                    userMsg = roleName + " " + sessionHandlerImpl.getUserFullName(request) + " " + messageSource.getMessage("acc.field.transactionhasbeenapprovedsuccessfully", null, RequestContextUtils.getLocale(request));
                    msg += "<li>" + messageSource.getMessage("acc.common.purchaseinvoice", null, RequestContextUtils.getLocale(request)) + "<b>" +" "+ cqObj.getGoodsReceiptNumber() + "</b> " + " at Level " + cqObj.getApprovestatuslevel() + "." + JEMsg + "</li>";
                } else {
                    txnManager.commit(status);
                    issuccess = true;
                    /*Message binding as per user are not authorise to  approval transaction*/
                    notAuthorisedUserMsg = roleName + " " + sessionHandlerImpl.getUserFullName(request) + " " + messageSource.getMessage("acc.field.transactionsarenotapproved", null, RequestContextUtils.getLocale(request));
                    notAuthorisedMsg += "<li>" + messageSource.getMessage("acc.common.purchaseinvoice", null, RequestContextUtils.getLocale(request)) + "<b>" +" "+ cqObj.getGoodsReceiptNumber() + "</b> " + " at Level " + cqObj.getApprovestatuslevel() + "." + JEMsg + "</li>";
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
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(AccountingException ae){
            if (!StringUtil.isNullObject(status)) {
                txnManager.rollback(status);
            }
            isAccountingExe=true;
            combineUseMsg = ae.getMessage();
            combineUseMsg = combineUseMsg.replaceFirst("Transaction", "JE Posting");
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ae);
        }catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", combineUseMsg);
                jobj.put("productIds", productIds);
                jobj.put("isAccountingExe", isAccountingExe);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public List<String> approvegr(GoodsReceipt gr, HashMap<String, Object> grApproveMap, boolean isMailApplicable) throws SessionExpiredException, AccountingException, ServiceException, ScriptException, MessagingException, JSONException {
        boolean hasAuthority = false;
        String companyid = "";
        boolean iscash = false;
        List returnList = new ArrayList();
        List mailParamList = new ArrayList();
        int returnStatus;

        if (grApproveMap.containsKey("companyid") && grApproveMap.get("companyid") != null) {
            companyid = grApproveMap.get("companyid").toString();
        }
        String currentUser = "";
        if (grApproveMap.containsKey("currentUser") && grApproveMap.get("currentUser") != null) {
            currentUser = grApproveMap.get("currentUser").toString();
        }
        int level = 0;
        if (grApproveMap.containsKey("level") && grApproveMap.get("level") != null) {
            level = Integer.parseInt(grApproveMap.get("level").toString());
        }
        String amount = "";
        if (grApproveMap.containsKey("totalAmount") && grApproveMap.get("totalAmount") != null) {
            amount = grApproveMap.get("totalAmount").toString();
        }
        boolean fromCreate = false;
        if (grApproveMap.containsKey("fromCreate") && grApproveMap.get("fromCreate") != null) {
            fromCreate = Boolean.parseBoolean(grApproveMap.get("fromCreate").toString());
        }
        int moduleid = 0;
        if (grApproveMap.containsKey("moduleid") && grApproveMap.get("moduleid") != null) {
            moduleid = Integer.parseInt(grApproveMap.get("moduleid").toString());
        }
        if (grApproveMap.containsKey("iscash") && grApproveMap.get("iscash") != null) {
            iscash = Boolean.parseBoolean(grApproveMap.get("iscash").toString());
        }
        JSONArray productDiscountMapList = null;
        if (grApproveMap.containsKey("productDiscountMapList") && grApproveMap.get("productDiscountMapList") != null) {
            productDiscountMapList = new JSONArray(grApproveMap.get("productDiscountMapList").toString());
        }

        if (!fromCreate) {
            String thisUser = currentUser;
            KwlReturnObject userclass = accountingHandlerDAOobj.getObject(User.class.getName(), thisUser);
            User user = (User) userclass.getEntityList().get(0);

            if (AccountingManager.isCompanyAdmin(user)) {
                hasAuthority = true;
            } else {
                /*
                 If "Send approval documents to next level" is disabled from system preferences & pending document is edited then
                 1. When user is authorised then document is always goes at first level
                 2. When user is not authorised then document remains at same level
                 
                 */
                boolean isEditedPendingDocumentWithCheckOff = false;
                if (grApproveMap.containsKey("isEditedPendingDocumentWithCheckOff") && grApproveMap.get("isEditedPendingDocumentWithCheckOff") != null) {
                    level = Integer.parseInt(grApproveMap.get("documentLevel").toString());//Actual level of document for fetching rule at that level for the user
                    grApproveMap.put("level", level);
                    isEditedPendingDocumentWithCheckOff = true;
                }
                
                hasAuthority = accountingHandlerDAOobj.checkForMultiLevelApprovalRules(grApproveMap);
                
                /*---If User is authorised at this level then sending document to first level that's why assigning "level=0" ------ */
                if (isEditedPendingDocumentWithCheckOff && hasAuthority) {
                    level = 0;
                }
            }
        } else {
            hasAuthority = true;
        }
        if (hasAuthority) {
            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = mgr.getEngineByName("JavaScript");
            int approvalStatus = 11;
            String grNumber = gr.getGoodsReceiptNumber();
            String grID = gr.getID();
            HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
            qdDataMap.put("companyid", companyid);
            qdDataMap.put("level", level + 1);
            qdDataMap.put("moduleid", moduleid);
            KwlReturnObject flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(qdDataMap);
            Iterator itr = flowresult.getEntityList().iterator();
            String fromName = "User";
            fromName = gr.getCreatedby().getFirstName().concat(" ").concat(gr.getCreatedby().getLastName());
            /**
             * parameters required for sending mail
             */
            Map<String, Object> mailParameters = new HashMap();
            mailParameters.put(Constants.companyid, companyid);
            mailParameters.put(Constants.prNumber, grNumber);
            mailParameters.put(Constants.fromName, fromName);
            mailParameters.put(Constants.moduleid, Constants.Acc_Vendor_Invoice_ModuleId);
            mailParameters.put(Constants.isCash, false);
            mailParameters.put(Constants.createdBy, gr.getCreatedby().getUserID());
            if (grApproveMap.containsKey(Constants.PAGE_URL)) {
                mailParameters.put(Constants.PAGE_URL, (String) grApproveMap.get(Constants.PAGE_URL));
            }
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                mailParameters.put(Constants.ruleid, row[0].toString());
                HashMap<String, Object> recMap = new HashMap();
//            JSONObject obj = new JSONObject();
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
                }else if(appliedUpon ==Constants.Specific_Products_Category){
                    sendForApproval = accountingHandlerDAOobj.checkForProductCategoryForProduct(productDiscountMapList, appliedUpon, rule);
                } else {
                    rule = rule.replaceAll("[$$]+", amount);
                }
                if (StringUtil.isNullOrEmpty(rule) || (!StringUtil.isNullOrEmpty(rule) && appliedUpon != Constants.Specific_Products && appliedUpon != Constants.Specific_Products_Discount && appliedUpon != Constants.Specific_Products_Category && Boolean.parseBoolean(engine.eval(rule).toString())) || sendForApproval) {
                    // send emails
                    boolean hasApprover = Boolean.parseBoolean(row[3].toString());
                    mailParameters.put(Constants.hasApprover, hasApprover);
                    if (isMailApplicable) {
                        mailParameters.put("level",level);
                        sendMailToApprover(mailParameters);
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
            accGoodsReceiptobj.approvePendinggr(grID, companyid, approvalStatus);
            returnStatus = approvalStatus;
        } else {
            returnStatus = Constants.NoAuthorityToApprove; //if not have approval permission then return one fix value like 999
        }
        returnList.add(returnStatus);
        returnList.add(mailParamList);

        return returnList;

    }
      /**
       * @param mailParameters(String companyid, String ruleId, String prNumber, String fromName, boolean hasApprover, int moduleid, boolean iscash,String createdby, String PAGE_URL)
       * @throws ServiceException 
       */
    public void sendMailToApprover(Map<String, Object> mailParameters) throws ServiceException {
        KwlReturnObject cap = null;
        int level=0;
        if (mailParameters.containsKey(Constants.companyid)) {
            cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), (String) mailParameters.get(Constants.companyid));
        }
        CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
        boolean hasApprover = false;
        boolean iscash = false;
        int moduleid = 0;
        String createdby = "";
        if(mailParameters.containsKey(Constants.createdBy)){
            createdby = (String) mailParameters.get(Constants.createdBy);
        }
        if(mailParameters.containsKey(Constants.moduleid)){
            moduleid = (int) mailParameters.get(Constants.moduleid);
        }
        if(mailParameters.containsKey(Constants.hasApprover)){
            hasApprover = (boolean) mailParameters.get(Constants.hasApprover);
        }
        if(mailParameters.containsKey(Constants.isCash)){
            iscash = (boolean) mailParameters.get(Constants.isCash);
        }
        if(mailParameters.containsKey("level")){
            level = (int) mailParameters.get("level");
        }
        String transactionName="";
        String transactionNo="";
        switch (moduleid) {
            case Constants.Acc_Vendor_Invoice_ModuleId:
                if (iscash) {
                    transactionName = "Cash Purchase";
                    transactionNo="Cash Purchase Number";
                } else {
                    transactionName = "Purchase Invoice";
                    transactionNo = "Purchase Invoice Number";
                }
                break;
            case Constants.Acc_Goods_Receipt_ModuleId:
                transactionName = "Goods Receipt";
                transactionNo = "Goods Receipt Number";
                break;
        }
        String requisitionApprovalSubject = transactionName + " : %s - Approval Notification";
        String requisitionApprovalHtmlMsg = "<html><head><title>Deskera Accounting - Your Deskera Account</title></head><style type='text/css'>"
                + "a:link, a:visited, a:active {\n"
                + " 	color: #03C;"
                + "}\n"
                + "body {\n"
                + "	font-family: Arial, Helvetica, sans-serif;"
                + "	color: #000;"
                + "	font-size: 13px;"
                + "}\n"
                + "</style><body>"
                + "<p>Hi All,</p>"
                + "<p></p>"
                + "<p>%s has created " + transactionName + " %s and sent it to you for approval. at level "+(level+1)+"</p>"
                + "<p>Please review and approve it (" + transactionNo + " : %s).</p>"
                + "<p>Company Name:- %s</p>"
                + "<p>Please check on Url:- %s</p>"
                + "<p></p>"
                + "<p>Thanks</p>"
                + "<p>This is an auto generated email. Do not reply<br>";
        String requisitionApprovalPlainMsg = "Hi All,\n\n"
                + "%s has created " + transactionName + "%s and sent it to you for approval. at level "+(level+1)+"\n"
                + "Please review and approve it (" + transactionNo + " : %s).\n\n"
                + "Company Name:- %s \n"
                + "Please check on Url:- %s \n\n"
                + "Thanks\n\n"
                + "This is an auto generated email. Do not reply\n";
        try {
            if (hasApprover && preferences.isSendapprovalmail()) {//If allow to send approval mail in company account preferences
                KwlReturnObject returnObject = null;
                if(mailParameters.containsKey(Constants.companyid)){
                    returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), (String) mailParameters.get(Constants.companyid));
                }
                Company company = (Company) returnObject.getEntityList().get(0);
                String fromEmailId = (!company.isEmailFromCompanyCreator()) ? Constants.ADMIN_EMAILID : authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                String companyName = company.getCompanyName();
                String subject = "";
                String htmlMsg = "";
                String plainMsg = "";
                if (mailParameters.containsKey(Constants.prNumber) ) {
                     subject = String.format(requisitionApprovalSubject, (String) mailParameters.get(Constants.prNumber));
                }
                if (mailParameters.containsKey(Constants.prNumber) && mailParameters.containsKey(Constants.fromName) && mailParameters.containsKey(Constants.PAGE_URL)) {
                     htmlMsg = String.format(requisitionApprovalHtmlMsg, (String) mailParameters.get(Constants.fromName), (String) mailParameters.get(Constants.prNumber),  (String) mailParameters.get(Constants.prNumber), companyName, (String) mailParameters.get(Constants.PAGE_URL));
                     plainMsg = String.format(requisitionApprovalPlainMsg, (String) mailParameters.get(Constants.fromName), (String) mailParameters.get(Constants.prNumber), (String) mailParameters.get(Constants.prNumber), companyName, (String) mailParameters.get(Constants.PAGE_URL));
                }
                ArrayList<String> emailArray = new ArrayList<String>();
                String[] emails = {};
                String userDepartment=null;
                KwlReturnObject returnObjectRes=null; 
                
                HashMap<String, Object> dataMap=new HashMap<String,Object>();
                if (mailParameters.containsKey(Constants.ruleid)) {
                    dataMap.put(Constants.ruleid, (String) mailParameters.get(Constants.ruleid));
                }
                if(mailParameters.containsKey(Constants.companyid)){
                    dataMap.put(Constants.companyKey, (String) mailParameters.get(Constants.companyid));
                }
                dataMap.put("checkdeptwiseapprover", true);
                
                KwlReturnObject userResult1 = accMultiLevelApprovalDAOObj.checkDepartmentWiseApprover(dataMap);
                if (userResult1 != null && userResult1.getEntityList() != null && userResult1.getEntityList().size() > 0) {
                    User user = null;
                    if (!StringUtil.isNullObject(createdby)) {
                        returnObjectRes = accountingHandlerDAOobj.getObject(User.class.getName(), createdby);
                        user = (User) returnObjectRes.getEntityList().get(0);
                    }
                     if(user!=null && !StringUtil.isNullObject(user.getDepartment())){
                       userDepartment= user.getDepartment();
                       dataMap.put("userdepartment", userDepartment);
                    }
                }
                
                KwlReturnObject userResult = accMultiLevelApprovalDAOObj.getApprovalRuleTargetUsers(dataMap);
                
                if(userResult.getEntityList()!=null && userResult.getEntityList().size()<=0 && !StringUtil.isNullOrEmpty(userDepartment )){
                    dataMap.remove("userdepartment");
                    userResult = accMultiLevelApprovalDAOObj.getApprovalRuleTargetUsers(dataMap);
                }
                Iterator useritr = userResult.getEntityList().iterator();
                while (useritr.hasNext()) {
                    Object[] userrow = (Object[]) useritr.next();
                    emailArray.add(userrow[3].toString());
                }
                emails = emailArray.toArray(emails);
                if (!StringUtil.isNullOrEmpty(preferences.getApprovalEmails())) {
                    String[] compPrefMailIds = preferences.getApprovalEmails().split(",");
                    emails = AccountingManager.getMergedMailIds(emails, compPrefMailIds);
                }
                if (emails.length > 0) {
                    Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                    SendMailHandler.postMail(emails, subject, htmlMsg, plainMsg, fromEmailId, smtpConfigMap);
                }
            }
        } catch (MessagingException ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /* 
     * Handele the Edit And Save For Bank Reconcialtion in Cash Sales
     */

    private void deleteBankReconcilation(Map<String, Object> requestParams) throws ServiceException {
        if (requestParams.containsKey("oldjeid")) {
            String reconsilationID = "";
            String unReconsilationID = "";
            String jeid = (String) requestParams.get("oldjeid");
            String companyid = (String) requestParams.get("companyId");

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
            if (!StringUtil.isNullOrEmpty(reconsilationID)) {
                accBankReconciliationObj.deleteBankReconciliation(reconsilationID, companyid);
            }
            if (!StringUtil.isNullOrEmpty(unReconsilationID)) {
                accBankReconciliationObj.deleteBankReconciliation(unReconsilationID, companyid);
            }
        }
    }

    private void saveBankReconsilation(Map<String, Object> requestParams, Map<String, Object> globalParams) throws ServiceException, JSONException, UnsupportedEncodingException {
        HashMap<String, Object> brMap = new HashMap<String, Object>();
        KwlReturnObject crresult = accCurrencyDAOobj.getCurrencyToBaseAmount(globalParams, (Double) requestParams.get("clearingamount"), (String) requestParams.get("currencyid"), (Date) requestParams.get("clearanceDate"), 0);
        double clearingAmount = (Double) crresult.getEntityList().get(0);

        if (requestParams.containsKey("oldjeid")) {
            deleteBankReconcilation(requestParams);
        }

        brMap.put("startdate", (Date) requestParams.get("startDate"));
        brMap.put("enddate", (Date) requestParams.get("endDate"));
        brMap.put("clearanceDate", (Date) requestParams.get("clearanceDate"));
        brMap.put("clearingamount", (0-clearingAmount));
        brMap.put("endingamount", (Double) requestParams.get("endingAmount"));
        brMap.put("accountid", (String) requestParams.get("bankAccountId"));
        brMap.put("companyid", (String) requestParams.get("companyId"));
        brMap.put("checkCount",1);
        brMap.put("depositeCount", 0);
        brMap.put("createdby", (String) requestParams.get("createdby"));
        GoodsReceipt invoice = (GoodsReceipt) requestParams.get("GoodsReceipt");
        JournalEntry entry = invoice.getJournalEntry();

        Set details = entry.getDetails();
        Iterator iter = details.iterator();
        String accountName = "";
        while (iter.hasNext()) {
            JournalEntryDetail d = (JournalEntryDetail) iter.next();
            if (!d.isDebit()) {
                continue;
            }
            accountName += d.getAccount().getName() + ", ";
        }
        accountName = accountName.substring(0, Math.max(0, accountName.length() - 2));


        KwlReturnObject brresult = accBankReconciliationObj.addBankReconciliation(brMap);
        BankReconciliation br = (BankReconciliation) brresult.getEntityList().get(0);
        String brid = br.getID();
        HashSet hs = new HashSet();
        HashMap<String, Object> brdMap = new HashMap<String, Object>();
        brdMap.put("companyid", (String) requestParams.get("companyId"));
        brdMap.put("amount", clearingAmount);
        brdMap.put("jeid", entry.getID());
        brdMap.put("accountname", accountName);
        brdMap.put("debit", false);
        brdMap.put("brid", brid);
        KwlReturnObject brdresult1 = accBankReconciliationObj.addBankReconciliationDetail(brdMap);
        BankReconciliationDetail brd1 = (BankReconciliationDetail) brdresult1.getEntityList().get(0);
        hs.add(brd1);
    }
    
//    public ModelAndView importPurchaseInvoices(HttpServletRequest request, HttpServletResponse response) {
//        JSONObject jobj = new JSONObject();
//        String msg = "";
//        boolean issuccess = false;
//        try {
//            JSONObject paramJobj = null;
//            String doAction = request.getParameter("do");
//            if (doAction.compareToIgnoreCase("import") == 0) {
//                paramJobj = getImportPurchaseInvoiceParams(request);
//                ImportPurchaseInvoiceobj.add(paramJobj);
//                if (!ImportPurchaseInvoiceobj.isIsworking()) {
//                    Thread t = new Thread(ImportPurchaseInvoiceobj);
//                    t.setPriority(7);
//                    t.start();
//                }
//                jobj.put("exceededLimit", "yes");
//                jobj.put("success", true);
//            } else if (doAction.compareToIgnoreCase("validateData") == 0) {
//                paramJobj = getImportPurchaseInvoiceParams(request);
//                String eParams = paramJobj.optString("extraParams", "");
//                JSONObject extraParams = StringUtil.isNullOrEmpty(eParams) ? new JSONObject() : new JSONObject(eParams);
//                HashMap<String, Object> requestParams = importHandler.getImportRequestParams(paramJobj);
//                requestParams.put("extraParams", extraParams);
//                requestParams.put("extraObj", null);
//                requestParams.put("servletContext", paramJobj.get("servletContext"));
//
//                jobj = importHandler.validateFileData(requestParams);
//                jobj.put("success", true);
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

    public ModelAndView importExpenceInvoices(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            /*
             Get parametrs from Request Objet
             */
            JSONObject paramJobj = getImportPurchaseInvoiceParams(request);
            /*
             Call to Import Expense Cash Purchase Invoice function.
             */
            jobj = accGoodsReceiptModuleService.importExpenseInvoiceJSON(paramJobj);
        } catch (Exception ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getImportPurchaseInvoiceParams(HttpServletRequest request) throws JSONException, SessionExpiredException {
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        paramJobj.put("servletContext", this.getServletContext());
        paramJobj.put("baseUrl", paramJobj.optString(Constants.PAGE_URL));
        paramJobj.put("locale", RequestContextUtils.getLocale(request));
        paramJobj.put(Constants.df, authHandler.getDateOnlyFormat(request));
        return paramJobj;
    }

    /**
     * This Method is used to Import Delivery Orders
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView importGoodsReceiptOrders(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            /* Get Import related global parameters */
            JSONObject paramJobj = getGoodsReceiptOrdersParams(request);
            /* Call validate and import data of GRO. */
            jobj = accGoodsReceiptModuleService.importGoodsReceiptOrdersJSON(paramJobj);
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    /**
     * Description : This Method is used to Get Request params for import Delivery Order
     * @param request
     * @return JSONObject
     * @throws JSONException
     * @throws SessionExpiredException 
     */
    public JSONObject getGoodsReceiptOrdersParams(HttpServletRequest request) throws JSONException, SessionExpiredException {
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        paramJobj.put("servletContext", this.getServletContext());
        return paramJobj;
    }
    /**
     * Description : This Method is used to Get Request params for To check Landing cost of category is allocated the method in Master Item
     * @param request
     * @param response 
     * @return ModelAndView     
     */
    public ModelAndView checkLCCategoryIsalloctedInMasterConfig(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String view = "jsonView";
        try {
            String lccId = request.getParameter("lccId");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            if (!StringUtil.isNullOrEmpty(lccId)) {
                String lccategory = lccId;
                KwlReturnObject returnObj = accMasterItemsDAOobj.getMasterItemFromLandingCostCategory(lccategory , companyid);
                issuccess=true;
                jobj.put("data","");
                jobj.put("count",returnObj.getRecordTotalCount());
                jobj.put("success", issuccess);
            }
        } catch (JSONException | SessionExpiredException | ServiceException ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    /**
     * Description : This Method is used to Get Request params for Landed Item report.
     * @param request
     * @param response 
     * @return ModelAndView
     */
    // Landed Cost Item Report
    public ModelAndView getLandingCostItemReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String view = "jsonView";
        String msg = "";
        int totalCount = 0;
        try {
            HashMap<String, Object> requestMapData=new HashMap<String,Object>();
            String companyid=sessionHandlerImpl.getCompanyid(request);
            DateFormat userdf = authHandler.getDateOnlyFormat(request);
            KwlReturnObject extraPref = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extrareferences = (ExtraCompanyPreferences) extraPref.getEntityList().get(0);
            JSONObject paramJobj = getGoodsReceiptOrdersParams(request);            
            Iterator<String> keyItr=paramJobj.keys();
            while(keyItr.hasNext()){
                String key= keyItr.next();
                requestMapData.put(key,paramJobj.get(key));
            }
            if(!StringUtil.isNullOrEmpty(companyid)){
                requestMapData.put("companyId",companyid);
            }
            if(!StringUtil.isNullObject(userdf)){
                requestMapData.put("df",userdf);
            }
            jobj=accGoodsReceiptModuleService.getLandingCostItemReport(requestMapData, companyid);
            issuccess=true;
        } catch (Exception ex) {
            issuccess = false;
            msg = "accGoodsReceiptController.getLandingCostItemReport : " + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    /**
     * Description : This Method is used to Get Request params for Landed Item report.
     * @param request
     * @param response 
     * @return ModelAndView
     */
    // Landed Cost Item Report
    public ModelAndView createLandingCostItemConfig(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String view = "jsonView";
        String msg = "";
        int totalCount = 0;
        try {
            HashMap<String, Object> requestMapData=new HashMap<String,Object>();
            String companyid=sessionHandlerImpl.getCompanyid(request);
            DateFormat userdf = authHandler.getDateOnlyFormat(request);
            KwlReturnObject extraPref = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extrareferences = (ExtraCompanyPreferences) extraPref.getEntityList().get(0);
            JSONObject paramJobj = getGoodsReceiptOrdersParams(request);            
            Iterator<String> keyItr=paramJobj.keys();
            while(keyItr.hasNext()){
                String key= keyItr.next();
                requestMapData.put(key,paramJobj.get(key));
            }
            if(!StringUtil.isNullOrEmpty(companyid)){
                requestMapData.put("companyId",companyid);
            }
            if(!StringUtil.isNullObject(userdf)){
                requestMapData.put("df",userdf);
            }
            jobj=accGoodsReceiptModuleService.createLandingCostItemConfig(requestMapData, companyid);
            issuccess=true;
        } catch (Exception ex) {
            issuccess = false;
            msg = "accGoodsReceiptController.getLandingCostItemReport : " + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
    /*
     *Create Purchase invoices from selected PO    
     1.Individual invoices per PO
     2.Bulk invoices per Vendor
     */
    public ModelAndView saveBulkInvoicesFromPO(HttpServletRequest request, HttpServletResponse response) {
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
            String pendingstatus="";
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Purchase_Order_ModuleId));
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            String userId = sessionHandlerImpl.getUserid(request);
            boolean iscash = StringUtil.isNullOrEmpty(request.getParameter("incash")) ? false : Boolean.parseBoolean(request.getParameter("incash"));
            String jeid = null;
            double discValue = 0.0;

            GoodsReceipt goodsreceipt = null;
            String nextAutoNumber = "";
            String purchaseOrderId = "";
            String invoiceNo = "";

            List purchaseOrderIdArray = new ArrayList();
            List purchaseOrderNoArray = new ArrayList();

            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);

            HashMap<String, JSONArray> hm = new HashMap<String, JSONArray>();

            /* 
             Preparing a map with Key Vendor ID and Value -Json Array
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

            /* Iterating Map on key Vendor ID*/
            for (String personIdKey : hm.keySet()) {

                JSONArray js = hm.get(personIdKey);
                try {
                    /* Opening transaction while creating bulk invoices*/
                    if (bulkInvoices) {
                        status = txnManager.getTransaction(def);
                    }

                    for (int i = 0; i < js.length(); i++) {

                        if (!bulkInvoices) {
                            /* Opening transaction while creating individual invoices*/
                            status = txnManager.getTransaction(def);
                        }

                        try {
                            JSONObject jobData = js.getJSONObject(i);
                            if (bulkInvoices) {
                                purchaseOrderIdArray.clear();
                                purchaseOrderNoArray.clear();
                                for (int k = 0; k < js.length(); k++) {
                                    JSONObject jobTempData = js.getJSONObject(k);
                                    purchaseOrderIdArray.add(jobTempData.getString("billid"));
                                    purchaseOrderNoArray.add(jobTempData.getString("billno"));
                                }
                            }

                            DateFormat df = authHandler.getDateOnlyFormat(request);
                            HashMap<String, Object> dataMap = new HashMap<String, Object>();

                            purchaseOrderId = jobData.getString("billid");
                            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), purchaseOrderId);
                            PurchaseOrder purchaseOrderObj = (PurchaseOrder) rdresult.getEntityList().get(0);

                            String venID = purchaseOrderObj.getVendor().getID();
                            String custWarehouse = "";
                            String createdby = sessionHandlerImpl.getUserid(request);
                            String modifiedby = sessionHandlerImpl.getUserid(request);
                            long createdon = System.currentTimeMillis();
                            long updatedon = System.currentTimeMillis();

                            String taxid = purchaseOrderObj.getTax() != null ? purchaseOrderObj.getTax().getID() : "";
                            String costCenterId = "";
                            costCenterId = purchaseOrderObj.getCostcenter() == null ? "" : purchaseOrderObj.getCostcenter().getID();
                            double externalCurrencyRate = purchaseOrderObj.getExternalCurrencyRate();
                            Discount discount = null;

                            String currencyid = (purchaseOrderObj.getCurrency() != null ? purchaseOrderObj.getCurrency().getCurrencyID() : sessionHandlerImpl.getCurrencyID(request));
                            Vendor vendor = new Vendor();
                            String accountId = "";
                            Map<String, Object> oldInvoicePrmt = new HashMap<String, Object>();
                            Map<String, Object> invoicePrmt = new HashMap<String, Object>();
                            Map<String, Object> newAuditKey = new HashMap<String, Object>();
                            KwlReturnObject venresult = null;

                            if (!StringUtil.isNullOrEmpty(venID)) {
                                venresult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), venID);
                                vendor = (Vendor) venresult.getEntityList().get(0);
                                if (vendor.getAccount() != null) {
                                    accountId = vendor.getAccount().getID();
                                }
                            }

                            if (!StringUtil.isNullOrEmpty(accountId)) {
                                invoicePrmt.put("accountid", accountId);
                            }
                            if (!StringUtil.isNullOrEmpty(venID)) {
                                invoicePrmt.put(VENDORID, venID);
                            }
                            double taxamount = 0;
                            invoicePrmt.put("autogenerated", true);

                            //Entry No generate
                            int from = StaticValues.AUTONUM_GOODSRECEIPT;

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

                            BillingShippingAddresses addresses = purchaseOrderObj.getBillingShippingAddresses();
                            invoicePrmt.put(ENTRYNUMBER, nextAutoNumber);
                            invoicePrmt.put("seqnumber", nextAutoNoInt);
                            invoicePrmt.put(Constants.DATEPREFIX, datePrefix);
                            invoicePrmt.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
                            invoicePrmt.put(Constants.DATESUFFIX, dateSuffix);
                            invoicePrmt.put("seqformat", sequenceformatInvoice);
                            invoicePrmt.put("isfavourite", purchaseOrderObj.isFavourite());
//                            invoicePrmt.put(termid, jobData.getString("termid"));
//                            invoicePrmt.put("agent", jobData.getString("agent"));
//                            invoicePrmt.put("supplierinvoiceno", jobData.getString("supplierinvoiceno"));
                            invoicePrmt.put("shipaddress", addresses == null ? "" : addresses.getShippingAddress());

                            if (!(purchaseOrderIdArray.size() > 1 && bulkInvoices)) {
                                invoicePrmt.put("shipvia", purchaseOrderObj.getShipvia());
                                invoicePrmt.put("fob", purchaseOrderObj.getFob());
//                                invoicePrmt.put("salesPerson", purchaseOrderObj.getSalesperson() != null ? purchaseOrderObj.getSalesperson().getID() : "");
                                invoicePrmt.put(MEMO, purchaseOrderObj.getMemo());
                                if (purchaseOrderObj.getShipdate() != null) {
                                    invoicePrmt.put(SHIPDATE, purchaseOrderObj.getShipdate());
                                }
                            } else {
                                invoicePrmt.put("shipvia", "");
                                invoicePrmt.put("fob", "");
                                invoicePrmt.put("salesPerson", "");
                                invoicePrmt.put(MEMO, "");
                            }
                            invoicePrmt.put("companyid", companyid);
                            invoicePrmt.put("currencyid", currencyid);
                            invoicePrmt.put("externalCurrencyRate", externalCurrencyRate);
                            invoicePrmt.put("createdby", createdby);
                            Calendar c = Calendar.getInstance();
                            c.setTime(new Date());
                            String dt = df.format(c.getTime());

                            invoicePrmt.put(DUEDATE, authHandler.getDateOnlyFormat(request).parse(dt));
                            invoicePrmt.put("modifiedby", modifiedby);
                            invoicePrmt.put("createdon", createdon);
                            invoicePrmt.put("updatedon", updatedon);
                            if (!StringUtil.isNullOrEmpty(custWarehouse)) {
                                invoicePrmt.put("custWarehouse", custWarehouse);
                            }
                            Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
                            Calendar c1 = Calendar.getInstance();
                            c1.setTime(new Date());
                            String dt1 = df.format(c1.getTime());
                            Date entryDate = authHandler.getDateOnlyFormat(request).parse(dt1);

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
                            jeDataMap.put(Constants.memo, purchaseOrderObj.getMemo());

                            jeDataMap.put(ENTRYDATE, entryDate);
                            jeDataMap.put("companyid", companyid);
                            jeDataMap.put("createdby", createdby);
                            jeDataMap.put("currencyid", currencyid);
                            jeDataMap.put("costcenterid", costCenterId);
                            jeDataMap.put("transactionModuleid", Constants.Acc_Vendor_Invoice_ModuleId);//For journal Entry Type

                            HashSet jeDetails = new HashSet();
                            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
                            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
                            jeid = journalEntry.getID();
                            invoicePrmt.put("journalentryid", jeid);
                            jeDataMap.put("jeid", jeid);

                            if (bulkInvoices) {
                                /*Fetching Purchase order details for all selected PO per Vendor */
                                result = accPurchaseOrderobj.getPurchaseOrderDetailsForBulkInvoices(purchaseOrderIdArray, companyid);
                            } else {
                                /*Fetching Purchase order details per PO*/
                                result = accPurchaseOrderobj.getPurchaseOrderDetails(purchaseOrderId, companyid);
                            }

                            /* Function to save Invoice details*/
                            List dll = saveInvoiceDetail(request, result, companyid, jobData, jeDetails, jeid);
                            double[] totals = (double[]) dll.get(0);
                            JSONObject discjson = new JSONObject();
                            discjson.put("originalamount", totals[1] - totals[0] + totals[2]);
                            discjson.put("companyid", companyid);
                            KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                            discount = (Discount) dscresult.getEntityList().get(0);
                            invoicePrmt.put("discountid", discount.getID());
                            discValue = discount.getDiscountValue();
                            HashSet<GoodsReceiptDetail> goodsreceiptdetails = (HashSet<GoodsReceiptDetail>) dll.get(1);
                            ArrayList<String> prodList = (ArrayList<String>) dll.get(2);
                            discValue += totals[0];

                            taxamount = totals[2];
                            double totalInvAmount = totals[1] - discValue;//totalamount - discount
                            boolean gstIncluded = purchaseOrderObj.isGstIncluded();
                            /* 
                             *If "including GST" option true then tax amount is not added in total invoice amount
                             */
                            if (!gstIncluded) {
                                totalInvAmount += totals[2];
                            }

                            /*
                             * If invoice terms applied then add mapping against invoice
                             */
                            double termTotalAmount = 0;
                            /*  Get Term details applied on Purchase Order*/
                            JSONArray termsArr = getTermDetailsForPurchaseOrder(purchaseOrderObj.getID());
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
                                List taxTermMapping = accGoodsReceiptobj.getTerms(taxid);
                                double taxPercent = 0;
                                if (taxTermMapping != null && taxTermMapping.size() > 0) {
                                    KwlReturnObject taxresult = accTaxObj.getTaxPercent(sessionHandlerImpl.getCompanyid(request), purchaseOrderObj.getOrderDate(), taxid);
                                    taxPercent = (Double) taxresult.getEntityList().get(0);
                                    taxamount = ((totals[1] - totals[0] + termTotalAmount) * taxPercent) / 100;
                                    totalInvAmount = totalInvAmount - totals[2] + taxamount;

                                }
                            }

                            /* Adding term amoount in total Amount of invoice */
                            totalInvAmount += termTotalAmount;

                            invoicePrmt.put(Constants.invoiceamountdue, totalInvAmount);
                            Date creationDate = (Date) authHandler.getDateOnlyFormat(request).parse(dt1);
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
                            jedjson.put("debit", false);
                            jedjson.put("jeid", jeid);
                            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jeDetails.add(jed);
                            invoicePrmt.put(VENDORENTRYID, jed.getID());

                            /* Saving Jedetails for discount*/
                            if (discValue > 0) {
                                jedjson = new JSONObject();
                                jedjson.put("srno", jeDetails.size() + 1);
                                jedjson.put(Constants.companyKey, companyid);
                                jedjson.put("amount", discValue);
                                jedjson.put("accountid", preferences.getDiscountReceived().getID());
                                jedjson.put("debit", false);
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
                                    jedjson.put("debit", entry.getValue() > 0 ? true : false);
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

                            /* 
                             *Add global level "tax amount" and "tax amount in base" value required to show invoice in GST report
                             */
                            invoicePrmt.put("taxAmount", taxamount);
                            KwlReturnObject baseAmountObj = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, taxamount, currencyid, purchaseOrderObj.getOrderDate(), externalCurrencyRate);
                            taxAmountinBase = (Double) baseAmountObj.getEntityList().get(0);
                            taxAmountinBase = authHandler.round(taxAmountinBase, companyid);
                            invoicePrmt.put("taxAmountInBase", taxAmountinBase);
                            excludingGstAmountInBase = totals[1] - discValue;

                            if (gstIncluded) {
                                excludingGstAmountInBase = totals[1] - totals[2] - discValue;
                                excludingGstAmountInBase += totals[3];//LIne level Term Amount
                            }
                            invoicePrmt.put("excludingGstAmount", excludingGstAmountInBase);
                            baseAmountObj = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, excludingGstAmountInBase, currencyid, purchaseOrderObj.getOrderDate(), externalCurrencyRate);
                            excludingGstAmountInBase = (Double) baseAmountObj.getEntityList().get(0);
                            excludingGstAmountInBase = authHandler.round(excludingGstAmountInBase, companyid);
                            invoicePrmt.put("excludingGstAmountInBase", excludingGstAmountInBase);

                            JSONObject invjson = new JSONObject();
                            Set<String> set = invoicePrmt.keySet();
                            for (String key : set) {
                                invjson.accumulate(key, invoicePrmt.get(key));
                            }
                            invjson.put(Constants.Checklocktransactiondate, request.getParameter("billdate"));
                            if (!StringUtil.isNullOrEmpty(request.getParameter("billdate"))) {
                                invjson.put("creationDate", df.parse((String) request.getParameter("billdate")));
                            }
                            invoicePrmt.put("creationDate", entryDate);

                            result = accGoodsReceiptobj.addGoodsReceipt(invoicePrmt);
                            goodsreceipt = (GoodsReceipt) result.getEntityList().get(0);//Create Invoice without invoice-details.
                            
                            List approvedlevel = null;
                            String currentUser = sessionHandlerImpl.getUserid(request);
                            HashMap<String, Object> grvApproveMap = new HashMap<String, Object>();
                            int approvalStatusLevel = 11;
                            int level = goodsreceipt.getApprovestatuslevel();
                            grvApproveMap.put("companyid", sessionHandlerImpl.getCompanyid(request));
                            grvApproveMap.put("level", level);
                            grvApproveMap.put("totalAmount", String.valueOf(authHandler.round(totalInvAmountinBase, companyid)));
                            grvApproveMap.put("currentUser", currentUser);
                            grvApproveMap.put("fromCreate", true);
                            grvApproveMap.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);
                            grvApproveMap.put("iscash", iscash);
                            approvedlevel = approvegr(goodsreceipt, grvApproveMap, false);
                            approvalStatusLevel = (Integer)approvedlevel.get(0);
                            
                            invoicePrmt.put("approvalstatuslevel", approvalStatusLevel);
                            int pendingApprovalFlag = 0;
                            
                            if(approvalStatusLevel!=11){
                                pendingApprovalFlag=1;
                                pendingstatus = " "+messageSource.getMessage("acc.field.butVendorInvoicePendingForApproval", null, RequestContextUtils.getLocale(request)); // " but Vendor Invoice pending for Approval.";
                            }else{
                                pendingApprovalFlag=0;
                            }

                            invoicePrmt.put("invoiceid", goodsreceipt.getID());
                            invoicePrmt.put("pendingapproval", pendingApprovalFlag);
                            
                            /*
                             * If invoice terms applied then add mapping against invoice
                             */
                            if (StringUtil.isAsciiString(InvoiceTerms) && !gstIncluded) {
                                mapInvoiceTerms(InvoiceTerms, goodsreceipt.getID(), sessionHandlerImpl.getUserid(request), false);
                            }
                            
                            /*
                             * If invoice terms applied then add mapping against invoice
                             */
                            JSONObject obj = new JSONObject();
                            /* Getting Purchase Order Custom/Dimension field*/
                            Map<String, Object> variableMap = new HashMap<String, Object>();
                            PurchaseOrderCustomData jeDetailCustom = (PurchaseOrderCustomData) purchaseOrderObj.getPoCustomData();
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

                            /* 
                             *Write a function to fetch custom field from fieldparams against fieldlabel where moduleid will be invoice  
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

                                requestParams1.put(Constants.filter_values, Arrays.asList(fieldLabel, companyid, Constants.Acc_Vendor_Invoice_ModuleId));

                                /* Fetching custom/dimension field "field Label" wise */
                                List result1 = accAccountDAOobj.getFieldParamsFieldLabelWise(requestParams1); // get custom field module wise from fieldlabel
                                if (result1 != null && result1.size() > 0) {

                                    fieldParams = (FieldParams) result1.get(0);

                                    jsonObject.put("fieldid", fieldParams.getId());

                                    /* If multiselect drop down then fetching actual value instead of id */
                                    if (fieldParams.getFieldtype() == Constants.MULTISELECTCOMBO || fieldParams.getFieldtype() == Constants.FIELDSET) {

                                        HashMap<String, Object> fieldParamsMap = new HashMap<String, Object>();
                                        fieldParamsMap.put("companyid", companyid);
                                        fieldParamsMap.put("moduleid", Constants.Acc_Purchase_Order_ModuleId);
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
                                customrequestParams.put(Constants.moduleid, Constants.Acc_Vendor_Invoice_ModuleId);
                                customrequestParams.put(Constants.companyKey, companyid);
                                customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                    jeDataMap.put("accjecustomdataref", journalEntry.getID());
                                    jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);
                                }

                            }

                            Iterator itr = goodsreceiptdetails.iterator();
                            while (itr.hasNext()) {
                                GoodsReceiptDetail ivd = (GoodsReceiptDetail) itr.next();
                                if (ivd.getInventory().isInvrecord()) {
                                    Inventory invtry = ivd.getInventory();
                                    invtry.setActquantity(invtry.getQuantity());
                                    invtry.setQuantity(0);
                                }
                                ivd.setGoodsReceipt(goodsreceipt);
                            }
                            invoicePrmt.put(GRID, goodsreceipt.getID());

                            invoicePrmt.put("gstIncluded", gstIncluded);

                            invoicePrmt.put(GRDETAILS, goodsreceiptdetails);

                            result = accGoodsReceiptobj.updateGoodsReceipt(invoicePrmt);

                            JSONObject jeJobj = new JSONObject();
                            HashSet<JournalEntryDetail> details = new HashSet<JournalEntryDetail>();
                            if (approvalStatusLevel == 11) {
                                jeJobj.put("pendingapproval", 0);
                            }
                            jeJobj.put("jeid", jeid);
                            jeJobj.put(JournalEntryConstants.COMPANYID, companyid);
                            jeJobj.put("transactionId", goodsreceipt.getID());
                            accJournalEntryobj.updateJournalEntry(jeJobj, details);

                            if (bulkInvoices) {
                                for (int ii = 0; ii < purchaseOrderIdArray.size(); ii++) {
                                    /* Updating Link Flag & open flag of PO  */

                                    KwlReturnObject rdresult1 = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), purchaseOrderIdArray.get(ii).toString());
                                    PurchaseOrder purchaseOrderObj1 = (PurchaseOrder) rdresult1.getEntityList().get(0);
                                    hMap.put("purchaseOrder", purchaseOrderObj1);
                                    hMap.put("value", "1");
                                    hMap.put("isOpen", false);
                                    accGoodsReceiptobj.updatePOLinkflag(hMap);
                                }
                            } else {
                                /* Updating Link Flag & open flag of PO  */

                                hMap.put("purchaseOrder", purchaseOrderObj);
                                hMap.put("value", "1");
                                hMap.put("isOpen", false);
                                accGoodsReceiptobj.updatePOLinkflag(hMap);
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

                                for (int l = 0; l < purchaseOrderNoArray.size(); l++) {
                                    /*
                                     * saving linking informaion of Purchase Order while
                                     * linking with Purchase Invoice
                                     */
                                    HashMap<String, Object> requestParamsLinking = new HashMap<String, Object>();
                                    requestParamsLinking.put("linkeddocid", goodsreceipt.getID());
                                    requestParamsLinking.put("docid", purchaseOrderObj.getID());
                                    requestParamsLinking.put(Constants.moduleid, Constants.Acc_Vendor_Invoice_ModuleId);
                                    requestParamsLinking.put("linkeddocno", nextAutoNumber);
                                    requestParamsLinking.put("sourceflag", 0);
                                    accPurchaseOrderobj.savePOLinking(requestParamsLinking);
                                    /*
                                     * saving linking informaion of Purchase Invoice while
                                     * linking with Purchase Order
                                     */
                                    requestParamsLinking.put("linkeddocid", purchaseOrderIdArray.get(l));
                                    requestParamsLinking.put("docid", goodsreceipt.getID());
                                    requestParamsLinking.put(Constants.moduleid, Constants.Acc_Purchase_Order_ModuleId);
                                    requestParamsLinking.put("linkeddocno", purchaseOrderNoArray.get(l));
                                    requestParamsLinking.put("sourceflag", 1);
                                    accGoodsReceiptobj.saveGRLinking(requestParamsLinking);
                                }

                            } else {
                                /*
                                 * saving linking informaion of purchase Order while
                                 * linking with purchase Invoice
                                 */
                                HashMap<String, Object> requestParamsLinking = new HashMap<String, Object>();
                                requestParamsLinking.put("linkeddocid", goodsreceipt.getID());
                                requestParamsLinking.put("docid", purchaseOrderObj.getID());
                                requestParamsLinking.put(Constants.moduleid, Constants.Acc_Vendor_Invoice_ModuleId);
                                requestParamsLinking.put("linkeddocno", nextAutoNumber);
                                requestParamsLinking.put("sourceflag", 0);
                                accPurchaseOrderobj.savePOLinking(requestParamsLinking);


                                /*
                                 * saving linking informaion of Purchase Invoice while
                                 * linking with Purchase Order
                                 */
                                requestParamsLinking.put("linkeddocid", purchaseOrderObj.getID());
                                requestParamsLinking.put("docid", goodsreceipt.getID());
                                requestParamsLinking.put(Constants.moduleid, Constants.Acc_Purchase_Order_ModuleId);
                                requestParamsLinking.put("linkeddocno", purchaseOrderObj.getPurchaseOrderNumber());
                                requestParamsLinking.put("sourceflag", 1);
                                accGoodsReceiptobj.saveGRLinking(requestParamsLinking);
                            }

                            /* 
                             *Leaving from inner loop as only one invoice(in case of bulk) per vendor 
                             *irrespective of the no of selected PO
                             */
                            if (bulkInvoices) {
                                break;
                            }
                            /* If creating individual Invoices per PO */
                            if (!bulkInvoices) {
                                /* Committing transaction while creating individual invoices*/
                                txnManager.commit(status);
                            }

                        } catch (Exception ex) {
                            if (status != null) {
                                txnManager.rollback(status);
                            }
                            msg = "" + ex.getMessage();
                            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                        }
                    }
                    /* If creating bulk Invoices per vendor */
                    if (bulkInvoices) {
                        /* Committing transaction while creating bulk invoices*/
                        txnManager.commit(status);
                    }
                } catch (Exception ex) {
                    if (status != null) {
                        txnManager.rollback(status);
                    }
                    msg = "" + ex.getMessage();
                    Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
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
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }

            msg = "Invoice generated successfully.<br><b> Invoice No:</b> " + "<font style=\"word-break: break-all;\">" + invoiceNo + "." + "</font>"+pendingstatus;
        } catch (SessionExpiredException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return new ModelAndView("jsonView", "model", jobj.toString());

    }
   /*
    *Function for getting term details of Purchase Order
    */
    public JSONArray getTermDetailsForPurchaseOrder(String id) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            HashMap<String, Object> requestParam = new HashMap();
            requestParam.put("purchaseOrder", id);
            KwlReturnObject curresult = accPurchaseOrderobj.getPurchaseOrderTermMap(requestParam);
            List<PurchaseOrderTermMap> termMap = curresult.getEntityList();
            for (PurchaseOrderTermMap PurchaseOrderTermMap : termMap) {
                InvoiceTermsSales mt = PurchaseOrderTermMap.getTerm();
                JSONObject jsonobj = new JSONObject();
                jsonobj.put("id", mt.getId());
                jsonobj.put("term", mt.getTerm());
                jsonobj.put("glaccount", mt.getAccount().getID());
                jsonobj.put("sign", mt.getSign());
                jsonobj.put("formula", mt.getFormula());
                jsonobj.put("termpercentage", PurchaseOrderTermMap.getPercentage());
                jsonobj.put("termamount", PurchaseOrderTermMap.getTermamount());
                jArr.put(jsonobj);
            }

        } catch (JSONException ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
    
    /*Save invoice details while creating bulk or individual invoices from PO */
    
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
                /* Iterating PO details to save in invoice details*/
                for (int i = 0; i < result.getEntityList().size(); i++) {
                    PurchaseOrderDetail purchaseOrderDetail = (PurchaseOrderDetail) result.getEntityList().get(i);
                    /* Manually Closed line item of PO is not included in invoice */
                    if (purchaseOrderDetail.isIsLineItemClosed()) {
                        continue;
                    }
                    double quantity = getPurchaseOrderDetailStatus(purchaseOrderDetail);
                    if (quantity == 0) {
                        continue;
                    }
                    amount = purchaseOrderDetail.getRate() * quantity;
                    GoodsReceiptDetail row = new GoodsReceiptDetail();
                    double discountPercent = purchaseOrderDetail.getDiscountispercent();
                    double discountValue = 0;
                    if (discountPercent == 0) { // Flat discount
                        discountValue = purchaseOrderDetail.getDiscount();
                    } else { // Percentage discount
                        discountValue = purchaseOrderDetail.getDiscount() / 100;
                        discountValue = amount * discountValue;
                    }

                    double taxAmt = 0;
                    double taxPercent = 0;
                    double rowTaxPercent = 0;
                    /* Do check line & global taxes to identify to execute if else block of code*/
                    if (purchaseOrderDetail.getTax() != null) {//Line Level Tax
                        taxAmt = (purchaseOrderDetail.getRowTaxAmount());
                        KwlReturnObject taxresult = accTaxObj.getTaxPercent(sessionHandlerImpl.getCompanyid(request), purchaseOrderDetail.getPurchaseOrder().getOrderDate(), purchaseOrderDetail.getTax().getID());
                        taxPercent = (Double) taxresult.getEntityList().get(0);
                        rowTaxPercent = taxPercent;
                    } else {
                        if (purchaseOrderDetail.getPurchaseOrder().getTax() != null) {
                            KwlReturnObject taxresult = accTaxObj.getTaxPercent(sessionHandlerImpl.getCompanyid(request), purchaseOrderDetail.getPurchaseOrder().getOrderDate(), purchaseOrderDetail.getPurchaseOrder().getTax().getID());
                            taxPercent = (Double) taxresult.getEntityList().get(0);

                        }
                        taxAmt = (taxPercent == 0 ? 0 : authHandler.round(((amount - discountValue) * taxPercent / 100), companyId));
                    }
                    Tax rowtax = null;
                    double rowtaxamount = 0d;
                    JournalEntryDetail jed;
                    row.setSrno(purchaseOrderDetail.getSrno());
                    row.setWasRowTaxFieldEditable(true);// this field has been added after making row tax field editable its value will be set always true from its implementation. i.e from 28-jan-2014.REASON -  when Row Tax Amount field was not editable then tax calculation was taking place according to tax percent, as selected From Tax combo in JS Side.
                    row.setPriceSource(!StringUtil.isNullOrEmpty(purchaseOrderDetail.getPriceSource()) ?  StringUtil.DecodeText(purchaseOrderDetail.getPriceSource()) : "");

                    boolean includingGST = false;
                    /* Set rateincludegst for invoicedetail */

                    if (purchaseOrderDetail.getRateincludegst() != 0) {
                        row.setRateincludegst(purchaseOrderDetail.getRateincludegst());
                        includingGST = true;

                    }

                    /*  Setting Line level Term amount in case of Including GSt true */
                    if (includingGST) {
                        if (purchaseOrderDetail.getLineLevelTermAmount() != 0) {
                            lineLevelTermAmount += purchaseOrderDetail.getLineLevelTermAmount();
                            row.setLineLevelTermAmount(purchaseOrderDetail.getLineLevelTermAmount());
                        }
                    }

                    if (!StringUtil.isNullOrEmpty(purchaseOrderDetail.getDescription())) {
                        try {
                            row.setDescription( StringUtil.DecodeText(purchaseOrderDetail.getDescription()));
                        } catch (Exception ex) {
                            row.setDescription(purchaseOrderDetail.getDescription());
                        }
                    }

                    KwlReturnObject prdresult = accountingHandlerDAOobj.getObject(Product.class.getName(), purchaseOrderDetail.getProduct().getID());
                    Product product = (Product) prdresult.getEntityList().get(0);

                    if (!StringUtil.isNullOrEmpty(purchaseOrderDetail.getInvstoreid())) {
                        row.setInvstoreid(purchaseOrderDetail.getInvstoreid());
                    } else {
                        row.setInvstoreid("");
                    }

                    if (!StringUtil.isNullOrEmpty(purchaseOrderDetail.getInvlocid())) {
                        row.setInvlocid(purchaseOrderDetail.getInvlocid());
                    } else {
                        row.setInvlocid("");
                    }
                    row.setPurchaseorderdetail(purchaseOrderDetail);
                    boolean updateInventoryFlag = (preferences.isWithInvUpdate()) ? false : true;

                    KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
                    Company company = (Company) cmpresult.getEntityList().get(0);
                    row.setCompany(company);
                    row.setRate(purchaseOrderDetail.getRate());

                    prodList.add(purchaseOrderDetail.getProduct().getID());

                    JSONObject inventoryjson = new JSONObject();

                    inventoryjson.put("productid", purchaseOrderDetail.getProduct().getID());
                    inventoryjson.put("quantity", quantity);
                    if (purchaseOrderDetail.getUom() != null) {
                        inventoryjson.put("uomid", purchaseOrderDetail.getUom().getID());
                    }
                    inventoryjson.put("baseuomquantity", updateInventoryFlag ? purchaseOrderDetail.getBaseuomquantity() : 0);
                    inventoryjson.put("actquantity", updateInventoryFlag ? 0 : quantity);
                    inventoryjson.put("baseuomrate", purchaseOrderDetail.getBaseuomrate());
                    inventoryjson.put("invrecord", updateInventoryFlag ? true : false);

                    inventoryjson.put("description", purchaseOrderDetail.getDescription());
                    inventoryjson.put("carryin", false);
                    inventoryjson.put("defective", false);
                    inventoryjson.put("newinventory", false);
                    inventoryjson.put("companyid", companyId);
                    inventoryjson.put("updatedate", purchaseOrderDetail.getPurchaseOrder().getOrderDate());

                    /* Adding entry in inventory for invoice*/
                    KwlReturnObject invresult = accProductObj.addInventory(inventoryjson);
                    Inventory inventory = (Inventory) invresult.getEntityList().get(0);

                    row.setInventory(inventory);
                    double rate = row.getRate();
                    double rowAmount = 0;

                    /* Calculating unit price when including GST option true */
                    if (includingGST) {
                        rate = purchaseOrderDetail.getRateincludegst();
                    }
                    rowAmount = authHandler.round(rate * quantity, companyId);
                    rowAmount = authHandler.round(rowAmount, companyId);
                    double rowdiscount = 0;
                    totalamount += rowAmount;
                    Discount discount = null;

                    double rowTaxAmtInBase = 0.0;
                    double disc = purchaseOrderDetail.getDiscount();
                    int rowdisc = purchaseOrderDetail.getDiscountispercent();
                    KwlReturnObject bAmt = null, jeResult = null;
                    JournalEntry journalEntry = null;
                    HashMap<String, Object> requestParams = new HashMap();

                    if (!requestParams.containsKey("gcurrencyid") && !StringUtil.isNullOrEmpty(jeId)) {
                        jeResult = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), jeId);
                        journalEntry = (JournalEntry) jeResult.getEntityList().get(0);
                        requestParams.put("gcurrencyid", journalEntry.getCompany().getCurrency().getCurrencyID());
                        requestParams.put("companyid", journalEntry.getCompany().getCompanyID());

                    }

                    if (purchaseOrderDetail.getTax() != null) {
                        row.setTax(purchaseOrderDetail.getTax());
                        row.setRowTaxAmount(purchaseOrderDetail.getRowTaxAmount());
                        rowtaxamount = purchaseOrderDetail.getRowTaxAmount();
                        /* Saving row taxamount in base*/
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, rowtaxamount, journalEntry.getCurrency().getCurrencyID(), journalEntry.getEntryDate(), journalEntry.getExternalCurrencyRate());
                        rowTaxAmtInBase = (Double) bAmt.getEntityList().get(0);
                        rowTaxAmtInBase = authHandler.round(rowTaxAmtInBase, companyId);
                        row.setRowTaxAmountInBase(rowTaxAmtInBase);
                        taxamount += purchaseOrderDetail.getRowTaxAmount();
                        rowtax = purchaseOrderDetail.getTax();
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
                    /*
                     *Below code is used to save exlcuding gst amt and excluding gst amount in base
                     *required to show invoice in GST report
                     */
                    rowExcludingGstAmount = rowAmount;
                    if (includingGST) {//include in GST
                        rowExcludingGstAmount -= rowdiscount;
                        rowExcludingGstAmount += purchaseOrderDetail.getLineLevelTermAmount();//including GST price + Line level term amount
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
                     In JE Detail Entry for Purchase amount = rowAmount - Tax Amount
                     */
                    double JElineAmount = rowAmount; // Row Amount Not Include Gst(Tax)

                    if (includingGST) { // Check For  Row Amount Include Gst(Tax)
                        JElineAmount -= rowtaxamount;
                    }

                    JSONObject jedjson = new JSONObject();

                    jedjson.put("srno", jeDetails.size() + 1);
                    jedjson.put("companyid", companyId);
                    jedjson.put("amount", JElineAmount);
                    jedjson.put("accountid", product.getPurchaseAccount().getID());
                    jedjson.put("debit", true);
                    jedjson.put("jeid", jeId);
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jeDetails.add(jed);

                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    row.setPurchaseJED(jed);

                    /* Doing entry in JEdetails for tax ,if tax is applied at row level*/
                    if (rowtax != null) {
                        jedjson = new JSONObject();
                        jedjson.put("srno", jeDetails.size() + 1);
                        jedjson.put("companyid", companyId);
                        jedjson.put("amount", rowtaxamount);
                        jedjson.put("accountid", rowtax.getAccount().getID());
                        jedjson.put("debit", true);
                        jedjson.put("jeid", jeId);
                        KwlReturnObject taxjedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        jed = (JournalEntryDetail) taxjedresult.getEntityList().get(0);
                        jeDetails.add(jed);
                        row.setGstJED(jed);
                    }

                    /* Get custom/Dimension field from Purchase Order which have been used in PO  & also available for Invoice */
                    HashMap<String, Object> fieldrequestParams1 = new HashMap();
                    HashMap<String, String> customFieldMap = new HashMap<String, String>();
                    HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
                    fieldrequestParams1.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                    fieldrequestParams1.put(Constants.filter_values, Arrays.asList(companyId, Constants.Acc_Purchase_Order_ModuleId, 1));
                    HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                    HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams1, replaceFieldMap, customFieldMap, customDateFieldMap);

                    /* Get Custom/Dimension field Data */
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    JSONObject obj = new JSONObject();

                    PurchaseOrderDetailsCustomData jeDetailCustom = (PurchaseOrderDetailsCustomData) purchaseOrderDetail.getPoDetailCustomData();

                    AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);

                    if (jeDetailCustom != null) {
                        JSONObject params = new JSONObject();
                        params.put(Constants.isExport, false);
                        params.put("isForReport", true);
                        params.put(Constants.isdefaultHeaderMap, false);

                        fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);

                    }

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
                        requestParams1.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);

                        /* 
                         *Function used to check custom/dimension field present in invoice
                         *also & if present then being saved for Invoice module
                         */
                        checkAndSaveLineLevelCustomField(requestParams1);

                    }

                    HashMap<String, Object> fieldrequestParamsProduct = new HashMap();
                    Map<String, Object> variableMapProduct = new HashMap<String, Object>();
                    fieldrequestParamsProduct.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                    fieldrequestParamsProduct.put(Constants.filter_values, Arrays.asList(companyId, Constants.Acc_Product_Master_ModuleId, 0));
                    HashMap<String, String> replaceFieldMapProduct = new HashMap<String, String>();
                    HashMap<String, Integer> FieldMapProduct = accAccountDAOobj.getFieldParamsMap(fieldrequestParamsProduct, replaceFieldMapProduct);

                    KwlReturnObject resultProduct = accountingHandlerDAOobj.getObject(PurchaseOrderDetailProductCustomData.class.getName(), purchaseOrderDetail.getID());
                    PurchaseOrderDetailProductCustomData objProduct = (PurchaseOrderDetailProductCustomData) resultProduct.getEntityList().get(0);
                    JSONObject obj1 = new JSONObject();
                    if (objProduct != null) {
                        JSONObject params = new JSONObject();
                        params.put(Constants.isExport, false);
                        params.put("isForReport", true);

                        setCustomColumnValuesForProduct(objProduct, FieldMapProduct, replaceFieldMapProduct, variableMapProduct, params);
                        for (Map.Entry<String, Object> varEntry : variableMapProduct.entrySet()) {
                            String coldata = varEntry.getValue().toString();
                            if (!StringUtil.isNullOrEmpty(coldata)) {
                                obj1.put(varEntry.getKey(), coldata);
                                obj1.put("key", varEntry.getKey());
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
     
     /*  
      *Function is used to get Line level status of PO
      */
     public double getPurchaseOrderDetailStatus(PurchaseOrderDetail pod) throws ServiceException {
        double result = pod.getQuantity();
        KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), pod.getCompany().getCompanyID());
        CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
        KwlReturnObject idresult = accGoodsReceiptobj.getIDFromGROD(pod.getID());
        List list = idresult.getEntityList();
        Iterator ite1 = list.iterator();
        double qua = 0.0;
        boolean fullInv = false;
        double quantPartTtInv = 0.0;
        while (ite1.hasNext()) {
            GoodsReceiptDetail ge = (GoodsReceiptDetail) ite1.next();
            fullInv = true;
            qua += ge.getInventory().getQuantity();
        }

        if (fullInv) {
            result = pod.getQuantity() - qua;
        } else {
            if (pod.getQuantity() * 100 > quantPartTtInv) {
                result = pod.getQuantity() - qua;
            } else {
                result = 0;
            }
        }

        return result;
    }
     
    /*  
     *Function used to check custom/dimension field for invoice module , 
     *if present then being saved custom/dimension 
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
        GoodsReceiptDetail row = (GoodsReceiptDetail) requestParams.get("invoicedetail");
        JournalEntryDetail jed = (JournalEntryDetail) requestParams.get("jedetail");
        int moduleid = (Integer) requestParams.get("moduleid");
        boolean isFromProduct = requestParams.get("isFromProduct") != null ? (Boolean) requestParams.get("isFromProduct") : false;
        try {

            requestParams1.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.Acc_custom_field));

            requestParams1.put(Constants.filter_values, Arrays.asList(companyid, moduleid, fieldname));

            if (isFromProduct) {
                requestParams1.put("isFromProduct", true);
            }

            // get custom field module wise from fieldlabel
            List result1 = accAccountDAOobj.getFieldParamsFieldLabelWise(requestParams1);
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
                customrequestParams.put(Constants.moduleid, Constants.Acc_Vendor_Invoice_ModuleId);
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
                customrequestParams.put(Constants.moduleid, Constants.Acc_Vendor_Invoice_ModuleId);
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
    
    /* 
     function is used to prepare a map for product custom data  used in Purchase Order
     */
     private void setCustomColumnValuesForProduct(PurchaseOrderDetailProductCustomData poDetailsProductCustomData, HashMap<String, Integer> fieldMap, Map<String, String> replaceFieldMap,
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
                        if (poDetailsProductCustomData != null) {
                            coldata = poDetailsProductCustomData.getCol(colnumber);
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
     public ModelAndView exportLandingCostItemReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String view = "jsonView";
        String msg = "";
        int totalCount = 0;
        try {
            HashMap<String, Object> requestMapData=new HashMap<String,Object>();
            String companyid=sessionHandlerImpl.getCompanyid(request);
            DateFormat userdf = authHandler.getDateOnlyFormat(request);
            KwlReturnObject extraPref = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extrareferences = (ExtraCompanyPreferences) extraPref.getEntityList().get(0);
            JSONObject paramJobj = getGoodsReceiptOrdersParams(request);            
            Iterator<String> keyItr=paramJobj.keys();
            while(keyItr.hasNext()){
                String key= keyItr.next();
                requestMapData.put(key,paramJobj.get(key));
            }
            if(!StringUtil.isNullOrEmpty(companyid)){
                requestMapData.put("companyId",companyid);
            }
            if(!StringUtil.isNullObject(userdf)){
                requestMapData.put("df",userdf);
            }
            jobj=accGoodsReceiptModuleService.getLandingCostItemReport(requestMapData, companyid);
            if(jobj.length() > 0){
                exportDaoObj.processRequest(request, response, jobj);
            }
            issuccess=true;
        } catch (Exception ex) {
            issuccess = false;
            msg = "accGoodsReceiptController.getLandingCostItemReport : " + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
     /**
      * Get Total Invoice Amount from Mapping table on Particular Bill date to check Daily Limit
      * @param request
      * @param response
      * @return 
      */
     public ModelAndView getTotalInvoiceAmountURDVendorPurchaseInvoice(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String view = "jsonView";
        String msg = "";
        try {
            JSONObject paramJobj = getGoodsReceiptOrdersParams(request);     
            DateFormat df = authHandler.getDateOnlyFormat();
            paramJobj.put(Constants.df, df);
            JSONObject returnobj = accGoodsReceiptModuleService.getTotalInvoiceAmountURDVendorPurchaseInvoice(paramJobj);
            jobj.put("amount", returnobj.optDouble("amount", 0));
            issuccess=true;
        } catch (Exception ex) {
            issuccess = false;
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
/**
 * Function to get ITC information i.e Blocked ITC GL etc.
 * @param request
 * @param response
 * @return 
 */     
    public ModelAndView getITCInformationForProducts(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String view = "jsonView";
        String msg = "";
        try {
            JSONObject paramJobj = getGoodsReceiptOrdersParams(request);
            DateFormat df = authHandler.getDateOnlyFormat();
            paramJobj.put(Constants.df, df);
            jobj = accGoodsReceiptModuleService.getITCInformationForProducts(paramJobj);
            issuccess = true;
        } catch (Exception ex) {
            issuccess = false;
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
     
         
    /**
     * ERP-39781
     * Checks if any landed cost transactions are present in the system
     * with term amounts for expense invoice.
     * @param request
     * @param response
     * @return true (for Present) and false (for Not Present)
     */
    public ModelAndView isLandedCostWithTermTransactionsPresent(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jObj = null;
        boolean isSuccess = false;
        String msg = "";
        try {
            JSONObject requestParams = StringUtil.convertRequestToJsonObject(request);
            requestParams.put(Constants.locale, RequestContextUtils.getLocale(request));
            jObj = accGoodsReceiptModuleService.isLandedCostWithTermTransactionsPresent(requestParams);
            isSuccess = jObj.optBoolean(Constants.RES_success);
            msg = jObj.optString(Constants.RES_msg);
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jObj.put(Constants.RES_success, isSuccess);
                jObj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jObj.toString());
    }
}
