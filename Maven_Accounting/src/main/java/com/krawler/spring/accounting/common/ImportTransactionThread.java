/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.common;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.admin.ImportLog;
import com.krawler.common.admin.InventoryLocation;
import com.krawler.common.admin.InventoryWarehouse;
import com.krawler.common.admin.KWLDateFormat;
import com.krawler.common.admin.Modules;
import com.krawler.common.admin.NewBatchSerial;
import com.krawler.common.admin.NewProductBatch;
import com.krawler.common.admin.StoreMaster;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.CsvReader;
import com.krawler.common.util.DataInvalidateException;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.Customer;
import com.krawler.hql.accounting.GoodsReceipt;
import com.krawler.hql.accounting.InvoiceTermsSales;
import com.krawler.hql.accounting.MasterItem;
import com.krawler.hql.accounting.PaymentMethod;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.Producttype;
import com.krawler.hql.accounting.Tax;
import com.krawler.hql.accounting.UnitOfMeasure;
import com.krawler.hql.accounting.Vendor;
import com.krawler.hql.accounting.invoice.service.AccInvoiceModuleService;
import com.krawler.hql.accounting.invoice.service.AccInvoiceModuleServiceImpl;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesCMN;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import static com.krawler.spring.accounting.goodsreceipt.GoodsReceiptConstants.DESC;
import static com.krawler.spring.accounting.goodsreceipt.GoodsReceiptConstants.PRDISCOUNT;
import static com.krawler.spring.accounting.goodsreceipt.GoodsReceiptConstants.PRODUCTID;
import static com.krawler.spring.accounting.goodsreceipt.GoodsReceiptConstants.RATE;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptController;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.goodsreceipt.service.accGoodsReceiptModuleService;
import com.krawler.spring.accounting.goodsreceipt.service.accGoodsReceiptModuleServiceImpl;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.invoice.accInvoiceController;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.product.accProductController;
import com.krawler.spring.accounting.salesorder.accSalesOrderService;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.importFunctionality.ImportDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.context.MessageSource;
import com.krawler.spring.accounting.productmodule.service.AccProductModuleService;
import com.krawler.spring.accounting.purchaseorder.service.AccPurchaseOrderModuleService;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.kwlCommonTablesDAO;

/**
 *
 * @author krawler
 */
public class ImportTransactionThread implements Runnable {

    private ArrayList processQueue = new ArrayList();
    private boolean isworking = false;
    private ImportDAO importDAOobj;
    private ImportHandler importHandlerobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accSalesOrderService accSalesOrderServiceobj;
    private MessageSource messageSource;
    public accCustomerDAO accCustomerDAOobj;
    private accAccountDAO accAccountDAOobj;
    private accMasterItemsDAO accMasterItemsDAOobj;
    private AccProductModuleService accProductModuleServiceobj;
    private AccInvoiceModuleService accInvoiceModuleService;
    private accInvoiceDAO accInvoiceDAOobj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private kwlCommonTablesDAO kwlCommonTablesDAOobj;
    private authHandlerDAO authHandlerDAOObj;
    private AccPurchaseOrderModuleService accPurchaseOrderModuleServiceObj;
    private accGoodsReceiptDAO accGoodsReceiptobj;
    private accTaxDAO accTaxObj;
    private accGoodsReceiptModuleService accGoodsReceiptModuleServiceobj;

    public boolean isIsworking() {
        return isworking;
    }

    public void setIsworking(boolean isworking) {
        this.isworking = isworking;
    }

    public void add(JSONObject requestParams) {
        try {
            processQueue.add(requestParams);
        } catch (Exception ex) {
            Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setimportDAO(ImportDAO importDAOobj) {
        this.importDAOobj = importDAOobj;
    }

    public void setimportHandler(ImportHandler importHandlerobj) {
        this.importHandlerobj = importHandlerobj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setAccSalesOrderServiceobj(accSalesOrderService accSalesOrderServiceobj) {
        this.accSalesOrderServiceobj = accSalesOrderServiceobj;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public void setAccCustomerDAO(accCustomerDAO accCustomerDAOobj) {
        this.accCustomerDAOobj = accCustomerDAOobj;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setaccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }

    public void setAccProductModuleService(AccProductModuleService accProductModuleServiceobj) {
        this.accProductModuleServiceobj = accProductModuleServiceobj;
    }

    public void setAccInvoiceModuleService(AccInvoiceModuleService accInvoiceModuleService) {
        this.accInvoiceModuleService = accInvoiceModuleService;
    }

    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOobj) {
        this.kwlCommonTablesDAOobj = kwlCommonTablesDAOobj;
    }

    public void setauthHandlerDAO(authHandlerDAO authHandlerDAOObj1) {
        this.authHandlerDAOObj = authHandlerDAOObj1;
    }

    public void setAccPurchaseOrderModuleServiceObj(AccPurchaseOrderModuleService accPurchaseOrderModuleServiceObj) {
        this.accPurchaseOrderModuleServiceObj = accPurchaseOrderModuleServiceObj;
    }

    public void setaccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
    }

    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }

    public void setaccGoodsReceiptModuleService(accGoodsReceiptModuleService accGoodsReceiptModuleServiceobj) {
        this.accGoodsReceiptModuleServiceobj = accGoodsReceiptModuleServiceobj;
    }

    public String addPendingImportLog(JSONObject requestParams) {
        String logId = null;
        try {
            //Insert Integration log
            String fileName = (String) requestParams.get("filename");
            String Module = (String) requestParams.get("modName");
            try {
                List list = importDAOobj.getModuleObject(Module);
                Modules module = (Modules) list.get(0); //Will throw null pointer if no module entry found
                Module = module.getId();
            } catch (Exception ex) {
                throw new DataInvalidateException("Column config not available for module " + Module);
            }
            HashMap<String, Object> logDataMap = new HashMap<String, Object>();
            logDataMap.put("FileName", ImportLog.getActualFileName(fileName));
            logDataMap.put("StorageName", fileName);
            logDataMap.put("Log", "Pending");
            logDataMap.put("Type", fileName.substring(fileName.lastIndexOf(".") + 1));
            logDataMap.put("Module", Module);
            logDataMap.put("ImportDate", new Date());
            logDataMap.put("User", (String) requestParams.get("userid"));
            logDataMap.put("Company", (String) requestParams.get("companyid"));
            ImportLog importlog = (ImportLog) importDAOobj.saveImportLog(logDataMap);
            logId = importlog.getId();
        } catch (Exception ex) {
            logId = null;
        }
        return logId;
    }

    public JSONObject importInvoicesJSON(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        try {
            jobj = importInvoiceRecordsForCSV(paramJobj);
        } catch (Exception ex) {
            try {
                jobj.put(Constants.RES_success, false);
                jobj.put(Constants.RES_msg, "" + ex.getMessage());
            } catch (JSONException jex) {
                Logger.getLogger(AccInvoiceModuleServiceImpl.class.getName()).log(Level.SEVERE, null, jex);
            }
        }
        return jobj;
    }

    public JSONObject importInvoiceRecordsForCSV(JSONObject requestJobj) throws AccountingException, IOException, JSONException {
        JSONObject returnObj = new JSONObject();
        String msg = "";
        int total = 0, failed = 0;
        String fileName = requestJobj.getString("filename");
        String companyID = requestJobj.getString(Constants.companyKey);
        String masterPreference = requestJobj.getString("masterPreference");
        boolean issuccess = true;
        boolean isAlreadyExist = false;
        boolean isRecordFailed = false;
        boolean isGenerateDeliveryOrder = false;
        FileInputStream fileInputStream = null;
        CsvReader csvReader = null;
        HashMap<String, Object> invoiceDataMap = new HashMap<>();
        Map<String, Object> rowDetailMap = new HashMap<>();
        Map<String, List<JSONObject>> batchMap = new HashMap<>();
        Map<String, List<Object>> batchSerialMap = new HashMap<>();
        JSONArray batchDetailArr = new JSONArray();
        Set<String> uniqueLineLevelRecordKeyList = new HashSet<>(); // Hashset to identify duplicate line level and inner line level record (InvoiceNumber + Product + Warehouse + Location + Batch)
        JSONObject paramJobj = new JSONObject();
        JSONArray rows = new JSONArray();
        JSONArray invoiceTermDetailsArray = new JSONArray();
        String prevRow = "";
        String prevInvNo = "";
        double totaldiscount = 0, totalamount = 0;
        double totalBatchQty = 0;
        String entryNumber = "";
        boolean imported = false;
        boolean isCashTransaction = requestJobj.optBoolean("incash", false);

        try {
            String dateFormat = null, dateFormatId = requestJobj.getString("dateFormat");
            if (!StringUtil.isNullOrEmpty(dateFormatId)) {
                KwlReturnObject kdfObj = accountingHandlerDAOobj.getObject(KWLDateFormat.class.getName(), dateFormatId);
                KWLDateFormat kdf = (KWLDateFormat) kdfObj.getEntityList().get(0);

                dateFormat = kdf != null ? kdf.getJavaForm() : null;
            }
            DateFormat df = new SimpleDateFormat(dateFormat);
            df.setLenient(false);
            requestJobj.put(Constants.importdf, df); //sending this format for Processing batch details

            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");
            boolean isSlaesTerm = true;
            LinkedHashMap<String, InvoiceTermsSales> salesTermMap = getSlaestermMapByNameKey(companyID, isSlaesTerm);

            KwlReturnObject extraPref = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyID);
            ExtraCompanyPreferences extrareferences = (ExtraCompanyPreferences) extraPref.getEntityList().get(0);
            Boolean isCurrencyCode = extrareferences.isCurrencyCode();

            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyID);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
            File filePath = new File(destinationDirectory + File.separator + fileName);
            fileInputStream = new FileInputStream(filePath);
            String delimiterType = requestJobj.getString("delimiterType");
            csvReader = new CsvReader(new InputStreamReader(fileInputStream), delimiterType);

            JSONObject resjson = new JSONObject(requestJobj.getString("resjson"));
            JSONArray jSONArray = resjson.getJSONArray("root");
            HashMap<String, Integer> columnConfig = new HashMap<>();
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
                columnConfig.put(jSONObject.getString("dataindex"), jSONObject.getInt("csvindex"));
            }
            Set invoiceTermIDsForSingleInvoice = new HashSet<>();
            int cnt = 0;
            StringBuilder failedRecords = new StringBuilder();
            StringBuilder singleInvociceFailedRecords = new StringBuilder();// Invoive with one row of failure record then all rows will be included in failure file
            int singleInvoiceFailureRecoredCount = 0;//  count of total invoice rows in import file
            Set<String> failureList = new HashSet<>(); // set of invoice having failyure record's

            HashMap currencyMap = accSalesOrderServiceobj.getCurrencyMap(isCurrencyCode);

            String logId = null;
            logId = addPendingImportLog(requestJobj);
            requestJobj.put("logId",logId);

