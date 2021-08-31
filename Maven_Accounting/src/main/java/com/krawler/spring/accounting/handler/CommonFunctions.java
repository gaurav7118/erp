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

package com.krawler.spring.accounting.handler;
import com.googlecode.cqengine.attribute.Attribute;
import com.krawler.acc.dm.ExchangeRateDetailInfo;
import com.krawler.acc.dm.ExchangeRateInfo;
import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.CompanyRoutingDataSource;
import com.krawler.common.util.Constants;
import com.krawler.common.util.DynamicIndexer;
import com.krawler.common.util.LandingCostAllocationType;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.spring.common.fieldDataManagerDAO;
import com.krawler.documentdesigner.AccDocumentDesignService;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.esp.servlets.ProfileImageServlet;
import com.krawler.esp.utils.ConfigReader;
import static com.krawler.esp.web.resource.Links.loginpageFull;
import com.krawler.hql.accounting.*;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.currency.service.AccCurrencyExchangeRate;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.accounting.account.accAccountControllerCMN;
import com.krawler.spring.accounting.account.accAccountControllerCMNService;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.creditnote.accCreditNoteDAO;
import com.krawler.spring.accounting.currency.accCurrencyController;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignHandler;
import com.krawler.spring.accounting.customDesign.CustomDesignLineItemProp;
import com.krawler.spring.accounting.debitnote.accDebitNoteDAO;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptCMN;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.invoice.accInvoiceCMN;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.product.accProductController;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.receipt.accReceiptDAO;
import com.krawler.spring.accounting.salesorder.accSalesOrderDAO;
import static com.krawler.spring.accounting.tax.TaxConstants.DATA;
import static com.krawler.spring.accounting.tax.TaxConstants.MSG;
import com.krawler.spring.accounting.tax.accTaxController;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.term.accTermController;
import com.krawler.spring.accounting.vendorpayment.accVendorPaymentDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.CommonFnController;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFuctionality.CreatePDF;
import com.krawler.spring.exportFuctionality.ExportRecord_LSH;
import com.krawler.spring.exportFuctionality.ExportRecord_VRNet;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import com.lowagie.text.DocumentException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.String;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;
import com.krawler.spring.exportFuctionality.AccExportReportsServiceDAO;
import java.io.ByteArrayInputStream;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import static com.krawler.spring.accounting.currency.CurrencyContants.APPLYDATE;
import static com.krawler.spring.accounting.currency.CurrencyContants.TODATE;
import static com.krawler.spring.accounting.currency.CurrencyContants.ERID;
import static com.krawler.spring.accounting.currency.CurrencyContants.COMPANYID;
import static com.krawler.spring.accounting.currency.CurrencyContants.EXCHANGERATE;
import com.krawler.spring.accounting.customDesign.CustomDesignerConstants;
import com.krawler.spring.accounting.masteritems.accMasterItemsController;
import com.krawler.spring.accounting.purchaseorder.accPurchaseOrderDAO;
import com.krawler.spring.accounting.salesreturn.AccSalesReturnService;
import com.krawler.spring.accounting.vendorpayment.AccVendorPaymentServiceDAO;
import java.lang.reflect.Method;

import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import net.sf.jasperreports.engine.util.JRProperties;
import com.krawler.esp.handlers.APICallHandlerService;
import com.krawler.spring.accounting.currency.AccTaxCurrencyExchangeDAO;
import com.krawler.hql.accounting.invoice.service.AccInvoiceModuleService;
import com.krawler.inventory.valuation.InventoryValuationProcess;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import static com.krawler.spring.accounting.goodsreceipt.GoodsReceiptConstants.ACCOUNTID;
import static com.krawler.spring.accounting.goodsreceipt.GoodsReceiptConstants.AMOUNT;
import static com.krawler.spring.accounting.goodsreceipt.GoodsReceiptConstants.DEBIT;
import static com.krawler.spring.accounting.goodsreceipt.GoodsReceiptConstants.DESCRIPTION;
import static com.krawler.spring.accounting.goodsreceipt.GoodsReceiptConstants.JEID;
import static com.krawler.spring.accounting.goodsreceipt.GoodsReceiptConstants.SRNO;
import com.krawler.spring.accounting.goodsreceipt.service.accGoodsReceiptModuleService;
import com.krawler.spring.accounting.invoice.AccInvoiceServiceDAO;
import com.krawler.spring.accounting.invoice.AccInvoiceServiceHandler;
import com.krawler.spring.accounting.invoice.accInvoiceController;
import com.krawler.spring.accounting.invoice.dm.InvoiceInfo;
import com.krawler.spring.accounting.journalentry.JournalEntryConstants;
import com.krawler.spring.accounting.mailNotification.AccMailNotificationDAO;
import com.krawler.spring.accounting.mailnotifier.AccMailNotifyService;
import com.krawler.spring.accounting.mailnotifier.AccMailNotifyServiceImpl;
import com.krawler.spring.accounting.mailnotifier.AccMailNotifyThread;
import com.krawler.spring.accounting.reports.AccReportsService;
import com.krawler.spring.accounting.tax.TaxConstants;
import com.krawler.spring.authHandler.authHandlerController;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.companyDetails.companyDetailsDAO;
import java.net.URL;
import java.text.ParseException;
import javax.sql.DataSource;
import org.apache.commons.lang.time.DateUtils;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.DbSupportFactory;
import org.flywaydb.core.internal.dbsupport.SqlScript;
import org.flywaydb.core.internal.util.scanner.Resource;
import org.flywaydb.core.internal.util.scanner.filesystem.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import java.net.URLDecoder;
import javax.servlet.http.HttpUtils;

/**
 *
 * @author krawler
 */
public class CommonFunctions  extends MultiActionController implements ApplicationListener, MessageSourceAware{
    private authHandlerDAO authHandlerDAOObj;
    private accInvoiceDAO accInvoiceDAOobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private AccTaxCurrencyExchangeDAO accTaxCurrencyExchangeDAOobj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private accTaxController AccTaxcontrollerObj;
    private accProductController AccProductcontrollerObj;
    private accTermController AccTermcontrollerObj;
    private accCurrencyController AccCurrencycontrollerObj;
    private MessageSource messageSource;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accAccountDAO accAccountDAOobj;
    private accTaxDAO accTaxObj;
    private accInvoiceCMN accInvoiceCommon;
    private accReceiptDAO accReceiptobj;
    private accCreditNoteDAO accCreditNoteobj;
    private accDebitNoteDAO accDebitNoteobj;
    private accVendorPaymentDAO accVendorPaymentobj;
    private accGoodsReceiptCMN accGoodsReceiptCommon;
    private VelocityEngine velocityEngine;
    private ExportRecord_VRNet ExportrecordVRnetObj;
    private ExportRecord_LSH ExportrecordLSHObj;
    private CreatePDF CreatePDFObj;
    private accGoodsReceiptDAO accGoodsReceiptobj;
    private accMasterItemsDAO accMasterItemsDAOobj;
    private accSalesOrderDAO accSalesOrderDAOobj;
    private accProductDAO accProductObj;
    private auditTrailDAO auditTrailObj;
    private accJournalEntryDAO accJournalEntryobj;
    private boolean appStarted=false;
    private AccExportReportsServiceDAO accExportOtherReportsServiceDAOobj;
    private accAccountControllerCMNService accAccountControllerCMNServiceObj;
    private AccCurrencyExchangeRate accCurrencyExchangeRate;
    private AccVendorPaymentServiceDAO accVendorPaymentServiceDAO;
    private HibernateTransactionManager txnManager;
    private SyncAllHandler SyncAllHandlerObj;
    private accPurchaseOrderDAO accPurchaseOrderobj;
    private AccSalesReturnService accSalesReturnService;
    private APICallHandlerService apiCallHandlerService;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private AccInvoiceModuleService accInvoiceModuleService;
    private authHandlerController authHandlerControllerObj;
    private InventoryValuationProcess inventoryValuationProcess;
    private CompanyRoutingDataSource routingDataSource;
    private AccInvoiceServiceDAO accInvoiceServiceDAO;
    private companyDetailsDAO companyDetailsDAOObj;
    private AccCommonTablesDAO accCommonTablesDAO;
    private AccDocumentDesignService accDocumentDesignService;
    private fieldDataManagerDAO fieldDataManagerDAOobj;
    private AccMailNotificationDAO accMailNotificationDAOObj;
    private AccMailNotifyService accMailNotifyServiceobj;
    private accGoodsReceiptModuleService accGoodsReceiptModuleService;
    private AccReportsService accReportsService;
    private fieldDataManager fieldDataManagercntrl;
    private accCreditNoteDAO accCreditNoteDAOobj;
    
    public void setfieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
    
    public void setaccCreditNoteDAO(accCreditNoteDAO accCreditNoteDAOobj) {
        this.accCreditNoteDAOobj = accCreditNoteDAOobj;
    }
        
    public void setAccMailNotifyServiceobj(AccMailNotifyService accMailNotifyServiceImplobj) {
        this.accMailNotifyServiceobj = accMailNotifyServiceImplobj;
    }
    
    public void setFieldDataManagerDAO(fieldDataManagerDAO fieldDataManagerDAOobj) {
        this.fieldDataManagerDAOobj = fieldDataManagerDAOobj;
    }
    
    public void setcompanyDetailsDAO(companyDetailsDAO companyDetailsDAOObj) {
        this.companyDetailsDAOObj = companyDetailsDAOObj;
    }
    
    public void setsyncAllHandler(SyncAllHandler SyncAllHandlerObj) {
    this.SyncAllHandlerObj = SyncAllHandlerObj;
    }

   @Override
    public void setMessageSource(MessageSource ms) {
		this.messageSource=ms;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    
    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyobj) {
        this.accCurrencyDAOobj = accCurrencyobj;
    }
    public void setAccTaxCurrencyExchangeDAO(AccTaxCurrencyExchangeDAO accTaxCurrencyExchangeDAOobj) {
        this.accTaxCurrencyExchangeDAOobj = accTaxCurrencyExchangeDAOobj;
    }
    public void setaccReceiptDAO(accReceiptDAO accReceiptobj) {
        this.accReceiptobj = accReceiptobj;
    }
    public void setaccVendorPaymentDAO(accVendorPaymentDAO accVendorPaymentobj) {
        this.accVendorPaymentobj = accVendorPaymentobj;
    }
    public void setAccCreditNoteDAO(accCreditNoteDAO accCreditNoteobj) {
        this.accCreditNoteobj = accCreditNoteobj;
    }
    public void setaccDebitNoteDAO(accDebitNoteDAO accDebitNoteobj) {
        this.accDebitNoteobj = accDebitNoteobj;
    }
    
    public void setCreatePDF(CreatePDF CreatePDFObj) {
        this.CreatePDFObj = CreatePDFObj;
    }
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    public void setaccTaxDAO (accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }

       public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }
    public void setauthHandlerDAO(authHandlerDAO authHandlerDAOObj1) {
        this.authHandlerDAOObj = authHandlerDAOObj1;
    }
    
    public void setaccTaxcontroller(accTaxController accTaxControllerObj) {
        this.AccTaxcontrollerObj = accTaxControllerObj;
    }

    public void setaccProductcontroller(accProductController accProductControllerObj) {
        this.AccProductcontrollerObj = accProductControllerObj;
    }

    public void setaccTermcontroller(accTermController accTermControllerObj) {
        this.AccTermcontrollerObj = accTermControllerObj;
    }

    public void setaccCurrencycontroller(accCurrencyController accCurrencyControllerObj) {
        this.AccCurrencycontrollerObj = accCurrencyControllerObj;
    }
    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }
    public void setaccInvoiceCommon(accInvoiceCMN accInvoiceCommon) {
        this.accInvoiceCommon = accInvoiceCommon;
    } 
    public void setaccGoodsReceiptCMN(accGoodsReceiptCMN accGoodsReceiptCommon) {
        this.accGoodsReceiptCommon = accGoodsReceiptCommon;
    }
    public void setvelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }
    public void setExportRecordVRnet(ExportRecord_VRNet ExportrecordVRnetObj) {
        this.ExportrecordVRnetObj = ExportrecordVRnetObj;
    }
     public void setExportRecordLSH(ExportRecord_LSH ExportrecordLSHObj) {
        this.ExportrecordLSHObj = ExportrecordLSHObj;
    }
    public void setaccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
    }
    public void setaccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }
    public void setaccSalesOrderDAO(accSalesOrderDAO accSalesOrderDAOobj) {
        this.accSalesOrderDAOobj = accSalesOrderDAOobj;
    }
    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }
    
    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj){
        this.auditTrailObj = auditTrailDAOObj;
    }
    
    public void setaccExportOtherReportsServiceDAO(AccExportReportsServiceDAO accExportReportsServiceDAOobj) {
        this.accExportOtherReportsServiceDAOobj = accExportReportsServiceDAOobj;
    }
        public void setaccAccountControllerCMNServiceObj(accAccountControllerCMNService accAccountControllerCMNServiceObj) {
        this.accAccountControllerCMNServiceObj = accAccountControllerCMNServiceObj;
    }
    
    public void setAccCurrencyExchangeRate(AccCurrencyExchangeRate accCurrencyExchangeRate) {
        this.accCurrencyExchangeRate = accCurrencyExchangeRate;
    }

    public void setAccVendorPaymentServiceDAO(AccVendorPaymentServiceDAO accVendorPaymentServiceDAO) {
        this.accVendorPaymentServiceDAO = accVendorPaymentServiceDAO;
    }
    
    public void setaccPurchaseOrderDAO(accPurchaseOrderDAO accPurchaseOrderobj) {
        this.accPurchaseOrderobj = accPurchaseOrderobj;
    }
     public void setAccSalesReturnService(AccSalesReturnService accSalesReturnService) {
        this.accSalesReturnService = accSalesReturnService;
    }

    public void setApiCallHandlerService(APICallHandlerService apiCallHandlerService) {     
        this.apiCallHandlerService = apiCallHandlerService;
    }
    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }
    
    public void setAccInvoiceModuleService(AccInvoiceModuleService accInvoiceModuleService) {
        this.accInvoiceModuleService = accInvoiceModuleService;
    }

    public void setauthHandlerController(authHandlerController authHandlerControllerObj) {
        this.authHandlerControllerObj = authHandlerControllerObj;
    }

    public void setInventoryValuationProcess(InventoryValuationProcess inventoryValuationProcess) {
        this.inventoryValuationProcess = inventoryValuationProcess;
    }

    public void setroutingDataSource(CompanyRoutingDataSource routingDataSource) {
        this.routingDataSource = routingDataSource;
    }
    public void setaccInvoiceServiceDAO(AccInvoiceServiceDAO accInvoiceServiceDAO) {
        this.accInvoiceServiceDAO = accInvoiceServiceDAO;
    }

    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setAccDocumentDesignService(AccDocumentDesignService accDocumentDesignService) {
        this.accDocumentDesignService = accDocumentDesignService;
    }

    
    public AccMailNotificationDAO getAccMailNotificationDAOObj() {
        return accMailNotificationDAOObj;
    }

    public void setAccMailNotificationDAOObj(AccMailNotificationDAO accMailNotificationDAOObj) {
        this.accMailNotificationDAOObj = accMailNotificationDAOObj;
    } 

    public void setAccGoodsReceiptModuleService(accGoodsReceiptModuleService accGoodsReceiptModuleService) {
        this.accGoodsReceiptModuleService = accGoodsReceiptModuleService;
    }
    
    public void setAccReportsService(AccReportsService accReportsService) {
        this.accReportsService = accReportsService;
    }
        
    public ModelAndView sendMail(HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException, IOException, DocumentException, ServiceException, JSONException {
       java.io.OutputStream os = null;
       JSONObject jobj = new JSONObject();
       List<String> list = new ArrayList<String>();
        List<Object> jasperList = new ArrayList<Object>();
        {
            String fileName = "";
            ByteArrayOutputStream baos = null;
            ByteArrayInputStream bais = null;
            JasperPrint jasperPrint = null;
            byte[] pdfByteArray = null;
            FileInputStream fis = null;
            FileOutputStream fos = null;
            boolean issuccess = false;
            boolean iscontraentryflag = false;
            boolean otherwiseFlag = false;
            boolean advanceFlag = false;
            double advanceAmount = 0;
            HashMap<String, Object> requestmap = null;
            try {
                requestmap = new HashMap<String, Object>();
                File tempDir = null;
                String[] emails = request.getParameter("emailid").split(";");
                String personid = request.getParameter("personid");
                String plainMsg = request.getParameter("message");
            
                String subject = request.getParameter("subject");
                boolean sendPdf = Boolean.parseBoolean((String) request.getParameter("sendpdf"));
                int mode = Integer.parseInt(request.getParameter("mode"));
                int moduleid = StringUtil.isNullOrEmpty(request.getParameter("moduleid")) ? 0 : Integer.parseInt(request.getParameter("moduleid"));
                String billid = request.getParameter("billid");
                String attachmentSelection = request.getParameter("attachmentSelection");
                String username = sessionHandlerImpl.getUserName(request);
                String baseUrl = URLUtil.getPageURL(request, loginpageFull);
                if (!StringUtil.isNullOrEmpty(request.getParameter("advanceAmount"))) {
                advanceAmount = Double.parseDouble(request.getParameter("advanceAmount"));
       }
                
                if (!StringUtil.isNullOrEmpty(request.getParameter("otherwise"))) {
                    otherwiseFlag = Boolean.parseBoolean(request.getParameter("otherwise"));
            }
        
                iscontraentryflag = Boolean.parseBoolean(request.getParameter("contraentryflag"));
                Locale loc = RequestContextUtils.getLocale(request);
                requestmap = new HashMap<String, Object>();
                requestmap.put("loc", loc);
                requestmap.put("locale", loc);
                requestmap.put("baseUrl", baseUrl);
                requestmap.put("otherwiseFlag", otherwiseFlag);
                requestmap.put("advanceAmount", advanceAmount);
                requestmap.put("iscontraentryflag", iscontraentryflag);
                requestmap.put("username", username);
                requestmap.put(Constants.companyKey, AccountingManager.getCompanyidFromRequest(request));
                requestmap.put(Constants.globalCurrencyKey, AccountingManager.getCompanyidFromRequest(request));
                requestmap.put(Constants.df, authHandler.getDateFormatter(request));
                requestmap.put("emails", (String[]) emails);
                requestmap.put("personid", personid.toString());
                requestmap.put("plainMsg", plainMsg.toString());
                requestmap.put("subject", subject.toString());
                requestmap.put("sendPdf", (boolean) sendPdf);
                requestmap.put("mode", (int) mode);
                requestmap.put("billid", billid.toString());
                requestmap.put("attachmentSelection", attachmentSelection.toString());            
                String[] attachmentSelectionArray = attachmentSelection.split(",");
                CompanyAccountPreferences preferences = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
                ExtraCompanyPreferences extrapreferences = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
                Company company = preferences.getCompany();
                KWLCurrency currency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
               // double amount = 0;
                Date invDate = new Date();
                String fromID = authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                File destDir = new File("");
                JSONArray attachments=new JSONArray();
                if(!StringUtil.isNullOrEmpty(request.getParameter("attachments"))){
                    attachments=new JSONArray(request.getParameter("attachments"));
    }
                String[] path = new String[]{};
                String[] Names = new String[]{};
                //setting sender mail id to user mailid when user mail check is on in company preferences
                String userId = sessionHandlerImpl.getUserid(request);
                User user = (User) kwlCommonTablesDAOObj.getClassObject(User.class.getName(), userId);
                    if (extrapreferences!=null && extrapreferences.getDefaultmailsenderFlag() == Constants.UserMail) {
                          if (user != null) {
                            fromID = user.getEmailID();
                        }
                    }
                if (sendPdf) {
                    double amount = 0.0;
                    if (!StringUtil.isNullOrEmpty(request.getParameter("amount"))) {
                        amount = Double.parseDouble((String) request.getParameter("amount"));
                    }               
                    DateFormat formatter = authHandlerDAOObj.getUserDateFormatter(sessionHandlerImpl.getDateFormatID(request), sessionHandlerImpl.getTimeZoneDifference(request), true);
                    String logoPath = ProfileImageServlet.getProfileImagePath(request, true, null);
                    String currencyid = request.getParameter("currencyid") == null ? currency.getCurrencyID() : request.getParameter("currencyid");
                    String dateStr = "";
                    try {
                        DateFormat df = authHandler.getDateFormatter(request);
                        dateStr = df.format(invDate);
                    } catch (Exception ex) {
                    }
                    String accname = null;
                    if (!StringUtil.isNullOrEmpty(request.getParameter("accname"))) {
                        accname = request.getParameter("accname");
                    }
            String companyId = sessionHandlerImpl.getCompanyid(request);
                    boolean isexpenseinv = false;
                    String cust = "", address = "";
                    if (!StringUtil.isNullOrEmpty(request.getParameter("isexpenseinv"))) {
                        isexpenseinv = Boolean.parseBoolean((String) request.getParameter("isexpenseinv"));
                    }
                    if (!StringUtil.isNullOrEmpty(request.getParameter("customer"))) {
                        cust = request.getParameter("customer");
                    }
                    if (!StringUtil.isNullOrEmpty(request.getParameter("address"))) {
                        address = request.getParameter("address");
                    }
                    if (storageHandlerImpl.GetVRnetCompanyId().contains(companyId)
                            && (mode == StaticValues.AUTONUM_INVOICE || mode == StaticValues.AUTONUM_QUOTATION
                            || mode == StaticValues.AUTONUM_SALESORDER || mode == StaticValues.AUTONUM_DELIVERYORDER
                            || mode == StaticValues.AUTONUM_CREDITNOTE || mode == StaticValues.AUTONUM_DEBITNOTE || mode == StaticValues.AUTONUM_PURCHASEORDER)) {
                        baos = ExportrecordVRnetObj.createVRNetPdf(request, currencyid, billid, formatter, mode, amount, logoPath, cust, accname, address, isexpenseinv, companyId, userId, sessionHandlerImpl.getCurrencyID(request));
                    } 
                    if (mode == 8 || mode == 4) {   //For Receive Payment & Make Payment Email
                        jasperList = accExportOtherReportsServiceDAOobj.exportDefaultPaymentVoucher(request, response);
                        jasperPrint = (JasperPrint) jasperList.get(0);
                        pdfByteArray = JasperExportManager.exportReportToPdf(jasperPrint);
                        bais = new ByteArrayInputStream(pdfByteArray);
                        fileName = "Transaction" + dateStr + ".pdf";
                    } else if (mode == StaticValues.AUTONUM_RFQ) {   //RFQ Template Jasper
                        jasperList = accExportOtherReportsServiceDAOobj.exportDefaultRFQ(request, response);
                        jasperPrint = (JasperPrint) jasperList.get(0);
                        pdfByteArray = JasperExportManager.exportReportToPdf(jasperPrint);
                        bais = new ByteArrayInputStream(pdfByteArray);
                        fileName = "Transaction" + dateStr + ".pdf";
                    } else {
                        baos = CreatePDFObj.createPdf(requestmap, currencyid, billid, formatter, mode, amount, logoPath, null, accname, null, isexpenseinv, companyId, userId, sessionHandlerImpl.getCurrencyID(request));                        
                    }
                    if (mode != 8 && mode != 4 && mode != StaticValues.AUTONUM_RFQ) { //Temporary Check : When Transaction is of Non-Receive Payment & Non-Make Payment
                        destDir = new File(storageHandlerImpl.GetProfileImgStorePath(), "Transaction" + dateStr + ".pdf");
                        FileOutputStream oss = new FileOutputStream(destDir);
                        baos.writeTo(oss);
                        list.add(destDir.getAbsolutePath());
                        oss.close();
                        path = list.toArray(new String[attachments.length() + 1]);
                        Names = new String[attachments.length() + 1];
                        Names[0] = "Transaction" + dateStr + ".pdf";
                        for (int i = 0; i < attachments.length(); i++) {
                            path[i + 1] = StorageHandler.GetDocStorePath() + attachments.getJSONObject(i).get("id").toString();
                            Names[i + 1] = attachments.getJSONObject(i).get("name").toString();
                        }
//                        path = new String[]{destDir.getAbsolutePath()};
//                        baos.close();
                    }
                }else if(mode == StaticValues.AUTONUM_QUOTATION || mode == StaticValues.AUTONUM_VENQUOTATION || mode == StaticValues.AUTONUM_INVOICE || mode == StaticValues.AUTONUM_GOODSRECEIPT || mode == StaticValues.AUTONUM_DELIVERYORDER || mode == StaticValues.AUTONUM_GOODSRECEIPTORDER || mode == StaticValues.AUTONUM_SALESORDER || mode == StaticValues.AUTONUM_PURCHASEORDER || mode == StaticValues.AUTONUM_SALESRETURN || mode == StaticValues.AUTONUM_PURCHASERETURN){
                    path = new String[attachments.length()];
                    Names = new String[attachments.length()];
                    for (int i = 0; i < attachments.length(); i++) {
                        path[i] = StorageHandler.GetDocStorePath() + attachments.getJSONObject(i).get("id").toString();
                        Names[i] = attachments.getJSONObject(i).get("name").toString();
                    }
                }
                if (attachmentSelectionArray.length > 0) {
                    /*
                     mode =2 ->Customer Invoice Without Inventory Mode
                     mode =11 ->Customer Invoice With Inventory Mode
                     mode =15 ->Vendor Invoice With Inventory Mode
                     mode =6 ->Vendor Invoice Without Inventory Mode 
                     */
                    if (mode == 2 || mode == 11 || mode == 15 || mode == 6) {  //only for customer invoice (Without Inventory) 
                        HashMap<String, Object> hashMap = new HashMap<String, Object>();
                        hashMap.put("invoiceID", billid);
                        hashMap.put("companyid", company.getCompanyID());
                        KwlReturnObject object = accInvoiceDAOobj.getinvoiceDocuments(hashMap);
            
                        tempDir = new File(storageHandlerImpl.GetDocStorePath() + "Temp");
                        if (!tempDir.exists()) {
                            tempDir.mkdir();
                        }
            
                        Iterator iterator = object.getEntityList().iterator();
                        while (iterator.hasNext()) {
                            Object[] obj = (Object[]) iterator.next();
                            String storeID = (String) obj[2];
                            for (int selectCount = 0; selectCount < attachmentSelectionArray.length; selectCount++) {
                                if (storeID.equalsIgnoreCase(attachmentSelectionArray[selectCount])) {
                                    String docName = (String) obj[0];
                                    String Ext = docName.substring(docName.lastIndexOf("."));
             
                                    try {
                                        File fp = new File(StorageHandler.GetDocStorePath() + storeID + Ext);
                                        File op = new File(storageHandlerImpl.GetDocStorePath() + "Temp" + "/", docName);
                                        if (fp.exists()) {
                                            byte buff[] = new byte[(int) fp.length()];
                                            fis = new FileInputStream(fp);
                                            int read = fis.read(buff);
                                            if (!op.exists()) {
                                                op.createNewFile();
                                            } 
                                                fos = new FileOutputStream(op);
                                                fos.write(buff);
                                                fos.flush();
                                            list.add(storageHandlerImpl.GetDocStorePath() + "Temp" + "/" + docName);
                                        }
                                    } catch (FileNotFoundException ex) {
                                        Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                        }
                    }
                }                    
                try {
                    if (emails.length > 0) {
                        Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                        // get notification rule for email
                        KwlReturnObject notificationRuleResult = accountingHandlerDAOobj.getEmailTemplateTosendApprovalMail(company.getCompanyID(), Constants.Email_Button_From_Report_fieldid, moduleid);
                        String hyperlinkText = "";
                        String ccemails="";
                        if(notificationRuleResult.getEntityList().size() > 0){
                            NotificationRules notificationRule = (NotificationRules) notificationRuleResult.getEntityList().get(0);
                            hyperlinkText = notificationRule.getHyperlinkText();
//                            String emailsStr = request.getParameter("emailid") + ";" + notificationRule.getEmailids().replaceAll(",", ";");
//                            emails = emailsStr.split(";");
                              ccemails=notificationRule.getEmailids();
                        }
                        // get hyperlink field from constants
                        JSONObject jObj = new JSONObject(CustomDesignerConstants.CustomDesign_Email_Notification_ExtraFieldsMap.get(CustomDesignerConstants.TEMPLATE_HYPERLINK_IN_EMAIL));
                        String hyperlinkFieldLabel = jObj.getString("label");
            
                        if(mode == 8 || mode == 4 || mode == StaticValues.AUTONUM_RFQ){ //For Receive Payment & Make Payment Email
                            String mainHtml = "";
                            String templateid = StringUtil.isNullOrEmpty(request.getParameter("templateid")) ? "" : request.getParameter("templateid");
                            String templateName = StringUtil.isNullOrEmpty(request.getParameter("templateName")) ? "" : request.getParameter("templateName");
                            if (!StringUtil.isNullOrEmpty(templateid) && !Constants.NONE.equalsIgnoreCase(templateid)) {
                                JSONObject requestParams = StringUtil.convertRequestToJsonObject(request);
                    
                                if (extrapreferences.isActivateDDTemplateFlow()) {
                                    mainHtml = accDocumentDesignService.getHTMLContentForEmailWithDDTemplate(requestParams);
                                }
                    
                                if (extrapreferences.isActivateDDInsertTemplateLink()) {
                                    StringBuilder appendString = new StringBuilder();
                                    requestParams.put(Constants.isdefaultHeaderMap, true);
                                    String resturl = accDocumentDesignService.PrintTemplateRestUrl(requestParams);
                                    if(!StringUtil.isNullOrEmpty(hyperlinkText)){
                                        templateName = hyperlinkText;
                                    }
                                    String templateurl = "<a href='" + resturl + "' target='_blank'>" + templateName + "</a>";
                    
                                    if(plainMsg.contains(hyperlinkFieldLabel)){
                                        plainMsg = plainMsg.replaceAll("#" + hyperlinkFieldLabel + "#", templateurl);
                                    } else{
                                        appendString.append(mainHtml);
                                        appendString.append("<br/><b>Please click on link below to preview template: </b> <br/>" + templateurl);
                                        mainHtml = appendString.toString();
                                    }
                                }
                            }
                            // Replace placeholder with empty text
                            plainMsg = plainMsg.replaceAll("#" + hyperlinkFieldLabel + "#", "");
                            SendMailHandler.attachPDFToMail(fileName, emails, subject, plainMsg+mainHtml, plainMsg+mainHtml, fromID, bais,mode, smtpConfigMap, ccemails);
                            issuccess = true;
                        } else {
                           if(mode == StaticValues.AUTONUM_QUOTATION || mode == StaticValues.AUTONUM_VENQUOTATION || mode == StaticValues.AUTONUM_INVOICE || mode == StaticValues.AUTONUM_GOODSRECEIPT || mode == StaticValues.AUTONUM_DELIVERYORDER || mode == StaticValues.AUTONUM_GOODSRECEIPTORDER || mode == StaticValues.AUTONUM_SALESORDER || mode == StaticValues.AUTONUM_PURCHASEORDER || mode == StaticValues.AUTONUM_SALESRETURN || mode == StaticValues.AUTONUM_PURCHASERETURN || mode == StaticValues.AUTONUM_PURCHASEREQUISITION){
                                String mainHtml = "";
                                String templateid = StringUtil.isNullOrEmpty(request.getParameter("templateid")) ? "" : request.getParameter("templateid");
                                String templateName = StringUtil.isNullOrEmpty(request.getParameter("templateName")) ? "" : request.getParameter("templateName");
                                if(!StringUtil.isNullOrEmpty(templateid) && !Constants.NONE.equalsIgnoreCase(templateid)){
                                    JSONObject requestParams = StringUtil.convertRequestToJsonObject(request);
  
                                    if (extrapreferences.isActivateDDTemplateFlow()) {
                                        mainHtml = accDocumentDesignService.getHTMLContentForEmailWithDDTemplate(requestParams);
                    }
                    
                                    if (extrapreferences.isActivateDDInsertTemplateLink()) {
                                        StringBuilder appendString = new StringBuilder();
                                        requestParams.put(Constants.isdefaultHeaderMap,true);
                                        String resturl = accDocumentDesignService.PrintTemplateRestUrl(requestParams);
                                        if(!StringUtil.isNullOrEmpty(hyperlinkText)){
                                            templateName = hyperlinkText;
                    }
                                        String templateurl = "<a href='" + resturl + "' target='_blank'>" + templateName + "</a>";
                    
                                        if(plainMsg.contains(hyperlinkFieldLabel)){
                                            plainMsg = plainMsg.replaceAll("#" + hyperlinkFieldLabel + "#", templateurl);
                                        } else{
                                            appendString.append(mainHtml);
                                            appendString.append("<br/><b>Please click on link below to preview template: </b> <br/>" + templateurl);
                                            mainHtml = appendString.toString();
                                        }
                                    }
                                }
                                // Replace placeholder with empty text
                                plainMsg = plainMsg.replaceAll("#" + hyperlinkFieldLabel + "#", "");
                                SendMailHandler.postMail(ccemails,new String[0],emails, subject, plainMsg+mainHtml, plainMsg+mainHtml, fromID, path,smtpConfigMap);
                                issuccess = true; 
                            }else {
                                String mainHtml = "";
                                String templateid = StringUtil.isNullOrEmpty(request.getParameter("templateid")) ? "" : request.getParameter("templateid");
                                String templateName = StringUtil.isNullOrEmpty(request.getParameter("templateName")) ? "" : request.getParameter("templateName");
                                if (!StringUtil.isNullOrEmpty(templateid) && !Constants.NONE.equalsIgnoreCase(templateid)) {
                                    JSONObject requestParams = StringUtil.convertRequestToJsonObject(request);
                                    if (extrapreferences.isActivateDDTemplateFlow()) {
                                        mainHtml = accDocumentDesignService.getHTMLContentForEmailWithDDTemplate(requestParams);
                                    }
                                    if (extrapreferences.isActivateDDInsertTemplateLink()) {
                                        StringBuilder appendString = new StringBuilder();
                                        requestParams.put(Constants.isdefaultHeaderMap,true);
                                        String resturl = accDocumentDesignService.PrintTemplateRestUrl(requestParams);
                                        if(!StringUtil.isNullOrEmpty(hyperlinkText)){
                                            templateName = hyperlinkText;
                                        }
                                        String templateurl = "<a href='" + resturl + "' target='_blank'>" + templateName + "</a>";
                        
                                        if(plainMsg.contains(hyperlinkFieldLabel)){
                                            plainMsg = plainMsg.replaceAll("#" + hyperlinkFieldLabel + "#", templateurl);
                                        } else{
                                            appendString.append(mainHtml);
                                            appendString.append("<b>Please click on link below to preview template: </b> <br/>" + templateurl);
                                            mainHtml = appendString.toString();
                    }
             }

           }
                                // Replace placeholder with empty text
                                plainMsg = plainMsg.replaceAll("#" + hyperlinkFieldLabel + "#", "");
                                SendMailHandler.postMail(emails, subject, plainMsg+mainHtml, plainMsg+mainHtml, fromID, path, smtpConfigMap);
                                issuccess = true;
        }
            
        }
                        
  }
                } catch (MessagingException e) {  
                    try {
                        throw new MessageSizeExceedingException(e.getMessage()); 
                    } catch (MessageSizeExceedingException exception) {
                        if (StringUtil.isNullOrEmpty(exception.toString())) {
                            Logger.getLogger(CommonFunctions.class.getName()).log(Level.SEVERE, null, e);
                        } else {
                            issuccess = false;
                            jobj.put("success", issuccess);
                            jobj.put("isMsgSizeException", true);
                            jobj.put("msg", exception.toString());
                       }
                    }                                                    
                }
                String entryno = request.getParameter("entryno");
                String moduleName="";
                if (moduleid ==Constants.Acc_Lease_Quotation){
                    moduleName="lease quotation ";
                }else if(moduleid ==Constants.Acc_FixedAssets_Sales_Return_ModuleId){
                    moduleName="assets sales return ";
                }else if(moduleid ==Constants.Acc_Lease_Order_ModuleId){
                    moduleName="lease order ";
                }else if(moduleid ==Constants.Acc_FixedAssets_Purchase_Return_ModuleId){
                    moduleName="assets purchase return ";
                } 
                auditTrailObj.insertAuditLog(AuditAction.SENT_EMAIL, "User " + sessionHandlerImpl.getUserFullName(request) + " has sent Email of "+ moduleName+" "+ entryno, request, "12");
         
                // Below Function is called to update sent Email flag 
                if (moduleid == Constants.Acc_Invoice_ModuleId || moduleid == Constants.Acc_Vendor_Invoice_ModuleId
                       || moduleid == Constants.Acc_Purchase_Order_ModuleId || moduleid == Constants.Acc_Sales_Order_ModuleId 
                       ||  moduleid == Constants.Acc_Make_Payment_ModuleId || moduleid == Constants.Acc_Receive_Payment_ModuleId
                       || moduleid == Constants.Acc_Customer_Quotation_ModuleId) {
                    accCommonTablesDAO.updateSentEmailFlag(moduleid, billid, AccountingManager.getCompanyidFromRequest(request));
                }
    
                } catch (SessionExpiredException ex) {
                    Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception e) {
                issuccess = false;
                 } finally {
                try {
                    if (baos != null) {
                        baos.close();
                    }
                    if (bais != null) {
                        bais.close();
                    }
                    if (os != null) {
                        os.close();
                    }
                    if (fis != null) {
                        fis.close();
                    }
                    if (fos != null) {
                        fos.close();
                    }
                         } catch (IOException ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (jobj.length() == 0) {
                        jobj.put("success", issuccess);
                        jobj.put("msg", messageSource.getMessage("acc.rem.165", null, RequestContextUtils.getLocale(request)));
                    }                        
                }
        }
       return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView sendInvoicesonMail(HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException, IOException, DocumentException, ServiceException, JSONException {
        JSONObject resultJSONObj = new JSONObject();
        String path = HttpUtils.getRequestURL(request).toString();
        String servPath = request.getServletPath();
        try {
            JSONObject requestparam = StringUtil.convertRequestToJsonObject(request);
            requestparam.put("path", path);
            requestparam.put("servPath", servPath);
            AccMailNotifyThread accMailNotifyThread = new AccMailNotifyThread(requestparam);
            accMailNotifyThread.setAccMailNotifyServiceobj(accMailNotifyServiceobj);
            Thread t = new Thread(accMailNotifyThread);
            t.start();
            if (resultJSONObj.length() == 0) {
                resultJSONObj.put("success", true);
                resultJSONObj.put("msg", messageSource.getMessage("acc.rem.165", null, RequestContextUtils.getLocale(request)) + " " + messageSource.getMessage("acc.rem.259", null, RequestContextUtils.getLocale(request)));
            }
        } catch (Exception ex) {
            resultJSONObj.put("msg", ex.getMessage());
            Logger.getLogger(CommonFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", resultJSONObj.toString());
    }
    
    public ModelAndView getInvoiceCreationJson(HttpServletRequest request, HttpServletResponse response ) throws FileNotFoundException, IOException, DocumentException, ServiceException, JSONException {
       JSONObject jobj = new JSONObject();
        {
            ByteArrayOutputStream baos = null;
            boolean issuccess = true;
            try {
                boolean loadTaxStore= Boolean.parseBoolean((String)request.getParameter("loadtaxstore"));
                boolean loadPriceStore = Boolean.parseBoolean((String)request.getParameter("loadpricestore"));
                boolean loadCurrencyStore =  Boolean.parseBoolean((String)request.getParameter("loadcurrencystore"));
                boolean loadTermStore =  Boolean.parseBoolean((String)request.getParameter("loadtermstore"));
                ModelAndView model=null;
                Map map=null;
                String modelStr;
                JSONObject obj;
                if(loadTaxStore){
                    JSONArray DataJArr = accAccountControllerCMNServiceObj.getTaxJson(request);
                    JSONObject jobj1 = new JSONObject();
                    String msg = "";
                    jobj1.put(DATA, DataJArr);
                    jobj1.put(MSG, msg);
//                    model=accountControllerCMN.getTax(request,response);
//                    map = model.getModel();
//                    modelStr = (String) map.get("model");
//                    obj = new JSONObject(modelStr);
//                    obj = new JSONObject(DataJArr.toString());
                     jobj.put("taxdata", jobj1);
                }
                if(loadPriceStore){
                     model=AccProductcontrollerObj.getProducts(request,response);
                     map = model.getModel();
                     modelStr = (String) map.get("model");
                     obj = new JSONObject(modelStr);
                     jobj.put("productdata", obj);
                }
                if(loadTermStore){
                     model=AccTermcontrollerObj.getTerm(request,response);
                     map = model.getModel();
                     modelStr = (String) map.get("model");
                     obj = new JSONObject(modelStr);
                     jobj.put("termdata", obj);
                }
                if(loadCurrencyStore){
                     model=AccCurrencycontrollerObj.getCurrencyExchange(request,response);
                     map = model.getModel();
                     modelStr = (String) map.get("model");
                     obj = new JSONObject(modelStr);
                     jobj.put("currencydata", obj);
                }
            }   catch (Exception e) {
                  issuccess=false;
             } finally {
             
                    jobj.put("success", issuccess);
                    jobj.put("msg", "Json Created");
            }
        }
       return new ModelAndView("jsonView", "model", jobj.toString());
    }
     
    public ModelAndView sendDueDateMail(HttpServletRequest request, HttpServletResponse response ) throws FileNotFoundException, IOException, DocumentException, ServiceException, JSONException {
      JSONObject jobj = new JSONObject();
      KwlReturnObject kmsg = null;
      boolean advanceFlag=false;
      boolean otherwiseFlag=false;
      double advanceAmount = 0;
      HashMap<String, Object> requestmap = null;
    try {
        requestmap = new HashMap<String, Object>();
        Date todayDate=new Date();
        DateFormat formatter ;
        formatter = new SimpleDateFormat("dd-MM-yyyy");
        String today =formatter.format(todayDate);        
        kmsg=accountingHandlerDAOobj.getDuedateCustomerInvoiceInfoList();
        List ll = kmsg.getEntityList();
        String baseUrl = URLUtil.getPageURL(request, loginpageFull);
        if (!StringUtil.isNullOrEmpty(request.getParameter("advanceAmount"))) {
            advanceAmount = Double.parseDouble(request.getParameter("advanceAmount"));
        }

        if (!StringUtil.isNullOrEmpty(request.getParameter("otherwise"))) {
            otherwiseFlag = Boolean.parseBoolean(request.getParameter("otherwise"));
        }

        if (!StringUtil.isNullOrEmpty((String) requestmap.get("advanceFlag"))) {
            advanceFlag = Boolean.parseBoolean(requestmap.get("advanceFlag").toString());
        }

        boolean iscontraentryflag = Boolean.parseBoolean(request.getParameter("contraentryflag"));
        Locale loc = RequestContextUtils.getLocale(request);
        requestmap.put("locale", loc);

        requestmap.put("advanceAmount", advanceAmount);
        requestmap.put("advanceFlag", advanceFlag);
        requestmap.put("otherwiseFlag", otherwiseFlag);
        requestmap.put("baseUrl", baseUrl);
        requestmap.put("iscontraentryflag", iscontraentryflag);
        
        Company company = (Company) kwlCommonTablesDAOObj.getClassObject(Company.class.getName(), sessionHandlerImpl.getCompanyid(request));
        Iterator it = ll.iterator();
           while(it.hasNext()){
                Object[] val = (Object[])it.next();
                Date invDate = new Date();
                ByteArrayOutputStream baos = null;
                String[] path = new String[]{};
                String dateStr = "";
                
                String cName=(val[0]==null)?"":val[0].toString();
                String cEMail=(val[1]==null)?"":val[1].toString();
                String cDueDate=val[2].toString();
                String cINumber=(val[3]==null)?"":val[3].toString();
                String invCurrencyID=(val[4]==null)?"":val[4].toString();               
                String invoiceID=(val[5]==null)?"":val[5].toString();
                boolean withoutinventory=Boolean.parseBoolean(val[6].toString());
                String companyMailID=(val[7]==null)?"":val[7].toString();
                String companyName=(val[8]==null)?"":val[8].toString();
                String companyID=(val[9]==null)?"":val[9].toString();
                String baseCurrency=(val[10]==null)?"":val[10].toString();               
                String userId=(val[11]==null)?"":val[11].toString();
                String fName=(val[12]==null)?"":val[12].toString();
                String lName=(val[13]==null)?"":val[13].toString();
                double amount=(val[14]==null) ? 0.0 : Double.parseDouble(val[14].toString());
                String accname=(val[15]==null)?"":val[15].toString();          
                String logoPath = ProfileImageServlet.getProfileImagePath(request, true, null);
                int mode; 
                if(withoutinventory==true)
                  mode=11;    
                else
                  mode=2;  
                
                if(!cEMail.isEmpty()){
                CreatePDF ExportrecordObj= new CreatePDF();
                ExportrecordObj.setkwlCommonTablesDAO(kwlCommonTablesDAOObj);
                ExportrecordObj.setMessageSource(messageSource);
                ExportrecordObj.setaccInvoiceDAO(accInvoiceDAOobj);
                ExportrecordObj.setauthHandlerDAO(authHandlerDAOObj);
                ExportrecordObj.setaccountingHandlerDAO(accountingHandlerDAOobj);
                ExportrecordObj.setaccTaxDAO(accTaxObj);
                requestmap.put("username", fName+" "+lName);
                requestmap.put(Constants.globalCurrencyKey, baseCurrency);
                requestmap.put(Constants.companyKey, invCurrencyID);
                baos = CreatePDFObj.createPdf(requestmap, invCurrencyID, invoiceID, formatter, mode, amount, logoPath, null, accname, null,false,companyID,userId,baseCurrency);
                 dateStr=formatter.format(invDate);
                 File destDir=new File("");
                 destDir = new File(storageHandlerImpl.GetProfileImgStorePath(), "Transaction"+dateStr+".pdf");
                 FileOutputStream oss = new FileOutputStream(destDir);
                 baos.writeTo(oss);
                 List<String> list = new ArrayList<String>();
                 list.add(destDir.getAbsolutePath());
                 oss.close();
                 path = list.toArray(new String[list.size()]);             
                    
                     String subject="Invoice-"+companyName+"-"+cName+"-"+cINumber;
                     String sender=fName+" "+lName; 
                     String sendorInfo=companyMailID;                                         
                     String htmlTextC="";              
                     htmlTextC+="<br/>Hello "+cName+"<br/>";  
                     htmlTextC+="<br/>We have enclosed your invoice for "+today+"<br/>";
                     htmlTextC+="<br/>For your kind information, the Due Date for payment is "+today+"<br/>";
                     htmlTextC+="<br/>If you have any questions about the invoice, please phone/mail at "+companyMailID+"<br/>";
                     htmlTextC+="<br/>We would be happy to help.<br/>";
                     htmlTextC+="<br/>Thank you for your business. We look forward to working with you.<br/>";
                     htmlTextC+="<br/>Sincerely,<br/>";
                     htmlTextC+="<br/>"+sender+"<br/>";
                     htmlTextC+="<br/>Thank you for your Inquiry. We look forward to work with you.<br/>";
                     htmlTextC+="<br/>Enclosures<br/>Invoice Number:"+cINumber;
                     
                     String plainMsgC="";
                     plainMsgC+="\nHello "+cName+"\n";  
                     plainMsgC+="\nWe have enclosed your invoice for "+today+"\n";
                     plainMsgC+="\nFor your kind information, the Due Date for payment is "+today+"\n";
                     plainMsgC+="\nIf you have any questions about the invoice, please phone/mail at "+companyMailID+"\n";
                     plainMsgC+="\nWe would be happy to help.\n";
                     plainMsgC+="\nThank you for your business. We look forward to working with you.\n";
                     plainMsgC+="\nSincerely,\n";
                     plainMsgC+="\n"+sender+"\n";
                     plainMsgC+="\nThank you for your Inquiry. We look forward to work with you.\n";
                     plainMsgC+="\nEnclosures<br/>Invoice Number:"+cINumber;
                     Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                     SendMailHandler.postMail(new String[]{cEMail},subject,htmlTextC,plainMsgC,sendorInfo,path, smtpConfigMap);
                 }
                
              }
        } catch (Exception e) {
        e.printStackTrace();
    } finally {
        
    }    
   return new ModelAndView("jsonView", "model", jobj.toString());   
  }
  
    public ModelAndView sendLoanDueDatePassedMail(String dbDuedate, String simpleDuedate, String companyId, String companyName, String baseCurrencyId, String userFname,
            String userLname, List userMailid, String mailContent, String mailSubject, NotificationRules nr,boolean isMailToSalesPerson,boolean isMailToContactPerson,String baseurl) throws FileNotFoundException, IOException, DocumentException, ServiceException, JSONException {
      JSONObject jobj = new JSONObject();
      KwlReturnObject kmsg = null;
      boolean advanceFlag=false;
      boolean otherwiseFlag=false;
      double advanceAmount = 0;
      HashMap<String, Object> requestmap = null;
    try {
        boolean isConsignmentLoanOutstadingReport = true;
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put(Constants.companyKey, companyId);
        requestParams.put("isConsignmentLoanOutstadingReport", isConsignmentLoanOutstadingReport);
        requestParams.put("duedate","applicable");
        requestParams.put("finalduedate",dbDuedate);
        requestParams.put("groupbydo",true);
        requestParams.put("groupbyso",false);
        requestmap = new HashMap<String, Object>();
        Date todayDate=new Date();
        DateFormat formatter ;
        formatter = new SimpleDateFormat("dd-MM-yyyy");
        String today =formatter.format(todayDate);  
        String subject="";
        
         
//            if (userMailid.length > 0) {
//                boolean isInvoice = false;
//                int sno = 1;
//                String htmlText = "";
//                String subject = mailSubject;
//                String plainMsg = "";
//                //String from = Constants.ADMIN_EMAILID;
//                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
//                Company company = (Company) returnObject.getEntityList().get(0);
//                String from = (!company.isEmailFromCompanyCreator()) ? Constants.ADMIN_EMAILID : authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
//                htmlText = mailContent.replace(Constants.DateLabel_MailContent_Placeholder, simpleDuedate).replace(Constants.Due_DateLabel_MailContent_Placeholder, simpleDuedate);
//                htmlText = htmlText.replace(Constants.TodayDateLabel_MailContent_Placeholder, today);
//                String dueDateID = new JSONObject(Constants.staticGlobalDateFields.get(Constants.dueDate)).optString("fieldid", "");
//
//                HashMap<String, Object> requestMap = new HashMap<String, Object>();
//        
//        
//            }
        
        
        
        kmsg=accInvoiceDAOobj.getConsignmentLoanDetails(requestParams);
        List ll = kmsg.getEntityList();
        
        
        requestParams.put("groupbydo",false);
        requestParams.put("groupbyso",true);
        KwlReturnObject kmsg1=accInvoiceDAOobj.getConsignmentLoanDetails(requestParams);
        
        
        List ll1 = kmsg1.getEntityList();
        Iterator it1 = ll1.iterator();
        
        List<String> soList=new ArrayList<String>();
        
        while(it1.hasNext()){
            
            Object[] val = (Object[])it1.next();
            soList.add(val[1].toString());
        }
        
        Company company = (Company) kwlCommonTablesDAOObj.getClassObject(Company.class.getName(), companyId);
        Iterator it = ll.iterator();
        List<String> doList=new ArrayList<String>();
           while(it.hasNext()){
                Object[] val = (Object[])it.next();
                Date invDate = new Date();
                ByteArrayOutputStream baos = null;
                String[] path = new String[]{};
                String dateStr = "";
                 DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                String cName=(val[0]==null)?"":val[0].toString();
                 String dodid = (val[0] != null) ? (String) val[0] : "";
                String consignmentRequistid = (val[1] != null) ? (String) val[1] : "";

                KwlReturnObject dodObj = accountingHandlerDAOobj.getObject(DeliveryOrderDetail.class.getName(), dodid);
                DeliveryOrderDetail doDetail = (DeliveryOrderDetail) dodObj.getEntityList().get(0);
                String Donumber=doDetail.getDeliveryOrder().getDeliveryOrderNumber();
                String cEMail=doDetail.getDeliveryOrder() != null ? doDetail.getDeliveryOrder().getCustomer().getEmail():"";
                BillingShippingAddresses billingAddr=doDetail.getDeliveryOrder().getBillingShippingAddresses();
                ArrayList<String>  rlist=new ArrayList<> ();
                rlist.addAll(userMailid);
                if(!StringUtil.isNullOrEmpty(cEMail)){
                   rlist.add(cEMail);
               }
               if (billingAddr != null && !StringUtil.isNullOrEmpty(billingAddr.getShippingEmail()) && isMailToContactPerson) {
                   rlist.add(billingAddr.getShippingEmail());
               }
                dodObj = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), consignmentRequistid);
                SalesOrder cRequest = (SalesOrder) dodObj.getEntityList().get(0);
                String salesPersonEMail=cRequest.getSalesperson() != null ? cRequest.getSalesperson().getEmailID():"";
                
                DeliveryOrder delOr=doDetail.getDeliveryOrder();
                
                if(!StringUtil.isNullOrEmpty(salesPersonEMail) && isMailToSalesPerson){
                    rlist.add(salesPersonEMail);
                }
                String sendorInfo=StringUtil.isNullOrEmpty(company.getEmailID())?authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID()):company.getEmailID();   
                if(!StringUtil.isNullOrEmpty(cEMail) || !StringUtil.isNullOrEmpty(salesPersonEMail) || !rlist.isEmpty()  ){
                            
//                    String subject="Loan Due date is passed";
                     String htmlTextC="";    
                     subject=mailSubject;
                     if(!StringUtil.isNullOrEmpty(subject)){
                     subject=subject.replaceAll("#Customer_Alias#", cRequest.getCustomer().getAliasname()==null?"":cRequest.getCustomer().getAliasname());
                            subject=subject.replaceAll("#Sales_Person#", cRequest.getSalesperson()!=null?cRequest.getSalesperson().getValue():"");
                            subject=subject.replaceAll("#Document_Number#", Donumber);
                            subject=subject.replaceAll("#Customer_Code#", cRequest.getCustomer().getAcccode()==null?"":cRequest.getCustomer().getAcccode());
//                            htmlTextC=mailContent;
//                            htmlTextC=htmlTextC.replaceAll("#Document_Number#", Donumber);
//                            htmlTextC=htmlTextC.replaceAll("#User_Name#", sender.getFullName()); 
                     }
                     String containt = mailContent;
                    if (!StringUtil.isNullOrEmpty(containt)) {
                        containt = containt.replaceAll("#Document_Number#", Donumber);
//                        htmlTextC += "<br/>" + containt + "<br/>";
                    }
                    if (!StringUtil.isNullOrEmpty(containt)) {
                        containt = containt.replaceAll("#Customer_Code#", cRequest.getCustomer().getAcccode() == null ? "" : cRequest.getCustomer().getAcccode());
                    }
                    htmlTextC += "<br/>" + containt + "<br/>";
//                     htmlTextC+=" <br/>Request Number</b> :"+cRequest.getSalesOrderNumber();
//                     htmlTextC+="<br/> For your kind information, the Consignment Loan Due Date is passed for the following Request: <br/> <b> <br/>Request Number</b> :"+cRequest.getSalesOrderNumber();
                     htmlTextC += "<br/><b>Delivery Order No :</b> "+Donumber + "</b>";
                     htmlTextC += "<br/><b>Customer Name :</b> "+cRequest.getCustomer().getName() + "</b>";
                     htmlTextC += "<br/><b>From Date :</b>     "+formatter.format(cRequest.getFromdate()) + "</b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b>To Date :</b>      "
                             + " "+formatter.format(cRequest.getTodate()) + "</b> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b> DO Date :</b>       "+ formatter.format(doDetail.getDeliveryOrder().getOrderDate()) +"</b>";
                     DeliveryOrder dor=doDetail.getDeliveryOrder();
                     
                     
                     htmlTextC += "<div style='width:650px'> <br/></br>"
                             + "<table style='border:1px solid black' width='100%' cellpadding='4' >"
                             + "<tr bgcolor=#DDDDDD height=25>"
                             + "<th align=left style='padding:0.5em' width='30px' height=25>  No. </th>"
                             + "<th align=left style='padding:0.5em' width='50px'> Product ID </th>"
                             + "<th align=left style='padding:0.5em' width='200px'> Product Descritpion </th>"
                             + "<th align=left style='padding:0.5em' width='50px'> Quantity </th>"
                             + "<th align=left style='padding:0.5em' width='50px'> UoM </th>"
                             + "<th align=left style='padding:0.5em' width='60px'> Request No </th></tr>";
                     
                    int rows = 0;
                    List<DeliveryOrderDetail> DodList=new ArrayList<DeliveryOrderDetail>();
                    
                    for (DeliveryOrderDetail dodt : delOr.getRows()) {
                        DodList.add(dodt);
                    }

                    Collections.sort(DodList, new Comparator<DeliveryOrderDetail>() {

                        public int compare(DeliveryOrderDetail s1, DeliveryOrderDetail s2) {
                            return Integer.valueOf(s1.getSrno()).compareTo(s2.getSrno());
                        }
                    });
   

                    for (DeliveryOrderDetail doDetail1 : DodList) {
                        SalesOrder so=doDetail1.getSodetails().getSalesOrder();
//                        KwlReturnObject dodObj1 = accInvoiceDAOobj.getDOdBySodID(sodtl.getID());
                        if (soList.contains(so.getID())) {
//                            DeliveryOrderDetail doDetail1 = (DeliveryOrderDetail) dodObj1.getEntityList().get(0);
                            double returnQty = 0;
                            KwlReturnObject returnDtl = accInvoiceDAOobj.getPartialFullSalesReturnDetailsByDOId(companyId, doDetail1.getID());
                            if (returnDtl.getEntityList() != null && !returnDtl.getEntityList().isEmpty() && returnDtl.getEntityList().size() > 0) {
                                Object[] dtlList = (Object[]) returnDtl.getEntityList().get(0);
                                returnQty = (dtlList != null) ? dtlList[2] != null ? (Double) dtlList[2] : 0 : 0;
                            }
                            if ((doDetail1.getBaseuomquantity() - returnQty) > 0) {
                                rows++;
                                htmlTextC += "<tr style='padding:0.5em'><td align=left>" + rows + "</td><td align=left> " + doDetail1.getProduct().getProductid() + "   </td><td align=left> " + doDetail1.getProduct().getDescription() + " </td><td align=left> " + (doDetail1.getBaseuomquantity() - returnQty) + " </td><td align=left>" + doDetail1.getUom().getNameEmptyforNA() + "</td><td align=left>" + so.getSalesOrderNumber() + "</td></tr>";
                            }
                        }
                    }
                     htmlTextC+="</table></div><br>";
//                     for (DeliveryOrderDetail dodtl : dor.getRows()) {
//                        htmlTextC += "<br/><b> Product :</b>     " + dodtl.getProduct().getProductid() + "</b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b>Description :</b>      "
//                                + " " + dodtl.getProduct().getDescription() + "</b> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b> Quantity :</b>       " + dodtl.getDeliveredQuantity() + "</b>";
//                    }
                     String plainMsgC = "";
                     plainMsgC += "\nRegards,\n";
                     plainMsgC += "\nDeskera Financials\n";
                     plainMsgC += "\n\n";
                     plainMsgC += "\nThis is an auto generated email"+baseurl+". Do not reply.\n";
                     Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                     String[] emails = rlist.toArray(new String[rlist.size()]); // Converted Arraylist to String Array
                     sendorInfo=!StringUtil.isNullOrEmpty(nr.getSenderid())?nr.getSenderid():sendorInfo;
                     SendMailHandler.postMail(emails,subject,htmlTextC,plainMsgC,sendorInfo,path, smtpConfigMap);
                 }
                
              }
        } catch (Exception e) {
          e.printStackTrace();
    } finally {
        
    }    
   return new ModelAndView("jsonView", "model", jobj.toString());   
  }
  
  public ModelAndView sendUserDueDateMail(HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException, IOException, DocumentException, ServiceException, JSONException {
     JSONObject jobj = new JSONObject();
      KwlReturnObject kwlCompany=null;
      KwlReturnObject result=null;
      Company company=null;
      List ll;
      String msg=null,companyId,baseCurrencyId,companyName;
     try{    
        KwlReturnObject fieldParamResult = null;
        HashMap<String, Object> requestParams = new HashMap<String, Object>();     
        String dueDateID = "";
        String invoiceDateID = "";
        String scheduleEndDateID = "";
        String selfBilledApprovalExpiryDateID = "";
        String proExpDateID = "",ProdGRExpDateID="";
        String groDoCheckDateID = ""; 
        DateFormat dbformat=new SimpleDateFormat("yyyy-MM-dd");
        DateFormat simpleformat=new SimpleDateFormat("dd-MM-yyyy"); 
        DateFormat datef=authHandler.getDateOnlyFormat();
        kwlCompany=accountingHandlerDAOobj.getCompanyList();
        ll=kwlCompany.getEntityList();
        Iterator iterator=ll.iterator();
        while(iterator.hasNext()){
             company=(Company)iterator.next();
             companyId=company.getCompanyID();
             companyName=company.getCompanyName();
             String baseUrl = com.krawler.common.util.URLUtil.getPageURL(request,loginpageFull,company.getSubDomain());
             String URl="";
             baseCurrencyId=company.getCurrency().getCurrencyID();
             
             /**0=Before Due Date
             1=On Due Date
             2=After Due Date
             
             0=Vendor Invoice
             1=Customer Invoice
             2=Journal Entry report
              */
             
             int moduleId, days, beforeAfter;
             /*
             Set contract expiry status for todays expiring Contract
             */
             setContractExpiryStatus(companyId);
             String fieldid = "", mailContent = "", mailSubject = "";
             boolean isMailToSalesPerson = false;
             boolean isMailToContactPerson = false;
             boolean isMailToAssignedPerson = false;
             result = accountingHandlerDAOobj.getNotifications(companyId);
             List<NotificationRules> list = result.getEntityList();
             for (NotificationRules nr : list) {
                 Calendar cld = Calendar.getInstance();                 
                 moduleId = nr.getModuleId();
                 fieldid = nr.getFieldid();
                 isMailToSalesPerson = nr.isMailToSalesPerson();
                 isMailToContactPerson = nr.isMailToContactPerson();
                 isMailToAssignedPerson = nr.isMailToAssignedTo();
                 days = nr.getDays();
                 mailContent = nr.getMailcontent();
                 URl ="<br> Note: This is an auto-generated notification from "+ baseUrl ;
//                 mailContent +="<br> please check on " + baseUrl +"<br><br>";
                 mailSubject = nr.getMailsubject();
                 String otherEmails = nr.getEmailids();
                 beforeAfter = nr.getBeforeafter();
                 if (beforeAfter == 2 && fieldid.equals("35")) {
                     days = -(days+1);
                 }
                 else if (beforeAfter == 0 && fieldid.equals("35")) {
                     days = (days+1);
                 }else if(beforeAfter == 2){
                     days = -days;
                 }
                 cld.add(Calendar.DATE, days);
                 Date advdueDate = cld.getTime();
                 String advddate=datef.format(advdueDate);
                 try{
                     advdueDate=datef.parse(advddate);
                 }catch(ParseException ex){
                     advdueDate = cld.getTime();
                 }
                 String dbDuedate = dbformat.format(advdueDate);
                 String simpleDuedate = simpleformat.format(advdueDate);
                 String userLname = "", userFname = "";
                 List<String> userEmailList = new ArrayList<String>();
                 if (!StringUtil.isNullOrEmpty(nr.getUsers()) || !StringUtil.isNullOrEmpty(otherEmails)) {
                     if (!StringUtil.isNullOrEmpty(nr.getUsers())) {
                     String[] userids = nr.getUsers().split(",");
                     
                     if (userids.length > 0) {
                         KwlReturnObject userDetailobj = accountingHandlerDAOobj.getUserDetailObj(userids);
                         List<User> user = userDetailobj.getEntityList();
                         for (User ur : user) {
                             userLname = StringUtil.isNullOrEmpty(ur.getLastName()) ? "" : ur.getLastName();
                             userFname = StringUtil.isNullOrEmpty(ur.getFirstName()) ? "" : ur.getFirstName();
                             String Mailid = StringUtil.isNullOrEmpty(ur.getEmailID()) ? "" : ur.getEmailID();
                             if (!StringUtil.isNullOrEmpty(Mailid)) {
                                 userEmailList.add(Mailid);
                         }
                     }
                     }
                     }
                     if(!StringUtil.isNullOrEmpty(otherEmails)){
                         String[] otherMails = otherEmails.split(",");
                         for(String Mailid : otherMails) {
                             userEmailList.add(Mailid);
                         }
                     }
                     switch(moduleId){
                         case Constants.Acc_Invoice_ModuleId: 
                             dueDateID = new JSONObject(Constants.staticGlobalDateFields.get(Constants.dueDate)).optString("fieldid","");
                             invoiceDateID = new JSONObject(Constants.staticGlobalDateFields.get(Constants.Invoice_Date)).optString("fieldid","");
                             break;
                         case Constants.Acc_Vendor_Invoice_ModuleId:  
                             dueDateID = new JSONObject(Constants.staticGlobalDateFields.get(Constants.dueDate)).optString("fieldid","");
                             invoiceDateID = new JSONObject(Constants.staticGlobalDateFields.get(Constants.Invoice_Date)).optString("fieldid","");
                             break;
                         case Constants.Acc_GENERAL_LEDGER_ModuleId :
                             dueDateID = new JSONObject(Constants.staticGlobalDateFields.get(Constants.JE_Date)).optString("fieldid","");
                             break;
                         case Constants.Acc_Sales_Order_ModuleId:    
                             dueDateID = new JSONObject(Constants.staticGlobalDateFields.get(Constants.SO_Date)).optString("fieldid","");
                             break;
                         case Constants.Acc_Purchase_Order_ModuleId: 
                             dueDateID = new JSONObject(Constants.staticGlobalDateFields.get(Constants.PO_Date)).optString("fieldid","");
                             break;
                         case Constants.Acc_Delivery_Order_ModuleId: 
                             dueDateID = new JSONObject(Constants.staticGlobalDateFields.get(Constants.DO_Date)).optString("fieldid","");
                             proExpDateID=new JSONObject(Constants.staticGlobalDateFields.get(Constants.DOEXp_Date)).optString("fieldid","");
                             break; 
                         case Constants.Acc_Goods_Receipt_ModuleId: 
                             dueDateID = new JSONObject(Constants.staticGlobalDateFields.get(Constants.GRO_Date)).optString("fieldid","");
                             ProdGRExpDateID = new JSONObject(Constants.staticGlobalDateFields.get(Constants.GROEXp_Date)).optString("fieldid","");
                             groDoCheckDateID=new JSONObject(Constants.staticGlobalDateFields.get(Constants.GR_DO_Sr_Check_Date)).optString("fieldid","");
                             break; 
                         case Constants.Acc_Sales_Return_ModuleId: 
                             dueDateID = new JSONObject(Constants.staticGlobalDateFields.get(Constants.SR_Date)).optString("fieldid","");
                             break; 
                         case Constants.Acc_Purchase_Return_ModuleId: 
                             dueDateID = new JSONObject(Constants.staticGlobalDateFields.get(Constants.PR_Date)).optString("fieldid","");
                             break; 
                         case Constants.Acc_Customer_ModuleId: 
                             dueDateID = new JSONObject(Constants.staticGlobalDateFields.get(Constants.CUST_CREATION_Date)).optString("fieldid","");
                             break; 
                         case Constants.Acc_Vendor_ModuleId: 
                             dueDateID = new JSONObject(Constants.staticGlobalDateFields.get(Constants.VEND_CREATION_Date)).optString("fieldid", "");
                             selfBilledApprovalExpiryDateID = new JSONObject(Constants.staticGlobalDateFields.get(Constants.VEND_Self_Billed_Approval_Expiry_Date)).optString("fieldid", "");
                             break;
                         case Constants.Acc_Contract_Order_ModuleId: 
                             dueDateID = new JSONObject(Constants.staticGlobalDateFields.get(Constants.CONTRACT_EXPIRY_DATE)).optString("fieldid","");
                             break; 
                         case Constants.Asset_Maintenance_ModuleId: 
                             dueDateID = new JSONObject(Constants.staticGlobalDateFields.get(Constants.Asset_Schedule_Start_Date)).optString("fieldid","");
                             scheduleEndDateID = new JSONObject(Constants.staticGlobalDateFields.get(Constants.Asset_Schedule_End_Date)).optString("fieldid","");
                             break; 
                         case Constants.Acc_Product_Master_ModuleId:
                             dueDateID = new JSONObject(Constants.staticGlobalDateFields.get(Constants.Product_Purchase_Date)).optString("fieldid", "");
                             proExpDateID=new JSONObject(Constants.staticGlobalDateFields.get(Constants.Product_Expiry_Date)).optString("fieldid","");
                             break;
                         case Constants.CONSIGNMENT_SALES_MODULE:
                             dueDateID = new JSONObject(Constants.staticGlobalDateFields.get(Constants.ConsignmentSales_DueDate_Passed)).optString("fieldid", "");
                             break;
                     }
                    String[] userMailid = userEmailList.toArray(new String[userEmailList.size()]);
                    if (moduleId == Constants.Acc_Invoice_ModuleId && (fieldid.equals(dueDateID) || fieldid.equals(invoiceDateID))) {
                        sendCustomerInvoiceMail(dbDuedate, simpleDuedate, companyId, companyName, baseCurrencyId, userFname, userLname, 
                                userMailid, mailContent, mailSubject,nr,URl);
                    } else if (moduleId == Constants.Acc_Vendor_Invoice_ModuleId && (fieldid.equals(dueDateID) || fieldid.equals(invoiceDateID))) {
                        sendVendorInvoiceMail(dbDuedate, simpleDuedate, companyId, companyName, baseCurrencyId, userFname, userLname,
                                userMailid, mailContent, mailSubject,nr,URl);                      
                    } else if (moduleId == Constants.Acc_GENERAL_LEDGER_ModuleId&& fieldid.equals(dueDateID)) {
                        Date advdueDate1 = cld.getTime();
                        advdueDate1 = simpleformat.parse(simpleformat.format(advdueDate1));
                        Calendar cld1 = Calendar.getInstance();
                        cld1.add(Calendar.DATE, days);
                        cld1.add(Calendar.HOUR, 24);
                        Date advdueDate2 = cld1.getTime();
                        advdueDate2 = simpleformat.parse(simpleformat.format(advdueDate2));
                        sendJournalEntryMail(advdueDate1.getTime(), advdueDate2.getTime(), simpleDuedate, companyId, companyName, 
                                baseCurrencyId, userFname, userLname, userMailid, mailContent, mailSubject,URl);
                    } else if (moduleId == Constants.Acc_Sales_Order_ModuleId && fieldid.equals(dueDateID)){
                        sendSalesOrderMail(dbDuedate, simpleDuedate, companyId, companyName, baseCurrencyId, userFname, userLname, 
                                userMailid, mailContent, mailSubject,nr,URl);                        
                    } else if (moduleId == Constants.Acc_Purchase_Order_ModuleId && fieldid.equals(dueDateID)){
                        sendPurchaseOrderMail(dbDuedate, simpleDuedate, companyId, companyName, baseCurrencyId, userFname, userLname, 
                                userMailid, mailContent, mailSubject,nr,URl);
                    } else if (moduleId == Constants.Acc_Delivery_Order_ModuleId && fieldid.equals(dueDateID)){
                        sendDeliveryOrderMail(dbDuedate, simpleDuedate, companyId, companyName, baseCurrencyId, userFname, userLname, 
                                userMailid, mailContent, mailSubject,nr,URl);
                    } else if (moduleId == Constants.Acc_Delivery_Order_ModuleId && fieldid.equals(proExpDateID)){
                        sendProductExpDeliveryOrderMail(dbDuedate, simpleDuedate, companyId, companyName, baseCurrencyId, userFname, userLname, 
                                userMailid, mailContent, mailSubject,URl);
                    } else if (moduleId == Constants.Acc_Goods_Receipt_ModuleId && fieldid.equals(dueDateID)){
                        sendGoodsReceiptOrderMail(dbDuedate, simpleDuedate, companyId, companyName, baseCurrencyId, userFname, userLname, 
                                userMailid, mailContent, mailSubject,nr,URl);
                    } else if (moduleId == Constants.Acc_Goods_Receipt_ModuleId && fieldid.equals(ProdGRExpDateID)){
                       sendProductExpGoodsReceiptOrderMail(dbDuedate, simpleDuedate, companyId, companyName, baseCurrencyId, userFname, userLname, 
                                userMailid, mailContent, mailSubject,URl);
                    } else if (moduleId == Constants.Acc_Goods_Receipt_ModuleId && fieldid.equals(groDoCheckDateID)){
                       sendGROrderDeliveryOrderSerialCheckMail(dbDuedate, simpleDuedate, companyId, companyName, baseCurrencyId, userFname, userLname, 
                                userMailid, mailContent, mailSubject,nr,URl);
                    } else if (moduleId == Constants.Acc_Sales_Return_ModuleId && fieldid.equals(dueDateID)){
                        sendSalesReturnMail(dbDuedate, simpleDuedate, companyId, companyName, baseCurrencyId, userFname, userLname, 
                                userMailid, mailContent, mailSubject,nr,URl);
                    } else if (moduleId == Constants.Acc_Purchase_Return_ModuleId && fieldid.equals(dueDateID)){
                        sendPurchaseReturnMail(dbDuedate, simpleDuedate, companyId, companyName, baseCurrencyId, userFname, userLname, 
                                userMailid, mailContent, mailSubject,nr,URl);
                    } else if (moduleId == Constants.Acc_Customer_ModuleId && fieldid.equals(dueDateID)){
                        sendCustomerCreationMail(dbDuedate, simpleDuedate, companyId, companyName, baseCurrencyId, userFname, userLname, 
                                userMailid, mailContent, mailSubject,URl);
                    } else if (moduleId == Constants.Acc_Vendor_ModuleId && fieldid.equals(dueDateID)){
                        sendVendorCreationMail(dbDuedate, simpleDuedate, companyId, companyName, baseCurrencyId, userFname, userLname, 
                                userMailid, mailContent, mailSubject,true,URl);
                    } else if (moduleId == Constants.Acc_Vendor_ModuleId && fieldid.equals(selfBilledApprovalExpiryDateID)){
                        sendVendorCreationMail(dbDuedate, simpleDuedate, companyId, companyName, baseCurrencyId, userFname, userLname, 
                                userMailid, mailContent, mailSubject,false,URl);
                    } else if (moduleId == Constants.Acc_Contract_Order_ModuleId && fieldid.equals(dueDateID)){
                        sendcontractExpiryNotificationMail(advdueDate, simpleDuedate, companyId, companyName, baseCurrencyId, userFname, userLname, 
                                userEmailList, mailContent, mailSubject, isMailToSalesPerson,URl);
                    } else if (moduleId == Constants.Asset_Maintenance_ModuleId && fieldid.equals(dueDateID)){
                        sendAssetMaintenanceMail(dbDuedate, simpleDuedate, companyId, companyName, baseCurrencyId, userFname, userLname, 
                                userEmailList, mailContent, mailSubject, isMailToAssignedPerson,true,URl);
                    } else if (moduleId == Constants.Asset_Maintenance_ModuleId && fieldid.equals(scheduleEndDateID)){
                        sendAssetMaintenanceMail(dbDuedate, simpleDuedate, companyId, companyName, baseCurrencyId, userFname, userLname, 
                                userEmailList, mailContent, mailSubject, isMailToAssignedPerson,false,URl);
                    } else if (moduleId == Constants.Acc_Product_Master_ModuleId && fieldid.equals(dueDateID)){
                        sendProductNotSoldMail(dbDuedate, simpleDuedate, companyId, companyName, baseCurrencyId, userFname, userLname, 
                                userMailid, mailContent, mailSubject,URl);
                    } else if (moduleId == Constants.Acc_Product_Master_ModuleId && fieldid.equals(proExpDateID)){
                        sendProductExpiryNotificationMail(companyId, companyName, baseCurrencyId, userFname, userLname, 
                                userMailid, mailContent, mailSubject,beforeAfter,days,URl);
                    } else if (moduleId == Constants.CONSIGNMENT_SALES_MODULE && fieldid.equals(dueDateID)){
                        sendLoanDueDatePassedMail(dbDuedate, simpleDuedate, companyId, companyName, baseCurrencyId, userFname, userLname, 
                                userEmailList, mailContent, mailSubject,nr,isMailToSalesPerson,isMailToContactPerson,URl);
                    } else { // if custom field
                       Date advdueDate1 = cld.getTime();
                       advdueDate1 = simpleformat.parse(simpleformat.format(advdueDate1));
                       Calendar cld1 = Calendar.getInstance();
                       cld1.add(Calendar.DATE, days);
                       cld1.add(Calendar.HOUR, 24);
                       Date advdueDate2 = cld1.getTime();
                       advdueDate2 = simpleformat.parse(simpleformat.format(advdueDate2));

                       // Get Field Info
                       int colnum = 0;
                       boolean isLineItem = false;
                       String fieldLabel = "";
                       requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.Acc_id));
                       requestParams.put(Constants.filter_values, Arrays.asList(companyId, nr.getModuleId(), nr.getFieldid()));
                       fieldParamResult = accAccountDAOobj.getFieldParams(requestParams);
                       List<FieldParams> fields = fieldParamResult.getEntityList();
                       for(FieldParams field : fields) {
                           colnum = field.getColnum();
                           isLineItem = field.getCustomcolumn()==0 ? false : true;
                           fieldLabel = field.getFieldlabel();
                       }

                       String colField = "";
                       if(colnum!=0) {
                           colField = "col".concat(String.valueOf(colnum));
                       }

                       switch(moduleId) {
                           case Constants.Acc_Invoice_ModuleId: 
                               if(isLineItem) { // if line level date field
                                   sendCustomerInvoiceLineCustomDateFieldMails(fieldLabel, colField, colnum, companyId, 
                                           advdueDate1, advdueDate2, userMailid, simpleformat, simpleDuedate, mailContent, mailSubject, nr,URl);
                               } else { // if global level date field
                                   sendInvoiceCustomDateFieldMails(fieldLabel, colField, colnum, companyId, 
                                           advdueDate1, advdueDate2, userMailid, simpleformat, simpleDuedate, mailContent, mailSubject, nr,URl);
                               }
                               break;
                           case Constants.Acc_Vendor_Invoice_ModuleId: 
                               if(isLineItem) { // if line level date field
                                   sendVendorInvoiceLineCustomDateFieldMails(fieldLabel, colField, colnum, companyId, 
                                           advdueDate1, advdueDate2, userMailid, simpleformat, simpleDuedate, mailContent, mailSubject,URl);
                               } else { // if global level date field
                                   sendVendorInvoiceCustomDateFieldMails(fieldLabel, colField, colnum, companyId, 
                                           advdueDate1, advdueDate2, userMailid, simpleformat, simpleDuedate, mailContent, mailSubject,URl);
                               }
                               break;
                           case Constants.Acc_Customer_Quotation_ModuleId: 
                               if(isLineItem) { // if line level date field
                                   sendCustomerQuotationDetailLineCustomDateFieldMails(fieldLabel, colField, colnum, companyId, advdueDate1, 
                                           advdueDate2, userMailid, simpleformat, simpleDuedate, mailContent, mailSubject,URl);
                               } else { // if global level date field
                                   sendCustomerQuotationCustomDateFieldMails(fieldLabel, colField, colnum, companyId, 
                                           advdueDate1, advdueDate2, userMailid, simpleformat, simpleDuedate, mailContent, mailSubject,URl);
                               }
                               break;
                           case Constants.Acc_Vendor_Quotation_ModuleId: 
                               if(isLineItem) { // if line level date field
                                   sendVendorQuotationDetailLineCustomDateFieldMails(fieldLabel, colField, colnum, companyId, advdueDate1, 
                                           advdueDate2, userMailid, simpleformat, simpleDuedate, mailContent, mailSubject,URl);
                               } else { // if global level date field
                                     sendVendorQuotationCustomDateFieldMails(fieldLabel, colField, colnum, companyId, 
                                           advdueDate1, advdueDate2, userMailid, simpleformat, simpleDuedate, mailContent, mailSubject,URl);
                               }
                               break;
                           case Constants.Acc_Sales_Order_ModuleId: 
                               if (!fieldid.equalsIgnoreCase(Constants.Mailnotification_Approval_Fieldid) && !fieldid.equalsIgnoreCase(Constants.Mailnotification_Rejection_Fieldid)) {//mail should not get sent when Fieldid is 23 and 22 which are for approval and rejection 
                                   if (isLineItem) { // if line level date field
                                       sendSalesOrderDetailLineCustomDateFieldMails(fieldLabel, colField, colnum, companyId, advdueDate1,
                                               advdueDate2, userMailid, simpleformat, simpleDuedate, mailContent, mailSubject,URl);
                                   } else { // if global level date field
                                       sendSalesOrderCustomDateFieldMails(fieldLabel, colField, colnum, companyId,
                                               advdueDate1, advdueDate2, userMailid, simpleformat, simpleDuedate, mailContent, mailSubject,URl);
                                   }
                               }
                               break;
                           case Constants.Acc_Purchase_Order_ModuleId: 
                               if(isLineItem) { // if line level date field
                                   sendPurchaseOrderDetailLineCustomDateFieldMails(fieldLabel, colField, colnum, companyId, 
                                           advdueDate1, advdueDate2, userMailid, simpleformat, simpleDuedate, mailContent, mailSubject,URl);
                               } else { // if global level date field
                                   sendPurchaseOrderCustomDateFieldMails(fieldLabel, colField, colnum, companyId, 
                                           advdueDate1, advdueDate2, userMailid, simpleformat, simpleDuedate, mailContent, mailSubject,URl);
                               }                               
                               break;
                           case Constants.Acc_Delivery_Order_ModuleId: 
                               if(isLineItem) { // if line level date field
                                   sendDeliveryOrderDetailLineCustomDateFieldMails(fieldLabel, colField, colnum, companyId, 
                                           advdueDate1, advdueDate2, userMailid, simpleformat, simpleDuedate, mailContent, mailSubject,URl);
                               } else { // if global level date field
                                   sendDeliveryOrderCustomDateFieldMails(fieldLabel, colField, colnum, companyId, 
                                           advdueDate1, advdueDate2, userMailid, simpleformat, simpleDuedate, mailContent, mailSubject,URl);
                               }                               
                               break;
                           case Constants.Acc_Goods_Receipt_ModuleId: 
                               if(isLineItem) { // if line level date field
                                   sendGoodsReceiptOrderDetailLineCustomDateFieldMails(fieldLabel, colField, colnum, companyId, 
                                           advdueDate1, advdueDate2, userMailid, simpleformat, simpleDuedate, mailContent, mailSubject,URl);
                               } else { // if global level date field
                                   sendGoodsReceiptOrderCustomDateFieldMails(fieldLabel, colField, colnum, companyId, 
                                           advdueDate1, advdueDate2, userMailid, simpleformat, simpleDuedate, mailContent, mailSubject,URl);
                               }                               
                               break;
                           case Constants.Acc_GENERAL_LEDGER_ModuleId: // For global level date field
                                   sendJournalEntryCustomDateFieldMails(fieldLabel, colField, colnum, companyId, 
                                           advdueDate1, advdueDate2, userMailid, simpleformat, simpleDuedate, mailContent, mailSubject,URl);
                               break;
                           case Constants.Acc_Sales_Return_ModuleId: 
                               if(isLineItem) { // if line level date field
                                   sendSalesReturnDetailLineCustomDateFieldMails(fieldLabel, colField, colnum, companyId, 
                                           advdueDate1, advdueDate2, userMailid, simpleformat, simpleDuedate, mailContent, mailSubject,URl);
                               } else { // if global level date field
                                   sendSalesReturnCustomDateFieldMails(fieldLabel, colField, colnum, companyId, 
                                           advdueDate1, advdueDate2, userMailid, simpleformat, simpleDuedate, mailContent, mailSubject,URl);
                               }                               
                               break;
                           case Constants.Acc_Purchase_Return_ModuleId: 
                               if(isLineItem) { // if line level date field
                                   sendPurchaseReturnDetailLineCustomDateFieldMails(fieldLabel, colField, colnum, companyId, 
                                           advdueDate1, advdueDate2, userMailid, simpleformat, simpleDuedate, mailContent, mailSubject,URl);
                               } else { // if global level date field
                                   sendPurchaseReturnCustomDateFieldMails(fieldLabel, colField, colnum, companyId, 
                                           advdueDate1, advdueDate2, userMailid, simpleformat, simpleDuedate, mailContent, mailSubject,URl);
                               }                               
                               break;
                           case Constants.Acc_Customer_ModuleId: // For global level date field
                               sendCustomerCustomDateFieldMails(fieldLabel, colField, colnum, companyId, 
                                           advdueDate1, advdueDate2, userMailid, simpleformat, simpleDuedate, mailContent, mailSubject,URl);
                               break;
                           case Constants.Acc_Vendor_ModuleId: // For global level date field
                               if(fieldid == Constants.staticGlobalDateFields.get(Constants.VEND_CREATION_Date)){
                               sendVendorCustomDateFieldMails(fieldLabel, colField, colnum, companyId,advdueDate1, advdueDate2, userMailid, simpleformat, simpleDuedate, mailContent, mailSubject,true,URl);
                               }else{
                               sendVendorCustomDateFieldMails(fieldLabel, colField, colnum, companyId,advdueDate1, advdueDate2, userMailid, simpleformat, simpleDuedate, mailContent, mailSubject,false,URl);
                               }
                               break;                         
                       }
                    }
                }
             }
             }
        }catch(Exception ex){
            msg=ex.getMessage();
        }finally{
            jobj.put("msg",msg);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());   
  }
  
  private void sendVendorInvoiceCustomDateFieldMails(String fieldLabel, String colField, int colnum, String companyId, Date advdueDate1, Date advdueDate2, 
          String[] userMailID, DateFormat simpleformat, String simpleDuedate, String mailContent, String mailSubject,String baseurl) {
      try {
        List finalData = new ArrayList();
        int sno=0;
        List headerItems = new ArrayList();
        headerItems.add("S.No.");
        headerItems.add("Invoice Number");
        headerItems.add("Creation Date");
        headerItems.add(fieldLabel);
        
        HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
        ArrayList Detailfilter_names = new ArrayList(),Detailfilter_params = new ArrayList();
        Detailfilter_names.add("inv.company.companyID");
        Detailfilter_params.add(companyId);
        Detailfilter_names.add("inv.journalEntry.accBillInvCustomData.moduleId");
        Detailfilter_params.add(Constants.Acc_Vendor_Invoice_ModuleId+"");
        if (!StringUtil.isNullOrEmpty(colField)) {
            Detailfilter_names.add(">=inv.journalEntry.accBillInvCustomData.".concat(colField));
            Detailfilter_params.add(advdueDate1);
            Detailfilter_names.add("<=inv.journalEntry.accBillInvCustomData.".concat(colField));
            Detailfilter_params.add(advdueDate2);
        }
        invDetailRequestParams.put("filter_names", Detailfilter_names);
        invDetailRequestParams.put("filter_params", Detailfilter_params);
        KwlReturnObject idcustresult = accountingHandlerDAOobj.getVendorInvoiceCustomFields(invDetailRequestParams);
        if(idcustresult.getEntityList().size()>0) {
            List<GoodsReceipt> invoiceList = idcustresult.getEntityList();
            for(GoodsReceipt invoice : invoiceList) {
                List data = new ArrayList();
                if (invoice != null) {
                    AccJECustomData customData =  invoice.getJournalEntry().getAccBillInvCustomData();
//                    long value = 0;
                    String value="";
                    if(colnum!=0) {
                         value =simpleformat.format(convertCustomStringDateToDateObject(customData.getCol(colnum)));
                    }
                    data.add(++sno);
                    data.add(invoice.getGoodsReceiptNumber());
                    data.add(simpleformat.format(new Date(invoice.getCreatedon())));
                    data.add(value);
                    finalData.add(data);
                }
            }
        }
        sendMail(finalData, headerItems, simpleformat, companyId, simpleDuedate, userMailID, mailContent,mailSubject,baseurl);
      } catch(Exception ex) {
          ex.printStackTrace();
      }
  }

  private void sendInvoiceCustomDateFieldMails(String fieldLabel, String colField, int colnum, String companyId, Date advdueDate1, Date advdueDate2, 
          String[] userMailID, DateFormat simpleformat, String simpleDuedate, String mailContent, String mailSubject, NotificationRules nr,String baseurl) {
      try {
        List finalData = new ArrayList();
        int sno=0;
        List headerItems = new ArrayList();
        headerItems.add("S.No.");
        headerItems.add("Invoice Number");
        headerItems.add("Creation Date");
        headerItems.add(fieldLabel);
        
        HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
        ArrayList Detailfilter_names = new ArrayList(),Detailfilter_params = new ArrayList();
        Detailfilter_names.add("inv.company.companyID");
        Detailfilter_params.add(companyId);
        Detailfilter_names.add("inv.journalEntry.accBillInvCustomData.moduleId");
        Detailfilter_params.add(Constants.Acc_Invoice_ModuleId+"");
        if (!StringUtil.isNullOrEmpty(colField)) {
            Detailfilter_names.add(">=inv.journalEntry.accBillInvCustomData.".concat(colField));
            Detailfilter_params.add(advdueDate1);
            Detailfilter_names.add("<=inv.journalEntry.accBillInvCustomData.".concat(colField));
            Detailfilter_params.add(advdueDate2);
        }
        invDetailRequestParams.put("filter_names", Detailfilter_names);
        invDetailRequestParams.put("filter_params", Detailfilter_params);
        KwlReturnObject idcustresult = accountingHandlerDAOobj.getInvoiceCustomFields(invDetailRequestParams);
        if(idcustresult.getEntityList().size()>0) {
            List<Invoice> invoiceList = idcustresult.getEntityList();
            for(Invoice invoice : invoiceList) {
                List data = new ArrayList();
                if (invoice != null) {
                    AccJECustomData customData =  invoice.getJournalEntry().getAccBillInvCustomData();
//                    long value = 0;
                    String value="";
                    if(colnum!=0) {
                         value =simpleformat.format(convertCustomStringDateToDateObject(customData.getCol(colnum)));
                    }
                    String mainHtml = "";
                    String templateid = !StringUtil.isNullOrEmpty(nr.getTemplateid()) ? nr.getTemplateid() : "";
                    if (!StringUtil.isNullOrEmpty(templateid) && !Constants.NONE.equalsIgnoreCase(templateid)) {
                        JSONObject requestObj = new JSONObject();
                        requestObj.put(Constants.moduleid, Constants.Acc_Invoice_ModuleId);
                        requestObj.put(Constants.isdefaultHeaderMap, true);
                        requestObj.put(Constants.companyKey, companyId);
                        requestObj.put(Constants.TEMPLATEID_KEY, templateid);
                        requestObj.put(Constants.billid, invoice.getID());
                        requestObj.put(Constants.userid, (invoice != null && invoice.getCreatedby() != null) ? invoice.getCreatedby().getUserID() : "");

                        StringBuilder appendString = new StringBuilder();
                        String resturl = accDocumentDesignService.PrintTemplateRestUrl(requestObj);
                        String templateurl = "<a href='" + resturl + "' target='_blank'>" + invoice.getInvoiceNumber() + "</a>";

                        appendString.append(templateurl);
                        mainHtml = appendString.toString();
                    }
                    data.add(++sno);
                    data.add(!StringUtil.isNullOrEmpty(mainHtml)? mainHtml :invoice.getInvoiceNumber());
                    data.add(simpleformat.format(new Date(invoice.getCreatedon())));
                    data.add(value);
                    finalData.add(data);
                }
            }
            sendMail(finalData, headerItems, simpleformat, companyId, simpleDuedate, userMailID, mailContent, mailSubject,baseurl);
        }
      } catch(Exception ex) {
          ex.printStackTrace();
      }
  }
  
  private void sendCustomerQuotationCustomDateFieldMails(String fieldLabel, String colField, int colnum, String companyId, Date advdueDate1, Date advdueDate2, 
          String[] userMailID, DateFormat simpleformat, String simpleDuedate, String mailContent, String mailSubject,String baseurl) {
      try {
        List finalData = new ArrayList();
        int sno=0;
        List headerItems = new ArrayList();
        headerItems.add("S.No.");
        headerItems.add("Quotation Number");
        headerItems.add("Creation Date");
        headerItems.add(fieldLabel);
        
        HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
        ArrayList Detailfilter_names = new ArrayList(),Detailfilter_params = new ArrayList();
        Detailfilter_names.add("q.company.companyID");
        Detailfilter_params.add(companyId);
        if (!StringUtil.isNullOrEmpty(colField)) {
            Detailfilter_names.add(">=qcustom.".concat(colField));
            Detailfilter_params.add(advdueDate1);
            Detailfilter_names.add("<=qcustom.".concat(colField));
            Detailfilter_params.add(advdueDate2);
        }
        invDetailRequestParams.put("filter_names", Detailfilter_names);
        invDetailRequestParams.put("filter_params", Detailfilter_params);
        KwlReturnObject idcustresult = accountingHandlerDAOobj.getCustomerQuotationCustomFields(invDetailRequestParams);
        if(idcustresult.getEntityList().size()>0) {
            List<Quotation> invoiceList = idcustresult.getEntityList();
            for(Quotation quotation : invoiceList) {
                List data = new ArrayList();
                if (quotation != null) {
                    AccCustomData customData =  quotation.getQuotationCustomData();
//                    long value = 0;
                    String value="";
                    if(colnum!=0) {
                         value =simpleformat.format(convertCustomStringDateToDateObject(customData.getCol(colnum)));
                    }
                    data.add(++sno);
                    data.add(quotation.getquotationNumber());
                    data.add(simpleformat.format(new Date(quotation.getCreatedon())));
                    data.add(value);
                    finalData.add(data);
                }
            }
            sendMail(finalData, headerItems, simpleformat, companyId, simpleDuedate, userMailID, mailContent, mailSubject,baseurl);
        }

      } catch(Exception ex) {
          ex.printStackTrace();
      }
  }
    
  private void sendCustomerQuotationDetailLineCustomDateFieldMails(String fieldLabel, String colField, int colnum, String companyId, Date advdueDate1, Date advdueDate2, 
          String[] userMailID, DateFormat simpleformat, String simpleDuedate, String mailContent, String mailSubject,String baseurl) {
      try {
        List finalData = new ArrayList();
        int sno=0;
        List headerItems = new ArrayList();
        headerItems.add("S.No.");
        headerItems.add("Quotation Number");
        headerItems.add("Creation Date");
        headerItems.add("Product");
        headerItems.add(fieldLabel);
        
        HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
        ArrayList Detailfilter_names = new ArrayList(),Detailfilter_params = new ArrayList();
        Detailfilter_names.add("qd.quotation.company.companyID");
        Detailfilter_params.add(companyId);
        if (!StringUtil.isNullOrEmpty(colField)) {
            Detailfilter_names.add(">=qdcustom.".concat(colField));
            Detailfilter_params.add(advdueDate1);
            Detailfilter_names.add("<=qdcustom.".concat(colField));
            Detailfilter_params.add(advdueDate2);
        }
        invDetailRequestParams.put("filter_names", Detailfilter_names);
        invDetailRequestParams.put("filter_params", Detailfilter_params);
        KwlReturnObject idcustresult = accountingHandlerDAOobj.getCustomerQuotationDetailsLineCustomDateFields(invDetailRequestParams);
        if(idcustresult.getEntityList().size()>0) {
            List<QuotationDetail> invoiceList = idcustresult.getEntityList();
            for(QuotationDetail qd : invoiceList) {
                List data = new ArrayList();
                if (qd  != null) {
                    AccCustomData customData =  qd.getQuotationDetailCustomData();
//                    long value = 0;
                    String value="";
                    if(colnum!=0) {
                         value =simpleformat.format(convertCustomStringDateToDateObject(customData.getCol(colnum)));
                    }
                    data.add(++sno);
                    data.add(qd.getQuotation().getquotationNumber());
                    data.add(simpleformat.format(new Date(qd.getQuotation().getCreatedon())));
                    data.add(qd.getProduct().getName());
                    data.add(value);
                    finalData.add(data);
                }
            }
            sendMail(finalData, headerItems, simpleformat, companyId, simpleDuedate, userMailID, mailContent, mailSubject,baseurl);
        }

      } catch(Exception ex) {
          ex.printStackTrace();
      }
  }
        
    private void sendVendorQuotationCustomDateFieldMails(String fieldLabel, String colField, int colnum, String companyId, Date advdueDate1, Date advdueDate2, 
          String[] userMailID, DateFormat simpleformat, String simpleDuedate, String mailContent, String mailSubject,String baseurl) {
      try {
        List finalData = new ArrayList();
        int sno=0;
        List headerItems = new ArrayList();
        headerItems.add("S.No.");
        headerItems.add("Quotation Number");
        headerItems.add("Creation Date");
        headerItems.add(fieldLabel);
        
        HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
        ArrayList Detailfilter_names = new ArrayList(),Detailfilter_params = new ArrayList();
        Detailfilter_names.add("q.company.companyID");
        Detailfilter_params.add(companyId);
        if (!StringUtil.isNullOrEmpty(colField)) {
            Detailfilter_names.add(">=qcustom.".concat(colField));
            Detailfilter_params.add(advdueDate1);
            Detailfilter_names.add("<=qcustom.".concat(colField));
            Detailfilter_params.add(advdueDate2);
        }
        invDetailRequestParams.put("filter_names", Detailfilter_names);
        invDetailRequestParams.put("filter_params", Detailfilter_params);
        KwlReturnObject idcustresult = accountingHandlerDAOobj.getVendorQuotationCustomFields(invDetailRequestParams);
        if(idcustresult.getEntityList().size()>0) {
            List<VendorQuotation> invoiceList = idcustresult.getEntityList();
            for(VendorQuotation quotation : invoiceList) {
                List data = new ArrayList();
                if (quotation != null) {
                    AccCustomData customData =  quotation.getVendorQuotationCustomData();
//                    long value = 0;
                    String value="";
                    if(colnum!=0) {
                         value =simpleformat.format(convertCustomStringDateToDateObject(customData.getCol(colnum)));
                    }
                    data.add(++sno);
                    data.add(quotation.getQuotationNumber());
                    data.add(simpleformat.format(new Date(quotation.getCreatedon())));
                    data.add(value);
                    finalData.add(data);
                }
            }
            sendMail(finalData, headerItems, simpleformat, companyId, simpleDuedate, userMailID, mailContent, mailSubject,baseurl);
        }
      } catch(Exception ex) {
          ex.printStackTrace();
      }
  }
   
    private void sendSalesOrderCustomDateFieldMails(String fieldLabel, String colField, int colnum, String companyId, Date advdueDate1, Date advdueDate2, 
          String[] userMailID, DateFormat simpleformat, String simpleDuedate, String mailContent, String mailSubject,String baseurl) {
      try {
        List finalData = new ArrayList();
        int sno=0;
        List headerItems = new ArrayList();
        headerItems.add("S.No.");
        headerItems.add("Sales Order");
        headerItems.add("Creation Date");
        headerItems.add(fieldLabel);
        
        HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
        ArrayList Detailfilter_names = new ArrayList(),Detailfilter_params = new ArrayList();
        Detailfilter_names.add("so.company.companyID");
        Detailfilter_params.add(companyId);
        if (!StringUtil.isNullOrEmpty(colField)) {
            Detailfilter_names.add(">=socustom.".concat(colField));
            Detailfilter_params.add(advdueDate1);
            Detailfilter_names.add("<=socustom.".concat(colField));
            Detailfilter_params.add(advdueDate2);
        }
        invDetailRequestParams.put("filter_names", Detailfilter_names);
        invDetailRequestParams.put("filter_params", Detailfilter_params);
        KwlReturnObject idcustresult = accountingHandlerDAOobj.getSalesOrderDateCustomFields(invDetailRequestParams);
        if(idcustresult.getEntityList().size()>0) {
            List<SalesOrder> list = idcustresult.getEntityList();
            for(SalesOrder salesorder : list) {
                List data = new ArrayList();
                if (salesorder != null) {
                    AccCustomData customData =  salesorder.getSoCustomData();
//                    long value = 0;
                    String value="";
                    if(colnum!=0) {
                         value =simpleformat.format(convertCustomStringDateToDateObject(customData.getCol(colnum)));
                    }
                    data.add(++sno);
                    data.add(salesorder.getSalesOrderNumber());
                    data.add(simpleformat.format(new Date(salesorder.getCreatedon())));
                    data.add(value);
                    finalData.add(data);
                }
            }
            sendMail(finalData, headerItems, simpleformat, companyId, simpleDuedate, userMailID, mailContent, mailSubject,baseurl);
        }
      } catch(Exception ex) {
          ex.printStackTrace();
      }
  }
    private void sendVendorQuotationDetailLineCustomDateFieldMails(String fieldLabel, String colField, int colnum, String companyId, Date advdueDate1, Date advdueDate2, 
          String[] userMailID, DateFormat simpleformat, String simpleDuedate, String mailContent, String mailSubject,String baseurl) {
      try {
        List finalData = new ArrayList();
        int sno=0;
        List headerItems = new ArrayList();
        headerItems.add("S.No.");
        headerItems.add("Quotation Number");
        headerItems.add("Creation Date");
        headerItems.add("Product");
        headerItems.add(fieldLabel);
        
        HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
        ArrayList Detailfilter_names = new ArrayList(),Detailfilter_params = new ArrayList();
        Detailfilter_names.add("q.vendorquotation.company.companyID");
        Detailfilter_params.add(companyId);
        if (!StringUtil.isNullOrEmpty(colField)) {
            Detailfilter_names.add(">=qcustom.".concat(colField));
            Detailfilter_params.add(advdueDate1);
            Detailfilter_names.add("<=qcustom.".concat(colField));
            Detailfilter_params.add(advdueDate2);
        }
        invDetailRequestParams.put("filter_names", Detailfilter_names);
        invDetailRequestParams.put("filter_params", Detailfilter_params);
        KwlReturnObject idcustresult = accountingHandlerDAOobj.getVendorQuotationDetailsLineCustomDateFields(invDetailRequestParams);
        if(idcustresult.getEntityList().size()>0) {
            List<VendorQuotationDetail> invoiceList = idcustresult.getEntityList();
            for(VendorQuotationDetail qd : invoiceList) {
                List data = new ArrayList();
                if (qd  != null) {
                    AccCustomData customData =  qd.getVendorQuotationDetailCustomData();
//                    long value = 0;
                    String value="";
                    if(colnum!=0) {
                         value =simpleformat.format(convertCustomStringDateToDateObject(customData.getCol(colnum)));
                    }
                    data.add(++sno);
                    data.add(qd.getVendorquotation().getQuotationNumber());
                    data.add(simpleformat.format(new Date(qd.getVendorquotation().getCreatedon())));
                    data.add(qd.getProduct().getName());
                    data.add(value);
                    finalData.add(data);
                }
            }
            sendMail(finalData, headerItems, simpleformat, companyId, simpleDuedate, userMailID, mailContent, mailSubject,baseurl);
        }

      } catch(Exception ex) {
          ex.printStackTrace();
      }
  }
    
    private void sendSalesOrderDetailLineCustomDateFieldMails(String fieldLabel, String colField, int colnum, String companyId, Date advdueDate1, Date advdueDate2, 
          String[] userMailID, DateFormat simpleformat, String simpleDuedate, String mailContent, String mailSubject,String baseurl) {
      try {
        List finalData = new ArrayList();
        int sno=0;
        List headerItems = new ArrayList();
        headerItems.add("S.No.");
        headerItems.add("Sales Order Number");
        headerItems.add("Creation Date");
        headerItems.add("Product");
        headerItems.add(fieldLabel);
        
        HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
        ArrayList Detailfilter_names = new ArrayList(),Detailfilter_params = new ArrayList();
        Detailfilter_names.add("sod.company.companyID");
        Detailfilter_params.add(companyId);
        if (!StringUtil.isNullOrEmpty(colField)) {
            Detailfilter_names.add(">=sodcustom.".concat(colField));
            Detailfilter_params.add(advdueDate1);
            Detailfilter_names.add("<=sodcustom.".concat(colField));
            Detailfilter_params.add(advdueDate2);
        }
        invDetailRequestParams.put("filter_names", Detailfilter_names);
        invDetailRequestParams.put("filter_params", Detailfilter_params);
        KwlReturnObject idcustresult = accountingHandlerDAOobj.getSalesOrderDetailsLineCustomDateFields(invDetailRequestParams);
        if(idcustresult.getEntityList().size()>0) {
            List<SalesOrderDetail> list = idcustresult.getEntityList();
            for(SalesOrderDetail sod : list) {
                List data = new ArrayList();
                if (sod  != null) {
                    AccCustomData customData =  sod.getSoDetailCustomData();
//                    long value = 0;
                    String value="";
                    if(colnum!=0) {
                         value =simpleformat.format(convertCustomStringDateToDateObject(customData.getCol(colnum)));
                    }
                    data.add(++sno);
                    data.add(sod.getSalesOrder().getSalesOrderNumber());
                    data.add(simpleformat.format(new Date(sod.getSalesOrder().getCreatedon())));
                    data.add(sod.getProduct().getName());
                    data.add(value);
                    finalData.add(data);
                }
            }
            sendMail(finalData, headerItems, simpleformat, companyId, simpleDuedate, userMailID, mailContent, mailSubject,baseurl);
        }

      } catch(Exception ex) {
          ex.printStackTrace();
      }
  }
  private void sendCustomerInvoiceLineCustomDateFieldMails(String fieldLabel, String colField, int colnum, String companyId, Date advdueDate1, Date advdueDate2, 
          String[] userMailID, DateFormat simpleformat, String simpleDuedate, String mailContent, String mailSubject, NotificationRules nr,String baseurl) {
      try {
        List finalData = new ArrayList();
        int sno=0;
        List headerItems = new ArrayList();
        headerItems.add("S.No.");
        headerItems.add("Customer Invoice");
        headerItems.add("Invoice Date");
        headerItems.add("Product");
        headerItems.add(fieldLabel);
        
        HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
        ArrayList Detailfilter_names = new ArrayList(),Detailfilter_params = new ArrayList();
        Detailfilter_names.add("inv.company");
        Detailfilter_params.add(companyId);
        if (!StringUtil.isNullOrEmpty(colField)) {
            Detailfilter_names.add(">=accjedcustom.".concat(colField));
            Detailfilter_params.add(advdueDate1);
            Detailfilter_names.add("<=accjedcustom.".concat(colField));
            Detailfilter_params.add(advdueDate2);
            invDetailRequestParams.put("selectfields", "accjedcustom.".concat(colField));
        }
        invDetailRequestParams.put("filter_names", Detailfilter_names);
        invDetailRequestParams.put("filter_values", Detailfilter_params);
        
        KwlReturnObject idcustresult = accountingHandlerDAOobj.getCustomerInvoiceLineCustomDateFieldMails(invDetailRequestParams);
        if(idcustresult.getEntityList().size()>0) {
            List<Object[]> list = idcustresult.getEntityList();
            for(Object[] obj : list) {
                List data = new ArrayList();
                if (obj != null) {
                    String mainHtml = "";
                    String templateid = !StringUtil.isNullOrEmpty(nr.getTemplateid()) ? nr.getTemplateid() : "";
                    if (!StringUtil.isNullOrEmpty(templateid) && obj[0] != null && !Constants.NONE.equalsIgnoreCase(templateid)) {
                        KwlReturnObject invoiceObj = accountingHandlerDAOobj.getObject(Invoice.class.getName(), obj[0].toString());
                        if (invoiceObj != null && invoiceObj.getEntityList().size() > 0) {
                            Invoice invoice = (Invoice) invoiceObj.getEntityList().get(0);
                            if (invoice != null) {
                                JSONObject requestObj = new JSONObject();
                                requestObj.put(Constants.moduleid, Constants.Acc_Invoice_ModuleId);
                                requestObj.put(Constants.isdefaultHeaderMap, true);
                                requestObj.put(Constants.companyKey, companyId);
                                requestObj.put(Constants.TEMPLATEID_KEY, templateid);
                                requestObj.put(Constants.billid, invoice.getID());
                                requestObj.put(Constants.userid, (invoice != null && invoice.getCreatedby() != null) ? invoice.getCreatedby().getUserID() : "");

                                StringBuilder appendString = new StringBuilder();
                                String resturl = accDocumentDesignService.PrintTemplateRestUrl(requestObj);
                                String templateurl = "<a href='" + resturl + "' target='_blank'>" + invoice.getInvoiceNumber() + "</a>";

                                appendString.append(templateurl);
                                mainHtml = appendString.toString();
                            }
                        }
                    }
                    
                    data.add(++sno);
                    data.add(!StringUtil.isNullOrEmpty(mainHtml)? mainHtml :obj[1]);
                    data.add(simpleformat.format((Date)(obj[2])));
                    data.add(obj[3]);
                    data.add(simpleformat.format((Date)obj[4]));                    
                    finalData.add(data);
                }
            }
            sendMail(finalData, headerItems, simpleformat, companyId, simpleDuedate, userMailID, mailContent, mailSubject,baseurl);
        }
      } catch(Exception ex) {
          ex.printStackTrace();
      }
  }
  
      
  private void sendVendorInvoiceLineCustomDateFieldMails(String fieldLabel, String colField, int colnum, String companyId, Date advdueDate1, Date advdueDate2, 
          String[] userMailID, DateFormat simpleformat, String simpleDuedate, String mailContent, String mailSubject,String baseurl) {
      try {
        List finalData = new ArrayList();
        int sno=0;
        List headerItems = new ArrayList();
        headerItems.add("S.No.");
        headerItems.add("Vendor Invoice");
        headerItems.add("Invoice Date");
        headerItems.add("Product");
        headerItems.add(fieldLabel);
        
        HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
        ArrayList Detailfilter_names = new ArrayList(),Detailfilter_params = new ArrayList();
        Detailfilter_names.add("gr.company");
        Detailfilter_params.add(companyId);
        if (!StringUtil.isNullOrEmpty(colField)) {
            Detailfilter_names.add(">=accjedcustom.".concat(colField));
            Detailfilter_params.add(advdueDate1);
            Detailfilter_names.add("<=accjedcustom.".concat(colField));
            Detailfilter_params.add(advdueDate2);
            invDetailRequestParams.put("selectfields", "accjedcustom.".concat(colField));
        }
        invDetailRequestParams.put("filter_names", Detailfilter_names);
        invDetailRequestParams.put("filter_values", Detailfilter_params);
        
        KwlReturnObject idcustresult = accountingHandlerDAOobj.getVendorInvoiceLineCustomDateFieldMails(invDetailRequestParams);
        if(idcustresult.getEntityList().size()>0) {
            List<Object[]> list = idcustresult.getEntityList();
            for(Object[] obj : list) {
                List data = new ArrayList();
                if (obj != null) {
                    data.add(++sno);
                    data.add(obj[1]);
                    data.add(simpleformat.format((Date)(obj[2])));
                    data.add(obj[3]);
                    data.add(simpleformat.format((Date)(obj[4])));
                    finalData.add(data);
                }
            }
            sendMail(finalData, headerItems, simpleformat, companyId, simpleDuedate, userMailID, mailContent, mailSubject,baseurl);
        }
      } catch(Exception ex) {
          ex.printStackTrace();
      }
  }
  
  private void sendMail(List finalData, List headerItems, DateFormat simpleformat, String companyid, String simpleDuedate, String[] userMailID, 
          String mailContent, String mailsubject,String baseurl) {
      try {
        String tablehtml = getTableHTML(headerItems, finalData);
        Date todayDate = new Date();
        String today = simpleformat.format(todayDate);
        String htmlText = "";
        String plainMsg = "";
        String from = Constants.ADMIN_EMAILID;
        htmlText = mailContent.replace(Constants.DateLabel_MailContent_Placeholder, simpleDuedate);
        htmlText = htmlText.replace(Constants.TodayDateLabel_MailContent_Placeholder, today);
        htmlText = htmlText.concat(tablehtml);
        htmlText +=baseurl;
        Company company = (Company) kwlCommonTablesDAOObj.getClassObject(Company.class.getName(), companyid);
        Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
        SendMailHandler.postMail(userMailID, mailsubject, htmlText, plainMsg, from, smtpConfigMap);
      } catch(Exception ex) {
          ex.printStackTrace();
      }
  }
  private String getTableHTML(List headerItems, List finalData) {
        String returnString = "";
        try {
            ArrayList<CustomDesignLineItemProp> headerlist = new ArrayList();
            for (Object header : headerItems) {
                CustomDesignLineItemProp headerprop = new CustomDesignLineItemProp();
                String a = header.toString();
                headerprop.setAlign("left");
                headerprop.setData(a);
                headerprop.setWidth("50px");
                headerlist.add(headerprop);
            }
            List finalProductList = new ArrayList();
            for (Object headerdata : finalData) {
                ArrayList<CustomDesignLineItemProp> prodlist = new ArrayList();
                List datalist = (List) headerdata;
                for (Object hdata : datalist) {
                    CustomDesignLineItemProp prop = new CustomDesignLineItemProp();
                    prop.setAlign("left");
                    prop.setData(hdata.toString());
                    prodlist.add(prop);
                }
                finalProductList.add(prodlist);
            }
            String top = "10px", left = "10px", tablewidth = CustomDesignHandler.pageWidth;
            StringWriter writer = new StringWriter();
            VelocityEngine ve = new VelocityEngine();
            ve.init();
            VelocityContext context = new VelocityContext();
            context.put("tableHeader", headerlist);
            context.put("prodList", finalProductList);
            context.put("top", top);
            context.put("left", left);
            context.put("width", tablewidth);
            velocityEngine.mergeTemplate("duemailitems.vm", "UTF-8", context, writer);
            returnString = "<br/><br/>"+writer.toString();
        } catch (Exception ex) {
            Logger.getLogger(CommonFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnString;
  }
    public void sendCustomerInvoiceMail(String dbDuedate, String simpleDuedate, String companyId, String companyName, String baseCurrencyId, String userFname,
            String userLname, String[] userMailid, String mailContent, String mailSubject, NotificationRules nr,String baseurl) throws FileNotFoundException, IOException, DocumentException, ServiceException, JSONException {
        KwlReturnObject kwlInvoice = null;
        try {
            String invoiceNumber, customerName, currencyName;
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            double amountDue = 0, invoiceAmount;
            Date todayDate = new Date();
            DateFormat formatter;
            formatter = new SimpleDateFormat("dd-MM-yyyy");
            String today = formatter.format(todayDate);

            List headerItems = null;
            List finalData = new ArrayList();
            ArrayList<CustomDesignLineItemProp> headerlist = new ArrayList();
            requestParams.put("gcurrencyid", baseCurrencyId);
            requestParams.put("companyid", companyId);
            headerItems = getHeaderData(true, Constants.Acc_Invoice_ModuleId);
            if (userMailid.length > 0) {
                boolean isInvoice = false;
                int sno = 1;
                String htmlText = "";
                String subject = mailSubject;
                String plainMsg = "";
                //String from = Constants.ADMIN_EMAILID;
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
                Company company = (Company) returnObject.getEntityList().get(0);
                String from = (!company.isEmailFromCompanyCreator()) ? Constants.ADMIN_EMAILID : authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                htmlText = mailContent.replace(Constants.DateLabel_MailContent_Placeholder, simpleDuedate).replace(Constants.Due_DateLabel_MailContent_Placeholder, simpleDuedate);
                htmlText = htmlText.replace(Constants.TodayDateLabel_MailContent_Placeholder, today);
                String dueDateID = new JSONObject(Constants.staticGlobalDateFields.get(Constants.dueDate)).optString("fieldid", "");

                HashMap<String, Object> requestMap = new HashMap<String, Object>();
                int endType = -1;
                int endInterval = -1;
                if (nr.getRecurringDetail() != null) {
                    NotifictionRulesRecurringDetail details = nr.getRecurringDetail();
                    endType = details.getEndType();
                    endInterval = details.getEndInterval();
                    requestMap.put("isRecurring", true);
                    requestMap.put("repeatTime", details.getRepeatTime());
                    requestMap.put("repeatTimeType", details.getRepeatTimeType());
                }
                requestMap.put("companyid", companyId);
                requestMap.put("dbDueDate", dbDuedate);
                requestMap.put("isDueDate", nr.getFieldid().equals(dueDateID));
               

                kwlInvoice = accountingHandlerDAOobj.getDuedateCustomerInvoiceList(requestMap);
                List list = kwlInvoice.getEntityList();
                Iterator iterator = list.iterator();

                while (iterator.hasNext()) {
                    List data = new ArrayList();
                    String id = (String) iterator.next();
                    KwlReturnObject invResult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), id);
                    Invoice invoice = (Invoice) invResult.getEntityList().get(0);

                    if (invoice != null) {
                        if (endType != -1 && endType == Constants.RECURRING_ENDAFTERINTERVAL) {
                            int mailCount = accountingHandlerDAOobj.getDocumentMailCount(invoice.getID(), companyId, nr.getID());
                            if (mailCount >= endInterval) { //if number of mail count reached its limit then no need to send mail notification
                                continue;
                            }
                        }
                        isInvoice = true;
                        amountDue=invoice.isIsOpeningBalenceInvoice()?invoice.getOpeningBalanceAmountDue():invoice.getInvoiceamountdue();
                        invoiceNumber = invoice.getInvoiceNumber();
                        Date duedate = invoice.getDueDate();
//                        Date creationDate = (Date) invoice.getJournalEntry().getEntryDate();
                        Date creationDate = (Date) invoice.getCreationDate();
                        currencyName = invoice.getCurrency().getName();
                        customerName = invoice.getCustomer().getName();
                        invoiceAmount = invoice.getCustomerEntry().getAmount();
                        formatter.setTimeZone(TimeZone.getTimeZone("GMT" + invoice.getCreatedby().getTimeZone().getDifference())); //to resolve timezone issue timezone is setted here 

                        String mainHtml = "";
                        String templateid = !StringUtil.isNullOrEmpty(nr.getTemplateid()) ? nr.getTemplateid() : "";
                        if (!StringUtil.isNullOrEmpty(templateid) && !Constants.NONE.equalsIgnoreCase(templateid)) {
                            JSONObject requestObj = new JSONObject();
                            requestObj.put(Constants.moduleid, Constants.Acc_Invoice_ModuleId);
                            requestObj.put(Constants.isdefaultHeaderMap, true);
                            requestObj.put(Constants.companyKey, companyId);
                            requestObj.put(Constants.TEMPLATEID_KEY, templateid);
                            requestObj.put(Constants.billid, id);
                            requestObj.put(Constants.userid, (invoice != null && invoice.getCreatedby() != null)?invoice.getCreatedby().getUserID():"");

                            StringBuilder appendString = new StringBuilder();
                            String resturl = accDocumentDesignService.PrintTemplateRestUrl(requestObj);
                            String templateurl = "<a href='" + resturl + "' target='_blank'>" + invoiceNumber + "</a>";

                            appendString.append(templateurl);
                            mainHtml = appendString.toString();
                        }
                        
                        data.add(sno);
                        data.add(!StringUtil.isNullOrEmpty(mainHtml)?mainHtml : invoiceNumber);
                        data.add(customerName);
                        data.add(formatter.format(creationDate));
                        data.add(formatter.format(duedate));
                        data.add(currencyName);
                        data.add(authHandler.formattedAmount(invoiceAmount, companyId));
                        data.add(authHandler.formattedAmount(amountDue, companyId));
                        finalData.add(data);
                        sno++;
                        if (nr.getRecurringDetail() != null) {//inserting data in table for mail history and mail count for recurring mails
                            accountingHandlerDAOobj.insertDocumentMailMapping(invoice.getID(), Constants.Acc_Invoice_ModuleId, nr.getID(), companyId);
                        }
                    }
                }
                if (isInvoice) {
                    for (Object header : headerItems) {
                        CustomDesignLineItemProp headerprop = new CustomDesignLineItemProp();
                        String a = header.toString();
                        headerprop.setAlign("left");
                        headerprop.setData(a);
                        headerprop.setWidth("50px");
                        headerlist.add(headerprop);
                    }
                    List finalProductList = new ArrayList();
                    for (Object headerdata : finalData) {
                        ArrayList<CustomDesignLineItemProp> prodlist = new ArrayList();
                        List datalist = (List) headerdata;
                        for (Object hdata : datalist) {
                            CustomDesignLineItemProp prop = new CustomDesignLineItemProp();
                            prop.setAlign("left");
                            prop.setData(hdata.toString());
                            prodlist.add(prop);
                        }
                        finalProductList.add(prodlist);
                    }
                    String top = "10px", left = "10px", tablewidth = CustomDesignHandler.pageWidth;
                    StringWriter writer = new StringWriter();
                    VelocityEngine ve = new VelocityEngine();
                    ve.init();
                    VelocityContext context = new VelocityContext();
                    context.put("tableHeader", headerlist);
                    context.put("prodList", finalProductList);
                    context.put("top", top);
                    context.put("left", left);
                    context.put("width", tablewidth);
                    velocityEngine.mergeTemplate("duemailitems.vm", "UTF-8", context, writer);
                    String tablehtml = writer.toString();
                    htmlText = htmlText.concat(tablehtml);
                    htmlText+=baseurl;
                    Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                    SendMailHandler.postMail(userMailid, subject, htmlText, plainMsg, from, smtpConfigMap);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public void sendAssetMaintenanceMail(String dbDuedate, String simpleDuedate, String companyId, String companyName, String baseCurrencyId, String userFname,
            String userLname, List<String> userEmailList, String mailContent, String mailSubject, boolean isMailToAssignedPerson, boolean isForStartDate,String baseurl) throws FileNotFoundException, IOException, DocumentException, ServiceException, JSONException {
        KwlReturnObject kwlSchedules = null;
        try {
            String scheduleName, assetName;
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            AssetMaintenanceScheduler maintenanceScheduler = null;
            Date todayDate = new Date();
            DateFormat formatter;
            formatter = new SimpleDateFormat("dd-MM-yyyy");
            String today = formatter.format(todayDate);

            List headerItems = null;
            List finalData = new ArrayList();
            ArrayList<CustomDesignLineItemProp> headerlist = new ArrayList();
            requestParams.put("companyId", companyId);
            if (isForStartDate) {
                requestParams.put("eventStartDate", dbDuedate);
            }else{
                requestParams.put("eventEndDate", dbDuedate);
            }
            headerItems = getHeaderData(true, Constants.Asset_Maintenance_ModuleId);
            
            Set<String> FinalMailList = new HashSet<String>();        
                
            kwlSchedules = accInvoiceDAOobj.getAssetMaintenanceScheduleForCrown(requestParams);
            List<AssetMaintenanceScheduler> schedulers = kwlSchedules.getEntityList();
            
            Iterator it = schedulers.iterator();
            
            while (it.hasNext()) {
                maintenanceScheduler = (AssetMaintenanceScheduler) it.next();
                if (maintenanceScheduler.getAssignedTo() != null) {
                    FinalMailList.add(maintenanceScheduler.getAssignedTo().getEmailID());
                    System.out.println(maintenanceScheduler.getAssignedTo().getEmailID());
                }
            }

            
            FinalMailList.addAll(userEmailList);
            String[] userMailid = FinalMailList.toArray(new String[FinalMailList.size()]);

            if (userMailid.length > 0) {
                boolean isSchedule = false;
                int sno = 1;
                String htmlText = "";
                String subject = mailSubject;
                String plainMsg = "";
                //String from = Constants.ADMIN_EMAILID;
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
                Company company = (Company) returnObject.getEntityList().get(0);
                String from = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                htmlText = mailContent.replace(Constants.DateLabel_MailContent_Placeholder, simpleDuedate);
                htmlText = htmlText.replace(Constants.TodayDateLabel_MailContent_Placeholder, today);

//                kwlSchedules = accInvoiceDAOobj.getDuedateCustomerInvoiceList(companyId, dbDuedate);
                
                List list = kwlSchedules.getEntityList();
                Iterator iterator = list.iterator();

                while (iterator.hasNext()) {
                    List data = new ArrayList();
                    maintenanceScheduler = (AssetMaintenanceScheduler) iterator.next();
                    if (maintenanceScheduler != null) {
                        isSchedule = true;
                        scheduleName = maintenanceScheduler.getAssetMaintenanceSchedulerObject().getScheduleName();
                        assetName = (maintenanceScheduler.getAssetMaintenanceSchedulerObject().getAssetDetails()!=null)?(maintenanceScheduler.getAssetMaintenanceSchedulerObject().getAssetDetails().getAssetId()):"";
                        Date startDate = maintenanceScheduler.getStartDate();
                        Date endDate = maintenanceScheduler.getEndDate();

                        data.add(sno);
                        data.add(scheduleName);
                        data.add(assetName);
                        data.add(formatter.format(startDate));
                        data.add(formatter.format(endDate));
                        finalData.add(data);
                        sno++;
                    }
                }

                if (isSchedule) {
                    for (Object header : headerItems) {
                        CustomDesignLineItemProp headerprop = new CustomDesignLineItemProp();
                        String a = header.toString();
                        headerprop.setAlign("left");
                        headerprop.setData(a);
                        headerprop.setWidth("50px");
                        headerlist.add(headerprop);
                    }
                    List finalProductList = new ArrayList();
                    for (Object headerdata : finalData) {
                        ArrayList<CustomDesignLineItemProp> prodlist = new ArrayList();
                        List datalist = (List) headerdata;
                        for (Object hdata : datalist) {
                            CustomDesignLineItemProp prop = new CustomDesignLineItemProp();
                            prop.setAlign("left");
                            prop.setData(hdata.toString());
                            prodlist.add(prop);
                        }
                        finalProductList.add(prodlist);
                    }
                    String top = "10px", left = "10px", tablewidth = CustomDesignHandler.pageWidth;
                    StringWriter writer = new StringWriter();
                    VelocityEngine ve = new VelocityEngine();
                    ve.init();
                    VelocityContext context = new VelocityContext();
                    context.put("tableHeader", headerlist);
                    context.put("prodList", finalProductList);
                    context.put("top", top);
                    context.put("left", left);
                    context.put("width", tablewidth);
                    velocityEngine.mergeTemplate("duemailitems.vm", "UTF-8", context, writer);
                    String tablehtml = writer.toString();
                    htmlText = htmlText.concat(tablehtml);
                    htmlText+=baseurl;
                    Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                    SendMailHandler.postMail(userMailid, subject, htmlText, plainMsg, from, smtpConfigMap);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
   
   public void sendVendorInvoiceMail(String dbDuedate,String simpleDuedate,String companyId,String companyName,String baseCurrencyId,String userFname,
           String userLname,String[] userMailid, String mailContent, String mailSubject,NotificationRules nr,String baseurl)throws FileNotFoundException, IOException, DocumentException, ServiceException, JSONException {
      KwlReturnObject kwlInvoice = null;
      try { 
        String invoiceNumber,vendorName,currencyName;
        double amountDue=0.0,invoiceAmount;
        Date creationDate,dueDate;
        HashMap<String, Object> requestParams=new HashMap<String, Object>();
        Date todayDate=new Date();
        DateFormat formatter ;
        formatter = new SimpleDateFormat("dd-MM-yyyy");
        String today =formatter.format(todayDate);       
        List headerItems =null;
        List finalData=new ArrayList();
        ArrayList<CustomDesignLineItemProp> headerlist = new ArrayList();      
        requestParams.put("gcurrencyid", baseCurrencyId);
        requestParams.put("companyid", companyId);
        headerItems=getHeaderData(false,Constants.Acc_Vendor_Invoice_ModuleId);
        String dueDateID = new JSONObject(Constants.staticGlobalDateFields.get(Constants.dueDate)).optString("fieldid","");

        if(userMailid.length>0){
          boolean isInvoice=false;
          int sno=1;
          String htmlText="";
          String subject=mailSubject;
          String plainMsg="";
          //String from=Constants.ADMIN_EMAILID;
          KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
          Company company = (Company) returnObject.getEntityList().get(0);
          String from = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
          htmlText = mailContent.replace(Constants.DateLabel_MailContent_Placeholder, simpleDuedate).replace(Constants.Due_DateLabel_MailContent_Placeholder, simpleDuedate);
          htmlText = htmlText.replace(Constants.TodayDateLabel_MailContent_Placeholder, today);
           
            HashMap<String, Object> requestMap = new HashMap<String, Object>();
            int endType = -1;
            int endInterval = -1;
            if (nr.getRecurringDetail() != null) {
                NotifictionRulesRecurringDetail details = nr.getRecurringDetail();
                endType = details.getEndType();
                endInterval = details.getEndInterval();
                requestMap.put("isRecurring", true);
                requestMap.put("repeatTime", details.getRepeatTime());
                requestMap.put("repeatTimeType", details.getRepeatTimeType());
            }
            requestMap.put("companyid", companyId);
            requestMap.put("dbDueDate", dbDuedate);
            requestMap.put("isDueDate", nr.getFieldid().equals(dueDateID));
            System.out.println("Before getting data");
            kwlInvoice = accountingHandlerDAOobj.getDuedateVendorInvoiceList(requestMap);
            List list = kwlInvoice.getEntityList();
            Iterator iterator = list.iterator();
            
            while (iterator.hasNext()) {
                System.out.println("After getting data Inside  loop");
                List data = new ArrayList();
                String id = (String) iterator.next();
                KwlReturnObject grResult = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), id);
                GoodsReceipt gr = (GoodsReceipt) grResult.getEntityList().get(0);
                if (gr != null) {
                    if (endType != -1 && endType == Constants.RECURRING_ENDAFTERINTERVAL) {
                        int mailCount = accountingHandlerDAOobj.getDocumentMailCount(gr.getID(), companyId, nr.getID());
                        if (mailCount >= endInterval) { //if number of mail count reached its limit then no need to send mail notification
                            continue;
                        }
                    }
                    amountDue=gr.isIsOpeningBalenceInvoice()?gr.getOpeningBalanceAmountDue():gr.getInvoiceamountdue();
                    isInvoice = true;
                    invoiceNumber = gr.getGoodsReceiptNumber();
                    dueDate = gr.getDueDate();
//                    creationDate = (Date) gr.getJournalEntry().getEntryDate();
                    creationDate = (Date) gr.getCreationDate();
                    currencyName = gr.getCurrency().getName();
                    vendorName = gr.getVendor().getName();
                    invoiceAmount = gr.getVendorEntry().getAmount();
                    formatter.setTimeZone(TimeZone.getTimeZone("GMT" + gr.getCreatedby().getTimeZone().getDifference()));//to resolve timezone issue timezone is setted here 
                    
                    String mainHtml = "";
                    String templateid = !StringUtil.isNullOrEmpty(nr.getTemplateid()) ? nr.getTemplateid() : "";
                    if (!StringUtil.isNullOrEmpty(templateid) && !Constants.NONE.equalsIgnoreCase(templateid)) {
                        JSONObject requestObj = new JSONObject();
                        requestObj.put(Constants.moduleid, Constants.Acc_Vendor_Invoice_ModuleId);
                        requestObj.put(Constants.isdefaultHeaderMap, true);
                        requestObj.put(Constants.companyKey, companyId);
                        requestObj.put(Constants.TEMPLATEID_KEY, templateid);
                        requestObj.put(Constants.billid, id);
                        requestObj.put(Constants.userid, (gr != null && gr.getCreatedby() != null) ? gr.getCreatedby().getUserID() : "");

                        StringBuilder appendString = new StringBuilder();
                        String resturl = accDocumentDesignService.PrintTemplateRestUrl(requestObj);
                        String templateurl = "<a href='" + resturl + "' target='_blank'>" + invoiceNumber + "</a>";

                        appendString.append(templateurl);
                        mainHtml = appendString.toString();
                    }

                    data.add(sno);
                    data.add(!StringUtil.isNullOrEmpty(mainHtml) ? mainHtml : invoiceNumber);
                    data.add(vendorName);
                    if(creationDate != null){
                    data.add(formatter.format(creationDate));
                    }
                    data.add(formatter.format(dueDate));
                    data.add(currencyName);
                    data.add(authHandler.formattedAmount(invoiceAmount, companyId));
                    data.add(authHandler.formattedAmount(amountDue, companyId));
                    finalData.add(data);
                    sno++;
                    if (nr.getRecurringDetail() != null) {//inserting data in table for mail history and mail count for recurring mails
                        accountingHandlerDAOobj.insertDocumentMailMapping(gr.getID(), Constants.Acc_Vendor_Invoice_ModuleId, nr.getID(), companyId);
                    }
                }
            }
              if (isInvoice) {
                  for (Object header : headerItems) {
                      CustomDesignLineItemProp headerprop = new CustomDesignLineItemProp();
                      String a = header.toString();
                      headerprop.setAlign("left");
                      headerprop.setData(a);
                      headerprop.setWidth("50px");
                      headerlist.add(headerprop);
                  }
                  List finalProductList = new ArrayList();
                  for (Object headerdata : finalData) {
                      ArrayList<CustomDesignLineItemProp> prodlist = new ArrayList();
                      List datalist = (List) headerdata;
                      for (Object hdata : datalist) {
                          CustomDesignLineItemProp prop = new CustomDesignLineItemProp();
                          prop.setAlign("left");
                          prop.setData(hdata.toString());
                          prodlist.add(prop);
                      }
                      finalProductList.add(prodlist);
                  }

                  String top = "10px", left = "10px", tablewidth = CustomDesignHandler.pageWidth;
                  StringWriter writer = new StringWriter();
                  VelocityEngine ve = new VelocityEngine();
                  ve.init();
                  VelocityContext context = new VelocityContext();
                  context.put("tableHeader", headerlist);
                  context.put("prodList", finalProductList);
                  context.put("top", top);
                  context.put("left", left);
                  context.put("width", tablewidth);
                  velocityEngine.mergeTemplate("duemailitems.vm", "UTF-8", context, writer);
                  String tablehtml = writer.toString();
                  htmlText = htmlText.concat(tablehtml);
                  htmlText+=baseurl;
                  Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                  SendMailHandler.postMail(userMailid, subject, htmlText, plainMsg, from, smtpConfigMap);
              }
          }
        }catch(Exception ex){
        ex.printStackTrace();
    }
    }

   private void sendJournalEntryMail(Long dbDuedate1, Long dbDuedate2,String simpleDuedate, String companyId, String companyName, String baseCurrencyId, 
           String userFname, String userLname, String[] userMailid, String mailContent, String mailSubject,String baseurl) {
      KwlReturnObject kwlresult = null;
      KwlReturnObject kwllist = null;
       try{
           Date now = new Date();
           DateFormat formatter;
           formatter = new SimpleDateFormat("dd-MM-yyyy");
           String today = formatter.format(now);
           kwllist=accountingHandlerDAOobj.getDuedateCustomefield(companyId);
           List customelist=kwllist.getEntityList();
           if(!customelist.isEmpty()){
           Iterator it = customelist.iterator(); 
           String columnname="";
           while(it.hasNext()){
              Object[] val = (Object[])it.next();
              columnname=val[1].toString();
           }
       
           kwlresult= accountingHandlerDAOobj.getDueJournalEntryList(companyId,dbDuedate1,dbDuedate2,"Col"+columnname);
           Iterator iterator=kwlresult.getEntityList().iterator();
           String htmlText="";
           String subject=mailSubject;
           String plainMsg="";
           //String from=Constants.ADMIN_EMAILID;
           KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
           Company company = (Company) returnObject.getEntityList().get(0);
           String from = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
           htmlText = mailContent.replace(Constants.DateLabel_MailContent_Placeholder, simpleDuedate);
           htmlText = htmlText.replace(Constants.TodayDateLabel_MailContent_Placeholder, today);
           List completelist=new ArrayList();
           int sno=0;
           boolean ismail=false;
           while(iterator.hasNext()){
               ismail=true;
               sno++;
               Object[] val = (Object[])iterator.next();
               List jelist=new ArrayList();
               Date cd=(Date)(val[0]);
               String creationDate=formatter.format(cd);
               String jenumber=(val[1]==null)?"":val[1].toString();
               String currencyName=(val[2]==null)?"":val[2].toString();
               jelist.add(sno);
               jelist.add(jenumber);
               jelist.add(creationDate);
               jelist.add(simpleDuedate);
               jelist.add(currencyName);
               completelist.add(jelist);      
           }
           
           String[] header={"S.No.","Entry Number","Creation Date","Due Date","Currency"};
           ArrayList<CustomDesignLineItemProp> headerlist = new ArrayList(); 
           for(int i=0;i<header.length;i++){
                CustomDesignLineItemProp headerprop = new CustomDesignLineItemProp();
                headerprop.setAlign("left");
                headerprop.setData(header[i]);
                headerprop.setWidth("50px");
                headerlist.add(headerprop);  
           }
           
           List finalitemlist=new ArrayList();
            for(Object headerdata: completelist){ 
                ArrayList<CustomDesignLineItemProp> itemlist = new ArrayList();
                List datalist=(List) headerdata;
                  for(Object hdata: datalist){
                      CustomDesignLineItemProp prop = new CustomDesignLineItemProp();
                      prop.setAlign("left");
                      prop.setData(hdata.toString());
                      itemlist.add(prop);
                  }
                finalitemlist.add(itemlist);
            }

            String top="10px",left="10px",tablewidth=CustomDesignHandler.pageWidth;
            StringWriter writer = new StringWriter();
            VelocityEngine ve = new VelocityEngine();  
            ve.init();  
            VelocityContext context = new VelocityContext();  
            context.put("tableHeader", headerlist); 
            context.put("prodList", finalitemlist); 
            context.put("top", top); 
            context.put("left", left); 
            context.put("width", tablewidth); 
            velocityEngine.mergeTemplate("duemailitems.vm","UTF-8" , context, writer);
            String tablehtml = writer.toString();
            htmlText = htmlText.concat(tablehtml);
            htmlText+=baseurl;
            if(ismail){
                Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                SendMailHandler.postMail(userMailid, subject, htmlText, plainMsg, from, smtpConfigMap); 
            }
           }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
   public void sendSalesOrderMail(String dbDuedate, String simpleDuedate, String companyId, String companyName, String baseCurrencyId, String userFname,
                String userLname, String[]  userMailid, String mailContent, String mailSubject, NotificationRules nr,String baseurl) throws FileNotFoundException, IOException, DocumentException, ServiceException, JSONException {
       KwlReturnObject kwlSO = null;
        try {
            String soNumber, customerName, currencyName;
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            SalesOrder salesOrder=null;
            BillingSalesOrder billingSO=null;
            Date todayDate = new Date();
            DateFormat formatter;
            formatter = new SimpleDateFormat("dd-MM-yyyy");
            String today = formatter.format(todayDate);

            List headerItems = null;
            List finalData = new ArrayList();
            ArrayList<CustomDesignLineItemProp> headerlist = new ArrayList();
            requestParams.put("gcurrencyid", baseCurrencyId);
            requestParams.put("companyid", companyId);
            headerItems = getHeaderData(true,Constants.Acc_Sales_Order_ModuleId);
            if (userMailid.length>0) {
                boolean isSalesOrder = false;
                int sno = 1;
                String htmlText = "";
                String subject = mailSubject;
                String plainMsg = "";
                //String from = Constants.ADMIN_EMAILID;
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
                Company company = (Company) returnObject.getEntityList().get(0);
                String from = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                htmlText = mailContent.replace(Constants.DateLabel_MailContent_Placeholder, simpleDuedate);
                htmlText = htmlText.replace(Constants.TodayDateLabel_MailContent_Placeholder, today);
                
                KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyId);
                CompanyAccountPreferences pref = (CompanyAccountPreferences) cap.getEntityList().get(0);
                String cashAccount = pref.getCashAccount().getID();
                
                kwlSO = accountingHandlerDAOobj.getSOdateSalesOrderList(companyId, dbDuedate);
                List list = kwlSO.getEntityList();
                Iterator iterator = list.iterator();
                
                while (iterator.hasNext()) {
                    List data = new ArrayList();
                    salesOrder = (SalesOrder) iterator.next();
                    if (salesOrder != null) {                            
                          isSalesOrder = true;
                          soNumber = salesOrder.getSalesOrderNumber();
                          Date orderDate = (Date) salesOrder.getOrderDate();
                          currencyName = salesOrder.getCurrency().getName();
                          customerName = salesOrder.getCustomer().getName();
                        
                          String mainHtml = "";
                          String templateid = !StringUtil.isNullOrEmpty(nr.getTemplateid()) ? nr.getTemplateid() : "";
                          if (!StringUtil.isNullOrEmpty(templateid) && !Constants.NONE.equalsIgnoreCase(templateid)) {
                              JSONObject requestObj = new JSONObject();
                              requestObj.put(Constants.moduleid, Constants.Acc_Sales_Order_ModuleId);
                              requestObj.put(Constants.isdefaultHeaderMap, true);
                              requestObj.put(Constants.companyKey, companyId);
                              requestObj.put(Constants.TEMPLATEID_KEY, templateid);
                              requestObj.put(Constants.billid, salesOrder.getID());
                              requestObj.put(Constants.userid, (salesOrder != null && salesOrder.getCreatedby() != null) ? salesOrder.getCreatedby().getUserID() : "");

                              StringBuilder appendString = new StringBuilder();
                              String resturl = accDocumentDesignService.PrintTemplateRestUrl(requestObj);
                              String templateurl = "<a href='" + resturl + "' target='_blank'>" + soNumber + "</a>";

                              appendString.append(templateurl);
                              mainHtml = appendString.toString();
                          }

                          data.add(sno);
                          data.add(!StringUtil.isNullOrEmpty(mainHtml) ? mainHtml : soNumber);
                          data.add(customerName);
                          if(salesOrder.getCreatedby()!=null && salesOrder.getCreatedby().getTimeZone()!=null && salesOrder.getCreatedby().getTimeZone().getDifference()!=null){
                              SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss aa");
                              sdf.setTimeZone(TimeZone.getTimeZone("GMT" + salesOrder.getCreatedby().getTimeZone().getDifference()));
                              String date=sdf.format(orderDate);                              
                              orderDate=formatter.parse(date);                              
                          }
                          data.add(formatter.format(orderDate));
                          data.add(currencyName);
                          finalData.add(data);
                          sno++;
                    }
                }
                kwlSO = accountingHandlerDAOobj.getSOdateBillingSalesOrderList(companyId, dbDuedate);
                list = kwlSO.getEntityList();
                iterator = list.iterator();
                while (iterator.hasNext()) {
                    List data = new ArrayList();
                    billingSO = (BillingSalesOrder) iterator.next(); 
                    isSalesOrder = true;
                    soNumber = billingSO.getSalesOrderNumber();
                    Date orderDate = (Date) billingSO.getOrderDate();
                    currencyName = billingSO.getCurrency().getName();
                    customerName = billingSO.getCustomer().getName();
                    
                    
                    String mainHtml = "";
                    String templateid = !StringUtil.isNullOrEmpty(nr.getTemplateid()) ? nr.getTemplateid() : "";
                    if (!StringUtil.isNullOrEmpty(templateid) && !Constants.NONE.equalsIgnoreCase(templateid)) {
                        JSONObject requestObj = new JSONObject();
                        requestObj.put(Constants.moduleid, Constants.Acc_Sales_Order_ModuleId);
                        requestObj.put(Constants.isdefaultHeaderMap, true);
                        requestObj.put(Constants.companyKey, companyId);
                        requestObj.put(Constants.TEMPLATEID_KEY, templateid);
                        requestObj.put(Constants.billid, salesOrder.getID());
                        requestObj.put(Constants.userid, (salesOrder != null && salesOrder.getCreatedby() != null) ? salesOrder.getCreatedby().getUserID() : "");

                        StringBuilder appendString = new StringBuilder();
                        String resturl = accDocumentDesignService.PrintTemplateRestUrl(requestObj);
                        String templateurl = "<a href='" + resturl + "' target='_blank'>" + soNumber + "</a>";

                        appendString.append(templateurl);
                        mainHtml = appendString.toString();
                    }

                    data.add(sno);
                    data.add(!StringUtil.isNullOrEmpty(mainHtml) ? mainHtml : soNumber);
                    data.add(customerName);
                    data.add(formatter.format(orderDate));
                    data.add(currencyName);
                    finalData.add(data);
                    sno++;
                }
                if (isSalesOrder) {
                    for (Object header : headerItems) {
                        CustomDesignLineItemProp headerprop = new CustomDesignLineItemProp();
                        String a = header.toString();
                        headerprop.setAlign("left");
                        headerprop.setData(a);
                        headerprop.setWidth("50px");
                        headerlist.add(headerprop);
                    }
                    List finalProductList = new ArrayList();
                    for (Object headerdata : finalData) {
                        ArrayList<CustomDesignLineItemProp> prodlist = new ArrayList();
                        List datalist = (List) headerdata;
                        for (Object hdata : datalist) {
                            CustomDesignLineItemProp prop = new CustomDesignLineItemProp();
                            prop.setAlign("left");
                            prop.setData(hdata.toString());
                            prodlist.add(prop);
                        }
                        finalProductList.add(prodlist);
                    }
                    String top = "10px", left = "10px", tablewidth = CustomDesignHandler.pageWidth;
                    StringWriter writer = new StringWriter();
                    VelocityEngine ve = new VelocityEngine();
                    ve.init();
                    VelocityContext context = new VelocityContext();
                    context.put("tableHeader", headerlist);
                    context.put("prodList", finalProductList);
                    context.put("top", top);
                    context.put("left", left);
                    context.put("width", tablewidth);
                    velocityEngine.mergeTemplate("duemailitems.vm", "UTF-8", context, writer);
                    String tablehtml = writer.toString();
                    htmlText = htmlText.concat(tablehtml);
                    htmlText+=baseurl;
                    Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                    SendMailHandler.postMail(userMailid, subject, htmlText, plainMsg, from, smtpConfigMap);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void sendPurchaseOrderMail(String dbDuedate, String simpleDuedate, String companyId, String companyName, String baseCurrencyId, String userFname,
                String userLname, String[]  userMailid, String mailContent, String mailSubject, NotificationRules nr,String baseurl) throws FileNotFoundException, IOException, DocumentException, ServiceException, JSONException {
        KwlReturnObject kwlPO = null;
        try {
            String poNumber, vendorName, currencyName;
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            PurchaseOrder purchaseOrder=null;
            BillingPurchaseOrder billingPO=null;
            Date todayDate = new Date();
            DateFormat formatter;
            formatter = new SimpleDateFormat("dd-MM-yyyy");
            String today = formatter.format(todayDate);

            List headerItems = null;
            List finalData = new ArrayList();
            ArrayList<CustomDesignLineItemProp> headerlist = new ArrayList();
            requestParams.put("gcurrencyid", baseCurrencyId);
            requestParams.put("companyid", companyId);
            headerItems = getHeaderData(false,Constants.Acc_Purchase_Order_ModuleId);
            if (userMailid.length>0) {
                boolean isPurchaseOrder = false;
                int sno = 1;
                String htmlText = "";
                String subject = mailSubject;
                String plainMsg = "";
                //String from = Constants.ADMIN_EMAILID;
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
                Company company = (Company) returnObject.getEntityList().get(0);
                String from = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                htmlText = mailContent.replace(Constants.DateLabel_MailContent_Placeholder, simpleDuedate);
                htmlText = htmlText.replace(Constants.TodayDateLabel_MailContent_Placeholder, today);
                
                KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyId);
                CompanyAccountPreferences pref = (CompanyAccountPreferences) cap.getEntityList().get(0);
                String cashAccount = pref.getCashAccount().getID();
                
                kwlPO = accountingHandlerDAOobj.getPOdatePurchaseOrderList(companyId, dbDuedate);
                List list = kwlPO.getEntityList();
                Iterator iterator = list.iterator();
                
                while (iterator.hasNext()) {
                    List data = new ArrayList();
                    purchaseOrder = (PurchaseOrder) iterator.next();
                    if (purchaseOrder != null) {                            
                        isPurchaseOrder = true;
                        poNumber = purchaseOrder.getPurchaseOrderNumber();
                        Date orderDate = (Date) purchaseOrder.getOrderDate();
                        currencyName = purchaseOrder.getCurrency().getName();
                        vendorName = purchaseOrder.getVendor().getName();

                        String mainHtml = "";
                        String templateid = !StringUtil.isNullOrEmpty(nr.getTemplateid()) ? nr.getTemplateid() : "";
                        if (!StringUtil.isNullOrEmpty(templateid) && !Constants.NONE.equalsIgnoreCase(templateid)) {
                            JSONObject requestObj = new JSONObject();
                            requestObj.put(Constants.moduleid, Constants.Acc_Purchase_Order_ModuleId);
                            requestObj.put(Constants.isdefaultHeaderMap, true);
                            requestObj.put(Constants.companyKey, companyId);
                            requestObj.put(Constants.TEMPLATEID_KEY, templateid);
                            requestObj.put(Constants.billid, purchaseOrder.getID());
                            requestObj.put(Constants.userid, (purchaseOrder != null && purchaseOrder.getCreatedby() != null) ? purchaseOrder.getCreatedby().getUserID() : "");

                            StringBuilder appendString = new StringBuilder();
                            String resturl = accDocumentDesignService.PrintTemplateRestUrl(requestObj);
                            String templateurl = "<a href='" + resturl + "' target='_blank'>" + poNumber + "</a>";

                            appendString.append(templateurl);
                            mainHtml = appendString.toString();
                        }

                        data.add(sno);
                        data.add(!StringUtil.isNullOrEmpty(mainHtml) ? mainHtml : poNumber);
                        data.add(vendorName);
                        data.add(formatter.format(orderDate));
                        data.add(currencyName);
                        finalData.add(data);
                        sno++;
                    }
                }
                kwlPO = accountingHandlerDAOobj.getPOdateBillingPurchaseOrderList(companyId, dbDuedate);
                list = kwlPO.getEntityList();
                iterator = list.iterator();
                while (iterator.hasNext()) {
                    List data = new ArrayList();
                    billingPO = (BillingPurchaseOrder) iterator.next(); 
                    isPurchaseOrder = true;
                    poNumber = billingPO.getPurchaseOrderNumber();
                    Date orderDate = (Date) billingPO.getOrderDate();
                    currencyName = billingPO.getCurrency().getName();
                    vendorName = billingPO.getVendor().getName();
                    
                    String mainHtml = "";
                    String templateid = !StringUtil.isNullOrEmpty(nr.getTemplateid()) ? nr.getTemplateid() : "";
                    if (!StringUtil.isNullOrEmpty(templateid) && !Constants.NONE.equalsIgnoreCase(templateid)) {
                        JSONObject requestObj = new JSONObject();
                        requestObj.put(Constants.moduleid, Constants.Acc_Purchase_Order_ModuleId);
                        requestObj.put(Constants.isdefaultHeaderMap, true);
                        requestObj.put(Constants.companyKey, companyId);
                        requestObj.put(Constants.TEMPLATEID_KEY, templateid);
                        requestObj.put(Constants.billid, purchaseOrder.getID());
                        requestObj.put(Constants.userid, (purchaseOrder != null && purchaseOrder.getCreatedby() != null) ? purchaseOrder.getCreatedby().getUserID() : "");

                        StringBuilder appendString = new StringBuilder();
                        String resturl = accDocumentDesignService.PrintTemplateRestUrl(requestObj);
                        String templateurl = "<a href='" + resturl + "' target='_blank'>" + poNumber + "</a>";

                        appendString.append(templateurl);
                        mainHtml = appendString.toString();
                    }

                    data.add(sno);
                    data.add(!StringUtil.isNullOrEmpty(mainHtml) ? mainHtml : poNumber);
                    data.add(vendorName);
                    data.add(formatter.format(orderDate));
                    data.add(currencyName);
                    finalData.add(data);
                    sno++;
                }
                if (isPurchaseOrder) {
                    for (Object header : headerItems) {
                        CustomDesignLineItemProp headerprop = new CustomDesignLineItemProp();
                        String a = header.toString();
                        headerprop.setAlign("left");
                        headerprop.setData(a);
                        headerprop.setWidth("50px");
                        headerlist.add(headerprop);
                    }
                    List finalProductList = new ArrayList();
                    for (Object headerdata : finalData) {
                        ArrayList<CustomDesignLineItemProp> prodlist = new ArrayList();
                        List datalist = (List) headerdata;
                        for (Object hdata : datalist) {
                            CustomDesignLineItemProp prop = new CustomDesignLineItemProp();
                            prop.setAlign("left");
                            prop.setData(hdata.toString());
                            prodlist.add(prop);
                        }
                        finalProductList.add(prodlist);
                    }
                    String top = "10px", left = "10px", tablewidth = CustomDesignHandler.pageWidth;
                    StringWriter writer = new StringWriter();
                    VelocityEngine ve = new VelocityEngine();
                    ve.init();
                    VelocityContext context = new VelocityContext();
                    context.put("tableHeader", headerlist);
                    context.put("prodList", finalProductList);
                    context.put("top", top);
                    context.put("left", left);
                    context.put("width", tablewidth);
                    velocityEngine.mergeTemplate("duemailitems.vm", "UTF-8", context, writer);
                    String tablehtml = writer.toString();
                    htmlText = htmlText.concat(tablehtml);
                    htmlText+=baseurl;
                    Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                    SendMailHandler.postMail(userMailid, subject, htmlText, plainMsg, from, smtpConfigMap);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void sendDeliveryOrderMail(String dbDuedate, String simpleDuedate, String companyId, String companyName, String baseCurrencyId, String userFname,
                String userLname, String[]  userMailid, String mailContent, String mailSubject, NotificationRules nr,String baseurl) throws FileNotFoundException, IOException, DocumentException, ServiceException, JSONException {
        KwlReturnObject kwlDO = null;
        try {
            String doNumber, customerName, currencyName;
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            DeliveryOrder deliveryOrder=null;
            Date todayDate = new Date();
            DateFormat formatter;
            formatter = new SimpleDateFormat("dd-MM-yyyy");
            String today = formatter.format(todayDate);

            List headerItems = null;
            List finalData = new ArrayList();
            ArrayList<CustomDesignLineItemProp> headerlist = new ArrayList();
            requestParams.put("gcurrencyid", baseCurrencyId);
            requestParams.put("companyid", companyId);
            headerItems = getHeaderData(true,Constants.Acc_Delivery_Order_ModuleId);
            if (userMailid.length>0) {
                boolean isDeliveryOrder = false;
                int sno = 1;
                String htmlText = "";
                String subject = mailSubject;
                String plainMsg = "";
                //String from = Constants.ADMIN_EMAILID;
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
                Company company = (Company) returnObject.getEntityList().get(0);
                String from = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                htmlText = mailContent.replace(Constants.DateLabel_MailContent_Placeholder, simpleDuedate);
                htmlText = htmlText.replace(Constants.TodayDateLabel_MailContent_Placeholder, today);
                
                KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyId);
                CompanyAccountPreferences pref = (CompanyAccountPreferences) cap.getEntityList().get(0);
                String cashAccount = pref.getCashAccount().getID();
                
                kwlDO = accountingHandlerDAOobj.getDOdateDeliveryOrderList(companyId, dbDuedate);
                List list = kwlDO.getEntityList();
                Iterator iterator = list.iterator();
                
                while (iterator.hasNext()) {
                    List data = new ArrayList();
                    deliveryOrder = (DeliveryOrder) iterator.next();
                    if (deliveryOrder != null) {                            
                          isDeliveryOrder = true;
                        doNumber = deliveryOrder.getDeliveryOrderNumber();
                        Date orderDate = (Date) deliveryOrder.getOrderDate();
                        customerName = deliveryOrder.getCustomer().getName();

                        String mainHtml = "";
                        String templateid = !StringUtil.isNullOrEmpty(nr.getTemplateid()) ? nr.getTemplateid() : "";
                        if (!StringUtil.isNullOrEmpty(templateid) && !Constants.NONE.equalsIgnoreCase(templateid)) {
                            JSONObject requestObj = new JSONObject();
                            requestObj.put(Constants.moduleid, Constants.Acc_Delivery_Order_ModuleId);
                            requestObj.put(Constants.isdefaultHeaderMap, true);
                            requestObj.put(Constants.companyKey, companyId);
                            requestObj.put(Constants.TEMPLATEID_KEY, templateid);
                            requestObj.put(Constants.billid, deliveryOrder.getID());
                            requestObj.put(Constants.userid, (deliveryOrder != null && deliveryOrder.getCreatedby() != null) ? deliveryOrder.getCreatedby().getUserID() : "");

                            StringBuilder appendString = new StringBuilder();
                            String resturl = accDocumentDesignService.PrintTemplateRestUrl(requestObj);
                            String templateurl = "<a href='" + resturl + "' target='_blank'>" + doNumber + "</a>";

                            appendString.append(templateurl);
                            mainHtml = appendString.toString();
                        }

                        data.add(sno);
                        data.add(!StringUtil.isNullOrEmpty(mainHtml) ? mainHtml : doNumber);
                        data.add(customerName);
                        data.add(formatter.format(orderDate));
                        finalData.add(data);
                        sno++;
                    }
                }
                if (isDeliveryOrder) {
                    for (Object header : headerItems) {
                        CustomDesignLineItemProp headerprop = new CustomDesignLineItemProp();
                        String a = header.toString();
                        headerprop.setAlign("left");
                        headerprop.setData(a);
                        headerprop.setWidth("50px");
                        headerlist.add(headerprop);
                    }
                    List finalProductList = new ArrayList();
                    for (Object headerdata : finalData) {
                        ArrayList<CustomDesignLineItemProp> prodlist = new ArrayList();
                        List datalist = (List) headerdata;
                        for (Object hdata : datalist) {
                            CustomDesignLineItemProp prop = new CustomDesignLineItemProp();
                            prop.setAlign("left");
                            prop.setData(hdata.toString());
                            prodlist.add(prop);
                        }
                        finalProductList.add(prodlist);
                    }
                    String top = "10px", left = "10px", tablewidth = CustomDesignHandler.pageWidth;
                    StringWriter writer = new StringWriter();
                    VelocityEngine ve = new VelocityEngine();
                    ve.init();
                    VelocityContext context = new VelocityContext();
                    context.put("tableHeader", headerlist);
                    context.put("prodList", finalProductList);
                    context.put("top", top);
                    context.put("left", left);
                    context.put("width", tablewidth);
                    velocityEngine.mergeTemplate("duemailitems.vm", "UTF-8", context, writer);
                    String tablehtml = writer.toString();
                    htmlText = htmlText.concat(tablehtml);
                    htmlText+=baseurl;
                    Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                    SendMailHandler.postMail(userMailid, subject, htmlText, plainMsg, from, smtpConfigMap);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void sendProductExpDeliveryOrderMail(String dbDuedate, String simpleDuedate, String companyId, String companyName, String baseCurrencyId, String userFname,
                String userLname, String[]  userMailid, String mailContent, String mailSubject,String baseurl) throws FileNotFoundException, IOException, DocumentException, ServiceException, JSONException {
        KwlReturnObject kwlDO = null;
        try {
            String doNumber, customerName, currencyName,doid;
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            DeliveryOrder deliveryOrder=null;
            Date todayDate = new Date();
            DateFormat formatter;
            formatter = new SimpleDateFormat("dd-MM-yyyy");
            String today = formatter.format(todayDate);

            List headerItems = null;
            List finalData = new ArrayList();
            ArrayList<CustomDesignLineItemProp> headerlist = new ArrayList();
            requestParams.put("gcurrencyid", baseCurrencyId);
            requestParams.put("companyid", companyId);
            headerItems = getHeaderData(true,Constants.Acc_Delivery_Order_ModuleId);
            if (userMailid.length>0) {
                boolean isDeliveryOrder = false;
                int sno = 1;
                String htmlText = "";
                String subject = mailSubject;
                String plainMsg = "";
                //String from = Constants.ADMIN_EMAILID;
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
                Company company = (Company) returnObject.getEntityList().get(0);
                String from = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                htmlText = mailContent.replace(Constants.DateLabel_MailContent_Placeholder, simpleDuedate);
                htmlText = htmlText.replace(Constants.TodayDateLabel_MailContent_Placeholder, today);
                
                KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyId);
                CompanyAccountPreferences pref = (CompanyAccountPreferences) cap.getEntityList().get(0);
                String cashAccount = pref.getCashAccount().getID();
                
                kwlDO = accountingHandlerDAOobj.getProductExpdateDeliveryOrderList(companyId, dbDuedate);
                List list = kwlDO.getEntityList();
                Iterator iterator = list.iterator();
                while (iterator.hasNext()) {
                    Object[] oj = (Object[]) iterator.next();
                    String orderid = oj[0].toString();
                    String productName = oj[3].toString();

                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(DeliveryOrder.class.getName(), orderid);
                    DeliveryOrder dOrder = (DeliveryOrder) objItr.getEntityList().get(0);
                      
                    List data = new ArrayList();
                    if (dOrder != null) {
                        isDeliveryOrder = true;
                        doid=dOrder.getID();
                        doNumber = dOrder.getDeliveryOrderNumber();
                        Date orderDate = (Date) dOrder.getOrderDate();
                        customerName = dOrder.getCustomer().getName();
                
                        data.add(sno);
                        data.add(doNumber);
                        data.add(customerName);
                        data.add(formatter.format(orderDate));
                        data.add(productName);
                        finalData.add(data);
                        sno++;
                    }
                }
                if (isDeliveryOrder) {
                    for (Object header : headerItems) {
                        CustomDesignLineItemProp headerprop = new CustomDesignLineItemProp();
                        String a = header.toString();
                        headerprop.setAlign("left");
                        headerprop.setData(a);
                        headerprop.setWidth("50px");
                        headerlist.add(headerprop);
                    }
                    List finalProductList = new ArrayList();
                    for (Object headerdata : finalData) {
                        ArrayList<CustomDesignLineItemProp> prodlist = new ArrayList();
                        List datalist = (List) headerdata;
                        for (Object hdata : datalist) {
                            CustomDesignLineItemProp prop = new CustomDesignLineItemProp();
                            prop.setAlign("left");
                            prop.setData(hdata.toString());
                            prodlist.add(prop);
                        }
                        finalProductList.add(prodlist);
                    }
                    String top = "10px", left = "10px", tablewidth = CustomDesignHandler.pageWidth;
                    StringWriter writer = new StringWriter();
                    VelocityEngine ve = new VelocityEngine();
                    ve.init();
                    VelocityContext context = new VelocityContext();
                    context.put("tableHeader", headerlist);
                    context.put("prodList", finalProductList);
                    context.put("top", top);
                    context.put("left", left);
                    context.put("width", tablewidth);
                    velocityEngine.mergeTemplate("duemailitems.vm", "UTF-8", context, writer);
                    String tablehtml = writer.toString();
                    htmlText = htmlText.concat(tablehtml);
                    htmlText+=baseurl;
                    Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                    SendMailHandler.postMail(userMailid, subject, htmlText, plainMsg, from, smtpConfigMap);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void sendProductExpGoodsReceiptOrderMail(String dbDuedate, String simpleDuedate, String companyId, String companyName, String baseCurrencyId, String userFname,
                String userLname, String[]  userMailid, String mailContent, String mailSubject,String baseurl) throws FileNotFoundException, IOException, DocumentException, ServiceException, JSONException {
        KwlReturnObject kwlDO = null;
        try {
            String doNumber, customerName, currencyName,doid;
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            DeliveryOrder deliveryOrder=null;
            Date todayDate = new Date();
            DateFormat formatter;
            formatter = new SimpleDateFormat("dd-MM-yyyy");
            String today = formatter.format(todayDate);

            List headerItems = null;
            List finalData = new ArrayList();
            ArrayList<CustomDesignLineItemProp> headerlist = new ArrayList();
            requestParams.put("gcurrencyid", baseCurrencyId);
            requestParams.put("companyid", companyId);
            headerItems = getHeaderData(false,Constants.Acc_Goods_Receipt_ModuleId);
            if (userMailid.length>0) {
                boolean isDeliveryOrder = false;
                int sno = 1;
                String htmlText = "";
                String subject = mailSubject;
                String plainMsg = "";
                //String from = Constants.ADMIN_EMAILID;
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
                Company company = (Company) returnObject.getEntityList().get(0);
                String from = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                htmlText = mailContent.replace(Constants.DateLabel_MailContent_Placeholder, simpleDuedate);
                htmlText = htmlText.replace(Constants.TodayDateLabel_MailContent_Placeholder, today);
                
                KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyId);
                CompanyAccountPreferences pref = (CompanyAccountPreferences) cap.getEntityList().get(0);
                String cashAccount = pref.getCashAccount().getID();
                
                kwlDO = accountingHandlerDAOobj.getProductExpdateGoodsReceiptOrderList(companyId, dbDuedate);
                List list = kwlDO.getEntityList();
                Iterator iterator = list.iterator();
                while (iterator.hasNext()) {
                    Object[] oj = (Object[]) iterator.next();
                    String orderid = oj[0].toString();
                     String productName = oj[3].toString();

                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(GoodsReceiptOrder.class.getName(), orderid);
                    GoodsReceiptOrder dOrder = (GoodsReceiptOrder) objItr.getEntityList().get(0);
                      
                    List data = new ArrayList();
                    if (dOrder != null) {
                        isDeliveryOrder = true;
                        doid=dOrder.getID();
                        doNumber = dOrder.getGoodsReceiptOrderNumber();
                        Date orderDate = (Date) dOrder.getOrderDate();
                        customerName = dOrder.getVendor().getName();
                    
                        data.add(sno);
                        data.add(doNumber);
                        data.add(customerName);
                        data.add(formatter.format(orderDate));
                        data.add(productName);
                        finalData.add(data);
                        sno++;
                    }
                }
                if (isDeliveryOrder) {
                    for (Object header : headerItems) {
                        CustomDesignLineItemProp headerprop = new CustomDesignLineItemProp();
                        String a = header.toString();
                        headerprop.setAlign("left");
                        headerprop.setData(a);
                        headerprop.setWidth("50px");
                        headerlist.add(headerprop);
                    }
                    List finalProductList = new ArrayList();
                    for (Object headerdata : finalData) {
                        ArrayList<CustomDesignLineItemProp> prodlist = new ArrayList();
                        List datalist = (List) headerdata;
                        for (Object hdata : datalist) {
                            CustomDesignLineItemProp prop = new CustomDesignLineItemProp();
                            prop.setAlign("left");
                            prop.setData(hdata.toString());
                            prodlist.add(prop);
                        }
                        finalProductList.add(prodlist);
                    }
                    String top = "10px", left = "10px", tablewidth = CustomDesignHandler.pageWidth;
                    StringWriter writer = new StringWriter();
                    VelocityEngine ve = new VelocityEngine();
                    ve.init();
                    VelocityContext context = new VelocityContext();
                    context.put("tableHeader", headerlist);
                    context.put("prodList", finalProductList);
                    context.put("top", top);
                    context.put("left", left);
                    context.put("width", tablewidth);
                    velocityEngine.mergeTemplate("duemailitems.vm", "UTF-8", context, writer);
                    String tablehtml = writer.toString();
                    htmlText = htmlText.concat(tablehtml);
                    htmlText+=baseurl;
                    Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                    SendMailHandler.postMail(userMailid, subject, htmlText, plainMsg, from, smtpConfigMap);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void sendGROrderDeliveryOrderSerialCheckMail(String dbDuedate, String simpleDuedate, String companyId, String companyName, String baseCurrencyId, String userFname,
                String userLname, String[]  userMailid, String mailContent, String mailSubject, NotificationRules nr,String baseurl) throws FileNotFoundException, IOException, DocumentException, ServiceException, JSONException {
        KwlReturnObject kwlGRODOCheck = null;
        try {
            String groNumber, vendorName, prodName, batchName;
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            GoodsReceiptOrder goodsReceiptOrder = null;
            Date todayDate = new Date();
            DateFormat formatter;
            formatter = new SimpleDateFormat("dd-MM-yyyy");
            String today = formatter.format(todayDate);

            List headerItems = new ArrayList();;
            List finalData = new ArrayList();
            ArrayList<CustomDesignLineItemProp> headerlist = new ArrayList();
            requestParams.put("gcurrencyid", baseCurrencyId);
            requestParams.put("companyid", companyId);
            headerItems.add("S.No.");
            headerItems.add("GR Order Number");
            headerItems.add("Vendor Name");
            headerItems.add("Order Date");
            headerItems.add("Product Name");
            headerItems.add("Serial Number(s)");
            if (userMailid.length > 0) {
                boolean isGoodsReceiptOrder = false;
                int sno = 1;
                String htmlText = "";
                String subject = mailSubject;
                String plainMsg = "";
                //String from = Constants.ADMIN_EMAILID;  
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
                Company company = (Company) returnObject.getEntityList().get(0);
                String from = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                
                htmlText = mailContent.replace(Constants.DateLabel_MailContent_Placeholder, simpleDuedate);
                htmlText = htmlText.replace(Constants.TodayDateLabel_MailContent_Placeholder, today);

                KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyId);
                CompanyAccountPreferences pref = (CompanyAccountPreferences) cap.getEntityList().get(0);
                String cashAccount = pref.getCashAccount().getID();

                kwlGRODOCheck = accountingHandlerDAOobj.getGRODateGoodsReceiptOrderList(companyId, dbDuedate);
                List list = kwlGRODOCheck.getEntityList();
                Iterator iterator = list.iterator();

                while (iterator.hasNext()) {
                    ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                    HashMap<String, Object> groRequestParams = new HashMap<String, Object>();
                    goodsReceiptOrder = (GoodsReceiptOrder) iterator.next();
                    if (goodsReceiptOrder != null) {
                        isGoodsReceiptOrder = true;
                        filter_names.add("grOrder.ID");
                        filter_params.add(goodsReceiptOrder.getID());
                        groRequestParams.put("filter_names", filter_names);
                        groRequestParams.put("filter_params", filter_params);
                                                
                        KwlReturnObject grodresult = accGoodsReceiptobj.getGoodsReceiptOrderDetails(groRequestParams);
                        Iterator itr = grodresult.getEntityList().iterator();
                        while (itr.hasNext()) {
                            groNumber = goodsReceiptOrder.getGoodsReceiptOrderNumber();
                            Date orderDate = (Date) goodsReceiptOrder.getOrderDate();
                            vendorName = goodsReceiptOrder.getVendor().getName();
                            
                            List data = new ArrayList();
                            String mainHtml = "";
                        String templateid = !StringUtil.isNullOrEmpty(nr.getTemplateid()) ? nr.getTemplateid() : "";
                        if (!StringUtil.isNullOrEmpty(templateid) && !Constants.NONE.equalsIgnoreCase(templateid)) {
                            JSONObject requestObj = new JSONObject();
                            requestObj.put(Constants.moduleid, Constants.Acc_Goods_Receipt_ModuleId);
                            requestObj.put(Constants.isdefaultHeaderMap, true);
                            requestObj.put(Constants.companyKey, companyId);
                            requestObj.put(Constants.TEMPLATEID_KEY, templateid);
                            requestObj.put(Constants.billid, goodsReceiptOrder.getID());
                            requestObj.put(Constants.userid, (goodsReceiptOrder != null && goodsReceiptOrder.getCreatedby() != null) ? goodsReceiptOrder.getCreatedby().getUserID() : "");

                            StringBuilder appendString = new StringBuilder();
                            String resturl = accDocumentDesignService.PrintTemplateRestUrl(requestObj);
                            String templateurl = "<a href='" + resturl + "' target='_blank'>" + groNumber + "</a>";

                            appendString.append(templateurl);
                            mainHtml = appendString.toString();
                        }

                        data.add(sno);
                        data.add(!StringUtil.isNullOrEmpty(mainHtml) ? mainHtml : groNumber);
                            data.add(vendorName);
                            data.add(formatter.format(orderDate));
                            
                            
                            GoodsReceiptOrderDetails goodsReceiptOrderDetailsObj = (GoodsReceiptOrderDetails) itr.next();
                            prodName = goodsReceiptOrderDetailsObj.getProduct().getName();
                            data.add(prodName);

                            ProductBatch prodBatch = null;
                            String batchId;
                            prodBatch = goodsReceiptOrderDetailsObj.getBatch();
                            KwlReturnObject srNoObj = null;
                            boolean serialCheck=false;
                            if (prodBatch != null) {
                                HashMap<String, Object> requestParams1 = new HashMap<String, Object>();
                                batchId = prodBatch.getId();
//                                batchName = prodBatch.getName();
//                                data.add(batchName);
                                requestParams1.put("batch", batchId);
                                requestParams1.put("companyid", companyId);
                                srNoObj = accMasterItemsDAOobj.getSerials(requestParams1);
                                if (srNoObj != null && srNoObj.getEntityList().size() > 0) {
                                    List list1 = srNoObj.getEntityList();
                                    Iterator itr1 = list1.iterator();
                                    while (itr1.hasNext()) {
                                        Object[] oj = (Object[]) itr1.next();
                                        String batchid = oj[0].toString();
                                        KwlReturnObject batchs = accountingHandlerDAOobj.getObject(BatchSerial.class.getName(), batchid);
                                        List<BatchSerial> prd = batchs.getEntityList();
                                        data.add(prd.get(0).getName());
                                        finalData.add(data);
                                        data = new ArrayList();
//                                        for (int i = 0; i < headerItems.size()-1; i++) {
                                        sno++;
                                        mainHtml = "";
                                        if (!StringUtil.isNullOrEmpty(templateid) && !Constants.NONE.equalsIgnoreCase(templateid)) {
                                            JSONObject requestObj = new JSONObject();
                                            requestObj.put(Constants.moduleid, Constants.Acc_Goods_Receipt_ModuleId);
                                            requestObj.put(Constants.isdefaultHeaderMap, true);
                                            requestObj.put(Constants.companyKey, companyId);
                                            requestObj.put(Constants.TEMPLATEID_KEY, templateid);
                                            requestObj.put(Constants.billid, goodsReceiptOrder.getID());
                                            requestObj.put(Constants.userid, (goodsReceiptOrder != null && goodsReceiptOrder.getCreatedby() != null) ? goodsReceiptOrder.getCreatedby().getUserID() : "");

                                            StringBuilder appendString = new StringBuilder();
                                            String resturl = accDocumentDesignService.PrintTemplateRestUrl(requestObj);
                                            String templateurl = "<a href='" + resturl + "' target='_blank'>" + groNumber + "</a>";

                                            appendString.append(templateurl);
                                            mainHtml = appendString.toString();
                                        }

                                        data.add(sno);
                                        data.add(!StringUtil.isNullOrEmpty(mainHtml) ? mainHtml : groNumber);
                                        data.add(vendorName);
                                        data.add(formatter.format(orderDate));
                                        data.add(prodName);
//                                        }
                                        serialCheck = true;
                                    }
                                } else {
                                    data.add("");
                                    finalData.add(data);
                                }
//                                if(serialCheck){
//                                    data=new ArrayList();
//                                    for (int i = 0; i < headerItems.size()-3; i++) {
//                                        data.add("");
//                                    }
//                                }
                            } else {
                                data.add("");
                                finalData.add(data);
                            }  
                            if(!serialCheck){
                                sno++;
                            }
                        }
                    }
                }
                if (isGoodsReceiptOrder) {
                    for (Object header : headerItems) {
                        CustomDesignLineItemProp headerprop = new CustomDesignLineItemProp();
                        String a = header.toString();
                        headerprop.setAlign("left");
                        headerprop.setData(a);
                        headerprop.setWidth("50px");
                        headerlist.add(headerprop);
                    }
                    List finalProductList = new ArrayList();
                    for (Object headerdata : finalData) {
                        ArrayList<CustomDesignLineItemProp> prodlist = new ArrayList();
                        List datalist = (List) headerdata;
                        for (Object hdata : datalist) {
                            CustomDesignLineItemProp prop = new CustomDesignLineItemProp();
                            prop.setAlign("left");
                            prop.setData(hdata.toString());
                            prodlist.add(prop);
                        }
                        finalProductList.add(prodlist);
                    }
                    String top = "10px", left = "10px", tablewidth = CustomDesignHandler.pageWidth;
                    StringWriter writer = new StringWriter();
                    VelocityEngine ve = new VelocityEngine();
                    ve.init();
                    VelocityContext context = new VelocityContext();
                    context.put("tableHeader", headerlist);
                    context.put("prodList", finalProductList);
                    context.put("top", top);
                    context.put("left", left);
                    context.put("width", tablewidth);
                    velocityEngine.mergeTemplate("duemailitems.vm", "UTF-8", context, writer);
                    String tablehtml = writer.toString();
                    htmlText = htmlText.concat(tablehtml);
                    htmlText+=baseurl;
                    Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                    SendMailHandler.postMail(userMailid, subject, htmlText, plainMsg, from, smtpConfigMap);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public void sendProductNotSoldMail(String dbDuedate, String simpleDuedate, String companyId, String companyName, String baseCurrencyId, String userFname,
            String userLname, String[] userMailid, String mailContent, String mailSubject,String baseurl) throws FileNotFoundException, IOException, DocumentException, ServiceException, JSONException {
        KwlReturnObject kwlGRODOCheck = null;
        try {
        
            Date todayDate = new Date();
            DateFormat formatter;
            formatter = new SimpleDateFormat("dd-MM-yyyy");
            DateFormat dbformat=new SimpleDateFormat("yyyy-MM-dd");
            String today = formatter.format(todayDate);
            int duration = 45;
            Date curDate = new Date();

            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            Calendar cld = Calendar.getInstance();
            cal1.setTime(curDate);
            cal2.setTime(curDate);
            cal1.add(Calendar.DAY_OF_YEAR, -duration);
            cal2.add(Calendar.DAY_OF_YEAR, -(duration * 2));
            
            
            cld.setTime(dbformat.parse(dbDuedate));
            cld.add(Calendar.YEAR, -1); // number of years to add
            Date lastdueDate = cld.getTime();
            String lastYearDate = dbformat.format(lastdueDate);
                    
            boolean isLifo = false;

           
            List finalData = new ArrayList();
            List headerItems = new ArrayList();;
            headerItems.add("S.No.");
            headerItems.add("Product Name");
            headerItems.add("Document No.");
            headerItems.add("Document Date");
            headerItems.add("Document Type");
            headerItems.add("Quantity");

            ArrayList<CustomDesignLineItemProp> headerlist = new ArrayList();
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("gcurrencyid", baseCurrencyId);
            requestParams.put("companyid", companyId);

            HashMap<String, Object> currencyParams = new HashMap<String, Object>();
            currencyParams.put(Constants.companyKey, companyId);
            currencyParams.put(Constants.globalCurrencyKey, baseCurrencyId);
            currencyParams.put(Constants.df, formatter);  //This format belongs to our global date format[i.e.new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa")]
            currencyParams.put(Constants.userdf, formatter); //This format holds users date format.



            if (userMailid.length > 0) {
                boolean isProductSold = false;
                int sno = 1;
                String htmlText = "";
                String subject = mailSubject;
                String plainMsg = "";
                //String from = Constants.ADMIN_EMAILID;
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
                Company company = (Company) returnObject.getEntityList().get(0);
                String from = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                htmlText = mailContent.replace(Constants.DateLabel_MailContent_Placeholder, simpleDuedate);
                htmlText = htmlText.replace(Constants.TodayDateLabel_MailContent_Placeholder, today);

                KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyId);
                CompanyAccountPreferences pref = (CompanyAccountPreferences) cap.getEntityList().get(0);

                KwlReturnObject onhandQuantityResult = accProductObj.getOnhandQuantityOfProduct(requestParams);
                List list = onhandQuantityResult.getEntityList();

                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    Object[] row = (Object[]) itr.next();
                    String productid = (String) row[0];
                    double onhand = (row[1] == null) ? 0 : (Double) row[1];
                    KwlReturnObject kwlReturnObject = accProductObj.getObject(Product.class.getName(), productid);
                    Product product = (Product) kwlReturnObject.getEntityList().get(0);

                    HashMap<String, Object> requestParamsforGR = new HashMap<String, Object>();
                    requestParamsforGR.put("companyid", companyId);
                    requestParamsforGR.put("productid", productid);
                    requestParamsforGR.put("lastYearDate", lastYearDate);
                    double lifo = 0;
                    List<Date> date = new ArrayList();
                    List<String> transactionNumber = new ArrayList();
                    List<Double> qty = new ArrayList();
                    List<Double> baseuomrate = new ArrayList(); // Conversion Factor
                    List<Double> rate = new ArrayList();
                    List<String> currency = new ArrayList();
                    List<String> transactionType = new ArrayList();

                    KwlReturnObject rateandQtyResult = accProductObj.getRateandQtyOfOpeningGRSR(requestParamsforGR); // getting transaction information of product opening, GR and SR
                    List rateandQtyList = rateandQtyResult.getEntityList();
                    Iterator it = rateandQtyList.iterator();
                    while (it.hasNext()) {
                        Object[] Objrow = (Object[]) it.next();
                        if (Objrow[3] != null) {
                            date.add((Date) Objrow[0]);
                            transactionNumber.add((String) Objrow[1]);
                            qty.add((Double) Objrow[2]);
                            baseuomrate.add((Double) Objrow[3]);
                            rate.add((Double) Objrow[4]);
                            if (StringUtil.isNullOrEmpty(Objrow[5].toString())) {
                                currency.add(baseCurrencyId);
                            } else {
                                currency.add((String) Objrow[5]);
                            }
                            transactionType.add((String) Objrow[6]);
                        }
                    }
                        double totalQty = onhand;
                        // For LIFO Valuation
                        if (onhand > 0 && isLifo) {
                            for (int i = 0; i < qty.size(); i++) {
                                isProductSold = true;
                                if (totalQty >= qty.get(i) && totalQty != 0) {
                                    JSONObject obj = new JSONObject();
                                    List data = new ArrayList();
                                    double tempRate = rate.get(i);
                                    Date transactionDate = date.get(i);
                                    double baseuomQty = qty.get(i) * baseuomrate.get(i);

                                    KwlReturnObject crresult = accCurrencyDAOobj.getCurrencyToBaseAmount(currencyParams, tempRate, currency.get(i), transactionDate, 0);
                                    tempRate = (Double) crresult.getEntityList().get(0);

                                    lifo = (baseuomQty * tempRate);
                                    data.add(sno);
                                    data.add(product != null ? product.getName() : "");
                                    data.add(transactionNumber.get(i-1));
                                    data.add(formatter.format(transactionDate));
                                    data.add(transactionType.get(i-1));
                                    data.add(authHandler.formattedQuantity(baseuomQty, companyId));

                                  
                                    totalQty = totalQty - baseuomQty;
                                    if (dbformat.format(transactionDate).equals(lastYearDate)) {
                                        finalData.add(data);
                                          sno++;
                                    }

                                } else if (totalQty < qty.get(i) && totalQty != 0) {
                                    JSONObject obj = new JSONObject();
                                    double tempRate = rate.get(i);
                                    Date transactionDate = date.get(i);

                                    KwlReturnObject crresult = accCurrencyDAOobj.getCurrencyToBaseAmount(currencyParams, tempRate, currency.get(i), transactionDate, 0);
                                    tempRate = (Double) crresult.getEntityList().get(0);

                                    lifo = totalQty * tempRate;
                                    List data = new ArrayList();
                                    data.add(sno);
                                    data.add(product != null ? product.getName() : "");
                                    data.add(transactionNumber.get(i-1));
                                    data.add(formatter.format(transactionDate));
                                    data.add(transactionType.get(i-1));
                                    data.add(authHandler.formattedQuantity(totalQty, companyId));
                                 
                                    totalQty = 0;
                                    if (dbformat.format(transactionDate).equals(lastYearDate)) {
                                        finalData.add(data);
                                           sno++;
                                    }
                                }
                            }
                        }

                        // For FIFO Valuation
                        if (onhand > 0 && !isLifo) {
                            for (int i = qty.size(); i > 0; i--) {
                                isProductSold = true;
                                if (totalQty >= qty.get(i - 1) && totalQty != 0) {
                                    JSONObject obj = new JSONObject();
                                    double tempRate = rate.get(i - 1);
                                    Date transactionDate = date.get(i - 1);
                                    double baseuomQty = qty.get(i - 1) * baseuomrate.get(i - 1);

                                    KwlReturnObject crresult = accCurrencyDAOobj.getCurrencyToBaseAmount(currencyParams, tempRate, currency.get(i - 1), transactionDate, 0);
                                    tempRate = (Double) crresult.getEntityList().get(0);

                                    lifo = (baseuomQty * tempRate);
                                    List data = new ArrayList();
                                    data.add(sno);
                                    data.add(product != null ? product.getName() : "");
                                    data.add(transactionNumber.get(i-1));
                                    data.add(formatter.format(transactionDate));
                                    data.add(transactionType.get(i-1));
                                    data.add(authHandler.formattedQuantity(baseuomQty, companyId));
                               
                                    totalQty = totalQty - baseuomQty;
                                    if (dbformat.format(transactionDate).equals(lastYearDate)) {
                                        finalData.add(data);
                                        sno++;
                                    }

                                } else if (totalQty < qty.get(i - 1) && totalQty != 0) {
                                    JSONObject obj = new JSONObject();
                                    double tempRate = rate.get(i - 1);
                                    Date transactionDate = date.get(i - 1);

                                    KwlReturnObject crresult = accCurrencyDAOobj.getCurrencyToBaseAmount(currencyParams, tempRate, currency.get(i - 1), transactionDate, 0);
                                    tempRate = (Double) crresult.getEntityList().get(0);

                                    lifo = totalQty * tempRate;
                                    List data = new ArrayList();
                                    data.add(sno);
                                    data.add(product != null ? product.getName() : "");
                                    data.add(transactionNumber.get(i-1));
                                    data.add(formatter.format(transactionDate));
                                    data.add(transactionType.get(i-1));
                                    data.add(authHandler.formattedQuantity(totalQty, companyId));
                                    totalQty = 0;
                                    if (dbformat.format(transactionDate).equals(lastYearDate)) {
                                        finalData.add(data);
                                         sno++;
                                    }
                                }
                            }
                        }

                    }

                if (isProductSold) {
                    for (Object header : headerItems) {
                        CustomDesignLineItemProp headerprop = new CustomDesignLineItemProp();
                        String a = header.toString();
                        headerprop.setAlign("left");
                        headerprop.setData(a);
                        headerprop.setWidth("50px");
                        headerlist.add(headerprop);
                    }
                    List finalProductList = new ArrayList();
                    for (Object headerdata : finalData) {
                        ArrayList<CustomDesignLineItemProp> prodlist = new ArrayList();
                        List datalist = (List) headerdata;
                        for (Object hdata : datalist) {
                            CustomDesignLineItemProp prop = new CustomDesignLineItemProp();
                            prop.setAlign("left");
                            prop.setData(hdata.toString());
                            prodlist.add(prop);
                        }
                        finalProductList.add(prodlist);
                    }
                    String top = "10px", left = "10px", tablewidth = CustomDesignHandler.pageWidth;
                    StringWriter writer = new StringWriter();
                    VelocityEngine ve = new VelocityEngine();
                    ve.init();
                    VelocityContext context = new VelocityContext();
                    context.put("tableHeader", headerlist);
                    context.put("prodList", finalProductList);
                    context.put("top", top);
                    context.put("left", left);
                    context.put("width", tablewidth);
                    velocityEngine.mergeTemplate("duemailitems.vm", "UTF-8", context, writer);
                    String tablehtml = writer.toString();
                    htmlText = htmlText.concat(tablehtml);
                    htmlText+=baseurl;
                    Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                    SendMailHandler.postMail(userMailid, subject, htmlText, plainMsg, from, smtpConfigMap);
                }
//            }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
        
        public void sendGoodsReceiptOrderMail(String dbDuedate, String simpleDuedate, String companyId, String companyName, String baseCurrencyId, String userFname,
                String userLname, String[]  userMailid, String mailContent, String mailSubject, NotificationRules nr,String baseurl) throws FileNotFoundException, IOException, DocumentException, ServiceException, JSONException {
        KwlReturnObject kwlGRO = null;
        try {
            String groNumber, vendorName;
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            GoodsReceiptOrder goodsReceiptOrder=null;
            Date todayDate = new Date();
            DateFormat formatter;
            formatter = new SimpleDateFormat("dd-MM-yyyy");
            String today = formatter.format(todayDate);

            List headerItems = null;
            List finalData = new ArrayList();
            ArrayList<CustomDesignLineItemProp> headerlist = new ArrayList();
            requestParams.put("gcurrencyid", baseCurrencyId);
            requestParams.put("companyid", companyId);
            headerItems = getHeaderData(false,Constants.Acc_Goods_Receipt_ModuleId);
            if (userMailid.length>0) {
                boolean isGoodsReceiptOrder = false;
                int sno = 1;
                String htmlText = "";
                String subject = mailSubject;
                String plainMsg = "";
                //String from = Constants.ADMIN_EMAILID;
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
                Company company = (Company) returnObject.getEntityList().get(0);
                String from = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                htmlText = mailContent.replace(Constants.DateLabel_MailContent_Placeholder, simpleDuedate);
                htmlText = htmlText.replace(Constants.TodayDateLabel_MailContent_Placeholder, today);
                
                KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyId);
                CompanyAccountPreferences pref = (CompanyAccountPreferences) cap.getEntityList().get(0);
                String cashAccount = pref.getCashAccount().getID();
                
                kwlGRO = accountingHandlerDAOobj.getGRODateGoodsReceiptOrderList(companyId, dbDuedate);
                List list = kwlGRO.getEntityList();
                Iterator iterator = list.iterator();
                
                while (iterator.hasNext()) {
                    List data = new ArrayList();
                    goodsReceiptOrder = (GoodsReceiptOrder) iterator.next();
                    if (goodsReceiptOrder != null) {                            
                        isGoodsReceiptOrder = true;
                        groNumber = goodsReceiptOrder.getGoodsReceiptOrderNumber();
                        Date orderDate = (Date) goodsReceiptOrder.getOrderDate();
                        vendorName = goodsReceiptOrder.getVendor().getName();

                        String mainHtml = "";
                        String templateid = !StringUtil.isNullOrEmpty(nr.getTemplateid()) ? nr.getTemplateid() : "";
                        if (!StringUtil.isNullOrEmpty(templateid) && !Constants.NONE.equalsIgnoreCase(templateid)) {
                            JSONObject requestObj = new JSONObject();
                            requestObj.put(Constants.moduleid, Constants.Acc_Goods_Receipt_ModuleId);
                            requestObj.put(Constants.isdefaultHeaderMap, true);
                            requestObj.put(Constants.companyKey, companyId);
                            requestObj.put(Constants.TEMPLATEID_KEY, templateid);
                            requestObj.put(Constants.billid, goodsReceiptOrder.getID());
                            requestObj.put(Constants.userid, (goodsReceiptOrder != null && goodsReceiptOrder.getCreatedby() != null) ? goodsReceiptOrder.getCreatedby().getUserID() : "");

                            StringBuilder appendString = new StringBuilder();
                            String resturl = accDocumentDesignService.PrintTemplateRestUrl(requestObj);
                            String templateurl = "<a href='" + resturl + "' target='_blank'>" + groNumber + "</a>";

                            appendString.append(templateurl);
                            mainHtml = appendString.toString();
                        }

                        data.add(sno);
                        data.add(!StringUtil.isNullOrEmpty(mainHtml) ? mainHtml : groNumber);
                        data.add(vendorName);
                        data.add(formatter.format(orderDate));
                        finalData.add(data);
                        sno++;
                    }
                }               
                if (isGoodsReceiptOrder) {
                    for (Object header : headerItems) {
                        CustomDesignLineItemProp headerprop = new CustomDesignLineItemProp();
                        String a = header.toString();
                        headerprop.setAlign("left");
                        headerprop.setData(a);
                        headerprop.setWidth("50px");
                        headerlist.add(headerprop);
                    }
                    List finalProductList = new ArrayList();
                    for (Object headerdata : finalData) {
                        ArrayList<CustomDesignLineItemProp> prodlist = new ArrayList();
                        List datalist = (List) headerdata;
                        for (Object hdata : datalist) {
                            CustomDesignLineItemProp prop = new CustomDesignLineItemProp();
                            prop.setAlign("left");
                            prop.setData(hdata.toString());
                            prodlist.add(prop);
                        }
                        finalProductList.add(prodlist);
                    }
                    String top = "10px", left = "10px", tablewidth = CustomDesignHandler.pageWidth;
                    StringWriter writer = new StringWriter();
                    VelocityEngine ve = new VelocityEngine();
                    ve.init();
                    VelocityContext context = new VelocityContext();
                    context.put("tableHeader", headerlist);
                    context.put("prodList", finalProductList);
                    context.put("top", top);
                    context.put("left", left);
                    context.put("width", tablewidth);
                    velocityEngine.mergeTemplate("duemailitems.vm", "UTF-8", context, writer);
                    String tablehtml = writer.toString();
                    htmlText = htmlText.concat(tablehtml);
                    htmlText+=baseurl;
                    Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                    SendMailHandler.postMail(userMailid, subject, htmlText, plainMsg, from, smtpConfigMap);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
        public void sendSalesReturnMail(String dbDuedate, String simpleDuedate, String companyId, String companyName, String baseCurrencyId, String userFname,
                String userLname, String[]  userMailid, String mailContent, String mailSubject, NotificationRules nr,String baseurl) throws FileNotFoundException, IOException, DocumentException, ServiceException, JSONException {
        KwlReturnObject kwlSR = null;
        try {
            String srNumber, customerName;
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            SalesReturn salesReturn=null;
            Date todayDate = new Date();
            DateFormat formatter;
            formatter = new SimpleDateFormat("dd-MM-yyyy");
            String today = formatter.format(todayDate);

            List headerItems = null;
            List finalData = new ArrayList();
            ArrayList<CustomDesignLineItemProp> headerlist = new ArrayList();
            requestParams.put("gcurrencyid", baseCurrencyId);
            requestParams.put("companyid", companyId);
            headerItems = getHeaderData(true,Constants.Acc_Sales_Return_ModuleId);
            if (userMailid.length>0) {
                boolean isSalesReturn = false;
                int sno = 1;
                String htmlText = "";
                String subject = mailSubject;
                String plainMsg = "";
                //String from = Constants.ADMIN_EMAILID;
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
                Company company = (Company) returnObject.getEntityList().get(0);
                String from = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                htmlText = mailContent.replace(Constants.DateLabel_MailContent_Placeholder, simpleDuedate);
                htmlText = htmlText.replace(Constants.TodayDateLabel_MailContent_Placeholder, today);
                
                KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyId);
                CompanyAccountPreferences pref = (CompanyAccountPreferences) cap.getEntityList().get(0);
                String cashAccount = pref.getCashAccount().getID();
                
                kwlSR = accountingHandlerDAOobj.getSRDateSalesReturnList(companyId, dbDuedate);
                List list = kwlSR.getEntityList();
                Iterator iterator = list.iterator();
                
                while (iterator.hasNext()) {
                    List data = new ArrayList();
                    salesReturn = (SalesReturn) iterator.next();
                    if (salesReturn != null) {                            
                        isSalesReturn = true;
                        srNumber = salesReturn.getSalesReturnNumber();
                        Date orderDate = (Date) salesReturn.getOrderDate();
                        customerName = salesReturn.getCustomer().getName();

                        String mainHtml = "";
                        String templateid = !StringUtil.isNullOrEmpty(nr.getTemplateid()) ? nr.getTemplateid() : "";
                        if (!StringUtil.isNullOrEmpty(templateid) && !Constants.NONE.equalsIgnoreCase(templateid)) {
                            JSONObject requestObj = new JSONObject();
                            requestObj.put(Constants.moduleid, Constants.Acc_Sales_Return_ModuleId);
                            requestObj.put(Constants.isdefaultHeaderMap, true);
                            requestObj.put(Constants.companyKey, companyId);
                            requestObj.put(Constants.TEMPLATEID_KEY, templateid);
                            requestObj.put(Constants.billid, salesReturn.getID());
                            requestObj.put(Constants.userid, (salesReturn != null && salesReturn.getCreatedby() != null) ? salesReturn.getCreatedby().getUserID() : "");

                            StringBuilder appendString = new StringBuilder();
                            String resturl = accDocumentDesignService.PrintTemplateRestUrl(requestObj);
                            String templateurl = "<a href='" + resturl + "' target='_blank'>" + srNumber + "</a>";

                            appendString.append(templateurl);
                            mainHtml = appendString.toString();
                        }

                        data.add(sno);
                        data.add(!StringUtil.isNullOrEmpty(mainHtml) ? mainHtml : srNumber);
                        data.add(customerName);
                        data.add(formatter.format(orderDate));
                        finalData.add(data);
                        sno++;
                    }
                }
                if (isSalesReturn) {
                    for (Object header : headerItems) {
                        CustomDesignLineItemProp headerprop = new CustomDesignLineItemProp();
                        String a = header.toString();
                        headerprop.setAlign("left");
                        headerprop.setData(a);
                        headerprop.setWidth("50px");
                        headerlist.add(headerprop);
                    }
                    List finalProductList = new ArrayList();
                    for (Object headerdata : finalData) {
                        ArrayList<CustomDesignLineItemProp> prodlist = new ArrayList();
                        List datalist = (List) headerdata;
                        for (Object hdata : datalist) {
                            CustomDesignLineItemProp prop = new CustomDesignLineItemProp();
                            prop.setAlign("left");
                            prop.setData(hdata.toString());
                            prodlist.add(prop);
                        }
                        finalProductList.add(prodlist);
                    }
                    String top = "10px", left = "10px", tablewidth = CustomDesignHandler.pageWidth;
                    StringWriter writer = new StringWriter();
                    VelocityEngine ve = new VelocityEngine();
                    ve.init();
                    VelocityContext context = new VelocityContext();
                    context.put("tableHeader", headerlist);
                    context.put("prodList", finalProductList);
                    context.put("top", top);
                    context.put("left", left);
                    context.put("width", tablewidth);
                    velocityEngine.mergeTemplate("duemailitems.vm", "UTF-8", context, writer);
                    String tablehtml = writer.toString();
                    htmlText = htmlText.concat(tablehtml);
                    htmlText+=baseurl;
                    Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                    SendMailHandler.postMail(userMailid, subject, htmlText, plainMsg, from, smtpConfigMap);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
          public void sendPurchaseReturnMail(String dbDuedate, String simpleDuedate, String companyId, String companyName, String baseCurrencyId, String userFname,
                String userLname, String[]  userMailid, String mailContent, String mailSubject, NotificationRules nr,String baseurl) throws FileNotFoundException, IOException, DocumentException, ServiceException, JSONException {
        KwlReturnObject kwlPR = null;
        try {
            String prNumber, vendorName;
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            PurchaseReturn purchaseReturn=null;
            Date todayDate = new Date();
            DateFormat formatter;
            formatter = new SimpleDateFormat("dd-MM-yyyy");
            String today = formatter.format(todayDate);

            List headerItems = null;
            List finalData = new ArrayList();
            ArrayList<CustomDesignLineItemProp> headerlist = new ArrayList();
            requestParams.put("gcurrencyid", baseCurrencyId);
            requestParams.put("companyid", companyId);
            headerItems = getHeaderData(false,Constants.Acc_Purchase_Return_ModuleId);
            if (userMailid.length>0) {
                boolean isPurchaseReturn = false;
                int sno = 1;
                String htmlText = "";
                String subject = mailSubject;
                String plainMsg = "";
                //String from = Constants.ADMIN_EMAILID;
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
                Company company = (Company) returnObject.getEntityList().get(0);
                String from = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                htmlText = mailContent.replace(Constants.DateLabel_MailContent_Placeholder, simpleDuedate);
                htmlText = htmlText.replace(Constants.TodayDateLabel_MailContent_Placeholder, today);
                
                KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyId);
                CompanyAccountPreferences pref = (CompanyAccountPreferences) cap.getEntityList().get(0);
                String cashAccount = pref.getCashAccount().getID();
                
                kwlPR = accountingHandlerDAOobj.getPRDatePurchaseReturnList(companyId, dbDuedate);
                List list = kwlPR.getEntityList();
                Iterator iterator = list.iterator();
                
                while (iterator.hasNext()) {
                    List data = new ArrayList();
                    purchaseReturn = (PurchaseReturn) iterator.next();
                    if (purchaseReturn != null) {                            
                        isPurchaseReturn = true;
                        prNumber = purchaseReturn.getPurchaseReturnNumber();
                        Date orderDate = (Date) purchaseReturn.getOrderDate();
                        vendorName = purchaseReturn.getVendor().getName();

                        String mainHtml = "";
                        String templateid = !StringUtil.isNullOrEmpty(nr.getTemplateid()) ? nr.getTemplateid() : "";
                        if (!StringUtil.isNullOrEmpty(templateid) && !Constants.NONE.equalsIgnoreCase(templateid)) {
                            JSONObject requestObj = new JSONObject();
                            requestObj.put(Constants.moduleid, Constants.Acc_Purchase_Return_ModuleId);
                            requestObj.put(Constants.isdefaultHeaderMap, true);
                            requestObj.put(Constants.companyKey, companyId);
                            requestObj.put(Constants.TEMPLATEID_KEY, templateid);
                            requestObj.put(Constants.billid, purchaseReturn.getID());
                            requestObj.put(Constants.userid, (purchaseReturn != null && purchaseReturn.getCreatedby() != null) ? purchaseReturn.getCreatedby().getUserID() : "");

                            StringBuilder appendString = new StringBuilder();
                            String resturl = accDocumentDesignService.PrintTemplateRestUrl(requestObj);
                            String templateurl = "<a href='" + resturl + "' target='_blank'>" + prNumber + "</a>";

                            appendString.append(templateurl);
                            mainHtml = appendString.toString();
                        }

                        data.add(sno);
                        data.add(!StringUtil.isNullOrEmpty(mainHtml) ? mainHtml : prNumber);
                        data.add(vendorName);
                        data.add(formatter.format(orderDate));
                        finalData.add(data);
                        sno++;
                    }
                }               
                if (isPurchaseReturn) {
                    for (Object header : headerItems) {
                        CustomDesignLineItemProp headerprop = new CustomDesignLineItemProp();
                        String a = header.toString();
                        headerprop.setAlign("left");
                        headerprop.setData(a);
                        headerprop.setWidth("50px");
                        headerlist.add(headerprop);
                    }
                    List finalProductList = new ArrayList();
                    for (Object headerdata : finalData) {
                        ArrayList<CustomDesignLineItemProp> prodlist = new ArrayList();
                        List datalist = (List) headerdata;
                        for (Object hdata : datalist) {
                            CustomDesignLineItemProp prop = new CustomDesignLineItemProp();
                            prop.setAlign("left");
                            prop.setData(hdata.toString());
                            prodlist.add(prop);
                        }
                        finalProductList.add(prodlist);
                    }
                    String top = "10px", left = "10px", tablewidth = CustomDesignHandler.pageWidth;
                    StringWriter writer = new StringWriter();
                    VelocityEngine ve = new VelocityEngine();
                    ve.init();
                    VelocityContext context = new VelocityContext();
                    context.put("tableHeader", headerlist);
                    context.put("prodList", finalProductList);
                    context.put("top", top);
                    context.put("left", left);
                    context.put("width", tablewidth);
                    velocityEngine.mergeTemplate("duemailitems.vm", "UTF-8", context, writer);
                    String tablehtml = writer.toString();
                    htmlText = htmlText.concat(tablehtml);
                    htmlText+=baseurl;
                    Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                    SendMailHandler.postMail(userMailid, subject, htmlText, plainMsg, from, smtpConfigMap);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
        
      public void sendCustomerCreationMail(String dbDuedate, String simpleDuedate, String companyId, String companyName, String baseCurrencyId, String userFname,
                String userLname, String[]  userMailid, String mailContent, String mailSubject,String baseurl) throws FileNotFoundException, IOException, DocumentException, ServiceException, JSONException {
        KwlReturnObject kwlCustomer = null;
        try {
            String custCode, customerName;
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            Customer customer=null;
            Date todayDate = new Date();
            DateFormat formatter;
            formatter = new SimpleDateFormat("dd-MM-yyyy");
            String today = formatter.format(todayDate);

            List headerItems = null;
            List finalData = new ArrayList();
            ArrayList<CustomDesignLineItemProp> headerlist = new ArrayList();
            requestParams.put("gcurrencyid", baseCurrencyId);
            requestParams.put("companyid", companyId);
            headerItems = getHeaderData(true,Constants.Acc_Customer_ModuleId);
            if (userMailid.length>0) {
                boolean isCustomer = false;
                int sno = 1;
                String htmlText = "";
                String subject = mailSubject;
                String plainMsg = "";
                //String from = Constants.ADMIN_EMAILID;
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
                Company company = (Company) returnObject.getEntityList().get(0);
                String from = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                htmlText = mailContent.replace(Constants.DateLabel_MailContent_Placeholder, simpleDuedate);
                htmlText = htmlText.replace(Constants.TodayDateLabel_MailContent_Placeholder, today);
                
                KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyId);
                CompanyAccountPreferences pref = (CompanyAccountPreferences) cap.getEntityList().get(0);
                String cashAccount = pref.getCashAccount().getID();
                
                kwlCustomer = accountingHandlerDAOobj.getCustomerCreationDateCustomerList(companyId, dbDuedate);
                List list = kwlCustomer.getEntityList();
                Iterator iterator = list.iterator();
                
                while (iterator.hasNext()) {
                    List data = new ArrayList();
                    customer = (Customer) iterator.next();
                    if (customer != null) {                            
                          isCustomer = true;
                          custCode = customer.getAcccode();
                          Date creationDate = (Date) customer.getCreatedOn();
                          customerName = customer.getName();
                        
                          data.add(sno);
                          data.add(custCode);
                          data.add(customerName);
                          data.add(formatter.format(creationDate));
                          finalData.add(data);
                          sno++;
                    }
                }
                if (isCustomer) {
                    for (Object header : headerItems) {
                        CustomDesignLineItemProp headerprop = new CustomDesignLineItemProp();
                        String a = header.toString();
                        headerprop.setAlign("left");
                        headerprop.setData(a);
                        headerprop.setWidth("50px");
                        headerlist.add(headerprop);
                    }
                    List finalProductList = new ArrayList();
                    for (Object headerdata : finalData) {
                        ArrayList<CustomDesignLineItemProp> prodlist = new ArrayList();
                        List datalist = (List) headerdata;
                        for (Object hdata : datalist) {
                            CustomDesignLineItemProp prop = new CustomDesignLineItemProp();
                            prop.setAlign("left");
                            prop.setData(hdata.toString());
                            prodlist.add(prop);
                        }
                        finalProductList.add(prodlist);
                    }
                    String top = "10px", left = "10px", tablewidth = CustomDesignHandler.pageWidth;
                    StringWriter writer = new StringWriter();
                    VelocityEngine ve = new VelocityEngine();
                    ve.init();
                    VelocityContext context = new VelocityContext();
                    context.put("tableHeader", headerlist);
                    context.put("prodList", finalProductList);
                    context.put("top", top);
                    context.put("left", left);
                    context.put("width", tablewidth);
                    velocityEngine.mergeTemplate("duemailitems.vm", "UTF-8", context, writer);
                    String tablehtml = writer.toString();
                    htmlText = htmlText.concat(tablehtml);
                    htmlText+=baseurl;
                    Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                    SendMailHandler.postMail(userMailid, subject, htmlText, plainMsg, from, smtpConfigMap);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
        public void sendVendorCreationMail(String dbDuedate, String simpleDuedate, String companyId, String companyName, String baseCurrencyId, String userFname,
                String userLname, String[]  userMailid, String mailContent, String mailSubject,boolean isCreationDate ,String baseurl) throws FileNotFoundException, IOException, DocumentException, ServiceException, JSONException {
        KwlReturnObject kwlVendor = null;
        try {
            String vendCode, vendorName;
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            Vendor vendor=null;
            Date todayDate = new Date();
            DateFormat formatter;
            formatter = new SimpleDateFormat("dd-MM-yyyy");
            String today = formatter.format(todayDate);

            List headerItems = null;
            List finalData = new ArrayList();
            ArrayList<CustomDesignLineItemProp> headerlist = new ArrayList();
            requestParams.put("gcurrencyid", baseCurrencyId);
            requestParams.put("companyid", companyId);
            headerItems = getHeaderData(false,Constants.Acc_Vendor_ModuleId);
            if (isCreationDate) {
                headerItems.add("Creation Date");
            } else {
                headerItems.add("Self-Billed Approval Expiry Date");
            } 
            if (userMailid.length>0) {
                boolean isVendor = false;
                int sno = 1;
                String htmlText = "";
                String subject = mailSubject;
                String plainMsg = "";
                //String from = Constants.ADMIN_EMAILID;
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
                Company company = (Company) returnObject.getEntityList().get(0);
                String from = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                htmlText = mailContent.replace(Constants.DateLabel_MailContent_Placeholder, simpleDuedate);
                htmlText = htmlText.replace(Constants.TodayDateLabel_MailContent_Placeholder, today);
                
                KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyId);
                CompanyAccountPreferences pref = (CompanyAccountPreferences) cap.getEntityList().get(0);
                String cashAccount = pref.getCashAccount().getID();
                
                kwlVendor = accountingHandlerDAOobj.getVendorCreationDateVendorList(companyId, dbDuedate);
                List list = kwlVendor.getEntityList();
                Iterator iterator = list.iterator();
                
                while (iterator.hasNext()) {
                    List data = new ArrayList();
                    vendor = (Vendor) iterator.next();
                    if (vendor != null) {                            
                          isVendor = true;
                          vendCode = vendor.getAcccode();
                          Date creationDate = (Date) vendor.getCreatedOn();
                          Date selfBilledApprovalExpiryDate = (Date) vendor.getSelfBilledToDate();
                          vendorName = vendor.getName();
                        
                          data.add(sno);
                          data.add(vendCode);
                          data.add(vendorName);
                          data.add(formatter.format(isCreationDate?creationDate:selfBilledApprovalExpiryDate));
                          finalData.add(data);
                          sno++;
                    }
                }               
                if (isVendor) {
                    for (Object header : headerItems) {
                        CustomDesignLineItemProp headerprop = new CustomDesignLineItemProp();
                        String a = header.toString();
                        headerprop.setAlign("left");
                        headerprop.setData(a);
                        headerprop.setWidth("50px");
                        headerlist.add(headerprop);
                    }
                    List finalProductList = new ArrayList();
                    for (Object headerdata : finalData) {
                        ArrayList<CustomDesignLineItemProp> prodlist = new ArrayList();
                        List datalist = (List) headerdata;
                        for (Object hdata : datalist) {
                            CustomDesignLineItemProp prop = new CustomDesignLineItemProp();
                            prop.setAlign("left");
                            prop.setData(hdata.toString());
                            prodlist.add(prop);
                        }
                        finalProductList.add(prodlist);
                    }
                    String top = "10px", left = "10px", tablewidth = CustomDesignHandler.pageWidth;
                    StringWriter writer = new StringWriter();
                    VelocityEngine ve = new VelocityEngine();
                    ve.init();
                    VelocityContext context = new VelocityContext();
                    context.put("tableHeader", headerlist);
                    context.put("prodList", finalProductList);
                    context.put("top", top);
                    context.put("left", left);
                    context.put("width", tablewidth);
                    velocityEngine.mergeTemplate("duemailitems.vm", "UTF-8", context, writer);
                    String tablehtml = writer.toString();
                    htmlText = htmlText.concat(tablehtml);
                    htmlText+=baseurl;
                    Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                    SendMailHandler.postMail(userMailid, subject, htmlText, plainMsg, from, smtpConfigMap);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
        
    public void sendcontractExpiryNotificationMail(Date advdueDate, String simpleDuedate, String companyId, String companyName, String baseCurrencyId, String userFname,
            String userLname, List<String> userEmailList, String mailContent, String mailSubject, boolean isMailToSalesPerson,String baseurl) throws FileNotFoundException, IOException, DocumentException, ServiceException, JSONException {
        try {
            Calendar cldDueDate = Calendar.getInstance();
            cldDueDate.setTime(advdueDate);
            cldDueDate.set(Calendar.HOUR_OF_DAY, 00);
            cldDueDate.set(Calendar.MINUTE, 00);
            cldDueDate.set(Calendar.SECOND, 00);
            cldDueDate.set(Calendar.MILLISECOND, 00);
            
            Date todayDate = new Date();
            DateFormat formatter;
            formatter = new SimpleDateFormat("dd-MM-yyyy");
            String today = formatter.format(todayDate);
            
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", companyId);
            
            KwlReturnObject contractResult = accSalesOrderDAOobj.getContractOrders(requestParams);
            List<Contract> contractList = contractResult.getEntityList();
            
            //String from = Constants.ADMIN_EMAILID;
            KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
            Company company = (Company) returnObject.getEntityList().get(0);
            Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
            for(Contract contract : contractList) {
                List<String> FinalMailList = new ArrayList<String>();
                if(!StringUtil.isNullOrEmpty(contract.getSalesOrderNumber())) {
                    KwlReturnObject salesOrderObj = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), contract.getSalesOrderNumber());
                    SalesOrder salesOrder = (SalesOrder) salesOrderObj.getEntityList().get(0);

                    if (isMailToSalesPerson && salesOrder != null && salesOrder.getSalesperson() != null && !StringUtil.isNullOrEmpty(salesOrder.getSalesperson().getEmailID())) {
                        FinalMailList.add(salesOrder.getSalesperson().getEmailID());
                    }
                }

                FinalMailList.addAll(userEmailList);
                String[] userMailid = FinalMailList.toArray(new String[FinalMailList.size()]);
                
                if(userMailid.length > 0) {
                    List finalData = new ArrayList();
                    ArrayList<CustomDesignLineItemProp> headerlist = new ArrayList();

                    List headerItems = null;
                    headerItems = getHeaderData(true, Constants.Acc_Contract_Order_ModuleId);

                    int sno = 1;
                    String subject = mailSubject;
                    String plainMsg = "";
                    String htmlText = "";
                    htmlText = mailContent.replace(Constants.DateLabel_MailContent_Placeholder, simpleDuedate);

                    String from = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                    KwlReturnObject contractendate = accSalesOrderDAOobj.getContractStrtendDates(contract.getID());
                    List<Object[]> contractEndDateList = contractendate.getEntityList();

                    for (Object[] contractDateObj : contractEndDateList) {
                        Date endDate = (Date) contractDateObj[1];

                        Calendar cldEndDate = Calendar.getInstance();
                        cldEndDate.setTime(endDate);
                        cldEndDate.set(Calendar.HOUR_OF_DAY, 00);
                        cldEndDate.set(Calendar.MINUTE, 00);
                        cldEndDate.set(Calendar.SECOND, 00);
                        cldEndDate.set(Calendar.MILLISECOND, 00);

                        boolean isContract = false;

                        // To get only today's expired contracts
                        if (cldDueDate.equals(cldEndDate)) {
                            isContract = true;

                            String salesOrderID = contract.getSalesOrderNumber();
                            KwlReturnObject salesOrderObj = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), salesOrderID);
                            SalesOrder salesOrder = (SalesOrder) salesOrderObj.getEntityList().get(0);

                            Set<SalesOrderDetail> soDetails = salesOrder.getRows();
                            for (SalesOrderDetail soDetail : soDetails) {
                                List data = new ArrayList();
                                data.add(sno);
                                data.add(contract.getContractNumber());
                                data.add(contract.getCustomer().getName());
                                data.add(soDetail.getProduct().getName());
                                data.add(formatter.format(contract.getOrderDate()));
                                data.add(formatter.format(contract.getEndDate()));
                                sno++;

                                finalData.add(data);
                            }
                        }

                        if (isContract) {
                            for (Object header : headerItems) {
                                CustomDesignLineItemProp headerprop = new CustomDesignLineItemProp();
                                String a = header.toString();
                                headerprop.setAlign("left");
                                headerprop.setData(a);
                                headerprop.setWidth("50px");
                                headerlist.add(headerprop);
                            }
                            List finalProductList = new ArrayList();
                            for (Object headerdata : finalData) {
                                ArrayList<CustomDesignLineItemProp> prodlist = new ArrayList();
                                List datalist = (List) headerdata;
                                for (Object hdata : datalist) {
                                    CustomDesignLineItemProp prop = new CustomDesignLineItemProp();
                                    prop.setAlign("left");
                                    prop.setData(hdata.toString());
                                    prodlist.add(prop);
                                }
                                finalProductList.add(prodlist);
                            }
                            String top = "10px", left = "10px", tablewidth = CustomDesignHandler.pageWidth;
                            StringWriter writer = new StringWriter();
                            VelocityEngine ve = new VelocityEngine();
                            ve.init();
                            VelocityContext context = new VelocityContext();
                            context.put("tableHeader", headerlist);
                            context.put("prodList", finalProductList);
                            context.put("top", top);
                            context.put("left", left);
                            context.put("width", tablewidth);
                            velocityEngine.mergeTemplate("duemailitems.vm", "UTF-8", context, writer);
                            String tablehtml = writer.toString();
                            htmlText = htmlText.concat(tablehtml);
                            htmlText+=baseurl;
                            SendMailHandler.postMail(userMailid, subject, htmlText, plainMsg, from, smtpConfigMap);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void setContractExpiryStatus(String companyId) throws ServiceException {
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", companyId);

            // To set contract expiry status for todays expiring Contract
            KwlReturnObject activeContractsResult = accSalesOrderDAOobj.getActiveContracts(requestParams);
            List<String> activeContractList = activeContractsResult.getEntityList();
            
            if (activeContractList != null && !activeContractList.isEmpty()) {
                Calendar cldCurrentDate = Calendar.getInstance();
                cldCurrentDate.set(Calendar.HOUR_OF_DAY, 00);
                cldCurrentDate.set(Calendar.MINUTE, 00);
                cldCurrentDate.set(Calendar.SECOND, 00);
                cldCurrentDate.set(Calendar.MILLISECOND, 00);
                String contractIDs = "";

                for (String contractID : activeContractList) {
                    KwlReturnObject contractObj = accountingHandlerDAOobj.getObject(Contract.class.getName(), contractID);
                    Contract contract = (Contract) contractObj.getEntityList().get(0);

                    if (contract != null) {
                        KwlReturnObject contractendate = accSalesOrderDAOobj.getContractStrtendDates(contract.getID());
                        List<Object[]> contractEndDateList = contractendate.getEntityList();

                        for (Object[] contractDateObj : contractEndDateList) {
                            Date endDate = (Date) contractDateObj[1];
                            
                            Calendar cldEndDate = Calendar.getInstance();
                            cldEndDate.setTime(endDate);
                            cldEndDate.set(Calendar.HOUR_OF_DAY, 00);
                            cldEndDate.set(Calendar.MINUTE, 00);
                            cldEndDate.set(Calendar.SECOND, 00);
                            cldEndDate.set(Calendar.MILLISECOND, 00);

                            // To get only today's expired contracts
                            if (cldCurrentDate.after(cldEndDate) || cldCurrentDate.equals(cldEndDate)) {
                                // contractIDs used for set listed contracts status as expired
                                if (contractIDs.length() > 1) {
                                    contractIDs += "," + contract.getID();
                                } else {
                                    contractIDs = contract.getID();
                                }
                            }
                        }

                    }

                }

                // For Expire the listed contracts in contractIDs
                if (contractIDs.length() > 1) {
                    accSalesOrderDAOobj.setContractExpiryStatus(contractIDs, companyId);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    //For Line level Date fields
       
     private void sendPurchaseOrderDetailLineCustomDateFieldMails(String fieldLabel, String colField, int colnum, String companyId, Date advdueDate1, Date advdueDate2, 
          String[] userMailID, DateFormat simpleformat, String simpleDuedate, String mailContent, String mailSubject,String baseurl) {
        try {
        List finalData = new ArrayList();
        int sno=0;
        List headerItems = new ArrayList();
        headerItems.add("S.No.");
        headerItems.add("Purchase Order Number");
        headerItems.add("Purchase Order Date");
        headerItems.add("Product");
        headerItems.add(fieldLabel);
        
        HashMap<String, Object> poDetailRequestParams = new HashMap<String, Object>();
        ArrayList Detailfilter_names = new ArrayList(),Detailfilter_params = new ArrayList();
        Detailfilter_names.add("po.company.companyID");
        Detailfilter_params.add(companyId);
        if (!StringUtil.isNullOrEmpty(colField)) {
            Detailfilter_names.add(">=podcustom.".concat(colField));
            Detailfilter_params.add(advdueDate1);
            Detailfilter_names.add("<=podcustom.".concat(colField));
            Detailfilter_params.add(advdueDate2);
        }
        poDetailRequestParams.put("filter_names", Detailfilter_names);
        poDetailRequestParams.put("filter_params", Detailfilter_params);
        KwlReturnObject idcustresult = accountingHandlerDAOobj.getPurchaseOrderDetailLineCustomDateFieldMails(poDetailRequestParams);
        if(idcustresult.getEntityList().size()>0) {
            List<PurchaseOrderDetail> poList = idcustresult.getEntityList();
            for(PurchaseOrderDetail pod : poList) {
                List data = new ArrayList();
                if (pod  != null) {
                    AccCustomData customData =  pod.getPoDetailCustomData();
//                    long value = 0;
                    String value="";
                    if(colnum!=0) {
                         value =simpleformat.format(convertCustomStringDateToDateObject(customData.getCol(colnum)));
                    }
                    data.add(++sno);
                    data.add(pod.getPurchaseOrder().getPurchaseOrderNumber());
                    data.add(simpleformat.format(pod.getPurchaseOrder().getOrderDate()));
                    data.add(pod.getProduct().getName());
                    data.add(value);
                    finalData.add(data);
                }
            }
            sendMail(finalData, headerItems, simpleformat, companyId, simpleDuedate, userMailID, mailContent, mailSubject,baseurl);
        }
      } catch(Exception ex) {
          ex.printStackTrace();
      }
     }
     
     private void sendDeliveryOrderDetailLineCustomDateFieldMails(String fieldLabel, String colField, int colnum, String companyId, Date advdueDate1, Date advdueDate2, 
          String[] userMailID, DateFormat simpleformat, String simpleDuedate, String mailContent, String mailSubject,String baseurl) {
         try {
        List finalData = new ArrayList();
        int sno=0;
        List headerItems = new ArrayList();
        headerItems.add("S.No.");
        headerItems.add("Delivery Order Number");
        headerItems.add("Delivery Order Date");
        headerItems.add("Product");
        headerItems.add(fieldLabel);
        
        HashMap<String, Object> doDetailRequestParams = new HashMap<String, Object>();
        ArrayList Detailfilter_names = new ArrayList(),Detailfilter_params = new ArrayList();
        Detailfilter_names.add("do.company.companyID");
        Detailfilter_params.add(companyId);
        if (!StringUtil.isNullOrEmpty(colField)) {
            Detailfilter_names.add(">=dodcustom.".concat(colField));
            Detailfilter_params.add(advdueDate1);
            Detailfilter_names.add("<=dodcustom.".concat(colField));
            Detailfilter_params.add(advdueDate2);
        }
        doDetailRequestParams.put("filter_names", Detailfilter_names);
        doDetailRequestParams.put("filter_params", Detailfilter_params);
        KwlReturnObject idcustresult = accountingHandlerDAOobj.getDeliveryOrderDetailLineCustomDateFieldMails(doDetailRequestParams);
        if(idcustresult.getEntityList().size()>0) {
            List<DeliveryOrderDetail> doList = idcustresult.getEntityList();
            for(DeliveryOrderDetail dod : doList) {
                List data = new ArrayList();
                if (dod  != null) {
                    AccCustomData customData =  dod.getDeliveryOrderDetailCustomData();
//                    long value = 0;
                    String value="";
                    if(colnum!=0) {
                          value = simpleformat.format(convertCustomStringDateToDateObject(customData.getCol(colnum)));
                    }
                    data.add(++sno);
                    data.add(dod.getDeliveryOrder().getDeliveryOrderNumber());
                    data.add(simpleformat.format(dod.getDeliveryOrder().getOrderDate()));
                    data.add(dod.getProduct().getName());
                    data.add(value);
                    finalData.add(data);
                }
            }
            sendMail(finalData, headerItems, simpleformat, companyId, simpleDuedate, userMailID, mailContent, mailSubject,baseurl);
        }
      } catch(Exception ex) {
          ex.printStackTrace();
      }
     } 
             
       private void sendGoodsReceiptOrderDetailLineCustomDateFieldMails(String fieldLabel, String colField, int colnum, String companyId, Date advdueDate1, Date advdueDate2, 
          String[] userMailID, DateFormat simpleformat, String simpleDuedate, String mailContent, String mailSubject,String baseurl) {
         try {
        List finalData = new ArrayList();
        int sno=0;
        List headerItems = new ArrayList();
        headerItems.add("S.No.");
        headerItems.add("GR Order Number");
        headerItems.add("GR Order Date");
        headerItems.add("Product");
        headerItems.add(fieldLabel);
        
        HashMap<String, Object> groDetailRequestParams = new HashMap<String, Object>();
        ArrayList Detailfilter_names = new ArrayList(),Detailfilter_params = new ArrayList();
        Detailfilter_names.add("grod.company.companyID");
        Detailfilter_params.add(companyId);
        if (!StringUtil.isNullOrEmpty(colField)) {
            Detailfilter_names.add(">=grodcustom.".concat(colField));
            Detailfilter_params.add(advdueDate1);
            Detailfilter_names.add("<=grodcustom.".concat(colField));
            Detailfilter_params.add(advdueDate2);
        }
        groDetailRequestParams.put("filter_names", Detailfilter_names);
        groDetailRequestParams.put("filter_params", Detailfilter_params);
        KwlReturnObject idcustresult = accountingHandlerDAOobj.getGoodsReceiptOrderDetailLineCustomDateFieldMails(groDetailRequestParams);
        if(idcustresult.getEntityList().size()>0) {
            List<GoodsReceiptOrderDetails> groList = idcustresult.getEntityList();
            for(GoodsReceiptOrderDetails grod : groList) {
                List data = new ArrayList();
                if (grod  != null) {
                    AccCustomData customData =  grod.getGoodsReceiptOrderDetailsCustomDate();
//                    long value = 0;
                    String value="";
                    if(colnum!=0) {
                         value = simpleformat.format(convertCustomStringDateToDateObject(customData.getCol(colnum)));
                    }
                    data.add(++sno);
                    data.add(grod.getGrOrder().getGoodsReceiptOrderNumber());
                    data.add(simpleformat.format(grod.getGrOrder().getOrderDate()));
                    data.add(grod.getProduct().getName());
                    data.add(value);
                    finalData.add(data);
                }
            }
            sendMail(finalData, headerItems, simpleformat, companyId, simpleDuedate, userMailID, mailContent, mailSubject,baseurl);
        }
      } catch(Exception ex) {
          ex.printStackTrace();
      }
     } 
     
       private void sendSalesReturnDetailLineCustomDateFieldMails(String fieldLabel, String colField, int colnum, String companyId, Date advdueDate1, Date advdueDate2, 
          String[] userMailID, DateFormat simpleformat, String simpleDuedate, String mailContent, String mailSubject,String baseurl) {
         try {
        List finalData = new ArrayList();
        int sno=0;
        List headerItems = new ArrayList();
        headerItems.add("S.No.");
        headerItems.add("Sales Return Number");
        headerItems.add("Sales Return  Date");
        headerItems.add("Product");
        headerItems.add(fieldLabel);
        
        HashMap<String, Object> srDetailRequestParams = new HashMap<String, Object>();
        ArrayList Detailfilter_names = new ArrayList(),Detailfilter_params = new ArrayList();
        Detailfilter_names.add("srd.company.companyID");
        Detailfilter_params.add(companyId);
        if (!StringUtil.isNullOrEmpty(colField)) {
            Detailfilter_names.add(">=srdcustom.".concat(colField));
            Detailfilter_params.add(advdueDate1);
            Detailfilter_names.add("<=srdcustom.".concat(colField));
            Detailfilter_params.add(advdueDate2);
        }
        srDetailRequestParams.put("filter_names", Detailfilter_names);
        srDetailRequestParams.put("filter_params", Detailfilter_params);
        KwlReturnObject idcustresult = accountingHandlerDAOobj.getSalesReturnDetailLineCustomDateFieldMails(srDetailRequestParams);
        if(idcustresult.getEntityList().size()>0) {
            List<SalesReturnDetail> srList = idcustresult.getEntityList();
            for(SalesReturnDetail srd : srList) {
                List data = new ArrayList();
                if (srd  != null) {
                    AccCustomData customData =  srd.getSalesReturnDetailCustomData();
//                    long value = 0;
                    String value="";
                    if(colnum!=0) {
                        value = simpleformat.format(convertCustomStringDateToDateObject(customData.getCol(colnum)));
                    }
                    data.add(++sno);
                    data.add(srd.getSalesReturn().getSalesReturnNumber());
                    data.add(simpleformat.format(srd.getSalesReturn().getOrderDate()));
                    data.add(srd.getProduct().getName());
                    data.add(value);
                    finalData.add(data);
                }
            }
            sendMail(finalData, headerItems, simpleformat, companyId, simpleDuedate, userMailID, mailContent, mailSubject,baseurl);
        }
      } catch(Exception ex) {
          ex.printStackTrace();
      }
     }
             
     private void sendPurchaseReturnDetailLineCustomDateFieldMails(String fieldLabel, String colField, int colnum, String companyId, Date advdueDate1, Date advdueDate2, 
          String[] userMailID, DateFormat simpleformat, String simpleDuedate, String mailContent, String mailSubject,String baseurl) {
         try {
        List finalData = new ArrayList();
        int sno=0;
        List headerItems = new ArrayList();
        headerItems.add("S.No.");
        headerItems.add("Purchase Return Number");
        headerItems.add("Purchase Return  Date");
        headerItems.add("Product");
        headerItems.add(fieldLabel);
        
        HashMap<String, Object> prDetailRequestParams = new HashMap<String, Object>();
        ArrayList Detailfilter_names = new ArrayList(),Detailfilter_params = new ArrayList();
        Detailfilter_names.add("prd.company.companyID");
        Detailfilter_params.add(companyId);
        if (!StringUtil.isNullOrEmpty(colField)) {
            Detailfilter_names.add(">=prdcustom.".concat(colField));
            Detailfilter_params.add(advdueDate1);
            Detailfilter_names.add("<=prdcustom.".concat(colField));
            Detailfilter_params.add(advdueDate2);
        }
        prDetailRequestParams.put("filter_names", Detailfilter_names);
        prDetailRequestParams.put("filter_params", Detailfilter_params);
        KwlReturnObject idcustresult = accountingHandlerDAOobj.getPurchaseReturnDetailLineCustomDateFieldMails(prDetailRequestParams);
        if(idcustresult.getEntityList().size()>0) {
            List<PurchaseReturnDetail> prList = idcustresult.getEntityList();
            for(PurchaseReturnDetail prd : prList) {
                List data = new ArrayList();
                if (prd  != null) {
                    AccCustomData customData =  prd.getPurchaseReturnDetailCustomDate();
//                    long value = 0;
                    String value="";
                    if(colnum!=0) {
                        value =simpleformat.format(convertCustomStringDateToDateObject(customData.getCol(colnum)));                        
                    }
                    data.add(++sno);
                    data.add(prd.getPurchaseReturn().getPurchaseReturnNumber());
                    data.add(simpleformat.format(prd.getPurchaseReturn().getOrderDate()));
                    data.add(prd.getProduct().getName());
                    data.add(value);
                    finalData.add(data);
                }
            }
            sendMail(finalData, headerItems, simpleformat, companyId, simpleDuedate, userMailID, mailContent, mailSubject,baseurl);
        }
      } catch(Exception ex) {
          ex.printStackTrace();
      }
     }
        
     // For custom date field
     private void sendPurchaseOrderCustomDateFieldMails(String fieldLabel, String colField, int colnum, String companyId, Date advdueDate1, Date advdueDate2, 
          String[] userMailID, DateFormat simpleformat, String simpleDuedate, String mailContent, String mailSubject,String baseurl) {
        try {
            List finalData = new ArrayList();
            int sno=0;
            List headerItems = new ArrayList();
            headerItems.add("S.No.");
            headerItems.add("Purchase Order Number");
            headerItems.add("Creation Date");
            headerItems.add(fieldLabel);

            HashMap<String, Object> poDetailRequestParams = new HashMap<String, Object>();
            ArrayList Detailfilter_names = new ArrayList(),Detailfilter_params = new ArrayList();
            Detailfilter_names.add("po.company.companyID");
            Detailfilter_params.add(companyId);
            if (!StringUtil.isNullOrEmpty(colField)) {
                Detailfilter_names.add(">=pocustom.".concat(colField));
                Detailfilter_params.add(advdueDate1);
                Detailfilter_names.add("<=pocustom.".concat(colField));
                Detailfilter_params.add(advdueDate2);
            }
            poDetailRequestParams.put("filter_names", Detailfilter_names);
            poDetailRequestParams.put("filter_params", Detailfilter_params);
            KwlReturnObject idcustresult = accountingHandlerDAOobj.getPurchaseOrderCustomFields(poDetailRequestParams);
            if(idcustresult.getEntityList().size()>0) {
                List<PurchaseOrder> poList = idcustresult.getEntityList();
                for(PurchaseOrder purchaseOrder : poList) {
                    List data = new ArrayList();
                    if (purchaseOrder != null) {
                        AccCustomData customData =  purchaseOrder.getPoCustomData();
//                        long value = 0;
                        String value="";
                        if(colnum!=0) {
                             value = simpleformat.format(convertCustomStringDateToDateObject(customData.getCol(colnum)));                             
                        }
                        data.add(++sno);
                        data.add(purchaseOrder.getPurchaseOrderNumber());
                        data.add(simpleformat.format(new Date(purchaseOrder.getCreatedon())));
                        data.add(value);
                        finalData.add(data);
                    }
                }
                sendMail(finalData, headerItems, simpleformat, companyId, simpleDuedate, userMailID, mailContent, mailSubject,baseurl);
        }

      } catch(Exception ex) {
          ex.printStackTrace();
      }
     }     
     
     private void sendDeliveryOrderCustomDateFieldMails(String fieldLabel, String colField, int colnum, String companyId, Date advdueDate1, Date advdueDate2, 
          String[] userMailID, DateFormat simpleformat, String simpleDuedate, String mailContent, String mailSubject,String baseurl) {
         try {
            List finalData = new ArrayList();
            int sno=0;
            List headerItems = new ArrayList();
            headerItems.add("S.No.");
            headerItems.add("Delivery Order Number");
            headerItems.add("Creation Date");
            headerItems.add(fieldLabel);

            HashMap<String, Object> doDetailRequestParams = new HashMap<String, Object>();
            ArrayList Detailfilter_names = new ArrayList(),Detailfilter_params = new ArrayList();
            Detailfilter_names.add("do.company.companyID");
            Detailfilter_params.add(companyId);
            if (!StringUtil.isNullOrEmpty(colField)) {
                Detailfilter_names.add(">=docustom.".concat(colField));
                Detailfilter_params.add(advdueDate1);
                Detailfilter_names.add("<=docustom.".concat(colField));
                Detailfilter_params.add(advdueDate2);
            }
            doDetailRequestParams.put("filter_names", Detailfilter_names);
            doDetailRequestParams.put("filter_params", Detailfilter_params);
            KwlReturnObject idcustresult = accountingHandlerDAOobj.getDeliveryOrderCustomFields(doDetailRequestParams);
            if(idcustresult.getEntityList().size()>0) {
                List<DeliveryOrder> doList = idcustresult.getEntityList();
                for(DeliveryOrder deliveryOrder : doList) {
                    List data = new ArrayList();
                    if (deliveryOrder != null) {
                        AccCustomData customData =  deliveryOrder.getDeliveryOrderCustomData();
//                        long value = 0;
                        String value="";
                        if(colnum!=0) {
                            value = simpleformat.format(convertCustomStringDateToDateObject(customData.getCol(colnum)));
                        }
                        data.add(++sno);
                        data.add(deliveryOrder.getDeliveryOrderNumber());
                        data.add(simpleformat.format(new Date(deliveryOrder.getCreatedon())));
                        data.add(value);
                        finalData.add(data);
                    }
                }
                sendMail(finalData, headerItems, simpleformat, companyId, simpleDuedate, userMailID, mailContent, mailSubject,baseurl);
        }

      } catch(Exception ex) {
          ex.printStackTrace();
      }
     } 
             
      private void sendGoodsReceiptOrderCustomDateFieldMails(String fieldLabel, String colField, int colnum, String companyId, Date advdueDate1, Date advdueDate2, 
          String[] userMailID, DateFormat simpleformat, String simpleDuedate, String mailContent, String mailSubject,String baseurl) {
         try {
            List finalData = new ArrayList();
            int sno=0;
            List headerItems = new ArrayList();
            headerItems.add("S.No.");
            headerItems.add("GR Order Number");
            headerItems.add("Creation Date");
            headerItems.add(fieldLabel);

            HashMap<String, Object> groDetailRequestParams = new HashMap<String, Object>();
            ArrayList Detailfilter_names = new ArrayList(),Detailfilter_params = new ArrayList();
            Detailfilter_names.add("gro.company.companyID");
            Detailfilter_params.add(companyId);
            if (!StringUtil.isNullOrEmpty(colField)) {
                Detailfilter_names.add(">=grocustom.".concat(colField));
                Detailfilter_params.add(advdueDate1);
                Detailfilter_names.add("<=grocustom.".concat(colField));
                Detailfilter_params.add(advdueDate2);
            }
            groDetailRequestParams.put("filter_names", Detailfilter_names);
            groDetailRequestParams.put("filter_params", Detailfilter_params);
            KwlReturnObject idcustresult = accountingHandlerDAOobj.getGoodsReceiptOrderCustomFields(groDetailRequestParams);
            if(idcustresult.getEntityList().size()>0) {
                List<GoodsReceiptOrder> groList = idcustresult.getEntityList();
                for(GoodsReceiptOrder goodsReceiptOrder : groList) {
                    List data = new ArrayList();
                    if (goodsReceiptOrder != null) {
                        AccCustomData customData =  goodsReceiptOrder.getGoodsReceiptOrderCustomData();
//                        long value = 0;
                        String value="";
                        if(colnum!=0) {
                            value = simpleformat.format(convertCustomStringDateToDateObject(customData.getCol(colnum)));
                        }
                        data.add(++sno);
                        data.add(goodsReceiptOrder.getGoodsReceiptOrderNumber());
                        data.add(simpleformat.format(new Date(goodsReceiptOrder.getCreatedon())));
                        data.add(value);
                        finalData.add(data);
                    }
                }
                sendMail(finalData, headerItems, simpleformat, companyId, simpleDuedate, userMailID, mailContent, mailSubject,baseurl);
        }

      } catch(Exception ex) {
          ex.printStackTrace();
      }
     } 
   
     private void sendJournalEntryCustomDateFieldMails(String fieldLabel, String colField, int colnum, String companyId, Date advdueDate1, Date advdueDate2, 
          String[] userMailID, DateFormat simpleformat, String simpleDuedate, String mailContent, String mailSubject,String baseurl) {
         try {
            List finalData = new ArrayList();
            int sno=0;
            List headerItems = new ArrayList();
            headerItems.add("S.No.");
            headerItems.add("Journal Entry Number");
            headerItems.add("Creation Date");
            headerItems.add(fieldLabel);

            HashMap<String, Object> jeDetailRequestParams = new HashMap<String, Object>();
            ArrayList Detailfilter_names = new ArrayList(),Detailfilter_params = new ArrayList();
            Detailfilter_names.add("je.company.companyID");
            Detailfilter_params.add(companyId);
            if (!StringUtil.isNullOrEmpty(colField)) {
                Detailfilter_names.add(">=jecustom.".concat(colField));
                Detailfilter_params.add(advdueDate1);
                Detailfilter_names.add("<=jecustom.".concat(colField));
                Detailfilter_params.add(advdueDate2);
            }
            jeDetailRequestParams.put("filter_names", Detailfilter_names);
            jeDetailRequestParams.put("filter_params", Detailfilter_params);
            KwlReturnObject idcustresult = accountingHandlerDAOobj.getJournalEntryCustomFields(jeDetailRequestParams);
            if(idcustresult.getEntityList().size()>0) {
                List<JournalEntry> jeList = idcustresult.getEntityList();
                for(JournalEntry journalEntry : jeList) {
                    List data = new ArrayList();
                    if (journalEntry != null) {
                        AccCustomData customData =  journalEntry.getAccBillInvCustomData();
//                        long value = 0;
                        String value="";
                        if(colnum!=0) {
                            value =simpleformat.format(convertCustomStringDateToDateObject(customData.getCol(colnum)));
                        }
                        data.add(++sno);
                        data.add(journalEntry.getEntryNumber());
                        data.add(simpleformat.format(journalEntry.getEntryDate()));
                        data.add(value);
                        finalData.add(data);
                    }
                }
                sendMail(finalData, headerItems, simpleformat, companyId, simpleDuedate, userMailID, mailContent, mailSubject,baseurl);
            }
      } catch(Exception ex) {
          ex.printStackTrace();
      }
     }
     
     private void sendSalesReturnCustomDateFieldMails(String fieldLabel, String colField, int colnum, String companyId, Date advdueDate1, Date advdueDate2, 
          String[] userMailID, DateFormat simpleformat, String simpleDuedate, String mailContent, String mailSubject,String baseurl) {
         try {
            List finalData = new ArrayList();
            int sno=0;
            List headerItems = new ArrayList();
            headerItems.add("S.No.");
            headerItems.add("Sales Return Number");
            headerItems.add("Sales Return Date");
            headerItems.add(fieldLabel);

            HashMap<String, Object> srDetailRequestParams = new HashMap<String, Object>();
            ArrayList Detailfilter_names = new ArrayList(),Detailfilter_params = new ArrayList();
            Detailfilter_names.add("sr.company.companyID");
            Detailfilter_params.add(companyId);
            if (!StringUtil.isNullOrEmpty(colField)) {
                Detailfilter_names.add(">=srcustom.".concat(colField));
                Detailfilter_params.add(advdueDate1);
                Detailfilter_names.add("<=srcustom.".concat(colField));
                Detailfilter_params.add(advdueDate2);
            }
            srDetailRequestParams.put("filter_names", Detailfilter_names);
            srDetailRequestParams.put("filter_params", Detailfilter_params);
            KwlReturnObject idcustresult = accountingHandlerDAOobj.getSalesReturnCustomFields(srDetailRequestParams);
            if(idcustresult.getEntityList().size()>0) {
                List<SalesReturn> srList = idcustresult.getEntityList();
                for(SalesReturn salesReturn : srList) {
                    List data = new ArrayList();
                    if (salesReturn != null) {
                        AccCustomData customData =  salesReturn.getSalesReturnCustomData();
//                        long value = 0;
                        String value="";
                        if(colnum!=0) {
                            value =simpleformat.format(convertCustomStringDateToDateObject(customData.getCol(colnum)));
                        }
                        data.add(++sno);
                        data.add(salesReturn.getSalesReturnNumber());
                        data.add(simpleformat.format(salesReturn.getOrderDate()));
                        data.add(value);
                        finalData.add(data);
                    }
                }
                sendMail(finalData, headerItems, simpleformat, companyId, simpleDuedate, userMailID, mailContent, mailSubject,baseurl);
        }

      } catch(Exception ex) {
          ex.printStackTrace();
      }
     } 
             
     private void sendPurchaseReturnCustomDateFieldMails(String fieldLabel, String colField, int colnum, String companyId, Date advdueDate1, Date advdueDate2, 
          String[] userMailID, DateFormat simpleformat, String simpleDuedate, String mailContent, String mailSubject,String baseurl) {
         try {
            List finalData = new ArrayList();
            int sno=0;
            List headerItems = new ArrayList();
            headerItems.add("S.No.");
            headerItems.add("Purchase Return Number");
            headerItems.add("Purchase Return Date");
            headerItems.add(fieldLabel);

            HashMap<String, Object> prDetailRequestParams = new HashMap<String, Object>();
            ArrayList Detailfilter_names = new ArrayList(),Detailfilter_params = new ArrayList();
            Detailfilter_names.add("pr.company.companyID");
            Detailfilter_params.add(companyId);
            if (!StringUtil.isNullOrEmpty(colField)) {
                Detailfilter_names.add(">=prcustom.".concat(colField));
                Detailfilter_params.add(advdueDate1);
                Detailfilter_names.add("<=prcustom.".concat(colField));
                Detailfilter_params.add(advdueDate2);
            }
            prDetailRequestParams.put("filter_names", Detailfilter_names);
            prDetailRequestParams.put("filter_params", Detailfilter_params);
            KwlReturnObject idcustresult = accountingHandlerDAOobj.getPurchaseReturnCustomFields(prDetailRequestParams);
            if(idcustresult.getEntityList().size()>0) {
                List<PurchaseReturn> prList = idcustresult.getEntityList();
                for(PurchaseReturn purchaseReturn : prList) {
                    List data = new ArrayList();
                    if (purchaseReturn != null) {
                        AccCustomData customData =  purchaseReturn.getPurchaseReturnCustomData();
//                        long value = 0;
                        String value="";
                        if(colnum!=0) {
                            value =simpleformat.format(convertCustomStringDateToDateObject(customData.getCol(colnum)));
                        }
                        data.add(++sno);
                        data.add(purchaseReturn.getPurchaseReturnNumber());
                        data.add(simpleformat.format(purchaseReturn.getOrderDate()));
                        data.add(value);
                        finalData.add(data);
                    }
                }
                sendMail(finalData, headerItems, simpleformat, companyId, simpleDuedate, userMailID, mailContent, mailSubject,baseurl);
        }

      } catch(Exception ex) {
          ex.printStackTrace();
      }
     } 
     
       private void sendCustomerCustomDateFieldMails(String fieldLabel, String colField, int colnum, String companyId, Date advdueDate1, Date advdueDate2, 
          String[] userMailID, DateFormat simpleformat, String simpleDuedate, String mailContent, String mailSubject,String baseurl) {
         try {
            List finalData = new ArrayList();
            int sno=0;
            List headerItems = new ArrayList();
            headerItems.add("S.No.");
            headerItems.add("Customer Acc Code");
            headerItems.add("Customer Name");
            headerItems.add("Creation Date");
            headerItems.add(fieldLabel);

            HashMap<String, Object> custDetailRequestParams = new HashMap<String, Object>();
            ArrayList Detailfilter_names = new ArrayList(),Detailfilter_params = new ArrayList();
            Detailfilter_names.add("c.company.companyID");
            Detailfilter_params.add(companyId);
            if (!StringUtil.isNullOrEmpty(colField)) {
                Detailfilter_names.add(">=ccustom.".concat(colField));
                Detailfilter_params.add(advdueDate1);
                Detailfilter_names.add("<=ccustom.".concat(colField));
                Detailfilter_params.add(advdueDate2);
            }
            custDetailRequestParams.put("filter_names", Detailfilter_names);
            custDetailRequestParams.put("filter_params", Detailfilter_params);
            KwlReturnObject idcustresult = accountingHandlerDAOobj.getCustomerCustomFields(custDetailRequestParams);
            if(idcustresult.getEntityList().size()>0) {
                List<Customer> custList = idcustresult.getEntityList();
                for(Customer customer : custList) {
                    List data = new ArrayList();
                    if (customer != null) {
                        AccCustomData customData =  customer.getAccCustomerCustomData();
//                        long value = 0;
                        String value="";
                        if(colnum!=0) {
                            value =simpleformat.format(convertCustomStringDateToDateObject(customData.getCol(colnum)));
                        }
                        data.add(++sno);
                        data.add(customer.getAcccode());
                        data.add(customer.getName());
                        data.add(simpleformat.format(customer.getCreatedOn()));
                        data.add(value);
                        finalData.add(data);
                    }
                }
                sendMail(finalData, headerItems, simpleformat, companyId, simpleDuedate, userMailID, mailContent, mailSubject,baseurl);
        }

      } catch(Exception ex) {
          ex.printStackTrace();
      }
     } 
             
      private void sendVendorCustomDateFieldMails(String fieldLabel, String colField, int colnum, String companyId, Date advdueDate1, Date advdueDate2, 
          String[] userMailID, DateFormat simpleformat, String simpleDuedate, String mailContent, String mailSubject,boolean isCreationDate,String baseurl) {
         try {
            List finalData = new ArrayList();
            int sno=0;
            List headerItems = new ArrayList();
            headerItems.add("S.No.");
            headerItems.add("Vendor Acc Code");
            headerItems.add("Vendor Name");
            headerItems.add("Creation Date");
            headerItems.add(fieldLabel);

            HashMap<String, Object> vendDetailRequestParams = new HashMap<String, Object>();
            ArrayList Detailfilter_names = new ArrayList(),Detailfilter_params = new ArrayList();
            Detailfilter_names.add("v.company.companyID");
            Detailfilter_params.add(companyId);
            if (!StringUtil.isNullOrEmpty(colField)) {
                Detailfilter_names.add(">=vcustom.".concat(colField));
                Detailfilter_params.add(advdueDate1);
                Detailfilter_names.add("<=vcustom.".concat(colField));
                Detailfilter_params.add(advdueDate2);
            }
            vendDetailRequestParams.put("filter_names", Detailfilter_names);
            vendDetailRequestParams.put("filter_params", Detailfilter_params);
            KwlReturnObject idcustresult = accountingHandlerDAOobj.getVendorCustomFields(vendDetailRequestParams);
            if(idcustresult.getEntityList().size()>0) {
                List<Vendor> vendList = idcustresult.getEntityList();
                for(Vendor vendor : vendList) {
                    List data = new ArrayList();
                    if (vendor != null) {
                        AccCustomData customData =  vendor.getAccVendorCustomData();
//                        long value = 0;
                        String value="";
                        if(colnum!=0) {
                            value =simpleformat.format(convertCustomStringDateToDateObject(customData.getCol(colnum)));
                        }
                        data.add(++sno);
                        data.add(vendor.getAcccode());
                        data.add(vendor.getName());
                        if(vendor.getSelfBilledToDate()!=null){
                        data.add((isCreationDate) ?simpleformat.format(vendor.getCreatedOn()):simpleformat.format(vendor.getSelfBilledToDate()));
                        }
                        data.add(value);
                        finalData.add(data);
                    }
                }
                sendMail(finalData, headerItems, simpleformat, companyId, simpleDuedate, userMailID, mailContent, mailSubject,baseurl);
        }
      } catch(Exception ex) {
          ex.printStackTrace();
      }
     } 
      
      
    private List getHeaderData(boolean isCustomer,int moduleId) throws JSONException {
        List header = new ArrayList();
        try{
            if(moduleId==Constants.Acc_Invoice_ModuleId || moduleId==Constants.Acc_Vendor_Invoice_ModuleId ){
                header.add("S.No.");
                header.add("Invoice Number");
                if(isCustomer){
                    header.add("Customer Name");
                }else{
                    header.add("Vendor Name"); 
                }    
                header.add("Creation Date");
                header.add("Due Date");
                header.add("Currency");
                header.add("Total Amount");
                header.add("Amount Due"); 
            } else if(moduleId==Constants.Acc_Sales_Order_ModuleId || moduleId==Constants.Acc_Purchase_Order_ModuleId ){
                header.add("S.No.");                
                if(isCustomer){
                    header.add("Sales Order Number");
                    header.add("Customer Name");
                }else{
                    header.add("Purchase Order Number");
                    header.add("Vendor Name"); 
                }    
                header.add("Order Date");
                header.add("Currency");
            } else if(moduleId==Constants.Acc_Delivery_Order_ModuleId || moduleId==Constants.Acc_Goods_Receipt_ModuleId ){
                header.add("S.No.");                
                if(isCustomer){
                    header.add("Delivery Order Number");
                    header.add("Customer Name");
                }else{
                    header.add("GR Order Number");
                    header.add("Vendor Name"); 
                }    
                header.add("Order Date");
                header.add("Product Name");
            } else if(moduleId==Constants.Acc_Sales_Return_ModuleId || moduleId==Constants.Acc_Purchase_Return_ModuleId ){
                header.add("S.No.");                
                if(isCustomer){
                    header.add("Sales Return Number");
                    header.add("Customer Name");
                }else{
                    header.add("Purchase Return Number");
                    header.add("Vendor Name"); 
                }    
                header.add("Creation Date");
            } else if(moduleId==Constants.Acc_Customer_ModuleId || moduleId==Constants.Acc_Vendor_ModuleId ){
                header.add("S.No.");                
                if(isCustomer){
                    header.add("Customer Code");
                    header.add("Customer Name");
                }else{
                    header.add("Vendor Code");
                    header.add("Vendor Name"); 
                }                  
//                header.add("Creation Date");
            } else if(moduleId==Constants.Acc_Contract_Order_ModuleId){
                header.add("S.No.");
                header.add("Contract Number");
                header.add("Customer Name");
                header.add("Product Name");
                header.add("Start Date");
                header.add("End Date");
            } else if(moduleId==Constants.Asset_Maintenance_ModuleId){
                header.add("S.No.");
                header.add("Schedule Name");
                header.add("Asset Id");
                header.add("Start Date");
                header.add("End Date");
            }
                
        }catch(Exception ex){
          
        }
        
        return header; 
    }
    class MessageSizeExceedingException extends Exception{

        String msg="";
        public MessageSizeExceedingException(String message) {
            this.msg=message.trim();
        }
    
        @Override
        public String toString(){
            String sizeExceedingMsg="552 4.3.1 Message size exceeds fixed maximum message size";
            
            if(sizeExceedingMsg.equalsIgnoreCase(this.msg)){
                 return ("Attached file(s) size is exceeding message size limit!");
            }else{
                return ("");
            }
                
        }                
    }
    
    public static String getBillingShippingAddressForSenwanTec(BillingShippingAddresses billingShippingAddresses, boolean isbillingAddr) {
        String address="";
        try{
            if(billingShippingAddresses!=null){
                if(isbillingAddr){ //For billing Address
                     String addr=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingAddress())?"":billingShippingAddresses.getBillingAddress();
                     String city=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingCity())?"":", "+billingShippingAddresses.getBillingCity();
                     String state=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingState())?"":", "+billingShippingAddresses.getBillingState();
                     String country=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingCountry())?"":", "+billingShippingAddresses.getBillingCountry();
                     address=addr+city+state+country;
                }else{  //For Shipping Address
                     String addr=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingAddress())?"":billingShippingAddresses.getShippingAddress();
                     String city=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingCity())?"":", "+billingShippingAddresses.getShippingCity();
                     String state=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingState())?"":", "+billingShippingAddresses.getShippingState();
                     String country=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingCountry())?"":", "+billingShippingAddresses.getShippingCountry();
                     address=addr+city+state+country;
                }
            }
        }catch(Exception ex){
           Logger.getLogger(CommonFunctions.class.getName()).log(Level.SEVERE, null, ex); 
        }
        return address;
    }
    public static String getTotalBillingShippingAddress(BillingShippingAddresses billingShippingAddresses, boolean isbillingAddr) {
        String address="";
        try{
            if(billingShippingAddresses!=null){
                if(isbillingAddr){ //For billing Address
                     String billingCountryName = StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingCountry())?"":billingShippingAddresses.getBillingCountry();
                     billingCountryName = billingCountryName.trim();
                     billingCountryName = billingCountryName.toLowerCase();
                     if(billingCountryName.contains("malaysia")){ // for malaysia country address format is different SDP-2247
                        String addr=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingAddress())?"":billingShippingAddresses.getBillingAddress();
                        String postalcode=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingPostal())?"":"!##"+billingShippingAddresses.getBillingPostal();
                        String city=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingCity())?"":StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingPostal())?"!##"+billingShippingAddresses.getBillingCity():" "+billingShippingAddresses.getBillingCity();
                        String state=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingState())?"":"!##"+billingShippingAddresses.getBillingState();
                        String country=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingCountry())?"":"!##"+billingShippingAddresses.getBillingCountry();
                        address = addr + postalcode + city + state + country;
                     } else if(billingCountryName.equals("us") || billingCountryName.contains("usa") || billingCountryName.contains("united states")){ // for USA country address format is different
                        String addr=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingAddress())?"":billingShippingAddresses.getBillingAddress();
                        String postalcode=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingPostal())?"":"!##"+billingShippingAddresses.getBillingPostal();
                        String county=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingCounty())?"":"!##"+billingShippingAddresses.getBillingCounty();
                        String city=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingCity())?"":"!##"+billingShippingAddresses.getBillingCity();
                        String state=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingState())?"":"!##"+billingShippingAddresses.getBillingState();
                        String country=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingCountry())?"":"!##"+billingShippingAddresses.getBillingCountry();
                        address = addr + county + city + state + postalcode + country;
                     } else{
                        String addr=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingAddress())?"":billingShippingAddresses.getBillingAddress();
                        String city=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingCity())?"":"!## "+billingShippingAddresses.getBillingCity();
                        String state=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingState())?"":"!## "+billingShippingAddresses.getBillingState();
                        String country=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingCountry())?"":"!## "+billingShippingAddresses.getBillingCountry();
                        String postalcode=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingPostal())?"":"-"+billingShippingAddresses.getBillingPostal();
                        address = addr + city + state + country + postalcode;
                     }
                } else{  //For Shipping Address
                     String shippingCountryName = StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingCountry())?"":billingShippingAddresses.getShippingCountry();
                     shippingCountryName = shippingCountryName.trim();
                     shippingCountryName = shippingCountryName.toLowerCase();
                     if(shippingCountryName.contains("malaysia")){ // for malaysia country address format is different SDP-2247
                        String addr=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingAddress())?"":billingShippingAddresses.getShippingAddress();
                        String postalcode=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingPostal())?"":"!##"+billingShippingAddresses.getShippingPostal();
                        String city=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingCity())?"":StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingPostal())?"!##"+billingShippingAddresses.getShippingCity():" "+billingShippingAddresses.getShippingCity();
                        String state=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingState())?"":"!##"+billingShippingAddresses.getShippingState();
                        String country=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingCountry())?"":"!##"+billingShippingAddresses.getShippingCountry();
                        address = addr + postalcode + city + state + country;
                     } else if(shippingCountryName.equals("us") || shippingCountryName.contains("usa") || shippingCountryName.contains("united states")){ // for USA country address format is different
                        String addr=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingAddress())?"":billingShippingAddresses.getShippingAddress();
                        String postalcode=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingPostal())?"":"!##"+billingShippingAddresses.getShippingPostal();
                        String county=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingCounty())?"":"!##"+billingShippingAddresses.getShippingCounty();
                        String city=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingCity())?"":"!##"+billingShippingAddresses.getShippingCity();
                        String state=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingState())?"":"!##"+billingShippingAddresses.getShippingState();
                        String country=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingCountry())?"":"!##"+billingShippingAddresses.getShippingCountry();
                        address = addr + county + city + state + postalcode + country;
                     } else{
                        String addr=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingAddress())?"":billingShippingAddresses.getShippingAddress();
                        String city=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingCity())?"":"!## "+billingShippingAddresses.getShippingCity();
                        String state=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingState())?"":"!## "+billingShippingAddresses.getShippingState();
                        String country=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingCountry())?"":"!## "+billingShippingAddresses.getShippingCountry();
                        String postalcode=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingPostal())?"":"-"+billingShippingAddresses.getShippingPostal();
                        address = addr + city + state + country + postalcode;
                     }
                }
            }
        }catch(Exception ex){
           Logger.getLogger(CommonFunctions.class.getName()).log(Level.SEVERE, null, ex); 
        }
        return address;
    }
    public static String getTotalCustomerShippingAddressFromPurchaseDocument(BillingShippingAddresses billingShippingAddresses) {
        String address="";
        try{
            if(billingShippingAddresses!=null){
            //For Customer shipping Address
            String customerShippingCountryName = StringUtil.isNullOrEmpty(billingShippingAddresses.getCustomerShippingCountry())?"":billingShippingAddresses.getCustomerShippingCountry();
            customerShippingCountryName = customerShippingCountryName.trim();
            customerShippingCountryName = customerShippingCountryName.toLowerCase();
            String addr=StringUtil.isNullOrEmpty(billingShippingAddresses.getCustomerShippingAddress())?"":billingShippingAddresses.getCustomerShippingAddress();
            String postalcode=StringUtil.isNullOrEmpty(billingShippingAddresses.getCustomerShippingPostal())?"":"!##"+billingShippingAddresses.getCustomerShippingPostal();
            String county=StringUtil.isNullOrEmpty(billingShippingAddresses.getCustomerShippingCounty())?"":"!##"+billingShippingAddresses.getCustomerShippingCounty();
            String city=StringUtil.isNullOrEmpty(billingShippingAddresses.getCustomerShippingCity())?"":"!##"+billingShippingAddresses.getCustomerShippingCity();
            String state=StringUtil.isNullOrEmpty(billingShippingAddresses.getCustomerShippingState())?"":"!##"+billingShippingAddresses.getCustomerShippingState();
            String country=StringUtil.isNullOrEmpty(billingShippingAddresses.getCustomerShippingCountry())?"":"!##"+billingShippingAddresses.getCustomerShippingCountry();
            String contactPerson=StringUtil.isNullOrEmpty(billingShippingAddresses.getCustomerShippingContactPerson())?"":"!##"+billingShippingAddresses.getCustomerShippingContactPerson();
            String contactPersonNumber=StringUtil.isNullOrEmpty(billingShippingAddresses.getCustomerShippingContactPersonNumber())?"":"!##"+billingShippingAddresses.getCustomerShippingContactPersonNumber();
            String email=StringUtil.isNullOrEmpty(billingShippingAddresses.getCustomerShippingEmail())?"":"!##"+billingShippingAddresses.getCustomerShippingEmail();
            String phone=StringUtil.isNullOrEmpty(billingShippingAddresses.getCustomerShippingPhone())?"":"!##"+billingShippingAddresses.getCustomerShippingPhone();
            String fax=StringUtil.isNullOrEmpty(billingShippingAddresses.getCustomerShippingFax())?"":"!##"+billingShippingAddresses.getCustomerShippingFax();
            String mobile=StringUtil.isNullOrEmpty(billingShippingAddresses.getCustomerShippingMobile())?"":"!##"+billingShippingAddresses.getCustomerShippingMobile();
            address = addr + county + city + state + country + postalcode + contactPerson + contactPersonNumber + email + phone + fax + mobile;
        }
        }catch(Exception ex){
           Logger.getLogger(CommonFunctions.class.getName()).log(Level.SEVERE, null, ex); 
        }
        return address;
    }
    
   public static String getBillingShippingAddress(BillingShippingAddresses billingShippingAddresses, boolean isbillingAddr) {
        String address="";
        try{
            if(billingShippingAddresses!=null){
                if(isbillingAddr){ //For billing Address
                     String addr=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingAddress())?"":billingShippingAddresses.getBillingAddress();
                     String county=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingCounty())?"":", "+billingShippingAddresses.getBillingCounty();
                     String city=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingCity())?"":", "+billingShippingAddresses.getBillingCity();
                     String state=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingState())?"":", "+billingShippingAddresses.getBillingState();
                     String country=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingCountry())?"":", "+billingShippingAddresses.getBillingCountry();
                     String postalcode=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingPostal())?"":" "+billingShippingAddresses.getBillingPostal();
                     String email=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingEmail())?"":"\nEmail : "+billingShippingAddresses.getBillingEmail();
                     String website=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingWebsite())?"":"\nWebsite : "+billingShippingAddresses.getBillingWebsite();
                     String phone=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingPhone())?"":"\nPhone : "+billingShippingAddresses.getBillingPhone();
                     String fax=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingFax())?"":StringUtil.isNullOrEmpty(phone)? "\nFax : "+billingShippingAddresses.getBillingFax() :", Fax : "+billingShippingAddresses.getBillingFax();                    
                     address=addr+county+city+state+country+postalcode+email+website+phone+fax;
                }else{  //For Shipping Address
                     String addr=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingAddress())?"":billingShippingAddresses.getShippingAddress();
                     String county=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingCounty())?"":", "+billingShippingAddresses.getShippingCounty();
                     String city=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingCity())?"":", "+billingShippingAddresses.getShippingCity();
                     String state=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingState())?"":", "+billingShippingAddresses.getShippingState();
                     String country=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingCountry())?"":", "+billingShippingAddresses.getShippingCountry();
                     String postalcode=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingPostal())?"":" "+billingShippingAddresses.getShippingPostal();
                     String email=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingEmail())?"":"\nEmail : "+billingShippingAddresses.getShippingEmail();
                     String website=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingWebsite())?"":"\nWebsite : "+billingShippingAddresses.getShippingWebsite();
                     String phone=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingPhone())?"":"\nPhone : "+billingShippingAddresses.getShippingPhone();
                     String fax=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingFax())?"":StringUtil.isNullOrEmpty(phone)? "\nFax : "+billingShippingAddresses.getShippingFax() :", Fax : "+billingShippingAddresses.getShippingFax();                    
                     address=addr+county+city+state+country+postalcode+email+website+phone+fax;
                }
            }
        }catch(Exception ex){
           Logger.getLogger(CommonFunctions.class.getName()).log(Level.SEVERE, null, ex); 
        }
        return address;
    }
   public static String getBillingShippingAddressWithAttn(BillingShippingAddresses billingShippingAddresses, boolean isbillingAddr) {
        String address="";
        try{
            if(billingShippingAddresses!=null){
                if(isbillingAddr){ //For billing Address
                     String addr=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingAddress())?"":billingShippingAddresses.getBillingAddress();
                     String county=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingCounty())?"":", "+billingShippingAddresses.getBillingCounty();
                     String city=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingCity())?"":", "+billingShippingAddresses.getBillingCity();
                     String state=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingState())?"":", "+billingShippingAddresses.getBillingState();
                     String country=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingCountry())?"":", "+billingShippingAddresses.getBillingCountry();
                     String postalcode=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingPostal())?"":" "+billingShippingAddresses.getBillingPostal();
                     String email=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingEmail())?"":"\nEmail : "+billingShippingAddresses.getBillingEmail();
                     String website=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingWebsite())?"":"\nWebsite : "+billingShippingAddresses.getBillingWebsite();
                     String phone=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingPhone())?"":"\nPhone : "+billingShippingAddresses.getBillingPhone();
                     String fax=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingFax())?"":StringUtil.isNullOrEmpty(phone)? "\nFax : "+billingShippingAddresses.getBillingFax() :", Fax : "+billingShippingAddresses.getBillingFax();                    
                     String contractpersonno=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingContactPersonNumber())?"":"\nContact Person No : "+billingShippingAddresses.getBillingContactPersonNumber();
                     String contractpersondesignation=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingContactPersonDesignation())?"":"\nContact Person Designation : "+billingShippingAddresses.getBillingContactPersonDesignation();
                     String mobile=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingMobile())?"":"\nMobile : "+billingShippingAddresses.getBillingMobile();
                     String attn=StringUtil.isNullOrEmpty(billingShippingAddresses.getBillingContactPerson())?"":"\nAttn. : "+billingShippingAddresses.getBillingContactPerson();
                     address=addr+county+city+state+country+postalcode+email+website+phone+fax+mobile+contractpersonno+contractpersondesignation+attn;
                }else{  //For Shipping Address
                     String addr=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingAddress())?"":billingShippingAddresses.getShippingAddress();
                     String county=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingCounty())?"":", "+billingShippingAddresses.getShippingCounty();
                     String city=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingCity())?"":", "+billingShippingAddresses.getShippingCity();
                     String state=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingState())?"":", "+billingShippingAddresses.getShippingState();
                     String country=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingCountry())?"":", "+billingShippingAddresses.getShippingCountry();
                     String postalcode=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingPostal())?"":" "+billingShippingAddresses.getShippingPostal();
                     String email=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingEmail())?"":"\nEmail : "+billingShippingAddresses.getShippingEmail();
                     String website=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingWebsite())?"":"\nWebsite : "+billingShippingAddresses.getShippingWebsite();
                     String phone=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingPhone())?"":"\nPhone : "+billingShippingAddresses.getShippingPhone();
                     String fax=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingFax())?"":StringUtil.isNullOrEmpty(phone)? "\nFax : "+billingShippingAddresses.getShippingFax() :", Fax : "+billingShippingAddresses.getShippingFax();                    
                     String contractpersonno=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingContactPersonNumber())?"":"\nContact Person No : "+billingShippingAddresses.getShippingContactPersonNumber();
                     String contractpersondesignation=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingContactPersonDesignation())?"":"\nContact Person Designation : "+billingShippingAddresses.getShippingContactPersonDesignation();
                     String mobile=StringUtil.isNullOrEmpty(billingShippingAddresses.getShippingMobile())?"":"\nMobile : "+billingShippingAddresses.getShippingMobile();
                     address=addr+county+city+state+country+postalcode+email+website+phone+fax+mobile+contractpersonno+contractpersondesignation;
                }
            }
        }catch(Exception ex){
           Logger.getLogger(CommonFunctions.class.getName()).log(Level.SEVERE, null, ex); 
        }
        return address;
    }
   //get Vendor Transactional Address
     public static String getTotalVendorTransactionalShippingAddress(BillingShippingAddresses billingShippingAddresses, boolean ispurchaseorsales) {
        String address="";
        try{
            if(billingShippingAddresses!=null){
                if(ispurchaseorsales){ //For billing Address
                     String addr=StringUtil.isNullOrEmpty(billingShippingAddresses.getVendcustShippingAddress())?"":billingShippingAddresses.getVendcustShippingAddress();
                     String county=StringUtil.isNullOrEmpty(billingShippingAddresses.getVendcustShippingCounty())?"":", "+billingShippingAddresses.getVendcustShippingCounty();
                     String city=StringUtil.isNullOrEmpty(billingShippingAddresses.getVendcustShippingCity())?"":", "+billingShippingAddresses.getVendcustShippingCity();
                     String state=StringUtil.isNullOrEmpty(billingShippingAddresses.getVendcustShippingState())?"":", "+billingShippingAddresses.getVendcustShippingState();
                     String country=StringUtil.isNullOrEmpty(billingShippingAddresses.getVendcustShippingCountry())?"":", "+billingShippingAddresses.getVendcustShippingCountry();
                     String postalcode=StringUtil.isNullOrEmpty(billingShippingAddresses.getVendcustShippingPostal())?"":"-"+billingShippingAddresses.getVendcustShippingPostal();
                     address=addr+county+city+state+country + postalcode;
                }
            }
        }catch(Exception ex){
           Logger.getLogger(CommonFunctions.class.getName()).log(Level.SEVERE, null, ex); 
        }
        return address;
    }
        
    public static String getCompanyAddress(Company company) {
        String address="";
        try{
            if(company!=null){
                     String addr=StringUtil.isNullOrEmpty(company.getAddress())?"":company.getAddress();
                     String city=StringUtil.isNullOrEmpty(company.getCity())?"":", "+company.getCity();
                     String state=company.getState()!=null?(StringUtil.isNullOrEmpty(company.getState().getStateName())?"":", "+company.getState().getStateName()):"";
                     String country=company.getCountry()!=null?(StringUtil.isNullOrEmpty(company.getCountry().getCountryName())?"":", "+company.getCountry().getCountryName()):"";
                     String postalcode=StringUtil.isNullOrEmpty(company.getZipCode())?"":" "+company.getZipCode();
                     String website=StringUtil.isNullOrEmpty(company.getWebsite())?"":"\nWebsite : "+company.getWebsite();
                     String email=StringUtil.isNullOrEmpty(company.getEmailID())?"":" Email : "+company.getEmailID();
                     String phone=StringUtil.isNullOrEmpty(company.getPhoneNumber())?"":"\nPhone : "+company.getPhoneNumber();
                     String fax=StringUtil.isNullOrEmpty(company.getFaxNumber())?"":" Fax : "+company.getFaxNumber();
                     address=addr+city+state+country+postalcode+website+email+phone+fax;
            }
        }catch(Exception ex){
           Logger.getLogger(CommonFunctions.class.getName()).log(Level.SEVERE, null, ex); 
        }
        return address;
    }
    
    public static String getCompanyAddressForSenwanTec(Company company) {
        String address="";
        try{
            if(company!=null){
                     String addr=StringUtil.isNullOrEmpty(company.getAddress())?"":company.getAddress();
                     String city=StringUtil.isNullOrEmpty(company.getCity())?"":", "+company.getCity();
                     String state=company.getState()!=null?(StringUtil.isNullOrEmpty(company.getState().getStateName())?"":", "+company.getState().getStateName()):"";
                     String country=company.getCountry()!=null?(StringUtil.isNullOrEmpty(company.getCountry().getCountryName())?"":", "+company.getCountry().getCountryName()):"";
                     String postalcode=StringUtil.isNullOrEmpty(company.getZipCode())?"":" "+company.getZipCode();
                     address=addr+city+state+country+postalcode;
            }
        }catch(Exception ex){
           Logger.getLogger(CommonFunctions.class.getName()).log(Level.SEVERE, null, ex); 
        }
        return address;
    }
    
    public static double getTotalTermsAmount(JSONArray jArr) {
        double totalTermAmount = 0.0;
        try {            
            for (int cnt = 0; cnt < jArr.length(); cnt++) {
                JSONObject temp = jArr.getJSONObject(cnt);
                if (temp != null) {
                    totalTermAmount += Double.parseDouble(temp.getString("termamount"));
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(CommonFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
        return totalTermAmount;
    }
    
    public ModelAndView reCalculateInvoiceAmountDue(HttpServletRequest request, HttpServletResponse response) {
        String msg = "False";
        try {
            boolean sendMail = false;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            int module = 0;
            if (!StringUtil.isNullOrEmpty(request.getParameter("sendmail"))) {
                sendMail = Boolean.parseBoolean(request.getParameter("sendmail"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("module"))) {
                module = Integer.parseInt(request.getParameter("module"));
            }
            String[] emails = ConfigReader.getinstance().get("superuseremailid").split(",");
            String cid = request.getParameter("cid");
            if (!StringUtil.isNullOrEmpty(cid)) {
                HashMap<String, Object> requestParams = new HashMap();
                if (module == 0) {
                    KwlReturnObject srresult = accInvoiceDAOobj.getCompanyInvoices(cid);
                    List<Invoice> invoiceList = srresult.getEntityList();
                    List ll = new ArrayList();
                    for (Invoice invObj : invoiceList) {
                        if (requestParams.isEmpty()) {
                            requestParams.put("gcurrencyid", invObj.getCompany().getCurrency().getCurrencyID());
                            requestParams.put("companyid", invObj.getCompany().getCompanyID());
                        }

                        double amountdue = 0, amountdueinbase = 0;
                        if (invObj.isIsOpeningBalenceInvoice() && !invObj.isNormalInvoice()) {
                            ll = accInvoiceCommon.getCalculatedAmountDueForOpeningInvoices(requestParams, invObj);
                            amountdue = (Double) ll.get(0);
                            KwlReturnObject bAmt = null;
                            if (invObj.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountdue, invObj.getCurrency().getCurrencyID(), invObj.getCreationDate(), invObj.getExchangeRateForOpeningTransaction());
                        } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, invObj.getCurrency().getCurrencyID(), invObj.getCreationDate(), invObj.getExchangeRateForOpeningTransaction());
                            }
                            amountdueinbase = (Double) bAmt.getEntityList().get(0);
                            amountdueinbase = authHandler.round(amountdueinbase, companyid);
                        } else {
                            ll = accInvoiceCommon.getAmountDue_Discount(requestParams, invObj);
                            amountdue = (Double) ll.get(0);
                            amountdueinbase = authHandler.round((Double) ll.get(4), companyid);
                        }

                        boolean conditionToCheck = false;
                        conditionToCheck = invObj.isIsOpeningBalenceInvoice() ? (amountdue != invObj.getOpeningBalanceAmountDue()) : (amountdue != invObj.getInvoiceamountdue() || (amountdueinbase != invObj.getInvoiceAmountDueInBase()));

                        if (conditionToCheck) {
                            String columnname = invObj.isIsOpeningBalenceInvoice() ? "openingbalanceamountdue" : "invoiceamountdue";
                            String plainMsgC = "";
                            plainMsgC += "\nHello Admin\n";
                            plainMsgC += "\nFollowing Invoice amount due not matched. Please check " + columnname + " column from invoice table. \n";
                            plainMsgC += "\nDetails - \n";
                            plainMsgC += "Invoice - " + invObj.getInvoiceNumber() + "\n";
                            plainMsgC += "Invoice ID - " + invObj.getID() + "\n";
                            plainMsgC += "Company - " + invObj.getCompany().getCompanyID() + "\n";
                            plainMsgC += "Subdomain - " + invObj.getCompany().getSubDomain() + "\n";
                            plainMsgC += "Stored Amount - " + (invObj.isIsOpeningBalenceInvoice() ? invObj.getOpeningBalanceAmountDue() : invObj.getInvoiceamountdue()) + "\n";
                            plainMsgC += "Calculated Amount - " + amountdue + "\n";
                            JSONObject invjson = new JSONObject();
                            invjson.put("invoiceid", invObj.getID());
                            if (invObj.isIsOpeningBalenceInvoice()) {
                                invjson.put("openingBalanceAmountDue", amountdue);
                            } else {
                                invjson.put(Constants.invoiceamountdue, amountdue);
                                invjson.put(Constants.invoiceamountdueinbase, amountdueinbase);
                            }
                            accInvoiceDAOobj.updateInvoice(invjson, null);
                            if (sendMail) {
                                try {
                                    String fromEmailId = (!invObj.getCompany().isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(invObj.getCompany().getCompanyID());
                                    Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(invObj.getCompany());
                                    SendMailHandler.postMail(emails, "[Deskera ERP] Amount Due not matched", plainMsgC, plainMsgC, fromEmailId, "Sagar", smtpConfigMap);
                                } catch (Exception ex) {
                                    System.out.println(plainMsgC);
                                }
                            }
                        }
                    }
                    msg = "Records processed successfully. Total Records - " + invoiceList.size();
                } else {
                    KwlReturnObject srresult = accGoodsReceiptobj.getCompanyGoodsReceipts(cid);
                    List<GoodsReceipt> invoiceList = srresult.getEntityList();
                    for (GoodsReceipt invObj : invoiceList) {
                        if (requestParams.isEmpty()) {
                            requestParams.put("gcurrencyid", invObj.getCompany().getCurrency().getCurrencyID());
                            requestParams.put("companyid", invObj.getCompany().getCompanyID());
                        }
                        List ll = new ArrayList();
                        double amountdue=0,amountdueInBase=0;
                        KwlReturnObject bAmt = null;
                        if (invObj.isIsExpenseType()) {
                            ll = accGoodsReceiptCommon.getExpGRAmountDue(requestParams, invObj);
                            amountdue = (Double) ll.get(1);
                            amountdue = authHandler.round(amountdue, companyid);
                            amountdueInBase = authHandler.round((Double) ll.get(5), companyid);
                        } else if (invObj.isIsOpeningBalenceInvoice() && !invObj.isNormalInvoice()) {
                                ll = accGoodsReceiptCommon.getAmountDueCalculatedForOpeningGoodsReceipt(requestParams, invObj);
                            amountdue = (Double) ll.get(1);
                            amountdue = authHandler.round(amountdue, companyid);
                            if (invObj.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountdue, invObj.getCurrency().getCurrencyID(), invObj.getCreationDate(), invObj.getExchangeRateForOpeningTransaction());
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountdue, invObj.getCurrency().getCurrencyID(), invObj.getCreationDate(), invObj.getExchangeRateForOpeningTransaction());
                            }
                            amountdueInBase = (Double) bAmt.getEntityList().get(0);
                            amountdueInBase = authHandler.round(amountdueInBase, companyid);

                        } else {
                            ll = accGoodsReceiptCommon.getGRAmountDue(requestParams, invObj);
                            amountdue = (Double) ll.get(1);
                        amountdue = authHandler.round(amountdue, companyid);
                            amountdueInBase = authHandler.round((Double) ll.get(6), companyid);
                        }


                        boolean conditionToCheck = false;
                        conditionToCheck = invObj.isIsOpeningBalenceInvoice() ? (amountdue != invObj.getOpeningBalanceAmountDue()) : (amountdue != invObj.getInvoiceamountdue() || amountdueInBase != invObj.getInvoiceAmountDueInBase());

                        if (conditionToCheck) {
                            String plainMsgC = "";
                            String columnName = invObj.isIsOpeningBalenceInvoice() ? "openingbalanceamountdue" : "invoiceamountdue";
                            plainMsgC += "\nHello Admin\n";
                            plainMsgC += "\nFollowing Goods Receipt amount due not matched. Please check " + columnName + " column from invoice table. \n";
                            plainMsgC += "\nDetails - \n";
                            plainMsgC += "Goods Receipt - " + invObj.getGoodsReceiptNumber() + "\n";
                            plainMsgC += "Invoice ID - " + invObj.getID() + "\n";
                            plainMsgC += "Company - " + invObj.getCompany().getCompanyID() + "\n";
                            plainMsgC += "Subdomain - " + invObj.getCompany().getSubDomain() + "\n";
                            plainMsgC += "Stored Amount - " + (invObj.isIsOpeningBalenceInvoice() ? invObj.getOpeningBalanceAmountDue() : invObj.getInvoiceamountdue()) + "\n";
                            plainMsgC += "Calculated Amount - " + amountdue + "\n";
                            Map<String, Object> greceipthm = new HashMap<String, Object>();
                            greceipthm.put("grid", invObj.getID());
                            if (invObj.isIsOpeningBalenceInvoice()) {
                                greceipthm.put("openingBalanceAmountDue", amountdue);
                            } else {
                                greceipthm.put(Constants.invoiceamountdue, amountdue);
                                greceipthm.put(Constants.invoiceamountdueinbase, amountdueInBase);
                            }
                            accGoodsReceiptobj.updateGoodsReceipt(greceipthm);
                            if (sendMail) {
                                try {
                                    String fromEmailId = (!invObj.getCompany().isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(invObj.getCompany().getCompanyID());
                                    Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(invObj.getCompany());
                                    SendMailHandler.postMail(emails, "[Deskera ERP] Amount Due not matched", plainMsgC, plainMsgC, fromEmailId, "Sagar", smtpConfigMap);
                                } catch (Exception ex) {
                                    System.out.println(plainMsgC);
                                }
                            }
                        }
                    }
                    msg = "Records processed successfully. Total Records - " + invoiceList.size();
                }

            }
        } catch (Exception ex) {
            System.out.println("------ Error at reCalculateInvoiceAmountDue() ----------");
            ex.printStackTrace();
        }
        return new ModelAndView("jsonView_ex", "model", msg);
    }
    //Same function which is used to calculate forex gain loss(Controller-accVendorPaymentControllerNew,funtion-oldPaymentRowsAmount)
    public ModelAndView findForeignGainLossForOldPayments(HttpServletRequest request, HttpServletResponse response) {
        String msg = "False";
        double amount = 0, actualAmount = 0;
        try {

            String cid = request.getParameter("cid");
            if (!StringUtil.isNullOrEmpty(cid)) {
                KwlReturnObject resultCompany = accountingHandlerDAOobj.getObject(Company.class.getName(), cid);
                Company company = (Company) resultCompany.getEntityList().get(0);
                String currencyid = company.getCurrency().getCurrencyID();
                HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
                GlobalParams.put("companyid", cid);
                GlobalParams.put("gcurrencyid", currencyid);
//                GlobalParams.put("dateformat", authHandler.getDateOnlyFormatter(request));
                SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy");//Change It before Commiting the code
                GlobalParams.put("dateformat", sdf);
                GlobalParams.put("df", sdf);
//                Date creationDate = authHandler.getDateOnlyFormatter(request).parse(request.getParameter("creationdate"));
                List<Payment> payments = accVendorPaymentobj.getPaymentListFromCompany(cid);
                CompanyAccountPreferences pref = null;
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), cid);
                pref = (CompanyAccountPreferences) returnObject.getEntityList().get(0);
                Account forexAccount = pref.getForeignexchange();
                boolean forex = true;
                for (Payment payment : payments) {
                    JSONArray jArr = new JSONArray();
                    actualAmount = 0d;
                    double externalCurrencyRate = 0d;
                    forex = true;
                    Date creationDate = null;
                    if (payment.isNormalPayment()) {
                        externalCurrencyRate = payment.getExternalCurrencyRate();
//                        creationDate = payment.getJournalEntry().getEntryDate();
                        creationDate = payment.getCreationDate();
                        for (JournalEntryDetail entryDetail : payment.getJournalEntry().getDetails()) {
                            if (entryDetail.getAccount().getID().equals(forexAccount.getID())) {
                                forex = false;
                            }
                        }
                    } else {
                        externalCurrencyRate = payment.getExchangeRateForOpeningTransaction();
                        creationDate = payment.getCreationDate();
                    }

                    getPaymentDetailsInfo(payment, null, GlobalParams, jArr);
                    for (int i = 0; i < jArr.length(); i++) {
                        double ratio = 0;
                        JSONObject jobj = jArr.getJSONObject(i);
                        //                  boolean revalFlag=false;
                        //                  GoodsReceipt gr = (GoodsReceipt) session.get(GoodsReceipt.class, jobj.getString("billid"));
                        double newrate = 0.0;
                        boolean revalFlag = false;
//                  KwlReturnObject result = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), jobj.getString("billid"));
                        KwlReturnObject result = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), jobj.getString("documentid"));
                        GoodsReceipt gr = (GoodsReceipt) result.getEntityList().get(0);
                        boolean isopeningBalanceInvoice = gr.isIsOpeningBalenceInvoice();
//                  Double recinvamount = jobj.getDouble("payment");
                        Double recinvamount = jobj.getDouble("enteramount");
                        boolean isopeningBalancePayment = jobj.optBoolean("isopeningBalancePayment", false);
                        boolean isConversionRateFromCurrencyToBase = jobj.optBoolean("isConversionRateFromCurrencyToBase", false);
                        double exchangeRate = 0d;
                        Date goodsReceiptCreationDate = null;
                        if (gr.isNormalInvoice()) {
                            exchangeRate = gr.getJournalEntry().getExternalCurrencyRate();
//                            goodsReceiptCreationDate = gr.getJournalEntry().getEntryDate();
                        } else {
                            exchangeRate = gr.getExchangeRateForOpeningTransaction();
                        }
                        goodsReceiptCreationDate = gr.getCreationDate();


                        HashMap<String, Object> invoiceId = new HashMap<String, Object>();
                        invoiceId.put("invoiceid", gr.getID());
                        invoiceId.put("companyid", cid);
//                  if (gr.isNormalInvoice()) {
                        result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                        RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                        if (history != null) {
                            exchangeRate = history.getEvalrate();
                            newrate = jobj.optDouble("exchangeratefortransaction", 0.0);
                            revalFlag = true;
                        }
//                  }
                        result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
                        KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
                        String currid = currency.getCurrencyID();
                        if (gr.getCurrency() != null) {
                            currid = gr.getCurrency().getCurrencyID();
                        }
                        currencyid = payment.getCurrency().getCurrencyID();
                        KwlReturnObject bAmt = null;
                        if (currid.equalsIgnoreCase(currencyid)) {
                            if (history == null && isopeningBalanceInvoice && gr.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getOneCurrencyToOtherAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
                            } else {
                                double paymentExternalCurrencyRate = externalCurrencyRate;
                                if (exchangeRate != paymentExternalCurrencyRate) {
                                    bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate, paymentExternalCurrencyRate);
                                } else {
                                    bAmt = accCurrencyDAOobj.getOneCurrencyToOther(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
                                }
                            }
                        } else {
                            if (history == null && isopeningBalanceInvoice && gr.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModifiedAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
                            } else {
                                double paymentExternalCurrencyRate = externalCurrencyRate;
                                if (exchangeRate != paymentExternalCurrencyRate) {
                                    bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate, paymentExternalCurrencyRate);
                                } else {
                                    bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
                                }
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
                                    double paymentExternalCurrencyRate = externalCurrencyRate;
                                    if (exchangeRate != paymentExternalCurrencyRate) {
                                        bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate, paymentExternalCurrencyRate);
                                    } else {
                                        bAmt = accCurrencyDAOobj.getOneCurrencyToOther(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
                                    }
                                }
                            } else {
                                if (history == null && isopeningBalanceInvoice && gr.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                                    bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModifiedAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
                                } else {
                                    double paymentExternalCurrencyRate = externalCurrencyRate;
                                    if (exchangeRate != paymentExternalCurrencyRate) {
                                        bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate, paymentExternalCurrencyRate);
                                    } else {
                                        bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
                                    }
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
                            actualAmount += (Double) bAmtActual.getEntityList().get(0);
                        }
                    }
                    if (amount != 0d && forex && payment.getRows() != null && payment.getRows().size() != 0) {
                        if (msg.equals("False")) {
                            boolean rateDecreased = false;
                            if (actualAmount < 0) {
                                rateDecreased = true;
                            }
                            actualAmount = (-1 * actualAmount);
                            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountRoundOff(GlobalParams, actualAmount, currencyid, creationDate, externalCurrencyRate);
                            msg = payment.getPaymentNumber() + " actualAmount: " + actualAmount + " baseAmount: " + bAmt.getEntityList().get(0);
                        } else {
                            boolean rateDecreased = false;
                            if (actualAmount < 0) {
                                rateDecreased = true;
                            }
                             actualAmount = (-1 * actualAmount);
                            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountRoundOff(GlobalParams, actualAmount, currencyid, creationDate, externalCurrencyRate);
                            msg+= "\n" + payment.getPaymentNumber() + " actualAmount: " + actualAmount + " baseAmount: " + bAmt.getEntityList().get(0);
                        }
                    }

                }
            }
        } catch (Exception ex) {
            System.out.println("------ Error at findForeignGainLossForOldPayments() ----------");
            ex.printStackTrace();
        }
        if (!msg.equals("False")) {
            System.out.println(msg);
        }
        return new ModelAndView("jsonView_ex", "model", msg);
    }

    public void getPaymentDetailsInfo(Payment payment, Account acc, HashMap<String, Object> requestParams, JSONArray jSONArray) throws JSONException, ServiceException {
        DateFormat df = (DateFormat) requestParams.get("df");
        Set<PaymentDetail> paymentDetails = payment.getRows();
        HashMap<String, Object> fieldrequestParams = new HashMap();
        HashMap<String, String> customFieldMap = new HashMap<String, String>();
        HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
        fieldrequestParams.put(Constants.filter_values, Arrays.asList(payment.getCompany().getCompanyID(), Constants.Acc_Make_Payment_ModuleId));
        HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
        HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
        String companyid = (String) requestParams.get("companyid");
        for (PaymentDetail paymentDetail : paymentDetails) {
            JSONObject obj = new JSONObject();
            GoodsReceipt goodsReceipt = paymentDetail.getGoodsReceipt();
            obj.put("type", Constants.PaymentAgainstInvoice);
            obj.put("modified", true);
            obj.put("exchangeratefortransaction", paymentDetail.getExchangeRateForTransaction());
            obj.put("enteramount", paymentDetail.getAmount());
//            obj.put("amountdue", paymentDetail.getAmountDueInPaymentCurrency());
//            obj.put("amountDueOriginal", paymentDetail.getAmountDueInGrCurrency());
//            obj.put("amountDueOriginalSaved", paymentDetail.getAmountDueInGrCurrency());
            obj.put("amountdue", authHandler.round(goodsReceipt.getInvoiceamountdue() * paymentDetail.getExchangeRateForTransaction(), companyid) + paymentDetail.getAmount());
            obj.put("amountDueOriginal", goodsReceipt.getInvoiceamountdue() + paymentDetail.getAmountInGrCurrency());
            obj.put("amountDueOriginalSaved", goodsReceipt.getInvoiceamountdue() + paymentDetail.getAmountInGrCurrency());
            obj.put("taxamount", 0);
            obj.put("prtaxid", "");
            obj.put("description", paymentDetail.getDescription());
            obj.put("documentid", goodsReceipt.getID());
            obj.put("documentno", goodsReceipt.getGoodsReceiptNumber());
            obj.put("payment", "");
            obj.put("currencyid", payment.getCurrency().getCurrencyID());
            obj.put("currencysymbol", payment.getCurrency().getSymbol());
            obj.put("currencyname", payment.getCurrency().getName());
            obj.put("currencysymboltransaction", goodsReceipt.getCurrency().getSymbol());
            obj.put("currencynametransaction", goodsReceipt.getCurrency().getName());
            obj.put("currencyidtransaction", goodsReceipt.getCurrency().getCurrencyID());
            obj.put("date", df.format((Date) goodsReceipt.getCreationDate()));
//            if (goodsReceipt.isIsOpeningBalenceInvoice() && !goodsReceipt.isNormalInvoice()) {
//                obj.put("date", df.format((Date) goodsReceipt.getCreationDate()));
//            } else {
//                obj.put("date", df.format((Date) goodsReceipt.getJournalEntry().getEntryDate()));
//            }
            jSONArray.put(obj);
        }
    }
    
    public ModelAndView reCalculateOpeningBalanceAmount(HttpServletRequest request, HttpServletResponse response) {
        String msg = "False";
         try {
             boolean sendMail = false;
//             int module = -1;
//             if(!StringUtil.isNullOrEmpty(request.getParameter("sendmail"))) {
//                 sendMail = Boolean.parseBoolean(request.getParameter("sendmail"));
//             }
//             if(!StringUtil.isNullOrEmpty(request.getParameter("module"))) {
//                 module = Integer.parseInt(request.getParameter("module"));
//             }
             String[] emails = ConfigReader.getinstance().get("superuseremailid").split(",");
             String cid = request.getParameter("cid");
             if(!StringUtil.isNullOrEmpty(cid)) {
                reCalculateOpeningBalanceAmountForCI(cid, sendMail, emails);
                reCalculateOpeningBalanceAmountForVI(cid, sendMail, emails);
                reCalculateOpeningBalanceAmountForRP(cid, sendMail, emails);
                reCalculateOpeningBalanceAmountForMP(cid, sendMail, emails);
                reCalculateOpeningBalanceAmountForCN(cid, sendMail, emails);
                reCalculateOpeningBalanceAmountForDN(cid, sendMail, emails);
             }
             msg = "True";
         } catch (Exception ex) {
             System.out.println("------ Error at reCalculateOpeningBalanceAmount() ----------");
             ex.printStackTrace();
         }
         return new ModelAndView("jsonView_ex", "model", msg);
    }
    
    private void reCalculateOpeningBalanceAmountForCI(String cid, boolean sendMail, String[] emails) {
        String msg = "";
        HashMap<String, Object> requestParams = new HashMap();
        try {
            requestParams.put(Constants.companyKey, cid);
            KwlReturnObject srresult = accInvoiceDAOobj.getOpeningBalanceInvoices(requestParams);
            List<Invoice> invoiceList = srresult.getEntityList();
            for(Invoice invoice : invoiceList) {
               if(!requestParams.containsKey("gcurrencyid")) {
                   requestParams.put("gcurrencyid", invoice.getCompany().getCurrency().getCurrencyID());
                   requestParams.put("companyid", invoice.getCompany().getCompanyID());
               }
                double externalCurrencyRate = 0d;
                boolean isopeningBalanceInvoice = invoice.isIsOpeningBalenceInvoice();
                Date invoiceCreationDate = null;
                double amount = 0d, amountDue=0d;
                double baseAmount = 0d, baseAmountDue = 0d;
                amount = invoice.getOriginalOpeningBalanceAmount();
                amountDue = invoice.getOpeningBalanceAmountDue();
                invoiceCreationDate = invoice.getCreationDate();
                externalCurrencyRate = invoice.getExchangeRateForOpeningTransaction();
                String fromcurrencyid = invoice.getCurrency().getCurrencyID();
                KwlReturnObject bAmt = null;
                if (isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amount, fromcurrencyid, invoiceCreationDate, externalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amount, fromcurrencyid, invoiceCreationDate, externalCurrencyRate);
                }

                baseAmount = (Double) bAmt.getEntityList().get(0);
                
                bAmt = null;
                if (isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, invoiceCreationDate, externalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, invoiceCreationDate, externalCurrencyRate);
                }
                baseAmountDue = (Double) bAmt.getEntityList().get(0);
               if(baseAmount!=invoice.getOriginalOpeningBalanceBaseAmount() || baseAmountDue!=invoice.getOpeningBalanceBaseAmountDue()) {
                   String plainMsgC = "";
                   plainMsgC+="\nHello Admin\n";  
                   plainMsgC+="\nFollowing Invoice "+Constants.originalOpeningBalanceBaseAmount+" not matched. Please check originalopeningbalancebaseamount column from invoice table. \n";
                   plainMsgC+="\nDetails - \n";
                   plainMsgC+="Customer Invoice - "+invoice.getInvoiceNumber()+"\n";
                   plainMsgC+="Customer Invoice ID - "+invoice.getID()+"\n";
                   plainMsgC+="Company - "+invoice.getCompany().getCompanyID()+"\n";
                   plainMsgC+="Subdomain - "+invoice.getCompany().getSubDomain()+"\n";
                   plainMsgC+="Stored "+Constants.originalOpeningBalanceBaseAmount+" - "+invoice.getOriginalOpeningBalanceBaseAmount()+"\n";
                   plainMsgC+="Calculated "+Constants.originalOpeningBalanceBaseAmount+" - "+baseAmount+"\n";
                   plainMsgC+="Stored "+Constants.openingBalanceBaseAmountDue+" - "+invoice.getOpeningBalanceBaseAmountDue()+"\n";
                   plainMsgC+="Calculated "+Constants.openingBalanceBaseAmountDue+" - "+baseAmountDue+"\n";
                   JSONObject invjson = new JSONObject();
                   invjson.put("invoiceid", invoice.getID());
                   invjson.put(Constants.originalOpeningBalanceBaseAmount, baseAmount);
                   invjson.put(Constants.openingBalanceBaseAmountDue, baseAmountDue);
                   accInvoiceDAOobj.updateInvoice(invjson, null);
                   if(sendMail) {
                       try {
                           String fromEmailId = (!invoice.getCompany().isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(invoice.getCompany().getCompanyID());
                           Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(invoice.getCompany());
                           SendMailHandler.postMail(emails, "[Deskera ERP] "+Constants.originalOpeningBalanceBaseAmount+" not matched", plainMsgC,plainMsgC, fromEmailId, "Sagar", smtpConfigMap);
                       } catch (Exception ex) {
                           System.out.println(plainMsgC);
                       }
                   }
               }
           }
           msg = "Customer Invoice - Records processed successfully. Total Records - "+invoiceList.size();
        } catch(Exception ex) {
            ex.printStackTrace();
        } finally {
            System.out.println(msg);
        }
    }
    
    private void reCalculateOpeningBalanceAmountForVI(String cid, boolean sendMail, String[] emails) {
        String msg = "";
        HashMap<String, Object> requestParams = new HashMap();
        try {
            requestParams.put(Constants.companyKey, cid);
            KwlReturnObject srresult = accGoodsReceiptobj.getOpeningBalanceInvoices(requestParams);
            List<String> invoiceList = srresult.getEntityList();
            for(String grId : invoiceList) {
                KwlReturnObject grReceiptReturnObj = kwlCommonTablesDAOObj.getObject(GoodsReceipt.class.getName(), grId);
                GoodsReceipt invoice = (GoodsReceipt) grReceiptReturnObj.getEntityList().get(0);
               if(!requestParams.containsKey("gcurrencyid")) {
                   requestParams.put("gcurrencyid", invoice.getCompany().getCurrency().getCurrencyID());
                   requestParams.put("companyid", invoice.getCompany().getCompanyID());
               }
                double externalCurrencyRate = 0d;
                boolean isopeningBalanceInvoice = invoice.isIsOpeningBalenceInvoice();
                Date invoiceCreationDate = null;
                double amount = 0d, amountDue=0d;
                double baseAmount = 0d, baseAmountDue=0d;
                amount = invoice.getOriginalOpeningBalanceAmount();
                amountDue = invoice.getOpeningBalanceAmountDue();
                invoiceCreationDate = invoice.getCreationDate();
                externalCurrencyRate = invoice.getExchangeRateForOpeningTransaction();
                String fromcurrencyid = invoice.getCurrency().getCurrencyID();
                KwlReturnObject bAmt = null;
                if (isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amount, fromcurrencyid, invoiceCreationDate, externalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amount, fromcurrencyid, invoiceCreationDate, externalCurrencyRate);
                }
                baseAmount = (Double) bAmt.getEntityList().get(0);
                
                bAmt = null;
                if (isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, invoiceCreationDate, externalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, invoiceCreationDate, externalCurrencyRate);
                }
                baseAmountDue = (Double) bAmt.getEntityList().get(0);
               if(baseAmount!=invoice.getOriginalOpeningBalanceBaseAmount() || baseAmountDue!=invoice.getOpeningBalanceBaseAmountDue()) {
                   String plainMsgC = "";
                   plainMsgC+="\nHello Admin\n";  
                   plainMsgC+="\nFollowing Invoice "+Constants.originalOpeningBalanceBaseAmount+" not matched. Please check originalopeningbalancebaseamount column from invoice table. \n";
                   plainMsgC+="\nDetails - \n";
                   plainMsgC+="Vendor Invoice - "+invoice.getGoodsReceiptNumber()+"\n";
                   plainMsgC+="Vendor Invoice ID - "+invoice.getID()+"\n";
                   plainMsgC+="Company - "+invoice.getCompany().getCompanyID()+"\n";
                   plainMsgC+="Subdomain - "+invoice.getCompany().getSubDomain()+"\n";
                   plainMsgC+="Stored "+Constants.originalOpeningBalanceBaseAmount+" - "+invoice.getOriginalOpeningBalanceBaseAmount()+"\n";
                   plainMsgC+="Calculated "+Constants.originalOpeningBalanceBaseAmount+" - "+baseAmount+"\n";
                   plainMsgC+="Stored "+Constants.openingBalanceBaseAmountDue+" - "+invoice.getOpeningBalanceBaseAmountDue()+"\n";
                   plainMsgC+="Calculated "+Constants.openingBalanceBaseAmountDue+" - "+baseAmountDue+"\n";
                   HashMap<String, Object> hm = new HashMap();
                   hm.put("grid", invoice.getID());
                   hm.put(Constants.originalOpeningBalanceBaseAmount, baseAmount);
                   hm.put(Constants.openingBalanceBaseAmountDue, baseAmountDue);
                   accGoodsReceiptobj.updateGoodsReceipt(hm);
                   if(sendMail) {
                       try {
                           String fromEmailId = (!invoice.getCompany().isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(invoice.getCompany().getCompanyID());
                           Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(invoice.getCompany());
                           SendMailHandler.postMail(emails, "[Deskera ERP] "+Constants.originalOpeningBalanceBaseAmount+" not matched", plainMsgC,plainMsgC, fromEmailId, "Sagar", smtpConfigMap);
                       } catch (Exception ex) {
                           System.out.println(plainMsgC);
                       }
                   }
               }
           }
           msg = "Vendor Invoice - Records processed successfully. Total Records - "+invoiceList.size();
        } catch(Exception ex) {
            ex.printStackTrace();
        } finally {
            System.out.println(msg);
        }
    }
    
    private void reCalculateOpeningBalanceAmountForRP(String cid, boolean sendMail, String[] emails) {
        String msg = "";
        HashMap<String, Object> requestParams = new HashMap();
        try {
            requestParams.put(Constants.companyKey, cid);
            KwlReturnObject receiptResult = accReceiptobj.getOpeningBalanceReceipts(requestParams);
            List<Receipt> receiptList = receiptResult.getEntityList();
            for(Receipt receipt : receiptList) {
               if(!requestParams.containsKey("gcurrencyid")) {
                   requestParams.put("gcurrencyid", receipt.getCompany().getCurrency().getCurrencyID());
                   requestParams.put("companyid", receipt.getCompany().getCompanyID());
               }
                double externalCurrencyRate = 0d;
                boolean isopeningBalanceRecceipt = receipt.isIsOpeningBalenceReceipt();
                double amount = 0d,amountDue=0d, baseAmount=0d, baseAmountDue=0d;
                Date receiptCreationDate = null;
                amount = receipt.getDepositAmount();
                amountDue = receipt.getOpeningBalanceAmountDue();
                receiptCreationDate = receipt.getCreationDate();
                externalCurrencyRate = receipt.getExchangeRateForOpeningTransaction();

                String fromcurrencyid = receipt.getCurrency().getCurrencyID();
                KwlReturnObject bAmt = null;
                if (isopeningBalanceRecceipt && receipt.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amount, fromcurrencyid, receiptCreationDate, externalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amount, fromcurrencyid, receiptCreationDate, externalCurrencyRate);
                }
                baseAmount = (Double) bAmt.getEntityList().get(0);
                
                bAmt = null;
                if (isopeningBalanceRecceipt && receipt.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, receiptCreationDate, externalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, receiptCreationDate, externalCurrencyRate);
                }
                baseAmountDue = (Double) bAmt.getEntityList().get(0);
               if(baseAmount!=receipt.getOriginalOpeningBalanceBaseAmount()  || baseAmountDue!=receipt.getOpeningBalanceBaseAmountDue()) {
                   String plainMsgC = "";
                   plainMsgC+="\nHello Admin\n";  
                   plainMsgC+="\nFollowing Receipt "+Constants.originalOpeningBalanceBaseAmount+" not matched. Please check originalopeningbalancebaseamount column from Receipt table. \n";
                   plainMsgC+="\nDetails - \n";
                   plainMsgC+="Receipt - "+receipt.getReceiptNumber()+"\n";
                   plainMsgC+="Receipt ID - "+receipt.getID()+"\n";
                   plainMsgC+="Company - "+receipt.getCompany().getCompanyID()+"\n";
                   plainMsgC+="Subdomain - "+receipt.getCompany().getSubDomain()+"\n";
                   plainMsgC+="Stored "+Constants.originalOpeningBalanceBaseAmount+" - "+receipt.getOriginalOpeningBalanceBaseAmount()+"\n";
                   plainMsgC+="Calculated "+Constants.originalOpeningBalanceBaseAmount+" - "+baseAmount+"\n";
                   plainMsgC+="Stored "+Constants.openingBalanceBaseAmountDue+" - "+receipt.getOpeningBalanceBaseAmountDue()+"\n";
                   plainMsgC+="Calculated "+Constants.openingBalanceBaseAmountDue+" - "+baseAmountDue+"\n";
                   HashMap<String, Object> hm = new HashMap();
                   hm.put("receiptid", receipt.getID());
                   hm.put(Constants.originalOpeningBalanceBaseAmount, baseAmount);
                   hm.put(Constants.openingBalanceBaseAmountDue, baseAmountDue);
                   accReceiptobj.saveReceipt(hm);
                   if(sendMail) {
                       try {
                           String fromEmailId = (!receipt.getCompany().isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(receipt.getCompany().getCompanyID());
                           Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(receipt.getCompany());
                           SendMailHandler.postMail(emails, "[Deskera ERP] "+Constants.originalOpeningBalanceBaseAmount+" not matched", plainMsgC,plainMsgC, fromEmailId, "Sagar", smtpConfigMap);
                       } catch (Exception ex) {
                           System.out.println(plainMsgC);
                       }
                   }
               }
           }
           msg = "Receipt - Records processed successfully. Total Records - "+receiptList.size();
        } catch(Exception ex) {
            ex.printStackTrace();
        } finally {
            System.out.println(msg);
        }
    }
    
    private void reCalculateOpeningBalanceAmountForMP(String cid, boolean sendMail, String[] emails) {
        String msg = "";
        HashMap<String, Object> requestParams = new HashMap();
        try {
            requestParams.put(Constants.companyKey, cid);
            KwlReturnObject paymentresult = accVendorPaymentobj.getOpeningBalancePayments(requestParams);
            List<Payment> paymentList = paymentresult.getEntityList();
            for(Payment payment : paymentList) {
               if(!requestParams.containsKey("gcurrencyid")) {
                   requestParams.put("gcurrencyid", payment.getCompany().getCurrency().getCurrencyID());
                   requestParams.put("companyid", payment.getCompany().getCompanyID());
               }
                double externalCurrencyRate = 0d;
                boolean isopeningBalancePayment = payment.isIsOpeningBalencePayment();
                double amount = 0d,amountDue=0d, baseAmount=0d, baseAmountDue=0d;
                Date receiptCreationDate = null;
//                    Receipt receipt = (Receipt) itr.next();
                if (!payment.isNormalPayment() && payment.isIsOpeningBalencePayment()) {
                    amount = payment.getDepositAmount();
                    amountDue = payment.getOpeningBalanceAmountDue();
                    receiptCreationDate = payment.getCreationDate();
                    externalCurrencyRate = payment.getExchangeRateForOpeningTransaction();
                }
                String fromcurrencyid = payment.getCurrency().getCurrencyID();
                KwlReturnObject bAmt = null;
                if (isopeningBalancePayment && payment.isConversionRateFromCurrencyToBase()) {// if payment is opening balance payment and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amount, fromcurrencyid, receiptCreationDate, externalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amount, fromcurrencyid, receiptCreationDate, externalCurrencyRate);
                }
                baseAmount = (Double) bAmt.getEntityList().get(0);
                
                bAmt = null;
                if (isopeningBalancePayment && payment.isConversionRateFromCurrencyToBase()) {// if payment is opening balance payment and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, receiptCreationDate, externalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, receiptCreationDate, externalCurrencyRate);
                }
                baseAmountDue = (Double) bAmt.getEntityList().get(0);
               if(baseAmount!=payment.getOriginalOpeningBalanceBaseAmount() || baseAmountDue!=payment.getOpeningBalanceBaseAmountDue()) {
                   String plainMsgC = "";
                   plainMsgC+="\nHello Admin\n";  
                   plainMsgC+="\nFollowing Payment "+Constants.originalOpeningBalanceBaseAmount+" not matched. Please check originalopeningbalancebaseamount column from Receipt table. \n";
                   plainMsgC+="\nDetails - \n";
                   plainMsgC+="Payment - "+payment.getPaymentNumber()+"\n";
                   plainMsgC+="Payment ID - "+payment.getID()+"\n";
                   plainMsgC+="Company - "+payment.getCompany().getCompanyID()+"\n";
                   plainMsgC+="Subdomain - "+payment.getCompany().getSubDomain()+"\n";
                   plainMsgC+="Stored "+Constants.originalOpeningBalanceBaseAmount+" - "+payment.getOriginalOpeningBalanceBaseAmount()+"\n";
                   plainMsgC+="Calculated "+Constants.originalOpeningBalanceBaseAmount+" - "+baseAmount+"\n";
                   plainMsgC+="Stored "+Constants.openingBalanceBaseAmountDue+" - "+payment.getOpeningBalanceBaseAmountDue()+"\n";
                   plainMsgC+="Calculated "+Constants.openingBalanceBaseAmountDue+" - "+baseAmountDue+"\n";
                   HashMap<String, Object> hm = new HashMap();
                   hm.put("paymentid", payment.getID());
                   hm.put(Constants.originalOpeningBalanceBaseAmount, baseAmount);
                   hm.put(Constants.openingBalanceBaseAmountDue, baseAmountDue);
                   accVendorPaymentobj.savePayment(hm);
                   if(sendMail) {
                       try {
                           String fromEmailId = (!payment.getCompany().isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(payment.getCompany().getCompanyID());
                           Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(payment.getCompany());
                           SendMailHandler.postMail(emails, "[Deskera ERP] "+Constants.originalOpeningBalanceBaseAmount+" not matched", plainMsgC,plainMsgC, fromEmailId, "Sagar", smtpConfigMap);
                       } catch (Exception ex) {
                           System.out.println(plainMsgC);
                       }
                   }
               }
           }
           msg = "Payment - Records processed successfully. Total Records - "+paymentList.size();
        } catch(Exception ex) {
            ex.printStackTrace();
        } finally {
            System.out.println(msg);
        }
    }
    
    private void reCalculateOpeningBalanceAmountForCN(String cid, boolean sendMail, String[] emails) {
        String msg = "";
        HashMap<String, Object> requestParams = new HashMap();
        try {
            requestParams.put(Constants.companyKey, cid);
            KwlReturnObject cnResult = accCreditNoteobj.getOpeningBalanceCNs(requestParams);
            List<CreditNote> cnList = cnResult.getEntityList();
            for(CreditNote cn : cnList) {
               if(!requestParams.containsKey("gcurrencyid")) {
                   requestParams.put("gcurrencyid", cn.getCompany().getCurrency().getCurrencyID());
                   requestParams.put("companyid", cn.getCompany().getCompanyID());
               }
                double externalCurrencyRate = 0d;
                double amount = 0d,amountDue=0d, baseAmount=0d, baseAmountDue=0d;
                boolean isopeningBalanceCN = cn.isIsOpeningBalenceCN();
                Date cnCreationDate = null;
                if (!cn.isNormalCN() && cn.isIsOpeningBalenceCN()) {
                    amount = cn.getCnamount();
                    amountDue = cn.getOpeningBalanceAmountDue();
                    cnCreationDate = cn.getCreationDate();
                    externalCurrencyRate = cn.getExchangeRateForOpeningTransaction();
                }
                String fromcurrencyid = cn.getCurrency().getCurrencyID();
                KwlReturnObject bAmt = null;
                if (isopeningBalanceCN && cn.isConversionRateFromCurrencyToBase()) {// if CN is opening balance CN and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amount, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amount, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                }
                baseAmount = (Double) bAmt.getEntityList().get(0);
                
                bAmt = null;
                if (isopeningBalanceCN && cn.isConversionRateFromCurrencyToBase()) {// if CN is opening balance CN and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                }
                baseAmountDue = (Double) bAmt.getEntityList().get(0);
               if(baseAmount!=cn.getOriginalOpeningBalanceBaseAmount() || baseAmountDue!=cn.getOpeningBalanceBaseAmountDue()) {
                   String plainMsgC = "";
                   plainMsgC+="\nHello Admin\n";  
                   plainMsgC+="\nFollowing CreditNote "+Constants.originalOpeningBalanceBaseAmount+" not matched. Please check originalopeningbalancebaseamount column from Receipt table. \n";
                   plainMsgC+="\nDetails - \n";
                   plainMsgC+="CreditNote - "+cn.getCreditNoteNumber()+"\n";
                   plainMsgC+="CreditNote ID - "+cn.getID()+"\n";
                   plainMsgC+="Company - "+cn.getCompany().getCompanyID()+"\n";
                   plainMsgC+="Subdomain - "+cn.getCompany().getSubDomain()+"\n";
                   plainMsgC+="Stored "+Constants.originalOpeningBalanceBaseAmount+" - "+cn.getOriginalOpeningBalanceBaseAmount()+"\n";
                   plainMsgC+="Calculated "+Constants.originalOpeningBalanceBaseAmount+" - "+baseAmount+"\n";
                   plainMsgC+="Stored "+Constants.openingBalanceBaseAmountDue+" - "+cn.getOpeningBalanceBaseAmountDue()+"\n";
                   plainMsgC+="Calculated "+Constants.openingBalanceBaseAmountDue+" - "+baseAmountDue+"\n";
                   HashMap<String, Object> hm = new HashMap();
                   hm.put("cnid", cn.getID());
                   hm.put(Constants.originalOpeningBalanceBaseAmount, baseAmount);
                   hm.put(Constants.openingBalanceBaseAmountDue, baseAmountDue);
                   accCreditNoteobj.updateCreditNote(hm);
                   if(sendMail) {
                       try {
                           String fromEmailId = (!cn.getCompany().isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(cn.getCompany().getCompanyID());
                           Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(cn.getCompany());
                           SendMailHandler.postMail(emails, "[Deskera ERP] "+Constants.originalOpeningBalanceBaseAmount+" not matched", plainMsgC,plainMsgC, fromEmailId, "Sagar", smtpConfigMap);
                       } catch (Exception ex) {
                           System.out.println(plainMsgC);
                       }
                   }
               }
           }
            
            cnResult = accCreditNoteobj.getOpeningBalanceVendorCNs(requestParams);
            cnList = cnResult.getEntityList();
            for(CreditNote cn : cnList) {
               if(!requestParams.containsKey("gcurrencyid")) {
                   requestParams.put("gcurrencyid", cn.getCompany().getCurrency().getCurrencyID());
                   requestParams.put("companyid", cn.getCompany().getCompanyID());
               }
                double externalCurrencyRate = 0d;
                double amount = 0d,amountDue=0d, baseAmount=0d, baseAmountDue=0d;
                boolean isopeningBalanceCN = cn.isIsOpeningBalenceCN();
                Date cnCreationDate = null;
                if (!cn.isNormalCN() && cn.isIsOpeningBalenceCN()) {
                    amount = cn.getCnamount();
                    amountDue = cn.getOpeningBalanceAmountDue();
                    cnCreationDate = cn.getCreationDate();
                    externalCurrencyRate = cn.getExchangeRateForOpeningTransaction();
                }
                String fromcurrencyid = cn.getCurrency().getCurrencyID();
                KwlReturnObject bAmt = null;
                if (isopeningBalanceCN && cn.isConversionRateFromCurrencyToBase()) {// if CN is opening balance CN and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amount, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amount, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                }
                baseAmount = (Double) bAmt.getEntityList().get(0);
                
                bAmt = null;
                if (isopeningBalanceCN && cn.isConversionRateFromCurrencyToBase()) {// if CN is opening balance CN and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                }
                baseAmountDue = (Double) bAmt.getEntityList().get(0);
               if(baseAmount!=cn.getOriginalOpeningBalanceBaseAmount() || baseAmountDue!=cn.getOpeningBalanceBaseAmountDue()) {
                   String plainMsgC = "";
                   plainMsgC+="\nHello Admin\n";  
                   plainMsgC+="\nFollowing CreditNote "+Constants.originalOpeningBalanceBaseAmount+" not matched. Please check originalopeningbalancebaseamount column from Receipt table. \n";
                   plainMsgC+="\nDetails - \n";
                   plainMsgC+="CreditNote - "+cn.getCreditNoteNumber()+"\n";
                   plainMsgC+="CreditNote ID - "+cn.getID()+"\n";
                   plainMsgC+="Company - "+cn.getCompany().getCompanyID()+"\n";
                   plainMsgC+="Subdomain - "+cn.getCompany().getSubDomain()+"\n";
                   plainMsgC+="Stored "+Constants.originalOpeningBalanceBaseAmount+" - "+cn.getOriginalOpeningBalanceBaseAmount()+"\n";
                   plainMsgC+="Calculated "+Constants.originalOpeningBalanceBaseAmount+" - "+baseAmount+"\n";
                   plainMsgC+="Stored "+Constants.openingBalanceBaseAmountDue+" - "+cn.getOpeningBalanceBaseAmountDue()+"\n";
                   plainMsgC+="Calculated "+Constants.openingBalanceBaseAmountDue+" - "+baseAmountDue+"\n";
                   HashMap<String, Object> hm = new HashMap();
                   hm.put("cnid", cn.getID());
                   hm.put(Constants.originalOpeningBalanceBaseAmount, baseAmount);
                   hm.put(Constants.openingBalanceBaseAmountDue, baseAmountDue);
                   accCreditNoteobj.updateCreditNote(hm);
                   if(sendMail) {
                       try {
                           String fromEmailId = (!cn.getCompany().isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(cn.getCompany().getCompanyID());
                           Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(cn.getCompany());
                           SendMailHandler.postMail(emails, "[Deskera ERP] "+Constants.originalOpeningBalanceBaseAmount+" not matched", plainMsgC,plainMsgC, fromEmailId, "Sagar", smtpConfigMap);
                       } catch (Exception ex) {
                           System.out.println(plainMsgC);
                       }
                   }
               }
           } 
           msg = "CreditNote - Records processed successfully. Total Records - "+cnList.size();
        } catch(Exception ex) {
            ex.printStackTrace();
        } finally {
            System.out.println(msg);
        }
    }
    
    private void reCalculateOpeningBalanceAmountForDN(String cid, boolean sendMail, String[] emails) {
        String msg = "";
        HashMap<String, Object> requestParams = new HashMap();
        try {
            requestParams.put(Constants.companyKey, cid);
            KwlReturnObject dnResult = accDebitNoteobj.getOpeningBalanceDNs(requestParams);
            List<DebitNote> dnList = dnResult.getEntityList();
            for(DebitNote dn : dnList) {
               if(!requestParams.containsKey("gcurrencyid")) {
                   requestParams.put("gcurrencyid", dn.getCompany().getCurrency().getCurrencyID());
                   requestParams.put("companyid", dn.getCompany().getCompanyID());
               }
                double externalCurrencyRate = 0d;
                double amount = 0d,amountDue=0d, baseAmount=0d, baseAmountDue=0d;
                boolean isopeningBalanceCN = dn.isIsOpeningBalenceDN();
                Date cnCreationDate = null;
                if (!dn.isNormalDN() && dn.isIsOpeningBalenceDN()) {
                    amount = dn.getDnamount();
                    amountDue = dn.getOpeningBalanceAmountDue();
                    cnCreationDate = dn.getCreationDate();
                    externalCurrencyRate = dn.getExchangeRateForOpeningTransaction();
                }
                String fromcurrencyid = dn.getCurrency().getCurrencyID();
                KwlReturnObject bAmt = null;
                if (isopeningBalanceCN && dn.isConversionRateFromCurrencyToBase()) {// if CN is opening balance CN and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amount, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amount, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                }
                baseAmount = (Double) bAmt.getEntityList().get(0);
                
                bAmt = null;
                if (isopeningBalanceCN && dn.isConversionRateFromCurrencyToBase()) {// if CN is opening balance CN and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                }
                baseAmountDue = (Double) bAmt.getEntityList().get(0);
               if(baseAmount!=dn.getOriginalOpeningBalanceBaseAmount()  || baseAmountDue!=dn.getOpeningBalanceBaseAmountDue()) {
                   String plainMsgC = "";
                   plainMsgC+="\nHello Admin\n";  
                   plainMsgC+="\nFollowing DebitNote "+Constants.originalOpeningBalanceBaseAmount+" not matched. Please check originalopeningbalancebaseamount column from Receipt table. \n";
                   plainMsgC+="\nDetails - \n";
                   plainMsgC+="DebitNote - "+dn.getDebitNoteNumber()+"\n";
                   plainMsgC+="DebitNote ID - "+dn.getID()+"\n";
                   plainMsgC+="Company - "+dn.getCompany().getCompanyID()+"\n";
                   plainMsgC+="Subdomain - "+dn.getCompany().getSubDomain()+"\n";
                   plainMsgC+="Stored "+Constants.originalOpeningBalanceBaseAmount+" - "+dn.getOriginalOpeningBalanceBaseAmount()+"\n";
                   plainMsgC+="Calculated "+Constants.originalOpeningBalanceBaseAmount+" - "+baseAmount+"\n";
                   plainMsgC+="Stored "+Constants.openingBalanceBaseAmountDue+" - "+dn.getOpeningBalanceBaseAmountDue()+"\n";
                   plainMsgC+="Calculated "+Constants.openingBalanceBaseAmountDue+" - "+baseAmountDue+"\n";
                   HashMap<String, Object> hm = new HashMap();
                   hm.put("dnid", dn.getID());
                   hm.put(Constants.originalOpeningBalanceBaseAmount, baseAmount);
                   hm.put(Constants.openingBalanceBaseAmountDue, baseAmountDue);
                   accDebitNoteobj.updateDebitNote(hm);
                   if(sendMail) {
                       try {
                           String fromEmailId = (!dn.getCompany().isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(dn.getCompany().getCompanyID());
                           Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(dn.getCompany());
                           SendMailHandler.postMail(emails, "[Deskera ERP] "+Constants.originalOpeningBalanceBaseAmount+" not matched", plainMsgC,plainMsgC, fromEmailId, "Sagar", smtpConfigMap);
                       } catch (Exception ex) {
                           System.out.println(plainMsgC);
                       }
                   }
               }
           }
            
            dnResult = accDebitNoteobj.getOpeningBalanceCustomerDNs(requestParams);
            dnList = dnResult.getEntityList();
            for(DebitNote dn : dnList) {
               if(!requestParams.containsKey("gcurrencyid")) {
                   requestParams.put("gcurrencyid", dn.getCompany().getCurrency().getCurrencyID());
                   requestParams.put("companyid", dn.getCompany().getCompanyID());
               }
                double externalCurrencyRate = 0d;
                double amount = 0d,amountDue=0d, baseAmount=0d, baseAmountDue=0d;
                boolean isopeningBalanceCN = dn.isIsOpeningBalenceDN();
                Date cnCreationDate = null;
                if (!dn.isNormalDN() && dn.isIsOpeningBalenceDN()) {
                    amount = dn.getDnamount();
                    amountDue = dn.getOpeningBalanceAmountDue();
                    cnCreationDate = dn.getCreationDate();
                    externalCurrencyRate = dn.getExchangeRateForOpeningTransaction();
                }
                String fromcurrencyid = dn.getCurrency().getCurrencyID();
                KwlReturnObject bAmt = null;
                if (isopeningBalanceCN && dn.isConversionRateFromCurrencyToBase()) {// if CN is opening balance CN and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amount, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amount, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                }
                baseAmount = (Double) bAmt.getEntityList().get(0);
                
                bAmt = null;
                if (isopeningBalanceCN && dn.isConversionRateFromCurrencyToBase()) {// if CN is opening balance CN and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
                }
                baseAmountDue = (Double) bAmt.getEntityList().get(0);
               if(baseAmount!=dn.getOriginalOpeningBalanceBaseAmount()  || baseAmountDue!=dn.getOpeningBalanceBaseAmountDue()) {
                   String plainMsgC = "";
                   plainMsgC+="\nHello Admin\n";  
                   plainMsgC+="\nFollowing DebitNote "+Constants.originalOpeningBalanceBaseAmount+" not matched. Please check originalopeningbalancebaseamount column from Receipt table. \n";
                   plainMsgC+="\nDetails - \n";
                   plainMsgC+="DebitNote - "+dn.getDebitNoteNumber()+"\n";
                   plainMsgC+="DebitNote ID - "+dn.getID()+"\n";
                   plainMsgC+="Company - "+dn.getCompany().getCompanyID()+"\n";
                   plainMsgC+="Subdomain - "+dn.getCompany().getSubDomain()+"\n";
                   plainMsgC+="Stored "+Constants.originalOpeningBalanceBaseAmount+" - "+dn.getOriginalOpeningBalanceBaseAmount()+"\n";
                   plainMsgC+="Calculated "+Constants.originalOpeningBalanceBaseAmount+" - "+baseAmount+"\n";
                   plainMsgC+="Stored "+Constants.openingBalanceBaseAmountDue+" - "+dn.getOpeningBalanceBaseAmountDue()+"\n";
                   plainMsgC+="Calculated "+Constants.openingBalanceBaseAmountDue+" - "+baseAmountDue+"\n";
                   HashMap<String, Object> hm = new HashMap();
                   hm.put("dnid", dn.getID());
                   hm.put(Constants.originalOpeningBalanceBaseAmount, baseAmount);
                   hm.put(Constants.openingBalanceBaseAmountDue, baseAmountDue);
                   accDebitNoteobj.updateDebitNote(hm);
                   if(sendMail) {
                       try {
                           String fromEmailId = (!dn.getCompany().isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(dn.getCompany().getCompanyID());
                           Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(dn.getCompany());
                           SendMailHandler.postMail(emails, "[Deskera ERP] "+Constants.originalOpeningBalanceBaseAmount+" not matched", plainMsgC,plainMsgC, fromEmailId, "Sagar", smtpConfigMap);
                       } catch (Exception ex) {
                           System.out.println(plainMsgC);
                       }
                   }
               }
           }
           msg = "DebitNote - Records processed successfully. Total Records - "+dnList.size();
        } catch(Exception ex) {
            ex.printStackTrace();
        } finally {
            System.out.println(msg);
        }
    }
    
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            if (appStarted) {
                return;
            }
            appStarted = true;
            try {
//                setTimedTask();
//                Constants.conectionTimer.schedule(Constants.connectionTimerTask, 0, 1800000);
                JRProperties.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
//                Map<String, Object> filterParams = new HashMap<String, Object>();
//                Constants.exchangeRateDetailsAttributes = (HashMap<String, Attribute<ExchangeRateDetailInfo, Comparable>>) DynamicIndexer.generateAttributesForPojo(ExchangeRateDetailInfo.class);
//                List<ExchangeRate> exRateList = accCurrencyDAOobj.getCurrencyExchange(filterParams).getEntityList();
//                Constants.exchangeRateAttributes = (HashMap<String, Attribute<ExchangeRateInfo, Comparable>>) DynamicIndexer.generateAttributesForPojo(ExchangeRateInfo.class);
//                Constants.exchangeRateInfo = DynamicIndexer.newAutoIndexedCollection(Constants.exchangeRateAttributes.values());
//                for (ExchangeRate obj : exRateList) {
//                    ExchangeRateInfo info = new ExchangeRateInfo();
//                    info.setID(obj.getID());
//                    info.setFromCurrency(obj.getFromCurrency().getCurrencyID());
//                    info.setToCurrency(obj.getToCurrency().getCurrencyID());
//                    Constants.exchangeRateInfo.add(info);
//                }
                executeSqlProcedures();
            } catch (Exception e) {
                Logger.getLogger(CommonFunctions.class.getName()).log(Level.SEVERE, null, e);
            } 
        }
    }

    private void executeSqlProcedures() {
        try {
            String newId = StringUtil.generateUUID();
            Map<Object, Object> targetDataSources = routingDataSource.getTargetDataSources();

            String filePath = "/sqlscript/copycompanydata.sql"; // accessing file from Resources folder
            URL url = getClass().getResource(filePath);
            if (url == null) {
                return;
            }
            String folderUrlPath = url.toString().substring(url.toString().indexOf("/"), url.toString().lastIndexOf("/"));
            File f = new File(folderUrlPath);

            for (Object entry : targetDataSources.entrySet()) {
                Map.Entry<Object, Object> e = (Map.Entry<Object, Object>) entry;
                JdbcTemplate template = new JdbcTemplate(routingDataSource.getDataSourceFromKey(e.getValue()));
                DataSource ds = routingDataSource.getDataSourceFromKey(e.getValue());

                DbSupport support = DbSupportFactory.createDbSupport(ds.getConnection(), false);

                List<File> sqlFiles = new ArrayList<File>();
                collectSqlFiles(f, sqlFiles);

                for (File sqlFile : sqlFiles) {
                    Resource resource = new FileSystemResource(sqlFile.getAbsolutePath());
                    SqlScript script = new SqlScript(resource.loadAsString("UTF-8"), support);
                    try {
                        script.execute(support.getJdbcTemplate());
                    } catch (Exception ex) {
                        Logger.getLogger(CommonFunctions.class.getName()).log(Level.SEVERE, null, ex);
                        continue;
                    }
                }

                String tableSchema = ds.getConnection().getCatalog();
                SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(template).withProcedureName("droplike");
                Map<String, Object> inParamMap = new HashMap<String, Object>();
                inParamMap.put("pattern", "IL_%");
                inParamMap.put("databasename", tableSchema);
                SqlParameterSource in = new MapSqlParameterSource(inParamMap);
                Map<String, Object> simpleJdbcCallResult = simpleJdbcCall.execute(in);
            }

        } catch (Exception ex) {
            Logger.getLogger(CommonFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void collectSqlFiles(File directory, List<File> collectedFiles) {

        for (String fileName : directory.list()) {
            File file = new File(directory, fileName);
            if (file.isDirectory()) {
                collectSqlFiles(file, collectedFiles);
            } else {
                if (fileName.endsWith(".sql")) {
                    collectedFiles.add(file);
                }
            }
        }
    }

    private void sendProductExpiryNotificationMail(String companyId, String companyName, String baseCurrencyId, String userFname, String userLname, String[] userMailid, String mailContent, String mailSubject, int beforeAfter, int days,String baseurl) { // 0-before , 1 after 
        KwlReturnObject kwlReturnObj = null;
        try {
            Date todayDate = new Date();
            DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            DateFormat dbformat = new SimpleDateFormat("yyyy-MM-dd");
            String today = formatter.format(todayDate);
            int duration = days;
            ArrayList<CustomDesignLineItemProp> headerlist = new ArrayList();

            todayDate.setHours(0);
            todayDate.setMinutes(0);
            todayDate.setSeconds(0);
            
            Calendar currentSystemDate = Calendar.getInstance();
            currentSystemDate.setTime(todayDate);

            List finalData = new ArrayList();

            List headerItems = new ArrayList();
            headerItems.add("No.");
            headerItems.add("Product Code");
            headerItems.add("Product Name");
            headerItems.add("Product Serial No.");
            headerItems.add("Expiry Date");


            String htmlText = "";
            String subject = mailSubject;
            String plainMsg = "";
            //String from = Constants.ADMIN_EMAILID;
            KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
            Company company = (Company) returnObject.getEntityList().get(0);
            String from = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
            htmlText = mailContent.replace(Constants.DateLabel_MailContent_Placeholder, today);
            htmlText = htmlText.replace(Constants.TodayDateLabel_MailContent_Placeholder, today);

            if (userMailid.length > 0) {
                
                int sno = 1;

                kwlReturnObj = accountingHandlerDAOobj.getProductExpiryList(companyId);
                List list = kwlReturnObj.getEntityList();
                Iterator iterator = list.iterator();

                while (iterator.hasNext()) {
                    Object[] obj = (Object[]) iterator.next();
                    if (obj != null) {
                        List data = new ArrayList();

                        Date dbExpDate = (Date) obj[3];

                        Calendar prodExpiryDate = Calendar.getInstance();
                        prodExpiryDate.setTime(dbExpDate);
                        prodExpiryDate.add(Calendar.DATE, (beforeAfter == 0 ? -duration : duration));
                        String expiryDateInString=formatter.format(prodExpiryDate.getTime());
                        
                        //if(prodExpiryDate.getTime() == currentSystemDate.getTime()){
                       // if(prodExpiryDate.compareTo(currentSystemDate) == 0){
                        if(expiryDateInString.equals(today)){
                            data.add(sno);
                            data.add(obj[0]); //product code
                            data.add(obj[1]); //product name
                            data.add(obj[2]); // serial
                            data.add(obj[3]); // expiry date
                            finalData.add(data);
                            sno++;
                        }
                    }
                }


                if (sno>1) {
                    for (Object header : headerItems) {
                        CustomDesignLineItemProp headerprop = new CustomDesignLineItemProp();
                        String a = header.toString();
                        headerprop.setAlign("left");
                        headerprop.setData(a);
                        headerprop.setWidth("50px");
                        headerlist.add(headerprop);
                    }
                    List finalProductList = new ArrayList();
                    for (Object headerdata : finalData) {
                        ArrayList<CustomDesignLineItemProp> prodlist = new ArrayList();
                        List datalist = (List) headerdata;
                        for (Object hdata : datalist) {
                            CustomDesignLineItemProp prop = new CustomDesignLineItemProp();
                            prop.setAlign("left");
                            prop.setData(hdata.toString());
                            prodlist.add(prop);
                        }
                        finalProductList.add(prodlist);
                    }
                    String top = "10px", left = "10px", tablewidth = CustomDesignHandler.pageWidth;
                    StringWriter writer = new StringWriter();
                    VelocityEngine ve = new VelocityEngine();
                    ve.init();
                    VelocityContext context = new VelocityContext();
                    context.put("tableHeader", headerlist);
                    context.put("prodList", finalProductList);
                    context.put("top", top);
                    context.put("left", left);
                    context.put("width", tablewidth);
                    velocityEngine.mergeTemplate("duemailitems.vm", "UTF-8", context, writer);
                    String tablehtml = writer.toString();
                    htmlText = htmlText.concat(tablehtml);
                    htmlText+=baseurl;
                    Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                    SendMailHandler.postMail(userMailid, subject, htmlText, plainMsg, from, smtpConfigMap);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
     
    public ModelAndView findForeignGainLossForCreditNote(HttpServletRequest request, HttpServletResponse response) {
        String msg = "False";
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String cid = request.getParameter("cid");
            if (!StringUtil.isNullOrEmpty(cid)) {
                KwlReturnObject resultCompany = accountingHandlerDAOobj.getObject(Company.class.getName(), cid);
                Company company = (Company) resultCompany.getEntityList().get(0);
                String currencyid = company.getCurrency().getCurrencyID();
                HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
                GlobalParams.put("companyid", cid);
                GlobalParams.put("gcurrencyid", currencyid);
//                GlobalParams.put("dateformat", authHandler.getDateOnlyFormatter(request));
                SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy");//Change It before Commiting the code
                GlobalParams.put("dateformat", sdf);
                GlobalParams.put("df", sdf);
//                Date creationDate = authHandler.getDateOnlyFormatter(request).parse(request.getParameter("creationdate"));
                List<Payment> payments = accVendorPaymentobj.getPaymentListFromCompany(cid);
                CompanyAccountPreferences pref = null;
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), cid);
                pref = (CompanyAccountPreferences) returnObject.getEntityList().get(0);
                Account forexAccount = pref.getForeignexchange();
                boolean forex = true;
                for (Payment payment : payments) {
                    double externalCurrencyRate = 0d;
                    forex = true;double forexgainloss = 0;
                    Date creationDate = null;
                    if (payment.isNormalPayment()) {
                        externalCurrencyRate = payment.getExternalCurrencyRate();
//                        creationDate = payment.getJournalEntry().getEntryDate();
                        creationDate = payment.getCreationDate();
                        for (JournalEntryDetail entryDetail : payment.getJournalEntry().getDetails()) {
                            if (entryDetail.getAccount().getID().equals(forexAccount.getID())) {
                                forex = false;
                            }
                        }
                    } else {
                        externalCurrencyRate = payment.getExchangeRateForOpeningTransaction();
                        creationDate = payment.getCreationDate();
                    }
                    JSONArray jArr = new JSONArray();
                    getPaymentCNDNArray(payment, jArr, companyid);
                    for (int i = 0; i < jArr.length(); i++) {
                        JSONObject jobj = jArr.getJSONObject(i);
                        String transactionCurrencyId = jobj.getString("currencyidtransaction");
                        if (!StringUtil.isNullOrEmpty(transactionCurrencyId) && !transactionCurrencyId.equals(payment.getCurrency().getCurrencyID())) {
//                            forexgainloss = cndnPaymentForexGailLossAmount(request, jobj, payment, transactionCurrencyId, currencyid, externalCurrencyRate);
                        } else if (transactionCurrencyId.equals(payment.getCurrency().getCurrencyID())) {
                            forexgainloss = cndnPaymentForexGailLossAmountForSameCurrency(request, jobj, payment, transactionCurrencyId, currencyid, externalCurrencyRate);
                        }
                        double actualAmount = forexgainloss;
                        if (forexgainloss != 0d && forex) {
                            if (msg.equals("False")) {
                                boolean rateDecreased = false;
                                if (actualAmount < 0) {
                                    rateDecreased = true;
                                }
                                actualAmount = (-1 * actualAmount);
                            } else {
                                boolean rateDecreased = false;
                                if (actualAmount < 0) {
                                    rateDecreased = true;
                                }
                                 actualAmount = (-1 * actualAmount);
                            }
                        }
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountRoundOff(GlobalParams, actualAmount, currencyid, creationDate, externalCurrencyRate);
                        msg+= "\n" + payment.getPaymentNumber() + " actualAmount: " + actualAmount + " baseAmount: " + bAmt.getEntityList().get(0);
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("------ Error at findForeignGainLossForCreditNote() ----------");
            ex.printStackTrace();
        }
        if (!msg.equals("False")) {
            System.out.println(msg);
        }
        return new ModelAndView("jsonView_ex", "model", msg);
    }
    
    private void getPaymentCNDNArray(Payment re, JSONArray innerJArrCNDN, String companyid) throws SessionExpiredException, ServiceException {
        try {
            KwlReturnObject cndnResult = accVendorPaymentobj.getVendorCnPayment(re.getID());
            List<CreditNotePaymentDetails> cnpdList = cndnResult.getEntityList();
            for (CreditNotePaymentDetails cnpd:cnpdList) {
                Double cnpaidamount = cnpd.getAmountPaid();
                Double cnPaidAmountPaymentCurrency = cnpd.getPaidAmountInPaymentCurrency();
                Double exchangeratefortransaction = cnpd.getExchangeRateForTransaction();//for documentdesigner
                String description = cnpd.getDescription()!=null?cnpd.getDescription():"";//for documentdesigner
                CreditNote creditNote = cnpd.getCreditnote();
                JSONObject obj = new JSONObject();
                obj.put("transectionno", creditNote.getCreditNoteNumber());
                obj.put("transectionid", creditNote.getID());
                if (creditNote.getVendor() != null) {
                    obj.put("accountid", creditNote.getVendor().getID());
                    obj.put("accountname", creditNote.getVendor().getName());
                } else if (creditNote.getCustomer() != null) {
                    obj.put("accountid", creditNote.getCustomer().getID());
                    obj.put("accountname", creditNote.getCustomer().getName());
                }
                if (creditNote != null) {
                    obj.put("currencyidtransaction", creditNote.getCurrency() == null ? re.getCurrency().getCurrencyID() : creditNote.getCurrency().getCurrencyID());
                } else {
                    obj.put("currencyidtransaction", re.getCurrency().getCurrencyID());
                }
                obj.put("totalamount", creditNote.getCnamount());
                obj.put("enteramount", cnPaidAmountPaymentCurrency);
                obj.put("amountdue", creditNote.getCnamountdue());
                obj.put("exchangeratefortransaction", exchangeratefortransaction);//for documentdesigner
                obj.put("newamountdue", authHandler.round((creditNote.getCnamountdue() * exchangeratefortransaction), companyid) + cnPaidAmountPaymentCurrency);//for documentdesigner
                obj.put("description", description);//for documentdesigner
                obj.put("cnpaidamount", cnpaidamount);
                obj.put("type", Constants.PaymentAgainstCNDN);
                innerJArrCNDN.put(obj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentController.getPaymentCNDNArray : " + ex.getMessage(), ex);
        }
    }
   
    public double cndnPaymentForexGailLossAmountForSameCurrency(HttpServletRequest request, JSONObject jobj, Payment payment, String transactionCurrencyId, String currencyid, double externalCurrencyRate) throws ServiceException, SessionExpiredException, JSONException {
        double amount = 0, actualAmount = 0;
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
//        requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
//        requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
//        requestParams.put("dateformat", authHandler.getDateOnlyFormatter(request));
        double enterAmountPaymentCurrencyOld = 0;
        double enterAmountTrancastionCurrencyNew = 0;
        double amountdiff = 0;
        try {

            double exchangeRate = 0d;
            double newrate = 0.0;
            double ratio = 0;
            Double enteramount = jobj.getDouble("enteramount");
            String documentId = jobj.getString("transectionid");
            KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), documentId);
            CreditNote creditMemo = (CreditNote) resultObject.getEntityList().get(0);
            JournalEntry je = creditMemo.getJournalEntry();
            Date creditNoteDate = null;
            boolean isopeningBalanceInvoice = creditMemo.isIsOpeningBalenceCN();
            if (creditMemo.isNormalCN()) {
                je = creditMemo.getJournalEntry();
//                creditNoteDate = je.getEntryDate();
                exchangeRate = je.getExternalCurrencyRate();
            } else {
                exchangeRate =creditMemo.getExchangeRateForOpeningTransaction();
                if(creditMemo.isConversionRateFromCurrencyToBase()) {
                   exchangeRate = 1 / exchangeRate;
                }
            }
            creditNoteDate = creditMemo.getCreationDate();
            double paymentExternalCurrencyRate=StringUtil.isNullOrEmpty(request.getParameter("externalcurrencyrate"))?1.0:Double.parseDouble(request.getParameter("externalcurrencyrate"));
                            
            KwlReturnObject bAmt = null;
            if(exchangeRate!=paymentExternalCurrencyRate){
                bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(requestParams, 1.0, transactionCurrencyId, currencyid, creditNoteDate, exchangeRate,paymentExternalCurrencyRate);
            }else{
                bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, 1.0, transactionCurrencyId, currencyid, creditNoteDate, exchangeRate);
            }
            double oldrate = (Double) bAmt.getEntityList().get(0);
            newrate = jobj.optDouble("exchangeratefortransaction", 0.0);
            amount = (enteramount - (enteramount / newrate) * oldrate) / newrate;
            KwlReturnObject bAmtActual = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, amount, currencyid, creditNoteDate, newrate);
            actualAmount += (Double) bAmtActual.getEntityList().get(0);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("cndnPaymentForexGailLossAmountForSameCurrency : " + ex.getMessage(), ex);
        }
        return (actualAmount);
    }
    
    public ModelAndView updatePaymentCurrencyToPaymentMethodCurrencyRate(HttpServletRequest request , HttpServletResponse response) throws ServiceException{
        String msg="False";
        try{
            String cid = request.getParameter("cid");
            if(!StringUtil.isNullOrEmpty(cid)){
            double rateToSet=0.0;
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", cid);
            KwlReturnObject cresult=accountingHandlerDAOobj.getObject(Company.class.getName(), cid);
            Company company=(Company)cresult.getEntityList().get(0);
            requestParams.put("gcurrencyid", company.getCurrency().getCurrencyID());
             List<Object> allPaymentsAndReceiptsAndJEs = new ArrayList<Object>();
            
                /*       ===================== Applicable for both Make Payment and Receive Payment ================
                                               *Explaination of the method use* 
                     * Method accCurrencyDAOobj.getBaseToCurrencyAmount() returns the amount in foreign currency from base currency according to transaction date
                     * Therefore we have passed amount=1.0 to get rate between base currency and foreign currency.
                     * Below, baseCurrencyToPaymentCurrencyConvertedAmount is calculated as per sopt rate we provide(it is base currency to payment currency)
                     * And baseCurrencyToPaymentMethodCurrencyConvertedAmount is calculated from payment date
                     * 
                     * For example, if base currency = SGD, Payment currency= USD , Payment method currency= INR. 
                     * Here baseCurrencyToPaymentCurrencyConvertedAmount i.e amount in USD will be according to Spot rate
                     * baseCurrencyToPaymentMethodCurrencyConvertedAmount i.e. amount in INR will be accordingto payment date.
                     * Finally, baseCurrencyToPaymentMethodCurrencyConvertedAmount/baseCurrencyToPaymentMethodCurrencyConvertedAmount will be set as PaymentCurrencyToPaymentMethodCurrencyRate for payment and its journal entry
               */
                
                // Payments
                /*
                 * call method to find such payment which are having payment currency different than payment method currency, but payment currency to payment method currency  rate=1.
                 */
                List paymentList = accVendorPaymentobj.getMulticurrencyPaymentsWithPCToPMCRateOne(cid);
                String allPayments="Payment Made : ";
                for(int i=0; i<paymentList.size();i++){
                    Object row[]=(Object[])paymentList.get(i);
                    KwlReturnObject paymentResult = accountingHandlerDAOobj.getObject(Payment.class.getName(), row[0].toString());
                    Payment payment= (Payment)paymentResult.getEntityList().get(0);
                    
                    String paymentMethodCurrencyId=payment.getPayDetail()==null?"":payment.getPayDetail().getPaymentMethod()==null?"":payment.getPayDetail().getPaymentMethod().getAccount().getCurrency().getCurrencyID();
                    
//                    KwlReturnObject baseCurrencyToPaymentCurrencyResult = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, 1.0, payment.getJournalEntry().getCurrency().getCurrencyID(), payment.getJournalEntry().getEntryDate(), payment.getJournalEntry().getExternalCurrencyRate());
                    KwlReturnObject baseCurrencyToPaymentCurrencyResult = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, 1.0, payment.getJournalEntry().getCurrency().getCurrencyID(), payment.getCreationDate(), payment.getJournalEntry().getExternalCurrencyRate());
                    double baseCurrencyToPaymentCurrencyConvertedAmount = (Double) baseCurrencyToPaymentCurrencyResult.getEntityList().get(0);
                    
//                    KwlReturnObject baseCurrencyToPaymentMethodCurrencyResult = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, 1.0, paymentMethodCurrencyId, payment.getJournalEntry().getEntryDate(), 0);
                    KwlReturnObject baseCurrencyToPaymentMethodCurrencyResult = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, 1.0, paymentMethodCurrencyId, payment.getCreationDate(), 0);
                    double baseCurrencyToPaymentMethodCurrencyConvertedAmount = (Double) baseCurrencyToPaymentMethodCurrencyResult.getEntityList().get(0);
                    
                    rateToSet=baseCurrencyToPaymentMethodCurrencyConvertedAmount/baseCurrencyToPaymentCurrencyConvertedAmount;
                    
                    payment.setPaymentcurrencytopaymentmethodcurrencyrate(rateToSet);   // Value updated for payment
                    payment.getJournalEntry().setPaymentcurrencytopaymentmethodcurrencyrate(rateToSet);// value updated updated for JE of that payment                    
                    
                    allPaymentsAndReceiptsAndJEs.add(payment);
                    allPaymentsAndReceiptsAndJEs.add(payment.getJournalEntry());
                    allPayments+=payment.getPaymentNumber()+", ";
}
                 
                // Receipts
                /*
                 * call method to find such receipts which are having payment currency different than payment method currency, but payment currency to payment method currency rate=1.
                 */
                List receiptList = accReceiptobj.getMulticurrencyReceiptsWithPCToPMCRateOne(cid);
                String allReceipts="Payments Received : ";
                for(int i=0; i<receiptList.size();i++){
                    Object row[]=(Object[])receiptList.get(i);
                    KwlReturnObject receiptResult = accountingHandlerDAOobj.getObject(Receipt.class.getName(), row[0].toString());
                    Receipt receipt= (Receipt)receiptResult.getEntityList().get(0);
                    
                    String paymentMethodCurrencyId=receipt.getPayDetail()==null?"":receipt.getPayDetail().getPaymentMethod()==null?"":receipt.getPayDetail().getPaymentMethod().getAccount().getCurrency().getCurrencyID();
                    
//                    KwlReturnObject baseCurrencyToPaymentCurrencyResult = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, 1.0, receipt.getJournalEntry().getCurrency().getCurrencyID(), receipt.getJournalEntry().getEntryDate(), receipt.getJournalEntry().getExternalCurrencyRate());
                    KwlReturnObject baseCurrencyToPaymentCurrencyResult = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, 1.0, receipt.getJournalEntry().getCurrency().getCurrencyID(), receipt.getCreationDate(), receipt.getJournalEntry().getExternalCurrencyRate());
                    double baseCurrencyToPaymentCurrencyConvertedAmount = (Double) baseCurrencyToPaymentCurrencyResult.getEntityList().get(0);
                    
//                    KwlReturnObject baseCurrencyToPaymentMethodCurrencyResult = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, 1.0, paymentMethodCurrencyId, receipt.getJournalEntry().getEntryDate(), 0);
                    KwlReturnObject baseCurrencyToPaymentMethodCurrencyResult = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, 1.0, paymentMethodCurrencyId, receipt.getCreationDate(), 0);
                    double baseCurrencyToPaymentMethodCurrencyConvertedAmount = (Double) baseCurrencyToPaymentMethodCurrencyResult.getEntityList().get(0);
                    
                    rateToSet=baseCurrencyToPaymentMethodCurrencyConvertedAmount/baseCurrencyToPaymentCurrencyConvertedAmount;
                    
                    receipt.setPaymentcurrencytopaymentmethodcurrencyrate(rateToSet); // Value updated for receipt
                    receipt.getJournalEntry().setPaymentcurrencytopaymentmethodcurrencyrate(rateToSet); // value updated for JE of that receipt
                    
                    allPaymentsAndReceiptsAndJEs.add(receipt);
                    allPaymentsAndReceiptsAndJEs.add(receipt.getJournalEntry());
                    allReceipts+=receipt.getReceiptNumber()+", ";
                }
                accAccountDAOobj.saveOrUpdateAll(allPaymentsAndReceiptsAndJEs);
                msg="Records Processed for subdomain "+company.getSubDomain()+": "+allPayments+" "+allReceipts;
            }
        } catch(Exception e){
            throw ServiceException.FAILURE("getBaseToCurrencyAmount : " + e.getMessage(), e);
        } finally {
            System.out.println(msg);
        }
        return new ModelAndView("jsonView_ex", "model", msg);
    }
    
    public ModelAndView updateExchangeRates(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        String msg = "False";
        try {
           KwlReturnObject companyResult = accountingHandlerDAOobj.getCompanyList();
           List companyList = companyResult.getEntityList();
           Iterator companyIterator = companyList.iterator();
           HashMap currencyMap = new HashMap();
           int i=0;
           KwlReturnObject currencyResult=accCurrencyDAOobj.getCurrencies(currencyMap);
           List currencyList = currencyResult.getEntityList();
           Iterator currencyIterator = currencyList.iterator();         
           DateFormat datef=authHandler.getDateOnlyFormat();
           
           
           while(companyIterator.hasNext()){
               
               Company company = (Company)companyIterator.next(); //(Company)companyObjResult.getEntityList().get(0);               
               KWLCurrency companyCurrency = company.getCurrency();
               String companyBaseCurrencyId = companyCurrency.getCurrencyID();
               String companyBaseCurrencyCode = companyCurrency.getCurrencyCode();
               Map<String, Object> requestMap=new HashMap<String, Object>();
               requestMap.put("baseCurrency", companyBaseCurrencyCode);
               JSONObject returnObject= accCurrencyExchangeRate.getUpdatedExchangeRates(requestMap);
               if(returnObject.has("error")){
                   msg = returnObject.optString("error");
                   throw new AccountingException(msg);
               }
               if(!returnObject.has("utctime")){
                   msg = returnObject.optString("error");
                   throw new AccountingException(msg);
               }               
               if(!returnObject.has("rates")){
                   msg = returnObject.optString("error");
                   throw new AccountingException(msg);
               }
               String timeStringInUTC = returnObject.optString("utctime");            
               timeStringInUTC=timeStringInUTC.replace(" ", "+");
               int lastIndexOfColon=timeStringInUTC.lastIndexOf(":");
               timeStringInUTC = timeStringInUTC.substring(0, lastIndexOfColon)+timeStringInUTC.substring(lastIndexOfColon+1);
               SimpleDateFormat df= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
               Date applyDate = authHandler.getDateOnlyFormat().parse(timeStringInUTC);              
               Calendar cal = Calendar.getInstance();
               cal.setTime(applyDate);
               cal.add(Calendar.DATE, 30);
               Date toDate = cal.getTime();
               String date=datef.format(toDate);
                try{
                    toDate=datef.parse(date);
                }catch(ParseException ex){
                    toDate = cal.getTime();
                }
               JSONObject ratesJson = returnObject.optJSONObject("rates");
               
               
               /* 
                * Fetching the Id's those are assigned to combination of two different currencies
                * We have set parameter 'fromcurrencyid' as company base currency, 
                * so that we will get id of combination of base currency to other currencies.
                */
               
               Map<String,Object> exchangeRateParams = new HashMap<String, Object>();
               exchangeRateParams.put("fromcurrencyid", companyBaseCurrencyId);
               KwlReturnObject exchangeRateLinkIdsResult = accCurrencyDAOobj.getCurrencyExchange(exchangeRateParams);
               List exchangeRateList=exchangeRateLinkIdsResult.getEntityList();
               Iterator exRateIterator = exchangeRateList.iterator();
               while(exRateIterator.hasNext()){
                   ExchangeRate exchangeRate = (ExchangeRate)exRateIterator.next();
                   KWLCurrency fromCurrency = exchangeRate.getFromCurrency();
                   KWLCurrency toCurrency = exchangeRate.getToCurrency();
                   String linkId = exchangeRate.getID();
                   Double rateToSet = ratesJson.getDouble(toCurrency.getCurrencyCode());
                   Map<String, Object> exchangeRateDetailsMap = new HashMap<String, Object>();
                   
                   exchangeRateDetailsMap.put(APPLYDATE, applyDate);
                   exchangeRateDetailsMap.put(TODATE, toDate);
                   exchangeRateDetailsMap.put(ERID,linkId);
                   exchangeRateDetailsMap.put(COMPANYID, company.getCompanyID());
                   exchangeRateDetailsMap.put(EXCHANGERATE,rateToSet);
                   accCurrencyDAOobj.addExchangeRateDetails(exchangeRateDetailsMap);
                   
               }      
               if (company.getCountry() != null && company.getCountry().getID().equals(Constants.INDONESIAN_COUNTRYID)) {
                   //For Indonesia, an extra currency table is added for Tax Reports.
                   Map<String, Object> TaxexchangeRateParams = new HashMap<String, Object>();
                   TaxexchangeRateParams.put("fromcurrencyid", companyBaseCurrencyId);
                   KwlReturnObject exchangeRateForTaxLinkIdsResult = accTaxCurrencyExchangeDAOobj.getTaxCurrencyExchange(TaxexchangeRateParams);
                   List exchangeRateForTaxList = exchangeRateForTaxLinkIdsResult.getEntityList();
                   Iterator exRateForTaxIterator = exchangeRateForTaxList.iterator();
                   while (exRateForTaxIterator.hasNext()) {
                       TaxExchangeRate exchangeRateForTax = (TaxExchangeRate) exRateForTaxIterator.next();
                       KWLCurrency fromCurrency1 = exchangeRateForTax.getFromCurrency();
                       KWLCurrency toCurrency1 = exchangeRateForTax.getToCurrency();
                       String linkId = exchangeRateForTax.getID();
                       Double rateToSet1 = ratesJson.getDouble(toCurrency1.getCurrencyCode());
                       Map<String, Object> exchangeRateDetailsMap = new HashMap<String, Object>();

                       exchangeRateDetailsMap.put(APPLYDATE, applyDate);
                       exchangeRateDetailsMap.put(TODATE, toDate);
                       exchangeRateDetailsMap.put(ERID, linkId);
                       exchangeRateDetailsMap.put(COMPANYID, company.getCompanyID());
                       exchangeRateDetailsMap.put(EXCHANGERATE, rateToSet1);
                       accTaxCurrencyExchangeDAOobj.addTaxExchangeRateDetails(exchangeRateDetailsMap);
                   }
               }
               System.out.println("Done for Subdomin"+ company.getSubDomain());                     
           }
           
        } catch (Exception e) {
            msg = e.getMessage();
            throw ServiceException.FAILURE("getBaseToCurrencyAmount : " + e.getMessage(), e);
        } finally {
            System.out.println(msg);
        }
        return new ModelAndView("jsonView_xe", "model", msg);
    }
    
     public static void getAddressSummaryData(List<AddressDetails> addressResultList, JSONObject summaryData, CompanyAccountPreferences companyAccountPreferences,ExtraCompanyPreferences extraCompanyPreferences) throws ServiceException, JSONException {
        try {
            String customervendorbillingaddress="";
            String customervendorshippingaddress="";
            if (!addressResultList.isEmpty()) {
                for (AddressDetails cas : addressResultList) {
                    if (cas.isIsDefaultAddress() && cas.isIsBillingAddress()) {//for defult billing address
                        summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingAddress_fieldTypeId, cas.getAddress() != null ? cas.getAddress() : "");
                        summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCity_fieldTypeId, cas.getCity() != null ? cas.getCity() : "");
                        summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingState_fieldTypeId, cas.getState() != null ? cas.getState() : "");
                        summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCountry_fieldTypeId, cas.getCountry() != null ? cas.getCountry() : "");
                        summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPostalCode_fieldTypeId, cas.getPostalCode() != null ? cas.getPostalCode() : "");
                        summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPhoneNo_fieldTypeId, cas.getPhone() != null ? cas.getPhone() : "");
                        summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingFaxNo_fieldTypeId, cas.getFax() != null ? cas.getFax() : "");
                        summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingContactPerson_fieldTypeId, cas.getContactPerson() != null ? cas.getContactPerson() : "");
                        summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingEmailID_fieldTypeId, cas.getEmailID() != null ? cas.getEmailID() : "");
                        summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingEmail1_fieldTypeId, cas.getEmailID() != null ? cas.getEmailID() : "");
                        summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingMobileNo_fieldTypeId, cas.getMobileNumber() != null ? cas.getMobileNumber() : "");
                        summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorContactBillingPhoneNo_fieldTypeId, cas.getContactPersonNumber() != null ? cas.getContactPersonNumber() : "");
                        summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorContactBillingDesignation_fieldTypeId, cas.getContactPersonDesignation() != null ? cas.getContactPersonDesignation() : "");
                        summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingRecipientName_fieldTypeId, cas.getRecipientName() != null ? cas.getRecipientName() : "");
                        
                        String addr = !StringUtil.isNullOrEmpty(cas.getAddress()) ? cas.getAddress() : "";
                        String city = !StringUtil.isNullOrEmpty(cas.getCity()) ? "!## "+cas.getCity() : "";
                        String state = !StringUtil.isNullOrEmpty(cas.getState()) ?  "!## "+cas.getState() : "";
                        String country =!StringUtil.isNullOrEmpty(cas.getCountry()) ?  "!## "+cas.getCountry() : "";
                        String postalcode =!StringUtil.isNullOrEmpty(cas.getPostalCode()) ?  "-"+cas.getPostalCode() : "";
                        customervendorbillingaddress = addr + city + state + country+postalcode;
                    }
                    if (cas.isIsDefaultAddress() && !cas.isIsBillingAddress()) { //for defult shipping address
                        summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingAddress_fieldTypeId, cas.getAddress() != null ? cas.getAddress() : "");
                        summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCity_fieldTypeId, cas.getCity() != null ? cas.getCity() : "");
                        summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingState_fieldTypeId, cas.getState() != null ? cas.getState() : "");
                        summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCountry_fieldTypeId, cas.getCountry() != null ? cas.getCountry() : "");
                        summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPostalCode_fieldTypeId, cas.getPostalCode() != null ? cas.getPostalCode() : "");
                        summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPhoneNo_fieldTypeId, cas.getPhone() != null ? cas.getPhone() : "");
                        summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingFaxNo_fieldTypeId, cas.getFax() != null ? cas.getFax() : "");
                        summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingContactPerson_fieldTypeId, cas.getContactPerson() != null ? cas.getContactPerson() : "");
                        summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingEmailID_fieldTypeId, cas.getEmailID() != null ? cas.getEmailID() : "");
                        summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingEmail1_fieldTypeId, cas.getEmailID() != null ? cas.getEmailID() : "");
                        summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorContactShippingPhoneNo_fieldTypeId, cas.getContactPersonNumber() != null ? cas.getContactPersonNumber() : "");
                        summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorContactShippingDesignation_fieldTypeId, cas.getContactPersonDesignation() != null ? cas.getContactPersonDesignation() : "");
                        summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingMobileNo_fieldTypeId, cas.getMobileNumber() != null ? cas.getMobileNumber() : "");
                        summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingRecipientName_fieldTypeId, cas.getRecipientName() != null ? cas.getRecipientName() : "");
                      
                        String addr = !StringUtil.isNullOrEmpty(cas.getAddress()) ? cas.getAddress() : "";
                        String city = !StringUtil.isNullOrEmpty(cas.getCity()) ? "!## "+cas.getCity() : "";
                        String state = !StringUtil.isNullOrEmpty(cas.getState()) ?  "!## "+cas.getState() : "";
                        String country =!StringUtil.isNullOrEmpty(cas.getCountry()) ?  "!## "+cas.getCountry() : "";
                        String postalcode =!StringUtil.isNullOrEmpty(cas.getPostalCode()) ?  "-"+cas.getPostalCode() : "";
                        customervendorshippingaddress = addr + city + state + country + postalcode;
                    }
                }
            } else {
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingAddress_fieldTypeId, "");
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCity_fieldTypeId, "");
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingState_fieldTypeId, "");
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCountry_fieldTypeId, "");
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPostalCode_fieldTypeId, "");
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPhoneNo_fieldTypeId, "");
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingFaxNo_fieldTypeId, "");
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingContactPerson_fieldTypeId, "");
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingAddress_fieldTypeId, "");
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCity_fieldTypeId, "");
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingState_fieldTypeId, "");
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCountry_fieldTypeId, "");
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPostalCode_fieldTypeId, "");
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPhoneNo_fieldTypeId, "");
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingFaxNo_fieldTypeId, "");
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingContactPerson_fieldTypeId, "");
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingEmailID_fieldTypeId, "");
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingEmail1_fieldTypeId, "");
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingEmail1_fieldTypeId, "");
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingMobileNo_fieldTypeId, "");
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorContactBillingPhoneNo_fieldTypeId, "");
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorContactShippingPhoneNo_fieldTypeId, "");
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingMobileNo_fieldTypeId, "");
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingRecipientName_fieldTypeId, "");
                summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingRecipientName_fieldTypeId, "");
            }

            String compbillingAddress = "";
            String compshippingAddress = "";
            /**
             * Set Blank is default value
             */
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyBillingAddressField_fieldTypeId, "");
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyBillingAddressCity_fieldTypeId, "");
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyBillingAddressState_fieldTypeId, "");
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyBillingAddressCountry_fieldTypeId, "");
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyBillingAddressPostalCode_fieldTypeId, "");
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyBillingAddressContactPerson_fieldTypeId, "");
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyBillingAddressContactPersonNo_fieldTypeId, "");
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyBillingAddressPhoneNo_fieldTypeId, "");
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyBillingAddressMobileNo_fieldTypeId, "");
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyBillingAddressFaxNo_fieldTypeId, "");
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyBillingAddressEmail_fieldTypeId, "");
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyShippingAddressField_fieldTypeId, "");
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyShippingAddressCity_fieldTypeId, "");
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyShippingAddressState_fieldTypeId, "");
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyShippingAddressCountry_fieldTypeId, "");
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyShippingAddressPostalCode_fieldTypeId, "");
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyShippingAddressContactPerson_fieldTypeId, "");
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyShippingAddressContactPersonNo_fieldTypeId, "");
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyShippingAddressPhoneNo_fieldTypeId, "");
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyShippingAddressMobileNo_fieldTypeId, "");
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyShippingAddressFaxNo_fieldTypeId, "");
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyShippingAddressEmail_fieldTypeId, "");
            Set<CompanyAddressDetails> rows = extraCompanyPreferences.getCompanyAddressRows();
            for (CompanyAddressDetails cad : rows) {
                if (cad.isIsDefaultAddress()) {
                    String addr = StringUtil.isNullOrEmpty(cad.getAddress()) ? "" : cad.getAddress();
                    String city = StringUtil.isNullOrEmpty(cad.getCity()) ? "" : "!## " + cad.getCity();
                    String state = StringUtil.isNullOrEmpty(cad.getState()) ? "" : "!## " + cad.getState();
                    String country = StringUtil.isNullOrEmpty(cad.getCountry()) ? "" : "!## " + cad.getCountry();
                    String postalcode = StringUtil.isNullOrEmpty(cad.getPostalCode()) ? "" : " " + cad.getPostalCode();
                    String email = StringUtil.isNullOrEmpty(cad.getEmailID()) ? "" : "\nEmail : " + cad.getEmailID();
                    String phone = StringUtil.isNullOrEmpty(cad.getPhone()) ? "" : "\nPhone : " + cad.getPhone();
                    String fax = StringUtil.isNullOrEmpty(cad.getFax()) ? "" : StringUtil.isNullOrEmpty(phone) ? "\nFax : " + cad.getFax() : "!## Fax : " + cad.getFax();
                    String contractpersonno = StringUtil.isNullOrEmpty(cad.getContactPersonNumber()) ? "" : "\nContact Person No : " + cad.getContactPersonNumber();
                    String contractpersondesignation = StringUtil.isNullOrEmpty(cad.getContactPersonDesignation()) ? "" : "\nContact Person Designation : " + cad.getContactPersonDesignation();
                    String mobile = StringUtil.isNullOrEmpty(cad.getMobileNumber()) ? "" : "\nMobile : " + cad.getMobileNumber();
                    String attn = StringUtil.isNullOrEmpty(cad.getContactPerson()) ? "" : "\nAttn. : " + cad.getContactPerson();
                    if (cad.isIsBillingAddress()) {
                        compbillingAddress= addr + city + state + country + postalcode + email + phone + fax + mobile + contractpersonno + contractpersondesignation + attn;
                        //ERP-21048 : [Document Designer] add field for Company Billing and Shipping address details separately  in Delivery Order module
                        summaryData.put(CustomDesignerConstants.CustomDesignCompanyBillingAddressField_fieldTypeId, StringUtil.isNullOrEmpty(cad.getAddress()) ? "" : cad.getAddress());
                        summaryData.put(CustomDesignerConstants.CustomDesignCompanyBillingAddressCity_fieldTypeId, StringUtil.isNullOrEmpty(cad.getCity()) ? "" : cad.getCity());
                        summaryData.put(CustomDesignerConstants.CustomDesignCompanyBillingAddressState_fieldTypeId, StringUtil.isNullOrEmpty(cad.getState()) ? "" : cad.getState());
                        summaryData.put(CustomDesignerConstants.CustomDesignCompanyBillingAddressCountry_fieldTypeId, StringUtil.isNullOrEmpty(cad.getCountry()) ? "" : cad.getCountry());
                        summaryData.put(CustomDesignerConstants.CustomDesignCompanyBillingAddressPostalCode_fieldTypeId, StringUtil.isNullOrEmpty(cad.getPostalCode()) ? "" : cad.getPostalCode());
                        summaryData.put(CustomDesignerConstants.CustomDesignCompanyBillingAddressContactPerson_fieldTypeId, StringUtil.isNullOrEmpty(cad.getContactPerson()) ? "" : cad.getContactPerson());
                        summaryData.put(CustomDesignerConstants.CustomDesignCompanyBillingAddressContactPersonNo_fieldTypeId, StringUtil.isNullOrEmpty(cad.getContactPersonNumber()) ? "" : cad.getContactPersonNumber());
                        summaryData.put(CustomDesignerConstants.CustomDesignCompanyBillingAddressPhoneNo_fieldTypeId, StringUtil.isNullOrEmpty(cad.getPhone()) ? "" : cad.getPhone());
                        summaryData.put(CustomDesignerConstants.CustomDesignCompanyBillingAddressMobileNo_fieldTypeId, StringUtil.isNullOrEmpty(cad.getMobileNumber()) ? "" : cad.getMobileNumber());
                        summaryData.put(CustomDesignerConstants.CustomDesignCompanyBillingAddressFaxNo_fieldTypeId, StringUtil.isNullOrEmpty(cad.getFax()) ? "" : cad.getFax());
                        summaryData.put(CustomDesignerConstants.CustomDesignCompanyBillingAddressEmail_fieldTypeId, StringUtil.isNullOrEmpty(cad.getEmailID()) ? "" : cad.getEmailID());
                    } else {
                        compshippingAddress = addr + city + state + country + postalcode + email + phone + fax + mobile + contractpersonno + contractpersondesignation + attn;
                        //ERP-21048 : [Document Designer] add field for Company Billing and Shipping address details separately  in Delivery Order module
                        summaryData.put(CustomDesignerConstants.CustomDesignCompanyShippingAddressField_fieldTypeId, StringUtil.isNullOrEmpty(cad.getAddress()) ? "" : cad.getAddress());
                        summaryData.put(CustomDesignerConstants.CustomDesignCompanyShippingAddressCity_fieldTypeId, StringUtil.isNullOrEmpty(cad.getCity()) ? "" : cad.getCity());
                        summaryData.put(CustomDesignerConstants.CustomDesignCompanyShippingAddressState_fieldTypeId, StringUtil.isNullOrEmpty(cad.getState()) ? "" : cad.getState());
                        summaryData.put(CustomDesignerConstants.CustomDesignCompanyShippingAddressCountry_fieldTypeId, StringUtil.isNullOrEmpty(cad.getCountry()) ? "" : cad.getCountry());
                        summaryData.put(CustomDesignerConstants.CustomDesignCompanyShippingAddressPostalCode_fieldTypeId, StringUtil.isNullOrEmpty(cad.getPostalCode()) ? "" : cad.getPostalCode());
                        summaryData.put(CustomDesignerConstants.CustomDesignCompanyShippingAddressContactPerson_fieldTypeId, StringUtil.isNullOrEmpty(cad.getContactPerson()) ? "" : cad.getContactPerson());
                        summaryData.put(CustomDesignerConstants.CustomDesignCompanyShippingAddressContactPersonNo_fieldTypeId, StringUtil.isNullOrEmpty(cad.getContactPersonNumber()) ? "" : cad.getContactPersonNumber());
                        summaryData.put(CustomDesignerConstants.CustomDesignCompanyShippingAddressPhoneNo_fieldTypeId, StringUtil.isNullOrEmpty(cad.getPhone()) ? "" : cad.getPhone());
                        summaryData.put(CustomDesignerConstants.CustomDesignCompanyShippingAddressMobileNo_fieldTypeId, StringUtil.isNullOrEmpty(cad.getMobileNumber()) ? "" : cad.getMobileNumber());
                        summaryData.put(CustomDesignerConstants.CustomDesignCompanyShippingAddressFaxNo_fieldTypeId, StringUtil.isNullOrEmpty(cad.getFax()) ? "" : cad.getFax());
                        summaryData.put(CustomDesignerConstants.CustomDesignCompanyShippingAddressEmail_fieldTypeId, StringUtil.isNullOrEmpty(cad.getEmailID()) ? "" : cad.getEmailID());
                    }
                }
            }
            summaryData.put(CustomDesignerConstants.CustomDesignCompAccPrefBillAddress_fieldTypeId, compbillingAddress);
            summaryData.put(CustomDesignerConstants.CustomDesignCompAccPrefShipAddress_fieldTypeId, compshippingAddress);
            summaryData.put(CustomDesignerConstants.RemitPaymentTo, !StringUtil.isNullOrEmpty(extraCompanyPreferences.getRemitpaymentto()) ? extraCompanyPreferences.getRemitpaymentto().replaceAll("<br>", "!##") : "");
            summaryData.put(CustomDesignerConstants.CustomerVendorBillingAddress, customervendorbillingaddress);
            summaryData.put(CustomDesignerConstants.CustomerVendorShippingAddress, customervendorshippingaddress);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public ModelAndView repeatePayment(HttpServletRequest request, HttpServletResponse response){        
        JSONObject objectToReturn=new JSONObject();
        try {
            objectToReturn=accVendorPaymentServiceDAO.repeatPayment();
            if(objectToReturn.has("recurrray")){
                //This code has written to make entry to Audit Trail Report for recurred Make Payment.
                JSONArray recurarray = objectToReturn.getJSONArray("recurrray");
                for(int i=0; i<recurarray.length(); i++){
                    JSONObject jobj = recurarray.getJSONObject(i);
                    User user = (User)jobj.get("user");
                    String paymentno = jobj.getString("paymentno");
                    auditTrailObj.insertRecurringAuditLog(AuditAction.ADD_RECURRING_MAKE_PAYMENT_ENTRY, "System has generated a recurring Make Payment - " + paymentno, request, paymentno, user);
                }
                objectToReturn.remove("recurrray");
            }             
        } catch (Exception e) {
            e.printStackTrace();
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, e);
        }         
        return new ModelAndView("jsonView_ex", "model", objectToReturn.toString());
    } 
    
    public ModelAndView sendToPOSProductsPrice(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException {
        JSONObject objectToReturn = new JSONObject();
        String msg = "";
        boolean isSuccess = false;
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        DateFormat datef=authHandler.getDateOnlyFormat();
        //Session session=null;
        try {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date applicableDate = cal.getTime();
            String date=datef.format(applicableDate);
            try{
                applicableDate=datef.parse(date);
            }catch(ParseException ex){
                applicableDate = cal.getTime();
            }
            List companyList = accountingHandlerDAOobj.getPOSCompanyList();
            if (companyList.size() > 0) {
                Iterator iterator = companyList.iterator();
                while (iterator.hasNext()) {
                    Object[] objs = (Object[]) iterator.next();
                    String companyId = objs[0] != null ? (String) objs[0] : null;
                    String subdomain = objs[1] != null ? (String) objs[1] : null;
                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("isSyncToPOS", true);
                    requestParams.put("companyID", companyId);
                    requestParams.put("applicableDate", applicableDate);
                    KwlReturnObject result = accMasterItemsDAOobj.getPOSProductsPrice(requestParams);

                    List<PricingBandMasterDetail> list = result.getEntityList();
                    for (PricingBandMasterDetail pricingBandMasterDetailObj : list) {
                        JSONObject obj = new JSONObject();
                        obj.put("productId", pricingBandMasterDetailObj.getProduct() != null ? pricingBandMasterDetailObj.getProduct() : "");
                        obj.put("costPrice", pricingBandMasterDetailObj.getPurchasePrice());
                        obj.put("salesPrice", pricingBandMasterDetailObj.getSalesPrice());
                        obj.put("currencyId", pricingBandMasterDetailObj.getCurrency() != null ? pricingBandMasterDetailObj.getCurrency().getCurrencyID() : "");
                        DataJArr.put(obj);
                    }

                    int totalCount = result.getRecordTotalCount();

                    jobj.put("data", DataJArr);
                    jobj.put("totalCount", totalCount);
                    //session = HibernateUtil.getCurrentSession();
                    String companySubDomain = subdomain;
                    String posURL = this.getServletContext().getInitParameter("posURL");
                    String action = "33";

                    JSONObject userData = new JSONObject();
                    userData.put("iscommit", true);
                    userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
//            userData.put("userid", sessionHandlerImpl.getUserid(request));
                    userData.put("companyid", companyId);
                    userData.put("subdomain", companySubDomain);
                    userData.put("action", action);
                    userData.put("data", jobj);
                    JSONObject resObj = apiCallHandlerService.callApp(posURL, userData, companyId, action);

                    if (!resObj.isNull("success") && resObj.getBoolean("success")) {
                        isSuccess = resObj.getBoolean("success");
                        msg = resObj.getString("msg");
                        objectToReturn.put("success", true);
                        objectToReturn.put("msg", msg);
//                jobj.put("companyexist", resObj.optBoolean("companyexist"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                objectToReturn.put("success", isSuccess);
                objectToReturn.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", "model", objectToReturn.toString());
    }
    
    public ModelAndView updateCNDNBaseAmountDue(HttpServletRequest request, HttpServletResponse response) {
        String msg = "Failed";
        String cid = request.getParameter("cid");
        boolean updateData = Boolean.parseBoolean(request.getParameter("updateData"));
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            if (!StringUtil.isNullOrEmpty(cid)) {
                KwlReturnObject companyObject = accountingHandlerDAOobj.getObject(Company.class.getName(), cid);
                Company company = (Company) companyObject.getEntityList().get(0);
                // Get paid CN records
                KwlReturnObject cnResult = accVendorPaymentobj.getCnPaymenyHistory(cid);
                Iterator iterator = cnResult.getEntityList().iterator();
                String areCreditNotesUpdated="";
                String affectedCreditNotes="";
                String arePaymentsUpdated="";
                String affectedPayments="";
                
                while (iterator.hasNext()) {
                    CreditNotePaymentDetails creditNotePaymentDetail = (CreditNotePaymentDetails) iterator.next();
                    String cndetailid = creditNotePaymentDetail.getID();                    
                    double cnpaidamountInCnCurrency = creditNotePaymentDetail.getAmountPaid();
                    double cnPaidAmountInBaseCurrencyOld = creditNotePaymentDetail.getAmountInBaseCurrency();
                    double cnPaidAmountInBaseCurrencyNew = 0.0d;                    
                    CreditNote creditNote = creditNotePaymentDetail.getCreditnote();
                    // Calculation is being done only for opening credit notes
                    if(creditNote.isIsOpeningBalenceCN() && !creditNote.isNormalCN())
                    {
                        double openingBalanceBaseAmountDueOld = 0;
                        double openingBalanceBaseAmountDueNew = 0;
                        double openingBalanceAmountDue = creditNote.getOpeningBalanceAmountDue();
                        double cnExternalCurrencyrate = 1d;
                        Date cnCreationDate = null;
                        openingBalanceBaseAmountDueOld = authHandler.round(creditNote.getOpeningBalanceBaseAmountDue(), companyid);
                        cnCreationDate = creditNote.getCreationDate();
                        if (creditNote.isIsOpeningBalenceCN() && !creditNote.isNormalCN()) {
                            cnExternalCurrencyrate = creditNote.getExchangeRateForOpeningTransaction();
                            if (creditNote.isConversionRateFromCurrencyToBase()) {
                                cnExternalCurrencyrate = 1 / cnExternalCurrencyrate;
                            }
                        } else {
                            cnExternalCurrencyrate = creditNote.getJournalEntry().getExternalCurrencyRate();
//                            cnCreationDate = creditNote.getJournalEntry().getEntryDate();
                        }
                        KwlReturnObject baseAmount = null;
                        HashMap<String, Object> requestParams = new HashMap();
                        requestParams.put(Constants.companyid, cid);
                        requestParams.put("gcurrencyid", company.getCurrency().getCurrencyID());

                        // Calculate current base amount due
                        baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, openingBalanceAmountDue, creditNote.getCurrency().getCurrencyID(), cnCreationDate, cnExternalCurrencyrate);
                        openingBalanceBaseAmountDueNew = authHandler.round((Double) baseAmount.getEntityList().get(0), companyid);

                        // calculate base amount paid against CN
                        baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, cnpaidamountInCnCurrency, creditNote.getCurrency().getCurrencyID(), cnCreationDate, cnExternalCurrencyrate);
                        cnPaidAmountInBaseCurrencyNew = (Double) baseAmount.getEntityList().get(0);
                        cnPaidAmountInBaseCurrencyNew = authHandler.round(cnPaidAmountInBaseCurrencyNew, companyid);

                        // Compare old and new base amount paid against CN and update accordingly
                        if (cnPaidAmountInBaseCurrencyNew != cnPaidAmountInBaseCurrencyOld) {
                            if(updateData){
                                HashMap<String, Object> creditNotePaymentDetailMap = new HashMap<String, Object>();
                                creditNotePaymentDetailMap.put("id", cndetailid);
                                creditNotePaymentDetailMap.put("amountInBaseCurrency", cnPaidAmountInBaseCurrencyNew);
                                accVendorPaymentobj.saveCreditNotePaymentDetails(creditNotePaymentDetailMap);
                            }    
                            affectedPayments += creditNotePaymentDetail.getPayment().getPaymentNumber() + "(" + cnPaidAmountInBaseCurrencyOld + "," + cnPaidAmountInBaseCurrencyNew + ")" + ",";
                        }

                        // Compare old and new base amount due (current figure) and update accordingly.
                        if (openingBalanceBaseAmountDueNew != openingBalanceBaseAmountDueOld) {
                            if(updateData){
                                HashMap<String, Object> credithm = new HashMap<String, Object>();
                                credithm.put("cnid", creditNote.getID());
                                credithm.put(Constants.openingBalanceBaseAmountDue, openingBalanceBaseAmountDueNew);
                                accCreditNoteobj.updateCreditNote(credithm);
                            }   
                            affectedCreditNotes += creditNote.getCreditNoteNumber() + "(Old Figure: " + openingBalanceBaseAmountDueOld + ",New Figure: " + openingBalanceBaseAmountDueNew + ")" + ",";
                        }

                    }
     
  
 }
                if(StringUtil.isNullOrEmpty(affectedCreditNotes)){                    
                    areCreditNotesUpdated = " No Credit Notes updated. ";
                } else {
                    affectedCreditNotes =affectedCreditNotes.substring(0,affectedCreditNotes.length()-1);
                    areCreditNotesUpdated=" Credit Notes updated - ";
                    areCreditNotesUpdated+=" "+affectedCreditNotes;
                }
                if(StringUtil.isNullOrEmpty(affectedPayments)){
                    arePaymentsUpdated = " No Payments updated.";
                } else {
                    affectedPayments = affectedPayments.substring(0, affectedPayments.length()-1);
                    arePaymentsUpdated = " Payments updated- ";
                    arePaymentsUpdated+= " "+affectedPayments;
                }
                
                // Get paid DN records
                KwlReturnObject dnResult = accReceiptobj.getDnReceiptHistory(cid);                
                Iterator iterator1 = dnResult.getEntityList().iterator();
                String areDebitNotesUpdated="";
                String affectedDebitNotes="";
                String areReceiptsUpdated="";
                String affectedReceipts="";
                while (iterator1.hasNext()) {
                    DebitNotePaymentDetails debitNotePaymentDetail = (DebitNotePaymentDetails) iterator1.next();
                    String dndetailid = debitNotePaymentDetail.getID();                    
                    double dnpaidamountInDnCurrency = debitNotePaymentDetail.getAmountPaid();
                    double dnPaidAmountInBaseCurrencyOld = debitNotePaymentDetail.getAmountInBaseCurrency();
                    double dnPaidAmountInBaseCurrencyNew = 0.0d;                    
                    DebitNote debitNote = debitNotePaymentDetail.getDebitnote();
                    //Calculation is being done only for opening DN
                    if (debitNote.isIsOpeningBalenceDN() && !debitNote.isNormalDN()) {
                        double openingBalanceBaseAmountDueOld = 0;
                        double openingBalanceBaseAmountDueNew = 0;
                        double openingBalanceAmountDue = debitNote.getOpeningBalanceAmountDue();
                        double dnExternalCurrencyrate = 1d;
                        Date dnCreationDate = null;
                        openingBalanceBaseAmountDueOld = authHandler.round(debitNote.getOpeningBalanceBaseAmountDue(), companyid);
                        dnCreationDate = debitNote.getCreationDate();
                        if (debitNote.isIsOpeningBalenceDN() && !debitNote.isNormalDN()) {
                            dnExternalCurrencyrate = debitNote.getExchangeRateForOpeningTransaction();
                            if (debitNote.isConversionRateFromCurrencyToBase()) {
                                dnExternalCurrencyrate = 1 / dnExternalCurrencyrate;
                            }
                        } else {
                            dnExternalCurrencyrate = debitNote.getJournalEntry().getExternalCurrencyRate();
//                            dnCreationDate = debitNote.getJournalEntry().getEntryDate();
                        }
                        KwlReturnObject baseAmount = null;
                        HashMap<String, Object> requestParams = new HashMap();
                        requestParams.put(Constants.companyid, cid);
                        requestParams.put("gcurrencyid", company.getCurrency().getCurrencyID());

                        // Calculate current base amount due
                        baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, openingBalanceAmountDue, debitNote.getCurrency().getCurrencyID(), dnCreationDate, dnExternalCurrencyrate);
                        openingBalanceBaseAmountDueNew = authHandler.round((Double) baseAmount.getEntityList().get(0), companyid);

                        // calculate base amount paid against DN
                        baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, dnpaidamountInDnCurrency, debitNote.getCurrency().getCurrencyID(), dnCreationDate, dnExternalCurrencyrate);
                        dnPaidAmountInBaseCurrencyNew = (Double) baseAmount.getEntityList().get(0);
                        dnPaidAmountInBaseCurrencyNew = authHandler.round(dnPaidAmountInBaseCurrencyNew, companyid);

                        // Compare old and new base amount paid against DN and update accordingly
                        if (dnPaidAmountInBaseCurrencyNew != dnPaidAmountInBaseCurrencyOld) {
                            if(updateData){
                                HashMap<String, Object> debitNotePaymentDetailMap = new HashMap<String, Object>();
                                debitNotePaymentDetailMap.put("id", dndetailid);
                                debitNotePaymentDetailMap.put("amountInBaseCurrency", dnPaidAmountInBaseCurrencyNew);
                                accReceiptobj.saveDebitNotePaymentDetails(debitNotePaymentDetailMap);
                            }   
                            affectedReceipts += debitNotePaymentDetail.getReceipt().getReceiptNumber() + "(" + dnPaidAmountInBaseCurrencyOld + "," + dnPaidAmountInBaseCurrencyNew + ")" + ",";
                        }

                        // Compare old and new base amount due (current figure) and update accordingly.
                        if (openingBalanceBaseAmountDueNew != openingBalanceBaseAmountDueOld) {
                            if(updateData){
                                HashMap<String, Object> debithm = new HashMap<String, Object>();
                                debithm.put("dnid", debitNote.getID());
                                debithm.put(Constants.openingBalanceBaseAmountDue, openingBalanceBaseAmountDueNew);
                                accDebitNoteobj.updateDebitNote(debithm);
                            }    
                            affectedDebitNotes += debitNote.getDebitNoteNumber() + "(Old Figure: " + openingBalanceBaseAmountDueOld + ",New Figure: " + openingBalanceBaseAmountDueNew + ")" + ",";
                        }
                }
                }
                if(StringUtil.isNullOrEmpty(affectedDebitNotes)){
                    areDebitNotesUpdated = " No debit notes updated.";
                } else {
                    affectedDebitNotes =affectedDebitNotes.substring(0,affectedDebitNotes.length()-1);
                    areDebitNotesUpdated= " Debit Notes updated - ";
                    areDebitNotesUpdated += " "+affectedDebitNotes;
                }                
                 if(StringUtil.isNullOrEmpty(affectedReceipts)){
                    areReceiptsUpdated= " No Receipts updated.";
                } else {
                    affectedReceipts = affectedReceipts.substring(0, affectedReceipts.length()-1);
                    areReceiptsUpdated = "Receipts updated- ";
                    areReceiptsUpdated += " "+affectedReceipts;
                }
                msg=areCreditNotesUpdated+areDebitNotesUpdated+arePaymentsUpdated+areReceiptsUpdated;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, e);
        }
        return new ModelAndView("jsonView_ex", "model", msg);
    }
    
    
    public ModelAndView SyncAllFromLMS(HttpServletRequest request, HttpServletResponse response){        
       JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MI_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String [] mapWithFieldTypeArr=request.getParameter("mapWithFieldType").split(",");
            String lmsURL = this.getServletContext().getInitParameter("lmsURL");
            int requestFlag = Integer.parseInt(request.getParameter("requestFlag"));
            HashMap<String, Object> requestParams=AccountingManager.getSyncAllRequestParams(request);
            requestParams.put("servletContext", this.getServletContext());
            requestParams.put("requestFlag", requestFlag);
            requestParams.put("lmsURL", lmsURL);
            ArrayList al=new ArrayList();
            if (requestFlag == 1) { //Only Dimension
                  Thread t1=null;
                    requestParams.put("mapWithFieldType", mapWithFieldTypeArr[0]);
                    requestParams.put("threadRequest","Dimension");
                    SyncAllHandlerObj.add(requestParams);
                   
                    HashMap<String, Object> requestParams1 = null;
                    requestParams1 =  (HashMap) requestParams.clone();
                    requestParams1.put("mapWithFieldType", mapWithFieldTypeArr[1]);
                    SyncAllHandlerObj.add(requestParams1);
                     
                     
                    HashMap<String, Object> requestParams2 = null;
                    requestParams2 = (HashMap) requestParams.clone();
                    requestParams2.put("mapWithFieldType", mapWithFieldTypeArr[2]);
                    SyncAllHandlerObj.add(requestParams2);
                    
                    
                    HashMap<String, Object> requestParams3=null;
                    requestParams3 = (HashMap) requestParams.clone();
                    requestParams3.put("mapWithFieldType", mapWithFieldTypeArr[3]);
                    SyncAllHandlerObj.add(requestParams3);
                   
                    
                    t1 = new Thread(SyncAllHandlerObj);
                    t1.start();
                    
                    
                
                
            }else if (requestFlag == 2) {   //Dimension and Product request
                
                Thread t1=null;
                    requestParams.put("mapWithFieldType", mapWithFieldTypeArr[0]);
                    requestParams.put("threadRequest","Dimension");
                    SyncAllHandlerObj.add(requestParams);
                   
                    HashMap<String, Object> requestParams1 = null;
                    requestParams1 =  (HashMap) requestParams.clone();
                    requestParams1.put("mapWithFieldType", mapWithFieldTypeArr[1]);
                    SyncAllHandlerObj.add(requestParams1);
                     
                     
                    HashMap<String, Object> requestParams2 = null;
                    requestParams2 = (HashMap) requestParams.clone();
                    requestParams2.put("mapWithFieldType", mapWithFieldTypeArr[2]);
                    SyncAllHandlerObj.add(requestParams2);
                    
                    
                    HashMap<String, Object> requestParams3=null;
                    requestParams3 = (HashMap) requestParams.clone();
                    requestParams3.put("mapWithFieldType", mapWithFieldTypeArr[3]);
                    SyncAllHandlerObj.add(requestParams3);
                    
                    
                    
                    
                    HashMap<String, Object> requestParams4=null;
                    requestParams4 = (HashMap) requestParams.clone();
                    requestParams4.put("threadRequest","Product");
                     SyncAllHandlerObj.add(requestParams4);
                    
                   
                  
                    t1 = new Thread(SyncAllHandlerObj);
                    t1.start();
                    
                    
               
            
            }else if (requestFlag == 3) {   //Dimension,Product and Customer request
                
                
               
                Thread t1=null;
                    requestParams.put("mapWithFieldType", mapWithFieldTypeArr[0]);
                    requestParams.put("threadRequest","Dimension");
                    SyncAllHandlerObj.add(requestParams);
                   
                    HashMap<String, Object> requestParams1 = null;
                    requestParams1 =  (HashMap) requestParams.clone();
                    requestParams1.put("mapWithFieldType", mapWithFieldTypeArr[1]);
                    SyncAllHandlerObj.add(requestParams1);
                     
                     
                    HashMap<String, Object> requestParams2 = null;
                    requestParams2 = (HashMap) requestParams.clone();
                    requestParams2.put("mapWithFieldType", mapWithFieldTypeArr[2]);
                    SyncAllHandlerObj.add(requestParams2);
                    
                    
                    HashMap<String, Object> requestParams3=null;
                    requestParams3 = (HashMap) requestParams.clone();
                    requestParams3.put("mapWithFieldType", mapWithFieldTypeArr[3]);
                    SyncAllHandlerObj.add(requestParams3);
                    
                    
                    
                    HashMap<String, Object> requestParams4=null;
                    requestParams4 = (HashMap) requestParams.clone();
                    requestParams4.put("threadRequest","Product");
                    SyncAllHandlerObj.add(requestParams4);
                    
                    
                     
                    HashMap<String, Object> requestParams5=null;
                    requestParams5 = (HashMap) requestParams.clone();
                    requestParams5.put("threadRequest","Customer");
                    SyncAllHandlerObj.add(requestParams5);
                     
                   
                    
                    t1 = new Thread(SyncAllHandlerObj);
                    t1.start();
                    
                    

                
                
            }else if (requestFlag == 4) { //Dimension,Product,Customer,Invoice request
                
                
                 Thread t1=null;
                    requestParams.put("mapWithFieldType", mapWithFieldTypeArr[0]);
                    requestParams.put("threadRequest","Dimension");
                    SyncAllHandlerObj.add(requestParams);
                   
                    HashMap<String, Object> requestParams1 = null;
                    requestParams1 =  (HashMap) requestParams.clone();
                    requestParams1.put("mapWithFieldType", mapWithFieldTypeArr[1]);
                    SyncAllHandlerObj.add(requestParams1);
                     
                     
                    HashMap<String, Object> requestParams2 = null;
                    requestParams2 = (HashMap) requestParams.clone();
                    requestParams2.put("mapWithFieldType", mapWithFieldTypeArr[2]);
                    SyncAllHandlerObj.add(requestParams2);
                    
                    
                    HashMap<String, Object> requestParams3=null;
                    requestParams3 = (HashMap) requestParams.clone();
                    requestParams3.put("mapWithFieldType", mapWithFieldTypeArr[3]);
                    SyncAllHandlerObj.add(requestParams3);
                    
                    
                    HashMap<String, Object> requestParams4=null;
                    requestParams4 = (HashMap) requestParams.clone();
                    requestParams4.put("threadRequest","Product");
                    SyncAllHandlerObj.add(requestParams4);
                    
                    
                     
                    HashMap<String, Object> requestParams5=null;
                    requestParams5 = (HashMap) requestParams.clone();
                    requestParams5.put("threadRequest","Customer");
                    SyncAllHandlerObj.add(requestParams5);
                    
                    HashMap<String, Object> requestParams6=null;
                    requestParams6 = (HashMap) requestParams.clone();
                    requestParams6.put("threadRequest","Invoice");
                    SyncAllHandlerObj.add(requestParams6);
                    
                   
                    t1 = new Thread(SyncAllHandlerObj);
                    t1.start();
                    
               
                
                
            }else if (requestFlag == 5) {  //Dimension,Product,Customer,Invoice,Receipt request
                
                Thread t1=null;
                    requestParams.put("mapWithFieldType", mapWithFieldTypeArr[0]);
                    requestParams.put("threadRequest","Dimension");
                    SyncAllHandlerObj.add(requestParams);
                   
                    HashMap<String, Object> requestParams1 = null;
                    requestParams1 =  (HashMap) requestParams.clone();
                    requestParams1.put("mapWithFieldType", mapWithFieldTypeArr[1]);
                    SyncAllHandlerObj.add(requestParams1);
                     
                     
                    HashMap<String, Object> requestParams2 = null;
                    requestParams2 = (HashMap) requestParams.clone();
                    requestParams2.put("mapWithFieldType", mapWithFieldTypeArr[2]);
                    SyncAllHandlerObj.add(requestParams2);
                    
                    
                    HashMap<String, Object> requestParams3=null;
                    requestParams3 = (HashMap) requestParams.clone();
                    requestParams3.put("mapWithFieldType", mapWithFieldTypeArr[3]);
                    SyncAllHandlerObj.add(requestParams3);
                    
                    
                    HashMap<String, Object> requestParams4=null;
                    requestParams4 = (HashMap) requestParams.clone();
                    requestParams4.put("threadRequest","Product");
                    SyncAllHandlerObj.add(requestParams4);
                    
                    
                     
                    HashMap<String, Object> requestParams5=null;
                    requestParams5 = (HashMap) requestParams.clone();
                    requestParams5.put("threadRequest","Customer");
                    SyncAllHandlerObj.add(requestParams5);
                    
                    HashMap<String, Object> requestParams6=null;
                    requestParams6 = (HashMap) requestParams.clone();
                    requestParams6.put("threadRequest","Invoice");
                    SyncAllHandlerObj.add(requestParams6);
                    
                    
                    HashMap<String, Object> requestParams7=null;
                    requestParams7 = (HashMap) requestParams.clone();
                    requestParams7.put("threadRequest","Receipt");
                    SyncAllHandlerObj.add(requestParams7);
                    
                    t1 = new Thread(SyncAllHandlerObj);
                    t1.start();
            }
            issuccess = true;
            msg = messageSource.getMessage("acc.field.DataSyncedEmailConfirmation", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);

        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView updateAmountInBase(HttpServletRequest request, HttpServletResponse response) {
        StringBuffer msg = new StringBuffer();
        try {
            String companyid = request.getParameter("companyid");
            String currencyid = request.getParameter("currencyid");
            HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
            GlobalParams.put("companyid", companyid);
            GlobalParams.put("gcurrencyid", currencyid);
            if (!StringUtil.isNullOrEmpty(companyid) && !StringUtil.isNullOrEmpty(currencyid)) {
                int count = updateAmountInBaseCN(GlobalParams);
                msg.append("<br> Credit Notes Updated- " + count);
                count = updateAmountInBaseDN(GlobalParams);
                msg.append("<br> Debit Notes Updated- " + count);
                count = updateAmountInBaseReceipt(GlobalParams);
                msg.append("<br> Receipts Updated- " + count);
                count=updateAmountInBasePayment(GlobalParams);
                msg.append("<br> Payments Updated- " + count);
            }
        } catch (Exception ex) {
            System.out.println("Exception occured-"+ex.getMessage());
        } finally {
            return new ModelAndView("jsonView_ex", "model", msg.toString());
        }
    }

    private int updateAmountInBaseCN(HashMap<String, Object> GlobalParams) {
        int count = 0;
        HashMap<String, Object> requestParams = new HashMap();
        try {
            String companyid = (String) GlobalParams.get("companyid");
            requestParams.put("companyid", (String)GlobalParams.get("companyid"));
            KwlReturnObject creditnotes = accCreditNoteobj.getNormalCreditNotes(requestParams);
            List<CreditNote> creditNoteList = creditnotes.getEntityList();
            for (CreditNote creditNote : creditNoteList) {
                JournalEntry je = creditNote.getJournalEntry();
                double externalCurrencyRate = 0;
                Date creditNoteCreationDate = null;
                if (je != null) {
                    externalCurrencyRate = je.getExternalCurrencyRate();
//                    creditNoteCreationDate = je.getEntryDate();
                    creditNoteCreationDate = creditNote.getCreationDate();
                }
                String transactionCurrency = creditNote.getCurrency() != null ? creditNote.getCurrency().getCurrencyID() :  (String)GlobalParams.get("currencyid");
                KwlReturnObject baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, creditNote.getCnamount(), transactionCurrency, creditNoteCreationDate, externalCurrencyRate);
                double cnamountinbase = (Double) baseAmount.getEntityList().get(0);
                cnamountinbase = authHandler.round(cnamountinbase, companyid);
                HashMap<String, Object> creditMap = new HashMap<String, Object>();
                creditMap.put("cnid", creditNote.getID());
                creditMap.put("cnamountinbase", cnamountinbase);
                KwlReturnObject result = accCreditNoteobj.updateCreditNote(creditMap);
                count++;
            }
        } catch (Exception ex) {
            System.out.println("Exception in updateAmountInBaseCN: " + ex.getMessage());
        } finally {
            return count;
        }
    }

    private int updateAmountInBaseDN(HashMap<String, Object> GlobalParams) {
        int count = 0;
        HashMap<String, Object> requestParams = new HashMap();
        try {
            String companyid = (String) GlobalParams.get("companyid");
            requestParams.put("companyid", (String) GlobalParams.get("companyid"));
            KwlReturnObject debitnotes = accDebitNoteobj.getNormalDebitNotes(requestParams);
            List<DebitNote> debitNoteList = debitnotes.getEntityList();
            for (DebitNote debitNote : debitNoteList) {
                JournalEntry je = debitNote.getJournalEntry();
                double externalCurrencyRate = 0;
                Date debitNoteDate = null;
                if (je != null) {
                    externalCurrencyRate = je.getExternalCurrencyRate();
//                    debitNoteDate = je.getEntryDate();
                    debitNoteDate = debitNote.getCreationDate();
                }
                String transactionCurrency = debitNote.getCurrency() != null ? debitNote.getCurrency().getCurrencyID() : (String)GlobalParams.get("currencyid");
                KwlReturnObject baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, debitNote.getDnamount(), transactionCurrency, debitNoteDate, externalCurrencyRate);
                double dnamountinbase = (Double) baseAmount.getEntityList().get(0);
                dnamountinbase = authHandler.round(dnamountinbase, companyid);
                HashMap<String, Object> dnMap = new HashMap<String, Object>();
                dnMap.put("dnid", debitNote.getID());
                dnMap.put("dnamountinbase", dnamountinbase);
                KwlReturnObject result = accDebitNoteobj.updateDebitNote(dnMap);
                count++;
            }
        } catch (Exception ex) {
            System.out.println("Exception in updateAmountInBaseDN: " + ex.getMessage());
        } finally {
            return count;
        }
    }

    private int updateAmountInBaseReceipt(HashMap<String, Object> GlobalParams) {
        int count = 0;
        HashMap<String, Object> requestParams = new HashMap();
        try {
            String companyid = (String) GlobalParams.get("companyid");
            requestParams.put("companyid", (String) GlobalParams.get("companyid"));
            KwlReturnObject debitnotes = accReceiptobj.getNormalReceipts(requestParams);
            List<Receipt> receiptList = debitnotes.getEntityList();
            for (Receipt receipt : receiptList) {
                JournalEntry je = receipt.getJournalEntry();
                double externalCurrencyRate = 0;
                Date receiptDate = null;
                if (je != null) {
                    externalCurrencyRate = je.getExternalCurrencyRate();
//                    receiptDate = je.getEntryDate();
                    receiptDate = receipt.getCreationDate();
                }
                String transactionCurrency = receipt.getCurrency() != null ? receipt.getCurrency().getCurrencyID() :  (String)GlobalParams.get("currencyid");
                KwlReturnObject baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, receipt.getDepositAmount(), transactionCurrency, receiptDate, externalCurrencyRate);
                double depositamountinbase = (Double) baseAmount.getEntityList().get(0);
                depositamountinbase = authHandler.round(depositamountinbase, companyid);
                HashMap<String, Object> receiptMap = new HashMap<String, Object>();
                receiptMap.put("receiptid", receipt.getID());
                receiptMap.put("depositamountinbase", depositamountinbase);
                KwlReturnObject result = accReceiptobj.updateReceipt(receiptMap);
                count++;
            }
        } catch (Exception ex) {
            System.out.println("Exception in updateAmountInBaseReceipt: " + ex.getMessage());
        } finally {
            return count;
        }
    }
    
    private int updateAmountInBasePayment(HashMap<String, Object> GlobalParams) {
        int count = 0;
        HashMap<String, Object> requestParams = new HashMap();
        try {
            String companyid = (String) GlobalParams.get("companyid");
            requestParams.put("companyid", (String) GlobalParams.get("companyid"));
            KwlReturnObject debitnotes = accVendorPaymentobj.getNormalPayments(requestParams);
            List<Payment> paymentList = debitnotes.getEntityList();
            for (Payment payment : paymentList) {
                JournalEntry je = payment.getJournalEntry();
                double externalCurrencyRate = 0;
                Date paymentDate = null;
                if (je != null) {
                    externalCurrencyRate = je.getExternalCurrencyRate();
//                    paymentDate = je.getEntryDate();
                    paymentDate = payment.getCreationDate();
                }
                String paycurrencyid = (payment.getCurrency() == null ? (String) GlobalParams.get("currencyid") : payment.getCurrency().getCurrencyID());
                KwlReturnObject baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, payment.getDepositAmount(), paycurrencyid, paymentDate, externalCurrencyRate);
                double depositamountinbase = (Double) baseAmount.getEntityList().get(0);
                depositamountinbase = authHandler.round(depositamountinbase, companyid);
                HashMap<String, Object> receiptMap = new HashMap<String, Object>();
                receiptMap.put("paymentid", payment.getID());
                receiptMap.put("depositamountinbase", depositamountinbase);
                KwlReturnObject result = accVendorPaymentobj.updatePayment(receiptMap);
                count++;
            }
        } catch (Exception ex) {
            System.out.println("Exception in updateAmountInBasePayment: " + ex.getMessage());
        } finally {
            return count;
        }
    }
     public ModelAndView updateQuotationAmount(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        int quotationsUpdated = 0;
        boolean issuccess = false;
        try {
            String companyid = request.getParameter("companyid");
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", companyid);

            KwlReturnObject result = accSalesOrderDAOobj.getAllQuotaionsByCompanyid(requestParams);
            List<Quotation> quotationList = result.getEntityList();
            for (Quotation quotation : quotationList) {

                Set<QuotationDetail> rows = quotation.getRows();
                Iterator itrRow = rows.iterator();
                double subTotal = 0, totalDiscount = 0, priceExcludingDiscount = 0, totalRowDiscount = 0, taxPercent = 0, totalTermAmount = 0, taxableTermAmount = 0;
                String quotationcurrency = quotation.getCurrency() == null ? quotation.getCompany().getCurrency().getCurrencyID() : quotation.getCurrency().getCurrencyID();

                while (itrRow.hasNext()) {
                    QuotationDetail sod = (QuotationDetail) itrRow.next();

                    double qrate = authHandler.roundUnitPrice(sod.getRate(), companyid);
                    double quantity = authHandler.roundQuantity(sod.getQuantity(), companyid);
                    boolean gstIncluded = quotation.isGstIncluded();
                    if (gstIncluded) {
                        qrate = sod.getRateincludegst();
                    }
                    double quotationPrice = authHandler.round(quantity * qrate, companyid);
                    double discountQD = authHandler.round(sod.getDiscount(), companyid);
                    double discountPerRow = 0;

                    if (sod.getDiscountispercent() == 1) {
                        discountPerRow = authHandler.round((quotationPrice * discountQD / 100), companyid);
                        priceExcludingDiscount = (quotationPrice) - discountPerRow;
                    } else {
                        priceExcludingDiscount = quotationPrice - discountQD;
                        discountPerRow = discountQD;
                    }
                    subTotal += priceExcludingDiscount;
                    totalRowDiscount += discountPerRow;
                    if (!gstIncluded) {                             //if gst not included add row level tax to subtotal
                        subTotal = subTotal + authHandler.round(sod.getRowTaxAmount(), companyid);
                    }
                }
                totalRowDiscount = authHandler.round(totalRowDiscount, companyid);

                // Calculation if Global level tax present
                double discountQ = authHandler.round(quotation.getDiscount(), companyid);
                if (discountQ != 0) {
                    if (quotation.isPerDiscount()) {
                        totalDiscount = subTotal * discountQ / 100;
                        subTotal = subTotal - totalDiscount;
                    } else {
                        subTotal = subTotal - discountQ;
                        totalDiscount = discountQ;
                    }
                }

                requestParams.put("gcurrencyid", quotation.getCompany().getCurrency().getCurrencyID());
                HashMap<String, Object> requestParam = new HashMap();
                HashMap<String, Object> filterrequestParams = new HashMap();
                requestParam.put("quotation", quotation.getID());
                KwlReturnObject quotationResult = null;
                filterrequestParams.put("taxid", quotation.getTax() == null ? "" : quotation.getTax().getID());
                quotationResult = accSalesOrderDAOobj.getQuotationTermMap(requestParam);
                List<QuotationTermMap> termMap = quotationResult.getEntityList();

                // Calculate Total Term Amount here...
                for (QuotationTermMap quotationTermMap : termMap) {
                    filterrequestParams.put("term", quotationTermMap.getTerm() == null ? "" : quotationTermMap.getTerm().getId());
                    InvoiceTermsSales mt = quotationTermMap.getTerm();
                    double termAmnt = quotationTermMap.getTermamount();
                    totalTermAmount = totalTermAmount + authHandler.round(termAmnt, companyid);
                    boolean isTermMappedwithTax = accTaxObj.isTermMappedwithTax(filterrequestParams);

                    if (isTermMappedwithTax) {
                        taxableTermAmount += termAmnt;
                    }
                }

                if (quotation.getTax() != null) {                   // Get Tax percent
                    requestParams.put("transactiondate", quotation.getQuotationDate());
                    requestParams.put("taxid", quotation.getTax().getID());
                    KwlReturnObject taxlist = accTaxObj.getTax(requestParams);
                    List taxList = taxlist.getEntityList();
                    Object[] taxObj = (Object[]) taxList.get(0);
                    taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                }

                double globaltax = (taxPercent == 0 ? 0 : authHandler.round(((subTotal + taxableTermAmount) * taxPercent / 100), companyid));

                double totalAmount = subTotal + globaltax + totalTermAmount;

                KwlReturnObject tAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, totalAmount, quotationcurrency, quotation.getQuotationDate(), quotation.getExternalCurrencyRate());
                double totalAmountInbase = authHandler.round((Double) tAmt.getEntityList().get(0), companyid);

                KwlReturnObject discAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, totalRowDiscount, quotationcurrency, quotation.getQuotationDate(), quotation.getExternalCurrencyRate());
                double discountinBase = authHandler.round((Double) discAmt.getEntityList().get(0), companyid);

                JSONObject tempParams = new JSONObject();
                tempParams.put("id", quotation.getID());
                tempParams.put("quotationamount", totalAmount);
                tempParams.put("quotationamountinbase", totalAmountInbase);
                tempParams.put("discountinbase", discountinBase);
                tempParams.put("totallineleveldiscount", totalRowDiscount);
                boolean success = accSalesOrderDAOobj.updateQuotationAmount(quotation, tempParams);
                if (success) {
                    quotationsUpdated++;
                }
            }
            issuccess = true;
        } catch (Exception ex) {
            System.out.println("------ Error at updateQuotationAmount() ----------");
            ex.printStackTrace();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("totalQuotationsUpdated", quotationsUpdated);
            } catch (JSONException ex) {
                System.out.println("------ Error at updateQuotationAmount() ----------");
                ex.printStackTrace();
            }
        }

        return new ModelAndView("jsonView", "model", jobj.toString());

    }

       public ModelAndView updateVendorQuotationAmount(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        int quotationsUpdated = 0;
        boolean issuccess = false;
        try {
            String companyid = request.getParameter("companyid");
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", companyid);

            KwlReturnObject result = accPurchaseOrderobj.getAllVendorQuotaionsByCompanyid(requestParams);
            List<VendorQuotation> quotationList = result.getEntityList();
            for (VendorQuotation quotation : quotationList) {

                Set<VendorQuotationDetail> rows = quotation.getRows();
                Iterator itrRow = rows.iterator();
                double subTotal = 0, totalDiscount = 0, priceExcludingDiscount = 0, totalRowDiscount = 0, taxPercent = 0, totalTermAmount = 0, taxableTermAmount = 0;
                String quotationcurrency = quotation.getCurrency() == null ? quotation.getCompany().getCurrency().getCurrencyID() : quotation.getCurrency().getCurrencyID();

                while (itrRow.hasNext()) {
                    VendorQuotationDetail sod = (VendorQuotationDetail) itrRow.next();

                    double qrate = authHandler.roundUnitPrice(sod.getRate(), companyid);
                    double quantity = authHandler.roundQuantity(sod.getQuantity(), companyid);
                    boolean gstIncluded = quotation.isGstIncluded();
                    if (gstIncluded) {
                        qrate = sod.getRateincludegst();
                    }
                    double quotationPrice = authHandler.round(quantity * qrate, companyid);
                    double discountQD = authHandler.round(sod.getDiscount(), companyid);
                    double discountPerRow = 0;

                    if (sod.getDiscountispercent() == 1) {
                        discountPerRow = authHandler.round((quotationPrice * discountQD / 100), companyid);
                        priceExcludingDiscount = (quotationPrice) - discountPerRow;
                    } else {
                        priceExcludingDiscount = quotationPrice - discountQD;
                        discountPerRow = discountQD;
                    }
                    subTotal += priceExcludingDiscount;
                    totalRowDiscount += discountPerRow;
                    if (!gstIncluded) {                             //if gst not included add row level tax to subtotal
                        subTotal = subTotal + authHandler.round(sod.getRowTaxAmount(), companyid);
                    }
                }
                totalRowDiscount = authHandler.round(totalRowDiscount, companyid);

                // Calculation if Global level tax present
                double discountQ = authHandler.round(quotation.getDiscount(), companyid);
                if (discountQ != 0) {
                    if (quotation.isPerDiscount()) {
                        totalDiscount = subTotal * discountQ / 100;
                        subTotal = subTotal - totalDiscount;
                    } else {
                        subTotal = subTotal - discountQ;
                        totalDiscount = discountQ;
                    }
                }

                requestParams.put("gcurrencyid", quotation.getCompany().getCurrency().getCurrencyID());
                HashMap<String, Object> requestParam = new HashMap();
                HashMap<String, Object> filterrequestParams = new HashMap();
                requestParam.put("vendorQuotation", quotation.getID());
                KwlReturnObject quotationResult = null;
                filterrequestParams.put("taxid", quotation.getTax() == null ? "" : quotation.getTax().getID());
                quotationResult = accPurchaseOrderobj.getVendorQuotationTermMap(requestParam);
                List<VendorQuotationTermMap> termMap = quotationResult.getEntityList();

                // Calculate Total Term Amount here...
                for (VendorQuotationTermMap quotationTermMap : termMap) {
                    filterrequestParams.put("term", quotationTermMap.getTerm() == null ? "" : quotationTermMap.getTerm().getId());
                    InvoiceTermsSales mt = quotationTermMap.getTerm();
                    double termAmnt = quotationTermMap.getTermamount();
                    totalTermAmount = totalTermAmount + authHandler.round(termAmnt, companyid);
                    boolean isTermMappedwithTax = accTaxObj.isTermMappedwithTax(filterrequestParams);

                    if (isTermMappedwithTax) {
                        taxableTermAmount += termAmnt;
                    }
                }

                if (quotation.getTax() != null) {                   // Get Tax percent
                    requestParams.put("transactiondate", quotation.getQuotationDate());
                    requestParams.put("taxid", quotation.getTax().getID());
                    KwlReturnObject taxlist = accTaxObj.getTax(requestParams);
                    List taxList = taxlist.getEntityList();
                    Object[] taxObj = (Object[]) taxList.get(0);
                    taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                }

                double globaltax = (taxPercent == 0 ? 0 : authHandler.round(((subTotal + taxableTermAmount) * taxPercent / 100), companyid));

                double totalAmount = subTotal + globaltax + totalTermAmount;

                KwlReturnObject tAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, totalAmount, quotationcurrency, quotation.getQuotationDate(), quotation.getExternalCurrencyRate());
                double totalAmountInbase = authHandler.round((Double) tAmt.getEntityList().get(0), companyid);

                KwlReturnObject discAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, totalRowDiscount, quotationcurrency, quotation.getQuotationDate(), quotation.getExternalCurrencyRate());
                double discountinBase = authHandler.round((Double) discAmt.getEntityList().get(0), companyid);

                JSONObject tempParams = new JSONObject();
                tempParams.put("id", quotation.getID());
                tempParams.put("quotationamount", totalAmount);
                tempParams.put("quotationamountinbase", totalAmountInbase);
                tempParams.put("discountinbase", discountinBase);
                tempParams.put("totallineleveldiscount", totalRowDiscount);
                boolean success = accPurchaseOrderobj.updateVendorQuotationAmount(quotation, tempParams);
                if (success) {
                    quotationsUpdated++;
                }
            }
            issuccess = true;
        } catch (Exception ex) {
            System.out.println("------ Error at updateQuotationAmount() ----------");
            ex.printStackTrace();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("totalQuotationsUpdated", quotationsUpdated);
            } catch (JSONException ex) {
                System.out.println("------ Error at updateQuotationAmount() ----------");
                ex.printStackTrace();
            }
        }

        return new ModelAndView("jsonView", "model", jobj.toString());

    }
       
    public ModelAndView updateAmountInPOandSO(HttpServletRequest request, HttpServletResponse response) {
        StringBuffer msg = new StringBuffer();
        try {
            String companyid = request.getParameter("companyid");
            HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
            GlobalParams.put("companyid", companyid);
            if (!StringUtil.isNullOrEmpty(companyid)) {
                int count = updateTotalSOAmount(GlobalParams);
                msg.append("<br> Sales Order Updated- " + count);
                count = updateTotalPOAmount(GlobalParams);
                msg.append("<br> Purchase Order Updated- " + count);
            }
        } catch (Exception ex) {
            System.out.println("Exception occured-" + ex.getMessage());
        } finally {
            return new ModelAndView("jsonView_ex", "model", msg.toString());
        }
    }
       
       private int updateTotalSOAmount(HashMap<String, Object> GlobalParams) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        int totalSOUpdated = 0;
        boolean issuccess = false;
        try {
            String companyid = (String) GlobalParams.get("companyid");
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", companyid);

            KwlReturnObject result = accSalesOrderDAOobj.getAllSalesOrderByCompanyid(requestParams);
            List<SalesOrder> soList = result.getEntityList();
            for (SalesOrder so : soList) {

                Set<SalesOrderDetail> rows = so.getRows();
                Iterator itrRow = rows.iterator();
                double subTotal = 0, totalDiscount = 0, priceExcludingDiscount = 0, totalRowDiscount = 0, taxPercent = 0, totalTermAmount = 0, taxableTermAmount = 0;
                String salesOrdercurrency = so.getCurrency() == null ? so.getCompany().getCurrency().getCurrencyID() : so.getCurrency().getCurrencyID();

                while (itrRow.hasNext()) {
                    SalesOrderDetail sod = (SalesOrderDetail) itrRow.next();

                    double qrate = authHandler.roundUnitPrice(sod.getRate(), companyid);
                    double quantity = authHandler.roundQuantity(sod.getQuantity(), companyid);
                    boolean gstIncluded = so.isGstIncluded();
                    if (gstIncluded) {
                        qrate = sod.getRateincludegst();
                    }
                    double quotationPrice = authHandler.round(quantity * qrate, companyid);
                    double discountQD = authHandler.round(sod.getDiscount(), companyid);
                    double discountPerRow = 0;

                    if (sod.getDiscountispercent() == 1) {
                        discountPerRow = authHandler.round((quotationPrice * discountQD / 100), companyid);
                        priceExcludingDiscount = (quotationPrice) - discountPerRow;
                    } else {
                        priceExcludingDiscount = quotationPrice - discountQD;
                        discountPerRow = discountQD;
                    }
                    subTotal += priceExcludingDiscount;
                    totalRowDiscount += discountPerRow;
                    if (!gstIncluded) {                             //if gst not included add row level tax to subtotal
                        subTotal = subTotal + authHandler.round(sod.getRowTaxAmount(), companyid);
                    }
                }
                totalRowDiscount = authHandler.round(totalRowDiscount, companyid);

                // Calculation if Global level tax present
                double discountQ = authHandler.round(so.getDiscount(), companyid);
                if (discountQ != 0) {
                    if (so.isPerDiscount()) {
                        totalDiscount = subTotal * discountQ / 100;
                        subTotal = subTotal - totalDiscount;
                    } else {
                        subTotal = subTotal - discountQ;
                        totalDiscount = discountQ;
                    }
                }

                requestParams.put("gcurrencyid", so.getCompany().getCurrency().getCurrencyID());
                HashMap<String, Object> requestParam = new HashMap();
                HashMap<String, Object> filterrequestParams = new HashMap();
                requestParam.put("salesOrder", so.getID());
                KwlReturnObject SOtermResult = null;
                filterrequestParams.put("taxid", so.getTax() == null ? "" : so.getTax().getID());
                SOtermResult = accSalesOrderDAOobj.getSalesOrderTermMap(requestParam);
                List<SalesOrderTermMap> termMap = SOtermResult.getEntityList();

                // Calculate Total Term Amount here...
                for (SalesOrderTermMap soTermMap : termMap) {
                    filterrequestParams.put("term", soTermMap.getTerm() == null ? "" : soTermMap.getTerm().getId());
                    InvoiceTermsSales mt = soTermMap.getTerm();
                    double termAmnt = soTermMap.getTermamount();
                    totalTermAmount = totalTermAmount + authHandler.round(termAmnt, companyid);
                    boolean isTermMappedwithTax = accTaxObj.isTermMappedwithTax(filterrequestParams);

                    if (isTermMappedwithTax) {
                        taxableTermAmount += termAmnt;
                    }
                }

                if (so.getTax() != null) {                   // Get Tax percent
                    requestParams.put("transactiondate", so.getOrderDate());
                    requestParams.put("taxid", so.getTax().getID());
                    KwlReturnObject taxlist = accTaxObj.getTax(requestParams);
                    List taxList = taxlist.getEntityList();
                    Object[] taxObj = (Object[]) taxList.get(0);
                    taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                }

                double globaltax = (taxPercent == 0 ? 0 : authHandler.round(((subTotal + taxableTermAmount) * taxPercent / 100), companyid));

                double totalAmount = subTotal + globaltax + totalTermAmount;

                KwlReturnObject tAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, totalAmount, salesOrdercurrency, so.getOrderDate(), so.getExternalCurrencyRate());
                double totalAmountInbase = authHandler.round((Double) tAmt.getEntityList().get(0), companyid);

                KwlReturnObject discAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, totalRowDiscount, salesOrdercurrency, so.getOrderDate(), so.getExternalCurrencyRate());
                double discountinBase = authHandler.round((Double) discAmt.getEntityList().get(0), companyid);

                 JSONObject tempParams = new JSONObject();
                tempParams.put("id", so.getID());
                tempParams.put("totalamount", totalAmount);
                tempParams.put("totalamountinbase", totalAmountInbase);
                tempParams.put("discountinbase", discountinBase);
                tempParams.put("totallineleveldiscount", totalRowDiscount);
                boolean success = accSalesOrderDAOobj.updateSalesOrderAmount(so, tempParams);
                if (success) {
                    totalSOUpdated++;
                }
            }
            issuccess = true;
        } catch (Exception ex) {
            System.out.println("------ Error at updateTotalSOAmount() ----------");
            ex.printStackTrace();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("totalSalesOrderUpdated", totalSOUpdated);
            } catch (JSONException ex) {
                System.out.println("------ Error at updateTotalSOAmount() ----------");
                ex.printStackTrace();
            }
        }

        return totalSOUpdated;

    }
       
       private int updateTotalPOAmount(HashMap<String, Object> GlobalParams) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        int totalPOUpdated = 0;
        boolean issuccess = false;
        try {
            String companyid = (String) GlobalParams.get("companyid");
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", companyid);

            KwlReturnObject result = accPurchaseOrderobj.getAllPurchaseOrderByCompanyid(requestParams);
            List<PurchaseOrder> poList = result.getEntityList();
            for (PurchaseOrder po : poList) {

                Set<PurchaseOrderDetail> rows = po.getRows();
                Iterator itrRow = rows.iterator();
                double subTotal = 0, totalDiscount = 0, priceExcludingDiscount = 0, totalRowDiscount = 0, taxPercent = 0, totalTermAmount = 0, taxableTermAmount = 0;
                String pocurrency = po.getCurrency() == null ? po.getCompany().getCurrency().getCurrencyID() : po.getCurrency().getCurrencyID();

                while (itrRow.hasNext()) {
                    PurchaseOrderDetail sod = (PurchaseOrderDetail) itrRow.next();

                    double qrate = authHandler.roundUnitPrice(sod.getRate(), companyid);
                    double quantity = authHandler.roundQuantity(sod.getQuantity(), companyid);
                    boolean gstIncluded = po.isGstIncluded();
                    if (gstIncluded) {
                        qrate = sod.getRateincludegst();
                    }
                    double quotationPrice = authHandler.round(quantity * qrate, companyid);
                    double discountQD = authHandler.round(sod.getDiscount(), companyid);
                    double discountPerRow = 0;

                    if (sod.getDiscountispercent() == 1) {
                        discountPerRow = authHandler.round((quotationPrice * discountQD / 100), companyid);
                        priceExcludingDiscount = (quotationPrice) - discountPerRow;
                    } else {
                        priceExcludingDiscount = quotationPrice - discountQD;
                        discountPerRow = discountQD;
                    }
                    subTotal += priceExcludingDiscount;
                    totalRowDiscount += discountPerRow;
                    if (!gstIncluded) {                             //if gst not included add row level tax to subtotal
                        subTotal = subTotal + authHandler.round(sod.getRowTaxAmount(), companyid);
                    }
                }
                totalRowDiscount = authHandler.round(totalRowDiscount, companyid);

                // Calculation if Global level tax present
                double discountQ = authHandler.round(po.getDiscount(), companyid);
                if (discountQ != 0) {
                    if (po.isPerDiscount()) {
                        totalDiscount = subTotal * discountQ / 100;
                        subTotal = subTotal - totalDiscount;
                    } else {
                        subTotal = subTotal - discountQ;
                        totalDiscount = discountQ;
                    }
                }

                requestParams.put("gcurrencyid", po.getCompany().getCurrency().getCurrencyID());
                HashMap<String, Object> requestParam = new HashMap();
                HashMap<String, Object> filterrequestParams = new HashMap();
                requestParam.put("purchaseOrder", po.getID());
                KwlReturnObject poTermResult = null;
                filterrequestParams.put("taxid", po.getTax() == null ? "" : po.getTax().getID());
                poTermResult = accPurchaseOrderobj.getPurchaseOrderTermMap(requestParam);
                List<PurchaseOrderTermMap> termMap = poTermResult.getEntityList();

                // Calculate Total Term Amount here...
                for (PurchaseOrderTermMap poTermMap : termMap) {
                    filterrequestParams.put("term", poTermMap.getTerm() == null ? "" : poTermMap.getTerm().getId());
                    InvoiceTermsSales mt = poTermMap.getTerm();
                    double termAmnt = poTermMap.getTermamount();
                    totalTermAmount = totalTermAmount + authHandler.round(termAmnt, companyid);
                    boolean isTermMappedwithTax = accTaxObj.isTermMappedwithTax(filterrequestParams);

                    if (isTermMappedwithTax) {
                        taxableTermAmount += termAmnt;
                    }
                }

                if (po.getTax() != null) {                   // Get Tax percent
                    requestParams.put("transactiondate", po.getOrderDate());
                    requestParams.put("taxid", po.getTax().getID());
                    KwlReturnObject taxlist = accTaxObj.getTax(requestParams);
                    List taxList = taxlist.getEntityList();
                    Object[] taxObj = (Object[]) taxList.get(0);
                    taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                }

                double globaltax = (taxPercent == 0 ? 0 : authHandler.round(((subTotal + taxableTermAmount) * taxPercent / 100), companyid));

                double totalAmount = subTotal + globaltax + totalTermAmount;

                KwlReturnObject tAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, totalAmount, pocurrency, po.getOrderDate(), po.getExternalCurrencyRate());
                double totalAmountInbase = authHandler.round((Double) tAmt.getEntityList().get(0), companyid);

                KwlReturnObject discAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, totalRowDiscount, pocurrency, po.getOrderDate(), po.getExternalCurrencyRate());
                double discountinBase = authHandler.round((Double) discAmt.getEntityList().get(0), companyid);

                JSONObject tempParams = new JSONObject();
                tempParams.put("id", po.getID());
                tempParams.put("totalamount", totalAmount);
                tempParams.put("totalamountinbase", totalAmountInbase);
                tempParams.put("discountinbase", discountinBase);
                tempParams.put("totallineleveldiscount", totalRowDiscount);
                
                boolean success = accPurchaseOrderobj.updatePurchaseOrderAmount(po, tempParams);
                if (success) {
                    totalPOUpdated++;
                }
                
            }
            issuccess = true;
        } catch (Exception ex) {
            System.out.println("------ Error at updateTotalPOAmount() ----------");
            ex.printStackTrace();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("totalPurchaseOrderUpdated", totalPOUpdated);
            } catch (JSONException ex) {
                System.out.println("------ Error at updateTotalPOAmount() ----------");
                ex.printStackTrace();
            }
        }

        return totalPOUpdated;

    }

       public ModelAndView updateGoodsReceiptOrderAmount(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        int quotationsUpdated = 0;
        boolean issuccess = false;
        try {
            String companyid = request.getParameter("companyid");
            String currencyid = request.getParameter("currencyid");
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", companyid);

            KwlReturnObject result = accGoodsReceiptobj.getGoodsReceiptsOrderByCompany(requestParams);
            List<GoodsReceiptOrder> orderList = result.getEntityList();
            for (GoodsReceiptOrder grOrder : orderList) {
                Set<GoodsReceiptOrderDetails> groDetails = grOrder.getRows();
                double totalAmount = 0;
                double subtotal=0;
                double quantity = 0;
                double discountAmount = 0;
                double discountAmountInBase = 0;
                double taxAmount = 0;
                if (groDetails != null && !groDetails.isEmpty()) {
                    for (GoodsReceiptOrderDetails cnt : groDetails) {
                        quantity = cnt.getInventory().getQuantity();
                        totalAmount += authHandler.round(cnt.getRate() * quantity, companyid);
                        subtotal=authHandler.round(cnt.getRate() * quantity, companyid);
                        taxAmount += authHandler.round(cnt.getRowTaxAmount(), companyid);
                        if (cnt.getDiscountispercent() == 1) {
                            discountAmount += authHandler.round((subtotal * cnt.getDiscount() / 100), companyid);
                        } else {
                            discountAmount += authHandler.round(cnt.getDiscount(), companyid);
                        }
                    }
                }
                double totalAmountInDocumentCurrecy = totalAmount - discountAmount;
                double taxPercent = 0;
                if (grOrder.getTax() != null) {
                    KwlReturnObject taxresult = accTaxObj.getTaxPercent(companyid, grOrder.getOrderDate(), grOrder.getTax().getID());
                    taxPercent = (Double) taxresult.getEntityList().get(0);
                    double ordertaxamount = (taxPercent == 0 ? 0 : authHandler.round((totalAmountInDocumentCurrecy * taxPercent / 100), companyid));
                    taxAmount += ordertaxamount;
                }
                totalAmountInDocumentCurrecy = totalAmountInDocumentCurrecy + taxAmount;
                double totalAmountInBaseCurrecy = 0;
                if (grOrder.getCurrency() != null) {
                    HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                    filterRequestParams.put("companyid", companyid);
                    filterRequestParams.put("gcurrencyid", currencyid);
                    KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, totalAmountInDocumentCurrecy, grOrder.getCurrency().getCurrencyID(), grOrder.getOrderDate(), grOrder.getExternalCurrencyRate());
                    totalAmountInBaseCurrecy = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                    KwlReturnObject bAmt1 = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, discountAmount, grOrder.getCurrency().getCurrencyID(), grOrder.getOrderDate(), grOrder.getExternalCurrencyRate());
                    discountAmountInBase = authHandler.round((Double) bAmt1.getEntityList().get(0), companyid);
                }
                totalAmountInDocumentCurrecy = authHandler.round(totalAmountInDocumentCurrecy, companyid);
                JSONObject tempParams = new JSONObject();
                tempParams.put("id", grOrder.getID());
                tempParams.put("discountAmountInBase", discountAmountInBase);
                tempParams.put("totalAmountInDocumentCurrecy", totalAmountInDocumentCurrecy);
                tempParams.put("totalAmountInBaseCurrecy", totalAmountInBaseCurrecy);
                boolean success = accGoodsReceiptobj.updateGoodsReceiptOrderAmount(grOrder, tempParams);
                if (success) {
                    quotationsUpdated++;
                }
            }
            issuccess = true;
        } catch (Exception ex) {
            System.out.println("------ Error at updateOrderAmount() ----------");
            ex.printStackTrace();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("totalOrdersUpdated", quotationsUpdated);
            } catch (JSONException ex) {
                System.out.println("------ Error at updateOrderAmount() ----------");
                ex.printStackTrace();
            }
        }

        return new ModelAndView("jsonView", "model", jobj.toString());

    }
       public ModelAndView updateDeliveryOrderAmount(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        int quotationsUpdated = 0;
        boolean issuccess = false;
        try {
            String companyid = request.getParameter("companyid");
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", companyid);
            String currencyid = request.getParameter("currencyid");

            KwlReturnObject result = accInvoiceDAOobj.getDeliveryOrderByCompany(requestParams);
            List<DeliveryOrder> orderList = result.getEntityList();
            for (DeliveryOrder grOrder : orderList) {
                Set<DeliveryOrderDetail> groDetails = grOrder.getRows();
                double totalAmount = 0;
                double subtotal=0;
                double quantity = 0;
                double discountAmount = 0;
                double discountAmountInBase = 0;
                double taxAmount = 0;
                if (groDetails != null && !groDetails.isEmpty()) {
                    for (DeliveryOrderDetail cnt : groDetails) {
                        quantity = cnt.getInventory().getQuantity();
                        totalAmount += authHandler.round(cnt.getRate() * quantity, companyid);
                        subtotal=authHandler.round(cnt.getRate() * quantity, companyid);
                        taxAmount += authHandler.round(cnt.getRowTaxAmount(), companyid);
                        if (cnt.getDiscountispercent() == 1) {
                            discountAmount += authHandler.round((subtotal * cnt.getDiscount() / 100), companyid);
                        } else {
                            discountAmount += authHandler.round(cnt.getDiscount(), companyid);
                        }
                    }
                }
                double totalAmountInDocumentCurrecy = totalAmount - discountAmount;
                double taxPercent = 0;
                if (grOrder.getTax() != null) {
                    KwlReturnObject taxresult = accTaxObj.getTaxPercent(companyid, grOrder.getOrderDate(), grOrder.getTax().getID());
                    taxPercent = (Double) taxresult.getEntityList().get(0);
                    double ordertaxamount = (taxPercent == 0 ? 0 : authHandler.round((totalAmountInDocumentCurrecy * taxPercent / 100), companyid));
                    taxAmount += ordertaxamount;
                }
                totalAmountInDocumentCurrecy = totalAmountInDocumentCurrecy + taxAmount;
                double totalAmountInBaseCurrecy = 0;
                if (grOrder.getCurrency() != null) {
                    HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                    filterRequestParams.put("companyid", companyid);
                    filterRequestParams.put("gcurrencyid", currencyid);
                    KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, totalAmountInDocumentCurrecy, grOrder.getCurrency().getCurrencyID(), grOrder.getOrderDate(), grOrder.getExternalCurrencyRate());
                    totalAmountInBaseCurrecy = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                    KwlReturnObject bAmt1 = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, discountAmount, grOrder.getCurrency().getCurrencyID(), grOrder.getOrderDate(), grOrder.getExternalCurrencyRate());
                    discountAmountInBase = authHandler.round((Double) bAmt1.getEntityList().get(0), companyid);
                }
                totalAmountInDocumentCurrecy = authHandler.round(totalAmountInDocumentCurrecy, companyid);
                JSONObject tempParams = new JSONObject();
                tempParams.put("id", grOrder.getID());
                tempParams.put("discountAmountInBase", discountAmountInBase);
                tempParams.put("totalAmountInDocumentCurrecy", totalAmountInDocumentCurrecy);
                tempParams.put("totalAmountInBaseCurrecy", totalAmountInBaseCurrecy);
                boolean success = accInvoiceDAOobj.updateDeliveryOrderAmount(grOrder, tempParams);
                if (success) {
                    quotationsUpdated++;
                }
            }
            issuccess = true;
        } catch (Exception ex) {
            System.out.println("------ Error at updateOrderAmount() ----------");
            ex.printStackTrace();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("totalOrdersUpdated", quotationsUpdated);
            } catch (JSONException ex) {
                System.out.println("------ Error at updateOrderAmount() ----------");
                ex.printStackTrace();
            }
        }

        return new ModelAndView("jsonView", "model", jobj.toString());

    }
       
       public ModelAndView updateAmountInPRandSR(HttpServletRequest request, HttpServletResponse response) {
        StringBuffer msg = new StringBuffer();
        try {
            String companyid = request.getParameter("companyid");
            HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
            GlobalParams.put("companyid", companyid);
            if (!StringUtil.isNullOrEmpty(companyid)) {
                int count = updateTotalPRAmountAndDiscount(GlobalParams);
                msg.append("<br> Purchase Return Updated- " + count);
               count = updateTotalSRAmountAndDiscount(GlobalParams);
               msg.append("<br> Sales Return Updated- " + count);
            }
        } catch (Exception ex) {
            System.out.println("Exception occured-" + ex.getMessage());
        } finally {
            return new ModelAndView("jsonView_ex", "model", msg.toString());
        }
    }
      
    private int updateTotalPRAmountAndDiscount(HashMap<String, Object> GlobalParams) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        int totalPRUpdated = 0;
        boolean issuccess = false;
        try {
            String companyid = (String) GlobalParams.get("companyid");
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", companyid);

            KwlReturnObject result = accPurchaseOrderobj.getAllPurchaseReturnByCompanyid(requestParams);
            List<PurchaseReturn> prList = result.getEntityList();
            for (PurchaseReturn pr : prList) {

                Set<PurchaseReturnDetail> rows = pr.getRows();
                Iterator itrRow = rows.iterator();
                double totalAmount = 0, discountinBase = 0, totalRowDiscount = 0;
                String pocurrency = pr.getCurrency() == null ? pr.getCompany().getCurrency().getCurrencyID() : pr.getCurrency().getCurrencyID();

                while (itrRow.hasNext()) {
                    PurchaseReturnDetail prd = (PurchaseReturnDetail) itrRow.next();

                    double quantity = prd.getInventory().getQuantity();
                    double rowAmt = prd.getRate() * quantity;

                    double discountPerRow = 0;

                    if (prd.getDiscountispercent() == 1) {
                        discountPerRow = rowAmt * prd.getDiscount() / 100;
                    } else {
                        discountPerRow = prd.getDiscount();
                    }

                    rowAmt = rowAmt - discountPerRow;

                    totalRowDiscount += discountPerRow;
                    // getting tax also

                    double taxAmt = prd.getRowTaxAmount();
                    rowAmt += taxAmt;

                    totalAmount += rowAmt;

                }

                JSONObject tempParams = new JSONObject();
                tempParams.put("totalamount", totalAmount);

                tempParams.put("totallineleveldiscount", totalRowDiscount);
                requestParams.put("gcurrencyid", pr.getCompany().getCurrency().getCurrencyID());

                if (pr.getCurrency() != null) {
                    KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, totalAmount, pocurrency, pr.getOrderDate(), pr.getExternalCurrencyRate());
                    tempParams.put("totalamountinbase", authHandler.round((Double) bAmt.getEntityList().get(0), companyid));
                }
                KwlReturnObject descbAmtTax = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, totalRowDiscount, pocurrency, pr.getOrderDate(), pr.getExternalCurrencyRate());
                discountinBase = authHandler.round((Double) descbAmtTax.getEntityList().get(0), companyid);

                tempParams.put("discountinbase", discountinBase);


                boolean success = accSalesReturnService.updatePurchasereturnAmount(pr, tempParams);
                if (success) {
                    totalPRUpdated++;
                }

            }
            issuccess = true;
        } catch (Exception ex) {
            System.out.println("------ Error at updateTotalPRAmount() ----------");
            ex.printStackTrace();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("totalPurchaseReturnUpdated", totalPRUpdated);
            } catch (JSONException ex) {
                System.out.println("------ Error at updateTotalPRAmount() ----------");
                ex.printStackTrace();
            }
        }

        return totalPRUpdated;

    }
    
     private int updateTotalSRAmountAndDiscount(HashMap<String, Object> GlobalParams) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        int totalSRUpdated = 0;
        boolean issuccess = false;
        try {
            String companyid = (String) GlobalParams.get("companyid");
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", companyid);

            KwlReturnObject result = accPurchaseOrderobj.getAllSalesReturnByCompanyid(requestParams);
            List<SalesReturn> srList = result.getEntityList();
            for (SalesReturn sr : srList) {

                Set<SalesReturnDetail> rows = sr.getRows();
                Iterator itrRow = rows.iterator();
                double totalAmount = 0, discountinBase = 0, totalRowDiscount = 0;
                String pocurrency = sr.getCurrency() == null ? sr.getCompany().getCurrency().getCurrencyID() : sr.getCurrency().getCurrencyID();

                while (itrRow.hasNext()) {
                    SalesReturnDetail srd = (SalesReturnDetail) itrRow.next();

                    double quantity = srd.getInventory().getQuantity();
                    double rowAmt = srd.getRate() * quantity;

                    double discountPerRow = 0;

                    if (srd.getDiscountispercent() == 1) {
                        discountPerRow = rowAmt * srd.getDiscount() / 100;
                    } else {
                        discountPerRow = srd.getDiscount();
                    }

                    rowAmt = rowAmt - discountPerRow;

                    totalRowDiscount += discountPerRow;
                    // getting tax also

                    double taxAmt = srd.getRowTaxAmount();
                    rowAmt += taxAmt;

                    totalAmount += rowAmt;

                }

                JSONObject tempParams = new JSONObject();
                tempParams.put("totalamount", totalAmount);

                tempParams.put("totallineleveldiscount", totalRowDiscount);
                requestParams.put("gcurrencyid", sr.getCompany().getCurrency().getCurrencyID());

                if (sr.getCurrency() != null) {
                    KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, totalAmount, pocurrency, sr.getOrderDate(), sr.getExternalCurrencyRate());
                    tempParams.put("totalamountinbase", authHandler.round((Double) bAmt.getEntityList().get(0), companyid));
                }
                KwlReturnObject descbAmtTax = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, totalRowDiscount, pocurrency, sr.getOrderDate(), sr.getExternalCurrencyRate());
                discountinBase = authHandler.round((Double) descbAmtTax.getEntityList().get(0), companyid);

                tempParams.put("discountinbase", discountinBase);


                boolean success = accSalesReturnService.updateSalesreturnAmount(sr, tempParams);
                if (success) {
                    totalSRUpdated++;
                }

            }
            issuccess = true;
        } catch (Exception ex) {
            System.out.println("------ Error at updateTotalSRAmount() ----------");
            ex.printStackTrace();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("totalSalesReturnUpdated", totalSRUpdated);
            } catch (JSONException ex) {
                System.out.println("------ Error at updateTotalSRAmount() ----------");
                ex.printStackTrace();
            }
        }

        return totalSRUpdated;

    }

    public ModelAndView updateAmountDueAndDiscount(HttpServletRequest request, HttpServletResponse response) {
        StringBuffer msg = new StringBuffer();
        try {
            String companyid = request.getParameter("companyid");
            String currencyid = request.getParameter("currencyid");
            HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
            GlobalParams.put("companyid", companyid);
            GlobalParams.put("gcurrencyid", currencyid);
            if (!StringUtil.isNullOrEmpty(companyid) && !StringUtil.isNullOrEmpty(currencyid)) {
                int count = updateCustomerInvoiceAmountDueAndDiscount(GlobalParams);
                msg.append("<br> Customer Invoice Updated- " + count);
                count = updateVendorInvoiceAmountDueAndDiscount(GlobalParams);
                msg.append("<br> Vendor Invoice Updated- " + count);
            }
        } catch (Exception ex) {
            System.out.println("Exception occured-" + ex.getMessage());
        } finally {
            return new ModelAndView("jsonView_ex", "model", msg.toString());
        }
    }

    private int updateCustomerInvoiceAmountDueAndDiscount(HashMap<String, Object> GlobalParams) {
        int count = 0;
        HashMap<String, Object> requestParams = new HashMap();
        try {
            String companyid = (String) GlobalParams.get("companyid");
            requestParams.put("companyid", (String) GlobalParams.get("companyid"));
            KwlReturnObject invoices = accInvoiceDAOobj.getNormalInvoices(requestParams);
            List<Invoice> invoiceList = invoices.getEntityList();
            for (Invoice invoice : invoiceList) {
                JournalEntry je = invoice.getJournalEntry();
                double externalCurrencyRate = 0;
                Date paymentDate = null;
                if (je != null) {
                    Set<InvoiceDetail> invoicedetails = invoice.getRows();
                    double rowDiscountAmt = 0, rowDiscountAmtInBase = 0;
                    for (InvoiceDetail invoicedetail : invoicedetails) {//Discount 
                        if (invoicedetail.getDiscount() != null) {
                            rowDiscountAmt += invoicedetail.getDiscount().getDiscountValue();
                        }
                    }
                    externalCurrencyRate = je.getExternalCurrencyRate();
//                    paymentDate = je.getEntryDate();
                    paymentDate = invoice.getCreationDate();
                    String invoiceCurrencyId = (invoice.getCurrency() == null ? (String) GlobalParams.get("currencyid") : invoice.getCurrency().getCurrencyID());
                    KwlReturnObject baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, invoice.getInvoiceamountdue(), invoiceCurrencyId, paymentDate, externalCurrencyRate);
                    double invoiceamountdueinbase = (Double) baseAmount.getEntityList().get(0);
                    //Discount in base
                    baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, rowDiscountAmt, invoiceCurrencyId, paymentDate, externalCurrencyRate);
                    rowDiscountAmtInBase = (Double) baseAmount.getEntityList().get(0);
                    rowDiscountAmtInBase = authHandler.round(rowDiscountAmtInBase, companyid);
                    JSONObject invoiceJSON = new JSONObject();
                    invoiceJSON.put("invoiceid", invoice.getID());
                    invoiceJSON.put(Constants.invoiceamountdueinbase, invoiceamountdueinbase);
                    invoiceJSON.put(Constants.discountAmount, rowDiscountAmt);
                    invoiceJSON.put(Constants.discountAmountInBase, rowDiscountAmtInBase);
                    boolean success = accInvoiceDAOobj.updateInvoiceAmountInBase(invoice, invoiceJSON);
                    if (success) {
                        count++;
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("Exception in updateCustomerInvoiceAmountDueAndDiscount: " + ex.getMessage());
        } finally {
            return count;
        }
    }
//    public ModelAndView getTDSRate(HttpServletRequest request, HttpServletResponse response) {
////        double rate =0.0;
//        String rate ="";
//        String id ="";
//        JSONObject jobj = new JSONObject();
//        try {
//            String natureOfPayment = request.getParameter("natureofPayment");
//            String deducteeType = request.getParameter("deducteeType");
//            String residentialstatus=request.getParameter("residentialstatus");
//            String date=request.getParameter("date");//yyyy-MM-dd formate
//            HashMap<String, Object> tdsParams = new HashMap<String, Object>();
//            tdsParams.put("natureofPayment", natureOfPayment);
//            tdsParams.put("deducteeType", deducteeType);
//            tdsParams.put("residentialstatus", residentialstatus);
//            tdsParams.put("date", date);
//            KwlReturnObject kjobj = kwlCommonTablesDAOObj.getTDSRate(tdsParams);
//            List listobj = kjobj.getEntityList();
//            if (listobj.size() > 0) {
//                Object[] row = (Object[]) listobj.get(0);
//                if (row != null) {
//                    rate = (String) row[0].toString();
//                    id = (String) row[1].toString();
//                }
//            }
//            jobj.put("rate",rate);    
//            jobj.put("id",id);    
//        } catch (Exception ex) {
//            System.out.println("Exception occured in getTDSRate -" + ex.getMessage());
//        } finally {
//            return new ModelAndView("jsonView_ex", "model", jobj.toString());
//        }
//    }
    private int updateVendorInvoiceAmountDueAndDiscount(HashMap<String, Object> GlobalParams) {
        int count = 0;
        HashMap<String, Object> requestParams = new HashMap();
        try {
            String companyid = (String) GlobalParams.get("companyid");
            requestParams.put("companyid", (String) GlobalParams.get("companyid"));
            KwlReturnObject invoices = accGoodsReceiptobj.getNormalGoodsReceipts(requestParams);
            List<GoodsReceipt> invoiceList = invoices.getEntityList();
            for (GoodsReceipt goodsreceipt : invoiceList) {
                JournalEntry je = goodsreceipt.getJournalEntry();
                double externalCurrencyRate = 0;
                Date paymentDate = null;
                if (je != null) {
                    Set<GoodsReceiptDetail> invoicedetails =  goodsreceipt.getRows();
                    double rowDiscountAmt = 0, rowDiscountAmtInBase = 0;
                    for (GoodsReceiptDetail goodsReceiptDetail : invoicedetails) {//Discount 
                        if (goodsReceiptDetail.getDiscount() != null) {
                            rowDiscountAmt += goodsReceiptDetail.getDiscount().getDiscountValue();
                        }
                    }
                    externalCurrencyRate = je.getExternalCurrencyRate();
//                    paymentDate = je.getEntryDate();
                    paymentDate = goodsreceipt.getCreationDate();
                    String invoiceCurrencyId = (goodsreceipt.getCurrency() == null ? (String) GlobalParams.get("currencyid") : goodsreceipt.getCurrency().getCurrencyID());
                    KwlReturnObject baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, goodsreceipt.getInvoiceamountdue(), invoiceCurrencyId, paymentDate, externalCurrencyRate);
                    double invoiceamountdueinbase = (Double) baseAmount.getEntityList().get(0);
                    invoiceamountdueinbase = authHandler.round(invoiceamountdueinbase, companyid);
                    JSONObject invoiceJSON = new JSONObject();
                    invoiceJSON.put("invoiceid", goodsreceipt.getID());
                    invoiceJSON.put(Constants.invoiceamountdueinbase, invoiceamountdueinbase);
                    invoiceJSON.put(Constants.discountAmount, rowDiscountAmt);
                    invoiceJSON.put(Constants.discountAmountInBase, rowDiscountAmtInBase);
                    boolean success = accGoodsReceiptobj.updateInvoiceAmountInBase(goodsreceipt, invoiceJSON);
                    if (success) {
                        count++;
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("Exception in updateVendorInvoiceAmountDueAndDiscount: " + ex.getMessage());
        } finally {
            return count;
        }
    }
    
    //get other details common to all modules in DOcument Designer
    public static void getCommonFieldsForAllModulesSummaryData(JSONObject summaryData, HashMap<String, Object> extraparams, AccountingHandlerDAO accountingHandlerDAOobj) throws ServiceException, JSONException {
        String companyid = "";
        int approvestatuslevel = 0;
        try {

            if (extraparams.containsKey(Constants.companyid)) {
                companyid = (String) extraparams.get(Constants.companyid);
            }
            if (extraparams.containsKey("approvestatuslevel")) {
                approvestatuslevel = (Integer) extraparams.get("approvestatuslevel");
            }
            if (!StringUtil.isNullOrEmpty(companyid)) {
                KwlReturnObject companyrefresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                Company companyobj = (Company) companyrefresult.getEntityList().get(0);
                if (companyobj.getCurrency() != null) {
                    summaryData.put(CustomDesignerConstants.Basecurrencyname, companyobj.getCurrency().getName());
                    summaryData.put(CustomDesignerConstants.Basecurrencycode, companyobj.getCurrency().getCurrencyCode());
                    summaryData.put(CustomDesignerConstants.Basecurrencysymbol, companyobj.getCurrency().getSymbol());
                }
            }
            //Approver levels
            if (approvestatuslevel > 1) {
                KwlReturnObject approvalresult = accountingHandlerDAOobj.getApprovalHistory(extraparams);
                if (approvalresult != null && approvalresult.getRecordTotalCount() > 0) {
                    List<Approvalhistory> list1 = approvalresult.getEntityList();
                    int level = 0;//firstObj.getApprovallevel(); //get level of approval set for module
                    int maxApprovedLevel = 0;//firstObj.getApprovallevel(); //Max level upto which document is approved.
                    boolean isLatestEntry = true;
                    // Date formatter for formatting approved date in user date format
                    DateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
                    if (extraparams.containsKey("dateformatter") && extraparams.get("dateformatter") != null) {
                        dateFormatter = (DateFormat) extraparams.get("dateformatter");
                    }
                    for (Approvalhistory historyObj : list1) {
                        if (isLatestEntry) {
                            level = historyObj.getApprovallevel(); 
                            maxApprovedLevel = historyObj.getApprovallevel(); //Max level upto which document is approved
                            summaryData.put(Constants.ApproverLevel + level, StringUtil.getFullName(historyObj.getApprover()));
                            summaryData.put(Constants.APPROVED_DATE_LEVEL + level, dateFormatter.format(historyObj.getApprovedon()));// Approved date
                            isLatestEntry = false;
                        } else {
                            level = historyObj.getApprovallevel();
                            if (maxApprovedLevel > level) {
                                summaryData.put(Constants.ApproverLevel + level, StringUtil.getFullName(historyObj.getApprover()));
                                summaryData.put(Constants.APPROVED_DATE_LEVEL + level, dateFormatter.format(historyObj.getApprovedon()));// Approved date
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
   public static List<PricingBandMasterDetail> getRRPFieldsForAllModulesLineItem(JSONObject requestJobj, AccountingHandlerDAO accountingHandlerDAOobj,accMasterItemsDAO accMasterItemsDAOobj) throws ServiceException, JSONException {
            List<PricingBandMasterDetail> list=new ArrayList();
        try {

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), requestJobj.optString(Constants.globalCurrencyKey,""));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            HashMap<String, Object> requestParams1 = new HashMap<String, Object>();
            requestParams1.put("isSyncToPOS", true);
            requestParams1.put("companyID", requestJobj.optString(Constants.companyKey,""));
            requestParams1.put("currencyID", currency.getCurrencyID());
            KwlReturnObject result = accMasterItemsDAOobj.getPOSProductsPrice(requestParams1);
            list = result.getEntityList();


        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    
    /*
     * -Purpose--- Put addresses in Summary Data: 
     * Parameters--Store Type
     * JsonObject summaryData FromStore Flag--To differentiate between FromStore
     * and ToStore Return type--void
     */
    
    public static void getTotalAddressofStore(Store Storeobj, Boolean fromStoreFlag, JSONObject summaryData) {
        StringBuilder addressString = new StringBuilder();
        try {

            if (Storeobj != null) {
                String addr = StringUtil.isNullOrEmpty(Storeobj.getAddress()) ? "" : Storeobj.getAddress();
                String appendedphoneno = StringUtil.isNullOrEmpty(Storeobj.getContactNo()) ? "" : "\nPhone : " + Storeobj.getContactNo();
                String appendedfax = StringUtil.isNullOrEmpty(Storeobj.getFaxNo()) ? "" : StringUtil.isNullOrEmpty(appendedphoneno) ? "\nFax : " + Storeobj.getFaxNo() : ", Fax : " + Storeobj.getFaxNo();
                String phoneno = StringUtil.isNullOrEmpty(Storeobj.getContactNo()) ? "" : Storeobj.getContactNo();
                addressString.append(addr);
                addressString.append(appendedphoneno); 
                addressString.append(appendedfax);
                if (fromStoreFlag) {
                    summaryData.put(CustomDesignerConstants.FromStoreAddress, addr);
                    summaryData.put(CustomDesignerConstants.FromStoreContactNo, phoneno);
                    summaryData.put(CustomDesignerConstants.FromStoreFaxNo, appendedfax);
                    summaryData.put(CustomDesignerConstants.FromStoreTotalAddress, addressString.toString());
                } else {
                    summaryData.put(CustomDesignerConstants.ToStoreAddress, addr);
                    summaryData.put(CustomDesignerConstants.ToStoreContactNo, phoneno);
                    summaryData.put(CustomDesignerConstants.ToStoreFaxNo, appendedfax);
                    summaryData.put(CustomDesignerConstants.ToStoreTotalAddress, addressString.toString());

                }
            }
        } catch (Exception ex) {
            Logger.getLogger(CommonFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ModelAndView adjustRoundingDifference(HttpServletRequest request, HttpServletResponse response) {
        StringBuffer msg = new StringBuffer();
        try {
            String companyid = request.getParameter("companyid");
            String currencyid = request.getParameter("currencyid");
            HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
            GlobalParams.put("companyid", companyid);
            GlobalParams.put("gcurrencyid", currencyid);
            if (!StringUtil.isNullOrEmpty(companyid) && !StringUtil.isNullOrEmpty(currencyid)) {
                List countList = updateJournalEntriesWithRoundingDifference(GlobalParams);
                msg.append("<br> Journal Entries Updated (Rounding Difference) - " + countList.get(0));
                msg.append("<br> Journal EntrY Detail Updated (Amount In Base)- " + countList.get(1));
            }
        } catch (Exception ex) {
            System.out.println("Exception occured-" + ex.getMessage());
        } finally {
            return new ModelAndView("jsonView_ex", "model", msg.toString());
        }
    }

    public List updateJournalEntriesWithRoundingDifference(HashMap<String, Object> GlobalParams) {
        List listCount=new ArrayList();
        int count = 0,amountinbasecount=0;
        HashMap<String, Object> requestParams = new HashMap();
        KwlReturnObject result = null;
        CompanyAccountPreferences pref = null;
                                
        try {
            String companyid = (String) GlobalParams.get("companyid");
            String gcurrencyid = (String)GlobalParams.get("gcurrencyid");
            requestParams.put("gcurrencyid",gcurrencyid);
            requestParams.put("companyid", (String) GlobalParams.get("companyid"));
            requestParams.put("roundingAdjustment",true);
            result = accJournalEntryobj.getJournalEntry(requestParams);
            List<JournalEntry> jeList = result.getEntityList();
            for (JournalEntry entry : jeList) {
                Set<JournalEntryDetail> details = entry.getDetails();
                if (!details.isEmpty()) {
                    double amount = 0.0;
                    double creditAmountInBase = 0.0;
                    double debitAmountInBase = 0.0;
                    boolean eliminateflag = false;
                    boolean intercompanyflag = false;
                    KwlReturnObject bAmt = null;
                    for (JournalEntryDetail jed : details) {
                        if (!jed.isIsSeparated()) {
                            if (!eliminateflag && jed.getAccount() != null && jed.getAccount().isEliminateflag()) {
                                eliminateflag = true;
                            }
                            if (!intercompanyflag && jed.getAccount() != null && jed.getAccount().isIntercompanyflag()) {
                                intercompanyflag = true;
                            }
                            double exchangeRate = jed.getJournalEntry().getExternalCurrencyRate();
                            if (gcurrencyid.equals(jed.getJournalEntry().getCurrency().getCurrencyID()) && gcurrencyid.equals(jed.getJournalEntry().getExternalCurrencyRate())) {
                                exchangeRate = 1;
                            }
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, jed.getAmount(), jed.getJournalEntry().getCurrency() == null ? gcurrencyid : jed.getJournalEntry().getCurrency().getCurrencyID(), jed.getJournalEntry().getEntryDate(), exchangeRate);
                            jed.setAmountinbase(authHandler.round((Double) bAmt.getEntityList().get(0), companyid)); // updated amountinbase for jedetails
                            if (jed.isDebit()) {
                                amount += jed.getAmount();
                                debitAmountInBase += authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                            } else {
                                amount -= jed.getAmount();
                                creditAmountInBase += authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                            }
                            amountinbasecount++;
                        }
                    }
                    if (Math.abs(amount) >= 0.000001 && (debitAmountInBase==creditAmountInBase)) {
                        System.out.println("Debit and credit amounts are not same for the Journal entry no: " + entry.getEntryNumber());
                    } else {
                        debitAmountInBase = authHandler.round(debitAmountInBase, companyid);
                        creditAmountInBase = authHandler.round(creditAmountInBase, companyid);

                        if (creditAmountInBase != debitAmountInBase) {
                            double diff = creditAmountInBase - debitAmountInBase;
                                if (creditAmountInBase < debitAmountInBase) {
                                    diff = diff * -1;
                                }
                                bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, diff, entry.getCurrency() == null? gcurrencyid : entry.getCurrency().getCurrencyID(), entry.getEntryDate(), entry.getExternalCurrencyRate());
                                double currencyAmount = (Double) bAmt.getEntityList().get(0);
                                JournalEntryDetail roundJeD = new JournalEntryDetail();
                                roundJeD.setAmount(currencyAmount);
                                if (creditAmountInBase < debitAmountInBase) {
                                    roundJeD.setDebit(false);
                                } else {
                                    roundJeD.setDebit(true);
                                }
                                roundJeD.setAmountinbase(authHandler.round(diff,companyid));
                                KwlReturnObject prefresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), entry.getCompany().getCompanyID());
                                pref = (CompanyAccountPreferences) prefresult.getEntityList().get(0);
                                if(StringUtil.isNullOrEmpty(pref.getRoundingDifferenceAccount().getID())){
                                     throw new AccountingException("Rounding Difference Account is not Mapped in Company Preferences");
                                }
                                roundJeD.setAccount(pref.getRoundingDifferenceAccount());
                                roundJeD.setCompany(entry.getCompany());
                                roundJeD.setJournalEntry(entry);
                                roundJeD.setRoundingDifferenceDetail(true);
                                details.add(roundJeD);
                                count++;
                                amountinbasecount++;
                            }
                    }
                    entry.setDetails(details);
                }
            }
        } catch (Exception ex) {
            System.out.println("Error Occured During Journal entry updation " + ex);
        } finally{
            listCount.add(count);
            listCount.add(amountinbasecount);
        }
        return listCount;
    }
    
    public static List getproductmastersFieldsToShowLineLevel(Map<String, Object> ProductFieldsRequestParams, AccountingHandlerDAO accountingHandlerDAOobj) throws ServiceException, JSONException {
        List masterFieldsResultList = new ArrayList();
        try {
            KwlReturnObject masterFieldsResult = accountingHandlerDAOobj.getProductMasterFieldsToShowAtLineLevel(ProductFieldsRequestParams);
            masterFieldsResultList = masterFieldsResult.getEntityList();
        } catch (Exception ex) {
            Logger.getLogger(CommonFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }

        return masterFieldsResultList;
    }

  public static void getterMethodForProductsData( Product product,List masterFieldsResultList,JSONObject obj){
         try {
                Class cl = product.getClass();
                Class returnType = null;
                for (Object customizeObj : masterFieldsResultList) {

                    DefaultHeader dh = (DefaultHeader) customizeObj;
                    String pojomethodName =StringUtil.toUpperCaseFirstLetterOfString(dh.getPojoMethodName());
                    if (dh.getDefaultHeader().equals("Initial Purchase Price")) {//initial purchase price is already present in jsonobject.
                        continue;
                    }
                    Method getter = cl.getMethod("get" + pojomethodName);
                    returnType = getter.getReturnType();
                    Object ob = getter.invoke(product);
                    if (ob != null && (returnType.equals(String.class) || returnType.equals(Integer.class) || returnType.equals(Boolean.class))) {
                        obj.put(dh.getDataIndex(), ob.toString());
                    } else if (ob != null) {
                        Class fkclass = ob.getClass();
                        String refdataCol = dh.getRefDataColumn_HbmName();
                        String refColPojoMethodname = StringUtil.toUpperCaseFirstLetterOfString(refdataCol);
                        Method fkgetter = fkclass.getMethod("get" + refColPojoMethodname);
                        String value = (String) fkgetter.invoke(ob);
                        obj.put(dh.getDataIndex(), value);
                    }
                }

            } catch (Exception ex) {
                 Logger.getLogger(CommonFunctions.class.getName()).log(Level.SEVERE, null, ex);
            }
    
    }
  
    //=======================Rounding JE For Transactions Script============================
    public ModelAndView postRoundingJEForTransactions(HttpServletRequest request, HttpServletResponse response) {
        StringBuilder msg = new StringBuilder();
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            paramJobj.put("DontCheckYearLock", true);
            String companyid = paramJobj.optString("companyid");
            int moduleID = paramJobj.optInt("module");
            boolean checkAmountBeforePost = paramJobj.optBoolean("checkRounding");


            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("RJE_Tx");
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus status = null;

            KwlReturnObject resultOBJ = null;
            if (moduleID == Constants.Acc_Goods_Receipt_ModuleId || moduleID == 0) {
                resultOBJ = accGoodsReceiptobj.getTransactionsForRoundingJE(Constants.Acc_Goods_Receipt_ModuleId, companyid);
                if (resultOBJ != null && resultOBJ.getRecordTotalCount() > 0) {
                    msg.append("</br>===================Purchase Invoice======================");
                    if (checkAmountBeforePost) {
                        msg.append("<table border='1'> <tr><th>Purchase Invoice</th><th>Rounding Difference</th><th>JE Creation Date</th></tr>");
                    } else {
                        msg.append("<table border='1'> <tr><th>Purchase Invoice</th><th>Rounding JE Number</th><th>Rounding Amount</th></tr>");
                    }
                    List<GoodsReceipt> invoiceList = resultOBJ.getEntityList();
                    for (GoodsReceipt gr : invoiceList) {
                        double amountinbase = authHandler.round(gr.isIsOpeningBalenceInvoice() ? gr.getOriginalOpeningBalanceBaseAmount() : gr.getInvoiceAmountInBase(), companyid);
                        String invnumber = gr.getGoodsReceiptNumber();
                        paramJobj.put("goodsReceiptObj", gr);
                        double difference = 0;
                        if (checkAmountBeforePost) {
                            double knockedOffAmount = accGoodsReceiptModuleService.getGrAmountUtilizedInMPandDN(paramJobj);
                            difference = authHandler.round(knockedOffAmount - amountinbase, companyid);
                            if (difference != 0 && Math.abs(difference)<=0.05) {
                                msg.append("<tr><td>").append(invnumber).append("</td><td>").append(difference).append("</td><td>").append(gr.getAmountDueDate()!=null?authHandler.getDateOnlyFormat().format(gr.getAmountDueDate()):"Empty").append("</td></tr>");
                            }
                        } else {
                            status = txnManager.getTransaction(def);
                            try {
                                JournalEntry roundingJE = accGoodsReceiptModuleService.createRoundingOffJE(paramJobj);
                                if (roundingJE != null) {
                                    Set<JournalEntryDetail> jedDetail = roundingJE.getDetails();
                                    for (JournalEntryDetail jed : jedDetail) {
                                        difference = jed.getAmountinbase();
                                        break;
                                    }
                                    msg.append("<tr><td>").append(invnumber).append("</td><td>").append(roundingJE.getEntryNumber()).append("</td><td>").append(difference).append("</td></tr>");
                                }
                                txnManager.commit(status);
                            } catch (Exception ex) {
                                if (status != null) {
                                    txnManager.rollback(status);
                                }
                            }
                        }
                    }
                    msg.append("</table>");
                }
            }

            if (moduleID == Constants.Acc_Invoice_ModuleId || moduleID == 0) {
                resultOBJ = accGoodsReceiptobj.getTransactionsForRoundingJE(Constants.Acc_Invoice_ModuleId, companyid);
                if (resultOBJ != null && resultOBJ.getRecordTotalCount() > 0) {
                    msg.append("</br>===================Sales Invoice======================");
                    if (checkAmountBeforePost) {
                        msg.append("<table border='1'> <tr><th>Sales Invoice</th><th>Rounding Difference</th><th>JE Creation Date</th></tr>");
                    } else {
                        msg.append("<table border='1'> <tr><th>Sales Invoice</th><th>Rounding JE Number</th><th>Rounding Amount</th></tr>");
                    }
                    List<Invoice> invoiceList = resultOBJ.getEntityList();
                    for (Invoice inv : invoiceList) {
                        double amountinbase = authHandler.round(inv.isIsOpeningBalenceInvoice() ? inv.getOriginalOpeningBalanceBaseAmount() : inv.getInvoiceamountinbase(), companyid);
                        String invnumber = inv.getInvoiceNumber();
                        paramJobj.put("salesInvoiceObj", inv);
                        double difference = 0;
                        if (checkAmountBeforePost) {
                            double knockedOffAmount = accInvoiceModuleService.getInvoiceAmountUtilizedInRPandCN(paramJobj);
                            difference = authHandler.round(knockedOffAmount - amountinbase, companyid);
                            if (difference != 0 && Math.abs(difference)<=0.05) {
                                msg.append("<tr><td>").append(invnumber).append("</td><td>").append(difference).append("</td><td>").append(inv.getAmountDueDate()!=null?authHandler.getDateOnlyFormat().format(inv.getAmountDueDate()):"Empty").append("</td></tr>");
                            }
                        } else {
                            status = txnManager.getTransaction(def);
                            try {
                                JournalEntry roundingJE = accInvoiceModuleService.createRoundingOffJE(paramJobj);
                                if (roundingJE != null) {
                                    Set<JournalEntryDetail> jedDetail = roundingJE.getDetails();
                                    for (JournalEntryDetail jed : jedDetail) {
                                        difference = jed.getAmountinbase();
                                        break;
                                    }
                                    msg.append("<tr><td>").append(invnumber).append("</td><td>").append(roundingJE.getEntryNumber()).append("</td><td>").append(difference).append("</td></tr>");
                                }
                                txnManager.commit(status);
                            } catch (Exception ex) {
                                if (status != null) {
                                    txnManager.rollback(status);
                                }
                            }
                        }
                    }
                    msg.append("</table>");
                }
            }

            if (msg.length() == 0) {
                msg.append("</br>Script executed sucessfully.No record to post rounding JE. ");
            }
        } catch (Exception ex) {
            System.out.println("Exception occured-" + ex.getMessage());
        } finally {
            return new ModelAndView("jsonView_ex", "model", msg.toString());
        }
    }

    public ModelAndView updateAccountOpeningBalance(HttpServletRequest request, HttpServletResponse response) {
        StringBuilder msg = new StringBuilder();
        String message="";
        try {
            int companycount =0;
            int failedcompanycount =0;
            int updatedAccountCount =0;
            JSONObject jsonParamObj = new JSONObject();
            String endDate = request.getParameter("enddate");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date enddate = sdf.parse(endDate);
            String dbnameToBeUpdated = request.getParameter("dbname");
            String updateQuery = "update " + dbnameToBeUpdated + ".account as acc set acc.openingbalance=";
            String subdomain = request.getParameter("subdomain");
            Map<String, Object> companyMap = new HashMap();
            if (!StringUtil.isNullOrEmpty(subdomain)) {
                String[] subDomainArray = {subdomain};
                companyMap.put("subdomains", subDomainArray);
            }
  
            KwlReturnObject companyResult = accCompanyPreferencesObj.getCompanyList(companyMap);
            List<String> companyIDList = companyResult.getEntityList();
            for (String companyid : companyIDList) {
                try {
                    companycount++;
                    msg.append("</br>Company ID : ").append(companyid);
                    msg.append("</br>Start Time : ").append(authHandler.getDateWithTimeFormat().format(new Date()));
                    jsonParamObj.put(Constants.companyKey, companyid);
                    HashMap<String, Object> filterParams = new HashMap();
                    filterParams.put(Constants.companyKey, companyid);
                    KwlReturnObject accountresult = accAccountDAOobj.getAccountEntry(filterParams);
                    if (accountresult != null && !accountresult.getEntityList().isEmpty()) {
                        List<Account> accountList = accountresult.getEntityList();
                        for (Account account : accountList) {
                            jsonParamObj.put(Constants.globalCurrencyKey, account.getCompany().getCurrency().getCurrencyID());
                            double accountBalance = accReportsService.getAccountBalance(jsonParamObj, account.getID(), new Date(1970), enddate,null);
                            accAccountDAOobj.updateAccountOpeningBalance(dbnameToBeUpdated, account.getID(), accountBalance);
                            updatedAccountCount++;
                        }
                    }
                    msg.append("</br>End Time : ").append(authHandler.getDateWithTimeFormat().format(new Date()));
                } catch (Exception ex) {
                    companycount--;
                    failedcompanycount++;
                    msg.append("</br>Exception Occured For Company: ").append(companyid);
                }
            }
            msg.append("</br>Thanks !!!Script Executed Successfully.");
            msg.append("</br>Sucess Company Count : ").append(companycount);
            msg.append("</br>Failure Company Count: ").append(failedcompanycount);
            msg.append("</br>Total Updated Account : ").append(updatedAccountCount);
        } catch (Exception ex) {
            System.out.println("Exception occured-" + ex.getMessage());
        } finally {
            return new ModelAndView("jsonView_ex", "model", msg.toString());
        }
    }
    
    public ModelAndView getReports(HttpServletRequest request, HttpServletResponse response) {
        
        JSONArray jArr=new JSONArray();
        JSONObject jobj = new JSONObject();
        try {

            HashMap<String, Object> requestParams = new HashMap();
            String moduleid = request.getParameter("moduleid");
            String companyId = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject companyObj = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
            Company company = (Company) companyObj.getEntityList().get(0);
            int countryid = !StringUtil.isNullOrEmpty(company.getCountry().getID())?Integer.parseInt(company.getCountry().getID()):0; 

            requestParams.put("moduleid", moduleid);
            requestParams.put("userid", sessionHandlerImpl.getUserid(request));
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("countryid", countryid);
            KwlReturnObject kwlReturnObj = accInvoiceDAOobj.getReports(requestParams);
            List list = kwlReturnObj.getEntityList();
            for (Object obj : list) {

                Object[] oj = (Object[]) obj;
                String reportid = oj[0].toString();
                KwlReturnObject reportM = accountingHandlerDAOobj.getObject(ReportMaster.class.getName(), reportid);
                List<ReportMaster> prd = reportM.getEntityList();
                
                String id = prd.get(0).getID();
                String name = prd.get(0).getName();
                String desc = prd.get(0).getDescription();
                String methodName = prd.get(0).getMethodName();
                String groupedunder = prd.get(0).getGroupedUnder();
                
                JSONObject tempJobj = new JSONObject();

                tempJobj.put("id", id);
                tempJobj.put("name", name);
                tempJobj.put("description", desc);
                tempJobj.put("methodName", methodName);
                tempJobj.put("groupedunder", groupedunder);
            

                jArr.put(tempJobj);
                
            }
            jobj.put("data",jArr);

        } catch (Exception ex) {
            Logger.getLogger(CommonFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView updateGSTActivationDateForMalaysianCompany(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jSONObject = new JSONObject();
        try {
            List<ExtraCompanyPreferences> extraCompanyPreferencesList = accCompanyPreferencesObj.getExtraCompanyPreferencesForMalaysia();
            Date today = new Date();
            if (extraCompanyPreferencesList != null && !extraCompanyPreferencesList.isEmpty()) {
                for (ExtraCompanyPreferences extraCompanyPreferences : extraCompanyPreferencesList) {
                    if (extraCompanyPreferences.getGstEffectiveDate() == null) {
                        extraCompanyPreferences.setEnableGST(false); //set enable GST as FALSE
                    } else if (extraCompanyPreferences.getGstEffectiveDate() != null && today.compareTo(extraCompanyPreferences.getGstEffectiveDate()) >= 0) {
                        extraCompanyPreferences.setEnableGST(true); // if today >= Effective date
                    }
                    if (extraCompanyPreferences.getGstDeactivationDate() != null && DateUtils.isSameDay(today, extraCompanyPreferences.getGstDeactivationDate())) {
                        extraCompanyPreferences.setGstEffectiveDate(null); // set Effective Date as FALSE once the deactivation date is reached
                        extraCompanyPreferences.setEnableGST(false); //set enable GST as FALSE
                    }
                }
            }
            jSONObject.put("success", true);
            jSONObject.put("msg", "Cron executed successfully.");
        } catch (Exception e) {
            Logger.getLogger(CommonFunctions.class.getName()).log(Level.SEVERE, null, e);
            try {
                jSONObject.put("msg", e.getMessage());
            } catch (JSONException ex) {
                Logger.getLogger(CommonFunctions.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", "model", jSONObject.toString());
    }
    
    public ModelAndView assemblyProductsJEUpdate(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<>();
            String companyid = request.getParameter("companyid");
            requestParams.put("companyid", request.getParameter("companyid"));
            requestParams.put("exportfalg", true);// Get All Assembly SKIPING PAGING
            KwlReturnObject extraCompanyPreferencesObj = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraCompanyPreferencesObj.getEntityList().get(0);
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            KwlReturnObject result = accProductObj.getAssemblyProducts(requestParams);
            List<ProductBuild> list = result.getEntityList();
            for (ProductBuild prodBuild : list) {

                String Productid = prodBuild.getID();
                double buildQty = prodBuild.getQuantity();
                double buildCost = 0.0;
                double totalWastageCost = 0.0;
                JournalEntry je = prodBuild.getJournalentry();
                if (je != null) {
                    /**
                     * DELETE OLD JE DETAILS*
                     */
                    accJournalEntryobj.deleteJEDtails(je.getID(), companyid);
                    /**
                     * CREDIT ENTRY FOR THE JOURNAL ENTRY
                     *
                     */
                    JSONObject jedjson;
                    KwlReturnObject resultDetails = accProductObj.getAssemblyBuidDetails(Productid);
                    List<ProductBuildDetails> resultDetailsList = resultDetails.getEntityList();
                    int i = 2;
                    for (ProductBuildDetails pbdetails : resultDetailsList) {
                        double detailRate = pbdetails.getRate();
                        detailRate = detailRate * pbdetails.getInventoryQuantity() * buildQty;
                        buildCost += authHandler.round(detailRate, companyid);
                        jedjson = new JSONObject();
                        jedjson.put("srno", i++);
                        jedjson.put("companyid", companyid);
                        jedjson.put("amount", authHandler.round(detailRate, companyid));
                        jedjson.put("accountid", pbdetails.getAproduct().getPurchaseAccount().getID());
                        jedjson.put("debit", false);
                        jedjson.put("jeid", je.getID());
                        KwlReturnObject BOMjedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        JournalEntryDetail BOMjed = (JournalEntryDetail) BOMjedresult.getEntityList().get(0);
                        pbdetails.setJedetail(BOMjed);
                        /**
                         * Adding Wastage JE detail for BOM.
                         */
                        if (extraCompanyPreferences != null && extraCompanyPreferences.isActivateWastageCalculation() && pbdetails.getWastageQuantity() != 0) {
                            double wastageamount = 0;
                            if (pbdetails.getAproduct().isWastageApplicable() && pbdetails.getAproduct().getWastageAccount() != null) {
                                jedjson.put("accountid", pbdetails.getAproduct().getWastageAccount().getID());
                            } else {
                                if (extraCompanyPreferences != null && prodBuild.getProduct().getInventoryAccount() != null && (extraCompanyPreferences.isActivateMRPModule() || preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD)) {
                                    jedjson.put("accountid", pbdetails.getAproduct().getInventoryAccount().getID());
                                } else {
                                    jedjson.put("accountid", pbdetails.getAproduct().getPurchaseAccount().getID());
                                }
                            }
                            if (pbdetails.getWastageQuantityType() == 1) { // For Percentage
                                wastageamount = authHandler.round(pbdetails.getRate() * ((pbdetails.getWastageQuantity() * pbdetails.getWastageInventoryQuantity()) / 100) * prodBuild.getQuantity(), companyid);
                            } else {
                                wastageamount = authHandler.round(pbdetails.getRate() * pbdetails.getWastageQuantity() * prodBuild.getQuantity(), companyid);
                            }
                            jedjson.put("amount", wastageamount);
                            totalWastageCost += wastageamount;
                            KwlReturnObject wastagejedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            JournalEntryDetail BOMWastagejed = (JournalEntryDetail) wastagejedresult.getEntityList().get(0);
                            pbdetails.setWastagejedetail(BOMWastagejed);
                        }

                    }
                    /**
                     * DEBIT ENTRY FOR PRODUCT ASSEMBLY
                     *
                     */
                    jedjson = new JSONObject();
                    jedjson.put("srno", 1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", buildCost);
                    jedjson.put("accountid", prodBuild.getProduct().getPurchaseAccount().getID());
                    jedjson.put("debit", true);
                    jedjson.put("jeid", je.getID());
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    JournalEntryDetail Totaljed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    prodBuild.setTotaljed(Totaljed);
                    prodBuild.setProductcost(buildCost);
                    /**
                     * Adding Wastage JE detail for Assembly product.
                     */
                    if (extraCompanyPreferences != null && extraCompanyPreferences.isActivateWastageCalculation() && totalWastageCost > 0) {
                        if (prodBuild.getProduct().isWastageApplicable() && prodBuild.getProduct().getWastageAccount() != null) {
                            jedjson.put("accountid", prodBuild.getProduct().getWastageAccount().getID());
                        } else {
                            if (extraCompanyPreferences != null && prodBuild.getProduct().getInventoryAccount() != null && (extraCompanyPreferences.isActivateMRPModule() || preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD)) {
                                jedjson.put("accountid", prodBuild.getProduct().getInventoryAccount().getID());
                            } else {
                                jedjson.put("accountid", prodBuild.getProduct().getPurchaseAccount().getID());
                            }
                        }
                        jedjson.put("amount", totalWastageCost);
                        KwlReturnObject wastagejedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        JournalEntryDetail wastageTotaljed = (JournalEntryDetail) wastagejedresult.getEntityList().get(0);
                        prodBuild.setWastagetotaljed(wastageTotaljed);
                    }
                }

            }
            Map<String, Object> reqMap = new HashMap<>();
            reqMap.put(Constants.companyKey, companyid);
            reqMap.put("updateTransactionAmount", true);
            inventoryValuationProcess.add(reqMap);
            Thread t = new Thread(inventoryValuationProcess);
            t.start();
            msg = "Build Assembly - Records processed successfully. Total Records - " + list.size();
        } catch (Exception ex) {
            System.out.println("------ Error at assemblyProductsJEUpdate() ----------");
            ex.printStackTrace();
        } finally {
            System.out.println(msg);
        }
        return new ModelAndView("jsonView_ex", "model", msg);
    }
     
     public ModelAndView linkInvoiceToDOForPOS(HttpServletRequest request, HttpServletResponse response) {
        JSONObject objectToReturn = new JSONObject();
        String msg = "";
        boolean isSuccess = false;

        JSONArray DataJArr = new JSONArray();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MI_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            authHandlerControllerObj.verifyLogin(request, response);
            String invoiceId = request.getParameter("invoiceid");
            String invoiceNo = request.getParameter("invoiceno");
            DeliveryOrder dod = accInvoiceModuleService.saveDeliveryOrder(request, invoiceId);
            isSuccess = true;
            msg = "</br>Delivery Order " + dod.getDeliveryOrderNumber() + "is generated successfully against invoice " + invoiceNo + "</br>";

            txnManager.commit(status);


        } catch (Exception e) {
            txnManager.rollback(status);
            e.printStackTrace();
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                objectToReturn.put("success", isSuccess);
                objectToReturn.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", "model", objectToReturn.toString());
    }
    public ModelAndView updateCostOfGoodsSoldGroup(HttpServletRequest request, HttpServletResponse response) {
        String msg = "False";
        try {
            String subdomain = "";
            String[] subdomainArray = null;
            if (!StringUtil.isNullOrEmpty(request.getParameter("subdomain"))) {
                subdomain = request.getParameter("subdomain").toString().trim();
                subdomainArray = subdomain.split(",");
            }
            KwlReturnObject rCompanyId = null;
            rCompanyId = accPurchaseOrderobj.getAllCompanyFromDb(subdomainArray);
            Iterator itrCompanyId = rCompanyId.getEntityList().iterator();
            String accountname = "Cost of Goods Sold";
            while (itrCompanyId.hasNext()) {
                String companyid = (String) itrCompanyId.next();
                Group group = accAccountDAOobj.getAccountGroup(companyid, accountname);
                if (group != null) {
                    group.setCostOfGoodsSoldGroup(true);
                    updateChildGroups(group);
                }

            }
            msg = "True";
        } catch (Exception ex) {
            System.out.println("------ Error at updateCostOfGoodsSoldGroup() ----------");
            ex.printStackTrace();
        }
        return new ModelAndView("jsonView_ex", "model", msg);
    }

    private void updateChildGroups(Group group) throws ServiceException {
        Set<Group> children = group.getChildren();
        if (children == null && children.isEmpty()) {
            return;
        }
        for (Group child : children) {
            if (child != null) {
                child.setCostOfGoodsSoldGroup(true);
                updateChildGroups(child);
            }
        }
    }
    public void setTimedTask() {
        /*
         * Note: 'this' implements an interface called UpdateIndicatorsReceiver
         */
        Constants.connectionTimerTask = new TimerTask() {

            @Override
            public void run() {
                Logger.getLogger("=============== TimerTask Run - setTimedTask() =================");
//                System.out.printf("=============== Process Running =================");
                Set<Map.Entry<String, String>> entrySet = Constants.connectionThreads.entrySet();
                for (Map.Entry<String, String> entry : entrySet) {
                    Logger.getLogger(entry.getValue());
                    Constants.connectionThreads.remove(entry.getKey());
//                    System.out.printf(entry.getValue());

            }

            }
        };
        /*
         * Code to actually set the timer....
         */
    }

    public ModelAndView updateStockAdjustmentOutPrice(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MI_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String companyid = request.getParameter("companyid");
            authHandlerControllerObj.verifyLogin(request, response);
            Map<String, Object> reqMap = new HashMap<>();
            reqMap.put(Constants.companyKey, companyid);
            reqMap.put("updateTransactionAmount", true);
            inventoryValuationProcess.add(reqMap);
            Thread t = new Thread(inventoryValuationProcess);
            t.start();
            txnManager.commit(status);

            msg = "Successfully updated Stock Adjustment Out Prices";
        } catch (Exception e) {
            txnManager.rollback(status);
            e.printStackTrace();
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            System.out.println(msg);
        }
        return new ModelAndView("jsonView_ex", "model", msg);
    }
    
    public ModelAndView sendTestMail(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = sendTestMail(paramJobj);
        } catch (JSONException | SessionExpiredException ex) {
            Logger.getLogger(CommonFunctions.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(CommonFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONObject sendTestMail(JSONObject paramJobj) throws JSONException {
        JSONObject returnJobj = new JSONObject();
        boolean issuccess = false;
        try {
            String plainMsg = paramJobj.optString("message", "");
            String subject = paramJobj.optString("subject", "");
            String[] path = new String[]{};
            String[] emails = {};
            if (paramJobj.has("emailid")) {
                emails = paramJobj.getString("emailid").split(",");
            }
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(), paramJobj.getString(Constants.companyKey));
            Company company = preferences.getCompany();
            String fromID = (!company.isEmailFromCompanyCreator()) ? Constants.ADMIN_EMAILID : authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());

            // for sending mail
            try {
                if (emails.length > 0) {
                    Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                    SendMailHandler.postMail(emails, subject, plainMsg, plainMsg, fromID, path, smtpConfigMap);
                    issuccess = true;
                }
            } catch (MessagingException e) {
                try {
                    throw new MessageSizeExceedingException(e.getMessage());
                } catch (MessageSizeExceedingException exception) {
                    if (StringUtil.isNullOrEmpty(exception.toString())) {
                        Logger.getLogger(CommonFunctions.class.getName()).log(Level.SEVERE, null, e);
                    } else {
                        issuccess = false;
                        returnJobj.put("success", issuccess);
                        returnJobj.put("isMsgSizeException", true);
                        returnJobj.put("msg", exception.toString());
                    }
                }
            }
            
            // for inserting audit trial entry
            Map<String, Object> auditRequestParams = new HashMap<>();
            auditRequestParams.put(Constants.reqHeader, paramJobj.getString(Constants.reqHeader));
            auditRequestParams.put(Constants.remoteIPAddress, paramJobj.getString(Constants.remoteIPAddress));
            auditRequestParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));
            auditTrailObj.insertAuditLog(AuditAction.SENT_EMAIL, "User " + paramJobj.getString(Constants.userfullname) + " has sent Test Email.", auditRequestParams, "");
        } catch (JSONException | ServiceException ex) {
            issuccess = false;
        } catch(Exception ex) {
            issuccess = false;
        } finally {
            if (returnJobj.length() == 0) {
                returnJobj.put("success", issuccess);
                returnJobj.put("msg", messageSource.getMessage("acc.rem.165", null, Locale.forLanguageTag(paramJobj.getString("language"))));
            }
        }
        
        return returnJobj;
    }

    public ModelAndView updateExternalCurrencyRate(HttpServletRequest request, HttpServletResponse response) {
        StringBuffer msg = new StringBuffer();
        try {
            String companyid = request.getParameter("companyid");
            String currencyid = request.getParameter("currencyid");
            HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
            GlobalParams.put("companyid", companyid);
            GlobalParams.put("gcurrencyid", currencyid);
            if (!StringUtil.isNullOrEmpty(companyid) && !StringUtil.isNullOrEmpty(currencyid)) {
                updateExternalCurrencyRateInJEAndPayment(request, GlobalParams);
            }
        } catch (Exception ex) {
            System.out.println("Exception occured-" + ex.getMessage());
        } finally {
            return new ModelAndView("jsonView_ex", "model", msg.toString());
        }
    }

    public List updateExternalCurrencyRateInJEAndPayment(HttpServletRequest request, HashMap<String, Object> GlobalParams) {
        List listCount = new ArrayList();
        int count = 0, amountinbasecount = 0;
        HashMap<String, Object> requestParams = new HashMap();
        KwlReturnObject result = null;
        try {
            String gcurrencyid = (String) GlobalParams.get("gcurrencyid");
            String companyid = (String) GlobalParams.get("companyid");
            requestParams.put("fromcurrencyid", gcurrencyid);
            requestParams.put("companyid", companyid);
            requestParams.put("updateexternalcurrencyrate", true);
            requestParams.put("isCurrencyExchangeWindow", false);
            result = accJournalEntryobj.getJournalEntry(requestParams);
            List<JournalEntry> jeList = result.getEntityList();
            for (JournalEntry entry : jeList) {
                String jeid = entry.getID();
                Date jeentrydate = entry.getEntryDate();
                double jeexternalrate = entry.getExternalCurrencyRate();
                String tocurrencyid = entry.getCurrency() != null ? entry.getCurrency().getCurrencyID() : gcurrencyid;
                if (jeexternalrate == 0) {
                    requestParams.put("tocurrencyid", tocurrencyid);
                    request.setAttribute("updaterate", true);
                    KwlReturnObject crresult = accCurrencyDAOobj.getCurrencyExchange(requestParams);
                    ExchangeRate er = (ExchangeRate) crresult.getEntityList().get(0);
                    boolean isCurrencyExchangeWindow = StringUtil.isNullOrEmpty(request.getParameter("iscurrencyexchangewindow")) ? false : Boolean.parseBoolean(request.getParameter("iscurrencyexchangewindow"));
                    KwlReturnObject erdresult = accCurrencyDAOobj.getExcDetailID(requestParams, tocurrencyid, jeentrydate, er.getID());
                    if (erdresult.getEntityList() != null && !erdresult.getEntityList().isEmpty() && erdresult.getEntityList().size() > 1) {
                        ExchangeRateDetails erd = (ExchangeRateDetails) erdresult.getEntityList().get(0);
                        if (erd != null) {
                            KwlReturnObject pyresult = accVendorPaymentobj.getPaymentMadeFromJE(jeid, companyid);
                            List pylist = pyresult.getEntityList();
                            if (pylist != null && !pylist.isEmpty()) {
                                double exchangerate = erd.getExchangeRate();
                                //Set exchangerate for JE's External Currency Rate
                                entry.setExternalCurrencyRate(exchangerate);
                                //Set exchangerate for Payment's External Currency Rate
                                Payment payment = (Payment) pylist.get(0);
                                payment.setExternalCurrencyRate(exchangerate);
                            }
                        }//erd
                    } else {
                        System.out.println("Exchange Rate List Count is more than one. Cannot Update");
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("Error Occured During Journal entry updation " + ex);
        } finally {
            listCount.add(count);
            listCount.add(amountinbasecount);
        }
        return listCount;
    }   
    /*Fetching the product termdetails*/
    public static JSONArray fetchProductTermMapDetails(ArrayList<ProductTermsMap> productTermDetail) {
        
        JSONArray productTermJsonArry = new JSONArray();
        try{
        for (ProductTermsMap productTermsMapObj : productTermDetail) {
            JSONObject productTermJsonObj = new JSONObject();
            productTermJsonObj.put("productid", productTermsMapObj.getProduct().getID());
            productTermJsonObj.put("termid", productTermsMapObj.getTerm().getId());
            productTermJsonObj.put("term", productTermsMapObj.getTerm().getTerm());
            productTermJsonObj.put("termtype", productTermsMapObj.getTerm().getTermType());
            productTermJsonObj.put("termsequence", productTermsMapObj.getTerm().getTermSequence());
            productTermJsonObj.put("formula", productTermsMapObj.getTerm().getFormula());
            productTermJsonObj.put("formulaids", productTermsMapObj.getTerm().getFormula());
            productTermJsonObj.put("termpercentage", productTermsMapObj.getPercentage());
            productTermJsonObj.put("originalTermPercentage", productTermsMapObj.getTerm().getPercentage()); // For Service tax term abatment calculation
            productTermJsonObj.put("termamount", "0.0");
            productTermJsonObj.put("glaccountname", productTermsMapObj.getAccount().getAccountName());
            productTermJsonObj.put("accountid", productTermsMapObj.getAccount().getID());
            productTermJsonObj.put("glaccount", productTermsMapObj.getAccount().getID());
            productTermJsonObj.put("IsOtherTermTaxable", productTermsMapObj.getTerm().isOtherTermTaxable());
            productTermJsonObj.put("sign", productTermsMapObj.getTerm().getSign());
            productTermJsonObj.put("taxtype", productTermsMapObj.getTaxType());
            productTermJsonObj.put("purchasevalueorsalevalue", productTermsMapObj.getPurchaseValueOrSaleValue());
            productTermJsonObj.put("isDefault", productTermsMapObj.isIsDefault());
            productTermJsonObj.put("deductionorabatementpercent", productTermsMapObj.getDeductionOrAbatementPercent());
            productTermJsonObj.put("taxvalue", productTermsMapObj.getPercentage());
            productTermJsonObj.put("formType", productTermsMapObj.getFormType());
            productTermJsonObj.put("isIsAdditionalTax", productTermsMapObj.getTerm().isIsAdditionalTax());
            productTermJsonObj.put("includeInTDSCalculation", productTermsMapObj.getTerm().isIncludeInTDSCalculation());
            productTermJsonObj.put("creditnotavailedaccount", productTermsMapObj.getTerm().getCreditNotAvailedAccount() != null ? productTermsMapObj.getTerm().getCreditNotAvailedAccount().getID() : "");
            productTermJsonObj.put("payableaccountid", productTermsMapObj.getTerm().getPayableAccount() != null ? productTermsMapObj.getTerm().getPayableAccount().getID() : "");
            productTermJsonArry.put(productTermJsonObj);
        }
        }catch(Exception ex){
             Logger.getLogger(CommonFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
        return productTermJsonArry;
    }
    
    // Function for adding new tax. It will be called from browse like JSP url. 
    @Deprecated
    public ModelAndView addNewTax(HttpServletRequest request, HttpServletResponse response) {
        JSONObject objectToReturn = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {

            String taxCode = request.getParameter("taxcode") != null ? request.getParameter("taxcode") : "";
            String taxName = request.getParameter("taxname") != null ? request.getParameter("taxname") : "";
            String taxDescription = request.getParameter("taxDescription") != null ? request.getParameter("taxDescription") : "";
            String country = request.getParameter("country") != null ? request.getParameter("country") : "";
            String taxTypeid = request.getParameter("taxtypeid") != null ? request.getParameter("taxtypeid") : "";
            String taxPercent = request.getParameter("taxPercent") != null ? request.getParameter("taxPercent") : "";
            String subdomains = "";
            String[] subdomainArray = null;

            if (StringUtil.isNullOrEmpty(taxCode) || StringUtil.isNullOrEmpty(taxName) || StringUtil.isNullOrEmpty(country) || Integer.parseInt(taxTypeid) < 0 || Integer.parseInt(taxTypeid) > 3 || Integer.parseInt(taxPercent) < 0 || Integer.parseInt(taxPercent) > 100) {
                issuccess = false;
                msg = "Please provide the valid values for the parameters taxcode,taxname,taxtypeid,taxpercent,country etc";
            } else {
                Map<String, Object> map = new HashMap<>();
                map.put("taxcode", taxCode);
                map.put("taxname", taxName);
                map.put("taxdescription", taxDescription);
                map.put("taxtypeid", Integer.parseInt(taxTypeid));
                map.put("taxpercent", Double.parseDouble(taxPercent));
                map.put("country", country);
                if (!StringUtil.isNullOrEmpty(request.getParameter("subdomain"))) {
                    subdomains = request.getParameter("subdomain").toString().trim();
                    subdomainArray = subdomains.split(",");
                    map.put("subdomainArray", subdomainArray);
                }
                objectToReturn = addNewTax(map);
                issuccess = true;
                msg = "Tax added successfully";
            }

        } catch (Exception e) {
            issuccess = false;
            msg = e.getMessage();
            e.printStackTrace();
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                objectToReturn.put("msg", msg);
                objectToReturn.put("success", issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(CommonFunctions.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", "model", objectToReturn.toString());
    }
    
    /*
     * Function written to add multiple taxes from table instead of executing addNewtax() script multiple times.
     */
    public ModelAndView addNewTaxes(HttpServletRequest request, HttpServletResponse response) {
        JSONObject objectToReturn = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            String taxname = request.getParameter("taxname") != null ? request.getParameter("taxname") : "";
            String country = request.getParameter("country") != null ? request.getParameter("country") : "";
            if (StringUtil.isNullOrEmpty(taxname) || StringUtil.isNullOrEmpty(country)) {
                issuccess = false;
                msg = "Please provide the valid values for the parameters taxcode,country.";
            }
            HashMap<String, Object> dataMap = new HashMap<>();
            dataMap.put("countryid", country);
            dataMap.put("taxname", taxname);
            KwlReturnObject result = accTaxObj.getDefaultGSTList(dataMap);
            List<Object[]> list = result.getEntityList();
            for (Object[] row : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("taxname", row[0]);
                map.put("taxdescription", row[1]);
                map.put("taxcode", row[2]);
                map.put("taxpercent", row[3]);
                map.put("taxtypeid", row[5]);
                map.put("country", country);
                objectToReturn = addNewTax(map);
                msg += "============================================[" + row[0] + " Tax added successfully]=====================\n";
                msg += "============================================[Companies Already Having Tax ]=============================\n" + objectToReturn.optString("companiesAlreadyHavingTax")+"\n";
                msg += "============================================[Companies Processed]=======================================\n" + objectToReturn.optString("companiesProcessed")+"\n";
            }
            msg += "\n==============================================[Script Execution Ended]=======================================\n";
        } catch (Exception e) {
            issuccess = false;
            msg = e.getMessage();
            e.printStackTrace();
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, e);
        }
        return new ModelAndView("jsonView_ex", "model", msg);
    }

    public JSONObject addNewTax(Map<String, Object> requestParams) throws AccountingException {
        JSONObject returnObject = new JSONObject();
        String companiesProcessed="";
        String companiesAlreadyHavingTaxes="";
        try {
            String[] subdomainArray = requestParams.get("subdomainArray") != null ? (String[]) requestParams.get("subdomainArray") : null;
            KwlReturnObject rCompanyId = null;
            Map<String, Object> companyMap = new HashMap();
            companyMap.put("subdomains", subdomainArray);
            String country = (String) requestParams.get("country");
            String accountIdForInputTax = "";
            String accountIdForOutputTax = "";
            int countryid = Integer.parseInt(country);

            companyMap.put("country", country);
            rCompanyId = accCompanyPreferencesObj.getCompanyList(companyMap);
            Iterator itrCompanyId = rCompanyId.getEntityList().iterator();
            Account account = null;
            Company company = null;
            String companyid = "";
            Tax tax = null;
            Tax taxObj = null;
            TaxList taxlist = null;

            KwlReturnObject companyResult = null;
            KwlReturnObject companyPrefResult = null;
            KwlReturnObject accresult = null;
            KwlReturnObject taxresult = null;
            KwlReturnObject taxlistresult = null;
            KwlReturnObject taxResult = null;
            List<Tax> listOfCompanyTaxes= null;

            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date financialYearStartDate = new Date();

            JSONObject accjson = new JSONObject();
            accjson.put("name", "Tax");
            accjson.put("balance", 0.0);
            accjson.put("budget", 0.0);
            accjson.put("minbudget", 0.0);
            accjson.put("eliminateflag", false);
            accjson.put("groupid", "3");
            accjson.put("life", 10);
            accjson.put("salvage", 0);

            String taxId = UUID.randomUUID().toString();
            String taxCode = (String) requestParams.get("taxcode");
            String taxName = (String) requestParams.get("taxname");
            String taxDescription = (String) requestParams.get("taxdescription");
            int taxtypeid = (int) requestParams.get("taxtypeid");
            double taxPercent = (double) requestParams.get("taxpercent");

            HashMap<String, Object> taxMap = new HashMap<>();

            taxMap.put("taxcode", StringUtil.DecodeText(taxCode));
            taxMap.put("taxname", StringUtil.DecodeText(taxName));
            taxMap.put("taxCodeWithoutPercentage", StringUtil.DecodeText(taxCode));
            taxMap.put("taxdescription", taxDescription);
            taxMap.put("taxtypeid", taxtypeid);

            HashMap<String, Object> taxListMap = new HashMap<>();
            taxListMap.put("percent", taxPercent);

            while (itrCompanyId.hasNext()) {
                String taxAccountId = "";
                companyid = (String) itrCompanyId.next();
                companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                company = (Company) companyResult.getEntityList().get(0);
                if (!company.getActivated()) {
                    continue;
                }
                companyPrefResult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                CompanyAccountPreferences preferences = (CompanyAccountPreferences) companyPrefResult.getEntityList().get(0);

                financialYearStartDate = preferences.getFirstFinancialYearFrom();
                if (financialYearStartDate == null) {
                    continue;
                }
                /*
                 * Check whether any of the company is already having the tax with similar name.
                 */ 
                taxResult = accTaxObj.getAllTaxOfCompany(companyid);
                listOfCompanyTaxes = taxResult.getEntityList();
                boolean IsTaxDuplicate =false;
                for(int i=0;i<listOfCompanyTaxes.size();i++){
                    taxObj = listOfCompanyTaxes.get(i);
                    if(taxObj.getName().equals(taxName)){
                        IsTaxDuplicate = true;
                        break;
                    }
                }
                if(IsTaxDuplicate){
                    companiesAlreadyHavingTaxes += company.getSubDomain()+",";
                    continue;
                }
                
                String userdiff = company.getCreator().getTimeZone() != null ? company.getCreator().getTimeZone().getDifference() : company.getTimeZone().getDifference();
                sdf1.setTimeZone(TimeZone.getTimeZone("GMT" + userdiff));
                financialYearStartDate = authHandler.getDateWithTimeFormat().parse(sdf1.format(financialYearStartDate));

                if (countryid == Constants.malaysian_country_id) {
                    KwlReturnObject accountReturnObject = accAccountDAOobj.getAccountFromName(companyid, Constants.MALAYSIAN_GST_INPUT_TAX);
                    List accountResultList = accountReturnObject.getEntityList();
                    if (!accountResultList.isEmpty()) {
                        accountIdForInputTax = ((Account) accountResultList.get(0)).getID();
                    }
                    
                    accountReturnObject = accAccountDAOobj.getAccountFromName(companyid, "GST(Input tax)");
                    accountResultList = accountReturnObject.getEntityList();
                    if (!accountResultList.isEmpty()) {
                        accountIdForInputTax = ((Account) accountResultList.get(0)).getID();
                    }
                    
                    accountReturnObject = accAccountDAOobj.getAccountFromName(companyid, Constants.MALAYSIAN_GST_OUTPUT_TAX);
                    accountResultList = accountReturnObject.getEntityList();
                    if (!accountResultList.isEmpty()) {
                        accountIdForOutputTax = ((Account) accountResultList.get(0)).getID();
                    }
                    
                    accountReturnObject = accAccountDAOobj.getAccountFromName(companyid, "GST(Output tax)");
                    accountResultList = accountReturnObject.getEntityList();
                    if (!accountResultList.isEmpty()) {
                        accountIdForOutputTax = ((Account) accountResultList.get(0)).getID();
                    }
                }
                if (countryid == Constants.malaysian_country_id && !StringUtil.isNullOrEmpty(accountIdForInputTax) && !StringUtil.isNullOrEmpty(accountIdForOutputTax)) {
                    if (taxtypeid == 1) {
                        taxAccountId = accountIdForInputTax;
                    } else if (taxtypeid == 2) {
                        taxAccountId = accountIdForOutputTax;
                    }
                } else {
                    accjson.put("currencyid", company.getCurrency().getCurrencyID());
                    accjson.put("companyid", company.getCompanyID());
                    accjson.put("creationdate", financialYearStartDate);
                    accresult = accAccountDAOobj.addAccount(accjson);
                    account = (Account) accresult.getEntityList().get(0);
                    taxAccountId = account.getID();
                }

                taxMap.put("taxid", UUID.randomUUID().toString());
                taxMap.put("companyid", company.getCompanyID());
                taxMap.put("accountid", taxAccountId);
                taxresult = accTaxObj.addTax(taxMap);
                tax = (Tax) taxresult.getEntityList().get(0);

                taxListMap.put("applydate", financialYearStartDate);
                taxListMap.put("taxid", tax.getID());
                taxListMap.put("companyid", company.getCompanyID());

                taxlistresult = accTaxObj.addTaxList(taxListMap);
                taxlist = (TaxList) taxlistresult.getEntityList().get(0);
                companiesProcessed += company.getSubDomain()+",";
            }
        } catch (Exception e) {
            throw new AccountingException(e.getMessage());
        }
        /*
         * Returning the names of companies for ehich the tax is added and for which tax with same name already exists.
         */
        try {
            if(!StringUtil.isNullOrEmpty(companiesProcessed)){
                companiesProcessed.substring(0,companiesProcessed.length()-1);
            }
            if(!StringUtil.isNullOrEmpty(companiesAlreadyHavingTaxes)){
                companiesAlreadyHavingTaxes.substring(0,companiesAlreadyHavingTaxes.length()-1);
            }
            returnObject.put("companiesProcessed", companiesProcessed);
            returnObject.put("companiesAlreadyHavingTax", companiesAlreadyHavingTaxes);
        } catch (JSONException ex) {
            Logger.getLogger(CommonFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnObject;
    }
    
    
    /* Below script is used to update the Tax amount and Tax amount in base columns value */
    public ModelAndView updateInvoiceTaxAmountandTaxAmountInBase(HttpServletRequest request, HttpServletResponse response) {
        String msg = "False";
        try {
            int module = 0;

            if (!StringUtil.isNullOrEmpty(request.getParameter("module"))) {
                module = Integer.parseInt(request.getParameter("module"));
            }
            String subdomain = request.getParameter("subdomain");
            if (!StringUtil.isNullOrEmpty(subdomain)) {
                msg = updateInvAmtAndTaxAmtForCompany(subdomain, module);
            } else {
                KwlReturnObject kwlCompany = accountingHandlerDAOobj.getCompanyList();
                List ll = kwlCompany.getEntityList();
                Iterator iterator = ll.iterator();
                StringBuilder msgBuilder = new StringBuilder();
                while (iterator.hasNext()) {
                    Company company = (Company) iterator.next();
                    subdomain = company.getSubDomain();                    
                    msgBuilder.append(updateInvAmtAndTaxAmtForCompany(subdomain, module));
                }
                msg = msgBuilder.toString();
            }
        } catch (Exception ex) {
            System.out.println("------ Error at updateInvoiceTaxAmountandTaxAmountInBase() ----------");
            ex.printStackTrace();
        } 
        Logger.getLogger(CommonFunctions.class.getName()).log(Level.INFO, msg);
        return new ModelAndView("jsonView_ex", "model", msg);
    }

    private String updateInvAmtAndTaxAmtForCompany(String subdomain, int module) throws ServiceException, JSONException {
        String msg = "False";
        double taxAmountInBase = 0;
        double externalCurrencyRate = 0d;
        Date invoiceCreationDate = null;
        double excludingGstAmount = 0;
        double excludingGstAmountInBase = 0;
        HashMap<String, Object> requestParams = new HashMap();
        requestParams.put("subdomain", subdomain);
        KwlReturnObject bAmt = null;
        String fromcurrencyid = "";
        String companyId = companyDetailsDAOObj.getCompanyid(subdomain);
        if (module == 0) { //Sales or Customer Invoices Tax 
            
            
            
            KwlReturnObject srresult = accInvoiceDAOobj.getCompanyInvoices(companyId);
            List<Invoice> invoiceList = srresult.getEntityList();

            for (Invoice invObj : invoiceList) {
                    if(!invObj.isIsOpeningBalenceInvoice()){
                    if (!requestParams.containsKey("gcurrencyid")) {
                        requestParams.put("gcurrencyid", invObj.getCompany().getCurrency().getCurrencyID());
                        requestParams.put("companyid", invObj.getCompany().getCompanyID());
                    }
//                    invoiceCreationDate = invObj.getJournalEntry().getEntryDate();
                    invoiceCreationDate = invObj.getCreationDate();
                    externalCurrencyRate = invObj.getJournalEntry().getExternalCurrencyRate();
                    fromcurrencyid = invObj.getCurrency().getCurrencyID();
                    JSONObject invjson = new JSONObject();
                    invjson.put("invoiceid", invObj.getID());
                    double termAmount = getTotalTermsAmount(getSalesSideTermDetails(invObj.getID(), accInvoiceDAOobj));
                    /*Update the Global level Tax*/
                    if (invObj.getTaxEntry() != null) {
                        invjson.put("taxAmount", authHandler.round(invObj.getTaxEntry().getAmount(), companyId));
                        invjson.put("taxAmountInBase", authHandler.round(invObj.getTaxEntry().getAmountinbase(), companyId));

                        invjson.put("excludingGstAmount", invObj.getCustomerEntry().getAmount() - invObj.getTaxEntry().getAmount() - termAmount);
                        excludingGstAmountInBase = invObj.getCustomerEntry().getAmount() - invObj.getTaxEntry().getAmount() - termAmount;
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, excludingGstAmountInBase, fromcurrencyid, invoiceCreationDate, externalCurrencyRate);
                        excludingGstAmountInBase = (Double) bAmt.getEntityList().get(0);
                        excludingGstAmountInBase = authHandler.round(excludingGstAmountInBase, companyId);
                        invjson.put("excludingGstAmountInBase", excludingGstAmountInBase);
                        
                        /*Update the Line level Excluding GST amount and GST amount In Base  column in Invoice Detail Table*/
                        Set<InvoiceDetail> invoiceDetails = invObj.getRows();
                        double ramount = 0;
                        double quantity;
                        double rdisc = 0;
                        double lineLevelTermAmount = 0;
                        double rowExcludingGstAmountInBase = 0d;
                        for (InvoiceDetail invoiceDetail : invoiceDetails) {
                            quantity = invoiceDetail.getInventory().getQuantity();
                            lineLevelTermAmount = invoiceDetail.getLineLevelTermAmount();
                            rdisc = (invoiceDetail.getDiscount() == null ? 0 : invoiceDetail.getDiscount().getDiscountValue());
                            if (invoiceDetail.getInvoice().isGstIncluded()) {
                                ramount = invoiceDetail.getRateincludegst() * quantity;
                                ramount -= rdisc;
                                ramount += lineLevelTermAmount;
                            } else {
                                ramount = invoiceDetail.getRate() * quantity;
                                ramount -= rdisc;
                            }
                            ramount = authHandler.round(ramount, companyId);
                            invoiceDetail.setRowExcludingGstAmount(ramount);
                            /*Calculate Excluding GST amount in Base*/
//                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, ramount, fromcurrencyid, invoiceDetail.getInvoice().getJournalEntry().getEntryDate(), externalCurrencyRate);
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, ramount, fromcurrencyid, invoiceDetail.getInvoice().getCreationDate(), externalCurrencyRate);
                            rowExcludingGstAmountInBase = (Double) bAmt.getEntityList().get(0);
                            rowExcludingGstAmountInBase = authHandler.round(rowExcludingGstAmountInBase, companyId);
                            invoiceDetail.setRowExcludingGstAmountInBase(rowExcludingGstAmountInBase);
                        }
                    } else {
                        /*Update the Line level Tax*/
                        Set<InvoiceDetail> invoiceDetails = invObj.getRows();
                        double rowTaxAmt = 0d;
                        double rowTaxAmtInBase = 0d;
                        double rowExcludingGstAmountInBase = 0d;
                        double quantity;
                        double rowTaxPercent = 0;
                        double ramount = 0;
                        double rdisc = 0;
                        double lineLevelTermAmount = 0;
                        for (InvoiceDetail invoiceDetail : invoiceDetails) {
                            rowTaxAmt += invoiceDetail.getRowTaxAmount() + invoiceDetail.getRowTermTaxAmount();
//                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, (invoiceDetail.getRowTaxAmount()+invoiceDetail.getRowTermTaxAmount()), fromcurrencyid, invoiceDetail.getInvoice().getJournalEntry().getEntryDate(), externalCurrencyRate);
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, (invoiceDetail.getRowTaxAmount()+invoiceDetail.getRowTermTaxAmount()), fromcurrencyid, invoiceDetail.getInvoice().getCreationDate(), externalCurrencyRate);
                            rowTaxAmtInBase = (Double) bAmt.getEntityList().get(0);
                            rowTaxAmtInBase = authHandler.round(rowTaxAmtInBase, companyId);
                            invoiceDetail.setRowTaxAmountInBase(rowTaxAmtInBase);

                            /*Calculate Excluding GST amount*/
                            quantity = invoiceDetail.getInventory().getQuantity();
                            ramount = authHandler.round(invoiceDetail.getRate() * quantity, companyId);
                            lineLevelTermAmount = invoiceDetail.getLineLevelTermAmount();
                            rdisc = (invoiceDetail.getDiscount() == null ? 0 : invoiceDetail.getDiscount().getDiscountValue());
                            if (invoiceDetail.getTax() != null) {
//                                KwlReturnObject perresult = accTaxObj.getTaxPercent((String) requestParams.get(Constants.companyKey), invoiceDetail.getInvoice().getJournalEntry().getEntryDate(), invoiceDetail.getTax().getID());
                                KwlReturnObject perresult = accTaxObj.getTaxPercent((String) requestParams.get(Constants.companyKey), invoiceDetail.getInvoice().getCreationDate(), invoiceDetail.getTax().getID());
                                rowTaxPercent = (Double) perresult.getEntityList().get(0);
                            }
                            if (invoiceDetail.getInvoice().isGstIncluded()) {
                                ramount = invoiceDetail.getRateincludegst() * quantity;
                                ramount -= rdisc;
                                ramount += lineLevelTermAmount;
                                double taxAppliedOn = 0;
                                taxAppliedOn = (100 * ramount) / (100 + rowTaxPercent);
                                ramount = taxAppliedOn;
                            } else {
                                ramount = invoiceDetail.getRate() * quantity;
                                ramount -= rdisc;
                            }
                            ramount = authHandler.round(ramount, companyId);
                            invoiceDetail.setRowExcludingGstAmount(ramount);
                            /*Calculate Excluding GST amount in Base*/
//                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, ramount, fromcurrencyid, invoiceDetail.getInvoice().getJournalEntry().getEntryDate(), externalCurrencyRate);
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, ramount, fromcurrencyid, invoiceDetail.getInvoice().getCreationDate(), externalCurrencyRate);
                            rowExcludingGstAmountInBase = (Double) bAmt.getEntityList().get(0);
                            rowExcludingGstAmountInBase = authHandler.round(rowExcludingGstAmountInBase, companyId);
                            invoiceDetail.setRowExcludingGstAmountInBase(rowExcludingGstAmountInBase);
                        }
                        invjson.put("taxAmount", authHandler.round(rowTaxAmt, companyId));
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, rowTaxAmt, fromcurrencyid, invoiceCreationDate, externalCurrencyRate);
                        taxAmountInBase = (Double) bAmt.getEntityList().get(0);
                        taxAmountInBase = authHandler.round(taxAmountInBase, companyId);
                        invjson.put("taxAmountInBase", taxAmountInBase);
                        if (invObj.isGstIncluded()) {
                            excludingGstAmount = invObj.getCustomerEntry().getAmount() - rowTaxAmt;
                        } else {
                            excludingGstAmount = invObj.getCustomerEntry().getAmount() - rowTaxAmt - termAmount;
                        }
                        invjson.put("excludingGstAmount", excludingGstAmount);
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, excludingGstAmount, fromcurrencyid, invoiceCreationDate, externalCurrencyRate);
                        excludingGstAmountInBase = (Double) bAmt.getEntityList().get(0);
                        excludingGstAmountInBase = authHandler.round(excludingGstAmountInBase, companyId);
                        invjson.put("excludingGstAmountInBase", excludingGstAmountInBase);
                    }
                    accInvoiceDAOobj.updateInvoice(invjson, null);
                }
            }
            msg = "Records processed successfully. Total Records - " + invoiceList.size();
        } else { //Purchase or Vendor Invoices Tax 
            KwlReturnObject srresult = accGoodsReceiptobj.getCompanyGoodsReceipts(companyId);
            List<GoodsReceipt> invoiceList = srresult.getEntityList();
            for (GoodsReceipt invObj : invoiceList) {
                
                if (!invObj.isIsOpeningBalenceInvoice()) {
                    if (!requestParams.containsKey("gcurrencyid")) {
                        requestParams.put("gcurrencyid", invObj.getCompany().getCurrency().getCurrencyID());
                        requestParams.put("companyid", invObj.getCompany().getCompanyID());
                    }
                    Map<String, Object> greceipthm = new HashMap<String, Object>();
                    greceipthm.put("grid", invObj.getID());
                    fromcurrencyid = invObj.getCurrency().getCurrencyID();
                    externalCurrencyRate = invObj.getJournalEntry().getExternalCurrencyRate();
//                    invoiceCreationDate = invObj.getJournalEntry().getEntryDate();
                    invoiceCreationDate = invObj.getCreationDate();

                    double termAmount = getTotalTermsAmount(getTermDetails(invObj.getID(), accGoodsReceiptobj));
                    /*Update the Global level Tax*/
                    if (invObj.getTaxEntry() != null) {
                        greceipthm.put("taxAmount", authHandler.round(invObj.getTaxEntry().getAmount(), companyId));
                        greceipthm.put("taxAmountInBase", authHandler.round(invObj.getTaxEntry().getAmountinbase(), companyId));
                        greceipthm.put("excludingGstAmount", invObj.getVendorEntry().getAmount() - invObj.getTaxEntry().getAmount() - termAmount);
                        excludingGstAmountInBase = invObj.getVendorEntry().getAmount() - invObj.getTaxEntry().getAmount() - termAmount;
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, excludingGstAmountInBase, fromcurrencyid, invoiceCreationDate, externalCurrencyRate);
                        excludingGstAmountInBase = (Double) bAmt.getEntityList().get(0);
                        excludingGstAmountInBase = authHandler.round(excludingGstAmountInBase, companyId);
                        greceipthm.put("excludingGstAmountInBase", excludingGstAmountInBase);

                        /*Update the Line level Excluding GST amount and GST amount In Base Column in Goods Receipt Detail Table */
                        Set<GoodsReceiptDetail> grDetails = invObj.getRows();
                        double ramount = 0;
                        double quantity;
                        double rdisc = 0;
                        double lineLevelTermAmount = 0;
                        double rowExcludingGstAmountInBase = 0d;
                        for (GoodsReceiptDetail goodsReceiptDetail : grDetails) {
                            quantity = goodsReceiptDetail.getInventory().getQuantity();
                            lineLevelTermAmount = goodsReceiptDetail.getLineLevelTermAmount();
                            rdisc = (goodsReceiptDetail.getDiscount() == null ? 0 : goodsReceiptDetail.getDiscount().getDiscountValue());
                            if (goodsReceiptDetail.getGoodsReceipt().isGstIncluded()) {
                                ramount = goodsReceiptDetail.getRateincludegst() * quantity;
                                ramount -= rdisc;
                                ramount += lineLevelTermAmount;
                            } else {
                                ramount = goodsReceiptDetail.getRate() * quantity;
                                ramount -= rdisc;
                            }
                            ramount = authHandler.round(ramount, companyId);
                            goodsReceiptDetail.setRowExcludingGstAmount(ramount);
                            /*Calculate Excluding GST amount in Base*/
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, ramount, fromcurrencyid, invoiceCreationDate, externalCurrencyRate);
                            rowExcludingGstAmountInBase = (Double) bAmt.getEntityList().get(0);
                            rowExcludingGstAmountInBase = authHandler.round(rowExcludingGstAmountInBase, companyId);
                            goodsReceiptDetail.setRowExcludingGstAmountInBase(rowExcludingGstAmountInBase);
                        }

                    } else {
                        /*Update the Line level Tax*/
                        Set<GoodsReceiptDetail> grDetails = invObj.getRows();
                        double rowTaxAmt = 0d;
                        double rowTaxAmtInBase = 0d;
                        double rowExcludingGstAmountInBase = 0d;
                        double quantity;
                        double rowTaxPercent = 0;
                        double ramount = 0;
                        double rdisc = 0;
                        double lineLevelTermAmount = 0;
                        for (GoodsReceiptDetail goodsReceiptDetail : grDetails) {
                            rowTaxAmt += goodsReceiptDetail.getRowTaxAmount();
//                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, goodsReceiptDetail.getRowTaxAmount(), fromcurrencyid, goodsReceiptDetail.getGoodsReceipt().getJournalEntry().getEntryDate(), externalCurrencyRate);
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, goodsReceiptDetail.getRowTaxAmount(), fromcurrencyid, goodsReceiptDetail.getGoodsReceipt().getCreationDate(), externalCurrencyRate);
                            rowTaxAmtInBase = (Double) bAmt.getEntityList().get(0);
                            rowTaxAmtInBase = authHandler.round(rowTaxAmtInBase, companyId);
                            goodsReceiptDetail.setRowTaxAmountInBase(rowTaxAmtInBase);
                            /*Calculate Excluding GST amount*/
                            quantity = goodsReceiptDetail.getInventory().getQuantity();
                            ramount = authHandler.round(goodsReceiptDetail.getRate() * quantity, companyId);
                            lineLevelTermAmount = goodsReceiptDetail.getLineLevelTermAmount();
                            rdisc = (goodsReceiptDetail.getDiscount() == null ? 0 : goodsReceiptDetail.getDiscount().getDiscountValue());
                            if (goodsReceiptDetail.getTax() != null) {
//                                KwlReturnObject perresult = accTaxObj.getTaxPercent((String) requestParams.get(Constants.companyKey), goodsReceiptDetail.getGoodsReceipt().getJournalEntry().getEntryDate(), goodsReceiptDetail.getTax().getID());
                                KwlReturnObject perresult = accTaxObj.getTaxPercent((String) requestParams.get(Constants.companyKey), goodsReceiptDetail.getGoodsReceipt().getCreationDate(), goodsReceiptDetail.getTax().getID());
                                rowTaxPercent = (Double) perresult.getEntityList().get(0);
                            }
                            if (goodsReceiptDetail.getGoodsReceipt().isGstIncluded()) {
                                ramount = goodsReceiptDetail.getRateincludegst() * quantity;
                                ramount -= rdisc;
                                ramount += lineLevelTermAmount;
                                double taxAppliedOn = 0;
                                taxAppliedOn = (100 * ramount) / (100 + rowTaxPercent);
                                ramount = taxAppliedOn;
                            } else {
                                ramount = goodsReceiptDetail.getRate() * quantity;
                                ramount -= rdisc;
                            }
                            ramount = authHandler.round(ramount, companyId);
                            goodsReceiptDetail.setRowExcludingGstAmount(ramount);
                            /*Calculate Excluding GST amount in Base*/
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, ramount, fromcurrencyid, invoiceCreationDate, externalCurrencyRate);
                            rowExcludingGstAmountInBase = (Double) bAmt.getEntityList().get(0);
                            rowExcludingGstAmountInBase = authHandler.round(rowExcludingGstAmountInBase, companyId);
                            goodsReceiptDetail.setRowExcludingGstAmountInBase(rowExcludingGstAmountInBase);
                        }
                        greceipthm.put("taxAmount", authHandler.round(rowTaxAmt, companyId));
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, rowTaxAmt, fromcurrencyid, invoiceCreationDate, externalCurrencyRate);
                        taxAmountInBase = (Double) bAmt.getEntityList().get(0);
                        taxAmountInBase = authHandler.round(taxAmountInBase, companyId);
                        greceipthm.put("taxAmountInBase", taxAmountInBase);
                        if (invObj.isGstIncluded()) {
                            excludingGstAmount = invObj.getVendorEntry().getAmount() - rowTaxAmt;
                        } else {
                            excludingGstAmount = invObj.getVendorEntry().getAmount() - rowTaxAmt - termAmount;
                        }
                        excludingGstAmount = authHandler.round(excludingGstAmount, companyId);
                        greceipthm.put("excludingGstAmount", excludingGstAmount);
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, excludingGstAmount, fromcurrencyid, invoiceCreationDate, externalCurrencyRate);
                        excludingGstAmountInBase = (Double) bAmt.getEntityList().get(0);
                        excludingGstAmountInBase = authHandler.round(excludingGstAmountInBase, companyId);
                        greceipthm.put("excludingGstAmountInBase", excludingGstAmountInBase);
                    }
                    accGoodsReceiptobj.updateGoodsReceipt(greceipthm);
                }

            }
            msg = "Records processed successfully. Total Records - " + invoiceList.size();

        }
        System.out.println(msg + " for Company -> " + subdomain);

        return msg;
    }
    public  JSONArray getSalesSideTermDetails(String invoiceid, accInvoiceDAO accInvoiceDAOobj) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            HashMap<String, Object> requestParam = new HashMap();
            requestParam.put("invoiceid", invoiceid);
            KwlReturnObject curresult = accInvoiceDAOobj.getInvoiceTermMap(requestParam);
            List<InvoiceTermsMap> termMap = curresult.getEntityList();
            for (InvoiceTermsMap invoiceTerMap : termMap) {
                    InvoiceTermsSales mt = invoiceTerMap.getTerm();
                    JSONObject jsonobj = new JSONObject();
                    jsonobj.put("id", mt.getId());
                    jsonobj.put("term", mt.getTerm());
                    jsonobj.put("glaccount", mt.getAccount().getID());
                    jsonobj.put("sign", mt.getSign());
                    jsonobj.put("formula", mt.getFormula());
                    jsonobj.put("termpercentage", invoiceTerMap.getPercentage());
                    jsonobj.put("termamount", invoiceTerMap.getTermamount());
                    jArr.put(jsonobj);
                }
        } catch (com.krawler.utils.json.base.JSONException ex) {
            Logger.getLogger(CommonFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
    public static JSONArray getTermDetails(String invoiceid, accGoodsReceiptDAO accGoodsReceiptobj) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            HashMap<String, Object> requestParam = new HashMap();
            requestParam.put("invoiceid", invoiceid);
            KwlReturnObject curresult = accGoodsReceiptobj.getInvoiceTermMap(requestParam);
            List<ReceiptTermsMap> termMap = curresult.getEntityList();
            for (ReceiptTermsMap invoiceTerMap : termMap) {
                InvoiceTermsSales mt = invoiceTerMap.getTerm();
                com.krawler.utils.json.base.JSONObject jsonobj = new com.krawler.utils.json.base.JSONObject();
                jsonobj.put("id", mt.getId());
                jsonobj.put("term", mt.getTerm());
                jsonobj.put("glaccount", mt.getAccount().getID());
                jsonobj.put("glaccountname", mt.getAccount().getAccountName());
                jsonobj.put("accountid", mt.getAccount().getID());
                jsonobj.put("accode", mt.getAccount().getAcccode());
                jsonobj.put("sign", mt.getSign());
                jsonobj.put("formula", mt.getFormula());
                jsonobj.put("termpercentage", invoiceTerMap.getPercentage());
                jsonobj.put("termamount", invoiceTerMap.getTermamount());
                jArr.put(jsonobj);
            }
        } catch (com.krawler.utils.json.base.JSONException ex) {
            Logger.getLogger(CommonFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
    /*
     * ERP-28048 	
     * Function for updating GST Currency rate for invoices,credit notes and debit notes where they are set to zero
     */ 
    public ModelAndView updateGSTCurrencyRate(HttpServletRequest request, HttpServletResponse response){
        String msg = "";
        try{
            KwlReturnObject result = null; 
            List<GoodsReceipt> GRList = null;
            List<GoodsReceiptDetail> GRDList = null;
            List<Invoice> invoiceList=null;
            List<InvoiceDetail> invoiceDetailsList = null;
            List<CreditNoteTaxEntry> cnTaxEntryList = null;
            List<DebitNoteTaxEntry> dnTaxEntryList = null;
            /*
             * Finding companies with country Singapore and Currency non SGD 
             */
            result = companyDetailsDAOObj.getSingaporeCompaniesWithDifferentCurrency();
            List<Company> list = result.getEntityList();
            String companyId="";
            String transactionCurrencyId = "";
            Date transactionDate = null;
            double gstCurrencyRateToSet = 0.0d;
            HashMap<String, Object> requestParams = new HashMap<>();
            
            requestParams.put(Constants.df, authHandler.getDateOnlyFormat());
        
            HashMap<String,Object> map = new HashMap<>();
            map.put("currencyNotIn", Constants.SGDID);
            map.put("gstCurrencyRate", 0.0);
            for(Company company:list){
                
                companyId = company.getCompanyID();
                requestParams.put(Constants.companyKey, companyId);
                requestParams.put(Constants.globalCurrencyKey, company.getCurrency().getCurrencyID());
                map.put("companyId", companyId);
                
                /*
                 * Fetching purchase invoices with global level tax
                 */ 
                result = accGoodsReceiptobj.getGoodsReceiptsWithGlobalTax(map);
                GRList = result.getEntityList();
                if(GRList!=null && !GRList.isEmpty()){
                   for(GoodsReceipt goodsReceipt:GRList) {
                       transactionCurrencyId = goodsReceipt.getCurrency().getCurrencyID();
//                       transactionDate = goodsReceipt.getJournalEntry().getEntryDate();
                       transactionDate = goodsReceipt.getCreationDate();
                       
                       result = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, 1.0, transactionCurrencyId, Constants.SGDID, transactionDate, 0);
                       gstCurrencyRateToSet = (double) result.getEntityList().get(0);
                       
                       goodsReceipt.setGstCurrencyRate(1/gstCurrencyRateToSet);
                   }
                }
                /*
                 * Fetching purchase invoices with line level tax
                 */ 
                result = accGoodsReceiptobj.getGoodsReceiptsWithLineLevelTax(map);
                GRDList = result.getEntityList();
                if(GRDList!=null && !GRDList.isEmpty()){
                    for(GoodsReceiptDetail GRD:GRDList){
                        transactionCurrencyId = GRD.getGoodsReceipt().getCurrency().getCurrencyID();
//                        transactionDate =  GRD.getGoodsReceipt().getJournalEntry().getEntryDate();
                        transactionDate =  GRD.getGoodsReceipt().getCreationDate();
                       
                        result = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, 1.0, transactionCurrencyId, Constants.SGDID, transactionDate, 0);
                        gstCurrencyRateToSet = (double) result.getEntityList().get(0);
                        
                        GRD.setGstCurrencyRate(1/gstCurrencyRateToSet);
                    }
                }
                /*
                 * Fetching sales invoices with global level tax
                 */ 
                result = accInvoiceDAOobj.getInvoicesWithGlobalTax(map);
                invoiceList = result.getEntityList();
                if(invoiceList!=null && !invoiceList.isEmpty()){
                    for(Invoice invoice:invoiceList){
                        transactionCurrencyId = invoice.getCurrency().getCurrencyID();
//                        transactionDate =  invoice.getJournalEntry().getEntryDate();
                        transactionDate =  invoice.getCreationDate();
                        
                        result = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, 1.0, transactionCurrencyId, Constants.SGDID, transactionDate, 0);
                        gstCurrencyRateToSet = (double) result.getEntityList().get(0);
                        
                        invoice.setGstCurrencyRate(1/gstCurrencyRateToSet);
                    }
                }
                /*
                 * Fetching sales invoices with line level tax
                 */ 
                result = accInvoiceDAOobj.getInvoicesWithLineLevelTax(map);
                invoiceDetailsList = result.getEntityList();
                if(invoiceDetailsList != null && !invoiceDetailsList.isEmpty()){
                    for(InvoiceDetail invDetail : invoiceDetailsList){
                        transactionCurrencyId = invDetail.getInvoice().getCurrency().getCurrencyID();
//                        transactionDate = invDetail.getInvoice().getJournalEntry().getEntryDate();
                        transactionDate = invDetail.getInvoice().getCreationDate();
                        
                        result = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, 1.0, transactionCurrencyId, Constants.SGDID, transactionDate, 0);
                        gstCurrencyRateToSet = (double) result.getEntityList().get(0);
                        
                        invDetail.setGstCurrencyRate(1/gstCurrencyRateToSet);
                    }
                }
                
                /*
                 * Fetching credit notes with tax
                 */ 
                result = accCreditNoteobj.getCNWithTax(map);
                cnTaxEntryList = result.getEntityList();
                if(cnTaxEntryList!=null && !cnTaxEntryList.isEmpty()){
                    for(CreditNoteTaxEntry cntaxEntry : cnTaxEntryList){
                        transactionCurrencyId = cntaxEntry.getCreditNote().getCurrency().getCurrencyID();
//                        transactionDate = cntaxEntry.getCreditNote().getJournalEntry().getEntryDate();
                        transactionDate = cntaxEntry.getCreditNote().getCreationDate();
                        
                        result = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, 1.0, transactionCurrencyId, Constants.SGDID, transactionDate, 0);
                        gstCurrencyRateToSet = (double) result.getEntityList().get(0);
                        
                        cntaxEntry.setGstCurrencyRate(1/gstCurrencyRateToSet);
                    } 
                }
                
                /*
                 * Fetching debit notes with tax
                 */ 
                result = accDebitNoteobj.getDNWithTax(map);
                dnTaxEntryList = result.getEntityList();
                if(dnTaxEntryList!=null && !dnTaxEntryList.isEmpty()){
                    for(DebitNoteTaxEntry dntaxEntry : dnTaxEntryList){
                        transactionCurrencyId = dntaxEntry.getDebitNote().getCurrency().getCurrencyID();
//                        transactionDate = dntaxEntry.getDebitNote().getJournalEntry().getEntryDate();
                        transactionDate = dntaxEntry.getDebitNote().getCreationDate();
                        
                        result = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, 1.0, transactionCurrencyId, Constants.SGDID, transactionDate, 0);
                        gstCurrencyRateToSet = (double) result.getEntityList().get(0);
                        
                        dntaxEntry.setGstCurrencyRate(1/gstCurrencyRateToSet);
                    }
                }
                
            }
            msg = "Records has been updated successfully ";
        } catch(Exception ex){
            System.out.println("------ Error at updateGSTCurrencyRate() ----------");
            ex.printStackTrace();
        }
        return new ModelAndView("jsonView_ex", "model", msg);
    }
    
    public ModelAndView activateMultiEntity(HttpServletRequest request, HttpServletResponse response) {
        String msg = "", msg2 = "";
        try {
            String subdomain = request.getParameter(Constants.COMPANY_SUBDOMAIN);
            String defaultValue = request.getParameter(Constants.defaultvalue);
            String companyId = "";
            if (!StringUtil.isNullOrEmpty(subdomain)) {
                companyId = companyDetailsDAOObj.getCompanyid(subdomain);
            } else {
                msg2 = "Please provide valid Subdomain.";
                throw new AccountingException("---------------Please provide valid Subdomain.--------------------");
            }
            if (!StringUtil.isNullOrEmpty(companyId)) {
                /*
                 * Inserting Custom Dimension in SI,PI,JE,MP,RP,DN,CN,DO for process multi entity 
                 * if Custom Dimension is not created from company setup
                 */
                KwlReturnObject companyPreferencesKwlObj = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyId);
                if (companyPreferencesKwlObj != null && companyPreferencesKwlObj.getEntityList().size() > 0) {
                    ExtraCompanyPreferences companyPreferences = (ExtraCompanyPreferences) companyPreferencesKwlObj.getEntityList().get(0);
                    if (!companyPreferences.isIsMultiEntity()) {
                        HashMap<String, Object> requestParams = new HashMap<>();
                        requestParams.put(Constants.isMultiEntity, Boolean.parseBoolean(request.getParameter(Constants.isMultiEntity)));
                        requestParams.put(Constants.companyid, companyId);
                        requestParams.put("DefaultValue", subdomain);
                        requestParams.put("DefaultValue", !StringUtil.isNullOrEmpty(defaultValue) ? defaultValue.trim() : subdomain);
                        boolean isDimensionCreated = accAccountDAOobj.insertDefaultCustomeFields(requestParams);
                        companyPreferences.setIsMultiEntity(true);
                        companyPreferences.setIsDimensionCreated(isDimensionCreated);
                        msg = "------- Multi Entity Feature activated successfully for Company <b>" + subdomain + "</b> ----------";
                    } else {
                        msg = "------- Multi Entity Feature already activated for Company <b>" + subdomain + "</b> ----------";
                    }
                }
            } else {
                msg2 = "Company not found with given Subdomain. Please provide valid Subdomain.";
            }
        } catch (Exception ex) {
            msg = "------- Error occurred while activating Multi Entity Feature. " + msg2 + " ----------";
            System.out.println("------- Error occurred while activating Multi Entity Feature----------");
        }
        return new ModelAndView("jsonView_ex", "model", msg);
    }
    
    public ModelAndView deleteAdvancePaymentAndReceiptLinking(HttpServletRequest request, HttpServletResponse response) {
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<>();
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.type))) {
                requestParams.put(Constants.type, Integer.parseInt(request.getParameter(Constants.type)));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.COMPANY_SUBDOMAIN))) {
                requestParams.put(Constants.companyKey, companyDetailsDAOObj.getCompanyid(request.getParameter(Constants.COMPANY_SUBDOMAIN)));
                requestParams.put(Constants.COMPANY_SUBDOMAIN, request.getParameter(Constants.COMPANY_SUBDOMAIN));
                msg = deleteAdvancePaymentAndReceiptLinking(requestParams);
            } else {
                KwlReturnObject kwlCompany = accountingHandlerDAOobj.getCompanyList();
                List<Company> companyList = kwlCompany.getEntityList();
                msg+="<html> <body>";
                for (Company company : companyList) {
                    requestParams.put(Constants.companyKey, company.getCompanyID());
                    requestParams.put(Constants.COMPANY_SUBDOMAIN, company.getSubDomain());
                    msg+=deleteAdvancePaymentAndReceiptLinking(requestParams);
                }
                msg+="<html> <body>";
            }
        } catch (Exception ex) {
            msg = "Error occurred while deleting corrupted linking data. ";
            Logger.getLogger(CommonFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView_ex", "model", msg);
    }

    public String deleteAdvancePaymentAndReceiptLinking(HashMap<String, Object> requestParams) throws ServiceException {
        String msg1 = "", msg2 = "";
        try {
            int count = 0;
            int type = requestParams.containsKey(Constants.type) ? (int) requestParams.get(Constants.type) : -1;
            String subdomain = requestParams.containsKey(Constants.COMPANY_SUBDOMAIN) ? (String) requestParams.get(Constants.COMPANY_SUBDOMAIN) : "";
            requestParams.put("unlinkflag", true);
            if (type == Constants.Acc_Vendor_Invoice_ModuleId) {
                /*
                 * To delete corrupted linking data of Purchase Invoice -> Advance Payment
                 */
                System.out.println("****************Deletion of Corrupted linking data started for Company :" + subdomain + "**************************");
                KwlReturnObject kwlPaymentDetailObj = null;
                KwlReturnObject linkResult = null;
                List<LinkDetailPayment> linkDetailPayment = null;
                requestParams.put("type", type);
                requestParams.put(Constants.moduleid, Constants.Acc_Make_Payment_ModuleId);//To fetch payment linking data only
                List<GoodsReceiptLinking> goodsReceiptLinkingDataList = accGoodsReceiptobj.getGoodsReceiptLinkingDataToValidateLinkingInfo(requestParams);
                for (GoodsReceiptLinking grl : goodsReceiptLinkingDataList) {
                    requestParams.put("grid", grl.getDocID() != null ? grl.getDocID().getID() : "");
                    requestParams.put("paymentid", grl.getLinkedDocID());
                    kwlPaymentDetailObj = accVendorPaymentobj.getPaymentsFromGReceipt(requestParams);
                    List<PaymentDetail> paymentDetails = kwlPaymentDetailObj.getEntityList();
                    if (paymentDetails.isEmpty()) {
                        /*
                         * if Payment is not against PI then verified its linking entry in LinkDetailPayment
                         */
                        linkResult = accVendorPaymentobj.getLinkedDetailsPayment(requestParams);
                        linkDetailPayment = linkResult.getEntityList();
                        if (linkDetailPayment.isEmpty()) {
                            /*
                             *If entry is not present in LinkDetailPayment then deleted from GoodsReceiptLinking and PaymentLinking
                             */
                            requestParams.put(Constants.billid, grl.getDocID() != null ? grl.getDocID().getID() : "");
                            requestParams.put("linkedTransactionID", grl.getLinkedDocID());
                            accGoodsReceiptobj.deleteLinkingInformationOfPI(requestParams);
                            count++;
                            System.out.println(count + ". PI No:" + grl.getDocID().getGoodsReceiptNumber());
                            Logger.getLogger(count + ". PI No:" + grl.getDocID().getGoodsReceiptNumber());
                        }
                    }
                }
            } else if (type == Constants.Acc_Invoice_ModuleId) {
                /*
                 * To delete corrupted linking data of sales Invoice -> advance Receipt
                 */
                System.out.println("**************** Deletion of Corrupted linking data started for Company :" + subdomain + "**************************");
                KwlReturnObject kwlReceiptDetailsObj = null;
                KwlReturnObject result = null;
                List<LinkDetailReceipt> linkDetailReceipt = null;
                requestParams.put("type", type);
                requestParams.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);//To fetch receipt linking data only
                List<InvoiceLinking> invoiceLinkingDataList = accInvoiceDAOobj.getInvoiceLinkingDataToValidateLinkingInfo(requestParams);
                for (InvoiceLinking invl : invoiceLinkingDataList) {
                    requestParams.put("invoiceid", invl.getDocID() != null ? invl.getDocID().getID() : "");
                    requestParams.put("receiptid", invl.getLinkedDocID());
                    kwlReceiptDetailsObj = accReceiptobj.getReceiptFromInvoice(requestParams);
                    List<ReceiptDetail> receiptDetails = kwlReceiptDetailsObj.getEntityList();
                    if (receiptDetails.isEmpty()) {
                        /*
                         * if Receipt is not against SI then verified its linking entry in LinkDetailReceipt
                         */
                        result = accReceiptobj.getLinkDetailReceipt(requestParams);
                        linkDetailReceipt = result.getEntityList();
                        if (linkDetailReceipt.isEmpty()) {
                            /*
                             * If entry is not present in LinkDetailReceipt then deleted from InvoiceLinking and ReceiptLinking
                             */
                            requestParams.put(Constants.billid, invl.getDocID() != null ? invl.getDocID().getID() : "");
                            requestParams.put("linkedTransactionID", invl.getLinkedDocID());
                            accInvoiceDAOobj.deleteLinkingInformationOfSI(requestParams);
                            count++;
                            System.out.println(count + ". SI No:" + invl.getDocID().getInvoiceNumber());
                            Logger.getLogger(count + ". SI No:" + invl.getDocID().getInvoiceNumber());
                        }
                    }
                }
            } else {
                msg2 = "Please provide valid type Id (6 For Purchase Invoice and 2 For Sales Invoice)";
                throw new AccountingException("---------------Please provide valid Module ID.--------------------");
            }
            msg1 = "Corrupted linking data deleted successfully. Total records - " + count + " for company -> <b>" + subdomain + "</b>.</br>";
        } catch (Exception ex) {
            throw ServiceException.FAILURE("CommonFunctions.deleteAdvancePaymentAndReceiptLinking : " + ex.getMessage(), ex);
        }
        return msg1 + msg2;
    }
    
    public ModelAndView updateAssemblyProduct(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        int quotationsUpdated = 0;
        boolean issuccess = false;
        HashMap<String, Object> buildRequestParams = new HashMap<String, Object>();

        //Create transaction
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
        TransactionStatus status = txnManager.getTransaction(def);

        try {
            String companyid = request.getParameter("companyid");
            String currencyId = request.getParameter("currencyid");

            buildRequestParams.put("type", Producttype.ASSEMBLY);
            buildRequestParams.put("isBuild", false);
            buildRequestParams.put(Constants.companyKey, companyid);
            KwlReturnObject pResult = accProductObj.getProductsByType(buildRequestParams);
            List<Object[]> pList = pResult.getEntityList();

            for (Object[] productRow : pList) {
                Product product = (Product) productRow[0];

                String productid = product.getID(); //"4028e4d356939f830156a2aa6cbc76cf";   //
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("companyid", companyid);
                requestParams.put(Constants.productid, productid);
                requestParams.put(Constants.currencyKey, currencyId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("isdefaultbom"))) {
                    boolean isdefaultbom = Boolean.parseBoolean(request.getParameter("isdefaultbom"));
                    requestParams.put("isdefaultbom", isdefaultbom);
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("bomdetailid"))) { //This is for Multiple MRP BOM Formula
                    requestParams.put("bomdetailid", request.getParameter("bomdetailid"));
                } else if (!StringUtil.isNullOrEmpty(request.getParameter("bomid"))) {    //This is for selected BOM formula in Build Assembly on Build Quantity selection
                    requestParams.put("bomdetailid", request.getParameter("bomid"));
                }
                KwlReturnObject result = accProductObj.getAssemblyItems(requestParams);

                JSONArray jArr = new JSONArray();
                List list = result.getEntityList();
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    Object[] row = (Object[]) itr.next();
                    ProductAssembly passembly = (ProductAssembly) row[0];
                    JSONObject obj = new JSONObject();

                    obj.put("purchaseprice", row[1] == null ? 0 : row[1]);
                    obj.put("percentage", passembly.getPercentage());
                    obj.put("quantity", passembly.getQuantity());
                    jArr.put(obj);
                }
                double subtotal = 0.0;
                for (int count = 0; count < jArr.length(); count++) {
                    double priceAfterDiscount = 0;
                    double price = 0.0;
                    double disc = 0.0;
                    price = jArr.optJSONObject(count).optDouble("purchaseprice", 0.0);
                    disc = jArr.optJSONObject(count).optDouble("percentage", 100);
                    double Qty = jArr.optJSONObject(count).optDouble("quantity", 0);
                    priceAfterDiscount = (double) (price * ((disc * Qty) / 100));
                    subtotal += priceAfterDiscount;
                }

                HashMap<String, Object> customRequestParams = new HashMap<String, Object>();
                HashMap<String, Object> productMap = new HashMap<String, Object>();
//                KwlReturnObject custumObjresult = accountingHandlerDAOobj.getObject(AccProductCustomData.class.getName(), productid);
                customRequestParams.put("Col1001", String.valueOf(authHandler.roundUnitPrice(subtotal, companyid)));
                customRequestParams.put("customdataclasspath", Constants.Acc_Product_custom_data_classpath);
                customRequestParams.put(Constants.Acc_Productid, productid);
                customRequestParams.put(Constants.Acc_Product_modulename, productid);
                customRequestParams.put("moduleprimarykey", Constants.Acc_Productid);
                customRequestParams.put("Company", companyid);

                customRequestParams.put("ModuleId", String.valueOf(Constants.Acc_Product_Master_ModuleId));
                productMap.put("id", product.getID());
                result = fieldDataManagerDAOobj.setCustomData(customRequestParams);
                if (result != null && result.getEntityList().size() > 0) {
                    productMap.put("accproductcustomdataref", product.getID());
                    result = accProductObj.updateProduct(productMap);
                }
                issuccess = true;
            }

        } catch (Exception ex) {
            System.out.println("------ Error at updateQuotationAmount() ----------");
            ex.printStackTrace();
            txnManager.rollback(status);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("totalQuotationsUpdated", quotationsUpdated);

            } catch (JSONException ex) {
                System.out.println("------ Error at updateQuotationAmount() ----------");
                ex.printStackTrace();
                txnManager.rollback(status);
            }
        }
        txnManager.commit(status);

        return new ModelAndView("jsonView", "model", jobj.toString());

    }
    
    public ModelAndView updateInventoryJE(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("USIJE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String companyid = request.getParameter("companyid");
            String productId = request.getParameter("productid");
            HashMap<String, Object> reqMap = new HashMap<>();
            if (!StringUtil.isNullOrEmpty(productId)) {
                reqMap.put("productIds", productId);
            }
            reqMap.put(Constants.companyKey, companyid);
            inventoryValuationProcess.add(reqMap);
            Thread t = new Thread(inventoryValuationProcess);
            t.setName("Valuation Thread..!");
            t.setPriority(8);
            t.start();
            txnManager.commit(status);
            issuccess = true;
            jobj.put("success", issuccess);
        } catch (Exception e) {
            txnManager.rollback(status);
            e.printStackTrace();
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            jobj.put("success", issuccess);
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
    
    
    
    public ModelAndView onMigrateIndianGSTAccountSetup(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        try {
            System.out.println(" Default Account GST Setup - Start");
            String subdomain = request.getParameter("subdomain");
            Map<String, Object> companyMap = new HashMap();
            if (!StringUtil.isNullOrEmpty(subdomain)) {
                String[] subDomainArray = {subdomain};
                companyMap.put("subdomains", subDomainArray);
            }
  
            KwlReturnObject companyResult = accCompanyPreferencesObj.getCompanyList(companyMap);
            List<String>ll=companyResult.getEntityList();
            for(String companyid : ll){
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                Company company = (Company) returnObject.getEntityList().get(0);
                Map<String, Object> defaultValueMap = new HashMap<String, Object>();
                defaultValueMap.put("companyid", company.getCompanyID());
                defaultValueMap.put(Constants.COUNTRY_ID, company.getCountry().getID());
                accAccountDAOobj.copyIndiaGSTDefaultAccounts(companyid, company.getCurrency().getCurrencyID(),defaultValueMap);
                System.out.println("------ Default GST Accounts copied for = "+ company.getSubDomain());
            }
            System.out.println(" Default Account GST Setup - Done");
        } catch (Exception e) {
            issuccess = false;
            e.printStackTrace();
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            jobj.put("success", issuccess);
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
    
    
    public ModelAndView onMigrateIndianGSTTermsSetup(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        try {
            System.out.println(" Default Terms GST Setup - Start");
            String subdomain = request.getParameter("subdomain");
            Map<String, Object> companyMap = new HashMap();
            if (!StringUtil.isNullOrEmpty(subdomain)) {
                String[] subDomainArray = {subdomain};
                companyMap.put("subdomains", subDomainArray);
            }
  
            KwlReturnObject companyResult = accCompanyPreferencesObj.getCompanyList(companyMap);
//            KwlReturnObject kwlCompany=accountingHandlerDAOobj.getCompanyList();
            List<String>ll=companyResult.getEntityList();
            for(String companyid : ll){
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                Company company = (Company) returnObject.getEntityList().get(0);
                
                HashMap<String, Object> defaultValueMap = new HashMap<String, Object>();
                defaultValueMap.put("companyid", company.getCompanyID());
                defaultValueMap.put(Constants.COUNTRY_ID, company.getCountry().getID());
                accAccountDAOobj.copyDefaultIndiaGSTTermsOnMigration(defaultValueMap);
                System.out.println("------ Default GST Terms copied for = "+ company.getSubDomain());
            }
            System.out.println(" Default Terms GST Setup - Done");
        } catch (Exception e) {
            e.printStackTrace();
            issuccess = false;
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            jobj.put("success", issuccess);
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }

    /**
     * Method to update JournalEntryDetail ID in Build Assembly records.
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView updateBuildAssemblyJE(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        StringBuilder msg = new StringBuilder();
        try {
            String isBuild = "true";
            if (!StringUtil.isNullOrEmpty(request.getParameter("isBuild"))) {
                isBuild = request.getParameter("isBuild");
            }
            String subdomains = request.getParameter("subdomain");
            Map<String, Object> requestMap = new HashMap<>();
            if (!StringUtil.isNullOrEmpty(subdomains)) {
                String[] subDomainArray = {subdomains};
                requestMap.put("subdomains", subDomainArray);
            }
            KwlReturnObject companyResultList = accCompanyPreferencesObj.getCompanyList(requestMap);
            List<String> companyList = companyResultList.getEntityList();
            for (String companyID : companyList) {
                String subdomain = companyDetailsDAOObj.getSubDomain(companyID);
                try {
                    HashMap<String, Object> requestParams = new HashMap<>();
                    requestParams.put("companyid", companyID);
                    requestParams.put("exportfalg", true);// Get All Assembly SKIPING PAGING
                    requestParams.put("isBuild", isBuild);
                    KwlReturnObject result = accProductObj.getAssemblyProducts(requestParams);
                    List<ProductBuild> list = result.getEntityList();
                    for (ProductBuild prodBuild : list) {
                        String Productid = prodBuild.getID();
                        double buildQty = prodBuild.getQuantity();
                        double buildCost = 0.0;
                        JournalEntry je = prodBuild.getJournalentry();
                        if (je != null) {
                            /**
                             * DELETE OLD JE DETAILS*
                             */
                            accJournalEntryobj.deleteJEDtails(je.getID(), companyID);
                            /**
                             * CREDIT ENTRY FOR THE JOURNAL ENTRY
                             *
                             */
                            JSONObject jedjson;
                            KwlReturnObject resultDetails = accProductObj.getAssemblyBuidDetails(Productid);
                            List<ProductBuildDetails> resultDetailsList = resultDetails.getEntityList();
                            int i = 2;
                            for (ProductBuildDetails pbdetails : resultDetailsList) {
                                double detailRate = pbdetails.getRate();
                                detailRate = detailRate * pbdetails.getInventoryQuantity() * buildQty;
                                buildCost += authHandler.round(detailRate, companyID);
                                jedjson = new JSONObject();
                                jedjson.put("srno", i++);
                                jedjson.put("companyid", companyID);
                                jedjson.put("amount", authHandler.round(detailRate, companyID));
                                jedjson.put("accountid", pbdetails.getAproduct().getPurchaseAccount().getID());
                                jedjson.put("debit", false);
                                jedjson.put("jeid", je.getID());
                                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                pbdetails.setJedetail(jed);
                            }
                            /**
                             * DEBIT ENTRY FOR PRODUCT ASSEMBLY
                             *
                             */
                            jedjson = new JSONObject();
                            jedjson.put("srno", 1);
                            jedjson.put("companyid", companyID);
                            jedjson.put("amount", buildCost);
                            jedjson.put("accountid", prodBuild.getProduct().getPurchaseAccount().getID());
                            jedjson.put("debit", true);
                            jedjson.put("jeid", je.getID());
                            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            JournalEntryDetail totaljed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            prodBuild.setTotaljed(totaljed);
                            prodBuild.setProductcost(buildCost);
                        }
                    }
                    msg.append("</br> Subdomain:").append("<b>").append(subdomain).append("</b>").append(" Updated: ").append(list.size());
                } catch (Exception ex) {
                    msg.append("</br> Subdomain:").append("<b>").append(subdomain).append("</b>").append(" Exception occured. ");
                }
            }
        } catch (Exception ex) {
            System.out.println("------ Error at assemblyProductsJEUpdate() ----------");
            ex.printStackTrace();
        } finally {
            System.out.println(msg);
        }
        return new ModelAndView("jsonView_ex", "model", msg.toString());
    }
    
    public Date convertCustomStringDateToDateObject(String date){    
        Date customDate=null; 
        try{
            DateFormat dateFormat = new SimpleDateFormat(Constants.MMMMdyyyy);// this is fixed format in which Custom Date retrived from customTable, i.e. from acccustomdata.getCol()
            customDate = dateFormat.parse(date);            
        }catch(Exception e){
           customDate= new Date();
        }finally{
            return customDate;
        }
    }
    //    public ModelAndView getFinancialReportDifference(HttpServletRequest request, HttpServletResponse response) {
//        String msg = "False";
//        try {
//            authHandlerControllerObj.verifyLogin(request, response);
//            int flagno = Integer.parseInt(request.getParameter("flagno"));
//            if (flagno == 1) {  //Trial Balance
//                //accReportsControllerObj.getTrialBalance(request);
//                msg = "success";
//            } else if (flagno == 3) {   //T & PL
//                //accReportsControllerObj.getTradingAndProfitLoss(request, response);
//            } else if (flagno == 4) {   //Balance Sheet
//                //accReportsService.getBalanceSheet(request);
//                msg = "success";
//            } else if (flagno == 2) {   //General Ledger
//                //accReportsControllerObj.getGeneralLedger(request, response);
//            } else if (flagno == 5) {   //Aged Payable
//                //HashMap<String, Object> requestParams = accGoodsReceiptServiceHandler.getGoodsReceiptRequestMap(request);
//                //accGoodsReceiptServiceDAO.getVendorAgedPayableMerged(request, requestParams);
//                msg = "success";
//            } else if (flagno == 6) {   //Aged Receivable
//                //AccInvoiceServiceDAOObj.getCustomerAgedReceivableMerged(request, false, true);
//                // msg = "success";
//            } else if (flagno == 71) {   //SOA Report - Customer
//                //accReportsControllerObj.getCustomerLedger(request);
//            } else if (flagno == 72) {   //SOA Report - Vendor
//                //accReportsControllerObj.getVendorLedger(request);
//            } else if (flagno == 8) {   //Bank Book
//                //accReportsService.getLedger(request);
//            } else if (flagno == 9) {   //Cash Book
//                //accReportsService.getLedger(request);
//            }
//        } catch (Exception ex) {
//            System.out.println("Error in : CommonFunctions.getFinancialReportDifference");
//            ex.printStackTrace();
//        }
//        return new ModelAndView("jsonView_ex", "model", msg);
//    }
     public ModelAndView onMigrateIndianGSTCopyAdvPayableAccountAndCreditNotAvailedAccountSetup(HttpServletRequest request, HttpServletResponse response) throws JSONException {
         JSONObject jobj = new JSONObject();
         boolean issuccess = false;
         try {
             System.out.println("IndianGST Copy Advance PayableAccount And Credit Not Availed Account Setup - Start");
             String subdomain = request.getParameter("subdomain");
             Map<String, Object> companyMap = new HashMap();
             if (!StringUtil.isNullOrEmpty(subdomain)) {
                 String[] subDomainArray = {subdomain};
                 companyMap.put("subdomains", subDomainArray);
             }

             KwlReturnObject companyResult = accCompanyPreferencesObj.getCompanyList(companyMap);
             List<String> ll = companyResult.getEntityList();
             for (String companyid : ll) {
                 KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                 Company company = (Company) returnObject.getEntityList().get(0);

                 HashMap<String, Object> defaultValueMap = new HashMap<String, Object>();
                 defaultValueMap.put("companyid", company.getCompanyID());
                 defaultValueMap.put("currencyid", company.getCurrency().getCurrencyID());

                 accAccountDAOobj.copyIndiaGSTTermsOnMigration(defaultValueMap);
                 System.out.println("------ IndianGST CopyAdvPayableAccount And CreditNotAvailedAccount Setup = " + company.getSubDomain());
                 issuccess = true;
             }
             System.out.println(" IndianGST Copy Advance Payable Account And Credit Not Availed Account Setup - Done");
         } catch (Exception e) {
             e.printStackTrace();
             Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, e);
         } finally {
             jobj.put("success", issuccess);
         }
         return new ModelAndView("jsonView_ex", "model", jobj.toString());
     }
     
    public ModelAndView copyTDSPayableAccountAndMapToMasterItemsNOP(HttpServletRequest request, HttpServletResponse response) throws JSONException{
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        try{
            System.out.println("Copy TDS Payable Account from Default Account and Map to Nature of Payment Master Item for India Country - Script start "+ System.currentTimeMillis());
            String subdomain = request.getParameter("subdomain");
            Map<String, Object> companyMap = new HashMap();
            if (!StringUtil.isNullOrEmpty(subdomain)) {
                String[] subDomainArray = {subdomain};
                companyMap.put("subdomains", subDomainArray);
            }
            // For only Indian country subdomains
            companyMap.put("country", "105");

            KwlReturnObject companyResult = accCompanyPreferencesObj.getCompanyList(companyMap);
            List<String> ll = companyResult.getEntityList();
            for (String companyid : ll) {
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                Company company = (Company) returnObject.getEntityList().get(0);
                
                HashMap<String, Object> defaultValueMap = new HashMap<String, Object>();
                defaultValueMap.put("companyid", company.getCompanyID());
                defaultValueMap.put("currencyid", company.getCurrency().getCurrencyID());
                accAccountDAOobj.copyTDSPayableAccountAndMapToMasterItemsNOP(defaultValueMap);
                System.out.println("Copy TDS Payable Account and map to Nature of Payment done for = " + company.getSubDomain());
                
                issuccess = true;
            }
            System.out.println("Copy TDS Payable Account from Default Account and Map to Nature of Payment Master Item for India Country - Script End "+ System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            jobj.put("success", issuccess);
        }
        
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
    
    /**
     * Method to post JournalEntryDetail for Non-Sale Product type. It is used
     * only if perpetual inventory is activated for the company.
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView postInventoryJEForNonSaleItem(HttpServletRequest request, HttpServletResponse response) {
        StringBuilder msg = new StringBuilder();
        try {
            String subdomains = request.getParameter("subdomain");
            Map<String, Object> requestMap = new HashMap<>();
            if (!StringUtil.isNullOrEmpty(subdomains)) {
                String[] subDomainArray = {subdomains};
                requestMap.put("subdomains", subDomainArray);
            }
            TransactionStatus status = null;
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("IC_Tx");
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            KwlReturnObject companyResultList = accCompanyPreferencesObj.getPerpetualInventoryActivatedCompanyList(requestMap);
            List companyList = companyResultList.getEntityList();
            for (Object obj : companyList) {
                Object[] objArr = (Object[]) obj;
                String companyID = (String) objArr[0];
                String subdomain = (String) objArr[1];
                String globalCurrency = (String) objArr[2];
                try {
                    List<String> grOrderIDs = accGoodsReceiptobj.getGoodsReceiptOrderWithNonSaleItem(companyID);
                    if (grOrderIDs!=null && !grOrderIDs.isEmpty()){
                        for (String  grOrderID : grOrderIDs) {
                            KwlReturnObject grOrderResult = kwlCommonTablesDAOObj.getObject(GoodsReceiptOrder.class.getName(), grOrderID);
                            if(grOrderResult!=null&& grOrderResult.getEntityList()!=null && !grOrderResult.getEntityList().isEmpty()){
                                GoodsReceiptOrder grOrder = (GoodsReceiptOrder)grOrderResult.getEntityList().get(0);
                                if(grOrder!=null){
                                    Set<GoodsReceiptOrderDetails> orderDetails = grOrder.getRows();
                                    boolean postInventoryJournalEntry = false;
                                    for (GoodsReceiptOrderDetails groDetail : orderDetails) {
                                        Product product = groDetail.getProduct();
                                        if (product != null && product.getProducttype() != null && !(product.getProducttype().getID().equals(Producttype.SERVICE) || product.getProducttype().getID().equals(Producttype.NON_INVENTORY_PART))) {
                                            postInventoryJournalEntry = true;
                                            break;
                                        }
                                    }
                                    if (grOrder.getInventoryJE() != null && postInventoryJournalEntry) {
                                        /**
                                         * JE is present we need to add non-sale
                                         * item JournalEntryDetail. More than
                                         * one product type is being used in GRN
                                         * for example inventory, inventory
                                         * assembly, inventory non-sale.
                                         */
                                        JournalEntry inventoryJE = grOrder.getInventoryJE();
                                        Set<JournalEntryDetail> jedetails = inventoryJE.getDetails();
                                        for (GoodsReceiptOrderDetails groDetail : orderDetails) {
                                            saveJournalEntryDetails(groDetail, jedetails, companyID, inventoryJE);
                                        }
                                        inventoryJE.setDetails(jedetails);
                                     } else if(postInventoryJournalEntry){
                                        /**
                                         * Create Inventory JE and add
                                         * JournalEntryDetail for inventory
                                         * non-sale item.
                                         */
                                        status = txnManager.getTransaction(def);
                                        HashMap<String, Object> JEFormatParams = new HashMap<>();
                                        JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                                        JEFormatParams.put("modulename", "autojournalentry");
                                        JEFormatParams.put("companyid", companyID);
                                        JEFormatParams.put("isdefaultFormat", true);
                                        KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                                        SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                                        Map<String, Object> seqNumberMap = new HashMap<>();
                                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyID, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, grOrder.getOrderDate());
                                        Map<String, Object> jeDataMap = new HashMap();
                                        jeDataMap.put(Constants.companyKey, companyID);
                                        JSONObject json = new JSONObject();
                                        json.put(Constants.companyKey, companyID);
                                        jeDataMap.put(Constants.globalCurrencyKey, globalCurrency);
                                        jeDataMap.put(Constants.df, authHandler.getDateOnlyFormat());
                                        jeDataMap.put(Constants.userdf, authHandler.getUserDateFormatterWithoutTimeZone(json));
                                        jeDataMap.put("entrynumber", (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER));
                                        jeDataMap.put("autogenerated", true);
                                        jeDataMap.put(Constants.SEQFORMAT, format.getID());
                                        jeDataMap.put(Constants.SEQNUMBER, (String) seqNumberMap.get(Constants.SEQNUMBER));
                                        jeDataMap.put(Constants.DATEPREFIX, (String) seqNumberMap.get(Constants.DATEPREFIX));
                                        jeDataMap.put(Constants.DATEAFTERPREFIX, (String) seqNumberMap.get(Constants.DATEAFTERPREFIX));
                                        jeDataMap.put(Constants.DATESUFFIX, (String) seqNumberMap.get(Constants.DATESUFFIX));
                                        jeDataMap.put("entrydate", grOrder.getOrderDate());
                                        jeDataMap.put("companyid", companyID);
                                        jeDataMap.put("createdby", grOrder.getCreatedby() != null ? grOrder.getCreatedby().getUserID() : "");
                                        jeDataMap.put("memo", grOrder.getMemo());
                                        jeDataMap.put("currencyid", grOrder.getCurrency().getCurrencyID());
                                        jeDataMap.put("costcenterid", grOrder.getCostcenter() != null ? grOrder.getCostcenter().getID() : "");
                                        jeDataMap.put("transactionModuleid", Constants.Acc_Goods_Receipt_ModuleId);
                                        jeDataMap.put("transactionId", grOrder.getID());
                                        jeDataMap.put(JournalEntryConstants.EXTERNALCURRENCYRATE, grOrder.getExternalCurrencyRate());
                                        KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap); // Create Journal entry without JEdetails
                                        JournalEntry inventoryJE = (JournalEntry) jeresult.getEntityList().get(0);
                                        Set<JournalEntryDetail> jedetails = new HashSet<>();
                                        for (GoodsReceiptOrderDetails orderDetail : orderDetails) {
                                            saveJournalEntryDetails(orderDetail, jedetails, companyID, inventoryJE);
                                        }
                                        grOrder.setInventoryJE(inventoryJE);
                                        int pendingApprovalFlag = (grOrder.getApprovestatuslevel() != 11) ? 1 : 0;
                                        inventoryJE.setPendingapproval(pendingApprovalFlag);
                                        inventoryJE.setDetails(jedetails);
                                        txnManager.commit(status);
                                        status = null;
                                    }
                                }
                            }
                        }
                    }
                    msg.append("</br> Subdomain:").append("<b>").append(subdomain).append("</b>").append(" Updated: ").append(grOrderIDs.size());
                } catch (Exception ex) {
                     if (status != null) {
                        txnManager.rollback(status);
                    }
                    status = null;
                    msg.append("</br> Subdomain:").append("<b>").append(subdomain).append("</b>").append(" Exception occured. ");
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(CommonFunctions.class.getName()).log(Level.WARNING,ex.getMessage());
//            System.out.println("------ Error at postInventoryJEForNonSaleItem() ----------");
            ex.printStackTrace();
        } finally {
            Logger.getLogger(CommonFunctions.class.getName()).log(Level.INFO, msg.toString());
//            System.out.println(msg);
        }
        return new ModelAndView("jsonView_ex", "model", msg.toString());
    }
    
    /**
     * Method is used to save JournalEntryDetails for Inventory Non-Sale item in
     * GRN. It is used if perpetual inventory valuation is activated for the
     * company.
     */
    private void saveJournalEntryDetails(GoodsReceiptOrderDetails grod, Set<JournalEntryDetail> jedetails, String companyid, JournalEntry inventoryJE) {
        Product product = grod.getProduct();
        try {
            if (product != null && product.getInventoryAccount() != null && product.getPurchaseAccount() != null && grod.getInventoryJEdetail() == null && grod.getPurchasesJEDetail() == null) {
                // Inventory Account
                JSONObject jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put("companyid", companyid);
                jedjson.put("amount", grod.getRate() * grod.getDeliveredQuantity());
                jedjson.put("accountid", product.getInventoryAccount() != null ? product.getInventoryAccount().getID() : "");
                jedjson.put("debit", true);
                jedjson.put("jeid", inventoryJE.getID());
                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                grod.setInventoryJEdetail(jed);
                jedetails.add(jed);
                // Accrued Purchase Account
                jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put("companyid", companyid);
                jedjson.put("amount", grod.getRate() * grod.getDeliveredQuantity());
                jedjson.put("accountid", product.getPurchaseAccount() != null ? product.getPurchaseAccount().getID() : "");
                jedjson.put("debit", false);
                jedjson.put("jeid", inventoryJE.getID());
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                grod.setPurchasesJEDetail(jed);
                jedetails.add(jed);
            }
        } catch (Exception ex) {
            Logger.getLogger(CommonFunctions.class.getName()).log(Level.WARNING, ex.getMessage());
        }
    }
    
    public ModelAndView insertInitialPurchasePrice(HttpServletRequest request, HttpServletResponse response) {
        StringBuilder msg = new StringBuilder();
        try {
            int companycount = 0;
            int failedcompanycount = 0;
            String subdomain = request.getParameter("subdomain");
            Map<String, Object> companyMap = new HashMap();
            if (!StringUtil.isNullOrEmpty(subdomain)) {
                String[] subDomainArray = {subdomain};
                companyMap.put("subdomains", subDomainArray);
            }
            KwlReturnObject companyResult = accCompanyPreferencesObj.getCompanyList(companyMap);
            List<String> companyIDList = companyResult.getEntityList();
            for (String companyid : companyIDList) {
                try {
                    accProductObj.insertInitialPurchasePrice(companyid);
                    companycount++;
//                    msg.append("</br>Company ID : ").append(companyid);
                } catch (Exception ex) {
                    failedcompanycount++;
//                    msg.append("</br>Exception Occured For Company: ").append(companyid);
                }
            }
            msg.append("\n Thanks !!!Script Executed Successfully.");
            msg.append("\n Success Company Count : ").append(companycount);
            msg.append(" Failure Company Count: ").append(failedcompanycount);
        } catch (Exception ex) {
            System.out.println("Exception occured-" + ex.getMessage());
        } finally {
            return new ModelAndView("jsonView_ex", "model", msg.toString());
        }
    }

    /**
     * This method is for updating all the landed cost JEs in a company for handling existing term amount cases and future uses 
     * @param request
     * @param response
     * @return
     * @throws JSONException 
     */
    public ModelAndView UpdateLandedCostJEs(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        int companycount = 0;
        int failedcompanycount = 0;
        StringBuilder msg = new StringBuilder();
        msg.append("<br><br><center>Execution Started @ ").append(new java.util.Date()).append("<br><br></center>");
        Map<String, Object> map = new HashMap<>();
        TransactionStatus status = null;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        try {

            String subdomain = request.getParameter("subdomain");
            String compid = request.getParameter("companyid");
            Map<String, Object> companyMap = new HashMap();
            if (!StringUtil.isNullOrEmpty(subdomain)) {
                String[] subDomainArray = {subdomain};
                companyMap.put("subdomains", subDomainArray);
            }
            KwlReturnObject companyResult = null;
            if (StringUtil.isNullOrEmpty(compid)) {
                companyResult = accCompanyPreferencesObj.getPerpetualInventoryActivatedCompanyList(companyMap);
            }
            List<Object> companyIDList = new ArrayList<>();
            if (companyResult != null) {
                companyIDList = companyResult.getEntityList();
            } else {
                companyIDList.add(compid);
            }
            for (Object comparr :companyIDList) {
                Object[] companyarray =(Object[]) comparr;
                String companyid =(String) (companyarray[0]!=null?companyarray[0] : "");
                String companyname = "";
                try {
                    map.put("companyId", companyid);
                    Company company = (Company) kwlCommonTablesDAOObj.getClassObject(Company.class.getName(), companyid);
                    KWLCurrency globalCurrency = company.getCurrency();
             
                    KwlReturnObject icomp = accountingHandlerDAOobj.getObject(IndiaComplianceCompanyPreferences.class.getName(), companyid);
                    IndiaComplianceCompanyPreferences complianceCompanyPreferences = (IndiaComplianceCompanyPreferences) icomp.getEntityList().get(0);
                    
                    KwlReturnObject ecpkwl = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
                    ExtraCompanyPreferences extracomppref = (ExtraCompanyPreferences) ecpkwl.getEntityList().get(0);

                    Map<String, Object> requestParams = new HashMap<>();
                    requestParams.put("gcurrencyid", company.getCurrency().getCurrencyID());
                    requestParams.put("companyid", companyid);
                    companyname = company.getSubDomain();
                    boolean isMalaysiaOrSingaporeCompany = false;
                    String countrycode = company.getCountry().getID();
                    if (countrycode.equalsIgnoreCase(String.valueOf(Constants.malaysian_country_id)) || countrycode.equalsIgnoreCase(String.valueOf(Constants.SINGAPOREID))) {
                        isMalaysiaOrSingaporeCompany = true;
                    }
                    KwlReturnObject kwlObj = accGoodsReceiptobj.getAllLandedInvoices(companyid);
                    if (kwlObj != null) {
                        List list = kwlObj.getEntityList();
                        Iterator itr = list.iterator();
                        while (itr.hasNext()) {
                            String oldLandedInvoiceJEId;                            
                            Set<JournalEntryDetail> newjedetails = new HashSet<>();

                            double totalWeight = 0.0, totalQuantity = 0.0, totalCost = 0.0;
                            double totaltermamount = 0.0;
                            double totaldiscount = 0.0;
                            double lineleveltaxamount = 0.0;
                            GoodsReceipt gr = (GoodsReceipt) itr.next();
                            JournalEntry je = gr.getLandedInvoiceJE();
                            String landedcostcategory = "";
                            JournalEntry ljetemp = je;
                            if (ljetemp != null) {
                                oldLandedInvoiceJEId = ljetemp.getID(); //old invoice id 
                                LandingCostCategory landedCostCategory = null;
                                if (gr.getLandingCostCategory() != null) {
                                    landedcostcategory = (!StringUtil.equalIgnoreCase(gr.getLandingCostCategory().getLccName(), "NOT APPLICABLE")) ? gr.getLandingCostCategory().getLccName() : "";
                                    landedCostCategory = gr.getLandingCostCategory();
                                }
                                //iterate all the expense invoice details and recreate the landed cost JE
                                for (ExpenseGRDetail expgrd : gr.getExpenserows()) {
                                    /**
                                     * JournalEntryDetail to be added for account used in expense account add
                                     * JournalEntryDetail only if landedCostCategory and perpetual  inventory valuation is activated at
                                     * company level.
                                     */
                                    totaltermamount += expgrd.getLineLevelTermAmount();
                                    //line level tax accounts to be included/excluded in landed cost JE
                                    if (expgrd.getTax() != null && isMalaysiaOrSingaporeCompany) {
                                        if (expgrd.getTax().isInputCredit()) {
                                            JSONObject ljedjson = new JSONObject();
                                            ljedjson.put(SRNO, newjedetails.size() + 1);
                                            ljedjson.put(Constants.companyid, company.getCompanyID());
                                            ljedjson.put(AMOUNT, expgrd.getRowTaxAmount());
                                            ljedjson.put(ACCOUNTID, expgrd.getTax().getAccount().getID());
                                            ljedjson.put(DEBIT, !expgrd.isIsdebit());
                                            ljedjson.put(JEID, oldLandedInvoiceJEId);
                                            ljedjson.put(DESCRIPTION, expgrd.getDescription());
                                            KwlReturnObject ljedresult = accJournalEntryobj.addJournalEntryDetails(ljedjson);
                                            JournalEntryDetail ljed = (JournalEntryDetail) ljedresult.getEntityList().get(0);
                                            newjedetails.add(ljed);
                                        } else {
                                            if (expgrd.isIsdebit()) {
                                                lineleveltaxamount += expgrd.getRowTaxAmount();
                                            } else {
                                                lineleveltaxamount -= expgrd.getRowTaxAmount();
                                            }
                                        }
                                    }                                     

                                    if ((landedcostcategory == null || (LandingCostAllocationType.getByValue(landedCostCategory.getLcallocationid()) != LandingCostAllocationType.CUSTOMDUTY)) || (landedCostCategory != null && LandingCostAllocationType.getByValue(landedCostCategory.getLcallocationid()) == LandingCostAllocationType.CUSTOMDUTY && !StringUtil.isNullOrEmpty(complianceCompanyPreferences.getIGSTAccount()) && !expgrd.getAccount().getID().equals(complianceCompanyPreferences.getIGSTAccount()))) {
                                        /**
                                         * Include Expense invoice account in landed cost calculation only in
                                         * following cases: 1.landedCostCategory is NULL 2.landedCostCategory is other than CUSTOM_DUTY allocation ID
                                         * 3.landedCostCategory is CUSTOM_DUTY(Only for Indian Countries) then it should not include
                                         * IGST account in landed cost JE.
                                         */
                                        JSONObject ljedjson = new JSONObject();
                                        double landedcosttax = isMalaysiaOrSingaporeCompany ? gr.isGstIncluded() ? expgrd.getRowTaxAmount() : 0.0 : expgrd.getRowTaxAmount(); //ERM-971 for malaysia/singapore companies tax is based in inputcredit check
                                        ljedjson.put(SRNO, newjedetails.size() + 1);
                                        ljedjson.put(Constants.companyid, company.getCompanyID());
                                        if (!gr.isGstIncluded() && expgrd.getTax() == null) {
                                            ljedjson.put(AMOUNT, expgrd.getRate() - totaldiscount - totaltermamount);
                                        } else {
                                            ljedjson.put(AMOUNT, expgrd.getRate() - totaldiscount - landedcosttax - totaltermamount);
                                        }
                                        ljedjson.put(ACCOUNTID, expgrd.getAccount().getID());
                                        ljedjson.put(DEBIT, !expgrd.isIsdebit());
                                        ljedjson.put(JEID, oldLandedInvoiceJEId);
                                        ljedjson.put(DESCRIPTION, expgrd.getDescription());
                                        KwlReturnObject ljedresult = accJournalEntryobj.addJournalEntryDetails(ljedjson);
                                        JournalEntryDetail ljed = (JournalEntryDetail) ljedresult.getEntityList().get(0);
                                        newjedetails.add(ljed);
                                    }
                                } //Expense Invoice detail loop ends

                                    /**
                                     * Manual/Custom Duty Landed Cost Category Block.
                                     */
                                    if (landedCostCategory != null && LandingCostAllocationType.getByValue(landedCostCategory.getLcallocationid()) == (LandingCostAllocationType.MANUAL) || (LandingCostAllocationType.getByValue(landedCostCategory.getLcallocationid()) == LandingCostAllocationType.CUSTOMDUTY)) {
                                        Set<LccManualWiseProductAmount> manualamtset = gr.getLccmanualwiseproductamount() !=null ? gr.getLccmanualwiseproductamount(): new HashSet<LccManualWiseProductAmount>();
                                        for(LccManualWiseProductAmount lccm:manualamtset){
                                            if (lccm != null) {
                                                Product product = lccm.getGrdetailid().getInventory().getProduct();

                                                if (product != null && product.getInventoryAccount() == null && !product.isAsset()) {
                                                    throw new AccountingException("Inventory Account is not mapped for product: " + product.getProductid());
                                                }
                                                if ((!product.isAsset() && product.getInventoryAccount() != null) || (product.isAsset() && product.getPurchaseAccount() != null)) {
                                                    JSONObject ljedjson = new JSONObject();
                                                    ljedjson.put(SRNO, newjedetails.size() + 1);
                                                    ljedjson.put(COMPANYID, company.getCompanyID());
                                                    if (LandingCostAllocationType.getByValue(landedCostCategory.getLcallocationid()) == LandingCostAllocationType.CUSTOMDUTY) {
                                                        ljedjson.put(AMOUNT, lccm.getCustomdutyandothercharges());
                                                    } else {
                                                        ljedjson.put(AMOUNT, lccm.getAmount());
                                                    }
                                                    if (product.isAsset()) {
                                                        ljedjson.put(ACCOUNTID, product.getPurchaseAccount().getID());
                                                    } else {
                                                        ljedjson.put(ACCOUNTID, product.getInventoryAccount().getID());
                                                    }
                                                    ljedjson.put(DEBIT, true);
                                                    ljedjson.put(JEID, oldLandedInvoiceJEId);
                                                    KwlReturnObject ljedresult = accJournalEntryobj.addJournalEntryDetails(ljedjson);
                                                    JournalEntryDetail ljed = (JournalEntryDetail) ljedresult.getEntityList().get(0);
                                                    newjedetails.add(ljed);

                                                    JSONObject landingCostJSON = new JSONObject();
                                                    landingCostJSON.put(LandingCostDetailMapping.LANDING_CATEGORY_ID, landedcostcategory);
                                                    landingCostJSON.put(LandingCostDetailMapping.INVENTORY_JED, ljed.getID());
                                                    landingCostJSON.put(LandingCostDetailMapping.GOODSRECEIPT_DETAIL_ID, lccm.getGrdetailid().getID());
                                                    landingCostJSON.put(LandingCostDetailMapping.EXPENSE_INVOICE_ID, lccm.getExpenseInvoiceid().getID());
                                                    landingCostJSON.put(LandingCostDetailMapping.LANDING_COST, lccm.getAmount());
                                                    accGoodsReceiptobj.saveLandingCostDetailMapping(landingCostJSON);
                                                }
                                            }
                                        }
                                    }//Manual/Custom Duty landed cost block ends
                                
                                //Manual/Custom Duty landed cost categories block begins 
                                if (LandingCostAllocationType.getByValue(landedCostCategory.getLcallocationid()) != LandingCostAllocationType.MANUAL && LandingCostAllocationType.getByValue(landedCostCategory.getLcallocationid()) != LandingCostAllocationType.CUSTOMDUTY){
                                //calculating details of all products in the linked procurement invoices of current expense invoice              
                                for (GoodsReceipt landingGoodsReceipt : gr.getLandedInvoice()) {
                                    KwlReturnObject grodrs = accGoodsReceiptobj.getGoodsReceiptDetailForLandingCategory(landingGoodsReceipt.getID(), landedCostCategory.getId());
                                    List grodList = grodrs.getEntityList();
                                    if (grodList != null && !grodList.isEmpty()) {
                                        for (Object object : grodList) {
                                            Object[] objArr = (Object[]) object;
                                            double rate = (Double) objArr[1];
                                            double quantity = (Double) objArr[2];
                                            double productWeight = (Double) objArr[3];
                                            KwlReturnObject resultBR = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, rate, landingGoodsReceipt.getCurrency().getCurrencyID(), landingGoodsReceipt.getCreationDate(), landingGoodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                            double baseRate = authHandler.roundUnitPrice((Double) resultBR.getEntityList().get(0), companyid);
                                            totalWeight += (quantity * productWeight);
                                            totalQuantity += quantity;
                                            totalCost += (quantity * baseRate);
                                        }
                                    }
                                }
                                double landingCosttax = 0.0;
                                double termtotalamount = 0.0;
                                //to calculate line level/global level tax in the invoice and reduce tax amount from JE 2 of landed cost 
                                Map<String, Object> taxmap = accGoodsReceiptobj.getGlobalandLineLevelTaxForGoodsReceipt(gr.getID(), companyid);
                                if (taxmap.containsKey("lineleveltax")) {
                                    double lineleveltax = 0;
                                    List<Object> lineleveltaxlist = (ArrayList) taxmap.get("lineleveltax");
                                    for (Object arr : lineleveltaxlist) {
                                        String grdid = arr != null ? arr.toString() : "";
                                        KwlReturnObject expkwl = kwlCommonTablesDAOObj.getObject(ExpenseGRDetail.class.getName(), grdid);
                                        ExpenseGRDetail expgrd = (ExpenseGRDetail) (expkwl.getEntityList().isEmpty() ? null : expkwl.getEntityList().get(0));
                                        if (expgrd != null) {
                                            if (!isMalaysiaOrSingaporeCompany || (isMalaysiaOrSingaporeCompany && expgrd.isIsdebit())) {
                                                lineleveltax += expgrd.getRowTaxAmount();
                                            } else if (isMalaysiaOrSingaporeCompany && !expgrd.isIsdebit()) { //IF malaysia/singapore company Expense Invoice has credit type account with landed cost tax not included
                                                lineleveltax -= expgrd.getRowTaxAmount();
                                            }
                                        }
                                    }
                                    //calculating taxes with input credit not available and reducing this amount from invoice cost
                                    landingCosttax += lineleveltax;
                                }
                                
                                //handling term amount with separate account for landed cost JE 
                                if(extracomppref.islandedcosttermJE()){
                                        HashMap<String,Object> termparams = new HashMap<>();
                                        termparams.put("invoiceid",gr.getID());
                                        KwlReturnObject termkwl = accGoodsReceiptobj.getInvoiceTermMap(termparams); //get invoice terms first
                                        if(termkwl!=null && !termkwl.getEntityList().isEmpty()){
                                            HashMap<String, Double> termAcc = new HashMap<>();
                                            List<ReceiptTermsMap> termlist = termkwl.getEntityList();
                                            for (ReceiptTermsMap receipttermmap : termlist) { //create map of terms based on their accounts
                                                double termamount = receipttermmap.getTermamount();
                                                if (termAcc.containsKey(receipttermmap.getTerm().getAccount().getID())) {
                                                    double tempAmount = termAcc.get(receipttermmap.getTerm().getAccount().getID());
                                                    termAcc.put(receipttermmap.getTerm().getAccount().getID(), termamount + tempAmount);
                                                } else {
                                                    termAcc.put(receipttermmap.getTerm().getAccount().getID(), termamount);
                                                }
                                            }
                                            for (Map.Entry<String, Double> entry : termAcc.entrySet()) { //update landed cost jedetail based on above termmap
                                                JSONObject ljedjson = new JSONObject();
                                                ljedjson.put("srno", newjedetails.size() + 1);
                                                ljedjson.put("companyid", company.getCompanyID());
                                                ljedjson.put("amount", entry.getValue() > 0 ? entry.getValue() : (entry.getValue() * (-1)));
                                                ljedjson.put("accountid", entry.getKey());
                                                ljedjson.put("debit", !(entry.getValue() > 0)); //this is a reverse JE hence conditions are reverse too
                                                ljedjson.put("jeid", oldLandedInvoiceJEId);
                                                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(ljedjson);
                                                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                                newjedetails.add(jed);
                                            }
                                        }
                                    }                                
                                if (gr.getTax() != null) { //global level tax reduce only if it is not included in landed cost
                                    if (!isMalaysiaOrSingaporeCompany || !gr.getTax().isInputCredit()) {
                                        landingCosttax += gr.getTaxamount();
                                    } else if ((isMalaysiaOrSingaporeCompany && gr.getTax().isInputCredit())) { 
                                        JSONObject ljedjson = new JSONObject();
                                        ljedjson.put(SRNO, newjedetails.size() + 1);
                                        ljedjson.put(Constants.companyid, company.getCompanyID());
                                        ljedjson.put(AMOUNT, gr.getTaxamount());
                                        ljedjson.put(ACCOUNTID, gr.getTax().getAccount().getID());
                                        ljedjson.put(DEBIT, false);
                                        ljedjson.put(JEID, oldLandedInvoiceJEId);
                                        KwlReturnObject ljedresult = accJournalEntryobj.addJournalEntryDetails(ljedjson);
                                        JournalEntryDetail ljed = (JournalEntryDetail) ljedresult.getEntityList().get(0);
                                        newjedetails.add(ljed);
                                    }
                                }
                                for (GoodsReceipt landingGoodsReceipt : gr.getLandedInvoice()) {
                                    KwlReturnObject grodResult = accGoodsReceiptobj.getGoodsReceiptDetailForLandingCategory(landingGoodsReceipt.getID(), landedCostCategory.getId());
                                    List groDetailList = grodResult.getEntityList();
                                    String procurementCurrency = landingGoodsReceipt.getCurrency() != null ? landingGoodsReceipt.getCurrency().getCurrencyID() : "";

                                    if (groDetailList != null && !groDetailList.isEmpty()) {
                                        for (Object object : groDetailList) {
                                            Object[] objArr = (Object[]) object;
                                            String groID = (String) (objArr[0] != null ? objArr[0] : "");
                                            String productId = (String) (objArr[4] != null ? objArr[4] : "");
                                            double landingCost = 0;
                                            KwlReturnObject productResult = accountingHandlerDAOobj.getObject(Product.class.getName(), productId);
                                            Product product = (productResult.getEntityList() != null && !productResult.getEntityList().isEmpty()) ? (Product) productResult.getEntityList().get(0) : null;
                                            if (product != null) {
                                                if (product.getInventoryAccount() == null) {
                                                    throw new AccountingException("Inventory Account is not mapped for product: " + product.getProductid());
                                                }
                                                double rate = (Double) objArr[1];
                                                double quantity = (Double) objArr[2];
                                                double productWeight = (Double) objArr[3];
                                                double baseRate = rate;
                                                double productlandedcostinJE=0.0d;
                                                if (globalCurrency != null && !globalCurrency.getCurrencyID().equals(procurementCurrency)) {
                                                    KwlReturnObject ruternBR = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, rate, procurementCurrency, landingGoodsReceipt.getCreationDate(), landingGoodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                                    baseRate = authHandler.roundUnitPrice((Double) ruternBR.getEntityList().get(0), companyid);
                                                }                                                
                                                double totalvalue = authHandler.roundUnitPrice(quantity * baseRate, companyid);
                                                
                                                /**
                                                 * SDP-15928(For perpetual JE case only) For adjustment of the rounding difference we
                                                 * are checking if this product has the rounding value adjusted during JE posting.
                                                 */
                                                HashMap<String, Object> lcdmparams = new HashMap<>();
                                                lcdmparams.put("grdetailid", groID);
                                                KwlReturnObject mappingkwl = accGoodsReceiptobj.getLandingCostDetailMapping(lcdmparams);
                                                Object[] mappingobj = mappingkwl.getEntityList().isEmpty() ? null : (Object[]) mappingkwl.getEntityList().get(0);
                                                String landingcostmappingid = (mappingobj[0] != null ? mappingobj[0].toString() : " "); //landingcostmapping table id

                                                mappingkwl = accountingHandlerDAOobj.getObject(LandingCostDetailMapping.class.getName(), landingcostmappingid);
                                                LandingCostDetailMapping lcmappingobj = (LandingCostDetailMapping) mappingkwl.getEntityList().get(0);
                                                productlandedcostinJE = lcmappingobj!=null ? lcmappingobj.getInventoryJED().getAmountinbase() : 0.0d ;
                                                
                                                //Get amount from Invoice Terms of Expense Invoice and exclude this from landed cost depending on the check in company preferences
                                                if (!extracomppref.islandedcosttermJE()) {
                                                    HashMap<String, Object> termParams = new HashMap();
                                                    termParams.put("invoiceid", gr.getID());
                                                    KwlReturnObject invoicetermkwl = accGoodsReceiptobj.getInvoiceTermMap(termParams);
                                                    List<ReceiptTermsMap> invoicetermlist = invoicetermkwl != null ? invoicetermkwl.getEntityList() : null;
                                                    for (ReceiptTermsMap termmap : invoicetermlist) {
                                                        if (termmap != null) {
                                                            termtotalamount += termmap.getTermamount();
                                                        }
                                                    }
                                                    termtotalamount = (termtotalamount > 0 ? 0 : termtotalamount);
                                                }
                                                switch (LandingCostAllocationType.getByValue(landedCostCategory.getLcallocationid())) {
                                                    case QUANTITY:
                                                    case VALUE:
                                                    case WEIGHT:
                                                        Map<String, Double> allcactionMthdData = new HashMap<>();
                                                        //Reducing term/tax amount as tax is based on input credit flag for malaysia/singapore and term is directly reduced from invoice cost
                                                        allcactionMthdData.put("totLandedCost", gr.getInvoiceAmount() - landingCosttax - termtotalamount);
                                                        allcactionMthdData.put("noEligiableItem", totalQuantity);
                                                        allcactionMthdData.put("lineItemQty", quantity);
                                                        allcactionMthdData.put("valueOfItem", totalvalue);
                                                        allcactionMthdData.put("eligiableItemCost", totalCost);
                                                        allcactionMthdData.put("eligiableItemWgt", totalWeight);
                                                        allcactionMthdData.put("itemWght", (productWeight * quantity));
                                                        allcactionMthdData.put("manualProductAmount", 0.0);
                                                        landingCost = LandingCostAllocationType.getTotalLanddedCost(landedCostCategory.getLcallocationid(), allcactionMthdData);
                                                        
                                                       /**
                                                        * SDP-15928(Perpetual Only) Check in the landed cost JE if the posted
                                                        * amount is the same as the one processed here to check if this is the product where
                                                        * rounding difference has been adjusted.
                                                        */
                                                        if (productlandedcostinJE != 0.0) {
                                                            double roundedlc = authHandler.round(landingCost, companyid);
                                                            if (productlandedcostinJE != roundedlc) {
                                                                double roundingdiff = Math.abs(productlandedcostinJE - roundedlc);
                                                                landingCost += roundingdiff;
                                                            }
                                                        }                                                        
                                                        landingCost = authHandler.roundUnitPrice(landingCost, companyid);
                                                        break;
                                                    case CUSTOMDUTY:
                                                        break;
                                                    case MANUAL:
                                                        /**
                                                         * Handled separately while saving LccManualWiseProductAmount.
                                                         */
                                                        break;
                                                }
                                                /**
                                                 * Add JournalEntryDetail against Product's Inventory Account.
                                                 */
                                                double landingCostInTransactionCurrency = landingCost;

                                                JSONObject ljedjson = new JSONObject();
                                                ljedjson.put(SRNO, newjedetails.size() + 1);
                                                ljedjson.put(Constants.companyid, company.getCompanyID());
                                                ljedjson.put(AMOUNT, landingCostInTransactionCurrency);
                                                ljedjson.put(ACCOUNTID, product.getInventoryAccount().getID());
                                                ljedjson.put(DEBIT, true);
                                                ljedjson.put(JEID, oldLandedInvoiceJEId);
                                                KwlReturnObject ljedresult = accJournalEntryobj.addJournalEntryDetails(ljedjson);
                                                JournalEntryDetail ljed = (JournalEntryDetail) ljedresult.getEntityList().get(0);
                                                newjedetails.add(ljed);


                                                /**
                                                 * Save LandingcostDetail Mapping entries.
                                                 */
                                                JSONObject landingCostJSON = new JSONObject();
                                                landingCostJSON.put(LandingCostDetailMapping.LANDING_CATEGORY_ID, landedCostCategory.getId());
                                                landingCostJSON.put(LandingCostDetailMapping.INVENTORY_JED, ljed.getID());
                                                landingCostJSON.put(LandingCostDetailMapping.GOODSRECEIPT_DETAIL_ID, groID);
                                                landingCostJSON.put(LandingCostDetailMapping.EXPENSE_INVOICE_ID, gr.getID());
                                                landingCostJSON.put(LandingCostDetailMapping.LANDING_COST, landingCostInTransactionCurrency);
                                                accGoodsReceiptobj.saveLandingCostDetailMapping(landingCostJSON);
                                            }
                                        } // PI Detail loop ends
                                    }
                                } // PI loop ends
                            } // non manual landed cost category block ends
                                
                                status = txnManager.getTransaction(def);
                                
                                //deleting landingcost detail mapping table entries
                                accGoodsReceiptobj.deleteLandingCostDetailMapping(gr.getID(), companyid, gr.isIsExpenseType());
                                
                                //delete existing JE details entry from DB by calling clear() method on its set
                                ljetemp.getDetails().clear();

                                //set new updated JE details only by using addAll method of existing details
                                ljetemp.getDetails().addAll(newjedetails);
                                txnManager.commit(status);
                            }
                        } //Expense Invoice Loop ends
                    }
                    companycount++;
                } catch (Exception ex) {
                    failedcompanycount++;
                    msg.append("<center></br>Exception Occured For Subdomain: ").append(companyname).append("</center>");                    
                }
            }//Company Loop ends 
        } catch (Exception ex) {
            System.out.println("Exception occured-" + ex.getMessage());
            if (status != null) {
                txnManager.rollback(status);
            }
        } finally {
            msg.append("\n <center><br><b>Script execution complete.</b>");
            msg.append("\n <b><br>Success Company Count :</b> ").append(companycount);
            msg.append("\n <b><br>Failure Company Count :</b> ").append(failedcompanycount);
            msg.append("<br><br><center>Execution Ended @ ").append(new java.util.Date()).append("</center>");
        }

        return new ModelAndView("jsonView_ex", "model", msg.toString());

    }
    
    /**
     * This method is to update Global level transaction of CQ,VQ,SO,PO,DO and GRO where invoice term is used.
     * @param request
     * @param response
     * @return
     * @throws JSONException 
     */
    public ModelAndView updateGlobalLevelTransactionsOfInvoiceTerm(HttpServletRequest request, HttpServletResponse response) {
        StringBuilder msg = new StringBuilder();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("USIJE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        try {
            String companyid = request.getParameter("companyid");
            String subdomain = request.getParameter("subdomain");
            HashMap<String, Object> globalParams = new HashMap<String, Object>();
            globalParams.put("companyid", companyid);
            KwlReturnObject bAmt = null;
            double externalCurrencyRate = 0d;
            Date creationDate = null;
            String fromcurrencyid ="";
            int updatedcountCQTerm=0,updatedcountVQTerm=0,updatedcountSOTerm=0,updatedcountPOTerm=0,updatedcountDOTerm=0,updatedcountGROTerm=0;
            
            
            if (!StringUtil.isNullOrEmpty(companyid)) {
                // Update Global level transaction of Customer Quotaion
                KwlReturnObject result1 = accSalesOrderDAOobj.getAllGlobalQuotaionsOfInvoiceTerms(globalParams);
                List globalQuotationList = result1.getEntityList();
                for (Object object : globalQuotationList) {
                    Object[] objArr = (Object[]) object;
                    HashMap<String, Object> termMap = new HashMap<String, Object>();
                    
                    String quotationId = (String) (objArr[0] != null ? objArr[0] : "");
                    String quotationTermId = (String) (objArr[1] != null ? objArr[1] : "");
                    String taxid = (String) (objArr[2] != null ? objArr[2] : "");
                    double termAmount = (double) (objArr[3] != null ? objArr[3] : 0);
                    double termTaxPercent = (double) (objArr[4] != null ? objArr[4] : 0);
                    String termId = (String) (objArr[5] != null ? objArr[5] : "");
                    
                    KwlReturnObject quotationResult = kwlCommonTablesDAOObj.getObject(Quotation.class.getName(), quotationId);
                    if(quotationResult!=null&& quotationResult.getEntityList()!=null && !quotationResult.getEntityList().isEmpty()){
                        Quotation quotation = (Quotation)quotationResult.getEntityList().get(0);
                        HashMap<String, Object> requestParams = new HashMap();
                        HashMap<String, Object> filterrequestParams = new HashMap();
                        double termAmountInBase = 0,termtaxamount=0,termtaxamountinbase=0;
                        
                        requestParams.put("gcurrencyid", quotation.getCompany().getCurrency().getCurrencyID());
                        requestParams.put("companyid", quotation.getCompany().getCompanyID());
                        
                        fromcurrencyid = quotation.getCurrency().getCurrencyID();
                        externalCurrencyRate = quotation.getExternalCurrencyRate();
                        creationDate = quotation.getQuotationDate();
                        
                        filterrequestParams.put("taxid",taxid);
                        filterrequestParams.put("term",termId);
                        
                        boolean isTermMappedwithTax = accTaxObj.isTermMappedwithTax(filterrequestParams);
                        
                        termMap.put("quotationtermid", quotationTermId);
                        
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, termAmount, fromcurrencyid, creationDate, externalCurrencyRate);
                        termAmountInBase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                        
                        termMap.put("termamountinbase", termAmountInBase);
                        termMap.put("termAmountExcludingTax", termAmount);
                        termMap.put("termAmountExcludingTaxInBase", termAmountInBase);
                        
                        if (isTermMappedwithTax) {
                            termtaxamount = authHandler.round(termAmount * (termTaxPercent / 100), companyid);
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, termtaxamount, fromcurrencyid, creationDate, externalCurrencyRate);
                            termtaxamountinbase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                            termMap.put("termtax", taxid);
                            termMap.put("termtaxamount", termtaxamount);
                            termMap.put("termtaxamountinbase", termtaxamountinbase);
                        }
                        status= txnManager.getTransaction(def);
                        
                        accSalesOrderDAOobj.updateQuotationTermMap(termMap);
                        
                        txnManager.commit(status);
                        status = null;
                        updatedcountCQTerm++;
                        msg.append("<center></br> Subdomain : ").append("<b>").append(subdomain).append("</b>").append(" Updated Customer Quotation Number : ").append("<b>").append(quotation.getquotationNumber()).append("</b>").append("</center>");
                    }
                }
                // Update Global level transaction of Vendor Quotaion
                KwlReturnObject result2 = accPurchaseOrderobj.getAllGlobalVendorQuotaionsOfInvoiceTerms(globalParams);
                List globalVendorQuotationList = result2.getEntityList();
                for (Object object : globalVendorQuotationList) {
                    Object[] objArr = (Object[]) object;
                    HashMap<String, Object> termMap = new HashMap<String, Object>();
                    
                    String quotationId = (String) (objArr[0] != null ? objArr[0] : "");
                    String quotationTermId = (String) (objArr[1] != null ? objArr[1] : "");
                    String taxid = (String) (objArr[2] != null ? objArr[2] : "");
                    double termAmount = (double) (objArr[3] != null ? objArr[3] : 0);
                    double termTaxPercent = (double) (objArr[4] != null ? objArr[4] : 0);
                    String termId = (String) (objArr[5] != null ? objArr[5] : "");
                    
                    KwlReturnObject quotationResult = kwlCommonTablesDAOObj.getObject(VendorQuotation.class.getName(), quotationId);
                    if(quotationResult!=null&& quotationResult.getEntityList()!=null && !quotationResult.getEntityList().isEmpty()){
                        VendorQuotation quotation = (VendorQuotation)quotationResult.getEntityList().get(0);
                        HashMap<String, Object> requestParams = new HashMap();
                        HashMap<String, Object> filterrequestParams = new HashMap();
                        double termAmountInBase = 0,termtaxamount=0,termtaxamountinbase=0;
                        
                        requestParams.put("gcurrencyid", quotation.getCompany().getCurrency().getCurrencyID());
                        requestParams.put("companyid", quotation.getCompany().getCompanyID());
                        
                        fromcurrencyid = quotation.getCurrency().getCurrencyID();
                        externalCurrencyRate = quotation.getExternalCurrencyRate();
                        creationDate = quotation.getQuotationDate();
                        
                        filterrequestParams.put("taxid",taxid);
                        filterrequestParams.put("term",termId);
                        
                        boolean isTermMappedwithTax = accTaxObj.isTermMappedwithTax(filterrequestParams);
                        
                        termMap.put("quotationtermid", quotationTermId);
                        
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, termAmount, fromcurrencyid, creationDate, externalCurrencyRate);
                        termAmountInBase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                        termMap.put("termamountinbase", termAmountInBase);
                        
                        termMap.put("termAmountExcludingTax", termAmount);
                        termMap.put("termAmountExcludingTaxInBase", termAmountInBase);
                        
                        if (isTermMappedwithTax) {
                            termtaxamount = authHandler.round(termAmount * (termTaxPercent / 100), companyid);
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, termtaxamount, fromcurrencyid, creationDate, externalCurrencyRate);
                            termtaxamountinbase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                            termMap.put("termtax", taxid);
                            termMap.put("termtaxamount", termtaxamount);
                            termMap.put("termtaxamountinbase", termtaxamountinbase);
                        }
                        
                        status= txnManager.getTransaction(def);
                        
                        accPurchaseOrderobj.updateVendorQuotationTermMap(termMap);
                        
                        txnManager.commit(status);
                        status = null;
                        updatedcountVQTerm++;
                        msg.append("<center></br> Subdomain : ").append("<b>").append(subdomain).append("</b>").append(" Updated Vendor Quotation Number : ").append("<b>").append(quotation.getQuotationNumber()).append("</b>").append("</center>");
                    }
                }

                // Update Global level transaction of Sales Order
                KwlReturnObject result3 = accSalesOrderDAOobj.getAllGlobalSalesOrderOfInvoiceTerms(globalParams);
                List globalSOList = result3.getEntityList();
                for (Object object : globalSOList) {
                    Object[] objArr = (Object[]) object;
                    HashMap<String, Object> termMap = new HashMap<String, Object>();
                    
                    String orderId = (String) (objArr[0] != null ? objArr[0] : "");
                    String orderTermId = (String) (objArr[1] != null ? objArr[1] : "");
                    String taxid = (String) (objArr[2] != null ? objArr[2] : "");
                    double termAmount = (double) (objArr[3] != null ? objArr[3] : 0);
                    double termTaxPercent = (double) (objArr[4] != null ? objArr[4] : 0);
                    String termId = (String) (objArr[5] != null ? objArr[5] : "");
                    
                    KwlReturnObject orderResult = kwlCommonTablesDAOObj.getObject(SalesOrder.class.getName(), orderId);
                    if(orderResult!=null&& orderResult.getEntityList()!=null && !orderResult.getEntityList().isEmpty()){
                        SalesOrder order = (SalesOrder)orderResult.getEntityList().get(0);
                        HashMap<String, Object> requestParams = new HashMap();
                        HashMap<String, Object> filterrequestParams = new HashMap();
                        double termAmountInBase = 0,termtaxamount=0,termtaxamountinbase=0;
                        
                        requestParams.put("gcurrencyid", order.getCompany().getCurrency().getCurrencyID());
                        requestParams.put("companyid", order.getCompany().getCompanyID());
                        
                        fromcurrencyid = order.getCurrency().getCurrencyID();
                        externalCurrencyRate = order.getExternalCurrencyRate();
                        creationDate = order.getOrderDate();
                        
                        filterrequestParams.put("taxid",taxid);
                        filterrequestParams.put("term",termId);
                        
                        boolean isTermMappedwithTax = accTaxObj.isTermMappedwithTax(filterrequestParams);
                        
                        termMap.put("ordertermid", orderTermId);
                        
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, termAmount, fromcurrencyid, creationDate, externalCurrencyRate);
                        termAmountInBase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                        termMap.put("termamountinbase", termAmountInBase);
                        
                        termMap.put("termAmountExcludingTax", termAmount);
                        termMap.put("termAmountExcludingTaxInBase", termAmountInBase);
                        
                        if(isTermMappedwithTax){
                            termtaxamount=authHandler.round(termAmount * (termTaxPercent/100), companyid);
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, termtaxamount, fromcurrencyid, creationDate, externalCurrencyRate);
                            termtaxamountinbase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                            termMap.put("termtax", taxid);
                            termMap.put("termtaxamount", termtaxamount);
                            termMap.put("termtaxamountinbase", termtaxamountinbase);
                        }
                        status= txnManager.getTransaction(def);
                        
                        accSalesOrderDAOobj.updateSalesOrderTermMap(termMap);
                        
                        txnManager.commit(status);
                        status = null;
                        updatedcountSOTerm++;
                        msg.append("<center></br> Subdomain : ").append("<b>").append(subdomain).append("</b>").append(" Updated Sales Order Number : ").append("<b>").append(order.getSalesOrderNumber()).append("</b>").append("</center>");
                    }
                }
                
                // Update Global level transaction of Purchase Order
                KwlReturnObject result4 = accPurchaseOrderobj.getAllGlobalPurcahseOrderOfInvoiceTerms(globalParams);
                List globalPOList = result4.getEntityList();
                for (Object object : globalPOList) {
                    Object[] objArr = (Object[]) object;
                    HashMap<String, Object> termMap = new HashMap<String, Object>();
                    
                    String orderId = (String) (objArr[0] != null ? objArr[0] : "");
                    String orderTermId = (String) (objArr[1] != null ? objArr[1] : "");
                    String taxid = (String) (objArr[2] != null ? objArr[2] : "");
                    double termAmount = (double) (objArr[3] != null ? objArr[3] : 0);
                    double termTaxPercent = (double) (objArr[4] != null ? objArr[4] : 0);
                    String termId = (String) (objArr[5] != null ? objArr[5] : "");
                    
                    KwlReturnObject orderResult = kwlCommonTablesDAOObj.getObject(PurchaseOrder.class.getName(), orderId);
                    if(orderResult!=null&& orderResult.getEntityList()!=null && !orderResult.getEntityList().isEmpty()){
                        PurchaseOrder order = (PurchaseOrder)orderResult.getEntityList().get(0);
                        HashMap<String, Object> requestParams = new HashMap();
                        HashMap<String, Object> filterrequestParams = new HashMap();
                        double termAmountInBase = 0,termtaxamount=0,termtaxamountinbase=0;
                        
                        requestParams.put("gcurrencyid", order.getCompany().getCurrency().getCurrencyID());
                        requestParams.put("companyid", order.getCompany().getCompanyID());
                        
                        fromcurrencyid = order.getCurrency().getCurrencyID();
                        externalCurrencyRate = order.getExternalCurrencyRate();
                        creationDate = order.getOrderDate();
                        
                        filterrequestParams.put("taxid",taxid);
                        filterrequestParams.put("term",termId);
                        
                        boolean isTermMappedwithTax = accTaxObj.isTermMappedwithTax(filterrequestParams);
                        
                        termMap.put("ordertermid", orderTermId);
                        
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, termAmount, fromcurrencyid, creationDate, externalCurrencyRate);
                        termAmountInBase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                        termMap.put("termamountinbase", termAmountInBase);
                        
                        termMap.put("termAmountExcludingTax", termAmount);
                        termMap.put("termAmountExcludingTaxInBase", termAmountInBase);
                        if(isTermMappedwithTax){
                            termtaxamount=authHandler.round(termAmount * (termTaxPercent/100), companyid);
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, termtaxamount, fromcurrencyid, creationDate, externalCurrencyRate);
                            termtaxamountinbase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                            termMap.put("termtax", taxid);
                            termMap.put("termtaxamount", termtaxamount);
                            termMap.put("termtaxamountinbase", termtaxamountinbase);
                        }
                        status= txnManager.getTransaction(def);
                        
                        accPurchaseOrderobj.updatePurchaseOrderTermMap(termMap);
                        
                        txnManager.commit(status);
                        status = null;
                        updatedcountPOTerm++;
                        msg.append("<center></br> Subdomain : ").append("<b>").append(subdomain).append("</b>").append(" Updated Purchase Order Number : ").append("<b>").append(order.getPurchaseOrderNumber()).append("</b>").append("</center>");
                    }
                }
                
//                // Update Global level transaction of Delivery Order
                KwlReturnObject result5 = accInvoiceDAOobj.getAllGlobalDeliveryOrderOfInvoiceTerms(globalParams);
                List globalDOList = result5.getEntityList();
                for (Object object : globalDOList) {
                    Object[] objArr = (Object[]) object;
                    HashMap<String, Object> termMap = new HashMap<String, Object>();
                    
                    String orderId = (String) (objArr[0] != null ? objArr[0] : "");
                    String orderTermId = (String) (objArr[1] != null ? objArr[1] : "");
                    String taxid = (String) (objArr[2] != null ? objArr[2] : "");
                    double termAmount = (double) (objArr[3] != null ? objArr[3] : 0);
                    double termTaxPercent = (double) (objArr[4] != null ? objArr[4] : 0);
                    String termId = (String) (objArr[5] != null ? objArr[5] : "");
                    
                    KwlReturnObject orderResult = kwlCommonTablesDAOObj.getObject(DeliveryOrder.class.getName(), orderId);
                    if(orderResult!=null&& orderResult.getEntityList()!=null && !orderResult.getEntityList().isEmpty()){
                        DeliveryOrder order = (DeliveryOrder)orderResult.getEntityList().get(0);
                        HashMap<String, Object> requestParams = new HashMap();
                        HashMap<String, Object> filterrequestParams = new HashMap();
                        double termAmountInBase = 0,termtaxamount=0,termtaxamountinbase=0;
                        
                        requestParams.put("gcurrencyid", order.getCompany().getCurrency().getCurrencyID());
                        requestParams.put("companyid", order.getCompany().getCompanyID());
                        
                        fromcurrencyid = order.getCurrency().getCurrencyID();
                        externalCurrencyRate = order.getExternalCurrencyRate();
                        creationDate = order.getOrderDate();
                        
                        filterrequestParams.put("taxid",taxid);
                        filterrequestParams.put("term",termId);
                        
                        boolean isTermMappedwithTax = accTaxObj.isTermMappedwithTax(filterrequestParams);
                        
                        termMap.put("ordertermid", orderTermId);
                        
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, termAmount, fromcurrencyid, creationDate, externalCurrencyRate);
                        termAmountInBase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                        termMap.put("termamountinbase", termAmountInBase);
                        
                        termMap.put("termAmountExcludingTax", termAmount);
                        termMap.put("termAmountExcludingTaxInBase", termAmountInBase);
                        if(isTermMappedwithTax){
                            termtaxamount=authHandler.round(termAmount * (termTaxPercent/100), companyid);
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, termtaxamount, fromcurrencyid, creationDate, externalCurrencyRate);
                            termtaxamountinbase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                            termMap.put("termtax", taxid);
                            termMap.put("termtaxamount", termtaxamount);
                            termMap.put("termtaxamountinbase", termtaxamountinbase);
                        }
                        status= txnManager.getTransaction(def);
                        
                        accInvoiceDAOobj.updateDeliveryOrderTermMap(termMap);
                        
                        txnManager.commit(status);
                        status = null;
                        updatedcountDOTerm++;
                        msg.append("<center></br> Subdomain : ").append("<b>").append(subdomain).append("</b>").append(" Updated Delivery Order Number : ").append("<b>").append(order.getDeliveryOrderNumber()).append("</b>").append("</center>");
                    }
                }

                // Update Global level transaction of Goods Receipt Order
                KwlReturnObject result6 = accGoodsReceiptobj.getAllGlobalGoodsReceiptsOrderOfInvoiceTerms(globalParams);
                List globalGROList = result6.getEntityList();
                for (Object object : globalGROList) {
                    Object[] objArr = (Object[]) object;
                    HashMap<String, Object> termMap = new HashMap<String, Object>();
                    
                    String orderId = (String) (objArr[0] != null ? objArr[0] : "");
                    String orderTermId = (String) (objArr[1] != null ? objArr[1] : "");
                    String taxid = (String) (objArr[2] != null ? objArr[2] : "");
                    double termAmount = (double) (objArr[3] != null ? objArr[3] : 0);
                    double termTaxPercent = (double) (objArr[4] != null ? objArr[4] : 0);
                    String termId = (String) (objArr[5] != null ? objArr[5] : "");
                    
                    KwlReturnObject orderResult = kwlCommonTablesDAOObj.getObject(GoodsReceiptOrder.class.getName(), orderId);
                    if(orderResult!=null&& orderResult.getEntityList()!=null && !orderResult.getEntityList().isEmpty()){
                        GoodsReceiptOrder order = (GoodsReceiptOrder)orderResult.getEntityList().get(0);
                        HashMap<String, Object> requestParams = new HashMap();
                        HashMap<String, Object> filterrequestParams = new HashMap();
                        double termAmountInBase = 0,termtaxamount=0,termtaxamountinbase=0;
                        
                        requestParams.put("gcurrencyid", order.getCompany().getCurrency().getCurrencyID());
                        requestParams.put("companyid", order.getCompany().getCompanyID());
                        
                        fromcurrencyid = order.getCurrency().getCurrencyID();
                        externalCurrencyRate = order.getExternalCurrencyRate();
                        creationDate = order.getOrderDate();
                        
                        filterrequestParams.put("taxid",taxid);
                        filterrequestParams.put("term",termId);
                        
                        boolean isTermMappedwithTax = accTaxObj.isTermMappedwithTax(filterrequestParams);
                        
                        termMap.put("ordertermid", orderTermId);
                        
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, termAmount, fromcurrencyid, creationDate, externalCurrencyRate);
                        termAmountInBase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                        termMap.put("termamountinbase", termAmountInBase);
                        
                        termMap.put("termAmountExcludingTax", termAmount);
                        termMap.put("termAmountExcludingTaxInBase", termAmountInBase);
                        if(isTermMappedwithTax){
                            termtaxamount=authHandler.round(termAmount * (termTaxPercent/100), companyid);
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, termtaxamount, fromcurrencyid, creationDate, externalCurrencyRate);
                            termtaxamountinbase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                            termMap.put("termtax", taxid);
                            termMap.put("termtaxamount", termtaxamount);
                            termMap.put("termtaxamountinbase", termtaxamountinbase);
                        }
                        status= txnManager.getTransaction(def);
                        
                        accGoodsReceiptobj.updateGoodsReceiptOrderTermMap(termMap);
                        
                        txnManager.commit(status);
                        status = null;
                        updatedcountGROTerm++;
                        msg.append("<center></br> Subdomain : ").append("<b>").append(subdomain).append("</b>").append(" Updated Goods Receipt Order Number : ").append("<b>").append(order.getGoodsReceiptOrderNumber()).append("</b>").append("</center>");
                    }
                }
                if(updatedcountCQTerm > 0 || updatedcountVQTerm > 0 || updatedcountSOTerm > 0 ||updatedcountPOTerm > 0 || updatedcountDOTerm > 0 || updatedcountGROTerm > 0){
                    msg.append("<center></br> ******* END for Company : ").append("<b>").append(subdomain).append("</b>  ******* </br> </center>");
                }else{
                    msg.append(" ");
                }
                
            }
        } catch (Exception ex) {
            txnManager.rollback(status);
            System.out.println("Exception occured-" + ex.getMessage());
        } finally {
            return new ModelAndView("jsonView_ex", "model", msg.toString());
        }
    }
    
    /**
     * This method is to update Global level transaction of SI and PI where invoice term is used.
     * @param request
     * @param response
     * @return
     * @throws JSONException 
     */
    public ModelAndView updateGlobalLevelTransactionSIandPIofInvoiceTerm(HttpServletRequest request, HttpServletResponse response) {
        StringBuilder msg = new StringBuilder();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("USIJE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        try{
            String companyid = request.getParameter("companyid");
            String subdomain = request.getParameter("subdomain");
            HashMap<String, Object> globalParams = new HashMap<String, Object>();
            globalParams.put("companyid", companyid);
            KwlReturnObject bAmt = null;
            double externalCurrencyRate = 0d;
            Date creationDate = null;
            String fromcurrencyid ="";
            int updatedcountInvTerm=0,updatedcountPITerm=0;
            if (!StringUtil.isNullOrEmpty(companyid)) {
                
                //Update Global level transaction of Sales Invoice
                KwlReturnObject invoices = accInvoiceDAOobj.getAllGlobalSalesInvoiceOfInvoiceTerms(globalParams);
                List<Invoice> invoiceList = invoices.getEntityList();
                
                for (Object object : invoiceList) {
                    Object[] objArr = (Object[]) object;
                    HashMap<String, Object> termMap = new HashMap<String, Object>();
                    
                    String invoiceId = (String) (objArr[0] != null ? objArr[0] : "");
                    String invoiceTermId = (String) (objArr[1] != null ? objArr[1] : "");
                    String taxid = (String) (objArr[2] != null ? objArr[2] : "");
                    double termAmount = (double) (objArr[3] != null ? objArr[3] : 0);
                    double termTaxPercent = (double) (objArr[4] != null ? objArr[4] : 0);
                    String termId = (String) (objArr[5] != null ? objArr[5] : "");
                    
                    KwlReturnObject invoiceResult = kwlCommonTablesDAOObj.getObject(Invoice.class.getName(), invoiceId);
                    if(invoiceResult!=null&& invoiceResult.getEntityList()!=null && !invoiceResult.getEntityList().isEmpty()){
                        Invoice invoice = (Invoice)invoiceResult.getEntityList().get(0);
                        HashMap<String, Object> requestParams = new HashMap();
                        HashMap<String, Object> filterrequestParams = new HashMap();
                        double termAmountInBase = 0,termtaxamount=0,termtaxamountinbase=0;
                        
                        requestParams.put("gcurrencyid", invoice.getCompany().getCurrency().getCurrencyID());
                        requestParams.put("companyid", invoice.getCompany().getCompanyID());
                        
                        fromcurrencyid = invoice.getCurrency().getCurrencyID();
                        externalCurrencyRate = invoice.getExternalCurrencyRate();
                        creationDate = invoice.getCreationDate();
                        
                        filterrequestParams.put("taxid",taxid);
                        filterrequestParams.put("term",termId);
                        
                        boolean isTermMappedwithTax = accTaxObj.isTermMappedwithTax(filterrequestParams);
                        
                        termMap.put("invoicetermid", invoiceTermId);
                        
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, termAmount, fromcurrencyid, creationDate, externalCurrencyRate);
                        termAmountInBase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                        
                        termMap.put("termamountinbase", termAmountInBase);
                        termMap.put("termAmountExcludingTax", termAmount);
                        termMap.put("termAmountExcludingTaxInBase", termAmountInBase);
                        
                        if (isTermMappedwithTax) {
                            termMap.put("termtax", taxid);
                        }
                        status= txnManager.getTransaction(def);
                        
                        accInvoiceDAOobj.updateInvoiceTermMap(termMap);
                        
                        txnManager.commit(status);
                        status = null;
                        updatedcountInvTerm++;
                        msg.append("<center></br> Subdomain : ").append("<b>").append(subdomain).append("</b>").append(" Updated Invoice Number : ").append("<b>").append(invoice.getInvoiceNumber()).append("</b>").append("</center>");
                    }
                }
                
                //Update Global level transaction of Purchase Invoice
                KwlReturnObject purchaseInvoices = accGoodsReceiptobj.getAllGlobalGoodsReceiptOfInvoiceTerms(globalParams);
                List<Invoice> piList = purchaseInvoices.getEntityList();
                
                for (Object object : piList) {
                    Object[] objArr = (Object[]) object;
                    HashMap<String, Object> termMap = new HashMap<String, Object>();
                    
                    String invoiceId = (String) (objArr[0] != null ? objArr[0] : "");
                    String receiptTermId = (String) (objArr[1] != null ? objArr[1] : "");
                    String taxid = (String) (objArr[2] != null ? objArr[2] : "");
                    double termAmount = (double) (objArr[3] != null ? objArr[3] : 0);
                    double termTaxPercent = (double) (objArr[4] != null ? objArr[4] : 0);
                    String termId = (String) (objArr[5] != null ? objArr[5] : "");
                    
                    KwlReturnObject invoiceResult = kwlCommonTablesDAOObj.getObject(GoodsReceipt.class.getName(), invoiceId);
                    if(invoiceResult!=null&& invoiceResult.getEntityList()!=null && !invoiceResult.getEntityList().isEmpty()){
                        GoodsReceipt goodsreceipt = (GoodsReceipt)invoiceResult.getEntityList().get(0);
                        HashMap<String, Object> requestParams = new HashMap();
                        HashMap<String, Object> filterrequestParams = new HashMap();
                        double termAmountInBase = 0,termtaxamount=0,termtaxamountinbase=0;
                        
                        requestParams.put("gcurrencyid", goodsreceipt.getCompany().getCurrency().getCurrencyID());
                        requestParams.put("companyid", goodsreceipt.getCompany().getCompanyID());
                        
                        fromcurrencyid = goodsreceipt.getCurrency().getCurrencyID();
                        externalCurrencyRate = goodsreceipt.getExternalCurrencyRate();
                        creationDate = goodsreceipt.getCreationDate();
                        
                        filterrequestParams.put("taxid",taxid);
                        filterrequestParams.put("term",termId);
                        
                        boolean isTermMappedwithTax = accTaxObj.isTermMappedwithTax(filterrequestParams);
                        
                        termMap.put("receipttermid", receiptTermId);
                        
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, termAmount, fromcurrencyid, creationDate, externalCurrencyRate);
                        termAmountInBase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                        
                        termMap.put("termamountinbase", termAmountInBase);
                        termMap.put("termAmountExcludingTax", termAmount);
                        termMap.put("termAmountExcludingTaxInBase", termAmountInBase);
                        
                        if (isTermMappedwithTax) {
                            termMap.put("termtax", taxid);
                        }
                        status= txnManager.getTransaction(def);
                        
                        accGoodsReceiptobj.updateGoodsReceiptTermMap(termMap);
                        
                        txnManager.commit(status);
                        status = null;
                        updatedcountPITerm++;
                        msg.append("<center></br> Subdomain : ").append("<b>").append(subdomain).append("</b>").append(" Updated Purchase Invoice Number : ").append("<b>").append(goodsreceipt.getGoodsReceiptNumber()).append("</b>").append("</center>");
                    }
                }
                
                if( updatedcountInvTerm > 0 || updatedcountPITerm > 0){
                    msg.append("<center></br> ******* END for Company : ").append("<b>").append(subdomain).append("</b>  ******* </br> </center>");
                }else{
                    msg.append(" ");
                }
            }
        } catch (Exception ex) {
            System.out.println("Exception occured-" + ex.getMessage());
        } finally {
            return new ModelAndView("jsonView_ex", "model", msg.toString());
        }
    }
    
    /**
     * This method is to update line level transaction of CQ,VQ,SO,PO,DO and GRO where Invoice Term is used.
     * @param request
     * @param response
     * @return
     * @throws JSONException 
     */
    public ModelAndView updateLineLevelTransactionOfInvoiceTerm(HttpServletRequest request, HttpServletResponse response) {
        StringBuilder msg = new StringBuilder();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("USIJE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        try{
            String companyid = request.getParameter("companyid");
            String subdomain = request.getParameter("subdomain");
            HashMap<String, Object> globalParams = new HashMap<String, Object>();
            globalParams.put("companyid", companyid);
            KwlReturnObject bAmt = null;
            double externalCurrencyRate = 0d;
            Date creationDate = null;
            String fromcurrencyid ="";
            int updatedcountCQTerm=0,updatedcountVQTerm=0,updatedcountSOTerm=0,updatedcountPOTerm=0,updatedcountDOTerm=0,updatedcountGROTerm=0;
            if (!StringUtil.isNullOrEmpty(companyid)) {
                
                //Update Line level transaction of Customer Quotation
                KwlReturnObject result1 = accSalesOrderDAOobj.getAllLineLevelQuotaionsOfInvoiceTerms(globalParams);
                List lineLevelQuotationList = result1.getEntityList();
                for (Object object : lineLevelQuotationList) {
                    Object[] objArr = (Object[]) object;
                    HashMap<String, Object> termMap = new HashMap<String, Object>();
                    List<String> taxList = new ArrayList();
                    
                    String quotationId = (String) (objArr[0] != null ? objArr[0] : "");
                    String quotationNumber = (String) (objArr[1] != null ? objArr[1] : "");
                    
                    KwlReturnObject quotationResult = kwlCommonTablesDAOObj.getObject(Quotation.class.getName(), quotationId);
                    if(quotationResult!=null&& quotationResult.getEntityList()!=null && !quotationResult.getEntityList().isEmpty()){
                        Quotation quotation = (Quotation)quotationResult.getEntityList().get(0);
                        
                        Set<QuotationDetail> qtdetails = quotation.getRows();
                        for (QuotationDetail quotationDetail : qtdetails) {
                            if(quotationDetail.getTax()!= null){
                                taxList.add(quotationDetail.getTax().toString());
                            }
                        }
                        
                        HashMap<String, Object> requestParam = new HashMap();
                        requestParam.put("quotation", quotation.getID());
                        KwlReturnObject quotationTermResult = accSalesOrderDAOobj.getQuotationTermMap(requestParam);
                        List<QuotationTermMap> qtermMap = quotationTermResult.getEntityList();
                        for (QuotationTermMap quotationTermMap : qtermMap) {
                            InvoiceTermsSales mt = quotationTermMap.getTerm();
                            requestParam.put("termid",mt.getId());
                            accAccountDAOobj.getLinkedTermTax(requestParam);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("Exception occured-" + ex.getMessage());
        } finally {
            return new ModelAndView("jsonView_ex", "model", msg.toString());
        }
    }
    public ModelAndView verifyStockValuationChanges(HttpServletRequest request, HttpServletResponse response) {
            String message = "";
        try {
            int companyCount =0;
            Date runningDate = new Date();
            long runningTime = runningDate.getTime();
            long timeDifference = 25200000;
//            long timeDifference = 120000;
            KwlReturnObject kwlCompany = accountingHandlerDAOobj.getCompanyList();
            List ll = kwlCompany.getEntityList();
            Iterator iterator = ll.iterator();
            StringBuilder companyBuilder = new StringBuilder();
            while (iterator.hasNext()) {
                companyCount++;
                Date currentDate = new Date();
                if((runningTime+timeDifference) < currentDate.getTime()){
                    break;
                }
                Company company = (Company) iterator.next();
                String companyid = company.getCompanyID();
                companyBuilder.append(company.getSubDomain()).append(",");
                try {
                    boolean stockValuationFlag = false;

                    ExtraCompanyPreferences extrapref = null;
                    KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
                    if (extraprefresult != null && !extraprefresult.getEntityList().isEmpty() && extraprefresult.getEntityList().get(0) != null) {
                        extrapref = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
                        stockValuationFlag = extrapref.isStockValuationFlag();
                    }
                    if (stockValuationFlag) {
                        System.out.println("*******************");
                        System.out.println("company -> "+ company.getSubDomain()+".... name-> "+company.getCompanyName());
                        System.out.println("**Balance Sheet**");
                        JSONObject paramjobj = new JSONObject();
                        paramjobj.put("cdomain", company.getSubDomain());
                        paramjobj.put("verificationScript", true);
                        paramjobj.put("timezoneid", "236");
                        paramjobj.put("sevletPath", "/ACCReports/getBalanceSheet.do");
                        paramjobj.put("browsertz", "+05:30");
                        paramjobj.put("javax.servlet.forward.context_path", "/Accounting");
                        paramjobj.put("userid", company.getCreator().getUserID());
                        paramjobj.put("userId", company.getCreator().getUserID());
                        paramjobj.put("lid", company.getCreator().getUserID());
                        paramjobj.put("org.apache.catalina.ASYNC_SUPPORTED", false);
                        paramjobj.put("userdateformat", "dd/MM/yyyy");
                        paramjobj.put("companyid", company.getCompanyID());
                        paramjobj.put("username", "admin");
                        paramjobj.put("templatecode", "-1");
                        paramjobj.put("filterConjuctionCriteria", "");
                        paramjobj.put("periodView", "true");
                        paramjobj.put("dateformatid", "23");
                        paramjobj.put("remoteAddress", "0:0:0:0:0:0:0:1");
                        paramjobj.put("company", company.getCompanyName());
                        paramjobj.put("usermailId", "kapil.gupta@deskera.com");
                        paramjobj.put("language", "en_US");
                        paramjobj.put("gcurrencyid", company.getCurrency().getCurrencyID());
                        paramjobj.put("isForTradingAndProfitLoss", true);
                        paramjobj.put("nondeleted", "true");
                        paramjobj.put("initialized", "true");
                        paramjobj.put("companyPreferences", "");
                        paramjobj.put("searchJson", "");
                        paramjobj.put("isFromBalanceSheet", true);
                        paramjobj.put("endpredate", "");
                        paramjobj.put("stpredate", "");
                        paramjobj.put("subdomain", company.getSubDomain());
                        paramjobj.put("mode", "66");
                        getFinancialYear(companyid, paramjobj);
                        accReportsService.getBalanceSheetAllAccounts(paramjobj, null);
                        
                        
                        System.out.println("**Trial Balance**");
                        paramjobj = new JSONObject();
                        paramjobj.put("cdomain", company.getSubDomain());
                        paramjobj.put("verificationScript", true);
                        paramjobj.put("timezoneid", "236");
                        paramjobj.put("sevletPath", "/ACCReports/getBalanceSheet.do");
                        paramjobj.put("browsertz", "+05:30");
                        paramjobj.put("javax.servlet.forward.context_path", "/Accounting");
                        paramjobj.put("userid", company.getCreator().getUserID());
                        paramjobj.put("userId", company.getCreator().getUserID());
                        paramjobj.put("lid", company.getCreator().getUserID());
                        paramjobj.put("org.apache.catalina.ASYNC_SUPPORTED", false);
                        paramjobj.put("userdateformat", "dd/MM/yyyy");
                        paramjobj.put("companyid", company.getCompanyID());
                        paramjobj.put("companyids", company.getCompanyID());
                        paramjobj.put("username", "admin");
                        paramjobj.put("dateformatid", "23");
                        paramjobj.put("remoteAddress", "0:0:0:0:0:0:0:1");
                        paramjobj.put("company", company.getCompanyName());
                        paramjobj.put("usermailId", "kapil.gupta@deskera.com");
                        paramjobj.put("language", "en_US");
                        paramjobj.put("gcurrencyid", company.getCurrency().getCurrencyID());
                        paramjobj.put("accountIds", "");
                        paramjobj.put("initialized", "true");
                        paramjobj.put("companyPreferences", "");
                        paramjobj.put("searchJson", "");
                        paramjobj.put("isOpeningBalanceFlag", true);
                        paramjobj.put("consolidateFlag", "false");
                        paramjobj.put("subdomain", company.getSubDomain());
                        paramjobj.put("roleid", "1");
                        paramjobj.put("callwith", "0");
                        paramjobj.put("mode", "62");
                        getFinancialYear(companyid, paramjobj);
                        accReportsService.getTrialBalance(paramjobj);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    continue;
                }
            }
            message ="Total companies done -> "+companyCount+" out of "+ll.size()+" total time-> "+((new Date()).getTime()-runningTime)/1000 +"... companies covered -> "+companyBuilder.toString();
            System.out.println("message of verifyStockValuationChanges -> "+message);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ModelAndView("jsonView_ex", "model", message);
    }
    
    private void getFinancialYear(String companyid, JSONObject paramjobj) throws ServiceException, SessionExpiredException, ParseException, JSONException {
        String fromdate = "";
        String todate = "";
        Date toDate = new Date();
        DateFormat df1 = authHandler.getOnlyDateFormat();
        Map<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("id", companyid);
        KwlReturnObject result = accCompanyPreferencesObj.getCompanyPreferences(requestParams);
        CompanyAccountPreferences pref = (CompanyAccountPreferences) result.getEntityList().get(0);
        Date financialYear = pref.getFinancialYearFrom();
//        requestParam.put("companyAccPref", pref);


        Date currentDate = new Date();

        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);
        int currentyear = cal.get(Calendar.YEAR);

        cal.setTime(financialYear);
        int financialmonth = cal.get(Calendar.MONTH);
        int financialdate = cal.get(Calendar.DATE);

        String financialYearMonth = String.valueOf(financialmonth).length() == 1 ? "0" + String.valueOf(financialmonth + 1) : String.valueOf(financialmonth + 1);
        String monthDateStr = financialYearMonth + "-" + financialdate;
        Date fd = df1.parse(currentyear + "-" + monthDateStr);
        if (currentDate.compareTo(fd) < 0) {
            fd = df1.parse((currentyear - 1) + "-" + monthDateStr);
        }
        financialYear = fd;
        fromdate = df1.format(financialYear);
        cal.setTime(financialYear);
        cal.add(Calendar.YEAR, 1);
        cal.add(Calendar.DATE, -1);
        toDate = cal.getTime();
        todate = df1.format(toDate);

        paramjobj.put("stdate", authHandler.getDateOnlyFormat().format(financialYear));
        paramjobj.put("enddate", authHandler.getDateOnlyFormat().format(toDate));
    }
   
    public ModelAndView verifyAgedReceivablesChanges(HttpServletRequest request, HttpServletResponse response) {
            String message = "";
            JSONArray jarr = new JSONArray();
            JSONObject fjobj = new JSONObject();
        try {            
            int companyCount =0;
            Date runningDate = new Date();
            long runningTime = runningDate.getTime();
            long timeDifference = 25200000;
//            long timeDifference = 120000;
            KwlReturnObject kwlCompany = accountingHandlerDAOobj.getCompanyList();
            List ll = kwlCompany.getEntityList();
            Iterator iterator = ll.iterator();
            StringBuilder companyBuilder = new StringBuilder();
            while (iterator.hasNext()) {
                companyCount++;
                Date currentDate = new Date();
                if((runningTime+timeDifference) < currentDate.getTime()){
                    break;
                }
                Company company = (Company) iterator.next();
//                if(!(company.getSubDomain().equals("cskbio") || company.getSubDomain().equals("malaysia") || company.getSubDomain().equals("singapore") || company.getSubDomain().equals("india") || company.getSubDomain().equals("perpetualinventory"))){
//                    continue;
//                }
               
                String companyid = company.getCompanyID();
                companyBuilder.append(company.getSubDomain()).append(",");
                try {
                    String view = Constants.jsonView;
                    JSONObject jobj = new JSONObject();
                    JSONArray DataJArr = new JSONArray();
                    boolean issuccess = false;
                    JSONObject totalAmountJSON = new JSONObject();
                    String msg = "";
                    try {
                        HashMap invoiceRequestParams = new HashMap();
                        JSONObject paramObj = StringUtil.convertRequestToJsonObject(request);
                        boolean isWidgetRequest = request.getParameter("isWidgetRequest") != null ? Boolean.parseBoolean(request.getParameter("isWidgetRequest")) : false;
                        boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
                        String[] companyids = (consolidateFlag && request.getParameter(Constants.companyids) != null) ? request.getParameter(Constants.companyids).split(",") : sessionHandlerImpl.getCompanyid(request).split(",");
                        String gcurrencyid = (consolidateFlag && request.getParameter(Constants.globalCurrencyKey) != null) ? request.getParameter(Constants.globalCurrencyKey) : sessionHandlerImpl.getCurrencyID(request);
                        boolean isAged = request.getParameter("isAged") != null ? Boolean.parseBoolean(request.getParameter("isAged")) : false;
                        int isgroupcomboDate = request.getParameter("groupcombo") != null ? Integer.parseInt(request.getParameter("groupcombo")) : 0;
                        boolean includeExcludeChildCmb;
                        if (request.getParameter("includeExcludeChildCmb") != null && request.getParameter("includeExcludeChildCmb").toString().equals("All")) {
                            includeExcludeChildCmb = true;
                        } else {
                            includeExcludeChildCmb = request.getParameter("includeExcludeChildCmb") != null ? Boolean.parseBoolean(request.getParameter("includeExcludeChildCmb")) : false;
                        }

                        boolean onlyOutstanding = false;
                        boolean report = false;
                        int totalCount = 0;
                        boolean onlyAmountDue = StringUtil.isNullOrEmpty(request.getParameter("onlyamountdue")) ? false : Boolean.parseBoolean(request.getParameter("onlyamountdue"));
                        if (!StringUtil.isNullOrEmpty(request.getParameter("onlyOutsatnding"))) {
                            onlyOutstanding = Boolean.parseBoolean(request.getParameter("onlyOutsatnding"));
                        }

//            String companyid = "";
                        for (int cnt = 0; cnt < companyids.length; cnt++) {
//                            companyid = companyids[cnt];
                            request.setAttribute(Constants.companyKey, companyid);
                            request.setAttribute(Constants.globalCurrencyKey, gcurrencyid);
                            request.setAttribute("onlyOutstanding", onlyOutstanding);
                            request.setAttribute("onlyamountdue", onlyAmountDue);
                            invoiceRequestParams = accInvoiceServiceDAO.getCustomerAgedReceivableMap(request, true);
                            if ((isgroupcomboDate == Constants.AgedPayableBaseCurrency || isgroupcomboDate == Constants.AgedPayableOtherthanBaseCurrency) && isAged) {//2==BaseCurrency,3=Other than base currency 
                                invoiceRequestParams.put("groupcombo", isgroupcomboDate);
                                invoiceRequestParams.put(Constants.globalCurrencyKey, sessionHandlerImpl.getCurrencyID(request));
                            }

                        }
//                        companyid = sessionHandlerImpl.getCompanyid(request);
                        JSONObject finalDataJObj = new JSONObject();

                        int moduleIDForFetchingGroupingData = !StringUtil.isNullOrEmpty(request.getParameter("moduleIDForFetchingGroupingData")) ? Integer.parseInt(request.getParameter("moduleIDForFetchingGroupingData")) : 0;
                        if (Constants.moduleSetForAgedReceivable.contains(moduleIDForFetchingGroupingData)) {
                            request.setAttribute("detailedviewgroupingondimensions", true);
                            finalDataJObj = accInvoiceServiceDAO.getCustomerAgedReceivableBasedOnDocumentsDimension(request, false, false);
                            DataJArr = finalDataJObj.getJSONArray("DataJobj");
                        } else {
                            invoiceRequestParams.put("isAgedDetailsReport", true);
                            invoiceRequestParams.put("includeExcludeChildCmb", includeExcludeChildCmb);
                            DataJArr = accInvoiceServiceDAO.getAgeingJson(invoiceRequestParams, paramObj);

//                    DataJArr = accTemplateReportService.getCustomerAgedReceivable(request, true, true);
//                    JSONArray CreditNotejArr = new JSONArray();
//
                            JSONArray OBJArryInvoice = new JSONArray();
                            KwlReturnObject result = null;
//                    if (isAdvanceSearch) {
//                        requestParams.put(Constants.Acc_Search_Json, invoiceSearchJson);
//                        requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
//                        requestParams.put(Constants.moduleid, Constants.Acc_Invoice_ModuleId);
//                    }
//                    DataJArr = accTemplateReportService.getCustomerAgedReceivable(request, true, true);

//                    result = accInvoiceDAOobj.getOpeningBalanceInvoices(invoiceRequestParams);
//                    if (result != null && result.getEntityList() != null && result.getEntityList().size() > 0) {
//                        OBJArryInvoice = AccInvoiceServiceHandler.getAgedOpeningBalanceInvoiceJson(invoiceRequestParams, result.getEntityList(), OBJArryInvoice, accCurrencyDAOobj, accInvoiceCommon, request, accountingHandlerDAOobj, accAccountDAOobj, fieldDataManagercntrl, accJournalEntryobj);
//                        for (int i = 0; i < OBJArryInvoice.length(); i++) {
//                            DataJArr.put(OBJArryInvoice.get(i));
//                        }
//                    }
//                    JSONArray OBJArryDebitNote = new JSONArray();
//                    if (isAdvanceSearch) {
//                        requestParams.put(Constants.Acc_Search_Json, dnSearchJson);
//                        requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
//                        requestParams.put(Constants.moduleid, Constants.Acc_Debit_Note_ModuleId);
//                    }
//                    result = accDebitNoteobj.getOpeningBalanceCustomerDNs(requestParams);
//                    if (result != null && result.getEntityList() != null && result.getEntityList().size() > 0) {
//                        OBJArryDebitNote = AccInvoiceServiceHandler.getAgedOpeningBalanceDebitNoteJson(requestParams, result.getEntityList(), OBJArryDebitNote, accCurrencyDAOobj, accReceiptDAOobj, request, accountingHandlerDAOobj, accAccountDAOobj, fieldDataManagercntrl, accJournalEntryobj,accCreditNoteDAOobj);
//                        for (int i = 0; i < OBJArryDebitNote.length(); i++) {
//                            DataJArr.put(OBJArryDebitNote.get(i));
//                       }
//                    }
//                    JSONArray OBJArryCreditNote = new JSONArray();
//                    if (isAdvanceSearch) {
//                        requestParams.put(Constants.Acc_Search_Json, cnSearchJson);
//                        requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
//                        requestParams.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
//                    }
//                    result = accCreditNoteDAOobj.getOpeningBalanceCNs(invoiceRequestParams);
//                    OBJArryCreditNote = AccInvoiceServiceHandler.getAgedOpeningBalanceCreditNoteJson(invoiceRequestParams, result.getEntityList(), OBJArryCreditNote, accCurrencyDAOobj, accPaymentDAOobj, request, accountingHandlerDAOobj, accCreditNoteDAOobj, accAccountDAOobj, fieldDataManagercntrl, accJournalEntryobj);
//                    if (result != null && result.getEntityList() != null && result.getEntityList().size() > 0) {
//                        for (int i = 0; i < OBJArryCreditNote.length(); i++) {
//                            DataJArr.put(OBJArryCreditNote.get(i));
//                        }
//                    }
//                    JSONArray OBJArryPayment = new JSONArray();
//                    if (isAdvanceSearch) {
//                        requestParams.put(Constants.Acc_Search_Json, receiptSearchJson);
//                        requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
//                        requestParams.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
//                    }
//                    result = accReceiptDAOobj.getOpeningBalanceReceipts(requestParams);
//                    if (result != null && result.getEntityList() != null && result.getEntityList().size() > 0) {
//                        OBJArryPayment = AccInvoiceServiceHandler.getAgedOpeningBalanceReceiptJson(requestParams, result.getEntityList(), OBJArryPayment, accCurrencyDAOobj, accReceiptDAOobj, request, accountingHandlerDAOobj, accAccountDAOobj, fieldDataManagercntrl, accJournalEntryobj);
//                        for (int i = 0; i < OBJArryPayment.length(); i++) {
//                            DataJArr.put(OBJArryPayment.get(i));
//                        }
//                    }
//                    if (isAdvanceSearch) {
//                        requestParams.put(Constants.Acc_Search_Json, cnSearchJson);
//                        requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
//                    }
//                   result = accCreditNoteDAOobj.getCreditNoteMerged(invoiceRequestParams);
//                    if (result != null && result.getEntityList() != null && result.getEntityList().size() > 0) {
//                        CreditNotejArr = AccInvoiceServiceHandler.getCreditNotesMergedJson(invoiceRequestParams, result.getEntityList(), CreditNotejArr, accountingHandlerDAOobj, authHandlerDAOObj, accCurrencyDAOobj, accPaymentDAOobj, request, accCreditNoteDAOobj, accAccountDAOobj, fieldDataManagercntrl, accJournalEntryobj);
//                        for (int i = 0; i < CreditNotejArr.length(); i++) {
//                            DataJArr.put(CreditNotejArr.get(i));
//                        }
//                    }
//                    invoiceRequestParams.put("cntype", 4);
//                    JSONArray DebitNotejArr = new JSONArray();//This is used for getting DN gainst customer 
//                    if (isAdvanceSearch) {
//                        requestParams.put(Constants.Acc_Search_Json, dnSearchJson);
//                        requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
//                    }
//                    result = accDebitNoteobj.getDebitNoteMerged(requestParams);
//                    if (result != null && result.getEntityList() != null && result.getEntityList().size() > 0) {
//                        DebitNotejArr = AccInvoiceServiceHandler.getDebitNotesMergedJson(requestParams, result.getEntityList(), DebitNotejArr, accountingHandlerDAOobj, authHandlerDAOObj, accCurrencyDAOobj, accReceiptDAOobj, request, accAccountDAOobj, fieldDataManagercntrl, accJournalEntryobj,accCreditNoteDAOobj);
//                        for (int i = 0; i < DebitNotejArr.length(); i++) {
//                            DataJArr.put(DebitNotejArr.get(i));
//                        }
//                    }
//                    requestParams.remove("cntype");
//                    JSONArray receivePaymentJArr = new JSONArray();
//                    if (isAdvanceSearch) {
//                        requestParams.put(Constants.Acc_Search_Json, receiptSearchJson);
//                        requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
//                    }
//                    
//                    requestParams.put("allAdvPayment", true); // needs only advance type record so that putted true
//                    requestParams.put("paymentWindowType", 1);//Receipt to Customer record
//                    result = accReceiptDAOobj.getReceipts(requestParams);
//                    receivePaymentJArr = AccInvoiceServiceHandler.getReceiptsJson(requestParams, result.getEntityList(), receivePaymentJArr, accountingHandlerDAOobj, authHandlerDAOObj, accCurrencyDAOobj, accReceiptDAOobj, request, accAccountDAOobj, fieldDataManagercntrl, accJournalEntryobj);
//                    for (int i = 0; i < receivePaymentJArr.length(); i++) {
//                        DataJArr.put(receivePaymentJArr.get(i));
//                    }
//////                    
//                    if (isAdvanceSearch) {
//                        requestParams.put(Constants.Acc_Search_Json, makePaymentSearchJson);
//                        requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
////                    }
//                    JSONArray makePaymentJArr = new JSONArray();
//                    invoiceRequestParams.put("allAdvPayment", true); // needs only refund type record so that putted true
//                    invoiceRequestParams.put("paymentWindowType", 2);//Payment to Customer record
//                    result = accVendorPaymentobj.getPayments(invoiceRequestParams);
//                    if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
//                        makePaymentJArr = accGoodsReceiptServiceHandler.getPaymentsJson(invoiceRequestParams, result.getEntityList(), makePaymentJArr, accCurrencyDAOobj, accountingHandlerDAOobj, accVendorPaymentobj);
//                    }
//                    for (int i = 0; i < makePaymentJArr.length(); i++) {
//                        DataJArr.put(makePaymentJArr.get(i));
//                    }
//                    requestParams.put("allAdvPayment", true); // needs only refund type record so that putted true
//                    requestParams.put("paymentWindowType", 2);//Payment to Customer record
                            String sortKey = "";
                            if (isgroupcomboDate == 1) {//IF Sort By date
                                sortKey = "date";
                                DataJArr = AccountingManager.sortJsonArrayOnDateValues(DataJArr, (DateFormat) invoiceRequestParams.get(Constants.df), sortKey, true);
                            } else {
                                sortKey = "type";
                                DataJArr = AccountingManager.sortJsonArrayOnStringValues(DataJArr, sortKey, true);
                            }
                            sortKey = "personinfo";
                            DataJArr = AccountingManager.sortJsonArrayOnStringValues(DataJArr, sortKey, true);    //show multigrouping JSONArray required to sort
                        }

                        if (request.getParameter("minimumAmountDue") != null) {
                            JSONArray temp = new JSONArray();
                            double minimumAmountDue = Double.parseDouble(request.getParameter("minimumAmountDue").toString());
                            for (int i = 0; i < DataJArr.length(); i++) {
                                if (DataJArr.getJSONObject(i).getDouble("amountdue") >= minimumAmountDue) {
                                    temp.put(DataJArr.getJSONObject(i));
                                }
                            }
                            DataJArr = temp;
                        }
                        int count = DataJArr.length();
                        JSONArray pagedJson = DataJArr;
                        boolean isAgeingExport = request.getAttribute("isAgeingExport") == null ? false : (Boolean) request.getAttribute("isAgeingExport");
                        if (!isAgeingExport && (consolidateFlag || isAged)) {
                            String start = request.getParameter("start");
                            String limit = request.getParameter("limit");
                            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                                if (isAged) { // ADD GRNAD TOTAL AS LAST RECORD IN PAGED JSON
                                    pagedJson = StringUtil.getPagedJSONForAgedWIthTotal(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
                                    totalAmountJSON = pagedJson.getJSONObject(pagedJson.length() - 1);
                                    pagedJson.remove(pagedJson.length() - 1);
                                    jobj.put(Constants.AGEDAMOUNTSUMMARY, totalAmountJSON);
                                } else {
                                    pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
                                }
                            }
                        }
                        addCustomFields(request, pagedJson, companyid, paramObj);

                        if (isWidgetRequest) {
                            view = "jsonView_ex";
                            JSONObject commData = new JSONObject();
                            JSONObject jMeta = new JSONObject();
                            Map<String, Object> requestParamsForCM = new HashMap<>();
                            Map<String, JSONArray> returnMap = accInvoiceServiceDAO.getColumnModuleForAPAR(requestParamsForCM);
                            JSONArray jarrRecords = returnMap.get("record");
                            JSONArray jarrColumns = returnMap.get("columns");
                            commData.put("success", true);
                            jMeta.put("totalProperty", "totalCount");
                            jMeta.put("root", "coldata");
                            jMeta.put("fields", jarrRecords); //Record Array
                            commData.put("coldata", pagedJson);  //Actual data
                            commData.put("columns", jarrColumns); //Column Module Array
                            commData.put("totalCount", count);
                            commData.put("metaData", jMeta);
                            jobj.put("valid", true);
                            jobj.put(Constants.data, commData);
                        } else {
                            jobj.put(Constants.data, pagedJson);
                        }
                        boolean isRecurringInvoice = !StringUtil.isNullOrEmpty(request.getParameter("getRepeateInvoice")) ? Boolean.parseBoolean(request.getParameter("getRepeateInvoice")) : false;
                        if (report || isRecurringInvoice) {
                            jobj.put("count", totalCount);
                        } else {
                            jobj.put("count", count);
                        }
                        issuccess = true;
                        message = "\nCompany -> "+company.getCompanyName()+"\nNo. Of records -> "+count+"\nGrand total -> "+totalAmountJSON.getDouble("grandAmountdueinbase");
                        System.out.println("\n"+message+"\n");
                    } catch (Exception ex) {
                        msg = "" + ex.getMessage();
                        Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        try {
                            jobj.put("success", issuccess);
                            jobj.put("msg", msg);
                            jobj.put("message", message);
                            jarr.put(jobj);
                        } catch (JSONException ex) {
                            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
//                    return new ModelAndView(view, "model", jobj.toString());
                   
//                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    continue;
                }
            }            
            fjobj.put("data", jarr);
            message ="\nTotal companies done -> "+companyCount+" out of "+ll.size()+" total time-> "+((new Date()).getTime()-runningTime)/1000 +"... companies covered -> "+companyBuilder.toString();
            System.out.println("\nMessage -> "+message+"\n");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ModelAndView("jsonView_ex", "model", fjobj.toString());
    }
   
    public void addCustomFields(HttpServletRequest request, JSONArray pagedJson, String companyid, JSONObject paramObj) throws JSONException, ServiceException, SessionExpiredException{
        //Invoice custom data 
        HashMap<String, String> invreplaceFieldMap = new HashMap();
        HashMap<String, String> invcustomFieldMap = new HashMap();
        HashMap<String, String> invcustomDateFieldMap = new HashMap();
        HashMap<String, Object> invfieldrequestParams = new HashMap();
        HashMap<String, Object> invfieldrequestParamsRows = new HashMap();
        HashMap<String, String> invreplaceFieldMapRows = new HashMap();
        HashMap<String, String> invcustomFieldMapRows = new HashMap();
        HashMap<String, String> invcustomDateFieldMapRows = new HashMap();
        HashMap<String, Integer> invfieldMap = null;
        HashMap<String, Integer> invfieldMapRows = null;

        //Custom field details Maps for Global data
        invreplaceFieldMap = new HashMap();
        invcustomFieldMap = new HashMap();
        invcustomDateFieldMap = new HashMap();
        invfieldrequestParams = new HashMap();
        int moduleid = Constants.Acc_Invoice_ModuleId;
        invfieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
        invfieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid));
        invfieldMap = accAccountDAOobj.getFieldParamsCustomMap(invfieldrequestParams, invreplaceFieldMap, invcustomFieldMap, invcustomDateFieldMap);

        //Custom field details Maps for Line Level data
        invfieldrequestParamsRows.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
        invfieldrequestParamsRows.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1));
        invfieldMapRows = accAccountDAOobj.getFieldParamsCustomMap(invfieldrequestParamsRows, invreplaceFieldMapRows, invcustomFieldMapRows, invcustomDateFieldMapRows);

        //CreditNote custom data
        HashMap<String, String> cnreplaceFieldMap = new HashMap<String, String>();
        HashMap<String, String> cncustomFieldMap = new HashMap<String, String>();
        HashMap<String, String> cncustomDateFieldMap = new HashMap<String, String>();
        HashMap<String, Integer> cnFieldMap = null;
        HashMap<String, Object> cnfieldrequestParams = new HashMap();
        cnfieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
        cnfieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Credit_Note_ModuleId));
        cnFieldMap = accAccountDAOobj.getFieldParamsCustomMap(cnfieldrequestParams, cnreplaceFieldMap, cncustomFieldMap, cncustomDateFieldMap);

        //Custom field details Maps for Line Level data
        HashMap<String, Object> cnfieldrequestParamsRows = new HashMap();
        HashMap<String, String> cnreplaceFieldMapRows = new HashMap();
        HashMap<String, String> cncustomFieldMapRows = new HashMap();
        HashMap<String, String> cncustomDateFieldMapRows = new HashMap();
        cnfieldrequestParamsRows.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
        cnfieldrequestParamsRows.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Credit_Note_ModuleId));
        HashMap<String, Integer> cnfieldMapRows = null;
        cnfieldMapRows = accAccountDAOobj.getFieldParamsCustomMapForRows(cnfieldrequestParamsRows, cnreplaceFieldMapRows, cncustomFieldMapRows, cncustomDateFieldMapRows);

        //Debit Note custom data
        HashMap<String, String> dnreplaceFieldMap = new HashMap<String, String>();
        HashMap<String, String> dncustomFieldMap = new HashMap<String, String>();
        HashMap<String, String> dncustomDateFieldMap = new HashMap<String, String>();
        HashMap<String, Integer> dnFieldMap = null;
        HashMap<String, Object> dnfieldrequestParams = new HashMap();
        dnfieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
        dnfieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Debit_Note_ModuleId));
        if (accAccountDAOobj != null) {
            dnFieldMap = accAccountDAOobj.getFieldParamsCustomMap(dnfieldrequestParams, dnreplaceFieldMap, dncustomFieldMap, dncustomDateFieldMap);
        }

        //Custom field details Maps for Line Level data
        HashMap<String, Object> dnfieldrequestParamsRows = new HashMap();
        HashMap<String, String> dnreplaceFieldMapRows = new HashMap();
        HashMap<String, String> dncustomFieldMapRows = new HashMap();
        HashMap<String, String> dncustomDateFieldMapRows = new HashMap();
        dnfieldrequestParamsRows.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
        dnfieldrequestParamsRows.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Debit_Note_ModuleId));
        HashMap<String, Integer> dnfieldMapRows = null;
        dnfieldMapRows = accAccountDAOobj.getFieldParamsCustomMapForRows(dnfieldrequestParamsRows, dnreplaceFieldMapRows, dncustomFieldMapRows, dncustomDateFieldMapRows);

        //Receipt custom data
        HashMap<String, Object> rtfieldrequestParamsGlobalLevel = new HashMap();
        HashMap<String, String> rtcustomFieldMapGlobalLevel = new HashMap<String, String>();
        HashMap<String, String> rtcustomDateFieldMapGlobalLevel = new HashMap<String, String>();
        HashMap<String, Integer> rtFieldMapGlobalLevel = null;
        rtfieldrequestParamsGlobalLevel.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
        rtfieldrequestParamsGlobalLevel.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Receive_Payment_ModuleId));
        HashMap<String, String> rtreplaceFieldMapGlobalLevel = new HashMap<String, String>();
        if (accAccountDAOobj != null) {
            rtFieldMapGlobalLevel = accAccountDAOobj.getFieldParamsCustomMap(rtfieldrequestParamsGlobalLevel, rtreplaceFieldMapGlobalLevel, rtcustomFieldMapGlobalLevel, rtcustomDateFieldMapGlobalLevel);
        }

        //Custom field details Maps for Line Level data
        HashMap<String, Object> rtfieldrequestParamsRows = new HashMap();
        HashMap<String, String> rtreplaceFieldMapRows = new HashMap();
        HashMap<String, String> rtcustomFieldMapRows = new HashMap();
        HashMap<String, String> rtcustomDateFieldMapRows = new HashMap();
        rtfieldrequestParamsRows.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
        rtfieldrequestParamsRows.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Receive_Payment_ModuleId));
        HashMap<String, Integer> rtfieldMapRows = null;
        if (accAccountDAOobj != null) {
            rtfieldMapRows = accAccountDAOobj.getFieldParamsCustomMapForRows(rtfieldrequestParamsRows, rtreplaceFieldMapRows, rtcustomFieldMapRows, rtcustomDateFieldMapRows);
        }

        //Custom Field for opening receipts
        HashMap<String, String> openingRTreplaceFieldMap = new HashMap<String, String>();
        HashMap<String, String> openingRTcustomFieldMap = new HashMap<String, String>();
        HashMap<String, String> openingRTcustomDateFieldMap = new HashMap<String, String>();
        HashMap<String, Integer> openingRTFieldMap = null;
        HashMap<String, Object> openingRTfieldrequestParams = new HashMap();
        openingRTfieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
        openingRTfieldrequestParams.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request), Constants.Acc_Receive_Payment_ModuleId));
        if (accAccountDAOobj != null) {
            openingRTFieldMap = accAccountDAOobj.getFieldParamsCustomMap(openingRTfieldrequestParams, openingRTreplaceFieldMap, openingRTcustomFieldMap, openingRTcustomDateFieldMap);
        }
        
        
        //Custom Field for opening invoices
        HashMap<String, String> openingInvreplaceFieldMap = new HashMap<String, String>();
        HashMap<String, String> openingInvcustomFieldMap = new HashMap<String, String>();
        HashMap<String, String> openingInvcustomDateFieldMap = new HashMap<String, String>();
        HashMap<String, Integer> openingInvFieldMap = null;
        HashMap<String, Object> openingInvfieldrequestParams = new HashMap();
        openingInvfieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
        openingInvfieldrequestParams.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request), Constants.Acc_Invoice_ModuleId));
        if (accAccountDAOobj != null) {
            openingInvFieldMap = accAccountDAOobj.getFieldParamsCustomMap(openingInvfieldrequestParams, openingInvreplaceFieldMap, openingInvcustomFieldMap, openingInvcustomDateFieldMap);
        }
        
        //Custom Field for opening CN
        HashMap<String, String> openingCNreplaceFieldMap = new HashMap<String, String>();
        HashMap<String, String> openingCNcustomFieldMap = new HashMap<String, String>();
        HashMap<String, String> openingCNcustomDateFieldMap = new HashMap<String, String>();
        HashMap<String, Integer> openingCNFieldMap = null;
        HashMap<String, Object> openingCNfieldrequestParams = new HashMap();
        openingCNfieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
        openingCNfieldrequestParams.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request), Constants.Acc_Credit_Note_ModuleId));
        if (accAccountDAOobj != null) {
            openingCNFieldMap = accAccountDAOobj.getFieldParamsCustomMap(openingCNfieldrequestParams, openingCNreplaceFieldMap, openingCNcustomFieldMap, openingCNcustomDateFieldMap);
        }
        
        //Custom Field for opening DN
        HashMap<String, String> openingDNreplaceFieldMap = new HashMap<String, String>();
        HashMap<String, String> openingDNcustomFieldMap = new HashMap<String, String>();
        HashMap<String, String> openingDNcustomDateFieldMap = new HashMap<String, String>();
        HashMap<String, Integer> openingDNFieldMap = null;
        HashMap<String, Object> openingDNfieldrequestParams = new HashMap();
        openingDNfieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
        openingDNfieldrequestParams.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request), Constants.Acc_Debit_Note_ModuleId));
        if (accAccountDAOobj != null) {
            openingDNFieldMap = accAccountDAOobj.getFieldParamsCustomMap(openingDNfieldrequestParams, openingDNreplaceFieldMap, openingDNcustomFieldMap, openingDNcustomDateFieldMap);
        }
            
            
        for (int i = 0; i < pagedJson.length(); i++) {
            JSONObject obj = pagedJson.getJSONObject(i);
            if(obj.optString("type","").equals("Sales Invoice")){
                String invId = obj.getString("billid");
                KwlReturnObject inv = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invId);
                Invoice invoice = (Invoice) inv.getEntityList().get(0); 
                if(invoice.isNormalInvoice()){
                    accInvoiceServiceDAO.getCustomFeild(obj, invoice, paramObj, invreplaceFieldMap, invcustomFieldMap, invcustomDateFieldMap, invfieldMap, invreplaceFieldMapRows, invcustomFieldMapRows, invcustomDateFieldMapRows, invfieldMapRows);
                }else{
                    AccInvoiceServiceHandler.getOpeningInvoiceCustomField(companyid, fieldDataManagercntrl, accJournalEntryobj, accountingHandlerDAOobj, request, invoice, openingInvreplaceFieldMap, openingInvcustomFieldMap, openingInvcustomDateFieldMap, openingInvFieldMap, obj);
                }
            }
            if(obj.optString("type","").equals("Credit Note")){
                String id = obj.getString("billid");
                CreditNote creditNote = (CreditNote)kwlCommonTablesDAOObj.getObject(CreditNote.class.getName(), id).getEntityList().get(0);
                if(creditNote.isNormalCN()){
                    AccInvoiceServiceHandler.getCreditNoteCustomField(companyid, fieldDataManagercntrl, accJournalEntryobj, accountingHandlerDAOobj, accCreditNoteDAOobj, request, creditNote, cnreplaceFieldMapRows, cncustomFieldMapRows, cncustomDateFieldMapRows, cnfieldMapRows, cnreplaceFieldMap, cncustomFieldMap, cncustomDateFieldMap, cnFieldMap, obj);
                }
                else{
                    AccInvoiceServiceHandler.getOpeningCreditNoteCustomField(companyid, fieldDataManagercntrl, accJournalEntryobj, request, creditNote, openingCNreplaceFieldMap, openingCNcustomFieldMap, openingCNcustomDateFieldMap, openingCNFieldMap, obj);
                }
            }
            if(obj.optString("type","").equals("Debit Note")){
                String id = obj.getString("billid");
                DebitNote debitNote = (DebitNote)kwlCommonTablesDAOObj.getObject(DebitNote.class.getName(), id).getEntityList().get(0);
                if(debitNote.isNormalDN()){
                    AccInvoiceServiceHandler.getDebitNoteCustomField(companyid, fieldDataManagercntrl, accJournalEntryobj, accountingHandlerDAOobj, accCreditNoteDAOobj, request, debitNote, dnreplaceFieldMapRows, dncustomFieldMapRows, dncustomDateFieldMapRows, dnfieldMapRows, dnreplaceFieldMap, dncustomFieldMap, dncustomDateFieldMap, dnFieldMap, obj);
                }
                else{
                    AccInvoiceServiceHandler.getOpeningDebitNoteCustomField(companyid, fieldDataManagercntrl, accJournalEntryobj, request, debitNote, openingDNreplaceFieldMap, openingDNcustomFieldMap, openingDNcustomDateFieldMap, openingDNFieldMap, obj);
                }
            }
            if(obj.optString("type","").equals("Payment Received")){
                String id = obj.getString("billid");
                Receipt receipt = (Receipt)kwlCommonTablesDAOObj.getObject(Receipt.class.getName(), id).getEntityList().get(0);
                if(receipt.isNormalReceipt()){
                    AccInvoiceServiceHandler.getReceiptCustomField(companyid, fieldDataManagercntrl, accJournalEntryobj, accountingHandlerDAOobj, accReceiptobj, request, receipt, rtcustomFieldMapGlobalLevel, rtcustomDateFieldMapGlobalLevel, rtFieldMapGlobalLevel, rtreplaceFieldMapGlobalLevel, rtreplaceFieldMapRows, rtcustomFieldMapRows, rtcustomDateFieldMapRows, rtfieldMapRows, obj);
                }
                else{
                    AccInvoiceServiceHandler.getOpeningReceiptCustomField(companyid, fieldDataManagercntrl, accJournalEntryobj, request, receipt, openingRTreplaceFieldMap, openingRTcustomFieldMap, openingRTcustomDateFieldMap, openingRTFieldMap, obj);
                }
            }
        }
    }
    
}
