/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.view;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.CsvReader;
import com.krawler.common.util.DataInvalidateException;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.exception.InventoryException;
import com.krawler.inventory.exception.SeqFormatException;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.location.LocationService;
import com.krawler.inventory.model.sequence.ModuleConst;
import com.krawler.inventory.model.sequence.SeqFormat;
import com.krawler.inventory.model.sequence.SeqService;
import com.krawler.inventory.model.stock.StockDAO;
import com.krawler.inventory.model.stock.StockService;
import com.krawler.inventory.model.stockmovement.*;
import com.krawler.inventory.model.stockout.StockAdjustment;
import com.krawler.inventory.model.stockout.StockAdjustmentDetail;
import com.krawler.inventory.model.stockout.StockAdjustmentService;
import com.krawler.inventory.model.store.Store;
import com.krawler.inventory.model.store.StoreService;
import static com.krawler.spring.accounting.companypreferances.CompanyPreferencesCMN.removeTimefromDate;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesConstants;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.product.accProductController;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.productmodule.service.AccProductModuleService;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.importFunctionality.ImportDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
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
public class ImportDataController extends MultiActionController implements MessageSourceAware{

    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final Logger lgr = Logger.getLogger(GoodsTransferController.class.getName());
    private HibernateTransactionManager txnManager;
    private String successView;
    public ImportHandler importHandler;
    private ImportDAO importDao;
    private accProductDAO accProductObj;
    private AccountingHandlerDAO accountingHandlerDAO;
    private LocationService locationService;
    private StockService stockService;
    private StockMovementService stockMovementService;
    private StockDAO stockDAO;
    private MessageSource messageSource;
    private StoreService storeService;
    private AccProductModuleService accProductModuleService;
    private SeqService seqService;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private accJournalEntryDAO accJournalEntryobj;
    private StockAdjustmentService stockAdjustmentService;
//    @Override

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }
    
    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setStockService(StockService stockService) {
        this.stockService = stockService;
    }

    public void setStockDAO(StockDAO stockDAO) {
        this.stockDAO = stockDAO;
    }

    public void setStockMovementService(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    public void setimportDAO(ImportDAO importDao) {
        this.importDao = importDao;
    }

    public void setAccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAO) {
        this.accountingHandlerDAO = accountingHandlerDAO;
    }

    
    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }

    public void setAccProductModuleService(AccProductModuleService accProductModuleService) {
        this.accProductModuleService = accProductModuleService;
    }

    public void setSeqService(SeqService seqService) {
        this.seqService = seqService;
    }

     public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }


    public void setStockAdjustmentService(StockAdjustmentService stockAdjustmentService) {
        this.stockAdjustmentService = stockAdjustmentService;
    }

    public ModelAndView importProductStockIn(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            String eParams = request.getParameter("extraParams");
            boolean typeXLSFile = (request.getParameter("typeXLSFile") != null) ? Boolean.parseBoolean(request.getParameter("typeXLSFile")) : false;
            JSONObject extraParams = StringUtil.isNullOrEmpty(eParams) ? new JSONObject() : new JSONObject(eParams);
            extraParams.put("Company", sessionHandlerImpl.getCompanyid(request));

            String doAction = request.getParameter("do");
            HashMap<String, Object> requestParams = importHandler.getImportRequestParams(request);
            requestParams.put("extraParams", extraParams);
            requestParams.put("extraObj", null);
            requestParams.put("servletContext", this.getServletContext());
            requestParams.put("importMethod", typeXLSFile ? "xls" : "csv");
//            ServerEventManager.publish("/importdata/111", "{total:34}",this.getServletContext());

            if (doAction.compareToIgnoreCase("import") == 0) {
                System.out.println("A(( Import start : " + new Date());
                JSONObject datajobj = new JSONObject();
                JSONObject resjson = new JSONObject(request.getParameter("resjson").toString());
                JSONArray resjsonJArray = resjson.getJSONArray("root");

                String filename = request.getParameter("filename");
                datajobj.put("filename", filename);

                String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
                File filepath = new File(destinationDirectory + "/" + filename);
                datajobj.put("FilePath", filepath);

                datajobj.put("resjson", resjsonJArray);

                jobj = importProductStockInRecords(request, datajobj);

                System.out.println("A(( Import end : " + new Date());
            } else if (doAction.compareToIgnoreCase("validateData") == 0) {
                System.out.println("A(( Validation start : " + new Date());
                jobj = importHandler.validateFileData(requestParams);
                System.out.println("A(( Validation end : " + new Date());
            }
            issuccess = true;
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();

            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException e) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }

            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject importProductStockInRecords(HttpServletRequest request, JSONObject jobj) throws AccountingException, IOException, SessionExpiredException, JSONException, ServiceException {
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
        StockMovement stockMovement = new StockMovement();
        BufferedReader br = null;
        int total = 0, failed = 0;
        String companyid = sessionHandlerImpl.getCompanyid(request);

        KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyid);
        Company company = (Company) jeresult.getEntityList().get(0);
        String currencyId = sessionHandlerImpl.getCurrencyID(request);
        String userId = sessionHandlerImpl.getUserid(request);
        DateFormat df = authHandler.getDateFormatter(request);
        String fileName = jobj.getString("filename");
        String masterPreference = request.getParameter("masterPreference");
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
            failedRecords.append(createCSVrecord(headArrayList.toArray()) + "\"Error Message\""); // failedRecords.append("\"Row No.\","+createCSVrecord(fileData)+"\"Error Message\"");
            HashMap currencyMap = getCurrencyMap();
            List<StockMovement> stockmntList = new ArrayList<StockMovement>();
            while ((record = br.readLine()) != null) {
                stockMovement = new StockMovement();
                if (cnt != 0) {
                    String[] recarr = record.split(",");
                    try {
                        currencyId = sessionHandlerImpl.getCurrencyID(request);

                        String quantity = "";
                        if (columnConfig.containsKey("Quantity")) {
                            quantity = recarr[(Integer) columnConfig.get("Quantity")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(quantity)) {
                                throw new AccountingException("Product Quantity is not available");
                            }
                        } else {
                            throw new AccountingException("Product Quantity column is not found.");
                        }

                        String transactionNo = "";
                        if (columnConfig.containsKey("ponumber")) {
                            transactionNo = recarr[(Integer) columnConfig.get("ponumber")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(transactionNo)) {
                                throw new AccountingException("Ponumber is not available");
                            }
                        } else {
                            throw new AccountingException("Ponumber column is not found.");
                        }

                        String description = "";
                        if (columnConfig.containsKey("description")) {
                            description = recarr[(Integer) columnConfig.get("description")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(description)) {
                                throw new AccountingException("description is not available");
                            }
                        } else {
                            throw new AccountingException("Description column is not found.");
                        }

                        String transactionDate = "";
                        if (columnConfig.containsKey("transactionDate")) {
                            transactionDate = recarr[(Integer) columnConfig.get("transactionDate")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(transactionDate)) {
                                throw new AccountingException("TransactionDate is not available");
                            }
                        } else {
                            throw new AccountingException("TransactionDate column is not found.");
                        }

                        String serialNames = "";
                        String[] serials = null;
                        if (columnConfig.containsKey("serialNames")) {
                            serialNames = recarr[(Integer) columnConfig.get("serialNames")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(serialNames)) {
                                throw new AccountingException("SerialNames is not available");
                            } else {
                                serials = serialNames.split(";");
                                boolean vl = serialNumberValidation(serials);
                                if (!vl) {
                                    throw new AccountingException("Duplicate SerialNames ");
                                }
                            }

                        } else {
                            throw new AccountingException("SerialNames column is not found.");
                        }

                        String batchName = "";
                        if (columnConfig.containsKey("batchName")) {
                            batchName = recarr[(Integer) columnConfig.get("batchName")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(batchName)) {
                                throw new AccountingException("BatchName is not available");
                            }
                        } else {
                            throw new AccountingException("BatchName column is not found.");
                        }

                        Product product = null;
                        String productCode = "";
                        if (columnConfig.containsKey("productcode")) {
                            productCode = recarr[(Integer) columnConfig.get("productcode")].replaceAll("\"", "").trim();

                            if (StringUtil.isNullOrEmpty(productCode)) {
                                throw new AccountingException("Product Code is not available");
                            } else {
                                product = getProductByName(companyid, productCode);
                                if (product == null) {
                                    throw new AccountingException("Product Code is not available");
                                }
                            }
                        } else {
                            throw new AccountingException("Product Code is not available");
                        }

                        Store store = null;
                        String storeName = "";
                        if (columnConfig.containsKey("storename")) {
                            storeName = recarr[(Integer) columnConfig.get("storename")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(storeName)) {
                                throw new AccountingException("Store is not available");
                            } else {
                                store = getStoreByName(companyid, storeName);
                                if (store == null) {
                                    throw new AccountingException("Store is not available");
                                }
                            }
                        } else {
                            throw new AccountingException("Store is not available");
                        }

                        Location location = null;
                        String locationName = "";
                        if (columnConfig.containsKey("LocationName")) {
                            locationName = recarr[(Integer) columnConfig.get("LocationName")].replaceAll("\"", "").trim();

                            if (StringUtil.isNullOrEmpty(locationName)) {
                                throw new AccountingException("LocationName  is not available");
                            } else {
                                location = getLocationByName(companyid, locationName, store);
                                if (location == null) {
                                    throw new AccountingException("LocationName  is not available");
                                }
                            }
                        } else {
                            throw new AccountingException("LocationName  is not available");
                        }
//                        Vendor vendor = null;
//                        String vendorName = "";
//                        if (columnConfig.containsKey("vendor")) {
//                            vendorName = recarr[(Integer) columnConfig.get("vendor")].replaceAll("\"", "").trim();
//
//                            if (StringUtil.isNullOrEmpty(vendorName)) {
//                                throw new AccountingException("Vendor is not available");
//                            } else {
//                                vendor = getVendorByName(vendorName,companyid);
//                                if (vendor == null) {
//                                    throw new AccountingException("Vendor is not available");
//                                }
//                            }
//                        } else {
//                            throw new AccountingException("Vendor is not available");
//                        }

                        String productUOMID = "";
                        UnitOfMeasure uom = null;
                        if (columnConfig.containsKey("stockuom")) {
                            String productUOMName = recarr[(Integer) columnConfig.get("stockuom")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(productUOMName)) {
                                uom = getUOMByName(productUOMName, companyid);
                                if (uom != null) {
                                    productUOMID = uom.getID();
                                } else {
                                    throw new AccountingException("Product Unit Of Measure is not found for " + productUOMName);
                                }
                            }
                        }
                        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
                        Date date = ft.parse(transactionDate);
                        Date createdDate = new Date();

                        double qty = 0;
                        if (!StringUtil.isNullOrEmpty(quantity)) {
                            qty = Double.parseDouble(quantity);
                        }
                        if (serials != null) {

                            Set<StockMovementDetail> stckdetlSet = new HashSet<StockMovementDetail>();

                            String uuid = UUID.randomUUID().toString();
//                            stockMovement.setId(uuid);

                            stockMovement.setCompany(company);
                            stockMovement.setProduct(product);
                            stockMovement.setQuantity(qty);
                            stockMovement.setStockUoM(uom);
                            stockMovement.setStore(store);
                            stockMovement.setTransactionDate(date);
                            stockMovement.setTransactionModule(TransactionModule.ERP_GRN);
                            stockMovement.setTransactionNo(transactionNo);
                            stockMovement.setTransactionType(TransactionType.IN);
                            for (int i = 0; i < serials.length; i++) {
                                StockMovementDetail stckdetl = new StockMovementDetail();
                                stckdetl.setBatchName(batchName);
                                stckdetl.setLocation(location);
                                stckdetl.setQuantity(1);
                                stckdetl.setSerialNames(serials[i]);
                                stckdetl.setStockMovement(stockMovement);
                                stckdetlSet.add(stckdetl);
                            }
                            stockMovement.setStockMovementDetails(stckdetlSet);

                            stockmntList.add(stockMovement);

                            stockService.updateERPInventory(true, createdDate, product, null, uom, qty, description);
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
            }
            stockMovementService.addOrUpdateBulkStockMovement(company, null, stockmntList);
            if (failed > 0) {
                createFailureFiles(fileName, failedRecords, ".csv");
            }
            int success = total - failed;
            if (total == 0) {
                msg = "Empty file.";
            } else if (success == 0) {
                msg = messageSource.getMessage("acc.rem.169", null, RequestContextUtils.getLocale(request));
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
                returnObj.put("Module", Constants.Inv_StockIn_ModuleId);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
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

    private Product getProductByName(String company, String productName) throws AccountingException {
        Product product = null;
        try {
            if (!StringUtil.isNullOrEmpty(productName)) {
                KwlReturnObject retObj = accProductObj.getProductByProductName(company, productName);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    product = (Product) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Product Type");
        }
        return product;
    }

    private Location getLocationByName(String company, String locationName, Store storeID) throws AccountingException {
        Location location = null;
        try {
            if (!StringUtil.isNullOrEmpty(locationName)) {
                KwlReturnObject retObj = accProductObj.getLocationByName(company, locationName, storeID);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    List resultList = retObj.getEntityList();
                    if (resultList != null) {

                        String locationId = resultList.get(0).toString();
                        location = locationService.getLocation(locationId);

                    }
//                    location = (Location) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Product Type");
        }
        return location;
    }

    private Store getStoreByName(String company, String storeName) throws AccountingException {
        Store store = null;
        try {
            if (!StringUtil.isNullOrEmpty(storeName)) {
                KwlReturnObject retObj = accProductObj.getStoreByName(company, storeName);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    store = (Store) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Product Type");
        }
        return store;
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
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Unit of Measure");
        }
        return uom;
    }

    public void createFailureFiles(String filename, StringBuilder failedRecords, String ext) {
        String destinationDirectory;
        try {
            destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
            if (StringUtil.isNullOrEmpty(ext)) {
                ext = filename.substring(filename.lastIndexOf("."));
            }
            if (".xlsx".equals(ext)) {
                ext=".xls";
               destinationDirectory = storageHandlerImpl.GetDocStorePath() + "xlsfiles";
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

    public boolean serialNumberValidation(String[] serialNames) {
        boolean valid = false;
        if (serialNames.length > 0) {
            List inputList = Arrays.asList(serialNames);
            Set inputSet = new HashSet(inputList);
            if (inputSet.size() < inputList.size()) {
                valid = false;
            } else {
                valid = true;
            }
        }
        return valid;

    }
    public JSONObject importStockAdjustmentRecords(HashMap<String, Object> requestParams, Map<String, Object> jeDataMap, Map<String, Object> jeDataMap1, Locale locale) throws AccountingException, IOException, SessionExpiredException, JSONException {
//        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
//        def.setName("SA_import_Tx");
//        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
//        TransactionStatus status = txnManager.getTransaction(def);
        boolean commitedEx = false;
        boolean issuccess = true;
        String msg = "";
        FileInputStream fileInputStream = null;
//        BufferedReader br = null;
        CsvReader csvReader = null;
        int total = 0, failed = 0;
        String companyid = requestParams.get("companyid").toString();
        String currencyId = requestParams.get("currencyId").toString();
        String userId = requestParams.get("userid").toString();
        JSONObject jobj = new JSONObject();
        jobj = (JSONObject) requestParams.get("jobj");
        String dateformateproduct = jobj.getString("dateformateproduct");
        String isQAActivated = jobj.getString("isQAActivated");
        String masterPreference = requestParams.get("masterPreference").toString();
        String fileName = jobj.getString("filename");
        String delimiterType = requestParams.get("delimiterType").toString();
        Date bookBeginningDate = (Date) requestParams.get("bookbeginning");
        JSONObject returnObj = new JSONObject();
        DateFormat dateFrmt=null;
        boolean isInventoryIntegrationOn = false;
        String logId = null;
        if (requestParams.containsKey("locale")) {
            locale = (Locale) requestParams.get("locale");
        }
        JSONArray jarr = new JSONArray();
        try {
            logId = addPendingImportLog(requestParams);
            boolean isBatchForProduct = false;
            boolean isSerialForProduct = false;
            boolean isLocationForProduct = false;
            boolean isWarehouseForProduct = false;
            boolean isRowForProduct = false;
            boolean isRackForProduct = false;
            boolean isBinForProduct = false;

            KwlReturnObject kmsg = null;

            KwlReturnObject extraCompanyPrefResult = accountingHandlerDAO.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraCompanyPrefResult.getEntityList().get(0);
            isInventoryIntegrationOn = extraCompanyPreferences.isActivateInventoryTab();
            
           // CompanyAccountPreferences
                    
            extraCompanyPrefResult = accountingHandlerDAO.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) extraCompanyPrefResult.getEntityList().get(0);
            
            Date newUserDate = new Date();
            KwlReturnObject custumObjresult = accountingHandlerDAO.getObject(Company.class.getName(), companyid);
            Company company1 = (Company) custumObjresult.getEntityList().get(0);
            if (company1.getCreator() != null) {
                newUserDate = authHandler.getUserNewDate(null, company1.getCreator().getTimeZone() != null ? company1.getCreator().getTimeZone().getDifference() : company1.getTimeZone().getDifference());
            }
            String dateFormat = null, dateFormatId = dateformateproduct;
            if (!StringUtil.isNullOrEmpty(dateFormatId)) {
                KwlReturnObject kdfObj = accountingHandlerDAO.getObject(KWLDateFormat.class.getName(), dateFormatId);
                KWLDateFormat kdf = (KWLDateFormat) kdfObj.getEntityList().get(0);
                dateFormat = kdf != null ? kdf.getJavaForm() : null;
            }
            
              dateFrmt = new SimpleDateFormat(dateFormat);

            fileInputStream = new FileInputStream(jobj.getString("FilePath"));
            csvReader = new CsvReader(new InputStreamReader(fileInputStream), delimiterType);
            String record = "";
            int cnt = 0;
            int count = 1;
            int limit = Constants.Transaction_Commit_Limit;

            StringBuilder failedRecords = new StringBuilder();

            HashMap<String, Integer> columnConfig = new HashMap<String, Integer>();
            JSONArray jSONArray = jobj.getJSONArray("resjson");
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);

                columnConfig.put(jSONObject.getString("dataindex"), jSONObject.getInt("csvindex"));
            }
            //HashMap currencyMap = getCurrencyMap();

            while (csvReader.readRecord()) {
                String[] recarr = csvReader.getValues();
                if (cnt != 0) {
                    try {

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
                        KwlReturnObject result = accProductObj.getProductIDCount(productID, companyid, false);
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

                            String batchName = "";
                            String serialName = "";

                            String invalidColumns = "";
                            if (isWarehouseForProduct) {
                                if (columnConfig.containsKey("warehouse") && recarr.length > (Integer) columnConfig.get("warehouse")) {
                                    String value = recarr[(Integer) columnConfig.get("warehouse")].replaceAll("\"", "").trim();
                                    if (StringUtil.isNullOrEmpty(value)) {
                                        if (!StringUtil.isNullOrEmpty(invalidColumns)) {
                                            invalidColumns += ",";
                                        }
                                        invalidColumns += " Warehouse";
                                    }
                                } else {
                                    if (!StringUtil.isNullOrEmpty(invalidColumns)) {
                                        invalidColumns += ",";
                                    }
                                    invalidColumns += " Warehouse";
                                }
                            }
                            if (isLocationForProduct) {
                                if (columnConfig.containsKey("location") && recarr.length > (Integer) columnConfig.get("location")) {
                                    String value = recarr[(Integer) columnConfig.get("location")].replaceAll("\"", "").trim();
                                    if (StringUtil.isNullOrEmpty(value)) {
                                        if (!StringUtil.isNullOrEmpty(invalidColumns)) {
                                            invalidColumns += ",";
                                        }
                                        invalidColumns += " Location";
                                    }
                                } else {
                                    if (!StringUtil.isNullOrEmpty(invalidColumns)) {
                                        invalidColumns += ",";
                                    }
                                    invalidColumns += " Location";
                                }
                            }
                            if (isRowForProduct) {
                                if (columnConfig.containsKey("row") && recarr.length > (Integer) columnConfig.get("row")) {
                                    String value = recarr[(Integer) columnConfig.get("row")].replaceAll("\"", "").trim();
                                    if (StringUtil.isNullOrEmpty(value)) {
                                        if (!StringUtil.isNullOrEmpty(invalidColumns)) {
                                            invalidColumns += ",";
                                        }
                                        invalidColumns += " Row";
                                    }
                                } else {
                                    if (!StringUtil.isNullOrEmpty(invalidColumns)) {
                                        invalidColumns += ",";
                                    }
                                    invalidColumns += " Row";
                                }
                            }
                            if (isRackForProduct) {
                                if (columnConfig.containsKey("rack") && recarr.length > (Integer) columnConfig.get("rack")) {
                                    String value = recarr[(Integer) columnConfig.get("rack")].replaceAll("\"", "").trim();
                                    if (StringUtil.isNullOrEmpty(value)) {
                                        if (!StringUtil.isNullOrEmpty(invalidColumns)) {
                                            invalidColumns += ",";
                                        }
                                        invalidColumns += " Rack";
                                    }
                                } else {
                                    if (!StringUtil.isNullOrEmpty(invalidColumns)) {
                                        invalidColumns += ",";
                                    }
                                    invalidColumns += " Rack";
                                }
                            }
                            if (isBinForProduct) {
                                if (columnConfig.containsKey("bin") && recarr.length > (Integer) columnConfig.get("bin")) {
                                    String value = recarr[(Integer) columnConfig.get("bin")].replaceAll("\"", "").trim();
                                    if (StringUtil.isNullOrEmpty(value)) {
                                        if (!StringUtil.isNullOrEmpty(invalidColumns)) {
                                            invalidColumns += ",";
                                        }
                                        invalidColumns += " Bin";
                                    }
                                } else {
                                    if (!StringUtil.isNullOrEmpty(invalidColumns)) {
                                        invalidColumns += ",";
                                    }
                                    invalidColumns += " Bin";
                                }
                            }

                            if (isBatchForProduct) {
                                if (columnConfig.containsKey("batchname") && recarr.length > (Integer) columnConfig.get("batchname")) {
                                    String value = recarr[(Integer) columnConfig.get("batchname")].replaceAll("\"", "").trim();
                                    batchName = value;
                                    if (StringUtil.isNullOrEmpty(value)) {
                                        invalidColumns += " Batch";
                                         throw new AccountingException(invalidColumns + " is not available.");
                                    }
                                } else {
                                    if (!StringUtil.isNullOrEmpty(invalidColumns)) {
                                        invalidColumns += ",";
                                    }
                                    invalidColumns += " Batch";
                                     throw new AccountingException(invalidColumns + " is not available.");
                                }
                            }
                            if (isSerialForProduct) {
                                if (columnConfig.containsKey("serialname") && recarr.length > (Integer) columnConfig.get("serialname")) {
                                    String value = recarr[(Integer) columnConfig.get("serialname")].replaceAll("\"", "").trim();
                                    serialName = value;
                                    if (StringUtil.isNullOrEmpty(value)) {
                                         invalidColumns += " Serial";
                                        throw new AccountingException(invalidColumns + " is not available.");                               
                                    }
                                } else {
                                    if (!StringUtil.isNullOrEmpty(invalidColumns)) {
                                        invalidColumns += ",";
                                    }
                                    invalidColumns += " Serial";
                                    throw new AccountingException(invalidColumns + " is not available.");        
                                }
                            }
                            String reason = "";
                            if (columnConfig.containsKey("reason") && recarr.length > (Integer) columnConfig.get("reason")) {
                                String value = recarr[(Integer) columnConfig.get("reason")].replaceAll("\"", "").trim();
                                reason = value;
                                if (StringUtil.isNullOrEmpty(value)) {
                                    if (!StringUtil.isNullOrEmpty(invalidColumns)) {
                                        invalidColumns += ",";
                                    }
                                    invalidColumns += " reason";
                                }
                            } else {
                                if (!StringUtil.isNullOrEmpty(invalidColumns)) {
                                    invalidColumns += ",";
                                }
                                invalidColumns += "reason";
                            }
                            String type = "";
                            if (columnConfig.containsKey("Type") && recarr.length > (Integer) columnConfig.get("Type")) {
                                String value = recarr[(Integer) columnConfig.get("Type")].replaceAll("\"", "").trim();
                                type = value;
                                if (StringUtil.isNullOrEmpty(value)) {
                                    if (!StringUtil.isNullOrEmpty(invalidColumns)) {
                                        invalidColumns += ",";
                                    }
                                    invalidColumns += " Type";
                                }
                            } else {
                                if (!StringUtil.isNullOrEmpty(invalidColumns)) {
                                    invalidColumns += ",";
                                }
                                invalidColumns += "Type";
                            }

                            if (!StringUtil.isNullOrEmpty(invalidColumns)) {
                                throw new AccountingException(invalidColumns + " is not available.");
                            }
                            
                            if (StringUtil.isNullOrEmpty(type)) {
                                throw new AccountingException(invalidColumns + " is not available.");
                            }
                            
                            if (!StringUtil.isNullOrEmpty(type) && !(("Stock Sales").equalsIgnoreCase(type) || ("Stock OUT").equalsIgnoreCase(type) || ("Stock IN").equalsIgnoreCase(type))) {
                                throw new AccountingException("Transaction type " + type + " is not available.");
                            }
                            
                            String memo = "";
                            if (columnConfig.containsKey("memo") && recarr.length > (Integer) columnConfig.get("memo")) {
                                String value = recarr[(Integer) columnConfig.get("memo")].replaceAll("\"", "").trim();
                                memo = value;
                                if (StringUtil.isNullOrEmpty(value)) {

                                    invalidColumns += " Memo";
                                    throw new AccountingException(invalidColumns + " is not available.");
                                }
                            } else {
                                if (!StringUtil.isNullOrEmpty(invalidColumns)) {
                                    invalidColumns += ",";
                                }
                                invalidColumns += "Memo";
                                throw new AccountingException(invalidColumns + " is not available.");
                            }
                            String businessdate = "";
                            Date bussinessDate=null;
                            boolean isValiddate=true;
                            if (columnConfig.containsKey("businessdate") && recarr.length > (Integer) columnConfig.get("businessdate")) {
                                String value = recarr[(Integer) columnConfig.get("businessdate")].replaceAll("\"", "").trim();
                                businessdate = value;
                               
                                if (StringUtil.isNullOrEmpty(value)) {
                                    invalidColumns += " businessdate";
                                    throw new AccountingException(invalidColumns + " is not available.");
                                } else {
                                    try {
                                        bussinessDate = dateFrmt.parse(businessdate);
                                         isValiddate=isvalidDate(bussinessDate,companyAccountPreferences);
                                    } catch (ParseException ex) {
                                        throw new AccountingException("Incorrect date format for Business Date, Please specify values in " + dateFormat + " format.");
                                    }
                                }
                            } else {
                                if (!StringUtil.isNullOrEmpty(invalidColumns)) {
                                    invalidColumns += ",";
                                }
                                invalidColumns += "businessdate";
                                throw new AccountingException(invalidColumns + " is not available.");
                            }if(!isValiddate){
                                throw new AccountingException("Bussiness date should not be before than Book Beginning Date.");
//                                throw new AccountingException(invalidColumns + " In valid business date format ");
                            }
                            
                            String expdateStr = "";
                            if ("Stock IN".equalsIgnoreCase(type) && (isBatchForProduct || isSerialForProduct)) {
                                if (columnConfig.containsKey("expdate")) {
                                    expdateStr = recarr[(Integer) columnConfig.get("expdate")].replaceAll("\"", "").trim();
                                    if (!StringUtil.isNullOrEmpty(expdateStr)) {
                                        try {
                                            dateFrmt.parse(expdateStr);
                                        } catch (Exception ex) {
                                            throw new AccountingException("Incorrect date format for expriy Date, Please specify values in " + dateFormat + " format. ");
                                        }
                                    }
                                }
                            }

                            HashMap<String, StoreMaster> storeMasterSet = new HashMap<String, StoreMaster>();
                            StoreMaster row = null;
                            String productRowName = null;
                            String productRowId = null;
                            if (columnConfig.containsKey("row") && isRowForProduct) {
                                productRowName = recarr[(Integer) columnConfig.get("row")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(productRowName)) {
                                    row = storeService.getStoreMasterByName(productRowName, companyid, 1);
                                    if (row == null) {
                                        throw new AccountingException("Row is not available.");
                                    } else {
                                        productRowId = row.getId();
                                        storeMasterSet.put("row", row);
                                    }
                                } else {
                                    throw new AccountingException("Row is not available.");
                                }
                            }

                            StoreMaster rack = null;
                            String productRackName = null;
                            String productRackId = null;
                            if (columnConfig.containsKey("rack") && isRackForProduct) {
                                productRackName = recarr[(Integer) columnConfig.get("rack")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(productRackName)) {
                                    rack = storeService.getStoreMasterByName(productRackName, companyid, 2);
                                    if (rack == null) {
                                        throw new AccountingException("Rack is not available.");
                                    } else {
                                        productRackId = rack.getId();
                                        storeMasterSet.put("rack", rack);
                                    }
                                } else {
                                    throw new AccountingException("Rack is not available.");
                                }
                            }
                            StoreMaster bin = null;
                            String productBinName = null;
                            String productBinId = null;
                            if (columnConfig.containsKey("bin") && isBinForProduct) {
                                productBinName = recarr[(Integer) columnConfig.get("bin")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(productBinName)) {
                                    bin = storeService.getStoreMasterByName(productBinName, companyid, 3);
                                    if (bin == null) {
                                        throw new AccountingException("Bin is not available.");
                                    } else {
                                        productBinId = bin.getId();
                                        storeMasterSet.put("bin", bin);
                                    }
                                } else {
                                    throw new AccountingException("Bin is not available.");
                                }
                            }
                            String productDefaultWarehouseID = "";
                            if (isWarehouseForProduct && columnConfig.containsKey("warehouse")) {
                                String productDefaultWarehouseName = recarr[(Integer) columnConfig.get("warehouse")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(productDefaultWarehouseName)) {
                                    InventoryWarehouse invWHouse = accProductModuleService.getInventoryWarehouseByName(productDefaultWarehouseName, companyid);
                                    if (invWHouse != null) {
                                        productDefaultWarehouseID = invWHouse.getId();
                                    }
                                }
                            }
                            if (StringUtil.isNullOrEmpty(productDefaultWarehouseID)) {
                                throw new AccountingException("warehouse is not available.");
                            }

                            String productDefaultLocationID = "";
                            if (isLocationForProduct && columnConfig.containsKey("location")) {
                                String productDefaultLocationName = recarr[(Integer) columnConfig.get("location")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(productDefaultLocationName)) {
                                    InventoryLocation invLoc = accProductModuleService.getInventoryLocationByName(productDefaultLocationName, companyid);
                                    if (invLoc != null) {
                                        productDefaultLocationID = invLoc.getId();
                                    }
                                }
                            }
                            if (StringUtil.isNullOrEmpty(productDefaultLocationID)) {
                                throw new AccountingException("location is not available.");
                            }

                            if (!StringUtil.isNullOrEmpty(reason)) {
                                result = accJournalEntryobj.getMasterItemByNameorID(companyid, reason, "31");
                                List list = result.getEntityList();
                                if (list != null && list.size() > 0) {
                                    Iterator itr = list.iterator();
                                    while (itr.hasNext()) {
//                                        Object[] values = (Object[]) itr.next();
                                        MasterItem fieldComboData = (MasterItem) itr.next();
                                        reason = fieldComboData.getID();
                                    }
                                }
//                                else if (masterPreference.equalsIgnoreCase("2")) {
//                                     // create new reason    
//                                    MasterItem msItem=new MasterItem();
//                                    String Id=java.util.UUID.randomUUID().toString();
//                                    msItem.setID(Id);
//                                    msItem.setActivated(true);
//                                    msItem.setValue(reason);
//                                    msItem.setCompany(company1);
//                                    KwlReturnObject kwlObj = accountingHandlerDAO.getObject(MasterGroup.class.getName(), "31");
//                                    MasterGroup masterGroup = (MasterGroup) kwlObj.getEntityList().get(0);
//                                    msItem.setMasterGroup(masterGroup);
////                                    stockDAO.saveOrUpdate(msItem);
////                                    reason=Id;
//                                } else {
//                                    throw new AccountingException("reason is not available.");
//                                }
                            } else {
                                throw new AccountingException("reason is not available.");
                            }

                            String quantity = "";
                            if (columnConfig.containsKey("quantity")) {
                                quantity = recarr[(Integer) columnConfig.get("quantity")].replaceAll("\"", "").trim();
                                if (StringUtil.isNullOrEmpty(quantity)) {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        quantity = "0";
                                    } else {
                                        if (product.getProducttype().equals(Producttype.SERVICE)) {
                                            quantity = "";
                                        } else {
                                            throw new AccountingException("Product  Quantity is not available");
                                        }
                                    }
                                }
                            }
                            String unitPrice = "";
                            if (columnConfig.containsKey("unitprice")) {
                                unitPrice = recarr[(Integer) columnConfig.get("unitprice")].replaceAll("\"", "").trim();
                                if (StringUtil.isNullOrEmpty(unitPrice)) {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        unitPrice = "0";
                                    } else {
                                        if (product.getProducttype().equals(Producttype.SERVICE)) {
                                            unitPrice = "";
                                        } else {
                                            throw new AccountingException("unitprice is not available");
                                        }
                                    }
                                }
                            } else {
                                throw new AccountingException("unitprice is not available");
                            }
                            String finalSerials = "";
                            NewProductBatch productBatch = null;
                            if (!("Stock IN".equalsIgnoreCase(type))) {
                                productBatch = stockDAO.getERPProductBatch(product.getID(), productDefaultWarehouseID, productDefaultLocationID, row, rack, bin, batchName, companyid);
                                if (productBatch == null || (productBatch.getQuantitydue() < Double.parseDouble(quantity))) {
                                    throw new AccountingException("Quantity is not available");
                                } else {
                                    if (isSerialForProduct && !StringUtil.isNullOrEmpty(serialName)) {
                                        serialName = serialName.trim();
                                        String[] serArr = serialName.split(",");
                                        for (int i = 0; i < serArr.length; i++) {
                                            
//                                            boolean isvalid=stockService.getISSerialisValidforOut(product, productDefaultWarehouseID, productDefaultLocationID, bussinessDate, serArr[i]);
//                                            if (isvalid) {
                                                NewBatchSerial batchSer = stockDAO.getERPBatchSerial(product.getID(), productBatch, serArr[i], companyid);
                                                if (batchSer != null && batchSer.getQuantitydue() >= 0) {
                                                    if (!StringUtil.isNullOrEmpty(finalSerials)) {
                                                        finalSerials += "," + serArr[i];
                                                    } else {
                                                        finalSerials = serArr[i];
                                                    }
                                                }
//                                            }
                                        }
                                        if (StringUtil.isNullOrEmpty(finalSerials)) {
                                            throw new AccountingException("serial is is not available");
                                        }
                                    }
                                }
                            } else if ("Stock IN".equalsIgnoreCase(type)) {
                                if (isSerialForProduct && !StringUtil.isNullOrEmpty(serialName)) {
                                    serialName = serialName.trim();
                                    String[] serArr = serialName.split(",");
                                    for (int i = 0; i < serArr.length; i++) {
                                        boolean isexist = stockDAO.isSerialExists(product, batchName, serArr[i]);
                                        if (!isexist && !StringUtil.isNullOrEmpty(finalSerials)) {
                                             finalSerials += "," + serArr[i];
                                        } else if (!isexist) {
                                           finalSerials = serArr[i];
                                        }
                                    }
                                    if (StringUtil.isNullOrEmpty(finalSerials)) {
                                        throw new AccountingException("serial already exist is not available");
                                    }
                                }
                            }

                            Inventory invetory = null;
                            if (quantity.length() > 0) {
                                JSONObject inventoryjson = new JSONObject();
                                double invIntialqty = Double.parseDouble(quantity);
                                double cost = Double.parseDouble(unitPrice);
                                if(isSerialForProduct){
                                    String[] srArr=finalSerials.split(",");
                                    invIntialqty=srArr.length;
                                }
                                if (!("Stock IN".equalsIgnoreCase(type)) && !StringUtil.isNullOrEmpty(quantity)) {
                                    invIntialqty = Double.parseDouble(quantity);
                                    if (isSerialForProduct) {
                                        String[] srArr = finalSerials.split(",");
                                        invIntialqty = srArr.length;
                                    }
                                    invIntialqty = -1 * invIntialqty;
                                    cost=0;
                                }
                                double prodInitPurchasePrice = 0;
                                KwlReturnObject initPurchasePriceObj = accProductObj.getInitialPrice(product.getID(), true);

                                if (initPurchasePriceObj != null && initPurchasePriceObj.isSuccessFlag() && initPurchasePriceObj.getEntityList() != null && initPurchasePriceObj.getEntityList().get(0) != null) {
                                    prodInitPurchasePrice = (double) initPurchasePriceObj.getEntityList().get(0);
                                }

                                if ((product.isIswarehouseforproduct() && StringUtil.isNullOrEmpty(productDefaultWarehouseID) && product.isIslocationforproduct() && StringUtil.isNullOrEmpty(productDefaultLocationID))) {
                                    throw new AccountingException("Warehouse & Location is enabled for this product but their values are not provided.");
                                } else if ((product.isIswarehouseforproduct() && StringUtil.isNullOrEmpty(productDefaultWarehouseID))) {
                                    throw new AccountingException("Warehouse is enabled for this product but its value is not provided.");
                                } else if ((product.isIslocationforproduct() && StringUtil.isNullOrEmpty(productDefaultLocationID))) {
                                    throw new AccountingException("Location is enabled for this product but its value is not provided.");
                                } else if ((isRowForProduct && StringUtil.isNullOrEmpty(productRowId))) {
                                    throw new AccountingException("Row is enabled for this product but its value is not provided.");
                                } else if ((isRackForProduct && StringUtil.isNullOrEmpty(productRackId))) {
                                    throw new AccountingException("Rack is enabled for this product but its value is not provided.");
                                } else if ((isBinForProduct && StringUtil.isNullOrEmpty(productBinId))) {
                                    throw new AccountingException("Bin is enabled for this product but its value is not provided.");
                                } else {

                                    JSONObject jdata = new JSONObject();

                                    jdata.put("companyid", companyid);
                                    jdata.put("quantity", String.valueOf(invIntialqty));
                                    jdata.put("purchaseprice", ("Stock IN".equals(type))?String.valueOf(cost):prodInitPurchasePrice);
                                    jdata.put("product", product.getID());
                                    jdata.put("warehouse", productDefaultWarehouseID);
                                    jdata.put("uomid", "");
                                    jdata.put("reason", reason);
                                    jdata.put("memo", memo);
                                    jdata.put("businessdate", businessdate);
                                    jdata.put("adjustmentType", type);//This is GRN Type Tranction  

                                    JSONObject detailObj = new JSONObject();
                                    detailObj.put("quantity", invIntialqty);
                                    detailObj.put("locationId", productDefaultLocationID);
                                    detailObj.put("rowId", productRowId);
                                    detailObj.put("rackId", productRackId);
                                    detailObj.put("binId", productBinId);
                                    detailObj.put("batchName", batchName);
                                    detailObj.put("serialNames", finalSerials);
                                    detailObj.put("skuFields", "");
                                    detailObj.put("approvalSerials", finalSerials);
                                    detailObj.put("mfgdate", "");
                                    if ("Stock IN".equals(type) && (isBatchForProduct || isSerialForProduct)) {
                                        detailObj.put("expdate", expdateStr);
                                    }

                                    JSONArray dtlArr = new JSONArray();
                                    dtlArr.put(detailObj);

                                    jdata.put("stockDetails", dtlArr);

                                    jarr.put(jdata);

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
                    failedRecords.append(createCSVrecord(recarr) + "\"Error Message\"");
                }
                cnt++;
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

        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Importing Records.");
        } finally {
            fileInputStream.close();
            csvReader.close();
            HashMap<String, Object> logDataMap = new HashMap<String, Object>();
            try {
                //Insert Integration log

                logDataMap.put("FileName", getActualFileName(fileName));
                logDataMap.put("StorageName", fileName);
                logDataMap.put("Log", msg);
                logDataMap.put("Type", "csv");
                logDataMap.put("FailureFileType", failed > 0 ? "csv" : "");
                logDataMap.put("TotalRecs", total);
                logDataMap.put("Rejected", failed);

                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userId);
                logDataMap.put("Company", companyid);
                logDataMap.put("Id", logId);

                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", getActualFileName(fileName));
                returnObj.put("data", jarr);
                returnObj.put("logData", logDataMap);

                if (issuccess && jarr.length() > 0) {

                    Map<String,Object> parmList=new HashMap<String,Object>();
                    parmList.put("logDataMap", logDataMap);
                    parmList.put("returnObj", returnObj);
                    parmList.put("companyid", companyid);
                    parmList.put("dateformat", dateFrmt);
                    parmList.put("userId", userId);
                    parmList.put("currencyId", currencyId);
                    parmList.put("jeDataMap", jeDataMap);
                    parmList.put("jeDataMap1", jeDataMap1);
                    parmList.put("locale", locale);
                    parmList.put("masterPreference", masterPreference);
                    parmList.put("isQAActivated", isQAActivated);

                    returnObj = requestStockAdjustment(parmList);
                }else if(issuccess){
                    importDao.saveImportLog(logDataMap);
                    String tableName = importDao.getTableName(logDataMap.get("StorageName").toString());
                    importDao.removeFileTable(tableName); // Remove table after importing all records
                }


            } catch (AccountingException ex) {
                throw new AccountingException(ex.getMessage());
            } catch (Exception ex) {
//                txnManager.rollback(lstatus);
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return returnObj;
    }
    public JSONObject importStockAdjustmentRecordsXls(HashMap<String, Object> requestParams, Map<String, Object> jeDataMap, Map<String, Object> jeDataMap1, Locale locale) throws AccountingException, IOException, SessionExpiredException, JSONException {
        boolean commitedEx = false;
        boolean issuccess = true;
        String msg = "";
        FileInputStream fileInputStream = null;
//        BufferedReader br = null;
        CsvReader csvReader = null;
        int total = 0, failed = 0;
        String companyid = requestParams.get("companyid").toString();
        String currencyId = requestParams.get("currencyId").toString();
        String userId = requestParams.get("userid").toString();
        JSONObject jobj = new JSONObject();
        jobj = (JSONObject) requestParams.get("jobj");
        String dateformateproduct =  importHandler.getDFPattern();
        String masterPreference = requestParams.get("masterPreference").toString();
        String isQAActivated = jobj.getString("isQAActivated");
        String fileName = jobj.optString("filename");
        JSONObject returnObj = new JSONObject();
        DateFormat dateFrmt=null;
        boolean isInventoryIntegrationOn = false;
        String logId = null;
        if (requestParams.containsKey("locale")) {
            locale = (Locale) requestParams.get("locale");
        }
        JSONArray jarr = new JSONArray();
        String failureMsg = "";
        try {
            logId = addPendingImportLog(requestParams);
            boolean isBatchForProduct = false;
            boolean isSerialForProduct = false;
            boolean isLocationForProduct = false;
            boolean isWarehouseForProduct = false;
            boolean isRowForProduct = false;
            boolean isRackForProduct = false;
            boolean isBinForProduct = false;

            KwlReturnObject kmsg = null;

            KwlReturnObject extraCompanyPrefResult = accountingHandlerDAO.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraCompanyPrefResult.getEntityList().get(0);
            isInventoryIntegrationOn = extraCompanyPreferences.isActivateInventoryTab();
            Date newUserDate = new Date();
            KwlReturnObject custumObjresult = accountingHandlerDAO.getObject(Company.class.getName(), companyid);
            Company company1 = (Company) custumObjresult.getEntityList().get(0);
           
            extraCompanyPrefResult = accountingHandlerDAO.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) extraCompanyPrefResult.getEntityList().get(0);

            if (company1.getCreator() != null) {
                newUserDate = authHandler.getUserNewDate(null, company1.getCreator().getTimeZone() != null ? company1.getCreator().getTimeZone().getDifference() : company1.getTimeZone().getDifference());
            }
            dateFrmt = new SimpleDateFormat(dateformateproduct);
            
            int sheetNo = Integer.parseInt(requestParams.get("sheetindex").toString());
            FileInputStream fs = new FileInputStream(jobj.getString("FilePath"));
            Workbook wb = WorkbookFactory.create(fs);// create workbook for all types of excel file like xls, xlsx etc
            Sheet sheet = wb.getSheetAt(sheetNo);
            int cnt = 0;
            StringBuilder failedRecords = new StringBuilder();
            HashMap<String, Integer> columnConfig = new HashMap<>();
            Map<String, JSONObject> configMap = new HashMap<>();
            JSONArray jSONArray = jobj.getJSONArray("resjson");
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
                columnConfig.put(jSONObject.getString("dataindex"), jSONObject.getInt("csvindex"));
                configMap.put(jSONObject.getString("dataindex"), jSONObject);
            }
            List failureArr = new ArrayList();
            List failureColumnArr = new ArrayList();
            List recarr = new ArrayList();
            int maxCol = 0;
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                failureMsg = "";
                Map<Integer, Object> invalidColumn = new HashMap<>();
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                if (i == 0) {
                    maxCol = row.getLastCellNum();
                    recarr = new ArrayList();
                    for (int cellcount = 0; cellcount < maxCol; cellcount++) {
                        Cell cell = row.getCell(cellcount);

                        if (cell != null) {
                            if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                                cell.setCellValue(cell.getStringCellValue().replaceAll("\n", ""));
                            } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                                String CellStringValue = Double.toString((Double) cell.getNumericCellValue()).replaceAll("\n", "");
                                cell.setCellValue(Double.parseDouble(CellStringValue)); //Parsed to Doouble as getnumericCellValue returns double by Default
                            }
                            recarr.add(cell);
                        } else {
                            recarr.add("");
                        }
                    }
                }
                ArrayList failureRecArr = new ArrayList();
                failureRecArr.addAll(recarr);
                failureRecArr.add("Error Message");
                failureArr.add(failureRecArr);
                failureColumnArr.add(invalidColumn);

                if (cnt != 0) {
                    recarr = new ArrayList();
                    for (int cellcount = 0; cellcount < maxCol; cellcount++) {
                        Cell cell = row.getCell(cellcount);

                        if (cell != null) {
                            if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                                cell.setCellValue(cell.getStringCellValue().replaceAll("\n", ""));
                            } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                                String CellStringValue = Double.toString((Double) cell.getNumericCellValue()).replaceAll("\n", "");
                                cell.setCellValue(Double.parseDouble(CellStringValue)); //Parsed to Doouble as getnumericCellValue returns double by Default
                            }
                            recarr.add(cell);
                        } else {
                            recarr.add("");
                        }
                    }
                    
                    try{
                        
                        String productID = "";
                        if (columnConfig.containsKey("productid")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("productid"));
                            if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
                                invalidColumn.put((Integer) columnConfig.get("productid"), "Invalid");
                                failureMsg += "Product ID is not available.";
                                throw new AccountingException("Product ID is not available.");
                            } else {
                                productID = importHandler.getCellValue(cell);
                            }
                        } else {
                            throw new AccountingException("Product ID is not available.");
                        }
                        
                        // getting product object
                        KwlReturnObject result = accProductObj.getProductIDCount(productID, companyid, false);
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

                            String batchName = "";
                            String serialName = "";

                            String invalidColumns = "";
                            if (isWarehouseForProduct) {
                                if (columnConfig.containsKey("warehouse") && recarr.size() > (Integer) columnConfig.get("warehouse")) {
                                    Cell cell = row.getCell((Integer) columnConfig.get("warehouse"));
                                    if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
                                        invalidColumn.put((Integer) columnConfig.get("warehouse"), "Invalid");
                                        failureMsg += "Product ID is not available.";
                                    } 
                                } else {
                                    if (!StringUtil.isNullOrEmpty(invalidColumns)) {
                                        invalidColumns += ",";
                                    }
                                    invalidColumns += " Warehouse";
                                }
                            }
                            if (isLocationForProduct) {
                                if (columnConfig.containsKey("location") && recarr.size() > (Integer) columnConfig.get("location")) {
                                    Cell cell = row.getCell((Integer) columnConfig.get("location"));
                                    if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
                                        invalidColumn.put((Integer) columnConfig.get("location"), "Invalid");
                                        failureMsg += "location ID is not available.";
                                    } 
                                } else {
                                    if (!StringUtil.isNullOrEmpty(invalidColumns)) {
                                        invalidColumns += ",";
                                    }
                                    invalidColumns += " location";
                                }
                            }
                            if (isLocationForProduct) {
                                if (columnConfig.containsKey("location") && recarr.size() > (Integer) columnConfig.get("location")) {
                                    Cell cell = row.getCell((Integer) columnConfig.get("location"));
                                    if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
                                        invalidColumn.put((Integer) columnConfig.get("location"), "Invalid");
                                        failureMsg += "location ID is not available.";
                                    } 
                                } else {
                                    if (!StringUtil.isNullOrEmpty(invalidColumns)) {
                                        invalidColumns += ",";
                                    }
                                    invalidColumns += " location";
                                }
                            }
                            
                            if (isBatchForProduct) {
                                if (columnConfig.containsKey("batchname") && recarr.size() > (Integer) columnConfig.get("batchname")) {
                                    Cell cell = row.getCell((Integer) columnConfig.get("batchname"));
                                    batchName = importHandler.getCellValue(cell);
                                    if (StringUtil.isNullOrEmpty(batchName)) {
                                        invalidColumns += " Batch";
                                        throw new AccountingException(invalidColumns + " is not available.");
                                    }
                                } else {
                                    if (!StringUtil.isNullOrEmpty(invalidColumns)) {
                                        invalidColumns += ",";
                                    }
                                    invalidColumns += " Batch";
                                     throw new AccountingException(invalidColumns + " is not available.");
                                }
                            }
                            if (isSerialForProduct) {
                                if (columnConfig.containsKey("serialname") && recarr.size() > (Integer) columnConfig.get("serialname")) {
                                     Cell cell = row.getCell((Integer) columnConfig.get("serialname"));
                                    String value = importHandler.getCellValue(cell);
                                    serialName = value;
                                    if (StringUtil.isNullOrEmpty(value)) {
                                       invalidColumns = " Serialname";
                                        throw new AccountingException(invalidColumns + " is not available.");
                                    }
                                } else {
                                    if (!StringUtil.isNullOrEmpty(invalidColumns)) {
                                        invalidColumns += ",";
                                    }
                                    invalidColumns += " Serial";
                                     throw new AccountingException(invalidColumns + " is not available.");
                                }
                            }
                            String reason = "";
                            if (columnConfig.containsKey("reason") && recarr.size() > (Integer) columnConfig.get("reason")) {
                                 Cell cell = row.getCell((Integer) columnConfig.get("reason"));
                                    String value = importHandler.getCellValue(cell);
                                reason = value;
                                if (StringUtil.isNullOrEmpty(value)) {
                                    if (!StringUtil.isNullOrEmpty(invalidColumns)) {
                                        invalidColumns += ",";
                                    }
                                    invalidColumns += " reason";
                                }
                            } else {
                                if (!StringUtil.isNullOrEmpty(invalidColumns)) {
                                    invalidColumns += ",";
                                }
                                invalidColumns += "reason";
                            }
                            
                            String type = "";
                            if (columnConfig.containsKey("Type") && recarr.size() > (Integer) columnConfig.get("Type")) {
                                 Cell cell = row.getCell((Integer) columnConfig.get("Type"));
                                    String value = importHandler.getCellValue(cell);
                                type = value;
                                if (StringUtil.isNullOrEmpty(value)) {
                                    if (!StringUtil.isNullOrEmpty(invalidColumns)) {
                                        invalidColumns += ",";
                                    }
                                    invalidColumns += " Type";
                                }
                            } else {
                                if (!StringUtil.isNullOrEmpty(invalidColumns)) {
                                    invalidColumns += ",";
                                }
                                invalidColumns += "Type";
                            }

                            if (StringUtil.isNullOrEmpty(type)) {
                                throw new AccountingException(invalidColumns + " is not available.");
                            }
                            if (!StringUtil.isNullOrEmpty(type) && !(("Stock Sales").equalsIgnoreCase(type) || ("Stock OUT").equalsIgnoreCase(type) || ("Stock IN").equalsIgnoreCase(type))) {
                                throw new AccountingException("Transaction type " + type + " is not available.");
                            }
                            
                            String memo = "";
                            if (columnConfig.containsKey("memo") && recarr.size() > (Integer) columnConfig.get("memo")) {
                                 Cell cell = row.getCell((Integer) columnConfig.get("memo"));
                                   String value = importHandler.getCellValue(cell);
                                memo = value;
                                if (StringUtil.isNullOrEmpty(value)) {
                                    if (!StringUtil.isNullOrEmpty(invalidColumns)) {
                                        invalidColumns += ",";
                                    }
                                    invalidColumns += " Memo";
                                }
                            } else {
                                if (!StringUtil.isNullOrEmpty(invalidColumns)) {
                                    invalidColumns += ",";
                                }
                                invalidColumns += "Memo";
                            }

                            if (StringUtil.isNullOrEmpty(memo)) {
                                throw new AccountingException(invalidColumns + " is not available.");
                            }

                            String businessdate = "";
                            Date bussinessDate=null;
                            boolean isValiddate=true;
                            if (columnConfig.containsKey("businessdate") && recarr.size()  > (Integer) columnConfig.get("businessdate")) {
                                Cell cell = row.getCell((Integer) columnConfig.get("businessdate"));
                               String value = importHandler.getCellValue(cell);
                                businessdate = value;
                                if (StringUtil.isNullOrEmpty(value)) {

                                    invalidColumns += " businessdate";
                                    throw new AccountingException(invalidColumns + " In valid " +invalidColumns + " format.");
                                } else {
                                    try {
                                        invalidColumns += " businessdate";
                                        bussinessDate = dateFrmt.parse(businessdate);
                                         isValiddate=isvalidDate(bussinessDate,companyAccountPreferences);
                                    } catch (ParseException ex) {
                                        throw new AccountingException("Incorrect date format for Business Date, Please specify values in " + dateformateproduct + " format.");
                                }
                                }
                            } else {
                                if (!StringUtil.isNullOrEmpty(invalidColumns)) {
                                    invalidColumns += ",";
                                }
                                invalidColumns += "businessdate";
                                throw new AccountingException(invalidColumns + " is not available.");
                            }if(!isValiddate){
//                                throw new AccountingException(invalidColumns + " In valid " +invalidColumns + " format.");
                                throw new AccountingException("Bussiness date should not be before than Book Beginning Date.");
                            }
                            
                            String expdateStr = "";
                            if ("Stock IN".equalsIgnoreCase(type) && (isBatchForProduct || isSerialForProduct)) {
                                if (columnConfig.containsKey("expdate")) {
                                    Cell cell = row.getCell((Integer) columnConfig.get("expdate"));
                                    expdateStr = importHandler.getCellValue(cell);
                                    if (!StringUtil.isNullOrEmpty(expdateStr)) {
                                        try {
                                            dateFrmt.parse(expdateStr);
                                        } catch (Exception ex) {
                                            throw new AccountingException("Incorrect date format for Expiry Date, Please specify values in " + dateformateproduct + " format.");
                                        }
                                    }
                                }
                            }

                            HashMap<String, StoreMaster> storeMasterSet = new HashMap<String, StoreMaster>();
                            StoreMaster Row = null;
                            String productRowName = null;
                            String productRowId = null;
                            if (columnConfig.containsKey("row") && isRowForProduct) {
                                Cell cell = row.getCell((Integer) columnConfig.get("row"));
                                if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
                                    invalidColumn.put((Integer) columnConfig.get("location"), "Invalid");
                                    failureMsg += "location ID is not available.";
                                } else {
                                    productRowName = importHandler.getCellValue(cell);
                                    if (!StringUtil.isNullOrEmpty(productRowName)) {
                                        Row = storeService.getStoreMasterByName(productRowName, companyid, 1);
                                        if (Row == null) {
                                            throw new AccountingException("Row is not available.");
                                        } else {
                                            productRowId = Row.getId();
                                            storeMasterSet.put("row", Row);
                                        }
                                    } else {
                                        throw new AccountingException("Row is not available.");
                                    }
                                }
                            }
                            
                            StoreMaster rack = null;
                            String productRackName = null;
                            String productRackId = null;
                            if (columnConfig.containsKey("rack") && isRackForProduct) {
                                Cell cell = row.getCell((Integer) columnConfig.get("rack"));
                                if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
                                    invalidColumn.put((Integer) columnConfig.get("location"), "Invalid");
                                    failureMsg += "rack ID is not available.";
                                } else {
                                    productRackName = importHandler.getCellValue(cell);
                                    if (!StringUtil.isNullOrEmpty(productRackName)) {
                                        rack = storeService.getStoreMasterByName(productRackName, companyid, 1);
                                        if (Row == null) {
                                            throw new AccountingException("rack is not available.");
                                        } else {
                                            productRackId = Row.getId();
                                            storeMasterSet.put("rack", rack);
                                        }
                                    } else {
                                        throw new AccountingException("rack is not available.");
                                    }
                                }
                            }
                            
                            StoreMaster bin = null;
                            String productBinName = null;
                            String productBinId = null;
                            if (columnConfig.containsKey("bin") && isBinForProduct) {
                                Cell cell = row.getCell((Integer) columnConfig.get("bin"));
                                if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
                                    invalidColumn.put((Integer) columnConfig.get("location"), "Invalid");
                                    failureMsg += "bin ID is not available.";
                                } else {
                                    productBinName = importHandler.getCellValue(cell);
                                    if (!StringUtil.isNullOrEmpty(productBinName)) {
                                        bin = storeService.getStoreMasterByName(productBinName, companyid, 1);
                                        if (bin == null) {
                                            throw new AccountingException("bin is not available.");
                                        } else {
                                            productBinId = bin.getId();
                                            storeMasterSet.put("bin", bin);
                                        }
                                    } else {
                                        throw new AccountingException("bin is not available.");
                                    }
                                }
                            }
                            
                            String productDefaultWarehouseID = "";
                            String productDefaultWarehouseName = "";
                            if (isWarehouseForProduct && columnConfig.containsKey("warehouse")) {
                                Cell cell = row.getCell((Integer) columnConfig.get("warehouse"));
                                if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
                                    invalidColumn.put((Integer) columnConfig.get("location"), "Invalid");
                                    failureMsg += "warehouse ID is not available.";
                                } else {
                                    productDefaultWarehouseName = importHandler.getCellValue(cell);
                                    if (!StringUtil.isNullOrEmpty(productDefaultWarehouseName)) {
                                        InventoryWarehouse invWHouse = accProductModuleService.getInventoryWarehouseByName(productDefaultWarehouseName, companyid);
                                        if (invWHouse != null) {
                                            productDefaultWarehouseID = invWHouse.getId();
                                        }
                                    }
                                }
                            }
                            if (StringUtil.isNullOrEmpty(productDefaultWarehouseID)) {
                                throw new AccountingException("warehouse is not available.");
                            }

                            String productDefaultLocationID = "";
                            String productDefaultLocationName = "";
                            if (isLocationForProduct && columnConfig.containsKey("location")) {
                                Cell cell = row.getCell((Integer) columnConfig.get("location"));
                                if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
                                    invalidColumn.put((Integer) columnConfig.get("location"), "Invalid");
                                    failureMsg += "warehouse ID is not available.";
                                } else {
                                    productDefaultLocationName = importHandler.getCellValue(cell);
                                    if (!StringUtil.isNullOrEmpty(productDefaultLocationName)) {
                                        InventoryLocation invLoc = accProductModuleService.getInventoryLocationByName(productDefaultLocationName, companyid);
                                        if (invLoc != null) {
                                            productDefaultLocationID = invLoc.getId();
                                        }
                                    }
                                }
                            }
                            if (StringUtil.isNullOrEmpty(productDefaultLocationID)) {
                                throw new AccountingException("location is not available.");
                            }

                            if (!StringUtil.isNullOrEmpty(reason)) {
                                result = accJournalEntryobj.getMasterItemByNameorID(companyid, reason, "31");
                                List list = result.getEntityList();
                                if (list != null && list.size() > 0) {
                                    Iterator itr = list.iterator();
                                    while (itr.hasNext()) {
//                                        Object[] values = (Object[]) itr.next();
                                        MasterItem fieldComboData = (MasterItem) itr.next();
                                        reason = fieldComboData.getID();
                                    }
                                } 
//                                else if (masterPreference.equalsIgnoreCase("2")) {
//                                     // create new reason    
//                                    MasterItem msItem=new MasterItem();
//                                    String Id=java.util.UUID.randomUUID().toString();
//                                    msItem.setID(Id);
//                                    msItem.setActivated(true);
//                                    msItem.setValue(reason);
//                                    msItem.setCompany(company1);
//                                    KwlReturnObject kwlObj = accountingHandlerDAO.getObject(MasterGroup.class.getName(), "31");
//                                    MasterGroup masterGroup = (MasterGroup) kwlObj.getEntityList().get(0);
//                                    msItem.setMasterGroup(masterGroup);
////                                    stockDAO.saveOrUpdate(msItem);
////                                    reason=Id;
//                                } else {
//                                    throw new AccountingException("reason is not available.");
//                                }
                            } else {
                                throw new AccountingException("reason is not available.");
                            }

                            String quantity = "";
                            if (columnConfig.containsKey("quantity")) {
                                Cell cell = row.getCell((Integer) columnConfig.get("quantity"));
                                quantity = importHandler.getCellValue(cell);
                                if (StringUtil.isNullOrEmpty(quantity)) {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        quantity = "0";
                                    } else {
                                        if (product.getProducttype().equals(Producttype.SERVICE)) {
                                            quantity = "";
                                        } else {
                                            throw new AccountingException("Product  Quantity is not available");
                                        }
                                    }
                                }
                            }
                            
                            String unitPrice = "";
                            if (columnConfig.containsKey("unitprice")) {
                                Cell cell = row.getCell((Integer) columnConfig.get("unitprice"));
                                unitPrice = importHandler.getCellValue(cell);
                                if (StringUtil.isNullOrEmpty(unitPrice)) {
                                    if (masterPreference.equalsIgnoreCase("1")) {
                                        unitPrice = "0";
                                    } else {
                                        if (product.getProducttype().equals(Producttype.SERVICE)) {
                                            unitPrice = "";
                                        } else {
                                            throw new AccountingException("unitPrice is not available");
                                        }
                                    }
                                }
                            } else {
                                throw new AccountingException("unitPrice is not available");
                            }
                            
                             String finalSerials = "";
                            NewProductBatch productBatch = null;
                            if (!("Stock IN".equals(type))) {
                                productBatch = stockDAO.getERPProductBatch(product.getID(), productDefaultWarehouseID, productDefaultLocationID, Row, rack, bin, batchName, companyid);
                                if (productBatch == null || (productBatch.getQuantitydue() < Double.parseDouble(quantity))) {
                                    throw new AccountingException("Quantity is not available");
                                } else {
                                    if (isSerialForProduct && !StringUtil.isNullOrEmpty(serialName)) {
                                        serialName = serialName.trim();
                                        String[] serArr = serialName.split(",");
                                        for (int j = 0; j < serArr.length; j++) {
                                            NewBatchSerial batchSer = stockDAO.getERPBatchSerial(product.getID(), productBatch, serArr[j], companyid);
                                            if (batchSer != null && batchSer.getQuantitydue() >= 0) {
                                                if (!StringUtil.isNullOrEmpty(finalSerials)) {
                                                    finalSerials += "," + serArr[j];
                                                } else {
                                                    finalSerials = serArr[j];
                                                }
                                            }
                                        }
                                        if (StringUtil.isNullOrEmpty(finalSerials)) {
                                            throw new AccountingException("serial is is not available");
                                        }
                                    }
                                }
                            } else if ("Stock IN".equalsIgnoreCase(type)) {
                                if (isSerialForProduct && !StringUtil.isNullOrEmpty(serialName)) {
                                    serialName = serialName.trim();
                                    String[] serArr = serialName.split(",");
                                    for (int k = 0; k < serArr.length; k++) {
                                        boolean isexist = stockDAO.isSerialExists(product, batchName, serArr[k]);
                                        if (!isexist && !StringUtil.isNullOrEmpty(finalSerials)) {
                                             finalSerials += "," + serArr[k];
                                        } else if (!isexist) {
                                           finalSerials = serArr[k];
                                        }
                                    }
                                    if (StringUtil.isNullOrEmpty(finalSerials)) {
                                        throw new AccountingException("serial already exist is not available");
                                    }
                                }
                            }
                            
                                      Inventory invetory = null;
                            if (quantity.length() > 0) {
                                JSONObject inventoryjson = new JSONObject();
                                double invIntialqty = Double.parseDouble(quantity);
                                double cost = Double.parseDouble(unitPrice);
                                if(isSerialForProduct){
                                    String[] srArr=finalSerials.split(",");
                                    invIntialqty=srArr.length;
                                }
                                if (!("Stock IN".equalsIgnoreCase(type)) && !StringUtil.isNullOrEmpty(quantity)) {
                                    invIntialqty = Double.parseDouble(quantity);
                                    if (isSerialForProduct) {
                                        String[] srArr = finalSerials.split(",");
                                        invIntialqty = srArr.length;
                                    }
                                    invIntialqty = -1 * invIntialqty;
                                    cost=0;
                                }
                                double prodInitPurchasePrice = 0;
                                KwlReturnObject initPurchasePriceObj = accProductObj.getInitialPrice(product.getID(), true);

                                if (initPurchasePriceObj != null && initPurchasePriceObj.isSuccessFlag() && initPurchasePriceObj.getEntityList() != null && initPurchasePriceObj.getEntityList().get(0) != null) {
                                    prodInitPurchasePrice = (double) initPurchasePriceObj.getEntityList().get(0);
                                }

                                if ((product.isIswarehouseforproduct() && StringUtil.isNullOrEmpty(productDefaultWarehouseID) && product.isIslocationforproduct() && StringUtil.isNullOrEmpty(productDefaultLocationID))) {
                                    throw new AccountingException("Warehouse & Location is enabled for this product but their values are not provided.");
                                } else if ((product.isIswarehouseforproduct() && StringUtil.isNullOrEmpty(productDefaultWarehouseID))) {
                                    throw new AccountingException("Warehouse is enabled for this product but its value is not provided.");
                                } else if ((product.isIslocationforproduct() && StringUtil.isNullOrEmpty(productDefaultLocationID))) {
                                    throw new AccountingException("Location is enabled for this product but its value is not provided.");
                                } else if ((isRowForProduct && StringUtil.isNullOrEmpty(productRowId))) {
                                    throw new AccountingException("Row is enabled for this product but its value is not provided.");
                                } else if ((isRackForProduct && StringUtil.isNullOrEmpty(productRackId))) {
                                    throw new AccountingException("Rack is enabled for this product but its value is not provided.");
                                } else if ((isBinForProduct && StringUtil.isNullOrEmpty(productBinId))) {
                                    throw new AccountingException("Bin is enabled for this product but its value is not provided.");
                                } else {

                                    JSONObject jdata = new JSONObject();

                                    jdata.put("companyid", companyid);
                                    jdata.put("quantity", String.valueOf(invIntialqty));
                                    jdata.put("purchaseprice", ("Stock IN".equals(type))?String.valueOf(cost):prodInitPurchasePrice);
                                    jdata.put("product", product.getID());
                                    jdata.put("warehouse", productDefaultWarehouseID);
                                    jdata.put("uomid", "");
                                    jdata.put("reason", reason);
                                    jdata.put("memo", memo);
                                    jdata.put("businessdate", businessdate);
                                    jdata.put("adjustmentType", type);//This is GRN Type Tranction  

                                    JSONObject detailObj = new JSONObject();
                                    detailObj.put("quantity", invIntialqty);
                                    detailObj.put("locationId", productDefaultLocationID);
                                    detailObj.put("rowId", productRowId);
                                    detailObj.put("rackId", productRackId);
                                    detailObj.put("binId", productBinId);
                                    detailObj.put("batchName", batchName);
                                    detailObj.put("serialNames", finalSerials);
                                    detailObj.put("skuFields", "");
                                    detailObj.put("approvalSerials", finalSerials);
                                    detailObj.put("mfgdate", "");
                                    if ("Stock IN".equals(type) && (isBatchForProduct || isSerialForProduct)) {
                                        detailObj.put("expdate", expdateStr);
                                    }

                                    JSONArray dtlArr = new JSONArray();
                                    dtlArr.put(detailObj);

                                    jdata.put("stockDetails", dtlArr);

                                    jarr.put(jdata);

                                }
                            }
                            
                        }
                        
                    }catch (Exception ex) {
                        failed++;
                        String errorMsg = ex.getMessage(), invalidColumns = "";
                        try {
                            JSONObject errorLog = new JSONObject(errorMsg);
                            errorMsg = errorLog.getString("errorMsg");
                            invalidColumns = errorLog.getString("invalidColumns");
                        } catch (JSONException jex) {
                        }
                        failedRecords.append("\n" + createCSVrecord(recarr.toArray()) + "\"" + errorMsg.replaceAll("\"", "") + "\"");
                    }
                    total++;
                }else {
                    failedRecords.append(createCSVrecord(recarr.toArray()) + "\"Error Message\"");
                }
                cnt++;
            }
             if (failed > 0) {
                createFailureFiles(fileName, failedRecords, ".xlsx");
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
            
        }  catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Importing Records.");
        } finally {
            
            HashMap<String, Object> logDataMap = new HashMap<String, Object>();
            try {
                //Insert Integration log

                logDataMap.put("FileName", getActualFileName(fileName));
                logDataMap.put("StorageName", fileName);
                logDataMap.put("Log", msg);
                logDataMap.put("Type", "xls");
                logDataMap.put("FailureFileType", failed > 0 ? "xls" : "");
                logDataMap.put("TotalRecs", total);
                logDataMap.put("Rejected", failed);

                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userId);
                logDataMap.put("Company", companyid);
                logDataMap.put("Id", logId);

                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", getActualFileName(fileName));
                returnObj.put("data", jarr);
                returnObj.put("logData", logDataMap);
                
                if (issuccess && jarr.length() > 0) {
                    
                    Map<String,Object> parmList=new HashMap<String,Object>();
                    parmList.put("logDataMap", logDataMap);
                    parmList.put("returnObj", returnObj);
                    parmList.put("companyid", companyid);
                    parmList.put("dateformat", dateFrmt);
                    parmList.put("userId", userId);
                    parmList.put("currencyId", currencyId);
                    parmList.put("jeDataMap", jeDataMap);
                    parmList.put("jeDataMap1", jeDataMap1);
                    parmList.put("locale", locale);
                    parmList.put("masterPreference", masterPreference);
                    parmList.put("isQAActivated", isQAActivated);
                    returnObj = requestStockAdjustment(parmList);
                }else if(issuccess){
                     importDao.saveImportLog(logDataMap);
                    String tableName = importDao.getTableName(logDataMap.get("StorageName").toString());
                    importDao.removeFileTable(tableName); // Remove table after importing all records
                }

            } catch (AccountingException ex) {
                throw new AccountingException(ex.getMessage());
            } catch (Exception ex) {
//                txnManager.rollback(lstatus);
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return returnObj;
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

    public ModelAndView importStockAdjustment(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            String eParams = request.getParameter("extraParams");
            JSONObject extraParams = StringUtil.isNullOrEmpty(eParams) ? new JSONObject() : new JSONObject(eParams);
            extraParams.put("Company", sessionHandlerImpl.getCompanyid(request));

            KwlReturnObject custumObjresult = accountingHandlerDAO.getObject(CompanyAccountPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) custumObjresult.getEntityList().get(0);
            Date bookBeginningDate = preferences.getBookBeginningFrom();
            String doAction = request.getParameter("do");
            HashMap<String, Object> requestParams = importHandler.getImportRequestParams(request);
            requestParams.put("extraParams", extraParams);
            requestParams.put("extraObj", null);
            requestParams.put("servletContext", this.getServletContext());
            requestParams.put("bookbeginning", bookBeginningDate);
            requestParams.put("locale", RequestContextUtils.getLocale(request));    //For Localize purpose

            if (doAction.compareToIgnoreCase("import") == 0) {
                System.out.println("A(( Import start : " + new Date());
                JSONObject datajobj = new JSONObject();
                JSONObject resjson = new JSONObject(request.getParameter("resjson").toString());
                JSONArray resjsonJArray = resjson.getJSONArray("root");

                String filename = request.getParameter("filename");
                datajobj.put("filename", filename);
                boolean typeXLSFile = (request.getParameter("typeXLSFile") != null) ? Boolean.parseBoolean(request.getParameter("typeXLSFile")) : false;
                String destinationDirectory = "";
                if (typeXLSFile) {
                    destinationDirectory = storageHandlerImpl.GetDocStorePath() + "xlsfiles";
                } else {
                    destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
                }

                File filepath = new File(destinationDirectory + "/" + filename);
                datajobj.put("FilePath", filepath);
                String currencyid = sessionHandlerImpl.getCurrencyID(request);
                String dateFormatId = request.getParameter("dateFormat");
                String isQAActivated = request.getParameter("isQAActivated");
                datajobj.put("resjson", resjsonJArray);
                requestParams.put("importflag", Constants.importproductopeningqty);
                requestParams.put("currencyId", currencyid);
                datajobj.put("dateformateproduct", dateFormatId);
                datajobj.put("isQAActivated", isQAActivated);
                requestParams.put("updateExistingRecordFlag", request.getParameter("updateExistingRecordFlag"));
                requestParams.put("jobj", datajobj);
                String exceededLimit = request.getParameter("exceededLimit");

//                    jobj = importProductopeningqtyRecords(request, datajobj);

                Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
                Map<String, Object> jeDataMap1 = AccountingManager.getGlobalParams(request);
                Locale locale = RequestContextUtils.getLocale(request);
                if (typeXLSFile) {
                    jobj = importStockAdjustmentRecordsXls(requestParams, jeDataMap, jeDataMap1, locale);
                } else {
                    jobj = importStockAdjustmentRecords(requestParams, jeDataMap, jeDataMap1, locale);
                }

                System.out.println("A(( Import end : " + new Date());
            } else if (doAction.compareToIgnoreCase("validateData") == 0) {
                System.out.println("A(( Validation start : " + new Date());
                jobj = importHandler.validateFileData(requestParams);
                System.out.println("A(( Validation end : " + new Date());
            }
            issuccess = true;
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();

            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException e) {
                Logger.getLogger(StockAdjustmentController.class.getName()).log(Level.SEVERE, null, ex);
            }

            Logger.getLogger(StockAdjustmentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject requestStockAdjustment(Map<String, Object> parmList) throws JSONException, AccountingException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SA_Tx_Save");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONObject jobjData=new JSONObject();
        try {
           
            
            HashMap<String, Object> logDataMap=(HashMap<String, Object>) parmList.get("logDataMap");
            jobjData= (JSONObject) parmList.get("returnObj");
            String companyid=  parmList.get("companyid").toString();
            String userid=  parmList.get("userId").toString();
            String currencyId=  parmList.get("currencyId").toString();
            Map<String, Object> jeDataMap=(HashMap<String, Object>) parmList.get("jeDataMap");
            Map<String, Object> jeDataMap1=(HashMap<String, Object>) parmList.get("jeDataMap1");
            Locale locale=  (Locale) parmList.get("locale");
            String masterPreference=  (String) parmList.get("masterPreference");
            String qApproval="false";
           Map<String,String> resnSet=new HashMap<>();
            String companyId = companyid;
            String userId = userid;
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);

            DateFormat df1 = null;
            if(parmList.get("dateformat")!=null && !StringUtil.isNullOrEmpty(parmList.get("dateformat").toString())){
                 df1=(SimpleDateFormat)parmList.get("dateformat");
            }

            jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            String seqNo = "";
            if (!StringUtil.isNullOrEmpty(parmList.get("isQAActivated").toString())) {
                qApproval = parmList.get("isQAActivated").toString();
            }
            {
                ExtraCompanyPreferences extraCompanyPreferences = null;
                KwlReturnObject extraprefresult = accountingHandlerDAO.getObject(ExtraCompanyPreferences.class.getName(), companyId);
                extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
                KwlReturnObject capresult = accountingHandlerDAO.getObject(CompanyAccountPreferences.class.getName(), companyId);
                CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
                boolean isSkuforCompany=extraCompanyPreferences.isSKUFieldParm();
                Date bussinessDate = new Date();
                boolean sendForQAApproval = false;

                String records = jobjData.optString("data");
                String adjustmentReason = "";

                String seqFormatId = "";

                JSONArray jArr = new JSONArray(records);
                SeqFormat seqFormat = null;
                SeqFormat  assetSeqFormat = null;
                try {
                    assetSeqFormat = isSkuforCompany ? seqService.getDefaultSeqFormat(company, ModuleConst.Asset_Module) : null;
                } catch (SeqFormatException ex) {
                    issuccess = false;
                    msg = "" + ex.getMessage();
                    Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    throw new AccountingException(messageSource.getMessage("acc.rem.263", null, (Locale) parmList.get("locale")));
                }
                try {
                    seqFormat = seqService.getDefaultSeqFormat(company, ModuleConst.STOCK_ADJUSTMENT);
                } catch (SeqFormatException ex) {
                    issuccess = false;
                    msg = "" + ex.getMessage();
                    Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    throw new AccountingException(messageSource.getMessage("acc.rem.262", null, (Locale) parmList.get("locale")));
                }
                 
                boolean allowNegativeInventory = false;

                synchronized (this) {
                    boolean seqExist = false;
                    seqNo = seqService.getNextFormatedSeqNumber(seqFormat);
                    if (!seqExist) {
                        for (int i = 0; i < jArr.length(); i++) {
                            JSONObject jObj = jArr.optJSONObject(i);
                            String productId = jObj.optString("product");
                            adjustmentReason = jObj.optString("reason");
                            String  businessdate = jObj.optString("businessdate");
                            bussinessDate = df1.parse(businessdate);
                            String memo = jObj.optString("memo");
                            String warehouse = jObj.optString("warehouse");
                            String uomId = jObj.optString("uomid");
                            String remark = jObj.optString("remark", "");
                            String adjustmentType = jObj.optString("adjustmentType");
                            String costCenterId = jObj.optString("costcenter");
                            double quantity = jObj.optDouble("quantity", 0);
                            double amount = jObj.optDouble("purchaseprice", 0);
                            String reason = jObj.optString("reason");
                            String linelevelcustomdata = jObj.optJSONArray(Constants.LineLevelCustomData) != null ? jObj.optJSONArray(Constants.LineLevelCustomData).toString() : "";

                            jeresult = accountingHandlerDAO.getObject(Product.class.getName(), productId);
                            Product product = (Product) jeresult.getEntityList().get(0);

                            jeresult = accountingHandlerDAO.getObject(UnitOfMeasure.class.getName(), uomId);
                            UnitOfMeasure uom = (UnitOfMeasure) jeresult.getEntityList().get(0);

                            jeresult = accountingHandlerDAO.getObject(CostCenter.class.getName(), costCenterId);
                            CostCenter costCenter = (CostCenter) jeresult.getEntityList().get(0);
                            
                            KwlReturnObject kwlObj = accountingHandlerDAO.getObject(MasterItem.class.getName(), adjustmentReason);
                            MasterItem masterItem = (MasterItem) kwlObj.getEntityList().get(0);
                            if (resnSet.containsKey(adjustmentReason)) {
                                reason = resnSet.get(adjustmentReason);
                            } else if (masterItem == null && "2".equals(masterPreference)) {
                                MasterItem msItem = new MasterItem();
                                String Id = java.util.UUID.randomUUID().toString();
                                msItem.setID(Id);
                                msItem.setActivated(true);
                                msItem.setValue(reason);
                                msItem.setCompany(company);
                                kwlObj = accountingHandlerDAO.getObject(MasterGroup.class.getName(), "31");
                                MasterGroup masterGroup = (MasterGroup) kwlObj.getEntityList().get(0);
                                msItem.setMasterGroup(masterGroup);
//                                stockDAO.saveOrUpdate(msItem);
//                                reason = Id;
                                resnSet.put(adjustmentReason, Id);
                            }
                            Store fromStore = storeService.getStoreById(warehouse);

                            StockAdjustment stockAdjustment = new StockAdjustment(product, fromStore, uom, quantity, amount, bussinessDate);
                            stockAdjustment.setTransactionNo(seqNo);
                            stockAdjustment.setRemark(remark);
                            stockAdjustment.setCostCenter(costCenter);
                            stockAdjustment.setAdjustmentType(!StringUtil.isNullOrEmpty(adjustmentType) ? adjustmentType : null);
                            stockAdjustment.setReason(reason);
                            stockAdjustment.setIsJobWorkIn(false);
                            stockAdjustment.setMemo(memo);
                            stockAdjustment.setFinalQuantity(0);
//                            stockAdjustment.setMemo("");
                            stockAdjustment.setStockAdjustmentReason(adjustmentReason);

//                             Create Journal Entry Number for wastage case
                            if (masterItem != null && masterItem.getDefaultMasterItem() != null && masterItem.getDefaultMasterItem().getID().equalsIgnoreCase(Constants.WASTAGE_ID)) {
                                String oldjeid = "";
                                String jeentryNumber = "";
                                String jeIntegerPart = "";
                                String jeDatePrefix = "";
                                String jeDateAfterPrefix = "";
                                String jeDateSuffix = "";
                                String jeSeqFormatId = "";
                                boolean jeautogenflag = false;
                                if (StringUtil.isNullOrEmpty(oldjeid)) {
                                    synchronized (this) {
                                        Map<String, Object> JEFormatParams = new HashMap<>();
                                        JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                                        JEFormatParams.put("modulename", CompanyPreferencesConstants.AUTOJOURNALENTRY);
                                        JEFormatParams.put("companyid", companyId);
                                        JEFormatParams.put("isdefaultFormat", true);

                                        kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                                        SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyId, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, bussinessDate);
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
                                jeDataMap.put("entrydate", bussinessDate);
                                jeDataMap.put("companyid", companyId);
                                jeDataMap.put("memo", "Stock Adjustment JE for " + product.getName());
                                jeDataMap.put("createdby", userId);
                                jeDataMap.put("currencyid", currencyId);

                                jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap); // Create Journal entry without JEdetails
                                JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);

                                stockAdjustment.setJournalEntry(journalEntry);
                                String jeid = journalEntry.getID();
                                jeDataMap.put("jeid", jeid);

                                HashSet jeDetails = new HashSet();
                                JSONObject jedjson = new JSONObject();
                                jedjson.put("srno", jeDetails.size() + 1);
                                jedjson.put("companyid", companyId);
                                jedjson.put("amount", authHandler.round(quantity * amount, Constants.AMOUNT_DIGIT_AFTER_DECIMAL));
                                jedjson.put("accountid", product.getWastageAccount().getID());
                                jedjson.put("debit", true);
                                jedjson.put("jeid", jeid);
                                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                jeDetails.add(jed);

                                if (product.getProducttype().getID().equalsIgnoreCase(Producttype.INVENTORY_PART)) {
                                    jedjson = new JSONObject();
                                    jedjson.put("srno", jeDetails.size() + 1);
                                    jedjson.put("companyid", companyId);
                                    jedjson.put("amount", authHandler.round(quantity * amount, Constants.AMOUNT_DIGIT_AFTER_DECIMAL));
                                    jedjson.put("accountid", product.getPurchaseAccount().getID());
                                    jedjson.put("debit", false);
                                    jedjson.put("jeid", jeid);
                                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                    jeDetails.add(jed);
                                } else if (product.getProducttype().getID().equalsIgnoreCase(Producttype.ASSEMBLY)) {
                                    jedjson = new JSONObject();
                                    jedjson.put("srno", jeDetails.size() + 1);
                                    jedjson.put("companyid", companyId);
                                    jedjson.put("amount", authHandler.round(quantity * amount, Constants.AMOUNT_DIGIT_AFTER_DECIMAL));
                                    jedjson.put("accountid", product.getSalesAccount().getID());
                                    jedjson.put("debit", false);
                                    jedjson.put("jeid", jeid);
                                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                    jeDetails.add(jed);
                                }
                            }
//                            

                            double totalFinalQuantity = 0;
                            JournalEntry inventoryJE = null;
                            if (product.getInventoryAccount() != null && !StringUtil.isNullOrEmpty(adjustmentType) && (adjustmentType.equalsIgnoreCase("Stock Sales") || adjustmentType.equalsIgnoreCase("Stock Out") || adjustmentType.equalsIgnoreCase("Stock In")) && (extraCompanyPreferences.isActivateMRPModule() || preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD)) {
                                // Create Journal Entry Number for MRP module
                                String oldjeid = "";
                                String jeentryNumber = "";
                                String jeIntegerPart = "";
                                String jeDatePrefix = "";
                                String jeDateAfterPrefix = "";
                                String jeDateSuffix = "";
                                String jeSeqFormatId = "";
                                boolean jeautogenflag = false;
                                if (StringUtil.isNullOrEmpty(oldjeid)) {
                                    synchronized (this) {
                                        Map<String, Object> JEFormatParams = new HashMap<>();
                                        JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                                        JEFormatParams.put("modulename", CompanyPreferencesConstants.AUTOJOURNALENTRY);
                                        JEFormatParams.put("companyid", companyId);
                                        JEFormatParams.put("isdefaultFormat", true);
                                        kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                                        SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyId, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, bussinessDate);
                                        jeentryNumber = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                                        jeIntegerPart = (String) seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                                        jeDatePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                                        jeDateAfterPrefix = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                                        jeDateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                                        jeSeqFormatId = format.getID();
                                        jeautogenflag = true;
                                    }
                                }

                                jeDataMap1.put("entrynumber", jeentryNumber);
                                jeDataMap1.put("autogenerated", jeautogenflag);
                                jeDataMap1.put(Constants.SEQFORMAT, jeSeqFormatId);
                                jeDataMap1.put(Constants.SEQNUMBER, jeIntegerPart);
                                jeDataMap1.put(Constants.DATEPREFIX, jeDatePrefix);
                                jeDataMap1.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
                                jeDataMap1.put(Constants.DATESUFFIX, jeDateSuffix);
                                jeDataMap1.put("entrydate", bussinessDate);
                                jeDataMap1.put("companyid", companyId);
                                jeDataMap1.put("memo", "Stock Adjustment JE for " + product.getName());
                                jeDataMap1.put("createdby", userid);
                                jeDataMap1.put("currencyid", currencyId);
                                jeDataMap1.put("transactionModuleid", Constants.Inventory_Stock_Adjustment_ModuleId);
                                jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap1); // Create Journal entry without JEdetails
                                inventoryJE = (JournalEntry) jeresult.getEntityList().get(0);
                                stockAdjustment.setInventoryJE(inventoryJE);
                                HashSet jeDetails = new HashSet();
                                JSONObject jedjson = new JSONObject();
                                jedjson.put("srno", jeDetails.size() + 1);
                                jedjson.put("companyid", companyId);
                                if (adjustmentType.equalsIgnoreCase("Stock Out")) { // Downward Direction-Stock OUT
                                    jedjson.put("amount", authHandler.round(((quantity * amount) * (-1)), Constants.AMOUNT_DIGIT_AFTER_DECIMAL));
                                } else {
                                    jedjson.put("amount", authHandler.round(quantity * amount, Constants.AMOUNT_DIGIT_AFTER_DECIMAL));
                                }
                                if (adjustmentType.equalsIgnoreCase("Stock Out")) {// Upward Direction-Stock IN
                                    jedjson.put("debit", false);
                                } else {
                                    jedjson.put("debit", true);
                                }
                                jedjson.put("accountid", product.getInventoryAccount().getID());
                                jedjson.put("jeid", inventoryJE.getID());
                                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                jeDetails.add(jed);
                                jedjson = new JSONObject();
                                jedjson.put("srno", jeDetails.size() + 1);
                                jedjson.put("companyid", companyId);
                                if (adjustmentType.equalsIgnoreCase("Stock Out")) { // Downward Direction-Stock OUT
                                    jedjson.put("amount", authHandler.round(((quantity * amount) * (-1)), Constants.AMOUNT_DIGIT_AFTER_DECIMAL));
                                } else {
                                    jedjson.put("amount", authHandler.round(quantity * amount, Constants.AMOUNT_DIGIT_AFTER_DECIMAL));
                                }
                                jedjson.put("accountid", product.getStockAdjustmentAccount().getID());
                                if (adjustmentType.equalsIgnoreCase("Stock Out")) { // Downward Direction-Stock OUT
                                    jedjson.put("debit", true);
                                } else {
                                    jedjson.put("debit", false);
                                }
                                jedjson.put("jeid", inventoryJE.getID());
                                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                jeDetails.add(jed);
                                inventoryJE.setDetails(jeDetails);
                                accJournalEntryobj.saveJournalEntryDetailsSet(jeDetails);
                            }
                            Set<StockAdjustmentDetail> adjustmentDetailSet = new HashSet<StockAdjustmentDetail>();
                            JSONArray stockDetails = jObj.optJSONArray("stockDetails");
                            for (int x = 0; x < stockDetails.length(); x++) {
                                JSONObject detailObj = stockDetails.optJSONObject(x);
                                String locationId = detailObj.optString("locationId");
                                String rowId = detailObj.optString("rowId");
                                String rackId = detailObj.optString("rackId");
                                String binId = detailObj.optString("binId");
                                String batchName = detailObj.optString("batchName");
                                String serialNames = detailObj.optString("serialNames");
                                String skuFields = detailObj.optString("skuFields");
                                String approvalSerials = detailObj.optString("approvalSerials");
                                double qty = detailObj.optDouble("quantity");
                                if(qty<0){
                                    qty=(-1*qty);
                                }
                                String mfgdate = detailObj.optString("mfgdate");
                                String expdate = detailObj.optString("expdate");
                                String warrantyexpfromdate = detailObj.optString("warrantyexpfromdate");
                                String warrantyexptodate = detailObj.optString("warrantyexptodate");

                                Location location = locationService.getLocation(locationId);
                                StoreMaster row = null;
                                if (product.isIsrowforproduct()) {
                                    row = storeService.getStoreMaster(rowId);
                                }
                                StoreMaster rack = null;
                                if (product.isIsrackforproduct()) {
                                    rack = storeService.getStoreMaster(rackId);
                                }
                                StoreMaster bin = null;
                                if (product.isIsbinforproduct()) {
                                    bin = storeService.getStoreMaster(binId);
                                }
                                if (StringUtil.isNullOrEmpty(warrantyexpfromdate) || warrantyexpfromdate.split(",").length == 0) {
                                    warrantyexpfromdate = expdate;
                                }
                                if (StringUtil.isNullOrEmpty(warrantyexptodate) || warrantyexptodate.split(",").length == 0) {
                                    warrantyexptodate = expdate;
                                }

                                if (quantity > 0 && (product.isIsSerialForProduct() || product.isIsBatchForProduct()) && (!StringUtil.isNullOrEmpty(expdate) && expdate.split(",").length > 0) && (!StringUtil.isNullOrEmpty(warrantyexpfromdate)) && !StringUtil.isNullOrEmpty(warrantyexptodate) 
                                        || (!StringUtil.isNullOrEmpty(serialNames) && adjustmentType.equalsIgnoreCase("Stock In")) && product.isIsSKUForProduct()) {
                                    Map<String, Object> tempTablMap = new HashMap<String, Object>();
                                    tempTablMap.put("serials", serialNames);
                                    tempTablMap.put("mfgdate", mfgdate);
                                    tempTablMap.put("expdate", expdate);
                                    tempTablMap.put("warrantyexpfromdate", warrantyexpfromdate);
                                    tempTablMap.put("warrantyexptodate", warrantyexptodate);
                                    tempTablMap.put("skufields", skuFields);
                                    
                                     if (product.isIsSKUForProduct() && (StringUtil.isNullOrEmpty(skuFields))&&adjustmentType.equalsIgnoreCase("Stock In")) {
                                        String[] srArr = serialNames.split(",");
                                        String skuStr = "";
                                         if (assetSeqFormat != null) {
                                        for (int sr = 0; sr < srArr.length; sr++) {
                                            String asetSeqNo = seqService.getNextFormatedSeqNumber(assetSeqFormat);
                                            if (StringUtil.isNullOrEmpty(skuStr)) {
                                                skuStr = asetSeqNo;
                                            } else {
                                                skuStr = skuStr + "," + asetSeqNo;
                                            }
                                            seqService.updateSeqNumber(assetSeqFormat);
                                        }
                                        }else{
                                             throw new InventoryException(InventoryException.Type.NULL, "Sequence format for asset(sku) field is not set.");
                                        }
                                        tempTablMap.put("skufields", skuStr);
                                    }

                                    stockAdjustmentService.saveSADetailInTemporaryTable(product, fromStore, location, batchName, tempTablMap);
//                                stockAdjustmentService.saveSADetailInTemporaryTable(product, fromStore, location, batchName, serialNames, mfgdate, expdate, warrantyexpfromdate, warrantyexptodate, skuFields);
                                }
                                String finalSerialNames = serialNames;
                                double finalQuantity = qty;
                                if (product.isIsSerialForProduct()) {
                                    if (!StringUtil.isNullOrEmpty(approvalSerials)) {
                                        String[] adjSerialArr = serialNames.split(",");
                                        String[] approvalSerialArr = approvalSerials.split(",");
                                        Set<String> adjSerialSet = new HashSet<String>(Arrays.asList(adjSerialArr));
                                        Set<String> approvalSerialSet = new HashSet<String>(Arrays.asList(approvalSerialArr));
                                        adjSerialSet.removeAll(approvalSerialSet);
                                        finalSerialNames = "";
                                        for (String serial : adjSerialSet) {
                                            if (StringUtil.isNullOrEmpty(finalSerialNames)) {
                                                finalSerialNames = serial;
                                            } else {
                                                finalSerialNames += "," + serial;
                                            }
                                        }
                                        finalQuantity = adjSerialSet.size();

                                    }
                                    totalFinalQuantity += finalQuantity;
                                } else {
                                    finalQuantity = 0;
                                    totalFinalQuantity = 0;
                                    finalSerialNames = null;
                                }


                                StockAdjustmentDetail sad = new StockAdjustmentDetail();
                                sad.setStockAdjustment(stockAdjustment);
                                sad.setLocation(location);
                                sad.setRow(row);
                                sad.setRack(rack);
                                sad.setBin(bin);

                                sad.setBatchName(batchName);
                                sad.setSerialNames(serialNames);
                                sad.setFinalSerialNames(finalSerialNames);
                                sad.setQuantity(qty);
                                sad.setFinalQuantity(finalQuantity);
                                adjustmentDetailSet.add(sad);

                            }
                            stockAdjustment.setFinalQuantity(totalFinalQuantity);
                            stockAdjustment.setStockAdjustmentDetail(adjustmentDetailSet);
                            if (!StringUtil.isNullOrEmpty(qApproval)) {
                                sendForQAApproval = Boolean.parseBoolean(qApproval);
                            }

                            if (sendForQAApproval) {
                                if ((StringUtil.isNullOrEmpty(extraCompanyPreferences.getInspectionStore()) && StringUtil.isNullOrEmpty(extraCompanyPreferences.getRepairStore()))) {
                                    throw new InventoryException(InventoryException.Type.NULL, "QA Store and Repair Store are not set in Company Preferences.");
                                } else if ((StringUtil.isNullOrEmpty(extraCompanyPreferences.getInspectionStore()))) {
                                    throw new InventoryException(InventoryException.Type.NULL, "QA Store is not set in Company Preferences.");
                                } else if (StringUtil.isNullOrEmpty(extraCompanyPreferences.getRepairStore())) {
                                    throw new InventoryException(InventoryException.Type.NULL, "Repair Store is not set in Company Preferences.");
                                }
                            }
                            HashMap<String, Object> requestparams = new HashMap<>();
                            requestparams.put("locale", locale);
                            requestparams.put(Constants.LineLevelCustomData, linelevelcustomdata);
                            stockAdjustmentService.requestStockAdjustment(user, stockAdjustment, allowNegativeInventory, sendForQAApproval, null, requestparams);
                            if (inventoryJE != null) {
                                inventoryJE.setTransactionId(stockAdjustment.getId());
                            }
                            // code for send mail notification when item qty goes below than reorder level.

                            DocumentEmailSettings documentEmailSettings = null;
                            KwlReturnObject documentEmailresult = accountingHandlerDAO.getObject(DocumentEmailSettings.class.getName(), companyId);
                            documentEmailSettings = documentEmailresult != null ? (DocumentEmailSettings) documentEmailresult.getEntityList().get(0) : null;
                            if (documentEmailSettings.isQtyBelowReorderLevelMail()) {
                                for (StockAdjustmentDetail sadetail : stockAdjustment.getStockAdjustmentDetail()) {
                                    double availableQtyinStore = stockService.getProductQuantityInStore(stockAdjustment.getProduct(), stockAdjustment.getStore());
                                    if (availableQtyinStore < stockAdjustment.getProduct().getReorderLevel() && stockAdjustment.getQuantity() < 0) {
                                        HashMap<String, String> data = new HashMap<String, String>();
                                        data.put("productName", stockAdjustment.getProduct().getProductName());
                                        data.put("storeId", stockAdjustment.getStore().getId());
                                        data.put("availableQty", Double.toString(availableQtyinStore));
                                        accountingHandlerDAO.sendReorderLevelEmails(user.getUserID(), null, TransactionModule.STOCK_ADJUSTMENT.toString(), data);
                                    }
                                }
                            }
                        }
                    }
                    if (!"NA".equals(seqFormatId)) {
                        seqService.updateSeqNumber(seqFormat);
                    }
                }
                issuccess = true;
            }
            if (status != null) {
                importDao.saveImportLog(logDataMap);
                String tableName = importDao.getTableName(logDataMap.get("StorageName").toString());
                importDao.removeFileTable(tableName); // Remove table after importing all records
                txnManager.commit(status);
            }
            
        } catch (AccountingException ex) {
            throw new AccountingException(ex.getMessage());
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
            if (StringUtil.isNullOrEmpty(msg)) {
                msg = "Error Occurred while processing";
            }
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            jobj = jobjData;
        }
        return jobj;
    }
    
    public boolean isvalidDate(Date bussinesDate, CompanyAccountPreferences preferences) {

        boolean valid = true;
        if (preferences != null && preferences.getFinancialYearFrom() != null) {
            Date finanDate = preferences.getFirstFinancialYearFrom() != null ? preferences.getFirstFinancialYearFrom() : preferences.getFinancialYearFrom();
            Date bookdate = preferences.getBookBeginningFrom();

            finanDate = removeTimefromDate(finanDate);
            bussinesDate = removeTimefromDate(bussinesDate);
            bookdate = removeTimefromDate(bookdate);

            if (finanDate.after(bussinesDate) || bookdate.after(bussinesDate)) {//if date is less than first financial year date
                valid = false;
            }
        }
        return valid;
    }

    public static Date removeTimefromDate(Date sampledate) { //removing time from date-Neeraj D
        Calendar c = Calendar.getInstance();
        c.setTime(sampledate);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        sampledate = c.getTime();
        return sampledate;
    }
}
