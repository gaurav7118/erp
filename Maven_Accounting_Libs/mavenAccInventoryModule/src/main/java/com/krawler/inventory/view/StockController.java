/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.view;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.ItemReusability;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccProductCustomData;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.UnitOfMeasure;
import com.krawler.inventory.exception.InventoryException;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.location.LocationService;
import com.krawler.inventory.model.stock.Stock;
import com.krawler.inventory.model.stock.StockService;
import com.krawler.inventory.model.stockout.StockAdjustmentDetail;
import com.krawler.inventory.model.store.Store;
import com.krawler.inventory.model.store.StoreService;
import com.krawler.inventory.model.store.StoreType;
import static com.krawler.inventory.view.ImportDataController.getActualFileName;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.masteritems.accMasterItemsController;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map.Entry;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.collections.ListUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author Vipin Gupta
 */
public class StockController extends MultiActionController {

    private static final Logger lgr = Logger.getLogger(StockController.class.getName());
    private HibernateTransactionManager txnManager;
    private String successView;
    private StockService stockService;
    private StoreService storeService;
    private LocationService locationService;
    private AccountingHandlerDAO accountingHandlerDAO;
    private accAccountDAO accAccountDAOobj;
    private accProductDAO productDAOObj;
    private AccCommonTablesDAO accCommonTablesDAO;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private auditTrailDAO auditTrailObj;
    private exportMPXDAOImpl exportDAO;
    private accMasterItemsDAO accMasterItemsDAOobj;
    private ImportInvData importInvData;
    public  ImportHandler importHandler;

    public void setAccMasterItemsDAOobj(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }

    private static final DateFormat yyyyMMdd_HIPHON = new SimpleDateFormat("yyyy-MM-dd");

    public void setAccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAO) {
        this.accountingHandlerDAO = accountingHandlerDAO;
    }

    public void setStockService(StockService stockService) {
        this.stockService = stockService;
    }

    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }

    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public void setExportDAO(exportMPXDAOImpl exportDAO) {
        this.exportDAO = exportDAO;
    }

    public accProductDAO getProductDAOObj() {
        return productDAOObj;
    }

    public void setProductDAOObj(accProductDAO productDAOObj) {
        this.productDAOObj = productDAOObj;
    }
    
    public ImportHandler getImportHandler() {
        return importHandler;
    }
    
    public void setImportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    public ImportInvData getImportInvData() {
        return importInvData;
    }

    public void setImportInvData(ImportInvData importInvData) {
        this.importInvData = importInvData;
    }
 
    
    
    public ModelAndView getBachWiseStockInventory(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
//        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
//        def.setName("STI_Tx");
//        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
//        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        JSONArray productCustomFieldInfo = new JSONArray();
        Paging paging = null;
        try {

            String companyId = sessionHandlerImpl.getCompanyid(request);
            String userId = sessionHandlerImpl.getUserid(request);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);
            Company company = user.getCompany();

            KwlReturnObject cap1 = accountingHandlerDAO.getObject(ExtraCompanyPreferences.class.getName(), companyId);
            ExtraCompanyPreferences ecf = (ExtraCompanyPreferences) cap1.getEntityList().get(0);

            boolean isSku = ecf.isSKUFieldParm();

            // Find out any product custom fields need to show in this report
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String customFieldQuery = "select customcolumninfo from showcustomcolumninreport where moduleid = ? and companyid = ?";
            List<String> customFieldinfoList = null;
            customFieldinfoList = accCommonTablesDAO.executeSQLQuery(customFieldQuery, new Object[]{Constants.Acc_Product_Master_ModuleId, companyId});

            HashMap<String, HashMap> productCustomData = new HashMap<String, HashMap>();
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = null;

            if (customFieldinfoList.size() > 0) {
                String jsonString = customFieldinfoList.get(0);
                JSONArray productCustomFields = new JSONArray(jsonString);
                String fieldIds = "";
                for (int jCnt = 0; jCnt < productCustomFields.length(); jCnt++) {
                    fieldIds = fieldIds.concat("'").concat(productCustomFields.getJSONObject(jCnt).getString("fieldid")).concat("',");
                }
                if (!StringUtil.isNullOrEmpty(fieldIds)) {
                    fieldIds = fieldIds.substring(0, fieldIds.length() - 1);
                }
                HashMap<String, Object> fieldrequestParams = new HashMap();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, "INid"));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyId, Constants.Acc_Product_Master_ModuleId, fieldIds));
                FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            }

            if (replaceFieldMap.size() > 0) {
                for (Map.Entry<String, String> varEntry : replaceFieldMap.entrySet()) {
                    JSONObject fieldInfo = new JSONObject();
                    fieldInfo.put("dataindex", varEntry.getKey());
                    fieldInfo.put("columnname", varEntry.getKey().replaceAll("Custom_", ""));
                    productCustomFieldInfo.put(fieldInfo);
                }
            }

            String exportFileName = request.getParameter("filename"); // for Export
            boolean isExport = false;
            if (!StringUtil.isNullOrEmpty(exportFileName)) {
                isExport = true;
                paging = null;
            } else {
                String start = request.getParameter("start");
                String limit = request.getParameter("limit");
                paging = new Paging(start, limit);
            }
            String searchString = request.getParameter("ss");

            String storeId = request.getParameter("store");
            String locationId = request.getParameter("location");
            Set<Store> storeSet = null;
            if (!StringUtil.isNullOrEmpty(storeId)) {
                Store store = storeService.getStoreById(storeId);
                if (store != null) {
                    storeSet = new HashSet<>();
                    storeSet.add(store);
                }

            } else {
                boolean includeQAAndRepairStore = true;
                boolean includePickandPackStore = true;
                List<Store> stores = storeService.getStoresByStoreExecutivesAndManagers(user, true, null, null, null,includeQAAndRepairStore,includePickandPackStore);
                if (!stores.isEmpty()) {
                    storeSet = new HashSet<>(stores);
                }
            }
//            Store store = storeService.getStoreById(storeId);
            Location location = null;
            if (!StringUtil.isNullOrEmpty(locationId)) {
                location = locationService.getLocation(locationId);
            }

//            List<Stock> stockList = stockService.getBatchWiseStockList(company, store, location, searchString, paging);
            String productid = "";
            List<Object[]> stockList = stockService.getStoreWiseDetailedStockList(company, storeSet, location, searchString, paging,productid);

            for (Object[] stock : stockList) {
                String productId = stock[0] != null ? (String) stock[0] : null;
                String productCode = stock[1] != null ? (String) stock[1] : null;
                String productName = stock[2] != null ? (String) stock[2] : null;
                String productDescription = stock[3] != null ? (String) stock[3] : null;
                String uomName = stock[4] != null ? (String) stock[4] : null;
                ItemReusability itemReusability = stock[5] != null ? (ItemReusability) stock[5] : null;
                String storeAbbrev = stock[6] != null ? (String) stock[6] : null;
                String storeDesc = stock[7] != null ? (String) stock[7] : null;
                String locName = stock[8] != null ? (String) stock[8] : null;
                String rowName = stock[9] != null ? (String) stock[9] : null;
                String rackName = stock[10] != null ? (String) stock[10] : null;
                String binName = stock[11] != null ? (String) stock[11] : null;
                String batchName = stock[12] != null ? (String) stock[12] : null;
                String serialNames = stock[13] != null ? (String) stock[13] : null;
                double quantity = stock[14] != null ? (Double) stock[14] : 0;
                String strId = stock[15] != null ? (String) stock[15] : null;
                String locatioId = stock[16] != null ? (String) stock[16] : null;
                JSONObject jObj = new JSONObject();

//                jObj.put("id", stock.getId());
                jObj.put("itemid", productId);
                jObj.put("itemcode", productCode);
//                jObj.put("partnumber", product == null ? "" : product.getCoilcraft());
                jObj.put("itemname", productName);
                jObj.put("itemdescription", productDescription);
//                jObj.put("ccpartnumber", product == null ? "" : product.getCoilcraft());
                jObj.put("uom", uomName);
                jObj.put("storecode", storeAbbrev);
                jObj.put("storedescription", storeDesc);
                jObj.put("location", locName);
                jObj.put("rowName", rowName);
                jObj.put("rackName", rackName);
                jObj.put("binName", binName);              
                jObj.put("quantity", isExport?authHandler.formattedQuantity(quantity,companyId):quantity);              
                jObj.put("batchName", batchName);
                jObj.put("serials", serialNames);
//                ItemReusability itemReusability = itemReusability;
                jObj.put("stockType", itemReusability != null ? itemReusability.toString() : "");
                jObj.put("stockTypeName", itemReusability == ItemReusability.REUSABLE ? "R" : (itemReusability == ItemReusability.DISPOSABLE ? "C" : "C"));

                NewProductBatch productBatch = stockService.getERPProductBatch(productId, strId, locatioId, null, null, null, batchName, companyId);
                if (!StringUtil.isNullOrEmpty(serialNames)) {
                    String serials = "";
                    String[] serialArray = serialNames.split(",");
                    String expDate = "";
                    String expDates = "";
                    String assets = "";
                        for (String serialName : serialArray) {
                            //ERP-33003: To get the Serial Batch to display Expiry date of product for QA and Repair Store.
                            //NewBatchSerial batchSerial = stockService.getERPBatchSerial(productId, productBatch, serialName.trim(), companyId);
                            KwlReturnObject returnProduct = productDAOObj.getProductByID(productId, companyId);
                            Product product = (Product) returnProduct.getEntityList().get(0);
                            NewBatchSerial batchSerial = stockService.getERPBatchSerial(product, productBatch, serialName.trim());
                            /*
                            if batchSerial is null then get Asset(SKU Field details) details from below method - SDP-5902 
                            */
                            if(batchSerial==null){
                              batchSerial=stockService.getSkuBySerialName(productId, serialName.trim(), companyId);  
                            }
                            if (batchSerial != null) {
                                serials += serialName + ", ";
                                String asset = ((!StringUtil.isNullOrEmpty(batchSerial.getSkufield()) && isSku) ? batchSerial.getSkufield() : "");
                                expDate = (batchSerial.getExptodate() != null ? batchSerial.getExptodate().toString() : "");
                                expDates += expDate + " , ";
                                assets += asset + " , ";
                            }
                        }
                        if (serials.endsWith(", ")) {
                            jObj.put("serials", serials.substring(0, serials.length() - 2));
                            jObj.put("serialexpdate", expDates.substring(0, expDates.length() - 2));
                            jObj.put("itemasset", assets.substring(0, assets.length() - 2));
                        }
                    } else if (productBatch != null) {
                    jObj.put("serialexpdate", productBatch.getExpdate());
                }
                    
                // Add Product Level Custom Fiels 
                if (FieldMap != null) {
                    if (productCustomData.containsKey(productId)) {
                        HashMap<String, String> prodDataArray = productCustomData.get(productId);
                        for (Map.Entry<String, String> varEntry : prodDataArray.entrySet()) {
                            jObj.put(varEntry.getKey(), varEntry.getValue());
                        }
                    } else {
                        AccProductCustomData obj1 = (AccProductCustomData) kwlCommonTablesDAOObj.getClassObject(AccProductCustomData.class.getName(), productId);
                        if (obj1 != null) {
                            HashMap<String, String> prodDataArray = new HashMap<String, String>();
                            HashMap<String, Object> variableMap = new HashMap<String, Object>();
                            AccountingManager.setCustomColumnValues(obj1, FieldMap, replaceFieldMap, variableMap);
                            DateFormat defaultDateFormat=new SimpleDateFormat(Constants.MMMMdyyyy);
                            Date dateFromDB=null;
                            for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                                String coldata = varEntry.getValue().toString();
                                if (customFieldMap.containsKey(varEntry.getKey())) {
                                    boolean isCustomExport = true;
                                    String value = "";
                                    String Ids[] = coldata.split(",");
                                    for (int i = 0; i < Ids.length; i++) {
                                        FieldComboData fieldComboData = (FieldComboData) kwlCommonTablesDAOObj.getClassObject(FieldComboData.class.getName(), Ids[i]);
                                        if (fieldComboData != null) {
                                            if (fieldComboData.getField().getFieldtype() == 12 && !isCustomExport) {
                                                value += Ids[i] != null ? Ids[i] + "," : ",";
                                            } else {
                                                value += fieldComboData.getValue() != null ? fieldComboData.getValue() + "," : ",";
                                            }
                                        }
                                    }
                                    if (!StringUtil.isNullOrEmpty(value)) {
                                        value = value.substring(0, value.length() - 1);
                                    }
                                    prodDataArray.put(varEntry.getKey(), value);
                                    jObj.put(varEntry.getKey(), value);
                                } else if (customDateFieldMap.containsKey(varEntry.getKey())) {
                                    
                                    try {
                                        dateFromDB = defaultDateFormat.parse(coldata);
                                        coldata = df.format(dateFromDB);
                                    } catch (Exception e) {
                                        Logger.getLogger(StockController.class.getName()).log(Level.SEVERE, null, e);
                                    }
                                    jObj.put(varEntry.getKey(), coldata);
                                    prodDataArray.put(varEntry.getKey(),coldata);
                                } else {
                                    if (!StringUtil.isNullOrEmpty(coldata)) {
                                        jObj.put(varEntry.getKey(), coldata);
                                        prodDataArray.put(varEntry.getKey(), coldata);
                                    }
                                }
                            }
                            productCustomData.put(productId, prodDataArray);
                        }
                    }
                }
                jArray.put(jObj);
            }
            if (isExport) {
                jobj.put("data", jArray);
                jobj.put("productcustomfield", productCustomFieldInfo);
                exportDAO.processRequest(request, response, jobj);
            }
            issuccess = true;
            msg = "Stock list has been fetched successfully";

