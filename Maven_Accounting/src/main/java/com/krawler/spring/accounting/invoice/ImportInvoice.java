/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.invoice;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.DataInvalidateException;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.customFieldMaster.fieldDataManager;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.model.stockmovement.StockMovementService;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.account.accCusVenMapDAO;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesConstants;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.costCenter.AccCostCenterDAO;
import com.krawler.spring.accounting.creditnote.accCreditNoteDAO;
import com.krawler.spring.accounting.creditnote.accCreditNoteService;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignDAO;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import com.krawler.spring.accounting.debitnote.accDebitNoteDAO;
import com.krawler.spring.accounting.discount.accDiscountDAO;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.handler.CommonEnglishNumberToWords;
import com.krawler.spring.accounting.inventory.AccImportService;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.accounting.product.Importproduct;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.receipt.accReceiptDAO;
import com.krawler.spring.accounting.salesorder.AccSalesOrderServiceDAO;
import com.krawler.spring.accounting.salesorder.accSalesOrderDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.term.accTermDAO;
import com.krawler.spring.accounting.vendor.accVendorDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFuctionality.AccExportReportsServiceDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.importFunctionality.ImportDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.sessionHandler.CompanySessionClass;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.spring.writeOffInvoice.accWriteOffServiceDao;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import com.mchange.v2.c3p0.C3P0Registry;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.context.MessageSource;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.support.RequestContextUtils;
import com.krawler.spring.accounting.product.productHandler;
import com.krawler.spring.accounting.productmodule.service.AccProductModuleService;
import com.krawler.spring.accounting.uom.accUomDAO;

/**
 *
 * @author krawler
 */
public class ImportInvoice implements Runnable {

    ArrayList processQueue = new ArrayList();
    private boolean isworking = false;
    private int importLimit = 1500;
    private HibernateTransactionManager txnManager;
    private accInvoiceDAO accInvoiceDAOobj;
    public ImportHandler importHandler;
    private ImportDAO importDao;
    private fieldDataManager fieldDataManagercntrl;
    private accJournalEntryDAO accJournalEntryobj;
    private accProductDAO accProductObj;
    private accDiscountDAO accDiscountobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accCustomerDAO accCustomerDAOobj;
    private AccCostCenterDAO accCostCenterObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private MessageSource messageSource;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private AccCommonTablesDAO accCommonTablesDAO;
    private accMasterItemsDAO accMasterItemsDAOobj;
    private accTermDAO accTermObj;
    public Importproduct importpoductobj;
    private authHandlerDAO authHandlerDAOObj;

    public void setAuthHandlerDAOObj(authHandlerDAO authHandlerDAOObj) {
        this.authHandlerDAOObj = authHandlerDAOObj;
    }
    public boolean isIsworking() {
        return isworking;
    }

    public void setIsworking(boolean isworking) {
        this.isworking = isworking;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }

    public void setaccDiscountDAO(accDiscountDAO accDiscountobj) {
        this.accDiscountobj = accDiscountobj;
    }

    public void setaccCustomerDAO(accCustomerDAO accCustomerDAOobj) {
        this.accCustomerDAOobj = accCustomerDAOobj;
    }

    public void setaccCostCenterDAO(AccCostCenterDAO accCostCenterObj) {
        this.accCostCenterObj = accCostCenterObj;
    }

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setaccTermDAO(accTermDAO accTermObj) {
        this.accTermObj = accTermObj;
    }

