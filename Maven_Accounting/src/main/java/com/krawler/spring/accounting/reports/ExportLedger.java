/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.reports;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.invoice.InvoiceConstants;
import com.krawler.spring.accounting.invoice.accInvoiceCMN;
import com.krawler.spring.accounting.jasperreports.*;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import javax.mail.MessagingException;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.DateTime;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
/**
 *
 * @author krawler
 */
public class ExportLedger implements Runnable {

    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accAccountDAO accAccountDAOobj;
   private authHandlerDAO authHandlerDAOObj;
    private HibernateTransactionManager txnManager;
    private AccReportsService accReportsService;
    private Boolean isLedgerPrintCSV;
    boolean isGeneralLedger;
    private Company company;
    private accJournalEntryDAO accJournalEntryobj;
    private accInvoiceCMN accInvoiceCommon;
    private DateFormat dateFormat;
    private String filename;
    private accProductDAO accProductObj;
    ArrayList processQueue = new ArrayList();
    private boolean isworking = false;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private accTaxDAO accTaxObj;
    private accPaymentDAO accPaymentDAOobj;
    public void setAccAccountDAOobj(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
     public void setAuthHandlerDAOObj(authHandlerDAO authHandlerDAOObj) {
        this.authHandlerDAOObj = authHandlerDAOObj;
    }
    public void setAccCurrencyDAOobj(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }


    public void setAccReportsService(AccReportsService accReportsService) {
        this.accReportsService = accReportsService;
    }

    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }
    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }
    
    public void setaccPaymentDAO(accPaymentDAO accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }
    public void setCompany(Company company) {
        this.company = company;
    }

    public void setIsGeneralLedger(boolean isGeneralLedger) {
        this.isGeneralLedger = isGeneralLedger;
    }

    public void setIsLedgerPrintCSV(Boolean isLedgerPrintCSV) {
        this.isLedgerPrintCSV = isLedgerPrintCSV;
    }


    public void setAccInvoiceCommon(accInvoiceCMN accInvoiceCommon) {
        this.accInvoiceCommon = accInvoiceCommon;
    }

    public void setAccJournalEntryobj(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }

    public void setDateFormat(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    public void setAccProductObj(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }

    public void add(HashMap<String, Object> requestParams) {
        try {
            processQueue.add(requestParams);
        } catch (Exception ex) {
            Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void SendMail(HashMap requestParams, String path,String fileType) throws ServiceException {


        String loginUserId = (String) requestParams.get("userid");

        KwlReturnObject KWLUser = accountingHandlerDAOobj.getObject(User.class.getName(), loginUserId);
        User user = (User) KWLUser.getEntityList().get(0);
        KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), user.getCompany().getCompanyID());
        Company company = (Company) returnObject.getEntityList().get(0);
        String sendorInfo = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
        String fileName = filename;
//        String exportFileType = "csv";//(String) requestParams.get("filetype");
        fileName = fileName + "." + fileType;

        String cEmail = user.getEmailID() != null ? user.getEmailID() : "";
        if (!StringUtil.isNullOrEmpty(cEmail)) {
            try {
                String subject = "General Ledger Report Export Status";
                //String sendorInfo = "admin@deskera.com";
                String htmlTextC = "";
                htmlTextC += "<br/>Hello " + user.getFirstName() + "<br/>";
                htmlTextC += "<br/>General Ledger Report <b>\"" + fileName + "\"</b> has been generated successfully.<br/><br/> You can download it from <b> Export Details Report</b>.<br/>";

                htmlTextC += "<br/>Regards,<br/>";
                htmlTextC += "<br/>ERP System<br/>";

                String plainMsgC = "";
                plainMsgC += "\nHello " + user.getFirstName() + "\n";
                plainMsgC += "\nGeneral Ledger Report <b>\"" + fileName + "\"</b> has been generated successfully.<br/><br/> You can download it from <b> Export Details Report</b>.\n";
                plainMsgC += "\nRegards,\n";
                plainMsgC += "\nERP System\n";

                
                Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                try {
                    SendMailHandler.postMail(new String[]{cEmail}, subject, htmlTextC, plainMsgC, sendorInfo, new String[]{path}, smtpConfigMap);
                } catch (MessagingException ex1) {
                    Logger.getLogger(ExportLedger.class.getName()).log(Level.SEVERE, "ExportLedger.SendMail :" + ex1.getMessage(), ex1);
                    SendMailHandler.postMail(new String[]{cEmail}, subject, htmlTextC, plainMsgC, sendorInfo, smtpConfigMap);
                }finally {
                    System.out.println("Mail Catch-1 Completed: " + new Date());
                }
                
            } catch (Exception ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }finally{
                System.out.println("Mail Catch-2 Completed: "+new Date());
            }
        }

    }

    @Override
    public void run() {
        try {
            while (!processQueue.isEmpty() && !isworking) {
                DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                def.setName("Account_Tx");
                def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
                TransactionStatus status = txnManager.getTransaction(def);
                isworking = true;
                HashMap<String, Object> requestParams = (HashMap<String, Object>) processQueue.get(0);
                Map<String, BillingInvoice> billingInvoiceMapGL = new HashMap<String, BillingInvoice>();
                Map<String, BillingGoodsReceipt> billingGrMapGL = new HashMap<String, BillingGoodsReceipt>();
                Map<String, Object[]> billingCreditNoteMapGL = new HashMap<String, Object[]>();
                Map<String, Object[]> billingDebitNoteMapGL = new HashMap<String, Object[]>();
                Map<String, Object[]> billingPaymentReceivedMapGL = new HashMap<String, Object[]>();
                Map<String, Object[]> billingPaymentMadeMapGL = new HashMap<String, Object[]>();
                Map<String, Invoice> invoiceMapGL = new HashMap<String, Invoice>();
                Map<String, GoodsReceipt> grMapGL = new HashMap<String, GoodsReceipt>();
                Map<String, Object[]> creditNoteMapGL = new HashMap<String, Object[]>();
                Map<String, Object[]> creditNoteMapVendorGL = new HashMap<String, Object[]>();
                Map<String, Object[]> debitNoteMapGL = new HashMap<String, Object[]>();
                Map<String, Object[]> debitNoteMapCustomerGL = new HashMap<String, Object[]>();
                Map<String, Object[]> paymentReceivedMapGL = new HashMap<String, Object[]>();
                Map<String, Object[]> paymentMadeMapGL = new HashMap<String, Object[]>();
                Map<String, GoodsReceipt> fixedAssetgrMapGL = new HashMap<String, GoodsReceipt>();
                Map<String, Invoice> fixedAssetInvoiceMapGL = new HashMap<String, Invoice>();
                Map<String, Invoice> cashSalesGL = new HashMap<String, Invoice>();
                Map<String, GoodsReceipt> cashPurchaseGL = new HashMap<String, GoodsReceipt>();
                Map<String, Integer> jeDetailPaymentTypeMapGL = new HashMap<String, Integer>();
                Map<String, Integer> jeDetailReceiptTypeMapGL = new HashMap<String, Integer>();
                FileOutputStream outputStream = null;
                try {
                    boolean showChildAccountsInGl = false;
                    if (requestParams.containsKey("showChildAccountsInGl") && requestParams.get("showChildAccountsInGl") != null) {//Check to show Child accounts.
                        showChildAccountsInGl = Boolean.parseBoolean(requestParams.get("showChildAccountsInGl").toString());
                    }
                    boolean includeExcludeChildBalances = false;
                    if (requestParams.containsKey("includeExcludeChildBalances") && requestParams.get("includeExcludeChildBalances") != null) {//Check to show Child accounts.
                        includeExcludeChildBalances = Boolean.parseBoolean(requestParams.get("includeExcludeChildBalances").toString());
                    }
                    HashMap<String, Object> reqParams = new HashMap<>();
                    for (Map.Entry e : requestParams.entrySet()) {
                        reqParams.put(e.getKey().toString(), e.getValue());
                    }
                    reqParams.put(Constants.REQ_startdate, requestParams.get("stdate").toString());
                    reqParams.put(Constants.REQ_enddate, requestParams.get("enddate").toString());
                    
                    reqParams.put("dateformat", dateFormat);
                    boolean consolidateFlag = (Boolean) requestParams.get("consolidateFlag");
                    String gcurrencyid = requestParams.get("currencyid").toString() ;
                    String companyid = requestParams.get("companyid").toString();
                    String Searchjson = "";
                    if(requestParams.containsKey(Constants.Acc_Search_Json) && !StringUtil.isNullOrEmpty((String)requestParams.get(Constants.Acc_Search_Json))){
                        Searchjson=(String) requestParams.get(Constants.Acc_Search_Json);
                    }
                    String filterCriteria = "";
                    if(requestParams.containsKey(Constants.Filter_Criteria) && !StringUtil.isNullOrEmpty((String)requestParams.get(Constants.Filter_Criteria))){
                        filterCriteria=(String) requestParams.get(Constants.Filter_Criteria);
                    }
                    String invoiceSearchJson = "";
                    String grSearchJson = "";
                    String cnSearchJson = "";
                    String dnSearchJson = "";
                    String paymentSearchJson = "";
                    String receiptSearchJson = "";
                    String fileType="csv";
                    if(requestParams.containsKey("filetype") && !StringUtil.isNullOrEmpty((String)requestParams.get("filetype"))){
                        fileType=(String)requestParams.get("filetype");
                    }
                    


                    Date requestTime = new Date();
//                    int exportStatus = 2;

//                    DateFormat sdfTemp = authHandler.getGlobalDateFormat();
                    SimpleDateFormat sdfTemp = new SimpleDateFormat("ddMMyyyy_hhmmssaa");
                    filename = "GeneralLedger_" + (sdfTemp.format(requestTime)).toString();
                    
                    HashMap<String, Object> exportDetails = new HashMap<String, Object>();
                    exportDetails.put("fileName", filename + "."+fileType);
                    exportDetails.put("requestTime", requestTime);
                    exportDetails.put("status", 1);
                    exportDetails.put("companyId", requestParams.get("companyid"));
                    exportDetails.put("fileType", fileType);
                    KwlReturnObject resultExportObj = accProductObj.saveProductExportDetails(exportDetails);
                    ProductExportDetail productExportDetail = (ProductExportDetail) resultExportObj.getEntityList().get(0);
                    
                    txnManager.commit(status);
                    status = txnManager.getTransaction(def);

                    JSONObject jobj = new JSONObject();
//                    boolean issuccess = true;
                    boolean noactivity = false;
//                    String msg = "";
//                    String view = "";
//                    String selectedBalPLId = "";
//                    double endingBalanceSummary = 0, openbalanceSummary = 0;
                    SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

                    ArrayList<GeneralLedger> generalLedgerList = new ArrayList<GeneralLedger>();
                    String selectedCurrencyIds = requestParams.get("currencyIds").toString();
                    String[] selectedCurrencyIdsArray = null;
                    if (!StringUtil.isNullOrEmpty(selectedCurrencyIds)) {
                        selectedCurrencyIdsArray = selectedCurrencyIds.split(",");
                    }
                    requestParams.put("exportGLCSV", true);
                    requestParams.put("includeExcludeChildBalances", includeExcludeChildBalances);
                    requestParams.put("generalLedgerFlag", isGeneralLedger);
                    requestParams.put("showChildAccountsInGl", showChildAccountsInGl);
                    requestParams.put("isGeneralLedger", isGeneralLedger);
                    KwlReturnObject result = accAccountDAOobj.getAccounts(requestParams);
                    List list = result.getEntityList();
                    jobj = getAccountJson(list, accCurrencyDAOobj, noactivity, requestParams);
                    JSONArray jSONArray = jobj.getJSONArray("data");

                    requestParams.put("dateformat", dateFormat);
                    requestParams.remove("ss");
                    
                    boolean excludePreviousYear = requestParams.get("excludePreviousYear") != null ? (Boolean)requestParams.get("excludePreviousYear") : false;
                    Date excludePreviousYearDate = requestParams.get("excludePreviousYearDate") != null ? (Date)requestParams.get("excludePreviousYearDate") : new Date(0);
                    Date start = new Date(1970);

                    Set<String> accountIdSet = new HashSet<String>();
//                    if (!StringUtil.isNullOrEmpty(Searchjson) && !StringUtil.isNullOrEmpty(filterCriteria)) {
//                        reqParams.put("isIAF", true);
//                        HashMap<String, Object> reqPar1 = new HashMap<String, Object>();
//                        reqPar1.put("companyid", companyid);
//                        reqPar1.put(Constants.Acc_Search_Json, Searchjson);
//                        reqPar1.put(Constants.Filter_Criteria, filterCriteria);
//
//                        reqPar1.put("moduleid", Constants.Acc_Invoice_ModuleId);
//                        invoiceSearchJson = accReportsService.getSearchJsonByModule(reqPar1);
//
//                        reqPar1.remove("moduleid");
//                        reqPar1.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);
//                        grSearchJson = accReportsService.getSearchJsonByModule(reqPar1);
//
//                        reqPar1.remove("moduleid");
//                        reqPar1.put("moduleid", Constants.Acc_Credit_Note_ModuleId);
//                        cnSearchJson = accReportsService.getSearchJsonByModule(reqPar1);
//
//                        reqPar1.remove("moduleid");
//                        reqPar1.put("moduleid", Constants.Acc_Debit_Note_ModuleId);
//                        dnSearchJson = accReportsService.getSearchJsonByModule(reqPar1);
//
//                        reqPar1.remove("moduleid");
//                        reqPar1.put("moduleid", Constants.Acc_Make_Payment_ModuleId);
//                        paymentSearchJson = accReportsService.getSearchJsonByModule(reqPar1);
//
//                        reqPar1.remove("moduleid");
//                        reqPar1.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
//                        receiptSearchJson = accReportsService.getSearchJsonByModule(reqPar1);
//                    }
                    if (!consolidateFlag) {
//                        HashMap<String, Object> reqParams1 = new HashMap<String, Object>();
//                        HashMap<String, Object> reqParams2 = new HashMap<String, Object>();

                        reqParams.put("companyid", companyid);
                        reqParams.put("gcurrencyid", gcurrencyid);
                        reqParams.put(Constants.Filter_Criteria, filterCriteria);

//                        billingInvoiceMapGL = accReportsService.getBillingInvoiceMap(reqParams);
//                        billingGrMapGL = accReportsService.getBillingGoodsReceiptMap(reqParams);
//                        billingCreditNoteMapGL = accReportsService.getBillingCreditNoteMap(reqParams);
//                        billingDebitNoteMapGL = accReportsService.getBillingDebitNoteMap(reqParams);
//                        billingPaymentReceivedMapGL = accReportsService.getBillingPaymentReceivedMap(reqParams);
//                        billingPaymentMadeMapGL = accReportsService.getBillingPaymentMadeMap(reqParams);
                        // reqParams.put("")
                        // reqParams.put("isFixedAsset", true);
                        if (!StringUtil.isNullOrEmpty(Searchjson)) {
                            reqParams.put(Constants.Acc_Search_Json, invoiceSearchJson);
                        }
//                        invoiceMapGL = accReportsService.getInvoiceMapNew(reqParams);
//                        reqParams1.putAll(reqParams);
//                        reqParams1.put("isFixedAsset", true);
//                        reqParams2.putAll(reqParams);
//                        reqParams2.put("cashonly", "true");
//                        fixedAssetInvoiceMapGL = accReportsService.getInvoiceMapNew(reqParams1);
//                        cashSalesGL = accReportsService.getInvoiceMapNew(reqParams2);
//                        if (!StringUtil.isNullOrEmpty(Searchjson)) {
//                            reqParams.remove(Constants.Acc_Search_Json);
//                            reqParams.put(Constants.Acc_Search_Json, grSearchJson);
//                        }
//                        grMapGL = accReportsService.getGoodsReceiptMapNew(reqParams);
//                        reqParams1.clear();
//                        reqParams2.clear();
//                        reqParams1.putAll(reqParams);
//                        reqParams1.put("isFixedAsset", true);
//                        reqParams2.putAll(reqParams);
//                        reqParams2.put("cashonly", "true");
//                        fixedAssetgrMapGL = accReportsService.getGoodsReceiptMapNew(reqParams1);
//                        cashPurchaseGL = accReportsService.getGoodsReceiptMapNew(reqParams2);


                        if (!StringUtil.isNullOrEmpty(Searchjson)) {
                            reqParams.remove(Constants.Acc_Search_Json);
                            reqParams.put(Constants.Acc_Search_Json, cnSearchJson);
                        }
//                        creditNoteMapGL = accReportsService.getCreditNoteMap(reqParams);
//                        creditNoteMapVendorGL = accReportsService.creditNoteMapVendor(reqParams);

                        if (!StringUtil.isNullOrEmpty(Searchjson)) {
                            reqParams.remove(Constants.Acc_Search_Json);
                            reqParams.put(Constants.Acc_Search_Json, dnSearchJson);
                        }
//                        debitNoteMapGL = accReportsService.getDebitNoteMap(reqParams);
//                        debitNoteMapCustomerGL = accReportsService.debitNoteMapCustomer(reqParams);

                        if (!StringUtil.isNullOrEmpty(Searchjson)) {
                            reqParams.remove(Constants.Acc_Search_Json);
                            reqParams.put(Constants.Acc_Search_Json, receiptSearchJson);
                        }
//                        paymentReceivedMapGL = accReportsService.getPaymentReceivedMap(reqParams);

                        if (!StringUtil.isNullOrEmpty(Searchjson)) {
                            reqParams.remove(Constants.Acc_Search_Json);
                            reqParams.put(Constants.Acc_Search_Json, paymentSearchJson);
                        }
//                        paymentMadeMapGL = accReportsService.getPaymentMadeMap(reqParams);

//                        invoiceMapGL.putAll(cashSalesGL);
//                        grMapGL.putAll(cashPurchaseGL);
//                        for (String jeid : paymentMadeMapGL.keySet()) {
//                            if (!StringUtil.isNullOrEmpty(jeid)) {
//                                Payment tempp = (Payment) paymentMadeMapGL.get(jeid)[0];
//                                if (tempp != null) {
//                                    accReportsService.createJEDetailPaymentTypeMapNew(tempp, jeDetailPaymentTypeMapGL, companyid);
//                                }
//                            }
//                        }
//                        for (String jeid : paymentReceivedMapGL.keySet()) {
//                            if (!StringUtil.isNullOrEmpty(jeid)) {
//                                Receipt tempr = (Receipt) paymentReceivedMapGL.get(jeid)[0];
//                                if (tempr != null) {
//                                    accReportsService.createJEDetailReceiptTypeMapNew(tempr, jeDetailReceiptTypeMapGL, companyid);
//                                }
//                            }
//                        }
                    }
                    StringBuilder reportSB = new StringBuilder();
                    String filePath= StorageHandler.GetDocStorePath() + filename + ".csv" ;
                    JSONObject params = new JSONObject();
                    for (Map.Entry e : requestParams.entrySet()) {
                        params.put(e.getKey().toString(), e.getValue());
                    }
                    JSONObject globalParams = new JSONObject();
                    globalParams.put(Constants.companyKey, companyid);
                    globalParams.put(Constants.globalCurrencyKey, gcurrencyid);
                    globalParams.put(Constants.df, df);
                    globalParams.put(Constants.userdf, requestParams.get(Constants.userdf));
                    params.put(Constants.GLOBAL_PARAMS, globalParams);
                    boolean headersFlag = false;
                    reportSB.append("\"Account Code\",\"Alias Code\",\"Account Name\",\"Type\",\"Date\",\"JE Number\",\"Number\",\"Name(Payer/Payee)\""
                            + ",\"Double Entry Movement\",\"Memo\",\"Description\",\"Exchange Rate\",\"Opening Balance\",\"Debit\",\"Credit\",\"Balance\"\n");
//                    for (int count = 0; count < 5; count++) {
                    for (int count = 0; count < jSONArray.length(); count++) {
                        if (jSONArray.getJSONObject(count).has("accid") && !accountIdSet.contains(jSONArray.getJSONObject(count).getString("accid")) ) {

                            String acccode = "";
                            String aliascode = "";
                            String accname = "";
                            String accId = jSONArray.getJSONObject(count).getString("accid");
                            String accounttypestring = jSONArray.getJSONObject(count).getString("accounttypestring");

                            accountIdSet.add(accId);
//                            System.out.print(accId + " accId \n");
//                            System.out.print(accId + " " + count);
                            KwlReturnObject accountResult = accountingHandlerDAOobj.getObject("com.krawler.hql.accounting.Account", accId);
                            KwlReturnObject custResult = accountingHandlerDAOobj.getObject("com.krawler.hql.accounting.Customer", accId);
                            KwlReturnObject venResult = accountingHandlerDAOobj.getObject("com.krawler.hql.accounting.Vendor", accId);
                            if (custResult.getEntityList().get(0) != null || venResult.getEntityList().get(0) != null) {
                                jSONArray.getJSONObject(count).put("isOnlyAccount", "false");
                            } else {
                                jSONArray.getJSONObject(count).put("isOnlyAccount", "true");
                            }
                            if (!noactivity) {
                                if (isGeneralLedger) {
//                                    Date startDateMinOne = new Date();
                                    Date startDate = new Date(0);
                                    Date endDate = new Date();
                                    String sDate = requestParams.get("startDate").toString();
                                    String eDate = requestParams.get("endDate").toString();
                                    double openingBalance = 0d, calculateOpeningBalance = 0, calculateOpenbalanceInbase = 0d, openbalanceInbase = 0d, endingBalance = 0d, calculatePeriodBalance = 0d, periodBalance = 0d;
                                    if (!StringUtil.isNullOrEmpty(sDate)) {
                                        startDate = dateFormat.parse(sDate);
                                    }
                                    if (!StringUtil.isNullOrEmpty(eDate)) {
                                        endDate = dateFormat.parse(eDate);
                                    }
                                    if (excludePreviousYear && accounttypestring.equals(Group.ACC_TYPE_PROFITLOSSSTR)) {
                                        start = excludePreviousYearDate;
                                    } else {
                                        start = new Date(0);
                                    }                                    
                                    Date openBalEndDate = new DateTime(startDate).minusDays(1).toDate();
//                                    Calendar startCal = Calendar.getInstance();
//                                    startCal.setTime(startDate);
//                                    startCal.add(Calendar.DATE, -1);
//                                    startDateMinOne = startCal.getTime();
                                    if (accountResult.getEntityList().get(0) != null) {
                                        Account account = (Account) accountResult.getEntityList().get(0);
                                        acccode = account.getAcccode() != null ? account.getAcccode() : "";
//                                        System.out.println("Account: " + count + ", Name: " + account.getAccountName() + ", Time: " + new Date());
                                        aliascode = account.getAliascode() != null ? account.getAliascode() : "";
                                        accname = account.getName() != null ? account.getName() : "";
                                        KwlReturnObject resultChild = accAccountDAOobj.getAccountChilds(account);
                                        List childlist = resultChild.getEntityList();
                                        //List childlist = new ArrayList(account.getChildren());
                                        if (!account.isDeleted()) {
                                            openingBalance = jSONArray.getJSONObject(count).getDouble("openbalance");
                                            openbalanceInbase = jSONArray.getJSONObject(count).getDouble("openbalanceinbase");

                                            boolean currencyFlag = false;
                                            if (selectedCurrencyIdsArray != null) {
                                                for (String obj : selectedCurrencyIdsArray) {
                                                    if (account.getCurrency().getCurrencyID().equals(obj)) {
                                                        currencyFlag = false;
                                                        break;
                                                    } else {
                                                        currencyFlag = true;
                                                    }
                                                }
                                            }
                                            if (childlist.isEmpty()) {
                                                periodBalance = accReportsService.getAccountBalance(params, account.getID(), startDate, endDate,null);
                                                calculateOpeningBalance = getAccountBalanceInOriginalCurrency(requestParams, account.getID(), excludePreviousYear ? start : new Date(0), openBalEndDate) + openingBalance;
                                                calculateOpenbalanceInbase = accReportsService.getAccountBalanceWithOutClosing(params, account.getID(), excludePreviousYear ? start : new Date(0), openBalEndDate,null) + openbalanceInbase;
                                                calculatePeriodBalance = periodBalance;
                                                if (account.getCreationDate().compareTo(startDate) < 0) {
                                                    calculateOpeningBalance -= openingBalance;
                                                    calculateOpenbalanceInbase -= openbalanceInbase;
                                                } else if (account.getCreationDate().compareTo(startDate) == 0) {
                                                    calculateOpeningBalance -= openingBalance;
                                                    calculateOpenbalanceInbase -= openbalanceInbase;
                                                    if (currencyFlag) {
                                                        calculatePeriodBalance = periodBalance;
                                                    } else {
                                                        calculatePeriodBalance = periodBalance - openbalanceInbase;
                                                    }
                                                } else if (account.getCreationDate().compareTo(startDate) > 0 && account.getCreationDate().compareTo(endDate) < 0) {
                                                    if (currencyFlag) {
                                                        calculateOpeningBalance -= openingBalance;
                                                        calculateOpenbalanceInbase -= openbalanceInbase;
                                                    } else if (account.getCreationDate().compareTo(startDate) == 0) {
                                                        calculateOpeningBalance -= openingBalance;
                                                        calculateOpenbalanceInbase -= openbalanceInbase;
                                                        if (currencyFlag) {
                                                            calculatePeriodBalance = periodBalance;
                                                        } else {
                                                            calculatePeriodBalance = periodBalance - openbalanceInbase;
                                                        }
                                                    } else if (account.getCreationDate().compareTo(startDate) > 0 && account.getCreationDate().compareTo(endDate) < 0) {
                                                        if (currencyFlag) {
                                                            calculateOpeningBalance -= openingBalance;
                                                            calculateOpenbalanceInbase -= openbalanceInbase;
                                                            calculatePeriodBalance = periodBalance;
                                                        } else {
                                                            calculatePeriodBalance = periodBalance - openbalanceInbase;
                                                            calculateOpenbalanceInbase -= openbalanceInbase;
                                                        }
                                                    }
                                                }
                                                endingBalance = calculateOpenbalanceInbase + calculatePeriodBalance;
                                            } else {
                                                
//                                            if (isGeneralLedger && !StringUtil.isNullOrEmpty(request.getParameter(Constants.Acc_Search_Json))) {
//                                                /*
//                                                    code in this if is done for:-
//                                                    * 1)do not include opening balance of account
//                                                    * 2) do not include opening trasactions amount of customer/vendor mapped with this current account when advanced search is performed on dimension
//                                                    * 3)when advanced search is performed on dimension then  documents are  considered for calculation are - saved with that dimension.
//                                                 */
//                                                openingBalance = 0;
//                                                openbalanceInbase = 0;
//                                            }
                                                //double periodAccountBalance = accReportsService.getAccountBalanceWithOutClosing(request, account.getID(), startDate, endDate);
                                                if (showChildAccountsInGl) {
                                                    if (includeExcludeChildBalances) {
                                                        params.put("stdate", sDate);
                                                        params.put("isPeriod", true);
                                                        //Calculate parent period balance
                                                        periodBalance = getAccountBalance(params, account.getID(), startDate, endDate);
                                                        //Calculate child period balance
                                                        periodBalance = accReportsService.getParentOpeningBalance(account, periodBalance, params, startDate, endDate);
                                                        calculateOpeningBalance = openingBalance;//getOpeningBalance+openingBalance;
                                                        params.put("isPeriod", false);
                                                        calculateOpenbalanceInbase = getAccountBalance(params, account.getID(), excludePreviousYear ? start : new Date(0), openBalEndDate);
                                                        calculateOpenbalanceInbase = accReportsService.getParentOpeningBalance(account, calculateOpenbalanceInbase, params, excludePreviousYear ? start : new Date(0), openBalEndDate);
                                                        calculatePeriodBalance = periodBalance;
                                                    } else {
                                                        periodBalance = accReportsService.getAccountBalance(params, account.getID(), startDate, endDate,null);
                                                        calculateOpeningBalance = getAccountBalanceInOriginalCurrency(requestParams, account.getID(), excludePreviousYear ? start : new Date(0), openBalEndDate) + openingBalance;
                                                        calculateOpenbalanceInbase = accReportsService.getAccountBalanceWithOutClosing(params, account.getID(), excludePreviousYear ? start : new Date(0), openBalEndDate,null) + openbalanceInbase;
                                                        calculatePeriodBalance = periodBalance;
                                                        if (account.getCreationDate().compareTo(startDate) < 0) {
                                                            calculateOpeningBalance -= openingBalance;
                                                            calculateOpenbalanceInbase -= openbalanceInbase;
                                                        } else if (account.getCreationDate().compareTo(startDate) == 0) {
                                                            calculateOpeningBalance -= openingBalance;
                                                            calculateOpenbalanceInbase -= openbalanceInbase;
                                                            if (currencyFlag) {
                                                                calculatePeriodBalance = periodBalance;
                                                            } else {
                                                                calculatePeriodBalance = periodBalance - openbalanceInbase;
                                                            }
                                                        } else if (account.getCreationDate().compareTo(startDate) > 0 && account.getCreationDate().compareTo(endDate) < 0) {
                                                            if (currencyFlag) {
                                                                calculateOpeningBalance -= openingBalance;
                                                                calculateOpenbalanceInbase -= openbalanceInbase;
                                                            } else if (account.getCreationDate().compareTo(startDate) == 0) {
                                                                calculateOpeningBalance -= openingBalance;
                                                                calculateOpenbalanceInbase -= openbalanceInbase;
                                                                if (currencyFlag) {
                                                                    calculatePeriodBalance = periodBalance;
                                                                } else {
                                                                    calculatePeriodBalance = periodBalance - openbalanceInbase;
                                                                }
                                                            } else if (account.getCreationDate().compareTo(startDate) > 0 && account.getCreationDate().compareTo(endDate) < 0) {
                                                                if (currencyFlag) {
                                                                    calculateOpeningBalance -= openingBalance;
                                                                    calculateOpenbalanceInbase -= openbalanceInbase;
                                                                    calculatePeriodBalance = periodBalance;
                                                                } else {
                                                                    calculatePeriodBalance = periodBalance - openbalanceInbase;
                                                                    calculateOpenbalanceInbase -= openbalanceInbase;
                                                                }
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    if (includeExcludeChildBalances) {
//                                        request.setAttribute("stdate", sDate);
//                                        request.setAttribute("enddate", eDate);
                                                        params.put("isPeriod", true);
                                                        //Calculate parent period balance
                                                        periodBalance = getAccountBalance(params, account.getID(), startDate, endDate);
                                                        //Calculate child period balance
                                                        periodBalance = accReportsService.getParentOpeningBalance(account, periodBalance, params, startDate, endDate);

//                                        double accountOpenBalanceInOriginalCurr = getAccountBalanceInOriginalCurrency(request, account.getID(), new Date(0), startDate);
//                                        double getOpeningBalance = accReportsService.getTotalAccountBalance(account, accountOpenBalanceInOriginalCurr, request);
                                                        calculateOpeningBalance = openingBalance;//getOpeningBalance+openingBalance;

//                                        double accountbalance = accReportsService.getAccountBalanceWithOutClosing(request, account.getID(), new Date(0), startDate);
//                                        double getOpenbalanceInbase = accReportsService.getTotalAccountBalance(account, accountbalance, request);
                                                        params.put("isPeriod", false);
                                                        calculateOpenbalanceInbase = getAccountBalance(params, account.getID(), excludePreviousYear ? start : new Date(0), openBalEndDate);
                                                        calculateOpenbalanceInbase = accReportsService.getParentOpeningBalance(account, calculateOpenbalanceInbase, params, excludePreviousYear ? start : new Date(0), openBalEndDate);

                                                        //*calculateOpenbalanceInbase = openbalanceInbase;
//                                        if (account.getCreationDate().compareTo(startDate) < 0) {
//                                            calculateOpenbalanceInbase = getOpenbalanceInbase+openbalanceInbase;
//                                            calculateOpeningBalance -= openingBalance;
//                                            calculateOpenbalanceInbase -= openbalanceInbase;
//                                            calculatePeriodBalance = periodBalance - calculateOpenbalanceInbase;
//                                        }  else if(account.getCreationDate().compareTo(startDate)==0){
//                                            calculateOpeningBalance -= openingBalance;
//                                            if (currencyFlag) {
//                                                calculateOpenbalanceInbase = getOpenbalanceInbase;
//                                                calculateOpenbalanceInbase -= periodBalance;
//                                                calculatePeriodBalance = periodBalance;
//                                            } else {
//                                                    calculateOpenbalanceInbase = getOpenbalanceInbase+openbalanceInbase;
//                                                calculatePeriodBalance = periodBalance - openbalanceInbase;
//                                                calculateOpenbalanceInbase -= periodBalance;
//                                            }
//                                        } else if(account.getCreationDate().compareTo(startDate)>0 && account.getCreationDate().compareTo(endDate)<0) {
//                                            if(currencyFlag){
//                                                calculateOpenbalanceInbase = getOpenbalanceInbase;
//                                                calculateOpeningBalance -= openingBalance;
//                                                calculateOpenbalanceInbase -= periodBalance;
//                                                calculatePeriodBalance = periodBalance;
//                                            }else{
//                                                calculateOpenbalanceInbase = getOpenbalanceInbase+openbalanceInbase;
//                                                calculatePeriodBalance = periodBalance - openbalanceInbase;
//                                                calculateOpenbalanceInbase -= periodBalance;
//                                            }
//                                        }
                                                        calculatePeriodBalance = periodBalance;
//                                            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.Acc_Search_Json))) { // ERP-11394 reset opening balance in case of advance serach
//                                                calculateOpenbalanceInbase = 0;
//                                            }
                                                    } else {
                                                        periodBalance = accReportsService.getAccountBalance(params, account.getID(), startDate, endDate,null);
                                                        calculateOpeningBalance = getAccountBalanceInOriginalCurrency(requestParams, account.getID(), excludePreviousYear ? start : new Date(0), openBalEndDate) + openingBalance;
                                                        calculateOpenbalanceInbase = accReportsService.getAccountBalanceWithOutClosing(params, account.getID(), excludePreviousYear ? start : new Date(0), openBalEndDate,null) + openbalanceInbase;
                                                        calculatePeriodBalance = periodBalance;
                                                        if (account.getCreationDate().compareTo(startDate) < 0) {
                                                            calculateOpeningBalance -= openingBalance;
                                                            calculateOpenbalanceInbase -= openbalanceInbase;
                                                        } else if (account.getCreationDate().compareTo(startDate) == 0) {
                                                            calculateOpeningBalance -= openingBalance;
                                                            calculateOpenbalanceInbase -= openbalanceInbase;
                                                            if (currencyFlag) {
                                                                calculatePeriodBalance = periodBalance;
                                                            } else {
                                                                calculatePeriodBalance = periodBalance - openbalanceInbase;
                                                            }
                                                        } else if (account.getCreationDate().compareTo(startDate) > 0 && account.getCreationDate().compareTo(endDate) < 0) {
                                                            if (currencyFlag) {
                                                                calculateOpeningBalance -= openingBalance;
                                                                calculateOpenbalanceInbase -= openbalanceInbase;
                                                            } else if (account.getCreationDate().compareTo(startDate) == 0) {
                                                                calculateOpeningBalance -= openingBalance;
                                                                calculateOpenbalanceInbase -= openbalanceInbase;
                                                                if (currencyFlag) {
                                                                    calculatePeriodBalance = periodBalance;
                                                                } else {
                                                                    calculatePeriodBalance = periodBalance - openbalanceInbase;
                                                                }
                                                            } else if (account.getCreationDate().compareTo(startDate) > 0 && account.getCreationDate().compareTo(endDate) < 0) {
                                                                if (currencyFlag) {
                                                                    calculateOpeningBalance -= openingBalance;
                                                                    calculateOpenbalanceInbase -= openbalanceInbase;
                                                                    calculatePeriodBalance = periodBalance;
                                                                } else {
                                                                    calculatePeriodBalance = periodBalance - openbalanceInbase;
                                                                    calculateOpenbalanceInbase -= openbalanceInbase;
                                                                }
                                                            }
                                                        }
                                                    }
                                                    endingBalance = calculateOpenbalanceInbase + calculatePeriodBalance;
                                                }
                                            }
//                                            if (account.getParent() == null) {
//                                                openbalanceSummary += calculateOpenbalanceInbase;
//                                                endingBalanceSummary += endingBalance;
//                                            }
                                            }

                                        //if (( isLedgerPrintCSV && account.getParent() == null && childlist.isEmpty()) || isLedgerPrintCSV==false) {
                                        //request.setAttribute("accountid", accId);
                                        jobj = accReportsService.getLedgerForExport(requestParams, accId, billingInvoiceMapGL, billingGrMapGL, billingCreditNoteMapGL, billingDebitNoteMapGL, billingPaymentReceivedMapGL, billingPaymentMadeMapGL,
                                            invoiceMapGL, grMapGL, creditNoteMapGL, creditNoteMapVendorGL, debitNoteMapGL, debitNoteMapCustomerGL, paymentReceivedMapGL, paymentMadeMapGL,
                                            fixedAssetgrMapGL, fixedAssetInvoiceMapGL, jeDetailPaymentTypeMapGL,jeDetailReceiptTypeMapGL);
                                        boolean emptyTransactionFlag = false;
                                        double debitsum = 0;
                                        double creditsum = 0;
                                        double balance = 0;



                                        JSONArray objJSONArray = new JSONArray();

                                        if (jobj.has("data")) {
                                            objJSONArray = jobj.getJSONArray("data");
                                        }

//                                        if (isLedgerPrintCSV) {
                                            GeneralLedger generalLedger1 = new GeneralLedger();
                                            generalLedger1.setAcccode(acccode);
                                            generalLedger1.setAccname(accname);
                                            generalLedger1.setAccountGroupID(accId);
                                            if (!isLedgerPrintCSV) {
                                                generalLedger1.setAliascode(aliascode);
                                                generalLedger1.setClosing(endingBalance);//done for ERP-13428 closing balance is not displayed .
                                            }
                                            generalLedger1.setOpening(calculateOpenbalanceInbase);
                                            generalLedger1.setBalance(calculateOpenbalanceInbase);
                                                generalLedgerList.add(generalLedger1);
//                                        }

                                        double parentGrandTotalDebitSum = 0;
                                        double parentGrandTotalCreditSum = 0;
                                        boolean isParentAccount = false;
                                        boolean isParentAccountNotHaveAnyTransaction = true;
                                        HashMap<String, Double> balanceAmtMap = new HashMap<String, Double>();

                                        for (int count1 = 0; count1 < objJSONArray.length(); count1++) {
                                            if ((objJSONArray.length() == 1 || objJSONArray.length() == 2) && ((objJSONArray.getJSONObject(count1).getString("d_accountname")).equals("Opening Balance") || (objJSONArray.getJSONObject(count1).getString("d_accountname")).equals("Balance c/f") || (objJSONArray.getJSONObject(count1).getString("d_accountname")).equals("Balance b/d") || (objJSONArray.getJSONObject(count1).getString("c_accountname")).equals("Opening Balance") || (objJSONArray.getJSONObject(count1).getString("c_accountname")).equals("Balance c/f") || (objJSONArray.getJSONObject(count1).getString("c_accountname")).equals("Balance b/d"))) {
                                                emptyTransactionFlag = true;
                                            }
                                            if ((objJSONArray.getJSONObject(count1).getString("d_accountname")).equals("Opening Balance")
                                                    || (objJSONArray.getJSONObject(count1).getString("d_accountname")).equals("Balance c/f")
                                                    || (objJSONArray.getJSONObject(count1).getString("d_accountname")).equals("Balance b/d")
                                                    || (objJSONArray.getJSONObject(count1).getString("c_accountname")).equals("Opening Balance")
                                                    || (objJSONArray.getJSONObject(count1).getString("c_accountname")).equals("Balance c/f")
                                                    || (objJSONArray.getJSONObject(count1).getString("c_accountname")).equals("Balance b/d")
                                                    || !StringUtil.isNullOrEmpty(objJSONArray.getJSONObject(count1).optString("isnetbalance", ""))) {
                                                continue;
                                            } else {

                                                String transactionAccountId = objJSONArray.getJSONObject(count1).getString("accountid");

                                                if (accId.equals(transactionAccountId) || !isLedgerPrintCSV) {//ERP-8745,ERP-8700 in case of Child account transactions- transaction accountid will not be equal to parent(selected) account id--> so this indicates that child account transaction is beibg iterate and in CSV Export we don't need show child account transactions in front of parent, only child account transaction amount will be shown in Grand Total

                                                    GeneralLedger generalLedger = new GeneralLedger();
                                                    String transactionSymbol = "";
                                                    String transactionDateString = "";
                                                    double exchangeRateAmount = 0.0;
                                                    generalLedger.setAcccode(acccode);
                                                    generalLedger.setAliascode(aliascode);
                                                    generalLedger.setAccname(accname);
                                                    generalLedger.setDate(!StringUtil.isNullOrEmpty(objJSONArray.getJSONObject(count1).getString("d_date")) ? df.format(dateFormat.parse(objJSONArray.getJSONObject(count1).getString("d_date"))) : !StringUtil.isNullOrEmpty(objJSONArray.getJSONObject(count1).getString("c_date")) ? df.format(dateFormat.parse(objJSONArray.getJSONObject(count1).getString("c_date"))) : "");
                                                    generalLedger.setVoucherno(!StringUtil.isNullOrEmpty(objJSONArray.getJSONObject(count1).getString("d_transactionID")) ? objJSONArray.getJSONObject(count1).getString("d_transactionID") : !StringUtil.isNullOrEmpty(objJSONArray.getJSONObject(count1).getString("c_transactionID")) ? objJSONArray.getJSONObject(count1).getString("c_transactionID") : "");
                                                    String name = !StringUtil.isNullOrEmpty(objJSONArray.getJSONObject(count1).getString("d_accountname")) ? objJSONArray.getJSONObject(count1).getString("d_accountname") : !StringUtil.isNullOrEmpty(objJSONArray.getJSONObject(count1).getString("c_accountname")) ? objJSONArray.getJSONObject(count1).getString("c_accountname") : "";
                                                    String desc = "";
                                                    Map<String, String> customFieldData = new HashMap<>();
                                                    Map<String, String> lineLevelCustomFieldData = new HashMap<>();                                                        
                                                    if (isLedgerPrintCSV) {
                                                        desc = !StringUtil.isNullOrEmpty(objJSONArray.getJSONObject(count1).optString("d_transactionDetailsForExpander", "")) ? objJSONArray.getJSONObject(count1).optString("d_transactionDetailsForExpander", "") : !StringUtil.isNullOrEmpty(objJSONArray.getJSONObject(count1).optString("c_transactionDetailsForExpander", "")) ? objJSONArray.getJSONObject(count1).optString("c_transactionDetailsForExpander", "") : "";
                                                    } else {
                                                        desc = !StringUtil.isNullOrEmpty(objJSONArray.getJSONObject(count1).optString("d_transactionDetails", "")) ? objJSONArray.getJSONObject(count1).optString("d_transactionDetails", "") : !StringUtil.isNullOrEmpty(objJSONArray.getJSONObject(count1).optString("c_transactionDetails", "")) ? objJSONArray.getJSONObject(count1).optString("c_transactionDetails", "") : "";
                                                    }
                                                    if (objJSONArray.getJSONObject(count1).has("d_transactionCustomFieldData") && objJSONArray.getJSONObject(count1).get("d_transactionCustomFieldData") != null && !StringUtil.isNullOrEmpty(objJSONArray.getJSONObject(count1).get("d_transactionCustomFieldData").toString())) {
                                                        try {
                                                            customFieldData = StringUtil.jsonStringtoMap(objJSONArray.getJSONObject(count1).get("d_transactionCustomFieldData").toString());
                                                        } catch (Exception ex) {
                                                            Logger.getLogger(ExportLedger.class.getName()).log(Level.SEVERE, null, ex);
                                                        }
                                                    } else if (objJSONArray.getJSONObject(count1).has("c_transactionCustomFieldData") && objJSONArray.getJSONObject(count1).get("c_transactionCustomFieldData") != null && !StringUtil.isNullOrEmpty(objJSONArray.getJSONObject(count1).get("c_transactionCustomFieldData").toString())) {
                                                        try {
                                                            customFieldData = StringUtil.jsonStringtoMap(objJSONArray.getJSONObject(count1).get("c_transactionCustomFieldData").toString());
                                                        } catch (Exception ex) {
                                                            Logger.getLogger(ExportLedger.class.getName()).log(Level.SEVERE, null, ex);
                                                        }
                                                    }
                                                    if (objJSONArray.getJSONObject(count1).has("d_lineLevelCustomFieldData") && objJSONArray.getJSONObject(count1).get("d_lineLevelCustomFieldData") != null && !StringUtil.isNullOrEmpty(objJSONArray.getJSONObject(count1).get("d_lineLevelCustomFieldData").toString())) {
                                                        try {
                                                            lineLevelCustomFieldData = StringUtil.jsonStringtoMap(objJSONArray.getJSONObject(count1).get("d_lineLevelCustomFieldData").toString());
                                                        } catch (Exception ex) {
                                                            Logger.getLogger(ExportLedger.class.getName()).log(Level.SEVERE, null, ex);
                                                        }
                                                    } else if (objJSONArray.getJSONObject(count1).has("c_lineLevelCustomFieldData") && objJSONArray.getJSONObject(count1).get("c_lineLevelCustomFieldData") != null && !StringUtil.isNullOrEmpty(objJSONArray.getJSONObject(count1).get("c_lineLevelCustomFieldData").toString())) {
                                                        try {
                                                            lineLevelCustomFieldData = StringUtil.jsonStringtoMap(objJSONArray.getJSONObject(count1).get("c_lineLevelCustomFieldData").toString());
                                                        } catch (Exception ex) {
                                                            Logger.getLogger(ExportLedger.class.getName()).log(Level.SEVERE, null, ex);
                                                        }
                                                    }
                                                    String memo = objJSONArray.getJSONObject(count1).optString("memoValue", "");
                                                    String type = !StringUtil.isNullOrEmpty(objJSONArray.getJSONObject(count1).optString("type", "")) ? objJSONArray.getJSONObject(count1).getString("type") : "";
                                                    String payer = !StringUtil.isNullOrEmpty(objJSONArray.getJSONObject(count1).optString("payer", "")) ? objJSONArray.getJSONObject(count1).getString("payer") : "";
                                                    String JENumber = !StringUtil.isNullOrEmpty(objJSONArray.getJSONObject(count1).optString("d_entryno", "")) ? objJSONArray.getJSONObject(count1).getString("d_entryno") : !StringUtil.isNullOrEmpty(objJSONArray.getJSONObject(count1).optString("c_entryno", "")) ? objJSONArray.getJSONObject(count1).getString("c_entryno") : "";
                                                    String gstCode = objJSONArray.getJSONObject(count1).optString("gstCode", "");
                                                    String costCenterName = objJSONArray.getJSONObject(count1).optString("costCenterName", "");
                                                    String salesPersonName = objJSONArray.getJSONObject(count1).optString("salesPersonName", "");

                                                    //                                        if(name.length()>255)
                                                    //                                            name = name.substring(0,254);
                                                    //                                       
                                                    //                                        if(desc.length()>255)
                                                    //                                            desc = desc.substring(0,254);
                                                    //                                       
                                                    //                                        if(memo.length()>255)
                                                    //                                            memo = memo.substring(0,254);

                                                    generalLedger.setName(name);
                                                    generalLedger.setType(type);
                                                    generalLedger.setMemo(memo);
                                                    generalLedger.setPayer(payer);
                                                    generalLedger.setDesc(desc);
                                                    generalLedger.setOnlydesc(desc);
                                                    generalLedger.setJEnumber(JENumber);
                                                    generalLedger.setCustomFieldData(customFieldData);
                                                    generalLedger.setLineLevelCustomFieldData(lineLevelCustomFieldData);
                                                    generalLedger.setGstCode(gstCode);
                                                    generalLedger.setCostCenterName(costCenterName);
                                                    generalLedger.setSalesPersonName(salesPersonName);
                                                            
                                                    if (!StringUtil.isNullOrEmpty(objJSONArray.getJSONObject(count1).optString("transactionCurrency", ""))) {
                                                        if (objJSONArray.getJSONObject(count1).getString("transactionCurrency").equals(company.getCurrency().getCurrencyID())) {
                                                            generalLedger.setErate("1");
							    generalLedger.setCurrencyName(!StringUtil.isNullOrEmpty(objJSONArray.getJSONObject(count1).getString("transactionSymbol")) ? objJSONArray.getJSONObject(count1).getString("transactionSymbol") : "");
                                                        } else {
                                                            if (objJSONArray.getJSONObject(count1).optDouble("d_amount", 0) > 0.0) {
                                                                transactionSymbol = !StringUtil.isNullOrEmpty(objJSONArray.getJSONObject(count1).getString("transactionSymbol")) ? objJSONArray.getJSONObject(count1).getString("transactionSymbol") : "";
                                                                transactionDateString = !StringUtil.isNullOrEmpty(objJSONArray.getJSONObject(count1).getString("transactionDateString")) ? objJSONArray.getJSONObject(count1).getString("transactionDateString") : "";
                                                                exchangeRateAmount = ((objJSONArray.getJSONObject(count1).getDouble("d_amount") / objJSONArray.getJSONObject(count1).getDouble("transactionAmount")) * 100 / 100);
                                                                generalLedger.setCurrencyName(transactionSymbol + "(" + transactionDateString + ")");
                                                                double externalcurrencyrate = objJSONArray.getJSONObject(count1).optDouble("d_externalcurrencyrate", 0);
                                                                if (externalcurrencyrate != 0) {
                                                                    externalcurrencyrate = 1 / externalcurrencyrate;
                                                                    externalcurrencyrate = (Math.round(externalcurrencyrate * Constants.ROUND_OFF_NUMBER)) / Constants.ROUND_OFF_NUMBER;
                                                                    generalLedger.setErate(externalcurrencyrate + "");
                                                                } else {
                                                                    generalLedger.setErate(exchangeRateAmount + "");
                                                                }
                                                            } else if (objJSONArray.getJSONObject(count1).optDouble("c_amount", 0) > 0.0) {
                                                                transactionSymbol = !StringUtil.isNullOrEmpty(objJSONArray.getJSONObject(count1).getString("transactionSymbol")) ? objJSONArray.getJSONObject(count1).getString("transactionSymbol") : "";
                                                                transactionDateString = !StringUtil.isNullOrEmpty(objJSONArray.getJSONObject(count1).getString("transactionDateString")) ? objJSONArray.getJSONObject(count1).getString("transactionDateString") : "";
                                                                exchangeRateAmount = ((objJSONArray.getJSONObject(count1).getDouble("c_amount") / objJSONArray.getJSONObject(count1).getDouble("transactionAmount")) * 100 / 100);
                                                                generalLedger.setCurrencyName(transactionSymbol + "(" + transactionDateString + ")");
                                                                double externalcurrencyrate = objJSONArray.getJSONObject(count1).optDouble("c_externalcurrencyrate", 0);
                                                                if (externalcurrencyrate != 0) {
                                                                    externalcurrencyrate = 1 / externalcurrencyrate;
                                                                    externalcurrencyrate = (Math.round(externalcurrencyrate * Constants.ROUND_OFF_NUMBER)) / Constants.ROUND_OFF_NUMBER;
                                                                    generalLedger.setErate(externalcurrencyrate + "");
                                                                } else {
                                                                    generalLedger.setErate(exchangeRateAmount + "");
                                                                }
                                                            }
                                                        }
                                                    }
                                                    debitsum = debitsum + objJSONArray.getJSONObject(count1).optDouble("d_amount", 0);
                                                    creditsum = creditsum + objJSONArray.getJSONObject(count1).optDouble("c_amount", 0);
                                                    balance = objJSONArray.getJSONObject(count1).optDouble("balanceAmount", 0);
                                                    balanceAmtMap.put(transactionAccountId, balance);
                                                    generalLedger.setDebit(objJSONArray.getJSONObject(count1).optDouble("d_amount", 0));
                                                    generalLedger.setCredit(objJSONArray.getJSONObject(count1).optDouble("c_amount", 0));
						    
                                                    //Credit / Debit Amount in Account Currency
                                                    generalLedger.setDebitAmtInAccCurrency(objJSONArray.getJSONObject(count1).optDouble("d_amountindocumentcurrency", 0));
                                                    generalLedger.setCreditAmtInAccCurrency(objJSONArray.getJSONObject(count1).optDouble("c_amountindocumentcurrency", 0));

                                                    generalLedger.setBalance(objJSONArray.getJSONObject(count1).optDouble("balanceAmount", 0));
                                                    generalLedger.setOpeningBalanceofAccount(calculateOpenbalanceInbase);
                                                    generalLedger.setOpening(0);
                                                    generalLedger.setClosing(endingBalance);
                                                    generalLedger.setPeriod(calculatePeriodBalance);
                                                    generalLedgerList.add(generalLedger);
                                                    isParentAccountNotHaveAnyTransaction = false;
                                                } else {
                                                    isParentAccount = true;
                                                    parentGrandTotalDebitSum += objJSONArray.getJSONObject(count1).optDouble("d_amount", 0);
                                                    parentGrandTotalCreditSum += objJSONArray.getJSONObject(count1).optDouble("c_amount", 0);
                                                    double parentBalance = objJSONArray.getJSONObject(count1).optDouble("balanceAmount", 0);;
                                                    balanceAmtMap.put(transactionAccountId, parentBalance);
                                                }
                                            }
                                        }
                                        if (isLedgerPrintCSV && isParentAccount && isParentAccountNotHaveAnyTransaction) {//ERP-8745,ERP-8700 in case of csv export if parent transaction does not have any transaction.
                                            GeneralLedger generalLedger = new GeneralLedger();
                                            generalLedger.setAcccode(acccode);
                                            generalLedger.setAliascode(aliascode);
                                            generalLedger.setAccname(accname);
                                            generalLedger.setDate("");
                                            generalLedger.setVoucherno("");
                                            generalLedger.setName("");
                                            generalLedger.setType("");
                                            generalLedger.setMemo("");
                                            generalLedger.setPayer("");
                                            generalLedger.setDesc("");
                                            generalLedger.setOnlydesc("");
                                            generalLedger.setJEnumber("");
                                            generalLedger.setErate("1");
                                            generalLedger.setDebit(0);
                                            generalLedger.setCredit(0);
                                            generalLedger.setBalance(0);
                                            generalLedger.setOpening(0);
                                            generalLedger.setClosing(endingBalance);
                                            generalLedger.setPeriod(calculatePeriodBalance);
                                            generalLedgerList.add(generalLedger);
                                        }

                                        if (isLedgerPrintCSV && objJSONArray.length() > 0 && !(creditsum == 0 && debitsum == 0 && balance == 0)) {
                                            GeneralLedger generalLedger = new GeneralLedger();
                                            generalLedger.setOnlydesc("Total");
                                            generalLedger.setCredit(creditsum);
                                            generalLedger.setDebit(debitsum);
                                            generalLedger.setOpening(calculateOpenbalanceInbase);
                                            generalLedger.setBalance(balance);
                                            generalLedgerList.add(generalLedger);
                                        }

                                        if (isParentAccount && isLedgerPrintCSV && (parentGrandTotalDebitSum != 0 || parentGrandTotalCreditSum != 0 || creditsum != 0 || debitsum != 0)) {//ERP-8745,ERP-8700 to show grand total for Parent Account
                                            double grandTotalBalance = 0;

                                            Set<String> keys = balanceAmtMap.keySet();
                                            for (String key : keys) {
                                                double bal = balanceAmtMap.get(key);
                                                grandTotalBalance += bal;
                                            }

                                            GeneralLedger generalLedger = new GeneralLedger();
                                            generalLedger.setOnlydesc("Grand Total");
                                            generalLedger.setCredit(parentGrandTotalCreditSum + creditsum);
                                            generalLedger.setDebit(parentGrandTotalDebitSum + debitsum);
                                            generalLedger.setOpening(calculateOpenbalanceInbase);
                                            generalLedger.setBalance(grandTotalBalance);
                                            generalLedgerList.add(generalLedger);
                                        }


                                        jSONArray.getJSONObject(count).remove("openbalance");
                                        jSONArray.getJSONObject(count).put("openbalance", calculateOpeningBalance);
                                        jSONArray.getJSONObject(count).remove("openbalanceinbase");
                                        jSONArray.getJSONObject(count).put("openbalanceinbase", calculateOpenbalanceInbase);
                                        jSONArray.getJSONObject(count).put("endingBalance", endingBalance);
                                        jSONArray.getJSONObject(count).put("periodBalance", calculatePeriodBalance);

                                        if (emptyTransactionFlag && !isLedgerPrintCSV) {// in case of PDF Export
                                            GeneralLedger generalLedger = new GeneralLedger();
                                            generalLedger.setAcccode(acccode);
                                            generalLedger.setAccname(accname);
                                            generalLedger.setDesc("No Transactions");
                                            generalLedger.setOpening(calculateOpeningBalance);
                                            generalLedger.setClosing(endingBalance);
                                            generalLedger.setPeriod(calculatePeriodBalance);
                                            generalLedgerList.add(generalLedger);
                                        }
                                        //}
                                    }
                                }
                            }
                        }
                        if(!StringUtil.isNullOrEmpty(fileType) && fileType.equals("csv")){
                            if (generalLedgerList.size() > Constants.THRESHOLD_VAL_TO_WRITE_INTO_CSV_FILE) {
                                if (!headersFlag) {
                                    outputStream = new FileOutputStream(filePath, true);
                                }
                                reportSB = getStringBuilderForGLExportToCsv(reportSB, generalLedgerList, companyid);
                                outputStream.write(reportSB.toString().getBytes());
                                headersFlag = true;
                                reportSB.setLength(0);
                                generalLedgerList.clear();
                            }
                        }
                       }
                    if (outputStream != null) {
                        outputStream.flush();
                        outputStream.close();
                    }
                    createGeneralLedgerCsvFile(generalLedgerList, productExportDetail, requestParams,headersFlag,fileType);
                    txnManager.commit(status);
                } catch (SessionExpiredException ex) {
                    Logger.getLogger(ExportLedger.class.getName()).log(Level.SEVERE, null, ex);
                    txnManager.rollback(status);
                } catch (ServiceException ex) {
                    Logger.getLogger(ExportLedger.class.getName()).log(Level.SEVERE, null, ex);
                    txnManager.rollback(status);
                } catch (JSONException ex) {
                    Logger.getLogger(ExportLedger.class.getName()).log(Level.SEVERE, null, ex);
                    txnManager.rollback(status);
                } catch (Exception ex) {
                    Logger.getLogger(ExportLedger.class.getName()).log(Level.SEVERE, null, ex);
                    txnManager.rollback(status);
                } finally {
                    processQueue.remove(requestParams);
                    isworking = false;
                    System.out.println("Done");
                    billingInvoiceMapGL.clear();
                    billingGrMapGL.clear();
                    billingCreditNoteMapGL.clear();
                    billingDebitNoteMapGL.clear();
                    billingPaymentReceivedMapGL.clear();
                    billingPaymentMadeMapGL.clear();
                    invoiceMapGL.clear();
                    grMapGL.clear();
                    creditNoteMapGL.clear();
                    creditNoteMapVendorGL.clear();
                    debitNoteMapGL.clear();
                    debitNoteMapCustomerGL.clear();
                    paymentReceivedMapGL.clear();
                    paymentMadeMapGL.clear();
                    fixedAssetgrMapGL.clear();
                    fixedAssetInvoiceMapGL.clear();
                    cashSalesGL.clear();
                    cashPurchaseGL.clear();
                    jeDetailPaymentTypeMapGL.clear();
                    jeDetailReceiptTypeMapGL.clear();
                    if (outputStream != null) {
                        outputStream.flush();
                        outputStream.close();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public StringBuilder getStringBuilderForGLExportToCsv(StringBuilder reportSB, ArrayList<GeneralLedger> generalLedgerList, String companyid) throws ServiceException, SessionExpiredException {
        try {
            for (GeneralLedger gl : generalLedgerList) {
                reportSB.append(StringUtil.isNullOrEmpty(gl.getAcccode()) ? "," : "\"" + gl.getAcccode() + "\",");
                reportSB.append(StringUtil.isNullOrEmpty(gl.getAliascode()) ? "," : "\"" + gl.getAliascode() + "\",");
                reportSB.append(StringUtil.isNullOrEmpty(gl.getAccname()) ? "," : "\"" + gl.getAccname() + "\",");
                reportSB.append(StringUtil.isNullOrEmpty(gl.getType()) ? "," : "\"" + gl.getType() + "\",");
                reportSB.append(StringUtil.isNullOrEmpty(gl.getDate()) ? "," : "\"" + gl.getDate() + "\",");
                reportSB.append(StringUtil.isNullOrEmpty(gl.getJEnumber()) ? "," : "\"" + gl.getJEnumber() + "\",");
                reportSB.append(StringUtil.isNullOrEmpty(gl.getVoucherno()) ? "," : "\"" + gl.getVoucherno() + "\",");
                reportSB.append(StringUtil.isNullOrEmpty(gl.getPayer()) ? "," : "\"" + gl.getPayer() + "\",");
                reportSB.append(StringUtil.isNullOrEmpty(gl.getName()) ? "," : "\"" + gl.getName() + "\",");
                reportSB.append(StringUtil.isNullOrEmpty(gl.getMemo()) ? "," : "\"" + gl.getMemo().replaceAll("\n", " ").replaceAll("\"", "\'") + "\",");
                reportSB.append(StringUtil.isNullOrEmpty(gl.getOnlydesc()) ? "," : "\"" + gl.getOnlydesc().replaceAll("\n", " ").replaceAll("\"", "\'") + "\",");
                reportSB.append(StringUtil.isNullOrEmpty(gl.getErate()) ? "," : "\"" + gl.getErate() + "\",");
                reportSB.append("\"" + authHandler.formattedCommaSeparatedUnitPrice(gl.getOpening(), companyid) + "\",");
                reportSB.append("\"" + authHandler.formattedCommaSeparatedUnitPrice(gl.getDebit(), companyid) + "\",");
                reportSB.append("\"" + authHandler.formattedCommaSeparatedUnitPrice(gl.getCredit(), companyid) + "\",");
                reportSB.append("\"" + authHandler.formattedCommaSeparatedUnitPrice(gl.getBalance(), companyid) + "\"\n");
            }

        } catch (Exception ex) {
            System.out.println("Exception in getStringForGeneralLedgerCsvFile : " + ex.getMessage());
        } finally {
            return reportSB;
        }
    }
    public void createGeneralLedgerCsvFile(ArrayList<GeneralLedger> generalLedgerList, ProductExportDetail productExportDetail, HashMap<String, Object> requestParams,boolean headersFlag,String fileType) throws ServiceException, SessionExpiredException {
        ByteArrayOutputStream os = null;
        try {

            String storePath = StorageHandler.GetDocStorePath();
//            File destDir = new File(storePath);
//            if (!destDir.exists()) {
//                destDir.mkdirs();
//            }
            String companyid = requestParams.get("companyid").toString();
            String filePath = storePath + filename + "."+fileType ;
            File destDir = new File(filePath);
             
            FileOutputStream oss = new FileOutputStream(destDir, true);
            if (!isLedgerPrintCSV) {
                FinanceDetails financeDetails = new FinanceDetails();
                Map<String, Object> financeDetailsMap = new HashMap<String, Object>();
                ArrayList<FinanceDetails> financeDetailsList = new ArrayList<FinanceDetails>();
                financeDetails.setName(company != null ? company.getCompanyName() : "");
                financeDetails.setEmail(company != null ? (company.getEmailID() != null ? company.getEmailID() : "") : "");
                financeDetails.setFax(company != null ? (company.getFaxNumber() != null ? company.getFaxNumber() : "") : "");
                financeDetails.setPhone(company != null ? (company.getPhoneNumber() != null ? company.getPhoneNumber() : "") : "");
                financeDetails.setCurrencyinword(company != null ? (company.getCurrency().getName() != null ? company.getCurrency().getName() : "") : "");
                if (requestParams.containsKey("dateRange") && !StringUtil.isNullOrEmpty((String) requestParams.get("dateRange"))) {
                    financeDetails.setDateRange((String) requestParams.get("dateRange"));
                }
                if (requestParams.containsKey("accountingperiod") && !StringUtil.isNullOrEmpty((String) requestParams.get("accountingperiod"))) {
                    financeDetails.setAccountigperiod((String) requestParams.get("accountingperiod"));
                }
                financeDetails.setReportname("General Ledger - Detailed");
                
                financeDetailsMap.put("GeneralLedgerSubReportData", new JRBeanCollectionDataSource(generalLedgerList));
                financeDetailsList.add(financeDetails);
                financeDetailsMap.put("datasource", new JRBeanCollectionDataSource(financeDetailsList));
                financeDetailsMap.put("format", "pdf");
                if (requestParams.containsKey("address") && !StringUtil.isNullOrEmpty((String) requestParams.get("address"))) {
                    financeDetailsMap.put("address", (String) requestParams.get("address"));
                }
                String jrxmlRealPath = (String) requestParams.get("jrxmlRealPath");
                boolean satsTemplateFlag = false;
                if (requestParams.containsKey("satsTemplateFlag") && !StringUtil.isNullOrEmpty(requestParams.get("satsTemplateFlag").toString())) {
                    satsTemplateFlag = Boolean.parseBoolean(requestParams.get("satsTemplateFlag").toString());
                }
                InputStream inputStream = new FileInputStream(jrxmlRealPath + "/GeneralLedger.jrxml");
                InputStream inputStreamSubReport = null;
                if (satsTemplateFlag) {
                    inputStreamSubReport = new FileInputStream(jrxmlRealPath + "/SATSGeneralLedgerSubReport.jrxml");
                } else {
                    inputStreamSubReport = new FileInputStream(jrxmlRealPath + "/GeneralLedgerSubReport.jrxml");
                }
                JasperDesign jasperDesign = JRXmlLoader.load(inputStream);
                JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
                JasperDesign jasperDesignSubReport = JRXmlLoader.load(inputStreamSubReport);
                JasperReport jasperReportSubReport = JasperCompileManager.compileReport(jasperDesignSubReport);
                financeDetailsMap.put("GeneralLedgerSubReport", jasperReportSubReport);
                JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(financeDetailsList);
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, financeDetailsMap, beanColDataSource);
                ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                JRPdfExporter exp = new JRPdfExporter();
                exp.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, baos1);
                exp.exportReport();
                oss.write(baos1.toByteArray());
                if(oss!=null){
                    oss.flush();
                    oss.close();
                }
            } else {
                if (!StringUtil.isNullOrEmpty(fileType) && fileType.equals("xls")) {
                    Workbook wb = null;
                    Sheet sheet = null;
                    Cell cell = null;
                    HashMap<String, Object> reqParams = new HashMap<String, Object>();
                    reqParams.put(Constants.filter_names, Arrays.asList(Constants.companyid,Constants.customfield,Constants.customcolumn));
                    reqParams.put(Constants.filter_values, Arrays.asList(companyid,0,0));
                    reqParams.put("isJETransactions", true);
                    reqParams.put("order_by", Arrays.asList("fieldlabel"));
                    reqParams.put("order_type", Arrays.asList("asc"));  
//                    String generalLedgerheader[] = {"Account Code", "Alias Code", "Account Name", "Type", "Date", "JE Number", "Number", "Name(Payer/Payee)", "Double Entry Movement", "Memo", "Description", "Currency","Exchange Rate", "Opening Balance", "Debit Amount", "Debit Amount in Base Currency", "Credit Amount", "Credit Amount in Base Currency", "Balance"};
                    
                    List<String> generalLedgerheader = new ArrayList<String>();
                    generalLedgerheader.add("Account Code");
                    generalLedgerheader.add("Alias Code");
                    generalLedgerheader.add("Account Name");
                    generalLedgerheader.add("Type");
                    generalLedgerheader.add("Date");
                    generalLedgerheader.add("JE Number");
                    generalLedgerheader.add("Number");
                    generalLedgerheader.add("Name(Payer/Payee)");
                    generalLedgerheader.add("Double Entry Movement");
                    generalLedgerheader.add("Memo");
                    generalLedgerheader.add("Description");
                    generalLedgerheader.add("Currency");
                    generalLedgerheader.add("Exchange Rate");
                    generalLedgerheader.add("Opening Balance");
                    generalLedgerheader.add("Debit Amount");
                    generalLedgerheader.add("Debit Amount in Base Currency");
                    generalLedgerheader.add("Credit Amount");
                    generalLedgerheader.add("Credit Amount in Base Currency");
                    generalLedgerheader.add("Balance");
                    generalLedgerheader.add("GST Code");
                    generalLedgerheader.add("Cost Center");
                    generalLedgerheader.add("Sales Person/Agent");
                    int defaultColumnsCnt = generalLedgerheader.size();
                    int rownum = 0;     //Row count
                    int cellnum = 0;    //Cell count
                    //Get all JE transaction distinct custom fields/ dimensions and put it as a header
                    KwlReturnObject result = accAccountDAOobj.getFieldParamsUsingSql(reqParams);
                    List list = result.getEntityList();
                    Iterator ite = list.iterator();
                    while (ite.hasNext()) {
                        Object[] temp = (Object[]) ite.next();
                        generalLedgerheader.add(temp[0].toString());
                    }
                    int columnsCnt = generalLedgerheader.size();
                    //Get all JE transaction distinct custom fields/ dimensions and put it as a header
                    reqParams.put(Constants.filter_names, Arrays.asList(Constants.companyid,Constants.customfield,Constants.customcolumn));
                    reqParams.put(Constants.filter_values, Arrays.asList(companyid,0,1));
                    result = accAccountDAOobj.getFieldParamsUsingSql(reqParams);
                    list = result.getEntityList();
                    Iterator ite1 = list.iterator();
                    while (ite1.hasNext()) {
                        Object[] temp = (Object[]) ite1.next();
                        generalLedgerheader.add(temp[0].toString());
                    }
                    
                    wb = new HSSFWorkbook();
                    sheet = wb.createSheet("Sheet-1");
                    Row headerRow = sheet.createRow(rownum++);
                    for (int header = 0; header < generalLedgerheader.size(); header++) {
                        cell = headerRow.createCell(cellnum++);  //Create new cell
                        cell.setCellValue(generalLedgerheader.get(header));
                    }
                    //HSSFRow row = sheet.createRow(rownum++);
                    for (GeneralLedger gl : generalLedgerList) {
                        cellnum = 0;
                        Row row = sheet.createRow(rownum++);
                        cell = row.createCell(cellnum++);  //Create new cell
                        cell.setCellValue(StringUtil.isNullOrEmpty(gl.getAcccode()) ? " " : gl.getAcccode());
                        cell = row.createCell(cellnum++);
                        cell.setCellValue(StringUtil.isNullOrEmpty(gl.getAliascode()) ? " " : gl.getAliascode());
                        cell = row.createCell(cellnum++);
                        cell.setCellValue(StringUtil.isNullOrEmpty(gl.getAccname()) ? " " : gl.getAccname());
                        cell = row.createCell(cellnum++);
                        cell.setCellValue(StringUtil.isNullOrEmpty(gl.getType()) ? " " : gl.getType());
                        cell = row.createCell(cellnum++);
                        cell.setCellValue(StringUtil.isNullOrEmpty(gl.getDate()) ? " " : gl.getDate());
                        cell = row.createCell(cellnum++);
                        cell.setCellValue(StringUtil.isNullOrEmpty(gl.getJEnumber()) ? " " : gl.getJEnumber());
                        cell = row.createCell(cellnum++);
                        cell.setCellValue(StringUtil.isNullOrEmpty(gl.getVoucherno()) ? " " : gl.getVoucherno());
                        cell = row.createCell(cellnum++);
                        cell.setCellValue(StringUtil.isNullOrEmpty(gl.getPayer()) ? " " : gl.getPayer());
                        cell = row.createCell(cellnum++);
                        cell.setCellValue(StringUtil.isNullOrEmpty(gl.getName()) ? " " : gl.getName());
                        cell = row.createCell(cellnum++);
                        cell.setCellValue(StringUtil.isNullOrEmpty(gl.getMemo()) ? " " : gl.getMemo().replaceAll("\n", " "));
                        cell = row.createCell(cellnum++);
                        cell.setCellValue(StringUtil.isNullOrEmpty(gl.getOnlydesc()) ? " " : gl.getOnlydesc().replaceAll("\n", " "));
                        cell = row.createCell(cellnum++);
			cell.setCellValue(StringUtil.isNullOrEmpty(gl.getCurrencyName()) ? " " : gl.getCurrencyName());
                        cell = row.createCell(cellnum++);
                        cell.setCellValue(StringUtil.isNullOrEmpty(gl.getErate()) ? " " : gl.getErate());
                        cell = row.createCell(cellnum++);
                        cell.setCellValue(authHandler.formattedCommaSeparatedUnitPrice(gl.getOpening(), companyid));
			cell = row.createCell(cellnum++);
			cell.setCellValue(authHandler.formattedCommaSeparatedUnitPrice(gl.getDebitAmtInAccCurrency(), companyid));     //Debit Amount in Account Currency         
                        cell = row.createCell(cellnum++);
                        cell.setCellValue(authHandler.formattedCommaSeparatedUnitPrice(gl.getDebit(), companyid));
			cell = row.createCell(cellnum++);
			cell.setCellValue(authHandler.formattedCommaSeparatedUnitPrice(gl.getCreditAmtInAccCurrency(), companyid));    //Credit in Account Currency                    
                        cell = row.createCell(cellnum++);
                        cell.setCellValue(authHandler.formattedCommaSeparatedUnitPrice(gl.getCredit(), companyid));
                        cell = row.createCell(cellnum++);
                        cell.setCellValue(authHandler.formattedCommaSeparatedUnitPrice(gl.getBalance(), companyid));
                        cell = row.createCell(cellnum++);
                        cell.setCellValue(StringUtil.isNullOrEmpty(gl.getGstCode()) ? " " : gl.getGstCode());
                        cell = row.createCell(cellnum++);
                        cell.setCellValue(StringUtil.isNullOrEmpty(gl.getCostCenterName()) ? " " : gl.getCostCenterName());
                        cell = row.createCell(cellnum++);
                        cell.setCellValue(StringUtil.isNullOrEmpty(gl.getSalesPersonName()) ? " " : gl.getSalesPersonName());
                        //Set Global Dimension data values to cell
                        for(int cnt=defaultColumnsCnt;cnt<columnsCnt;cnt++){
                            Map<String, String> customFieldData = gl.getCustomFieldData();
                            cell = row.createCell(cellnum++);
                            if(customFieldData!=null){
                                if(customFieldData.containsKey(generalLedgerheader.get(cnt)) && !StringUtil.isNullOrEmpty(customFieldData.get(generalLedgerheader.get(cnt)).toString())) {
                                    cell.setCellValue(customFieldData.get(generalLedgerheader.get(cnt)).toString());
                                }else{
                                    cell.setCellValue("");
                                }
                            }else{
                                cell.setCellValue("");
                            }
                        }
                        //Set Line level dimension data values to cell
                        for(int cnt=columnsCnt;cnt<generalLedgerheader.size();cnt++){
                            Map<String, String> lineLevelCustomFieldData = gl.getLineLevelCustomFieldData();
                            cell = row.createCell(cellnum++);
                            if(lineLevelCustomFieldData!=null){
                                if(lineLevelCustomFieldData.containsKey(generalLedgerheader.get(cnt)) && !StringUtil.isNullOrEmpty(lineLevelCustomFieldData.get(generalLedgerheader.get(cnt)).toString())) {
                                    cell.setCellValue(lineLevelCustomFieldData.get(generalLedgerheader.get(cnt)).toString());
                                }else{
                                    cell.setCellValue("");
                                }
                            } else {
                                cell.setCellValue("");
                            }
                        }
                    }
                     //oss.write((wb.getBytes()));
                    wb.write(oss);
                    oss.flush();
                    if (oss != null) {
                        oss.close();
                    }

                } else {
                    StringBuilder reportSB = new StringBuilder();
                    if (!headersFlag) {
                        reportSB.append("\"Account Code\",\"Alias Code\",\"Account Name\",\"Type\",\"Date\",\"JE Number\",\"Number\",\"Name(Payer/Payee)\""
                                + ",\"Double Entry Movement\",\"Memo\",\"Description\",\"Exchange Rate\",\"Opening Balance\",\"Debit\",\"Credit\",\"Balance\"\n");
                    }
                    for (GeneralLedger gl : generalLedgerList) {

                        reportSB.append(StringUtil.isNullOrEmpty(gl.getAcccode()) ? "," : "\"" + gl.getAcccode() + "\",");
                        reportSB.append(StringUtil.isNullOrEmpty(gl.getAliascode()) ? "," : "\"" + gl.getAliascode() + "\",");
                        reportSB.append(StringUtil.isNullOrEmpty(gl.getAccname()) ? "," : "\"" + gl.getAccname() + "\",");
                        reportSB.append(StringUtil.isNullOrEmpty(gl.getType()) ? "," : "\"" + gl.getType() + "\",");
                        reportSB.append(StringUtil.isNullOrEmpty(gl.getDate()) ? "," : "\"" + gl.getDate() + "\",");
                        reportSB.append(StringUtil.isNullOrEmpty(gl.getJEnumber()) ? "," : "\"" + gl.getJEnumber() + "\",");
                        reportSB.append(StringUtil.isNullOrEmpty(gl.getVoucherno()) ? "," : "\"" + gl.getVoucherno() + "\",");
                        reportSB.append(StringUtil.isNullOrEmpty(gl.getPayer()) ? "," : "\"" + gl.getPayer() + "\",");
                        reportSB.append(StringUtil.isNullOrEmpty(gl.getName()) ? "," : "\"" + gl.getName() + "\",");
                        reportSB.append(StringUtil.isNullOrEmpty(gl.getMemo()) ? "," : "\"" + gl.getMemo().replaceAll("\n", " ").replaceAll("\"", "\'") + "\",");
                        reportSB.append(StringUtil.isNullOrEmpty(gl.getOnlydesc()) ? "," : "\"" + gl.getOnlydesc().replaceAll("\n", " ").replaceAll("\"", "\'") + "\",");
                        reportSB.append(StringUtil.isNullOrEmpty(gl.getErate()) ? "," : "\"" + gl.getErate() + "\",");
                        reportSB.append("\"" + authHandler.formattedCommaSeparatedUnitPrice(gl.getOpening(), companyid) + "\",");
                        reportSB.append("\"" + authHandler.formattedCommaSeparatedUnitPrice(gl.getDebit(), companyid) + "\",");
                        reportSB.append("\"" + authHandler.formattedCommaSeparatedUnitPrice(gl.getCredit(), companyid) + "\",");
                        reportSB.append("\"" + authHandler.formattedCommaSeparatedUnitPrice(gl.getBalance(), companyid) + "\"\n");
                    }

                    oss.write(reportSB.toString().getBytes());

                    oss.flush();
                    if (oss != null) {
                        oss.close();
                    }
                }
            }
            

            HashMap<String, Object> exportDetails = new HashMap<String, Object>();

            exportDetails.put("id", productExportDetail.getId());
            exportDetails.put("status", 2);
            exportDetails.put("fileType", fileType);
            accProductObj.saveProductExportDetails(exportDetails);

            SendMail(requestParams, filePath,fileType);

        } catch (Exception ex) {
            ex.printStackTrace();

        }
    }
    
    public JSONObject getAccountJson(List list, accCurrencyDAO accCurrencyDAOobj, boolean noactivity, HashMap<String, Object> requestParams) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        KwlReturnObject result = null;
        try {
            boolean isSplitOpeningBalanceAmount = (requestParams.containsKey("isSplitOpeningBalanceAmount") && requestParams.get("isSplitOpeningBalanceAmount") != null) ? (Boolean) requestParams.get("isSplitOpeningBalanceAmount") : false;
            boolean isSplitOpeningBalanceSearch = (requestParams.containsKey("isSplitOpeningBalanceSearch") && requestParams.get("isSplitOpeningBalanceSearch") != null) ? (Boolean) requestParams.get("isSplitOpeningBalanceSearch") : false;
//            Iterator itr = list.iterator();
            KwlReturnObject bAmt = null, presentBaseAmount = null;
            String currencyid = "";
            String companyid = (String) requestParams.get(Constants.companyKey);
//            KwlReturnObject companyObj = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
//            Company company = (Company) companyObj.getEntityList().get(0);
            int countryid = accCompanyPreferencesObj.getCountryID(companyid);
            boolean accountHasJedTransaction;
            boolean accountTypeTransaction;
            boolean isGeneralLedger = false;
            boolean includeExcludeChildBalances = (requestParams.containsKey("includeExcludeChildBalances") &&  requestParams.get("includeExcludeChildBalances") != null) ? Boolean.parseBoolean(requestParams.get("includeExcludeChildBalances").toString()) : true;
            double openbalanceInbase = 0, presentbalanceInBase = 0, openbalanceSummary = 0, presentbalanceSummary = 0;
            for (Object object : list) {
                accountHasJedTransaction = false;
                accountTypeTransaction = false;
                openbalanceInbase = 0;
                presentbalanceInBase = 0;
                Object[] row = (Object[]) object;
                Account account = (Account) row[0];
                Group group = account.getGroup();
                JSONObject obj = new JSONObject();
                obj.put("accid", account.getID());
                obj.put("accname", account.getName());
                obj.put("groupid", group.getID());
                obj.put("groupname", group.getName());   //To show group name in COA Report.
                obj.put("nature", group.getNature());
                KWLCurrency currency = (KWLCurrency) row[5];
                currencyid = account.getCurrency() == null ? currency.getCurrencyID() : account.getCurrency().getCurrencyID();
                if (requestParams.containsKey("isGeneralLedger") && (Boolean) requestParams.get("isGeneralLedger")) {
                    isGeneralLedger = true;
                }
                if (!isGeneralLedger) {
                    int count = 0;
                    if (!StringUtil.isNullOrEmpty(account.getID())) {
                        result = accJournalEntryobj.getJEDfromAccount(account.getID(), companyid);
                        count += result.getRecordTotalCount();
                        if (count > 0) {
                            accountHasJedTransaction = true;
                        }
                    }
                    obj.put("accountHasJedTransaction", accountHasJedTransaction);

                    if (!StringUtil.isNullOrEmpty(account.getID())) {
                        result = accAccountDAOobj.getIBGDetailsForAccount(account.getID(), companyid);
                        count += result.getRecordTotalCount();
                        if (count > 0) {
                            accountTypeTransaction = true;
                        } else {
                            result = accTaxObj.getTaxFromAccount(account.getID(), companyid);
                            count += result.getRecordTotalCount();
                        }
                        if (count > 0) {
                            accountTypeTransaction = true;
                        } else {
                            result = accPaymentDAOobj.getPaymentMethodFromAccount(account.getID(), companyid);
                            count += result.getRecordTotalCount();
                        }
                        if (count > 0) {
                            accountTypeTransaction = true;
                        } else {
                            result = accProductObj.getProductfromAccount(account.getID(), companyid);
                            count += result.getRecordTotalCount();
                        }
                        if (count > 0) {
                            accountTypeTransaction = true;
                        }
                    }
                    obj.put("accountTypeTransaction", accountTypeTransaction);
                }
                // calculation of opening balance 
                JSONObject paramJObj = new JSONObject();

                for (Map.Entry e : requestParams.entrySet()) {
                    paramJObj.put(e.getKey().toString(), e.getValue());
                }
                paramJObj.put("currencyid", currencyid);
                paramJObj.put("gcurrencyid", currencyid);

                double openbalance = accInvoiceCommon.getOpeningBalanceOfAccountJson(paramJObj, account, false, null);
                boolean isCustomer = false;
                boolean isVendor = false;
                boolean isDepreciationAccount = false;               


                boolean accountHasOpeningTransactions = false;
                if (account.getUsedIn() != null) {
                    if (account.getUsedIn().contains(Constants.Customer_Default_Account)) {
                        isCustomer = true;
                    } else if (account.getUsedIn().contains(Constants.Vendor_Default_Account)) {
                        isVendor = true;
                    } else if (account.getUsedIn().equals(Constants.Depreciation_Provision_GL_Account)) {
                        isDepreciationAccount = true;
                    }

                    if (isCustomer || isVendor ||isDepreciationAccount) {
                        accountHasOpeningTransactions = accInvoiceCommon.accountHasOpeningTransactionsJson(paramJObj, account, false, null);
                        bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, openbalance, currencyid, account.getCreationDate(), 0);
                    }
                }
                obj.put("accountHasOpeningTransactions", accountHasOpeningTransactions);
                obj.put("accountopenbalance", openbalance);
                obj.put("acctaxcode", (!StringUtil.isNullOrEmpty(account.getTaxid())) ? account.getTaxid() : "");//"c340667e2896c0d80128a569f065017a");

                double openingBalanceInAccountCurrency = 0;
                if (account != null && !accountHasOpeningTransactions) {
                    openingBalanceInAccountCurrency = account.getOpeningBalance(); //when we change the account balance by some .1 or .01 then due to calaculation it shows wrong figures
                } else {
                    openingBalanceInAccountCurrency = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                }
                obj.put("openbalance", openingBalanceInAccountCurrency);

                if (!noactivity && !account.isDeleted()) {
                    List childlist = new ArrayList(account.getChildren());
                    if (childlist.isEmpty()) {
                        openbalanceInbase = openbalance;
                        openbalanceInbase = authHandler.round(openbalanceInbase, companyid);
                    } else {
                        openbalanceInbase = openbalance;
                        openbalanceInbase = authHandler.round(openbalanceInbase, companyid);
                        if (includeExcludeChildBalances) {
                            openbalanceInbase =  getTotalOpeningBalance(account, openbalanceInbase, currency.getCurrencyID(), accCurrencyDAOobj, requestParams);
                        }
                    }
                    if (account.getParent() == null) {
                        openbalanceSummary += openbalanceInbase;
                    }
                    obj.put("openbalanceinbase", openbalanceInbase);
                } else {
                    obj.put("openbalanceinbase", openbalanceInbase);
                }
                
                if (!isGeneralLedger) {
                    presentBaseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, account.getPresentValue(), currencyid, account.getCreationDate(), 0);
                    presentbalanceInBase = authHandler.round((Double) presentBaseAmount.getEntityList().get(0), companyid);
                    obj.put("presentbalanceInBase", presentbalanceInBase);
                    Account parentAccount = (Account) row[6];
                    if (parentAccount != null) {
                        obj.put("parentid", parentAccount.getID());
                        obj.put("parentname", parentAccount.getName());
                    }
                    obj.put("taxid", account.getTaxid());
                    if (!StringUtil.isNullOrEmpty(account.getTaxid())) {
                        KwlReturnObject taxResult = accountingHandlerDAOobj.getObject(Tax.class.getName(), account.getTaxid());
                        Tax tax = (Tax) taxResult.getEntityList().get(0);
                        obj.put("taxName", tax.getName());
                    }
                }
                               
                obj.put("currencyid", currencyid);
                obj.put("currencysymbol", (account.getCurrency() == null ? currency.getCurrencyID() : account.getCurrency().getSymbol()));
                obj.put("currencyname", (account.getCurrency() == null ? currency.getName() : account.getCurrency().getName()));
                obj.put("level", row[3]);
                obj.put("leaf", row[4]);
                obj.put("presentbalance", account.getPresentValue());
                obj.put("custminbudget", account.getCustMinBudget());

                if (!StringUtil.isNullOrEmpty(account.getAcccode())) {
                    obj.put("acccode", account.getAcccode());
                } else {
                    obj.put("acccode", "");
                }
                boolean accountCodeNotAdded = (requestParams.containsKey("accountCodeNotAdded") && requestParams.get("accountCodeNotAdded") != null) ? Boolean.parseBoolean((String) requestParams.get("accountCodeNotAdded")) : false;
                obj.put("accnamecode", (accountCodeNotAdded) ? account.getName() : ((!StringUtil.isNullOrEmpty(account.getAcccode())) ? ("[" + account.getAcccode() + "] " + account.getName()) : account.getName()));
                obj.put("deleted", account.isDeleted());
                obj.put("creationDate", authHandler.getGlobalDateFormat().format(account.getCreationDate()));
                obj.put("userid", account.getUser() == null ? "" : account.getUser().getUserID());
                obj.put("costcenterid", account.getCostcenter() == null ? "" : account.getCostcenter().getID());
                obj.put("costcenterName", account.getCostcenter() == null ? "" : account.getCostcenter().getName());
                obj.put("aliascode", account.getAliascode() == null ? "" : account.getAliascode());
                obj.put("accounttype", account.getAccounttype());
                obj.put("accdesc", StringUtil.isNullOrEmpty(account.getDescription()) ? "" : account.getDescription());
                obj.put("controlAccounts", account.isControlAccounts());
                obj.put("isactivate", account.isActivate());
                obj.put("hasAccess", account.isActivate());
                /* ------------------------ Indian Company TDS Flow (ERP-20907)----------------------------------- */
                obj.put("bankbranchname", account.getBankbranchname());
                obj.put("bankbranchaddress", account.getBankbranchaddress());
                obj.put("branchstate", account.getBranchstate()!=null?(account.getBranchstate().getID()!=null?account.getBranchstate().getID():""):"");
                obj.put("bsrcode", account.getBsrcode());
                obj.put("pincode", account.getPincode());
                /* -----------------------------------------------------------------------------------------------*/
                switch (account.getAccounttype()) {
                    case Group.ACC_TYPE_BALANCESHEET:
                        obj.put("accounttypestring", Group.ACC_TYPE_BALANCESHEETSTR);
                        break;
                    case Group.ACC_TYPE_PROFITLOSS:
                        obj.put("accounttypestring", Group.ACC_TYPE_PROFITLOSSSTR);
                        break;
                }
                obj.put("mastertypevalue", account.getMastertypevalue());
                switch (account.getMastertypevalue()) {
                    case Group.ACCOUNTTYPE_GL:
                        obj.put("mastertypevaluestring", Group.ACCOUNTTYPE_GLSTR);
                        break;
                    case Group.ACCOUNTTYPE_CASH:
                        obj.put("mastertypevaluestring", Group.ACCOUNTTYPE_CASHSTR);
                        break;
                    case Group.ACCOUNTTYPE_BANK:
                        obj.put("mastertypevaluestring", Group.ACCOUNTTYPE_BANKSTR);
                        break;
                    case Group.ACCOUNTTYPE_GST:
                        if(countryid == Constants.indian_country_id){//For India Country
                            obj.put("mastertypevaluestring", Group.ACCOUNTTYPE_GSTSTRForIndia);
                        }else{
                            obj.put("mastertypevaluestring", Group.ACCOUNTTYPE_GSTSTR);
                        }
                        break;
                }

                if (!account.isDeleted()) {
                    presentbalanceSummary += presentbalanceInBase;
                }
                if (account.isHeaderaccountflag()) {
                    obj.put("isHeaderAccount", true);
                } else {
                    obj.put("isHeaderAccount", false);
                }
                obj.put("eliminateflag", account.isEliminateflag());

                obj.put(Constants.IS_IBG_BANK, account.isIBGBank());
                if (account.isIBGBank()) {
                    if (account.getIbgBankType() == Constants.DBS_BANK_Type) {       // FOR DBS bank
                        KwlReturnObject ibgDetailResult = accAccountDAOobj.getIBGDetailsForAccount(account.getID(), account.getCompany().getCompanyID());
                        if (!ibgDetailResult.getEntityList().isEmpty()) {
                            IBGBankDetails IBGBankDetails = (IBGBankDetails) ibgDetailResult.getEntityList().get(0);
                            obj.put(Constants.IBG_BANK_DETAIL_ID, IBGBankDetails.getID());
                            obj.put(Constants.IBG_BANK, IBGBankDetails.getIbgbank());
                            obj.put(Constants.BANK_CODE, IBGBankDetails.getBankCode());
                            obj.put(Constants.BRANCH_CODE, IBGBankDetails.getBranchCode());
                            obj.put(Constants.ACCOUNT_NUMBER, IBGBankDetails.getAccountNumber());
                            obj.put(Constants.ACCOUNT_NAME, IBGBankDetails.getAccountName());
                            obj.put(Constants.SENDERS_COMPANYID, IBGBankDetails.getSendersCompanyID());
                            obj.put(Constants.BANK_DAILY_LIMIT, IBGBankDetails.getBankDailyLimit());
                        }
                    } else if(account.getIbgBankType() == Constants.CIMB_BANK_Type){    // FOR CIMB bank
                        KwlReturnObject ibgDetailResult = accAccountDAOobj.getCIMBDetailsForAccount(account.getID(), account.getCompany().getCompanyID());
                        if (!ibgDetailResult.getEntityList().isEmpty()) {
                            CIMBBankDetails IBGBankDetails = (CIMBBankDetails) ibgDetailResult.getEntityList().get(0);
                            obj.put(Constants.CIMB_BANK_DETAIL_ID, IBGBankDetails.getID());
                            obj.put("bankAccountNumber", IBGBankDetails.getBankAccountNumber());
                            obj.put("serviceCode", IBGBankDetails.getServiceCode());
                            obj.put("ordererName", IBGBankDetails.getOrdererName());
                            obj.put("currencyCode", IBGBankDetails.getCurrencyCode());
                            obj.put("settlementMode", IBGBankDetails.getSettelementMode());
                            obj.put("postingIndicator", IBGBankDetails.getPostingIndicator());
                        }
                    }
                    obj.put("ibgbanktype",account.getIbgBankType());
                }
                
                if (isSplitOpeningBalanceAmount && isSplitOpeningBalanceSearch) {
                    if (openbalanceInbase != 0) {
                        jArr.put(obj);
                    }
                } else {
                    jArr.put(obj);
                }
//            }//if
            }//while
            jobj.put("data", jArr);
            jobj.put("openbalanceSummary", openbalanceSummary);
            jobj.put("presentbalanceSummary", presentbalanceSummary);
        } catch (JSONException ex) {
            Logger.getLogger(ExportLedger.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getAccountJson : " + ex.getMessage(), ex);
        }
        return jobj;
    }

    public double getTotalOpeningBalance(Account account, double totalOpeningBalance, String defaultCurrencyid, accCurrencyDAO accCurrencyDAOobj, HashMap<String, Object> requestParams) throws ServiceException {
        try {
            KwlReturnObject resultChild = accAccountDAOobj.getAccountChilds(account);
            List childlist = resultChild.getEntityList();
            Iterator itr = null;
            String companyid = (String) requestParams.get("companyid");
            if(childlist!= null && !childlist.isEmpty()) {
                itr = childlist.iterator();
            }
            
            while (itr != null && itr.hasNext()) {
                Account subAccount = (Account) itr.next();
                //HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
                double balance = 0;
                String currencyid = "";
                if (!subAccount.isDeleted()) {
                    currencyid = subAccount.getCurrency() == null ? defaultCurrencyid : subAccount.getCurrency().getCurrencyID();
                    double openingBalance = accInvoiceCommon.getOpeningBalanceOfAccountLedger(requestParams, subAccount, false, null);
//                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, subAccount.getOpeningBalance(), currencyid, subAccount.getCreationDate(), 0);
                    balance = authHandler.round(openingBalance, companyid);
                }
                totalOpeningBalance = totalOpeningBalance + balance;
                //if (subAccount.getChildren().isEmpty()) {
                //    continue;
                //}
                
                KwlReturnObject resultChildSubAccount = accAccountDAOobj.getAccountChilds(subAccount);
                List childlistSubAccount = resultChildSubAccount.getEntityList();

                if(childlistSubAccount == null) {
                    continue;
                } else if(childlistSubAccount!= null && childlistSubAccount.isEmpty()) {
                    continue;
                }
                
                //Recursive function to get child accounts
                totalOpeningBalance = getTotalOpeningBalance(subAccount, totalOpeningBalance, defaultCurrencyid, accCurrencyDAOobj, requestParams);
            }
        } catch (Exception ex) {
            Logger.getLogger(ExportLedger.class.getName()).log(Level.SEVERE, null, ex);
        }
        return totalOpeningBalance;
    }

    public double getAccountBalanceInOriginalCurrency(String accountid, Date startDate, Date endDate, HashMap<String, Object> requestParams) throws ServiceException, SessionExpiredException {

        
        // requestParams.put(Constants.moduleid ,"100");
        String selectedCurrencyIds = requestParams.get("currencyIds").toString();
        if (!StringUtil.isNullOrEmpty(selectedCurrencyIds)) {
            requestParams.put("currencyFlag", true);
            requestParams.put("selectedCurrencyIds", selectedCurrencyIds);
        }
        return getAccountBalanceInOriginalCurrency(requestParams, accountid, startDate, endDate);
    }
    public double getAccountBalance(JSONObject params, String accountid, Date startDate, Date endDate) throws ServiceException, SessionExpiredException {
        HashMap<String, Object> requestParams = new HashMap<>();
        try {
            requestParams = (HashMap<String, Object>) params.get(Constants.GLOBAL_PARAMS);
            requestParams.put("costcenter", params.optString("costcenter", ""));
            requestParams.put(Constants.Acc_Search_Json, params.optString(Constants.Acc_Search_Json, ""));
            requestParams.put(Constants.Filter_Criteria, params.optString(InvoiceConstants.Filter_Criteria, ""));
            requestParams.put("templatecode", (StringUtil.isNullOrEmpty(params.optString("templatecode", null))) ? -1 : Integer.parseInt(params.optString("templatecode")));
            String selectedCurrencyIds = params.optString("currencyIds");
            if (!StringUtil.isNullOrEmpty(selectedCurrencyIds)) {
                requestParams.put("currencyFlag", true);
                requestParams.put("selectedCurrencyIds", selectedCurrencyIds);
            }
            if (!StringUtil.isNullOrEmpty(params.optString("isGeneralLedger"))) {
                boolean isGeneralLedger = Boolean.parseBoolean(params.optString("isGeneralLedger"));
                requestParams.put("generalLedgerFlag", isGeneralLedger);
            }
        } catch (Exception ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return accReportsService.getAccountBalance(params, requestParams, accountid, startDate, endDate);
    }
    public double getAccountBalanceInOriginalCurrency(HashMap<String, Object> requestParams, String accountid, Date startDate, Date endDate) throws ServiceException {
        double amount = 0;
        try {
            String gcurrencyid = (String) requestParams.get(Constants.globalCurrencyKey);

            KwlReturnObject accresult = accountingHandlerDAOobj.getObject(Account.class.getName(), accountid);
            Account account = (Account) accresult.getEntityList().get(0);

            int templatecode = (Integer) requestParams.get("templatecode");
            String tocurrencyid = requestParams.containsKey("tocurrencyid") && requestParams.get("tocurrencyid") != null ? (String) requestParams.get("tocurrencyid") : account.getCurrency().getCurrencyID();
            boolean convertOBFlag = requestParams.containsKey("tocurrencyid") && requestParams.get("tocurrencyid") != null ? true : false;//No need to convert opening balance in case of tocurrencyid = acc currency as opening balance value enetered is in account currnecy only

            String costCenterId = requestParams.containsKey("costcenter") && requestParams.get("costcenter") != null ? (String) requestParams.get("costcenter") : "";
            if ((templatecode == -1) || (account.getTemplatepermcode() != null && account.getTemplatepermcode() != 0 && ((templatecode & account.getTemplatepermcode()) == templatecode))) {
                if (StringUtil.isNullOrEmpty(costCenterId)) { //Don't consider opening balance for CostCenter
                    if (startDate != null && ((startDate.before(account.getCreationDate()) || startDate.equals(account.getCreationDate())) && endDate.after(account.getCreationDate()) || endDate.equals(account.getCreationDate()))) {
//                        KwlReturnObject result = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams,account.getOpeningBalance(),account.getCurrency().getCurrencyID(),account.getCreationDate(),0);
                        JSONObject paramJObj = new JSONObject();
                        for (Map.Entry e : requestParams.entrySet()) {
                            paramJObj.put(e.getKey().toString(), e.getValue());
                        }
                        paramJObj.put("currencyid", tocurrencyid);
                        paramJObj.put("gcurrencyid", gcurrencyid);
                        double accountOpeningBalanceInBase = accInvoiceCommon.getOpeningBalanceOfAccountJson(paramJObj, account, false, null);
                        
//                        double accountOpeningBalanceInBase = accInvoiceCommon.getOpeningBalanceOfAccountLedger(requestParams, account, false, null);
                        amount = accountOpeningBalanceInBase;//account.getOpeningBalance();
                        KwlReturnObject cresult = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, amount, account.getCurrency().getCurrencyID(), account.getCreationDate(), 0);
                        amount = (Double) cresult.getEntityList().get(0);
                        if (convertOBFlag) {
                            String fromcurrencyid = account.getCurrency().getCurrencyID();
                            KwlReturnObject crresult = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, amount, fromcurrencyid, tocurrencyid, account.getCreationDate(), 0);
                            amount = (Double) crresult.getEntityList().get(0);
                        }
                    }

                }
            }
            String Searchjson = "";

            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            if (requestParams.containsKey(InvoiceConstants.Filter_Criteria) && requestParams.get(InvoiceConstants.Filter_Criteria) != null) {
                if (requestParams.get(InvoiceConstants.Filter_Criteria).toString().equalsIgnoreCase("OR")) {
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }
            if (requestParams.containsKey(Constants.Acc_Search_Json) && requestParams.get(Constants.Acc_Search_Json) != null) {
                Searchjson = requestParams.get(Constants.Acc_Search_Json).toString();
            }
//            if(StringUtil.isNullOrEmpty(Searchjson) && account.getCompany().isOptimizedflag() && (templatecode == -1)) {
//                KwlReturnObject abresult = accJournalEntryobj.getAccountBalance_optimized(accountid, startDate, endDate, costCenterId);
//                List list = abresult.getEntityList();
//                if(list.size() > 0 && list.get(0) != null) {
//                    amount += (Double) list.get(0);
//                }
//            } else {
//            KwlReturnObject abresult = accJournalEntryobj.getAccountBalance(requestParams, accountid, startDate, endDate, costCenterId, filterConjuctionCriteria, Searchjson);
//            List list = abresult.getEntityList();
//            Iterator itr = list.iterator();
//            while (itr.hasNext()) {
//                Object[] row = (Object[]) itr.next();
//                JournalEntryDetail jed = (JournalEntryDetail) row[1];
//                JournalEntry je = (JournalEntry) row[3];
//                if ((templatecode == -1) || (je.getTemplatepermcode() != null && je.getTemplatepermcode() != 0 && ((templatecode & je.getTemplatepermcode()) == templatecode))) {
//                    if (jed.getJournalEntry().getIsReval() == 0) {
//                        String fromcurrencyid = (je.getCurrency() == null ? gcurrencyid : row[2].toString());
//                        //            amount += CompanyHandler.getCurrencyToBaseAmount(session, request, ((Double) row[0]).doubleValue(), fromcurrencyid, jed.getJournalEntry().getEntryDate());
//                        KwlReturnObject crresult = null;//if Same currency then use - getOneCurrencyToOther()
//                        if (fromcurrencyid.equalsIgnoreCase(tocurrencyid)) {
//                            crresult = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, ((Double) row[0]).doubleValue(), fromcurrencyid, tocurrencyid, je.getEntryDate(), je.getExternalCurrencyRate());
//                        } else {
//                            crresult = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, ((Double) row[0]).doubleValue(), fromcurrencyid, tocurrencyid, je.getEntryDate(), je.getExternalCurrencyRate());
//                        }
//
//                        amount += (Double) crresult.getEntityList().get(0);
//                    }
//                }
//            }
             KwlReturnObject abresult = accJournalEntryobj.getAccountBalanceAmount(requestParams, accountid, startDate, endDate, costCenterId, filterConjuctionCriteria, Searchjson, null);
            List list = abresult.getEntityList();
            if (list.get(0) != null) {
                amount += (Double) list.get(0);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getAccountBalance : " + ex.getMessage(), ex);
        }
        return amount;
    }

    public List getChildAccounts(List ll, Account account) {
        Iterator<Account> itr = account.getChildren().iterator();
        while (itr.hasNext()) {
            Account child = itr.next();
            ll.add(child);
            ll = getChildAccounts(ll, child);
        }
        return ll;
    }
}