//            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
//            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
//            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
//            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArray);
                if (paging != null) {
                    jobj.put("count", paging.getTotalRecord());
                } else {
                    jobj.put("count", jArray.length());
                }
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

    public ModelAndView getDateWiseBatchStockInventory(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
//        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
//        def.setName("STI_Tx");
//        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
//        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        JSONArray productCustomFieldInfo = new JSONArray();
        Paging paging = null;
        try {

            String loginId = sessionHandlerImpl.getUserid(request);
            String companyId = sessionHandlerImpl.getCompanyid(request);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), loginId);
            User user = (User) jeresult.getEntityList().get(0);
            Company company = user.getCompany();

            // Find out any product custom fields need to show in this report
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String customFieldQuery = "select customcolumninfo from showcustomcolumninreport where moduleid = ? and companyid = ?";
            List<String> customFieldinfoList = null;
            customFieldinfoList = accCommonTablesDAO.executeSQLQuery(customFieldQuery, new Object[]{Constants.Acc_Product_Master_ModuleId, companyId});

            HashMap<String, HashMap> productCustomData = new HashMap<String, HashMap>();
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = null;

            if (customFieldinfoList.size() > 0) {
                String jsonString = customFieldinfoList.get(0);
                JSONArray productCustomFields = new JSONArray(jsonString);
                String fieldIds = "";
                for (int jCnt = 0; jCnt < productCustomFields.length(); jCnt++) {
                    fieldIds = fieldIds.concat("'").concat(productCustomFields.getJSONObject(jCnt).getString("fieldid")).concat("',");
                }
                if (!StringUtil.isNullOrEmpty(fieldIds)) {
                    fieldIds = fieldIds.substring(0, fieldIds.length() - 1);
                }
                HashMap<String, Object> fieldrequestParams = new HashMap();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, "INid"));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyId, Constants.Acc_Product_Master_ModuleId, fieldIds));
                FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            }

            if (replaceFieldMap.size() > 0) {
                for (Map.Entry<String, String> varEntry : replaceFieldMap.entrySet()) {
                    JSONObject fieldInfo = new JSONObject();
                    fieldInfo.put("dataindex", varEntry.getKey());
                    fieldInfo.put("columnname", varEntry.getKey().replaceAll("Custom_", ""));
                    productCustomFieldInfo.put(fieldInfo);
                }
            }

            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            paging = new Paging(start, limit);

            String exportFileName = request.getParameter("filename"); // for Export
            boolean isExport = false;
            if (!StringUtil.isNullOrEmpty(exportFileName)) {
                isExport = true;
                paging = null;
            }
            String ondate = request.getParameter("ondate");
            Date date = df.parse(ondate);
            String searchString = request.getParameter("ss");

            String storeId = request.getParameter("store");
            String locationId = request.getParameter("location");
            Location location = null;
            if (!StringUtil.isNullOrEmpty(locationId)) {
                location = locationService.getLocation(locationId);
            }

            Set<Store> storeSet = null;
            if (!StringUtil.isNullOrEmpty(storeId)) {
                Store store = storeService.getStoreById(storeId);
                if (store != null) {
                    storeSet = new HashSet<>();
                    storeSet.add(store);
                }

            } else {
                boolean includeQAAndRepairStore = true;
                boolean includePickandPackStore = true;
                List<Store> stores = storeService.getStoresByStoreExecutivesAndManagers(user, true, null, null, null,includeQAAndRepairStore,includePickandPackStore);
                if (!stores.isEmpty()) {
                    storeSet = new HashSet<>(stores);
                }
            }
            Map<Product, List<Object[]>> stockDetailMap = stockService.getDateWiseStockDetailList(company, storeSet, location, date, searchString, null); // only use paging on product not on records
            for (Entry<Product, List<Object[]>> entry : stockDetailMap.entrySet()) {
                Product product = entry.getKey();
                for (Object[] stock : entry.getValue()) {
                    JSONObject jObj = new JSONObject();
//                    jObj.put("id", stock.getId());
//                    Product product = stock.getProduct();
                    jObj.put("itemid", product == null ? "" : product.getID());
                    jObj.put("itemcode", product == null ? "" : product.getProductid());
//                    jObj.put("partnumber", product == null ? "" : product.getCoilcraft());
                    jObj.put("itemname", (product != null) ? product.getName() : "");
                    jObj.put("itemdescription", product == null ? "" : product.getDescription());
//                    jObj.put("ccpartnumber", product == null ? "" : product.getCoilcraft());
                    jObj.put("uom", stock[10]);//product.getUnitOfMeasure() == null ? "" : product.getUnitOfMeasure().getName());
                    jObj.put("storecode", stock[1]);//stock.getStore() == null ? "" : stock.getStore().getAbbreviation());
                    jObj.put("storedescription", stock[9]);//stock.getStore() == null ? "" : stock.getStore().getDescription());
                    jObj.put("location", stock[2]);//stock.getLocation() == null ? "" : stock.getLocation().getName());
                    jObj.put("rowName", stock[3]);//stock.getRow() == null ? "" : stock.getRow().getName());
                    jObj.put("rackName", stock[4]);//stock.getRack() == null ? "" : stock.getRack().getName());
                    jObj.put("binName", stock[5]);//stock.getBin() == null ? "" : stock.getBin().getName());                   
                    jObj.put("quantity", isExport?authHandler.formattedQuantity((double)stock[8],companyId):stock[8]);//stock.getQuantity());                 
                    jObj.put("batchName", stock[6]);//stock.getBatchName());
                    jObj.put("serials", (product != null && product.isIsSerialForProduct()) ? stock[7] : "");
                    ItemReusability itemReusability = product != null ? product.getItemReusability() : null ;
                    jObj.put("stockType", itemReusability != null ? itemReusability.toString() : "");
                    jObj.put("stockTypeName", (itemReusability != null ) ? (itemReusability == ItemReusability.REUSABLE  ? "R" : (itemReusability == ItemReusability.DISPOSABLE ? "C" : "C")) : "");

                    // Add Product Level Custom Fiels 
                    if (FieldMap != null) {
                        if (productCustomData.containsKey(product.getID())) {
                            HashMap<String, String> prodDataArray = productCustomData.get(product.getID());
                            for (Map.Entry<String, String> varEntry : prodDataArray.entrySet()) {
                                jObj.put(varEntry.getKey(), varEntry.getValue());
                            }
                        } else {
                            AccProductCustomData obj1 = (AccProductCustomData) kwlCommonTablesDAOObj.getClassObject(AccProductCustomData.class.getName(), product.getID());
                            if (obj1 != null) {
                                HashMap<String, String> prodDataArray = new HashMap<String, String>();
                                HashMap<String, Object> variableMap = new HashMap<String, Object>();
                                AccountingManager.setCustomColumnValues(obj1, FieldMap, replaceFieldMap, variableMap);
                                DateFormat defaultDateFormat=new SimpleDateFormat(Constants.MMMMdyyyy);
                                Date dateFromDB=null;
                                for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                                    String coldata = varEntry.getValue().toString();
                                    if (customFieldMap.containsKey(varEntry.getKey())) {
                                        boolean isCustomExport = true;
                                        String value = "";
                                        String Ids[] = coldata.split(",");
                                        for (int i = 0; i < Ids.length; i++) {
                                            FieldComboData fieldComboData = (FieldComboData) kwlCommonTablesDAOObj.getClassObject(FieldComboData.class.getName(), Ids[i]);
                                            if (fieldComboData != null) {
                                                if (fieldComboData.getField().getFieldtype() == 12 && !isCustomExport) {
                                                    value += Ids[i] != null ? Ids[i] + "," : ",";
                                                } else {
                                                    value += fieldComboData.getValue() != null ? fieldComboData.getValue() + "," : ",";
                                                }
                                            }
                                        }
                                        if (!StringUtil.isNullOrEmpty(value)) {
                                            value = value.substring(0, value.length() - 1);
                                        }
                                        prodDataArray.put(varEntry.getKey(), value);
                                        jObj.put(varEntry.getKey(), value);
                                    } else if (customDateFieldMap.containsKey(varEntry.getKey())) {
                                        try {
                                            dateFromDB = defaultDateFormat.parse(coldata);
                                            coldata = df.format(dateFromDB);
                                        } catch (Exception e) {
                                        }
                                        jObj.put(varEntry.getKey(), coldata);
                                        prodDataArray.put(varEntry.getKey(),coldata);
                                    } else {
                                        if (!StringUtil.isNullOrEmpty(coldata)) {
                                            jObj.put(varEntry.getKey(), coldata);
                                            prodDataArray.put(varEntry.getKey(), coldata);
                                        }
                                    }
                                }
                                productCustomData.put(product.getID(), prodDataArray);
                            }
                        }
                    }
                    jArray.put(jObj);
                }
            }
            if (isExport) {
                jobj.put("data", jArray);
                jobj.put("productcustomfield", productCustomFieldInfo);
                exportDAO.processRequest(request, response, jobj);
            }
            issuccess = true;
            msg = "Stock list has been fetched successfully";

//            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
//            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
//            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
//            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArray);
                if (paging != null) {
                    jobj.put("count", paging.getTotalRecord());
                } else {
                    jobj.put("count", jArray.length());
                }
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

    public ModelAndView getStoreWiseStockInventory(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        String msg = "";
        String inventoryCatType = "";
        boolean issuccess = false;
//        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
//        def.setName("IST_Tx");
//        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
//        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        JSONArray productCustomFieldInfo = new JSONArray();
        try {

            if (request.getParameter("inventoryCatType") != null) {
                inventoryCatType = (String) request.getParameter("inventoryCatType");
            }
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String userId = sessionHandlerImpl.getUserid(request);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            Company company = user.getCompany();

            KwlReturnObject cap1 = accountingHandlerDAO.getObject(ExtraCompanyPreferences.class.getName(), companyId);
            ExtraCompanyPreferences ecf = (ExtraCompanyPreferences) cap1.getEntityList().get(0);

            String repairStore = ecf.getRepairStore();
            String qaStore = ecf.getInspectionStore();

            // Find out any product custom fields need to show in this report
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String customFieldQuery = "select customcolumninfo from showcustomcolumninreport where moduleid = ? and companyid = ?";
            List<String> customFieldinfoList = null;
            customFieldinfoList = accCommonTablesDAO.executeSQLQuery(customFieldQuery, new Object[]{Constants.Acc_Product_Master_ModuleId, companyId});
            boolean isnegativestockforlocwar = false;
            isnegativestockforlocwar = ecf.isIsnegativestockforlocwar();
            HashMap<String, HashMap> productCustomData = new HashMap<String, HashMap>();
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = null;

            if (customFieldinfoList.size() > 0) {
                String jsonString = customFieldinfoList.get(0);
                JSONArray productCustomFields = new JSONArray(jsonString);
                String fieldIds = "";
                for (int jCnt = 0; jCnt < productCustomFields.length(); jCnt++) {
                    fieldIds = fieldIds.concat("'").concat(productCustomFields.getJSONObject(jCnt).getString("fieldid")).concat("',");
                }
                if (!StringUtil.isNullOrEmpty(fieldIds)) {
                    fieldIds = fieldIds.substring(0, fieldIds.length() - 1);
                }
                HashMap<String, Object> fieldrequestParams = new HashMap();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, "INid"));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyId, Constants.Acc_Product_Master_ModuleId, fieldIds));
                FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            }

            if (replaceFieldMap.size() > 0) {
                for (Map.Entry<String, String> varEntry : replaceFieldMap.entrySet()) {
                    JSONObject fieldInfo = new JSONObject();
                    fieldInfo.put("dataindex", varEntry.getKey());
                    fieldInfo.put("columnname", varEntry.getKey().replaceAll("Custom_", ""));
                    productCustomFieldInfo.put(fieldInfo);
                }
            }

            String exportFileName = request.getParameter("filename"); // for Export
            boolean isExport = false;
            if (!StringUtil.isNullOrEmpty(exportFileName)) {
                isExport = true;
            } else {
                String start = request.getParameter("start");
                String limit = request.getParameter("limit");
                paging = new Paging(start, limit);
            }
            String searchString = request.getParameter("ss");

            String storeId = request.getParameter("store");
            String locationId = request.getParameter("location");
            Set<Store> storeSet = null;
            Store store = null;
            boolean singleStore = false;
            boolean isQAorRepairStore = false;
            if (!StringUtil.isNullOrEmpty(storeId)) {
                store = storeService.getStoreById(storeId);
                if (store != null) {
                    storeSet = new HashSet<>();
                    storeSet.add(store);
                    singleStore = true;
                    if (storeId.equals(qaStore) || storeId.equals(repairStore)) {
                        isQAorRepairStore = true;
                    }
                }

            } else {
//                List<Store> stores = storeService.getStores(user.getCompany(), null, null);
                boolean includeQAAndRepairStore = true;
                boolean includePickandPackStore = true;
                List<Store> stores = storeService.getStoresByStoreExecutivesAndManagers(user, true, null, null, null, includeQAAndRepairStore,includePickandPackStore);
                if (!stores.isEmpty()) {
                    storeSet = new HashSet<>(stores);
//                    for (Store s : stores) {
//                        Set<User> storeManagers = s.getStoreManagerSet();
//                        storeManagers.addAll(s.getStoreExecutiveSet());
//                        if (storeManagers.contains(user)) {
//                            storeSet.add(s);
//                        }
//                    }
                }
            }
            List<Object[]> stockList = null;
            Location location = null;
            InventoryLocation invLocation=null;
            if (!StringUtil.isNullOrEmpty(locationId)) {
                location = locationService.getLocation(locationId);
                
                jeresult = accountingHandlerDAO.getObject(InventoryLocation.class.getName(), locationId);
//                invLocation = (InventoryLocation) jeresult.getEntityList().get(0);
            }
            
