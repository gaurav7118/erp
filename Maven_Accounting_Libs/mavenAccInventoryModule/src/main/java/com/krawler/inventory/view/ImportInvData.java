/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.view;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.CostCenter;
import com.krawler.common.admin.DocumentEmailSettings;
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
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import static com.krawler.common.util.Constants.locale;
import com.krawler.common.util.CsvReader;
import com.krawler.common.util.DataInvalidateException;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.Inventory;
import com.krawler.hql.accounting.JournalEntry;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.hql.accounting.MasterGroup;
import com.krawler.hql.accounting.MasterItem;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.ProductBuild;
import com.krawler.hql.accounting.Producttype;
import com.krawler.hql.accounting.SequenceFormat;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.hql.accounting.UnitOfMeasure;
import com.krawler.inventory.exception.InventoryException;
import com.krawler.inventory.exception.SeqFormatException;
import com.krawler.inventory.model.ist.InterStoreTransferService;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.location.LocationService;
import com.krawler.inventory.model.sequence.ModuleConst;
import com.krawler.inventory.model.sequence.SeqFormat;
import com.krawler.inventory.model.sequence.SeqService;
import com.krawler.inventory.model.stock.StockDAO;
import com.krawler.inventory.model.stock.StockService;
import com.krawler.inventory.model.stockmovement.StockMovementService;
import com.krawler.inventory.model.stockmovement.TransactionModule;
import com.krawler.inventory.model.stockout.StockAdjustment;
import com.krawler.inventory.model.stockout.StockAdjustmentDetail;
import com.krawler.inventory.model.stockout.StockAdjustmentService;
import com.krawler.inventory.model.store.Store;
import com.krawler.inventory.model.store.StoreService;
import com.krawler.inventory.view.GoodsTransferController;
import static com.krawler.inventory.view.ImportDataController.getActualFileName;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesConstants;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.costCenter.AccCostCenterDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.product.accProductController;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.productmodule.service.AccProductModuleService;
import com.krawler.spring.accounting.salesorder.accSalesOrderServiceImpl;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.companyDetails.companyDetailsDAO;
import com.krawler.spring.importFunctionality.ImportDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import io.jsonwebtoken.lang.Strings;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.jfree.chart.block.Arrangement;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 *
 * @author krawler
 */
public class ImportInvData implements Runnable, MessageSourceAware {

    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final Logger lgr = Logger.getLogger(GoodsTransferController.class.getName());
    private HibernateTransactionManager txnManager;
    private String successView;
    public ImportHandler importHandler;
    private accAccountDAO accAccountDAOobj;
    private accMasterItemsDAO accMasterItemsDAOobj;
    private ImportDAO importDao;
    private accProductDAO accProductDAO;
    private AccountingHandlerDAO accountingHandlerDAO;
    private AccCostCenterDAO accCostCenterObj;
    private LocationService locationService;
    private StockService stockService;
    private StockMovementService stockMovementService;
    private InterStoreTransferService istService;
    private StockDAO stockDAO;
    private MessageSource messageSource;
    private StoreService storeService;
    private AccProductModuleService accProductModuleService;
    private SeqService seqService;
    private accCompanyPreferencesDAO accCompanyPreferencesDAO;
    private accJournalEntryDAO accJournalEntryDAO;
    private StockAdjustmentService stockAdjustmentService;
    private HashMap<String, Object> requestParams;
    private Map<String, Object> jeDataMap;
    private Map<String, Object> jeDataMap1;
    private Locale locale;
    private boolean fileTypeExcel = false;

    public ImportDAO getImportDao() {
        return importDao;
    }

    public void setImportDao(ImportDAO importDao) {
        this.importDao = importDao;
    }

    public ImportHandler getImportHandler() {
        return importHandler;
    }

    public void setImportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    public accProductDAO getAccProductDAO() {
        return accProductDAO;
    }

    public void setAccProductDAO(accProductDAO accProductDAO) {
        this.accProductDAO = accProductDAO;
    }

    public accCompanyPreferencesDAO getAccCompanyPreferencesDAO() {
        return accCompanyPreferencesDAO;
    }

