/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.product;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.CsvReader;
import com.krawler.common.util.DataInvalidateException;
import com.krawler.common.util.StringUtil;
import com.krawler.customFieldMaster.fieldDataManager;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.model.frequency.Frequency;
import com.krawler.inventory.model.packaging.Packaging;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import static com.krawler.spring.accounting.product.accProductController.getActualFileName;
import com.krawler.spring.accounting.productmodule.service.AccProductModuleService;
import com.krawler.spring.accounting.uom.accUomDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.importFunctionality.ImportDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
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
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.springframework.context.MessageSource;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class ImportProduct implements Runnable {

    ArrayList processQueue = new ArrayList();
    private boolean isworking = false;

    public boolean isIsworking() {
        return isworking;
    }

    public void setIsworking(boolean isworking) {
        this.isworking = isworking;
    }
    private int importLimit = 1500;
    private HibernateTransactionManager txnManager;
    private accProductDAO accProductObj;
//    private String successView;
    private MessageSource messageSource;
    private fieldDataManager fieldDataManagercntrl;
    private accAccountDAO accAccountDAOobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    public ImportHandler importHandler;
    private ImportDAO importDao;
    private accJournalEntryDAO accJournalEntryobj;
    private AccCommonTablesDAO accCommonTablesDAO;
    private accUomDAO accUomObj;
    private accMasterItemsDAO accMasterItemsDAOobj;
    private AccProductModuleService accProductModuleService;
    private accCurrencyDAO accCurrencyDAOobj;
    private authHandlerDAO authHandlerDAOObj;

    private kwlCommonTablesDAO kwlCommonTablesDAOObj;

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public void setAuthHandlerDAOObj(authHandlerDAO authHandlerDAOObj) {
        this.authHandlerDAOObj = authHandlerDAOObj;
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

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
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

    public void setaccUomDAO(accUomDAO accUomObj) {
        this.accUomObj = accUomObj;
    }

    public void setaccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }

    public void setAccProductModuleService(AccProductModuleService accProductModuleService) {
        this.accProductModuleService = accProductModuleService;
    }

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
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
            logDataMap.put("Log", "Pending");
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

                    boolean allowropagatechildcompanies = requestParams1.containsKey("allowropagatechildcompanies") ? Boolean.parseBoolean(requestParams1.get("allowropagatechildcompanies").toString()) : false;
                    if (allowropagatechildcompanies) {
                        Object childcompanies = requestParams1.containsKey("childcompanylist") ? requestParams1.get("childcompanylist") : "";
                        List childcompanylist = (List) childcompanies;
                        int totalChildCompanies = childcompanylist.size();

                        int currentChildCompanycount = 0;
                        requestParams1.put("totalChildCompanies", totalChildCompanies);
                        for (Object childObj : childcompanylist) {
                            try {
                                currentChildCompanycount++;
                                Object[] childdataOBj = (Object[]) childObj;
                                String childCompanyID = (String) childdataOBj[0];
                                requestParams1.put("companyid", childCompanyID);
                                requestParams1.put("currentChildCompanycount", currentChildCompanycount);
                                JSONObject jobj = new JSONObject();
                                String modulename = requestParams1.get("modName").toString();
                                String importflag = requestParams1.get("importflag").toString();
                                if (importflag.equalsIgnoreCase(Constants.importproductxls)) {
                                    jobj = importproductsdata(requestParams1);
                                } else if (importflag.equalsIgnoreCase(Constants.importproductcsv)) {
                                    jobj = importProductcsv(requestParams1);
                                }
                                sendMail(requestParams1, jobj);
                            } catch (Exception ex) {
                                Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        }
                    } else {

                        JSONObject jobj = new JSONObject();
                        String modulename = requestParams1.get("modName").toString();
                        String importflag = requestParams1.get("importflag").toString();
                        if (importflag.equalsIgnoreCase(Constants.importproductxls)) {
                            jobj = importproductsdata(requestParams1);
                        } else if (importflag.equalsIgnoreCase(Constants.importassemblyproduct)) {
                            jobj = importAssemblyProductCSV(requestParams1);  //import Assembly product for csv File
                        } else if (importflag.equalsIgnoreCase(Constants.importproductpricecsv)) {
                            jobj = importProductPriceCSV(requestParams1);
                        } else if (importflag.equalsIgnoreCase(Constants.importproductopeningqty)) {
                            jobj = importProductopeningqtyRecords(requestParams1); //import product opening quantity 
                        } else if (importflag.equalsIgnoreCase(Constants.importproductcsv)) {
                            jobj = importProductcsv(requestParams1);
                        } else if (importflag.equalsIgnoreCase(Constants.importproductcategorycsv)) {
                            jobj = importProductCategorycsv(requestParams1);
                        }
                        sendMail(requestParams1, jobj);
                    }
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

    public void sendMail(Map<String, Object> requestParams1, JSONObject jobj) throws ServiceException {
        try {
            String modulename = requestParams1.get("modName").toString();
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
        } catch (Exception ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE("Importproduct.sendMail : " + ex.getMessage(), ex);
        }

    }

    public JSONObject importproductsdata(HashMap<String, Object> requestParams1) throws AccountingException {

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("import_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        boolean commitedEx = false;
        boolean issuccess = true;
        String msg = "";
        String fileName = null;
        int total = 0, failed = 0;
        String companyid = requestParams1.get("companyid").toString();
        String currencyId = requestParams1.get("currencyId").toString();
        String userId = requestParams1.get("userid").toString();
        DateFormat df = (DateFormat) requestParams1.get("dateFormat");
        JSONObject datajobj = new JSONObject();
        datajobj = (JSONObject) requestParams1.get("jobj");
        String masterPreference = requestParams1.get("masterPreference").toString();
        boolean updateExistingRecordFlag = false;
        if (!StringUtil.isNullOrEmpty(requestParams1.get("updateExistingRecordFlag").toString())) {
            updateExistingRecordFlag = Boolean.FALSE.parseBoolean(requestParams1.get("updateExistingRecordFlag").toString());
        }
        int limit = Constants.Transaction_Commit_Limit;
        int count = 1;

        JSONObject returnObj = new JSONObject();

        try {
            Date newUserDate = new Date();
            KwlReturnObject custumObjresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company1 = (Company) custumObjresult.getEntityList().get(0);
            if (company1.getCreator() != null) {
                newUserDate = authHandler.getUserNewDate(null, company1.getCreator().getTimeZone()!=null?company1.getCreator().getTimeZone().getDifference() : company1.getTimeZone().getDifference());
            }
            fileName = datajobj.getString("filename");
            KwlReturnObject extraCompanyPrefResult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraCompanyPrefResult.getEntityList().get(0);

            int sheetNo = Integer.parseInt(requestParams1.get("sheetindex").toString());
            String customfield = "";
            System.out.println("filenameinrun" + datajobj.getString("FilePath"));
            FileInputStream fs = new FileInputStream(datajobj.getString("FilePath"));

            Workbook wb = WorkbookFactory.create(fs);
//            HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(wb);
            Sheet sheet = wb.getSheetAt(sheetNo);

            int cnt = 0;

            StringBuilder failedRecords = new StringBuilder();

            Map<Integer, Frequency> frequencyMap = getCCFrequencyMap();
            HashMap<String, Integer> columnConfig = new HashMap<String, Integer>();
            JSONArray jSONArray = datajobj.getJSONArray("resjson");
            List headArrayList = new ArrayList();
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
                headArrayList.add(jSONObject.get("csvheader"));

                columnConfig.put(jSONObject.getString("dataindex"), jSONObject.getInt("csvindex"));
            }

            failedRecords.append(createCSVrecord(headArrayList.toArray()) + "\"Error Message\""); // failedRecords.append("\"Row No.\","+createCSVrecord(fileData)+"\"Error Message\"");
            HashMap currencyMap = getCurrencyMap();
            int maxCol = 0;

            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (i == 0) {
                    maxCol = row.getLastCellNum();
                }
                if (cnt != 0) {
                    List recarr = new ArrayList();

                    for (int cellcount = 0; cellcount < maxCol; cellcount++) {
                        Cell cell = row.getCell(cellcount);

                        if (cell != null) {
                            recarr.add(cell);
                        } else {
                            recarr.add("");
                        }
                    }

                    try {
                        //currencyId = sessionHandlerImpl.getCurrencyID(request);

                        String productTypeID = "";
                        if (columnConfig.containsKey("type")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("type"));

                            if (cell == null) {
                                throw new AccountingException("Product Type is not available");
                            }

                            String productTypeName = cell.getStringCellValue().trim();

                            Producttype producttype = getProductTypeByName(productTypeName);
                            if (producttype != null) {
                                productTypeID = producttype.getID();
                            } else {
                                throw new AccountingException("Product Type is not found for " + productTypeName);
                            }
                        } else {
                            throw new AccountingException("Product Type column is not found.");
                        }

                        String productName = "";
                        if (columnConfig.containsKey("productname")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("productname"));
                            if (cell == null) {
                                throw new AccountingException("Product Name is not available");
                            } else {
                                switch (cell.getCellType()) {
                                    case Cell.CELL_TYPE_NUMERIC:
                                        productName = Integer.toString((int) cell.getNumericCellValue());
                                        break;
                                    case Cell.CELL_TYPE_STRING:
                                        productName = cell.getStringCellValue().trim();
                                        break;
                                }
                            }
                        } else {
                            throw new AccountingException("Product Name column is not found.");
                        }

                        String productID = "";
                        if (columnConfig.containsKey("pid")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("pid"));

                            if (cell == null) {
                                throw new AccountingException("Product ID is not available");
                            } else {
                                switch (cell.getCellType()) {
                                    case Cell.CELL_TYPE_NUMERIC:
                                        productID = Integer.toString((int) cell.getNumericCellValue());
                                        break;
                                    case Cell.CELL_TYPE_STRING:
                                        productID = cell.getStringCellValue().trim();
                                        break;
                                }
                                System.out.println(productID);
                            }
                        } else {
                            throw new AccountingException("Product ID column is not found");
                        }

                        String productDescription = "";
                        if (columnConfig.containsKey("desc")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("desc"));
                            if (cell != null) {
                                switch (cell.getCellType()) {
                                    case Cell.CELL_TYPE_NUMERIC:
                                        productDescription = Integer.toString((int) cell.getNumericCellValue());
                                        break;
                                    case Cell.CELL_TYPE_STRING:
                                        productDescription = cell.getStringCellValue().trim();
                                        break;
                                }
                            }
                        }

                        String productUOMID = "";
                        UnitOfMeasure uom = null;
                        if (columnConfig.containsKey("uomname")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("uomname"));

                            if (cell != null) {
                                String productUOMName = cell.getStringCellValue().trim();
                                uom = getUOMByName(productUOMName, companyid);
                                if (uom != null) {
                                    productUOMID = uom.getID();
                                } else {
                                    if (masterPreference.equalsIgnoreCase("2")) {
                                        HashMap<String, Object> uomMap = new HashMap<String, Object>();
                                        uomMap.put("uomname", StringUtil.DecodeText(productUOMName));
                                        uomMap.put("uomtype", StringUtil.DecodeText(productUOMName));
                                        uomMap.put("precision", 0);
                                        uomMap.put("companyid", companyid);

                                        KwlReturnObject uomresult = accUomObj.addUoM(uomMap);
                                        uom = (UnitOfMeasure) uomresult.getEntityList().get(0);
                                        productUOMID = uom.getID();
                                    } else {
//                                        if (masterPreference.equalsIgnoreCase("1")) {
//                                            productUOMID = "";
//                                        } else {
                                            throw new AccountingException("Product Unit Of Measure is not found for " + productUOMName);
//                                        }
                                    }
                                }
                            } else {
//                                if (masterPreference.equalsIgnoreCase("1")) {
//                                    productUOMID = "";
//                                } else {
                                    if (productTypeID.equals(Producttype.SERVICE)) {
//                                        productUOMID = "";
                                        uom = getUOMByName("N/A", companyid);
                                        if (uom != null) {
                                            productUOMID = uom.getID();
                                        }
                                    } else {
                                        throw new AccountingException("Product Unit Of Measure is not available");
                                    }
//                                }
                            }
                        } else {
                            throw new AccountingException("Product Unit Of Measure is not available");
                        }

                        String casingUoMID = "";
                        UnitOfMeasure casinguom = null;
                        if (columnConfig.containsKey("casinguom")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("casinguom"));
                            if (cell != null) {
                                String productUOMName = cell.getStringCellValue().trim();
                                casinguom = getUOMByName(productUOMName, companyid);
                                if (casinguom != null) {
                                    casingUoMID = casinguom.getID();
                                } else {
                                    if (masterPreference.equalsIgnoreCase("2")) {
                                        HashMap<String, Object> uomMap = new HashMap<String, Object>();
                                        uomMap.put("uomname", StringUtil.DecodeText(productUOMName));
                                        uomMap.put("uomtype", StringUtil.DecodeText(productUOMName));
                                        uomMap.put("precision", 0);
                                        uomMap.put("companyid", companyid);
                                        KwlReturnObject uomresult = accUomObj.addUoM(uomMap);
                                        casinguom = (UnitOfMeasure) uomresult.getEntityList().get(0);
                                        casingUoMID = casinguom.getID();
                                    } else {
                                        if (masterPreference.equalsIgnoreCase("1")) {
                                            casingUoMID = "";
                                        } else {
                                            throw new AccountingException("Product Unit Of Measure is not found for " + productUOMName);
                                        }
                                    }
                                }
                            } else {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    casingUoMID = "";
                                } else {
                                    if (productTypeID.equals(Producttype.SERVICE)) {
                                        casingUoMID = "";
                                    } else {
                                        throw new AccountingException("Product  Casing Unit Of Measure is not available");
                                    }
                                }
                            }
                        } else {
                            casingUoMID = "";
                        }

                        String innerUoMID = "";
                        UnitOfMeasure inneruom = null;
                        if (columnConfig.containsKey("inneruom")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("inneruom"));
                            if (cell != null) {
                                String productUOMName = cell.getStringCellValue().trim();
                                inneruom = getUOMByName(productUOMName, companyid);
                                if (inneruom != null) {
                                    innerUoMID = inneruom.getID();
                                } else {
                                    if (masterPreference.equalsIgnoreCase("2")) {
                                        HashMap<String, Object> uomMap = new HashMap<String, Object>();
                                        uomMap.put("uomname", StringUtil.DecodeText(productUOMName));
                                        uomMap.put("uomtype", StringUtil.DecodeText(productUOMName));
                                        uomMap.put("precision", 0);
                                        uomMap.put("companyid", companyid);

                                        KwlReturnObject uomresult = accUomObj.addUoM(uomMap);
                                        inneruom = (UnitOfMeasure) uomresult.getEntityList().get(0);
                                        innerUoMID = inneruom.getID();
                                    } else {
                                        if (masterPreference.equalsIgnoreCase("1")) {
                                            innerUoMID = "";
                                        } else {
                                            throw new AccountingException("Product Inner Unit Of Measure is not found for " + productUOMName);
                                        }
                                    }
                                }
                            } else {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    innerUoMID = "";
                                } else {
                                    if (productTypeID.equals(Producttype.SERVICE)) {
                                        innerUoMID = "";
                                    } else {
                                        throw new AccountingException("Product Inner Unit Of Measure is not available");
                                    }
                                }
                            }
                        } else {
                            innerUoMID = "";
                        }

                        String PurchaseUOMID = "";
                        UnitOfMeasure purchaseruom = null;
                        if (columnConfig.containsKey("purchaseuom")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("purchaseuom"));
                            if (cell != null) {
                                String productUOMName = cell.getStringCellValue().trim();
                                purchaseruom = getUOMByName(productUOMName, companyid);
                                if (purchaseruom != null) {
                                    PurchaseUOMID = purchaseruom.getID();
                                } else {
                                    if (masterPreference.equalsIgnoreCase("2")) {
                                        HashMap<String, Object> uomMap = new HashMap<String, Object>();
                                        uomMap.put("uomname", StringUtil.DecodeText(productUOMName));
                                        uomMap.put("uomtype", StringUtil.DecodeText(productUOMName));
                                        uomMap.put("precision", 0);
                                        uomMap.put("companyid", companyid);

                                        KwlReturnObject uomresult = accUomObj.addUoM(uomMap);
                                        purchaseruom = (UnitOfMeasure) uomresult.getEntityList().get(0);
                                        PurchaseUOMID = purchaseruom.getID();
                                    } else {
                                        if (masterPreference.equalsIgnoreCase("1")) {
                                            PurchaseUOMID = "";
                                        } else {
                                            throw new AccountingException("Product Purchase Unit Of Measure is not found for " + productUOMName);
                                        }
                                    }
                                }
                            } else {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    PurchaseUOMID = "";
                                } else {
                                    if (productTypeID.equals(Producttype.SERVICE)) {
                                        PurchaseUOMID = "";
                                    } else {
                                        throw new AccountingException("Product Purchase Unit Of Measure is not available");
                                    }
                                }
                            }
                        } else {
                            PurchaseUOMID = "";
                        }
                        String SalesUOMID = "";
                        UnitOfMeasure salesuom = null;
                        if (columnConfig.containsKey("salesuom")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("salesuom"));
                            if (cell != null) {
                                String productUOMName = cell.getStringCellValue().trim();
                                salesuom = getUOMByName(productUOMName, companyid);
                                if (salesuom != null) {
                                    SalesUOMID = salesuom.getID();
                                } else {
                                    if (masterPreference.equalsIgnoreCase("2")) {
                                        HashMap<String, Object> uomMap = new HashMap<String, Object>();
                                        uomMap.put("uomname", StringUtil.DecodeText(productUOMName));
                                        uomMap.put("uomtype", StringUtil.DecodeText(productUOMName));
                                        uomMap.put("precision", 0);
                                        uomMap.put("companyid", companyid);

                                        KwlReturnObject uomresult = accUomObj.addUoM(uomMap);
                                        salesuom = (UnitOfMeasure) uomresult.getEntityList().get(0);
                                        SalesUOMID = salesuom.getID();
                                    } else {
                                        if (masterPreference.equalsIgnoreCase("1")) {
                                            SalesUOMID = "";
                                        } else {
                                            throw new AccountingException("Product Sales Unit Of Measure is not found for " + productUOMName);
                                        }
                                    }
                                }
                            } else {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    SalesUOMID = "";
                                } else {
                                    if (productTypeID.equals(Producttype.SERVICE)) {
                                        SalesUOMID = "";
                                    } else {
                                        throw new AccountingException("Product Sales Unit Of Measure is not available");
                                    }
                                }
                            }
                        } else {
                            SalesUOMID = "";
                        }

                        String casinguomvalue = "1";
                        if (columnConfig.containsKey("casinguom_value")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("casinguom_value"));
                            if (cell != null) {
                                casinguomvalue = Double.toString(cell.getNumericCellValue());

                            }
                        }

                        String inneruomvalue = "1";
                        if (columnConfig.containsKey("inneruom_value")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("inneruom_value"));
                            if (cell != null) {
                                inneruomvalue = Double.toString(cell.getNumericCellValue());

                            }
                        }

                        String stcokuomvalue = "1";
                        if (columnConfig.containsKey("stockuom_value")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("stockuom_value"));
                            if (cell != null) {
                                stcokuomvalue = Double.toString(cell.getNumericCellValue());

                            }
                        }

                        String productReorderLevel = "";
                        if (columnConfig.containsKey("reorderlevel")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("reorderlevel"));

                            if (cell == null) {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    productReorderLevel = "0";
                                } else {
                                    if (productTypeID.equals(Producttype.SERVICE)) {
                                        productReorderLevel = "0";
                                    } else {
                                        throw new AccountingException("Product Reorder Level is not available");
                                    }
                                }
                            } else {
                                productReorderLevel = Double.toString(cell.getNumericCellValue());
                            }
                        } else {
                            productReorderLevel = "0";
                        }

                        String productReorderQuantity = "";
                        if (columnConfig.containsKey("reorderquantity")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("reorderquantity"));

                            if (cell == null) {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    productReorderQuantity = "0";
                                } else {
                                    if (productTypeID.equals(Producttype.SERVICE)) {
                                        productReorderQuantity = "0";
                                    } else {
                                        throw new AccountingException("Product Reorder Quantity is not available");
                                    }
                                }
                            } else {
                                productReorderQuantity = Double.toString(cell.getNumericCellValue());
                            }
                        } else {
                            productReorderQuantity = "0";
                        }

                        String productWarrantyPeriod = "";
                        if (columnConfig.containsKey("warrantyperiod")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("warrantyperiod"));

                            if (cell == null) {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    productWarrantyPeriod = "0";
                                } else {
                                    if (productTypeID.equals(Producttype.SERVICE)) {
                                        productWarrantyPeriod = "0";
                                    } else {
                                        throw new AccountingException("Product Warranty Period is not available");
                                    }
                                }
                            } else {
                                productWarrantyPeriod = Integer.toString((int) cell.getNumericCellValue());
                            }
                        } else {
                            productWarrantyPeriod = "0";
                        }

                        String productSalesWarrantyPeriod = "";
                        if (columnConfig.containsKey("warrantyperiodsal")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("warrantyperiodsal"));

                            if (cell == null) {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    productSalesWarrantyPeriod = "0";
                                } else {
                                    if (productTypeID.equals(Producttype.SERVICE)) {
                                        productSalesWarrantyPeriod = "0";
                                    } else {
                                        throw new AccountingException("Product Sales Warranty Period is not available");
                                    }
                                }
                            } else {
                                productSalesWarrantyPeriod = Integer.toString((int) cell.getNumericCellValue());
                            }
                        } else {
                            productSalesWarrantyPeriod = "0";
                        }

                        String productLeadTime = "";
                        if (columnConfig.containsKey("leadtime")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("leadtime"));

                            if (cell == null) {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    productLeadTime = "0";
                                } else {
                                    if (productTypeID.equals(Producttype.SERVICE)) {
                                        productLeadTime = "0";
                                    } else {
                                        throw new AccountingException("Product Lead Time is not available");
                                    }
                                }
                            } else {
                                productLeadTime = Integer.toString((int) cell.getNumericCellValue());
                                if (Integer.parseInt(productLeadTime) > 365) {
                                    throw new AccountingException("Product Lead Time should not be greater than 365");
                                } else if (Integer.parseInt(productLeadTime) < 0) {
                                    throw new AccountingException("Product Lead Time should not be less than 0");
                                }
                            }
                        } else {
                            productLeadTime = "0";
                        }

                        String productCycleCountInterval = "";
                        if (columnConfig.containsKey("ccountinterval")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("ccountinterval"));

                            if (cell == null) {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    productCycleCountInterval = "1";
                                } else {
                                    if (productTypeID.equals(Producttype.SERVICE)) {
                                        productCycleCountInterval = "1";
                                    } else {
                                        throw new AccountingException("Product Cycle Count Interval is not available");
                                    }
                                }
                            } else {
                                productCycleCountInterval = Integer.toString((int) cell.getNumericCellValue());
                            }
                        } else {
                            productCycleCountInterval = "1";
                        }

                        String productCycleCountTolerance = "";
                        if (columnConfig.containsKey("ccounttolerance")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("ccounttolerance"));

                            if (cell == null) {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    productCycleCountTolerance = "0";
                                } else {
                                    if (productTypeID.equals(Producttype.SERVICE)) {
                                        productCycleCountTolerance = "0";
                                    } else {
                                        throw new AccountingException("Product Cycle Count Tolerance is not available");
                                    }
                                }
                            } else {
                                productCycleCountTolerance = Integer.toString((int) cell.getNumericCellValue());
                            }
                        } else {
                            productCycleCountTolerance = "0";
                        }

                        String parentProductUUID = "";
                        if (columnConfig.containsKey("parentid")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("parentid"));

                            if (cell != null) {
                                String parentProductID = cell.getStringCellValue().trim();
                                Product parentProduct = getProductByProductID(parentProductID, companyid);
                                if (parentProduct != null) {
                                    parentProductUUID = parentProduct.getID();
                                } else {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        parentProductUUID = null;
                                    } else {
                                        throw new AccountingException("Parent Product is not found for " + parentProductID);
                                    }
                                }
                            } else {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    parentProductUUID = null;
                                } else {
                                    throw new AccountingException("Parent Product is not available");
                                }
                            }
                        } else {
                            parentProductUUID = null;
                        }

                        String productSalesAccId = "";
                        if (columnConfig.containsKey("salesaccountname")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("salesaccountname"));

                            if (cell != null) {
                                String productSalesAccountName = cell.getStringCellValue().trim();
                                Account salesAccount = getAccountByName(productSalesAccountName, companyid);
                                if (salesAccount != null) {
                                    productSalesAccId = salesAccount.getID();
                                } else {
                                    throw new AccountingException("Product Sales Account is not found for " + productSalesAccountName);
                                }
                            } else {
                                throw new AccountingException("Product Sales Account is not available");
                            }
                        } else {
                            throw new AccountingException("Product Sales Account column is not found.");
                        }

                        String productSalesReturnAccId = "";
                        if (columnConfig.containsKey("salesretaccountname")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("salesretaccountname"));

                            if (cell != null) {
                                String productSalesReturnAccountName = cell.getStringCellValue().trim();
                                Account salesReturnAccount = getAccountByName(productSalesReturnAccountName, companyid);
                                if (salesReturnAccount != null) {
                                    productSalesReturnAccId = salesReturnAccount.getID();
                                } else {
                                    throw new AccountingException("Product Sales Return Account is not found for " + productSalesReturnAccountName);
                                }
                            } else {
                                throw new AccountingException("Product Sales Return Account is not available");
                            }
                        } else {
                            throw new AccountingException("Product Sales Return Account column is not found.");
                        }

                        String productPreferedVendorID = "";
                        if (columnConfig.containsKey("vendornameid")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("vendornameid"));

                            if (cell != null) {
                                String productPreferedVendorName = cell.getStringCellValue().trim();
                                Vendor vendor = getVendorByName(productPreferedVendorName, companyid);
                                if (vendor != null) {
                                    productPreferedVendorID = vendor.getID();
                                } else {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        productPreferedVendorID = null;
                                    } else {
                                        throw new AccountingException("Prefered Vendor is not found for " + productPreferedVendorName);
                                    }
                                }
                            } else {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    productPreferedVendorID = null;
                                } else {
                                    if (productTypeID.equals(Producttype.SERVICE)) {
                                        productPreferedVendorID = null;
                                    } else {
                                        throw new AccountingException("Prefered Vendor is not available");
                                    }
                                }
                            }
                        } else {
                            productPreferedVendorID = null;
                        }

                        String productPurchaseAccId = "";
                        if (columnConfig.containsKey("purchaseaccountname")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("purchaseaccountname"));

                            if (cell != null) {
                                String productPurchaseAccountName = cell.getStringCellValue().trim();
                                Account purchaseAccount = getAccountByName(productPurchaseAccountName, companyid);
                                if (purchaseAccount != null) {
                                    productPurchaseAccId = purchaseAccount.getID();
                                } else {
                                    throw new AccountingException("Product Purchase Account is not found for " + productPurchaseAccountName);
                                }
                            } else {
                                throw new AccountingException("Product Purchase Account is not available");
                            }
                        } else {
                            throw new AccountingException("Product Purchase Account column is not found.");
                        }

                        String productPurchaseReturnAccId = "";
                        if (columnConfig.containsKey("purchaseretaccountname")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("purchaseretaccountname"));

                            if (cell != null) {
                                String productPurchaseReturnAccountName = cell.getStringCellValue().trim();
                                Account purchaseReturnAccount = getAccountByName(productPurchaseReturnAccountName, companyid);
                                if (purchaseReturnAccount != null) {
                                    productPurchaseReturnAccId = purchaseReturnAccount.getID();
                                } else {
                                    throw new AccountingException("Product Purchase Return Account is not found for " + productPurchaseReturnAccountName);
                                }
                            } else {
                                throw new AccountingException("Product Purchase Return Account is not available");
                            }
                        } else {
                            throw new AccountingException("Product Purchase Return Account column is not found.");
                        }

                        String productInitialQuantity = "";
                        if (columnConfig.containsKey("quantity")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("quantity"));

                            if (cell == null) {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    productInitialQuantity = "0";
                                } else {
                                    if (productTypeID.equals(Producttype.SERVICE)) {
                                        productInitialQuantity = "";
                                    } else {
                                        throw new AccountingException("Product Initial Quantity is not available");
                                    }
                                }
                            } else {
                                productInitialQuantity = Double.toString(cell.getNumericCellValue());
                            }
                        } else {
                            productInitialQuantity = "0";
                        }

                        String productInitialPurchasePrise = "";
                        if (columnConfig.containsKey("purchaseprice")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("purchaseprice"));

                            if (cell == null) {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    productInitialPurchasePrise = "";
                                } else {
                                    throw new AccountingException("Product Initial Purchase Price is not available");
                                }
                            } else {
                                productInitialPurchasePrise = Double.toString(cell.getNumericCellValue());
                            }
                        } else {
                            productInitialPurchasePrise = "";
                        }

                        String productSalesPrice = "";
                        if (columnConfig.containsKey("saleprice")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("saleprice"));

                            if (cell == null) {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    productSalesPrice = "";
                                } else {
                                    throw new AccountingException("Product Sales Price is not available");
                                }
                            } else {
                                productSalesPrice = Double.toString(cell.getNumericCellValue());
                            }
                        } else {
                            productSalesPrice = "";
                        }

                        String MsgExep = "";
                        if (columnConfig.containsKey("currencyName")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("currencyName"));

                            if (cell != null) {
                                String productPriceCurrencyStr = cell.getStringCellValue().trim();
                                currencyId = getCurrencyId(productPriceCurrencyStr, currencyMap);
                                if (StringUtil.isNullOrEmpty(currencyId)) {
                                    MsgExep = "Currency format you entered is not correct. it should be like \"SG Dollar (SGD)\""; // messageSource.getMessage("acc.field.ImportProductExceptionFormat", null, RequestContextUtils.getLocale(request));
                                    throw new AccountingException(MsgExep);
                                }
                            } else {
                                if (!masterPreference.equalsIgnoreCase("1")) {
                                    throw new AccountingException("Currency is not available.");
                                }
                            }
                        }

                        UOMschemaType uomSchemaType = null;
                        if (columnConfig.containsKey("uomSchemaTypeName")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("uomSchemaTypeName"));
                            if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
                                String uomSchemaTypeName = cell.getStringCellValue().trim();
                                uomSchemaType = getUOMschemaTypeByName(uomSchemaTypeName, companyid);
                                if (uomSchemaType == null) {
                                    if (!masterPreference.equalsIgnoreCase("1")) {
                                        throw new AccountingException("UOM Schema is not found for " + uomSchemaTypeName);
                                    }
                                }
                            }
                        }

                        if (uomSchemaType != null && !productUOMID.equalsIgnoreCase(uomSchemaType.getStockuom().getID())) {
                            throw new AccountingException("Stock UOM of Product and UOM Schema's Stock UOM should be same.");
                        }

                        String productDefaultLocationID = "";
                        if (columnConfig.containsKey("locationName")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("locationName"));

                            if (cell != null) {
                                String productDefaultLocationName = cell.getStringCellValue().trim();
                                InventoryLocation invLoc = getInventoryLocationByName(productDefaultLocationName, companyid);
                                if (invLoc != null) {
                                    productDefaultLocationID = invLoc.getId();
                                } else {
                                    if (masterPreference.equalsIgnoreCase("2")) {
                                        HashMap requestParam = requestParams1; //AccountingManager.getGlobalParams(request);
                                        requestParam.put("id", "");
                                        requestParam.put("name", productDefaultLocationName);
                                        requestParam.put("parent", null);
                                        KwlReturnObject jeresult = accountingHandlerDAOobj.getObject(User.class.getName(), userId);
                                        User user = (User) jeresult.getEntityList().get(0);
                                        requestParam.put("user", user);
                                        KwlReturnObject locationResult = accMasterItemsDAOobj.addLocationItem(requestParam);
                                        invLoc = (InventoryLocation) locationResult.getEntityList().get(0);
                                        productDefaultLocationID = invLoc.getId();
                                    } else {
                                        if (masterPreference.equalsIgnoreCase("1")) {
                                            productDefaultLocationID = null;
                                        } else {
                                            throw new AccountingException("Default Location is not found for " + productDefaultLocationName);
                                        }
                                    }
                                }
                            } else {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    productDefaultLocationID = null;
                                } else {
                                    throw new AccountingException("Default Location is not available.");
                                }
                            }
                        } else {
                            productDefaultLocationID = null;
                        }

                        String productDefaultWarehouseID = "";
                        if (columnConfig.containsKey("warehouseName")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("warehouseName"));

                            if (cell != null) {
                                String productDefaultWarehouseName = cell.getStringCellValue().trim();
                                InventoryWarehouse invWHouse = getInventoryWarehouseByName(productDefaultWarehouseName, companyid);
                                if (invWHouse != null) {
                                    productDefaultWarehouseID = invWHouse.getId();
                                } else {
                                    if (masterPreference.equalsIgnoreCase("2")) {
                                        HashMap requestParam = requestParams1;// AccountingManager.getGlobalParams(request);
                                        requestParam.put("id", "");
                                        requestParam.put("name", productDefaultWarehouseName);
                                        requestParam.put("parent", null);
                                        requestParam.put("location", null);

                                        KwlReturnObject warehouseResult = accMasterItemsDAOobj.addWarehouseItem(requestParam);
                                        invWHouse = (InventoryWarehouse) warehouseResult.getEntityList().get(0);
                                        productDefaultWarehouseID = invWHouse.getId();
                                    } else {
                                        if (masterPreference.equalsIgnoreCase("1")) {
                                            productDefaultWarehouseID = null;
                                        } else {
                                            throw new AccountingException("Default Warehouse is not found for " + productDefaultWarehouseName);
                                        }
                                    }
                                }
                            } else {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    productDefaultWarehouseID = null;
                                } else {
                                    throw new AccountingException("Default Warehouse is not available.");
                                }
                            }
                        } else {
                            productDefaultWarehouseID = null;
                        }

                        Boolean isSyncable = false;
                        if (columnConfig.containsKey("syncable")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("syncable"));

                            if (cell != null) {
                                String productMakeAvailableInOtherApp = cell.getStringCellValue().trim();
                                if (productMakeAvailableInOtherApp.equalsIgnoreCase("T")) {
                                    isSyncable = true;
                                } else if (productMakeAvailableInOtherApp.equalsIgnoreCase("F")) {
                                    isSyncable = false;
                                } else {
                                    throw new AccountingException("Format you entered is not correct. It should be like \"T\" or \"F\"");
                                }
                            } else {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    isSyncable = false;
                                } else {
                                    throw new AccountingException("Make available in other application is not available.");
                                }
                            }
                        }

                        Boolean isMultiUOM = false;
                        if (columnConfig.containsKey("multiuom")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("multiuom"));

                            if (cell != null) {
                                String multipleUOM = cell.getStringCellValue().trim();
                                if (multipleUOM.equalsIgnoreCase("T")) {
                                    isMultiUOM = true;
                                } else if (multipleUOM.equalsIgnoreCase("F")) {
                                    isMultiUOM = false;
                                } else {
                                    throw new AccountingException("Format you entered is not correct. It should be like \"T\" or \"F\"");
                                }
                            } else {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    isMultiUOM = false;
                                } else {
                                    if (productTypeID.equals(Producttype.SERVICE)) {
                                        isMultiUOM = false;
                                    } else {
                                        throw new AccountingException("Multiple UOM is not available.");
                                    }
                                }
                            }
                        }
                        Boolean isIslocationforproduct = false;
                        if (columnConfig.containsKey(Constants.ACTIVATE_LOCATION)) {
                            Cell cell = row.getCell((Integer) columnConfig.get(Constants.ACTIVATE_LOCATION));
                            if (cell != null) {
                                String ISlocationforproduct = cell.getStringCellValue().trim();
                                if (ISlocationforproduct.equalsIgnoreCase("T")) {
                                    isIslocationforproduct = true;
                                } else if (ISlocationforproduct.equalsIgnoreCase("F")) {
                                    isIslocationforproduct = false;
                                } else {
                                    throw new AccountingException("Format you entered is not correct. It should be like \"T\" or \"F\"");
                                }
                            }
                        }
                        Boolean isIsSerialForProduct = false;
                        if (columnConfig.containsKey(Constants.ACTIVATE_SERIAL_NO)) {
                            Cell cell = row.getCell((Integer) columnConfig.get(Constants.ACTIVATE_SERIAL_NO));
                            if (cell != null) {
                                String IsSerialForProduct = cell.getStringCellValue().trim();
                                if (IsSerialForProduct.equalsIgnoreCase("T")) {
                                    isIsSerialForProduct = true;
                                } else if (IsSerialForProduct.equalsIgnoreCase("F")) {
                                    isIsSerialForProduct = false;
                                } else {
                                    throw new AccountingException("Format you entered is not correct. It should be like \"T\" or \"F\"");
                                }
                            }
                        }
                        Boolean isIswarehouseforproduct = false;
                        if (columnConfig.containsKey(Constants.ACTIVATE_WAREHOUSE)) {
                            Cell cell = row.getCell((Integer) columnConfig.get(Constants.ACTIVATE_WAREHOUSE));
                            if (cell != null) {
                                String Iswarehouseforproduct = cell.getStringCellValue().trim();
                                if (Iswarehouseforproduct.equalsIgnoreCase("T")) {
                                    isIswarehouseforproduct = true;
                                } else if (Iswarehouseforproduct.equalsIgnoreCase("F")) {
                                    isIswarehouseforproduct = false;
                                } else {
                                    throw new AccountingException("Format you entered is not correct. It should be like \"T\" or \"F\"");
                                }
                            }
                        }
                        Boolean isIsBatchForProduct = false;
                        if (columnConfig.containsKey(Constants.ACTIVATE_BATCH)) {
                            Cell cell = row.getCell((Integer) columnConfig.get(Constants.ACTIVATE_BATCH));
                            if (cell != null) {
                                String IsBatchForProduct = cell.getStringCellValue().trim();
                                if (IsBatchForProduct.equalsIgnoreCase("T")) {
                                    isIsBatchForProduct = true;
                                } else if (IsBatchForProduct.equalsIgnoreCase("F")) {
                                    isIsBatchForProduct = false;
                                } else {
                                    throw new AccountingException("Format you entered is not correct. It should be like \"T\" or \"F\"");
                                }
                            }
                        }

                        String wipoffset = "";
                        if (columnConfig.containsKey("wipoffset")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("wipoffset"));
                            if (cell != null) {
                                wipoffset = cell.getStringCellValue().trim();
                            }
                        }

                        String inventoryoffset = "";
                        if (columnConfig.containsKey("inventoryoffset")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("inventoryoffset"));
                            if (cell != null) {
                                inventoryoffset = cell.getStringCellValue().trim();
                            }
                        }

                        String hscode = "";
                        if (columnConfig.containsKey("hscode")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("hscode"));
                            if (cell != null) {
                                hscode = cell.getStringCellValue().trim();
                            }
                        }
                        String additionalfreetext = "";
                        if (columnConfig.containsKey("additionalfreetext")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("additionalfreetext"));
                            if (cell != null) {
                                additionalfreetext = cell.getStringCellValue().trim();
                            }
                        }
                        String itemcolor = "";
                        if (columnConfig.containsKey("itemcolor")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("itemcolor"));
                            if (cell != null) {
                                itemcolor = cell.getStringCellValue().trim();
                            }
                        }
                        String alternateproduct = "";
                        if (columnConfig.containsKey("alternateproduct")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("alternateproduct"));
                            if (cell != null) {
                                alternateproduct = cell.getStringCellValue().trim();
                            }
                        }
                        String purchasemfg = "";
                        if (columnConfig.containsKey("purchasemfg")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("purchasemfg"));
                            if (cell != null) {
                                purchasemfg = cell.getStringCellValue().trim();
                            }
                        }
                        String catalogno = "";
                        if (columnConfig.containsKey("catalogno")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("catalogno"));
                            if (cell != null) {
                                catalogno = cell.getStringCellValue().trim();
                            }
                        }
                        String barcode = "";
                        if (columnConfig.containsKey("barcode")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("barcode"));
                            if (cell != null) {
                                barcode = cell.getStringCellValue().trim();
                            }
                        }
                        String additionaldesc = "";
                        if (columnConfig.containsKey("additionaldesc")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("additionaldesc"));
                            if (cell != null) {
                                additionaldesc = cell.getStringCellValue().trim();
                            }
                        }
                        String descinforeign = "";
                        if (columnConfig.containsKey("descinforeign")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("descinforeign"));
                            if (cell != null) {
                                descinforeign = cell.getStringCellValue().trim();
                            }
                        }
                        String licensecode = "";
                        if (columnConfig.containsKey("licensecode")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("licensecode"));
                            if (cell != null) {
                                licensecode = cell.getStringCellValue().trim();
                            }
                        }
                        String itemgroup = "";
                        if (columnConfig.containsKey("itemgroup")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("itemgroup"));
                            if (cell != null) {
                                itemgroup = cell.getStringCellValue().trim();
                            }
                        }
                        String pricelist = "";
                        if (columnConfig.containsKey("pricelist")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("pricelist"));
                            if (cell != null) {
                                pricelist = cell.getStringCellValue().trim();
                            }
                        }
                        String shippingtype = "";
                        if (columnConfig.containsKey("shippingtype")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("shippingtype"));
                            if (cell != null) {
                                shippingtype = cell.getStringCellValue().trim();
                            }
                        }
                        Boolean recyclable = false;
                        if (columnConfig.containsKey("recyclable")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("recyclable"));
                            if (cell != null) {
                                String isRecyclable = cell.getStringCellValue().trim();
                                if (isRecyclable.equalsIgnoreCase("T")) {
                                    recyclable = true;
                                } else if (isRecyclable.equalsIgnoreCase("F")) {
                                    recyclable = false;
                                } else {
                                    throw new AccountingException("Format you entered is not correct. It should be like \"T\" or \"F\"");
                                }
                            }
                        }
                        Boolean qaenable = false;
                        if (columnConfig.containsKey("qaenable")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("qaenable"));
                            if (cell != null) {
                                String isQaenable = cell.getStringCellValue().trim();
                                if (isQaenable.equalsIgnoreCase("T")) {
                                    qaenable = true;
                                } else if (isQaenable.equalsIgnoreCase("F")) {
                                    qaenable = false;
                                } else {
                                    throw new AccountingException("Format you entered is not correct. It should be like \"T\" or \"F\"");
                                }
                            }
                        }
                        Boolean isknittingitem = false;
                        if (columnConfig.containsKey("isknittingitem")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("isknittingitem"));
                            if (cell != null) {
                                String isIsknittingitem = cell.getStringCellValue().trim();
                                if (isIsknittingitem.equalsIgnoreCase("T")) {
                                    isknittingitem = true;
                                } else if (isIsknittingitem.equalsIgnoreCase("F")) {
                                    isknittingitem = false;
                                } else {
                                    throw new AccountingException("Format you entered is not correct. It should be like \"T\" or \"F\"");
                                }
                            }
                        }
                        Boolean isactive = false;
                        if (columnConfig.containsKey("isactive")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("isactive"));
                            if (cell != null) {
                                String isIsactive = cell.getStringCellValue().trim();
                                if (isIsactive.equalsIgnoreCase("T")) {
                                    isactive = true;
                                } else if (isIsactive.equalsIgnoreCase("F")) {
                                    isactive = false;
                                } else {
                                    throw new AccountingException("Format you entered is not correct. It should be like \"T\" or \"F\"");
                                }
                            }
                        }
                        Boolean blockloosesell = false;
                        if (columnConfig.containsKey("blockloosesell")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("blockloosesell"));
                            if (cell != null) {
                                String isBlockloosesell = cell.getStringCellValue().trim();
                                if (isBlockloosesell.equalsIgnoreCase("T")) {
                                    blockloosesell = true;
                                } else if (isBlockloosesell.equalsIgnoreCase("F")) {
                                    blockloosesell = false;
                                } else {
                                    throw new AccountingException("Format you entered is not correct. It should be like \"T\" or \"F\"");
                                }
                            }
                        }
                        String itemsalesvolume = "";
                        if (columnConfig.containsKey("itemsalesvolume")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("itemsalesvolume"));
                            if (cell != null) {
                                itemsalesvolume = Double.toString(cell.getNumericCellValue());

                            }
                        }
                        String productweight = "0";
                        if (columnConfig.containsKey("productweight")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("productweight"));
                            if (cell != null) {
                                productweight = Double.toString(cell.getNumericCellValue());

                            }
                        }
                        String itemsaleswidth = "";
                        if (columnConfig.containsKey("itemsaleswidth")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("itemsaleswidth"));
                            if (cell != null) {
                                itemsaleswidth = Double.toString(cell.getNumericCellValue());

                            }
                        }
                        String itemsalesheight = "";
                        if (columnConfig.containsKey("itemsalesheight")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("itemsalesheight"));
                            if (cell != null) {
                                itemsalesheight = Double.toString(cell.getNumericCellValue());

                            }
                        }
                        String itemwidth = "";
                        if (columnConfig.containsKey("itemwidth")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("itemwidth"));
                            if (cell != null) {
                                itemwidth = Double.toString(cell.getNumericCellValue());

                            }
                        }
                        String itemvolume = "";
                        if (columnConfig.containsKey("itemvolume")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("itemvolume"));
                            if (cell != null) {
                                itemvolume = Double.toString(cell.getNumericCellValue());

                            }
                        }
                        String itempurchasewidth = "";
                        if (columnConfig.containsKey("itempurchasewidth")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("itempurchasewidth"));
                            if (cell != null) {
                                itempurchasewidth = Double.toString(cell.getNumericCellValue());

                            }
                        }
                        String itempurchasevolume = "";
                        if (columnConfig.containsKey("itempurchasevolume")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("itempurchasevolume"));
                            if (cell != null) {
                                itempurchasevolume = Double.toString(cell.getNumericCellValue());

                            }
                        }
                        String itempurchaselength = "0";
                        if (columnConfig.containsKey("itempurchaselength")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("itempurchaselength"));
                            if (cell != null) {
                                itempurchaselength = Double.toString(cell.getNumericCellValue());

                            }
                        }
                        String qaleadtimeindays = "0";
                        if (columnConfig.containsKey("itempurchaselength")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("itempurchaselength"));
                            if (cell != null) {
                                qaleadtimeindays = Double.toString(cell.getNumericCellValue());

                            }
                        }
                        String reusabilitycount = "";
                        if (columnConfig.containsKey("itempurchaselength")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("itempurchaselength"));
                            if (cell != null) {
                                reusabilitycount = Double.toString(cell.getNumericCellValue());

                            }
                        }

                        String orderinguom = "";
                        UnitOfMeasure orderuom = null;
                        if (columnConfig.containsKey("orderinguom")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("orderinguom"));
                            if (cell != null) {
                                String productUOMName = cell.getStringCellValue().trim();
                                if (!StringUtil.isNullOrEmpty(productUOMName)) {
                                    orderuom = getUOMByName(productUOMName, companyid);
                                    if (orderuom != null) {
                                        orderinguom = salesuom.getID();
                                    } else {
                                        if (masterPreference.equalsIgnoreCase("2")) {
                                            HashMap<String, Object> uomMap = new HashMap<String, Object>();
                                            uomMap.put("uomname", StringUtil.DecodeText(productUOMName));
                                            uomMap.put("uomtype", StringUtil.DecodeText(productUOMName));
                                            uomMap.put("precision", 0);
                                            uomMap.put("companyid", companyid);

                                            KwlReturnObject uomresult = accUomObj.addUoM(uomMap);
                                            orderuom = (UnitOfMeasure) uomresult.getEntityList().get(0);
                                            orderinguom = orderuom.getID();
                                        } else {
                                            if (masterPreference.equalsIgnoreCase("1")) {
                                                orderinguom = "";
                                            } else {
                                                throw new AccountingException("Product Sales Unit Of Measure is not found for " + productUOMName);
                                            }
                                        }
                                    }
                                } else {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        orderinguom = "";
                                    } else {
                                        if (productTypeID.equals(Producttype.SERVICE)) {
                                            orderinguom = "";
                                        } else {
                                            throw new AccountingException("Product Ordering Unit Of Measure is not available");
                                        }
                                    }
                                }
                            }
                        } else {
                            orderinguom = "";
                        }
                        String transferuom = "";
                        UnitOfMeasure transferUOM = null;
                        if (columnConfig.containsKey("transferuom")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("transferuom"));
                            if (cell != null) {
                                String productUOMName = cell.getStringCellValue().trim();
                                if (!StringUtil.isNullOrEmpty(productUOMName)) {
                                    transferUOM = getUOMByName(productUOMName, companyid);
                                    if (transferUOM != null) {
                                        transferuom = salesuom.getID();
                                    } else {
                                        if (masterPreference.equalsIgnoreCase("2")) {
                                            HashMap<String, Object> uomMap = new HashMap<String, Object>();
                                            uomMap.put("uomname", StringUtil.DecodeText(productUOMName));
                                            uomMap.put("uomtype", StringUtil.DecodeText(productUOMName));
                                            uomMap.put("precision", 0);
                                            uomMap.put("companyid", companyid);

                                            KwlReturnObject uomresult = accUomObj.addUoM(uomMap);
                                            transferUOM = (UnitOfMeasure) uomresult.getEntityList().get(0);
                                            transferuom = orderuom.getID();
                                        } else {
                                            if (masterPreference.equalsIgnoreCase("1")) {
                                                transferuom = "";
                                            } else {
                                                throw new AccountingException("Product Sales Unit Of Measure is not found for " + productUOMName);
                                            }
                                        }
                                    }
                                } else {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        transferuom = "";
                                    } else {
                                        if (productTypeID.equals(Producttype.SERVICE)) {
                                            transferuom = "";
                                        } else {
                                            throw new AccountingException("Product Ordering Unit Of Measure is not available");
                                        }
                                    }
                                }
                            }
                        } else {
                            transferuom = "";
                        }
                        
                        Set<Frequency> ccFrequencies = new HashSet();
                        if (!productTypeID.equals(Producttype.SERVICE) && !productTypeID.equals(Producttype.SERVICE_Name) && columnConfig.containsKey("CCFrequency")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("CCFrequency"));
                            if (cell != null) {
                                String frequencies = cell.getStringCellValue().trim();
                                if (!StringUtil.isNullOrEmpty(frequencies)) {
                                    String[] frqs = frequencies.split(",");
                                    String notFoundNames = "";
                                    for (int c = 0; c < frqs.length; c++) {
                                        Frequency frq = null;
                                        if ("Daily".equalsIgnoreCase(frqs[c])) {
                                            frq = frequencyMap.get(Frequency.DAILY);
                                        } else if ("Weekly".equalsIgnoreCase(frqs[c])) {
                                            frq = frequencyMap.get(Frequency.WEEKLY);
                                        } else if ("Fortnightly".equalsIgnoreCase(frqs[c])) {
                                            frq = frequencyMap.get(Frequency.FORTNIGHT);
                                        } else if ("Monthly".equalsIgnoreCase(frqs[c])) {
                                            frq = frequencyMap.get(Frequency.MONTHLY);
                                        } else {
                                            notFoundNames += frqs[c] + ", ";
                                        }
                                        if (frq != null) {
                                            ccFrequencies.add(frq);
                                        }
                                    }
                                    if (!StringUtil.isNullOrEmpty(notFoundNames) && masterPreference.equalsIgnoreCase("0")) {
                                        notFoundNames = notFoundNames.substring(0, notFoundNames.lastIndexOf(","));
                                        throw new AccountingException("Cycle Count Frequency is not found for " + notFoundNames);
                                    }
                                }
                            }
                        }
                        // For Checking 'ProductID' is exist or not
                        KwlReturnObject result = accProductObj.getProductIDCount(productID, companyid,false);
                        int nocount = result.getRecordTotalCount();
                        if (nocount > 0 && !updateExistingRecordFlag) {
                            throw new AccountingException("Product ID '" + productID + "' already exists.");
                        } else if (nocount == 0 && updateExistingRecordFlag) {
                            throw new AccountingException("Product ID '" + productID + "' not exists.");
                        }

                        // creating product Hashmap
                        HashMap<String, Object> productMap = new HashMap<String, Object>();

                        boolean isUsedInTransaction = false;
                        if (updateExistingRecordFlag) {
                            Product product = (Product) result.getEntityList().get(0);
                            productMap.put("id", product.getID());
                            List listObj = null;//accProductModuleService.isProductUsedintransction(product.getID(), companyid, request);
                            isUsedInTransaction = (Boolean) listObj.get(0);    //always boolean value
                        } else {
                            productMap.put("producttype", productTypeID);
                        }

                        productMap.put("name", productName);
                        productMap.put("productid", productID);
                        productMap.put("desc", productDescription);
                        productMap.put("syncable", false);
                        productMap.put("multiuom", false);
                        productMap.put("WIPoffset", wipoffset);
                        productMap.put("Inventoryoffset", inventoryoffset);
                        productMap.put("hsCode", hscode);
                        productMap.put("additionalfreetext", additionalfreetext);
                        productMap.put("itemcolor", itemcolor);
                        productMap.put("alternateproductid", alternateproduct);
                        productMap.put("purchasemfg", purchasemfg);
                        productMap.put("catalogNo", catalogno);
                        productMap.put("barcode", barcode);
                        productMap.put("additionaldescription", additionaldesc);
                        productMap.put("foreigndescription", descinforeign);
                        productMap.put("licensecode", licensecode);
                        productMap.put("itemgroup", itemgroup);
                        productMap.put("itempricelist", pricelist);
                        productMap.put("shippingtype", shippingtype);
                        productMap.put("isrecyclable", recyclable);
                        productMap.put("isQAenable", qaenable);
                        productMap.put("isKnittingItem", isknittingitem);
                        productMap.put("isActiveItem", isactive);
                        productMap.put("blockLooseSell", blockloosesell);
                        if (!StringUtil.isNullOrEmpty(itemsalesvolume)) {
                            productMap.put("itemsalesvolume", Double.parseDouble(itemsalesvolume));
                        }
                        if (!StringUtil.isNullOrEmpty(productweight)) {
                            productMap.put("productweight", Double.parseDouble(productweight));
                        }
                        if (!StringUtil.isNullOrEmpty(itemsaleswidth)) {
                            productMap.put("itemsaleswidth", Double.parseDouble(itemsaleswidth));
                        }
                        if (!StringUtil.isNullOrEmpty(itemsalesheight)) {
                            productMap.put("itemsalesheight", Double.parseDouble(itemsalesheight));
                        }
                        if (!StringUtil.isNullOrEmpty(itemwidth)) {
                            productMap.put("itemwidth", Double.parseDouble(itemwidth));
                        }
                        if (!StringUtil.isNullOrEmpty(itemvolume)) {
                            productMap.put("itemvolume", Double.parseDouble(itemvolume));
                        }
                        if (!StringUtil.isNullOrEmpty(itempurchasewidth)) {
                            productMap.put("itempurchasewidth", Double.parseDouble(itempurchasewidth));
                        }
                        if (!StringUtil.isNullOrEmpty(itempurchasevolume)) {
                            productMap.put("itempurchasevolume", Double.parseDouble(itempurchasevolume));
                        }
                        if (!StringUtil.isNullOrEmpty(itempurchaselength)) {
                            productMap.put("itempurchaselength", Double.parseDouble(itempurchaselength));
                        }
                        if (!StringUtil.isNullOrEmpty(qaleadtimeindays)) {
                            productMap.put("QAleadtime", Integer.parseInt(qaleadtimeindays));
                        }
                        if (!StringUtil.isNullOrEmpty(reusabilitycount)) {
                            productMap.put("reusabilitycount", Integer.parseInt(reusabilitycount));
                        }
                        if (!StringUtil.isNullOrEmpty(transferuom)) {
                            productMap.put("transferUoM", transferuom);
                        }
                        if (!StringUtil.isNullOrEmpty(orderinguom)) {
                            productMap.put("orderUoM", orderinguom);
                        }
                        if (!ccFrequencies.isEmpty()) {
                            productMap.put("CCFrequency", ccFrequencies);
                            productMap.put("countable", true); 
                        }else{
                            productMap.put("countable", false); 
                        }
                        productMap.put("currencyid", currencyId);

                        if (!isUsedInTransaction) {
                            productMap.put("isBatchForProduct", isIsBatchForProduct);
                            productMap.put("isSerialForProduct", isIsSerialForProduct);
                            productMap.put("isLocationForProduct", isIslocationforproduct);
                            productMap.put("isWarehouseForProduct", isIswarehouseforproduct);
                        }

                        productMap.put("uomid", productUOMID);
                        if (!productTypeID.equals(Producttype.SERVICE)) {
                            if (uom != null) {
                                productMap.put("transferUoM", uom);
                                productMap.put("orderUoM", uom);
                            }
                            if (salesuom != null) {
                                productMap.put("salesuomid", salesuom);
                            } else if (salesuom == null && uom != null) {
                                productMap.put("salesuomid", uom);
                            }
                            if (purchaseruom != null) {
                                productMap.put("purchaseuomid", purchaseruom);
                            } else if (purchaseruom == null && uom != null) {
                                productMap.put("purchaseuomid", uom);
                            }

                            KwlReturnObject companyObj = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                            Company company = (Company) companyObj.getEntityList().get(0);
                            Packaging packaging = null;
                            if (inneruom != null || casinguom != null) {
                                packaging = new Packaging();
                                packaging.setCasingUoM(casinguom);
                                packaging.setInnerUoM(inneruom);
                                packaging.setStockUoM(uom);
                                packaging.setCasingUomValue(Double.parseDouble(casinguomvalue));
                                packaging.setInnerUomValue(Double.parseDouble(inneruomvalue));
                                packaging.setStockUomValue(Double.parseDouble(stcokuomvalue));
                                packaging.setCompany(company);

                            }
                            if (packaging != null) {
                                accProductObj.saveProductPackging(packaging);
                                productMap.put("packaging", packaging);
                            }
                        }

                        productMap.put("reorderlevel", Double.parseDouble(productReorderLevel));
                        productMap.put("reorderquantity", Double.parseDouble(productReorderQuantity));
                        productMap.put("warrantyperiod", Integer.parseInt(productWarrantyPeriod));
                        productMap.put("warrantyperiodsal", Integer.parseInt(productSalesWarrantyPeriod));
                        productMap.put("leadtime", Integer.parseInt(productLeadTime));

                        productMap.put("parentid", parentProductUUID);
                        productMap.put("salesaccountid", productSalesAccId);
                        productMap.put("purchaseretaccountid", productPurchaseReturnAccId);
                        productMap.put("salesretaccountid", productSalesReturnAccId);
                        productMap.put("vendorid", productPreferedVendorID);
                        productMap.put("purchaseaccountid", productPurchaseAccId);
                        productMap.put("purchaseretaccountid", productPurchaseReturnAccId);

                        // if product have multiuom = 'T' and Account Preferences have UOM Setting for UOM Schema then set uomschemaType for product
                        if (uomSchemaType != null && isMultiUOM && extraCompanyPreferences != null && extraCompanyPreferences.getUomSchemaType() == 0) {
                            productMap.put("uomschemaType", uomSchemaType);
                        }

                        if (!StringUtil.isNullOrEmpty(productDefaultLocationID)) {
                            productMap.put("location", productDefaultLocationID);
                        }

                        if (!StringUtil.isNullOrEmpty(productDefaultWarehouseID)) {
                            productMap.put("warehouse", productDefaultWarehouseID);
                        }

                        productMap.put("syncable", isSyncable);
                        productMap.put("multiuom", isMultiUOM);
                        productMap.put("deletedflag", false);
                        productMap.put("companyid", companyid);
                        productMap.put("vendorid", null);
                        productMap.put("isImport", 1);

                        KwlReturnObject productresult = null;
                        if (!updateExistingRecordFlag) {
                            productresult = accProductObj.addProduct(productMap);
                        } else {
                            productresult = accProductObj.updateProduct(productMap);
                        }
                        Product product = (Product) productresult.getEntityList().get(0);