//            List<InventoryWarehouse> invStore=storeService.getWarehouseByStoreExecutivesAndManagers(user, true, storeSet);
//            Set<InventoryWarehouse> wareHouseSet = new HashSet<>(invStore);
            if (storeSet != null && !storeSet.isEmpty()) {
                stockList = stockService.getStoreWiseProductStockList(company, storeSet, location, searchString, paging, inventoryCatType);
//                stockList = stockService.getWarehouseWiseProductStockList(company, wareHouseSet, invLocation, searchString, paging, inventoryCatType);
            }
            Map<String, Double> quantityUnderQA = stockService.getProductQuantityUnderQA(company, store);
            Map<String, Double> quantityUnderRepair = stockService.getProductQuantityUnderRepair(company, store);
            Map<String, Double> blockedQuantityList = stockService.getProductBlockedQuantity(company, store, location, searchString);
            if (stockList != null && !stockList.isEmpty()) {
                for (Object[] obj : stockList) {
                    String productId = obj[0] != null ? (String) obj[0] : null;
                    String productCode = obj[1] != null ? (String) obj[1] : null;
                    String productName = obj[2] != null ? (String) obj[2] : null;
                    String productDescription = obj[3] != null ? (String) obj[3] : null;
                    String uomName = obj[4] != null ? (String) obj[4] : null;
                    ItemReusability itemReusability = obj[5] != null ? (ItemReusability) obj[5] : null;
                    double pQty = obj[6] != null ? (Double) obj[6] : 0;
                    boolean isSerialForProduct = obj[7] != null ? (Boolean) obj[7] : false;
                    boolean isBatchForProduct = obj[8] != null ? (Boolean) obj[8] : false;
                    
//                     boolean isSerialForProduct = (obj[7] != null && obj[7].toString().equals("T")) ? true : false;
//                    boolean isBatchForProduct =  (obj[8] != null && obj[8].toString().equals("T")) ? true : false;
                    JSONObject jObj = new JSONObject();

                    double qaQuantity = 0;
                    if (quantityUnderQA != null && quantityUnderQA.containsKey(productId)) {
                        qaQuantity = quantityUnderQA.get(productId);
                    }
                    double repairQuantity = 0;
                    if (quantityUnderRepair != null && quantityUnderRepair.containsKey(productId)) {
                        repairQuantity = quantityUnderRepair.get(productId);
                    }
                    double blkQuantity = 0;
                    if (blockedQuantityList != null && blockedQuantityList.containsKey(productId)) {
                        blkQuantity = blockedQuantityList.get(productId);
                    }
//                    jObj.put("id", ist.getId());
                    jObj.put("itemid", productId);
                    jObj.put("itemcode", productCode);
//                    jObj.put("partnumber", product.getCoilcraft());
                    jObj.put("itemdescription", productDescription);
                    jObj.put("itemname", productName);
//                    jObj.put("ccpartnumber", product.getCoilcraft());
                    jObj.put("uom", uomName);
//                    jObj.put("storecode", ist.getStore().getAbbreviation());
//                    jObj.put("storedescription", ist.getStore().getDescription());
                    double qty = singleStore ? (isQAorRepairStore ? (pQty) - (qaQuantity + repairQuantity) : pQty) : (pQty) - (qaQuantity + repairQuantity);
                    if (qty < 0 && ( isnegativestockforlocwar && ! (isBatchForProduct || isSerialForProduct))) {
                        jObj.put("quantity", authHandler.formattedQuantity((qty - blkQuantity),companyId));
                    } else {
//                        jObj.put("quantity", qty > blkQuantity ? (qty - blkQuantity) : (blkQuantity - qty));
//                        jObj.put("quantity", (qty > blkQuantity || qty < 0) ? (qty - blkQuantity) : (blkQuantity - qty));
                          jObj.put("quantity", authHandler.formattedQuantity((qty - blkQuantity),companyId));
                    } 
//                    jObj.put("quantity", qty);
                    jObj.put("blockquantity", authHandler.formattedQuantity(blkQuantity, companyId));
                    jObj.put("qaquantity", authHandler.formattedQuantity(qaQuantity,companyId));
                    jObj.put("repairquantity", authHandler.formattedQuantity(repairQuantity,companyId));
//                    jObj.put("batchPricePerUnit", ist.getPricePerUnit());
//                    jObj.put("batchNo", ist.getBatchNo());
//                    jObj.put("batchQty", ist.getQuantity());
//                    jObj.put("isBatchForProduct", product.isIsBatchForProduct());
                    jObj.put("isSerialForProduct", isSerialForProduct);
//                    jObj.put("isRowForProduct", product.isIsrowforproduct());
//                    jObj.put("isRackForProduct", product.isIsrackforproduct());
//                    jObj.put("isBinForProduct", product.isIsbinforproduct());
//                    ItemReusability itemReusability = product.getItemReusability();
                    jObj.put("stockType", itemReusability != null ? itemReusability.toString() : "");
                    jObj.put("stockTypeName", itemReusability == ItemReusability.REUSABLE ? "R" : (itemReusability == ItemReusability.DISPOSABLE ? "C" : "C"));

//                if (store != null) {
//                    JSONArray stockDetails = new JSONArray();
//                    List<Stock> stockDetailList = stockService.getBatchSerialListByStoreProductLocation(product, storeSet, location);
//                    if (stockDetailList != null && !stockDetailList.isEmpty()) {
//                        for (Stock stock : stockDetailList) {
//                            if (stock != null) {
//                                JSONObject srObject = new JSONObject();
////                                srObject.put("id", stock.getId());
//                                srObject.put("storeName", stock.getStore().getFullName());
//                                srObject.put("locationName", stock.getLocation().getName());
//                                srObject.put("rowName", stock.getRow() != null ? stock.getRow().getName() : "");
//                                srObject.put("rackName", stock.getRack() != null ? stock.getRack().getName() : "");
//                                srObject.put("binName", stock.getBin() != null ? stock.getBin().getName(): "");
//                                srObject.put("quantity", stock.getQuantity());
//                                srObject.put("serialNames", (stock.getSerialNames() != null) ? stock.getSerialNames().replace(",", ", ") : "");
//                                srObject.put("batchName", (stock.getBatchName() != null) ? stock.getBatchName() : "");
//                                stockDetails.put(srObject);
//                            }
//
//                        }
//                    }
//                    jObj.put("stockDetails", stockDetails);
//                }
                    // Add Product Level Custom Fiels 
                    if (FieldMap != null) {
                        if (productCustomData.containsKey(productId)) {
                            HashMap<String, String> prodDataArray = productCustomData.get(productId);
                            for (Map.Entry<String, String> varEntry : prodDataArray.entrySet()) {
                                jObj.put(varEntry.getKey(), varEntry.getValue());
                            }
                        } else {
                            AccProductCustomData obj1 = (AccProductCustomData) kwlCommonTablesDAOObj.getClassObject(AccProductCustomData.class.getName(), productId);
                            if (obj1 != null) {
                                HashMap<String, String> prodDataArray = new HashMap<String, String>();
                                HashMap<String, Object> variableMap = new HashMap<String, Object>();
                                AccountingManager.setCustomColumnValues(obj1, FieldMap, replaceFieldMap, variableMap);
                                DateFormat defaultDateFormat=new SimpleDateFormat(Constants.MMMMdyyyy);
                                Date dateFromDB=null;
                                for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                                    String coldata = varEntry.getValue().toString();
                                    if (customFieldMap.containsKey(varEntry.getKey())) {
                                        boolean isCustomExport = true;
                                        String value = "";
                                        String Ids[] = coldata.split(",");
                                        for (int i = 0; i < Ids.length; i++) {
                                            FieldComboData fieldComboData = (FieldComboData) kwlCommonTablesDAOObj.getClassObject(FieldComboData.class.getName(), Ids[i]);
                                            if (fieldComboData != null) {
                                                if (fieldComboData.getField().getFieldtype() == 12 && !isCustomExport) {
                                                    value += Ids[i] != null ? Ids[i] + "," : ",";
                                                } else {
                                                    value += fieldComboData.getValue() != null ? fieldComboData.getValue() + "," : ",";
                                                }
                                            }
                                        }
                                        if (!StringUtil.isNullOrEmpty(value)) {
                                            value = value.substring(0, value.length() - 1);
                                        }
                                        prodDataArray.put(varEntry.getKey(), value);
                                        jObj.put(varEntry.getKey(), value);
                                    } else if (customDateFieldMap.containsKey(varEntry.getKey())) {
                                        try {
                                            dateFromDB = defaultDateFormat.parse(coldata);
                                            coldata = df.format(dateFromDB);
                                        } catch (Exception e) {
                                        }

                                        jObj.put(varEntry.getKey(),coldata);
                                        prodDataArray.put(varEntry.getKey(),coldata);
                                    } else {
                                        if (!StringUtil.isNullOrEmpty(coldata)) {
                                            jObj.put(varEntry.getKey(), coldata);
                                            prodDataArray.put(varEntry.getKey(), coldata);
                                        }
                                    }
                                }
                                productCustomData.put(productId, prodDataArray);
                            }
                        }
                    }
                    jArray.put(jObj);
                }
            }
            if (isExport) {
                jobj.put("data", jArray);
                jobj.put("productcustomfield", productCustomFieldInfo);
                exportDAO.processRequest(request, response, jobj);
            }
            issuccess = true;
            msg = "Stock list has been fetched successfully";