    public void setAccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }

    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    public void setimportDAO(ImportDAO importDao) {
        this.importDao = importDao;
    }

    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setaccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }

    public void setFieldDataManagercntrl(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }

    public void add(HashMap<String, Object> requestParams) {
        try {
            processQueue.add(requestParams);
        } catch (Exception ex) {
            Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String addPendingImportLog(HashMap<String, Object> requestParams) {
        String logId = null;
        try {
            //Insert Integration log
            String fileName = (String) requestParams.get("filename");
            String Module = (String) requestParams.get("modName");
            try {
                List list = importDao.getModuleObject(Module);
                Modules module = (Modules) list.get(0); //Will throw null pointer if no module entry found
                Module = module.getId();
            } catch (Exception ex) {
                throw new DataInvalidateException("Column config not available for module " + Module);
            }
            HashMap<String, Object> logDataMap = new HashMap<String, Object>();
            logDataMap.put("FileName", ImportLog.getActualFileName(fileName));
            logDataMap.put("StorageName", fileName);
            logDataMap.put("Log", "In Progress");
            logDataMap.put("Type", fileName.substring(fileName.lastIndexOf(".") + 1));
            logDataMap.put("Module", Module);
            logDataMap.put("ImportDate", new Date());
            logDataMap.put("User", (String) requestParams.get("userid"));
            logDataMap.put("Company", (String) requestParams.get("companyid"));
            ImportLog importlog = (ImportLog) importDao.saveImportLog(logDataMap);
            logId = importlog.getId();
        } catch (Exception ex) {
            logId = null;
        }
        return logId;
    }

    @Override
    public void run() {
        try {
            while (!processQueue.isEmpty() && !isworking) {
                this.isworking = true;
                HashMap<String, Object> requestParams1 = (HashMap<String, Object>) processQueue.get(0);
                try {
                    JSONObject jobj = new JSONObject();
                    String modulename = requestParams1.get("modName").toString();
                    jobj = importInvoiceRecords(requestParams1);
                    User user = (User) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.User", requestParams1.get("userid").toString());
                    Company company = (Company) kwlCommonTablesDAOObj.getClassObject(Company.class.getName(), user.getCompany().getCompanyID());
                    String htmltxt = "Report for data imported.<br/>";
                    htmltxt += "<br/>Module Name: " + modulename + "<br/>";
                    htmltxt += "<br/>File Name: " + jobj.get("filename") + "<br/>";
                    htmltxt += "Total Records: " + jobj.get("totalrecords") + "<br/>";
                    htmltxt += "Records Imported Successfully: " + jobj.get("successrecords");
                    htmltxt += "<br/>Failed Records: " + jobj.get("failedrecords");
                    htmltxt += "<br/><br/>Please check the import log in the system for more details.";
                    htmltxt += "<br/>For queries, email us at support@deskera.com<br/>";
                    htmltxt += "Deskera Team";

                    String plainMsg = "Report for data imported.\n";
                    plainMsg += "\nModule Name: " + modulename + "\n";
                    plainMsg += "\nFile Name:" + jobj.get("filename") + "\n";
                    plainMsg += "Total Records: " + jobj.get("totalrecords");
                    plainMsg += "\nRecords Imported Successfully: " + jobj.get("successrecords");
                    plainMsg += "\nFailed Records: " + jobj.get("failedrecords");
                    plainMsg += "\n\nPlease check the import log in the system for more details.";

                    plainMsg += "\nFor queries, email us at support@deskera.com\n";
                    plainMsg += "Deskera Team";
                    String fromEmailId = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                    Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                    SendMailHandler.postMail(new String[]{user.getEmailID()}, "Deskera Accounting - Report for data imported", htmltxt, plainMsg, fromEmailId, smtpConfigMap);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    processQueue.remove(requestParams1);
                    this.isworking = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    public JSONObject importInvoiceRecords(HashMap<String, Object> requestParams1) throws AccountingException, IOException, SessionExpiredException, JSONException, com.krawler.utils.json.base.JSONException {
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
        String companyID = (String) requestParams1.get("companyid");
        String currencyID = (String) requestParams1.get("gcurrencyid");
        JSONObject jobj = (JSONObject) requestParams1.get("jobj");
        String userId = (String) requestParams1.get("userid");
        String logid = (String) requestParams1.get("logId");
        String fileName = jobj.getString("filename");
        boolean isDraft = jobj.optBoolean("isDraft");
        String masterPreference = (String) requestParams1.get("masterPreference");
        String prevInvNo = "";
        JSONObject invjson = new JSONObject();
        int srNo = 0;
        HashSet jeDetails = new HashSet();
        String jeid = "";
        Map<String, Object> jeDataMap = new HashMap<String, Object>();
        KwlReturnObject jeresult;
        JournalEntry journalEntry;
        String gcurrencyId = (String) requestParams1.get("gcurrencyid");
        HashSet<InvoiceDetail> rows = new HashSet<InvoiceDetail>();
        ArrayList<String> prodList = new ArrayList<String>();
        double totalamount = 0;
        boolean isAlreadyExist = false;
        boolean isRecordFailed = false;
        String jeSeqFormatId = "";
        int jeSeqNo = 0;
        String customfield = "";
        double totaldiscount = 0;
        int count = 1;
        int limit = Constants.Transaction_Commit_Limit;

        JSONObject returnObj = new JSONObject();

        try {
            String dateFormat = null, dateFormatId = (String) requestParams1.get("dateFormat");
            if (!StringUtil.isNullOrEmpty(dateFormatId)) {

                KwlReturnObject kdfObj = accountingHandlerDAOobj.getObject(KWLDateFormat.class.getName(), dateFormatId);
                KWLDateFormat kdf = (KWLDateFormat) kdfObj.getEntityList().get(0);

                dateFormat = kdf != null ? kdf.getJavaForm() : null;
            }
            DateFormat df = new SimpleDateFormat(dateFormat);
            KwlReturnObject extraPref = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyID);
            ExtraCompanyPreferences extrareferences = (ExtraCompanyPreferences) extraPref.getEntityList().get(0);
            Boolean isCurrencyCode=extrareferences.isCurrencyCode();
            fileInputStream = new FileInputStream(jobj.getString("FilePath"));
            br = new BufferedReader(new InputStreamReader(fileInputStream));
            String record = "";
            int cnt = 0;

            StringBuilder failedRecords = new StringBuilder();

            HashMap<String, Integer> columnConfig = new HashMap<String, Integer>();
            JSONArray jSONArray = jobj.getJSONArray("resjson");
            List headArrayList = new ArrayList();
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
                headArrayList.add(jSONObject.get("csvheader"));

                columnConfig.put(jSONObject.getString("dataindex"), jSONObject.getInt("csvindex"));
            }

            failedRecords.append(createCSVrecord(headArrayList.toArray()) + "\"Error Message\""); // failedRecords.append("\"Row No.\","+createCSVrecord(fileData)+"\"Error Message\"");
            HashMap currencyMap = getCurrencyMap(isCurrencyCode);

            while ((record = br.readLine()) != null) {
                if (cnt != 0 && cnt != 1) {
                    String[] recarr = record.split(",");
                    try {
                        currencyID = (String) requestParams1.get("gcurrencyid");

                        String sequenceFormatID = "";
                        if (columnConfig.containsKey("sequenceformat")) {
                            String sequenceFormatName = recarr[(Integer) columnConfig.get("sequenceformat")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(sequenceFormatName)) {
                                sequenceFormatID = getSequenceFormatIDByName(sequenceFormatName, companyID);
                                if (StringUtil.isNullOrEmpty(sequenceFormatID)) {
                                    throw new AccountingException("Sequence Format is not found for " + sequenceFormatName);
                                }
                            }
                        }

                        String invoiceNumber = "";
                        if (columnConfig.containsKey("invoiceNumber")) {
                            invoiceNumber = recarr[(Integer) columnConfig.get("invoiceNumber")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(invoiceNumber)) {
                                throw new AccountingException("Invoice Number is not available");
                            }
                        } else {
                            throw new AccountingException("Invoice Number column is not found.");
                        }

                        Date invoiceDate = null;
                        if (columnConfig.containsKey("entryDate")) {
                            String invoiceDateStr = recarr[(Integer) columnConfig.get("entryDate")].replaceAll("\"", "").trim();

                            if (StringUtil.isNullOrEmpty(invoiceDateStr)) {
                                throw new AccountingException("Invoice Date is not available");
                            } else {
                                invoiceDate = df.parse(invoiceDateStr);
                            }
                        } else {
                            throw new AccountingException("Invoice Date column is not found.");
                        }

                        String poRefNumber = "";
                        if (columnConfig.containsKey("poRefNumber")) {
                            poRefNumber = recarr[(Integer) columnConfig.get("poRefNumber")].replaceAll("\"", "").trim();
                        }

                        String costCenterID = "";
                        if (columnConfig.containsKey("costcenter")) {
                            String costCenterName = recarr[(Integer) columnConfig.get("costcenter")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(costCenterName)) {
                                costCenterID = getCostCenterIDByName(costCenterName, companyID);
                                if (StringUtil.isNullOrEmpty(costCenterID)) {
                                    throw new AccountingException("Cost Center is not found for name " + costCenterName);
                                }
                            } else {
                                throw new AccountingException("Cost Center is not available.");
                            }
                        }

                        Date dueDate = null;
                        if (columnConfig.containsKey("dueDate")) {
                            String invoiceDateStr = recarr[(Integer) columnConfig.get("dueDate")].replaceAll("\"", "").trim();

                            if (StringUtil.isNullOrEmpty(invoiceDateStr)) {
                                throw new AccountingException("Due Date is not available");
                            } else {
                                dueDate = df.parse(invoiceDateStr);
                            }
                        } else {
                            throw new AccountingException("Due Date column is not found.");
                        }

                        String customerID = "";
                        String accountID = "";
                        
                        /*
                         * 1. Customer Code
                         */
                        if (columnConfig.containsKey("CustomerCode")) {
                            String customerCode = recarr[(Integer) columnConfig.get("CustomerCode")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(customerCode)) {
                                Customer customer = getCustomerByCode(customerCode, companyID);
                                if (customer != null) {
                                    accountID = customer.getAccount().getID();
                                    customerID = customer.getID();
                                } else {
                                    throw new AccountingException("Customer is not found for customer code " + customerCode);
                                }
                            }
                        }
                        
                        /*2. Customer Name
                         *if customerID is empty it menas customer is not found for given code. so need to serch data on name
                         */
                        if (StringUtil.isNullOrEmpty(customerID)) {
                            if (columnConfig.containsKey("CustomerName")) {
                                String customerName = recarr[(Integer) columnConfig.get("CustomerName")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(customerName)) {
                                    Customer customer=null;
                                    KwlReturnObject retObj = accCustomerDAOobj.getCustomerByName(customerName, companyID);
                                    if (retObj != null && !retObj.getEntityList().isEmpty()) {
                                        customer = (Customer) retObj.getEntityList().get(0);
                                    }
                                    if (customer != null) {
                                        accountID = customer.getAccount().getID();
                                        customerID = customer.getID();
                                    } else {
                                        throw new AccountingException("Customer is not found for customer name " + customerName);
                                    }
                                } else {
                                    throw new AccountingException("Customer is not found for customer name ");
                                }
                            } else {
                                throw new AccountingException("Customer is not found for customer code as well name ");
                            }
                        }

                        String termID = "";
                        if (columnConfig.containsKey("terms")) {
                            String termName = recarr[(Integer) columnConfig.get("terms")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(termName)) {
                                termID = getTermIDByName(termName, companyID);
                                if (StringUtil.isNullOrEmpty(termID)) {
                                    throw new AccountingException("Credit Term is not found for name " + termName);
                                }
                            } else {
                                throw new AccountingException("Credit Term is not available.");
                            }
                        } else {
                            throw new AccountingException("Credit Term column is not found.");
                        }

                        String memo = "";
                        if (columnConfig.containsKey("memo")) {
                            memo = recarr[(Integer) columnConfig.get("memo")].replaceAll("\"", "").trim();
                        }

                        String shipVia = "";
                        if (columnConfig.containsKey("shipvia")) {
                            shipVia = recarr[(Integer) columnConfig.get("shipvia")].replaceAll("\"", "").trim();
                        }

                        String fob = "";
                        if (columnConfig.containsKey("fob")) {
                            fob = recarr[(Integer) columnConfig.get("fob")].replaceAll("\"", "").trim();
                        }

                        String salesPersonID = "";
                        if (columnConfig.containsKey("salesperson")) {
                            String salesPersonName = recarr[(Integer) columnConfig.get("salesperson")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(salesPersonName)) {
                                salesPersonID = getSalesPersonIDByName(salesPersonName, companyID);
                                if (StringUtil.isNullOrEmpty(salesPersonID)) {
                                    throw new AccountingException("Sales Person is not found for name " + salesPersonName);
                                }
                            } else {
                                throw new AccountingException("Sales Person is not available.");
                            }
                        } else {
                            throw new AccountingException("Sales Person column is not found.");
                        }

                       if (isCurrencyCode?columnConfig.containsKey("currencyCode"):columnConfig.containsKey("currencyName")) {
                            String currencyStr = isCurrencyCode?recarr[(Integer) columnConfig.get("currencyCode")].replaceAll("\"", "").trim():recarr[(Integer) columnConfig.get("currencyName")].replaceAll("\"", "").trim();
                              if (!StringUtil.isNullOrEmpty(currencyStr)) {
                                currencyID = getCurrencyId(currencyStr, currencyMap);

                                if (StringUtil.isNullOrEmpty(currencyID)) {
                                    throw new AccountingException("Currency format you entered is not correct. it should be like \'SG Dollar (SGD)\'");//messageSource.getMessage("acc.field.ImportProductExceptionFormat", null, RequestContextUtils.getLocale(request)));
                                }
                            } else {
                                if (!masterPreference.equalsIgnoreCase("1")) {
                                    throw new AccountingException("Currency is not available.");
                                }
                            }
                        }

                        String productUUID = "";
                        if (columnConfig.containsKey("productID")) {
                            String productID = recarr[(Integer) columnConfig.get("productID")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(productID)) {
                                Product parentProduct = getProductByProductID(productID, companyID);
                                if (parentProduct != null) {
                                    productUUID = parentProduct.getID();
                                } else {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        productUUID = null;
                                    } else {
                                        throw new AccountingException("Product is not found for " + productID);
                                    }
                                }
                            } else {
                                throw new AccountingException("Product is not available.");
                            }
                        } else {
                            throw new AccountingException("Product column is not found.");
                        }

                        String quantity = "";
                        if (columnConfig.containsKey("initialquantity")) {
                            quantity = recarr[(Integer) columnConfig.get("initialquantity")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(quantity)) {
                                throw new AccountingException("Quantity is not available");
                            }
                        } else {
                            throw new AccountingException("Quantity column is not found.");
                        }

                        String unitPrice = "";
                        if (columnConfig.containsKey("rate")) {
                            unitPrice = recarr[(Integer) columnConfig.get("rate")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(unitPrice)) {
                                throw new AccountingException("Unit Price is not available");
                            }
                        } else {
                            throw new AccountingException("Unit Price column is not found.");
                        }

                        String productUOMID = "";
                        if (columnConfig.containsKey("uomname")) {
                            String productUOMName = recarr[(Integer) columnConfig.get("uomname")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(productUOMName)) {
                                UnitOfMeasure uom = getUOMByName(productUOMName, companyID);
                                if (uom != null) {
                                    productUOMID = uom.getID();
                                } else {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        productUOMID = "";
                                    } else {
                                        throw new AccountingException("Product Unit Of Measure is not found for " + productUOMName);
                                    }
                                }
                            } else {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    productUOMID = "";
                                } else {
                                    throw new AccountingException("Product Unit Of Measure is not available");
                                }
                            }
                        } else {
                            productUOMID = "";
                        }

                        int rowdisc = 1;
                        String discountType = "";
                        if (columnConfig.containsKey("discountType")) {
                            discountType = recarr[(Integer) columnConfig.get("discountType")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(discountType)) {
                                if (discountType.equalsIgnoreCase("Percentage")) {
                                    rowdisc = 1;
                                } else if (discountType.equalsIgnoreCase("Flat")) {
                                    rowdisc = 0;
                                } else {
                                    throw new AccountingException("Format you entered is not correct. It should be like \"Percentage\" or \"Flat\"");
                                }
                            } else {
                                if (!masterPreference.equalsIgnoreCase("1")) {
                                    throw new AccountingException("Discount Type is not available");
                                }
                            }
                        }

                        String dicountStr = "0";
                        if (columnConfig.containsKey("discount")) {
                            dicountStr = recarr[(Integer) columnConfig.get("discount")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(dicountStr)) {
                                throw new AccountingException("Dicount is not available");
                            }
                        } else {
                            dicountStr = "0";
                        }

                        isAlreadyExist = false;
                        KwlReturnObject result = accInvoiceDAOobj.getInvoiceCount(invoiceNumber, companyID);
                        int nocount = result.getRecordTotalCount();
                        if (nocount > 0) {
                            isAlreadyExist = true;
                            throw new AccountingException("Invoice number '" + invoiceNumber + "' already exists.");
                        }

                        double exchangeRateForTransaction = 1;
//                        if (!StringUtil.isNullOrEmpty(exchangeRateForTransactionStr)) {
//                            exchangeRateForTransaction = Double.parseDouble(exchangeRateForTransactionStr);
//                        } else {
                        Map<String, Object> currMap = new HashMap<String, Object>();
                        Date applyDate = invoiceDate;

                        currMap.put("applydate", applyDate);
                        currMap.put("gcurrencyid", gcurrencyId);
                        currMap.put("companyid", companyID);
                        KwlReturnObject retObj = accCurrencyDAOobj.getExcDetailID(currMap, currencyID, applyDate, null);
                        if (retObj != null) {
                            List li = retObj.getEntityList();
                            if (!li.isEmpty()) {
                                Iterator itr = li.iterator();
                                ExchangeRateDetails erd = (ExchangeRateDetails) itr.next();
                                if (erd != null) {
                                    exchangeRateForTransaction = erd.getExchangeRate();
                                }
                            }
                        }
//                        }

                        String createdby = (String) requestParams1.get("userid");
                        String modifiedby = (String) requestParams1.get("userid");
                        long createdon = System.currentTimeMillis();
                        long updatedon = System.currentTimeMillis();
                        String jeentryNumber = "";
                        String jeIntegerPart = "";
                        String jeDatePrefix = "";
                        String jeDateAfterPrefix = "";
                        String jeDateSuffix = "";
                        boolean jeautogenflag;

                        // creating invoice json
                        if (!prevInvNo.equalsIgnoreCase(invoiceNumber)) {
                            prevInvNo = invoiceNumber;
                            srNo = 0;

                            if (rows.size() > 0 && !isRecordFailed) {
                                updateInvoice(requestParams1, invjson, rows, jeDataMap, prodList, totalamount, jeDetails, customfield, totaldiscount);
                                totaldiscount = 0;
                                customfield = "";
                                jeDetails = new HashSet();
                                invjson = new JSONObject();
                                totalamount = 0;
                                prodList = new ArrayList<String>();
                                rows = new HashSet<InvoiceDetail>();
                            }

                            isRecordFailed=false;
                            // For create custom field array
                            JSONArray customJArr = new JSONArray();
                            for (int i = 0; i < jSONArray.length(); i++) {
                                JSONObject jSONObject = jSONArray.getJSONObject(i);

                                if (jSONObject.optBoolean("customflag", false) && !jSONObject.optBoolean("isLineItem", false)) {
                                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                                    requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.fieldlabel));
                                    requestParams.put(Constants.filter_values, Arrays.asList(companyID, Constants.Acc_Invoice_ModuleId, jSONObject.getString("columnname")));

                                    KwlReturnObject fieldParamsResult = accCommonTablesDAO.getFieldParams(requestParams); // get custom field for module
                                    FieldParams params = (FieldParams) fieldParamsResult.getEntityList().get(0);

                                    if (!StringUtil.isNullOrEmpty(recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim())) {
                                        JSONObject customJObj = new JSONObject();
                                        customJObj.put("fieldid", params.getId());
                                        customJObj.put("filedid", params.getId());
                                        customJObj.put("refcolumn_name", "Col" + params.getRefcolnum());
                                        customJObj.put("fieldname", "Custom_" + params.getFieldlabel());
                                        customJObj.put("xtype", params.getFieldtype());

                                        String fieldComboDataStr = "";
                                        if (params.getFieldtype() == 3) { // if field of date type
                                            String dateStr = recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim();
                                            customJObj.put("Col" + params.getColnum(), df.parse(dateStr).getTime());
                                            customJObj.put("fieldDataVal", df.parse(dateStr).getTime());
                                        } else if (params.getFieldtype() == 4 || params.getFieldtype() == 7) { // if field of MultiSelect Drop Down OR Drop Down
                                            String[] fieldComboDataArr = recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim().split(";");

                                            for (int dataArrIndex = 0; dataArrIndex < fieldComboDataArr.length; dataArrIndex++) {
                                                requestParams = new HashMap<String, Object>();
                                                requestParams.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "value", "deleteflag"));
                                                requestParams.put(Constants.filter_values, Arrays.asList(params.getId(), fieldComboDataArr[dataArrIndex], 0));


                                                fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParams);
                                                if (fieldParamsResult != null && !fieldParamsResult.getEntityList().isEmpty()) {
                                                    FieldComboData fieldComboData = (FieldComboData) fieldParamsResult.getEntityList().get(0);
                                                    fieldComboDataStr += fieldComboData.getId() + ",";
                                                }
                                            }

                                            if (!StringUtil.isNullOrEmpty(fieldComboDataStr)) {
                                                customJObj.put("Col" + params.getColnum(), fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                                customJObj.put("fieldDataVal", fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                            } else {
                                                continue;
                                            }
                                        } else if (params.getFieldtype() == 11) { // if field of check box type 
                                            customJObj.put("Col" + params.getColnum(), Boolean.parseBoolean(recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim()));
                                            customJObj.put("fieldDataVal", Boolean.parseBoolean(recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim()));
                                        } else if (params.getFieldtype() == 12) { // if field of check list type
                                            requestParams = new HashMap<String, Object>();
                                            requestParams.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "deleteflag"));
                                            requestParams.put(Constants.filter_values, Arrays.asList(params.getId(), 0));


                                            fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParams);
                                            List<FieldComboData> fieldComboDataList = fieldParamsResult.getEntityList();

                                            String[] fieldComboDataArr = recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim().split(";");

                                            int dataArrIndex = 0;

                                            for (FieldComboData fieldComboData : fieldComboDataList) {
                                                if (fieldComboDataArr.length != dataArrIndex && fieldComboDataArr[dataArrIndex] != null && fieldComboDataArr[dataArrIndex].replaceAll("\"", "").trim().equalsIgnoreCase("true")) {
                                                    fieldComboDataStr += fieldComboData.getId() + ",";
                                                }
                                                dataArrIndex++;
                                            }

                                            if (!StringUtil.isNullOrEmpty(fieldComboDataStr)) {
                                                customJObj.put("Col" + params.getColnum(), fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                                customJObj.put("fieldDataVal", fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                            } else {
                                                continue;
                                            }
                                        } else {
                                            customJObj.put("Col" + params.getColnum(), recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim());
                                            customJObj.put("fieldDataVal", recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim());
                                        }

                                        customJObj.put("Custom_" + params.getFieldlabel(), "Col" + params.getColnum());

                                        customJArr.put(customJObj);
                                    }
                                }
                            }

                            customfield = customJArr.toString();

                            String sequenceNo = "";
                            if (!StringUtil.isNullOrEmpty(sequenceFormatID)) {
                                KwlReturnObject seqResult = accountingHandlerDAOobj.getObject(SequenceFormat.class.getName(), sequenceFormatID);
                                SequenceFormat sequenceFormat = (SequenceFormat) seqResult.getEntityList().get(0);

                                sequenceNo = invoiceNumber;
                                sequenceNo = sequenceNo.replace(sequenceFormat.getPrefix(), "");
                                sequenceNo = sequenceNo.replace(sequenceFormat.getSuffix(), "");
                            }


                            invjson.put("accountid", accountID);
                            invjson.put("customerid", customerID);
                            if (!StringUtil.isNullOrEmpty(sequenceFormatID)) {
                                invjson.put(Constants.SEQFORMAT, sequenceFormatID);
                                invjson.put(Constants.SEQNUMBER, sequenceNo);
                            }

                            invjson.put("entrynumber", invoiceNumber);
                            invjson.put("autogenerated", true);
                            invjson.put("memo", memo);
                            invjson.put("porefno", poRefNumber);
                            invjson.put("duedate", dueDate);
                            invjson.put("companyid", companyID);
                            invjson.put("currencyid", currencyID);
                            invjson.put("externalCurrencyRate", exchangeRateForTransaction);
                            invjson.put("salesPerson", salesPersonID);
                            invjson.put("shipvia", shipVia);
                            invjson.put("fob", fob);
                            invjson.put("createdby", createdby);
                            invjson.put("modifiedby", modifiedby);
                            invjson.put("createdon", createdon);
                            invjson.put("updatedon", updatedon);
                            invjson.put("termid", termID);
                            invjson.put("isDraft", isDraft);

                            synchronized (this) {
                                HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                                JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                                JEFormatParams.put("modulename", CompanyPreferencesConstants.AUTOJOURNALENTRY);
                                JEFormatParams.put("companyid", companyID);
                                JEFormatParams.put("isdefaultFormat", true);

                                KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                                SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);

                                if (jeSeqNo == 0) {
                                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyID, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, invoiceDate);
                                    jeentryNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                                    jeIntegerPart = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                                    jeDatePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                                    jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);//Date After Prefix Part
                                    jeDateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                                    jeSeqFormatId = format.getID();

                                    jeSeqNo = Integer.parseInt(jeIntegerPart);
                                }
                                jeautogenflag = true;

                                int numberofdigit = format.getNumberofdigit();
                                boolean showleadingzero = format.isShowleadingzero();
                                String nextNumTemp = jeSeqNo + "";
                                if (showleadingzero) {
                                    while (nextNumTemp.length() < numberofdigit) {
                                        nextNumTemp = "0" + nextNumTemp;
                                    }
                                }
                                jeentryNumber = format.getPrefix() + nextNumTemp + format.getSuffix();

                                jeIntegerPart = nextNumTemp;

                                jeSeqNo++;
                            }

                            jeDataMap = new HashMap<String, Object>(requestParams1);  //AccountingManager.getGlobalParams(request);
                            jeDataMap.put("entrynumber", jeentryNumber);
                            jeDataMap.put("autogenerated", jeautogenflag);
                            jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                            jeDataMap.put(Constants.SEQNUMBER, jeIntegerPart);
                            jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
                            jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
                            jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
                            jeDataMap.put("entrydate", invoiceDate);
                            jeDataMap.put("companyid", companyID);
                            jeDataMap.put("memo", memo);
                            jeDataMap.put("currencyid", currencyID);
                            jeDataMap.put("costcenterid", costCenterID);
                            jeDataMap.put("isDraft", isDraft);
                            jeDetails = new HashSet();
                            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap); // Create Journal entry without JEdetails
                            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
                            jeid = journalEntry.getID();
                            jeDataMap.put("jeid", jeid);

                            invjson.put("entrydate", invoiceDate);
                            invjson.put("journalerentryid", jeid);
                        }



                        /**
                         * ********************************************************
                         * For save Invoice details
                         * ****************************8********************************************
                         */
                        KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyID);
                        CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);

                        KwlReturnObject cmp = accountingHandlerDAOobj.getObject(Company.class.getName(), companyID);
                        Company company = (Company) cmp.getEntityList().get(0);

                        KwlReturnObject prdresult = accountingHandlerDAOobj.getObject(Product.class.getName(), productUUID);
                        Product product = (Product) prdresult.getEntityList().get(0);

                        boolean updateInventoryFlag = (preferences.isWithInvUpdate()) ? false : true;
                        JournalEntryDetail jed;

                        prodList.add(productUUID);

                        InvoiceDetail row = new InvoiceDetail();

                        row.setSrno(srNo + 1);
                        srNo++;
                        row.setWasRowTaxFieldEditable(true);
                        row.setInvstoreid("");
                        row.setInvlocid("");
                        row.setCompany(company);
                        row.setRate(Double.parseDouble(unitPrice));

                        // for adding inventory entry
                        JSONObject inventoryjson = new JSONObject();
                        inventoryjson.put("productid", productUUID);
                        inventoryjson.put("quantity", Double.parseDouble(quantity));
                        if (!StringUtil.isNullOrEmpty(productUOMID)) {
                            inventoryjson.put("uomid", productUOMID);
                        }
                        double conversionFactor = 1;
                        inventoryjson.put("baseuomquantity", updateInventoryFlag ? Double.parseDouble(quantity) * conversionFactor : 0);
                        inventoryjson.put("actquantity", updateInventoryFlag ? 0 : Double.parseDouble(quantity) * conversionFactor);
                        inventoryjson.put("baseuomrate", conversionFactor);
                        inventoryjson.put("invrecord", updateInventoryFlag ? true : false);
                        inventoryjson.put("description", product.getDescription());
                        inventoryjson.put("carryin", false);
                        inventoryjson.put("defective", false);
                        inventoryjson.put("newinventory", false);
                        inventoryjson.put("companyid", companyID);
                        inventoryjson.put("updatedate", invoiceDate);
                        KwlReturnObject invresult = accProductObj.addInventory(inventoryjson);
                        Inventory inventory = (Inventory) invresult.getEntityList().get(0);


                        row.setInventory(inventory);

                        double rowAmount = authHandler.round(row.getRate() * Double.parseDouble(quantity), companyID);
                        rowAmount = authHandler.round(rowAmount, companyID);

                        totalamount += rowAmount;

                        double rowdiscount = 0;
                        Discount discount = null;
                        double disc = Double.parseDouble(dicountStr);
                        if (disc != 0.0) {
                            JSONObject discjson = new JSONObject();
                            discjson.put("discount", disc);
                            discjson.put("inpercent", (rowdisc == 1) ? true : false);
                            discjson.put("originalamount", rowAmount);
                            discjson.put("companyid", companyID);
                            KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                            discount = (Discount) dscresult.getEntityList().get(0);
                            row.setDiscount(discount);
                            rowdiscount = discount.getDiscountValue();
                            totaldiscount += rowdiscount;
                        }

                        JSONObject jedjson = new JSONObject();
                        jedjson.put("srno", jeDetails.size() + 1);
                        jedjson.put("companyid", companyID);
                        jedjson.put("amount", rowAmount);
                        jedjson.put("accountid", product.getSalesAccount().getID());
                        jedjson.put("debit", false);
                        jedjson.put("jeid", invjson.get("journalerentryid"));
                        KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jeDetails.add(jed);

                        row.setDeferredJeDetailId(jed.getID());


                        // Add Custom fields details of line items

                        // For create custom field array
                        JSONArray lineCustomJArr = new JSONArray();
                        for (int i = 0; i < jSONArray.length(); i++) {
                            JSONObject jSONObject = jSONArray.getJSONObject(i);

                            if (jSONObject.optBoolean("customflag", false) && jSONObject.optBoolean("isLineItem", false)) {
                                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                                requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.fieldlabel));
                                requestParams.put(Constants.filter_values, Arrays.asList(companyID, Constants.Acc_Invoice_ModuleId, jSONObject.getString("columnname")));

                                KwlReturnObject fieldParamsResult = accCommonTablesDAO.getFieldParams(requestParams); // get custom field for module
                                FieldParams params = (FieldParams) fieldParamsResult.getEntityList().get(0);

                                if (!StringUtil.isNullOrEmpty(recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim())) {
                                    JSONObject customJObj = new JSONObject();
                                    customJObj.put("fieldid", params.getId());
                                    customJObj.put("filedid", params.getId());
                                    customJObj.put("refcolumn_name", "Col" + params.getRefcolnum());
                                    customJObj.put("fieldname", "Custom_" + params.getFieldlabel());
                                    customJObj.put("xtype", params.getFieldtype());

                                    String fieldComboDataStr = "";
                                    if (params.getFieldtype() == 3) { // if field of date type
                                        String dateStr = recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim();
                                        customJObj.put("Col" + params.getColnum(), df.parse(dateStr).getTime());
                                        customJObj.put("fieldDataVal", df.parse(dateStr).getTime());
                                    } else if (params.getFieldtype() == 4 || params.getFieldtype() == 7) { // if field of MultiSelect Drop Down OR Drop Down
                                        String[] fieldComboDataArr = recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim().split(";");

                                        for (int dataArrIndex = 0; dataArrIndex < fieldComboDataArr.length; dataArrIndex++) {
                                            requestParams = new HashMap<String, Object>();
                                            requestParams.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "value", "deleteflag"));
                                            requestParams.put(Constants.filter_values, Arrays.asList(params.getId(), fieldComboDataArr[dataArrIndex], 0));


                                            fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParams);
                                            if (fieldParamsResult != null && !fieldParamsResult.getEntityList().isEmpty()) {
                                                FieldComboData fieldComboData = (FieldComboData) fieldParamsResult.getEntityList().get(0);
                                                fieldComboDataStr += fieldComboData.getId() + ",";
                                            }
                                        }

                                        if (!StringUtil.isNullOrEmpty(fieldComboDataStr)) {
                                            customJObj.put("Col" + params.getColnum(), fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                            customJObj.put("fieldDataVal", fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                        } else {
                                            continue;
                                        }
                                    } else if (params.getFieldtype() == 11) { // if field of check box type 
                                        customJObj.put("Col" + params.getColnum(), Boolean.parseBoolean(recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim()));
                                        customJObj.put("fieldDataVal", Boolean.parseBoolean(recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim()));
                                    } else if (params.getFieldtype() == 12) { // if field of check list type
                                        requestParams = new HashMap<String, Object>();
                                        requestParams.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "deleteflag"));
                                        requestParams.put(Constants.filter_values, Arrays.asList(params.getId(), 0));


                                        fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParams);
                                        List<FieldComboData> fieldComboDataList = fieldParamsResult.getEntityList();

                                        String[] fieldComboDataArr = recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim().split(";");

                                        int dataArrIndex = 0;

                                        for (FieldComboData fieldComboData : fieldComboDataList) {
                                            if (fieldComboDataArr.length != dataArrIndex && fieldComboDataArr[dataArrIndex] != null && fieldComboDataArr[dataArrIndex].replaceAll("\"", "").trim().equalsIgnoreCase("true")) {
                                                fieldComboDataStr += fieldComboData.getId() + ",";
                                            }
                                            dataArrIndex++;
                                        }

                                        if (!StringUtil.isNullOrEmpty(fieldComboDataStr)) {
                                            customJObj.put("Col" + params.getColnum(), fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                            customJObj.put("fieldDataVal", fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                        } else {
                                            continue;
                                        }
                                    } else {
                                        customJObj.put("Col" + params.getColnum(), recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim());
                                        customJObj.put("fieldDataVal", recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim());
                                    }

                                    customJObj.put("Custom_" + params.getFieldlabel(), "Col" + params.getColnum());

                                    lineCustomJArr.put(customJObj);
                                }
                            }
                        }

                        String lineCustomfield = lineCustomJArr.toString();

                        if (!StringUtil.isNullOrEmpty(lineCustomfield)) {
                            JSONArray jcustomarray = new JSONArray(lineCustomfield);
                            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                            customrequestParams.put("customarray", jcustomarray);
                            customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                            customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);
                            customrequestParams.put("modulerecid", jed.getID());
                            customrequestParams.put("recdetailId", row.getInventory().getID());
                            customrequestParams.put("moduleid", Constants.Acc_Invoice_ModuleId);
                            customrequestParams.put("companyid", companyID);
                            customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                jedjson.put("accjedetailcustomdata", jed.getID());
                                jedjson.put("jedid", jed.getID());
                                jedresult = accJournalEntryobj.updateJournalEntryDetails(jedjson);
                            }
                        }

                        rows.add(row);
                        System.out.println(invjson.get("entrynumber")); // remove after test
                    } catch (Exception ex) {
                        failed++;
                        isRecordFailed=true;
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

            // update invioce for last record
            if (!isAlreadyExist && !isRecordFailed) {
                updateInvoice(requestParams1, invjson, rows, jeDataMap, prodList, totalamount, jeDetails, customfield, totaldiscount);
            }

            if (failed > 0) {
                createFailureFiles(fileName, failedRecords, ".csv");
            }

            int success = total - failed;
            if (total == 0) {
                msg = "Empty file.";
            } else if (success == 0) {
//                issuccess = false;
                msg = "Failed to import all the records.";
            } else if (success == total) {
                msg = "All records are imported successfully.";
            } else {
                msg = "Imported " + success + " record" + (success > 1 ? "s" : "") + " successfully";
                msg += (failed == 0 ? "." : " and failed to import " + failed + " record" + (failed > 1 ? "s" : "") + ".");
            }

            try {
                txnManager.commit(status);
            } catch (Exception ex) {
                commitedEx = true;
                throw ex;
            }
        } catch (Exception ex) {
            if (!commitedEx) { // if exception occurs during commit then dont call rollback
                txnManager.rollback(status);
            }
            issuccess = false;
            msg = "" + ex.getMessage();

            Logger.getLogger(ImportInvoice.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Importing Records.");
        } finally {
            fileInputStream.close();
            br.close();

            DefaultTransactionDefinition ldef = new DefaultTransactionDefinition();
            ldef.setName("import_Tx");
            ldef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus lstatus = txnManager.getTransaction(ldef);

            try {
                // Insert Integration log
                HashMap<String, Object> logDataMap = new HashMap<String, Object>();
                logDataMap.put("FileName", getActualFileName(fileName));
                logDataMap.put("StorageName", fileName);
                logDataMap.put("Log", msg);
                logDataMap.put("Type", "csv");
                logDataMap.put("FailureFileType", failed>0?"csv":"");
                logDataMap.put("TotalRecs", total);
                logDataMap.put("Rejected", failed);
                logDataMap.put("Module", Constants.Acc_Invoice_ModuleId);
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userId);
                logDataMap.put("Company", companyID);
                logDataMap.put("Id", logid);
                importDao.saveImportLog(logDataMap);
                txnManager.commit(lstatus);
            } catch (Exception ex) {
                txnManager.rollback(lstatus);
                Logger.getLogger(ImportInvoice.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", getActualFileName(fileName));
                returnObj.put("Module", Constants.Acc_Invoice_ModuleId);
            } catch (JSONException ex) {
                Logger.getLogger(ImportInvoice.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return returnObj;
    }

    public String createCSVrecord(Object[] listArray) {
        String rec = "";
        for (int i = 0; i < listArray.length; i++) {    //Discard columns id at index 0 and isvalid,invalidColumns, validationlog at last 3 indexes.
//            String s = (listArray[i]==null)?"":listArray[i].toString();
            rec += "\"" + (listArray[i] == null ? "" : listArray[i].toString().replaceAll("\"", "")) + "\",";
        }
        return rec;
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

    private String getSequenceFormatIDByName(String sequenceFormatName, String companyID) throws AccountingException {
        String sequenceFormatID = "";
        try {
            if (!StringUtil.isNullOrEmpty(sequenceFormatName) && !StringUtil.isNullOrEmpty(companyID)) {
                HashMap<String, Object> formatParams = new HashMap<String, Object>();
                formatParams.put("moduleid", Constants.Acc_Invoice_ModuleId);
                formatParams.put("name", sequenceFormatName);
                formatParams.put("companyid", companyID);

                KwlReturnObject retObj = accCompanyPreferencesObj.getSequenceFormat(formatParams);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    SequenceFormat format = (SequenceFormat) retObj.getEntityList().get(0);
                    sequenceFormatID = format.getID();
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Sequence Format.");
        }
        return sequenceFormatID;
    }

    private String getCostCenterIDByName(String costCenterName, String companyID) throws AccountingException {
        String costCenterID = "";
        try {
            if (!StringUtil.isNullOrEmpty(costCenterName) && !StringUtil.isNullOrEmpty(companyID)) {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                filter_names.add("company.companyID");
                filter_params.add(companyID);
                filter_names.add("name");
                filter_params.add(costCenterName);
                requestParams.put(Constants.filterNamesKey, filter_names);
                requestParams.put(Constants.filterParamsKey, filter_params);

                KwlReturnObject retObj = accCostCenterObj.getCostCenter(requestParams);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    CostCenter costCenter = (CostCenter) retObj.getEntityList().get(0);
                    costCenterID = costCenter.getID();
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Cost Center.");
        }
        return costCenterID;
    }

    private Customer getCustomerByCode(String customerCode, String companyID) throws AccountingException {
        Customer customer = null;
        try {
            if (!StringUtil.isNullOrEmpty(customerCode) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accCustomerDAOobj.getCustomerByCode(customerCode, companyID);
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

    private Customer getCustomerByCodeOrName(String customerCode, String companyID) throws AccountingException {
        Customer customer = null;
        try {
            if (!StringUtil.isNullOrEmpty(customerCode) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accCustomerDAOobj.getCustomerByCodeOrName(customerCode, companyID);
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

    private String getTermIDByName(String termName, String companyID) throws AccountingException {
        String termID = "";
        try {
            if (!StringUtil.isNullOrEmpty(termName) && !StringUtil.isNullOrEmpty(companyID)) {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("companyid", companyID);
                requestParams.put("termname", termName);


                KwlReturnObject retObj = accTermObj.getTerm(requestParams);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    Term term = (Term) retObj.getEntityList().get(0);
                    termID = term.getID();
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Term.");
        }
        return termID;
    }

    private String getSalesPersonIDByName(String salesPersonName, String companyID) throws AccountingException {
        String salesPersonID = "";
        try {
            if (!StringUtil.isNullOrEmpty(salesPersonName) && !StringUtil.isNullOrEmpty(companyID)) {
                HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                filter_names.add("company.companyID");
                filter_params.add(companyID);
                filter_names.add("masterGroup.ID");
                filter_params.add("15"); // For Geting Sales Person
                filter_names.add("value");
                filter_params.add(salesPersonName);
                filterRequestParams.put("filter_names", filter_names);
                filterRequestParams.put("filter_params", filter_params);

                KwlReturnObject retObj = accMasterItemsDAOobj.getMasterItems(filterRequestParams);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    MasterItem salesPerson = (MasterItem) retObj.getEntityList().get(0);
                    salesPersonID = salesPerson.getID();
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Sales Person.");
        }
        return salesPersonID;
    }

    private Product getProductByProductID(String productID, String companyID) throws AccountingException {
        Product product = null;
        try {
            if (!StringUtil.isNullOrEmpty(productID) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accProductObj.getProductByProductID(productID, companyID);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    product = (Product) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Product");
        }
        return product;
    }

    private UnitOfMeasure getUOMByName(String productUOMName, String companyID) throws AccountingException {
        UnitOfMeasure uom = null;
        try {
            if (!StringUtil.isNullOrEmpty(productUOMName) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accProductObj.getUOMByName(productUOMName, companyID);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    uom = (UnitOfMeasure) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Unit of Measure");
        }
        return uom;
    }

    private void updateInvoice(HashMap<String, Object> requestParams1, JSONObject invjson, HashSet<InvoiceDetail> invcdetails, Map<String, Object> jeDataMap, ArrayList<String> prodList, double totalAmount, HashSet jeDetails, String customfield, double discValue) throws AccountingException {
        try {
            String companyid = (String) requestParams1.get("companyid");
            HashMap<String, Object> requestParams = requestParams1;
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), invjson.getString("companyid"));
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);

            double totalInvAmount = totalAmount - discValue;
            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, totalInvAmount, invjson.getString("currencyid"), (Date) invjson.get("entrydate"), invjson.getDouble("externalCurrencyRate"));
            double totalInvAmountinBase = (Double) bAmt.getEntityList().get(0);

            invjson.put(Constants.invoiceamountdue, totalInvAmount);
            invjson.put(Constants.invoiceamountdueinbase, totalInvAmountinBase);
            invjson.put(Constants.invoiceamount, totalInvAmount);
            invjson.put(Constants.invoiceamountinbase, totalInvAmountinBase);
            ArrayList amountApprove = (accountingHandlerDAOobj.getApprovalFlagForAmount(totalInvAmountinBase, Constants.CUSTOMER_INVOICE_APPROVAL, Constants.TRANS_AMOUNT, invjson.getString("companyid")));
            boolean amountExceed = (Boolean) amountApprove.get(0);

            ArrayList prodApprove = (accountingHandlerDAOobj.getApprovalFlagForProducts(prodList, Constants.CUSTOMER_INVOICE_APPROVAL, Constants.TRANS_PRODUCT, invjson.getString("companyid")));
            boolean prodExists = (Boolean) prodApprove.get(0);

            boolean isDraft=invjson.optBoolean("isDraft", false);
            boolean pendingApprovalFlagForDisc = false;

            int pendingApprovalFlag = 0;
            int approvalLevel = 11;

            if(isDraft){
                pendingApprovalFlag=0;
                approvalLevel=11;
            } else {
                 pendingApprovalFlag = (amountExceed || prodExists || pendingApprovalFlagForDisc) ? 1 : 0; // No need of approval if transaction is created as only template
                 approvalLevel = ((Integer) (amountApprove.get(1)) > (Integer) (prodApprove.get(1))) ? (Integer) (amountApprove.get(1)) : (Integer) (prodApprove.get(1));
            }
            
            JSONObject jedjson = new JSONObject();
            jedjson.put("srno", jeDetails.size() + 1);
            jedjson.put("companyid", invjson.getString("companyid"));
            jedjson.put("amount", totalInvAmount);
            jedjson.put("accountid", invjson.getString("accountid"));
            jedjson.put("debit", true);
            jedjson.put("jeid", invjson.get("journalerentryid"));
            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jeDetails.add(jed);

            invjson.put("customerentryid", jed.getID());

            if (discValue > 0) {
                jedjson = new JSONObject();
                jedjson.put("srno", jeDetails.size() + 1);
                jedjson.put("companyid", invjson.getString("companyid"));
                jedjson.put("amount", discValue);
                jedjson.put("accountid", preferences.getDiscountGiven().getID());
                jedjson.put("debit", true);
                jedjson.put("jeid", invjson.get("journalerentryid"));
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jeDetails.add(jed);
            }

            jeDataMap.put("jedetails", jeDetails);
            jeDataMap.put("pendingapproval", pendingApprovalFlag);
            jeDataMap.put("externalCurrencyRate", invjson.getDouble("externalCurrencyRate"));
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap); // Add Journal entry details
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);

            /**
             * ************************************ For saving custom fields
             * *********************************************
             */
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                customrequestParams.put("modulerecid", journalEntry.getID());
                customrequestParams.put("moduleid", Constants.Acc_Invoice_ModuleId);
                customrequestParams.put("companyid", invjson.getString("companyid"));
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    jeDataMap.put("accjecustomdataref", journalEntry.getID());
                    jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);
                }
            }
            /**
             * *************************************************************************************************************
             */
            KwlReturnObject ERresult = accCurrencyDAOobj.getExcDetailID(requestParams, invjson.getString("currencyid"), (Date) invjson.get("entrydate"), null);
            ExchangeRateDetails erd = (ExchangeRateDetails) ERresult.getEntityList().get(0);
            String erdid = (erd == null) ? null : erd.getID();
            invjson.put("erdid", erdid);

            invjson.put("pendingapproval", pendingApprovalFlag);
            invjson.put("istemplate", 0);
            invjson.put("approvallevel", approvalLevel);
            invjson.put("incash", false);

            // Used to save billing and shipping addresses
            boolean isDefaultAddress = true;
            Map<String, Object> addressParams = new HashMap<String, Object>();
            if (isDefaultAddress) {
                KwlReturnObject custResult = accountingHandlerDAOobj.getObject(Customer.class.getName(), invjson.getString("customerid"));
                Customer customer = (Customer) custResult.getEntityList().get(0);
                addressParams = AccountingAddressManager.getDefaultCustomerAddressParams(customer.getID(), companyid, accountingHandlerDAOobj);
            } else {
                addressParams = (Map<String, Object>) requestParams.get("addressParams"); //AccountingManager.getAddressParams(request)
            }
            KwlReturnObject addressresult = accountingHandlerDAOobj.saveAddressDetail(addressParams, invjson.getString("companyid"));
            BillingShippingAddresses bsa = (BillingShippingAddresses) addressresult.getEntityList().get(0);
            invjson.put("billshipAddressid", bsa.getID());
            
            
            invjson.put(Constants.Checklocktransactiondate, invjson.get("entrydate"));
            KwlReturnObject result = accInvoiceDAOobj.addInvoice(invjson, new HashSet());
            Invoice invoice = (Invoice) result.getEntityList().get(0); // Create Invoice without invoice-details.

            Iterator itr = invcdetails.iterator();
            while (itr.hasNext()) {
                InvoiceDetail ivd = (InvoiceDetail) itr.next();
                if (pendingApprovalFlag == 1 && ivd.getInventory().isInvrecord()) {
                    Inventory invtry = ivd.getInventory();
                    invtry.setActquantity(invtry.getQuantity());
                    invtry.setQuantity(0);
                }
                ivd.setInvoice(invoice);
            }
            invjson.put("invoiceid", invoice.getID());
            result = accInvoiceDAOobj.updateInvoice(invjson, invcdetails);
//            invoice = (Invoice) result.getEntityList().get(0); // Add invoice details 
        } catch (Exception ex) {
            Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException(ex.getMessage());
        }
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

    public static String getActualFileName(String storageName) {
        String ext = storageName.substring(storageName.lastIndexOf("."));
        String actualName = storageName.substring(0, storageName.lastIndexOf("_"));
        actualName = actualName + ext;
        return actualName;
    }

    private String getCurrencyId(String currencyName, HashMap currencyMap) {
        String currencyId = "1";
        if (currencyMap != null && currencyMap.containsKey(currencyName)) {
            currencyId = currencyMap.get(currencyName).toString();
        }
        return currencyId;
    }
}