            while (csvReader.readRecord()) {
                String failureMsg = "";
                boolean isfailurerecord = false; // used to keep track Invoice Row  failure/correct
                String[] recarr = csvReader.getValues();

                if (cnt == 0) {
                    failedRecords.append(accSalesOrderServiceobj.createCSVrecord(recarr)).append("\" \"");
                } else if (cnt == 1) {
                    failedRecords.append("\n").append(accSalesOrderServiceobj.createCSVrecord(recarr)).append("\"Error Message\"");
                } else {
                    try {
                        String currencyID = requestJobj.getString(Constants.globalCurrencyKey);
                        /**
                         * ***************************Validating Global Level
                         * Data ****************************
                         */
                        entryNumber = "";
                        if (columnConfig.containsKey("invoiceNumber")) {
                            entryNumber = recarr[(Integer) columnConfig.get("invoiceNumber")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(entryNumber)) {
                                failureMsg += "Invoice Number is not available. ";
                            }
                        } else {
                            failureMsg += "Invoice Number column is not found. ";
                        }

                        Date billDate = null;
                        if (columnConfig.containsKey("billdate")) {
                            String customerQutationDateStr = recarr[(Integer) columnConfig.get("billdate")].replaceAll("\"", "").trim();

                            if (StringUtil.isNullOrEmpty(customerQutationDateStr)) {
                                failureMsg += "Invoice Date is not available. ";
                            } else {
                                try {
                                    billDate = df.parse(customerQutationDateStr);
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect date format for Invoice Date, Please specify values in " + dateFormat + " format. ";
                                }
                            }
                        } else {
                            failureMsg += "Invoice Date column is not found. ";
                        }

                        String poRefNumber = "";
                        if (columnConfig.containsKey("poRefNumber")) {
                            poRefNumber = recarr[(Integer) columnConfig.get("poRefNumber")].replaceAll("\"", "").trim();
                        }
                        String exportDeclarationNo = "";
                        if (columnConfig.containsKey(Constants.importExportDeclarationNo)) {
                            exportDeclarationNo = recarr[columnConfig.get(Constants.importExportDeclarationNo)].replaceAll("\"", "").trim();
                        }

                        String costCenterID = "";
                        if (columnConfig.containsKey("costcenter")) {
                            String costCenterName = recarr[(Integer) columnConfig.get("costcenter")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(costCenterName)) {
                                costCenterID = accSalesOrderServiceobj.getCostCenterIDByName(costCenterName, companyID);
                                if (StringUtil.isNullOrEmpty(costCenterID)) {
                                    failureMsg += "Cost Center is not found for name " + costCenterName + ". ";
                                }
                            }
                        }

                        Date dueDate = null;
                        if (!isCashTransaction) {
                            if (columnConfig.containsKey("dueDate")) {
                                String dueDateStr = recarr[(Integer) columnConfig.get("dueDate")].replaceAll("\"", "").trim();

                                if (StringUtil.isNullOrEmpty(dueDateStr)) {
                                    failureMsg += "Due Date is not available. ";
                                } else {
                                    dueDate = df.parse(dueDateStr);
                                }
                            } else {
                                failureMsg += "Due Date column is not found. ";
                            }
                        }

                        String customerID = "";

                        /*
                         * 1. Customer Code
                         */
                        if (columnConfig.containsKey("CustomerCode")) {
                            String customerCode = recarr[(Integer) columnConfig.get("CustomerCode")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(customerCode)) {
                                Customer customer = accInvoiceModuleService.getCustomerByCode(customerCode, companyID);
                                if (customer != null) {
                                    customerID = customer.getID();
                                } else {
                                    throw new AccountingException(messageSource.getMessage("acc.field.CustomerCodeisnotavailable", null, Locale.forLanguageTag(requestJobj.getString(Constants.language))) + customerCode);
                                }
                            }
                        }

                        /*
                         * 2. Customer Name if customerID is empty it menas
                         * customer is not found for given code. so need to
                         * serch data on name
                         */
                        if (StringUtil.isNullOrEmpty(customerID)) {
                            if (columnConfig.containsKey("CustomerName")) {
                                String customerName = recarr[(Integer) columnConfig.get("CustomerName")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(customerName)) {
                                    Customer customer = null;
                                    KwlReturnObject retObj = accCustomerDAOobj.getCustomerByName(customerName, companyID);
                                    if (retObj != null && !retObj.getEntityList().isEmpty()) {
                                        customer = (Customer) retObj.getEntityList().get(0);
                                    }
                                    if (customer != null) {
                                        customerID = customer.getID();
                                    } else {
                                        failureMsg += messageSource.getMessage("acc.field.CustomerisnotfoundforCustomerCodeName", null, Locale.forLanguageTag(requestJobj.getString(Constants.language)));
                                    }
                                } else {
                                    failureMsg += messageSource.getMessage("acc.field.CustomerisnotfoundforCustomerCodeName", null, Locale.forLanguageTag(requestJobj.getString(Constants.language)));
                                }
                            } else {
                                failureMsg += messageSource.getMessage("acc.field.CustomerisnotfoundforCustomerCodeName", null, Locale.forLanguageTag(requestJobj.getString(Constants.language)));
                            }
                        }

                        String termID = "";
                        if (!isCashTransaction) {
                            if (columnConfig.containsKey("terms")) {
                                String termName = recarr[(Integer) columnConfig.get("terms")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(termName)) {
                                    termID = accSalesOrderServiceobj.getTermIDByName(termName, companyID);
                                    if (StringUtil.isNullOrEmpty(termID)) {
                                        failureMsg += "Credit Term is not found for name " + termName + ". ";
                                    }
                                } else {
                                    failureMsg += "Credit Term is not available. ";
                                }
                            } else {
                                failureMsg += "Credit Term column is not found. ";
                            }
                        }

                        String memo = "";
                        if (columnConfig.containsKey(Constants.memo)) {
                            memo = recarr[(Integer) columnConfig.get(Constants.memo)].replaceAll("\"", "").trim();
                        }

                        String shipVia = "";
                        if (columnConfig.containsKey("shipvia")) {
                            shipVia = recarr[(Integer) columnConfig.get("shipvia")].replaceAll("\"", "").trim();
                        }
                        /*
                         Ship date as Delivery Date for Invoice module
                         */
                        Date shipDate = null;
                        if (columnConfig.containsKey("shipdate")) {
                            String shipDateStr = recarr[(Integer) columnConfig.get("shipdate")].replaceAll("\"", "").trim();

                            if (!StringUtil.isNullOrEmpty(shipDateStr)) {
                                try {
                                    shipDate = df.parse(shipDateStr);
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect date format for Ship Date, Please specify values in " + dateFormat + " format. ";
                                }
                            }
                        }

                        String fob = "";
                        if (columnConfig.containsKey(Constants.fob)) {
                            fob = recarr[(Integer) columnConfig.get(Constants.fob)].replaceAll("\"", "").trim();
                        }

                        String salesPersonID = "";
                        if (columnConfig.containsKey("salesperson")) {
                            String salesPersonName = recarr[(Integer) columnConfig.get("salesperson")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(salesPersonName)) {
                                salesPersonID = accSalesOrderServiceobj.getSalesPersonIDByName(salesPersonName, companyID);
                                if (StringUtil.isNullOrEmpty(salesPersonID)) {
                                    failureMsg += "Sales Person is not found for name " + salesPersonName + ". ";
                                }
                            }
                        }

                        if (isCurrencyCode ? columnConfig.containsKey("currencyCode") : columnConfig.containsKey("currencyName")) {
                            String currencyStr = isCurrencyCode ? recarr[(Integer) columnConfig.get("currencyCode")].replaceAll("\"", "").trim() : recarr[(Integer) columnConfig.get("currencyName")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(currencyStr)) {
                                currencyID = accSalesOrderServiceobj.getCurrencyId(currencyStr, currencyMap);

                                if (StringUtil.isNullOrEmpty(currencyID)) {
                                    failureMsg += messageSource.getMessage("acc.field.ImportProductExceptionFormat", null, Locale.forLanguageTag(requestJobj.getString(Constants.language))) + ". ";
                                }
                            } else {
                                if (!masterPreference.equalsIgnoreCase("1")) {
                                    failureMsg += "Currency is not available. ";
                                }
                            }
                        }

                        isGenerateDeliveryOrder = false;
                        if (columnConfig.containsKey("generateDeliveryOrder")) {
                            String generateGoodsReceipt = recarr[(Integer) columnConfig.get("generateDeliveryOrder")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(generateGoodsReceipt)) {
                                if (generateGoodsReceipt.equalsIgnoreCase("TRUE")) {
                                    isGenerateDeliveryOrder = true;
                                } else if (generateGoodsReceipt.equalsIgnoreCase("FALSE")) {
                                    isGenerateDeliveryOrder = false;
                                } else {
                                    throw new AccountingException("Format you entered for Generate Delivery Order is not correct. It should be like \"TRUE\" or \"FALSE\"");
                                }
                            } else {
                                isGenerateDeliveryOrder = false;
                            }
                        }

                        String deliveryOrderNo = "";
                        if (columnConfig.containsKey("deliveryOrderNo")) {
                            deliveryOrderNo = recarr[(Integer) columnConfig.get("deliveryOrderNo")].replaceAll("\"", "").trim();
                            if (isGenerateDeliveryOrder) {
                                if (StringUtil.isNullOrEmpty(deliveryOrderNo)) {
                                    failureMsg += "Delivery Order Number is not available. ";
                                }
                            }
                        }

                        boolean isIncludingGST = false;
                        /**
                         * If Avalara Integration is enabled, then includingGST
                         * flag is always false
                         */
                        if (columnConfig.containsKey("gstIncluded") && !extrareferences.isAvalaraIntegration()) {
                            String isIncludingGSTStr = recarr[(Integer) columnConfig.get("gstIncluded")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(isIncludingGSTStr)) {
                                if (isIncludingGSTStr.equalsIgnoreCase("TRUE")) {
                                    isIncludingGST = true;
                                } else if (isIncludingGSTStr.equalsIgnoreCase("FALSE")) {
                                    isIncludingGST = false;
                                } else {
                                    failureMsg += "Format you entered for Include GST is not correct. It should be like \"TRUE\" or \"FALSE\". ";
                                }
                            }
                        }

                        boolean isIncludeProductTax = false;
                        if (columnConfig.containsKey("includeprotax")) {
                            String isIncludeProductTaxStr = recarr[(Integer) columnConfig.get("includeprotax")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(isIncludeProductTaxStr)) {
                                if (isIncludeProductTaxStr.equalsIgnoreCase("Yes")) {
                                    isIncludeProductTax = true;
                                } else if (isIncludeProductTaxStr.equalsIgnoreCase("No")) {
                                    isIncludeProductTax = false;
                                } else {
                                    failureMsg += "Format you entered for Include Product Tax is not correct. It should be like \"Yes\" or \"No\". ";
                                }
                            }
                        }

                        if (isIncludingGST && !isIncludeProductTax) {
                            failureMsg += "If value Including GST is \"TRUE\" then value of Include Product Tax should be \"Yes\". ";
                        }

                        boolean isIncludeTotalTax = false;
                        if (columnConfig.containsKey("taxincluded")) {
                            String isIncludeTotalTaxStr = recarr[(Integer) columnConfig.get("taxincluded")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(isIncludeTotalTaxStr)) {
                                if (isIncludeTotalTaxStr.equalsIgnoreCase("Yes")) {
                                    isIncludeTotalTax = true;
                                } else if (isIncludeTotalTaxStr.equalsIgnoreCase("No")) {
                                    isIncludeTotalTax = false;
                                } else {
                                    failureMsg += "Format you entered for Include Total Tax is not correct. It should be like \"Yes\" or \"No\". ";
                                }
                            }
                        }

                        if (isIncludeProductTax && isIncludeTotalTax) {
                            failureMsg += "If value of Include Product Tax is \"Yes\" then value of Include Total Tax should be \"No\".";
                        }

                        String taxID = "";
                        if (columnConfig.containsKey("taxid")) {
                            String taxCode = recarr[(Integer) columnConfig.get("taxid")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(taxCode)) {
//                                Tax tax = accSalesOrderServiceobj.getGSTByCode(taxCode, companyID);
                                Map taxMap = new HashMap<>();
                                taxMap.put(Constants.companyKey, companyID);
                                taxMap.put(Constants.TAXCODE, taxCode);
                                ArrayList taxList = importHandlerobj.getTax(taxMap);
                                if (taxList.get(0) != null) {
                                    Tax tax = (Tax) taxList.get(0);
                                    if (tax.getTaxtype() == Constants.PURCHASE_TYPE_TAX && isIncludeTotalTax) {
                                        failureMsg += "Tax Code is not Sales Type Tax for code " + taxCode;
                                    } else {
                                        taxID = tax.getID();
                                    }
                                    
                                } else if (!StringUtil.isNullOrEmpty((String) taxList.get(2))) {
                                    failureMsg += (String) taxList.get(2) + taxCode;
                                }
                            } else {
                                if (!isIncludingGST && !isIncludeProductTax && isIncludeTotalTax) {
                                    failureMsg += "Tax Code is not available. ";
                                }
                            }
                        } else {
                            if (!isIncludingGST && !isIncludeProductTax && isIncludeTotalTax) {
                                failureMsg += "Tax Code column is not found. ";
                            }
                        }

                        /**
                         * *************************** Validating Invoice Term
                         * Data ****************************
                         */
                        boolean isNewInvoiceRecord = (!prevInvNo.equalsIgnoreCase(entryNumber) || entryNumber.equalsIgnoreCase(""));
                        if (isNewInvoiceRecord) {
                            invoiceTermIDsForSingleInvoice = new HashSet<>();
                        }
                        boolean isAllInvoiceTermDataisEmpty = isAllTermInvoiceInformationIsEmpty(columnConfig, recarr);
                        InvoiceTermsSales termsSales = null;
                        int termType = 0;
                        double termValue = 0;
                        if (!isAllInvoiceTermDataisEmpty) {
                            String termName = "";
                            if (columnConfig.containsKey("invoiceterm")) {
                                termName = recarr[(Integer) columnConfig.get("invoiceterm")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(termName)) {
                                    if (salesTermMap.containsKey(termName.toLowerCase()) && salesTermMap.get(termName.toLowerCase()) != null) {//while putting key in salesTermMap we have putted it as lowercase hence checkinh here with lowercase
                                        termsSales = salesTermMap.get(termName.toLowerCase());
                                    }
                                    if (termsSales == null) {
                                        failureMsg += "Invoice Term " + termName + " is not avilable.";
                                    }
                                    if (termsSales != null) {
                                        boolean added = invoiceTermIDsForSingleInvoice.add(termsSales.getId());
                                        if (!added) {
                                            /*If- sucessfully added it means user has applying term first time. 
                                             else- user has already applied and trying to give twice which is invalid case for single invoice */
                                            failureMsg += "Invoice Term " + termName + " is already applied for invoice " + entryNumber + ".";
                                        }
                                    }

                                }
                            } else {
                                failureMsg += "Invoice Term column is not found. ";
                            }

                            String termTypeStr = "";
                            if (columnConfig.containsKey("termtype")) {
                                termTypeStr = recarr[(Integer) columnConfig.get("termtype")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(termTypeStr)) {
                                    if (termTypeStr.equalsIgnoreCase("percentage")) {
                                        termType = 1;
                                    } else if (termTypeStr.equalsIgnoreCase("flat")) {
                                        termType = 2;
                                    } else {
                                        failureMsg += "Incorrect value " + termTypeStr + " found in column Term Type. It should be like \"Percentage\" or \"Flat\". ";
                                    }
                                } else {
                                    failureMsg += "Empty data found in column Term Type";
                                }
                            } else {
                                failureMsg += "Term Type column is not found. ";
                            }

                            String termValueStr = "";
                            if (columnConfig.containsKey("termvalue")) {
                                termValueStr = recarr[(Integer) columnConfig.get("termvalue")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(termValueStr)) {
                                    try {
                                        termValue = authHandler.round(Double.parseDouble(termValueStr), companyID);
                                    } catch (Exception ex) {
                                        failureMsg += "Incorrect numeric value for Term Value, Please ensure that value type of Term Value matches with the Term Value. ";
                                    }
                                } else {
                                    failureMsg += "Empty data found in column Term Value.";
                                }
                            } else {
                                failureMsg += "Term Value column is not found. ";
                            }
                        }

                        /**
                         * **********Code for validating Global Level Data is
                         * same or different************
                         */
                        if (!isNewInvoiceRecord && StringUtil.isNullOrEmpty(failureMsg) && paramJobj.length() > 0) {//when old invoice repeating
                            // Global Level Mandatory Fields
                            String mismatchColumns = "";
                            if (paramJobj.has("termid")) {// Credit term
                                if (!paramJobj.optString("termid", "").equals(termID)) {
                                    mismatchColumns += "Credit Term,";
                                }
                            }
                            if (paramJobj.has(Constants.currencyKey)) {// Currency
                                if (!paramJobj.optString(Constants.currencyKey, "").equals(currencyID)) {
                                    mismatchColumns += "Currency,";
                                }
                            }
                            if (paramJobj.has("customer")) {// Cudtomer Code
                                if (!paramJobj.optString("customer", "").equals(customerID)) {
                                    mismatchColumns += "Customer Code,";
                                }
                            }

                            if (paramJobj.has("numberDo")) {// Delivery Order Number
                                if (!paramJobj.optString("numberDo", "").equals(deliveryOrderNo)) {
                                    mismatchColumns += "Delivery Order Number,";
                                }
                            }
                            if (paramJobj.has("duedate") && !StringUtil.isNullOrEmpty(paramJobj.optString("duedate", null)) && dueDate != null) {// Due Date
                                if (!paramJobj.optString("duedate").equals(sdf.format(dueDate))) {
                                    mismatchColumns += "Due date,";
                                }
                            }
                            if (paramJobj.has("billdate") && !StringUtil.isNullOrEmpty(paramJobj.optString("billdate", null)) && billDate != null) {//// Invoice Date
                                if (!paramJobj.optString("billdate").equals(sdf.format(billDate))) {
                                    mismatchColumns += "Invoice Date,";
                                }
                            }

                            //Global Level Non Mandatory Fields
                            if (paramJobj.has("isAutoCreateDO")) {// Delivery Order Number
                                if (!paramJobj.optString("isAutoCreateDO", "").equals(String.valueOf(isGenerateDeliveryOrder))) {
                                    mismatchColumns += "Generate Delivery Order,";
                                }
                            } else if (isGenerateDeliveryOrder) {
                                mismatchColumns += "Generate Delivery Order,";
                            }

                            if (paramJobj.has("porefno")) {
                                if (!paramJobj.optString("porefno", "").equalsIgnoreCase(poRefNumber)) {
                                    mismatchColumns += "PO Reference Number,";
                                }
                            }
                            if (paramJobj.has("costcenter")) {
                                if (!paramJobj.optString("costcenter", "").equals(costCenterID)) {
                                    mismatchColumns += "Cost Center,";
                                }
                            }
                            if (paramJobj.has("salesPerson")) {
                                if (!paramJobj.optString("salesPerson", "").equals(salesPersonID)) {
                                    mismatchColumns += "Sales Person,";
                                }
                            }
                            if (paramJobj.has("includingGST")) {
                                if (!paramJobj.optString("includingGST", "").equals(String.valueOf(isIncludingGST))) {
                                    mismatchColumns += "Including GST,";
                                }
                            }
                            if (paramJobj.has("includeprotax")) {
                                if (!paramJobj.optString("includeprotax", "").equals(String.valueOf(isIncludeProductTax))) {
                                    mismatchColumns += "Include Product Tax,";
                                }
                            }
                            if (paramJobj.has("taxid")) {
                                if (isIncludeTotalTax && StringUtil.isNullOrEmpty(paramJobj.optString("taxid", ""))) {//This condition true menas column Include Total Tax has differnt value
                                    mismatchColumns += "Include Total Tax,";
                                } else {
                                    String matchTaxID = "";
                                    if (isIncludeTotalTax) {
                                        matchTaxID = taxID;
                                    }
                                    if (!paramJobj.optString("taxid", "").equals(matchTaxID)) {
                                        mismatchColumns += "Tax Code,";
                                    }
                                }
                            }
                            if (!StringUtil.isNullOrEmpty(mismatchColumns)) {
                                mismatchColumns = mismatchColumns.substring(0, mismatchColumns.length() - 1);
                                failureMsg += "Data for Global columns " + mismatchColumns + " are found different, data should be same at Global Level.";
                            }
                        }

                        /*
                         Below code is Cash Sales import related fields 
                         */
                        Date chequeDate = null;
                        Date clearanceDate = null;
                        PaymentMethod payMethod = null;
                        String ChequeNo = "";
                        String bankName = "";
                        String bankNameMasterItemID = "";
                        boolean cleared = false;
                        String ChequeDesc = "";
                        String payMethodId = "";
                        if (isCashTransaction) {
                            if (columnConfig.containsKey("paymentmethodid")) {
                                String paymentMethodStr = recarr[(Integer) columnConfig.get("paymentmethodid")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(paymentMethodStr)) {
                                    KwlReturnObject retObj = accMasterItemsDAOobj.getPaymentMethodIdFromName(paymentMethodStr, companyID);
                                    if (retObj != null && !retObj.getEntityList().isEmpty()) {
                                        payMethod = (PaymentMethod) retObj.getEntityList().get(0);
                                        payMethodId = payMethod.getID();
                                    }
                                    if (payMethod == null) {
                                        failureMsg += "Payment Method is not found for " + paymentMethodStr + ". ";
                                    }
                                } else {
                                    failureMsg += "Payment Method is not available. ";
                                }
                            } else {
                                failureMsg += "Payment Method column is not found. ";
                            }

                            if (columnConfig.containsKey("chequeno")) {
                                ChequeNo = recarr[(Integer) columnConfig.get("chequeno")].replaceAll("\"", "").trim();
                                failureMsg += importHandlerobj.isChequeNumberValidForImport(ChequeNo,false);
                            }

                            if (columnConfig.containsKey("bankname")) {
                                bankName = recarr[(Integer) columnConfig.get("bankname")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(bankName)) {
                                    KwlReturnObject returnObject = importDAOobj.getBankNameMasterItemName(companyID, bankName);
                                    if (returnObject.getEntityList().isEmpty()) {
                                        failureMsg += "Incorrect Bank Name type value for Bank Name. Please add new Bank Name as \"" + bankName + "\" with other details.";
                                    } else {
                                        MasterItem masterItem = (MasterItem) returnObject.getEntityList().get(0);
                                        bankNameMasterItemID = masterItem.getID();
                                    }
                                } else {
                                    if (payMethod != null && payMethod.getDetailType() == Constants.bank_detail_type) {
                                        failureMsg += "Empty data found in Bank Name, cannot set empty data for Bank Name if Payment Method is selected as Bank.";
                                    }
                                }
                            } else {
                                if (payMethod != null && payMethod.getDetailType() == Constants.bank_detail_type) {
                                    failureMsg += "Bank Name column is not found. ";
                                }
                            }

                            if (columnConfig.containsKey("chequedate")) {
                                String chequeDateStr = recarr[(Integer) columnConfig.get("chequedate")].replaceAll("\"", "").trim();

                                if (StringUtil.isNullOrEmpty(chequeDateStr)) {
                                    if (payMethod != null && payMethod.getDetailType() != PaymentMethod.TYPE_CASH) {
                                        failureMsg += "Cheque Date is not available. ";
                                    }
                                } else {
                                    try {
                                        chequeDate = df.parse(chequeDateStr);
                                    } catch (Exception ex) {
                                        failureMsg += "Incorrect date format for Cheque Date, Please specify values in " + dateFormat + " format. ";
                                    }
                                }
                            } else {
                                if (payMethod != null && payMethod.getDetailType() != PaymentMethod.TYPE_CASH) {
                                    failureMsg += "Cheque Date column is not found. ";
                                }
                            }

                            if (columnConfig.containsKey("paymentStatus")) {
                                String paymentStatusStr = recarr[(Integer) columnConfig.get("paymentStatus")].replaceAll("\"", "").trim();
                                if (StringUtil.isNullOrEmpty(paymentStatusStr)) {
                                    if (payMethod != null && payMethod.getDetailType() == Constants.bank_detail_type) {
                                        failureMsg += "Payment Status is not available. ";
                                    }
                                } else {
                                    if (paymentStatusStr.equalsIgnoreCase("Cleared")) {
                                        cleared = true;
                                    } else if (paymentStatusStr.equalsIgnoreCase("Uncleared")) {
                                        cleared = false;
                                    } else {
                                        failureMsg += "Incorrect Payment Status type value for Payment Status. It should be either Cleared or Uncleared. ";
                                    }
                                }
                            } else {
                                if (payMethod != null && payMethod.getDetailType() == Constants.bank_detail_type) {
                                    failureMsg += "Payment Status column is not found. ";
                                }
                            }

                            if (columnConfig.containsKey("clearanceDate")) {
                                if (payMethod.getDetailType() != PaymentMethod.TYPE_CASH && cleared) { // when payment type is other than cash and payment sttus is clear then only need of clerance date. So its validation
                                    String clearenceDateStr = recarr[(Integer) columnConfig.get("clearanceDate")].replaceAll("\"", "").trim();
                                    if (!StringUtil.isNullOrEmpty(clearenceDateStr)) {
                                        try {
                                            clearanceDate = df.parse(clearenceDateStr);
                                            if (chequeDate.compareTo(clearanceDate) > 0) {
                                                failureMsg += "Clearence date should be greter than Cheque date.";
                                            }
                                        } catch (ParseException ex) {
                                            failureMsg += "Incorrect date format for Clearence Date, Please specify values in " + dateFormat + " format.";
                                        }
                                    } else {
                                        failureMsg += "You have entered the Payment Status as Cleared. So you cannot set empty data for Clearence Date.";
                                    }
                                }
                            } else {
                                if (payMethod.getDetailType() != PaymentMethod.TYPE_CASH && cleared) {
                                    failureMsg += "Clearance Date column is not found. ";
                                }
                            }

                            if (columnConfig.containsKey("chequedescription")) {
                                ChequeDesc = recarr[(Integer) columnConfig.get("chequedescription")].replaceAll("\"", "").trim();
                            }
                        }

                        /**
                         * *************************** Validating Line Level
                         * Data ****************************
                         */
                        //Product Level data can be empty when number of Terms more than Line Item. So this variable used to find Is all column of line level is empty or not
                        boolean isAllLineLevelDataIsEmpty = accInvoiceModuleService.isAllLineLevelInformationIsEmpty(columnConfig, recarr);

                        Product product = null;
                        double quantity = 0;
                        double unitPrice = 0;
                        double unitPriceIncludingGST = 0;
                        UnitOfMeasure uom = null;
                        int rowdisc = 1;
                        String rowtaxamount = "0";
                        Tax rowtax = null;
                        double discountValue = 0;
                        // Line Level and Invoice Term related data can not be empty at the same time. Also user can not keep line level data empty for first record of invoice
                        if (isAllLineLevelDataIsEmpty && (isNewInvoiceRecord || isAllInvoiceTermDataisEmpty)) {
                            failureMsg += "Empty data found for all Line Item columns.";
                        } else if (!isAllLineLevelDataIsEmpty) {
                            if (columnConfig.containsKey("productID")) {
                                String productID = recarr[(Integer) columnConfig.get("productID")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(productID)) {
                                    product = accSalesOrderServiceobj.getProductByProductID(productID, companyID);
                                    if (product == null) {
                                        failureMsg += "Product ID is not found for " + productID + ". ";
                                    }
                                } else {
                                    failureMsg += "Product ID is not available. ";
                                }
                            } else {
                                failureMsg += "Product ID column is not found. ";
                            }

                            if (columnConfig.containsKey("quantity")) {
                                String quantityStr = recarr[(Integer) columnConfig.get("quantity")].replaceAll("\"", "").trim();
                                if (StringUtil.isNullOrEmpty(quantityStr)) {
                                    failureMsg += "Quantity is not available. ";
                                } else {
                                    try {
                                        quantity = Double.parseDouble(quantityStr);
                                    } catch (Exception ex) {
                                        failureMsg += "Incorrect numeric value for Quantity, Please ensure that value type of Quantity matches with the Quantity. ";
                                    }
                                }
                            } else {
                                failureMsg += "Quantity column is not found. ";
                            }

                            if (columnConfig.containsKey("rate")) {
                                String unitPriceStr = recarr[(Integer) columnConfig.get("rate")].replaceAll("\"", "").trim();
                                if (StringUtil.isNullOrEmpty(unitPriceStr)) {
                                    failureMsg += "Unit Price is not available. ";
                                } else {
                                    try {
                                        unitPrice = Double.parseDouble(unitPriceStr);
                                        if (unitPrice < 0) {
                                            failureMsg += "Unit Price should not be negative.";
                                        }
                                    } catch (Exception ex) {
                                        failureMsg += "Incorrect numeric value for Unit Price, Please ensure that value type of Unit Price matches with the Unit Price. ";
                                    }
                                }
                            } else {
                                failureMsg += "Unit Price column is not found. ";
                            }

                            if (columnConfig.containsKey("rateIncludingGst")) {
                                String unitPriceIncludingGSTStr = recarr[(Integer) columnConfig.get("rateIncludingGst")].replaceAll("\"", "").trim();
                                if (isIncludingGST && StringUtil.isNullOrEmpty(unitPriceIncludingGSTStr)) {
                                    failureMsg += "Unit Price Including GST is not available. ";
                                } else if (!StringUtil.isNullOrEmpty(unitPriceIncludingGSTStr)) {
                                    try {
                                        unitPriceIncludingGST = Double.parseDouble(unitPriceIncludingGSTStr);
                                        if (unitPriceIncludingGST < 0) {
                                            failureMsg += "Unit Price Including GST should not be negative.";
                                        }
                                    } catch (Exception ex) {
                                        failureMsg += "Incorrect numeric value for Unit Price Including GST, Please ensure that value type of Unit Price matches with the Unit Price Including GST. ";
                                    }
                                }
                            } else {
                                if (isIncludingGST) {
                                    failureMsg += "Unit Price Including GST column is not found. ";
                                }
                            }

                            if (columnConfig.containsKey("uomname")) {
                                String productUOMName = recarr[(Integer) columnConfig.get("uomname")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(productUOMName)) {
                                    uom = accSalesOrderServiceobj.getUOMByName(productUOMName, companyID);
                                    if (uom == null) {
                                        if (!masterPreference.equalsIgnoreCase("1")) {
                                            failureMsg += "Product Unit Of Measure is not found for " + productUOMName + ". ";
                                        }
                                    }
                                } else {
                                    if (!masterPreference.equalsIgnoreCase("1")) {
                                        failureMsg += "Product Unit Of Measure is not available. ";
                                    }
                                }
                            }

                            String discountTypeStr = "";
                            if (columnConfig.containsKey("discountType")) {
                                discountTypeStr = recarr[(Integer) columnConfig.get("discountType")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(discountTypeStr)) {
                                    if (discountTypeStr.equalsIgnoreCase("Percentage")) {
                                        rowdisc = 1;
                                    } else if (discountTypeStr.equalsIgnoreCase("Flat")) {
                                        rowdisc = 0;
                                    } else {
                                        failureMsg += "Format you entered is not correct. It should be like \"Percentage\" or \"Flat\". ";
                                    }
                                }
                            }

                            if (columnConfig.containsKey("discount")) {
                                String discountStr = recarr[(Integer) columnConfig.get("discount")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(discountStr)) {
                                    try {
                                        discountValue = Double.parseDouble(discountStr);
                                        if (discountValue < 0) {
                                            failureMsg += "Discount should not be negative.";
                                        }
                                    } catch (Exception ex) {
                                        failureMsg += "Incorrect numeric value for Discount, Please ensure that value type of Discount matches with the Discount. ";
                                    }
                                }
                            }

                            if (columnConfig.containsKey("prtaxid")) {
                                String taxCode = recarr[(Integer) columnConfig.get("prtaxid")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(taxCode)) {
//                                    rowtax = accSalesOrderServiceobj.getGSTByCode(taxCode, companyID);
                                    Map taxMap = new HashMap<>();
                                    taxMap.put(Constants.companyKey, companyID);
                                    taxMap.put(Constants.TAXCODE, taxCode);
                                    ArrayList taxList = importHandlerobj.getTax(taxMap);
                                    if (taxList.get(0) != null) {
                                        rowtax = (Tax) taxList.get(0);
                                        if (rowtax.getTaxtype() == Constants.PURCHASE_TYPE_TAX && isIncludeProductTax) {
                                            failureMsg += "Tax Code is not Sales Type Tax for code " + taxCode;
                                        }
                                    } else if (!StringUtil.isNullOrEmpty((String) taxList.get(2))) {
                                        failureMsg += (String) taxList.get(2) + taxCode;
                                    }
                                } else {
                                    if (isIncludeProductTax) {
                                        failureMsg += "Product Tax is not available. ";
                                    }
                                }
                            } else {
                                if (isIncludeProductTax) {
                                    failureMsg += "Product Tax column is not found. ";
                                }
                            }

                            if (columnConfig.containsKey("taxamount")) {
                                rowtaxamount = recarr[(Integer) columnConfig.get("taxamount")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(rowtaxamount)) {
                                    try {
                                        double rowtaxamountvalue = Double.parseDouble(rowtaxamount);
                                        if (rowtaxamountvalue < 0) {
                                            failureMsg += "Tax Amount should not be negative.";
                                        }
                                    } catch (Exception ex) {
                                        failureMsg += "Incorrect numeric value for Tax Amount. ";
                                    }
                                }
                            }
                        }

                          // Warehouse Location Batch detail validation
                        boolean isLocationForProduct = false;
                        boolean isWarehouseForProduct = false;
                        boolean isBatchForProduct = false;
                        boolean isSerialForProduct = false;
                        boolean isRowForProduct = false;
                        boolean isRackForProduct = false;
                        boolean isBinForProduct = false;
                        String mfgdate = null;
                        String expdate = null;
                        String expstart = null;
                        String expend = null;
                        double batchquantity = 0;
                        String serialName = "";
                        String batchName = "";

                        InventoryWarehouse warehouseObj = null;
                        InventoryLocation locationObj = null;
                        NewProductBatch batchObj = null;
                        NewBatchSerial newBatchSerial = null;
                        StoreMaster rowObj = null;
                        StoreMaster rackObj = null;
                        StoreMaster binObj = null;
                        if (product != null) {
                            if (isGenerateDeliveryOrder && (product.isIswarehouseforproduct() && product.isIslocationforproduct())) {
                                Map<String, Object> requestMap = new HashMap<>();
                                requestMap.put("isSalesTransaction", true); // Need to send true for Sales Transactions
                                requestMap.put("product", product);
                                requestMap.put("columnConfig", columnConfig);
                                requestMap.put("recarr", recarr);
                                requestMap.put("companyID", companyID);
                                requestMap.put("failureMsg", failureMsg);
                                requestMap.put("masterPreference", masterPreference);
                                requestMap.put("df", df);
                                requestMap.put("dateFormat", dateFormat);
                                Map<String, Object> returnMap = accProductModuleServiceobj.validateBatchSerialDetail(requestMap);
                                if (returnMap.containsKey("isWarehouseForProduct") && returnMap.get("isWarehouseForProduct") != null) {
                                    isWarehouseForProduct = (boolean) returnMap.get("isWarehouseForProduct");
                                }
                                if (returnMap.containsKey("isLocationForProduct") && returnMap.get("isLocationForProduct") != null) {
                                    isLocationForProduct = (boolean) returnMap.get("isLocationForProduct");
                                }
                                if (returnMap.containsKey("isBatchForProduct") && returnMap.get("isBatchForProduct") != null) {
                                    isBatchForProduct = (boolean) returnMap.get("isBatchForProduct");
                                }
                                if (returnMap.containsKey("isSerialForProduct") && returnMap.get("isSerialForProduct") != null) {
                                    isSerialForProduct = (boolean) returnMap.get("isSerialForProduct");
                                }
                                if (returnMap.containsKey("isRowForProduct") && returnMap.get("isRowForProduct") != null) {
                                    isRowForProduct = (boolean) returnMap.get("isRowForProduct");
                                }
                                if (returnMap.containsKey("isRackForProduct") && returnMap.get("isRackForProduct") != null) {
                                    isRackForProduct = (boolean) returnMap.get("isRackForProduct");
                                }
                                if (returnMap.containsKey("isBinForProduct") && returnMap.get("isBinForProduct") != null) {
                                    isBinForProduct = (boolean) returnMap.get("isBinForProduct");
                                }

                                if (returnMap.containsKey("warehouseObj") && returnMap.get("warehouseObj") != null) {
                                    warehouseObj = (InventoryWarehouse) returnMap.get("warehouseObj");
                                }
                                if (returnMap.containsKey("locationObj") && returnMap.get("locationObj") != null) {
                                    locationObj = (InventoryLocation) returnMap.get("locationObj");
                                }
                                if (returnMap.containsKey("batchObj") && returnMap.get("batchObj") != null) {
                                    batchObj = (NewProductBatch) returnMap.get("batchObj");
                                }
                                if (returnMap.containsKey("serialObj") && returnMap.get("serialObj") != null) {
                                    newBatchSerial = (NewBatchSerial) returnMap.get("serialObj");
                                }
                                if (returnMap.containsKey("rowObj") && returnMap.get("rowObj") != null) {
                                    rowObj = (StoreMaster) returnMap.get("rowObj");
                                }
                                if (returnMap.containsKey("rackObj") && returnMap.get("rackObj") != null) {
                                    rackObj = (StoreMaster) returnMap.get("rackObj");
                                }
                                if (returnMap.containsKey("binObj") && returnMap.get("binObj") != null) {
                                    binObj = (StoreMaster) returnMap.get("binObj");
                                }
                                if (returnMap.containsKey("mfgdate") && returnMap.get("mfgdate") != null) {
                                    mfgdate = (String) returnMap.get("mfgdate");
                                }
                                if (returnMap.containsKey("expdate") && returnMap.get("expdate") != null) {
                                    expdate = (String) returnMap.get("expdate");
                                }
                                if (returnMap.containsKey("expstart") && returnMap.get("expstart") != null) {
                                    expstart = (String) returnMap.get("expstart");
                                }
                                if (returnMap.containsKey("expend") && returnMap.get("expend") != null) {
                                    expend = (String) returnMap.get("expend");
                                }
                                if (returnMap.containsKey("batchName") && returnMap.get("batchName") != null) {
                                    batchName = (String) returnMap.get("batchName");
                                }
                                if (returnMap.containsKey("serialName") && returnMap.get("serialName") != null) {
                                    serialName = (String) returnMap.get("serialName");
                                }

                                if (returnMap.containsKey("failureMsg") && returnMap.get("failureMsg") != null) {
                                    failureMsg = (String) returnMap.get("failureMsg");
                                }
                                if (returnMap.containsKey("batchquantity") && returnMap.get("batchquantity") != null) {
                                    batchquantity = (double) returnMap.get("batchquantity");
                                    totalBatchQty += batchquantity;
                                }

                                if (prevRow.equalsIgnoreCase("") || !prevRow.equalsIgnoreCase(entryNumber + product.getProductid())) {
                                    prevRow = entryNumber + product.getProductid();
                                    totalBatchQty = batchquantity;
                                }
                                if (totalBatchQty > quantity) {
                                    failureMsg += "Batch Quantity can't be greater than Product Quantity. ";
                                }

                            }
                        }

//                        if (!StringUtil.isNullOrEmpty(failureMsg)) {
//                            failureList.add(entryNumber);
//                        }
                        JSONObject job = new JSONObject();
                        if (StringUtil.isNullOrEmpty(failureMsg) && !isAllLineLevelDataIsEmpty) { // If all Validation is Done then we can check Quantity available

                            job.put("transType", "2");
                            job.put("isEdit", false);
                            job.put("linkflag", false);
                            job.put("isfromdo", true);
                            job.put("fromSubmit", false);
                            job.put("isforReturn", false);
                            job.put(Constants.companyKey, companyID);

                            JSONObject detailObj = new JSONObject();
                            detailObj.put(Constants.productid, product.getID());

                            detailObj.put("isWarehouseForProduct", isWarehouseForProduct);
                            detailObj.put("isLocationForProduct", isLocationForProduct);
                            detailObj.put("isBatchForProduct", isBatchForProduct);
                            detailObj.put("isSerialForProduct", isSerialForProduct);
                            detailObj.put("isRowForProduct", isRowForProduct);
                            detailObj.put("isRackForProduct", isRackForProduct);
                            detailObj.put("isBinForProduct", isBinForProduct);
                            detailObj.put("warehouse", warehouseObj != null ? warehouseObj.getId() : "");
                            detailObj.put("location", locationObj != null ? locationObj.getId() : "");
                            detailObj.put("batchObj", batchObj != null ? batchObj.getId() : "");
//                    detailObj.put("serialName", serialName);
                            detailObj.put("row", rowObj != null ? rowObj.getId() : "");
                            detailObj.put("rack", rackObj != null ? rackObj.getId() : "");
                            detailObj.put("bin", binObj != null ? binObj.getId() : "");
                            detailObj.put("quantity", batchquantity);
                            JSONArray detailSubArr = new JSONArray();
                            detailSubArr.put(detailObj);

                            //job.put("batchdetails", deatilArr);
                            JSONObject detailArrObj = new JSONObject();
                            detailArrObj.put("batchdetails", detailSubArr);
                            JSONArray detailArr = new JSONArray();
                            detailArr.put(detailArrObj);
                            job.put("detail", detailArr);

                            if (!extrareferences.isIsnegativestockforlocwar()) {
                                JSONObject resultObject = accInvoiceModuleService.getBatchRemainingQuantityForImport(job);
                                if (resultObject.has("prodname") && !StringUtil.isNullOrEmpty(resultObject.optString("prodname", ""))) {

                                    failureMsg += "Quantity for Product " + resultObject.optString("prodname", "") + " is not available in particular Location/Warehouse";
                                }
                            }
                        }

                        // creating json
                        if (!prevInvNo.equalsIgnoreCase(entryNumber) || entryNumber.equalsIgnoreCase("")) {//This is the case when invoice number changes. i.e new invoice case

                            if (failureList.contains(prevInvNo)) {
                                failed += singleInvoiceFailureRecoredCount;
                                failedRecords.append(singleInvociceFailedRecords);
                                singleInvociceFailedRecords = new StringBuilder();
                                singleInvoiceFailureRecoredCount = 0;
                            }

                            if ((rowDetailMap.size() > 0 || rows.length() > 0) && !isAlreadyExist && !isRecordFailed) {
                                JSONArray invoiceTermArrayDetails = new JSONArray();
                                double amountAfterRemovingDiscount = totalamount - totaldiscount;
                                if (invoiceTermDetailsArray.length() > 0) {
                                    Map<String, Object> invoiceTermMap = new HashMap();
                                    invoiceTermMap.put("invoiceTermDetailsArray", invoiceTermDetailsArray);
                                    invoiceTermMap.put("amountAfterRemovingDiscount", amountAfterRemovingDiscount);
                                    invoiceTermMap.put(Constants.companyid, companyID);
                                    invoiceTermArrayDetails = accInvoiceModuleService.getInvoiceTermDetailsArray(invoiceTermMap, salesTermMap);
                                    paramJobj.put("invoicetermsmap", invoiceTermArrayDetails.toString());
                                }
                                double taxamount = 0.0;
                                if (!StringUtil.isNullOrEmpty(paramJobj.optString("taxid", null))) {
                                    taxamount = accInvoiceModuleService.getGlobalTaxAmount(paramJobj, companyID, amountAfterRemovingDiscount);
                                }
                                paramJobj.put("taxamount", String.valueOf(taxamount));
                                if (isIncludingGST) {
                                    /*
                                     * When Including GST is true then we need
                                     * to recalculate line level tax,unit price
                                     * and invoiceterm amount accordingly Below
                                     * method does the same.
                                     */
                                    Map<String, Object> invoiceMap = new HashMap();
                                    invoiceMap.put(Constants.companyid, companyID);
                                    invoiceMap.put("amountAfterRemovingDiscount", amountAfterRemovingDiscount);
                                    invoiceMap.put("billdate", billDate);
                                    accInvoiceModuleService.updateLineLevelTaxUnitPriceOnIncludeGST(rows, invoiceTermArrayDetails, invoiceMap);
                                }
                                // on next document saving current documents rows
                                if (rowDetailMap.size() > 0) {
                                    Map<String, Object> resultMap = accInvoiceModuleService.manipulateRowDetails(rowDetailMap, batchSerialMap, batchMap, batchDetailArr, failedRecords, singleInvociceFailedRecords, totalBatchQty, isRecordFailed, rows, isGenerateDeliveryOrder);
                                    if (resultMap.containsKey("isRecordFailed")) {
                                        isRecordFailed = (boolean) resultMap.get("isRecordFailed");
                                        if (isRecordFailed) {
                                            if (!failureList.contains(prevInvNo)) {
                                                failureList.add(prevInvNo);
                                            }
                                            failed += singleInvoiceFailureRecoredCount;
                                            if (singleInvociceFailedRecords.toString().length() > 0) {
                                                failedRecords.append(singleInvociceFailedRecords);
                                            }
                                        }
                                    }
                                    if (resultMap.containsKey("batchMap") && resultMap.get("batchMap") != null) {
                                        batchMap = (Map<String, List<JSONObject>>) resultMap.get("batchMap");
                                    }
                                }
                                if (!isRecordFailed) {
                                    paramJobj.put(Constants.detail, rows.toString());
                                    paramJobj.put(Constants.PAGE_URL, requestJobj.optString(Constants.PAGE_URL));
                                    // for save
                                    accInvoiceModuleService.saveInvoice(paramJobj);
                                    imported = true;
                                }

                            }

                            // reset variables
                            totaldiscount = 0;
                            totalamount = 0;
                            paramJobj = new JSONObject();
                            rows = new JSONArray();
                            invoiceTermDetailsArray = new JSONArray();
                            isRecordFailed = false;
                            isAlreadyExist = false;
                            rowDetailMap = new HashMap<>();
                            batchSerialMap = new HashMap<>();
                            batchDetailArr = new JSONArray();
                            //below variable are get initialized to give correct failure and sucess
                            singleInvociceFailedRecords = new StringBuilder();
                            singleInvoiceFailureRecoredCount = 0;
                            prevInvNo = entryNumber;

                            KwlReturnObject result = accInvoiceDAOobj.getInvoiceCount(entryNumber, companyID);
                            int nocount = result.getRecordTotalCount();
                            if (nocount > 0) {
                                isAlreadyExist = true;
                                failureList.add(entryNumber);
                                failureMsg += "Invoice number'" + entryNumber + "' already exists.";
                            }

                            /*ERP- 38099*/
                            if (isGenerateDeliveryOrder) {
                                KwlReturnObject doResult = accInvoiceDAOobj.getDeliveryOrderCount(deliveryOrderNo, companyID);
                                if (doResult.getRecordTotalCount() > 0) {
                                    isAlreadyExist = true;
                                    failureList.add(entryNumber);
                                    failureMsg += "Delivery Order Number " + deliveryOrderNo + " is already exist. ";
                                }
                            }
                            if (!StringUtil.isNullOrEmpty(failureMsg)) {
                                failureList.add(entryNumber);
                                throw new AccountingException(failureMsg);
                            }

                            // For create custom field array
                            JSONArray customJArr = accSalesOrderServiceobj.createGlobalCustomFieldArrayForImport(requestJobj, jSONArray, recarr, df, Constants.Acc_Invoice_ModuleId);

                            // For getting exchange rate
                            double exchangeRateForTransaction = accSalesOrderServiceobj.getExchangeRateForTransaction(requestJobj, billDate, currencyID);

                            String sequenceFormatID = "NA";
                            boolean autogenerated = false;
                            if (!StringUtil.isNullOrEmpty(entryNumber)) {
                                Map<String, String> sequenceNumberDataMap = new HashMap<String, String>();
                                sequenceNumberDataMap.put("moduleID", String.valueOf(Constants.Acc_Invoice_ModuleId));
                                sequenceNumberDataMap.put("entryNumber", entryNumber);
                                sequenceNumberDataMap.put("companyID", companyID);
                                List list = importHandlerobj.checksEntryNumberForSequenceNumber(sequenceNumberDataMap);
                                if (!list.isEmpty()) {
                                    boolean isvalidEntryNumber = (Boolean) list.get(0);
                                    if (!isvalidEntryNumber) {
                                        String formatID = (String) list.get(2);
                                        int intSeq = (Integer) list.get(3);
                                        paramJobj.put(Constants.SEQNUMBER, intSeq);
                                        paramJobj.put(Constants.SEQFORMAT, formatID);
                                        autogenerated = true;
                                        sequenceFormatID = formatID;
                                    }
                                }
                            }

                            String deliveryOrderSequenceformatID = "NA";
                            if (!StringUtil.isNullOrEmpty(deliveryOrderNo)) {
                                Map<String, String> sequenceNumberDataMap = new HashMap<String, String>();
                                sequenceNumberDataMap.put("moduleID", String.valueOf(Constants.Acc_Delivery_Order_ModuleId));
                                sequenceNumberDataMap.put("entryNumber", deliveryOrderNo);
                                sequenceNumberDataMap.put("companyID", companyID);
                                List list = importHandlerobj.checksEntryNumberForSequenceNumber(sequenceNumberDataMap);
                                if (!list.isEmpty()) {
                                    boolean isvalidEntryNumber = (Boolean) list.get(0);
                                    if (!isvalidEntryNumber) {
                                        String formatID = (String) list.get(2);
                                        int intSeq = (Integer) list.get(3);
                                        paramJobj.put("DOSeqNum", intSeq);
                                        deliveryOrderSequenceformatID = formatID;
                                    }
                                }
                            }

                            // param obj
                            paramJobj.put(Constants.companyKey, companyID);
                            paramJobj.put(Constants.globalCurrencyKey, requestJobj.getString(Constants.globalCurrencyKey));
                            paramJobj.put(Constants.useridKey, requestJobj.getString(Constants.useridKey));
                            paramJobj.put(Constants.lid, requestJobj.getString(Constants.useridKey));
                            paramJobj.put(Constants.userfullname, requestJobj.getString(Constants.userfullname));
                            paramJobj.put(Constants.reqHeader, requestJobj.getString(Constants.reqHeader));
                            paramJobj.put(Constants.remoteIPAddress, requestJobj.getString(Constants.remoteIPAddress));
                            paramJobj.put(Constants.language, requestJobj.getString(Constants.language));
                            paramJobj.put(Constants.timezonedifference, requestJobj.optString(Constants.timezonedifference));
                            paramJobj.put(Constants.currencyKey, currencyID);
                            if (isGenerateDeliveryOrder == true) {
                                paramJobj.put("autogenerateDO", "on");
                                paramJobj.put("isAutoCreateDO", "true");
                                paramJobj.put(Constants.fromLinkComboAutoDO, "Sales Invoice");
                            }
                            paramJobj.put("number", entryNumber);
                            paramJobj.put("numberDo", deliveryOrderNo);
                            paramJobj.put("sequenceformat", sequenceFormatID);
                            paramJobj.put("autogenerated", autogenerated);
                            paramJobj.put(Constants.sequenceformatDo, deliveryOrderSequenceformatID);
                            paramJobj.put("customer", customerID);
                            paramJobj.put("porefno", poRefNumber);
                            paramJobj.put("costcenter", costCenterID);
                            paramJobj.put("defaultAdress", "true");
                            paramJobj.put(Constants.memo, memo);
                            paramJobj.put("posttext", "");
                            paramJobj.put("term", termID);//while saving delivery order term key is used
                            paramJobj.put("termid", termID);
                            paramJobj.put("billdate", sdf.format(billDate));
                            paramJobj.put("perdiscount", "false");
                            paramJobj.put("discount", "0");
                            paramJobj.put("includingGST", String.valueOf(isIncludingGST));
                            paramJobj.put("gstIncluded", String.valueOf(isIncludingGST));
                            if (shipDate != null) {
                                paramJobj.put("shipdate", sdf.format(shipDate));
                            }
                            paramJobj.put("shipvia", shipVia);
                            paramJobj.put(Constants.fob, fob);
                            paramJobj.put("isfavourite", "false");
                            paramJobj.put("salesPerson", salesPersonID);
                            paramJobj.put("externalcurrencyrate", String.valueOf(exchangeRateForTransaction));
                            paramJobj.put("istemplate", "0");
                            paramJobj.put("taxamount", "0");
                            paramJobj.put("invoicetermsmap", "[]");
                            paramJobj.put("termsincludegst", "false");
                            paramJobj.put("fromLinkCombo", "");
                            paramJobj.put("linkFrom", "");
                            paramJobj.put("linkNumber", "");
                            paramJobj.put("templatename", "");
                            paramJobj.put(Constants.customfield, customJArr.toString());
                            paramJobj.put("isEdit", "false");
                            paramJobj.put("copyInv", "false");
                            paramJobj.put("isDraft", "false");
                            paramJobj.put("includeprotax", "false");
                            paramJobj.put("shipLength", "1");
                            paramJobj.put("incash", isCashTransaction);
                            //put Cash Sales related fields
                            if (isCashTransaction) {
                                //create payment details json
                                JSONObject payDetail = new JSONObject();
                                payDetail.put("chequeno", ChequeNo);
                                payDetail.put("description", ChequeDesc);
                                payDetail.put("bankname", bankName);
                                payDetail.put("paymentStatus", cleared ? "Cleared" : "Uncleared");
                                payDetail.put("bankmasteritemid", bankNameMasterItemID);
                                payDetail.put("payDate", chequeDate != null ? sdf.format(chequeDate) : "");
                                payDetail.put("clearanceDate", clearanceDate != null ? sdf.format(clearanceDate) : "");
                                //put payment method details in map
                                paramJobj.put("pmtmethod", payMethodId);
                                paramJobj.put("paydetail", payDetail.toString());
                            }
                            paramJobj.put(Constants.importExportDeclarationNo, exportDeclarationNo);

                            if (isIncludeTotalTax) {
                                paramJobj.put("taxid", taxID);
                            } else {
                                paramJobj.put("taxid", "");
                            }

                            paramJobj.put("deletedLinkedDocumentId", "");
                            paramJobj.put("invoicetype", "");
                            paramJobj.put("seqformat_oldflag", "false");
                            paramJobj.put("includeprotax", String.valueOf(isIncludeProductTax));
                            if (dueDate != null) {
                                paramJobj.put("duedate", sdf.format(dueDate));
                            } else if (isCashTransaction) {//For Cash Sales invoice put due date as bill date
                                paramJobj.put("duedate", sdf.format(billDate));
                            }
                            SimpleDateFormat formatter = (SimpleDateFormat) authHandler.getGlobalDateFormat();
                            paramJobj.put(Constants.Checklocktransactiondate, formatter.format(billDate));

                            Map<String, Object> requestParams = new HashMap<>();
                            requestParams.put(Constants.companyKey, companyID);
                            CompanyPreferencesCMN.checkLockPeriod(accCompanyPreferencesObj, requestParams, billDate, false);
                            CompanyPreferencesCMN.checkActiveDateRange(accCompanyPreferencesObj, requestJobj, billDate);
                        } // end global details

                        if (!StringUtil.isNullOrEmpty(failureMsg) && (StringUtil.isNullOrEmpty(prevInvNo) || prevInvNo.equalsIgnoreCase(entryNumber))) {
                            throw new AccountingException(failureMsg);
                        }

                        if (!isAllInvoiceTermDataisEmpty && termsSales != null) {// When Invoice Term given then put in an array
                            JSONObject termDetail = new JSONObject();
                            termDetail.put("invoiceTerm", termsSales);
                            termDetail.put("isPercentage", (termType == 1) ? true : false);
                            termDetail.put("termValue", termValue);
                            invoiceTermDetailsArray.put(termDetail);
                        }
                        // For Line level details
                        if (!isAllLineLevelDataIsEmpty && !isRecordFailed) {//When number of Invoice Terms is greater than in that case product can be empty. So no need to execute below code 
                            double conversionFactor = 1;
                            // Add Custom fields details of line items
                            JSONArray lineCustomJArr = accSalesOrderServiceobj.createLineLevelCustomFieldArrayForImport(requestJobj, jSONArray, recarr, df, Constants.Acc_Invoice_ModuleId);

                            String key = entryNumber + product.getProductid();
                            JSONObject obj = new JSONObject();
                            if (product.isIswarehouseforproduct() && product.isIslocationforproduct()) {   // For batch Serial Flow
                                String newLineLevelKey = key;
                                if (isWarehouseForProduct && warehouseObj != null) {
                                    key = key + warehouseObj.getName();
                                    obj.put("warehouse", warehouseObj.getId());
                                }
                                if (isLocationForProduct && locationObj != null) {
                                    key = key + locationObj.getName();
                                    obj.put("location", locationObj.getId());
                                }

                                // check if inner line level record combination is duplicate (InvoiceNumber + Product + Warehouse + Location + Batch)
                                if (uniqueLineLevelRecordKeyList.contains(key + batchName)) {// Duplicate found then reset all variables and throw failure message.
                                    rowDetailMap = new HashMap<>();
                                    batchMap = new HashMap<>();
                                    batchSerialMap = new HashMap<>();
                                    batchDetailArr = new JSONArray();
                                    paramJobj = new JSONObject();
                                    rows = new JSONArray();
                                    throw new AccountingException("Duplicate record found for InvoiceNumber, Product, Warehouse, Location and Batch.");
                                } else {
                                    uniqueLineLevelRecordKeyList.add(key + batchName);
                                }

                                if (isRowForProduct && rowObj != null) {
                                    obj.put("row", rowObj.getId());
                                }
                                if (isRackForProduct && rackObj != null) {
                                    obj.put("rack", rackObj.getId());
                                }
                                if (isBinForProduct && binObj != null) {
                                    obj.put("bin", binObj.getId());
                                }
                                obj.put("quantity", batchquantity);
                                obj.put("isBatchForProduct", isBatchForProduct);
                                obj.put("purchasebatchid", batchObj != null ? batchObj.getId() : ""); // passeed Batch Id to Update the Quantity form given batch
                                if (isBatchForProduct) {
                                    obj.put("batch", batchName);
                                    obj.put("mfgdate", mfgdate);
                                    obj.put("expdate", expdate);
                                    obj.put("isSerialForProduct", isSerialForProduct);

                                    String batchKey = isBatchForProduct ? key + batchName : key;
                                    if (batchMap.containsKey(batchKey)) {
                                        List<JSONObject> list = batchMap.get(batchKey);
                                        list.add(obj);
                                        batchMap.put(batchKey, list);
                                    } else {
                                        List<JSONObject> list = new ArrayList<>();
                                        list.add(obj);
                                        batchMap.put(batchKey, list);
                                    }
                                }
                                if (isSerialForProduct) {
                                    obj.put("purchaseserialid", newBatchSerial != null ? newBatchSerial.getId() : "");
                                    obj.put("serialno", serialName);
                                    obj.put("serialnoid", "");
                                    obj.put("expstart", expstart);
                                    obj.put("expend", expend);

                                    String batchKey = isBatchForProduct ? key + batchName : key;
                                    if (batchSerialMap.containsKey(batchKey)) {
                                        List<Object> list = batchSerialMap.get(batchKey);
                                        list.add(serialName);
                                        batchSerialMap.put(batchKey, list);
                                    } else {
                                        List<Object> list = new ArrayList<>();
                                        list.add(serialName);
                                        batchSerialMap.put(batchKey, list);
                                    }
                                }
                                if (!rowDetailMap.containsKey(newLineLevelKey)) {// If Entry No + Productid is changed then it is new line level, so process previous batch stuff of previous line level.
                                    if (rowDetailMap.size() > 0) {
                                        Map<String, Object> resultMap = accInvoiceModuleService.manipulateRowDetails(rowDetailMap, batchSerialMap, batchMap, batchDetailArr, failedRecords, singleInvociceFailedRecords, totalBatchQty, isRecordFailed, rows, isGenerateDeliveryOrder);
                                        if (resultMap.containsKey("isRecordFailed")) {
                                            isRecordFailed = (boolean) resultMap.get("isRecordFailed");
                                            if (isRecordFailed) {
                                                if (!failureList.contains(entryNumber)) {
                                                    failureList.add(entryNumber);
                                                }
                                                paramJobj = new JSONObject();
                                                rows = new JSONArray();
                                            }
                                        }
                                        if (resultMap.containsKey("batchMap") && resultMap.get("batchMap") != null) {
                                            batchMap = (Map<String, List<JSONObject>>) resultMap.get("batchMap");
                                        }
                                        rowDetailMap = new HashMap<>();
                                        batchSerialMap = new HashMap<>();
                                        batchDetailArr = new JSONArray();
                                    }

                                    if (!isRecordFailed) {
                                        JSONObject detailData = new JSONObject();
                                        detailData.put(Constants.productid, (product != null) ? product.getID() : "");
                                        detailData.put("rate", unitPrice);
                                        if (isIncludingGST) {
                                            detailData.put("rateIncludingGst", unitPriceIncludingGST);
                                        }
                                        detailData.put("priceSource", "");
                                        detailData.put("quantity", String.valueOf(quantity));
                                        detailData.put("uomid", (uom != null) ? uom.getID() : "");
                                        detailData.put("baseuomquantity", quantity * conversionFactor);
                                        detailData.put("baseuomrate", String.valueOf(conversionFactor));
                                        detailData.put("dependentType", "");
                                        detailData.put("inouttime", "");
                                        detailData.put("showquantity", "");
                                        detailData.put("desc", (product != null) ? product.getDescription() : "");
                                        detailData.put("invstore", "");
                                        detailData.put("invlocation", "");
                                        detailData.put("rowid", "");
                                        detailData.put("prdiscount", String.valueOf(discountValue));
                                        detailData.put("discountispercent", String.valueOf(rowdisc));
                                        if (!StringUtil.isNullOrEmpty(paramJobj.optString("includeprotax", "")) && paramJobj.getString("includeprotax").equalsIgnoreCase("true") && rowtax != null) {
                                            detailData.put("prtaxid", rowtax.getID());
                                            detailData.put("taxamount", rowtaxamount);
                                        } else {
                                            detailData.put("prtaxid", "");
                                            detailData.put("taxamount", "0");
                                        }
                                        detailData.put("linkto", "");
                                        detailData.put("savedrowid", "");
                                        detailData.put("recTermAmount", "");
                                        detailData.put("OtherTermNonTaxableAmount", "");
                                        detailData.put("productcustomfield", "[{}]");
                                        detailData.put("LineTermdetails", "");
                                        detailData.put("productMRP", "");
                                        detailData.put("valuationType", "");
                                        detailData.put("reortingUOMExcise", "");
                                        detailData.put("reortingUOMSchemaExcise", "");
                                        detailData.put("valuationTypeVAT", "");
                                        detailData.put("reportingUOMVAT", "");
                                        detailData.put("reportingUOMSchemaVAT", "");
                                        detailData.put(Constants.customfield, lineCustomJArr.toString());
                                        if (product != null && (product.getProducttype().getID().equals(Producttype.SERVICE) || product.getProducttype().getID().equals(Producttype.NON_INVENTORY_PART))) {
                                            detailData.put("noQuantityMatchingRequire", true);
                                        }

                                        double rate = unitPrice;
                                        if (paramJobj.optString("includingGST", "").equalsIgnoreCase("true")) {
                                            rate = unitPriceIncludingGST;
                                        }
                                        double rowAmount = rate * quantity;
                                        rowAmount = rowAmount;
                                        totalamount += rowAmount;

                                        double rowdiscount = discountValue;
                                        if (rowdisc == 1) { // for percent disc
                                            rowdiscount = (rowAmount * discountValue) / 100;
                                        }
                                        rowdiscount = rowdiscount;
                                        detailData.put("prdrowiscount", String.valueOf(rowdiscount));

                                        totaldiscount += rowdiscount;
                                        rowDetailMap.put(newLineLevelKey, detailData);
                                        batchDetailArr.put(obj);
                                    }
                                } else {
                                    batchDetailArr.put(obj);
                                }

                            } else {
                                // for already existing row having batch details and next product don't have batchdetails. to first row details then process current 
                                if (rowDetailMap.size() > 0) {
                                    Map<String, Object> resultMap = accInvoiceModuleService.manipulateRowDetails(rowDetailMap, batchSerialMap, batchMap, batchDetailArr, failedRecords, singleInvociceFailedRecords, totalBatchQty, isRecordFailed, rows, isGenerateDeliveryOrder);
                                    if (resultMap.containsKey("isRecordFailed")) {
                                        isRecordFailed = (boolean) resultMap.get("isRecordFailed");
                                        if (isRecordFailed) {
                                            if (!failureList.contains(entryNumber)) {
                                                failureList.add(entryNumber);
                                            }
                                            paramJobj = new JSONObject();
                                            rows = new JSONArray();
                                        }
                                    }
                                    if (resultMap.containsKey("batchMap") && resultMap.get("batchMap") != null) {
                                        batchMap = (Map<String, List<JSONObject>>) resultMap.get("batchMap");
                                    }
                                    rowDetailMap = new HashMap<>();
                                    batchSerialMap = new HashMap<>();
                                    batchDetailArr = new JSONArray();
                                }

                                if (uniqueLineLevelRecordKeyList.contains(key)) {
                                    throw new AccountingException("Duplicate record found for InvoiceNumber and Product.");
                                } else {
                                    uniqueLineLevelRecordKeyList.add(key);
                                }

                                if (!isRecordFailed) {
                                    JSONObject detailData = new JSONObject();
                                    detailData.put(Constants.productid, (product != null) ? product.getID() : "");
                                    detailData.put("rate", unitPrice);
                                    if (isIncludingGST) {
                                        detailData.put("rateIncludingGst", unitPriceIncludingGST);
                                    }
                                    detailData.put("priceSource", "");
                                    detailData.put("quantity", String.valueOf(quantity));
                                    detailData.put("uomid", (uom != null) ? uom.getID() : "");
                                    detailData.put("baseuomquantity", quantity * conversionFactor);
                                    detailData.put("baseuomrate", String.valueOf(conversionFactor));
                                    detailData.put("dependentType", "");
                                    detailData.put("inouttime", "");
                                    detailData.put("showquantity", "");
                                    detailData.put("desc", (product != null) ? product.getDescription() : "");
                                    detailData.put("invstore", "");
                                    detailData.put("invlocation", "");
                                    detailData.put("rowid", "");
                                    detailData.put("prdiscount", String.valueOf(discountValue));
                                    detailData.put("discountispercent", String.valueOf(rowdisc));
                                    if (!StringUtil.isNullOrEmpty(paramJobj.optString("includeprotax", "")) && paramJobj.getString("includeprotax").equalsIgnoreCase("true") && rowtax != null) {
                                        detailData.put("prtaxid", rowtax.getID());
                                        detailData.put("taxamount", rowtaxamount);
                                    } else {
                                        detailData.put("prtaxid", "");
                                        detailData.put("taxamount", "0");
                                    }
                                    detailData.put("linkto", "");
                                    detailData.put("savedrowid", "");
                                    detailData.put("recTermAmount", "");
                                    detailData.put("OtherTermNonTaxableAmount", "");
                                    detailData.put("productcustomfield", "[{}]");
                                    detailData.put("LineTermdetails", "");
                                    detailData.put("productMRP", "");
                                    detailData.put("valuationType", "");
                                    detailData.put("reortingUOMExcise", "");
                                    detailData.put("reortingUOMSchemaExcise", "");
                                    detailData.put("valuationTypeVAT", "");
                                    detailData.put("reportingUOMVAT", "");
                                    detailData.put("reportingUOMSchemaVAT", "");
                                    detailData.put(Constants.customfield, lineCustomJArr.toString());

                                    double rate = unitPrice;
                                    if (paramJobj.optString("includingGST", "").equalsIgnoreCase("true")) {
                                        rate = unitPriceIncludingGST;
                                    }
                                    double rowAmount = rate * quantity;
                                    rowAmount = rowAmount;
                                    totalamount += rowAmount;

                                    double rowdiscount = discountValue;
                                    if (rowdisc == 1) { // for percent disc
                                        rowdiscount = (rowAmount * discountValue) / 100;
                                    }
                                    rowdiscount = rowdiscount;
                                    detailData.put("prdrowiscount", String.valueOf(rowdiscount));

                                    totaldiscount += rowdiscount;

                                    rows.put(detailData);
                                }
                            }
                        }
                    } catch (Exception ex) {
//                        failed++;
                        isRecordFailed = true;
                        isfailurerecord = true;
                        String errorMsg = "";
                        if (ex.getMessage() != null) {
                            errorMsg = ex.getMessage();
                        } else if (ex.getCause() != null) {
                            errorMsg = ex.getCause().getMessage();
                        }
                        if (!failureList.contains(entryNumber)) {
//                                failed += singleInvoiceFailureRecoredCount;
                            if (singleInvoiceFailureRecoredCount > 0) {
                                failedRecords.append(singleInvociceFailedRecords);
                                singleInvociceFailedRecords = new StringBuilder();
                            }
                            prevInvNo = entryNumber;
                            failureList.add(entryNumber);
                        }
                        singleInvoiceFailureRecoredCount++;
                        singleInvociceFailedRecords.append("\n").append(accSalesOrderServiceobj.createCSVrecord(recarr)).append("\"").append(errorMsg.replaceAll("\"", "")).append("\"");
                    }
                    if (!isfailurerecord) {
                        singleInvoiceFailureRecoredCount++;
                        singleInvociceFailedRecords.append("\n").append(accSalesOrderServiceobj.createCSVrecord(recarr)).append("\"").append(" ").append("\"");
                    }
                    total++;
                }
                cnt++;
            }
            // save last record
            if (!isAlreadyExist && !isRecordFailed) {
                JSONArray invoiceTermArrayDetails = new JSONArray();
                double amountAfterRemovingDiscount = totalamount - totaldiscount;
                if (invoiceTermDetailsArray.length() > 0) {
                    Map<String, Object> invoiceTermDetailMap = new HashMap();
                    invoiceTermDetailMap.put("invoiceTermDetailsArray", invoiceTermDetailsArray);
                    invoiceTermDetailMap.put("amountAfterRemovingDiscount", amountAfterRemovingDiscount);
                    invoiceTermArrayDetails = accInvoiceModuleService.getInvoiceTermDetailsArray(invoiceTermDetailMap, salesTermMap);
                    paramJobj.put("invoicetermsmap", invoiceTermArrayDetails.toString());
                }
                double taxamount = 0.0;
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("taxid", null))) {
                    taxamount = accInvoiceModuleService.getGlobalTaxAmount(paramJobj, companyID, amountAfterRemovingDiscount);
                }
                if (paramJobj.optString("includingGST", "").equalsIgnoreCase("true")) {
                    /*
                     * When Including GST is true then we need to recalculate
                     * line level tax,unit price and invoiceterm amount
                     * accordingly Below method does the same.
                     */
                    Map<String, Object> invoiceMap = new HashMap();
                    invoiceMap.put(Constants.companyid, companyID);
                    invoiceMap.put("amountAfterRemovingDiscount", amountAfterRemovingDiscount);
                    invoiceMap.put("billdate", sdf.parse(paramJobj.getString("billdate")));
                    accInvoiceModuleService.updateLineLevelTaxUnitPriceOnIncludeGST(rows, invoiceTermArrayDetails, invoiceMap);
                }
                paramJobj.put("taxamount", String.valueOf(taxamount));
                Map<String, Object> resultMap = accInvoiceModuleService.manipulateRowDetails(rowDetailMap, batchSerialMap, batchMap, batchDetailArr, failedRecords, singleInvociceFailedRecords, totalBatchQty, isRecordFailed, rows, isGenerateDeliveryOrder);
                if (resultMap.containsKey("isRecordFailed")) {
                    isRecordFailed = (boolean) resultMap.get("isRecordFailed");
                    if (!isRecordFailed) {
                        rowDetailMap = new HashMap<>();
                        batchSerialMap = new HashMap<>();
                        batchDetailArr = new JSONArray();
                    }
                }
                if (resultMap.containsKey("batchMap") && resultMap.get("batchMap") != null) {
                    batchMap = (Map<String, List<JSONObject>>) resultMap.get("batchMap");
                }

                if (!isRecordFailed) {
                    paramJobj.put(Constants.detail, rows.toString());
                    // for save
                    paramJobj.put(Constants.PAGE_URL, requestJobj.optString(Constants.PAGE_URL));
                    accInvoiceModuleService.saveInvoice(paramJobj);
                    imported = true;
                }
            }
            if (isRecordFailed) {// only if last invoice is failed
                failed += singleInvoiceFailureRecoredCount; // last interation failure record
                if (singleInvoiceFailureRecoredCount > 0) {
                    failedRecords.append(singleInvociceFailedRecords);
                }
            }
            if (failed > 0) {
                importHandlerobj.createFailureFiles(fileName, failedRecords, ".csv");
            }

            int success = (imported == true) ? (total - failed) : 0;
            failed = (imported == true) ? failed : total;
            if (total == 0) {
                msg = "Empty file.";
            } else if (success == 0) {
                msg = "Failed to import all the records.";
            } else if (success == total) {
                msg = "All records are imported successfully.";
            } else {
                msg = "Imported " + success + " record" + (success > 1 ? "s" : "") + " successfully";
                msg += (failed == 0 ? "." : " and failed to import " + failed + " record" + (failed > 1 ? "s" : "") + ".");
            }
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();

            Logger.getLogger(AccInvoiceModuleServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Importing Records.");
        } finally {
            fileInputStream.close();
            csvReader.close();

            // For saving import log
            accSalesOrderServiceobj.saveImportLog(requestJobj, msg, total, failed, Constants.Acc_Invoice_ModuleId);

            try {
                returnObj.put(Constants.RES_success, issuccess);
                returnObj.put(Constants.RES_msg, msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", ImportLog.getActualFileName(fileName));
                returnObj.put("Module", Constants.Acc_Invoice_ModuleId);
            } catch (JSONException ex) {
                Logger.getLogger(AccInvoiceModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return returnObj;
    }

    public JSONObject importPurchaseInvoiceJSON(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        try {
            jobj = importPurchaseInvoiceRecordsForCSV(paramJobj);
        } catch (Exception ex) {
            try {
                jobj.put("success", false);
                jobj.put("msg", "" + ex.getMessage());
            } catch (JSONException jex) {
                Logger.getLogger(accGoodsReceiptModuleServiceImpl.class.getName()).log(Level.SEVERE, null, jex);
            }
        }
        return jobj;
    }

    public JSONObject importPurchaseInvoiceRecordsForCSV(JSONObject requestJobj) throws AccountingException, IOException, JSONException {
        JSONObject returnObj = new JSONObject();
        String msg = "";
        int total = 0, failed = 0;
        String fileName = requestJobj.getString("filename");
        String companyID = requestJobj.getString(Constants.companyKey);
        String masterPreference = requestJobj.getString("masterPreference");
        Locale locale = (Locale) requestJobj.get("locale");
        boolean issuccess = true;
        boolean isAlreadyExist = false;
        boolean isRecordFailed = false;
        FileInputStream fileInputStream = null;
        CsvReader csvReader = null;
        JSONObject paramJobj = new JSONObject();
        Map<String, Object> rowDetailMap = new HashMap<>();
        Map<String, List<JSONObject>> batchMap = new HashMap<>();
        Map<String, List<Object>> batchSerialMap = new HashMap<>();
        JSONArray batchDetailArr = new JSONArray();
        JSONArray rows = new JSONArray();
        String vendorInvoiceNumber = "";
        String prevInvNo = "";
        String prevRow = "";
        String prevBatch = "";
        double totaldiscount = 0, totalamount = 0;
        double totalBatchQty = 0;

        try {
            String dateFormat = null, dateFormatId = requestJobj.getString("dateFormat");
            if (!StringUtil.isNullOrEmpty(dateFormatId)) {
                KwlReturnObject kdfObj = accountingHandlerDAOobj.getObject(KWLDateFormat.class.getName(), dateFormatId);
                KWLDateFormat kdf = (KWLDateFormat) kdfObj.getEntityList().get(0);

                dateFormat = kdf != null ? kdf.getJavaForm() : null;
            }
            DateFormat df = new SimpleDateFormat(dateFormat);
            df.setLenient(false);
            requestJobj.put(Constants.importdf, df); //sending this format for Processing batch details

            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");
            DateFormat dateOnlydf = null;
            if (requestJobj.has(Constants.df) && requestJobj.get(Constants.df) != null) {
                dateOnlydf = (DateFormat) requestJobj.get(Constants.df);
            }

            KwlReturnObject extraPref = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyID);
            ExtraCompanyPreferences extrareferences = (ExtraCompanyPreferences) extraPref.getEntityList().get(0);
            Boolean isCurrencyCode = extrareferences.isCurrencyCode();

            String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
            File filePath = new File(destinationDirectory + File.separator + fileName);
            fileInputStream = new FileInputStream(filePath);
            String delimiterType = requestJobj.getString("delimiterType");
            csvReader = new CsvReader(new InputStreamReader(fileInputStream), delimiterType);

            JSONObject resjson = new JSONObject(requestJobj.getString("resjson"));
            JSONArray jSONArray = resjson.getJSONArray("root");
            HashMap<String, Integer> columnConfig = new HashMap<>();
            for (int i = 0; i < jSONArray.length(); i++) { // Map the column config with csv column 
                JSONObject jSONObject = jSONArray.getJSONObject(i);
                columnConfig.put(jSONObject.getString("dataindex"), jSONObject.getInt("csvindex"));
            }

            int cnt = 0;
            StringBuilder failedRecords = new StringBuilder();
            StringBuilder singleInvociceFailedRecords = new StringBuilder();// Invoive with one row of failure record then all rows will be included in failure file
            int singleInvoiceFailureRecoredCount = 0;//  count of total invoice rows in import file
            Set<String> failureList = new HashSet<>(); // set of invoice having failyure record's
            HashMap currencyMap = accSalesOrderServiceobj.getCurrencyMap(isCurrencyCode);
            
            String logId = null;
            logId = addPendingImportLog(requestJobj);
            requestJobj.put("logId",logId);
            
            while (csvReader.readRecord()) {
                String failureMsg = "";
                boolean isfailurerecord = false; // used to keep track Invoice Row  failure/correct
                String[] recarr = csvReader.getValues();

                if (cnt == 0) {
                    failedRecords.append(accSalesOrderServiceobj.createCSVrecord(recarr)).append("\" \"");
                } else if (cnt == 1) {
                    failedRecords.append("\n").append(accSalesOrderServiceobj.createCSVrecord(recarr)).append("\"Error Message\"");
                } else {
                    try {
                        String currencyID = requestJobj.getString(Constants.globalCurrencyKey);

                        vendorInvoiceNumber = "";
                        if (columnConfig.containsKey("vendorInvoiceNumber")) {
                            vendorInvoiceNumber = recarr[(Integer) columnConfig.get("vendorInvoiceNumber")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(vendorInvoiceNumber)) {
                                failureMsg += "Vendor Invoice Number is not available. ";
                            }
                        } else {
                            failureMsg += "Vendor Invoice Number column is not found. ";
                        }

                        Date vendorInvoiceDate = null;
                        if (columnConfig.containsKey("vendorInvoiceDate")) {
                            String vendorInvoiceDateStr = recarr[(Integer) columnConfig.get("vendorInvoiceDate")].replaceAll("\"", "").trim();

                            if (StringUtil.isNullOrEmpty(vendorInvoiceDateStr)) {
                                failureMsg += "Vendor Invoice Date is not available. ";
                            } else {
                                try {
                                    vendorInvoiceDate = df.parse(vendorInvoiceDateStr);
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect date format for Vendor Invoice Date, Please specify values in " + dateFormat + " format. ";
                                }
                            }
                        } else {
                            failureMsg += "Vendor Invoice Date column is not found. ";
                        }

                        String supplierInvoiceNo = "";
                        if (columnConfig.containsKey(Constants.SUPPLIERINVOICENO)) {
                            supplierInvoiceNo = recarr[(Integer) columnConfig.get(Constants.SUPPLIERINVOICENO)].replaceAll("\"", "").trim();
                        }
                        /**
                         * get and put Cash Purchase related columns details and
                         * validation
                         */
                        String payMethodId = "";
                        String ChequeNo = "";
                        String bankName = "";
                        String bankNameMasterItemID = "";
                        Date chequeDate = null;
                        boolean cleared = false;
                        Date clearanceDate = null;
                        String ChequeDesc = "";
                        if (requestJobj.optBoolean("incash", false)) {
                            //get Payment Method
                            PaymentMethod payMethod = null;
                            if (columnConfig.containsKey("paymentmethodid")) {
                                String paymentMethodStr = recarr[(Integer) columnConfig.get("paymentmethodid")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(paymentMethodStr)) {
                                    KwlReturnObject retObj = accMasterItemsDAOobj.getPaymentMethodIdFromName(paymentMethodStr, companyID);
                                    if (retObj != null && !retObj.getEntityList().isEmpty()) {
                                        payMethod = (PaymentMethod) retObj.getEntityList().get(0);
                                        payMethodId = payMethod.getID();
                                    }
                                    if (payMethod == null) {
                                        failureMsg += "Payment Method is not found for " + paymentMethodStr + ". ";
                                    }
                                } else {
                                    failureMsg += "Payment Method is not available. ";
                                }
                            } else {
                                failureMsg += "Payment Method column is not found. ";
                            }
                            //get Cheque Number
                            if (columnConfig.containsKey("chequeno")) {
                                ChequeNo = recarr[(Integer) columnConfig.get("chequeno")].replaceAll("\"", "").trim();
                                failureMsg += importHandlerobj.isChequeNumberValidForImport(ChequeNo,false);
                            }
                            //get Bank Name
                            if (columnConfig.containsKey("bankname")) {
                                bankName = recarr[(Integer) columnConfig.get("bankname")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(bankName)) {
                                    KwlReturnObject returnObject = importDAOobj.getBankNameMasterItemName(companyID, bankName);
                                    if (returnObject.getEntityList().isEmpty()) {
//                                        failureMsg += "Incorrect Bank Name type value for Bank Name. Please add new Bank Name as \"" + bankName + "\" with other details.";
                                    } else {
                                        MasterItem masterItem = (MasterItem) returnObject.getEntityList().get(0);
                                        bankNameMasterItemID = masterItem.getID();
                                    }
                                } else {
                                    if (payMethod != null && payMethod.getDetailType() == Constants.bank_detail_type) {
                                        failureMsg += "Empty data found in Bank Name, cannot set empty data for Bank Name if Payment Method is selected as Bank.";
                                    }
                                }
                            } else {
                                if (payMethod != null && payMethod.getDetailType() == Constants.bank_detail_type) {
                                    failureMsg += "Bank Name column is not found. ";
                                }
                            }
                            //get Cheque Date
                            if (columnConfig.containsKey("chequedate")) {
                                String chequeDateStr = recarr[(Integer) columnConfig.get("chequedate")].replaceAll("\"", "").trim();
                                if (StringUtil.isNullOrEmpty(chequeDateStr)) {
                                    if (payMethod != null && payMethod.getDetailType() != PaymentMethod.TYPE_CASH) {
                                        failureMsg += "Cheque Date is not available. ";
                                    }
                                } else {
                                    try {
                                        chequeDate = df.parse(chequeDateStr);
                                    } catch (Exception ex) {
                                        failureMsg += "Incorrect date format for Cheque Date, Please specify values in " + dateFormat + " format. ";
                                    }
                                }
                            } else {
                                if (payMethod != null && payMethod.getDetailType() != PaymentMethod.TYPE_CASH) {
                                    failureMsg += "Cheque Date column is not found. ";
                                }
                            }
                            //get Payment Status
                            if (columnConfig.containsKey("paymentStatus")) {
                                String paymentStatusStr = recarr[(Integer) columnConfig.get("paymentStatus")].replaceAll("\"", "").trim();
                                if (StringUtil.isNullOrEmpty(paymentStatusStr)) {
                                    if (payMethod != null && payMethod.getDetailType() == Constants.bank_detail_type) {
                                        failureMsg += "Payment Status is not available. ";
                                    }
                                } else {
                                    if (paymentStatusStr.equalsIgnoreCase("Cleared")) {
                                        cleared = true;
                                    } else if (paymentStatusStr.equalsIgnoreCase("Uncleared")) {
                                        cleared = false;
                                    } else {
                                        failureMsg += "Incorrect Payment Status type value for Payment Status. It should be either Cleared or Uncleared. ";
                                    }
                                }
                            } else {
                                if (payMethod != null && payMethod.getDetailType() == Constants.bank_detail_type) {
                                    failureMsg += "Payment Status column is not found. ";
                                }
                            }
                            //get Clearance Date
                            if (columnConfig.containsKey("clearanceDate")) {
                                if (payMethod != null && payMethod.getDetailType() != PaymentMethod.TYPE_CASH && cleared) { // when payment type is other than cash and payment sttus is clear then only need of clerance date. So its validation
                                    String clearenceDateStr = recarr[(Integer) columnConfig.get("clearanceDate")].replaceAll("\"", "").trim();
                                    if (!StringUtil.isNullOrEmpty(clearenceDateStr)) {
                                        try {
                                            clearanceDate = df.parse(clearenceDateStr);
                                            if (chequeDate.compareTo(clearanceDate) > 0) {
                                                failureMsg += "Clearence date should be greter than Cheque date.";
                                            }
                                        } catch (ParseException ex) {
                                            failureMsg += "Incorrect date format for Clearence Date, Please specify values in " + dateFormat + " format.";
                                        }
                                    } else {
                                        failureMsg += "You have entered the Payment Status as Cleared. So you cannot set empty data for Clearence Date.";
                                    }
                                }
                            } else {
                                if (payMethod != null && payMethod.getDetailType() != PaymentMethod.TYPE_CASH && cleared) {
                                    failureMsg += "Clearance Date column is not found. ";
                                }
                            }
                            //get Cheque Description
                            if (columnConfig.containsKey("chequedescription")) {
                                ChequeDesc = recarr[(Integer) columnConfig.get("chequedescription")].replaceAll("\"", "").trim();
                            }
                        }

                        String importDeclarationNo = "";
                        if (columnConfig.containsKey(Constants.importExportDeclarationNo)) {
                            importDeclarationNo = recarr[columnConfig.get(Constants.importExportDeclarationNo)].replaceAll("\"", "").trim();
                        }

                        String costCenterID = "";
                        if (columnConfig.containsKey("costcenter")) {
                            String costCenterName = recarr[(Integer) columnConfig.get("costcenter")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(costCenterName)) {
                                costCenterID = accPurchaseOrderModuleServiceObj.getCostCenterIDByName(costCenterName, companyID);
                                if (StringUtil.isNullOrEmpty(costCenterID)) {
                                    failureMsg += "Cost Center is not found for name " + costCenterName + ". ";
                                }
                            }
                        }

                        Date dueDate = null;
                        if (columnConfig.containsKey("dueDate")) {
                            String dueDateStr = recarr[(Integer) columnConfig.get("dueDate")].replaceAll("\"", "").trim();

                            if (StringUtil.isNullOrEmpty(dueDateStr)) {
                                failureMsg += "Due Date is not available. ";
                            } else {
                                try {
                                    dueDate = df.parse(dueDateStr);
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect date format for Due Date, Please specify values in " + dateFormat + " format. ";
                                }
                            }
                        } else {
                            failureMsg += "Due Date column is not found. ";
                        }

                        Date shipDate = null;
                        if (columnConfig.containsKey("shipdate")) {
                            String shipDateStr = recarr[(Integer) columnConfig.get("shipdate")].replaceAll("\"", "").trim();

                            if (!StringUtil.isNullOrEmpty(shipDateStr)) {
                                try {
                                    shipDate = df.parse(shipDateStr);
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect date format for Ship Date, Please specify values in " + dateFormat + " format. ";
                                }
                            }
                        }

                        String vendorID = "";
                        /*
                         * 1. Vendor Code
                         */
                        if (columnConfig.containsKey("vendorCode")) {
                            String vendorCode = recarr[(Integer) columnConfig.get("vendorCode")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(vendorCode)) {
                                Vendor vendor = accPurchaseOrderModuleServiceObj.getVendorByCode(vendorCode, companyID);
                                if (vendor != null) {
                                    vendorID = vendor.getID();
                                } else {
                                    failureMsg += messageSource.getMessage("acc.field.VendorCodeisnotavailable", null, Locale.forLanguageTag(requestJobj.getString(Constants.language))) + vendorCode + ". ";
                                }
                            }
                        }

                        /*
                         * 2. Vendor Name if vendorID is empty it means
                         * Vendor is not found for given code. so need to
                         * search data on name
                         */
                        if (StringUtil.isNullOrEmpty(vendorID)) {
                            if (columnConfig.containsKey("VendorName")) {
                                String vendorName = recarr[(Integer) columnConfig.get("VendorName")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(vendorName)) {
                                    Vendor vendor = accPurchaseOrderModuleServiceObj.getVendorByName(vendorName, companyID);
                                    if (vendor != null) {
                                        vendorID = vendor.getID();
                                    } else {
                                        failureMsg += messageSource.getMessage("acc.field.VendorisnotfoundforVendorCodeName", null, Locale.forLanguageTag(requestJobj.getString(Constants.language))) + ". ";
                                    }
                                } else {
                                    failureMsg += messageSource.getMessage("acc.field.VendorisnotfoundforVendorCodeName", null, Locale.forLanguageTag(requestJobj.getString(Constants.language))) + ".";
                                }
                            } else {
                                failureMsg += messageSource.getMessage("acc.field.VendorisnotfoundforVendorCodeName", null, Locale.forLanguageTag(requestJobj.getString(Constants.language))) + ".";
                            }
                        }

                        String termID = "";
                        if (columnConfig.containsKey("term")) {
                            String termName = recarr[(Integer) columnConfig.get("term")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(termName)) {
                                termID = accSalesOrderServiceobj.getTermIDByName(termName, companyID);
                                if (StringUtil.isNullOrEmpty(termID)) {
                                    failureMsg += "Debit Term is not found for name " + termName + ". ";
                                }
                            } else {
                                failureMsg += "Debit Term is not available. ";
                            }
                        } else {
                            failureMsg += "Debit Term column is not found. ";
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

                        String agentID = "";
                        if (columnConfig.containsKey("agent")) {
                            String agentName = recarr[(Integer) columnConfig.get("agent")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(agentName)) {
                                agentID = accPurchaseOrderModuleServiceObj.getAgentIDByName(agentName, companyID);
                                if (StringUtil.isNullOrEmpty(agentID)) {
                                    failureMsg += "Agent is not found for name " + agentName + ". ";
                                }
                            }
                        }

                        if (isCurrencyCode ? columnConfig.containsKey("currencyCode") : columnConfig.containsKey("currencyName")) {
                            String currencyStr = isCurrencyCode ? recarr[(Integer) columnConfig.get("currencyCode")].replaceAll("\"", "").trim() : recarr[(Integer) columnConfig.get("currencyName")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(currencyStr)) {
                                currencyID = accSalesOrderServiceobj.getCurrencyId(currencyStr, currencyMap);

                                if (StringUtil.isNullOrEmpty(currencyID)) {
                                    failureMsg += messageSource.getMessage("acc.field.ImportProductExceptionFormat", null, Locale.forLanguageTag(requestJobj.getString(Constants.language))) + ". ";
                                }
                            } else {
                                if (!masterPreference.equalsIgnoreCase("1")) {
                                    failureMsg += "Currency is not available. ";
                                }
                            }
                        }

                        String consignmentID = "";
                        if (columnConfig.containsKey("consignmentNumber")) {
                            String consignmentNumber = recarr[(Integer) columnConfig.get("consignmentNumber")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(consignmentNumber)) {
                                KwlReturnObject consignmentResult = accGoodsReceiptobj.getReceiptFromNo(consignmentNumber, companyID);
                                if (consignmentResult.getEntityList() != null && !consignmentResult.getEntityList().isEmpty()) {
                                    GoodsReceipt goodsReceipt = (GoodsReceipt) consignmentResult.getEntityList().get(0);
                                    consignmentID = goodsReceipt.getID();
                                } else {
                                    failureMsg += "Consignment Number is not found for name " + consignmentNumber + ". ";
                                }
                            }
                        }

                        boolean isGenerateGoodsReceipt = false;
                        if (columnConfig.containsKey("generateGoodsReceipt")) {
                            String generateGoodsReceipt = recarr[(Integer) columnConfig.get("generateGoodsReceipt")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(generateGoodsReceipt)) {
                                if (generateGoodsReceipt.equalsIgnoreCase("TRUE")) {
                                    isGenerateGoodsReceipt = true;
                                } else if (generateGoodsReceipt.equalsIgnoreCase("FALSE")) {
                                    isGenerateGoodsReceipt = false;
                                } else {
                                    throw new AccountingException("Format you entered for Generate Goods Receipt is not correct. It should be like \"TRUE\" or \"FALSE\"");
                                }
                            } else {
                                isGenerateGoodsReceipt = false;
                            }
                        }

                        String goodsReceiptNo = "";
                        if (columnConfig.containsKey("goodsReceiptNo") && isGenerateGoodsReceipt) {
                            goodsReceiptNo = recarr[(Integer) columnConfig.get("goodsReceiptNo")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(goodsReceiptNo)) {
                                failureMsg += "Goods Receipt No is not available. ";
                            } else {
                                KwlReturnObject groResult = accGoodsReceiptobj.getGoodsReceiptOrderCount(goodsReceiptNo, companyID);
                                if (groResult.getRecordTotalCount() > 0) {
                                    failureMsg += "Goods Receipt No " + goodsReceiptNo + " is already exist. ";
                                }
                            }
                        }

                        boolean isIncludingGST = false;
                        if (columnConfig.containsKey("gstIncluded")) {
                            String isIncludingGSTStr = recarr[(Integer) columnConfig.get("gstIncluded")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(isIncludingGSTStr)) {
                                if (isIncludingGSTStr.equalsIgnoreCase("TRUE")) {
                                    isIncludingGST = true;
                                } else if (isIncludingGSTStr.equalsIgnoreCase("FALSE")) {
                                    isIncludingGST = false;
                                } else {
                                    failureMsg += "Format you entered for Include GST is not correct. It should be like \"TRUE\" or \"FALSE\". ";
                                }
                            }
                        }

                        boolean isIncludeProductTax = false;
                        if (columnConfig.containsKey("includeprotax")) {
                            String isIncludeProductTaxStr = recarr[(Integer) columnConfig.get("includeprotax")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(isIncludeProductTaxStr)) {
                                if (isIncludeProductTaxStr.equalsIgnoreCase("Yes")) {
                                    isIncludeProductTax = true;
                                } else if (isIncludeProductTaxStr.equalsIgnoreCase("No")) {
                                    isIncludeProductTax = false;
                                } else {
                                    failureMsg += "Format you entered for Include Product Tax is not correct. It should be like \"Yes\" or \"No\". ";
                                }
                            }
                        }

                        if (isIncludingGST && !isIncludeProductTax) {
                            failureMsg += "If value Including GST is \"TRUE\" then value of Include Product Tax should be \"Yes\". ";
                        }

                        boolean isIncludeTotalTax = false;
                        if (columnConfig.containsKey("taxincluded")) {
                            String isIncludeTotalTaxStr = recarr[(Integer) columnConfig.get("taxincluded")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(isIncludeTotalTaxStr)) {
                                if (isIncludeTotalTaxStr.equalsIgnoreCase("Yes")) {
                                    isIncludeTotalTax = true;
                                } else if (isIncludeTotalTaxStr.equalsIgnoreCase("No")) {
                                    isIncludeTotalTax = false;
                                } else {
                                    failureMsg += "Format you entered for Include Total Tax is not correct. It should be like \"Yes\" or \"No\". ";
                                }
                            }
                        }

                        if (isIncludeProductTax && isIncludeTotalTax) {
                            failureMsg += "If value of Include Product Tax is \"Yes\" then value of Include Total Tax should be \"No\".";
                        }

                        String taxID = "";
                        if (columnConfig.containsKey("taxid")) {
                            String taxCode = recarr[(Integer) columnConfig.get("taxid")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(taxCode)) {
//                                Tax tax = accGoodsReceiptModuleServiceobj.getGSTByCode(taxCode, companyID);
                                Map taxMap = new HashMap<>();
                                taxMap.put(Constants.companyKey, companyID);
                                taxMap.put(Constants.TAXCODE, taxCode);
                                ArrayList taxList = importHandlerobj.getTax(taxMap);
                                if (taxList.get(0) != null) {
                                    Tax tax = (Tax) taxList.get(0);
                                    taxID = tax.getID();
                                    if (tax.getTaxtype() == Constants.SALES_TYPE_TAX && isIncludeTotalTax) {
                                        failureMsg += "Tax Code is not Purchase Type TAX for code " + taxCode;
                                    }
                                } else if (!StringUtil.isNullOrEmpty((String) taxList.get(2))) {
                                    failureMsg += (String) taxList.get(2) + taxCode;
                                }
                            } else {
                                if (!isIncludingGST && !isIncludeProductTax && isIncludeTotalTax) {
                                    failureMsg += "Tax Code is not available. ";
                                }
                            }
                        } else {
                            if (!isIncludingGST && !isIncludeProductTax && isIncludeTotalTax) {
                                failureMsg += "Tax Code column is not found. ";
                            }
                        }

                        Product product = null;
                        if (columnConfig.containsKey("productID")) {
                            String productID = recarr[(Integer) columnConfig.get("productID")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(productID)) {
                                product = accSalesOrderServiceobj.getProductByProductID(productID, companyID);
                                if (product == null) {
                                    if (!masterPreference.equalsIgnoreCase("1")) {
                                        failureMsg += "Product is not found for " + productID + ". ";
                                    }
                                }
                            } else {
                                failureMsg += "Product is not available. ";
                            }
                        } else {
                            failureMsg += "Product column is not found. ";
                        }

                        double quantity = 0;
                        if (columnConfig.containsKey("quantity")) {
                            String quantityStr = recarr[(Integer) columnConfig.get("quantity")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(quantityStr)) {
                                failureMsg += "Quantity is not available. ";
                            } else {
                                try {
                                    quantity = authHandler.roundQuantity(Double.parseDouble(quantityStr), companyID);
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect numeric value for Quantity, Please ensure that value type of Quantity matches with the Quantity. ";
                                }
                            }
                        } else {
                            failureMsg += "Quantity column is not found. ";
                        }

                        String unitPriceIncludingGST = "0";
                        if (columnConfig.containsKey("rateIncludingGst")) {
                            unitPriceIncludingGST = recarr[(Integer) columnConfig.get("rateIncludingGst")].replaceAll("\"", "").trim();
                            if (isIncludingGST && StringUtil.isNullOrEmpty(unitPriceIncludingGST)) {
                                failureMsg += "Unit Price Including GST is not available. ";
                            }
                        } else {
                            if (isIncludingGST) {
                                failureMsg += "Unit Price Including GST column is not found. ";
                            }
                        }

                        double unitPrice = 0;
                        if (columnConfig.containsKey("rate")) {
                            String unitPriceStr = recarr[(Integer) columnConfig.get("rate")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(unitPriceStr)) {
                                failureMsg += "Unit Price is not available. ";
                            } else {
                                try {
                                    unitPrice = authHandler.roundQuantity(Double.parseDouble(unitPriceStr), companyID);
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect numeric value for Unit Price, Please ensure that value type of Unit Price matches with the Unit Price. ";
                                }
                            }
                        } else {
                            failureMsg += "Unit Price column is not found. ";
                        }

                        UnitOfMeasure uom = null;
                        if (columnConfig.containsKey("uomname")) {
                            String productUOMName = recarr[(Integer) columnConfig.get("uomname")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(productUOMName)) {
                                uom = accSalesOrderServiceobj.getUOMByName(productUOMName, companyID);
                                if (uom == null) {
                                    if (!masterPreference.equalsIgnoreCase("1")) {
                                        failureMsg += "Product Unit Of Measure is not found for " + productUOMName + ". ";
                                    }
                                }
                            } else {
                                if (!masterPreference.equalsIgnoreCase("1")) {
                                    failureMsg += "Product Unit Of Measure is not available. ";
                                }
                            }
                        }

                        int discountType = 1;
                        if (columnConfig.containsKey("discountType")) {
                            String discountTypeStr = recarr[(Integer) columnConfig.get("discountType")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(discountTypeStr)) {
                                if (discountTypeStr.equalsIgnoreCase("Percentage")) {
                                    discountType = 1;
                                } else if (discountTypeStr.equalsIgnoreCase("Flat")) {
                                    discountType = 0;
                                } else {
                                    failureMsg += "Format you entered is not correct. It should be like \"Percentage\" or \"Flat\". ";
                                }
                            }
                        }

                        double discount = 0;
                        if (columnConfig.containsKey("discount")) {
                            String discountStr = recarr[(Integer) columnConfig.get("discount")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(discountStr)) {
                                try {
                                    discount = authHandler.round(Double.parseDouble(discountStr), companyID);
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect numeric value for Discount, Please ensure that value type of Discount matches with the Discount. ";
                                }
                            }
                        }

                        Tax rowtax = null;
                        if (columnConfig.containsKey("prtaxid")) {
                            String taxCode = recarr[(Integer) columnConfig.get("prtaxid")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(taxCode)) {
//                                rowtax = accGoodsReceiptModuleServiceobj.getGSTByCode(taxCode, companyID);
                                Map taxMap = new HashMap<>();
                                taxMap.put(Constants.companyKey, companyID);
                                taxMap.put(Constants.TAXCODE, taxCode);
                                ArrayList taxList = importHandlerobj.getTax(taxMap);
                                if (taxList.get(0) != null) {
                                    rowtax = (Tax) taxList.get(0);
                                    if (rowtax.getTaxtype() == Constants.SALES_TYPE_TAX && isIncludeProductTax) {
                                        failureMsg += "Tax Code is not Purchase Type TAX for code " + taxCode;
                                    }
                                } else if (!StringUtil.isNullOrEmpty((String) taxList.get(2))) {
                                    failureMsg += (String) taxList.get(2) + taxCode;
                                }
                            } else {
                                if (isIncludeProductTax) {
                                    failureMsg += "Product Tax is not available. ";
                                }
                            }
                        } else {
                            if (isIncludeProductTax) {
                                failureMsg += "Product Tax column is not found. ";
                            }
                        }

                        String rowtaxamount = "0";
                        if (columnConfig.containsKey("taxamount")) {
                            rowtaxamount = recarr[(Integer) columnConfig.get("taxamount")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(rowtaxamount)) {
                                try {
                                    double rowtaxamountvalue = Double.parseDouble(rowtaxamount);
                                    if (rowtaxamountvalue < 0) {
                                        failureMsg += "Tax Amount should not be negative.";
                                    }
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect numeric value for Tax Amount. ";
                                }
                            }
                        }

                        // Warehouse Location Batch detail validation
                        Map<String, Object> requestMap = new HashMap<>();
                        requestMap.put("isGenerateGoodsReceipt", isGenerateGoodsReceipt);
                        requestMap.put("product", product);
                        requestMap.put("columnConfig", columnConfig);
                        requestMap.put("recarr", recarr);
                        requestMap.put("companyID", companyID);
                        requestMap.put("failureMsg", failureMsg);
                        requestMap.put("masterPreference", masterPreference);
                        requestMap.put("df", df);
                        requestMap.put("dateFormat", dateFormat);

                        boolean isLocationForProduct = false;
                        boolean isWarehouseForProduct = false;
                        boolean isBatchForProduct = false;
                        boolean isSerialForProduct = false;
                        boolean isRowForProduct = false;
                        boolean isRackForProduct = false;
                        boolean isBinForProduct = false;
                        String mfgdate = null;
                        String expdate = null;
                        String expstart = null;
                        String expend = null;
                        double batchquantity = 0;
                        String serialName = "";
                        String batchName = "";

                        InventoryWarehouse warehouseObj = null;
                        InventoryLocation locationObj = null;
                        StoreMaster rowObj = null;
                        StoreMaster rackObj = null;
                        StoreMaster binObj = null;

                        if (!StringUtil.isNullOrEmpty(consignmentID) && product != null) {
                            KwlReturnObject productTypeObj = importDAOobj.getProductTypeOfProduct(product.getID(), companyID);
                            if (productTypeObj.getEntityList() != null && !productTypeObj.getEntityList().isEmpty()) {
                                String productType = productTypeObj.getEntityList().get(0).toString();

                                if (productType.equalsIgnoreCase(Constants.ASSEMBLY) || productType.equalsIgnoreCase(Constants.INVENTORY_PART)) {
                                    failureMsg += "Inventory and Assembly Type Product are not allowed in Consignment link case.";
                                }
                            }
                        }

                        // creating invoice json
                        if (!prevInvNo.equalsIgnoreCase(vendorInvoiceNumber) || vendorInvoiceNumber.equalsIgnoreCase("")) {
                            //If failed invoice then increase failed count and append in failed records string
                            if (failureList.contains(prevInvNo)) {
                                if (singleInvoiceFailureRecoredCount > 0) {
                                    //append record in failed records string
                                    failedRecords.append(singleInvociceFailedRecords);
                                }
                                //Increase failed records count
                                failed += singleInvoiceFailureRecoredCount;
                                //reinitialize variables for next record
                                singleInvociceFailedRecords = new StringBuilder();
                                singleInvoiceFailureRecoredCount = 0;
                            }

                            if ((rowDetailMap.size() > 0 || rows.length() > 0) && !isAlreadyExist && !isRecordFailed) {
                                double taxamount = 0.0;
                                if (!StringUtil.isNullOrEmpty(paramJobj.optString("taxid", null))) {
                                    HashMap<String, Object> taxParams = new HashMap<>();
                                    taxParams.put("transactiondate", sdf.parse(paramJobj.optString("billdate")));
                                    taxParams.put("taxid", paramJobj.optString("taxid"));
                                    taxParams.put("companyid", companyID);
                                    KwlReturnObject taxResult = accTaxObj.getTax(taxParams);
                                    Object[] taxRow = (Object[]) taxResult.getEntityList().get(0);
                                    double taxPercentage = (double) taxRow[1];
                                    taxamount = ((totalamount - totaldiscount) * taxPercentage) / 100;
                                    taxamount = authHandler.round(taxamount, companyID);
                                }
                                paramJobj.put("taxamount", String.valueOf(taxamount));
                                paramJobj.remove("isIncludeProductTax");
                                // on next document saving current documents rows
                                // If product is not service type and non-inventory type then manipulate rows
                                if (rowDetailMap.size() > 0) {
                                    Map<String, Object> resultMap = accGoodsReceiptModuleServiceobj.manipulateRowDetails(rowDetailMap, batchSerialMap, batchMap, batchDetailArr, failedRecords, singleInvociceFailedRecords, totalBatchQty, isRecordFailed, rows);
                                    if (resultMap.containsKey("isRecordFailed")) {
                                        isRecordFailed = (boolean) resultMap.get("isRecordFailed");
                                        if (isRecordFailed) {
                                            if (!failureList.contains(prevInvNo)) {
                                                failureList.add(prevInvNo);
                                            }
                                            failed += singleInvoiceFailureRecoredCount;
                                            if (singleInvociceFailedRecords.toString().length() > 0) {
                                                failedRecords.append(singleInvociceFailedRecords);
                                            }
                                        }
                                    }
                                    if (resultMap.containsKey("batchMap") && resultMap.get("batchMap") != null) {
                                        batchMap = (Map<String, List<JSONObject>>) resultMap.get("batchMap");
                                    }
                                }
                                if (!isRecordFailed) {
                                    paramJobj.put(Constants.detail, rows.toString());
                                    paramJobj.put(Constants.PAGE_URL, requestJobj.optString(Constants.PAGE_URL));
                                    // for save Purchase Invoice
                                    accGoodsReceiptModuleServiceobj.saveGoodsReceipt(paramJobj);
                                }
                            }
                            prevInvNo = vendorInvoiceNumber;
                            // reset variables
                            totaldiscount = 0;
                            totalamount = 0;
                            paramJobj = new JSONObject();
                            rowDetailMap = new HashMap<>();
                            batchMap = new HashMap<>();
                            batchSerialMap = new HashMap<>();
                            batchDetailArr = new JSONArray();
                            rows = new JSONArray();
                            isRecordFailed = false;
                            isAlreadyExist = false;
                            //below variable are get initialized to give correct failure and sucess
                            singleInvociceFailedRecords = new StringBuilder();
                            singleInvoiceFailureRecoredCount = 0;
                            /**
                             * Check invoice number exist or not. If exist then
                             * throw exception with error message.
                             */
                            KwlReturnObject result = accGoodsReceiptobj.getReceiptFromNo(vendorInvoiceNumber, companyID);
                            int nocount = result.getRecordTotalCount();
                            if (nocount > 0) {
                                isAlreadyExist = true;
                                throw new AccountingException("Vendor Invoice number '" + vendorInvoiceNumber + "' already exists.");
                            }
                            
                            result = accGoodsReceiptobj.getGoodsReceiptOrderCount(goodsReceiptNo, companyID);
                            nocount = result.getRecordTotalCount();
                            if (nocount > 0) {
                                isAlreadyExist = true;
                                throw new AccountingException("Goods Receipt No '" + goodsReceiptNo + "' already exists.");
                            }

                            // For create custom field array
                            JSONArray customJArr = accPurchaseOrderModuleServiceObj.createGlobalCustomFieldArrayForImport(requestJobj, jSONArray, recarr, df, Constants.Acc_Vendor_Invoice_ModuleId);

                            // For getting exchange rate
                            double exchangeRateForTransaction = accPurchaseOrderModuleServiceObj.getExchangeRateForTransaction(requestJobj, vendorInvoiceDate, currencyID);

                            String sequenceFormatID = "NA";
                            boolean autogenerated = false;
                            boolean isFromOtherSource = false;
                            if (!StringUtil.isNullOrEmpty(vendorInvoiceNumber)) {
                                int moduleId = requestJobj.optBoolean("incash", false) ? Constants.Acc_Cash_Purchase_ModuleId : Constants.Acc_Vendor_Invoice_ModuleId;
                                Map<String, String> sequenceNumberDataMap = new HashMap<String, String>();
                                sequenceNumberDataMap.put("moduleID", String.valueOf(moduleId));
                                sequenceNumberDataMap.put("entryNumber", vendorInvoiceNumber);
                                sequenceNumberDataMap.put("companyID", companyID);
                                sequenceNumberDataMap.put("isFromImport", "true");
                                List list = importHandlerobj.checksEntryNumberForSequenceNumber(sequenceNumberDataMap);
                                if (!list.isEmpty()) {
                                    boolean isvalidEntryNumber = (Boolean) list.get(0);
                                    if (!isvalidEntryNumber) {
                                        String formatID = (String) list.get(2);
                                        int intSeq = (Integer) list.get(3);
                                        paramJobj.put(Constants.SEQNUMBER, intSeq);
                                        paramJobj.put(Constants.SEQFORMAT, formatID);
                                        autogenerated = true;
                                        sequenceFormatID = formatID;
                                        isFromOtherSource = true;
                                    }
                                }
                            }

                            String goodsReceiptSequenceformatID = "NA";
                            if (!StringUtil.isNullOrEmpty(goodsReceiptNo)) {
                                Map<String, String> sequenceNumberDataMap = new HashMap<String, String>();
                                sequenceNumberDataMap.put("moduleID", String.valueOf(Constants.Acc_Goods_Receipt_ModuleId));
                                sequenceNumberDataMap.put("entryNumber", goodsReceiptNo);
                                sequenceNumberDataMap.put("companyID", companyID);
                                sequenceNumberDataMap.put("isFromImport", "true");
                                List list = importHandlerobj.checksEntryNumberForSequenceNumber(sequenceNumberDataMap);
                                if (!list.isEmpty()) {
                                    boolean isvalidEntryNumber = (Boolean) list.get(0);
                                    if (!isvalidEntryNumber) {
                                        String formatID = (String) list.get(2);
                                        int intSeq = (Integer) list.get(3);
                                        paramJobj.put("DOSeqNum", intSeq);
                                        goodsReceiptSequenceformatID = formatID;
                                        isFromOtherSource = true;
                                    }
                                }
                            }

                            // param obj
                            paramJobj.put(Constants.companyKey, companyID);
                            paramJobj.put(Constants.globalCurrencyKey, requestJobj.getString(Constants.globalCurrencyKey));
                            paramJobj.put(Constants.useridKey, requestJobj.getString(Constants.useridKey));
                            paramJobj.put(Constants.lid, requestJobj.getString(Constants.useridKey));
                            paramJobj.put(Constants.userfullname, requestJobj.getString(Constants.userfullname));
                            paramJobj.put(Constants.reqHeader, requestJobj.getString(Constants.reqHeader));
                            paramJobj.put(Constants.remoteIPAddress, requestJobj.getString(Constants.remoteIPAddress));
                            paramJobj.put(Constants.language, requestJobj.getString(Constants.language));
                            paramJobj.put(Constants.timezonedifference, requestJobj.optString(Constants.timezonedifference));
                            paramJobj.put(Constants.currencyKey, currencyID);
                            // request map for save goods receipt  
                            paramJobj.put("locale", locale);
                            paramJobj.put("customfield", customJArr.toString());
                            paramJobj.put("companyid", companyID);
                            paramJobj.put("df", dateOnlydf);
                            paramJobj.put("userid", requestJobj.getString(Constants.useridKey));
                            paramJobj.put("agent", agentID);
                            if (isGenerateGoodsReceipt == true) {
                                paramJobj.put("autogenerateDO", "on");
                                paramJobj.put("isAutoCreateDO", "true");
                                paramJobj.put("fromLinkComboAutoDO", "Vendor Invoice");
                            }
                            paramJobj.put("billdate", sdf.format(vendorInvoiceDate));
                            paramJobj.put(Constants.SUPPLIERINVOICENO, supplierInvoiceNo);
                            //put cash purchase related fields
                            if (requestJobj.optBoolean("incash", false)) {
                                //create payment details json
                                JSONObject payDetail = new JSONObject();
                                payDetail.put("chequeno", ChequeNo);
                                payDetail.put("description", ChequeDesc);
                                payDetail.put("bankname", bankName);
                                payDetail.put("paymentStatus", cleared ? "Cleared" : "Uncleared");
                                payDetail.put("bankmasteritemid", bankNameMasterItemID);
                                payDetail.put("payDate", chequeDate != null ? sdf.format(chequeDate) : "");
                                payDetail.put("clearanceDate", clearanceDate != null ? sdf.format(clearanceDate) : "");
                                //put payment method details in map
                                paramJobj.put("pmtmethod", payMethodId);
                                paramJobj.put("paydetail", payDetail.toString());
                            }
                            paramJobj.put(Constants.importExportDeclarationNo, importDeclarationNo);
                            paramJobj.put("costcenter", costCenterID);
                            paramJobj.put("currencyid", currencyID);
                            paramJobj.put("duedate", sdf.format(dueDate));
                            paramJobj.put("externalcurrencyrate", String.valueOf(exchangeRateForTransaction));
                            paramJobj.put("fob", fob);
                            paramJobj.put("landedInvoiceID", consignmentID);
                            paramJobj.put("landedInvoiceNumber", consignmentID);
                            paramJobj.put("memo", memo);
                            paramJobj.put("number", vendorInvoiceNumber);
                            paramJobj.put("numberDo", goodsReceiptNo);
                            paramJobj.put("sequenceformat", sequenceFormatID);
                            paramJobj.put("autogenerated", autogenerated);
                            paramJobj.put("isFromOtherSource", isFromOtherSource);
                            paramJobj.put("sequenceformatDo", goodsReceiptSequenceformatID);
                            if (shipDate != null) {
                                paramJobj.put("shipdate", sdf.format(shipDate));
                            }
                            paramJobj.put("shipvia", shipVia);
                            paramJobj.put("term", termID);
                            paramJobj.put("termid", termID);
                            paramJobj.put("vendor", vendorID);
                            paramJobj.put("istemplate", "0");
                            paramJobj.put("defaultAdress", "true");
                            paramJobj.put("seqformat_oldflag", "false");
                            paramJobj.put("seqformat_oldflagDo", "false");
                            paramJobj.put("fromLinkCombo", "");
                            paramJobj.put("linkNumber", "");
                            paramJobj.put("termsincludegst", "false");
                            paramJobj.put("taxamount", "0");
                            paramJobj.put("incash", requestJobj.optString("incash", "false"));
                            paramJobj.put("includeprotax", String.valueOf(isIncludeProductTax));
                            paramJobj.put("includingGST", String.valueOf(isIncludingGST));
                            paramJobj.put("isAllowToEdit", "false");
                            paramJobj.put("isCapitalGoodsAcquired", "false");
                            paramJobj.put("isDraft", "false");
                            paramJobj.put("isEdit", "false");
                            paramJobj.put("isExciseInvoice", "false");
                            paramJobj.put("isLinkedTransaction", "false");
                            paramJobj.put("isMRPSalesOrder", "false");
                            paramJobj.put("isOpeningBalanceOrder", "false");
                            paramJobj.put("isRetailPurchase", "false");
                            paramJobj.put("isfavourite", "false");
                            paramJobj.put("islockQuantity", "false");
                            paramJobj.put("partialinv", "false");
                            paramJobj.put("perdiscount", "false");
                            paramJobj.put("moduletempname", "false");
                            paramJobj.put("invoicetermsmap", "[]");
                            paramJobj.put("shipLength", "1");
                            paramJobj.put("copyInv", "");

                            if (isIncludeTotalTax) {
                                paramJobj.put("taxid", taxID);
                            } else {
                                paramJobj.put("taxid", "");
                            }

                            paramJobj.put("template", "");
                            paramJobj.put("templatename", "");
                            paramJobj.put("validdate", "");
                            paramJobj.put("shippingterm", "");
                            paramJobj.put("podept", "");
                            paramJobj.put("posttext", "");
                            paramJobj.put("poyourref", "");
                            paramJobj.put("project", "");
                            paramJobj.put("requestor", "");
                            paramJobj.put("manufacturerType", "");
                            paramJobj.put("RMCDApprovalNo", "");
                            paramJobj.put("customerporefno", "");
                            paramJobj.put("defaultnatureofpurchase", "");
                            paramJobj.put("deletedLinkedDocumentId", "");
                            paramJobj.put("deliveryTime", "");
                            paramJobj.put("delydate", "");
                            paramJobj.put("delyterm", "");
                            paramJobj.put("discount", "0");
                            paramJobj.put("doid", "");
                            paramJobj.put("excisetypeid", "");
                            paramJobj.put("formtypeid", "");
                            paramJobj.put("gstCurrencyRate", "0");
                            paramJobj.put("importService", "false");
                            paramJobj.put("invoiceto", "");
                            paramJobj.put("invoicetype", "");
                            paramJobj.put("isselfbilledinvoice", "");
                            paramJobj.put("merno", "");
                            paramJobj.put("mode", "11");
                            paramJobj.put("isIncludeProductTax", isIncludeProductTax);

                            SimpleDateFormat formatter = (SimpleDateFormat) authHandler.getGlobalDateFormat();
                            paramJobj.put(Constants.Checklocktransactiondate, formatter.format(vendorInvoiceDate));

                            Map<String, Object> requestParams = new HashMap<>();
                            requestParams.put(Constants.companyKey, companyID);
                            CompanyPreferencesCMN.checkLockPeriod(accCompanyPreferencesObj, requestParams, vendorInvoiceDate, false);
                        } // end global details
                        /**
                         * Validate warehouse, location, batch, serial, row,
                         * rack, bin, batch quantity etc.
                         */
                        if (isGenerateGoodsReceipt && product != null && (product.isIswarehouseforproduct() && product.isIslocationforproduct())) {
                            Map<String, Object> returnMap = accProductModuleServiceobj.validateBatchSerialDetail(requestMap);
                            if (returnMap.containsKey("isWarehouseForProduct") && returnMap.get("isWarehouseForProduct") != null) {
                                isWarehouseForProduct = (boolean) returnMap.get("isWarehouseForProduct");
                            }
                            if (returnMap.containsKey("isLocationForProduct") && returnMap.get("isLocationForProduct") != null) {
                                isLocationForProduct = (boolean) returnMap.get("isLocationForProduct");
                            }
                            if (returnMap.containsKey("isBatchForProduct") && returnMap.get("isBatchForProduct") != null) {
                                isBatchForProduct = (boolean) returnMap.get("isBatchForProduct");
                            }
                            if (returnMap.containsKey("isSerialForProduct") && returnMap.get("isSerialForProduct") != null) {
                                isSerialForProduct = (boolean) returnMap.get("isSerialForProduct");
                            }
                            if (returnMap.containsKey("isRowForProduct") && returnMap.get("isRowForProduct") != null) {
                                isRowForProduct = (boolean) returnMap.get("isRowForProduct");
                            }
                            if (returnMap.containsKey("isRackForProduct") && returnMap.get("isRackForProduct") != null) {
                                isRackForProduct = (boolean) returnMap.get("isRackForProduct");
                            }
                            if (returnMap.containsKey("isBinForProduct") && returnMap.get("isBinForProduct") != null) {
                                isBinForProduct = (boolean) returnMap.get("isBinForProduct");
                            }
                            if (returnMap.containsKey("warehouseObj") && returnMap.get("warehouseObj") != null) {
                                warehouseObj = (InventoryWarehouse) returnMap.get("warehouseObj");
                            }
                            if (returnMap.containsKey("locationObj") && returnMap.get("locationObj") != null) {
                                locationObj = (InventoryLocation) returnMap.get("locationObj");
                            }
                            if (returnMap.containsKey("rowObj") && returnMap.get("rowObj") != null) {
                                rowObj = (StoreMaster) returnMap.get("rowObj");
                            }
                            if (returnMap.containsKey("rackObj") && returnMap.get("rackObj") != null) {
                                rackObj = (StoreMaster) returnMap.get("rackObj");
                            }
                            if (returnMap.containsKey("binObj") && returnMap.get("binObj") != null) {
                                binObj = (StoreMaster) returnMap.get("binObj");
                            }
                            if (returnMap.containsKey("mfgdate") && returnMap.get("mfgdate") != null) {
                                mfgdate = (String) returnMap.get("mfgdate");
                            }
                            if (returnMap.containsKey("expdate") && returnMap.get("expdate") != null) {
                                expdate = (String) returnMap.get("expdate");
                            }
                            if (returnMap.containsKey("expstart") && returnMap.get("expstart") != null) {
                                expstart = (String) returnMap.get("expstart");
                            }
                            if (returnMap.containsKey("expend") && returnMap.get("expend") != null) {
                                expend = (String) returnMap.get("expend");
                            }
                            if (returnMap.containsKey("batchName") && returnMap.get("batchName") != null) {
                                batchName = (String) returnMap.get("batchName");
                            }
                            if (returnMap.containsKey("serialName") && returnMap.get("serialName") != null) {
                                serialName = (String) returnMap.get("serialName");
                            }
                            if (returnMap.containsKey("failureMsg") && returnMap.get("failureMsg") != null) {
                                failureMsg = (String) returnMap.get("failureMsg");
                            }
                            if (returnMap.containsKey("batchquantity") && returnMap.get("batchquantity") != null) {
                                batchquantity = (double) returnMap.get("batchquantity");
                                totalBatchQty += batchquantity;
                            }
                            if (prevRow.equalsIgnoreCase("") || !prevRow.equalsIgnoreCase(vendorInvoiceNumber + product.getProductid() + unitPrice)) {
                                prevRow = vendorInvoiceNumber + product.getProductid() + unitPrice;
                                totalBatchQty = batchquantity;
                            }
                            if (totalBatchQty > quantity) {
                                failureMsg += "Batch Quantity can't be greater than Product Quantity. ";
                            }
                        }

                        //Throw exception with error message if failure message is available
                        if (!StringUtil.isNullOrEmpty(failureMsg)) {
                            throw new AccountingException(failureMsg);
                        }

                        double conversionFactor = 1;
                        // Add Custom fields details of line items
                        JSONArray lineCustomJArr = accPurchaseOrderModuleServiceObj.createLineLevelCustomFieldArrayForImport(requestJobj, jSONArray, recarr, df, Constants.Acc_Vendor_Invoice_ModuleId);

                        String key = vendorInvoiceNumber + product.getProductid() + unitPrice;
                        JSONObject obj = new JSONObject();
                        if (!isRecordFailed) {
                            if (isGenerateGoodsReceipt && (product.isIswarehouseforproduct() && product.isIslocationforproduct())) {   // For batch Serial Flow
                                String newLineLevelKey = key;
                                if (isWarehouseForProduct && warehouseObj != null) {
                                    key = key + warehouseObj.getName();
                                    obj.put("warehouse", warehouseObj.getId());
                                }
                                if (isLocationForProduct && locationObj != null) {
                                    key = key + locationObj.getName();
                                    obj.put("location", locationObj.getId());
                                }
                                if (isRowForProduct && rowObj != null) {
                                    obj.put("row", rowObj.getId());
                                }
                                if (isRackForProduct && rackObj != null) {
                                    obj.put("rack", rackObj.getId());
                                }
                                if (isBinForProduct && binObj != null) {
                                    obj.put("bin", binObj.getId());
                                }
                                obj.put("quantity", batchquantity);
                                obj.put("isBatchForProduct", isBatchForProduct);
                                if (isBatchForProduct) {
                                    obj.put("batch", batchName);
                                    obj.put("mfgdate", mfgdate);
                                    obj.put("expdate", expdate);
                                    obj.put("isSerialForProduct", isSerialForProduct);

                                    String batchKey = isBatchForProduct ? key + batchName : key;
                                    if (batchMap.containsKey(batchKey)) {
                                        List<JSONObject> list = batchMap.get(batchKey);
                                        list.add(obj);
                                        batchMap.put(batchKey, list);
                                    } else {
                                        List<JSONObject> list = new ArrayList<>();
                                        list.add(obj);
                                        batchMap.put(batchKey, list);
                                    }
                                }
                                if (isSerialForProduct) {
                                    obj.put("serialno", serialName);
                                    obj.put("serialnoid", "");
                                    obj.put("expstart", expstart);
                                    obj.put("expend", expend);

                                    String batchKey = isBatchForProduct ? key + batchName : key;
                                    if (batchSerialMap.containsKey(batchKey)) {
                                        List<Object> list = batchSerialMap.get(batchKey);
                                        list.add(serialName);
                                        batchSerialMap.put(batchKey, list);
                                    } else {
                                        List<Object> list = new ArrayList<>();
                                        list.add(serialName);
                                        batchSerialMap.put(batchKey, list);
                                    }
                                }

                                if (!rowDetailMap.containsKey(newLineLevelKey)) {
                                    if (rowDetailMap.size() > 0) {
                                        Map<String, Object> resultMap = accGoodsReceiptModuleServiceobj.manipulateRowDetails(rowDetailMap, batchSerialMap, batchMap, batchDetailArr, failedRecords, singleInvociceFailedRecords, totalBatchQty, isRecordFailed, rows);
                                        if (resultMap.containsKey("isRecordFailed")) {
                                            isRecordFailed = (boolean) resultMap.get("isRecordFailed");
                                            if (isRecordFailed) {
                                                if (!failureList.contains(vendorInvoiceNumber)) {
                                                    failureList.add(vendorInvoiceNumber);
                                                }
                                                paramJobj = new JSONObject();
                                                rows = new JSONArray();
                                            }
                                        }
                                        if (resultMap.containsKey("batchMap") && resultMap.get("batchMap") != null) {
                                            batchMap = (Map<String, List<JSONObject>>) resultMap.get("batchMap");
                                        }
                                        rowDetailMap = new HashMap<>();
                                        batchSerialMap = new HashMap<>();
                                        batchDetailArr = new JSONArray();
                                    }

                                    if (!isRecordFailed) {
                                        JSONObject invdData = new JSONObject();
                                        invdData.put("priceSource", "");
                                        invdData.put("dependentType", "");
                                        invdData.put("inouttime", "");
                                        invdData.put("showquantity", "");
                                        invdData.put(DESC, (product != null && !StringUtil.isNullOrEmpty(product.getDescription())) ? product.getDescription() : "");
                                        invdData.put("supplierpartnumber", "");
                                        invdData.put("invstore", "");
                                        invdData.put("invlocation", "");
                                        invdData.put("permit", "");
                                        invdData.put("gstCurrencyRate", "0.0");
                                        invdData.put("linkto", "");
                                        invdData.put("rowid", "");
                                        invdData.put("savedrowid", "");
                                        invdData.put(RATE, String.valueOf(authHandler.roundUnitPrice(unitPrice, companyID)));
                                        if (isIncludingGST) {
                                            invdData.put("rateIncludingGst", String.valueOf(authHandler.roundUnitPrice(Double.parseDouble(unitPriceIncludingGST), companyID)));
                                        }
                                        invdData.put(PRODUCTID, (product != null) ? product.getID() : "");
                                        invdData.put("quantity", String.valueOf(authHandler.roundQuantity(quantity, companyID)));
                                        invdData.put("uomid", (uom != null) ? uom.getID() : "");
                                        invdData.put("baseuomquantity", String.valueOf(authHandler.roundQuantity(quantity * conversionFactor, companyID)));
                                        invdData.put(PRDISCOUNT, String.valueOf(discount));
                                        invdData.put("discountispercent", String.valueOf(discountType));
                                        if (paramJobj.has("isIncludeProductTax") && paramJobj.optBoolean("isIncludeProductTax") == true && rowtax != null) {
                                            invdData.put("prtaxid", rowtax.getID());
                                            invdData.put("taxamount", rowtaxamount);
                                        } else {
                                            invdData.put("prtaxid", "");
                                            invdData.put("taxamount", "0");
                                        }

                                        invdData.put("taxpercent", 0);
                                        invdData.put("LineTermdetails", "");
                                        invdData.put("productcustomfield", "[{}]");
                                        invdData.put("productMRP", "");
                                        invdData.put("valuationType", "");
                                        invdData.put("reortingUOMExcise", "");
                                        invdData.put("reortingUOMSchemaExcise", "");
                                        invdData.put("valuationTypeVAT", "");
                                        invdData.put("reportingUOMVAT", "");
                                        invdData.put("reportingUOMSchemaVAT", "");
                                        invdData.put("recTermAmount", "");
                                        invdData.put("OtherTermNonTaxableAmount", "");
                                        invdData.put("changedQuantity", String.valueOf(authHandler.roundQuantity(quantity, companyID)));
                                        invdData.put("baseuomrate", String.valueOf(conversionFactor));
                                        invdData.put("customfield", lineCustomJArr.toString());
                                        
                                        if (product != null && (product.getProducttype().getID().equals(Producttype.SERVICE) || product.getProducttype().getID().equals(Producttype.NON_INVENTORY_PART))) {
                                            invdData.put("noQuantityMatchingRequire", true);
                                        }
                                        double rate = authHandler.roundUnitPrice(unitPrice, companyID);
                                        if (paramJobj.has("includingGST") && paramJobj.optString("includingGST").equalsIgnoreCase("true") && !StringUtil.isNullOrEmpty(unitPriceIncludingGST)) {
                                            rate = authHandler.roundUnitPrice(Double.parseDouble(unitPriceIncludingGST), companyID);
                                        }
                                        double rowAmount = authHandler.round(rate * authHandler.round(quantity, companyID), companyID);
                                        rowAmount = authHandler.round(rowAmount, companyID);
                                        totalamount += rowAmount;

                                        double rowdiscount = discount;
                                        if (discountType == 1) { // for percent disc
                                            rowdiscount = (rowAmount * discount) / 100;
                                        }
                                        rowdiscount = authHandler.round(rowdiscount, companyID);
                                        totaldiscount += rowdiscount;
                                        rowDetailMap.put(newLineLevelKey, invdData);
                                        batchDetailArr.put(obj);
                                    }
                                } else {
                                    batchDetailArr.put(obj);
                                }
                            } else {
                                // for already existing row having batch details and next product don't have batchdetails. to first row details then process current 
                                if (rowDetailMap.size() > 0) {
                                    Map<String, Object> resultMap = accGoodsReceiptModuleServiceobj.manipulateRowDetails(rowDetailMap, batchSerialMap, batchMap, batchDetailArr, failedRecords, singleInvociceFailedRecords, totalBatchQty, isRecordFailed, rows);
                                    if (resultMap.containsKey("isRecordFailed")) {
                                        isRecordFailed = (boolean) resultMap.get("isRecordFailed");
                                        if (isRecordFailed) {
                                            if (!failureList.contains(vendorInvoiceNumber)) {
                                                failureList.add(vendorInvoiceNumber);
                                            }
                                            paramJobj = new JSONObject();
                                            rows = new JSONArray();
                                        }
                                    }
                                    if (resultMap.containsKey("batchMap") && resultMap.get("batchMap") != null) {
                                        batchMap = (Map<String, List<JSONObject>>) resultMap.get("batchMap");
                                    }
                                    rowDetailMap = new HashMap<>();
                                    batchSerialMap = new HashMap<>();
                                    batchDetailArr = new JSONArray();
                                }

                                if (!isRecordFailed) {
                                    // For Normal Flow
                                    JSONObject invdData = new JSONObject();
                                    invdData.put("priceSource", "");
                                    invdData.put("dependentType", "");
                                    invdData.put("inouttime", "");
                                    invdData.put("showquantity", "");
                                    invdData.put(DESC, (product != null && !StringUtil.isNullOrEmpty(product.getDescription())) ? product.getDescription() : "");
                                    invdData.put("supplierpartnumber", "");
                                    invdData.put("invstore", "");
                                    invdData.put("invlocation", "");
                                    invdData.put("permit", "");
                                    invdData.put("gstCurrencyRate", "0.0");
                                    invdData.put("linkto", "");
                                    invdData.put("rowid", "");
                                    invdData.put("savedrowid", "");
                                    invdData.put(RATE, String.valueOf(authHandler.roundUnitPrice(unitPrice, companyID)));
                                    if (isIncludingGST) {
                                        invdData.put("rateIncludingGst", String.valueOf(authHandler.roundUnitPrice(Double.parseDouble(unitPriceIncludingGST), companyID)));
                                    }
                                    invdData.put(PRODUCTID, (product != null) ? product.getID() : "");
                                    invdData.put("quantity", String.valueOf(authHandler.roundQuantity(quantity, companyID)));
                                    invdData.put("uomid", (uom != null) ? uom.getID() : "");
                                    invdData.put("baseuomquantity", String.valueOf(authHandler.roundQuantity(quantity * conversionFactor, companyID)));
                                    invdData.put(PRDISCOUNT, String.valueOf(discount));
                                    invdData.put("discountispercent", String.valueOf(discountType));
                                    if (paramJobj.has("isIncludeProductTax") && paramJobj.optBoolean("isIncludeProductTax") == true && rowtax != null) {
                                        invdData.put("prtaxid", rowtax.getID());
                                        invdData.put("taxamount", rowtaxamount);
                                    } else {
                                        invdData.put("prtaxid", "");
                                        invdData.put("taxamount", "0");
                                    }

                                    invdData.put("taxpercent", 0);
                                    invdData.put("LineTermdetails", "");
                                    invdData.put("productcustomfield", "[{}]");
                                    invdData.put("productMRP", "");
                                    invdData.put("valuationType", "");
                                    invdData.put("reortingUOMExcise", "");
                                    invdData.put("reortingUOMSchemaExcise", "");
                                    invdData.put("valuationTypeVAT", "");
                                    invdData.put("reportingUOMVAT", "");
                                    invdData.put("reportingUOMSchemaVAT", "");
                                    invdData.put("recTermAmount", "");
                                    invdData.put("OtherTermNonTaxableAmount", "");
                                    invdData.put("changedQuantity", String.valueOf(authHandler.roundQuantity(quantity, companyID)));
                                    invdData.put("baseuomrate", String.valueOf(conversionFactor));
                                    invdData.put("customfield", lineCustomJArr.toString());

                                    double rate = authHandler.roundUnitPrice(unitPrice, companyID);
                                    if (paramJobj.has("includingGST") && paramJobj.optString("includingGST").equalsIgnoreCase("true") && !StringUtil.isNullOrEmpty(unitPriceIncludingGST)) {
                                        rate = authHandler.roundUnitPrice(Double.parseDouble(unitPriceIncludingGST), companyID);
                                    }
                                    double rowAmount = authHandler.round(rate * authHandler.roundQuantity(quantity, companyID), companyID);
                                    rowAmount = authHandler.round(rowAmount, companyID);
                                    totalamount += rowAmount;

                                    double rowdiscount = discount;
                                    if (discountType == 1) { // for percent disc
                                        rowdiscount = (rowAmount * discount) / 100;
                                    }
                                    rowdiscount = authHandler.round(rowdiscount, companyID);
                                    totaldiscount += rowdiscount;
                                    rows.put(invdData);
                                }
                            }
                        }

                    } catch (Exception ex) {
//                        failed++;
                        isRecordFailed = true;
                        isfailurerecord = true;
                        String errorMsg = "";
                        if (ex.getMessage() != null) {
                            errorMsg = ex.getMessage();
                        } else if (ex.getCause() != null) {
                            errorMsg = ex.getCause().getMessage();
                        }
                        if (!failureList.contains(vendorInvoiceNumber)) {
                            if (singleInvoiceFailureRecoredCount > 0) {
                                failedRecords.append(singleInvociceFailedRecords);
                                singleInvociceFailedRecords = new StringBuilder();
                            }
//                                failed += singleInvoiceFailureRecoredCount;
                            prevInvNo = vendorInvoiceNumber;
                            failureList.add(vendorInvoiceNumber);
                        }
                        singleInvoiceFailureRecoredCount++;
                        singleInvociceFailedRecords.append("\n").append(accSalesOrderServiceobj.createCSVrecord(recarr)).append("\"").append(errorMsg.replaceAll("\"", "")).append("\"");
                    }
                    if (!isfailurerecord) {
                        singleInvoiceFailureRecoredCount++;
                        singleInvociceFailedRecords.append("\n").append(accSalesOrderServiceobj.createCSVrecord(recarr)).append("\"").append(" ").append("\"");
                    }
                    total++;
                }
                cnt++;
            }

            // save PI for last record
            if (!isAlreadyExist && !isRecordFailed) {
                double taxamount = 0.0;
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("taxid"))) {
                    HashMap<String, Object> taxParams = new HashMap<>();
                    taxParams.put("transactiondate", sdf.parse(paramJobj.optString("billdate")));
                    taxParams.put("taxid", paramJobj.optString("taxid"));
                    taxParams.put("companyid", companyID);
                    KwlReturnObject taxResult = accTaxObj.getTax(taxParams);
                    Object[] taxRow = (Object[]) taxResult.getEntityList().get(0);
                    double taxPercentage = (double) taxRow[1];
                    taxamount = ((totalamount - totaldiscount) * taxPercentage) / 100;
                    taxamount = authHandler.round(taxamount, companyID);
                }
                paramJobj.put("taxamount", String.valueOf(taxamount));
                paramJobj.remove("isIncludeProductTax");
                Map<String, Object> resultMap = accGoodsReceiptModuleServiceobj.manipulateRowDetails(rowDetailMap, batchSerialMap, batchMap, batchDetailArr, failedRecords, singleInvociceFailedRecords, totalBatchQty, isRecordFailed, rows);
                if (resultMap.containsKey("isRecordFailed")) {
                    isRecordFailed = (boolean) resultMap.get("isRecordFailed");
                    if (!isRecordFailed) {
                        rowDetailMap = new HashMap<>();
                        batchSerialMap = new HashMap<>();
                        batchDetailArr = new JSONArray();

                    }
                }
                if (resultMap.containsKey("batchMap") && resultMap.get("batchMap") != null) {
                    batchMap = (Map<String, List<JSONObject>>) resultMap.get("batchMap");
                }

                if (!isRecordFailed) {

                    paramJobj.put(Constants.detail, rows.toString());
                    paramJobj.put(Constants.PAGE_URL, requestJobj.optString(Constants.PAGE_URL));
                    // for save Purchase Invoice
                    accGoodsReceiptModuleServiceobj.saveGoodsReceipt(paramJobj);
                }
            }

            if (isRecordFailed) {// only if last invoice is failed
                failed += singleInvoiceFailureRecoredCount; // last interation failure record
                if (singleInvociceFailedRecords.toString().length() > 0) {
                    failedRecords.append(singleInvociceFailedRecords);
                }
            }

            if (failed > 0) {
                importHandlerobj.createFailureFiles(fileName, failedRecords, ".csv");
            }

            int success = total - failed;
            if (total == 0) {
                msg = "Empty file.";
            } else if (success == 0) {
                msg = "Failed to import all the records.";
            } else if (success == total) {
                msg = "All records are imported successfully.";
            } else {
                msg = "Imported " + success + " record" + (success > 1 ? "s" : "") + " successfully";
                msg += (failed == 0 ? "." : " and failed to import " + failed + " record" + (failed > 1 ? "s" : "") + ".");
            }
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();

            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Importing Records.");
        } finally {
            fileInputStream.close();
            csvReader.close();

            // For saving import log
            accPurchaseOrderModuleServiceObj.saveImportLog(requestJobj, msg, total, failed, Constants.Acc_Vendor_Invoice_ModuleId);

            try {
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", ImportLog.getActualFileName(fileName));
                returnObj.put("Module", Constants.Acc_Vendor_Invoice_ModuleId);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return returnObj;
    }

    public void run() {
        try {
            JSONObject jobj = new JSONObject();
            while (!processQueue.isEmpty() && !isworking) {
                this.isworking = true;
                JSONObject paramJobj = (JSONObject) processQueue.get(0);
                try {
                    boolean isSales = paramJobj.optBoolean("issales", false);
                    if (isSales) {                        
                        jobj = importInvoicesJSON(paramJobj);
                    } else {                    
                        jobj = importPurchaseInvoiceJSON(paramJobj);
                    }
                    sendMail(paramJobj, jobj);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    processQueue.remove(paramJobj);
                    this.isworking = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
//        importInvoiceModule(paramJobj);
    }

    public void sendMail(JSONObject requestParams1, JSONObject jobj) throws ServiceException {
        try {
            String modulename = requestParams1.get("modName").toString();
            User user = (User) kwlCommonTablesDAOobj.getClassObject("com.krawler.common.admin.User", requestParams1.get("userid").toString());
            Company company = (Company) kwlCommonTablesDAOobj.getClassObject(Company.class.getName(), user.getCompany().getCompanyID());
            String htmltxt = "Report for data imported.<br/>";
            htmltxt += "<br/>Module Name: " + modulename + "<br/>";
            htmltxt += "<br/>File Name: " + jobj.optString("filename", "") + "<br/>";
            htmltxt += "Total Records: " + jobj.optString("totalrecords", "") + "<br/>";
            htmltxt += "Records Imported Successfully: " + jobj.get("successrecords");
            htmltxt += "<br/>Failed Records: " + jobj.get("failedrecords");
            htmltxt += "<br/>URL: " + requestParams1.optString("baseUrl") + "<br/>";
            htmltxt += "<br/><br/>Please check the import log in the system for more details.";
            htmltxt += "<br/>For queries, email us at support@deskera.com<br/>";
            htmltxt += "Deskera Team";

            String plainMsg = "Report for data imported.\n";
            plainMsg += "\nModule Name: " + modulename + "\n";
            plainMsg += "\nFile Name:" + jobj.get("filename") + "\n";
            plainMsg += "Total Records: " + jobj.get("totalrecords");
            plainMsg += "\nRecords Imported Successfully: " + jobj.get("successrecords");
            plainMsg += "\nFailed Records: " + jobj.get("failedrecords");
            plainMsg += "\nURL: " + requestParams1.optString("baseUrl");
            plainMsg += "\n\nPlease check the import log in the system for more details.";

            plainMsg += "\nFor queries, email us at support@deskera.com\n";
            plainMsg += "Deskera Team";
            String fromEmailId = (!company.isEmailFromCompanyCreator()) ? Constants.ADMIN_EMAILID : authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
            Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
            SendMailHandler.postMail(new String[]{user.getEmailID()}, "Deskera Accounting - Report for data imported", htmltxt, plainMsg, fromEmailId, smtpConfigMap);
        } catch (Exception ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE("Importproduct.sendMail : " + ex.getMessage(), ex);
        }

    }

    public LinkedHashMap<String, InvoiceTermsSales> getSlaestermMapByNameKey(String companyid, boolean isSlaesTerm) {
        LinkedHashMap<String, InvoiceTermsSales> salesTermMap = new LinkedHashMap<>();//LinkedHashMap used for keeping order
        HashMap hashMap = new HashMap();
        hashMap.put("companyid", companyid);
        hashMap.put("salesOrPurchaseFlag", String.valueOf(isSlaesTerm));
        KwlReturnObject result = accAccountDAOobj.getInvoiceTerms(hashMap);
        List<InvoiceTermsSales> termList = result.getEntityList();
        for (InvoiceTermsSales sales : termList) {
            salesTermMap.put(sales.getTerm().toLowerCase(), sales);
        }
        return salesTermMap;
    }

    public boolean isAllTermInvoiceInformationIsEmpty(HashMap<String, Integer> columnConfig, String[] recarr) {
        boolean isEmptyLineLevelData = true;
        Set<String> configSet = new HashSet<>();
        configSet.add("invoiceterm");//Invoice Term
        configSet.add("termtype");//Term Type
        configSet.add("termvalue");//Term Value
        for (String configName : configSet) {
            if (columnConfig.containsKey(configName)) {
                String configValue = recarr[(Integer) columnConfig.get(configName)].replaceAll("\"", "").trim();
                if (!StringUtil.isNullOrEmpty(configValue)) {
                    isEmptyLineLevelData = false;
                    break;
                }
            }
        }
        return isEmptyLineLevelData;
    }

    
}