//            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
//            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
//            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
//            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArray);
                if (paging != null) {
                    jobj.put("count", paging.getTotalRecord());
                } else {
                    jobj.put("count", jArray.length());
                }
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());

    }
    
    
    public ModelAndView getStoreWiseStockInventorySummary(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        double totalRecCount=0;
        JSONArray jArray = new JSONArray();
        JSONArray pagedJson = new JSONArray();
        Paging paging = null;
        try {

            
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String userId = sessionHandlerImpl.getUserid(request);
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            Company company = user.getCompany();

            KwlReturnObject cap1 = accountingHandlerDAO.getObject(ExtraCompanyPreferences.class.getName(), companyId);
            ExtraCompanyPreferences ecf = (ExtraCompanyPreferences) cap1.getEntityList().get(0);

            String repairStore = ecf.getRepairStore();
            String qaStore = ecf.getInspectionStore();

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            
            boolean isnegativestockforlocwar = false;
            isnegativestockforlocwar = ecf.isIsnegativestockforlocwar();

            String exportFileName = request.getParameter("filename"); // for Export
            boolean isExport = false;
            if (!StringUtil.isNullOrEmpty(exportFileName)) {
                isExport = true;
            } else {
//                paging = new Paging(start, limit);
            }
            String searchString = request.getParameter("ss");

            String storeIdStr = request.getParameter("store");
            Set<Store> storeSet= new HashSet<>();
            Store store = null;
            if (!StringUtil.isNullOrEmpty(storeIdStr)) {
                String []storeIdArr=storeIdStr.split(",");
                for (String storeId : storeIdArr) {
                    store = storeService.getStoreById(storeId);
                    if (store != null) {
                        storeSet.add(store);
                    }
                }
            } else {
                boolean includeQAAndRepairStore = true;
                boolean includePickandPackStore = true;
                List<Store> stores = storeService.getStoresByStoreExecutivesAndManagers(user, true, null, null, null, includeQAAndRepairStore,includePickandPackStore);
                if (!stores.isEmpty()) {
                    storeSet = new HashSet<>(stores);
                }
            }
            List<Object[]> stockList = null;
           
            if (storeSet != null && !storeSet.isEmpty()) {
                stockList = stockService.getStoreWiseProductStockSummaryList(company, storeSet, null, searchString, paging, null); 
            }
            Map<String, Double> quantityUnderQA = stockService.getProductQuantityUnderQA(company, null);
            Map<String, Double> quantityUnderRepair = stockService.getProductQuantityUnderRepair(company, null);
            Map<String, Double> blockedQuantityList = stockService.getProductBlockedQuantity(company, null, null, searchString);
            Map<String, Double> blockedQuantityListWithStore = stockService.getProductBlockedQuantityWithInStore(company, null, searchString);
            Map<String,ArrayList<HashMap>> prodStockMap=new HashMap<String,ArrayList<HashMap>>();
            if (stockList != null && !stockList.isEmpty()) {
                
                for(Object[] obj: stockList ){
                    ArrayList dataList;
                    String productId = obj[0] != null ? (String) obj[0] : null;
                    double prodQtyInStore = obj[1] != null ? (Double) obj[1] : 0;
                    String storeId = obj[2] != null ? (String) obj[2] : "";
                    HashMap hm=new HashMap<String,Double>();
                    hm.put(storeId, prodQtyInStore);
                    if(prodStockMap.containsKey(productId)){
                        dataList=(ArrayList)prodStockMap.get(productId);
                        dataList.add(hm);
                    }else{
                        dataList=new ArrayList();
                        dataList.add(hm);
                        prodStockMap.put(productId, dataList);
                    }
                }
                
                for(Map.Entry<String,ArrayList<HashMap>> entry : prodStockMap.entrySet()){
                    KwlReturnObject prodRes = accountingHandlerDAO.getObject(Product.class.getName(), entry.getKey());
                    Product product = (Product) prodRes.getEntityList().get(0);
                    
                    JSONObject jObj = new JSONObject();
                    
                    if (product != null) {
                        String productId=product.getID();
                        double productTotalAvailableQty=stockService.getProductTotalQuantity(product);
                        
                        double qaQuantity = 0;
                        if (quantityUnderQA != null && quantityUnderQA.containsKey(productId)) {
                            qaQuantity = quantityUnderQA.get(productId);
                        }
                        double repairQuantity = 0;
                        if (quantityUnderRepair != null && quantityUnderRepair.containsKey(productId)) {
                            repairQuantity = quantityUnderRepair.get(productId);
                        }
                        double blkQuantity = 0;
                        if (blockedQuantityList != null && blockedQuantityList.containsKey(productId)) {
                            blkQuantity = blockedQuantityList.get(productId);
                        }

                        jObj.put("itemid", productId);
                        jObj.put("itemcode", product.getProductid());
                        jObj.put("itemdescription", product.getDescription());
                        jObj.put("itemname", product.getName());
                        jObj.put("uom", product.getUnitOfMeasure() != null ? product.getUnitOfMeasure().getNameEmptyforNA() : "");
                        jObj.put("isBatchForProduct", product.isIsBatchForProduct());
                        jObj.put("isSerialForProduct", product.isIsSerialForProduct());
                        jObj.put("isRowForProduct", product.isIsrowforproduct());
                        jObj.put("isRackForProduct", product.isIsrackforproduct());
                        jObj.put("isBinForProduct", product.isIsbinforproduct());

                        ArrayList stocklt = entry.getValue();
                        for (Object obj : stocklt) {
                            HashMap<String, Double> stkMap = (HashMap<String, Double>) obj;
                            for (Map.Entry<String, Double> innerMapEntry : stkMap.entrySet()) {
                                double blockQtyForStore = 0;
                                String storeId=innerMapEntry.getKey();
                                if (blockedQuantityListWithStore != null && blockedQuantityListWithStore.containsKey(productId+storeId)) {
                                    blockQtyForStore = blockedQuantityListWithStore.get(productId+storeId);
                                }
                                jObj.put(storeId, (innerMapEntry.getValue()-blockQtyForStore));
                            }
                        }
                        
                        double qtyToDisplay=  (productTotalAvailableQty) - (qaQuantity + repairQuantity);
                        if (qtyToDisplay < 0 && ( isnegativestockforlocwar && ! (product.isIsBatchForProduct() || product.isIsSerialForProduct()))) {
                            jObj.put("quantity", authHandler.formattedQuantity((qtyToDisplay - blkQuantity),companyId));
                        } else {
                            jObj.put("quantity", authHandler.formattedQuantity( (qtyToDisplay > blkQuantity || qtyToDisplay < 0) ? (qtyToDisplay - blkQuantity) : (blkQuantity - qtyToDisplay),companyId));
                        }
                        jObj.put("blockquantity", blkQuantity);
                        jObj.put("qaquantity", qaQuantity);
                        jObj.put("repairquantity", repairQuantity);
                        jArray.put(jObj);
                        totalRecCount ++;
                    }
                }
            }
            
            pagedJson = jArray;
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                    pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            
            if (isExport) {
                jobj.put("data", jArray);
                exportDAO.processRequest(request, response, jobj);
            }
            issuccess = true;
            msg = "Stock Summary list has been fetched successfully";

        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", pagedJson);
                jobj.put("count", totalRecCount);
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

    public ModelAndView getStockDetailForProduct(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        JSONObject jo = new JSONObject();
        Paging paging = null;
        try {

            String companyId = sessionHandlerImpl.getCompanyid(request);
            String userId = sessionHandlerImpl.getUserid(request);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            String exportFileName = request.getParameter("filename"); // for Export
            boolean isExport = false;
            if (!StringUtil.isNullOrEmpty(exportFileName)) {
                isExport = true;
            }

            String productId = request.getParameter("productId");
            String storeId = request.getParameter("store");
            String locationId = request.getParameter("location");
            Product product = null;
            boolean isBatchForProduct = false;
            boolean isSerialForProduct = false;
//            double quantity = Double.parseDouble(request.getParameter("quantity"));
            if (!StringUtil.isNullOrEmpty(productId)) {
                jeresult = accountingHandlerDAO.getObject(Product.class.getName(), productId);
                product = (Product) jeresult.getEntityList().get(0);
                isBatchForProduct = product.isIsBatchForProduct();
                isSerialForProduct = product.isIsSerialForProduct();
            }
            Set<Store> storeSet = null;
            if (!StringUtil.isNullOrEmpty(storeId)) {
                Store store = storeService.getStoreById(storeId);
                if (store != null) {
                    storeSet = new HashSet<>();
                    storeSet.add(store);
                }

            } else {
                boolean includeQAAndRepairStore = true;
                boolean includePickandPackStore = true;
                List<Store> stores = storeService.getStoresByStoreExecutivesAndManagers(user, true, null, null, null, includeQAAndRepairStore,includePickandPackStore);
                if (!stores.isEmpty()) {
                    storeSet = new HashSet<>(stores);
                }
            }
            Location location = null;
            if (!StringUtil.isNullOrEmpty(locationId)) {
                location = locationService.getLocation(locationId);
            }
            List<Stock> stockDetailList = stockService.getBatchSerialListByStoreProductLocation(product, storeSet, location);
            Map<String, NewProductBatch> batchMp = stockService.getBatchListByStoreProductLocation(product, storeSet, location,false);
            Map<String, String> batchSerialMp = stockService.getSerialListByStoreProductLocation(product, storeSet, location);
            if (stockDetailList != null && !stockDetailList.isEmpty()) {

                jo.put("isBatchForProduct", product.isIsBatchForProduct());
                jo.put("isSerialForProduct", product.isIsSerialForProduct());
                jo.put("isRowForProduct", product.isIsrowforproduct());
                jo.put("isRackForProduct", product.isIsrackforproduct());
                jo.put("isBinForProduct", product.isIsbinforproduct());
                JSONArray jArray = new JSONArray();
                for (Stock stock : stockDetailList) {
                    if (stock != null) {
                        JSONObject srObject = new JSONObject();

                        srObject.put("storeName", stock.getStore().getFullName());
                        srObject.put("locationName", stock.getLocation().getName());
                        srObject.put("rowName", stock.getRow() != null ? stock.getRow().getName() : "");
                        srObject.put("rackName", stock.getRack() != null ? stock.getRack().getName() : "");
                        srObject.put("binName", stock.getBin() != null ? stock.getBin().getName() : "");
                        srObject.put("quantity", stock.getQuantity());
                        srObject.put("serialNames", (stock.getSerialNames() != null) ? stock.getSerialNames().replace(",", ", ") : "");
                        srObject.put("batchName", (stock.getBatchName() != null) ? stock.getBatchName() : "");

                        String rowRackBin = (stock.getRow() != null ? stock.getRow().getId() : "") + (stock.getRack() != null ? stock.getRack().getId() : "") + (stock.getBin() != null ? stock.getBin().getId() : "");
                        String keyValue = product.getID() + stock.getStore().getId() + stock.getLocation().getId() + rowRackBin + stock.getBatchName();
                        NewProductBatch batches = batchMp.containsKey(keyValue) ? batchMp.get(keyValue) : null;
                        if (batches != null && !product.isIsSerialForProduct()) {
                            srObject.put("blockedBatchName", batches.getLockquantity());
                            srObject.put("quantity", stock.getQuantity() - batches.getLockquantity());
                        }
                        String nsr = "";
                        if (product.isIsSerialForProduct() && !StringUtil.isNullOrEmpty(stock.getBatchName())) {
                            nsr = batchSerialMp.containsKey(stock.getBatchName() + stock.getLocation().getId()+stock.getStore().getId()) ? batchSerialMp.get(stock.getBatchName() + stock.getLocation().getId()+stock.getStore().getId()) : "";
                        } else {
                            nsr = batchSerialMp.containsKey(stock.getLocation().getId()+stock.getStore().getId()) ? batchSerialMp.get(stock.getLocation().getId()+stock.getStore().getId()) : "";
                        }
                        if (!StringUtil.isNullOrEmpty(nsr)) {
                            String[] serArr = nsr.split(",");
                            srObject.put("quantity", stock.getQuantity() - serArr.length);
                            String avlser = (stock.getSerialNames() != null) ? stock.getSerialNames().replace(",", ", ") : "";
                            srObject.put("serialNames", getAvlSerials(nsr, avlser).replace(",", ", "));
                        }
                        srObject.put("blockedSerialNames", nsr.replace(",", ", "));
                        jArray.put(srObject);
                    }
                }
                jo.put("stockDetails", jArray);

            }
            if (isExport) {
                jobj.put("data", jo);
                exportDAO.processRequest(request, response, jobj);
            }
            issuccess = true;
            msg = "Stock Detail list has been fetched successfully";

        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jo);
                jobj.put("count", 1);

            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());

    }
    
    public ModelAndView getStockDetailOfProduct(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        JSONObject jo = new JSONObject();
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {

            String companyId = sessionHandlerImpl.getCompanyid(request);
            String userId = sessionHandlerImpl.getUserid(request);
            String productBatchID="";
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();      
           KwlReturnObject cap1 = accountingHandlerDAO.getObject(ExtraCompanyPreferences.class.getName(), companyId);
           ExtraCompanyPreferences ecf = (ExtraCompanyPreferences) cap1.getEntityList().get(0);

            String exportFileName = request.getParameter("filename"); // for Export
            boolean isExport = false;
            if (!StringUtil.isNullOrEmpty(exportFileName)) {
                isExport = true;
            }

            boolean shippingdo=false;
            String isShippingDO=request.getParameter("isShippingDO");
            if(!StringUtil.isNullOrEmpty(isShippingDO)){
                shippingdo=Boolean.parseBoolean(isShippingDO);
            }
            
            boolean isEdit=false;
            String Edit=request.getParameter("isEdit");
            if(!StringUtil.isNullOrEmpty(Edit)){
                isEdit=Boolean.parseBoolean(Edit);
            }
            boolean linkflag=false;
            String link_flag=request.getParameter("linkflag");
            if(!StringUtil.isNullOrEmpty(link_flag)){
                linkflag=Boolean.parseBoolean(link_flag);
            }
            String productId = request.getParameter("productId");
            String documentid = request.getParameter("documentid");
            String ss = request.getParameter("ss");
            String storeId = request.getParameter("store");
            String locationId = request.getParameter("location");
            int moduleid = Integer.parseInt(request.getParameter("moduleid"));
            Product product = null;
            boolean isBatchForProduct = false;
            boolean isSerialForProduct = false;
            double quantity = Double.parseDouble(request.getParameter("quantity"));
            if (!StringUtil.isNullOrEmpty(productId)) {
                jeresult = accountingHandlerDAO.getObject(Product.class.getName(), productId);
                product = (Product) jeresult.getEntityList().get(0);
                isBatchForProduct = product.isIsBatchForProduct();
                isSerialForProduct = product.isIsSerialForProduct();
            }
            Set<Store> storeSet = null;
            if (!StringUtil.isNullOrEmpty(storeId)) {
                Store store = storeService.getStoreById(storeId);
                if (store != null) {
                    storeSet = new HashSet<>();
                    storeSet.add(store);
                }

            } else {
                boolean includeQAAndRepairStore = false;
                boolean includePickandPackStore = false;
                StoreType[] storetypes = null;
                //ERM-691 excluding scrap and repair stores from being displayed in other modules
                if (ecf.isActivateQAApprovalFlow()) {
                    storetypes = new StoreType[]{StoreType.HEADQUARTER, StoreType.RETAIL, StoreType.WAREHOUSE};
                }
                List<Store> stores = storeService.getStoresByStoreExecutivesAndManagers(user, true, storetypes, null, null,includeQAAndRepairStore,includePickandPackStore);
                if (!stores.isEmpty()) {
                    storeSet = new HashSet<>(stores);
                } else {
                    //If none of the store is assigned to current user, we will not send any store list to json object
                    throw new AccountingException("No store is assigned to current user.");
                }
            }
            Location location = null;
            if (!StringUtil.isNullOrEmpty(locationId)) {
                location = locationService.getLocation(locationId);
            }
            List<Stock> stockDetailList = stockService.getBatchSerialListByStoreProductLocation(product.getID(), storeSet, location,ss,isEdit);
            Map<String, NewProductBatch> batchMp = stockService.getBatchListByStoreProductLocation(product, storeSet, location,isEdit);
            Map<String, String> batchSerialMp = stockService.getSerialListByStoreProductLocation(product, storeSet, location);
            List<String> list=null;
            if(isEdit && !StringUtil.isNullOrEmpty(documentid)){
                 list=stockService.getbatchidFromDocumentId(documentid);
            }
            if (stockDetailList != null && !stockDetailList.isEmpty() && !shippingdo) {
                for (Stock stock : stockDetailList) {
                    if (stock != null) {
                        JSONObject srObject = new JSONObject();
                        
                        srObject.put("id", stock.getId());
                        srObject.put("productid", product.getID());
                        srObject.put("isBatchForProduct", product.isIsBatchForProduct());
                        srObject.put("isSerialForProduct", product.isIsSerialForProduct());
                        srObject.put("isRowForProduct", product.isIsrowforproduct());
                        srObject.put("isRackForProduct", product.isIsrackforproduct());
                        srObject.put("isBinForProduct", product.isIsbinforproduct());
                        srObject.put("warehouse", stock.getStore().getId());
                        srObject.put("packwarehouse",ecf.getPackingstore() );
                        srObject.put("storeName", stock.getStore().getFullName());
                        srObject.put("locationName", stock.getLocation().getName());
                        srObject.put("location", stock.getLocation().getId());
                        srObject.put("rowName", stock.getRow() != null ? stock.getRow().getName() : "");
                        srObject.put("rackName", stock.getRack() != null ? stock.getRack().getName() : "");
                        srObject.put("binName", stock.getBin() != null ? stock.getBin().getName() : "");
                        srObject.put("row", stock.getRow() != null ? stock.getRow().getId() : "");
                        srObject.put("rack", stock.getRack() != null ? stock.getRack().getId() : "");
                        srObject.put("bin", stock.getBin() != null ? stock.getBin().getId() : "");
                        srObject.put("avialblequantity", authHandler.roundQuantity(stock.getQuantity(), companyId));
                        if (!(isBatchForProduct || isSerialForProduct) && stock.getStore().getId().equals(product.getWarehouse().getId()) && stock.getLocation().getId().equals(product.getLocation().getId())) {
                            if (stock.getQuantity()>=quantity) {
                               // srObject.put("quantity", request.getParameter("quantity"));
                            }
                        } else {
                        srObject.put("quantity", "");
                        }
                        srObject.put("serialNames", (stock.getSerialNames() != null) ? stock.getSerialNames().replace(",", ", ") : "");
                        srObject.put("batchName", (stock.getBatchName() != null) ? StringUtil.DecodeText(stock.getBatchName()) : "");

                        String rowRackBin = (stock.getRow() != null ? stock.getRow().getId() : "") + (stock.getRack() != null ? stock.getRack().getId() : "") + (stock.getBin() != null ? stock.getBin().getId() : "");
                        String keyValue = product.getID() + stock.getStore().getId() + stock.getLocation().getId() + rowRackBin + stock.getBatchName();
                        NewProductBatch batches = batchMp.containsKey(keyValue) ? batchMp.get(keyValue) : null;
                        if(batches !=null){
                        srObject.put("avialblequantity", (batches.getQuantitydue()-batches.getLockquantity()));
                        }  
                        // Get available quantity by document id in edit case..
                        if(!StringUtil.isNullObject(batches) && !StringUtil.isNullOrEmpty(documentid) && (isEdit || linkflag) && (moduleid==Constants.Acc_Delivery_Order_ModuleId || moduleid==Constants.Acc_Cash_Sales_ModuleId || moduleid==Constants.Acc_Invoice_ModuleId)){
                            LocationBatchDocumentMapping locationBatchDocumentMapping = null;
                            ArrayList filter_names = new ArrayList();
                            ArrayList filter_params = new ArrayList();
                            filter_names.add("batchmapid.id");
                            filter_names.add("documentid");
                            filter_params.add(batches.getId());
                            filter_params.add(documentid);
                            filterRequestParams.put("filter_names", filter_names);
                            filterRequestParams.put("filter_params", filter_params);
                            KwlReturnObject locbatchdocresult = accMasterItemsDAOobj.getPRBatchQuantity(filterRequestParams);
                            if (!StringUtil.isNullObject(locbatchdocresult) && !locbatchdocresult.getEntityList().isEmpty()) {
                                locationBatchDocumentMapping = (LocationBatchDocumentMapping) locbatchdocresult.getEntityList().get(0);
                                srObject.put("avialblequantity", (batches.getQuantitydue()-batches.getLockquantity()) + locationBatchDocumentMapping.getQuantity());
                            }
                          }                        
                        if (batches != null && !product.isIsSerialForProduct()) {
                            srObject.put("blockedBatchName", batches.getLockquantity());
//                            srObject.put("quantity", stock.getQuantity() - batches.getLockquantity());
                            srObject.put("mfgdate", batches.getMfgdate() != null ? batches.getMfgdate() : "");
                            srObject.put("expdate", batches.getExpdate() != null ? batches.getExpdate() : "" );
                        }
                        if(product.isIsBatchForProduct()){
                             NewProductBatch batchObj = stockService.getERPProductBatch(product,stock.getStore() , stock.getLocation(), stock.getRow(), stock.getRack(), stock.getBin(), stock.getBatchName());
                             if (list!=null && isEdit && !(list.contains(batchObj.getId())) && stock.getQuantity() <= 0) {
                                continue;
                            }
                             srObject.put("purchasebatchid", batchObj != null ? batchObj.getId():"");
                             productBatchID=batchObj != null ? batchObj.getId():"";
                        }else{
                             NewProductBatch batchObj = stockService.getERPProductBatch(product,stock.getStore() , stock.getLocation(), stock.getRow(), stock.getRack(), stock.getBin(), "");
                             if (list!=null && isEdit && !(list.contains(batchObj.getId())) && stock.getQuantity() <= 0) {
                                continue;
                            }
                             srObject.put("purchasebatchid", batchObj != null ? batchObj.getId():"");
                             productBatchID=batchObj != null ? batchObj.getId():"";
                        }

                        HashMap<String, Object> hashMap = new HashMap<String, Object>();
                        hashMap.put("invoiceID", productBatchID);
                        hashMap.put(Constants.companyKey, companyId);
                        /**
                         * Get document count attached to batch
                         */
                        srObject.put("attachment", 0);
                        srObject.put("attachmentids", "");
                        KwlReturnObject object = accMasterItemsDAOobj.getBatchDocuments(hashMap);
                        if (object.getEntityList() != null && object.getEntityList().size() > 0) {
                            srObject.put("attachment", object.getEntityList().size());
                            List<Object[]> attachmentDetails = object.getEntityList();
                            String docids = "";
                            for (Object[] attachmentArray : attachmentDetails) {
                                docids = docids + attachmentArray[3] + ",";
                            }
                            if (!StringUtil.isNullOrEmpty(docids)) {
                                docids = docids.substring(0, docids.length() - 1);
                            }
                            srObject.put("attachmentids", docids);
                        }
                        String nsr = "";
                        if (product.isIsSerialForProduct() && !StringUtil.isNullOrEmpty(stock.getBatchName())) {
                            nsr = batchSerialMp.containsKey(stock.getBatchName() + stock.getLocation().getId()) ? batchSerialMp.get(stock.getBatchName() + stock.getLocation().getId()) : "";
                        } else {
                            nsr = batchSerialMp.containsKey(stock.getLocation().getId()) ? batchSerialMp.get(stock.getLocation().getId()) : "";
                        }
                        if (!StringUtil.isNullOrEmpty(nsr)) {
                            String[] serArr = nsr.split(",");
//                            srObject.put("quantity", stock.getQuantity() - serArr.length);
                            String avlser = (stock.getSerialNames() != null) ? stock.getSerialNames().replace(",", ", ") : "";
                            srObject.put("serialNames", getAvlSerials(nsr, avlser).replace(",", ", "));
                        }
                        srObject.put("blockedSerialNames", nsr.replace(",", ", "));
                        jArray.put(srObject);
                        
                    }
                }
//                jo.put("stockDetails", jArray);

            }
            if (isExport) {
                jobj.put("data", jo);
                exportDAO.processRequest(request, response, jobj);
            }
            issuccess = true;
            msg = "Stock Detail list has been fetched successfully";

        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data",jArray);
                jobj.put("count", jArray.length());

            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());

    }

    private String getAvlSerials(String nsr, String avlser) {
        String availableSer = "";
        if (!StringUtil.isNullOrEmpty(avlser)) {
            avlser = avlser.replace(" ", "");
            List<String> blkList = new ArrayList<String>(Arrays.asList(nsr.split(",")));
            List<String> avlList = new ArrayList<String>(Arrays.asList(avlser.split(",")));
            avlList.removeAll(blkList);
            for (String ser : avlList) {
                if (!StringUtil.isNullOrEmpty(availableSer)) {
                    availableSer = availableSer + "," + ser;
                } else {
                    availableSer = ser;
                }
            }
        }
        return availableSer;
    }

    public ModelAndView getDateWiseStockInventory(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
//        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
//        def.setName("IST_Tx");
//        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
//        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        JSONArray productCustomFieldInfo = new JSONArray();
        try {

            String companyId = sessionHandlerImpl.getCompanyid(request);
            String userId = sessionHandlerImpl.getUserid(request);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);
            Company company = user.getCompany();

            KwlReturnObject cap1 = accountingHandlerDAO.getObject(ExtraCompanyPreferences.class.getName(), companyId);
            ExtraCompanyPreferences ecf = (ExtraCompanyPreferences) cap1.getEntityList().get(0);

            String repairStore = ecf.getRepairStore();
            String qaStore = ecf.getInspectionStore();

            // Find out any product custom fields need to show in this report
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String customFieldQuery = "select customcolumninfo from showcustomcolumninreport where moduleid = ? and companyid = ?";
            List<String> customFieldinfoList = null;
            customFieldinfoList = accCommonTablesDAO.executeSQLQuery(customFieldQuery, new Object[]{Constants.Acc_Product_Master_ModuleId, companyId});

            HashMap<String, HashMap> productCustomData = new HashMap<String, HashMap>();
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = null;

            if (customFieldinfoList.size() > 0) {
                String jsonString = customFieldinfoList.get(0);
                JSONArray productCustomFields = new JSONArray(jsonString);
                String fieldIds = "";
                for (int jCnt = 0; jCnt < productCustomFields.length(); jCnt++) {
                    fieldIds = fieldIds.concat("'").concat(productCustomFields.getJSONObject(jCnt).getString("fieldid")).concat("',");
                }
                if (!StringUtil.isNullOrEmpty(fieldIds)) {
                    fieldIds = fieldIds.substring(0, fieldIds.length() - 1);
                }
                HashMap<String, Object> fieldrequestParams = new HashMap();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, "INid"));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyId, Constants.Acc_Product_Master_ModuleId, fieldIds));
                FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            }

            if (replaceFieldMap.size() > 0) {
                for (Map.Entry<String, String> varEntry : replaceFieldMap.entrySet()) {
                    JSONObject fieldInfo = new JSONObject();
                    fieldInfo.put("dataindex", varEntry.getKey());
                    fieldInfo.put("columnname", varEntry.getKey().replaceAll("Custom_", ""));
                    productCustomFieldInfo.put(fieldInfo);
                }
            }

            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            paging = new Paging(start, limit);
            String exportFileName = request.getParameter("filename"); // for Export
            boolean isExport = false;
            if (!StringUtil.isNullOrEmpty(exportFileName)) {
                isExport = true;
                paging = null;
            }
            String searchString = request.getParameter("ss");

            String ondate = request.getParameter("ondate");
            Date date = df.parse(ondate);
            String storeId = request.getParameter("store");
            String locationId = request.getParameter("location");