//                        if (!productTypeID.equals(Producttype.SERVICE)) {
//                            // creating Price list Hashmap
//                            HashMap<String, Object> cycleParams = new HashMap<String, Object>();
//                            cycleParams.put("productid", product.getID());
//                            cycleParams.put("interval", Integer.parseInt(productCycleCountInterval));
//                            cycleParams.put("tolerance", Integer.parseInt(productCycleCountTolerance));
//                            accProductObj.saveProductCycleCount(cycleParams);
//                        }
                        if (productInitialQuantity.length() > 0) { //&& !updateExistingRecordFlag
                            JSONObject inventoryjson = new JSONObject();
                            inventoryjson.put("productid", product.getID());
                            inventoryjson.put("quantity", Double.parseDouble(productInitialQuantity));
                            if (!isUsedInTransaction) {
                                inventoryjson.put("baseuomquantity", Double.parseDouble(productInitialQuantity));
                            }
                            inventoryjson.put("baseuomrate", 1);
                            if (product.getUnitOfMeasure() != null) {
                                inventoryjson.put("uomid", product.getUnitOfMeasure().getID());
                            }
                            inventoryjson.put("description", "Inventory Opened");
                            inventoryjson.put("carryin", true);
                            inventoryjson.put("defective", false);
                            inventoryjson.put("newinventory", true);
                            inventoryjson.put("companyid", companyid);
                            inventoryjson.put("updatedate", newUserDate);
                            if (!updateExistingRecordFlag) {
                                accProductObj.addInventory(inventoryjson);
                            } else {
                                accProductObj.updateInitialInventory(inventoryjson);
                            }

                            HashMap<String, Object> assemblyParams = requestParams1;
                            assemblyParams.put("assembly", "");
                            assemblyParams.put("applydate", authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
                            assemblyParams.put("quantity", Double.parseDouble(productInitialQuantity));
                            assemblyParams.put("memo", "Inventory Opened");
                            assemblyParams.put("refno", "");
                            assemblyParams.put("buildproductid", product.getID());
                            accProductObj.updateAssemblyInventory(assemblyParams);
                        }
                        Date ondate = new Date();
                        if (productInitialPurchasePrise.length() > 0 && !isUsedInTransaction) {
                            // creating Price list Hashmap
                            HashMap<String, Object> initialPurchasePriceMap = new HashMap<String, Object>();
                            initialPurchasePriceMap.put("productid", product.getID());
                            initialPurchasePriceMap.put("companyid", companyid);
                            initialPurchasePriceMap.put("carryin", true);
                            initialPurchasePriceMap.put("price", Double.parseDouble(productInitialPurchasePrise));
                            initialPurchasePriceMap.put("applydate", authHandler.getConstantDateFormatter().parse(authHandler.getConstantDateFormatter().format(ondate)));
                            initialPurchasePriceMap.put("affecteduser", "-1");
                            initialPurchasePriceMap.put("currencyid", currencyId);
                            initialPurchasePriceMap.put("uomid", product.getUnitOfMeasure().getID());
                            accProductObj.addPriceList(initialPurchasePriceMap);
                        }

                        if (productSalesPrice.length() > 0 && !isUsedInTransaction) {
                            // creating Price list Hashmap
                            HashMap<String, Object> salesPriceMap = new HashMap<String, Object>();
                            salesPriceMap.put("productid", product.getID());
                            salesPriceMap.put("companyid", companyid);
                            salesPriceMap.put("carryin", false);
                            salesPriceMap.put("price", Double.parseDouble(productSalesPrice));
                            salesPriceMap.put("applydate", authHandler.getConstantDateFormatter().parse(authHandler.getConstantDateFormatter().format(ondate)));
                            salesPriceMap.put("affecteduser", "-1");
                            salesPriceMap.put("currencyid", currencyId);
                            salesPriceMap.put("uomid", product.getUnitOfMeasure().getID());
                            accProductObj.addPriceList(salesPriceMap);
                        }
                        // Custom Column Implementation
                        customfield = "";
                        JSONArray customJArr = new JSONArray();
                        for (int j = 0; j < jSONArray.length(); j++) {
                            JSONObject jSONObject = jSONArray.getJSONObject(j);
                            if (jSONObject.optBoolean("customflag", false)) { // customflag=true : Custom Field
                                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                                requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.fieldlabel));
                                requestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, jSONObject.getString("columnname")));
                                KwlReturnObject fieldParamsResult = accCommonTablesDAO.getFieldParams(requestParams); // get custom field for module
                                FieldParams params = (FieldParams) fieldParamsResult.getEntityList().get(0);
                                if (recarr.get(jSONObject.getInt("csvindex")) != null && !StringUtil.isNullOrEmpty(recarr.get(jSONObject.getInt("csvindex")).toString().replaceAll("\"", "").trim())) {
                                    JSONObject customJObj = new JSONObject();
                                    customJObj.put("fieldid", params.getId());
                                    customJObj.put("refcolumn_name", "Col" + params.getRefcolnum());
                                    customJObj.put("fieldname", "Custom_" + params.getFieldlabel());
                                    customJObj.put("xtype", params.getFieldtype());
                                    String fieldComboDataStr = "";
                                    if (params.getFieldtype() == 3) { // if field of date typed
                                        Cell cell = row.getCell(jSONObject.getInt("csvindex"));
                                        if (cell != null) {
                                            customJObj.put("Col" + params.getColnum(), cell.getDateCellValue().getTime());
                                            customJObj.put("fieldDataVal", cell.getDateCellValue().getTime());
                                        }
                                    } else if (params.getFieldtype() == 4 || params.getFieldtype() == 7) { // if field of MultiSelect Drop Down OR Drop Down
                                        String[] fieldComboDataArr = recarr.get(jSONObject.getInt("csvindex")).toString().replaceAll("\"", "").trim().split(";");
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
                                        customJObj.put("Col" + params.getColnum(), Boolean.parseBoolean(recarr.get(jSONObject.getInt("csvindex")).toString().replaceAll("\"", "").trim()));
                                        customJObj.put("fieldDataVal", Boolean.parseBoolean(recarr.get(jSONObject.getInt("csvindex")).toString().replaceAll("\"", "").trim()));
                                    } else if (params.getFieldtype() == 12) { // if field of check list type
                                        requestParams = new HashMap<String, Object>();
                                        requestParams.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "deleteflag"));
                                        requestParams.put(Constants.filter_values, Arrays.asList(params.getId(), 0));
                                        fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParams);
                                        List<FieldComboData> fieldComboDataList = fieldParamsResult.getEntityList();
                                        String[] fieldComboDataArr = recarr.get(jSONObject.getInt("csvindex")).toString().replaceAll("\"", "").trim().split(";");
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
                                        customJObj.put("Col" + params.getColnum(), recarr.get(jSONObject.getInt("csvindex")).toString().replaceAll("\"", "").trim());
                                        customJObj.put("fieldDataVal", recarr.get(jSONObject.getInt("csvindex")).toString().replaceAll("\"", "").trim());
                                    }
                                    customJObj.put("Custom_" + params.getFieldlabel(), "Col" + params.getColnum());
                                    customJArr.put(customJObj);
                                }
                            }
                        }
                        //customfield = customJArr.toString();
                        if (!StringUtil.isNullOrEmpty(customfield)) {
                            JSONArray jcustomarray = new JSONArray(customfield);
                            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                            customrequestParams.put("customarray", jcustomarray);
                            customrequestParams.put("modulename", Constants.Acc_Product_modulename);
                            customrequestParams.put("moduleprimarykey", Constants.Acc_Productid);
                            customrequestParams.put("modulerecid", product.getID());
                            customrequestParams.put("moduleid", Constants.Acc_Product_Master_ModuleId);
                            customrequestParams.put("companyid", companyid);
                            productMap.put("id", product.getID());
                            customrequestParams.put("customdataclasspath", Constants.Acc_Product_custom_data_classpath);
                            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                productMap.put("accproductcustomdataref", product.getID());
                                productresult = accProductObj.updateProduct(productMap);
                            }
                            HashMap<String, Object> customHistoryParams = new HashMap<String, Object>();
                            customHistoryParams.put("productId", product.getID());
                            customHistoryParams.put("customarray", jcustomarray);
                            maintainCustomFieldHistoryForProduct(requestParams1, customHistoryParams);
                        }
                    } catch (Exception ex) {
                        failed++;
                        String errorMsg = ex.getMessage(), invalidColumns = "";
                        try {
                            JSONObject errorLog = new JSONObject("errorMsg");
                            errorMsg = errorLog.getString("errorMsg");
                            invalidColumns = errorLog.getString("invalidColumns");
                        } catch (JSONException jex) {
                            // Logger.getLogger(Importproduct.class.getName()).log(Level.SEVERE, null, jex);
                        }
                        failedRecords.append("\n" + createCSVrecord(recarr.toArray()) + "\"" + errorMsg.replaceAll("\"", "") + "\"");
                    }
                    total++;
                }
                cnt++;

                if (count == limit) {
                    txnManager.commit(status);

                    // for reset counter of transaction limit
                    count = 1;
                    def = new DefaultTransactionDefinition();
                    def.setName("import_Tx");
                    def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
                    status = txnManager.getTransaction(def);
                } else {
                    count++;
                }
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
            if (!commitedEx) { //if exception occurs during commit then dont call rollback
                txnManager.rollback(status);
            }
            issuccess = false;
            msg = "" + ex.getMessage();
            Logger.getLogger(ImportProduct.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Importing Records.");
        } finally {
//            fileInputStream.close();
//            br.close();
            // processQueue.remove(requestParams1);
            //this.isworking = false;
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
                logDataMap.put("Type", "xls");
                logDataMap.put("FailureFileType", failed>0?"csv":"");
                logDataMap.put("TotalRecs", total);
                logDataMap.put("Rejected", failed);
                logDataMap.put("Module", Constants.Acc_Product_Master_ModuleId);
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userId);
                logDataMap.put("Company", companyid);
                importDao.saveImportLog(logDataMap);

                String tableName = importDao.getTableName(fileName);
                importDao.removeFileTable(tableName); // Remove table after importing all records

                txnManager.commit(lstatus);
            } catch (Exception ex) {
                txnManager.rollback(lstatus);
                Logger.getLogger(ImportProduct.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", getActualFileName(fileName));
                returnObj.put("Module", Constants.Acc_Product_Master_ModuleId);
            } catch (JSONException ex) {
                Logger.getLogger(ImportProduct.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return returnObj;

    }

    public JSONObject importAssemblyProductCSV(HashMap<String, Object> requestParams) throws AccountingException, IOException, SessionExpiredException, JSONException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("import_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        boolean commitedEx = false;
        boolean issuccess = true;
        boolean isCurrencyColum = false;
        Product product = null;
        String prevProductCode = "";  //To Compare with new / next record in CSV / XLS
        String msg = "";
        String customfield = "";
        String fileName = null;
        double productInitialPurchasePrise = 0; // Product Initial Purchase Price     
        Date ondate = new Date();
        String productSalesPrice = ""; //Product Sales Price
        HashMap<String, Object> productMap = null;  //Product Hashmap
        KwlReturnObject productresult = null;
        FileInputStream fileInputStream = null;
        BufferedReader br = null;
        int total = 0, failed = 0;
        String companyid = requestParams.get("companyid").toString();
        String currencyId = requestParams.get("currencyId").toString();
        String userId = requestParams.get("userid").toString();
        SimpleDateFormat df = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");
        // DateFormat df = (DateFormat) requestParams.get("dateFormat");
        JSONObject jobj = new JSONObject();
        jobj = (JSONObject) requestParams.get("jobj");
        String masterPreference = requestParams.get("masterPreference").toString();
        String delimiterType = requestParams.get("delimiterType").toString();
        int limit = Constants.Transaction_Commit_Limit;
        int count = 1;
        boolean updateExistingRecordFlag = false;
        if (!StringUtil.isNullOrEmpty(requestParams.get("updateExistingRecordFlag").toString())) {
            updateExistingRecordFlag = Boolean.FALSE.parseBoolean(requestParams.get("updateExistingRecordFlag").toString());
        }

        JSONObject returnObj = new JSONObject();
        try {
            Date newUserDate = new Date();
            KwlReturnObject custumObjresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company1 = (Company) custumObjresult.getEntityList().get(0);
            if (company1.getCreator() != null) {
                newUserDate = authHandler.getUserNewDate(null, company1.getCreator().getTimeZone()!=null?company1.getCreator().getTimeZone().getDifference() : company1.getTimeZone().getDifference());
            }
            fileName = jobj.getString("filename");
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
            // the below line is commited due to not proparely showing header in error log file . ERP-11483
            // failedRecords.append(createCSVrecord(headArrayList.toArray()) + "\"Error Message\""); // failedRecords.append("\"Row No.\","+createCSVrecord(fileData)+"\"Error Message\"");
            HashMap currencyMap = getCurrencyMap();

            while ((record = br.readLine()) != null) {
                if (cnt != 0) {
                    String[] recarr = null;
                    if (delimiterType.equalsIgnoreCase("Bar")) {
                        recarr = record.split("\\|");
                    } else {
                        recarr = record.split(",");
                    }

                    try {
                        currencyId = currencyId;   //sessionHandlerImpl.getCurrencyID(request);

                        /*-------------------------------Sub-product Details (BOM)----------------------------------*/
                        /*If BOM Product ID OR Quantity is not available then Global Product Details will not save, */
                        //Build Of Material (BOM) Details
                        String bomProductID = "";
                        if (columnConfig.containsKey("bompid")) {
                            try {
                                bomProductID = recarr[(Integer) columnConfig.get("bompid")].replaceAll("\"", "").trim();
                                if (StringUtil.isNullOrEmpty(bomProductID)) {
                                    throw new AccountingException("BOM Product ID is not available");
                                }
                            } catch (ArrayIndexOutOfBoundsException ae) { //If user do not enter BOM % then default value ll be 100.
                                throw new AccountingException("BOM Product ID is not available");
                            }
                        } else {
                            throw new AccountingException("BOM Product ID column is not found");
                        }

                        //Get BOM Product Name
                        String bomProductName = "";
                        String bompid = ""; //Used to read Product Price.
                        Product bomproduct = getProductByProductName(companyid, bomProductID);
                        if (bomproduct != null) {
                            bomProductName = bomproduct.getName();
                            bompid = bomproduct.getID();
                        } else {
                            throw new AccountingException("Product Name is not found for " + bomProductID);
                        }

                        //Get BOM Product Description
                        String bomProductDesc = "";
                        if (bomproduct != null) {
                            bomProductDesc = bomproduct.getDescription();
                        } else {
                            throw new AccountingException("Product Description is not found for " + bomProductID);
                        }

                        //Get BOM Product Type
                        String bomProductType = "";
                        Producttype bomproducttype = getProductTypeByProductID(bomProductID);
                        if (bomproducttype != null) {
                            bomProductType = bomproducttype.getID();
                        } else {
                            throw new AccountingException("Product Type is not found for " + bomProductID);
                        }

                        //Get BOM Product Initial Purchase Price
                        double bomProductPurchasePrice = 0;
                        KwlReturnObject purchase = accProductObj.getProductPrice(bompid, true, null, "", ""); //True for Purchase Price, False for Sales Price
                        bomProductPurchasePrice = purchase.getEntityList().get(0) == null ? 0 : ((Double) purchase.getEntityList().get(0)).doubleValue();    //Converted into primitive data type

                        //Component Quantity, Actual Quantity, Total
                        String componentQuantity = "";
                        double compQuantity = 0;
                        String componentpercent = "";
                        double comppercent = 100;
                        double compactualQuantity = 0;
                        double comptotal = 0;
                        double recylequantity = 0;
                        double inventoryquantiy = 0;
                        double remainingquantity = 0;

                        if (columnConfig.containsKey("bomquantity")) {
                            try {
                                componentQuantity = recarr[(Integer) columnConfig.get("bomquantity")].replaceAll("\"", "").trim();
                                compQuantity = Double.parseDouble(componentQuantity);
                                if (compQuantity != 0) {
                                    if (columnConfig.containsKey("bompercent")) {
                                        try {
                                            componentpercent = recarr[(Integer) columnConfig.get("bompercent")].replaceAll("\"", "").trim();

                                            comppercent = Double.parseDouble(componentpercent);
                                            if (comppercent > 100) {
                                                throw new AccountingException("BOM percentage should not be greater than 100%.");
                                            }
                                            compactualQuantity = compQuantity * (comppercent / 100);
                                            comptotal = compQuantity * bomProductPurchasePrice * (comppercent / 100);
                                        } catch (NumberFormatException ne) {
                                            throw new AccountingException("Invalid BOM Component percent. It should be greater than zero");
                                        } catch (ArrayIndexOutOfBoundsException ae) {
                                            comppercent = 100;  //If user do not enter BOM % then default value ll be 100.
                                        }
                                    } else {    //Default Percentage
                                        compactualQuantity = compQuantity * 1; //componentpercent = 100
                                        comptotal = compQuantity * bomProductPurchasePrice * (comppercent / 100);
                                    }
                                } else {
                                    throw new AccountingException("Component Quantity should be greater than zero.");

                                }
                            } catch (NumberFormatException e) {
                                throw new AccountingException("Invalid BOM Quantity.");
                            } catch (ArrayIndexOutOfBoundsException ae) {
                                throw new AccountingException("Invalid BOM Quantity.");
                            }
                        }//Component Quantity

                        /*-------------------------------Sub-product Details (BOM)----------------------------------*/
                        //Get Product ID
                        String productID = "";
                        if (columnConfig.containsKey("pid")) {
                            productID = recarr[(Integer) columnConfig.get("pid")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(productID)) {
                                throw new AccountingException("Product ID is not available");
                            }
                        } else {
                            throw new AccountingException("Product ID column is not found");
                        }
                        if (!prevProductCode.equalsIgnoreCase(productID)) { //if Multiple BOM Components are associated with Single Product then we ll not hold global data again for same product
                            // for saving BOM initial purchase price
                            if (!prevProductCode.equalsIgnoreCase("") && productInitialPurchasePrise > 0 && product != null) {
                                if (productInitialPurchasePrise > 0) {
                                    HashMap<String, Object> initialPurchasePriceMap = new HashMap<String, Object>();
                                    initialPurchasePriceMap.put("productid", product.getID());
                                    initialPurchasePriceMap.put("companyid", companyid);
                                    initialPurchasePriceMap.put("carryin", true);
                                    initialPurchasePriceMap.put("price", productInitialPurchasePrise);
                                    initialPurchasePriceMap.put("applydate", ondate);
                                    initialPurchasePriceMap.put("affecteduser", "-1");
                                    initialPurchasePriceMap.put("currencyid", currencyId);
                                    initialPurchasePriceMap.put("uomid", product.getUnitOfMeasure().getID());
                                    accProductObj.addPriceList(initialPurchasePriceMap);
                                }
                            }

                            productInitialPurchasePrise = 0;
                            prevProductCode = productID;
                            //Get Product Type 
                            String productTypeID = "";
                            if (columnConfig.containsKey("type")) {
                                String productTypeName = recarr[(Integer) columnConfig.get("type")].replaceAll("\"", "").trim();
                                if (StringUtil.isNullOrEmpty(productTypeName)) {
                                    prevProductCode = "";
                                    throw new AccountingException("Product Type is not available");
                                }

                                Producttype producttype = getProductTypeByName(productTypeName);
                                if (producttype != null) {
                                    productTypeID = producttype.getID();
                                } else {
                                    prevProductCode = "";
                                    throw new AccountingException("Product Type is not found for " + productTypeName);
                                }
                            } else {
                                throw new AccountingException("Product Type column is not found.");
                            }

                            //Get Product Name
                            String productName = "";
                            if (columnConfig.containsKey("productname")) {
                                productName = recarr[(Integer) columnConfig.get("productname")].replaceAll("\"", "").trim();
                                if (StringUtil.isNullOrEmpty(productName)) {
                                    prevProductCode = "";
                                    throw new AccountingException("Product Name is not available");
                                }
                            } else {
                                throw new AccountingException("Product Name column is not found.");
                            }

                            //Get Product Description
                            String productDescription = "";
                            if (columnConfig.containsKey("desc")) {
                                productDescription = recarr[(Integer) columnConfig.get("desc")].replaceAll("\"", "").trim();
                            }

                            //Get Product UOM ID
                            String productUOMID = "";
                            UnitOfMeasure uom = null;
                            if (columnConfig.containsKey("uomname")) {
                                String productUOMName = recarr[(Integer) columnConfig.get("uomname")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(productUOMName)) {
                                    uom = getUOMByName(productUOMName, companyid);
                                    if (uom != null) {
                                        productUOMID = uom.getID();
                                    } else {
                                        if (masterPreference.equalsIgnoreCase("2")) {
                                            HashMap<String, Object> uomMap = new HashMap<String, Object>();
                                            uomMap.put("uomname", StringUtil.DecodeText(productUOMName));
                                            uomMap.put("uomtype", StringUtil.DecodeText(productUOMName));
                                            uomMap.put("precision", 0);
                                            uomMap.put("companyid", companyid);

                                            KwlReturnObject uomresult = accUomObj.addUoM(uomMap);
                                            uom = (UnitOfMeasure) uomresult.getEntityList().get(0);
                                            productUOMID = uom.getID();
                                        } else {
                                            if (masterPreference.equalsIgnoreCase("1")) {
                                                productUOMID = "";
                                            } else {
                                                prevProductCode = "";
                                                throw new AccountingException("Product Stock Unit Of Measure is not found for " + productUOMName);
                                            }
                                        }
                                    }
                                } else {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        productUOMID = "";
                                    } else {
                                        if (productTypeID.equals(Producttype.ASSEMBLY)) {
                                            productUOMID = "";
                                        } else {
                                            prevProductCode = "";
                                            throw new AccountingException("Product Stock Unit Of Measure is not available");
                                        }
                                    }
                                }
                            } else {
                                throw new AccountingException("Product Stock UOM column is not found.");
                            }

                            //Get Product Casing UOM ID
                            String casingUoMID = "";
                            UnitOfMeasure casinguom = null;
                            if (columnConfig.containsKey("casinguom")) {
                                String productUOMName = recarr[(Integer) columnConfig.get("casinguom")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(productUOMName)) {
                                    casinguom = getUOMByName(productUOMName, companyid);
                                    if (casinguom != null) {
                                        casingUoMID = casinguom.getID();
                                    } else {
                                        if (masterPreference.equalsIgnoreCase("2")) {
                                            HashMap<String, Object> uomMap = new HashMap<String, Object>();
                                            uomMap.put("uomname", StringUtil.DecodeText(productUOMName));
                                            uomMap.put("uomtype", StringUtil.DecodeText(productUOMName));
                                            uomMap.put("precision", 0);
                                            uomMap.put("companyid", companyid);
                                            KwlReturnObject uomresult = accUomObj.addUoM(uomMap);
                                            casinguom = (UnitOfMeasure) uomresult.getEntityList().get(0);
                                            casingUoMID = casinguom.getID();
                                        } else {
                                            if (masterPreference.equalsIgnoreCase("1")) {
                                                casingUoMID = "";
                                            } else {
                                                prevProductCode = "";
                                                throw new AccountingException("Product Unit Of Measure is not found for " + productUOMName);
                                            }
                                        }
                                    }
                                } else {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        casingUoMID = "";
                                    } else {
                                        if (productTypeID.equals(Producttype.ASSEMBLY)) {
                                            casingUoMID = "";
                                        } else {
                                            prevProductCode = "";
                                            throw new AccountingException("Product  Casing Unit Of Measure is not available");
                                        }
                                    }
                                }
                            } else {
                                casingUoMID = "";
                            }

                            //Get Product Inner UOM ID
                            String innerUoMID = "";
                            UnitOfMeasure inneruom = null;
                            if (columnConfig.containsKey("inneruom")) {
                                String productUOMName = recarr[(Integer) columnConfig.get("inneruom")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(productUOMName)) {
                                    inneruom = getUOMByName(productUOMName, companyid);
                                    if (inneruom != null) {
                                        innerUoMID = inneruom.getID();
                                    } else {
                                        if (masterPreference.equalsIgnoreCase("2")) {
                                            HashMap<String, Object> uomMap = new HashMap<String, Object>();
                                            uomMap.put("uomname", StringUtil.DecodeText(productUOMName));
                                            uomMap.put("uomtype", StringUtil.DecodeText(productUOMName));
                                            uomMap.put("precision", 0);
                                            uomMap.put("companyid", companyid);

                                            KwlReturnObject uomresult = accUomObj.addUoM(uomMap);
                                            inneruom = (UnitOfMeasure) uomresult.getEntityList().get(0);
                                            innerUoMID = inneruom.getID();
                                        } else {
                                            if (masterPreference.equalsIgnoreCase("1")) {
                                                innerUoMID = "";
                                            } else {
                                                prevProductCode = "";
                                                throw new AccountingException("Product Inner Unit Of Measure is not found for " + productUOMName);
                                            }
                                        }
                                    }
                                } else {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        innerUoMID = "";
                                    } else {
                                        if (productTypeID.equals(Producttype.ASSEMBLY)) {
                                            innerUoMID = "";
                                        } else {
                                            prevProductCode = "";
                                            throw new AccountingException("Product Inner Unit Of Measure is not available");
                                        }
                                    }
                                }
                            } else {
                                innerUoMID = "";
                            }

                            //Get Product Purchase UOM ID
                            String PurchaseUOMID = "";
                            UnitOfMeasure purchaseruom = null;
                            if (columnConfig.containsKey("purchaseuom")) {
                                String productUOMName = recarr[(Integer) columnConfig.get("purchaseuom")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(productUOMName)) {
                                    purchaseruom = getUOMByName(productUOMName, companyid);
                                    if (purchaseruom != null) {
                                        PurchaseUOMID = purchaseruom.getID();
                                    } else {
                                        if (masterPreference.equalsIgnoreCase("2")) {
                                            HashMap<String, Object> uomMap = new HashMap<String, Object>();
                                            uomMap.put("uomname", StringUtil.DecodeText(productUOMName));
                                            uomMap.put("uomtype", StringUtil.DecodeText(productUOMName));
                                            uomMap.put("precision", 0);
                                            uomMap.put("companyid", companyid);

                                            KwlReturnObject uomresult = accUomObj.addUoM(uomMap);
                                            purchaseruom = (UnitOfMeasure) uomresult.getEntityList().get(0);
                                            PurchaseUOMID = purchaseruom.getID();
                                        } else {
                                            if (masterPreference.equalsIgnoreCase("1")) {
                                                PurchaseUOMID = "";
                                            } else {
                                                prevProductCode = "";
                                                throw new AccountingException("Product Purchase Unit Of Measure is not found for " + productUOMName);
                                            }
                                        }
                                    }
                                } else {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        PurchaseUOMID = "";
                                    } else {
                                        if (productTypeID.equals(Producttype.ASSEMBLY)) {
                                            PurchaseUOMID = "";
                                        } else {
                                            prevProductCode = "";
                                            throw new AccountingException("Product Purchase Unit Of Measure is not available");
                                        }
                                    }
                                }
                            } else {
                                PurchaseUOMID = "";
                            }

                            //Get Product Sales UOM ID
                            String SalesUOMID = "";
                            UnitOfMeasure salesuom = null;
                            if (columnConfig.containsKey("salesuom")) {
                                String productUOMName = recarr[(Integer) columnConfig.get("salesuom")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(productUOMName)) {
                                    salesuom = getUOMByName(productUOMName, companyid);
                                    if (salesuom != null) {
                                        SalesUOMID = salesuom.getID();
                                    } else {
                                        if (masterPreference.equalsIgnoreCase("2")) {
                                            HashMap<String, Object> uomMap = new HashMap<String, Object>();
                                            uomMap.put("uomname", StringUtil.DecodeText(productUOMName));
                                            uomMap.put("uomtype", StringUtil.DecodeText(productUOMName));
                                            uomMap.put("precision", 0);
                                            uomMap.put("companyid", companyid);

                                            KwlReturnObject uomresult = accUomObj.addUoM(uomMap);
                                            salesuom = (UnitOfMeasure) uomresult.getEntityList().get(0);
                                            SalesUOMID = salesuom.getID();
                                        } else {
                                            if (masterPreference.equalsIgnoreCase("1")) {
                                                SalesUOMID = "";
                                            } else {
                                                prevProductCode = "";
                                                throw new AccountingException("Product Sales Unit Of Measure is not found for " + productUOMName);
                                            }
                                        }
                                    }
                                } else {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        SalesUOMID = "";
                                    } else {
                                        if (productTypeID.equals(Producttype.ASSEMBLY)) {
                                            SalesUOMID = "";
                                        } else {
                                            prevProductCode = "";
                                            throw new AccountingException("Product Sales Unit Of Measure is not available");
                                        }
                                    }
                                }
                            } else {
                                SalesUOMID = "";
                            }

                            //Get Casing UOM Value
                            String casinguomvalue = "1";
                            if (columnConfig.containsKey("casinguom_value")) {
                                casinguomvalue = recarr[(Integer) columnConfig.get("casinguom_value")].replaceAll("\"", "").trim();
                            }

                            //Get Inner UOM Value
                            String inneruomvalue = "1";
                            if (columnConfig.containsKey("inneruom_value")) {
                                inneruomvalue = recarr[(Integer) columnConfig.get("inneruom_value")].replaceAll("\"", "").trim();
                            }

                            //Get Stock UOM Value
                            String stcokuomvalue = "1";
                            if (columnConfig.containsKey("stockuom_value")) {
                                stcokuomvalue = recarr[(Integer) columnConfig.get("stockuom_value")].replaceAll("\"", "").trim();
                            }

                            //Product Reorder Level
                            String productReorderLevel = "";
                            if (columnConfig.containsKey("reorderlevel")) {
                                productReorderLevel = recarr[(Integer) columnConfig.get("reorderlevel")].replaceAll("\"", "").trim();
                                if (StringUtil.isNullOrEmpty(productReorderLevel)) {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        productReorderLevel = "0";
                                    } else {
                                        if (productTypeID.equals(Producttype.ASSEMBLY)) {
                                            productReorderLevel = "0";
                                        } else {
                                            prevProductCode = "";
                                            throw new AccountingException("Product Reorder Level is not available");
                                        }
                                    }
                                }
                            } else {
                                productReorderLevel = "0";
                            }

                            //Product Reorder Quantity
                            String productReorderQuantity = "";
                            if (columnConfig.containsKey("reorderquantity")) {
                                productReorderQuantity = recarr[(Integer) columnConfig.get("reorderquantity")].replaceAll("\"", "").trim();
                                if (StringUtil.isNullOrEmpty(productReorderQuantity)) {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        productReorderQuantity = "0";
                                    } else {
                                        if (productTypeID.equals(Producttype.ASSEMBLY)) {
                                            productReorderQuantity = "0";
                                        } else {
                                            prevProductCode = "";
                                            throw new AccountingException("Product Reorder Quantity is not available");
                                        }
                                    }
                                }
                            } else {
                                productReorderQuantity = "0";
                            }

                            //Product Warranty Period
                            String productWarrantyPeriod = "";
                            if (columnConfig.containsKey("warrantyperiod")) {
                                productWarrantyPeriod = recarr[(Integer) columnConfig.get("warrantyperiod")].replaceAll("\"", "").trim();
                                if (StringUtil.isNullOrEmpty(productWarrantyPeriod)) {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        productWarrantyPeriod = "0";
                                    } else {
                                        if (productTypeID.equals(Producttype.ASSEMBLY)) {
                                            productWarrantyPeriod = "0";
                                        } else {
                                            prevProductCode = "";
                                            throw new AccountingException("Product Warranty Period is not available");
                                        }
                                    }
                                }
                            } else {
                                productWarrantyPeriod = "0";
                            }

                            //Product Sales Warranty Period
                            String productSalesWarrantyPeriod = "";
                            if (columnConfig.containsKey("warrantyperiodsal")) {
                                productSalesWarrantyPeriod = recarr[(Integer) columnConfig.get("warrantyperiodsal")].replaceAll("\"", "").trim();
                                if (StringUtil.isNullOrEmpty(productSalesWarrantyPeriod)) {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        productSalesWarrantyPeriod = "0";
                                    } else {
                                        if (productTypeID.equals(Producttype.ASSEMBLY)) {
                                            productSalesWarrantyPeriod = "0";
                                        } else {
                                            prevProductCode = "";
                                            throw new AccountingException("Product Sales Warranty Period is not available");
                                        }
                                    }
                                }
                            } else {
                                productSalesWarrantyPeriod = "0";
                            }

                            //Product Lead Time
                            String productLeadTime = "";
                            if (columnConfig.containsKey("leadtime")) {
                                productLeadTime = recarr[(Integer) columnConfig.get("leadtime")].replaceAll("\"", "").trim();
                                if (StringUtil.isNullOrEmpty(productLeadTime)) {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        productLeadTime = "0";
                                    } else {
                                        if (productTypeID.equals(Producttype.ASSEMBLY)) {
                                            productLeadTime = "0";
                                        } else {
                                            prevProductCode = "";
                                            throw new AccountingException("Product Lead Time is not available");
                                        }
                                    }
                                } else {
                                    if (Integer.parseInt(productLeadTime) > 365) {
                                        throw new AccountingException("Product Lead Time should not be greater than 365");
                                    } else if (Integer.parseInt(productLeadTime) < 0) {
                                        throw new AccountingException("Product Lead Time should not be less than 0");
                                    }
                                }
                            } else {
                                productLeadTime = "0";
                            }

                            String productweight = "";
                            if (columnConfig.containsKey("productweight")) {
                                productweight = recarr[(Integer) columnConfig.get("productweight")].replaceAll("\"", "").trim();
                                if (StringUtil.isNullOrEmpty(productweight)) {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        productweight = "0";
                                    } else {
                                        if (productTypeID.equals(Producttype.ASSEMBLY)) {
                                            productweight = "0";
                                        } else {
                                            prevProductCode = "";
                                            throw new AccountingException("This is not an Assembly Product");
                                        }
                                    }
                                } else {
                                    if (Double.parseDouble(productweight) <= -1) {
                                        throw new AccountingException("Product Weight should not be less than zero");
                                    }
                                }
                            } else {
                                productweight = "0";
                            }

                            //Cycle Count Interval
                            String productCycleCountInterval = "";
                            if (columnConfig.containsKey("ccountinterval")) {
                                productCycleCountInterval = recarr[(Integer) columnConfig.get("ccountinterval")].replaceAll("\"", "").trim();
                                if (StringUtil.isNullOrEmpty(productCycleCountInterval)) {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        productCycleCountInterval = "1";
                                    } else {
                                        if (productTypeID.equals(Producttype.ASSEMBLY)) {
                                            productCycleCountInterval = "1";
                                        } else {
                                            prevProductCode = "";
                                            throw new AccountingException("Product Cycle Count Interval is not available");
                                        }
                                    }
                                }
                            } else {
                                productCycleCountInterval = "1";
                            }

                            //Cycle Count Tolerance
                            String productCycleCountTolerance = "";
                            if (columnConfig.containsKey("ccounttolerance")) {
                                productCycleCountTolerance = recarr[(Integer) columnConfig.get("ccounttolerance")].replaceAll("\"", "").trim();
                                if (StringUtil.isNullOrEmpty(productCycleCountTolerance)) {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        productCycleCountTolerance = "0";
                                    } else {
                                        if (productTypeID.equals(Producttype.ASSEMBLY)) {
                                            productCycleCountTolerance = "0";
                                        } else {
                                            prevProductCode = "";
                                            throw new AccountingException("Product Cycle Count Tolerance is not available");
                                        }
                                    }
                                }
                            } else {
                                productCycleCountTolerance = "0";
                            }

                            //Parent Product UUID
                            String parentProductUUID = "";
                            if (columnConfig.containsKey("parentid")) {
                                String parentProductID = recarr[(Integer) columnConfig.get("parentid")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(parentProductID)) {
                                    Product parentProduct = getProductByProductID(parentProductID, companyid);
                                    if (parentProduct != null) {
                                        parentProductUUID = parentProduct.getID();
                                    } else {
                                        if (masterPreference.equalsIgnoreCase("1")) {
                                            parentProductUUID = null;
                                        } else {
                                            prevProductCode = "";
                                            throw new AccountingException("Parent Product is not found for " + parentProductID);
                                        }
                                    }
                                } else {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        parentProductUUID = null;
                                    } else {
                                        prevProductCode = "";
                                        throw new AccountingException("Parent Product is not available");
                                    }
                                }
                            } else {
                                parentProductUUID = null;
                            }

                            //Product Sales Account ID
                            String productSalesAccId = "";
                            if (columnConfig.containsKey("salesaccountname")) {
                                String productSalesAccountName = recarr[(Integer) columnConfig.get("salesaccountname")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(productSalesAccountName)) {
                                    Account salesAccount = getAccountByName(productSalesAccountName, companyid);
                                    if (salesAccount != null) {
                                        productSalesAccId = salesAccount.getID();
                                    } else {
                                        prevProductCode = "";
                                        throw new AccountingException("Product Sales Account is not found for " + productSalesAccountName);
                                    }
                                } else {
                                    prevProductCode = "";
                                    throw new AccountingException("Product Sales Account is not available");
                                }
                            } else {
                                throw new AccountingException("Product Sales Account column is not found.");
                            }

                            //Product Sales Account Return ID
                            String productSalesReturnAccId = "";
                            if (columnConfig.containsKey("salesretaccountname")) {
                                String productSalesReturnAccountName = recarr[(Integer) columnConfig.get("salesretaccountname")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(productSalesReturnAccountName)) {
                                    Account salesReturnAccount = getAccountByName(productSalesReturnAccountName, companyid);
                                    if (salesReturnAccount != null) {
                                        productSalesReturnAccId = salesReturnAccount.getID();
                                    } else {
                                        prevProductCode = "";
                                        throw new AccountingException("Product Sales Return Account is not found for " + productSalesReturnAccountName);
                                    }
                                } else {
                                    prevProductCode = "";
                                    throw new AccountingException("Product Sales Return Account is not available");
                                }
                            } else {
                                throw new AccountingException("Product Sales Return Account column is not found.");
                            }

                            //Vendor Details
                            String productPreferedVendorID = "";
                            if (columnConfig.containsKey("vendornameid")) {
                                String productPreferedVendorName = recarr[(Integer) columnConfig.get("vendornameid")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(productPreferedVendorName)) {
                                    Vendor vendor = getVendorByName(productPreferedVendorName, companyid);
                                    if (vendor != null) {
                                        productPreferedVendorID = vendor.getID();
                                    } else {
                                        if (masterPreference.equalsIgnoreCase("1")) {
                                            productPreferedVendorID = null;
                                        } else {
                                            prevProductCode = "";
                                            throw new AccountingException("Prefered Vendor is not found for " + productPreferedVendorName);
                                        }
                                    }
                                } else {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        productPreferedVendorID = null;
                                    } else {
                                        if (productTypeID.equals(Producttype.ASSEMBLY)) {
                                            productPreferedVendorID = null;
                                        } else {
                                            prevProductCode = "";
                                            throw new AccountingException("Prefered Vendor is not available");
                                        }
                                    }
                                }
                            } else {
                                productPreferedVendorID = null;
                            }

                            //Product Purchase Account ID
                            String productPurchaseAccId = "";
                            if (columnConfig.containsKey("purchaseaccountname")) {
                                String productPurchaseAccountName = recarr[(Integer) columnConfig.get("purchaseaccountname")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(productPurchaseAccountName)) {
                                    Account purchaseAccount = getAccountByName(productPurchaseAccountName, companyid);
                                    if (purchaseAccount != null) {
                                        productPurchaseAccId = purchaseAccount.getID();
                                    } else {
                                        prevProductCode = "";
                                        throw new AccountingException("Product Purchase Account is not found for " + productPurchaseAccountName);
                                    }
                                } else {
                                    prevProductCode = "";
                                    throw new AccountingException("Product Purchase Account is not available");
                                }
                            } else {
                                throw new AccountingException("Product Purchase Account column is not found.");
                            }

                            //Product Purchase Return Account ID
                            String productPurchaseReturnAccId = "";
                            if (columnConfig.containsKey("purchaseretaccountname")) {
                                String productPurchaseReturnAccountName = recarr[(Integer) columnConfig.get("purchaseretaccountname")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(productPurchaseReturnAccountName)) {
                                    Account purchaseReturnAccount = getAccountByName(productPurchaseReturnAccountName, companyid);
                                    if (purchaseReturnAccount != null) {
                                        productPurchaseReturnAccId = purchaseReturnAccount.getID();
                                    } else {
                                        prevProductCode = "";
                                        throw new AccountingException("Product Purchase Return Account is not found for " + productPurchaseReturnAccountName);
                                    }
                                } else {
                                    prevProductCode = "";
                                    throw new AccountingException("Product Purchase Return Account is not available");
                                }
                            } else {
                                throw new AccountingException("Product Purchase Return Account column is not found.");
                            }

                            //Product Initial Quantity   
                            String productInitialQuantity = "0";
                            if (columnConfig.containsKey("initialquantity")) {
                                productInitialQuantity = recarr[(Integer) columnConfig.get("initialquantity")].replaceAll("\"", "").trim();
                                if (StringUtil.isNullOrEmpty(productInitialQuantity)) {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        productInitialQuantity = "0";
                                    } else {
                                        if (productTypeID.equals(Producttype.ASSEMBLY)) {
                                            productInitialQuantity = "0";
                                        } else {
                                            prevProductCode = "";
                                            throw new AccountingException("Product Initial Quantity is not available");
                                        }
                                    }
                                }
                            } else {
                                productInitialQuantity = "0";
                            }

                            if (columnConfig.containsKey("initialsalesprice")) {
                                productSalesPrice = recarr[(Integer) columnConfig.get("initialsalesprice")].replaceAll("\"", "").trim();
                                if (StringUtil.isNullOrEmpty(productSalesPrice)) {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        productSalesPrice = "";
                                    } else {
                                        prevProductCode = "";
                                        throw new AccountingException("Product Sales Price is not available");
                                    }
                                }
                            } else {
                                productSalesPrice = "";
                            }

                            //Currency Details
                            String MsgExep = "";
                            if (columnConfig.containsKey("currencyName")) {
                                String productPriceCurrencyStr = recarr[(Integer) columnConfig.get("currencyName")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(productPriceCurrencyStr)) {
                                    currencyId = getCurrencyId(productPriceCurrencyStr, currencyMap);
                                    if (StringUtil.isNullOrEmpty(currencyId)) {
                                        MsgExep = "Currency format you entered is not correct. it should be like \\\"SG Dollar (SGD)\\\""; //messageSource.getMessage("acc.field.ImportProductExceptionFormat", null, RequestContextUtils.getLocale(request));
                                        prevProductCode = "";
                                        throw new AccountingException(MsgExep);
                                    }
                                } else {
                                    prevProductCode = "";
                                    throw new AccountingException("Currency is not available.");
                                }
                            } else {
                                throw new AccountingException("Currency column is not found.");
                            }

                            //Product Default Location
                            String productDefaultLocationID = "";
                            if (columnConfig.containsKey("locationName")) {
                                String productDefaultLocationName = recarr[(Integer) columnConfig.get("locationName")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(productDefaultLocationName)) {
                                    InventoryLocation invLoc = getInventoryLocationByName(productDefaultLocationName, companyid);
                                    if (invLoc != null) {
                                        productDefaultLocationID = invLoc.getId();
                                    } else {
                                        if (masterPreference.equalsIgnoreCase("2")) {
                                            HashMap requestParam = requestParams;//AccountingManager.getGlobalParams(request);
                                            requestParam.put("id", "");
                                            requestParam.put("name", productDefaultLocationName);
                                            requestParam.put("parent", null);
                                            KwlReturnObject jeresult = accountingHandlerDAOobj.getObject(User.class.getName(), userId);
                                            User user = (User) jeresult.getEntityList().get(0);
                                            requestParam.put("user", user);
                                            KwlReturnObject locationResult = accMasterItemsDAOobj.addLocationItem(requestParam);
                                            invLoc = (InventoryLocation) locationResult.getEntityList().get(0);
                                            productDefaultLocationID = invLoc.getId();
                                        } else {
                                            if (masterPreference.equalsIgnoreCase("1")) {
                                                productDefaultLocationID = null;
                                            } else {
                                                prevProductCode = "";
                                                throw new AccountingException("Default Location is not found for " + productDefaultLocationName);
                                            }
                                        }
                                    }
                                } else {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        productDefaultLocationID = null;
                                    } else {
                                        prevProductCode = "";
                                        throw new AccountingException("Default Location is not available.");
                                    }
                                }
                            } else {
                                productDefaultLocationID = null;
                            }

                            //Default Warehouse
                            String productDefaultWarehouseID = "";
                            if (columnConfig.containsKey("warehouseName")) {
                                String productDefaultWarehouseName = recarr[(Integer) columnConfig.get("warehouseName")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(productDefaultWarehouseName)) {
                                    InventoryWarehouse invWHouse = getInventoryWarehouseByName(productDefaultWarehouseName, companyid);
                                    if (invWHouse != null) {
                                        productDefaultWarehouseID = invWHouse.getId();
                                    } else {
                                        if (masterPreference.equalsIgnoreCase("2")) {
                                            HashMap requestParam = requestParams;// AccountingManager.getGlobalParams(request);
                                            requestParam.put("id", "");
                                            requestParam.put("name", productDefaultWarehouseName);
                                            requestParam.put("parent", null);
                                            requestParam.put("location", null);

                                            KwlReturnObject warehouseResult = accMasterItemsDAOobj.addWarehouseItem(requestParam);
                                            invWHouse = (InventoryWarehouse) warehouseResult.getEntityList().get(0);
                                            productDefaultWarehouseID = invWHouse.getId();
                                        } else {
                                            if (masterPreference.equalsIgnoreCase("1")) {
                                                productDefaultWarehouseID = null;
                                            } else {
                                                prevProductCode = "";
                                                throw new AccountingException("Default Warehouse is not found for " + productDefaultWarehouseName);
                                            }
                                        }
                                    }
                                } else {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        productDefaultWarehouseID = null;
                                    } else {
                                        prevProductCode = "";
                                        throw new AccountingException("Default Warehouse is not available.");
                                    }
                                }
                            } else {
                                productDefaultWarehouseID = null;
                            }

                            //Make available in other application
                            Boolean isSyncable = false;
                            if (columnConfig.containsKey("syncable")) {
                                String productMakeAvailableInOtherApp = recarr[(Integer) columnConfig.get("syncable")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(productMakeAvailableInOtherApp)) {
                                    if (productMakeAvailableInOtherApp.equalsIgnoreCase("T")) {
                                        isSyncable = true;
                                    } else if (productMakeAvailableInOtherApp.equalsIgnoreCase("F")) {
                                        isSyncable = false;
                                    } else {
                                        prevProductCode = "";
                                        throw new AccountingException("Format you entered is not correct. It should be like \"T\" or \"F\"");
                                    }
                                } else {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        isSyncable = false;
                                    } else {
                                        prevProductCode = "";
                                        throw new AccountingException("Make available in other application is not available.");
                                    }
                                }
                            }

                            //Multiple UOM
                            Boolean isMultiUOM = false;
                            if (columnConfig.containsKey("multiuom")) {
                                String multipleUOM = recarr[(Integer) columnConfig.get("multiuom")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(multipleUOM)) {
                                    if (multipleUOM.equalsIgnoreCase("T")) {
                                        isMultiUOM = true;
                                    } else if (multipleUOM.equalsIgnoreCase("F")) {
                                        isMultiUOM = false;
                                    } else {
                                        prevProductCode = "";
                                        throw new AccountingException("Format you entered is not correct. It should be like \"T\" or \"F\"");
                                    }
                                } else {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        isMultiUOM = false;
                                    } else {
                                        if (productTypeID.equals(Producttype.ASSEMBLY)) {
                                            isMultiUOM = false;
                                        } else {
                                            prevProductCode = "";
                                            throw new AccountingException("Multiple UOM is not available.");
                                        }
                                    }
                                }
                            }

                            Boolean isIslocationforproduct = false;
                            if (columnConfig.containsKey(Constants.ACTIVATE_LOCATION)) {
                                String ISlocationforproduct = recarr[(Integer) columnConfig.get(Constants.ACTIVATE_LOCATION)].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(ISlocationforproduct)) {
                                    if (ISlocationforproduct.equalsIgnoreCase("T")) {
                                        isIslocationforproduct = true;
                                    } else if (ISlocationforproduct.equalsIgnoreCase("F")) {
                                        isIslocationforproduct = false;
                                    } else {
                                        prevProductCode = "";
                                        throw new AccountingException("Format you entered is not correct. It should be like \"T\" or \"F\"");
                                    }
                                }
                            }

                            Boolean isIsSerialForProduct = false;
                            if (columnConfig.containsKey(Constants.ACTIVATE_SERIAL_NO)) {
                                String IsSerialForProduct = recarr[(Integer) columnConfig.get(Constants.ACTIVATE_SERIAL_NO)].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(IsSerialForProduct)) {
                                    if (IsSerialForProduct.equalsIgnoreCase("T")) {
                                        isIsSerialForProduct = true;
                                    } else if (IsSerialForProduct.equalsIgnoreCase("F")) {
                                        isIsSerialForProduct = false;
                                    } else {
                                        prevProductCode = "";
                                        throw new AccountingException("Format you entered is not correct. It should be like \"T\" or \"F\"");
                                    }
                                }
                            }
                            Boolean isIswarehouseforproduct = false;
                            if (columnConfig.containsKey(Constants.ACTIVATE_WAREHOUSE)) {
                                String Iswarehouseforproduct = recarr[(Integer) columnConfig.get(Constants.ACTIVATE_WAREHOUSE)].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(Iswarehouseforproduct)) {
                                    if (Iswarehouseforproduct.equalsIgnoreCase("T")) {
                                        isIswarehouseforproduct = true;
                                    } else if (Iswarehouseforproduct.equalsIgnoreCase("F")) {
                                        isIswarehouseforproduct = false;
                                    } else {
                                        prevProductCode = "";
                                        throw new AccountingException("Format you entered is not correct. It should be like \"T\" or \"F\"");
                                    }
                                }
                            }
                            Boolean isIsBatchForProduct = false;
                            if (columnConfig.containsKey(Constants.ACTIVATE_BATCH)) {
                                String IsBatchForProduct = recarr[(Integer) columnConfig.get(Constants.ACTIVATE_BATCH)].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(IsBatchForProduct)) {
                                    if (IsBatchForProduct.equalsIgnoreCase("T")) {
                                        isIsBatchForProduct = true;
                                    } else if (IsBatchForProduct.equalsIgnoreCase("F")) {
                                        isIsBatchForProduct = false;
                                    } else {
                                        prevProductCode = "";
                                        throw new AccountingException("Format you entered is not correct. It should be like \"T\" or \"F\"");
                                    }
                                }
                            }
                            // For Checking 'ProductID' is exist or not
                            KwlReturnObject result = accProductObj.getProductIDCount(productID, companyid,false);
                            int nocount = result.getRecordTotalCount();
                            if (nocount > 0 && !updateExistingRecordFlag) {
                                throw new AccountingException("Product ID '" + productID + "' already exists.");
                            } else if (nocount == 0 && updateExistingRecordFlag) {
                                throw new AccountingException("Product ID '" + productID + "' not exists.");
                            }
                            MasterItem prodMasterItemObj = accProductObj.getProductsMasterItem(companyid, productID);
                            KwlReturnObject kwlReturnObject_SPA = accProductObj.selectSubProductFromAssembly(productID);
                            KwlReturnObject kwlReturnObject_I = accProductObj.selectInventoryByProduct(productID, companyid);
                            boolean isUsedInTransaction = false;
                            if (updateExistingRecordFlag) {
                                Product prod = (Product) result.getEntityList().get(0);
                                //Check whether this product is used in any transaction or not
                                List listObj = accProductModuleService.isProductUsedintransction(prod.getID(), companyid, requestParams);
                                isUsedInTransaction = (Boolean) listObj.get(0);    //always boolean value
                                if (isUsedInTransaction) {
                                    throw new AccountingException("Product ID '" + productID + "' used in transaction(s). So it cannot update");
                                }
                                //Check whether this product is used for Build Assembly or not
                                KwlReturnObject rs = accProductObj.getAssemblyProductDetails(prod.getID()); //ERP-12252
                                if (rs.getRecordTotalCount() > 0) {
                                    throw new AccountingException(productID + "' used in Build Assembly. You cannot edit this Assembly Product.");
                                }
                                //In update case, if product is not linked in any transaction, delete this product
                                System.out.println("Product not linked. so deleting this product to update");
                                accProductObj.deleteProductCustomData(prod.getID());
                                accProductObj.deleteProPricePermanently(prod.getID(), companyid);
                                accProductObj.deleteProductCategoryMappingDtails(productID);
                                if (prod.getProducttype().getID().equals(Producttype.ASSEMBLY)) {
                                    accProductObj.deleteProductAssembly(prod.getID());
                                    accProductObj.deleteProductBuildDetails(prod.getID(), companyid);
                                    accProductObj.deleteProductbBuild(prod.getID(), companyid);
                                }
                                if (!StringUtil.isNullOrEmpty(productID) && prodMasterItemObj != null) {
                                    accProductObj.deleteProductCategoryMappingDtails(prod.getID());
                                }
                                if (!StringUtil.isNullOrEmpty(prod.getID())) {
                                    accProductObj.deleteNewProductBatch(prod.getID(), companyid);
                                }
                                if (!StringUtil.isNullOrEmpty(productID) && kwlReturnObject_I.getRecordTotalCount() > 0 && !prod.getProducttype().getID().equals(Producttype.ASSEMBLY)) {
                                    accProductObj.deleteProductInitialInventoryDtails(prod.getID(), companyid);
                                } else {
                                    accProductObj.deleteAssemblyProductInventory(prod.getID(), companyid);
                                }
                                accProductObj.deleteProductPermanently(prod.getID(), companyid);
                            }
                            //Create Global Product Map
                            productMap = new HashMap<String, Object>();
                            productMap.put("producttype", productTypeID);
                            productMap.put("name", productName);
                            productMap.put("productid", productID);
                            productMap.put("desc", productDescription);
                            productMap.put("syncable", false);
                            productMap.put("multiuom", false);
                            productMap.put("isBatchForProduct", isIsBatchForProduct);
                            productMap.put("isSerialForProduct", isIsSerialForProduct);
                            productMap.put("uomid", productUOMID);
                            if (!productTypeID.equals(Producttype.SERVICE)) { //Service Product
                                if (uom != null) {
                                    productMap.put("transferUoM", uom);
                                    productMap.put("orderUoM", uom);
                                }
                                if (salesuom != null) {
                                    productMap.put("salesuomid", salesuom);
                                } else if (salesuom == null && uom != null) {
                                    productMap.put("salesuomid", uom);
                                }
                                if (purchaseruom != null) {
                                    productMap.put("purchaseuomid", purchaseruom);
                                } else if (purchaseruom == null && uom != null) {
                                    productMap.put("purchaseuomid", uom);
                                }

                                //UOM Details
                                KwlReturnObject companyObj = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                                Company company = (Company) companyObj.getEntityList().get(0);
                                Packaging packaging = null;
                                if (inneruom != null || casinguom != null) {
                                    packaging = new Packaging();
                                    packaging.setCasingUoM(casinguom);
                                    packaging.setInnerUoM(inneruom);
                                    packaging.setStockUoM(uom);
                                    packaging.setCasingUomValue(Double.parseDouble(casinguomvalue));
                                    packaging.setInnerUomValue(Double.parseDouble(inneruomvalue));
                                    packaging.setStockUomValue(Double.parseDouble(stcokuomvalue));
                                    packaging.setCompany(company);

                                }
                                if (packaging != null) {
                                    accProductObj.saveProductPackging(packaging);
                                    productMap.put("packaging", packaging);
                                }
                            } //Service Product

                            productMap.put("reorderlevel", Double.parseDouble(productReorderLevel));
                            productMap.put("reorderquantity", Double.parseDouble(productReorderQuantity));
                            productMap.put("warrantyperiod", Integer.parseInt(productWarrantyPeriod));
                            productMap.put("warrantyperiodsal", Integer.parseInt(productSalesWarrantyPeriod));
                            productMap.put("leadtime", Integer.parseInt(productLeadTime));
                            productMap.put("productweight", Double.parseDouble(productweight));

                            productMap.put("parentid", parentProductUUID);
                            productMap.put("salesaccountid", productSalesAccId);
                            productMap.put("purchaseretaccountid", productPurchaseReturnAccId);
                            productMap.put("salesretaccountid", productSalesReturnAccId);
                            productMap.put("vendorid", productPreferedVendorID);
                            productMap.put("purchaseaccountid", productPurchaseAccId);
                            productMap.put("purchaseretaccountid", productPurchaseReturnAccId);

                            if (!StringUtil.isNullOrEmpty(productDefaultLocationID)) {
                                productMap.put("location", productDefaultLocationID);
                            }

                            if (!StringUtil.isNullOrEmpty(productDefaultWarehouseID)) {
                                productMap.put("warehouse", productDefaultWarehouseID);
                            }

                            productMap.put("syncable", isSyncable);
                            productMap.put("multiuom", isMultiUOM);
                            productMap.put("isLocationForProduct", isIslocationforproduct);
                            productMap.put("isWarehouseForProduct", isIswarehouseforproduct);
                            productMap.put("deletedflag", false);
                            productMap.put("companyid", companyid);
                            productMap.put("isImport", 1);
                            productMap.put("updateExistingRecordFlag", updateExistingRecordFlag);
                            productMap.put("currencyid", currencyId);

                            //Save Global Product Details
                            if (!StringUtil.isNullOrEmpty(bomProductID) && compQuantity > 0) { //Global Product ll save only when Bom Product ID & Quantity is not empty
                                productresult = accProductObj.addProduct(productMap);
                                product = (Product) productresult.getEntityList().get(0);
                            }

                            if (productInitialQuantity.length() > 0) {
                                JSONObject inventoryjson = new JSONObject();
                                inventoryjson.put("productid", product.getID());
                                inventoryjson.put("quantity", Double.parseDouble(productInitialQuantity));
                                inventoryjson.put("baseuomquantity", Double.parseDouble(productInitialQuantity));
                                inventoryjson.put("baseuomrate", 1);
                                if (product.getUnitOfMeasure() != null) {
                                    inventoryjson.put("uomid", product.getUnitOfMeasure().getID());
                                }
                                inventoryjson.put("description", "Inventory Opened");
                                inventoryjson.put("carryin", true);
                                inventoryjson.put("defective", false);
                                inventoryjson.put("newinventory", true);
                                inventoryjson.put("companyid", companyid);
                                inventoryjson.put("updatedate", newUserDate);
                                accProductObj.addInventory(inventoryjson);

                                HashMap<String, Object> assemblyParams = requestParams; //AccountingManager.getGlobalParams(request);
                                assemblyParams.put("assembly", "");
                                assemblyParams.put("applydate", authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
                                assemblyParams.put("quantity", Double.parseDouble(productInitialQuantity));
                                assemblyParams.put("memo", "Inventory Opened");
                                assemblyParams.put("refno", "");
                                assemblyParams.put("buildproductid", product.getID());
                                accProductObj.updateAssemblyInventory(assemblyParams);
                            }

                            if (productSalesPrice.length() > 0) {
                                // creating Price list Hashmap
                                HashMap<String, Object> salesPriceMap = new HashMap<String, Object>();
                                salesPriceMap.put("productid", product.getID());
                                salesPriceMap.put("companyid", companyid);
                                salesPriceMap.put("carryin", false);
                                salesPriceMap.put("price", Double.parseDouble(productSalesPrice));
                                salesPriceMap.put("applydate", authHandler.getConstantDateFormatter().parse(authHandler.getConstantDateFormatter().format(ondate)));
                                salesPriceMap.put("affecteduser", "-1");
                                salesPriceMap.put("currencyid", currencyId);
                                salesPriceMap.put("uomid", product.getUnitOfMeasure().getID());
                                accProductObj.addPriceList(salesPriceMap);
                            }
                            // For create custom field array
                            customfield = "";
                            JSONArray customJArr = new JSONArray();
                            for (int i = 0; i < jSONArray.length(); i++) {
                                JSONObject jSONObject = jSONArray.getJSONObject(i);

                                if (jSONObject.optBoolean("customflag", false)) {//&& !jSONObject.optBoolean("isLineItem",false)
                                    HashMap<String, Object> requestParams1 = new HashMap<String, Object>();
                                    requestParams1.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.fieldlabel));
                                    requestParams1.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, jSONObject.getString("columnname")));

                                    KwlReturnObject fieldParamsResult = accCommonTablesDAO.getFieldParams(requestParams1); // get custom field for module
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
                                                requestParams1 = new HashMap<String, Object>();
                                                requestParams1.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "value", "deleteflag"));
                                                requestParams1.put(Constants.filter_values, Arrays.asList(params.getId(), fieldComboDataArr[dataArrIndex], 0));

                                                fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParams1);
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
                                            requestParams1 = new HashMap<String, Object>();
                                            requestParams1.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "deleteflag"));
                                            requestParams1.put(Constants.filter_values, Arrays.asList(params.getId(), 0));

                                            fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParams1);
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
                            }//Create Custom Field Array
                            customfield = customJArr.toString();
                            if (!StringUtil.isNullOrEmpty(customfield) && !customfield.equals("[]")) {
                                JSONArray jcustomarray = new JSONArray(customfield);
                                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                                customrequestParams.put("customarray", jcustomarray);
                                customrequestParams.put("modulename", Constants.Acc_Product_modulename);
                                customrequestParams.put("moduleprimarykey", Constants.Acc_Productid);
                                customrequestParams.put("modulerecid", product.getID());
                                customrequestParams.put("moduleid", Constants.Acc_Product_Master_ModuleId);//isFixedAsset ? Constants.Acc_FixedAssets_AssetsGroups_ModuleId:
                                customrequestParams.put("companyid", companyid);
                                productMap.put("id", product.getID());
                                customrequestParams.put("customdataclasspath", Constants.Acc_Product_custom_data_classpath);
                                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                    productMap.put("accproductcustomdataref", product.getID());
                                    productresult = accProductObj.updateProduct(productMap);
                                }
                                HashMap<String, Object> customHistoryParams = new HashMap<String, Object>();
                                customHistoryParams.put("productId", product.getID());
                                customHistoryParams.put("customarray", jcustomarray);
                                maintainCustomFieldHistoryForProduct(requestParams, customHistoryParams);
                            }
                        }// Product Code check                        

                        //BOM Product Map
                        JSONArray bomarray = new JSONArray();
                        JSONObject bomobj = new JSONObject();
                        bomobj.put("bomproductpid", bompid);
                        bomobj.put("bomproductname", bomProductName);
                        bomobj.put("bomproductdesc", bomProductDesc);
                        bomobj.put("bomProductType", bomProductType);
                        bomobj.put("bomProductPurchasePrice", bomProductPurchasePrice);
                        bomobj.put("compQuantity", compQuantity);
                        bomobj.put("comppercent", comppercent);
                        bomobj.put("compactualQuantity", compactualQuantity);
                        bomobj.put("comptotal", comptotal);
                        bomobj.put("recylequantity", recylequantity);
                        bomobj.put("inventoryquantiy", compactualQuantity);  //Inventory Quantiy & Actual Quantity is the same
                        bomobj.put("remainingquantity", remainingquantity);
                        bomarray.put(bomobj);
                        System.out.println(bomProductName);
                        // for calculating total cost of bom product
                        productInitialPurchasePrise += comptotal;

                        //Save BOM Products of an Assembly Product  
                        if (product != null) {
                            if (product.getProducttype().getID().equals(Producttype.ASSEMBLY)) {
                                saveImportAssemblyProduct(bomarray, product, updateExistingRecordFlag, companyid);
                            }
                        } else {
                            if (!prevProductCode.equalsIgnoreCase(productID)) {  //ERP-11477
                                throw new AccountingException("This is not an Assembly Product.");
                            } else {
                                throw new AccountingException("Product ID '" + productID + "' already exists.");   //ERP-11477
                            }
                        }
                    } catch (Exception ex) {// Inner Try-Catch
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
                }// if(cnt!=0)
                else {                               // the below line is commited due to not proparely showing header in error log file . ERP-11483
                    String[] recarr = null;
                    if (delimiterType.equalsIgnoreCase("Bar")) {
                        recarr = record.split("\\|");
                    } else {
                        recarr = record.split(",");
                    }
                    failedRecords.append(createCSVrecord(recarr) + "\"Error Message\"");
                }
                cnt++;
                if (count == limit) {
                    txnManager.commit(status);
                    // for reset counter of transaction limit
                    count = 1;
                    def = new DefaultTransactionDefinition();
                    def.setName("import_Tx");
                    def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
                    status = txnManager.getTransaction(def);
                } else {
                    count++;
                }
            }//while

            // for saving BOM initial purchase price
            if (!prevProductCode.equalsIgnoreCase("") && productInitialPurchasePrise > 0 && product != null) {
                if (productInitialPurchasePrise > 0) {
                    HashMap<String, Object> initialPurchasePriceMap = new HashMap<String, Object>();
                    initialPurchasePriceMap.put("productid", product.getID());
                    initialPurchasePriceMap.put("companyid", companyid);
                    initialPurchasePriceMap.put("carryin", true);
                    initialPurchasePriceMap.put("price", productInitialPurchasePrise);
                    initialPurchasePriceMap.put("applydate", authHandler.getConstantDateFormatter().parse(authHandler.getConstantDateFormatter().format(ondate)));
                    initialPurchasePriceMap.put("affecteduser", "-1");
                    initialPurchasePriceMap.put("currencyid", currencyId);
                    initialPurchasePriceMap.put("uomid", product.getUnitOfMeasure().getID());
                    accProductObj.addPriceList(initialPurchasePriceMap);
                }
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
        } catch (Exception ex) {// Outer Try-Catch
            if (!commitedEx) { //if exception occurs during commit then dont call rollback
                txnManager.rollback(status);
            }
            issuccess = false;
            msg = "" + ex.getMessage();

            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Importing Records.");
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
                logDataMap.put("Module", Constants.Acc_Product_Master_ModuleId);
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userId);
                logDataMap.put("Company", companyid);
                importDao.saveImportLog(logDataMap);
                txnManager.commit(lstatus);
            } catch (Exception ex) {
                try {  //ERP-12252
                    txnManager.rollback(lstatus);
                } catch (Exception ee) {
                    Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ee);
                }
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", getActualFileName(fileName));
                returnObj.put("Module", Constants.Acc_Product_Master_ModuleId);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }//finally
        return returnObj;
    }
    
    public JSONObject importAssemblyProductWithoutBOMCSV(HashMap<String, Object> requestParams) throws AccountingException, IOException, SessionExpiredException, JSONException {
        JSONObject jobj = new JSONObject();
        return jobj;
    }

    public JSONObject importProductPriceCSV(HashMap<String, Object> requestParam) throws JSONException, AccountingException, IOException {
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
        int limit = Constants.Transaction_Commit_Limit;
        int count = 1;
        String companyid = requestParam.get("companyid").toString();
        String currencyId = requestParam.get("currencyId").toString();
        String userId = requestParam.get("userid").toString();
//        String currencyId = sessionHandlerImpl.getCurrencyID(request);
//        String companyid = sessionHandlerImpl.getCompanyid(request);
//        String userId = sessionHandlerImpl.getUserid(request);

        JSONObject jobj = new JSONObject();
        jobj = (JSONObject) requestParam.get("jobj");
        String fileName = jobj.getString("filename");
        String masterPreference = requestParam.get("masterPreference").toString();
        String timzonediff = jobj.getString("timeZoneDifferenceId");
        String dateformateproduct = jobj.getString("dateformateproduct");
        JSONObject returnObj = new JSONObject();

        try {
            String dateFormat = null, dateFormatId = dateformateproduct;
            if (!StringUtil.isNullOrEmpty(dateFormatId)) {

                KwlReturnObject kdfObj = accountingHandlerDAOobj.getObject(KWLDateFormat.class.getName(), dateFormatId);
                KWLDateFormat kdf = (KWLDateFormat) kdfObj.getEntityList().get(0);

                dateFormat = kdf != null ? kdf.getJavaForm() : null;
            }

            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
//            sdf.setTimeZone(TimeZone.getTimeZone("GMT" + timzonediff));//sessionHandlerImpl.getTimeZoneDifference(request)
            DateFormat df = sdf; // For ERP-13295
            fileInputStream = new FileInputStream(jobj.getString("FilePath"));
            br = new BufferedReader(new InputStreamReader(fileInputStream));
            String record = "";
            int cnt = 0;

            StringBuilder failedRecords = new StringBuilder();

            HashMap<String, Integer> columnConfig = new HashMap<String, Integer>();
            JSONArray jSONArray = jobj.getJSONArray("resjson");
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);

                columnConfig.put(jSONObject.getString("dataindex"), jSONObject.getInt("csvindex"));
            }

            HashMap currencyMap = getCurrencyMap();

            while ((record = br.readLine()) != null) {
                String[] recarr = record.split(",");
                if (cnt == 0) {
                    failedRecords.append(createCSVrecord(recarr) + "\"Error Message\"");
                }
                if (cnt != 0) {
                    try {
                        currencyId = currencyId;   // sessionHandlerImpl.getCurrencyID(request);

                        String productID = "";
                        if (columnConfig.containsKey("productid")) {
                            productID = recarr[(Integer) columnConfig.get("productid")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(productID)) {
                                productID = productID.replaceAll("\"", "");
                            } else {
                                throw new AccountingException("Product ID is not available.");
                            }
                        } else {
                            throw new AccountingException("Product ID column is not found.");
                        }

                        String customerId = "";
                        if (columnConfig.containsKey("customer")) {
                            String customerCode = recarr[(Integer) columnConfig.get("customer")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(customerCode)) {
                                customerId = getCustomerIDByCode(customerCode, companyid);
                                if (StringUtil.isNullOrEmpty(customerId)) {
                                    throw new AccountingException("Customer is not found for Code " + customerCode);
                                }
                            } else {
                                if (!masterPreference.equalsIgnoreCase("1")) {
                                    throw new AccountingException("Customer is not available.");
                                }
                            }
                        }

                        String vendorId = "";
                        if (columnConfig.containsKey("vendor")) {
                            String vendorCode = recarr[(Integer) columnConfig.get("vendor")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(vendorCode)) {
                                vendorId = getVendorIDByCode(vendorCode, companyid);
                                if (StringUtil.isNullOrEmpty(vendorId)) {
                                    throw new AccountingException("Vendor is not found for Code " + vendorCode);
                                }
                            } else {
                                if (!masterPreference.equalsIgnoreCase("1")) {
                                    throw new AccountingException("Vendor is not available.");
                                }
                            }
                        }

                        String productPurchasePrice = "";
                        if (columnConfig.containsKey("purchasePrice")) {
                            productPurchasePrice = recarr[(Integer) columnConfig.get("purchasePrice")].replaceAll("\"", "").trim();
//                            if (StringUtil.isNullOrEmpty(productPurchasePrice)) {
//                                throw new AccountingException("Product Purchase Price is not available");
//                            }
                        } else {
                            throw new AccountingException("Purchase Price column is not found.");
                        }

                        String productSalesPrice = "";
                        if (columnConfig.containsKey("salesPrice")) {
                            productSalesPrice = recarr[(Integer) columnConfig.get("salesPrice")].replaceAll("\"", "").trim();

//                            if (StringUtil.isNullOrEmpty(productSalesPrice)) {
//                                throw new AccountingException("Product Sales Price is not available");
//                            }
                        } else {
                            throw new AccountingException("Sales Price column is not found.");
                        }

                        Date productPriceDate = null;
                        if (columnConfig.containsKey("applyDate")) {
                            String productPriceDateStr = recarr[(Integer) columnConfig.get("applyDate")].replaceAll("\"", "").trim();

                            if (StringUtil.isNullOrEmpty(productPriceDateStr)) {
                                throw new AccountingException("Product Price Date is not available");
                            } else {
                                productPriceDate = df.parse(productPriceDateStr);
                            }
                        } else {
                            throw new AccountingException("Applicable Date column is not found.");
                        }

                        if (columnConfig.containsKey("currencyName")) {
                            String productPriceCurrencyStr = recarr[(Integer) columnConfig.get("currencyName")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(productPriceCurrencyStr)) {
                                currencyId = getCurrencyId(productPriceCurrencyStr, currencyMap);

                                if (StringUtil.isNullOrEmpty(currencyId)) {
                                    throw new AccountingException("Currency format you entered is not correct. it should be like \\\"SG Dollar (SGD)\\\"");//(messageSource.getMessage("acc.field.ImportProductExceptionFormat", null, RequestContextUtils.getLocale(request)));
                                }
                            } else {
                                if (!masterPreference.equalsIgnoreCase("1")) {
                                    throw new AccountingException("Currency is not available.");
                                }
                            }
                        }

                        // getting product object
                        KwlReturnObject result = accProductObj.getProductIDCount(productID, companyid,false);
                        int nocount = result.getRecordTotalCount();
                        if (nocount == 0) {
                            throw new AccountingException("Product ID '" + productID + "' not exists.");
                        }
                        Product product = (Product) result.getEntityList().get(0);
                        
                        /**
                         * Below Code is written to Validate UOM sent in csv
                         * file if UOM is present in master then the uomid is
                         * fetched from db and passed to json to save the price
                         * of product mapped with UOM.If uom is not present then
                         * for service type product id of N/A Uom is set and in
                         * case of special rate for customer stock uomid is set
                         * ERM-389 / ERP-35140.
                         */
                        ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences", companyid);
                        String uomName = "";
                        String uomid = "";
                        if (columnConfig.containsKey("uom")) {
                            uomName = recarr[(Integer) columnConfig.get("uom")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(uomName)) {
                                JSONObject paramsJobj = new JSONObject();
                                paramsJobj.put("companyid", companyid);
                                paramsJobj.put("uomname", uomName);
                                uomid = getUOMId(paramsJobj);
                                if (StringUtil.isNullOrEmpty(uomid)) {
                                    throw new AccountingException("UOM " + uomName + " not exists. ");
                                }
                            } else if (product.getProducttype().getID().equalsIgnoreCase(Producttype.SERVICE)) {
                                JSONObject paramsJobj = new JSONObject();
                                paramsJobj.put("companyid", companyid);
                                paramsJobj.put("uomname", "N/A");
                                uomid = getUOMId(paramsJobj);
                            } else if (extraPref.isBandsWithSpecialRateForSales()) {
                                uomid = product.getUnitOfMeasure().getID();
                            }
                        } else if (product.getProducttype().getID().equalsIgnoreCase(Producttype.SERVICE)) {
                            JSONObject paramsJobj = new JSONObject();
                            paramsJobj.put("companyid", companyid);
                            paramsJobj.put("uomname", "N/A");
                            uomid = getUOMId(paramsJobj);
                        } else if (extraPref.isBandsWithSpecialRateForSales()) {
                            uomid = product.getUnitOfMeasure().getID();
                        } else {                                                //if Special rate is not activated in company preferences and UOM colomn is not found then error message is shown.
                            throw new AccountingException("UOM column is not found.");
                        }
                        // For Purchase Price
                        if (productPurchasePrice.length() > 0) {
                            HashMap<String, Object> requestParams = requestParam;// AccountingManager.getGlobalParams(request);
                            requestParams.put("productid", product.getID());
                            requestParams.put("carryin", true);
                            requestParams.put("applydate", productPriceDate);
                            requestParams.put("price", Double.parseDouble(productPurchasePrice));
                            requestParams.put("currencyid", currencyId);
                            requestParams.put("companyid", companyid);
                            requestParams.put("uomid", uomid);
                            if (!StringUtil.isNullOrEmpty(vendorId)) {
                                // Purchase Price for Vendor
                                requestParams.put("affecteduser", vendorId);
                            } else {
                                // Purchase Price for All
                                requestParams.put("affecteduser", "-1");
                            }

                            KwlReturnObject priceListResult = accProductObj.getPriceListEntry(requestParams);
                            List purchasePriceList = priceListResult.getEntityList();

                            HashMap<String, Object> purchacePriceMap = new HashMap<String, Object>();
                            purchacePriceMap.put("price", Double.parseDouble(productPurchasePrice));

                            if (purchasePriceList.size() <= 0) {
                                purchacePriceMap.put("productid", product.getID());
                                purchacePriceMap.put("companyid", companyid);
                                purchacePriceMap.put("carryin", true);
                                purchacePriceMap.put("applydate", productPriceDate);
                                purchacePriceMap.put("currencyid", currencyId);
                                purchacePriceMap.put("uomid", uomid);
                                if (!StringUtil.isNullOrEmpty(vendorId)) {
                                    // Purchase Price for Vendor
                                    purchacePriceMap.put("affecteduser", vendorId);
                                } else {
                                    // Purchase Price for All
                                    purchacePriceMap.put("affecteduser", "-1");
                                }
                                accProductObj.addPriceList(purchacePriceMap);
                            } else {
                                PriceList purchasePrice = (PriceList) purchasePriceList.get(0);
                                purchacePriceMap.put("priceid", purchasePrice.getID());
                                accProductObj.updatePriceList(purchacePriceMap);
                            }
                        }

                        // For Sales Price
                        if (productSalesPrice.length() > 0) {
                            HashMap<String, Object> requestParams = requestParam; //AccountingManager.getGlobalParams(request);
                            requestParams.put("productid", product.getID());
                            requestParams.put("carryin", false);
                            requestParams.put("applydate", productPriceDate);
                            requestParams.put("price", Double.parseDouble(productSalesPrice));
                            requestParams.put("currencyid", currencyId);
                            requestParams.put("companyid", companyid);
                            requestParams.put("uomid", uomid);
                            if (!StringUtil.isNullOrEmpty(customerId)) {
                                // Sales Price for customer
                                requestParams.put("affecteduser", customerId);
                            } else {
                                // Sales Price for All
                                requestParams.put("affecteduser", "-1");
                            }

                            KwlReturnObject salesPriceListResult = accProductObj.getPriceListEntry(requestParams);
                            List salesPriceList = salesPriceListResult.getEntityList();

                            HashMap<String, Object> salesPriceMap = new HashMap<String, Object>();
                            salesPriceMap.put("price", Double.parseDouble(productSalesPrice));

                            if (salesPriceList.size() <= 0) {
                                salesPriceMap.put("productid", product.getID());
                                salesPriceMap.put("companyid", companyid);
                                salesPriceMap.put("carryin", false);
                                salesPriceMap.put("applydate", productPriceDate);
                                salesPriceMap.put("currencyid", currencyId);
                                salesPriceMap.put("uomid", uomid);
                                if (!StringUtil.isNullOrEmpty(customerId)) {
                                    // Sales Price for customer
                                    salesPriceMap.put("affecteduser", customerId);
                                } else {
                                    // Sales Price for All
                                    salesPriceMap.put("affecteduser", "-1");
                                }
                                accProductObj.addPriceList(salesPriceMap);
                            } else {
                                PriceList salesPrice = (PriceList) salesPriceList.get(0);
                                salesPriceMap.put("priceid", salesPrice.getID());
                                accProductObj.updatePriceList(salesPriceMap);
                            }
                        }
                        System.out.println(product.getID());
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
                if (count == limit) {
                    txnManager.commit(status);

                    // for reset counter of transaction limit
                    count = 1;
                    def = new DefaultTransactionDefinition();
                    def.setName("import_Tx");
                    def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
                    status = txnManager.getTransaction(def);
                } else {
                    count++;
                }
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
            if (!commitedEx) { //if exception occurs during commit then dont call rollback
                txnManager.rollback(status);
            }
            issuccess = false;
            msg = "" + ex.getMessage();

            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Importing Records.");
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
                logDataMap.put("Module", Constants.Acc_Product_Price_List_ModuleId);
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userId);
                logDataMap.put("Company", companyid);
                importDao.saveImportLog(logDataMap);

                String tableName = importDao.getTableName(fileName);
                importDao.removeFileTable(tableName); // Remove table after importing all records

                txnManager.commit(lstatus);
            } catch (Exception ex) {
                txnManager.rollback(lstatus);
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", getActualFileName(fileName));
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return returnObj;
    }

    private String getCustomerIDByCode(String customerCode, String companyID) throws AccountingException {
        String customerID = "";
        try {
            if (!StringUtil.isNullOrEmpty(customerCode) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accProductObj.getCustomerIDByCode(customerCode, companyID);
                List list = retObj.getEntityList();
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    customerID = (String) itr.next();
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Customer");
        }
        return customerID;
    }

    private String getVendorIDByCode(String vendorCode, String companyID) throws AccountingException {
        String vendorID = "";
        try {
            if (!StringUtil.isNullOrEmpty(vendorCode) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accProductObj.getVendorIDByCode(vendorCode, companyID);
                List list = retObj.getEntityList();
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    vendorID = (String) itr.next();
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Vendor");
        }
        return vendorID;
    }

    //Save Assembly Product
    public void saveImportAssemblyProduct(JSONArray bomarray, Product assemblyProduct, boolean updateExistingRecordFlag, String companyid) throws ServiceException {
        try {
//            if (updateExistingRecordFlag) {  //This function need to implement to update inventory in case of update existing product
//                updateImportBillofMaterialsInventory(bomarray, assemblyProduct, companyid);
//            }
//                accProductObj.deleteProductAssembly(assemblyProduct.getID());

            HashMap<String, Object> assemblyMap = new HashMap<String, Object>();
            for (int i = 0; i < bomarray.length(); i++) {
                JSONObject obj = new JSONObject();
                obj = (JSONObject) bomarray.get(i);

                assemblyMap.put("productid", assemblyProduct.getID());

                if (!StringUtil.isNullObject(obj.get("bomproductpid"))) {
                    assemblyMap.put("subproductid", (String) obj.get("bomproductpid"));
                }

                if (!StringUtil.isNullObject(obj.get("compQuantity"))) {
                    assemblyMap.put("quantity", (Double) obj.get("compQuantity"));
                }

                if (!StringUtil.isNullObject(obj.get("comppercent"))) {
                    assemblyMap.put("percentage", (Double) obj.get("comppercent"));
                }

                if (!StringUtil.isNullObject(obj.get("compactualQuantity"))) {
                    assemblyMap.put("actualquantity", (Double) obj.get("compactualQuantity"));
                }

                if (!StringUtil.isNullObject(obj.get("inventoryquantiy"))) {
                    assemblyMap.put("inventoryquantiy", (Double) obj.get("inventoryquantiy"));
                }

                if (!StringUtil.isNullObject(obj.get("recylequantity"))) {
                    assemblyMap.put("recylequantity", obj.get("recylequantity"));
                }

                if (!StringUtil.isNullObject(obj.get("remainingquantity"))) {
                    assemblyMap.put("remainingquantity", obj.get("remainingquantity"));
                }

                accProductObj.saveProductAssembly(assemblyMap); //Save Assembly Product
            }//for
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveAssemblyProduct : " + ex.getMessage(), ex);
        }
    }

    public Product getProductByProductName(String companyid, String productTypeID) throws AccountingException {
        Product product = null;
        try {
            if (!StringUtil.isNullOrEmpty(productTypeID)) {
                KwlReturnObject retObj = accProductObj.getProductByProductName(companyid, productTypeID);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    product = (Product) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Product Details");
        }
        return product;
    }

    public Producttype getProductTypeByProductID(String productTypeID) throws AccountingException {
        Producttype producttype = null;
        try {
            if (!StringUtil.isNullOrEmpty(productTypeID)) {
                KwlReturnObject retObj = accProductObj.getProductTypeByProductID(productTypeID);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    producttype = (Producttype) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Product Type");
        }
        return producttype;
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
            if (StringUtil.isNullOrEmpty(ext)) {
                ext = filename.substring(filename.lastIndexOf("."));
            }
            filename = filename.substring(0, filename.lastIndexOf("."));

            java.io.FileOutputStream failurefileOut = new java.io.FileOutputStream(destinationDirectory + "/" + filename + ImportLog.failureTag + ext);
            failurefileOut.write(failedRecords.toString().getBytes());
            failurefileOut.flush();
            failurefileOut.close();
        } catch (Exception ex) {
            System.out.println("\nError file write [success/failed] " + ex);
        }
    }

    public Product maintainCustomFieldHistoryForProduct(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        String productId = request.getParameter("productId");
        String fieldId = request.getParameter("fieldId");
        String value = request.getParameter("value");
        String applyDateString = request.getParameter("applyDate");
        String creationDateString = request.getParameter("creationDate");
        String loginId = sessionHandlerImpl.getUserid(request);
        String moduleIdString = request.getParameter("moduleId");
        String customfield = request.getParameter("customfield");
        int moduleId = 0;
        if (!StringUtil.isNullOrEmpty(moduleIdString)) {
            moduleId = Integer.parseInt(moduleIdString);
        }
        Date applyDate = null;
        Date creationDate = null;
        if (!StringUtil.isNullOrEmpty(applyDateString)) {
            try {
                applyDate = authHandler.getDateFormatter(request).parse(applyDateString);
            } catch (ParseException ex) {
                Logger.getLogger(ImportProduct.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (!StringUtil.isNullOrEmpty(creationDateString)) {
            try {
                creationDate = authHandler.getDateFormatter(request).parse(creationDateString);
            } catch (ParseException ex) {
                Logger.getLogger(ImportProduct.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put("productId", productId);
        requestParams.put("fieldId", fieldId);
        requestParams.put("value", value);
        requestParams.put("applyDate", applyDate);
        requestParams.put("creationDate", creationDate);
        requestParams.put("loginId", loginId);
        requestParams.put("moduleId", moduleId);
        KwlReturnObject fieldReturnObject = accProductObj.getCustomFieldHistoryForProduct(requestParams);
        List list = fieldReturnObject.getEntityList();
        if (list.size() > 0) {
            accProductObj.deleteCustomFieldHistoryForProduct(requestParams);
        }
        KwlReturnObject returnObject = accProductObj.maintainCustomFieldHistoryForProduct(requestParams);
        ProductCustomFieldHistory customFieldHistory = (ProductCustomFieldHistory) returnObject.getEntityList().get(0);
        Product product = null;
        if (customFieldHistory != null) {
            product = customFieldHistory.getProduct();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy");
        Date currDate = new Date();
        try {
            applyDate = sdf.parse(sdf.format(applyDate));
            currDate = sdf.parse(sdf.format(currDate));
        } catch (ParseException ex) {
            Logger.getLogger(ImportProduct.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (applyDate.equals(currDate)) {// we will update here AccProductCustomData values only in this condition
            saveCustomFieldData(request);
        }
        return product;
    }

    public void saveCustomFieldData(HttpServletRequest request) {
        String customfield = request.getParameter("customfield");
        if (!StringUtil.isNullOrEmpty(customfield)) {
            try {
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                String productId = request.getParameter("productId");
                customrequestParams = AccountingManager.getGlobalParams(request);
                JSONArray jcustomarray = new JSONArray(customfield);
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_Product_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_Productid);
                customrequestParams.put("modulerecid", productId);
                customrequestParams.put("moduleid", Constants.Acc_Product_Master_ModuleId);
                customrequestParams.put("id", productId);
                customrequestParams.put("customdataclasspath", Constants.Acc_Product_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
            } catch (ServiceException ex) {
                Logger.getLogger(ImportProduct.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SessionExpiredException ex) {
                Logger.getLogger(ImportProduct.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JSONException ex) {
                Logger.getLogger(ImportProduct.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void maintainCustomFieldHistoryForProduct(HashMap<String, Object> requestParams1, HashMap<String, Object> customrequestParams) {
        try {
            JSONArray jcustomarray = (JSONArray) customrequestParams.get("customarray");
            String loginId = requestParams1.get("logId").toString();   // sessionHandlerImpl.getUserid(request);
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy");
            Date applyDate = sdf.parse(requestParams1.get("ApplyDate").toString()); //authHandler.getDateOnlyFormatter(request).parse(authHandler.getDateOnlyFormatter(request).format(new Date()));
            for (int i = 0; i < jcustomarray.length(); i++) {
                HashMap<String, Object> requestParams = requestParams1;
                String productId = "";
                String fieldId = "";
                int moduleId = 30;
                String value = "";
                JSONObject jobj = jcustomarray.getJSONObject(i);
                if (jobj != null) {
                    productId = (String) customrequestParams.get("productId");
                    fieldId = jobj.getString("fieldid");
                    moduleId = 30;
                    int fieldType = jobj.getInt("xtype");
                    value = jobj.getString("fieldDataVal");
                    requestParams.put("productId", productId);
                    requestParams.put("fieldId", fieldId);
                    requestParams.put("value", value);
                    requestParams.put("applyDate", applyDate);
                    requestParams.put("moduleId", moduleId);
                    requestParams.put("creationDate", applyDate);
                    requestParams.put("loginId", loginId);
                    KwlReturnObject fieldReturnObject = accProductObj.getCustomFieldHistoryForProduct(requestParams);
                    List list = fieldReturnObject.getEntityList();
                    if (list.size() > 0) {
                        accProductObj.deleteCustomFieldHistoryForProduct(requestParams);
                    }
                    if ((fieldType == 1 || fieldType == 2) && !StringUtil.isNullOrEmpty(value)) {
                        KwlReturnObject returnObject = accProductObj.maintainCustomFieldHistoryForProduct(requestParams);
                    }
//                    else if((fieldType == 1 || fieldType == 2) && StringUtil.isNullOrEmpty(value)){
//                        KwlReturnObject returnObject = accProductObj.deleteCustomFieldHistoryForProduct(requestParams);
//                    }
                }

            }
        } catch (Exception e) {
            Logger.getLogger(ImportProduct.class.getName()).log(Level.SEVERE, null, e);
        }
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
            Logger.getLogger(ImportProduct.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Unit of Measure");
        }
        return uom;
    }

    private InventoryWarehouse getInventoryWarehouseByName(String inventoryWarehouse, String companyID) throws AccountingException {
        InventoryWarehouse invWHouse = null;
        try {
            if (!StringUtil.isNullOrEmpty(inventoryWarehouse) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accProductObj.getInventoryWarehouseByName(inventoryWarehouse, companyID);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    invWHouse = (InventoryWarehouse) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(ImportProduct.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Inventory Warehouse");
        }
        return invWHouse;
    }

    private Vendor getVendorByName(String vendorName, String companyID) throws AccountingException {
        Vendor vendor = null;
        try {
            if (!StringUtil.isNullOrEmpty(vendorName) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accProductObj.getVendorByName(vendorName, companyID);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    vendor = (Vendor) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(ImportProduct.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Vendor");
        }
        return vendor;
    }

    private InventoryLocation getInventoryLocationByName(String inventoryLocation, String companyID) throws AccountingException {
        InventoryLocation invLoc = null;
        try {
            if (!StringUtil.isNullOrEmpty(inventoryLocation) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accProductObj.getInventoryLocationByName(inventoryLocation, companyID);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    invLoc = (InventoryLocation) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(ImportProduct.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Inventory Location");
        }
        return invLoc;
    }

    private UOMschemaType getUOMschemaTypeByName(String uomSchemaTypeName, String companyID) throws AccountingException {
        UOMschemaType uomSchemaType = null;
        try {
            if (!StringUtil.isNullOrEmpty(uomSchemaTypeName) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accUomObj.getUOMschemaTypeByName(uomSchemaTypeName, companyID);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    uomSchemaType = (UOMschemaType) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(ImportProduct.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching UOM Schema");
        }
        return uomSchemaType;
    }

    private String getCurrencyId(String currencyName, HashMap currencyMap) {
        String currencyId = "";
        if (currencyMap != null && currencyMap.containsKey(currencyName)) {
            currencyId = currencyMap.get(currencyName).toString();
        }
        return currencyId;
    }

    private Account getAccountByName(String accountName, String companyID) throws AccountingException {
        Account account = null;
        try {
            if (!StringUtil.isNullOrEmpty(accountName) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accProductObj.getAccountByName(accountName, companyID);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    Iterator itr = retObj.getEntityList().iterator();
                    if (itr!=null && itr.hasNext()) {
                       String accountID = (String) itr.next();
                       KwlReturnObject custumObjresult = accountingHandlerDAOobj.getObject(Account.class.getName(), accountID);
                       account = (Account) custumObjresult.getEntityList().get(0);
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(ImportProduct.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Account");
        }
        return account;
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
            Logger.getLogger(ImportProduct.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Product");
        }
        return product;
    }

    private Producttype getProductTypeByName(String productTypeName) throws AccountingException {
        Producttype producttype = null;
        try {
            if (!StringUtil.isNullOrEmpty(productTypeName)) {
                KwlReturnObject retObj = accProductObj.getProductTypeByName(productTypeName);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    producttype = (Producttype) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(ImportProduct.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Product Type");
        }
        return producttype;
    }

    public HashMap getCurrencyMap() throws ServiceException {
        HashMap currencyMap = new HashMap();
        KwlReturnObject returnObject = accProductObj.getCurrencies();
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
    public Map<Integer, Frequency> getCCFrequencyMap() throws ServiceException {
        Map<Integer, Frequency> fMap = new HashMap<>();
        List<Frequency> frequencyList = accProductObj.getFrequencies();
        for (Frequency frequency : frequencyList) {
            fMap.put(frequency.getId(), frequency);
        }
        return fMap;
    }
    public JSONObject importProductopeningqtyRecords(HashMap<String, Object> requestParams) throws AccountingException, IOException, SessionExpiredException, JSONException {
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
        String companyid = requestParams.get("companyid").toString();
        String currencyId = requestParams.get("currencyId").toString();
        String userId = requestParams.get("userid").toString();
        JSONObject jobj = new JSONObject();
        jobj = (JSONObject) requestParams.get("jobj");
        String dateformateproduct = jobj.getString("dateformateproduct");
        String masterPreference = requestParams.get("masterPreference").toString();
        String fileName = jobj.getString("filename");
        String delimiterType = requestParams.get("delimiterType").toString();
        JSONObject returnObj = new JSONObject();
        try {
            Date newUserDate = new Date();
            KwlReturnObject custumObjresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company1 = (Company) custumObjresult.getEntityList().get(0);
            if (company1.getCreator() != null) {
                newUserDate = authHandler.getUserNewDate(null, company1.getCreator().getTimeZone()!=null?company1.getCreator().getTimeZone().getDifference() : company1.getTimeZone().getDifference());
            }
            boolean isBatchForProduct = false;
            boolean isSerialForProduct = false;
            boolean isLocationForProduct = false;
            boolean isWarehouseForProduct = false;
            boolean isRowForProduct = false;
            boolean isRackForProduct = false;
            boolean isBinForProduct = false;
            KwlReturnObject kmsg = null;
            String dateFormat = null, dateFormatId = dateformateproduct;
            if (!StringUtil.isNullOrEmpty(dateFormatId)) {
                KwlReturnObject kdfObj = accountingHandlerDAOobj.getObject(KWLDateFormat.class.getName(), dateFormatId);
                KWLDateFormat kdf = (KWLDateFormat) kdfObj.getEntityList().get(0);
                dateFormat = kdf != null ? kdf.getJavaForm() : null;
            }

            fileInputStream = new FileInputStream(jobj.getString("FilePath"));
            br = new BufferedReader(new InputStreamReader(fileInputStream));
            String record = "";
            int cnt = 0;
            int count = 1;
            int limit = Constants.Transaction_Commit_Limit;

            StringBuilder failedRecords = new StringBuilder();

            HashMap<String, Integer> columnConfig = new HashMap<String, Integer>();
            JSONArray jSONArray = jobj.getJSONArray("resjson");
            List headArrayList = new ArrayList();
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
                headArrayList.add(jSONObject.get("csvheader"));

                columnConfig.put(jSONObject.getString("dataindex"), jSONObject.getInt("csvindex"));
            }
            failedRecords.append(createCSVrecord(headArrayList.toArray()) + "\"Error Message\"");//failedRecords.append("\"Row No.\","+createCSVrecord(fileData)+"\"Error Message\"");
            HashMap currencyMap = getCurrencyMap();
            while ((record = br.readLine()) != null) {
                if (cnt != 0) {
                    String[] recarr = record.split(",");
                    try {
//                        currencyId = currencyId;    //sessionHandlerImpl.getCurrencyID(request);
                        NewProductBatch productBatch = null;
                        String productBatchId = "";
                        String productID = "";
                        if (columnConfig.containsKey("productid")) {
                            productID = recarr[(Integer) columnConfig.get("productid")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(productID)) {
                                productID = productID.replaceAll("\"", "");
                            } else {
                                throw new AccountingException("Product ID is not available.");
                            }
                        } else {
                            throw new AccountingException("Product ID column is not found.");
                        }

                        // getting product object
                        KwlReturnObject result = accProductObj.getProductIDCount(productID, companyid,false);
                        int nocount = result.getRecordTotalCount();
                        if (nocount == 0) {
                            throw new AccountingException("productID '" + productID + "' not exists.");
                        }
                        Product product = (Product) result.getEntityList().get(0);
                        if (product != null) {
                            isBatchForProduct = product.isIsBatchForProduct();
                            isSerialForProduct = product.isIsSerialForProduct();
                            isLocationForProduct = product.isIslocationforproduct();
                            isWarehouseForProduct = product.isIswarehouseforproduct();
                            isRowForProduct = product.isIsrowforproduct();
                            isRackForProduct = product.isIsrackforproduct();
                            isBinForProduct = product.isIsbinforproduct();

                            String productDefaultLocationID = "";
                            if (columnConfig.containsKey("locationName")) {
                                String productDefaultLocationName = recarr[(Integer) columnConfig.get("locationName")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(productDefaultLocationName)) {
                                    InventoryLocation invLoc = getInventoryLocationByName(productDefaultLocationName, companyid);
                                    if (invLoc != null) {
                                        productDefaultLocationID = invLoc.getId();
                                    } else {
                                        if (masterPreference.equalsIgnoreCase("2")) {
                                            HashMap requestParam = requestParams;//AccountingManager.getGlobalParams(request);
                                            requestParam.put("id", "");
                                            requestParam.put("name", productDefaultLocationName);
                                            requestParam.put("parent", null);
                                            KwlReturnObject jeresult = accountingHandlerDAOobj.getObject(User.class.getName(), userId);
                                            User user = (User) jeresult.getEntityList().get(0);
                                            requestParam.put("user", user);
                                            KwlReturnObject locationResult = accMasterItemsDAOobj.addLocationItem(requestParam);
                                            invLoc = (InventoryLocation) locationResult.getEntityList().get(0);
                                            productDefaultLocationID = invLoc.getId();
                                        } else {
                                            if (masterPreference.equalsIgnoreCase("1")) {
                                                productDefaultLocationID = null;
                                            } else {
                                                throw new AccountingException("Default Location is not found for " + productDefaultLocationName);
                                            }
                                        }
                                    }
                                } else {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        productDefaultLocationID = null;
                                    } else {
                                        throw new AccountingException("Default Location is not available.");
                                    }
                                }
                            } else {
                                productDefaultLocationID = null;
                            }

                            String productDefaultWarehouseID = "";
                            if (columnConfig.containsKey("warehouseName")) {
                                String productDefaultWarehouseName = recarr[(Integer) columnConfig.get("warehouseName")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(productDefaultWarehouseName)) {
                                    InventoryWarehouse invWHouse = getInventoryWarehouseByName(productDefaultWarehouseName, companyid);
                                    if (invWHouse != null) {
                                        productDefaultWarehouseID = invWHouse.getId();
                                    } else {
                                        if (masterPreference.equalsIgnoreCase("2")) {
                                            HashMap requestParam = requestParams; //AccountingManager.getGlobalParams(request);
                                            requestParam.put("id", "");
                                            requestParam.put("name", productDefaultWarehouseName);
                                            requestParam.put("parent", null);
                                            requestParam.put("location", null);

                                            KwlReturnObject warehouseResult = accMasterItemsDAOobj.addWarehouseItem(requestParam);
                                            invWHouse = (InventoryWarehouse) warehouseResult.getEntityList().get(0);
                                            productDefaultWarehouseID = invWHouse.getId();
                                        } else {
                                            if (masterPreference.equalsIgnoreCase("1")) {
                                                productDefaultWarehouseID = null;
                                            } else {
                                                throw new AccountingException("Default Warehouse is not found for " + productDefaultWarehouseName);
                                            }
                                        }
                                    }
                                } else {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        productDefaultWarehouseID = null;
                                    } else {
                                        throw new AccountingException("Default Warehouse is not available.");
                                    }
                                }
                            } else {
                                productDefaultWarehouseID = null;
                            }
                            String productInitialQuantity = "";
                            if (columnConfig.containsKey("initialquantity")) {
                                productInitialQuantity = recarr[(Integer) columnConfig.get("initialquantity")].replaceAll("\"", "").trim();
                                if (StringUtil.isNullOrEmpty(productInitialQuantity)) {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        productInitialQuantity = "0";
                                    } else {
                                        if (product.getProducttype().equals(Producttype.SERVICE)) {
                                            productInitialQuantity = "";
                                        } else {
                                            throw new AccountingException("Product Initial Quantity is not available");
                                        }
                                    }
                                }
                            } else {
                                productInitialQuantity = "0";
                            }

                            if (productInitialQuantity.length() > 0) {
                                JSONObject inventoryjson = new JSONObject();
                                inventoryjson.put("productid", product.getID());
                                inventoryjson.put("quantity", Double.parseDouble(productInitialQuantity));
                                inventoryjson.put("baseuomquantity", Double.parseDouble(productInitialQuantity));
                                inventoryjson.put("baseuomrate", 1);
                                if (product.getUnitOfMeasure() != null) {
                                    inventoryjson.put("uomid", product.getUnitOfMeasure().getID());
                                }
                                inventoryjson.put("description", "Inventory Opened");
                                inventoryjson.put("carryin", true);
                                inventoryjson.put("defective", false);
                                inventoryjson.put("newinventory", true);
                                inventoryjson.put("companyid", companyid);
                                inventoryjson.put("updatedate", newUserDate);
                                accProductObj.addInventory(inventoryjson);

                                HashMap<String, Object> assemblyParams = requestParams; // AccountingManager.getGlobalParams(request);
                                assemblyParams.put("assembly", "");
                                assemblyParams.put("applydate", authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
                                assemblyParams.put("quantity", productInitialQuantity);
                                assemblyParams.put("memo", "Inventory Opened");
                                assemblyParams.put("refno", "");
                                assemblyParams.put("buildproductid", product.getID());
                                accProductObj.updateAssemblyInventory(assemblyParams);

                                if ((isLocationForProduct || isWarehouseForProduct)) {

                                    HashMap<String, Object> pdfTemplateMap = new HashMap<String, Object>();
                                    pdfTemplateMap.put("companyid", companyid);
                                    pdfTemplateMap.put("quantity", productInitialQuantity);
                                    pdfTemplateMap.put("location", productDefaultLocationID);
                                    pdfTemplateMap.put("product", product.getID());
                                    pdfTemplateMap.put("warehouse", productDefaultWarehouseID);
                                    pdfTemplateMap.put("name", "");
                                    pdfTemplateMap.put("isopening", true);
                                    pdfTemplateMap.put("transactiontype", "28");//This is GRN Type Tranction  
                                    pdfTemplateMap.put("ispurchase", true);
                                    kmsg = accCommonTablesDAO.saveNewBatchForProduct(pdfTemplateMap);

                                    if (kmsg != null && kmsg.getEntityList().size() != 0) {
                                        productBatch = (NewProductBatch) kmsg.getEntityList().get(0);
                                        productBatchId = productBatch.getId();
                                    }
                                    HashMap<String, Object> documentMap = new HashMap<String, Object>();
                                    documentMap.put("quantity", productInitialQuantity);
                                    documentMap.put("batchmapid", productBatchId);
                                    documentMap.put("documentid", product.getID());
                                    documentMap.put("transactiontype", "28");//This is GRN Type Tranction
                                    accCommonTablesDAO.saveBatchDocumentMapping(documentMap);
                                }
                            }
                        }
                        System.out.println(product.getID()); // remove after test
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
                } else {
                    String[] recarr = null;
                    if (delimiterType.equalsIgnoreCase("Bar")) {
                        recarr = record.split("\\|");
                    } else {
                        recarr = record.split(",");
                    }
                    failedRecords.append(createCSVrecord(recarr) + "\"Error Message\"");
                }
                cnt++;
                if (count == limit) {
                    txnManager.commit(status);
                    // for reset counter of transaction limit
                    count = 1;
                    def = new DefaultTransactionDefinition();
                    def.setName("import_Tx");
                    def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
                    status = txnManager.getTransaction(def);
                } else {
                    count++;
                }
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
            if (!commitedEx) { //if exception occurs during commit then dont call rollback
                txnManager.rollback(status);
            }
            issuccess = false;
            msg = "" + ex.getMessage();

            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Importing Records.");
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
                logDataMap.put("Module", Constants.Acc_Product_opening_stock_ModuleId);
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userId);
                logDataMap.put("Company", companyid);
                importDao.saveImportLog(logDataMap);

                String tableName = importDao.getTableName(fileName);
                importDao.removeFileTable(tableName); // Remove table after importing all records

                txnManager.commit(lstatus);
            } catch (Exception ex) {
                txnManager.rollback(lstatus);
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", getActualFileName(fileName));
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return returnObj;
    }
        public JSONObject importProductcsv(HashMap<String,Object> requestParams1) throws AccountingException, IOException, SessionExpiredException, JSONException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("import_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        boolean commitedEx = false;
        boolean issuccess = true;
        boolean isCurrencyColum = false;
        String msg = "";
        String customfield = "";
        FileInputStream fileInputStream = null;
        BufferedReader br = null;
       CsvReader csvReader=null;
        int total = 0, failed = 0;
        String companyid = requestParams1.get("companyid").toString();
        String currencyId = requestParams1.get("currencyId").toString();
        String userId = requestParams1.get("userid").toString();
        //DateFormat df = (DateFormat) requestParams1.get("dateFormat");
        JSONObject jobj = new JSONObject();
        jobj = (JSONObject) requestParams1.get("jobj");
        String masterPreference = requestParams1.get("masterPreference").toString();
        String delimiterType = requestParams1.get("delimiterType").toString();
        boolean updateExistingRecordFlag = false;
        if (!StringUtil.isNullOrEmpty(requestParams1.get("updateExistingRecordFlag").toString())) {
            updateExistingRecordFlag = Boolean.FALSE.parseBoolean(requestParams1.get("updateExistingRecordFlag").toString());
        }
        int limit = Constants.Transaction_Commit_Limit;
        int count = 1;
        String fileName = jobj.getString("filename");
        
        boolean isInventoryIntegrationOn=false;
        JSONObject returnObj = new JSONObject();

        try {
            Date newUserDate = new Date();
            KwlReturnObject custumObjresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company1 = (Company) custumObjresult.getEntityList().get(0);
            if (company1.getCreator() != null) {
                newUserDate = authHandler.getUserNewDate(null, company1.getCreator().getTimeZone()!=null?company1.getCreator().getTimeZone().getDifference() : company1.getTimeZone().getDifference());
            }
            KwlReturnObject extraCompanyPrefResult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraCompanyPrefResult.getEntityList().get(0);
            
            isInventoryIntegrationOn=extraCompanyPreferences.isActivateInventoryTab();
            
            String dateFormat = null, dateFormatId = requestParams1.get("dateFormat").toString();
            if (!StringUtil.isNullOrEmpty(dateFormatId)) {
                KwlReturnObject kdfObj = accountingHandlerDAOobj.getObject(KWLDateFormat.class.getName(), dateFormatId);
                KWLDateFormat kdf = (KWLDateFormat) kdfObj.getEntityList().get(0);
                dateFormat = kdf != null ? kdf.getJavaForm() : null;
            }
            DateFormat df = new SimpleDateFormat(dateFormat);
            
            fileInputStream = new FileInputStream(jobj.getString("FilePath"));
            //br = new BufferedReader(new InputStreamReader(fileInputStream));
            csvReader = new CsvReader(new InputStreamReader(fileInputStream), delimiterType);
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
            HashMap currencyMap = getCurrencyMap();
            
            Map<Integer, Frequency> frequencyMap = getCCFrequencyMap();

            while (csvReader.readRecord()) {
                if (cnt != 0) {
                    record=csvReader.getRawRecord();
                    record=record.replaceAll("\n", "");
                    String[] recarr = null;
                    if (delimiterType.equalsIgnoreCase("Bar")) {
                        recarr = record.split("\\|", -1);           //apply pattern as often as possible refer ticket - ERP-14275
                    } else {
                        recarr = record.split(",", -1);             //apply pattern as often as possible refer ticket - ERP-14275
                    }

                    try {
                        currencyId =currencyId;    // sessionHandlerImpl.getCurrencyID(request);

                        String productTypeID = "";
                        if (columnConfig.containsKey("type")) {
                            String productTypeName = recarr[(Integer) columnConfig.get("type")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(productTypeName)) {
                                throw new AccountingException("Product Type is not available");
                            }

                            Producttype producttype = getProductTypeByName(productTypeName);
                            if (producttype != null) {
                                productTypeID = producttype.getID();
                            } else {
                                throw new AccountingException("Product Type is not found for " + productTypeName);
                            }
                        } else {
                            throw new AccountingException("Product Type column is not found.");
                        }

                        String productName = "";
                        if (columnConfig.containsKey("productname")) {
                            productName = recarr[(Integer) columnConfig.get("productname")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(productName)) {
                                throw new AccountingException("Product Name is not available");
                            }
                        } else {
                            throw new AccountingException("Product Name column is not found.");
                        }

                        String productID = "";
                        if (columnConfig.containsKey("pid")) {
                            productID = recarr[(Integer) columnConfig.get("pid")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(productID)) {
                                throw new AccountingException("Product ID is not available");
                            }
                        } else {
                            throw new AccountingException("Product ID column is not found");
                        }

                        String productDescription = "";
                        if (columnConfig.containsKey("desc")) {
                            productDescription = recarr[(Integer) columnConfig.get("desc")].replaceAll("\"", "").trim();
                        }

                        String productUOMID = "";
                        UnitOfMeasure uom = null;
                        if (columnConfig.containsKey("uomname")) {
                            String productUOMName = recarr[(Integer) columnConfig.get("uomname")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(productUOMName)) {
                                uom = getUOMByName(productUOMName, companyid);
                                if (uom != null) {
                                    productUOMID = uom.getID();
                                } else {
                                    if (masterPreference.equalsIgnoreCase("2")) {
                                        HashMap<String, Object> uomMap = new HashMap<String, Object>();
                                        uomMap.put("uomname", StringUtil.DecodeText(productUOMName));
                                        uomMap.put("uomtype", StringUtil.DecodeText(productUOMName));
                                        uomMap.put("precision", 0);
                                        uomMap.put("companyid", companyid);

                                        KwlReturnObject uomresult = accUomObj.addUoM(uomMap);
                                        uom = (UnitOfMeasure) uomresult.getEntityList().get(0);
                                        productUOMID = uom.getID();
                                    } else {
                                        if (masterPreference.equalsIgnoreCase("1")) {
                                            productUOMID = "";
                                        } else {
                                            throw new AccountingException("Product Unit Of Measure is not found for " + productUOMName);
                                        }
                                    }
                                }
                            } else {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    productUOMID = "";
                                } else {
                                    if (productTypeID.equals(Producttype.SERVICE)) {
                                        productUOMID = "";
                                    } else {
                                        throw new AccountingException("Product Unit Of Measure is not available");
                                    }
                                }
                            }
                        } else {
                            productUOMID = "";
                        }
                        String casingUoMID = "";
                        UnitOfMeasure casinguom = null;
                        if (columnConfig.containsKey("casinguom")) {
                            String productUOMName = recarr[(Integer) columnConfig.get("casinguom")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(productUOMName)) {
                                casinguom = getUOMByName(productUOMName, companyid);
                                if (casinguom != null) {
                                    casingUoMID = casinguom.getID();
                                } else {
                                    if (masterPreference.equalsIgnoreCase("2")) {
                                        HashMap<String, Object> uomMap = new HashMap<String, Object>();
                                        uomMap.put("uomname", StringUtil.DecodeText(productUOMName));
                                        uomMap.put("uomtype", StringUtil.DecodeText(productUOMName));
                                        uomMap.put("precision", 0);
                                        uomMap.put("companyid", companyid);
                                        KwlReturnObject uomresult = accUomObj.addUoM(uomMap);
                                        casinguom = (UnitOfMeasure) uomresult.getEntityList().get(0);
                                        casingUoMID = casinguom.getID();
                                    } else {
                                        if (masterPreference.equalsIgnoreCase("1")) {
                                            casingUoMID = "";
                                        } else {
                                            throw new AccountingException("Product Unit Of Measure is not found for " + productUOMName);
                                        }
                                    }
                                }
                            } else {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    casingUoMID = "";
                                } else {
                                    if (productTypeID.equals(Producttype.SERVICE)) {
                                        casingUoMID = "";
                                    } else {
                                        throw new AccountingException("Product  Casing Unit Of Measure is not available");
                                    }
                                }
                            }
                        } else {
                            casingUoMID = "";
                        }
                        String innerUoMID = "";
                        UnitOfMeasure inneruom = null;
                        if (columnConfig.containsKey("inneruom")) {
                            String productUOMName = recarr[(Integer) columnConfig.get("inneruom")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(productUOMName)) {
                                inneruom = getUOMByName(productUOMName, companyid);
                                if (inneruom != null) {
                                    innerUoMID = inneruom.getID();
                                } else {
                                    if (masterPreference.equalsIgnoreCase("2")) {
                                        HashMap<String, Object> uomMap = new HashMap<String, Object>();
                                        uomMap.put("uomname", StringUtil.DecodeText(productUOMName));
                                        uomMap.put("uomtype", StringUtil.DecodeText(productUOMName));
                                        uomMap.put("precision", 0);
                                        uomMap.put("companyid", companyid);

                                        KwlReturnObject uomresult = accUomObj.addUoM(uomMap);
                                        inneruom = (UnitOfMeasure) uomresult.getEntityList().get(0);
                                        innerUoMID = inneruom.getID();
                                    } else {
                                        if (masterPreference.equalsIgnoreCase("1")) {
                                            innerUoMID = "";
                                        } else {
                                            throw new AccountingException("Product Inner Unit Of Measure is not found for " + productUOMName);
                                        }
                                    }
                                }
                            } else {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    innerUoMID = "";
                                } else {
                                    if (productTypeID.equals(Producttype.SERVICE)) {
                                        innerUoMID = "";
                                    } else {
                                        throw new AccountingException("Product Inner Unit Of Measure is not available");
                                    }
                                }
                            }
                        } else {
                            innerUoMID = "";
                        }
                        String PurchaseUOMID = "";
                        UnitOfMeasure purchaseruom = null;
                        if (columnConfig.containsKey("purchaseuom")) {
                            String productUOMName = recarr[(Integer) columnConfig.get("purchaseuom")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(productUOMName)) {
                                purchaseruom = getUOMByName(productUOMName, companyid);
                                if (purchaseruom != null) {
                                    PurchaseUOMID = purchaseruom.getID();
                                } else {
                                    if (masterPreference.equalsIgnoreCase("2")) {
                                        HashMap<String, Object> uomMap = new HashMap<String, Object>();
                                        uomMap.put("uomname", StringUtil.DecodeText(productUOMName));
                                        uomMap.put("uomtype", StringUtil.DecodeText(productUOMName));
                                        uomMap.put("precision", 0);
                                        uomMap.put("companyid", companyid);

                                        KwlReturnObject uomresult = accUomObj.addUoM(uomMap);
                                        purchaseruom = (UnitOfMeasure) uomresult.getEntityList().get(0);
                                        PurchaseUOMID = purchaseruom.getID();
                                    } else {
                                        if (masterPreference.equalsIgnoreCase("1")) {
                                            PurchaseUOMID = "";
                                        } else {
                                            throw new AccountingException("Product Purchase Unit Of Measure is not found for " + productUOMName);
                                        }
                                    }
                                }
                            } else {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    PurchaseUOMID = "";
                                } else {
                                    if (productTypeID.equals(Producttype.SERVICE)) {
                                        PurchaseUOMID = "";
                                    } else {
                                        throw new AccountingException("Product Purchase Unit Of Measure is not available");
                                    }
                                }
                            }
                        } else {
                            PurchaseUOMID = "";
                        }
                        String SalesUOMID = "";
                        UnitOfMeasure salesuom = null;
                        if (columnConfig.containsKey("salesuom")) {
                            String productUOMName = recarr[(Integer) columnConfig.get("salesuom")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(productUOMName)) {
                                salesuom = getUOMByName(productUOMName, companyid);
                                if (salesuom != null) {
                                    SalesUOMID = salesuom.getID();
                                } else {
                                    if (masterPreference.equalsIgnoreCase("2")) {
                                        HashMap<String, Object> uomMap = new HashMap<String, Object>();
                                        uomMap.put("uomname", StringUtil.DecodeText(productUOMName));
                                        uomMap.put("uomtype", StringUtil.DecodeText(productUOMName));
                                        uomMap.put("precision", 0);
                                        uomMap.put("companyid", companyid);

                                        KwlReturnObject uomresult = accUomObj.addUoM(uomMap);
                                        salesuom = (UnitOfMeasure) uomresult.getEntityList().get(0);
                                        SalesUOMID = salesuom.getID();
                                    } else {
                                        if (masterPreference.equalsIgnoreCase("1")) {
                                            SalesUOMID = "";
                                        } else {
                                            throw new AccountingException("Product Sales Unit Of Measure is not found for " + productUOMName);
                                        }
                                    }
                                }
                            } else {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    SalesUOMID = "";
                                } else {
                                    if (productTypeID.equals(Producttype.SERVICE)) {
                                        SalesUOMID = "";
                                    } else {
                                        throw new AccountingException("Product Sales Unit Of Measure is not available");
                                    }
                                }
                            }
                        } else {
                            SalesUOMID = "";
                        }

                        String casinguomvalue = "1";
                        if (columnConfig.containsKey("casinguom_value")) {
                            casinguomvalue = recarr[(Integer) columnConfig.get("casinguom_value")].replaceAll("\"", "").trim();
                        }
                        String inneruomvalue = "1";
                        if (columnConfig.containsKey("inneruom_value")) {
                            inneruomvalue = recarr[(Integer) columnConfig.get("inneruom_value")].replaceAll("\"", "").trim();
                        }
                        String stcokuomvalue = "1";
                        if (columnConfig.containsKey("stockuom_value")) {
                            stcokuomvalue = recarr[(Integer) columnConfig.get("stockuom_value")].replaceAll("\"", "").trim();
                        }

                        String productReorderLevel = "";
                        if (columnConfig.containsKey("reorderlevel")) {
                            productReorderLevel = recarr[(Integer) columnConfig.get("reorderlevel")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(productReorderLevel)) {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    productReorderLevel = "0";
                                } else {
                                    if (productTypeID.equals(Producttype.SERVICE)) {
                                        productReorderLevel = "0";
                                    } else {
                                        throw new AccountingException("Product Reorder Level is not available");
                                    }
                                }
                            }
                        } else {
                            productReorderLevel = "0";
                        }

                        String productReorderQuantity = "";
                        if (columnConfig.containsKey("reorderquantity")) {
                            productReorderQuantity = recarr[(Integer) columnConfig.get("reorderquantity")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(productReorderQuantity)) {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    productReorderQuantity = "0";
                                } else {
                                    if (productTypeID.equals(Producttype.SERVICE)) {
                                        productReorderQuantity = "0";
                                    } else {
                                        throw new AccountingException("Product Reorder Quantity is not available");
                                    }
                                }
                            }
                        } else {
                            productReorderQuantity = "0";
                        }

                        String productWarrantyPeriod = "";
                        if (columnConfig.containsKey("warrantyperiod")) {
                            productWarrantyPeriod = recarr[(Integer) columnConfig.get("warrantyperiod")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(productWarrantyPeriod)) {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    productWarrantyPeriod = "0";
                                } else {
                                    if (productTypeID.equals(Producttype.SERVICE)) {
                                        productWarrantyPeriod = "0";
                                    } else {
                                        throw new AccountingException("Product Warranty Period is not available");
                                    }
                                }
                            }
                        } else {
                            productWarrantyPeriod = "0";
                        }

                        String productSalesWarrantyPeriod = "";
                        if (columnConfig.containsKey("warrantyperiodsal")) {
                            productSalesWarrantyPeriod = recarr[(Integer) columnConfig.get("warrantyperiodsal")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(productSalesWarrantyPeriod)) {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    productSalesWarrantyPeriod = "0";
                                } else {
                                    if (productTypeID.equals(Producttype.SERVICE)) {
                                        productSalesWarrantyPeriod = "0";
                                    } else {
                                        throw new AccountingException("Product Sales Warranty Period is not available");
                                    }
                                }
                            }
                        } else {
                            productSalesWarrantyPeriod = "0";
                        }

                        String productLeadTime = "";
                        if (columnConfig.containsKey("leadtime")) {
                            productLeadTime = recarr[(Integer) columnConfig.get("leadtime")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(productLeadTime)) {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    productLeadTime = "0";
                                } else {
                                    if (productTypeID.equals(Producttype.SERVICE)) {
                                        productLeadTime = "0";
                                    } else {
                                        throw new AccountingException("Product Lead Time is not available");
                                    }
                                }
                            } else {
                                if (Integer.parseInt(productLeadTime) > 365) {
                                    throw new AccountingException("Product Lead Time should not be greater than 365");
                                } else if (Integer.parseInt(productLeadTime) < 0) {
                                    throw new AccountingException("Product Lead Time should not be less than 0");
                                }
                            }
                        } else {
                            productLeadTime = "0";
                        }

                        String productCycleCountInterval = "";
                        if (columnConfig.containsKey("ccountinterval")) {
                            productCycleCountInterval = recarr[(Integer) columnConfig.get("ccountinterval")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(productCycleCountInterval)) {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    productCycleCountInterval = "1";
                                } else {
                                    if (productTypeID.equals(Producttype.SERVICE)) {
                                        productCycleCountInterval = "1";
                                    } else {
                                        throw new AccountingException("Product Cycle Count Interval is not available");
                                    }
                                }
                            }
                        } else {
                            productCycleCountInterval = "1";
                        }

                        String productCycleCountTolerance = "";
                        if (columnConfig.containsKey("ccounttolerance")) {
                            productCycleCountTolerance = recarr[(Integer) columnConfig.get("ccounttolerance")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(productCycleCountTolerance)) {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    productCycleCountTolerance = "0";
                                } else {
                                    if (productTypeID.equals(Producttype.SERVICE)) {
                                        productCycleCountTolerance = "0";
                                    } else {
                                        throw new AccountingException("Product Cycle Count Tolerance is not available");
                                    }
                                }
                            }
                        } else {
                            productCycleCountTolerance = "0";
                        }

                        String parentProductUUID = "";
                        if (columnConfig.containsKey("parentid")) {
                            String parentProductID = recarr[(Integer) columnConfig.get("parentid")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(parentProductID)) {
                                Product parentProduct = getProductByProductID(parentProductID, companyid);
                                if (parentProduct != null) {
                                    parentProductUUID = parentProduct.getID();
                                } else {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        parentProductUUID = null;
                                    } else {
                                        throw new AccountingException("Parent Product is not found for " + parentProductID);
                                    }
                                }
                            } else {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    parentProductUUID = null;
                                } else {
                                    throw new AccountingException("Parent Product is not available");
                                }
                            }
                        } else {
                            parentProductUUID = null;
                        }

                        String productSalesAccId = "";
                        if (columnConfig.containsKey("salesaccountname")) {
                            String productSalesAccountName = recarr[(Integer) columnConfig.get("salesaccountname")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(productSalesAccountName)) {
                                Account salesAccount = getAccountByName(productSalesAccountName, companyid);
                                if (salesAccount != null) {
                                    productSalesAccId = salesAccount.getID();
                                } else {
                                    throw new AccountingException("Product Sales Account is not found for " + productSalesAccountName);
                                }
                            } else {
                                throw new AccountingException("Product Sales Account is not available");
                            }
                        } else {
                            throw new AccountingException("Product Sales Account column is not found.");
                        }

                        String productSalesReturnAccId = "";
                        if (columnConfig.containsKey("salesretaccountname")) {
                            String productSalesReturnAccountName = recarr[(Integer) columnConfig.get("salesretaccountname")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(productSalesReturnAccountName)) {
                                Account salesReturnAccount = getAccountByName(productSalesReturnAccountName, companyid);
                                if (salesReturnAccount != null) {
                                    productSalesReturnAccId = salesReturnAccount.getID();
                                } else {
                                    throw new AccountingException("Product Sales Return Account is not found for " + productSalesReturnAccountName);
                                }
                            } else {
                                throw new AccountingException("Product Sales Return Account is not available");
                            }
                        } else {
                            throw new AccountingException("Product Sales Return Account column is not found.");
                        }

                        String productPreferedVendorID = "";
                        if (columnConfig.containsKey("vendornameid")) {
                            String productPreferedVendorName = recarr[(Integer) columnConfig.get("vendornameid")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(productPreferedVendorName)) {
                                Vendor vendor = getVendorByName(productPreferedVendorName, companyid);
                                if (vendor != null) {
                                    productPreferedVendorID = vendor.getID();
                                } else {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        productPreferedVendorID = null;
                                    } else {
                                        throw new AccountingException("Prefered Vendor is not found for " + productPreferedVendorName);
                                    }
                                }
                            } else {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    productPreferedVendorID = null;
                                } else {
                                    if (productTypeID.equals(Producttype.SERVICE)) {
                                        productPreferedVendorID = null;
                                    } else {
                                        throw new AccountingException("Prefered Vendor is not available");
                                    }
                                }
                            }
                        } else {
                            productPreferedVendorID = null;
                        }

                        String productPurchaseAccId = "";
                        if (columnConfig.containsKey("purchaseaccountname")) {
                            String productPurchaseAccountName = recarr[(Integer) columnConfig.get("purchaseaccountname")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(productPurchaseAccountName)) {
                                Account purchaseAccount = getAccountByName(productPurchaseAccountName, companyid);
                                if (purchaseAccount != null) {
                                    productPurchaseAccId = purchaseAccount.getID();
                                } else {
                                    throw new AccountingException("Product Purchase Account is not found for " + productPurchaseAccountName);
                                }
                            } else {
                                throw new AccountingException("Product Purchase Account is not available");
                            }
                        } else {
                            throw new AccountingException("Product Purchase Account column is not found.");
                        }

                        String productPurchaseReturnAccId = "";
                        if (columnConfig.containsKey("purchaseretaccountname")) {
                            String productPurchaseReturnAccountName = recarr[(Integer) columnConfig.get("purchaseretaccountname")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(productPurchaseReturnAccountName)) {
                                Account purchaseReturnAccount = getAccountByName(productPurchaseReturnAccountName, companyid);
                                if (purchaseReturnAccount != null) {
                                    productPurchaseReturnAccId = purchaseReturnAccount.getID();
                                } else {
                                    throw new AccountingException("Product Purchase Return Account is not found for " + productPurchaseReturnAccountName);
                                }
                            } else {
                                throw new AccountingException("Product Purchase Return Account is not available");
                            }
                        } else {
                            throw new AccountingException("Product Purchase Return Account column is not found.");
                        }

                        String productInitialQuantity = "";
                        if (columnConfig.containsKey("quantity")) {
                            productInitialQuantity = recarr[(Integer) columnConfig.get("quantity")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(productInitialQuantity)) {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    productInitialQuantity = "0";
                                } else {
                                    if (productTypeID.equals(Producttype.SERVICE)) {
                                        productInitialQuantity = "";
                                    } else {
                                        throw new AccountingException("Product Initial Quantity is not available");
                                    }
                                }
                            }
                        } else {
                            productInitialQuantity = "0";
                        }

                        String productInitialPurchasePrise = "";
                        if (columnConfig.containsKey("purchaseprice")) {
                            productInitialPurchasePrise = recarr[(Integer) columnConfig.get("purchaseprice")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(productInitialPurchasePrise)) {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    productInitialPurchasePrise = "";
                                } else {
                                    throw new AccountingException("Product Initial Purchase Price is not available");
                                }
                            }
                        } else {
                            productInitialPurchasePrise = "";
                        }

                        String productSalesPrice = "";
                        if (columnConfig.containsKey("saleprice")) {
                            productSalesPrice = recarr[(Integer) columnConfig.get("saleprice")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(productSalesPrice)) {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    productSalesPrice = "";
                                } else {
                                    throw new AccountingException("Product Sales Price is not available");
                                }
                            }
                        } else {
                            productSalesPrice = "";
                        }

                        String MsgExep = "";
                        if (columnConfig.containsKey("currencyName")) {
                            String productPriceCurrencyStr = recarr[(Integer) columnConfig.get("currencyName")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(productPriceCurrencyStr)) {
                                currencyId = getCurrencyId(productPriceCurrencyStr, currencyMap);
                                if (StringUtil.isNullOrEmpty(currencyId)) {
                                    //MsgExep = messageSource.getMessage("acc.field.ImportProductExceptionFormat", null, RequestContextUtils.getLocale(request));
                                    throw new AccountingException("Currency format you entered is not correct. it should be like \"SG Dollar (SGD)\"");
                                }
                            } else {
                                if (!masterPreference.equalsIgnoreCase("1")) {
                                    throw new AccountingException("Currency is not available.");
                                }
                            }
                        }
                        
                        UOMschemaType uomSchemaType = null;
                        if (columnConfig.containsKey("uomSchemaTypeName") && recarr.length > columnConfig.get("uomSchemaTypeName")) {
                            String uomSchemaTypeName = recarr[(Integer) columnConfig.get("uomSchemaTypeName")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(uomSchemaTypeName)) {
                                uomSchemaType = getUOMschemaTypeByName(uomSchemaTypeName, companyid);
                                if (uomSchemaType == null) {
                                    if (!masterPreference.equalsIgnoreCase("1")) {
                                        throw new AccountingException("UOM Schema is not found for " + uomSchemaTypeName);
                                    }
                                }
                            }
                        }
                        
                        if (uomSchemaType != null && !productUOMID.equalsIgnoreCase(uomSchemaType.getStockuom().getID())) {
                            throw new AccountingException("Stock UOM of Product and UOM Schema's Stock UOM should be same.");
                        }

                        String productDefaultLocationID = "";
                        if (columnConfig.containsKey("locationName")) {
                            String productDefaultLocationName = recarr[(Integer) columnConfig.get("locationName")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(productDefaultLocationName)) {
                                InventoryLocation invLoc = getInventoryLocationByName(productDefaultLocationName, companyid);
                                if (invLoc != null) {
                                    productDefaultLocationID = invLoc.getId();
                                } else {
                                    if (masterPreference.equalsIgnoreCase("2")) {
                                        HashMap requestParam =requestParams1; // AccountingManager.getGlobalParams(request);
                                        requestParam.put("id", "");
                                        requestParam.put("name", productDefaultLocationName);
                                        requestParam.put("parent", null);
                                        KwlReturnObject jeresult = accountingHandlerDAOobj.getObject(User.class.getName(), userId);
                                        User user = (User) jeresult.getEntityList().get(0);
                                        requestParam.put("user", user);
                                        KwlReturnObject locationResult = accMasterItemsDAOobj.addLocationItem(requestParam);
                                        invLoc = (InventoryLocation) locationResult.getEntityList().get(0);
                                        productDefaultLocationID = invLoc.getId();
                                    } else {
                                        if (masterPreference.equalsIgnoreCase("1")) {
                                            productDefaultLocationID = null;
                                        } else {
                                            throw new AccountingException("Default Location is not found for " + productDefaultLocationName);
                                        }
                                    }
                                }
                            } else {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    productDefaultLocationID = null;
                                } else {
                                    throw new AccountingException("Default Location is not available.");
                                }
                            }
                        } else {
                            productDefaultLocationID = null;
                        }


                        String productDefaultWarehouseID = "";
                        if (columnConfig.containsKey("warehouseName")) {
                            String productDefaultWarehouseName = recarr[(Integer) columnConfig.get("warehouseName")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(productDefaultWarehouseName)) {
                                InventoryWarehouse invWHouse = getInventoryWarehouseByName(productDefaultWarehouseName, companyid);
                                if (invWHouse != null) {
                                    productDefaultWarehouseID = invWHouse.getId();
                                } else {
                                    if (masterPreference.equalsIgnoreCase("2")) {
                                        HashMap requestParam =requestParams1;// AccountingManager.getGlobalParams(request);
                                        requestParam.put("id", "");
                                        requestParam.put("name", productDefaultWarehouseName);
                                        requestParam.put("parent", null);
                                        requestParam.put("location", null);

                                        KwlReturnObject warehouseResult = accMasterItemsDAOobj.addWarehouseItem(requestParam);
                                        invWHouse = (InventoryWarehouse) warehouseResult.getEntityList().get(0);
                                        productDefaultWarehouseID = invWHouse.getId();
                                    } else {
                                        if (masterPreference.equalsIgnoreCase("1")) {
                                            productDefaultWarehouseID = null;
                                        } else {
                                            throw new AccountingException("Default Warehouse is not found for " + productDefaultWarehouseName);
                                        }
                                    }
                                }
                            } else {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    productDefaultWarehouseID = null;
                                } else {
                                    throw new AccountingException("Default Warehouse is not available.");
                                }
                            }
                        } else {
                            productDefaultWarehouseID = null;
                        }


                        Boolean isSyncable = false;
                        if (columnConfig.containsKey("syncable")) {
                            String productMakeAvailableInOtherApp = recarr[(Integer) columnConfig.get("syncable")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(productMakeAvailableInOtherApp)) {
                                if (productMakeAvailableInOtherApp.equalsIgnoreCase("T")) {
                                    isSyncable = true;
                                } else if (productMakeAvailableInOtherApp.equalsIgnoreCase("F")) {
                                    isSyncable = false;
                                } else {
                                    throw new AccountingException("Format you entered is not correct. It should be like \"T\" or \"F\"");
                                }
                            } else {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    isSyncable = false;
                                } else {
                                    throw new AccountingException("Make available in other application is not available.");
                                }
                            }
                        }


                        Boolean isMultiUOM = false;
                        if (columnConfig.containsKey("multiuom")) {
                            String multipleUOM = recarr[(Integer) columnConfig.get("multiuom")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(multipleUOM)) {
                                if (multipleUOM.equalsIgnoreCase("T")) {
                                    isMultiUOM = true;
                                } else if (multipleUOM.equalsIgnoreCase("F")) {
                                    isMultiUOM = false;
                                } else {
                                    throw new AccountingException("Format you entered is not correct. It should be like \"T\" or \"F\"");
                                }
                            } else {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    isMultiUOM = false;
                                } else {
                                    if (productTypeID.equals(Producttype.SERVICE)) {
                                        isMultiUOM = false;
                                    } else {
                                        throw new AccountingException("Multiple UOM is not available.");
                                    }
                                }
                            }
                        }

                        Boolean isIslocationforproduct = false;
                        if (columnConfig.containsKey(Constants.ACTIVATE_LOCATION)) {
                            String ISlocationforproduct = recarr[(Integer) columnConfig.get(Constants.ACTIVATE_LOCATION)].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(ISlocationforproduct)) {
                                if (ISlocationforproduct.equalsIgnoreCase("T")) {
                                    isIslocationforproduct = true;
                                } else if (ISlocationforproduct.equalsIgnoreCase("F")) {
                                    isIslocationforproduct = false;
                                } else {
                                    throw new AccountingException("Format you entered is not correct. It should be like \"T\" or \"F\"");
                                }
                            }
                        }
                        
                        if(isIslocationforproduct && StringUtil.isNullOrEmpty(productDefaultLocationID)){
                           throw new AccountingException("Location is activated but Default Location Value is not Provided."); 
                        }

                        Boolean isIsSerialForProduct = false;
                        if (columnConfig.containsKey(Constants.ACTIVATE_SERIAL_NO)) {
                            String IsSerialForProduct = recarr[(Integer) columnConfig.get(Constants.ACTIVATE_SERIAL_NO)].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(IsSerialForProduct)) {
                                if (IsSerialForProduct.equalsIgnoreCase("T")) {
                                    isIsSerialForProduct = true;
                                } else if (IsSerialForProduct.equalsIgnoreCase("F")) {
                                    isIsSerialForProduct = false;
                                } else {
                                    throw new AccountingException("Format you entered is not correct. It should be like \"T\" or \"F\"");
                                }
                            }
                        }
                        
                        if(isIsSerialForProduct && Double.parseDouble(productInitialQuantity)>0){
                            throw new AccountingException("Serial Data can't be imported because Opening Quantity is more than 0."); 
                        }
                        
                        Boolean isIswarehouseforproduct = false;
                        if (columnConfig.containsKey(Constants.ACTIVATE_WAREHOUSE)) {
                            String Iswarehouseforproduct = recarr[(Integer) columnConfig.get(Constants.ACTIVATE_WAREHOUSE)].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(Iswarehouseforproduct)) {
                                if (Iswarehouseforproduct.equalsIgnoreCase("T")) {
                                    isIswarehouseforproduct = true;
                                } else if (Iswarehouseforproduct.equalsIgnoreCase("F")) {
                                    isIswarehouseforproduct = false;
                                } else {
                                    throw new AccountingException("Format you entered is not correct. It should be like \"T\" or \"F\"");
                                }
                            }
                        }
                        
                        if(isIswarehouseforproduct && StringUtil.isNullOrEmpty(productDefaultWarehouseID)){
                           throw new AccountingException("Warehouse is activated but Default Warehouse Value is not Provided."); 
                        }
                        
                        Boolean isIsBatchForProduct = false;
                        if (columnConfig.containsKey(Constants.ACTIVATE_BATCH)) {
                            String IsBatchForProduct = recarr[(Integer) columnConfig.get(Constants.ACTIVATE_BATCH)].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(IsBatchForProduct)) {
                                if (IsBatchForProduct.equalsIgnoreCase("T")) {
                                    isIsBatchForProduct = true;
                                } else if (IsBatchForProduct.equalsIgnoreCase("F")) {
                                    isIsBatchForProduct = false;
                                } else {
                                    throw new AccountingException("Format you entered is not correct. It should be like \"T\" or \"F\"");
                                }
                            }
                        }
                        
                        String wipoffset = "";
                        if (columnConfig.containsKey("wipoffset")) {
                            String temp = recarr[(Integer) columnConfig.get("wipoffset")].replaceAll("\"", "").trim();                 
                            if (!StringUtil.isNullOrEmpty(temp)) {
                               wipoffset = temp;
                            } 
                        }
                        
                        String inventoryoffset = "";
                        if (columnConfig.containsKey("inventoryoffset")) {
                           String temp = recarr[(Integer) columnConfig.get("inventoryoffset")].replaceAll("\"", "").trim();                 
                            if (!StringUtil.isNullOrEmpty(temp)) {
                               inventoryoffset = temp;
                            } 
                        }
                        
                        String hscode = "";
                        if (columnConfig.containsKey("hscode")) {
                            String temp = recarr[(Integer) columnConfig.get("hscode")].replaceAll("\"", "").trim();                 
                            if (!StringUtil.isNullOrEmpty(temp)) {
                               hscode = temp;
                            } 
                        }
                        String additionalfreetext = "";
                        if (columnConfig.containsKey("additionalfreetext")) {
                             String temp = recarr[(Integer) columnConfig.get("additionalfreetext")].replaceAll("\"", "").trim();                 
                            if (!StringUtil.isNullOrEmpty(temp)) {
                               additionalfreetext = temp;
                            } 
                        }
                        String itemcolor = "";
                        if (columnConfig.containsKey("itemcolor")) {
                            String temp = recarr[(Integer) columnConfig.get("itemcolor")].replaceAll("\"", "").trim();                 
                            if (!StringUtil.isNullOrEmpty(temp)) {
                               itemcolor = temp;
                            } 
                        }
                        String alternateproduct = "";
                        if (columnConfig.containsKey("alternateproduct")) {
                             String temp = recarr[(Integer) columnConfig.get("alternateproduct")].replaceAll("\"", "").trim();                 
                            if (!StringUtil.isNullOrEmpty(temp)) {
                               alternateproduct = temp;
                            } 
                        }
                        String purchasemfg = "";
                        if (columnConfig.containsKey("purchasemfg")) {
                            String temp = recarr[(Integer) columnConfig.get("purchasemfg")].replaceAll("\"", "").trim();                 
                            if (!StringUtil.isNullOrEmpty(temp)) {
                               purchasemfg = temp;
                            } 
                        }
                        String catalogno = "";
                        if (columnConfig.containsKey("catalogno")) {
                            String temp = recarr[(Integer) columnConfig.get("catalogno")].replaceAll("\"", "").trim();                 
                            if (!StringUtil.isNullOrEmpty(temp)) {
                               catalogno = temp;
                            } 
                        }
                        String barcode = "";
                        if (columnConfig.containsKey("barcode")) {
                             String temp = recarr[(Integer) columnConfig.get("barcode")].replaceAll("\"", "").trim();                 
                            if (!StringUtil.isNullOrEmpty(temp)) {
                               barcode = temp;
                            } 
                        }
                        String additionaldesc = "";
                        if (columnConfig.containsKey("additionaldesc")) {
                           String temp = recarr[(Integer) columnConfig.get("additionaldesc")].replaceAll("\"", "").trim();                 
                            if (!StringUtil.isNullOrEmpty(temp)) {
                               additionaldesc = temp;
                            } 
                        }
                        String descinforeign = "";
                        if (columnConfig.containsKey("descinforeign")) {
                            String temp = recarr[(Integer) columnConfig.get("descinforeign")].replaceAll("\"", "").trim();                 
                            if (!StringUtil.isNullOrEmpty(temp)) {
                               descinforeign = temp;
                            } 
                        }
                        String licensecode = "";
                        if (columnConfig.containsKey("licensecode")) {
                           String temp = recarr[(Integer) columnConfig.get("licensecode")].replaceAll("\"", "").trim();                 
                            if (!StringUtil.isNullOrEmpty(temp)) {
                               licensecode = temp;
                            } 
                        }
                        String itemgroup = "";
                        if (columnConfig.containsKey("itemgroup")) {
                            String temp = recarr[(Integer) columnConfig.get("itemgroup")].replaceAll("\"", "").trim();                 
                            if (!StringUtil.isNullOrEmpty(temp)) {
                               itemgroup = temp;
                            } 
                        }
                        String pricelist = "";
                        if (columnConfig.containsKey("pricelist")) {
                            String temp = recarr[(Integer) columnConfig.get("pricelist")].replaceAll("\"", "").trim();                 
                            if (!StringUtil.isNullOrEmpty(temp)) {
                               pricelist = temp;
                            } 
                        }
                        String shippingtype = "";
                        if (columnConfig.containsKey("shippingtype")) {
                            String temp = recarr[(Integer) columnConfig.get("shippingtype")].replaceAll("\"", "").trim();                 
                            if (!StringUtil.isNullOrEmpty(temp)) {
                               shippingtype = temp;
                            }
                        }
                        Boolean recyclable = false;
                        if (columnConfig.containsKey("recyclable")) {
                             String temp = recarr[(Integer) columnConfig.get("recyclable")].replaceAll("\"", "").trim();            
                            if (!StringUtil.isNullOrEmpty(temp)) {
                                String isRecyclable = temp;
                                if (isRecyclable.equalsIgnoreCase("T")) {
                                    recyclable = true;
                                } else if (isRecyclable.equalsIgnoreCase("F")) {
                                    recyclable = false;
                                } else {
                                    throw new AccountingException("Format you entered is not correct. It should be like \"T\" or \"F\"");
                                }
                            } 
                        }
                        Boolean qaenable = false;
                        if (columnConfig.containsKey("qaenable")) {
                            String temp = recarr[(Integer) columnConfig.get("qaenable")].replaceAll("\"", "").trim();            
                            if (!StringUtil.isNullOrEmpty(temp)) {
                                String isQaenable = temp;
                                if (isQaenable.equalsIgnoreCase("T")) {
                                    qaenable = true;
                                } else if (isQaenable.equalsIgnoreCase("F")) {
                                    qaenable = false;
                                } else {
                                    throw new AccountingException("Format you entered is not correct. It should be like \"T\" or \"F\"");
                                }
                            } 
                        }
                        Boolean isknittingitem = false;
                        if (columnConfig.containsKey("isknittingitem")) {
                            String temp = recarr[(Integer) columnConfig.get("isknittingitem")].replaceAll("\"", "").trim();            
                            if (!StringUtil.isNullOrEmpty(temp)) {
                                String isIsknittingitem = temp;
                                if (isIsknittingitem.equalsIgnoreCase("T")) {
                                    isknittingitem = true;
                                } else if (isIsknittingitem.equalsIgnoreCase("F")) {
                                    isknittingitem = false;
                                } else {
                                    throw new AccountingException("Format you entered is not correct. It should be like \"T\" or \"F\"");
                                }
                            } 
                        }
                        Boolean isactive = false;
                        if (columnConfig.containsKey("isactive")) {
                            String temp = recarr[(Integer) columnConfig.get("isactive")].replaceAll("\"", "").trim();            
                            if (!StringUtil.isNullOrEmpty(temp)) {
                                String isIsactive = temp;
                                if (isIsactive.equalsIgnoreCase("T")) {
                                    isactive = true;
                                } else if (isIsactive.equalsIgnoreCase("F")) {
                                    isactive = false;
                                } else {
                                    throw new AccountingException("Format you entered is not correct. It should be like \"T\" or \"F\"");
                                }
                            } 
                        }
                        Boolean blockloosesell = false;
                        if (columnConfig.containsKey("blockloosesell")) {
                           String temp = recarr[(Integer) columnConfig.get("blockloosesell")].replaceAll("\"", "").trim();            
                            if (!StringUtil.isNullOrEmpty(temp)) {
                                String isBlockloosesell = temp;
                                if (isBlockloosesell.equalsIgnoreCase("T")) {
                                    blockloosesell = true;
                                } else if (isBlockloosesell.equalsIgnoreCase("F")) {
                                    blockloosesell = false;
                                } else {
                                    throw new AccountingException("Format you entered is not correct. It should be like \"T\" or \"F\"");
                                }
                            } 
                        }
                        String itemsalesvolume = "";
                        if (columnConfig.containsKey("itemsalesvolume")) {
                            String temp = recarr[(Integer) columnConfig.get("itemsalesvolume")].replaceAll("\"", "").trim();            
                            if (!StringUtil.isNullOrEmpty(temp)) {
                                itemsalesvolume = temp;

                            }
                        }
                        String productweight = "0";
                        if (columnConfig.containsKey("productweight")) {
                             String temp = recarr[(Integer) columnConfig.get("productweight")].replaceAll("\"", "").trim();            
                            if (!StringUtil.isNullOrEmpty(temp)) {
                                productweight = temp;

                            }
                        }
                        String itemsaleswidth = "";
                        if (columnConfig.containsKey("itemsaleswidth")) {
                             String temp = recarr[(Integer) columnConfig.get("itemsaleswidth")].replaceAll("\"", "").trim();            
                            if (!StringUtil.isNullOrEmpty(temp)) {
                                itemsaleswidth = temp;

                            }
                        }
                        String itemsalesheight = "";
                        if (columnConfig.containsKey("itemsalesheight")) {
                            String temp = recarr[(Integer) columnConfig.get("itemsalesheight")].replaceAll("\"", "").trim();            
                            if (!StringUtil.isNullOrEmpty(temp)) {
                                itemsalesheight = temp;

                            }
                        }
                        String itemwidth = "";
                        if (columnConfig.containsKey("itemwidth")) {
                            String temp = recarr[(Integer) columnConfig.get("itemwidth")].replaceAll("\"", "").trim();            
                            if (!StringUtil.isNullOrEmpty(temp)) {
                                itemwidth = temp;

                            }
                        }
                        String itemvolume = "";
                        if (columnConfig.containsKey("itemvolume")) {
                            String temp = recarr[(Integer) columnConfig.get("itemvolume")].replaceAll("\"", "").trim();            
                            if (!StringUtil.isNullOrEmpty(temp)) {
                                itemvolume = temp;

                            }
                        }
                        String itempurchasewidth = "";
                        if (columnConfig.containsKey("itempurchasewidth")) {
                            String temp = recarr[(Integer) columnConfig.get("itempurchasewidth")].replaceAll("\"", "").trim();            
                            if (!StringUtil.isNullOrEmpty(temp)) {
                                itempurchasewidth = temp;

                            }
                        }
                        String itempurchasevolume = "";
                        if (columnConfig.containsKey("itempurchasevolume")) {
                           String temp = recarr[(Integer) columnConfig.get("itempurchasevolume")].replaceAll("\"", "").trim();            
                            if (!StringUtil.isNullOrEmpty(temp)) {
                                itempurchasevolume = temp;

                            }
                        }
                        String itempurchaselength = "0";
                        if (columnConfig.containsKey("itempurchaselength")) {
                            String temp = recarr[(Integer) columnConfig.get("itempurchaselength")].replaceAll("\"", "").trim();            
                            if (!StringUtil.isNullOrEmpty(temp)) {
                                itempurchaselength = temp;

                            }
                        }
                        String qaleadtimeindays = "0";
                        if (columnConfig.containsKey("qaleadtimeindays")) {
                            String temp = recarr[(Integer) columnConfig.get("qaleadtimeindays")].replaceAll("\"", "").trim();            
                            if (!StringUtil.isNullOrEmpty(temp)) {
                                qaleadtimeindays = temp;

                            }
                        }
                        String reusabilitycount = "";
                        if (columnConfig.containsKey("reusabilitycount")) {
                            String temp = recarr[(Integer) columnConfig.get("reusabilitycount")].replaceAll("\"", "").trim();            
                            if (!StringUtil.isNullOrEmpty(temp)) {
                                reusabilitycount = temp;

                            }
                        }
                        
                        String orderinguom = "";
                        UnitOfMeasure orderuom=null;
                        if (columnConfig.containsKey("orderinguom")) {
                            String productUOMName = recarr[(Integer) columnConfig.get("orderinguom")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(productUOMName)) {
                                orderuom = getUOMByName(productUOMName, companyid);
                                if (orderuom != null) {
                                    orderinguom = salesuom.getID();
                                } else {
                                    if (masterPreference.equalsIgnoreCase("2")) {
                                        HashMap<String, Object> uomMap = new HashMap<String, Object>();
                                        uomMap.put("uomname", StringUtil.DecodeText(productUOMName));
                                        uomMap.put("uomtype", StringUtil.DecodeText(productUOMName));
                                        uomMap.put("precision", 0);
                                        uomMap.put("companyid", companyid);

                                        KwlReturnObject uomresult = accUomObj.addUoM(uomMap);
                                        orderuom = (UnitOfMeasure) uomresult.getEntityList().get(0);
                                        orderinguom = orderuom.getID();
                                    } else {
                                        if (masterPreference.equalsIgnoreCase("1")) {
                                            orderinguom = "";
                                        } else {
                                            throw new AccountingException("Product Sales Unit Of Measure is not found for " + productUOMName);
                                        }
                                    }
                                }
                            } else {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    orderinguom = "";
                                } else {
                                    if (productTypeID.equals(Producttype.SERVICE)) {
                                        orderinguom = "";
                                    } else {
                                        throw new AccountingException("Product Ordering Unit Of Measure is not available");
                                    }
                                }
                            }
                        } else {
                            orderinguom = "";
                        }
                        String transferuom = "";
                        UnitOfMeasure transferUOM=null;
                        if (columnConfig.containsKey("transferuom")) {
                            String productUOMName = recarr[(Integer) columnConfig.get("transferuom")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(productUOMName)) {
                                transferUOM = getUOMByName(productUOMName, companyid);
                                if (transferUOM != null) {
                                    transferuom = salesuom.getID();
                                } else {
                                    if (masterPreference.equalsIgnoreCase("2")) {
                                        HashMap<String, Object> uomMap = new HashMap<String, Object>();
                                        uomMap.put("uomname", StringUtil.DecodeText(productUOMName));
                                        uomMap.put("uomtype", StringUtil.DecodeText(productUOMName));
                                        uomMap.put("precision", 0);
                                        uomMap.put("companyid", companyid);

                                        KwlReturnObject uomresult = accUomObj.addUoM(uomMap);
                                        transferUOM = (UnitOfMeasure) uomresult.getEntityList().get(0);
                                        transferuom = orderuom.getID();
                                    } else {
                                        if (masterPreference.equalsIgnoreCase("1")) {
                                            transferuom = "";
                                        } else {
                                            throw new AccountingException("Product Sales Unit Of Measure is not found for " + productUOMName);
                                        }
                                    }
                                }
                            } else {
                                if (masterPreference.equalsIgnoreCase("1")) {
                                    transferuom = "";
                                } else {
                                    if (productTypeID.equals(Producttype.SERVICE)) {
                                        transferuom = "";
                                    } else {
                                        throw new AccountingException("Product Ordering Unit Of Measure is not available");
                                    }
                                }
                            }
                        } else {
                            transferuom = "";
                        }
                        
                        Set<Frequency> ccFrequencies = new HashSet();
                        if (!productTypeID.equals(Producttype.SERVICE) && !productTypeID.equals(Producttype.SERVICE_Name) && columnConfig.containsKey("CCFrequency")) {
                            String frequencies = recarr[(Integer) columnConfig.get("CCFrequency")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(frequencies)) {
                                String[] frqs = frequencies.split(",");
                                String notFoundNames = "";
                                for (int i = 0; i < frqs.length; i++) {
                                    Frequency frq = null;
                                    if ("Daily".equalsIgnoreCase(frqs[i])) {
                                        frq = frequencyMap.get(Frequency.DAILY);
                                    } else if ("Weekly".equalsIgnoreCase(frqs[i])) {
                                        frq = frequencyMap.get(Frequency.WEEKLY);
                                    } else if ("Fortnightly".equalsIgnoreCase(frqs[i])) {
                                        frq = frequencyMap.get(Frequency.FORTNIGHT);
                                    } else if ("Monthly".equalsIgnoreCase(frqs[i])) {
                                        frq = frequencyMap.get(Frequency.MONTHLY);
                                    } else {
                                        notFoundNames += frqs[i] + ", ";
                                    }
                                    if (frq != null) {
                                        ccFrequencies.add(frq);
                                    }
                                }
                                if (!StringUtil.isNullOrEmpty(notFoundNames) && masterPreference.equalsIgnoreCase("0")) {
                                    notFoundNames = notFoundNames.substring(0, notFoundNames.lastIndexOf(","));
                                    throw new AccountingException("Cycle Count Frequency is not found for " + notFoundNames);
                                }
                            }
                        }
                        
                        
                            String tariffName = "";
                            if (columnConfig.containsKey("tariffname")) {                              
                                String temp = recarr[(Integer) columnConfig.get("tariffname")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(temp)) {
                                    tariffName = temp;
                                } else {
                                    throw new AccountingException("Tariff Name column is not found.");
                                }
                            }

                            String hsncode = "";
                            if ( columnConfig.containsKey("hsncode")) {
                                String temp = recarr[(Integer) columnConfig.get("hsncode")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(temp)) {
                                    hsncode = temp;
                                } else {
                                    throw new AccountingException("HSNCode column is not found.");
                                }
                            }

                            String reportinguomName = "", reportinguomID = "";
                            UnitOfMeasure reportinguom = null;
                            if (columnConfig.containsKey("reportinguom")) {
                                String temp = recarr[(Integer) columnConfig.get("reportinguom")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(temp)) {
                                    reportinguomName = temp;
                                    reportinguom = accProductModuleService.getUOMByName(reportinguomName, companyid);
                                    if (reportinguom != null) {
                                        reportinguomID = reportinguom.getID();
                                    }
                                } else {
                                    throw new AccountingException("Reporting UOM column is not found.");
                                }
                            }

                            UOMschemaType reportingSchemaType = null;
                            if ( columnConfig.containsKey("reportingschematype")) {
                                String temp = recarr[(Integer) columnConfig.get("reportingschematype")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(temp)) {
                                    String uomSchemaTypeName = temp;//cell.getStringCellValue().trim();
                                    if (!StringUtil.isNullOrEmpty(uomSchemaTypeName)) {
                                        reportingSchemaType = getUOMschemaTypeByName(uomSchemaTypeName, companyid);
                                    }
                                    // Ref Optimization related code. 
                                    if (reportingSchemaType == null) {
                                        throw new AccountingException("UOM Schema is not found for " + uomSchemaTypeName);
                                    }
                                }
                            }

                            String excisemethod = "";
                            if (!productTypeID.equals(Producttype.SERVICE) && columnConfig.containsKey("excisemethodmain")) {
                                String temp = recarr[(Integer) columnConfig.get("excisemethodmain")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(temp)) {
                                    excisemethod = temp;
                                    Map<String, String> tempMap = new HashMap<String, String>();
                                    tempMap.put("Ad Valorem Method", "valorem");
                                    tempMap.put("Quantity", "quantity");
                                    tempMap.put("Specific basis", "specific");
                                    tempMap.put("MRP (Maximum Retail Price)", "mrp");
                                    if (tempMap.containsKey(excisemethod)) {
                                        excisemethod = tempMap.get(excisemethod);
                                    }
                                } else {
                                    throw new AccountingException("Valuation Type column is not found.");
                                }
                            }
                            String vatMethodType = "";
                            if (columnConfig.containsKey("vatMethodType")) {
                                String temp = recarr[(Integer) columnConfig.get("vatMethodType")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(temp)) {
                                    Map<String, String> tempMap = new HashMap<>();
                                    tempMap.put("Ad Valorem Method", "valorem");
                                    tempMap.put("Quantity", "quantity");
                                    tempMap.put("Specific basis", "specific");
                                    tempMap.put("MRP (Maximum Retail Price)", "mrp");
                                    if (tempMap.containsKey(temp)) {
                                        vatMethodType = tempMap.get(temp);
                                    }
                                } else {
                                    throw new AccountingException("Vat Valuation Type column is not found.");
                                }
                            }

                            Date vatAbatementPeriodFromDate = null;
                            if (columnConfig.containsKey("vatAbatementPeriodFromDate")) {//when AsofDate header is mapped
                                String temp = recarr[(Integer) columnConfig.get("vatAbatementPeriodFromDate")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(temp)) {
                                    String stringAsOfDate = temp;
                                    if (!stringAsOfDate.equals("")) {
                                        try {
                                            vatAbatementPeriodFromDate = df.parse(stringAsOfDate);
                                        } catch (Exception ex) {
                                            throw new AccountingException("Incorrect date format for VAT Abatement Period From Date, Please specify values in " + temp + " format.");
                                        }
                                    }
                                }
                            }

                            Date vatAbatementPeriodToDate = null;
                            if (columnConfig.containsKey("vatAbatementPeriodToDate")) {//when AsofDate header is mapped
                                String temp = recarr[(Integer) columnConfig.get("vatAbatementPeriodToDate")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(temp)) {
                                    String stringAsOfDate = temp;
                                    if (!stringAsOfDate.equals("")) {
                                        try {

                                            vatAbatementPeriodToDate = df.parse(stringAsOfDate);
                                        } catch (Exception ex) {
                                            throw new AccountingException("Incorrect date format for VAT Period To Date, Please specify values in " + temp + " format.");
                                        }
                                    }
                                }
                            }

                            String vatcommoditycode = "";
                            if (columnConfig.containsKey("vatcommoditycode")) {
                                String temp = recarr[(Integer) columnConfig.get("vatcommoditycode")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(temp)) {
                                    String vatcommodityname = temp;
                                    Map tempData = new HashMap<String, String>();
                                    tempData.put("vatcommodityname", vatcommodityname);
                                    tempData.put("company", companyid);
                                    KwlReturnObject vatCommresult = accProductObj.getVATCommodityCodeByName(tempData);
                                    if (vatCommresult.getRecordTotalCount() > 0) {
                                        List list = (ArrayList) vatCommresult.getEntityList();
                                        Iterator itr = list.iterator();
                                        if (itr.hasNext()) {
                                            vatcommoditycode = (String) itr.next();
                                        }
                                    } else {
                                        throw new AccountingException(vatcommodityname + " is not found in VAT Commodity Name.");
                                    }
                                }
                            }

                            String interStatePurchaseAccount = "";
                            if (columnConfig.containsKey("interStatePurchaseAccount")) {
                                String temp = recarr[(Integer) columnConfig.get("interStatePurchaseAccount")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(temp)) {
                                    Account purchaseAccount = getAccountByName(temp, companyid);
                                    if (purchaseAccount != null) {
                                        interStatePurchaseAccount = purchaseAccount.getID();
                                    } else {
                                        throw new AccountingException("Inter State Purchase Account is not found for " + temp);
                                    }
                                }
                            }

                            String interStatePurchaseAccountCForm = "";
                            if (columnConfig.containsKey("interStatePurchaseAccountCForm")) {
                                String temp = recarr[(Integer) columnConfig.get("interStatePurchaseAccountCForm")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(temp)) {
                                    Account purchaseAccount = getAccountByName(temp, companyid);
                                    if (purchaseAccount != null) {
                                        interStatePurchaseAccountCForm = purchaseAccount.getID();
                                    } else {
                                        throw new AccountingException("Inter State Purchase Account C-Form is not found for " + temp);
                                    }
                                }
                            }

                            String interStatePurchaseReturnAccount = "";
                            if (columnConfig.containsKey("interStatePurchaseReturnAccount")) {
                                String temp = recarr[(Integer) columnConfig.get("interStatePurchaseReturnAccount")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(temp)) {
                                    Account purchaseAccount = getAccountByName(temp, companyid);
                                    if (purchaseAccount != null) {
                                        interStatePurchaseReturnAccount = purchaseAccount.getID();
                                    } else {
                                        throw new AccountingException("Inter State Purchase Return Account is not found for " + temp);
                                    }
                                }
                            }

                            String interStatePurchaseReturnAccountCForm = "";
                            if (columnConfig.containsKey("interStatePurchaseReturnAccountCForm")) {
                                String temp = recarr[(Integer) columnConfig.get("interStatePurchaseReturnAccountCForm")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(temp)) {
                                    Account purchaseAccount = getAccountByName(temp, companyid);
                                    if (purchaseAccount != null) {
                                        interStatePurchaseReturnAccountCForm = purchaseAccount.getID();
                                    } else {
                                        throw new AccountingException("Inter State Purchase Return Account CForm is not found for " + temp);
                                    }
                                }
                            }
                            String interStateSalesAccount = "";
                            if (columnConfig.containsKey("interStateSalesAccount")) {
                                String temp = recarr[(Integer) columnConfig.get("interStateSalesAccount")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(temp)) {
                                    Account purchaseAccount = getAccountByName(temp, companyid);
                                    if (purchaseAccount != null) {
                                        interStateSalesAccount = purchaseAccount.getID();
                                    } else {
                                        throw new AccountingException("Inter State Sales Account is not found for " + temp);
                                    }
                                }
                            }

                            String interStateSalesAccountCForm = "";
                            if (columnConfig.containsKey("interStateSalesAccountCForm")) {
                                String temp = recarr[(Integer) columnConfig.get("interStateSalesAccountCForm")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(temp)) {
                                    Account purchaseAccount = getAccountByName(temp, companyid);
                                    if (purchaseAccount != null) {
                                        interStateSalesAccountCForm = purchaseAccount.getID();
                                    } else {
                                        throw new AccountingException("Inter State Sales Account CForm is not found for " + temp);
                                    }
                                }
                            }

                            String interStateSalesReturnAccount = "";
                            if (columnConfig.containsKey("interStateSalesReturnAccount")) {
                                String temp = recarr[(Integer) columnConfig.get("interStateSalesReturnAccount")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(temp)) {
                                    Account purchaseAccount = getAccountByName(temp, companyid);
                                    if (purchaseAccount != null) {
                                        interStateSalesReturnAccount = purchaseAccount.getID();
                                    } else {
                                        throw new AccountingException("Inter State Sales Return Account is not found for " + temp);
                                    }
                                }
                            }

                            String interStateSalesReturnAccountCForm = "";
                            if (columnConfig.containsKey("interStateSalesReturnAccountCForm")) {
                                String temp = recarr[(Integer) columnConfig.get("interStateSalesReturnAccountCForm")].replaceAll("\"", "").trim();

                                if (!StringUtil.isNullOrEmpty(temp)) {
                                    Account purchaseAccount = getAccountByName(temp, companyid);
                                    if (purchaseAccount != null) {
                                        interStateSalesReturnAccountCForm = purchaseAccount.getID();
                                    } else {
                                        throw new AccountingException("Inter State Sales Return Account CForm is not found for " + temp);
                                    }
                                }
                            }

                        // For Checking 'ProductID' is exist or not
                        KwlReturnObject result = accProductObj.getProductIDCount(productID, companyid,false);
                        int nocount = result.getRecordTotalCount();
                        if (nocount > 0 && !updateExistingRecordFlag) {
                            throw new AccountingException("Product ID '" + productID + "' already exists.");
                        } else if (nocount == 0 && updateExistingRecordFlag) {
                            throw new AccountingException("Product ID '" + productID + "' not exists.");
                        }

                        // creating product Hashmap
                        HashMap<String, Object> productMap = new HashMap<String, Object>();

                        boolean isUsedInTransaction = false;
                        if (updateExistingRecordFlag) {
                            Product product = (Product) result.getEntityList().get(0);
                            productMap.put("id", product.getID());
                            List listObj = accProductModuleService.isProductUsedintransction(product.getID(), companyid, requestParams1);
                            isUsedInTransaction =(Boolean) listObj.get(0);    //always boolean value
                        } else {
                            productMap.put("producttype", productTypeID);
                        }

                        productMap.put("name", productName);
                        productMap.put("productid", productID);
                        productMap.put("desc", productDescription);
                        productMap.put("syncable", false);
                        productMap.put("multiuom", false);
                        productMap.put("WIPoffset",wipoffset);
                        productMap.put("Inventoryoffset",inventoryoffset);
                        productMap.put("hsCode",hscode);
                        productMap.put("additionalfreetext",additionalfreetext);
                        productMap.put("itemcolor",itemcolor);
                        productMap.put("alternateproductid",alternateproduct);
                        productMap.put("purchasemfg",purchasemfg);
                        productMap.put("catalogNo",catalogno);
                        productMap.put("barcode",barcode);
                        productMap.put("additionaldescription",additionaldesc);
                        productMap.put("foreigndescription",descinforeign);
                        productMap.put("licensecode",licensecode);
                        productMap.put("itemgroup",itemgroup);
                        productMap.put("itempricelist",pricelist);
                        productMap.put("shippingtype",shippingtype);
                        productMap.put("isrecyclable",recyclable);
                        productMap.put("isQAenable",qaenable);
                        productMap.put("isKnittingItem",isknittingitem);
                        productMap.put("isActiveItem",isactive);
                        productMap.put("blockLooseSell",blockloosesell);
                        if(!StringUtil.isNullOrEmpty(itemsalesvolume))
                            productMap.put("itemsalesvolume",Double.parseDouble(itemsalesvolume));
                        if(!StringUtil.isNullOrEmpty(productweight))
                            productMap.put("productweight",Double.parseDouble(productweight));
                        if(!StringUtil.isNullOrEmpty(itemsaleswidth))
                            productMap.put("itemsaleswidth",Double.parseDouble(itemsaleswidth));
                        if(!StringUtil.isNullOrEmpty(itemsalesheight))
                            productMap.put("itemsalesheight",Double.parseDouble(itemsalesheight));
                        if(!StringUtil.isNullOrEmpty(itemwidth))
                            productMap.put("itemwidth",Double.parseDouble(itemwidth));
                        if(!StringUtil.isNullOrEmpty(itemvolume))
                            productMap.put("itemvolume",Double.parseDouble(itemvolume));
                        if(!StringUtil.isNullOrEmpty(itempurchasewidth))
                            productMap.put("itempurchasewidth",Double.parseDouble(itempurchasewidth));
                        if(!StringUtil.isNullOrEmpty(itempurchasevolume))
                            productMap.put("itempurchasevolume",Double.parseDouble(itempurchasevolume));
                        if(!StringUtil.isNullOrEmpty(itempurchaselength))
                            productMap.put("itempurchaselength",Double.parseDouble(itempurchaselength));
                        if(!StringUtil.isNullOrEmpty(qaleadtimeindays))
                            productMap.put("QAleadtime",Integer.parseInt(qaleadtimeindays));
                        if(!StringUtil.isNullOrEmpty(reusabilitycount))
                            productMap.put("reusabilitycount",Integer.parseInt(reusabilitycount));
                        if (!StringUtil.isNullOrEmpty(transferuom)) {
                            productMap.put("transferUoM",transferuom);
                        }
                        if (!StringUtil.isNullOrEmpty(orderinguom)) {
                            productMap.put("orderUoM", orderinguom);
                        }
                        if (!ccFrequencies.isEmpty()) {
                            productMap.put("CCFrequency", ccFrequencies);
                            productMap.put("countable", true); 
                        }else{
                            productMap.put("countable", false); 
                        }
                        /*  -------------------- Indian Company Fields ------------------ */
                            
                           if (!StringUtil.isNullOrEmpty(tariffName)) {
                                productMap.put("tariffname", tariffName);
                            }
                            if (!StringUtil.isNullOrEmpty(hsncode)) {
                                productMap.put("hsCode", hsncode);
                            }
                            if (reportingSchemaType != null) {
                                productMap.put("reportingSchemaType", reportingSchemaType);
                            }
                            if (!StringUtil.isNullOrEmpty(excisemethod)) {
                                productMap.put("excisemethod", excisemethod);
                            }
                            if (!StringUtil.isNullOrEmpty(vatMethodType)) {
                                productMap.put("vatMethodType", vatMethodType);
                            }             
                            if (vatAbatementPeriodFromDate != null) {
                                productMap.put("vatAbatementPeriodFromDate", vatAbatementPeriodFromDate);
                            }
                            if (vatAbatementPeriodToDate != null) {
                                productMap.put("vatAbatementPeriodToDate", vatAbatementPeriodToDate);
                            }
                            if (!StringUtil.isNullOrEmpty(vatcommoditycode)) {
                                productMap.put("vatcommoditycode", vatcommoditycode);
                            }
                            if (!StringUtil.isNullOrEmpty(interStatePurchaseAccount)) {
                                productMap.put("interStatePurAccID", interStatePurchaseAccount);
                            }
                            if (!StringUtil.isNullOrEmpty(interStatePurchaseAccountCForm)) {
                                productMap.put("interStatePurAccCformID", interStatePurchaseAccountCForm);
                            }
                            if (!StringUtil.isNullOrEmpty(interStatePurchaseReturnAccount)) {
                                productMap.put("interStatePurReturnAccID", interStatePurchaseReturnAccount);
                            }
                            if (!StringUtil.isNullOrEmpty(interStatePurchaseReturnAccountCForm)) {
                                productMap.put("interStatePurReturnAccCformID", interStatePurchaseReturnAccountCForm);
                            }
                            if (!StringUtil.isNullOrEmpty(interStateSalesAccount)) {
                                productMap.put("interStateSalesAccID", interStateSalesAccount);
                            }
                            if (!StringUtil.isNullOrEmpty(interStateSalesAccountCForm)) {
                                productMap.put("interStateSalesAccCformID", interStateSalesAccountCForm);
                            }
                            if (!StringUtil.isNullOrEmpty(interStateSalesReturnAccount)) {
                                productMap.put("interStateSalesReturnAccID", interStateSalesReturnAccount);
                            }
                            if (!StringUtil.isNullOrEmpty(interStateSalesReturnAccountCForm)) {
                                productMap.put("interStateSalesReturnAccCformID", interStateSalesReturnAccountCForm);
                            }
                            
                        productMap.put("currencyid", currencyId);
                        if (!isUsedInTransaction) {
                            productMap.put("isBatchForProduct", isIsBatchForProduct);
                            productMap.put("isSerialForProduct", isIsSerialForProduct);
                            productMap.put("isLocationForProduct", isIslocationforproduct);
                            productMap.put("isWarehouseForProduct", isIswarehouseforproduct);
                        }

                        productMap.put("uomid", productUOMID);
                        if (!productTypeID.equals(Producttype.SERVICE)) {
                            if (uom != null) {
                                productMap.put("transferUoM", uom);
                                productMap.put("orderUoM", uom);
                            }
                            if (salesuom != null) {
                                productMap.put("salesuomid", salesuom);
                            } else if (salesuom == null && uom != null) {
                                productMap.put("salesuomid", uom);
                            }
                            if (purchaseruom != null) {
                                productMap.put("purchaseuomid", purchaseruom);
                            } else if (purchaseruom == null && uom != null) {
                                productMap.put("purchaseuomid", uom);
                            }

                            KwlReturnObject companyObj = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                            Company company = (Company) companyObj.getEntityList().get(0);
                            Packaging packaging = null;
                            if (inneruom != null || casinguom != null) {
                                packaging = new Packaging();
                                packaging.setCasingUoM(casinguom);
                                packaging.setInnerUoM(inneruom);
                                packaging.setStockUoM(uom);
                                packaging.setCasingUomValue(Double.parseDouble(casinguomvalue));
                                packaging.setInnerUomValue(Double.parseDouble(inneruomvalue));
                                packaging.setStockUomValue(Double.parseDouble(stcokuomvalue));
                                packaging.setCompany(company);

                            }
                            if (packaging != null) {
                                accProductObj.saveProductPackging(packaging);
                                productMap.put("packaging", packaging);
                            }
                        }

                        productMap.put("reorderlevel", Double.parseDouble(productReorderLevel));
                        productMap.put("reorderquantity", Double.parseDouble(productReorderQuantity));
                        productMap.put("warrantyperiod", Integer.parseInt(productWarrantyPeriod));
                        productMap.put("warrantyperiodsal", Integer.parseInt(productSalesWarrantyPeriod));
                        productMap.put("leadtime", Integer.parseInt(productLeadTime));

                        productMap.put("parentid", parentProductUUID);
                        productMap.put("salesaccountid", productSalesAccId);
//                        productMap.put("salesGL", sellassetglaccountId);
                        productMap.put("purchaseretaccountid", productPurchaseReturnAccId);
                        productMap.put("salesretaccountid", productSalesReturnAccId);
                        productMap.put("vendorid", productPreferedVendorID);
                        productMap.put("purchaseaccountid", productPurchaseAccId);
                        productMap.put("purchaseretaccountid", productPurchaseReturnAccId);

                        // if product have multiuom = 'T' and Account Preferences have UOM Setting for UOM Schema then set uomschemaType for product
                        if (uomSchemaType != null && isMultiUOM && extraCompanyPreferences != null && extraCompanyPreferences.getUomSchemaType() == 0) {
                            productMap.put("uomschemaType", uomSchemaType);
                        }
                        
                        if (!StringUtil.isNullOrEmpty(productDefaultLocationID)) {
                            productMap.put("location", productDefaultLocationID);
                        }

                        if (!StringUtil.isNullOrEmpty(productDefaultWarehouseID)) {
                            productMap.put("warehouse", productDefaultWarehouseID);
                        }

                        productMap.put("syncable", isSyncable);
                        productMap.put("multiuom", isMultiUOM);
                        productMap.put("deletedflag", false);
                        productMap.put("companyid", companyid);
                        productMap.put("isImport", 1);

                        KwlReturnObject productresult = null;
                        if (!updateExistingRecordFlag) {
                            productresult = accProductObj.addProduct(productMap);
                        } else {
                            productresult = accProductObj.updateProduct(productMap);
                        }
                        System.out.println(productName);
                        Product product = (Product) productresult.getEntityList().get(0);

//                        if (!productTypeID.equals(Producttype.SERVICE)) {
//                            // creating Price list Hashmap
//                            HashMap<String, Object> cycleParams = new HashMap<String, Object>();
//                            cycleParams.put("productid", product.getID());
//                            cycleParams.put("interval", Integer.parseInt(productCycleCountInterval));
//                            cycleParams.put("tolerance", Integer.parseInt(productCycleCountTolerance));
//                            accProductObj.saveProductCycleCount(cycleParams);
//                        }


                        if (productInitialQuantity.length() > 0) {
                            JSONObject inventoryjson = new JSONObject();
                            inventoryjson.put("productid", product.getID());
                                inventoryjson.put("quantity", Double.parseDouble(productInitialQuantity));
                            if (!isUsedInTransaction) {
                                inventoryjson.put("baseuomquantity", Double.parseDouble(productInitialQuantity));
                            }
//                            inventoryjson.put("baseuomquantity", Double.parseDouble(productInitialQuantity));
                            inventoryjson.put("baseuomrate", 1);
                            if (product.getUnitOfMeasure() != null) {
                                inventoryjson.put("uomid", product.getUnitOfMeasure().getID());
                            }
                            inventoryjson.put("description", "Inventory Opened");
                            inventoryjson.put("carryin", true);
                            inventoryjson.put("defective", false);
                            inventoryjson.put("newinventory", true);
                            inventoryjson.put("companyid", companyid);
                            inventoryjson.put("updatedate", newUserDate);
//                            accProductObj.addInventory(inventoryjson);
                            double prodInitPurchasePrice = 0;
                            if (productInitialPurchasePrise.length() > 0 && !isUsedInTransaction) {
                                prodInitPurchasePrice = Double.parseDouble(productInitialPurchasePrise);
                            }
                            if (!updateExistingRecordFlag) {
                                KwlReturnObject newInvObj=accProductObj.addInventory(inventoryjson);
                                if(!StringUtil.isNullOrEmpty(productDefaultLocationID) && !StringUtil.isNullOrEmpty(productDefaultWarehouseID) && isInventoryIntegrationOn){
//                                    accProductObj.addStockInventorySideAndAddStockMovementEntry(newInvObj,inventoryjson,productDefaultWarehouseID,productDefaultLocationID,prodInitPurchasePrice);
                                }
                            } else {
                                KwlReturnObject updatedInvObj=accProductObj.updateInitialInventory(inventoryjson);
                                if(!StringUtil.isNullOrEmpty(productDefaultLocationID) && !StringUtil.isNullOrEmpty(productDefaultWarehouseID) && isInventoryIntegrationOn){
//                                    accProductObj.updateStockInventorySideAndUpdateStockMovementEntry(updatedInvObj,inventoryjson,productDefaultWarehouseID,productDefaultLocationID,prodInitPurchasePrice);
                                }
                            }

                            HashMap<String, Object> assemblyParams =requestParams1; //AccountingManager.getGlobalParams(request);
                            assemblyParams.put("assembly", "");
                            assemblyParams.put("applydate", authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
                            assemblyParams.put("quantity", Double.parseDouble(productInitialQuantity));
                            assemblyParams.put("memo", "Inventory Opened");
                            assemblyParams.put("refno", "");
                            assemblyParams.put("buildproductid", product.getID());
                            accProductObj.updateAssemblyInventory(assemblyParams);
                        }
                        Date ondate = new Date();
                        if (productInitialPurchasePrise.length() > 0 && !isUsedInTransaction) {
                            // creating Price list Hashmap
                            HashMap<String, Object> initialPurchasePriceMap = new HashMap<String, Object>();
                            initialPurchasePriceMap.put("productid", product.getID());
                            initialPurchasePriceMap.put("companyid", companyid);
                            initialPurchasePriceMap.put("carryin", true);
                            initialPurchasePriceMap.put("price", Double.parseDouble(productInitialPurchasePrise));
                            initialPurchasePriceMap.put("applydate", authHandler.getConstantDateFormatter().parse(authHandler.getConstantDateFormatter().format(ondate)));
                            initialPurchasePriceMap.put("affecteduser", "-1");
                            initialPurchasePriceMap.put("currencyid", currencyId);
                            initialPurchasePriceMap.put("uomid", product.getUnitOfMeasure().getID());
                            accProductObj.addPriceList(initialPurchasePriceMap);
                        }

                        if (productSalesPrice.length() > 0 && !isUsedInTransaction) {
                            // creating Price list Hashmap
                            HashMap<String, Object> salesPriceMap = new HashMap<String, Object>();
                            salesPriceMap.put("productid", product.getID());
                            salesPriceMap.put("companyid", companyid);
                            salesPriceMap.put("carryin", false);
                            salesPriceMap.put("price", Double.parseDouble(productSalesPrice));
                            salesPriceMap.put("applydate", authHandler.getConstantDateFormatter().parse(authHandler.getConstantDateFormatter().format(ondate)));
                            salesPriceMap.put("affecteduser", "-1");
                            salesPriceMap.put("currencyid", currencyId);
                            salesPriceMap.put("uomid", product.getUnitOfMeasure().getID());
                            accProductObj.addPriceList(salesPriceMap);
                        }
                        // For create custom field array
                        customfield = "";
                        JSONArray customJArr = new JSONArray();
                        for (int i = 0; i < jSONArray.length(); i++) {
                            JSONObject jSONObject = jSONArray.getJSONObject(i);

                            if (jSONObject.optBoolean("customflag", false)) {//&& !jSONObject.optBoolean("isLineItem",false)
                                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                                requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.fieldlabel));
                                requestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, jSONObject.getString("columnname")));

                                KwlReturnObject fieldParamsResult = accCommonTablesDAO.getFieldParams(requestParams); // get custom field for module
                                FieldParams params = (FieldParams) fieldParamsResult.getEntityList().get(0);
                                if (jSONObject.getInt("csvindex") > recarr.length - 1) {// (csv) arrayindexoutofbound when last custom column value is empty.
                                    continue;
                                }
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
                        if (!StringUtil.isNullOrEmpty(customfield)&& !customfield.equals("[]")) {
                            JSONArray jcustomarray = new JSONArray(customfield);
                            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                            customrequestParams.put("customarray", jcustomarray);
                            customrequestParams.put("modulename", Constants.Acc_Product_modulename);
                            customrequestParams.put("moduleprimarykey", Constants.Acc_Productid);
                            customrequestParams.put("modulerecid", product.getID());
                            customrequestParams.put("moduleid", Constants.Acc_Product_Master_ModuleId);//isFixedAsset ? Constants.Acc_FixedAssets_AssetsGroups_ModuleId:
                            customrequestParams.put("companyid", companyid);
                            productMap.put("id", product.getID());
                            customrequestParams.put("customdataclasspath", Constants.Acc_Product_custom_data_classpath);
                            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                productMap.put("accproductcustomdataref", product.getID());
                                productresult = accProductObj.updateProduct(productMap);
                            }
                            HashMap<String, Object> customHistoryParams = new HashMap<String, Object>();
                            customHistoryParams.put("productId", product.getID());
                            customHistoryParams.put("customarray", jcustomarray);
                            maintainCustomFieldHistoryForProduct(requestParams1, customHistoryParams);
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
                        failedRecords.append("\n" + createCSVrecord(recarr) + "\"" + errorMsg.replaceAll("\"", "") + "\"");
                    }
                    total++;
                }
                cnt++;
                
                if (count == limit) {
                    txnManager.commit(status);
                    
                    // for reset counter of transaction limit
                    count = 1;
                    def = new DefaultTransactionDefinition();
                    def.setName("import_Tx");
                    def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
                    status = txnManager.getTransaction(def);
                } else {
                    count++;
                }
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
            if (!commitedEx) { //if exception occurs during commit then dont call rollback
                txnManager.rollback(status);
            }
            issuccess = false;
            msg = "" + ex.getMessage();

            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Importing Records.");
        } finally {
            fileInputStream.close();
            csvReader.close();

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
                logDataMap.put("Module", Constants.Acc_Product_Master_ModuleId);
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userId);
                logDataMap.put("Company", companyid);
                importDao.saveImportLog(logDataMap);
                txnManager.commit(lstatus);
            } catch (Exception ex) {
                txnManager.rollback(lstatus);
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", getActualFileName(fileName));
                returnObj.put("Module", Constants.Acc_Product_Master_ModuleId);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return returnObj;
    }
       public JSONObject importProductCategorycsv(HashMap<String,Object> requestParams) throws AccountingException, IOException, SessionExpiredException, JSONException {
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
        int count = 1;
        int limit = Constants.Transaction_Commit_Limit;
        String companyid = requestParams.get("companyid").toString();
        String userId = requestParams.get("userid").toString();
        JSONObject jobj=new JSONObject();
        jobj = (JSONObject) requestParams.get("jobj");
        String masterPreference = requestParams.get("masterPreference").toString();
        String fileName = jobj.getString("filename");
       
  
        JSONObject returnObj = new JSONObject();

        try {
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

            failedRecords.append(createCSVrecord(headArrayList.toArray()) + "\"Error Message\"");

            while ((record = br.readLine()) != null) {
                if (cnt != 0) {
                    String[] recarr = record.split(",");
                    try {
                        String productIDUUID = "";
                        String productID = "";
                        if (columnConfig.containsKey("productid")) {
                            productID = recarr[(Integer) columnConfig.get("productid")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(productID)) {
                                Product product = getProductByProductID(productID, companyid);
                                if (product != null) {
                                    productIDUUID = product.getID();
                                } else {
                                    throw new AccountingException("Product ID is not found for " + productID);
                                }
                                
                            } else {
                                throw new AccountingException("Product ID is not available.");
                            }
                        } else {
                            throw new AccountingException("Product ID column is not found.");
                        }

                        String productCategoryID = "";
                        String productCategoryName = "";
                        if (columnConfig.containsKey("productCategory")) {
                            productCategoryName = recarr[(Integer) columnConfig.get("productCategory")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(productCategoryName)) {
                                if (StringUtil.equal(productCategoryName, "None")) {
                                    productCategoryID = null;
                                } else {
                                    productCategoryID = getProductCategoryIDByName(productCategoryName, companyid);
                                    if (StringUtil.isNullOrEmpty(productCategoryID)) {
                                        throw new AccountingException("Product Category is not found for " + productCategoryName);
                                    }
                                }
                            } else {
                                throw new AccountingException("Product Category is not available.");
                            }
                        } else {
                            throw new AccountingException("Product Category column is not found.");
                        }
                        
                        if (!StringUtil.isNullOrEmpty(productCategoryID)) {
                            KwlReturnObject categoryResult = accProductObj.getProductCategoryMapping(productIDUUID, productCategoryID);
                            int nocount = categoryResult.getRecordTotalCount();
                            if (nocount > 0) {
                                throw new AccountingException("Product Category '" + productCategoryName + "' for Product ID '" + productID + "' is already exists.");
                            }
                        }
                        
                        if (!StringUtil.isNullOrEmpty(productCategoryID)) {
                            accProductObj.deleteProductCategoryMappingForNoneCategory(productIDUUID);
                        } else {
                            accProductObj.deleteProductCategoryMappingDtails(productIDUUID);
                        }
                        System.out.println(productID);
                        // For saving Product and Category Mapping
                        accProductObj.saveProductCategoryMapping(productIDUUID, productCategoryID);
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
                  if (count == limit) {
                    txnManager.commit(status);
                    
                    // for reset counter of transaction limit
                    count = 1;
                    def = new DefaultTransactionDefinition();
                    def.setName("import_Tx");
                    def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
                    status = txnManager.getTransaction(def);
                } else {
                    count++;
                }
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

            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
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
                logDataMap.put("Module", Constants.Acc_Product_Category_ModuleId);
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userId);
                logDataMap.put("Company", companyid);
                importDao.saveImportLog(logDataMap);

                String tableName = importDao.getTableName(fileName);
                importDao.removeFileTable(tableName); // Remove table after importing all records

                txnManager.commit(lstatus);
            } catch (Exception ex) {
                txnManager.rollback(lstatus);
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", getActualFileName(fileName));
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return returnObj;
    }
     private String getProductCategoryIDByName(String productCategoryName, String companyID) throws AccountingException {
        String productCategoryID = "";
        try {
            if (!StringUtil.isNullOrEmpty(productCategoryName) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accProductObj.getProductCategoryIDByName(productCategoryName, companyID);
                List list = retObj.getEntityList();
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    productCategoryID = (String) itr.next();
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Product Category");
        }
        return productCategoryID;
    }
     public String getUOMId(JSONObject params) {
        String uomid = "";
        HashMap<String, Object> filterParams = new HashMap<>();
        try {
            filterParams.put("companyid", params.getString("companyid"));
            filterParams.put("uomname", params.getString("uomname"));
            KwlReturnObject returnObject = accUomObj.getUnitOfMeasure(filterParams);
            List uomList = returnObject.getEntityList();
            if (!uomList.isEmpty()) {
                UnitOfMeasure uom = (UnitOfMeasure) uomList.get(0);
                uomid = !StringUtil.isNullObject(uom) ? uom.getID() : "";
            }
        } catch (Exception ex) {
            Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return uomid;
    }
}