    public void setAccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesDAO) {
        this.accCompanyPreferencesDAO = accCompanyPreferencesDAO;
    }

    public accJournalEntryDAO getAccJournalEntryDAO() {
        return accJournalEntryDAO;
    }

    public void setAccJournalEntryDAO(accJournalEntryDAO accJournalEntryDAO) {
        this.accJournalEntryDAO = accJournalEntryDAO;
    }

    public AccountingHandlerDAO getAccountingHandlerDAO() {
        return accountingHandlerDAO;
    }

    public void setAccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAO) {
        this.accountingHandlerDAO = accountingHandlerDAO;
    }

    public LocationService getLocationService() {
        return locationService;
    }

    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    public StockService getStockService() {
        return stockService;
    }

    public void setStockService(StockService stockService) {
        this.stockService = stockService;
    }

    public StockMovementService getStockMovementService() {
        return stockMovementService;
    }

    public void setStockMovementService(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    public StockDAO getStockDAO() {
        return stockDAO;
    }

    public void setStockDAO(StockDAO stockDAO) {
        this.stockDAO = stockDAO;
    }

    public StoreService getStoreService() {
        return storeService;
    }

    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }

    public AccProductModuleService getAccProductModuleService() {
        return accProductModuleService;
    }

    public void setAccProductModuleService(AccProductModuleService accProductModuleService) {
        this.accProductModuleService = accProductModuleService;
    }

    public SeqService getSeqService() {
        return seqService;
    }

    public void setSeqService(SeqService seqService) {
        this.seqService = seqService;
    }

    public StockAdjustmentService getStockAdjustmentService() {
        return stockAdjustmentService;
    }

    public void setStockAdjustmentService(StockAdjustmentService stockAdjustmentService) {
        this.stockAdjustmentService = stockAdjustmentService;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public HibernateTransactionManager getTxnManager() {
        return txnManager;
    }

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public accAccountDAO getAccAccountDAOobj() {
        return accAccountDAOobj;
    }

    public void setAccAccountDAOobj(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public accMasterItemsDAO getAccMasterItemsDAOobj() {
        return accMasterItemsDAOobj;
    }

    public void setAccMasterItemsDAOobj(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }

    public AccCostCenterDAO getAccCostCenterObj() {
        return accCostCenterObj;
    }

    public void setAccCostCenterObj(AccCostCenterDAO accCostCenterObj) {
        this.accCostCenterObj = accCostCenterObj;
    }

    public InterStoreTransferService getIstService() {
        return istService;
    }

    public void setIstService(InterStoreTransferService istService) {
        this.istService = istService;
    }

   

//    public ImportInvData ()
//    {
//        
//    }
//    public ImportInvData(HashMap<String, Object> reqparam, Map<String, Object> jdata, Map<String, Object> jdata1, Locale lcl,boolean flType) {
//        this.requestParams = reqparam;
//        this.jeDataMap = jdata;
//        this.jeDataMap1 = jdata1;
//        this.locale = lcl;
//        this.fileTypeExcel=flType;
//    }
    ArrayList processQueue = new ArrayList();

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void add(HashMap<String, Object> requestParams) {
        try {
            processQueue.add(requestParams);

        } catch (Exception ex) {
            Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        if (!processQueue.isEmpty()) {
            HashMap<String, Object> requestParams = (HashMap<String, Object>) processQueue.get(0);
            this.requestParams = requestParams;
            this.jeDataMap = (HashMap<String, Object>) requestParams.get("jeDataMap");
            this.jeDataMap1 = (HashMap<String, Object>) requestParams.get("jeDataMap1");
            this.locale = (Locale) requestParams.get("locale");
            this.fileTypeExcel = (Boolean) requestParams.get("typeXLSFile");
            try {
                if (fileTypeExcel) {

                    JSONObject jobj = importStockAdjustmentRecordsXls(requestParams, jeDataMap, jeDataMap1, locale);

                } else {

                    JSONObject jobj = importStockAdjustmentRecords(requestParams, jeDataMap, jeDataMap1, locale);

                }
            } catch (Exception ex) {
                Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
                processQueue.clear();
            } finally {
                processQueue.clear();
            }
        }
    }
    /*
    *ERM-718 Import IST
     this function for import IST request
    */
    public JSONObject importInterStoreTransferRecord(HashMap<String, Object> requestParams, Map<String, Object> jeDataMap, Map<String, Object> jeDataMap1, Locale locale) throws AccountingException, IOException, SessionExpiredException, JSONException {
        boolean commitedEx = false;
        boolean issuccess = true;
        String failureMsg="";
        String msg = "";
        FileInputStream fileInputStream = null;
        CsvReader csvReader = null;
        int total = 0, failed = 0;
        String companyid = requestParams.get("companyid").toString();
        String currencyId = requestParams.get("currencyId").toString();
        JSONObject jobj = new JSONObject();
        JSONObject batchDetailObj = new JSONObject();
        jobj = (JSONObject) requestParams.get("jobj");
        String userId = requestParams.get("userid").toString();
        String dateformateproduct = jobj.getString("dateformateproduct");
        String isQAActivated = jobj.getString("isQAActivated");
        String masterPreference = requestParams.get("masterPreference").toString();
        String fileName = jobj.getString("filename");
        String delimiterType = requestParams.get("delimiterType").toString();
        Date bookBeginningDate = (Date) requestParams.get("bookbeginning");
        JSONObject returnObj = new JSONObject();
        DateFormat dateFrmt = null;
        String inspectionStore = "";
        String jobWorkOrderStore="";
        String repairStore = "";
        String packingStore = "";
        boolean isInventoryIntegrationOn = false;
        String logId = null;
        if (requestParams.containsKey("locale")) {
            locale = (Locale) requestParams.get("locale");
        }
        JSONArray jarr = new JSONArray();
        JSONArray jarrayFinal = new JSONArray();
        try {
            //logId = addPendingImportLog(requestParams);
          
            String scrapStore = "";

            KwlReturnObject kmsg = null;
            List  AlreadyExistingSeqForIST = new ArrayList();
            Map<String, Object> seqParams=new HashMap<String, Object>();
            seqParams.put("companyid",requestParams.get("companyid"));
            AlreadyExistingSeqForIST=seqService.getExistingSeqNumbers(seqParams);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyid);
            Company company = (Company) jeresult.getEntityList().get(0);
            KwlReturnObject extraCompanyPrefResult = accountingHandlerDAO.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraCompanyPrefResult.getEntityList().get(0);
            isInventoryIntegrationOn = extraCompanyPreferences.isActivateInventoryTab();

            JSONObject columnpref = null;
            if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                columnpref = new JSONObject(extraCompanyPreferences.getColumnPref());
            }
            if (columnpref != null) {
                scrapStore = columnpref.optString("scrapStore", "");
            }

            // CompanyAccountPreferences
            extraCompanyPrefResult = accountingHandlerDAO.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) extraCompanyPrefResult.getEntityList().get(0);

//            Date newUserDate = new Date();
            KwlReturnObject custumObjresult = accountingHandlerDAO.getObject(Company.class.getName(), companyid);
//            Company company1 = (Company) custumObjresult.getEntityList().get(0);
//            if (company1.getCreator() != null) {
//                newUserDate = authHandler.getUserNewDate(null, company1.getCreator().getTimeZone() != null ? company1.getCreator().getTimeZone().getDifference() : company1.getTimeZone().getDifference());
//            }
            String dateFormat = null, dateFormatId = dateformateproduct;
            if (!StringUtil.isNullOrEmpty(dateFormatId)) {
                KwlReturnObject kdfObj = accountingHandlerDAO.getObject(KWLDateFormat.class.getName(), dateFormatId);
                KWLDateFormat kdf = (KWLDateFormat) kdfObj.getEntityList().get(0);
                dateFormat = kdf != null ? kdf.getJavaForm() : null;
            }

            dateFrmt = new SimpleDateFormat(dateFormat);
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
          
            inspectionStore= StringUtil.isNullOrEmpty(extraCompanyPreferences.getInspectionStore())?"":extraCompanyPreferences.getInspectionStore();
            jobWorkOrderStore= StringUtil.isNullOrEmpty(extraCompanyPreferences.getVendorjoborderstore())?"":extraCompanyPreferences.getVendorjoborderstore();
            packingStore= StringUtil.isNullOrEmpty(extraCompanyPreferences.getPackingstore())?"":extraCompanyPreferences.getPackingstore();
            fileInputStream = new FileInputStream(jobj.getString("FilePath"));
            csvReader = new CsvReader(new InputStreamReader(fileInputStream), delimiterType);
            String record = "";
            int cnt = 0;
            int count = 1;
            int limit = Constants.Transaction_Commit_Limit;
            String invalidColumns = null;
            StringBuilder failedRecords = new StringBuilder();
            HashMap<String, Integer> columnConfig = new HashMap<String, Integer>();
            JSONArray jSONArray = jobj.getJSONArray("resjson");
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);

                columnConfig.put(jSONObject.getString("dataindex"), jSONObject.getInt("csvindex"));
            }
            List keyList = new ArrayList();
            while (csvReader.readRecord()) {
                failureMsg="";
                String[] recarr = csvReader.getValues();
                if (cnt != 0) {
                    try {
                        String productID = "";
                        if (columnConfig.containsKey("productid")) {
                            productID = recarr[(Integer) columnConfig.get("productid")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(productID)) {
                                productID = productID.replaceAll("\"", "");
                            } else {
                                failureMsg+="Product ID is not available.";
                            }
                        } else {
                            failureMsg+="Product ID column is not found.";
                        }
                        // getting product object
                        KwlReturnObject result = accProductDAO.getProductIDCount(productID, companyid, false);
                        int nocount = result.getRecordTotalCount();
                        if (nocount == 0) {
                            failureMsg+= "productID '" + productID + "' not exists.";
                        }
                        boolean isValidProduct=false;
                          Product product = (Product) result.getEntityList().get(0);
                          if(product !=null && product.getProducttype().getID()!=null){
                           isValidProduct= (product.getProducttype().getID().equalsIgnoreCase(Producttype.NON_INVENTORY_PART) || product.getProducttype().getID().equalsIgnoreCase(Producttype.SERVICE))?false:true;
                          }
                          boolean isActive=product.isIsActive();
                          if(!isValidProduct || !isActive){
                              failureMsg+="ProductID '"+productID+"' is Not Valid Product";
                          }
                        if (product != null && isValidProduct) {
                            HashMap<String,Object> requestParams1=new HashMap<String,Object>();
                            requestParams1.put("product",product);
                            requestParams1.put("companyid", companyid);
                            requestParams1.put("columnConfig",columnConfig);
                            requestParams1.put("masterPreference",masterPreference);
                            requestParams1.put("recarr",recarr);
                            requestParams1.put("inspectionStore",inspectionStore);
                            requestParams1.put("jobWorkOrderStore",jobWorkOrderStore);
                            requestParams1.put("packingStore",packingStore);
                            requestParams1.put("packingStore",packingStore);
                            requestParams1.put("recarr",recarr);
                            HashMap<String, Object>returnMap=new HashMap<String, Object>();
                            returnMap = validateBatchDetails(requestParams1); //Validate batch details
                            if (returnMap.containsKey("failureMsg") && returnMap.get("failureMsg") != null) {
                                failureMsg += (String) returnMap.get("failureMsg");
                            }

                            if (StringUtil.isNullOrEmpty(failureMsg) && returnMap.containsKey("batchDetail") && returnMap.get("batchDetail") != null) {
                                batchDetailObj= (JSONObject) returnMap.get("batchDetail");
                               
                            }
                          
                        }
                        // bussinessDate
                        String bussinessdate = null;
                        Date bussinessDate = null;
                        boolean isValiddate = true;
                        if (columnConfig.containsKey("businessdate")) {
                            String dateValue = recarr[(Integer) columnConfig.get("businessdate")].replaceAll("\"", "").trim();
                            bussinessdate = dateValue;
                            if (!StringUtil.isNullOrEmpty(dateValue)) {
                                try {
                                    bussinessDate = dateFrmt.parse(bussinessdate);
                                    isValiddate = isvalidDate(bussinessDate, companyAccountPreferences);
                                } catch (ParseException ex) {
                                    throw new AccountingException("Incorrect date format for Business Date, Please specify values in " + dateFormat + " format.");
                                }
                            }
                        } else {
                           
                            failureMsg += "businessdate is not available.";
                        }
                        if (!isValiddate) {
                            failureMsg+="Bussiness date should not be before than Book Beginning Date.";
                        }
                        
                        //validating document no
                        boolean seqExist = false;
                        String sequenceFormatID = "NA";
                        boolean isSeqExist = false;
                        String documentNumber = "";
                        if (columnConfig.containsKey("number")) {
                            documentNumber = recarr[(Integer) columnConfig.get("number")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(documentNumber)) {
                                failureMsg += "Sequence Number is not available";
                            }else{
                                seqExist = AlreadyExistingSeqForIST.contains(documentNumber);
                                if (seqExist) {
                                    failureMsg += "Sequence number already exist, please enter other one.";
                                }
                            }
                        }
                       
                        
                        
                        
                        //validating memo
                        String memo = "";
                        if (columnConfig.containsKey("memo")) {
                            memo = recarr[(Integer) columnConfig.get("memo")].replaceAll("\"", "").trim();
                        }
                        //validating costcenter
                        
                        String costCenterID = "";
                        if (columnConfig.containsKey("costcenter")) {
                            String costCenterName = recarr[(Integer) columnConfig.get("costcenter")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(costCenterName)) {
                                costCenterID = getCostCenterIDByName(costCenterName, companyid);
                                if (StringUtil.isNullOrEmpty(costCenterID)) {
                                    failureMsg += "Cost Center is not found for name " + costCenterName;
                                }
                            }
                            
                        } else {
                            failureMsg += "Cost Center is not availabel";
                        }
                        String remark="";
                       if(columnConfig.containsKey("remark")){
                           remark=recarr[(Integer)columnConfig.get("remark")].replaceAll("\"", "").trim();
                       }else{
                           failureMsg+="Remark is not available";
                       }
                       
                        if (!StringUtil.isNullOrEmpty(failureMsg)) {
//                            failureList.add(entryNumber);
                            throw new AccountingException(failureMsg);
                        }
                        if(StringUtil.isNullOrEmpty(failureMsg) && batchDetailObj!=null){
                        UnitOfMeasure uom=product.getUnitOfMeasure();
                        boolean UOMSheamatype=product.getUomSchemaType()!=null?true:false;
                        
                        double quantity=batchDetailObj.optDouble("quantity");
                        JSONObject stockDetail=new JSONObject();
                        stockDetail.put("itemid",product.getID());
                        stockDetail.put("businessdate",sdf.format(bussinessDate));
                        stockDetail.put("costcenter",costCenterID);
                        stockDetail.put("packaging",(product.getPackaging()==null?"":product.getPackaging().getId()));
                        stockDetail.put("uom",uom==null?"": uom.getID());
                        stockDetail.put("uomname",uom==null?"": uom.getName());
                        stockDetail.put("remark",remark);
                        stockDetail.put("quantity",quantity);
                        JSONArray detArr = new JSONArray();
                        detArr.put(batchDetailObj);
                        stockDetail.put("stockDetails", detArr);
                        
                        JSONArray finalArr=new JSONArray();
                        finalArr.put(stockDetail);
                        
     
                            JSONObject jdata = new JSONObject();
                            jdata.put("seqCheckedWhileImport", true);
                            jdata.put("companyid", companyid);
                            jdata.put("userid",userId);
                            jdata.put("remoteAddress",requestParams.get("remoteAddress"));
                            jdata.put("reqHeader",requestParams.get("reqHeader"));
                            jdata.put("language",requestParams.get("locale"));
                            jdata.put("product",product.getID());
                            jdata.put("transactionno","");
                            jdata.put("documentNumber",documentNumber);
                            jdata.put("seqFormatId",sequenceFormatID);
                            jdata.put("fromstore",StringUtil.isNullOrEmpty(batchDetailObj.optString("warehouse"))?"":batchDetailObj.optString("warehouse"));
                            jdata.put("tostore",StringUtil.isNullOrEmpty(batchDetailObj.optString("towarehouse"))?"":batchDetailObj.optString("towarehouse")); 
                            jdata.put("UomSchemaType",UOMSheamatype);
                            jdata.put("memo",memo);
                            jdata.put("str",finalArr.toString());
                            
                             boolean isAccept=true;
                            // save inTransit
                             
                            JSONObject inTransitObj=new JSONObject();
                            inTransitObj=istService.saveInterStoreTransferRequest(jdata);
                            boolean transitSuccess=(Boolean)inTransitObj.get("success");
                            String id=inTransitObj.optString("billid");
                            String detailId=inTransitObj.optString("datailId");
                            if(transitSuccess && isAccept){
                                JSONObject acceptJson=new JSONObject();
                                JSONObject detail= new JSONObject();
                                JSONObject StockDetails=new JSONObject();
                                JSONArray Jarray= new JSONArray();
                                String toLocationId= !StringUtil.isNullOrEmpty(batchDetailObj.optString("toLocationId"))?batchDetailObj.optString("toLocationId"):"";
                                String toRowId= !StringUtil.isNullOrEmpty(batchDetailObj.optString("toRowId"))?batchDetailObj.optString("toRowId"):"";
                                String toRackId= !StringUtil.isNullOrEmpty(batchDetailObj.optString("toRackId"))?batchDetailObj.optString("toRackId"):"";
                                String toBinId= !StringUtil.isNullOrEmpty(batchDetailObj.optString("toBinId"))?batchDetailObj.optString("toBinId"):"";
                                String batchName= !StringUtil.isNullOrEmpty(batchDetailObj.optString("batchName"))?batchDetailObj.optString("batchName"):"";
                                String serialName= !StringUtil.isNullOrEmpty(batchDetailObj.optString("serialNames"))?batchDetailObj.optString("serialNames"):"";
                            
                                StockDetails.put("detailId",detailId);
                                StockDetails.put("locationId",toLocationId);
                                StockDetails.put("quantity",quantity);
                                StockDetails.put("rowId",toRowId);
                                StockDetails.put("rackId",toRackId);
                                StockDetails.put("binId",toBinId);
                                StockDetails.put("batchName",batchName);
                                StockDetails.put("serialNames",serialName);
                                Jarray.put(StockDetails);
                                
                                detail.put("id",id);
                                detail.put("quantity",quantity);
                                detail.put("stockDetails",Jarray); 
                                
                                acceptJson.put("userid",userId);
                                acceptJson.put("remark",remark);
                                acceptJson.put("companyid",companyid);
                                
                                acceptJson.put("remoteAddress", requestParams.get("remoteAddress"));
                                acceptJson.put("reqHeader", requestParams.get("reqHeader"));
                                acceptJson.put("language", requestParams.get("locale"));
                                acceptJson.put("jsondata",detail.toString());
                                JSONObject acceptObj = istService.acceptInterStoreTransferRequest(acceptJson);
                                boolean acceptSuccess=acceptObj.optBoolean("success");
                               
                       }
                       
                       }
                       
                        
                        
                    } catch (Exception ex) {
                         String errorMsg = ex.getMessage();
                        if (ex.getMessage() != null) {
                            errorMsg = ex.getMessage();
                        } else if (ex.getCause() != null) {
                            errorMsg = ex.getCause().getMessage();
                        }
                        failed++;
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

            Logger.getLogger(GoodsTransferController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Importing Records.");
        }
        finally{
            
            fileInputStream.close();
            csvReader.close();
            HashMap<String, Object> logDataMap = new HashMap<String, Object>();
            try {
                logDataMap.put("FileName", getActualFileName(fileName));
                logDataMap.put("StorageName", fileName);
                logDataMap.put("Log", msg);
                logDataMap.put("Type", "csv");
                logDataMap.put("FailureFileType", failed > 0 ? "csv" : "");
                logDataMap.put("TotalRecs", total);
                logDataMap.put("Rejected", failed);
                logDataMap.put("Module",Constants.Acc_InterStore_ModuleId);
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userId);
                logDataMap.put("Company", companyid);
                //logDataMap.put("Id", logId);
                
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", getActualFileName(fileName));
                returnObj.put("data", jarr);
                returnObj.put("logData", logDataMap);
                if (issuccess) {
                    saveIstImportLog(logDataMap);
//                    String tableName = importDao.getTableName(logDataMap.get("StorageName").toString());
//                    importDao.removeFileTable(tableName); // Remove table after importing all records
                }
               
            }  catch (Exception ex) {
//                txnManager.rollback(lstatus);
                Logger.getLogger(GoodsTransferController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return returnObj;
    }
    /*
    *Validate Batch details ERM-718
    */
    public HashMap<String, Object>  validateBatchDetails(HashMap<String, Object> params)throws JSONException,ServiceException {
        HashMap<String, Object> returnMap=new HashMap<String, Object>();
        HashMap<String, Integer> columnConfig = new HashMap();
        String companyID = null;
        Product product = null;
        String[] recarr = null;
        String failureMsg = "";
        String masterPreference = null;
        String storeID = "";
        String toStoreID="";
        String warehouse = null;
        String location = null;
        String serialName = "";
        String batchName = "";
        String rowName = "";
        String rackName = "";
        String binName = "";
        boolean isLocationForProduct = false;
        boolean isWarehouseForProduct = false;
        boolean isBatchForProduct = false;
        boolean isSerialForProduct = false;
        boolean isRowForProduct = false;
        boolean isRackForProduct = false;
        boolean isBinForProduct = false;
    
        String inspectionStore = "";
        String packingStore = "";
        String jobWorkOrderStore="";
        NewProductBatch productBatchObj = null;
        NewBatchSerial newBatchSerial = null;
        double quantity=0;
        JSONObject batchDetailObj = new JSONObject();
        JSONArray batchDetailArr = new JSONArray();
        Map<String, Object> requestParams = new HashMap<>();
        if (params.containsKey("companyid") && params.get("companyid") != null) {
            companyID = (String) params.get("companyid");
        }
        if (params.containsKey("product") && params.get("product") != null) {
            product = (Product) params.get("product");
        }
        if (params.containsKey("masterPreference") && params.get("masterPreference") != null) {
            masterPreference = (String) params.get("masterPreference");
        }
        if (params.containsKey("recarr") && params.get("recarr") != null) {
            recarr = (String[]) params.get("recarr");
        }
        if (params.containsKey("inspectionStore") && params.get("inspectionStore") != null) {
            inspectionStore = (String) params.get("inspectionStore");
        }
        if (params.containsKey("packingStore") && params.get("packingStore") != null) {
            packingStore = (String) params.get("packingStore");
        }
        if (params.containsKey("jobWorkOrderStore") && params.get("jobWorkOrderStore") != null) {
            jobWorkOrderStore = (String) params.get("jobWorkOrderStore");
        }
        if(params.containsKey("columnConfig")&&params.get("columnConfig")!=null){
            columnConfig = (HashMap<String, Integer>) params.get("columnConfig");
        }
        try {
                isBatchForProduct = product.isIsBatchForProduct();
                isSerialForProduct = product.isIsSerialForProduct();
                isLocationForProduct = product.isIslocationforproduct();
                isWarehouseForProduct = product.isIswarehouseforproduct();
                isRowForProduct = product.isIsrowforproduct();
                isRackForProduct = product.isIsrackforproduct();
                isBinForProduct = product.isIsbinforproduct();
                requestParams.put("productid", product.getID());
                requestParams.put("companyid", companyID);
                
                /*
                Validate and get Warehouse detail object
                */
               

             if (columnConfig.containsKey("fromwarehouse")) { //From Warehouse
                    warehouse = recarr[(Integer) columnConfig.get("fromwarehouse")].replaceAll("\"", "").trim();
                    if (!StringUtil.isNullOrEmpty(warehouse)) {
                        requestParams.put("abbrev", warehouse);//abbrev is code of Warehouse
                        storeID = accountingHandlerDAO.getStoreByTypes(requestParams);
                        String msg=" So please give different warehouse.";
                        if (StringUtil.isNullOrEmpty(storeID)) {
                            failureMsg += "Warehouse is not found or type of warehouse is QA,Scrap,Repair etc.";
                        } else if (inspectionStore.equals(storeID)) {
                            failureMsg += warehouse + " warehouse is already used in QA Inspection store."+msg;
                        }  else if (packingStore.equals(storeID)) {
                            failureMsg += warehouse + " warehouse is already used in Packaging Warehouse for Stock Transfer."+msg;
                        } else if (jobWorkOrderStore.equals(storeID)) {
                            failureMsg += warehouse + " warehouse is already used in Vendor Job Order Store."+msg;
                        }else {
                            batchDetailObj.put("warehouse", storeID);
                            requestParams.put("store", storeID);
                            
                        }

                    } else {
                        if (!masterPreference.equalsIgnoreCase("1")) {
                            failureMsg += "From Warehouse is not available. ";
                        }
                    }
                }
             String  towarehouse ="";
             if (columnConfig.containsKey("towarehouse")) { //To Warehouse
                   towarehouse = recarr[(Integer) columnConfig.get("towarehouse")].replaceAll("\"", "").trim();
                    if (!StringUtil.isNullOrEmpty(towarehouse)) {
                        requestParams.put("abbrev", towarehouse);//abbrev is code of Warehouse
                        
                        toStoreID = accountingHandlerDAO.getStoreByTypes(requestParams);
                        String msg=" So please give different warehouse.";
                        if (StringUtil.isNullOrEmpty(toStoreID)) {
                            failureMsg += "Warehouse is not found or type of warehouse is QA,Scrap,Repair etc.";
                        } else if (inspectionStore.equals(toStoreID)) {
                            failureMsg += towarehouse + " warehouse is already used in QA Inspection store."+msg;
                        }  else if (packingStore.equals(toStoreID)) {
                            failureMsg += towarehouse + " warehouse is already used in Packaging Warehouse for Stock Transfer."+msg;
                        } else if (jobWorkOrderStore.equals(toStoreID)) {
                            failureMsg += towarehouse + " warehouse is already used in Vendor Job Order Store."+msg;
                        }else {
                            batchDetailObj.put("towarehouse", toStoreID);
                            requestParams.put("tostore", toStoreID);
                            
                        }

                    } else {
                        if (!masterPreference.equalsIgnoreCase("1")) {
                            failureMsg += "to Warehouse is not available. ";
                        }
                    }
                }
             if(storeID.equalsIgnoreCase(toStoreID)){
                 failureMsg+="From Warehouse and To Warehouse should be different. ";
             }
             //Validate and get location
            KwlReturnObject jreresult = null;
            jreresult = accountingHandlerDAO.getObject(Company.class.getName(), companyID);
            Company company = (Company) jreresult.getEntityList().get(0);

            Location locationObj = null;
            if (columnConfig.containsKey("fromlocation")) {
                location = recarr[(Integer) columnConfig.get("fromlocation")].replaceAll("\"", "").trim();
                if(!StringUtil.isNullOrEmpty(location)){
                    locationObj = locationService.getLocationByName(company, location);
                }
                if (!(locationObj == null)) {
                    batchDetailObj.put("locationId", locationObj.getId());
                    batchDetailObj.put("fromLocationName",locationObj.getName());
                    requestParams.put("location", locationObj.getId());
                } else {
                    failureMsg += "From Location is not available.";
                }
            } else {
                if (!masterPreference.equalsIgnoreCase("1")) {
                    failureMsg += "From Location is not available. ";
                }
            }
            Location toLocationObj=null;
            if (columnConfig.containsKey("tolocation")) {
                String tolocation = recarr[(Integer) columnConfig.get("tolocation")].replaceAll("\"", "").trim();
                 if (!StringUtil.isNullOrEmpty(tolocation)) {
                    toLocationObj = locationService.getLocationByName(company, tolocation);
                }
                if (!(toLocationObj == null)) {
                    batchDetailObj.put("toLocationId", toLocationObj.getId());
                    batchDetailObj.put("toLocationName",toLocationObj.getName());
                    requestParams.put("tolocation", toLocationObj.getId());
                } else {
                    failureMsg += "To Location is not available.";
                }
            } else {
                if (!masterPreference.equalsIgnoreCase("1")) {
                    failureMsg += "From Location is not available. ";
                }
            }
            
            //Validate and get Row
            StoreMaster row = null;
            String productRowName = null;
            if (isRowForProduct && columnConfig.containsKey("fromrow")) {
                productRowName = recarr[(Integer) columnConfig.get("fromrow")].replaceAll("\"", "").trim();
                if (StringUtil.isNullOrEmpty(productRowName)) {
                    failureMsg += "Product Row is not available.";
                } else {
                    row = storeService.getStoreMasterByName(productRowName, companyID, 1);
                    if (row == null) {
                        failureMsg += "Row is not found for " + productRowName + ". ";
                    } else {
                        batchDetailObj.put("rowId", row.getId());
                        batchDetailObj.put("fromRowName", row.getName());
                        requestParams.put("row", row);
                    }
                }
            }
            String toRowName = null;
            StoreMaster torow = null;
            if (isRowForProduct && columnConfig.containsKey("torow")) {
                toRowName = recarr[(Integer) columnConfig.get("torow")].replaceAll("\"", "").trim();
                if (StringUtil.isNullOrEmpty(toRowName)) {
                    failureMsg += "Product Row is not available. ";
                } else {
                    torow = storeService.getStoreMasterByName(toRowName, companyID, 1);
                    if (torow == null) {
                        failureMsg += "Row is not found for " + toRowName + ". ";
                    } else {
                        batchDetailObj.put("toRowId", torow.getId());
                        batchDetailObj.put("toRowName", torow.getName());
                        requestParams.put("torow", torow);
                    }
                }
            }
            //Validate and get rack
            StoreMaster rack = null;
            String productRackName = null;
            if (isRackForProduct && columnConfig.containsKey("fromrack")) {
                productRackName = recarr[(Integer) columnConfig.get("fromrack")].replaceAll("\"", "").trim();
                if (StringUtil.isNullOrEmpty(productRackName)) {
                    failureMsg += "Product Rack is not available. ";
                }else{
                    rack = storeService.getStoreMasterByName(productRackName, companyID, 2);
                    if (rack == null) {
                     failureMsg +="Rack is not found for "+productRackName+". ";
                    } else {
                       batchDetailObj.put("rackId", rack.getId());
                       batchDetailObj.put("fromRackName", rack.getName());
                        requestParams.put("rack", rack);
                    }
                
                }
                } 

             String toRackName = null;
            StoreMaster torack = null;
            if (isRackForProduct && columnConfig.containsKey("torack")) {
                toRackName = recarr[(Integer) columnConfig.get("torack")].replaceAll("\"", "").trim();
                if (StringUtil.isNullOrEmpty(toRackName)) {
                    failureMsg += "Product Rack is not available. ";
                } else {
                    torack = storeService.getStoreMasterByName(toRackName, companyID, 2);
                    if (torack == null) {
                        failureMsg += "Rack is not found for" + toRackName+". ";
                    } else {
                        batchDetailObj.put("toRackId", torack.getId());
                        batchDetailObj.put("toRackName", torack.getName());
                        requestParams.put("torack", torack);
                    }

                }

            }
              //validate bin for product
            StoreMaster bin = null;
            String productBinName = null;
            if (isBinForProduct && columnConfig.containsKey("frombin")) {
                productBinName = recarr[(Integer) columnConfig.get("frombin")].replaceAll("\"", "").trim();
                if (StringUtil.isNullOrEmpty(productBinName)) {
                    failureMsg += "Product Bin is not available. ";
                } else {
                    bin = storeService.getStoreMasterByName(productBinName, companyID, 3);
                    if (bin == null) {
                        failureMsg += "Bin is not found for" + productBinName+". ";
                    } else {
                        batchDetailObj.put("binId", bin.getId());
                        batchDetailObj.put("FromBinName", bin.getName());
                        requestParams.put("bin", bin);
                    }
                }
            }
            StoreMaster tobin = null;
            String toBinName = null;
            if (isBinForProduct && columnConfig.containsKey("tobin")) {
                toBinName = recarr[(Integer) columnConfig.get("tobin")].replaceAll("\"", "").trim();
                if (StringUtil.isNullOrEmpty(toBinName)) {
                    failureMsg += "Product Bin is not available. ";
                } else {
                    tobin = storeService.getStoreMasterByName(toBinName, companyID, 3);
                    if (tobin == null) {
                        failureMsg += "Bin is not found for" + toBinName+". ";
                    } else {
                        batchDetailObj.put("toBinId", tobin.getId());
                        batchDetailObj.put("toBinName", tobin.getName());
                        requestParams.put("tobin", tobin);
                    }
                }
            }
                 // end row rack bin validations
            //validate quantity
                 if(columnConfig.containsKey("quantity"))
                {
                quantity = Double.parseDouble(recarr[(Integer) columnConfig.get("quantity")].replaceAll("\"", "").trim());
                if (quantity <= 0) {
                    failureMsg += "Quantity should be greater than 0. ";

                } else {
                    batchDetailObj.put("quantity", quantity);
                    if (!isBatchForProduct) {
                        NewProductBatch productQuantity = (NewProductBatch) accountingHandlerDAO.getERPProductBatch(requestParams);
                        if (productQuantity != null) { // if not batch for product
                            if (productQuantity.getQuantitydue() < quantity) {
                                failureMsg += "Quantity is not available for product " + product.getName() + ". ";
                            }
                        } else {
                            failureMsg += "Quantity is not available for product " + product.getName() + ". ";
                        }
                    }
                }
            }
                 /*
                Validate and get batch detail object
                validate from warehouse,from location, Batch and quantity available or not
                */
                 if(isBatchForProduct || (!isBatchForProduct && isSerialForProduct)){
                if (columnConfig.containsKey("batchname")) {
                    batchName = recarr[(Integer) columnConfig.get("batchname")].replaceAll("\"", "").trim();
                    if (!StringUtil.isNullOrEmpty(batchName) && isBatchForProduct) {
                        requestParams.put("batch", batchName);
                    }
                    if (isBatchForProduct && StringUtil.isNullOrEmpty(batchName)) {
                        failureMsg += "Product ID '" + product.getProductid() + "' has batch activated. Please specify valid batch name. ";
                    }
                    productBatchObj = (NewProductBatch) accountingHandlerDAO.getERPProductBatch(requestParams);
                    if (productBatchObj != null && isBatchForProduct) {
                        if (quantity <= productBatchObj.getQuantitydue()) {
                            batchDetailObj.put("batch", productBatchObj.getBatchname());
                            batchDetailObj.put("batchName", productBatchObj.getBatchname());
                        } else {
                            failureMsg += "Quantity is not available for product  " + product.getName() + ". ";
                        }
                    }
                }
            }
              int serialCount=0;
              String finalSerial="";
              if(isSerialForProduct && columnConfig.containsKey("serialname")){
                  serialName=recarr[(Integer) columnConfig.get("serialname")].replaceAll("\"", "").trim();
                  if(StringUtil.isNullOrEmpty(serialName)){
                      failureMsg += "Product ID '" + product.getProductid() + "' has serial activated. Please specify valid serial number. ";
                  }else{
                      serialName = serialName.trim();
                      String[] serArr = serialName.split(",");
                      serialCount = serArr.length;
                      int duplicateCount = 0;
                      KwlReturnObject result = null;
                      if (serialCount != quantity) {
                          failureMsg += "Quantity and No. of Serial does not match. ";
                      }
                      for (int index = 0; index < serArr.length; index++) {
                          if (productBatchObj != null) {
                              result = accMasterItemsDAOobj.checkDuplicateSerialforProduct(product.getID(), productBatchObj.getId(), serArr[index].trim(), companyID);
                              duplicateCount = result.getRecordTotalCount();
                          } else {
                              result = accMasterItemsDAOobj.checkDuplicateSerialforProduct(product.getID(), "", serArr[index].trim(), companyID);
                              duplicateCount = result.getRecordTotalCount();
                          }

                          if (duplicateCount == 0) {
                              failureMsg += "Serial " + serArr[index] + " is not exists. ";
                          }
                          if (duplicateCount == 1) {
                              newBatchSerial = result.getEntityList().get(0) != null ? (NewBatchSerial) result.getEntityList().get(0) : null;
                          }
                          if (duplicateCount > 1) {
                              failureMsg += "Serial " + serArr[index] + "is already exists. ";
                          }
                          if (newBatchSerial != null) {
                              if (StringUtil.isNullOrEmpty(finalSerial)) {
                                  finalSerial = serArr[index];
                              } else {
                                  finalSerial += "," + serArr[index];
                              }

                          }

                      }
                      batchDetailObj.put("serialNames", finalSerial);
                }
                }
              
            if (StringUtil.isNullOrEmpty(failureMsg)) {
                returnMap.put("batchDetail", batchDetailObj);
            }

            returnMap.put("failureMsg", failureMsg);
         
              
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return returnMap;
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
        DateFormat dateFrmt = null;
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
            String scrapStore="";

            KwlReturnObject kmsg = null;

            KwlReturnObject extraCompanyPrefResult = accountingHandlerDAO.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraCompanyPrefResult.getEntityList().get(0);
            isInventoryIntegrationOn = extraCompanyPreferences.isActivateInventoryTab();

            JSONObject columnpref = null;
            if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                columnpref = new JSONObject(extraCompanyPreferences.getColumnPref());
            }
            if (columnpref != null) {
                scrapStore=columnpref.optString("scrapStore", "");
            }

            // CompanyAccountPreferences
            extraCompanyPrefResult = accountingHandlerDAO.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) extraCompanyPrefResult.getEntityList().get(0);

//            Date newUserDate = new Date();
            KwlReturnObject custumObjresult = accountingHandlerDAO.getObject(Company.class.getName(), companyid);
//            Company company1 = (Company) custumObjresult.getEntityList().get(0);
//            if (company1.getCreator() != null) {
//                newUserDate = authHandler.getUserNewDate(null, company1.getCreator().getTimeZone() != null ? company1.getCreator().getTimeZone().getDifference() : company1.getTimeZone().getDifference());
//            }
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
 
            List keyList = new ArrayList();
            List serKeyList=new ArrayList();
            
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
                        KwlReturnObject result = accProductDAO.getProductIDCount(productID, companyid, false);
                        int nocount = result.getRecordTotalCount();
                        if (nocount == 0) {
                            throw new AccountingException("productID '" + productID + "' not exists.");
                        }
                        Product product = (Product) result.getEntityList().get(0);
                        if (product != null) {
                            String key = productID;
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
                            Date bussinessDate = null;
                            boolean isValiddate = true;
                            if (columnConfig.containsKey("businessdate") && recarr.length > (Integer) columnConfig.get("businessdate")) {
                                String value = recarr[(Integer) columnConfig.get("businessdate")].replaceAll("\"", "").trim();
                                businessdate = value;

                                if (StringUtil.isNullOrEmpty(value)) {
                                    invalidColumns += " businessdate";
                                    throw new AccountingException(invalidColumns + " is not available.");
                                } else {
                                    try {
                                        bussinessDate = dateFrmt.parse(businessdate);
                                        isValiddate = isvalidDate(bussinessDate, companyAccountPreferences);
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
                            }
                            if (!isValiddate) {
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
                            String productRowId = "";
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
                            String productRackId = "";
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
                            String productBinId = "";
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
                            
                            
                            
                            String seqNumber = null;
                            if (columnConfig.containsKey("seqnumber")) {
                                seqNumber = recarr[(Integer) columnConfig.get("seqnumber")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(seqNumber)) {
                                    boolean isExist = seqService.isExistingSeqNumber(seqNumber, companyAccountPreferences.getCompany(), ModuleConst.STOCK_ADJUSTMENT);
                                    if (isExist) {
                                        throw new AccountingException("Sequence number already exist, please enter other one.");
                                    }
                                } else {
                                    throw new AccountingException("Please provide Sequence number.");
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
                            } else if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getInspectionStore()) && (extraCompanyPreferences.getInspectionStore().equals(productDefaultWarehouseID))) {
                                throw new AccountingException("warehouse is used as QA store, so can used for transaction.");
                            }else if(!StringUtil.isNullOrEmpty(extraCompanyPreferences.getRepairStore())&& (extraCompanyPreferences.getRepairStore().equals(productDefaultWarehouseID))) {
                                throw new AccountingException("warehouse is used as Repair store, so can used for transaction.");
                            }else if(!StringUtil.isNullOrEmpty(scrapStore) && scrapStore.equals(productDefaultWarehouseID)) {
                                throw new AccountingException("warehouse is used as Scrap store, so can used for transaction.");
                            }else if(!StringUtil.isNullOrEmpty(extraCompanyPreferences.getPackingstore()) && extraCompanyPreferences.getPackingstore().equals(productDefaultWarehouseID)) {
                                throw new AccountingException("warehouse is used as Pick Pack store, so can used for transaction.");
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
                                result = accJournalEntryDAO.getMasterItemByNameorID(companyid, reason, "31");
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

                            key += productDefaultWarehouseID + productDefaultLocationID + productRowId + productRackId + productBinId + batchName ;

                            if (keyList.contains(key)) {
                                throw new AccountingException("Already record exist for product");
                            } else {
                                keyList.add(key);
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
                            if (!("Stock IN".equalsIgnoreCase(type)) && (("Stock Sales").equalsIgnoreCase(type) || ("Stock OUT").equalsIgnoreCase(type))) {
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
                                        String serKey=productID+""+serArr[i];
                                        if (!serKeyList.contains(serKey)) {
                                            boolean isexist = stockDAO.isSerialExists(product, batchName, serArr[i]);
                                            List srrpList = stockService.getStockForPendingRepairSerial(product.getID(), batchName, serArr[i]);
                                            List srqaList = stockService.getStockForPendingApprovalSerial(product.getID(), batchName, serArr[i]);
                                            if (!isexist && (srrpList.size() > 0 || srqaList.size() > 0)) {
                                                isexist = true;
                                            }
                                            if (!isexist && !StringUtil.isNullOrEmpty(finalSerials)) {
                                                finalSerials += "," + serArr[i];
                                            } else if (!isexist) {
                                                finalSerials = serArr[i];
                                            }
                                            serKeyList.add(serKey);
                                        }else if(serKeyList.contains(serKey)){
                                             throw new AccountingException("Duplicate serial in file");
                                        }
                                    }
                                    if (StringUtil.isNullOrEmpty(finalSerials)) {
                                        throw new AccountingException("Already exist serial is not allowed");
                                    }
                                }
                            }

                            Inventory invetory = null;
                            if (quantity.length() > 0) {
                                JSONObject inventoryjson = new JSONObject();
                                double invIntialqty = Double.parseDouble(quantity);
                                if(invIntialqty<=0){
                                    throw new AccountingException("Quantity should be greater than 0");
                                }
                                double cost = Double.parseDouble(unitPrice);
                                if (isSerialForProduct) {
                                    String[] srArr = finalSerials.split(",");
                                    invIntialqty = srArr.length;
                                }
                                if (!("Stock IN".equalsIgnoreCase(type)) && !StringUtil.isNullOrEmpty(quantity)) {
                                    invIntialqty = Double.parseDouble(quantity);
                                    if (isSerialForProduct) {
                                        String[] srArr = finalSerials.split(",");
                                        invIntialqty = srArr.length;
                                    }
                                    invIntialqty = -1 * invIntialqty;
                                    cost = 0;
                                }
                                double prodInitPurchasePrice = 0;
                                KwlReturnObject initPurchasePriceObj = accProductDAO.getInitialPrice(product.getID(), true);

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
                                    jdata.put("purchaseprice", ("Stock IN".equalsIgnoreCase(type)) ? String.valueOf(cost) : prodInitPurchasePrice);
                                    jdata.put("product", product.getID());
                                    jdata.put("warehouse", productDefaultWarehouseID);
                                    jdata.put("uomid", "");
                                    jdata.put("reason", reason);
                                    jdata.put("memo", memo);
                                    jdata.put("businessdate", businessdate);
                                    jdata.put("seqno", seqNumber);
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
                                    if ("Stock IN".equalsIgnoreCase(type) && (isBatchForProduct || isSerialForProduct)) {
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

                    Map<String, Object> parmList = new HashMap<String, Object>();
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
                } else if (issuccess) {
                    saveImportLog(logDataMap);
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

    public void createFailureFiles(String filename, StringBuilder failedRecords, String ext) {
        String destinationDirectory;
        try {
            destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
            if (StringUtil.isNullOrEmpty(ext)) {
                ext = filename.substring(filename.lastIndexOf("."));
            }
            if (".xlsx".equals(ext)) {
                ext = ".xls";
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

    public JSONObject requestStockAdjustment(Map<String, Object> parmList) throws JSONException, AccountingException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SA_Tx_Save");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONObject jobjData = new JSONObject();
        try {

            HashMap<String, Object> logDataMap = (HashMap<String, Object>) parmList.get("logDataMap");
            jobjData = (JSONObject) parmList.get("returnObj");
            String companyid = parmList.get("companyid").toString();
            String userid = parmList.get("userId").toString();
            ProductBuild productbuild = null;
            if(parmList.containsKey("productbuild") && parmList.get("productbuild")!=null){
                productbuild = (ProductBuild)  parmList.get("productbuild");
            }
            boolean isfrombuildassembly =  parmList.containsKey("isfrombuildassembly") && parmList.get("isfrombuildassembly")!=null?Boolean.parseBoolean(parmList.get("isfrombuildassembly").toString()):false;
            String currencyId = parmList.get("currencyId").toString();
            Map<String, Object> jeDataMap = (HashMap<String, Object>) parmList.get("jeDataMap");
            Map<String, Object> jeDataMap1 = (HashMap<String, Object>) parmList.get("jeDataMap1");
            Locale locale = (Locale) parmList.get("locale");
            String masterPreference = (String) parmList.get("masterPreference");
            String qApproval = "false";
            Map<String, String> resnSet = new HashMap<>();
            String companyId = companyid;
            String userId = userid;
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);

            SimpleDateFormat df1 = null;
            if (parmList.get("dateformat") != null && !StringUtil.isNullOrEmpty(parmList.get("dateformat").toString())) {
                df1 = (SimpleDateFormat) parmList.get("dateformat");
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
                boolean isSkuforCompany = extraCompanyPreferences.isSKUFieldParm();
                Date bussinessDate = new Date();
                boolean sendForQAApproval = false;

                String records = jobjData.optString("data");
                String filename=jobjData.optString("filename");
                String adjustmentReason = "";

                String seqFormatId = "";

                JSONArray jArr = new JSONArray(records);
                SeqFormat seqFormat = null;
                SeqFormat assetSeqFormat = null;
                try {
                    assetSeqFormat = (isSkuforCompany && !isfrombuildassembly) ? seqService.getDefaultSeqFormat(company, ModuleConst.Asset_Module) : null;
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
                   // seqNo = seqService.getNextFormatedSeqNumber(seqFormat);
                    if (!seqExist) {
                        for (int i = 0; i < jArr.length(); i++) {
                            JSONObject jObj = jArr.optJSONObject(i);
                            String productId = jObj.optString("product");
                            adjustmentReason = jObj.optString("reason");
                            String businessdate = jObj.optString("businessdate");
                            seqNo = jObj.optString("seqno");
                            bussinessDate = df1.parse(businessdate);
                            String memo = jObj.optString("memo");
                            String warehouse = jObj.optString("warehouse");
                            String uomId = jObj.optString("uomid");
                            String remark = jObj.optString("remark",isfrombuildassembly? jObj.optString("remark") : "imported");
                            String throughFile = filename;
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
                            stockAdjustment.setThroughFile(throughFile);
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

                                        kwlObj = accCompanyPreferencesDAO.getSequenceFormat(JEFormatParams);
                                        SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                                        seqNumberMap = accCompanyPreferencesDAO.getNextAutoNumber_Modified(companyId, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, bussinessDate);
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

                                jeresult = accJournalEntryDAO.saveJournalEntry(jeDataMap); // Create Journal entry without JEdetails
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
                                KwlReturnObject jedresult = accJournalEntryDAO.addJournalEntryDetails(jedjson);
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
                                    jedresult = accJournalEntryDAO.addJournalEntryDetails(jedjson);
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
                                    jedresult = accJournalEntryDAO.addJournalEntryDetails(jedjson);
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
                                        kwlObj = accCompanyPreferencesDAO.getSequenceFormat(JEFormatParams);
                                        SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                                        seqNumberMap = accCompanyPreferencesDAO.getNextAutoNumber_Modified(companyId, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, bussinessDate);
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
                                jeresult = accJournalEntryDAO.saveJournalEntry(jeDataMap1); // Create Journal entry without JEdetails
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
                                KwlReturnObject jedresult = accJournalEntryDAO.addJournalEntryDetails(jedjson);
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
                                jedresult = accJournalEntryDAO.addJournalEntryDetails(jedjson);
                                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                jeDetails.add(jed);
                                inventoryJE.setDetails(jeDetails);
                                accJournalEntryDAO.saveJournalEntryDetailsSet(jeDetails);
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
                                if (qty < 0) {
                                    qty = (-1 * qty);
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

                                    if (product.isIsSKUForProduct() && (StringUtil.isNullOrEmpty(skuFields)) && adjustmentType.equalsIgnoreCase("Stock In")) {
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
                                        } else {
                                            throw new InventoryException(InventoryException.Type.NULL, "Sequence format for asset(sku) field is not set.");
                                        }
                                        tempTablMap.put("skufields", skuStr);
                                    }

                                    stockAdjustmentService.saveSADetailInTemporaryTable(product, fromStore, location, batchName, tempTablMap, df1);
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
                            if (productbuild != null && stockAdjustment != null) {
                                productbuild.setStockadjustment(stockAdjustment.getId());
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
                       // seqService.updateSeqNumber(seqFormat);
                    }
                }
                issuccess = true;
            }
            if (status != null) {
                if (logDataMap != null && logDataMap.get("StorageName") != null) {
                    saveImportLog(logDataMap);
                    String tableName = importDao.getTableName(logDataMap.get("StorageName").toString());
                    importDao.removeFileTable(tableName); // Remove table after importing all records
                }
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

    public String createCSVrecord(Object[] listArray) {
        String rec = "";
        for (int i = 0; i < listArray.length; i++) {    //Discard columns id at index 0 and isvalid,invalidColumns, validationlog at last 3 indexes.
//            String s = (listArray[i]==null)?"":listArray[i].toString();
            rec += "\"" + (listArray[i] == null ? "" : listArray[i].toString().replaceAll("\"", "")) + "\",";
        }
        return rec;
    }
    
    public String getCostCenterIDByName(String costCenterName, String companyID) throws AccountingException {
        String costCenterID = "";
        try {
            if (!StringUtil.isNullOrEmpty(costCenterName) && !StringUtil.isNullOrEmpty(companyID)) {
                HashMap<String, Object> requestParams = new HashMap<>();
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
            Logger.getLogger(accSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Cost Center.");
        }
        return costCenterID;
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
        String dateformateproduct = importHandler.getDFPattern();
        String masterPreference = requestParams.get("masterPreference").toString();
        String isQAActivated = jobj.getString("isQAActivated");
        String fileName = jobj.optString("filename");
        JSONObject returnObj = new JSONObject();
        DateFormat dateFrmt = null;
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
            String scrapStore="";

            KwlReturnObject kmsg = null;

            KwlReturnObject extraCompanyPrefResult = accountingHandlerDAO.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraCompanyPrefResult.getEntityList().get(0);
            isInventoryIntegrationOn = extraCompanyPreferences.isActivateInventoryTab();
//            Date newUserDate = new Date();
            KwlReturnObject custumObjresult = accountingHandlerDAO.getObject(Company.class.getName(), companyid);
            Company company1 = (Company) custumObjresult.getEntityList().get(0);

            extraCompanyPrefResult = accountingHandlerDAO.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) extraCompanyPrefResult.getEntityList().get(0);

            JSONObject columnpref = null;
            if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                columnpref = new JSONObject(extraCompanyPreferences.getColumnPref());
            }
            if (columnpref != null) {
                scrapStore=columnpref.optString("scrapStore", "");
            }

//            if (company1.getCreator() != null) {
//                newUserDate = authHandler.getUserNewDate(null, company1.getCreator().getTimeZone() != null ? company1.getCreator().getTimeZone().getDifference() : company1.getTimeZone().getDifference());
//            }
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

            List keyList = new ArrayList();
            List serKeyList=new ArrayList();

            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                String key = "";
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

                    try {

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
                        KwlReturnObject result = accProductDAO.getProductIDCount(productID, companyid, false);
                        int nocount = result.getRecordTotalCount();
                        if (nocount == 0) {
                            throw new AccountingException("productID '" + productID + "' not exists.");
                        }
                        Product product = (Product) result.getEntityList().get(0);
                        if (product != null) {

                            key = productID;
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
                            Date bussinessDate = null;
                            boolean isValiddate = true;
                            if (columnConfig.containsKey("businessdate") && recarr.size() > (Integer) columnConfig.get("businessdate")) {
                                Cell cell = row.getCell((Integer) columnConfig.get("businessdate"));
                                String value = importHandler.getCellValue(cell);
                                businessdate = value;
                                if (StringUtil.isNullOrEmpty(value)) {

                                    invalidColumns += " businessdate";
                                    throw new AccountingException(invalidColumns + " In valid " + invalidColumns + " format.");
                                } else {
                                    try {
                                        invalidColumns += " businessdate";
                                        bussinessDate = dateFrmt.parse(businessdate);
                                        isValiddate = isvalidDate(bussinessDate, companyAccountPreferences);
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
                            }
                            if (!isValiddate) {
//                                throw new AccountingException(invalidColumns + " In valid " +invalidColumns + " format.");
                                throw new AccountingException("Bussiness date should not be before than Book Beginning Date.");
                            }

                            String expdateStr = "";
                            if ("Stock IN".equalsIgnoreCase(type) && (isBatchForProduct || isSerialForProduct)) {
                                if (columnConfig.containsKey("expdate")) {
                                    Cell cell = row.getCell((Integer) columnConfig.get("expdate"));
                                    expdateStr = cell != null ? importHandler.getCellValue(cell) : "";
                                    if (!StringUtil.isNullOrEmptyWithTrim(expdateStr)) {
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
                            String productRowId = "";
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
                            String productRackId = "";
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
                            String productBinId = "";
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
                            
                            String seqNumber = null;
                            if (columnConfig.containsKey("seqnumber")) {
                                Cell cell = row.getCell((Integer) columnConfig.get("seqnumber"));
                                if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
                                    invalidColumn.put((Integer) columnConfig.get("seqnumber"), "Invalid");
                                    failureMsg += "sequence number is not available.";
                                } else {
                                    seqNumber = importHandler.getCellValue(cell);
                                }
                                if (!StringUtil.isNullOrEmpty(seqNumber)) {
                                    boolean isExist = seqService.isExistingSeqNumber(seqNumber, companyAccountPreferences.getCompany(), ModuleConst.STOCK_ADJUSTMENT);
                                    if (isExist) {
                                        throw new AccountingException("Sequence number already exist.");
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
                            }else if(!StringUtil.isNullOrEmpty(extraCompanyPreferences.getInspectionStore()) && extraCompanyPreferences.getInspectionStore().equals(productDefaultWarehouseID)){
                                throw new AccountingException("warehouse is used as QA store, so can used for transaction.");
                            }else if(!StringUtil.isNullOrEmpty(extraCompanyPreferences.getRepairStore())&& (extraCompanyPreferences.getRepairStore().equals(productDefaultWarehouseID))) {
                                throw new AccountingException("warehouse is used as Repair store, so can used for transaction.");
                            }else if(!StringUtil.isNullOrEmpty(scrapStore) && scrapStore.equals(productDefaultWarehouseID)) {
                                throw new AccountingException("warehouse is used as Scrap store, so can used for transaction.");
                            }else if(!StringUtil.isNullOrEmpty(extraCompanyPreferences.getPackingstore()) && extraCompanyPreferences.getPackingstore().equals(productDefaultWarehouseID)) {
                                throw new AccountingException("warehouse is used as Pick Pack store, so can used for transaction.");
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
                                result = accJournalEntryDAO.getMasterItemByNameorID(companyid, reason, "31");
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

                            key += productDefaultWarehouseID + productDefaultLocationID + productRowId + productRackId +productBinId + batchName ;
                            if (keyList.contains(key)) {
                                throw new AccountingException("Duplicate records found in this file.");
                            } else {
                                keyList.add(key);
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
                                            throw new AccountingException("Product Quantity is not available");
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
                            if (!("Stock IN".equalsIgnoreCase(type))) {
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
                                            } else {
                                                throw new AccountingException("serial" + serArr[j] + " is not avialble ");
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
                                        String serKey=productID+""+serArr[i];
                                        if (!serKeyList.contains(serKey)) {
                                            boolean isexist = stockDAO.isSerialExists(product, batchName, serArr[k]);
                                            List srrpList = stockService.getStockForPendingRepairSerial(product.getID(), batchName, serArr[k]);
                                            List srqaList = stockService.getStockForPendingApprovalSerial(product.getID(), batchName, serArr[k]);
                                            if (!isexist && (srrpList.size() > 0 || srqaList.size() > 0)) {
                                                isexist = true;
                                            }
                                            if (!isexist && !StringUtil.isNullOrEmpty(finalSerials)) {
                                                finalSerials += "," + serArr[k];
                                            } else if (!isexist) {
                                                finalSerials = serArr[k];
                                            } else if (isexist) {
                                                throw new AccountingException("Already exist serial " + serArr[k] + "  is not allowed");
                                            }
                                            serKeyList.add(serKey);
                                        }else if(serKeyList.contains(serKey)){
                                             throw new AccountingException("Duplicate serial in file");
                                        }
                                    }
                                    if (StringUtil.isNullOrEmpty(finalSerials)) {
                                        throw new AccountingException("Already exist serial is not allowed");
                                    }
                                }
                            }

                            Inventory invetory = null;
                            if (quantity.length() > 0) {
                                JSONObject inventoryjson = new JSONObject();
                                double invIntialqty = Double.parseDouble(quantity);
                                if(invIntialqty<=0){
                                    throw new AccountingException("Quantity should be greater than 0");
                                }
                                double cost = Double.parseDouble(unitPrice);
                                if (isSerialForProduct) {
                                    String[] srArr = finalSerials.split(",");
                                    invIntialqty = srArr.length;
                                }
                                if (!("Stock IN".equalsIgnoreCase(type)) && !StringUtil.isNullOrEmpty(quantity)) {
                                    invIntialqty = Double.parseDouble(quantity);
                                    if (isSerialForProduct) {
                                        String[] srArr = finalSerials.split(",");
                                        invIntialqty = srArr.length;
                                    }
                                    invIntialqty = -1 * invIntialqty;
                                    cost = 0;
                                }
                                double prodInitPurchasePrice = 0;
                                KwlReturnObject initPurchasePriceObj = accProductDAO.getInitialPrice(product.getID(), true);

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
                                    jdata.put("purchaseprice", ("Stock IN".equalsIgnoreCase(type)) ? String.valueOf(cost) : prodInitPurchasePrice);
                                    jdata.put("product", product.getID());
                                    jdata.put("warehouse", productDefaultWarehouseID);
                                    jdata.put("uomid", "");
                                    jdata.put("reason", reason);
                                    jdata.put("memo", memo);
                                    jdata.put("businessdate", businessdate);
                                    jdata.put("seqno", seqNumber);
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
                                    if ("Stock IN".equalsIgnoreCase(type) && (isBatchForProduct || isSerialForProduct)) {
                                        detailObj.put("expdate", expdateStr);
                                    }

                                    JSONArray dtlArr = new JSONArray();
                                    dtlArr.put(detailObj);

                                    jdata.put("stockDetails", dtlArr);

                                    jarr.put(jdata);

                                }
                            }

                        }

                    } catch (Exception ex) {
                        failed++;
                        String errorMsg = ex.getMessage(), invalidColumns = "";
                        try {
                            JSONObject errorLog = new JSONObject(errorMsg);
                            errorMsg = errorLog.getString("errorMsg");
                            invalidColumns = errorLog.getString("invalidColumns");
                        } catch (Exception jex) {
                        }

                        errorMsg = !StringUtil.isNullOrEmpty(errorMsg) ? errorMsg : " ";
                        failedRecords.append("\n" + createCSVrecord(recarr.toArray()) + "\"" + errorMsg.replaceAll("\"", "") + "\"");
                    }
                    total++;
                } else {
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

        } catch (Exception ex) {
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

                    Map<String, Object> parmList = new HashMap<String, Object>();
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
                } else if (issuccess) {
                    saveImportLog(logDataMap);
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

    public void saveImportLog(HashMap<String, Object> logDataMap) throws ServiceException, DataInvalidateException {
        if (!StringUtil.isNullOrEmpty(logDataMap.get("Id").toString())) {
            String Id = logDataMap.get("Id").toString();
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(ImportLog.class.getName(), Id);
            ImportLog importLog = (ImportLog) jeresult.getEntityList().get(0);

            importLog.setFileName(logDataMap.get("FileName").toString());
            importLog.setLog(logDataMap.get("Log").toString());
            importLog.setRejected(Integer.parseInt(logDataMap.get("Rejected").toString()));
            importLog.setTotalRecs(Integer.parseInt(logDataMap.get("TotalRecs").toString()));
            importLog.setTotalRecs(Integer.parseInt(logDataMap.get("TotalRecs").toString()));
            importLog.setFailureFileType(logDataMap.get("Type").toString());
            importDao.saveorupdateObject(importLog);
        }
    }
    
    
     public void saveIstImportLog(HashMap<String,Object>logDataMap) {
        DefaultTransactionDefinition ldef = new DefaultTransactionDefinition();
        ldef.setName("import_Tx");
        ldef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus lstatus = txnManager.getTransaction(ldef);

        try {
            
            importDao.saveImportLog(logDataMap);
            txnManager.commit(lstatus);
        } catch (ServiceException | DataInvalidateException | TransactionException ex) {
            txnManager.rollback(lstatus);
            Logger.getLogger(ImportInvData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
   public boolean isFileInProcess(String companyID) throws ServiceException, JSONException, ParseException {

        boolean isPending = false;
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        Date dt=new Date();
        String currDate=sdf.format(dt);
        dt=sdf.parse(currDate);

       requestParams.put("startdate", dt);
//       requestParams.put("enddate", dt);
       requestParams.put("companyid", companyID);
       requestParams.put("module", 1115);
       requestParams.put("start", "0");
       requestParams.put("limit", "30");

        KwlReturnObject result = importDao.getImportLogForDate(requestParams);
        List list = result.getEntityList();
        DateFormat df = authHandler.getGlobalDateFormat();
        JSONArray jArr = new JSONArray();
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            ImportLog ilog = (ImportLog) itr.next();
            JSONObject jtemp = new JSONObject();
            jtemp.put("id", ilog.getId());
            jtemp.put("filename", ilog.getFileName());
            jtemp.put("storename", ilog.getStorageName());
            jtemp.put("failurename", ilog.getStorageName() != null ? ilog.getFailureFileName() : "");
            jtemp.put("log", ilog.getLog());
            if (ilog.getLog().equalsIgnoreCase("Pending")) {
                isPending = true;
                return true;
            }
        }
        return isPending;
    }
}