//            List<Store> stores = new ArrayList();
            Set<Store> storeSet = null;
            Store store = null;
            boolean singleStore = false;
            boolean isQAorRepairStore = false;
            if (!StringUtil.isNullOrEmpty(storeId)) {
                store = storeService.getStoreById(storeId);
                storeSet = new HashSet();
                storeSet.add(store);
                singleStore = true;
                if (storeId.equals(qaStore) || storeId.equals(repairStore)) {
                    isQAorRepairStore = true;
                }
            } else {
                boolean includeQAAndRepairStore = true;
                boolean includePickandPackStore = true;
                List<Store> stores = storeService.getStoresByStoreExecutivesAndManagers(user, null, null, null, null,includeQAAndRepairStore,includePickandPackStore);
                if (!stores.isEmpty()) {
                    storeSet = new HashSet<>(stores);
                }
//
//                for (Store s : stores) {
//                    Set<User> storeManagers = s.getStoreManagerSet();
//                    storeManagers.addAll(s.getStoreExecutiveSet());
//                    if (storeManagers.contains(user)) {
//                        storeSet.add(s);
//                    }
//                }
            }
            Map<Product, Double> stockList = null;
            Map<Product, List<Stock>> stockDetailMap = null;
            Location location = null;
            if (!StringUtil.isNullOrEmpty(locationId)) {
                location = locationService.getLocation(locationId);
            }
            if (storeSet != null && !storeSet.isEmpty()) {
                stockList = stockService.getDateWiseStockList(company, storeSet, location, date, searchString, paging);
//                stockDetailMap = stockService.getDateWiseStockDetailList(company, storeSet, location, date, searchString, null);
            }
            Map<String, Double> quantityUnderQA = stockService.getProductQuantityUnderQA(company, store);
            Map<String, Double> quantityUnderRepair = stockService.getProductQuantityUnderRepair(company, store);
            if (stockList != null && !stockList.isEmpty()) {
                for (Entry<Product, Double> productStock : stockList.entrySet()) {
                    Product product = productStock.getKey();
                    double qty = productStock.getValue();
                    JSONObject jObj = new JSONObject();

                    double qaQuantity = quantityUnderQA.containsKey(product.getID()) ? quantityUnderQA.get(product.getID()) : 0;
                    double repairQuantity = quantityUnderRepair.containsKey(product.getID()) ? quantityUnderRepair.get(product.getID()) : 0;
                    jObj.put("itemid", product.getID());
                    jObj.put("itemcode", product.getProductid());
                    jObj.put("partnumber", product.getCoilcraft());
                    jObj.put("itemdescription", product.getDescription());
                    jObj.put("itemname", product.getName());
//                    jObj.put("ccpartnumber", product.getCoilcraft());
                    jObj.put("uom", product.getUnitOfMeasure() == null ? "" : product.getUnitOfMeasure().getNameEmptyforNA());
//                    jObj.put("storecode", stock.getStore().getAbbreviation());
//                    jObj.put("storedescription", stock.getStore().getDescription());
                    double availqty = singleStore ? (isQAorRepairStore ? qty - (qaQuantity + repairQuantity) : qty) : qty - (qaQuantity + repairQuantity);
                    jObj.put("quantity", authHandler.formattedQuantity(availqty,companyId));
                    jObj.put("qaquantity", qaQuantity);
                    jObj.put("repairquantity", repairQuantity);
//                    jObj.put("batchPricePerUnit", stock.getPricePerUnit());
//                    jObj.put("batchNo", stock.getBatchNo());
                    jObj.put("batchQty", qty);
//                    jObj.put("isBatchForProduct", product.isIsBatchForProduct());
//                    jObj.put("isSerialForProduct", product.isIsSerialForProduct());
//                    jObj.put("isRowForProduct", product.isIsrowforproduct());
//                    jObj.put("isRackForProduct", product.isIsrackforproduct());
//                    jObj.put("isBinForProduct", product.isIsbinforproduct());
                    ItemReusability itemReusability = product.getItemReusability();
                    jObj.put("stockType", itemReusability != null ? itemReusability.toString() : "");
                    jObj.put("stockTypeName", itemReusability == ItemReusability.REUSABLE ? "R" : (itemReusability == ItemReusability.DISPOSABLE ? "C" : "C"));

//                if (store != null) {
//                    JSONArray stockDetails = new JSONArray();
//                    if (stockDetailMap != null && !stockDetailMap.isEmpty() && stockDetailMap.containsKey(product)) {
//                        List<Stock> stockDetailList = stockDetailMap.get(product);
//                        for (Stock stockDetail : stockDetailList) {
//                            if (stockDetail != null && stockDetail.getQuantity() != 0) {
//                                JSONObject srObject = new JSONObject();
////                                srObject.put("id", stockDetail.getId());
//                                srObject.put("storeName", stockDetail.getStore().getFullName());
//                                srObject.put("locationName", stockDetail.getLocation().getName());
//                                srObject.put("rowName", stockDetail.getRow() != null ? stockDetail.getRow().getName() : "");
//                                srObject.put("rackName", stockDetail.getRack() != null ? stockDetail.getRack().getName() : "");
//                                srObject.put("binName", stockDetail.getBin() != null ? stockDetail.getBin().getName(): "");
//                                srObject.put("quantity", stockDetail.getQuantity());
//                                srObject.put("serialNames", (stockDetail.getSerialNames() != null) ? stockDetail.getSerialNames().replace(",", ", ") : "");
//                                srObject.put("batchName", (stockDetail.getBatchName() != null) ? stockDetail.getBatchName() : "");
//                                stockDetails.put(srObject);
//                            }
//
//                        }
//                    }
//                    jObj.put("stockDetails", stockDetails);
//                }
                    // Add Product Level Custom Fiels 
                    if (FieldMap != null) {
                        if (productCustomData.containsKey(product.getID())) {
                            HashMap<String, String> prodDataArray = productCustomData.get(product.getID());
                            for (Map.Entry<String, String> varEntry : prodDataArray.entrySet()) {
                                jObj.put(varEntry.getKey(), varEntry.getValue());
                            }
                        } else {
                            AccProductCustomData obj1 = (AccProductCustomData) kwlCommonTablesDAOObj.getClassObject(AccProductCustomData.class.getName(), product.getID());
                            if (obj1 != null) {
                                HashMap<String, String> prodDataArray = new HashMap<String, String>();
                                HashMap<String, Object> variableMap = new HashMap<String, Object>();
                                AccountingManager.setCustomColumnValues(obj1, FieldMap, replaceFieldMap, variableMap);
                                DateFormat defaultDateFormat=new SimpleDateFormat(Constants.MMMMdyyyy);
                                Date dateFromDB=null;
                                for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                                    String coldata = varEntry.getValue().toString();
                                    if (customFieldMap.containsKey(varEntry.getKey())) {
                                        boolean isCustomExport = true;
                                        String value = "";
                                        String Ids[] = coldata.split(",");
                                        for (int i = 0; i < Ids.length; i++) {
                                            FieldComboData fieldComboData = (FieldComboData) kwlCommonTablesDAOObj.getClassObject(FieldComboData.class.getName(), Ids[i]);
                                            if (fieldComboData != null) {
                                                if (fieldComboData.getField().getFieldtype() == 12 && !isCustomExport) {
                                                    value += Ids[i] != null ? Ids[i] + "," : ",";
                                                } else {
                                                    value += fieldComboData.getValue() != null ? fieldComboData.getValue() + "," : ",";
                                                }
                                            }
                                        }
                                        if (!StringUtil.isNullOrEmpty(value)) {
                                            value = value.substring(0, value.length() - 1);
                                        }
                                        prodDataArray.put(varEntry.getKey(), value);
                                        jObj.put(varEntry.getKey(), value);
                                    } else if (customDateFieldMap.containsKey(varEntry.getKey())) {
                                        try {
                                            dateFromDB = defaultDateFormat.parse(coldata);
                                            coldata = df.format(dateFromDB);
                                        } catch (Exception e) {
                                        }
                                        jObj.put(varEntry.getKey(),coldata);
                                        prodDataArray.put(varEntry.getKey(),coldata);
                                    } else {
                                        if (!StringUtil.isNullOrEmpty(coldata)) {
                                            jObj.put(varEntry.getKey(), coldata);
                                            prodDataArray.put(varEntry.getKey(), coldata);
                                        }
                                    }
                                }
                                productCustomData.put(product.getID(), prodDataArray);
                            }
                        }
                    }
                    jArray.put(jObj);
                }
            }
            if (isExport) {
                jobj.put("data", jArray);
                jobj.put("productcustomfield", productCustomFieldInfo);
                exportDAO.processRequest(request, response, jobj);
            }
            issuccess = true;
            msg = "Stock list has been fetched successfully";

//            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
//            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
//            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
//            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArray);
                if (paging != null) {
                    jobj.put("count", paging.getTotalRecord());
                } else {
                    jobj.put("count", jArray.length());
                }
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());

    }

    public ModelAndView getStockDetailByDateForProduct(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        JSONObject jo = new JSONObject();
        Paging paging = null;
        int totalCount = 0;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {

            String companyId = sessionHandlerImpl.getCompanyid(request);
            String userId = sessionHandlerImpl.getUserid(request);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            String exportFileName = request.getParameter("filename"); // for Export
            boolean isExport = false;
            if (!StringUtil.isNullOrEmpty(exportFileName)) {
                isExport = true;
            }

            String productId = request.getParameter("productId");
            String storeId = request.getParameter("store");
            String locationId = request.getParameter("location");
            String searchString = request.getParameter("ss");
            String ondate = request.getParameter("ondate");
            Date date = df.parse(ondate);

            Product product = null;
            if (!StringUtil.isNullOrEmpty(productId)) {
                jeresult = accountingHandlerDAO.getObject(Product.class.getName(), productId);
                product = (Product) jeresult.getEntityList().get(0);
            }
            Set<Store> storeSet = null;
            if (!StringUtil.isNullOrEmpty(storeId)) {
                Store store = storeService.getStoreById(storeId);
                if (store != null) {
                    storeSet = new HashSet<>();
                    storeSet.add(store);
                }

            } else {
                boolean includeQAAndRepairStore = true;
                boolean includePickandPackStore = true;
                List<Store> stores = storeService.getStoresByStoreExecutivesAndManagers(user, true, null, null, null,includeQAAndRepairStore,includePickandPackStore);
                if (!stores.isEmpty()) {
                    storeSet = new HashSet<>(stores);
                }
            }
            Location location = null;
            if (!StringUtil.isNullOrEmpty(locationId)) {
                location = locationService.getLocation(locationId);
            }
            List<Object[]> stockDetailList = stockService.getDateWiseStockDetailListForProduct(product, storeSet, location, date, searchString, null);
            if (stockDetailList != null && !stockDetailList.isEmpty()) {

                jo.put("isBatchForProduct", product.isIsBatchForProduct());
                jo.put("isSerialForProduct", product.isIsSerialForProduct());
                jo.put("isRowForProduct", product.isIsrowforproduct());
                jo.put("isRackForProduct", product.isIsrackforproduct());
                jo.put("isBinForProduct", product.isIsbinforproduct());
                JSONArray jArray = new JSONArray();
                for (Object[] stock : stockDetailList) {
                    if (stock != null) {
                        JSONObject srObject = new JSONObject();

                        srObject.put("storeName", stock[9]);//stock.getStore().getFullName());
                        srObject.put("locationName", stock[2]);//stock.getLocation().getName());
                        srObject.put("rowName", stock[3]);//stock.getRow() != null ? stock.getRow().getName() : "");
                        srObject.put("rackName", stock[4]);//stock.getRack() != null ? stock.getRack().getName() : "");
                        srObject.put("binName", stock[5]);//stock.getBin() != null ? stock.getBin().getName() : "");
                        srObject.put("quantity", stock[8]);//stock.getQuantity());
                        srObject.put("serialNames", stock[7]);//(stock.getSerialNames() != null) ? stock.getSerialNames().replace(",", ", ") : "");
                        srObject.put("batchName", stock[6]);//(stock.getBatchName() != null) ? stock.getBatchName() : "");
                        jArray.put(srObject);
                    }
                }
                jo.put("stockDetails", jArray);
                totalCount = jArray.length();
            }
            if (isExport) {
                jobj.put("data", jo);
                exportDAO.processRequest(request, response, jobj);
            }
            issuccess = true;
            msg = "Stock Detail list has been fetched successfully";

        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jo);
                jobj.put("count", totalCount);

            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());

    }

    public ModelAndView getProductwiseSerialList(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IST_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {

            String companyId = sessionHandlerImpl.getCompanyid(request);
            String userId = sessionHandlerImpl.getUserid(request);

//            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
//            Company company = (Company) jeresult.getEntityList().get(0);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            paging = new Paging(start, limit);

            String searchString = request.getParameter("ss");
            String productid = request.getParameter("itemid");

            String storeId = request.getParameter("store");
            String locationId = request.getParameter("location");
            List<Store> stores = new ArrayList();
            Set<Store> storeSet = new HashSet();
            Store store = null;
            if (!StringUtil.isNullOrEmpty(storeId)) {
                store = storeService.getStoreById(storeId);
                storeSet.add(store);
            } else {
                stores = storeService.getStores(user.getCompany(), null, null);
                for (Store s : stores) {
                    Set<User> storeManagers = s.getStoreManagerSet();
                    if (storeManagers.contains(user)) {
                        storeSet.add(s);
                    }
                }
            }

//            Location location = null;
//            if (!StringUtil.isNullOrEmpty(locationId)) {
//                location = locationService.getLocation(locationId);
//            }
            List<NewBatchSerial> batchSerialsList = stockService.getProductSerialList(productid, companyId, storeSet, locationId, storeId, searchString, paging);   //storeSet, location, searchString, paging

            for (NewBatchSerial batchSerial : batchSerialsList) {
                JSONObject jObj = new JSONObject();
//                JSONArray stockDetails = new JSONArray();
                jObj.put("serialid", batchSerial.getId());
                jObj.put("itemid", batchSerial.getProduct());
                jObj.put("quantity", batchSerial.getQuantity());
                jObj.put("quantitydue", batchSerial.getQuantitydue());
                jObj.put("ispurchase", batchSerial.isIspurchase());
                jObj.put("transactiontype", batchSerial.getTransactiontype());
                jObj.put("lockquantity", batchSerial.getLockquantity());
                jObj.put("isForconsignment", batchSerial.isIsForconsignment());
                jObj.put("serialname", batchSerial.getSerialname());
                jObj.put("isserialupdate", false);//Sent Default false
                NewProductBatch productBatch = batchSerial.getBatch();
                if (batchSerial.getBatch() != null) {
                    jObj.put("batchid", productBatch.getId());
                    jObj.put("batchname", productBatch.getBatchname());
                    jObj.put("location", productBatch.getLocation() != null ? productBatch.getLocation().getId() : "");
                    jObj.put("row", productBatch.getRow() != null ? productBatch.getRow().getId() : "");
                    jObj.put("rack", productBatch.getRack() != null ? productBatch.getRack().getId() : "");
                    jObj.put("bin", productBatch.getBin() != null ? productBatch.getBin().getId() : "");
                    jObj.put("warehouse", productBatch.getWarehouse() != null ? productBatch.getWarehouse().getId() : "");
                }

                jArray.put(jObj);
            }
            issuccess = true;
            msg = "Stock list has been fetched successfully";

            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArray);
                if (paging != null) {
                    jobj.put("count", paging.getTotalRecord());
                } else {
                    jobj.put("count", jArray.length());
                }
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());

    }

    public ModelAndView updateSerialNames(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            msg = UpdateSerialNames(request);
            issuccess = true;
            txnManager.commit(status);
            //msg = "Serials updated successfully.";
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(StockController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(StockController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public String UpdateSerialNames(HttpServletRequest request) {
        String msg = "Serials updated successfully";
        try {
            JSONArray jArr = new JSONArray(request.getParameter("detailsArr"));
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                String itemid = jobj.getString("itemid");
                boolean isSerialUpdate = false;
                isSerialUpdate = Boolean.parseBoolean(jobj.optString("isserialupdate"));
                String oldSerialName = !StringUtil.isNullOrEmpty(jobj.getString("serialname")) ? StringUtil.decodeString(jobj.getString("serialname")) : "";
                String batchName = jobj.getString("batchname");
                String newSerialName = !StringUtil.isNullOrEmpty(jobj.getString("newserialname")) ? StringUtil.decodeString(jobj.getString("newserialname")) : "";
                String serialid = jobj.getString("serialid");
                if (isSerialUpdate == true) {
                    KwlReturnObject jeresult = accountingHandlerDAO.getObject(Product.class.getName(), itemid);
                    Product product = (Product) jeresult.getEntityList().get(0);

                    jeresult = accountingHandlerDAO.getObject(NewBatchSerial.class.getName(), serialid);
                    NewBatchSerial newBatchSerial = (NewBatchSerial) jeresult.getEntityList().get(0);
                    if (newBatchSerial != null && newBatchSerial.getQaApprovalstatus() != QaApprovalStatus.REJECTED) {
                        newBatchSerial.setSerialname(newSerialName);
                        stockService.updateObject(newBatchSerial);
                        stockService.renameSerialInStock(product, batchName, oldSerialName, newSerialName);
                        auditTrailObj.insertAuditLog(AuditAction.SERIAL_CHANGED, "User " + sessionHandlerImpl.getUserFullName(request) + "  has changed the Serial from ("
                                + oldSerialName + " to " + newSerialName + " ) of " + product.getProductid(), request, serialid);
                        msg = "Serials updated successfully.";
                    } else if (newBatchSerial.getQaApprovalstatus() == QaApprovalStatus.REJECTED) {
                        throw new AccountingException("You can't rename the QA Rejected Serials.");
                    }

                }
            }
        } catch (Exception ex) {
            msg = ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(StockController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return msg;
    }

    public ModelAndView getStockByStoreProduct(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IST_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {

            String companyId = sessionHandlerImpl.getCompanyid(request);
            String productId = request.getParameter("productId");

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);

            Product product = null;
            if (!StringUtil.isNullOrEmpty(productId)) {
                jeresult = accountingHandlerDAO.getObject(Product.class.getName(), productId);
                product = (Product) jeresult.getEntityList().get(0);
            }

            String storeId = request.getParameter("toStoreId");
            Store store = null;
            if (!StringUtil.isNullOrEmpty(storeId)) {
                store = storeService.getStoreById(storeId);
            }

            List<Stock> stockList = stockService.getStockByStoreProduct(product, store);

            Iterator itr = stockList.iterator();
            while (itr.hasNext()) {
                Stock stock = (Stock) itr.next();
                JSONObject jObj = new JSONObject();
                jObj.put("productId", stock.getProduct().getProductid());
                jObj.put("locationId", stock.getLocation().getId());
                jObj.put("locationName", stock.getLocation().getName());
                StoreMaster row = stock.getRow();
                StoreMaster rack = stock.getRack();
                StoreMaster bin = stock.getBin();
                if (row != null) {
                    jObj.put("rowId", row.getId());
                    jObj.put("rowName", row.getName());
                }
                if (rack != null) {
                    jObj.put("rackId", rack.getId());
                    jObj.put("rackName", rack.getName());
                }
                if (bin != null) {
                    jObj.put("binId", bin.getId());
                    jObj.put("binName", bin.getName());
                }
                jObj.put("availableQty", stock.getQuantity());
                jArray.put(jObj);
            }

            issuccess = true;
            msg = "store product wise stock list has been fetched successfully";

            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArray);
                if (paging != null) {
                    jobj.put("count", paging.getTotalRecord());
                } else {
                    jobj.put("count", jArray.length());
                }
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());

    }

    public ModelAndView getAvailableQtyByStoreProduct(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IST_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {

            String companyId = sessionHandlerImpl.getCompanyid(request);
            String productId = request.getParameter("productId");
            String storeId = request.getParameter("fromStoreId");
            double availableQty = 0;

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);

            Product product = null;
            if (!StringUtil.isNullOrEmpty(productId)) {
                jeresult = accountingHandlerDAO.getObject(Product.class.getName(), productId);
                product = (Product) jeresult.getEntityList().get(0);
            }

            Store store = null;
            if (!StringUtil.isNullOrEmpty(storeId)) {
                store = storeService.getStoreById(storeId);
            }

            availableQty = stockService.getProductQuantityInStore(product, store);

            JSONObject jObj = new JSONObject();
            jObj.put("availableQty", availableQty);
            jArray.put(jObj);

            issuccess = true;
            msg = "product's available quantity in store has been fetched successfully";

            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArray);
                if (paging != null) {
                    jobj.put("count", paging.getTotalRecord());
                } else {
                    jobj.put("count", jArray.length());
                }
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());

    }

    public ModelAndView getAvailableQtyByStoreLocation(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IST_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        boolean isUsed = true;
        String storeNames = "";
        try {

            String companyId = sessionHandlerImpl.getCompanyid(request);
            String locationId = request.getParameter("locationid");
            String storeId = request.getParameter("storeid");
            String storeIds = "";
            String storeIdArr[] = null;
            if (!StringUtil.isNullOrEmpty(storeId)) {
                storeIds = storeId.substring(0, storeId.length() - 1);
            }
            if (!StringUtil.isNullOrEmpty(storeIds)) {
                storeIdArr = storeIds.split(",");
            }
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);

            Store store = null;
            Set<Store> storeSet = new HashSet();
            if (storeIdArr != null && storeIdArr.length > 0) {
                for (String sId : storeIdArr) {
                    if (!StringUtil.isNullOrEmpty(sId)) {
                        store = storeService.getStoreById(sId);
                        storeSet.add(store);
                    }
                }
            }
            Location location = null;
            if (!StringUtil.isNullOrEmpty(locationId)) {
                location = locationService.getLocation(locationId);
            }
            List<Stock> stocklList = null;
            if (storeSet != null && !storeSet.isEmpty() && location != null) {
                stocklList = stockService.getStoreWiseStockList(company, storeSet, location, null, null, null);
            }
            if (stocklList != null && !stocklList.isEmpty()) {
                boolean isFirst = true;
                isUsed = true;
                for (Stock ist : stocklList) {
                    if (isFirst) {
                        storeNames = ist.getStore().getFullName();
                        isFirst = false;
                    } else if (!storeNames.contains(ist.getStore().getFullName())) {
                        storeNames += " ," + ist.getStore().getFullName();
                    }

                }
            } else {
                isUsed = false;
            }

            JSONObject jObj = new JSONObject();
            jObj.put("isUsed", isUsed);
            jObj.put("storena", isUsed);
            jArray.put(jObj);

            issuccess = true;
            if (!StringUtil.isNullOrEmpty(storeNames)) {
                msg = "Some stock is available in the selected location for store (" + storeNames + "), so you cannot remove it.";
            }

            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArray);
                if (paging != null) {
                    jobj.put("count", paging.getTotalRecord());
                } else {
                    jobj.put("count", jArray.length());
                }
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());

    }

    public ModelAndView getStoreProductWiseLocationBatchList(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IST_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {

            String companyId = sessionHandlerImpl.getCompanyid(request);
            String productId = request.getParameter("productId");
            boolean checkQAReject = false;
            if ("true".equals(request.getParameter("checkQAReject"))) {
                checkQAReject = true;
            }
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);

            Product product = null;
            if (!StringUtil.isNullOrEmpty(productId)) {
                jeresult = accountingHandlerDAO.getObject(Product.class.getName(), productId);
                product = (Product) jeresult.getEntityList().get(0);
            }

            String storeId = request.getParameter("toStoreId");
            Store store = null;
            if (!StringUtil.isNullOrEmpty(storeId)) {
                store = storeService.getStoreById(storeId);
            }

            List<NewProductBatch> stockList = stockService.getERPActiveBatchList(product, store);

            for (NewProductBatch batch : stockList) {
                JSONObject jObj = new JSONObject();
                jObj.put("batchId", batch.getId());
                jObj.put("batchName", batch.getBatchname());
                jObj.put("locationId", batch.getLocation().getId());
                jObj.put("locationName", batch.getLocation().getName());
                StoreMaster row = batch.getRow();
                StoreMaster rack = batch.getRack();
                StoreMaster bin = batch.getBin();

                if (row != null) {
                    jObj.put("rowId", row.getId());
                    jObj.put("rowName", row.getName());
                }
                if (rack != null) {
                    jObj.put("rackId", rack.getId());
                    jObj.put("rackName", rack.getName());
                }
                if (bin != null) {
                    jObj.put("binId", bin.getId());
                    jObj.put("binName", bin.getName());
                }
                jObj.put("productId", product.getID());
                boolean addData = true;
                if (product.isIsSerialForProduct()) {
                    List serials = stockService.getERPActiveSerialList(product, batch, checkQAReject);
                    jObj.put("availableQty", serials.size());
                    if (serials.isEmpty()) {
                        addData = false;
                    }
                } else {
                    jObj.put("availableQty", batch.getQuantitydue());
                }

                if (addData) { // if available qty is greater than 0
                    jArray.put(jObj);
                }
            }

            issuccess = true;
            msg = "store product wise location batch list has been fetched successfully";

            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArray);
                if (paging != null) {
                    jobj.put("count", paging.getTotalRecord());
                } else {
                    jobj.put("count", jArray.length());
                }
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());

    }

    public ModelAndView getStoreProductWiseDetailList(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IST_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;

        try {

            String companyId = sessionHandlerImpl.getCompanyid(request);
            String productId = request.getParameter("productId");
            String defaultloc = request.getParameter("defaultloc");
            boolean checkQAReject = false;
//            if ("true".equals(request.getParameter("checkQAReject"))) {
//                checkQAReject = false;
//            }
            KwlReturnObject cap1 = accountingHandlerDAO.getObject(ExtraCompanyPreferences.class.getName(), companyId);
            ExtraCompanyPreferences ecp = (ExtraCompanyPreferences) cap1.getEntityList().get(0);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);
            Store qaStore = stockService.getQaStore(company);
            Product product = null;
            if (!StringUtil.isNullOrEmpty(productId)) {
                jeresult = accountingHandlerDAO.getObject(Product.class.getName(), productId);
                product = (Product) jeresult.getEntityList().get(0);
            }

            String storeId = request.getParameter("storeId");
            Store store = null;
            Location location = null;
            if (!StringUtil.isNullOrEmpty(storeId)) {
                store = storeService.getStoreById(storeId);
                if ("true".equals(defaultloc)) {
                    location = store.getDefaultLocation();
                }
            }
            Map<String, Stock> pendingforApprovalStockMap = stockService.getPendingApprovalStock(product, store);

            List<NewProductBatch> productBatchList = location == null ? stockService.getERPActiveBatchList(product, store) : stockService.getERPActiveBatchList(product, store, location);

            for (NewProductBatch productBatch : productBatchList) {
                JSONObject jObj = new JSONObject();
                jObj.put("fromStoreId", productBatch.getWarehouse().getId());
                jObj.put("batchName", productBatch.getBatchname());
                jObj.put("purchasebatchid", productBatch.getId());
                jObj.put("fromLocationId", productBatch.getLocation().getId());
                jObj.put("warehouse", productBatch.getWarehouse() != null ? productBatch.getWarehouse().getId():"");
                jObj.put("location", productBatch.getLocation() != null ? productBatch.getLocation().getId():"");
                jObj.put("fromLocationName", productBatch.getLocation().getName());
                StoreMaster row = productBatch.getRow();
                StoreMaster rack = productBatch.getRack();
                StoreMaster bin = productBatch.getBin();
                if (row != null) {
                    jObj.put("fromRowId", row.getId());
                    jObj.put("fromRowName", row.getName());
                }
                if (rack != null) {
                    jObj.put("fromRackId", rack.getId());
                    jObj.put("fromRackName", rack.getName());
                }
                if (bin != null) {
                    jObj.put("fromBinId", bin.getId());
                    jObj.put("fromBinName", bin.getName());
                }

                boolean addRecord = true;

                Location fromLocation = null;
                jeresult = accountingHandlerDAO.getObject(Location.class.getName(), productBatch.getLocation().getId());
                fromLocation = (Location) jeresult.getEntityList().get(0);

                String locId = productBatch.getLocation() != null ? productBatch.getLocation().getId() : "";
                String rowID = productBatch.getRow() != null ? productBatch.getRow().getId() : "";
                String rackID = productBatch.getRack() != null ? productBatch.getRack().getId() : "";
                String binID = productBatch.getBin() != null ? productBatch.getBin().getId() : "";
                String batchID = productBatch.getBatchname() != null ? productBatch.getBatchname() : "";

                String key = product.getID() + storeId + locId + rowID + rackID + binID + batchID;

                Stock pendingApprovalStock = pendingforApprovalStockMap.get(key);
                if (pendingApprovalStock == null) {
                    pendingApprovalStock = new Stock();
                }
                if (product.isIsSerialForProduct()) {
                    StringBuilder serials = new StringBuilder();
                    StringBuilder sku = new StringBuilder();
                    List<NewBatchSerial> serialList = stockService.getERPActiveSerialList(product, productBatch, checkQAReject);
                    int serialCount = 0;
                    for (NewBatchSerial batchSerial : serialList) {
                        if (!pendingApprovalStock.getSerialNames().contains(batchSerial.getSerialname())) {
                            if (serials.length() == 0) {
                                serials.append(batchSerial.getSerialname());
                                if (ecp.isSKUFieldParm()) {
                                    sku.append(batchSerial.getSkufield() == null ? "" : batchSerial.getSkufield());
                                }
                            } else {
                                serials.append(",").append(batchSerial.getSerialname());
                                if (ecp.isSKUFieldParm()) {
                                    sku.append(",").append(batchSerial.getSkufield() == null ? "" : batchSerial.getSkufield());
                                }
                            }
                            serialCount++;
                        }
                    }
                    jObj.put("availableSerials", serials.toString());
                    jObj.put("availableSKUs", sku.toString());
                    jObj.put("availableQty", serialCount);

                    if (serialCount == 0) {
                        addRecord = false;
                    }
                } else if (!product.isIsSerialForProduct() && product.isIsBatchForProduct()) {
                    jObj.put("availableSerials", "");
                    jObj.put("availableSKUs", "");
                    if (qaStore != null && qaStore == store) {
                        jObj.put("availableQty", authHandler.roundQuantity(productBatch.getQuantitydue() - (pendingApprovalStock.getQuantity() + productBatch.getLockquantity()),companyId));
                    } else {
                        jObj.put("availableQty", authHandler.roundQuantity(productBatch.getQuantitydue() - (productBatch.getLockquantity()), companyId));
                    }
                } else if (!product.isIsBatchForProduct() && !product.isIsSerialForProduct()) {
                    jObj.put("availableQty", authHandler.roundQuantity(productBatch.getQuantitydue() - (productBatch.getLockquantity()),companyId));
                    jObj.put("defaultlocqty", authHandler.roundQuantity(productBatch.getQuantitydue() - (productBatch.getLockquantity()),companyId));
                    jObj.put("deflocation", location != null ? location.getId() : "");
                } else {
                    jObj.put("availableSerials", "");
                    jObj.put("availableSKUs", "");
                    jObj.put("availableQty", authHandler.roundQuantity((stockService.getProductTotalAvailableQuantity(product, store, fromLocation) - pendingApprovalStock.getQuantity()),companyId));
//                    jObj.put("availableQty",productBatch.getQuantitydue() - (productBatch.getLockquantity()));
                }
                if (addRecord) {
                    jArray.put(jObj);
                }

            }
            if ((StringUtil.isNullObject(jArray) || jArray.length() == 0) && "true".equals(defaultloc)) {
                JSONObject job = new JSONObject();
                job.put("defaultlocqty", 0);
                job.put("deflocation", location != null ? location.getId() : "");
                jArray.put(job);
            }

            issuccess = true;
            msg = "store product wise location batch list has been fetched successfully";

            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArray);
                if (paging != null) {
                    jobj.put("count", paging.getTotalRecord());
                } else {
                    jobj.put("count", jArray.length());
                }
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());

    }
    
    public ModelAndView getStoreProductWiseAllAvailableStockDetailList(HttpServletRequest request, HttpServletResponse response) throws JSONException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        jobj = stockService.getStoreProductWiseAllAvailableStockDetailList(paramJobj);
        return new ModelAndView(successView, "model", jobj.toString());
    }
    
    public ModelAndView getProductBatchWiseSerialList(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IST_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {

            String companyId = sessionHandlerImpl.getCompanyid(request);
            String productId = request.getParameter("productid");
            String storeId = request.getParameter("storeid");
            String locationId = request.getParameter("locationid");
            String batch = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("batch"))) {
                batch = request.getParameter("batch");
            }
            boolean checkQAReject = false;
            if ("true".equals(request.getParameter("checkQAReject"))) {
                checkQAReject = true;
            }

            Product product = null;
            if (!StringUtil.isNullOrEmpty(productId)) {
                KwlReturnObject jeresult = accountingHandlerDAO.getObject(Product.class.getName(), productId);
                product = (Product) jeresult.getEntityList().get(0);
            }
            Store store = null;
            if (!StringUtil.isNullOrEmpty(storeId)) {
                store = storeService.getStoreById(storeId);
            }
            Location location = null;
            if (!StringUtil.isNullOrEmpty(locationId)) {
                location = locationService.getLocation(locationId);
            }

            List<NewBatchSerial> serialList = stockService.getERPActiveSerialList(product, store, location, batch, checkQAReject);

            for (NewBatchSerial serial : serialList) {
                JSONObject jObj = new JSONObject();
                jObj.put("serial", serial.getSerialname());
                jArray.put(jObj);
            }

            issuccess = true;
            msg = "product batch wise serial list has been fetched successfully";

            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArray);
                if (paging != null) {
                    jobj.put("count", paging.getTotalRecord());
                } else {
                    jobj.put("count", jArray.length());
                }
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());

    }

    public ModelAndView isSerialExists(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IST_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONObject jObj = new JSONObject();
        Paging paging = null;
        try {

            String productId = request.getParameter("productid");
            String storeId = request.getParameter("storeid");
            String locationId = request.getParameter("locationid");
            String batchName = request.getParameter("batch");
            String serialName = request.getParameter("serial");
            String module = request.getParameter("module");
            boolean isSerialPresent = false;
            boolean isSA = !StringUtil.isNullOrEmpty(request.getParameter("isSA")) ? Boolean.parseBoolean(request.getParameter("isSA")) : false;
            KwlReturnObject jeresult = null;

            Product product = null;
            if (!StringUtil.isNullOrEmpty(productId)) {
                jeresult = accountingHandlerDAO.getObject(Product.class.getName(), productId);
                product = (Product) jeresult.getEntityList().get(0);
            }
            Store store = storeService.getStoreById(storeId);
            Location location = locationService.getLocation(locationId);
            isSerialPresent = stockService.isSerialExists(product, store, location, batchName, serialName, module,null);
            if(!StringUtil.isNullOrEmpty(request.getParameter("isEdit")) && Boolean.parseBoolean(request.getParameter("isEdit")) && !StringUtil.isNullOrEmpty(request.getParameter("documentid")) && isSerialPresent){
            isSerialPresent = stockService.isSerialExists(product, store, location, batchName, serialName, module, request.getParameter("documentid"));     // edit case for sales return.
            }
            
            List srrpList = stockService.getStockForPendingRepairSerial(productId, batchName, serialName);
            List srqaList = stockService.getStockForPendingApprovalSerial(productId, batchName, serialName);
            if (!isSerialPresent && (srrpList.size() > 0||srqaList.size()>0)) {
                isSerialPresent = true;
            }
            if (!isSerialPresent && isSA) {
//                isSerialPresent = stockService.isSerialExistsinDO(product, batchName, serialName);
            }
            jObj.put("isSerialPresent", isSerialPresent);

            issuccess = true;
            msg = "Serial Exists or not has been checked successfully";

            txnManager.commit(status);

        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jObj);
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());

    }
    public ModelAndView isBatchExists(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IST_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONObject jObj = new JSONObject();
        Paging paging = null;
        try {
            String productId = request.getParameter("productid");
            String batchName = request.getParameter("batch");
            String isEdit = request.getParameter("isEdit");
            String documentId = request.getParameter("documentid");
            String companyId = sessionHandlerImpl.getCompanyid(request);
            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("batchname", batchName);
            boolean isBatchPresent = false;
            KwlReturnObject jeresult = null;

            requestParams.put("company", companyId);
            requestParams.put("documentid", documentId);
            requestParams.put("isEdit", isEdit);
            requestParams.put("moduleid", request.getParameter("moduleid"));
            requestParams.put("productid", productId);


            isBatchPresent = productDAOObj.IsBatchUsedInOutTransaction(requestParams);

            jObj.put("isBatchPresent", isBatchPresent);

            issuccess = true;
            msg = "Batch Exists or not has been checked successfully";

            txnManager.commit(status);

        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jObj);
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());

    }

    public ModelAndView isSKUExists(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IST_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONObject jObj = new JSONObject();
        Paging paging = null;
        try {

            String productId = request.getParameter("productid");
            String batchName = request.getParameter("batch");
            String skuName = request.getParameter("sku");
            boolean isSKUPresent = false;
            KwlReturnObject jeresult = null;

            Product product = null;
            if (!StringUtil.isNullOrEmpty(productId)) {
                jeresult = accountingHandlerDAO.getObject(Product.class.getName(), productId);
                product = (Product) jeresult.getEntityList().get(0);
            }
            isSKUPresent = stockService.isSKUExists(product, batchName, skuName);
//            if(!isSKUPresent){
//                isSKUPresent = stockService.isSKUExists(product, batchName, skuName);
//            }

            jObj.put("isSKUPresent", isSKUPresent);

            issuccess = true;
            msg = "SKU Exists or not has been checked successfully";

            txnManager.commit(status);

        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jObj);
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());

    }

    public ModelAndView isAnyStockLiesInStore(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IST_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONObject jObj = new JSONObject();
        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String storeId = request.getParameter("storeid");
            boolean isQAinspection = !StringUtil.isNullOrEmpty(request.getParameter("isQainspectionFlow")) ? Boolean.parseBoolean(request.getParameter("isQainspectionFlow")) : false;
            boolean isQA = !StringUtil.isNullOrEmpty(request.getParameter("isQA")) ? Boolean.parseBoolean(request.getParameter("isQA")) : false;
            boolean isRepair = !StringUtil.isNullOrEmpty(request.getParameter("isRepair")) ? Boolean.parseBoolean(request.getParameter("isRepair")) : false;
            boolean isStockPresent = false;
            KwlReturnObject jeresult = null;
            jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);
            Store store = null;
            Set<Store> storeSet = new HashSet<>();
            if (!StringUtil.isNullOrEmpty(storeId)) {
                store = storeService.getStoreById(storeId);
                storeSet.add(store);

            }
            if (isQA) {
                isStockPresent = stockService.isAnyStockLiesInStore(company, isQA);               // true for QA ,false for repair
            } else if (isRepair) {
                isStockPresent = stockService.isAnyStockLiesInStore(company, !isRepair);               // true for QA ,false for repair
            } else {
                isStockPresent = stockService.isAnyStockLiesInStore(company, null);
            }
            jObj.put("isStockPresent", isStockPresent);

            issuccess = true;
            msg = "Stock exists or not has been checked successfully";

            txnManager.commit(status);

        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jObj);
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());

    }

    public ModelAndView getReorderLevelReportList(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IST_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;

        try {

            String companyId = sessionHandlerImpl.getCompanyid(request);
            String userId = sessionHandlerImpl.getUserid(request);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);

            KwlReturnObject cap1 = accountingHandlerDAO.getObject(ExtraCompanyPreferences.class.getName(), companyId);
            ExtraCompanyPreferences ecf = (ExtraCompanyPreferences) cap1.getEntityList().get(0);

            jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            boolean isBelowReorderFilter = StringUtil.isNullOrEmpty(request.getParameter("reordelLevelFilter")) ? false : Boolean.parseBoolean(request.getParameter("reordelLevelFilter"));
            paging = new Paging(start, limit);
            String exportFileName = request.getParameter("filename"); // for Export
            boolean isExport = false;
            if (!StringUtil.isNullOrEmpty(exportFileName)) {
                isExport = true;
                paging = null;
            }
            String searchString = request.getParameter("ss");

            String storeId = request.getParameter("store");
            List<Store> stores = new ArrayList();
            Set<Store> storeSet = new HashSet();
            Store store = null;
            if (!StringUtil.isNullOrEmpty(storeId)) {
                store = storeService.getStoreById(storeId);
                storeSet.add(store);
            } else {
                stores = storeService.getStores(user.getCompany(), null, null);

                for (Store s : stores) {
                    Set<User> storeManagers = s.getStoreManagerSet();
                    storeManagers.addAll(s.getStoreExecutiveSet());
                    if (storeManagers.contains(user)) {
                        storeSet.add(s);
                    }
                }
            }
            List<Stock> stockList = null;

            if (storeSet != null && !storeSet.isEmpty()) {
                stockList = stockService.getReorderReportList(company, storeSet, searchString, isBelowReorderFilter, paging);
            }

            if (stockList != null && !stockList.isEmpty()) {
                for (Stock ist : stockList) {
                    JSONObject jObj = new JSONObject();
                    jObj.put("id", ist.getId());
                    jObj.put("itemid", ist.getProduct().getID());
                    jObj.put("itemcode", ist.getProduct().getProductid());
                    jObj.put("itemdescription", ist.getProduct().getDescription());
                    jObj.put("itemname", ist.getProduct().getName());
                    jObj.put("stockuom", (ist.getProduct().getPackaging() == null )? "" : (ist.getProduct().getPackaging().getStockUoM()!=null?ist.getProduct().getPackaging().getStockUoM().getNameEmptyforNA():""));
                    jObj.put("quantity", authHandler.formattedQuantity(ist.getQuantity(),companyId));
                    jObj.put("reorderlevel", ist.getProduct().getReorderLevel());
                    jObj.put("isbelowreorder", ist.getProduct().getReorderLevel() > ist.getQuantity());
                    jArray.put(jObj);
                }
            }
            if (isExport) {
                jobj.put("data", jArray);
                exportDAO.processRequest(request, response, jobj);
            }
            issuccess = true;
            msg = "Stock list has been fetched successfully";

            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArray);
                if (paging != null) {
                    jobj.put("count", paging.getTotalRecord());
                } else {
                    jobj.put("count", jArray.length());
                }
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());

    }
    /*
    * @author: Sayed kausar Ali
    * @Params: {HttpServletRequest,HttpServletResponse}
    * @Purpose: To Fecth Batches used in Stock Adjustment for a particular Job Work Order
    */
    
    public ModelAndView getBatchDetailsForJobWorkOrder(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        JSONArray jArray = new JSONArray();
        Paging paging = null;

        try {
            DateFormat df = authHandler.getDateFormatter(request);
            String jobworkorderid="";
            String productid="";
            String locationid="";
            String warehouseid="";
            if (!StringUtil.isNullOrEmpty(request.getParameter("jobworkorderid"))) {
                   jobworkorderid = (request.getParameter("jobworkorderid"));
}
            if (!StringUtil.isNullOrEmpty(request.getParameter("productid"))) {
                   productid = (request.getParameter("productid"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("location"))) {
                   locationid = (request.getParameter("location"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("warehouse"))) {
                   warehouseid = (request.getParameter("warehouse"));
            }
            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("soid", jobworkorderid);
            /*
            * Fetching Stock Adjustment Details for a particular Job Work Order.
            */
            KwlReturnObject result = productDAOObj.getSADetailsForSO(requestParams);
            List<String> sadetailids = result.getEntityList();
            /*
            * For all Stock Adjustment Details Fetched.
            */
            for (String saDetailid : sadetailids) {
                /*
                * Fetching Location, Ware house, row, rack, bin information etc.
                */
                KwlReturnObject saresult = accountingHandlerDAO.getObject(StockAdjustmentDetail.class.getName(), saDetailid);
                StockAdjustmentDetail sadetailObj = (StockAdjustmentDetail) saresult.getEntityList().get(0);
                Product product = sadetailObj.getStockAdjustment().getProduct();
//                Store store = sadetailObj.getStockAdjustment().getStore();
                Store store = (Store) kwlCommonTablesDAOObj.getClassObject(Store.class.getName(), warehouseid);
                Location location = (Location) kwlCommonTablesDAOObj.getClassObject(Location.class.getName(), locationid);
                StoreMaster row = sadetailObj.getRow();
                StoreMaster rack = sadetailObj.getRack();
                StoreMaster bin = sadetailObj.getBin();
                String batchName = sadetailObj.getBatchName();
                /*
                * If Product Id of Stock Adjustment Details Matches with passed Product Id.
                */
                if (productid.equals(product.getID())) {
                    /*
                    * Fetching object of NewProductBatch and creating a json for it.
                    */
                    NewProductBatch productBatch = stockService.getERPProductBatch(product, store,location, row, rack, bin, batchName);
                    if (productBatch != null) {
                        JSONObject obj = new JSONObject();
                        obj.put("id", productBatch.getId());
                        obj.put("batch", productBatch.getId());
                        obj.put("name", productBatch.getBatchname());
                        obj.put("batchname", productBatch.getBatchname());
                        obj.put("mfgdate", productBatch.getMfgdate() != null ? df.format(productBatch.getMfgdate()) : "");
                        obj.put("expdate", productBatch.getExpdate() != null ? df.format(productBatch.getExpdate()) : "");
                        obj.put("warehouse", productBatch.getWarehouse() != null ? productBatch.getWarehouse().getId() : "");
                        obj.put("warehousename", productBatch.getWarehouse() != null ? productBatch.getWarehouse().getName() : "");
                        obj.put("location", productBatch.getLocation() != null ? productBatch.getLocation().getId() : "");
                        obj.put("locationname", productBatch.getLocation() != null ? productBatch.getLocation().getName() : "");
                        obj.put("productid", productBatch.getProduct() != null ? productBatch.getProduct() : "");
                        jArray.put(obj);
                    }
                }
                
                
            }
            jobj.put("data", jArray);

        } catch (Exception ex) {
            Logger.getLogger(StockController.class.getName()).log(Level.SEVERE, null, ex);
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
    
    public ModelAndView getAssetDetailswithStock(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        JSONArray jArray = new JSONArray();
        Paging paging = null;

        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String userId = sessionHandlerImpl.getUserid(request);
            String type=request.getParameter("type");

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);
            Company company = user.getCompany();
             DateFormat df = authHandler.getDateOnlyFormat(request);
            String jobworkorderid="";
            String productid="";
            String locationid="";
            String warehouseid="";
            
              String exportFileName = request.getParameter("filename"); // for Export
            boolean isExport = false;
            if (!StringUtil.isNullOrEmpty(exportFileName)) {
                isExport = true;
                paging = null;
            } else {
                String start = request.getParameter("start");
                String limit = request.getParameter("limit");
                paging = new Paging(start, limit);
            }
            String searchString = request.getParameter("ss");

            String storeId = request.getParameter("store");
            String locationId = request.getParameter("location");
            Set<Store> storeSet = null;
            if (!StringUtil.isNullOrEmpty(storeId)&&!("ALL".equals(storeId)||"Customer".equals(storeId))) {
                Store store = storeService.getStoreById(storeId);
                if (store != null) {
                    storeSet = new HashSet<>();
                    storeSet.add(store);
                }

            } else if(!("Customer".equals(storeId))){
                boolean includeQAAndRepairStore = true;
                boolean includePickandPackStore = true;
                List<Store> stores = storeService.getStoresByStoreExecutivesAndManagers(user, true, null, null, null,includeQAAndRepairStore,includePickandPackStore);
                if (!stores.isEmpty()) {
                    storeSet = new HashSet<>(stores);
                }
            }
             
            if (!StringUtil.isNullOrEmpty(request.getParameter("productid"))) {
                   productid = (request.getParameter("productid"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("location"))&&!("ALL".equals(request.getParameter("location")))) {
                   locationid = (request.getParameter("location"));
            }
             
            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("company", companyId);
            requestParams.put("storeset", storeSet);
            requestParams.put("locationid", locationid);
            requestParams.put("df", df);
            requestParams.put("type", type);
            requestParams.put("ss", searchString);
            requestParams.put("Customer",storeId);
            requestParams.put("request",request);
            /*
            * Fetching Stock Adjustment Details for a particular Job Work Order.
            */
            jobj = stockService.getAssetDetailList(requestParams, paging,company);
            if (isExport) {
                exportDAO.processRequest(request, response, jobj);
            }
            issuccess=true;
            msg="Data Fetched successfully";

        } catch (Exception ex) {
            Logger.getLogger(StockController.class.getName()).log(Level.SEVERE, null, ex);
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
    public ModelAndView importStockAdjustment(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        String auditMessage = "";
        boolean issuccess = false;
        try {
            String userId = sessionHandlerImpl.getUserid(request);
            String eParams = request.getParameter("extraParams");
            JSONObject extraParams = StringUtil.isNullOrEmpty(eParams) ? new JSONObject() : new JSONObject(eParams);
            extraParams.put("Company", sessionHandlerImpl.getCompanyid(request));

            KwlReturnObject custumObjresult = accountingHandlerDAO.getObject(CompanyAccountPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) custumObjresult.getEntityList().get(0);
            custumObjresult= accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) custumObjresult.getEntityList().get(0);
            Date bookBeginningDate = preferences.getBookBeginningFrom();
            String doAction = request.getParameter("do");
            HashMap<String, Object> requestParams = importHandler.getImportRequestParams(request);
            requestParams.put("extraParams", extraParams);
            requestParams.put("extraObj", null);
            requestParams.put("servletContext", this.getServletContext());
            requestParams.put("bookbeginning", bookBeginningDate);
            requestParams.put("locale", RequestContextUtils.getLocale(request));    //For Localize purpose

            if (importInvData.processQueue != null && !importInvData.processQueue.isEmpty()) {
                msg = "Some files are in process , please try after some time.";
                jobj.put("msg", msg);
                jobj.put("success", true);
            } else {
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

                    Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
                    Map<String, Object> jeDataMap1 = AccountingManager.getGlobalParams(request);
                    Locale locale = RequestContextUtils.getLocale(request);

                    requestParams.put("jeDataMap", jeDataMap);
                    requestParams.put("jeDataMap1", jeDataMap1);
                    requestParams.put("locale", locale);
                    requestParams.put("typeXLSFile", typeXLSFile);
                    int totalRecsInFile = 0;

                    if (typeXLSFile) {
                        int sheetNo = Integer.parseInt(requestParams.get("sheetindex").toString());
                        FileInputStream fs = new FileInputStream(filepath);
                        Workbook wb = WorkbookFactory.create(fs);// create workbook for all types of excel file like xls, xlsx etc
                        Sheet sheet = wb.getSheetAt(sheetNo);
                        totalRecsInFile = sheet.getLastRowNum() - 1;
                    } else {
                        totalRecsInFile = importHandler.dumpCSVFileData(filename, ",", 1, "true");
                    }
                    if (importInvData.isFileInProcess(user.getCompany().getCompanyID())) {
                        msg = "Some files are in process , please try after some time.";
                        jobj.put("msg", msg);
                        jobj.put("success", true);
                    } else {
                    if (totalRecsInFile < 200) {
                        if (typeXLSFile) {
                            jobj = importInvData.importStockAdjustmentRecordsXls(requestParams, jeDataMap, jeDataMap1, locale);
                        } else {
                            jobj = importInvData.importStockAdjustmentRecords(requestParams, jeDataMap, jeDataMap1, locale);
                        }
                    } else {
                        ArrayList l = importInvData.processQueue;
                        if (l != null && !l.isEmpty()) {
                            msg = " Some file is already in process, please try again later. ";
                            jobj.put("msg", msg);
                            jobj.put("success", true);
                        } else {
                            importInvData.add(requestParams);
//                    ImportInvData importInvDataThred = new ImportInvData(requestParams, jeDataMap, jeDataMap1, locale, typeXLSFile);
                            Thread t = new Thread(importInvData);
                            t.start();
                            msg = "File contains more than 200 records, so it will take time to import.";
                            jobj.put("msg", msg);
                            jobj.put("success", true);
                        }
                    }
                        if (isQAActivated.equals("true")) {
                            auditMessage = "User " + user.getFullName() + " has imported Stock Adjustment File: " + getActualFileName(request.getParameter("filename")) + " with QA Inspection";
                            auditTrailObj.insertAuditLog(AuditAction.STOCK_ADJUSTMENT_ADDED, auditMessage, request, "0");
                        } else {
                            auditMessage = "User " + user.getFullName() + " has imported Stock Adjustment File: " + getActualFileName(request.getParameter("filename")) + " without QA Inspection. ";
                            auditTrailObj.insertAuditLog(AuditAction.STOCK_ADJUSTMENT_ADDED, auditMessage, request, "0");
                        }
                    }
                   // System.out.println("A(( Import end : " + new Date());

                } else if (doAction.compareToIgnoreCase("validateData") == 0) {
                    System.out.println("A(( Validation start : " + new Date());
                    jobj = importHandler.validateFileData(requestParams);
                    System.out.println("A(( Validation end : " + new Date());
                }
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
}
